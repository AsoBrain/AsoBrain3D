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
package ab.j3d.view.jogl2;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Utility methods for JOGL.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JOGLTools()
	{
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   transform   Transformation to multiply with.
	 */
	public static void glMultMatrixd( final GL gl , final Matrix3D transform )
	{
		final GL2 gl2 = gl.getGL2();
		gl2.glMultMatrixd( new double[]
			{
				transform.xx , transform.yx , transform.zx , 0.0 ,
				transform.xy , transform.yy , transform.zy , 0.0 ,
				transform.xz , transform.yz , transform.zz , 0.0 ,
				transform.xo , transform.yo , transform.zo , 1.0
			} , 0 );
	}

	/**
	 * Loads a shader and returns an int which specifies the shader's location on
	 * the graphics card. If the shader couldn't be compiled an error will be
	 * shown in the console.
	 *
	 * @param gl            OpenGL pipeline.
	 * @param shader        Shader to compile
	 * @param shaderType    Type of shader.
	 *
	 * @return Returns the shader, returns 0 when the shader couldn't compile.
	 */
	public static int compileAndLoadShader( final GL gl , final String[] shader , final int shaderType )
	{
		final GL2 gl2 = gl.getGL2();

		final int   shaderId      = gl2.glCreateShaderObjectARB( shaderType );
		final int[] lengths       = new int[ shader.length ];
		final int[] compileShader = new int[ 1 ];
		      int   result        = 0;

		for( int i = 0 ; i < shader.length ; i++)
			lengths[ i ] = shader[ i ].length();
		gl2.glShaderSourceARB( shaderId , shader.length , shader , lengths , 0 );
		gl2.glCompileShaderARB( shaderId );

		gl2.glGetShaderiv( shaderId , GL2.GL_COMPILE_STATUS , compileShader , 0 );

		if ( compileShader[ 0 ] == GL.GL_TRUE )
		{
			result = shaderId;
		}
		else
		{
			final IntBuffer len = BufferUtil.newIntBuffer( 1 );
			gl2.glGetShaderiv( shaderId , GL2.GL_INFO_LOG_LENGTH , len );
			final int infoLen = len.get();

			if( infoLen > 1 )
			{
				len.flip();
				final ByteBuffer infoLogBuf = BufferUtil.newByteBuffer( infoLen );
				gl2.glGetShaderInfoLog(  shaderId , infoLen , len , infoLogBuf );

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
	 * @param gl        OpenGL pipeline.
	 * @param shaders   Shaders to link and use.
	 *
	 * @return Returns the shader program, will return 0 when the program
	 *                 couldn't be loaded.
	 */
	public static int loadProgram( final GL gl , final int[] shaders )
	{
		final GL2 gl2 = gl.getGL2();

		final int shaderProgramId = gl2.glCreateProgramObjectARB();

		// link shaders
		for( final int shader : shaders )
		{
			gl2.glAttachObjectARB( shaderProgramId , shader );
		}

		gl2.glLinkProgramARB(shaderProgramId);
		final int[] linkStatus = new int[ 1 ];

		gl2.glGetObjectParameterivARB( shaderProgramId , GL2.GL_OBJECT_LINK_STATUS_ARB , linkStatus , 0 );

		if ( linkStatus[ 0 ] == GL.GL_FALSE )
		{
			final IntBuffer len = BufferUtil.newIntBuffer( 1 );
			gl2.glGetObjectParameterivARB( shaderProgramId , GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB , len );
			final int infoLen = len.get(); // this value includes the null, where actualWidth does not

			if( infoLen > 1 )
			{
				len.flip();
				final ByteBuffer infoLogBuf = BufferUtil.newByteBuffer( infoLen );
				gl2.glGetInfoLogARB( shaderProgramId , infoLen , len , infoLogBuf );

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
	 * @param   gl              OpenGL pipeline.
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

		fragShader = compileAndLoadShader( gl , fragmentShader , GL2.GL_FRAGMENT_SHADER );
		vertShader = compileAndLoadShader( gl , vertexShader , GL2.GL_VERTEX_SHADER );

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
	 * @param   gl  OpenGL pipeline.
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
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2.GL_TEXTURE_WRAP_R    , GL.GL_CLAMP_TO_EDGE );

		return result;
	}
}
