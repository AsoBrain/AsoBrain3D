/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2022 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.awt.image.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import com.jogamp.opengl.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link JOGLOffscreenView}.
 *
 * @author Gerrit Meinders
 */
public class TestJOGLOffscreenView
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestJOGLOffscreenView.class.getName();

	/**
	 * Tests if any offscreen rendering takes place.
	 */
	@Test
	public void testRenderer()
	{
		final String where = CLASS_NAME + ".testRenderer()";
		System.out.println( where );

		final JOGLEngine joglEngine = new JOGLEngine( new ClassLoaderTextureLibrary( getClass().getClassLoader() ), JOGLConfiguration.createLusciousInstance() );

		final Scene scene = new Scene( Scene.METER );
		scene.setAmbient( 1, 1, 1 );
		scene.addContentNode( "light", Matrix3D.IDENTITY, new Light3D( 0.0f, 0.0f ) );
		final Appearance green = BasicAppearances.GREEN;
		scene.addContentNode( "box", Matrix3D.getTranslation( -0.5, -0.5, -0.5 ), new Box3D( 1.0, 1.0, 1.0, null, green ) );

		try
		{
			System.out.println( " - When called from non-OpenGL thread (" + Thread.currentThread() + ")" );
			new JOGLOffscreenView( joglEngine, scene );
			fail( "Must throw an exception if not called from OpenGL thread." );
		}
		catch ( final IllegalStateException ignored )
		{
			assertFalse( "Test must not be run on OpenGL thread.", Threading.isOpenGLThread() );
		}

		Threading.invokeOnOpenGLThread( true, new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println( " - When called from OpenGL thread (" + Thread.currentThread() + ")" );
				try
				{
					final JOGLOffscreenView view = new JOGLOffscreenView( joglEngine, scene );
					try
					{
						view.setCameraControl( new FromToCameraControl( view, 1.0 ) );
						final BufferedImage image = view.renderImage( 1, 1 );
						assertEquals( "Unexpected rendering: should be a green pixel.", Integer.toHexString( green.getDiffuseColor().getARGB() ), Integer.toHexString( image.getRGB( 0, 0 ) ) );
					}
					finally
					{
						view.dispose();
					}
				}
				finally
				{
					joglEngine.dispose();
				}
			}
		} );
	}
}
