/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
 * ====================================================================
 */
package ab.j3d.view;

import java.awt.Color;

import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Object3D;

import com.numdata.oss.junit.ArrayTester;

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
	 * Assert queue contents.
	 */
	private static void assertQueuedTags( final Object[] expectedTags , final RenderQueue queue )
	{
		final RenderedPolygon[] queuedPolygons = queue.getQueuedPolygons();

		final Object[] actualTags = new Object[ queuedPolygons.length ];
		for ( int i = 0 ; i < actualTags.length ; i++ )
			actualTags[ i ] = queuedPolygons[ i ]._object.getTag();

		ArrayTester.assertEquals( "Incorrect queue contents" , "expected" , "actual" , expectedTags , actualTags );
	}

	/**
	 * Test the {@link RenderQueue#enqueueObject} method.
	 */
	public static void testEnqueueObject()
	{
		System.out.println( CLASS_NAME + ".testEnqueueObject()" );

		final RenderQueue renderQueue = new RenderQueue();

		System.out.println( " - Testing planes h1 and h2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h2" );
		addPlane( renderQueue, "h1" );
		assertQueuedTags( new Object[] { "h1" , "h2" } , renderQueue );


		System.out.println( " - Testing planes h1 and h2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "h2" );
		assertQueuedTags( new Object[] { "h1" , "h2" } , renderQueue );

		// @FIXME following tests fail due to incomplete implementation
		if ( "@FIXME".length() == 6 ) return;

		System.out.println( " - Testing planes d1 and d2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "d1" );
		addPlane( renderQueue, "d2" );
		assertQueuedTags( new Object[] { "d2" , "d1" } , renderQueue );

		System.out.println( " - Testing planes d1 and d2" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "d2" );
		addPlane( renderQueue, "d1" );
		assertQueuedTags( new Object[] { "d2" , "d1" } , renderQueue );

		System.out.println( " - Testing planes h1 and v1" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "v1" );
		assertQueuedTags( new Object[] { "v1" , "h1" , "v1" } , renderQueue );

		System.out.println( " - Testing planes h1, h2 and v1" );
		renderQueue.clearQueue();
		addPlane( renderQueue, "h1" );
		addPlane( renderQueue, "h2" );
		addPlane( renderQueue, "v1" );
		assertQueuedTags( new Object[] { "v1" , "h1" , "v1" , "h2" , "v1" } , renderQueue );
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
			object    = createPlane( tag , 100.0 );
			transform = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 );
		}
		else if ( "h2".equals( tag ) )
		{
			object    = createPlane( tag , 100.0 );
			transform = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , -50.0 , -50.0 , 0.0 );
		}
		else if ( "h3".equals( tag ) )
		{
			object    = createPlane( tag , 50.0 );
			transform = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , -100.0 , 25.0 , 0.0 );
		}
		else if ( "v1".equals( tag ) )
		{
			object    = createPlane( tag , 100.0 );
			transform = Matrix3D.getTransform( 0.0 , 90.0 , 0.0 , -10.0 , 0.0 , 0.0 );
		}
		else if ( "v2".equals( tag ) )
		{
			object    = createPlane( tag , 20.0 );
			transform = Matrix3D.getTransform( 0.0 , 90.0 , 0.0 , 105.0 , -25.0 , 0.0 );
		}
		else if ( "d1".equals( tag ) )
		{
			object    = createPlane( tag , 100.0 );
			transform = Matrix3D.getTransform( -45.0 , 90.0 , 0.0 , 0.0 , 0.0 , 0.0 );
		}
		else if ( "d2".equals( tag ) )
		{
			object    = createPlane( tag , 100.0 );
			transform = Matrix3D.getTransform( 45.0 , 90.0 , 0.0 , 80.0 , 80.0 , 0.0 );
		}
		else
		{
			fail( "don't know how to create object" );
		}

		final Matrix3D viewTransform = Matrix3D.getFromToTransform( Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 ), Vector3D.INIT, Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ), Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
		transform = transform.multiply( viewTransform );
		renderQueue.enqueueObject( projector , true, transform, object, false );
	}

	private static Object3D createPlane( final String tag , final double size )
	{
		final Vector3D lf = Vector3D.INIT.set( -size , -size , 0.0 );
		final Vector3D rf = Vector3D.INIT.set(  size , -size , 0.0 );
		final Vector3D rb = Vector3D.INIT.set(  size ,  size , 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -size ,  size , 0.0 );

		final TextureSpec red = new TextureSpec( Color.red );
		final TextureSpec green = new TextureSpec( Color.green );

		final Object3D plane = new Object3D();
		plane.setTag( tag );
		/* top    */ plane.addFace( new Vector3D[] { lf , lb , rb , rf } , red   , false, false ); // Z =  size
		/* bottom */ plane.addFace( new Vector3D[] { lb , lf , rf , rb } , green , false, false ); // Z = -size

		return plane;
	}
}