/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.Gerwin.AbtoJ3D;

import java.awt.Graphics2D;
import java.awt.Panel;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.renderer.Object3D;

/**
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class J2dView
	extends ViewView
{
	/**
	 * Canvas to draw on.
	 */
	private final Panel _canvas;

	/**
	 * Construct new J2dView.
	 */
	public J2dView( final Vector3D from , final Vector3D to )
	{
		_canvas = new Panel();

		// @FIXME Set the correct transform, how to calculate this....
	}

	/**
	 * Paint the specified paint queue onto the <code>_canvas</code>.
	 */
	public void paint( final List paintQueue )
	{
		final Graphics2D g2 = (Graphics2D)_canvas.getGraphics();
		final Matrix3D gXform = getTransform();
		final Matrix3D objXform = Matrix3D.INIT; //@FIXME How to get this info??

		for ( int i = 0 ; i < paintQueue.size() ; i++ )
		{
			final Object o =  paintQueue.get( i );
			if ( o instanceof Object3D )
			{
				( (Object3D)o ).paint( g2 , gXform , objXform );
			}
		}
	}

	/**
	 * Get the drawing canvas.
	 *
	 * @return  The drawing canvas
	 */
	public Panel getCanvas()
	{
		return _canvas;
	}
}
