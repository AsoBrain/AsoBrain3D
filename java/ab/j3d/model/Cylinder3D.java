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

import ab.j3d.Material;

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
	public Cylinder3D( final double radiusBottom , final double radiusTop , final double height , final int numEdges , final Material material , final boolean smoothCircumference , final boolean smoothCaps , final boolean hasTopCap , final boolean hasBottomCap )
	{
		if ( radiusBottom < 0.0 || radiusTop < 0.0 || height < 0.0 || numEdges < 3 )
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", material=" + material + ')' );

		if ( radiusBottom == 0.0 && radiusTop == 0.0 )
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );

		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		/*
		 * Setup properties of cylinder.
		 */
		final boolean  hasBottom         = ( radiusBottom > 0.0 );
		final boolean  hasTop            = ( radiusTop    > 0.0 );
		final int      vertexCount       = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final double[] vertexCoordinates = new double[ vertexCount * 3 ];

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
				vertexCoordinates[ v++ ] =  Math.sin( rad ) * radiusBottom;
				vertexCoordinates[ v++ ] = -Math.cos( rad ) * radiusBottom;
				vertexCoordinates[ v++ ] = 0.0;
			}
		}
		else
		{
			vertexCoordinates[ v++ ] = 0.0;
			vertexCoordinates[ v++ ] = 0.0;
			vertexCoordinates[ v++ ] = 0.0;
		}

		if ( hasTop )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double rad = (double)i * radStep;
				vertexCoordinates[ v++ ] =  Math.sin( rad ) * radiusTop;
				vertexCoordinates[ v++ ] = -Math.cos( rad ) * radiusTop;
				vertexCoordinates[ v++ ] = height;
			}
		}
		else
		{
			vertexCoordinates[ v++ ] = 0.0;
			vertexCoordinates[ v++ ] = 0.0;
			vertexCoordinates[ v/*++*/ ] = height;
		}

		setVertexCoordinates( vertexCoordinates );

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
}
