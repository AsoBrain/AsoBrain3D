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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link CollisionModel3D}.
 *
 * @author Amir Geva (original C++ version)
 * @author Peter S. Heijnen (Java Port)
 * @version 1.1
 */
class CollisionModel3DImpl
extends CollisionModel3D
{
	enum ColType
	{
		Models, Ray, Sphere
	}

	/**
	 * Create a new collision model object.
	 *
	 * @param isStatic  Indicates that the model does not move a lot, and certain
	 *                  calculations can be done every time its transform changes
	 *                  instead of every collision test.
	 * @param triangles Optimization for construction speed. If you know the number
	 *                  of triangles. Set to -1 if unknown.
	 */
	CollisionModel3DImpl( final boolean isStatic, final int triangles )
	{
		_triangles = new ArrayList<BoxedTriangleNode>(
		( triangles < 0 ) ? 10 : triangles );
		_root = new BoxTreeInnerNode( Vector3D.Zero, Vector3D.Zero );
		_transform = Matrix3D.Identity;
		_invTransform = Matrix3D.Identity;
		_colTri1 = null;
		_colTri2 = null;
		_iColTri1 = -1;
		_iColTri2 = -1;
		_final = false;
		_static = isStatic;
	}

	public void addTriangle( final float x1, final float y1, final float z1,
	                         final float x2, final float y2, final float z2,
	                         final float x3, final float y3, final float z3 )
	{
		addTriangle( new Vector3D( x1, y1, z1 ),
		             new Vector3D( x2, y2, z2 ),
		             new Vector3D( x3, y3, z3 ) );
	}

	public void addTriangle( final float[] v1, final float[] v2, final float[] v3 )
	{
		addTriangle( new Vector3D( v1[ 0 ], v1[ 1 ], v1[ 2 ] ),
		             new Vector3D( v2[ 0 ], v2[ 1 ], v2[ 2 ] ),
		             new Vector3D( v3[ 0 ], v3[ 1 ], v3[ 2 ] ) );
	}

	public void addTriangle( final Vector3D v1, final Vector3D v2, final Vector3D v3 )
	{
		if ( _final )
			throw new IllegalStateException();

		_triangles.add( new BoxedTriangleNode( v1, v2, v3 ) );
	}

	public void finish()
	{
		if ( _final )
			throw new IllegalStateException();

		// Prepare initial triangle list
		_final = true;

		for ( final BoxedTriangleNode triangle : _triangles )
		{
			_root._boxes.add( triangle );
		}

		_root.divide();
	}

	public void setTransform( final float[/*16*/] m )
	{
		setTransform( new Matrix3D( m[ 0 ], m[ 1 ], m[ 2 ], m[ 3 ], m[ 4 ], m[ 5 ], m[ 6 ], m[ 7 ], m[ 8 ], m[ 9 ], m[ 10 ], m[ 11 ], m[ 12 ], m[ 13 ], m[ 14 ], m[ 15 ] ) );
	}

	public void setTransform( final Matrix3D m )
	{
		_transform = m;
		if ( _static ) _invTransform = _transform.inverse();
	}

	public boolean collision( final CollisionModel3D other, final Matrix3D otherTransform )
	{
		if ( !_final )
			throw new IllegalStateException();

		final CollisionModel3DImpl o = (CollisionModel3DImpl)( other );
		if ( !o._final )
			throw new IllegalStateException();

		Matrix3D transform = ( otherTransform == null ) ? o._transform : otherTransform;
		if ( _static )
		{
			transform = transform.multiply( _invTransform );
		}
		else
		{
			transform = transform.multiply( _transform.inverse() );
		}

		final Vector3D[] rsN = new Vector3D[]
			{
				new Vector3D( transform.get( 0 , 0 ), transform.get( 0 , 1 ), transform.get( 0 , 2 ) ) ,
				new Vector3D( transform.get( 1 , 0 ), transform.get( 1 , 1 ), transform.get( 1 , 2 ) ) ,
				new Vector3D( transform.get( 2 , 0 ), transform.get( 2 , 1 ), transform.get( 2 , 2 ) )
			};

		final int num = Math.max( _triangles.size(), o._triangles.size() );
		int allocated = Math.max( 64, ( num >> 4 ) );
		final List<Check> checks = new ArrayList<Check>( allocated );
		while ( checks.size() < allocated )
		{
			checks.add( new Check( null, null ) );
		}

		int queue_idx = 1;

		Check c = checks.get( 0 );
		c._first = _root;
		c._second = o._root;

		while ( queue_idx > 0 )
		{
			if ( queue_idx > ( allocated / 2 ) ) // enlarge the queue.
			{
				allocated *= 2;
				while ( checks.size() < allocated )
				{
					checks.add( new Check( null, null ) );
				}
			}

			// @@@ add depth check
			//Check c=checks.back();
			c = checks.get( --queue_idx );
			final BoxTreeNode first = c._first;
			final BoxTreeNode second = c._second;

			if ( first.intersect( second, rsN , transform ) )
			{
				final int tnum1 = first.getTrianglesNumber();
				final int tnum2 = second.getTrianglesNumber();
				if ( tnum1 > 0 && tnum2 > 0 )
				{
					{
						for ( int i = 0; i < tnum2; i++ )
						{
							final BoxedTriangleNode bt2 = second.getTriangle( i );

							final Triangle tt = new Triangle( bt2.getTriangle().v1.transform( transform ), bt2
							.getTriangle().v2.transform( transform ), bt2.getTriangle().v3.transform( transform ) );

							for ( int j = 0; j < tnum1; j++ )
							{
								final BoxedTriangleNode bt1 = first.getTriangle( j );
								if ( tt.intersect( bt1.getTriangle() ) )
								{
									_colTri1 = bt1;
									_iColTri1 = getTriangleIndex( bt1 );
									_colTri2 = bt2;
									_iColTri2 = o.getTriangleIndex( bt2 );
									return true;
								}
							}
						}
					}
				}
				else if ( first.getSonsNumber() == 0 )
				{
					final BoxTreeNode s1 = second.getSon( 0 );
					final BoxTreeNode s2 = second.getSon( 1 );

					final Check c1 = checks.get( queue_idx++ );
					c1._first = first;
					c1._second = s1;

					final Check c2 = checks.get( queue_idx++ );
					c2._first = first;
					c2._second = s2;
				}
				else if ( second.getSonsNumber() == 0 )
				{
					final BoxTreeNode f1 = first.getSon( 0 );
					final BoxTreeNode f2 = first.getSon( 1 );

					final Check c1 = checks.get( queue_idx++ );
					c1._first = f1;
					c1._second = second;

					final Check c2 = checks.get( queue_idx++ );
					c2._first = f2;
					c2._second = second;
				}
				else
				{
					final float v1 = first.getVolume();
					final float v2 = second.getVolume();
					if ( v1 > v2 )
					{
						final BoxTreeNode f1 = first.getSon( 0 );
						final BoxTreeNode f2 = first.getSon( 1 );
						assert ( f1 != null );
						assert ( f2 != null );

						final Check c1 = checks.get( queue_idx++ );
						c1._first = f1;
						c1._second = second;

						final Check c2 = checks.get( queue_idx++ );
						c2._first = f2;
						c2._second = second;
					}
					else
					{
						final BoxTreeNode s1 = second.getSon( 0 );
						final BoxTreeNode s2 = second.getSon( 1 );
						assert ( s1 != null );
						assert ( s2 != null );

						final Check c1 = checks.get( queue_idx++ );
						c1._first = first;
						c1._second = s1;

						final Check c2 = checks.get( queue_idx++ );
						c2._first = first;
						c2._second = s2;
					}
				}
			}
		}
		return false;
	}


	public Triangle[] getCollidingTriangles( final boolean modelSpace )
	{
		final Triangle[] result = new Triangle[2];

		final Triangle triangle1 = _colTri1.getTriangle();
		final Triangle triangle2 = _colTri2.getTriangle();

		if ( modelSpace )
		{
			result[ 0 ] = triangle1;
			result[ 1 ] = triangle2;
		}
		else
		{
			final Matrix3D t = _transform;
			result[ 0 ] = new Triangle( triangle1.v1.transform( t ), triangle1.v2.transform( t ), triangle1.v3.transform( t ) );
			result[ 1 ] = new Triangle( triangle2.v1.transform( t ), triangle2.v2.transform( t ), triangle2.v3.transform( t ) );
		}

		return result;
	}


	public int[] getCollidingTriangles()
	{
		return new int[] { _iColTri1 , _iColTri2 };
	}

	public int getTriangleIndex( final BoxedTriangleNode bt )
	{
		return _triangles.indexOf( bt );
	}

	/**
	 * Stores all the actual triangles.  Other objects will use pointers into this
	 * array.
	 */
	private List<BoxedTriangleNode> _triangles;

	/**
	 * Root of the hierarchy tree.
	 */
	private BoxTreeInnerNode _root;

	/**
	 * The current transform.
	 */
	private Matrix3D _transform;

	/**
	 * The current inverse transform.
	 */
	private Matrix3D _invTransform;

	/**
	 * The first triangle that last collided.
	 */
	private BoxedTriangleNode _colTri1;

	/**
	 * The second triangle that last collided.
	 */
	private BoxedTriangleNode _colTri2;

	/**
	 * The index of the first triangle that last collided.
	 */
	private int _iColTri1;

	/**
	 * The index of the second triangle that last collided.
	 */
	private int _iColTri2;

	/**
	 * Flag for indicating the model is finished.
	 */
	private boolean _final;

	/**
	 * Static models will maintain the same transform for a while so the inverse
	 * transform is calculated each set instead of in the collision test.
	 */
	private boolean _static;

	private static class Check
	{
		Check( final BoxTreeNode f, final BoxTreeNode s )
		{
			_first = f;
			_second = s;
		}

		private BoxTreeNode _first;

		private BoxTreeNode _second;
	}
}

