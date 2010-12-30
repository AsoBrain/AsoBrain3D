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
package ab.j3d;

/**
 * Describes a time-dependent value, which may be used for animation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Alpha
{
	/**
	 * Time to start the first period at. This time is relative to the
	 * {@link System#currentTimeMillis()} and may be in the past, present,
	 * or future.
	 */
	private long _startAt;

	/**
	 * Length of period. Set zero or less to run forever.
	 */
	private long _length;

	/**
	 * Delay in milliseconds between periods. If set to a negative value,
	 * only one period exists.
	 */
	private long _repeatDelay;

	/**
	 * Value to return before the first period is started.
	 */
	private double _valueBeforeStart;

	/**
	 * Value to return after a period has ended.
	 */
	private double _valueAfterEnd;

	/**
	 * Constant value used to calculate result of {@link #getFunction}.
	 */
	private double _constant;

	/**
	 * Linear factor used to calculate result of {@link #getFunction}.
	 */
	private double _linearFactor;

	/**
	 * Construct alpha with default settings.
	 */
	public Alpha()
	{
		_startAt = 0L;
		_length = 0L;
		_repeatDelay = -1L;
		_valueBeforeStart = 0.0;
		_valueAfterEnd = 0.0;
		_constant = 0.0;
		_linearFactor = 1.0;
	}

	/**
	 * Construct alpha with given settings.
	 *
	 * @param   startAt         Time to start the period at.
	 * @param   length          Length of period in ms (<= 0 if infinite).
	 * @param   repeatDelay     Repeat delay (<0 if not repatitive).
	 */
	public Alpha( final long startAt, final long length, final long repeatDelay )
	{
		this();
		setStartAt( startAt );
		setLength( length );
		setRepeatDelay( repeatDelay );
	}

	/**
	 * Get time to start the first period at. This time is relative to the
	 * {@link System#currentTimeMillis()} and may be in the past, present,
	 * or future.
	 *
	 * @return  Time to start the first period at.
	 */
	public long getStartAt()
	{
		return _startAt;
	}

	/**
	 * Set time to start the first period at. This time is relative to the
	 * {@link System#currentTimeMillis()} and may be in the past, present,
	 * or future.
	 *
	 * @param   time    Time to start the first period at.
	 */
	public void setStartAt( final long time )
	{
		_startAt = time;
	}

	/**
	 * Get length of period. Set zero or less to run forever.
	 *
	 * @return  Length of period in ms (<= 0 if infinite).
	 */
	public long getLength()
	{
		return _length;
	}

	/**
	 * Set length of period. Set zero or less to run forever.
	 *
	 * @param   length  Length of period in ms (<= 0 if infinite).
	 */
	public void setLength( final long length )
	{
		_length = length;
	}

	/**
	 * Get delay in milliseconds between periods. If set to a negative
	 * value, only one period exists.
	 *
	 * @return  Repeat delay (<0 if not repatitive).
	 */
	public long getRepeatDelay()
	{
		return _repeatDelay;
	}

	/**
	 * Set delay in milliseconds between periods. If set to a negative
	 * value, only one period exists.
	 *
	 * @param   delay   Repeat delay (<0 if not repatitive).
	 */
	public void setRepeatDelay( final long delay )
	{
		_repeatDelay = delay;
	}

	/**
	 * Get value to return before the first period is started.
	 *
	 * @return  Value to return before the first period is started.
	 */
	public double getValueBeforeStart()
	{
		return _valueBeforeStart;
	}

	/**
	 * Set value to return before the first period is started.
	 *
	 * @param   value   Value to return before the first period is started.
	 */
	public void setValueBeforeStart( final double value )
	{
		_valueBeforeStart = value;
	}

	/**
	 * Get value to return after a period has ended.
	 *
	 * @return  Value to return after a period has ended.
	 */
	public double getValueAfterEnd()
	{
		return _valueAfterEnd;
	}

	/**
	 * Set value to return after a period has ended.
	 *
	 * @param   value   Value to return after a period has ended.
	 */
	public void setValueAfterEnd( final double value )
	{
		_valueAfterEnd = value;
	}

	/**
	 * Get constant value used to calculate result of {@link #getFunction}.
	 *
	 * @return  Constant value.
	 */
	public double getConstant()
	{
		return _constant;
	}

	/**
	 * Set constant value used to calculate result of {@link #getFunction}.
	 *
	 * @param   constant    Constant value.
	 */
	public void setConstant( final double constant )
	{
		_constant = constant;
	}

	/**
	 * Get linear factor used to calculate result of {@link #getFunction}.
	 *
	 * @return  Linear factor.
	 */
	public double getLinearFactor()
	{
		return _linearFactor;
	}

	/**
	 * Set linear factor used to calculate result of {@link #getFunction}.
	 *
	 * @param   factor  Linear factor.
	 */
	public void setLinearFactor( final double factor )
	{
		_linearFactor = factor;
	}

	/**
	 * Get value based on current time.
	 *
	 * @return  Value.
	 */
	public double get()
	{
		final double result;

		long time = getTime();
		if ( time < 0L )
		{
			result = getValueBeforeStart();
		}
		else
		{
			final long length = getLength();
			if ( length <= 0L ) /* infinite */
			{
				result = getFunction( time, 0.0 );
			}
			else if ( time <= length ) /* initial period */
			{
				result = getFunction( time, (double) time / (double) length );
			}
			else
			{
				final long repeatDelay = getRepeatDelay();
				if ( repeatDelay < 0L ) /* after one-shot */
				{
					result = getValueAfterEnd();
				}
				else /* repeating */
				{
					time = time % ( length + repeatDelay );
					if ( time <= length ) /* in period */
					{
						result = getFunction( time, (double) time / (double) length );
					}
					else /* repeat delay */
					{
						result = getValueAfterEnd();
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get value based on time-dependent function. If the length is set to zero
	 * or less, the <code>position</code> is always 0.0.
	 *
	 * @param   time        Time since start of period in ms.
	 * @param   position    Relative position in period (between 0.0 and 1.0).
	 *
	 * @return  Value to return.
	 */
	public double getFunction( final long time, final double position )
	{
		return getConstant() + position * getLinearFactor();
	}

	/**
	 * Get time relative to the start of the first period.
	 *
	 * @return  Time relative to the start of the first period in ms.
	 */
	protected long getTime()
	{
		return System.currentTimeMillis() - _startAt;
	}
}
