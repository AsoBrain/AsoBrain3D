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

/**
 * A PaintableTriangle models a triangle in a {@link TetraHedron}. Its surface
 * is paintable with {@link #paintAt}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class PaintableTriangle
{
	/**
	 * The TetraHedron that this triangle is part of.
	 */
	private final TetraHedron _hedron;

	/**
	 * The face number of this triangle. Corresponds to the constant int values
	 * <code>BOTTOM_FACE</code>, <code>BACK_FACE</code>, <code>LEFT_FACE</code>
	 * and <code>RIGHT_FACE</code> in {@link TetraHedron}.
	 */
	private final int _face;

	/**
	 * The length of all edges in this triangle.
	 */
	private double _size;

	/**
	 * Construct a new PaintableTriangle.
	 *
	 * @param   hedron  The {@link TetraHedron} this triangle is part of.
	 * @param   face    The face number of this triangle.
	 * @param   size    The size of the edges of this triangle.
	 */
	public PaintableTriangle( final TetraHedron hedron , final int face , final double size )
	{
		_hedron = hedron;
		_face = face;
		_size = size;
	}

	/**
	 * Returns the {@link TetraHedron} this triangle is part of.
	 *
	 * @return  {@link TetraHedron} this triangle is part of.
	 */
	public TetraHedron getTetraHedron()
	{
		return _hedron;
	}

	/**
	 * Returns the face number of this triangle. This is one of
	 * {@link TetraHedron#BOTTOM_FACE}, {@link TetraHedron#BACK_FACE},
	 * {@link TetraHedron#LEFT_FACE}, {@link TetraHedron#RIGHT_FACE}.
	 *
	 * @return  face number of this triangle.
	 */
	public int getFaceNumber()
	{
		return _face;
	}

	/**
	 * Sets a new size for the edges of this triangle.
	 *
	 * @param   size    The new size for the edges of this triangle.
	 */
	protected void setSize( final double size )
	{
		_size = size;
	}

	/**
	 * Returns the size of the edges of this triangle.
	 *
	 * @return  size of the edges of this triangle.
	 */
	public double getSize()
	{
		return _size;
	}

	/**
	 * Paint on a location on this triangle.
	 *
	 * @param   x   The x coordinate of where to paint.
	 * @param   y   The y coordinate of where to paint.
	 */
	public void paintAt( final double x , final double y )
	{
		/*This method does not actually work, but that is not a priority at
		  this moment, as long as the mechanism of clicking and painting works*/

		System.out.println( "Paint at x: " + x + " , y: " + y );
	}

}
