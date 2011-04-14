/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * A simple directed graph with a point in 3D space associated with each node.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class Graph
	implements Iterable<Graph.Node>
{
	/**
	 * Nodes in the graph.
	 */
	private List<Node> _nodes;

	/**
	 * Constructs a new graph.
	 */
	public Graph()
	{
		_nodes = new ArrayList<Node>();
	}

	@Override
	public Iterator<Node> iterator()
	{
		return _nodes.iterator();
	}

	/**
	 * Adds the given node to the graph.
	 *
	 * @param   node    Node to be added.
	 */
	public void add( @NotNull final Node node )
	{
		_nodes.add( node );
	}

	/**
	 * Returns a node near the given point, if any.
	 *
	 * @param   point   Point to get a node for.
	 *
	 * @return  Node near <code>point</code>;
	 *          <code>null</code> if there is no such node in the graph.
	 */
	@Nullable
	public Node get( @NotNull final Vector3D point )
	{
		Node result = null;
		for ( final Node node : _nodes )
		{
			if ( point.almostEquals( node.getPoint() ) )
			{
				result = node;
			}
		}
		return result;
	}

	/**
	 * Returns a node near the given point, creating a new node at that
	 * point if no nearby point exists in the graph.
	 *
	 * @param   point   Point to get a node for.
	 *
	 * @return  Node near <code>point</code>.
	 */
	@NotNull
	public Node getOrAdd( @NotNull final Vector3D point )
	{
		Node node = get( point );
		if ( node == null )
		{
			node = new Node( point );
			add( node );
		}
		return node;
	}

	/**
	 * Represents a node in the graph.
	 */
	static class Node
	{
		/**
		 * Point that the node represents.
		 */
		private final Vector3D _point;

		/**
		 * Set of nodes that this node is connected to.
		 */
		private Set<Node> _connected = Collections.emptySet();

		/**
		 * Constructs a new node
		 *
		 * @param   point   Point to create a node for.
		 */
		Node( final Vector3D point )
		{
			_point = point;
		}

		/**
		 * Set of nodes that are connected to this node.
		 *
		 * @return  Connected nodes.
		 */
		public Set<Node> getConnected()
		{
			return Collections.unmodifiableSet( _connected );
		}

		/**
		 * Point that the node represents.
		 *
		 * @return  Point.
		 */
		public Vector3D getPoint()
		{
			return _point;
		}

		/**
		 * Connects the node to another node, forming an edge.
		 *
		 * @param   other   Node to connect to.
		 */
		public void connect( final Node other )
		{
			Set<Node> connected = _connected;
			if ( connected.isEmpty() )
			{
				connected = new HashSet<Node>();
				_connected = connected;
			}
			connected.add( other );
		}

		/**
		 * Disconnects the node from another node, removing the edge between
		 * the two nodes.
		 *
		 * @param   other   Node to disconnect from.
		 */
		public void disconnect( final Node other )
		{
			if ( !_connected.isEmpty() )
			{
				_connected.remove( other );
			}
		}
	}
}
