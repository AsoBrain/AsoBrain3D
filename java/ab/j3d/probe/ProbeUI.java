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
import java.awt.image.BufferedImage;
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
import ab.j3d.control.FromToCameraControl2;
import ab.j3d.model.Light3D;
import ab.j3d.model.Scene;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.ProjectionPolicy;
import ab.j3d.view.RenderEngine;
import ab.j3d.view.RenderingPolicy;
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
	private JPanel _expectedContainer;

	private JPanel _actualContainer;

	/**
	 * Construct new DiagnosticUI.
	 */
	public ProbeUI()
	{
		final List<Probe> probes = Arrays.<Probe>asList(
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
			button.setBackground( probe.isSupported() ? Color.GREEN : Color.YELLOW );
			probesPanel.add( button );
		}

		final JPanel expectedContainer = new JPanel();
		expectedContainer.setBackground( Color.BLACK );
		expectedContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		expectedContainer.setPreferredSize( new Dimension( 300 , 300 ) );
		expectedContainer.setLayout( new BorderLayout() );
		_expectedContainer = expectedContainer;

		final JPanel actualContainer = new JPanel();
		actualContainer.setBackground( Color.BLACK );
		actualContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		actualContainer.setPreferredSize( new Dimension( 300 , 300 ) );
		actualContainer.setLayout( new BorderLayout() );
		_actualContainer = actualContainer;

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

			final JPanel expectedContainer = _expectedContainer;
			final JPanel actualContainer   = _actualContainer;

			expectedContainer.removeAll();
			actualContainer.removeAll();
			actualContainer.revalidate();

			final Scene scene = createScene();

			final RenderEngine engine = new JOGLEngine();

			final JOGLView view = (JOGLView)engine.createView( scene );
			configureView( view );

			actualContainer.add( view.getComponent() );
			actualContainer.revalidate();

			final JOGLCapabilities capabilities = view.getCapabilities();
			capabilities.printSummary( System.out );
		}

		protected abstract Scene createScene();

		protected abstract void configureView( final JOGLView view );
	}

	private abstract class RGBLightsProbe
		extends JOGLProbe
	{
		protected RGBLightsProbe( final String name )
		{
			super( name );
		}

		protected Scene createScene()
		{
			final Scene scene = new Scene( Scene.MM );

			final Light3D light1 = new Light3D();
			light1.setDiffuse( 1.0f , 0.0f , 0.0f );
			light1.setFallOff( 10.0 );

			final Light3D light2 = new Light3D();
			light2.setDiffuse( 0.0f , 1.0f , 0.0f );
			light2.setFallOff( 10.0 );

			final Light3D light3 = new Light3D();
			light3.setDiffuse( 0.0f , 0.0f , 1.0f );
			light3.setFallOff( 10.0 );

			scene.addContentNode( "light-1" , Matrix3D.INIT.setTranslation(  4.0 , -4.0 ,  4.0 ) , light1 );
			scene.addContentNode( "light-2" , Matrix3D.INIT.setTranslation( -4.0 , -4.0 ,  4.0 ) , light2 );
			scene.addContentNode( "light-3" , Matrix3D.INIT.setTranslation(  0.0 , -4.0 , -4.0 ) , light3 );

			return scene;
		}

		protected void configureView( final JOGLView view )
		{
			view.setBackground( Color.BLACK );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new FromToCameraControl2( view , new Vector3D( -1.0 , -3.0 , 2.0 ) , Vector3D.INIT ) );
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

		protected Scene createScene()
		{
			final Scene scene = super.createScene();

			final Material sphereMaterial = new Material( 0xffe0c060 );
			scene.addContentNode( "sphere"     , Matrix3D.INIT , new Sphere3D( 1.0 , 16 , 16 , sphereMaterial ) );
			scene.addContentNode( "sphere-inv" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , new Sphere3D( 2.0 , 16 , 16 , sphereMaterial , true ) );

			return scene;
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

		protected Scene createScene()
		{
			final Scene scene = super.createScene();

			MapTools.imageMapDirectory = "";
			final BufferedImage image = MapTools.getImage( "image" );
			System.out.println( "image = " + image );

			final Material sphereMaterial = new Material( 0xffe0c060 );
			sphereMaterial.colorMap       = "/home/meinders/soda/AsoBrain3D/java/image";
			sphereMaterial.colorMapWidth  = 0.01f;
			sphereMaterial.colorMapHeight = 0.01f;

			scene.addContentNode( "sphere"     , Matrix3D.INIT , new Sphere3D( 1.0 , 16 , 16 , sphereMaterial ) );
			scene.addContentNode( "sphere-inv" , Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 ) , new Sphere3D( 2.0 , 16 , 16 , sphereMaterial , true ) );

			return scene;
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
		protected Scene createScene()
		{
			final Scene scene = new Scene( Scene.MM );

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

			return scene;
		}

		protected void configureView( final JOGLView view )
		{
			view.setBackground( Color.BLACK );
			view.setFrontClipDistance( 0.01 );
			view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
			view.setRenderingPolicy( RenderingPolicy.SOLID );
			view.setCameraControl( new FromToCameraControl2( view , new Vector3D( -1.0 , -3.0 , 2.0 ) , Vector3D.INIT ) );
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
		protected void configureView( final JOGLView view )
		{
			super.configureView( view );

			final JOGLConfiguration configuration = view.getConfiguration();
			configuration.setPerPixelLightingEnabled( false );
			configuration.setDepthPeelingEnabled( true );
		}
	}
}
