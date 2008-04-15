/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2008
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

import java.awt.Graphics2D;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * This implementation of {@link PlaneControl} moves the selected node on a
 * fixed plane.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PlaneMoveControl
	extends AbstractPlaneControl
{
	/**
	 * Transformation from drag plane to WCS.
	 */
	private Matrix3D _plane2wcs;

	/**
	 * Drag plane is two-sided vs. one-sided.
	 */
	private final boolean _twoSidedPlane;

	/**
	 * Node transform when drag operation started.
	 */
	private Matrix3D _startTransform;

	/**
	 * Manipulation mode.
	 */
	public static enum ManipulationMode
	{
		/** Not manipulating anything. */ NONE ,
		/** Move.                      */ MOVE ,
		/** Rotate.                    */ ROTATE ,
	}

	/**
	 * Current manipulation mode.
	 */
	private ManipulationMode _manipulationMode;

	/**
	 * Create control.
	 *
	 * @param   plane2wcs       Transformation from drag plane to WCS.
	 * @param   twoSidedPlane   Plane is two-sided vs. one-sided.
	 */
	public PlaneMoveControl( final Matrix3D plane2wcs , final boolean twoSidedPlane )
	{
		_plane2wcs = plane2wcs;
		_twoSidedPlane = twoSidedPlane;
		_startTransform = null;
		_manipulationMode = ManipulationMode.NONE;
	}

	public Matrix3D getPlane2Wcs()
	{
		return _plane2wcs;
	}

	public boolean isPlaneTwoSided()
	{
		return _twoSidedPlane;
	}

	/**
	 * Get manipulation mode.
	 *
	 * @return  Manipulation mode.
	 */
	public ManipulationMode getManipulationMode()
	{
		return _manipulationMode;
	}

	public boolean mousePressed( final ControlInputEvent event , final ViewModelNode viewModelNode , final Vector3D wcsStart )
	{
		final ManipulationMode manipulationMode;

		if ( super.mousePressed( event , viewModelNode , wcsStart ) )
		{
			manipulationMode = event.isControlDown() ? ManipulationMode.ROTATE : ManipulationMode.MOVE;
		}
		else
		{
			manipulationMode = ManipulationMode.NONE;
		}

		_startTransform = viewModelNode.getTransform();
		_manipulationMode = manipulationMode;

		return ( manipulationMode != ManipulationMode.NONE );
	}

	public void mouseDragged( final ControlInputEvent event , final ViewModelNode viewModelNode , final Vector3D wcsPoint )
	{
		super.mouseDragged( event , viewModelNode , wcsPoint );

		drag( viewModelNode , event );
	}

	public void mouseReleased( final ControlInputEvent event , final ViewModelNode viewModelNode , final Vector3D wcsPoint )
	{
		super.mouseReleased( event , viewModelNode , wcsPoint );
		drag( viewModelNode , event );
	}

	public void paint( final ViewModelView view , final Graphics2D g2d )
	{
	}

	/**
	 * Perform drag operation.
	 *
	 * @param   viewModelNode   Node that is being controlled.
	 * @param   event           Event from control.
	 */
	protected void drag( final ViewModelNode viewModelNode , final ControlInputEvent event )
	{
		switch ( getManipulationMode() )
		{
			case MOVE :
				viewModelNode.setTransform( _startTransform.setTranslation( getNodeEnd() ) );
				break;

			case ROTATE :
				final double toDegrees         = Math.toDegrees( ViewModelView.DEFAULT_PIXELS_TO_RADIANS_FACTOR );
				final double verticalDegrees   = -toDegrees * (double)event.getDragDeltaY();
				final double horizontalDegrees =  toDegrees * (double)event.getDragDeltaX();

				final Matrix3D rotation = Matrix3D.getTransform( 0.0 , 0.0 , verticalDegrees - horizontalDegrees , 0.0 , 0.0 , 0.0 );
				viewModelNode.setTransform( rotation.multiply( _startTransform ) );
				break;
		}
	}

	/**
	 * Get end point for node of drag operation in the world coordinate system (WCS).
	 *
	 * @return  End point for node of drag operation in WCS.
	 */
	protected Vector3D getNodeEnd()
	{
		final Matrix3D nodeTransform = _startTransform;
		final Vector3D wcsDelta      = getWcsDelta();

		return wcsDelta.plus( nodeTransform.xo , nodeTransform.yo , nodeTransform.zo );
	}
}
