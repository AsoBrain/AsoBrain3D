/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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

import java.awt.Graphics2D;
import java.awt.Paint;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * This class is a transformation node in the graphics tree. It
 * contains a TransformModel that manages a transformation matrix
 * to be associated with this node.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Transform3D
	extends Node3D
{
	/**
	 * Rotation around X-axis in decimal degrees.
	 */
	private double _rotationX;

	/**
	 * Rotation around Y-axis in decimal degrees.
	 */
	private double _rotationY;

	/**
	 * Rotation around Z-axis in decimal degrees.
	 */
	private double _rotationZ;

	/**
	 * Transform translation.
	 */
	private Vector3D _translation;

	/**
	 * Matrix with transformation.
	 */
	private Matrix3D _matrix;

	/**
	 * This flag indicates that the transformation matrix is not
	 * up-to-date and must be re-calculated when it is requested.
	 * This is set when one of the properties affecting the
	 * transformation is changed.
	 */
	private boolean _matrixDirty;

	/**
	 * Matrix with inverse transformation.
	 */
	private Matrix3D _inverseMatrix;

	/**
	 * This flag indicates that the inverse transformation matrix
	 * is not up-to-date and must be re-calculated when it is
	 * requested. This is set when one of the properties affecting
	 * the transformation is changed.
	 */
	private boolean _inverseMatrixDirty;

	/**
	 * Default constructor. Creates an identity transform model.
	 */
	public Transform3D()
	{
		_rotationX          = 0.0;
		_rotationY          = 0.0;
		_rotationZ          = 0.0;
		_translation        = Vector3D.INIT;
		_matrix             = Matrix3D.INIT;
		_matrixDirty        = true;
		_inverseMatrix      = Matrix3D.INIT;
		_inverseMatrixDirty = true;
	}

	/**
	 * Constructor based for transformation based on a matrix.
	 *
	 * @param   m   Explicit matrix to use for transformation.
	 */
	public Transform3D( final Matrix3D m )
	{
		_rotationX          = 0.0;
		_rotationY          = 0.0;
		_rotationZ          = 0.0;
		_translation        = Vector3D.INIT;
		_matrix             = m;
		_matrixDirty        = false;
		_inverseMatrix      = Matrix3D.INIT;
		_inverseMatrixDirty = true;
	}

	/**
	 * Constructor based for transformation based on a translation.
	 *
	 * @param   translation     Transform translation.
	 */
	public Transform3D( final Vector3D translation )
	{
		this();
		setTranslation( translation );
	}

	/**
	 * Constructor based for transformation based on translation and
	 * rotation.
	 *
	 * @param   translation     Transform translation.
	 * @param   rotationX       Rotation around X-axis in decimal degrees.
	 * @param   rotationY       Rotation around Y-axis in decimal degrees.
	 * @param   rotationZ       Rotation around Z-axis in decimal degrees.
	 */
	public Transform3D( final Vector3D translation , final double rotationX , final double rotationY , final double rotationZ )
	{
		this( translation );
		setRotation( rotationX , rotationY , rotationZ );
	}

	/**
	 * This method creates a collection of leafs of the graphics tree of a
	 * specific class. This is typically used during the rendering process to
	 * collect objects that need to be rendered.
	 * <p>
	 * This proces consists of two phases: first going up the tree to find the
	 * root; then going down the tree to gather the leafs and storing them in
	 * a collection.
	 * <p>
	 * During both phases, a matrix is maintained that transforms coordinates
	 * from the current node to coordinates of the node where the proces
	 * started. If a node of the requested class is found, it will be stored
	 * in combination with its matrix in the returned collection.
	 *
	 * @param   leafs       Collection that contains all gathered leafs.
	 * @param   leafClass   Class of requested leafs.
	 * @param   xform       Transformation matrix upto this node.
	 * @param   upwards     Direction in which the tree is being traversed
	 *                      (should be <code>true</code> for the first call).
	 *
	 */
	public void gatherLeafs( final Node3DCollection leafs , final Class leafClass , final Matrix3D xform , final boolean upwards )
	{
		/*
		 * Determine modified transform based on combination of the
		 * supplied transformation and the transformation defined
		 * by this object.
		 */
		 final Matrix3D combinedTransform = ( upwards ? getInverseMatrix() : getMatrix() ).multiply( xform );

		/*
		 * Let super-class do its job with the modified transformation.
		 */
		super.gatherLeafs( leafs , leafClass , combinedTransform , upwards );
	}

	/**
	 * Get matrix with inverse transformation.
	 *
	 * @return  Matrix3D with inverse transformation matrix.
	 */
	public Matrix3D getInverseMatrix()
	{
		if ( _inverseMatrixDirty )
		{
			final Matrix3D matrix = getMatrix();
			_inverseMatrix = matrix.inverse();
			_inverseMatrixDirty = false;
		}

		return _inverseMatrix;
	}

	/**
	 * Get matrix with transformation.
	 *
	 * @return  Matrix3D with transformation matrix.
	 */
	public Matrix3D getMatrix()
	{
		final Matrix3D result;

		if ( _matrixDirty )
		{
			final Vector3D translation = _translation;

			result = Matrix3D.getTransform( _rotationX , _rotationY , _rotationZ , translation.x , translation.y , translation.z );
			_matrix = result;
			_matrixDirty = false;
		}
		else
		{
			result = _matrix;
		}

		return result;
	}

	/**
	 * Set transformation using an explicit matrix for the transformation
	 * (transformation variables are ignored).
	 *
	 * @param   matrix      Explicit matrix to use for transformation.
	 */
	public void setMatrix( final Matrix3D matrix )
	{
		_matrix             = matrix;
		_matrixDirty        = false;
		_inverseMatrixDirty = true;
	}

	/**
	 * Get rotation value around X-axis.
	 *
	 * @return  Rotation around X-axis in decimal degrees.
	 */
	public double getRotationX()
	{
		return _rotationX;
	}

	/**
	 * Get rotation value around Y-axis.
	 *
	 * @return  Rotation around Y-axis in decimal degrees.
	 */
	public double getRotationY()
	{
		return _rotationY;
	}

	/**
	 * Get rotation value around Z-axis.
	 *
	 * @return  Rotation around Z-axis in decimal degrees.
	 */
	public double getRotationZ()
	{
		return _rotationZ;
	}

	/**
	 * Get transform translation.
	 *
	 * @return  Transform translation.
	 */
	public Vector3D getTranslation()
	{
		return _translation;
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final boolean alternateAppearance )
	{
		final Matrix3D matrix = getMatrix();
		super.paint( g , gTransform , matrix.multiply( viewTransform ) , alternateAppearance );
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final Matrix3D matrix = getMatrix();
		super.paint( g , gTransform , matrix.multiply( viewTransform ) , outlinePaint , fillPaint , shadeFactor );
	}

	/**
	 * Set dirty field(s) to indicate that the transformation has
	 * changed in some way.
	 */
 	private void setDirty()
 	{
	 	_matrixDirty = true;
	 	_inverseMatrixDirty = true;
 	}

	/**
	 * Set new rotation values for all axises.
	 *
	 * @param   rotationX   Rotation around X-axis in decimal degrees.
	 * @param   rotationY   Rotation around Y-axis in decimal degrees.
	 * @param   rotationZ   Rotation around Z-axis in decimal degrees.
	 */
	public void setRotation( final double rotationX , final double rotationY , final double rotationZ )
	{
		if ( rotationX != _rotationX ||
			 rotationY != _rotationY ||
			 rotationZ != _rotationZ )
		{
			_rotationX = rotationX;
			_rotationY = rotationY;
			_rotationZ = rotationZ;

			setDirty();
		}
	}

	/**
	 * Set new rotation value around X-axis.
	 *
	 * @param   rotation    Rotation around X-axis in decimal degrees.
	 */
	public void setRotationX( final double rotation )
	{
		if ( _rotationX != rotation )
		{
			_rotationX	= rotation;
			setDirty();
		}
	}

	/**
	 * Set new rotation value around Y-axis.
	 *
	 * @param   rotation    Rotation around Y-axis in decimal degrees.
	 */
	public void setRotationY( final double rotation )
	{
		if ( _rotationY != rotation )
		{
			_rotationY	= rotation;
			setDirty();
		}
	}

	/**
	 * Set new rotation value around Z-axis.
	 *
	 * @param   rotation    Rotation around Z-axis in decimal degrees.
	 */
	public void setRotationZ( final double rotation )
	{
		if ( _rotationZ != rotation )
		{
			_rotationZ	= rotation;
			setDirty();
		}
	}

	/**
	 * Set new transform translation.
	 *
	 * @param   translation Transform translation.
	 */
	public void setTranslation( final Vector3D translation )
	{
		if ( translation != null && !_translation.equals( translation ) )
		{
			_translation = translation;
			setDirty();
		}
	}
}
