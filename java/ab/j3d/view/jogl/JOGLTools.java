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
import java.awt.Rectangle;
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

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

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
	private static final String NORMALIZATION_CUBE_MAP = "__normalizationCubeMap";

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JOGLTools()
	{
	}

	/**
	 * Paint 3D object on GL context.
	 *
	 * @param   glWrapper           GLWrapper.
	 * @param   object3D            Object to draw.
	 * @param   node2gl             Transformation from node to current GL transform.
	 * @param   useTextures         Use textures on this object3D.
	 * @param   useBump             Whether to use bump mapping or not.
	 * @param   useAlternate        Use alternative color to paint this object3D.
	 * @param   hasLighting         Specify material lighting properties.
	 * @param   textureCache        Texture cache.
	 * @param   fill                Fill polygons.
	 * @param   materialOverride    Material to use instead of actual materials.
	 */
	public static void paintObject3D( final GLWrapper glWrapper , final Object3D object3D , final Matrix3D node2gl , final boolean useTextures , final boolean useBump , final boolean useAlternate , final boolean hasLighting , final Map<String,SoftReference<Texture>> textureCache , final boolean fill , final Material materialOverride )
	{
		paintObject3D( glWrapper , object3D , node2gl , useTextures , useBump , useAlternate , hasLighting ? Vector3D.INIT : null , textureCache , fill , materialOverride );
	}

	/**
	 * Paint 3D object on GL context.
	 *
	 * @param   glWrapper           GLWrapper.
	 * @param   object3D            Object to draw.
	 * @param   node2gl             Transformation from node to current GL transform.
	 * @param   useTextures         Use textures on this object3D.
	 * @param   useBump             Whether to use bump mapping or not.
	 * @param   useAlternate        Use alternative color to paint this object3D.
	 * @param   lightPosition       Position of the dominant light source, if any.
	 * @param   textureCache        Texture cache.
	 * @param   fill                Fill polygons.
	 * @param   materialOverride    Material to use instead of actual materials.
	 */
	public static void paintObject3D( final GLWrapper glWrapper , final Object3D object3D , final Matrix3D node2gl , final boolean useTextures , final boolean useBump , final boolean useAlternate , final Vector3D lightPosition , final Map<String,SoftReference<Texture>> textureCache , final boolean fill , final Material materialOverride )
	{
		final boolean hasLighting = ( lightPosition != null );

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
						drawFace( glWrapper , face , false , false , null , lightPosition );
					}
					else
					{
						drawFace( glWrapper , face , true , useBump , textureCache , lightPosition );
					}
					face.setMaterial( temp );
				}
				else if ( face.getMaterial() != null && !useAlternate && useTextures )
				{
					drawFace( glWrapper , face , true , useBump , textureCache , lightPosition );
				}
				else if ( face.getMaterial() != null )
				{
					drawFace( glWrapper , face , false , false , null , lightPosition );
				}
				else if ( !useTextures && face.getMaterial() == null )
				{
					drawFace( glWrapper , face , false , false , null , null );
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
	 * @see     Shape#getPathIterator(AffineTransform,double)
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
	 * @param   gridBounds          Bounds of grid.
	 * @param   cellSize            Size of each cell.
	 * @param   hightlightAxes      If set, hightlight X=0 and Y=0 axes.
	 * @param   highlightInterval   Interval to use for highlighting grid lines.
	 */
	public static void drawGrid( final GLWrapper glWrapper , final Matrix3D grid2world , final Rectangle gridBounds , final int cellSize , final boolean hightlightAxes , final int highlightInterval )
	{
		if ( ( grid2world != null ) && ( gridBounds != null ) && ( gridBounds.width >= 0 ) && ( gridBounds.height >= 0 ) && ( cellSize >= 0 ) ) // argument sanity
		{
			final GL gl = glWrapper.getGL();

			glWrapper.glPushMatrix();
			glWrapper.glMultMatrixd( grid2world );

			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			glWrapper.setLineSmooth( true );

			glWrapper.setLighting( false );

			final int minCellX = gridBounds.x;
			final int maxCellX = minCellX + gridBounds.width;
			final int minCellY = gridBounds.y;
			final int maxCellY = minCellY + gridBounds.height;

			final int minX = minCellX * cellSize;
			final int maxX = maxCellX * cellSize;
			final int minY = minCellY * cellSize;
			final int maxY = maxCellY * cellSize;

			if ( hightlightAxes )
			{
				final boolean hasXaxis = ( minCellY <= 0 ) && ( maxCellY >= 0 );
				final boolean hasYaxis = ( minCellX <= 0 ) && ( maxCellX >= 0 );

				if ( ( hasXaxis || hasYaxis ) )
				{
					glWrapper.glLineWidth( 2.5f );
					glWrapper.glColor3f( 0.1f , 0.1f , 0.1f );
					gl.glBegin( GL.GL_LINES );

					if ( hasXaxis )
					{
						gl.glVertex3i( minX , 0 , 0 );
						gl.glVertex3i( maxX , 0 , 0 );
					}

					if ( hasYaxis )
					{
						gl.glVertex3i( 0 , minY , 0 );
						gl.glVertex3i( 0 , maxY , 0 );
					}

					gl.glEnd();
				}
			}

			if ( highlightInterval > 1 )
			{
				final int highlightMinX = minCellX - minCellX % highlightInterval;
				final int highLightMaxX = maxCellX - maxCellX % highlightInterval;
				final int highlightMinY = minCellX - minCellX % highlightInterval;
				final int highLightMaxY = maxCellX - maxCellX % highlightInterval;

				final boolean hasHighlightX = ( highLightMaxX >= highlightMinX ) && ( !hightlightAxes || ( highlightMinX < 0 ) || ( highLightMaxX > 0 ) );
				final boolean hasHighlightY = ( highLightMaxY >= highlightMinY ) && ( !hightlightAxes || ( highlightMinY < 0 ) || ( highLightMaxY > 0 ) );

				if ( hasHighlightX || hasHighlightY )
				{
					glWrapper.glLineWidth( 1.5f );
					glWrapper.glColor3f( 0.5f , 0.5f , 0.5f );
					gl.glBegin( GL.GL_LINES );

					for ( int x = highlightMinX ; x <= highLightMaxX ; x += highlightInterval )
					{
						if ( !hightlightAxes || ( x != 0 ) )
						{
							gl.glVertex3i( x * cellSize , minY , 0 );
							gl.glVertex3i( x * cellSize , maxY , 0 );
						}
					}

					for ( int y = highlightMinY ; y <= highLightMaxY ; y += highlightInterval )
					{
						if ( !hightlightAxes || ( y != 0 ) )
						{
							gl.glVertex3i( minX , y * cellSize , 0 );
							gl.glVertex3i( maxX , y * cellSize , 0 );
						}
					}

					gl.glEnd();
				}
			}
//
			glWrapper.glLineWidth( 1.0f );
			glWrapper.glColor3f( 0.75f , 0.75f , 0.75f );
			glWrapper.glBegin( GL.GL_LINES );

			for ( int x = minCellX ; x <= maxCellX ; x++ )
			{
				if ( ( !hightlightAxes || ( x != 0 ) ) && ( ( highlightInterval <= 1 ) || ( x % highlightInterval != 0 ) ) )
				{
					gl.glVertex3i( x * cellSize , minY , 0 );
					gl.glVertex3i( x * cellSize , maxY , 0 );
				}
			}

			for ( int y = minCellY ; y <= maxCellY ; y++ )
			{
				if ( ( !hightlightAxes || ( y != 0 ) ) && ( ( highlightInterval <= 1 ) || ( y % highlightInterval != 0 ) ) )
				{
					gl.glVertex3i( minX , y * cellSize , 0 );
					gl.glVertex3i( maxX , y * cellSize , 0 );
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
	 * @param   glWrapper       GLWrapper.
	 * @param   face            {@link Face3D } drawn.
	 * @param   useTexture      Whether to draw outline or not.
	 * @param   useBump         Whether to use bump mapping or not.
	 * @param   textureCache    Texture cache.
	 * @param   lightPosition   Whether lighting is applied or not.
	 */
	private static void drawFace( final GLWrapper glWrapper , final Face3D face , final boolean useTexture , final boolean useBump , final Map<String,SoftReference<Texture>> textureCache , final Vector3D lightPosition )
	{
		final boolean hasLighting = ( lightPosition != null );

		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final GL gl = glWrapper.getGL();

			final Texture texture    = !useTexture ? null : getColorMapTexture( gl , face , textureCache );
			final Texture bumpMap    = !useBump    ? null : getBumpMapTexture ( gl , face , textureCache );
			final boolean hasTexture = ( texture != null );
			final boolean hasBump    = ( bumpMap != null );
			final float[] textureU   = ( hasTexture || hasBump ) ? face.getTextureU() : null;
			final float[] textureV   = ( hasTexture || hasBump ) ? face.getTextureV() : null;

			if ( hasBump )
			{
				final SoftReference<Texture> normalizationCubeMapReference = textureCache.get( NORMALIZATION_CUBE_MAP );
				Texture normalizationCubeMap = ( normalizationCubeMapReference == null ) ? null : normalizationCubeMapReference.get();
				if ( normalizationCubeMap == null )
				{
					normalizationCubeMap = createNormalizationCubeMap( gl );
					textureCache.put( NORMALIZATION_CUBE_MAP , new SoftReference<Texture>( normalizationCubeMap ) );
				}

				/*
				 * Set The First Texture Unit To Normalize Our Vector From The
				 * Surface To The Light. Set The Texture Environment Of The First
				 * Texture Unit To Replace It With The Sampled Value Of The
				 * Normalization Cube Map.
				 */
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				gl.glEnable( GL.GL_TEXTURE_CUBE_MAP );
				gl.glBindTexture( GL.GL_TEXTURE_CUBE_MAP , normalizationCubeMap.getTextureObject() );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_REPLACE );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_TEXTURE );

				/*
				 * Set The Second Unit To The Bump Map. Set The Texture Environment
				 * Of The Second Texture Unit To Perform A Dot3 Operation With The
				 * Value Of The Previous Texture Unit (The Normalized Vector Form
				 * The Surface To The Light) And The Sampled Texture Value (The
				 * Normalized Normal Vector Of Our Bump Map).
				 */
				gl.glActiveTexture( GL.GL_TEXTURE1 );
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glBindTexture( GL.GL_TEXTURE_2D , bumpMap.getTextureObject() );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE  );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_DOT3_RGB );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PREVIOUS );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE1_RGB      , GL.GL_TEXTURE  );

				/*
				 * The third unit is used to apply the diffuse color of the
				 * material.
				 */
				gl.glActiveTexture( GL.GL_TEXTURE2 );
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glBindTexture( GL.GL_TEXTURE_2D , bumpMap.getTextureObject() );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE       );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_MODULATE      );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PRIMARY_COLOR );

				if ( hasTexture )
				{
					/*
					 * Set The Fourth Texture Unit To Our Texture. Set The Texture
					 * Environment Of The Third Texture Unit To Modulate (Multiply) The
					 * Result Of Our Dot3 Operation With The Texture Value.
					 */
					gl.glActiveTexture( GL.GL_TEXTURE3 );
					gl.glEnable( GL.GL_TEXTURE_2D );
					gl.glBindTexture( GL.GL_TEXTURE_2D , texture.getTextureObject() );
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_MODULATE );
				}

				final Material material = face.getMaterial();
				final double bumpScaleX = hasTexture ? ( material.colorMapWidth  / material.bumpMapWidth  ) : ( 1.0 / material.bumpMapWidth  );
				final double bumpScaleY = hasTexture ? ( material.colorMapHeight / material.bumpMapHeight ) : ( 1.0 / material.bumpMapHeight );

				/*
				 * Render the face.
				 */
				switch ( vertexCount )
				{
					case 2:
					{
						glWrapper.glBegin( GL.GL_LINES );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						glWrapper.glEnd();
					}
					break;

					case 3:
					{
						glWrapper.glBegin( GL.GL_TRIANGLES );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 2 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						glWrapper.glEnd();
					}
					break;

					case 4:
					{
						glWrapper.glBegin( GL.GL_QUADS );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 3 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 2 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						glWrapper.glEnd();
					}
					break;

					default:
					{
						glWrapper.glBegin( GL.GL_POLYGON );
						for ( int i = vertexCount ; --i >= 0 ; )
							setBumpedFaceVertex( glWrapper , face , textureU , textureV , bumpScaleX , bumpScaleY , i , lightPosition );
						glWrapper.glEnd();
					}
					break;
				}

				gl.glActiveTexture( GL.GL_TEXTURE3 );
				gl.glDisable( GL.GL_TEXTURE_2D );
				gl.glActiveTexture( GL.GL_TEXTURE2 );
				gl.glDisable( GL.GL_TEXTURE_2D );
				gl.glActiveTexture( GL.GL_TEXTURE1 );
				gl.glDisable( GL.GL_TEXTURE_2D );
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				gl.glDisable( GL.GL_TEXTURE_CUBE_MAP );
			}
			else
			{
				if ( hasTexture )
				{
					glWrapper.setTexture2D( true );
					texture.bind();
				}

				switch ( vertexCount )
				{
					case 2:
					{
						glWrapper.glBegin( GL.GL_LINES );
						setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
						glWrapper.glEnd();
					}
					break;

					case 3:
					{
						glWrapper.glBegin( GL.GL_TRIANGLES );
						setFaceVertex( glWrapper , face , textureU , textureV , 2 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
						glWrapper.glEnd();
					}
					break;

					case 4:
					{
						glWrapper.glBegin( GL.GL_QUADS );
						setFaceVertex( glWrapper , face , textureU , textureV , 3 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 2 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 1 , hasLighting );
						setFaceVertex( glWrapper , face , textureU , textureV , 0 , hasLighting );
						glWrapper.glEnd();
					}
					break;

					default:
					{
						glWrapper.glBegin( GL.GL_POLYGON );
						for ( int i = vertexCount ; --i >= 0 ; )
							setFaceVertex( glWrapper , face , textureU , textureV , i , hasLighting );
						glWrapper.glEnd();
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
			glWrapper.glNormal3dv( vertexNormals , vertexIndex );
		}
		else if ( hasLighting )
		{
			final Vector3D faceNormal = face.getNormal();
			glWrapper.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
		}

		final double[] vertexCoordinates = object.getVertexCoordinates();
		glWrapper.glVertex3dv( vertexCoordinates , vertexIndex );
	}

	/**
	 * Set vertex properties using GL for a face with a bump map applied.
	 *
	 * @param   glWrapper           GLWrapper.
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   faceVertexIndex     Index of vertex in face.
	 * @param   lightPosition       Light source position.
	 */
	private static void setBumpedFaceVertex( final GLWrapper glWrapper , final Face3D face , final float[] textureU , final float[] textureV , final double bumpScaleX , final double bumpScaleY , final int faceVertexIndex , final Vector3D lightPosition )
	{
		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		final double[] vertexCoordinates = object.getVertexCoordinates();

		final double relativeLightX = lightPosition.x + vertexCoordinates[ vertexIndex     ];
		final double relativeLightY = lightPosition.y + vertexCoordinates[ vertexIndex + 1 ];
		final double relativeLightZ = lightPosition.z + vertexCoordinates[ vertexIndex + 2 ];

		final GL gl = glWrapper.getGL();

		gl.glMultiTexCoord3d( GL.GL_TEXTURE0 , relativeLightX , relativeLightY , relativeLightZ );
		if ( ( textureU != null ) && ( textureV != null ) )
		{
			gl.glMultiTexCoord2f( GL.GL_TEXTURE1 , (float)bumpScaleX * textureU[ faceVertexIndex ] , (float)bumpScaleY * -textureV[ faceVertexIndex ] );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE3 , textureU[ faceVertexIndex ] , -textureV[ faceVertexIndex ] );
		}

		if ( face.isSmooth() )
		{
			final double[] vertexNormals = object.getVertexNormals();
			gl.glNormal3dv( vertexNormals , vertexIndex );
		}
		else
		{
			final Vector3D faceNormal = face.getNormal();
			gl.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
		}

		gl.glVertex3dv( vertexCoordinates , vertexIndex );
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
	 * Get {@link Texture} for bump map of {@link Face3D}.
	 *
	 * @param   gl              OpenGL context.
	 * @param   face            Face whose color map texture to return.
	 * @param   textureCache    Texture cache.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public static Texture getBumpMapTexture( final GL gl , final Face3D face , final Map<String,SoftReference<Texture>> textureCache )
	{
		final Texture result;

		final Material material = face.getMaterial();

		if ( ( material != null ) && ( material.bumpMap != null ) && ( face.getTextureU() != null ) && ( face .getTextureV() != null ) )
		{
			Texture result11 = null;

			if ( TextTools.isNonEmpty( material.bumpMap ) )
			{
				SoftReference<Texture> reference = textureCache.get( material.bumpMap );

				final boolean loadTexture;
				if ( reference == null )
				{
					loadTexture = !textureCache.containsKey( material.bumpMap );
				}
				else
				{
					result11 = reference.get();
					loadTexture = ( result11 == null );
				}

				if ( loadTexture )
				{
					BufferedImage bufferedImage = MapTools.loadImage( material.bumpMap );
					if ( bufferedImage != null )
					{
						bufferedImage = createNormalMapFromBumpMap( bufferedImage );

						final boolean autoMipmapGeneration = ( gl.isExtensionAvailable( "GL_VERSION_1_4"          ) ||
						                                       gl.isExtensionAvailable( "GL_SGIS_generate_mipmap" ) );

						System.out.println( "MipMap: " + ( autoMipmapGeneration ? "enabled" : "disabled" ) );

						result11 = TextureIO.newTexture( createCompatibleTextureImage( bufferedImage , gl ) , autoMipmapGeneration );

						result11.setTexParameteri( GL.GL_TEXTURE_WRAP_S , GL.GL_REPEAT );
						result11.setTexParameteri( GL.GL_TEXTURE_WRAP_T , GL.GL_REPEAT );

						if ( autoMipmapGeneration )
						{
							try
							{
								/**
								 * Set generate mipmaps to true, this greatly increases performance and viewing pleasure in big scenes.
								 * @TODO need to find out if generated mipmaps are faster or if pregenerated mipmaps are faster
								 */
								result11.setTexParameteri( GL.GL_GENERATE_MIPMAP , GL.GL_TRUE );

								/** Set texture magnification to linear to support mipmaps. */
								result11.setTexParameteri( GL.GL_TEXTURE_MAG_FILTER , GL.GL_LINEAR );

								/** Set texture minification to linear_mipmap)_nearest to support mipmaps */
								result11.setTexParameteri( GL.GL_TEXTURE_MIN_FILTER , GL.GL_LINEAR_MIPMAP_NEAREST );
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
					reference = ( result11 != null ) ? new SoftReference<Texture>( result11 ) : null ;
					textureCache.put( material.bumpMap , reference );
				}
			}

			result = result11;
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
		return getTexture( gl , material.colorMap , textureCache );
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   gl              OpenGL context.
	 * @param   map             Name of the texture map.
	 * @param   textureCache    Map containing the {@link SoftReference}s to the cached textures.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public static Texture getTexture( final GL gl , final String map , final Map<String,SoftReference<Texture>> textureCache )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( map ) )
		{
			SoftReference<Texture> reference = textureCache.get( map );

			final boolean loadTexture;
			if ( reference == null )
			{
				loadTexture = !textureCache.containsKey( map );
			}
			else
			{
				result      = reference.get();
				loadTexture = ( result == null );
			}

			if ( loadTexture )
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
				reference = ( result != null ) ? new SoftReference<Texture>( result ) : null ;
				textureCache.put( map , reference );
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
	private static BufferedImage createCompatibleTextureImage( final BufferedImage image , final GL gl )
	{
		/*
		 * Textures must not exceed the maximum size.
		 */
		final int[] maxTextureSizeBuffer = new int[ 1 ];
		gl.glGetIntegerv( GL.GL_MAX_TEXTURE_SIZE , maxTextureSizeBuffer , 0 );
		final int maximumTextureSize = maxTextureSizeBuffer[ 0 ];

		if ( maximumTextureSize <= 0 )
		{
			throw new IllegalStateException( "maximumTextureSize <= 0" );
		}

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
	private static BufferedImage createNormalMapFromBumpMap( final BufferedImage bumpMap )
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
	private static Texture createNormalizationCubeMap( final GL gl )
	{
		final Texture result = TextureIO.newTexture( GL.GL_TEXTURE_CUBE_MAP );
		gl.glBindTexture( GL.GL_TEXTURE_CUBE_MAP , result.getTextureObject() );

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