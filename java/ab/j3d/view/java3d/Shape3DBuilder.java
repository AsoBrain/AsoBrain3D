/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
package ab.j3d.view.java3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.LineArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This object builds {@link Shape3D} objects based on data from
 * {@link Face3D} objects fed to it. The faces (with any number of
 * vertices), are translated into lines, triangles, and quads as defined by the
 * Java 3D API.
 *
 * IMPORTANT: This class should be used following a strict recipe:
 * <ol>
 *   <li>
 *     Construct a new {@link Shape3DBuilder}.
 *   </li>
 *   <li>
 *     Call the {@link #prepareFace} method for every face that will be
 *     added later. This is used to determine correct storage requirements,
 *     so no collection classes are needed.
 *   </li>
 *   <li>
 *     Call the {@link #addFace} method for every face that was
 *     previously announced using {@link #prepareFace}. This will take
 *     care of the the line/triangle/quad geometry conversions.
 *   </li>
 *   <li>
 *     Call the {@link #buildShapes} method. This will build the
 *     actual {@link Shape3D} objects from the collected geometry data,
 *     and add them to a supplied {@link BranchGroup} node.
 *   </li>
 * </ol>
 * After completion, steps 2 to 4 may be repeated to create more shapes.
 *
 * @see     Java3dTools
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Shape3DBuilder
{
	/**
	 * Create a Java 3D {@link BranchGroup} containing a content graph by
	 * converting {@link Object3D} instances in a
	 * {@link Node3DCollection} using this builder.
	 *
	 * @param   nodes               Collection of nodes to create the branch group from.
	 * @param   textureOverride     Texture to use instead of actual object texture.
	 * @param   extraOpacity        Extra object opacity (0.0=translucent, 1.0=opaque).
	 *
	 * @return  {@link BranchGroup} containing the created content graph.
	 */
	public static BranchGroup createBranchGroup( final Node3DCollection nodes , final TextureSpec textureOverride , final float extraOpacity )
	{
		final Shape3DBuilder shapeBuilder = new Shape3DBuilder();

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Object3D object3d  = (Object3D)nodes.getNode( i );
			final int      faceCount = object3d.getFaceCount();

			for ( int j = 0 ; j < faceCount ; j++ )
				shapeBuilder.prepareFace( object3d.getFace( j ) , textureOverride , extraOpacity );
		}

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Matrix3D xform         = nodes.getMatrix( i );
			final Object3D object3d      = (Object3D)nodes.getNode( i );
			final int      faceCount     = object3d.getFaceCount();
			final double[] pointCoords   = object3d.getPointCoords();
			final double[] vertexNormals = object3d.getVertexNormals();

			for ( int j = 0 ; j < faceCount ; j++ )
				shapeBuilder.addFace( xform , pointCoords , vertexNormals , object3d.getFace( j ) , textureOverride , extraOpacity );
		}

		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		result.setCapability( BranchGroup.ALLOW_DETACH );
		shapeBuilder.buildShapes( result );
		return result;
	}

	/**
	 * Create a Java 3D {@link BranchGroup} containing a content graph by
	 * converting an {@link Object3D} instance using this builder.
	 *
	 * @param   xform               Transform to apply to vertices.
	 * @param   object3d            Object3D to convert.
	 * @param   textureOverride     Texture to use instead of actual object texture.
	 * @param   extraOpacity        Extra object opacity (0.0=translucent, 1.0=opaque).
	 *
	 * @return  {@link BranchGroup} containing the created content graph.
	 */
	public static BranchGroup createBranchGroup( final Matrix3D xform , final Object3D object3d , final TextureSpec textureOverride , final float extraOpacity )
	{
		final Shape3DBuilder shapeBuilder = new Shape3DBuilder();

		final int      faceCount     = object3d.getFaceCount();
		final double[] pointCoords   = object3d.getPointCoords();
		final double[] vertexNormals = object3d.getVertexNormals();

		for ( int j = 0 ; j < faceCount ; j++ )
			shapeBuilder.prepareFace( object3d.getFace( j ) , textureOverride , extraOpacity );

		for ( int j = 0 ; j < faceCount ; j++ )
			shapeBuilder.addFace( xform , pointCoords , vertexNormals , object3d.getFace( j ) , textureOverride , extraOpacity );

		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		result.setCapability( BranchGroup.ALLOW_DETACH );
		shapeBuilder.buildShapes( result );
		return result;
	}

	/**
	 * Array containing texture groups. A texture group contains shapes that
	 * share the same texture.
	 * <p />
	 * The array length should not be used to determine the number of elements
	 * contained therein (it is initially <code>null</code> anyway), use the
	 * {@link #_textureGroupCount} field instead.
	 */
	private TextureGroup[] _textureGroups;

	/**
	 * The number of elements stored in {@link #_textureGroups}.
	 */
	private int _textureGroupCount;

	/**
	 * This is the workhorse of the builder. It contains a set of shapes that
	 * share the same texture. It translates faces with any number of vertices
	 * to lines, triangles, and quads as defined by the Java 3D API.
	 *
	 * IMPORTANT: This class should be used following a strict recipe:
	 * <ol>
	 *   <li>
	 *     Construct the group with the correct texture
	 *     ({@link TextureSpec}) and appearance ({@link Appearance}).
	 *   </li>
	 *   <li>
	 *     Call the {@link #prepareFace} method for every face that will be
	 *     added later. This is used to determine correct storage requirements,
	 *     so no collection classes are needed.
	 *   </li>
	 *   <li>
	 *     Call the {@link #addFace} method for every face that was
	 *     previously announced using {@link #prepareFace}. This will take
	 *     care of the the line/triangle/quad geometry conversions.
	 *   </li>
	 *   <li>
	 *     Call the {@link #buildShapes} method. This will build the
	 *     actual {@link Shape3D} objects from the collected geometry data,
	 *     and add them to a supplied {@link BranchGroup} node.
	 *   </li>
	 * </ol>
	 * After completion, steps 2 to 4 may be repeated to create more shapes.
	 * Notice that this is probably not worthwhile, since no other data than the
	 * object itself is reused.
	 */
	private static class TextureGroup
	{
		private TextureSpec   _texture;
		private int           _alpha;
		private boolean       _hasBackface;
		private Appearance    _appearance;
		private int           _lineCount;
		private LineArray     _lineArray;
		private int           _triangleCount;
		private TriangleArray _triangleArray;
		private int           _quadCount;
		private QuadArray     _quadArray;


		TextureGroup( final TextureSpec texture , final int alpha , final boolean hasBackface , final Appearance appearance )
		{
			_texture       = texture;
			_alpha         = alpha;
			_hasBackface   = hasBackface;
			_appearance    = appearance;
			_lineCount     = 0;
			_lineArray     = null;
			_triangleCount = 0;
			_triangleArray = null;
			_quadCount     = 0;
			_quadArray     = null;
		}

		void prepareFace( final int numberOfPointsInFace )
		{
			if ( numberOfPointsInFace == 2 )
			{
				_lineCount++;
			}
			else if ( numberOfPointsInFace > 2 )
			{
				final int numberOfQuads = ( numberOfPointsInFace - 2 ) / 2;
				if ( numberOfQuads > 0 )
				{
					_quadCount += numberOfQuads;
				}

				if ( ( numberOfPointsInFace % 2 ) != 0 )
				{
					_triangleCount++;
				}
			}
		}

		void addFace( final Matrix3D xform , final double[] pointCoords , final double[] vertexNormals , final Face3D face )
		{
			final int vertexCount = face.getVertexCount();
			if ( vertexCount >= 2 )
			{
				final Appearance appearance   = _appearance;
				final int[]      pointIndices = face.getPointIndices();

				if ( vertexCount == 2 )
				{
					LineArray lineArray = _lineArray;
					int       lineCount = _lineCount;

					if ( lineArray == null )
					{
						if ( lineCount == 0 )
							throw new IllegalStateException( "should have called prepareFace()!" );

						lineArray = new LineArray( 2 * lineCount , LineArray.COORDINATES );
						lineCount = 0;
					}

					final int coordinateIndex = lineCount * 2;
					lineArray.setCoordinate( coordinateIndex     , getPointCoordinate( xform , pointCoords , pointIndices[ 0 ] ) );
					lineArray.setCoordinate( coordinateIndex + 1 , getPointCoordinate( xform , pointCoords , pointIndices[ 1 ] ) );

					_lineArray = lineArray;
					_lineCount = lineCount + 1;
				}
				else // vertexCount > 2
				{
					final int[]   textureU   = face.getTextureU();
					final int[]   textureV   = face.getTextureV();
					final boolean hasTexture = ( _texture == face.getTexture() ) && ( textureU != null ) && ( textureV != null ) && ( appearance.getTexture() != null );

					final int        index0        = pointIndices[ 0 ];
					final Point3d    pointCoord0   = getPointCoordinate( xform , pointCoords , index0 );
					final Vector3f   normal0       = getVertexNormal( xform , vertexNormals , index0 );
					final TexCoord2f textureCoord0 = hasTexture ? getTextureCoordinate( textureU , textureV , 0 ) : null;

					final int  index1        = pointIndices[ 1 ];
					Point3d    pointCoord1   = getPointCoordinate( xform , pointCoords , index1 );
					Vector3f   normal1       = getVertexNormal( xform , vertexNormals , index1 );
					TexCoord2f textureCoord1 = hasTexture ? getTextureCoordinate( textureU , textureV , 1 ) : null;

					if ( vertexCount > 3 ) // more than 3 vertices => add quads
					{
						QuadArray quadArray = _quadArray;
						int       quadCount = _quadCount;

						if ( quadArray == null )
						{
							if ( quadCount == 0 )
								throw new IllegalStateException( "should have called prepareFace()!" );

							quadArray = new QuadArray( 4 * quadCount , QuadArray.COORDINATES | QuadArray.NORMALS | ( hasTexture ? QuadArray.TEXTURE_COORDINATE_2 : 0 ) );
							quadCount = 0;
						}

						for ( int vertex3 = 3 ; vertex3 < vertexCount ; vertex3 += 2 )
						{
							final int        vertex2       = vertex3 - 1;
							final int        index2        = pointIndices[ vertex2 ];
							final Point3d    pointCoord2   = getPointCoordinate( xform , pointCoords , index2 );
							final Vector3f   normal2       = getVertexNormal( xform , vertexNormals , index2 );
							final TexCoord2f textureCoord2 = hasTexture ? getTextureCoordinate( textureU , textureV , vertex2 ) : null;

							final int        index3        = pointIndices[ vertex3 ];
							final Point3d    pointCoord3   = getPointCoordinate( xform , pointCoords , index3 );
							final Vector3f   normal3       = getVertexNormal( xform , vertexNormals , index3 );
							final TexCoord2f textureCoord3 = hasTexture ? getTextureCoordinate( textureU , textureV , vertex3 ) : null;

							final int coordinateIndex = quadCount * 4;

							quadArray.setCoordinate( coordinateIndex     , pointCoord3 );
							quadArray.setCoordinate( coordinateIndex + 1 , pointCoord2 );
							quadArray.setCoordinate( coordinateIndex + 2 , pointCoord1 );
							quadArray.setCoordinate( coordinateIndex + 3 , pointCoord0 );

							quadArray.setNormal( coordinateIndex     , normal3 );
							quadArray.setNormal( coordinateIndex + 1 , normal2 );
							quadArray.setNormal( coordinateIndex + 2 , normal1 );
							quadArray.setNormal( coordinateIndex + 3 , normal0 );

							if ( hasTexture )
							{
								quadArray.setTextureCoordinate( 0 , coordinateIndex     , textureCoord3 );
								quadArray.setTextureCoordinate( 0 , coordinateIndex + 1 , textureCoord2 );
								quadArray.setTextureCoordinate( 0 , coordinateIndex + 2 , textureCoord1 );
								quadArray.setTextureCoordinate( 0 , coordinateIndex + 3 , textureCoord0 );
							}

							pointCoord1   = pointCoord3;
							normal1       = normal3;
							textureCoord1 = textureCoord3;

							quadCount++;
						}

						_quadArray = quadArray;
						_quadCount = quadCount;
					}

					if ( ( vertexCount % 2 ) != 0 ) // odd number of vertices => add triangle
					{
						TriangleArray triangleArray   = _triangleArray;
						int           triangleCount   = _triangleCount;

						if ( triangleArray == null )
						{
							if ( triangleCount == 0 )
								throw new IllegalStateException( "should have called prepareFace()!" );

							triangleArray = new TriangleArray( 3 * triangleCount , TriangleArray.COORDINATES | TriangleArray.NORMALS | ( hasTexture ? TriangleArray.TEXTURE_COORDINATE_2 : 0 ) );
							triangleCount = 0;
						}

						final int        index2        = pointIndices[ vertexCount - 1 ];
						final Point3d    pointCoord2   = getPointCoordinate( xform , pointCoords , index2 );
						final Vector3f   normal2       = getVertexNormal( xform , vertexNormals , index2 );
						final TexCoord2f textureCoord2 = hasTexture ? getTextureCoordinate( textureU , textureV , vertexCount - 1 ) : null;

						final int coordinateIndex = triangleCount * 3;

						triangleArray.setCoordinate( coordinateIndex     , pointCoord2 );
						triangleArray.setCoordinate( coordinateIndex + 1 , pointCoord1 );
						triangleArray.setCoordinate( coordinateIndex + 2 , pointCoord0 );

						triangleArray.setNormal( coordinateIndex     , normal2 );
						triangleArray.setNormal( coordinateIndex + 1 , normal1 );
						triangleArray.setNormal( coordinateIndex + 2 , normal0 );

						if ( hasTexture )
						{
							triangleArray.setTextureCoordinate( 0 , coordinateIndex     , textureCoord2 );
							triangleArray.setTextureCoordinate( 0 , coordinateIndex + 1 , textureCoord1 );
							triangleArray.setTextureCoordinate( 0 , coordinateIndex + 2 , textureCoord0 );
						}

						_triangleArray = triangleArray;
						_triangleCount = triangleCount + 1;
					}
				}
			}
		}

		void buildShapes( final BranchGroup parentGroup )
		{
			final Appearance    appearance    = _appearance;
			final LineArray     lineArray     = _lineArray;
			final TriangleArray triangleArray = _triangleArray;
			final QuadArray     quadArray     = _quadArray;

			if ( lineArray != null )
				parentGroup.addChild( new Shape3D( lineArray , appearance ) );

			if ( triangleArray != null )
				parentGroup.addChild( new Shape3D( triangleArray , appearance ) );

			if ( quadArray != null )
				parentGroup.addChild( new Shape3D( quadArray , appearance ) );

			_lineArray     = null;
			_lineCount     = 0;
			_triangleArray = null;
			_triangleCount = 0;
			_quadArray     = null;
			_quadCount     = 0;
		}

		private Point3d getPointCoordinate( final Matrix3D xform , final double[] pointCoords , final int pointIndex )
		{
			final int    i = pointIndex * 3;
			final double x = pointCoords[ i ];
			final double y = pointCoords[ i + 1 ];
			final double z = pointCoords[ i + 2 ];

			return new Point3d( xform.transformX( x , y , z ) , xform.transformY( x , y , z ) , xform.transformZ( x , y , z ) );
		}

		private Vector3f getVertexNormal( final Matrix3D xform , final double[] vertexNormals , final int pointIndex )
		{
			final int    i = pointIndex * 3;
			final double x = vertexNormals[ i     ];
			final double y = vertexNormals[ i + 1 ];
			final double z = vertexNormals[ i + 2 ];

			return new Vector3f( (float)xform.rotateX( x , y , z ) ,
			                     (float)xform.rotateY( x , y , z ) ,
			                     (float)xform.rotateZ( x , y , z ) );
		}

		private TexCoord2f getTextureCoordinate( final int[] textureU , final int[] textureV , final int vertexIndex )
		{
			final Texture j3dTexture = _appearance.getTexture();

			return ( j3dTexture == null ) ? new TexCoord2f() : new TexCoord2f(
				(float)textureU[ vertexIndex ] / (float)j3dTexture.getWidth()  ,
				(float)textureV[ vertexIndex ] / (float)j3dTexture.getHeight() );

		}

	}

	/**
	 * Construct builder.
	 *
	 * Please read the class comment for usage instructions.
	 */
	Shape3DBuilder()
	{
		_textureGroups     = null;
		_textureGroupCount = 0;
	}

	/**
	 * Prepare for building the specified face. This is used to determine the
	 * correct storage requirements, so no collection classes are needed.
	 *
	 * @param   face                Face to prepare for.
	 * @param   textureOverride     Texture to use instead of actual face texture.
	 * @param   extraOpacity        Extra face opacity (0.0=translucent, 1.0=opaque).
	 *
	 * @see     #addFace
	 */
	public void prepareFace( final Face3D face , final TextureSpec textureOverride , final float extraOpacity )
	{
		final TextureSpec texture = ( textureOverride != null ) ? textureOverride : face.getTexture();
		if ( texture != null )
		{
			final float   opacity     = extraOpacity * face.getOpacity();
			final boolean hasBackface = face.hasBackface();

			final TextureGroup group = getGroup( texture , opacity , hasBackface );
			group.prepareFace( face.getVertexCount() );
		}
	}

	/**
	 * Add the specified face. This myst be called for every face that was
	 * previously announced using {@link #prepareFace}. This will take
	 * care of the the line/triangle/quad geometry conversions.
	 *
	 * @param   xform               Transform to apply to point coordinates.
	 * @param   pointCoords         Coordinates of points referred to by the face.
	 * @param   vertexNormals       Normals of vertices referred to by the face.
	 * @param   face                Face to add.
	 * @param   textureOverride     Texture to use instead of actual face texture.
	 * @param   extraOpacity        Extra face opacity (0.0=translucent, 1.0=opaque).
	 *
	 * @see     #prepareFace
	 * @see     #buildShapes
	 */
	public void addFace( final Matrix3D xform , final double[] pointCoords , final double[] vertexNormals , final Face3D face , final TextureSpec textureOverride , final float extraOpacity )
	{
		final TextureSpec texture = ( textureOverride != null ) ? textureOverride : face.getTexture();
		if ( texture != null )
		{
			final float   opacity     = extraOpacity * face.getOpacity();
			final boolean hasBackface = face.hasBackface();

			final TextureGroup group = getGroup( texture , opacity , hasBackface );
			group.addFace( xform , pointCoords , vertexNormals , face );
		}
	}

	/**
	 * Build shapes. This will build the {@link Shape3D} objects from the
	 * geometry data collected by the {@link #addFace} method, and add
	 * them to a supplied {@link BranchGroup} node.
	 *
	 * @param   result  Branch group to add shapes to.
	 *
	 * @see     #prepareFace
	 * @see     #addFace
	 */
	public void buildShapes( final BranchGroup result )
	{
		final int groupCount = _textureGroupCount;

		for ( int i = 0 ; i < groupCount ; i++ )
			_textureGroups[ i ].buildShapes( result );
	}

	private TextureGroup getGroup( final TextureSpec texture , final float opacity , final boolean hasBackface )
	{
		TextureGroup result = null;

		final int            alpha  = Math.max( 0 , Math.min( Math.round( opacity * 255.0f ) , 255 ) );
		final TextureGroup[] groups = _textureGroups;
		final int            count  = _textureGroupCount;

		for ( int i = 0 ; i < count ; i++ )
		{
			final TextureGroup group = groups[ i ];
			if ( ( group._texture == texture ) && ( group._alpha == alpha ) && ( group._hasBackface == hasBackface ) )
			{
				result = group;
				break;
			}
		}

		if ( result == null )
		{
			final TextureGroup[] newGroups;
			if ( groups == null )
			{
				newGroups = new TextureGroup[ 8 ];
			}
			else if ( count >= groups.length )
			{
				newGroups = new TextureGroup[ groups.length * 2 ];
				System.arraycopy( groups , 0 , newGroups , 0 , count );
			}
			else
			{
				newGroups = groups;
			}

			final Java3dTools tools      = Java3dTools.getInstance();
			final Appearance  appearance = tools.getAppearance( texture , opacity , hasBackface );

			result = new TextureGroup( texture , alpha , hasBackface , appearance );
			newGroups[ count ] = result;

			_textureGroups     = newGroups;
			_textureGroupCount = count + 1;
		}

		return result;
	}
}
