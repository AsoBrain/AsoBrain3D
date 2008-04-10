/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2008
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
package ab.j3d.geom;

import ab.j3d.Vector3D;

/**
 * This class contains utility methods to solve common geometric problems.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class GeometryTools
{
	/**
	 * Tools class is not supposed to be instantiated.
	 */
	private GeometryTools()
	{
	}

	/**
	 * Returns the intersection point between a ray and a {@link Polygon3D}.
	 * The intersection point is returned as a {@link Vector3D}; however, if
	 * any of the following conditions is met, no intersection exists, and
	 * <code>null</code> will be returned:
	 * <ol>
	 *  <li>The ray is parallel to the polygon's plane;</li>
	 *  <li>The ray does not point towards the polygon;</li>
	 *  <li>The ray intersects the polygon's plane outside the polygon.</li>
	 * </ol>
	 *
	 * @param   polygon     Polygon to get intersection from.
	 * @param   ray         Ray to get intersection from.
	 *
	 * @return  A {@link Vector3D} with the location where the line shot from
	 *          the mouse pointer intersects with a given plane;
	 *          <code>null</code> if no intersection exists (ray parallel to
	 *          polygon, from negative side of one-sided polygon, outside range
	 *          of half-ray, or outside polygon).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public static Vector3D getIntersectionBetweenRayAndPolygon( final Polygon3D polygon , final Ray3D ray )
	{
		Vector3D result = null;

		final int vertexCount = polygon.getVertexCount();
		if ( vertexCount >= 3 )
		{
			result = getIntersectionBetweenRayAndPlane( polygon , ray );
			if ( ( result != null ) && !isPointInsidePolygon( polygon , result ) )
				result = null;
		}

		return result;
	}

	/**
	 * Returns the intersection point between a ray and a plane. The point is
	 * returned as a {@link Vector3D}; however, if any of the following
	 * conditions is met, no intersection exists, and <code>null</code> will be
	 * returned:
	 * <ol>
	 *  <li>The ray is parallel to the plane;</li>
	 *  <li>The ray does not point in the plane's direction.</li>
	 * </ol>
	 * For an explanation of the math used here, see this sites:
	 * <a href='http://astronomy.swin.edu.au/~pbourke/geometry/planeline/'>http://astronomy.swin.edu.au/~pbourke/geometry/planeline/</a> and
	 * <A href='http://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm'>http://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm</a>.
	 *
	 * @param   plane   Plane to get intersection from.
	 * @param   ray     Ray to get intersection from.
	 *
	 * @return  Intersection-point between ray and plane;
	 *          <code>null</code> if no intersection exists (ray parallel to
	 *          plane, from negative side of one-sided plane, or outside range
	 *          of half-ray).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public static Vector3D getIntersectionBetweenRayAndPlane( final Plane3D plane , final Ray3D ray )
	{
		final Vector3D planeNormal = plane.getNormal();
		return getIntersectionBetweenRayAndPlane( planeNormal.x , planeNormal.y , planeNormal.z , plane.getDistance() , plane.isTwoSided() , ray.getOrigin() , ray.getDirection() , ray.isHalfRay() );
	}

	/**
	 * Returns the intersection point between a ray and a plane. The point is
	 * returned as a {@link Vector3D}; however, if any of the following
	 * conditions is met, no intersection exists, and <code>null</code> will be
	 * returned:
	 * <ol>
	 *  <li>The ray is parallel to the plane;</li>
	 *  <li>The ray does not point in the plane's direction.</li>
	 * </ol>
	 * For an explanation of the math used here, see this sites:
	 * <a href='http://astronomy.swin.edu.au/~pbourke/geometry/planeline/'>http://astronomy.swin.edu.au/~pbourke/geometry/planeline/</a> and
	 * <A href='http://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm'>http://www.siggraph.org/education/materials/HyperGraph/raytrace/rayplane_intersection.htm</a>.
	 *
	 * @param   planeNormalX    X component of plane normal.
	 * @param   planeNormalY    Y component of plane normal.
	 * @param   planeNormalZ    Z component of plane normal.
	 * @param   planeDistance   Distance of plane to origin.
	 * @param   twoSidedPlane   Consider both sides of plane in intersection test.
	 * @param   rayOrigin       Origin of ray.
	 * @param   rayDirection    Direction of ray.
	 *
	 * @return  Intersection-point between ray and plane;
	 *          <code>null</code> if no intersection exists (ray parallel to
	 *          plane, from negative side of one-sided plane, or outside range
	 *          of half-ray).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public static Vector3D getIntersectionBetweenRayAndPlane( final double planeNormalX , final double planeNormalY , final double planeNormalZ , final double planeDistance , final boolean twoSidedPlane , final Vector3D rayOrigin , final Vector3D rayDirection , final boolean halfRay )
	{
		Vector3D result = null;

		final double denominator = planeNormalX * rayDirection.x
		                         + planeNormalY * rayDirection.y
		                         + planeNormalZ * rayDirection.z;

		if ( twoSidedPlane ? ( denominator != 0.0 ) : ( denominator < 0.0 ) ) /* line parallel to plane */
		{
			final double numerator = planeDistance - planeNormalX * rayOrigin.x
			                                       - planeNormalY * rayOrigin.y
			                                       - planeNormalZ * rayOrigin.z;

			final double intersectionDistance = numerator / denominator;

			if ( ( intersectionDistance > -0.000001 )/* almost on plane */
			  && ( intersectionDistance <  0.000001 ) )
			{
				result = rayOrigin;
			}
			else if ( !halfRay || ( intersectionDistance > 0.0 ) ) /* complete ray, or ray pointing in plane direction */
			{
				final double x = rayOrigin.x + intersectionDistance * rayDirection.x;
				final double y = rayOrigin.y + intersectionDistance * rayDirection.y;
				final double z = rayOrigin.z + intersectionDistance * rayDirection.z;

				result = Vector3D.INIT.set( x , y , z );
			}
		}

		return result;
	}

	/**
	 * Get normal vector of plane on which the triangle with the specified three
	 * points is defined. The normal will point 'outwards' if the points are
	 * specified in clockwise ordering.
	 *
	 * @param   p1  First point of triangle.
	 * @param   p2  Second point of triangle.
	 * @param   p3  Third point of triangle.
	 *
	 * @return  Normal vector.
	 *
	 * @see     Plane3D#getNormal
	 * @see     Plane3D#isTwoSided
	 */
	public static Vector3D getPlaneNormal( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 )
	{
		final Vector3D u = p1.minus( p2 );
		final Vector3D v = p3.minus( p2 );

		final Vector3D cross = Vector3D.cross( u , v );

		return cross.normalize();
	}

	/**
	 * Test if the specified point is 'inside' the specified 3D polygon. Points
	 * on the edges and vertices are also considered 'inside'.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>The point must be specified in the object's own coordinate system.</dd>
	 * </dl>
	 * For an explanation of the math used here, see this site under 'Solution 4 (3D)':
	 * <a href='http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/'>http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/</a>
	 *
	 * @param   polygon     Polygon to test point against.
	 * @param   point       Point to test.
	 *
	 * @return  <code>true</code> if the point is inside the polygon;
	 *          <code>false</code> otherwise.
	 *
	 * @throws  NullPointerException if an input argument is <code>null</code>.
	 */
	public static boolean isPointInsidePolygon( final Polygon3D polygon , final Vector3D point )
	{
		return isPointInsidePolygon( polygon , point.x , point.y , point.z );
	}

	/**
	 * Test if the specified point is 'inside' the specified 3D polygon. Points
	 * on the edges and vertices are also considered 'inside'.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>The point must be specified in the object's own coordinate system.</dd>
	 * </dl>
	 * For an explanation of the math used here, see this site under 'Solution 4 (3D)':
	 * <a href='http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/'>http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/</a>
	 *
	 * @param   polygon     Polygon to test point against.
	 * @param   x           X coordinate of point to test.
	 * @param   y           Y coordinate of point to test.
	 * @param   z           Z coordinate of point to test.
	 *
	 * @return  <code>true</code> if the point is inside the polygon;
	 *          <code>false</code> otherwise.
	 *
	 * @throws  NullPointerException if <code>polygon</code> is <code>null</code>.
	 */
	public static boolean isPointInsidePolygon( final Polygon3D polygon , final double x , final double y , final double z )
	{
		final boolean result;

		final int vertexCount = polygon.getVertexCount();
		if ( vertexCount >= 3 )
		{
			/*
			 * The following code is almost a carbon copy of the example code
			 * on the web page with only performance enhancements and data
			 * structure integration.
			 */
			int    i;
			double d;
			double angleSum = 0.0;

			double p1x;
			double p1y;
			double p1z;
			double m1;

			double p2x = polygon.getX( i = vertexCount - 1 ) - x;
			double p2y = polygon.getY( i ) - y;
			double p2z = polygon.getZ( i ) - z;
			double m2  = Math.sqrt( p2x * p2x + p2y * p2y + p2z * p2z );

			for ( i = 0 ; i < vertexCount ; i++ )
			{
				p1x = p2x;
				p1y = p2y;
				p1z = p2z;
				m1  = m2;

				p2x = polygon.getX( i ) - x;
				p2y = polygon.getY( i ) - y;
				p2z = polygon.getZ( i ) - z;
				m2  = Math.sqrt( p2x * p2x + p2y * p2y + p2z * p2z );

				d = m1 * m2;
				if ( d <= 0.0000001 ) /* We are on a node, consider this inside */
					break;

				angleSum += Math.acos( ( p1x * p2x + p1y * p2y + p1z * p2z ) / d );
			}

			/*
			 * Inside if:
			 *  - Aborted prematurely; or
			 *  - Angle sum is 2PI (with 0.0000001 deviation tolerance)
			 */
			result = ( i < vertexCount ) || ( ( angleSum > 6.2831852 ) && ( angleSum < 6.2831854 ) );
		}
		else
		{
			result = false;
		}

		return result;
	}
}
