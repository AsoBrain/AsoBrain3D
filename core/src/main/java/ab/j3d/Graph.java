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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * A simple directed graph with a data associated with each node.
 *
 * @param   <T> Data type.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class Graph<T>
	implements Iterable<Graph.Node<T>>
{
	/**
	 * Nodes in the graph.
	 */
	private final Map<T,Node<T>> _nodeMap = new HashMap<T,Node<T>>();

	public Iterator<Node<T>> iterator()
	{
		final Collection<Node<T>> nodes = getNodes();
		return nodes.iterator();
	}

	/**
	 * Test whether the graph contains any nodes.
	 *
	 * @return  {@code true} if graph contains no nodes;
	 *          {@code false} if graph contains at least one node.
	 */
	public boolean isEmpty()
	{
		return _nodeMap.isEmpty();
	}

	/**
	 * Get nodes in this graph.
	 *
	 * @return  Collection with all nodes in this graph.
	 */
	public Collection<Node<T>> getNodes()
	{
		return _nodeMap.values();
	}

	/**
	 * Adds the given node to the graph.
	 *
	 * @param   node    Node to be added.
	 */
	public void add( final Node<T> node )
	{
		final T data = node.getData();
		if ( _nodeMap.put( data, node ) != null )
		{
			throw new IllegalArgumentException( "Duplicate node for data: " + data );
		}
	}

	/**
	 * Returns a node near the given data, if any.
	 *
	 * @param   data   Data to get a node for.
	 *
	 * @return  Node<T> near <code>data</code>;
	 *          <code>null</code> if there is no such node in the graph.
	 */
	public Node<T> get( final T data )
	{
		return _nodeMap.get( data );
	}

	/**
	 * Returns a node near the given data, creating a new node at that
	 * data if no nearby data exists in the graph.
	 *
	 * @param   data   Data to get a node for.
	 *
	 * @return  Node<T> near <code>data</code>.
	 */
	@NotNull
	public Node<T> getOrAdd( final T data )
	{
		Node<T> result = get( data );
		if ( result == null )
		{
			result = new Node<T>( data );
			add( result );
		}
		return result;
	}

	/**
	 * Represents a node in the graph.
	 */
	public static class Node<T>
	{
		/**
		 * Data that the node represents.
		 */
		private final T _data;

		/**
		 * Set of nodes that this node is connected to.
		 */
		private Set<Node<T>> _connected = Collections.emptySet();

		/**
		 * Constructs a new node.
		 *
		 * @param   data   Data to create a node for.
		 */
		public Node( final T data )
		{
			_data = data;
		}

		/**
		 * Test whether this node is a sink. A sink is a node that has no
		 * outgoing connections (outdegree is zero).
		 *
		 * @return  {@code true} node is a sink.
		 */
		public boolean isSink()
		{
			return _connected.isEmpty();
		}

		/**
		 * Set of nodes that are connected to this node.
		 *
		 * @return  Connected nodes.
		 */
		public Set<Node<T>> getConnected()
		{
			return Collections.unmodifiableSet( _connected );
		}

		/**
		 * Data that the node represents.
		 *
		 * @return  Data.
		 */
		public T getData()
		{
			return _data;
		}

		/**
		 * Connects the node to another node, forming an edge.
		 *
		 * @param   other   Node<T> to connect to.
		 */
		public void connect( final Node<T> other )
		{
			Set<Node<T>> connected = _connected;
			if ( ! ( connected instanceof HashSet ) )
			{
				connected = new HashSet<Node<T>>();
				_connected = connected;
			}

			if ( !connected.add( other ) )
			{
				throw new IllegalArgumentException( this + " was already connected to " + other );
			}
		}

		/**
		 * Disconnects the node from another node, removing the edge between
		 * the two nodes.
		 *
		 * @param   other   Node<T> to disconnect from.
		 */
		public void disconnect( final Node<T> other )
		{
			final Set<Node<T>> connected = _connected;
			if ( connected.isEmpty() )
			{
				throw new IllegalArgumentException( this + " is not connected to any node" );
			}

			if ( !connected.remove( other ) )
			{
				throw new IllegalArgumentException( this + " was not connected to " + other );
			}
		}
	}
}
