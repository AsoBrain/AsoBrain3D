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
package ab.j3d.geom.tessellator;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;
import com.numdata.oss.ui.*;

/**
 * Example program for the tessellation functionality in the library.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TessellationExample
{
	/**
	 * Red material.
	 */
	private static final Material RED = new Material( 0xFFFF0000 );

	/**
	 * Green material.
	 */
	private static final Material GREEN = new Material( 0xFF00FF00 );

	/**
	 * Blue material.
	 */
	private static final Material BLUE = new Material( 0xFF0000FF );

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
		createSituation1( scene, 0.0, 0.0 );
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
		view.setProjectionPolicy( ProjectionPolicy.PARALLEL );
		view.setScene2View( Matrix3D.getTranslation( 0.0, 0.0, -100.0 ) );
		view.setCameraControl( new PanZoomAndRotateCameraControl( view ) );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent(), BorderLayout.CENTER );
		viewPanel.add( view.createToolBar( Locale.ENGLISH ), BorderLayout.NORTH );

		final JFrame frame = WindowTools.createFrame( String.valueOf( TessellationExample.class ), 800, 600, viewPanel );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
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
		final String name = "rectangle";
//		final Shape shape = new Rectangle2D.Double( -10.0, -20.0, 20.0, 40.0 );
		final Shape positive = new Rectangle2D.Double( 20.0, 20.0, 40.0, 40.0 );
		final Shape negative = new Rectangle2D.Double( 10.0, 30.0, 20.0, 20.0 );

		final Object3DBuilder builder = new Object3DBuilder();
//		final Vector3D extrusionPos = new Vector3D( 0.0, 0.0, 5.0 );
//		builder.addExtrudedShape( shape, 1.0, extrusionPos, Matrix3D.INIT.plus( -300.0, 150.0, 0.0 ), green, uvMap, false, red, uvMap, false, blue, uvMap, false, false, false, false );
		builder.addTessellatedShape( Matrix3D.INIT, positive, Arrays.asList( negative ), Vector3D.POSITIVE_Z_AXIS, 1.0, GREEN, UV_MAP, false, false );
		scene.addContentNode( name, Matrix3D.getTranslation( x, y, 0.0), builder.getObject3D() );

		return 50.0;
	}
}