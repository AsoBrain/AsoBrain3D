/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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
import java.beans.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import com.numdata.oss.event.*;
import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a 3D view of a {@link Scene}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class View3D
	implements SceneUpdateListener
{
	/**
	 * Default scale factor from pixels to radians. By default, this set to make
	 * a full circle by moving the mouse cursor 250 pixels (in no particular
	 * direction).
	 *
	 * @see     #getPixelsToRadiansFactor()
	 */
	public static final double DEFAULT_PIXELS_TO_RADIANS_FACTOR = ( 2.0 *  Math.PI ) / 250.0;

	/**
	 * Bound property name: rendering policy.
	 */
	public static final String RENDERING_POLICY_PROPERTY = "renderingPolicy";

	/**
	 * Bound property name: projection policy.
	 */
	public static final String PROJECTION_POLICY_PROPERTY = "projectionPolicy";

	/**
	 * Scene being viewed.
	 */
	private final Scene _scene;

	/**
	 * Resolution of image in meters per pixel. If is set to <code>0.0</code>,
	 * the resolution will be determined automatically.
	 *
	 * @see     #getResolution
	 * @see     #setResolution
	 */
	private double _resolution;

	/**
	 * Projection policy of this view.
	 */
	private ProjectionPolicy _projectionPolicy;

	/**
	 * Rendering policy of this view.
	 */
	private RenderingPolicy _renderingPolicy;

	/**
	 * Transformation of view.
	 */
	private Transform3D _transform;

	/**
	 * Camera from where the view is created.
	 */
	private final Camera3D _camera;

	/**
	 * Control for this view.
	 */
	private CameraControl _cameraControl;

	/**
	 * A {@link List} of {@link ViewOverlay}s that are to paint over this
	 * view after rendering is completed.
	 */
	private final List<ViewOverlay> _overlays = new ArrayList<ViewOverlay>();

	/**
	 * Label of this view (<code>null</code> if none).
	 */
	private String _label;

	/**
	 * Render style filters.
	 */
	private final List<RenderStyleFilter> _renderStyleFilters = new ArrayList<RenderStyleFilter>();

	/**
	 * Grid to be shown (when enabled).
	 */
	private final Grid _grid;

	/**
	 * Background of the view.
	 */
	private final Background _background;

	/**
	 * Provides support for bound properties.
	 */
	protected final PropertyChangeSupport _pcs = new PropertyChangeSupport( this );

	/**
	 * Construct new view.
	 *
	 * @param   scene   Scene to view.
	 */
	protected View3D( @NotNull final Scene scene )
	{
		scene.addSceneUpdateListener( this );
		_scene = scene;

		_resolution = 0.0;

		_projectionPolicy = ProjectionPolicy.PERSPECTIVE;

		_renderingPolicy = RenderingPolicy.SOLID;

		_label = null;

		final Camera3D camera = new Camera3D();
		_camera = camera;

		final Transform3D transform = new Transform3D();
		transform.addChild( camera );
		_transform = transform;

		_cameraControl = null;

		final Grid grid = new Grid();
		grid.setCellSize( (int)Math.round( 1.0 / scene.getUnit() ) );
		_grid = grid;

		_background = Background.createDefault();

		appendRenderStyleFilter( new ViewStyleFilter() );
	}

	/**
	 * Dispose this view. This releases resources used by this view.
	 */
	public void dispose()
	{
		for ( final ViewOverlay overlay : new ArrayList<ViewOverlay>( _overlays ) )
		{
			removeOverlay( overlay );
		}

		_renderStyleFilters.clear();

		_transform = null;
		_cameraControl = null;
		_label = null;
	}

	/**
	 * Create tool bar to control this view.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 *
	 * @return  Tool bar.
	 */
	public JToolBar createToolBar( final Locale locale )
	{
		final String label = getLabel();

		final JToolBar toolbar = new JToolBar( label );

		if ( label != null )
		{
			toolbar.add( new JLabel( getLabel() + ": " ) );
		}

		final CameraControl cameraControl = getCameraControl();
		if ( cameraControl != null )
		{
			ActionTools.addToToolBar( toolbar, cameraControl.getActions( locale ) );
		}

		ActionTools.addToToolBar( toolbar, getActions( locale ) );

		return toolbar;
	}

	/**
	 * Sets the background of the view.
	 *
	 * @param   background  Background to be set.
	 */
	public void setBackground( @NotNull final Background background )
	{
		_background.set( background );
	}

	/**
	 * Returns the grid shown in the view (when enabled).
	 *
	 * @return  Grid.
	 */
	public Grid getGrid()
	{
		return _grid;
	}

	/**
	 * Returns the background of the view.
	 *
	 * @return  Background.
	 */
	public Background getBackground()
	{
		return _background;
	}

	/**
	 * Get scene being viewed.
	 *
	 * @return  View being viewed.
	 */
	public Scene getScene()
	{
		return _scene;
	}

	/**
	 * Get camera from where the view is created.
	 *
	 * @return  Camera from where the view is created (never <code>null</code>).
	 */
	public Camera3D getCamera()
	{
		return _camera;
	}

	/**
	 * Get multiplicative scale factor from image coordinates (pixels) to
	 * rotational units (radians).
	 *
	 * @return  Scale factor from pixels to radians.
	 *
	 * @see     #getResolution
	 */
	public double getPixelsToRadiansFactor()
	{
		return DEFAULT_PIXELS_TO_RADIANS_FACTOR;
	}

	/**
	 * Get multiplicative scale factor from image coordinates (pixels) to view
	 * coordinates (units).
	 *
	 * @return  Scale factor from pixels to view units.
	 *
	 * @see     Scene#getUnit
	 * @see     #getProjector
	 * @see     Projector#getView2pixels
	 */
	public double getPixelsToUnitsFactor()
	{
		final Scene scene = getScene();
		return getResolution() / ( getZoomFactor() * scene.getUnit() );
	}

	/**
	 * Get camera aperture for this view. This only applies to perspective
	 * projections.
	 *
	 * @return  Camera aperture in radians.
	 *
	 * @see     Camera3D#getAperture
	 */
	public double getAperture()
	{
		return _camera.getAperture();
	}

	/**
	 * Get linear zoom factor. View units are multiplied by this factor to get
	 * rendered units.
	 *
	 * @return  Linear zoom factor.
	 *
	 * @see     Camera3D#getZoomFactor
	 */
	public double getZoomFactor()
	{
		return _camera.getZoomFactor();
	}

	/**
	 * Get linear zoom factor. View units are multiplied by this factor to get
	 * rendered units.
	 *
	 * @param   zoomFactor  Linear zoom factor.
	 *
	 * @see     Camera3D#setZoomFactor
	 */
	public void setZoomFactor( final double zoomFactor )
	{
		_camera.setZoomFactor( zoomFactor );
	}

	/**
	 * Returns the distance between the camera and the front clipping plane,
	 * in view units. The distance is measured in the viewing direction. Any
	 * objects closer to the camera than the front clipping plane are invisible.
	 *
	 * <p>For perspective projections, this distance should always be positive.
	 * Parallel projections may have a zero or negative clipping distance,
	 * though some view implementations may not support it.
	 *
	 * @return  Distance from the camera to the front clipping plane,
	 *          in view units.
	 */
	public abstract double getFrontClipDistance();

	/**
	 * Sets the distance between the camera and the front clipping plane,
	 * in view units. The distance is measured in the viewing direction. Any
	 * objects closer to the camera than the front clipping plane are invisible.
	 *
	 * <p>For perspective projections, this distance should always be positive.
	 * Parallel projections may have a zero or negative clipping distance,
	 * though some view implementations may not support it.
	 *
	 * @param   front   Distance from the camera to the front clipping plane,
	 *                  in view units.
	 */
	public abstract void setFrontClipDistance( double front );

	/**
	 * Returns the distance between the camera and the back clipping plane,
	 * in view units. The distance is measured in the viewing direction. Any
	 * objects further away from the camera than the back clipping plane are
	 * invisible.
	 *
	 * <p>This value must be greater than the front clipping plane distance,
	 * given by {@link #getFrontClipDistance()}.
	 *
	 * @return  Distance from the camera to the back clipping plane,
	 *          in view units.
	 */
	public abstract double getBackClipDistance();

	/**
	 * Returns the distance between the camera and the back clipping plane,
	 * in view units. The distance is measured in the viewing direction. Any
	 * objects further away from the camera than the back clipping plane are
	 * invisible.
	 *
	 * <p>This value must be greater than the front clipping plane distance,
	 * given by {@link #getFrontClipDistance()}.
	 *
	 * @param   back    Distance from the camera to the back clipping plane,
	 *                  in view units.
	 */
	public abstract void setBackClipDistance( double back );

	/**
	 * Get control for this view.
	 *
	 * @return  Control for this view.
	 */
	public CameraControl getCameraControl()
	{
		return _cameraControl;
	}

	/**
	 * Set control for this view.
	 *
	 * @param   cameraControl     Control for this view.
	 */
	public void setCameraControl( final CameraControl cameraControl )
	{
		final CameraControl oldCameraControl = getCameraControl();
		_cameraControl = cameraControl;

		if ( oldCameraControl != cameraControl )
		{
			if ( oldCameraControl != null )
			{
				removeControl( oldCameraControl );
			}

			if ( cameraControl != null )
			{
				appendControl( cameraControl );
			}
		}
	}

	/**
	 * Get view transform.
	 *
	 * @return  Scene to view transform.
	 */
	public Matrix3D getScene2View()
	{
		return _transform.getInverseTransform();
	}

	/**
	 * Set view transform.
	 *
	 * @param   scene2view  Scene to view transform.
	 *
	 * @throws  NullPointerException if <code>transform</code> is <code>null</code>.
	 */
	public void setScene2View( final Matrix3D scene2view )
	{
		if ( !scene2view.equals( getScene2View() ) )
		{
			_transform.setTransform( scene2view.inverse() );
			update();
		}
	}

	/**
	 * Get view transform.
	 *
	 * @return  View to scene transform.
	 */
	public Matrix3D getView2Scene()
	{
		return _transform.getTransform();
	}

	/**
	 * Set view transform.
	 *
	 * @param   view2scene  Scene to view transform.
	 *
	 * @throws  NullPointerException if <code>transform</code> is <code>null</code>.
	 */
	public void setView2scene( final Matrix3D view2scene )
	{
		if ( !view2scene.equals( getView2Scene() ) )
		{
			_transform.setTransform( view2scene );
			update();
		}
	}

	/**
	 * Get graphical representation of view as AWT component.
	 *
	 * @return  Component that represents this view.
	 */
	public abstract Component getComponent();

	/**
	 * Get resolution of image in meters per pixel.
	 *
	 * @return  Resolution of image in meters per pixel.
	 */
	public double getResolution()
	{
		double result = _resolution;

		if ( result == 0.0 )
		{
			final Component component = getComponent();
			final Toolkit   toolkit   = ( component != null ) ? component.getToolkit() : Toolkit.getDefaultToolkit();

			result = Scene.INCH / (double)toolkit.getScreenResolution();
		}

		return result;
	}

	/**
	 * Set resolution of image in meters per pixel. If is set to
	 * <code>0.0</code>, the resolution will be determined automatically.
	 *
	 * @param   resolution  Resolution in meters per pixel, 0.0 if automatic.
	 */
	public void setResolution( final double resolution )
	{
		if ( ( resolution < 0.0 ) || Double.isNaN( resolution ) )
		{
			throw new IllegalArgumentException( String.valueOf( resolution ) );
		}

		_resolution = resolution;
	}

	/**
	 * Update contents of view. This may be the result of changes to the 3D
	 * scene or view transform.
	 */
	public abstract void update();

	/**
	 * Get projection policy of this view.
	 *
	 * @return  Projection policy.
	 */
	public ProjectionPolicy getProjectionPolicy()
	{
		return _projectionPolicy;
	}

	/**
	 * Set projection policy of this view.
	 *
	 * @param   policy  Projection policy.
	 */
	public void setProjectionPolicy( final ProjectionPolicy policy )
	{
		final ProjectionPolicy oldValue = _projectionPolicy;
		if ( policy != oldValue )
		{
			_projectionPolicy = policy;
			_pcs.firePropertyChange( PROJECTION_POLICY_PROPERTY, oldValue, policy );
			update();
		}
	}

	/**
	 * Get rendering policy of this view.
	 *
	 * @return  Projection policy.
	 */
	public RenderingPolicy getRenderingPolicy()
	{
		return _renderingPolicy;
	}

	/**
	 * Set rendering policy of this view.
	 *
	 * @param   policy  Rendering policy.
	 */
	public void setRenderingPolicy( final RenderingPolicy policy )
	{
		final RenderingPolicy oldValue = _renderingPolicy;
		if ( policy != oldValue )
		{
			_renderingPolicy = policy;
			_pcs.firePropertyChange( RENDERING_POLICY_PROPERTY, oldValue, policy );
			update();
		}
	}

	/**
	 * Get render style filters for this view.
	 *
	 * @return  Render style filters.
	 */
	@NotNull
	public Collection<RenderStyleFilter> getRenderStyleFilters()
	{
		return Collections.unmodifiableList( _renderStyleFilters );
	}

	/**
	 * Add render style filters to this view.
	 *
	 * @param   styleFilter     Render style filter to add.
	 */
	public void appendRenderStyleFilter( @NotNull final RenderStyleFilter styleFilter )
	{
		_renderStyleFilters.add( styleFilter );
	}

	/**
	 * Remove render style filters from this view.
	 *
	 * @param   styleFilter     Render style filter to remove.
	 */
	public void removeRenderStyleFilter( @NotNull final RenderStyleFilter styleFilter )
	{
		if ( !_renderStyleFilters.remove( styleFilter ) )
		{
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Removes all render style filters from the view.
	 */
	public void clearRenderStyleFilters()
	{
		_renderStyleFilters.clear();
	}

	/**
	 * Returns the {@link Projector} for this View.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public abstract Projector getProjector();

	/**
	 * Returns the {@link ViewControlInput}, if this class has one. If it
	 * does not, <code>null</code> is returned.
	 *
	 * @return  The {@link ViewControlInput} for this view;
	 *          <code>null</code> if this view has none.
	 */
	protected abstract ViewControlInput getControlInput();

	/**
	 * Adds a {@link Control} to the end of the control chain of this view.
	 * the list of controls.
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Not all views support user input. If not, calls to this method may
	 *   be ignored.</dd>
	 * </dl>
	 *
	 * @param   control     The {@link Control} to add
	 */
	public void appendControl( final Control control )
	{
		final ViewControlInput controlInput = getControlInput();
		if ( controlInput != null )
		{
			final EventDispatcher eventQueue = controlInput.getEventDispatcher();
			eventQueue.appendFilter( control );
		}
	}

	/**
	 * Inserts a {@link Control} at the start of the control chain of this view.
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Not all views support user input. If not, calls to this method may
	 *   be ignored.</dd>
	 * </dl>
	 *
	 * @param   control     The {@link Control} to add.
	 */
	public void insertControl( final Control control )
	{
		final ViewControlInput controlInput = getControlInput();
		if ( controlInput != null )
		{
			final EventDispatcher eventQueue = controlInput.getEventDispatcher();
			eventQueue.insertFilter( control );
		}
	}

	/**
	 * Removes a {@link Control} from the list of controls.
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Not all views support user input. If not, calls to this method may
	 *   be ignored.</dd>
	 * </dl>
	 *
	 * @param   control     The Control to remove
	 */
	public void removeControl( final Control control )
	{
		final ViewControlInput controlInput = getControlInput();
		if ( controlInput != null )
		{
			final EventDispatcher eventQueue = controlInput.getEventDispatcher();
			eventQueue.removeFilter( control );
		}
	}

	/**
	 * Adds an {@link ViewOverlay} to this view. The overlay that is added
	 * first, will get the first turn in painting.
	 *
	 * @param   overlay     {@link ViewOverlay} to add.

	 * @see     #removeOverlay
	 * @see     #hasOverlay
	 * @see     #paintOverlay
	 */
	public void addOverlay( @NotNull final ViewOverlay overlay )
	{
		final List<ViewOverlay> overlays = _overlays;
		if ( overlays.contains( overlay ) )
		{
			throw new IllegalArgumentException();
		}

		overlay.addView( this );

		overlays.add( overlay );
	}

	/**
	 * Removes an {@link ViewOverlay} from this view.
	 *
	 * @param   overlay     {@link ViewOverlay} to remove.

	 * @see     #addOverlay
	 * @see     #hasOverlay
	 * @see     #paintOverlay
	 */
	public void removeOverlay( @NotNull final ViewOverlay overlay )
	{
		final List<ViewOverlay> overlays = _overlays;
		if ( !overlays.remove( overlay ) )
		{
			throw new IllegalArgumentException();
		}

		overlay.removeView( this );
	}

	/**
	 * Returns wether or not this view has registered {@link ViewOverlay}.
	 *
	 * @return  wether or not this view has registered {@link ViewOverlay}.
	 *
	 * @see     #addOverlay
	 * @see     #removeOverlay
	 * @see     #paintOverlay
	 */
	protected boolean hasOverlay()
	{
		return !_overlays.isEmpty();
	}

	/**
	 * Iterates through all registered {@link ViewOverlay}s, and for each of
	 * them calls the {@link ViewOverlay#paintOverlay} method. This method should
	 * only be called by the view after rendering of the 3d scene has been
	 * completed.
	 *
	 * @param   g2d     {@link Graphics2D} object the painters can use to paint.

	 * @see     #addOverlay
	 * @see     #hasOverlay
	 * @see     #removeOverlay
	 */
	protected void paintOverlay( @NotNull final Graphics2D g2d )
	{
		for ( final ViewOverlay overlay : _overlays )
		{
			overlay.paintOverlay( this, g2d );
		}
	}

	/**
	 * Get actions of the view.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 *
	 * @return  Actions of the view.
	 */
	public Action[] getActions( final Locale locale )
	{
		return new Action[] { new SwitchRenderingPolicyAction( locale, this, getRenderingPolicy() ), new ToggleGridAction( locale, this ) };
	}


	/**
	 * Get label of view.
	 *
	 * @return  Label of view;
	 *          <code>null</code> if label has no label.
	 */
	public String getLabel()
	{
		return _label;
	}

	/**
	 * Set label of view.
	 *
	 * @param   label   Label of view (<code>null</code> to remove label).
	 */
	public void setLabel( final String label )
	{
		_label = label;
	}

	@Override
	public void contentNodeAdded( final SceneUpdateEvent event )
	{
		update();
	}

	@Override
	public void contentNodeContentUpdated( final SceneUpdateEvent event )
	{
		update();
	}

	@Override
	public void contentNodePropertyChanged( final SceneUpdateEvent event )
	{
		update();
	}

	@Override
	public void contentNodeRemoved( final SceneUpdateEvent event )
	{
		update();
	}

	@Override
	public void ambientLightChanged( final SceneUpdateEvent event )
	{
		update();
	}

	/**
	 * Add a property change listener.
	 *
	 * @see     PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 *
	 * @param   listener    Listener to be added.
	 */
	public void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( listener );
	}

	/**
	 * Add a property change listener for a specific property.
	 *
	 * @see     PropertyChangeSupport#addPropertyChangeListener(String,PropertyChangeListener)
	 *
	 * @param   propertyName    Name of the property.
	 * @param   listener        Listener to be added.
	 */
	public void addPropertyChangeListener( final String propertyName, final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( propertyName, listener );
	}
}
