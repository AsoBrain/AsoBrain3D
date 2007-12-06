/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
import ab.j3d.view.control.planar.PlanarControl;
import ab.j3d.view.control.planar.PlanarGraphics2D;

/**
 * This class implements default controls for {@link ViewModelNode}s. It is
 * automatically attached to all {@link ViewModelView}s by {@link ViewModel}.
 *
 * @author  Jark Reijerink
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
	 * Currently active drag behavior.
	 */
	private PlanarControl _activePlanarControl = null;

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
						final JPopupMenu popup = new ViewModelNodePopupMenu( event , viewModelNode, intersection );

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
			final ViewModelNode viewModelNode = _viewModel.getNode( intersection.getObjectID() );
			if ( viewModelNode != null )
			{
				for ( final PlanarControl planarControl : viewModelNode.getPlanarControls() )
				{
					final Point2D dragPoint = getPlanarDragPoint( viewModelNode , planarControl, event.getPointerRay() , true );
					if ( dragPoint != null )
					{
						_activeNode          = viewModelNode;
						_activePlanarControl = planarControl;

						planarControl.mousePressed( event , viewModelNode , dragPoint.getX() , dragPoint.getY() );
						_viewModel.updateOverlay();

						startCapture( event );
						result = null;
						break;
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
		final ViewModelNode activeNode          = _activeNode;
		final PlanarControl activePlanarControl = _activePlanarControl;

		if ( ( activeNode != null ) && ( activePlanarControl != null ) )
		{
			final Point2D dragPoint = getPlanarDragPoint( activeNode , activePlanarControl, event.getPointerRay() , false );
			if ( dragPoint != null )
			{
				activePlanarControl.mouseDragged( event , activeNode , dragPoint.getX() , dragPoint.getY() );
				_viewModel.updateOverlay();
			}
		}
	}

	public void mouseReleased( final ControlInputEvent event )
	{
		final ViewModelNode activeNode          = _activeNode;
		final PlanarControl activePlanarControl = _activePlanarControl;

		if ( ( activeNode != null ) && ( activePlanarControl != null ) )
		{
			final Point2D dragPoint = getPlanarDragPoint( activeNode , activePlanarControl, event.getPointerRay() , false );
			if ( dragPoint != null )
			{
				activePlanarControl.mouseReleased( event , activeNode , dragPoint.getX() , dragPoint.getY() );
				_viewModel.updateOverlay();
			}
		}

		_activeNode          = null;
		_activePlanarControl = null;
	}

	public void paint( final ViewModelView view , final Graphics2D g2d )
	{
		final ViewModelNode dragNode      = _activeNode;
		final PlanarControl planarControl = _activePlanarControl;

		if ( ( dragNode != null ) && ( planarControl != null ) )
		{
			final Matrix3D  node2wcs   = dragNode.getTransform();
			final Matrix3D  plane2node = planarControl.getPlane2Node();
			final Matrix3D  plane2wcs  = plane2node.multiply( node2wcs );
			final Matrix3D  wcs2view   = view.getViewTransform();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
			planarControl.paint( view , planarGraphics2D );
		}
	}

	/**
	 * Get point on 2D drag plane defined by a node's {@link PlanarControl}.
	 *
	 * @param   viewModelNode       Node whose behavior is used.
	 * @param   planarControl        {@link PlanarControl} that defined the drag behavior.
	 * @param   pointerRayWCS       Pointer ray to determine drag point.
	 * @param   mustBeWithinBounds  If set, only return point if it falls within
	 *                              the drag bounds.
	 *
	 * @return  Point on 2D drag plane;
	 *          <code>null</code> if no drag point was found.
	 */
	public static Point2D getPlanarDragPoint( final ViewModelNode viewModelNode , final PlanarControl planarControl, final Ray3D pointerRayWCS , final boolean mustBeWithinBounds )
	{
		Point2D result = null;

		final Matrix3D     plane2node   = planarControl.getPlane2Node();
		final Matrix3D     node2wcs     = viewModelNode.getTransform();
		final Matrix3D     plane2wcs    = plane2node.multiply( node2wcs );
		final BasicPlane3D dragPlaneWCS = new BasicPlane3D( plane2wcs , planarControl.isPlaneTwoSided() );

		final Vector3D intersectPointWCS = GeometryTools.getIntersectionBetweenRayAndPlane( dragPlaneWCS , pointerRayWCS );
		if( intersectPointWCS != null )
		{
			final Vector3D intersectionPointPlane = plane2wcs.inverseMultiply( intersectPointWCS );

			final double dragX = intersectionPointPlane.x;
			final double dragY = intersectionPointPlane.y;

			if ( !mustBeWithinBounds || ( ( dragX >= 0.0 ) && ( dragY >= 0.0 ) && ( dragX <= planarControl.getPlaneWidth() ) && ( dragY <= planarControl.getPlaneHeight() ) ) )
			{
				result = new Point2D.Double( dragX, dragY );
			}
		}

		return result;
	}
}
