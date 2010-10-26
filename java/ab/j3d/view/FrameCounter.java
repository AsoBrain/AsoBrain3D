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
package ab.j3d.view;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.event.*;

/**
 * Provides a frame count per second.
 *
 * <p>
 * This implementation is not thread-safe.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class FrameCounter
{
	/**
	 * Value of {@link System#nanoTime()} when the current second started.
	 */
	private long _start = 0L;

	/**
	 * Number of frames rendered so far during the current second.
	 */
	private int _counter = 0;

	/**
	 * Total number of frames rendered during the previous second.
	 */
	private int _value = 0;

	/**
	 * Listeners to be notified when the frame counter value is updated.
	 */
	private final List<ChangeListener> _listeners;

	/**
	 * Constructs a new frame counter.
	 */
	public FrameCounter()
	{
		_start = System.nanoTime();
		_listeners = new ArrayList<ChangeListener>();
	}

	/**
	 * Increments the frame counter, adding one to the number of frames rendered
	 * this second.
	 */
	public void increment()
	{
		_counter++;
	}

	/**
	 * Returns the number of frames rendered during the previous second.
	 *
	 * @return  Number of frames rendered.
	 */
	public int get()
	{
		update();
		return _value;
	}

	/**
	 * Check whether the current second has passed and, if so, updates the value
	 * of the frame counter.
	 */
	private void update()
	{
		final int secondsPassed = (int)TimeUnit.NANOSECONDS.toSeconds( System.nanoTime() - _start );
		if ( secondsPassed > 0 )
		{
			_start += TimeUnit.SECONDS.toNanos( (long)secondsPassed );
			_value = _counter / secondsPassed;
			_counter = 0;
			fireChangeEvent();
		}
	}

	/**
	 * Adds a listener. The listener will be notified when the frame counter
	 * value is updated.
	 *
	 * @param   changeListener  Change listener to be added.
	 */
	public void addChangeListener( final ChangeListener changeListener )
	{
		_listeners.add( changeListener );
	}

	/**
	 * Removes a listener.
	 *
	 * @param   changeListener  Change listener to be added.
	 */
	public void removeChangeListener( final ChangeListener changeListener )
	{
		_listeners.remove( changeListener );
	}

	/**
	 * Notifies all listeners.
	 */
	protected void fireChangeEvent()
	{
		if ( !_listeners.isEmpty() )
		{
			final ChangeEvent event = new ChangeEvent( this );
			for ( final ChangeListener listener : _listeners )
			{
				listener.stateChanged( event );
			}
		}
	}
}
