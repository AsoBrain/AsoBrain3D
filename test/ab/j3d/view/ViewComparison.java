/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2009
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.model.Box3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Scene;
import ab.j3d.model.Sphere3D;
import ab.j3d.pov.AbToPovConverter;
import ab.j3d.pov.PovScene;
import ab.j3d.pov.PovVector;
import ab.j3d.view.java3d.Java3dEngine;
import ab.j3d.view.jogl.JOGLEngine;

/**
 * Shows a 3D scene using the various render engines in several lighting modes,
 * for comparison purposes.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ViewComparison
	implements Runnable
{
	/**
	 * Whether to include a POV view.
	 */
	private static final boolean INCLUDE_POV_VIEW = true;

	/**
	 * Fall-off distance for lights in the scene.
	 */
	private static final double FALL_OFF = 1700.0;

	/**
	 * Whether to render specular highlights.
	 */
	private static final boolean SPECULAR_HIGHLIGHTS = true;

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		SwingUtilities.invokeLater( new ViewComparison() );
	}

	public void run()
	{
		final ArrayList<Template> templates = new ArrayList<Template>();
		templates.add( new DefaultTemplate() );
		templates.add( new DiffuseOnlyTemplate() );
		templates.add( new AmbientOnlyTemplate() );

		final JFrame frame = new JFrame( "AB3D View Comparison Tool" );
		frame.setLayout( new GridLayout( 1 , 0 ) );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.addWindowListener( new WindowAdapter()
		{
			public void windowClosed( final WindowEvent e )
			{
				System.exit( 0 );
			}
		} );

		for ( final Template template : templates )
		{
			final JPanel layoutPanel = new JPanel();
			layoutPanel.setLayout( new GridLayout( 0 , 1 ) );

			final Scene scene = new Scene( Scene.MM );
			template.createModel( scene );

			final Collection<RenderEngine> renderEngines = createRenderEngines( scene );
			View3D view = null;
			for ( final RenderEngine model : renderEngines )
			{
				view = model.createView( scene );
				template.configureView( view );

				final Component component = view.getComponent();
				layoutPanel.add( component );
			}

			if ( INCLUDE_POV_VIEW )
			{
				final View3D povView = view;

				final JLabel povComponent = new JLabel();
				layoutPanel.add( povComponent );

				final SwingWorker<Object,BufferedImage> povImageRenderer = new SwingWorker<Object,BufferedImage>()
				{
					protected Object doInBackground()
						throws Exception
					{
						final Thread thread = Thread.currentThread();
						thread.setPriority( Thread.MIN_PRIORITY );

						while ( !isCancelled() )
						{
							final Dimension size = povComponent.getSize();
							if ( ( size.width > 0 ) && ( size.height > 0 ) )
							{
								final Camera3D camera          = povView.getCamera();
								final Scene    scene           = povView.getScene();
								final Matrix3D viewTransform   = povView.getViewTransform();
								final Matrix3D cameraTransform = viewTransform.inverse();
								final double   aspectRatio     = (double)size.width / (double)size.height;

								final AbToPovConverter converter = new AbToPovConverter( MapTools.imageMapDirectory );
								final PovScene povScene = converter.convert( scene.getContent() );
								povScene.add( AbToPovConverter.convertCamera3D( cameraTransform , camera , aspectRatio ) );

								povScene.setBackground( new PovVector( Color.GRAY ) );

								publish( povScene.render( null , size.width , size.height , null , new PrintWriter( System.err ) , true ) );
							}
							Thread.sleep( 1000L );
						}
						return null;
					}

					protected void process( final List<BufferedImage> chunks )
					{
						try
						{
							final BufferedImage image = chunks.get( chunks.size() - 1 );
							povComponent.setIcon( new ImageIcon( image ) );
						}
						catch ( Exception e )
						{
							povComponent.setText( "<html><p>Failed to render scene using POV.</p><p>" + e.getMessage() + "</p>" );
						}
					}
				};
				povImageRenderer.execute();
				frame.addWindowListener( new WindowAdapter()
				{
					public void windowClosed( final WindowEvent e )
					{
						povImageRenderer.cancel( false );
					}
				} );
			}

			frame.add( layoutPanel );
		}

		frame.pack();
		frame.setExtendedState( Frame.MAXIMIZED_BOTH );
		frame.setVisible( true );
	}

	private static Collection<RenderEngine> createRenderEngines( final Scene scene )
	{
		final Collection<RenderEngine> models = new ArrayList<RenderEngine>();
		models.add( new JOGLEngine( Color.GRAY ) );
		models.add( new Java3dEngine( scene , Color.GRAY ) );
		return models;
	}

	private abstract static class Template
	{
		private Vector3D _cameraLocation;

		private Vector3D _cameraTarget;

		protected Template()
		{
			_cameraLocation = Vector3D.INIT.set( 500.0 , -500.0 , 500.0 );
			_cameraTarget   = Vector3D.INIT.plus( 0.0 , 150.0 , 40.0 );
//			_cameraLocation = Vector3D.INIT.set( 4000.0 , 1500.0 , 4000.0 );
//			_cameraTarget   = Vector3D.INIT.plus( 0.0 , 1500.0 , 40.0 );
		}

		public void setCameraLocation( final Vector3D cameraLocation )
		{
			_cameraLocation = cameraLocation;
		}

		public void setCameraTarget( final Vector3D cameraTarget )
		{
			_cameraTarget = cameraTarget;
		}

		public void configureView( final View3D target )
		{
			target.setCameraControl( new FromToCameraControl( target , _cameraLocation , _cameraTarget ) );
		}

		public abstract void createModel( final Scene target );
	}

	private static class DefaultTemplate
		extends Template
	{
		public void createModel( final Scene target )
		{
			final Material solid     = new Material( 0xffff8000 ); solid    .code = "solid";
			final Material shiny     = new Material( 0xffff8000 ); shiny    .code = "shiny";
			final Material shinier   = new Material( 0xffff8000 ); shinier  .code = "shinier";
			final Material textured  = new Material( 0xffffffff ); textured .code = "textured";
			final Material textured2 = new Material( 0xff0080ff ); textured2.code = "textured2";
			final Material textured3 = new Material( 0xffff0000 ); textured3.code = "textured3";

			if ( !SPECULAR_HIGHLIGHTS )
			{
				solid    .specularColorRed = 0.0f; solid    .specularColorGreen = 0.0f; solid    .specularColorBlue = 0.0f;
				shiny    .specularColorRed = 0.0f; shiny    .specularColorGreen = 0.0f; shiny    .specularColorBlue = 0.0f;
				shinier  .specularColorRed = 0.0f; shinier  .specularColorGreen = 0.0f; shinier  .specularColorBlue = 0.0f;
				textured .specularColorRed = 0.0f; textured .specularColorGreen = 0.0f; textured .specularColorBlue = 0.0f;
				textured2.specularColorRed = 0.0f; textured2.specularColorGreen = 0.0f; textured2.specularColorBlue = 0.0f;
				textured3.specularColorRed = 0.0f; textured3.specularColorGreen = 0.0f; textured3.specularColorBlue = 0.0f;
			}

			shiny.shininess = 64;
			shinier.shininess = 128;
			textured.colorMap = "CB";
			textured2.colorMap = "CB";
			textured3.colorMap = "CB";
			textured3.ambientColorRed   = 0.5f;
			textured3.ambientColorGreen = 1.0f;
			textured3.ambientColorBlue  = 0.5f;

			/*
			 * Test basic specular highlights, smoothing and texturing.
			 */
			target.addContentNode( "sphere-1" , Matrix3D.INIT.plus( -100.0 , -100.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.1 , 80.2 , 16 , 16 , solid     , false ) , null , 1.0f );
			target.addContentNode( "sphere-2" , Matrix3D.INIT.plus(    0.0 , -100.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.1 , 80.2 , 16 , 16 , solid     , true  ) , null , 1.0f );
			target.addContentNode( "sphere-3" , Matrix3D.INIT.plus(  100.0 , -100.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , textured  , true  ) , null , 1.0f );
			target.addContentNode( "sphere-4" , Matrix3D.INIT.plus( -100.0 ,    0.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , solid     , true  ) , null , 1.0f );
			target.addContentNode( "sphere-5" , Matrix3D.INIT.plus(    0.0 ,    0.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , shiny     , true  ) , null , 1.0f );
			target.addContentNode( "sphere-6" , Matrix3D.INIT.plus(  100.0 ,    0.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , shinier   , true  ) , null , 1.0f );
			target.addContentNode( "box-1"    , Matrix3D.INIT.plus( -140.0 ,   60.0 ,  0.0 ) , new Box3D   ( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , Scene.MM , solid , solid ) , null , 1.0f );

			/*
			 * Test advanced texturing. (i.e. with non-white diffuse color)
			 */
			target.addContentNode( "sphere-7" , Matrix3D.INIT.plus(    0.0 ,  100.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , textured2 , true  ) , null , 1.0f );
			target.addContentNode( "sphere-8" , Matrix3D.INIT.plus(  100.0 ,  100.0 , 40.0 ) , new Sphere3D( Matrix3D.INIT , 80.0 , 80.0 , 80.0 , 16 , 16 , textured3 , true  ) , null , 1.0f );

			/*
			 * Test combinations of diffuse and ambient colors.
			 */
			double z = 0.0;
			int j = 0;
			for ( final Color diffuseColor : Arrays.asList( new Color( 0xff8000 ) , Color.GREEN , new Color( 0x4080ff ) ) )
			{
				int i = 0;
				for ( final Color ambientColor : Arrays.asList( Color.RED , Color.ORANGE , Color.YELLOW , new Color( 0x80ff00 ) , Color.GREEN , Color.CYAN , new Color( 0x0080ff ) , Color.BLUE , Color.MAGENTA ) )
				{
					final Material material = new Material( diffuseColor.getRGB() );
					material.code = "ambient" + j;
					material.ambientColorRed   = (float)ambientColor.getRed()   / 255.0f;
					material.ambientColorGreen = (float)ambientColor.getGreen() / 255.0f;
					material.ambientColorBlue  = (float)ambientColor.getBlue()  / 255.0f;

					final double x = (double)( i % 3 ) * 100.0 - 100.0;
					final double y = (double)( i / 3 ) * 100.0 + 200.0;
					target.addContentNode( "ambient-sphere-" + j , Matrix3D.INIT.plus( x , y , z ) , new Sphere3D( Matrix3D.INIT , 40.0 , 40.0 , 40.0 , 16 , 16 , material , true ) , null , 1.0f );

					i++;
					j++;
				}
				z += 100.0;
			}

			/*
			 * Test light fall-off
			 */
			for ( int i = 0 ; i < 50 ; i++ )
			{
				target.addContentNode( "distant-sphere-a-" + i , Matrix3D.INIT.plus( -100.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( Matrix3D.INIT , 40.0 , 40.0 , 40.0 , 16 , 16 , solid    , true ) , null , 1.0f );
				target.addContentNode( "distant-sphere-b-" + i , Matrix3D.INIT.plus(    0.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( Matrix3D.INIT , 40.0 , 40.0 , 40.0 , 16 , 16 , shiny    , true ) , null , 1.0f );
				target.addContentNode( "distant-sphere-c-" + i , Matrix3D.INIT.plus(  100.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( Matrix3D.INIT , 40.0 , 40.0 , 40.0 , 16 , 16 , textured , true ) , null , 1.0f );
			}

			createLights( target );
		}

		protected void createLights( final Scene target )
		{
			target.addContentNode( "ambient-1" , Matrix3D.INIT , new Light3D( 128 , -1.0 ) , null , 1.0f );
			target.addContentNode( "light-1" , Matrix3D.INIT.plus(  1000.0 ,  -1000.0 ,  1000.0 ) , new Light3D( 150 , FALL_OFF ) , null , 1.0f );
		}
	}

	private static class DiffuseOnlyTemplate
		extends DefaultTemplate
	{
		protected void createLights( final Scene target )
		{
			target.addContentNode( "light-1" , Matrix3D.INIT.plus(  1000.0 ,  -1000.0 ,  1000.0 ) , new Light3D( 150 , FALL_OFF ) , null , 1.0f );
		}
	}

	private static class AmbientOnlyTemplate
		extends DefaultTemplate
	{
		protected void createLights( final Scene target )
		{
			target.addContentNode( "ambient-1" , Matrix3D.INIT , new Light3D( 128 , -1.0 ) , null , 1.0f );
		}
	}
}
