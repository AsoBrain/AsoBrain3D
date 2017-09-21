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
 * Base class for all geometry in the POV world.
 * <p>
 * Note that the {@link PovLight} and the {@link PovCamera} are
 * {@link PovGeometry} as well.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class PovGeometry
	extends PovObject
	implements Comparable<PovGeometry>
{
	/**
	 * The name of the geometry object. Has no use in the Pov language, is only
	 * used to make the Pov source human-readable so objects can be identified.
	 */
	private String _name;

	/**
	 * Flat to indicate that an object is hollow.
	 * <p>
	 * POV-Ray by default assumes that objects are made of a solid material that
	 * completely fills the interior of an object. Hollow objects are very
	 * useful if you want atmospheric effects to exist inside an object. It is
	 * even required for objects containing an interior media.
	 * <p>
	 * In order to get a hollow CSG object you just have to make the top level
	 * object hollow. All children will assume the same hollow state except
	 * their state is explicitly set. The following example will set both
	 * spheres inside the union hollow:
	 * <pre>
	 * union {
	 *   sphere { -0.5*x, 1 }
	 *   sphere {  0.5*x, 1 }
	 *   hollow
	 * }
	 * </pre>
	 * while the next example will only set the second sphere hollow because the
	 * first sphere was explicitly set to be not hollow.
	 * <pre>
	 * union {
	 *   sphere { -0.5*x, 1 hollow off }
	 *   sphere {  0.5*x, 1 }
	 *   hollow on
	 * }
	 * </pre>
	 */
	private boolean _hollow;

	/**
	 * Flag to indicate that an object is inside-out.
	 * <p>
	 * When using CSG it is often useful to invert an object so that it'll be
	 * inside-out. The appearance of the object is not changed, just the way
	 * that POV-Ray perceives it. If set to inverse, the inside of the shape is
	 * flipped to become the outside and vice versa.
	 * <p>
	 * The inside/outside distinction is also important when attaching interior
	 * to an object especially if media is also used. Atmospheric media and fog
	 * also do not work as expected if your camera is inside an object. Using
	 * inverse is useful to correct that problem.
	 * <p>
	 * Finally the <code>internal_reflections</code> and
	 * <code>internal_highlights</code> keywords depend upon the inside/outside
	 * status of an object.
	 */
	private boolean _inverse;

	/**
	 * Flag to indicate that an object casts no shadow.
	 * <pre>
	 * This is useful for special effects and for creating the illusion that a
	 * light source actually is visible. It is useful for creating things like
	 * laser beams or other unreal effects. During test rendering it speeds
	 * things up if no_shadow is applied.
	 */
	private boolean _noShadow;

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
		_hollow      = false;
		_inverse     = false;
		_noShadow    = false;
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
	 * Test if object is hollow. POV-Ray by default assumes that objects are
	 * made of a solid material that completely fills the interior of an object.
	 *
	 * @return  <code>true</code> if this object is explicitly hollow;
	 *          <code>false</code> otherwise (may be hollow due to hollow parent).
	 */
	public boolean isHollow()
	{
		return _hollow;
	}

	/**
	 * Set object explicitly to hollow. POV-Ray by default assumes that objects
	 * are made of a solid material that completely fills the interior of an
	 * object. Hollow objects are very useful if you want atmospheric effects to
	 * exist inside an object. It is even required for objects containing an
	 * interior media.
	 * <p>
	 * In order to get a hollow CSG object you just have to make the top level
	 * object hollow. All children will assume the same hollow state except
	 * their state is explicitly set.
	 *
	 * @param   hollow      If <code>true</code>, the object is explicitly set
	 *                      to hollow (may still be implicitly hollow due to
	 *                      hollow parent).
	 */
	public void setHollow( final boolean hollow )
	{
		_hollow = hollow;
	}

	/**
	 * Test if object is inside-out. The appearance of the object is not
	 * changed, just the way that POV-Ray perceives it. If set to inverse, the
	 * inside of the shape is flipped to become the outside and vice versa.
	 *
	 * @return  <code>true</code> if the object is inside-out;
	 *          <code>false</code> if the object is outside-out.
	 */
	public boolean isInverse()
	{
		return _inverse;
	}

	/**
	 * Set object to be inside-out. When using CSG it is often useful to invert
	 * an object so that it'll be inside-out. The appearance of the object is
	 * not changed, just the way that POV-Ray perceives it. If set to inverse,
	 * the inside of the shape is flipped to become the outside and vice versa.
	 * <p>
	 * The inside/outside distinction is also important when attaching interior
	 * to an object especially if media is also used. Atmospheric media and fog
	 * also do not work as expected if your camera is inside an object. Using
	 * inverse is useful to correct that problem.
	 * <p>
	 * Finally the <code>internal_reflections</code> and
	 * <code>internal_highlights</code> keywords depend upon the inside/outside
	 * status of an object.
	 *
	 * @param   inverse     If <code>true</code>, the object is inside-out.
	 */
	public void setInverse( final boolean inverse )
	{
		_inverse = inverse;
	}

	/**
	 * Test if object casts no shadow. This is useful for special effects and for
	 * creating the illusion that a light source actually is visible. It is
	 * useful for creating things like laser beams or other unreal effects.
	 * During test rendering it speeds things up if no_shadow is applied.
	 * Flag to indicate that an object casts no shadow.
	 * <pre>
	 * This is useful for special effects and for creating the illusion that a
	 * light source actually is visible. It is useful for creating things like
	 * laser beams or other unreal effects. During test rendering it speeds
	 * things up if no_shadow is applied.
	 */
	public boolean isNoShadow()
	{
		return _noShadow;
	}

	/**
	 * Set object to cast no shadow. This is useful for special effects and for
	 * creating the illusion that a light source actually is visible. It is
	 * useful for creating things like laser beams or other unreal effects.
	 * During test rendering it speeds things up if no_shadow is applied.
	 *
	 * @param   noShadow        If <code>true</code>, the object casts no
	 *                          shadow.
	 */
	public void setNoShadow( final boolean noShadow )
	{
		_noShadow = noShadow;
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
	protected final void writeShortTransformation( final PovWriter out )
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
	 * Writes the modifiers of the geometry to the specified writer.
	 * <pre>
	 * OBJECT_MODIFIER:
	 *  clipped_by { UNTEXTURED_SOLID_OBJECT... } |
	 *  clipped_by { bounded_by } |
	 *  bounded_by { UNTEXTURED_SOLID_OBJECT... } |
	 *  bounded_by { clipped_by } |
	 *  inverse |
	 *  hollow |
	 *  no_shadow |
	 *  sturm [ Bool ] |
	 *  hierarchy [ Bool ] |
	 *  interior { INTERIOR_ITEMS... } |
	 *  texture { TEXTURE_BODY } |
	 *  pigment { PIGMENT_BODY } |
	 *  normal { NORMAL_BODY } |
	 *  finish { FINISH_ITEMS... } |
	 *  TRANSFORMATION
	 * </pre>
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	protected final void writeModifiers( final PovWriter out )
		throws IOException
	{
		/* inverse/hollow/no_shadow */
		if ( isInverse() )
		{
			out.writeln( "inverse" );
		}

		if ( isHollow() )
		{
			out.writeln( "hollow" );
		}

		if ( isNoShadow() )
		{
			out.writeln( "no_shadow" );
		}

		/* texture/pigment/finish */
		final PovTexture texture = getTexture();
		if ( texture != null )
		{
			texture.write( out );
		}

		/* TRANSFORMATION */
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
	public int compareTo( final PovGeometry other )
	{
		final int result;

		final boolean thisIsCamera  = ( this  instanceof PovCamera );
		final boolean otherIsCamera = ( other instanceof PovCamera );

		if ( thisIsCamera != otherIsCamera )
		{
			result = thisIsCamera ? -1 : 1;
		}
		else
		{
			final String thisName = getName();
			final String otherName = other.getName();
			result = ( thisName == null ) ? ( otherName != null ) ? -1 : 0 : ( otherName == null ) ? 1 : thisName.compareTo( otherName );
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
