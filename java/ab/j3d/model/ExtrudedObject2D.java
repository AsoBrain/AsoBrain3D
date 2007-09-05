/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

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
	 * Indicates whether normals are flipped.
	 */
	public final boolean flipNormals;

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface )
	{
		this( shape , extrusion , transform , material , flatness , hasBackface , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface , final boolean flipNormals )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;
		this.flipNormals = flipNormals;

		generate( this , shape , extrusion , transform , material , flatness , hasBackface , flipNormals );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 */
	public static void generate( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface )
	{
		generate( target , shape , extrusion , transform , material , flatness , hasBackface , false );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 */
	public static void generate( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface , final boolean flipNormals )
	{
		final double  ex            = extrusion.x;
		final double  ey            = extrusion.y;
		final double  ez            = extrusion.z;
		final boolean hasExtrusion  = !MathTools.almostEqual( ex , 0.0 ) || !MathTools.almostEqual( ey , 0.0 ) || !MathTools.almostEqual( ez , 0.0 );
		final boolean flipExtrusion = flipNormals ^ ( hasExtrusion && !hasBackface && ( ez < 0.0 ) );

		final FlatteningPathIterator pathIterator = new FlatteningPathIterator( shape.getPathIterator( null ) , flatness );

		final double[] coords = new double[ 6 ];
		int lastIndex          = -1;
		int lastExtrudedIndex  = -1;
		int lastMoveTo         = -1;
		int lastExtrudedMoveTo = -1;

		// @TODO Provide better mapping.
		final UVMap uvMap = new ManhattanUVMap( 0.001 , Vector3D.INIT ); // @FIXME Get actual model units from somewhere.

		while ( !pathIterator.isDone() )
		{
			final int[] vertexIndices;

			boolean hasBackfaceOverride = hasBackface;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
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

					vertexIndices = null;
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
							vertexIndices = flipExtrusion ? new int[] { vertexIndex , extrudedVertexIndex , lastExtrudedIndex   , lastIndex   }
							                              : new int[] { lastIndex   , lastExtrudedIndex   , extrudedVertexIndex , vertexIndex };
							lastExtrudedIndex = extrudedVertexIndex;
						}
						else
						{
							vertexIndices = new int[] { lastIndex , vertexIndex };
							hasBackfaceOverride = true;
						}
					}
					else
					{
						vertexIndices = null;
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
							vertexIndices = flipExtrusion ? new int[] { lastMoveTo , lastExtrudedMoveTo , lastExtrudedIndex  , lastIndex  }
							                              : new int[] { lastIndex  , lastExtrudedIndex  , lastExtrudedMoveTo , lastMoveTo };
							lastExtrudedIndex = lastExtrudedMoveTo;
						}
						else
						{
							vertexIndices = new int[] { lastIndex , lastMoveTo };
							hasBackfaceOverride = true;
						}
					}
					else
					{
						vertexIndices = null;
					}

					lastIndex = lastMoveTo;
					break;
				}

				default:
					vertexIndices = null;
			}

			if ( vertexIndices != null )
			{
				final float[] textureU;
				final float[] textureV;
				if ( ( material == null ) || ( material.colorMap == null ) )
				{
					textureU = null;
					textureV = null;
				}
				else
				{

					textureU = new float[ vertexIndices.length ];
					textureV = new float[ vertexIndices.length ];

					uvMap.generate( material , target.getVertexCoordinates(), vertexIndices , textureU , textureV );
				}

				target.addFace( vertexIndices , material , textureU , textureV , 1.0f , false , hasBackfaceOverride );
			}

			pathIterator.next();
		}
	}
}
