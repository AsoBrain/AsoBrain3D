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

import ab.j3d.*;
import ab.j3d.geom.tessellator.PriorityQ.*;

/**
 * References:
 * <ul>
 *  <li><a href="http://en.wikipedia.org/wiki/Delaunay_triangulation">Wikipedia: Delaunay triangulation</a></li>
 *  <li><a href="http://www.cs.cmu.edu/~quake/tripaper/triangle0.html">Triangle: Engineering a 2D Quality Mesh Generator and Delaunay Triangulator</a></li>
 * </ul>
 *
 * Invariants for the Mesh (the processed portion).
 * <ul>
 *  <li>the portion of the mesh left of the sweep line is a planar graph,
 *   ie. there is *some* way to embed it in the plane</li>
 *  <li>no processed edge has zero length</li>
 *  <li>no two processed vertices have identical coordinates</li>
 *  <li>each "inside" region is monotone, ie. can be broken into two chains
 *   of monotonically increasing vertices according to VertLeq(v1,v2)</li>
 *  <li>a non-invariant: these chains may intersect (very slightly)</li>
 * </ul>
 *
 * Invariants for the Sweep.
 * <ul>
 *  <li>if none of the edges incident to the event vertex have an activeRegion
 *   (ie. none of these edges are in the edge dictionary), then the vertex
 *   has only right-going edges.</li>
 *  <li>if an edge is marked "fixUpperEdge" (it is a temporary edge introduced
 *   by ConnectRightVertex), then it is the only right-going edge from
 *   its associated vertex.  (This says that these edges exist only
 *   when it is necessary.)</li>
 * </ul>
 */
class Sweep
{
	public static final double MAX_COORD = 1.0e150;

	/**
	 * Because vertices at exactly the same location are merged together
	 * before we process the sweep event, some degenerate cases can't occur.
	 * However if someone eventually makes the modifications required to
	 * merge features which are close together, the cases below marked
	 * TOLERANCE_NONZERO will be useful.  They were debugged before the
	 * code to merge identical vertices in the main loop was added.
	 */
	private static final boolean TOLERANCE_NONZERO = false;

	/**
	 * Make the sentinel coordinates big enough that they will never be
	 * merged with real input features.  (Even with the largest possible
	 * input contour and the maximum tolerance of 1.0, no merging will be
	 * done with coordinates larger than 3 * MAX_COORD).
	 */
	private static final double SENTINEL_COORD = ( 4.0 * MAX_COORD );

	private static final Comparator PRIORITY_Q_LEQ = new Comparator()
	{
		@Override
		public boolean leq( final Object key1, final Object key2 )
		{
			return Geom.vertLeq( ( (Vertex) key1 ), (Vertex) key2 );
		}
	};

	/**
	 * Mesh that is processed.
	 */
	private final Mesh _mesh;

	/**
	 * Edge dictionary for sweep line.
	 * <p>
	 * Invariants for the Edge Dictionary.
	 * <ul>
	 *  <li>each pair of adjacent edges e2=Succ(e1) satisfies EdgeLeq(e1,e2)
	 *   at any valid location of the sweep event</li>
	 *  <li>if EdgeLeq(e2,e1) as well (at any valid sweep event), then e1 and e2
	 *   share a common endpoint</li>
	 *  <li>for each e, e.Dst has been processed, but not e.Org</li>
	 *  <li>each edge e satisfies VertLeq(e.Dst,event) && VertLeq(event,e.Org)
	 *   where "event" is the current sweep line event.</li>
	 *  <li>no edge e has zero length</li>
	 * </ul>
	 */
	private RegionDict _dict;

	/**
	 * Priority queue of vertex events
	 */
	private PriorityQ _pq;

	/**
	 * Current sweep event being processed
	 */
	Vertex _event;

	/**
	 * Create sweep.
	 *
	 * @param   mesh    Mesh that is processed.
	 */
	Sweep( final Mesh mesh )
	{
		_mesh = mesh;
	}

	/**
	 * When we merge two edges into one, we need to compute the combined
	 * winding of the new edge.
	 */
	private void addWinding( final HalfEdge eDst, final HalfEdge eSrc )
	{
		eDst.winding += eSrc.winding;
		eDst.symmetric.winding += eSrc.symmetric.winding;
	}

	private Region regionBelow( final Region r )
	{
		return r.nodeUp.prev.key;
	}

	private Region regionAbove( final Region r )
	{
		return r.nodeUp.next.key;
	}

	void deleteRegion( final Region reg )
	{
		if ( reg.fixUpperEdge )
		{
			/* It was created with zero winding number, so it better be
			 * deleted with zero winding number (ie. it better not get merged
			 * with a real edge).
			 */
			assert ( reg.upperEdge.winding == 0 );
		}
		reg.upperEdge.activeRegion = null;
		_dict.delete( reg.nodeUp );
	}

	/**
	 * Replace an upper edge which needs fixing (see ConnectRightVertex).
	 */
	private void fixUpperEdge( final Region reg, final HalfEdge newEdge )
	{
		assert ( reg.fixUpperEdge );
		reg.upperEdge.delete();
		reg.fixUpperEdge = false;
		reg.upperEdge = newEdge;
		newEdge.activeRegion = reg;
	}

	Region topLeftRegion( Region reg )
	{
		final Vertex org = reg.upperEdge.origin;
		final HalfEdge e;

		/* Find the region above the uppermost edge with the same origin */
		do
		{
			reg = regionAbove( reg );
		}
		while ( reg.upperEdge.origin == org );

		/* If the edge above was a temporary edge introduced by ConnectRightVertex,
		 * now is the time to fix it.
		 */
		if ( reg.fixUpperEdge )
		{
			e = HalfEdge.connectEdges( regionBelow( reg ).upperEdge.symmetric, reg.upperEdge.ccwAroundLeftFace );
			fixUpperEdge( reg, e );
			reg = regionAbove( reg );
		}
		return reg;
	}

	Region topRightRegion( Region reg )
	{
		final Vertex dst = reg.upperEdge.symmetric.origin;

		/* Find the region above the uppermost edge with the same destination */
		do
		{
			reg = regionAbove( reg );
		}
		while ( reg.upperEdge.symmetric.origin == dst );
		return reg;
	}

	/**
	 * Add a new active region to the sweep line, *somewhere* below "regAbove"
	 * (according to where the new edge belongs in the sweep-line dictionary).
	 * The upper edge of the new region will be "eNewUp".
	 * Winding number and "inside" flag are not updated.
	 */
	Region addRegionBelow( final Region regAbove, final HalfEdge eNewUp )
	{
		final Region regNew = new Region();

		regNew.upperEdge = eNewUp;
		regNew.nodeUp = _dict.insertBefore( regAbove.nodeUp, regNew );
		regNew.fixUpperEdge = false;
		regNew.sentinel = false;
		regNew.dirty = false;

		eNewUp.activeRegion = regNew;
		return regNew;
	}

	void computeWinding( final Region reg )
	{
		reg.windingNumber = regionAbove( reg ).windingNumber + reg.upperEdge.winding;
		reg.inside = _mesh.windingRule.isInside( reg.windingNumber );
	}

	/**
	 * Delete a region from the sweep line.  This happens when the upper
	 * and lower chains of a region meet (at a vertex on the sweep line).
	 * The "inside" flag is copied to the appropriate mesh face (we could
	 * not do this before -- since the structure of the mesh is always
	 * changing, this face may not have even existed until now).
	 */
	void finishRegion( final Region reg )
	{
		final HalfEdge e = reg.upperEdge;
		final Face f = e.leftFace;

		f.inside = reg.inside;
		f.anEdge = e; /* optimization for {@link tessellateMonotoneRegion#tessellateMonotoneRegion} */
		deleteRegion( reg );
	}

	/**
	 * We are given a vertex with one or more left-going edges.  All affected
	 * edges should be in the edge dictionary.  Starting at regFirst.eUp,
	 * we walk down deleting all regions where both edges have the same
	 * origin vOrg.  At the same time we copy the "inside" flag from the
	 * active region to the face, since at this point each face will belong
	 * to at most one region (this was not necessarily true until this point
	 * in the sweep).  The walk stops at the region above regLast; if regLast
	 * is null we walk as far as possible.  At the same time we relink the
	 * mesh if necessary, so that the ordering of edges around vOrg is the
	 * same as in the dictionary.
	 */
	HalfEdge finishLeftRegions( final Region regFirst, final Region regLast )
	{
		Region reg;
		Region regPrev;
		HalfEdge e;
		HalfEdge ePrev;

		regPrev = regFirst;
		ePrev = regFirst.upperEdge;
		while ( regPrev != regLast )
		{
			regPrev.fixUpperEdge = false;	/* placement was OK */
			reg = regionBelow( regPrev );
			e = reg.upperEdge;
			if ( e.origin != ePrev.origin )
			{
				if ( !reg.fixUpperEdge )
				{
					/* Remove the last left-going edge.  Even though there are no further
					 * edges in the dictionary with this origin, there may be further
					 * such edges in the mesh (if we are adding left edges to a vertex
					 * that has already been processed).  Thus it is important to call
					 * FinishRegion rather than just DeleteRegion.
					 */
					finishRegion( regPrev );
					break;
				}
				/* If the edge below was a temporary edge introduced by
				 * ConnectRightVertex, now is the time to fix it.
				 */
				e = HalfEdge.connectEdges( ePrev.ccwAroundOrigin.symmetric, e.symmetric );
				fixUpperEdge( reg, e );
			}

			/* Relink edges so that ePrev.Onext == e */
			if ( ePrev.ccwAroundOrigin != e )
			{
				Mesh.spliceMesh( e.symmetric.ccwAroundLeftFace, e );
				Mesh.spliceMesh( ePrev, e );
			}
			finishRegion( regPrev );	/* may change reg.eUp */
			ePrev = reg.upperEdge;
			regPrev = reg;
		}
		return ePrev;
	}

	/*
	 * Purpose: insert right-going edges into the edge dictionary, and update
	 * winding numbers and mesh connectivity appropriately.  All right-going
	 * edges share a common origin vOrg.  Edges are inserted CCW starting at
	 * eFirst; the last edge inserted is eLast.Sym.Lnext.  If vOrg has any
	 * left-going edges already processed, then eTopLeft must be the edge
	 * such that an imaginary upward vertical segment from vOrg would be
	 * contained between eTopLeft.Sym.Lnext and eTopLeft; otherwise eTopLeft
	 * should be null.
	 */

	void addRightEdges( final Region regUp, final HalfEdge eFirst, final HalfEdge eLast, HalfEdge eTopLeft, final boolean cleanUp )
	{
		Region reg;
		Region regPrev;
		HalfEdge e;
		HalfEdge ePrev;
		boolean firstTime = true;

		/* Insert the new right-going edges in the dictionary */
		e = eFirst;
		do
		{
			assert ( Geom.vertLeq( e.origin, e.symmetric.origin ) );
			addRegionBelow( regUp, e.symmetric );
			e = e.ccwAroundOrigin;
		}
		while ( e != eLast );

		/* Walk *all* right-going edges from e.Org, in the dictionary order,
		 * updating the winding numbers of each region, and re-linking the mesh
		 * edges to match the dictionary ordering (if necessary).
		 */
		if ( eTopLeft == null )
		{
			eTopLeft = regionBelow( regUp ).upperEdge.symmetric.ccwAroundOrigin;
		}
		regPrev = regUp;
		ePrev = eTopLeft;
		for (; ; )
		{
			reg = regionBelow( regPrev );
			e = reg.upperEdge.symmetric;
			if ( e.origin != ePrev.origin )
			{
				break;
			}

			if ( e.ccwAroundOrigin != ePrev )
			{
				/* Unlink e from its current position, and relink below ePrev */
				Mesh.spliceMesh( e.symmetric.ccwAroundLeftFace, e );
				Mesh.spliceMesh( ePrev.symmetric.ccwAroundLeftFace, e );
			}
			/* Compute the winding number and "inside" flag for the new regions */
			reg.windingNumber = regPrev.windingNumber - e.winding;
			reg.inside = _mesh.windingRule.isInside( reg.windingNumber );

			/* Check for two outgoing edges with same slope -- process these
			 * before any intersection tests (see example in computeInterior).
			 */
			regPrev.dirty = true;
			if ( !firstTime && checkForRightSplice( regPrev ) )
			{
				addWinding( e, ePrev );
				deleteRegion( regPrev );
				ePrev.delete();
			}

			firstTime = false;
			regPrev = reg;
			ePrev = e;
		}
		regPrev.dirty = true;
		assert ( regPrev.windingNumber - e.winding == reg.windingNumber );

		if ( cleanUp )
		{
			/* Check for intersections between newly adjacent edges. */
			walkDirtyRegions( regPrev );
		}
	}


	/**
	 * Two vertices with idential coordinates are combined into one.
	 * e1.Org is kept, while e2.Org is discarded.
	 */
	void spliceMergeVertices( final HalfEdge e1, final HalfEdge e2 )
	{
		// COMBINE CALLBACK HERE
		Mesh.spliceMesh( e1, e2 );
	}

	/**
	 * Check the upper and lower edge of "regUp", to make sure that the
	 * eUp.Org is above eLo, or eLo.Org is below eUp (depending on which
	 * origin is leftmost).
	 * <p/>
	 * The main purpose is to splice right-going edges with the same
	 * dest vertex and nearly identical slopes (ie. we can't distinguish
	 * the slopes numerically).  However the splicing can also help us
	 * to recover from numerical errors.  For example, suppose at one
	 * point we checked eUp and eLo, and decided that eUp.Org is barely
	 * above eLo.  Then later, we split eLo into two edges (eg. from
	 * a splice operation like this one).  This can change the result of
	 * our test so that now eUp.Org is incident to eLo, or barely below it.
	 * We must correct this condition to maintain the dictionary invariants.
	 * <p/>
	 * One possibility is to check these edges for intersection again
	 * (ie. CheckForIntersect).  This is what we do if possible.  However
	 * CheckForIntersect requires that _event lies between eUp and eLo,
	 * so that it has something to fall back on when the intersection
	 * calculation gives us an unusable answer.  So, for those cases where
	 * we can't check for intersection, this routine fixes the problem
	 * by just splicing the offending vertex into the other edge.
	 * This is a guaranteed solution, no matter how degenerate things get.
	 * Basically this is a combinatorial solution to a numerical problem.
	 */
	private boolean checkForRightSplice( final Region regUp )
	{
		final Region regLo = regionBelow( regUp );
		final HalfEdge eUp = regUp.upperEdge;
		final HalfEdge eLo = regLo.upperEdge;

		if ( Geom.vertLeq( eUp.origin, eLo.origin ) )
		{
			if ( Geom.edgeSign( eLo.symmetric.origin, eUp.origin, eLo.origin ) > 0 )
			{
				return false;
			}

			/* eUp.Org appears to be below eLo */
			if ( !Geom.vertEq( eUp.origin, eLo.origin ) )
			{
				/* Splice eUp.Org into eLo */
				eLo.symmetric.split();
				Mesh.spliceMesh( eUp, eLo.symmetric.ccwAroundLeftFace );
				regUp.dirty = regLo.dirty = true;

			}
			else if ( eUp.origin != eLo.origin )
			{
				/* merge the two vertices, discarding eUp.Org */
				_pq.pqDelete( eUp.origin.pqHandle ); /* pqSortDelete */
				spliceMergeVertices( eLo.symmetric.ccwAroundLeftFace, eUp );
			}
		}
		else
		{
			if ( Geom.edgeSign( eUp.symmetric.origin, eLo.origin, eUp.origin ) < 0 )
			{
				return false;
			}

			/* eLo.Org appears to be above eUp, so splice eLo.Org into eUp */
			regionAbove( regUp ).dirty = regUp.dirty = true;
			eUp.symmetric.split();
			Mesh.spliceMesh( eLo.symmetric.ccwAroundLeftFace, eUp );
		}
		return true;
	}

	/**
	 * Check the upper and lower edge of "regUp", to make sure that the
	 * eUp.Sym.Org is above eLo, or eLo.Sym.Org is below eUp (depending on which
	 * destination is rightmost).
	 * <p/>
	 * Theoretically, this should always be true.  However, splitting an edge
	 * into two pieces can change the results of previous tests.  For example,
	 * suppose at one point we checked eUp and eLo, and decided that eUp.Sym.Org
	 * is barely above eLo.  Then later, we split eLo into two edges (eg. from
	 * a splice operation like this one).  This can change the result of
	 * the test so that now eUp.Sym.Org is incident to eLo, or barely below it.
	 * We must correct this condition to maintain the dictionary invariants
	 * (otherwise new edges might get inserted in the wrong place in the
	 * dictionary, and bad stuff will happen).
	 * <p/>
	 * We fix the problem by just splicing the offending vertex into the
	 * other edge.
	 */
	private boolean checkForLeftSplice( final Region regUp )
	{
		final Region regLo = regionBelow( regUp );
		final HalfEdge eUp = regUp.upperEdge;
		final HalfEdge eLo = regLo.upperEdge;
		final HalfEdge e;

		assert ( !Geom.vertEq( eUp.symmetric.origin, eLo.symmetric.origin ) );

		if ( Geom.vertLeq( eUp.symmetric.origin, eLo.symmetric.origin ) )
		{
			if ( Geom.edgeSign( eUp.symmetric.origin, eLo.symmetric.origin, eUp.origin ) < 0 )
			{
				return false;
			}

			/* eLo.Sym.Org is above eUp, so splice eLo.Sym.Org into eUp */
			regionAbove( regUp ).dirty = regUp.dirty = true;
			e = eUp.split();
			Mesh.spliceMesh( eLo.symmetric, e );
			e.leftFace.inside = regUp.inside;
		}
		else
		{
			if ( Geom.edgeSign( eLo.symmetric.origin, eUp.symmetric.origin, eLo.origin ) > 0 )
			{
				return false;
			}

			/* eUp.Sym.Org is below eLo, so splice eUp.Sym.Org into eLo */
			regUp.dirty = regLo.dirty = true;
			e = eLo.split();
			Mesh.spliceMesh( eUp.ccwAroundLeftFace, eLo.symmetric );
			e.symmetric.leftFace.inside = regUp.inside;
		}
		return true;
	}

	/**
	 * Check the upper and lower edges of the given region to see if
	 * they intersect.  If so, create the intersection and add it
	 * to the data structures.
	 * <p/>
	 * Returns true if adding the new intersection resulted in a recursive
	 * call to AddRightEdges(); in this case all "dirty" regions have been
	 * checked for intersections, and possibly regUp has been deleted.
	 */
	private boolean checkForIntersect( Region regUp )
	{
		Region regLo = regionBelow( regUp );
		HalfEdge eUp = regUp.upperEdge;
		HalfEdge eLo = regLo.upperEdge;
		final Vertex orgUp = eUp.origin;
		final Vertex orgLo = eLo.origin;
		final Vertex dstUp = eUp.symmetric.origin;
		final Vertex dstLo = eLo.symmetric.origin;
		final double tMinUp;
		final double tMaxLo;
		final Vertex isect = new Vertex();
		final Vertex orgMin;
		final HalfEdge e;

		assert ( !Geom.vertEq( dstLo, dstUp ) );
		assert ( Geom.edgeSign( dstUp, _event, orgUp ) <= 0 );
		assert ( Geom.edgeSign( dstLo, _event, orgLo ) >= 0 );
		assert ( orgUp != _event && orgLo != _event );
		assert ( !regUp.fixUpperEdge && !regLo.fixUpperEdge );

		if ( orgUp == orgLo )
		{
			return false;
		}	/* right endpoints are the same */

		tMinUp = Math.min( orgUp.location.getY(), dstUp.location.getY() );
		tMaxLo = Math.max( orgLo.location.getY(), dstLo.location.getY() );
		if ( tMinUp > tMaxLo )
		{
			return false;
		}	/* t ranges do not overlap */

		if ( Geom.vertLeq( orgUp, orgLo ) )
		{
			if ( Geom.edgeSign( dstLo, orgUp, orgLo ) > 0 )
			{
				return false;
			}
		}
		else
		{
			if ( Geom.edgeSign( dstUp, orgLo, orgUp ) < 0 )
			{
				return false;
			}
		}

		/* At this point the edges intersect, at least marginally */

		isect.location = Geom.edgeIntersect( dstUp, orgUp, dstLo, orgLo );
		/* The following properties are guaranteed: */
		assert ( Math.min( orgUp.location.getY(), dstUp.location.getY() ) <= isect.location.getY() );
		assert ( isect.location.getY() <= Math.max( orgLo.location.getY(), dstLo.location.getY() ) );
		assert ( Math.min( dstLo.location.getX(), dstUp.location.getX() ) <= isect.location.getX() );
		assert ( isect.location.getX() <= Math.max( orgLo.location.getX(), orgUp.location.getX() ) );

		if ( Geom.vertLeq( isect, _event ) )
		{
			/*
			 * The intersection point lies slightly to the left of the sweep line,
			 * so move it until it''s slightly to the right of the sweep line.
			 * (If we had perfect numerical precision, this would never happen
			 * in the first place).  The easiest and safest thing to do is
			 * replace the intersection by _event.
			 */
			isect.location = _event.location;
		}
		/*
		 * Similarly, if the computed intersection lies to the right of the
		 * rightmost origin (which should rarely happen), it can cause
		 * unbelievable inefficiency on sufficiently degenerate inputs.
		 * (If you have the test program, try running test54.d with the
		 * "X zoom" option turned on).
		 */
		orgMin = Geom.vertLeq( orgUp, orgLo ) ? orgUp : orgLo;
		if ( Geom.vertLeq( orgMin, isect ) )
		{
			isect.location = orgMin.location;
		}

		if ( Geom.vertEq( isect, orgUp ) || Geom.vertEq( isect, orgLo ) )
		{
			/* Easy case -- intersection at one of the right endpoints */
			checkForRightSplice( regUp );
			return false;
		}

		if ( ( !Geom.vertEq( dstUp, _event )
			&& Geom.edgeSign( dstUp, _event, isect ) >= 0 )
			|| ( !Geom.vertEq( dstLo, _event )
			&& Geom.edgeSign( dstLo, _event, isect ) <= 0 ) )
		{
			/*
			 * Very unusual -- the new upper or lower edge would pass on the
			 * wrong side of the sweep event, or through it.  This can happen
			 * due to very small numerical errors in the intersection calculation.
			 */
			if ( dstLo == _event )
			{
				/* Splice dstLo into eUp, and process the new region(s) */
				eUp.symmetric.split();
				Mesh.spliceMesh( eLo.symmetric, eUp );
				regUp = topLeftRegion( regUp );
				if ( regUp == null )
				{
					throw new RuntimeException();
				}
				eUp = regionBelow( regUp ).upperEdge;
				finishLeftRegions( regionBelow( regUp ), regLo );
				addRightEdges( regUp, eUp.symmetric.ccwAroundLeftFace, eUp, eUp, true );
				return true;
			}
			if ( dstUp == _event )
			{
				/* Splice dstUp into eLo, and process the new region(s) */
				eLo.symmetric.split();
				Mesh.spliceMesh( eUp.ccwAroundLeftFace, eLo.symmetric.ccwAroundLeftFace );
				regLo = regUp;
				regUp = topRightRegion( regUp );
				e = regionBelow( regUp ).upperEdge.symmetric.ccwAroundOrigin;
				regLo.upperEdge = eLo.symmetric.ccwAroundLeftFace;
				eLo = finishLeftRegions( regLo, null );
				addRightEdges( regUp, eLo.ccwAroundOrigin, eUp.symmetric.ccwAroundOrigin, e, true );
				return true;
			}
			/* Special case: called from ConnectRightVertex.  If either
			 * edge passes on the wrong side of _event, split it
			 * (and wait for ConnectRightVertex to splice it appropriately).
			 */
			if ( Geom.edgeSign( dstUp, _event, isect ) >= 0 )
			{
				regionAbove( regUp ).dirty = regUp.dirty = true;
				eUp.symmetric.split();
				eUp.origin.location = _event.location;
			}
			if ( Geom.edgeSign( dstLo, _event, isect ) <= 0 )
			{
				regUp.dirty = regLo.dirty = true;
				eLo.symmetric.split();
				eLo.origin.location = _event.location;
			}
			/* leave the rest for ConnectRightVertex */
			return false;
		}

		/*
		 * General case -- split both edges, splice into new vertex.
		 * When we do the splice operation, the order of the arguments is
		 * arbitrary as far as correctness goes.  However, when the operation
		 * creates a new face, the work done is proportional to the size of
		 * the new face.  We expect the faces in the processed part of
		 * the mesh (ie. eUp.Lface) to be smaller than the faces in the
		 * unprocessed original contours (which will be eLo.Sym.Lnext.Lface).
		 */
		eUp.symmetric.split();
		eLo.symmetric.split();
		Mesh.spliceMesh( eLo.symmetric.ccwAroundLeftFace, eUp );
		eUp.origin.location = isect.location;
		eUp.origin.pqHandle = _pq.pqInsert( eUp.origin ); /* pqSortInsert */
		// COMBINE CALLBACK HERE: eUp.origin.vertexIndex = _tessellationBuilder.addVertex( eUp.origin.getX(), eUp.origin.getY(), 0.0 );
		regLo.dirty = true;
		regUp.dirty = true;
		regionAbove( regUp ).dirty = true;
		return false;
	}

	/**
	 * When the upper or lower edge of any region changes, the region is
	 * marked "dirty".  This routine walks through all the dirty regions
	 * and makes sure that the dictionary invariants are satisfied
	 * (see the comments at the beginning of this file).  Of course
	 * new dirty regions can be created as we make changes to restore
	 * the invariants.
	 */
	void walkDirtyRegions( Region regUp )
	{
		Region regLo = regionBelow( regUp );
		HalfEdge eUp;
		HalfEdge eLo;

		for (; ; )
		{
			/* Find the lowest dirty region (we walk from the bottom up). */
			while ( regLo.dirty )
			{
				regUp = regLo;
				regLo = regionBelow( regLo );
			}
			if ( !regUp.dirty )
			{
				regLo = regUp;
				regUp = regionAbove( regUp );
				if ( regUp == null || !regUp.dirty )
				{
					/* We've walked all the dirty regions */
					return;
				}
			}
			regUp.dirty = false;
			eUp = regUp.upperEdge;
			eLo = regLo.upperEdge;

			if ( eUp.symmetric.origin != eLo.symmetric.origin )
			{
				/* Check that the edge ordering is obeyed at the Dst vertices. */
				if ( checkForLeftSplice( regUp ) )
				{

					/*
					 * If the upper or lower edge was marked fixUpperEdge, then
					 * we no longer need it (since these edges are needed only for
					 * vertices which otherwise have no right-going edges).
					 */
					if ( regLo.fixUpperEdge )
					{
						deleteRegion( regLo );
						eLo.delete();
						regLo = regionBelow( regUp );
						eLo = regLo.upperEdge;
					}
					else if ( regUp.fixUpperEdge )
					{
						deleteRegion( regUp );
						eUp.delete();
						regUp = regionAbove( regLo );
						eUp = regUp.upperEdge;
					}
				}
			}
			if ( eUp.origin != eLo.origin )
			{
				if ( eUp.symmetric.origin != eLo.symmetric.origin
					&& !regUp.fixUpperEdge && !regLo.fixUpperEdge
					&& ( eUp.symmetric.origin == _event || eLo.symmetric.origin == _event ) )
				{
					/*
					 * When all else fails in CheckForIntersect(), it uses _event
					 * as the intersection location.  To make this possible, it requires
					 * that _event lie between the upper and lower edges, and also
					 * that neither of these is marked fixUpperEdge (since in the worst
					 * case it might splice one of these edges into _event, and
					 * violate the invariant that fixable edges are the only right-going
					 * edge from their associated vertex).
						 */
					if ( checkForIntersect( regUp ) )
					{
						/* WalkDirtyRegions() was called recursively; we're done */
						return;
					}
				}
				else
				{
					/* Even though we can't use CheckForIntersect(), the Org vertices
					 * may violate the dictionary edge ordering.  Check and correct this.
					 */
					checkForRightSplice( regUp );
				}
			}
			if ( eUp.origin == eLo.origin && eUp.symmetric.origin == eLo.symmetric.origin )
			{
				/* A degenerate loop consisting of only two edges -- delete it. */
				addWinding( eLo, eUp );
				deleteRegion( regUp );
				eUp.delete();
				regUp = regionAbove( regLo );
			}
		}
	}

	/**
	 * Purpose: connect a "right" vertex vEvent (one where all edges go left)
	 * to the unprocessed portion of the mesh.  Since there are no right-going
	 * edges, two regions (one above vEvent and one below) are being merged
	 * into one.  "regUp" is the upper of these two regions.
	 * <p/>
	 * There are two reasons for doing this (adding a right-going edge):
	 * - if the two regions being merged are "inside", we must add an edge
	 * to keep them separated (the combined region would not be monotone).
	 * - in any case, we must leave some record of vEvent in the dictionary,
	 * so that we can merge vEvent with features that we have not seen yet.
	 * For example, maybe there is a vertical edge which passes just to
	 * the right of vEvent; we would like to splice vEvent into this edge.
	 * <p/>
	 * However, we don't want to connect vEvent to just any vertex.  We don''t
	 * want the new edge to cross any other edges; otherwise we will create
	 * intersection vertices even when the input data had no self-intersections.
	 * (This is a bad thing; if the user's input data has no intersections,
	 * we don't want to generate any false intersections ourselves.)
	 * <p/>
	 * Our eventual goal is to connect vEvent to the leftmost unprocessed
	 * vertex of the combined region (the union of regUp and regLo).
	 * But because of unseen vertices with all right-going edges, and also
	 * new vertices which may be created by edge intersections, we don''t
	 * know where that leftmost unprocessed vertex is.  In the meantime, we
	 * connect vEvent to the closest vertex of either chain, and mark the region
	 * as "fixUpperEdge".  This flag says to delete and reconnect this edge
	 * to the next processed vertex on the boundary of the combined region.
	 * Quite possibly the vertex we connected to will turn out to be the
	 * closest one, in which case we won''t need to make any changes.
	 */
	void connectRightVertex( Region regUp, HalfEdge eBottomLeft )
	{
		HalfEdge eNew;
		HalfEdge eTopLeft = eBottomLeft.ccwAroundOrigin;
		final Region regLo = regionBelow( regUp );
		final HalfEdge eUp = regUp.upperEdge;
		final HalfEdge eLo = regLo.upperEdge;
		boolean degenerate = false;

		if ( eUp.symmetric.origin != eLo.symmetric.origin )
		{
			checkForIntersect( regUp );
		}

		/* Possible new degeneracies: upper or lower edge of regUp may pass
		 * through vEvent, or may coincide with new intersection vertex
		 */
		if ( Geom.vertEq( eUp.origin, _event ) )
		{
			Mesh.spliceMesh( eTopLeft.symmetric.ccwAroundLeftFace, eUp );
			regUp = topLeftRegion( regUp );
			if ( regUp == null )
			{
				throw new RuntimeException();
			}
			eTopLeft = regionBelow( regUp ).upperEdge;
			finishLeftRegions( regionBelow( regUp ), regLo );
			degenerate = true;
		}
		if ( Geom.vertEq( eLo.origin, _event ) )
		{
			Mesh.spliceMesh( eBottomLeft, eLo.symmetric.ccwAroundLeftFace );
			eBottomLeft = finishLeftRegions( regLo, null );
			degenerate = true;
		}
		if ( degenerate )
		{
			addRightEdges( regUp, eBottomLeft.ccwAroundOrigin, eTopLeft, eTopLeft, true );
			return;
		}

		/* Non-degenerate situation -- need to add a temporary, fixable edge.
		 * Connect to the closer of eLo.Org, eUp.Org.
		 */
		if ( Geom.vertLeq( eLo.origin, eUp.origin ) )
		{
			eNew = eLo.symmetric.ccwAroundLeftFace;
		}
		else
		{
			eNew = eUp;
		}
		eNew = HalfEdge.connectEdges( eBottomLeft.ccwAroundOrigin.symmetric, eNew );

		/* Prevent cleanup, otherwise eNew might disappear before we've even
		 * had a chance to mark it as a temporary edge.
		 */
		addRightEdges( regUp, eNew, eNew.ccwAroundOrigin, eNew.ccwAroundOrigin, false );
		eNew.symmetric.activeRegion.fixUpperEdge = true;
		walkDirtyRegions( regUp );
	}

	/**
	 * The event vertex lies exacty on an already-processed edge or vertex.
	 * Adding the new vertex involves splicing it into the already-processed
	 * part of the mesh.
	 */
	void connectLeftDegenerate( Region regUp, final Vertex vEvent )
	{
		final HalfEdge e;
		HalfEdge eTopLeft;
		HalfEdge eTopRight;
		final HalfEdge eLast;
		final Region reg;

		e = regUp.upperEdge;
		if ( Geom.vertEq( e.origin, vEvent ) )
		{
			/* e.Org is an unprocessed vertex - just combine them, and wait
			 * for e.Org to be pulled from the queue
			 */
			assert ( TOLERANCE_NONZERO );
			spliceMergeVertices( e, vEvent.anEdge );
			return;
		}

		if ( !Geom.vertEq( e.symmetric.origin, vEvent ) )
		{
			/* General case -- splice vEvent into edge e which passes through it */
			e.symmetric.split();
			if ( regUp.fixUpperEdge )
			{
				/* This edge was fixable -- delete unused portion of original edge */
				e.ccwAroundOrigin.delete();
				regUp.fixUpperEdge = false;
			}
			Mesh.spliceMesh( vEvent.anEdge, e );
			sweepEvent( vEvent );	/* recurse */
			return;
		}

		/*
		 * vEvent coincides with e.Sym.Org, which has already been processed.
		 * Splice in the additional right-going edges.
		 */
		assert ( TOLERANCE_NONZERO );
		regUp = topRightRegion( regUp );
		reg = regionBelow( regUp );
		eTopRight = reg.upperEdge.symmetric;
		eTopLeft = eLast = eTopRight.ccwAroundOrigin;
		if ( reg.fixUpperEdge )
		{
			/*
			 * Here e.Sym.Org has only a single fixable edge going right.
			 * We can delete it since now we have some real right-going edges.
			 */
			assert ( eTopLeft != eTopRight );   /* there are some left edges too */
			deleteRegion( reg );
			eTopRight.delete();
			eTopRight = eTopLeft.symmetric.ccwAroundLeftFace;
		}
		Mesh.spliceMesh( vEvent.anEdge, eTopRight );
		if ( !eTopLeft.goesLeft() )
		{
			/* e.Sym.Org had no left-going edges -- indicate this to AddRightEdges() */
			eTopLeft = null;
		}
		addRightEdges( regUp, eTopRight.ccwAroundOrigin, eLast, eTopLeft, true );
	}

	/**
	 * Purpose: connect a "left" vertex (one where both edges go right)
	 * to the processed portion of the mesh.  Let R be the active region
	 * containing vEvent, and let U and L be the upper and lower edge
	 * chains of R.  There are two possibilities:
	 * <p/>
	 * - the normal case: split R into two regions, by connecting vEvent to
	 * the rightmost vertex of U or L lying to the left of the sweep line
	 * <p/>
	 * - the degenerate case: if vEvent is close enough to U or L, we
	 * merge vEvent into that edge chain.  The subcases are:
	 * - merging with the rightmost vertex of U or L
	 * - merging with the active edge of U or L
	 * - merging with an already-processed portion of U or L
	 */
	void connectLeftVertex( final Vertex vEvent )
	{
		final Region regUp;
		final Region regLo;
		final Region reg;
		final HalfEdge eUp;
		final HalfEdge eLo;
		final HalfEdge eNew;
		final Region tmp = new Region();

		/* assert ( vEvent.anEdge.Onext.Onext == vEvent.anEdge ); */

		/* Get a pointer to the active region containing vEvent */
		tmp.upperEdge = vEvent.anEdge.symmetric;
		/* __GL_DICTLISTKEY */ /* dictListSearch */
		regUp = _dict.search( tmp ).key;
		regLo = regionBelow( regUp );
		eUp = regUp.upperEdge;
		eLo = regLo.upperEdge;

		/* Try merging with U or L first */
		if ( Geom.edgeSign( eUp.symmetric.origin, vEvent, eUp.origin ) == 0 )
		{
			connectLeftDegenerate( regUp, vEvent );
			return;
		}

		/*
		 * Connect vEvent to rightmost processed vertex of either chain.
		 * e.Sym.Org is the vertex that we will connect to vEvent.
		 */
		reg = Geom.vertLeq( eLo.symmetric.origin, eUp.symmetric.origin ) ? regUp : regLo;

		if ( regUp.inside || reg.fixUpperEdge )
		{
			if ( reg == regUp )
			{
				eNew = HalfEdge.connectEdges( vEvent.anEdge.symmetric, eUp.ccwAroundLeftFace );
			}
			else
			{
				final HalfEdge tempHalfEdge = HalfEdge.connectEdges( eLo.symmetric.ccwAroundOrigin.symmetric, vEvent.anEdge );
				eNew = tempHalfEdge.symmetric;

			}
			if ( reg.fixUpperEdge )
			{
				fixUpperEdge( reg, eNew );
			}
			else
			{
				computeWinding( addRegionBelow( regUp, eNew ) );
			}
			sweepEvent( vEvent );
		}
		else
		{
			/*
			 * The new vertex is in a region which does not belong to the polygon.
			 * We don''t need to connect this vertex to the rest of the mesh.
			 */
			addRightEdges( regUp, vEvent.anEdge, vEvent.anEdge, null, true );
		}
	}

	/**
	 * Does everything necessary when the sweep line crosses a vertex.
	 * Updates the mesh and the edge dictionary.
	 */
	void sweepEvent( final Vertex vEvent )
	{
		final Region regUp;
		final Region reg;
		HalfEdge e;
		final HalfEdge eTopLeft;
		final HalfEdge eBottomLeft;

		_event = vEvent;		/* for access in EdgeLeq() */

		/*
		 * Check if this vertex is the right endpoint of an edge that is
		 * already in the dictionary.  In this case we don't need to waste
		 * time searching for the location to insert new edges.
		 */
		e = vEvent.anEdge;
		while ( e.activeRegion == null )
		{
			e = e.ccwAroundOrigin;
			if ( e == vEvent.anEdge )
			{
				/* All edges go right -- not incident to any processed edges */
				connectLeftVertex( vEvent );
				return;
			}
		}

		/*
		 * Processing consists of two phases: first we "finish" all the
		 * active regions where both the upper and lower edges terminate
		 * at vEvent (ie. vEvent is closing off these regions).
		 * We mark these faces "inside" or "outside" the polygon according
		 * to their winding number, and delete the edges from the dictionary.
		 * This takes care of all the left-going edges from vEvent.
		 */
		regUp = topLeftRegion( e.activeRegion );
		if ( regUp == null )
		{
			throw new RuntimeException();
		}
		reg = regionBelow( regUp );
		eTopLeft = reg.upperEdge;
		eBottomLeft = finishLeftRegions( reg, null );

		/* Next we process all the right-going edges from vEvent.  This
		 * involves adding the edges to the dictionary, and creating the
		 * associated "active regions" which record information about the
		 * regions between adjacent dictionary edges.
		 */
		if ( eBottomLeft.ccwAroundOrigin == eTopLeft )
		{
			/* No right-going edges -- add a temporary "fixable" edge */
			connectRightVertex( regUp, eBottomLeft );
		}
		else
		{
			addRightEdges( regUp, eBottomLeft.ccwAroundOrigin, eTopLeft, eTopLeft, true );
		}
	}

	/**
	 * We add two sentinel edges above and below all other edges,
	 * to avoid special cases at the top and bottom.
	 */
	void addSentinel( final double t )
	{
		final HalfEdge sentinelEdge = _mesh.createSelfLoopEdge();
		sentinelEdge.origin.location = new Vector2D( SENTINEL_COORD, t );
		sentinelEdge.symmetric.origin.location = new Vector2D( -SENTINEL_COORD, t );
		_event = sentinelEdge.symmetric.origin;

		final Region region = new Region();
		region.upperEdge = sentinelEdge;
		region.windingNumber = 0;
		region.inside = false;
		region.fixUpperEdge = false;
		region.sentinel = true;
		region.dirty = false;
		region.nodeUp = _dict.insert( region ); /* dictListInsertBefore */
	}

	/**
	 * We maintain an ordering of edge intersections with the sweep line.
	 * This order is maintained in a dynamic dictionary.
	 */
	void initEdgeDict()
	{
		_dict = new RegionDict( this );
		addSentinel( -SENTINEL_COORD );
		addSentinel( SENTINEL_COORD );
	}

	/**
	 * Remove zero-length edges, and contours with fewer than 3 vertices.
	 */
	void removeDegenerateEdges()
	{
		HalfEdge e;
		HalfEdge eNext;
		HalfEdge eLnext;
		final HalfEdge eHead = _mesh._edgeListHead;

		for ( e = eHead.next; e != eHead; e = eNext )
		{
			eNext = e.next;
			eLnext = e.ccwAroundLeftFace;

			if ( Geom.vertEq( e.origin, e.symmetric.origin ) && e.ccwAroundLeftFace.ccwAroundLeftFace != e )
			{
				/* Zero-length edge, contour has at least 3 edges */

				spliceMergeVertices( eLnext, e );	/* deletes e.Org */
				e.delete(); /* e is a self-loop */
				e = eLnext;
				eLnext = e.ccwAroundLeftFace;
			}
			if ( eLnext.ccwAroundLeftFace == e )
			{
				/* Degenerate contour (one or two edges) */

				if ( eLnext != e )
				{
					if ( eLnext == eNext || eLnext == eNext.symmetric )
					{
						eNext = eNext.next;
					}
					eLnext.delete();
				}
				if ( e == eNext || e == eNext.symmetric )
				{
					eNext = eNext.next;
				}
				e.delete();
			}
		}
	}

	/**
	 * Insert all vertices into the priority queue which determines the
	 * order in which vertices cross the sweep line.
	 */
	private void initPriorityQ()
	{
		final PriorityQSort pq = new PriorityQSort( PRIORITY_Q_LEQ );

		final Vertex vHead = _mesh._vertexListHead;
		for ( Vertex v = vHead.next; v != vHead; v = v.next )
		{
			v.pqHandle = pq.pqInsert( v );
		}

		pq.pqInit();

		_pq = pq;
	}

	/**
	 * Delete any degenerate faces with only two edges.  WalkDirtyRegions()
	 * will catch almost all of these, but it won't catch degenerate faces
	 * produced by splice operations on already-processed edges.
	 * The two places this can happen are in FinishLeftRegions(), when
	 * we splice in a "temporary" edge produced by ConnectRightVertex(),
	 * and in CheckForLeftSplice(), where we splice already-processed
	 * edges to ensure that our dictionary invariants are not violated
	 * by numerical errors.
	 * <p/>
	 * In both these cases it is *very* dangerous to delete the offending
	 * edge at the time, since one of the routines further up the stack
	 * will sometimes be keeping a pointer to that edge.
	 */
	private void removeDegenerateFaces( final Mesh mesh )
	{
		final Face head = mesh._faceListHead;
		Face face = head.next;
		while ( face != head )
		{
			final Face nextFace = face.next;

			final HalfEdge e = face.anEdge;
			assert ( e.ccwAroundLeftFace != e );

			if ( e.ccwAroundLeftFace.ccwAroundLeftFace == e )
			{
				/* A face with only two edges */
				addWinding( e.ccwAroundOrigin, e );
				e.delete();
			}

			face = nextFace;
		}
	}

	/**
	 * Computes the planar arrangement specified by the given contours, and
	 * further subdivides this arrangement into regions.  Each region is marked
	 * "inside" if it belongs to the polygon, according to the rule given by
	 * _windingRule. Each interior region is guaranteed be monotone.
	 */
	public void computeInterior()
	{
		removeDegenerateEdges();

		/*
		 * Each vertex defines an event for our sweep line.  Start by inserting
		 * all the vertices in a priority queue.  Events are processed in
		 * lexicographic order, ie.
		 *
		 *	e1 < e2  iff  e1.x < e2.x || (e1.x == e2.x && e1.y < e2.y)
		 */
		initPriorityQ();
		initEdgeDict();

		Vertex v;
		Vertex vNext;

		while ( ( v = (Vertex) _pq.pqExtractMin() ) != null )
		{
			for (; ; )
			{
				vNext = (Vertex) _pq.pqMinimum();
				if ( vNext == null || !Geom.vertEq( vNext, v ) )
				{
					break;
				}

				/*
				 * Merge together all vertices at exactly the same location.
				 * This is more efficient than processing them one at a time,
				 * simplifies the code (see connectLeftDegenerate), and is also
				 * important for correct handling of certain degenerate cases.
				 * For example, suppose there are two identical edges A and B
				 * that belong to different contours (so without this code they
				 * would be processed by separate sweep events). Suppose another
				 * edge C crosses A and B from above.  When A is processed, we
				 * split it at its intersection point with C.  However this also
				 * splits C, so when we insert B we may compute a slightly
				 * different intersection point.  This might leave two edges
				 * with a small gap between them.  This kind of error is
				 * especially obvious when using boundary extraction.
				 */
				vNext = (Vertex) _pq.pqExtractMin();
				spliceMergeVertices( v.anEdge, vNext.anEdge );
			}
			sweepEvent( v );
		}

		_event = _dict.min().key.upperEdge.origin;
		_dict = null;
		_pq = null;

		removeDegenerateFaces( _mesh );
	}
}
