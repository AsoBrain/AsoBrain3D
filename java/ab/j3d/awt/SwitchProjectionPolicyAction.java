/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2011 Peter S. Heijnen
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
 * This action switches the projection policy of a {@link View3D}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class SwitchProjectionPolicyAction
	extends EnumChoiceAction
{
	/**
	 * The {@link View3D} this action belongs to.
	 */
	private final View3D _view;

	/**
	 * Construct a new action to switch the projection policy of a view.
	 *
	 * @param   locale          Preferred locale for internationalization.
	 * @param   view            The view this action belongs to.
	 * @param   currentPolicy   Current projection policy of the view.
	 */
	public SwitchProjectionPolicyAction( final Locale locale, final View3D view, final ProjectionPolicy currentPolicy )
	{
		super( ResourceBundleTools.getBundle( ProjectionPolicy.class, locale ), ProjectionPolicy.class );
		_view = view;

		view.addPropertyChangeListener( new PropertyChangeListener()
		{
			public void propertyChange( final PropertyChangeEvent e )
			{
				if ( View3D.PROJECTION_POLICY_PROPERTY.equals( e.getPropertyName() ) )
				{
					setSelectedValue( e.getNewValue() );
				}
			}
		} );

		setSelectedValue( currentPolicy );
	}

	public void run()
	{
		_view.setProjectionPolicy( (ProjectionPolicy)getSelectedValue() );
	}
}
