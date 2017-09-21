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
 * A {@link Graph} for {@link Vector3D} data, using
 * {@link Vector3D#almostEquals} to test for equality.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
*/
public class Vector3DGraph
	extends Graph<Vector3D>
{
	@Override
	public Node<Vector3D> get( final Vector3D data )
	{
		Node<Vector3D> result = null;

		for ( final Node<Vector3D> node : this )
		{
			if ( ( data == null ) ? ( node.getData() == null ) : data.almostEquals( node.getData() ) )
			{
				result = node;
			}
		}

		return result;
	}
}
