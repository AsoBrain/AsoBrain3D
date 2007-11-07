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
import java.util.HashMap;
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
	 * Texture map cache (maps map name to texture).
	 */
	private static final Map<String,SoftReference<Texture>> _textureCache = new HashMap<String, SoftReference<Texture>>();

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
		if ( fill )
		{
			for ( int i = 0; i < object3D.getFaceCount(); i++ )
			{
			final Face3D face = object3D.getFace( i );
			final Face3D previousFace = ( i > 0 ) ? object3D.getFace( i - 1 ) : null;
			final Face3D nextFace = ( ( i + 1 ) < object3D.getFaceCount() ) ? object3D.getFace( i + 1 ) : null;

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
					if( i > 0 && previousFace != null ) // i > 0 : Because first face does not have a previous face!
					{
						if ( face.getMaterial() != previousFace.getMaterial() )
						{
					setMaterial( gl , face.getMaterial() );
						}
					}
					else
					{
						setMaterial( gl , face.getMaterial() );
					}
					if ( face.isTwoSided() )
					{
						gl.glDisable( GL.GL_CULL_FACE );
					}
					else
					{
						gl.glEnable( GL.GL_CULL_FACE );
						gl.glCullFace( GL.GL_BACK );
					}
				}

				gl.glPolygonMode( GL.GL_FRONT_AND_BACK , GL.GL_FILL );
				drawFace( gl , face , false , previousFace , nextFace );
			}
		}

		if ( outline )
		{
			// set the blend mode
			gl.glBlendFunc ( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );

			// enable blending
			gl.glEnable ( GL.GL_BLEND );

			// set the line width
			gl.glLineWidth ( 1.0f );

			// change the depth mode
			gl.glDepthFunc ( GL.GL_LEQUAL );

			// enable backface culling
			gl.glEnable( GL.GL_CULL_FACE );
			gl.glCullFace( GL.GL_BACK );

			// set polygonmode to lines only
			gl.glPolygonMode( GL.GL_FRONT , GL.GL_LINE );

			// smooth lines
			gl.glEnable( GL.GL_LINE_SMOOTH );

			// disable lighting
			gl.glDisable( GL.GL_LIGHTING );

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

			final float[] argb = color.getRGBComponents( null );
			gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );

			// draw all faces.
			for ( int i = 0; i < object3D.getFaceCount(); i++ )
			{
				final Face3D face = object3D.getFace( i );
				drawFace( gl , face, outline , null , null );
			}

			// enable lighting
			gl.glEnable( GL.GL_LIGHTING );

			// disable backface culling
			gl.glDisable( GL.GL_CULL_FACE );

			// change the depth mode back
			gl.glDepthFunc( GL.GL_LESS );

			// disable blending
			gl.glDisable( GL.GL_BLEND );
		}

		/*
		 * Pop matrix stack, replace current matrix with one below it on the stack.
		 */
		gl.glPopMatrix();
	}

	/**
	 * Draw a 3D grid centered around point x,y,z with size dx,dy.
	 *
	 * @param gl        {@link GL}  context.
	 * @param x         X position of the grid.
	 * @param y         Y position of the grid.
	 * @param z         Z position of the grid.
	 * @param dx        Width of the grid.
	 * @param dy        Height of the grid.
	 * @param spacing   Spacing between the grid lines.
	 */
	public static void drawGrid( final GL gl , final int x , final int y , final int z , final int dx , final int dy , final int spacing )
	{
		if( spacing != 0 ) //check if spacing is not null else we'll get an infinite loop.
		{
			// set the anti-aliasing options
			gl.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			gl.glEnable( GL.GL_BLEND );
			gl.glEnable( GL.GL_LINE_SMOOTH );

			//set line width to 1.0f, the smallest possible line in opengl
			gl.glLineWidth( 1.0f );
			gl.glDisable( GL.GL_LIGHTING );
			gl.glBegin( GL.GL_LINES );

			for( int i = -dx / 2 + x ; i <= dx / 2 + x ; i += spacing )
			{
				if ( ( ( i - dx ) / spacing ) % 10 == 0 )
					gl.glColor3f( 0.5f , 0.5f , 0.5f );
				else
					gl.glColor3f( 0.75f , 0.75f , 0.75f );

				gl.glVertex3i( i , -dy / 2 + y , z );
				gl.glVertex3i( i ,  dy / 2 + y , z );
			}

			for( int i = -dy / 2 + y ; i <= dy / 2 + y ; i += spacing )
			{
				if ( ( ( i - dy ) / spacing ) % 10 == 0 )
					gl.glColor3f( 0.5f , 0.5f , 0.5f );
				else
					gl.glColor3f( 0.75f , 0.75f , 0.75f );

				gl.glVertex3i( -dx / 2 + x , i , z );
				gl.glVertex3i(  dx / 2 + x , i , z );
			}

			gl.glEnd();
			gl.glDisable( GL.GL_BLEND );
		}
	}

	/**
	 * Draw 3D face on GL context.
	 *
	 * @param gl            {@link GL}  context.
	 * @param face          {@link Face3D } drawn.
	 * @param outline       Whether to draw outline or not
	 * @param previousFace  Previous face.
	 * @param nextFace      Next face to draw.
	 */
	private static void drawFace( final GL gl , final Face3D face , final boolean outline , final Face3D previousFace , final Face3D nextFace )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Texture texture    = outline ? null : getColorMapTexture( face );
			final boolean hasTexture = ( texture != null );
			final float[] textureU   = hasTexture ? face.getTextureU() : null;
			final float[] textureV   = hasTexture ? face.getTextureV() : null;
			// If face has texture, previous face is null or has a different texture: Enable Texturing & Bind texture.
			if ( hasTexture && ( previousFace == null || (face.getMaterial().colorMap != previousFace.getMaterial().colorMap ) ) )
			{
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glBindTexture(texture.getTarget(), texture.getTextureObject());
			}

			switch ( vertexCount )
			{
				case 2:
				{
				gl.glBegin( GL.GL_LINES );
					setFaceVertex( gl , face , textureU , textureV , 0 );
					setFaceVertex( gl , face , textureU , textureV , 1 );
					gl.glEnd();
				}
				break;

				case 3:
				{
					gl.glBegin( GL.GL_TRIANGLES );
					setFaceVertex( gl , face , textureU , textureV , 2 );
					setFaceVertex( gl , face , textureU , textureV , 1 );
					setFaceVertex( gl , face , textureU , textureV , 0 );
					gl.glEnd();
				}
				break;

				case 4:
				{
					gl.glBegin( GL.GL_QUADS );
					setFaceVertex( gl , face , textureU , textureV , 3 );
					setFaceVertex( gl , face , textureU , textureV , 2 );
					setFaceVertex( gl , face , textureU , textureV , 1 );
					setFaceVertex( gl , face , textureU , textureV , 0 );
					gl.glEnd();
				}
				break;

				default:
				{
					gl.glBegin( GL.GL_POLYGON );

					for ( int i = vertexCount ; --i >= 0 ; )
						setFaceVertex( gl , face , textureU , textureV , i );

					gl.glEnd();
				}
				break;
			}
			// If current face has texture, and next face is not null and does not have the same texture OR next face is null: Disable texture. ("unbind")
			if ( ( hasTexture ) && ( ( nextFace != null && ( ( face.getMaterial().colorMap != nextFace.getMaterial().colorMap ) ) ) || nextFace == null ) )
				gl.glDisable( texture.getTarget() );
			// If current face has texture, and next face is not null and does not have a texture OR next face is null: Disable texturing!
			if ( ( hasTexture ) && ( ( nextFace != null && TextTools.isEmpty( nextFace.getMaterial().colorMap ) )                   || nextFace == null ) )
				gl.glDisable( GL.GL_TEXTURE_2D );
		}
	}

	/**
	 * Set vertex properties using GL.
	 *
	 * @param   gl                  GL context.
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   faceVertexIndex     Index of vertex in face.
	 */
	private static void setFaceVertex( final GL gl , final Face3D face , final float[] textureU , final float[] textureV , final int faceVertexIndex )
	{
		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		if ( ( textureU != null ) && ( textureV != null ) )
		{
			gl.glTexCoord2f( textureU[ faceVertexIndex ] , textureV[ faceVertexIndex ] );
		}

		if ( face.isSmooth() )
		{
			final double[] vertexNormals = object.getVertexNormals();
			gl.glNormal3d( vertexNormals[ vertexIndex ] , vertexNormals[ vertexIndex + 1 ] , vertexNormals[ vertexIndex + 2 ] );
		}
		else
		{
			final Vector3D faceNormal = face.getNormal();
			gl.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
		}

		final double[] vertexCoordinates = object.getVertexCoordinates();
		gl.glVertex3d( vertexCoordinates[ vertexIndex ] , vertexCoordinates[ vertexIndex + 1 ] , vertexCoordinates[ vertexIndex + 2 ] );
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
	 * gl.glEnable( GL.GL_COLORMATERIAL ) and
	 * gl.glDisable( GL.GL_COLORMATERIAL ) after each block is necessary because
	 * only stating it at the begin and at the end gives problems (all objects
	 * are white) with certain OpenGL implementations.
	 *
	 * @param gl       {@link GL} context.
	 * @param material {@link Material} properties.
	 */
	public static void setMaterial( final GL gl , final Material material )
	{
		/* Set color, used when lights are disabled. */
		gl.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );

		/* Set shininess and specular color of material. */
		gl.glMaterialf( GL.GL_FRONT_AND_BACK , GL.GL_SHININESS , (float)material.shininess );
		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_SPECULAR );
		gl.glColor3f( material.specularColorRed , material.specularColorGreen , material.specularColorBlue );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		/* Set ambient color of material. */
		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_AMBIENT );
		gl.glColor3f( material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		/* Set diffuse color and alpha of material. */
		gl.glColorMaterial( GL.GL_FRONT_AND_BACK , GL.GL_DIFFUSE );
		gl.glColor4f( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );
		gl.glEnable( GL.GL_COLOR_MATERIAL );
		gl.glDisable( GL.GL_COLOR_MATERIAL );

		/* Set emissive color of material. */
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

		if ( ( material != null ) && ( material.colorMap != null ) && ( face.getTextureU() != null ) && ( face .getTextureV() != null ) )
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
			final Map<String,SoftReference<Texture>> cache = _textureCache;
			SoftReference<Texture> reference = cache.get( name );
			if ( reference != null )
			{
				result = reference.get();
			}
			if ( ( result == null ) && ( ( reference == null ) || !cache.containsKey( name ) ) )
			{
				final BufferedImage bufferedImage = MapTools.loadImage( name );
				if ( bufferedImage != null )
				{
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
				cache.put( name , reference );
			}
		}
		return result;
	}
}