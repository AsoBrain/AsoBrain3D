/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2006 Peter S. Heijnen
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
package ab.j3d.view.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Transform3D;
import ab.j3d.view.ViewModelView;

/**
 * This panel is used as view and control of a <code>Renderer</code>. It starts
 * or stops the renderer based on the visibility of this panel.
 *
 * @see     RenderThread
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderPanel
	extends JComponent
	implements ComponentListener
{
	/** Render mode: quick (wireframe). */ public static final int QUICK = 1;
	/** Render mode: full (solid).      */ public static final int FULL  = 2;

	/**
	 * Drag support for render panel.
	 */
	private final RenderControl _renderControl;

	/**
	 * Transform node of view.
	 */
	private final Transform3D _modelTransform;

	/**
	 * Base node of rendered model.
	 */
	private final Transform3D _model;

	/**
	 * Model bounds. Used to center the output.
	 *
	 * @see     #setBounds
	 * @see     #center
	 */
	private Bounds3D _bounds = Bounds3D.INIT;

	/**
	 * The camera node with which this ViewPanel is associated. The node can
	 * be considered the model of ViewPanel.
	 */
	private final Camera3D _camera;

	/**
	 * This is the current rendering mode (either QUICK or FULL),
	 * this may set to QUICK during frequent updates and to FULL
	 * when those updates are done. The renderer may use this to
	 * speed up its operation (e.g. by lowering detail) during
	 * updates.
	 */
	private int _renderingMode = FULL;

	/**
	 * Thread that handles the update process.
	 */
	private RenderThread _renderThread;

	/**
	 * Renderer for temporary wireframe.
	 */
	private final WireframeRenderer _wireFrameRenderer = new WireframeRenderer();

	/**
	 * Construct renderer for the specified component with the specified
	 * initial dimensions.
	 */
	public RenderPanel()
	{
		setDoubleBuffered( true );
		setForeground( Color.black );

		/*
		 * Construct 3D world.
		 */
		final Node3D world = new Node3D();

		final Transform3D model = new Transform3D();
		final Transform3D modelTransform = new Transform3D();
		modelTransform.addChild( model );
		world.addChild( modelTransform );
		_modelTransform = modelTransform;
		_model = model;

		final Light3D ambientLight = new Light3D( 500 , -1.0 ); // 384
		world.addChild( ambientLight );

		final Light3D pointLight = new Light3D( 10000 , 30.0 );
		final Transform3D pointLightTransform = new Transform3D( Matrix3D.getTransform( 0.0 , 0.0 , 0.0 , -750.0 , -2500.0 , 1700.0 ) );
		world.addChild( pointLightTransform );
		pointLightTransform.addChild( pointLight );

		final Camera3D camera = new Camera3D( 300.0 , Math.toRadians( 60.0 ) );
		final Transform3D cameraTransform = new Transform3D( Matrix3D.getTransform( 0.0 , 0.0 , 0.0 , 0.0 , -3000.0 , 0.0 ) );
		cameraTransform .addChild( camera );
		world.addChild( cameraTransform );
		_camera = camera;

		_renderControl = new RenderControl( this , 50.0 , Math.toDegrees( ViewModelView.FULL_CIRCLE_PER_250_PIXELS ) );

		/*
		 * Initialize render/control variables.
		 */
		_renderThread = null;
		reset();

		/*
		 * Enable control events.
		 */
		addComponentListener( this );
	}

	/**
	 * Get base transform of rendered model.
	 *
	 * @return  Base transform of rendered model.
	 */
	public final Transform3D getBase()
	{
		return _model;
	}

	/**
	 * Get camera used by this renderer to view the scene.
	 *
	 * @return  Camera used to view the scene.
	 */
	public Camera3D getCamera()
	{
		return _camera;
	}

	/**
	 * Get transform node of view.
	 *
	 * @return  Transform node of view.
	 */
	public Transform3D getModelTransform()
	{
		return _modelTransform;
	}

	/**
	 * Set minimum/maximum coordiantes of displayed model.
	 *
	 * @param   bounds  Bounds of displayed model.
	 */
	public final void setLimits( final Bounds3D bounds )
	{
		_bounds = bounds;
	}

	/**
	 * Set current control mode for panel.
	 *
	 * @param   mode    Control mode for panel (ZOOM,PAN,ROTATE).
	 */
	public final void setControlMode( final int mode )
	{
		_renderControl.setControlMode( mode );
	}

	/**
	 * Set the rendering mode to use. This may be set to QUICK during frequent
	 * updates, and to FULL when those updates are done. The renderer may use
	 * this to speed up its operation (e.g. by lowering detail) during updates.
	 *
	 * @param   mode    Rendering mode to use (QUICK or FULL).
	 */
	public final void setRenderingMode( final int mode )
	{
		if ( _renderingMode != mode )
		{
			_renderingMode = mode;
			requestUpdate();
		}
	}

	/**
	 * Get string with view settings of renderer.
	 *
	 * @return  String with view settings of renderer.
	 */
	public final String getViewSettings()
	{
		return String.valueOf( Bounds3D.INIT.set(
			Vector3D.INIT.set(
				_renderControl.getRotationX() ,
			    _renderControl.getRotationY() ,
			    _renderControl.getRotationZ() ) ,
		    Vector3D.INIT.set(
			    _renderControl.getTranslationX() ,
		        _renderControl.getTranslationY() ,
		        _renderControl.getTranslationZ() )
			) );
	}

	/**
	 * Set view settings based on string previously returned by #getViewSettings().
	 *
	 * @param   settings    String with view settings.
	 */
	public final void setViewSettings( final String settings )
	{
		if ( ( settings != null ) && ( settings.length() > 0 ) )
		{
			try
			{
				final Bounds3D b = Bounds3D.fromString( settings );

				final RenderControl ds = _renderControl;
				ds.setRotationX( b.v1.x );
				ds.setRotationY( b.v1.y );
				ds.setRotationZ( b.v1.z );
				ds.setTranslationX( b.v2.x );
				ds.setTranslationY( b.v2.y );
				ds.setTranslationZ( b.v2.z );
			}
			catch ( Exception e )
			{
				System.err.println( "setViewSettings( '" + settings + "' ) => " + e );
			}
		}
	}

	/**
	 * Reset to default render view.
	 */
	public final void reset()
	{
		final RenderControl renderControl = _renderControl;
		renderControl.setRotationX( -10.0 );
		renderControl.setRotationY( 0.0 );
		renderControl.setRotationZ( -35.0 );
		renderControl.setTranslationX( 0.0 );
		renderControl.setTranslationY( 0.0 );
		renderControl.setTranslationZ( 0.0 );

		center();
		requestUpdate();
	}

	/**
	 * Place base in center of view.
	 */
	public final void center()
	{
		final Transform3D model     = _model;
		final Bounds3D    bounds    = _bounds;
		final Matrix3D    transform = model.getTransform();

		model.setTransform( transform.setTranslation(
			-0.5 * ( bounds.v1.x + bounds.v2.x ) ,
		    -0.5 * ( bounds.v1.y + bounds.v2.y ) ,
		    -0.5 * ( bounds.v1.z + bounds.v2.z ) ) );
	}

	/**
	 * Start background render thread if it was not already started. It is not
	 * required to call this method if the panel is registered as component
	 * listener to a container, in which case the render will be started/stopped
	 * automatically.
	 *
	 * @see     #stopRenderer
	 * @see     #componentShown
	 * @see     #componentHidden
	 */
	public void startRenderer()
	{
		final RenderThread renderThread = _renderThread;
		if ( ( renderThread == null ) || !renderThread.isAlive() )
		{
			_renderThread = createRenderThread();
		}
	}

	/**
	 * Stop background render thread if it was started. It is not required to
	 * call this method if the panel is registered as component listener to a
	 * container, in which case the render will be started/stopped automatically.
	 *
	 * @see     #startRenderer
	 * @see     #componentShown
	 * @see     #componentHidden
	 */
	public void stopRenderer()
	{
		final RenderThread renderThread = _renderThread;
		if ( renderThread != null )
			renderThread.requestTermination();
	}

	/**
	 * This method is called by {@link #startRenderer}
	 *
	 * @return  Render thread instance (never <code>null</code>).
	 */
	protected RenderThread createRenderThread()
	{
		return new RenderThread( this , getCamera() );
	}

	/**
	 * Paint the component.
	 *
	 * @param   g       Graphics context.
	 */
	protected void paintComponent( final Graphics g )
	{
		super.paintComponent( g );

		final Insets insets = getInsets();
		final int    x      = insets.left;
		final int    y      = insets.top;
		final int    width  = getWidth() - insets.left - insets.right;
		final int    height = getHeight() - insets.top - insets.bottom;

		final RenderThread renderThread = _renderThread;
		final BufferedImage image = ( renderThread == null ) ? null : _renderThread.getRenderedImage();

		if ( ( image == null ) || ( _renderingMode != FULL ) )
		{
			final Graphics clipped = g.create( x , y , width , height );
			clipped.setColor( getBackground() );
			clipped.fillRect( 0 , 0 , width , height );
			clipped.setColor( getForeground() );
			_wireFrameRenderer.renderScene( clipped , 0 , 0 , width , height , _camera );
			clipped.dispose();
		}
		else
		{
			g.drawImage( image , x , y , width , height , this );
		}
	}

	/**
	 * Set flag to indicate that the frame must be updated. This
	 * will only set the flag, render() must be called to actually
	 * do the rendering at a suitable time.
	 */
	public final synchronized void requestUpdate()
	{
		final RenderThread renderThread = _renderThread;
		if ( renderThread != null )
		{
			_modelTransform.setTransform( _renderControl.getTransform() );
//			System.out.println( "_modelTransform.getMatrix() = " + _modelTransform.getMatrix().toFriendlyString() );
			renderThread.requestUpdate();
			repaint();
		}
	}

	/**
	 * Respond to 'component hidden' event. This is used to stop the update thread
	 * of the renderer.
	 *
	 * @param   e   Component event.
	 *
	 * @see     #componentShown
	 * @see     #startRenderer
	 * @see     #stopRenderer
	 */
	public void componentHidden( final ComponentEvent e )
	{
		stopRenderer();
	}

	/**
	 * Respond to 'component moved' event.
	 *
	 * @param   e   Component event.
	 */
	public void componentMoved( final ComponentEvent e )
	{
	}

	/**
	 * Respond to 'component resized' event. This is used to re-initialize the
	 * image buffers to the new component size.
	 *
	 * @param   e   Component event.
	 */
	public void componentResized( final ComponentEvent e )
	{
		if ( e.getSource() == this )
			requestUpdate();
	}

	/**
	 * Respond to 'component shown' event. This is used to start the update thread
	 * of the renderer.
	 *
	 * @param   e   Component event.
	 */
	public final void componentShown( final ComponentEvent e )
	{
		startRenderer();

		if ( e.getSource() == this )
			requestUpdate();
	}
}
