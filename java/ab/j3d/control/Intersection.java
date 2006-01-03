/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import ab.j3d.Vector3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Face3D;

/**
 * An {@link Intersection} holds information about an intersection between a
 * line and an object. It holds a reference to the intersected face and it's
 * parent object, holds the location of the intersection (both in world and
 * local coordinates) and it holds the distance from the start of the
 * intersection line to the intersection point.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class Intersection
{
	/**
	 * The ID of the intersected object.
	 */
	private final Object _id;

	/**
	 * The intersected {@link Object3D}.
	 */
	private final Object3D _object;

	/**
	 * The intersected {@link Face3D}.
	 */
	private final Face3D _face;

	/**
	 * The distance from the start of the intersection line to the intersected
	 * face.
	 */
	private final double _intersectionDistance;

	/**
	 * The intersection point in world coordinates.
	 */
	private final Vector3D _worldIntersection;

	/**
	 * The intersection point in the intersected object's coordinate system.
	 */
	private final Vector3D _localIntersection;

	/**
	 * Construct new {@link Intersection}.
	 *
	 * @param   id                      The ID of the intersected object
	 * @param   face                    The intersected {@link Face3D}
	 * @param   intersectionDistance    The distance from the start of the
	 *                                  intersection line to the intersected
	 *                                  face.
	 * @param   worldIntersection       Intersection point in world coordinates
	 * @param   localIntersection       Intersection point in the object's
	 *                                  coordinate system.
	 */
	public Intersection( final Object id , final Face3D face , final double intersectionDistance , final Vector3D worldIntersection , final Vector3D localIntersection )
	{
		_id                   = id;
		_object               = face.getObject();
		_face                 = face;
		_intersectionDistance = intersectionDistance;
		_worldIntersection    = worldIntersection;
		_localIntersection    = localIntersection;
	}

	/**
	 * Returns the ID of the intersected object.
	 *
	 * @return  ID of the intersected object.
	 */
	public Object getID()
	{
		return _id;
	}

	/**
	 * Returns the intersected {@link Object3D}.
	 * 
	 * @return  Intersected {@link Object3D}.
	 */
	public Object3D getObject()
	{
		return _object;
	}

	/**
	 * Returns the intersected {@link Face3D}.
	 *
	 * @return  Intersected {@link Face3D}.
	 */
	public Face3D getFace()
	{
		return _face;
	}

	/**
	 * Returns the distance from the start of the intersection line to the
	 * intersected face.
	 *
	 * @return  Iistance from the start of the intersection line to the
	 *          intersected face.
	 */
	public double getIntersectionDistance()
	{
		return _intersectionDistance;
	}

	/**
	 * Returns the intersection point in world coordinates.
	 *
	 * @return  Intersection point in world coordinates.
	 */
	public Vector3D getWorldIntersection()
	{
		return _worldIntersection;
	}

	/**
	 * Returns the intersection point in the intersected object's coordinate
	 * system.
	 *
	 * @return  Intersection point in the coordinate system of the object.
	 */
	public Vector3D getLocalIntersection()
	{
		return _localIntersection;
	}

}
