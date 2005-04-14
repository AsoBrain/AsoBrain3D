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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This class implements a simple renderer using Java 2D for rendering (no pixel
 * buffers/rendering is performed).
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
		final Face3D  face;
		final double  z;
		final int[]   xs;
		final int[]   ys;
		final boolean alternateAppearance;
		final double  nz;

		QueueItem( final Face3D face , final int[] xs , final int[] ys , final double z , final boolean alternateAppearance , final double nz )
		{
			this.face                = face;
			this.z                   = z;
			this.xs                  = xs;
			this.ys                  = ys;
			this.alternateAppearance = alternateAppearance;
			this.nz                  = nz;
		}
	}

	public PolygonRenderer( final Matrix3D gTransform , final Matrix3D viewTransform , final boolean hasPerspective )
	{
		_projectionTransform = gTransform;
		_viewTransform       = viewTransform;
		_perspective         = hasPerspective;
		_queue               = new ArrayList();
		_nodes               = new Node3DCollection();
		_pointCoordsCache    = null;
	}

	public void add( final Node3D node , final Color overridePaint )
	{
		final Node3DCollection nodes = _nodes;
		nodes.clear();
		node.gatherLeafs( nodes , Object3D.class , Matrix3D.INIT , false );

		for ( int nodeIndex = 0 ; nodeIndex < nodes.size() ; nodeIndex++ )
		{
			final Object3D object = (Object3D)nodes.getNode( nodeIndex );

			object.outlinePaint = overridePaint;
			object.fillPaint    = overridePaint;
		}

		add( node , false );
	}

	public void add( final Node3D node , final boolean alternateAppearance )
	{
		final Node3DCollection nodes = _nodes;
		nodes.clear();
		node.gatherLeafs( nodes , Object3D.class , _viewTransform , false );

		for ( int nodeIndex = 0 ; nodeIndex < nodes.size() ; nodeIndex++ )
		{
			final Object3D object      = (Object3D)nodes.getNode( nodeIndex );
			final Matrix3D xform       = nodes.getMatrix( nodeIndex );
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

					averageZ = averageZ / (double)vertexCount;
					_queue.add( new QueueItem( face , xs , ys , averageZ , alternateAppearance , normalZ ) );
				}
			}
		}
	}

	public void paint( final Graphics2D g , final boolean fill , final boolean outline )
	{
		/*
		 * Process queue and process its entries in sorted order.
		 */
		final QueueItem[] list = (QueueItem[])_queue.toArray( new QueueItem[ _queue.size() ] );

		int todo = list.length;
		while ( todo > 0 )
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
			final Face3D   face          = entry.face;
			final Object3D object        = face.getObject();
			final int[]    xs            = entry.xs;
			final int[]    ys            = entry.ys;
			final int      length        = xs.length;
			final boolean  polygonFilled = ( fill || ( length == 1 ) );

			if ( polygonFilled )
			{
				Paint paint = entry.alternateAppearance ? object.alternateFillPaint : object.fillPaint;
				if ( fill && _perspective && ( paint instanceof Color ) )
				{
					final float shadeFactor = 0.5f;

					final float factor = Math.min( 1.0f , ( 1.0f - shadeFactor ) + shadeFactor * Math.abs( (float)entry.nz ) );
					if ( factor < 1.0f )
					{
						final Color   color = (Color)paint;
						final float[] rgb   = color.getRGBComponents( null );

						paint = new Color( factor * rgb[ 0 ] , factor * rgb[ 1 ] , factor * rgb[ 2 ] , rgb[ 3 ] );
					}
				}

				g.setPaint( paint );
				g.fillPolygon( xs , ys , length );
			}

			if ( outline )
			{
				if ( polygonFilled )
					g.setColor( Color.DARK_GRAY );
				else
					g.setPaint( entry.alternateAppearance ? object.alternateOutlinePaint : object.outlinePaint );

				g.drawPolygon( xs , ys , length );
			}
		}
	}
}
