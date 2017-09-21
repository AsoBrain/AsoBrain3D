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
package ab.j3d.view;

import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Defines what the background of a view should look like.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class Background
{
	/**
	 * Creates a default background.
	 *
	 * @return  Background.
	 */
	public static Background createDefault()
	{
		return createSolid( new Color4f( 0xEE, 0xEE, 0xEE ) );
	}

	/**
	 * Creates a solid color background.
	 *
	 * @param   color   Background color.
	 *
	 * @return  Background.
	 */
	public static Background createSolid( final Color4 color )
	{
		final Background background = new Background();
		background.setColor( color );
		return background;
	}

	/**
	 * Creates a gradient background with up to four colors. Vector3fs are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @param   colors  Vector3fs that make up the gradient.
	 *
	 * @return  Background.
	 */
	public static Background createGradient( @NotNull final Color4... colors )
	{
		if ( colors.length == 0 )
		{
			throw new IllegalArgumentException( "no colors specified" );
		}

		final Background background = new Background();
		background.setGradient( Arrays.asList( colors ) );
		return background;
	}

	/**
	 * Solid background color.
	 */
	@NotNull
	private Color4 _color;

	/**
	 * Up to four gradient background colors, from bottom left,
	 * counter-clockwise. Empty for no gradient.
	 */
	@NotNull
	private List<Color4> _gradient;

	/**
	 * Constructs a new background consisting of only a solid white color.
	 */
	private Background()
	{
		_color = new Color4f( 1.0f, 1.0f, 1.0f );
		_gradient = Collections.emptyList();
	}

	/**
	 * Sets all properties to match the given background.
	 *
	 * @param   background  Background to be set.
	 */
	public void set( final Background background )
	{
		setColor( background.getColor() );
		setGradient( background.getGradient() );
	}

	/**
	 * Returns the background color.
	 *
	 * @return  Background color.
	 */
	@NotNull
	public Color4 getColor()
	{
		return _color;
	}

	/**
	 * Sets the background color.
	 *
	 * @param   color   Background color.
	 */
	public void setColor( @NotNull final Color4 color )
	{
		_color = color;
	}

	/**
	 * Returns up to four colors used for a gradient background. Vector3fs are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @return  Gradient colors; empty for no gradient.
	 */
	@NotNull
	public List<Color4> getGradient()
	{
		return Collections.unmodifiableList( _gradient );
	}

	/**
	 * Sets up to four colors used for a gradient background. Vector3fs are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @param   gradient    Gradient to be set; empty to disable the gradient.
	 */
	public void setGradient( @NotNull final List<Color4> gradient )
	{
		_gradient = new ArrayList<Color4>( gradient );
	}

	@Override
	public boolean equals( final Object obj )
	{
		boolean result = false;
		if ( obj instanceof Background )
		{
			final Background other = (Background)obj;
			result = _color.equals( other._color ) && _gradient.equals( other._gradient );
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return _color.hashCode() ^ _gradient.hashCode();
	}
}
