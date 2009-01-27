/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.awt.Color;

/**
 * Style filter to visualize selection.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class SelectionStyleFilter
	implements RenderStyleFilter
{
	/**
	 * Color to blend fill color with for selected objects.
	 */
	private Color _selectionFillColor;

	/**
	 * Create filter with default settings.
	 */
	protected SelectionStyleFilter()
	{
		this( new Color( 0x400000FF , true ) );
	}

	/**
	 * Create filter.
	 *
	 * @param   color   Color to blend fill color with for selected objects.
	 */
	protected SelectionStyleFilter( final Color color )
	{
		_selectionFillColor = color;
	}

	public RenderStyle applyFilter( final RenderStyle style , final Object context )
	{
		return isSelected( context ) ? applySelectionStyle( style ) : style;
	}

	/**
	 * Test if the specified context is selected.
	 *
	 * @param   context     Context to test.
	 *
	 * @return  <code>true</code> if the object is selected;
	 *          <code>false</code> otherwise.
	 */
	protected boolean isSelected( final Object context )
	{
		return ( context instanceof ViewModelNode ) && isNodeSelected( (ViewModelNode)context );
	}

	/**
	 * Test if a {@link ViewModelNode} is selected.
	 *
	 * @param   node    Node to test.
	 *
	 * @return  <code>true</code> if selected;
	 *          <code>false</code> otherwise.
	 */
	protected abstract boolean isNodeSelected( final ViewModelNode node );

	/**
	 * Apply style for selection to existing style.
	 *
	 * @param   style   Style to filter.
	 *
	 * @return  Filtered style.
	 */
	protected RenderStyle applySelectionStyle( final RenderStyle style )
	{
		final RenderStyle result = style.clone();
		result.setFillEnabled( true );
		result.setFillColor( RenderStyle.blendColors( result.getFillColor() , _selectionFillColor ) );
		return result;
	}
}
