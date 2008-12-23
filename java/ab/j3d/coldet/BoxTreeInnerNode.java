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

import java.util.List;

/**
 * Inner node, containing other nodes.
 *
 * @author Amir Geva (original C++ version)
 * @author Peter S. Heijnen (Java Port)
 * @version 1.1
 */
public class BoxTreeInnerNode
	extends BoxTreeNode
{
	BoxTreeInnerNode( final Vector3D pos, final Vector3D size )
	{
		super( pos, size );
		_first = null;
		_second = null;
	}

	/**
	 * Create the sons that will divide this box
	 */
	int createSons( final Vector3D divisionPoint )
	{
		final Vector3D oldPosition = getPosition();
		final Vector3D oldSize     = getSize();

		float distX = 0.0f;
		float distY = 0.0f;
		float distZ = 0.0f;

		for ( final BoxedTriangleNode bt : _boxes )
		{
			final Vector3D triangleCenter = bt.getTriangle().center;
			distX += Math.abs( triangleCenter.x - divisionPoint.x );
			distY += Math.abs( triangleCenter.y - divisionPoint.y );
			distZ += Math.abs( triangleCenter.z - divisionPoint.z );
		}

		final int      result;
		final Vector3D splitPos;
		final Vector3D subSize1;
		final Vector3D subSize2;

		if ( ( distX >= distZ ) && ( distX >= distY ) ) /* X is longest */
		{
			final float divisionSize = divisionPoint.x - oldPosition.x;

			result   = 0;
			splitPos = new Vector3D( divisionPoint.x , oldPosition.y , oldPosition.z );
			subSize1 = new Vector3D( divisionSize , oldSize.y, oldSize.z );
			subSize2 = new Vector3D( oldSize.x - divisionSize , oldSize.y, oldSize.z );
		}
		else if ( ( distZ <= distX ) || ( distZ <= distY ) ) /* Y is longest */
		{
			final float divisionSize = divisionPoint.y - oldPosition.y;

			result   = 1;
			splitPos = new Vector3D( oldPosition.x , divisionPoint.y , oldPosition.z );
			subSize1 = new Vector3D( oldSize.x , divisionSize , oldSize.z );
			subSize2 = new Vector3D( oldSize.x , oldSize.y - divisionSize , oldSize.z );
		}
		else /* Z is longest */
		{
			final float divisionSize = divisionPoint.z - oldPosition.z;

			result   = 1;
			splitPos = new Vector3D( oldPosition.x , oldPosition.y , divisionPoint.z );
			subSize1 = new Vector3D( oldSize.x , oldSize.y , divisionSize );
			subSize2 = new Vector3D( oldSize.x , oldSize.y , oldSize.z - divisionSize );

		}

		_first  = new BoxTreeInnerNode( oldPosition , subSize1 );
		_second = new BoxTreeInnerNode( splitPos , subSize2 );

		return result;
	}

	public static int getLongest( final float f1 , final float f2 , final float f3 )
	{
		return ( f1 >= f3 ) && ( f1 >= f2 ) ? 0
		       : ( f3 <= f1 ) || ( f3 <= f2 ) ? 1 : 2;
	}

	/**
	 * Recalculate the bounds of this box to fully contain all of its triangles
	 */
	Vector3D recalcBounds()
	{
		final Vector3D result;

		if ( !_boxes.isEmpty() )
		{
			float avgX = 0.0f;
			float avgY = 0.0f;
			float avgZ = 0.0f;
			float minX = 9e9f;
			float minY = 9e9f;
			float minZ = 9e9f;
			float maxX = -9e9f;
			float maxY = -9e9f;
			float maxZ = -9e9f;


			for ( final BoxedTriangleNode bt : _boxes )
			{
				final Triangle triangle = bt.getTriangle();
				final Vector3D v1 = triangle.v1;
				final Vector3D v2 = triangle.v2;
				final Vector3D v3 = triangle.v3;

				avgX += triangle.center.x;
				avgY += triangle.center.y;
				avgZ += triangle.center.z;
				minX = Math.min( Math.min( Math.min( v1.x, v2.x ), v3.x ), minX );
				minY = Math.min( Math.min( Math.min( v1.y, v2.y ), v3.y ), minY );
				minZ = Math.min( Math.min( Math.min( v1.z, v2.z ), v3.z ), minZ );
				maxX = Math.max( Math.max( Math.max( v1.x, v2.x ), v3.x ), maxX );
				maxY = Math.max( Math.max( Math.max( v1.y, v2.y ), v3.y ), maxY );
				maxZ = Math.max( Math.max( Math.max( v1.z, v2.z ), v3.z ), maxZ );
			}

			if ( minX == maxX )
			{
				minX -= 0.001f;
				maxX += 0.001f;
			}
			if ( minY == maxY )
			{
				minY -= 0.001f;
				maxY += 0.001f;
			}
			if ( minZ == maxZ )
			{
				minZ -= 0.001f;
				maxZ += 0.001f;
			}


			_pos = new Vector3D( minX, minY, minZ );
			_size = new Vector3D( maxX - minX, maxY - minY, maxZ - minZ );
			_center = new Vector3D( 0.5f * ( minX + maxX ), 0.5f * ( minY + maxY ), 0.5f * ( minZ + maxZ ) );

			final float averageFactor = 1.0f / (float)_boxes.size();
			result = new Vector3D( avgX * averageFactor, avgY * averageFactor, avgZ * averageFactor );
		}
		else
		{
			result = null;
		}
		return result;
	}


	/**
	 * Recursively divide this box
	 */
	int divide()
	{
		if ( _boxes.isEmpty() ) return 0;
		final Vector3D center = recalcBounds();

		final int longest = createSons( center );
		final BoxTreeInnerNode f = (BoxTreeInnerNode)( _first );
		final BoxTreeInnerNode s = (BoxTreeInnerNode)( _second );
		int depth = 1;
		final int bnum = _boxes.size();
		for ( int i = 0; i < bnum; i++ )
		{
			final BoxedTriangleNode bt = _boxes.get( i );
			if ( bt.getTriangle().center.get( longest ) < center.get( longest ) )
			{
				f._boxes.add( bt );
			}
			else
			{
				s._boxes.add( bt );
			}
		}

		final int b1num = f._boxes.size();
		final int b2num = s._boxes.size();
		if ( ( b1num == bnum || b2num == bnum ) )// && p_depth>m_logdepth)
		{
			_first = null;
			_second = null;
			return depth + 1;
		}

		_boxes.clear();
		if ( f._boxes.isEmpty() )
		{
			_first = null;
		}
		else if ( f._boxes.size() == 1 )
		{
			final BoxedTriangleNode bt = f._boxes.get( f._boxes.size() - 1 );
			_first = bt;
		}
		else depth = f.divide();
		if ( s._boxes.isEmpty() )
		{
			_second = null;
		}
		else if ( s._boxes.size() == 1 )
		{
			final BoxedTriangleNode bt = s._boxes.get( s._boxes.size() - 1 );
			_second = bt;
		}
		else
		{
			depth = Math.max( depth, s.divide() );
		}
		return depth + 1;
	}

	public int getTrianglesNumber()
	{
		return _boxes.size();
	}

	public BoxedTriangleNode getTriangle( final int which )
	{
		return ( which >= 0 && which < getTrianglesNumber() )
		       ? _boxes.get( which ) : null;
	}

	public int getSonsNumber()
	{
		return ( _first != null ) ? ( _second != null ) ? 2 : 1 : 0;
	}

	public BoxTreeNode getSon( final int which )
	{
		return ( which == 0 ) ? _first : ( which == 1 ) ? _second : null;
	}

	BoxTreeNode _first;

	BoxTreeNode _second;

	List<BoxedTriangleNode> _boxes;
}