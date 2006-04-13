/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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
package ab.j3d.control;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

/**
 * IntersectionSupport provides methods for testing intersection between
 * objects. Currently, only the intersection between a line and objects can be
 * tested.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class IntersectionSupport
{
	/**
	 * Returns a {@link Node3DCollection} with all {@link Object3D}s in the
	 * scene. Implementing classes should only put objects that need to be
	 * tested for intersection in this collection. The transform matrices in
	 * the {@link Node3DCollection} should hold the matrix for transforming the
	 * object to world coordinates. If the scene is empty, an empty
	 * {@link Node3DCollection} should be returned.
	 *
	 * @return  A {@link Node3DCollection} containing the objects in the scene.
	 */
	protected abstract Node3DCollection getScene();

	/**
	 * Returns the ID for an {@link Object3D}.
	 *
	 * @param   object  The object for which to return the ID.
	 *
	 * @return  The ID for <code>object</code>.
	 */
	protected abstract Object getIDForObject( Object3D object );

	/**
	 * Returns a List of {@link Intersection}s, which hold information about the
	 * objects that are intersected by a line starting at
	 * <code>lineStart</code> going through <code>linePoint</code>.
	 *
	 * @param   lineStart   The startpoint of the intersection line
	 * @param   linePoint   A point on the intersection line.
	 *
	 * @return  A list of {@link Intersection}s, ordered from front to back.
	 */
	public List getIntersections( final Vector3D lineStart, final Vector3D linePoint )
	{
		Vector3D start = lineStart;
		Vector3D point = linePoint;

		final List intersections = new LinkedList();

		final Node3DCollection scene = getScene();

		final Matrix3D lineTransform = Matrix3D.getFromToTransform( start , point , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
		final double d = Vector3D.distanceBetween( start , point );

		if ( d < 0.001 && d > -0.001 )
			throw new IllegalArgumentException( "lineStart and linePoint are not supposed to be the same." );

		start = Vector3D.INIT;
		point = Vector3D.INIT.set( 0.0 , 0.0 , -d );

		for ( int i = 0 ; i < scene.size() ; i++ )
		{
			final Object3D object = (Object3D)scene.getNode( i );
			final Matrix3D objectTransform = scene.getMatrix( i );

			intersections.addAll( getIntersections( object , objectTransform , lineTransform , start , point ) );
		}

		for ( int pointer = 1 ; pointer < intersections.size() ; pointer++ )
		{
			final Intersection intersection = (Intersection)intersections.get( pointer );
			final double distance = intersection.getIntersectionDistance();

			boolean stop = false;
			int index = pointer;
			while ( index >= 0 && !stop )
			{
				final Intersection current = (Intersection)intersections.get( index );

				if ( distance > current.getIntersectionDistance() )
				{
					index++;
					stop = true;
				}
				else
				{
					index--;
				}
			}

			if ( !stop )
				index = 0;

			intersections.remove( pointer );
			intersections.add( index , intersection );
		}

		return intersections;
	}

	/**
	 * Returns a list of {@link Intersection}s for the faces of
	 * <code>object</code> that are intersected by the line that starts at
	 * <code>lineStart</code> going through <code>linePoint</code>.<p> The
	 * Matrix <code>WorldTransform</code> is used to transform the object's
	 * coordinates to world coordinates. After that, they are multiplied with
	 * <code>lineTransform</code>, which transforms the world so that the
	 * intersection line runs parallel to the z axis.
	 *
	 * @param   object          The object for which to check intersection.
	 * @param   worldTransform  The Matrix for transforming the object.
	 *                          coordinates to world coordinates.
	 * @param   lineTransform   The Matrix for transforming the object
	 *                          coordinates to line coordinates.
	 * @param   lineStart       The startpoint of the intersection line.
	 * @param   linePoint       A point on the intersection line.
	 *
	 * @return  A list with the intersections.
	 */
	protected List getIntersections( final Object3D object , final Matrix3D worldTransform , final Matrix3D lineTransform , final Vector3D lineStart , final Vector3D linePoint )
	{
		final List intersections = new ArrayList( object.getFaceCount() );

		final Matrix3D objectTransform = worldTransform.multiply( lineTransform );

		final int      faceCount   = object.getFaceCount();
		final double[] vertices    = object.getVertexCoordinates( objectTransform , null );
		final double[] faceNormals = object.getFaceNormals( objectTransform , null );

		for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
		{
			final Face3D face        = object.getFace( faceIndex );
			final int    vertexCount = face.getVertexCount();

			/*
			 * If there are less than two points, its not a valid line/face.
			 */
			if ( vertexCount > 1 )
			{
				final int[] vertexIndices = face.getVertexIndices();
				final int   in            = vertexIndices[ 1 ] * 3;

				final Vector3D normal = Vector3D.INIT.set( faceNormals[ ( faceIndex * 3 ) ] , faceNormals[ ( faceIndex * 3 ) + 1 ] , faceNormals[ ( faceIndex * 3 ) + 2 ] );
				final Vector3D p3     = Vector3D.INIT.set( vertices[ in ] , vertices[ in + 1 ] , vertices[ in + 2 ]                                                       );

				final double divide1 = Vector3D.dot( normal , p3.minus( lineStart )      );
				final double divide2 = Vector3D.dot( normal , linePoint.minus( lineStart ) );
				double u = 0.0;
				if ( divide2 != 0.0 )
				{
					u = divide1 / divide2;
				}

				/*
				 * If u is greater than or equal to zero, there is an intersection.
				 */
				if ( u >= 0.0 )
				{
					final double intX = lineStart.x + u * ( linePoint.x - lineStart.x );
					final double intY = lineStart.y + u * ( linePoint.y - lineStart.y );
					final double intZ = lineStart.z + u * ( linePoint.z - lineStart.z );

					final int lastIndex = vertexIndices[ vertexIndices.length - 1 ] * 3;
					double  x1        = vertices[ lastIndex ];
					double  y1        = vertices[ lastIndex + 1 ];
					boolean left      = false;
					boolean right     = false;
					boolean center    = false;

					for ( int vertex = 0 ; vertex < vertexCount && !( left && right ) ; vertex++ )
					{
						final int    index = vertexIndices[ vertex ] * 3;
						final double x2    = vertices[ index ];
						final double y2    = vertices[ index + 1 ];

						if ( x1 != x2 || y1 != y2 )
						{
							final double dir = ( intY - y1 ) * ( x2 - x1 ) - ( intX - x1 ) * ( y2 - y1 );
							left  = left  || dir > 0.0;
							right = right || dir < 0.0;

							if ( dir == 0.0 )
							{
								final double minX = x1 <  x2 ? x1 : x2;
								final double maxX = x1 >= x2 ? x1 : x2;
								final double minY = y1 <  y2 ? y1 : y2;
								final double maxY = y1 >= y2 ? y1 : y2;
								center = center || intX >= minX && intX <= maxX && intY >= minY && intY <= maxY;
							}

							x1 = x2;
							y1 = y2;
						}
					}

					if ( left ^ right || center )
					{
						final Object   id                = getIDForObject( object );
						final Vector3D intersectionPoint = Vector3D.INIT.set( intX , intY , intZ );
						final double   distance          = intersectionPoint.distanceTo( lineStart );

						final Matrix3D lineInverse  = lineTransform.inverse();
						final Vector3D world        = lineInverse.multiply( intersectionPoint );
						final Matrix3D worldInverse = worldTransform.inverse();
						final Vector3D local        = worldInverse.multiply( world );

						final Intersection intersection = new Intersection( id , face , distance , world , local );

						intersections.add( intersection );

					}
				}

			}

		}

		return intersections;
	}

}
