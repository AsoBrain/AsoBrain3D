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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.GeometryTools;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Scene;
import ab.j3d.view.View3D;
import ab.j3d.view.ViewControlInput;

import com.numdata.oss.MathTools;

/**
 * Basic implementation of a control to drag stuff.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class DragControl
	extends MouseControl
{
	/**
	 * Registered draggers.
	 */
	private final List<Dragger> _draggers = new ArrayList<Dragger>();

	/**
	 * Active dragger.
	 */
	protected Dragger _activeDragger = null;

	/**
	 * Dragged node.
	 */
	protected ContentNode _draggedNode = null;

	/**
	 * Plane used for dragging operation.
	 */
	protected Matrix3D _wcsDragPlane = null;

	/**
	 * Calculate plane to use for dragging operation. Parameters and result
	 * are all in the same coordinate system. This makes sure that the
	 * pointer ray is not parallel to the reference Z=0 plane. The result is
	 * the reference translates to the specified point.
	 *
	 * @param   orientedPlane   Reference plane for dragging.
	 * @param   pointerRay      Pointer ray.
	 * @param   point           Point on drag plane.
	 *
	 * @return  Plane on which dragging should take place;
	 *          <code>null</code> if no drag plane is available.
	 */
	public static Matrix3D getDragPlane( final Matrix3D orientedPlane  , final Ray3D pointerRay , final Vector3D point )
	{
		final Vector3D pointerDirection = pointerRay.getDirection();
		final boolean pointerRayParallelToPlane = MathTools.almostEqual( 0.0 , Vector3D.dot( orientedPlane .xz , orientedPlane .yz , orientedPlane .zz , pointerDirection.x , pointerDirection.y , pointerDirection.z ) );
		return !pointerRayParallelToPlane ? orientedPlane .setTranslation( point ) : null;
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		EventObject result = event;

		if ( isEnabled() && event.isMouseButton1Down() )
		{
			final ViewControlInput viewControlInput = (ViewControlInput)event.getSource();
			final View3D           view             = viewControlInput.getView();
			final Scene            scene            = view.getScene();

			for( final Face3DIntersection intersection : event.getIntersections() )
			{
				final ContentNode node = scene.getContentNode( intersection.getObjectID() );
				if ( node != null )
				{
					if ( dragStart( event , node , intersection ) )
					{
						if ( _activeDragger != null )
						{
							startCapture( event );
						}
						result = null;
						break;
					}
				}
			}
		}

		return result;
	}

	public EventObject mouseDragged( final ControlInputEvent event )
	{
		final EventObject result;

		if ( _activeDragger != null )
		{
			dragTo( event );
			result = null;
		}
		else
		{
			result = super.mouseDragged( event );
		}

		return result;
	}

	public EventObject mouseReleased( final ControlInputEvent event )
	{
		final EventObject result;

		if ( _activeDragger != null )
		{
			dragTo( event );
			dragStop();
			result = null;
		}
		else
		{
			result = super.mouseReleased( event );
		}

		return result;
	}

	/**
	 * Add dragger to this control.
	 *
	 * @param   dragger     Dragger to add.
	 */
	public void addDragger( final Dragger dragger )
	{
		if ( dragger == null )
			throw new NullPointerException( "dragger" );

		_draggers.add( dragger );
	}

	/**
	 * Try to start drag operation.
	 *
	 * @param   event           Event from control.
	 * @param   node            Node to test (never <code>null</code>).
	 * @param   intersection    Intersection with node.
	 *
	 * @return  <code>true</code> to stop trying to drag;
	 *          <code>false</code> if we should continue.
	 */
	protected boolean dragStart( final ControlInputEvent event , final ContentNode node , final Face3DIntersection intersection )
	{
		boolean result = false;

		for ( final Dragger dragger : _draggers )
		{
			final Matrix3D wcsDragPlane = dragger.dragStart( event , node , event.getPointerRay() , intersection );
			if ( wcsDragPlane != null )
			{
				_activeDragger = dragger;
				_draggedNode = node;
				_wcsDragPlane = wcsDragPlane;
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Perform dragging.
	 *
	 * @param   event   Event from control.
	 */
	protected void dragTo( final ControlInputEvent event )
	{
		final Dragger activeDragger = _activeDragger;
		if ( activeDragger != null )
		{
			final Matrix3D wcsDragPlane = _wcsDragPlane;

			final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( wcsDragPlane , true , event.getPointerRay() );
			if ( wcsPoint != null )
			{
				activeDragger.dragTo( event , _draggedNode , wcsDragPlane.inverseRotate( wcsPoint.minus( wcsDragPlane.xo , wcsDragPlane.yo , wcsDragPlane.zo ) ) );
			}
		}
	}

	/**
	 * End drag operation.
	 *
	 * @param   event   Event from control.
	 */
	protected void dragEnd( final ControlInputEvent event )
	{
		_activeDragger.dragEnd( event , _draggedNode );
	}

	/**
	 * Stop dragging.
	 */
	protected void dragStop()
	{
		_activeDragger = null;
		_draggedNode = null;
		_wcsDragPlane = null;
		stopCapture();
	}

	/**
	 * Interface to drag nodes.
	 */
	public static interface Dragger
	{
		/**
		 * Calculate plane to use for dragging operation. Parameters and result
		 * are in world coordinates (WCS).
		 *
		 * @param   event           Event from control.
		 * @param   node            Node to drag.
		 * @param   pointerRay      Pointer ray.
		 * @param   intersection    Intersection between pointer and node.
		 *
		 * @return  Plane on which dragging should take place (plane to wcs);
		 *          <code>null</code> if no drag plane is available.
		 */
		Matrix3D dragStart( ControlInputEvent event , ContentNode node , Ray3D pointerRay , Face3DIntersection intersection );

		/**
		 * Drag node.
		 *
		 * @param   event       Event from control.
		 * @param   node        Node being dragged.
		 * @param   planeDelta  Movement on plane during drag operation.
		 */
		void dragTo( ControlInputEvent event , ContentNode node, Vector3D planeDelta );

		/**
		 * End drag operation.
		 *
		 * @param   event       Event from control.
		 * @param   node        Node being dragged.
		 */
		void dragEnd( ControlInputEvent event , ContentNode node );
	}
}