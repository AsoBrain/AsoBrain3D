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
	public final float dx;

	/**
	 * Height of sphere/egg (y-axis).
	 */
	public final float dy;

	/**
	 * Depth of sphere/egg (z-axis).
	 */
	public final float dz;

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
	public Sphere3D( final Matrix3D xform , final float dx , final float dy , final float dz , final int p , final int q , final TextureSpec texture , final boolean smooth )
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
	public Sphere3D( final Matrix3D xform , final float radius , final int p , final int q )
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

		final int vertexCount = p * ( q - 1 ) + 2;
		final int faceCount = p * q;

		final float[] vertices = new float[ vertexCount * 3 ];
		final int[][] faceVert = new int[ faceCount ][];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		vertices[ v++ ] = 0;
		vertices[ v++ ] = 0;
		vertices[ v++ ] = dz / -2.0f;

		for ( int qc = 1 ; qc < q ; qc++ )
		{
			final float qa = (float)( qc * Math.PI / q );

			final float radiusX =  (float)( Math.sin( qa ) * dx / 2 );
			final float radiusY =  (float)( Math.sin( qa ) * dy / 2 );
			final float circleZ = -(float)( Math.cos( qa ) * dz / 2 );

			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final float pa = (float)( pc * 2 * Math.PI / p );

				vertices[ v++ ] =  (float)( Math.sin( pa ) * radiusX );
				vertices[ v++ ] = -(float)( Math.cos( pa ) * radiusY );
				vertices[ v++ ] = circleZ;
			}
		}

		vertices[ v++ ] = 0;
		vertices[ v++ ] = 0;
		vertices[ v++ ] = dz / 2.0f;

		xform.transform( vertices , vertices , v / 3 );

		/*
		 * Define faces (new style)
		 */
		final int lastQ = q - 1;
		final int lastV = vertexCount - 1;

		int f = 0;
		for ( int qc = 0 ; qc < q ; qc++ )
		{
			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final int p1 = ( qc - 1 ) * p +     pc             + 1;
				final int p2 = ( qc - 1 ) * p + ( ( pc + 1 ) % p ) + 1;
				final int p3 =   qc       * p +     pc             + 1;
				final int p4 =   qc       * p + ( ( pc + 1 ) % p ) + 1;

				faceVert[ f++ ] = ( qc == 0     ) ? new int[] { 0 , p3 , p4 } :
				                  ( qc == lastQ ) ? new int[] { p2 , p1 , lastV } :
				                                    new int[] { p2 , p1 , p3 , p4 };
			}
		}

		/*
		 * Set Object3D properties.
		 */
		set( vertices ,  faceVert , actualTexture , smooth );
	}
}
