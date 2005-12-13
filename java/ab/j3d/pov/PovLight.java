/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2005
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
	public final PovVector location;

	/**
	 * The color of the light.
	 */
	public final PovVector color;

	/**
	 * If true, this light casts shadows (default in pov).
	 * If false it is equal to the 'shadowless' parameter in pov.
	 */
	public boolean castShadows = true;

	/**
	 * Light is a spot light.
	 */
	public boolean spotlight = false;

	/**
	 * Target point of the spot ligt.
	 */
	public PovVector target = new PovVector( 0.0 , 0.0 , 0.0 );

	/**
	 * Fall-off distance.
	 */
	public double falloff = 200000.0;

	/**
	 * Spot light parameter.
	 *
	 * @FIXME need description
	 */
	public final double hotspot = -1.0;

	/**
	 * Spot light parameter.
	 *
	 * @FIXME need description
	 */
	public final double tightness = 1.0;

	/**
	 * Adaptive parameter of spotlight.
	 */
	public double adaptive = 1.0;

	/**
	 * Light parameter.
	 *
	 * @FIXME need description
	 */
	public final boolean jitter = false;

	/**
	 * Light is an area light to soften shadows.
	 */
	public boolean arealight = false;

	/**
	 * Horizontal range vector of area array.
	 */
	public PovVector area_hor_size = null;

	/**
	 * Vertical range vector of  area array.
	 */
	public PovVector area_ver_size = null;

	/**
	 * Number of horizontal lights.
	 */
	public double area_hor_count = 1.0;

	/**
	 * Number of vertical lights.
	 */
	public double area_ver_count = 1.0;

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
		location    = new PovVector( x , y , z );
		this.color       = color;
		this.castShadows = castShadows;
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
		arealight = true;
		area_hor_size = horSize;
		area_ver_size = verSize;
		area_hor_count = horCount;
		area_ver_count = verCount;
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
		spotlight = true;
		this.target = target;
		this.falloff = falloff;
		this.adaptive = adaptive;
	}

	/**
	 * Writes the PovObject to the specified output stream.
	 * The method should use indentIn and indentOut to maintain the overview.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "light_source // " + name );
		out.writeln( "{" );
		out.indentIn();
		out.writeln( "" + location );
		out.writeln( "color " + color );
		if( !castShadows )
			out.writeln( "shadowless" );
		if ( spotlight )
		{
			out.writeln( "spotlight" );
			out.writeln( "point_at " + target );
			if ( hotspot != -1 )
				out.writeln( "hotspot " + hotspot );
			if ( falloff != -1 )
				out.writeln( "falloff " + falloff );
			out.writeln( "adaptive " + adaptive );
			out.writeln( "tightness " + tightness );
			if ( jitter )
				out.writeln( "jitter" );
		}
		if ( arealight )
		{
			out.writeln( "area_light " + area_hor_size + ", " + area_ver_size + ", " + area_hor_count + ", " + area_ver_count );
		}
		writeTransformation( out );
		out.indentOut();
		out.write( "}" );
		out.writeln();
	}

}
