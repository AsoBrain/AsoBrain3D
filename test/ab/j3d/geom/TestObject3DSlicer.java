/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.model.*;
import junit.framework.*;

/**
 * Test {@link Object3DSlicer} class.
 *
 * @author Peter S. Heijnen
 */
public class TestObject3DSlicer
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestObject3DSlicer.class.getName();

	/**
	 * Test {@link Object3DSlicer#slice} method.
	 * <pre>
	 *                      0------1
	 *           17        .        \
	 *           | \      .          \
	 *           |  \    .            \
	 *          16   \  .        13    2
	 *         /      18        /  \    \
	 *        /                /    \   \
	 *       /                /  6   \   \
	 *      /                /  / \   \   \
	 *     15               /  /   \   \   \
	 *       \             /  / 9   7   \   \
	 *        \           /  /  |\  |    \   \
	 *  -------\---------/--/---|-\-|-----\---\---------------
	 *          \       /  5    |  \|     12   3
	 *           \     /    \   10  8    /    /
	 *            \   /      \   \      /    /
	 *             \ /        \   \    /    /
	 *             14          \   \  /    /
	 *                          \   11    /
	 *                           \       /
	 *                            \     /
	 *                             \   /
	 *                              \ /
	 *                               4
	 * </pre>
	 *
	 * @throws Exception if the test fails.
	 */
	public void testSliceOutline()
	throws Exception
	{
		final String where = CLASS_NAME + ".testSliceOutline()";

		boolean closed = true;
		do
		{
			System.out.println( where + " - closed=" + closed );

			final Object3D object = new Object3D();
			final Matrix3D faceTransform = Matrix3D.getPlaneTransform( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, true );
			final Plane3D facePlane = new BasicPlane3D( faceTransform, true );
			final Face3DBuilder faceBuilder = new Face3DBuilder( object, facePlane.getNormal() );

			final IntArray outline = new IntArray( 20 );
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 70.0, 110.0, 0.0 ), 0.0f, 0.0f ) ); //  0
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 105.0, 95.0, 0.0 ), 0.0f, 0.0f ) ); //  1
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 125.0, 75.0, 0.0 ), 0.0f, 0.0f ) ); //  2
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 165.0, 45.0, 0.0 ), 0.0f, 0.0f ) ); //  3
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0, 0.0, 0.0 ), 0.0f, 0.0f ) ); //  4
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 65.0, 45.0, 0.0 ), 0.0f, 0.0f ) ); //  5
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 90.0, 70.0, 0.0 ), 0.0f, 0.0f ) ); //  6
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0, 55.0, 0.0 ), 0.0f, 0.0f ) ); //  7
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0, 40.0, 0.0 ), 0.0f, 0.0f ) ); //  8
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 95.0, 55.0, 0.0 ), 0.0f, 0.0f ) ); //  9
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 95.0, 40.0, 0.0 ), 0.0f, 0.0f ) ); // 10
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 125.0, 25.0, 0.0 ), 0.0f, 0.0f ) ); // 11
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 145.0, 45.0, 0.0 ), 0.0f, 0.0f ) ); // 12
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 85.0, 75.0, 0.0 ), 0.0f, 0.0f ) ); // 13
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 40.0, 35.0, 0.0 ), 0.0f, 0.0f ) ); // 14
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 0.0, 56.0, 0.0 ), 0.0f, 0.0f ) ); // 15
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 20.0, 66.0, 0.0 ), 0.0f, 0.0f ) ); // 16
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 24.0, 93.0, 0.0 ), 0.0f, 0.0f ) ); // 17
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 51.0, 76.0, 0.0 ), 0.0f, 0.0f ) ); // 18
			if ( closed )
			{
				outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 70.0, 110.0, 0.0 ), 0.0f, 0.0f ) ); // 19
			}
			faceBuilder.addOutline( outline.toArray() );

			object.addFace( BasicAppearances.BLUE, false, true, faceBuilder.buildFace3D() );

			final Plane3D cuttingPlane = new BasicPlane3D( Vector3D.POSITIVE_Z_AXIS, 50.0, true );

			final Object3DSlicer slicer = new Object3DSlicer();
			slicer.setTopEnabled( true );
			slicer.setTopCapped( true );
			slicer.setBottomEnabled( true );
			slicer.setBottomCapped( true );
			slicer.setSliceEnabled( true );
			slicer.slice( object, cuttingPlane );

			final Object3D topObject = slicer.getTopObject();

			final List<FaceGroup> topFaceGroups = topObject.getFaceGroups();
			assertEquals( "Unexpected number of top face groups", 1 , topFaceGroups.size() );
			final FaceGroup topFaceGroup = topFaceGroups.get( 0 );
			final List<Face3D> topFaces = topFaceGroup.getFaces();
			assertEquals( "Unexpected number of top faces", 1 , topFaces.size() );
			final Face3D topFace = topFaces.get( 0 );
			final Tessellation topTessellation = topFace.getTessellation();

			final List<int[]> topOutlines = topTessellation.getOutlines();
			final List<List<Integer>> outlineVertexIndices = new ArrayList<List<Integer>>();

			for ( final int[] topOutline : topOutlines )
			{
				final List<Integer> faceVertexIndices = new ArrayList<Integer>();
				for ( final int vi : topOutline )
				{
					faceVertexIndices.add( faceBuilder.getVertexIndex( topFace.getVertex( vi ).point, 0.0f, 0.0f ) );
				}
				System.out.println( "\ttopOutline = " + faceVertexIndices );
				outlineVertexIndices.add( faceVertexIndices );
			}

			if ( closed )
			{
				assertEquals( "Unexpected number of outlines", 2, topOutlines.size() );
				assertEquals( "Unexpected outline[ 0 ]", Arrays.asList( 0, 1, 2, 19, 20, 13, 21, 22, 15, 16, 17, 18, 0 ), outlineVertexIndices.get( 0 ) );
				assertEquals( "Unexpected outline[ 1 ]", Arrays.asList( 6, 7, 23, 24, 9, 25, 26, 6 ), outlineVertexIndices.get( 1 ) );
			}
			else
			{
				assertEquals( "Unexpected number of outlines", 5, topOutlines.size() );
				assertEquals( "Unexpected outline[ 0 ]", Arrays.asList( 0, 1, 2, 19 ), outlineVertexIndices.get( 0 ) );
				assertEquals( "Unexpected outline[ 1 ]", Arrays.asList( 20, 6, 7, 21 ), outlineVertexIndices.get( 1 ) );
				assertEquals( "Unexpected outline[ 2 ]", Arrays.asList( 22, 9, 23 ), outlineVertexIndices.get( 2 ) );
				assertEquals( "Unexpected outline[ 3 ]", Arrays.asList( 24, 13, 25 ), outlineVertexIndices.get( 3 ) );
				assertEquals( "Unexpected outline[ 4 ]", Arrays.asList( 26, 15, 16, 17, 18 ), outlineVertexIndices.get( 4 ) );
			}

			closed = !closed;
		}
		while ( !closed );
	}
}
