/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Object3D;
import ab.j3d.view.RenderStyle;
import ab.j3d.view.RenderStyleFilter;
import ab.j3d.view.Renderer;

/**
 * Implements {@link Renderer} for JOGL.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class JOGLRenderer
	extends Renderer
{
	/**
	 * OpenGL pipeline.
	 */
	private final GL _gl;

	/**
	 * Texture cache.
	 */
	private final Map<String,Texture> _textureCache;

	/**
	 * Wrapper for OpenGL pipeline.
	 */
	private GLWrapper _glWrapper;

	/**
	 * Maximum number of lights allowed in this scene.
	 */
	private int _maxLights;

	/**
	 * Background color of image.
	 */
	private final Color _backgroundColor;

	/**
	 * Grid enabled/disabled flag.
	 */
	private boolean _gridEnabled;

	/**
	 * Transforms grid to world coordinates.
	 */
	private Matrix3D _grid2wcs;

	/**
	 * Bounds of grid in cell units.
	 */
	private Rectangle _gridBounds;

	/**
	 * Size of each grid cell in world units.
	 */
	private int _gridCellSize;

	/**
	 * If set, highlight X/Y grid axes.
	 */
	private boolean _gridHighlightAxes;

	/**
	 * Interval for highlighted grid lines. Less or equal to zero if
	 * highlighting is disabled.
	 */
	private int _gridHighlightInterval;

	/**
	 * Counting variable with index of JOGL light.
	 *
	 * @see     #renderLight
	 */
	private int _lightIndex;

	/**
	 * Combined ambient light intensity. Set to 0 durig init and set to final
	 * value after all lights are processed.
	 */
	private float _ambientLightIntensity;

	/**
	 * Position of most dominant light in the scene.
	 */
	private Vector3D _dominantLightPosition;

	/**
	 * Intensity of most dominant light in the scene.
	 */
	private float _dominantLightIntensity;

	/**
	 * Temporary variable set by {@link #renderObjectBegin} to the most
	 * dominant light source relative to the object.
	 */
	private Vector3D _lightPositionRelativeToObject;

	/**
	 * Construct new JOGL renderer.
	 *
	 * @param   gl                      GL pipeline.
	 * @param   textureCache            Map containing {@link Texture}s used in the scene.
	 * @param   backgroundColor         Backgroundcolor to use.
	 * @param   gridIsEnabled           <code>true</code> if the grid must be rendered,
	 *                                  <code>false</code> otherwise.
	 * @param   grid2wcs                Transforms grid to world coordinates.
	 * @param   gridBounds              Bounds of grid.
	 * @param   gridCellSize            Size of each cell.
	 * @param   gridHighlightAxes       If set, hightlight X=0 and Y=0 axes.
	 * @param   gridHighlightInterval   Interval to use for highlighting grid lines.
	 */
	public JOGLRenderer( final GL gl , final Map<String,Texture> textureCache , final Color backgroundColor , final boolean gridIsEnabled , final Matrix3D grid2wcs , final Rectangle gridBounds , final int gridCellSize , final boolean gridHighlightAxes , final int gridHighlightInterval )
	{
		_gl = gl;
		_textureCache = textureCache;

		_backgroundColor = backgroundColor;

		_gridEnabled = gridIsEnabled;
		_grid2wcs = grid2wcs;
		_gridBounds = gridBounds;
		_gridCellSize = gridCellSize;
		_gridHighlightAxes = gridHighlightAxes;
		_gridHighlightInterval = gridHighlightInterval;

		_glWrapper = null;
		_maxLights = 0;

		_lightIndex = 0;
		_ambientLightIntensity = 0.0f;
		_dominantLightIntensity = 0.0f;
		_dominantLightPosition = null;

		_lightPositionRelativeToObject = null;
	}

	public void renderScene( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		final GL gl = _gl;
		_glWrapper = new GLWrapper( gl );
		_maxLights = JOGLTools.MAX_LIGHTS;

		/* Clear depth and color buffer. */
		final float[] backgroundRGB = _backgroundColor.getRGBColorComponents( null );
		gl.glClearColor( backgroundRGB[ 0 ] , backgroundRGB[ 1 ] , backgroundRGB[ 2 ] , 1.0f );
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT );

		/* Set backface culling. */
		gl.glCullFace( GL.GL_BACK );

		/* Let super render scene. */
		super.renderScene( nodes , styleFilters , sceneStyle );

		/* Finish up. */
		if ( _gridEnabled )
		{
			drawGrid( _grid2wcs , _gridBounds , _gridCellSize , _gridHighlightAxes , _gridHighlightInterval );
		}
	}

	protected void renderLights( final List<ContentNode> nodes )
	{
		final GL gl = _gl;

		_lightIndex = 0;
		_ambientLightIntensity = 0.0f;
		_dominantLightPosition = null;
		_dominantLightIntensity = 0.0f;

		/* Set Light Model to two sided lighting. */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE , GL.GL_TRUE );

		/* Set local view point */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_LOCAL_VIEWER , GL.GL_TRUE );

		/* Apply specular hightlight after texturing (otherwise, this would be done before texturing, so we won't see it). */
		if ( gl.isExtensionAvailable( "GL_VERSION_1_2" ) )
		{
			gl.glLightModeli( GL.GL_LIGHT_MODEL_COLOR_CONTROL , GL.GL_SEPARATE_SPECULAR_COLOR );
		}

		/* Disable all lights */
		for( int lightIndex = 0 ; lightIndex < _maxLights ; lightIndex++ )
		{
			gl.glDisable( GL.GL_LIGHT0 + lightIndex );
		}

		/* Let super render lights */
		super.renderLights( nodes );

		/* Set combined ambient light intensity. */
		final float ambientLightIntensity = _ambientLightIntensity;
		gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT , new float[]{ambientLightIntensity , ambientLightIntensity , ambientLightIntensity , 1.0f} , 0 );
	}

	protected void renderLight( final Matrix3D light2world , final Light3D light )
	{
		final GL gl = _gl;

		final float lightIntensity = (float)light.getIntensity() / 255.0f;

		if ( light.isAmbient() )
		{
			_ambientLightIntensity += lightIntensity;
		}
		else
		{
			final int lightIndex = _lightIndex++;
			if ( lightIndex >= _maxLights )
			{
				throw new IllegalStateException( "No more than " + _maxLights + " lights supported." );
			}

			final int lightNumber = GL.GL_LIGHT0 + lightIndex;

			gl.glLightfv( lightNumber , GL.GL_AMBIENT  , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
			gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] { (float)light2world.xo , (float)light2world.yo , (float)light2world.zo , 1.0f } , 0 );
			gl.glLightfv( lightNumber , GL.GL_DIFFUSE  , new float[] {  lightIntensity , lightIntensity , lightIntensity , 1.0f } , 0 );
			gl.glLightfv( lightNumber , GL.GL_SPECULAR , new float[] {  lightIntensity , lightIntensity , lightIntensity , 1.0f } , 0 );

			final float fallOff = (float)light.getFallOff();
			if ( fallOff > 0.0f )
			{
				/*
				 * intensity = 1 / ( c + l * d + q * d^2 )
				 * constant + quadratic * distance ^ 2 )
				 */
				gl.glLightfv( lightNumber , GL.GL_CONSTANT_ATTENUATION  , new float[] { 0.5f } , 0 );
				gl.glLightfv( lightNumber , GL.GL_LINEAR_ATTENUATION    , new float[] { 0.0f } , 0 );
				gl.glLightfv( lightNumber , GL.GL_QUADRATIC_ATTENUATION , new float[] { 0.5f / ( fallOff * fallOff ) } , 0 );
			}
			else
			{
				gl.glLightfv( lightNumber , GL.GL_CONSTANT_ATTENUATION  , new float[] { 1.0f } , 0 );
				gl.glLightfv( lightNumber , GL.GL_LINEAR_ATTENUATION    , new float[] { 0.0f } , 0 );
				gl.glLightfv( lightNumber , GL.GL_QUADRATIC_ATTENUATION , new float[] { 0.0f } , 0 );
			}

			gl.glEnable( lightNumber );
		}

		/**
		 * Determine dominant light position, used for bump mapping.
		 * This method can be rather inaccurate, especially if the most
		 * intense light is far away from a bump mapped object.
		 */
		if ( ( light.getFallOff() >= 0.0 ) && ( ( _dominantLightPosition == null ) || ( _dominantLightIntensity < lightIntensity ) ) )
		{
			_dominantLightPosition = Vector3D.INIT.set( light2world.xo , light2world.yo , light2world.zo );
			_dominantLightIntensity = lightIntensity;
		}
	}

	protected void renderObjectBegin( final Matrix3D object2world , final Object3D object , final RenderStyle objectStyle )
	{
		_lightPositionRelativeToObject = ( _dominantLightPosition != null ) ? object2world.inverseMultiply( _dominantLightPosition ) : null;
		_gl.glPushMatrix();
		JOGLTools.glMultMatrixd( _gl , object2world );
	}

	protected void renderObjectEnd()
	{
		_gl.glPopMatrix();
	}

	protected void renderMaterialFace( final Face3D face , final RenderStyle style )
	{
		final int      vertexCount = face.getVertexCount();
		final Material material    = ( style.getMaterialOverride() != null ) ? style.getMaterialOverride() : face.getMaterial();

		if ( ( material != null ) && ( vertexCount >= 2 ) )
		{
			final Vector3D lightPosition   = _lightPositionRelativeToObject;
			final boolean  hasLighting     = style.isMaterialLightingEnabled() && ( lightPosition != null );
			final boolean  backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();

			final GLWrapper glWrapper = _glWrapper;
			glWrapper.setPolygonMode( GL.GL_FILL );
			glWrapper.setPolygonOffsetFill( false );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );
			setMaterial( material );
			final GL gl = _gl;

			final boolean hasUV      = ( face.getTextureU() != null ) && ( face.getTextureV() != null );
			final Texture texture    = hasUV ? JOGLTools.getColorMapTexture( gl , material , _textureCache ) : null;
			final boolean hasTexture = ( texture != null );
			final Texture bumpMap    = ( hasLighting && hasUV ) ? JOGLTools.getBumpMapTexture( gl , material , _textureCache ) : null;

			if ( bumpMap != null )
			{
				final float[] textureU = face.getTextureU();
				final float[] textureV = face.getTextureV();

				Texture normalizationCubeMap = _textureCache.get( JOGLTools.NORMALIZATION_CUBE_MAP );
				if ( normalizationCubeMap == null )
				{
					normalizationCubeMap = JOGLTools.createNormalizationCubeMap( gl );
					_textureCache.put( JOGLTools.NORMALIZATION_CUBE_MAP , normalizationCubeMap );
				}

				/*
				 * Set The First Texture Unit To Normalize Our Vector From The
				 * Surface To The Light. Set The Texture Environment Of The First
				 * Texture Unit To Replace It With The Sampled Value Of The
				 * Normalization Cube Map.
				 */
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				normalizationCubeMap.enable();
				normalizationCubeMap.bind();
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
				bumpMap.enable();
				bumpMap.bind();
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE  );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_DOT3_RGB );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PREVIOUS );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE1_RGB      , GL.GL_TEXTURE  );

				/*
				 * The third unit is used to apply the diffuse color of the
				 * material.
				 */
				gl.glActiveTexture( GL.GL_TEXTURE2 );
				bumpMap.enable();
				bumpMap.bind();
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_COMBINE       );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_COMBINE_RGB      , GL.GL_MODULATE      );
				gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_SOURCE0_RGB      , GL.GL_PRIMARY_COLOR );

				final double bumpScaleX;
				final double bumpScaleY;

				if ( hasTexture )
				{
					/*
					 * Set The Fourth Texture Unit To Our Texture. Set The Texture
					 * Environment Of The Third Texture Unit To Modulate (Multiply) The
					 * Result Of Our Dot3 Operation With The Texture Value.
					 */
					gl.glActiveTexture( GL.GL_TEXTURE3 );
					texture.enable();
					texture.bind();
					gl.glTexEnvi( GL.GL_TEXTURE_ENV , GL.GL_TEXTURE_ENV_MODE , GL.GL_MODULATE );

					/*
					 * Scale bump map based on color map size.
					 */
					bumpScaleX = material.colorMapWidth  / material.bumpMapWidth;
					bumpScaleY = material.colorMapHeight / material.bumpMapHeight;
				}
				else
				{
					bumpScaleX = 1.0 / material.bumpMapWidth;
					bumpScaleY = 1.0 / material.bumpMapHeight;
				}

				/*
				 * Render the face.
				 */
				switch ( vertexCount )
				{
					case 0 :
						break;

					case 1 :
						gl.glBegin( GL.GL_POINTS );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						gl.glEnd();
						break;

					case 2:
						gl.glBegin( GL.GL_LINES );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						gl.glEnd();
						break;

					case 3:
						gl.glBegin( GL.GL_TRIANGLES );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 2 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						gl.glEnd();
						break;

					case 4:
						gl.glBegin( GL.GL_QUADS );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 3 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 2 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 1 , lightPosition );
						setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , 0 , lightPosition );
						gl.glEnd();
						break;

					default:
						gl.glBegin( GL.GL_POLYGON );
						for ( int i = vertexCount ; --i >= 0 ; )
						{
							setBumpedFaceVertex( face , textureU , textureV , bumpScaleX , bumpScaleY , i , lightPosition );
						}
						gl.glEnd();
						break;
				}

				if ( hasTexture )
				{
					gl.glActiveTexture( GL.GL_TEXTURE3 );
					texture.disable();
				}
				gl.glActiveTexture( GL.GL_TEXTURE2 );
				bumpMap.disable();
				gl.glActiveTexture( GL.GL_TEXTURE1 );
				bumpMap.disable();
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				normalizationCubeMap.disable();
			}
			else if ( hasTexture )
			{
				final float[] textureU = face.getTextureU();
				final float[] textureV = face.getTextureV();

				texture.enable();
				texture.bind();

				switch ( vertexCount )
				{
					case 0 :
						break;

					case 1 :
						gl.glBegin( GL.GL_POINTS );
						setFaceVertex( face , 0 , hasLighting );
						gl.glEnd();
						break;

					case 2:
						gl.glBegin( GL.GL_LINES );
						setFaceVertex( face , textureU , textureV , 0 , hasLighting );
						setFaceVertex( face , textureU , textureV , 1 , hasLighting );
						gl.glEnd();
						break;

					case 3:
						gl.glBegin( GL.GL_TRIANGLES );
						setFaceVertex( face , textureU , textureV , 2 , hasLighting );
						setFaceVertex( face , textureU , textureV , 1 , hasLighting );
						setFaceVertex( face , textureU , textureV , 0 , hasLighting );
						gl.glEnd();
						break;

					case 4:
						gl.glBegin( GL.GL_QUADS );
						setFaceVertex( face , textureU , textureV , 3 , hasLighting );
						setFaceVertex( face , textureU , textureV , 2 , hasLighting );
						setFaceVertex( face , textureU , textureV , 1 , hasLighting );
						setFaceVertex( face , textureU , textureV , 0 , hasLighting );
						gl.glEnd();
						break;

					default:
						gl.glBegin( GL.GL_POLYGON );
						for ( int i = vertexCount ; --i >= 0 ; )
						{
							setFaceVertex( face , textureU , textureV , i , hasLighting );
						}
						gl.glEnd();
						break;
				}

				texture.disable();
			}
			else
			{
				drawFaceWithoutTexture( face , hasLighting );
			}

		}
	}

	protected void renderFilledFace( final Face3D face , final RenderStyle style )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Color   color           = style.getFillColor();
			final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting     = style.isFillLightingEnabled() && ( _lightPositionRelativeToObject != null );

			final GLWrapper glWrapper = _glWrapper;
			glWrapper.setPolygonMode( GL.GL_FILL );
			glWrapper.setPolygonOffsetFill( true );
			glWrapper.glPolygonOffset( 1.0f , 1.0f );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );
			setColor( color );

			drawFaceWithoutTexture( face , hasLighting );
		}
	}

	protected void renderStrokedFace( final Face3D face , final RenderStyle style )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount >= 2 )
		{
			final Color   color           = style.getStrokeColor();
			final float   width           = style.getStrokeWidth();
			final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting     = style.isStrokeLightingEnabled() && ( _lightPositionRelativeToObject != null );

			final GLWrapper glWrapper = _glWrapper;
			glWrapper.setPolygonMode( GL.GL_LINE );
			glWrapper.setPolygonOffsetFill( true );
			glWrapper.glPolygonOffset( 1.0f , 2.0f );
			glWrapper.glLineWidth( width );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );
			setColor( color );

			// @FIXME Drawing outlines like this is only needed while Face3D doesn't support concave faces.
			// (A single Face3D could then be used for a concave shape, and the proper outline would be drawn automatically.)
			final Object3D object3D = face.getObject();
			if ( object3D instanceof ExtrudedObject2D )
			{
				if ( face == object3D.getFace( 0 ) )
				{
					final ExtrudedObject2D extruded  = (ExtrudedObject2D)object3D;
					final Shape            shape     = extruded.shape;
					final Vector3D         extrusion = extruded.extrusion;

					final GL gl = _gl;
					gl.glPushMatrix();
					JOGLTools.glMultMatrixd( gl , extruded.transform );

					/* Draw the bottom outline of the extruded shape. */
					drawShape( shape , extruded.flatness );

					/* Draw the sides of the extruded shape. */
					drawExtrusionLines( shape , extrusion );

					/* Draw the top outline of the extruded shape. */
					gl.glMultMatrixd( new double[] {
						1.0 , 0.0 , 0.0 , 0.0 ,
						0.0 , 1.0 , 0.0 , 0.0 ,
						0.0 , 0.0 , 1.0 , 0.0 ,
						extrusion.x , extrusion.y ,extrusion.z , 1.0 } , 0 );

					drawShape( shape , extruded.flatness );

					gl.glPopMatrix();
				}
			}
			else
			{
				drawFaceWithoutTexture( face , hasLighting );
			}
		}
	}

	protected void renderFaceVertices( final RenderStyle style , final Matrix3D object2world , final Face3D face )
	{
		final int vertexCount = face.getVertexCount();
		if ( vertexCount > 0 )
		{
			final Color   color           = style.getVertexColor();
			final boolean backfaceCulling = style.isBackfaceCullingEnabled() && !face.isTwoSided();
			final boolean hasLighting     = style.isVertexLightingEnabled() && ( _lightPositionRelativeToObject != null );

			final GLWrapper glWrapper = _glWrapper;
			glWrapper.setPolygonMode( GL.GL_POINT );
			glWrapper.setPolygonOffsetFill( true );
			glWrapper.glPolygonOffset( 1.0f , 2.0f );
			glWrapper.setCullFace( backfaceCulling );
			glWrapper.setLighting( hasLighting );

			setColor( color );

			drawFaceWithoutTexture( face , hasLighting );
		}
	}

	/**
	 * Draw a 3D face through OpenGL using a {@link Color}. This is simpler to
	 * draw since we won't have anything to do with textures here.
	 *
	 * @param   face            Face to draw.
	 * @param   setNormals     If set, enable lighting (generate normals).
	 */
	private void drawFaceWithoutTexture( final Face3D face , final boolean setNormals )
	{
		final GL gl = _gl;

		final int vertexCount = face.getVertexCount();
		switch ( vertexCount )
		{
			case 0 :
				break;

			case 1 :
				gl.glBegin( GL.GL_POINTS );
				setFaceVertex( face , 0 , setNormals );
				gl.glEnd();
				break;

			case 2 :
				gl.glBegin( GL.GL_LINES );
				setFaceVertex( face , 0 , setNormals );
				setFaceVertex( face , 1 , setNormals );
				gl.glEnd();
				break;

			case 3:
				gl.glBegin( GL.GL_TRIANGLES );
				setFaceVertex( face , 2 , setNormals );
				setFaceVertex( face , 1 , setNormals );
				setFaceVertex( face , 0 , setNormals );
				gl.glEnd();
				break;

			case 4:
				gl.glBegin( GL.GL_QUADS );
				setFaceVertex( face , 3 , setNormals );
				setFaceVertex( face , 2 , setNormals );
				setFaceVertex( face , 1 , setNormals );
				setFaceVertex( face , 0 , setNormals );
				gl.glEnd();
				break;

			default:
				gl.glBegin( GL.GL_POLYGON );
				for ( int i = vertexCount ; --i >= 0 ; )
				{
					setFaceVertex( face , i , setNormals );
				}
				gl.glEnd();
				break;
		}
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   color   Color to set.
	 */
	public void setColor( final Color color )
	{
		final float[] rgba  = color.getRGBComponents( null );
		final float   red   = rgba[ 0 ];
		final float   green = rgba[ 1 ];
		final float   blue  = rgba[ 2 ];
		final float   alpha = rgba[ 3 ];

		final GLWrapper glWrapper = _glWrapper;
		glWrapper.setColor( red , green , blue , alpha );
		glWrapper.setMaterialAmbient( red , green , blue , 1.0f );
		glWrapper.setMaterialDiffuse( red , green , blue , alpha );
		glWrapper.setMaterialSpecular( 1.0f , 1.0f , 1.0f , 1.0f );
		glWrapper.setMaterialShininess( 16.0f );
		glWrapper.setMaterialEmission( 0.0f , 0.0f , 0.0f , 1.0f );
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   material  {@link Material} properties.
	 */
	public void setMaterial( final Material material )
	{
		final GLWrapper glWrapper = _glWrapper;
		glWrapper.setColor( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );
		glWrapper.setMaterialAmbient( material.ambientColorRed , material.ambientColorGreen , material.ambientColorBlue , 1.0f );
		glWrapper.setMaterialDiffuse( material.diffuseColorRed , material.diffuseColorGreen , material.diffuseColorBlue , material.diffuseColorAlpha );
		glWrapper.setMaterialSpecular(  material.specularColorRed , material.specularColorGreen , material.specularColorBlue , 1.0f );
		glWrapper.setMaterialEmission( material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue , 1.0f );
		glWrapper.setMaterialShininess( (float)material.shininess );
	}

	/**
	 * Set vertex properties using GL.
	 *
	 * @param   face                Face whose vertex to set.
	 * @param   faceVertexIndex     Index of vertex in face.
	 * @param   setNormals          Specifies whether normals are set or not.
	 */
	private void setFaceVertex( final Face3D face , final int faceVertexIndex , final boolean setNormals )
	{
		final GL gl = _gl;

		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		if ( setNormals )
		{
			if ( face.isSmooth() )
			{
				gl.glNormal3dv( object.getVertexNormals() , vertexIndex );
			}
			else
			{
				final Vector3D faceNormal = face.getNormal();
				gl.glNormal3d( faceNormal.x , faceNormal.y , faceNormal.z );
			}
		}

		gl.glVertex3dv( object.getVertexCoordinates() , vertexIndex );
	}

	/**
	 * Set vertex properties using GL.
	 *
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   faceVertexIndex     Index of vertex in face.
	 * @param   hasLighting         Specifies whether lighting is used or not
	 */
	private void setFaceVertex( final Face3D face , final float[] textureU , final float[] textureV , final int faceVertexIndex , final boolean hasLighting )
	{
		if ( ( textureU != null ) && ( textureV != null ) )
		{
			_gl.glTexCoord2f( textureU[ faceVertexIndex ] , -textureV[ faceVertexIndex ] );
		}

		setFaceVertex( face , faceVertexIndex , hasLighting );
	}

	/**
	 * Set vertex properties using GL for a face with a bump map applied.
	 *
	 * @param   face                Face whose vertex to set.
	 * @param   textureU            Horizontal texture coordinate.
	 * @param   textureV            Vertical texture coordinate.
	 * @param   bumpScaleX          Multiply U coordinates with this for bump X.
	 * @param   bumpScaleY          Multiply V coordinates with this for bump Y.
	 * @param   faceVertexIndex     Index of vertex in face.
	 * @param   lightPosition       Light source position.
	 */
	private void setBumpedFaceVertex( final Face3D face , final float[] textureU , final float[] textureV , final double bumpScaleX , final double bumpScaleY , final int faceVertexIndex , final Vector3D lightPosition )
	{
		final Object3D object            = face.getObject();
		final int[]    faceVertexIndices = face.getVertexIndices();
		final int      vertexIndex       = faceVertexIndices[ faceVertexIndex ] * 3;

		final double[] vertexCoordinates = object.getVertexCoordinates();

		final double relativeLightX = lightPosition.x + vertexCoordinates[ vertexIndex     ];
		final double relativeLightY = lightPosition.y + vertexCoordinates[ vertexIndex + 1 ];
		final double relativeLightZ = lightPosition.z + vertexCoordinates[ vertexIndex + 2 ];

		final GL gl = _gl;
		gl.glMultiTexCoord3d( GL.GL_TEXTURE0 , relativeLightX , relativeLightY , relativeLightZ );

		if ( ( textureU != null ) && ( textureV != null ) )
		{
			gl.glMultiTexCoord2f( GL.GL_TEXTURE1 , (float)bumpScaleX * textureU[ faceVertexIndex ] , (float)bumpScaleY * -textureV[ faceVertexIndex ] );
			gl.glMultiTexCoord2f( GL.GL_TEXTURE3 , textureU[ faceVertexIndex ] , -textureV[ faceVertexIndex ] );
		}

		setFaceVertex( face , faceVertexIndex , true );
	}

	/**
	 * Draws the outline of the given shape.
	 *
	 * @param   shape       Shape to be drawn.
	 * @param   flatness    Flatness used to interpolate the shape's curves.
	 *
	 * @see     Shape#getPathIterator(AffineTransform ,double)
	 */
	private void drawShape( final Shape shape , final double flatness )
	{
		final GL gl = _gl;
		gl.glBegin( GL.GL_LINE_STRIP );

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
						gl.glEnd();
						gl.glBegin( GL.GL_LINE_STRIP );
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

			gl.glVertex3d( x , y , 0.0 );
		}

		gl.glEnd();
	}

	/**
	 * Draw the sides of the extruded shape.
	 *
	 * @param   shape       Base shape.
	 * @param   extrusion   Extrusion vector (control-point displacement). This is a displacement relative to the shape being extruded.
	 */
	private void drawExtrusionLines( final Shape shape , final Vector3D extrusion )
	{
		final GL gl = _gl;

		gl.glBegin( GL.GL_LINES );

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

			gl.glVertex3d( x , y , 0.0 );
			gl.glVertex3d( x + extrusion.x , y + extrusion.y , extrusion.z );
		}

		gl.glEnd();
	}

	/**
	 * Draw a 3D grid centered around point x,y,z with size dx,dy.
	 *
	 * @param   grid2world          Transforms grid to world coordinates.
	 * @param   gridBounds          Bounds of grid.
	 * @param   cellSize            Size of each cell.
	 * @param   hightlightAxes      If set, hightlight X=0 and Y=0 axes.
	 * @param   highlightInterval   Interval to use for highlighting grid lines.
	 */
	public void drawGrid( final Matrix3D grid2world , final Rectangle gridBounds , final int cellSize , final boolean hightlightAxes , final int highlightInterval )
	{
		if ( ( grid2world != null ) && ( gridBounds != null ) && ( gridBounds.width >= 0 ) && ( gridBounds.height >= 0 ) && ( cellSize >= 0 ) ) // argument sanity
		{
			final GL gl = _gl;
			final GLWrapper glWrapper = _glWrapper;

			gl.glPushMatrix();
			JOGLTools.glMultMatrixd( gl , grid2world );

			glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
			glWrapper.setBlend( true );
			gl.glEnable( GL.GL_LINE_SMOOTH );

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
					glWrapper.setColor( 0.1f , 0.1f , 0.1f , 1.0f );
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
					glWrapper.setColor( 0.5f , 0.5f , 0.5f , 1.0f );
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
			glWrapper.setColor( 0.75f , 0.75f , 0.75f , 1.0f );
			gl.glBegin( GL.GL_LINES );

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

			gl.glEnd();

			gl.glPopMatrix();
		}
	}
}
