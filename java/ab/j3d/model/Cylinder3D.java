/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.UVMap;

/**
 * This class defines a 3D cylinder with optionally different radi for the caps. Using
 * these radi, this class may also be used to construct cones.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Cylinder3D
	extends Object3D
{
	/**
	 * Base transform of cylinder.
	 */
	public final Matrix3D base;

	/**
	 * Height of cylinder (z-axis).
	 */
	public final double height;

	/**
	 * Radius at top (z=height).
	 */
	public final double radiusTop;

	/**
	 * Radius at bottom (z=0).
	 */
	public final double radiusBottom;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
	 *
	 * @param   radiusBottom        Radius at bottom (z=0).
	 * @param   radiusTop           Radius at top (z=height).
	 * @param   height              Height of cylinder (z-axis).
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   material            Material of cylinder.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   smoothCaps          Apply smoothing to caps of cylinder.
	 * @param   hasTopCap           Whether the cylinder is capped at the top.
	 * @param   hasBottomCap        Whether the cylinder is capped at the bottom.
	 */
	public Cylinder3D( final double radiusBottom , final double radiusTop , final double height , final int numEdges , @Nullable final Material material , final boolean smoothCircumference , final boolean smoothCaps , final boolean hasTopCap , final boolean hasBottomCap )
	{
		if ( radiusBottom < 0.0 || radiusTop < 0.0 || height < 0.0 || numEdges < 3 )
		{
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", material=" + material + ')' );
		}

		if ( radiusBottom == 0.0 && radiusTop == 0.0 )
		{
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );
		}

		base = Matrix3D.INIT;
		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		/*
		 * Setup properties of cylinder.
		 */
		final boolean    hasBottom         = ( radiusBottom > 0.0 );
		final boolean    hasTop            = ( radiusTop    > 0.0 );
		final int        vertexCount       = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final Vector3D[] vertexCoordinates = new Vector3D[ vertexCount ];

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		final int topCoordinatesOffset = hasBottom ? numEdges : 1;

		if ( !hasBottom )
		{
			vertexCoordinates[ 0 ] = Vector3D.ZERO;
		}

		if ( !hasTop )
		{
			vertexCoordinates[ topCoordinatesOffset ] = new Vector3D( 0.0 , 0.0 , height );
		}

		for ( int i = 0 ; i < numEdges ; i++ )
		{
			final double rad = (double)i * radStep;
			final double x   =  Math.sin( rad ) * radiusBottom;
			final double y   = -Math.cos( rad ) * radiusBottom;

			if ( hasBottom )
			{
				vertexCoordinates[ i ] = new Vector3D( x , y , 0.0 );
			}

			if ( hasTop )
			{
				vertexCoordinates[ i + topCoordinatesOffset ] = new Vector3D( x , y , height );
			}
		}

		setVertexCoordinates( Arrays.asList( vertexCoordinates ) );

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom && hasBottomCap )
		{
			final int[] vertexIndices = new int[ numEdges ];
			final Point2D.Float[] texturePoints = new Point2D.Float[ numEdges ];

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double rad = (double)i * radStep;

				vertexIndices[ i ] = i;
				texturePoints[ i ] = new Point2D.Float( (float)( 0.5 + 0.5 * Math.sin( rad ) ) , (float)( 0.5 - 0.5 * Math.cos( rad ) ) );
			}

			addFace( new Face3D( this , vertexIndices , material , texturePoints , null , smoothCaps , false ) );
		}

		/*
		 * Circumference.
		 */
		if ( hasTop || hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final float u1 = (float)  i1       / (float)numEdges;
				final float u2 = (float)( i1 + 1 ) / (float)numEdges;

				final int[] vertexIndices;
				final Point2D.Float[] texturePoints;

				if ( !hasTop )
				{
					vertexIndices = new int[]{i2 , i1 , numEdges};
					texturePoints = new Point2D.Float[] { new Point2D.Float( u2 , 0.0f ) , new Point2D.Float( u1 , 0.0f ) , new Point2D.Float( 0.5f , 1.0f ) };
				}
				else if ( !hasBottom )
				{
					vertexIndices = new int[] { 0 , 1 + i1 , 1 + i2 };
					texturePoints = new Point2D.Float[] { new Point2D.Float( 0.5f , 0.0f ) , new Point2D.Float( u1 , 1.0f ) , new Point2D.Float( u2 , 1.0f ) };
				}
				else
				{
					vertexIndices = new int[] { i2 , i1 , numEdges + i1 , numEdges + i2 };
					texturePoints = new Point2D.Float[] { new Point2D.Float( u2 , 0.0f ) , new Point2D.Float( u1 , 0.0f ) , new Point2D.Float( u1 , 1.0f ) , new Point2D.Float( u2 , 1.0f ) };
				}

				addFace( new Face3D( this , vertexIndices , material , texturePoints , null , smoothCircumference , false ) );
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop && hasTopCap )
		{
			final int[] vertexIndices = new int[ numEdges ];
			final Point2D.Float[] texturePoints = new Point2D.Float[ numEdges ];

			final int lastVertex = ( hasBottom ? numEdges : 1 ) + numEdges - 1;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double rad = (double)i * radStep;

				vertexIndices[ i ] = lastVertex - i;
				texturePoints[ i ] = new Point2D.Float( (float)( 0.5 + 0.5 * Math.sin( rad ) ) , (float)( 0.5 - 0.5 * Math.cos( rad ) ) );
			}

			addFace( new Face3D( this , vertexIndices , material , texturePoints , null , smoothCaps , false ) );
		}
	}

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
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
	public Cylinder3D( @NotNull final Vector3D origin , @NotNull final Vector3D direction , final double height , final double radiusBottom , final double radiusTop , final int numEdges , @Nullable final Material sideMaterial , @Nullable final UVMap sideMap , final boolean smoothCircumference , @Nullable final Material topMaterial , @Nullable final UVMap topMap , final @Nullable Material bottomMaterial , @Nullable final UVMap bottomMap , final boolean flipNormals )
	{
		if ( ( radiusBottom < 0.0 ) || ( radiusTop < 0.0 ) || ( height < 0.0 ) || ( numEdges < 3 ) )
		{
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor" );
		}

		final Matrix3D base = Matrix3D.getPlaneTransform( origin , direction , true );

		this.base         = base;
		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		/*
		 * Setup properties of cylinder.
		 */
		final boolean hasBottom = ( radiusBottom > 0.0 );
		final boolean hasTop    = ( radiusTop > 0.0 );

		final int vertexCount = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final Vector3D[] vertexCoordinatesArray = new Vector3D[ vertexCount ];
		final List<Vector3D> vertexCoordinates = Arrays.asList( vertexCoordinatesArray );

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		if ( !hasTop && !hasBottom )
		{
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );
		}

		/*
		 * Generate vertices.
		 */
		final int topCoordinatesOffset = hasBottom ? numEdges : 1;

		if ( !hasBottom )
		{
			vertexCoordinatesArray[ 0 ] = base.getTranslation();
		}

		if ( !hasBottom )
		{
			vertexCoordinatesArray[ topCoordinatesOffset ] = base.transform( 0.0 , 0.0 , height );
		}

		for ( int i = 0 ; i < numEdges ; i++ )
		{
			final double rad = (double)i * radStep;
			final double x   = Math.sin( rad ) * radiusBottom;
			final double y   = -Math.cos( rad ) * radiusBottom;

			if ( hasBottom )
			{
				vertexCoordinatesArray[ i ] = base.transform( x , y , 0.0 );
			}

			if ( hasTop )
			{
				vertexCoordinatesArray[ topCoordinatesOffset + i ] = base.transform( x , y , height );
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

			final Point2D.Float[] texturePoints = ( bottomMap != null ) ? bottomMap.generate( bottomMaterial , vertexCoordinates , vertexIndices , false ) : null;
			addFace( new Face3D( this , vertexIndices , bottomMaterial , texturePoints , null , false , false ) );
		}

		/*
		 * Circumference.
		 */
		for ( int i1 = 0 ; i1 < numEdges ; i1++ )
		{
			final int   i2 = ( i1 + 1 ) % numEdges;

			final int[] vertexIndices;

			if ( !hasTop )
			{
				vertexIndices = flipNormals ? new int[] { numEdges , i1 , i2 } : new int[] { i2 , i1 , numEdges };
			}
			else if ( !hasBottom )
			{
				vertexIndices = flipNormals ? new int[] { 1 + i2 , 1 + i1 , 0 } : new int[] { 0 , 1 + i1 , 1 + i2 };
			}
			else
			{
				vertexIndices = flipNormals ? new int[] { numEdges + i2 , numEdges + i1 , i1 , i2 } : new int[] { i2 , i1 , numEdges + i1 , numEdges + i2 };
			}

			final Point2D.Float[] texturePoints = ( sideMap != null ) ? sideMap.generate( sideMaterial , vertexCoordinates , vertexIndices , false ) : null;
			addFace( new Face3D( this , vertexIndices , sideMaterial , texturePoints , null , smoothCircumference , false ) );
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

			final Point2D.Float[] texturePoints = ( topMap != null ) ? topMap.generate( topMaterial , vertexCoordinates , vertexIndices , false ) : null;
			addFace( new Face3D( this , vertexIndices , topMaterial , texturePoints , null , false , false ) );
		}
	}
}
