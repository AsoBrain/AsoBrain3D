/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Transform3D;
import ab.j3d.view.MouseViewControl;
import ab.j3d.view.MouseViewEvent;
import ab.j3d.view.MouseViewListener;

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
	implements ComponentListener, MouseViewListener
{
	/** Render mode: quick (wireframe). */ public static final int QUICK = 1;
	/** Render mode: full (solid).      */ public static final int FULL  = 2;

	/**
	 * Mouse view control for render panel.
	 */
	private final MouseViewControl _mouseViewControl;

	/**
	 * Transform of model.
	 */
	private final Transform3D _modelTransform;

	/**
	 * Base node of rendered model.
	 */
	private final Transform3D _model;

	/**
	 * The camera node with which this ViewPanel is associated. The node can
	 * be considered the model of ViewPanel.
	 */
	private final Camera3D _camera;

	/**
	 * Transform of camera.
	 */
	private final Transform3D _cameraTransform;

	/**
	 * Model bounds. Used to center the output.
	 *
	 * @see     #setBounds
	 * @see     #center
	 */
	private Bounds3D _bounds = Bounds3D.INIT;

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
	private final Node3DCollection _wireframeObjects = new Node3DCollection();

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
		_model = new Transform3D();
		_modelTransform = new Transform3D();
		_modelTransform.addChild( _model );

		_camera = new Camera3D( 300.0f , 60.0f );
		_cameraTransform = new Transform3D( Vector3D.INIT.set( 0 , -3000 , 0 ) );
		_cameraTransform.addChild( _camera );

		final Node3D world = new Node3D();
		world.addChild( _modelTransform );
		world.addChild( _cameraTransform );
		world.addChild( new Light3D( 500 , -1.0f ) ); // 384
		world.addChild( new Transform3D( Vector3D.INIT.set( -750.0f , -2500.0f , 1700.0f ) ) ).addChild( new Light3D( 10000 , 30.0f ) );

		/*
		 * Initialize render/control variables.
		 */
		_wireframeRenderObjects = null;
		_renderer = null;
		reset();

		/*
		 * Enable control events.
		 */
		addComponentListener( this );
		_mouseViewControl = new MouseViewControl( this );
		_mouseViewControl.addMouseViewListener( this );
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
	public final Transform3D getBase()
	{
		return _model;
	}

	/**
	 * Get transform for model.
	 *
	 * @return  Transform3D for model.
	 */
	public final Transform3D getModelTransform()
	{
		return _modelTransform;
	}

	/**
	 * Get string with view settings of renderer.
	 *
	 * @return  String with view settings of renderer.
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
	 * Handle event from mouse control.
	 */
	public void mouseViewChanged( final MouseViewEvent event )
	{
		setRenderingMode( _mouseViewControl.isButtonDown() ? QUICK : FULL );
		_modelTransform.setMatrix( _mouseViewControl.getTransform() );
		requestUpdate();
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
	public final void paintWireframe( final Graphics g , final Camera3D camera , final int x , final int y , final int width , final int height )
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

				for ( RenderObject.Face face = ro._faces ; face != null ; face = face._next )
					paintWireframeFace( g , x , y , face );
			}
		}
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   g       Graphics context to paint on.
	 * @param   x       Origin X coordinate.
	 * @param   y       Origin Y coordinate.
	 * @param   face    Face to render.
	 */
	protected void paintWireframeFace( final Graphics g , final int x , final int y , final RenderObject.Face face )
	{
		final int[] vertexIndices = face._vi;
		if ( vertexIndices.length >= 3 )
		{
			final RenderObject ro = face.getRenderObject();
			final int[] vertexX = ro._ph;
			final int[] vertexY = ro._pv;

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
		_mouseViewControl.setControlMode( mode );
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
