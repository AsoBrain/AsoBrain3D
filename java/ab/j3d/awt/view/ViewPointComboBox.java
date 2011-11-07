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
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * This action switches the projection policy of a {@link View3D}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ViewPointComboBox
	extends JComboBox
{
	/**
	 * View transform for front view.
	 */
	public static final ViewPoint FRONT_VIEW = new ViewPoint( "front", new Matrix3D(
		 1.0,  0.0,  0.0, 0.0,
		 0.0,  0.0,  1.0, 0.0,
		 0.0, -1.0,  0.0, 0.0 ) );

	/**
	 * View transform for rear view.
	 */
	public static final ViewPoint REAR_VIEW = new ViewPoint( "rear", new Matrix3D(
		-1.0,  0.0,  0.0, 0.0,
		 0.0,  0.0,  1.0, 0.0,
		 0.0,  1.0,  0.0, 0.0 ) );

	/**
	 * View transform for top view.
	 */
	public static final ViewPoint TOP_VIEW = new ViewPoint( "top", new Matrix3D(
		 1.0,  0.0,  0.0, 0.0,
		 0.0,  1.0,  0.0, 0.0,
		 0.0,  0.0,  1.0, 0.0 ) );

	/**
	 * View transform for bottom view.
	 */
	public static final ViewPoint BOTTOM_VIEW = new ViewPoint( "bottom", new Matrix3D(
		 1.0,  0.0,  0.0, 0.0,
		 0.0, -1.0,  0.0, 0.0,
		 0.0,  0.0, -1.0, 0.0 ) );

	/**
	 * View transform for right view.
	 */
	public static final ViewPoint RIGHT_VIEW = new ViewPoint( "right", new Matrix3D(
		 0.0,  1.0,  0.0, 0.0,
		 0.0,  0.0,  1.0, 0.0,
		 1.0,  0.0,  0.0, 0.0 ) );

	/**
	 * View transform for left view.
	 */
	public static final ViewPoint LEFT_VIEW = new ViewPoint( "left", new Matrix3D(
		 0.0, -1.0,  0.0, 0.0,
		 0.0,  0.0,  1.0, 0.0,
		-1.0,  0.0,  0.0, 0.0 ) );

	/**
	 * View transform for isometric view.
	 */
	public static final ViewPoint ISOMETRIC_VIEW = new ViewPoint( "isometric", new Matrix3D(
		Math.sqrt( 0.5 ), Math.sqrt( 0.5 ),              0.0, 0.0,
		            -0.5,              0.5, Math.sqrt( 0.5 ), 0.0,
		             0.5,             -0.5, Math.sqrt( 0.5 ), 0.0 ) );

	/**
	 * Name of resource bundle for this class.
	 */
	private static final String BUNDLE_NAME = ViewPointComboBox.class.getPackage().getName() + ".LocalStrings";

	/**
	 * Standard available view points.
	 */
	public static final List<ViewPoint> STANDARD_VIEW_POINTS = Arrays.asList( FRONT_VIEW, REAR_VIEW, TOP_VIEW, BOTTOM_VIEW, RIGHT_VIEW, LEFT_VIEW, ISOMETRIC_VIEW );

	/**
	 * Locale to use.
	 */
	protected final Locale _locale;

	/**
	 * The {@link View3D} this action belongs to.
	 */
	protected final View3D _view;

	/**
	 * Currently selected view point.
	 */
	private ViewPoint _selectedViewPoint;

	/**
	 * Construct a new action to switch the projection policy of a view.
	 *
	 * @param   locale              Locale to use.
	 * @param   view                The view this action belongs to.
	 * @param   defaultViewPoint    Default view point.
	 */
	public ViewPointComboBox( final Locale locale, final View3D view, final ViewPoint defaultViewPoint )
	{
		final List<ViewPoint> viewPoints = STANDARD_VIEW_POINTS;
		if ( !viewPoints.contains( defaultViewPoint ) )
		{
			throw new IllegalArgumentException( "Unknown view point" );
		}

		_locale = locale;
		_view = view;
		_selectedViewPoint = null;

		final ResourceBundle bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );

		final Map<ViewPoint,String> labels = new HashMap<ViewPoint, String>();
		for ( final ViewPoint viewPoint : viewPoints )
		{
			String label = viewPoint.getName();
			try
			{
				label = bundle.getString( label );
			}
			catch ( MissingResourceException e )
			{
				/* ignore */
			}
			labels.put( viewPoint, label );
		}

		setViewPoint( defaultViewPoint );
		setSelectedItem( defaultViewPoint );


		final JComboBox comboBox = new JComboBox( viewPoints.toArray() );
		comboBox.setMaximumSize( comboBox.getPreferredSize() );

		final ListCellRenderer originalRenderer = comboBox.getRenderer();
		comboBox.setRenderer( new ListCellRenderer()
		{
			public Component getListCellRendererComponent( final JList list , final Object value , final int index , final boolean isSelected , final boolean cellHasFocus )
			{
				return originalRenderer.getListCellRendererComponent( list , labels.get( value ) , index , isSelected , cellHasFocus );
			}
		} );

		comboBox.addItemListener( new ItemListener()
		{
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					final ViewPoint viewPoint = (ViewPoint) e.getItem();
					setViewPoint( viewPoint );
				}
			}
		} );
	}

	/**
	 * Get current view point.
	 *
	 * @return  Current view point.
	 */
	public ViewPoint getViewPoint()
	{
		return _selectedViewPoint;
	}

	/**
	 * Set view point.
	 *
	 * @param   viewPoint   View point to set.
	 */
	public void setViewPoint( final ViewPoint viewPoint )
	{
		final ViewPoint oldViewPoint = _selectedViewPoint;
		if ( viewPoint != oldViewPoint )
		{
			final View3D view = _view;
			final Matrix3D oldScene2View = view.getScene2View();
			final Matrix3D newOrientation = viewPoint.getScene2view();
			view.setScene2View( newOrientation.setTranslation( oldScene2View.getTranslation() ) );
			view.zoomToFitScene();
			_selectedViewPoint = viewPoint;
			setSelectedItem( viewPoint );
		}
	}

	/**
	 * Definition of view point.
	 */
	public static class ViewPoint
	{
		/**
		 * Name of view point.
		 */
		@NotNull
		final String _name;

		/**
		 * Transform for view point.
		 */
		@NotNull
		final Matrix3D _scene2view;

		/**
		 * Create view point.
		 *
		 * @param   name            Name of view point.
		 * @param   scene2view      Transform for view point.
		 */
		public ViewPoint( @NotNull final String name, @NotNull final Matrix3D scene2view )
		{
			if ( !scene2view.isRighthanded() || !Vector3D.ZERO.equals( scene2view.getTranslation() ) )
			{
				throw new IllegalArgumentException( "Invalid transform: " + scene2view.toFriendlyString() );
			}

			_name = name;
			_scene2view = scene2view;
		}

		/**
		 * Get name of view point.
		 *
		 * @return  Name of view point.
		 */
		@NotNull
		public String getName()
		{
			return _name;
		}

		/**
		 * Get transform for view point.
		 *
		 * @return  Transform for view point.
		 */
		@NotNull
		public Matrix3D getScene2view()
		{
			return _scene2view;
		}

		@NotNull
		@Override
		public String toString()
		{
			return _name;
		}
	}
}
