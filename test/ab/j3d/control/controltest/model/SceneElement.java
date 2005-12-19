/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
package ab.j3d.control.controltest.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import ab.j3d.Matrix3D;

/**
 * Each element in a {@link Model} has to be a subclass of this class, so that
 * each object in the scene has support for {@link PropertyChangeListener}s and
 * has a {@link Matrix3D} with the transformation of the object.
 * {@link PropertyChangeListener}s can be added to be notified when the element
 * changes.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class SceneElement
{
	/**
	 * {@link PropertyChangeListener}s should listen to this property when they
	 * wish to be notified about changes to an element.
	 *
	 * @see #addPropertyChangeListener
	 * @see #removePropertyChangeListener
	 */
	public static final String ELEMENT_CHANGED = "element changed";

	/**
	 * The PropertyChangeSupport to assist in registering the
	 * {@link PropertyChangeListener}s.
	 */
	private final PropertyChangeSupport _pcs;

	/**
	 * Constructs a new SceneElement.
	 */
	protected SceneElement()
	{
		_pcs = new PropertyChangeSupport( this );
	}

	/**
	 * Returns a {@link Matrix3D} with the transformation of this element. This
	 * should include translation, rotation and scale where needed.
	 *
	 * @return  Transformation matrix of this element.
	 */
	public abstract Matrix3D getTransform();

	/**
	 * Add a {@link PropertyChangeListener} to this model. The listener will be
	 * notified when this element changes.
	 *
	 * @param   p   property to listen for.
	 * @param   l   {@link PropertyChangeListener} to add
	 */
	protected final void addPropertyChangeListener( final String p , final PropertyChangeListener l )
	{
		_pcs.addPropertyChangeListener( p , l );
	}

	/**
	 * Removes a {@link PropertyChangeListener} from this model.
	 *
	 * @param p The property the listener was listening to
	 * @param l The Listener to remove.
	 */
	protected final void removePropertyChangeListener( final String p , final PropertyChangeListener l )
	{
		_pcs.removePropertyChangeListener( p , l );
	}

	/**
	 * Inheriting classes can call this method when all registered
	 * {@link PropertyChangeListener}s need to be notified about element
	 * changes. This method fires a property change event to all listeners
	 * listening for <code>ELEMENT_CHANGED</code> events.
	 *
	 * @see #addPropertyChangeListener
	 * @see #removePropertyChangeListener
	 */
	protected final void elementChanged()
	{
		_pcs.firePropertyChange( ELEMENT_CHANGED , null , this );
	}
}
