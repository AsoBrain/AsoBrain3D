/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Node3D;
import ab.j3d.view.FromToViewControl;
import ab.j3d.view.ViewModel;
import ab.j3d.view.java3d.Java3dModel;

import com.numdata.oss.ui.WindowTools;

/**
 * This is a sample application for the {@link Max3DSLoader} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Max3DSLoaderApp
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
//			final double   unit      = Java3dModel.FOOT;
			final double   unit      = Java3dModel.INCH;

			final Matrix3D transform = Matrix3D.INIT; // .rotateX( Math.toRadians( 90.0 ) );

//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/fishtank/fishtank.3ds";
			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/flower01_s/flower01.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/vtr/vtr.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man1/man1.3ds"; // standing straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man2/man2.3ds"; // sitting + lifted arms
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man3/man3.3ds"; // on knees + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man4/man4.3ds"; // walking straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man5/man5.3ds"; // kneeling + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man6/man6.3ds"; // bending  + grasping
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man7/man7.3ds"; // kneeling + accepting
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man8/man8.3ds"; // stair walking + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman1/woman1.3ds"; // standing straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman2/woman2.3ds"; // sitting + lifted arms
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman3/woman3.3ds"; // on knees + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman4/woman4.3ds"; // walking straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman5/woman5.3ds"; // kneeling + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman6/woman6.3ds"; // bending  + grasping
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman7/woman7.3ds"; // kneeling + accepting
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman8/woman8.3ds"; // stair walking + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman9/woman1.3ds"; // full nudity
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/tricycle/trecic3.3ds";


			final Node3D object3d = Max3DSLoader.load( transform , new File( path ) );

			final Bounds3D bounds   = Bounds3D.INIT; // object3d.getBounds( null , null );
			final Vector3D size     = bounds.size();
			final double   toCM     = 100.0 * unit;

			System.out.println( "object size = " + Math.round( toCM * size.x ) + " x " + Math.round( toCM * size.y ) + " x " + Math.round( toCM * size.z ) + " cm" );

			final ViewModel viewModel = new Java3dModel( unit , Color.lightGray ); // new Color( 51 , 77 , 102 ) );
			viewModel.createNode( "obj" , Matrix3D.INIT.plus( 0.0 , 0.0 , -bounds.v1.z ) , object3d , null , 1.0f );

			final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
			final Vector3D  viewAt   = Vector3D.INIT.set( 0.0 , 0.0 , bounds.v2.z / 2.0 );

			viewModel.createView( "view" , new FromToViewControl( viewFrom , viewAt ) );

			final JPanel viewPanel = viewModel.createViewPanel( new Locale( "nl" ), "view" );

			final JFrame frame = WindowTools.createFrame( viewModel.getClass() + " example" , 800 , 600 , viewPanel );
			frame.setVisible( true );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Max3DSLoaderApp()
	{
	}
}
