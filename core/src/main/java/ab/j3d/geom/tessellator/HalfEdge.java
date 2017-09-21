/*
 * License Applicability. Except to the extent portions of this file are made
 * subject to an alternative license as permitted in the SGI Free Software
 * License B, Version 1.1 (the "License"), the contents of this file are subject
 * only to the provisions of the License. You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at Silicon
 * Graphics, Inc., attn: Legal Services, 1600 Amphitheatre Parkway, Mountain
 * View, CA 94043-1351, or at:
 *
 * http://oss.sgi.com/projects/FreeB
 *
 * Note that, as provided in the License, the Software is distributed on an
 * "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 * DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 *
 * NOTE: The Original Code (as defined below) has been licensed under the SGI
 * Free Software License B (Version 1.1), shown above ("SGI License"). Pursuant
 * to Section 3.2(3) of the SGI License, the Covered Code, is distributed as
 * in modified form as part of the AsoBrain 3D Toolkit, which is licensed under
 * an alternative license, the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. This alternative license applies to all code that
 * is not part of the "Original Code" (as defined below) and is
 * Copyright (C) 1999-2010 Peter S. Heijnen. You may obtain a copy of the
 * GNU Lesser General Public License from the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Original Code. The Original Code is: OpenGL Sample Implementation,
 * Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 * Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 * Copyright in any portions created by third parties is as indicated elsewhere
 * herein. All Rights Reserved.
 *
 * Author: Eric Veach, July 1994
 * Java Port: Pepijn Van Eeckhoudt, July 2003
 * Java Port: Nathan Parker Burg, August 2003
 * AsoBrain3D Port: Peter S. Heijnen, December 2010
 */
package ab.j3d.geom.tessellator;

import org.jetbrains.annotations.*;

class HalfEdge
{
	/**
	 * doubly-linked list (prev==Sym->next)
	 */
	public HalfEdge next;

	/**
	 * same edge, opposite direction
	 */
	public HalfEdge symmetric;

	/**
	 * next edge CCW around origin
	 */
	public HalfEdge ccwAroundOrigin;

	/**
	 * next edge CCW around left face
	 */
	public HalfEdge ccwAroundLeftFace;

	/**
	 * Origin vertex (Overtex too long)
	 */
	public Vertex origin;

	/**
	 * left face
	 */
	public Face leftFace;

	/* Internal data (keep hidden) */

	/**
	 * a region with this upper edge (sweep.c)
	 */
	public Region activeRegion;

	/**
	 * change in winding number when crossing
	 */
	public int winding;

	public boolean first;

	HalfEdge( final boolean first )
	{
		this.first = first;
	}

	boolean goesLeft()
	{
		return Geom.vertLeq( symmetric.origin, origin );
	}

	boolean goesRight()
	{
		return Geom.vertLeq( origin, symmetric.origin );
	}

	/*
	 * Splits this edge into two edges. This edge and a new edge, such that new
	 * {@link HalfEdge} == this.{@link #eOrg.Lnext.  The new vertex is eOrg.Sym.Org == eNew.Org.
	 * eOrg and eNew will have the same left face.
	 */
	@NotNull
	HalfEdge split()
	{
		final HalfEdge tempHalfEdge = makeEdge( this );
		final HalfEdge eNewSym = tempHalfEdge.symmetric;

		/* Connect the new edge appropriately */
		spliceEdge( tempHalfEdge, ccwAroundLeftFace );

		/* Set the vertex and face information */
		tempHalfEdge.origin = symmetric.origin;
		Mesh.makeVertex( eNewSym, tempHalfEdge.origin );
		tempHalfEdge.leftFace = leftFace;
		eNewSym.leftFace = leftFace;

		final HalfEdge result = tempHalfEdge.symmetric;

		/* Disconnect eOrg from eOrg.Sym.Org and connect it to eNew.Org */
		spliceEdge( symmetric, symmetric.symmetric.ccwAroundLeftFace );
		spliceEdge( symmetric, result );

		/* Set the vertex and face information */
		symmetric.origin = result.origin;
		result.symmetric.origin.anEdge = result.symmetric;	/* may have pointed to eOrg.Sym */
		result.symmetric.leftFace = symmetric.leftFace;
		result.winding = winding;	/* copy old winding information */
		result.symmetric.winding = symmetric.winding;

		return result;
	}

	/**
	 * MakeEdge creates a new pair of half-edges which form their own loop.
	 * No vertex or face structures are allocated, but these must be assigned
	 * before the current edge operation is completed.
	 */
	@NotNull
	static HalfEdge makeEdge( HalfEdge eNext )
	{
		final HalfEdge e;
		final HalfEdge eSym;
		final HalfEdge ePrev;

		e = new HalfEdge( true );
		eSym = new HalfEdge( false );

		/* Make sure eNext points to the first edge of the edge pair */
		if ( !eNext.first )
		{
			eNext = eNext.symmetric;
		}

		/* Insert in circular doubly-linked list before eNext.
		 * Note that the prev pointer is stored in Sym->next.
		 */
		ePrev = eNext.symmetric.next;
		eSym.next = ePrev;
		ePrev.symmetric.next = e;
		e.next = eNext;
		eNext.symmetric.next = eSym;

		e.symmetric = eSym;
		e.ccwAroundOrigin = e;
		e.ccwAroundLeftFace = eSym;
		e.origin = null;
		e.leftFace = null;
		e.winding = 0;
		e.activeRegion = null;

		eSym.symmetric = e;
		eSym.ccwAroundOrigin = eSym;
		eSym.ccwAroundLeftFace = e;
		eSym.origin = null;
		eSym.leftFace = null;
		eSym.winding = 0;
		eSym.activeRegion = null;

		return e;
	}

	/**
	 * Removes this edge. There are several cases:
	 *
	 * if (Lface != Rface), we join two loops into one; the loop Lface is
	 * deleted.  Otherwise, we are splitting one loop into two; the newly
	 * created loop will contain Dst. If the deletion of would create isolated
	 * vertices, those are deleted as well.
	 */
	void delete()
	{
		final HalfEdge eDelSym = symmetric;
		boolean joiningLoops = false;

		/* First step: disconnect the origin vertex eDel.Org.  We make all
		 * changes to get a consistent mesh in this "intermediate" state.
		 */
		if ( leftFace != symmetric.leftFace )
		{
			/* We are joining two loops into one -- remove the left face */
			joiningLoops = true;
			Mesh.killFace( leftFace, symmetric.leftFace );
		}

		if ( ccwAroundOrigin == this )
		{
			Mesh.killVertex( origin, null );
		}
		else
		{
			/* Make sure that eDel.Org and eDel.Sym.Lface point to valid half-edges */
			symmetric.leftFace.anEdge = symmetric.ccwAroundLeftFace;
			origin.anEdge = ccwAroundOrigin;

			spliceEdge( this, symmetric.ccwAroundLeftFace );
			if ( !joiningLoops )
			{

				/* We are splitting one loop into two -- create a new loop for eDel. */
				makeFace( this, leftFace );
			}
		}

		/* Claim: the mesh is now in a consistent state, except that eDel.Org
		 * may have been deleted.  Now we disconnect eDel.Dst.
		 */
		if ( eDelSym.ccwAroundOrigin == eDelSym )
		{
			Mesh.killVertex( eDelSym.origin, null );
			Mesh.killFace( eDelSym.leftFace, null );
		}
		else
		{
			/* Make sure that eDel.Dst and eDel.Lface point to valid half-edges */
			leftFace.anEdge = eDelSym.symmetric.ccwAroundLeftFace;
			eDelSym.origin.anEdge = eDelSym.ccwAroundOrigin;
			spliceEdge( eDelSym, eDelSym.symmetric.ccwAroundLeftFace );
		}

		/*
		 * Any isolated vertices or faces have already been freed, so we can now
		 * remove this edge from the circular double-linked list
		 */
		final HalfEdge firstEdge = first ? this : symmetric;
		final HalfEdge eNext = firstEdge.next;
		final HalfEdge ePrev = firstEdge.symmetric.next;
		eNext.symmetric.next = ePrev;
		ePrev.symmetric.next = eNext;
	}

	/**
	 * Attaches a new face and makes it the left face of all edges in the face
	 * loop to which eOrig belongs.  "fNext" gives a place to insert the new
	 * face in the global face list.  We insert the new face *before* fNext so
	 * that algorithms which walk the face list will not see the newly created
	 * faces.
	 */
	static void makeFace( final HalfEdge eOrig, final Face fNext )
	{
		final Face result = new Face();
		HalfEdge e;
		final Face fPrev;

		/* insert in circular doubly-linked list before fNext */
		fPrev = fNext.prev;
		result.prev = fPrev;
		fPrev.next = result;
		result.next = fNext;
		fNext.prev = result;

		result.anEdge = eOrig;
		result.renderStack = null;
		result.rendered = false;

		/*
		 * The new face is marked "inside" if the old one was.  This is a
		 * convenience for the common case where a face has been split in two.
		 */
		result.inside = fNext.inside;

		/* fix other edges on this face loop */
		e = eOrig;
		do
		{
			e.leftFace = result;
			e = e.ccwAroundLeftFace;
		}
		while ( e != eOrig );
	}

	/**
	 * Splice( a, b ) is best described by the Guibas/Stolfi paper. Basically it
	 * modifies the mesh so that a->Onext and b->Onext are exchanged. This can
	 * have various effects depending on whether a and b belong to different
	 * face or vertex rings.
	 *
	 * @param   a   First edge.
	 * @param   b   Second edge.
	 */
	static void spliceEdge( final HalfEdge a, final HalfEdge b )
	{
		final HalfEdge aOnext = a.ccwAroundOrigin;
		final HalfEdge bOnext = b.ccwAroundOrigin;

		aOnext.symmetric.ccwAroundLeftFace = b;
		bOnext.symmetric.ccwAroundLeftFace = a;
		a.ccwAroundOrigin = bOnext;
		b.ccwAroundOrigin = aOnext;
	}

	/**
	 * Creates a new edge from eOrg.Sym.Org
	 * to eDst.Org, and returns the corresponding half-edge eNew.
	 * If eOrg.Lface == eDst.Lface, this splits one loop into two,
	 * and the newly created loop is eNew.Lface.  Otherwise, two disjoint
	 * loops are merged into one, and the loop eDst.Lface is destroyed.
	 * <p/>
	 * If (eOrg == eDst), the new face will have only two edges.
	 * If (eOrg.Lnext == eDst), the old face is reduced to a single edge.
	 * If (eOrg.Lnext.Lnext == eDst), the old face is reduced to two edges.
	 */
	@NotNull
	static HalfEdge connectEdges( final HalfEdge eOrg, final HalfEdge eDst )
	{
		final HalfEdge eNewSym;
		boolean joiningLoops = false;
		final HalfEdge result = makeEdge( eOrg );

		eNewSym = result.symmetric;

		if ( eDst.leftFace != eOrg.leftFace )
		{
			/* We are connecting two disjoint loops -- destroy eDst.Lface */
			joiningLoops = true;
			Mesh.killFace( eDst.leftFace, eOrg.leftFace );
		}

		/* Connect the new edge appropriately */
		spliceEdge( result, eOrg.ccwAroundLeftFace );
		spliceEdge( eNewSym, eDst );

		/* Set the vertex and face information */
		result.origin = eOrg.symmetric.origin;
		eNewSym.origin = eDst.origin;
		result.leftFace = eNewSym.leftFace = eOrg.leftFace;

		/* Make sure the old face points to a valid half-edge */
		eOrg.leftFace.anEdge = eNewSym;

		if ( !joiningLoops )
		{

			/* We split one loop into two -- the new loop is eNew.Lface */
			makeFace( result, eOrg.leftFace );
		}

		return result;
	}
}
