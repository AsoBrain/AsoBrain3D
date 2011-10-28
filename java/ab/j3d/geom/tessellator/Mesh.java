/*
* Portions Copyright (C) 2003-2006 Sun Microsystems, Inc.
* All rights reserved.
*/

/*
** License Applicability. Except to the extent portions of this file are
** made subject to an alternative license as permitted in the SGI Free
** Software License B, Version 1.1 (the "License"), the contents of this
** file are subject only to the provisions of the License. You may not use
** this file except in compliance with the License. You may obtain a copy
** of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
** Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
**
** http://oss.sgi.com/projects/FreeB
**
** Note that, as provided in the License, the Software is distributed on an
** "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
** DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
** CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
** PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
**
** NOTE:  The Original Code (as defined below) has been licensed to Sun
** Microsystems, Inc. ("Sun") under the SGI Free Software License B
** (Version 1.1), shown above ("SGI License").   Pursuant to Section
** 3.2(3) of the SGI License, Sun is distributing the Covered Code to
** you under an alternative license ("Alternative License").  This
** Alternative License includes all of the provisions of the SGI License
** except that Section 2.2 and 11 are omitted.  Any differences between
** the Alternative License and the SGI License are offered solely by Sun
** and not by SGI.
**
** Original Code. The Original Code is: OpenGL Sample Implementation,
** Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
** Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
** Copyright in any portions created by third parties is as indicated
** elsewhere herein. All Rights Reserved.
**
** Additional Notice Provisions: The application programming interfaces
** established by SGI in conjunction with the Original Code are The
** OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
** April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
** 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
** Window System(R) (Version 1.3), released October 19, 1998. This software
** was created using the OpenGL(R) version 1.2.1 Sample Implementation
** published by SGI, but has not been independently verified as being
** compliant with the OpenGL(R) version 1.2.1 Specification.
**
** Author: Eric Veach, July 1994
** Java Port: Pepijn Van Eeckhoudt, July 2003
** Java Port: Nathan Parker Burg, August 2003
*/
package ab.j3d.geom.tessellator;

import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * The {@link Mesh} class defines a mesh during the tessellation process.
 */
public class Mesh
{
	/**
	 * Determines which parts of the polygon are on the "interior".
	 * <P>
	 * To understand how the winding rule works, consider that the contours
	 * partition the plane into regions. The winding rule determines which of
	 * these regions are inside the polygon.
	 * <P>
	 * For a single contour C, the winding number of a point x is simply the
	 * signed number of revolutions we make around x as we travel once around C
	 * (where CCW is positive).  When there are several contours, the individual
	 * winding numbers are summed. This procedure associates a signed integer
	 * value with each point x in the plane.  Note that the winding number is
	 * the same for all points in a single region.
	 * <P>
	 * The winding rule classifies a region as "inside" if its winding number
	 * belongs to the chosen category (odd, nonzero, positive, negative, or
	 * absolute value of at least two). The "odd" and "nonzero" rules are common
	 * ways to define the interior. The other three rules are useful for polygon
	 * CSG operations.
	 */
	public enum WindingRule
	{
		ODD,
		NONZERO,
		POSITIVE,
		NEGATIVE,
		ABS_GEQ_TWO;

		/**
		 * Test wether a region with the given winding number is considered
		 * "inside".
		 *
		 * @param   windingNumber   Winding number of region.
		 *
		 * @return  <code>true</code> if region is "inside";
		 *          <code>false</code> if region is "outside".
		 */
		boolean isInside( final int windingNumber )
		{
			final boolean result;

			switch ( this )
			{
				case ODD:
					result = ( ( windingNumber & 1 ) != 0 );
					break;

				case NONZERO:
					result = ( windingNumber != 0 );
					break;

				case POSITIVE:
					result = ( windingNumber > 0 );
					break;

				case NEGATIVE:
					result = ( windingNumber < 0 );
					break;

				case ABS_GEQ_TWO:
					result = ( windingNumber >= 2 ) || ( windingNumber <= -2 );
					break;

				default:
					throw new AssertionError( this );
			}

			return result;
		}

	}

	/**
	 * Winding rule of mesh.
	 */
	final WindingRule windingRule;

	/**
	 * head of vertex list
	 */
	Vertex _vertexListHead;

	/**
	 * head of face list
	 */
	Face _faceListHead;

	/**
	 * head of edge list
	 */
	HalfEdge _edgeListHead;

	/**
	 * symmetric counterpart of head of edge list
	 */
	HalfEdge _edgeListHeadSymmetric;

	/**
	 * Most recently added edge of contour that is being build. This is
	 * <code>null</code> if no contour is started yet or the contour is
	 * finished.
	 */
	private HalfEdge _lastContourEdge;

	/**
	 * Flag to indicate that {@link #finish} was called.
	 */
	private boolean _finished;

	/**
	 * Flag to indicate that the mesh was tessellated.
	 *
	 * @see     #constructPrimitives
	 */
	private boolean _tessellated;

	/**
	 * Flag to indicate that the mesh was outlined.
	 *
	 * @see     #constructOutlines
	 */
	private boolean _outlined;

	/**
	 * Creates a new mesh with no edges, no vertices, and no loops (what we
	 * usually call a "face").
	 *
	 * @param   windingRule     Winding rule of mesh.
	 */
	public Mesh( final WindingRule windingRule )
	{
		this.windingRule = windingRule;

		final Vertex vertexListHead = new Vertex();
		vertexListHead.prev = vertexListHead;
		vertexListHead.next = vertexListHead;
		vertexListHead.anEdge = null;
		vertexListHead.vertexIndex = -1;

		final Face faceListHead = new Face();
		faceListHead.prev = faceListHead;
		faceListHead.next = faceListHead;
		faceListHead.anEdge = null;
		faceListHead.renderStack = null;
		faceListHead.rendered = false;
		faceListHead.inside = false;

		final HalfEdge edgeListHead  = new HalfEdge( true );
		edgeListHead.next = edgeListHead;
		edgeListHead.ccwAroundOrigin = null;
		edgeListHead.ccwAroundLeftFace = null;
		edgeListHead.origin = null;
		edgeListHead.leftFace = null;
		edgeListHead.winding = 0;
		edgeListHead.activeRegion = null;

		final HalfEdge edgeListHeadSymmetric = new HalfEdge( false );
		edgeListHeadSymmetric.next = edgeListHeadSymmetric;
		edgeListHeadSymmetric.ccwAroundOrigin = null;
		edgeListHeadSymmetric.ccwAroundLeftFace = null;
		edgeListHeadSymmetric.origin = null;
		edgeListHeadSymmetric.leftFace = null;
		edgeListHeadSymmetric.winding = 0;
		edgeListHeadSymmetric.activeRegion = null;

		edgeListHead.symmetric = edgeListHeadSymmetric;
		edgeListHeadSymmetric.symmetric = edgeListHead;

		_vertexListHead = vertexListHead;
		_faceListHead = faceListHead;
		_edgeListHead = edgeListHead;
		_edgeListHeadSymmetric = edgeListHeadSymmetric;

		_lastContourEdge = null;
		_finished = false;
		_tessellated = false;
		_outlined = false;
	}

	/**
	 * Begin the definition of a new contour. The following vertices specify a
	 * closed contour (the last vertex is automatically connected to the first).
	 */
	public void beginContour()
	{
		_lastContourEdge = null;
	}

	/**
	 * Add vertex to current contour. Can only be called between
	 * {@link #beginContour} and {@link #endContour}.
	 *
	 * @param   x   X coordinate of vertex.
	 * @param   y   Y coordinate of vertex.
	 */
	public void addVertex( final double x, final double y )
	{
		if ( _finished )
		{
			throw new IllegalStateException( "finish() called" );
		}

		HalfEdge edge = _lastContourEdge;
		if ( edge == null )
		{
			/* Make a self-loop (one vertex, one edge). */

			edge = createSelfLoopEdge();
			spliceMesh( edge, edge.symmetric );
		}
		else
		{
			/* Create a new vertex and edge which immediately follow e
			 * in the ordering around the left face.
			 */
			edge.split();
			edge = edge.ccwAroundLeftFace;
		}

		/* The new vertex is now e.Org. */
		edge.origin.location.set( x, y );

		/*
		 * The winding of an edge says how the winding number changes as we
		 * cross from the edge''s right face to its left face.  We add the
		 * vertices in such an order that a CCW contour will add +1 to
		 * the winding number of the region inside the contour.
		 */
		edge.winding = 1;
		edge.symmetric.winding = -1;

		_lastContourEdge = edge;
	}

	/**
	 * Indicates the end of a polygon contour.
	 */
	public void endContour()
	{
		_lastContourEdge = null;
	}

	/**
	 * Ends the definition of the mesh. When this method is called, the polygon
	 * can be tessellated or outlined using the {@link #constructPrimitives} or
	 * {@link #constructOutlines} methods, respectively.
	 */
	public void finish()
	{
		if ( _finished )
		{
			throw new IllegalStateException( "finish() called" );
		}

		_lastContourEdge = null;

		final Sweep sweep = new Sweep( this );
		sweep.computeInterior();

		_finished = true;
		_tessellated = false;
		_outlined = false;
	}

	/**
	 * Constructs triangles for interior of mesh.
	 *
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  All triangles in the mesh.
	 */
	public int[] constructTriangles( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		if ( !_finished )
		{
			throw new IllegalStateException( "need finish()" );
		}

		if ( _outlined )
		{
			throw new IllegalStateException( "not possible after outline()" );
		}

		return TesselationConstructor.constructTriangles( this, vertexList, counterClockwise );
	}

	/**
	 * Constructs tessellation of mesh using primitives (triangle fans, triangle
	 * strips, and separate triangles).
	 *
	 * @param   vertexList          List of 2D vertices in tessellation result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  List of primitives that form the tessellation.
	 */
	@NotNull
	public List<TessellationPrimitive> constructPrimitives( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		if ( !_finished )
		{
			throw new IllegalStateException( "need finish()" );
		}

		if ( _outlined )
		{
			throw new IllegalStateException( "not possible after outline()" );
		}

		if ( !_tessellated )
		{
			MonotoneTessellator.tessellateInterior( this );
			_tessellated = true;
		}

		return TesselationConstructor.constructPrimitives( this, vertexList, counterClockwise );
	}

	/**
	 * Construct outlines of mesh. An outline is created for each boundary
	 * between the "inside" and "outside" of the mesh.
	 *
	 * @param   vertexList          List of 2D vertices in tessellation result.
	 * @param   counterClockwise    Construct counter-clockwise outlines.
	 *
	 * @return  Outlines of shape.
	 */
	public List<int[]> constructOutlines( final HashList<Vector2D> vertexList, final boolean counterClockwise )
	{
		if ( !_finished )
		{
			throw new IllegalStateException( "need finish()" );
		}

		if ( !_outlined )
		{
			/*
			 * If the user wants only the boundary contours, we throw away all edges
			 * except those which separate the interior from the exterior.
			 * Otherwise we tessellate all the regions marked "inside".
			 */
			setWindingNumber( 1, true );
			_outlined = true;
		}

		return TesselationConstructor.constructOutlines( this, vertexList, counterClockwise );
	}

	/**** END OF PUBLIC API ****/

	/**
	 * Creates one edge, two vertices, and a loop (face).
	 * The loop consists of the two new half-edges.
	 *
	 * @return  Edge of loop.
	 */
	@NotNull
	HalfEdge createSelfLoopEdge()
	{
		final HalfEdge result = HalfEdge.makeEdge( _edgeListHead );
		makeVertex( result, _vertexListHead );
		makeVertex( result.symmetric, _vertexListHead );
		HalfEdge.makeFace( result, _faceListHead );
		return result;
	}

	/**
	 * Resets the winding numbers on all edges so that regions marked "inside"
	 * the polygon have a winding number of "value", and regions outside have a
	 * winding number of 0.
	 *
	 * @param   windingNumber       Winding number to set on all edges.
	 * @param   keepOutlineOnly     Delete edges which do not separate interior
	 *                              from exterior regions.
	 */
	void setWindingNumber( final int windingNumber, final boolean keepOutlineOnly )
	{
		HalfEdge eNext;

		for ( HalfEdge e = _edgeListHead.next; e != _edgeListHead; e = eNext )
		{
			eNext = e.next;
			if ( e.symmetric.leftFace.inside != e.leftFace.inside )
			{
				/* This is a boundary edge (one side is interior, one is exterior). */
				e.winding = e.leftFace.inside ? windingNumber : -windingNumber;
			}
			else
			{
				/* Both regions are interior, or both are exterior. */
				if ( keepOutlineOnly )
				{
					e.delete();
				}
				else
				{
					e.winding = 0;
				}
			}
		}
	}

	/*
	 * Attaches a new vertex and makes it the
	 * origin of all edges in the vertex loop to which eOrig belongs. "vNext" gives
	 * a place to insert the new vertex in the global vertex list.  We insert
	 * the new vertex *before* vNext so that algorithms which walk the vertex
	 * list will not see the newly created vertices.
	 */
	static Vertex makeVertex( final HalfEdge eOrig, final Vertex vNext )
	{
		final Vertex result = new Vertex();

		final Vertex vPrev;

		/* insert in circular doubly-linked list before vNext */
		vPrev = vNext.prev;
		result.prev = vPrev;
		vPrev.next = result;
		result.next = vNext;
		vNext.prev = result;

		result.anEdge = eOrig;
		result.vertexIndex = -1;
		/* leave coords, s, t undefined */

		/* fix other edges on this vertex loop */
		HalfEdge e = eOrig;
		do
		{
			e.origin = result;
			e = e.ccwAroundOrigin;
		}
		while ( e != eOrig );

		return result;
	}

	/**
	 * Destroys a vertex and removes it from the global
	 * vertex list.  It updates the vertex loop to point to a given new vertex.
	 *
	 * @param   vertex      Vertex to delete.
	 * @param   newOrigin   New origin of affected edges.
	 */
	static void killVertex( final Vertex vertex, final Vertex newOrigin )
	{
		/* change the origin of all affected edges */
		final HalfEdge startEdge = vertex.anEdge;
		HalfEdge edge = startEdge;
		do
		{
			edge.origin = newOrigin;
			edge = edge.ccwAroundOrigin;
		}
		while ( edge != startEdge );

		/* delete from circular double-linked list */
		final Vertex previous = vertex.prev;
		final Vertex next = vertex.next;
		next.prev = previous;
		previous.next = next;
	}

	/**
	 * Destroys a face and removes it from the global face
	 * list.  It updates the face loop to point to a given new face.
	 *
	 * @param   face            Face to remove.
	 * @param   newLeftFace     New left face of affected edges.
	 */
	static void killFace( final Face face, final Face newLeftFace )
	{
		/* change the left face of all affected edges */
		final HalfEdge startEdge = face.anEdge;
		HalfEdge edge = startEdge;
		do
		{
			edge.leftFace = newLeftFace;
			edge = edge.ccwAroundLeftFace;
		}
		while ( edge != startEdge );

		/* delete from circular doubly-linked list */
		final Face previous = face.prev;
		final Face next = face.next;
		next.prev = previous;
		previous.next = next;
	}

	/**
	 * This is the basic operation for changing the mesh connectivity and topology.
	 * It changes the mesh so that
	 * eOrg->Onext <- OLD( eDst->Onext )
	 * eDst->Onext <- OLD( eOrg->Onext )
	 * where OLD(...) means the value before the meshSplice operation.
	 * <p/>
	 * This can have two effects on the vertex structure:
	 * <ul>
	 *  <li>if eOrg->Org != eDst->Org, the two vertices are merged together</li>
	 *  <li>if eOrg->Org == eDst->Org, the origin is split into two vertices</li>
	 * </ul>
	 * In both cases, eDst->Org is changed and eOrg->Org is untouched.
	 * <p/>
	 * Similarly (and independently) for the face structure,
	 * <ul>
	 *  <li>if eOrg->Lface == eDst->Lface, one loop is split into two</li>
	 *  <li>if eOrg->Lface != eDst->Lface, two distinct loops are joined into one</li>
	 * </ul>
	 * In both cases, eDst->Lface is changed and eOrg->Lface is unaffected.
	 * <p/>
	 * Some special cases:
	 * <ul>
	 *  <li>If eDst == eOrg, the operation has no effect.</li>
	 *  <li>If eDst == eOrg->Lnext, the new face will have a single edge.</li>
	 *  <li>If eDst == eOrg->Lprev, the old face will have a single edge.</li>
	 *  <li>If eDst == eOrg->Onext, the new vertex will have a single edge.</li>
	 *  <li>If eDst == eOrg->Oprev, the old vertex will have a single edge.</li>
	 * </ul>
	 */
	static void spliceMesh( final HalfEdge eOrg, final HalfEdge eDst )
	{
		boolean joiningLoops = false;
		boolean joiningVertices = false;

		if ( eOrg != eDst )
		{
			if ( eDst.origin != eOrg.origin )
			{
				/* We are merging two disjoint vertices -- destroy eDst->Org */
				joiningVertices = true;
				killVertex( eDst.origin, eOrg.origin );
			}
			if ( eDst.leftFace != eOrg.leftFace )
			{
				/* We are connecting two disjoint loops -- destroy eDst.Lface */
				joiningLoops = true;
				killFace( eDst.leftFace, eOrg.leftFace );
			}

			/* Change the edge structure */
			HalfEdge.spliceEdge( eDst, eOrg );

			if ( !joiningVertices )
			{
				/* We split one vertex into two -- the new vertex is eDst.Org.
				 * Make sure the old vertex points to a valid half-edge.
				 */
				makeVertex( eDst, eOrg.origin );
				eOrg.origin.anEdge = eOrg;
			}
			if ( !joiningLoops )
			{

				/* We split one loop into two -- the new loop is eDst.Lface.
				 * Make sure the old face points to a valid half-edge.
				 */
				HalfEdge.makeFace( eDst, eOrg.leftFace );
				eOrg.leftFace.anEdge = eOrg;
			}
		}
	}
}
