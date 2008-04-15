/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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

import java.awt.event.ActionEvent;
import java.util.Locale;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ui.ToggleAction;

/**
 * This action toggles the grid on/off.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ToggleGridAction
	extends ToggleAction
{
	/**
	 * The {@link ViewModelView} this action belongs to.
	 */
	private ViewModelView _view;

	/**
	 * Create action.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 * @param   view    View to create action for.
	 */
	public ToggleGridAction( final Locale locale , final ViewModelView view )
	{
		super( ResourceBundleTools.getBundle( ToggleGridAction.class , locale ) , "toggleGrid" );
		_view = view;
	}

	public void actionPerformed( final ActionEvent e )
	{
		setValue( !getValue() );
	}

	public boolean getValue()
	{
		final ViewModelView view = _view;
		return view.isGridEnabled();
	}

	public void setValue( final boolean value )
	{
		final ViewModelView view = _view;
		if ( value != view.isGridEnabled() )
		{
			view.setGridEnabled( value );
			view.update();
		}
	}
}