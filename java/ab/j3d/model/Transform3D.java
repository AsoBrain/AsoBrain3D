package ab.light3d.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import java.awt.Graphics;

import ab.light3d.Matrix3D;
import ab.light3d.Vector3D;

/**
 * This class is a transformation node in the graphics tree. It
 * contains a TransformModel that manages a transformation matrix
 * to be associated with this node.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Transform
	extends TreeNode
{
	private	float		_rotationX		= 0.0f;
	private	float		_rotationY		= 0.0f;
	private	float		_rotationZ		= 0.0f;
	private	Vector3D _translation = Vector3D.INIT;

	/**
	 * Matrix with transformation.
	 */
	private	Matrix3D _matrix = Matrix3D.INIT;

	/**
	 * This flag indicates that the transformation matrix is not
	 * up-to-date and must be re-calculated when it is requested.
	 * This is set when one of the properties affecting the
	 * transformation is changed.
	 */
	private	boolean _matrixDirty = true;

	/**
	 * Matrix with inverse transformation.
	 */
	private	Matrix3D _inverseMatrix = Matrix3D.INIT;

	/**
	 * This flag indicates that the inverse transformation matrix
	 * is not up-to-date and must be re-calculated when it is
	 * requested. This is set when one of the properties affecting
	 * the transformation is changed.
	 */
	private	boolean _inverseMatrixDirty = true;
	/**
	 * Default constructor. Creates an identity transform model.
	 */
	public Transform()
	{
	}

	/**
	 * Constructor based for transformation based on a matrix.
	 *
	 * @param	m	Explicit matrix to use for transformation.
	 */
	public Transform( final Matrix3D m )
	{
		_matrix = m;
		_matrixDirty = false;
	}

	/**
	 * Constructor based for transformation based on a translation.
	 *
	 * @param	translation	Transform translation.
	 */
	public Transform( final Vector3D translation )
	{
		setTranslation( translation );
	}

	/**
	 * Constructor based for transformation based on translation and
	 * rotation.
	 *
	 * @param	translation	Transform translation.
	 * @param	rotationX		Rotation around X-axis in decimal degrees.
	 * @param	rotationY		Rotation around Y-axis in decimal degrees.
	 * @param	rotationZ		Rotation around Z-axis in decimal degrees.
	 */
	public Transform( final Vector3D translation ,
		final float rotationX , final float rotationY , final float rotationZ )
	{
		setTranslation( translation );
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
	 * @param	leafs		Collection that contains all gathered leafs.
	 * @param	leafClass	Class of requested leafs.
	 * @param	xform		Transformation matrix upto this node.
	 * @param	upwards		Direction in which the tree is being traversed
	 *						(should be <code>true</code> for the first call).
	 *
	 */
	public void gatherLeafs( final LeafCollection leafs , final Class leafClass , Matrix3D xform , final boolean upwards )
	{
		/*
		 * Determine modified transform based on combination of the
		 * supplied transformation and the transformation defined
		 * by this object.
		 */
		 xform = (upwards ? getInverseMatrix() : getMatrix()).multiply( xform );

		/*
		 * Let super-class do its job with the modified transformation.
		 */
		super.gatherLeafs( leafs , leafClass , xform , upwards );
	}

	/**
	 * Get matrix with inverse transformation.
	 *
	 * @return	Matrix3D with inverse transformation matrix.
	 */
	public Matrix3D getInverseMatrix()
	{
		if ( _inverseMatrixDirty )
		{
			_inverseMatrix = getMatrix().inverse();
			_inverseMatrixDirty = false;
		}

		return( _inverseMatrix );
	}

	/**
	 * Get matrix with transformation.
	 *
	 * @return	Matrix3D with transformation matrix.
	 */
	public Matrix3D getMatrix()
	{
		if ( _matrixDirty )
		{
			_matrix = Matrix3D.getTransform( _rotationX , _rotationY , _rotationZ , _translation.x , _translation.y , _translation.z );
			_matrixDirty = false;
		}
		return( _matrix );
	}

	/**
	 * Get rotation value around X-axis.
	 *
	 * @return	Rotation around X-axis in decimal degrees.
	 */
	public float getRotationX()
	{
		return( _rotationX );
	}

	/**
	 * Get rotation value around Y-axis.
	 *
	 * @return	Rotation around Y-axis in decimal degrees.
	 */
	public float getRotationY()
	{
		return( _rotationY );
	}

	/**
	 * Get rotation value around Z-axis.
	 *
	 * @return	Rotation around Z-axis in decimal degrees.
	 */
	public float getRotationZ()
	{
		return( _rotationZ );
	}

	/**
	 * Get transform translation.
	 *
	 * @return	Transform translation.
	 */
	public Vector3D getTranslation()
	{
		return _translation;
	}

	/**
	 * Paint 2D representation of this node and all its leaf nodes.
	 *
	 * @param	g			Graphics context.
	 * @param	gXform		Transformation to pan/scale the graphics context.
	 * @param	objXform	Transformation from object's to view coordinate system.
	 */
	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D objXform )
	{
		super.paint( g , gXform , getMatrix().multiply( objXform ) );
	}

	/**
	 * Set dirty field(s) to indicate that the transformation has
	 * changed in some way.
	 */
 	protected void setDirty()
 	{
	 	_matrixDirty = true;
	 	_inverseMatrixDirty = true;
 	}

	/**
	 * Set new rotation values for all axises.
	 *
	 * @param	rotationX	Rotation around X-axis in decimal degrees.
	 * @param	rotationY	Rotation around Y-axis in decimal degrees.
	 * @param	rotationZ	Rotation around Z-axis in decimal degrees.
	 */
	public void setRotation( final float rotationX , final float rotationY , final float rotationZ )
	{
		if ( rotationX != _rotationX ||
			 rotationY != _rotationY ||
			 rotationZ != _rotationZ )
		{
			_rotationX	= rotationX;
			_rotationY	= rotationY;
			_rotationZ	= rotationZ;

			setDirty();
		}
	}

	/**
	 * Set new rotation value around X-axis.
	 *
	 * @param	rotation	Rotation around X-axis in decimal degrees.
	 */
	public void setRotationX( final float rotation )
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
	 * @param	rotation	Rotation around Y-axis in decimal degrees.
	 */
	public void setRotationY( final float rotation )
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
	 * @param	rotation	Rotation around Z-axis in decimal degrees.
	 */
	public void setRotationZ( final float rotation )
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
	 * @param	translation	Transform translation.
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
