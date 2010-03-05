/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2010
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ui.ChoiceAction;

/**
 * This action switches the rendering policy of a {@link View3D}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class SwitchRenderingPolicyAction
	extends ChoiceAction
{
	/**
	 * The {@link View3D} this action belongs to.
	 */
	final View3D _view;

	/**
	 * Construct a new action to switch the rendering policy of a view.
	 *
	 * @param   locale          Preferred locale for internationalization.
	 * @param   view            The view this action belongs to.
	 * @param   currentPolicy   Current rendering policy of the view.
	 */
	public SwitchRenderingPolicyAction( final Locale locale , final View3D view , final RenderingPolicy currentPolicy )
	{
		super( ResourceBundleTools.getBundle( RenderingPolicy.class , locale ) , RenderingPolicy.class );
		_view = view;

		view.addPropertyChangeListener( new PropertyChangeListener()
		{
			@Override
			public void propertyChange( final PropertyChangeEvent e )
			{
				if ( View3D.RENDERING_POLICY_PROPERTY.equals( e.getPropertyName() ) )
				{
					setSelectedValue( e.getNewValue() );
				}
			}
		} );

		setSelectedValue( currentPolicy );
	}

	public void run()
	{
		_view.setRenderingPolicy( (RenderingPolicy)getSelectedValue() );
	}
}
