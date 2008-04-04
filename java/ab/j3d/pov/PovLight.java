/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2007
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
package ab.j3d.pov;

import java.io.IOException;

import com.numdata.oss.io.IndentingWriter;

/**
 * Pov Light source.
 * <pre>
 * light_source
 * {
 *     &lt; x , y , z &gt;
 *     color {color}
 *     [shadowless]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public final class PovLight
	extends PovGeometry
{
	/** Fade power constant: no falloff.        */ public static final int FADE_NONE      = 0;
	/** Fade power constant: linear falloff.    */ public static final int FADE_LINEAR    = 1;
	/** Fade power constant: quadratic falloff. */ public static final int FADE_QUADRATIC = 2;

	/**
	 * The location of the light.
	 */
	private PovVector _location;

	/**
	 * The color of the light.
	 */
	private PovVector _color;

	/**
	 * If true, this light casts shadows (default in pov).
	 * If false it is equal to the 'shadowless' parameter in pov.
	 */
	private boolean _castShadows;

	/**
	 * Indicates whether the light is a spotlight, i.e. a light which casts a
	 * cone of light that is bright in the center and falls of to darkness in a
	 * soft fringe effect at the edge.
	 */
	private boolean _spotlight;

	/**
	 * Spotlight parameter, which tells the spotlight to point at a particular 3D
	 * coordinate. A line from the location of the spotlight to the 'pointAt'
	 * coordinate forms the center line of the cone of light.
	 */
	private PovVector _pointAt;

	/**
	 * Spotlight parameter, which specifies the overall size of the cone of
	 * light. This is the point where the light falls off to zero intensity. The
	 * float value you specify is the angle, in degrees, between the edge of the
	 * cone and center line.
	 */
	private double _falloff;

	/**
	 * Spotlight parameter, which specifies the size of the "hot-spot" at the
	 * center of the cone of light. The "hot-spot" is a brighter cone of light
	 * inside the spotlight cone and has the same center line. The radius value
	 * specifies the angle, in degrees, between the edge of this bright, inner
	 * cone and the center line. The light inside the inner cone is of uniform
	 * intensity. The light between the inner and outer cones tapers off to
	 * zero.
	 */
	private double _radius;

	/**
	 * The tightness of a spotlight specifies an additional exponential
	 * softening of the edges. A value other than 0, will affect light within
	 * the radius cone as well as light in the falloff cone. The intensity of
	 * light at an angle from the center line is given by: <code>intensity *
	 * cos(angle)tightness</code>. The default value for tightness is 0. Lower
	 * tightness values will make the spotlight brighter, making the spot wider
	 * and the edges sharper. Higher values will dim the spotlight, making the
	 * spot tighter and the edges softer. Values from 0 to 100 are acceptable.
	 */
	private double _tightness;

	/**
	 * Area light parameter that, when enabled, causes the positions of point
	 * lights in the array to be randomly jittered to eliminate any shadow
	 * banding that may occur. The jittering is completely random from render to
	 * render and should not be used when generating animations.
	 */
	private boolean _jitter;

	/**
	 * Light is an area light to soften shadows.
	 */
	private boolean _arealight;

	/**
	 * Horizontal range vector of area array.
	 */
	private PovVector _areaHorSize;

	/**
	 * Vertical range vector of  area array.
	 */
	private PovVector _areaVerSize;

	/**
	 * Number of horizontal lights.
	 */
	private int _areaHorCount;

	/**
	 * Number of vertical lights.
	 */
	private int _areaVerCount;

	/**
	 * Distance at which the light reaches its specified intensity.
	 */
	private double _fadeDistance = 0.0;

	/**
	 * Indicates the falloff rate of the light.
	 */
	private int _fadePower = FADE_NONE;

	/**
	 * Creates a light with name, color, and optional shadow cast flag.
	 *
	 * @param   name            Name of the light.
	 * @param   x               Horizontal position of the light.
	 * @param   y               Depth position of the light.
	 * @param   z               Vertical position of the light.
	 * @param   color           Color of light using a PovVector.
	 * @param   castShadows     Light casts a shadow (default in POV).
	 */
	public PovLight( final String name , final double x , final double y , final double z , final PovVector color , final boolean castShadows )
	{
		super( name );

		_location     = new PovVector( x , y , z );
		_color        = color;
		_castShadows  = castShadows;

		_spotlight    = false;
		_pointAt      = new PovVector( 0.0 , 0.0 , 0.0 );
		_radius       = 30.0;
		_falloff      = 45.0;
		_tightness    =  0.0;

		_arealight    = false;
		_areaHorSize  = null;
		_areaVerSize  = null;
		_areaHorCount = 1;
		_areaVerCount = 1;
		_jitter       = false;
	}

	/**
	 * Make the light an area light to soften shadows.
	 *
	 * @param   horSize     Horizontal range vector of area array.
	 * @param   horCount    Number of horizontal lights.
	 * @param   verSize     Vertical range vector of  area array.
	 * @param   verCount    Number of vertical lights.
	 */
	public void makeArea( final PovVector horSize , final int horCount , final PovVector verSize , final int verCount )
	{
		_arealight    = true;
		_areaHorSize  = horSize;
		_areaVerSize  = verSize;
		_areaHorCount = horCount;
		_areaVerCount = verCount;
	}

	/**
	 * Returns whether the positions of point lights that make up an area light
	 * are randomly jittered to eliminate any shadow banding that may occur.

	 * @return  <code>true</code> if jittering is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isJitter()
	{
		return _jitter;
	}

	/**
	 * Sets whether the positions of point lights that make up an area light are
	 * randomly jittered to eliminate any shadow banding that may occur. The
	 * jittering is completely random from render to render and should not be
	 * used when generating animations.

	 * @param   jitter  <code>true</code> to enabled jittering;
	 *                  <code>false</code> otherwise.
	 */
	public void setJitter( final boolean jitter )
	{
		_jitter = jitter;
	}

	/**
	 * Convenience method to set common spotlight properties.
	 *
	 * @param   target      Target point of the spot.
	 * @param   radius      Angle between the center line and the outer edge of
	 *                      the light's "hot-spot", in degrees.
	 * @param   falloff     Angle between the center line and the outer edge of
	 *                      the cone of light, in degrees.
	 *
	 * @throws  NullPointerException if target is <code>null</code>.
	 * @throws  IllegalArgumentException if <code>radius</code> or
	 *          <code>falloff</code> is outside of the valid range from 0.0 to
	 *          180.0 (inclusive), or if <code>falloff < radius</code>.
	 */
	public void makeSpot( final PovVector target , final double radius , final double falloff )
	{
		if ( target == null )
			throw new NullPointerException( "target" );
		if ( ( radius < 0.0 ) || ( radius > 180.0 ) )
			throw new IllegalArgumentException( "radius" );
		if ( ( falloff < radius ) || ( falloff > 180.0 ) )
			throw new IllegalArgumentException( "falloff" );

		_spotlight = true;
		_pointAt   = target;
		_falloff   = falloff;
		_radius    = radius;
	}

	/**
	 * Returns the the tightness of a spotlight, which specifies an additional
	 * exponential softening of the edges.
	 *
	 * @return  Tightness of the spotlight.
	 */
	public double getTightness()
	{
		return _tightness;
	}

	/**
	 * Sets the the tightness of a spotlight, which specifies an additional
	 * exponential softening of the edges. If the light isn't a spotlight, this
	 * parameter has no effect.
	 *
	 * <p>
	 * A value other than 0, will affect light within the radius cone as well as
	 * light in the falloff cone. The intensity of light at an angle from the
	 * center line is given by: <code>intensity * cos(angle)tightness</code>.
	 * The default value for tightness is 0. Lower tightness values will make
	 * the spotlight brighter, making the spot wider and the edges sharper.
	 * Higher values will dim the spotlight, making the spot tighter and the
	 * edges softer. Values from 0 to 100 are acceptable.
	 *
	 * @param   tightness   Tightness of the spotlight.
	 *
	 * @throws  IllegalArgumentException if the given tightness falls outside of
	 *          the allowed range from 0 to 100 (inclusive).
	 */
	public void setTightness( final double tightness )
	{
		if ( ( tightness < 0.0 ) || ( tightness > 100.0 ) )
			throw new IllegalArgumentException( "tightness" );

		_tightness = tightness;
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		out.write( "light_source" );
		final String name = getName();
		if ( name != null )
		{
			out.write( " // " );
			out.write( name );
		}
		out.newLine();
		out.writeln( "{" );
		out.indentIn();

		_location.write( out );
		out.newLine();

		out.write( "color " );
		_color.write( out );
		out.newLine();

		if( !_castShadows )
		{
			out.writeln( "shadowless" );
		}

		if ( _spotlight )
		{
			out.writeln( "spotlight" );

			out.write( "point_at " );
			_pointAt.write( out );
			out.newLine();

			if ( _radius != 30.0 )
			{
				out.write( "radius " );
				out.write( format( _radius ) );
				out.newLine();
			}

			if ( _falloff != 45.0 )
			{
				out.write( "falloff " );
				out.write( format( _falloff ) );
				out.newLine();
			}

			if ( _tightness != 0.0 )
			{
				out.write( "tightness " );
				out.write( format( _tightness ) );
				out.newLine();
			}

			if ( _jitter )
			{
				out.writeln( "jitter" );
			}
		}

		if ( _arealight )
		{
			out.write( "area_light " );
			_areaHorSize.write( out );
			out.write( " , " );
			_areaVerSize.write( out );
			out.write( " , " );
			out.write( format( _areaHorCount ) );
			out.write( " , " );
			out.write( format( _areaVerCount ) );
			out.newLine();

			if ( _jitter )
			{
				out.writeln( "jitter" );
			}
		}

		if ( _fadePower != FADE_NONE )
		{
			out.write( "fade_distance " );
			out.write( format( _fadeDistance ) );
			out.newLine();
			out.write( "fade_power " );
			out.write( format( _fadePower ) );
			out.newLine();
		}

		writeModifiers( out );

		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Returns the distance at which the light reaches its specified intensity.
	 *
	 * @return  Distance of 100% light intensity.
	 */
	public double getFadeDistance()
	{
		return _fadeDistance;
	}

	/**
	 * Sets the distance at which the light reaches its specified intensity.
	 *
	 * @param   fadeDistance    Distance of 100% light intensity.
	 *
	 * @throws  IllegalArgumentException if <code>fadeDistance &lt; 0.0</code>.
	 */
	public void setFadeDistance( final double fadeDistance )
	{
		if ( fadeDistance < 0.0 )
		{
			throw new IllegalArgumentException( "fadeDistance" );
		}
		_fadeDistance = fadeDistance;
	}

	/**
	 * Indicates the falloff rate of the light.
	 *
	 * @return  One of: {@link #FADE_NONE}, {@link #FADE_LINEAR},
	 *          {@link #FADE_QUADRATIC}.
	 */
	public int getFadePower()
	{
		return _fadePower;
	}

	/**
	 * Sets the falloff rate of the light.
	 *
	 * @param   fadePower   One of: {@link #FADE_NONE}, {@link #FADE_LINEAR},
	 *                      {@link #FADE_QUADRATIC}.
	 *
	 * @throws  IllegalArgumentException if <code>fadePower</code> is not one
	 *          of the specified values.
	 */
	public void setFadePower( final int fadePower )
	{
		if ( ( fadePower != FADE_NONE ) && ( fadePower != FADE_LINEAR ) && ( fadePower != FADE_QUADRATIC ) )
		{
			throw new IllegalArgumentException( "fadePower" );
		}
		_fadePower = fadePower;
	}
}
