/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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

import java.awt.Component;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.model.Node3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;

/**
 * Java 2D implementation of view model.
 *
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dModel
	extends ViewModel
{
	/**
	 * Paint queue.
	 */
	private final List _paintQueue = new LinkedList();

	/**
	 * Construct new Java 2D view model.
	 */
	public Java2dModel()
	{
	}

	public void createNode( final Object id , final Node3D node3D )
	{
		removeNode( id );
		if ( node3D != null )
			addNode( new Java2dNode( id , node3D ) );
	}

	protected void addNode( final ViewModelNode node )
	{
		super.addNode( node );
		updatePaintQueue();
		updateViews();
	}

	public void removeNode( final Object id )
	{
		super.removeNode( id );
		updatePaintQueue();
		updateViews();
	}

	public void updateNode( final Object id )
	{
		super.updateNode( id );

		updatePaintQueue();
		updateViews();
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		final Java2dView view = new Java2dView( this , id , viewControl );
		addView( view );
		return view.getComponent();
	}

	/**
	 * Update the paint queue.
	 */
	private void updatePaintQueue()
	{
		_paintQueue.clear();
		final List nodeIDs = getNodeIDs();
		for ( int i = 0 ; i < nodeIDs.size() ; i++ )
		{
			final Object     nodeID = nodeIDs.get( i );
			final Java2dNode node   = (Java2dNode)getNode( nodeID );

			for ( Iterator paintQueue = node.getPaintQueue() ; paintQueue.hasNext() ; )
				_paintQueue.add( paintQueue.next() );
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
