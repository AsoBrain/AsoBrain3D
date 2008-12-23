/*   ColDet - C++ 3D Collision Detection Library
 *   Copyright (C) 2000 Amir Geva
 *
 *   ColDet - 3D Collision Detection Library for Java
 *   Copyright (C) 2008 Numdata BV
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package ab.j3d.coldet;

/**
 * Collision Model.  Will represent the mesh to be tested for collisions.  It
 * has to be notified of all triangles, via addTriangle() After all triangles
 * are added, a call to finish() will process the information and prepare for
 * collision tests. Call collision() to check for a collision
 * <p/>
 * Note: Transformations must not contain scaling.
 *
 * @author  Amir Geva (original C++ version)
 * @author  Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public abstract class CollisionModel3D
{
	/**
	 * Create a new collision model object.
	 *
	 * @param   isStatic    Indicates that the model does not move a lot, and
	 *                      certain calculations can be done every time its
	 *                      transform changes instead of every collision test.
	 * @param   triangles   Optimization for construction speed. If you know
	 *                      the number of triangles. Set to -1 if unknown.
	 */
	public static CollisionModel3D newCollisionModel3D( final boolean isStatic , final int triangles )
	{
		return new CollisionModel3DImpl( isStatic , triangles );
	}

	/**
	 * Use any of the forms of this functions to enter the coordinates of the
	 * model's triangles.
	 */
	public abstract void addTriangle( float x1, float y1, float z1,
	                                  float x2, float y2, float z2,
	                                  float x3, float y3, float z3 );

	public abstract void addTriangle( float[/*3*/] v1 , float[/*3*/] v2, float[/*3*/] v3 );

	/**
	 * All triangles have been added, process model.
	 */
	public abstract void finish();

	/**
	 * The the current affine matrix for the model. See transform.txt for format
	 * information
	 */
	public abstract void setTransform( float[/*16*/] m );

	/**
	 * Check for collision with another model. Do not mix model types here.
	 * <p/>
	 * other_transform allows overriding the other model's transform, by supplying
	 * an alternative one. This can be useful when testing a model against itself
	 * with different orientations.
	 */
	public abstract boolean collision( CollisionModel3D other, Matrix3D otherTransform/*=0*/ );

	/**
	 * Retrieve the pair of triangles [t1,t2] that collided. Only valid after a
	 * call to collision() that returned true. t1 is this model's triangle and t2
	 * is the other one. In case of ray or sphere collision, only t1 will be valid.
	 * The coordinates will be in _this_ model's coordinate space, unless
	 * ModelSpace is false, in which case, coordinates will be transformed by the
	 * model's current transform to world space.
	 */
	public abstract Triangle[/*2*/] getCollidingTriangles( boolean modelSpace/*=true*/ );

	/**
	 * Retrieve the pair [t1,t2] of triangles indices that collided. Only valid
	 * after a call to collision() that returned true. t1 belongs to _this_ model,
	 * while t2 is in the other one.
	 */
	public abstract int[] getCollidingTriangles();

}