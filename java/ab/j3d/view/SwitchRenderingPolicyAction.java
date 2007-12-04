/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import java.util.Locale;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ui.ChoiceAction;

/**
 * This action switches the rendering policy of a {@link ViewModelView}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class SwitchRenderingPolicyAction
	extends ChoiceAction
{
	/**
	 * The {@link ViewModelView} this action belongs to.
	 */
	final ViewModelView _view;

	/**
	 * Construct a new action to switch the rendering policy of a view.
	 *
	 * @param   locale          Preferred locale for internationalization.
	 * @param   view            The view this action belongs to.
	 * @param   currentPolicy   Current rendering policy of the view.
	 */
	public SwitchRenderingPolicyAction( final Locale locale , final ViewModelView view , final ViewModelView.RenderingPolicy currentPolicy )
	{

		super( ResourceBundleTools.getBundle( ViewModelView.class , locale ) , currentPolicy );
		_view = view;
	}

	public void run()
	{
		_view.setRenderingPolicy( (ViewModelView.RenderingPolicy)getSelectedValue() );
	}
}