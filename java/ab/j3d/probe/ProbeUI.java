/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.probe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.control.FromToCameraControl2;
import ab.j3d.geom.PlanarUVMap;
import ab.j3d.model.Light3D;
import ab.j3d.model.Object3DBuilder;
import ab.j3d.model.Scene;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.ProjectionPolicy;
import ab.j3d.view.RenderEngine;
import ab.j3d.view.RenderingPolicy;
import ab.j3d.view.View3D;
import ab.j3d.view.jogl.JOGLCapabilities;
import ab.j3d.view.jogl.JOGLConfiguration;
import ab.j3d.view.jogl.JOGLEngine;
import ab.j3d.view.jogl.JOGLView;

/**
 * A user interface for testing 3D capabilities.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ProbeUI
	extends JPanel
{
	private JLabel _expected;

	private JOGLView _actual;

	/**
	 * Construct new DiagnosticUI.
	 */
	public ProbeUI()
	{
		final List<Probe> probes = Arrays.<Probe>asList(
			new TextureProbe() ,
			new VertexLighting() ,
			new PixelLighting() ,
			new Blending() ,
			new DepthPeeling()
		);

		final JPanel probesPanel = new JPanel();
		for ( final Probe probe : probes )
		{
			final JButton button = new JButton( new AbstractAction( probe.getName() )
			{
				public void actionPerformed( final ActionEvent e )
				{
					if ( probe.isSupported() )
					{
						probe.run();
					}
					else
					{
						final int choice = JOptionPane.showConfirmDialog( ProbeUI.this , "It appears that your computer isn't capable of running this probe.\nAre you sure you want to try it anyway?" , "Probe not supported" , JOptionPane.YES_NO_CANCEL_OPTION , JOptionPane.QUESTION_MESSAGE );
						if ( choice == JOptionPane.YES_OPTION )
						{
							probe.run();
						}
					}
				}
			} );
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					button.setBackground( probe.isSupported() ? Color.GREEN : Color.YELLOW );
				}
			} );
			probesPanel.add( button );
		}

		final JPanel expectedContainer = new JPanel();
		expectedContainer.setBackground( Color.BLACK );
		expectedContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		expectedContainer.setPreferredSize( new Dimension( 300 , 300 ) );
		expectedContainer.setLayout( new BorderLayout() );

		final JLabel expected = new JLabel();
		expectedContainer.add( expected , BorderLayout.CENTER );

		final JPanel actualContainer = new JPanel();
		actualContainer.setBackground( Color.BLACK );
		actualContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		actualContainer.setPreferredSize( new Dimension( 300 , 300 ) );
		actualContainer.setLayout( new BorderLayout() );

		final RenderEngine engine = new JOGLEngine();
		final Scene scene = new Scene( Scene.MM );
		final JOGLView view = (JOGLView)engine.createView( scene );
		actualContainer.add( view.getComponent() , BorderLayout.CENTER );
		_actual = view;

		final JPanel expectedPanel = new JPanel();
		expectedPanel.setLayout( new BorderLayout( 5 , 5 ) );
		expectedPanel.add( new JLabel( "Expected" , SwingConstants.CENTER ) , BorderLayout.NORTH );
		expectedPanel.add( expectedContainer , BorderLayout.CENTER );

		final JPanel actualPanel = new JPanel();
		actualPanel.setLayout( new BorderLayout( 5 , 5 ) );
		actualPanel.add( new JLabel( "Your computer" , SwingConstants.CENTER ) , BorderLayout.NORTH );
		actualPanel.add( actualContainer , BorderLayout.CENTER );

		final JPanel comparisonPanel = new JPanel();
		comparisonPanel.setLayout( new GridLayout( 1 , 2 , 5 , 5 ) );
		comparisonPanel.add( expectedPanel );
		comparisonPanel.add( actualPanel );

		setBorder( BorderFactory.createEmptyBorder( 5 , 5 , 5 , 5 ) );
		setLayout( new BorderLayout( 5 , 5 ) );
		add( probesPanel , BorderLayout.NORTH );
		add( comparisonPanel , BorderLayout.CENTER );
	}

	private abstract static class Probe
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

		public boolean isSupported()
		{
			return true;
		}

		public void run()
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
			capabilities.printSummary( System.out );
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

		protected void createScene( final Scene scene )
		{
			MapTools.imageMapFilenameSuffix = ".png";
			MapTools.imageMapDirectory = "";

			final Material texture1 = new Material( 0xffffffff );
			texture1.colorMap = "ab/j3d/probe/texture1";
			texture1.colorMapWidth = 0.001f;
			texture1.colorMapHeight = 0.001f;
			texture1.specularColorRed = 0.0f;
			texture1.specularColorGreen = 0.0f;
			texture1.specularColorBlue = 0.0f;

			final Material green = new Material( 0xff00ff00 );
			green.specularColorRed = 0.0f;
			green.specularColorGreen = 0.0f;
			green.specularColorBlue = 0.0f;

			final Light3D light1 = new Light3D();
			light1.setFallOff( 0.0 );

			scene.addContentNode( "light" , Matrix3D.INIT.setTranslation(  0.0 , -4.0 ,  0.0 ) , light1 );

			final Object3DBuilder plane1 = new Object3DBuilder();
			plane1.addQuad( new Vector3D( -0.5 , 0.0 , -0.5 ) ,
			                new Vector3D( -0.5 , 0.0 ,  0.5 ) ,
			                new Vector3D(  0.5 , 0.0 ,  0.5 ) ,
			                new Vector3D(  0.5 , 0.0 , -0.5 ) , texture1 , new PlanarUVMap( scene.getUnit() , new Vector3D( -0.5 , 0.0 , -0.5 ) , Vector3D.NEGATIVE_Y_AXIS ) , false );
			scene.addContentNode( "plane1" , Matrix3D.INIT , plane1.getObject3D() );

			final Object3DBuilder plane2 = new Object3DBuilder();
			plane2.addQuad( new Vector3D( -0.5 , 0.1 , -0.5 ) ,
			                new Vector3D( -0.5 , 0.1 ,  0.5 ) ,
			                new Vector3D(  0.5 , 0.1 ,  0.5 ) ,
			                new Vector3D(  0.5 , 0.1 , -0.5 ) , green , false );
			scene.addContentNode( "plane2" , Matrix3D.INIT , plane2.getObject3D() );
		}

		protected void configureView( final JOGLView view )
		{
			view.setBackground( Color.WHITE );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new FromToCameraControl( view , new Vector3D( 0.0 , -1.5 , 0.0 ) , Vector3D.INIT ) );
		}
	}

	private abstract class RGBLightsProbe
		extends JOGLProbe
	{
		protected RGBLightsProbe( final String name )
		{
			super( name );
		}

		protected void createScene( final Scene scene )
		{
			final Light3D light1 = new Light3D();
			light1.setDiffuse( 1.0f , 0.0f , 0.0f );
			light1.setFallOff( 10.0 );

			final Light3D light2 = new Light3D();
			light2.setDiffuse( 0.0f , 1.0f , 0.0f );
			light2.setFallOff( 10.0 );

			final Light3D light3 = new Light3D();
			light3.setDiffuse( 1.0f , 1.0f , 1.0f );
			light3.setFallOff( 5.0 );

			scene.addContentNode( "light-1" , Matrix3D.INIT.setTranslation(  4.0 , -4.0 ,  4.0 ) , light1 );
			scene.addContentNode( "light-2" , Matrix3D.INIT.setTranslation( -4.0 , -4.0 ,  4.0 ) , light2 );
			scene.addContentNode( "light-3" , Matrix3D.INIT.setTranslation(  0.0 ,  4.0 ,  0.0 ) , light3 );
		}

		protected void configureView( final JOGLView view )
		{
			view.setBackground( Color.BLACK );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new FromToCameraControl( view , new Vector3D( -1.0 , -3.0 , 2.0 ) , Vector3D.INIT ) );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setDepthPeelingEnabled( false );
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

		protected void createScene( final Scene scene )
		{
			super.createScene( scene );

			final Material sphereMaterial = new Material( 0xffe0c060 );
			scene.addContentNode( "sphere"     , Matrix3D.INIT , new Sphere3D( 0.5 , 16 , 16 , sphereMaterial ) );
			scene.addContentNode( "sphere-inv" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , new Sphere3D( 2.0 , 16 , 16 , sphereMaterial , true ) );

			final Object3DBuilder twoSide1 = new Object3DBuilder();
			twoSide1.addQuad( new Vector3D( -1.0 , 0.0 , -1.0 ) ,
			                  new Vector3D( -1.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 , -1.0 ) , sphereMaterial , true );
			scene.addContentNode( "two-side1" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , twoSide1.getObject3D() );

			final Object3DBuilder twoSide2 = new Object3DBuilder();
			twoSide2.addQuad( new Vector3D(  0.0 , 0.0 , -1.0 ) ,
			                  new Vector3D(  1.0 , 0.0 , -1.0 ) ,
			                  new Vector3D(  1.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 ,  1.0 ) , sphereMaterial , true );
			scene.addContentNode( "two-side2" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , twoSide2.getObject3D() );
		}

		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setPerPixelLightingEnabled( false );
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

		protected void createScene( final Scene scene )
		{
			super.createScene( scene );

			final Material sphereMaterial = new Material( 0xffe0c060 );
			scene.addContentNode( "sphere"     , Matrix3D.INIT , new Sphere3D( 0.5 , 16 , 16 , sphereMaterial ) );
			scene.addContentNode( "sphere-inv" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , new Sphere3D( 2.0 , 16 , 16 , sphereMaterial , true ) );

			final Object3DBuilder twoSide1 = new Object3DBuilder();
			twoSide1.addQuad( new Vector3D( -1.0 , 0.0 , -1.0 ) ,
			                  new Vector3D( -1.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 , -1.0 ) , sphereMaterial , true );
			scene.addContentNode( "two-side1" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , twoSide1.getObject3D() );

			final Object3DBuilder twoSide2 = new Object3DBuilder();
			twoSide2.addQuad( new Vector3D(  0.0 , 0.0 , -1.0 ) ,
			                  new Vector3D(  1.0 , 0.0 , -1.0 ) ,
			                  new Vector3D(  1.0 , 0.0 ,  1.0 ) ,
			                  new Vector3D(  0.0 , 0.0 ,  1.0 ) , sphereMaterial , true );
			scene.addContentNode( "two-side2" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , twoSide2.getObject3D() );
		}

		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setPerPixelLightingEnabled( true );
		}
	}

	private abstract class TransparencyProbe
		extends JOGLProbe
	{
		protected TransparencyProbe( final String name )
		{
			super( name );
		}

		@Override
		protected void createScene( final Scene scene )
		{
			final Light3D light1 = new Light3D();
			light1.setFallOff( 10.0 );

			final Light3D light2 = new Light3D();
			light2.setFallOff( 5.0 );

			scene.addContentNode( "light-1" , Matrix3D.INIT.setTranslation(  4.0 , -4.0 ,  4.0 ) , light1 );
			scene.addContentNode( "light-2" , Matrix3D.INIT.setTranslation( -4.0 , -4.0 ,  4.0 ) , light2 );

			final Material opaque1      = new Material( 0xffffffff );
			final Material opaque2      = new Material( 0xffffff00 );
			final Material transparent1 = new Material( 0x80ff0000 );
			final Material transparent2 = new Material( 0x8000ff00 );
			final Material transparent3 = new Material( 0x800000ff );

			scene.addContentNode( "sphere-2" , Matrix3D.INIT.setTranslation(  0.0 , 0.0 ,  0.0 ) , new Sphere3D( 0.5 , 16 , 16 , transparent2 ) );
			scene.addContentNode( "sphere-3" , Matrix3D.INIT.setTranslation( -0.5 , 0.0 ,  0.5 ) , new Sphere3D( 0.5 , 16 , 16 , opaque1 ) );
			scene.addContentNode( "sphere-1" , Matrix3D.INIT , new Sphere3D( 1.0 , 16 , 16 , transparent1 ) );
			scene.addContentNode( "sphere-4" , Matrix3D.INIT.setTranslation(  0.5 , 0.0 ,  0.5 ) , new Sphere3D( 0.5 , 16 , 16 , transparent3 ) );
			scene.addContentNode( "sphere-5" , Matrix3D.INIT.setTranslation(  0.0 , 0.0 , -0.7 ) , new Sphere3D( 0.5 , 16 , 16 , opaque2 ) );
		}

		protected void configureView( final JOGLView view )
		{
			view.setBackground( Color.BLACK );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new FromToCameraControl( view , new Vector3D( -1.0 , -3.0 , 2.0 ) , Vector3D.INIT ) );
		}
	}

	private class Blending
		extends TransparencyProbe
	{
		private Blending()
		{
			super( "Blending" );
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setPerPixelLightingEnabled( false );
			configuration.setDepthPeelingEnabled( false );
		}
	}

	private class DepthPeeling
		extends TransparencyProbe
	{
		private DepthPeeling()
		{
			super( "Depth peeling" );
		}

		@Override
		public boolean isSupported()
		{
			final JOGLCapabilities capabilities = _actual.getCapabilities();
			return super.isSupported() && capabilities.isDepthPeelingSupported();
		}

		@Override
		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setPerPixelLightingEnabled( false );
			configuration.setDepthPeelingEnabled( true );
		}
	}

	private static class FromToCameraControl
		extends FromToCameraControl2
	{
		FromToCameraControl( final View3D view , final Vector3D from , final Vector3D to )
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
