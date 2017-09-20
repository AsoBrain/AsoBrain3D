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
package ab.j3d.geom;

import ab.j3d.*;

/**
 * Abstract implementation of {@link Triangle3D} interface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractTriangle3D
	implements Triangle3D
{
	/**
	 * Normal vector of triangle. Calculated on-demand by {@link #getNormal}.
	 */
	protected Vector3D _normal;

	/**
	 * Perimeter of triangle. Calculated on-demand by {@link #getPerimeter}.
	 */
	protected double _perimeter;

	/**
	 * Area of triangle. Calculated on-demand by {@link #getArea}.
	 */
	protected double _area;

	/**
	 * Average point of triangle. Calculated on-demand by {@link #getAveragePoint}.
	 */
	protected Vector3D _averagePoint;

	/**
	 * Construct new AbstractTriangle3D.
	 */
	protected AbstractTriangle3D()
	{
		_normal = null;
		_perimeter = -1.0;
		_averagePoint = null;
		_area = -1.0;
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
			case 0 : result = getP1().x; break;
			case 1 : result = getP2().x; break;
			case 2 : result = getP3().x; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getY( final int index )
	{
		final double result;

		switch ( index )
		{
			case 0 : result = getP1().y; break;
			case 1 : result = getP2().y; break;
			case 2 : result = getP3().y; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getZ( final int index )
	{
		final double result;

		switch ( index )
		{
			case 0 : result = getP1().z; break;
			case 1 : result = getP2().z; break;
			case 2 : result = getP3().z; break;

			default : throw new IndexOutOfBoundsException( "No such vertex index for triangle: " + index );
		}

		return result;
	}

	public double getDistance()
	{
		return Vector3D.dot( getNormal(), getP1() );
	}

	public Vector3D getNormal()
	{
		Vector3D result = _normal;
		if ( result == null )
		{
			result = GeometryTools.getPlaneNormal( getP1(), getP2(), getP3() );
			_normal = result;
		}
		return result;
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
			result = calculatePerimeter( getP1(), getP2(), getP3() );
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
	public static double calculatePerimeter( final Vector3D p1, final Vector3D p2, final Vector3D p3 )
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
			result = calculateArea( getP1(), getP2(), getP3() );
			_area = result;
		}
		return result;
	}

	public Vector3D getAveragePoint()
	{
		Vector3D result = _averagePoint;
		if ( result == null )
		{
			final Vector3D p1 = getP1();
			final Vector3D p2 = getP2();
			final Vector3D p3 = getP3();
			result = p1.set( p1.x + p2.x + p3.x / 3.0, p1.y + p2.y + p3.y / 3.0, p1.z + p2.z + p3.z / 3.0 );
			_averagePoint = result;
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
	public static double calculateArea( final Vector3D p1, final Vector3D p2, final Vector3D p3 )
	{
		final double a = p1.distanceTo( p2 );
		final double b = p2.distanceTo( p3 );
		final double c = p3.distanceTo( p1 );
		final double p = ( a + b + c ) / 2.0;

		return Math.sqrt( p * ( p - a ) * ( p - b ) * ( p - c ) );
	}
}
