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

		generate();
	}

	private void generate()
	{
		final FlatteningPathIterator pathIterator = new FlatteningPathIterator( _shape.getPathIterator( null ) , _flatness );

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

					final double x1 = _xform.transformX( x , y , 0.0 );
					final double y1 = _xform.transformY( x , y , 0.0 );
					final double z1 = _xform.transformZ( x , y , 0.0 );
					lastIndex  = getOrAddPointIndex( x1 , y1 , z1 );
					lastMoveTo = lastIndex;

					if ( _extrusion > 0.0 )
					{
						final double x2 = _xform.transformX( x , y , _extrusion );
						final double y2 = _xform.transformY( x , y , _extrusion );
						final double z2 = _xform.transformZ( x , y , _extrusion );
						lastExtrudedIndex  = getOrAddPointIndex( x2 , y2 , z2 );
						lastExtrudedMoveTo = lastExtrudedIndex;
					}
					break;
				}

				case FlatteningPathIterator.SEG_LINETO:
				{
					final double x = coords[ 0 ];
					final double y = coords[ 1 ];

					final double x1 = _xform.transformX( x , y , 0.0 );
					final double y1 = _xform.transformY( x , y , 0.0 );
					final double z1 = _xform.transformZ( x , y , 0.0 );
					final int pointIndex = getOrAddPointIndex( x1 , y1 , z1 );

					int extrudedPointIndex = -1;
					if ( _extrusion > 0.0 )
					{
						final double x2 = _xform.transformX( x , y , _extrusion );
						final double y2 = _xform.transformY( x , y , _extrusion );
						final double z2 = _xform.transformZ( x , y , _extrusion );
						extrudedPointIndex = getOrAddPointIndex( x2 , y2 , z2 );
					}

					if ( lastIndex != pointIndex )
					{
						if ( _extrusion > 0.0 )
						{
							addFace( new int[]{ lastIndex , lastExtrudedIndex , extrudedPointIndex , pointIndex } , _texture  , false );
							lastExtrudedIndex = extrudedPointIndex;
						}
						else
						{
							addFace( new int[]{ lastIndex , pointIndex } , _texture  , false );
						}
					}

					lastIndex = pointIndex;
					break;
				}

				case FlatteningPathIterator.SEG_CLOSE:
				{
					if ( lastIndex != lastMoveTo )
					{
						if ( _extrusion > 0.0)
						{
							addFace( new int[]{ lastIndex , lastExtrudedIndex , lastExtrudedMoveTo , lastMoveTo } , _texture  , false );
							lastExtrudedIndex = lastExtrudedMoveTo;
						}
						else
						{
							addFace( new int[]{ lastIndex , lastMoveTo } , _texture  , false );
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
