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

import java.awt.Color;
import java.io.IOException;

import ab.j3d.Material;

import com.numdata.oss.io.IndentingWriter;

/**
 * Pov Texture / material definition.
 *
 * <p>Definition of texture (RGB):</p>
 * <pre>
 * texture
 * {
 *     pigment { color rgb &lt; r , g , b &gt; }
 *     finish
 *     {
 *        [ambient    rgb &lt; ar , ag , ab &gt]
 *        [diffuse    d]
 *        [phong      p]
 *        [phong_size ps]
 *        [specular   s]
 *        [metallic]
 *        [reflection r]
 *     }
 *     [scale &lt; x , y , z&gt;]
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
 *        [ambient    rgb &lt; ar , ag , ab &gt]
 *        [diffuse    d]
 *        [phong      p]
 *        [phong_size ps]
 *        [specular   s]
 *        [metallic]
 *        [reflection r]
 *     }
 *     [scale &lt; x , y , z&gt;]
 * }
 * </pre>
 *
 * <p>(Rotated) reference to predefined texture (SubTexture):</p>
 * <pre>
 * texture
 * {
 *     referenceName [rotate &lt; x , y , z &gt; ]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
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
	 * A free material
	 */
	private String _free;

	/**
	 * Indicates the texture has been declared,
	 * so when used we only have to specify reference name.
	 */
	private boolean _declared;

	/**
	 * In case of RGB texture, the rgb value.
	 */
	private PovVector _rgb;

	/**
	 * In case of image map, the name of the image file.
	 */
	private String _imageMap;

	/**
	 * Type of image map (e.g. 'jpeg').
	 */
	private String _imageMapType;

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
	 * Reflection factor.
	 */
	private double _reflection;

	/**
	 * Metallic texture flag.
	 */
	private boolean _metallic;

	/**
	 * Filter factor (transparency).
	 */
	private double _filter;

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
	 * Modified reference to a Texture.
	 * Currently only support rotation.
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
		 */
		public Reference( final PovTexture reference , final PovVector rotation )
		{
			super( getReferenceCode( reference._name , rotation ) );

			_rotation  = rotation;
			_reference = reference;
		}

		public void write( final IndentingWriter out )
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

		public final boolean isDeclared()
		{
			return ( ( _rotation == null ) ? _reference.isDeclared() : super.isDeclared() );
		}

		public final void declare( final IndentingWriter out )
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
	 * Creates a texture map using the specified map.
	 *
	 * @param   name    Name of the texture.
	 * @param   rgb     Color of the texture.
	 * @param   imageMap     Path to map image to use.
	 */
	private PovTexture( final String name , final PovVector rgb , final String free , final String imageMap )
	{
		_name         = name;
		_free         = free;
		_declared     = false;
		_rgb          = rgb;
		_imageMap     = imageMap;
		_imageMapType = "jpeg";
		_phong        = 0.0;
		_phongSize    = 0.0;
		_ambient      = null;
		_diffuse      = 0.0;
		_specular     = 0.0;
		_reflection   = 0.0;
		_filter       = 0.0;
		_scale        = null;
		_metallic     = false;
	}

	/**
	 * Creates a texture only based on a name.
	 * This constructor may only be used by descendants of
	 * this class. It is currently used to create a texture reference.
	 *
	 * @param   name    Name of the texture.
	 */
	protected PovTexture( final String name )
	{
		this( name , null , null , null );
	}

	/**
	 * Creates a texture based on red, green and blue values.
	 *
	 * @param   name    Name of the texture.
	 * @param   r       Red value of the color.
	 * @param   g       Green value of the color.
	 * @param   b       Blue value of the color.
	 */
	public PovTexture( final String name , final double r , final double g , final double b )
	{
		this( name , new PovVector( r , g , b ) , null , null );
	}

	/**
	 * Creates a texture based on java.awt.Color.
	 *
	 * @param   name    Name of the texture.
	 * @param   rgb     Color of the texture.
	 */
	public PovTexture( final String name , final Color rgb )
	{
		this( name , new PovVector( rgb ) , null , null );
	}

	/**
	 * Creates a texture map using the specified map.
	 *
	 * @param   name                Name of the texture.
	 * @param   textureDirectory    Directory containing POV-textures.
	 * @param   imageMap                 Filename of map to use.
	 */
	public PovTexture( final String name, final String textureDirectory , final String imageMap )
	{
		this( name , null , null , ( ( imageMap != null ) && ( textureDirectory != null ) ) ? textureDirectory + imageMap
		                           : imageMap );
	}

	/**
	 * Creates a texture that can be specified as you would specify a texture in
	 * pov-ray yourself. Nothing will be generated, only the string specified as
	 * free will be printed. Use this constructor with care, since you can
	 * introduce errors in the scene.
	 *
	 * @param   name    Name of the texture.
	 * @param   free    Definition of the texture.
	 */
	public PovTexture( final String name , final String free )
	{
		this( name , null , free , null );
	}

	/**
	 * Create a new {@link PovTexture} based on the given {@link Material}
	 * object.
	 *
	 * @param   textureDirectory    Directory containing POV-textures.
	 * @param   material            {@link Material} object to be used to
	 *                              construct the {@link PovTexture}.
	 */
	public PovTexture( final String textureDirectory , final Material material )
	{
		this( getNameForMaterial( material ) ,
		      ( material.colorMap == null ) ? new PovVector( new Color( material.getARGB() ) ) : null , null ,
		      ( material.colorMap != null ) ? ( textureDirectory != null ) ? textureDirectory + '/' + material.colorMap : material.colorMap : null );

		setAmbient( new PovVector( (double)material.ambientColorRed, (double)material.ambientColorGreen, (double)material.ambientColorBlue ) );
		setDiffuse( material.getDiffuseReflectivity() );
		setFilter( 1.0 - (double)material.diffuseColorAlpha );
		setPhong( material.getSpecularReflectivity() );
		setPhongSize( (double)material.shininess );
	}

	/**
	 * Get name to give to a texture for the specified material.
	 *
	 * @param   material    Material to get name for.
	 *
	 * @return  Name for material.
	 */
	public static String getNameForMaterial( final Material material )
	{
		final String result;

		if ( material.code != null )
		{
			result = material.code;
		}
		else if ( material.colorMap != null )
		{
			result = material.colorMap;
		}
		else
		{
			final int argb  = material.getARGB();
			final int red   = ( ( argb >> 16 ) & 0xFF );
			final int green = ( ( argb >>  8 ) & 0xFF );
			final int blue  = ( argb & 0xFF );

			result = "RGB_" + red + '_' + green + '_' + blue;
		}

		return result;
	}

	/**
	 * Returns the name of the texture with specified rotation if it is
	 * referenced.
	 *
	 * @param   parentCode  Name of the texture to reference.
	 * @param   rotation    Rotation of the texture when referencing.
	 *
	 * @return  Name of texture with specified rotation if it is referenced.
	 */
	public static String getReferenceCode( final String parentCode , final PovVector rotation )
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
	 * @param   code    Unique code of the texture.
	 *
	 * @return  Name of the texture for the specified code.
	 */
	public static String getTextureCode( final String code )
	{
		return !code.startsWith( "TEX_" ) ? "TEX_" + code : code;
	}

	/**
	 * Get name of texture (for declaration).
	 *
	 * @return  Name of texture.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Get ambient reflectivity color.
	 *
	 * @return  Ambient reflectivity color;
	 *          <code>null</code> if absent.
	 */
	public final PovVector getAmbient()
	{
		return _ambient;
	}

	/**
	 * Set ambient reflectivity color.
	 *
	 * @param   ambient     Ambient reflectivity factor (<code>null</code> if
	 *                      absent).
	 */
	public final void setAmbient( final PovVector ambient )
	{
		_ambient = ambient;
	}

	/**
	 * Set ambient reflectivity factor.
	 *
	 * @param   ambient     Ambient reflectivity factor.
	 */
	public final void setAmbient( final double ambient )
	{
		setAmbient( ( ambient <= 0.0 ) ? null : new PovVector( ambient , ambient , ambient ) );
	}

	/**
	 * Get diffuse reflectivity factor.
	 *
	 * @return  Diffuse reflectivity factor.
	 */
	public final double getDiffuse()
	{
		return _diffuse;
	}

	/**
	 * Set diffuse reflectivity factor.
	 *
	 * @param   diffuse     Diffuse reflectivity factor.
	 */
	public final void setDiffuse( final double diffuse )
	{
		_diffuse = diffuse;
	}

	/**
	 * Get specular reflectivity factor.
	 *
	 * @return  Specular reflectivity factor.
	 */
	public final double getSpecular()
	{
		return _specular;
	}

	/**
	 * Set specular reflectivity factor.
	 *
	 * @param   specular    Specular reflectivity factor.
	 */
	public final void setSpecular( final double specular )
	{
		_specular = specular;
	}

	/**
	 * Get reflection factor.
	 *
	 * @return  Reflection factor.
	 */
	public final double getReflection()
	{
		return _reflection;
	}

	/**
	 * Set reflection factor.
	 *
	 * @param   reflection  Reflection factor.
	 */
	public final void setReflection( final double reflection )
	{
		_reflection = reflection;
	}

	/**
	 * Get metallic texture flag.
	 *
	 * @return  Metallic texture flag.
	 */
	public final boolean isMetallic()
	{
		return _metallic;
	}

	/**
	 * Set metallic texture flag.
	 *
	 * @param   metallic    Metallic texture flag.
	 */
	public final void setMetallic( final boolean metallic )
	{
		_metallic = metallic;
	}

	/**
	 * Get filter factor (transparency).
	 *
	 * @return  Filter factor (transparency).
	 */
	public final double getFilter()
	{
		return _filter;
	}

	/**
	 * Set filter factor (transparency).
	 *
	 * @param   filter  Filter factor (transparency).
	 */
	public final void setFilter( final double filter )
	{
		_filter = filter;
	}

	/**
	 * Get phong highlight amount.
	 *
	 * @return  Phong highlight amount.
	 */
	public final double getPhong()
	{
		return _phong;
	}

	/**
	 * Set phong highlight amount.
	 *
	 * @param   phong   Phong highlight amount.
	 */
	public final void setPhong( final double phong )
	{
		_phong = phong;
	}

	/**
	 * Get phong highlight spot size.
	 *
	 * @return  Phong highlight spot size.
	 */
	public final double getPhongSize()
	{
		return _phongSize;
	}

	/**
	 * Set phong highlight spot size.
	 *
	 * @param   phongSize   Phong highlight spot size.
	 */
	public final void setPhongSize( final double phongSize )
	{
		_phongSize = phongSize;
	}

	/**
	 * Get scaling factor for texture image maps.
	 *
	 * @return  Scaling factor for texture image maps.
	 */
	public final PovVector getScale()
	{
		return _scale;
	}

	/**
	 * Set scaling factor for texture image maps.
	 *
	 * @param   scale   Scaling factor for texture image maps.
	 */
	public final void setScale( final PovVector scale )
	{
		_scale = scale;
	}

	/**
	 * Checks if this texture is a texture map.
	 *
	 * @return  true if this is a texturemap.
	 */
	public final boolean hasImageMap()
	{
		return ( _imageMap != null );
	}

	/**
	 * Checks if this texture is already declared.
	 * If it is declared, it should not be added completely
	 * when printing, only the reference to the declaration
	 * should be printed.
	 *
	 * @return  true if declared.
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
	 * When the scene is writing to a scene, it can call
	 * this method to have the texture be declared, so it
	 * only has to be specified once. When actually using
	 * this texture, only the reference code has to be printed.
	 * This not only saves filesize, it also improves readability.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void declare( final IndentingWriter out )
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

	public void write( final IndentingWriter out )
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
	 * This helper method writes the actual texture including all its
	 * properties to the specified destination.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	private void writeTexture( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "texture" );
		out.writeln( "{" );
		out.indentIn();

		out.writeln( "pigment" );
		out.writeln( "{" );
		out.indentIn();

		final PovVector rgb = _rgb;
		if ( rgb != null )
		{
			out.write( "color      rgb " );
			rgb.write( out );
			out.newLine();
		}

		final String imageMap = _imageMap;
		if ( imageMap != null )
		{
			final String imageMapType = _imageMapType;
			//String imageMapType = map.substring( map.lastIndexOf( (int)'.' ) + 1 );

			out.write( "image_map  { " );
			out.write( imageMapType );
			out.write( " \"" );
			out.write( imageMap );
			out.write( "\" }" );
			out.newLine();
		}

		final double filter = getFilter();
		if ( filter > 0.0 )
		{
			out.write( "filter     " );
			out.write( format( filter ) );
			out.newLine();
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

		final double reflection = getReflection();
		if ( reflection > 0.0 )
		{
			out.write( "reflection " );
			out.writeln( format( reflection ) );
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

	public final boolean equals( final Object other )
	{
		final boolean equals;

		if ( other instanceof PovTexture )
		{
			final PovTexture texture = (PovTexture)other;
			equals =  _name.equalsIgnoreCase( texture._name );
		}
		else
		{
			equals = false;
		}

		return equals;
	}
}
