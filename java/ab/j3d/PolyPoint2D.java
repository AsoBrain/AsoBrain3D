/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2001-2004 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package ab.light3d;

/**
 * This class describes a polyline control point in 2D.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class PolyPoint2D
{
	/**
	 * Tolerance for almostEquals() method.
	 *
	 * @see	#almostEquals
	 */
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
	 * @param   x	X coordinate of control point.
	 * @param   y	Y coordinate of control point.
	 */
	public PolyPoint2D( final float x , final float y )
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Test if the specified point is 'almost equal to' this point. The
	 * other point must have the same coordinates with a extremely small
	 * tolerance.
	 *
	 * @param   other	Point to compare with.
	 *
	 * @return  <CODE>true</CODE> if the specified point is 'almost' equal;
	 * 			<CODE>false</CODE> if the specified point is different.
	 */
	public boolean almostEquals( final PolyPoint2D other )
	{
		return almostEquals( other , ALMOST );
	}

	/**
	 * Test if the specified point is 'almost equal to' this point. The
	 * other point must have the same coordinates as this one with the
	 * specified tolerance.
	 *
	 * @param   other		Point to compare with.
	 * @param   tolerance	Acceptable tolerance.
	 *
	 * @return  <CODE>true</CODE> if the specified point is 'almost' equal;
	 * 			<CODE>false</CODE> if the specified point is different.
	 */
	public boolean almostEquals( final PolyPoint2D other , final float tolerance )
	{
		if ( other == null ) return false;
		if ( other == this ) return true;

		float d;
		return ( ( d = x - other.x ) > -tolerance ) && ( d < tolerance )
		    && ( ( d = y - other.y ) > -tolerance ) && ( d < tolerance );
	}

	/**
	 * Test for equality between this control point and the specified one.
	 *
	 * @param   other	Control point to compare with.
	 *
	 * @return  <CODE>true</CODE> if the control points are equals;
	 *			<CODE>false</CODE> otherwise.
	 */
	public boolean equals( final PolyPoint2D other )
	{
		return x == other.x && y == other.y;
	}

	/**
	 * Test for equality between this control point and the specified object.
	 * If the other object is a PolyPoint2D, this call is forwarded to
	 * #equals(PolyPoint2D).
	 *
	 * @param   other	Object to compare with.
	 *
	 * @return  <CODE>true</CODE> if the specified object is equal to this;
	 *			<CODE>false</CODE> otherwise.
	 */
	public boolean equals( final Object other )
	{
		return ( other instanceof PolyPoint2D ) && equals( (PolyPoint2D)other );
	}

    /**
     * Returns a hash code for this object.
     *
     * @return  Hash code value for this object.
     */
    public int hashCode()
	{
		return Float.floatToIntBits( x ) ^ Float.floatToIntBits( y );
    }

	/**
	 * Get length of segment of which this is the end point, and the specified argument
	 * represents the start point.
	 *
	 * @param   start	PolyPoint2D instance that defines the start point.
	 *
	 * @return  Length of segment.
	 */
	public float getLength( final PolyPoint2D start )
	{
		if ( almostEquals( start ) )
			return 0;

		final float dx = x - start.x;
		final float dy = y - start.y;

		return (float)Math.sqrt( dx * dx + dy * dy );
	}

	/**
	 * Construct point from string representation that was previously
	 * generated by the toString() method.
	 *
	 * @param   str		String representation of point.
	 *
	 * @return  Point that was created (may be <CODE>null</CODE>).
	 */
	public static PolyPoint2D createInstance( final String str )
	{
		if ( str == null || str.length() == 0 )
			throw new IllegalArgumentException( "invalid point specification: " + str );

		final int firstComma = str.indexOf( ',' );
		if ( firstComma < 0 )
			throw new IllegalArgumentException( "insufficient tokens in specification: " + str );

		final String type = str.substring( 0 , firstComma ).trim();

		if ( "L".equals( type ) )
		{
			final int secondComma = str.indexOf( ',' , firstComma + 1 );
			if ( ( secondComma < 0 ) || ( str.indexOf( ',' , secondComma + 1 ) >= 0 ) )
				throw new IllegalArgumentException( "invalid token count in line specification: " + str );

			final float x = new Float( str.substring( firstComma + 1 , secondComma ).trim() ).floatValue();
			final float y = new Float( str.substring( secondComma + 1 ).trim() ).floatValue();

			return new PolyPoint2D( x , y );
		}
		else
		{
			throw new IllegalArgumentException( "unrecognized point type: " + type );
		}
	}

	/**
	 * Get string representation of this object.
	 *
	 * @return  String representing this object.
	 */
	public synchronized String toString()
	{
		return new StringBuffer()
						.append( "L," )
						.append( x )
						.append( "," )
						.append( y )
						.toString();
	}

	/**
	 * This method transforms this control point using the specified transformation
	 * matrix.
	 *
	 * @param   xform	Transformation matrix to apply.
	 *
	 * @return  PolyPoint2D resulting from transformation (may return this instance
	 *			if the transformation has no effect).
	 */
	protected PolyPoint2D transform( final Matrix3D xform )
	{
		if ( xform == null )
			return this;

		final float tx = x * xform.xx + y * xform.xy + xform.xo;
		final float ty = x * xform.yx + y * xform.yy + xform.yo;

		if ( tx == x && ty == y )
			return this;

		return new PolyPoint2D( tx , ty );
	}

}
