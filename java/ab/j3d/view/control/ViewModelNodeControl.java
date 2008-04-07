/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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
package ab.j3d.view.control;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.EventObject;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.control.MouseControl;
import ab.j3d.geom.BasicPlane3D;
import ab.j3d.geom.GeometryTools;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.view.OverlayPainter;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.control.planar.PlanarGraphics2D;
import ab.j3d.view.control.planar.SubPlaneControl;

/**
 * This class implements the default control and overlay painter for
 * {@link ViewModelNode}s. It is automatically attached to all
 * {@link ViewModelView}s by {@link ViewModel}.
 *
 * @author  Jark Reijerink
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ViewModelNodeControl
	extends MouseControl
	implements OverlayPainter
{
	/**
	 * View model that contains all view nodes.
	 */
	private ViewModel _viewModel = null;

	/**
	 * Currently node being controlled.
	 */
	private ViewModelNode _activeNode = null;

	/**
	 * Currently active sub-plane control.
	 */
	private SubPlaneControl _activeSubPlaneControl = null;

	/**
	 * Construct control that implements common view node behavior.
	 *
	 * @param   viewModel   View model on which this control is created.
	 */
	public ViewModelNodeControl( final ViewModel viewModel )
	{
		_viewModel = viewModel;
	}

	public EventObject mouseClicked( final ControlInputEvent event )
	{
		EventObject result = event;

		if ( event.getMouseButton() == MouseEvent.BUTTON3 )
		{
			for( final Face3DIntersection intersection : event.getIntersections() )
			{
				final ViewModelNode viewModelNode = _viewModel.getNode( intersection.getObjectID() );
				if ( viewModelNode != null )
				{
					final List<Action> actions = viewModelNode.getContextActions();
					if ( !actions.isEmpty() )
					{
						final JPopupMenu popup = new ViewModelNodePopupMenu( event , viewModelNode , intersection );

						for ( final Action action : actions )
							popup.add( action );

						popup.show( event.getSourceComponent() , event.getX() , event.getY() );
						result = null;
						break;
					}
				}
			}
		}

		return result;
	}

	public EventObject mousePressed( final ControlInputEvent event )
	{
		EventObject result = event;

		for( final Face3DIntersection intersection : event.getIntersections() )
		{
			final ViewModelNode node = _viewModel.getNode( intersection.getObjectID() );
			if ( node != null )
			{
				for ( final SubPlaneControl subPlaneControl : node.getSubPlaneControls() )
				{
					if ( subPlaneControl.isEnabled() )
					{
						final Point2D dragPoint = getSubPlaneDragPoint( node , subPlaneControl, event.getPointerRay() , true );
						if ( dragPoint != null )
						{
							_activeNode            = node;
							_activeSubPlaneControl = subPlaneControl;

							subPlaneControl.mousePressed( event , node , dragPoint.getX() , dragPoint.getY() );
							_viewModel.updateOverlay();

							startCapture( event );
							result = null;
							break;
						}
					}
				}
			}

			if ( result == null )
				break;
		}

		return result;
	}

	public void mouseDragged( final ControlInputEvent event )
	{
		final ViewModelNode   activeNode            = _activeNode;
		final SubPlaneControl activeSubPlaneControl = _activeSubPlaneControl;

		if ( ( activeNode != null ) && ( activeSubPlaneControl != null ) )
		{
			final Point2D dragPoint = getSubPlaneDragPoint( activeNode , activeSubPlaneControl, event.getPointerRay() , false );
			if ( dragPoint != null )
			{
				activeSubPlaneControl.mouseDragged( event , activeNode , dragPoint.getX() , dragPoint.getY() );
				_viewModel.updateOverlay();
			}
		}
	}

	public void mouseReleased( final ControlInputEvent event )
	{
		final ViewModelNode   activeNode            = _activeNode;
		final SubPlaneControl activeSubPlaneControl = _activeSubPlaneControl;

		if ( ( activeNode != null ) && ( activeSubPlaneControl != null ) )
		{
			final Point2D dragPoint = getSubPlaneDragPoint( activeNode , activeSubPlaneControl, event.getPointerRay() , false );
			if ( dragPoint != null )
			{
				activeSubPlaneControl.mouseReleased( event , activeNode , dragPoint.getX() , dragPoint.getY() );
				_viewModel.updateOverlay();
			}
		}

		_activeNode            = null;
		_activeSubPlaneControl = null;
	}

	public void paint( final ViewModelView view , final Graphics2D g2d )
	{
		final ViewModelNode   activeNode            = _activeNode;
		final SubPlaneControl activeSubPlaneControl = _activeSubPlaneControl;

		if ( ( activeNode != null ) && ( activeSubPlaneControl != null ) )
		{
			final Matrix3D  node2wcs   = activeNode.getTransform();
			final Matrix3D  plane2node = activeSubPlaneControl.getPlane2Node();
			final Matrix3D  plane2wcs  = plane2node.multiply( node2wcs );
			final Matrix3D  wcs2view   = view.getViewTransform();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
			activeSubPlaneControl.paint( view , planarGraphics2D );
		}
	}

	/**
	 * Get point on 2D drag plane defined by a node's {@link SubPlaneControl}.
	 *
	 * @param   viewModelNode       Node whose behavior is used.
	 * @param   subPlaneControl     {@link SubPlaneControl} that defined the drag behavior.
	 * @param   pointerRayWCS       Pointer ray to determine drag point.
	 * @param   mustBeWithinBounds  If set, only return point if it falls within
	 *                              the drag bounds.
	 *
	 * @return  Point on 2D drag plane;
	 *          <code>null</code> if no drag point was found.
	 */
	public static Point2D getSubPlaneDragPoint( final ViewModelNode viewModelNode , final SubPlaneControl subPlaneControl , final Ray3D pointerRayWCS , final boolean mustBeWithinBounds )
	{
		Point2D result = null;

		final Matrix3D     plane2node   = subPlaneControl.getPlane2Node();
		final Matrix3D     node2wcs     = viewModelNode.getTransform();
		final Matrix3D     plane2wcs    = plane2node.multiply( node2wcs );
		final BasicPlane3D dragPlaneWCS = new BasicPlane3D( plane2wcs , subPlaneControl.isPlaneTwoSided() );

		final Vector3D intersectPointWCS = GeometryTools.getIntersectionBetweenRayAndPlane( dragPlaneWCS , pointerRayWCS );
		if( intersectPointWCS != null )
		{
			final Vector3D intersectionPointPlane = plane2wcs.inverseMultiply( intersectPointWCS );

			final double dragX = intersectionPointPlane.x;
			final double dragY = intersectionPointPlane.y;

			if ( !mustBeWithinBounds || ( ( dragX >= 0.0 ) && ( dragY >= 0.0 ) && ( dragX <= subPlaneControl.getPlaneWidth() ) && ( dragY <= subPlaneControl.getPlaneHeight() ) ) )
			{
				result = new Point2D.Double( dragX, dragY );
			}
		}

		return result;
	}
}
