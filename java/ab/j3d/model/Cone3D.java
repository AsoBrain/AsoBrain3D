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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a (partial) 3D cone with.
 * <p>
 * The partial cone has its base at the local origin, and has a given radius on
 * the Z=0 plane, extends upto a given height along the positive Z-axis where
 * it also has a given radius on the Z=height plane.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Cone3D
	extends Object3D
{
	/**
	 * Height of cone (z-axis).
	 */
	public final double height;

	/**
	 * Radius at top (z=height plane).
	 */
	public final double radiusTop;

	/**
	 * Radius at bottom (z=0 plane).
	 */
	public final double radiusBottom;

	/**
	 * Constructor for (partial) cone.
	 *
	 * @param   height              Height of cone (z-axis).
	 * @param   radiusBottom        Radius at bottom (z=0).
	 * @param   radiusTop           Radius at top (z=height).
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   sideMaterial        Material of cone circumference.
	 * @param   sideMap             UV map to use for circumference.
	 * @param   smoothCircumference Apply smoothing to circumference of cone.
	 * @param   topMaterial         Material for top cap (<code>null</code> => no cap).
	 * @param   topMap              UV map for top cap.
	 * @param   bottomMaterial      Material for bottom cap (<code>null</code> => no cap).
	 * @param   bottomMap           UV map for bottom cap.
	 * @param   flipNormals         If set, flip normals.
	 */
	public Cone3D( final double height, final double radiusBottom, final double radiusTop, final int numEdges, @Nullable final Material sideMaterial, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Material topMaterial, @Nullable final UVMap topMap, @Nullable final Material bottomMaterial, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		if ( ( radiusBottom < 0.0 ) || ( radiusTop < 0.0 ) || ( radiusTop == radiusBottom ) || ( height <= 0.0 ) || ( numEdges < 3 ) )
		{
			throw new IllegalArgumentException( "inacceptable arguments to Cone constructor" );
		}

		this.radiusTop = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height = height;

		/*
		 * Setup properties of cone.
		 */
		final boolean hasBottom = ( radiusBottom > 0.0 );
		final boolean hasTop    = ( radiusTop > 0.0 );

		final int vertexCount = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final Vector3D[] vertexCoordinatesArray = new Vector3D[ vertexCount ];
		final List<Vector3D> vertexCoordinates = Arrays.asList( vertexCoordinatesArray );

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		final int topCoordinatesOffset = hasBottom ? numEdges : 1;

		if ( !hasBottom )
		{
			vertexCoordinatesArray[ 0 ] = Vector3D.ZERO;
		}

		if ( !hasTop )
		{
			vertexCoordinatesArray[ topCoordinatesOffset ] = new Vector3D( 0.0, 0.0, height );
		}

		for ( int i = 0 ; i < numEdges ; i++ )
		{
			final double rad = (double)i * radStep;
			final double x   =  Math.sin( rad );
			final double y   = -Math.cos( rad );

			if ( hasBottom )
			{
				vertexCoordinatesArray[ i ] = new Vector3D( x * radiusBottom, y * radiusBottom, 0.0 );
			}

			if ( hasTop )
			{
				vertexCoordinatesArray[ topCoordinatesOffset + i ] = new Vector3D( x * radiusTop, y * radiusTop, height );
			}
		}

		setVertexCoordinates( vertexCoordinates );

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom && ( bottomMaterial != null ) )
		{
			final int[] vertexIndices = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( numEdges - 1 - i ) : i;
			}

			final float[] texturePoints = ( bottomMap != null ) ? bottomMap.generate( bottomMaterial.getColorMap(), vertexCoordinates, vertexIndices, false ) : null;
			final FaceGroup faceGroup = getFaceGroup( bottomMaterial, false, false );
			faceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, null ) );
		}

		/*
		 * Circumference.
		 */
		final FaceGroup sideFaceGroup = getFaceGroup( sideMaterial, smoothCircumference, false );
		for ( int i1 = 0 ; i1 < numEdges ; i1++ )
		{
			final int   i2 = ( i1 + 1 ) % numEdges;

			final int[] vertexIndices;

			if ( !hasTop )
			{
				vertexIndices = flipNormals ? new int[] { numEdges, i1, i2 } : new int[] { i2, i1, numEdges };
			}
			else if ( !hasBottom )
			{
				vertexIndices = flipNormals ? new int[] { 1 + i2, 1 + i1, 0 } : new int[] { 0, 1 + i1, 1 + i2 };
			}
			else
			{
				vertexIndices = flipNormals ? new int[] { numEdges + i2, numEdges + i1, i1, i2 } : new int[] { i2, i1, numEdges + i1, numEdges + i2 };
			}

			final TextureMap colorMap = ( sideMaterial == null ) ? null : sideMaterial.getColorMap();
			final float[] texturePoints = ( sideMap != null ) ? sideMap.generate( colorMap, vertexCoordinates, vertexIndices, false ) : null;
			sideFaceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, null ) );
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop && ( topMaterial != null ) )
		{
			final int[] vertexIndices = new int[ numEdges ];

			final int lastVertex = ( hasBottom ? numEdges : 1 ) + numEdges - 1;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( lastVertex - numEdges + 1 + i ) : ( lastVertex - i );
			}

			final float[] texturePoints = ( topMap != null ) ? topMap.generate( topMaterial.getColorMap(), vertexCoordinates, vertexIndices, false ) : null;
			final FaceGroup faceGroup = getFaceGroup( topMaterial, false, false );
			faceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, null ) );
		}
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()

	{
		final double radius = Math.max( radiusTop, radiusBottom );
		return new Bounds3D( -radius, -radius, 0.0, radius, radius, height );
	}
}