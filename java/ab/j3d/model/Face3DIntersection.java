/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class provides information about the intersection between a ray and a
 * face. It provides:
 * <ul>
 *   <li>Information about the intersected object;</li>
 *   <li>The intersected face;</li>
 *   <li>The intersecting ray;</li>
 *   <li>The intersection point;</li>
 *   <li>Distance between ray origin and intersection point.</li>
 * </ul>
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class Face3DIntersection
	implements Comparable<Face3DIntersection>
{
	/**
	 * ID of intersected object.
	 */
	@NotNull
	private final Object _objectID;

	/**
	 * Transforms {@link Object3D} to world coordinates.
	 */
	@NotNull
	private final Matrix3D _object2world;

	/**
	 * Object that was tested.
	 */
	@NotNull
	private final Object3D _object;

	/**
	 * Path in scene graph to <code>object</code>.
	 */
	@NotNull
	private final Node3DPath _path;

	/**
	 * The {@link Polygon3D} that was intersected.
	 */
	@NotNull
	private final Polygon3D _polygon;

	/**
	 * Intersection point in world coordinates.
	 */
	@NotNull
	private final Vector3D _intersectionPoint;

	/**
	 * Ray that intersected the face.
	 */
	@NotNull
	private final Ray3D _ray;

	/**
	 * Distance between reference and intersection point. This value is
	 * calculated on-demand (until then, it's set to {@link Double#NaN}.
	 *
	 * @see     #getDistance
	 */
	private double _distance;

	/**
	 * Construct new intersection information object. All geometry is expressed
	 * in world coordinates (wcs).
	 *
	 * @param   objectID            ID of intersected object.
	 * @param   object2world        Transforms object to world coordinates.
	 * @param   object              Object that was intersected.
	 * @param   path                Path in scene graph to <code>object</code>.
	 * @param   polygon             The {@link Polygon3D} that was intersected.
	 * @param   ray                 {@link Ray3D} that intersected the face.
	 * @param   intersectionPoint   Intersection point.
	 */
	public Face3DIntersection( @NotNull final Object objectID , @NotNull final Matrix3D object2world , @NotNull final Object3D object , @NotNull final Node3DPath path, @NotNull final Polygon3D polygon , @NotNull final Ray3D ray , @NotNull final Vector3D intersectionPoint )
	{
		_objectID = objectID;
		_object2world = object2world;
		_object = object;
		_path = path;
		_polygon = polygon;
		_ray = ray;
		_intersectionPoint = intersectionPoint;
		_distance = Double.NaN;
	}

	/**
	 * This methods implements insertion-sort by intersection distance.
	 *
	 * @param   result          Sorted list of {@link Face3DIntersection}'s.
	 * @param   intersection    {@link Face3DIntersection} that needs to be added to the list.
	 */
	public static void addSortedByDistance( final List<Face3DIntersection> result , final Face3DIntersection intersection )
	{
		final double distance = intersection.getDistance();

		int insertionIndex = 0;

		final int resultSize = result.size();
		if ( resultSize > 0 )
		{
			int min = 0;
			int max = resultSize - 1;

			while ( min <= max )
			{
				insertionIndex = ( max + min ) / 2;

				final Face3DIntersection other = result.get( insertionIndex );

				if ( distance <= other.getDistance() )
				{
					max = insertionIndex - 1;
				}
				else
				{
					min = ++insertionIndex;
				}
			}
		}

		result.add( insertionIndex , intersection );
	}

	@Override
	public int compareTo( final Face3DIntersection face3DIntersection )
	{
		final double delta = getDistance() - face3DIntersection.getDistance();
		return ( delta < 0.0 ) ? -1 : ( delta == 0.0 ) ? 0 : 1;
	}

	/**
	 * Get distance between reference and intersection point.
	 *
	 * @return  Distance between reference and intersection point.
	 */
	public double getDistance()
	{
		double result = _distance;
		if ( Double.isNaN( result ) )
		{
			result = _intersectionPoint.distanceTo( _ray.getOrigin() );
			_distance = result;
		}

		return result;
	}

	/**
	 * Get path in scene graph to <code>object</code>.
	 *
	 * @return  Path in scene graph to <code>object</code>.
	 */
	@NotNull
	public Node3DPath getPath()
	{
		return _path;
	}

	/**
	 * Get the {@link Polygon3D} that was intersected.
	 *
	 * @return  {@link Polygon3D} that was intersected.
	 */
	@NotNull
	public Polygon3D getPolygon()
	{
		return _polygon;
	}

	/**
	 * Get intersection point in world coordinates.
	 *
	 * @return  Intersection point in world coordinates.
	 */
	@NotNull
	public Vector3D getIntersectionPoint()
	{
		return _intersectionPoint;
	}

	/**
	 * Get the {@link Object3D} that was intersected.
	 *
	 * @return  {@link Object3D} that was intersected.
	 */
	@NotNull
	public Object3D getObject()
	{
		return _object;
	}

	/**
	 * Get transformation matrix from the {@link Object3D} that was intersected with.
	 * This matrix transforms {@link Object3D} to world coordinates.
	 *
	 * @return  Transformation matrix from {@link Object3D} to world coordinates.
	 */
	@NotNull
	public Matrix3D getObject2world()
	{
		return _object2world;
	}

	/**
	 * Returns the ID of the intersected object.
	 *
	 * @return  ID of the intersected object.
	 */
	@NotNull
	public Object getObjectID()
	{
		return _objectID;
	}

	/**
	 * Get ray that intersected the face.
	 *
	 * @return  {@link Ray3D} that intersected the face.
	 */
	@NotNull
	public Ray3D getRay()
	{
		return _ray;
	}
}
