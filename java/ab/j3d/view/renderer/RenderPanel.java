/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
import ab.j3d.view.DragEvent;
import ab.j3d.view.DragListener;
import ab.j3d.view.DragSupport;

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
	implements ComponentListener, DragListener
{
	/** Render mode: quick (wireframe). */ public static final int QUICK = 1;
	/** Render mode: full (solid).      */ public static final int FULL  = 2;

	/**
	 * Drag support for render panel.
	 */
	private final DragSupport _dragSupport;

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
		final Transform3D pointLightTransform = new Transform3D( Vector3D.INIT.set( -750.0 , -2500.0 , 1700.0 ) );
		world.addChild( pointLightTransform );
		pointLightTransform.addChild( pointLight );

		final Camera3D camera = new Camera3D( 300.0 , 60.0 );
		final Transform3D cameraTransform = new Transform3D( Vector3D.INIT.set( 0.0 , -3000.0 , 0.0 ) );
		cameraTransform .addChild( camera );
		world.addChild( cameraTransform );
		_camera = camera;

		final DragSupport dragSupport = new DragSupport( this , 0.001 );
		dragSupport.addDragListener( this );
		_dragSupport = dragSupport;

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
	}

	/**
	 * Place base in center of view.
	 */
	public final void center()
	{
		final Transform3D model       = _model;
		final Bounds3D    bounds      = _bounds;
		final Vector3D    translation = model.getTranslation();

		model.setTranslation( translation.set(
			-0.5 * ( bounds.v1.x + bounds.v2.x ) ,
			-0.5 * ( bounds.v1.y + bounds.v2.y ) ,
			-0.5 * ( bounds.v1.z + bounds.v2.z ) ) );
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
	 * Get string with view settings of renderer.
	 *
	 * @return  String with view settings of renderer.
	 */
	public final String getViewSettings()
	{
		return String.valueOf( Bounds3D.INIT.set(
			Vector3D.INIT.set(
				_dragSupport.getRotationX() ,
				_dragSupport.getRotationY() ,
				_dragSupport.getRotationZ() ) ,
			Vector3D.INIT.set(
				_dragSupport.getTranslationX() ,
				_dragSupport.getTranslationY() ,
				_dragSupport.getTranslationZ() )
			) );
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

	public void dragStart( final DragEvent event )
	{
		setRenderingMode( QUICK );
		requestUpdate();
	}

	public void dragTo( final DragEvent event )
	{
		_modelTransform.setMatrix( _dragSupport.getTransform() );
		requestUpdate();
	}

	public void dragStop( final DragEvent event )
	{
		setRenderingMode( FULL );
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
			final Graphics clipped = g.create( x , y , width , height );
			if ( _showTemporaryWireframe )
			{
				clipped.setColor( getForeground() );
				paintWireframe( clipped , _camera , 0 , 0 , width , height );
			}
			clipped.dispose();
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
		final Node3DCollection wireframeObjects = _wireframeObjects;
		wireframeObjects.clear();
		_camera.gatherLeafs( wireframeObjects , Object3D.class , Matrix3D.INIT , true );
		final int nrObjects = wireframeObjects.size();

		if ( nrObjects > 0 )
		{
			RenderObject[] wireframeRenderObjects = _wireframeRenderObjects;
			if ( ( wireframeRenderObjects == null ) || ( nrObjects > wireframeRenderObjects.length ) )
			{
				final RenderObject[] newWireframeRenderObjects = new RenderObject[ nrObjects ];
				if ( wireframeRenderObjects != null )
					System.arraycopy( wireframeRenderObjects , 0 , newWireframeRenderObjects , 0 , wireframeRenderObjects.length );

				wireframeRenderObjects = newWireframeRenderObjects;
				_wireframeRenderObjects = newWireframeRenderObjects;
			}

			/*
			 * Add all objects and draw them.
			 */
			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				RenderObject ro = wireframeRenderObjects[ i ];
				if ( ro == null )
					wireframeRenderObjects[ i ] = ro = new RenderObject();

				ro.set( (Object3D)wireframeObjects.getNode( i ) , wireframeObjects.getMatrix( i ) , camera.aperture , camera.zoom , width , height , true );
			}

			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				final RenderObject ro = wireframeRenderObjects[ i ];

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
			_modelTransform.setMatrix( _dragSupport.getTransform() );
//			System.out.println( "_modelTransform.getMatrix() = " + _modelTransform.getMatrix().toFriendlyString() );
			renderer.requestUpdate();
			repaint();
		}
	}

	/**
	 * Reset to default render view.
	 */
	public final void reset()
	{
		final DragSupport dragSupport = _dragSupport;
		dragSupport.setRotationX( -10.0 );
		dragSupport.setRotationY( 0.0 );
		dragSupport.setRotationZ( -35.0 );
		dragSupport.setTranslationX( 0.0 );
		dragSupport.setTranslationY( 0.0 );
		dragSupport.setTranslationZ( 0.0 );

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
		_dragSupport.setControlMode( mode );
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

				final DragSupport ds = _dragSupport;
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
}
