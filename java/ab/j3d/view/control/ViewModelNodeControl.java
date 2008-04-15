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
import ab.j3d.view.control.planar.PlaneControl;
import ab.j3d.view.control.planar.SubPlaneControl;

import com.numdata.oss.MathTools;

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
	private ViewModelNode _node = null;

	/**
	 * Drag plane in WCS for active control.
	 */
	private Matrix3D _plane2wcs = null;

	/**
	 * Currently active plane control
	 */
	private PlaneControl _planeControl = null;

	/**
	 * Currently active sub-plane control.
	 */
	private SubPlaneControl _subPlaneControl = null;

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

		final ViewModel viewModel = _viewModel;

		for( final Face3DIntersection intersection : event.getIntersections() )
		{
			final ViewModelNode node = viewModel.getNode( intersection.getObjectID() );
			if ( node != null )
			{
				final PlaneControl planeControl = node.getPlaneControl();
				if ( ( planeControl != null ) && planeControl.isEnabled() )
				{
					final Vector3D wcsPoint         = intersection.getIntersectionPoint();
					final Matrix3D controlPlane2wcs = planeControl.getPlane2Wcs();
					final Matrix3D plane2wcs        = controlPlane2wcs.setTranslation( wcsPoint );

					final Ray3D    pointerRay       = event.getPointerRay();
					final Vector3D pointerDirection = pointerRay.getDirection();

					if ( !MathTools.almostEqual( Vector3D.dot( pointerDirection.x , pointerDirection.y , pointerDirection.z , plane2wcs.xz , plane2wcs.yz , plane2wcs.zz ) , 0.0 ) &&
					     planeControl.mousePressed( event , node , wcsPoint ) )
					{
						_node         = node;
						_plane2wcs    = plane2wcs;
						_planeControl = planeControl;

						viewModel.updateOverlay();

						startCapture( event );
						result = null;
					}
				}

				if ( result != null )
				{
					for ( final SubPlaneControl subPlaneControl : node.getSubPlaneControls() )
					{
						if ( subPlaneControl.isEnabled() )
						{
							final Matrix3D plane2node = subPlaneControl.getPlane2Node();
							final Matrix3D node2wcs   = node.getTransform();
							final Matrix3D plane2wcs  = plane2node.multiply( node2wcs );

							final Vector3D wcsPoint = getPointerOnPlaneInWcs( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
							if ( wcsPoint != null )
							{
								final Vector3D planePoint = plane2wcs.inverseMultiply( wcsPoint );

								if ( ( planePoint.x >= 0.0 ) && ( planePoint.y >= 0.0 ) && ( planePoint.x <= subPlaneControl.getPlaneWidth() ) && ( planePoint.y <= subPlaneControl.getPlaneHeight() ) )
								{
									if ( subPlaneControl.mousePressed( event , node , planePoint.x , planePoint.y ) )
									{
										_node            = node;
										_plane2wcs       = plane2wcs;
										_subPlaneControl = subPlaneControl;

										viewModel.updateOverlay();

										startCapture( event );
										result = null;
										break;
									}
								}
							}
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
		final ViewModel       viewModel       = _viewModel;
		final ViewModelNode   node            = _node;
		final Matrix3D        plane2wcs       = _plane2wcs;
		final PlaneControl    planeControl    = _planeControl;
		final SubPlaneControl subPlaneControl = _subPlaneControl;

		if ( ( node != null ) && ( plane2wcs != null ) )
		{
			if ( planeControl != null )
			{
				final Vector3D wcsPoint = getPointerOnPlaneInWcs( plane2wcs , planeControl.isPlaneTwoSided() , event.getPointerRay() );
				if ( wcsPoint != null )
				{
					planeControl.mouseDragged( event , node , wcsPoint );
					viewModel.updateOverlay();
				}
			}

			if ( subPlaneControl != null )
			{
				final Vector3D wcsPoint = getPointerOnPlaneInWcs( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
				if ( wcsPoint != null )
				{
					final Vector3D planePoint = plane2wcs.inverseMultiply( wcsPoint );
					subPlaneControl.mouseDragged( event , node , planePoint.x , planePoint.y );
					viewModel.updateOverlay();
				}
			}
		}
	}

	public void mouseReleased( final ControlInputEvent event )
	{
		final ViewModel       viewModel       = _viewModel;
		final ViewModelNode   node            = _node;
		final Matrix3D        plane2wcs       = _plane2wcs;
		final PlaneControl    planeControl    = _planeControl;
		final SubPlaneControl subPlaneControl = _subPlaneControl;

		if ( ( node != null ) && ( plane2wcs != null ) )
		{
			if ( planeControl != null )
			{
				final Vector3D wcsPoint = getPointerOnPlaneInWcs( plane2wcs , planeControl.isPlaneTwoSided() , event.getPointerRay() );
				if ( wcsPoint != null )
				{
					planeControl.mouseReleased( event , node , wcsPoint );
					viewModel.updateOverlay();
				}
			}

			if ( subPlaneControl != null )
			{
				final Vector3D wcsPoint = getPointerOnPlaneInWcs( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
				if ( wcsPoint != null )
				{
					final Vector3D planePoint = plane2wcs.inverseMultiply( wcsPoint );
					subPlaneControl.mouseReleased( event , node , planePoint.x , planePoint.y );
					viewModel.updateOverlay();
				}
			}
		}

		_node            = null;
		_plane2wcs       = null;
		_planeControl    = null;
		_subPlaneControl = null;
	}

	public void paint( final ViewModelView view , final Graphics2D g2d )
	{
		final ViewModelNode   node            = _node;
		final Matrix3D        plane2wcs       = _plane2wcs;
		final PlaneControl    planeControl    = _planeControl;
		final SubPlaneControl subPlaneControl = _subPlaneControl;

		if ( node != null )
		{
			if ( planeControl != null )
			{
				final Matrix3D  wcs2view   = view.getViewTransform();
				final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
				final Projector projector  = view.getProjector();

				final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
				planeControl.paint( view , g2d );
				planarGraphics2D.dispose();
			}

			if ( subPlaneControl != null )
			{
				final Matrix3D  wcs2view   = view.getViewTransform();
				final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
				final Projector projector  = view.getProjector();

				final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
				subPlaneControl.paint( view , planarGraphics2D );
				planarGraphics2D.dispose();
			}
		}
	}

	/**
	 * Determine point in WCS where a plane and pointer ray intersect.
	 *
	 * @param   plane2wcs       Transformation from plane to WCS.
	 * @param   twoSidedPlane   Plane is two-sided.
	 * @param   pointerRay      Pointer ray in WCS.
	 *
	 * @return  Intersection point in WCS;
	 *
	 */
	protected static Vector3D getPointerOnPlaneInWcs( final Matrix3D plane2wcs , final boolean twoSidedPlane , final Ray3D pointerRay )
	{
		return GeometryTools.getIntersectionBetweenRayAndPlane( new BasicPlane3D( plane2wcs , twoSidedPlane ) , pointerRay );
	}

}
