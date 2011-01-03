/* $Id$
 *
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
 */
package ab.j3d.example;

import java.util.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.model.Face3D.Vertex;

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
	 * @param   material        Material of the sphere.
	 */
	public GeoSphere3D( final double radius, final int subdivisions, final Material material )
	{
		createIcosahedralGeometry( radius, subdivisions, material );
//		createOctahedralGeometry( radius, subdivisions, material );
	}

	private void createOctahedralGeometry( final double radius, final int subdivisions, final Material material )
	{
		final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>();
		final List<Vector3D> vertexNormals = new ArrayList<Vector3D>();

		final Vector3D p0 = new Vector3D( 0.0, 0.0, radius );
		final Vector3D p1 = new Vector3D( -radius, 0.0, 0.0 );
		final Vector3D p2 = new Vector3D( 0.0, -radius, 0.0 );
		final Vector3D p3 = new Vector3D( radius, 0.0, 0.0 );
		final Vector3D p4 = new Vector3D( 0.0, radius, 0.0 );
		final Vector3D p5 = new Vector3D( 0.0, 0.0, -radius );

		vertexCoordinates.add( p0 );
		vertexCoordinates.add( p1 );
		vertexCoordinates.add( p2 );
		vertexCoordinates.add( p3 );
		vertexCoordinates.add( p4 );
		vertexCoordinates.add( p5 );

		vertexNormals.add( p0.multiply( 1.0 / radius ) );
		vertexNormals.add( p1.multiply( 1.0 / radius ) );
		vertexNormals.add( p2.multiply( 1.0 / radius ) );
		vertexNormals.add( p3.multiply( 1.0 / radius ) );
		vertexNormals.add( p4.multiply( 1.0 / radius ) );
		vertexNormals.add( p5.multiply( 1.0 / radius ) );

		final Vertex v0 = new Vertex( p0, 0 );
		final Vertex v1 = new Vertex( p1, 1 );
		final Vertex v2 = new Vertex( p2, 2 );
		final Vertex v3 = new Vertex( p3, 3 );
		final Vertex v4 = new Vertex( p4, 4 );
		final Vertex v5 = new Vertex( p5, 5 );

		addSubdividedFace( vertexCoordinates, vertexNormals, v0, v1, v2, radius, subdivisions, material );
		addSubdividedFace( vertexCoordinates, vertexNormals, v0, v2, v3, radius, subdivisions, Materials.GRAY );
		addSubdividedFace( vertexCoordinates, vertexNormals, v0, v3, v4, radius, subdivisions, material );
		addSubdividedFace( vertexCoordinates, vertexNormals, v0, v4, v1, radius, subdivisions, Materials.GRAY );

		addSubdividedFace( vertexCoordinates, vertexNormals, v5, v2, v1, radius, subdivisions, Materials.GRAY );
		addSubdividedFace( vertexCoordinates, vertexNormals, v5, v3, v2, radius, subdivisions, material );
		addSubdividedFace( vertexCoordinates, vertexNormals, v5, v4, v3, radius, subdivisions, Materials.GRAY );
		addSubdividedFace( vertexCoordinates, vertexNormals, v5, v1, v4, radius, subdivisions, material );

		setVertexCoordinates( vertexCoordinates );
		setVertexNormals( vectorsToDoubles( vertexNormals ) );

//		createFaces( vertexCoordinates, vertexNormals, material );
	}

	private void createIcosahedralGeometry( final double radius, final int subdivisions, final Material material )
	{
		/*
		 * Source: http://en.wikipedia.org/wiki/Icosahedron
		 * And minimal high school math.
		 *
		 * golden ratio: ? = (1 + ?5)/2
		 *
		 * vertex coordinates for edge length a:
		 *   0, ±0.5a, ±0.5?a
		 *   ±0.5a, ±0.5?a, 0
		 *   ±0.5?a, 0, ±0.5a
		 *
		 * circumscribed sphere radius r:
		 *   r = 0.5a ?( ??5 )
		 *
		 * gives:
		 *   a = 2r / ?( ? ?5 )
		 *
		 * vertex coordinates for circumscribed sphere radius r:
		 *   0, ±r / ?( ? ?5 ), ±?r / ?( ? ?5 )
		 *   ±r / ?( ? ?5 ), ±?r / ?( ? ?5 ), 0
		 *   ±?r / ?( ? ?5 ), 0, ±r / ?( ? ?5 )
		 *
		 *
		 * hmm... see also http://www.cs.umbc.edu/~squire/reference/polyhedra.shtml
		 */

		final double phi = ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
		final double factor1 = 1.0 / Math.sqrt( phi * Math.sqrt( 5.0 ) );
		final double factor2 = phi * factor1;

		final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>();
		final List<Vector3D> vertexNormals = new ArrayList<Vector3D>();

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

		final List<Vertex> vertices = new ArrayList<Vertex>( 12 );
		for ( int i = 0 ; i < vertexCoordinates.size() ; i++ )
		{
			final Vector3D point = vertexCoordinates.get( i );
			System.out.println( " - point = " + point );
			vertexNormals.add( point.normalize() );
			vertices.add( new Vertex( point, i ) );
		}

		addSubdividedFace( vertexCoordinates, vertexNormals, vertices.get( 0 ), vertices.get( 1 ), vertices.get( 2 ), radius, subdivisions, material );

		setVertexCoordinates( vertexCoordinates );
		setVertexNormals( vectorsToDoubles( vertexNormals ) );
	}

	private static double[] vectorsToDoubles( final List<Vector3D> vectors )
	{
		final double[] result = new double[ vectors.size() * 3 ];
		for ( int i = 0; i < vectors.size(); i++ )
		{
			final Vector3D vector = vectors.get( i );
			result[ i * 3 ] = vector.x;
			result[ i * 3 + 1 ] = vector.y;
			result[ i * 3 + 2 ] = vector.z;
		}
		return result;
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new GeoSphere3D( 1.0, 1, Materials.WHITE );
	}

	private void addSubdividedFace( final List<Vector3D> vertexCoordinates, final List<Vector3D> vertexNormals, final Vertex v0, final Vertex v1, final Vertex v2, final double radius, final int subdivisions, final Material material )
	{
		/*
		subdivide( face (p0, p1, p2) )
		{
			s0 = radius * normalize( (p0 + p1) / 2 )
			s1 = radius * normalize( (p1 + p2) / 2 )
			s2 = radius * normalize( (p2 + p0) / 2 )

			subdivide( face (p0, s0, s2) )
			subdivide( face (p1, s1, s0) )
			subdivide( face (p2, s2, s1) )
			subdivide( face (s0, s1, s2) )
		}
		*/

		if ( subdivisions == 0 )
		{
			System.out.println( "add face:" );
			System.out.println( " - v0 = " + v0 );
			System.out.println( " - v1 = " + v1 );
			System.out.println( " - v2 = " + v2 );
			addFace( new Face3D( this, Arrays.asList( v0, v2, v1 ), null, material, true, false ) );
		}
		else
		{
			final Vector3D p0 = subdivide( v0.point, v1.point, radius );
			final Vector3D p1 = subdivide( v1.point, v2.point, radius );
			final Vector3D p2 = subdivide( v2.point, v0.point, radius );

			final int i = vertexCoordinates.size();
			vertexCoordinates.add( p0 );
			vertexCoordinates.add( p1 );
			vertexCoordinates.add( p2 );

			final Vertex s0 = new Vertex( p0, i );
			final Vertex s1 = new Vertex( p1, i + 1 );
			final Vertex s2 = new Vertex( p2, i + 2 );

			final Vector3D n0 = p0.multiply( 1.0 / radius );
			final Vector3D n1 = p1.multiply( 1.0 / radius );
			final Vector3D n2 = p2.multiply( 1.0 / radius );

			vertexNormals.add( n0 );
			vertexNormals.add( n1 );
			vertexNormals.add( n2 );

			s0.setNormal( n0 );
			s1.setNormal( n1 );
			s2.setNormal( n2 );

			addSubdividedFace( vertexCoordinates, vertexNormals, v0, s0, s2, radius, subdivisions - 1, material );
			addSubdividedFace( vertexCoordinates, vertexNormals, v1, s1, s0, radius, subdivisions - 1, material );
			addSubdividedFace( vertexCoordinates, vertexNormals, v2, s2, s1, radius, subdivisions - 1, material );
			addSubdividedFace( vertexCoordinates, vertexNormals, s0, s1, s2, radius, subdivisions - 1, material );
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