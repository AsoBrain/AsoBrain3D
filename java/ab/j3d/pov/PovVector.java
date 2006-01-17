/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2006
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

import java.awt.Color;
import java.io.IOException;

import ab.j3d.Vector3D;

import com.numdata.oss.io.IndentingWriter;

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
	public final Vector3D v;

	/**
	 * Creates a vector based on a 3D vector.
	 *
	 * @param   v   3D vector.
	 */
	public PovVector( final Vector3D v )
	{
		this.v = v;
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
		v = Vector3D.INIT.set( x , y , z );
	}

	/**
	 * Creates a vector based on a java.awt.Color. The three values of the
	 * vector will be the r, g and b values of the color.
	 *
	 * @param   color   Color to base the vector on.
	 */
	public PovVector( final Color color )
	{
		this( (double)color.getRed() / 255.0 , (double)color.getGreen() / 255.0 , (double)color.getBlue() / 255.0 );
	}

	/**
	 * Check whether this object equals other object.
	 *
	 * @param other The object to be compared to this object.
	 * @return True if other equals this object, false otherwise.
	 */
	public boolean equals( final Object other )
	{
		boolean equals = false;

		if ( other instanceof PovVector )
		{
			final PovVector otherVector = (PovVector)other;
			if ( v.x == otherVector.v.x && v.y == otherVector.v.y && v.z == otherVector.v.z )
			{
				equals = true;
			}
		}

		return equals;
	}

	/**
	 * Returns a string representation of the vector.
	 *
	 * @return  String representation of the vector.
	 */
	public String toString()
	{
		return "< " + FLOAT_FORMAT.format( v.x ) + " , " + FLOAT_FORMAT.format( v.y ) + " , " + FLOAT_FORMAT.format( v.z ) + " >";
	}

	/**
	 * Writes the PovObject to the specified output stream.
	 * The method should use indentIn and indentOut to maintain the overview.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final IndentingWriter out )
		throws IOException
	{
		out.write( "< " );
		out.write( FLOAT_FORMAT.format( v.x ) );
		out.write( " , " );
		out.write( FLOAT_FORMAT.format( v.y ) );
		out.write( " , " );
		out.write( FLOAT_FORMAT.format( v.z ) );
		out.write( " >" );
	}

}
