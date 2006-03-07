/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2006 Numdata BV
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
import java.util.List;
import javax.swing.JFrame;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.control.SceneInputTranslator;
import ab.j3d.control.MouseControlEvent;
import ab.j3d.control.ControlEvent;
import ab.j3d.control.Control;
import ab.j3d.control.Intersection;
import ab.j3d.control.ControlEventQueue;
import ab.j3d.model.Object3D;

import com.numdata.oss.ui.WindowTools;

/**
 * Base implementation for view model examples.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class ViewModelExample
{
	/**
	 * Construct example application.
	 *
	 * @param   viewModel   View model used to create example.
	 */
	protected ViewModelExample( final ViewModel viewModel )
	{
		final double unit = viewModel.getUnit();

//		final Object3D plane1 = createPlane( 0.2 / unit );
//		plane1.setTag( "Plane 1" );
//		Matrix3D transform1 = Matrix3D.getTransform( 225, 0, 90, 0.0 , 0.175 / unit , 0.02 / unit );
//		viewModel.createNode( "plane1" , transform1 , plane1 , null , 1.0f );
//
//		final Object3D plane2 = createPlane( 0.150 / unit );
//		plane2.setTag( "Plane 2" );
//		Matrix3D transform2 = Matrix3D.getTransform( 0, 225, 90, -0.250 / unit , 0.050 / unit , 0);
//		viewModel.createNode( "plane2" , transform2 , plane2 , null , 1.0f );
//
//		final Object3D plane3 = createPlane( 0.100 / unit );
//		plane3.setTag( "Plane 3" );
//		Matrix3D transform3 = Matrix3D.getTransform( 90, 0, 315, 0.225 / unit , 0, 0);
//		viewModel.createNode( "plane3" , transform3 , plane3 , null , 1.0f );


		final Object3D cube = createCube( 0.1 / unit );
		cube.setTag( "Cube 1" );
		viewModel.createNode( "cube" , Matrix3D.INIT , cube , null , 1.0f );

		final Object3D cubeLeft = createCube( 0.075 / unit );
		cubeLeft.setTag( "Cube left");
		viewModel.createNode( "cubeLeft" , Matrix3D.getTransform( 0.0 , 225.0 , 90.0 , -0.250 / unit , 0.050 / unit , 0.0 ) , cubeLeft , null , 1.0f );

		final Object3D cubeRight = createCube( 0.050 / unit );
		cubeRight.setTag( "Cube right");
		viewModel.createNode( "cubeRight" , Matrix3D.getTransform( 90.0 , 0.0 , 315.0 , 0.225 / unit , 0.0 , 0.0 ) , cubeRight , null , 1.0f );

		final Vector3D  viewFrom = Vector3D.INIT.set( 0.0 , -1.0 / unit , 0.0 );
		final Vector3D  viewAt   = Vector3D.INIT;

		viewModel.createView( "view" , new FromToViewControl( viewFrom , viewAt ) );
		final ViewModelView view = viewModel.getView( "view" );
		view.setProjectionPolicy( Projector.PARALLEL );

		final JFrame frame = WindowTools.createFrame( viewModel.getClass() + " example" , 800 , 600 , view.getComponent() );
		frame.setVisible( true );

		final SceneInputTranslator inputTranslator = view.getInputTranslator();
		if ( inputTranslator != null )
		{
			final ControlEventQueue eventQueue = inputTranslator.getEventQueue();
			eventQueue.addControl( new Control() {

					public ControlEvent handleEvent( final ControlEvent e )
					{
						if ( e instanceof MouseControlEvent )
						{
							final MouseControlEvent event = (MouseControlEvent)e;
							if ( MouseControlEvent.MOUSE_PRESSED == event.getType() )
							{
								final List objects = event.getIntersections();
								final StringBuffer string = new StringBuffer();
								string.append( objects.size() );
								string.append( " objects under the mouse: " );
								for ( int i = 0 ; i < objects.size() ; i++ )
								{
									string.append( "  Object: " );
									string.append( ((Intersection)objects.get( i )).getID() );
								}
								System.out.println( string.toString() );
							}
						}
						return e;
					}

					public int getDataRequiredMask()
					{
						return 0;
					}
				} );
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

	public static Object3D createPlane( final double size )
	{
		final Vector3D lf = Vector3D.INIT.set( -size , -size , 0.0 );
		final Vector3D rf = Vector3D.INIT.set(  size , -size , 0.0 );
		final Vector3D rb = Vector3D.INIT.set(  size ,  size , 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -size ,  size , 0.0 );

		final TextureSpec red     = new TextureSpec( Color.red   );
		final TextureSpec green   = new TextureSpec( Color.green );

		final Object3D plane = new Object3D();
		/* top    */ plane.addFace( new Vector3D[] { lf , lb , rb , rf } , red   , false , false ); // Z =  size
		/* bottom */ plane.addFace( new Vector3D[] { lb , lf , rf , rb } , green , false , false ); // Z = -size

		return plane;
	}
}
