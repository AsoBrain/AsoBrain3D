/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
import java.util.List;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.control.planar.*;

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
		final ContentNode cubeNode = scene.addContentNode( "cube", Matrix3D.IDENTITY, cube );
		cubeNode.setPlaneControl( createPlaneControl( cubeNode.getTransform() ) );

		final Object3D cubeLeft = createCube( 0.075 / unit );
		cubeLeft.setTag( "Cube left");
		final ContentNode cubeLeftNode = scene.addContentNode( "cubeLeft", Matrix3D.getTransform( 0.0, 225.0, 90.0, -0.250 / unit, 0.050 / unit, 0.0 ), cubeLeft );
		cubeLeftNode.setPlaneControl( createPlaneControl( cubeLeftNode.getTransform() ) );

		final BasicAppearance sphereAppearance = BasicAppearance.createForColor( new Color4f( 0.0f, 1.0f, 1.0f, 075f ) );
		sphereAppearance.setReflectionMap( new SingleImageCubeMap( getClass().getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		sphereAppearance.setReflectionMin( 0.2f );
		sphereAppearance.setReflectionMax( 0.8f );
		final Object3D sphere = new Sphere3D( 0.1 / unit, 20, 20, sphereAppearance );
		sphere.setTag( "Sphere" );
		final ContentNode sphereNode = scene.addContentNode( "shere", Matrix3D.getTransform( 90.0, 0.0, 315.0, 0.225 / unit, 0.0, 0.0 ), sphere );
		sphereNode.setPlaneControl( createPlaneControl( sphereNode.getTransform() ) );

		addMaterialCubes( scene );

		final Vector3D viewFrom = Vector3D.polarToCartesian( 1.5 / unit, -0.2 * Math.PI, 0.4 * Math.PI );
		final Vector3D viewAt   = Vector3D.ZERO;

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
		viewPanel.add( View3DPanel.createToolBar( view, Locale.ENGLISH ), BorderLayout.NORTH );

		final JFrame frame = new JFrame( renderEngine.getClass() + " example" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane( viewPanel );
		final Toolkit toolkit = frame.getToolkit();
		frame.setSize( 800, 600 );

		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		if ( graphicsConfiguration != null )
		{
			final Rectangle screenBounds = graphicsConfiguration.getBounds();
			final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
			frame.setLocation( screenBounds.x + ( screenBounds.width + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2,
			                   screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );
		}
		else
		{
			final Dimension screenSize = toolkit.getScreenSize();
			frame.setLocation( ( screenSize.width - frame.getWidth() ) / 2,
			                   ( screenSize.height - 5 - frame.getHeight() ) / 2 );
		}

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
				Appearance red     = new Appearance( Color.WHITE    .getRGB() );
				red.colorMap="test-"+(10000+i);
				testCube.getFace(0).setAppearance(red);
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

		final ViewControlInput controlInput = view.getControlInput();
		controlInput.insertControlInputListener( new MouseControl()
		{
			@Override
			public void mouseClicked( final ControlInputEvent event )
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

		scene.addContentNode( "skybox", Matrix3D.IDENTITY, createSkyBox() );
	}

	/**
	 * Create sky box for examples.
	 *
	 * @return  Sky box object.
	 */
	public static SkyBox3D createSkyBox()
	{
		final Appearance north   = BasicAppearance.createForColor( Color4.LIGHT_GRAY );
		final Appearance east    = BasicAppearance.createForColor( Color4.GRAY );
		final Appearance south   = BasicAppearance.createForColor( Color4.DARK_GRAY );
		final Appearance west    = BasicAppearance.createForColor( Color4.GRAY );
		final Appearance ceiling = BasicAppearance.createForColor( new Color4f( 0xffc0e0ff ) ); // 'sky blue'
		final Appearance floor   = BasicAppearance.createForColor( new Color4f( 0xff806040 ) ); // 'dirt brown'

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

		final Vector3D lfb = new Vector3D( min, min, min );
		final Vector3D rfb = new Vector3D( max, min, min );
		final Vector3D rbb = new Vector3D( max, max, min );
		final Vector3D lbb = new Vector3D( min, max, min );
		final Vector3D lft = new Vector3D( min, min, max );
		final Vector3D rft = new Vector3D( max, min, max );
		final Vector3D rbt = new Vector3D( max, max, max );
		final Vector3D lbt = new Vector3D( min, max, max );

		final Appearance red     = BasicAppearance.createForColor( new Color4f( 0xC0FF0000 ) );
		final Appearance magenta = BasicAppearance.createForColor( new Color4f( 0xC0FF00FF ) );
		final Appearance blue    = BasicAppearance.createForColor( new Color4f( 0xC00000FF ) );
		final Appearance cyan    = BasicAppearance.createForColor( new Color4f( 0xC000FFFF ) );
		final Appearance green   = BasicAppearance.createForColor( new Color4f( 0xC000FF00 ) );
		final Appearance yellow  = BasicAppearance.createForColor( new Color4f( 0xC0FFFF00 ) );

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
					grid.setGrid2wcs( Matrix3D.IDENTITY );
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
			final String text = _text;
			if ( ( text != null ) && !text.isEmpty() )
			{
				final FontMetrics metrics = g2.getFontMetrics();
				final Component component = view.getComponent();
				final int y = component.getHeight() * 3 / 4;
				g2.setColor( new Color( 0x80000000, true ) );
				g2.fillRect( 0, y - metrics.getHeight() / 2, component.getWidth(), 2 * metrics.getHeight() );
				g2.setColor( Color.WHITE );
				g2.drawString( text, 50 + metrics.getLeading(), y + metrics.getLeading() + metrics.getAscent() );
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
			"EG_F005",
			"EG_F007",
			"EG_F492",
			"EG_F268",
			"EG_F268",
			"EG_F004",
			"EG_F185",
			"EG_F150",
			"EG_F184",
			"EG_H3410",
			"EG_F125",
			"EG_F008",
			"EG_F238",
			"EG_F322",
			"EG_F338",
			"EG_F242",
			"EG_F337",
			"EG_F348",
			"EG_F151",
			"EG_F158",
			"EG_F349",
			"EG_F371",
			"EG_F339",
			"CB",
			"EG_F491",
			"EG_F143",
			"EG_F058",
			"EG_H3024",
			"EG_H3005",
			"EG_F161",
			"EG_F236",
			"EG_H3411",
			"EG_F136",
			"EG_F148",
			"EG_F163",
			"EG_H1883",
			"EG_H296",
			"EG_H2572",
			"EG_F275",
			"EG_F157",
			"EG_F026",
			"EG_H068",
			"EG_H3015",
			"EG_F254",
			"EG_H293",
			"EG_H3016",
			"EG_H3739",
			"EG_F274",
			"EG_F369",
			"EG_F145",
			"EG_H3734",
			"EG_F589",
			"EG_H3736",
			"EG_H3387",
			"EG_H3400",
			"EG_H1395",
			"EG_F270",
			"EG_F902",
			"EG_H1979",
			"EG_H1950",
			"EG_H3025",
			"EG_H3388",
			"EG_H3328",
			"EG_H1980",
			"EG_H3031",
			"EG_H178",
			"EG_H3386",
			"EG_H1932",
			"EG_H3006",
			"EG_H1425",
			"EG_H1394",
			"EG_F759",
			"EG_H1435",
			"EG_H3030",
			"EG_H1582",
			"EG_H3730",
			"EG_H1509",
			"EG_H044",
			"EG_F901",
			"EG_F333",
			"EG_F064",
			"EG_F115",
			"EG_H1920",
			"EG_F371",
			"EG_F503",
			"EG_H1453",
			"EG_H1428",
			"EG_H3389",
			"EG_H3703",
			"EG_H3316",
			"LU_NOTEN",
			"EG_H3127",
			"EG_H1381",
			"EG_H045",
			"EG_H1584",
			"EG_F554",
			"EG_H1521",
			"EG_H3363",
			"EG_F253",
			"EG_H3382",
			"EG_F757",
			"EG_F202",
			"EG_H1235",
			"EG_H1903",
			"EG_H1392",
			"EG_H3738",
			"EG_H3735",
			"IV_ivogebogen",
			"EG_F147",
			"IV_ivorecht",
			"EG_H1295",
			"EG_H046",
			"EG_H1984",
			"EG_H1555",
			"EG_H1553",
			"EG_H3721",
			"EG_F202",
			"EG_H1706",
			"EG_H1704",
			"EG_F137",
			"EG_H1032",
			"EG_H047",
			"EG_H1342",
			"EG_H1232",
			"EG_H1665",
			"EG_H1518",
			"EG_H207",
			"EG_H3713",
			"EG_H1277",
			"IV_vendome",
			"EG_F702",
			"EG_H1696",
			"IV_manoireik",
			"EG_H3362",
			"EG_H1344",
			"EG_H369",
			"EG_H1310",
			"IV_bolero",
			"EG_F900",
			"EG_H1319",
			"EG_H1516",
			"EG_H1586",
			"EG_F584",
			"EG_H3802",
			"EG_H1874",
			"CB_side",
			"EG_H1664",
			"EG_F065",
			"EG_H1873",
			"EG_H1513",
			"EG_H1532",
			"EG_H1354",
			"EG_H1879",
			"EG_H1887",
			"EG_H1550",
			"IV_louis15",
			"EG_F621",
			"EG_H199",
			"EG_F622",
			"EG_F364",
			"EG_H1642",
			"EG_H1954",
			"EG_F042",
			"EG_H1511",
			"EG_H1512",
			"wall_stuc",
			"EG_F583",
			"EG_F518",
			"EG_H1893",
			"EG_F581",
			"EG_H1599",
			"EG_H1637",
			"EG_H1554",
			"EG_H1692",
			"EG_H1951",
			"EG_H1727",
			"EG_F124",
			"EG_F797",
			"EG_H1946",
			"EG_H1867",
			"EG_H1705",
			"EG_H1861",
			"EG_F699",
			"EG_H1869",
			"EG_H1703",
			"EG_H1774",
			"EG_H1530",
			"EG_F040",
			"EG_H1832",
			"IV_verona",
			"EG_H1968",
			"EG_H1348",
			"EG_H1731",
			"EG_F104",
			"EG_H1424",
			"EG_H1502",
			"EG_H1775",
			"EG_F550",
			"EG_FA114",
			"IV_zurich",
			"EG_H1137",
			"EG_H1643",
			"EG_F620",
			"EG_H1462",
			"EG_H1817",
			"EG_H1801",
			"EG_H1531",
			"EG_H1686",
			"EG_FA102",
			"EG_H1698",
			"EG_H3129",
			"EG_FA115",
			"EG_H1862",
			"EG_H1748",
			"EG_H1284",
			"EG_H1834",
			"EG_F501",
			"MDF",
			"EG_FA117",
			"EG_F558",
			"EG_F752",
			"IV_ravel",
			"IV_lugano",
			"EG_F796",
			"IV_zianoboog",
			"IV_dublin",
			"IV_beukrood",
			"EG_F516",
			"EG_H1538",
			"EG_H1699",
			"EG_H3128",
			"EG_F039",
			"IV_palazzo",
			"EG_F483",
			"EG_F131",
			"EG_F784",
			"EG_F697",
			"EG_H1641",
			"IV_sevilla",
			"EG_H1733",
			"EG_F132",
			"EG_H1952",
			"EG_FA101",
			"EG_F698",
			"EG_F798",
			"EG_F306",
			"EG_F753",
			"EG_F799",
			"EG_H3704",
			"EG_F509",
			"MPX",
			"EG_H3304",
			"EG_F755",
			"EG_F309",
			"EG_H1709",
			"EG_F785",
			"EG_F696",
			"EG_F751",
			"EG_F748",
			"IV_dover",
			"EG_H3306",
			"EG_H1334",
			"EG_F107",
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
			"EG_F306",
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
					createAppearance( textures[ i * 6 ] ),
					createAppearance( textures[ i * 6 + 1 ] ),
					createAppearance( textures[ i * 6 + 2 ] ),
					createAppearance( textures[ i * 6 + 3 ] ),
					createAppearance( textures[ i * 6 + 4 ] ),
					createAppearance( textures[ i * 6 + 5 ] ) );

			final Matrix3D matrix = Matrix3D.getTranslation( 1.1 * boxSize * (double) ( i % 10 - 5 ), 100.0, 1.1 * boxSize * (double) ( i / 10 ) );

			scene.addContentNode( "texture-box-" + i, matrix, box );
		}
	}

	/**
	 * Creates a appearance with the given color map.
	 *
	 * @param   texture     Color map to be used.
	 *
	 * @return  Created appearance.
	 */
	private static Appearance createAppearance( final String texture )
	{
		final BasicAppearance result = new BasicAppearance();
		result.setAmbientColor( new Color4f( 0.3f, 0.3f, 0.3f ) );
		result.setDiffuseColor( Color4.WHITE );
		result.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		result.setShininess( 32 );
		result.setColorMap( new FileTextureMap( RenderEngineExample.class.getResource( "decors/" + texture + ".jpg" ), 1.0f, 1.0f ) );
		return result;
	}
}
