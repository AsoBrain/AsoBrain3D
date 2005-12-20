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
import ab.j3d.control.SceneInputTranslator;
import ab.j3d.control.ControlEventQueue;
import ab.j3d.control.Control;

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
	 * Rendering policy: solid.
	 * <p />
	 * This should result in a photorealistic rendering of the scene, taking the
	 * physical properties of the scene into account as much as possible.
	 * <p />
	 * Example implementation: ray-tracing / per-pixel shading and texture mapping.
	 *
	 * @see     #setRenderingPolicy
	 */
	public static final int SOLID = 0;

	/**
	 * Rendering policy: schematic.
	 * <p />
	 * This should clarify the structure and design of the scene. This is
	 * generally a form that should allow manipulation of (large) objects in a
	 * scene and could be used to provide dimension information.
	 * <p />
	 * Example implementation: flat shading / functional color coding.
	 *
	 * @see     #setRenderingPolicy
	 */
	public static final int SCHEMATIC = 1;

	/**
	 * Rendering policy: schematic.
	 * <p />
	 * A non-photorealistic rendering method that give a good idea of what is
	 * intended by the scene, but does not require much detail.
	 * <p />
	 * Example implementation: pencil sketch / cartoon rendering / silhouette.
	 *
	 * @see     #setRenderingPolicy
	 */
	public static final int SKETCH = 2;

	/**
	 * Rendering policy: wireframe.
	 * <p />
	 * Technical rendering including only edges, points, or iconic
	 * representations of elements in a scene. This is the classical rendering
	 * method in CAD software. This provides a quick overview and insight to
	 * the complexity of a scene.
	 * <p />
	 * Example implementation: pencil sketch / cartoon rendering / silhouette.
	 *
	 * @see     #setRenderingPolicy
	 */
	public static final int WIREFRAME = 3;

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
	 * Set projection policy of this view.
	 *
	 * @param   policy      Projection policy of this view
	 *                      ({@link Projector#PERSPECTIVE},
	 *                      {@link Projector#ISOMETRIC}, or
	 *                      {@link Projector#PARALLEL}).
	 */
	public abstract void setProjectionPolicy( final int policy );

	/**
	 * Set rendering policy of this view.
	 *
	 * @param   policy      Projection policy of this view
	 *                      ({@link #SOLID}, {@link #SCHEMATIC},
	 *                      {@link #SKETCH}, or {@link #WIREFRAME}).
	 */
	public abstract void setRenderingPolicy( final int policy );

	/**
	 * Returns the {@link Projector} for this View.
	 *
	 * @return  the {@link Projector} for this view
	 */
	protected abstract Projector getProjector ();

	/**
	 * Returns wether or not this ViewModelView has a
	 * {@link SceneInputTranslator}.
	 *
	 * @return  <code>true</code> if there is a SceneInputTranslator,
	 *          <code>false</code> otherwise.
	 */
	protected boolean hasInputTranslator()
	{
		return false;
	}

	/**
	 * Returns the {@link SceneInputTranslator}, if this class has one. The
	 * method {@link #hasInputTranslator()} can be used to
	 * check before calling this function.
	 *
	 * @return  The {@link SceneInputTranslator} for this view
	 */
	protected SceneInputTranslator getInputTranslator()
	{
		return null;
	}

	/**
	 * Adds a {@link Control} to this view. This control is added to the end of
	 * the list of controls. <p>Note that not all views support
	 * controls. This can be check with the method
	 * {@link ViewModel#supportsControls()}.
	 *
	 * @param   control     The {@link Control} to add
	 */
	public final void addControl( final Control control )
	{
		final SceneInputTranslator inputTranslator = getInputTranslator();
		final ControlEventQueue queue = inputTranslator.getEventQueue();

		addControl( queue.size() , control);
	}

	/**
	 * Adds a {@link Control} to this view. This control is added to  the list
	 * of Controls at <code>index</code>. <p>Note that not all views support
	 * Controls. This can be checked with the method
	 * {@link ViewModel#supportsControls()}.
	 *
	 * @param   index       The index at which to place the control
	 * @param   control     The {@link Control} to add
	 */
	public final void addControl( final int index , final Control control )
	{
		if ( hasInputTranslator() )
		{
			final SceneInputTranslator inputTranslator = getInputTranslator();
			final ControlEventQueue queue = inputTranslator.getEventQueue();

			if ( index < 0 || index > queue.size() )
				throw new IllegalArgumentException( "The given index must be greater than 0 and smaller or equal to the number of controllers" );

			queue.addControl( index , control );
		}
	}

	/**
	 * Removes a {@link Control} from the list of controls.
	 * <p>Note that not all views support Controls. This can be checked with the
	 * method {@link ViewModel#supportsControls()}.
	 *
	 * @param   control     The Control to remove
	 */
	public final void removeControl( final Control control )
	{
		if ( hasInputTranslator() )
		{
			final SceneInputTranslator inputTranslator = getInputTranslator();
			final ControlEventQueue queue = inputTranslator.getEventQueue();

			queue.removeControl( control );
		}
	}

	/**
	 * Temporary method, can be removed when camera3d/projector classes are implemented correctly
	 *
	 * @return The field of view (45 degrees).
	 */
	public static double getFieldOfView()
	{
		return 45.0;
	}
}