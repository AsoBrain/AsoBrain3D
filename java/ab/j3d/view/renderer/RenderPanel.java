/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
package ab.j3d.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This panel is used as view and control of a <code>Renderer</code>. It starts
 * or stops the renderer based on the visibility of this panel.
 *
 * @see     Renderer
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderPanel
    extends JComponent
	implements ComponentListener
{
	/** Control mode: zoom.   */ public static final int ZOOM   = 1;
	/** Control mode: pan.    */ public static final int PAN    = 2;
	/** Control mode: rotate. */ public static final int ROTATE = 3;

	/** Render mode: quick (wireframe). */ public static final int QUICK = 1;
	/** Render mode: full (solid).      */ public static final int FULL  = 2;

	/**
	 * Transform of model.
	 */
	private final Transform _modelTransform;

	/**
	 * Base node of rendered model.
	 */
	private final Transform _model;

	/**
	 * The camera node with which this ViewPanel is associated. The node can
	 * be considered the model of ViewPanel.
	 */
	private final Camera _camera;

	/**
	 * Transform of camera.
	 */
	private final Transform _cameraTransform;

	/**
	 * Model bounds. Used to center the output.
	 *
	 * @see     #setBounds
	 * @see     #center
	 */
	private Bounds3D _bounds = Bounds3D.INIT;

	/**
	 * This is the current "control mode" of the view. This
	 * may be ZOOM, PAN, or ROTATE.
	 */
	private int _controlMode = ROTATE;

	/**
	 * Mouse drag rotation sensitivity.
	 */
	private static final float MOUSE_ROTATION_SENSITIVITY = 1.4f;

	/**
	 * Mouse drag movement speed.
	 */
	private static final float MOUSE_PANNING_SENSITIVITY = 20.0f;

	/**
	 * Mouse X coordinate when dragging started.
	 */
	private int _dragMouseStartX;

	/**
	 * Mouse Y coordinate when dragging started.
	 */
	private int _dragMouseStartY;

	/**
	 * Rotation of model around X axis when dragging started.
	 */
	private float _dragStartRotationX;

	/**
	 * Rotation of model around Z axis when dragging started.
	 */
	private float _dragStartRotationZ;

	/**
	 * Position of dragged object when dragging started.
	 */
	private Vector3D _dragStartPosition;

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
	private Renderer _renderer;

	/**
	 * Flag to indicate that a temporary wireframe is drawn
	 * during the rendering process.
	 */
	protected boolean _showTemporaryWireframe = true;

	/**
	 * Temporary wireframe object nodes.
	 */
	private final LeafCollection _wireframeObjects = new LeafCollection();

	/**
	 * Temporary wireframe render objects.
	 */
	private RenderObject[] _wireframeRenderObjects;

	/**
	 * Construct renderer for the specified component with the specified
	 * initial dimensions.
	 */
	public RenderPanel()
	{
		setDoubleBuffered( true );
		setForeground( Color.white );

		/*
		 * Construct 3D world.
		 */
		_model = new Transform();
		_modelTransform = new Transform();
		_modelTransform.addChild( _model );

		_camera = new Camera( 300.0f , 60.0f );
		_cameraTransform = new Transform( Vector3D.INIT.set( 0 , -3000 , 0 ) );
		_cameraTransform.addChild( _camera );

		final TreeNode world = new TreeNode();
		world.addChild( _modelTransform );
		world.addChild( _cameraTransform );
		world.addChild( new Light( 500 , -1.0f ) ); // 384
		world.addChild( new Transform( Vector3D.INIT.set( -750.0f , -2500.0f , 1700.0f ) ) ).addChild( new Light( 10000 , 30.0f ) );

		/*
		 * Initialize render/control variables.
		 */
		_wireframeRenderObjects = null;
		_renderer = null;
		_dragStartPosition = null;
		_dragStartRotationX = 0;
		_dragStartRotationZ = 0;
		_dragMouseStartX = 0;
		_dragMouseStartY = 0;

		reset();

		/*
		 * Enable control events.
		 */
		addComponentListener( this );
		enableEvents( MouseEvent.MOUSE_MOTION_EVENT_MASK | MouseEvent.MOUSE_EVENT_MASK );
	}

	/**
	 * Place base in center of view.
	 */
	public final void center()
	{
		_model.setTranslation( _model.getTranslation().set(
			( _bounds.v1.x + _bounds.v2.x ) / -2 ,
			( _bounds.v1.y + _bounds.v2.y ) / -2 ,
			( _bounds.v1.z + _bounds.v2.z ) / -2 ) );
	}

	/**
	 * Respond to 'component hidden' event. This is used to stop the update thread
	 * of the renderer.
	 *
	 * @param   e   Component event.
	 */
	public void componentHidden( final ComponentEvent e )
	{
		final Renderer renderer = _renderer;
		if ( renderer != null )
			renderer.requestTermination();
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
		final Renderer renderer = _renderer;
		if ( ( renderer == null ) || !renderer.isAlive() )
		{
			_renderer = new Renderer( this , _camera );
		}
		else if ( e.getSource() == this )
		{
			requestUpdate();
		}
	}

	/**
	 * Get base transform of rendered model.
	 *
	 * @return  Base transform of rendered model.
	 */
	public final Transform getBase()
	{
		return _model;
	}

	/**
	 * Get transform for model.
	 *
	 * @return	Transform for model.
	 */
	public final Transform getModelTransform()
	{
		return _modelTransform;
	}

	/**
	 * Get string with view settings of renderer.
	 *
	 * @return	String with view settings of renderer.
	 */
	public final String getViewSettings()
	{
		return Bounds3D.INIT.set(
			Vector3D.INIT.set(
				_modelTransform.getRotationX() ,
				_modelTransform.getRotationY() ,
				_modelTransform.getRotationZ() ) ,
				_cameraTransform.getTranslation()
			).toString();
	}

	/**
	 * Get flag to indicate that a temporary wireframe is drawn
	 * during the rendering process.
	 *
	 * @return  <code>true</code> if a temporary wireframe is
	 *          drawn, <code>false</code> if not.
	 */
	public final boolean isShowTemporaryWireframe()
	{
		return( _showTemporaryWireframe );
	}

	/**
	 * Process mouse events.
	 *
	 * @param   event   Mouse event.
	 */
	protected void processMouseEvent( final MouseEvent event )
	{
		super.processMouseEvent( event );

		switch ( event.getID() )
		{
			/*
			 * Handle mouse button 'pressed' event. When this event occurs, request focus,
			 * and save view settings (so that we can manipulate it later).
			 */
			case MouseEvent.MOUSE_PRESSED :
				requestFocus();
				final Transform x = /*_controlLight ? _lightTransform :*/ _cameraTransform;

				_dragMouseStartX    = event.getX();
				_dragMouseStartY    = event.getY();
				_dragStartRotationX = _modelTransform.getRotationX();
				_dragStartRotationZ = _modelTransform.getRotationZ();
				_dragStartPosition  = x.getTranslation();
				break;

			/**
			 * Handle mouse button 'released' event. When this event occurs, change the
			 * renderer back to FULL mode, so that the manipulated view will be rendered
			 * fully.
			 */
			case MouseEvent.MOUSE_RELEASED :
				setRenderingMode( RenderPanel.FULL );
				//requestUpdate();
		}
	}

	/**
	 * Process mouse motion events.
	 *
	 * @param   event   Mouse event.
	 */
	protected void processMouseMotionEvent( final MouseEvent event )
	{
		super.processMouseMotionEvent( event );

		/*
		 * Handle mouse 'dragged' event. When this event occurs, manipulate
		 * the view settings and repaint the view. The renderer is set to QUICK mode,
		 * to allow fast manipulation until the mouse button is released.
		 */
		if ( event.getID() == MouseEvent.MOUSE_DRAGGED )
		{
			final int dx = event.getX() - _dragMouseStartX;
			final int dy = event.getY() - _dragMouseStartY;


			final int modifiers = event.getModifiers();
			int mode      = _controlMode;

			if ( ( modifiers & MouseEvent.BUTTON2_MASK ) != 0 )
			{
				mode = PAN;
			}
			else if ( ( modifiers & MouseEvent.BUTTON3_MASK ) != 0 )
			{
				mode = ZOOM;
			}

			final Transform x = /*_controlLight ? _lightTransform :*/ _cameraTransform;

			switch ( mode )
			{
				case ROTATE :
					setRenderingMode( RenderPanel.QUICK );
					_modelTransform.setRotationZ( _dragStartRotationZ + MOUSE_ROTATION_SENSITIVITY * dx );
					_modelTransform.setRotationX( _dragStartRotationX - MOUSE_ROTATION_SENSITIVITY * dy );
					requestUpdate();
					break;

				case PAN :
					setRenderingMode( RenderPanel.QUICK );
					x.setTranslation( _dragStartPosition.plus( -dx * MOUSE_PANNING_SENSITIVITY , 0 , dy * MOUSE_PANNING_SENSITIVITY ) );
					requestUpdate();
					break;

				case ZOOM :
					setRenderingMode( RenderPanel.QUICK );
					x.setTranslation( _dragStartPosition.set( _dragStartPosition.x , Math.max( -10000 , _dragStartPosition.y - dy * MOUSE_PANNING_SENSITIVITY ) , _dragStartPosition.z ) );
					requestUpdate();
					break;
			}
		}
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

		final Renderer renderer = _renderer;
		final BufferedImage image = ( renderer == null ) ? null : _renderer.getRenderedImage();

		if ( ( image == null ) || ( _renderingMode != FULL ) )
		{
			if ( _showTemporaryWireframe )
			{
				g.setColor( getForeground() );
				paintWireframe( g , _camera , x , y , width , height );
			}
		}
		else
		{
			g.drawImage( image , x , y , width , height , this );
		}
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   g       Graphics to paint on.
	 * @param   camera  Node with camera that defines the view.
	 * @param   x       Origin X-coordinate of painted image.
	 * @param   y       Origin Y-coordinate of painted image.
	 * @param   width   Width of painted image.
	 * @param   height  Height of painted image.
	 */
	public final void paintWireframe( final Graphics g , final Camera camera , final int x , final int y , final int width , final int height )
	{
		_wireframeObjects.clear();
		_camera.gatherLeafs( _wireframeObjects , Object3D.class , Matrix3D.INIT , true );
		final int nrObjects = _wireframeObjects.size();

		if ( nrObjects > 0 )
		{
			if ( _wireframeRenderObjects == null || nrObjects > _wireframeRenderObjects.length )
			{
				final RenderObject[] objects = new RenderObject[ nrObjects ];
				if ( _wireframeRenderObjects != null )
					System.arraycopy( _wireframeRenderObjects , 0 , objects , 0 , _wireframeRenderObjects.length );
				_wireframeRenderObjects = objects;
			}

			/*
			 * Add all objects and draw them.
			 */
			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				RenderObject ro = _wireframeRenderObjects[ i ];
				if ( ro == null )
					_wireframeRenderObjects[ i ] = ro = new RenderObject();

				ro.set( (Object3D)_wireframeObjects.getNode( i ) , _wireframeObjects.getMatrix( i ) , camera.aperture , camera.zoom , width , height , true );
			}

			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				final RenderObject ro = _wireframeRenderObjects[ i ];

				for ( RenderObject.Face face = ro.faces ; face != null ; face = face.next )
					paintWireframeFace( g , x , y , face );
			}
		}
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   g       Graphics context to paint on.
	 * @param   face    Face to render.
	 */
	protected void paintWireframeFace( final Graphics g , final int x , final int y , final RenderObject.Face face )
	{
		final int[] vertexIndices = face.vi;
		if ( vertexIndices.length >= 3 )
		{
			final RenderObject ro = face.getRenderObject();
			final int[] vertexX = ro.ph;
			final int[] vertexY = ro.pv;

			int vertexIndex = vertexIndices[ vertexIndices.length - 1 ];
			int x1 = x + ( vertexX[ vertexIndex ] >> 8 );
			int y1 = y + ( vertexY[ vertexIndex ] >> 8 );
			int x2;
			int y2;

			for ( int vertex = 0 ; vertex < vertexIndices.length ; vertex++ )
			{
				vertexIndex = vertexIndices[ vertex ];
				x2 = x + ( vertexX[ vertexIndex ] >> 8 );
				y2 = y + ( vertexY[ vertexIndex ] >> 8 );

				g.drawLine( x1 , y1 , x2 , y2 );

				x1 = x2;
				y1 = y2;
			}
		}
	}

	/**
	 * Set flag to indicate that the frame must be updated. This
	 * will only set the flag, render() must be called to actually
	 * do the rendering at a suitable time.
	 */
	public final synchronized void requestUpdate()
	{
		final Renderer renderer = _renderer;
		if ( renderer != null )
		{
			renderer.requestUpdate();
			repaint();
		}
	}

	/**
	 * Reset to default render view.
	 */
	public final void reset()
	{
		_modelTransform.setRotationX( -10.0f );
		_modelTransform.setRotationZ( -35.0f );
		_cameraTransform.setTranslation( Vector3D.INIT.set( 0.0f , -4000.0f , 0.0f ) );
		center();

		requestUpdate();
	}

	/**
	 * Set current control mode for panel.
	 *
	 * @param   mode    Control mode for panel (ZOOM,PAN,ROTATE).
	 */
	public final void setControlMode( final int mode )
	{
		_controlMode = mode;
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
	 * Set flag to indicate that a temporary wireframe is drawn during the
	 * rendering process.
	 *
	 * @param   show    <code>true</code> to indicate that a temporary wireframe
	 *                  should be drawn; <code>false</code> if not.
	 */
	public final void setShowTemporaryWireframe( final boolean show )
	{
		_showTemporaryWireframe = show;
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
				final Vector3D rv = b.v1;
				_modelTransform.setRotation( rv.x , rv.y , rv.z );
				_cameraTransform.setTranslation( b.v2 );
			}
			catch ( Exception e ) { /* ignored */ }
		}
	}
}
