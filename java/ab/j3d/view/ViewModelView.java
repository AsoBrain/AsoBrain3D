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

import ab.j3d.Matrix3D;

/**
 * View in view model.
 *
 * @see     ViewModel
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public abstract class ViewView
{
	/**
	 * Transform for this view view.
	 */
	private Matrix3D _transform;

	/**
	 * Construct new ViewView.
	 */
	protected ViewView()
	{
		_transform = Matrix3D.INIT;
	}

	/**
	 * Set the transform for this view view.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( final Matrix3D transform )
	{
		if ( transform == null )
			throw new NullPointerException( "transform" );

		_transform = transform;
	}

	public Matrix3D getTransform()
	{
		return _transform;
	}
}
