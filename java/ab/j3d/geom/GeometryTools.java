/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2011 Peter S. Heijnen
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

import java.awt.geom.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * This class contains utility methods to solve common geometric problems.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class GeometryTools
{
	/**
	 * Constant for 2 times <i>pi</i>, the ratio of the circumference of a
	 * circle to its radius.
	 */
	public static final double TWO_PI = Math.PI + Math.PI;

	/**
	 * Tolerance to use for floating-point comparisons.
	 */
	private static final double EPSILON = (double)0.00001f;

	/**
	 * Tools class is not supposed to be instantiated.
	 */
	private GeometryTools()
	{
	}

	/**
	 * Converts an oriented bounding box (OBB) to an (word) axis-aligned
	 * bounding box (AABB).
	 *
	 * @param   box2world   Transforms box to world coordinates.
	 * @param   box         Oriented bounding box.
	 *
	 * @return  Axis-aligned bounding box.
	 */
	@NotNull
	public static Bounds3D convertObbToAabb( @NotNull final Matrix3D box2world, @NotNull final Bounds3D box )
	{
		return convertObbToAabb( box2world, box.v1.x, box.v1.y, box.v1.z, box.v2.x, box.v2.y, box.v2.z );
	}

	/**
	 * Converts an oriented bounding box (OBB) to an (word) axis-aligned
	 * bounding box (AABB).
	 *
	 * @param   box2world   Transforms box to world coordinates.
	 * @param   x1          Minimum X coordinate of oriented bounding box.
	 * @param   y1          Minimum Y coordinate of oriented bounding box.
	 * @param   z1          Minimum Z coordinate of oriented bounding box.
	 * @param   x2          Maximum X coordinate of oriented bounding box.
	 * @param   y2          Maximum Y coordinate of oriented bounding box.
	 * @param   z2          Maximum Z coordinate of oriented bounding box.
	 *
	 * @return  Axis-aligned bounding box.
	 */
	@NotNull
	public static Bounds3D convertObbToAabb( @NotNull final Matrix3D box2world, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2 )
	{
		double tx = box2world.transformX( x1, y1, z1 );
		double ty = box2world.transformY( x1, y1, z1 );
		double tz = box2world.transformZ( x1, y1, z1 );
		double minX = tx;
		double minY = ty;
		double minZ = tz;
		double maxX = tx;
		double maxY = ty;
		double maxZ = tz;

		tx = box2world.transformX( x1, y1, z2 );
		ty = box2world.transformY( x1, y1, z2 );
		tz = box2world.transformZ( x1, y1, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x1, y2, z1 );
		ty = box2world.transformY( x1, y2, z1 );
		tz = box2world.transformZ( x1, y2, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x1, y2, z2 );
		ty = box2world.transformY( x1, y2, z2 );
		tz = box2world.transformZ( x1, y2, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y1, z1 );
		ty = box2world.transformY( x2, y1, z1 );
		tz = box2world.transformZ( x2, y1, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y1, z2 );
		ty = box2world.transformY( x2, y1, z2 );
		tz = box2world.transformZ( x2, y1, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y2, z1 );
		ty = box2world.transformY( x2, y2, z1 );
		tz = box2world.transformZ( x2, y2, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y2, z2 );
		ty = box2world.transformY( x2, y2, z2 );
		tz = box2world.transformZ( x2, y2, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		return new Bounds3D( minX, minY, minZ, maxX, maxY, maxZ );
	}

	/**
	 * Test sphere intersection.
	 *
	 * @param   center1     Center of sphere 1.
	 * @param   radius1     Radius of sphere 1.
	 * @param   from2to1    Transformation from sphere 2 to sphere 1.
	 * @param   center2     Center of sphere 2.
	 * @param   radius2     Radius of sphere 2.
	 *
	 * @return  <code>true</code> if the spheres intersect;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testSphereIntersection( final Vector3D center1, final double radius1, final Matrix3D from2to1, final Vector3D center2, final double radius2 )
	{
		final double dx = from2to1.transformX( center2 ) - center1.x;
		final double dy = from2to1.transformY( center2 ) - center1.y;
		final double dz = from2to1.transformZ( center2 ) - center1.z;
		return testSphereIntersection( radius1, dx, dy, dz, radius2 );
	}

	/**
	 * Test sphere intersection.
	 *
	 * @param   radius1     Radius of sphere 2.
	 * @param   centerDx    Delta X between center of spheres.
	 * @param   centerDy    Delta Y between center of spheres.
	 * @param   centerDz    Delta Z between center of spheres.
	 * @param   radius2     Radius of sphere 2.
	 *
	 * @return  <code>true</code> if the spheres intersect;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testSphereIntersection( final double radius1, final double centerDx, final double centerDy, final double centerDz, final double radius2 )
	{
		final double maxDistance = radius1 + radius2;
		return ( centerDx * centerDx + centerDy * centerDy + centerDz * centerDz ) < ( maxDistance * maxDistance );
	}

	/**
	 * Test oriented bounding box intersection.
	 *
	 * Borrowed code from <A href='http://channel9.msdn.com/ShowPost.aspx?PostID=276041'>XNA Oriented Bounding Box Intersection Test</A>,
	 * which was based on <A href='http://www.cs.unc.edu/~geom/theses/gottschalk/main.pdf'>Collision Queries using Oriented Boxes</A> by Stefan Gottschalk.
	 *
	 * @param   box1        Oriented bounding box #1.
	 * @param   from2to1    Transformation from box #2 to box #1.
	 * @param   box2        Oriented bounding box #2.
	 *
	 * @return  <code>true</code> if the bounding boxes intersect;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testOrientedBoundingBoxIntersection( @NotNull final Bounds3D box1, @NotNull final Matrix3D from2to1, @NotNull final Bounds3D box2 )
	{
		return testOrientedBoundingBoxIntersection( box1.v1.x, box1.v1.y, box1.v1.z, box1.v2.x - box1.v1.x, box1.v2.y - box1.v1.y, box1.v2.z - box1.v1.z, from2to1, box2.v1.x, box2.v1.y, box2.v1.z, box2.v2.x - box2.v1.x, box2.v2.y - box2.v1.y, box2.v2.z - box2.v1.z );
	}

	/**
	 * Test oriented bounding box intersection.
	 *
	 * Borrowed code from <A href='http://channel9.msdn.com/ShowPost.aspx?PostID=276041'>XNA Oriented Bounding Box Intersection Test</A>,
	 * which was based on <A href='http://www.cs.unc.edu/~geom/theses/gottschalk/main.pdf'>Collision Queries using Oriented Boxes</A> by Stefan Gottschalk.
	 *
	 * @param   ox1         X coordinate of local axis-aligned origin of box 1.
	 * @param   oy1         Y coordinate of local axis-aligned origin of box 1.
	 * @param   oz1         Z coordinate of local axis-aligned origin of box 1.
	 * @param   dx1         Local axis-aligned size along X-axis of box 1.
	 * @param   dy1         Local axis-aligned size along Y-axis of box 1.
	 * @param   dz1         Local axis-aligned size along Z-axis of box 1.
	 * @param   from2to1    Transformation from box 2 to box 1.
	 * @param   ox2         X coordinate of local axis-aligned origin of box 2.
	 * @param   oy2         Y coordinate of local axis-aligned origin of box 2.
	 * @param   oz2         Z coordinate of local axis-aligned origin of box 2.
	 * @param   dx2         Local axis-aligned size along X-axis of box 2.
	 * @param   dy2         Local axis-aligned size along Y-axis of box 2.
	 * @param   dz2         Local axis-aligned size along Z-axis of box 2.
	 *
	 * @return  <code>true</code> if the bounding boxes intersect;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testOrientedBoundingBoxIntersection( final double ox1, final double oy1, final double oz1, final double dx1, final double dy1, final double dz1, @NotNull final Matrix3D from2to1, final double ox2, final double oy2, final double oz2, final double dx2, final double dy2, final double dz2 )
	{
		final double extents1X   = 0.5 * dx1;
		final double extents1Y   = 0.5 * dy1;
		final double extents1Z   = 0.5 * dz1;

		final double extents2X   = 0.5 * dx2;
		final double extents2Y   = 0.5 * dy2;
		final double extents2Z   = 0.5 * dz2;

		final double centerOtherX = ox2 + 0.5 * dx2;
		final double centerOtherY = oy2 + 0.5 * dy2;
		final double centerOtherZ = oz2 + 0.5 * dz2;

		final double separationX = from2to1.transformX( centerOtherX, centerOtherY, centerOtherZ ) - ( ox1 + 0.5 * dx1 );
		final double separationY = from2to1.transformY( centerOtherX, centerOtherY, centerOtherZ ) - ( oy1 + 0.5 * dy1 );
		final double separationZ = from2to1.transformZ( centerOtherX, centerOtherY, centerOtherZ ) - ( oz1 + 0.5 * dz1 );

		final double absXX = Math.abs( from2to1.xx );
		final double absXY = Math.abs( from2to1.xy );
		final double absXZ = Math.abs( from2to1.xz );
		final double absYX = Math.abs( from2to1.yx );
		final double absYY = Math.abs( from2to1.yy );
		final double absYZ = Math.abs( from2to1.yz );
		final double absZX = Math.abs( from2to1.zx );
		final double absZY = Math.abs( from2to1.zy );
		final double absZZ = Math.abs( from2to1.zz );

		return
		/* Test 1 X axis */ significantlyLessThan( Math.abs( separationX ), extents1X + Vector3D.dot( extents2X, extents2Y, extents2Z, absXX, absXY, absXZ ) ) &&
		/* Test 1 Y axis */ significantlyLessThan( Math.abs( separationY ), extents1Y + Vector3D.dot( extents2X, extents2Y, extents2Z, absYX, absYY, absYZ ) ) &&
		/* Test 1 Z axis */ significantlyLessThan( Math.abs( separationZ ), extents1Z + Vector3D.dot( extents2X, extents2Y, extents2Z, absZX, absZY, absZZ ) ) &&
		/* Test 2 X axis */ significantlyLessThan( Math.abs( Vector3D.dot( from2to1.xx, from2to1.yx, from2to1.zx, separationX, separationY, separationZ ) ), Vector3D.dot( extents1X, extents1Y, extents1Z, absXX, absYX, absZX ) + extents2X ) &&
		/* Test 2 Y axis */ significantlyLessThan( Math.abs( Vector3D.dot( from2to1.xy, from2to1.yy, from2to1.zy, separationX, separationY, separationZ ) ), Vector3D.dot( extents1X, extents1Y, extents1Z, absXY, absYY, absZY ) + extents2Y ) &&
		/* Test 2 Z axis */ significantlyLessThan( Math.abs( Vector3D.dot( from2to1.xz, from2to1.yz, from2to1.zz, separationX, separationY, separationZ ) ), Vector3D.dot( extents1X, extents1Y, extents1Z, absXZ, absYZ, absZZ ) + extents2Z ) &&
		/* Test 3 case 1 */ ( Math.abs( separationZ * from2to1.yx - separationY * from2to1.zx ) <= extents1Y * absZX + extents1Z * absYX + extents2Y * absXZ + extents2Z * absXY ) &&
		/* Test 3 case 2 */ ( Math.abs( separationZ * from2to1.yy - separationY * from2to1.zy ) <= extents1Y * absZY + extents1Z * absYY + extents2X * absXZ + extents2Z * absXX ) &&
		/* Test 3 case 3 */ ( Math.abs( separationZ * from2to1.yz - separationY * from2to1.zz ) <= extents1Y * absZZ + extents1Z * absYZ + extents2X * absXY + extents2Y * absXX ) &&
		/* Test 3 case 4 */ ( Math.abs( separationX * from2to1.zx - separationZ * from2to1.xx ) <= extents1X * absZX + extents1Z * absXX + extents2Y * absYZ + extents2Z * absYY ) &&
		/* Test 3 case 5 */ ( Math.abs( separationX * from2to1.zy - separationZ * from2to1.xy ) <= extents1X * absZY + extents1Z * absXY + extents2X * absYZ + extents2Z * absYX ) &&
		/* Test 3 case 6 */ ( Math.abs( separationX * from2to1.zz - separationZ * from2to1.xz ) <= extents1X * absZZ + extents1Z * absXZ + extents2X * absYY + extents2Y * absYX ) &&
		/* Test 3 case 7 */ ( Math.abs( separationY * from2to1.xx - separationX * from2to1.yx ) <= extents1X * absYX + extents1Y * absXX + extents2Y * absZZ + extents2Z * absZY ) &&
		/* Test 3 case 8 */ ( Math.abs( separationY * from2to1.xy - separationX * from2to1.yy ) <= extents1X * absYY + extents1Y * absXY + extents2X * absZZ + extents2Z * absZX ) &&
		/* Test 3 case 9 */ ( Math.abs( separationY * from2to1.xz - separationX * from2to1.yz ) <= extents1X * absYZ + extents1Y * absXZ + extents2X * absZY + extents2Y * absZX );
		/* No separating axes => we have intersection */
	}

	/**
	 * Test intersection between to triangles in 3D.
	 *
	 * @param   v0  First vertex of first triangle.
	 * @param   v1  Second vertex of first triangle.
	 * @param   v2  Third vertex of first triangle.
	 * @param   u0  First vertex of second triangle.
	 * @param   u1  Second vertex of second triangle.
	 * @param   u2  Third vertex of second triangle.
	 *
	 * @return  <code>true</code> if the triangles intersect;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testTriangleTriangleIntersection( final Vector3D v0, final Vector3D v1, final Vector3D v2, final Vector3D u0, final Vector3D u1, final Vector3D u2 )
	{
		return TriTriMoeler.testTriangleTriangle( v0, v1, v2, u0, u1, u2 );
	}

	/**
	 * Test intersection between axis-aligned box and sphere.
	 *
	 * @param   sphereCenter    Center of sphere.
	 * @param   sphereRadius    Radius of sphere.
	 * @param   box             Axis-aligned box.
	 *
	 * @return  <code>true</code> if sphere intersects with box;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testSphereBoxIntersection( final Vector3D sphereCenter, final double sphereRadius , final Bounds3D box )
	{
		return testSphereBoxIntersection( sphereCenter.x,  sphereCenter.y,  sphereCenter.z,  sphereRadius, box.v1.x, box.v1.y, box.v1.z, box.v2.x, box.v2.y, box.v2.z );
	}

	/**
	 * Test intersection between axis-aligned box and sphere.
	 *
	 * @param   sphereCenterX   X coordinate of sphere center.
	 * @param   sphereCenterY   Y coordinate of sphere center.
	 * @param   sphereCenterZ   Z coordinate of sphere center.
	 * @param   sphereRadius    Radius of sphere.
	 * @param   boxMinX         Minimum X coordinate of axis-aligned box.
	 * @param   boxMinY         Minimum Y coordinate of axis-aligned box.
	 * @param   boxMinZ         Minimum Z coordinate of axis-aligned box.
	 * @param   boxMaxX         Maximum X coordinate of axis-aligned box.
	 * @param   boxMaxY         Maximum Y coordinate of axis-aligned box.
	 * @param   boxMaxZ         Maximum Z coordinate of axis-aligned box.
	 *
	 * @return  <code>true</code> if sphere intersects with box;
	 *          <code>false</code> otherwise.
	 *
	 * @noinspection MethodWithMultipleReturnPoints
	 */
	public static boolean testSphereBoxIntersection( final double sphereCenterX, final double sphereCenterY, final double sphereCenterZ, final double sphereRadius , final double boxMinX, final double boxMinY, final double boxMinZ, final double boxMaxX, final double boxMaxY, final double boxMaxZ )
	{
		double maxDistance = 0.0;

		if ( sphereCenterX < boxMinX )
		{
			final double dx = boxMinX - sphereCenterX;
			if ( dx > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dx * dx );
		}
		else if ( sphereCenterX > boxMaxX )
		{
			final double dx = sphereCenterX - boxMaxX;
			if ( dx > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dx * dx );
		}

		if ( sphereCenterY < boxMinY )
		{
			final double dy = boxMinY - sphereCenterY;
			if ( dy > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dy * dy );
		}
		else if ( sphereCenterY > boxMaxY )
		{
			final double dy = sphereCenterY - boxMaxY;
			if ( dy > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dy * dy );
		}

		if ( sphereCenterZ < boxMinZ )
		{
			final double dz = boxMinZ - sphereCenterZ;
			if ( dz > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dz * dz );
		}
		else if ( sphereCenterZ > boxMaxZ )
		{
			final double dz = sphereCenterZ - boxMaxZ;
			if ( dz > sphereRadius )
			{
				return false;
			}

			maxDistance = maxDistance + ( dz * dz );
		}

		return ( maxDistance < sphereRadius * sphereRadius );
	}

	/**
	 * Tests whether a sphere and a cylinder intersect, including containment
	 * and border cases.
	 *
	 * @param   sphereCenterX   X coordinate of sphere center.
	 * @param   sphereCenterY   Y coordinate of sphere center.
	 * @param   sphereCenterZ   Z coordinate of sphere center.
	 * @param   sphereRadius    Radius of sphere.
	 * @param   toSphere        Transformation from cylinder to sphere.
	 * @param   cylinderHeight  Height of the cylinder.
	 * @param   cylinderRadius  Radius of cylinder.
	 *
	 * @return  <code>true</code> if sphere intersects with cylinder;
	 *          <code>false</code> otherwise.
	 */
	public static boolean testSphereCylinderIntersection( final double sphereCenterX, final double sphereCenterY, final double sphereCenterZ, final double sphereRadius, final Matrix3D toSphere, final double cylinderHeight, final double cylinderRadius )
	{
		final double x = toSphere.inverseTransformX( sphereCenterX, sphereCenterY, sphereCenterZ );
		final double y = toSphere.inverseTransformY( sphereCenterX, sphereCenterY, sphereCenterZ );
		final double z = toSphere.inverseTransformZ( sphereCenterX, sphereCenterY, sphereCenterZ );
		return testSphereCylinderIntersection( x, y, z, sphereRadius, cylinderHeight, cylinderRadius );
	}

	/**
	 * Tests whether a sphere and a cylinder intersect, including containment
	 * and border cases. Coordinates are relative to the base of the cylinder.
	 *
	 * @param   sphereCenterX   X coordinate of sphere center.
	 * @param   sphereCenterY   Y coordinate of sphere center.
	 * @param   sphereCenterZ   Z coordinate of sphere center.
	 * @param   sphereRadius    Radius of sphere.
	 * @param   cylinderHeight  Height of the cylinder.
	 * @param   cylinderRadius  Radius of cylinder.
	 *
	 * @return  <code>true</code> if sphere intersects with cylinder;
	 *          <code>false</code> otherwise.
	 *
	 * @noinspection MethodWithMultipleReturnPoints
	 */
	public static boolean testSphereCylinderIntersection( final double sphereCenterX, final double sphereCenterY, final double sphereCenterZ, final double sphereRadius, final double cylinderHeight, final double cylinderRadius )
	{
		final double effectiveSphereRadius;

		if ( sphereCenterZ < 0.0 )
		{
			if ( sphereCenterZ < -sphereRadius )
			{
				return false;
			}

			effectiveSphereRadius = Math.sqrt( sphereRadius * sphereRadius - sphereCenterZ * sphereCenterZ );
		}
		else if ( sphereCenterZ > cylinderHeight )
		{
			if ( sphereCenterZ > cylinderHeight + sphereRadius )
			{
				return false;
			}

			final double offsetZ = sphereCenterZ - cylinderHeight;
			effectiveSphereRadius = Math.sqrt( sphereRadius * sphereRadius - offsetZ * offsetZ );
		}
		else
		{
			effectiveSphereRadius = sphereRadius;
		}

		return testCircleIntersection( cylinderRadius, sphereCenterX, sphereCenterY, effectiveSphereRadius );
	}

	/**
	 * Tests whether two circles intersect.
	 *
	 * @param   radius1     Radius of the first circle.
	 * @param   centerDx    Distance along x-axis between circle center points.
	 * @param   centerDy    Distance along y-axis between circle center points.
	 * @param   radius2     Radius of the second circle.
	 *
	 * @return  <code>true</code> if the circles intersect;
	 *          <code>false</code> otherwise.
	 */
	private static boolean testCircleIntersection( final double radius1, final double centerDx, final double centerDy, final double radius2 )
	{
		final double combinedRadius = radius1 + radius2;
		return lessOrAlmostEqual( centerDx * centerDx + centerDy * centerDy, combinedRadius * combinedRadius );
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
	public static Point2D[] getIntersectionBetweenLineSegments( final Point2D p1, final Point2D p2, final Point2D p3, final Point2D p4 )
	{
		return getIntersectionBetweenLineSegments( p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY() );
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
	public static Point2D[] getIntersectionBetweenLineSegments( final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final double x4, final double y4 )
	{
		Point2D[] result = null;

		final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
		final double d  = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );

		if ( almostEqual( d, 0.0 ) ) /* lines are parallel */
		{
			if ( almostEqual( n1, 0.0 ) ) /* they are coincident */
			{
				double sx1;
				double sx2;
				boolean isPositive;

				if ( x1 <= x2 ) { sx1 = x1; sx2 = x2; isPositive = true; }
				           else { sx1 = x2; sx2 = x1; isPositive = false; }

				if ( x3 <= x4 )
				{
					if ( x3 > sx1 )
					{
						sx1 = x3;
					}

					if ( x4 < sx2 )
					{
						sx2 = x4;
					}
				}
				else
				{
					if ( x4 > sx1 )
					{
						sx1 = x4;
					}

					if ( x3 < sx2 )
					{
						sx2 = x3;
					}
				}

				if ( sx1 <= sx2 )
				{
					double sy1;
					double sy2;

					if ( y1 <= y2 ) { sy1 = y1; sy2 = y2; }
					           else { sy1 = y2; sy2 = y1; isPositive = !isPositive; }

					if ( y3 <= y4 )
					{
						if ( y3 > sy1 )
						{
							sy1 = y3;
						}

						if ( y4 < sy2 )
						{
							sy2 = y4;
						}
					}
					else
					{
						if ( y4 > sy1 )
						{
							sy1 = y4;
						}

						if ( y3 < sy2 )
						{
							sy2 = y3;
						}
					}

					if ( sy1 <= sy2 )
					{
						/*
						 * Return the intersection.
						 */
						if ( almostEqual( sx1, sx2 ) && almostEqual( sy1, sy2 ) )
						{
							result = new Point2D[] { new Point2D.Double( sx1, sy1 ) };
						}
						else if ( isPositive )
						{
							result = new Point2D[] { new Point2D.Double( sx1, sy1 ), new Point2D.Double( sx2, sy2 ) };
						}
						else
						{
							result = new Point2D[] { new Point2D.Double( sx1, sy2 ), new Point2D.Double( sx2, sy1 ) };
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
			if ( ( ua >= -EPSILON ) && ( ua <= ( 1.0 + EPSILON ) ) ) // float round error fix
			{

				final double n2 = ( x2 - x1 ) * ( y1 - y3 ) - ( y2 - y1 ) * ( x1 - x3 );
				final double ub = n2 / d;

				if ( ( ub >= -EPSILON ) && ( ub <= ( 1.0 + EPSILON ) ) ) // float round error fix
				{
					final double x = x1 + ua * ( x2 - x1 );
					final double y = y1 + ua * ( y2 - y1 );

					result = new Point2D[] { new Point2D.Double( x, y ) };
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
	@Nullable
	public static Point2D getIntersectionBetweenLines( final Point2D p1, final Point2D p2, final Point2D p3, final Point2D p4 )
	{
		return getIntersectionBetweenLines( p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY() );
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
	@Nullable
	public static Point2D getIntersectionBetweenLines( final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final double x4, final double y4 )
	{
		Point2D result = null;

		final double d = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );
		if ( !almostEqual( d, 0.0 ) ) /* are not parallel, so they intersect at some point */
		{
			final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
			final double ua = n1 / d;

			final double x = x1 + ua * ( x2 - x1 );
			final double y = y1 + ua * ( y2 - y1 );

			result = new Point2D.Double( x, y );
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
	@Nullable
	public static Vector3D getIntersectionBetweenRayAndPolygon( final Polygon3D polygon, final Ray3D ray )
	{
		Vector3D result = null;

		final int vertexCount = polygon.getVertexCount();
		if ( vertexCount >= 3 )
		{
			result = getIntersectionBetweenRayAndPlane( polygon, ray );
			if ( ( result != null ) && !isPointInsidePolygon( polygon, result ) )
			{
				result = null;
			}
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
	@Nullable
	public static Vector3D getIntersectionBetweenRayAndPlane( final Matrix3D planeTransform, final boolean twoSidedPlane, final Ray3D ray )
	{
		return getIntersectionBetweenRayAndPlane( planeTransform.xz, planeTransform.yz, planeTransform.zz, Vector3D.dot( planeTransform.xz, planeTransform.yz, planeTransform.zz, planeTransform.xo, planeTransform.yo, planeTransform.zo ), twoSidedPlane, ray.getOrigin(), ray.getDirection(), ray.isHalfRay() );
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
	@Nullable
	public static Vector3D getIntersectionBetweenRayAndPlane( final Plane3D plane, final Ray3D ray )
	{
		final Vector3D planeNormal = plane.getNormal();
		return getIntersectionBetweenRayAndPlane( planeNormal.x, planeNormal.y, planeNormal.z, plane.getDistance(), plane.isTwoSided(), ray.getOrigin(), ray.getDirection(), ray.isHalfRay() );
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
	@Nullable
	public static Vector3D getIntersectionBetweenRayAndPlane( final double planeNormalX, final double planeNormalY, final double planeNormalZ, final double planeDistance, final boolean twoSidedPlane, final Vector3D rayOrigin, final Vector3D rayDirection, final boolean halfRay )
	{
		Vector3D result = null;

		final double denominator = planeNormalX * rayDirection.x
		                         + planeNormalY * rayDirection.y
		                         + planeNormalZ * rayDirection.z;

		if ( twoSidedPlane ? !almostEqual( denominator, 0.0 ) : significantlyLessThan( denominator, 0.0 ) ) /* line parallel to plane */
		{
			final double numerator = planeDistance - planeNormalX * rayOrigin.x
			                                       - planeNormalY * rayOrigin.y
			                                       - planeNormalZ * rayOrigin.z;

			final double intersectionDistance = numerator / denominator;

			if ( almostEqual( intersectionDistance, 0.0 ) ) /* (almost) on plane */
			{
				result = rayOrigin;
			}
			else if ( !halfRay || ( intersectionDistance > 0.0 ) ) /* complete ray, or ray pointing in plane direction */
			{
				final double x = rayOrigin.x + intersectionDistance * rayDirection.x;
				final double y = rayOrigin.y + intersectionDistance * rayDirection.y;
				final double z = rayOrigin.z + intersectionDistance * rayDirection.z;

				result = Vector3D.INIT.set( x, y, z );
			}
		}

		return result;
	}

	/**
	 * Get normal vector of plane defined by the specified set of points. The
	 * normal will point 'outwards' if the points are specified in clockwise
	 * ordering.
	 *
	 * @param   points  List of points to get normal from.
	 *
	 * @return  Plane normal vector;
	 *          <code>null</code> if no plane normal could be determined.
	 */
	@Nullable
	public static Vector3D getPlaneNormal( final Vector3D... points )
	{
		return ( points.length > 3 ) ? getPlaneNormal( points[ 0 ], points[ 1 ], points[ 2 ] ) : null;
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
	 * @return  Plane normal vector;
	 *          <code>null</code> if no plane normal could be determined.
	 *
	 * @see     Plane3D#getNormal
	 * @see     Plane3D#isTwoSided
	 */
	@Nullable
	public static Vector3D getPlaneNormal( final Vector3D p1, final Vector3D p2, final Vector3D p3 )
	{
		final double ux = p1.x - p2.x;
		final double uy = p1.y - p2.y;
		final double uz = p1.z - p2.z;

		final double vx = p3.x - p2.x;
		final double vy = p3.y - p2.y;
		final double vz = p3.z - p2.z;

		final double crossX = uy * vz - uz * vy;
		final double crossY = uz * vx - ux * vz;
		final double crossZ = ux * vy - uy * vx;

		final double l = Vector3D.length( crossX, crossY, crossZ );
		//noinspection FloatingPointEquality
		return ( l == 0.0 ) ? null : ( l == 1.0 ) ? new Vector3D( crossX, crossY, crossZ ) : new Vector3D( crossX / l, crossY / l, crossZ / l );
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
	public static boolean isPointInsidePolygon( final Polygon3D polygon, final Vector3D point )
	{
		return isPointInsidePolygon( polygon, point.x, point.y, point.z );
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
	public static boolean isPointInsidePolygon( final Polygon3D polygon, final double x, final double y, final double z )
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

			i = vertexCount - 1;
			double p2x = polygon.getX( i ) - x;
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
				if ( almostEqual( d, 0.0 ) ) /* We are on a node, consider this inside */
				{
					break;
				}

				angleSum += Math.acos( ( p1x * p2x + p1y * p2y + p1z * p2z ) / d );
			}

			/*
			 * Inside if:
			 *  - Aborted prematurely; or
			 *  - Angle sum is 2PI
			 */
			result = ( i < vertexCount ) || almostEqual( angleSum, TWO_PI );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Returns whether the given point is inside the specified triangle. The
	 * point must be on the same plane as the triangle. Otherwise the results
	 * are undefined.
	 *
	 * <p>
	 * This implementation first calculates the barycentric coordinates of the
	 * point and then performs a simple comparison in that coordinate space.
	 * Explanations of this and other methods can be found here:
	 * <a href="http://softsurfer.com/Archive/algorithm_0105/algorithm_0105.htm">http://softsurfer.com/Archive/algorithm_0105/algorithm_0105.htm</a> and
	 * <a href="http://www.blackpawn.com/texts/pointinpoly/default.html">http://www.blackpawn.com/texts/pointinpoly/default.html</a>
	 *
	 * @param   v1  First vertex of the triangle.
	 * @param   v2  Second vertex of the triangle.
	 * @param   v3  Third vertex of the triangle.
	 * @param   p   Point on the same plane as the triangle.
	 *
	 * @return  <code>true</code> if the point is inside the triangle.
	 */
	public static boolean isPointInsideTriangle( final Vector3D v1, final Vector3D v2, final Vector3D v3, final Vector3D p )
	{
		final double ux = v2.x - v1.x;
		final double uy = v2.y - v1.y;
		final double uz = v2.z - v1.z;
		final double vx = v3.x - v1.x;
		final double vy = v3.y - v1.y;
		final double vz = v3.z - v1.z;
		final double wx = p.x  - v1.x;
		final double wy = p.y  - v1.y;
		final double wz = p.z  - v1.z;

		// Various dot products
		final double uu = ux * ux + uy * uy + uz * uz;
		final double uv = ux * vx + uy * vy + uz * vz;
		final double vv = vx * vx + vy * vy + vz * vz;
		final double wu = wx * ux + wy * uy + wz * uz;
		final double wv = wx * vx + wy * vy + wz * vz;
		final double d = uv * uv - uu * vv;

		// Compute and test barycentric coordinates
		final boolean result;

		final double s = ( uv * wv - vv * wu ) / d;
		if ( s >= 0.0 && s <= 1.0 )
		{
			final double t = ( uv * wu - uu * wv ) / d;
			result = ( t >= 0.0 ) && ( s + t <= 1.0 );
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
	@Nullable
	public static Vector3D getClosestPointOnLine( final Vector3D p, final Vector3D p1, final Vector3D p2, final boolean segmentOnly )
	{
		final double dx = p2.x - p1.x;
		final double dy = p2.y - p1.y;
		final double dz = p2.z - p1.z;
		final double u  = ( ( p.x - p1.x ) * dx + ( p.y - p1.y ) * dy + ( p.z - p1.z ) * dz ) / ( dx * dx + dy * dy + dz * dz );

		return ( !segmentOnly || ( ( u >= -EPSILON ) && u <= ( 1.0 + EPSILON ) ) ) ? p1.plus( u * dx, u * dy, p.z ) : null;
	}

	/**
	 * Calculate area of specified triangle.
	 * <p />
	 * The area is calculated by using "Heron's formula".
	 *
	 * @param   p1  First point of the triangle.
	 * @param   p2  Second point of the triangle.
	 * @param   p3  Third point of the triangle.
	 *
	 * @return  Area of specified triangle.
	 */
	public static double getTriangleArea( final Point2D p1, final Point2D p2, final Point2D p3 )
	{
		final double a = p1.distance( p2 );
		final double b = p2.distance( p3 );
		final double c = p3.distance( p1 );
		final double p = ( a + b + c ) / 2.0;

		return Math.sqrt( p * ( p - a ) * ( p - b ) * ( p - c ) );
	}

	/**
	 * Test if the specified values are 'almost' equal (the difference between
	 * them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is the values are within a tolerance of
	 *          {@link #EPSILON} of eachother; <code>false</code> otherwise.
	 */
	public static boolean almostEqual( final double value1, final double value2 )
	{
		final double delta = value1 - value2;
		return ( delta <= EPSILON ) && ( delta >= -EPSILON );
	}

	/**
	 * Test if the first operand is greater than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is <code>value1</code> is greater than or within
	 *          a tolerance of {@link #EPSILON} of <code>value2</code>;
	 *          <code>false</code> otherwise.
	 */
	public static boolean greaterOrAlmostEqual( final double value1 , final double value2 )
	{
		return ( ( value2 - value1 ) <= EPSILON );
	}

	/**
	 * Test if the first operand is less than the second operand or almost
	 * equal (the difference between them approaches the value 0).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is <code>value1</code> is less than or within
	 *          a tolerance of {@link #EPSILON} of <code>value2</code>;
	 *          <code>false</code> otherwise.
	 */
	public static boolean lessOrAlmostEqual( final double value1, final double value2 )
	{
		return ( ( value1 - value2 ) <= EPSILON );
	}

	/**
	 * Test if the first operand is significantly greater than the second operand
	 * (the difference between them exceeds a tolerance of {@link #EPSILON}).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is <code>value1</code> is at least
	 *          {@link #EPSILON} greater than <code>value2</code>;
	 *          <code>false</code> otherwise.
	 */
	public static boolean significantlyGreaterThan( final double value1 , final double value2 )
	{
		return ( ( value1 - value2 ) > EPSILON );
	}

	/**
	 * Test if the first operand is significantly less than the second operand
	 * (the difference between them exceeds a tolerance of {@link #EPSILON}).
	 *
	 * @param   value1      First value to compare.
	 * @param   value2      Second value to compare.
	 *
	 * @return  <code>true</code> is <code>value1</code> is at least
	 *          {@link #EPSILON} less than <code>value2</code>;
	 *          <code>false</code> otherwise.
	 */
	public static boolean significantlyLessThan( final double value1, final double value2 )
	{
		return ( ( value2 - value1 ) > EPSILON );
	}

	/**
	 * Returns the radius of an arc with the given chord length and height.
	 *
	 * @param   chordLength     Distance between the arc's end points.
	 * @param   height          Height of the arc.
	 *
	 * @return  Radius of the arc.
	 */
	public static double arcHeightToRadius( final double chordLength, final double height )
	{
		final double halfChordLength = chordLength / 2.0;
		return ( halfChordLength * halfChordLength + height * height ) / ( 2.0 * height );
	}

	/**
	 * Returns the height of an arc with the given chord length and radius.
	 * For <code>radius > chordLength / 2</code>, there are two
	 * possible solutions. This method returns the solution closest to zero,
	 * which results in an included angle of at most 180 degrees.
	 *
	 * @param   chordLength     Distance between the arc's end points.
	 * @param   radius          Radius of the arc.
	 *
	 * @return  Height of the arc; <code>NaN</code> if there is no solution.
	 */
	public static double arcRadiusToHeight( final double chordLength, final double radius )
	{
		final double halfChordLength = chordLength / 2.0;
		return Math.signum( radius) * ( Math.abs( radius ) - Math.sqrt( radius * radius - halfChordLength * halfChordLength ) );
	}

	/**
	 * Returns the included angle of an arc with the given height.
	 *
	 * @param   chordLength     Distance between the arc's end points.
	 * @param   height          Height of the arc.
	 *
	 * @return  Included angle.
	 */
	public static double arcHeightToAngle( final double chordLength, final double height )
	{
		final double halfChordLength = chordLength / 2.0;
		return 4.0 * Math.atan2( height, halfChordLength );
	}

	/**
	 * Returns the height of an arc with the given included angle.
	 *
	 * @param   chordLength     Distance between the arc's end points.
	 * @param   angle           Included angle of the arc.
	 *
	 * @return  Height of the arc.
	 */
	public static double arcAngleToHeight( final double chordLength, final double angle )
	{
		final double halfChordLength = chordLength / 2.0;
		return halfChordLength * Math.tan( angle / 4.0 );
	}

	/**
	 * Returns the height of an arc between the given start and end points
	 * through the other point.
	 *
	 * @param   start   Start point of the arc.
	 * @param   end     End point of the arc.
	 * @param   point   Point on the arc.
	 * @param   normal  Normal of the plane containing the arc.
	 *
	 * @return  Arc height.
	 */
	public static double arcThroughPoint( final Vector3D start, final Vector3D end, final Vector3D point, final Vector3D normal )
	{
		// line segment SE is a chord of the circle
		final Vector3D s = start;
		final Vector3D e = end;

		// P is an arbitrary point on the circle
		final Vector3D p = point;

		// line segments SE and SP
		final Vector3D se = e.minus( s );
		final Vector3D sp = p.minus( s );

		final double result;

		// check for straight line
		final Vector3D n = Vector3D.cross( se, sp );
		if ( Vector3D.ZERO.almostEquals( n ) )
		{
			result = 0.0;
		}
		else
		{
			// midpoints of SE en SP
			final Vector3D halfSE = s.plus( 0.5 * se.x, 0.5 * se.y, 0.5 * se.z );
			final Vector3D halfSP = s.plus( 0.5 * sp.x, 0.5 * sp.y, 0.5 * sp.z );

			// direction of perpendiculars of SE and SP
			final Vector3D towardsCenterFromSE = Vector3D.cross( se, n );
			final Vector3D towardsCenterFromSP = Vector3D.cross( sp, n );

			// center point of the circle
			final Vector3D c = intersectLinesPointDirection( halfSE, towardsCenterFromSE, halfSP, towardsCenterFromSP );
			if ( c == null )
			{
				// lines must intersect
				throw new AssertionError( "no intersection found" );
			}

			// are points C and P on the same side of line segment SE?
			final Vector3D sc = c.minus( s );
			final double tripleProduct = Vector3D.dot( n, Vector3D.cross( se, sc ) );

			final double radius = Vector3D.distanceBetween( c, s );
			final double sagitta = Vector3D.distanceBetween( c, halfSE );
			final double arcHeight = radius + sagitta * Math.signum( tripleProduct );
			result = arcHeight * Math.signum( -Vector3D.dot( normal, n ) );
		}

		return result;
	}

	/**
	 * Intersects two lines, which are specified as a point and a direction.
	 * The directions don't have to be normalized.
	 *
	 * <p>This implementation is limited to 2D intersections.
	 * The z-coordinate of all input points is ignored (assumed zero).
	 *
	 * @see     <a href="http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/">Intersection point of two lines (2 dimensions)</a>
	 *
	 * @param   p1  Point on the first line.
	 * @param   d1  Direction of the first line.
	 * @param   p2  Point on the second line.
	 * @param   d2  Direction of the second line.
	 *
	 * @return  Intersection point.
	 */
	@Nullable
	private static Vector3D intersectLinesPointDirection( final Vector3D p1, final Vector3D d1, final Vector3D p2, final Vector3D d2 )
	{
		final Vector3D result;

		// p1 + ua . d1 = p2 + ub . d2
		// ua = (d2 × (p1 - p2)) / (d2 × d1)

		final double d = d2.y * d1.x - d2.x * d1.y;
		if ( almostEqual( d, 0.0 ) )
		{
			result = null;
		}
		else
		{
			final double ua = ( d2.x * ( p1.y - p2.y ) - d2.y * ( p1.x - p2.x ) ) / d;
			result = new Vector3D( p1.x + ua * d1.x, p1.y + ua * d1.y, p1.z );
		}

		return result;
	}
}
