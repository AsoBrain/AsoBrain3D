/*
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.demo;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.awt.view.*;
import ab.j3d.awt.view.java2d.*;
import ab.j3d.awt.view.jogl.*;
import ab.j3d.control.*;
import ab.j3d.loader.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * <p>Simple 3D model viewer for use on the web. Currently only supports zipped
 * OBJ models including the MTL file.
 *
 * <table>
 * <tr><th>Parameter</th><th>Description</th></tr>
 * <tr><td>source</td><td>Location of the model. (required)</td></tr>
 * <tr><td>textures</td><td>Base URL for textures used in the model.</td></tr>
 * <tr><td>opengl</td><td>Whether OpenGL should be used.</td></tr>
 * <tr><td>shadows</td><td>Whether shadows should be rendered; requires OpenGL.</td></tr>
 * <tr><td>controls</td><td>Whether to show a tool bar with some additional controls.</td></tr>
 * <tr><td>background</td><td>Background color (#rrggbb) or gradient (#rrggbb #rrggbb).</td></tr>
 * </table>
 *
 * @author  G. Meinders
 */
public class ViewerApplet
	extends JApplet
{
	/**
	 * RGB color specified in hexadecimal using 6 digits.
	 */
	private static final Pattern COLOR_RGB_6 = Pattern.compile( "#[0-9a-f]{6}" );

	/**
	 * Gradient of two RGB colors specified in hexadecimal using 6 digits each.
	 */
	private static final Pattern GRADIENT_2_RGB_6 = Pattern.compile( "#[0-9a-f]{6} #[0-9a-f]{6}" );

	/**
	 * Name of resource bundle for this class.
	 */
	private static final String BUNDLE_NAME = ViewerApplet.class.getPackage().getName() + ".LocalStrings";

	/**
	 * 3D view.
	 */
	private View3D _view = null;

	/**
	 * Scene to be viewed.
	 */
	private Scene _scene = null;

	/**
	 * Executor for loading models asynchronously.
	 */
	private ExecutorService _executor = null;

	/**
	 * Shows status messages on top of the 3D view.
	 */
	private StatusOverlay _statusOverlay = null;

	/**
	 * Parses a 6-digit hexadecimal color with leading '#' character, starting
	 * at the given offset in the given string.
	 *
	 * @param   string  String to be parsed.
	 * @param   offset  Offset in the string.
	 *
	 * @return  Parsed color.
	 */
	private static Color4f parseRgb6( final String string, final int offset )
	{
		final int red = Integer.parseInt( string.substring( offset + 1, offset + 3 ), 16 );
		final int green = Integer.parseInt( string.substring( offset + 3, offset + 5 ), 16 );
		final int blue = Integer.parseInt( string.substring( offset + 5, offset + 7 ), 16 );
		return new Color4f( red, green, blue );
	}

	@Override
	public void init()
	{
		final Scene scene = new Scene( Scene.MM );
		Scene.addLegacyLights( scene );
		_scene = scene;
	}

	@Override
	public void start()
	{
		SwingUtilities.invokeLater( new InitOnEDT() );
	}

	@Override
	public void stop()
	{
		if ( _view != null )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					_view.dispose();
				}
			} );
		}
		else
		{
			System.err.println( "stop() called before view was made" );
		}

		final ExecutorService executor = _executor;
		if ( executor != null )
		{
			executor.shutdownNow();
		}
		else
		{
			System.err.println( "stop() called before executor was made" );
		}
	}

	/**
	 * Shows status text on top of 3D views.
	 */
	private static class StatusOverlay
		implements ViewOverlay
	{
		/**
		 * Registered views.
		 */
		private final List<View3D> _views = new ArrayList<View3D>();

		/**
		 * Current status text.
		 */
		private String _status = null;

		/**
		 * Sets the status text.
		 *
		 * @param   status  Status text; <code>null</code> to show nothing.
		 */
		public void setStatus( final String status )
		{
			_status = status;
			updateViews();
		}

		/**
		 * Updates all registered views.
		 */
		private void updateViews()
		{
			for ( final View3D view : _views )
			{
				view.update();
			}
		}

		public void addView( final View3D view )
		{
			_views.add( view );
		}

		public void removeView( final View3D view )
		{
			_views.remove( view );
		}

		public void paintOverlay( final View3D view, final Graphics2D g )
		{
			if ( _status != null )
			{
				final String status = _status;
				final FontMetrics fontMetrics = g.getFontMetrics();
				final Rectangle2D statusBounds = fontMetrics.getStringBounds( status, g );
				final Component component = view.getComponent();
				final int padding = 2;

				g.setFont( new Font( Font.SANS_SERIF, Font.PLAIN, 14 ) );
				g.setColor( new Color( 0x80000000, true ) );
				g.fillRect( 0, 0, component.getWidth(), fontMetrics.getHeight() + fontMetrics.getLeading() + 2 * padding );
				g.setColor( Color.WHITE );
				g.drawString( status, (int)( (double)component.getWidth() - statusBounds.getWidth() ) / 2, fontMetrics.getLeading() + fontMetrics.getAscent() + padding );
			}
		}
	}

	/**
	 * Performs initialization of the applet to be performed on the AWT event
	 * dispatch thread, such as creation of Swing components.
	 */
	private class InitOnEDT
		implements Runnable
	{
		public void run()
		{
			final Locale locale = getLocale();
			final ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );

			try
			{
				final Scene scene = _scene;
				final RenderEngine engine;

				if ( Boolean.parseBoolean( getParameter( "opengl" ) ) )
				{
					engine = new JOGLEngine();
				}
				else
				{
					engine = new Java2dEngine();
				}

				if ( engine instanceof JOGLEngine )
				{
					if ( Boolean.parseBoolean( getParameter( "shadows" ) ) )
					{
						final JOGLEngine joglEngine = (JOGLEngine)engine;
						final JOGLConfiguration configuration = joglEngine.getConfiguration();
						configuration.setShadowEnabled( true );
						configuration.setShadowMultisampleEnabled( true );
					}
				}
				System.err.println( "Engine initialized" );

				final View3D view = engine.createView( scene );
				view.setCameraControl( new FromToCameraControl( view ) );

				final String background = getParameter( "background" );
				if ( background != null )
				{
					if ( COLOR_RGB_6.matcher( background ).matches() )
					{
						view.setBackground( Background.createSolid( parseRgb6( background, 0 ) ) );
					}
					else if ( GRADIENT_2_RGB_6.matcher( background ).matches() )
					{
						final Color4f color1 = parseRgb6( background, 0 );
						final Color4f color2 = parseRgb6( background, 8 );
						view.setBackground( Background.createGradient( color1, color1, color2, color2 ) );
					}
					else
					{
						System.out.println( "Ignoring invalid background parameter: " + background );
					}
				}

				final StatusOverlay statusOverlay = new StatusOverlay();
				String statusText = "Loading model...";
				try
				{
					statusText = bundle.getString( "loadingModel" );
				}
				catch ( MissingResourceException e )
				{
					/* ignored, will return default value */
				}
				statusOverlay.setStatus( statusText );
				view.addOverlay( statusOverlay );
				_statusOverlay = statusOverlay;

				_view = view;
				System.err.println( "View initialized" );

				if ( Boolean.parseBoolean( getParameter( "controls" ) ) )
				{
					final JPanel panel = new JPanel( new BorderLayout() );
					panel.add( view.getComponent() );
					panel.add( View3DPanel.createToolBar( view, locale ), BorderLayout.PAGE_END );
					setContentPane( panel );
				}
				else
				{
					add( view.getComponent() );
				}

				final ExecutorService executor = Executors.newSingleThreadExecutor();
				_executor = executor;
				System.err.println( "Initialization complete" );

				executor.submit( new Start() );
			}
			catch ( Throwable e )
			{
				System.err.println( "Initialization failed" );
				e.printStackTrace();
				_statusOverlay.setStatus( e.toString() );
			}
		}
	}

	/**
	 * Called by {@link ViewerApplet#start()} to start the viewer.
	 */
	private class Start
		implements Runnable
	{
		public void run()
		{
			final Scene scene = _scene;
			final String source = getParameter( "source" );

			Object3D model = null;
			if ( scene == null )
			{
				System.err.println( "Can't start, because the scene was not initialized" );

			}
			else if ( source == null )
			{
				System.err.println( "No 'source' specified" );
			}
			else if ( !scene.hasContentNode( "model" ) )
			{
				System.out.println( "Loading model from: " + source );
				try
				{
					final MultiResourceLoader resourceLoader = new MultiResourceLoader();

					final URL sourceUrl = new URL( source );
					resourceLoader.mount( new ZipResourceLoader( sourceUrl ) );

					final String textures = getParameter( "textures" );
					if ( textures != null )
					{
						System.out.println( "Loading textures from: " + textures );
						final URL texturesUrl = new URL( textures );
						resourceLoader.mount( new URLResourceLoader( texturesUrl ) );
					}

					final ObjLoader objLoader = new ObjLoader( Matrix3D.IDENTITY );
					model = objLoader.load( resourceLoader, "model.obj" );
				}
				catch ( IOException e )
				{
					System.err.println( "Exception occurred while trying to load model" );
					e.printStackTrace();
					_statusOverlay.setStatus( e.toString() );
					throw new RuntimeException( e );
				}
			}

			if ( model != null )
			{
				scene.addContentNode( "model", Matrix3D.IDENTITY, model );

				SwingUtilities.invokeLater( new StartOnEDT() );
			}
			else
			{
				System.out.println( "Failed to load model" );
				_statusOverlay.setStatus( "Failed to load model" );
			}
		}
	}

	/**
	 * Called from {@link Start} on EDT to start the viewer.
	 */
	private class StartOnEDT
		implements Runnable
	{
		public void run()
		{
			System.out.println( "start" );

			final View3D view = _view;
			final Scene scene = _scene;
			final Bounds3D sceneBounds = scene.getBounds();
			if ( sceneBounds != null )
			{
				final Vector3D center = sceneBounds.center();
				final double fromX = ( sceneBounds.v2.x - center.x ) * 0.5 + center.x;
				final double fromY = -1000.0;
				final double fromZ = Math.max( 500.0, ( sceneBounds.v2.z - center.z ) * 0.5 + center.z );
				view.setScene2View( Matrix3D.getFromToTransform( new Vector3D( fromX, fromY, fromZ ), center, Vector3D.POSITIVE_Z_AXIS, Vector3D.POSITIVE_Y_AXIS ) );
				final CameraControl cameraControl = view.getCameraControl();
				cameraControl.zoomToFit();
				cameraControl.save();
			}

			_statusOverlay.setStatus( null );
		}
	}
}