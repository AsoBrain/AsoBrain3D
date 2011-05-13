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

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;
import com.numdata.oss.ui.*;

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
			final double   unit      = Scene.M;
			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );

			//Load from jar file...
			//final ResourceLoader fileLoader =   new FileResourceLoader( "/home/wijnand/cube/" );
			//final ResourceLoader loader =   new ZipResourceLoader( fileLoader.getResource( "penguin.jar" ) );

			//Or load from directory
			final ResourceLoader loader = new FileResourceLoader( System.getProperty( "user.home" ) + "/soda/Ivenza_LayoutPlugin/objects/HiFi" );

			final Object3D object3d = ObjLoader.load( transform , loader , "tv06.obj" );
			GeometryTools.smooth( object3d, 30.0, 5.0, false );
			final Bounds3D bounds   = object3d.getOrientedBoundingBox();
			final Vector3D size     = bounds.size();
			final double   toCM     = 100.0 * unit;

			final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
			final Vector3D viewAt   = Vector3D.INIT.set( 0.0 , 0.0 , bounds.v2.z / 2.0 );

			final Scene scene = new Scene( unit );
			scene.addContentNode( "obj" , Matrix3D.getTransform( 90.0, 0.0, 0.0, 0.0 , 0.0 , -bounds.v1.z ) , object3d );
//			scene.addContentNode( "sphere", null, new Sphere3D( 0.1, 8, 8, Materials.ALU_PLATE ) );

			final Light3D light1 = new Light3D();
			light1.setIntensity( 0.7f );
			light1.setFallOff( 10.0 );
			light1.setCastingShadows( true );
			final ContentNode lightNode1 = scene.addContentNode( "light-1", null, light1 );

			final Light3D light2 = new Light3D();
			light2.setIntensity( 0.5f );
			light2.setFallOff( 0.0 );
			scene.addContentNode( "light-2", Matrix3D.getTranslation( -10.0, 10.0, 10.0 ), light2 );

			final RenderEngine renderEngine = new JOGLEngine( /*JOGLConfiguration.createLusciousInstance()*/ );

			final View3D view = renderEngine.createView( scene );
			view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );
			view.addViewListener( new ViewListener()
			{
				@Override
				public void beforeFrame( final View3D view )
				{
				}

				@Override
				public void afterFrame( final View3D view )
				{
					final double alpha = ( (double)System.currentTimeMillis() / 1000.0 ) % ( 2.0 * Math.PI );
					final Vector3D center = bounds.center();
					lightNode1.setTransform( Matrix3D.getTranslation( center.x + Math.cos( alpha ) * 5.0, center.y + Math.sin( alpha ) * 5.0, center.z + 5.0 ) );
				}
			} );
			scene.setAnimated( true );

			final JPanel viewPanel = new JPanel( new BorderLayout() );
			viewPanel.add( view.getComponent() , BorderLayout.CENTER );
			viewPanel.add( view.createToolBar( new Locale( "nl" ) ) , BorderLayout.SOUTH );

			final JFrame frame = WindowTools.createFrame( renderEngine.getClass() + " example" , 800 , 600 , viewPanel );
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
