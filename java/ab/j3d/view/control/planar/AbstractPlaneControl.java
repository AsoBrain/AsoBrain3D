/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2010
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
package ab.j3d.view.control.planar;

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.model.ContentNode;
import ab.j3d.view.View3D;

/**
 * This class provides some basic functionality of a {@link PlaneControl}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlaneControl
	implements PlaneControl
{
	/**
	 * Flag to indicate that this behavior is 'active'.
	 */
	private boolean _active;

	/**
	 * Start point of drag operation in WCS.
	 */
	private Vector3D _wcsStart;

	/**
	 * End point of drag operation in WCS.
	 */
	private Vector3D _wcsEnd;

	/**
	 * List of registered views.
	 */
	private List<View3D> _views = new ArrayList<View3D>();

	/**
	 * Construct new control.
	 */
	protected AbstractPlaneControl()
	{
		_active   = false;
		_wcsStart = null;
		_wcsEnd   = null;
	}

	/**
	 * Test if this control is active. Meaning that is currently being used.
	 *
	 * @return  <code>true</code> if this control is active;
	 *          <code>false</code> otherwise.
	 */
	public boolean isActive()
	{
		return _active;
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void addView( final View3D view )
	{
		_views.add( view );
	}

	public void removeView( final View3D view )
	{
		_views.remove( view );
	}

	/**
	 * Update all views.
	 */
	protected void updateViews()
	{
		for ( final View3D view : _views )
		{
			view.update();
		}
	}

	public boolean mousePressed( final ControlInputEvent event , final ContentNode contentNode , final Vector3D wcsStart )
	{
		_wcsStart = wcsStart;
		_wcsEnd   = null;
		_active   = true;

		return isActive();
	}

	public void mouseDragged( final ControlInputEvent event , final ContentNode contentNode , final Vector3D wcsEnd )
	{
		_wcsEnd = wcsEnd;

	}

	public void mouseReleased( final ControlInputEvent event , final ContentNode contentNode , final Vector3D wcsEnd )
	{
		_wcsEnd = wcsEnd;
		_active = false;
	}

	/**
	 * Get start point of drag operation in the world coordinate system (WCS).
	 *
	 * @return  Start point of drag operation in WCS.
	 */
	public Vector3D getWcsStart()
	{
		return _wcsStart;
	}

	/**
	 * Get end point of drag operation in the world coordinate system (WCS).
	 *
	 * @return  End point of drag operation in WCS.
	 */
	public Vector3D getWcsEnd()
	{
		return _wcsEnd;
	}

	/**
	 * Get movement of drag operation in the world coordinate system (WCS).
	 *
	 * @return  Movement of drag operation in WCS.
	 */
	public Vector3D getWcsDelta()
	{
		return ( _wcsEnd != null ) && ( _wcsStart != null ) ? _wcsEnd.minus( _wcsStart ) : Vector3D.ZERO;
	}

	/**
	 * Get movement of drag operation on plane. This is always a 2D movement
	 * (the Z component of the result is always 0).
	 *
	 * @return  Movement of drag operation on plane.
	 */
	public Vector3D getPlaneDelta()
	{
		final Matrix3D plane2wcs = getPlane2Wcs();
		return plane2wcs.inverseTransform( getWcsDelta() );
	}
}
