/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This visitor collects collects paths to visited nodes of a specific type.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Node3DCollector
	implements Node3DVisitor
{
	/**
	 * List with paths to collected nodes.
	 */
	@NotNull
	private final List<Node3DPath> _collectedNodes;

	/**
	 * Type of nodes that will be collected (<code>null</code> if any).
	 */
	@Nullable
	private final Class<? extends Node3D> _collectedNodeType;

	/**
	 * Construct collector.
	 *
	 * @param   collectedNodeType   Type of nodes that will be collected
	 *                              (<code>null</code> if any).
	 */
	public Node3DCollector( @Nullable final Class<? extends Node3D> collectedNodeType )
	{
		this( new ArrayList<Node3DPath>(), collectedNodeType );
	}

	/**
	 * Construct collector.
	 *
	 * @param   collectedNodes      Target collection for collected nodes.
	 * @param   collectedNodeType   Type of nodes that will be collected
	 *                              (<code>null</code> if any).
	 */
	public Node3DCollector( final List<Node3DPath> collectedNodes, @Nullable final Class<? extends Node3D> collectedNodeType )
	{
		_collectedNodeType = collectedNodeType;
		_collectedNodes = collectedNodes;
	}

	/**
	 * Get collection with paths to collected nodes.
	 *
	 * @return  Paths to collected nodes.
	 */
	@NotNull
	public List<Node3DPath> getCollectedNodes()
	{
		return _collectedNodes;
	}

	@Override
	public boolean visitNode( @NotNull final Node3DPath path )
	{
		final boolean typeMatched = ( _collectedNodeType == null ) || _collectedNodeType.isInstance( path.getNode() );
		return !typeMatched || collectNode( path );
	}

	/**
	 * This method is called by {@link #visitNode} to collect a node.
	 *
	 * @param   path    Path to the node to collect.
	 *
	 * @return  <code>true</code> if the tree walk should continue;
	 *          <code>false</code> if the tree walk should be aborted.
	 */
	protected boolean collectNode( final Node3DPath path )
	{
		_collectedNodes.add( path );
		return true;
	}
}