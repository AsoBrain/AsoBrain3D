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
package ab.j3d;

import java.io.*;

/**
 * This class defines a color using red, green, blue, and alpha properties as
 * single precision floating values.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Color4i
	implements Color4, Serializable
{
	/**
	 * Serialized data version.
	 */
	static final long serialVersionUID = 4877680141580006740L;

	/**
	 * Red component (0.0 - 255).
	 */
	private final int _red;

	/**
	 * Green component (0.0 - 255).
	 */
	private final int _green;

	/**
	 * Blue component (0.0 - 255).
	 */
	private final int _blue;

	/**
	 * Alpha value (0 - 255 = transparent - opaque).
	 */
	private final int _alpha;

	/**
	 * Create color from integer encoded in ARGB format (bits 0-7: blue,
	 * bits 8-15: green, bits 16-23: red, bits 24-31: alpha). If the alpha is
	 * zero, it is automatically changed to 255 (opaque).
	 *
	 * @param   argb    Color as ARGB-encoded integer.
	 */
	public Color4i( final int argb )
	{
		this( ( argb >> 16 ) & 0xFF, ( argb >> 8 ) & 0xFF, argb & 0xFF, ( argb < 0x1000000 ) ? 255 : ( argb >> 24 & 0xFF ) );
	}

	/**
	 * Create color. Alpha is set to fully opaque.
	 *
	 * @param   red     Red component (0.0 - 1.0).
	 * @param   green   Green component (0.0 - 1.0).
	 * @param   blue    Blue component (0.0 - 1.0).
	 */
	public Color4i( final float red, final float green, final float blue )
	{
		this( Math.round( red * 255.0f ), Math.round( green * 255.0f ), Math.round( blue * 255.0f ) );
	}

	/**
	 * Create color.
	 *
	 * @param   red     Red component (0.0 - 1.0).
	 * @param   green   Green component (0.0 - 1.0).
	 * @param   blue    Blue component (0.0 - 1.0).
	 * @param   alpha   Alpha value (0.0 - 1.0 = transparent - opaque).
	 */
	public Color4i( final float red, final float green, final float blue, final float alpha )
	{
		this( Math.round( red * 255.0f ), Math.round( green * 255.0f ), Math.round( blue * 255.0f ), Math.round( alpha * 255.0f ) );
	}

	/**
	 * Create color. Alpha is set to fully opaque.
	 *
	 * @param   red     Red component (0 - 255).
	 * @param   green   Green component (0 - 255).
	 * @param   blue    Blue component (0 - 255).
	 */
	public Color4i( final int red, final int green, final int blue )
	{
		this( red, green, blue, 255 );
	}

	/**
	 * Create color.
	 *
	 * @param   red     Red component (0 - 255).
	 * @param   green   Green component (0 - 255).
	 * @param   blue    Blue component (0 - 255).
	 * @param   alpha   Alpha value (0 - 255 = transparent - opaque).
	 */
	public Color4i( final int red, final int green, final int blue, final int alpha )
	{
		_red = red;
		_green = green;
		_blue = blue;
		_alpha = alpha;
	}

	public float getRedFloat()
	{
		return (float) getRedInt() / 255.0f;
	}

	public int getRedInt()
	{
		return _red;
	}

	public float getGreenFloat()
	{
		return (float) getGreenInt() / 255.0f;
	}

	public int getGreenInt()
	{
		return _green;
	}

	public float getBlueFloat()
	{
		return (float) getBlueInt() / 255.0f;
	}

	public int getBlueInt()
	{
		return _blue;
	}

	public float getAlphaFloat()
	{
		return (float) getAlphaInt() / 255.0f;
	}

	public int getAlphaInt()
	{
		return _alpha;
	}

	public float getLuminance()
	{
		return ( 0.3f * (float)getRedInt() + 0.59f * (float)getGreenInt() + 0.11f * (float)getBlueInt() ) / 255.0f;
	}

	public int getRGB()
	{
		return getBlueInt() | ( getGreenInt() <<  8 ) | ( getRedInt() << 16 );
	}

	public int getARGB()
	{
		return getBlueInt() | ( getGreenInt() <<  8 ) | ( getRedInt() << 16 ) | ( getAlphaInt() << 24 );
	}
}
