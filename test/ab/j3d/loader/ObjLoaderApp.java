/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ab.j3d.Bounds3D;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelTools;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.java3d.Java3dModel;

import com.numdata.oss.ui.WindowTools;

/**
 * This is a sample application for the {@link ObjLoader} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ObjLoaderApp
{
	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		try
		{
			final Map<String,Material> materials = new HashMap();

//		materials.put(  "casing_black"  , new Material( 0xFF000000 ) );
//		materials.put(  "key_blue"      , new Material( 0xFF8080FF ) );
//		materials.put(  "keys_auv"      , new Material( 0xFF808080 ) );
//		materials.put(  "paint"         , new Material( 0xFFFF0000 ) );
//		materials.put(  "wrought"       , new Material( 0xFFC0C0D0 ) );


// lava lamp
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lavalamp.obj";

// office desk chair (complex, no material)
//			final double   unit      = Java3dModel.FOOT;
//			final Matrix3D transform = Matrix3D.INIT.rotateZ( Math.toRadians( 135.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/Offchr9.obj";

// T typical office desk chair
//			final double   unit      = Java3dModel.M / 2.0;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/chair1.obj";
//			materials.put(  "fabric"        , new Material( 0xFF0000FF ) );

// T computer desk (wood panels + metal frame)
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/desk.obj";

// T detailed desk lamp with wire
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/completedesklamp.obj";

// big beer cup with lit
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/complete_closed_stein.obj";

// T wooden conference chair on steel struts
//			final double   unit      = Java3dModel.CM;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/turbosquid/7chair.obj";
//			materials.put(  "cap"           , new Material( 0xFF101010 ) );
//			materials.put(  "leg"           , new Material( 0xFFE0E0F0 ) );
//			materials.put(  "sheet"         , new Material( 0xFFE0C060 ) );

// overly detailed toilet
//			final double   unit      = Java3dModel.M / 10.0;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/turbosquid/t18_mod.obj";

// overly detailed thinkpad
//			final double   unit      = Java3dModel.INCH;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/turbosquid/t30.obj";

// T bed (straight with drawers, wood + mint sheet)
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/bed.obj";
//			materials.put(  "wood_dark" , new Material( 0xFF502810 ) );
//			materials.put(  "sheet"     , new Material( 0xFFA0C0A0 ) );

// T coffee table (wood + glass)
//			final double   unit      = Java3dModel.M / 10.0;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/coffeetable.obj";
//			materials.put(  "wood_1" , new Material( 0xFF603820 ) );
//			materials.put(  "wood_2" , new Material( 0xFF603820 ) );
//			materials.put(  "wood_3" , new Material( 0xFF603820 ) );
//			materials.put(  "wood_4" , new Material( 0xFF603820 ) );
//			materials.put(  "glass2" , new Material( 0x20101814 ) );

// T dining room chair
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/chair2.obj";
//			materials.put(  "surf1" , new Material( 0xFF603820 ) ); // main
//			materials.put(  "surf2" , new Material( 0xFF502810 ) ); // bottom side

// T love seat (modern, lush purple)
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/loveseat.obj";
//			materials.put(  "fabric" , new Material( 0xFF4000FF ) );

// T office chair
//			final double   unit      = Java3dModel.CM;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/office_chair.obj";
//			materials.put(  "surf1" , new Material( 0xFFC0C8C4 ) ); // stand
//			materials.put(  "surf2" , new Material( 0xFF7BA598 ) ); // fabric
//			materials.put(  "surf3" , new Material( 0xFF303020 ) ); // bottom surface

// T piano chair
//			final double   unit      = Java3dModel.M / 10.0;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/piano_chair.obj";
//			materials.put(  "fabric" , new Material( 0xFFD8D0C0 ) );

// T table
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/table.obj";
//			materials.put( "default" , new Material( 0xFF603820 ) );

// ax
//			final double   unit      = Java3dModel.M;
//			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
//			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/ax.obj";
//			materials.put(  "steel" , new Material( 0xFFC0C0A8 ) );
//			materials.put(  "wood"  , new Material( 0xFF502810 ) );

// grand piano
			final double   unit      = Java3dModel.M;
			final Matrix3D transform = Matrix3D.INIT.rotateX( Math.toRadians( 90.0 ) );
			final String   path      = "/numdata/3d/obj-from-web/lynnart.net/grand_piano.obj";
			materials.put(  "Strings"      , new Material( 0xFFA0A0A8 ) );
			materials.put(  "BlackKeys"    , new Material( 0xFF101010 ) );
			materials.put(  "WhiteKeys"    , new Material( 0xFFF0E8E0 ) );
			materials.put(  "Piano_Case_1" , new Material( 0xFF101010 ) );
			materials.put(  "Piano_Case_2" , new Material( 0xFF101010 ) );
			materials.put(  "Piano_Case3"  , new Material( 0xFF101010 ) );
			materials.put(  "piano_wheels" , new Material( 0xFF202020 ) );
			materials.put(  "Piano_Harp"   , new Material( 0xFF202010 ) );
			materials.put(  "Piano_Metal"  , new Material( 0xFF282810 ) );
			materials.put(  "Wood"         , new Material( 0xFF502810 ) );

			final FileReader fr = new FileReader( path );
			try
			{
				final Object3D object3d = ObjLoader.load( transform , materials , new BufferedReader( fr ) , null );
				final Bounds3D bounds   = object3d.getBounds( null , null );
				final Vector3D size     = bounds.size();
				final double   toCM     = 100.0 * unit;

				System.out.println( "object size = " + Math.round( toCM * size.x ) + " x " + Math.round( toCM * size.y ) + " x " + Math.round( toCM * size.z ) + " cm" );

				final ViewModel viewModel = new Java3dModel( unit , Color.WHITE ); // new Color( 51 , 77 , 102 ) );
				ViewModelTools.addLegacyLights( viewModel );
				viewModel.createNode( "obj" , Matrix3D.INIT.plus( 0.0 , 0.0 , -bounds.v1.z ) , object3d , null , 1.0f );

				final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
				final Vector3D  viewAt   = Vector3D.INIT.set( 0.0 , 0.0 , bounds.v2.z / 2.0 );

				final ViewModelView view = viewModel.createView( "view" );
				view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );

				final JPanel viewPanel = viewModel.createViewPanel( new Locale( "nl" ) , "view" );

				final JFrame frame = WindowTools.createFrame( viewModel.getClass() + " example" , 800 , 600 , viewPanel );
				frame.setVisible( true );
			}
			finally
			{
				fr.close();
			}
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
