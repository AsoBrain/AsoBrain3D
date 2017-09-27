/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2017 Peter S. Heijnen
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
 */
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a node in a 3D scene graph. It can be used directly as
 * a collection or merge point. Actual scene graph functionality is implemented
 * by sub-classes.
 * <p>
 * This class implements basic functionality for graph manipulation and
 * traversal.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 */
public class Node3D
{
	/**
	 * Collection of children of this node.
	 */
	@NotNull
	private final List<Node3D> _children;

	/**
	 * Tag of this node.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @see     #getTag
	 * @see     #setTag
	 */
	@Nullable
	private Object _tag;

	/**
	 * Construct empty tree node.
	 */
	public Node3D()
	{
		_children = new ArrayList<Node3D>();
		_tag = null;
	}

	/**
	 * Invalidate any cached data in this node.
	 */
	protected void invalidateCache()
	{
	}

	/**
	 * Get tag that was set by {@link #setTag}.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @return  Tag that was set by setTag().
	 *
	 * @see     #setTag
	 */
	@Nullable
	public Object getTag()
	{
		final Map<String, Object> propertyMap = getPropertyMap();
		return propertyMap == null ? _tag : propertyMap.get( "tag" );
	}

	/**
	 * Set tag of this node.
	 * <p />
	 * A tag is an application-assigned object that is typically used to link
	 * application objects to scene graph objects. It is not used in any way
	 * by the AsoBrain 3D Toolkit classes.
	 *
	 * @param   tag     Tag of node.
	 *
	 * @see     #getTag
	 */
	public void setTag( @Nullable final Object tag )
	{
		final Map<String, Object> propertyMap = getPropertyMap();
		if ( propertyMap == null )
		{
			_tag = tag;
		}
		else if ( tag == null )
		{
			removeProperty( "tag" );
		}
		else
		{
			setProperty( "tag", String.valueOf( tag ) );
		}
	}

	/**
	 * Returns the specified property.
	 *
	 * @param name Property to get.
	 *
	 * @return Property value.
	 */
	@Nullable
	public Object getProperty( @NotNull final String name )
	{
		return getProperties().get( name );
	}

	/**
	 * Returns the specified string property.
	 *
	 * @param name Property to get.
	 * @param defaultValue Return value if the property is not set.
	 *
	 * @return Property value.
	 */
	public String getProperty( @NotNull final String name, final String defaultValue )
	{
		final Object value = getProperty( name );
		return value instanceof String ? (String)value : defaultValue;
	}

	/**
	 * Returns the node properties.
	 *
	 * @return Node properties.
	 */
	@NotNull
	public Map<String, Object> getProperties()
	{
		final Map<String, Object> result;
		final Map<String, Object> propertyMap = getPropertyMap();
		if ( propertyMap != null )
		{
			result = propertyMap;
		}
		else
		{
			final Object tag = getTag();
			result = tag == null ? Collections.<String, Object>emptyMap() : Collections.singletonMap( "tag", tag );
		}
		return result;
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final int value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, Integer.valueOf( value ) );
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final double value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, Double.valueOf( value ) );
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final String value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, value );
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final Vector3D value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, value );
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final Matrix3D value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, value );
	}

	/**
	 * Sets a node property.
	 *
	 * @param key   Property to set.
	 * @param value Property value.
	 */
	public void setProperty( @NotNull final String key, final boolean value )
	{
		final Map<String, Object> properties = getPropertyMapNotNull();
		properties.put( key, Boolean.valueOf( value ) );
	}

	/**
	 * Removes a node property.
	 *
	 * @param key Property to remove.
	 */
	public void removeProperty( @NotNull final String key )
	{
		final Map<String, Object> propertyMap = getPropertyMapNotNull();
		propertyMap.remove( key );
	}

	/**
	 * Returns the property map for this node. If no properties were set, the
	 * result is {@code null}.
	 *
	 * @return Property map.
	 */
	@Nullable
	private Map<String, Object> getPropertyMap()
	{
		final Object tag = _tag;
		return tag instanceof Map ? (Map<String, Object>)tag : null;
	}

	/**
	 * Returns the property map for this node. If no properties were set, a new
	 * property map is created containing only the current tag object (if set).
	 *
	 * @return Property map.
	 */
	@NotNull
	private Map<String, Object> getPropertyMapNotNull()
	{
		final Object tag = _tag;
		final Map<String, Object> result;
		if ( tag instanceof Map )
		{
			result = (Map<String, Object>)tag;
		}
		else
		{
			result = new HashMap<String, Object>();
			if ( tag != null )
			{
				result.put( "tag", tag );
			}
			_tag = result;
		}
		return result;
	}

	/**
	 * Get child node with the specified index.
	 *
	 * @param   index   Index of child node.
	 *
	 * @return  Child node with specified index.
	 *
	 * @throws  IndexOutOfBoundsException if the index is out of range.
	 *
	 * @see     #getChildCount
	 */
	public Node3D getChild( final int index )
	{
		return _children.get( index );
	}

	/**
	 * Get number of child nodes of this node.
	 *
	 * @return  Number of child nodes (0 => none => leaf node).
	 *
	 * @see     #getChild
	 * @see     #isLeaf
	 */
	public int getChildCount()
	{
		return _children.size();
	}

	/**
	 * Returns the collection of children of this node.
	 *
	 * @return  Children of this node.
	 */
	public List<Node3D> getChildren()
	{
		return new ArrayList<Node3D>( _children );
	}

	/**
	 * This method returns <code>true</code> if this node is a leaf node
	 * (it has no children).
	 *
	 * @return  <code>true</code> if this node is a leaf node,
	 *          <code>false</code> otherwise.
	 *
	 * @see     #getChildCount
	 */
	public boolean isLeaf()
	{
		return _children.isEmpty();
	}

	/**
	 * Add a child node to this node.
	 *
	 * @param   node    Node to add as a child.
	 *
	 * @see     #removeChild
	 */
	public void addChild( @NotNull final Node3D node )
	{
		final List<Node3D> children = _children;
		if ( children.contains( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		children.add( node );

		invalidateCache();
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Node3D... nodes )
	{
		final List<Node3D> children = _children;
		boolean nodeAdded = false;
		for ( final Node3D node : nodes )
		{
			children.add( node );
			nodeAdded = true;
		}

		if ( nodeAdded )
		{
			invalidateCache();
		}
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Collection<? extends Node3D> nodes )
	{
		if ( !nodes.isEmpty() )
		{
			final List<Node3D> children = _children;
			children.addAll( nodes );
			invalidateCache();
		}
	}

	/**
	 * Add child nodes to this node.
	 *
	 * @param   nodes   Nodes to add as a child.
	 */
	public void addChildren( @NotNull final Iterable<? extends Node3D> nodes )
	{
		final List<Node3D> children = _children;
		boolean nodeAdded = false;
		for ( final Node3D node : nodes )
		{
			children.add( node );
			nodeAdded = true;
		}

		if ( nodeAdded )
		{
			invalidateCache();
		}
	}

	/**
	 * Remove a child node from this node.
	 *
	 * @param   node    Child node to remove.
	 *
	 * @see     #addChild
	 */
	public void removeChild( @NotNull final Node3D node )
	{
		if ( !_children.remove( node ) )
		{
			throw new IllegalArgumentException( node.toString() );
		}

		invalidateCache();
	}

	/**
	 * Remove all child nodes from this node.
	 *
	 * @see     #removeChild
	 */
	public void removeAllChildren()
	{
		if ( !_children.isEmpty() )
		{
			_children.clear();
			invalidateCache();
		}
	}

	/**
	 * Calculate combined bounds all objects starting at this node.
	 *
	 * @param   transform   Transformation to apply to this node.
	 *
	 * @return  Calculated bounds;
	 *          <code>null</code> if bounds could not be determined.
	 */
	@Nullable
	public Bounds3D calculateBounds( final Matrix3D transform )
	{
		final Bounds3DBuilderVisitor worker = new Bounds3DBuilderVisitor();
		Node3DTreeWalker.walk( worker, transform, this );
		return worker.getBounds();
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{tag=" + getTag() + '}';
	}
}
