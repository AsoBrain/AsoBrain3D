/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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
package ab.j3d.control;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class extends {@link ControlInput} by listening to events from a
 * specified (view) component. This basically forwards all input events to
 * the {@link #dispatchControlInputEvent} method.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ComponentControlInput
	extends ControlInput
	implements KeyListener , MouseListener , MouseMotionListener
{
	/**
	 * Construct new component control input.
	 *
	 * @param   component   The component to listen to for events
	 */
	protected ComponentControlInput( final Component component )
	{
		if ( component == null )
			throw new NullPointerException( "components" );

		component.addKeyListener( this );
		component.addMouseListener( this );
		component.addMouseMotionListener( this );
	}

	/**
	 * Simply oass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyPressed( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyReleased( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyTyped( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseClicked( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseDragged( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseEntered( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseExited( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseMoved( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mousePressed( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply oass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseReleased( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}
}
