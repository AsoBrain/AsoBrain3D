/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
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
	 * Scale factor from pixels to radians.
	 *
	 * @see     #getPixelsToRadiansFactor()
	 */
	private double _pixelsToRadiansFactor;

	/**
	 * Projection policy of this view.
	 */
	private ProjectionPolicy _projectionPolicy;

	/**
	 * Rendering policy of this view.
	 */
	private RenderingPolicy _renderingPolicy;

	/**
	 * Matrix that transforms scene to view coordinates.
	 */
	private Matrix3D _scene2view;

	/**
	 * Field of view (in radians). This only applies to perspective projections.
	 */
	private double _fieldOfView;

	/**
	 * Linear zoom factor. This factor use in combination with the image
	 * resolution and scene units to translated view units to pixels.
	 *
	 * @see     #getPixelsToUnitsFactor()
	 */
	private double _zoomFactor;

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
	 * View listeners.
	 */
	private final List<ViewListener> _viewListeners = new ArrayList<ViewListener>();

	/**
	 * Whether view frustum culling should be enabled, if supported by the view.
	 */
	private boolean _viewFrustumCulling = true;

	/**
	 * Whether dynamic level of detail should be enabled, if supported by the
	 * view.
	 */
	private boolean _levelOfDetail = true;

	/**
	 * Increase bounds of scene by this amount before calculating how to fit
	 * the scene in the view. This can be used when additional room around the
	 * viewed scene is desired.
	 *
	 * @see     #zoomToFitSceneBounds
	 */
	private Vector3D _zoomToFitSceneSizeAdjustment = Vector3D.ZERO;

	/**
	 * Adjustment to zoom factor when fitting bounds within a view. This can be
	 * used when additional room around the viewed bounds is desired.
	 *
	 * @see     #zoomToFitViewBounds
	 */
	private double _zoomToFitZoomAdjust = 0.95;

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

		_pixelsToRadiansFactor = DEFAULT_PIXELS_TO_RADIANS_FACTOR;

		_projectionPolicy = ProjectionPolicy.PERSPECTIVE;

		_renderingPolicy = RenderingPolicy.SOLID;

		_label = null;

		_fieldOfView = Math.toRadians( 45.0 );
		_zoomFactor = 1.0;

		_scene2view = Matrix3D.IDENTITY;

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

		_scene2view = Matrix3D.IDENTITY;
		_cameraControl = null;
		_label = null;
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
	 * Get multiplicative scale factor from image coordinates (pixels) to
	 * rotational units (radians).
	 *
	 * @return  Scale factor from pixels to radians.
	 *
	 * @see     #getResolution
	 */
	public double getPixelsToRadiansFactor()
	{
		return _pixelsToRadiansFactor;
	}

	/**
	 * Set factor from pixels to radians.
	 *
	 * @param   factor  Scale factor from pixels to radians (radians per pixel).
	 *
	 * @see     #getResolution
	 */
	public void setPixelsToRadiansFactor( final double factor )
	{
		_pixelsToRadiansFactor = factor;
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
	 * Get field of view. This only applies to perspective projections.
	 *
	 * @return  Field of view in radians.
	 */
	public double getFieldOfView()
	{
		return _fieldOfView;
	}

	/**
	 * Set field of view. This only applies to perspective projections.
	 *
	 * @param   fieldOfView     Field of view in radians.
	 */
	public void setFieldOfView( final double fieldOfView )
	{
		_fieldOfView = fieldOfView;
	}

	/**
	 * Get linear zoom factor. This factor use in combination with the image
	 * resolution and scene units to translated view units to pixels.
	 *
	 * @return  Linear zoom factor.
	 *
	 * @see     #getPixelsToUnitsFactor()
	 */
	public double getZoomFactor()
	{
		return _zoomFactor;
	}

	/**
	 * Set linear zoom factor. This factor use in combination with the image
	 * resolution and scene units to translated view units to pixels.
	 *
	 * @param   zoomFactor  Linear zoom factor.
	 *
	 * @see     #getPixelsToUnitsFactor()
	 */
	public void setZoomFactor( final double zoomFactor )
	{
		_zoomFactor = zoomFactor;
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
	 * Sets whether view frustum culling is enabled, if supported.
	 *
	 * @param   viewFrustumCulling  <code>true</code> to enable view frustum culling.
	 */
	public void setViewFrustumCulling( final boolean viewFrustumCulling )
	{
		_viewFrustumCulling = viewFrustumCulling;
	}

	/**
	 * Sets whether view frustum culling is enabled, if supported.
	 *
	 * @return  <code>true</code> if view frustum culling is enabled.
	 */
	public boolean isViewFrustumCulling()
	{
		return _viewFrustumCulling;
	}

	/**
	 * Returns whether the given object is visible in this view.
	 *
	 * @param   transform   Object-to-world transformation.
	 * @param   object      Object to be checked.
	 *
	 * @return  <code>true</code> if the object is visible.
	 */
	public boolean isVisible( final Matrix3D transform, final Object3D object )
	{
		boolean result = true;
		if ( isViewFrustumCulling() )
		{
			final Projector projector = getProjector();
			final Bounds3D obb = object.getOrientedBoundingBox();
			result = ( obb != null ) && projector.inViewVolume( transform.multiply( getScene2View() ), obb );
		}
		return result;
	}

	/**
	 * Returns whether (dynamic) level of detail is enabled.
	 *
	 * @return  <code>true</code> if level of detail is enabled.
	 */
	public boolean isLevelOfDetail()
	{
		return _levelOfDetail;
	}

	/**
	 * Sets whether (dynamic) level of detail is enabled.
	 *
	 * @param   levelOfDetail   <code>true</code> to enable level of detail.
	 */
	public void setLevelOfDetail( final boolean levelOfDetail )
	{
		_levelOfDetail = levelOfDetail;
	}

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
			final ViewControlInput controlInput = getControlInput();
			if ( controlInput != null )
			{
				if ( oldCameraControl != null )
				{
					controlInput.removeControlInputListener( oldCameraControl );
				}

				if ( cameraControl != null )
				{
					controlInput.addControlInputListener( cameraControl );
				}
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
		return _scene2view;
	}

	/**
	 * Set view transform.
	 *
	 * @param   scene2view  Scene to view transform.
	 */
	public void setScene2View( @NotNull final Matrix3D scene2view )
	{
		if ( !scene2view.equals( getScene2View() ) )
		{
			_scene2view = scene2view;
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
		return _scene2view.inverse();
	}

	/**
	 * Get graphical representation of view as AWT component.
	 *
	 * @return  Component that represents this view.
	 */
	public abstract Component getComponent();

	/**
	 * Returns the width of the view, in pixels.
	 *
	 * @return View width in pixels.
	 */
	protected int getWidth()
	{
		final Component viewComponent = getComponent();
		return viewComponent.getWidth();
	}

	/**
	 * Returns the height of the view, in pixels.
	 *
	 * @return View height in pixels.
	 */
	protected int getHeight()
	{
		final Component viewComponent = getComponent();
		return viewComponent.getHeight();
	}

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
	 * Get aspect ratio of view (component). This ratio is calculated by
	 * dividing the image width by the image height.
	 *
	 * @return  Aspect ratio.
	 */
	public double getAspectRatio()
	{
		return (double)getWidth() / (double)getHeight();
	}

	/**
	 * Test wether an animation is running in this view. If this is the case,
	 * the view should continuously be rendered, even if no explicit update was
	 * requested.
	 * <p>
	 * This simply returns the {@link Scene#isAnimated()} result.
	 *
	 * @return  <code>true</code> if animation is running;
	 *          <code>false</code> if view is static.
	 */
	public boolean isAnimationRunning()
	{
		final Scene scene = getScene();
		return scene.isAnimated();
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
	public abstract ViewControlInput getControlInput();

	/**
	 * Adds an {@link ViewOverlay} to this view. The overlay that is added
	 * first, will get the first turn in painting.
	 *
	 * @param   overlay     {@link ViewOverlay} to add.
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
	 * Returns whether the view has a {@link ViewOverlay}.
	 *
	 * @return  <code>true</code> if the view has an overlay.
	 */
	protected boolean hasOverlay()
	{
		return !_overlays.isEmpty();
	}

	/**
	 * Paints all registered overlay painters.
	 *
	 * @param   g2d     Graphics to paint to.
	 */
	protected void paintOverlay( @NotNull final Graphics2D g2d )
	{
		for ( final ViewOverlay overlay : _overlays )
		{
			overlay.paintOverlay( this, g2d );
		}
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

	/**
	 * Get scene size adjustment for zoom-to-fit operation. The scene bounds are
	 * increased by this amount before calculating how to fit the scene in the
	 * view. This can be used when additional room around the viewed scene is
	 * desired.
	 *
	 * @return  Scene size adjustment for zoom-to-fit operation.
	 *
	 * @see     #zoomToFitScene
	 * @see     #zoomToFitSceneBounds
	 */
	public Vector3D getZoomToFitSceneSizeAdjustment()
	{
		return _zoomToFitSceneSizeAdjustment;
	}

	/**
	 * Set scene size adjustment for zoom-to-fit operation. The scene bounds are
	 * increased by this amount before calculating how to fit the scene in the
	 * view. This can be used when additional room around the viewed scene is
	 * desired.
	 *
	 * @param   adjustment  Scene size adjustment for zoom-to-fit operation.
	 *
	 * @see     #zoomToFitScene
	 * @see     #zoomToFitSceneBounds
	 */
	public void setZoomToFitSceneSizeAdjustment( final Vector3D adjustment )
	{
		_zoomToFitSceneSizeAdjustment = adjustment;
	}

	/**
	 * Get adjustment to zoom factor when fitting bounds within a view. This can
	 * be used when additional room around the viewed bounds is desired.
	 *
	 * @return  Zoom factor adjustment factor (e.g. 0.95 to zoom to 95%).
	 *
	 * @see     #zoomToFitViewBounds
	 */
	public double getZoomToFitZoomAdjust()
	{
		return _zoomToFitZoomAdjust;
	}

	/**
	 * Set adjustment to zoom factor when fitting bounds within a view. This can
	 * be used when additional room around the viewed bounds is desired.
	 *
	 * @param   adjustment  Zoom factor adjustment factor (e.g. 0.95 to zoom to 95%).
	 *
	 * @see     #zoomToFitViewBounds
	 */
	public void setZoomToFitZoomAdjust( final double adjustment )
	{
		_zoomToFitZoomAdjust = adjustment;
	}

	/**
	 * Adjust view volume to fit all scene contents. This is done by changing
	 * the view position and zoom factor, but without changes to the view
	 * direction.
	 *
	 * @see     Scene#getBounds()
	 */
	public void zoomToFitScene()
	{
		final Scene scene = getScene();

		final Bounds3D sceneBounds = scene.getBounds();
		if ( sceneBounds != null )
		{
			zoomToFitSceneBounds( sceneBounds );
		}
	}

	/**
	 * Adjust view volume to fit the specified bounds in the scene's coordinate
	 * system. This is done by changing the view position and zoom factor, but
	 * without changes to the view direction.
	 *
	 * @param   sceneBounds     Bounds in scene to fit in view.
	 */
	public void zoomToFitSceneBounds( @NotNull final Bounds3D sceneBounds )
	{
		final Bounds3D adjustedBounds;

		final Vector3D adjustment = _zoomToFitSceneSizeAdjustment;
		if ( ( adjustment != null ) && !Vector3D.ZERO.equals( adjustment ) )
		{
			adjustedBounds = new Bounds3D( sceneBounds.v1.x - 0.5 * adjustment.x, sceneBounds.v1.y - 0.5 * adjustment.y, sceneBounds.v1.z - 0.5 * adjustment.z,
			                               sceneBounds.v2.x + 0.5 * adjustment.x, sceneBounds.v2.y + 0.5 * adjustment.y, sceneBounds.v2.z + 0.5 * adjustment.z );
		}
		else
		{
			adjustedBounds = sceneBounds;
		}

		zoomToFitViewBounds( GeometryTools.convertObbToAabb( getScene2View(), adjustedBounds ) );
	}

	/**
	 * Adjust view volume to fit the specified bounds in the view's locale
	 * coordinate system. This is done by changing the view position and zoom
	 * factor, but without changes to the view direction.
	 *
	 * @param   viewBounds  Bounds in view to fit in view.
	 */
	public void zoomToFitViewBounds( @NotNull final Bounds3D viewBounds )
	{
		final double viewSizeX = viewBounds.v2.x - viewBounds.v1.x;
		final double viewSizeY = viewBounds.v2.y - viewBounds.v1.y;

		final Scene scene = getScene();
		final double unscaledPixels2units = getResolution() / scene.getUnit();
		final double aspectRatio = getAspectRatio();
		final double fieldOfView = getFieldOfView();
		final double frontClipDistance = getFrontClipDistance();

		final double scaleX = (double)getWidth() / viewSizeX;
		final double scaleY = (double)getHeight() / viewSizeY;
		final double zoomFactor = Math.min( scaleX, scaleY ) * unscaledPixels2units;

		final double eyeDistance2width = 2.0 * Math.tan( fieldOfView / 2.0 );
		final double eyeDistance = Math.max( viewSizeX, aspectRatio * viewSizeY ) / eyeDistance2width;

		final double viewOriginX = 0.5 * ( viewBounds.v1.x + viewBounds.v2.x );
		final double viewOriginY = 0.5 * ( viewBounds.v1.y + viewBounds.v2.y );
		final double viewOriginZ = viewBounds.v2.z + Math.max( frontClipDistance, eyeDistance );

		final Matrix3D oldScene2View = getScene2View();
		final Matrix3D scene2view = oldScene2View.minus( viewOriginX, viewOriginY, viewOriginZ );

		setZoomFactor( _zoomToFitZoomAdjust * zoomFactor );
		setScene2View( scene2view );
	}

	public void contentNodeAdded( final SceneUpdateEvent event )
	{
		update();
	}

	public void contentNodeContentUpdated( final SceneUpdateEvent event )
	{
		update();
	}

	public void contentNodePropertyChanged( final SceneUpdateEvent event )
	{
		update();
	}

	public void contentNodeRemoved( final SceneUpdateEvent event )
	{
		update();
	}

	public void ambientLightChanged( final SceneUpdateEvent event )
	{
		update();
	}

	public void animationStarted( final SceneUpdateEvent event )
	{
		update();
	}

	public void animationStopped( final SceneUpdateEvent event )
	{
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

	/**
	 * Adds a view listener to the view.
	 *
	 * @param   listener    Listener to be added.
	 */
	public void addViewListener( final ViewListener listener )
	{
		_viewListeners.add( listener );
	}

	/**
	 * Removes a view listener to the view.
	 *
	 * @param   listener    Listener to be removed.
	 */
	public void removeViewListener( final ViewListener listener )
	{
		_viewListeners.remove( listener );
	}

	/**
	 * Notifies view listeners of a {@link ViewListener#beforeFrame} event.
	 */
	protected void fireBeforeFrameEvent()
	{
		for ( final ViewListener listener : _viewListeners )
		{
			listener.beforeFrame( this );
		}
	}

	/**
	 * Notifies view listeners of a {@link ViewListener#afterFrame} event.
	 */
	protected void fireAfterFrameEvent()
	{
		for ( final ViewListener listener : _viewListeners )
		{
			listener.afterFrame( this );
		}
	}

}
