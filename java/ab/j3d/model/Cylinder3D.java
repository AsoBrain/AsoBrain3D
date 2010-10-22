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
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a bound 3D cylinder.
 * <p>
 * The cylinder has its base at the local origin, has a given constant radius,
 * and extends upto a given height along the positive Z-axis.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Cylinder3D
	extends Object3D
{
	/**
	 * Height of cylinder (z-axis).
	 */
	public final double height;

	/**
	 * Radius at bottom (z=0).
	 */
	public final double radius;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
	 *
	 * @param   height              Height of cylinder (z-axis).
	 * @param   radius              Radius.
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
	public Cylinder3D( final double height, final double radius, final int numEdges, @Nullable final Material sideMaterial, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Material topMaterial, @Nullable final UVMap topMap, @Nullable final Material bottomMaterial, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		if ( ( radius <= 0.0 ) || ( height <= 0.0 ) || ( numEdges < 3 ) )
		{
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor" );
		}

		this.height       = height;
		this.radius = radius;

		/*
		 * Setup properties of cylinder.
		 */
		final int vertexCount = numEdges + numEdges;
		final Vector3D[] vertexCoordinatesArray = new Vector3D[ vertexCount ];
		final List<Vector3D> vertexCoordinates = Arrays.asList( vertexCoordinatesArray );

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		final int topCoordinatesOffset = numEdges;

		for ( int i = 0 ; i < numEdges ; i++ )
		{
			final double rad = (double)i * radStep;
			final double x   =  radius * Math.sin( rad );
			final double y   = -radius * Math.cos( rad );

			vertexCoordinatesArray[ i ] = new Vector3D( x, y, 0.0 );
			vertexCoordinatesArray[ topCoordinatesOffset + i ] = new Vector3D( x, y, height );
		}

		setVertexCoordinates( vertexCoordinates );

		/*
		 * Bottom face (if it exists).
		 */
		if ( bottomMaterial != null )
		{
			final int[] vertexIndices = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( numEdges - 1 - i ) : i;
			}

			final float[] texturePoints = ( bottomMap != null ) ? bottomMap.generate( bottomMaterial, vertexCoordinates, vertexIndices, false ) : null;
			addFace( new Face3D( this, vertexIndices, bottomMaterial, texturePoints, null, false, false ) );
		}

		/*
		 * Circumference.
		 */
		for ( int i1 = 0 ; i1 < numEdges ; i1++ )
		{
			final int   i2 = ( i1 + 1 ) % numEdges;

			final int[] vertexIndices = flipNormals ? new int[] { numEdges + i2, numEdges + i1, i1, i2 } : new int[] { i2, i1, numEdges + i1, numEdges + i2 };

			final float[] texturePoints = ( sideMap != null ) ? sideMap.generate( sideMaterial, vertexCoordinates, vertexIndices, false ) : null;
			addFace( new Face3D( this, vertexIndices, sideMaterial, texturePoints, null, smoothCircumference, false ) );
		}

		/*
		 * Top face (if it exists).
		 */
		if ( topMaterial != null )
		{
			final int[] vertexIndices = new int[ numEdges ];

			final int lastVertex = vertexCount - 1;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( lastVertex - numEdges + 1 + i ) : ( lastVertex - i );
			}

			final float[] texturePoints = ( topMap != null ) ? topMap.generate( topMaterial, vertexCoordinates, vertexIndices, false ) : null;
			addFace( new Face3D( this, vertexIndices, topMaterial, texturePoints, null, false, false ) );
		}
	}

	/**
	 * Get height of cylinder.
	 *
	 * @return  Height of cylinder.
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * Get radius of cylinder.
	 *
	 * @return  Radius of cylinder.
	 */
	public double getRadius()
	{
		return radius;
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final double radius = this.radius;
		return new Bounds3D( -radius, -radius, 0.0, radius, radius, height );
	}

	@Override
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		if ( other instanceof Sphere3D )
		{
			final Sphere3D sphere = (Sphere3D)other;
			final double x = fromOtherToThis.transformX( 0.0, 0.0, 0.0 );
			final double y = fromOtherToThis.transformY( 0.0, 0.0, 0.0 );
			final double z = fromOtherToThis.transformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereCylinderIntersection( x, y, z, sphere.radius, height, radius );
		}
		else
		{
			result = super.collidesWith( fromOtherToThis, other );
		}

		return result;
	}
}
