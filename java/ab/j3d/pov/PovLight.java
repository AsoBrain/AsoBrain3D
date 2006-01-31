/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2006
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
	 * Light is a spot light.
	 */
	public boolean _spotlight;

	/**
	 * Target point of the spot ligt.
	 */
	private PovVector _pointAt;

	/**
	 * Fall-off distance.
	 */
	private double _falloff;

	/**
	 * Spot light parameter.
	 *
	 * @FIXME need description
	 */
	private double _hotspot;

	/**
	 * Spot light parameter.
	 *
	 * @FIXME need description
	 */
	private double _tightness;

	/**
	 * Adaptive parameter of spotlight.
	 */
	private double _adaptive;

	/**
	 * Light parameter.
	 *
	 * @FIXME need description
	 */
	private boolean _jitter;

	/**
	 * Light is an area light to soften shadows.
	 */
	public boolean _arealight;

	/**
	 * Horizontal range vector of area array.
	 */
	public PovVector _areaHorSize;

	/**
	 * Vertical range vector of  area array.
	 */
	public PovVector _areaVerSize;

	/**
	 * Number of horizontal lights.
	 */
	public double _areaHorCount;

	/**
	 * Number of vertical lights.
	 */
	public double _areaVerCount;

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
		_falloff      = 200000.0;
		_hotspot      = -1.0;
		_tightness    = 1.0;
		_adaptive     = 1.0;
		_jitter       = false;
		_arealight    = false;
		_areaHorSize  = null;
		_areaVerSize  = null;
		_areaHorCount = 1.0;
		_areaVerCount = 1.0;
	}

	/**
	 * Make the light an area light to soften shadows.
	 *
	 * @param   horSize     Horizontal range vector of area array.
	 * @param   horCount    Number of horizontal lights.
	 * @param   verSize     Vertical range vector of  area array.
	 * @param   verCount    Number of vertical lights.
	 */
	public void makeArea( final PovVector horSize , final double horCount , final PovVector verSize , final double verCount )
	{
		_arealight    = true;
		_areaHorSize  = horSize;
		_areaVerSize  = verSize;
		_areaHorCount = horCount;
		_areaVerCount = verCount;
	}

	/**
	 * Same as above constructor, but now jitter can be set also.
	 *
	 * @param   horSize     Horizontal range vector of area array.
	 * @param   horCount    Number of horizontal lights.
	 * @param   verSize     Vertical range vector of  area array.
	 * @param   verCount    Number of vertical lights.
	 */
	public void makeArea( final PovVector horSize , final double horCount , final PovVector verSize , final double verCount , final boolean jitter )
	{
		makeArea( horSize , horCount , verSize , verCount );
		_jitter = jitter;
	}

	/**
	 * Make the light a spot light.
	 *
	 * @param   target      Target point of the spot.
	 * @param   falloff     Fall-off distance.
	 * @param   adaptive    Adaptive parameter of spotlight.
	 */
	public void makeSpot( final PovVector target , final double falloff , final double adaptive )
	{
		_spotlight = true;
		_pointAt   = target;
		_falloff   = falloff;
		_adaptive  = adaptive;
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

			if ( _hotspot != -1.0 )
			{
				out.write( "hotspot " );
				out.write( format( _hotspot ) );
				out.newLine();
			}

			if ( _falloff != -1.0 )
			{
				out.write( "falloff " );
				out.write( format( _falloff ) );
				out.newLine();
			}

			out.write( "adaptive " );
			out.write( format( _adaptive ) );
			out.newLine();

			out.write( "tightness " );
			out.write( format( _tightness ) );
			out.newLine();

			if ( _jitter )
			{
				out.writeln( "jitter" );
			}
		}

		if ( _arealight )
		{
			out.write( "area_light " );
			_areaHorSize.write( out );
			out.write( ", " );
			_areaVerSize.write( out );
			out.write( ", " );
			out.write( format( _areaHorCount ) );
			out.write( ", " );
			out.write( format( _areaVerCount ) );
			out.newLine();

			if ( _jitter )
			{
				out.writeln( "jitter" );
			}
		}

		writeTransformation( out );

		out.indentOut();
		out.writeln( "}" );
	}
}
