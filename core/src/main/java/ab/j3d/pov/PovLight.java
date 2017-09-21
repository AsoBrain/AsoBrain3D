/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.io.*;


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
	 * Spotlight and parallel light parameter, which tells the light to point at
	 * a particular 3D coordinate. For spotlights, a line from the location of
	 * the spotlight to the 'pointAt' coordinate forms the center line of the
	 * cone of light.
	 */
	private PovVector _pointAt;

	/**
	 * Spotlight parameter, which specifies the overall size of the cone of
	 * light. This is the point where the light falls off to zero intensity. The
	 * float value you specify is the angle, in degrees, between the edge of the
	 * cone and center line.
	 */
	private double _fallOff;

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
	 * Parallel lights are useful for simulating very distant light sources,
	 * such as sunlight. As the name suggests, it makes the light rays parallel.
	 */
	private boolean _parallel;

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

		_location      = new PovVector( x , y , z );
		_color         = color;
		_castShadows   = castShadows;

		_spotlight     = false;
		_pointAt       = new PovVector( 0.0 , 0.0 , 0.0 );
		_radius        = 30.0;
		_fallOff       = 45.0;
		_tightness     =  0.0;

		_arealight     = false;
		_parallel      = false;
		_areaHorSize   = null;
		_areaVerSize   = null;
		_areaHorCount  = 1;
		_areaVerCount  = 1;
		_jitter        = false;
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
	 * Returns whether the light is a spotlight, i.e. a light which casts a cone
	 * of light that is bright in the center and falls of to darkness in a soft
	 * fringe effect at the edge.
	 *
	 * @return  <code>true</code> if the light is a spotlight;
	 *          <code>false</code> otherwise.
	 */
	public boolean isSpotlight()
	{
		return _spotlight;
	}

	/**
	 * Sets whether the light is a spotlight, i.e. a light which casts a cone of
	 * light that is bright in the center and falls of to darkness in a soft
	 * fringe effect at the edge.
	 *
	 * @param   spotlight   <code>true</code> to make the light a spotlight;
	 *                      <code>false</code> otherwise.
	 */
	public void setSpotlight( final boolean spotlight )
	{
		_spotlight = spotlight;
	}

	/**
	 * Returns the 3D coordinate that the spotlight or parallel light points at.
	 * For spotlights, a line from the location of the spotlight to the
	 * 'pointAt' coordinate forms the center line of the cone of light.
	 *
	 * @return  Coordinate that the light points at.
	 */
	public PovVector getPointAt()
	{
		return _pointAt;
	}

	/**
	 * Sets the 3D coordinate that the spotlight or parallel light points at.
	 * For spotlights, a line from the location of the spotlight to the
	 * 'pointAt' coordinate forms the center line of the cone of light.
	 *
	 * @param   pointAt     Coordinate to point at.
	 */
	public void setPointAt( final PovVector pointAt )
	{
		_pointAt = pointAt;
	}

	/**
	 * Returns the overall size of the cone of light. This is the point where
	 * the light falls off to zero intensity. The float value you specify is the
	 * angle, in degrees, between the edge of the cone and center line.
	 *
	 * @return  Angle between the edge of the cone and the center line of the
	 *          spotlight, in degrees.
	 */
	public double getFallOff()
	{
		return _fallOff;
	}

	/**
	 * Sets the overall size of the cone of light. This is the point where
	 * the light falls off to zero intensity. The float value you specify is the
	 * angle, in degrees, between the edge of the cone and center line.
	 *
	 * @param   fallOff     Angle between the edge of the cone and the center
	 *                      line of the spotlight, in degrees.
	 */
	public void setFallOff( final double fallOff )
	{
		_fallOff = fallOff;
	}

	/**
	 * Returns the size of the "hot-spot" at the center of the cone of light.
	 * The "hot-spot" is a brighter cone of light inside the spotlight cone and
	 * has the same center line. The radius value specifies the angle, in
	 * degrees, between the edge of this bright, inner cone and the center line.
	 * The light inside the inner cone is of uniform intensity. The light
	 * between the inner and outer cones tapers off to zero.
	 *
	 * @return  Angle between the bright inner cone and the center line of the
	 *          spotlight, in degrees.
	 */
	public double getRadius()
	{
		return _radius;
	}

	/**
	 * Returns the size of the "hot-spot" at the center of the cone of light.
	 * The "hot-spot" is a brighter cone of light inside the spotlight cone and
	 * has the same center line. The radius value specifies the angle, in
	 * degrees, between the edge of this bright, inner cone and the center line.
	 * The light inside the inner cone is of uniform intensity. The light
	 * between the inner and outer cones tapers off to zero.
	 *
	 * @param   radius  Angle between the bright inner cone and the center line
	 *                  of the spotlight, in degrees.
	 */
	public void setRadius( final double radius )
	{
		_radius = radius;
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

	/**
	 * Returns whether the light is a parallel light. Parallel lights are useful
	 * for simulating very distant light sources, such as sunlight. As the name
	 * suggests, it makes the light rays parallel.
	 *
	 * @return  <code>true</code> if the light emits parallel light rays;
	 *          <code>false</code> otherwise.
	 */
	public boolean isParallel()
	{
		return _parallel;
	}

	/**
	 * Sets whether the light is a parallel light. Parallel lights are useful
	 * for simulating very distant light sources, such as sunlight. As the name
	 * suggests, it makes the light rays parallel.
	 *
	 * @param   parallel    <code>true</code> to make the light rays parallel;
	 *                      <code>false</code> otherwise.
	 */
	public void setParallel( final boolean parallel )
	{
		_parallel = parallel;
	}

	public void write( final PovWriter out )
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

			if ( _fallOff != 45.0 )
			{
				out.write( "falloff " );
				out.write( format( _fallOff ) );
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

		if ( _parallel )
		{
			out.write( "parallel " );
			out.write( "point_at " );
			_pointAt.write( out );
			out.newLine();
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
	 * Indicates the fall-off rate of the light.
	 *
	 * @return  One of: {@link #FADE_NONE}, {@link #FADE_LINEAR},
	 *          {@link #FADE_QUADRATIC}.
	 */
	public int getFadePower()
	{
		return _fadePower;
	}

	/**
	 * Sets the fall-off rate of the light.
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
