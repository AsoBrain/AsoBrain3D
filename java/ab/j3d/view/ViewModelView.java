/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ab.j3d.Matrix3D;

/**
 * This class defines a view in the view model.
 *
 * @see     ViewModel
 * @see     ViewControl
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class ViewModelView
{
	/**
	 * Perspective projection policy constant.
	 *
	 * @see     #setProjectionPolicy
	 */
	public static final int PERSPECTIVE = 0;

	/**
	 * Parallel projection policy constant.
	 *
	 * @see     #setProjectionPolicy
	 */
	public static final int PARALLEL = 1;

	/**
	 * Application-assigned ID of this view.
	 */
	private final Object _id;

	/**
	 * Control for this view.
	 */
	private final ViewControl _viewControl;

	/**
	 * Property change event listener.
	 */
	private final PropertyChangeListener _propertyChangeListener =
		new PropertyChangeListener()
		{
			public void propertyChange( final PropertyChangeEvent evt )
			{
				update();
			}
		};

	/**
	 * Construct new view.
	 *
	 * @param   id              Application-assigned ID of this view.
	 * @param   viewControl     Control to use for this view.
	 */
	protected ViewModelView( final Object id , final ViewControl viewControl )
	{
		_id = id;
		_viewControl = viewControl;
		viewControl.addPropertyChangeListener( "transform" , _propertyChangeListener );
	}

	/**
	 * Get application-assigned ID of this view.
	 *
	 * @return  Application-assigned ID of this view.
	 */
	public final Object getID()
	{
		return _id;
	}

	/**
	 * Get control for this view.
	 *
	 * @return  Control for this view.
	 */
	public final ViewControl getViewControl()
	{
		return _viewControl;
	}

	/**
	 * Get view transform from view control.
	 *
	 * @return  View transform from view control.
	 */
	public final Matrix3D getViewTransform()
	{
		final ViewControl viewControl = getViewControl();

		return viewControl.getTransform();
	}

	/**
	 * Get graphical reprsentation of view as AWT component.
	 *
	 * @return  Component that represents this view.
	 */
	public abstract Component getComponent();

	/**
	 * Update contents of view. This may be the result of changes to the 3D
	 * scene or view control.
	 */
	public abstract void update();

	/**
	 * Set projection policy of this view. The policy can be either
	 * <code>PERSPECTIVE</code> or <code>PARALLEL</code>.
	 *
	 * @param   policy      Projection policy of this view
	 *                      (<code>PERSPECTIVE</code> or <code>PARALLEL</code>).
	 */
	public abstract void setProjectionPolicy( final int policy );
}
