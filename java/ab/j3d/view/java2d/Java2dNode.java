/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
package ab.j3d.view.java2d;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModelNode;

/**
 * Java 2D implementation of view model node.
 *
 * @see     Java2dModel
 * @see     ViewModelNode
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dNode
	extends ViewModelNode
{
	/**
	 * Paint queue.
	 */
	private final List _paintQueue = new LinkedList();

	/**
	 * Construct new view model node.
	 *
	 * @param   id                  Application-assigned ID of this node.
	 * @param   node3D              Root in the 3D scene.
	 * @param   textureOverride     Texture to use instead of actual textures.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).
	 */
	Java2dNode( final Object id , final Node3D node3D , final TextureSpec textureOverride , final float opacity )
	{
		super( id , node3D , textureOverride , opacity );
	}

	public void update()
	{
		final Node3DCollection leafs = new Node3DCollection();
		final Node3D node3D = getNode3D();
		node3D.gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		// update paint queue....
		// @FIXME How to determine 'smart' paint order....
		_paintQueue.clear();
		for ( int i = 0 ; i < leafs.size() ; i++ )
		{
			_paintQueue.add( leafs.getNode( i ) );
		}
	}

	/**
	 * Get paint queue iterator.
	 *
	 * @return  Paint queue iterator.
	 */
	public Iterator getPaintQueue()
	{
		return _paintQueue.iterator();
	}
}
