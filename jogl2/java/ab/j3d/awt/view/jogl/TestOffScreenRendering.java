/*
 * (C) Copyright Numdata BV 2014-2014 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.awt.view.jogl;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * FIXME Need comment.
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
		final JOGLEngine engine = new JOGLEngine( JOGLConfiguration.createLusciousInstance() );
		final Scene scene = new Scene( Scene.MM );
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
				final FileOutputStream out = new FileOutputStream( "/tmp/image-" + i + ".jpg" );
				try
				{
					ImageIO.write( bufferedImage, "jpg", out );
				}
				finally
				{
					out.close();
				}
			}
			catch ( IOException e )
			{
				e.printStackTrace(); // FIXME: Generated try-catch block.
			}

			final long endRender = System.nanoTime();
			System.out.println( "Render: " + (double)( ( endRender - startRender ) / 1000000L ) / 1000.0 + " s" );
		}
	}
}
