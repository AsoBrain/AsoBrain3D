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

import ab.j3d.model.*;

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
	 * Create filter.
	 */
	protected SelectionStyleFilter()
	{
	}

	public RenderStyle applyFilter( final RenderStyle style, final Object context )
	{
		RenderStyle result = style;

		if ( context instanceof ContentNode )
		{
			final ContentNode contentNode = (ContentNode)context;
			if ( isNodeSelected( contentNode ) )
			{
				result = applySelectionStyle( style, contentNode );
			}
			else if ( hasSelection() )
			{
				result = applyUnselectedStyle( style, contentNode );
			}
		}

		return result;
	}

	/**
	 * Test if there is an active selection. If this is <code>true</code>,
	 * {@link #applyUnselectedStyle} will be called for any
	 * unselected {@link ContentNode}.
	 *
	 * @return  <code>true</code> if there is an active selection;
	 *          <code>false</code> otherwise (unselected style not applied).
	 */
	protected abstract boolean hasSelection();

	/**
	 * Test if a {@link ContentNode} is selected.
	 *
	 * @param   node    Node to test.
	 *
	 * @return  <code>true</code> if selected;
	 *          <code>false</code> otherwise.
	 */
	protected abstract boolean isNodeSelected( final ContentNode node );

	/**
	 * Apply style for selected nodes.
	 *
	 * @param   style   Style to filter.
	 * @param   node    Content node being styled.
	 *
	 * @return  Filtered style.
	 */
	protected abstract RenderStyle applySelectionStyle( final RenderStyle style, final ContentNode node );

	/**
	 * Apply style for unselected nodes.
	 *
	 * @param   style   Style to filter.
	 * @param   node    Content node being styled.
	 *
	 * @return  Filtered style.
	 */
	protected abstract RenderStyle applyUnselectedStyle( final RenderStyle style, final ContentNode node );
}
