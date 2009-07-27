/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2009 Peter S. Heijnen
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

import java.util.Arrays;

import ab.j3d.Material;
import ab.j3d.Vector3D;
import ab.j3d.geom.UVMap;

/**
 * This class defines a 3D box.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Box3D
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
	 * Constructs a new box with the specified dimensions and the given material
	 * on all sides, mapped according to the given UV map. At least one of the
	 * box's dimensions must be non-zero.
	 *
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   uvMap           UV mapping to use.
	 * @param   material        Material applied to all sides of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final UVMap uvMap , final Material material  )
	{
		this( dx , dy , dz , uvMap , material , material , material , material , material , material );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given
	 * materials on their respective sides, mapped according to the given UV
	 * map. At least one of the box's dimensions must be non-zero.
	 *
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   uvMap           UV mapping to use.
	 * @param   frontMaterial   Material applied to the front of the box.
	 * @param   rearMaterial    Material applied to the rear of the box.
	 * @param   rightMaterial   Material applied to the right of the box.
	 * @param   leftMaterial    Material applied to the left of the box.
	 * @param   topMaterial     Material applied to the top of the box.
	 * @param   bottomMaterial  Material applied to the bottom of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final UVMap uvMap , final Material frontMaterial , final Material rearMaterial , final Material rightMaterial , final Material leftMaterial , final Material topMaterial , final Material bottomMaterial )
	{
		this( dx , dy , dz , frontMaterial , uvMap , rearMaterial , uvMap , rightMaterial , uvMap , leftMaterial , uvMap , topMaterial , uvMap , bottomMaterial , uvMap );
	}

	/**
	 * Constructs a new box with the specified dimensions and the given
	 * materials on their respective sides, mapped according to the given UV
	 * map. At least one of the box's dimensions must be non-zero.
	 *
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   frontMaterial   Material applied to the front of the box.
	 * @param   frontMap        UV-mapping to use for the front of the box.
	 * @param   rearMaterial    Material applied to the rear of the box.
	 * @param   rearMap         UV-mapping to use for the rear of the box.
	 * @param   rightMaterial   Material applied to the right of the box.
	 * @param   rightMap        UV-mapping to use for the right of the box.
	 * @param   leftMaterial    Material applied to the left of the box.
	 * @param   leftMap         UV-mapping to use for the left of the box.
	 * @param   topMaterial     Material applied to the top of the box.
	 * @param   topMap          UV-mapping to use for the top of the box.
	 * @param   bottomMaterial  Material applied to the bottom of the box.
	 * @param   bottomMap       UV-mapping to use for the bottom of the box.
	 *
	 * @throws  IllegalArgumentException if <code>dx</code>, <code>dy</code> and
	 *          <code>dz</code> are all zero.
	 */
	public Box3D( final double dx , final double dy , final double dz , final Material frontMaterial , final UVMap frontMap , final Material rearMaterial , final UVMap rearMap , final Material rightMaterial , final UVMap rightMap , final Material leftMaterial , final UVMap leftMap , final Material topMaterial , final UVMap topMap , final Material bottomMaterial , final UVMap bottomMap )
	{
		final double size = Vector3D.length( dx, dy, dz );
		if ( size <= 0.0 )
			throw new IllegalArgumentException( dx + " x " + dy + " x " + dz );

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
			new Vector3D( 0.0 , 0.0 , 0.0 ) ,    // 0        7------6
			new Vector3D(  dx , 0.0 , 0.0 ) ,    // 1       /:     /|
			new Vector3D(  dx ,  dy , 0.0 ) ,    // 2      4------5 |
			new Vector3D( 0.0 ,  dy , 0.0 ) ,    // 3      | :    | |
			new Vector3D( 0.0 , 0.0 ,  dz ) ,    // 4      | :    | |
			new Vector3D(  dx , 0.0 ,  dz ) ,    // 5      | 3....|.2
			new Vector3D(  dx ,  dy ,  dz ) ,    // 6      |.     |/
			new Vector3D( 0.0 ,  dy ,  dz ) ) ); // 7      0------1

		setVertexNormals( new double[]
		{
			/* 0 */ -nx , -ny , -nz ,
			/* 1 */  nx , -ny , -nz ,
			/* 2 */  nx ,  ny , -nz ,
			/* 3 */ -nx ,  ny , -nz ,
			/* 4 */ -nx , -ny ,  nz ,
			/* 5 */  nx , -ny ,  nz ,
			/* 6 */  nx ,  ny ,  nz ,
			/* 7 */ -nx ,  ny ,  nz
		} );

		addFace( FRONT_VERTICES  , frontMaterial  , frontMap  );
		addFace( REAR_VERTICES   , rearMaterial   , rearMap   );
		addFace( RIGHT_VERTICES  , rightMaterial  , rightMap  );
		addFace( LEFT_VERTICES   , leftMaterial   , leftMap   );
		addFace( TOP_VERTICES    , topMaterial    , topMap    );
		addFace( BOTTOM_VERTICES , bottomMaterial , bottomMap );
	}

	/**
	 * Helper method to add a texture mapped face.
	 *
	 * @param   vertices        Vertices that define the face.
	 * @param   material        Material to use for the face.
	 * @param   uvMap           UV map to use.
	 */
	private void addFace( final int[] vertices , final Material material , final UVMap uvMap )
	{
		addFace( new Face3D( this , vertices , material , ( uvMap != null ) ? uvMap.generate( material , _vertexCoordinates , vertices , false ) : null , null , false , false ) );
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

}
