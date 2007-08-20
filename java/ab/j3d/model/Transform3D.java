/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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

import ab.j3d.Matrix3D;

/**
 * This class defines a transformation node in the graphics tree..
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Transform3D
	extends Node3D
{
	/**
	 * Transformation matrix.
	 */
	private Matrix3D _transform;

	/**
	 * Inverse transformation matrix (<code>null</code> if not known yet).
	 */
	private Matrix3D _inverseTransform;

	/**
	 * Default constructor. Initializes node to an identity transform.
	 */
	public Transform3D()
	{
		_transform        = Matrix3D.INIT;
		_inverseTransform = Matrix3D.INIT;
	}

	/**
	 * Construct node with specific initial transformation matrix.
	 *
	 * @param   transform   Transformation matrix to initialize node to.
	 */
	public Transform3D( final Matrix3D transform )
	{
		_transform        = transform;
		_inverseTransform = null;
	}

	public <T extends Node3D> Node3DCollection<T> collectNodes( final Node3DCollection<T> collection , final Class<? extends T> nodeClass , final Matrix3D transform , final boolean upwards )
	{
		/*
		 * Determine modified transform based on combination of the
		 * supplied transformation and the transformation defined
		 * by this node.
		 */
		final Matrix3D combinedTransform;

		final Matrix3D nodeTransform = ( upwards ? getInverseTransform() : getTransform() );
		if ( ( nodeTransform != null ) && ( nodeTransform != Matrix3D.INIT ) )
		{
			combinedTransform = nodeTransform.multiply( transform );
		}
		else
		{
			combinedTransform = transform;
		}

		/*
		 * Let super-class do its job with the modified transformation.
		 */
		return super.collectNodes( collection , nodeClass , combinedTransform , upwards );
	}

	/**
	 * Get inverse transformation matrix.
	 *
	 * @return  Inverse transformation matrix.
	 */
	public Matrix3D getInverseTransform()
	{
		Matrix3D result = _inverseTransform;
		if ( result == null )
		{
			final Matrix3D transform = getTransform();
			result = transform.inverse();
			_inverseTransform = result;
		}

		return result;
	}

	/**
	 * Set inverse transformation matrix. This also sets the normal
	 * transformation matrix.
	 *
	 * @param   transform   Inverse transformation matrix.
	 */
	public void setInverseTransform( final Matrix3D transform )
	{
		_transform        = transform.inverse();
		_inverseTransform = transform;
	}

	/**
	 * Get transformation matrix.
	 *
	 * @return  Transformation matrix.
	 *
	 * @see     #getInverseTransform
	 */
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set transformation matrix.
	 *
	 * @param   transform   Transformation matrix.
	 */
	public void setTransform( final Matrix3D transform )
	{
		_transform        = transform;
		_inverseTransform = null;
	}
}
