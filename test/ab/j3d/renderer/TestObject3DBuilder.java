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
package ab.light3d.renderer;

import junit.framework.TestCase;

/**
 * This class tests the <code>Object3DBuilder</code> class.
 *
 * @see     Object3DBuilder
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public class TestObject3DBuilder
    extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestObject3DBuilder.class.getName();

	/**
	 * Test <code>Object3DBuilder.getRangeAdjustment()</code> method.
	 *
	 * @see Object3DBuilder#getRangeAdjustment
	 */
	public static void testGetRangeAdjustment()
	{
		System.out.println( CLASS_NAME + ".testGetRangeAdjustment()" );

		final int[][] tests =
		{
			{ -20 , 10 ,  20 } ,
			{ -19 , 10 ,  20 } ,
			{ -11 , 10 ,  20 } ,
			{ -10 , 10 ,  10 } ,
			{  -9 , 10 ,  10 } ,
			{  -1 , 10 ,  10 } ,
			{   0 , 10 ,   0 } ,
			{   1 , 10 ,   0 } ,
			{   9 , 10 ,   0 } ,
			{  10 , 10 , -10 } ,
			{  11 , 10 , -10 } ,
			{  19 , 10 , -10 } ,
			{  20 , 10 , -20 } ,
		};

		for ( int i = 0 ; i < tests.length ; i++ )
		{
			final int value  = tests[ i ][ 0 ];
			final int range  = tests[ i ][ 1 ];
			final int result = tests[ i ][ 2 ];

			assertEquals( "getRangeAdjustment( " + value + " , " + range + " )" , result , Object3DBuilder.getRangeAdjustment( value , range ) );
		}
	}
}
