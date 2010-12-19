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

import org.jetbrains.annotations.*;

class RegionDict
{
	private final Sweep _sweep;

	DictNode head;

	RegionDict( final Sweep sweep )
	{
		_sweep = sweep;
		head = new DictNode();
		head.key = null;
		head.next = head;
		head.prev = head;
	}

	/**
	 * Both edges must be directed from right to left (this is the canonical
	 * direction for the upper edge of each region).
	 *
	 * The strategy is to evaluate a "t" value for each edge at the
	 * current sweep line position, given by tess.event.  The calculations
	 * are designed to be very stable, but of course they are not perfect.
	 *
	 * Special case: if both edge destinations are at the sweep event,
	 * we sort the edges by slope (they would otherwise compare equally).
	 */
	public boolean lessOrEqual( final Region region1, final Region region2 )
	{
		final boolean result;

		final Vertex event = _sweep._event;
		final HalfEdge e1 = region1.upperEdge;
		final HalfEdge e2 = region2.upperEdge;

		if ( e1.symmetric.origin == event )
		{
			if ( e2.symmetric.origin == event )
			{
				/*
				 * Two edges right of the sweep line which meet at the sweep event.
				 * Sort them by slope.
				 */
				if ( Geom.vertLeq( e1.origin, e2.origin ) )
				{
					result = Geom.edgeSign( e2.symmetric.origin, e1.origin, e2.origin ) <= 0;
				}
				else
				{
					result = Geom.edgeSign( e1.symmetric.origin, e2.origin, e1.origin ) >= 0;
				}
			}
			else
			{
				result = Geom.edgeSign( e2.symmetric.origin, event, e2.origin ) <= 0;
			}
		}
		else if ( e2.symmetric.origin == event )
		{
			result = Geom.edgeSign( e1.symmetric.origin, event, e1.origin ) >= 0;
		}
		else
		{
			/* General case - compute signed distance *from* e1, e2 to event */
			final double t1 = Geom.edgeEval( e1.symmetric.origin, event, e1.origin );
			final double t2 = Geom.edgeEval( e2.symmetric.origin, event, e2.origin );
			result = ( t1 >= t2 );
		}

		return result;
	}

	DictNode search( final Region key )
	{
		DictNode node = head;

		do
		{
			node = node.next;
		}
		while ( node.key != null && !( lessOrEqual( key, node.key ) ) );

		return node;
	}

	void delete( final DictNode node )
	{
		node.next.prev = node.prev;
		node.prev.next = node.next;
	}

	DictNode max()
	{
		return head.prev;
	}

	DictNode min()
	{
		return head.next;
	}

	@NotNull
	DictNode insertBefore( DictNode node, final Region key )
	{
		do
		{
			node = node.prev;
		}
		while ( node.key != null && !lessOrEqual( node.key, key ) );

		final DictNode result = new DictNode();
		result.key = key;
		result.next = node.next;
		node.next.prev = result;
		result.prev = node;
		node.next = result;

		return result;
	}

	@NotNull
	DictNode insert( final Region key )
	{
		return insertBefore( head, key );
	}

	void clear()
	{
		head = new DictNode();
		head.key = null;
		head.next = head;
		head.prev = head;
	}

	static class DictNode
	{
		Region key;
		DictNode next;
		DictNode prev;
	 }
}
