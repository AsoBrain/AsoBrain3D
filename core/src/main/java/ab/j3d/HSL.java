/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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

import static ab.j3d.MathTools.*;

/**
 * Color in the HSL (hue, saturation, lightness) color model.
 *
 * @author Gerrit Meinders
 */
public class HSL
{
	/**
	 * Hue, in degrees.
	 */
	private float _hue;

	/**
	 * Saturation, between 0 and 1.
	 */
	private float _saturation;

	/**
	 * Lightness, between 0 and 1.
	 */
	private float _lightness;

	/**
	 * Alpha, between 0 and 1.
	 */
	private float _alpha;

	/**
	 * Constructs a new instance.
	 *
	 * @param hue        Hue, in degrees.
	 * @param saturation Saturation, between 0 and 1.
	 * @param lightness  Lightness, between 0 and 1.
	 */
	public HSL( final float hue, final float saturation, final float lightness )
	{
		this( hue, saturation, lightness, 1 );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param hue        Hue, in degrees.
	 * @param saturation Saturation, between 0 and 1.
	 * @param lightness  Lightness, between 0 and 1.
	 * @param alpha      Alpha, between 0 and 1.
	 */
	public HSL( final float hue, final float saturation, final float lightness, final float alpha )
	{
		_hue = ( hue % 360 + 360 ) % 360;
		_saturation = (float)clamp( saturation, 0, 1 );
		_lightness = (float)clamp( lightness, 0, 1 );
		_alpha = (float)clamp( alpha, 0, 1 );
	}

	public float getHue()
	{
		return _hue;
	}

	public void setHue( final float hue )
	{
		_hue = hue;
	}

	public float getSaturation()
	{
		return _saturation;
	}

	public void setSaturation( final float saturation )
	{
		_saturation = saturation;
	}

	public float getLightness()
	{
		return _lightness;
	}

	public void setLightness( final float lightness )
	{
		_lightness = lightness;
	}

	public float getAlpha()
	{
		return _alpha;
	}

	public void setAlpha( final float alpha )
	{
		_alpha = alpha;
	}

	/**
	 * Creates an instance from the given color.
	 *
	 * @param rgba Color as RGB.
	 *
	 * @return Same color as HSL.
	 */
	public static HSL fromColor( final Color4 rgba )
	{
		final float r = rgba.getRedFloat();
		final float g = rgba.getGreenFloat();
		final float b = rgba.getBlueFloat();

		final float max = Math.max( Math.max( r, g ), b );
		final float min = Math.min( Math.min( r, g ), b );

		final float hue; // https://en.wikipedia.org/wiki/HSL_and_HSV#Hue_and_chroma
		final float saturation; // https://en.wikipedia.org/wiki/HSL_and_HSV#Saturation
		final float lightness = ( min + max ) / 2; // https://en.wikipedia.org/wiki/HSL_and_HSV#Lightness

		if ( min == max )
		{
			hue = 0;
			saturation = 0;
		}
		else
		{
			final float c = max - min;

			final float h;
			if ( max == r )
			{
				h = ( ( g - b ) / c + 6 ) % 6;
			}
			else if ( max == g )
			{
				h = ( ( b - r ) / c ) + 2;
			}
			else // if ( max == b )
			{
				h = ( ( r - g ) / c ) + 4;
			}

			hue = h * 60;
			saturation = lightness == 1 ? 0 : c / ( 1 - Math.abs( 2 * lightness - 1 ) );
		}

		return new HSL( hue, saturation, lightness, rgba.getAlphaFloat() );
	}

	/**
	 * Creates an RGB color from this HSL color.
	 *
	 * @return Same color as RGB.
	 */
	public Color4 toColor()
	{
		// https://en.wikipedia.org/wiki/HSL_and_HSV#From_HSL

		final float c = ( 1 - Math.abs( 2 * _lightness - 1 ) ) * _saturation;
		final float h = _hue / 60;
		final float x = c * ( 1 - Math.abs( h % 2 - 1 ) );

		final float r1;
		final float g1;
		final float b1;

		if ( h < 1 )
		{
			r1 = c;
			g1 = x;
			b1 = 0;
		}
		else if ( h < 2 )
		{
			r1 = x;
			g1 = c;
			b1 = 0;
		}
		else if ( h < 3 )
		{
			r1 = 0;
			g1 = c;
			b1 = x;
		}
		else if ( h < 4 )
		{
			r1 = 0;
			g1 = x;
			b1 = c;
		}
		else if ( h < 5 )
		{
			r1 = x;
			g1 = 0;
			b1 = c;
		}
		else
		{
			r1 = c;
			g1 = 0;
			b1 = x;
		}

		final float m = _lightness - c / 2;
		return new Color4f( r1 + m, g1 + m, b1 + m, _alpha );
	}
}
