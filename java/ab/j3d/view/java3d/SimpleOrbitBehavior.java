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

import ab.j3d.view.DragEvent;
import ab.j3d.view.DragListener;
import ab.j3d.view.DragSupport;

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
	implements DragListener
{
	/**
	 * Drag control for behavior.
	 */
	private final DragSupport _dragSupport;

	/**
	 * Creates a new <code>SimpleOrbitBehavior</code>.
	 *
	 * @param   c       The Canvas3D to add the behavior to.
	 * @param   unit    Unit scale factor (e.g. <code>MM</code>).
	 */
	public SimpleOrbitBehavior( final Canvas3D c , final float unit )
	{
		super( c , 0 );

		_dragSupport = new DragSupport( c , unit );
		_dragSupport.addDragListener( this );
		_dragSupport._controlX[ 0 ] =  DragSupport.ROTATION_Y;
		_dragSupport._controlX[ 1 ] = -DragSupport.TRANSLATION_X;
		_dragSupport._controlX[ 2 ] =  DragSupport.DISABLED;

		_dragSupport._controlY[ 0 ] =  DragSupport.ROTATION_X;
		_dragSupport._controlY[ 1 ] = -DragSupport.TRANSLATION_Y;
		_dragSupport._controlY[ 2 ] =  DragSupport.TRANSLATION_Z;
	}

	protected synchronized void integrateTransforms()
	{
		targetTG.setTransform( Java3dTools.convertMatrix3DToTransform3D( _dragSupport.getTransform() ) );
	}

	public void dragStart( final DragEvent event )
	{
	}

	public void dragTo( final DragEvent event )
	{
		integrateTransforms();
	}

	public void dragStop( final DragEvent event )
	{
	}
}
