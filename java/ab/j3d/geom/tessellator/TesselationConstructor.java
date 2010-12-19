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

import java.awt.geom.*;
import java.util.*;

import ab.j3d.geom.*;
import com.numdata.oss.*;

/**
 * This constructs primitives and outlines from a {@link Mesh}.
 */
class TesselationConstructor
{
	private TesselationConstructor()
	{
	}

	/**
	 * Constructs triangles for interior of mesh.
	 *
	 * @param   mesh                Mesh to build tesselation of.
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  All triangles in the mesh.
	 */
	static int[] constructTriangles( final Mesh mesh, final HashList<Point2D> vertexList, final boolean counterClockwise )
	{
		int vertexCount = 0;

		for ( Face face = mesh._faceListHead.next; face != mesh._faceListHead; face = face.next )
		{
			if ( face.inside )
			{
				vertexCount += 3;
			}
		}

		final int[] vertices = new int[ vertexCount ];

		if ( counterClockwise )
		{
			vertexCount = 0;

			for ( Face face = mesh._faceListHead.next; face != mesh._faceListHead; face = face.next )
			{
				if ( face.inside )
				{
					/* Loop once for each edge (there will always be 3 edges) */
					HalfEdge e = face.anEdge;
					do
					{
						vertices[ vertexCount++ ] = getVertexIndex( vertexList, e.origin );
						e = e.ccwAroundLeftFace;
					}
					while ( e != face.anEdge );
				}
			}
		}
		else
		{
			for ( Face face = mesh._faceListHead.next; face != mesh._faceListHead; face = face.next )
			{
				if ( face.inside )
				{
					/* Loop once for each edge (there will always be 3 edges) */
					HalfEdge e = face.anEdge;
					do
					{
						vertices[ --vertexCount ] = getVertexIndex( vertexList, e.origin );
						e = e.ccwAroundLeftFace;
					}
					while ( e != face.anEdge );
				}
			}
		}

		return vertices;
	}

	/**
	 * Constructs tessellation of mesh using primitives (triangle fans, triangle
	 * strips, and separate triangles).
	 * <p>
	 * A substantial effort is made to use as few rendering primitives as
	 * possible (ie. to make the fans and strips as large as possible).
	 *
	 * @param   mesh                Mesh to build tesselation of.
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  List of primitives that form the tessellation.
	 */
	static List<TessellationPrimitive> constructPrimitives( final Mesh mesh, final HashList<Point2D> vertexList, final boolean counterClockwise )
	{
		final List<TessellationPrimitive> result = new LinkedList<TessellationPrimitive>();

		/* Make a list of separate triangles so we can render them all at once */
		Face triangleList = null;

		for ( Face face = mesh._faceListHead.next; face != mesh._faceListHead; face = face.next )
		{
			face.rendered = false;
		}

		for ( Face face = mesh._faceListHead.next; face != mesh._faceListHead; face = face.next )
		{
			/*
			 * We examine all faces in an arbitrary order.  Whenever we find
			 * an unprocessed face F, we output a group of faces including F
			 * whose size is maximum.
			 */
			if ( face.inside && !face.rendered )
			{
				final TessellationPrimitive primitive = buildMaximumPrimitive( face, vertexList, counterClockwise );
				if ( primitive != null )
				{
					result.add( primitive );
				}
				else
				{
					triangleList = markRenderedAndPushOnStack( triangleList, face );
				}
			}
		}

		if ( triangleList != null )
		{
			result.add( createTriangleList( triangleList, vertexList, counterClockwise ) );
		}

		return result;
	}

	/**
	 * Construct outlines of the given mesh. An outline is created for each
	 * boundary between the "inside" and "outside" of the mesh.
	 * <p>
	 * NOTE THAT THIS DELETES ALL EDGES WHICH DO NOT SEPARATE AN INTERIOR REGION
	 * FROM AN EXTERIOR ONE, SO IT ALWAYS BE CALLED AFTER USING THE MESH FOR
	 * TESSELLATION PURPOSES.
	 *
	 * @param   mesh                Mesh to build tesselation of.
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise outlines.
	 *
	 * @return  Outlines of shape.
	 */
	static List<int[]> constructOutlines( final Mesh mesh, final HashList<Point2D> vertexList, final boolean counterClockwise )
	{
		final List<int[]> result = new LinkedList<int[]>();

		for ( Face f = mesh._faceListHead.next; f != mesh._faceListHead; f = f.next )
		{
			if ( f.inside )
			{
				int vertexCount = 0;
				HalfEdge e = f.anEdge;
				do
				{
					vertexCount++;
					e = e.ccwAroundLeftFace;
				}
				while ( e != f.anEdge );

				final int[] vertices = new int[ vertexCount ];

				if ( counterClockwise )
				{
					vertexCount = 0;
				}

				e = f.anEdge;
				do
				{
					vertices[ counterClockwise ? vertexCount++ : --vertexCount ] = getVertexIndex( vertexList, e.origin );
					e = e.ccwAroundLeftFace;
				}
				while ( e != f.anEdge );

				result.add( vertices );
			}
		}

		return result;
	}

	/**
	 * We want to find the largest triangle fan or strip of unmarked faces
	 * which includes the given face fOrig.  There are 3 possible fans
	 * passing through fOrig (one centered at each vertex), and 3 possible
	 * strips (one for each CCW permutation of the vertices).  Our strategy
	 * is to try all of these, and take the primitive which uses the most
	 * triangles (a greedy approach).
	 *
	 * @param   face                Face to create primitive for.
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  <code>TessellationPrimitive</code> that was built;
	 *          <code>null</code> if face is a lonely triangle.
	 */
	private static TessellationPrimitive buildMaximumPrimitive( final Face face, final HashList<Point2D> vertexList, final boolean counterClockwise )
	{
		final HalfEdge edge1 = face.anEdge;
		final HalfEdge edge2 = edge1.ccwAroundLeftFace;
		final HalfEdge edge3 = edge1.ccwAroundOrigin.symmetric;

		PrimitiveFactoryMethod primitiveFactory = new TriangleFanMethod( edge1, counterClockwise );

		PrimitiveFactoryMethod candidate = new TriangleFanMethod( edge2, counterClockwise );
		if ( candidate.getTriangleCount() > primitiveFactory.getTriangleCount() )
		{
			primitiveFactory = candidate;
		}

		candidate = new TriangleFanMethod( edge3, counterClockwise );
		if ( candidate.getTriangleCount() > primitiveFactory.getTriangleCount() )
		{
			primitiveFactory = candidate;
		}

		candidate = new TriangleStripMethod( edge1, counterClockwise );
		if ( candidate.getTriangleCount() > primitiveFactory.getTriangleCount() )
		{
			primitiveFactory = candidate;
		}

		candidate = new TriangleStripMethod( edge2, counterClockwise );
		if ( candidate.getTriangleCount() > primitiveFactory.getTriangleCount() )
		{
			primitiveFactory = candidate;
		}

		candidate = new TriangleStripMethod( edge3, counterClockwise );
		if ( candidate.getTriangleCount() > primitiveFactory.getTriangleCount() )
		{
			primitiveFactory = candidate;
		}

		return ( primitiveFactory.getTriangleCount() > 1 ) ? primitiveFactory.createPrimitive( vertexList ) : null;
	}

	/**
	 * Now we render all the separate triangles which could not be
	 * grouped into a triangle fan or strip.
	 *
	 * @param   triangleList        Triangles to create list of.
	 * @param   vertexList          List of 2D vertices to use in result.
	 * @param   counterClockwise    Construct counter-clockwise primitives.
	 *
	 * @return  {@link TriangleList}.
	 */
	private static TriangleList createTriangleList( final Face triangleList, final HashList<Point2D> vertexList, final boolean counterClockwise )
	{
		int vertexCount = 0;
		for ( Face face = triangleList; face != null; face = face.renderStack )
		{
			vertexCount += 3;
		}

		final int[] vertices = new int[ vertexCount ];

		if ( counterClockwise )
		{
			vertexCount = 0;

			for ( Face f = triangleList; f != null; f = f.renderStack )
			{
				/* Loop once for each edge (there will always be 3 edges) */
				HalfEdge e = f.anEdge;
				do
				{
					vertices[ vertexCount++ ] = getVertexIndex( vertexList, e.origin );
					e = e.ccwAroundLeftFace;
				}
				while ( e != f.anEdge );
			}
		}
		else
		{
			for ( Face f = triangleList; f != null; f = f.renderStack )
			{
				/* Loop once for each edge (there will always be 3 edges) */
				HalfEdge e = f.anEdge;
				do
				{
					vertices[ --vertexCount ] = getVertexIndex( vertexList, e.origin );
					e = e.ccwAroundLeftFace;
				}
				while ( e != f.anEdge );
			}
		}

		return new TriangleList( vertices );
	}

	/**
	 * Get vertex index for the given vertex from a vertex list. The vertex is
	 * added if it is not already in the list.
	 *
	 * @param   vertexList  List of 2D vertices.
	 * @param   vertex      Vertex to get index of.
	 *
	 * @return  {@link Vertex#vertexIndex} (new index may be assigned).
	 */
	protected static int getVertexIndex( final HashList<Point2D> vertexList, final Vertex vertex )
	{
		int vertexIndex = vertex.vertexIndex;
		if ( vertexIndex < 0 )
		{
			vertexIndex = vertexList.indexOfOrAdd( vertex );
			vertex.vertexIndex = vertexIndex;
		}
		return vertexIndex;
	}

	/**
	 * This pushes a face on a render stack.
	 *
	 * @param   stack   Previous top element of stack (may be <code>null</code>).
	 * @param   face    Face to push on the stack.
	 *
	 * @return  New top element of stack (this is the given <code>face</code>).
	 */
	private static Face markRenderedAndPushOnStack( final Face stack, final Face face )
	{
		face.rendered = true;
		face.renderStack = stack;
		return face;
	}

	/**
	 * Clear render stack. This clears the {@link Face#rendered} flag
	 *
	 * @param   stack   Top element of stack (<code>null</code> if empty).
	 */
	private static void clearRenderStack( final Face stack )
	{
		Face stackElement = stack;
		while ( stackElement != null )
		{
			final Face next = stackElement.renderStack;
			stackElement.rendered = false;
			stackElement.renderStack = null;
			stackElement = next;
		}
	}

	/**
	 * This interface encapsulates a method for rendering a primitive. It
	 * determines the number of triangles that will be in the created primitive,
	 * so it can be used to decide which the rendering method that produces a
	 * primitive with the most triangles.
	 */
	private interface PrimitiveFactoryMethod
	{
		/**
		 * Get number of triangles in the primitive that will be created.
		 *
		 * @return  Number of triangles.
		 */
		int getTriangleCount();

		/**
		 * Create primitive for tessellation.
		 *
		 * @param   vertexList  List of 2D vertices to use in result.
		 *
		 * @return  {@link TessellationPrimitive}.
		 */
		TessellationPrimitive createPrimitive( final HashList<Point2D> vertexList );
	}

	/**
	 * This creates a {@link TriangleFan}.
	 */
	private static class TriangleFanMethod
		implements PrimitiveFactoryMethod
	{
		/**
		 * Resulting primitive will be counter-clockwise vs. clockwise.
		 */
		private final boolean _counterClockwise;

		/**
		 * Edge to start with when creating the primitive.
		 */
		private final HalfEdge _startEdge;

		/**
		 * Number of triangles that will be in the resulting primitive.
		 */
		private final int _triangleCount;

		/**
		 * Construct factory for primitive that includes the given edge.
		 *
		 * @param   anEdge              Edge to create primitive from.
		 * @param   counterClockwise    Create counter-clockwise primitive.
		 */
		TriangleFanMethod( final HalfEdge anEdge, final boolean counterClockwise )
		{
			Face renderStack = null;
			int triangleCount = 0;

			HalfEdge leftEdge = anEdge;
			while ( true )
			{
				final Face leftFace = leftEdge.leftFace;
				if ( !leftFace.inside || leftFace.rendered )
				{
					break;
				}

				renderStack = markRenderedAndPushOnStack( renderStack, leftFace );
				triangleCount++;

				leftEdge = leftEdge.ccwAroundOrigin;
			}

			HalfEdge rightEdge = anEdge;
			while ( true )
			{
				final Face rightFace = rightEdge.symmetric.leftFace;
				if ( !rightFace.inside || rightFace.rendered )
				{
					break;
				}

				renderStack = markRenderedAndPushOnStack( renderStack, rightFace );
				triangleCount++;

				rightEdge = rightEdge.symmetric.ccwAroundLeftFace;
			}

			clearRenderStack( renderStack );

			_startEdge = counterClockwise ? rightEdge : leftEdge;
			_triangleCount = triangleCount;
			_counterClockwise = counterClockwise;
		}

		@Override
		public int getTriangleCount()
		{
			return _triangleCount;
		}

		@Override
		public TriangleFan createPrimitive( final HashList<Point2D> vertexList )
		{
			final int[] vertices = new int[ _triangleCount + 2 ];
			int vertexIndex = 0;

			HalfEdge edge = _startEdge;
			vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.origin );
			vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.symmetric.origin );

			if ( _counterClockwise )
			{
				while ( true )
				{
					final Face leftFace = edge.leftFace;
					if ( !leftFace.inside || leftFace.rendered )
					{
						break;
					}

					leftFace.rendered = true;
					edge = edge.ccwAroundOrigin;
					vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.symmetric.origin );
				}
			}
			else /* clockwise */
			{
				while ( true )
				{
					final Face rightFace = edge.symmetric.leftFace;
					if ( !rightFace.inside || rightFace.rendered )
					{
						break;
					}

					rightFace.rendered = true;
					edge = edge.symmetric.ccwAroundLeftFace;
					vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.symmetric.origin );
				}
			}

			return new TriangleFan( vertices );
		}
	}

	/**
	 * This creates a {@link TriangleStrip}.
	 */
	private static class TriangleStripMethod
		implements PrimitiveFactoryMethod
	{
		/**
		 * Resulting primitive will be counter-clockwise vs. clockwise.
		 */
		private final boolean _counterClockwise;

		/**
		 * Edge to start with when creating the primitive.
		 */
		private final HalfEdge _startEdge;

		/**
		 * Number of triangles that will be in the resulting primitive.
		 */
		private final int _triangleCount;

		/**
		 * Construct factory for primitive that includes the given edge.
		 *
		 * @param   anEdge              Edge to create primitive from.
		 * @param   counterClockwise    Create counter-clockwise primitive.
		 */
		TriangleStripMethod( final HalfEdge anEdge, final boolean counterClockwise )
		{
			/*
			 * Here we are looking for a maximal strip starting at a given edge.
			 * <p/>
			 * We walk forward and backward as far as possible. However for
			 * strips there is a twist: to get the correct CW/CCW orientations,
			 * there must be an *even* number of triangles in the strip on one
			 * side of <code>anEdge</code>.
			 *
			 * We walk the strip starting on a side with an even number of
			 * triangles; if both side have an odd number, we are forced to
			 * shorten one side.
			 */
			int rightSize = 0;
			int leftSize = 0;
			Face renderStack = null;

			HalfEdge leftEdge = anEdge;
			for ( boolean dir = counterClockwise; ; dir = !dir )
			{
				final Face leftFace = leftEdge.leftFace;
				if ( !leftFace.inside || leftFace.rendered )
				{
					break;
				}

				renderStack = markRenderedAndPushOnStack( renderStack, leftFace );
				leftSize++;

				leftEdge = dir ? leftEdge.ccwAroundLeftFace.symmetric : leftEdge.ccwAroundOrigin;
			}

			HalfEdge rightEdge = anEdge;
			for ( boolean dir = counterClockwise; ; dir = !dir )
			{
				final Face rightFace = rightEdge.symmetric.leftFace;
				if ( !rightFace.inside || rightFace.rendered )
				{
					break;
				}

				renderStack = markRenderedAndPushOnStack( renderStack, rightFace );
				rightSize++;

				rightEdge = dir ? rightEdge.symmetric.ccwAroundLeftFace : rightEdge.symmetric.ccwAroundOrigin.symmetric;
			}

			clearRenderStack( renderStack );

			final int triangleCount;
			final HalfEdge startEdge;

			if ( ( leftSize & 1 ) == 0 ) /* even */
			{
				startEdge = leftEdge.symmetric;
				triangleCount = leftSize + rightSize;
			}
			else if ( ( rightSize & 1 ) == 0 ) /* even */
			{
				startEdge = rightEdge;
				triangleCount = leftSize + rightSize;
			}
			else
			{
				/*
				 * Both sides have odd length, we must shorten one of them.
				 * In fact, we must start from the right side to guarantee
				 * inclusion of {@link HalfEdge#leftFace anEdge.leftFace}.
				 */
				startEdge = counterClockwise ? rightEdge.ccwAroundOrigin : rightEdge.ccwAroundLeftFace.symmetric;
				triangleCount = leftSize + rightSize - 1;
			}

			_counterClockwise = counterClockwise;
			_startEdge = startEdge;
			_triangleCount = triangleCount;
	}

		@Override
		public int getTriangleCount()
		{
			return _triangleCount;
		}

		@Override
		public TriangleStrip createPrimitive( final HashList<Point2D> vertexList )
		{
			final boolean counterClockwise = _counterClockwise;
			HalfEdge edge = _startEdge;

			final int[] vertices = new int[ _triangleCount + 2 ];
			vertices[ 0 ] = getVertexIndex( vertexList, counterClockwise ? edge.origin : edge.symmetric.origin );
			vertices[ 1 ] = getVertexIndex( vertexList, counterClockwise ? edge.symmetric.origin : edge.origin );
			int vertexIndex = 2;

			for ( boolean dir = counterClockwise; ; dir = !dir )
			{
				final Face leftFace = edge.leftFace;
				if ( !leftFace.inside || leftFace.rendered )
				{
					break;
				}

				leftFace.rendered = true;

				if ( dir )
				{
					edge = edge.ccwAroundLeftFace.symmetric;
					vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.origin );
				}
				else
				{
					edge = edge.ccwAroundOrigin;
					vertices[ vertexIndex++ ] = getVertexIndex( vertexList, edge.symmetric.origin );
				}
			}

			return new TriangleStrip( vertices );
		}
	}
}
