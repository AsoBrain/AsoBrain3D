/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2009
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
package ab.j3d.geom;

import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Bounds3DBuilder;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This object can be used to test for collisions between polygon soups.
 * <p />
 * Geometry is defined as a set of triangles and is stored in a bounding-box
 * tree that is dynamically split.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class CollisionNode
{
	/**
	 * Triangles at this node.
	 */
	private List<Triangle3D> _triangles;

	/**
	 * Start offset in '_triangles'.
	 */
	private int _trianglesOffset;

	/**
	 * Number of triangles.
	 */
	private int _trianglesCount;

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
	private CollisionNode _child1;

	/**
	 * If this node was split, this contains the second sub-tree node.
	 */
	private CollisionNode _child2;

	/**
	 * Construct a new node.
	 *
	 * @param   triangles           Triangles.
	 * @param   trianglesOffset     Offset in <code>triangles</code>.
	 * @param   trianglesCount      Number of triangles at this node.
	 *
	 * @throws  IllegalArgumentException if <code>trianglesCount</code> is <code>0</code>.
	 * @throws  IndexOutOfBoundsException if a invalid range is specified.
	 */
	public CollisionNode( final List<Triangle3D> triangles , final int trianglesOffset , final int trianglesCount )
	{
		if ( trianglesOffset < 0 )
			throw new IndexOutOfBoundsException( String.valueOf( trianglesOffset ) );

		if ( trianglesCount <= 0 )
			throw new IllegalArgumentException( String.valueOf( trianglesCount ) );

		if ( trianglesOffset + trianglesCount > triangles.size() )
			throw new IndexOutOfBoundsException( String.valueOf( trianglesOffset + trianglesCount ) );

		final Bounds3DBuilder boundsBuilder = new Bounds3DBuilder();

		for ( int i = 0 ; i < trianglesCount ; i++ )
		{
			final Triangle3D triangle = triangles.get( trianglesOffset + i );
			boundsBuilder.addPoint( triangle.getP1() );
			boundsBuilder.addPoint( triangle.getP2() );
			boundsBuilder.addPoint( triangle.getP3() );
		}

		_triangles = triangles;
		_trianglesOffset = trianglesOffset;
		_trianglesCount = trianglesCount;
		_bounds = boundsBuilder.getBounds();
		_splitPoint = ( trianglesCount > 1 ) ? boundsBuilder.getAveragePoint() : null;
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
	public boolean collidesWith( final CollisionNode other , final Matrix3D other2this )
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
	 *
	 * @throws  NullPointerException if any parameter is <code>null</code>.
	 */
	public boolean collidesWith( final CollisionNode other , final Matrix3D other2this , final Matrix3D this2other )
	{
		if ( other == null )
			throw new NullPointerException( "other" );

		if ( other2this == null )
			throw new NullPointerException( "other2this" );

		if ( this2other == null )
			throw new NullPointerException( "this2other" );

		final boolean result;

		final Bounds3D bounds1 = _bounds;
		final Bounds3D bounds2 = other._bounds;

		if ( ( bounds1 != null ) && ( bounds2 != null ) && GeometryTools.testOrientedBoundingBoxIntersection( bounds1 , other2this , bounds2 ) )
		{
			if ( !split() )
			{
				if ( !other.split() )
				{
					/*
					 * Make sure 'triangles1' has more elements than 'triangles2' to
					 * minimize the number of iterations in the outer loop.
					 */
					if ( _trianglesCount > other._trianglesCount )
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
					result = other._child1.collidesWith( this , this2other , other2this ) ||
					         other._child2.collidesWith( this , this2other , other2this );
				}
			}
			else if ( ( bounds1.volume() > bounds2.volume() ) || !other.split() )
			{
				result = _child1.collidesWith( other , other2this , this2other ) ||
				         _child2.collidesWith( other , other2this , this2other );
			}
			else
			{
				result = other._child1.collidesWith( this , this2other , other2this ) ||
				         other._child2.collidesWith( this , this2other , other2this );
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
	private boolean testTriangleTriangleCollision( final CollisionNode other , final Matrix3D other2this )
	{
		boolean result = false;

		final List<Triangle3D> thisTriangles = _triangles;
		final int thisStart = _trianglesOffset;
		final int thisEnd = thisStart + _trianglesCount;

		final List<Triangle3D> otherTriangles = other._triangles;
		final int otherStart = other._trianglesOffset;
		final int otherEnd = otherStart + other._trianglesCount;

		for ( int otherIndex = otherStart ; otherIndex < otherEnd ; otherIndex++ )
		{
			final Triangle3D otherTriangle = otherTriangles.get( otherIndex );

			final Vector3D otherP1 = other2this.transform( otherTriangle.getP1() );
			final Vector3D otherP2 = other2this.transform( otherTriangle.getP2() );
			final Vector3D otherP3 = other2this.transform( otherTriangle.getP3() );

			for ( int thisIndex = thisStart ; thisIndex < thisEnd ; thisIndex++ )
			{
				final Triangle3D thisTriangle = thisTriangles.get( thisIndex );

				if ( GeometryTools.testTriangleTriangleIntersection( otherP1 , otherP2 , otherP3 , thisTriangle.getP1() , thisTriangle.getP2() , thisTriangle.getP3() ) )
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

			final List<Triangle3D> triangles = _triangles;
			final int trianglesOffset = _trianglesOffset;
			final int trianglesCount = _trianglesCount;

			int countX = 0;
			int countY = 0;
			int countZ = 0;

			for ( int i = 0 ; i < trianglesCount ; i++ )
			{
				final Triangle3D triangle = triangles.get( trianglesOffset + i );

				final Vector3D p = triangle.getAveragePoint();

				if ( p.x < splitPoint.x )
				{
					countX++;
				}

				if ( p.y < splitPoint.y )
				{
					countY++;
				}

				if ( p.z < splitPoint.z )
				{
					countZ++;
				}
			}

			/*
			 * Only split if we don't end up with empty branches.
			 */
			if ( ( ( countX > 0 ) && ( countX < trianglesCount ) ) ||
				 ( ( countY > 0 ) && ( countY < trianglesCount ) ) ||
				 ( ( countZ > 0 ) && ( countZ < trianglesCount ) ) )
			{
				final int halfSize = trianglesCount / 2;
				final int balanceX = Math.abs( countX - halfSize );
				final int balanceY = Math.abs( countY - halfSize );
				final int balanceZ = Math.abs( countZ - halfSize );

				final int countLeft;

				if ( ( balanceX <= balanceY ) && ( balanceX <= balanceZ ) ) /* X is most balanced */
				{
					splitTrianglesOnX( splitPoint.x );
					countLeft = countX;
				}
				else if ( balanceY <= balanceZ ) /* Y is most balanced */
				{
					splitTrianglesOnY( splitPoint.y );
					countLeft = countY;
				}
				else /* Z is most balanced */
				{
					splitTrianglesOnZ( splitPoint.z );
					countLeft = countZ;
				}

				_child1 = new CollisionNode( triangles , trianglesOffset , countLeft );
				_child2 = new CollisionNode( triangles , trianglesOffset + countLeft , trianglesCount - countLeft );
				result = true;
			}
			else
			{
				result = false;
			}
		}

		return result;
	}

	/**
	 * Used by {@link #split()} to split the triangle list on X coordinate.
	 *
	 * @param   splitX  X coordinate to split on.
	 */
	private void splitTrianglesOnX( final double splitX )
	{
		final List<Triangle3D> triangles = _triangles;

		int leftIndex = _trianglesOffset;
		int rightIndex = leftIndex + _trianglesCount - 1;

		outerLoop: while ( leftIndex < rightIndex )
		{
			/*
			 * Find element on left side that should be on the right side.
			 */
			Triangle3D rightElement;
			while ( true )
			{
				rightElement = triangles.get( leftIndex );

				final boolean isLeft = ( rightElement.getAveragePoint().x < splitX );
				if ( !isLeft )
					break;

				if ( ++leftIndex >= rightIndex )
					break outerLoop;
			}

			/*
			 * Find element on right side that should be on the left side.
			 */
			Triangle3D leftElement;
			while ( true )
			{
				leftElement = triangles.get( rightIndex );

				final boolean isLeft = ( leftElement.getAveragePoint().x < splitX );
				if ( isLeft )
					break;

				if ( leftIndex >= --rightIndex )
					break outerLoop;
			}

			triangles.set( leftIndex++ , leftElement );
			triangles.set( rightIndex-- , rightElement );
		}
	}

	/**
	 * Used by {@link #split()} to split the triangle list on Y coordinate.
	 *
	 * @param   splitY  Y coordinate to split on.
	 */
	private void splitTrianglesOnY( final double splitY )
	{
		final List<Triangle3D> triangles = _triangles;

		int leftIndex = _trianglesOffset;
		int rightIndex = leftIndex + _trianglesCount - 1;

		outerLoop: while ( leftIndex < rightIndex )
		{
			/*
			 * Find element on left side that should be on the right side.
			 */
			Triangle3D rightElement;
			while ( true )
			{
				rightElement = triangles.get( leftIndex );

				final boolean isLeft = ( rightElement.getAveragePoint().y < splitY );
				if ( !isLeft )
					break;

				if ( ++leftIndex >= rightIndex )
					break outerLoop;
			}

			/*
			 * Find element on right side that should be on the left side.
			 */
			Triangle3D leftElement;
			while ( true )
			{
				leftElement = triangles.get( rightIndex );

				final boolean isLeft = ( leftElement.getAveragePoint().y < splitY );
				if ( isLeft )
					break;

				if ( leftIndex >= --rightIndex )
					break outerLoop;
			}

			triangles.set( leftIndex++ , leftElement );
			triangles.set( rightIndex-- , rightElement );
		}
	}

	/**
	 * Used by {@link #split()} to split the triangle list on Z coordinate.
	 *
	 * @param   splitZ  Z coordinate to split on.
	 */
	private void splitTrianglesOnZ( final double splitZ )
	{
		final List<Triangle3D> triangles = _triangles;

		int leftIndex = _trianglesOffset;
		int rightIndex = leftIndex + _trianglesCount - 1;

		outerLoop: while ( leftIndex < rightIndex )
		{
			/*
			 * Find element on left side that should be on the right side.
			 */
			Triangle3D rightElement;
			while ( true )
			{
				rightElement = triangles.get( leftIndex );

				final boolean isLeft = ( rightElement.getAveragePoint().z < splitZ );
				if ( !isLeft )
					break;

				if ( ++leftIndex >= rightIndex )
					break outerLoop;
			}

			/*
			 * Find element on right side that should be on the left side.
			 */
			Triangle3D leftElement;
			while ( true )
			{
				leftElement = triangles.get( rightIndex );

				final boolean isLeft = ( leftElement.getAveragePoint().z < splitZ );
				if ( isLeft )
					break;

				if ( leftIndex >= --rightIndex )
					break outerLoop;
			}

			triangles.set( leftIndex++ , leftElement );
			triangles.set( rightIndex-- , rightElement );
		}
	}
}