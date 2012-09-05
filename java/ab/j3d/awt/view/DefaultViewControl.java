/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.awt.view;

import java.awt.*;
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

/**
 * This class implements the default control and overlay painter for
 * {@link ContentNode}s. It is automatically attached to all
 * {@link View3D}s by {@link RenderEngine}.
 *
 * TODO: This class is starting to do too many different things (or even the same things in different ways). Should be split.
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
	private final List<View3D> _views = new ArrayList<View3D>();

	/**
	 * Currently node being controlled.
	 */
	private ContentNode _node = null;

	/**
	 * Drag plane in WCS for active control.
	 */
	private Matrix3D _plane2wcs = null;

	/**
	 * Currently active node control.
	 */
	private ContentNodeControl _control = null;

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
	 * Point where the mouse was last clicked.
	 */
	private Ray3D _previousRay = null;

	/**
	 * Content nodes associated with the last click.
	 */
	private List<ContentNode> _previousContentNodes = null;

	/**
	 * Returns content nodes with a {@link ContentNodeControl} in the appropriate order
	 * for the given event.
	 *
	 * <p>
	 * This implementation sorts based on {@link ContentNodeControl#getDepth(Ray3D)}
	 * only. To apply additional (or different) sorting, override this method.
	 *
	 * @param   event   Control input event.
	 *
	 * @return  Sorted list of content nodes with a <code>NodeControl</code>.
	 */
	protected List<ContentNode> getControlledContentNodes( final ControlInputEvent event )
	{
		final ViewControlInput source = event.getSource();
		final View3D view = source.getView();
		final Scene scene = view.getScene();
		final Ray3D pointerRay = event.getPointerRay();

		final List<ContentNode> contentNodes = scene.getContentNodes();
		final Map<ContentNodeControl,Double> controlDepths = new HashMap<ContentNodeControl, Double>();

		for ( Iterator<ContentNode> it = contentNodes.iterator(); it.hasNext(); )
		{
			final ContentNode contentNode = it.next();

			final ContentNodeControl control = contentNode.getControl();
			if ( control == null )
			{
				it.remove();
			}
			else
			{
				final Double depth = control.getDepth( pointerRay );
				if ( depth == null )
				{
					it.remove();
				}
				else
				{
					controlDepths.put( control, depth );
				}
			}
		}

		Collections.sort( contentNodes, new Comparator<ContentNode>()
		{
			public int compare( final ContentNode o1, final ContentNode o2 )
			{
				int result = 0;

				final ContentNodeControl control1 = o1.getControl();
				final ContentNodeControl control2 = o2.getControl();

				if ( ( control1 != null ) && ( control2 != null ) ) /* always true */
				{
					result = control2.getPrecedence() - control1.getPrecedence();
					if ( result == 0 )
					{
						final Double depth1 = controlDepths.get( control1 );
						final Double depth2 = controlDepths.get( control2 );
						if ( ( depth1 != null ) && ( depth2 != null ) ) /* always true */
						{
							result = depth1.compareTo( depth2 );
						}
					}
				}

				return result;
			}
		} );

		return contentNodes;
	}

	@Override
	public void mouseClicked( final ControlInputEvent event )
	{
		if ( event.isMouseButton3Down() )
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
						final JPopupMenu popup = new ContentNodeContextMenu( null, event, contentNode, intersection );

						for ( final Action action : actions )
						{
							popup.add( action );
						}

						popup.show( event.getSourceComponent(), event.getX(), event.getY() );
						break;
					}
				}
			}
		}
	}

	@Override
	public void mousePressed( final ControlInputEvent event )
	{
		final ViewControlInput viewControlInput = event.getSource();
		final View3D view = viewControlInput.getView();
		final Scene scene = view.getScene();

		final List<ContentNode> controlledContentNodes;
		if ( _previousRay != null && _previousRay.equals( event.getPointerRay() ) )
		{
			controlledContentNodes = _previousContentNodes;
			if ( controlledContentNodes.size() > 1 )
			{
				controlledContentNodes.add( controlledContentNodes.remove( 0 ) );
			}
		}
		else
		{
			controlledContentNodes = getControlledContentNodes( event );
			_previousRay = event.getPointerRay();
			_previousContentNodes = controlledContentNodes;
		}

		for ( final ContentNode contentNode : controlledContentNodes )
		{
			final ContentNodeControl control = contentNode.getControl();
			if ( ( control != null ) && control.mousePressed( event, contentNode ) )
			{
				_node = contentNode;
				_control = control;

				// Fix order of '_previousContentNodes' (this node's control must come first).
				for ( int i = 0; i < controlledContentNodes.indexOf( contentNode ); i++ )
				{
					controlledContentNodes.add( controlledContentNodes.remove( 0 ) );
				}

				updateViews();
				startCaptureOnDrag( event );
				break;
			}
		}

		final Ray3D pointerRay = event.getPointerRay();

		if ( !isCaptured() )
		{
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

						final Vector3D pointerDirection = pointerRay.getDirection();

						if ( !MathTools.almostEqual( Vector3D.dot( pointerDirection.x, pointerDirection.y, pointerDirection.z, plane2wcs.xz, plane2wcs.yz, plane2wcs.zz ), 0.0 ) &&
							 planeControl.mousePressed( event, node, wcsPoint ) )
						{
							_node         = node;
							_plane2wcs    = plane2wcs;
							_planeControl = planeControl;

							updateViews();
							startCapture( event );
							break;
						}
					}

					for ( final SubPlaneControl subPlaneControl : node.getSubPlaneControls() )
					{
						if ( subPlaneControl.isEnabled() )
						{
							final Matrix3D plane2node = subPlaneControl.getPlane2Node();
							final Matrix3D node2wcs   = node.getTransform();
							final Matrix3D plane2wcs  = plane2node.multiply( node2wcs );

							final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, subPlaneControl.isPlaneTwoSided(), pointerRay );
							if ( wcsPoint != null )
							{
								final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );

								if ( ( planePoint.x >= 0.0 ) && ( planePoint.y >= 0.0 ) && ( planePoint.x <= subPlaneControl.getPlaneWidth() ) && ( planePoint.y <= subPlaneControl.getPlaneHeight() ) )
								{
									if ( subPlaneControl.mousePressed( event, node, planePoint.x, planePoint.y ) )
									{
										_node            = node;
										_plane2wcs       = plane2wcs;
										_subPlaneControl = subPlaneControl;

										updateViews();

										startCapture( event );
										break;
									}
								}
							}
						}
					}
				}

				if ( isCaptured() )
				{
					break;
				}
			}
		}

		if ( !isCaptured() )
		{
			for ( final ScenePlaneControl planeControl : scene.getPlaneControls() )
			{
				if ( planeControl.isEnabled() )
				{
					final Matrix3D plane2wcs  = planeControl.getPlane2Wcs();

					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, planeControl.isPlaneTwoSided(), pointerRay );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );

						final Rectangle2D bounds = planeControl.getPlaneBounds();
						if ( bounds.contains( planePoint.x, planePoint.y ) )
						{
							if ( planeControl.mousePressed( event, planePoint.x, planePoint.y ) )
							{
								_plane2wcs         = plane2wcs;
								_scenePlaneControl = planeControl;

								updateViews();

								startCapture( event );
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void mouseDragged( final ControlInputEvent event )
	{
		final ContentNode       node              = _node;
		final ContentNodeControl control           = _control;
		final PlaneControl      planeControl      = _planeControl;
		final SubPlaneControl   subPlaneControl   = _subPlaneControl;
		final ScenePlaneControl scenePlaneControl = _scenePlaneControl;

		if ( control != null )
		{
			if ( control.mouseDragged( event, node ) )
			{
				if ( !isCaptured() )
				{
					startCapture( event );
					updateViews();
				}
			}
		}

		if ( isCaptured() )
		{
			final Matrix3D plane2wcs = _plane2wcs;
			if ( plane2wcs != null )
			{
				if ( planeControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, planeControl.isPlaneTwoSided(), event.getPointerRay() );
					/**
					 * The wcsPoint can be null, this is done to accomplish elevation.
					 * Be sure to handle it in the planeControl's mouseDragged method.
					 */
					planeControl.mouseDragged( event, node, wcsPoint );
					updateViews();
				}

				if ( subPlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, subPlaneControl.isPlaneTwoSided(), event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						subPlaneControl.mouseDragged( event, node, planePoint.x, planePoint.y );
						updateViews();
					}
				}

				if ( scenePlaneControl != null )
				{
					final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, scenePlaneControl.isPlaneTwoSided(), event.getPointerRay() );
					if ( wcsPoint != null )
					{
						final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
						scenePlaneControl.mouseDragged( event, planePoint.x, planePoint.y );
						updateViews();
					}
				}
			}
		}
	}

	@Override
	public void mouseReleased( final ControlInputEvent event )
	{
		if ( !event.isMouseButtonDown() )
		{
			final ContentNode       node              = _node;
			final ContentNodeControl control           = _control;
			final PlaneControl      planeControl      = _planeControl;
			final SubPlaneControl   subPlaneControl   = _subPlaneControl;
			final ScenePlaneControl scenePlaneControl = _scenePlaneControl;

			if ( control != null )
			{
				control.mouseReleased( event, node );
				updateViews();
			}

			if ( isCaptured() )
			{
				final Matrix3D plane2wcs = _plane2wcs;
				if ( plane2wcs != null )
				{
					if ( planeControl != null )
					{
						final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, planeControl.isPlaneTwoSided(), event.getPointerRay() );
						if ( wcsPoint != null )
						{
							planeControl.mouseReleased( event, node, wcsPoint );
							updateViews();
						}
					}

					if ( subPlaneControl != null )
					{
						final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, subPlaneControl.isPlaneTwoSided(), event.getPointerRay() );
						if ( wcsPoint != null )
						{
							final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
							subPlaneControl.mouseReleased( event, node, planePoint.x, planePoint.y );
							updateViews();
						}
					}

					if ( scenePlaneControl != null )
					{
						final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, scenePlaneControl.isPlaneTwoSided(), event.getPointerRay() );
						if ( wcsPoint != null )
						{
							final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
							scenePlaneControl.mouseReleased( event, planePoint.x, planePoint.y );
							updateViews();
						}
					}
				}
			}
		}

		_node              = null;
		_plane2wcs         = null;
		_control           = null;
		_planeControl      = null;
		_subPlaneControl   = null;
		_scenePlaneControl = null;
	}

	@Override
	public void mouseMoved( final ControlInputEvent event )
	{
		final ViewControlInput viewControlInput = event.getSource();
		final View3D view = viewControlInput.getView();
		final Scene scene = view.getScene();

		boolean update = false;

		final List<ContentNode> controlledContentNodes = getControlledContentNodes( event );
		for ( final ContentNode contentNode : controlledContentNodes )
		{
			final ContentNodeControl control = contentNode.getControl();
			if ( control != null )
			{
				if ( control.getDepth( event.getPointerRay() ) != null )
				{
					control.mouseMoved( event, contentNode );
					break;
				}
			}
		}

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

	public void paintOverlay( final View3D view, final Graphics2D g2d )
	{
		final Scene scene = view.getScene();
		for ( final ContentNode node : scene.getContentNodes() )
		{
			final ContentNodeControl control = node.getControl();
			if ( ( control instanceof ViewOverlay ) )
			{
				final ViewOverlay overlay = (ViewOverlay)control;
				overlay.paintOverlay( view, g2d );
			}
		}

		final Matrix3D          plane2wcs         = _plane2wcs;
		final PlaneControl      planeControl      = _planeControl;
		final SubPlaneControl   subPlaneControl   = _subPlaneControl;

		if ( ( planeControl != null ) && ( planeControl.isEnabled() ) )
		{
			final Matrix3D  wcs2view   = view.getScene2View();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d, plane2view, projector );
			planeControl.paintOverlay( view, g2d );
			planarGraphics2D.dispose();
		}

		if ( ( subPlaneControl != null ) && ( subPlaneControl.isEnabled() ) )
		{
			final Matrix3D  wcs2view   = view.getScene2View();
			final Matrix3D  plane2view = plane2wcs.multiply( wcs2view );
			final Projector projector  = view.getProjector();

			final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d, plane2view, projector );
			subPlaneControl.paint( view, planarGraphics2D );
			planarGraphics2D.dispose();
		}

		for ( final ScenePlaneControl scenePlaneControl : scene.getPlaneControls() )
		{
			if ( scenePlaneControl.isVisible( view ) )
			{
				final Matrix3D controlPlane2wcs = scenePlaneControl.getPlane2Wcs();
				final Matrix3D wcs2view = view.getScene2View();
				final Matrix3D plane2view = controlPlane2wcs.multiply( wcs2view );
				final Projector projector = view.getProjector();

				final PlanarGraphics2D planarGraphics2D = new PlanarGraphics2D( g2d, plane2view, projector );
				scenePlaneControl.paint( view, planarGraphics2D );
				planarGraphics2D.dispose();
			}
		}
	}
}
