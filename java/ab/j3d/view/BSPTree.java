/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This class manages a Binary Space Partitioning Tree that can be used by a 3D render engine.
 * <p />
 * Following docs are used for algorithm:
 * <ul>
 *   <li>BSP FAQ (Original): http://www.faqs.org/faqs/graphics/bsptree-faq/ </li>
 *   <li>BSP FAQ (Nicely formatted): http://www.xs4all.nl/~smit/whole.htm <li>
 * </ul>
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class BSPTree
{
	private class BSPTreeNode
	{
		private RenderedPolygon       _partitionPlane;
		private List<RenderedPolygon> _nodePolygons;
		private BSPTreeNode           _front;
		private BSPTreeNode           _back;

		private BSPTreeNode()
		{
			_partitionPlane = null;
			_nodePolygons   = new ArrayList<RenderedPolygon>();
			_front          = null;
			_back           = null;
		}

		public void addPolygon( final RenderedPolygon polygon )
		{
			_nodePolygons.add( polygon );
		}

		public List<RenderedPolygon> getPolygons()
		{
			return _nodePolygons;
		}

		public void setPartitionPlane( final RenderedPolygon plane )
		{
			if ( _partitionPlane == null )
			{
				_partitionPlane = plane;
			}
			else
			{
				throw new IllegalStateException( "Partition plane for this node was already set." );
			}
		}

		public RenderedPolygon getPartitionPlane()
		{
			return _partitionPlane;
		}

		public void setFront( final BSPTreeNode front )
		{
			if ( _front == null )
			{
				_front = front;
			}
			else
			{
				throw new IllegalStateException( "Front tree node for this node was already set." );
			}
		}

		public BSPTreeNode getFront()
		{
			return _front;
		}

		public void setBack( final BSPTreeNode back )
		{
			if ( _back == null )
			{
				_back = back;
			}
			else
			{
				throw new IllegalStateException( "Back tree node for this node was already set." );
			}
		}

		public BSPTreeNode getBack()
		{
			return _back;
		}
	}

	/**
	 * Root of the tree.
	 */
	private BSPTreeNode _root;

	/**
	 * List containing polygons to create tree from.
	 */
	private List<RenderedPolygon> _polygons;

	/**
	 * Used to split polygons.
	 *
	 * @see RenderQueue#clip
	 */
	private RenderQueue _renderQueue;

	/**
	 * Construct a new Binary Space Partitioning Tree.
	 */
	public BSPTree()
	{
		_root = null;
		_polygons = null;
		_renderQueue = new RenderQueue();

		reset();
	}

	/**
	 * Reset/clear the tree.
	 */
	public void reset()
	{
		_root = new BSPTreeNode();
		_polygons = new ArrayList<RenderedPolygon>();
	}

	/**
	 * Add a scene to the tree ( Note: the tree is not rebuild! ).
	 *
	 * @param   nodes   Nodes that specify the scene (only {@link Object3D} objects are used).
	 */
	public void addScene( final Node3DCollection<Node3D> nodes )
	{
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Node3D   object       = nodes.getNode( i );
			final Matrix3D object2world = nodes.getMatrix( i );

			if ( object instanceof Object3D )
			{
				addObject3D( (Object3D)object , object2world );
			}
		}
	}

	/**
	 * Add a {@link Object3D} to the tree ( Note: the tree is not rebuild! ).
	 *
	 * @param   object          Object to add.
	 * @param   object2model    Transformation from object to model coordinates.
	 */
	public void addObject3D( final Object3D object , final Matrix3D object2model )
	{
		final int faceCount = object.getFaceCount();

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			addPolygon( object2model , object , i , false );
		}
	}

	/**
	 * Add a polygon to the tree ( Note: the tree is not rebuild! ).
	 *
	 * @param   object2model            Transforms object to model coordinates.
	 * @param   object                  Object to get face from.
	 * @param   faceIndex               Index of object's face.
	 * @param   alternateAppearance     Use alternate vs. regular object appearance.
	 */
	public void addPolygon( final Matrix3D object2model , final Object3D object , final int faceIndex , final boolean alternateAppearance )
	{
		final Face3D face = object.getFace( faceIndex );
		final RenderedPolygon polygon = new RenderedPolygon( face.getVertexCount() );
		polygon.initialize( object2model , null , object , face , alternateAppearance );
		_polygons.add( polygon );
	}

	/**
	 * Get polygons to render in the specified order ('back-to-front' or 'front-to-back').
	 *
	 * @param   viewPoint           Point from where the view is rendered.
	 * @param   projector           Projector used to e.g. check polygons against view volume.
	 * @param   model2view          Transformation from model to view coordinates.
	 * @param   backfaceCulling     Prevent backfaces from being rendered.
	 * @param   backToFront         Should the polygons be ordered 'back-to-front' or 'front-to-back'.
	 *
	 * @return  Array filled with {@link RenderedPolygon} objects in specified paint order.
	 */
	public RenderedPolygon[] getRenderQueue( final Vector3D viewPoint , final Projector projector , final Matrix3D model2view , final boolean backfaceCulling , final boolean backToFront )
	{
		final List<RenderedPolygon> queue = new ArrayList<RenderedPolygon>();
		getSortedPolygons( viewPoint , _root , queue , backToFront );

		final List<RenderedPolygon> result = new ArrayList<RenderedPolygon>();
		for ( final RenderedPolygon polygon : queue )
		{
			final RenderedPolygon renderedPolygon = getRenderedPolygon( polygon , model2view , projector , backfaceCulling );
			if ( renderedPolygon != null )
			{
				result.add( renderedPolygon );
			}
		}

		return result.toArray( new RenderedPolygon[ result.size() ] );
	}

	/**
	 * Create a {@link RenderedPolygon} by using another one.
	 * <p />
	 * The vertices are translated by using the specified {@link Matrix3D},
	 * and projected by using the specified {@link Projector}.
	 *
	 * @param   polygon             Source polygon (that is in BSP tree).
	 * @param   model2view          Transformation from model to view coordinates.
	 * @param   projector           Projector used to e.g. check polygons against view volume.
	 * @param   backfaceCulling     Prevent backfaces from being rendered.
	 *
	 * @return  The created rendered polygon.
	 */
	private static RenderedPolygon getRenderedPolygon( final RenderedPolygon polygon , final Matrix3D model2view , final Projector projector , final boolean backfaceCulling )
	{
		double x;
		double y;
		double z;

		RenderedPolygon result = null;

		final int      vertexCount          = polygon._vertexCount;
		final double[] viewCoordinates      = new double[ vertexCount * 3 ];
		final int[]    projectedCoordinates = new int[ vertexCount * 2 ];
		final Vector3D viewNormal           = model2view.transform( Vector3D.INIT.set( polygon._planeNormalX , polygon._planeNormalY , polygon._planeNormalZ ) );

		for ( int j = 0 ; j < vertexCount ; j++ )
		{
			x = polygon._viewX[ j ];
			y = polygon._viewY[ j ];
			z = polygon._viewZ[ j ];

			final Vector3D translated = model2view.transform( Vector3D.INIT.set( x , y , z ) );

			viewCoordinates[ j * 3     ] = translated.x;
			viewCoordinates[ j * 3 + 1 ] = translated.y;
			viewCoordinates[ j * 3 + 2 ] = translated.z;
		}
		projector.project( viewCoordinates , projectedCoordinates , vertexCount );

		if ( !projector.outsideViewVolume( viewCoordinates ) )
		{
			result = new RenderedPolygon( vertexCount );

			int    minX = Integer.MAX_VALUE;
			int    maxX = Integer.MIN_VALUE;
			int    minY = Integer.MAX_VALUE;
			int    maxY = Integer.MIN_VALUE;
			double minZ = Double.POSITIVE_INFINITY;
			double maxZ = Double.NEGATIVE_INFINITY;

			for ( int i = 0 ; i < vertexCount ; i++ )
			{
				final int index1 = i * 2;
				final int projX = projectedCoordinates[ index1     ];
				final int projY = projectedCoordinates[ index1 + 1 ];
				result._projectedX[ i ] = projX;
				result._projectedY[ i ] = projY;

				final int index2 = i * 3;
				result._viewX[ i ] = viewCoordinates[ index2     ];
				result._viewY[ i ] = viewCoordinates[ index2 + 1 ];
				result._viewZ[ i ] = viewCoordinates[ index2 + 2 ];
				z = viewCoordinates[ index2 + 2 ];

				minX = projX < minX ? projX : minX;
				maxX = projX > maxX ? projX : maxX;
				minY = projY < minY ? projY : minY;
				maxY = projY > maxY ? projY : maxY;
				minZ = z     < minZ ? z     : minZ;
				maxZ = z     > maxZ ? z     : maxZ;
			}

			result._minImageX = minX;
			result._maxImageX = maxX;
			result._minImageY = minY;
			result._maxImageY = maxY;
			result._minViewZ = minZ;
			result._maxViewZ = maxZ;

			final double x0 = result._viewX[ 0 ];
			final double y0 = result._viewY[ 0 ];
			final double z0 = result._viewZ[ 0 ];

			final double  planeNormalX  = viewNormal.x;
			final double  planeNormalY  = viewNormal.y;
			final double  planeNormalZ  = viewNormal.z;
			final double  planeConstant = planeNormalX * x0 + planeNormalY * y0 + planeNormalZ * z0;
			final boolean backface      = ( projector instanceof Projector.PerspectiveProjector ) ? ( planeConstant <= 0.0 ) : ( planeNormalZ <= 0.0 );

			result._object              = polygon._object;
			result._planeNormalX        = planeNormalX;
			result._planeNormalY        = planeNormalY;
			result._planeNormalZ        = planeNormalZ;
			result._planeConstant       = planeConstant;
			result._backface            = backface;
			result._material            = polygon._material;
			result._alternateAppearance = polygon._alternateAppearance;
		}

		// Perform backface culling.
		if ( result != null && result.isBackface() && backfaceCulling )
			result = null;

		return result;
	}

	/**
	 * Build up the BSP tree from the currently specified polygons. If there
	 * are no polygons specified yet, the tree is not build.
	 */
	public void build()
	{
		if ( !_polygons.isEmpty() )
		{
			build( _root , _polygons );
		}
	}

	/**
	 * Build up a {@link BSPTree} from the specified list of polygons.
	 * <p />
	 * The algorithm to build a BSP tree is very simple:
	 * <ol>
	 *   <li>Select a partition plane.</li>
	 *   <li>Partition the set of polygons with the plane.</li>
	 *   <li>Recurse with each of the two new sets.</li>
	 * </ol>
	 *
	 * This method should be further improved:
	 * <ul>
	 *   <li>The number of polygons that is splitted should be decreased
	 *       by selecting a better partition plane. </li>
	 *   <li>Try to create a more balanced tree.</li>
	 * </ul>
	 *
	 * @param   root        Root node of the tree.
	 * @param   polygons    List of polygons to build up tree from.
	 */
	private void build( final BSPTreeNode root , final List<RenderedPolygon> polygons )
	{
		final RenderedPolygon       partitionPolygon = getPartitionPlane( polygons );
		final RenderedPolygon       partitionPlane   = partitionPolygon;
		final List<RenderedPolygon> frontList        = new ArrayList<RenderedPolygon>();
		final List<RenderedPolygon> backList         = new ArrayList<RenderedPolygon>();

		root.setPartitionPlane( partitionPlane );
		root.addPolygon( partitionPolygon );

		for ( final RenderedPolygon poly : polygons )
		{
			if ( poly != partitionPolygon )
			{
				switch ( RenderQueue.compare( poly , partitionPlane ) )
				{
					case RenderQueue.COPLANAR:
					{
						root.addPolygon( poly );
						break;
					}

					case RenderQueue.BEHIND:
					{
						backList.add( poly );
						break;
					}

					case RenderQueue.IN_FRONT:
					{
						frontList.add( poly );
						break;
					}

					case RenderQueue.INTERSECTING:
					{
						final RenderedPolygon[] splitted = _renderQueue.clip( poly , partitionPlane );
						backList.add( splitted[ 0 ] );
						frontList.add( splitted[ 1 ] );
						break;
					}
				}
			}
		}

		if ( !frontList.isEmpty() )
		{
			final BSPTreeNode frontTree = new BSPTreeNode();
			root.setFront( frontTree );

			build( frontTree , frontList );
		}

		if ( !backList.isEmpty() )
		{
			final BSPTreeNode backTree = new BSPTreeNode();
			root.setBack( backTree );

			build( backTree , backList );
		}
	}

	/**
	 * Method that tries to determine the "best" polygon that can be used as
	 * partition plane from a list of polygons.
	 * <p />
	 * The polygon that potentially has the greatest surface, will be returned.
	 *
	 * @param   polygons    Polygons to choose from (not <code>null</code>).
	 *
	 * @return  Determined polygon;
	 *          <code>null</code> if specified polygon list is empty.
	 */
	private static RenderedPolygon getPartitionPlane( final List<RenderedPolygon> polygons )
	{
		final RenderedPolygon result;

		if ( polygons.isEmpty() )
		{
			result = null;
		}
		else if ( polygons.size() == 1 )
		{
			result = polygons.get( 0 );
		}
		else
		{
			RenderedPolygon potential     = polygons.get( 0 );
			double          potentialSize = potential.getEstimatedSurfaceAreaFactor();

			for ( int i = 1 ; i < polygons.size() ; i++ )
			{
				final RenderedPolygon polygon = polygons.get( i );
				final double          size    = polygon.getEstimatedSurfaceAreaFactor();

				if ( size > potentialSize )
				{
					potentialSize = size;
					potential = polygon;
				}
			}

			result = potential;
		}

		return result;
	}

	/**
	 * Sort polygons in the specified order ('back-to-front' or 'front-to-back').
	 *
	 * @param   viewPoint       Point from where the view is rendered.
	 * @param   root            Start node in tree.
	 * @param   result          Result list.
	 * @param   backToFront     Should the polygons be ordered 'back-to-front' or 'front-to-back'.
	 */
	private static void getSortedPolygons( final Vector3D viewPoint , final BSPTreeNode root , final List<RenderedPolygon> result , final boolean backToFront )
	{
		if ( root != null )
		{
			final RenderedPolygon       partitionPlane = root.getPartitionPlane();
			final BSPTreeNode           front          = backToFront ? root.getFront() : root.getBack();
			final BSPTreeNode           back           = backToFront ? root.getBack() : root.getFront();
			final List<RenderedPolygon> polygons       = root.getPolygons();

			if ( front == null && back == null )
			{
				result.addAll( polygons );
			}
			else
			{
				switch ( RenderQueue.compare( partitionPlane , viewPoint ) )
				{
					case RenderQueue.IN_FRONT :
					{
						getSortedPolygons( viewPoint , back  , result , backToFront );
						result.addAll( polygons );
						getSortedPolygons( viewPoint , front , result , backToFront );
						break;
					}

					case RenderQueue.BEHIND :
					{
						getSortedPolygons( viewPoint , front , result , backToFront );
						result.addAll( polygons );
						getSortedPolygons( viewPoint , back  , result , backToFront );
						break;
					}

					case RenderQueue.COPLANAR :
					{
						getSortedPolygons( viewPoint , back  , result , backToFront );
						getSortedPolygons( viewPoint , front , result , backToFront );
						break;
					}
				}
			}
		}
	}
}
