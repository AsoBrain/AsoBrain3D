/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2001-2004 Numdata BV
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
package ab.j3d.view.java3d;

import javax.media.j3d.Canvas3D;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;

import ab.j3d.view.MouseViewControl;
import ab.j3d.view.MouseViewEvent;
import ab.j3d.view.MouseViewListener;

/**
 * Replacement of <code>OrbitBehavior</code> for more manageable controls. Drag
 * movements are translated to fixed rotation axes, not relative to the current
 * state of a matrix.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class SimpleOrbitBehavior
	extends OrbitBehavior
	implements MouseViewListener
{
	/**
	 * Mouse view control for behavior.
	 */
	private final MouseViewControl _mouseViewControl;

	/**
	 * Creates a new <code>SimpleOrbitBehavior</code>.
	 *
	 * @param   c       The Canvas3D to add the behavior to.
	 * @param   unit    Unit scale factor (e.g. <code>MM</code>).
	 */
	public SimpleOrbitBehavior( final Canvas3D c , final float unit )
	{
		super( c , 0 );

		_mouseViewControl = new MouseViewControl( c , unit );
		_mouseViewControl.addMouseViewListener( this );
		_mouseViewControl._controlX[ 0 ] =  MouseViewControl.ROTATION_Y;
		_mouseViewControl._controlX[ 1 ] = -MouseViewControl.TRANSLATION_X;
		_mouseViewControl._controlX[ 2 ] =  MouseViewControl.DISABLED;

		_mouseViewControl._controlY[ 0 ] =  MouseViewControl.ROTATION_X;
		_mouseViewControl._controlY[ 1 ] = -MouseViewControl.TRANSLATION_Y;
		_mouseViewControl._controlY[ 2 ] =  MouseViewControl.TRANSLATION_Z;
	}

	protected synchronized void integrateTransforms()
	{
		targetTG.setTransform( Java3dTools.convertMatrix3DToTransform3D( _mouseViewControl.getTransform() ) );
	}

	public void mouseViewChanged( final MouseViewEvent event )
	{
		integrateTransforms();
	}
}
