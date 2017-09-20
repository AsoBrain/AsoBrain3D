/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.example;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * This application provides an example for the {@link Cylinder3D} class.
 *
 * @author Peter S. Heijnen
 */
public class Cylinder3DExample
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		final Cylinder3DExample example = new Cylinder3DExample();

		final JFrame frame = new JFrame( "Cylinder3D Example" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setContentPane( example._viewPanel );
		frame.setSize( 1024, 700 );

		final Toolkit toolkit = frame.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		frame.setLocation( screenBounds.x + ( screenBounds.width + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2,
		                   screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );

		frame.setVisible( true );
	}

	/**
	 * Panel with 3D view.
	 */
	private final JPanel _viewPanel;

	/**
	 * Construct example.
	 */
	private Cylinder3DExample()
	{
		final BoxUVMap uvMap = new BoxUVMap( Scene.MM );

		final Scene scene = new Scene( Scene.MM );
		Scene.addLegacyLights( scene );
		scene.addContentNode( "cyl1", Matrix3D.getTranslation( -100.0, 0.0, 0.0 ), new Cylinder3D( 40.0, 10.0, 11, BasicAppearances.BLUE, uvMap, true, BasicAppearances.GREEN, uvMap, BasicAppearances.RED, uvMap, true ) );
		scene.addContentNode( "cyl2", Matrix3D.getTranslation( -50.0, 0.0, 0.0 ), new Cylinder3D( 40.0, 15.0, 11, BasicAppearances.BLUE, null, true, null, null, null, null, false ) );
		scene.addContentNode( "cyl3", Matrix3D.IDENTITY, new Cylinder3D( 40.0, 15.0, 11, BasicAppearances.BLUE, uvMap, true, BasicAppearances.GREEN, uvMap, BasicAppearances.RED, uvMap, false ) );
		scene.addContentNode( "cyl4", Matrix3D.getTranslation( 50.0, 0.0, 0.0 ), new Cylinder3D( 40.0, 15.0, 11, BasicAppearances.BLUE, uvMap, false, BasicAppearances.GREEN, uvMap, BasicAppearances.RED, uvMap, false ) );
		scene.addContentNode( "cyl5", Matrix3D.getTranslation( 100.0, 0.0, 0.0 ), new Cylinder3D( 0.0, 15.0, 11, BasicAppearances.BLUE, uvMap, true, BasicAppearances.GREEN, uvMap, BasicAppearances.RED, uvMap, false ) );
		scene.addContentNode( "cyl6", Matrix3D.getTranslation( 150.0, 0.0, 0.0 ), new Cylinder3D( 0.0, 15.0, 11, BasicAppearances.BLUE, uvMap, true, BasicAppearances.GREEN, uvMap, BasicAppearances.RED, uvMap, true ) );

		final RenderEngine renderEngine = RenderEngineFactory.createJOGLEngine( new NullTextureLibrary(), JOGLConfiguration.createDefaultInstance() );

		final View3D view = renderEngine.createView( scene );
		view.setRenderingPolicy( RenderingPolicy.SKETCH );
		view.setBackground( Background.createGradient( new Color4f( 0x67, 0x79, 0x88 ), new Color4f( 0x17, 0x47, 0x72 ), new Color4f( 0x85, 0xA4, 0xBF ), new Color4f( 0x9F, 0xB8, 0xCE ) ) );
		view.setCameraControl( new FromToCameraControl( view, new Vector3D( 0.0, -1500.0, 250.0 ), Vector3D.ZERO ) );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent(), BorderLayout.CENTER );
		viewPanel.add( View3DPanel.createToolBar( view, new Locale( "nl" ) ), BorderLayout.SOUTH );
		_viewPanel = viewPanel;
	}

	/**
	 * Get panel with 3D view.
	 *
	 * @return Panel with 3D view.
	 */
	public JPanel getViewPanel()
	{
		return _viewPanel;
	}
}
