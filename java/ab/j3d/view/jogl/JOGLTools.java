/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.view.jogl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;
import com.numdata.oss.TextTools;
import com.numdata.oss.ui.ImageTools;

/**
 * Utility methods related to JOGL implementation of view model.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLTools
{
	/**
	 * Texture cache key for the normalization cube map, used for DOT3 bump
	 * mapping.
	 */
	public static final String NORMALIZATION_CUBE_MAP = "__normalizationCubeMap";

	/**
	 * Maximum number of lights possible. Standard value is 8 because all
	 * OpenGL implementations have atleast this number of lights.
	 */
	public static final int MAX_LIGHTS = 8;

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JOGLTools()
	{
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param   gl          GL context.
	 * @param   transform   Transformation to multiply with.
	 */
	public static void glMultMatrixd( final GL gl , final Matrix3D transform )
	{
		gl.glMultMatrixd( new double[]
			{
				transform.xx , transform.yx , transform.zx , 0.0 ,
				transform.xy , transform.yy , transform.zy , 0.0 ,
				transform.xz , transform.yz , transform.zz , 0.0 ,
				transform.xo , transform.yo , transform.zo , 1.0
			} , 0 );
	}

	/**
	 * Get {@link Texture} for color map of {@link Material}.
	 *
	 * @param   gl              OpenGL context.
	 * @param   material        Material to get color map texture from.
	 * @param   textureCache    Texture cache.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getColorMapTexture( final GL gl , final Material material , final Map<String,Texture> textureCache  )
	{
		final Texture result;

		if ( ( material != null ) && ( material.colorMap != null ) )
		{
			result = getTexture( gl , material , textureCache );
		}
		else
		{
			result = null;
		}
		return result;
	}

	/**
	 * Get {@link Texture} for bump map of {@link Material}.
	 *
	 * @param   gl              OpenGL context.
	 * @param   material        MAterial to get bump map texture from.
	 * @param   textureCache    Texture cache.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getBumpMapTexture( final GL gl , final Material material , final Map<String,Texture> textureCache )
	{
		Texture result = null;

		if ( ( material != null ) && TextTools.isNonEmpty( material.bumpMap ) )
		{
			result = textureCache.get( material.bumpMap );

			if ( result == null )
			{
				BufferedImage bufferedImage = MapTools.loadImage( material.bumpMap );
				if ( bufferedImage != null )
				{
					bufferedImage = createNormalMapFromBumpMap( bufferedImage );

					final boolean autoMipmapGeneration = ( gl.isExtensionAvailable( "GL_VERSION_1_4"          ) ||
					                                       gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" ) );

					System.out.println( "MipMap: " + ( autoMipmapGeneration ? "enabled" : "disabled" ) );

					result = TextureIO.newTexture( createCompatibleTextureImage( bufferedImage , gl ) , autoMipmapGeneration );

					result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

					if ( autoMipmapGeneration )
					{
						try
						{
							/**
							 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
							 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
							 */
							result.setTexParameteri( GL.GL_GENERATE_MIPMAP , GL.GL_TRUE );

							/** Set texture magnification to linear to support mipmaps. */
							result.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

							/** Set texture minification to linear_mipmap)_nearest to support mipmaps */
							result.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
						}
						catch ( GLException e )
						{
							/*
							 * If setting texture parameters fails, it's no
							 * severe problem. Catch any exception so the view
							 * doesn't crash.
							 */
							e.printStackTrace();
						}
					}
				}

				textureCache.put( material.bumpMap , result );
			}
		}

		return result;
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   gl              OpenGL context.
	 * @param   material        {@link Material} to get texture for.
	 * @param   textureCache    Map containing the cached textures.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public static Texture getTexture( final GL gl , final Material material , final Map<String,Texture> textureCache )
	{
		return getTexture( gl , material.colorMap , textureCache );
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   gl              OpenGL context.
	 * @param   map             Name of the texture map.
	 * @param   textureCache    Map containing the cached textures.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public static Texture getTexture( final GL gl , final String map , final Map<String,Texture> textureCache )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( map ) )
		{
			result = textureCache.get( map );

			if ( result == null )
			{
				final BufferedImage bufferedImage = MapTools.loadImage( map );
				if ( bufferedImage != null )
				{
					final boolean autoMipmapGeneration = ( gl.isExtensionAvailable( "GL_VERSION_1_4"          ) ||
					                                       gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" ) );

					System.out.println( "MipMap: " + ( autoMipmapGeneration ? "enabled" : "disabled" ) );

					result = TextureIO.newTexture( createCompatibleTextureImage( bufferedImage , gl ) , autoMipmapGeneration );

					result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

					if ( autoMipmapGeneration )
					{
						try
						{
							/**
							 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
							 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
							 */
							result.setTexParameteri( GL.GL_GENERATE_MIPMAP , GL.GL_TRUE );

							/** Set texture magnification to linear to support mipmaps. */
							result.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

							/** Set texture minification to linear_mipmap)_nearest to support mipmaps */
							result.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
						}
						catch ( GLException e )
						{
							/*
							 * If setting texture parameters fails, it's no
							 * severe problem. Catch any exception so the view
							 * doesn't crash.
							 */
							e.printStackTrace();
						}
					}
				}

				textureCache.put( map , result );
			}
		}

		return result;
	}

	/**
	 * Scales the given image, if necessary, such that it is compatible with the
	 * given GL context. The aspect ratio of the image may not be preserved.
	 *
	 * @param   image   Image to be scaled, if necessary.
	 * @param   gl      GL context.
	 *
	 * @return  Compatible texture image. If the given image already meets all
	 *          requirements, that same image is returned.
	 *
	 * @throws  IllegalStateException if the given GL context specifies a
	 *          non-positive maximum texture size.
	 */
	public static BufferedImage createCompatibleTextureImage( final BufferedImage image , final GL gl )
	{
		/*
		 * Textures must not exceed the maximum size. According to the OpenGL
		 * specification, this must be at least 64.
		 */
		final int[] maxTextureSizeBuffer = new int[ 1 ];
		gl.glGetIntegerv( GL.GL_MAX_TEXTURE_SIZE , maxTextureSizeBuffer , 0 );
		final int maximumTextureSize = Math.max( 64 , maxTextureSizeBuffer[ 0 ] );

		int scaledWidth  = Math.min( maximumTextureSize , image.getWidth()  );
		int scaledHeight = Math.min( maximumTextureSize , image.getHeight() );

		/*
		 * Texture sizes may need to be powers of two.
		 */
		if ( !gl.isExtensionAvailable( "GL_ARB_texture_non_power_of_two" ) )
		{
			scaledWidth  = MathTools.nearestPowerOfTwo( scaledWidth  );
			scaledHeight = MathTools.nearestPowerOfTwo( scaledHeight );
		}

		return ImageTools.createScaledInstance( image , scaledWidth , scaledHeight , false );
	}

	/**
	 * Loads a shader and returns an int which specifies the shader's location on
	 * the graphics card. If the shader couldn't be compiled an error will be
	 * shown in the console.
	 *
	 * @param gl            OpenGL context.
	 * @param shader        Shader to compile
	 * @param shaderType    Type of shader.
	 *
	 * @return Returns the shader, returns 0 when the shader couldn't compile.
	 */
	public static int compileAndLoadShader( final GL gl , final String[] shader , final int shaderType )
	{
		final int   shaderId      = gl.glCreateShaderObjectARB( shaderType );
		final int[] lengths       = new int[ shader.length ];
		final int[] compileShader = new int[ 1 ];
		      int   result        = 0;

		for( int i = 0 ; i < shader.length ; i++)
			lengths[ i ] = shader[ i ].length();
		gl.glShaderSourceARB( shaderId , shader.length , shader , lengths , 0 );
		gl.glCompileShaderARB( shaderId );

		gl.glGetShaderiv( shaderId , GL.GL_COMPILE_STATUS , compileShader , 0 );

		if ( compileShader[ 0 ] == GL.GL_TRUE )
		{
			result = shaderId;
		}
		else
		{
			final IntBuffer len = BufferUtil.newIntBuffer( 1 );
			gl.glGetShaderiv( shaderId , GL.GL_INFO_LOG_LENGTH , len );
			final int infoLen = len.get();

			if( infoLen > 1 )
			{
				len.flip();
				final ByteBuffer infoLogBuf = BufferUtil.newByteBuffer( infoLen );
				gl.glGetShaderInfoLog(  shaderId , infoLen , len , infoLogBuf );

				final int actualWidth = len.get();
				final StringBuilder sBuf = new StringBuilder( actualWidth );

				for ( int i = 0 ; i < actualWidth ; i++ )
				{
					sBuf.append( (char) infoLogBuf.get() );
				}

				System.err.println( sBuf.toString() );

			}
			else
			{
				System.err.println( "ERROR: But no info returned from info log" );
			}
		}
		return result;
	}

	/**
	 * Creats and loads a shader program. It also tries to attach the specified
	 * shaders to the program, these shaders must be compiled already. If the
	 * shader program couldn't be loaded an error is shown in the console.
	 *
	 * @param gl        OpenGL context.
	 * @param shaders   Shaders to link and use.
	 *
	 * @return Returns the shader program, will return 0 when the program
	 *                 couldn't be loaded.
	 */
	public static int loadProgram( final GL gl , final int[] shaders )
	{
		final int shaderProgramId = gl.glCreateProgramObjectARB();

		// link shaders
		for( final int shader : shaders )
		{
			gl.glAttachObjectARB( shaderProgramId , shader );
		}

		gl.glLinkProgramARB(shaderProgramId);
		final int[] linkStatus = new int[ 1 ];

		gl.glGetObjectParameterivARB( shaderProgramId , GL.GL_OBJECT_LINK_STATUS_ARB , linkStatus , 0 );

		if ( linkStatus[ 0 ] == GL.GL_FALSE )
		{
			final IntBuffer len = BufferUtil.newIntBuffer( 1 );
			gl.glGetObjectParameterivARB( shaderProgramId , GL.GL_OBJECT_INFO_LOG_LENGTH_ARB , len );
			final int infoLen = len.get(); // this value includes the null, where actualWidth does not

			if( infoLen > 1 )
			{
				len.flip();
				final ByteBuffer infoLogBuf = BufferUtil.newByteBuffer( infoLen );
				gl.glGetInfoLogARB( shaderProgramId , infoLen , len , infoLogBuf );

				final int actualWidth = len.get();
				final StringBuilder sBuf = new StringBuilder( actualWidth );

				for ( int i = 0 ; i < actualWidth ; i++ )
				{
					sBuf.append( (char) infoLogBuf.get() );
				}

				System.err.println( sBuf.toString() );

			}
			else
			{
				System.err.println( "ERROR: But no info returned from info log" );
			}
		}
		return shaderProgramId;
	}

	/**
	 * Compiles both shaders and attaches them to a newly created shader program.
	 *
	 * @param   gl              OpenGL context.
	 * @param   fragmentShader  Fragment shader to use.
	 * @param   vertexShader    Vertes shader to use.
	 *
	 * @return  Returns the shader program, fill
	 */
	public static int loadShaders( final GL gl , final String[] fragmentShader , final String[] vertexShader )
	{
		final int fragShader;
		final int vertShader;
		final int result;

		fragShader = compileAndLoadShader( gl , fragmentShader , GL.GL_FRAGMENT_SHADER_ARB );
		vertShader = compileAndLoadShader( gl , vertexShader , GL.GL_VERTEX_SHADER_ARB );

		if( fragShader != 0 && vertShader != 0 )
		{
			result = loadProgram( gl , new int[]{ fragShader , vertShader } );
		}
		else
		{
			result = 0;
		}
		return result;
	}

	/**
	 * Creates a normal map from the given bump map.
	 *
	 * @param   bumpMap     Bump map.
	 *
	 * @return  Normal map.
	 */
	public static BufferedImage createNormalMapFromBumpMap( final BufferedImage bumpMap )
	{
		final int width  = bumpMap.getWidth()  - 2;
		final int height = bumpMap.getHeight() - 2;

		final BufferedImage result = new BufferedImage( width , height , BufferedImage.TYPE_INT_RGB );

		for ( int y = 1 ; y <= height ; y++ )
		{
			for ( int x = 1 ; x <= width ; x++ )
			{
				final int corner1 = bumpMap.getRGB( x - 1 , y - 1 );
				final int corner2 = bumpMap.getRGB( x + 1 , y - 1 );
				final int corner3 = bumpMap.getRGB( x - 1 , y + 1 );
				final int corner4 = bumpMap.getRGB( x + 1 , y + 1 );
				final int edgeX1  = bumpMap.getRGB( x - 1 , y     );
				final int edgeX2  = bumpMap.getRGB( x + 1 , y     );
				final int edgeY1  = bumpMap.getRGB( x     , y + 1 );
				final int edgeY2  = bumpMap.getRGB( x     , y - 1 );

				/*
				 * Apply sobel operation in x and y directions to determine the
				 * approximate normal vector.
				 */
				int sobelX = ( ( corner1 & 0xff ) + ( edgeX1 & 0xff ) * 2 + ( corner3 & 0xff ) -
				               ( corner2 & 0xff ) - ( edgeX2 & 0xff ) * 2 - ( corner4 & 0xff ) ) / 8;

				int sobelY = ( ( corner3 & 0xff ) + ( edgeY1 & 0xff ) * 2 + ( corner4 & 0xff ) -
				               ( corner1 & 0xff ) - ( edgeY2 & 0xff ) * 2 - ( corner2 & 0xff ) ) / 8;

				/*
				 * Amplify the bumpiness by a factor of 2.
				 */
				sobelX = Math.max( -0x80 , Math.min( 0x7f , sobelX * 2 ) );
				sobelY = Math.max( -0x80 , Math.min( 0x7f , sobelY * 2 ) );

				final int z = (int)Math.sqrt( (double)( 0xff * 0xff - sobelX * sobelX - sobelY * sobelY ) ) / 2 + 0x80;

				sobelX = sobelX + 0x80;
				sobelY = sobelY + 0x80;
				result.setRGB( x - 1 , y - 1 , sobelX << 16 | sobelY << 8 | z );
			}
		}

		return result;
	}

	/**
	 * Creates a normalization cube map, used to perform DOT3 bump mapping. For
	 * each 3D texture coordinate, the value of the map represents the
	 * normalized vector from the origin in the direction of the coordinate.
	 *
	 * @param   gl  GL context.
	 *
	 * @return  Normalization cube map.
	 */
	public static Texture createNormalizationCubeMap( final GL gl )
	{
		final Texture result = TextureIO.newTexture( GL.GL_TEXTURE_CUBE_MAP );
		result.bind();

		final int   size     = 128;
		final double offset   = 0.5;
		final double halfSize = (double)size / 2.0;

		/*
		 * Create the six sides of the cube map.
		 */
		final ByteBuffer data = ByteBuffer.allocate( size * size * 3 );
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x = halfSize;
				final double y = ( (double)j + offset - halfSize );
				final double z = -( (double)i + offset - halfSize );

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		data.rewind();
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x = -halfSize;
				final double y = (double)j + offset - halfSize;
				final double z = (double)i + offset - halfSize;

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		data.rewind();
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x = (double)i + offset - halfSize;
				final double y = -halfSize;
				final double z = (double)j + offset - halfSize;

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		data.rewind();
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x =  ( (double)i + offset - halfSize );
				final double y = halfSize;
				final double z = -( (double)j + offset - halfSize );

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		data.rewind();
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x = (double)i + offset - halfSize;
				final double y = (double)j + offset - halfSize;
				final double z = halfSize;

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		data.rewind();
		for ( int j = 0; j < size; j++ )
		{
			for ( int i = 0; i < size; i++ )
			{
				final double x = -( (double)i + offset - halfSize );
				final double y =  ( (double)j + offset - halfSize );
				final double z = -halfSize;

				// scale to [0..1]
				final Vector3D normalized = Vector3D.normalize( x , y , z );
				data.put( (byte)( ( normalized.x + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.y + 1.0 ) * 255.0 / 2.0 ) );
				data.put( (byte)( ( normalized.z + 1.0 ) * 255.0 / 2.0 ) );
			}
		}
		data.rewind();
		gl.glTexImage2D( GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z , 0 , GL.GL_RGB8 , size , size , 0 , GL.GL_RGB , GL.GL_UNSIGNED_BYTE , data );

		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR        );
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR        );
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_S     , GL.GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_T     , GL.GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL.GL_TEXTURE_WRAP_R     , GL.GL_CLAMP_TO_EDGE );

		return result;
	}
}
