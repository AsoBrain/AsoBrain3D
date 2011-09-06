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
package ab.j3d.view;

import java.util.*;

import ab.j3d.*;
import com.numdata.oss.*;
import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * This action switches the projection policy of a {@link View3D}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class SelectViewPointAction
	extends ChoiceAction
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
	 * Standard avaiable view points.
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
	 * Avaiable view points to choose from.
	 */
	private final List<ViewPoint> _viewPoints;

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
	public SelectViewPointAction( final Locale locale, final View3D view, final ViewPoint defaultViewPoint )
	{
		final List<ViewPoint> viewPoints = STANDARD_VIEW_POINTS;
		if ( !viewPoints.contains( defaultViewPoint ) )
		{
			throw new IllegalArgumentException( "Unknown view point" );
		}

		_locale = locale;
		_view = view;
		_selectedViewPoint = null;
		_viewPoints = new ArrayList<ViewPoint>( viewPoints );

		setViewPoint( defaultViewPoint );
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
			final Matrix3D newOrientation = viewPoint._scene2view;
			view.setScene2View( newOrientation.setTranslation( oldScene2View.getTranslation() ) );
			view.zoomToFitScene();
			_selectedViewPoint = viewPoint;
			firePropertyChange( SELECTED_VALUE, oldViewPoint, viewPoint );
		}
	}

	@Override
	public Object getLabel( final Object value )
	{
		final Object result;

		if ( value instanceof ViewPoint )
		{
			final ViewPoint viewPoint = (ViewPoint) value;
			result = viewPoint.getDescription( _locale );
		}
		else
		{
			result = value;
		}

		return result;
	}

	@Override
	public ViewPoint[] getValues()
	{
		final List<ViewPoint> viewPoints = _viewPoints;
		return viewPoints.toArray( new ViewPoint[ viewPoints.size() ] );
	}

	@Override
	public ViewPoint getSelectedValue()
	{
		return _selectedViewPoint;
	}

	@Override
	public void setSelectedValue( final Object selectedValue )
	{
		if ( selectedValue instanceof  ViewPoint )
		{
			setViewPoint( (ViewPoint) selectedValue );
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
		 * Description of view point.
		 */
		@NotNull
		final LocalizableString _description;

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
			_description = new ResourceBundleString( SelectViewPointAction.class, name );
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
		 * Get description of view point.
		 *
		 * @param   locale  Locale to get description for.
		 *
		 * @return  Description of view point.
		 */
		@NotNull
		public String getDescription( final Locale locale )
		{
			final String result = _description.get( locale );
			return ( result == null ) ? getName() : result;
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
