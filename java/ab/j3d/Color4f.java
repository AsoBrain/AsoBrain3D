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
public class Color4f
	implements Color4, Serializable
{
	/**
	 * Serialized data version.
	 */
	static final long serialVersionUID = 4877680141580006740L;

	/**
	 * Red component (0.0 - 1.0).
	 */
	private final float _red;

	/**
	 * Green component (0.0 - 1.0).
	 */
	private final float _green;

	/**
	 * Blue component (0.0 - 1.0).
	 */
	private final float _blue;

	/**
	 * Alpha value (0.0 - 1.0 = transparent - opaque).
	 */
	private final float _alpha;

	/**
	 * Create color from integer encoded in ARGB format (bits 0-7: blue,
	 * bits 8-15: green, bits 16-23: red, bits 24-31: alpha). If the alpha is
	 * zero, it is automatically changed to 255 (opaque).
	 *
	 * @param   argb    Color as ARGB-encoded integer.
	 */
	public Color4f( final int argb )
	{
		this( ( argb >> 16 ) & 0xFF, ( argb >> 8 ) & 0xFF, argb & 0xFF, ( argb < 0x1000000 ) ? 255 : ( argb >> 24 & 0xFF ) );
	}

	/**
	 * Create color.
	 *
	 * @param   red     Red component (0.0 - 1.0).
	 * @param   green   Green component (0.0 - 1.0).
	 * @param   blue    Blue component (0.0 - 1.0).
	 */
	public Color4f( final float red, final float green, final float blue )
	{
		this( red, green, blue, 1.0f );
	}

	/**
	 * Create color.
	 *
	 * @param   red     Red component (0.0 - 1.0).
	 * @param   green   Green component (0.0 - 1.0).
	 * @param   blue    Blue component (0.0 - 1.0).
	 * @param   alpha   Alpha value (0.0 - 1.0 = transparent - opaque).
	 */
	public Color4f( final float red, final float green, final float blue, final float alpha )
	{
		_red = red;
		_green = green;
		_blue = blue;
		_alpha = alpha;
	}

	/**
	 * Create color. Alpha is set to fully opaque.
	 *
	 * @param   red     Red component (0 - 255).
	 * @param   green   Green component (0 - 255).
	 * @param   blue    Blue component (0 - 255).
	 */
	public Color4f( final int red, final int green, final int blue )
	{
		this( (float)red / 255.0f, (float)green / 255.0f, (float)blue / 255.0f );
	}

	/**
	 * Create color.
	 *
	 * @param   red     Red component (0 - 255).
	 * @param   green   Green component (0 - 255).
	 * @param   blue    Blue component (0 - 255).
	 * @param   alpha   Alpha value (0 - 255 = transparent - opaque).
	 */
	public Color4f( final int red, final int green, final int blue, final int alpha )
	{
		this( (float)red / 255.0f, (float)green / 255.0f, (float)blue / 255.0f, (float)alpha / 255.0f );
	}

	/**
	 * Get red component.
	 *
	 * @return  Red component (0.0 - 1.0).
	 */
	public float getRed()
	{
		return _red;
	}

	public float getRedFloat()
	{
		return getRed();
	}

	public int getRedInt()
	{
		return Math.round( getRed() * 255.0f );
	}

	/**
	 * Get green component.
	 *
	 * @return  Green component (0.0 - 1.0).
	 */
	public float getGreen()
	{
		return _green;
	}

	public float getGreenFloat()
	{
		return getGreen();
	}

	public int getGreenInt()
	{
		return Math.round( getGreen() * 255.0f );
	}

	/**
	 * Get blue component.
	 *
	 * @return  Blue component (0.0 - 1.0).
	 */
	public float getBlue()
	{
		return _blue;
	}

	public float getBlueFloat()
	{
		return getBlue();
	}

	public int getBlueInt()
	{
		return Math.round( getBlue() * 255.0f );
	}

	/**
	 * Get alpha value.
	 *
	 * @return  Alpha value (0.0 - 1.0 = transparent - opaque).
	 */
	public float getAlpha()
	{
		return _alpha;
	}

	public float getAlphaFloat()
	{
		return getAlpha();
	}

	public int getAlphaInt()
	{
		return Math.round( getAlpha() * 255.0f );
	}

	public int getRGB()
	{
		return getBlueInt() | ( getGreenInt() <<  8 ) | ( getRedInt() << 16 );
	}

	public int getARGB()
	{
		return getBlueInt() | ( getGreenInt() <<  8 ) | ( getRedInt() << 16 ) | ( Math.round( getAlpha() * 255.0f ) << 24 );
	}
}
