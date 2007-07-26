/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2006 Peter S. Heijnen
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

import ab.j3d.Material;
import ab.j3d.Matrix3D;

/**
 * This class defines a 3D sphere.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Sphere3D
	extends Object3D
{
	/**
	 * Transformation applied to all vertices of the box.
	 */
	public final Matrix3D xform;

	/**
	 * Width of sphere/egg (x-axis).
	 */
	public final double dx;

	/**
	 * Height of sphere/egg (y-axis).
	 */
	public final double dy;

	/**
	 * Depth of sphere/egg (z-axis).
	 */
	public final double dz;

	/**
	 * Constructor for sphere.
	 *
	 * @param   xform       Transformation to apply to the object's vertices.
	 * @param   dx          Width of sphere (x-axis).
	 * @param   dy          Height of sphere (y-axis).
	 * @param   dz          Depth of sphere (z-axis).
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 * @param   material    Material of sphere.
	 * @param   smooth      Smooth surface.
	 */
	public Sphere3D( final Matrix3D xform , final double dx , final double dy , final double dz , final int p , final int q , final Material material , final boolean smooth )
	{
		this.xform =  xform;
		this.dx    = dx;
		this.dy    = dy;
		this.dz    = dz;

		generate( p , q , ( material == null ) ? new Material() : material , smooth );
	}

	/**
	 * Constructor for sphere.
	 *
	 * @param   xform       Transformation to apply to the object's vertices.
	 * @param   radius      Radius of sphere.
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 */
	public Sphere3D( final Matrix3D xform , final double radius , final int p , final int q )
	{
		this( xform , radius * 2.0 , radius * 2.0 , radius * 2.0 , p , q , null , false );
	}

	/**
	 * Generate Object3D properties.
	 *
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 * @param   material    Material of sphere.
	 * @param   smooth      Smooth surface.
	 */
	public void generate( final int p , final int q , final Material material , final boolean smooth )
	{
		final int      vertexCount       = p * ( q - 1 ) + 2;
		final double[] vertexCoordinates = new double[ vertexCount * 3 ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		vertexCoordinates[ v++ ] = 0.0;
		vertexCoordinates[ v++ ] = 0.0;
		vertexCoordinates[ v++ ] = dz / -2.0;

		for ( int qc = 1 ; qc < q ; qc++ )
		{
			final double qa = (double)qc * Math.PI / (double)q;

			final double radiusX =  0.5 * dx * Math.sin( qa );
			final double radiusY =  0.5 * dy * Math.sin( qa );
			final double circleZ = -0.5 * dz * Math.cos( qa );

			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final double pa = (double)pc * 2.0 * Math.PI / (double)p;

				vertexCoordinates[ v++ ] =  radiusX * Math.sin( pa );
				vertexCoordinates[ v++ ] = -radiusY * Math.cos( pa );
				vertexCoordinates[ v++ ] = circleZ;
			}
		}

		vertexCoordinates[ v++ ] = 0.0;
		vertexCoordinates[ v++ ] = 0.0;
		vertexCoordinates[ v   ] = dz / 2.0;

		clear();
		setVertexCoordinates( xform.transform( vertexCoordinates , vertexCoordinates , vertexCount ) );

		/*
		 * Define faces
		 */
		final int lastQ = q - 1;
		final int lastV = vertexCount - 1;

		final float scaleU = 1.0f / (float)p;
		final float scaleV = 1.0f / (float)q;

		for ( int qc = 0 ; qc < q ; qc++ )
		{
			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final int p1 = ( qc - 1 ) * p +     pc             + 1;
				final int p2 = ( qc - 1 ) * p + ( ( pc + 1 ) % p ) + 1;
				final int p3 =   qc       * p +     pc             + 1;
				final int p4 =   qc       * p + ( ( pc + 1 ) % p ) + 1;

				final float uLeft   = (float)pc         * scaleU;
				final float uRight  = (float)( pc + 1 ) * scaleU;
				final float uCenter = ( uLeft + uRight )  * 0.5f;

				final float vBottom = (float)( q -   qc       ) * scaleV;
				final float vTop    = (float)( q - ( qc + 1 ) ) * scaleV;

				final int[]   vertexIndices;
				final float[] textureU;
				final float[] textureV;

				if ( qc == 0 )
				{
					vertexIndices = new int[]   {     0             , p3    , p4     };
				    textureU      = new float[] {     uCenter       , uLeft , uRight };
					textureV      = new float[] {     vBottom       , vTop  , vTop   };
				}
				else if ( qc < lastQ )
				{
					vertexIndices = new int[]   { p2      , p1      , p3    , p4     };
					textureU      = new float[] { uRight  , uLeft   , uLeft , uRight };
					textureV      = new float[] { vBottom , vBottom , vTop  , vTop   };
				}
				else // qc == lastQ
				{
					vertexIndices = new int[]   { p2      , p1      ,     lastV      };
					textureU      = new float[] { uRight  , uLeft   ,     uCenter    };
					textureV      = new float[] { vBottom , vBottom ,     vTop       };
				}

				addFace( vertexIndices , material , textureU , textureV , 1.0f , smooth , false );
			}
		}
	}
}
