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
 * Base class for all geometry in the POV world.
 * <p>
 * Note that the {@link PovLight} and the {@link PovCamera} are
 * {@link PovGeometry} aswell.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class PovGeometry
	extends PovObject
	implements Comparable
{
	/**
	 * The name of the geometry object. Has no use in the Pov language, is only
	 * used to make the Pov source human-readable so objects can be identified.
	 */
	private String _name;

	/**
	 * The texture applied to this geometry object (<code>null</code> if no
	 * texture is applied).
	 */
	private PovTexture _texture;

	/**
	 * Transformation matrix applied to this geometry object. Set to
	 * <code>null</code> if no transformation matrix is applied.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 */
	private PovMatrix _transform;

	/**
	 * Rotation of the geometry (vector with decimal degrees for each axis).
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 */
	private PovVector _rotation;

	/**
	 * Translation of the geometry.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 */
	private PovVector _translation;

	/**
	 * Construct geometry object with the specified name and no texture.
	 *
	 * @param   name    Name of object.
	 */
	protected PovGeometry( final String name )
	{
		this( name , null );
	}

	/**
	 * Construct geometry object with the specified name and texture.
	 *
	 * @param   name        Name of object.
	 * @param   texture     Optional texture to apply to object.
	 */
	protected PovGeometry( final String name , final PovTexture texture )
	{
		_name        = name;
		_texture     = texture;
		_transform   = null;
		_rotation    = null;
		_translation = null;
	}

	/**
	 * Get the name of the geometry object. Has no use in the Pov language, is only
	 * used to make the Pov source human-readable so objects can be identified.
	 *
	 * @return  Name of the object;
	 *          <code>null</code> if the object has no name.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Set the name of the geometry object. Has no use in the Pov language, is only
	 * used to make the Pov source human-readable so objects can be identified.
	 *
	 * @param   name    Optional name of the object.
	 */
	public final void setName( final String name )
	{
		_name = name;
	}

	/**
	 * Get the texture that is applied to this geometry object.
	 *
	 * @return  Texture that is applied to this geometry object;
	 *          <code>null</code> if no texture is applied.
	 */
	public final PovTexture getTexture()
	{
		return _texture;
	}

	/**
	 * Set the texture to apply to this geometry object.
	 *
	 * @param   texture     Texture to apply to this geometry object
	 *                      (<code>null</code> to apply no texture).
	 */
	public final void setTexture( final PovTexture texture )
	{
		_texture = texture;
	}

	/**
	 * Test if geometry is transformed/rotated/translated.
	 *
	 * @return  <code>true</code> if the geometry is transformed/rotated/translated;
	 *          <code>false</code> otherwise.
	 */
	protected boolean isTransformed()
	{
		return ( ( getTransform() != null ) || ( getTranslation() != null ) || ( getRotation() != null ) );
	}

	/**
	 * Get transformation matrix applied to this geometry object.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @return  Transformation matrix;
	 *          <code>null</code> if no transformation matrix is used.
	 */
	public final PovMatrix getTransform()
	{
		return _transform;
	}

	/**
	 * Set transformation matrix to apply to this geometry object.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @param   transform   Transformation matrix (<code>null</code> to
	 *                      clear transformation matrix).
	 */
	public final void setTransform( final PovMatrix transform )
	{
		_transform = transform;
	}

	/**
	 * Get rotation for this geometry.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @return  Rotation (vector with decimal degrees for each axis);
	 *          <code>null</code> if geometry is not rotated.
	 */
	public final PovVector getRotation()
	{
		return _rotation;
	}

	/**
	 * Set rotation for this geometry.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @param   rotation    Rotation (vector with decimal degrees for each axis;
	 *                      <code>null</code> to clear rotation).
	 */
	public final void setRotation( final PovVector rotation )
	{
		_rotation = rotation;
	}

	/**
	 * Get translation for this geometry.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @return  Translation (vector);
	 *          <code>null</code> if geometry is not translated.
	 */
	public final PovVector getTranslation()
	{
		return _translation;
	}

	/**
	 * Set translation for this geometry.
	 * <p>
	 * NOTE: Either rotation/translation or transform should be used, not both.
	 *
	 * @param   translation     Translation (vector; <code>null</code> to clear
	 *                          translation).
	 */
	public final void setTranslation( final PovVector translation )
	{
		_translation = translation;
	}

	/**
	 * Writes the transformation part of rthe geometry to the specified writer.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected final void writeShortTransformation( final IndentingWriter out )
		throws IOException
	{
		final PovMatrix transform = getTransform();
		if ( transform != null )
		{
			out.write( " matrix " );
			transform.writeShort( out );
		}

		final PovVector rotation = getRotation();
		if ( rotation != null )
		{
			out.write( "rotate " );
			rotation.write( out );
		}

		final PovVector translation = getTranslation();
		if ( translation != null )
		{
			out.write( "translate " );
			translation.write( out );
		}
	}

	/**
	 * Writes the texture part of the geometry to the specified writer.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected final void writeTexture( final IndentingWriter out )
		throws IOException
	{
		final PovTexture texture = getTexture();
		if ( texture != null )
			texture.write( out );
	}

	/**
	 * Writes the transformation part of the geometry to the specified writer.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected final void writeTransformation( final IndentingWriter out )
		throws IOException
	{
		final PovMatrix transform = getTransform();
		if ( transform != null )
		{
			transform.write( out );
		}

		final PovVector rotation = getRotation();
		if ( rotation != null )
		{
			out.write( "rotate " );
			rotation.write( out );
			out.newLine();
		}

		final PovVector translation = getTranslation();
		if ( translation != null )
		{
			out.write( "translate " );
			translation.write( out );
			out.newLine();
		}
	}

	/**
	 * Implemtation of {@link Comparable#compareTo} from comparable interface,
	 * compares one geometry to another to enable sorting.
	 *
	 * @param   other   Other geometry to compare with this one.
	 *
	 * @return  <0 if other object is greater;
	 *          0  if other object is equal;
	 *          >1 if other object is smaller.
	 */
	public int compareTo( final Object other )
	{
		final int result;

		final boolean thisIsCamera  = ( this  instanceof PovCamera );
		final boolean otherIsCamera = ( other instanceof PovCamera );

		if ( thisIsCamera != otherIsCamera )
		{
			result = thisIsCamera ? -1 : 1;
		}
		else if ( other instanceof PovGeometry )
		{
			final String thisName  = getName();
			final String otherName = ((PovGeometry)other).getName();

			result = ( thisName == null ) ? ( ( otherName == null ) ? 0 : 1 ) : thisName.compareTo( otherName );
		}
		else
		{
			result = 0;
		}

		return result;
	}

	/**
	 * String representation of the geometry. Simply returns the name.
	 *
	 * @return  String representation of the geometry.
	 */
	public String toString()
	{
		return getName();
	}
}
