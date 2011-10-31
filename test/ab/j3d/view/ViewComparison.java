/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.awt.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.pov.*;
import ab.j3d.view.jogl.*;

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

			if ( ( view != null ) && INCLUDE_POV_VIEW )
			{
				final View3D finalView = view;

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
								final String cameraName = finalView.getLabel();
								final Matrix3D view2Scene = finalView.getView2Scene();
								final double cameraAngle = Math.toDegrees( finalView.getFieldOfView() );
								final double  aspectRatio = (double)size.width / (double)size.height;

								final AbToPovConverter converter = new AbToPovConverter( MapTools.imageMapDirectory );
								final PovScene povScene = converter.convert( scene );
								povScene.add( new PovCamera( cameraName, view2Scene, cameraAngle, aspectRatio ) );
								povScene.setBackground( new PovVector( 0.5f, 0.5f, 0.5f ) );

								publish( PovRenderer.render( povScene, null, size.width, size.height, null, new PrintWriter( System.err ), true ) );
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
		models.add( new JOGLEngine() );
//		models.add( new Java3dEngine( scene , Color.GRAY ) );
		return models;
	}

	private abstract static class Template
	{
		private Vector3D _cameraLocation;

		private Vector3D _cameraTarget;

		protected Template()
		{
			_cameraLocation = new Vector3D( 500.0 , -500.0 , 500.0 );
			_cameraTarget   = new Vector3D( 0.0 , 150.0 , 40.0 );
//			_cameraLocation = new Vector3D( 4000.0 , 1500.0 , 4000.0 );
//			_cameraTarget   = new Vector3D( 0.0 , 1500.0 , 40.0 );
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
			target.addContentNode( "sphere-1" , Matrix3D.getTranslation( -100.0 , -100.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , solid    ) );
			target.addContentNode( "sphere-2" , Matrix3D.getTranslation(    0.0 , -100.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , solid    ) );
			target.addContentNode( "sphere-3" , Matrix3D.getTranslation(  100.0 , -100.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , textured ) );
			target.addContentNode( "sphere-4" , Matrix3D.getTranslation( -100.0 ,    0.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , solid    ) );
			target.addContentNode( "sphere-5" , Matrix3D.getTranslation(    0.0 ,    0.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , shiny    ) );
			target.addContentNode( "sphere-6" , Matrix3D.getTranslation(  100.0 ,    0.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , shinier  ) );
			target.addContentNode( "box-1"    , Matrix3D.getTranslation( -140.0 ,   60.0 ,  0.0 ) , new Box3D   ( 80.0 , 80.0 , 80.0 , new BoxUVMap( Scene.MM ) , solid ) );

			/*
			 * Test advanced texturing. (i.e. with non-white diffuse color)
			 */
			target.addContentNode( "sphere-7" , Matrix3D.getTranslation(    0.0 ,  100.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , textured2 ) );
			target.addContentNode( "sphere-8" , Matrix3D.getTranslation(  100.0 ,  100.0 , 40.0 ) , new Sphere3D( 80.0 , 16 , 16 , textured3 ) );

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
					target.addContentNode( "ambient-sphere-" + j , Matrix3D.getTranslation( x , y , z ) , new Sphere3D( 40.0 , 16 , 16 , material ) );

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
				target.addContentNode( "distant-sphere-a-" + i , Matrix3D.getTranslation( -100.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( 40.0 , 16 , 16 , solid    ) );
				target.addContentNode( "distant-sphere-b-" + i , Matrix3D.getTranslation(    0.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( 40.0 , 16 , 16 , shiny    ) );
				target.addContentNode( "distant-sphere-c-" + i , Matrix3D.getTranslation(  100.0 , 500.0 + (double)i * 100.0 , 0.0 ) , new Sphere3D( 40.0 , 16 , 16 , textured ) );
			}

			createLights( target );
		}

		protected void createLights( final Scene target )
		{
			target.setAmbient( 0.5f , 0.5f , 0.5f );
			createDiffuseLights( target );
		}

		protected void createDiffuseLights( final Scene target )
		{
			final Light3D pointLight = new Light3D();
			pointLight.setIntensity( 0.5f );
			pointLight.setFallOff( FALL_OFF );
			target.addContentNode( "light-1" , Matrix3D.getTranslation(  1000.0 ,  -1000.0 ,  1000.0 ) , pointLight );

			final SpotLight3D spotLight = new SpotLight3D( Vector3D.normalize( 1.0 , 1.0 , -1.0 ) , 10.0f );
			spotLight.setIntensity( 2.0f );
			spotLight.setFallOff( FALL_OFF );
			spotLight.setConcentration( 32.0f );
			target.addContentNode( "light-2" , Matrix3D.getTransform( 0.0 , 0.0 , 10.0 , -1000.0 ,  -1000.0 ,  1000.0 ) , spotLight );
		}
	}

	private static class DiffuseOnlyTemplate
		extends DefaultTemplate
	{
		protected void createLights( final Scene target )
		{
			createDiffuseLights( target );
		}
	}

	private static class AmbientOnlyTemplate
		extends DefaultTemplate
	{
		protected void createLights( final Scene target )
		{
			target.setAmbient( 0.5f , 0.5f , 0.5f );
		}
	}
}
