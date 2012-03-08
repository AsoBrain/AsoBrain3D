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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;

/**
 * This class defines a 3D box.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Box3D
	extends Object3D
{
	/** Vertices for front face.  */ private static final int[] FRONT_VERTICES  = { 0 , 4 , 5 , 1 };
	/** Vertices for rear face.   */ private static final int[] REAR_VERTICES   = { 2 , 6 , 7 , 3 };
	/** Vertices for right face.  */ private static final int[] RIGHT_VERTICES  = { 1 , 5 , 6 , 2 };
	/** Vertices for left face.   */ private static final int[] LEFT_VERTICES   = { 3 , 7 , 4 , 0 };
	/** Vertices for top face.    */ private static final int[] TOP_VERTICES    = { 4 , 7 , 6 , 5 };
	/** Vertices for bottom face. */ private static final int[] BOTTOM_VERTICES = { 1 , 2 , 3 , 0 };

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
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   uvMap           UV mapping to use.
	 * @param   appearance        Appearance applied to all sides of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final UVMap uvMap , final Appearance appearance  )
	{
		this( dx , dy , dz , uvMap , appearance , appearance , appearance , appearance , appearance , appearance );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given
	 * appearances on their respective sides, mapped according to the given UV
	 * map. At least one of the box's dimensions must be non-zero.
	 *
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   uvMap           UV mapping to use.
	 * @param   frontAppearance   Appearance applied to the front of the box.
	 * @param   rearAppearance    Appearance applied to the rear of the box.
	 * @param   rightAppearance   Appearance applied to the right of the box.
	 * @param   leftAppearance    Appearance applied to the left of the box.
	 * @param   topAppearance     Appearance applied to the top of the box.
	 * @param   bottomAppearance  Appearance applied to the bottom of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final UVMap uvMap , final Appearance frontAppearance , final Appearance rearAppearance , final Appearance rightAppearance , final Appearance leftAppearance , final Appearance topAppearance , final Appearance bottomAppearance )
	{
		this( dx , dy , dz , frontAppearance , uvMap , rearAppearance , uvMap , rightAppearance , uvMap , leftAppearance , uvMap , topAppearance , uvMap , bottomAppearance , uvMap );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given
	 * appearances on their respective sides, mapped according to the given UV
	 * map. At least one of the box's dimensions must be non-zero.
	 *
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   frontAppearance   Appearance applied to the front of the box.
	 * @param   frontMap        UV-mapping to use for the front of the box.
	 * @param   rearAppearance    Appearance applied to the rear of the box.
	 * @param   rearMap         UV-mapping to use for the rear of the box.
	 * @param   rightAppearance   Appearance applied to the right of the box.
	 * @param   rightMap        UV-mapping to use for the right of the box.
	 * @param   leftAppearance    Appearance applied to the left of the box.
	 * @param   leftMap         UV-mapping to use for the left of the box.
	 * @param   topAppearance     Appearance applied to the top of the box.
	 * @param   topMap          UV-mapping to use for the top of the box.
	 * @param   bottomAppearance  Appearance applied to the bottom of the box.
	 * @param   bottomMap       UV-mapping to use for the bottom of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final Appearance frontAppearance , final UVMap frontMap , final Appearance rearAppearance , final UVMap rearMap , final Appearance rightAppearance , final UVMap rightMap , final Appearance leftAppearance , final UVMap leftMap , final Appearance topAppearance , final UVMap topMap , final Appearance bottomAppearance , final UVMap bottomMap )
	{
		final double size = Vector3D.length( dx, dy, dz );
		if ( size <= 0.0 )
		{
			throw new IllegalArgumentException( dx + " x " + dy + " x " + dz );
		}

		_dx = dx;
		_dy = dy;
		_dz = dz;

		final double nx = dx / size;
		final double ny = dy / size;
		final double nz = dz / size;

		/*
		 * Create vertex coordinates and normals.
		 */
		setVertexCoordinates( Arrays.asList(
			Vector3D.ZERO ,    // 0        7------6
			new Vector3D(  dx , 0.0 , 0.0 ) ,    // 1       /:     /|
			new Vector3D(  dx ,  dy , 0.0 ) ,    // 2      4------5 |
			new Vector3D( 0.0 ,  dy , 0.0 ) ,    // 3      | :    | |
			new Vector3D( 0.0 , 0.0 ,  dz ) ,    // 4      | :    | |
			new Vector3D(  dx , 0.0 ,  dz ) ,    // 5      | 3....|.2
			new Vector3D(  dx ,  dy ,  dz ) ,    // 6      |.     |/
			new Vector3D( 0.0 ,  dy ,  dz ) ) ); // 7      0------1

		// TODO: Why does a cube have normals that make it appear spherical?
		final Vector3D[] vertexNormals =
		{
			/* 0 */ new Vector3D( -nx , -ny , -nz ) ,
			/* 1 */ new Vector3D(  nx , -ny , -nz ) ,
			/* 2 */ new Vector3D(  nx ,  ny , -nz ) ,
			/* 3 */ new Vector3D( -nx ,  ny , -nz ) ,
			/* 4 */ new Vector3D( -nx , -ny ,  nz ) ,
			/* 5 */ new Vector3D(  nx , -ny ,  nz ) ,
			/* 6 */ new Vector3D(  nx ,  ny ,  nz ) ,
			/* 7 */ new Vector3D( -nx ,  ny ,  nz )
		};

		addFace( FRONT_VERTICES  , vertexNormals , frontAppearance  , frontMap  );
		addFace( REAR_VERTICES   , vertexNormals , rearAppearance   , rearMap   );
		addFace( RIGHT_VERTICES  , vertexNormals , rightAppearance  , rightMap  );
		addFace( LEFT_VERTICES   , vertexNormals , leftAppearance   , leftMap   );
		addFace( TOP_VERTICES    , vertexNormals , topAppearance    , topMap    );
		addFace( BOTTOM_VERTICES , vertexNormals , bottomAppearance , bottomMap );
	}

	/**
	 * Helper method to add a texture mapped face.
	 *
	 * @param   vertexCoordinates   Vertices that define the face.
	 * @param   normals             Normals by vertex coordinate index.
	 * @param   appearance          Appearance to use for the face.
	 * @param   uvMap               UV map to use.
	 */
	private void addFace( final int[] vertexCoordinates , final Vector3D[] normals , final Appearance appearance , final UVMap uvMap )
	{
		final TextureMap colorMap = ( appearance == null ) ? null : appearance.getColorMap();
		final float[] textureCoordinates = ( uvMap != null ) ? uvMap.generate( colorMap, getVertexCoordinates(), vertexCoordinates, false ) : null;

		final List<Face3D.Vertex> vertices = Face3D.createVertices( this, vertexCoordinates, textureCoordinates, null );
		for ( final Face3D.Vertex vertex : vertices )
		{
			vertex.normal = normals[ vertex.vertexCoordinateIndex ];
		}

		final FaceGroup faceGroup = getFaceGroup( appearance, false, false );
		faceGroup.addFace( new Face3D( vertices, null ) );
	}

	/**
	 * Get width of box (x-axis).
	 *
	 * @return  Width of box (x-axis).
	 */
	public double getDX()
	{
		return _dx;
	}

	/**
	 * Get height of box (y-axis).
	 *
	 * @return  Height of box (y-axis).
	 */
	public double getDY()
	{
		return _dy;
	}

	/**
	 * Get depth of box (z-axis).
	 *
	 * @return  Depth of box (z-axis).
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
			final Bounds3D thisOrientedBoundingBox  = getOrientedBoundingBox();
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
