/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

import junit.framework.TestCase;

import ab.j3d.Matrix3D;

/**
 * This class tests the <code>Node3DCollection</code> class.
 *
 * @see     Node3DCollection
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestNode3DCollection
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestNode3DCollection.class.getName();

	/**
	 * Test the <code>Node3DCollection</code> constructor.
	 *
	 * @throws Exception if the test fails.
	 * @see Node3DCollection#Node3DCollection
	 */
	public void testNode3DCollection()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testNode3DCollection()" );

		/*
		 * Just create object to test if constructor fails.
		 * All other tests will also fail (of course) when this happens
		 */
		new Node3DCollection();
	}

	/**
	 * Test the <code>add()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Node3DCollection#add
	 */
	public void testAdd()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testAdd()" );

		final Node3DCollection collection = new Node3DCollection();
		assertEquals( "Test #1" , 0 , collection.size() );

		collection.add( null , null );
		assertEquals( "Test #2" , 1 , collection.size() );

		collection.add( null , new Node3D() );
		assertEquals( "Test #3" , 2 , collection.size() );

		collection.add( Matrix3D.INIT , null );
		assertEquals( "Test #4" , 3 , collection.size() );

		collection.add( Matrix3D.INIT , new Node3D() );
		assertEquals( "Test #5" , 4 , collection.size() );
	}

	/**
	 * Test the <code>clear()</code> method.
	 *
	 * @see     Node3DCollection#clear
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testClear()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testClear()" );

		final Node3DCollection collection = new Node3DCollection();
		assertEquals( "Test #1" , 0 , collection.size() );
		collection.add( Matrix3D.INIT , new Node3D() );
		assertEquals( "Test #2" , 1 , collection.size() );
		collection.clear();
		assertEquals( "Test #3" , 0 , collection.size() );
	}

	/**
	 * Test the <code>getMatrix()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Node3DCollection#getMatrix
	 */
	public void testGetMatrix()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetMatrix()" );

		/*
		 * Define test properties.
		 */
		final Matrix3D matrix0 = Matrix3D.INIT;
		final Matrix3D matrix1 = Matrix3D.getTransform( 1 , 2 , 3 , 4 , 5 , 6 );
		final Node3D   node    = new Node3D();

		class Test
		{
			final int    index;
			final Object out;

			final Node3DCollection collection;

			Test( final int nrMatrices , final int index , final Object out )
			{
				this.index = index;
				this.out = out;

				final Node3DCollection collection = new Node3DCollection();
				if ( nrMatrices > 0 ) collection.add( matrix0 , node );
				if ( nrMatrices > 1 ) collection.add( matrix1 , node );
				if ( nrMatrices > 2 ) throw new IllegalArgumentException( "nrMatrices" );
				this.collection = collection;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( 0 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #2  */ new Test( 0 ,  0 , ArrayIndexOutOfBoundsException.class ),
			/* Test #3  */ new Test( 0 ,  1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #4  */ new Test( 1 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #5  */ new Test( 1 ,  0 , matrix0 ),
			/* Test #6  */ new Test( 1 ,  1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #7  */ new Test( 2 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #8  */ new Test( 2 ,  0 , matrix0 ),
			/* Test #9  */ new Test( 2 ,  1 , matrix1 ),
			/* Test #10 */ new Test( 2 ,  2 , ArrayIndexOutOfBoundsException.class ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Class expectedException = ( test.out instanceof Class ) ? (Class)test.out : null;
			try
			{
				final Matrix3D result = test.collection.getMatrix( test.index );
				if ( expectedException != null )
					fail( description + " should have thrown exception" );

				assertSame( description , test.out , result );
			}
			catch ( Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception" , expectedException.getName() , e.getClass().getName() );
			}
		}
	}

	/**
	 * Test the <code>getNode()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Node3DCollection#getNode
	 */
	public void testGetNode()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetNode()" );

		/*
		 * Define test properties.
		 */
		final Matrix3D matrix = Matrix3D.INIT;
		final Node3D   node0  = new Node3D();
		final Node3D   node1  = new Node3D();

		class Test
		{
			final int    index;
			final Object out;

			final Node3DCollection collection;

			Test( final int nrNodes , final int index , final Object out )
			{
				this.index = index;
				this.out = out;

				final Node3DCollection collection = new Node3DCollection();
				if ( nrNodes > 0 ) collection.add( matrix , node0 );
				if ( nrNodes > 1 ) collection.add( matrix , node1 );
				if ( nrNodes > 2 ) throw new IllegalArgumentException( "nrNodes" );
				this.collection = collection;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
			/* Test #1  */ new Test( 0 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #2  */ new Test( 0 ,  0 , ArrayIndexOutOfBoundsException.class ),
			/* Test #3  */ new Test( 0 ,  1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #4  */ new Test( 1 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #5  */ new Test( 1 ,  0 , node0 ),
			/* Test #6  */ new Test( 1 ,  1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #7  */ new Test( 2 , -1 , ArrayIndexOutOfBoundsException.class ),
			/* Test #8  */ new Test( 2 ,  0 , node0 ),
			/* Test #9  */ new Test( 2 ,  1 , node1 ),
			/* Test #10 */ new Test( 2 ,  2 , ArrayIndexOutOfBoundsException.class ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test   test        = tests[ i ];
			final String description = "Test #" + ( i + 1 );

			final Class expectedException = ( test.out instanceof Class ) ? (Class)test.out : null;
			try
			{
				final Node3D result = test.collection.getNode( test.index );
				if ( expectedException != null )
					fail( description + " should have thrown exception" );

				assertSame( description , test.out , result );
			}
			catch ( Exception e )
			{
				if ( expectedException == null )
				{
					System.err.println( description + " threw unexpected exception: " + e );
					throw e;
				}

				assertEquals( description + " threw wrong exception" , expectedException.getName() , e.getClass().getName() );
			}
		}
	}

	/**
	 * Test the <code>size()</code> method.
	 *
	 * @throws  Exception if the test fails.
	 *
	 * @see     Node3DCollection#size
	 */
	public void testSize()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testSize()" );

		final Node3DCollection collection = new Node3DCollection();
		assertEquals( "Test #1" , 0 , collection.size() );
		collection.add( Matrix3D.INIT , new Node3D() );
		assertEquals( "Test #2" , 1 , collection.size() );
		collection.add( Matrix3D.INIT , new Node3D() );
		assertEquals( "Test #3" , 2 , collection.size() );
		collection.add( Matrix3D.INIT , new Node3D() );
		assertEquals( "Test #4" , 3 , collection.size() );
		collection.clear();
		assertEquals( "Test #5" , 0 , collection.size() );
	}
}
