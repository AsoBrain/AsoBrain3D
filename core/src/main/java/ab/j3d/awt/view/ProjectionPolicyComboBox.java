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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.view.*;

/**
 * This combo box switches the projection policy of a {@link View3D}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ProjectionPolicyComboBox
	extends JComboBox
{
	/**
	 * Name of resource bundle for this class.
	 */
	private static final String BUNDLE_NAME = ProjectionPolicyComboBox.class.getPackage().getName() + ".LocalStrings";

	/**
	 * Construct a new action to switch the projection policy of a view.
	 *
	 * @param   locale          Preferred locale for internationalization.
	 * @param   view            The view this action belongs to.
	 * @param   currentPolicy   Current projection policy of the view.
	 */
	public ProjectionPolicyComboBox( final Locale locale, final View3D view, final ProjectionPolicy currentPolicy )
	{
		super( ProjectionPolicy.values() );
		setSelectedItem( currentPolicy );
		setMaximumSize( getPreferredSize() );

		final ProjectionPolicy[] values = ProjectionPolicy.values();

		final ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );

		final Map<ProjectionPolicy,String> labels = new EnumMap<ProjectionPolicy, String>( ProjectionPolicy.class );
		for ( final ProjectionPolicy value : values )
		{
			labels.put( value, bundle.getString( value.name() ) );
		}

		final ListCellRenderer originalRenderer = getRenderer();
		setRenderer( new ListCellRenderer()
		{
			public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
			{
				return originalRenderer.getListCellRendererComponent( list, labels.get( value ), index, isSelected, cellHasFocus );
			}
		} );

		addItemListener( new ItemListener()
		{
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					view.setProjectionPolicy( (ProjectionPolicy) e.getItem() );
				}
			}
		} );

		view.addPropertyChangeListener( new PropertyChangeListener()
		{
			public void propertyChange( final PropertyChangeEvent e )
			{
				if ( View3D.PROJECTION_POLICY_PROPERTY.equals( e.getPropertyName() ) )
				{
					setSelectedItem( e.getNewValue() );
				}
			}
		} );
	}
}
