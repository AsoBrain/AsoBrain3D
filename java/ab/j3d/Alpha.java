/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2014 Peter S. Heijnen
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
package ab.j3d;

import static java.lang.Math.*;
import org.jetbrains.annotations.*;

/**
 * Describes a time-dependent value, which may be used for animation.
 *
 * @author Peter S. Heijnen
 */
public class Alpha
{
	/**
	 * Linear interpolation function.
	 */
	public static final InterpolationFunction LINEAR = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return linearFunction( time );
		}
	};

	/**
	 * "Quadratic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUADRATIC_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quadraticInFunction( time );
		}
	};

	/**
	 * "Quadratic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUADRATIC_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quadraticOutFunction( time );
		}
	};

	/**
	 * "Quadratic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUADRATIC_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quadraticInOutFunction( time );
		}
	};

	/**
	 * "Cubic" tweening function from Robert Penner's "Programming Macromedia Flash
	 * MX" (2002).
	 */
	public static final InterpolationFunction CUBIC_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return cubicInFunction( time );
		}
	};

	/**
	 * "Cubic" tweening function from Robert Penner's "Programming Macromedia Flash
	 * MX" (2002).
	 */
	public static final InterpolationFunction CUBIC_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return cubicOutFunction( time );
		}
	};

	/**
	 * "Cubic" tweening function from Robert Penner's "Programming Macromedia Flash
	 * MX" (2002).
	 */
	public static final InterpolationFunction CUBIC_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return cubicInOutFunction( time );
		}
	};

	/**
	 * "Quartic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUARTIC_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quarticInFunction( time );
		}
	};

	/**
	 * "Quartic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUARTIC_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quarticOutFunction( time );
		}
	};

	/**
	 * "Quartic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUARTIC_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quarticInOutFunction( time );
		}
	};

	/**
	 * "Quintic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUINTIC_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quinticInFunction( time );
		}
	};

	/**
	 * "Quintic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUINTIC_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quinticOutFunction( time );
		}
	};

	/**
	 * "Quintic" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction QUINTIC_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return quinticInOutFunction( time );
		}
	};

	/**
	 * "Exponential" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction EXPONENTIAL_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return exponentialInFunction( time );
		}
	};

	/**
	 * "Exponential" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction EXPONENTIAL_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return exponentialOutFunction( time );
		}
	};

	/**
	 * "Exponential" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction EXPONENTIAL_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return exponentialInOutFunction( time );
		}
	};

	/**
	 * "Circular" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction CIRCULAR_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return circularInFunction( time );
		}
	};

	/**
	 * "Circular" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction CIRCULAR_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return circularOutFunction( time );
		}
	};

	/**
	 * "Circular" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction CIRCULAR_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return circularInOutFunction( time );
		}
	};

	/**
	 * "Sinusoidal" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction SINUSIODAL_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return sinusoidalInFunction( time );
		}
	};

	/**
	 * "Sinusoidal" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction SINUSIODAL_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return sinusoidalOutFunction( time );
		}
	};

	/**
	 * "Sinusoidal" tweening function from Robert Penner's "Programming Macromedia
	 * Flash MX" (2002).
	 */
	public static final InterpolationFunction SINUSIODAL_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return sinusoidalInOutFunction( time );
		}
	};

	/**
	 * "Back" tweening function.
	 */
	public static final InterpolationFunction BACK_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return backInFunction( time );
		}
	};

	/**
	 * "Back" tweening function.
	 */
	public static final InterpolationFunction BACK_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return backOutFunction( time );
		}
	};

	/**
	 * "Back" tweening function.
	 */
	public static final InterpolationFunction BACK_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return backInOutFunction( time );
		}
	};

	/**
	 * "Bounce" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction BOUNCE_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return bounceInFunction( time );
		}
	};

	/**
	 * "Bounce" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction BOUNCE_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return bounceOutFunction( time );
		}
	};

	/**
	 * "Bounce" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction BOUNCE_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return bounceInOutFunction( time );
		}
	};

	/**
	 * "Elastic" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction ELASTIC_IN = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return elasticInFunction( time );
		}
	};

	/**
	 * "Elastic" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction ELASTIC_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return elasticOutFunction( time );
		}
	};

	/**
	 * "Elastic" tweening function by Robert Penner.
	 */
	public static final InterpolationFunction ELASTIC_IN_OUT = new InterpolationFunction()
	{
		public double getValue( final double time )
		{
			return elasticInOutFunction( time );
		}
	};

	/**
	 * Time to start the first period at. This time is relative to the {@link
	 * System#currentTimeMillis()} and may be in the past, present, or future.
	 */
	private long _startAt = System.currentTimeMillis();

	/**
	 * Length of period. Set zero or less to run forever.
	 */
	private long _length = 0L;

	/**
	 * Delay in milliseconds between periods. If set to a negative value, only one
	 * period exists.
	 */
	private long _repeatDelay = -1L;

	/**
	 * Value to return before the first period is started.
	 */
	private double _valueBeforeStart = 0.0;

	/**
	 * Value to return after a period has ended.
	 */
	private double _valueAfterEnd = 0.0;

	/**
	 * Constant value used to calculate result of {@link #getFunction}.
	 */
	private double _constant = 0.0;

	/**
	 * Linear factor used to calculate result of {@link #getFunction}.
	 */
	private double _linearFactor = 1.0;

	/**
	 * Interpolation function. This function has an input time scale of 0.0 to 1.0
	 * and an output scale of 0.0 to 1.0. The alpha value is calculated by
	 * multiplying the value of this function by the {@link #getLinearFactor()} and
	 * adding {@link #getConstant()}. By default, the alpha is set to {@link
	 * #LINEAR} interpolation.
	 */
	@NotNull
	private InterpolationFunction _interpolationFunction = LINEAR;

	/**
	 * Construct alpha with default settings.
	 */
	public Alpha()
	{
	}

	/**
	 * Construct alpha with given settings.
	 *
	 * @param startAt     Time to start the period at.
	 * @param length      Length of period in ms (<= 0 if infinite).
	 * @param repeatDelay Repeat delay (<0 if not repatitive).
	 */
	public Alpha( final long startAt, final long length, final long repeatDelay )
	{
		this();
		setStartAt( startAt );
		setLength( length );
		setRepeatDelay( repeatDelay );
	}

	/**
	 * Get time to start the first period at. This time is relative to the {@link
	 * System#currentTimeMillis()} and may be in the past, present, or future.
	 *
	 * @return Time to start the first period at.
	 */
	public long getStartAt()
	{
		return _startAt;
	}

	/**
	 * Set time to start the first period at. This time is relative to the {@link
	 * System#currentTimeMillis()} and may be in the past, present, or future.
	 *
	 * @param time Time to start the first period at.
	 */
	public void setStartAt( final long time )
	{
		_startAt = time;
	}

	/**
	 * Get length of period. Set zero or less to run forever.
	 *
	 * @return Length of period in ms (<= 0 if infinite).
	 */
	public long getLength()
	{
		return _length;
	}

	/**
	 * Set length of period. Set zero or less to run forever.
	 *
	 * @param length Length of period in ms (<= 0 if infinite).
	 */
	public void setLength( final long length )
	{
		_length = length;
	}

	/**
	 * Get delay in milliseconds between periods. If set to a negative value, only
	 * one period exists.
	 *
	 * @return Repeat delay (<0 if not repatitive).
	 */
	public long getRepeatDelay()
	{
		return _repeatDelay;
	}

	/**
	 * Set delay in milliseconds between periods. If set to a negative value, only
	 * one period exists.
	 *
	 * @param delay Repeat delay (<0 if not repatitive).
	 */
	public void setRepeatDelay( final long delay )
	{
		_repeatDelay = delay;
	}

	/**
	 * Get value to return before the first period is started.
	 *
	 * @return Value to return before the first period is started.
	 */
	public double getValueBeforeStart()
	{
		return _valueBeforeStart;
	}

	/**
	 * Set value to return before the first period is started.
	 *
	 * @param value Value to return before the first period is started.
	 */
	public void setValueBeforeStart( final double value )
	{
		_valueBeforeStart = value;
	}

	/**
	 * Get value to return after a period has ended.
	 *
	 * @return Value to return after a period has ended.
	 */
	public double getValueAfterEnd()
	{
		return _valueAfterEnd;
	}

	/**
	 * Set value to return after a period has ended.
	 *
	 * @param value Value to return after a period has ended.
	 */
	public void setValueAfterEnd( final double value )
	{
		_valueAfterEnd = value;
	}

	/**
	 * Get constant value used to calculate result of {@link #getFunction}.
	 *
	 * @return Constant value.
	 */
	public double getConstant()
	{
		return _constant;
	}

	/**
	 * Set constant value used to calculate result of {@link #getFunction}.
	 *
	 * @param constant Constant value.
	 */
	public void setConstant( final double constant )
	{
		_constant = constant;
	}

	/**
	 * Get linear factor used to calculate result of {@link #getFunction}.
	 *
	 * @return Linear factor.
	 */
	public double getLinearFactor()
	{
		return _linearFactor;
	}

	/**
	 * Set linear factor used to calculate result of {@link #getFunction}.
	 *
	 * @param factor Linear factor.
	 */
	public void setLinearFactor( final double factor )
	{
		_linearFactor = factor;
	}

	/**
	 * Get interpolation function. This function has an input time scale of 0.0 to
	 * 1.0 and an output scale of 0.0 to 1.0. The alpha value is calculated by
	 * multiplying the value of this function by the {@link #getLinearFactor()} and
	 * adding {@link #getConstant()}. By default, the alpha is set to {@link
	 * #LINEAR} interpolation.
	 *
	 * @return Interpolation function.
	 */
	@NotNull
	public InterpolationFunction getInterpolationFunction()
	{
		return _interpolationFunction;
	}

	/**
	 * Set interpolation function. This function has an input time scale of 0.0 to
	 * 1.0 and an output scale of 0.0 to 1.0. The alpha value is calculated by
	 * multiplying the value of this function by the {@link #getLinearFactor()} and
	 * adding {@link #getConstant()}. By default, the alpha is set to {@link
	 * #LINEAR} interpolation.
	 *
	 * @param interpolationFunction Interpolation function.
	 */
	public void setInterpolationFunction( @NotNull final InterpolationFunction interpolationFunction )
	{
		_interpolationFunction = interpolationFunction;
	}

	/**
	 * Get value based on current time.
	 *
	 * @return Value.
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
				result = getFunction( 0.0 );
			}
			else if ( time <= length ) /* initial period */
			{
				result = getFunction( (double)time / (double)length );
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
						result = getFunction( (double)time / (double)length );
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
	 * Get value based on time-dependent function. If the length is set to zero or
	 * less, the {@code position} is always 0.0.
	 *
	 * @param time Time parameter (between 0.0 and 1.0).
	 *
	 * @return Value to return.
	 */
	public double getFunction( final double time )
	{
		return getConstant() + getInterpolationFunction().getValue( time ) * getLinearFactor();
	}

	/**
	 * Get time relative to the start of the first period.
	 *
	 * @return Time relative to the start of the first period in ms.
	 */
	protected long getTime()
	{
		return System.currentTimeMillis() - _startAt;
	}

	/**
	 * Interface for interpolation functions.
	 */
	public interface InterpolationFunction
	{
		/**
		 * Get interpolation function value.
		 *
		 * @param time Time parameter (between 0.0 and 1.0).
		 *
		 * @return Interpolation value between 0.0 and 1.0.
		 */
		double getValue( final double time );
	}

	/**
	 * Get result of linear interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double linearFunction( final double time )
	{
		return time;
	}

	/**
	 * Get result of "quadratic in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quadraticInFunction( final double time )
	{
		return time * time;
	}

	/**
	 * Get result of "quadratic out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quadraticOutFunction( final double time )
	{
		return -time * ( time - 2 );
	}

	/**
	 * Get result of "quadratic in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quadraticInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			final double t = time * 2.0;
			return 0.5 * t * t;
		}
		else
		{
			final double t = ( time - 0.5 ) * 2.0;
			return -0.5 * ( t * ( t - 2.0 ) - 1.0 );
		}
	}

	/**
	 * Get result of "cubic in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double cubicInFunction( final double time )
	{
		return time * time * time;
	}

	/**
	 * Get result of "cubic out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double cubicOutFunction( final double time )
	{
		final double t = time - 1.0;
		return ( t * t * t + 1 );
	}

	/**
	 * Get result of "cubic in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double cubicInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			final double t = time * 2.0;
			return 0.5 * t * t * t;
		}
		else
		{
			final double t = time * 2.0 - 1.0;
			return 0.5 * ( t * t * t + 2 );
		}
	}

	/**
	 * Get result of "quartic in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quarticInFunction( final double time )
	{
		return time * time * time * time;
	}

	/**
	 * Get result of "quartic out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quarticOutFunction( final double time )
	{
		final double x = time - 1.0;
		return 1.0 - x * x * x * x;
	}

	/**
	 * Get result of "quartic in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quarticInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			final double x = 2.0 * time;
			return 0.5 * x * x * x * x;
		}
		else
		{
			final double x = 2.0 * ( time - 0.5 );
			return 1.0 - 0.5 * x * x * x * x;
		}
	}

	/**
	 * Get result of "quintic in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quinticInFunction( final double time )
	{
		final double t = time - 1;
		return t * t * t * t * t + 1;
	}

	/**
	 * Get result of "quintic out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quinticOutFunction( final double time )
	{
		return time * time * time * time * time;
	}

	/**
	 * Get result of "quintic in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double quinticInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			final double x = 2.0 * time;
			return 0.5 * x * x * x * x * x;
		}
		else
		{
			final double x = 2.0 * ( time - 0.5 ) - 1.0;
			return 1.0 + 0.5 * x * x * x * x * x;
		}
	}

	/**
	 * Get result of "exponential in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double exponentialInFunction( final double time )
	{
		return ( time == 0 ) ? 0.0 : pow( 2.0, 10.0 * ( time - 1.0 ) );
	}

	/**
	 * Get result of "exponential out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double exponentialOutFunction( final double time )
	{
		return ( time >= 1.0 ) ? 1.0 : 1.0 - pow( 2.0, -10.0 * time );
	}

	/**
	 * Get result of "exponential in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double exponentialInOutFunction( final double time )
	{
		if ( time <= 0 )
		{
			return 0.0;
		}
		else if ( time >= 1.0 )
		{
			return 1.0;
		}
		else if ( time < 0.5 )
		{
			final double t = time * 2.0 - 1.0;
			return 0.5 * pow( 2.0, 10.0 * t );
		}
		else
		{
			final double t = time * 2.0 - 1.0;
			return 0.5 * ( -pow( 2.0, -10.0 * t ) + 2.0 );
		}
	}

	/**
	 * Get result of "circular in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double circularInFunction( final double time )
	{
		return ( 1 - sqrt( 1 - time * time ) );
	}

	/**
	 * Get result of "circular out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double circularOutFunction( final double time )
	{
		final double x = time - 1;
		return sqrt( 1 - x * x );
	}

	/**
	 * Get result of "circular in-out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double circularInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			final double t = time * 2.0;
			return -0.5 * ( sqrt( 1 - t * t ) - 1 );
		}
		else
		{
			final double t = ( time - 1.0 ) * 2.0;
			return 0.5 * ( sqrt( 1 - t * t ) + 1 );
		}
	}

	/**
	 * Get result of "sinusoidal in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double sinusoidalInFunction( final double time )
	{
		return -cos( time * Math.PI / 2 );
	}

	/**
	 * Get result of "sinusoidal out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double sinusoidalOutFunction( final double time )
	{
		return sin( time * Math.PI / 2 );
	}

	/**
	 * Get result of "sinusoidal in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double sinusoidalInOutFunction( final double time )
	{
		return 0.5 * ( 1 - cos( time * Math.PI ) );
	}

	/**
	 * Get result of "back in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double backInFunction( final double time )
	{
		final double overshoot = 1.70158;
		return time * time * ( ( overshoot + 1 ) * time - overshoot );
	}

	/**
	 * Get result of "back out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double backOutFunction( final double time )
	{
		final double overshoot = 1.70158;
		final double x = time - 1.0;
		return 1 + x * x * ( ( overshoot + 1 ) * x + overshoot );
	}

	/**
	 * Get result of "back in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double backInOutFunction( final double time )
	{
		double overshoot = 1.70158;

		if ( time < 0.5 )
		{
			final double t = time * 2.0;
			return 0.5 * ( t * t * ( ( ( overshoot *= ( 1.525 ) ) + 1 ) * t - overshoot ) );
		}
		else
		{
			final double t = ( time - 0.5 ) * 2.0;
			return 0.5 * ( t * t * ( ( ( overshoot *= ( 1.525 ) ) + 1 ) * t + overshoot ) + 2 );
		}
	}

	/**
	 * Get result of "bounce in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double bounceInFunction( final double time )
	{
		return 1.0 - bounceOutFunction( 1.0 - time );
	}

	/**
	 * Get result of "bounce out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double bounceOutFunction( final double time )
	{
		if ( time < ( 1 / 2.75 ) )
		{
			return ( 7.5625 * time * time );
		}
		else if ( time < ( 2 / 2.75 ) )
		{
			final double x = time - ( 1.5 / 2.75 );
			return ( 7.5625 * x * x + 0.75 );
		}
		else if ( time < ( 2.5 / 2.75 ) )
		{
			final double x = time - ( 2.25 / 2.75 );
			return ( 7.5625 * x * x + 0.9375 );
		}
		else
		{
			final double x = time - ( 2.625 / 2.75 );
			return ( 7.5625 * x * x + 0.984375 );
		}
	}

	/**
	 * Get result of "bounce in-out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double bounceInOutFunction( final double time )
	{
		if ( time < 0.5 )
		{
			return 0.5 * bounceInFunction( time * 2 );
		}
		else
		{
			return 0.5 + 0.5 * bounceOutFunction( ( time - 0.5 ) * 2 );
		}
	}

	/**
	 * Get result of "elastic in" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double elasticInFunction( final double time )
	{
		if ( time == 0 )
		{
			return 0.0;
		}
		else if ( time == 1.0 )
		{
			return 1.0;
		}
		else
		{
			double amplitude = 0;
			final double period = 0.3;
			final double scale;
			if ( ( amplitude == 0.0 ) || ( amplitude < 1.0 ) )
			{
				amplitude = 1.0;
				scale = period / 4;
			}
			else
			{
				scale = period / ( Math.PI / 2 ) * asin( 1.0 / amplitude );
			}

			return -( amplitude * pow( 2, 10 * ( time - 1.0 ) ) * sin( ( time - 1.0 - scale ) * Math.PI / 2 / period ) );
		}
	}

	/**
	 * Get result of "elastic out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double elasticOutFunction( final double time )
	{

		if ( time == 0 )
		{
			return 0.0;
		}
		else if ( time == 1 )
		{
			return 1.0;
		}
		else
		{
			double amplitude = 0.0;
			final double scale;
			final double period = 0.3;
			if ( ( amplitude == 0.0 ) || amplitude < 1.0 )
			{
				amplitude = 1.0;
				scale = period / 4;
			}
			else
			{
				scale = period / ( Math.PI / 2 ) * asin( 1.0 / amplitude );
			}

			return 1.0 + amplitude * pow( 2, -10 * time ) * sin( ( time - scale ) * Math.PI / 2 / period );
		}
	}

	/**
	 * Get result of "elastic in and out" interpolation function.
	 *
	 * @param time Time value (between 0.0 and 1.0).
	 *
	 * @return Function result on 0.0 to 1.0 scale.
	 */
	public static double elasticInOutFunction( final double time )
	{
		if ( time <= 0.0 )
		{
			return 0.0;
		}
		else if ( time >= 1.0 )
		{
			return 1.0;
		}
		else
		{
			double amplitude = 0;
			final double period = ( 0.3 * 1.5 );
			final double scale;
			if ( ( amplitude == 0.0 ) || amplitude < 1.0 )
			{
				amplitude = 1.0;
				scale = period / 4;
			}
			else
			{
				scale = period / ( Math.PI / 2 ) * asin( 1.0 / amplitude );
			}

			final double t = time * 2.0 - 1.0;
			if ( time < 0.5 )
			{
				return -0.5 * ( amplitude * pow( 2.0, 10.0 * t ) * sin( ( t - scale ) * Math.PI / 2 / period ) );
			}
			else
			{
				return 1.0 + amplitude * pow( 2.0, -10.0 * t ) * sin( ( t - scale ) * Math.PI / 2 / period ) * 0.5;
			}
		}
	}
}