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
import java.io.PrintWriter;
import java.io.IOException;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

import com.numdata.oss.ArrayTools;
import com.numdata.oss.AugmentedArrayList;
import com.numdata.oss.AugmentedList;
import com.numdata.oss.io.IndentingWriter;

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
	 * Indicates a polygon is in front of another. Used in {@link #compare}.
	 *
	 * @see #compare
	 */
	private static final int IN_FRONT     = 0;

	/**
	 * Indicates a polygon is in behind another. Used in {@link #compare}.
	 *
	 * @see #compare
	 */
	private static final int BEHIND       = 1;

	/**
	 * Indicates a polygon intersects with another. Used in {@link #compare}.
	 *
	 * @see #compare
	 */
	private static final int INTERSECTING = 2;

	/**
	 * Indicates a polygon is coplanar to another. Used in {@link #compare}.
	 *
	 * @see #compare
	 */
	private static final int COPLANAR     = 3;

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
	 * Temporary storage for clipping variables.
	 */
	private double[] _frontX;

	/**
	 * Temporary storage for clipping variables.
	 */
	private double[] _frontY;

	/**
	 * Temporary storage for clipping variables.
	 */
	private double[] _frontZ;

	/**
	 * Temporary storage for clipping variables.
	 */
	private double[] _backX;

	/**
	 * Temporary storage for clipping variables.
	 */
	private double[] _backY;

	/**
	 * Temporary storage for clipping variables.
	 */
	private double[] _backZ;

	/**
	 * Temporary storage for clipping variables.
	 */
	private int[] _frontProjX;

	/**
	 * Temporary storage for clipping variables.
	 */
	private int[] _frontProjY;

	/**
	 * Temporary storage for clipping variables.
	 */
	private int[] _backProjX;

	/**
	 * Temporary storage for clipping variables.
	 */
	private int[] _backProjY;

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

				//@FIXME: Debugging variable. Should be removed when the renderqueue works properly.
				polygon._name = (String)object.getTag();

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
		boolean stop = false;

		if ( _queue.isEmpty() )
		{
			_queue.add( polygon );
			stop = true;
		}

		final double z = polygon._minZ;
		for ( int pointer = 0 ; pointer < _queue.size() && !stop ; pointer++ )
		{
			final RenderedPolygon other = (RenderedPolygon)_queue.get( pointer );
			final double otherZ = other._minZ;

			if ( otherZ > z )
			{
				_queue.add( pointer , polygon );
				stop = true;
			}
		}

		if ( !stop )
		{
			_queue.add( _queue.size() , polygon);
		}
	}

	/**
	 * Method for debugging purposes. Should be removed when the RenderQueue
	 * works properly. This methods sorts the {@link List} of
	 * {@link RenderedPolygon}s given to it, based on insertion sort.
	 *
	 * @param   tempQueue   {@link List} of {@link RenderedPolygon}s to sort.
	 *
	 * @return  Sorted version of the {@link List}.
	 */
	public static List sortPolygonList( final List tempQueue )
	{
		final List result = new ArrayList( tempQueue.size() );
		for ( int i = 0 ; i < tempQueue.size() ; i++ )
		{
			final RenderedPolygon polygon = (RenderedPolygon)tempQueue.get( i );
			boolean stop = false;

			if ( result.isEmpty() )
			{
				result.add( polygon );
				stop = true;
			}

			final double z = polygon._minZ;
			for ( int pointer = 0 ; pointer < result.size() && !stop ; pointer++ )
			{
				final RenderedPolygon other = (RenderedPolygon)result.get( pointer );
				final double otherZ = other._minZ;

				if ( otherZ > z )
				{
					result.add( pointer , polygon );
					stop = true;
				}
			}

			if ( !stop )
			{
				result.add( result.size() , polygon);
			}
		}

		return result;
	}

	/**
	 * Get sorted list of polygons in queue.
	 *
	 * @return  Sorted list of polygons in queue.
	 */
	public RenderedPolygon[] getQueuedPolygons()
	{
		List queue = _queue;

//		System.out.print( "Temp queue order: " );
//		for( int i = 0 ; i < queue.size() ; i++ )
//		{
//			RenderedPolygon p = (RenderedPolygon)queue.get( i );
//			System.out.print( p._name + " " );
//		}
//		System.out.println( " " );

		final IndentingWriter out = new IndentingWriter( new PrintWriter( System.out ) );

		queue = sortQueue( queue , out );
//		queue = sortPolygonList( queue );
//		queue = sortQueue( queue );

//		System.out.print( "Final queue order: " );
//		for( int i = 0 ; i < queue.size() ; i++ )
//		{
//			RenderedPolygon p = (RenderedPolygon)queue.get( i );
//			System.out.print( p._name + " " );
//		}
//		System.out.println( " " );

		return (RenderedPolygon[])queue.toArray( new RenderedPolygon[ queue.size() ] );
	}

	/**
	 * Method for debugging purposes. Contents of this method should be moved to
	 * {@link #getQueuedPolygons} when the {@link RenderQueue} works properly.
	 * This method sorts the {@link RenderedPolygon}s in a given {@link List},
	 * calling the {@link #order} method for each.
	 *
	 * @param   queue   {@link List} with the {@link RenderedPolygon}s to be
	 *                  sorted.
	 * @param   out     {@link IndentingWriter} used for writing debugging info
	 *                  to the command line. Should be removed when renderqueue
	 *                  works properly
	 *
	 * @return  Sorted list of polygons in queue.
	 *
	 * @see     #order
	 */
	public List sortQueue( final List queue , final IndentingWriter out )
	{
		final ArrayList result = new ArrayList( queue.size() + ( queue.size() / 3 ) );
		final ArrayList tempQueue = new ArrayList( queue );

		int i = 0;
		while ( i < tempQueue.size() )
		{
			for ( i = 0 ; i < tempQueue.size() ; i++ )
			{
				final RenderedPolygon p = (RenderedPolygon)tempQueue.get( i );
				if ( p != null )
				{
					try
					{
//						out.writeln( "Ordering polygon " + p._name );
//						out.writeln( "{" );
//						out.indentIn();
						order( p , tempQueue , result , new ArrayList( 5 ), out );
//						out.indentOut();
//						out.writeln( "}" );
//						out.flush();
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Moves {@link RenderedPolygon} <code>poly</code> from the temporary queue
	 * of polygons (<code>tempQueue</code>) to the right position in the result
	 * queue (<code>finalQueue</code>). This method compares <code>poly</code>
	 * to all other {@link RenderedPolygon}s in <code>tempQueue</code> to see if
	 * it really is the {@link RenderedPolygon} furthest from the camera. If it
	 * is, it's reference in <code>tempQueue</code> is set to <code>null</code>
	 * and the polygon is added to <code>finalQueue</code>. If it is not
	 * furthest from the camera, the {@link RenderedPolygon} that lies behind it
	 * will be ordered first (by recursively calling this method for the other
	 * {@link RenderedPolygon}).
	 * <p>
	 * Although it is not absolutely necessary, it is recommended to sort the
	 * polygons in <code>tempQueue</code> by z-depth before ordering any
	 * polygons.
	 * <p>
	 * Note that this method does not work as it should at the moment. The
	 * biggest problem is that cycle detection isn't working properly. This
	 * causes either an infinite loop or some polygons are not sorted properly.
	 * An {@link IndentingWriter} is passed on as a parameter to generate output
	 * of the ordering proces.
	 *
	 * @param   poly            {@link RenderedPolygon} to put in the final
	 *                          queue.
	 * @param   tempQueue       {@link List} holding the
	 *                          {@link RenderedPolygon}s still to be sorted.
	 *                          This {@link List} may contain <code>null</code>
	 *                          references to indicate the polygon at that
	 *                          location has already been sorted into the final
	 *                          queue.
	 * @param   finalQueue      {@link List} holding the
	 *                          {@link RenderedPolygon}s that have already been
	 *                          sorted.
	 * @param   polygonStack    {@link List} of all polygons that were being
	 *                          sorted but needed to recursively call this
	 *                          method because another polygon lay behind them.
	 *                          Used for cycle detection.
	 * @param   out             {@link IndentingWriter} used for writing
	 *                          debugging info to the command line. Should be
	 *                          removed when renderqueue works properly.
	 *
	 * @throws  IOException if the {@link IndentingWriter} throws an exception.
	 */
	private void order( final RenderedPolygon poly , final List tempQueue , final List finalQueue , final List polygonStack , final IndentingWriter out )
		throws IOException
	{
		RenderedPolygon polygon = poly;
		final int queueIndex = tempQueue.indexOf( polygon );

		/**
		 * Walk through the temporary queue to test this polygon with all others
		 * still in that queue.
		 */
		for ( int i = 0 ; i < tempQueue.size() ; i++ )
		{
			/**
			 * Check if there is a polygon at this place in the queue. If there
			 * is only null the polygon at that location has already been sorted
			 * into the final queue.
			 */
			final Object object = tempQueue.get( i );
			if ( ( object != null ) && ( object != polygon ) )
			{
				final RenderedPolygon other = (RenderedPolygon)object;

//				out.write( "Comparing with polygon " + other._name + " --- " );
				/**
				 * Comparing the polygons for an overlap in the z direction, x
				 * direction or y direction. If there is no overlap, there is no
				 * need for any sorting and the next polygon can be tested.
				 */
				if ( other._minZ < polygon._maxZ )
				{
					if ( other._minX < polygon._maxX && other._maxX > polygon._minX )
					{
						if ( other._minY < polygon._maxY && other._maxY > polygon._minY )
						{
							/**
							 * If there is an overlap, the two polygons are
							 * compared to eachother.
							 */
							int relation = -1;
							int compared = compare( polygon , other );

							if ( compared == IN_FRONT )
							{
								relation = IN_FRONT;
							}
							else if ( compared == INTERSECTING )
							{
//								out.write( " intersection between the polygons, comparing again --- " );
								/**
								 * If there is an intersection, the polygons need to be
								 * tested again, but now swapped. See javadoc for compare
								 * for an expanation.
								 */
								compared = compare( other , polygon );

								if ( compared == BEHIND )
								{
									relation = IN_FRONT;
								}
								else if ( compared == INTERSECTING )
								{
									relation = INTERSECTING;
								}
							}


							if ( relation == IN_FRONT )
							{
								/**
								 * If the polygon is in front of the other polygon,
								 * the other needs to be sorted first. This polygon
								 * is added to the stack, and removed from the stack
								 * once sorting is complete.
								 */
								if ( polygonStack.contains( other ) )
								{
//									out.writeln( "this polygon is in front of the other, but " + other._name + " is already on the stack." );
								}
								else
								{
//									out.writeln( "this polygon is in front of the other. Sorting " + other._name + " first " );
//									out.writeln( "{" );
//									out.indentIn();
									polygonStack.add( polygon );
									order( other , tempQueue , finalQueue , polygonStack , out );
									polygonStack.remove( polygon );
//									out.indentOut();
//									out.writeln( "}" );
								}
							}

							/**
							 * If the two polygons intersect, the polygon is clipped.
							 * The part furthest away from the camera is used to continue
							 * sorting, the other part is added to the temporary queue
							 * (using insertion sort)
							 */
							if ( relation == INTERSECTING )
							{
								final RenderedPolygon[] clipped = clip( polygon , other );
								final RenderedPolygon clip;

								if ( clipped[ 0 ]._minZ == polygon._minZ )
								{
									polygon = clipped[ 0 ];
									clip = clipped[ 1 ];
								}
								else
								{
									polygon = clipped[ 1 ];
									clip = clipped[ 0 ];
								}

								final double z = clip._minZ;
								boolean stop = false;
								int pointer;
								for ( pointer = 0 ; pointer < tempQueue.size() && !stop ; pointer++ )
								{
									if ( tempQueue.get( pointer ) != null )
									{
										final RenderedPolygon rp = (RenderedPolygon)tempQueue.get( pointer );
										if ( rp._minZ > z )
										{
											tempQueue.add( pointer , clip );
											stop = true;
										}
									}
								}

								if ( !stop )
								{
									tempQueue.add( tempQueue.size() , clip );
								}

//								out.writeln( "clipping polygons. This is now " + polygon._name );
							}
							else
							{
//								out.writeln( "this polygon is behind or parallel to the other, continuing sorting" );
							}

						}
						else
						{
//							out.writeln( "no overlap in y. ( Polygon minY " + polygon._minY + " , maxY " + polygon._maxY+ " ) ( Other minY " + other._minY+ " , maxY " + other._maxY+ " )" );
						}
					}
					else
					{
//						out.writeln( "no overlap in x. ( Polygon minX " + polygon._minX + " , maxX " + polygon._maxX+ " ) ( Other minX " + other._minX+ " , maxX " + other._maxX+ " )" );
					}
				}
				else
				{
//					out.writeln( "no overlap in z. ( Polygon minZ " + polygon._minZ + " , maxZ " + polygon._maxZ+ " ) ( Other minZ " + other._minZ+ " , maxZ " + other._maxZ+ " )" );
				}
			}

		}
		/**
		 * Finally, after the polygon has been tested against all other polygons
		 * in the temporary queue, it is safe to add it to the final queue.
		 */
//		out.writeln( "done sorting, adding polygon." );
		finalQueue.add( polygon );
		tempQueue.set( queueIndex , null );
	}

	/**
	 * Compares two {@link RenderedPolygon}s with eachother. The result is one
	 * of {@link #IN_FRONT}, {@link #BEHIND}, {@link #INTERSECTING} or
	 * {@link #COPLANAR}, indicating where <code>polygon</code> is in relation
	 * to <code>other</code>. Please note that {@link #INTERSECTING} does not
	 * necessarily mean both polygons intersect, it simply means that the
	 * vertices of <code>polygon</code> are on both sides of a plane parallel to
	 * <code>other</code>. To test wether they really intersect, it is necessary
	 * to compare the two again, but now with <code>polygon</code> and
	 * <code>other</code> interchanged.
	 *
	 * @param   polygon     {@link RenderedPolygon} to test
	 * @param   other       {@link RenderedPolygon} to test against.
	 *
	 * @return  Where <code>polygon</code> is in relation to <code>other</code>.
	 *          One of {@link #IN_FRONT}, {@link #BEHIND}, {@link #INTERSECTING}
	 *          or {@link #COPLANAR}.
	 *
	 * @see     #IN_FRONT
	 * @see     #BEHIND
	 * @see     #INTERSECTING
	 * @see     #COPLANAR
	 */
	private static int compare( final RenderedPolygon polygon , final RenderedPolygon other )
	{
		boolean  behind      = false;
		boolean  inFront     = false;
		final double   d           = other._planeConstant;
		final int      vertexCount = polygon._pointCount;
		final double[] xCoords     = polygon._viewX;
		final double[] yCoords     = polygon._viewY;
		final double[] zCoords     = polygon._viewZ;

		for ( int vertex = 0 ; !( behind && inFront ) && ( vertex < vertexCount ) ; vertex++ )
		{
			/**
			 * The plane constant of a plane is the result of the equation
			 * N . P = x, where N is the plane normal and P is any point on the
			 * plane. Substituting P for another point in the 3d world, for
			 * example a vertex of another polygon, can indicate wether this
			 * vertex is in front ( x < constant) or behind ( x > constant) the
			 * plane.
			 */
			final double x = xCoords[ vertex ];
			final double y = yCoords[ vertex ];
			final double z = zCoords[ vertex ];
			final double dot = Vector3D.dot( other._planeNormalX , other._planeNormalY , other._planeNormalZ , x , y , z );

			behind  = behind  || ( d > ( dot + 0.001 ) );
			inFront = inFront || ( d < ( dot - 0.001 ) );
		}

		return behind
		       ? inFront ? INTERSECTING : BEHIND
		       : inFront ? IN_FRONT : COPLANAR;
	}

	/**
	 * Clips {@link RenderedPolygon} <code>polygon</code> into two polygons,
	 * clipping at the plane of <code>cuttingPlane</code>. The result is an
	 * array containing the two new {@link RenderedPolygon}s.
	 *
	 * @param polygon       {@link RenderedPolygon} to clip.
	 * @param cuttingPlane  {@link RenderedPolygon} to clip at.
	 *
	 * @return  An array containing the two new {@link RenderedPolygon}s.
	 */
	private RenderedPolygon[] clip( final RenderedPolygon polygon , final RenderedPolygon cuttingPlane )
	{
		/**
		 * Setup variables to be used.
		 */
		final double   cuttingD    = cuttingPlane._planeConstant;
		final int      vertexCount = polygon._pointCount;
		final double[] xCoords     = polygon._viewX;
		final double[] yCoords     = polygon._viewY;
		final double[] zCoords     = polygon._viewZ;
		final int[]    projectedX  = polygon._projectedX;
		final int[]    projectedY  = polygon._projectedY;

		int frontCount = 0;
		int backCount = 0;
		final double[] frontX = _frontX = (double[])ArrayTools.ensureLength( _frontX , double.class , 1 , vertexCount + 2 );
		final double[] frontY = _frontY = (double[])ArrayTools.ensureLength( _frontY , double.class , 1 , vertexCount + 2 );
		final double[] frontZ = _frontZ = (double[])ArrayTools.ensureLength( _frontZ , double.class , 1 , vertexCount + 2 );
		final double[]  backX =  _backX = (double[])ArrayTools.ensureLength(  _backX , double.class , 1 , vertexCount + 2 );
		final double[]  backY =  _backY = (double[])ArrayTools.ensureLength(  _backY , double.class , 1 , vertexCount + 2 );
		final double[]  backZ =  _backZ = (double[])ArrayTools.ensureLength(  _backZ , double.class , 1 , vertexCount + 2 );

		final int[] frontProjX = _frontProjX = (int[])ArrayTools.ensureLength( _frontProjX , int.class , 1 , vertexCount + 2 );
		final int[] frontProjY = _frontProjY = (int[])ArrayTools.ensureLength( _frontProjY , int.class , 1 , vertexCount + 2 );
		final int[]  backProjX =  _backProjX = (int[])ArrayTools.ensureLength(  _backProjX , int.class , 1 , vertexCount + 2 );
		final int[]  backProjY =  _backProjY = (int[])ArrayTools.ensureLength(  _backProjY , int.class , 1 , vertexCount + 2 );

		final double cuttingNX = cuttingPlane._planeNormalX;
		final double cuttingNY = cuttingPlane._planeNormalY;
		final double cuttingNZ = cuttingPlane._planeNormalZ;

		final int lastIndex = vertexCount - 1;
		double lastX     =    xCoords[ lastIndex ];
		double lastY     =    yCoords[ lastIndex ];
		double lastZ     =    zCoords[ lastIndex ];
		int    lastProjX = projectedX[ lastIndex ];
		int    lastProjY = projectedY[ lastIndex ];
		double lastDot   = Vector3D.dot( cuttingNX , cuttingNY , cuttingNZ , lastX , lastY , lastZ );

		/**
		 * Iterate through all vertices of the plane
		 */
		for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
		{
			/**
			 * The plane constant of a plane is the result of the equation
			 * N . P = x, where N is the plane normal and P is any point on the
			 * plane. Substituting P for another point in the 3d world, for
			 * example a vertex of another polygon, can indicate wether this
			 * vertex is in front ( x < constant) or behind ( x > constant) the
			 * plane.
			 */
			final double x     =    xCoords[ vertex ];
			final double y     =    yCoords[ vertex ];
			final double z     =    zCoords[ vertex ];
			final int    projX = projectedX[ vertex ];
			final int    projY = projectedY[ vertex ];
			final double dot   = Vector3D.dot( cuttingNX , cuttingNY , cuttingNZ , x , y , z );

			/**
			 * If this vertex is on one side of the plane and the previous
			 * vertex on the other, the intersection point must be calculated
			 * and added to both sides.
			 */
			if ( ( dot > cuttingD && lastDot < cuttingD )
			  || ( dot < cuttingD && lastDot > cuttingD ) )
			{
				/**
				 * For an explanation of this piece of math, read: http://astronomy.swin.edu.au/~pbourke/geometry/planeline/
				 */
				final double u = ( ( cuttingNX *   lastX )     + ( cuttingNY *   lastY )     + ( cuttingNZ *   lastZ )     - cuttingD )
				                                                        /
				                   ( cuttingNX * ( lastX - x ) +   cuttingNY * ( lastY - y ) +   cuttingNZ * ( lastZ - z ) );

				if ( u < -0.001 || u > 1.001 )
				{
					System.err.println( "cuttingNormal: " + Vector3D.toFriendlyString( Vector3D.INIT.set( cuttingNX , cuttingNY , cuttingNZ ) ) + " - cuttingD: " + cuttingD );
					System.err.println( "Last: coordinates: (" + (int)lastX + ',' + (int)lastY + ',' + (int)lastZ + ") - dot: " + lastDot );
					System.err.println( "Current: coordinates: (" + (int)x + ',' + (int)y + ',' + (int)z + ")  - dot: " + dot );

					throw new IllegalStateException( "Error! Trying to cut a line that does not intersect the cutting plane (u=" + u ); //+ " , u1=" + u1 + ')' );
				}

				final double cutX     = lastX + u * ( x - lastX );
				final double cutY     = lastY + u * ( y - lastY );
				final double cutZ     = lastZ + u * ( z - lastZ );
				final int    cutProjX = (int)( (double)lastProjX + u * (double)( projX - lastProjX ) );
				final int    cutProjY = (int)( (double)lastProjY + u * (double)( projY - lastProjY ) );

				frontX[ frontCount ]     = cutX;
				frontY[ frontCount ]     = cutY;
				frontZ[ frontCount ]     = cutZ;
				frontProjX[ frontCount ] = cutProjX;
				frontProjY[ frontCount ] = cutProjY;
				frontCount++;

				backX[ backCount ]     = cutX;
				backY[ backCount ]     = cutY;
				backZ[ backCount ]     = cutZ;
				backProjX[ backCount ] = cutProjX;
				backProjY[ backCount ] = cutProjY;
				backCount++;
			}
			/**
			 * If the point is on the front side, add it to the front.
			 */
			if ( dot >= cuttingD )
			{
				frontX[ frontCount ]     = x;
				frontY[ frontCount ]     = y;
				frontZ[ frontCount ]     = z;
				frontProjX[ frontCount ] = projX;
				frontProjY[ frontCount ] = projY;
				frontCount++;
			}
			/**
			 * Same thing for the back side.
			 */
			if ( dot <= cuttingD )
			{
				backX[ backCount ]     = x;
				backY[ backCount ]     = y;
				backZ[ backCount ]     = z;
				backProjX[ backCount ] = projX;
				backProjY[ backCount ] = projY;
				backCount++;
			}

			lastDot   = dot;
			lastX     = x;
			lastY     = y;
			lastZ     = z;
			lastProjX = projX;
			lastProjY = projY;
		}

		/**
		 * Finally create the two new polygons.
		 */
//		System.out.println( "Frontcount: " + frontVertexCount + "  - backcount: " + backVertexCount );
		final RenderedPolygon front = new RenderedPolygon( frontCount );
		double minZ = Double.MAX_VALUE;
		double maxZ = Double.MIN_VALUE;
		int    minX = Integer.MAX_VALUE;
		int    maxX = Integer.MIN_VALUE;
		int    minY = Integer.MAX_VALUE;
		int    maxY = Integer.MIN_VALUE;
		for ( int i = 0 ; i < frontCount ; i++ )
		{
			front._viewX[ i ] = frontX[ i ];
			front._viewY[ i ] = frontY[ i ];
			front._viewZ[ i ] = frontZ[ i ];

			final int projX = frontProjX[ i ];
			final int projY = frontProjY[ i ];
			front._projectedX[ i ] = projX;
			front._projectedY[ i ] = projY;

			final double vertexZ = frontZ[ i ];
			minZ = vertexZ < minZ ? vertexZ : minZ;
			maxZ = vertexZ > maxZ ? vertexZ : maxZ;
			minX = projX   < minX ? projX   : minX;
			maxX = projX   > maxX ? projX   : maxX;
			minY = projY   < minY ? projY   : minY;
			maxY = projY   > maxY ? projY   : maxY;
		}
		front._minZ                = minZ;
		front._maxZ                = maxZ;
		front._minX                = minX;
		front._maxX                = maxX;
		front._minY                = minY;
		front._maxY                = maxY;
		front._alternateAppearance = polygon._alternateAppearance;
		front._object              = polygon._object;
		front._planeConstant       = polygon._planeConstant;
		front._planeNormalX        = polygon._planeNormalX;
		front._planeNormalY        = polygon._planeNormalY;
		front._planeNormalZ        = polygon._planeNormalZ;
		front._texture             = polygon._texture;
		front._name                = polygon._name + "_front";

		final RenderedPolygon back = new RenderedPolygon( backCount );
		minZ = Double.MAX_VALUE;
		maxZ = Double.MIN_VALUE;
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		for ( int i = 0 ; i < backCount ; i++ )
		{
			back._viewX[ i ] = backX[ i ];
			back._viewY[ i ] = backY[ i ];
			back._viewZ[ i ] = backZ[ i ];

			final int projX = backProjX[ i ];
			final int projY = backProjY[ i ];
			back._projectedX[ i ] = projX;
			back._projectedY[ i ] = projY;

			final double vertexZ = backZ[ i ];
			minZ = vertexZ < minZ ? vertexZ : minZ;
			maxZ = vertexZ > maxZ ? vertexZ : maxZ;
			minX = projX   < minX ? projX   : minX;
			maxX = projX   > maxX ? projX   : maxX;
			minY = projY   < minY ? projY   : minY;
			maxY = projY   > maxY ? projY   : maxY;
		}
		back._minZ                = minZ;
		back._maxZ                = maxZ;
		back._minX                = minX;
		back._maxX                = maxX;
		back._minY                = minY;
		back._maxY                = maxY;
		back._alternateAppearance = polygon._alternateAppearance;
		back._object              = polygon._object;
		back._planeConstant       = polygon._planeConstant;
		back._planeNormalX        = polygon._planeNormalX;
		back._planeNormalY        = polygon._planeNormalY;
		back._planeNormalZ        = polygon._planeNormalZ;
		back._texture             = polygon._texture;
		back._name                = polygon._name + "_back";

		return new RenderedPolygon[] { back , front };
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
