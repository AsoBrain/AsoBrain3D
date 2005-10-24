/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
package ab.j3d.view;

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

import com.numdata.oss.AugmentedArrayList;
import com.numdata.oss.AugmentedList;

/**
 * This class manages a render queue that can be used by a 3D render engine. It
 * has the following features:
 * <dl>
 *   <dt>Manage a queue of 'to be rendered' objects.</dt>
 *   <dd>
 *     This is the core function of this class. The queue is filled based on
 *     the contents of a view and updated whenever the view changes. The render
 *     engine takes the queue contents and renders them on the output device.
 *     <br /><br />Methods: {@link #enqueueObject}.
 *   </dd>
 *   <dt>Provide a mechanism to reuse objects in the queue.</dt>
 *   <dd>
 *     Objects in the queue may require considerable memory but have a short
 *     life cycle (it ends whenever the view volume or its contents change).
 *     To improve performance a lightweight mechanism to reuse such objects
 *     is needed. This should also allow the use of more complex or memory
 *     consuming temporary objects.
 *     <br /><br />Methods: {@link #allocatePolygon}, {@link #releasePolygon}.
 *   </dd>
 *   </dd>
 * </dl>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class RenderQueue
{
	/**
	 * List of queued {@link RenderedPolygon} instances. If sorted, they shall
	 * be sorted in rear-to-front ordering.
	 */
	final List _queue;

	/**
	 * List of {@link List} instances containing freed objects. The index in the
	 * list is the number of points in the freed polygon Index minus 1. Elements
	 * are created on-demand (inialized to <code>null</code>).
	 */
	final AugmentedList _freeLists;

	/**
	 * Temporary/shared storage area for {@link #enqueueScene}.
	 */
	private final Node3DCollection _tmpNodeCollection;

	/**
	 * Temporary/shared storage area for {@link #enqueueObject}.
	 */
	private double[] _tmpPointCoords;

	/**
	 * Temporary/shared storage area for {@link #enqueueObject}.
	 */
	private int[] _tmpProjectedCoords;

	/**
	 * Temporary/shared storage area for {@link #enqueueObject}.
	 */
	private double[] _tmpFaceNormals;

//	/**
//	 * Temporary/shared storage area for {@link #enqueueObject}.
//	 */
//	private double[] _tmpVertexNormals;

	/**
	 * Construct new render queue.
	 */
	public RenderQueue()
	{
		_queue              = new ArrayList( 64 );
		_freeLists          = new AugmentedArrayList( 4 );
		_tmpNodeCollection  = new Node3DCollection();
		_tmpPointCoords     = null;
		_tmpProjectedCoords = null;
		_tmpFaceNormals     = null;
//		_tmpVertexNormals   = null;
	}

	/**
	 * Clear contents of queue.
	 */
	public void clearQueue()
	{
		final List queue = _queue;

		for ( int i = queue.size() ; --i >= 0 ; )
			releasePolygon( (RenderedPolygon)queue.get( i ) );

		queue.clear();
	}

	/**
	 * Add scene to queue from the specified camera.
	 *
	 * @param   camera              Camera to enqueue scene for.
	 * @param   projector           Projects camera coordinates on image plate pixels.
	 * @param   backfaceCulling     Prevent backfaces from being rendered.
	 */
	public void enqueueScene( final Camera3D camera , final Projector projector , final boolean backfaceCulling )
	{
		final Node3DCollection nodeCollection = _tmpNodeCollection;
		nodeCollection.clear();
		camera.gatherLeafs( nodeCollection , Object3D.class , Matrix3D.INIT , true );

		for ( int i = 0 ; i < nodeCollection.size() ; i++ )
		{
			final Node3D node = nodeCollection.getNode( i );
			if ( node instanceof Object3D )
				enqueueObject( projector , backfaceCulling , nodeCollection.getMatrix( i ) , (Object3D)node , false );
		}
	}

	/**
	 * Add 3D object to queue.
	 *
	 * @param   projector               Projects view coordinates on image plate pixels.
	 * @param   backfaceCulling         Prevent backfaces from being rendered.
	 * @param   object2view             Transformation from object to view space.
	 * @param   object                  Object to add to queue.
	 * @param   alternateAppearance     Enqueue object using alternate appearance properties.
	 */
	public void enqueueObject( final Projector projector , final boolean backfaceCulling , final Matrix3D object2view , final Object3D object , final boolean alternateAppearance )
	{
		final int      faceCount       = object.getFaceCount();
		final int      pointCount      = object.getPointCount();
		final double[] pointCoords     = ( _tmpPointCoords     = object2view.transform( object.getPointCoords()   , _tmpPointCoords     , pointCount ) );
		final int[]    projectedCoords = ( _tmpProjectedCoords = projector.project    ( pointCoords               , _tmpProjectedCoords , pointCount ) );
		final double[] faceNormals     = ( _tmpFaceNormals     = object2view.rotate   ( object.getFaceNormals()   , _tmpFaceNormals     , faceCount  ) );
//		final double[] vertexNormals   = ( _tmpVertexNormals   = object2view.rotate   ( object.getVertexNormals() , _tmpVertexNormals   , pointCount ) );

		for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
		{
			final Face3D face = object.getFace( faceIndex );

			if ( projector.inViewVolume( face , pointCoords ) )
			{
				final RenderedPolygon polygon = allocatePolygon( face.getVertexCount() );
				polygon.initialize( face , pointCoords , projectedCoords , faceNormals , alternateAppearance );

				if ( !backfaceCulling || face.hasBackface() || !polygon.isBackface() ) // Perform backface removal
					enqueuePolygon( polygon );
			}
		}
	}

	/**
	 * Add polygon to queue.
	 *
	 * @param   polygon     Polygon to enqueue.
	 */
	public void enqueuePolygon( final RenderedPolygon polygon )
	{
		_queue.add( polygon );
	}

	/**
	 * Get sorted list of polygons in queue.
	 *
	 * @return  Sorted list of polygons in queue.
	 */
	public RenderedPolygon[] getQueuedPolygons()
	{
		final RenderedPolygon[] result = new RenderedPolygon[ _queue.size() ];
		_queue.toArray( result );
		return result;
	}

	/**
	 * Allocate polygon with the specified number of points.
	 *
	 * @param   pointCount      Desired number of points in polygon.
	 *
	 * @return  {@link RenderedPolygon} object.
	 *
	 * @see     #releasePolygon
	 */
	public RenderedPolygon allocatePolygon( final int pointCount )
	{
		final RenderedPolygon result;

		final List lists     = _freeLists;
		final int  listIndex = pointCount - 1;

		if ( listIndex >= lists.size() )
		{
			result = new RenderedPolygon( pointCount );
		}
		else
		{
			final List list = (List)lists.get( listIndex );
			if ( ( list == null ) || list.isEmpty() )
			{
				result = new RenderedPolygon( pointCount );
			}
			else
			{
				final int listSize = list.size();
				result = (RenderedPolygon)list.remove( listSize - 1 );
			}
		}

		return result;
	}

	/**
	 * Release polygon for reuse by the render queue. This can be called after
	 * a polygon is removed from the queue and is no longer used.
	 * <dl>
	 *  <dt>IMPORTANT:</dt>
	 *  <dd>
	 *   This method invalidates the specified polygon. <strong>Do not use
	 *   the polygon object in any way after calling this method.</strong>
	 *  </dd>
	 * </dl>
	 *
	 * @param   polygon     Polygon to be released.
	 *
	 * @see     #allocatePolygon
	 */
	public void releasePolygon( final RenderedPolygon polygon )
	{
		final int pointCount = polygon._pointCount;
		final int listIndex  = pointCount - 1;

		final AugmentedList lists = _freeLists;
		if ( pointCount >= lists.size() )
			lists.setLength( pointCount );

		List list = (List)lists.get( listIndex );
		if ( list == null )
		{
			list = new ArrayList( 16 );
			lists.set( listIndex , list );
		}

		polygon.destroy();
		list.add( polygon );
	}
}
