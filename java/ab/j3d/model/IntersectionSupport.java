/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * @author Mart Slot
 * @version $Revision$ $Date$
 * IntersectionSupport provides methods for testing intersection between
 * objects. Currently, only the intersection between a line and objects can be
 * tested.
 */
public class IntersectionSupport
{
	protected class Intersection
	{
		public Object3D object;

		public Face3D face;

		public double intersectionDistance;
	}

	/**
	 * Construct new IntersectionSupport.
	 */
	public IntersectionSupport()
	{
	}

	/**
	 * Returns a List of the faces of the objects in {@link Node3DCollection}
	 * <code>scene</code> that are intersected by the line with startpoint
	 * <code>lineStart</code> and endpoint <code>lineEnd</code>.
	 *
	 * @param scene     The objects for which intersection should be tested
	 * @param lineStart The startpoint of the intersection line
	 * @param lineEnd   The endpoint of the intersection line
	 *
	 * @return A list of the selected {@link Face3D}s, ordered from front to back.
	 */
	public List getIntersectingFaces( Node3DCollection scene, Vector3D lineStart, Vector3D lineEnd )
	{
		List rawIntersections = new LinkedList();

		Vector3D direction = ( lineEnd.minus( lineStart ) ).normalize();
		double rotateX = Math.asin( direction.z );
		double rotateZ = Math.asin( direction.x );
		Matrix3D lineTransform = Matrix3D.INIT.rotateX( rotateX ).rotateZ( rotateZ );
		lineTransform = Matrix3D.INIT.setTranslation( -lineStart.x, -lineStart.y, -lineStart.z ).multiply( lineTransform );

		double d = Vector3D.distanceBetween( lineStart, lineEnd );
		lineStart = Vector3D.INIT;
		lineEnd   = Vector3D.INIT.set( 0 , d , 0 );

		for ( int i = 0; i < scene.size(); i++ )
		{
			final Object3D object = (Object3D)scene.getNode( i );
			final Matrix3D matrix = scene.getMatrix( i ).multiply( lineTransform );

//			System.out.println( "\nTesting intersection with object " + object.getTag() );
			rawIntersections.addAll( getIntersectingFaces( object, matrix, lineStart, lineEnd ) );
		}

		List sortedIntersections = new ArrayList( rawIntersections.size() );
		for ( Iterator iterator = rawIntersections.iterator(); iterator.hasNext(); )
		{
			final Intersection intersection = (Intersection)iterator.next();
			double distance = intersection.intersectionDistance;
//			System.out.println( "Face: "+ intersection.object.getTag()+"  distance: " + distance );
			boolean stop = false;

			if ( sortedIntersections.isEmpty() )
			{
//				System.out.println( "empty" );
				sortedIntersections.add( intersection );
				stop = true;
			}

			for ( int i = sortedIntersections.size() - 1; i >= 0 && !stop; i-- )
			{
				Intersection temp = (Intersection)sortedIntersections.get( i );
				if ( distance >= temp.intersectionDistance )
				{
//					System.out.println( "added at " + (i+1) );
					sortedIntersections.add( i + 1, intersection );
					stop = true;
				}
			}

			if ( !stop )
			{
//				System.out.println( "added at front" );
				sortedIntersections.add( 0, intersection );
			}
		}

		List intersectingFaces = new ArrayList( sortedIntersections.size() );
		for ( int i = 0; i < sortedIntersections.size(); i++ )
		{
			Intersection intersection = (Intersection)sortedIntersections.get( i );
			intersectingFaces.add( intersection.face );
		}
		return intersectingFaces;
	}

	/**
	 * Returns a list with all the faces of Object3D <code>object</code> that are
	 * intersected by the line that starts at <code>lineStart</code> and ends at
	 * <code>lineEnd</code>.<p> Before testing the intersection, the object is
	 * transformed with <code> objectTransform</code>. This can be used for
	 * instance for moving the object to world coordinates before checking
	 * intersection.
	 *
	 * @param object          The object to check intersection.
	 * @param objectTransform A Matrix with which the object is multiplied before
	 *                        the intersection check
	 * @param lineStart       The startpoint of the intersection line
	 * @param lineEnd         The endpoint of the intersection line
	 *
	 * @return A list with the intersected faces
	 */
	protected List getIntersectingFaces( Object3D object, Matrix3D objectTransform, Vector3D lineStart, Vector3D lineEnd )
	{
		List intersections = new ArrayList( object.getFaceCount() );

		final int      faceCount   = object.getFaceCount();
		final double[] vertices    = objectTransform.transform( object.getPointCoords() , null , object.getPointCount() );
		final double[] faceNormals = objectTransform.rotate( object.getFaceNormals() , null , faceCount );

		for ( int faceIndex = 0; faceIndex < faceCount; faceIndex++ )
		{
			final Face3D face        = object.getFace( faceIndex );
			final int    vertexCount = face.getVertexCount();

			/*
			 * If we have less than two points, its not a valid line/face.
			 */
			if ( vertexCount > 1 )
			{
//				System.out.println( "vertexcount > 1" );
				final int[] pointIndices = face.getPointIndices();
				final int in = pointIndices[ 1 ] * 3;

				final Vector3D normal = Vector3D.INIT.set( faceNormals[ ( faceIndex * 3 ) ] , faceNormals[ ( faceIndex * 3 ) + 1 ] , faceNormals[ ( faceIndex * 3 ) + 2 ] );
				final Vector3D p3     = Vector3D.INIT.set( vertices[ in ] , vertices[ in + 1 ] , vertices[ in + 2 ]                                                       );
//				System.out.println( normal.toFriendlyString() );
//				System.out.println( lineStart.toFriendlyString() );
//				System.out.println( lineEnd.toFriendlyString() );
//				System.out.println( p3.toFriendlyString() );

				final double divide1 = Vector3D.dot( normal , p3.minus( lineStart )      );
				final double divide2 = Vector3D.dot( normal , lineEnd.minus( lineStart ) );
				double u = 0;
				if ( divide2 != 0 )
				{
					u = divide1 / divide2;
				}

				if ( u >= 0 && u <= 1 )
				{
//					System.out.println( "u between 0 and 1, u="+ u );
					double intX = lineStart.x + u * ( lineEnd.x - lineStart.x );
					double intZ = lineStart.z + u * ( lineEnd.z - lineStart.z );
//					System.out.println( "Intersection  intX" + intX + "  intZ" + intZ );

					int     lastIndex = pointIndices[ pointIndices.length - 1 ] * 3;
					double  x1        = vertices[ lastIndex ];
					double  z1        = vertices[ lastIndex + 2 ];
					boolean left      = false;
					boolean right     = false;
					boolean center    = false;

					for ( int vertex = 0; vertex < vertexCount && !( left && right ); vertex++ )
					{
						final int    index = pointIndices[ vertex ] * 3;
						final double x2    = vertices[ index ];
						final double z2    = vertices[ index + 2 ];
//						System.out.println( "Vertex " + vertex + "  x1 " +x1 + "  x2 " +x2+ "  z1 " +z1+ "  z2 " +z2);

						if ( x1 != x2 || z1 != z2 )
						{
							double dir = ( intZ - z1 ) * ( x2 - x1 ) - ( intX - x1 ) * ( z2 - z1 );
							left  = left  || dir > 0;
							right = right || dir < 0;

							if ( dir == 0 )
							{
								double minX = x1 <  x2 ? x1 : x2;
								double maxX = x1 >= x2 ? x1 : x2;
								double minZ = z1 <  z2 ? z1 : z2;
								double maxZ = z1 >= z2 ? z1 : z2;
								center = center || intX >= minX && intX <= maxX && intZ >= minZ && intZ <= maxZ;
							}

//							System.out.println( "Vertex " + vertex + "  x1 " +x1 + "  x2 " +x2+ "  z1 " +z1+ "  z2 " +z2 + "  dir="+ dir );

							x1 = x2;
							z1 = z2;
						}
					}

//					System.out.println( "Left: " + left + "  Right: " + right + "  Center: " + center );
					if ( left ^ right || center )
					{
						Intersection intersection = new Intersection();
						intersection.object = object;
						intersection.face = face;
						intersection.intersectionDistance = u;
						intersections.add( intersection );
//						System.out.println( "left XOR right" );
					}
				}

			}

		}

		return intersections;
	}

}
