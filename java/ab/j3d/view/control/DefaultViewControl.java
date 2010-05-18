/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2010
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.planar.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class implements the default control and overlay painter for
 * {@link ContentNode}s. It is automatically attached to all
 * {@link View3D}s by {@link RenderEngine}.
 *
 * @author  Jark Reijerink
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DefaultViewControl
	extends MouseControl
	implements ViewOverlay
{
	/**
	 * List of registered views.
	 */
	private List<View3D> _views = new ArrayList<View3D>();

	/**
	 * Currently node being controlled.
	 */
	private ContentNode _node = null;

	/**
	 * Drag plane in WCS for active control.
	 */
	private Matrix3D _plane2wcs = null;

	/**
	 * Currently active plane control.
	 */
	private PlaneControl _planeControl = null;

	/**
	 * Currently active sub-plane control.
	 */
	private SubPlaneControl _subPlaneControl = null;

	/**
	 * Currently active scene plane control.
	 */
	private ScenePlaneControl _scenePlaneControl = null;

	/**
	 * Construct control that implements common view node behavior.
	 */
	public DefaultViewControl()
	{
	}

	@Nullable
	@Override
	public EventObject mouseClicked( final ControlInputEvent event )
	{
		EventObject result = event;

		final MouseEvent mouseEvent = (MouseEvent)event.getInputEvent();
		if ( mouseEvent.getButton() == MouseEvent.BUTTON3 )
		{
			final ViewControlInput viewControlInput = event.getSource();
			final View3D view = viewControlInput.getView();
			final Scene scene = view.getScene();

			for( final Face3DIntersection intersection : event.getIntersections() )
			{
				final ContentNode contentNode = scene.getContentNode( intersection.getObjectID() );
				if ( contentNode != null )
				{
					final List<Action> actions = contentNode.getContextActions();
					if ( !actions.isEmpty() )
					{
						final JPopupMenu popup = new ContentNodeContextMenu( null , event , contentNode , intersection );

						for ( final Action action : actions )
						{
							popup.add( action );
						}

						popup.show( event.getSourceComponent() , event.getX() , event.getY() );
						result = null;
						break;
					}
				}
			}
		}

		return result;
	}

	@Override
	public EventObject mousePressed( final ControlInputEvent event )
	{
		EventObject result = event;

		final ViewControlInput viewControlInput = event.getSource();
		final View3D view = viewControlInput.getView();
		final Scene scene = view.getScene();

		for( final Face3DIntersection intersection : event.getIntersections() )
		{
			final ContentNode node = scene.getContentNode( intersection.getObjectID() );
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

						updateViews();

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

							final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
							if ( wcsPoint != null )
							{
								final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );

								if ( ( planePoint.x >= 0.0 ) && ( planePoint.y >= 0.0 ) && ( planePoint.x <= subPlaneControl.getPlaneWidth() ) && ( planePoint.y <= subPlaneControl.getPlaneHeight() ) )
								{
									if ( subPlaneControl.mousePressed( event , node , planePoint.x , planePoint.y ) )
									{
										_node            = node;
										_plane2wcs       = plane2wcs;
										_subPlaneControl = subPlaneControl;

										updateViews();

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
			{
				break;
			}
		}

		if ( result != null )
		{
			for ( final ScenePlaneControl planeControl : scene.getPlaneControls() )
			{
				if ( planeControl.isEnabled() )
				{
					final Matrix3D plane2wcs  = planeControl.getPlane2Wcs();

					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , planeControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );

						final Rectangle2D bounds = planeControl.getPlaneBounds();
						if ( bounds.contains( planePoint.x , planePoint.y ) )
						{
							if ( planeControl.mousePressed( event , planePoint.x , planePoint.y ) )
							{
								_plane2wcs         = plane2wcs;
								_scenePlaneControl = planeControl;

								updateViews();

								startCapture( event );
								result = null;
								break;
							}
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public EventObject mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			final ContentNode       node              = _node;
			final Matrix3D          plane2wcs         = _plane2wcs;
			final PlaneControl      planeControl      = _planeControl;
			final SubPlaneControl   subPlaneControl   = _subPlaneControl;
			final ScenePlaneControl scenePlaneControl = _scenePlaneControl;

			if ( plane2wcs != null )
			{
				if ( planeControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , planeControl.isPlaneTwoSided() , event.getPointerRay() );
					/**
					 * The wcsPoint can be null, this is done to accomplish elevation.
					 * Be sure to handle it in the planeControl's mouseDragged method.
					 */
					planeControl.mouseDragged( event , node , wcsPoint );
					updateViews();
				}

				if ( subPlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						subPlaneControl.mouseDragged( event , node , planePoint.x , planePoint.y );
						updateViews();
					}
				}

				if ( scenePlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , scenePlaneControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						scenePlaneControl.mouseDragged( event , planePoint.x , planePoint.y );
						updateViews();
					}
				}
			}
		}

		return super.mouseDragged( event );
	}

	@Override
	public EventObject mouseReleased( final ControlInputEvent event )
	{
		if ( isCaptured() && !event.isMouseButtonDown() )
		{
			final ContentNode       node              = _node;
			final Matrix3D          plane2wcs         = _plane2wcs;
			final PlaneControl      planeControl      = _planeControl;
			final SubPlaneControl   subPlaneControl   = _subPlaneControl;
			final ScenePlaneControl scenePlaneControl = _scenePlaneControl;

			if ( plane2wcs != null )
			{
				if ( planeControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , planeControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						planeControl.mouseReleased( event , node , wcsPoint );
						updateViews();
					}
				}

				if ( subPlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , subPlaneControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						subPlaneControl.mouseReleased( event , node , planePoint.x , planePoint.y );
						updateViews();
					}
				}

				if ( scenePlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs , scenePlaneControl.isPlaneTwoSided() , event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						scenePlaneControl.mouseReleased( event , planePoint.x , planePoint.y );
						updateViews();
					}
				}
			}
		}

		_node              = null;
		_plane2wcs         = null;
		_planeControl      = null;
		_subPlaneControl   = null;
		_scenePlaneControl = null;

		return super.mouseReleased( event );
	}

	@Override
	public EventObject mouseMoved( final ControlInputEvent event )
	{
		final ViewControlInput viewControlInput = event.getSource();
		final View3D view = viewControlInput.getView();
		final Scene scene = view.getScene();

		boolean update = false;

		for ( final ScenePlaneControl planeControl : scene.getPlaneControls() )
		{
			if ( planeControl.isEnabled() )
			{
				final Matrix3D plane2wcs = planeControl.getPlane2Wcs();

				final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, planeControl.isPlaneTwoSided(), event.getPointerRay() );
				if ( wcsPoint != null )
				{
					final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );

					final Rectangle2D bounds = planeControl.getPlaneBounds();
					if ( bounds.contains( planePoint.x, planePoint.y ) )
					{
						planeControl.mouseMoved( event, planePoint.x, planePoint.y );
						update = true;
					}
				}
			}
		}

		if ( update )
		{
			view.update();
		}

		return event;
	}

	@Override
	public void addView( final View3D view )
	{
		_views.add( view );
	}

	@Override
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

	@Override
	public void paintOverlay( final View3D view , final Graphics2D g2d )
	{
		final Matrix3D          plane2wcs         = _plane2wcs;
		final PlaneControl      planeControl      = _planeControl;
		final SubPlaneControl   subPlaneControl   = _subPlaneControl;

		if ( ( planeControl != null ) && ( planeControl.isEnabled() ) )
		{
			final Matrix3D  wcs2view   = view.getScene2View();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
			planeControl.paintOverlay( view , g2d );
			planarGraphics2D.dispose();
		}

		if ( ( subPlaneControl != null ) && ( subPlaneControl.isEnabled() ) )
		{
			final Matrix3D  wcs2view   = view.getScene2View();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
			subPlaneControl.paint( view , planarGraphics2D );
			planarGraphics2D.dispose();
		}

		final Scene scene = view.getScene();
		for ( final ScenePlaneControl control : scene.getPlaneControls() )
		{
			if ( control.isVisible( view ) )
			{
				final Matrix3D controlPlane2wcs = control.getPlane2Wcs();
				final Matrix3D wcs2view = view.getScene2View();
				final Matrix3D plane2view = controlPlane2wcs.multiply( wcs2view );
				final Projector projector = view.getProjector();

				final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d , plane2view , projector );
				control.paint( view , planarGraphics2D );
				planarGraphics2D.dispose();
			}
		}
	}
}
