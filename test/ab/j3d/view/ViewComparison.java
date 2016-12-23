/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
package ab.j3d.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.*;
import ab.j3d.awt.view.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.pov.*;

/**
 * Shows a 3D scene using the various render engines in several lighting modes,
 * for comparison purposes.
 *
 * @author G. Meinders
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
	 * Texture library.
	 */
	private final ClassLoaderTextureLibrary _textureLibrary = new ClassLoaderTextureLibrary();

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
		frame.setLayout( new GridLayout( 1, 0 ) );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				System.exit( 0 );
			}
		} );

		for ( final Template template : templates )
		{
			final JPanel layoutPanel = new JPanel();
			layoutPanel.setLayout( new GridLayout( 0, 1 ) );

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

				final SwingWorker<Object, BufferedImage> povImageRenderer = new SwingWorker<Object, BufferedImage>()
				{
					@Override
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
								final double aspectRatio = (double)size.width / (double)size.height;

								final AbToPovConverter converter = new AbToPovConverter();
								final PovScene povScene = converter.convert( scene );
								povScene.add( new PovCamera( cameraName, view2Scene, cameraAngle, aspectRatio ) );
								povScene.setBackground( new PovVector( 0.5f, 0.5f, 0.5f ) );

								publish( PovRenderer.render( povScene, null, null, size.width, size.height, null, new PrintWriter( System.err ), true, _textureLibrary ) );
							}
							Thread.sleep( 1000L );
						}
						return null;
					}

					@Override
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
					@Override
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

	private Collection<RenderEngine> createRenderEngines( final Scene scene )
	{
		final Collection<RenderEngine> models = new ArrayList<RenderEngine>();
		models.add( RenderEngineFactory.createJOGLEngine( _textureLibrary, new JOGLConfiguration() ) );
//		models.add( new Java3dEngine( scene, Color.GRAY ) );
		return models;
	}

	private abstract static class Template
	{
		private Vector3D _cameraLocation;

		private Vector3D _cameraTarget;

		protected Template()
		{
			_cameraLocation = new Vector3D( 500.0, -500.0, 500.0 );
			_cameraTarget = new Vector3D( 0.0, 150.0, 40.0 );
//			_cameraLocation = new Vector3D( 4000.0, 1500.0, 4000.0 );
//			_cameraTarget   = new Vector3D( 0.0, 1500.0, 40.0 );
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
			target.setCameraControl( new FromToCameraControl( target, _cameraLocation, _cameraTarget ) );
		}

		public abstract void createModel( final Scene target );
	}

	private static class DefaultTemplate
	extends Template
	{
		@Override
		public void createModel( final Scene target )
		{
			final BasicAppearance solid = BasicAppearance.createForColor( new Color4f( 0xffff8000 ) ); // "solid";
			final BasicAppearance shiny = BasicAppearance.createForColor( new Color4f( 0xffff8000 ) ); // "shiny";
			final BasicAppearance shinier = BasicAppearance.createForColor( new Color4f( 0xffff8000 ) ); // "shinier";
			final BasicAppearance textured = BasicAppearance.createForColor( new Color4f( 0xffffffff ) ); // "textured";
			final BasicAppearance textured2 = BasicAppearance.createForColor( new Color4f( 0xff0080ff ) ); // "textured2";
			final BasicAppearance textured3 = BasicAppearance.createForColor( new Color4f( 0xffff0000 ) ); // "textured3";

			if ( SPECULAR_HIGHLIGHTS )
			{
				solid.setSpecularColor( Color4.WHITE );
				shiny.setSpecularColor( Color4.WHITE );
				shinier.setSpecularColor( Color4.WHITE );
				textured.setSpecularColor( Color4.WHITE );
				textured2.setSpecularColor( Color4.WHITE );
				textured3.setSpecularColor( Color4.WHITE );
			}

			shiny.setShininess( 64 );
			shinier.setShininess( 128 );
			final TextureMap colorMap = new BasicTextureMap( "decors/CB.jpg" );
			textured.setColorMap( colorMap );
			textured2.setColorMap( colorMap );
			textured3.setColorMap( colorMap );
			textured3.setAmbientColor( new Color4f( 0.5f, 1.0f, 0.5f ) );

			/*
			 * Test basic specular highlights, smoothing and texturing.
			 */
			target.addContentNode( "sphere-1", Matrix3D.getTranslation( -100.0, -100.0, 40.0 ), new Sphere3D( 80.0, 16, 16, solid ) );
			target.addContentNode( "sphere-2", Matrix3D.getTranslation( 0.0, -100.0, 40.0 ), new Sphere3D( 80.0, 16, 16, solid ) );
			target.addContentNode( "sphere-3", Matrix3D.getTranslation( 100.0, -100.0, 40.0 ), new Sphere3D( 80.0, 16, 16, textured ) );
			target.addContentNode( "sphere-4", Matrix3D.getTranslation( -100.0, 0.0, 40.0 ), new Sphere3D( 80.0, 16, 16, solid ) );
			target.addContentNode( "sphere-5", Matrix3D.getTranslation( 0.0, 0.0, 40.0 ), new Sphere3D( 80.0, 16, 16, shiny ) );
			target.addContentNode( "sphere-6", Matrix3D.getTranslation( 100.0, 0.0, 40.0 ), new Sphere3D( 80.0, 16, 16, shinier ) );
			target.addContentNode( "box-1", Matrix3D.getTranslation( -140.0, 60.0, 0.0 ), new Box3D( 80.0, 80.0, 80.0, new BoxUVMap( Scene.MM ), solid ) );

			/*
			 * Test advanced texturing. (i.e. with non-white diffuse color)
			 */
			target.addContentNode( "sphere-7", Matrix3D.getTranslation( 0.0, 100.0, 40.0 ), new Sphere3D( 80.0, 16, 16, textured2 ) );
			target.addContentNode( "sphere-8", Matrix3D.getTranslation( 100.0, 100.0, 40.0 ), new Sphere3D( 80.0, 16, 16, textured3 ) );

			/*
			 * Test combinations of diffuse and ambient colors.
			 */
			double z = 0.0;
			int j = 0;
			for ( final Color4 diffuseColor : Arrays.asList( new Color4f( 0xff8000 ), Color4.GREEN, new Color4f( 0x4080ff ) ) )
			{
				int i = 0;
				for ( final Color4 ambientColor : Arrays.asList( Color4.RED, Color4.ORANGE, Color4.YELLOW, new Color4f( 0x80ff00 ), Color4.GREEN, Color4.CYAN, new Color4f( 0x0080ff ), Color4.BLUE, Color4.MAGENTA ) )
				{
					final BasicAppearance appearance = new BasicAppearance(); // code = "ambient" + j;
					appearance.setAmbientColor( ambientColor );
					appearance.setDiffuseColor( diffuseColor );
					appearance.setSpecularColor( Color4.WHITE );
					appearance.setShininess( 16 );

					final double x = (double)( i % 3 ) * 100.0 - 100.0;
					final double y = (double)( i / 3 ) * 100.0 + 200.0;
					target.addContentNode( "ambient-sphere-" + j, Matrix3D.getTranslation( x, y, z ), new Sphere3D( 40.0, 16, 16, appearance ) );

					i++;
					j++;
				}
				z += 100.0;
			}

			/*
			 * Test light fall-off
			 */
			for ( int i = 0; i < 50; i++ )
			{
				target.addContentNode( "distant-sphere-a-" + i, Matrix3D.getTranslation( -100.0, 500.0 + (double)i * 100.0, 0.0 ), new Sphere3D( 40.0, 16, 16, solid ) );
				target.addContentNode( "distant-sphere-b-" + i, Matrix3D.getTranslation( 0.0, 500.0 + (double)i * 100.0, 0.0 ), new Sphere3D( 40.0, 16, 16, shiny ) );
				target.addContentNode( "distant-sphere-c-" + i, Matrix3D.getTranslation( 100.0, 500.0 + (double)i * 100.0, 0.0 ), new Sphere3D( 40.0, 16, 16, textured ) );
			}

			createLights( target );
		}

		protected void createLights( final Scene target )
		{
			target.setAmbient( 0.5f, 0.5f, 0.5f );
			createDiffuseLights( target );
		}

		protected void createDiffuseLights( final Scene target )
		{
			final Light3D pointLight = new Light3D();
			pointLight.setIntensity( 0.5f );
			pointLight.setFallOff( FALL_OFF );
			target.addContentNode( "light-1", Matrix3D.getTranslation( 1000.0, -1000.0, 1000.0 ), pointLight );

			final SpotLight3D spotLight = new SpotLight3D( Vector3D.normalize( 1.0, 1.0, -1.0 ), 10.0f );
			spotLight.setIntensity( 2.0f );
			spotLight.setFallOff( FALL_OFF );
			spotLight.setConcentration( 32.0f );
			target.addContentNode( "light-2", Matrix3D.getTransform( 0.0, 0.0, 10.0, -1000.0, -1000.0, 1000.0 ), spotLight );
		}
	}

	private static class DiffuseOnlyTemplate
	extends DefaultTemplate
	{
		@Override
		protected void createLights( final Scene target )
		{
			createDiffuseLights( target );
		}
	}

	private static class AmbientOnlyTemplate
	extends DefaultTemplate
	{
		@Override
		protected void createLights( final Scene target )
		{
			target.setAmbient( 0.5f, 0.5f, 0.5f );
		}
	}
}
