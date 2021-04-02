/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
import Matrix3D from './Matrix3D';
import Vector3D from './Vector3D';
import MathTools from './MathTools';

/**
 * This class represents rectangular 3D bounds (specified by two vectors).
 *
 * @author Peter S. Heijnen
 */
export default class Bounds3D
{
	/**
	 * First vector component of box. Normally the minimum vector.
	 */
	v1: Vector3D;

	/**
	 * Second vector component of box. Normally the maximum vector.
	 */
	v2: Vector3D;

	/**
	 * Empty box defined by zero-vectors.
	 */
	static EMPTY = new Bounds3D( Vector3D.ZERO, Vector3D.ZERO );

	/**
	 * Create a new box.
	 *
	 * @param [v1] First vector of box.
	 * @param [v2] Second vector of box.
	 */
	constructor( v1?: Vector3D, v2?: Vector3D )
	{
		if ( ( v1 !== undefined ) && !( v1 instanceof Vector3D ) )
		{
			throw new TypeError();
		}

		if ( ( v2 !== undefined ) && !( v2 instanceof Vector3D ) )
		{
			throw new TypeError();
		}

		this.v1 = v1 || Vector3D.ZERO;
		this.v2 = v2 || Vector3D.ZERO;
	}

	/**
	 * Get center point of these bounds.
	 *
	 * @return Vector describing boundary center (average of coordinates).
	 */
	center(): Vector3D
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return v1.set( ( v1.x + v2.x ) / 2, ( v1.y + v2.y ) / 2, ( v1.z + v2.z ) / 2 );
	}

	/**
	 * Get center X.
	 *
	 * @return Center X (avaerage of X coordinate of vector 1 and vector 2).
	 */
	centerX(): number
	{
		return 0.5 * ( this.v1.x + this.v2.x );
	}

	/**
	 * Get center Y.
	 *
	 * @return Center Y (avaerage of Y coordinate of vector 1 and vector 2).
	 */
	centerY(): number
	{
		return 0.5 * ( this.v1.y + this.v2.y );
	}

	/**
	 * Get center Z.
	 *
	 * @return Center Z (avaerage of Z coordinate of vector 1 and vector 2).
	 */
	centerZ(): number
	{
		return 0.5 * ( this.v1.z + this.v2.z );
	}

	/**
	 * Get delta X.
	 *
	 * @return Delta X (X coordinate of vector 1 substracted from vector 2).
	 */
	deltaX(): number
	{
		return this.v2.x - this.v1.x;
	}

	/**
	 * Get delta Y.
	 *
	 * @return Delta Y (Y coordinate of vector 1 substracted from vector 2).
	 */
	deltaY(): number
	{
		return this.v2.y - this.v1.y;
	}

	/**
	 * Get delta Z.
	 *
	 * @return Delta Z (Z coordinate of vector 1 substracted from vector 2).
	 */
	deltaZ(): number
	{
		return this.v2.z - this.v1.z;
	}

	/**
	 * Get volume contained within these bounds.
	 *
	 * @return Volume contained within these bounds.
	 */
	volume(): number
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return Math.abs( ( v2.x - v1.x ) * ( v2.y - v1.y ) * ( v2.z - v1.z ) );
	}

	/**
	 * Test whether these bounds are empty. This returns true if these bounds
	 * describe a zero-volume, meaning that any of the coordinates is equal for
	 * both vectors.
	 *
	 * @return <code>true</code> if the bounds are empty (zero volume);
	 *         <code>false</code> if the bounds are not empty (non-zero volume).
	 */
	isEmpty(): boolean
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return ( ( v1.x === v2.x ) || ( v1.y === v2.y ) || ( v1.z === v2.z ) );
	}

	/**
	 * Test whether these bounds are sorted. This means that each coordinate of
	 * the first vector is lesser or equal to the same coordinate of the second
	 * vector.
	 *
	 * @return <code>true</code> if the bounds are sorted;
	 *         <code>false</code> if the bounds are not sorted.
	 */
	isSorted(): boolean
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return ( ( v1.x <= v2.x ) && ( v1.y <= v2.y ) && ( v1.z <= v2.z ) );
	}

	/**
	 * Test if this bounds contains the specified point.
	 *
	 * @param point Point to test.
	 *
	 * @return <code>true</code> if this bounds contains the specified point;
	 *         <code>false</code> if the point is outside these bounds.
	 */
	contains( point: Vector3D ): boolean
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return ( point.x >= Math.min( v1.x, v2.x ) ) && ( point.x <= Math.max( v1.x, v2.x ) ) &&
		       ( point.y >= Math.min( v1.y, v2.y ) ) && ( point.y <= Math.max( v1.y, v2.y ) ) &&
		       ( point.z >= Math.min( v1.z, v2.z ) ) && ( point.z <= Math.max( v1.z, v2.z ) );
	}

	/**
	 * Compare these bounds to the specified bounds.
	 *
	 * @param other Bounds to compare with.
	 *
	 * @return <code>true</code> if the bounds are equal, <code>false</code> if not.
	 */
	equals( other: Bounds3D ): boolean
	{
		return ( this === other ) || ( other && this.v1.equals( other.v1 ) && this.v2.equals( other.v2 ) );
	}

	/**
	 * Calculate intersection between two bounding boxes. Note that the result
	 * will have one or more negative factors for v2 - v1 when the bounding
	 * boxes are disjunct.
	 *
	 * @param bounds1 First object for intersection.
	 * @param bounds2 Seconds object for intersection.
	 *
	 * @return Bounds of intersection.
	 */
	static intersect( bounds1: Bounds3D, bounds2: Bounds3D ): Bounds3D
	{
		return Bounds3D.rebuild( bounds1, bounds2,
				bounds1.v1.set(
						Math.max( Math.min( bounds1.v1.x, bounds1.v2.x ), Math.min( bounds2.v1.x, bounds2.v2.x ) ),
						Math.max( Math.min( bounds1.v1.y, bounds1.v2.y ), Math.min( bounds2.v1.y, bounds2.v2.y ) ),
						Math.max( Math.min( bounds1.v1.z, bounds1.v2.z ), Math.min( bounds2.v1.z, bounds2.v2.z ) ) ),
				bounds1.v2.set(
						Math.min( Math.max( bounds1.v1.x, bounds1.v2.x ), Math.max( bounds2.v1.x, bounds2.v2.x ) ),
						Math.min( Math.max( bounds1.v1.y, bounds1.v2.y ), Math.max( bounds2.v1.y, bounds2.v2.y ) ),
						Math.min( Math.max( bounds1.v1.z, bounds1.v2.z ), Math.max( bounds2.v1.z, bounds2.v2.z ) ) ) );
	}

	/**
	 * Determine whether the two specified bounding boxes intersect. This method
	 * does not return <code>true</code> if the intersection along any axis is
	 * less than the specified <code>epsilon</code> value.
	 *
	 * @param bounds1 First object for intersection test.
	 * @param bounds2 Seconds object for intersection test.
	 * @param [epsilon] Tolerance (always a positive number).
	 *
	 * @return <code>true</code> if the bounds intersect;
	 *         <code>false</code> if the bounds are disjunct.
	 */
	static intersects( bounds1: Bounds3D, bounds2: Bounds3D, epsilon: number = 0 ): boolean
	{
		let result;

		if ( !epsilon )
		{
			result = ( Math.min( bounds1.v1.x, bounds1.v2.x ) < Math.max( bounds2.v1.x, bounds2.v2.x ) ) &&
					 ( Math.min( bounds2.v1.x, bounds2.v2.x ) < Math.max( bounds1.v1.x, bounds1.v2.x ) ) &&
					 ( Math.min( bounds1.v1.y, bounds1.v2.y ) < Math.max( bounds2.v1.y, bounds2.v2.y ) ) &&
					 ( Math.min( bounds2.v1.y, bounds2.v2.y ) < Math.max( bounds1.v1.y, bounds1.v2.y ) ) &&
					 ( Math.min( bounds1.v1.z, bounds1.v2.z ) < Math.max( bounds2.v1.z, bounds2.v2.z ) ) &&
					 ( Math.min( bounds2.v1.z, bounds2.v2.z ) < Math.max( bounds1.v1.z, bounds1.v2.z ) );
		}
		else
		{
			result = MathTools.significantlyLessThan( Math.min( bounds1.v1.x, bounds1.v2.x ), Math.max( bounds2.v1.x, bounds2.v2.x ), epsilon ) &&
					 MathTools.significantlyLessThan( Math.min( bounds2.v1.x, bounds2.v2.x ), Math.max( bounds1.v1.x, bounds1.v2.x ), epsilon ) &&
					 MathTools.significantlyLessThan( Math.min( bounds1.v1.y, bounds1.v2.y ), Math.max( bounds2.v1.y, bounds2.v2.y ), epsilon ) &&
					 MathTools.significantlyLessThan( Math.min( bounds2.v1.y, bounds2.v2.y ), Math.max( bounds1.v1.y, bounds1.v2.y ), epsilon ) &&
					 MathTools.significantlyLessThan( Math.min( bounds1.v1.z, bounds1.v2.z ), Math.max( bounds2.v1.z, bounds2.v2.z ), epsilon ) &&
					 MathTools.significantlyLessThan( Math.min( bounds2.v1.z, bounds2.v2.z ), Math.max( bounds1.v1.z, bounds1.v2.z ), epsilon );
		}

		return result;
	}

	/**
	 * Calculate joined bounds of this and the given bounds or vector.
	 *
	 * @param other Bounds or vector to join with.
	 *
	 * @return Joined bounds.
	 */
	join( other: Bounds3D | Vector3D ): Bounds3D
	{
		let result;

		if ( other instanceof Bounds3D )
		{
			result = Bounds3D.rebuild( this, other,
					this.v1.set(
							Math.min( Math.min( this.v1.x, this.v2.x ), Math.min( other.v1.x, other.v2.x ) ),
							Math.min( Math.min( this.v1.y, this.v2.y ), Math.min( other.v1.y, other.v2.y ) ),
							Math.min( Math.min( this.v1.z, this.v2.z ), Math.min( other.v1.z, other.v2.z ) )
					),
					this.v2.set(
							Math.max( Math.max( this.v1.x, this.v2.x ), Math.max( other.v1.x, other.v2.x ) ),
							Math.max( Math.max( this.v1.y, this.v2.y ), Math.max( other.v1.y, other.v2.y ) ),
							Math.max( Math.max( this.v1.z, this.v2.z ), Math.max( other.v1.z, other.v2.z ) )
					)
			);
		}
		else
		{
			result = this.set(
					other.set(
							Math.min( other.x, Math.min( this.v1.x, this.v2.x ) ),
							Math.min( other.y, Math.min( this.v1.y, this.v2.y ) ),
							Math.min( other.z, Math.min( this.v1.z, this.v2.z ) )
					),
					other.set(
							Math.max( other.x, Math.max( this.v1.x, this.v2.x ) ),
							Math.max( other.y, Math.max( this.v1.y, this.v2.y ) ),
							Math.max( other.z, Math.max( this.v1.z, this.v2.z ) )
					)
			);
		}

		return result;
	}

	/**
	 * Determine maximum vector of box.
	 *
	 * @return Resulting vector.
	 */
	max(): Vector3D
	{
		let x = Math.max( this.v1.x, this.v2.x );
		let y = Math.max( this.v1.y, this.v2.y );
		let z = Math.max( this.v1.z, this.v2.z );
		return this.v2.set( x, y, z );
	}

	/**
	 * Determine maximum X coordinate of bounds.
	 *
	 * @return Maximum X coordinate of bounds.
	 */
	maxX(): number
	{
		return Math.max( this.v1.x, this.v2.x );
	}

	/**
	 * Determine maximum Y coordinate of bounds.
	 *
	 * @return Maximum Y coordinate of bounds.
	 */
	maxY(): number
	{
		return Math.max( this.v1.y, this.v2.y );
	}

	/**
	 * Determine maximum Z coordinate of bounds.
	 *
	 * @return Maximum Z coordinate of bounds.
	 */
	maxZ(): number
	{
		return Math.max( this.v1.z, this.v2.z );
	}

	/**
	 * Determine minimum vector of bounds.
	 *
	 * @return Resulting vector.
	 */
	min(): Vector3D
	{
		let x = Math.min( this.v1.x, this.v2.x );
		let y = Math.min( this.v1.y, this.v2.y );
		let z = Math.min( this.v1.z, this.v2.z );
		return this.v1.set( x, y, z );
	}

	/**
	 * Determine minimum X coordinate of bounds.
	 *
	 * @return Minimum X coordinate of bounds.
	 */
	minX(): number
	{
		return Math.min( this.v1.x, this.v2.x );
	}

	/**
	 * Determine minimum Y coordinate of bounds.
	 *
	 * @return Minimum Y coordinate of bounds.
	 */
	minY(): number
	{
		return Math.min( this.v1.y, this.v2.y );
	}

	/**
	 * Determine minimum Z coordinate of bounds.
	 *
	 * @return Minimum Z coordinate of bounds.
	 */
	minZ(): number
	{
		return Math.min( this.v1.z, this.v2.z );
	}

	/**
	 * Subtract vector from bounds.
	 *
	 * @param vector  Vector to subtract from bounds.
	 *
	 * @return Resulting bounds.
	 */
	minus( vector: Vector3D ): Bounds3D
	{
		return this.set( this.v1.minus( vector ), this.v2.minus( vector ) );
	}

	/**
	 * Determine box after scalar multiplication.
	 *
	 * @param factor Scale multiplication factor.
	 *
	 * @return Resulting box.
	 */
	multiply( factor: number ): Bounds3D
	{
		return this.set( this.v1.multiply( factor ), this.v2.multiply( factor ) );
	}

	/**
	 * Add a vector to bounds.
	 *
	 * @param vector Vector to add to bounds.
	 *
	 * @return Resulting bounds.
	 */
	plus( vector: Vector3D ): Bounds3D
	{
		return this.set( this.v1.plus( vector ), this.v2.plus( vector ) );
	}

	/**
	 * Construct new box from the specified coordinates, and try to reuse
	 * existing boxes.
	 *
	 * @param box1 Reusable box object.
	 * @param box2 Reusable box object.
	 * @param v1 First vector of bounds to set.
	 * @param v2 Second vector of bounds to set.
	 *
	 * @return Bounds3D object based on the desired coordinates.
	 */
	static rebuild( box1: Bounds3D, box2: Bounds3D, v1: Vector3D, v2: Vector3D ): Bounds3D
	{
		let result;

		/*
		 * Try to reuse the existing vectors. If not possible, create
		 * new ones.
		 */
		if ( box1.v1.equals( v1 ) )
		{
			v1 = box1.v1;
		}
		else if ( box1.v2.equals( v1 ) )
		{
			v1 = box1.v2;
		}
		else if ( box2.v1.equals( v1 ) )
		{
			v1 = box2.v1;
		}
		else if ( box2.v2.equals( v1 ) )
		{
			v1 = box2.v2;
		}

		if ( box1.v1.equals( v2 ) )
		{
			v2 = box1.v1;
		}
		else if ( box1.v2.equals( v2 ) )
		{
			v2 = box1.v2;
		}
		else if ( box2.v1.equals( v2 ) )
		{
			v2 = box2.v1;
		}
		else if ( box2.v2.equals( v2 ) )
		{
			v2 = box2.v2;
		}
		else if ( v1.equals( v2 ) )
		{
			v2 = v1;
		}

		/*
		 * Try to reuse the existing boxes. If not possible, create
		 * a new one.
		 */
		if ( ( box1.v1 === v1 ) && ( box1.v2 === v2 ) )
		{
			result = box1;
		}
		else if ( ( box2.v1 === v1 ) && ( box2.v2 === v2 ) )
		{
			result = box2;
		}
		else
		{
			result = new Bounds3D( v1, v2 );
		}

		return result;
	}

	/**
	 * Set bounds to the specified vectors.
	 *
	 * @param [newV1] First vector of bounds to set.
	 * @param [newV2] Second vector of bounds to set.
	 *
	 * @return Resulting bounds.
	 */
	set( newV1?: Vector3D, newV2?: Vector3D ): Bounds3D
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return ( !newV1 || newV1.equals( v1 ) ) &&
			   ( !newV2 || newV2.equals( v2 ) ) ? this : new Bounds3D( newV1 || v1, newV2 || v2 );
	}

	/**
	 * Get size of these bounds.
	 *
	 * @return Vector describing bound size (v2-v1).
	 */
	size(): Vector3D
	{
		let v1 = this.v1;
		let v2 = this.v2;
		return v2.set( Math.abs( v2.x - v1.x ), Math.abs( v2.y - v1.y ), Math.abs( v2.z - v1.z ) );
	}

	/**
	 * Get size along X axis.
	 *
	 * @return Size along X (distance between X coordinates of vector 1 and 2).
	 */
	sizeX(): number
	{
		return Math.abs( this.v2.x - this.v1.x );
	}

	/**
	 * Get size along Y axis.
	 *
	 * @return Size along Y (distance between Y coordinates of vector 1 and 2).
	 */
	sizeY(): number
	{
		return Math.abs( this.v2.y - this.v1.y );
	}

	/**
	 * Get size along Z axis.
	 *
	 * @return Size along Z (distance between Z coordinates of vector 1 and 2).
	 */
	sizeZ(): number
	{
		return Math.abs( this.v2.z - this.v1.z );
	}

	/**
	 * Determine sorted bounds. If bounds are sorted, than the x/y/z
	 * components of {@link #v1} are always less or equal to the
	 * matching components of {@link #v2}.
	 *
	 * @return Resulting bounds.
	 */
	sorted(): Bounds3D
	{
		return this.set( this.min(), this.max() );
	}

	/**
	 * Converts the bounds from an oriented bounding box (OBB) to an (world)
	 * axis-aligned bounding box (AABB).
	 *
	 * @param box2world Transforms box to world coordinates.
	 *
	 * @return Axis-aligned bounding box.
	 */
	convertObbToAabb( box2world: Matrix3D ): Bounds3D
	{
		const x1 = this.v1.x;
		const y1 = this.v1.y;
		const z1 = this.v1.z;
		const x2 = this.v2.x;
		const y2 = this.v2.y;
		const z2 = this.v2.z;

		let tx = box2world.transformX( x1, y1, z1 );
		let ty = box2world.transformY( x1, y1, z1 );
		let tz = box2world.transformZ( x1, y1, z1 );
		let minX = tx;
		let minY = ty;
		let minZ = tz;
		let maxX = tx;
		let maxY = ty;
		let maxZ = tz;

		tx = box2world.transformX( x1, y1, z2 );
		ty = box2world.transformY( x1, y1, z2 );
		tz = box2world.transformZ( x1, y1, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x1, y2, z1 );
		ty = box2world.transformY( x1, y2, z1 );
		tz = box2world.transformZ( x1, y2, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x1, y2, z2 );
		ty = box2world.transformY( x1, y2, z2 );
		tz = box2world.transformZ( x1, y2, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y1, z1 );
		ty = box2world.transformY( x2, y1, z1 );
		tz = box2world.transformZ( x2, y1, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y1, z2 );
		ty = box2world.transformY( x2, y1, z2 );
		tz = box2world.transformZ( x2, y1, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y2, z1 );
		ty = box2world.transformY( x2, y2, z1 );
		tz = box2world.transformZ( x2, y2, z1 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		tx = box2world.transformX( x2, y2, z2 );
		ty = box2world.transformY( x2, y2, z2 );
		tz = box2world.transformZ( x2, y2, z2 );
		minX = Math.min( minX, tx );
		minY = Math.min( minY, ty );
		minZ = Math.min( minZ, tz );
		maxX = Math.max( maxX, tx );
		maxY = Math.max( maxY, ty );
		maxZ = Math.max( maxZ, tz );

		return new Bounds3D( new Vector3D( minX, minY, minZ ), new Vector3D( maxX, maxY, maxZ ) );
	}
}
