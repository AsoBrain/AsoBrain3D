/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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
 * Defines a triangle in 3D space.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Triangle3D
	implements Polygon3D
{
	/**
	 * First point of the triangle.
	 */
	private final Vector3D _p1;

	/**
	 * Second point of the triangle.
	 */
	private final Vector3D _p2;

	/**
	 * Third point of the triangle.
	 */
	private final Vector3D _p3;

	/**
	 * Plane is two-sided.
	 */
	private final boolean _twoSided;

	/**
	 * Normal vector of triangle. Calculated on-demand by {@link #getNormal}.
	 */
	private Vector3D _normal;

	/**
	 * Perimeter of triangle. Calculated on-demand by {@link #getPerimeter}.
	 */
	private double _perimeter;

	/**
	 * Area of triangle. Calculated on-demand by {@link #getArea}.
	 */
	private double _area;

	/**
	 * Construct new triangle 3D.
	 *
	 * @param   p1          First point of the triangle.
	 * @param   p2          Second point of the triangle.
	 * @param   p3          Third point of the triangle.
	 * @param   twoSided    Plane is two-sided.
	 */
	public Triangle3D( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 , final boolean twoSided )
	{
		_p1        = p1;
		_p2        = p2;
		_p3        = p3;
		_twoSided  = twoSided;
		_normal    = null;
		_perimeter = -1.0;
		_area      = -1.0;
	}

	public int getVertexCount()
	{
		return 3;
	}

	public double getX( final int index )
	{
		final double result;

		switch ( index )
		{
			case 0 : result = _p1.x; break;
			case 1 : result = _p2.x; break;
			case 2 : result = _p3.x; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getY( final int index )
	{
		final double result;

		switch ( index )
		{
			case 0 : result = _p1.y; break;
			case 1 : result = _p2.y; break;
			case 2 : result = _p3.y; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getZ( final int index )
	{
		final double result;

		switch ( index )
		{
			case 0 : result = _p1.z; break;
			case 1 : result = _p2.z; break;
			case 2 : result = _p3.z; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getDistance()
	{
		return Vector3D.dot( getNormal() , _p1 );
	}

	public Vector3D getNormal()
	{
		Vector3D result = _normal;
		if ( result == null )
		{
			result = GeometryTools.getPlaneNormal( _p1 , _p2 , _p3 );
			_normal = result;
		}
		return result;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	/**
	 * Get perimeter this triangle.
	 *
	 * @return  Perimeter of this triangle.
	 */
	public double getPerimeter()
	{
		double result = _perimeter;
		if ( result == -1.0 )
		{
			result = calculatePerimeter( _p1 , _p2 , _p3 );
			_perimeter = result;
		}
		return result;
	}

	/**
	 * Calculate perimeter of specified triangle.
	 *
	 * @param   p1  First point of the triangle.
	 * @param   p2  Second point of the triangle.
	 * @param   p3  Third point of the triangle.
	 *
	 * @return  Perimeter of specified triangle.
	 */
	public static double calculatePerimeter( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 )
	{
		final double edge1Length = p1.distanceTo( p2 );
		final double edge2Length = p2.distanceTo( p3 );
		final double edge3Length = p3.distanceTo( p1 );

		return edge1Length + edge2Length + edge3Length;
	}

	/**
	 * Get area of this triangle.
	 *
	 * @return  Area of this triangle.
	 */
	public double getArea()
	{
		double result = _area;
		if ( result == -1.0 )
		{
			result = calculateArea( _p1 , _p2 , _p3 );
			_area = result;
		}
		return result;
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
	public static double calculateArea( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 )
	{
		final double a = p1.distanceTo( p2 );
		final double b = p2.distanceTo( p3 );
		final double c = p3.distanceTo( p1 );
		final double p = ( a + b + c ) / 2.0;

		return Math.sqrt( p * ( p - a ) * ( p - b ) * ( p - c ) );
	}
}