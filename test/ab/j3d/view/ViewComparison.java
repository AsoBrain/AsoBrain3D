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
import java.util.Collection;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.model.Box3D;
import ab.j3d.model.Light3D;
import ab.j3d.pov.AbToPovConverter;
import ab.j3d.pov.PovScene;
import ab.j3d.view.java3d.Java3dModel;
import ab.j3d.view.jogl.JOGLModel;

/**
 * Shows several scenes using various view models, for comparison purposes.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ViewComparison
	implements Runnable
{
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

			final Iterator<ViewModel> modelIterator = models.iterator();
			final ViewModel           first         = modelIterator.next();
			final AbToPovConverter    povConverter  = new AbToPovConverter( MapTools.imageMapDirectory );
			final PovScene            povScene      = povConverter.convert( first.getScene() );

			final JLabel povComponent = new JLabel();
			layoutPanel.add( povComponent );

			final SwingWorker<BufferedImage, Object> povImageRenderer = new SwingWorker<BufferedImage, Object>()
			{
				protected BufferedImage doInBackground()
					throws Exception
				{
					final Dimension size = povComponent.getSize();
					return povScene.render( null , size.width , size.height , null , new PrintWriter( System.err ) , true );
				}

				protected void done()
				{
					try
					{
						final BufferedImage image = get();
						povComponent.setIcon( new ImageIcon( image ) );
					}
					catch ( Exception e )
					{
						povComponent.setText( "<html><p>Failed to render scene using POV.</p><p>" + e.getMessage() + "</p>" );
					}
				}
			};
			povImageRenderer.execute();

			frame.add( layoutPanel );
		}

		frame.pack();
		frame.setExtendedState( Frame.MAXIMIZED_BOTH );
		frame.setVisible( true );
	}

	private static Collection<ViewModel> createViewModels()
	{
		final Collection<ViewModel> models = new ArrayList<ViewModel>();
		models.add( new JOGLModel( ViewModel.MM , Color.BLACK ) );
		models.add( new Java3dModel( ViewModel.MM , Color.BLACK ) );
		return models;
	}

	private abstract static class Scene
	{
		private Vector3D _cameraLocation;

		private Vector3D _cameraTarget;

		protected Scene()
		{
			_cameraLocation = Vector3D.INIT.set( 50.0 , -200.0 , 200.0 );
			_cameraTarget   = Vector3D.INIT;
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
			final Material material = new Material( 0xffff8000 );
			final Box3D box = new Box3D( Matrix3D.INIT, 50.0, 80.0, 110.0, ViewModel.MM, material, material );
			target.createNode( "box" , box , null , 1.0f );

			target.createNode( "ambient-1" , Matrix3D.INIT , new Light3D( 10 , -1.0 ) , null , 1.0f );
			target.createNode( "light-1" , Matrix3D.INIT.plus(  1000.0 ,  -1000.0 ,  1000.0 ) , new Light3D( 2550 , 10000.0 ) , null , 1.0f );
		}
	}

	private static class DiffuseOnlyScene
		extends Scene
	{
		public void createModel( final ViewModel target )
		{

		}
	}

	private static class AmbientOnlyScene
		extends Scene
	{
		public void createModel( final ViewModel target )
		{

		}
	}
}
