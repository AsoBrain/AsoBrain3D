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
package ab.j3d.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import ab.j3d.Matrix3D;

/**
 * This interface is used to control a view in the view model.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class ViewControl
{
	/**
	 * View control event (reused to avoid too much garbage).
	 */
	protected final PropertyChangeSupport _pcs = new PropertyChangeSupport( this );

	/**
	 * Add a <code>PropertyChangeListener</code> to the listener list. The
	 * listener is registered for all properties.
	 *
	 * @param   listener    Listener to be added
	 */
	public final void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( listener );
	}

	/**
	 * Remove a <code>PropertyChangeListener</code> from the listener list. This
	 * removes a listener that was registered for all properties.
	 *
	 * @param   listener    Listener to be removed
	 */
	public final void removePropertyChangeListener( final PropertyChangeListener listener )
	{
		_pcs.removePropertyChangeListener( listener );
	}

	/**
	 * Add a <code>PropertyChangeListener</code> for a specific property. The
	 * listener will be invoked only when a that specific property is changed.
	 *
	 * @param   propertyName    Name of the property to listen on.
	 * @param   listener        Listener to be added
	 */
	public final void addPropertyChangeListener( final String propertyName , final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( propertyName , listener );
	}

	/**
	 * Remove a <code>PropertyChangeListener</code> for a specific property.
	 *
	 * @param   propertyName    Name of the property that was listened on.
	 * @param   listener        Listener to be removed
	 */
	public final void removePropertyChangeListener( final String propertyName , final PropertyChangeListener listener )
	{
		_pcs.removePropertyChangeListener( propertyName , listener );
	}

	/**
	 * Get view transform.
	 *
	 * @return  View transform.
	 */
	public abstract Matrix3D getTransform();
}
