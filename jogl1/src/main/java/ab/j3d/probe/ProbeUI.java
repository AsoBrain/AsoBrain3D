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
package ab.j3d.probe;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.awt.view.jogl.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * A user interface for testing 3D capabilities.
 *
 * @author  G. Meinders
 */
public class ProbeUI
	extends JPanel
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final Logger LOG = Logger.getLogger( ProbeUI.class.getName() );

	/**
	 * View with rendered image.
	 */
	private final JOGLView _actual;

	/**
	 * Texture loader.
	 */
	private final TextureLibrary _textureLibrary = new ClassLoaderTextureLibrary( getClass().getClassLoader(), "ab/j3d/probe/" );

	/**
	 * Construct new probe UI.
	 */
	public ProbeUI()
	{
		if ( !SwingUtilities.isEventDispatchThread() )
		{
			throw new AssertionError( "ProbeUI must be created on the EDT." );
		}

		final List<Probe> probes = Arrays.<Probe>asList(
			new TextureProbe(),
			new VertexLighting(),
			new PixelLighting(),
			new Blending()
		);

		final JPanel expectedContainer = new JPanel();
		expectedContainer.setBackground( Color.BLACK );
		expectedContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		expectedContainer.setPreferredSize( new Dimension( 300, 300 ) );
		expectedContainer.setLayout( new BorderLayout() );

		final JLabel expected = new JLabel();
		expectedContainer.add( expected, BorderLayout.CENTER );

		final JPanel probesPanel = new JPanel();
		for ( final Probe probe : probes )
		{
			final JButton button = new JButton( new AbstractAction( probe.getName() )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					try
					{
						final Image expectedImage = probe.getExpectedImage();
						if ( expectedImage == null )
						{
							expected.setIcon( null );
						}
						else
						{
							expected.setIcon( new ImageIcon( expectedImage ) );
						}

						if ( probe.isSupported() )
						{
							probe.run();
						}
						else
						{
							final int choice = JOptionPane.showConfirmDialog( ProbeUI.this, "It appears that your computer isn't capable of running this probe.\nAre you sure you want to try it anyway?", "Probe not supported", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
							if ( choice == JOptionPane.YES_OPTION )
							{
								probe.run();
							}
						}
					}
					catch ( IOException ioe )
					{
						JOptionPane.showMessageDialog( ProbeUI.this, "The image showing the expected result could not be loaded.", "Failed to load image of expected result", JOptionPane.ERROR_MESSAGE );
					}
				}
			} );
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					button.setBackground( probe.isSupported() ? Color.GREEN : Color.YELLOW );
				}
			} );
			probesPanel.add( button );
		}

		final JPanel actualContainer = new JPanel();
		actualContainer.setBackground( Color.BLACK );
		actualContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		actualContainer.setPreferredSize( new Dimension( 300, 300 ) );
		actualContainer.setLayout( new BorderLayout() );

		final RenderEngine engine = new JOGLEngine( _textureLibrary );
		final Scene scene = new Scene( Scene.MM );
		final JOGLView view = (JOGLView)engine.createView( scene );
		actualContainer.add( view.getComponent(), BorderLayout.CENTER );
		_actual = view;

		final JPanel expectedPanel = new JPanel();
		expectedPanel.setLayout( new BorderLayout( 5, 5 ) );
		expectedPanel.add( new JLabel( "Expected", SwingConstants.CENTER ), BorderLayout.NORTH );
		expectedPanel.add( expectedContainer, BorderLayout.CENTER );

		final JPanel actualPanel = new JPanel();
		actualPanel.setLayout( new BorderLayout( 5, 5 ) );
		actualPanel.add( new JLabel( "Your computer", SwingConstants.CENTER ), BorderLayout.NORTH );
		actualPanel.add( actualContainer, BorderLayout.CENTER );

		final JPanel comparisonPanel = new JPanel();
		comparisonPanel.setLayout( new GridLayout( 1, 2, 5, 5 ) );
		comparisonPanel.add( expectedPanel );
		comparisonPanel.add( actualPanel );

		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		setLayout( new BorderLayout( 5, 5 ) );
		add( probesPanel, BorderLayout.NORTH );
		add( comparisonPanel, BorderLayout.CENTER );
	}

	/**
	 * Dispose the probe, its 3D view and any resources it may hold.
	 */
	public void dispose()
	{
		_actual.dispose();
	}

	private abstract class Probe
	{
		private final String _name;

		protected Probe( final String name )
		{
			_name = name;
		}

		public String getName()
		{
			return _name;
		}

		public abstract Image getExpectedImage()
		throws IOException;

		public abstract boolean isSupported();

		public abstract void run();
	}

	private abstract class JOGLProbe
		extends Probe
	{
		protected JOGLProbe( final String name )
		{
			super( name );
		}

		@Override
		public boolean isSupported()
		{
			return true;
		}

		@Override
		public final void run()
		{
			if ( !SwingUtilities.isEventDispatchThread() )
			{
				throw new AssertionError();
			}

			final JOGLView view = _actual;

			final Scene scene = view.getScene();
			scene.removeAllContentNodes();
			createScene( scene );

			configureView( view );
			view.disposeRenderer();

			final JOGLCapabilities capabilities = view.getCapabilities();
			capabilities.logSummary( LOG, Level.INFO );
		}

		protected abstract void createScene( final Scene scene );

		protected abstract void configureView( final JOGLView view );
	}

	private class TextureProbe
		extends JOGLProbe
	{
		protected TextureProbe()
		{
			super( "Texture" );
		}

		@Override
		public boolean isSupported()
		{
			final JOGLCapabilities capabilities = _actual.getCapabilities();
			return super.isSupported() &&
			       ( capabilities.isTextureRectangleSupported() ||
			         capabilities.isNonPowerOfTwoSupported() ||
			         capabilities.isNonPowerOfTwoARBSupported() );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			final BasicAppearance texture1 = new BasicAppearance();
			texture1.setAmbientColor( Color4.WHITE );
			texture1.setDiffuseColor( 1, 1, 1, 0.98f );
			texture1.setSpecularColor( Color4.BLACK );
			texture1.setShininess( 16 );
			texture1.setColorMap( new BasicTextureMap( "texture1.png", 0.001f, 0.001f ) );

			final BasicAppearance green = new BasicAppearance();
			green.setAmbientColor( Color4.GREEN );
			green.setDiffuseColor( Color4.GREEN );
			green.setSpecularColor( Color4.WHITE );
			green.setShininess( 16 );

			final Light3D light1 = new Light3D();
			light1.setFallOff( 0.0 );
			light1.setSpecular( 0 );

			scene.addContentNode( "light", Matrix3D.getTranslation(  0.0, -4.0,  0.0 ), light1 );

			final Object3DBuilder plane1 = new Object3DBuilder();
			plane1.addQuad( new Vector3D( -0.5, 0.0, -0.5 ),
			                new Vector3D( -0.5, 0.0,  0.5 ),
			                new Vector3D(  0.5, 0.0,  0.5 ),
			                new Vector3D(  0.5, 0.0, -0.5 ), texture1, new PlanarUVMap( scene.getUnit(), Matrix3D.IDENTITY.rotateX( Math.PI / -2 ).plus( -0.5, 0.0, -0.5 ) ), false );
			scene.addContentNode( "plane1", Matrix3D.IDENTITY, plane1.getObject3D() );

			final Object3DBuilder plane2 = new Object3DBuilder();
			plane2.addQuad( new Vector3D( -0.5, 0.1, -0.5 ),
			                new Vector3D( -0.5, 0.1,  0.5 ),
			                new Vector3D(  0.5, 0.1,  0.5 ),
			                new Vector3D(  0.5, 0.1, -0.5 ), green, false );
			scene.addContentNode( "plane2", Matrix3D.IDENTITY, plane2.getObject3D() );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			view.setBackground( Background.createSolid( new Color4f( 1.0f, 1.0f, 1.0f ) ) );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new MyFromToCameraControl( view, new Vector3D( 0.0, -1.5, 0.0 ), Vector3D.ZERO ) );
		}

		@Nullable
		@Override
		public Image getExpectedImage()
		throws IOException
		{
			return _textureLibrary.loadImage( new BasicTextureMap( "expected-texture.png" ) );
		}
	}

	private abstract class RGBLightsProbe
		extends JOGLProbe
	{
		protected RGBLightsProbe( final String name )
		{
			super( name );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			final Light3D light1 = new Light3D();
			light1.setDiffuse( 1.0f, 0.0f, 0.0f );
			light1.setFallOff( 10.0 );

			final Light3D light2 = new Light3D();
			light2.setDiffuse( 0.0f, 1.0f, 0.0f );
			light2.setFallOff( 10.0 );

			final Light3D light3 = new Light3D();
			light3.setDiffuse( 1.0f, 1.0f, 1.0f );
			light3.setFallOff( 5.0 );

			scene.addContentNode( "light-1", Matrix3D.getTranslation(  4.0, -4.0,  4.0 ), light1 );
			scene.addContentNode( "light-2", Matrix3D.getTranslation( -4.0, -4.0,  4.0 ), light2 );
			scene.addContentNode( "light-3", Matrix3D.getTranslation(  0.0,  4.0,  0.0 ), light3 );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			view.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new MyFromToCameraControl( view, new Vector3D( -1.0, -3.0, 2.0 ), Vector3D.ZERO ) );
		}
	}

	private class VertexLighting
		extends RGBLightsProbe
	{
		VertexLighting()
		{
			super( "Per-vertex lighting" );
		}

		protected VertexLighting( final String name )
		{
			super( name );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			super.createScene( scene );

			final Color4 color = new Color4f( 0xffe0c060 );
			final BasicAppearance sphereAppearance = new BasicAppearance();
			sphereAppearance.setAmbientColor( color );
			sphereAppearance.setDiffuseColor( color );
			sphereAppearance.setSpecularColor( Color4.WHITE );
			sphereAppearance.setShininess( 16 );

			scene.addContentNode( "sphere"    , Matrix3D.IDENTITY, new Sphere3D( 0.5, 16, 16, sphereAppearance ) );
			scene.addContentNode( "sphere-inv", Matrix3D.getTranslation( 0.0, 0.0, 0.0 ), new Sphere3D( 2.0, 16, 16, sphereAppearance, true ) );

			final Object3DBuilder twoSide1 = new Object3DBuilder();
			twoSide1.addQuad( new Vector3D( -1.0, 0.0, -1.0 ),
			                  new Vector3D( -1.0, 0.0,  1.0 ),
			                  Vector3D.POSITIVE_Z_AXIS,
			                  Vector3D.NEGATIVE_Z_AXIS, sphereAppearance, true );
			scene.addContentNode( "two-side1", Matrix3D.getTranslation( 0.0, 0.0, 0.0 ), twoSide1.getObject3D() );

			final Object3DBuilder twoSide2 = new Object3DBuilder();
			twoSide2.addQuad( Vector3D.NEGATIVE_Z_AXIS,
			                  new Vector3D( 1.0, 0.0, -1.0 ),
			                  new Vector3D( 1.0, 0.0, 1.0 ),
			                  Vector3D.POSITIVE_Z_AXIS, sphereAppearance, true );
			scene.addContentNode( "two-side2", Matrix3D.getTranslation( 0.0, 0.0, 0.0 ), twoSide2.getObject3D() );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setSafeOptions();
			configuration.setPerPixelLightingEnabled( false );
		}

		@Nullable
		@Override
		public Image getExpectedImage()
		throws IOException
		{
			return _textureLibrary.loadImage( new BasicTextureMap( "expected-lighting.png" ) );
		}
	}

	private class PixelLighting
		extends RGBLightsProbe
	{
		private PixelLighting()
		{
			super( "Per-pixel lighting" );
		}

		@Override
		public boolean isSupported()
		{
			final JOGLCapabilities capabilities = _actual.getCapabilities();
			return super.isSupported() &&
			       ( capabilities.isShaderSupported() ||
			         capabilities.isShaderSupportedARB() );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			super.createScene( scene );

			final Color4 color = new Color4f( 0xffe0c060 );
			final BasicAppearance sphereAppearance = new BasicAppearance();
			sphereAppearance.setAmbientColor( color );
			sphereAppearance.setDiffuseColor( color );
			sphereAppearance.setSpecularColor( Color4.WHITE );
			sphereAppearance.setShininess( 16 );

			scene.addContentNode( "sphere"    , Matrix3D.IDENTITY, new Sphere3D( 0.5, 16, 16, sphereAppearance ) );
			scene.addContentNode( "sphere-inv", Matrix3D.getTranslation( 0.0, 0.0, 0.0 ), new Sphere3D( 2.0, 16, 16, sphereAppearance, true ) );

			final Object3DBuilder twoSide1 = new Object3DBuilder();
			twoSide1.addQuad( new Vector3D( -1.0, 0.0, -1.0 ),
			                  new Vector3D( -1.0, 0.0,  1.0 ),
			                  new Vector3D( 0.0, 0.0, 1.0 ),
			                  new Vector3D( 0.0, 0.0, -1.0 ), sphereAppearance, true );
			scene.addContentNode( "two-side1", Matrix3D.IDENTITY, twoSide1.getObject3D() );

			final Object3DBuilder twoSide2 = new Object3DBuilder();
			twoSide2.addQuad( new Vector3D( 0.0, 0.0, -1.0 ),
			                  new Vector3D(  1.0, 0.0, -1.0 ),
			                  new Vector3D(  1.0, 0.0,  1.0 ),
			                  new Vector3D( 0.0, 0.0, 1.0 ), sphereAppearance, true );
			scene.addContentNode( "two-side2", Matrix3D.IDENTITY, twoSide2.getObject3D() );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setSafeOptions();
			configuration.setPerPixelLightingEnabled( true );
		}

		@Nullable
		@Override
		public Image getExpectedImage()
		throws IOException
		{
			return _textureLibrary.loadImage( new BasicTextureMap( "expected-perPixelLighting.png" ) );
		}
	}

	private class Blending
		extends JOGLProbe
	{
		private Blending()
		{
			super( "Blending" );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			final Light3D light1 = new Light3D();
			light1.setFallOff( 10.0 );

			final Light3D light2 = new Light3D();
			light2.setFallOff( 5.0 );

			scene.addContentNode( "light-1", Matrix3D.getTranslation(  4.0, -4.0,  4.0 ), light1 );
			scene.addContentNode( "light-2", Matrix3D.getTranslation( -4.0, -4.0,  4.0 ), light2 );

			final BasicAppearance opaque1 = new BasicAppearance();
			opaque1.setAmbientColor( Color4.WHITE );
			opaque1.setDiffuseColor( Color4.WHITE );
			opaque1.setSpecularColor( Color4.WHITE );
			opaque1.setShininess( 16 );

			final Color4 color3 = Color4.YELLOW;
			final BasicAppearance opaque2 = new BasicAppearance();
			opaque2.setAmbientColor( color3 );
			opaque2.setDiffuseColor( color3 );
			opaque2.setSpecularColor( Color4.WHITE );
			opaque2.setShininess( 16 );

			final Color4 color2 = new Color4f( 1.0f, 0.0f, 0.0f, 0.5f );
			final BasicAppearance transparent1 = new BasicAppearance();
			transparent1.setAmbientColor( color2 );
			transparent1.setDiffuseColor( color2 );
			transparent1.setSpecularColor( Color4.WHITE );
			transparent1.setShininess( 16 );

			final Color4 color1 = new Color4f( 0.0f, 1.0f, 0.0f, 0.5f );
			final BasicAppearance transparent2 = new BasicAppearance();
			transparent2.setAmbientColor( color1 );
			transparent2.setDiffuseColor( color1 );
			transparent2.setSpecularColor( Color4.WHITE );
			transparent2.setShininess( 16 );

			final Color4 color = new Color4f( 0.0f, 0.0f, 1.0f, 0.5f );
			final BasicAppearance transparent3 = new BasicAppearance();
			transparent3.setAmbientColor( color );
			transparent3.setDiffuseColor( color );
			transparent3.setSpecularColor( Color4.WHITE );
			transparent3.setShininess( 16 );

			scene.addContentNode( "sphere-2", Matrix3D.getTranslation(  0.0, 0.0,  0.0 ), new Sphere3D( 0.5, 16, 16, transparent2 ) );
			scene.addContentNode( "sphere-3", Matrix3D.getTranslation( -0.5, 0.0,  0.5 ), new Sphere3D( 0.5, 16, 16, opaque1 ) );
			scene.addContentNode( "sphere-1", Matrix3D.IDENTITY, new Sphere3D( 1.0, 16, 16, transparent1 ) );
			scene.addContentNode( "sphere-4", Matrix3D.getTranslation(  0.5, 0.0,  0.5 ), new Sphere3D( 0.5, 16, 16, transparent3 ) );
			scene.addContentNode( "sphere-5", Matrix3D.getTranslation(  0.0, 0.0, -0.7 ), new Sphere3D( 0.5, 16, 16, opaque2 ) );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			view.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new MyFromToCameraControl( view, new Vector3D( -1.0, -3.0, 2.0 ), Vector3D.ZERO ) );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setSafeOptions();
		}

		@Nullable
		@Override
		public Image getExpectedImage()
		throws IOException
		{
			return _textureLibrary.loadImage( new BasicTextureMap( "expected-blending.png" ) );
		}
	}

	private static class MyFromToCameraControl
		extends FromToCameraControl
	{
		MyFromToCameraControl( final View3D view, final Vector3D from, final Vector3D to )
		{
			super( view, from, to );
		}

		@Override
		protected boolean isDragFromAroundToEvent( final ControlInputEvent event )
		{
			return SwingUtilities.isLeftMouseButton( event.getMouseEvent() );
		}
	}
}
