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
package ab.j3d.view.java3d;

import java.awt.Component;
import javax.media.j3d.BranchGroup;

import ab.j3d.model.Node3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModel;

/**
 * View model implementation for Java 3D.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dModel
	extends ViewModel
{
	/**
	 * Shared Java 3D toolbox.
	 */
	private final Java3dTools _j3dTools;

	/**
	 * Java 3D universe containing scene.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Content branch graph of the j3d tree.
	 */
	private final BranchGroup _contentGraph;

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dTools    Java 3D utility toolbox.
	 */
	public Java3dModel( final Java3dTools j3dTools )
	{
		this( j3dTools , Java3dUniverse.MM );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dTools    Java 3D utility toolbox.
	 * @param   unit        Unit scale factor (e.g. <code>Java3dUniverse.MM</code>).
	 */
	public Java3dModel( final Java3dTools j3dTools , final double unit )
	{
		this( j3dTools , new Java3dUniverse( unit ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dUniverse Java 3D universe.
	 * @param   j3dTools    Java 3D utility toolbox.
	 */
	public Java3dModel( final Java3dTools j3dTools , final Java3dUniverse j3dUniverse )
	{
		_j3dTools = j3dTools;
		_universe = j3dUniverse;
		_contentGraph = Java3dTools.createDynamicScene( _universe.getContent() );
	}

	public void createNode( final Object id , final Node3D node3D )
	{
		addNode( new Java3dNode( _j3dTools , _contentGraph , id , node3D ) );
	}

	public void removeNode( final Object id )
	{
		final Java3dNode node = (Java3dNode)getNode( id );
		if ( node != null )
			_contentGraph.removeChild( node.getSceneGraphObject() );

		super.removeNode( id );
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		final Java3dView view = new Java3dView( _universe , id , viewControl );
		addView( view );
		return view.getComponent();
	}

	/**
	 * Get Java 3D universe containing scene.
	 *
	 * @return  Java 3D universe containing scene.
	 */
	public Java3dUniverse getUniverse()
	{
		return _universe;
	}
}
