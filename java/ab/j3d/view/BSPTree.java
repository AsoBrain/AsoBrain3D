/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2006-2006 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
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
		private RenderedPolygon _partitionPlane;
		private List            _nodePolygons;
		private BSPTreeNode     _front;
		private BSPTreeNode     _back;

		private BSPTreeNode()
		{
			_partitionPlane = null;
			_nodePolygons   = new ArrayList();
			_front          = null;
			_back           = null;
		}

		public void addPolygon( final RenderedPolygon polygon )
		{
			_nodePolygons.add( polygon );
		}

		public List getPolygons()
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
	 * List containing {@link RenderedPolygon} objects to create tree from.
	 */
	private List _polygons;

	/**
	 * Boolean indicating if the tree is build.
	 */
	private boolean _isBuild;

	/**
	 * Used to split polygons.
	 *
	 * @see RenderQueue#clip
	 */
	private RenderQueue _renderQueue = new RenderQueue();

	/**
	 * Temporary/shared storage area for {@link #addObject3D}.
	 */
	private double[] _tmpVertexCoordinates;

	/**
	 * Temporary/shared storage area for {@link #addObject3D}.
	 */
	private double[] _tmpFaceNormals;

	/**
	 * Construct a new Binary Space Partitioning Tree.
	 */
	public BSPTree()
	{
		reset();
	}

	/**
	 * Reset/clear the tree.
	 */
	public void reset()
	{
		_root                 = new BSPTreeNode();
		_polygons             = new ArrayList();
		_isBuild              = false;
		_tmpVertexCoordinates = null;
		_tmpFaceNormals       = null;
	}

	/**
	 * Check if the tree is build.
	 *
	 * @return  <code>true</code> if the tree is build;
	 *          <code>false</code> otherwise.
	 */
	public boolean isBuild()
	{
		return _isBuild;
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
			final Face3D   face              = object.getFace( i );
			final double[] vertexCoordinates = ( _tmpVertexCoordinates = object.getVertexCoordinates( object2model ,_tmpVertexCoordinates ) );
			final double[] faceNormals       = ( _tmpFaceNormals = object.getFaceNormals( object2model , _tmpFaceNormals ) );

			addPolygon( face , vertexCoordinates , faceNormals , false );
		}
	}

	/**
	 * Add a polygon to the tree ( Note: the tree is not rebuild! ).
	 *
	 * @param   face                    Face to get polygon properties from.
	 * @param   objectModelCoordinates  Vertex coordinates of object in model space.
	 * @param   faceModelNormals        Normals of object faces in model space.
	 * @param   alternateAppearance     Use alternate vs. regular object appearance.
	 */
	public void addPolygon( final Face3D face , final double[] objectModelCoordinates , final double[] faceModelNormals , final boolean alternateAppearance )
	{
		final Object3D object                    = face.getObject();
		final int[]    dummyProjectedCoordinates = new int[ object.getVertexCount() * 2 ];

		final RenderedPolygon polygon = new RenderedPolygon( face.getVertexCount() );
		polygon.initialize( face , objectModelCoordinates , dummyProjectedCoordinates , faceModelNormals , alternateAppearance );

		_polygons.add( polygon );
	}

	/**
	 * Get polygons to render in the specified order ('back-to-front' or 'front-to-back').
	 *
	 * @param   eyePoint        Eye point in model coordinates.
	 * @param   projector       Projector used to e.g. check polygons against view volume.
	 * @param   model2view      Transformation from model to view coordinates.
	 * @param   backToFront     Should the polygons be ordered 'back-to-front' or 'front-to-back'.
	 *
	 * @return  Array filled with {@link RenderedPolygon} objects in specified paint order.
	 */
	public RenderedPolygon[] getRenderQueue( final Vector3D eyePoint , final Projector projector , final Matrix3D model2view , final boolean backToFront )
	{
		final ArrayList queue = new ArrayList();
		getSortedPolygons( eyePoint , _root , queue , backToFront );

		final List result = new ArrayList();
		for ( int i = 0 ; i < queue.size() ; i++ )
		{
			final RenderedPolygon  polygon         = (RenderedPolygon)queue.get( i );
			final RenderedPolygon  renderedPolygon = getRenderedPolygon( polygon , model2view , projector );

			if ( renderedPolygon != null )
			{
				result.add( renderedPolygon );
			}
		}

		return (RenderedPolygon[])result.toArray( new RenderedPolygon[ result.size() ] );
	}

	/**
	 * Create a {@link RenderedPolygon} by using another one.
	 * <p />
	 * The vertices are translated by using the specified {@link Matrix3D},
	 * and projected by using the specified {@link Projector}.
	 *
	 * @param   polygon     Source polygon (that is in BSP tree).
	 * @param   model2view  Transformation from model to view coordinates.
	 * @param   projector   Projector used to e.g. check polygons against view volume.
	 *
	 * @return  The created rendered polygon.
	 */
	private static RenderedPolygon getRenderedPolygon( final RenderedPolygon polygon , final Matrix3D model2view , final Projector projector )
	{
		double x;
		double y;
		double z;

		RenderedPolygon result = null;

		final int      vertexCount          = polygon._vertexCount;
		final double[] viewCoordinates      = new double[ vertexCount * 3 ];
		final int[]    projectedCoordinates = new int[ vertexCount * 2 ];
		final Vector3D viewNormal           = model2view.multiply( Vector3D.INIT.set( polygon._planeNormalX , polygon._planeNormalY , polygon._planeNormalZ ) );

		for ( int j = 0 ; j < vertexCount ; j++ )
		{
			x = polygon._viewX[ j ];
			y = polygon._viewY[ j ];
			z = polygon._viewZ[ j ];

			final Vector3D translated = model2view.multiply( Vector3D.INIT.set( x, y, z ) );

			viewCoordinates[ j * 3     ] = translated.x;
			viewCoordinates[ j * 3 + 1 ] = translated.y;
			viewCoordinates[ j * 3 + 2 ] = translated.z;
		}
		projector.project( viewCoordinates , projectedCoordinates , vertexCount );

		if ( !projector.outsideViewVolume( viewCoordinates ) ) // @TODO Also add backface culling test??
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

			result._minX = minX;
			result._maxX = maxX;
			result._minY = minY;
			result._maxY = maxY;
			result._minZ = minZ;
			result._maxZ = maxZ;

			final double x0 = result._viewX[ 0 ];
			final double y0 = result._viewY[ 0 ];
			final double z0 = result._viewZ[ 0 ];

			final double planeNormalX = viewNormal.x;
			final double planeNormalY = viewNormal.y;
			final double planeNormalZ = viewNormal.z;
			final double planeConstant = planeNormalX * x0 + planeNormalY * y0 + planeNormalZ * z0;

			result._object              = polygon._object;
			result._planeNormalX        = planeNormalX;
			result._planeNormalY        = planeNormalY;
			result._planeNormalZ        = planeNormalZ;
			result._planeConstant       = planeConstant;
			result._texture             = polygon._texture;
			result._alternateAppearance = polygon._alternateAppearance;
		}

		return result;
	}

	/**
	 * Build up the BSP tree from the currently specified polygons. If there
	 * are no polygons specified yet, the tree is not build.
	 */
	public void build()
	{
		if ( _polygons.size() > 0 )
		{
			build( _root , _polygons );
			_isBuild = true;
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
	private void build( final BSPTreeNode root , final List polygons )
	{
		final RenderedPolygon  partitionPolygon = (RenderedPolygon)polygons.get( 0 ); // Not very intelligent....
		final RenderedPolygon  partitionPlane   = partitionPolygon;
		final List             frontList        = new ArrayList();
		final List             backList         = new ArrayList();

		root.setPartitionPlane( partitionPlane );
		root.addPolygon( partitionPolygon );

		RenderedPolygon poly;
		for ( int i = 0 ; i < polygons.size() ; i++ )
		{
			poly = (RenderedPolygon)polygons.get( i );

			if ( poly == partitionPolygon )
				continue;

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
					backList.add ( splitted[ 0 ] );
					frontList.add( splitted[ 1 ] );
					break;
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
	 * Sort polygons in the specified order ('back-to-front' or 'front-to-back').
	 *
	 * @param   eyePoint        Eye point in model coordinates.
	 * @param   root            Start node in tree.
	 * @param   result          Result list.
	 * @param   backToFront     Should the polygons be ordered 'back-to-front' or 'front-to-back'.
	 */
	private static void getSortedPolygons( final Vector3D eyePoint , final BSPTreeNode root , final List result , final boolean backToFront )
	{
		if ( root != null )
		{
			final RenderedPolygon partitionPlane = root.getPartitionPlane();
			final BSPTreeNode     front          = backToFront ? root.getFront() : root.getBack();
			final BSPTreeNode     back           = backToFront ? root.getBack() : root.getFront();
			final List            polygons       = root.getPolygons();

			if ( front == null && back == null )
			{
				result.addAll( polygons );
			}
			else
			{
				switch ( RenderQueue.compare( partitionPlane , eyePoint ) )
				{
					case RenderQueue.IN_FRONT :
					{
						getSortedPolygons( eyePoint , back  , result , backToFront );
						result.addAll( polygons );
						getSortedPolygons( eyePoint , front , result , backToFront );
						break;
					}

					case RenderQueue.BEHIND :
					{
						getSortedPolygons( eyePoint , front , result , backToFront );
						result.addAll( polygons );
						getSortedPolygons( eyePoint , back  , result , backToFront );
						break;
					}

					case RenderQueue.COPLANAR :
					{
						getSortedPolygons( eyePoint , back  , result , backToFront );
						getSortedPolygons( eyePoint , front , result , backToFront );
						break;
					}
				}
			}
		}
	}
}