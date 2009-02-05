/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2008-2009 Numdata BV.  All rights reserved.
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
package ab.j3d.geom;

import junit.framework.TestCase;

import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

/**
 * Test {@link BasicTriangle3D} class.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestAbstractTriangle3D
	extends TestCase
{
	/**
	 * Tests the {@link AbstractTriangle3D#calculatePerimeter} and {@link AbstractTriangle3D#calculateArea} methods.
	 */
	public void testTriangleMath()
	{
		/*
		 * Create test class.
		 */
		class Test
		{
			Vector3D _p1;
			Vector3D _p2;
			Vector3D _p3;

			double _expectedPerimeter;
			double _expectedArea;

			Test( final Vector3D p1 , final Vector3D p2 , final Vector3D p3 , final double expectedPerimeter , final double expectedArea )
			{
				_p1 = p1;
				_p2 = p2;
				_p3 = p3;

				_expectedPerimeter = expectedPerimeter;
				_expectedArea      = expectedArea;
			}
		}

		/*
		 * Create tests.
		 */
		final Test[] tests = new Test[]
			{
				/* Test 1 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set( 10.0 , 10.0 ,  0.0 ) , 34.142 , 50.000 ) ,
				/* Test 2 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set( 20.0 , 10.0 ,  0.0 ) , 46.502 , 49.999 ) ,
				/* Test 3 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set( 10.0 , 0.0 ,  0.0 ) , Vector3D.INIT.set(  5.0 , 10.0 ,  0.0 ) , 32.360 , 50.000 ) ,
				/* Test 4 */ new Test( Vector3D.INIT.set( 0.0 , 0.0 , 0.0 ) , Vector3D.INIT.set(  0.0 , 0.0 , 10.0 ) , Vector3D.INIT.set(  0.0 , 10.0 , 10.0 ) , 34.142 , 50.000 ) ,
			};

		/*
		 * Run tests.
		 */
		int testNr = 1;

		for ( final Test test : tests )
		{
			final String desc = "Test #" + ( testNr++ ) + ": ";

			final double actualPerimeter = AbstractTriangle3D.calculatePerimeter( test._p1 , test._p2 , test._p3 );
			assertTrue( desc + "Invalid perimeter calculated. Expected: " + test._expectedPerimeter + " Actual: " + actualPerimeter , MathTools.almostEqual( test._expectedPerimeter , actualPerimeter ) );

			final double actualArea = AbstractTriangle3D.calculateArea( test._p1 , test._p2 , test._p3 );
			assertTrue( desc + "Invalid area calculated. Expected: " + test._expectedArea + " Actual: " + actualArea , MathTools.almostEqual( test._expectedArea , actualArea ) );
		}
	}
}