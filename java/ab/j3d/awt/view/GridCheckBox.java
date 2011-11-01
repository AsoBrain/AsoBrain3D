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
package ab.j3d.awt.view;

import java.util.*;
import javax.swing.*;

import ab.j3d.view.*;

/**
 * This check box toggles the grid of a view on/off.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class GridCheckBox
	extends JCheckBox
{
	/**
	 * Create action.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 * @param   view    View to create action for.
	 */
	public GridCheckBox( final Locale locale, final View3D view )
	{
		setOpaque( false );

		final ResourceBundle bundle = ResourceBundle.getBundle( "LocalStrings", locale );
		setText( bundle.getString( "toggleGrid" ) );

		setModel( new ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				final Grid grid = view.getGrid();
				return grid.isEnabled();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );

				final Grid grid = view.getGrid();
				if ( value != grid.isEnabled() )
				{
					grid.setEnabled( value );
					view.update();
				}
			}
		} );

		final Grid grid = view.getGrid();
		setSelected( grid.isEnabled() );
	}
}
