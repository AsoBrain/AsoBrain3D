/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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
package ab.j3d.loader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelTools;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.jogl.JOGLModel;

import com.numdata.oss.ui.WindowTools;

/**
 * This is a sample application for the {@link ObjLoader} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ObjLoaderApp
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		try
		{
			final double   unit      = JOGLModel.MM;
			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );

			//Load from jar file...
			//final ResourceLoader fileLoader =   new FileResourceLoader( "/home/wijnand/cube/" );
			//final ResourceLoader loader =   new ZipResourceLoader( fileLoader.getResource( "penguin.jar" ) );

			//Or load from directory
			final ResourceLoader loader = new FileResourceLoader( "/home/meinders/obj/" );

			final Object3D object3d = ObjLoader.load( transform , loader , "door2c.obj" );
			final Bounds3D bounds   = object3d.getOrientedBoundingBox();
			final Vector3D size     = bounds.size();
			final double   toCM     = 100.0 * unit;

			final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
			final Vector3D viewAt   = Vector3D.INIT.set( 0.0 , 0.0 , bounds.v2.z / 2.0 );

			final ViewModel viewModel = new JOGLModel( unit , Color.WHITE );
			ViewModelTools.addLegacyLights( viewModel );

			viewModel.createNode( "obj" , Matrix3D.INIT.plus( 0.0 , 0.0 , -bounds.v1.z ) , object3d , null , 1.0f );

			final ViewModelView view = viewModel.createView();
			view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );

			final JPanel viewPanel = new JPanel( new BorderLayout() );
			viewPanel.add( view.getComponent() , BorderLayout.CENTER );
			viewPanel.add( view.createToolBar( new Locale( "nl" ) ) , BorderLayout.SOUTH );

			final JFrame frame = WindowTools.createFrame( viewModel.getClass() + " example" , 800 , 600 , viewPanel );
			frame.setVisible( true );

			System.out.println( "object size = " + Math.round( toCM * size.x ) + " x " + Math.round( toCM * size.y ) + " x " + Math.round( toCM * size.z ) + " cm" );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private ObjLoaderApp()
	{
	}
}
