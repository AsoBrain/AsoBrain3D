/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
	 * @param   texture     Texture of sphere.
	 * @param   smooth      Smooth surface.
	 */
	public Sphere3D( final Matrix3D xform , final double dx , final double dy , final double dz , final int p , final int q , final TextureSpec texture , final boolean smooth )
	{
		this.xform =  xform;
		this.dx    = dx;
		this.dy    = dy;
		this.dz    = dz;

		generate( p , q , ( texture == null ) ? new TextureSpec() : texture , smooth );
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
		this( xform , radius * 2 , radius * 2 , radius * 2 , p , q , null , false );
	}

	/**
	 * Generate Object3D properties.
	 *
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 * @param   texture     Texture of sphere.
	 * @param   smooth      Smooth surface.
	 */
	public void generate( final int p , final int q , final TextureSpec texture , final boolean smooth )
	{
		final TextureSpec actualTexture = ( texture == null ) ? new TextureSpec() : texture;

		final int      pointCount  = p * ( q - 1 ) + 2;
		final double[] pointCoords = new double[ pointCount * 3 ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		pointCoords[ v++ ] = 0;
		pointCoords[ v++ ] = 0;
		pointCoords[ v++ ] = dz / -2.0;

		for ( int qc = 1 ; qc < q ; qc++ )
		{
			final double qa = qc * Math.PI / q;

			final double radiusX =  0.5 * dx * Math.sin( qa );
			final double radiusY =  0.5 * dy * Math.sin( qa );
			final double circleZ = -0.5 * dz * Math.cos( qa );

			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final double pa = pc * 2 * Math.PI / p;

				pointCoords[ v++ ] =  radiusX * Math.sin( pa );
				pointCoords[ v++ ] = -radiusY * Math.cos( pa );
				pointCoords[ v++ ] = circleZ;
			}
		}

		pointCoords[ v++ ] = 0;
		pointCoords[ v++ ] = 0;
		pointCoords[ v   ] = dz / 2.0;

		clear();
		setPointCoords( xform.transform( pointCoords , pointCoords , pointCount ) );

		/*
		 * Define faces (new style)
		 */
		final int lastQ = q - 1;
		final int lastV = pointCount - 1;

		for ( int qc = 0 ; qc < q ; qc++ )
		{
			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final int p1 = ( qc - 1 ) * p +     pc             + 1;
				final int p2 = ( qc - 1 ) * p + ( ( pc + 1 ) % p ) + 1;
				final int p3 =   qc       * p +     pc             + 1;
				final int p4 =   qc       * p + ( ( pc + 1 ) % p ) + 1;

				final int[] pointIndices = ( qc == 0     ) ? new int[] { 0 , p3 , p4 }
				                         : ( qc == lastQ ) ? new int[] { p2 , p1 , lastV }
				                         :                   new int[] { p2 , p1 , p3 , p4 };

				addFace( pointIndices , actualTexture , smooth );
			}
		}
	}
}
