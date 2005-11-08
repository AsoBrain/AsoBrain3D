/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import java.awt.Color;
import java.util.List;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.java2d.Java2dModel;

/**
 * This class tests the {@link IntersectionSupport} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 * @see IntersectionSupport
 */
public class TestIntersectionSupport
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestIntersectionSupport.class.getName();

	/**
	 * Fixture instance.
	 */
	private IntersectionSupport _intersectionSupport;

	private ViewModel _viewModel;
	private Object3D _plane1;
	private Object3D _plane2;
	private Object3D _plane3;
	private Object3D _plane4;
	private Object3D _plane5;
	private Object3D _plane6;
	private Object3D _plane7;

	private Matrix3D _transform1;
	private Matrix3D _transform2;
	private Matrix3D _transform3;
	private Matrix3D _transform4;
	private Matrix3D _transform5;
	private Matrix3D _transform6;
	private Matrix3D _transform7;

	private Node3DCollection _collection;


	/**
	 * Setup text fixture.
	 *
	 * @throws Exception if there was a problem setting up the fixture.
	 */
	public void setUp()
	throws Exception
	{
		super.setUp();

		_collection = new Node3DCollection();
		_viewModel = new Java2dModel();


		_plane1 = createPlane( 100.0 );
		_plane1.setTag( "Plane 1" );
		_transform1 = Matrix3D.getTransform( 90, 0, 0, 0, 0, 0);
		_viewModel.createNode( "plane1" , _transform1 , _plane1 , null , 1.0f );
		_collection.add( _transform1, _plane1 );

		_plane2 = createPlane( 100.0 );
		_plane2.setTag( "Plane 2" );
		_transform2 = Matrix3D.getTransform( 90, 0, 0, 10, -1, 0);
		_collection.add( _transform2, _plane2 );

		_plane3 = createPlane( 100.0 );
		_plane3.setTag( "Plane 3" );
		_transform3 = Matrix3D.getTransform( 90, 0, 0, -10, -20, 0);
		_collection.add( _transform3, _plane3 );

		_plane4 = createPlane( 100.0 );
		_plane4.setTag( "Plane 4" );
		_transform4 = Matrix3D.getTransform( 45, 90, 0, -150, 0, 0);
		_collection.add( _transform4, _plane4 );

		_plane5 = createPlane( 100.0 );
		_plane5.setTag( "Plane 5" );
		_transform5 = Matrix3D.getTransform( -45, 90, 0, -150, 0, 0);
		_collection.add( _transform5, _plane5 );

		_plane6 = createPlane( 100.0 );
		_plane6.setTag( "Plane 6" );
		_transform6 = Matrix3D.getTransform( 90, 0, 0, 150, 0, 0);
		_collection.add( _transform6, _plane6 );

		_plane7 = createPlane( 100.0 );
		_plane7.setTag( "Plane 7" );
		_transform7 = Matrix3D.getTransform( 0, 90, 0, 150, 0, 0);
		_collection.add( _transform7, _plane7 );

		_intersectionSupport = new IntersectionSupport( );

	}

	/**
	 * Tear down test fixture.
	 *
	 * @throws Exception if there was a problem tearing down the fixture.
	 */
	public void tearDown()
	throws Exception
	{
		_intersectionSupport = null;

		super.tearDown();
	}

	/**
	 * Test the {@link IntersectionSupport#getIntersectingFaces} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testGetIntersectingFaces()
	throws Exception
	{
		List selection;

		System.out.println( "\n\n" + CLASS_NAME + ".testFaceSelection( Object3D , Matrix3D , Vector3D , Vector3D )" );

		final Matrix3D viewTransform = Matrix3D.INIT.set( 1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0);

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( 0, 0, -500 ), Vector3D.INIT.set( 0, 0, 500 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		Object tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 1" , tag1);

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( -25, -25, -500 ), Vector3D.INIT.set( -25, -25, 500 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 1" , tag1);

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( -25, 25, -50 ), Vector3D.INIT.set( 25, 25, 50 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 1" , tag1);

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( 50, 50, -500 ), Vector3D.INIT.set( 50, 50, 500 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 1" , tag1);

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( 50.1, 0, -500 ), Vector3D.INIT.set( 50.1, 0, 500 ) );
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

		System.out.println( "\nTesting intersection with plane 1" );
		selection = _intersectionSupport.getIntersectingFaces( _plane1, _transform1.multiply( viewTransform ) , Vector3D.INIT.set( 0, 50.1, -500 ), Vector3D.INIT.set( 0, 50.1, 500 ) );
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _plane7, _transform7.multiply( viewTransform ) , Vector3D.INIT.set( 150, 0, -500 ), Vector3D.INIT.set( 150, 0, 500 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 7" , tag1);

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _plane7, _transform7.multiply( viewTransform ) , Vector3D.INIT.set( 150.1, 0, -500 ), Vector3D.INIT.set( 150.1, 0, 500 ) );
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _plane7, _transform7.multiply( viewTransform ) , Vector3D.INIT.set( 150, 50.1, -500 ), Vector3D.INIT.set( 150, 50.1, 500 ) );
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _plane7, _transform7.multiply( viewTransform ) , Vector3D.INIT.set( 100, 0, 25 ), Vector3D.INIT.set( 200, 0, 25 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 7" , tag1);

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _plane7, _transform7.multiply( viewTransform ) , Vector3D.INIT.set( -500, 0, 200 ), Vector3D.INIT.set( 500, 0, 200 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((IntersectionSupport.Intersection)selection.get( 0)).object.getTag();
		assertEquals( "The intersected wrong object was intersected" , "Plane 7" , tag1);


		System.out.println( "\n\n" + CLASS_NAME + ".testGetSelection( Node3DCollection , Vector3D , Vector3D )" );

		System.out.println( "\nTesting intersection with planes 1 and 3" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( -45, -500, 0) , Vector3D.INIT.set( -45, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		Object tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 3" ) && tag2.equals( "Plane 1" ));

		System.out.println( "\nTesting intersection with planes 1 and 2" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( 45, -500, 0) , Vector3D.INIT.set( 45, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 2" ) && tag2.equals( "Plane 1" ));

		System.out.println( "\nTesting intersection with planes 1, 2 and 3" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( 0, -500, 0) , Vector3D.INIT.set( 0, 500, 0 ));
		assertTrue( "The number of intersected faces is not 6, but " + selection.size() , selection.size() == 6 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		Object tag3 = ((Face3D)selection.get( 4)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 3" ) && tag2.equals( "Plane 2" ) && tag3.equals( "Plane 1" ));

		System.out.println( "\nTesting intersection with planes 4 and 5" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( -125, -500, 0) , Vector3D.INIT.set( -125, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 4" ) && tag2.equals( "Plane 5" ));

		System.out.println( "\nTesting intersection with planes 4 and 5" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( -149.9, -500, 0) , Vector3D.INIT.set( -149.9, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 4" ) && tag2.equals( "Plane 5" ));

		System.out.println( "\nTesting intersection with planes 4 and 5" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( -150, -500, 0) , Vector3D.INIT.set( -150, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );

		System.out.println( "\nTesting intersection with planes 6 and 7" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( 150, -500, 0) , Vector3D.INIT.set( 150, 500, 0 ));
		assertTrue( "The number of intersected faces is not 4, but " + selection.size() , selection.size() == 4 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		tag2 = ((Face3D)selection.get( 2)).getObject().getTag();
		assertTrue( "The planes are not listed from front to back" , tag1.equals( "Plane 7" ) && tag2.equals( "Plane 6" ));

		System.out.println( "\nTesting intersection with plane 7" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( 100, 25, 0 ), Vector3D.INIT.set( 200, 25, 0 ) );
		assertTrue( "The number of intersected faces is not 2, but " + selection.size() , selection.size() == 2 );
		tag1 = ((Face3D)selection.get( 0)).getObject().getTag();
		assertTrue( "The intersected plane is not plane 7" , tag1.equals( "Plane 7" ));

		System.out.println( "\nTesting no intersections" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( 100, 100, 0 ), Vector3D.INIT.set( 200, 100, 0 ) );
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

		System.out.println( "\nTesting no intersections" );
		selection = _intersectionSupport.getIntersectingFaces( _collection, Vector3D.INIT.set( -60.1, -500, 0) , Vector3D.INIT.set( -60.1, 500, 0 ));
		assertTrue( "The number of intersected faces is not 0, but " + selection.size() , selection.size() == 0 );

	}

	/**
	 * Creates a new Object3D in a plane shape, with a top and a bottom face.
	 * @param size The size of the plane
	 * @return The plane object
	 */
	private Object3D createPlane( final double size )
	{
		final double halfSize = size / 2;
		final Vector3D lf = Vector3D.INIT.set( -halfSize , -halfSize , 0.0 );
		final Vector3D rf = Vector3D.INIT.set(  halfSize , -halfSize , 0.0 );
		final Vector3D rb = Vector3D.INIT.set(  halfSize ,  halfSize , 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -halfSize ,  halfSize , 0.0 );

		final TextureSpec red     = new TextureSpec( Color.red     );
		final TextureSpec green   = new TextureSpec( Color.green   );

		final Object3D plane = new Object3D();
		/* top    */ plane.addFace( new Vector3D[] { lf , lb , rb , rf } , red     , false , false ); // Z =  size
		/* bottom */ plane.addFace( new Vector3D[] { lb , lf , rf , rb } , green   , false , false ); // Z = -size

		return plane;
	}
/*
		final Vector3D lf = Vector3D.INIT.set( -halfSize , 0.0 , -halfSize );
		final Vector3D rf = Vector3D.INIT.set(  halfSize , 0.0 , -halfSize );
		final Vector3D rb = Vector3D.INIT.set(  halfSize , 0.0 ,  halfSize );
		final Vector3D lb = Vector3D.INIT.set( -halfSize , 0.0 ,  halfSize );
*/
}
