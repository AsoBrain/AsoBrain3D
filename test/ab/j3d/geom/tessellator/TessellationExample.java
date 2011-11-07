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
package ab.j3d.geom.tessellator;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.*;
import ab.j3d.awt.view.*;
import ab.j3d.awt.view.jogl.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * Example program for the tessellation functionality in the library.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TessellationExample
{
	/**
	 * UV-map.
	 */
	private static final BoxUVMap UV_MAP = new BoxUVMap( Scene.MM );

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new TessellationExample();
	}

	/**
	 * Construct example application.
	 */
	private TessellationExample()
	{
		final Scene scene = new Scene( Scene.MM );
		Scene.addLegacyLights( scene );
		double x = -140.0;
		x += createSituation1( scene, x, 40.0 ) + 10.0;
		x += createSituation2( scene, x, 40.0 ) + 10.0;
		x += createSituation3( scene, x, 40.0 ) + 10.0;
		x += createSituation4( scene, x, 40.0 ) + 10.0;
		x += createSituation5( scene, x, 40.0 ) + 10.0;
		createSituation6( scene, -140.0, 110.0 );
		showScene( scene );
	}

	/**
	 * Show the given scene in a frame.
	 *
	 * @param   scene   SCene to show.
	 */
	private static void showScene( final Scene scene )
	{
		final JOGLConfiguration joglConfiguration = new JOGLConfiguration();
		final JOGLEngine engine = new JOGLEngine( joglConfiguration );

		final View3D view = engine.createView( scene );
		view.setRenderingPolicy( RenderingPolicy.SKETCH );
		view.setFrontClipDistance( 10.0 );
		view.setBackClipDistance( 2000.0 );
		view.setCameraControl( new FromToCameraControl( view, new Vector3D( 0.0, 0.0, 500.0 ), Vector3D.ZERO )
		{
			@Override
			protected boolean isDragFromAroundToEvent( final ControlInputEvent event )
			{
				return ( event.getSupportedModifiers() == InputEvent.BUTTON1_DOWN_MASK ) || super.isDragFromAroundToEvent( event );
			}
		} );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent(), BorderLayout.CENTER );
		viewPanel.add( View3DPanel.createToolBar( view, Locale.ENGLISH ), BorderLayout.NORTH );

		final JFrame frame = new JFrame( String.valueOf( TessellationExample.class ) );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setContentPane( viewPanel );
		frame.setSize( 800, 600 );

		final Toolkit toolkit = frame.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		frame.setLocation( screenBounds.x + ( screenBounds.width  + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2,
		                   screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );

		frame.setVisible( true );
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 *
	 * @return  Size of content along X-axis.
	 */
	private static double createSituation1( final Scene scene, final double x, final double y  )
	{
		final Shape positive = new Rectangle2D.Double( 10.0, 10.0, 40.0, 40.0 );
		final Shape negative = new Rectangle2D.Double( 0.0, 20.0, 20.0, 20.0 );

		final List<Shape> negativeShapes = Arrays.asList( negative );

		addToScene( scene, x, y, "situation1", positive, negativeShapes, 60.0 );

		return 50.0;
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 *
	 * @return  Size of content along X-axis.
	 */
	private static double createSituation2( final Scene scene, final double x, final double y  )
	{
		final Shape positive = new Rectangle2D.Double( 10.0, 10.0, 40.0, 40.0 );
		final Shape negative1 = new Rectangle2D.Double( 0.0, 20.0, 20.0, 20.0 );
		final Shape negative2 = new Rectangle2D.Double( 15.0, 0.0, 20.0, 30.0 );
		final List<Shape> negativeShapes = Arrays.asList( negative1, negative2 );

		addToScene( scene, x, y, "situation2", positive, negativeShapes, 60.0 );

		return 50.0;
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 *
	 * @return  Size of content along X-axis.
	 */
	private static double createSituation3( final Scene scene, final double x, final double y  )
	{
		final Shape positive = new Rectangle2D.Double( 10.0, 10.0, 40.0, 40.0 );
		final Shape negative1 = new Rectangle2D.Double( 0.0, 20.0, 20.0, 20.0 );
		final Shape negative2 = new Rectangle2D.Double( 25.0, 15.0, 10.0, 30.0 );
		final List<Shape> negativeShapes = Arrays.asList( negative1, negative2 );

		addToScene( scene, x, y, "situation3", positive, negativeShapes, 60.0 );

		return 50.0;
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 *
	 * @return  Size of content along X-axis.
	 */
	private static double createSituation4( final Scene scene, final double x, final double y  )
	{
		final Path2D.Double positive = new Path2D.Double( Path2D.WIND_EVEN_ODD );
		positive.moveTo( 10.0, 10.0 );
		positive.lineTo( 50.0, 50.0 );
		positive.lineTo( 10.0, 50.0 );
		positive.lineTo( 50.0, 10.0 );
		positive.closePath();

		addToScene( scene, x, y, "situation4", positive, Collections.<Shape>emptyList(), 60.0 );

		return 50.0;
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 *
	 * @return  Size of content along X-axis.
	 */
	private static double createSituation5( final Scene scene, final double x, final double y  )
	{
		final Path2D.Double positive = new Path2D.Double( Path2D.WIND_EVEN_ODD );
		positive.moveTo( 10.0, 10.0 );
		positive.lineTo( 50.0, 50.0 );
		positive.lineTo( 10.0, 50.0 );
		positive.lineTo( 50.0, 10.0 );
		positive.closePath();

		final Shape negative = new Rectangle2D.Double( 20.0, 20.0, 20.0, 20.0 );

		addToScene( scene, x, y, "situation5", positive, Arrays.asList( negative ), 60.0 );

		return 50.0;
	}

	/**
	 * Create situation and add it to the scene.
	 *
	 * @param   scene   Scene to add content to.
	 * @param   x       Position in scene to add content at.
	 * @param   y       Position in scene to add content at.
	 */
	private static void createSituation6( final Scene scene, final double x, final double y  )
	{
		final FontRenderContext renderContext = new FontRenderContext( new AffineTransform( 1.0, 0.0, 0.0, 1.0, 0.0, 0.0), false, true );

		final Font font = new Font( "serif", Font.PLAIN, 30 );

		final GlyphVector glyphVector = font.createGlyphVector( renderContext, "abcefghjkmoqrsxy" );
		final int numGlyphs = glyphVector.getNumGlyphs();
		for ( int i = 0; i < numGlyphs; i++ )
		{
			final Shape outline = glyphVector.getGlyphOutline( i );
			final Point2D position = glyphVector.getGlyphPosition( i );
			addToScene( scene, x + 0.3 * position.getX(), y + position.getY(), "text-" + i, outline, Collections.<Shape>emptyList(), 30.0 );
		}
	}

	private static void addToScene( final Scene scene, final double x, final double y, final String name, final Shape positive, final List<Shape> negativeShapes, final double offsetY )
	{
		final Area area = new Area( positive );
		for ( final Shape shape : negativeShapes )
		{
			area.subtract( new Area( shape ) );
		}

		final Matrix3D flipY = Matrix3D.getTransform( 180.0, 0.0, 0.0, 0.0, 0.0, 0.0 );

		{
			final Object3DBuilder builder = new Object3DBuilder();
			ShapeTools.addFilledShape2D( builder, flipY, area, 0.1, BasicAppearances.GREEN, UV_MAP, false, true, false );
			scene.addContentNode( name + "-1" , Matrix3D.getTranslation( x, y, 0.0), builder.getObject3D() );
		}

		final Tessellator areaTessellator = ShapeTools.createTessellator( area, 0.1 );

		{
			final Object3DBuilder builder = new Object3DBuilder();
			final Vector3D extrusionPos = new Vector3D( 0.0, 0.0, 5.0 );
			builder.addExtrudedShape( areaTessellator, extrusionPos, true, flipY, true, BasicAppearances.BLUE, UV_MAP, false, true, BasicAppearances.RED, UV_MAP, false, true, BasicAppearances.YELLOW, UV_MAP, false, false, false, false );
			scene.addContentNode( name + "-2" , Matrix3D.getTranslation( x, y - offsetY, 0.0), builder.getObject3D() );
		}

		{
			final Object3DBuilder builder = new Object3DBuilder();
			final Vector3D extrusionNeg = new Vector3D( 0.0, 0.0, -5.0 );
			builder.addExtrudedShape( areaTessellator, extrusionNeg, true, flipY, true, BasicAppearances.SILVER, UV_MAP, false, true, BasicAppearances.CHROME, UV_MAP, false, true, BasicAppearances.GOLD, UV_MAP, false, false, false, false );
			scene.addContentNode( name + "-3" , Matrix3D.getTranslation( x, y - 2 * offsetY, 0.0), builder.getObject3D() );
		}
	}
}
