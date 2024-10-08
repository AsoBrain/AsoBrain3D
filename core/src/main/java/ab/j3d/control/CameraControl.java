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
package ab.j3d.control;

import java.awt.event.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.view.*;

/**
 * This abstract class defined a control(ler) for a 3D view.
 * <p />
 * The basic function of a camera control is providing a view transform (this is
 * available as a bound '<code>transform</code>' property, so property change
 * listeners may act on it).
 * <p />
 * A view is normally controlled through mouse operations, so the
 * <code>DragListener</code> is implemented. Most mouse behavior should be
 * implemented by descendant classes, but the following default behavior is
 * implemented:
 * <dl>
 *  <dt>Double-clicking the left mouse button.</dt>
 *  <dd>Restore last saved camera control state (calls <code>restore()</code>).</dd>
 *
 *  <dt>Double-clicking any other mouse button.</dt>
 *  <dd>Save camera control state (calls <code>save()</code>).</dd>
 * </dl>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class CameraControl
	extends MouseControl
{
	/**
	 * Action: Zoom to fit.
	 */
	public static final String ZOOM_TO_FIT = "zoomToFit";

	/**
	 * Action: Save settings.
	 */
	public static final String SAVE = "save";

	/**
	 * Action: Restore settings.
	 */
	public static final String RESTORE = "restore";

	/**
	 * View being controlled.
	 */
	protected final View3D _view;

	/**
	 * Construct camera control.
	 *
	 * @param   view    View to be controlled.
	 */
	protected CameraControl( final View3D view )
	{
		_view = view;
	}

	/**
	 * Get view transform.
	 *
	 * @return  View transform.
	 */
	public Matrix3D getScene2View()
	{
		return _view.getScene2View();
	}

	/**
	 * Set view transform value.
	 *
	 * @param   transform   View transform.
	 */
	protected void setScene2View( final Matrix3D transform )
	{
		_view.setScene2View( transform );
	}

	@Override
	public void mouseClicked( final ControlInputEvent event )
	{
		final MouseEvent mouseEvent = (MouseEvent)event.getInputEvent();
		if ( mouseEvent.getClickCount() == 2 )
		{
			switch ( mouseEvent.getButton() )
			{
				case MouseEvent.BUTTON1 : /* button #1 - restore saved state */
					restore();
					break;

				default : /* button #2 and beyond - save state */
					save();
					break;
			}
		}
	}

	@Override
	public void mousePressed( final ControlInputEvent event )
	{
		if ( !isCaptured() && isDragStartEvent( event ) )
		{
			startCaptureOnDrag( event );
		}
	}

	/**
	 * Test whether the specified event is used to start a dragging operation
	 * using this control.
	 *
	 * @param   event   Event to test (always {@link MouseEvent#MOUSE_PRESSED}).
	 *
	 * @return  {@code true} if the event is a match;
	 *          {@code false} otherwise.
	 */
	protected abstract boolean isDragStartEvent( final ControlInputEvent event );

	/**
	 * Adjust view to fit scene contents.
	 *
	 * @see     View3D#zoomToFitScene()
	 */
	public void zoomToFit()
	{
		_view.zoomToFitScene();
	}

	/**
	 * Save current camera control settings. The saved settings can be restored
	 * later using the <code>restore()</code> method.
	 *
	 * @see     #restore()
	 */
	public abstract void save();

	/**
	 * Restore current camera control settings. The saved settings can be restored
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
