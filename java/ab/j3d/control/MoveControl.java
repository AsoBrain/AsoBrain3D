/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.util.EventObject;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicPlane3D;
import ab.j3d.geom.GeometryTools;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

import com.numdata.oss.MathTools;

/**
 * Basic implementation of a control to move/rotate/scale stuff.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class MoveControl
	extends MouseControl
{
	/**
	 * Manipulation mode.
	 */
	public static enum ManipulationMode
	{
		/**
		 * Not manipulating anything.
		 */
		NONE ,

		/**
		 * Move.
		 */
		MOVE ,

		/**
		 * Rotate.
		 */
		ROTATE ,

		/**
		 * Scale.
		 */
		SCALE ,
	}

	/**
	 * Current manipulation mode.
	 */
	private ManipulationMode _manipulationMode = ManipulationMode.NONE;

	/**
	 * Manipulated node.
	 */
	private ViewModelNode _manipulatedNode = null;

	/**
	 * Plane used for dragging operation. This is the control plane translated
	 * to the initial intersection point.
	 */
	private Matrix3D _dragPlane2wcs = null;

	/**
	 * Start point of drag operation in WCS.
	 */
	private Vector3D _wcsStart = null;

	public boolean isEnabled()
	{
		return ( getPlane2wcs() != null );
	}

	/**
	 * Get plane to use for manipulation.
	 *
	 * @return  Plane to use for manipluation;
	 *          <code>null</code> if no such plane is currently available.
	 */
	protected abstract Matrix3D getPlane2wcs();

	public EventObject mousePressed( final ControlInputEvent event )
	{
		EventObject result = event;

		if ( isEnabled() )
		{
			final ViewControlInput viewControlInput = (ViewControlInput)event.getSource();
			final ViewModelView    view             = viewControlInput.getView();
			final ViewModel        viewModel        = view.getModel();

			for( final Face3DIntersection intersection : event.getIntersections() )
			{
				final ViewModelNode node = viewModel.getNode( intersection.getObjectID() );
				if ( node != null )
				{
					final Object   target           = node.getID();
					final Vector3D wcsPoint         = intersection.getIntersectionPoint();
					final Matrix3D controlPlane2wcs = getPlane2wcs();
					final Matrix3D plane2wcs        = controlPlane2wcs.setTranslation( wcsPoint );

					final Ray3D    pointerRay       = event.getPointerRay();
					final Vector3D pointerDirection = pointerRay.getDirection();

					if ( !MathTools.almostEqual( Vector3D.dot( pointerDirection.x , pointerDirection.y , pointerDirection.z , plane2wcs.xz , plane2wcs.yz , plane2wcs.zz ) , 0.0 ) )
					{
						_wcsStart         = wcsPoint;
						_manipulationMode = dragStart( event , target );
						_manipulatedNode  = node;
						_dragPlane2wcs    = plane2wcs;

						startCapture( event );
						result = null;
						break;
					}
				}
			}
		}

		return result;
	}

	public void mouseDragged( final ControlInputEvent event )
	{
		if ( _manipulationMode != ManipulationMode.NONE )
		{
			final Matrix3D dragPlane2wcs = _dragPlane2wcs;

			final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( new BasicPlane3D( dragPlane2wcs , false ) , event.getPointerRay() );
			if ( wcsPoint != null )
			{
				final Object   target     = _manipulatedNode.getID();
				final Vector3D wcsDelta   = wcsPoint.minus( _wcsStart );
				final Vector3D planeDelta = dragPlane2wcs.inverseRotate( wcsDelta );

				switch ( _manipulationMode )
				{
					case MOVE :
						move( target , planeDelta );
						break;

					case ROTATE :
						rotate( target , planeDelta );
						break;
				}
			}
		}
	}

	public void mouseReleased( final ControlInputEvent event )
	{
		if ( _manipulationMode != ManipulationMode.NONE )
		{
			mouseDragged( event );
			dragEnd( _manipulatedNode.getID() );

			_manipulationMode = ManipulationMode.NONE;
			_manipulatedNode = null;
		}
	}

	/**
	 * Start drag operation.
	 *
	 * @param   event       Event from control.
	 * @param   target      Target object.
	 *
	 * @return  Manipulation mode to use.
	 */
	protected abstract ManipulationMode dragStart( final ControlInputEvent event , final Object target );

	/**
	 * Move target object.
	 *
	 * @param   target      Target object.
	 * @param   planeDelta  Movement on plane during drag operation.
	 */
	protected abstract void move( final Object target , final Vector3D planeDelta );

	/**
	 * Rotate target object.
	 *
	 * @param   target      Target object.
	 * @param   planeDelta  Movement on plane during drag operation.
	 */
	protected abstract void rotate( final Object target , final Vector3D planeDelta );

	/**
	 * Finish drag operation.
	 *
	 * @param   target      Target object.
	 */
	protected abstract void dragEnd( final Object target );
}