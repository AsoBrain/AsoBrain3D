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
package ab.j3d.pov;

import java.io.*;

import ab.j3d.*;


/**
 * Pov Vector (also used to define colors).
 * <pre>
 * &lt; x , y , z &gt;
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovVector
	extends PovObject
{
	/**
	 * Vector.
	 */
	private Vector3D _vector;

	/**
	 * Creates a vector based on a 3D vector.
	 *
	 * @param   v   3D vector.
	 */
	public PovVector( final Vector3D v )
	{
		_vector = v;
	}

	/**
	 * Creates a vector based on 3D coordinates.
	 *
	 * @param   x   X coordinate of vector.
	 * @param   y   Y coordinate of vector.
	 * @param   z   Z coordinate of vector.
	 */
	public PovVector( final float x , final float y , final float z )
	{
		_vector = new Vector3D( (double)x , (double)y , (double)z );
	}

	/**
	 * Creates a vector based on 3D coordinates.
	 *
	 * @param   x   X coordinate of vector.
	 * @param   y   Y coordinate of vector.
	 * @param   z   Z coordinate of vector.
	 */
	public PovVector( final double x , final double y , final double z )
	{
		_vector = new Vector3D( x , y , z );
	}

	/**
	 * Get X component of vector.
	 *
	 * @return  X component of vector.
	 */
	public double getX()
	{
		return _vector.x;
	}

	/**
	 * Get Y component of vector.
	 *
	 * @return  Y component of vector.
	 */
	public double getY()
	{
		return _vector.y;
	}

	/**
	 * Get Z component of vector.
	 *
	 * @return  Z component of vector.
	 */
	public double getZ()
	{
		return _vector.z;
	}

	/**
	 * Set vector.
	 *
	 * @param   vector  Vector to set.
	 */
	public void setVector3D( final Vector3D vector )
	{
		_vector = vector;
	}

	public void write( final PovWriter out )
		throws IOException
	{
		out.write( (int)'<' );
		out.write( format( getX() ) );
		out.write( (int)',' );
		out.write( format( getY() ) );
		out.write( (int)',' );
		out.write( format( getZ() ) );
		out.write( (int)'>' );
	}

	public boolean equals( final Object other )
	{
		final boolean result;

		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof PovVector )
		{
			final PovVector otherVector = (PovVector)other;
			result = _vector.equals( otherVector._vector );
		}
		else
		{
			result = false;
		}

		return result;
	}

	public int hashCode()
	{
		return _vector.hashCode();
	}

	public String toString()
	{
		return "PovVector[x=" + getX() + ",y=" + getY() + ",z=" + getZ() + ']';
	}
}
