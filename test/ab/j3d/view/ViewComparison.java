/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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
import java.util.Iterator;
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
import ab.j3d.model.Sphere3D;
import ab.j3d.pov.AbToPovConverter;
import ab.j3d.pov.PovScene;
import ab.j3d.pov.PovVector;
import ab.j3d.view.java3d.Java3dModel;
import ab.j3d.view.jogl.JOGLModel;

/**
 * Shows a 3D scene using the various view models in several lighting modes, for
 * comparison purposes.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ViewComparison
	implements Runnable
{
	private static final boolean INCLUDE_POV_VIEW = true;

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
		final ArrayList<Scene> scenes = new ArrayList<Scene>();
		scenes.add( new DefaultScene() );
		scenes.add( new DiffuseOnlyScene() );
		scenes.add( new AmbientOnlyScene() );

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

		for ( final Scene scene : scenes )
		{
			final JPanel layoutPanel = new JPanel();
			layoutPanel.setLayout( new GridLayout( 0 , 1 ) );

			final Collection<ViewModel> models = createViewModels();
			for ( final ViewModel model : models )
			{
				scene.createModel( model );

				final ViewModelView view = model.createView( "view" );
				scene.configureView( view );

				final Component component = view.getComponent();
				layoutPanel.add( component );
			}

			if ( INCLUDE_POV_VIEW )
			{
				final Iterator<ViewModel> modelIterator = models.iterator();
				final ViewModel           povModel      = modelIterator.next();
				final ViewModelView       povView       = povModel.getView( "view" );

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
								final Matrix3D viewTransform   = povView.getViewTransform();
								final Matrix3D cameraTransform = viewTransform.inverse();
								final double   aspectRatio     = (double)size.width / (double)size.height;

								final AbToPovConverter converter = new AbToPovConverter( MapTools.imageMapDirectory );
								final PovScene scene = converter.convert( povModel.getScene() );
								scene.add( AbToPovConverter.convertCamera3D( cameraTransform , camera , aspectRatio ) );

								scene.setBackground( new PovVector( Color.GRAY ) );

								publish( scene.render( null , size.width , size.height , null , new PrintWriter( System.err ) , true ) );
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

	private static Collection<ViewModel> createViewModels()
	{
		final Collection<ViewModel> models = new ArrayList<ViewModel>();
		models.add( new JOGLModel  ( ViewModel.MM , Color.GRAY ) );
		models.add( new Java3dModel( ViewModel.MM , Color.GRAY ) );
		return models;
	}

	private abstract static class Scene
	{
		private Vector3D _cameraLocation;

		private Vector3D _cameraTarget;

		protected Scene()
		{
			_cameraLocation = Vector3D.INIT.set( 500.0 , -500.0 , 500.0 );
			_cameraTarget   = Vector3D.INIT.plus( 0.0 , 150.0 , 40.0 );
		}

		public void setCameraLocation( final Vector3D cameraLocation )
		{
			_cameraLocation = cameraLocation;
		}

		public void setCameraTarget( final Vector3D cameraTarget )
		{
			_cameraTarget = cameraTarget;
		}

		public void configureView( final ViewModelView target )
		{
			target.setCameraControl( new FromToCameraControl( target , _cameraLocation , _cameraTarget ) );
		}

		public abstract void createModel( final ViewModel target );
	}

	private static class DefaultScene
		extends Scene
	{
		public void createModel( final ViewModel target )
		{
			final Material solid     = new Material( 0xffff8000 ); solid    .code = "solid";
			final Material shiny     = new Material( 0xffff8000 ); shiny    .code = "shiny";
			final Material shinier   = new Material( 0xffff8000 ); shinier  .code = "shinier";
			final Material textured  = new Material( 0xffffffff ); textured .code = "textured";
			final Material textured2 = new Material( 0xff0080ff ); textured2.code = "textured2";
			final Material textured3 = new Material( 0xffff0000 ); textured2.code = "textured3";

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
			target.createNode( "sphere-1" , new Sphere3D( Matrix3D.INIT.plus( -100.0 , -100.0 , 40.0 ) , 80.0 , 80.1 , 80.2 , 16 , 16 , solid     , false ) , null , 1.0f );
			target.createNode( "sphere-2" , new Sphere3D( Matrix3D.INIT.plus(    0.0 , -100.0 , 40.0 ) , 80.0 , 80.1 , 80.2 , 16 , 16 , solid     , true  ) , null , 1.0f );
			target.createNode( "sphere-3" , new Sphere3D( Matrix3D.INIT.plus(  100.0 , -100.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , textured  , true  ) , null , 1.0f );
			target.createNode( "sphere-4" , new Sphere3D( Matrix3D.INIT.plus( -100.0 ,    0.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , solid     , true  ) , null , 1.0f );
			target.createNode( "sphere-5" , new Sphere3D( Matrix3D.INIT.plus(    0.0 ,    0.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , shiny     , true  ) , null , 1.0f );
			target.createNode( "sphere-6" , new Sphere3D( Matrix3D.INIT.plus(  100.0 ,    0.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , shinier   , true  ) , null , 1.0f );
			target.createNode( "box-1"    , new Box3D   ( Matrix3D.INIT.plus( -140.0 ,   60.0 ,  0.0 ) , 80.0 , 80.0 , 80.0 , ViewModel.MM , solid , solid ) , null , 1.0f );

			/*
			 * Test advanced texturing. (i.e. with non-white diffuse color)
			 */
			target.createNode( "sphere-7" , new Sphere3D( Matrix3D.INIT.plus(    0.0 ,  100.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , textured2 , true  ) , null , 1.0f );
			target.createNode( "sphere-8" , new Sphere3D( Matrix3D.INIT.plus(  100.0 ,  100.0 , 40.0 ) , 80.0 , 80.0 , 80.0 , 16 , 16 , textured3 , true  ) , null , 1.0f );

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
					target.createNode( "ambient-sphere-" + j , new Sphere3D( Matrix3D.INIT.plus( x , y , z ) , 40.0 , 40.0 , 40.0 , 16 , 16 , material , true ) , null , 1.0f );

					i++;
					j++;
				}
				z += 100.0;
			}

			createLights( target );
		}

		protected void createLights( final ViewModel target )
		{
			target.createNode( "ambient-1" , Matrix3D.INIT , new Light3D( 128 , -1.0 ) , null , 1.0f );
			target.createNode( "light-1" , Matrix3D.INIT.plus(  1000.0 ,  -1000.0 ,  1000.0 ) , new Light3D( 250 , 0.0 ) , null , 1.0f );
		}
	}

	private static class DiffuseOnlyScene
		extends DefaultScene
	{
		protected void createLights( final ViewModel target )
		{
			target.createNode( "light-1" , Matrix3D.INIT.plus(  1000.0 ,  -1000.0 ,  1000.0 ) , new Light3D( 250 , 0.0 ) , null , 1.0f );
		}
	}

	private static class AmbientOnlyScene
		extends DefaultScene
	{
		protected void createLights( final ViewModel target )
		{
			target.createNode( "ambient-1" , Matrix3D.INIT , new Light3D( 128 , -1.0 ) , null , 1.0f );
		}
	}
}
