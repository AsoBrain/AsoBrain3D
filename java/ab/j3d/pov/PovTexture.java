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
import com.numdata.oss.TextTools;

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
 *         phong      P
 *        [ambient    A]
 *        [diffuse    D]
 *        [specular   S]
 *        [reflection R]
 *        [metallic]
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
 *         phong      p
 *        [ambient    a]
 *        [diffuse    d]
 *        [specular   s]
 *        [reflection r]
 *        [metallic]
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

	private String _type;

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
	private String _map;

	/**
	 * Ambient reflectivity factor.
	 */
	private double _ambient;

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
	 * Phong shading factor.
	 */
	private double _phong;

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
		public final PovTexture _reference;

		/**
		 * The rotation of the texture.
		 */
		public final PovVector _rotation;

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
	 * @param   map     Path to map image to use.
	 */
	private PovTexture( final String name, final PovVector rgb, final String free, final String map )
	{
		_name       = name;
		_type       = "jpeg";
		_free       = free;
		_declared   = false;
		_rgb        = rgb;
		_map        = map;
		_phong      = 0.5;
		_ambient    = 0.0;
		_diffuse    = 0.0;
		_specular   = 0.0;
		_reflection = 0.0;
		_filter     = 0.0;
		_scale      = null;
		_metallic   = false;
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
	 * @param   map                 Filename of map to use.
	 */
	public PovTexture( final String name, final String textureDirectory , final String map )
	{
		this( name , null , null , ( ( map != null ) && ( textureDirectory != null ) ) ? textureDirectory + map : map );
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
		      ( material.colorMap != null ) ? ( textureDirectory != null ) ? textureDirectory + material.colorMap : material.colorMap : null );

		_filter   = 1.0 - (double)material.diffuseColorAlpha;
		_ambient  = material.getAmbientReflectivity();
		_diffuse  = material.getDiffuseReflectivity();
		_specular = material.getSpecularReflectivity();
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
			result = "RGB_" + TextTools.toHexString( material.getARGB() , 6 , true );
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
	 * Get ambient reflectivity factor.
	 *
	 * @return  Ambient reflectivity factor.
	 */
	public final double getAmbient()
	{
		return _ambient;
	}

	/**
	 * Set ambient reflectivity factor.
	 *
	 * @param   ambient     Ambient reflectivity factor.
	 */
	public final void setAmbient( final double ambient )
	{
		_ambient = ambient;
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
	 * Get phong shading factor.
	 *
	 * @return  Phong shading factor.
	 */
	public final double getPhong()
	{
		return _phong;
	}

	/**
	 * Set phong shading factor.
	 *
	 * @param   phong   Phong shading factor.
	 */
	public final void setPhong( final double phong )
	{
		_phong = phong;
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
	public final boolean isMap()
	{
		return ( _map != null );
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
		//if ( !_declared )
		//{
			out.write( "#declare " );
			out.write( getTextureCode( _name ) );
			out.write( " =" );
			out.newLine();
			out.indentIn();

			//write( out );

			//out.indentOut();
			_declared = true;
		//}


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

		if ( _map != null )
		{
			//String type = map.substring( map.lastIndexOf( (int)'.' ) + 1 );

			out.write( "image_map  { " );
			out.write( _type );
			out.write( " \"" );
			out.write( _map );
			out.write( "\" }" );
			out.newLine();
		}

		if ( _filter > 0.0 )
		{
			out.write( "filter     " );
			out.write( format( _filter ) );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );

		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();

		if ( _phong      > 0.0 ) { out.write( "phong      " ); out.writeln( format( _phong      ) ); }
		if ( _ambient    > 0.0 ) { out.write( "ambient    " ); out.writeln( format( _ambient    ) ); }
		if ( _diffuse    > 0.0 ) { out.write( "diffuse    " ); out.writeln( format( _diffuse    ) ); }
		if ( _specular   > 0.0 ) { out.write( "specular   " ); out.writeln( format( _specular   ) ); }
		if ( _reflection > 0.0 ) { out.write( "reflection " ); out.writeln( format( _reflection ) ); }
		if ( _metallic         ) out.writeln( "metallic" );

		out.indentOut();
		out.writeln( "}" );

		if ( _scale != null )
		{
			out.write( "scale " );
			_scale.write( out );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );
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
			out.writeln( "texture" );
			out.writeln( "{" );
			out.indentIn();

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

			if ( _map != null )
			{
				//String type = map.substring( map.lastIndexOf( (int)'.' ) + 1 );

				out.write( "image_map { " );
				out.write( _type );
				out.write( " \"" );
				out.write( _map );
				out.write( "\" }" );
				out.newLine();
			}

			if ( _filter > 0.0 )
			{
				out.write( "filter " );
				out.write( format( _filter ) );
				out.newLine();
			}

			out.indentOut();
			out.writeln( "}" );

			out.writeln( "finish" );
			out.writeln( "{" );
			out.indentIn();

			if ( _phong      > 0.0 ) { out.write( "phong "      ); out.writeln( format( _phong      ) ); }
			if ( _ambient    > 0.0 ) { out.write( "ambient "    ); out.writeln( format( _ambient    ) ); }
			if ( _diffuse    > 0.0 ) { out.write( "diffuse "    ); out.writeln( format( _diffuse    ) ); }
			if ( _specular   > 0.0 ) { out.write( "specular "   ); out.writeln( format( _specular   ) ); }
			if ( _reflection > 0.0 ) { out.write( "reflection " ); out.writeln( format( _reflection ) ); }
			if ( _metallic         ) out.writeln( "metallic"    );

			out.indentOut();
			out.writeln( "}" );

			final PovVector scale = _scale;
			if ( scale != null )
			{
				out.write( "scale " );
				scale.write( out );
				out.newLine();
			}

			out.indentOut();
			out.writeln( "}" );
		}
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
