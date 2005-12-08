/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest;

import java.awt.Component;

import ab.j3d.Vector3D;
import ab.j3d.control.Control;
import ab.j3d.view.FromToViewControl;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.ViewControl;

/**
 * The {@link View3D} provides a view on a {@link Model3D}. It has a
 * {@link Component} which displays this view. The view position can be changed
 * to a pre defined position using the method {@link #setViewType}. The type
 * of projection (parallel or perspective) can be changed with the method
 * {@link #setPerspective}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class View3D
{
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
	 * The {@link ViewModelView} that does the rendering.
	 */
	private final ViewModelView _view;

	/**
	 * The {@link Component} that displays the 3d scene.
	 */
	private final Component _component;

	/**
	 * The {@link ViewControl} that controls the camera position.
	 */
	private final FromToViewControl _viewControl;

	/**
	 * Construct new View3D.
	 *
	 * @param   model       The {@link Model3D} for which to create a view.
	 * @param   viewType    The type of view to create. Should be one of
	 *                      {@link #TOP_VIEW}, {@link #FRONT_VIEW},
	 *                      {@link #LEFT_VIEW} or {@link #PERSPECTIVE_VIEW}.
	 * @param   perspective Wether this view should have perspective projection.
	 */
	public View3D( final Model3D model , final int viewType , final boolean perspective)
	{
		_viewControl = new FromToViewControl( 100.0 );

		_view = model.createView( this , _viewControl);
		_component = _view.getComponent();

		setPerspective( perspective );
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
	 * Sets wether this view should have perspective projection
	 * (<code>true</code>) or parallel projection (<code>false</code>).
	 *
	 * @param   perspective     Wether this view should have perspective
	 *                          projection.
	 */
	public void setPerspective( final boolean perspective )
	{
		_view.setProjectionPolicy( perspective ? Projector.PERSPECTIVE : Projector.PARALLEL );
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
				from = Vector3D.INIT.set( 0.0 , 0.0 , 100.0 );
				break;

			case FRONT_VIEW :
				from = Vector3D.INIT.set( 0.0 , -100.0 , 0.0 );
				break;

			case LEFT_VIEW :
				from = Vector3D.INIT.set( -100.0 , 0.0 , 0.0 );
				break;

			case PERSPECTIVE_VIEW :
				from = Vector3D.INIT.set( -100.0 , -100.0 , 100.0 );
				break;

			default :
				throw new IllegalArgumentException( "The viewType should be one of TOP_VIEW, FRONT_VIEW, SIDE_VIEW or PERSPECTIVE_VIEW" );
		}

		_viewControl.setTo( Vector3D.INIT );
		_viewControl.setFrom( from );
	}

	/**
	 * Adds a {@link Control} to this {@link View3D}.
	 *
	 * @param   control     The {@link Control} to add.
	 */
	public void addControl( final Control control )
	{
		_view.addControl( control );
	}

	/**
	 * Removes a {@link Control} from this {@link View3D}.
	 *
	 * @param   control     The {@link Control} to remove.
	 */
	public void removeControl( final Control control )
	{
		_view.removeControl( control );
	}
}
