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

/**
 * Pov Camera
 * <pre>
 * camera // name
 * {
 *     location  &lt; x , y , z &gt;
 *     up        &lt; 0 , 0 , 1 &gt;
 *     right     &lt; 1.33 , 0 , 0 &gt;
 *     sky       &lt; 0 , 0 , 1 &gt;
 *     look_at   &lt; x , y , z &gt;
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovCamera
	extends PovGeometry
{
	/**
	 * Location of camera.
	 */
	public final PovVector location;

	/**
	 * Direction/target of camera.
	 */
	public final PovVector lookAt;

	public PovVector right = new PovVector( 1.33 , 0 , 0 );

	public double angle = 30.0;

	/**
	 * Camera constructor comment.
	 */
	public PovCamera( String name , float x , float y , float z , float tx , float ty , float tz , double angle )
	{
		super( name );

		this.location = new PovVector( x , y , z );
		this.lookAt   = new PovVector( tx , ty , tz );
		this.angle    = angle;
	}

	/**
	 * Construct camera.
	 */
	public PovCamera( String name , PovVector location , PovVector lookAt , double angle )
	{
		super( name );

		this.location = location;
		this.lookAt   = lookAt;
		this.angle    = angle;
	}

	public PovCamera( final String name , final PovVector location , final PovVector lookAt , final PovVector right , final double angle )
	{
		this( name, location , lookAt , angle );
		if ( ( right != null ) && ( right.v.x > 0.0 ) )
			this.right = right;
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "camera // " + name );
		out.writeln( "{" );
		out.indentIn();

		if ( xform == null )
		{
			out.write( "location\t" );
			location.write( out );
			out.writeln();

			out.writeln( "up\t\t<0,0,1>" );

			out.writeln( "sky\t\t<0,0,1>" );

			out.write( "look_at\t" );
			lookAt.write( out );
			out.writeln();
		}

		out.write( "right    " );
		right.write( out );
		out.writeln();

		out.write( "angle    " );
		out.writeln( String.valueOf( angle ) );
		writeTransformation( out );

		out.indentOut();
		out.writeln( "}" );
	}

}
