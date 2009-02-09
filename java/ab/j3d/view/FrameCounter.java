/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.util.concurrent.TimeUnit;

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
	private long _start   = 0L;

	/**
	 * Number of frames rendered so far during the current second.
	 */
	private int _counter = 0;

	/**
	 * Total number of frames rendered during the previous second.
	 */
	private int _value = 0;

	/**
	 * Constructs a new frame counter.
	 */
	public FrameCounter()
	{
		_start = System.nanoTime();
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
		if ( TimeUnit.NANOSECONDS.toSeconds( System.nanoTime() - _start ) > 0L )
		{
			_start += TimeUnit.SECONDS.toNanos( 1L );
			_value = _counter;
		}
	}
}
