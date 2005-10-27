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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import junit.framework.TestCase;

import ab.j3d.model.Object3D;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.TextureSpec;
import ab.j3d.view.java2d.Painter;

/**
 * This class tests the {@link RenderQueue} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 * @see RenderQueue
 */
public class TestRenderQueue
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestRenderQueue.class.getName();

	/**
	 * Fixture instance.
	 */
	private RenderQueue _renderQueue;

	/**
	 * Setup text fixture.
	 *
	 * @throws Exception if there was a problem setting up the fixture.
	 */
	public void setUp()
	throws Exception
	{
		super.setUp();

		_renderQueue = new RenderQueue();
	}

	/**
	 * Tear down test fixture.
	 *
	 * @throws Exception if there was a problem tearing down the fixture.
	 */
	public void tearDown()
	throws Exception
	{
		_renderQueue = null;

		super.tearDown();
	}

	/**
	 * Test the {@link RenderQueue#enqueueObject} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testEnqueueObject()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testEnqueueObject()" );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and h2" );
		_renderQueue.clearQueue();
		addPlane( "h2" );
		addPlane( "h1" );
		RenderedPolygon[] rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not "+rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be h1, not " + rendered[ 0 ]._object.getTag(), rendered[ 0 ]._object.getTag().equals( "h1" ) );


		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and h2" );
		_renderQueue.clearQueue();
		addPlane( "h1" );
		addPlane( "h2" );
		rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not " + rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be h1, not " + rendered[ 0 ]._object.getTag(), rendered[ 0 ]._object.getTag().equals( "h1" ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes d1 and d2" );
		_renderQueue.clearQueue();
		addPlane( "d1" );
		addPlane( "d2" );
		rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not "+rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be d2, not " + rendered[ 0 ]._object.getTag(), rendered[ 0 ]._object.getTag().equals( "d2" ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes d1 and d2" );
		_renderQueue.clearQueue();
		addPlane( "d2" );
		addPlane( "d1" );
		rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 2, not " + rendered.length, 2, rendered.length );
		assertTrue( "The first item in the queue should be d2, not " + rendered[ 0 ]._object.getTag(), rendered[ 0 ]._object.getTag().equals( "d2" ) );

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1 and v1" );
		_renderQueue.clearQueue();
		addPlane( "h1" );
		addPlane( "v1" );
		rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 3, not " + rendered.length, 3, rendered.length );
		assertTrue( "The order in which the polygons are given back should be v1, h1, v1, not " + rendered[ 0 ]._object.getTag() + ", " + rendered[ 1 ]._object.getTag() + ", " + rendered[ 2 ]._object.getTag() + "" , rendered[ 0 ]._object.getTag().equals( "v1" ) && rendered[ 1 ]._object.getTag().equals( "h1" ) && rendered[ 2 ]._object.getTag().equals( "v1" ));

		System.out.println( "\n--------------------------" );
		System.out.println( "Testing planes h1, h2 and v1" );
		_renderQueue.clearQueue();
		addPlane( "h1" );
		addPlane( "h2" );
		addPlane( "v1" );
		rendered = _renderQueue.getQueuedPolygons();
		assertEquals( "The number of rendered polygons should be 5, not " + rendered.length, 5, rendered.length );
		assertTrue( "The order in which the polygons are given back should be v1, h1, v1, h2, v1, not " + rendered[ 0 ]._object.getTag() + ", " + rendered[ 1 ]._object.getTag() + ", " + rendered[ 2 ]._object.getTag() + ""  + rendered[ 3 ]._object.getTag() + ""  + rendered[ 4 ]._object.getTag() + "" , rendered[ 0 ]._object.getTag().equals( "v1" ) && rendered[ 1 ]._object.getTag().equals( "h1" ) && rendered[ 2 ]._object.getTag().equals( "v1" ) && rendered[ 3 ]._object.getTag().equals( "h2" ) && rendered[ 4 ]._object.getTag().equals( "v1" ));

	}

	private void addPlane(String tag)
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

		if ( tag.equals( "h1" ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "h1" );
			transform = Matrix3D.getTransform( 90, 0, 0, 0, 0, 0 );
		}
		else if ( tag.equals( "h2" ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "h2" );
			transform = Matrix3D.getTransform( 90, 0, 0, -50, -50, 0 );
		}
		else if ( tag.equals( "h3" ) )
		{
			object = createPlane( 50 );
			object.setTag( "h3" );
			transform = Matrix3D.getTransform( 90, 0, 0, -100, 25, 0 );
		}
		else if ( tag.equals( "v1" ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "v1" );
			transform = Matrix3D.getTransform( 0, 90, 0, -10, 0, 0 );
		}
		else if ( tag.equals( "v2" ) )
		{
			object = createPlane( 20 );
			object.setTag( "v2" );
			transform = Matrix3D.getTransform( 0, 90, 0, 105, -25, 0 );
		}
		else if ( tag.equals( "d1" ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "d1" );
			transform = Matrix3D.getTransform( -45, 90, 0, 0, 0, 0 );
		}
		else if ( tag.equals( "d2" ) )
		{
			object = createPlane( 100.0 );
			object.setTag( "d2" );
			transform = Matrix3D.getTransform( 45, 90, 0, 80, 80, 0 );
		}

		Matrix3D viewTransform = Matrix3D.getFromToTransform(  Vector3D.INIT.set( 0, -1000, 0), Vector3D.INIT , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
		transform = transform.multiply( viewTransform );
		_renderQueue.enqueueObject( projector , true, transform, object, false );
	}

	public static Object3D createPlane( final double size )
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
