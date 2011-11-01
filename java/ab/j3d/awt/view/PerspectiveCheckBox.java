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
 * This check box toggles the perspective projection of a view on/off.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PerspectiveCheckBox
	extends JCheckBox
{
	/**
	 * Create action.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 * @param   view    View to create action for.
	 */
	public PerspectiveCheckBox( final Locale locale, final View3D view )
	{
		setOpaque( false );

		final ResourceBundle bundle = ResourceBundle.getBundle( "LocalStrings", locale );
		setText( bundle.getString( "togglePerspective" ) );

		setModel( new ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return ( view.getProjectionPolicy() == ProjectionPolicy.PERSPECTIVE );
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );

				if ( value != isSelected() )
				{
					view.setProjectionPolicy( value ? ProjectionPolicy.PERSPECTIVE : ProjectionPolicy.PARALLEL );
				}
			}
		} );

		setSelected( view.getProjectionPolicy() == ProjectionPolicy.PERSPECTIVE );
	}
}
