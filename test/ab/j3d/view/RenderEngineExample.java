/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2009 Numdata BV
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.control.MouseControl;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Scene;
import ab.j3d.model.ContentNode;
import ab.j3d.model.SkyBox3D;
import ab.j3d.view.control.planar.PlaneControl;
import ab.j3d.view.control.planar.PlaneMoveControl;

import com.numdata.oss.TextTools;
import com.numdata.oss.ui.WindowTools;

/**
 * Base implementation for render engine examples.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class RenderEngineExample
{
	/**
	 * Construct example application.
	 *
	 * @param   renderEngine   Render engine used to create example.
	 */
	protected RenderEngineExample( final RenderEngine renderEngine )
	{
		final double unit = Scene.MM;

		final Scene scene = new Scene( unit );
		Scene.addLegacyLights( scene );

//		final Object3D plane1 = createPlane( 0.2 / unit );
//		plane1.setTag( "Plane 1" );
//		Matrix3D transform1 = Matrix3D.getTransform( 225, 0, 90, 0.0 , 0.175 / unit , 0.02 / unit );
//		scene.addContentNode( "plane1" , transform1 , plane1 , null , 1.0f );
//
//		final Object3D plane2 = createPlane( 0.150 / unit );
//		plane2.setTag( "Plane 2" );
//		Matrix3D transform2 = Matrix3D.getTransform( 0, 225, 90, -0.250 / unit , 0.050 / unit , 0);
//		scene.addContentNode( "plane2" , transform2 , plane2 , null , 1.0f );
//
//		final Object3D plane3 = createPlane( 0.100 / unit );
//		plane3.setTag( "Plane 3" );
//		Matrix3D transform3 = Matrix3D.getTransform( 90, 0, 315, 0.225 / unit , 0, 0);
//		scene.addContentNode( "plane3" , transform3 , plane3 , null , 1.0f );

		final Object3D cube = createCube( 0.1 / unit );
		cube.setTag( "Cube 1" );
		final ContentNode cubeNode = scene.addContentNode( "cube" , Matrix3D.INIT , cube , null , 1.0f );
		cubeNode.setPlaneControl( createPlaneControl( cubeNode.getTransform() ) );

		final Object3D cubeLeft = createCube( 0.075 / unit );
		cubeLeft.setTag( "Cube left");
		final ContentNode cubeLeftNode = scene.addContentNode( "cubeLeft" , Matrix3D.getTransform( 0.0 , 225.0 , 90.0 , -0.250 / unit , 0.050 / unit , 0.0 ) , cubeLeft , null , 1.0f );
		cubeLeftNode.setPlaneControl( createPlaneControl( cubeLeftNode.getTransform() ) );

		final Object3D cubeRight = createCube( 0.050 / unit );
		cubeRight.setTag( "Cube right");
		final ContentNode cubeRightNode = scene.addContentNode( "cubeRight" , Matrix3D.getTransform( 90.0 , 0.0 , 315.0 , 0.225 / unit , 0.0 , 0.0 ) , cubeRight , null , 1.0f );
		cubeRightNode.setPlaneControl( createPlaneControl( cubeRightNode.getTransform() ) );

		final Vector3D viewFrom = Vector3D.polarToCartesian( 1.5 / unit , -0.2 * Math.PI , 0.4 * Math.PI );
		final Vector3D viewAt   = Vector3D.INIT;

		final View3D view = renderEngine.createView( scene );
		view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );
//		view.setProjectionPolicy( Projector.PARALLEL );
		view.setGridEnabled( true );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent() , BorderLayout.CENTER );
		viewPanel.add( view.createToolBar( Locale.ENGLISH ) , BorderLayout.NORTH );

		final JFrame frame = WindowTools.createFrame( renderEngine.getClass() + " example" , 800 , 600 , viewPanel );
		frame.setVisible( true );

		final Object3D testCube = createCube(0.075 / unit);
		final ContentNode testCubeNode = scene.addContentNode( "banaan" , Matrix3D.getTransform( 190.0 , 0.0 , -315.0 , 0.525 / unit , 0.0 , 0.0 ) , testCube , null , 1.0f );
		testCubeNode.setPlaneControl( createPlaneControl( testCubeNode.getTransform() ) );
/*

		//Image cache test
		for(int i = 0; i < 2000; i++)
		{
			try
			{
				Thread.sleep(10);
				System.out.println("Plaatje " + i);
				System.out.println( Runtime.getRuntime().freeMemory() + " vrij geheugen" );
				Material red     = new Material( Color.WHITE    .getRGB() );
				red.colorMap="test-"+(10000+i);
				testCube.getFace(0).setMaterial(red);
				view.update();
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}

		}
*/

		final TextOverlay clicked = new TextOverlay();
		clicked.setText( "Click on an object to change this text." );
		view.addOverlay( clicked );

		view.insertControl( new MouseControl()
			{
				public EventObject mouseClicked( final ControlInputEvent event )
				{
					final List<Face3DIntersection> objects = event.getIntersections();

					final StringBuilder sb = new StringBuilder();
					sb.append( objects.size() );
					sb.append( " objects under the mouse: " );
					for ( final Face3DIntersection object : objects )
					{
						sb.append( "  Object: " );
						sb.append( object.getObjectID() );
					}

					System.out.println( sb.toString() );

					clicked.setText( sb.toString() );

					view.update();
					return event;
				}
			} );

		final Component component = view.getComponent();
		component.addMouseListener( new MouseAdapter()
		{
			/**
			 * Saved rendering policy setting.
			 */
			private RenderingPolicy _oldRenderingPolicy = null;

			public void mousePressed( final MouseEvent e )
			{
				if ( SwingUtilities.isRightMouseButton( e ) )
				{
					_oldRenderingPolicy = view.getRenderingPolicy();
					view.setRenderingPolicy( RenderingPolicy.WIREFRAME );
				}
			}

			public void mouseReleased( final MouseEvent e )
			{
				if ( SwingUtilities.isRightMouseButton( e ) )
				{
					view.setRenderingPolicy( _oldRenderingPolicy );
				}
			}
		} );

		scene.addContentNode( "skybox" , Matrix3D.INIT , createSkyBox() , null , 1.0f );
	}

	/**
	 * Create sky box for examples.
	 *
	 * @return  Sky box object.
	 */
	public static SkyBox3D createSkyBox()
	{
		final Material north   = new Material( Color.LIGHT_GRAY.getRGB() );
		final Material east    = new Material( Color.GRAY      .getRGB() );
		final Material south   = new Material( Color.DARK_GRAY .getRGB() );
		final Material west    = new Material( Color.GRAY      .getRGB() );
		final Material ceiling = new Material( 0xffc0e0ff ); // 'sky blue'
		final Material floor   = new Material( 0xff806040 ); // 'dirt brown'

		return new SkyBox3D( north , east , south , west , ceiling , floor );
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
		final double min = -size;
		final double max =  size;

		final Vector3D lfb = Vector3D.INIT.set( min , min , min );
		final Vector3D rfb = Vector3D.INIT.set( max , min , min );
		final Vector3D rbb = Vector3D.INIT.set( max , max , min );
		final Vector3D lbb = Vector3D.INIT.set( min , max , min );
		final Vector3D lft = Vector3D.INIT.set( min , min , max );
		final Vector3D rft = Vector3D.INIT.set( max , min , max );
		final Vector3D rbt = Vector3D.INIT.set( max , max , max );
		final Vector3D lbt = Vector3D.INIT.set( min , max , max );

		final Material red     = new Material( Color.RED    .getRGB() );
		final Material magenta = new Material( Color.MAGENTA.getRGB() );
		final Material blue    = new Material( Color.BLUE   .getRGB() );
		final Material cyan    = new Material( Color.CYAN   .getRGB() );
		final Material green   = new Material( Color.GREEN  .getRGB() );
		final Material yellow  = new Material( Color.YELLOW .getRGB() );


		final float []  textureU = { 0.0f , 1.0f , 1.0f , 0.0f };
		final float []  textureV = { 0.0f , 0.0f , 1.0f , 1.0f };

		final Object3D cube = new Object3D();
		cube.fillColor = Color.BLUE;
		cube.outlineColor = Color.BLACK;
		cube.alternateFillColor = Color.GREEN;
		cube.alternateOutlineColor = Color.BLACK;
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , red     , textureU , textureV , 1.0f , false , false ); // Z =  size
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , green   , textureU , textureV , 1.0f , false , false ); // Z = -size
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , cyan    , textureU , textureV , 1.0f , false , false ); // Y = -size
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , magenta , textureU , textureV , 1.0f , false , false ); // Y =  size
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , yellow  , textureU , textureV , 1.0f , false , false ); // X = -size
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , blue    , textureU , textureV , 1.0f , false , false ); // X =  size

		return cube;
	}

	/**
	 * Create a control for a node that highlights the node and shows a grid.
	 *
	 * @param   plane2wcs   Transformation from drag plane to WCS.
	 *
	 * @return  Plane control.
	 */
	public static PlaneControl createPlaneControl( final Matrix3D plane2wcs )
	{
		return new PlaneMoveControl( plane2wcs , true )
			{
				public boolean mousePressed( final ControlInputEvent event , final ContentNode contentNode , final Vector3D wcsPoint )
				{
					final boolean result = super.mousePressed( event , contentNode , wcsPoint );

					if ( result )
					{
						final ViewControlInput controlInput = (ViewControlInput)event.getSource();
						final View3D view = controlInput.getView();

						final Matrix3D plane2wcs  = getPlane2Wcs();
						final Matrix3D node2world = contentNode.getTransform();
						final Matrix3D grid2wcs   = plane2wcs.setTranslation( node2world.xo , node2world.yo , node2world.zo );

						view.setGrid2wcs( grid2wcs );
						contentNode.setAlternate( true );
					}

					return result;
				}

				public void mouseReleased( final ControlInputEvent event , final ContentNode contentNode , final Vector3D wcsPoint )
				{
					super.mouseReleased( event , contentNode , wcsPoint );

					final ViewControlInput controlInput = (ViewControlInput)event.getSource();
					final View3D view = controlInput.getView();

					view.setGrid2wcs( Matrix3D.INIT );
					contentNode.setAlternate( false );
				}
			};
	}

	/**
	 * Paints a semi-transparent bar with text on it as overlay.
	 */
	private static class TextOverlay
		implements ViewOverlay
	{
		/**
		 * Text.
		 */
		private String _text;

		/**
		 * Construct overlay painter.
		 */
		private TextOverlay()
		{
			_text = null;
		}

		/**
		 * Set text.
		 *
		 * @param   text    Text to paint.
		 */
		public void setText( final String text )
		{
			_text = text;
		}

		public void addView( final View3D view )
		{
		}

		public void removeView( final View3D view )
		{
		}

		public void paintOverlay( final View3D view , final Graphics2D g2 )
		{
			if ( TextTools.isNonEmpty( _text ) )
			{
				final FontMetrics metrics = g2.getFontMetrics();
				final Component component = view.getComponent();
				final int y = component.getHeight() * 3 / 4;
				g2.setColor( new Color( 0x80000000 , true ) );
				g2.fillRect( 0 , y - metrics.getHeight() / 2 , component.getWidth() , 2 * metrics.getHeight() );
				g2.setColor( Color.WHITE );
				g2.drawString( _text , 50 + metrics.getLeading() , y + metrics.getLeading() + metrics.getAscent() );
			}
		}
	}
}