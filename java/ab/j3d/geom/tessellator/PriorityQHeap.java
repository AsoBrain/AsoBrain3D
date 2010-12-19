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

class PriorityQHeap
	extends PriorityQ
{
	PQnode[] nodes;

	PQhandleElem[] handles;

	int size;

	int max;

	int freeList;

	boolean initialized;

	Comparator leq;

	protected PriorityQHeap( final Comparator leq )
	{
		size = 0;
		max = INIT_SIZE;
		nodes = new PQnode[INIT_SIZE + 1];
		for ( int i = 0; i < nodes.length; i++ )
		{
			nodes[ i ] = new PQnode();
		}
		handles = new PQhandleElem[INIT_SIZE + 1];
		for ( int i = 0; i < handles.length; i++ )
		{
			handles[ i ] = new PQhandleElem();
		}
		initialized = false;
		freeList = 0;
		this.leq = leq;

		nodes[ 1 ].handle = 1;	/* so that Minimum() returns NULL */
		handles[ 1 ].key = null;
	}

	void floatDown( int curr )
	{
		final PQnode[] n = nodes;
		final PQhandleElem[] h = handles;
		final int hCurr;
		int hChild;
		int child;

		hCurr = n[ curr ].handle;
		for (; ; )
		{
			child = curr << 1;
			if ( child < size && leq.leq( h[ n[ child + 1 ].handle ].key,
				h[ n[ child ].handle ].key ) )
			{
				++child;
			}

			assert ( child <= max );

			hChild = n[ child ].handle;
			if ( child > size || leq.leq( h[ hCurr ].key, h[ hChild ].key ) )
			{
				n[ curr ].handle = hCurr;
				h[ hCurr ].node = curr;
				break;
			}
			n[ curr ].handle = hChild;
			h[ hChild ].node = curr;
			curr = child;
		}
	}


	void floatUp( int curr )
	{
		final PQnode[] n = nodes;
		final PQhandleElem[] h = handles;
		final int hCurr;
		int hParent;
		int parent;

		hCurr = n[ curr ].handle;
		for (; ; )
		{
			parent = curr >> 1;
			hParent = n[ parent ].handle;
			if ( parent == 0 || leq.leq( h[ hParent ].key, h[ hCurr ].key ) )
			{
				n[ curr ].handle = hCurr;
				h[ hCurr ].node = curr;
				break;
			}
			n[ curr ].handle = hParent;
			h[ hParent ].node = curr;
			curr = parent;
		}
	}

	/* really __gl_pqHeapInit */
	@Override
	void pqInit()
	{
		/* This method of building a heap is O(n), rather than O(n lg n). */
		for ( int i = size; i >= 1; --i )
		{
			floatDown( i );
		}
		initialized = true;
	}

	/* really __gl_pqHeapInsert */

	/* returns LONG_MAX iff out of memory */
	@Override
	int pqInsert( final Object keyNew )
	{
		final int curr;
		final int free;

		curr = ++size;
		if ( ( curr * 2 ) > max )
		{
			/* If the heap overflows, double its size. */
			max <<= 1;
			final PQnode[] pqNodes = new PQnode[max + 1];
			System.arraycopy( nodes, 0, pqNodes, 0, nodes.length );
			for ( int i = nodes.length; i < pqNodes.length; i++ )
			{
				pqNodes[ i ] = new PQnode();
			}
			nodes = pqNodes;

			final PQhandleElem[] pqHandles = new PQhandleElem[max + 1];
			System.arraycopy( handles, 0, pqHandles, 0, handles.length );
			for ( int i = handles.length; i < pqHandles.length; i++ )
			{
				pqHandles[ i ] = new PQhandleElem();
			}
			handles = pqHandles;
		}

		if ( freeList == 0 )
		{
			free = curr;
		}
		else
		{
			free = freeList;
			freeList = handles[ free ].node;
		}

		nodes[ curr ].handle = free;
		handles[ free ].node = curr;
		handles[ free ].key = keyNew;

		if ( initialized )
		{
			floatUp( curr );
		}
		assert ( free != Integer.MAX_VALUE );
		return free;
	}

	/* really __gl_pqHeapExtractMin */
	@Override
	Object pqExtractMin()
	{
		final PQnode[] n = nodes;
		final PQhandleElem[] h = handles;
		final int hMin = n[ 1 ].handle;
		final Object min = h[ hMin ].key;

		if ( size > 0 )
		{
			n[ 1 ].handle = n[ size ].handle;
			h[ n[ 1 ].handle ].node = 1;

			h[ hMin ].key = null;
			h[ hMin ].node = freeList;
			freeList = hMin;

			if ( --size > 0 )
			{
				floatDown( 1 );
			}
		}
		return min;
	}

	@Override
	void pqDelete( final int hCurr )
	{
		final PQnode[] n = nodes;
		final PQhandleElem[] h = handles;
		final int curr;

		assert ( hCurr >= 1 && hCurr <= max && h[ hCurr ].key != null );

		curr = h[ hCurr ].node;
		n[ curr ].handle = n[ size ].handle;
		h[ n[ curr ].handle ].node = curr;

		if ( curr <= --size )
		{
			if ( curr <= 1 || leq.leq( h[ n[ curr >> 1 ].handle ].key, h[ n[ curr ].handle ].key ) )
			{
				floatDown( curr );
			}
			else
			{
				floatUp( curr );
			}
		}
		h[ hCurr ].key = null;
		h[ hCurr ].node = freeList;
		freeList = hCurr;
	}

	@Override
	Object pqMinimum()
	{
		return handles[ nodes[ 1 ].handle ].key;
	}

	@Override
	boolean pqIsEmpty()
	{
		return size == 0;
	}
}
