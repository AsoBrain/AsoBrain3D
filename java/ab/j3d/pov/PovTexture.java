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

import java.awt.Color;
import java.io.IOException;

import com.numdata.oss.io.IndentingWriter;

import ab.j3d.TextureSpec;

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
 *         phong P
 *         [ambient A]
 *         [diffuse D]
 *         [specular S]
 *         [reflection R]
 *         [metallic]
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
 *         phong p
 *         [ambient a]
 *         [diffuse d]
 *         [specular s]
 *         [reflection r]
 *         [metallic]
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
	 * Path to textures.
	 *
	 * @FIXME Make adjustable.
	 */
	public static String texturePath = "Z:\\apps\\povexport\\textures\\";

	/**
	 * Name of texture (for declaration).
	 */
	public final String name;

	public String type = "jpeg";

	/**
	 * A free material
	 */
	public final String free;

	/**
	 * Indicates the texture has been declared,
	 * so when used we only have to specify reference name.
	 */
	private boolean _declared = false;

	/**
	 * In case of RGB texture, the rgb value.
	 */
	public final PovVector rgb;

	/**
	 * In case of image map, the name of the image file.
	 */
	public final String map;

	/**
	 * Phong value.
	 */
	public float phong = 0.5f;

	/**
	 * Ambient reflectivity.
	 */
	public float ambient = 0.0f;

	/**
	 * Diffuse reflectivity.
	 */
	public float diffuse = 0.0f;

	/**
	 * Specular reflectivity.
	 */
	public float specular = 0.0f;

	/**
	 * Reflection factor.
	 */
	public float reflection = 0.0f;

	/**
	 * Filter factor (transparancy).
	 */
	public float _filter = 0.0f;

	/**
	 * Scaling factor for image maps.
	 */
	public PovVector scale = null;

	/**
	 * Metallic setting.
	 */
	public boolean metallic = false;

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
		public final PovTexture reference;

		/**
		 * The rotation of the texture.
		 */
		public final PovVector rotation;

		/**
		 * Construct reference to PovTexture instance.
		 */
		public Reference( final PovTexture reference , final PovVector rotation )
		{
			super( getReferenceCode( reference.name , rotation ) );

			this.rotation = rotation;
			this.reference= reference;
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
			if ( isDeclared() )
			{
				out.write( "texture { " + name  + " }" );
				return;
			}

			if ( rotation == null )
			{
				out.write( "texture { " + reference.name + " }" );
			}
			else
			{
				out.write( "texture { " + reference.name + " rotate " + rotation + " }" );
			}
		}

		public boolean isDeclared()
		{
			if ( rotation == null )
				return reference.isDeclared();
			else
				return super.isDeclared();
		}

		public void declare( final IndentingWriter out )
			throws IOException
		{
			if ( !reference.isDeclared() )
				reference.declare( out );

			if ( rotation != null )
			{
				out.write( "#declare " + name + " = " );
				out.indentIn();
				write( out );
				out.newLine();
				out.indentOut();
			}

			reference._declared = true;
		}
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
		this.name = name;
		rgb = null;
		map = null;
		free = null;
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
		this.name = name;

		rgb  = new PovVector( r , g , b );
		map  = null;
		free = null;
	}

	/**
	 * Creates a texture based on java.awt.Color.
	 *
	 * @param   name    Name of the texture.
	 * @param   rgb     Color of the texture.
	 */
	public PovTexture( final String name , final Color rgb )
	{
		this.name = name;
		this.rgb = new PovVector( rgb );
		map = null;
		free = null;
	}

	/**
	 * Creates a texture map using the specified map.
	 *
	 * @param   name    Name of the texture.
	 * @param   map     Filename of map to use.
	 */
	public PovTexture( final String name , final String map )
	{
		this.name = name;
		rgb = null;
		this.map = texturePath + map;
		free = null;
	}

	/**
	 * Creates a texture that can be specified as you would
	 * specify a texture in pov-ray yourself. Nothing will be
	 * generated, only the string specified as free will be
	 * printed. Use this constructor with care, since you can
	 * introduce errors in the scene.
	 *
	 * @param   name    Name of the texture.
	 * @param   free    Definition of the texture.
	 * @param   a       Extra argument to differ from other String,String constructor.
	 */
	public PovTexture( final String name , final String free , final boolean a )
	{
		this.name = name;
		rgb = null;
		map = null;
		this.free = free;
	}

	/**
	 * Create a new PovTexture based on the given TextureSpec object.
	 *
	 * @param texture The TextureSpec object to be used to construct the PovTexture.
	 */
	public PovTexture( final TextureSpec texture )
	{
		if ( texture.isTexture() )
		{
			rgb = null;
			map = texturePath + texture.code;
			name = texture.code;
		} else {
			final Color color = texture.getColor();
			rgb = new PovVector( color );
			map = null;
			name = "RGB_" + color.getRed() + "_" + color.getGreen() + "_" + color.getBlue();
		}

		free = null;
		_filter = 1.0f - texture.opacity;
		ambient = texture.ambientReflectivity;
		diffuse = texture.diffuseReflectivity;
		specular = texture.specularReflectivity;
	}

	public boolean equals( final Object other )
	{
		final boolean equals;

		if ( other instanceof PovTexture )
		{
			final PovTexture texture = (PovTexture)other;
			equals =  name.equalsIgnoreCase( texture.name );
		}
		else
		{
			equals = false;
		}

		return equals;
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
			out.writeln( "#declare " + PovTexture.getTextureCode( name ) + " =" );
			out.indentIn();
			//write( out );
			//out.indentOut();
			_declared = true;
		//}


		out.writeln( "texture" );
		out.writeln( "{" );
		out.indentIn();
		if ( rgb != null )
		{
			if ( _filter > 0.0 )
			{
				out.writeln( "pigment" );
				out.writeln( "{" );
				out.indentIn();
				out.writeln( "color rgb " + rgb );
				out.writeln( "filter " + _filter );
				out.indentOut();
				out.writeln( "}" );
			}
			else
			{
				out.writeln( "pigment { color rgb "  + rgb + " }");
			}
		}
		else if ( map != null )
		{
			//String type = map.substring( map.lastIndexOf( "." ) + 1 );

			out.writeln( "pigment" );
			out.writeln( "{" );
			out.indentIn();
			out.writeln( "image_map { " + type + " \"" + map + "\" }" );
			out.indentOut();
			out.writeln( "}" );
		}

		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();

		if ( phong      > 0 ) out.writeln( "phong " + phong );
		if ( ambient    > 0 ) out.writeln( "ambient " + ambient );
		if ( diffuse    > 0 ) out.writeln( "diffuse " + diffuse );
		if ( specular   > 0 ) out.writeln( "specular " + specular );
		if ( reflection > 0 ) out.writeln( "reflection " + reflection );
		if ( metallic        ) out.writeln( "metallic" );
		out.indentOut();
		out.writeln( "}" );
		if ( scale != null ) out.writeln( "scale " + scale );
		out.indentOut();
		out.writeln( "}" );
		out.indentOut();


	}

	/**
	 * Returns the name of the texture with specified rotation if it where
	 * referenced.
	 *
	 * @param   parentCode  Name of the texture to reference.
	 * @param   rotation    Rotation of the texture when referencing.
	 */
	public static String getReferenceCode( final String parentCode , final PovVector rotation )
	{
		final StringBuffer sb = new StringBuffer();
		sb.append( parentCode );

		if ( rotation == null )
		{
			sb.append( "_REF" );
		}
		else
		{
			final int rx = (int)Math.round( rotation.v.x );
			final int ry = (int)Math.round( rotation.v.y );
			final int rz = (int)Math.round( rotation.v.z );

			sb.append( rx < 0 ? "_m" : "_" );
			sb.append( rx < 0 ? -rx : rx );
			sb.append( ry < 0 ? "_m" : "_" );
			sb.append( ry < 0 ? -ry : ry );
			sb.append( rz < 0 ? "_m" : "_" );
			sb.append( rz < 0 ? -rz : rz );
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
		if ( code.startsWith( "TEX_" ) )
			return code;
		return "TEX_" + code;
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
	 * Checks if this texture is a texture map.
	 *
	 * @return  true if this is a texturemap.
	 */
	public boolean isMap()
	{
		return map != null;
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
		if ( isDeclared() )
		{
			out.writeln( "texture { " + getTextureCode( name )  + " }" );
			return;
		}

		if ( free != null )
		{
			out.writeln( free );
			return;
		}

		out.writeln( "texture" );
		out.writeln( "{" );
		out.indentIn();
		if ( rgb != null )
		{
			if ( _filter > 0.0f )
			{
				out.writeln( "pigment" );
				out.writeln( "{" );
				out.indentIn();
				out.writeln( "color rgb " + rgb );
				out.writeln( "filter " + _filter );
				out.indentOut();
				out.writeln( "}" );
			}
			else
			{
				out.writeln( "pigment { color rgb "  + rgb + " }");
			}
		}
		else if ( map != null )
		{
			//String type = map.substring( map.lastIndexOf( "." ) + 1 );

			out.writeln( "pigment" );
			out.writeln( "{" );
			out.indentIn();
			out.writeln( "image_map { " + type + " \"" + map + "\" }" );
			out.indentOut();
			out.writeln( "}" );
		}

		out.writeln( "finish" );
		out.writeln( "{" );
		out.indentIn();

		if ( phong      > 0 ) out.writeln( "phong " + phong );
		if ( ambient    > 0 ) out.writeln( "ambient " + ambient );
		if ( diffuse    > 0 ) out.writeln( "diffuse " + diffuse );
		if ( specular   > 0 ) out.writeln( "specular " + specular );
		if ( reflection > 0 ) out.writeln( "reflection " + reflection );
		if ( metallic        ) out.writeln( "metallic" );
		out.indentOut();
		out.writeln( "}" );
		if ( scale != null ) out.writeln( "scale " + scale );
		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Method to set this texture as declared.
	 */
	public void setDeclared()
	{
		_declared = true;
	}
}
