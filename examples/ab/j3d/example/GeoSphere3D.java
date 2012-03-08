/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.example;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.model.*;

/**
 * A geodesic sphere is an approximation of a sphere, based on subdivision of a
 * regular polyhedron according to the great circles (geodesics) of the sphere.
 * All faces are approximately equilateral triangles.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class GeoSphere3D
	extends Object3D
{
	/**
	 * Constructs a new geodesic sphere.
	 *
	 * @param   radius          Radius of the sphere.
	 * @param   subdivisions    Number of subdivision steps. (More is smoother.)
	 * @param   appearance        Appearance of the sphere.
	 */
	public GeoSphere3D( final double radius, final int subdivisions, final Appearance appearance )
	{
//		createIcosahedralGeometry( radius, subdivisions, appearance );
		createOctahedralGeometry( radius, subdivisions, appearance );
	}

	/**
	 * Creates geodesic sphere geometry based on an octahedron.
	 *
	 * @param   radius          Radius of the sphere.
	 * @param   subdivisions    Number of (recursive) subdivisions.
	 * @param   appearance        Appearance to be used.
	 */
	private void createOctahedralGeometry( final double radius, final int subdivisions, final Appearance appearance )
	{
		final Vector3D p0 = new Vector3D( 0.0, 0.0, radius );
		final Vector3D p1 = new Vector3D( -radius, 0.0, 0.0 );
		final Vector3D p2 = new Vector3D( 0.0, -radius, 0.0 );
		final Vector3D p3 = new Vector3D( radius, 0.0, 0.0 );
		final Vector3D p4 = new Vector3D( 0.0, radius, 0.0 );
		final Vector3D p5 = new Vector3D( 0.0, 0.0, -radius );

		final Object3DBuilder builder = getBuilder();

		addFaces( builder, p0, p2, p1, radius, subdivisions, appearance );
		addFaces( builder, p0, p3, p2, radius, subdivisions, appearance );
		addFaces( builder, p0, p4, p3, radius, subdivisions, appearance );
		addFaces( builder, p0, p1, p4, radius, subdivisions, appearance );
		addFaces( builder, p5, p1, p2, radius, subdivisions, appearance );
		addFaces( builder, p5, p2, p3, radius, subdivisions, appearance );
		addFaces( builder, p5, p3, p4, radius, subdivisions, appearance );
		addFaces( builder, p5, p4, p1, radius, subdivisions, appearance );
	}

	private void createIcosahedralGeometry( final double radius, final int subdivisions, final Appearance appearance )
	{
		/*
		 * Source: http://en.wikipedia.org/wiki/Icosahedron
		 * And minimal high school math.
		 *
		 * golden ratio: φ = (1 + √5)/2
		 *
		 * vertex coordinates for edge length a:
		 *   0, ±0.5a, ±0.5φa
		 *   ±0.5a, ±0.5φa, 0
		 *   ±0.5φa, 0, ±0.5a
		 *
		 * circumscribed sphere radius r:
		 *   r = 0.5a √( φ√5 )
		 *
		 * gives:
		 *   a = 2r / √( φ√5 )
		 *
		 * vertex coordinates for circumscribed sphere radius r:
		 *   0, ±r / √( φ√5 ), ±φr / √( φ√5 )
		 *   ±r / √( φ√5 ), ±φr / √( φ√5 ), 0
		 *   ±φr / √( φ√5 ), 0, ±r / √( φ√5 )
		 *
		 * But that doesn't yield a vertex at (0,0,-radius) and (0,0,radius).
		 *
		 * So either rotate it or see: http://www.cs.umbc.edu/~squire/reference/polyhedra.shtml
		 * NOTE: 26.5650512 degrees = acos(1 / (sqrt(φ * sqrt(5)) * cos(54 degrees)))
		 */

		final double phi = ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
		final double factor1 = 1.0 / Math.sqrt( phi * Math.sqrt( 5.0 ) );
		final double factor2 = phi * factor1;

		final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>();
		final List<Vector3D> vertexNormals = new ArrayList<Vector3D>();

		final Object3DBuilder builder = getBuilder();

		vertexCoordinates.add( new Vector3D( 0.0, -factor1 * radius, -factor2 * radius ) );
		vertexCoordinates.add( new Vector3D( 0.0,  factor1 * radius, -factor2 * radius ) );
		vertexCoordinates.add( new Vector3D( 0.0,  factor1 * radius,  factor2 * radius ) );
		vertexCoordinates.add( new Vector3D( 0.0, -factor1 * radius,  factor2 * radius ) );
		vertexCoordinates.add( new Vector3D( -factor1 * radius, -factor2 * radius, 0.0 ) );
		vertexCoordinates.add( new Vector3D(  factor1 * radius, -factor2 * radius, 0.0 ) );
		vertexCoordinates.add( new Vector3D(  factor1 * radius,  factor2 * radius, 0.0 ) );
		vertexCoordinates.add( new Vector3D( -factor1 * radius,  factor2 * radius, 0.0 ) );
		vertexCoordinates.add( new Vector3D( -factor2 * radius, 0.0, -factor1 * radius ) );
		vertexCoordinates.add( new Vector3D( -factor2 * radius, 0.0,  factor1 * radius ) );
		vertexCoordinates.add( new Vector3D(  factor2 * radius, 0.0,  factor1 * radius ) );
		vertexCoordinates.add( new Vector3D(  factor2 * radius, 0.0, -factor1 * radius ) );

		final List<Vertex3D> vertices = new ArrayList<Vertex3D>( 12 );
		for ( int i = 0 ; i < vertexCoordinates.size() ; i++ )
		{
			final Vector3D point = vertexCoordinates.get( i );
			vertexNormals.add( point.normalize() );
			vertices.add( new Vertex3D( point, i ) );
		}

		// FIXME: Use a builder, same as 'createOctahedralGeometry'.
//		addSubdividedFace( vertexCoordinates, vertexNormals, vertices.get( 0 ), vertices.get( 1 ), vertices.get( 2 ), radius, subdivisions, appearance );
//
//		setVertexCoordinates( vertexCoordinates );
//		setVertexNormals( vectorsToDoubles( vertexNormals ) );
	}

	/**
	 * Adds spherical geometry starting with the given triangle.
	 *
	 * @param   builder         Builder to be used.
	 * @param   v0              Vertex coordinate.
	 * @param   v1              Vertex coordinate.
	 * @param   v2              Vertex coordinate.
	 * @param   radius          Radius of the sphere.
	 * @param   subdivisions    Number of (recursive) subdivisions.
	 * @param   appearance        Appearance to be used.
	 */
	private static void addFaces( final Object3DBuilder builder, final Vector3D v0, final Vector3D v1, final Vector3D v2, final double radius, final int subdivisions, final Appearance appearance )
	{
		if ( subdivisions == 0 )
		{
			final Vector3D[] coordinates = { v0, v1, v2 };
			final Vector3D[] normals =
				{
					v0.multiply( 1.0 / radius ),
					v1.multiply( 1.0 / radius ),
					v2.multiply( 1.0 / radius ),
				};

			builder.addFace( coordinates, appearance, null, normals, true, false );
		}
		else
		{
			final Vector3D p0 = subdivide( v0, v1, radius );
			final Vector3D p1 = subdivide( v1, v2, radius );
			final Vector3D p2 = subdivide( v2, v0, radius );

			addFaces( builder, v0, p0, p2, radius, subdivisions - 1, appearance );
			addFaces( builder, v1, p1, p0, radius, subdivisions - 1, appearance );
			addFaces( builder, v2, p2, p1, radius, subdivisions - 1, appearance );
			addFaces( builder, p0, p1, p2, radius, subdivisions - 1, appearance );
		}
	}

	/**
	 * Calculates the nearest point on the geodesic through the given points
	 * that is equidistant to both points.
	 *
	 * @param   p0      First point.
	 * @param   p1      Second point.
	 * @param   radius  Radius of the sphere.
	 *
	 * @return  Point on the sphere, between the given points.
	 */
	private static Vector3D subdivide( final Vector3D p0, final Vector3D p1, final double radius )
	{
		final double x = ( p0.x + p1.x ) / 2.0;
		final double y = ( p0.y + p1.y ) / 2.0;
		final double z = ( p0.z + p1.z ) / 2.0;
		final double f = radius / Math.sqrt( x * x + y * y + z * z );
		return new Vector3D( x * f, y * f, z * f );
	}
}
