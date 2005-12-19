/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
package ab.j3d.control.controltest.model;

import java.util.Set;
import java.util.HashSet;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * The Model contains a number of {@link SceneElement}s, which can be added and
 * removed. A {@link SceneElement} can be selected, as can a face on a
 * {@link TetraHedron}.<p>
 * {@link PropertyChangeListener}s can register themselves to receive events
 * when the model or the selection changes.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class Model
	implements PropertyChangeListener
{

	/**
	 * The property name for model change events.
	 * {@link PropertyChangeListener}s listening to this property will be
	 * notified when the model has changed. This means new objects have been
	 * added or other objects have been removed.
	 *
	 * @see #addPropertyChangeListener
	 */
	public static final String MODEL_CHANGED = "model changed";

	/**
	 * The property name for selection change events.
	 * {@link PropertyChangeListener}s listening to this property will be
	 * notified when another {@link SceneElement} has been selected.
	 *
	 * @see #setSelection
	 * @see #getSelection
	 * @see #addPropertyChangeListener
	 */
	public static final String SELECTION_CHANGED = "selection changed";

	/**
	 * The property name for face selection change events.
	 * {@link PropertyChangeListener}s listening to this property will be
	 * notified when the another face has been selected.
	 *
	 * @see #setSelectedFace
	 * @see #getSelectedFace
	 * @see #addPropertyChangeListener
	 */
	public static final String FACE_SELECTION_CHANGED = "face selection changed";

	/**
	 * A {@link Set} containing all {@link SceneElement}s in this model.
	 */
	private final Set _scene;

	/**
	 * The selected TetraHedron (may be null if nothing is selected).
	 */
	private SceneElement _selection;

	/**
	 * The selected TetraHedron face.
	 */
	private PaintableTriangle _selectedFace;

	/**
	 * The {@link PropertyChangeSupport} to assist in registering the
	 * {@link PropertyChangeListener}s.
	 */
	private final PropertyChangeSupport _pcs;

	/**
	 * Construct new Model.
	 */
	public Model()
	{
		_scene = new HashSet();

		_selection = null;
		_selectedFace = null;

		_pcs = new PropertyChangeSupport( this );
	}

	/**
	 * Returns a set with all {@link SceneElement}s in the model.
	 *
	 * @return  a set with all {@link SceneElement}s in the model
	 */
	public Set getScene()
	{
		return new HashSet( _scene );
	}

	/**
	 * Returns the selected {@link SceneElement}. This method may return null,
	 * if nothing is selected.
	 *
	 * @return  currently selected {@link SceneElement}. <code>null</code> if
	 *          nothing is selected.
	 */
	public SceneElement getSelection()
	{
		return _selection;
	}

	/**
	 * Set the selected {@link SceneElement}. If <code>element</code> is null,
	 * nothing will be selected.<p>
	 * If the selection changes because of a call to this method, a
	 * {@link PropertyChangeEvent} is fired to all
	 * {@link PropertyChangeListener}s listening to
	 * <code>SELECTION_CHANGED</code>.
	 *
	 * @param   element     {@link SceneElement} that should be selected.
	 *
	 * @see #SELECTION_CHANGED
	 */
	public void setSelection( final SceneElement element )
	{
		setSelectedFace( null );

		final SceneElement old = _selection;
		_selection = element;

		_pcs.firePropertyChange( SELECTION_CHANGED , old , element );
	}

	/**
	 * Returns the selected {@link PaintableTriangle}. This may return null, if
	 * nothing is selected.
	 *
	 * @return  Selected {@link PaintableTriangle}.
	 */
	public PaintableTriangle getSelectedFace()
	{
		return _selectedFace;
	}

	/**
	 * Set the selected {@link PaintableTriangle}. If <code>face</code> is null,
	 * nothing is selected.<p>
	 * If the selection changes because of a call to this method, a
	 * {@link PropertyChangeEvent} is fired to all
	 * {@link PropertyChangeListener}s listening to
	 * <code>FACE_SELECTION_CHANGED</code>.
	 *
	 * @param   face    {@link PaintableTriangle} that should be selected.
	 *
	 * @see #FACE_SELECTION_CHANGED
	 */
	public void setSelectedFace( final PaintableTriangle face )
	{
		final SceneElement selection = _selection;

		if ( selection != null && selection instanceof TetraHedron )
		{
			final PaintableTriangle old = _selectedFace;
			_selectedFace = face;
			_pcs.firePropertyChange( FACE_SELECTION_CHANGED , old , face );
		}
	}

	/**
	 * Adds a {@link SceneElement} to this model.
	 * {@link PropertyChangeListener}s listening to <code>MODEL_CHANGED</code>
	 * events will be notified of the change.
	 *
	 * @param   element     {@link SceneElement} to add to the scene.
	 *
	 * @see #MODEL_CHANGED
	 */
	public void addSceneElement( final SceneElement element )
	{
		_scene.add( element );
		element.addPropertyChangeListener( SceneElement.ELEMENT_CHANGED , this );
		_pcs.firePropertyChange( MODEL_CHANGED , null , this );
	}

	/**
	 * Removes a {@link SceneElement} from this model.
	 * {@link PropertyChangeListener}s listening to <code>MODEL_CHANGED</code>
	 * events will be notified of the change.
	 *
	 * @param   element     {@link SceneElement} to remove.
	 *
	 * @see #MODEL_CHANGED
	 */
	public void removeSceneElement( final SceneElement element )
	{
		_scene.remove( element );
		element.removePropertyChangeListener( SceneElement.ELEMENT_CHANGED , this );
		_pcs.firePropertyChange( MODEL_CHANGED , null , this );
	}

	/**
	 * Add a {@link PropertyChangeListener} to this model. The listener will be
	 * notified when the model changes.
	 *
	 * @param   p   The property to listen for. Must be one of
	 *              {@link #MODEL_CHANGED},
	 *              {@link #SELECTION_CHANGED} or
	 *              {@link #FACE_SELECTION_CHANGED}.
	 * @param   l   The Listener to add
	 */
	public void addPropertyChangeListener( final String p, final PropertyChangeListener l )
	{
		_pcs.addPropertyChangeListener( p , l );
	}

	/**
	 * Removes a {@link PropertyChangeListener} from this model.
	 *
	 * @param   p   The property the listener was listening to. Must be one of
	 *              {@link #MODEL_CHANGED},
	 *              {@link #SELECTION_CHANGED} or
	 *              {@link #FACE_SELECTION_CHANGED}.
	 * @param   l   The Listener to remove.
	 */
	public void removePropertyChangeListener( final String p, final PropertyChangeListener l )
	{
		_pcs.removePropertyChangeListener( p , l );
	}

	/**
	 * When one of the {@link SceneElement}s in the scene is changed, this
	 * method is called. (The model listens to all elements in the scene). It
	 * then throws a {@link PropertyChangeEvent} to all
	 * {@link PropertyChangeListener}s listening to
	 * {@link SceneElement#ELEMENT_CHANGED} events.
	 *
	 * @param   evt     {@link PropertyChangeEvent} that was thrown.
	 *
	 * @see SceneElement#ELEMENT_CHANGED
	 */
	public void propertyChange( final PropertyChangeEvent evt )
	{
		final String propertyName = evt.getPropertyName();

		if ( propertyName.equals( SceneElement.ELEMENT_CHANGED ) )
		{
			_pcs.firePropertyChange( SceneElement.ELEMENT_CHANGED , null , evt.getNewValue() );
		}
	}
}