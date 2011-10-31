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
package ab.j3d.awt;

import java.beans.*;
import java.util.*;

import ab.j3d.view.*;
import com.numdata.oss.*;
import com.numdata.oss.ui.*;

/**
 * This action switches the rendering policy of a {@link View3D}.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class SwitchRenderingPolicyAction
	extends EnumChoiceAction
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
	 */
	public SwitchRenderingPolicyAction( final Locale locale, final View3D view )
	{
		super( ResourceBundleTools.getBundle( RenderingPolicy.class, locale ), RenderingPolicy.class );
		_view = view;

		view.addPropertyChangeListener( new PropertyChangeListener()
		{
			public void propertyChange( final PropertyChangeEvent e )
			{
				if ( View3D.RENDERING_POLICY_PROPERTY.equals( e.getPropertyName() ) )
				{
					setSelectedValue( e.getNewValue() );
				}
			}
		} );

		setSelectedValue( view.getRenderingPolicy() );
	}

	public void run()
	{
		_view.setRenderingPolicy( (RenderingPolicy)getSelectedValue() );
	}
}
