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

import java.io.IOException;

import com.numdata.oss.io.IndentingWriter;

/**
 * A reference to declared geometry.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovDeclared
	extends PovGeometry
{
	/**
	 * Creates a new reference to a declared shape.
	 *
	 * @param   reference   Name of the shape to reference.
	 */
	public PovDeclared( final String reference )
	{
		super( getDeclaredName( reference ) );
	}

	/**
	 * Creates a new reference to a declared shape.
	 *
	 * @param   reference   Name of the shape to reference.
	 * @param   texture     New texture for the shape.
	 */
	public PovDeclared( final String reference, final PovTexture texture )
	{
		super( getDeclaredName( reference ) , texture );
	}

	/**
	 * Gets the name of the declared shape.
	 *
	 * @param   name    Name of the shape to reference.
	 *
	 * @return  Name of the reference.
	 */
	public static String getDeclaredName( final String name )
	{
		return "SHAPE_" + name.replace( '.' , '_' );
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		out.write( "object { " );
		out.write( getName() );
		out.write( (int)' ' );
		writeShortTransformation( out );
		out.writeln( "}" );
	}
}
