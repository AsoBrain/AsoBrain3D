/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

import com.numdata.oss.TextTools;

/**
 * Utility methods related to JOGL implementation of view model.
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
	 * Paint 3D object on GL context.
	 *
	 * @param glWrapper         GLWrapper.
	 * @param object3D          Object to draw.
	 * @param node2gl           Transformation from node to current GL transform.
	 * @param useTextures       Use textures on this object3D.
	 * @param useAlternate      Use alternative color to paint this object3D.
	 * @param hasLighting       Specify material lighting properties.
	 * @param textureCache      Texture cache.
	 * @param fill              Fill polygons.
	 * @param materialOverride  Material to use instead of actual materials.
	 */
	public static void paintObject3D( final GLWrapper glWrapper , final Object3D object3D , final Matrix3D node2gl , final boolean useTextures , final boolean useAlternate , final boolean hasLighting , final Map<String, SoftReference<Texture>> textureCache , final boolean fill , final Material materialOverride )
	{
		final Paint paint;
		final Paint alternatePaint;

		if ( fill )
		{
			glWrapper.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL );
			paint          = object3D.fillPaint;
			alternatePaint = object3D.alternateFillPaint;
		}
		else
		{
			glWrapper.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_LINE );
			paint          = object3D.outlinePaint;
			alternatePaint = object3D.alternateOutlinePaint;
		}

		Material material = materialOverride;

		if ( useAlternate && material == null )
		{
			final Color color;
			if ( alternatePaint instanceof Color )
			{
				color = (Color)alternatePaint;
			}
			else
			{
				return;
			}
			final float[] argb = color.getRGBComponents( null );
			glWrapper.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

			if ( hasLighting )
			{
				material = new Material( color.getRGB() );
			}
		}
		else if ( !useTextures && material == null )
		{
			final Color color;
			if ( paint instanceof Color )
			{
					color = (Color)paint;
			}
			else
			{
				return;
			}
			final float[] argb = color.getRGBComponents( null );
			glWrapper.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
			if ( hasLighting )
			{
				material = new Material( color.getRGB() );
			}
		}
		else if ( materialOverride != null )
		{
			glWrapper.glColor4f( materialOverride.diffuseColorRed , materialOverride.diffuseColorGreen , materialOverride.diffuseColorBlue , materialOverride.diffuseColorAlpha );
		}

		/*
		 * Push the current matrix stack down by one
		 */
		glWrapper.glPushMatrix();

		/*
		 * Set the object transform
		 */
		glWrapper.glMultMatrixd( node2gl );


		if ( !fill && object3D instanceof ExtrudedObject2D )
		{
			// @FIXME Drawing outlines like this is only needed while Face3D doesn't support concave faces.
			// (A single Face3D could then be used for a concave shape, and the proper outline would be drawn automatically.)

			final ExtrudedObject2D extruded  = (ExtrudedObject2D)object3D;
			final Shape            shape     = extruded.shape;
			final Vector3D         extrusion = extruded.extrusion;

			glWrapper.glPushMatrix();
			glWrapper.glMultMatrixd( extruded.transform );

			/* Draw the bottom outline of the extruded shape. */
			drawShape( glWrapper , shape , extruded.flatness );

			/* Draw the sides of the extruded shape. */
			drawExtrusionLines( glWrapper , shape , extrusion );

			/* Draw the top outline of the extruded shape. */
			glWrapper.glMultMatrixd( Matrix3D.getTransform( 0.0 , 0.0 , 0.0 , extrusion.x , extrusion.y , extrusion.z ) );
			drawShape( glWrapper , shape , extruded.flatness );

			glWrapper.glPopMatrix();
		}
		else
		{
			for ( int i = 0 ; i < object3D.getFaceCount() ; i++ )
			{
				final Face3D face = object3D.getFace( i );

				if ( face.isTwoSided() )
				{
					glWrapper.setCullFace( false );
				}
				else
				{
					glWrapper.setCullFace( true );
					glWrapper.glCullFace( GL.GL_BACK );
				}
				if ( hasLighting )
				{
					if ( material != null )
					{
						setMaterial( glWrapper , material );
					}
					else if ( face.getMaterial() != null )
					{
						setMaterial( glWrapper , face.getMaterial() );
					}
				}

				if ( materialOverride != null )
				{
					final Material temp = face.getMaterial();
					face.setMaterial( materialOverride );
					if ( TextTools.isEmpty( materialOverride.colorMap ) )
					{
						drawFace( glWrapper , face , false , null , hasLighting );
					}
					else
					{
						drawFace( glWrapper , face , true , textureCache , hasLighting );
					}
					face.setMaterial( temp );
				}
				else if ( face.getMaterial() != null && !useAlternate && useTextures )
				{
					drawFace( glWrapper , face , true , textureCache , hasLighting );
				}
				else if ( face.getMaterial() != null )
				{
					drawFace( glWrapper , face , false , null , hasLighting );
				}
			}
		}

		/*
		 * Pop matrix stack, replace current matrix with one below it on the stack.
		 */
		glWrapper.glPopMatrix();
	}

	/**
	 * Draw the sides of the extruded shape.
	 *
	 * @param glWrapper     {@link GLWrapper} to use.
	 * @param shape         Base shape.
	 * @param extrusion     Extrusion vector (control-point displacement). This is a displacement relative to the shape being extruded.
	 */
	private static void drawExtrusionLines( final GLWrapper glWrapper , final Shape shape , final Vector3D extrusion )
	{
		glWrapper.glBegin( GL.GL_LINES );

		final double[] coords = new double[ 6 ];
		for ( final PathIterator pathIterator = shape.getPathIterator( null ) ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double x;
			final double y;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_CLOSE:
					continue;
				case PathIterator.SEG_LINETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_QUADTO: // reduce to line
					x = coords[ 2 ];
					y = coords[ 3 ];
					break;
				case PathIterator.SEG_CUBICTO: // reduce to line
					x = coords[ 4 ];
					y = coords[ 5 ];
					break;

				default:
					throw new AssertionError( "unknown type: " + type );
			}

			glWrapper.glVertex3d( x , y , 0.0 );
			glWrapper.glVertex3d( x + extrusion.x , y + extrusion.y , extrusion.z );
		}

		glWrapper.glEnd();
	}

	/**
	 * Draws the outline of the given shape.
	 *
	 * @param   glWrapper   GL wrapper.
	 * @param   shape       Shape to be drawn.
	 * @param   flatness    Flatness used to interpolate the shape's curves.
	 *
	 * @see     Shape#getPathIterator(AffineTransform, double)
	 */
	private static void drawShape( final GLWrapper glWrapper , final Shape shape , final double flatness )
	{
		glWrapper.glBegin( GL.GL_LINE_STRIP );

		boolean isFirst = true;
		double  startX  = 0.0;
		double  startY  = 0.0;

		final double[] coords = new double[ 6 ];
		for ( final PathIterator pathIterator = shape.getPathIterator( null , flatness ) ; !pathIterator.isDone() ; pathIterator.next() )
		{
			final double x;
			final double y;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
					if ( !isFirst )
					{
						/* Start a new line strip. */
						glWrapper.glEnd();
						glWrapper.glBegin( GL.GL_LINE_STRIP );
					}
					else
					{
						isFirst = false;
					}

					x = coords[ 0 ];
					y = coords[ 1 ];
					startX = x;
					startY = y;
					break;

				case PathIterator.SEG_CLOSE:
					x = startX;
					y = startY;
					break;
				case PathIterator.SEG_LINETO:
					x = coords[ 0 ];
					y = coords[ 1 ];
					break;
				case PathIterator.SEG_QUADTO: // reduce to line
					x = coords[ 2 ];
					y = coords[ 3 ];
					break;
				case PathIterator.SEG_CUBICTO: // reduce to line
					x = coords[ 4 ];
					y = coords[ 5 ];
					break;

				default:
					throw new AssertionError( "unknown type: " + type );
			}

			glWrapper.glVertex3d( x , y , 0.0 );
		}

		glWrapper.glEnd();
	}

	/**
	 * Draw a 3D grid centered around point x,y,z with size dx,dy.
	 *
	 * @param   glWrapper           GL context wrapper.
	 * @param   grid2world          Transforms grid to world coordinates.
	 * @param   cellCount           Number of cells in grid.
	 * @param   cellSize            Size of each cell.
	 * @param   hightlightAxes      If set, hightlight X=0 and Y=0 axes.
	 * @param   highlightInterval   Interval to use for highlighting grid lines.
	 */
	public static void drawGrid( final GLWrapper glWrapper , final Matrix3D grid2world , final int cellCount , final int cellSize , final boolean hightlightAxes , final int highlightInterval )
	{
		if ( ( grid2world != null ) && ( cellCount > 0 ) && ( cellSize > 0 ) ) // argument sanity
		{
			final GL gl = glWrapper.getGL();

			glWrapper.glPushMatrix();
			glWrapper.glMultMatrixd( grid2world );

			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			glWrapper.setLineSmooth( true );

			glWrapper.setLighting( false );

			final int limit = cellCount * cellSize;

			if ( hightlightAxes )
			{
				glWrapper.glLineWidth( 2.5f );
				glWrapper.glColor3f( 0.1f , 0.1f , 0.1f );
				gl.glBegin( GL.GL_LINES );

				gl.glVertex3i(      0 , -limit , 0 );
				gl.glVertex3i(      0 ,  limit , 0 );
				gl.glVertex3i( -limit ,      0 , 0 );
				gl.glVertex3i(  limit ,      0 , 0 );

				gl.glEnd();
			}

			if ( ( highlightInterval > 1 ) && ( highlightInterval <= cellCount ) )
			{
				glWrapper.glLineWidth( 1.5f );
				glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
				gl.glBegin( GL.GL_LINES );

				for ( int step = highlightInterval ; step <= cellCount ; step += highlightInterval )
				{
					final int position = step * cellSize;

					gl.glVertex3i( -position , -limit , 0 );
					gl.glVertex3i( -position ,  limit , 0 );
					gl.glVertex3i(  position , -limit , 0 );
					gl.glVertex3i(  position ,  limit , 0 );
					gl.glVertex3i( -limit , -position , 0 );
					gl.glVertex3i(  limit , -position , 0 );
					gl.glVertex3i( -limit ,  position , 0 );
					gl.glVertex3i(  limit ,  position , 0 );
				}

				gl.glEnd();
			}

			glWrapper.glLineWidth( 1.0f );
			glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );
			glWrapper.glBegin( GL.GL_LINES );

			for ( int step = hightlightAxes ? 1 : 0 ; step <= cellCount ; step++ )
			{
				final int position = step * cellSize;

				if ( ( highlightInterval < 2 ) || ( step % highlightInterval != 0 ) )
				{
					gl.glVertex3i( -position , -limit , 0 );
					gl.glVertex3i( -position ,  limit , 0 );
					gl.glVertex3i(  position , -limit , 0 );
					gl.glVertex3i(  position ,  limit , 0 );
					gl.glVertex3i( -limit , -position , 0 );
					gl.glVertex3i(  limit , -position , 0 );
					gl.glVertex3i( -limit ,  position , 0 );
					gl.glVertex3i(  limit ,  position , 0 );

				}
			}

			glWrapper.glEnd();

			glWrapper.setLighting( true );
			glWrapper.glPopMatrix();
		}
	}

	/**
	 * Draw 3D face on GL context.
	 *
	 * @param glWrapper     GLWrapper.
	 * @param face          {@link Face3D } drawn.
	 * @param useTexture    Whether to draw outline or not
	 * @param textureCache  Texture cache.
	 * @param hasLighting   Whether lighting is applied or not.
	 */
	private static void drawFace( final GLWrapper glWrapper , final Face3D face , final boolean useTexture , final Map<String, SoftReference<Texture>> textureCache , final boolean hasLighting )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Texture texture    = !useTexture ? null : getColorMapTexture( glWrapper.getGL() , face , textureCache );
			final boolean hasTexture = ( texture != null ); //if a texture has been loaded
			final float[] textureU   = hasTexture ? face.getTextureU() : null;
			final float[] textureV   = hasTexture ? face.getTextureV() : null;
			if ( hasTexture )
			{
				glWrapper.setTexture2D( true );
				glWrapper.glBindTexture( texture.getTarget(), texture.getTextureObject()) ;
			}

			switch ( vertexCount )
			{
				case 2:
				{
                glWrapper.glBegin( GL.GL_LINES );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
				glWrapper.glEnd(  );
				}
				break;

				case 3:
				{
				glWrapper.glBegin( GL.GL_TRIANGLES );
					setFaceVertex( glWrapper , face , textureU , textureV , 2 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
				glWrapper.glEnd(  );
				}
				break;

				case 4:
				{
				glWrapper.glBegin( GL.GL_QUADS );
					setFaceVertex( glWrapper , face , textureU , textureV , 3 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 2 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
				glWrapper.glEnd(  );
				}
				break;

				default:
				{
				glWrapper.glBegin( GL.GL_POLYGON );
					for ( int i = vertexCount ; --i >= 0 ; )
						setFaceVertex( glWrapper , face , textureU , textureV , i , hasLighting );
				glWrapper.glEnd(  );
				}
				break;
			}
			if ( hasTexture )
			{
				glWrapper.glDisable( texture.getTarget() );
				glWrapper.setTexture2D( false );
			}
		}
	}

	/**
	 * Set vertex properties using GL.
	 *
	 * @param   glWrapper           GLWrapper.
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   faceVertexIndex     Index of vertex in face.
	 * @param   hasLighting         Specifies whether lighting is used or not
	 */
	private static void setFaceVertex( final GLWrapper glWrapper , final Face3D face , final float[] textureU , final float[] textureV , final int faceVertexIndex , final boolean hasLighting )
	{
		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		if ( ( textureU != null ) && ( textureV != null ) )
		{
			glWrapper.glTexCoord2f( textureU[ faceVertexIndex ] , -textureV[ faceVertexIndex ] );
		}

		if ( face.isSmooth() && hasLighting)
		{
			final double[] vertexNormals = object.getVertexNormals();
			glWrapper.glNormal3d( vertexNormals[ vertexIndex ] , vertexNormals[ vertexIndex + 1 ] , vertexNormals[ vertexIndex + 2 ] );
		}
		else if ( hasLighting )
		{
			final Vector3D faceNormal = face.getNormal();
			glWrapper.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
		}

		final double[] vertexCoordinates = object.getVertexCoordinates();
		glWrapper.glVertex3d( vertexCoordinates[ vertexIndex ] , vertexCoordinates[ vertexIndex + 1 ] , vertexCoordinates[ vertexIndex + 2 ] );
	}

	/**
	 * Set GL material properties.
	 *
	 * @param glWrapper {@link GLWrapper}.
	 * @param material  {@link Material} properties.
	 */
	public static void setMaterial( final GLWrapper glWrapper , final Material material )
	{
		/* Set color, used when lights are disabled. */
		glWrapper.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );

		/* Set ambient color of material. */
		glWrapper.glMaterialfv( GL.GL_FRONT , GL.GL_AMBIENT , new float[] { material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue , 1.0f } , 0 );

		/* Set diffuse color and alpha of material. */
		glWrapper.glMaterialfv( GL.GL_FRONT , GL.GL_DIFFUSE , new float[] { material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha } , 0 );

		/* Set shininess and specular color of material. */
		glWrapper.glMaterialfv( GL.GL_FRONT , GL.GL_SPECULAR , new float[] { material.specularColorRed , material.specularColorGreen , material.specularColorBlue , 1.0f } , 0 );
		glWrapper.glMaterialf( GL.GL_FRONT  , GL.GL_SHININESS , (float)material.shininess );

		/* Set emissive color of material. */
		glWrapper.glMaterialfv( GL.GL_FRONT , GL.GL_EMISSION , new float[] { material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue , 1.0f } , 0 );
	}

	/**
	 * Get {@link Texture} for color map of {@link Face3D}.
	 *
	 * @param   gl              OpenGL context.
	 * @param   face            Face whose color map texture to return.
	 * @param   textureCache    Texture cache.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getColorMapTexture( final GL gl , final Face3D face , final Map<String,SoftReference<Texture>> textureCache  )
	{
		final Texture result;

		final Material material = face.getMaterial();

		if ( ( material != null ) && ( material.colorMap != null ) && ( face.getTextureU() != null ) && ( face .getTextureV() != null ) )
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
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   gl              OpenGL context.
	 * @param   material        {@link Material} to get texture for.
	 * @param   textureCache    Map containing the {@link SoftReference}s to the cached textures.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public static Texture getTexture( final GL gl , final Material material , final Map<String,SoftReference<Texture>> textureCache )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( material.colorMap ) )
		{
			SoftReference<Texture> reference = textureCache.get( material.colorMap );
			if ( reference != null )
					result = reference.get();
			if ( !textureCache.containsKey( material.colorMap ) )
			{
				if ( result == null )
				{
					final BufferedImage bufferedImage = material.getColorMapImage( false );
					if ( bufferedImage != null )
					{
						final boolean autoMipmapGeneration = ( ( gl.isExtensionAvailable( "GL_VERSION_1_4"          ) )
						                                    || ( gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" ) ) ); // @TODO Also check Power Of Two??

						System.out.println( "MipMap: " + ( autoMipmapGeneration ? "enabled" : "disabled" ) );

						result = TextureIO.newTexture( ( bufferedImage ) , autoMipmapGeneration );

						try
						{
							result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
							result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );
							result.setTexParameteri( GL.GL_TEXTURE_WRAP_R , GL.GL_REPEAT );
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
					reference = ( result != null ) ? new SoftReference<Texture>( result ) : null ;
					textureCache.put( material.colorMap , reference );
				}
			}
		}
		return result;
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
}