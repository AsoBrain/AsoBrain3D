/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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
package ab.j3d.coldet;

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Bounds3DBuilder;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.CollisionTester;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

/**
 * This object can be used to test for collisions between polygon soups.
 * <p />
 * Geometry is defined as a set of triangles and is stored in a bounding-box
 * tree that is dynamically split.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class CollisionObject
{
	/**
	 * Triangles at this node.
	 */
	private List<Triangle> _triangles;

	/**
	 * Bounding box of for this node that contains all triangles at this node.
	 */
	private Bounds3D _bounds;

	/**
	 * Point to split this node at.
	 * <p>
	 * The split point is determined by taking the average point of all faces.
	 * <p>
	 * This point is set during construction and later used by the
	 * {@link #split()} method to split this node. After the fist call to
	 * {@link #split()} method, this is set to <code>null</code>.
	 */
	private Vector3D _splitPoint;

	/**
	 * If this node was split, this contains the first sub-tree node.
	 */
	private CollisionObject _child1;

	/**
	 * If this node was split, this contains the second sub-tree node.
	 */
	private CollisionObject _child2;

	/**
	 * Create the root collision node for a 3D object.
	 *
	 * @param   object3d    Object3D to create collision model for.
	 */
	public CollisionObject( final Object3D object3d )
	{
		final double[] vertexCoordinates = object3d.getVertexCoordinates();
		final int      faceCount         = object3d.getFaceCount();

		int nrTriangles = 0;

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face = object3d.getFace( i );

			final int[] triangles = face.triangulate();
			if ( triangles != null )
			{
				nrTriangles += triangles.length / 3;
			}
		}

		final List<Triangle> triangles = new ArrayList<Triangle>( nrTriangles );
		final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();

		for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
		{
			final Face3D face = object3d.getFace( faceIndex );

			final int[] faceTriangles = face.triangulate();
			if ( faceTriangles != null )
			{
				final int[] vertexIndices = face.getVertexIndices();

				for ( final int vertexIndex : vertexIndices )
				{
					final int i = vertexIndex * 3;
					boundsBuilder.addPoint( vertexCoordinates[ i ] , vertexCoordinates[ i + 1 ] , vertexCoordinates[ i + 2 ] );
				}

				for ( int triangleIndex = 0 ; triangleIndex < faceTriangles.length ; triangleIndex += 3 )
				{
					final int vi1 = vertexIndices[ faceTriangles[ triangleIndex     ] ] * 3;
					final int vi2 = vertexIndices[ faceTriangles[ triangleIndex + 1 ] ] * 3;
					final int vi3 = vertexIndices[ faceTriangles[ triangleIndex + 2 ] ] * 3;

					final Vector3D p1 = Vector3D.INIT.set( vertexCoordinates[ vi1 ] , vertexCoordinates[ vi1 + 1 ] , vertexCoordinates[ vi1 + 2 ] );
					final Vector3D p2 = Vector3D.INIT.set( vertexCoordinates[ vi2 ] , vertexCoordinates[ vi2 + 1 ] , vertexCoordinates[ vi2 + 2 ] );
					final Vector3D p3 = Vector3D.INIT.set( vertexCoordinates[ vi3 ] , vertexCoordinates[ vi3 + 1 ] , vertexCoordinates[ vi3 + 2 ] );

					triangles.add( new Triangle( p1 , p2 , p3 ) );
				}
			}
		}

		_triangles = triangles;
		_bounds = boundsBuilder.getBounds();
		_splitPoint = ( nrTriangles > 1 ) ? boundsBuilder.getAveragePoint() : null;
		_child1 = null;
		_child2 = null;
	}

	/**
	 * Construct a new node.
	 *
	 * @param   triangles   Triangles assigned to this node.
	 */
	public CollisionObject( final List<Triangle> triangles )
	{
		final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();
		for ( final Triangle triangle : triangles )
		{
			boundsBuilder.addPoint( triangle.v1 );
			boundsBuilder.addPoint( triangle.v2 );
			boundsBuilder.addPoint( triangle.v3 );
		}

		_triangles = triangles;
		_bounds = boundsBuilder.getBounds();
		_splitPoint = ( triangles.size() > 1 ) ? boundsBuilder.getAveragePoint() : null;
		_child1 = null;
		_child2 = null;
	}

	/**
	 * Check for collision with another node.
	 *
	 * @param   other       Node to test collision with.
	 * @param   other2this  Transformation from other to this coordinate system.
	 *
	 * @return  <code>true</code> if a collision was found;
	 *          <code>false</code> if no collision was found.
	 */
	public boolean collidesWith( final CollisionObject other , final Matrix3D other2this )
	{
		return collidesWith( other , other2this , other2this.inverse() );
	}

	/**
	 * Check for collision with another node.
	 *
	 * @param   other       Node to test collision with.
	 * @param   other2this  Transformation from other to this coordinate system.
	 * @param   this2other  Transformation from this to other coordinate system.
	 *
	 * @return  <code>true</code> if a collision was found;
	 *          <code>false</code> if no collision was found.
	 */
	public boolean collidesWith( final CollisionObject other , final Matrix3D other2this , final Matrix3D this2other )
	{
		final boolean result;

		final Bounds3D bounds1 = _bounds;
		final Bounds3D bounds2 = other._bounds;

		if ( CollisionTester.testOrientedBoundingBox( bounds1 , other2this , bounds2 ) )
		{
			if ( !split() )
			{
				if ( !other.split() )
				{
					/*
					 * Make sure 'triangles1' has more elements than 'triangles2' to
					 * minimize the number of iterations in the outer loop.
					 */
					if ( _triangles.size() > other._triangles.size() )
					{
						result = testTriangleTriangleCollision( other, other2this );
					}
					else
					{
						result = other.testTriangleTriangleCollision( this , this2other );
					}
				}
				else
				{
					result = other._child1.collidesWith( this, this2other, other2this ) ||
					         other._child2.collidesWith( this, this2other, other2this );
				}
			}
			else if ( ( bounds1.volume() > bounds2.volume() ) || !other.split() )
			{
				result = _child1.collidesWith( other, other2this, this2other ) ||
				         _child2.collidesWith( other, other2this, this2other );
			}
			else
			{
				result = other._child1.collidesWith( this, this2other, other2this ) ||
				         other._child2.collidesWith( this, this2other, other2this );
			}
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Check for collision between triangles of this and another node.
	 *
	 * @param   other       Node to test collision with.
	 * @param   other2this  Transformation from other to this coordinate system.
	 *
	 * @return  <code>true</code> if a collision was found;
	 *          <code>false</code> if no collision was found.
	 */
	private boolean testTriangleTriangleCollision( final CollisionObject other , final Matrix3D other2this )
	{
		boolean result = false;

		for ( final Triangle triangle2 : other._triangles )
		{
			final Vector3D v1 = other2this.multiply( triangle2.v1 );
			final Vector3D v2 = other2this.multiply( triangle2.v2 );
			final Vector3D v3 = other2this.multiply( triangle2.v3 );

			for ( final Triangle triangle1 : _triangles )
			{
				if ( TriTriMoeler.testTriangleTriangle( v1 , v2 , v3 , triangle1.v1, triangle1.v2, triangle1.v3 ) )
				{
					result = true;
					break;
				}
			}

			if ( result )
			{
				break;
			}
		}

		return result;
	}

	/**
	 * Split this node if possible and if we have not tried to do so before.
	 *
	 * @return  <code>true</code> if this node was split;
	 *          <code>false</code> if this node is not split (leaf node).
	 */
	private boolean split()
	{
		final boolean result;

		final Vector3D splitPoint = _splitPoint;
		if ( splitPoint == null )
		{
			result = ( _child1 != null );
		}
		else
		{
			_splitPoint = null;

			final List<Triangle> triangles = _triangles;
			final int nrTriangles = triangles.size();

			int countX = 0;
			int countY = 0;
			int countZ = 0;

			for ( final Triangle triangle : triangles )
			{
				if ( triangle.center.x < splitPoint.x )
					countX++;

				if ( triangle.center.y < splitPoint.y )
					countY++;

				if ( triangle.center.z < splitPoint.z )
					countZ++;
			}

			/*
			 * Only split if we don't end up with empty branches.
			 */
			if ( ( ( countX > 0 ) && ( countX < nrTriangles ) ) ||
				 ( ( countY > 0 ) && ( countY < nrTriangles ) ) ||
				 ( ( countZ > 0 ) && ( countZ < nrTriangles ) ) )
			{
				final int balance  = nrTriangles / 2;
				final int balanceX = Math.abs( countX - balance );
				final int balanceY = Math.abs( countY - balance );
				final int balanceZ = Math.abs( countZ - balance );

				final List<Triangle> triangles1;
				final List<Triangle> triangles2;

				if ( ( balanceX <= balanceY ) && ( balanceX <= balanceZ ) ) /* X is most balanced */
				{
					triangles1 = new ArrayList<Triangle>( countX );
					triangles2 = new ArrayList<Triangle>( nrTriangles - countX );

					for ( final Triangle triangle : triangles )
					{
						if ( triangle.center.x < splitPoint.x )
						{
							triangles1.add( triangle );
						}
						else
						{
							triangles2.add( triangle );
						}
					}
				}
				else if ( balanceY <= balanceZ ) /* Y is most balanced */
				{
					triangles1 = new ArrayList<Triangle>( countY );
					triangles2 = new ArrayList<Triangle>( nrTriangles - countY );

					for ( final Triangle triangle : triangles )
					{
						if ( triangle.center.y < splitPoint.y )
						{
							triangles1.add( triangle );
						}
						else
						{
							triangles2.add( triangle );
						}
					}
				}
				else /* Z is most balanced */
				{
					triangles1 = new ArrayList<Triangle>( countZ );
					triangles2 = new ArrayList<Triangle>( nrTriangles - countZ );

					for ( final Triangle triangle : triangles )
					{
						if ( triangle.center.z < splitPoint.z )
						{
							triangles1.add( triangle );
						}
						else
						{
							triangles2.add( triangle );
						}
					}
				}

				_child1 = new CollisionObject( triangles1 );
				_child2 = new CollisionObject( triangles2 );
				result = true;
			}
			else
			{
				result = false;
			}
		}

		return result;
	}

	private static class Triangle
	{
		Vector3D v1;
		Vector3D v2;
		Vector3D v3;
		Vector3D center;

		Triangle( final Vector3D v1 , final Vector3D v2 , final Vector3D v3 )
		{
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			center = v1.set( v1.x + v2.x + v3.x / 3.0, v1.y + v2.y + v3.y / 3.0, v1.z + v2.z + v3.z / 3.0 );
		}

	}
}
