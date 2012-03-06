/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.awt.view.java3d;

import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import javax.vecmath.Vector3f;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.model.Face3D.*;
import org.jetbrains.annotations.*;

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
 *     Call the {@link #getAppearanceGroup} for each {@link FaceGroup}.
 *   </li>
 *   <li>
 *     Call the {@link AppearanceGroup#prepareFace} method for every face that
 *     will be added later. This is used to determine correct storage
 *     requirements, so no collection classes are needed.
 *   </li>
 *   <li>
 *     Call the {@link AppearanceGroup#addFace} method for every face that was
 *     previously announced using {@link AppearanceGroup#prepareFace}. This will
 *     take care of the the line/triangle/quad geometry conversions.
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
	 * converting {@link Object3D} instances.
	 *
	 * @param   objectNodes Collection of nodes to create the branch group from.
	 *
	 * @return  {@link BranchGroup} containing the created content graph.
	 */
	public static BranchGroup createBranchGroup( final Collection<Node3DPath> objectNodes )
	{
		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		result.setCapability( BranchGroup.ALLOW_DETACH );

		if ( ( objectNodes != null ) && !objectNodes.isEmpty() )
		{
			final Shape3DBuilder shapeBuilder = new Shape3DBuilder();

			for ( final Node3DPath path : objectNodes )
			{
				final Object3D object3d = (Object3D)path.getNode();
				for ( final FaceGroup faceGroup : object3d.getFaceGroups() )
				{
					final AppearanceGroup appearanceGroup = shapeBuilder.getAppearanceGroup( faceGroup );
					if ( appearanceGroup != null )
					{
						for ( final Face3D face : faceGroup.getFaces() )
						{
							appearanceGroup.prepareFace( face.getVertexCount() );
						}
					}
				}
			}

			for ( final Node3DPath path : objectNodes )
			{
				final Object3D object3d = (Object3D)path.getNode();
				final Matrix3D transform = path.getTransform();

				for ( final FaceGroup faceGroup : object3d.getFaceGroups() )
				{
					final AppearanceGroup appearanceGroup = shapeBuilder.getAppearanceGroup( faceGroup );
					if ( appearanceGroup != null )
					{
						for ( final Face3D face : faceGroup.getFaces() )
						{
							appearanceGroup.addFace( transform, face );
						}
					}
				}
			}

			shapeBuilder.buildShapes( result );
		}

		return result;
	}

	/**
	 * Create a Java 3D {@link BranchGroup} containing a content graph by
	 * converting an {@link Object3D} instance using this builder.
	 *
	 * @param   object2branch       Transform to apply to vertices.
	 * @param   object3d    Object3D to convert.
	 *
	 * @return  {@link BranchGroup} containing the created content graph.
	 */
	public static BranchGroup createBranchGroup( final Matrix3D object2branch, final Object3D object3d )
	{
		final Shape3DBuilder shapeBuilder = new Shape3DBuilder();

		for ( final FaceGroup faceGroup : object3d.getFaceGroups() )
		{
			final AppearanceGroup appearanceGroup = shapeBuilder.getAppearanceGroup( faceGroup );
			if ( appearanceGroup != null )
			{
				for ( final Face3D face : faceGroup.getFaces() )
				{
					appearanceGroup.prepareFace( face.getVertexCount() );
				}
			}
		}

		for ( final FaceGroup faceGroup : object3d.getFaceGroups() )
		{
			final AppearanceGroup appearanceGroup = shapeBuilder.getAppearanceGroup( faceGroup );
			if ( appearanceGroup != null )
			{
				for ( final Face3D face : faceGroup.getFaces() )
				{
					appearanceGroup.addFace( object2branch, face );
				}
			}
		}

		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		result.setCapability( BranchGroup.ALLOW_DETACH );
		shapeBuilder.buildShapes( result );
		return result;
	}

	/**
	 * Appearance groups. A appearance group contains shapes that share the same
	 * apperance.
	 */
	private final List<AppearanceGroup> _appearanceGroups = new ArrayList<AppearanceGroup>();

	/**
	 * This is the workhorse of the builder. It contains a set of shapes that
	 * share the same appearance. It translates faces with any number of vertices
	 * to lines, triangles, and quads as defined by the Java 3D API.
	 *
	 * IMPORTANT: This class should be used following a strict recipe:
	 * <ol>
	 *   <li>
	 *     Construct the group with the correct {@link Appearance}.
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
	 *
	 * @noinspection JavaDoc
	 */
	private static class AppearanceGroup
	{
		private final ab.j3d.appearance.Appearance _material;

		private final boolean _hasBackface;

		private final Appearance _appearance;

		private int _lineCount;

		private LineArray _lineArray;

		private int _triangleCount;

		private TriangleArray _triangleArray;

		private int _quadCount;

		private QuadArray _quadArray;

		AppearanceGroup( final ab.j3d.appearance.Appearance material, final boolean hasBackface, final Appearance appearance )
		{
			_material      = material;
			_hasBackface   = hasBackface;
			_appearance    = appearance;
			_lineCount     = 0;
			_lineArray     = null;
			_triangleCount = 0;
			_triangleArray = null;
			_quadCount     = 0;
			_quadArray     = null;
		}

		void prepareFace( final int numberOfVerticesInFace )
		{
			if ( numberOfVerticesInFace == 2 )
			{
				_lineCount++;
			}
			else if ( numberOfVerticesInFace > 2 )
			{
				final int numberOfQuads = ( numberOfVerticesInFace - 2 ) / 2;
				if ( numberOfQuads > 0 )
				{
					_quadCount += numberOfQuads;
				}

				if ( ( numberOfVerticesInFace % 2 ) != 0 )
				{
					_triangleCount++;
				}
			}
		}

		void addFace( final Matrix3D object2view, final Face3D face )
		{
			final List<Vertex> vertices = face.getVertices();
			final int vertexCount = vertices.size();
			if ( vertexCount >= 2 )
			{
				if ( vertexCount == 2 )
				{
					LineArray lineArray = _lineArray;
					int       lineCount = _lineCount;

					if ( lineArray == null )
					{
						if ( lineCount == 0 )
						{
							throw new IllegalStateException( "should have called prepareFace()!" );
						}

						lineArray = new LineArray( 2 * lineCount, LineArray.COORDINATES );
						lineCount = 0;
					}

					final int coordinateIndex = lineCount * 2;
					lineArray.setCoordinate( coordinateIndex    , getVertexCoordinate( object2view, vertices.get( 0 ) ) );
					lineArray.setCoordinate( coordinateIndex + 1, getVertexCoordinate( object2view, vertices.get( 1 ) ) );

					_lineArray = lineArray;
					_lineCount = lineCount + 1;
				}
				else // vertexCount > 2
				{
					Vertex vertex = vertices.get( 0 );

					final Point3d       vertexCoord0  = getVertexCoordinate( object2view, vertex );
					final TexCoord2f    textureCoord0 = new TexCoord2f( vertex.colorMapU, vertex.colorMapV );
					final Vector3f      vertexNormal0 = getVertexNormal( object2view, face, 0 );

					vertex = vertices.get( 1 );

					Point3d    vertexCoord1  = getVertexCoordinate( object2view, vertex );
					TexCoord2f textureCoord1 = new TexCoord2f( vertex.colorMapU, vertex.colorMapV );
					Vector3f   vertexNormal1 = getVertexNormal( object2view, face, 1 );

					if ( vertexCount > 3 ) // more than 3 vertices => add quads
					{
						QuadArray quadArray = _quadArray;
						int       quadCount = _quadCount;

						if ( quadArray == null )
						{
							if ( quadCount == 0 )
							{
								throw new IllegalStateException( "should have called prepareFace()!" );
							}

							quadArray = new QuadArray( 4 * quadCount, QuadArray.COORDINATES | QuadArray.NORMALS | QuadArray.TEXTURE_COORDINATE_2 );
							quadCount = 0;
						}

						for ( int vertexIndex3 = 3 ; vertexIndex3 < vertexCount ; vertexIndex3 += 2 )
						{
							vertex = vertices.get( vertexIndex3 - 1 );
							final Point3d    vertexCoord2  = getVertexCoordinate( object2view, vertex );
							final TexCoord2f textureCoord2 = new TexCoord2f( vertex.colorMapU, vertex.colorMapV );
							final Vector3f   vertexNormal2 = getVertexNormal( object2view, face, vertexIndex3 - 1 );

							vertex = vertices.get( vertexIndex3 );
							final Point3d    vertexCoord3  = getVertexCoordinate( object2view, vertex );
							final TexCoord2f textureCoord3 = new TexCoord2f( vertex.colorMapU, vertex.colorMapV );
							final Vector3f   vertexNormal3 = getVertexNormal( object2view, face, vertexIndex3 );

							final int coordinateIndex = quadCount * 4;

							quadArray.setCoordinate( coordinateIndex    , vertexCoord3 );
							quadArray.setCoordinate( coordinateIndex + 1, vertexCoord2 );
							quadArray.setCoordinate( coordinateIndex + 2, vertexCoord1 );
							quadArray.setCoordinate( coordinateIndex + 3, vertexCoord0 );

							quadArray.setTextureCoordinate( 0, coordinateIndex    , textureCoord3 );
							quadArray.setTextureCoordinate( 0, coordinateIndex + 1, textureCoord2 );
							quadArray.setTextureCoordinate( 0, coordinateIndex + 2, textureCoord1 );
							quadArray.setTextureCoordinate( 0, coordinateIndex + 3, textureCoord0 );

							quadArray.setNormal( coordinateIndex    , vertexNormal3 );
							quadArray.setNormal( coordinateIndex + 1, vertexNormal2 );
							quadArray.setNormal( coordinateIndex + 2, vertexNormal1 );
							quadArray.setNormal( coordinateIndex + 3, vertexNormal0 );

							vertexCoord1  = vertexCoord3;
							vertexNormal1 = vertexNormal3;
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
							{
								throw new IllegalStateException( "should have called prepareFace()!" );
							}

							triangleArray = new TriangleArray( 3 * triangleCount, TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.TEXTURE_COORDINATE_2 );
							triangleCount = 0;
						}

						vertex = vertices.get( vertexCount - 1 );
						final Point3d    vertexCoord2  = getVertexCoordinate( object2view, vertex );
						final Vector3f   vertexNormal2 = getVertexNormal( object2view, face, vertexCount - 1 );
						final TexCoord2f textureCoord2 = new TexCoord2f( vertex.colorMapU, vertex.colorMapV );

						final int coordinateIndex = triangleCount * 3;

						triangleArray.setCoordinate( coordinateIndex    , vertexCoord2 );
						triangleArray.setCoordinate( coordinateIndex + 1, vertexCoord1 );
						triangleArray.setCoordinate( coordinateIndex + 2, vertexCoord0 );

						triangleArray.setTextureCoordinate( 0, coordinateIndex    , textureCoord2 );
						triangleArray.setTextureCoordinate( 0, coordinateIndex + 1, textureCoord1 );
						triangleArray.setTextureCoordinate( 0, coordinateIndex + 2, textureCoord0 );

						triangleArray.setNormal( coordinateIndex    , vertexNormal2 );
						triangleArray.setNormal( coordinateIndex + 1, vertexNormal1 );
						triangleArray.setNormal( coordinateIndex + 2, vertexNormal0 );

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
			{
				parentGroup.addChild( new Shape3D( lineArray, appearance ) );
			}

			if ( triangleArray != null )
			{
				parentGroup.addChild( new Shape3D( triangleArray, appearance ) );
			}

			if ( quadArray != null )
			{
				parentGroup.addChild( new Shape3D( quadArray, appearance ) );
			}

			_lineArray     = null;
			_lineCount     = 0;
			_triangleArray = null;
			_triangleCount = 0;
			_quadArray     = null;
			_quadCount     = 0;
		}

		private static Point3d getVertexCoordinate( final Matrix3D xform, final Vertex vertex )
		{
			final Vector3D point = vertex.point;
			return new Point3d( xform.transformX( point ), xform.transformY( point ), xform.transformZ( point ) );
		}

		private static Vector3f getVertexNormal( final Matrix3D object2view, final Face3D face, final int vertexIndex )
		{
			final Vector3D normal = face.getVertexNormal( vertexIndex );
			final Vector3D rotatedNormal = object2view.rotate( normal );
			return new Vector3f( (float)rotatedNormal.x, (float)rotatedNormal.y, (float)rotatedNormal.z );
		}
	}

	/**
	 * Construct builder.
	 *
	 * Please read the class comment for usage instructions.
	 */
	Shape3DBuilder()
	{
	}

	/**
	 * Build shapes. This will build the {@link Shape3D} objects from the
	 * geometry data collected in the {@link AppearanceGroup}s, and add
	 * them to a supplied {@link BranchGroup} node.
	 *
	 * @param   result  Branch group to add shapes to.
	 *
	 * @see     #getAppearanceGroup
	 * @see     AppearanceGroup#prepareFace
	 * @see     AppearanceGroup#addFace
	 */
	public void buildShapes( final BranchGroup result )
	{
		for ( final AppearanceGroup appearanceGroup : _appearanceGroups )
		{
			appearanceGroup.buildShapes( result );
		}
	}

	/**
	 * Get {@link AppearanceGroup} for specified material and culling mode.
	 *
	 * @param   faceGroup   {@link FaceGroup} to get {@link AppearanceGroup} for.
	 *
	 * @return  {@link AppearanceGroup}.
	 */
	@Nullable
	private AppearanceGroup getAppearanceGroup( final FaceGroup faceGroup )
	{
		AppearanceGroup result = null;

		final ab.j3d.appearance.Appearance abAppearance = faceGroup.getAppearance();
		if ( abAppearance != null )
		{
			final boolean hasBackface = faceGroup.isTwoSided();

			final List<AppearanceGroup> appearanceGroups = _appearanceGroups;

			for ( final AppearanceGroup appearanceGroup : appearanceGroups )
			{
				if ( ( appearanceGroup._material == abAppearance ) && ( appearanceGroup._hasBackface == hasBackface ) )
				{
					result = appearanceGroup;
					break;
				}
			}

			if ( result == null )
			{
				final Java3dTools tools = Java3dTools.getInstance();
				final Appearance appearance = tools.getAppearance( abAppearance, 1.0f, hasBackface );

				result = new AppearanceGroup( abAppearance, hasBackface, appearance );
				appearanceGroups.add( result );
			}
		}

		return result;
	}

}
