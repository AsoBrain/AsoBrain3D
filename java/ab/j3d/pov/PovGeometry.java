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
 * Baseclass for all geometry in the Pov world.
 * Note that the Light and the Camera are PovGeometry also.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class PovGeometry
	extends PovObject
	implements Comparable
{
	/**
	 * The name of the geometry. Has no use in the Pov language, is only used
	 * to make the Pov source human-readable so objects can be identified.
	 */
	public final String name;

	/**
	 * The texture of the geometry. Not used by PovCamera and PovLight.
	 */
	public final PovTexture texture;

	/**
	 * Rotation of the geometry.
	 * NOTE: Either rotation or xform should be used, not both.
	 */
	protected PovVector rotation = null;

	/**
	 * Transformation of the geometry.
	 * NOTE: Either rotation or xform should be used, not both.
	 */
	protected PovMatrix xform = null;

	/**
	 * Translation of the geometry.
	 * NOTE : Either translation or xform should be used, not both.
	 */
	protected PovVector translation = null;

	/**
	 * PovGeometry constructor comment.
	 */
	public PovGeometry( String name )
	{
		this.name = name;
		this.texture = null;
	}

	/**
	 * PovGeometry constructor comment.
	 */
	public PovGeometry( String name , PovTexture texture )
	{
		this.name = name;
		this.texture = texture;
	}

	/**
	 * Implemtation of compareTo from comparable interface,
	 * compares one geometry to another to enable sorting.
	 *
	 * @param   other	the other geometry to check with this one.
	 *
	 * @return < 0 if other object is greater.
	 * 			  0 if other object is equal
	 * 			> 1 if other object is smaller.
	 */
	public int compareTo( Object other )
	{
		if ( (this instanceof PovCamera) && !(other instanceof PovCamera) )
			return -1;
		if ( !(this instanceof PovCamera) && (other instanceof PovCamera) )
			return 1;

		if ( other instanceof PovGeometry )
			return name.compareTo( ((PovGeometry)other).name );

		return 0;
	}

	/**
	 * Gets the transformation of the geometry.
	 *
	 * @return transformation matrix.
	 */
	public PovMatrix getTransform()
	{
		return xform;
	}

	/**
	 * Set rotation for this geometry.
	 *
	 * @param   rotation	the new rotation.
	 */
	public void setRotation( PovVector rotation )
	{
		this.rotation = rotation;
	}

	/**
	 * Set transformation for this geometry.
	 *
	 * @param   xform	the new transformation.
	 */
	public void setTransform( PovMatrix xform )
	{
		this.xform = xform;
	}

	/**
	 * Set translation for this geometry.
	 *
	 * @param   translation		the new translation.
	 */
	public void setTranslation( PovVector translation )
	{
		this.translation = translation;
	}

	/**
	 * String representation of the geometry, simply returns the name
	 *
	 * @return String representation of the geometry.
	 */
	public String toString()
	{
		return name;
	}

	/**
	 * Writes the transformation part of the geometry to the
	 * output stream.
	 *
	 * @param   out		the stream to print to.
	 */
	protected void writeShortTransformation( IndentingWriter out )
		throws IOException
	{
		if ( xform != null )
		{
			out.write( " matrix " );
			xform.writeShort( out );
		}
		if ( rotation != null )
		{
			out.write( " rotate " + rotation );
		}
		if ( translation != null )
		{
			out.write( " translate " + translation );
		}
	}

	/**
	 * Writes the texture part of the geometry to the
	 * output stream.
	 *
	 * @param   out		the stream to print to.
	 */
	protected void writeTexture( IndentingWriter out )
		throws IOException
	{
		if ( texture != null )
		{
			texture.write( out );
		}
	}

	/**
	 * Writes the transformation part of the geometry to the
	 * output stream.
	 *
	 * @param   out		the stream to print to.
	 */
	protected void writeTransformation( IndentingWriter out )
		throws IOException
	{
		if ( xform != null )
		{
			xform.write( out );
			out.indentOut();
		}
		if ( rotation != null )
		{
			out.writeln( "rotate " + rotation );
		}
		if ( translation != null )
		{
			out.writeln( "translate " + translation );
		}
	}

}
