/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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

/**
 * This class extends <code>Object3D</code>. The vertices and faces
 * are generated out of a simple awt shape. A extrusion factor can
 * be added.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class ExtrudedObject2D
	extends Object3D
{
	/**
	 * Transform to apply.
	 */
	public final Matrix3D xform;

	/**
	 * Base shape.
	 */
	public final Shape shape;

	/**
	 * Extrusion value. This is a positive or negative displacement in the
	 * Z-axis direction to create an extruded shape (the rest of the shape is
	 *  placed on the <code>Z=0</code> plane).
	 */
	public final double extrusion;

	/**
	 * Texture to apply to all faces of the extruded shape.
	 */
	public final TextureSpec texture;

	/**
	 * Tte maximum allowable distance between the control points and the flattened curve.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator(AffineTransform, double)
	 */
	public final double flatness;

	/**
	 * Construct new ExtrudedObject2D.
	 *
	 * @param   shape       Base shape.
	 * @param   extrusion   Extrusion value.
	 * @param   xform       Transform to apply.
	 * @param   texture     Texture to apply.
	 * @param   flatness    Flatness to use.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final double extrusion , final Matrix3D xform , final TextureSpec texture , final double flatness )
	{
		this.shape     = shape;
		this.extrusion = extrusion;
		this.xform     = xform;
		this.texture   = texture;
		this.flatness  = flatness;

		generate( this , shape , extrusion , xform , texture , flatness );
	}

	public static void generate( final Object3D target , final Shape shape , final double extrusion , final Matrix3D xform , final TextureSpec texture , final double flatness )
	{
		final boolean hasExtrusion = !Matrix3D.almostEqual( extrusion , 0.0 );
		final double  shapeZ1      = hasExtrusion ? ( extrusion < 0.0 ) ? extrusion : 0.0 : 0.0;
		final double  shapeZ2      = hasExtrusion ? ( extrusion < 0.0 ) ? 0.0 : extrusion : 0.0;

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

					final double x1 = xform.transformX( shapeX , shapeY , shapeZ1 );
					final double y1 = xform.transformY( shapeX , shapeY , shapeZ1 );
					final double z1 = xform.transformZ( shapeX , shapeY , shapeZ1 );

					lastIndex  = target.getOrAddPointIndex( x1 , y1 , z1 );
					lastMoveTo = lastIndex;

					if ( hasExtrusion )
					{
						final double x2 = xform.transformX( shapeX , shapeY , shapeZ2 );
						final double y2 = xform.transformY( shapeX , shapeY , shapeZ2 );
						final double z2 = xform.transformZ( shapeX , shapeY , shapeZ2 );

						lastExtrudedIndex  = target.getOrAddPointIndex( x2 , y2 , z2 );
						lastExtrudedMoveTo = lastExtrudedIndex;
					}
					break;
				}

				case FlatteningPathIterator.SEG_LINETO:
				{
					final double shapeX = coords[ 0 ];
					final double shapeY = coords[ 1 ];

					final double x1 = xform.transformX( shapeX , shapeY , shapeZ1 );
					final double y1 = xform.transformY( shapeX , shapeY , shapeZ1 );
					final double z1 = xform.transformZ( shapeX , shapeY , shapeZ1 );
					final int pointIndex = target.getOrAddPointIndex( x1 , y1 , z1 );

					int extrudedPointIndex = -1;
					if ( hasExtrusion )
					{
						final double x2 = xform.transformX( shapeX , shapeY , shapeZ2 );
						final double y2 = xform.transformY( shapeX , shapeY , shapeZ2 );
						final double z2 = xform.transformZ( shapeX , shapeY , shapeZ2 );
						extrudedPointIndex = target.getOrAddPointIndex( x2 , y2 , z2 );
					}

					if ( lastIndex != pointIndex )
					{
						if ( hasExtrusion )
						{
							target.addFace( new int[]{ lastIndex , lastExtrudedIndex , extrudedPointIndex , pointIndex } , texture  , false , true );
							lastExtrudedIndex = extrudedPointIndex;
						}
						else
						{
							target.addFace( new int[]{ lastIndex , pointIndex } , texture  , false );
						}
					}

					lastIndex = pointIndex;
					break;
				}

				case FlatteningPathIterator.SEG_CLOSE:
				{
					if ( lastIndex != lastMoveTo )
					{
						if ( hasExtrusion )
						{
							target.addFace( new int[]{ lastIndex , lastExtrudedIndex , lastExtrudedMoveTo , lastMoveTo } , texture  , false , true );
							lastExtrudedIndex = lastExtrudedMoveTo;
						}
						else
						{
							target.addFace( new int[]{ lastIndex , lastMoveTo } , texture  , false );
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
