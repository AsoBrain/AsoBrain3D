/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.awt.Color;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.java2d.Java2dModel;

/**
 * This class tests the {@link ViewInputTranslator} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 * @see ViewInputTranslator
 */
public class TestViewInputTranslator
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestViewInputTranslator.class.getName();

	/**
	 * Fixture instance.
	 */
	private ViewInputTranslator _translator;

	private Object3D _plane1;
	private Object3D _plane2;

	private Matrix3D _transform1;
	private Matrix3D _transform2;

	/**
	 * Setup text fixture.
	 *
	 * @throws Exception if there was a problem setting up the fixture.
	 */
	public void setUp()
	throws Exception
	{
		super.setUp();

		Projector projector = Projector.createInstance( Projector.PARALLEL, 100, 100, 1, 1, 10, 1000, Math.toRadians( 45 ) , 1.0);
		ViewModel model = new Java2dModel();
		model.createView( "view" , new FromToViewControl(Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 ), Vector3D.INIT ) );
		ViewModelView view = model.getView( "view" );

		_plane1 = createPlane( 100.0 );
		_plane1.setTag( "Plane 1" );
		_transform1 = Matrix3D.getTransform( 90, 0, 0, 0, 0, 0);
		model.createNode( "plane1" , _transform1 , _plane1 , null , 1.0f );

		_plane2 = createPlane( 100.0 );
		_plane2.setTag( "Plane 2" );
		_transform2 = Matrix3D.getTransform( 0, 90, 0, 150, 0, 0);
		model.createNode( "plane2" , _transform2 , _plane2 , null , 1.0f );


		_translator = new ViewInputTranslator(view, model, projector);
	}

	/**
	 * Tear down test fixture.
	 *
	 * @throws Exception if there was a problem tearing down the fixture.
	 */
	public void tearDown()
	throws Exception
	{
		_translator = null;

		super.tearDown();
	}

	/**
	 * Test the {@link ViewInputTranslator#getScene} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testGetScene()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetScene()" );

		Node3DCollection scene = _translator.getScene();

		assertEquals( "There are not two objects in the scene, but " + scene.size() , 2 , scene.size());

		assertTrue( "The nodes in the scene are not Object3Ds", scene.getNode( 0) instanceof Object3D && scene.getNode( 1 ) instanceof Object3D );

		Object3D node1 = (Object3D)scene.getNode( 0 );
		Object3D node2 = (Object3D)scene.getNode( 1 );
		Matrix3D matrix1 = scene.getMatrix( 0 );
		Matrix3D matrix2 = scene.getMatrix( 1 );

		assertTrue( "Plane 1 is not in the scene" , node1.getTag().equals( "Plane 1") || node2.getTag().equals( "Plane 1"));
		assertTrue( "Plane 1 is not in the scene" , node1.getTag().equals( "Plane 2") || node2.getTag().equals( "Plane 2"));

		Matrix3D transform1 = node1.getTag().equals( "Plane 1" ) ? matrix1 : matrix2;
		Matrix3D transform2 = node1.getTag().equals( "Plane 1" ) ? matrix2 : matrix1;

		assertTrue( "Plane 1's matrix has not been transformed properly" , transform1.almostEquals( Matrix3D.INIT.set( 1.0 , 0.0 , 0.0 ,   0.0 ,  0.0 , -1.0 , 0.0 , 0.0 , 0.0 ,  0.0 , -1.0 , -1000 ) ) );
		assertTrue( "Plane 2's matrix has not been transformed properly" , transform2.almostEquals( Matrix3D.INIT.set( 0.0 , 0.0 , 1.0 , 150.0 , -1.0 ,  0.0 , 0.0 , 0.0 , 0.0 , -1.0 ,  0.0 , -1000 ) ) );
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
}
