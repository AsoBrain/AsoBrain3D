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

/**
 * This class defines a 3D sphere.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Sphere3D
	extends Object3D
{
	/**
	 * Radius of sphere.
	 */
	public final double _radius;

	/**
	 * Constructor for sphere.
	 *
	 * @param   radius      Radius of sphere.
	 * @param   p           Number of faces around Y-axis to approximate the sphere.
	 * @param   q           Number of faces around X/Z-axis to approximate the sphere.
	 * @param   appearance  Appearance of sphere.
	 */
	public Sphere3D( final double radius, final int p, final int q, final Appearance appearance )
	{
		this( radius, p, q, appearance, false );
	}

	/**
	 * Constructor for sphere.
	 *
	 * @param   radius          Radius of sphere.
	 * @param   p               Number of faces around Y-axis to approximate
	 *                          the sphere.
	 * @param   q               Number of faces around X/Z-axis to approximate
	 *                          the sphere.
	 * @param   appearance        Appearance of sphere.
	 * @param   flipNormals     <code>true</code> to flip the faces/normals of
	 *                          the sphere, turning it inside-out.
	 */
	public Sphere3D( final double radius, final int p, final int q, final Appearance appearance, final boolean flipNormals )
	{
		_radius = radius;

		final int vertexCount = p * ( q - 1 ) + 2;
		final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>( vertexCount );
		final List<Vector3D> vertexNormals = new ArrayList<Vector3D>( vertexCount );

		/*
		 * Generate vertices.
		 */
		vertexCoordinates.add( new Vector3D( 0.0, 0.0, -radius ) );
		vertexNormals.add( flipNormals ? Vector3D.POSITIVE_Z_AXIS : Vector3D.NEGATIVE_Z_AXIS );

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

				vertexCoordinates.add( new Vector3D( radius * normalX, radius * normalY, radius * normalZ ) );
				vertexNormals.add( flipNormals ? new Vector3D( -normalX, -normalY, -normalZ ) : new Vector3D( normalX, normalY, normalZ ) );
			}
		}

		vertexCoordinates.add( new Vector3D( 0.0, 0.0, radius ) );
		vertexNormals.add( flipNormals ? Vector3D.NEGATIVE_Z_AXIS : Vector3D.POSITIVE_Z_AXIS );

		setVertexCoordinates( vertexCoordinates );

		/*
		 * Define faces
		 */
		final int lastQ = q - 1;
		final int lastV = vertexCount - 1;

		final float scaleU = 1.0f / (float)p;
		final float scaleV = 1.0f / (float)q;

		final FaceGroup faceGroup = getFaceGroup( appearance, true, false );
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

				final float[] texturePoints;

				if ( qc == 0 )
				{
					vertexIndices = flipNormals ? new int[] { 0, p4, p3 } :
					                              new int[] { 0, p3, p4 };
					texturePoints = flipNormals ? new float[] { uCenter, vBottom, uRight, vTop, uLeft , vTop } :
					                              new float[] { uCenter, vBottom, uLeft , vTop, uRight, vTop };
				}
				else if ( qc < lastQ )
				{
					vertexIndices = flipNormals ? new int[] { p2, p4, p3, p1 } :
					                              new int[] { p2, p1, p3, p4 };
					texturePoints = flipNormals ? new float[] { uRight, vBottom, uRight, vTop   , uLeft, vTop, uLeft , vBottom } :
					                              new float[] { uRight, vBottom, uLeft , vBottom, uLeft, vTop, uRight, vTop    };
				}
				else // qc == lastQ
				{
					vertexIndices = flipNormals ? new int[] { p1, p2, lastV } :
					                              new int[] { p2, p1, lastV };
					texturePoints = flipNormals ? new float[] { uLeft , vBottom, uRight, vBottom, uCenter, vTop } :
					                              new float[] { uRight, vBottom, uLeft , vBottom, uCenter, vTop };
				}

				final Face3D face = new Face3D( this, vertexIndices, texturePoints, null );
				for ( final Face3D.Vertex vertex : face.getVertices() )
				{
					vertex.setNormal( vertexNormals.get( vertex.vertexCoordinateIndex ) );
				}
				faceGroup.addFace( face );
			}
		}
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final double radius = _radius;
		return new Bounds3D( -radius,  -radius, -radius, radius, radius, radius );
	}

	@Override
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		if ( other instanceof Sphere3D ) /* sphere vs. sphere */
		{
			final Sphere3D sphere = (Sphere3D)other;
			result = GeometryTools.testSphereIntersection( _radius, fromOtherToThis.xo, fromOtherToThis.yo, fromOtherToThis.zo,  sphere._radius );
		}
		else if ( other instanceof Box3D ) /* sphere vs. box */
		{
			final Box3D box = (Box3D)other;
			final double centerX = fromOtherToThis.inverseTransformX( 0.0, 0.0, 0.0 );
			final double centerY = fromOtherToThis.inverseTransformY( 0.0, 0.0, 0.0 );
			final double centerZ = fromOtherToThis.inverseTransformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereBoxIntersection( centerX, centerY, centerZ, _radius, 0.0, 0.0, 0.0, box.getDX(), box.getDY(), box.getDZ() );
		}
		else if ( other instanceof Cylinder3D ) /* sphere vs. cylinder */
		{
			final Cylinder3D cylinder = (Cylinder3D)other;
			result = GeometryTools.testSphereCylinderIntersection( 0.0, 0.0, 0.0, _radius, fromOtherToThis, cylinder.height, cylinder.radius );
		}
		else
		{
			result = super.collidesWith( fromOtherToThis, other );
		}

		return result;
	}
}
