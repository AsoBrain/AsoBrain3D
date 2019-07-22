/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.awt.image.*;
import java.nio.*;

import ab.j3d.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.*;
import com.jogamp.opengl.util.texture.*;

/**
 * Utility methods for JOGL.
 *
 * @author  G.B.M. Rupert
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
		result.bind( gl );

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
		gl.glTexParameteri( GL.GL_TEXTURE_CUBE_MAP , GL2ES2.GL_TEXTURE_WRAP_R , GL.GL_CLAMP_TO_EDGE );

		return result;
	}

	/**
	 * Renders the given texture to the screen. The rectangle to render to is
	 * specified using normalized device coordinates.
	 *
	 * @param   gl          OpenGL pipeline.
	 * @param   texture     Texture.
	 * @param   x1          X-coordinate of left side.
	 * @param   y1          Y-coordinate of bottom side.
	 * @param   x2          X-coordinate of right side.
	 * @param   y2          Y-coordinate of top side.
	 */
	public static void renderToScreen( final GL gl, final int texture, final float x1, final float y1, final float x2, final float y2 )
	{
		final GL2 gl2 = gl.getGL2();

		gl2.glPushAttrib( GL.GL_DEPTH_BUFFER_BIT );
		gl.glDisable( GL.GL_DEPTH_TEST );

		gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
		gl2.glPushMatrix();
		gl2.glLoadIdentity();
		gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl2.glPushMatrix();
		gl2.glLoadIdentity();

		gl.glBindTexture( GL.GL_TEXTURE_2D, texture );
		gl.glEnable( GL.GL_TEXTURE_2D );

		gl2.glColor3f( 1.0f, 1.0f, 1.0f );
		gl2.glBegin( GL2.GL_QUADS );
		gl2.glTexCoord2f( 0.0f, 0.0f );
		gl2.glVertex2f( x1, y1 );
		gl2.glTexCoord2f( 1.0f, 0.0f );
		gl2.glVertex2f( x2, y1 );
		gl2.glTexCoord2f( 1.0f, 1.0f );
		gl2.glVertex2f( x2, y2 );
		gl2.glTexCoord2f( 0.0f, 1.0f );
		gl2.glVertex2f( x1, y2 );
		gl2.glEnd();

		gl.glDisable( GL.GL_TEXTURE_2D );
		gl.glBindTexture( GL.GL_TEXTURE_2D, 0 );

		gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
		gl2.glPopMatrix();
		gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
		gl2.glPopMatrix();

		gl2.glPopAttrib();
	}
}
