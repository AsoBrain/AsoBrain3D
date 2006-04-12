/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2006
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
package ab.j3d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

/**
 * This class extends {@link Object3D}. The vertices and faces are generated out
 * of a Java 2D simple {@link Shape}. An extrusion vector is used to define the
 * coordinate displacement.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ExtrudedObject2D
	extends Object3D
{
	/**
	 * Transform to apply.
	 */
	public final Matrix3D transform;

	/**
	 * Base shape.
	 */
	public final Shape shape;

	/**
	 * Extrusion vector (control-point displacement). This is a displacement
	 * relative to the shape being extruded.
	 */
	public final Vector3D extrusion;

	/**
	 * The maximum allowable distance between the control points and a
	 * flattened curve.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator(AffineTransform, double)
	 */
	public final double flatness;

	/**
	 * Flag to indicate if extruded faces have a backface.
	 */
	public final boolean hasBackface;

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   texture         Texture to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final TextureSpec texture , final double flatness , final boolean hasBackface )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;

		generate( this , shape , extrusion , transform , texture , flatness , hasBackface );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   texture         Texture to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 */
	public static void generate( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final TextureSpec texture , final double flatness , final boolean hasBackface )
	{
		final double  ex            = extrusion.x;
		final double  ey            = extrusion.y;
		final double  ez            = extrusion.z;
		final boolean hasExtrusion  = !Matrix3D.almostEqual( ex , 0.0 ) || !Matrix3D.almostEqual( ey , 0.0 ) || !Matrix3D.almostEqual( ez , 0.0 );
		final boolean flipExtrusion = hasExtrusion && !hasBackface && ( ez < 0.0 );

		final FlatteningPathIterator pathIterator = new FlatteningPathIterator( shape.getPathIterator( null ) , flatness );

		final double[] coords = new double[ 6 ];
		int lastIndex          = -1;
		int lastExtrudedIndex  = -1;
		int lastMoveTo         = -1;
		int lastExtrudedMoveTo = -1;

		while ( !pathIterator.isDone() )
		{
			switch( pathIterator.currentSegment( coords ) )
			{
				case FlatteningPathIterator.SEG_MOVETO:
				{
					final double shapeX = coords[ 0 ];
					final double shapeY = coords[ 1 ];

					final double x1 = transform.transformX( shapeX , shapeY , 0.0 );
					final double y1 = transform.transformY( shapeX , shapeY , 0.0 );
					final double z1 = transform.transformZ( shapeX , shapeY , 0.0 );

					lastIndex  = target.getVertexIndex( x1 , y1 , z1 );
					lastMoveTo = lastIndex;

					if ( hasExtrusion )
					{
						final double x2 = transform.transformX( shapeX + ex , shapeY + ey , ez );
						final double y2 = transform.transformY( shapeX + ex , shapeY + ey , ez );
						final double z2 = transform.transformZ( shapeX + ex , shapeY + ey , ez );

						lastExtrudedIndex  = target.getVertexIndex( x2 , y2 , z2 );
						lastExtrudedMoveTo = lastExtrudedIndex;
					}
					break;
				}

				case FlatteningPathIterator.SEG_LINETO:
				{
					final double shapeX = coords[ 0 ];
					final double shapeY = coords[ 1 ];

					final double x1 = transform.transformX( shapeX , shapeY , 0.0 );
					final double y1 = transform.transformY( shapeX , shapeY , 0.0 );
					final double z1 = transform.transformZ( shapeX , shapeY , 0.0 );

					final int vertexIndex = target.getVertexIndex( x1 , y1 , z1 );

					int extrudedVertexIndex = -1;
					if ( hasExtrusion )
					{
						final double x2 = transform.transformX( shapeX + ex , shapeY + ey , ez );
						final double y2 = transform.transformY( shapeX + ex , shapeY + ey , ez );
						final double z2 = transform.transformZ( shapeX + ex , shapeY + ey , ez );
						extrudedVertexIndex = target.getVertexIndex( x2 , y2 , z2 );
					}

					if ( lastIndex != vertexIndex )
					{
						if ( hasExtrusion )
						{
							target.addFace(
								flipExtrusion ? new int[] { vertexIndex , extrudedVertexIndex , lastExtrudedIndex  , lastIndex  }
								              : new int[] { lastIndex  , lastExtrudedIndex  , extrudedVertexIndex , vertexIndex } ,
								texture , null , null , 1.0f , false , hasBackface );
							lastExtrudedIndex = extrudedVertexIndex;
						}
						else
						{
							target.addFace( new int[]{ lastIndex , vertexIndex } , texture , null , null , 1.0f , false , true );
						}
					}

					lastIndex = vertexIndex;
					break;
				}

				case FlatteningPathIterator.SEG_CLOSE:
				{
					if ( lastIndex != lastMoveTo )
					{
						if ( hasExtrusion )
						{
							target.addFace(
								flipExtrusion ? new int[] { lastMoveTo , lastExtrudedMoveTo , lastExtrudedIndex  , lastIndex  }
								              : new int[] { lastIndex  , lastExtrudedIndex  , lastExtrudedMoveTo , lastMoveTo } ,
								texture , null , null , 1.0f , false , hasBackface );

							lastExtrudedIndex = lastExtrudedMoveTo;
						}
						else
						{
							target.addFace( new int[] { lastIndex , lastMoveTo } , texture , null , null , 1.0f , false , true );
						}
					}

					lastIndex = lastMoveTo;
					break;
				}
			}
			pathIterator.next();
		}
	}
}
