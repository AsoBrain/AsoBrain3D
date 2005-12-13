/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2005
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

import ab.j3d.Vector3D;

/**
 * Pov Sphere.
 * <pre>
 * sphere // name
 * {
 *     &lt; x , y , z &gt; , radius
 *     [rotate]
 *     [matrix]
 *     [texture]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovSphere
	extends PovGeometry
{
	/**
	 * The center of the sphere.
	 */
	public final PovVector location;

	/**
	 * The radius of the sphere.
	 */
	public final double  radius;

	/**
	 * Creates a sphere with name at specified position and size.
	 *
	 * @param   name        Name of the shape.
	 * @param   location    Position of the sphere.
	 * @param   r           Radius of the sphere.
	 * @param   texture     Texture of the shape.
	 */
	public PovSphere( final String name , final Vector3D location , final double r , final PovTexture texture )
	{
		this( name , new PovVector( location ) , r , texture );
	}

	/**
	 * Creates a sphere with name at specified position and size.
	 *
	 * @param   name        Name of the shape.
	 * @param   location    Position of the sphere.
	 * @param   radius      Radius of the sphere.
	 * @param   texture     Texture of the shape.
	 */
	public PovSphere( final String name , final PovVector location , final double radius , final PovTexture texture )
	{
		super( name , texture );
		this.location = location;
		this.radius = radius;
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
		out.writeln( "sphere // " + name );
		out.writeln( "{" );
		out.indentIn();
		out.write( String.valueOf( location ) );
		out.write( ", " );
		out.writeln( String.valueOf( radius ) );
		writeTransformation( out );
		writeTexture( out );
		out.indentOut();
		out.writeln( "}" );
	}
}
