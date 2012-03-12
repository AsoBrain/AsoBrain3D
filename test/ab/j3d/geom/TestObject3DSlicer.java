package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.model.*;
import junit.framework.*;

/**
 * Test {@link Object3DSlicer} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestObject3DSlicer
	extends TestCase
{
	/**
	 * Test {@link Object3DSlicer#sliceOutline} method.
	 * <pre>
	 *                      0------1
	 *           17        /        \
	 *           | \      /          \
	 *           |  \    /            \
	 *          16   \  /        13    2
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
		boolean closed = true;
		do
		{
			final Object3D object = new Object3D();
			final Matrix3D faceTransform = Matrix3D.getPlaneTransform( Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS, true );
			final BasicPlane3D facePlane = new BasicPlane3D( faceTransform, true );
			final Face3DBuilder faceBuilder = new Face3DBuilder( object, facePlane.getNormal() );

			final IntArray outline = new IntArray( 20 );
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  70.0, 110.0, 0.0 ), 0.0f, 0.0f ) ); //  0
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 105.0,  95.0, 0.0 ), 0.0f, 0.0f ) ); //  1
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 125.0,  75.0, 0.0 ), 0.0f, 0.0f ) ); //  2
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 165.0,  45.0, 0.0 ), 0.0f, 0.0f ) ); //  3
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0,   0.0, 0.0 ), 0.0f, 0.0f ) ); //  4
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  65.0,  45.0, 0.0 ), 0.0f, 0.0f ) ); //  5
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  90.0,  70.0, 0.0 ), 0.0f, 0.0f ) ); //  6
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0,  55.0, 0.0 ), 0.0f, 0.0f ) ); //  7
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 120.0,  40.0, 0.0 ), 0.0f, 0.0f ) ); //  8
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  95.0,  55.0, 0.0 ), 0.0f, 0.0f ) ); //  9
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  95.0,  40.0, 0.0 ), 0.0f, 0.0f ) ); // 10
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 125.0,  25.0, 0.0 ), 0.0f, 0.0f ) ); // 11
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform( 145.0,  45.0, 0.0 ), 0.0f, 0.0f ) ); // 12
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  85.0,  75.0, 0.0 ), 0.0f, 0.0f ) ); // 13
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  40.0,  35.0, 0.0 ), 0.0f, 0.0f ) ); // 14
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(   0.0,  56.0, 0.0 ), 0.0f, 0.0f ) ); // 15
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  20.0,  66.0, 0.0 ), 0.0f, 0.0f ) ); // 16
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  24.0,  93.0, 0.0 ), 0.0f, 0.0f ) ); // 17
			outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  51.0,  76.0, 0.0 ), 0.0f, 0.0f ) ); // 18
			if ( closed )
			{
				outline.add( faceBuilder.getVertexIndex( faceTransform.transform(  70.0, 110.0, 0.0 ), 0.0f, 0.0f ) ); // 19
			}
			faceBuilder.addOutline( outline.toArray() );

			object.addFace( BasicAppearances.BLUE, false, true, faceBuilder.buildFace3D() );

			final BasicPlane3D cuttingPlane = new BasicPlane3D( Vector3D.POSITIVE_Z_AXIS, 50.0, true );

			final Object3DSlicer slicer = new Object3DSlicer();
			slicer.slice( object, cuttingPlane );

			final Object3D topObject = slicer.getTopObject();
			final List<FaceGroup> topFaceGroups = topObject.getFaceGroups();
			final FaceGroup topFaceGroup = topFaceGroups.get( 0 );
			final List<Face3D> topFaces = topFaceGroup.getFaces();
			final Face3D topFace = topFaces.get( 0 );
			final Tessellation topTessellation = topFace.getTessellation();
			final List<int[]> topOutlines = topTessellation.getOutlines();

			for ( int[] topOutline : topOutlines )
			{
				System.out.println( "topOutline = " + Arrays.toString( topOutline ) );
			}

			closed = !closed;
		}
		while ( !closed );
	}
}
