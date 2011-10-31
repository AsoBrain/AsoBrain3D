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
package ab.j3d.control;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * This class can assist with dragging an object over a plane.
 * <p />
 * This will takes care of all projections and coordinate system conversions
 * required for the dragging operation. In the most basic application, the
 * coding using this class, just moves the object the new position returned
 * by {@link #getModelEnd}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PlaneMovementDragger
{
	/**
	 * Transforms model to world coordinates.
	 */
	private final Matrix3D _model2world;

	/**
	 * Plane across which dragging takes place. Note that this is not the same
	 * as the plane that was passed to the constructor; it has the same
	 * orientation of the original drag plane, but is translated to the
	 * intersection point that was passed to the constructor.
	 */
	private final Plane3D _wcsDragPlane;

	/**
	 * Transforms view to world coordinates.
	 */
	private final Matrix3D _view2world;

	/**
	 * Projector used to project 3D points on 2D image.
	 */
	private final Projector _projector;

	/**
	 * Translation from intersection point within object to object origin in
	 * world coordinates (WCS).
	 */
	private final Vector3D _wcsTranslation;

	/**
	 * End location of dragged object in the world coordinates (WCS).
	 *
	 * @see     #getWcsEnd
	 */
	private Vector3D _wcsEnd;

	/**
	 * Object drag movement relative the its original location in world
	 * coordinates (WCS).
	 *
	 * @see     #getWcsMovement
	 */
	private Vector3D _wcsMovement;

	/**
	 * Start location of dragged object in the world coordinates (WCS).
	 *
	 * @see     #getWcsStart
	 */
	private final Vector3D _wcsStart;

	/**
	 * End location of dragged object in model.
	 *
	 * @see     #getModelEnd
	 */
	private Vector3D _modelEnd;

	/**
	 * Object drag movement relative the its original location in the model.
	 *
	 * @see     #getModelMovement
	 */
	private Vector3D _modelMovement;

	/**
	 * Start location of dragged object in model.
	 *
	 * @see     #getModelStart
	 */
	private Vector3D _modelStart;

	/**
	 * Start drag operation based on the specified parameters.
	 *
	 * @param   model2world     Transforms model to world coordinates.
	 * @param   wcsDragPlane    Plane across which dragging takes place.
	 * @param   world2view      Transforms world to view coordinates.
	 * @param   projector       Projector used to project 3D points on 2D image.
	 * @param   intersection    Intersection between pointer and 3D object/face.
	 *
	 * @throws  NullPointerException if any argument is <code>null</code>.
	 */
	public PlaneMovementDragger( final Matrix3D model2world , final Plane3D wcsDragPlane , final Matrix3D world2view , final Projector projector , final Face3DIntersection intersection )
	{
		final Matrix3D object2world         = intersection.getObject2world();
		final Matrix3D view2world           = world2view.inverse();

		final Vector3D wcsObjectOrigin      = object2world.getTranslation();
		final Vector3D wcsIntersectionPoint = intersection.getIntersectionPoint();

		_view2world     = view2world;
		_projector      = projector;
		_wcsTranslation = wcsObjectOrigin.minus( wcsIntersectionPoint );
		_wcsDragPlane   = new BasicPlane3D( wcsDragPlane , wcsIntersectionPoint );
		_wcsStart       = wcsObjectOrigin;
		_wcsEnd         = wcsObjectOrigin;
		_model2world    = model2world;

		_wcsMovement   = null;
		_modelMovement = null;
		_modelEnd      = null;
		_modelStart    = null;
	}

	/**
	 * Handle dragging of mouse to the specified location.
	 *
	 * @param   x   X coordinate of mouse pointer.
	 * @param   y   Y coordinate of mouse pointer.
	 */
	public void dragTo( final int x , final int y )
	{
		final Ray3D pointerRay = _projector.getPointerRay( _view2world , (double)x , (double)y );

		final Vector3D wcsIntersection = GeometryTools.getIntersectionBetweenRayAndPlane( _wcsDragPlane , pointerRay );
		if ( wcsIntersection != null )
		{
			final Vector3D wcsEnd = wcsIntersection.plus( _wcsTranslation );
			if ( !wcsEnd.equals( _wcsEnd ) )
			{
				_wcsEnd        = wcsEnd;
				_wcsMovement   = null;
				_modelEnd      = null;
				_modelMovement = null;
			}
		}
	}

	/**
	 * Get end location of dragged object in model.
	 *
	 * @return  End location of object in model.
	 */
	public Vector3D getModelEnd()
	{
		Vector3D result = _modelEnd;
		if ( result == null )
		{
			result = _model2world.inverseTransform( _wcsEnd );
			_modelEnd = result;
		}

		return result;
	}

	/**
	 * Get object drag movement relative the its original location in the
	 * model.
	 *
	 * @return  Relative movement of object in model.
	 */
	public Vector3D getModelMovement()
	{
		Vector3D result = _modelMovement;
		if ( result == null )
		{
			final Vector3D start = _wcsStart;
			final Vector3D end   = _wcsEnd;

			result = _model2world.inverseRotate( end.x - start.x, end.y - start.y, end.z - start.z );
			_modelMovement = result;
		}

		return result;
	}

	/**
	 * Get start location of dragged object in model coordinates.
	 *
	 * @return  Start location of object in model.
	 */
	public Vector3D getModelStart()
	{
		Vector3D result = _modelStart;
		if ( result == null )
		{
			result = _model2world.inverseTransform( _wcsStart );
			_modelStart = result;
		}

		return result;
	}

	/**
	 * Get end location of dragged object in the world coordinate system
	 * (WCS).
	 *
	 * @return  End location of object in WCS.
	 */
	public Vector3D getWcsEnd()
	{
		return _wcsEnd;
	}

	/**
	 * Get object drag movement relative the its original location in the
	 * world coordinate system (WCS).
	 *
	 * @return  Relative movement of object in WCS.
	 */
	public Vector3D getWcsMovement()
	{
		Vector3D result = _wcsMovement;
		if ( result == null )
		{
			result = _wcsEnd.minus( _wcsStart );
			_wcsMovement = result;
		}

		return result;
	}

	/**
	 * Get start location of dragged object in the world coordinate system
	 * (WCS).
	 *
	 * @return  Start location of object in WCS.
	 */
	public Vector3D getWcsStart()
	{
		return _wcsStart;
	}
}
