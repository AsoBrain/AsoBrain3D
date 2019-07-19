/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
import java.io.*;
import javax.imageio.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import com.jogamp.opengl.*;

/**
 * App for testing off-screen rendering.
 *
 * @author Gerrit Meinders
 */
public class TestOffScreenRendering
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final JOGLEngine engine = new JOGLEngine( new ClassLoaderTextureLibrary( TestOffScreenRendering.class.getClassLoader() ), JOGLConfiguration.createLusciousInstance() );
		final Scene scene = new Scene( Scene.MM );
		Threading.invokeOnOpenGLThread( true, new Runnable()
		{
			@Override
			public void run()
			{
				final JOGLOffscreenView view = new JOGLOffscreenView( engine, scene );

				for ( int i = 0; i < 10; i++ )
				{
					final long startRender = System.nanoTime();

					scene.removeAllContentNodes();
					scene.addContentNode( "cube", Matrix3D.IDENTITY, new Box3D( 1.0, 1.0, 1.0, null, BasicAppearances.RED ) );

					view.setFrontClipDistance( 0.01 );
					view.setBackClipDistance( 100.0 );
					view.setBackground( Background.createSolid( Color4f.GRAY ) );
					view.setCameraControl( new FromToCameraControl( view, new Vector3D( 2.0, -5.0, 3.0 ), Vector3D.ZERO ) );

					final BufferedImage bufferedImage = view.renderImage( 100 * ( i + 1 ), 100 * ( i + 1 ) );

					try
					{
						final FileOutputStream out = new FileOutputStream( "/tmp/image-" + i + ".png" );
						try
						{
							ImageIO.write( bufferedImage, "png", out );
						}
						finally
						{
							out.close();
						}
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}

					final long endRender = System.nanoTime();
					System.out.println( "Render: " + (double)( ( endRender - startRender ) / 1000000L ) / 1000.0 + " s" );
				}
			}
		} );
	}
}
