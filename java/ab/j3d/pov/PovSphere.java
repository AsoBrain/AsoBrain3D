/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2007
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

import ab.j3d.Vector3D;

import com.numdata.oss.io.IndentingWriter;

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
	 * Center of the sphere.
	 */
	private PovVector _location;

	/**
	 * Radius of the sphere.
	 */
	private double _radius;

	/**
	 * Creates a sphere with name at specified position and size.
	 *
	 * @param   name        Name of the shape.
	 * @param   location    Position of the sphere.
	 * @param   radius      Radius of the sphere.
	 * @param   texture     Texture of the shape.
	 */
	public PovSphere( final String name , final Vector3D location , final double radius , final PovTexture texture )
	{
		this( name , new PovVector( location ) , radius , texture );
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

		_location = location;
		_radius   = radius;
	}

	/**
	 * Get center of the sphere.
	 *
	 * @return  Center of the sphere.
	 */
	public final PovVector getLocation()
	{
		return _location;
	}

	/**
	 * Set center of the sphere.
	 *
	 * @param   location    Center of the sphere.
	 */
	public final void setLocation( final PovVector location )
	{
		_location = location;
	}

	/**
	 * Get radius of the sphere.
	 *
	 * @return  Radius of the sphere.
	 */
	public final double getRadius()
	{
		return _radius;
	}

	/**
	 * Set radius of the sphere.
	 *
	 * @param   radius  Radius of the sphere.
	 */
	public final void setRadius( final double radius )
	{
		_radius = radius;
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		out.write( "sphere" );
		final String name = getName();
		if ( name != null )
		{
			out.write( " // " );
			out.write( name );
		}
		out.newLine();
		out.writeln( "{" );
		out.indentIn();

		final PovVector location = getLocation();
		location.write( out );
		out.write( ", " );
		out.write( format( getRadius() ) );
		out.newLine();

		writeModifiers( out );

		out.indentOut();
		out.writeln( "}" );
	}
}
