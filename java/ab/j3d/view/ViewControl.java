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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.Action;

import ab.j3d.Matrix3D;

import com.numdata.oss.ResourceBundleTools;
import com.numdata.oss.ui.BasicAction;

/**
 * This abstract class defined a control(ler) for a view in the view model.
 * <p />
 * The basic function of a view control is providing a view transform (this is
 * available as a bound '<code>transform</code>' property, so property change
 * listeners may act on it).
 * <p />
 * A view is normally controlled through mouse operations, so the
 * <code>DragListener</code> is implemented. Most mouse behavior should be
 * implemented by descendant classes, but the following default behavior is
 * implemented:
 * <dl>
 *  <dt>Double-clicking the left mouse button.</dt>
 *  <dd>Restore last saved view control state (calls <code>restore()</code>).</dd>
 *
 *  <dt>Double-clicking any other mouse button.</dt>
 *  <dd>Save view control state (calls <code>save()</code>).</dd>
 * </dl>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class ViewControl
	implements DragListener
{
	/** Action ID: Save settings.    */ public static final String SAVE_ACTION     = "save";
	/** Action ID: Restore settings. */ public static final String RESTORE_ACTION  = "restore";

	/**
	 * View control event (reused to avoid too much garbage).
	 */
	protected final PropertyChangeSupport _pcs = new PropertyChangeSupport( this );

	/**
	 * Current view transform value.
	 *
	 * @see     #getTransform()
	 * @see     #setTransform(Matrix3D)
	 */
	private Matrix3D _transform;

	/**
	 * Construct view control.
	 */
	protected ViewControl()
	{
		_transform = Matrix3D.INIT;
	}

	/**
	 * Get actions of the view control.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 *
	 * @return  Actions of the view control.
	 */
	public Action[] getActions( final Locale locale )
	{
		final ResourceBundle res = ResourceBundleTools.getBundle( ViewControl.class , locale );

		return new Action[]
			{
				new BasicAction( res , RESTORE_ACTION ) {
					public void actionPerformed( final ActionEvent event )
					{
						restore();
					} } ,

				new BasicAction( res , SAVE_ACTION ) {
					public void actionPerformed( final ActionEvent event )
					{
						save();
					} } ,
			};
	}

	/**
	 * Add a <code>PropertyChangeListener</code> to the listener list. The
	 * listener is registered for all properties.
	 *
	 * @param   listener    Listener to be added
	 */
	public final void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( listener );
	}

	/**
	 * Remove a <code>PropertyChangeListener</code> from the listener list. This
	 * removes a listener that was registered for all properties.
	 *
	 * @param   listener    Listener to be removed
	 */
	public final void removePropertyChangeListener( final PropertyChangeListener listener )
	{
		_pcs.removePropertyChangeListener( listener );
	}

	/**
	 * Add a <code>PropertyChangeListener</code> for a specific property. The
	 * listener will be invoked only when a that specific property is changed.
	 *
	 * @param   propertyName    Name of the property to listen on.
	 * @param   listener        Listener to be added
	 */
	public final void addPropertyChangeListener( final String propertyName , final PropertyChangeListener listener )
	{
		_pcs.addPropertyChangeListener( propertyName , listener );
	}

	/**
	 * Remove a <code>PropertyChangeListener</code> for a specific property.
	 *
	 * @param   propertyName    Name of the property that was listened on.
	 * @param   listener        Listener to be removed
	 */
	public final void removePropertyChangeListener( final String propertyName , final PropertyChangeListener listener )
	{
		_pcs.removePropertyChangeListener( propertyName , listener );
	}

	/**
	 * Get view transform.
	 *
	 * @return  View transform.
	 */
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set view transform value.
	 *
	 * @param   transform   View transform.
	 */
	protected void setTransform( final Matrix3D transform )
	{
		final Matrix3D oldTransform = _transform;
		_transform = transform;
		_pcs.firePropertyChange( "transform" , oldTransform , transform );
	}

	public void dragStart( final DragEvent event )
	{
		if ( event.getClickCount() == 2 )
		{
			switch ( event.getButtonNumber() )
			{
				case 0 : /* button #1 - restore saved state */
					restore();
					break;

				default : /* button #2 and beyond - save state */
					save();
					break;
			}
		}
	}

	public void dragTo( final DragEvent event )
	{
		switch ( event.getButtonNumber() )
		{
			case 0 :
				dragLeftButton( event );
				break;

			case 1 :
				dragMiddleButton( event );
				break;

			case 2 :
				dragRightButton( event );
				break;
		}
	}

	public void dragStop( final DragEvent event )
	{
	}

	/**
	 * Handle drag operation with left mouse button. This method is called by
	 * the <code>dragTo()</code> method.
	 *
	 * @param   event   Drag event.
	 */
	protected abstract void dragLeftButton( final DragEvent event );

	/**
	 * Handle drag operation with middle mouse button. This method is called by
	 * the <code>dragTo()</code> method.
	 *
	 * @param   event   Drag event.
	 */
	protected abstract void dragMiddleButton( final DragEvent event );

	/**
	 * Handle drag operation with right mouse button. This method is called by
	 * the <code>dragTo()</code> method.
	 *
	 * @param   event   Drag event.
	 */
	protected abstract void dragRightButton( final DragEvent event );

	/**
	 * Save current view control settings. The saved settings can be restored
	 * later using the <code>restore()</code> method.
	 *
	 * @see     #restore()
	 */
	public abstract void save();

	/**
	 * Restore current view control settings. The saved settings can be restored
	 * later using the <code>restore()</code> method.
	 *
	 * @see     #save()
	 */
	public abstract void restore();


	/**
	 * Save settings into a <code>Properties</code> object. This may set any
	 * property value, but may not assign any value to the reserved keys
	 * '<code>type</code>' and '<code>class</code>'.
	 *
	 * @param   settings    Properties to save settings to.
	 *
	 * @see     #loadSettings(Properties)
	 */
	public abstract void saveSettings( Properties settings );

	/**
	 * Load settings from a <code>Properties</code> object that were previously
	 * saved using the <code>saveSettings()</code> method. The implementation
	 * should ignore errors in the saved settings silently.
	 *
	 * @param   settings    Properties to load settings from.
	 *
	 * @see     #saveSettings(Properties)
	 */
	public abstract void loadSettings( Properties settings );
}
