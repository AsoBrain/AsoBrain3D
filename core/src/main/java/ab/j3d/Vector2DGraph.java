/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d;

/**
 * A {@link Graph} for {@link Vector2D} data, using
 * {@link Vector2D#almostEquals} to test for equality.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
*/
public class Vector2DGraph
	extends Graph<Vector2D>
{
	@Override
	public Node<Vector2D> get( final Vector2D data )
	{
		Node<Vector2D> result = null;

		for ( final Node<Vector2D> node : this )
		{
			if ( ( data == null ) ? ( node.getData() == null ) : data.almostEquals( node.getData() ) )
			{
				result = node;
			}
		}

		return result;
	}
}
