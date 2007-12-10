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
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Map;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

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
	 * @param useTextures       Use textures on this object3D.
	 * @param useAlternate      Use alternative color to paint this object3D.
	 * @param hasLighting       Specify material lighting properties.
	 * @param textureCache      Texture cache.
	 * @param fill              Fill polygons.
	 * @param materialOverride  Material to use instead of actual materials.
	 */
	public static void paintObject3D( final GLWrapper glWrapper , final Object3D object3D , final Matrix3D node2gl , final boolean useTextures , final boolean useAlternate , final boolean hasLighting , final Map<String, SoftReference<Texture>> textureCache , final boolean fill , final Material materialOverride )
	{
		/*
		 * Push the current matrix stack down by one
		 */
		glWrapper.glPushMatrix();

		/*
		 * Set the object transform
		 */
		glWrapper.glMultMatrixd( node2gl );

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
			final float[] argb;
			if( alternatePaint instanceof Color )
			{
				argb = ((Color)alternatePaint).getRGBComponents( null );
			}
			else
			{
				System.out.println( "AlternatePaint is not a Color" );
				argb = Color.BLUE.getRGBComponents( null );
			}

			glWrapper.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

			if( hasLighting )
			{
				material = new Material( ((Color)alternatePaint).getRGB() );
			}
		}
		else if( !useTextures && material == null )
		{
			final float[] argb;

			if( paint instanceof Color )
			{
				argb = ((Color)paint).getRGBComponents( null );
			}
			else
			{
				System.out.println( "Paint is not a Color" );
				argb = Color.RED.getRGBComponents( null );
			}

			glWrapper.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
			if( hasLighting )
			{
				material = new Material( ((Color)paint).getRGB() );
			}
		}
		else if( materialOverride != null )
		{
			glWrapper.glColor4f( materialOverride.diffuseColorRed , materialOverride.diffuseColorGreen , materialOverride.diffuseColorBlue , materialOverride.diffuseColorAlpha );
		}

		for ( int i = 0 ; i < object3D.getFaceCount() ; i++ )
		{
			final Face3D face = object3D.getFace( i );

			if( face.isTwoSided() )
			{
				glWrapper.setCullFace( false );
			}
			else
			{
				glWrapper.setCullFace( true );
				glWrapper.glCullFace( GL.GL_BACK );
			}
			if( hasLighting )
			{
				if( material != null )
				{
					setMaterial( glWrapper , material );
				}
				else if( face.getMaterial() != null )
				{
					setMaterial( glWrapper , face.getMaterial() );
				}
			}

			if( materialOverride != null )
			{
				final Material temp = face.getMaterial();
				face.setMaterial( materialOverride );
				if( face.getMaterial().colorMap.isEmpty() )
					drawFace( glWrapper , face , false , null , hasLighting );
				else
					drawFace( glWrapper , face , true , textureCache , hasLighting  );
				face.setMaterial( temp );
			}
			else if( face.getMaterial() != null && !useAlternate && useTextures)
				drawFace( glWrapper , face , true , textureCache , hasLighting );
			else if( face.getMaterial() != null )
				drawFace( glWrapper , face , false , null , hasLighting );
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
			final GL gl = glWrapper.getGL();

			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			glWrapper.setLineSmooth( true );

			glWrapper.glLineWidth( 1.0f );
			glWrapper.setLighting( false );
			glWrapper.glBegin( GL.GL_LINES );

			for( int i = -dx / 2 + x ; i <= dx / 2 + x ; i += spacing )
			{
				if ( ( ( i - dx ) / spacing ) % 10 == 0 )
				{
					gl.glEnd();
					glWrapper.glLineWidth( 2.0f );
					gl.glBegin( GL.GL_LINES );
										glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
					gl.glVertex3i( i , -dy / 2 + y , z );
					gl.glVertex3i( i ,  dy / 2 + y , z );
					gl.glEnd();
					glWrapper.glLineWidth( 1.0f );
					gl.glBegin( GL.GL_LINES );
				}
				else
				{
					glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );
					gl.glVertex3i( i , -dy / 2 + y , z );
					gl.glVertex3i( i ,  dy / 2 + y , z );
				}
			}

			for( int i = -dy / 2 + y ; i <= dy / 2 + y ; i += spacing )
			{
				if ( ( ( i - dy ) / spacing ) % 10 == 0 )
				{
					gl.glEnd();
					glWrapper.glLineWidth( 2.0f );

					gl.glBegin( GL.GL_LINES );
										glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
					gl.glVertex3i( -dx / 2 + x , i , z );
					gl.glVertex3i(  dx / 2 + x , i , z );
					gl.glEnd();
					glWrapper.glLineWidth( 1.0f );
					gl.glBegin( GL.GL_LINES );
				}
				else
				{
					gl.glVertex3i( -dx / 2 + x , i , z );
					gl.glVertex3i(  dx / 2 + x , i , z );
					glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );
				}
			}

			glWrapper.glEnd();
			glWrapper.setLighting( true );
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
	private static void drawFace( final GLWrapper glWrapper , final Face3D face , final boolean useTexture, final Map<String, SoftReference<Texture>> textureCache , final boolean hasLighting )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Texture texture    = !useTexture ? null : getColorMapTexture( face , textureCache);
			final boolean hasTexture = ( texture != null ); //if a texture has been loaded
			final float[] textureU   = hasTexture ? face.getTextureU() : null;
			final float[] textureV   = hasTexture ? face.getTextureV() : null;
			if( hasTexture )
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
			if( hasTexture )
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
		else if( hasLighting )
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
			result = getTexture( material , textureCache );
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
	 * @param   material      Material to get texture for.
	 * @param   textureCache  Texturecache
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public static Texture getTexture( final Material material , final Map<String,SoftReference<Texture>> textureCache  )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( material.colorMap ) )
		{
			SoftReference<Texture> reference = textureCache.get( material.colorMap );
			if ( reference != null )
			{
				result = reference.get();
			}
			if ( ( result == null ) || ( !textureCache.containsKey( material.colorMap ) ) )
			{
				final BufferedImage bufferedImage = material.getColorMapImage( false );
				if ( bufferedImage != null )
				{
					System.out.println( "Loading Texture: " + material.colorMap );
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
				textureCache.put( material.colorMap , reference );
			}
		}
		return result;
	}
}