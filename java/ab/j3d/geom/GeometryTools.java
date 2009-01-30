/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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

import java.awt.geom.Point2D;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

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
	 * Get intersection of two line segments.
	 *
	 * @param   p1      Start point of line segment 1.
	 * @param   p2      End point of line segment 1
	 * @param   p3      Start point of line segment 2.
	 * @param   p4      End point of line segment 2
	 *
	 * @return  Points describing the intersection (1 or 2 points);
	 *          <code>null</code> if no intersection was found.
	 */
	public static Point2D[] getIntersectionBetweenLineSegments( final Point2D p1 , final Point2D p2 , final Point2D p3 , final Point2D p4 )
	{
		return getIntersectionBetweenLineSegments( p1.getX() , p1.getY() , p2.getX() , p2.getY() , p3.getX() , p3.getY() , p4.getX() , p4.getY() );
	}

	/**
	 * Get intersection of two line segments.
	 *
	 * This uses the following algorithm:
	 *
	 *    "Intersection point of two lines (2 dimension)"
	 *    Author: Paul Bourke (april 1989)
	 *    http://astronomy.swin.edu.au/pbourke/geometry/lineline2d/
	 *
	 * @param   x1      Start point of line segment 1.
	 * @param   y1      Start point of line segment 1
	 * @param   x2      End point of line segment 1
	 * @param   y2      End point of line segment 1
	 * @param   x3      Start point of line segment 2.
	 * @param   y3      Start point of line segment 2
	 * @param   x4      End point of line segment 2
	 * @param   y4      End point of line segment 2
	 *
	 * @return  Points describing the intersection (1 or 2 points);
	 *          <code>null</code> if no intersection was found.
	 */
	public static Point2D[] getIntersectionBetweenLineSegments( final double x1 , final double y1 , final double x2 , final double y2 , final double x3 , final double y3 , final double x4 , final double y4 )
	{
		Point2D[] result = null;

		final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
		final double d  = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );

		if ( MathTools.almostEqual( d , 0.0 , 0.00001 ) ) /* lines are parallel */
		{
			if ( MathTools.almostEqual( n1 , 0.0 , 0.00001 ) ) /* they are coincident */
			{
				double sx1;
				double sx2;
				boolean isPositive;

				if ( x1 <= x2 ) { sx1 = x1; sx2 = x2; isPositive = true; }
				           else { sx1 = x2; sx2 = x1; isPositive = false; }

				if ( x3 <= x4 )
				{
					if ( x3 > sx1 ) sx1 = x3;
					if ( x4 < sx2 ) sx2 = x4;
				}
				else
				{
					if ( x4 > sx1 ) sx1 = x4;
					if ( x3 < sx2 ) sx2 = x3;
				}

				if ( sx1 <= sx2 )
				{
					double sy1;
					double sy2;

					if ( y1 <= y2 ) { sy1 = y1; sy2 = y2; }
					           else { sy1 = y2; sy2 = y1; isPositive = !isPositive; }

					if ( y3 <= y4 )
					{
						if ( y3 > sy1 ) sy1 = y3;
						if ( y4 < sy2 ) sy2 = y4;
					}
					else
					{
						if ( y4 > sy1 ) sy1 = y4;
						if ( y3 < sy2 ) sy2 = y3;
					}

					if ( sy1 <= sy2 )
					{
						/*
						 * Return the intersection.
						 */
						if ( MathTools.almostEqual( sx1 , sx2 , 0.00001 ) && MathTools.almostEqual( sy1 , sy2 , 0.00001 ) )
						{
							result = new Point2D[] { new Point2D.Double( sx1 , sy1 ) };
						}
						else if ( isPositive )
						{
							result = new Point2D[] { new Point2D.Double( sx1 , sy1 ) , new Point2D.Double( sx2 , sy2 ) };
						}
						else
						{
							result = new Point2D[] { new Point2D.Double( sx1 , sy2 ) , new Point2D.Double( sx2 , sy1 ) };
						}
					}
				}
			}
		}
		else /* lines intersect at some point */
		{
			/*
			 * Test if intersection point is within both line segments
			 */
			final double ua = n1 / d;
			if ( ( ua >= -0.00001 ) && ( ua <= 1.00001 ) ) // float round error fix
			{

				final double n2 = ( x2 - x1 ) * ( y1 - y3 ) - ( y2 - y1 ) * ( x1 - x3 );
				final double ub = n2 / d;

				if ( ( ub >= -0.00001 ) && ( ub <= 1.00001 ) ) // float round error fix
				{
					final double x = x1 + ua * ( x2 - x1 );
					final double y = y1 + ua * ( y2 - y1 );

					result = new Point2D[] { new Point2D.Double( x , y ) };
				}
			}
		}

		return result;
	}

	/**
	 * Get intersection of two lines.
	 *
	 * @param   p1      First point on line 1.
	 * @param   p2      Second point on line 1
	 * @param   p3      First point on line 2.
	 * @param   p4      Second point on line 2
	 *
	 * @return  Point of intersection;
	 *          <code>null</code> if no intersection exists (parallel lines).
	 */
	public static Point2D getIntersectionBetweenLines( final Point2D p1 , final Point2D p2 , final Point2D p3 , final Point2D p4 )
	{
		return getIntersectionBetweenLines( p1.getX() , p1.getY() , p2.getX() , p2.getY() , p3.getX() , p3.getY() , p4.getX() , p4.getY() );
	}

	/**
	 * Get intersection of two lines.
	 *
	 * @param   x1      First point on line 1.
	 * @param   y1      First point on line 1
	 * @param   x2      Second point on line 1
	 * @param   y2      Second point on line 1
	 * @param   x3      First point on line 2.
	 * @param   y3      First point on line 2
	 * @param   x4      Second point on line 2
	 * @param   y4      Second point on line 2
	 *
	 * @return  Point of intersection;
	 *          <code>null</code> if no intersection exists (parallel lines).
	 */
	public static Point2D getIntersectionBetweenLines( final double x1 , final double y1 , final double x2 , final double y2 , final double x3 , final double y3 , final double x4 , final double y4 )
	{
		Point2D result = null;

		final double d = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );
		if ( !MathTools.almostEqual( d , 0.0 , 0.00001 ) ) /* are not parallel, so they intersect at some point */
		{
			final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
			final double ua = n1 / d;

			final double x = x1 + ua * ( x2 - x1 );
			final double y = y1 + ua * ( y2 - y1 );

			result = new Point2D.Double( x , y );
		}

		return result;
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
	 * @param   planeTransform  Matrix to defined the plane from.
	 * @param   twoSidedPlane   Consider both sides of plane in intersection test.
	 * @param   ray             Ray to get intersection from.
	 *
	 * @return  Intersection-point between ray and plane;
	 *          <code>null</code> if no intersection exists (ray parallel to
	 *          plane, from negative side of one-sided plane, or outside range
	 *          of half-ray).
	 *
	 * @throws  NullPointerException if a required input argument is <code>null</code>.
	 */
	public static Vector3D getIntersectionBetweenRayAndPlane( final Matrix3D planeTransform , final boolean twoSidedPlane , final Ray3D ray )
	{
		return getIntersectionBetweenRayAndPlane( planeTransform.xz , planeTransform.yz , planeTransform.zz , Vector3D.dot( planeTransform.xz , planeTransform.yz , planeTransform.zz , planeTransform.xo , planeTransform.yo , planeTransform.zo ) , twoSidedPlane , ray.getOrigin() , ray.getDirection() , ray.isHalfRay() );
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
	 * @param   halfRay         Wether the ray is infinite or not.
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

		if ( twoSidedPlane ? !MathTools.almostEqual( denominator , 0.0 , 0.000001 ) : MathTools.significantlyLessThan( denominator , 0.0 , 0.000001 ) ) /* line parallel to plane */
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

	/**
	 * Get closest point on a line to another point.
	 *
	 * This uses the following algorithm:
	 * <pre>
	 *   "Minimum Distance between a Point and a Line"
	 *   Author: Paul Bourke (october 1988)
	 *   http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
	 * </pre>
	 * The equation of a line defined through two points P1 (x1,y1) and
	 * P2 (x2,y2) is: P = P1 + u (P2 - P1).
	 * <p>
	 * The point P3 (x3,y3) is closest to the line at the tangent to the line
	 * which passes through P3, that is, the dot product of the tangent and
	 * line is 0, thus (P3 - P) dot (P2 - P1) = 0
	 * <p>
	 * Substituting the equation of the line gives
	 * <pre>
	 *   [ P3 - P1 - u(P2 - P1) ] dot ( P2 - P1 ) = 0
	 * </pre>
	 * Solving this gives the value of u
	 * <pre>
	 *       ( x3 - x1 ) ( x2 - x1 ) + ( y3 - y1 ) ( y2 - y1 )
	 *   u = -------------------------------------------------
	 *                        || P2 - P1 || ^ 2
	 * </pre>
	 *
	 * @param   p               Point to find closest point to.
	 * @param   p1              First point of line to find point on.
	 * @param   p2              Second point of line to find point on.
	 * @param   segmentOnly     If set, only return result if the point is on
	 *                          the line segment.
	 *
	 * @return  Point on line closest to the given point;
	 *          <code>null</code> if point lies outside the line segment.
	 */
	public static Vector3D getClosestPointOnLine( final Vector3D p , final Vector3D p1 , final Vector3D p2 , final boolean segmentOnly )
	{
		final double dx = p2.x - p1.x;
		final double dy = p2.y - p1.y;
		final double dz = p2.z - p1.z;
		final double u  = ( ( p.x - p1.x ) * dx + ( p.y - p1.y ) * dy + ( p.z - p1.z ) * dz ) / ( dx * dx + dy * dy + dz * dz );

		return ( !segmentOnly || ( ( u >= 0.00001 ) && u <= 1.00001 ) ) ? p1.plus( u * dx, u * dy , p.z ) : null;
	}
}
