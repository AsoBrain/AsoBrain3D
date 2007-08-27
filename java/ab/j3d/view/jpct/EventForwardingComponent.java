/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
 * ====================================================================
 */
package ab.j3d.view.jpct;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;

/**
 * Component that forwards any calls to add or remove mouse listeners,
 * mouse motion listeners and key listeners to the components it contains. This
 * component is needed because events on a heavyweight component don't 'fall
 * through' to the underlying container if the component is disabled (as is the
 * case with lightweights).
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
class EventForwardingComponent
	extends JComponent
{
	/**
	 * Constructs a new component.
	 */
	protected EventForwardingComponent()
	{
	}

	public void addMouseListener( final MouseListener l )
	{
		super.addMouseListener( l );
		for ( final Component component : getComponents() )
		{
			component.addMouseListener( l );
		}
	}

	public void removeMouseListener( final MouseListener l )
	{
		super.removeMouseListener( l );
		for ( final Component component : getComponents() )
		{
			component.removeMouseListener( l );
		}
	}

	public void addMouseMotionListener( final MouseMotionListener l )
	{
		super.addMouseMotionListener( l );
		for ( final Component component : getComponents() )
		{
			component.addMouseMotionListener( l );
		}
	}

	public void removeMouseMotionListener( final MouseMotionListener l )
	{
		super.removeMouseMotionListener( l );
		for ( final Component component : getComponents() )
		{
			component.removeMouseMotionListener( l );
		}
	}

	public void addKeyListener( final KeyListener l )
	{
		super.addKeyListener( l );
		for ( final Component component : getComponents() )
		{
			component.addKeyListener( l );
		}
	}

	public void removeKeyListener( final KeyListener l )
	{
		super.removeKeyListener( l );
		for ( final Component component : getComponents() )
		{
			component.removeKeyListener( l );
		}
	}
}
