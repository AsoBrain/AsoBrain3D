/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Box3D;
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
		viewModel.createNode( "cube" , cube );

		final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , -800.0 , 300.0 );
		final Vector3D  viewAt   = Vector3D.INIT;
		final Component view     = viewModel.createView( "view" , viewFrom , viewAt );

		final JFrame frame = WindowTools.createFrame( "Java 3D Model Example" , 800 , 600 , view );
		frame.setVisible( true );
	}

	private static Object3D createCube( final double size )
	{
		final TextureSpec texture = new TextureSpec();
		texture.code = "green";
		texture.rgb  = Color.green.getRGB();

		return new Box3D( Matrix3D.INIT.minus( size , size , size ) , size * 2.0 , size * 2.0 , size * 2.0 , texture , texture );
	}
}
