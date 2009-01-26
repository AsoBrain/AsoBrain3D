/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import ab.j3d.Matrix3D;
import ab.j3d.control.CameraControl;
import ab.j3d.control.Control;
import ab.j3d.control.ControlInput;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Transform3D;
import ab.j3d.view.Projector.ProjectionPolicy;

import com.numdata.oss.event.EventDispatcher;
import com.numdata.oss.ui.ActionTools;

/**
 * This class defines a view in the view model.
 *
 * @see     ViewModel
 * @see     CameraControl
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewModelView
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
	 * Model being viewed.
	 */
	private ViewModel _model;

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
	 * A {@link List} of {@link OverlayPainter}s that are to paint over this
	 * view after rendering is completed.
	 */
	private final List<OverlayPainter> _painters = new ArrayList<OverlayPainter>();

	/**
	 * Label of this view (<code>null</code> if none).
	 */
	private String _label;

	/**
	 * Render style filters.
	 */
	private final List<RenderStyleFilter> _renderStyleFilters = new ArrayList<RenderStyleFilter>();

	/**
	 * Grid enabled/disabled flag.
	 */
	private boolean _gridEnabled;

	/**
	 * Transforms grid to world coordinates.
	 */
	private Matrix3D _grid2wcs;

	/**
	 * Bounds of grid in cell units.
	 */
	private Rectangle _gridBounds;

	/**
	 * Size of each grid cell in world units.
	 */
	private int _gridCellSize;

	/**
	 * If set, highlight X/Y grid axes.
	 */
	private boolean _gridHighlightAxes;

	/**
	 * Interval for highlighted grid lines. Less or equal to zero if
	 * highlighting is disabled.
	 */
	private int _gridHighlightInterval;

	/**
	 * Construct new view.
	 *
	 * @param   model   Model being viewed.
	 */
	protected ViewModelView( final ViewModel model )
	{
		if ( model == null )
			throw new NullPointerException( "model" );

		_model = model;

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

		_gridEnabled           = false;
		_grid2wcs              = Matrix3D.INIT;
		_gridBounds            = new Rectangle( -100 , -100 , 200 , 200 );
		_gridCellSize          = (int)Math.round( 1.0 / model.getUnit() );
		_gridHighlightAxes     = true;
		_gridHighlightInterval = 10;

		appendRenderStyleFilter( new ViewModelViewStyleFilter() );
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
			ActionTools.addToToolBar( toolbar , cameraControl.getActions( locale ) );
		}

		ActionTools.addToToolBar( toolbar , getActions( locale ) );

		return toolbar;
	}

	/**
	 * Get model being viewed.
	 *
	 * @return  Model being viewed.
	 */
	public ViewModel getModel()
	{
		return _model;
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
	 * Get unit scale factor in meters per unit. This scale factor, when
	 * multiplied, converts design units to meters.
	 *
	 * @return  Unit scale (meters per unit).
	 */
	public double getUnit()
	{
		final ViewModel model = getModel();
		return model.getUnit();
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
	 * @see     ViewModel#getUnit
	 * @see     #getProjector
	 * @see     Projector#getView2pixels
	 */
	public double getPixelsToUnitsFactor()
	{
		final ViewModel model = getModel();
		return getResolution() / ( getZoomFactor() * model.getUnit() );
	}

	/**
	 * Get camera aperture for this view. This only applies to perspective
	 * projections.
	 *
	 * @return  Camera aperture in radians.
	 *
	 * @see     Camera3D#getAperture
	 */
	public final double getAperture()
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
	public final double getZoomFactor()
	{
		return _camera.getZoomFactor();
	}

	/**
	 * Get control for this view.
	 *
	 * @return  Control for this view.
	 */
	public final CameraControl getCameraControl()
	{
		return _cameraControl;
	}

	/**
	 * Set control for this view.
	 *
	 * @param   cameraControl     Control for this view.
	 */
	public final void setCameraControl( final CameraControl cameraControl )
	{
		final CameraControl oldCameraControl = getCameraControl();
		_cameraControl = cameraControl;

		if ( oldCameraControl != cameraControl )
		{
			if ( oldCameraControl != null )
				removeControl( oldCameraControl );

			if ( cameraControl != null )
				appendControl( cameraControl );
		}
	}

	/**
	 * Get view transform (tranforms model coordinates to view coordinates).
	 *
	 * @return  View transform.
	 */
	public final Matrix3D getViewTransform()
	{
		return _transform.getInverseTransform();
	}

	/**
	 * Get inverse view transform (tranforms view coordinates to model coordinates).
	 *
	 * @return  Inverse view transform.
	 */
	public final Matrix3D getInverseViewTransform()
	{
		return _transform.getTransform();
	}

	/**
	 * Set view transform (tranforms model coordinates to view coordinates).
	 *
	 * @param   transform   View transform.
	 *
	 * @throws  NullPointerException if <code>transform</code> is <code>null</code>.
	 */
	public final void setViewTransform( final Matrix3D transform )
	{
		if ( !transform.equals( getViewTransform() ) )
		{
			_transform.setInverseTransform( transform );
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

			result = ViewModel.INCH / (double)toolkit.getScreenResolution();
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
			throw new IllegalArgumentException( String.valueOf( resolution ) );

		_resolution = resolution;
	}

	/**
	 * Update contents of view. This may be the result of changes to the 3D
	 * scene or view transform.
	 */
	protected abstract void update();

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
		if ( policy != _projectionPolicy )
		{
			_projectionPolicy = policy;
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
		if ( policy != _renderingPolicy )
		{
			_renderingPolicy = policy;
			update();
		}
	}

	/**
	 * Get render style filters for this view.
	 *
	 * @return  Render style filters.
	 */
	public Collection<RenderStyleFilter> getRenderStyleFilters()
	{
		return Collections.unmodifiableList( _renderStyleFilters );
	}

	/**
	 * Add render style filters to this view.
	 *
	 * @param   styleFilter     Render style filter to add.
	 */
	public void appendRenderStyleFilter( final RenderStyleFilter styleFilter )
	{
		if ( styleFilter == null )
			throw new NullPointerException( "styleFilter" );

		_renderStyleFilters.add( styleFilter );
	}

	/**
	 * Returns the {@link Projector} for this View.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public abstract Projector getProjector();

	/**
	 * Returns the {@link ControlInput}, if this class has one. If it
	 * does not, <code>null</code> is returned.
	 *
	 * @return  The {@link ControlInput} for this view;
	 *          <code>null</code> if this view has none.
	 */
	protected abstract ControlInput getControlInput();

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
	public final void appendControl( final Control control )
	{
		final ControlInput controlInput = getControlInput();
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
	public final void insertControl( final Control control )
	{
		final ControlInput controlInput = getControlInput();
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
	public final void removeControl( final Control control )
	{
		final ControlInput controlInput = getControlInput();
		if ( controlInput != null )
		{
			final EventDispatcher eventQueue = controlInput.getEventDispatcher();
			eventQueue.removeFilter( control );
		}
	}

	/**
	 * Adds an {@link OverlayPainter} to the list of painters. The painter that
	 * is added first will get the first turn in painting.
	 *
	 * @param   painter     {@link OverlayPainter} to add.

	 * @see     #removeOverlayPainter
	 * @see     #hasOverlayPainters
	 * @see     #paintOverlay
	 */
	public void addOverlayPainter( final OverlayPainter painter )
	{
		if ( painter == null )
			throw new NullPointerException( "painter" );

		_painters.add( painter );
	}

	/**
	 * Removes an {@link OverlayPainter} from the list of painters.
	 *
	 * @param   painter     {@link OverlayPainter} to remove.

	 * @see     #addOverlayPainter
	 * @see     #hasOverlayPainters
	 * @see     #paintOverlay
	 */
	public void removeOverlayPainter( final OverlayPainter painter )
	{
		_painters.remove( painter );
	}

	/**
	 * Returns wether or not this view has registered {@link OverlayPainter}.
	 *
	 * @return  wether or not this view has registered {@link OverlayPainter}.
	 *
	 * @see     #addOverlayPainter
	 * @see     #removeOverlayPainter
	 * @see     #paintOverlay
	 */
	protected final boolean hasOverlayPainters()
	{
		return !_painters.isEmpty();
	}

	/**
	 * Iterates through all registered {@link OverlayPainter}s, and for each of
	 * them calls the {@link OverlayPainter#paint} method. This method should
	 * only be called by the view after rendering of the 3d scene has been
	 * completed.
	 *
	 * @param   g2d     {@link Graphics2D} object the painters can use to paint.

	 * @see     #addOverlayPainter
	 * @see     #hasOverlayPainters
	 * @see     #removeOverlayPainter
	 */
	protected final void paintOverlay( final Graphics2D g2d )
	{
		for ( final OverlayPainter painter : _painters )
			painter.paint( this , g2d );
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
		return new Action[] { new SwitchRenderingPolicyAction( locale , this , getRenderingPolicy() ) , new ToggleGridAction( locale , this ) };
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
	 * Get grid enabled option.
	 *
	 * @return  <code>true</code> if grid is enabled;
	 *          <code>false</code> if grid is disabled.
	 */
	public boolean isGridEnabled()
	{
		return _gridEnabled;
	}

	/**
	 * Get grid enabled option.
	 *
	 * @param   enabled     Grid enabled.
	 */
	public void setGridEnabled( final boolean enabled )
	{
		_gridEnabled = enabled;
	}

	/**
	 * Get tranform from grid to world coordinates.
	 *
	 * @return  Transform from grid to world coordinates.
	 */
	public Matrix3D getGrid2wcs()
	{
		return _grid2wcs;
	}

	/**
	 * Set tranform from grid to world coordinates.
	 *
	 * @param   grid2wcs    Transforms grid to world coordinates.
	 */
	public void setGrid2wcs( final Matrix3D grid2wcs )
	{
		_grid2wcs = grid2wcs;
	}

	/**
	 * Get bounds of grid in cell units.
	 *
	 * @return  Bounds of grid in cell units.
	 */
	public Rectangle getGridBounds()
	{
		return _gridBounds;
	}

	/**
	 * Set bounds of grid in cell units.
	 *
	 * @param   bounds  Bounds of grid in cell units.
	 */
	public void setGridBounds( final Rectangle bounds )
	{
		_gridBounds = bounds;
	}

	/**
	 * Get size of each grid cell in world units.
	 *
	 * @return  Size of each grid cell in world units.
	 */
	public int getGridCellSize()
	{
		return _gridCellSize;
	}

	/**
	 * Size of each grid cell in world units.
	 *
	 * @param   cellSize    Size of each grid cell in world units.
	 */
	public void setGridCellSize( final int cellSize )
	{
		_gridCellSize = cellSize;
	}

	/**
	 * Get X/Y grid axes highlight option.
	 *
	 * @return  <code>true</code> if X/Y grid axes are highlighted;
	 *          <code>false</code> otherwise.
	 */
	public boolean isGridHighlightAxes()
	{
		return _gridHighlightAxes;
	}

	/**
	 * Set X/Y grid axes highlight option.
	 *
	 * @param   highlightAxes   If set, highlight X/Y grid axes.
	 */
	public void setGridHighlightAxes( final boolean highlightAxes )
	{
		_gridHighlightAxes = highlightAxes;
	}

	/**
	 * Get interval for highlighted grid lines.
	 *
	 * @return  Interval for highlighted grid lines;
	 *          <= 0 if disabled.
	 */
	public int getGridHighlightInterval()
	{
		return _gridHighlightInterval;
	}

	/**
	 * Set interval for highlighted grid lines.
	 *
	 * @param   highlightInterval   Interval for highlighted grid lines
	 *                              (<= 0 to disable).
	 */
	public void setGridHighlightInterval( final int highlightInterval )
	{
		_gridHighlightInterval = highlightInterval;
	}
}
