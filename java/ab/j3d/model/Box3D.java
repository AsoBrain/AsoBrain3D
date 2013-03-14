/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;

/**
 * This class defines a 3D box.
 *
 * @author Peter S. Heijnen
 */
public class Box3D
extends Object3D
{
	/**
	 * Vertices for front face.
	 */
	private static final int[] FACE_0451 = { 0, 4, 5, 1 };

	/**
	 * Vertices for rear face.
	 */
	private static final int[] FACE_3267 = { 3, 2, 6, 7 };

	/**
	 * Vertices for right face.
	 */
	private static final int[] FACE_1562 = { 1, 5, 6, 2 };

	/**
	 * Vertices for left face.
	 */
	private static final int[] FACE_0374 = { 0, 3, 7, 4 };

	/**
	 * Vertices for top face.
	 */
	private static final int[] FACE_7654 = { 7, 6, 5, 4 };

	/**
	 * Vertices for bottom face.
	 */
	private static final int[] FACE_0123 = { 0, 1, 2, 3 };

	/**
	 * Vertices for top face when box is flat (dz=0).
	 */
	private static final int[] FACE_3210 = { 3, 2, 1, 0 };

	/**
	 * Tessellation that is shared by all faces.
	 */
	private static final Tessellation TESSELLATION = new Tessellation( Collections.singletonList( new int[] { 3, 2, 1, 0, 3 } ), Collections.<TessellationPrimitive>singletonList( new QuadList( new int[] { 3, 2, 1, 0 } ) ) );

	/**
	 * Width of box (x-axis).
	 */
	private final double _dx;

	/**
	 * Height of box (y-axis).
	 */
	private final double _dy;

	/**
	 * Depth of box (z-axis).
	 */
	private final double _dz;

	/**
	 * Constructs a new box with the specified dimensions and the given appearance
	 * on all sides, mapped according to the given UV map. At least one of the
	 * box's dimensions must be non-zero.
	 *
	 * @param dx         Width of box (x-axis).
	 * @param dy         Height of box (y-axis).
	 * @param dz         Depth of box (z-axis).
	 * @param uvMap      UV mapping to use.
	 * @param appearance Appearance applied to all sides of the box.
	 *
	 * @throws IllegalArgumentException if {@code dx}, {@code dy} and {@code dz}
	 * are all zero.
	 */
	public Box3D( final double dx, final double dy, final double dz, final UVMap uvMap, final Appearance appearance )
	{
		this( dx, dy, dz, uvMap, appearance, appearance, appearance, appearance, appearance, appearance );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given appearances
	 * on their respective sides, mapped according to the given UV map. At least
	 * one of the box's dimensions must be non-zero.
	 *
	 * @param dx               Width of box (x-axis).
	 * @param dy               Height of box (y-axis).
	 * @param dz               Depth of box (z-axis).
	 * @param uvMap            UV mapping to use.
	 * @param frontAppearance  Appearance applied to the front of the box.
	 * @param rearAppearance   Appearance applied to the rear of the box.
	 * @param rightAppearance  Appearance applied to the right of the box.
	 * @param leftAppearance   Appearance applied to the left of the box.
	 * @param topAppearance    Appearance applied to the top of the box.
	 * @param bottomAppearance Appearance applied to the bottom of the box.
	 *
	 * @throws IllegalArgumentException if {@code dx}, {@code dy} and {@code dz}
	 * are all zero.
	 */
	public Box3D( final double dx, final double dy, final double dz, final UVMap uvMap, final Appearance frontAppearance, final Appearance rearAppearance, final Appearance rightAppearance, final Appearance leftAppearance, final Appearance topAppearance, final Appearance bottomAppearance )
	{
		this( dx, dy, dz, frontAppearance, uvMap, rearAppearance, uvMap, rightAppearance, uvMap, leftAppearance, uvMap, topAppearance, uvMap, bottomAppearance, uvMap );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given appearances
	 * on their respective sides, mapped according to the given UV map. At least
	 * one of the box's dimensions must be non-zero.
	 *
	 * @param dx               Width of box (x-axis).
	 * @param dy               Height of box (y-axis).
	 * @param dz               Depth of box (z-axis).
	 * @param frontAppearance  Appearance applied to the front of the box.
	 * @param frontMap         UV-mapping to use for the front of the box.
	 * @param rearAppearance   Appearance applied to the rear of the box.
	 * @param rearMap          UV-mapping to use for the rear of the box.
	 * @param rightAppearance  Appearance applied to the right of the box.
	 * @param rightMap         UV-mapping to use for the right of the box.
	 * @param leftAppearance   Appearance applied to the left of the box.
	 * @param leftMap          UV-mapping to use for the left of the box.
	 * @param topAppearance    Appearance applied to the top of the box.
	 * @param topMap           UV-mapping to use for the top of the box.
	 * @param bottomAppearance Appearance applied to the bottom of the box.
	 * @param bottomMap        UV-mapping to use for the bottom of the box.
	 *
	 * @throws IllegalArgumentException if {@code dx}, {@code dy} and {@code dz}
	 * are all zero.
	 */
	public Box3D( final double dx, final double dy, final double dz, final Appearance frontAppearance, final UVMap frontMap, final Appearance rearAppearance, final UVMap rearMap, final Appearance rightAppearance, final UVMap rightMap, final Appearance leftAppearance, final UVMap leftMap, final Appearance topAppearance, final UVMap topMap, final Appearance bottomAppearance, final UVMap bottomMap )
	{
		final boolean zeroDx = ( dx == 0.0 );
		final boolean zeroDy = ( dy == 0.0 );
		final boolean zeroDz = ( dz == 0.0 );

		if ( ( ( dx < 0.0 ) || ( dy < 0.0 ) || ( dz < 0.0 ) ) || ( zeroDx && zeroDy ) || ( zeroDx && zeroDz ) || ( zeroDy && zeroDz ) )
		{
			throw new IllegalArgumentException( "Invalid box dimensions: " + dx + " x " + dy + " x " + dz );
		}

		_dx = dx;
		_dy = dy;
		_dz = dz;

		if ( zeroDx )
		{
			/*
			 * Define mesh with following vertices and normals:
			 *      2
			 *     /|
			 *   /  |     right  = 3, 2, 1, 0  X+
			 *  3   |     left   = 0, 1, 2, 3  X-
			 *  |   1
			 *  |  /
			 *  |/
			 *  0
			 */
			setVertexCoordinates( Arrays.asList( Vector3D.ZERO,
			                                     new Vector3D( 0.0, dy, 0.0 ),
			                                     new Vector3D( 0.0, dy, dz ),
			                                     new Vector3D( 0.0, 0.0, dz ) ) );

			if ( ( leftAppearance == rightAppearance ) && ( leftMap == rightMap ) )
			{
				addFace( FACE_3210, Vector3D.POSITIVE_X_AXIS, rightAppearance, rightMap, true );
			}
			else
			{
				addFace( FACE_3210, Vector3D.POSITIVE_X_AXIS, rightAppearance, rightMap, false );
				addFace( FACE_0123, Vector3D.NEGATIVE_Y_AXIS, leftAppearance, leftMap, false );
			}
		}
		else if ( zeroDy )
		{
			/*
			 * Define mesh with following vertices and normals:
			 *  3-----2
			 *  |     |    front  = 0, 1, 2, 3  Y-
			 *  |     |    rear   = 3, 2, 1, 0  Y+
			 *  0-----1
			 */
			setVertexCoordinates( Arrays.asList( Vector3D.ZERO,
			                                     new Vector3D( dx, 0.0, 0.0 ),
			                                     new Vector3D( dx, 0.0, dz ),
			                                     new Vector3D( 0.0, 0.0, dz ) ) );

			if ( ( frontAppearance == rearAppearance ) && ( frontMap == rearMap ) )
			{
				addFace( FACE_3210, Vector3D.NEGATIVE_Y_AXIS, frontAppearance, frontMap, true );
			}
			else
			{
				addFace( FACE_3210, Vector3D.NEGATIVE_Y_AXIS, frontAppearance, frontMap, false );
				addFace( FACE_0123, Vector3D.POSITIVE_Y_AXIS, rearAppearance, rearMap, false );
			}
		}
		else if ( zeroDz )
		{
			/*
			 * Define mesh with following vertices and normals:
			 *    3------2
			 *   /      /      top    = 3, 2, 1, 0  Z+
			 *  0------1       bottom = 0, 1, 2, 3  Z-
			 */
			setVertexCoordinates( Arrays.asList( Vector3D.ZERO,
			                                     new Vector3D( dx, 0.0, 0.0 ),
			                                     new Vector3D( dx, dy, 0.0 ),
			                                     new Vector3D( 0.0, dy, 0.0 ) ) );

			if ( ( topAppearance == bottomAppearance ) && ( topMap == bottomMap ) )
			{
				addFace( FACE_3210, Vector3D.POSITIVE_Z_AXIS, topAppearance, topMap, true );
			}
			else
			{
				addFace( FACE_3210, Vector3D.POSITIVE_Z_AXIS, topAppearance, topMap, false );
				addFace( FACE_0123, Vector3D.NEGATIVE_Z_AXIS, bottomAppearance, bottomMap, false );
			}
		}
		else
		{
			/*
			 * Define mesh with following vertices and normals:
			 *    7------6
			 *   /:     /|     front  = 0, 4, 5, 1  Y-
			 *  4------5 |     rear   = 2, 6, 7, 3  Y+
			 *  | :    | |     right  = 1, 5, 6, 2  X+
			 *  | :    | |     left   = 3, 7, 4, 0  X-
			 *  | 3....|.2     top    = 4, 7, 6, 5  Z+
			 *  |.     |/      bottom = 0, 1, 2, 3  Z-
			 *  0------1
			 */
			setVertexCoordinates( Arrays.asList(
			Vector3D.ZERO,
			new Vector3D( dx, 0.0, 0.0 ),
			new Vector3D( dx, dy, 0.0 ),
			new Vector3D( 0.0, dy, 0.0 ),
			new Vector3D( 0.0, 0.0, dz ),
			new Vector3D( dx, 0.0, dz ),
			new Vector3D( dx, dy, dz ),
			new Vector3D( 0.0, dy, dz ) ) );

			addFace( FACE_0451, Vector3D.NEGATIVE_Y_AXIS, frontAppearance, frontMap, false );
			addFace( FACE_3267, Vector3D.POSITIVE_Y_AXIS, rearAppearance, rearMap, false );
			addFace( FACE_1562, Vector3D.POSITIVE_X_AXIS, rightAppearance, rightMap, false );
			addFace( FACE_0374, Vector3D.NEGATIVE_X_AXIS, leftAppearance, leftMap, false );
			addFace( FACE_7654, Vector3D.POSITIVE_Z_AXIS, topAppearance, topMap, false );
			addFace( FACE_0123, Vector3D.NEGATIVE_Z_AXIS, bottomAppearance, bottomMap, false );
		}
	}

	/**
	 * Helper method to add a texture mapped face.
	 *
	 * @param vertexIndices Mesh vertex index for each face vertex.
	 * @param normal        Face normal.
	 * @param appearance    Appearance to use for the face.
	 * @param uvMap         UV map to use.
	 * @param twoSided      Create two-sided face.
	 */
	private void addFace( final int[] vertexIndices, final Vector3D normal, final Appearance appearance, final UVMap uvMap, final boolean twoSided )
	{
		final List<Vector3D> vertexCoordinates = getVertexCoordinates();

		final double planeDistance = Vector3D.dot( vertexCoordinates.get( vertexIndices[ 0 ] ), normal );

		final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( appearance, uvMap, normal, false );
		final List<Vertex3D> vertices = new ArrayList<Vertex3D>( vertexIndices.length );
		for ( final int vertexIndex : vertexIndices )
		{
			final Vertex3D vertex = new Vertex3D( vertexCoordinates, vertexIndex );
			uvGenerator.generate( vertex.point );
			vertex.colorMapU = uvGenerator.getU();
			vertex.colorMapV = uvGenerator.getV();
			vertex.normal = normal;
			vertices.add( vertex );
		}

		final FaceGroup faceGroup = getFaceGroup( appearance, false, twoSided );
		faceGroup.addFace( new Face3D( normal, planeDistance, vertices, TESSELLATION ) );
	}

	/**
	 * Get width of box (x-axis).
	 *
	 * @return Width of box (x-axis).
	 */
	public double getDX()
	{
		return _dx;
	}

	/**
	 * Get height of box (y-axis).
	 *
	 * @return Height of box (y-axis).
	 */
	public double getDY()
	{
		return _dy;
	}

	/**
	 * Get depth of box (z-axis).
	 *
	 * @return Depth of box (z-axis).
	 */
	public double getDZ()
	{
		return _dz;
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		return new Bounds3D( Vector3D.ZERO, new Vector3D( _dx, _dy, _dz ) );
	}

	@Override
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		if ( other instanceof Box3D ) /* box vs. box */
		{
			final Bounds3D thisOrientedBoundingBox = getOrientedBoundingBox();
			final Bounds3D otherOrientedBoundingBox = other.getOrientedBoundingBox();

			result = ( ( thisOrientedBoundingBox != null ) && ( otherOrientedBoundingBox != null ) && GeometryTools.testOrientedBoundingBoxIntersection( thisOrientedBoundingBox, fromOtherToThis, otherOrientedBoundingBox ) );
		}
		else if ( other instanceof Sphere3D ) /* box vs. sphere */
		{
			final Sphere3D sphere = (Sphere3D)other;
			final double centerX = fromOtherToThis.transformX( 0.0, 0.0, 0.0 );
			final double centerY = fromOtherToThis.transformY( 0.0, 0.0, 0.0 );
			final double centerZ = fromOtherToThis.transformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereBoxIntersection( centerX, centerY, centerZ, sphere._radius, 0.0, 0.0, 0.0, getDX(), getDY(), getDZ() );
		}
		else
		{

			result = super.collidesWith( fromOtherToThis, other );
		}

		return result;
	}
}
