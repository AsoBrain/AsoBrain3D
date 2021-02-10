/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class can be used to create an {@link Object3D} instance.
 *
 * @author HRM Bleumink
 * @author G.B.M. Rupert
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "OverlyComplexMethod", "DuplicatedCode" } )
public class Object3DBuilder
{
	/**
	 * The {@link Object3D} being build.
	 */
	@NotNull
	private final Object3D _target;

	/**
	 * Construct builder.
	 */
	public Object3DBuilder()
	{
		_target = new Object3D();
	}

	/**
	 * Construct builder.
	 *
	 * @param target Target object.
	 */
	public Object3DBuilder( @NotNull final Object3D target )
	{
		this( target, true );
	}

	/**
	 * Construct builder from existing 3D object.
	 *
	 * @param object Existing 3D object.
	 * @param copy   {@code true} to create new object copy; {@code false} to
	 *               modify existing object.
	 */
	Object3DBuilder( @NotNull final Object3D object, final boolean copy )
	{
		final Object3D target;
		if ( copy )
		{
			target = new Object3D( object.getVertexCoordinates() );

			for ( final FaceGroup originalFaceGroup : object.getFaceGroups() )
			{
				final FaceGroup faceGroup = new FaceGroup( originalFaceGroup.getAppearance(), originalFaceGroup.isSmooth(), originalFaceGroup.isTwoSided() );
				target.addFaceGroup( faceGroup );

				for ( final Face3D face : originalFaceGroup.getFaces() )
				{
					faceGroup.addFace( face );
				}
			}
		}
		else
		{
			target = object;
		}
		_target = target;
	}

	/**
	 * Add {@link Object3D} content to this object.
	 *
	 * @param object Object to add to this object.
	 */
	public void addObject( @NotNull final Object3D object )
	{
		for ( final FaceGroup originalFaceGroup : object.getFaceGroups() )
		{
			// try to reuse existing 'FaceGroup'; create new one if necessary
			final boolean smooth = originalFaceGroup.isSmooth();
			final boolean twoSided = originalFaceGroup.isTwoSided();
			final Appearance appearance = originalFaceGroup.getAppearance();

			FaceGroup faceGroup = null;
			for ( final FaceGroup existingFaceGroup : _target.getFaceGroups() )
			{
				if ( ( existingFaceGroup.isSmooth() == smooth ) && ( existingFaceGroup.isTwoSided() == twoSided ) && ( ( appearance == null ) ? ( existingFaceGroup.getAppearance() == null ) : appearance.equals( existingFaceGroup.getAppearance() ) ) )
				{
					faceGroup = existingFaceGroup;
				}
			}

			if ( faceGroup == null )
			{
				faceGroup = new FaceGroup( appearance, smooth, twoSided );
				_target.addFaceGroup( faceGroup );
			}

			// clone faces
			for ( final Face3D originalFace : originalFaceGroup.getFaces() )
			{
				final List<Vertex3D> originalVertices = originalFace.getVertices();
				final List<Vertex3D> vertices = new ArrayList<>( originalVertices.size() );
				for ( final Vertex3D originalVertex : originalVertices )
				{
					vertices.add( new Vertex3D( originalVertex.point, originalVertex.normal, _target.getVertexIndex( originalVertex.point ), originalVertex.colorMapU, originalVertex.colorMapV ) );
				}

				faceGroup.addFace( new Face3D( originalFace.getNormal(), originalFace.getDistance(), vertices, originalFace.getTessellation() ) );
			}
		}
	}

	/**
	 * Get {@link Node3D} that was built.
	 *
	 * @return The {@link Node3D} that was built.
	 */
	@NotNull
	public Object3D getObject3D()
	{
		return _target;
	}

	/**
	 * Get index of vertex at the specified point. If no vertex was found at the
	 * specified point, a new one is created.
	 *
	 * @param point Point to get vertex index of.
	 *
	 * @return Vertex index.
	 */
	public int getVertexIndex( @NotNull final Vector3D point )
	{
		return _target.getVertexIndex( point );
	}

	/**
	 * Get number of vertices in object.
	 *
	 * @return Number of vertices.
	 */
	public int getVertexCount()
	{
		return _target.getVertexCount();
	}

	/**
	 * Set coordinates of all vertices in this object. This can only be used
	 * safely if no faces have been added yet, since the object integrity may
	 * otherwise be compromised.
	 *
	 * @param vertexCoordinates Vertex coordinates.
	 */
	public void setVertexCoordinates( @NotNull final Collection<Vector3D> vertexCoordinates )
	{
		_target.setVertexCoordinates( vertexCoordinates );
	}

	/**
	 * Add arc.
	 *
	 * @param centerPoint Center-point of circle on which the arc is defined.
	 * @param radius      Radius of circle on which the arc is defined.
	 * @param startAngle  Start-angle of arc relative to X-axis (radians).
	 * @param endAngle    End-angle of arc relative to X-axis (radians).
	 * @param startWidth  Start-width of arc (0.0 => no width).
	 * @param endWidth    End-width of arc (0.0 => no width).
	 * @param extrusion   Extrusion to apply ({@code null} or 0-vector => no
	 *                    extrusion).
	 * @param appearance  Appearance specification to use for shading.
	 * @param fill        Create filled shape vs. create wireframe.
	 */
	public void addArc( @NotNull final Vector3D centerPoint, final double radius, final double startAngle, final double endAngle, final double startWidth, final double endWidth, @Nullable final Vector3D extrusion, @Nullable final Appearance appearance, final boolean fill )
	{
		final double twoPI = 2.0 * Math.PI;

		double enclosedAngle = ( endAngle - startAngle ) % twoPI;
		if ( enclosedAngle < 0.0 )
		{
			enclosedAngle += twoPI;
		}

		final int nrSegments = ( enclosedAngle < Math.PI / 4.0 ) ? 3 : (int)( ( 32.0 * enclosedAngle ) / twoPI + 0.5 );
		final double angleStep = enclosedAngle / (double)nrSegments;
		final boolean extruded = ( extrusion != null ) && !extrusion.almostEquals( Vector3D.ZERO );

		if ( ( startWidth == 0.0 ) && ( endWidth == 0.0 ) )
		{
			double angle = startAngle;
			double cos = Math.cos( angle );
			double sin = Math.sin( angle );

			Vector3D point1 = centerPoint.plus( radius * cos, radius * sin, 0.0 );

			for ( int i = 0; i < nrSegments; i++ )
			{
				angle += angleStep;
				cos = Math.cos( angle );
				sin = Math.sin( angle );

				final Vector3D point2 = centerPoint.plus( radius * cos, radius * sin, 0.0 );

				if ( extruded )
				{
					final Vector3D point1a = point1.plus( extrusion );
					final Vector3D point2a = point2.plus( extrusion );

					addQuad( point1, point2, point2a, point1a, null, appearance, fill, true );
				}
				else
				{
					addLine( point1, point2, appearance );
				}

				point1 = point2;
			}
		}
		else
		{
			final double radiusStep = ( endWidth - startWidth ) / (double)( 2 * nrSegments );

			double angle = startAngle;
			double cos = Math.cos( angle );
			double sin = Math.sin( angle );
			double innerRadius = radius - startWidth / 2.0;
			double outerRadius = innerRadius + startWidth;

			Vector3D inner1 = centerPoint.plus( innerRadius * cos, innerRadius * sin, 0.0 );
			Vector3D outer1 = centerPoint.plus( outerRadius * cos, outerRadius * sin, 0.0 );

			for ( int i = 0; i < nrSegments; i++ )
			{
				angle += angleStep;
				innerRadius -= radiusStep;
				outerRadius += radiusStep;
				cos = Math.cos( angle );
				sin = Math.sin( angle );

				final Vector3D inner2 = centerPoint.plus( innerRadius * cos, innerRadius * sin, 0.0 );
				final Vector3D outer2 = centerPoint.plus( outerRadius * cos, outerRadius * sin, 0.0 );

				if ( extruded )
				{
					final Vector3D extrudedInner1 = inner1.plus( extrusion );
					final Vector3D extrudedOuter1 = outer1.plus( extrusion );
					final Vector3D extrudedOuter2 = outer2.plus( extrusion );
					final Vector3D extrudedInner2 = inner2.plus( extrusion );

					final boolean isFirst = ( i == 0 );
					final boolean isLast = ( i == ( nrSegments - 1 ) );

					addQuad( outer1, outer2, inner2, inner1, null, appearance, fill, true );

					if ( isFirst )
					{
						addQuad( outer1, inner1, extrudedInner1, extrudedOuter1, null, appearance, fill, true );
					}

					addQuad( inner1, inner2, extrudedInner2, extrudedInner1, null, appearance, fill, true );

					addQuad( inner2, outer2, extrudedOuter2, extrudedInner2, null, appearance, fill, true );

					if ( isLast )
					{
						addQuad( outer2, outer1, extrudedOuter1, extrudedOuter2, null, appearance, fill, true );
					}

					addQuad( extrudedOuter1, extrudedInner1, extrudedInner2, extrudedOuter2, null, appearance, fill, true );
				}
				else
				{
					addQuad( outer1, inner1, inner2, outer2, null, appearance, fill, true );
				}

				inner1 = inner2;
				outer1 = outer2;
			}
		}
	}

	/**
	 * Add cylinder primitive.
	 *
	 * @param origin              Origin of cylinder.
	 * @param direction           Direction of cylinder.
	 * @param height              Height of cylinder (z-axis).
	 * @param radiusBottom        Radius at bottom (z=0).
	 * @param radiusTop           Radius at top (z=height).
	 * @param numEdges            Number of edges to approximate circle
	 *                            (minimum: 3).
	 * @param sideAppearance      Appearance of cylinder circumference.
	 * @param sideMap             UV map to use for circumference.
	 * @param smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param topAppearance       Appearance for top cap ({@code null} => no
	 *                            cap).
	 * @param topMap              UV map for top cap.
	 * @param bottomAppearance    Appearance for bottom cap ({@code null} => no
	 *                            cap).
	 * @param bottomMap           UV map for bottom cap.
	 * @param flipNormals         If set, flip normals.
	 */
	public void addCylinder( @NotNull final Vector3D origin, @NotNull final Vector3D direction, final double height, final double radiusBottom, final double radiusTop, final int numEdges, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		final Matrix3D base = Matrix3D.getPlaneTransform( origin, direction, true );

		/*
		 * Setup properties of cylinder.
		 */
		final boolean hasBottom = ( radiusBottom > 0.0 );
		final boolean hasTop = ( radiusTop > 0.0 );
		final int vertexCount = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final int[] vertexIndices = new int[ vertexCount ];

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			for ( int i = 0; i < numEdges; i++ )
			{
				final double rad = (double)i * radStep;
				vertexIndices[ v++ ] = getVertexIndex( base.transform( Math.sin( rad ) * radiusBottom, -Math.cos( rad ) * radiusBottom, 0.0 ) );
			}
		}
		else
		{
			vertexIndices[ v++ ] = getVertexIndex( origin );
		}

		if ( hasTop )
		{
			for ( int i = 0; i < numEdges; i++ )
			{
				final double rad = (double)i * radStep;
				vertexIndices[ v++ ] = getVertexIndex( base.transform( Math.sin( rad ) * radiusTop, -Math.cos( rad ) * radiusTop, height ) );
			}
		}
		else
		{
			vertexIndices[ v/*++*/ ] = getVertexIndex( base.transform( 0.0, 0.0, height ) );
		}

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom && ( bottomAppearance != null ) )
		{
			final int[] faceVertices = new int[ numEdges ];
			for ( int i = 0; i < numEdges; i++ )
			{
				faceVertices[ i ] = flipNormals ? ( numEdges - 1 - i ) : i;
			}

			addFace( faceVertices, bottomAppearance, bottomMap, false, false, false );
		}

		/*
		 * Circumference.
		 */
		if ( hasTop || hasBottom )
		{
			for ( int i1 = 0; i1 < numEdges; i1++ )
			{
				final int i2 = ( i1 + 1 ) % numEdges;

				final int[] faceVertices;

				if ( !hasTop )
				{
					faceVertices = flipNormals ? new int[] { vertexIndices[ numEdges ], vertexIndices[ i1 ], vertexIndices[ i2 ] } : new int[] { vertexIndices[ i2 ], vertexIndices[ i1 ], vertexIndices[ numEdges ] };
				}
				else if ( !hasBottom )
				{
					faceVertices = flipNormals ? new int[] { vertexIndices[ 1 + i2 ], vertexIndices[ 1 + i1 ], vertexIndices[ 0 ] } : new int[] { vertexIndices[ 0 ], vertexIndices[ 1 + i1 ], vertexIndices[ 1 + i2 ] };
				}
				else
				{
					faceVertices = flipNormals ? new int[] { vertexIndices[ numEdges + i2 ], vertexIndices[ numEdges + i1 ], vertexIndices[ i1 ], vertexIndices[ i2 ] } : new int[] { vertexIndices[ i2 ], vertexIndices[ i1 ], vertexIndices[ numEdges + i1 ], vertexIndices[ numEdges + i2 ] };
				}

				addFace( faceVertices, sideAppearance, sideMap, false, smoothCircumference, false );
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop && ( topAppearance != null ) )
		{
			final int[] faceVertices = new int[ numEdges ];

			final int lastVertex = ( hasBottom ? numEdges : 1 ) + numEdges - 1;

			for ( int i = 0; i < numEdges; i++ )
			{
				faceVertices[ i ] = vertexIndices[ flipNormals ? ( lastVertex - numEdges + 1 + i ) : ( lastVertex - i ) ];
			}

			addFace( faceVertices, topAppearance, topMap, false, false, false );
		}
	}

	/**
	 * Add line primitive.
	 *
	 * @param point1     First point.
	 * @param point2     Second point.
	 * @param appearance Appearance specification to use for shading.
	 */
	public void addLine( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @Nullable final Appearance appearance )
	{
		addFace( new Vector3D[] { point1, point2 }, appearance, false, true );
	}

	/**
	 * Add line.
	 *
	 * @param point1     First point.
	 * @param point2     Second point.
	 * @param extrusion  Extrusion to apply ({@code null} or 0-vector => no
	 *                   extrusion).
	 * @param appearance Appearance specification to use for shading.
	 * @param fill       Create filled shape vs. create wireframe.
	 */
	public void addLine( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @Nullable final Vector3D extrusion, @Nullable final Appearance appearance, final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.ZERO ) )
		{
			final Vector3D p1a = point1.plus( extrusion );
			final Vector3D p2a = point2.plus( extrusion );

			addQuad( point1, p1a, p2a, point2, null, appearance, fill, true );
		}
		else
		{
			addLine( point1, point2, appearance );
		}
	}

	/**
	 * Add face without texture.
	 *
	 * @param points     Vertex coordinates that define the face.
	 * @param appearance Appearance specification to use for shading.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		addFace( points, appearance, null, null, smooth, twoSided );
	}

	/**
	 * Add face without texture.
	 *
	 * @param vertexIndices Vertex indices of added face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, appearance, null, null, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param points      Vertex coordinates that define the face.
	 * @param appearance  Appearance specification to use for shading.
	 * @param uvMap       UV-map used to generate texture coordinates.
	 * @param flipTexture Flip texture direction.
	 * @param smooth      Face is smooth/curved vs. flat.
	 * @param twoSided    Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		addFace( points, appearance, uvMap, null, flipTexture, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param points        Vertex coordinates that define the face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param uvMap         UV-map used to generate texture coordinates.
	 * @param vertexNormals Normal for each vertex.
	 * @param flipTexture   Flip texture direction.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, @Nullable final Vector3D[] vertexNormals, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		final int[] vertexIndices = new int[ points.length ];
		for ( int i = 0; i < points.length; i++ )
		{
			vertexIndices[ i ] = getVertexIndex( points[ i ] );
		}

		addFace( vertexIndices, appearance, uvMap, vertexNormals, flipTexture, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param vertexIndices Vertex indices of added face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param uvMap         UV-map used to generate texture coordinates.
	 * @param flipTexture   Flip texture direction.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, appearance, uvMap, null, flipTexture, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param vertexIndices Vertex indices of added face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param uvMap         UV-map used to generate texture coordinates.
	 * @param vertexNormals Normal for each vertex.
	 * @param flipTexture   Flip texture direction.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final Vector3D[] vertexNormals, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, appearance, ( uvMap != null ) ? uvMap.generate( ( appearance == null ) ? null : appearance.getColorMap(), _target.getVertexCoordinates(), vertexIndices, flipTexture ) : null, vertexNormals, smooth, twoSided );
	}

	/**
	 * Add face.
	 *
	 * @param points        Points for vertices that define the face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param texturePoints Texture coordinates for each vertex.
	 * @param vertexNormals Normal for each vertex.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Appearance appearance, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		final int vertexCount = points.length;
		final int[] vertexIndices = new int[ vertexCount ];

		for ( int i = 0; i < vertexCount; i++ )
		{
			vertexIndices[ i ] = getVertexIndex( points[ i ] );
		}

		addFace( vertexIndices, appearance, texturePoints, vertexNormals, smooth, twoSided );
	}

	/**
	 * Add face.
	 *
	 * @param vertexIndices Vertex indices of added face.
	 * @param appearance    Appearance specification to use for shading.
	 * @param texturePoints Texture coordinates for each vertex.
	 * @param vertexNormals Normal for each vertex.
	 * @param smooth        Face is smooth/curved vs. flat.
	 * @param twoSided      Face is two-sided.
	 */
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Appearance appearance, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		_target.addFace( appearance, smooth, twoSided, new Face3D( _target, vertexIndices, texturePoints, vertexNormals ) );
	}

	/**
	 * Add quad primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param point4     Fourth vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Appearance appearance, final boolean twoSided )
	{
		addFace( new Vector3D[] {
		point1, point2, point3, point4
		}, appearance, false, twoSided );
	}

	/**
	 * Add quad primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param point4     Fourth vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param uvMap      UV-map used to generate texture coordinates.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, appearance, uvMap, false, false, twoSided );
	}

	/**
	 * Add quad primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param point4     Fourth vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param uvMap      UV-map used to generate texture coordinates.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean smooth, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, appearance, uvMap, false, smooth, twoSided );
	}

	/**
	 * Add textured quad.
	 *
	 * @param point1        First vertex coordinates.
	 * @param texturePoint1 First vertex texture coordinates.
	 * @param point2        Second vertex coordinates.
	 * @param texturePoint2 Second vertex texture coordinates.
	 * @param point3        Third vertex coordinates.
	 * @param texturePoint3 Third vertex texture coordinates.
	 * @param point4        Fourth vertex coordinates.
	 * @param texturePoint4 Fourth vertex texture coordinates.
	 * @param appearance    Appearance specification to use for shading.
	 * @param twoSided      Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector2f texturePoint1, @NotNull final Vector3D point2, @NotNull final Vector2f texturePoint2, @NotNull final Vector3D point3, @NotNull final Vector2f texturePoint3, @NotNull final Vector3D point4, @NotNull final Vector2f texturePoint4, @Nullable final Appearance appearance, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, appearance, new float[] { texturePoint1.getX(), texturePoint1.getY(), texturePoint2.getX(), texturePoint2.getY(), texturePoint3.getX(), texturePoint3.getY(), texturePoint4.getX(), texturePoint4.getY() }, null, false, twoSided );
	}

	/**
	 * Add quad with optional extrusion.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param point4     Fourth vertex coordinates.
	 * @param extrusion  Extrusion to apply ({@code null} or 0-vector => no
	 *                   extrusion).
	 * @param appearance Appearance specification to use for shading.
	 * @param fill       Create filled shape vs. create wireframe.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Vector3D extrusion, @Nullable final Appearance appearance, final boolean fill, final boolean twoSided )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.ZERO ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );
			final Vector3D point4a = point4.plus( extrusion );

			if ( fill )
			{
				addQuad( point4, point3, point2, point1, appearance, twoSided );
				addQuad( point1, point2, point2a, point1a, appearance, twoSided );
				addQuad( point2, point3, point3a, point2a, appearance, twoSided );
				addQuad( point3, point4, point4a, point3a, appearance, twoSided );
				addQuad( point4, point1, point1a, point4a, appearance, twoSided );
				addQuad( point1a, point2a, point3a, point4a, appearance, twoSided );
			}
			else
			{
				addLine( point1, point1a, appearance );
				addLine( point1, point2, appearance );
				addLine( point1a, point2a, appearance );

				addLine( point2, point2a, appearance );
				addLine( point2, point3, appearance );
				addLine( point2a, point3a, appearance );

				addLine( point3, point3a, appearance );
				addLine( point3, point4, appearance );
				addLine( point3a, point4a, appearance );

				addLine( point4, point4a, appearance );
				addLine( point4, point1, appearance );
				addLine( point4a, point1a, appearance );
			}
		}
		else
		{
			if ( fill )
			{
				addQuad( point1, point2, point3, point4, appearance, twoSided );
			}
			else
			{
				addLine( point1, point2, appearance );
				addLine( point2, point3, appearance );
				addLine( point3, point4, appearance );
				addLine( point4, point1, appearance );
			}
		}
	}

	/**
	 * Adds a face that appears to be a quad, but is actually subdivided into
	 * many smaller quads. This improves rendering quality for large quads when
	 * per-vertex lighting is used.
	 *
	 * <p> Example of a quad subdivided into 3 by 2 subdivisions:
	 * <pre>
	 * p4             p3
	 *   o---+---+---o
	 *   |   |   |   |
	 *   +---+---+---+
	 *   |   |   |   |
	 *   o---+---+---o
	 * p1             p2
	 * </pre>
	 *
	 * <p>This implementation uses a single triangle strip to represent all of
	 * the quads.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param point4     Fourth vertex coordinates.
	 * @param segmentsX  Number of subdivisions along the local X-axis.
	 * @param segmentsY  Number of subdivisions along the local Y-axis.
	 * @param appearance Appearance specification to use for shading.
	 * @param uvMap      UV-map used to generate texture coordinates.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addSubdividedQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, final int segmentsX, final int segmentsY, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean smooth, final boolean twoSided )
	{
		final List<Vertex3D> vertices = new ArrayList<>( ( segmentsX + 1 ) * ( segmentsY + 1 ) );

		final Vector3D cross = Vector3D.cross( point3.minus( point1 ), point2.minus( point1 ) );
		final Vector3D normal = cross.normalize();
		final Vector2f textureCoordinate = new Vector2f( 0.0f, 0.0f );

		for ( int y = 0; y <= segmentsY; y++ )
		{
			final double a = (double)y / (double)segmentsY;

			final double x1 = point1.getX() * ( 1.0 - a ) + point4.getX() * a;
			final double y1 = point1.getY() * ( 1.0 - a ) + point4.getY() * a;
			final double z1 = point1.getZ() * ( 1.0 - a ) + point4.getZ() * a;

			final double x2 = point2.getX() * ( 1.0 - a ) + point3.getX() * a;
			final double y2 = point2.getY() * ( 1.0 - a ) + point3.getY() * a;
			final double z2 = point2.getZ() * ( 1.0 - a ) + point3.getZ() * a;

			for ( int x = 0; x <= segmentsX; x++ )
			{
				final double b = (double)x / (double)segmentsX;

				final Vector3D point = new Vector3D( x1 * ( 1.0 - b ) + x2 * b,
				                                     y1 * ( 1.0 - b ) + y2 * b,
				                                     z1 * ( 1.0 - b ) + z2 * b );

				if ( ( uvMap != null ) && ( appearance != null ) )
				{
					uvMap.generate( textureCoordinate, appearance.getColorMap(), point, normal, false );
				}

				final Vertex3D vertex = new Vertex3D( point, vertices.size(), textureCoordinate.getX(), textureCoordinate.getY() );
				vertex.setNormal( normal );
				vertex.vertexCoordinateIndex = getVertexIndex( point );
				vertices.add( vertex );
			}
		}

		final int[] triangleStrip = new int[ ( 2 * segmentsX + 4 ) * segmentsY - 2 ];
		int i = 0;
		for ( int y = 0; y < segmentsY; y++ )
		{
			for ( int x = 0; x <= segmentsX; x++ )
			{
				triangleStrip[ i++ ] = vertices.get( x + ( segmentsX + 1 ) * y ).vertexCoordinateIndex;
				triangleStrip[ i++ ] = vertices.get( x + ( segmentsX + 1 ) * ( y + 1 ) ).vertexCoordinateIndex;
			}

			if ( y < segmentsY - 1 )
			{
				triangleStrip[ i++ ] = vertices.get( segmentsX + ( segmentsX + 1 ) * ( y + 1 ) ).vertexCoordinateIndex;
				triangleStrip[ i++ ] = vertices.get( ( segmentsX + 1 ) * ( y + 1 ) ).vertexCoordinateIndex;
			}
		}

		final List<int[]> outline = Collections.singletonList( new int[] { 0, segmentsX, ( segmentsX + 1 ) * ( segmentsY + 1 ) - 1, ( segmentsX + 1 ) * segmentsY, 0 } );
		final Tessellation tessellation = new Tessellation( outline, Collections.singletonList( new TriangleStrip( triangleStrip ) ) );
		addFace( vertices, tessellation, appearance, smooth, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Appearance appearance, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, appearance, false, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param uvMap      UV-map used to generate texture coordinates.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, appearance, uvMap, false, false, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param appearance Appearance specification to use for shading.
	 * @param uvMap      UV-map used to generate texture coordinates.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean smooth, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, appearance, uvMap, false, smooth, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param point1        First vertex coordinates.
	 * @param texturePoint1 First vertex texture coordinates.
	 * @param point2        Second vertex coordinates.
	 * @param texturePoint2 Second vertex texture coordinates.
	 * @param point3        Third vertex coordinates.
	 * @param texturePoint3 Third vertex texture coordinates.
	 * @param appearance    Appearance specification to use for shading.
	 * @param twoSided      Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector2f texturePoint1, @NotNull final Vector3D point2, @NotNull final Vector2f texturePoint2, @NotNull final Vector3D point3, @NotNull final Vector2f texturePoint3, @Nullable final Appearance appearance, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, appearance, new float[] { texturePoint1.getX(), texturePoint1.getY(), texturePoint2.getX(), texturePoint2.getY(), texturePoint3.getX(), texturePoint3.getY() }, null, false, twoSided );
	}

	/**
	 * Add triangle with optional extrusion.
	 *
	 * @param point1     First vertex coordinates.
	 * @param point2     Second vertex coordinates.
	 * @param point3     Third vertex coordinates.
	 * @param extrusion  Extrusion to apply ({@code null} or 0-vector => no
	 *                   extrusion).
	 * @param appearance Appearance specification to use for shading.
	 * @param fill       Create filled shape vs. create wireframe.
	 * @param twoSided   Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Vector3D extrusion, @Nullable final Appearance appearance, final boolean fill, final boolean twoSided )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.ZERO ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );

			if ( fill )
			{
				addTriangle( point3, point2, point1, appearance, false );
				addQuad( point1, point2, point2a, point1a, appearance, false );
				addQuad( point2, point3, point3a, point2a, appearance, false );
				addQuad( point3, point1, point1a, point3a, appearance, false );
				addTriangle( point1a, point2a, point3a, appearance, false );
			}
			else
			{
				addLine( point1, point1a, appearance );
				addLine( point1, point2, appearance );
				addLine( point1a, point2a, appearance );

				addLine( point2, point2a, appearance );
				addLine( point2, point3, appearance );
				addLine( point2a, point3a, appearance );

				addLine( point3, point3a, appearance );
				addLine( point3, point1, appearance );
				addLine( point3a, point1a, appearance );
			}
		}
		else
		{
			if ( fill )
			{
				addTriangle( point1, point2, point3, appearance, twoSided );
			}
			else
			{
				addLine( point1, point2, appearance );
				addLine( point2, point3, appearance );
				addLine( point3, point1, appearance );
			}
		}
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested, the
	 * ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used to
	 * rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for Z!
	 *
	 * @param transform           Optional transform to apply to points.
	 * @param radii               X coordinates of 2D outline.
	 * @param zCoordinates        Z coordinates of 2D outline.
	 * @param detail              Number of segments around the Y-axis.
	 * @param appearance          Appearance specification to use for shading.
	 * @param smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param closeEnds           Close ends of shape (make solid).
	 */
	public void addRotatedObject( @Nullable final Matrix3D transform, final double[] radii, final double[] zCoordinates, final int detail, @Nullable final Appearance appearance, final boolean smoothCircumference, final boolean closeEnds )
	{
		int[] prevVertexIndices = null;

		for ( int i = 0; i < radii.length; i++ )
		{
			final double radius = radii[ i ];
			final double z = zCoordinates[ i ];

			/*
			 * Based on 'radius', create a list of vertex indices at this point.
			 */
			final int[] vertexIndices;
			if ( MathTools.almostEqual( radius, 0.0 ) )
			{
				final Vector3D point = ( transform != null ) ? transform.transform( 0.0, 0.0, z ) : new Vector3D( 0.0, 0.0, z );
				vertexIndices = new int[] { getVertexIndex( point ) };
			}
			else
			{
				vertexIndices = new int[ detail ];

				final double stepSize = 2.0 * Math.PI / (double)detail;
				for ( int step = 0; step < detail; step++ )
				{
					final double angle = (double)step * stepSize;
					final double x = Math.sin( angle ) * radius;
					final double y = -Math.cos( angle ) * radius;

					final Vector3D point = ( transform != null ) ? transform.transform( x, y, z ) : new Vector3D( x, y, z );
					vertexIndices[ step ] = getVertexIndex( point );
				}

				if ( closeEnds )
				{
					if ( i == 0 )
					{
						addFace( vertexIndices, appearance, false, false );
					}
					else if ( i == radii.length - 1 )
					{
						final int[] reversed = new int[ detail ];
						for ( int step = 0; step < detail; step++ )
						{
							reversed[ step ] = vertexIndices[ detail - 1 - step ];
						}

						addFace( reversed, appearance, false, false );
					}
				}
			}

			/*
			 * Construct faces between this and the previous 'row'.
			 */
			if ( prevVertexIndices != null )
			{
				if ( vertexIndices.length > 1 )
				{
					if ( prevVertexIndices.length > 1 )
					{
						for ( int step = 0; step < detail; step++ )
						{
							final int nextStep = ( step + 1 ) % detail;
							addFace( new int[] { prevVertexIndices[ step ], vertexIndices[ step ], vertexIndices[ nextStep ], prevVertexIndices[ nextStep ] }, appearance, smoothCircumference, false );
						}
					}
					else
					{
						for ( int step = 0; step < detail; step++ )
						{
							addFace( new int[] { prevVertexIndices[ 0 ], vertexIndices[ step ], vertexIndices[ ( step + 1 ) % detail ] }, appearance, smoothCircumference, false );
						}
					}
				}
				else if ( prevVertexIndices.length > 1 )
				{
					for ( int step = 0; step < detail; step++ )
					{
						addFace( new int[] { prevVertexIndices[ step ], vertexIndices[ 0 ], prevVertexIndices[ ( step + 1 ) % detail ] }, appearance, smoothCircumference, false );
					}
				}
			}
			prevVertexIndices = vertexIndices;
		}
	}

	/**
	 * Add extruded shape with caps.
	 *
	 * @param tessellator       Tessellator that defines the shape.
	 * @param extrusion         Extrusion vector (control-point displacement).
	 * @param extrusionLines    Include extrusion (out)lines.
	 * @param transform         Transform to apply.
	 * @param top               {@code true} to include a top face.
	 * @param topAppearance     Appearance to apply to the top cap.
	 * @param topMap            Provides UV coordinates for top cap.
	 * @param topFlipTexture    Whether the top texture direction is flipped.
	 * @param bottom            {@code true} to include a bottom face.
	 * @param bottomAppearance  Appearance to apply to the bottom cap.
	 * @param bottomMap         Provides UV coordinates for bottom cap.
	 * @param bottomFlipTexture Whether the bottom texture direction is
	 *                          flipped.
	 * @param side              {@code true} to include side faces.
	 * @param sideAppearance    Appearance to apply to the extruded sides.
	 * @param sideMap           Provides UV coordinates for extruded sides.
	 * @param sideFlipTexture   Whether the side texture direction is flipped.
	 * @param twoSided          Flag to indicate if extruded faces are
	 *                          two-sided.
	 * @param flipNormals       If {@code true}, normals are flipped to point in
	 *                          the opposite direction.
	 * @param smooth            Shape is smooth.
	 *
	 * @throws IllegalArgumentException if the shape is not extruded along the
	 * z-axis.
	 */
	public void addExtrudedShape( @NotNull final Tessellator tessellator, @NotNull final Vector3D extrusion, final boolean extrusionLines, @NotNull final Matrix3D transform, final boolean top, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, final boolean topFlipTexture, final boolean bottom, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean bottomFlipTexture, final boolean side, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean sideFlipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		final boolean flipVertexOrder = flipNormals ^ ( extrusion.getZ() < 0.0 );
		final Matrix3D topTransform = transform.plus( transform.rotate( extrusion ) );
		addExtrudedShape( tessellator, flipVertexOrder, transform, topTransform, extrusionLines, top, topAppearance, topMap, topFlipTexture, bottom, bottomAppearance, bottomMap, bottomFlipTexture, side, sideAppearance, sideMap, sideFlipTexture, twoSided, flipNormals, smooth );
	}

	/**
	 * Add extruded shape with caps.
	 *
	 * @param tessellator       Tessellator that defines the shape.
	 * @param flipVertexOrder   Flip (counter-)clockwise vertex order.
	 * @param bottomTransform   Transforms 2D points to bottom face points.
	 * @param topTransform      Transforms 2D points to top face points.
	 * @param extrusionLines    Include extrusion (out)lines.
	 * @param top               {@code true} to include a top face.
	 * @param topAppearance     Appearance to apply to the top cap.
	 * @param topMap            Provides UV coordinates for top cap.
	 * @param topFlipTexture    Whether the top texture direction is flipped.
	 * @param bottom            {@code true} to include a bottom face.
	 * @param bottomAppearance  Appearance to apply to the bottom cap.
	 * @param bottomMap         Provides UV coordinates for bottom cap.
	 * @param bottomFlipTexture Whether the bottom texture direction is
	 *                          flipped.
	 * @param side              {@code true} to include side faces.
	 * @param sideAppearance    Appearance to apply to the extruded sides.
	 * @param sideMap           Provides UV coordinates for extruded sides.
	 * @param sideFlipTexture   Whether the side texture direction is flipped.
	 * @param twoSided          Flag to indicate if extruded faces are
	 *                          two-sided.
	 * @param flipNormals       If {@code true}, normals are flipped to point in
	 *                          the opposite direction.
	 * @param smooth            Shape is smooth.
	 */
	public void addExtrudedShape( @NotNull final Tessellator tessellator, final boolean flipVertexOrder, @NotNull final Matrix3D bottomTransform, @NotNull final Matrix3D topTransform, final boolean extrusionLines, final boolean top, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, final boolean topFlipTexture, final boolean bottom, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean bottomFlipTexture, final boolean side, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean sideFlipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		addExtrudedShape( tessellator, flipVertexOrder, bottomTransform, topTransform, true, extrusionLines, top, topAppearance, topMap, topFlipTexture, bottom, bottomAppearance, bottomMap, bottomFlipTexture, side, sideAppearance, sideMap, sideFlipTexture, twoSided, flipNormals, smooth );
	}

	/**
	 * Add extruded shape with caps.
	 *
	 * @param tessellator       Tessellator that defines the shape.
	 * @param flipVertexOrder   Flip (counter-)clockwise vertex order.
	 * @param bottomTransform   Transforms 2D points to bottom face points.
	 * @param topTransform      Transforms 2D points to top face points.
	 * @param outline           Include top/bottom outlines.
	 * @param extrusionLines    Include extrusion (out)lines.
	 * @param top               {@code true} to include a top face.
	 * @param topAppearance     Appearance to apply to the top cap.
	 * @param topMap            Provides UV coordinates for top cap.
	 * @param topFlipTexture    Whether the top texture direction is flipped.
	 * @param bottom            {@code true} to include a bottom face.
	 * @param bottomAppearance  Appearance to apply to the bottom cap.
	 * @param bottomMap         Provides UV coordinates for bottom cap.
	 * @param bottomFlipTexture Whether the bottom texture direction is
	 *                          flipped.
	 * @param side              {@code true} to include side faces.
	 * @param sideAppearance    Appearance to apply to the extruded sides.
	 * @param sideMap           Provides UV coordinates for extruded sides.
	 * @param sideFlipTexture   Whether the side texture direction is flipped.
	 * @param twoSided          Flag to indicate if extruded faces are
	 *                          two-sided.
	 * @param flipNormals       If {@code true}, normals are flipped to point in
	 *                          the opposite direction.
	 * @param smooth            Shape is smooth.
	 */
	public void addExtrudedShape( @NotNull final Tessellator tessellator, final boolean flipVertexOrder, @NotNull final Matrix3D bottomTransform, @NotNull final Matrix3D topTransform, final boolean outline, final boolean extrusionLines, final boolean top, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, final boolean topFlipTexture, final boolean bottom, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean bottomFlipTexture, final boolean side, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean sideFlipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		if ( !side && !top && !bottom )
		{
			throw new IllegalArgumentException( "at least one kind of geometry is required" );
		}

		final List<TessellationPrimitive> topPrimitives = top ? flipVertexOrder ? tessellator.getClockwisePrimitives() : tessellator.getCounterClockwisePrimitives() : null;
		final List<TessellationPrimitive> bottomPrimitives = bottom ? flipVertexOrder ? tessellator.getCounterClockwisePrimitives() : tessellator.getClockwisePrimitives() : null;
		final List<int[]> outlines = outline ? flipVertexOrder ? tessellator.getClockwiseOutlines() : tessellator.getCounterClockwiseOutlines() : Collections.emptyList();

		final List<Vector3D> topPoints = ( top || side ) ? transform( topTransform, tessellator.getVertexList() ) : null;

		if ( top )
		{
			final List<Vertex3D> topVertices = createVertices( topPoints, topAppearance, topMap, topFlipTexture );

			final Tessellation topTessellation = new Tessellation( outlines, topPrimitives );
			addFace( topVertices, topTessellation, topAppearance, false, twoSided );
		}

		final List<Vector3D> bottomPoints = ( bottom || side ) ? transform( bottomTransform, tessellator.getVertexList() ) : null;

		if ( bottom )
		{
			final List<Vertex3D> bottomVertices = createVertices( bottomPoints, bottomAppearance, bottomMap, bottomFlipTexture );

			final Tessellation bottomTessellation = new Tessellation( outlines, bottomPrimitives );
			addFace( bottomVertices, bottomTessellation, bottomAppearance, false, twoSided );
		}

		if ( side && !outlines.isEmpty() )
		{
			final FaceGroup sideFaceGroup = _target.getFaceGroup( sideAppearance, smooth, twoSided );

			final List<int[]> extrusionOutlines = extrusionLines ? Collections.singletonList( new int[] { 0, 1, 2, 3, 0 } ) : Collections.emptyList();
			final List<TessellationPrimitive> extrusionPrimitives = Collections.singletonList( new QuadList( new int[] { 0, 1, 2, 3 } ) );
			final Tessellation extrusionTessellation = new Tessellation( extrusionOutlines, extrusionPrimitives );

			for ( final int[] contour : outlines )
			{
				final int lastIndex = contour[ 0 ];
				Vector3D previousP1 = bottomPoints.get( lastIndex );
				Vector3D previousP2 = topPoints.get( lastIndex );
				int previousI1 = getVertexIndex( previousP1 );
				int previousI2 = getVertexIndex( previousP2 );

				for ( int i = 1; i < contour.length; i++ )
				{
					final int vertexIndex = contour[ i ];
					final Vector3D nextP1 = bottomPoints.get( vertexIndex );
					final Vector3D nextP2 = topPoints.get( vertexIndex );
					final int nextI1 = getVertexIndex( nextP1 );
					final int nextI2 = getVertexIndex( nextP2 );

					final Vertex3D v1 = new Vertex3D( nextP1, nextI1 );
					final Vertex3D v2 = new Vertex3D( nextP2, nextI2 );
					final Vertex3D v3 = new Vertex3D( previousP2, previousI2 );
					final Vertex3D v4 = new Vertex3D( previousP1, previousI1 );

					final Vector3D normal = flipNormals ? GeometryTools.getPlaneNormal( v1.point, v2.point, v3.point ) : GeometryTools.getPlaneNormal( v3.point, v2.point, v1.point );
					if ( normal != null )
					{
						if ( sideMap != null )
						{
							final UVGenerator uv = UVGenerator.getColorMapInstance( sideAppearance, sideMap, normal, sideFlipTexture );

							uv.generate( v1.point );
							v1.colorMapU = uv.getU();
							v1.colorMapV = uv.getV();

							uv.generate( v2.point );
							v2.colorMapU = uv.getU();
							v2.colorMapV = uv.getV();

							uv.generate( v3.point );
							v3.colorMapU = uv.getU();
							v3.colorMapV = uv.getV();

							uv.generate( v4.point );
							v4.colorMapU = uv.getU();
							v4.colorMapV = uv.getV();
						}

						sideFaceGroup.addFace( new Face3D( normal, Arrays.asList( v1, v2, v3, v4 ), extrusionTessellation ) );
					}

					previousP1 = nextP1;
					previousP2 = nextP2;
					previousI1 = nextI1;
					previousI2 = nextI2;
				}
			}
		}
	}

	/**
	 * Add extruded shape without caps.
	 *
	 * @param contours    Contours that define the shape.
	 * @param extrusion   Extrusion vector (control-point displacement).
	 * @param transform   Transform to apply.
	 * @param uvMap       Provides UV coordinates.
	 * @param appearance  Appearance specification to use for shading.
	 * @param flipTexture Whether the side texture direction is flipped.
	 * @param twoSided    Flag to indicate if extruded faces are two-sided.
	 * @param flipNormals If {@code true}, normals are flipped to point in the
	 *                    opposite direction.
	 * @param smooth      Shape is smooth.
	 *
	 * @throws IllegalArgumentException if the shape is not extruded along the
	 * z-axis.
	 */
	public void addExtrudedShape( @NotNull final Iterable<Contour> contours, @NotNull final Vector3D extrusion, @NotNull final Matrix3D transform, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		final double extrusionZ = extrusion.getZ();
		if ( extrusionZ == 0.0 )
		{
			throw new IllegalArgumentException( "extrusion.z: " + extrusionZ );
		}

		final boolean flipExtrusion = flipNormals ^ ( extrusionZ < 0.0 );

		for ( final Contour contour : contours )
		{
			final List<Contour.Point> points = contour.getPoints();

			final Contour.Point firstPoint = points.get( 0 );
			final int first1 = getVertexIndex( transform.transform( firstPoint.getX(), firstPoint.getY(), 0.0 ) );
			final int first2 = getVertexIndex( transform.transform( firstPoint.getX() + extrusion.getX(), firstPoint.getY() + extrusion.getY(), extrusionZ ) );

			int previous1 = first1;
			int previous2 = first2;

			for ( int pointIndex = 1; pointIndex < points.size(); pointIndex++ )
			{
				final Contour.Point nextPoint = points.get( pointIndex );
				final int next1 = getVertexIndex( transform.transform( nextPoint.getX(), nextPoint.getY(), 0.0 ) );

				if ( previous1 != next1 )
				{
					final int next2 = getVertexIndex( transform.transform( nextPoint.getX() + extrusion.getX(), nextPoint.getY() + extrusion.getY(), extrusionZ ) );
					if ( flipExtrusion )
					{
						addFace( new int[] { previous1, next1, next2, previous2 }, appearance, uvMap, flipTexture, smooth, twoSided );
					}
					else
					{
						addFace( new int[] { previous1, previous2, next2, next1 }, appearance, uvMap, flipTexture, smooth, twoSided );
					}

					previous2 = next2;
					previous1 = next1;
				}
			}

			final Contour.ShapeClass shapeClass = contour.getShapeClass();
			final boolean isClosed = ( shapeClass != Contour.ShapeClass.LINE_SEGMENT ) && ( shapeClass != Contour.ShapeClass.OPEN_PATH );
			if ( isClosed )
			{
				if ( flipExtrusion )
				{
					addFace( new int[] { previous1, first1, first2, previous2 }, appearance, uvMap, flipTexture, smooth, twoSided );
				}
				else
				{
					addFace( new int[] { previous1, previous2, first2, first1 }, appearance, uvMap, flipTexture, smooth, twoSided );
				}
			}
		}
	}

	/**
	 * Add filled tessellated shape.
	 *
	 * @param transform   Transforms 2D to 3D coordinates.
	 * @param tessellator Tessellator that defines the shape.
	 * @param appearance  Appearance specification to use for shading.
	 * @param uvMap       UV-map used to generate texture coordinates.
	 * @param flipTexture Whether the bottom texture direction is flipped.
	 * @param flipNormals Flip normals using CW instead of CCW triangles.
	 * @param twoSided    Resulting face will be two-sided (has backface).
	 */
	public void addFilledShape2D( @NotNull final Matrix3D transform, @NotNull final Tessellator tessellator, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean flipNormals, final boolean twoSided )
	{
		addFilledShape2D( transform, tessellator, true, appearance, uvMap, flipTexture, flipNormals, twoSided );
	}

	/**
	 * Add filled tessellated shape.
	 *
	 * @param transform   Transforms 2D to 3D coordinates.
	 * @param tessellator Tessellator that defines the shape.
	 * @param outline     Create shape outlines.
	 * @param appearance  Appearance specification to use for shading.
	 * @param uvMap       UV-map used to generate texture coordinates.
	 * @param flipTexture Whether the bottom texture direction is flipped.
	 * @param flipNormals Flip normals using CW instead of CCW triangles.
	 * @param twoSided    Resulting face will be two-sided (has backface).
	 */
	public void addFilledShape2D( @NotNull final Matrix3D transform, @NotNull final Tessellator tessellator, final boolean outline, @Nullable final Appearance appearance, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean flipNormals, final boolean twoSided )
	{
		final List<TessellationPrimitive> primitives = flipNormals ? tessellator.getClockwisePrimitives() : tessellator.getCounterClockwisePrimitives();
		final List<int[]> outlines = outline ? flipNormals ? tessellator.getClockwiseOutlines() : tessellator.getCounterClockwiseOutlines() : Collections.emptyList();

		final Tessellation tessellation = new Tessellation( outlines, primitives );
		final List<Vertex3D> vertices = createVertices( transform( transform, tessellator.getVertexList() ), appearance, uvMap, flipTexture );

		addFace( vertices, tessellation, appearance, false, twoSided );
	}

	/**
	 * Create vertices from the given list of 3D points.
	 *
	 * @param points      3D points used as vertex coordinates.
	 * @param appearance  Appearance specification to use for shading.
	 * @param uvMap       UV-map used to generate texture coordinates.
	 * @param flipTexture Whether the bottom texture direction is flipped.
	 *
	 * @return Vertices for 3D face.
	 */
	public @NotNull List<Vertex3D> createVertices( final @NotNull List<Vector3D> points, final @Nullable Appearance appearance, final @Nullable UVMap uvMap, final boolean flipTexture )
	{
		final int vertexCount = points.size();
		final List<Vertex3D> vertices = new ArrayList<>( vertexCount );

		if ( uvMap != null )
		{
			final TextureMap colorMap = ( appearance == null ) ? null : appearance.getColorMap();
			final float[] uvCoords = uvMap.generate( colorMap, points, null, flipTexture );

			int i = 0;
			int j = 0;
			while ( i < vertexCount )
			{
				final Vector3D point = points.get( i++ );
				vertices.add( new Vertex3D( point, getVertexIndex( point ), uvCoords[ j++ ], uvCoords[ j++ ] ) );
			}
		}
		else
		{
			for ( final Vector3D point : points )
			{
				vertices.add( new Vertex3D( point, getVertexIndex( point ) ) );
			}
		}

		return vertices;
	}

	/**
	 * Transforms points from a 2D plane into points in 3D space.
	 *
	 * @param transform Transforms 2D to 3D coordinates.
	 * @param points    2D points to transform.
	 *
	 * @return 3D points.
	 */
	public static @NotNull List<Vector3D> transform( final @NotNull Matrix3D transform, final @NotNull Collection<? extends Vector2D> points )
	{
		final List<Vector3D> result = new ArrayList<>( points.size() );

		for ( final Vector2D point : points )
		{
			result.add( transform.transform( point.getX(), point.getY(), 0.0 ) );
		}

		return result;
	}

	/**
	 * Add face to current 3D object.
	 *
	 * @param vertices     Vertices that defines the face.
	 * @param tessellation Tessellation of face.
	 * @param appearance   Appearance specification to use for shading.
	 * @param smooth       Face is smooth/curved vs. flat.
	 * @param twoSided     Resulting face will be two-sided (has backface).
	 */
	public void addFace( @NotNull final List<Vertex3D> vertices, @Nullable final Tessellation tessellation, @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		_target.addFace( appearance, smooth, twoSided, new Face3D( vertices, tessellation ) );
	}
}
