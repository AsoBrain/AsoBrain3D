/*
 * License Applicability. Except to the extent portions of this file are made
 * subject to an alternative license as permitted in the SGI Free Software
 * License B, Version 1.1 (the "License"), the contents of this file are subject
 * only to the provisions of the License. You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at Silicon
 * Graphics, Inc., attn: Legal Services, 1600 Amphitheatre Parkway, Mountain
 * View, CA 94043-1351, or at:
 *
 * http://oss.sgi.com/projects/FreeB
 *
 * Note that, as provided in the License, the Software is distributed on an
 * "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 * DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 * CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 * PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 *
 * NOTE: The Original Code (as defined below) has been licensed under the SGI
 * Free Software License B (Version 1.1), shown above ("SGI License"). Pursuant
 * to Section 3.2(3) of the SGI License, the Covered Code, is distributed as
 * in modified form as part of the AsoBrain 3D Toolkit, which is licensed under
 * an alternative license, the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version. This alternative license applies to all code that
 * is not part of the "Original Code" (as defined below) and is
 * Copyright (C) 1999-2010 Peter S. Heijnen. You may obtain a copy of the
 * GNU Lesser General Public License from the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Original Code. The Original Code is: OpenGL Sample Implementation,
 * Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 * Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 * Copyright in any portions created by third parties is as indicated elsewhere
 * herein. All Rights Reserved.
 *
 * Author: Eric Veach, July 1994
 * Java Port: Pepijn Van Eeckhoudt, July 2003
 * Java Port: Nathan Parker Burg, August 2003
 * AsoBrain3D Port: Peter S. Heijnen, December 2010
 */
package ab.j3d.geom.tessellator;

class PriorityQSort
	extends PriorityQ
{
	PriorityQHeap heap;

	Object[] keys;

	// JAVA: 'order' contains indices into the keys array.
	// This simulates the indirect pointers used in the original C code
	// (from Frank Suykens, Luciad.com).
	int[] order;

	int size;

	int max;

	boolean initialized;

	Comparator leq;

	protected PriorityQSort( final Comparator leq )
	{
		heap = new PriorityQHeap( leq );

		keys = new Object[INIT_SIZE];

		size = 0;
		max = INIT_SIZE;
		initialized = false;
		this.leq = leq;
	}

	private static boolean lessThan( final Comparator comparator, final Object a, final Object b )
	{
		return !comparator.leq( b, a );
	}

	private static boolean greaterThan( final Comparator comparator, final Object x, final Object y )
	{
		return ( !comparator.leq( x, y ) );
	}

	private static void swap( final int[] array, final int a, final int b )
	{
		final int tmp = array[ a ];
		array[ a ] = array[ b ];
		array[ b ] = tmp;
	}

	private static class Stack
	{
		int p;

		int r;
	}

	@Override
	void pqInit()
	{
		int p;
		int r;
		int i;
		int j;
		int piv;
		final Stack[] stack = new Stack[50];
		for ( int k = 0; k < stack.length; k++ )
		{
			stack[ k ] = new Stack();
		}
		int top = 0;

		int seed = 2016473283;

		/* Create an array of indirect pointers to the keys, so that we
		 * the handles we have returned are still valid.
		 */
		order = new int[size + 1];
		/* the previous line is a patch to compensate for the fact that IBM */
		/* machines return a null on a malloc of zero bytes (unlike SGI),   */
		/* so we have to put in this defense to guard against a memory      */
		/* fault four lines down. from fossum@austin.ibm.com.               */
		p = 0;
		r = size - 1;
		for ( piv = 0, i = p; i <= r; ++piv, ++i )
		{
			// indirect pointers: keep an index into the keys array, not a direct pointer to its contents
			order[ i ] = piv;
		}

		/* Sort the indirect pointers in descending order,
		 * using randomized Quicksort
		 */
		stack[ top ].p = p;
		stack[ top ].r = r;
		++top;
		while ( --top >= 0 )
		{
			p = stack[ top ].p;
			r = stack[ top ].r;
			while ( r > p + 10 )
			{
				seed = Math.abs( seed * 1539415821 + 1 );
				i = p + seed % ( r - p + 1 );
				piv = order[ i ];
				order[ i ] = order[ p ];
				order[ p ] = piv;
				i = p - 1;
				j = r + 1;
				do
				{
					do
					{
						++i;
					}
					while ( greaterThan( leq, keys[ order[ i ] ], keys[ piv ] ) );
					do
					{
						--j;
					}
					while ( lessThan( leq, keys[ order[ j ] ], keys[ piv ] ) );
					swap( order, i, j );
				}
				while ( i < j );
				swap( order, i, j );	/* Undo last swap */
				if ( i - p < r - j )
				{
					stack[ top ].p = j + 1;
					stack[ top ].r = r;
					++top;
					r = i - 1;
				}
				else
				{
					stack[ top ].p = p;
					stack[ top ].r = i - 1;
					++top;
					p = j + 1;
				}
			}
			/* Insertion sort small lists */
			for ( i = p + 1; i <= r; ++i )
			{
				piv = order[ i ];
				for ( j = i; j > p && lessThan( leq, keys[ order[ j - 1 ] ], keys[ piv ] ); --j )
				{
					order[ j ] = order[ j - 1 ];
				}
				order[ j ] = piv;
			}
		}
		max = size;
		initialized = true;
		heap.pqInit(); /* always succeeds */
	}

	/* returns LONG_MAX iff out of memory */
	@Override
	int pqInsert( final Object keyNew )
	{
		final int curr;

		if ( initialized )
		{
			return heap.pqInsert( keyNew );
		}
		curr = size;
		if ( ++size >= max )
		{
			/* If the heap overflows, double its size. */
			max <<= 1;
			final Object[] pqKeys = new Object[max];
			System.arraycopy( keys, 0, pqKeys, 0, keys.length );
			keys = pqKeys;
		}
		assert curr != Integer.MAX_VALUE;
		keys[ curr ] = keyNew;

		/* Negative handles index the sorted array. */
		return -( curr + 1 );
	}

	@Override
	Object pqExtractMin()
	{
		final Object sortMin;
		final Object heapMin;

		if ( size == 0 )
		{
			return heap.pqExtractMin();
		}
		sortMin = keys[ order[ size - 1 ] ];
		if ( !heap.pqIsEmpty() )
		{
			heapMin = heap.pqMinimum();
			if ( leq.leq( heapMin, sortMin ) )
			{
				return heap.pqExtractMin();
			}
		}
		do
		{
			--size;
		}
		while ( size > 0 && keys[ order[ size - 1 ] ] == null );
		return sortMin;
	}

	@Override
	Object pqMinimum()
	{
		final Object sortMin;
		final Object heapMin;

		if ( size == 0 )
		{
			return heap.pqMinimum();
		}
		sortMin = keys[ order[ size - 1 ] ];
		if ( !heap.pqIsEmpty() )
		{
			heapMin = heap.pqMinimum();
			if ( leq.leq( heapMin, sortMin ) )
			{
				return heapMin;
			}
		}
		return sortMin;
	}

	@Override
	boolean pqIsEmpty()
	{
		return ( size == 0 ) && heap.pqIsEmpty();
	}

	@Override
	void pqDelete( int curr )
	{
		if ( curr >= 0 )
		{
			heap.pqDelete( curr );
			return;
		}
		curr = -( curr + 1 );
		assert curr < max && keys[ curr ] != null;

		keys[ curr ] = null;
		while ( size > 0 && keys[ order[ size - 1 ] ] == null )
		{
			--size;
		}
	}
}
