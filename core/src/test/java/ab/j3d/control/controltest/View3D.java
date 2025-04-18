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
package ab.j3d.control.controltest;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.view.*;

/**
 * The {@link View3D} provides a view on a {@link Model3D}. It has a
 * {@link Component} which displays this view. The view position can be changed
 * to a pre defined position using the method {@link #setViewType}. The type
 * of projection (parallel or perspective) can be changed with the method
 * {@link #setProjection}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class View3D
{
	/**
	 * Use perspective projection.
	 *
	 * @see #setProjection
	 */
	public static final int PERSPECTIVE_PROJECTION = 1;

	/**
	 * Use parallel projection.
	 *
	 * @see #setProjection
	 */
	public static final int PARALLEL_PROJECTION = 2;

	/**
	 * A view from above, giving an overview of the x,y plane.
	 */
	public static final int TOP_VIEW = 0;

	/**
	 * A view from the front, giving an overview of the x,z plane.
	 */
	public static final int FRONT_VIEW = 1;

	/**
	 * A view from the left, giving an overview of the y,z plane.
	 */
	public static final int LEFT_VIEW = 2;

	/**
	 * An overview view of the scene.
	 */
	public static final int PERSPECTIVE_VIEW = 3;

	/**
	 * The {@link ab.j3d.view.View3D} that does the rendering.
	 */
	private final ab.j3d.view.View3D _view;

	/**
	 * The {@link Component} that displays the 3d scene.
	 */
	private final Component _component;

	/**
	 * The {@link CameraControl} that controls the camera position.
	 */
	private final FromToCameraControl _cameraControl;

	/**
	 * Construct new View3D.
	 *
	 * @param   model       The {@link Model3D} for which to create a view.
	 * @param   viewType    The type of view to create. Should be one of
	 *                      {@link #TOP_VIEW}, {@link #FRONT_VIEW},
	 *                      {@link #LEFT_VIEW} or {@link #PERSPECTIVE_VIEW}.
	 * @param   perspective Whether this view should have perspective projection.
	 */
	public View3D( final Model3D model , final int viewType , final int perspective)
	{
		_view = model.createView();
		_cameraControl = new FromToCameraControl( _view , 100.0 );
		_view.setCameraControl( _cameraControl );

		_component = _view.getComponent();

		setProjection( perspective );
		setViewType( viewType );
	}

	/**
	 * The {@link Component} that holds the 3d view.
	 *
	 * @return  The {@link Component} that holds the 3d view.
	 */
	public Component getComponent()
	{
		return _component;
	}

	/**
	 * Sets whether this view should have perspective projection
	 * (<code>true</code>) or parallel projection (<code>false</code>).
	 *
	 * @param   projection  Whether this view should have perspective projection.
	 */
	public void setProjection( final int projection )
	{
		if ( PERSPECTIVE_PROJECTION == projection )
		{
			_view.setProjectionPolicy( ProjectionPolicy.PERSPECTIVE );
		}
		else if ( PARALLEL_PROJECTION == projection )
		{
			_view.setProjectionPolicy( ProjectionPolicy.PARALLEL );
			_view.setZoomFactor( 0.004 );
		}
		else
		{
			throw new IllegalArgumentException( "projection has to been one of PERSPECTIVE or PARALLEL" );
		}
	}

	/**
	 * Sets the position of the camera to one of the pre-defined view types.
	 * <code>viewType</code> should be one of {@link #TOP_VIEW},
	 * {@link #FRONT_VIEW}, {@link #LEFT_VIEW} or {@link #PERSPECTIVE_VIEW}.
	 *
	 * @param   viewType    The type of view to set. Should be one of
	 *                      {@link #TOP_VIEW}, {@link #FRONT_VIEW},
	 *                      {@link #LEFT_VIEW} or {@link #PERSPECTIVE_VIEW}.
	 */
	public void setViewType( final int viewType )
	{
		final Vector3D from;

		switch( viewType ){

			case TOP_VIEW :
				from = new Vector3D( 0.0 , 0.0 , 200.0 );
				break;

			case FRONT_VIEW :
				from = new Vector3D( 0.0 , -200.0 , 0.0 );
				break;

			case LEFT_VIEW :
				from = new Vector3D( -200.0 , 0.0 , 0.0 );
				break;

			case PERSPECTIVE_VIEW :
				from = new Vector3D( -100.0 , -100.0 , 100.0 );
				break;

			default :
				throw new IllegalArgumentException( "The viewType should be one of TOP_VIEW, FRONT_VIEW, SIDE_VIEW or PERSPECTIVE_VIEW" );
		}

		_cameraControl.look( from, Vector3D.ZERO );
	}

	/**
	 * Adds a {@link Control} to this {@link View3D}.
	 *
	 * @param   control     The {@link Control} to add.
	 */
	public void insertControl( final Control control )
	{
		final ViewControlInput controlInput = _view.getControlInput();
		if ( controlInput != null )
		{
			controlInput.insertControlInputListener( control );
		}
	}

	/**
	 * Removes a {@link Control} from this {@link View3D}.
	 *
	 * @param   control     The {@link Control} to remove.
	 */
	public void removeControl( final Control control )
	{
		final ViewControlInput controlInput = _view.getControlInput();
		if ( controlInput != null )
		{
			controlInput.removeControlInputListener( control );
		}
	}
}
