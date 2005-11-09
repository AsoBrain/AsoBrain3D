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
package ab.j3d.view;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Object3D;

import com.numdata.oss.ui.WindowTools;

/**
 * Base implementation for view model examples.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ViewModelExample
{
	/**
	 * Construct example application.
	 */
	protected ViewModelExample( final ViewModel viewModel )
	{
		final Object3D cube = createCube( 100.0 );
		cube.setTag( "Cube 1" );
		viewModel.createNode( "cube" , Matrix3D.INIT , cube , null , 1.0f );

		final Object3D cubeLeft = createCube( 75.0 );
		cubeLeft.setTag( "Cube left");
		viewModel.createNode( "cubeLeft" , Matrix3D.getTransform( 0, 225, 90, -250, 50, 0) , cubeLeft , null , 1.0f );

		final Object3D cubeRight = createCube( 50.0 );
		cubeRight.setTag( "Cube right");
		viewModel.createNode( "cubeRight" , Matrix3D.getTransform( 90, 0, 315, 225, 0, 0) , cubeRight , null , 1.0f );

		final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D  viewAt   = Vector3D.INIT;

		viewModel.createView( "view" , new FromToViewControl( viewFrom , viewAt ) );
		ViewModelView view = viewModel.getView( "view" );

		final JFrame frame = WindowTools.createFrame( viewModel.getClass() + " example" , 800 , 600 , view.getComponent() );
		frame.setVisible( true );

		/*if ( view.hasInputTranslator() )
		{
			SceneInputTranslator translator = view.getInputTranslator();
			translator.getEventQueue().addControl( new Control() {

				public ControlEvent handleEvent( ControlEvent e )
				{
					if ( e instanceof MouseControlEvent )
					{
						MouseControlEvent event = (MouseControlEvent)e;
						List faces = event.getFacesClicked();
						String string = faces.size() + " faces under the mouse: ";
						for ( int i = 0; i < faces.size(); i++ )
						{
							Face3D face = (Face3D)faces.get( i );
							string += "  Face of " + face.getObject().getTag();
						}
						System.out.println( string );
					}
					//System.out.println( "\n" + e.toString() );
					return e;
				}
			});
		}*/

		if ( viewModel.supportsSelection() )
		{
			viewModel.addSelectionListener( new SelectionListener() {

				public void selectionChanged( SelectionChangeEvent e )
				{
					List selected = new LinkedList(e.getSelection());

					String string = selected.size() + " object selected. ID's: ";
					for ( int i = 0; i < selected.size(); i++ )
					{
						Object id = (Object)selected.get( i );
						string += "  "  + id;
					}
					System.out.println( string );
				}
			});
		}

	}

	/**
	 * Create a reference 'color cube' based on the 'ColorCube' in Java 3D.
	 * <p />
	 * The cube will be centered around the origin and range from coordinates
	 * <code>(-size,-size,-size)</code> to <code>(size,size,size)</code>; so the
	 * cube is twice the <code>size</code> argument in each axis direction.
	 * <p />
	 * The color assignments are:
	 * <table>
	 *   <tr><th>Side  </th><th>View direction</th><th>Color  </th></tr>
	 *   <tr><td>top   </td><td>Z-            </td><td>red    </td></tr>
	 *   <tr><td>bottom</td><td>Z+            </td><td>green  </td></tr>
	 *   <tr><td>front </td><td>Y+            </td><td>cyan   </td></tr>
	 *   <tr><td>back  </td><td>Y-            </td><td>magenta</td></tr>
	 *   <tr><td>left  </td><td>X+            </td><td>yellow </td></tr>
	 *   <tr><td>right </td><td>X-            </td><td>blue   </td></tr>
	 * </table>
	 *
	 * @param   size    Half edge size of cube.
	 *
	 * @return  Cube 3D object.
	 */
	public static Object3D createCube( final double size )
	{
		final Vector3D lfb = Vector3D.INIT.set( -size , -size , -size );
		final Vector3D rfb = Vector3D.INIT.set(  size , -size , -size );
		final Vector3D rbb = Vector3D.INIT.set(  size ,  size , -size );
		final Vector3D lbb = Vector3D.INIT.set( -size ,  size , -size );
		final Vector3D lft = Vector3D.INIT.set( -size , -size ,  size );
		final Vector3D rft = Vector3D.INIT.set(  size , -size ,  size );
		final Vector3D rbt = Vector3D.INIT.set(  size ,  size ,  size );
		final Vector3D lbt = Vector3D.INIT.set( -size ,  size ,  size );

		final TextureSpec red     = new TextureSpec( Color.red     );
		final TextureSpec magenta = new TextureSpec( Color.magenta );
		final TextureSpec blue    = new TextureSpec( Color.blue    );
		final TextureSpec cyan    = new TextureSpec( Color.cyan    );
		final TextureSpec green   = new TextureSpec( Color.green   );
		final TextureSpec yellow  = new TextureSpec( Color.yellow  );

		final Object3D cube = new Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , red     , false , false ); // Z =  size
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , green   , false , false ); // Z = -size
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , cyan    , false , false ); // Y = -size
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , magenta , false , false ); // Y =  size
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , yellow  , false , false ); // X = -size
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , blue    , false , false ); // X =  size

		return cube;
	}
}
