/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.control.controltest.model.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;

/**
 * The {@link MoveControl} extends {@link Control} to handle events from a
 * {@link ab.j3d.view.View3D}. It moves {@link TetraHedron}s when the
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
	private static final BasicPlane3D FLOOR_PLANE = new BasicPlane3D( 0.0, 0.0, 1.0, 0.0, true );

	/**
	 * The {@link Model} that holds all objects in the scene.
	 */
	private final Model _model;

	/**
	 * The difference between the mouse click location and the selected tetra
	 * hedron's location. Used when dragging, so that the hedron stays in the
	 * same place in relation to the mouse.
	 */
	private PlaneMovementDragger _dragger;

	/**
	 * Construct new {@link MoveControl}.
	 *
	 * @param   model   {@link Model} that holds all objects in the scene.
	 */
	public MoveControl( final Model model )
	{
		_model   = model;
		_dragger = null;
	}

	@Override
	public void mousePressed( final ControlInputEvent event )
	{
		final SceneElement selection = _model.getSelection();
		if ( selection instanceof TetraHedron )
		{
			final TetraHedron selectedHedron = (TetraHedron)selection;

			final List<Face3DIntersection> intersections = event.getIntersections();
			if ( !intersections.isEmpty() )
			{
				final Face3DIntersection intersection = intersections.get( 0 );
				if ( selectedHedron == intersection.getObjectID() )
				{
					_dragger = new PlaneMovementDragger( Matrix3D.IDENTITY, FLOOR_PLANE, event.getScene2View(), event.getProjector(), intersection );
					startCapture( event );
				}
			}
		}
	}

	@Override
	public void mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			final SceneElement selection = _model.getSelection();
			if ( selection instanceof TetraHedron )
			{
				final TetraHedron selectedHedron = (TetraHedron)selection;

				final PlaneMovementDragger dragger = _dragger;
				dragger.dragTo( event.getX(), event.getY() );
				final Vector3D newPosition = dragger.getModelEnd();

				selectedHedron.setLocation( newPosition.x, newPosition.y );
			}
		}
	}
}
