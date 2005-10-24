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
package ab.j3d.model;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

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
	 * Transformation applied to all vertices of the box.
	 */
	public final Matrix3D xform;

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
	 * @param   xform               Transform to apply to the cylinder's vertices.
	 * @param   radiusBottom        Radius at bottom (z=0).
	 * @param   radiusTop           Radius at top (z=height).
	 * @param   height              Height of cylinder (z-axis).
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   texture             Texture of cylinder.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   smoothCaps          Apply smoothing to caps of cylinder.
	 */
	public Cylinder3D( final Matrix3D xform , final double radiusBottom , final double radiusTop , final double height , final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( radiusBottom < 0 || radiusTop < 0 || height < 0 || numEdges < 3 )
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", text=" + texture + ')' );

		if ( radiusBottom == 0 && radiusTop == 0 )
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );

		this.xform        = xform;
		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		generate( numEdges , texture , smoothCircumference , smoothCaps );
	}

	/**
	 * Generate Object3D properties for cylinder.
	 *
	 * @param   numEdges            Number of edges to approximate circle (minimum: 3).
	 * @param   texture             Texture of cylinder.
	 * @param   smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param   smoothCaps          Apply smoothing to caps of cylinder.
	 */
	public void generate( final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		/*
		 * Setup properties of cylinder.
		 */
		final boolean  hasBottom   = ( radiusBottom > 0 );
		final boolean  hasTop      = ( radiusTop > 0 );
		final int      pointCount  = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final double[] pointCoords = new double[ pointCount * 3 ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			final double step = 2.0 * Math.PI / (double)numEdges;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double a = (double)i * step;
				pointCoords[ v++ ] =  Math.sin( a ) * radiusBottom;
				pointCoords[ v++ ] = -Math.cos( a ) * radiusBottom;
				pointCoords[ v++ ] = 0.0;
			}
		}
		else
		{
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
		}

		if ( hasTop )
		{
			final double step = 2.0 * Math.PI / (double)numEdges;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				final double a = (double)i * step;
				pointCoords[ v++ ] =  Math.sin( a ) * radiusTop;
				pointCoords[ v++ ] = -Math.cos( a ) * radiusTop;
				pointCoords[ v++ ] = height;
			}
		}
		else
		{
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v++ ] = 0.0;
			pointCoords[ v/*++*/ ] = height;
		}

		clear();
		setPointCoords( xform.transform( pointCoords , pointCoords , pointCount ) );

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom )
		{
			final int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = i;

			addFace( fv , texture , smoothCaps );
		}

		/*
		 * Circumference.
		 */
		if ( hasBottom && hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { i2 , i1 , numEdges + i1 , numEdges + i2 };

				addFace( fv , texture , smoothCircumference );
			}
		}
		else if ( hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { i2 , i1 , numEdges };

				addFace( fv , texture , smoothCircumference );
			}
		}
		else if ( hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				final int   i2 = ( i1 + 1 ) % numEdges;
				final int[] fv = { 0 , 1 + i1 , 1 + i2 };

				addFace( fv , texture , smoothCircumference );
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop )
		{
			final int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = ( hasBottom ? numEdges : 1 ) + ( numEdges - i - 1 );

			addFace( fv , texture , smoothCaps );
		}
	}
}
