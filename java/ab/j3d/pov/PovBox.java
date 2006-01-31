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

import ab.j3d.Vector3D;

import com.numdata.oss.io.IndentingWriter;

/**
 * Pov Box, 8 points / 12 faces.
 * <pre>
 * box // name
 * {
 *     &lt; x1 , y1 , z1 &gt;
 *     &lt; x2 , y2 , z2 &gt;
 *     [rotate]
 *     [matrix]
 *     [texture]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovBox
	extends PovGeometry
{
	public static final boolean COMPACT = true;

	/**
	 * Lower left front corner of box.
	 */
	private PovVector _p1;

	/**
	 * Upper right back corner of box.
	 */
	private PovVector _p2;

	/**
	 * Creates a box with name, bounds and texture using 6 floats.
	 *
	 * @param   name        Name of the shape.
	 * @param   v1          Lower-left-front corner of box.
	 * @param   v2          Upper-right-back corner of box.
	 * @param   texture     Texture of the shape.
	 */
	public PovBox( final String name , final Vector3D v1 , final Vector3D v2 , final PovTexture texture )
	{
		this( name , new PovVector( v1 ) , new PovVector( v2 ) , texture );
	}

	/**
	 * Creates a box with name, bounds and texture
	 * using 2 vectors.
	 *
	 * @param   name        Name of the shape.
	 * @param   p1          Lower-left-front corner of box.
	 * @param   p2          Upper-right-back corner of box.
	 * @param   texture     Texture of the shape.
	 */
	public PovBox( final String name , final PovVector p1 , final PovVector p2 , final PovTexture texture )
	{
		super( name , texture );

		_p1 = p1;
		_p2 = p2;
	}

	/**
	 * Get lower-left-front corner of box.
	 *
	 * @return  Lower-left-front corner of box.
	 */
	public PovVector getP1()
	{
		return _p1;
	}

	/**
	 * Set lower-left-front corner of box.
	 *
	 * @param   p1  Lower-left-front corner of box.
	 */
	public void setP1( final PovVector p1 )
	{
		_p1 = p1;
	}

	/**
	 * Get upper-right-back corner of box.
	 *
	 * @return  Upper-right-back corner of box.
	 */
	public PovVector getP2()
	{
		return _p2;
	}

	/**
	 * Set upper-right-back corner of box.
	 *
	 * @param   p2  Upper-right-back corner of box.
	 */
	public void setP2( final PovVector p2 )
	{
		_p2 = p2;
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		final PovVector p1 = getP1();
		final PovVector p2 = getP2();

		if ( COMPACT && !isTransformed() )
		{
			out.write( "box { " );
			p1.write( out );
			out.write( ", " );
			p2.write( out );

			final PovTexture texture = getTexture();
			if ( texture != null )
			{
				out.write( (int)' ' );
				texture.write( out );
			}

			out.write( " }" );

			final String name = getName();
			if ( name != null )
			{
				out.write( " // " );
				out.write( name );
			}

			out.newLine();
		}
		else
		{
			out.write( "box" );
			final String name = getName();
			if ( name != null )
			{
				out.write( " // " );
				out.write( name );
			}
			out.newLine();
			out.writeln( "{" );
			out.indentIn();

			p1.write( out );
			out.write( (int)',' );
			out.newLine();

			p2.write( out );
			out.newLine();

			writeTexture( out );
			writeTransformation( out );

			out.indentOut();
			out.write( (int)'}' );
			out.newLine();
		}
	}
}
