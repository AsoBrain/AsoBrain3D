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
import java.lang.ref.SoftReference;
import java.util.Map;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
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
	 * @param fill              Fill polygons.
	 * @param fillColor         Override color to use for filling.
	 * @param outline           Draw polygon outlines.
	 * @param outlineColor      Override color to use for outlines.
	 * @param useAlternative    Use alternative vs. regular object colors.
	 * @param textureCache      Texture cache.
	 */
	public static void paintObject3D( final GLWrapper glWrapper , final Object3D object3D , final Matrix3D node2gl , final boolean fill , final Color fillColor , final boolean outline , final Color outlineColor , final boolean useAlternative , final Map<String,SoftReference<Texture>> textureCache )
	{
		/*
		 * Push the current matrix stack down by one
		 */
		glWrapper.glPushMatrix();

		/*
		 * Set the object transform
		 */
		glMultMatrixd( glWrapper , node2gl );

		/*
		 * Draw the object3d.
		 */
		if ( fill )
		{
			glWrapper.setPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL );
			for ( int i = 0; i < object3D.getFaceCount(); i++ )
			{
			final Face3D face = object3D.getFace( i );
				if ( fillColor != null )
				{
					final float[] argb = fillColor.getRGBComponents( null );
					glWrapper.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
					glWrapper.setLightning( false );
				}
				else
				{
					glWrapper.setLightning( true );
					glWrapper.setShadeModel( ( face.isSmooth() ? GL.GL_SMOOTH : GL.GL_FLAT ) );
					setMaterial( glWrapper , face.getMaterial() );
					if ( face.isTwoSided() )
					{
						glWrapper.setCullFace( false );
					}
					else
					{
						glWrapper.setCullFace( true );
						glWrapper.setCullFaceMode( GL.GL_BACK );
					}
				}
				drawFace( glWrapper , face , false , textureCache );
			}

		}

		if ( outline )
		{
			// set the blend mode
			glWrapper.setBlendFunc(GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );

			// enable blending
			glWrapper.setBlend( true );

			// set the line width
			glWrapper.glLineWidth ( 1.0f );

			// change the depth mode
			glWrapper.glDepthFunc ( GL.GL_LEQUAL );

			// enable backface culling
			glWrapper.setCullFace( true );
			glWrapper.setCullFaceMode( GL.GL_BACK );

			// set polygonmode to lines only
			glWrapper.setPolygonMode( GL.GL_FRONT , GL.GL_LINE );

			// smooth lines
			glWrapper.setSmooth( true );

			// disable lighting
			glWrapper.setLightning( false );

			// set color
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

			final float[] rgba = color.getRGBComponents( null );
			glWrapper.glColor4f( rgba );

			// draw all faces.
			for ( int i = 0; i < object3D.getFaceCount(); i++ )
			{
				final Face3D face = object3D.getFace( i );
				drawFace( glWrapper , face, outline , textureCache);
			}

			// enable lighting
			glWrapper.setLightning( true );

			// disable backface culling
			glWrapper.setCullFace( false );

			// change the depth mode back
			glWrapper.glDepthFunc( GL.GL_LESS );

			// disable blending
			glWrapper.setBlend( false );
		}

		/*
		 * Pop matrix stack, replace current matrix with one below it on the stack.
		 */
		glWrapper.glPopMatrix();
	}

	/**
	 * Draw a 3D grid centered around point x,y,z with size dx,dy.
	 *
	 * @param glWrapper GLWrapper.
	 * @param x         X position of the grid.
	 * @param y         Y position of the grid.
	 * @param z         Z position of the grid.
	 * @param dx        Width of the grid.
	 * @param dy        Height of the grid.
	 * @param spacing   Spacing between the grid lines.
	 */
	public static void drawGrid( final GLWrapper glWrapper , final int x , final int y , final int z , final int dx , final int dy , final int spacing )
	{
		if( spacing != 0 ) //check if spacing is not null else we'll get an infinite loop.
		{
			final GL gl = glWrapper.getgl();
			// set the anti-aliasing options
			glWrapper.setBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			glWrapper.setSmooth( true );

			//set line width to 1.0f, the smallest possible line in opengl
			glWrapper.glLineWidth( 1.0f );
			glWrapper.setLightning( false );
			glWrapper.glBegin( GL.GL_LINES );

			for( int i = -dx / 2 + x ; i <= dx / 2 + x ; i += spacing )
			{
				if ( ( ( i - dx ) / spacing ) % 10 == 0 )
					glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
				else
					glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );


				gl.glVertex3i( i , -dy / 2 + y , z );
				gl.glVertex3i( i ,  dy / 2 + y , z );
			}

			for( int i = -dy / 2 + y ; i <= dy / 2 + y ; i += spacing )
			{
				if ( ( ( i - dy ) / spacing ) % 10 == 0 )
					glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
				else
					glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );

				gl.glVertex3i( -dx / 2 + x , i , z );
				gl.glVertex3i(  dx / 2 + x , i , z );
			}

			glWrapper.glEnd();
			glWrapper.setBlend( false );
		}
	}

	/**
	 * Draw 3D face on GL context.
	 *
	 * @param glWrapper     GLWrapper.
	 * @param face          {@link Face3D } drawn.
	 * @param outline       Whether to draw outline or not
	 * @param textureCache  Texture cache.
	 */
	private static void drawFace( final GLWrapper glWrapper , final Face3D face , final boolean outline, final Map<String, SoftReference<Texture>> textureCache  )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Texture texture    = outline ? null : getColorMapTexture( face , textureCache);
			final boolean hasTexture = ( texture != null );
			final float[] textureU   = hasTexture ? face.getTextureU() : null;
			final float[] textureV   = hasTexture ? face.getTextureV() : null;
			if( hasTexture )
			{
				glWrapper.setTexture2D( true );
				glWrapper.setBindTexture( texture.getTarget(), texture.getTextureObject()) ;
			}

			switch ( vertexCount )
			{
				case 2:
				{
				glWrapper.glBegin( GL.GL_LINES );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 );
				glWrapper.glEnd(  );
				}
				break;

				case 3:
				{
				glWrapper.glBegin( GL.GL_TRIANGLES );
					setFaceVertex( glWrapper , face , textureU , textureV , 2 );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 );
				glWrapper.glEnd(  );
				}
				break;

				case 4:
				{
				glWrapper.glBegin( GL.GL_QUADS );
					setFaceVertex( glWrapper , face , textureU , textureV , 3 );
					setFaceVertex( glWrapper , face , textureU , textureV , 2 );
					setFaceVertex( glWrapper , face , textureU , textureV , 1 );
					setFaceVertex( glWrapper , face , textureU , textureV , 0 );
				glWrapper.glEnd(  );
				}
				break;

				default:
				{
				glWrapper.glBegin( GL.GL_POLYGON );
					for ( int i = vertexCount ; --i >= 0 ; )
						setFaceVertex( glWrapper , face , textureU , textureV , i );
				glWrapper.glEnd(  );
				}
				break;
			}
			if( hasTexture )
			{
				glWrapper.setDisableTexture( texture.getTarget() );
				glWrapper.setTexture2D( false );
			}
		}
	}

	/**
	 * Set vertex properties using GL.
	 *
	 * @param glWrapper             GLWrapper.
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   faceVertexIndex     Index of vertex in face.
	 */
	private static void setFaceVertex( final GLWrapper glWrapper , final Face3D face , final float[] textureU , final float[] textureV , final int faceVertexIndex )
	{
		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		if ( ( textureU != null ) && ( textureV != null ) )
		{
			glWrapper.glTexCoord2f( textureU[ faceVertexIndex ] , textureV[ faceVertexIndex ] );
		}

		if ( face.isSmooth() )
		{
			final double[] vertexNormals = object.getVertexNormals();
			glWrapper.glNormal3d( vertexNormals[ vertexIndex ] , vertexNormals[ vertexIndex + 1 ] , vertexNormals[ vertexIndex + 2 ] );
		}
		else
		{
			final Vector3D faceNormal = face.getNormal();
			glWrapper.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
		}

		final double[] vertexCoordinates = object.getVertexCoordinates();
		glWrapper.glVertex3d( vertexCoordinates[ vertexIndex ] , vertexCoordinates[ vertexIndex + 1 ] , vertexCoordinates[ vertexIndex + 2 ] );
	}


	/**
	 * Clear GL canvas.
	 *
	 * @param glWrapper GLWrapper.
	 * @param color     Color to clear canvas with.
	 */
	public static void glClearColor( final GLWrapper glWrapper , final Color color )
	{
		final float[] argb = new float[4];
		color.getRGBComponents( argb );
		glWrapper.glClearColor( argb[0] , argb[1] , argb[2] , argb[3] );
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param glWrapper GLWrapper.
	 * @param transform Transformation to multiply with.
	 */
	public static void glMultMatrixd( final GLWrapper glWrapper , final Matrix3D transform )
	{
		glWrapper.glMultMatrixd( new double[]
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
	 * gl.glEnable( GL.GL_COLORMATERIAL ) and
	 * gl.glDisable( GL.GL_COLORMATERIAL ) after each block is necessary because
	 * only stating it at the begin and at the end gives problems (all objects
	 * are white) with certain OpenGL implementations.
	 *
	 * @param glWrapper GLWrapper.
	 * @param material  {@link Material} properties.
	 */
	public static void setMaterial( final GLWrapper glWrapper , final Material material )
	{
		/* Set color, used when lights are disabled. */
		glWrapper.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );

		/* Set shininess and specular color of material. */
		glWrapper.glMaterialf( GL.GL_FRONT_AND_BACK , GL.GL_SHININESS , (float)material.shininess );
		glWrapper.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_SPECULAR );
		glWrapper.glColor3f( material.specularColorRed , material.specularColorGreen , material.specularColorBlue );
		glWrapper.setColorMaterial( true );
		glWrapper.setColorMaterial( false );

		/* Set ambient color of material. */
		glWrapper.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_AMBIENT );
		glWrapper.glColor3f( material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue );
		glWrapper.setColorMaterial( true );
		glWrapper.setColorMaterial( false );

		/* Set diffuse color and alpha of material. */
		glWrapper.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_DIFFUSE );
		glWrapper.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );
		glWrapper.setColorMaterial( true );
		glWrapper.setColorMaterial( false );

		/* Set emissive color of material. */
		glWrapper.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_EMISSION );
		glWrapper.glColor3f( material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue );
		glWrapper.setColorMaterial( true );
		glWrapper.setColorMaterial( false );
	}

	/**
	 * Get {@link Texture} for color map of {@link Face3D}.
	 *
	 * @param face          Face whose color map texture to return.
	 * @param textureCache  Texture cache.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getColorMapTexture( final Face3D face, final Map<String, SoftReference<Texture>> textureCache  )
	{
		final Texture result;

		final Material material = face.getMaterial();

		if ( ( material != null ) && ( material.colorMap != null ) && ( face.getTextureU() != null ) && ( face .getTextureV() != null ) )
		{
			result = getTexture( material.colorMap , textureCache );
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
	 * @param name          Name of texture map to get.
	 * @param textureCache  Texturecache
	 *
	 * @return Texture for the specified name; <code>null</code> if the name was
	 *         empty or no map by the given name was found.
	 */

	public static Texture getTexture( final String name, final Map<String, SoftReference<Texture>> textureCache  )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( name ) )
		{
			SoftReference<Texture> reference = textureCache.get( name );
			if ( reference != null )
			{
				result = reference.get();
			}
			if ( ( result == null ) || ( !textureCache.containsKey( name ) ) )
			{
				final BufferedImage bufferedImage = MapTools.loadImage( name );
				if ( bufferedImage != null )
				{
					System.out.println( "Loading Texture: " + name );
					result = TextureIO.newTexture( ( bufferedImage ) , true );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_R , GL.GL_REPEAT );
					result.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

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
				else
				{
					System.out.println( "Could not load texture." );
				}
				reference = ( result != null ) ? new SoftReference<Texture>( result ) : new SoftReference<Texture> ( null );
				textureCache.put( name , reference );
			}
		}
		return result;
	}
}