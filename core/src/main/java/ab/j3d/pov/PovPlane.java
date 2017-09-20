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


/**
 * An infinite flat surface. The plane is not a thin boundary or can be compared
 * to a sheet of paper. A plane is a solid object of infinite size that divides
 * POV-space in two parts, inside and outside the plane.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class PovPlane
	extends PovGeometry
{
	/**
	 * Normal pointing out of the plane.
	 */
	private final PovVector _normal;

	/**
	 * Distance from the origin to the plane, in the direction of its normal.
	 */
	private final double _distance;

	/**
	 * Constructs new plane.
	 *
	 * @param   name        Name of the object.
	 * @param   normal      Normal pointing out of the plane.
	 * @param   distance    Distance from the origin to the plane, in the
	 *                      direction of the plane's normal.
	 */
	public PovPlane( final String name , final PovVector normal , final double distance )
	{
		super( name );
		_normal   = normal;
		_distance = distance;
	}

	/**
	 * Constructs new plane.
	 *
	 * @param   name        Name of the object.
	 * @param   texture     Texture to be applied to the object.
	 * @param   normal      Normal pointing out of plane.
	 * @param   distance    Distance from the origin to the plane, in the
	 *                      direction pointed by the plane's normal.
	 */
	public PovPlane( final String name , final PovTexture texture , final PovVector normal , final double distance )
	{
		super( name , texture );

		_normal   = normal;
		_distance = distance;
	}

	public void write( final PovWriter out )
		throws IOException
	{
		out.writeln( "plane" );
		out.writeln( "{" );
		out.indentIn();

		_normal.write( out );
		out.write( " , " );
		out.writeln( Double.toString( _distance ) );

		final PovTexture texture = getTexture();
		if ( texture != null )
		{
			texture.write( out );
		}

		out.indentOut();
		out.writeln( "}" );
	}
}
