package backoffice;

import common.model.Matrix3D;

/**
 * This class describes a polyline control point in 2D.
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 * @author	Sjoerd Bouwman
 *
 * Copyright (c) 2001 Numdata BV, Eibergen, The Netherlands
 */
public class PolyPoint2D 
{
	public static final float ALMOST = 0.0001f;

	/**
	 * X coordinate of control point.
	 */ 
	public final float x;
	
	/**
	 * Y coordinate of control point.
	 */ 
	public final float y;
	
	/**
	 * Construct new control point.
	 *
	 * @param	x	X coordinate of control point.
	 * @param	y	Y coordinate of control point.
	 */
	public PolyPoint2D( float x , float y )
	{
		this.x = x;
		this.y = y;
	}

	public boolean almostEquals( PolyPoint2D other )
	{
		return almostEquals( other , ALMOST );
	}

	public boolean almostEquals( PolyPoint2D other , float tolerance )
	{
		if ( other == null )
			return false;

		if ( other.equals( this ) )
			return true;
			
		return 	x > other.x - tolerance && x < other.x + tolerance 
	         && y > other.y - tolerance && y < other.y + tolerance;
	}

	/**
	 * Test for equality between this control point and the specified one.
	 *
	 * @param	other	Control point to compare with.
	 *
	 * @return	<code>true</code> if the control points are equals;
	 *			<code>false</code> otherwise.
	 */
	public boolean equals( PolyPoint2D other )
	{
		return x == other.x && y == other.y;
	}

	/**
	 * Test for equality between this control point and the specified object.
	 * If the other object is a PolyPoint2D, this call is forwarded to
	 * #equals(PolyPoint2D).
	 *
	 * @param	other	Object to compare with.
	 *
	 * @return	<code>true</code> if the specified object is equal to this;
	 *			<code>false</code> otherwise.
	 */
	public boolean equals( Object other )
	{
		return ( other instanceof PolyPoint2D ) && equals( (PolyPoint2D)other );
	}

	/**
	 * Get length of segment of which this is the end point, and the specified argument
	 * represents the start point.
	 *
	 * @param	start	PolyPoint2D instance that defines the start point.
	 *
	 * @return	Length of segment.
	 */
	public float getLength( PolyPoint2D start )
	{
		if ( almostEquals( start ) )
			return 0;
		
		float dx = x - start.x;
		float dy = y - start.y;
		
		return (float)Math.sqrt( dx * dx + dy * dy );
	}

	/**
	 * Get string representation of this object.
	 *
	 * @return	String representing this object.
	 */
	public synchronized String toString()
	{
		StringBuffer sb = new StringBuffer( 16 );
		sb.append( "(" );
		sb.append( x );
		sb.append( "," );
		sb.append( y );
		sb.append( ")" );
		return sb.toString();
	}

	/**
	 * This method transforms this control point using the specified transformation
	 * matrix.
	 *
	 * @param	xform	Transformation matrix to apply.
	 *
	 * @return	PolyPoint2D resulting from transformation (may return this instance
	 *			if the transformation has no effect).
	 */
	protected PolyPoint2D transform( Matrix3D xform )
	{
		if ( xform == null )
			return this;

		float tx = x * xform.xx + y * xform.xy + xform.xo;
		float ty = x * xform.yx + y * xform.yy + xform.yo;

		if ( tx == x && ty == y )
			return this;
			
		return new PolyPoint2D( tx , ty );
	}

}
