/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.control.controltest.model;

import ab.j3d.Matrix3D;

/**
 * This class models a regular tetrahedron, a pyramid consisting of four
 * identical triangles. These triangles are modeled by {@link PaintableTriangle}s.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class TetraHedron
	extends SceneElement
{
	/**
	 * The index for the bottom face of a tetrahedron.
	 */
	public static final int BOTTOM_FACE = 0;

	/**
	 * The index for the back face of a tetrahedron.
	 */
	public static final int BACK_FACE = 1;

	/**
	 * The index for the left face of a tetrahedron.
	 */
	public static final int LEFT_FACE = 2;

	/**
	 * The index for the right face of a tetrahedron.
	 */
	public static final int RIGHT_FACE = 3;

	/**
	 * The X coordinate of the location of this tetrahedron.
	 */
	private double _x;

	/**
	 * The Y coordinate of the location of this tetrahedron.
	 */
	private double _y;

	/**
	 * This tetrahedron's rotation around the Z-axis.
	 */
	private double _rotation;

	/**
	 * The size of this tetrahedron. This is the length of all edges.
	 */
	private double _size;

	/**
	 * The faces of this tetrahedron. This is an array with all four faces. The
	 * indices of the faces correspond to the static ints BOTTOM_FACE,
	 * BACK_FACE, LEFT_FACE and RIGHT_FACE
	 */
	private final PaintableTriangle[] _faces;

	/**
	 * Construct a new {@link TetraHedron}.
	 *
	 * @param   x           X location of the new tetrahedron
	 * @param   y           Y location of the new tetrahedron
	 * @param   rotation    Rotation around the z axis.
	 * @param   size        Size of the tetrahedron. This will be the length of
	 *                      the edges.
	 */
	public TetraHedron( final double x , final double y , final double rotation , final double size )
	{
		_x        = x;
		_y        = y;
		_rotation = rotation;
		_size     = size;

		_faces = new PaintableTriangle[ 4 ];
		for ( int i = 0 ; i < 4 ; i++ )
		{
			_faces[ i ] = new PaintableTriangle( this , i , size );
		}
	}

	/**
	 * Returns a {@link Matrix3D} with the transformation of this element. This
	 * should include translation, rotation and scale where needed.
	 *
	 * @return  Transformation matrix of this element.
	 */
	public Matrix3D getTransform()
	{
		return Matrix3D.getTransform( 0.0 , 0.0 , _rotation , _x , _y , 0.0 );
	}

	/**
	 * Returns the x coordinate of the location of this tetrahedron.
	 *
	 * @return  X coordinate of the location of this tetrahedron
	 */
	public double getX()
	{
		return _x;
	}

	/**
	 * Sets the x coordinate of the location of this tetrahedron.
	 *
	 * @param   x   new x coordinate.
	 */
	public void setX( final double x )
	{
		_x = x;
		elementChanged();
	}

	/**
	 * Returns the y coordinate of the location of this tetrahedron.
	 *
	 * @return  Y coordinate of the location of this tetrahedron
	 */
	public double getY()
	{
		return _y;
	}

	/**
	 * Sets the y coordinate of the location of this tetrahedron.
	 *
	 * @param   y   new y coordinate.
	 */
	public void setY( final double y )
	{
		_y = y;
		elementChanged();
	}

	/**
	 * Sets the new location of this tetrahedron.
	 *
	 * @param   x   x coordinate of the new location
	 * @param   y   y coordinate of the new location
	 */
	public void setLocation( final double x , final double y )
	{
		_x = x;
		_y = y;
		elementChanged();
	}

	/**
	 * Returns this tetrahedron's rotation. This is always a rotation around the
	 * z-axis, in degrees.
	 *
	 * @return  This tetrahedron's rotation.
	 */
	public double getRotation()
	{
		return _rotation;
	}

	/**
	 * Sets the new rotation of this tetrahedron. This is the rotation around
	 * the z axis, in degrees
	 *
	 * @param   r   new rotation of the tetrahedron, in degrees.
	 */
	public void setRotation( final double r )
	{
		_rotation = r;
		elementChanged();
	}

	/**
	 * Returns the size of the edges of this tetrahedron.
	 *
	 * @return  size of the edges of this tetrahedron.
	 */
	public double getSize()
	{
		return _size;
	}

	/**
	 * Sets the new size of this tetrahedron. This is the size of the edges.
	 *
	 * @param   size    new size of the edges of the tetrahedron.
	 */
	public void setSize( final double size )
	{
		_size = size;

		for ( int i = 0; i < 4; i++ )
		{
			_faces[i].setSize( size );
		}

		elementChanged();
	}

	/**
	 * Returns one of the faces of this tetrahedron. The variable
	 * <code>face</code> should be one of {@link #BACK_FACE},
	 * {@link #BOTTOM_FACE}, {@link #LEFT_FACE} or {@link #RIGHT_FACE}.
	 *
	 * @param   face    The number of the face to return.
	 *
	 * @return  One of the faces of this tetrahedron.
	 */
	public PaintableTriangle getFace( final int face )
	{
		return _faces[ face ];
	}
}
