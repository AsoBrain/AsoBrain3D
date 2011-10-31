/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.control.planar.*;
import com.numdata.oss.*;
import com.numdata.oss.ui.*;

/**
 * Base implementation for render engine examples.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderEngineExample
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
//		Matrix3D transform1 = Matrix3D.getTransform( 225, 0, 90, 0.0, 0.175 / unit, 0.02 / unit );
//		scene.addContentNode( "plane1", transform1, plane1, null, 1.0f );
//
//		final Object3D plane2 = createPlane( 0.150 / unit );
//		plane2.setTag( "Plane 2" );
//		Matrix3D transform2 = Matrix3D.getTransform( 0, 225, 90, -0.250 / unit, 0.050 / unit, 0);
//		scene.addContentNode( "plane2", transform2, plane2, null, 1.0f );
//
//		final Object3D plane3 = createPlane( 0.100 / unit );
//		plane3.setTag( "Plane 3" );
//		Matrix3D transform3 = Matrix3D.getTransform( 90, 0, 315, 0.225 / unit, 0, 0);
//		scene.addContentNode( "plane3", transform3, plane3, null, 1.0f );

		final Object3D cube = createCube( 0.1 / unit );
		cube.setTag( "Cube 1" );
		final ContentNode cubeNode = scene.addContentNode( "cube", Matrix3D.INIT, cube );
		cubeNode.setPlaneControl( createPlaneControl( cubeNode.getTransform() ) );

		final Object3D cubeLeft = createCube( 0.075 / unit );
		cubeLeft.setTag( "Cube left");
		final ContentNode cubeLeftNode = scene.addContentNode( "cubeLeft", Matrix3D.getTransform( 0.0, 225.0, 90.0, -0.250 / unit, 0.050 / unit, 0.0 ), cubeLeft );
		cubeLeftNode.setPlaneControl( createPlaneControl( cubeLeftNode.getTransform() ) );

		final Object3D sphere = new Sphere3D( 0.1 / unit, 20, 20, new Material( 0xC000FFFF ) );
		sphere.setTag( "Sphere" );
		final ContentNode sphereNode = scene.addContentNode( "shere", Matrix3D.getTransform( 90.0, 0.0, 315.0, 0.225 / unit, 0.0, 0.0 ), sphere );
		sphereNode.setPlaneControl( createPlaneControl( sphereNode.getTransform() ) );

		addMaterialCubes( scene );

		final Vector3D viewFrom = Vector3D.polarToCartesian( 1.5 / unit, -0.2 * Math.PI, 0.4 * Math.PI );
		final Vector3D viewAt   = Vector3D.INIT;

		final View3D view = renderEngine.createView( scene );
		view.setCameraControl( new FromToCameraControl( view, viewFrom, viewAt )
		{
			@Override
			protected boolean isDragFromAroundToEvent( final ControlInputEvent event )
			{
				return event.isMouseButton1Down();
			}
		} );
//		view.setProjectionPolicy( Projector.PARALLEL );

		view.setBackground( Background.createGradient( new Color4f(192, 192, 192), new Color4f(64, 64, 64), new Color4f( 255, 255, 255 ), new Color4f( 255, 255, 255 ) ) );

		final Grid grid = view.getGrid();
		grid.setEnabled( true );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent(), BorderLayout.CENTER );
		viewPanel.add( view.createToolBar( Locale.ENGLISH ), BorderLayout.NORTH );

		final JFrame frame = WindowTools.createFrame( renderEngine.getClass() + " example", 800, 600, viewPanel );
		frame.setVisible( true );

		final Object3D testCube = createCube(0.075 / unit);
		final ContentNode testCubeNode = scene.addContentNode( "banaan", Matrix3D.getTransform( 190.0, 0.0, -315.0, 0.525 / unit, 0.0, 0.0 ), testCube );
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
			@Override
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

			@Override
			public void mousePressed( final MouseEvent e )
			{
				if ( SwingUtilities.isRightMouseButton( e ) )
				{
					_oldRenderingPolicy = view.getRenderingPolicy();
					view.setRenderingPolicy( RenderingPolicy.WIREFRAME );
				}
			}

			@Override
			public void mouseReleased( final MouseEvent e )
			{
				if ( SwingUtilities.isRightMouseButton( e ) )
				{
					view.setRenderingPolicy( _oldRenderingPolicy );
				}
			}
		} );

		scene.addContentNode( "skybox", Matrix3D.INIT, createSkyBox() );
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

		return new SkyBox3D( north, east, south, west, ceiling, floor );
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

		final Vector3D lfb = Vector3D.INIT.set( min, min, min );
		final Vector3D rfb = Vector3D.INIT.set( max, min, min );
		final Vector3D rbb = Vector3D.INIT.set( max, max, min );
		final Vector3D lbb = Vector3D.INIT.set( min, max, min );
		final Vector3D lft = Vector3D.INIT.set( min, min, max );
		final Vector3D rft = Vector3D.INIT.set( max, min, max );
		final Vector3D rbt = Vector3D.INIT.set( max, max, max );
		final Vector3D lbt = Vector3D.INIT.set( min, max, max );

		final Material red     = new Material( 0xC0FF0000 );
		final Material magenta = new Material( 0xC0FF00FF );
		final Material blue    = new Material( 0xC00000FF );
		final Material cyan    = new Material( 0xC000FFFF );
		final Material green   = new Material( 0xC000FF00 );
		final Material yellow  = new Material( 0xC0FFFF00 );

		final float[] texturePoints = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };

		final Object3DBuilder builder = new Object3DBuilder();
		/* top    */ builder.addFace( new Vector3D[] { lft, lbt, rbt, rft }, red    , texturePoints, null, false, false ); // Z =  size
		/* bottom */ builder.addFace( new Vector3D[] { lbb, lfb, rfb, rbb }, green  , texturePoints, null, false, false ); // Z = -size
		/* front  */ builder.addFace( new Vector3D[] { lfb, lft, rft, rfb }, cyan   , texturePoints, null, false, false ); // Y = -size
		/* back   */ builder.addFace( new Vector3D[] { rbb, rbt, lbt, lbb }, magenta, texturePoints, null, false, false ); // Y =  size
		/* left   */ builder.addFace( new Vector3D[] { lbb, lbt, lft, lfb }, yellow , texturePoints, null, false, false ); // X = -size
		/* right  */ builder.addFace( new Vector3D[] { rfb, rft, rbt, rbb }, blue   , texturePoints, null, false, false ); // X =  size

		return builder.getObject3D();
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
		return new PlaneMoveControl( plane2wcs, true )
			{
				@Override
				public boolean mousePressed( final ControlInputEvent event, final ContentNode contentNode, final Vector3D wcsPoint )
				{
					final boolean result = super.mousePressed( event, contentNode, wcsPoint );

					if ( result )
					{
						final ViewControlInput controlInput = event.getSource();
						final View3D view = controlInput.getView();

						final Matrix3D plane2wcs  = getPlane2Wcs();
						final Matrix3D node2world = contentNode.getTransform();
						final Matrix3D grid2wcs   = plane2wcs.setTranslation( node2world.xo, node2world.yo, node2world.zo );

						final Grid grid = view.getGrid();
						grid.setGrid2wcs( grid2wcs );
						contentNode.setAlternate( true );
					}

					return result;
				}

				@Override
				public void mouseReleased( final ControlInputEvent event, final ContentNode contentNode, final Vector3D wcsPoint )
				{
					super.mouseReleased( event, contentNode, wcsPoint );

					final ViewControlInput controlInput = event.getSource();
					final View3D view = controlInput.getView();

					final Grid grid = view.getGrid();
					grid.setGrid2wcs( Matrix3D.INIT );
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

		public void paintOverlay( final View3D view, final Graphics2D g2 )
		{
			if ( TextTools.isNonEmpty( _text ) )
			{
				final FontMetrics metrics = g2.getFontMetrics();
				final Component component = view.getComponent();
				final int y = component.getHeight() * 3 / 4;
				g2.setColor( new Color( 0x80000000, true ) );
				g2.fillRect( 0, y - metrics.getHeight() / 2, component.getWidth(), 2 * metrics.getHeight() );
				g2.setColor( Color.WHITE );
				g2.drawString( _text, 50 + metrics.getLeading(), y + metrics.getLeading() + metrics.getAscent() );
			}
		}
	}

	/**
	 * Creates lots cubes with lots of materials mapped on them.
	 *
	 * @param   scene   Scene to add the cubes to.
	 */
	private static void addMaterialCubes( final Scene scene )
	{
		final String[] textures =
		{
			"EG_F005_ST72",
			"EG_F007_ST2",
			"EG_F492_ST82",
			"EG_F268_HG",
			"EG_F268_ST82",
			"EG_F004_ST72",
			"EG_F185_ST2",
			"EG_F150_ST2",
			"EG_F135_HG",
			"EG_F184_ST72",
			"EG_H3410_ST22",
			"EG_F125_ST2",
			"EG_F135_ST2",
			"EG_F008_ST72",
			"EG_F238_ST15",
			"EG_F322_ST82",
			"EG_F338_ST72",
			"EG_F242_ST2",
			"EG_F337_ST72",
			"EG_F348_ST70",
			"EG_F151_ST2",
			"EG_F158_ST15",
			"EG_F349_ST70",
			"EG_F371_HG",
			"EG_F339_ST72",
			"CB",
			"EG_F491_ST82",
			"EG_F143_ST82",
			"EG_F058_ST2",
			"EG_H3024_ST15",
			"EG_H3005_ST22",
			"EG_F161_ST70",
			"EG_F236_ST15",
			"EG_H3411_ST22",
			"EG_F136_ST2",
			"EG_F148_ST82",
			"EG_F163_ST70",
			"EG_H1883_ST15",
			"EG_H296_ST22",
			"EG_H2572_ST9",
			"EG_F275_ST9",
			"EG_F157_ST15",
			"EG_F026_ST2",
			"EG_H068_ST24",
			"EG_H3015_ST9",
			"EG_F254_ST15",
			"EG_H293_ST70",
			"EG_H3016_ST24",
			"EG_H3739_ST15",
			"EG_F274_ST9",
			"EG_F369_ST82",
			"EG_F145_ST2",
			"EG_H3734_ST9",
			"EG_F589_ST21",
			"EG_H3736_ST9",
			"EG_H3387_ST24",
			"EG_H3400_ST22",
			"EG_H1395_ST15",
			"EG_F270_ST82",
			"EG_F902_ST9",
			"EG_H1979_ST9",
			"EG_H1950_ST15",
			"EG_H3025_ST15",
			"EG_H3388_ST24",
			"EG_H3328_ST9",
			"EG_H1980_ST15",
			"EG_H3031_ST9",
			"EG_H178_ST15",
			"EG_H3386_ST24",
			"EG_H1932_ST9",
			"EG_H3006_ST22",
			"EG_H1425_ST21",
			"EG_H1394_ST9",
			"EG_F759_ST15",
			"EG_H1435_ST9",
			"EG_H3030_ST9",
			"EG_H1582_ST15",
			"EG_H3730_ST24",
			"EG_H1509_ST24",
			"EG_H044_ST15",
			"EG_F901_ST9",
			"EG_F333_ST15",
			"EG_F064_ST9",
			"EG_F115_ST2",
			"EG_H1920_ST9",
			"EG_F371_ST82",
			"EG_F503_ST2",
			"EG_H1453_ST15",
			"EG_H1428_ST22",
			"EG_H3389_ST24",
			"EG_H3703_ST15",
			"EG_H3316_ST9",
			"LU_NOTEN",
			"EG_H3127_ST9",
			"EG_H1381_ST24",
			"EG_H045_ST15",
			"EG_H1584_ST15",
			"EG_F554_ST15",
			"EG_H1521_ST15",
			"EG_H3363_ST9",
			"EG_F253_ST15",
			"EG_H3382_ST9",
			"EG_F757_ST15",
			"EG_F202_HG",
			"EG_H1235_ST9",
			"EG_H1903_ST15",
			"EG_H1392_ST15",
			"EG_H3738_ST9",
			"EG_H3735_ST9",
			"IV_ivogebogen",
			"EG_F147_ST82",
			"IV_ivorecht",
			"EG_H1295_ST15",
			"EG_H046_ST15",
			"EG_H1984_ST15",
			"EG_H1555_ST15",
			"EG_H1553_ST15",
			"EG_H3721_ST15",
			"EG_F202_ST15",
			"EG_H1706_ST15",
			"EG_H1704_ST15",
			"EG_F137_ST2",
			"EG_H1032_ST15",
			"EG_H047_ST15",
			"EG_H1342_ST24",
			"EG_H1232_ST9",
			"EG_H1665_ST15",
			"EG_H1518_ST15",
			"EG_H207_ST9",
			"EG_H3713_ST9",
			"EG_H1277_ST9",
			"IV_vendome",
			"EG_F702_ST15",
			"EG_H1696_ST15",
			"IV_manoireik",
			"EG_H3362_ST9",
			"EG_H1344_ST15",
			"EG_H369_ST15",
			"EG_H1310_ST24",
			"IV_bolero",
			"EG_F900_ST9",
			"EG_H1319_ST24",
			"EG_H1516_ST15",
			"EG_H1586_ST15",
			"EG_F584_ST22",
			"EG_H3802_ST9",
			"EG_H1874_ST15",
			"CB_side",
			"EG_H1664_ST15",
			"EG_F065_ST70",
			"EG_H1873_ST15",
			"EG_H1513_ST15",
			"EG_H1532_ST15",
			"EG_H1354_ST15",
			"EG_H1879_ST15",
			"EG_H1887_ST9",
			"EG_H1550_ST15",
			"IV_louis15",
			"EG_F621_ST15",
			"EG_H199_ST70",
			"EG_F622_ST15",
			"EG_F364_ST9",
			"EG_H1642_ST9",
			"EG_H1954_ST9",
			"EG_F042_ST70",
			"EG_H1511_ST15",
			"EG_H1512_ST15",
			"wall_stuc",
			"EG_F583_ST22",
			"EG_F518_ST2",
			"EG_H1893_ST24",
			"EG_F581_ST21",
			"EG_H1599_ST15",
			"EG_H1637_ST15",
			"EG_H1554_ST15",
			"EG_H1692_ST15",
			"EG_H1951_ST15",
			"EG_H1727_ST15",
			"EG_F124_ST70",
			"EG_F797_ST15",
			"EG_H1946_ST15",
			"EG_H1867_ST9",
			"EG_H1705_ST15",
			"EG_H1861_ST15",
			"EG_F699_ST15",
			"EG_H1869_ST9",
			"EG_H1703_ST24",
			"EG_H1774_ST15",
			"EG_H1530_ST15",
			"EG_F040_ST72",
			"EG_H1832_ST15",
			"IV_verona",
			"EG_H1968_ST9",
			"EG_H1348_ST15",
			"EG_H1731_ST24",
			"EG_F104_ST2",
			"EG_H1424_ST22",
			"EG_H1502_ST15",
			"EG_H1775_ST9",
			"EG_F550_ST15",
			"EG_FA114",
			"IV_zurich",
			"EG_H1137_ST24",
			"EG_H1643_ST15",
			"EG_F620_ST15",
			"EG_H1462_ST24",
			"EG_H1817_ST9",
			"EG_H1801_ST15",
			"EG_H1531_ST15",
			"EG_H1686_ST15",
			"EG_FA102",
			"EG_H1698_ST15",
			"EG_H3129_ST9",
			"EG_FA115",
			"EG_H1862_ST15",
			"EG_H1748_ST15",
			"EG_H1284_ST3",
			"EG_H1834_ST9",
			"EG_F501_ST2",
			"MDF",
			"EG_FA117",
			"EG_F558_ST15",
			"EG_F752_ST15",
			"IV_ravel",
			"IV_lugano",
			"EG_F796_ST15",
			"IV_zianoboog",
			"IV_dublin",
			"IV_beukrood",
			"EG_F516_ST2",
			"EG_H1538_ST15",
			"EG_H1699_ST15",
			"EG_H3128_ST15",
			"EG_F039_ST2",
			"IV_palazzo",
			"EG_F483_ST2",
			"EG_F131_ST15",
			"EG_F784_ST2",
			"EG_F697_ST15",
			"EG_H1641_ST15",
			"IV_sevilla",
			"EG_H1733_ST9",
			"EG_F132_ST15",
			"EG_H1952_ST15",
			"EG_FA101",
			"EG_F698_ST15",
			"EG_F798_ST15",
			"EG_F306_HG",
			"EG_F753_ST15",
			"EG_F799_ST15",
			"EG_H3704",
			"EG_F509_ST2",
			"MPX",
			"EG_H3304",
			"EG_F755_ST15",
			"EG_F309_ST2",
			"EG_H1709",
			"EG_F785_ST2",
			"EG_F696_ST9",
			"EG_F751_ST15",
			"EG_F748_ST15",
			"IV_dover",
			"EG_H3306",
			"EG_H1334",
			"EG_F107_ST2",
			"IV_victorianz",
			"WOOD",
			"PF_F7684",
			"IV_geneve",
			"EG_H1615",
			"PF_R5412",
			"PF_R5410",
			"PF_R5330",
			"PF_R5608",
			"PF_R5340",
			"PF_R5360",
			"PF_R5411",
			"PF_R5474",
			"PF_F7612",
			"PF_R4601",
			"PF_R4346",
			"PF_R5341",
			"PF_F7611",
			"EG_F306_ST2",
			"PF_F7614",
			"PF_F8523",
			"PF_F7615",
			"PF_F7343",
			"PF_R5401",
			"PF_R5413",
			"PF_F8110",
			"PF_R5320",
			"PF_R5402",
			"PF_F7613",
			"PF_R4400",
			"sill",
			"HB",
			"PF_R5008"
		};

		final double boxSize = 100.0;
		for ( int i = 0 ; i < Math.min( 100, textures.length ) / 6 ; i++ )
		{
			final Box3D box = new Box3D( boxSize, boxSize, boxSize,
					new BoxUVMap( 1.0 / boxSize ),
					createMaterial( textures[ i * 6     ] ),
					createMaterial( textures[ i * 6 + 1 ] ),
					createMaterial( textures[ i * 6 + 2 ] ),
					createMaterial( textures[ i * 6 + 3 ] ),
					createMaterial( textures[ i * 6 + 4 ] ),
					createMaterial( textures[ i * 6 + 5 ] ) );

			final Matrix3D matrix = Matrix3D.INIT.plus( 1.1 * boxSize * (double)( i % 10 - 5 ), 100.0, 1.1 * boxSize * (double)( i / 10 ) );

			scene.addContentNode( "texture-box-" + i, matrix, box );
		}
	}

	/**
	 * Creates a material with the given color map.
	 *
	 * @param   texture     Color map to be used.
	 *
	 * @return  Created material.
	 */
	private static Material createMaterial( final String texture )
	{
		return new Material( texture, 0.3f, 0.3f, 0.3f, 1.0f, 1.0f, 1.0f, 1.0f, 0.2f, 0.2f, 0.2f, 32, 0.0f, 0.0f, 0.0f, texture, 1.0f, 1.0f, true );
	}
}
