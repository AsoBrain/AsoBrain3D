/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2010-2010
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

import java.awt.*;
import java.util.*;
import java.util.List;

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
		return createSolid( new Color( 0xeeeeee ) );
	}

	/**
	 * Creates a solid color background.
	 *
	 * @param   color   Background color.
	 *
	 * @return  Background.
	 */
	public static Background createSolid( final Color color )
	{
		final Background background = new Background();
		background.setColor( color );
		return background;
	}

	/**
	 * Creates a gradient background with up to four colors. Colors are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @param   colors  Colors that make up the gradient.
	 *
	 * @return  Background.
	 */
	public static Background createGradient( @NotNull final Color... colors )
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
	private Color _color;

	/**
	 * Up to four gradient background colors, from bottom left,
	 * counter-clockwise. Emtpy for no gradient.
	 */
	@NotNull
	private List<Color> _gradient;

	/**
	 * Constructs a new background consisting of only a solid white color.
	 */
	private Background()
	{
		_color = Color.WHITE;
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
	public Color getColor()
	{
		return _color;
	}

	/**
	 * Sets the background color.
	 *
	 * @param   color   Background color.
	 */
	public void setColor( @NotNull final Color color )
	{
		_color = color;
	}

	/**
	 * Returns up to four colors used for a gradient background. Colors are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @return  Gradient colors; empty for no gradient.
	 */
	@NotNull
	public List<Color> getGradient()
	{
		return Collections.unmodifiableList( _gradient );
	}

	/**
	 * Sets up to four colors used for a gradient background. Colors are
	 * specified starting from the bottom left corner in counter-clockwise
	 * order. If fewer than four colors are specified, the specified sequence
	 * of colors is repeated.
	 *
	 * @param   gradient    Gradient to be set; empty to disable the gradient.
	 */
	public void setGradient( @NotNull final List<Color> gradient )
	{
		_gradient = new ArrayList<Color>( gradient );
	}
}
