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

class MonotoneTessellator
{
	/**
	 * Tessellates each region of the mesh which is marked "inside" the
	 * polygon.  Each such region must be monotone.
	 *
	 * @param   mesh    Mesh to tessellate (each region must be monotone).
	 */
	public static void tessellateInterior( final Mesh mesh )
	{
		Face next;

		for ( Face f = mesh._faceListHead.next; f != mesh._faceListHead; f = next )
		{
			/* Make sure we don''t try to tessellate the new triangles. */
			next = f.next;
			if ( f.inside )
			{
				tessellateMonotoneRegion( f );
			}
		}
	}

	/**
	 * Tessellates a monotone region (what else would it do??). The region must
	 * consist of a single loop of half-edges oriented CCW. "Monotone" in this
	 * case means that any vertical line intersects the interior of the region
	 * in a single interval.
	 *
	 * Tessellation consists of adding interior edges (actually pairs of
	 * half-edges), to split the region into non-overlapping triangles.
	 *
	 * The basic idea is explained in Preparata and Shamos (which I don''t
	 * have handy right now), although their implementation is more
	 * complicated than this one.  The are two edge chains, an upper chain
	 * and a lower chain.  We process all vertices from both chains in order,
	 * from right to left.
	 *
	 * The algorithm ensures that the following invariant holds after each
	 * vertex is processed: the untessellated region consists of two chains,
	 * where one chain (say the upper) is a single edge, and the other chain is
	 * concave.  The left vertex of the single edge is always to the left of all
	 * vertices in the concave chain.
	 *
	 * Each step consists of adding the rightmost unprocessed vertex to one of
	 * the two chains, and forming a fan of triangles from the rightmost of two
	 * chain endpoints.  Determining whether we can add each triangle to the fan
	 * is a simple orientation test.  By making the fan as large as possible, we
	 * restore the invariant (check it yourself).
	 *
	 * @param   face    Face representing monotone region.
	 */
	private static void tessellateMonotoneRegion( final Face face )
	{
		HalfEdge up;
		HalfEdge lo;

		/* All edges are oriented CCW around the boundary of the region.
		 * First, find the half-edge whose origin vertex is rightmost.
		 * Since the sweep goes from left to right, face->anEdge should
		 * be close to the edge we want.
		 */
		up = face.anEdge;
		assert ( up.ccwAroundLeftFace != up && up.ccwAroundLeftFace.ccwAroundLeftFace != up );

		while ( Geom.vertLeq( up.symmetric.origin, up.origin ) )
		{
			up = up.ccwAroundOrigin.symmetric;
		}

		while ( Geom.vertLeq( up.origin, up.symmetric.origin ) )
		{
			up = up.ccwAroundLeftFace;
		}

		lo = up.ccwAroundOrigin.symmetric;

		while ( up.ccwAroundLeftFace != lo )
		{
			if ( Geom.vertLeq( up.symmetric.origin, lo.origin ) )
			{
				/* up.Sym.Org is on the left.  It is safe to form triangles from lo.Org.
				 * The EdgeGoesLeft test guarantees progress even when some triangles
				 * are CW, given that the upper and lower chains are truly monotone.
				 */
				while ( lo.ccwAroundLeftFace != up && ( lo.ccwAroundLeftFace.goesLeft()
					|| Geom.edgeSign( lo.origin, lo.symmetric.origin, lo.ccwAroundLeftFace.symmetric.origin ) <= 0 ) )
				{
					final HalfEdge tempHalfEdge = HalfEdge.connectEdges( lo.ccwAroundLeftFace, lo );
					lo = tempHalfEdge.symmetric;
				}
				lo = lo.ccwAroundOrigin.symmetric;
			}
			else
			{
				/* lo.Org is on the left.  We can make CCW triangles from up.Sym.Org. */
				while ( lo.ccwAroundLeftFace != up && ( up.ccwAroundOrigin.symmetric.goesRight()
					|| Geom.edgeSign( up.symmetric.origin, up.origin, up.ccwAroundOrigin.symmetric.origin ) >= 0 ) )
				{
					final HalfEdge tempHalfEdge = HalfEdge.connectEdges( up, up.ccwAroundOrigin.symmetric );
					up = tempHalfEdge.symmetric;
				}
				up = up.ccwAroundLeftFace;
			}
		}

		/* Now lo.Org == up.Sym.Org == the leftmost vertex.  The remaining region
		 * can be tessellated in a fan from this leftmost vertex.
		 */
		assert ( lo.ccwAroundLeftFace != up );
		while ( lo.ccwAroundLeftFace.ccwAroundLeftFace != up )
		{
			final HalfEdge tempHalfEdge = HalfEdge.connectEdges( lo.ccwAroundLeftFace, lo );
			lo = tempHalfEdge.symmetric;
		}
	}
}
