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
package ab.j3d.view.java3d;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;

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
	 * Java 3D universe containing scene.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Content branch graph of the Java 3D scene.
	 */
	private final BranchGroup _contentGraph;

	/**
	 * Map node ID (<code>Object</code>) to Java 3D content graph object
	 * (<code>BranchGroup</code>).
	 */
	private final Map _nodeContentMap = new HashMap();

	/**
	 * Construct new Java 3D model.
	 */
	public Java3dModel()
	{
		this( Java3dUniverse.MM );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   unit    Unit scale factor (e.g. <code>Java3dUniverse.MM</code>).
	 */
	public Java3dModel( final double unit )
	{
		this( new Java3dUniverse( unit ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dUniverse Java 3D universe.
	 */
	public Java3dModel( final Java3dUniverse j3dUniverse )
	{
		_universe     = j3dUniverse;
		_contentGraph = Java3dTools.createDynamicScene( _universe.getContent() );
	}

	public void createNode( final Object id , final Node3D node3D )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		removeNode( id );

		if ( node3D != null )
		{
			final BranchGroup nodeRoot = new BranchGroup();
			nodeRoot.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
			nodeRoot.setCapability( BranchGroup.ALLOW_DETACH );
			_nodeContentMap.put( id , nodeRoot );

			final TransformGroup nodeTransform = new TransformGroup();
			nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );
			nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE );
			nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );
			nodeTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
			nodeRoot.addChild( nodeTransform );

			addNode( new Java3dNode( nodeTransform , id , node3D ) );

			_contentGraph.addChild( nodeRoot );
		}
	}

	public void removeNode( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		if ( nodeRoot != null )
			_contentGraph.removeChild( nodeRoot );

		super.removeNode( id );
	}

	public BranchGroup getJava3dNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		return (BranchGroup)_nodeContentMap.get( id );
	}

	public TransformGroup getJava3dTransform( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		return (TransformGroup)nodeRoot.getChild( 0 );
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

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
