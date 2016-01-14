/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.pov;

import java.io.*;
import java.net.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Pov Texture / material definition.
 *
 * <p>Definition of texture (RGB):</p>
 * <pre>
 * texture
 * {
 *     pigment { color rgb &lt; r, g, b &gt; }
 *     finish
 *     {
 *        [ambient    rgb &lt; ar, ag, ab &gt]
 *        [diffuse    d]
 *        [phong      p]
 *        [phong_size ps]
 *        [specular   s]
 *        [metallic]
 *        [reflection r]
 *     }
 *     [scale &lt; x, y, z&gt;]
 * }
 * </pre>
 *
 * <p>Definition of texture (Texturemap):</p>
 * <pre>
 * texture
 * {
 *     pigment { image_map { (gif/png) "filename" } }
 *     finish
 *     {
 *        [ambient    rgb &lt; ar, ag, ab &gt]
 *        [diffuse    d]
 *        [phong      p]
 *        [phong_size ps]
 *        [specular   s]
 *        [metallic]
 *        [reflection r]
 *     }
 *     [scale &lt; x, y, z&gt;]
 * }
 * </pre>
 *
 * <p>(Rotated) reference to predefined texture (SubTexture):</p>
 * <pre>
 * texture
 * {
 *     referenceName [rotate &lt; x, y, z &gt; ]
 * }
 * </pre>
 *
 * @author Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovTexture
extends PovObject
{
	/**
	 * Name of texture (for declaration).
	 */
	private String _name;

	/**
	 * A free material.
	 */
	private String _free;

	/**
	 * Indicates the texture has been declared, so when used we only have to
	 * specify reference name.
	 */
	private boolean _declared;

	/**
	 * In case of RGB texture, the rgb value.
	 */
	private PovVector _rgb;

	/**
	 * In case of image map, URL to the image file.
	 */
	private URL _image;

	/**
	 * Ambient reflectivity factor.
	 */
	private PovVector _ambient;

	/**
	 * Diffuse reflectivity factor.
	 */
	private double _diffuse;

	/**
	 * Specular reflectivity factor.
	 */
	private double _specular;

	/**
	 * Reflection factor parallel to surface normal.
	 */
	private PovVector _reflectionMin;

	/**
	 * Reflection factor perpendicular to surface normal.
	 */
	private PovVector _reflectionMax;

	/**
	 * Metallic texture flag.
	 */
	private boolean _metallic;

	/**
	 * Specifies the amount of filtered transparency of a substance.
	 */
	private double _filter;

	/**
	 * Specifies the amount of non-filtered light that is transmitted through a
	 * surface.
	 */
	private double _transmit;

	/**
	 * Phong highlight amount.
	 */
	private double _phong;

	/**
	 * Phong highlight spot size.
	 */
	private double _phongSize;

	/**
	 * Scaling factor for texture image maps.
	 */
	private PovVector _scale;

	/**
	 * Modified reference to a Texture. Currently only support rotation.
	 */
	public static class Reference
	extends PovTexture
	{
		/**
		 * The texture that is referenced.
		 */
		private final PovTexture _reference;

		/**
		 * The rotation of the texture.
		 */
		private final PovVector _rotation;

		/**
		 * Construct reference to PovTexture instance.
		 *
		 * @param reference Referenced texture.
		 * @param rotation  Rotation to be applied.
		 */
		public Reference( final PovTexture reference, final PovVector rotation )
		{
			super( getReferenceCode( reference._name, rotation ) );

			_rotation = rotation;
			_reference = reference;
		}

		@Override
		public void write( final PovWriter out )
		throws IOException
		{
			if ( isDeclared() )
			{
				out.write( "texture { " );
				out.write( getName() );
				out.write( " }" );
			}
			else
			{
				out.write( "texture { " );
				out.write( _reference._name );

				if ( _rotation != null )
				{
					out.write( " rotate " );
					_rotation.write( out );
				}

				out.write( " }" );
			}
		}

		@Override
		public final boolean isDeclared()
		{
			return ( ( _rotation == null ) ? _reference.isDeclared() : super.isDeclared() );
		}

		@Override
		public final void declare( final PovWriter out )
		throws IOException
		{
			final PovTexture reference = _reference;

			if ( !reference.isDeclared() )
			{
				reference.declare( out );
			}

			if ( _rotation != null )
			{
				out.write( "#declare " );
				out.write( getName() );
				out.write( " = " );
				out.indentIn();
				write( out );
				out.newLine();
				out.indentOut();
			}

			reference._declared = true;
		}
	}

	/**
	 * Constructs a new texture instance with the defaults set. The constructed
	 * instance cannot be used as is; either the name of the texture or a free-form
	 * texture definition must be specified.
	 */
	private PovTexture()
	{
		_name = null;
		_free = null;
		_declared = false;
		_rgb = null;
		_image = null;
		_phong = 0.0;
		_phongSize = 0.0;
		_ambient = null;
		_diffuse = 0.0;
		_specular = 0.0;
		_reflectionMin = null;
		_reflectionMax = null;
		_filter = 0.0;
		_transmit = 0.0;
		_scale = null;
		_metallic = false;
	}

	/**
	 * Creates a texture only based on a name. This constructor may only be used by
	 * descendants of this class. It is currently used to create a texture
	 * reference.
	 *
	 * @param name Name of the texture.
	 */
	protected PovTexture( final String name )
	{
		this();
		_name = name;
	}

	/**
	 * Creates a texture based on red, green and blue values.
	 *
	 * @param name Name of the texture.
	 * @param r    Red value of the color.
	 * @param g    Green value of the color.
	 * @param b    Blue value of the color.
	 */
	public PovTexture( final String name, final double r, final double g, final double b )
	{
		this();
		_name = name;
		_rgb = new PovVector( r, g, b );
	}

	/**
	 * Creates a texture map using the specified map.
	 *
	 * @param name  Name of the texture.
	 * @param image URL to image map to use.
	 */
	public PovTexture( final String name, final URL image )
	{
		this();
		_name = name;
		_image = image;
	}

	/**
	 * Creates a texture that can be specified as you would specify a texture in
	 * pov-ray yourself. Nothing will be generated, only the string specified as
	 * free will be printed. Use this constructor with care, since you can
	 * introduce errors in the scene.
	 *
	 * @param name Name of the texture.
	 * @param free Definition of the texture.
	 */
	public PovTexture( final String name, final String free )
	{
		this();
		_name = name;
		_free = free;
	}

	/**
	 * Create texture from {@link Appearance} object.
	 *
	 * @param appearance {@link Appearance} to construct texture from.
	 */
	public PovTexture( final Appearance appearance )
	{
		this();
		_name = getName( appearance );

		final Color4 ambientColor = appearance.getAmbientColor();
		final Color4 diffuseColor = appearance.getDiffuseColor();
		final Color4 specularColor = appearance.getSpecularColor();

		final TextureMap colorMap = appearance.getColorMap();
		if ( ( colorMap != null ) && !"file".equals( colorMap.getImageUrl().getProtocol() ) )
		{
			throw new IllegalArgumentException( "Color map image must have 'file' URL for POV-Ray, but is '" + colorMap.getImageUrl() + "')." );
		}

		final int shininess = appearance.getShininess();
		final CubeMap reflectionMap = appearance.getReflectionMap();

		final double diffuseRed = Math.max( (double)diffuseColor.getRedFloat(), 0.001 );
		final double diffuseGreen = Math.max( (double)diffuseColor.getGreenFloat(), 0.001 );
		final double diffuseBlue = Math.max( (double)diffuseColor.getBlueFloat(), 0.001 );
		final double ambientRed = (double)ambientColor.getRedFloat() / diffuseRed;
		final double ambientGreen = (double)ambientColor.getGreenFloat() / diffuseGreen;
		final double ambientBlue = (double)ambientColor.getBlueFloat() / diffuseBlue;

		_rgb = new PovVector( diffuseRed, diffuseGreen, diffuseBlue );
		_image = ( colorMap != null ) ? colorMap.getImageUrl() : null;
		setAmbient( new PovVector( ambientRed, ambientGreen, ambientBlue ) );
		setDiffuse( 1.0 );
		setTransmit( (double)( 1.0f - diffuseColor.getAlphaFloat() ) );
		setPhong( (double)specularColor.getLuminance() );
		setPhongSize( 0.25 * (double)shininess );

		if ( reflectionMap != null )
		{
			final double min = (double)appearance.getReflectionMin();
			final double max = (double)appearance.getReflectionMax();
			if ( ( min > 0.0 ) || ( max > 0.0 ) )
			{
				final Color4 reflectionColor = appearance.getReflectionColor();
				final double red = (double)reflectionColor.getRedFloat();
				final double green = (double)reflectionColor.getGreenFloat();
				final double blue = (double)reflectionColor.getBlueFloat();

				final PovVector reflectionMin = new PovVector( min * red, min * green, min * blue );
				final PovVector reflectionMax = new PovVector( max * red, max * green, max * blue );
				setReflection( reflectionMin, reflectionMax );
			}
		}
	}

	/**
	 * Returns the name used when a texture is created for the given appearance.
	 *
	 * @param appearance Appearance to generate a name for.
	 *
	 * @return Texture name based on the given appearance.
	 */
	@NotNull
	public static String getName( final Appearance appearance )
	{
		return "APPEARANCE_" + Integer.toHexString( System.identityHashCode( appearance ) );
	}

	/**
	 * Returns the name of the texture with specified rotation if it is
	 * referenced.
	 *
	 * @param parentCode Name of the texture to reference.
	 * @param rotation   Rotation of the texture when referencing.
	 *
	 * @return Name of texture with specified rotation if it is referenced.
	 */
	public static String getReferenceCode( final String parentCode, final PovVector rotation )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( parentCode );

		if ( rotation == null )
		{
			sb.append( "_REF" );
		}
		else
		{
			final int rx = (int)Math.round( rotation.getX() );
			final int ry = (int)Math.round( rotation.getY() );
			final int rz = (int)Math.round( rotation.getZ() );

			sb.append( ( rx < 0 ) ? "_m" : "_" );
			sb.append( ( rx < 0 ) ? -rx : rx );
			sb.append( ( ry < 0 ) ? "_m" : "_" );
			sb.append( ( ry < 0 ) ? -ry : ry );
			sb.append( ( rz < 0 ) ? "_m" : "_" );
			sb.append( ( rz < 0 ) ? -rz : rz );
		}

		return sb.toString();
	}

	/**
	 * Gets the name of a texture with specified texture code.
	 *
	 * @param code Unique code of the texture.
	 *
	 * @return Name of the texture for the specified code.
	 */
	public static String getTextureCode( final String code )
	{
		final String fixed = code.replaceAll( "[/\\-:]", "_" );
		return !code.startsWith( "TEX_" ) ? "TEX_" + fixed : fixed;
	}

	/**
	 * Gets the name of a pigment with specified pigment code.
	 *
	 * @param code Unique code of the pigment.
	 *
	 * @return Name of the pigment for the specified code.
	 */
	public static String getPigmentCode( final String code )
	{
		final String fixed = code.replaceAll( "[/\\-:]", "_" );
		return !code.startsWith( "PIG_" ) ? "PIG_" + fixed : fixed;
	}

	/**
	 * Get name of texture (for declaration).
	 *
	 * @return Name of texture.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Get ambient reflectivity color.
	 *
	 * @return Ambient reflectivity color; <code>null</code> if absent.
	 */
	public final PovVector getAmbient()
	{
		return _ambient;
	}

	/**
	 * Set ambient reflectivity color.
	 *
	 * @param ambient Ambient reflectivity factor (<code>null</code> if absent).
	 */
	public final void setAmbient( final PovVector ambient )
	{
		_ambient = ambient;
	}

	/**
	 * Set ambient reflectivity factor.
	 *
	 * @param ambient Ambient reflectivity factor.
	 */
	public final void setAmbient( final double ambient )
	{
		setAmbient( ( ambient <= 0.0 ) ? null : new PovVector( ambient, ambient, ambient ) );
	}

	/**
	 * Get diffuse reflectivity factor.
	 *
	 * @return Diffuse reflectivity factor.
	 */
	public final double getDiffuse()
	{
		return _diffuse;
	}

	/**
	 * Set diffuse reflectivity factor.
	 *
	 * @param diffuse Diffuse reflectivity factor.
	 */
	public final void setDiffuse( final double diffuse )
	{
		_diffuse = diffuse;
	}

	/**
	 * Get reflectivity factor of specular highlight.
	 *
	 * @return Specular highlight reflectivity factor.
	 */
	public final double getSpecular()
	{
		return _specular;
	}

	/**
	 * Set reflectivity factor of specular highlight.
	 *
	 * <p> This value is used to create highlights only. For mirror-like specular
	 * reflection, use {@link #setReflection} instead.
	 *
	 * @param specular Specular highlight reflectivity factor.
	 */
	public final void setSpecular( final double specular )
	{
		_specular = specular;
	}

	/**
	 * Returns the specular reflection factor at an angle parallel to the surface
	 * normal.
	 *
	 * @return Reflection factor.
	 */
	public PovVector getReflectionMin()
	{
		return _reflectionMin;
	}

	/**
	 * Returns the specular reflection factor at angles perpendicular to the
	 * surface normal.
	 *
	 * @return Reflection factor.
	 */
	public PovVector getReflectionMax()
	{
		return _reflectionMax;
	}

	/**
	 * Set specular reflection factors.
	 *
	 * <p> This value is used to produce mirror-like reflections. For specular
	 * highlights, use {@link #setSpecular} instead.
	 *
	 * @param reflectionMin Reflection factor parallel to surface normal.
	 * @param reflectionMax Reflection factor perpendicular to surface normal.
	 */
	public final void setReflection( final PovVector reflectionMin, final PovVector reflectionMax )
	{
		if ( ( reflectionMin == null ) != ( reflectionMax == null ) )
		{
			throw new NullPointerException( "arguments must either both or neither be null" );
		}

		_reflectionMin = reflectionMin;
		_reflectionMax = reflectionMax;
	}

	/**
	 * Set specular reflection factor.
	 *
	 * <p> This value is used to produce mirror-like reflections. For specular
	 * highlights, use {@link #setSpecular} instead.
	 *
	 * @param reflection Reflection factor.
	 */
	public final void setReflection( final double reflection )
	{
		if ( reflection == 0.0 )
		{
			setReflection( null, null );
		}
		else
		{
			final PovVector vector = new PovVector( reflection, reflection, reflection );
			setReflection( vector, vector );
		}
	}

	/**
	 * Get metallic texture flag.
	 *
	 * @return Metallic texture flag.
	 */
	public final boolean isMetallic()
	{
		return _metallic;
	}

	/**
	 * Set metallic texture flag.
	 *
	 * @param metallic Metallic texture flag.
	 */
	public final void setMetallic( final boolean metallic )
	{
		_metallic = metallic;
	}

	/**
	 * Returns the amount of filtered transparency of a substance.
	 *
	 * @return Amount of filtered transparency.
	 */
	public final double getFilter()
	{
		return _filter;
	}

	/**
	 * Sets the amount of filtered transparency of a substance. Some real-world
	 * examples of filtered transparency are stained glass windows or tinted
	 * cellophane. The light passing through such objects is tinted by the
	 * appropriate color as the material selectively absorbs some frequencies of
	 * light while allowing others to pass through. The color of the object is
	 * subtracted from the light passing through so this is called subtractive
	 * transparency.
	 *
	 * @param filter Filtered transparency, typically between 0.0 (opaque) and 1.0
	 *               (transparent).
	 */
	public final void setFilter( final double filter )
	{
		_filter = filter;
	}

	/**
	 * Returns the amount of non-filtered light that is transmitted through a
	 * surface.
	 *
	 * @return Amount of non-filtered light that is transmitted through a surface.
	 */
	public double getTransmit()
	{
		return _transmit;
	}

	/**
	 * Sets the amount of non-filtered light that is transmitted through a surface.
	 * Some real-world examples of non-filtered transparency are thin see-through
	 * cloth, fine mesh netting and dust on a surface. In these examples, all
	 * frequencies of light are allowed to pass through tiny holes in the surface.
	 * Although the amount of light passing through is diminished, the color of the
	 * light passing through is unchanged.
	 *
	 * @param transmit Amount of non-filtered light, typically between 0.0 (opaque)
	 *                 and 1.0 (transparent).
	 */
	public void setTransmit( final double transmit )
	{
		_transmit = transmit;
	}

	/**
	 * Get phong highlight amount.
	 *
	 * @return Phong highlight amount.
	 */
	public final double getPhong()
	{
		return _phong;
	}

	/**
	 * Set phong highlight amount.
	 *
	 * @param phong Phong highlight amount.
	 */
	public final void setPhong( final double phong )
	{
		_phong = phong;
	}

	/**
	 * Get phong highlight spot size.
	 *
	 * @return Phong highlight spot size.
	 */
	public final double getPhongSize()
	{
		return _phongSize;
	}

	/**
	 * Set phong highlight spot size.
	 *
	 * @param phongSize Phong highlight spot size.
	 */
	public final void setPhongSize( final double phongSize )
	{
		_phongSize = phongSize;
	}

	/**
	 * Get scaling factor for texture image maps.
	 *
	 * @return Scaling factor for texture image maps.
	 */
	public final PovVector getScale()
	{
		return _scale;
	}

	/**
	 * Set scaling factor for texture image maps.
	 *
	 * @param scale Scaling factor for texture image maps.
	 */
	public final void setScale( final PovVector scale )
	{
		_scale = scale;
	}

	/**
	 * Check if this texture is white according to its RGB values.
	 *
	 * @return <code>true</code> if this texture has RGB values and those values
	 *         are set to white (red, green, and blue set to 1.0);
	 *         <code>false</code> otherwise.
	 */
	public final boolean isWhite()
	{
		final PovVector rgb = _rgb;
		return ( ( rgb != null ) && ( rgb.getX() == 1.0 ) && ( rgb.getY() == 1.0 ) && ( rgb.getZ() == 1.0 ) );
	}

	/**
	 * Get file extension of image.
	 *
	 * @return File extension of image; <code>null</code> if image is undefined or
	 *         has no extension.
	 */
	private String getImageType()
	{
		String result = null;

		final URL image = _image;
		if ( image != null )
		{
			final String path = image.getPath();
			final String filename = path.substring( path.lastIndexOf( '/' ) + 1 );

			final int dot = filename.lastIndexOf( '.' );
			if ( dot >= 0 )
			{
				result = filename.substring( dot + 1 );
				if ( "jpg".equals( result ) )
				{
					result = "jpeg";
				}
			}
		}

		return result;
	}

	/**
	 * Checks if this texture is a texture map.
	 *
	 * @return true if this is a texturemap.
	 */
	public final boolean hasImageMap()
	{
		return ( _image != null );
	}

	/**
	 * Checks if this texture is already declared. If it is declared, it should not
	 * be added completely when printing, only the reference to the declaration
	 * should be printed.
	 *
	 * @return true if declared.
	 */
	public boolean isDeclared()
	{
		return _declared;
	}

	/**
	 * Method to set this texture as declared.
	 */
	public final void setDeclared()
	{
		_declared = true;
	}

	/**
	 * When the scene is writing to a scene, it can call this method to have the
	 * texture be declared, so it only has to be specified once. When actually
	 * using this texture, only the reference code has to be printed. This not only
	 * saves filesize, it also improves readability.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	public void declare( final PovWriter out )
	throws IOException
	{
		out.write( "#declare " );
		out.write( getTextureCode( _name ) );
		out.write( " =" );
		out.newLine();
		out.indentIn();
		_declared = true;
		writeTexture( out );
		out.indentOut();
	}

	/**
	 * When the scene is writing to a scene, it can call this method to have the
	 * pigments  be declared, so it only has to be specified once. When actually
	 * using the pigments, only the reference code has to be printed. This not only
	 * saves filesize, it also improves readability.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	public void declarePigments( final PovWriter out )
	throws IOException
	{
		out.write( "#declare " );
		out.write( getPigmentCode( _name ) + "_RGB" );
		out.write( " =" );
		out.newLine();
		out.indentIn();
		_declared = true;
		writeColorPigment( out );
		out.indentOut();
		out.write( "#declare " );
		out.write( getPigmentCode( _name ) + "_IMG" );
		out.write( " =" );
		out.newLine();
		out.indentIn();
		_declared = true;
		writeTexturePigment( out );
		out.indentOut();
	}

	/**
	 * When the scene is writing to a scene, it can call this method to have the
	 * pigment map be declared, so it only has to be specified once. When actually
	 * using this pigment map, only the reference code has to be printed. This not
	 * only saves filesize, it also improves readability.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	public void declarePigmentMap( final PovWriter out )
	throws IOException
	{
		out.write( "#declare " );
		out.write( getTextureCode( _name ) );
		out.write( " =" );
		out.newLine();
		out.indentIn();
		_declared = true;
		writePigmentMap( out );
		out.indentOut();
	}

	@Override
	public void write( final PovWriter out )
	throws IOException
	{
		if ( isDeclared() )
		{
			out.write( "texture { " );
			out.write( getTextureCode( _name ) );
			out.write( " }" );
			out.newLine();
		}
		else if ( _free != null )
		{
			out.writeln( _free );
		}
		else
		{
			writeTexture( out );
		}
	}

	/**
	 * This helper method writes the actual texture including all its properties to
	 * the specified destination.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	private void writeTexture( final PovWriter out )
	throws IOException
	{
		out.writeln( "texture" );
		out.writeln( "{" );
		out.indentIn();

		out.writeln( "pigment" );
		out.writeln( "{" );
		out.indentIn();

		final PovVector rgb = _rgb;

		final URL image = _image;
		if ( image != null )
		{
			if ( !"file".equals( image.getProtocol() ) )
			{
				throw new IOException( "POV-Ray can only access files, not URLs with protocol " + image.getProtocol() );
			}

			final String imageType = getImageType();

			final URI imageURI;
			try
			{
				imageURI = image.toURI();
			}
			catch ( URISyntaxException e )
			{
				throw new IOException( "Unable to convert URL to URI: " + e, e );
			}

			final File imageFile = new File( imageURI );

			out.write( "image_map" );
			out.newLine();
			out.write( "{" );
			out.newLine();
			out.indentIn();

			if ( imageType != null )
			{
				out.write( imageType );
				out.write( ' ' );
			}
			out.write( '"' );
			out.write( imageFile.getPath() );
			out.write( '"' );
			out.newLine();

			final double filter = getFilter();
			final double transmit = getTransmit();

			if ( filter != 0.0 )
			{
				out.write( "filter all " );
				out.write( format( filter ) );
				out.newLine();
			}

			if ( transmit != 0.0 )
			{
				out.write( "transmit all " );
				out.write( format( transmit ) );
				out.newLine();
			}

			out.indentOut();
			out.write( "}" );
			out.newLine();
		}
		else
		{
			out.write( "color      rgb " );
			rgb.write( out );
			out.newLine();

			final double filter = getFilter();
			if ( filter != 0.0 )
			{
				out.write( "filter     " );
				out.write( format( filter ) );
				out.newLine();
			}

			final double transmit = getTransmit();
			if ( transmit != 0.0 )
			{
				out.write( "transmit   " );
				out.write( format( transmit ) );
				out.newLine();
			}
		}

		out.indentOut();
		out.writeln( "}" );

		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();

		final PovVector ambient = getAmbient();
		if ( ambient != null )
		{
			out.write( "ambient    " );
			if ( ( ambient.getX() != ambient.getY() ) || ( ambient.getY() != ambient.getZ() ) )
			{
				out.write( "rgb " );
				ambient.write( out );
			}
			else
			{
				out.write( format( ambient.getX() ) );
			}
			out.newLine();
		}

		final double diffuse = getDiffuse();
		if ( diffuse > 0.0 )
		{
			out.write( "diffuse    " );
			out.writeln( format( diffuse ) );
		}

		if ( hasImageMap() && !isWhite() )
		{
			/*
			 * Add another texture layer to adjust the texture color.
			 */
			out.indentOut();
			out.writeln( "}" );

			final PovVector scale = getScale();
			if ( scale != null )
			{
				out.write( "scale " );
				scale.write( out );
				out.newLine();
			}

			out.indentOut();
			out.writeln( "}" );

			out.writeln( "texture" );
			out.writeln( "{" );
			out.indentIn();

			out.writeln( "pigment" );
			out.writeln( "{" );
			out.indentIn();

			out.write( "color      rgb " );
			rgb.write( out );
			out.write( " filter 1.0" );
			out.newLine();

			out.indentOut();
			out.writeln( "}" );

			out.writeln( "finish" );
			out.writeln( "{" );
			out.indentIn();
		}

		final double phong = getPhong();
		if ( phong > 0.0 )
		{
			out.write( "phong      " );
			out.writeln( format( phong ) );

			final double phongSize = getPhongSize();
			if ( phongSize > 0.0 )
			{
				out.write( "phong_size " );
				out.writeln( format( phongSize ) );
			}
		}

		final double specular = getSpecular();
		if ( specular > 0.0 )
		{
			out.write( "specular   " );
			out.writeln( format( specular ) );
		}

		final boolean metallic = isMetallic();
		if ( metallic )
		{
			out.writeln( "metallic" );
		}

		final PovVector reflectionMin = getReflectionMin();
		final PovVector reflectionMax = getReflectionMax();
		if ( ( reflectionMin != null ) && ( reflectionMax != null ) )
		{
			out.write( "reflection { " );
			reflectionMin.write( out );
			out.writeln( ", " );
			reflectionMax.write( out );
			out.write( " }" );
		}

		out.indentOut();
		out.writeln( "}" );

		final PovVector scale = getScale();
		if ( scale != null )
		{
			out.write( "scale " );
			scale.write( out );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * This helper method writes the actual pigment map including all its
	 * properties to the specified destination.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	private void writePigmentMap( final PovWriter out )
	throws IOException
	{
		out.writeln( "texture" );
		out.writeln( "{" );
		out.indentIn();

		out.writeln( "pigment" );
		out.writeln( "{" );
		out.indentIn();

		out.writeln( "average" );
		out.writeln( "pigment_map" );
		out.writeln( "{" );
		out.indentIn();

		final String pigmentCode = getPigmentCode( _name );
		out.writeln( "[ 1.0 " + pigmentCode + "_RGB ]" );
		out.writeln( "[ 1.0 " + pigmentCode + "_IMG ]" );

		out.indentOut();
		out.writeln( "}" );

		out.indentOut();
		out.writeln( "}" );

		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();

		final PovVector ambient = getAmbient();
		if ( ambient != null )
		{
			out.write( "ambient    " );
			if ( ( ambient.getX() != ambient.getY() ) || ( ambient.getY() != ambient.getZ() ) )
			{
				out.write( "rgb " );
				ambient.write( out );
			}
			else
			{
				out.write( format( ambient.getX() ) );
			}
			out.newLine();
		}

		final double diffuse = getDiffuse();
		if ( diffuse > 0.0 )
		{
			out.write( "diffuse    " );
			out.writeln( format( diffuse ) );
		}

		final double phong = getPhong();
		if ( phong > 0.0 )
		{
			out.write( "phong      " );
			out.writeln( format( phong ) );
		}

		final double phongSize = getPhongSize();
		if ( phongSize > 0.0 )
		{
			out.write( "phong_size " );
			out.writeln( format( phongSize ) );
		}

		final double specular = getSpecular();
		if ( specular > 0.0 )
		{
			out.write( "specular   " );
			out.writeln( format( specular ) );
		}

		final boolean metallic = isMetallic();
		if ( metallic )
		{
			out.writeln( "metallic" );
		}

		final PovVector reflectionMin = getReflectionMin();
		final PovVector reflectionMax = getReflectionMax();
		if ( ( reflectionMin != null ) && ( reflectionMax != null ) )
		{
			out.write( "reflection { " );
			reflectionMin.write( out );
			out.writeln( ", " );
			reflectionMax.write( out );
			out.write( " }" );
		}

		out.indentOut();
		out.writeln( "}" );

		final PovVector scale = getScale();
		if ( scale != null )
		{
			out.write( "scale " );
			scale.write( out );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );
	}


	/**
	 * This helper method writes the actual texture including all its properties to
	 * the specified destination.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	private void writeTexturePigment( final PovWriter out )
	throws IOException
	{
		out.writeln( "pigment" );
		out.writeln( "{" );
		out.indentIn();

		final URL image = _image;
		if ( image != null )
		{
			if ( !"file".equals( image.getProtocol() ) )
			{
				throw new IOException( "POV-Ray can only access files, not URLs with protocol " + image.getProtocol() );
			}

			final String imageType = getImageType();

			out.write( "image_map  { " );
			if ( imageType != null )
			{
				out.write( imageType );
				out.write( ' ' );
			}
			out.write( '"' );
			out.write( image.getPath() );
			out.write( "\" }" );
			out.newLine();

			final double filter = getFilter();
			if ( filter != 0.0 )
			{
				out.write( "filter all " );
				out.write( format( filter ) );
				out.newLine();
			}

			final double transmit = getTransmit();
			if ( transmit != 0.0 )
			{
				out.write( "transmit all " );
				out.write( format( transmit ) );
				out.newLine();
			}
		}

		out.indentOut();
		out.writeln( "}" );
		out.newLine();
	}

	/**
	 * This helper method writes the actual texture including all its properties to
	 * the specified destination.
	 *
	 * @param out IndentingWriter to use for writing.
	 *
	 * @throws IOException when writing failed.
	 */
	private void writeColorPigment( final PovWriter out )
	throws IOException
	{
		out.writeln( "pigment" );
		out.writeln( "{" );
		out.indentIn();

		final PovVector rgb = _rgb;
		if ( rgb != null )
		{
			out.write( "color rgb " );
			rgb.write( out );
			out.newLine();
		}

		final double filter = getFilter();
		if ( filter != 0.0 )
		{
			out.write( "filter     " );
			out.write( format( filter ) );
			out.newLine();
		}

		final double transmit = getTransmit();
		if ( transmit != 0.0 )
		{
			out.write( "transmit   " );
			out.write( format( transmit ) );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );
		out.newLine();
	}

	@Override
	public int hashCode()
	{
		final int result;

		String name = _name;
		if ( name != null )
		{
			name = name.toUpperCase();
			result = name.hashCode();
		}
		else
		{
			result = 0;
		}

		return result;
	}

	@Override
	public final boolean equals( final Object other )
	{
		final boolean equals;

		if ( other instanceof PovTexture )
		{
			final PovTexture texture = (PovTexture)other;
			equals = _name.equalsIgnoreCase( texture._name );
		}
		else
		{
			equals = false;
		}

		return equals;
	}
}
