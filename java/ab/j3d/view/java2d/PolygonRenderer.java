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
import ab.j3d.TextureSpec;
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
	private Matrix3D _projectionTransform;
	private Matrix3D _viewTransform;
	private boolean  _perspective;
	private boolean  _backfaceCulling;

	private final List _queuedItems = new ArrayList();

	private final Node3DCollection _tempNodeCollection = new Node3DCollection();
	private double[] _tempPointCoords;
	private double[] _tempNormals;

	private static final class QueueItem
	{
		private Face3D  face;
		private double  z;
		private int[]   xs;
		private int[]   ys;
		private int     vertexCount;
		private boolean alternateAppearance;
		private double  normalZ;

		QueueItem()
		{
			face                = null;
			z                   = 0.0;
			xs                  = null;
			ys                  = null;
			vertexCount         = 0;
			alternateAppearance = false;
			normalZ             = 1.0;
		}
	}

	public PolygonRenderer()
	{
		_projectionTransform = null;
		_viewTransform       = null;
		_perspective         = false;
		_backfaceCulling     = false;
		_tempPointCoords     = null;
		_tempNormals         = null;
	}

	public void clear( final Matrix3D projectionTransform , final Matrix3D viewTransform , final boolean hasPerspective , final boolean performBackfaceCulling )
	{
		_projectionTransform = projectionTransform;
		_viewTransform       = viewTransform;
		_perspective         = hasPerspective;
		_backfaceCulling     = performBackfaceCulling;

		_tempNodeCollection.clear();
		_queuedItems.clear();
	}

	public void add( final Node3D node , final Color overridePaint )
	{
		final Node3DCollection nodes = _tempNodeCollection;
		nodes.clear();
		node.gatherLeafs( nodes , Object3D.class , _viewTransform , false );

		for ( int nodeIndex = 0 ; nodeIndex < nodes.size() ; nodeIndex++ )
		{
			final Matrix3D transform = nodes.getMatrix( nodeIndex );
			final Object3D object    = (Object3D)nodes.getNode( nodeIndex );

			object.outlinePaint = overridePaint;
			object.fillPaint    = overridePaint;

			add( transform , object , false );
		}
	}

	public void add( final Node3D node , final boolean alternateAppearance )
	{
		final Node3DCollection nodes = _tempNodeCollection;
		nodes.clear();
		node.gatherLeafs( nodes , Object3D.class , _viewTransform , false );

		for ( int nodeIndex = 0 ; nodeIndex < nodes.size() ; nodeIndex++ )
		{
			final Matrix3D transform = nodes.getMatrix( nodeIndex );
			final Object3D object    = (Object3D)nodes.getNode( nodeIndex );

			add( transform , object , alternateAppearance );
		}
	}

	public void add( final Matrix3D transform , final Object3D object , final boolean alternateAppearance )
	{
		final boolean  perspective         = _perspective;
		final boolean  backfaceCulling     = _backfaceCulling;
		final Matrix3D projectionTransform = _projectionTransform;
		final List     queuedItems         = _queuedItems;

		final int      pointCount  = object.getPointCount();
		final double[] pointCoords = ( _tempPointCoords = transform.transform( object.getPointCoords() , _tempPointCoords , pointCount ) );

		int faceCount = object.getFaceCount();
		if ( ( faceCount > 0 ) && perspective )
		{
			for ( int i = 0 , k = 0 ; i < pointCount ; i++ , k += 3 )
			{
				final double x = pointCoords[ k     ];
				final double y = pointCoords[ k + 1 ];
				final double z = pointCoords[ k + 2 ];

				if ( z > 1999.0 )
				{
					faceCount = 0;
					break;
				}

				final double f = 1000.0 / ( 2000.0 - z );

				pointCoords[ k     ] = x * f;
				pointCoords[ k + 1 ] = y * f;
			}
		}

		if ( faceCount > 0 )
		{
			final double[] faceNormals = ( _tempNormals = transform.rotate( object.getFaceNormals() , _tempNormals , faceCount ) );

			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face        = object.getFace( faceIndex );
				final int    vertexCount = face.getVertexCount();

				/*
				 * If we have less than two points, its not a valid line/face.
				 */
				if ( vertexCount > 1 )
				{
					final int[]   pointIndices = face.getPointIndices();
					final boolean hasBackface  = face.hasBackface();

					/*
					 * Perform backface removal
					 *
					 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
					 */
					if ( !hasBackface && backfaceCulling )
					{
						final double x1 = pointCoords[ pointIndices[ 0 ] * 3     ];
						final double y1 = pointCoords[ pointIndices[ 0 ] * 3 + 1 ];

						final double x2 = pointCoords[ pointIndices[ 1 ] * 3     ];
						final double y2 = pointCoords[ pointIndices[ 1 ] * 3 + 1 ];

						final double x3 = pointCoords[ pointIndices[ 2 ] * 3     ];
						final double y3 = pointCoords[ pointIndices[ 2 ] * 3 + 1 ];

						if ( ( ( x1 - x2 ) * ( y3 - y2 ) - ( y1 - y2 ) * ( x3 - x2 ) ) < 0 )
							continue;
					}

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

						xs[ p ] = (int)projectionTransform.transformX( x , y , 0.0 );
						ys[ p ] = (int)projectionTransform.transformY( x , y , 0.0 );
					}

					averageZ = averageZ / (double)vertexCount;

					final QueueItem queueItem = new QueueItem();
					queueItem.face                = face;
					queueItem.xs                  = xs;
					queueItem.ys                  = ys;
					queueItem.vertexCount         = vertexCount;
					queueItem.z                   = averageZ;
					queueItem.alternateAppearance = alternateAppearance;
					queueItem.normalZ             = normalZ;
					queuedItems.add( queueItem );
				}
			}
		}
	}

	public void paint( final Graphics2D g , final boolean fill , final boolean outline , final boolean useTextures )
	{
		/*
		 * Process queue and process its entries in sorted order.
		 */
		final QueueItem[] list = (QueueItem[])_queuedItems.toArray( new QueueItem[ _queuedItems.size() ] );

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
			final Face3D      face          = entry.face;
			final TextureSpec texture       = face.getTexture();
			final Object3D    object        = face.getObject();
			final int[]       xs            = entry.xs;
			final int[]       ys            = entry.ys;
			final int         length        = entry.vertexCount;
			final boolean     polygonFilled = ( fill || ( length < 3 ) );

			Paint fillPaint = null;
			if ( polygonFilled )
			{
				fillPaint = entry.alternateAppearance ? object.alternateFillPaint : ( useTextures && ( texture != null ) && !texture.isTexture() ) ? texture.getColor() : object.fillPaint;
				if ( fill && _perspective && ( fillPaint instanceof Color ) )
				{
					final float shadeFactor = 0.5f;

					final float factor = Math.min( 1.0f , ( 1.0f - shadeFactor ) + shadeFactor * Math.abs( (float)entry.normalZ ) );
					if ( factor < 1.0f )
					{
						final Color   color = (Color)fillPaint;
						final float[] rgb   = color.getRGBComponents( null );

						fillPaint = new Color( factor * rgb[ 0 ] , factor * rgb[ 1 ] , factor * rgb[ 2 ] , rgb[ 3 ] );
					}
				}

				if ( fillPaint != null )
				{
					g.setPaint( fillPaint );
					g.fillPolygon( xs , ys , length );
				}
			}

			if ( outline || ( fillPaint == null ) )
			{
				if ( fillPaint != null )
					g.setColor( Color.darkGray );
				else
					g.setPaint( entry.alternateAppearance ? object.alternateOutlinePaint : object.outlinePaint );

				g.drawPolygon( xs , ys , length );
			}
		}
	}
}
