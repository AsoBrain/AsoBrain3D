/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
	 * Base shape.
	 */
	private Shape _shape;

	/**
	 * Extrusion value.
	 */
	private double _extrusion;

	/**
	 * Transform to apply.
	 */
	private Matrix3D _xform;

	/**
	 * Texture to apply.
	 */
	private TextureSpec _texture;

	/**
	 * Tte maximum allowable distance between the
	 * control points and the flattened curve.
	 */
	private double _flatness;

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
		_shape     = shape;
		_extrusion = extrusion;
		_xform     = xform;
		_texture   = texture;
		_flatness  = flatness;

		generate( this , shape , extrusion , xform , texture , flatness );
	}

	public static void generate( final Object3D target , final Shape shape , final double extrusion , final Matrix3D xform , final TextureSpec texture , final double flatness )
	{
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
					final double x = coords[ 0 ];
					final double y = coords[ 1 ];

					final double x1 = xform.transformX( x , y , 0.0 );
					final double y1 = xform.transformY( x , y , 0.0 );
					final double z1 = xform.transformZ( x , y , 0.0 );
					lastIndex  = target.getOrAddPointIndex( x1 , y1 , z1 );
					lastMoveTo = lastIndex;

					if ( extrusion > 0.0 )
					{
						final double x2 = xform.transformX( x , y , extrusion );
						final double y2 = xform.transformY( x , y , extrusion );
						final double z2 = xform.transformZ( x , y , extrusion );
						lastExtrudedIndex  = target.getOrAddPointIndex( x2 , y2 , z2 );
						lastExtrudedMoveTo = lastExtrudedIndex;
					}
					break;
				}

				case FlatteningPathIterator.SEG_LINETO:
				{
					final double x = coords[ 0 ];
					final double y = coords[ 1 ];

					final double x1 = xform.transformX( x , y , 0.0 );
					final double y1 = xform.transformY( x , y , 0.0 );
					final double z1 = xform.transformZ( x , y , 0.0 );
					final int pointIndex = target.getOrAddPointIndex( x1 , y1 , z1 );

					int extrudedPointIndex = -1;
					if ( extrusion > 0.0 )
					{
						final double x2 = xform.transformX( x , y , extrusion );
						final double y2 = xform.transformY( x , y , extrusion );
						final double z2 = xform.transformZ( x , y , extrusion );
						extrudedPointIndex = target.getOrAddPointIndex( x2 , y2 , z2 );
					}

					if ( lastIndex != pointIndex )
					{
						if ( extrusion > 0.0 )
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
						if ( extrusion > 0.0)
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
