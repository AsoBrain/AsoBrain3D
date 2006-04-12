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
package ab.j3d.model;

import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * An {@link Face3DIntersection} holds information about an intersection between a
 * line and an object. It holds a reference to the intersected face and it's
 * parent object, holds the location of the intersection (both in world and
 * local coordinates) and it holds the distance from the start of the
 * intersection line to the intersection point.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class Face3DIntersection
	implements Comparable
{
	/**
	 * ID of intersected object.
	 */
	private final Object _objectID;

	/**
	 * Transforms object to world coordinates.
	 */
	private final Matrix3D _object2world;

	/**
	 * The {@link Face3D} that was intersected.
	 */
	private final Face3D _face;

	/**
	 * Intersection point in world coordinates.
	 */
	private final Vector3D _intersectionPoint;

	/**
	 * Distance between reference and intersection point.
	 */
	private final double _distance;

	/**
	 * Construct new intersection information object.
	 *
	 * @param   objectID            ID of intersected object.
	 * @param   object2world        Transforms object to world coordinates.
	 * @param   face                The {@link Face3D} that was intersected.
	 * @param   intersectionPoint   Intersection point in world coordinates.
	 * @param   distance            Distance between reference and intersection point.
	 */
	public Face3DIntersection( final Object objectID , final Matrix3D object2world ,  final Face3D face , final Vector3D intersectionPoint , final double distance )
	{
		_objectID          = objectID;
		_object2world       = object2world;
		_face              = face;
		_intersectionPoint = intersectionPoint;
		_distance          = distance;
	}

	public void addSortedByDistance( final List result )
	{
		final double distance = _distance;

		int insertionIndex = 0;

		final int resultSize = result.size();
		if ( resultSize > 0 )
		{
			int min = 0;
			int max = resultSize - 1;

			while ( min <= max )
			{
				insertionIndex = ( max + min ) / 2;

				if ( distance <= ((Face3DIntersection)result.get( insertionIndex )).getDistance() )
					max = insertionIndex - 1;
				else
					min = ++insertionIndex;
			}
		}

		result.add( insertionIndex , this );
	}

	public int compareTo( final Object o )
	{
		final double delta = getDistance() - ((Face3DIntersection)o).getDistance();
		return ( delta < 0.0 ) ? -1 : ( delta == 0.0 ) ? 0 : 1;
	}

	/**
	 * Returns the ID of the intersected object.
	 *
	 * @return  ID of the intersected object.
	 */
	public Object getObjectID()
	{
		return _objectID;
	}

	/**
	 * Get transformation matrix that transforms object to world coordinates.
	 *
	 * @return  Transformation matrix from object to world coordinates.
	 */
	public Matrix3D getObject2world()
	{
		return _object2world;
	}

	/**
	 * Get the {@link Object3D} that was intersected.
	 *
	 * @return  {@link Object3D} that was intersected.
	 */
	public Object3D getObject()
	{
		return _face.getObject();
	}

	/**
	 * Get the {@link Face3D} that was intersected.
	 *
	 * @return  {@link Face3D} that was intersected.
	 */
	public Face3D getFace()
	{
		return _face;
	}

	/**
	 * Get intersection point in world coordinates.
	 *
	 * @return  Intersection point in world coordinates.
	 */
	public Vector3D getIntersectionPoint()
	{
		return _intersectionPoint;
	}

	/**
	 * Get distance between reference and intersection point.
	 *
	 * @return  Distance between reference and intersection point.
	 */
	public double getDistance()
	{
		return _distance;
	}
}
