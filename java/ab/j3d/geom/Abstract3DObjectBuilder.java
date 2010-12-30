/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.geom;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.ShapeTools.*;
import ab.j3d.model.Face3D.*;
import ab.j3d.model.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This abstract class defines an interface through which an 3D object can easily
 * be created from (mostly 2D) shapes.
 * <p />
 * Implementors only need to implement simple shapes, since more comlex ones are
 * automatically converted into simpler elements. If an implementation can provide
 * a better/more direct implementation of a complex shape, such an implementation
 * is always preferred.
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class Abstract3DObjectBuilder
{
	/**
	 * Construct builder.
	 */
	protected Abstract3DObjectBuilder()
	{
	}

	/**
	 * Get index of vertex at the specified point. If no vertex was found at the
	 * specified point, a new one is created.
	 *
	 * @param   point   Point to get vertex index of.
	 *
	 * @return  Vertex index.
	 */
	public abstract int getVertexIndex( @NotNull Vector3D point );

	/**
	 * Set coordinates of all vertices in this object. This is only allowed when
	 * no faces have been added yet, since the object integrity is otherwise
	 * lost.
	 *
	 * @param   vertexPoints    Vertex points.
	 */
	public abstract void setVertexCoordinates( @NotNull List<Vector3D> vertexPoints );

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices. This is only allowed when vertex
	 * coordinates have been set and all faces have been added, since the object
	 * integrity is otherwise lost.
	 *
	 * @param   vertexNormals   Vertex normals (one triplet per vertex).
	 */
	public abstract void setVertexNormals( @NotNull double[] vertexNormals );

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices. This is only allowed when vertex
	 * coordinates have been set and all faces have been added, since the object
	 * integrity is otherwise lost.
	 *
	 * @param   vertexNormals   Vertex normals.
	 */
	public void setVertexNormals( @NotNull final List<Vector3D> vertexNormals )
	{
		final int vertexCount = vertexNormals.size();
		final double[] doubles = new double[ vertexCount * 3 ];
		int i = 0;
		int j = 0;
		while ( i < vertexCount )
		{
			final Vector3D normal = vertexNormals.get( i++ );
			doubles[ j++ ] = normal.x;
			doubles[ j++ ] = normal.y;
			doubles[ j++ ] = normal.z;
		}
		setVertexNormals( doubles );
	}

	/**
	 * Add arc.
	 *
	 * @param   centerPoint     Center-point of circle on which the arc is defined.
	 * @param   radius          Radius of circle on which the arc is defined.
	 * @param   startAngle      Start-angle of arc relative to X-axis (radians).
	 * @param   endAngle        End-angle of arc relative to X-axis (radians).
	 * @param   startWidth      Start-width of arc (0.0 => no width).
	 * @param   endWidth        End-width of arc (0.0 => no width).
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addArc( @NotNull final Vector3D centerPoint, final double radius, final double startAngle, final double endAngle, final double startWidth, final double endWidth, @Nullable final Vector3D extrusion, @Nullable final Material material, final boolean fill )
	{
		final double twoPI = 2.0 * Math.PI;

		double enclosedAngle = ( endAngle - startAngle ) % twoPI;
		if ( enclosedAngle < 0.0 )
		{
			enclosedAngle += twoPI;
		}

		final int     nrSegments = ( enclosedAngle < Math.PI / 4.0 ) ? 3 : (int)( ( 32.0 * enclosedAngle ) / twoPI + 0.5 );
		final double  angleStep  = enclosedAngle / (double)nrSegments;
		final boolean extruded   = ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT );

		if ( ( startWidth == 0.0 ) && ( endWidth == 0.0 ) )
		{
			double angle       = startAngle;
			double cos         = Math.cos( angle );
			double sin         = Math.sin( angle );

			Vector3D point1 = centerPoint.plus( radius * cos, radius * sin, 0.0 );

			for ( int i = 0 ; i < nrSegments ; i++ )
			{
				angle += angleStep;
				cos   = Math.cos( angle );
				sin   = Math.sin( angle );

				final Vector3D point2 = centerPoint.plus( radius * cos, radius * sin, 0.0 );

				if ( extruded )
				{
					final Vector3D point1a = point1.plus( extrusion );
					final Vector3D point2a = point2.plus( extrusion );

					addQuad( point1, point2, point2a, point1a, null, material, fill, true );
				}
				else
				{
					addLine( point1, point2, material );
				}

				point1 = point2;
			}
		}
		else
		{
			final double radiusStep = ( endWidth - startWidth ) / (double)( 2 * nrSegments );

			double angle       = startAngle;
			double cos         = Math.cos( angle );
			double sin         = Math.sin( angle );
			double innerRadius = radius - startWidth / 2.0;
			double outerRadius = innerRadius + startWidth;

			Vector3D inner1 = centerPoint.plus( innerRadius * cos, innerRadius * sin, 0.0 );
			Vector3D outer1 = centerPoint.plus( outerRadius * cos, outerRadius * sin, 0.0 );

			for ( int i = 0 ; i < nrSegments ; i++ )
			{
				angle       += angleStep;
				innerRadius -= radiusStep;
				outerRadius += radiusStep;
				cos         = Math.cos( angle );
				sin         = Math.sin( angle );

				final Vector3D inner2 = centerPoint.plus( innerRadius * cos, innerRadius * sin, 0.0 );
				final Vector3D outer2 = centerPoint.plus( outerRadius * cos, outerRadius * sin, 0.0 );

				if ( extruded )
				{
					final Vector3D extrudedInner1 = inner1.plus( extrusion );
					final Vector3D extrudedOuter1 = outer1.plus( extrusion );
					final Vector3D extrudedOuter2 = outer2.plus( extrusion );
					final Vector3D extrudedInner2 = inner2.plus( extrusion );

					final boolean isFirst = ( i == 0 );
					final boolean isLast  = ( i == ( nrSegments -1 ) );

					addQuad( outer1, outer2, inner2, inner1, null, material, fill, true );

					if ( isFirst )
					{
						addQuad( outer1, inner1, extrudedInner1, extrudedOuter1, null, material, fill, true );
					}

					addQuad( inner1, inner2, extrudedInner2, extrudedInner1, null, material, fill, true );

					addQuad( inner2, outer2, extrudedOuter2, extrudedInner2, null, material, fill, true );

					if ( isLast  )
					{
						addQuad( outer2, outer1, extrudedOuter1, extrudedOuter2, null, material, fill, true );
					}

					addQuad( extrudedOuter1, extrudedInner1, extrudedInner2, extrudedOuter2, null, material, fill, true );
				}
				else
				{
					addQuad( outer1, inner1, inner2, outer2, null, material, fill, true );
				}

				inner1 = inner2;
				outer1 = outer2;
			}
		}
	}

	/**
	 * Add circle primitive.
	 *
	 * @param   centerPoint     Center-point of circle.
	 * @param   radius          Radius of circle.
	 * @param   normal          Normal pointing out of the circle's center.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addCircle( @NotNull final Vector3D centerPoint, final double radius, @NotNull final Vector3D normal, @Nullable final Vector3D extrusion, @Nullable final Material material, final boolean fill )
	{
		final Matrix3D base = Matrix3D.getPlaneTransform( centerPoint, normal, true );

		final float diameter = (float)radius * 2.0f;
		final float origin = (float)-radius;
		final Ellipse2D ellipse2d = new Ellipse2D.Float( origin, origin, diameter, diameter );

		final UVMap uvMap = new BoxUVMap( Scene.MM, base ); // @FIXME Retrieve model units instead of assuming millimeters.

		// TODO Use more intelligence here, we know the outline is a simple convex shape etc, no need for complex extrusion/tessellation here
		final boolean hasExtrusion = ( extrusion != null ) && !extrusion.almostEquals( Vector3D.ZERO );
		if ( fill )
		{
			if ( hasExtrusion )
			{
				addExtrudedShape( ellipse2d, radius * 0.02, extrusion, true, base, true, material, uvMap, false, true, material, uvMap, false, true, material, uvMap, false, true, false, true );
			}
			else
			{
				addFilledShape2D( base, ellipse2d, radius * 0.02, material, uvMap, false, false, true );
			}
		}
		else if ( hasExtrusion )
		{
			addExtrudedShape( ellipse2d, radius * 0.02, extrusion, base, material, uvMap, false, true, false, true );
		}
	}

	/**
	 * Add cylinder primitive.
	 *
	 * @param   origin              Origin of cylinder.
	 * @param   direction           Direction of cylinder.
	 * @param   height              Height of cylinder (z-axis).
	 * @param   radiusBottom        Radius at bottom (z=0).
	 * @param   radiusTop           Radius at top (z=height).
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   sideMaterial        Material of cylinder circumference.
	 * @param   sideMap             UV map to use for circumference.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   topMaterial         Material for top cap (<code>null</code> => no cap).
	 * @param   topMap              UV map for top cap.
	 * @param   bottomMaterial      Material for bottom cap (<code>null</code> => no cap).
	 * @param   bottomMap           UV map for bottom cap.
	 * @param   flipNormals         If set, flip normals.
	 */
	public void addCylinder( @NotNull final Vector3D origin, @NotNull final Vector3D direction, final double height, final double radiusBottom, final double radiusTop, final int numEdges, @Nullable final Material sideMaterial, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Material topMaterial, @Nullable final UVMap topMap, @Nullable final Material bottomMaterial, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		final Matrix3D base = Matrix3D.getPlaneTransform( origin, direction, true );

		/*
		 * Setup properties of cylinder.
		 */
		final boolean  hasBottom         = ( radiusBottom > 0.0 );
		final boolean  hasTop            = ( radiusTop    > 0.0 );
		final int      vertexCount       = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final int[]    vertexIndices     = new int[ vertexCount ];

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
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
			for ( int i = 0 ; i < numEdges ; i++ )
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
		if ( hasBottom && ( bottomMaterial != null ) )
		{
			final int[] faceVertices = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				faceVertices[ i ] = flipNormals ? ( numEdges - 1 - i ) : i;
			}

			addFace( faceVertices, bottomMaterial, bottomMap, false, false, false );
		}

		/*
		 * Circumference.
		 */
		if ( hasTop || hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
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

				addFace( faceVertices, sideMaterial, sideMap, false, smoothCircumference, false );
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop && ( topMaterial != null ) )
		{
			final int[] faceVertices = new int[ numEdges ];

			final int lastVertex = ( hasBottom ? numEdges : 1 ) + numEdges - 1;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				faceVertices[ i ] = vertexIndices[ flipNormals ? ( lastVertex - numEdges + 1 + i ) : ( lastVertex - i ) ];
			}

			addFace( faceVertices, topMaterial, topMap, false, false, false );
		}
	}

	/**
	 * Add line primitive.
	 *
	 * @param   point1      First point.
	 * @param   point2      Second point.
	 * @param   material    Material specification to use for shading.
	 */
	public void addLine( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @Nullable final Material material )
	{
		addFace( new Vector3D[] { point1, point2 }, material, false, true );
	}

	/**
	 * Add line.
	 *
	 * @param   point1      First point.
	 * @param   point2      Second point.
	 * @param   extrusion   Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material    Material specification to use for shading.
	 * @param   fill        Create filled shape vs. create wireframe.
	 */
	public void addLine( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @Nullable final Vector3D extrusion, @Nullable final Material material, final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D p1a = point1.plus( extrusion );
			final Vector3D p2a = point2.plus( extrusion );

			addQuad( point1, p1a, p2a, point2, null, material, fill, true );
		}
		else
		{
			addLine( point1, point2, material );
		}
	}

	/**
	 * Add face without texture.
	 *
	 * @param   points          Vertex coordinates that define the face.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		addFace( points, material, null, null, smooth, twoSided );
	}

	/**
	 * Add face without texture.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public void addFace( @NotNull final int[] vertexIndices, @Nullable final Material material, final boolean smooth, final boolean twoSided )
	{
		addFace( vertexIndices, material, null, null, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param   points          Vertex coordinates that define the face.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Flip texture direction.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean smooth, final boolean twoSided )
	{
		final int[] vertexIndices = new int[ points.length ];
		for ( int i = 0 ; i < points.length ; i++ )
		{
			vertexIndices[ i ] = getVertexIndex( points[ i ] );
		}

		addFace( vertexIndices, material, uvMap, flipTexture, smooth, twoSided );
	}

	/**
	 * Add face with texture.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Flip texture direction.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( @NotNull int[] vertexIndices, @Nullable Material material, @Nullable UVMap uvMap, boolean flipTexture, boolean smooth, boolean twoSided );

	/**
	 * Add face.
	 *
	 * @param   points          Points for vertices that define the face.
	 * @param   material        Material to apply to the face.
	 * @param   texturePoints   Texture coordiantes for each vertex.
	 * @param   vertexNormals   Normal for each vertex.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public void addFace( @NotNull final Vector3D[] points, @Nullable final Material material, @Nullable final float[] texturePoints, @Nullable final Vector3D[] vertexNormals, final boolean smooth, final boolean twoSided )
	{
		final int   vertexCount   = points.length;
		final int[] vertexIndices = new int[ vertexCount ];

		for ( int i = 0 ; i < vertexCount ; i++ )
		{
			vertexIndices[ i ] = getVertexIndex( points[ i ] );
		}

		addFace( vertexIndices, material, texturePoints, vertexNormals, smooth, twoSided );
	}

	/**
	 * Add face.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   texturePoints   Texture coordiantes for each vertex.
	 * @param   vertexNormals   Normal for each vertex.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( @NotNull int[] vertexIndices, @Nullable Material material, @Nullable float[] texturePoints, @Nullable Vector3D[] vertexNormals, boolean smooth, boolean twoSided );

	/**
	 * Add quad primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Material material, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, material, false, twoSided );
	}

	/**
	 * Add quad primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, material, uvMap, false, false, twoSided );
	}

	/**
	 * Add quad primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean smooth, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, material, uvMap, false, smooth, twoSided );
	}

	/**
	 * Add textured quad.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   texturePoint1   First vertex texture coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   texturePoint2   Second vertex texture coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   texturePoint3   Third vertex texture coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   texturePoint4   Fourth vertex texture coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Point2D.Float texturePoint1, @NotNull final Vector3D point2, @NotNull final Point2D.Float texturePoint2, @NotNull final Vector3D point3, @NotNull final Point2D.Float texturePoint3, @NotNull final Vector3D point4, @NotNull final Point2D.Float texturePoint4, @Nullable final Material material, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3, point4 }, material, new float[] { texturePoint1.x, texturePoint1.y, texturePoint2.x, texturePoint2.y, texturePoint3.x, texturePoint3.y, texturePoint4.x, texturePoint4.y }, null, false, twoSided );
	}

	/**
	 * Add quad with optional extrusion.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addQuad( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @NotNull final Vector3D point4, @Nullable final Vector3D extrusion, @Nullable final Material material, final boolean fill, final boolean twoSided )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );
			final Vector3D point4a = point4.plus( extrusion );

			if ( fill )
			{
				addQuad( point4, point3, point2, point1, material, twoSided );
				addQuad( point1, point2, point2a, point1a, material, twoSided );
				addQuad( point2, point3, point3a, point2a, material, twoSided );
				addQuad( point3, point4, point4a, point3a, material, twoSided );
				addQuad( point4, point1, point1a, point4a, material, twoSided );
				addQuad( point1a, point2a, point3a, point4a, material, twoSided );
			}
			else
			{
				addLine( point1, point1a, material );
				addLine( point1, point2, material );
				addLine( point1a, point2a, material );

				addLine( point2, point2a, material );
				addLine( point2, point3, material );
				addLine( point2a, point3a, material );

				addLine( point3, point3a, material );
				addLine( point3, point4, material );
				addLine( point3a, point4a, material );

				addLine( point4, point4a, material );
				addLine( point4, point1, material );
				addLine( point4a, point1a, material );
			}
		}
		else
		{
			if ( fill )
			{
				addQuad( point1, point2, point3, point4, material, twoSided );
			}
			else
			{
				addLine( point1, point2, material );
				addLine( point2, point3, material );
				addLine( point3, point4, material );
				addLine( point4, point1, material );
			}
		}
	}

	/**
	 * Add text.
	 *
	 * @param   text            Text value.
	 * @param   origin          Starting point
	 * @param   height          Height.
	 * @param   rotationAngle   Rotation angle.
	 * @param   obliqueAngle    Oblique angle.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material        Material specification to use for shading.
	 */
	public void addText( @NotNull final String text, @NotNull final Vector3D origin, final double height, final double rotationAngle, final double obliqueAngle, @Nullable final Vector3D extrusion, @Nullable final Material material )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Add text.
	 *
	 * @param   transform   Transforms text to the object being build.
	 * @param   text        Text to add.
	 * @param   font        Font to use.
	 * @param   alignX      X alignment (0=left, 0.5=left, 1=right).
	 * @param   alignY      Y alignment (0=baseline, 0.5=center, 1=ascent).
	 * @param   alignZ      Z alignment (0=below, 0.5=center, 1=on top).
	 * @param   extrusion   Extrusion along Z-axis.
	 * @param   material    Material.
	 * @param   uvMap       UV map.
	 */
	public void addText( final Matrix3D transform, final String text, final Font font, final double alignX, final double alignY, final double alignZ, final double extrusion, final Material material, final BoxUVMap uvMap )
	{
		addText( transform, text, font, 1.0, alignX, alignY, alignZ, extrusion, 0.025, material, uvMap, material, uvMap, material, uvMap );
	}

	/**
	 * Add text.
	 *
	 * @param   transform       Transforms text to the object being build.
	 * @param   text            Text to add.
	 * @param   font            Font to use.
	 * @param   scale           To apply to the font.
	 * @param   alignX          X alignment (0=left, 0.5=left, 1=right).
	 * @param   alignY          Y alignment (0=baseline, 0.5=center, 1=ascent).
	 * @param   alignZ          Z alignment (0=below, 0.5=center, 1=on top).
	 * @param   extrusion       Extrusion along Z-axis.
	 * @param   flatness        Flatness to use for curves.
	 * @param   topMaterial     Material for top surface.
	 * @param   topMap          UV map for top surface.
	 * @param   bottomMaterial  Material for bottom surface.
	 * @param   bottomMap       UV map for bottom surface.
	 * @param   sideMaterial    Material for side surfaces.
	 * @param   sideMap         UV map for side surfaces.
	 */
	public void addText( final Matrix3D transform, final String text, final Font font, final double scale, final double alignX, final double alignY, final double alignZ, final double extrusion, final double flatness, final Material topMaterial, final BoxUVMap topMap, final Material bottomMaterial, final BoxUVMap bottomMap, final Material sideMaterial, final BoxUVMap sideMap )
	{
		final GlyphVector glyphVector = font.createGlyphVector( new FontRenderContext( null, false, true ), text );
		final Rectangle2D visualBounds = glyphVector.getVisualBounds();

		final double x = -visualBounds.getMinX() - alignX * visualBounds.getWidth();
		final double y = visualBounds.getMaxY() - alignY * visualBounds.getHeight();
		final double z = -alignZ * extrusion;

		final Matrix3D glyphTransform = Matrix3D.multiply( scale,  0.0,  0.0, scale * x, 0.0, -scale,  0.0, scale * y, 0.0,  0.0, -scale, scale * z, transform );
		final Vector3D extrusionVector = new Vector3D( 0.0, 0.0, -extrusion );
		addExtrudedShape( glyphVector.getOutline(), flatness, extrusionVector, true, glyphTransform, true, topMaterial, topMap, false, true, bottomMaterial, bottomMap, false, true, sideMaterial, sideMap, false, false, false, true );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Material material, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, material, false, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, material, uvMap, false, false, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean smooth, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, material, uvMap, false, smooth, twoSided );
	}

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   texturePoint1   First vertex texture coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   texturePoint2   Second vertex texture coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   texturePoint3   Third vertex texture coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Point2D.Float texturePoint1, @NotNull final Vector3D point2, @NotNull final Point2D.Float texturePoint2, @NotNull final Vector3D point3, @NotNull final Point2D.Float texturePoint3, @Nullable final Material material, final boolean twoSided )
	{
		addFace( new Vector3D[] { point1, point2, point3 }, material, new float[] { texturePoint1.x, texturePoint1.y, texturePoint2.x, texturePoint2.y, texturePoint3.x, texturePoint3.y }, null, false, twoSided );
	}

	/**
	 * Add triangle with optional extrusion.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 * @param   twoSided        Flag to indicate if face has a backface.
	 */
	public void addTriangle( @NotNull final Vector3D point1, @NotNull final Vector3D point2, @NotNull final Vector3D point3, @Nullable final Vector3D extrusion, @Nullable final Material material, final boolean fill, final boolean twoSided )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );

			if ( fill )
			{
				addTriangle( point3, point2, point1, material, false );
				addQuad( point1, point2, point2a, point1a, material, false );
				addQuad( point2, point3, point3a, point2a, material, false );
				addQuad( point3, point1, point1a, point3a, material, false );
				addTriangle( point1a, point2a, point3a, material, false );
			}
			else
			{
				addLine( point1, point1a, material );
				addLine( point1, point2, material );
				addLine( point1a, point2a, material );

				addLine( point2, point2a, material );
				addLine( point2, point3, material );
				addLine( point2a, point3a, material );

				addLine( point3, point3a, material );
				addLine( point3, point1, material );
				addLine( point3a, point1a, material );
			}
		}
		else
		{
			if ( fill )
			{
				addTriangle( point1, point2, point3, material, twoSided );
			}
			else
			{
				addLine( point1, point2, material );
				addLine( point2, point3, material );
				addLine( point3, point1, material );
			}
		}
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested,
	 * the ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used
	 * to rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for Z!
	 *
	 * @param   transform           Optional transform to apply to points.
	 * @param   radii               X coordinates of 2D outline.
	 * @param   zCoordinates        Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   material            Material to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 */
	public void addRotatedObject( @Nullable final Matrix3D transform, final double[] radii, final double[] zCoordinates, final int detail, @Nullable final Material material, final boolean smoothCircumference, final boolean closeEnds )
	{
		int[] prevVertexIndices = null;

		for ( int i = 0 ; i < radii.length ; i++ )
		{
			final double radius = radii[ i ];
			final double z      = zCoordinates[ i ];

			/*
			 * Based on 'radius', create a list of vertex indices at this point.
			 */
			final int[] vertexIndices;
			if ( MathTools.almostEqual( radius, 0.0 ) )
			{
				final Vector3D point = ( transform != null ) ? transform.transform( 0.0, 0.0, z ) : Vector3D.INIT.set( 0.0, 0.0, z );
				vertexIndices = new int[] { getVertexIndex( point ) };
			}
			else
			{
				vertexIndices = new int[ detail ];

				final double stepSize = 2.0 * Math.PI / (double)detail;
				for ( int step = 0 ; step < detail ; step++ )
				{
					final double angle = (double)step * stepSize;
					final double x     =  Math.sin( angle ) * radius;
					final double y     = -Math.cos( angle ) * radius;

					final Vector3D point = ( transform != null ) ? transform.transform( x, y, z ) : Vector3D.INIT.set( x, y, z );
					vertexIndices[ step ] = getVertexIndex( point );
				}

				if ( closeEnds )
				{
					if ( i == 0 )
					{
						addFace( vertexIndices, material, false, false );
					}
					else if ( i == radii.length - 1 )
					{
						final int[] reversed = new int[ detail ];
						for ( int step = 0 ; step < detail ; step++ )
						{
							reversed[ step ] = vertexIndices[ detail - 1 - step ];
						}

						addFace( reversed, material, false, false );
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
						for ( int step = 0 ; step < detail ; step++ )
						{
							final int nextStep = ( step + 1 ) % detail;
							addFace( new int[] { prevVertexIndices[ step ], vertexIndices[ step ], vertexIndices[ nextStep ], prevVertexIndices[ nextStep ] }, material, smoothCircumference, false );
						}
					}
					else
					{
						for ( int step = 0 ; step < detail ; step++ )
						{
							addFace( new int[] { prevVertexIndices[ 0 ], vertexIndices[ step ], vertexIndices[ ( step + 1 ) % detail ] }, material, smoothCircumference, false );
						}
					}
				}
				else if ( prevVertexIndices.length > 1 )
				{
					for ( int step = 0 ; step < detail ; step++ )
					{
						addFace( new int[] { prevVertexIndices[ step ], vertexIndices[ 0 ], prevVertexIndices[ ( step + 1 ) % detail ] }, material, smoothCircumference, false );
					}
				}
			}
			prevVertexIndices = vertexIndices;
		}
	}

	/**
	 * Add extruded shape with caps.
	 *
	 * @param   shape               Shape to add.
	 * @param   extrusion           Extrusion vector (control-point displacement).
	 * @param   extrusionLines      Include extrusion (out)lines.
	 * @param   transform           Transform to apply.
	 * @param   top                 <code>true</code> to include a top face.
	 * @param   topMaterial         Material to apply to the top cap.
	 * @param   topMap              Provides UV coordinates for top cap.
	 * @param   topFlipTexture      Whether the top texture direction is flipped.
	 * @param   bottom              <code>true</code> to include a bottom face.
	 * @param   bottomMaterial      Material to apply to the bottom cap.
	 * @param   bottomMap           Provides UV coordinates for bottom cap.
	 * @param   bottomFlipTexture   Whether the bottom texture direction is flipped.
	 * @param   side                <code>true</code> to include side faces.
	 * @param   sideMaterial        Material to apply to the extruded sides.
	 * @param   sideMap             Provides UV coordinates for extruded sides.
	 * @param   sideFlipTexture     Whether the side texture direction is flipped.
	 * @param   flatness            Flatness to use.
	 * @param   twoSided            Flag to indicate if extruded faces are two-sided.
	 * @param   flipNormals         If <code>true</code>, normals are flipped to
	 *                              point in the opposite direction.
	 * @param   smooth              Shape is smooth.
	 *
	 * @throws  IllegalArgumentException if the shape is not extruded along
	 *          the z-axis.
	 */
	public void addExtrudedShape( @NotNull final Shape shape, final double flatness, @NotNull final Vector3D extrusion, final boolean extrusionLines, @NotNull final Matrix3D transform, final boolean top, @Nullable final Material topMaterial, @Nullable final UVMap topMap, final boolean topFlipTexture, final boolean bottom, @Nullable final Material bottomMaterial, @Nullable final UVMap bottomMap, final boolean bottomFlipTexture, final boolean side, @Nullable final Material sideMaterial, @Nullable final UVMap sideMap, final boolean sideFlipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		if ( extrusion.z == 0.0 )
		{
			throw new IllegalArgumentException( "extrusion.z: " + extrusion.z );
		}

		if ( !side && !top && !bottom )
		{
			throw new IllegalArgumentException( "at least one kind of geometry is required" );
		}

		final boolean flipExtrusion = flipNormals ^ ( extrusion.z < 0.0 );

		final Tessellator tessellator = new Tessellator();
		tessellator.defineShape( shape, flatness );

		final HashList<Point2D> vertexList = new HashList<Point2D>();
		final List<TessellationPrimitive> topPrimitives = top ? tessellator.constructPrimitives( vertexList, !flipExtrusion ) : null;
		final List<TessellationPrimitive> bottomPrimitives = bottom ? tessellator.constructPrimitives( vertexList, flipExtrusion ) : null;
		final List<int[]> outlines = tessellator.constructOutlines( vertexList, !flipExtrusion );

		final List<Vector3D> topPoints = ( top || side ) ? transform( transform.plus( transform.rotate( extrusion ) ), vertexList ) : null;

		if ( top )
		{
			final List<Vertex> topVertices = createVertices( topPoints, topMaterial, topMap, topFlipTexture );

			final Tessellation topTessellation = new Tessellation( outlines, topPrimitives );
			addFace( topVertices, topTessellation, topMaterial, false, twoSided );
		}

		final List<Vector3D> bottomPoints = ( bottom || side ) ? transform( transform, vertexList ) : null;

		if ( bottom )
		{
			final List<Vertex> bottomVertices = createVertices( bottomPoints, bottomMaterial, bottomMap, bottomFlipTexture );

			final Tessellation bottomTessellation = new Tessellation( outlines, bottomPrimitives );
			addFace( bottomVertices, bottomTessellation, bottomMaterial, false, twoSided );
		}

		if ( side && !outlines.isEmpty() )
		{
			final Point2D.Float uv = ( sideMap != null ) ? new Point2D.Float() : null;
			final Tessellation extrusionTessellation = new Tessellation( extrusionLines ? Collections.<int[]>singletonList( new int[] { 0, 1, 2, 3 } ) : Collections.<int[]>emptyList(), Collections.<TessellationPrimitive>singletonList( new TriangleFan( new int[] { 0, 1, 2, 3 } ) ) );

			for ( final int[] contour : outlines )
			{
				final int lastIndex = contour[ contour.length - 1 ];
				Vector3D previousP1 = bottomPoints.get( lastIndex );
				Vector3D previousP2 = topPoints.get( lastIndex );
				int previousI1 = getVertexIndex( previousP1 );
				int previousI2 = getVertexIndex( previousP2 );

				for ( final int vertexIndex : contour )
				{
					final Vector3D nextP1 = bottomPoints.get( vertexIndex );
					final Vector3D nextP2 = topPoints.get( vertexIndex );
					final int nextI1 = getVertexIndex( nextP1 );
					final int nextI2 = getVertexIndex( nextP2 );

					final Vertex v1 = new Vertex( nextP1, nextI1 );
					final Vertex v2 = new Vertex( nextP2, nextI2 );
					final Vertex v3 = new Vertex( previousP2, previousI2 );
					final Vertex v4 = new Vertex( previousP1, previousI1 );

					final Vector3D normal = GeometryTools.getPlaneNormal( v1.point, v2.point, v3.point );
					if ( normal != null )
					{
						if ( sideMap != null )
						{
							sideMap.generate( uv, sideMaterial, v1.point, normal, sideFlipTexture );
							v1.colorMapU = uv.x;
							v1.colorMapV = uv.y;

							sideMap.generate( uv, sideMaterial, v2.point, normal, sideFlipTexture );
							v2.colorMapU = uv.x;
							v2.colorMapV = uv.y;

							sideMap.generate( uv, sideMaterial, v3.point, normal, sideFlipTexture );
							v3.colorMapU = uv.x;
							v3.colorMapV = uv.y;

							sideMap.generate( uv, sideMaterial, v4.point, normal, sideFlipTexture );
							v4.colorMapU = uv.x;
							v4.colorMapV = uv.y;
						}

						addFace( Arrays.asList( v1, v2, v3, v4 ), extrusionTessellation, sideMaterial, smooth, twoSided );
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
	 * @param   shape           Shape to add.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   uvMap           Provides UV coordinates.
	 * @param   material        Material to apply to the extruded sides.
	 * @param   flipTexture     Whether the side texture direction is flipped.
	 * @param   flatness        Flatness to use.
	 * @param   twoSided        Flag to indicate if extruded faces are two-sided.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   smooth          Shape is smooth.
	 *
	 * @throws  IllegalArgumentException if the shape is not extruded along
	 *          the z-axis.
	 */
	public void addExtrudedShape( @NotNull final Shape shape, final double flatness, @NotNull final Vector3D extrusion, @NotNull final Matrix3D transform, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean twoSided, final boolean flipNormals, final boolean smooth )
	{
		if ( extrusion.z == 0.0 )
		{
			throw new IllegalArgumentException( "extrusion.z: " + extrusion.z );
		}

		final boolean flipExtrusion = flipNormals ^ ( extrusion.z < 0.0 );

		final List<Contour> contours = Contour.createContours( shape, flatness, true, true );

		for ( final Contour contour : contours )
		{
			final List<Contour.Point> points = contour.getPoints();

			final Contour.Point firstPoint = points.get( 0 );
			final int first1 = getVertexIndex( transform.transform( firstPoint.x, firstPoint.y, 0.0 ) );
			final int first2 = getVertexIndex( transform.transform( firstPoint.x + extrusion.x, firstPoint.y + extrusion.y, extrusion.z ) );

			int previous1 = first1;
			int previous2 = first2;

			for ( int pointIndex = 1; pointIndex < points.size(); pointIndex++ )
			{
				final Contour.Point nextPoint = points.get( pointIndex );
				final int next1 = getVertexIndex( transform.transform( nextPoint.x, nextPoint.y, 0.0 ) );

				if ( previous1 != next1 )
				{
					final int next2 = getVertexIndex( transform.transform( nextPoint.x + extrusion.x, nextPoint.y + extrusion.y, extrusion.z ) );
					if ( flipExtrusion )
					{
						addFace( new int[] { previous1, next1, next2, previous2 }, material, uvMap, flipTexture, smooth, twoSided );
					}
					else
					{
						addFace( new int[] { previous1, previous2, next2, next1 }, material, uvMap, flipTexture, smooth, twoSided );
					}

					previous2 = next2;
					previous1 = next1;
				}
			}

			final ShapeClass shapeClass = contour.getShapeClass();
			final boolean isClosed = ( shapeClass != ShapeClass.LINE_SEGMENT ) && ( shapeClass != ShapeClass.OPEN_PATH );
			if ( isClosed )
			{
				if ( flipExtrusion )
				{
					addFace( new int[] { previous1, first1, first2, previous2 }, material, uvMap, flipTexture, smooth, twoSided );
				}
				else
				{
					addFace( new int[] { previous1, previous2, first2, first1 }, material, uvMap, flipTexture, smooth, twoSided );
				}
			}
		}
	}

	/**
	 * Add tessellated shape.
	 *
	 * @param   transform       Transforms 2D to 3D coordinates.
	 * @param   shape           Shape to add.
	 * @param   flatness        Flatness used to approximate curves.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Whether the bottom texture direction is flipped.
	 * @param   flipNormals     Flip normals using CW instead of CCW triangles.
	 * @param   twoSided        Resulting face will be two-sided (has backface).
	 */
	public void addFilledShape2D( @NotNull final Matrix3D transform, @NotNull final Shape shape, final double flatness, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture, final boolean flipNormals, final boolean twoSided )
	{
		final Tessellator tessellator = new Tessellator();
		tessellator.defineShape( shape, flatness );

		final HashList<Point2D> vertexList = new HashList<Point2D>();
		final List<TessellationPrimitive> primitives = tessellator.constructPrimitives( vertexList, !flipNormals );
		final List<int[]> outlines = tessellator.constructOutlines( vertexList, !flipNormals );

		final Tessellation tessellation = new Tessellation( outlines, primitives );
		final List<Vertex> vertices = createVertices( transform( transform, vertexList ), material, uvMap, flipTexture );

		addFace( vertices, tessellation, material, false, twoSided );
	}

	/**
	 * Create vertices for 3D face using the specified tessellation.
	 *
	 * @param   points          3D points used as vertex coordinates.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Whether the bottom texture direction is flipped.
	 *
	 * @return  Vertices for 3D face.
	 */
	protected List<Vertex> createVertices( final List<Vector3D> points, @Nullable final Material material, @Nullable final UVMap uvMap, final boolean flipTexture )
	{
		final int vertexCount = points.size();
		final List<Vertex> vertices = new ArrayList<Vertex>( vertexCount );

		if ( uvMap != null )
		{
			final float[] uvCoords = uvMap.generate( material, points, null, flipTexture );
			for ( int i = 0, j= 0 ; i < vertexCount; )
			{
				final Vector3D point = points.get( i++ );
				vertices.add( new Vertex( point, getVertexIndex( point ), uvCoords[ j++ ], uvCoords[ j++ ] ) );
			}
		}
		else
		{
			for ( int i = 0 ; i < vertexCount; i++ )
			{
				final Vector3D point = points.get( i );
				vertices.add( new Vertex( point, getVertexIndex( point ) ) );
			}
		}

		return vertices;
	}

	/**
	 * Transforms points from a 2D plane into points in 3D space.
	 *
	 * @param   transform   Transforms 2D to 3D coordinates.
	 * @param   points      2D points to transform.
	 *
	 * @return  3D points.
	 */
	protected static List<Vector3D> transform( @NotNull final Matrix3D transform, @NotNull final Collection<? extends Point2D> points )
	{
		final List<Vector3D> result = new ArrayList<Vector3D>( points.size() );

		for ( final Point2D point : points )
		{
			result.add( transform.transform( point.getX(), point.getY(), 0.0 ) );
		}

		return result;
	}

	/**
	 * Add face to current 3D object.
	 *
	 * @param   vertices        Vertices that defines the face.
	 * @param   tessellation    Tessellation of face.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Resulting face will be two-sided (has backface).
	 */
	protected abstract void addFace( @NotNull List<Vertex> vertices, @NotNull Tessellation tessellation, @Nullable Material material, boolean smooth, boolean twoSided );
}
