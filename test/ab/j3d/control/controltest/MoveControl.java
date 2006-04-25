/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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

import java.util.EventObject;
import java.util.List;

import ab.j3d.Vector3D;
import ab.j3d.control.Control;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.control.MouseControl;
import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.SceneElement;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.geom.BasicPlane3D;
import ab.j3d.model.Face3DIntersection;

/**
 * The {@link MoveControl} extends {@link Control} to handle events from a
 * {@link ab.j3d.view.ViewModelView}. It moves {@link TetraHedron}s when the
 * user drags the mouse while one is selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class MoveControl
	extends MouseControl
{
	/**
	 * Plane that defines the 'floor'.
	 */
	private static final BasicPlane3D FLOOR_PLANE = new BasicPlane3D( 0.0 , 0.0 , 1.0 , 0.0 , true );

	/**
	 * The {@link Model} that holds all objects in the scene.
	 */
	private Model _model;

	/**
	 * The difference between the mouse click location and the selected tetra
	 * hedron's location. Used when dragging, so that the hedron stays in the
	 * same place in relation to the mouse.
	 */
	private Vector3D _dragDifference;

	/**
	 * Construct new {@link MoveControl}.
	 *
	 * @param   model   {@link Model} that holds all objects in the scene.
	 */
	public MoveControl( final Model model )
	{
		_model = model;

		_dragDifference = Vector3D.INIT;
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		EventObject result = event;

		final SceneElement selection = _model.getSelection();
		if ( selection instanceof TetraHedron )
		{
			final TetraHedron selectedHedron = (TetraHedron)selection;

			final List intersections = event.getIntersections();
			if ( !intersections.isEmpty() )
			{
				final Face3DIntersection intersection = (Face3DIntersection)intersections.get( 0 );

				if ( selectedHedron == intersection.getObjectID() )
				{
					final Vector3D dragStart = event.getIntersectionWithPlane( FLOOR_PLANE );
					if ( dragStart != null )
					{
						final Vector3D hedronLocation = Vector3D.INIT.set( selectedHedron.getX() , selectedHedron.getY() , 0.0 );
						_dragDifference = hedronLocation.minus( dragStart );

						startCapture( event );
						result = null;
					}
				}
			}
		}

		return result;
	}

	public void mouseDragged( final ControlInputEvent event )
	{
		final SceneElement selection = _model.getSelection();
		if ( selection instanceof TetraHedron )
		{
			final TetraHedron selectedHedron = (TetraHedron)selection;

			final Vector3D dragPosition = event.getIntersectionWithPlane( FLOOR_PLANE );
			if ( dragPosition != null )
			{
				final Vector3D location = dragPosition.plus( _dragDifference );

				selectedHedron.setX( location.x );
				selectedHedron.setY( location.y );
			}
		}
	}
}
