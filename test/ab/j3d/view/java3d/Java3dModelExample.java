/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2005 Numdata BV
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
package ab.j3d.view.java3d;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JFrame;

import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;

import com.numdata.oss.ui.WindowTools;

/**
 * Example program for the Java 3D view model implementation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Java3dModelExample
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Java3dModelExample()
	{
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final ViewModel viewModel = new Java3dModel();

		final Object3D cube = createCube( 100.0 );
		viewModel.createNode( "cube" , cube , null , 1.0f );

		final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , -800.0 , 0.0 );
		final Vector3D  viewAt   = Vector3D.INIT;
		final Component view     = viewModel.createView( "view" , viewFrom , viewAt );

		final JFrame frame = WindowTools.createFrame( "Java 3D Model Example" , 800 , 600 , view );
		frame.setVisible( true );
	}

	private static Object3D createCube( final double size )
	{
		final Vector3D lfb = Vector3D.INIT.set( -size , -size , -size );
		final Vector3D rfb = Vector3D.INIT.set(  size , -size , -size );
		final Vector3D rbb = Vector3D.INIT.set(  size ,  size , -size );
		final Vector3D lbb = Vector3D.INIT.set( -size ,  size , -size );
		final Vector3D lft = Vector3D.INIT.set( -size , -size ,  size );
		final Vector3D rft = Vector3D.INIT.set(  size , -size ,  size );
		final Vector3D rbt = Vector3D.INIT.set(  size ,  size ,  size );
		final Vector3D lbt = Vector3D.INIT.set( -size ,  size ,  size );

		final TextureSpec red = new TextureSpec();
		red.code = "red";
		red.rgb  = Color.red.getRGB();

		final TextureSpec magenta = new TextureSpec();
		magenta.code = "magenta";
		magenta.rgb  = Color.magenta.getRGB();

		final TextureSpec blue = new TextureSpec();
		blue.code = "blue";
		blue.rgb  = Color.blue.getRGB();

		final TextureSpec cyan = new TextureSpec();
		cyan.code = "cyan";
		cyan.rgb  = Color.cyan.getRGB();

		final TextureSpec green = new TextureSpec();
		green.code = "green";
		green.rgb  = Color.green.getRGB();

		final TextureSpec yellow = new TextureSpec();
		yellow.code = "yellow";
		yellow.rgb  = Color.yellow.getRGB();

		final Object3D cube = new Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , red     , false ); // Z =  size
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , green   , false ); // Z = -size
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , cyan    , false ); // Y = -size
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , magenta , false ); // Y =  size
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , yellow  , false ); // X = -size
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , blue    , false ); // X =  size

		return cube;
	}
}
