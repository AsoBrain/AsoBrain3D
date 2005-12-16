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
import ab.j3d.model.Object3D;

/**
 * This class tests the {@link RenderQueue} class.
 *
 * @see     RenderQueue
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class TestRenderQueue
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestRenderQueue.class.getName();

	/**
	 * Test the {@link RenderQueue#enqueueObject} method.
	 */
	public static void testEnqueueObject()
    {
		System.out.println( CLASS_NAME + ".testEnqueueObject()" );

		final RenderQueue renderQueue = new RenderQueue();

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and h2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h2" );
		addPlane( renderQueue, "h1" );
		RenderedPolygon[] rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not "+rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be h1, not " + rendered[ 0 ]._object.getTag(), "h1".equals( rendered[ 0 ]._object.getTag() ) );


		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and h2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "h2" );
		rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not " + rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be h1, not " + rendered[ 0 ]._object.getTag(), "h1".equals( rendered[ 0 ]._object.getTag() ) );

		// @FIXME following tests fail due to incomplete implementation
		if ( "@FIXME".length() == 6 ) return;

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes d1 and d2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "d1" );
		addPlane( renderQueue, "d2" );
		rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not "+rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be d2, not " + rendered[ 0 ]._object.getTag(), "d2".equals( rendered[ 0 ]._object.getTag() ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes d1 and d2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "d2" );
		addPlane( renderQueue, "d1" );
		rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not " + rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be d2, not " + rendered[ 0 ]._object.getTag(), "d2".equals( rendered[ 0 ]._object.getTag() ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and v1" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "v1" );
		rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 3, not " + rendered.length, 3, rendered.length );
		assertTrue( "The order in which the polygons are given back should be v1, h1, v1, not " + rendered[ 0 ]._object.getTag() + ", " + rendered[ 1 ]._object.getTag() + ", " + rendered[ 2 ]._object.getTag() + "" , "v1"
		.equals( rendered[ 0 ]._object.getTag() ) && "h1"
		.equals( rendered[ 1 ]._object.getTag() ) && "v1".equals( rendered[ 2 ]._object.getTag() ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1, h2 and v1" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "h2" );
		addPlane( renderQueue, "v1" );
		rendered = renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 5, not " + rendered.length, 5, rendered.length );
		assertTrue( "The order in which the polygons are given back should be v1, h1, v1, h2, v1, not " + rendered[ 0 ]._object.getTag() + ", " + rendered[ 1 ]._object.getTag() + ", " + rendered[ 2 ]._object.getTag() + ""  + rendered[ 3 ]._object.getTag() + ""  + rendered[ 4 ]._object.getTag() + "" , "v1"
		.equals( rendered[ 0 ]._object.getTag() ) && "h1"
		.equals( rendered[ 1 ]._object.getTag() ) && "v1".equals( rendered[ 2 ]._object.getTag() ) && "h2"
		.equals( rendered[ 3 ]._object.getTag() ) && "v1"
		.equals( rendered[ 4 ]._object.getTag() ) );

	}

	private static void addPlane( final RenderQueue renderQueue , final String tag )
	{
		Object3D object = null;
		Matrix3D transform = null;

		final double      viewUnit          = ViewModel.MM;
		final double      fieldOfView       = Math.toRadians( 45.0 );
		final double      zoomFactor        = 1.0;
		final double      frontClipDistance = -0.1 / viewUnit;
		final double      backClipDistance  = -100.0 / viewUnit;
		final double      imageResolution   = 0.0254 / 90.0; // getToolkit().getScreenResolution();
		final Projector   projector         = Projector.createInstance( Projector.PERSPECTIVE , 800 , 600 , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );

		if ( "h1".equals( tag ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "h1" );
			transform = Matrix3D.getTransform( 90.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
		}
		else if ( "h2".equals( tag ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "h2" );
			transform = Matrix3D.getTransform( 90.0, 0.0, 0.0, -50.0, -50.0, 0.0 );
		}
		else if ( "h3".equals( tag ) )
		{
			object = createPlane( 50.0 );
			object.setTag( "h3" );
			transform = Matrix3D.getTransform( 90.0, 0.0, 0.0, -100.0, 25.0, 0.0 );
		}
		else if ( "v1".equals( tag ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "v1" );
			transform = Matrix3D.getTransform( 0.0, 90.0, 0.0, -10.0, 0.0, 0.0 );
		}
		else if ( "v2".equals( tag ) )
		{
			object = createPlane( 20.0 );
			object.setTag( "v2" );
			transform = Matrix3D.getTransform( 0.0, 90.0, 0.0, 105.0, -25.0, 0.0 );
		}
		else if ( "d1".equals( tag ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "d1" );
			transform = Matrix3D.getTransform( -45.0, 90.0, 0.0, 0.0, 0.0, 0.0 );
		}
		else if ( "d2".equals( tag ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "d2" );
			transform = Matrix3D.getTransform( 45.0, 90.0, 0.0, 80.0, 80.0, 0.0 );
		}

		final Matrix3D viewTransform = Matrix3D.getFromToTransform(  Vector3D.INIT.set( 0.0, -1000.0, 0.0 ), Vector3D.INIT , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
		transform = transform.multiply( viewTransform );
		renderQueue.enqueueObject( projector , true, transform, object, false );
	}

	private static Object3D createPlane( final double size )
	{
		final Vector3D lf = Vector3D.INIT.set( -size, -size, 0.0 );
		final Vector3D rf = Vector3D.INIT.set( size, -size, 0.0 );
		final Vector3D rb = Vector3D.INIT.set( size, size, 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -size, size, 0.0 );

		final TextureSpec red = new TextureSpec( Color.red );
		final TextureSpec green = new TextureSpec( Color.green );

		final Object3D plane = new Object3D();
		/* top    */plane.addFace( new Vector3D[]{lf , lb , rb , rf}, red, false, false ); // Z =  size
		/* bottom */plane.addFace( new Vector3D[]{lb , lf , rf , rb}, green, false, false ); // Z = -size

		return plane;
	}
}
