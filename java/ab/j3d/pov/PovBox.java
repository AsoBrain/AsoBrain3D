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
import ab.j3d.model.Box3D;

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
	public final PovVector p1;

	/**
	 * Upper right back corner of box.
	 */
	public final PovVector p2;

	/**
	 * Construct box based on Box3D.
	 *
	 * @param   name        Name of shape.
	 * @param   box         Box3D specification.
	 * @param   texture     Texture for shape.
	 */
	public PovBox( final String name , final Box3D box , final PovTexture texture )
	{
		this( name , Vector3D.INIT , Vector3D.INIT.set( box.getDX() , box.getDY() , box.getDZ() ) , texture );

		setTransform( new PovMatrix( box.getTransform() ) );
	}

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

		this.p1 = p1;
		this.p2 = p2;
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
		if ( COMPACT && ( xform == null ) && ( translation == null ) && ( rotation == null ) )
		{
			out.write( "box { " + p1 + ", " + p2 );
			if ( texture != null )
			{
				out.write( " " );
				texture.write( out );
			}
			out.writeln( " } // " + name );
		}
		else
		{
			out.writeln( "box // " + name );
			out.writeln( "{" );
			out.indentIn();
			out.writeln( String.valueOf( p1 ) + "," );
			out.writeln( String.valueOf( p2 ) );
			writeTexture( out );
			writeTransformation( out );
			out.indentOut();
			out.write( "}\n" );
		}
	}
}
