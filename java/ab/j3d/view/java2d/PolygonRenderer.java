/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2001-2005
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
package ab.j3d.view.java2d;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This class implements a simple renderer using filled polygons on a Graphics
 * (Java 1.1) context (no pixel buffers are used).
 *
 * @FIXME   This class is not production-quality, but it serves as a working starting-point for a proper Java 2D renderer.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class PolygonRenderer
{
	private final Matrix3D         _projectionTransform;
	private final Matrix3D         _viewTransform;
	private final boolean          _perspective;
	private final List             _queue;
	private final Node3DCollection _nodes;
	private       double[]         _pointCoordsCache;
	private       double[]         _normalsCache;

	static final class QueueItem
	{
		final double z;
		final int[]  xs;
		final int[]  ys;
		final Color  color;
		final double nz;

		QueueItem( final int[] xs , final int[] ys , final double z , final Color color , final double nz )
		{
			this.z     = z;
			this.xs    = xs;
			this.ys    = ys;
			this.color = color;
			this.nz    = nz;
		}
	}

	public PolygonRenderer( final Matrix3D gTransform , final Matrix3D viewTransform , final boolean hasPerspective )
	{
		_projectionTransform      = gTransform;
		_viewTransform   = viewTransform;
		_perspective = hasPerspective;
		_queue       = new ArrayList();
		_nodes          = new Node3DCollection();
		_pointCoordsCache         = null;
	}

	public synchronized void add( final Node3D node , final Color color )
	{
		_nodes.clear();
		node.gatherLeafs( _nodes , Object3D.class , _viewTransform , false );

		for ( int nodeIndex = 0 ; nodeIndex < _nodes.size() ; nodeIndex++ )
		{
			final Object3D object      = (Object3D)_nodes.getNode( nodeIndex );
			final Matrix3D xform       = _nodes.getMatrix( nodeIndex );
			final int      pointCount  = object.getPointCount();
			final double[] pointCoords = ( _pointCoordsCache = xform.transform( object.getPointCoords() , _pointCoordsCache , pointCount ) );
			final int      faceCount   = object.getFaceCount();
			final double[] faceNormals = ( _normalsCache = xform.rotate( object.getFaceNormals() , _normalsCache , faceCount ) );

			if ( _perspective )
			{
				for ( int i = 0 , k = 0 ; i < pointCount ; i++ , k += 3 )
				{
					final double x = pointCoords[ k     ];
					final double y = pointCoords[ k + 1 ];
					final double z = pointCoords[ k + 2 ];

					if ( z > 1999.0 )
						return;

					final double f = 1000.0 / ( 2000.0 - z );

					pointCoords[ k     ] = x * f;
					pointCoords[ k + 1 ] = y * f;
				}
			}

			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face         = object.getFace( faceIndex );
				final int    vertexCount  = face.getVertexCount();
				final int[]  pointIndices = face.getPointIndices();

				/*
				 * If we have less than three points, its not a valid face.
				 */
				if ( vertexCount > 2 )
				{
					/*
					 * Perform backface removal
					 *
					 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
					 */
					final double x1 = pointCoords[ pointIndices[ 0 ] * 3     ];
					final double y1 = pointCoords[ pointIndices[ 0 ] * 3 + 1 ];

					final double x2 = pointCoords[ pointIndices[ 1 ] * 3     ];
					final double y2 = pointCoords[ pointIndices[ 1 ] * 3 + 1 ];

					final double x3 = pointCoords[ pointIndices[ 2 ] * 3     ];
					final double y3 = pointCoords[ pointIndices[ 2 ] * 3 + 1 ];

					if ( ( ( x1 - x2 ) * ( y3 - y2 ) - ( y1 - y2 ) * ( x3 - x2 ) ) < 0 )
						continue;

					/*
					 * Put entry in queue.
					 */
					final double normalZ = faceNormals[ faceIndex * 3 + 2 ];

					final int[] xs = new int[ vertexCount ];
					final int[] ys = new int[ vertexCount ];

					double averageZ = 0.0;

					for ( int p = 0 ; p < vertexCount ; p++ )
					{
						final int    vi = pointIndices[ p ] * 3;
						final double x  = pointCoords[ vi     ];
						final double y  = pointCoords[ vi + 1 ];
						final double z  = pointCoords[ vi + 2 ];

						averageZ += z;
						//if ( p == 0 || z < qz ) qz = z;

						xs[ p ] = (int)_projectionTransform.transformX( x , y , 0.0 );
						ys[ p ] = (int)_projectionTransform.transformY( x , y , 0.0 );
					}

					averageZ = averageZ / vertexCount;
					_queue.add( new QueueItem( xs , ys , averageZ , color , normalZ ) );
				}
			}
		}
	}

	public void paint( final Graphics g , final boolean solid )
	{
		/*
		 * Process queue and process its entries in sorted order.
		 */
		final QueueItem[] list = new QueueItem[ _queue.size() ];
		_queue.toArray( list );

		for ( int todo = list.length ; todo > 0 ; )
		{
			/*
			 * Remove 'entry' with lowest Z from 'list' (todo - 1).
			 */
			final QueueItem entry;

			int    cur = 0;
			double z   = list[ 0 ].z;

			for ( int i = todo ; --i >= 1 ; )
				if ( list[ i ].z < z ) z = ( list[ cur = i ] ).z;

			entry = list[ cur ];
			list[ cur ] = list[ --todo ];

			/*
			 * Paint entry
			 */
			if ( solid && _perspective )
			{
				final int c = entry.color.getRGB();
				final double nz = 0.1 + 1.0 * ( 1.0 + entry.nz );
				if ( nz < 0 )
					continue;

				g.setColor( new Color(
					Math.min( (int)(nz * ((c >> 16) & 255)) , 255 ) ,
					Math.min( (int)(nz * ((c >>  8) & 255)) , 255 ) ,
					Math.min( (int)(nz * ( c        & 255)) , 255 ) ) );
			}
			else
			{
				g.setColor( entry.color );
			}

			final int[] xs = entry.xs;
			final int[] ys = entry.ys;
			final int   l  = xs.length;

			if ( solid || l == 1 )
			{
				g.fillPolygon( xs , ys , l );
				//g.setColor( Color.black );
				g.setColor( new Color( 64 , 64 , 64 ) );
				//g.setColor( new Color( 128 , 128 , 128 ) );
			}

			for ( int i = 0 , j = l - 1 ; i < l ; j = i++ )
				g.drawLine( xs[ i ] , ys[ i ] , xs[ j ] , ys[ j ] );
		}
	}
}
