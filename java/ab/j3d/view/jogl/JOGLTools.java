/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.Map;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

import com.numdata.oss.TextTools;
import com.numdata.oss.io.SoftHashMap;

/**
 * Utility methods related to JOGL implementation of view model.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLTools
{
	/**
	 * Texture map cache (maps map name to texture).
	 */
	private static final SoftHashMap<String, Texture> _textureCache = new SoftHashMap();

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JOGLTools()
	{
	}

	/**
	 * Paint 3D object on GL context.
	 *
	 * @param gl             GL context.
	 * @param object3D       Object to draw.
	 * @param node2gl        Transformation from node to current GL transform.
	 * @param fill           Fill polygons.
	 * @param fillColor      Override color to use for filling.
	 * @param outline        Draw polygon outlines.
	 * @param outlineColor   Override color to use for outlines.
	 * @param useAlternative Use alternative vs. regular object colors.
	 */
	public static void paintObject3D( final GL gl , final Object3D object3D , final Matrix3D node2gl , final boolean fill , final Color fillColor , final boolean outline , final Color outlineColor , final boolean useAlternative )
	{
		/*
		 * Push the current matrix stack down by one
		 */
		gl.glPushMatrix();

		/*
		 * Set the object transform
		 */
		glMultMatrixd( gl , node2gl );

		/*
		 * Draw the object3d.
		 */
		for ( int i = 0; i < object3D.getFaceCount(); i++ )
		{
			final Face3D face = object3D.getFace( i );

			if ( fill )
			{
				if ( fillColor != null )
				{
					final float[] argb = fillColor.getRGBComponents( null );
					gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
					gl.glDisable( GL.GL_LIGHTING );
				}
				else
				{
					gl.glShadeModel( face.isSmooth() ? GL.GL_SMOOTH : GL.GL_FLAT );
					gl.glEnable( GL.GL_LIGHTING );
					setMaterial( gl , face.getMaterial() );

					if ( face.isTwoSided() )
					{
						gl.glDisable( GL.GL_CULL_FACE );
					}
					else
					{
						gl.glEnable( GL.GL_CULL_FACE );
						gl.glCullFace( GL.GL_FRONT );
					}

				}

				gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL );


				drawFace( gl, object3D.getFace( i ) );
			}

			if ( outline )
			{
				final Color color;
				if ( outlineColor != null )
				{
					color = outlineColor;
				}
				else
				if ( useAlternative && ( object3D.alternateOutlinePaint instanceof Color ) )
				{
					color = (Color)object3D.alternateOutlinePaint;
				}
				else if ( object3D.outlinePaint instanceof Color )
				{
					color = (Color)object3D.outlinePaint;
				}
				else
				{
					color = Color.WHITE;
				}

				final float[] argb = color.getRGBComponents( null );
				gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

				// enable backface culling
				gl.glEnable( GL.GL_CULL_FACE );
				gl.glCullFace( GL.GL_FRONT );

				// set polygonmode to lines only
				gl.glPolygonMode( GL.GL_BACK , GL.GL_LINE );

				gl.glDisable( GL.GL_LIGHTING );
				drawFace( gl , object3D.getFace( i ) );
				gl.glEnable( GL.GL_LIGHTING );

				//disable backface culling
				gl.glDisable( GL.GL_CULL_FACE );
			}
		}

		/*
		 * Pop matrix stack, replace current matrix with one below it on the stack.
		 */
		gl.glPopMatrix();
	}

	/**
	 * Draw 3D face on GL context.
	 *
	 * @param gl   GL context.
	 * @param face Face draw.
	 */
	private static void drawFace( final GL gl , final Face3D face )
	{
		final Object3D object            = face.getObject();
		final double[] vertexCoordinates = object.getVertexCoordinates();

		final int vertexCount = face.getVertexCount();
		switch ( vertexCount )
		{
			case 0:
			case 1:
				break;

			case 2:
			{
				final int[] faceVertexIndices = face.getVertexIndices();

				final int vi1 = faceVertexIndices[ 0 ] * 3;
				final int vi2 = faceVertexIndices[ 1 ] * 3;

				final double v1x = vertexCoordinates[ vi1 ];
				final double v1y = vertexCoordinates[ vi1 + 1 ];
				final double v1z = vertexCoordinates[ vi1 + 2 ];
				final double n1x = face.getVertexNormalX( 0 );
				final double n1y = face.getVertexNormalY( 0 );
				final double n1z = face.getVertexNormalZ( 0 );
				final double v2x = vertexCoordinates[ vi2 ];
				final double v2y = vertexCoordinates[ vi2 + 1 ];
				final double v2z = vertexCoordinates[ vi2 + 2 ];
				final double n2x = face.getVertexNormalX( 1 );
				final double n2y = face.getVertexNormalY( 1 );
				final double n2z = face.getVertexNormalZ( 1 );

				drawLine( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z , n2x , n2y , n2z );
			}
			break;

			case 3:
			{
				final int[] faceVertexIndices = face.getVertexIndices();

				final int vi1 = faceVertexIndices[ 0 ] * 3;
				final int vi2 = faceVertexIndices[ 1 ] * 3;
				final int vi3 = faceVertexIndices[ 2 ] * 3;

				final double v1x = vertexCoordinates[ vi1 ];
				final double v1y = vertexCoordinates[ vi1 + 1 ];
				final double v1z = vertexCoordinates[ vi1 + 2 ];
				final double n1x = face.getVertexNormalX( 0 );
				final double n1y = face.getVertexNormalY( 0 );
				final double n1z = face.getVertexNormalZ( 0 );
				final double v2x = vertexCoordinates[ vi2 ];
				final double v2y = vertexCoordinates[ vi2 + 1 ];
				final double v2z = vertexCoordinates[ vi2 + 2 ];
				final double n2x = face.getVertexNormalX( 1 );
				final double n2y = face.getVertexNormalY( 1 );
				final double n2z = face.getVertexNormalZ( 1 );
				final double v3x = vertexCoordinates[ vi3 ];
				final double v3y = vertexCoordinates[ vi3 + 1 ];
				final double v3z = vertexCoordinates[ vi3 + 2 ];
				final double n3x = face.getVertexNormalX( 2 );
				final double n3y = face.getVertexNormalY( 2 );
				final double n3z = face.getVertexNormalZ( 2 );

				//Determine if face has texture
				if (face.getMaterial().colorMap != null)
				{
					final Texture texture = getColorMapTexture( face );
					final float[] textureU = face.getTextureU();
					final float[] textureV = face.getTextureV();
					gl.glEnable( GL.GL_TEXTURE_2D );
					texture.bind();
						drawTriangle( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z , n2x , n2y , n2z , v3x , v3y , v3z , n3x , n3y , n3z , textureU , textureV );
					gl.glDisable( GL.GL_TEXTURE_2D );
					gl.glDeleteTextures( texture.getTarget(), IntBuffer.allocate( texture.getTextureObject() ) );
				}
				else
				{
						drawTriangle( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z , n2x , n2y , n2z , v3x , v3y , v3z , n3x , n3y , n3z );
				}
			}
			break;

			case 4:
			{
				final int[] faceVertexIndices = face.getVertexIndices();

				final int vi1 = faceVertexIndices[ 0 ] * 3;
				final int vi2 = faceVertexIndices[ 1 ] * 3;
				final int vi3 = faceVertexIndices[ 2 ] * 3;
				final int vi4 = faceVertexIndices[ 3 ] * 3;

				final double v1x = vertexCoordinates[ vi1 ];
				final double v1y = vertexCoordinates[ vi1 + 1 ];
				final double v1z = vertexCoordinates[ vi1 + 2 ];
				final double n1x = face.getVertexNormalX( 0 );
				final double n1y = face.getVertexNormalY( 0 );
				final double n1z = face.getVertexNormalZ( 0 );
				final double v2x = vertexCoordinates[ vi2 ];
				final double v2y = vertexCoordinates[ vi2 + 1 ];
				final double v2z = vertexCoordinates[ vi2 + 2 ];
				final double n2x = face.getVertexNormalX( 1 );
				final double n2y = face.getVertexNormalY( 1 );
				final double n2z = face.getVertexNormalZ( 1 );
				final double v3x = vertexCoordinates[ vi3 ];
				final double v3y = vertexCoordinates[ vi3 + 1 ];
				final double v3z = vertexCoordinates[ vi3 + 2 ];
				final double n3x = face.getVertexNormalX( 2 );
				final double n3y = face.getVertexNormalY( 2 );
				final double n3z = face.getVertexNormalZ( 2 );
				final double v4x = vertexCoordinates[ vi4 ];
				final double v4y = vertexCoordinates[ vi4 + 1 ];
				final double v4z = vertexCoordinates[ vi4 + 2 ];
				final double n4x = face.getVertexNormalX( 3 );
				final double n4y = face.getVertexNormalY( 3 );
				final double n4z = face.getVertexNormalZ( 3 );

				//Determine if face has texture
				if ( face.getMaterial().colorMap != null )
				{
					final Texture texture = getColorMapTexture( face );
					final float[] textureU = face.getTextureU();
					final float[] textureV = face.getTextureV();
					gl.glEnable( GL.GL_TEXTURE_2D );
					texture.bind();
					drawQuad( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z, n2x , n2y , n2z , v3x , v3y , v3z, n3x , n3y , n3z , v4x , v4y , v4z , n4x , n4y , n4z , textureU, textureV );
					gl.glDisable( GL.GL_TEXTURE_2D );
					texture.disable();
				}
				else
				{
					drawQuad( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z, n2x , n2y , n2z , v3x , v3y , v3z, n3x , n3y , n3z , v4x , v4y , v4z , n4x , n4y , n4z );
				}
			}
			break;

			default:
			{
				final int[] triangles = face.triangulate();
				if ( triangles != null )
				{
					// Draw the triangles.
					final int[] faceVertexIndices = face.getVertexIndices();

					for ( int j = 0; j < triangles.length; j = j + 3 )
					{
						final int vi1 = faceVertexIndices[ triangles[ j ] ] * 3;
						final int vi2 = faceVertexIndices[ triangles[ j + 1 ] ] * 3;
						final int vi3 = faceVertexIndices[ triangles[ j + 2 ] ] * 3;

						final double v1x = vertexCoordinates[ vi1 ];
						final double v1y = vertexCoordinates[ vi1 + 1 ];
						final double v1z = vertexCoordinates[ vi1 + 2 ];
						final double n1x = face.getVertexNormalX( 0 );
						final double n1y = face.getVertexNormalY( 0 );
						final double n1z = face.getVertexNormalZ( 0 );
						final double v2x = vertexCoordinates[ vi2 ];
						final double v2y = vertexCoordinates[ vi2 + 1 ];
						final double v2z = vertexCoordinates[ vi2 + 2 ];
						final double n2x = face.getVertexNormalX( 1 );
						final double n2y = face.getVertexNormalY( 1 );
						final double n2z = face.getVertexNormalZ( 1 );
						final double v3x = vertexCoordinates[ vi3 ];
						final double v3y = vertexCoordinates[ vi3 + 1 ];
						final double v3z = vertexCoordinates[ vi3 + 2 ];
						final double n3x = face.getVertexNormalX( 2 );
						final double n3y = face.getVertexNormalY( 2 );
						final double n3z = face.getVertexNormalZ( 2 );

						//Determine if face has texture
						if (face.getMaterial().colorMap!= null)
						{
							final Texture texture = getColorMapTexture( face );
							final float[] textureU = face.getTextureU();
							final float[] textureV = face.getTextureV();
							gl.glEnable( GL.GL_TEXTURE_2D );
							texture.bind();
							drawTriangle( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z , n2x , n2y , n2z , v3x , v3y , v3z , n3x , n3y , n3z , textureU , textureV );
							gl.glDisable( GL.GL_TEXTURE_2D );
							gl.glDeleteTextures( texture.getTarget(), IntBuffer.allocate( texture.getTextureObject() ) );
						}
						else
						{
							drawTriangle( gl , v1x , v1y , v1z , n1x , n1y , n1z , v2x , v2y , v2z , n2x , n2y , n2z , v3x , v3y , v3z , n3x , n3y , n3z );
						}
					}
				}
			}
			break;
		}
	}

	/**
	 * Draw line using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 */
	public static void drawLine( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                             final double n1X , final double n1Y , final double n1Z ,
	                             final double v2X , final double v2Y , final double v2Z ,
	                             final double n2X , final double n2Y , final double n2Z )
	{
		gl.glBegin( GL.GL_LINES );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );

		gl.glEnd();
	}

	/**
	 * Draw line using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 * @param textureU horizontal coordinate of texture
	 * @param textureV vertical coordinate of texture
	 */
	public static void drawLine( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                             final double  n1X , final double  n1Y , final double n1Z ,
	                             final double  v2X , final double  v2Y , final double v2Z ,
	                             final double  n2X , final double  n2Y , final double n2Z ,
	                             final float[] textureU , final float[] textureV )
	{
		gl.glBegin( GL.GL_LINES );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glTexCoord2f( textureU[ 0 ] , textureV[ 0 ] );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glTexCoord2f( textureU[ 1 ] , textureV[ 1 ] );

		gl.glEnd();
	}

	/**
	 * Draw quad using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 * @param v3X X coordinate of third vertex.
	 * @param v3Y Y coordinate of third vertex.
	 * @param v3Z Z coordinate of third vertex.
	 * @param n3X X coordinate of third normal.
	 * @param n3Y Y coordinate of third normal.
	 * @param n3Z Z coordinate of third normal.
	 * @param v4X X coordinate of fourth vertex.
	 * @param v4Y Y coordinate of fourth vertex.
	 * @param v4Z Z coordinate of fourth vertex.
	 * @param n4X X coordinate of fourth normal.
	 * @param n4Y Y coordinate of fourth normal.
	 * @param n4Z Z coordinate of fourth normal.
	 * @param textureU horizontal coodrinate of texture.
	 * @param textureV vertical coordinate of texture.
	 */
	public static void drawQuad( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                             final double  n1X , final double  n1Y , final double n1Z ,
	                             final double  v2X , final double  v2Y , final double v2Z ,
	                             final double  n2X , final double  n2Y , final double n2Z ,
	                             final double  v3X , final double  v3Y , final double v3Z ,
	                             final double  n3X , final double  n3Y , final double n3Z ,
	                             final double  v4X , final double  v4Y , final double v4Z ,
	                             final double  n4X , final double  n4Y , final double n4Z ,
	                             final float[] textureU , final float[] textureV )
	{
		gl.glBegin( GL.GL_QUADS );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glTexCoord2f( textureU[ 0 ] , textureV[ 0 ] );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glTexCoord2f( textureU[ 1 ] , textureV[ 1 ] );

		gl.glNormal3d( n3X , n3Y , n3Z );
		gl.glVertex3d( v3X , v3Y , v3Z );
		gl.glTexCoord2f( textureU[ 2 ] , textureV[ 2 ] );

		gl.glNormal3d( n4X , n4Y , n4Z );
		gl.glVertex3d( v4X , v4Y , v4Z );
		gl.glTexCoord2f( textureU[ 3 ] , textureV[ 3 ] );

		gl.glEnd();
	}

	/**
	 * Draw quad using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 * @param v3X X coordinate of third vertex.
	 * @param v3Y Y coordinate of third vertex.
	 * @param v3Z Z coordinate of third vertex.
	 * @param n3X X coordinate of third normal.
	 * @param n3Y Y coordinate of third normal.
	 * @param n3Z Z coordinate of third normal.
	 * @param v4X X coordinate of fourth vertex.
	 * @param v4Y Y coordinate of fourth vertex.
	 * @param v4Z Z coordinate of fourth vertex.
	 * @param n4X X coordinate of fourth normal.
	 * @param n4Y Y coordinate of fourth normal.
	 * @param n4Z Z coordinate of fourth normal.
	 */
	public static void drawQuad( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                             final double n1X , final double n1Y , final double n1Z ,
	                             final double v2X , final double v2Y , final double v2Z ,
	                             final double n2X , final double n2Y , final double n2Z ,
	                             final double v3X , final double v3Y , final double v3Z ,
	                             final double n3X , final double n3Y , final double n3Z ,
	                             final double v4X , final double v4Y , final double v4Z ,
	                             final double n4X , final double n4Y , final double n4Z )
	{
		gl.glBegin( GL.GL_QUADS );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );

		gl.glNormal3d( n3X , n3Y , n3Z );
		gl.glVertex3d( v3X , v3Y , v3Z );

		gl.glNormal3d( n4X , n4Y , n4Z );
		gl.glVertex3d( v4X , v4Y , v4Z );

		gl.glEnd();
	}

	/**
	 * Draw triangle using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 * @param v3X X coordinate of third vertex.
	 * @param v3Y Y coordinate of third vertex.
	 * @param v3Z Z coordinate of third vertex.
	 * @param n3X X coordinate of third normal.
	 * @param n3Y Y coordinate of third normal.
	 * @param n3Z Z coordinate of third normal.
	 */
	public static void drawTriangle( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                                 final double n1X , final double n1Y , final double n1Z ,
	                                 final double v2X , final double v2Y , final double v2Z ,
	                                 final double n2X , final double n2Y , final double n2Z ,
	                                 final double v3X , final double v3Y , final double v3Z ,
	                                 final double n3X , final double n3Y , final double n3Z )
	{
		gl.glBegin( GL.GL_TRIANGLES );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );

		gl.glNormal3d( n3X , n3Y , n3Z );
		gl.glVertex3d( v3X , v3Y , v3Z );

		gl.glEnd();
	}

	/**
	 * Draw triangle using GL.
	 *
	 * @param gl  GL context.
	 * @param v1X X coordinate of first vertex.
	 * @param v1Y Y coordinate of first vertex.
	 * @param v1Z Z coordinate of first vertex.
	 * @param n1X X coordinate of first normal.
	 * @param n1Y Y coordinate of first normal.
	 * @param n1Z Z coordinate of first normal.
	 * @param v2X X coordinate of second vertex.
	 * @param v2Y Y coordinate of second vertex.
	 * @param v2Z Z coordinate of second vertex.
	 * @param n2X X coordinate of second normal.
	 * @param n2Y Y coordinate of second normal.
	 * @param n2Z Z coordinate of second normal.
	 * @param v3X X coordinate of third vertex.
	 * @param v3Y Y coordinate of third vertex.
	 * @param v3Z Z coordinate of third vertex.
	 * @param n3X X coordinate of third normal.
	 * @param n3Y Y coordinate of third normal.
	 * @param n3Z Z coordinate of third normal.
	 * @param textureU horizontal coordinate of texture.
	 * @param textureV vertical coordinate of texture.
	 */
	public static void drawTriangle( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                                 final double n1X , final double n1Y , final double n1Z ,
	                                 final double v2X , final double v2Y , final double v2Z ,
	                                 final double n2X , final double n2Y , final double n2Z ,
	                                 final double v3X , final double v3Y , final double v3Z ,
	                                 final double n3X , final double n3Y , final double n3Z ,
	                                 final float[] textureU , final float[] textureV )
	{
		gl.glBegin( GL.GL_TRIANGLES );

		gl.glNormal3d( n1X , n1Y , n1Z );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glTexCoord2f( textureU[ 0 ] , textureV[ 0 ] );

		gl.glNormal3d( n2X , n2Y , n2Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glTexCoord2f( textureU[ 1 ] , textureV[ 1 ] );

		gl.glNormal3d( n3X , n3Y , n3Z );
		gl.glVertex3d( v3X , v3Y , v3Z );
		gl.glTexCoord2f( textureU[ 2 ] , textureV[ 2 ] );

		gl.glEnd();
	}


	/**
	 * Clear GL canvas.
	 *
	 * @param gl    GL context.
	 * @param color Color to clear canvas with.
	 */
	public static void glClearColor( final GL gl , final Color color )
	{
		final float[] argb = new float[4];
		color.getRGBComponents( argb );
		gl.glClearColor( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param gl        GL context.
	 * @param transform Transformation to multiply with.
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
	 * Set GL material properties.
	 *
	 * @param gl       GL context.
	 * @param material Material properties.
	 */
	public static void setMaterial( final GL gl , final Material material )
	{
		/* Set color, used when lights are disabled. */
		gl.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );

		/* Set material properties. */
		gl.glMaterialf( GL.GL_FRONT_AND_BACK , GL.GL_SHININESS , (float)material.shininess );
		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_SPECULAR );
		gl.glColor3f( material.specularColorRed , material.specularColorGreen , material.specularColorBlue );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_AMBIENT );
		gl.glColor3f( material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_DIFFUSE );
		gl.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_EMISSION );
		gl.glColor3f( material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );
	}
	/**
	 * Get {@link Texture} for color map of {@link Face3D}.
	 *
	 * @param face Face whose color map texture to return.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getColorMapTexture( final Face3D face )
	{
		final Texture result;

		final Material material = face.getMaterial();
		final float[] textureU = face.getTextureU();
		final float[] textureV = face.getTextureV();

		if ( ( textureU != null ) && ( textureV != null ) && ( material != null ) && ( material.colorMap != null ) )
		{
			result = getTexture( material.colorMap );
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param name Name of texture map to get.
	 *
	 * @return Texture for the specified name; <code>null</code> if the name was
	 *         empty or no map by the given name was found.
	 */
	public static Texture getTexture( final String name )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( name ) )
		{
			final Map<String, Texture> cache = _textureCache;
			if ( cache.containsKey( name ) )
			{
				result = cache.get( name );
			}
			else
			{
				final BufferedImage bufferedImage = MapTools.loadImage( name );
				if ( bufferedImage != null )
				{
					result = TextureIO.newTexture( ( bufferedImage ) , true );
					result.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_NEAREST );
					result.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_NEAREST );
				}
				cache.put( name , result );
			}
		}
		return result;
	}
}