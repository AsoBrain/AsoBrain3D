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

import java.awt.geom.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;

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
	 * Radius of sphere.
	 */
	public final double radius;

	/**
	 * Constructor for sphere.
	 *
	 * @param   radius      Radius of sphere.
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 * @param   material    Material of sphere.
	 */
	public Sphere3D( final double radius , final int p , final int q , final Material material )
	{
		this( radius , p , q , material , false );
	}

	/**
	 * Constructor for sphere.
	 *
	 * @param   radius          Radius of sphere.
	 * @param   p               Number of faces around Y-axis to approximate
	 *                          the sphere.
	 * @param   q               Number of faces around X/Z-axis to approximate
	 *                          the sphere.
	 * @param   material        Material of sphere.
	 * @param   flipNormals     <code>true</code> to flip the faces/normals of
	 *                          the sphere, turning it inside-out.
	 */
	public Sphere3D( final double radius , final int p , final int q , final Material material , final boolean flipNormals )
	{
		this.radius = radius;

		final int      vertexCount       = p * ( q - 1 ) + 2;
		final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>( vertexCount );
		final double[] vertexNormals     = new double[ vertexCount * 3 ];

		/*
		 * Generate vertices.
		 */
		int n = 0;

		vertexCoordinates.add( new Vector3D( 0.0 , 0.0 , -radius ) );
		vertexNormals[ n++ ] = 0.0;
		vertexNormals[ n++ ] = 0.0;
		vertexNormals[ n++ ] = -1.0;

		for ( int qc = 1 ; qc < q ; qc++ )
		{
			final double qrad = (double)qc * Math.PI / (double)q;
			final double sinq = Math.sin( qrad );
			final double cosq = Math.cos( qrad );

			for ( int pc = 0 ; pc < p ; pc++ )
			{
				final double prad = (double)pc * 2.0 * Math.PI / (double)p;
				final double normalX =  sinq * Math.sin( prad );
				final double normalY = -sinq * Math.cos( prad );
				final double normalZ = -cosq;

				vertexCoordinates.add( new Vector3D( radius * normalX , radius * normalY , radius * normalZ ) );
				vertexNormals[ n++ ] = normalX;
				vertexNormals[ n++ ] = normalY;
				vertexNormals[ n++ ] = normalZ;
			}
		}

		vertexCoordinates.add( new Vector3D( 0.0 , 0.0 , radius ) );
		vertexNormals[ n++ ] = 0.0;
		vertexNormals[ n++ ] = 0.0;
		vertexNormals[ n   ] = 1.0;

		if ( flipNormals )
		{
			for ( int i = 0 ; i < vertexNormals.length ; i++ )
			{
				vertexNormals[ i ] = -vertexNormals[ i ];
			}
		}

		setVertexCoordinates( vertexCoordinates );
		setVertexNormals( vertexNormals );

		/*
		 * Define faces
		 */
		final int lastQ = q - 1;
		final int lastV = vertexCount - 1;

		final float scaleU = 1.0f / (float)p;
		final float scaleV = 1.0f / (float)q;

		for ( int qc = 0 ; qc < q; qc++ )
		{
			for ( int pc = 0 ; pc < p; pc++ )
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

				final int[] vertexIndices;

				final Point2D.Float[] texturePoints;

				if ( qc == 0 )
				{
					vertexIndices = flipNormals ? new int[] { 0 , p4 , p3 } :
					                              new int[] { 0 , p3 , p4 };
					texturePoints = flipNormals ? new Point2D.Float[] { new Point2D.Float( uCenter , vBottom ) , new Point2D.Float( uRight , vTop ) , new Point2D.Float( uLeft  , vTop ) } :
					                              new Point2D.Float[] { new Point2D.Float( uCenter , vBottom ) , new Point2D.Float( uLeft  , vTop ) , new Point2D.Float( uRight , vTop ) };
				}
				else if ( qc < lastQ )
				{
					vertexIndices = flipNormals ? new int[] { p2 , p4 , p3 , p1 } :
					                              new int[] { p2 , p1 , p3 , p4 };
					texturePoints = flipNormals ? new Point2D.Float[] { new Point2D.Float( uRight , vBottom ) , new Point2D.Float( uRight , vTop    ) , new Point2D.Float( uLeft , vTop ) , new Point2D.Float( uLeft  , vBottom ) } :
					                              new Point2D.Float[] { new Point2D.Float( uRight , vBottom ) , new Point2D.Float( uLeft  , vBottom ) , new Point2D.Float( uLeft , vTop ) , new Point2D.Float( uRight , vTop    ) };
				}
				else // qc == lastQ
				{
					vertexIndices = flipNormals ? new int[] { p1 , p2 , lastV } :
					                              new int[] { p2 , p1 , lastV };
					texturePoints = flipNormals ? new Point2D.Float[] { new Point2D.Float( uLeft  , vBottom ) , new Point2D.Float( uRight , vBottom ) , new Point2D.Float( uCenter , vTop ) } :
					                              new Point2D.Float[] { new Point2D.Float( uRight , vBottom ) , new Point2D.Float( uLeft  , vBottom ) , new Point2D.Float( uCenter , vTop ) };
				}

				_faces.add( new Face3D( this , vertexIndices , material , texturePoints , null , true , false ) );
			}
		}
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final double radius = this.radius;
		return new Bounds3D( -radius,  -radius, -radius, radius, radius, radius );
	}

	@Override
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		if ( other instanceof Sphere3D ) /* sphere vs. sphere */
		{
			final Sphere3D sphere = (Sphere3D)other;
			result = GeometryTools.testSphereIntersection( radius, fromOtherToThis.xo, fromOtherToThis.yo, fromOtherToThis.zo,  sphere.radius );
		}
		else if ( other instanceof Box3D ) /* sphere vs. box */
		{
			final Box3D box = (Box3D)other;
			final double centerX = fromOtherToThis.inverseTransformX( 0.0, 0.0, 0.0 );
			final double centerY = fromOtherToThis.inverseTransformY( 0.0, 0.0, 0.0 );
			final double centerZ = fromOtherToThis.inverseTransformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereBoxIntersection( centerX, centerY, centerZ, radius, 0.0, 0.0, 0.0, box.getDX(), box.getDY(), box.getDZ() );
		}
		else
		{
			result = super.collidesWith( fromOtherToThis, other );
		}

		return result;
	}
}
