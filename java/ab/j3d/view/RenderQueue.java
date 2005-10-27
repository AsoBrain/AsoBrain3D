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
import ab.j3d.Vector3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

import com.numdata.oss.AugmentedArrayList;
import com.numdata.oss.AugmentedList;
import com.numdata.oss.ArrayTools;

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

	/**
	 * Temporary storage for clipping variables
	 */
	private double[] _frontX;
	private double[] _frontY;
	private double[] _frontZ;
	private double[] _backX;
	private double[] _backY;
	private double[] _backZ;

	/**
	 * Temporary storage for clipping variables
	 */
	private int[] _frontProjX;
	private int[] _frontProjY;
	private int[] _backProjX;
	private int[] _backProjY;


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
			else
			{
				//System.out.println( "Face not in view volume" );
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
		//System.out.println( "\ninserting item " + polygon.name );
		boolean stop = false;

		if ( _queue.isEmpty() )
		{
			//System.out.println( "Queue is empty, just adding item." );
			_queue.add( polygon );
			stop = true;
		}
		for ( int pointer = _queue.size() - 1; pointer >= 0 && !stop; pointer-- )
		{
			final RenderedPolygon other = (RenderedPolygon)_queue.get( pointer );

			//System.out.println( "Comparing with " + other.name );
			switch ( compare( polygon , other ) )
			{
				case IN_FRONT:
					//System.out.println( "In front. Stop sorting." );
					_queue.add( pointer + 1 , polygon );
					stop = true;
					break;

				case INTERSECTING:
					//System.out.print( "Intersecting -- " );
					switch ( compare( other , polygon ) )
					{
						case BEHIND:
							//System.out.println( "Other item is behind. Stop sorting" );
							_queue.add( pointer + 1 , polygon );
							stop = true;
							break;

						case INTERSECTING:
							//System.out.println( "Intersecting. Clipping." );
							//if ( overlap(item, other) )
							{
								//polygon.text += "Clipping  ";
								clip( polygon , other );
								stop = true;
							}
							break;

						case COPLANAR:
						case IN_FRONT:
							//System.out.println( "Other item is coplanar or in front. Keep sorting" );
							/* keep sorting */
							break;
					}
					break;

				case COPLANAR:
				case BEHIND:
					//System.out.println( "Coplanar or behind. Keep sorting" );
					/* keep sorting */
					break;
			}
		}

		if ( !stop )
		{
			/*System.out.println( "insert at 0 (size: " +_queuedItems.size()+ ")" );*/
			//System.out.println( "Stop is false. Inserting item at front" );
			_queue.add( 0 , polygon );
		}
	}

	private int compare( RenderedPolygon polygon , RenderedPolygon other )
	{
		boolean  behind      = false;
		boolean  inFront     = false;
		double   d           = other._planeConstant;
		int      vertexCount = polygon._pointCount;
		double[] xCoords     = polygon._viewX;
		double[] yCoords     = polygon._viewY;
		double[] zCoords     = polygon._viewZ;

//		System.out.print( "compare: " );
		if ( polygon._maxZ <= other._minZ )
		{
//			System.out.println( " Item's Z ("+polygon._maxZ+") is smaller than other's Z ("+other._minZ+")" );
			/*item.text += " - Item's Z ("+item.maxZ+") is smaller than other's Z ("+other.minZ+")";*/
			behind = true;
		}
		else if ( polygon._minZ >= other._maxZ )
		{
//			System.out.println( " Others Z ("+other._maxZ+") is smaller than item's Z ("+polygon._minZ+")" );
			/*item.text += " - Others Z ("+other.maxZ+") is smaller than item's Z ("+item.minZ+")";*/
			inFront = true;
		}
		else
		{
			behind = false;
			inFront = false;
			for ( int vertex = 0; !( behind && inFront ) && vertex < vertexCount; vertex++ )
			{
				final double x = xCoords[ vertex ];
				final double y = yCoords[ vertex ];
				final double z = zCoords[ vertex ];

				final double dot = Vector3D.dot( other._planeNormalX , other._planeNormalY , other._planeNormalZ , x , y , z );
//				System.out.println( "Checking wether in front or behind. Coordinates: (" +x+ ", " +y+ "," +z+ "). D: " + d + "  Dot: " +dot+ "." );
				behind  = behind  || ( d > ( dot + 0.001 ) );
				inFront = inFront || ( d < ( dot - 0.001 ) );
			}

/*			if ( behind && !inFront )
			{
//				System.out.println( "Item is behind" );
				polygon.text += " - " + polygon.name + " is behind " + other.name;
			}
			if ( inFront && !behind)
			{
//				System.out.println( "Item is in front" );
				polygon.text += " - " + polygon.name + " is in front of " + other.name;
			}

			if ( behind && inFront )
			{
				polygon.text += " - " +polygon.name+ " intersects " + other.name;
			}

			if ( !behind && !inFront )
			{
				polygon.text += " - " +polygon.name+ " is coplanar to " + other.name;
			}*/
		}

		int compared = behind
		               ? inFront ? INTERSECTING : BEHIND
		               : inFront ? IN_FRONT : COPLANAR;
		return compared;
	}

	private void clip( RenderedPolygon polygon , RenderedPolygon cuttingPlane )
	{
		/*System.out.println( "----------------------------------------------------------------------" );
		System.out.println( item.face.toFriendlyString( "item: " ) );
		System.out.println( cuttingPlane.face.toFriendlyString( "cuttingPlane: " ) );*/

		final double   cuttingD    = cuttingPlane._planeConstant;
		final int      vertexCount = polygon._pointCount;
		final double[] xCoords     = polygon._viewX;
		final double[] yCoords     = polygon._viewY;
		final double[] zCoords     = polygon._viewZ;
		final int[]    projectedX  = polygon._projectedX;
		final int[]    projectedY  = polygon._projectedY;

		int frontCount = 0;
		int backCount = 0;
		_frontX = (double[])ArrayTools.ensureLength( _frontX , double.class , 1 , vertexCount + 2 );
		_frontY = (double[])ArrayTools.ensureLength( _frontY , double.class , 1 , vertexCount + 2 );
		_frontZ = (double[])ArrayTools.ensureLength( _frontZ , double.class , 1 , vertexCount + 2 );
		_backX  = (double[])ArrayTools.ensureLength( _backX , double.class , 1 ,  vertexCount + 2 );
		_backY  = (double[])ArrayTools.ensureLength( _backY , double.class , 1 ,  vertexCount + 2 );
		_backZ  = (double[])ArrayTools.ensureLength( _backZ , double.class , 1 ,  vertexCount + 2 );

		_frontProjX = (int[])ArrayTools.ensureLength( _frontProjX , int.class , 1 , vertexCount + 2 );
		_frontProjY = (int[])ArrayTools.ensureLength( _frontProjY , int.class , 1 , vertexCount + 2 );
		_backProjX  = (int[])ArrayTools.ensureLength( _backProjX , int.class , 1 ,  vertexCount + 2 );
		_backProjY  = (int[])ArrayTools.ensureLength( _backProjY , int.class , 1 ,  vertexCount + 2 );

		final double cuttingNX = cuttingPlane._planeNormalX;
		final double cuttingNY = cuttingPlane._planeNormalY;
		final double cuttingNZ = cuttingPlane._planeNormalZ;

		int lastIndex = vertexCount - 1;
		double lastX     = xCoords[ lastIndex ];
		double lastY     = yCoords[ lastIndex ];
		double lastZ     = zCoords[ lastIndex ];
		int    lastProjX = projectedX[ lastIndex ];
		int    lastProjY = projectedY[ lastIndex ];
		double lastDot   = Vector3D.dot( cuttingNX , cuttingNY , cuttingNZ , lastX , lastY , lastZ );

		for ( int vertex = 0; vertex < vertexCount; vertex++ )
		{
			final double x     = xCoords[ vertex ];
			final double y     = yCoords[ vertex ];
			final double z     = zCoords[ vertex ];
			final int    projX = projectedX[ vertex ];
			final int    projY = projectedY[ vertex ];
			final double dot   = Vector3D.dot( cuttingNX , cuttingNY , cuttingNZ , x , y , z );

			if ( ( dot > cuttingD && lastDot < cuttingD )
			     || ( dot < cuttingD && lastDot > cuttingD ) )
			{
				double u = ( ( cuttingNX * lastX ) + ( cuttingNY * lastY ) + ( cuttingNZ * lastZ ) - cuttingD )
				           / ( cuttingNX * ( lastX - x ) + cuttingNY * ( lastY - y ) + cuttingNZ * ( lastZ - z ) );

				if ( u < 0 || u > 1 )
				{
					System.out.println( "cuttingNormal: " + Vector3D.toFriendlyString( Vector3D.INIT.set( cuttingNX , cuttingNY , cuttingNZ ) ) + " - cuttingD: " + cuttingD );
					System.out.println( "Last: coordinates: (" + (int)lastX + ',' + (int)lastY + ',' + (int)lastZ + ") - dot: " + lastDot );
					System.out.println( "Current: coordinates: (" + (int)x + ',' + (int)y + ',' + (int)z + ")  - dot: " + dot );

					throw new IllegalStateException( "Error! Trying to cut a line that does not intersect the cutting plane (u=" + u ); //+ " , u1=" + u1 + ')' );
				}

				double cutX     = lastX + u * ( x - lastX );
				double cutY     = lastY + u * ( y - lastY );
				double cutZ     = lastZ + u * ( z - lastZ );
				int    cutProjX = (int)( (double)lastProjX + u * (double)( projX - lastProjX ) );
				int    cutProjY = (int)( (double)lastProjY + u * (double)( projY - lastProjY ) );

//				System.out.println( "Intersection  newX: " + cutX + "  newY: " +cutY+ "  newZ: " +cutZ+ " " );
				_frontX[ frontCount ]     = cutX;
				_frontY[ frontCount ]     = cutY;
				_frontZ[ frontCount ]     = cutZ;
				_frontProjX[ frontCount ] = cutProjX;
				_frontProjY[ frontCount ] = cutProjY;
				frontCount++;

				_backX[ backCount ]     = cutX;
				_backY[ backCount ]     = cutY;
				_backZ[ backCount ]     = cutZ;
				_backProjX[ backCount ] = cutProjX;
				_backProjY[ backCount ] = cutProjY;
				backCount++;
				//System.out.println( "Item "+item.name+"A: " +cuttingNX+ " B: " +cuttingNY+ " C: " +cuttingNZ+ " D: " +cuttingD+ " U: "+u+" P1 Coordinates: (" +(int)lastX+ "," +(int)lastY+ "," +(int)lastZ+ ") - P2 coordinates: (" +(int)x+ "," +(int)y+ "," +(int)z+ ") - Cut point (" +cutX+ "," +cutY+ "," +cutZ+ ")");
			}
			if ( dot >= cuttingD )
			{
//				System.out.println( "Front  newX: " + x + "  newY: " +y+ "  newZ: " +z+ " " );
				_frontX[ frontCount ]     = x;
				_frontY[ frontCount ]     = y;
				_frontZ[ frontCount ]     = z;
				_frontProjX[ frontCount ] = projX;
				_frontProjY[ frontCount ] = projY;
				frontCount++;
			}
			if ( dot <= cuttingD )
			{
//				System.out.println( "Back  newX: " + x + "  newY: " +y+ "  newZ: " +z+ " " );
				_backX[ backCount ]     = x;
				_backY[ backCount ]     = y;
				_backZ[ backCount ]     = z;
				_backProjX[ backCount ] = projX;
				_backProjY[ backCount ] = projY;
				backCount++;
			}

			lastDot   = dot;
			lastX     = x;
			lastY     = y;
			lastZ     = z;
			lastProjX = projX;
			lastProjY = projY;
		}

//		System.out.println( "Frontcount: " + frontVertexCount + "  - backcount: " + backVertexCount );
		RenderedPolygon front = new RenderedPolygon( frontCount );
		int minZ = Integer.MAX_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for ( int i = 0; i < frontCount; i++ )
		{
			int vertexZ = (int)_frontZ[ i ];
			front._viewX[ i ] = _frontX[ i ];
			front._viewY[ i ] = _frontY[ i ];
			front._viewZ[ i ] = _frontZ[ i ];

			front._projectedX[ i ] = _frontProjX[ i ];
			front._projectedY[ i ] = _frontProjY[ i ];
//			System.out.println( "Front: (" +frontCoords[p]+ ", " +frontCoords[p+1]+ ", " +frontCoords[p+2]+ ")" );

			minZ = vertexZ < minZ ? vertexZ : minZ;
			maxZ = vertexZ > maxZ ? vertexZ : maxZ;
		}
		front._minZ                = minZ;
		front._maxZ                = maxZ;
		front._alternateAppearance = polygon._alternateAppearance;
		front._object              = polygon._object;
		front._planeConstant       = polygon._planeConstant;
		front._planeNormalX        = polygon._planeNormalX;
		front._planeNormalY        = polygon._planeNormalY;
		front._planeNormalZ        = polygon._planeNormalZ;
		front._texture             = polygon._texture;
		front.name                 = polygon.name + "_front";
		enqueuePolygon( front );

//		System.out.println( "backcount: " + backVertexCount + "  - backcount: " + backVertexCount );
		RenderedPolygon back = new RenderedPolygon( backCount );
		minZ = Integer.MAX_VALUE;
		maxZ = Integer.MIN_VALUE;
		for ( int i = 0; i < backCount; i++ )
		{
			int vertexZ = (int)_backZ[ i ];
			back._viewX[ i ] = _backX[ i ];
			back._viewY[ i ] = _backY[ i ];
			back._viewZ[ i ] = _backZ[ i ];

			back._projectedX[ i ] = _backProjX[ i ];
			back._projectedY[ i ] = _backProjY[ i ];
//			System.out.println( "back: (" +backCoords[p]+ ", " +backCoords[p+1]+ ", " +backCoords[p+2]+ ")" );

			minZ = vertexZ < minZ ? vertexZ : minZ;
			maxZ = vertexZ > maxZ ? vertexZ : maxZ;
		}
		back._minZ                = minZ;
		back._maxZ                = maxZ;
		back._alternateAppearance = polygon._alternateAppearance;
		back._object              = polygon._object;
		back._planeConstant       = polygon._planeConstant;
		back._planeNormalX        = polygon._planeNormalX;
		back._planeNormalY        = polygon._planeNormalY;
		back._planeNormalZ        = polygon._planeNormalZ;
		back._texture             = polygon._texture;
		back.name                 = polygon.name + "_back";
		enqueuePolygon( back );
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

	private static final int IN_FRONT     = 0;
	private static final int BEHIND       = 1;
	private static final int INTERSECTING = 2;
	private static final int COPLANAR     = 3;
}

