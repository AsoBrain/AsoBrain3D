/*
 * $Id$
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
package ab.j3d.control;

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.planar.*;
import org.jetbrains.annotations.*;

/**
 * Control for a side of a box.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public abstract class BoxSide
	implements ContentNodeControl, ViewOverlay
{
	/**
	 * Default color for behavior hints.
	 */
	private static final Color HINT_COLOR = Color.GREEN.darker();

	/**
	 * Color for behavior hints.
	 */
	private Color _hintColor = HINT_COLOR;

	/**
	 * Specifies a side of the box.
	 */
	public enum Side
	{
		/** Left side (X-).   */ LEFT,
		/** Front side (Y-).  */ FRONT,
		/** Bottom side (Z-). */ BOTTOM,
		/** Right side (X+).  */ RIGHT,
		/** Rear side (Y+).   */ REAR,
		/** Top side (Z+).    */ TOP
	}

	/**
	 * Side of the box.
	 */
	private final Side _side;

	/**
	 * Defines the behavior of the side or one of its edges.
	 */
	public enum Behavior
	{
		/**
		 * The object is controlled parallel to the side of the box.
		 */
		PLANE,

		/**
		 * The object is controlled perpendicular to the side of the box.
		 */
		NORMAL
	}

	/**
	 * Behavior of the side, excluding the edges.
	 */
	@NotNull
	private Behavior _behavior = Behavior.PLANE;

	/**
	 * Indicates what kind of behavior is associated with a side or edge from
	 * the user's point of view.
	 */
	public enum BehaviorHint
	{
		/** No hint is shown. */ NONE,
		/** Hint for movement over the XY-plane. */ MOVE_PLANE,
		/** Hint for movement along the X-axis. */ MOVE_HORIZONTAL,
		/** Hint for movement along the Y-axis. */ MOVE_VERTICAL,
		/** Hint for movement along the plane normal. */ MOVE_NORMAL
	}

	/**
	 * Hints what the behavior of this side is.
	 */
	private BehaviorHint _behaviorHint = BehaviorHint.NONE;

	/**
	 * Behavior of the edges (and corners) of the side.
	 */
	@NotNull
	private Behavior _edgeBehavior = Behavior.PLANE;

	/**
	 * Listener for this side of the box.
	 */
	@Nullable
	private BoxControlDelegate _listener = null;

	/**
	 * Edge on the X- side (in local plane coordinates).
	 */
	@NotNull
	private BoxEdge _edgeX1 = new BoxEdge();

	/**
	 * Edge on the X+ side (in local plane coordinates).
	 */
	@NotNull
	private BoxEdge _edgeX2 = new BoxEdge();

	/**
	 * Edge on the Y- side (in local plane coordinates).
	 */
	@NotNull
	private BoxEdge _edgeY1 = new BoxEdge();

	/**
	 * Edge on the Y+ side (in local plane coordinates).
	 */
	@NotNull
	private BoxEdge _edgeY2 = new BoxEdge();

	/**
	 * Whether the mouse is currently positioned inside the box side.
	 */
	private boolean _hover = false;

	/**
	 * Edge on the X- or X+ side where the mouse is currently located, if any.
	 */
	private BoxEdge _hoverEdgeX;

	/**
	 * Edge on the Y- or Y+ side where the mouse is currently located, if any.
	 */
	private BoxEdge _hoverEdgeY;

	/**
	 * Point in scene coordinates where the current drag operation started.
	 */
	@Nullable
	private Vector3D _dragStart = null;

	/**
	 * Active transformation matrix from plane to scene coordinates.
	 */
	@Nullable
	private Matrix3D _activePlane = null;

	/**
	 * Active edge on the X- or X+ side.
	 */
	@Nullable
	private BoxEdge _activeEdgeX = null;

	/**
	 * Active edge on the Y- or Y+ side.
	 */
	@Nullable
	private BoxEdge _activeEdgeY = null;

	/**
	 * Color for this side of the box.
	 */
	@Nullable
	private Color _color = null;

	/**
	 * Color for corners of the box, used where edges overlap.
	 */
	@Nullable
	private Color _cornerColor = null;

	/**
	 * <code>true</code> for a box controlled from the inside.
	 */
	private boolean _insideOfBox;

	/**
	 * Constructs a new instance.
	 *
	 * @param   side    Normal on this side of the box.
	 */
	protected BoxSide( @NotNull final Side side )
	{
		this( side, false );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   side            Normal on this side of the box.
	 * @param   insideOfBox     <code>true</code> for the inside of the box.
	 */
	protected BoxSide( @NotNull final Side side, final boolean insideOfBox )
	{
		_side = side;
		_insideOfBox = insideOfBox;
	}

	/**
	 * Returns the listener associated with this side.
	 *
	 * @return  Listener.
	 */
	@Nullable
	public BoxControlDelegate getListener()
	{
		return _listener;
	}

	/**
	 * Sets the listener associated with this side.
	 *
	 * @param   listener    Listener to be set.
	 */
	public void setListener( @Nullable final BoxControlDelegate listener )
	{
		_listener = listener;
	}

	/**
	 * Returns the dragging behavior on this side.
	 *
	 * @return  Dragging behavior.
	 */
	@NotNull
	public Behavior getBehavior()
	{
		return _behavior;
	}

	/**
	 * Sets the dragging behavior on this side.
	 *
	 * @param   behavior    Dragging behavior to be set.
	 */
	public void setBehavior( @NotNull final Behavior behavior )
	{
		_behavior = behavior;
	}

	/**
	 * Returns the behavior hint for this side.
	 *
	 * @return  Behavior hint.
	 */
	public BehaviorHint getBehaviorHint()
	{
		return _behaviorHint;
	}

	/**
	 * Sets the behavior hint for this side.
	 *
	 * @param   behaviorHint    Behavior hint.
	 */
	public void setBehaviorHint( final BehaviorHint behaviorHint )
	{
		_behaviorHint = behaviorHint;
	}

	/**
	 * Sets the listener for the left (X-) edge of this side.
	 *
	 * @param   edgeListenerX1  Listener to be set.
	 */
	public void setEdgeListenerX1( final BoxControlDelegate edgeListenerX1 )
	{
		_edgeX1.setListener( edgeListenerX1 );
	}

	/**
	 * Sets the listener for the right (X+) edge of this side.
	 *
	 * @param   edgeListenerX2  Listener to be set.
	 */
	public void setEdgeListenerX2( final BoxControlDelegate edgeListenerX2 )
	{
		_edgeX2.setListener( edgeListenerX2 );
	}

	/**
	 * Sets the listener for the bottom (Y-) edge of this side.
	 *
	 * @param   edgeListenerY1  Listener to be set.
	 */
	public void setEdgeListenerY1( final BoxControlDelegate edgeListenerY1 )
	{
		_edgeY1.setListener( edgeListenerY1 );
	}

	/**
	 * Sets the listener for the top (Y+) edge of this side.
	 *
	 * @param   edgeListenerY2  Listener to be set.
	 */
	public void setEdgeListenerY2( final BoxControlDelegate edgeListenerY2 )
	{
		_edgeY2.setListener( edgeListenerY2 );
	}

	/**
	 * Returns the dragging behavior of edges on this side.
	 *
	 * @return  Dragging behavior.
	 */
	@NotNull
	public Behavior getEdgeBehavior()
	{
		return _edgeBehavior;
	}

	/**
	 * Sets the dragging behavior of edges on this side.
	 *
	 * @param   edgeBehavior    Dragging behavior to be set.
	 */
	public void setEdgeBehavior( @NotNull final Behavior edgeBehavior )
	{
		_edgeBehavior = edgeBehavior;
	}

	/**
	 * Sets the width of all edges on this side.
	 *
	 * @param   width   Width to be set.
	 */
	public void setEdgeWidth( final double width )
	{
		_edgeX1.setWidth( width );
		_edgeY1.setWidth( width );
		_edgeX2.setWidth( width );
		_edgeY2.setWidth( width );
	}

	/**
	 * Returns the edge on the X- side (in local plane coordinates).
	 *
	 * @return  Edge on the X- side.
	 */
	@NotNull
	public BoxEdge getEdgeX1()
	{
		return _edgeX1;
	}

	/**
	 * Returns the edge on the X+ side (in local plane coordinates).
	 *
	 * @return  Edge on the X+ side.
	 */
	@NotNull
	public BoxEdge getEdgeX2()
	{
		return _edgeX2;
	}

	/**
	 * Returns the edge on the Y- side (in local plane coordinates).
	 *
	 * @return  Edge on the Y- side.
	 */
	@NotNull
	public BoxEdge getEdgeY1()
	{
		return _edgeY1;
	}

	/**
	 * Returns the edge on the Y+ side (in local plane coordinates).
	 *
	 * @return  Edge on the Y+ side.
	 */
	@NotNull
	public BoxEdge getEdgeY2()
	{
		return _edgeY2;
	}

	/**
	 * Returns the color of this side.
	 *
	 * @return  Color of this side;
	 *          if <code>null</code>, the side is not painted.
	 */
	@Nullable
	public Color getColor()
	{
		return _color;
	}

	/**
	 * Sets the color of this side.
	 *
	 * @param   color   Color of this side;
	 *                  if <code>null</code>, the side is not painted.
	 */
	public void setColor( @Nullable final Color color )
	{
		_color = color;
	}

	/**
	 * Sets the color of all edges at once.
	 *
	 * @param   color   Color to be set.
	 */
	public void setEdgeColor( @Nullable final Color color )
	{
		_edgeX1.setColor( color );
		_edgeY1.setColor( color );
		_edgeX2.setColor( color );
		_edgeY2.setColor( color );
	}

	/**
	 * Returns the color of corners on this side. Corners are only painted where
	 * edges overlap.
	 *
	 * @return  Corner color;
	 *          <code>null</code> if corners are not painted.
	 */
	@Nullable
	public Color getCornerColor()
	{
		return _cornerColor;
	}

	/**
	 * Sets the color of corners on this side. Corners are only painted where
	 * edges overlap.
	 *
	 * @param   cornerColor     Corner color;
	 *                          <code>null</code> if corners are not painted.
	 */
	public void setCornerColor( @Nullable final Color cornerColor )
	{
		_cornerColor = cornerColor;
	}

	/**
	 * Returns the transformation from plane to scene/world coordinates.
	 *
	 * @return  Plane-to-scene transformation.
	 */
	protected Matrix3D getPlane2Wcs()
	{
		Matrix3D result = Matrix3D.IDENTITY;

		final Matrix3D transform = getTransform();
		final Bounds3D bounds = getBounds();
		if ( bounds != null )
		{
			final Matrix3D plane = getPlaneToObject();
			result = plane.multiply( transform );
		}

		return result;
	}

	/**
	 * Returns the transformation from controlled object to world coordinates.
	 *
	 * @return  Content node to world transform.
	 */
	protected abstract Matrix3D getTransform();

	/**
	 * Returns the bounds of the controlled object.
	 *
	 * @return  Object bounds, in local coordinates.
	 */
	protected abstract Bounds3D getBounds();

	/**
	 * Returns the transformation from plane to object coordinates.
	 *
	 * @return  Plane-to-scene transformation.
	 */
	private Matrix3D getPlaneToObject()
	{
		Matrix3D result = Matrix3D.IDENTITY;

		final Bounds3D bounds = getBounds();
		if ( bounds != null )
		{
			final Vector3D min = bounds.min();
			final Vector3D max = bounds.max();

			if ( _insideOfBox )
			{
				switch ( _side )
				{
					default:
					case LEFT:
						result = new Matrix3D( 0.0, 0.0, 1.0, min.x,
						                       1.0, 0.0, 0.0, min.y,
						                       0.0, 1.0, 0.0, min.z );
						break;
					case FRONT:
						result = new Matrix3D( -1.0, 0.0, 0.0, max.x,
						                        0.0, 0.0, 1.0, min.y,
						                        0.0, 1.0, 0.0, min.z );
						break;
					case BOTTOM:
						result = new Matrix3D( 1.0, 0.0, 0.0, min.x,
						                       0.0, 1.0, 0.0, min.y,
						                       0.0, 0.0, 1.0, min.z );
						break;
					case RIGHT:
						result = new Matrix3D(  0.0, 0.0, -1.0, max.x,
						                       -1.0, 0.0,  0.0, max.y,
						                        0.0, 1.0,  0.0, min.z );
						break;
					case REAR:
						result = new Matrix3D( 1.0, 0.0,  0.0, min.x,
						                       0.0, 0.0, -1.0, max.y,
						                       0.0, 1.0,  0.0, min.z );
						break;
					case TOP:
						result = new Matrix3D( 1.0,  0.0,  0.0, min.x,
						                       0.0, -1.0,  0.0, max.y,
						                       0.0,  0.0, -1.0, max.z );
						break;
				}
			}
			else
			{
				switch ( _side )
				{
					default:
					case LEFT:
						result = new Matrix3D(  0.0, 0.0, -1.0, min.x,
						                       -1.0, 0.0,  0.0, max.y,
						                        0.0, 1.0,  0.0, min.z );
						break;
					case FRONT:
						result = new Matrix3D( 1.0, 0.0,  0.0, min.x,
						                       0.0, 0.0, -1.0, min.y,
						                       0.0, 1.0,  0.0, min.z );
						break;
					case BOTTOM:
						result = new Matrix3D( 1.0,  0.0,  0.0, min.x,
						                       0.0, -1.0,  0.0, max.y,
						                       0.0,  0.0, -1.0, min.z );
						break;
					case RIGHT:
						result = new Matrix3D( 0.0, 0.0, 1.0, max.x,
						                       1.0, 0.0, 0.0, min.y,
						                       0.0, 1.0, 0.0, min.z );
						break;
					case REAR:
						result = new Matrix3D( -1.0, 0.0, 0.0, max.x,
						                        0.0, 0.0, 1.0, max.y,
						                        0.0, 1.0, 0.0, min.z );
						break;
					case TOP:
						result = new Matrix3D( 1.0, 0.0, 0.0, min.x,
						                       0.0, 1.0, 0.0, min.y,
						                       0.0, 0.0, 1.0, max.z );
						break;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the 2D bounds of this side, on the plane defined by
	 * {@link #getPlaneToObject()}.
	 *
	 * @return  Bounds of the side on its plane.
	 */
	private Rectangle2D getPlaneBounds()
	{
		Rectangle2D result = new Rectangle();

		final Bounds3D bounds = getBounds();
		if ( bounds != null )
		{
			final Matrix3D plane = getPlaneToObject();
			final double dx = Math.abs( plane.inverseTransformX( bounds.v1 ) - plane.inverseTransformX( bounds.v2 ) );
			final double dy = Math.abs( plane.inverseTransformY( bounds.v1 ) - plane.inverseTransformY( bounds.v2 ) );
			result = new Rectangle2D.Double( 0.0, 0.0, dx, dy );
		}

		return result;
	}

	/**
	 * Returns whether this side is two-sided.
	 *
	 * @return  <code>true</code> if two-sided.
	 */
	public boolean isPlaneTwoSided()
	{
		return false;
	}

	/**
	 * Returns whether the side is enabled, i.e. capable of receiving events.
	 *
	 * @return  <code>true</code> if the side is enabled.
	 */
	public boolean isEnabled()
	{
		return ( ( _listener != null ) && _listener.isEnabled() ) || _edgeX1.isEnabled() || _edgeX2.isEnabled() || _edgeY1.isEnabled() || _edgeY2.isEnabled();
	}

	/**
	 * Returns whether the side is visible.
	 *
	 * @return  <code>true</code> if the side is visible.
	 */
	public boolean isVisible()
	{
		return ( ( _listener != null ) && _listener.isVisible() ) || _edgeX1.isVisible() || _edgeX2.isVisible() || _edgeY1.isVisible() || _edgeY2.isVisible();
	}

	public Double getDepth( final Ray3D pointerRay )
	{
		Double result = null;

		final Matrix3D plane2wcs = getPlane2Wcs();
		final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, isPlaneTwoSided(), pointerRay );

		if ( wcsPoint != null )
		{
			final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
//			if ( isEnabledAtPoint( planePoint ) )
			final Rectangle2D planeBounds = getPlaneBounds();
			if ( planeBounds.contains( planePoint.x, planePoint.y ) )
			{
				result = Double.valueOf( Vector3D.dot( pointerRay.getDirection(), wcsPoint.minus( pointerRay.getOrigin() ) ) );
			}
		}

		return result;
	}

	/**
	 * Returns whether the side is enabled at the specified point.
	 *
	 * @param   point   A point, in plane coordinates.
	 *
	 * @return  <code>true</code> if the side is enabled at the given point.
	 */
	private boolean isEnabledAtPoint( final Vector3D point )
	{
		boolean result = isEnabled();

		if ( result )
		{
			final Rectangle2D bounds = getPlaneBounds();
			result = bounds.contains( point.x, point.y );

			if ( result && ( ( _listener == null ) || !_listener.isEnabled() ) )
			{
				final BoxEdge edgeX = getEdgeX( bounds, point.x );
				final BoxEdge edgeY = getEdgeY( bounds, point.y );

				result = ( ( edgeX != null ) && edgeX.isEnabled() ) ||
			             ( ( edgeY != null ) && edgeY.isEnabled() );
			}
		}

		return result;
	}

	/**
	 * Returns the vertical edge (X- or X+) that contains the point indicated
	 * by the given X-coordinate, if any.
	 *
	 * @param   bounds  Bounds of the side.
	 * @param   x       X-coordinate.
	 *
	 * @return  Edge that contains the specified point.
	 */
	private BoxEdge getEdgeX( final Rectangle2D bounds, final double x )
	{
		final BoxEdge edgeX1 = _edgeX1;
		final BoxEdge edgeX2 = _edgeX2;

		BoxEdge edgeX = null;
		if ( edgeX1.isEnabled() && ( x > bounds.getMinX() ) && ( x < bounds.getMinX() + edgeX1.getWidth() ) )
		{
			edgeX = edgeX1;
		}
		else if ( edgeX2.isEnabled() && ( x > bounds.getMaxX() - edgeX2.getWidth() ) && ( x < bounds.getMaxX() ) )
		{
			edgeX = edgeX2;
		}

		return edgeX;
	}

	/**
	 * Returns the horizontal edge (Y- or Y+) that contains the point indicated
	 * by the given Y-coordinate, if any.
	 *
	 * @param   bounds  Bounds of the side.
	 * @param   y       Y-coordinate.
	 *
	 * @return  Edge that contains the specified point.
	 */
	private BoxEdge getEdgeY( final Rectangle2D bounds, final double y )
	{
		final BoxEdge edgeY1 = _edgeY1;
		final BoxEdge edgeY2 = _edgeY2;

		BoxEdge edgeY = null;
		if ( edgeY1.isEnabled() && ( y > bounds.getMinY() ) && ( y < bounds.getMinY() + edgeY1.getWidth() ) )
		{
			edgeY = edgeY1;
		}
		else if ( edgeY2.isEnabled() && ( y > bounds.getMaxY() - edgeY2.getWidth() ) && ( y < bounds.getMaxY() ) )
		{
			edgeY = edgeY2;
		}

		return edgeY;
	}

	public void mouseMoved( final ControlInputEvent event, final ContentNode contentNode )
	{
		final Ray3D pointerRay = event.getPointerRay();

		final Matrix3D plane2wcs = getPlane2Wcs();
		final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, isPlaneTwoSided(), pointerRay );

		boolean hover = false;
		BoxEdge hoverEdgeX = null;
		BoxEdge hoverEdgeY = null;

		if ( wcsPoint != null )
		{
			final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
			final Rectangle2D planeBounds = getPlaneBounds();

			if ( planeBounds.contains( planePoint.x, planePoint.y ) )
			{
				hover = true;
				hoverEdgeX = getEdgeX( planeBounds, planePoint.x );
				hoverEdgeY = getEdgeY( planeBounds, planePoint.y );
			}
		}

		if ( ( hover != _hover ) || ( hoverEdgeX != _hoverEdgeX ) || ( hoverEdgeY != _hoverEdgeY ) )
		{
			_hover = hover;
			_hoverEdgeX = hoverEdgeX;
			_hoverEdgeY = hoverEdgeY;

			final ViewControlInput source = event.getSource();
			final View3D view = source.getView();
			view.update();
		}
	}

	/**
	 * Returns whether the mouse is currently hovering over this side. The value
	 * of this property is automatically updated for each {@code mouseMoved}
	 * event.
	 *
	 * @return  {@code true} if the mouse hovers over this side.
	 */
	public boolean isHover()
	{
		return _hover;
	}

	public boolean mousePressed( final ControlInputEvent event, final ContentNode contentNode )
	{
		boolean result = false;

		if ( isEnabled() && event.isMouseButton1Down() )
		{
			final Matrix3D plane2wcs = getPlane2Wcs();
			final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, isPlaneTwoSided(), event.getPointerRay() );

			if ( wcsPoint != null )
			{
				final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
				if ( isEnabledAtPoint( planePoint ) )
				{
					result = mousePressed( event, contentNode, wcsPoint, event.getScene2View() );
				}
			}
		}

		return result;
	}

	/**
	 * Handles a mouse press on this side.
	 *
	 * @param   event       Event to be handled.
	 * @param   node        Content node.
	 * @param   wcsPoint    Intersection point on the side in world coordinates.
	 * @param   scene2view  Scene to view transformation.
	 *
	 * @return  <code>true</code> if the event should be captured.
	 */
	private boolean mousePressed( final ControlInputEvent event, final ContentNode node, final Vector3D wcsPoint, final Matrix3D scene2view )
	{
		final Matrix3D plane2wcs = getPlane2Wcs();
		final Vector3D planePoint = plane2wcs.inverseTransform( wcsPoint );
		final Rectangle2D bounds = getPlaneBounds();

		final double x = planePoint.x;
		final double y = planePoint.y;

		final BoxEdge edgeX = getEdgeX( bounds, x );
		final BoxEdge edgeY = getEdgeY( bounds, y );

		final Behavior behavior;
		if ( ( edgeX == null ) && ( edgeY == null ) )
		{
			behavior = getBehavior();
		}
		else
		{
			behavior = getEdgeBehavior();
		}

		final Matrix3D dragPlane;
		switch ( behavior )
		{
			default:
			case PLANE:
				dragPlane = plane2wcs;
				break;

			case NORMAL:
				final Vector3D planeNormal = new Vector3D( plane2wcs.xz, plane2wcs.yz, plane2wcs.zz );
				dragPlane = calculateDragPlaneAlongLine( scene2view, wcsPoint, planeNormal );
				break;
		}

		if ( _listener != null )
		{
			_hover = true;
			_listener.mousePressed( event, node );
		}

		if ( edgeX != null )
		{
			_hoverEdgeX = edgeX;
			final BoxControlDelegate listener = edgeX.getListener();
			listener.mousePressed( event, node );
		}

		if ( edgeY != null )
		{
			_hoverEdgeY = edgeY;
			final BoxControlDelegate listener = edgeY.getListener();
			listener.mousePressed( event, node );
		}

		_activeEdgeX = edgeX;
		_activeEdgeY = edgeY;
		_activePlane = dragPlane;
		_dragStart = wcsPoint;

		return ( _listener != null ) || ( edgeX != null ) || ( edgeY != null );
	}

	public boolean mouseDragged( final ControlInputEvent event, final ContentNode contentNode )
	{
		boolean result = false;

		final Matrix3D plane2wcs = _activePlane;
		if ( plane2wcs != null )
		{
			final Vector3D wcsPoint = GeometryTools.getIntersectionBetweenRayAndPlane( plane2wcs, isPlaneTwoSided(), event.getPointerRay() );

			if ( wcsPoint != null )
			{
				final Vector3D dragStart = _dragStart;
				final BoxEdge edgeX = _activeEdgeX;
				final BoxEdge edgeY = _activeEdgeY;

				if ( ( edgeX == null ) && ( edgeY == null ) )
				{
					final BoxControlDelegate listener = _listener;
					if ( listener != null )
					{
						switch ( _behavior )
						{
							default:
							case PLANE:
							{
								final Vector3D offset = wcsPoint.minus( dragStart );
								final Matrix3D transform = getTransform();
								final Vector3D localOffset = transform.inverseRotate( offset );
								listener.mouseDragged( event, contentNode, localOffset );
								result = true;
								break;
							}

							case NORMAL:
							{
								final double startX = plane2wcs.inverseTransformX( dragStart );
								final double endX = plane2wcs.inverseTransformX( wcsPoint );

								final Vector3D normal;
								switch ( _side )
								{
									default:
									case LEFT: normal = Vector3D.NEGATIVE_X_AXIS; break;
									case FRONT: normal = Vector3D.NEGATIVE_Y_AXIS; break;
									case BOTTOM: normal = Vector3D.NEGATIVE_Z_AXIS; break;
									case RIGHT: normal = Vector3D.POSITIVE_X_AXIS; break;
									case REAR: normal = Vector3D.POSITIVE_Y_AXIS; break;
									case TOP: normal = Vector3D.POSITIVE_Z_AXIS; break;
								}

								listener.mouseDragged( event, contentNode, normal.multiply( endX - startX ) );
								result = true;
								break;
							}
						}
					}
				}
				else
				{
					final Matrix3D transform = contentNode.getTransform();
					final Vector3D offset = wcsPoint.minus( dragStart );
					final Vector3D localOffset = transform.inverseRotate( offset );

					switch ( _edgeBehavior )
					{
						default:
						case PLANE:
						{
							if ( edgeX != null )
							{
								final BoxControlDelegate listener = edgeX.getListener();
								listener.mouseDragged( event, contentNode, localOffset );
								result = true;
							}

							if ( edgeY != null )
							{
								final BoxControlDelegate listener = edgeY.getListener();
								listener.mouseDragged( event, contentNode, localOffset );
								result = true;
							}
							break;
						}

						case NORMAL:
						{
							if ( edgeX != null )
							{
								final BoxControlDelegate listener = edgeX.getListener();
								listener.mouseDragged( event, contentNode, localOffset );
								result = true;
							}

							if ( edgeY != null )
							{
								final BoxControlDelegate listener = edgeY.getListener();
								listener.mouseDragged( event, contentNode, localOffset );
								result = true;
							}
							break;
						}
					}
				}
			}
		}

		return result;
	}

	public void mouseReleased( final ControlInputEvent event, final ContentNode contentNode )
	{
		if ( _activePlane != null )
		{
			final BoxEdge edgeY = _activeEdgeY;
			final BoxEdge edgeX = _activeEdgeX;

			if ( ( edgeX == null ) && ( edgeY == null ) )
			{
				final BoxControlDelegate listener = _listener;
				if ( listener != null )
				{
					listener.mouseReleased( event, contentNode );
				}
			}
			else
			{
				if ( ( edgeX != null ) && edgeX.isEnabled() )
				{
					final BoxControlDelegate listener = edgeX.getListener();
					if ( listener != null )
					{
						listener.mouseReleased( event, contentNode );
					}
				}

				if ( ( edgeY != null ) && edgeY.isEnabled() )
				{
					final BoxControlDelegate listener = edgeY.getListener();
					if ( listener != null )
					{
						listener.mouseReleased( event, contentNode );
					}
				}
			}
		}
	}

	public void mouseEntered( final ControlInputEvent event, final ContentNode contentNode )
	{
	}

	public void mouseExited( final ControlInputEvent event, final ContentNode contentNode )
	{
		if ( _hover || ( _hoverEdgeX != null ) || ( _hoverEdgeY != null ) )
		{
			_hover = false;
			_hoverEdgeX = null;
			_hoverEdgeY = null;

			final ViewControlInput source = event.getSource();
			final View3D view = source.getView();
			view.update();
		}
	}

	public void paintOverlay( final View3D view, final Graphics2D g )
	{
		if ( isVisible() )
		{
			final Rectangle2D bounds = getPlaneBounds();

			final Matrix3D scene2view = view.getScene2View();
			final Matrix3D plane2wcs = getPlane2Wcs();
			final Matrix3D plane2view = plane2wcs.multiply( scene2view );
			final Projector projector = view.getProjector();

			if ( isVisible( plane2view, bounds, projector ) )
			{
				final PlanarGraphics2D planarGraphics = new PlanarGraphics2D( g, plane2view, projector );
				paintPlane( planarGraphics, bounds );
				paintBehaviorHints( view, g, planarGraphics, bounds );
				planarGraphics.dispose();
			}
		}
	}

	/**
	 * Paints colored edges and the color for the entire side (if set).
	 *
	 * @param   planarGraphics  Planar graphics to paint to.
	 * @param   bounds          Bounds of the side.
	 */
	protected void paintPlane( final PlanarGraphics2D planarGraphics, final Rectangle2D bounds )
	{
		if ( _color != null )
		{
			planarGraphics.setColor( _color );
			planarGraphics.fill( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight() ) );
		}

		final BoxEdge edgeX1 = _edgeX1;
		final BoxEdge edgeX2 = _edgeX2;
		final BoxEdge edgeY1 = _edgeY1;
		final BoxEdge edgeY2 = _edgeY2;

		if ( edgeX1.isVisible() && ( edgeX1.getColor() != null ) )
		{
			planarGraphics.setColor( edgeX1.getColor() );
			planarGraphics.fill( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), edgeX1.getWidth(), bounds.getHeight() ) );
		}

		if ( edgeX2.isVisible() && ( edgeX2.getColor() != null ) )
		{
			planarGraphics.setColor( edgeX2.getColor() );
			planarGraphics.fill( new Rectangle2D.Double( bounds.getMaxX() - edgeX2.getWidth(), bounds.getMinY(), edgeX2.getWidth(), bounds.getHeight() ) );
		}

		if ( edgeY1.isVisible() && ( edgeY1.getColor() != null ) )
		{
			planarGraphics.setColor( edgeY1.getColor() );
			planarGraphics.fill( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), edgeY1.getWidth() ) );
		}

		if ( edgeY2.isVisible() && ( edgeY2.getColor() != null ) )
		{
			planarGraphics.setColor( edgeY2.getColor() );
			planarGraphics.fill( new Rectangle2D.Double( bounds.getMinX(), bounds.getMaxY() - edgeY2.getWidth(), bounds.getWidth(), edgeY2.getWidth() ) );
		}

		if ( _cornerColor != null )
		{
			planarGraphics.setColor( _cornerColor );

			if ( edgeX1.isVisible() && edgeY1.isVisible() )
			{
				planarGraphics.draw( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), edgeX1.getWidth(), edgeY1.getWidth() ) );
			}

			if ( edgeX2.isVisible() && edgeY1.isVisible() )
			{
				planarGraphics.draw( new Rectangle2D.Double( bounds.getMaxX() - edgeX2.getWidth(), bounds.getMinY(), edgeX2.getWidth(), edgeY1.getWidth() ) );
			}

			if ( edgeX2.isVisible() && edgeY2.isVisible() )
			{
				planarGraphics.draw( new Rectangle2D.Double( bounds.getMaxX() - edgeX2.getWidth(), bounds.getMaxY() - edgeY2.getWidth(), edgeX2.getWidth(), edgeY2.getWidth() ) );
			}

			if ( edgeX1.isVisible() && edgeY2.isVisible() )
			{
				planarGraphics.draw( new Rectangle2D.Double( bounds.getMinX(), bounds.getMaxY() - edgeY2.getWidth(), edgeX1.getWidth(), edgeY2.getWidth() ) );
			}
		}
	}

	/**
	 * Paints behavior hints.
	 *
	 * @param   view            View being painted.
	 * @param   viewGraphics    Graphics to paint to.
	 * @param   planarGraphics  Planar graphics to paint to.
	 * @param   bounds          Bounds of the side.
	 */
	private void paintBehaviorHints( final View3D view, final Graphics2D viewGraphics, final PlanarGraphics2D planarGraphics, final Rectangle2D bounds )
	{
		planarGraphics.setColor( _hintColor );

		if ( _hover )
		{
			paintBehaviorHint( view, viewGraphics, planarGraphics, _behaviorHint, bounds.getCenterX(), bounds.getCenterY() );
		}

		final BoxEdge hoverEdgeX = _hoverEdgeX;
		if ( hoverEdgeX != null )
		{
			final double width = hoverEdgeX.getWidth();
			final double centerX = ( hoverEdgeX == _edgeX1 ) ? bounds.getMinX() + width / 2.0 : bounds.getMaxX() - width / 2.0;
			final BehaviorHint behaviorHint = ( _edgeBehavior == Behavior.PLANE ) ? BehaviorHint.MOVE_HORIZONTAL : BehaviorHint.MOVE_NORMAL;
			paintBehaviorHint( view, viewGraphics, planarGraphics, behaviorHint, centerX, bounds.getCenterY() );
		}

		final BoxEdge hoverEdgeY = _hoverEdgeY;
		if ( hoverEdgeY != null )
		{
			final double width = hoverEdgeY.getWidth();
			final double centerY = ( hoverEdgeY == _edgeY1 ) ? bounds.getMinY() + width / 2.0 : bounds.getMaxY() - width / 2.0;
			final BehaviorHint behaviorHint = ( _edgeBehavior == Behavior.PLANE ) ? BehaviorHint.MOVE_VERTICAL : BehaviorHint.MOVE_NORMAL;
			paintBehaviorHint( view, viewGraphics, planarGraphics, behaviorHint, bounds.getCenterX(), centerY );
		}
	}

	/**
	 * Paints a single behavior hint.
	 *
	 * @param   view            View being painted.
	 * @param   viewGraphics    Graphics to paint to.
	 * @param   planarGraphics  Planar graphics to paint to.
	 * @param   behaviorHint    Hint to be painted.
	 * @param   centerX         Center along the plane's X-axis.
	 * @param   centerY         Center along the plane's Y-axis.
	 */
	private void paintBehaviorHint( final View3D view, final Graphics2D viewGraphics, final PlanarGraphics2D planarGraphics, final BehaviorHint behaviorHint, final double centerX, final double centerY )
	{
		if ( ( behaviorHint == BehaviorHint.MOVE_PLANE ) ||
		     ( behaviorHint == BehaviorHint.MOVE_HORIZONTAL ) )
		{
			final double size = 40.0;
			final double outside = 50.0;
			final double inside = outside - 0.5 * size;

			{
				final Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo( centerX - inside, centerY - 0.5 * size );
				triangle.lineTo( centerX - outside, centerY );
				triangle.lineTo( centerX - inside, centerY + 0.5 * size );
				triangle.closePath();
				planarGraphics.fill( triangle );
			}
			{
				final Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo( centerX + inside, centerY - 0.5 * size );
				triangle.lineTo( centerX + outside, centerY );
				triangle.lineTo( centerX + inside, centerY + 0.5 * size );
				triangle.closePath();
				planarGraphics.fill( triangle );
			}
		}

		if ( ( behaviorHint == BehaviorHint.MOVE_PLANE ) ||
		     ( behaviorHint == BehaviorHint.MOVE_VERTICAL ) )
		{
			final double size = 40.0;
			final double outside = 50.0;
			final double inside = outside - 0.5 * size;

			{
				final Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo( centerX - 0.5 * size, centerY - inside );
				triangle.lineTo( centerX, centerY - outside );
				triangle.lineTo( centerX + 0.5 * size, centerY - inside );
				triangle.closePath();
				planarGraphics.fill( triangle );
			}
			{
				final Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo( centerX - 0.5 * size, centerY + inside );
				triangle.lineTo( centerX, centerY + outside );
				triangle.lineTo( centerX + 0.5 * size, centerY + inside );
				triangle.closePath();
				planarGraphics.fill( triangle );
			}
		}

		if ( behaviorHint == BehaviorHint.MOVE_NORMAL )
		{
			final Matrix3D scene2view = view.getScene2View();
			final Matrix3D plane2wcs = getPlane2Wcs();
			final Matrix3D plane2view = plane2wcs.multiply( scene2view );
			final Projector projector = view.getProjector();
			final double[] point = new double[ 2 ];

			final double size = 60.0;
			{
				final Path2D.Double triangle = new Path2D.Double();
				projector.project( point, 0, plane2view.transform( centerX - 0.5 * size, centerY, 0.0 ) );
				triangle.moveTo( point[ 0 ], point[ 1 ] );
				projector.project( point, 0, plane2view.transform( centerX, centerY, 0.5 * size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				projector.project( point, 0, plane2view.transform( centerX + 0.5 * size, centerY, 0.0 ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				triangle.closePath();

				projector.project( point, 0, plane2view.transform( centerX - 0.5 * size, centerY, size ) );
				triangle.moveTo( point[ 0 ], point[ 1 ] );
				projector.project( point, 0, plane2view.transform( centerX, centerY, 1.5 * size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				projector.project( point, 0, plane2view.transform( centerX + 0.5 * size, centerY, size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				triangle.closePath();

				/*
				projector.project( point, plane2view.transform( centerX, centerY - 0.5 * size, 0.0 ) );
				triangle.moveTo( point[ 0 ], point[ 1 ] );
				projector.project( point, plane2view.transform( centerX, centerY, 0.5 * size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				projector.project( point, plane2view.transform( centerX, centerY + 0.5 * size, 0.0 ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				triangle.closePath();

				projector.project( point, plane2view.transform( centerX, centerY - 0.5 * size, size ) );
				triangle.moveTo( point[ 0 ], point[ 1 ] );
				projector.project( point, plane2view.transform( centerX, centerY, 1.5 * size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				projector.project( point, plane2view.transform( centerX, centerY + 0.5 * size, size ) );
				triangle.lineTo( point[ 0 ], point[ 1 ] );
				triangle.closePath();
				*/

				viewGraphics.fill( triangle );
			}
		}
	}

	public void addView( final View3D view )
	{
	}

	public void removeView( final View3D view )
	{
	}

	@Override
	public String toString()
	{
		return super.toString() + "[listener=" + _listener + ",behaviorHint=" + _behaviorHint + "]";
	}

	/**
	 * Returns whether the specified plane is visible.
	 *
	 * @param   plane2view  Plane-to-view transformation.
	 * @param   bounds      Bounds of the plane.
	 * @param   projector   Projector.
	 *
	 * @return  <code>true</code> if the plane is front-facing.
	 */
	private static boolean isVisible( final Matrix3D plane2view, final Rectangle2D bounds, final Projector projector )
	{
		final Vector3D v1 = plane2view.transform( bounds.getMinX(), bounds.getMinY(), 0.0 );
		final Vector3D v2 = plane2view.transform( bounds.getMaxX(), bounds.getMinY(), 0.0 );
		final Vector3D v3 = plane2view.transform( bounds.getMinX(), bounds.getMaxY(), 0.0 );
		final Vector3D v4 = plane2view.transform( bounds.getMaxX(), bounds.getMaxY(), 0.0 );

		boolean result = projector.inViewVolume( v1 ) &&
		                 projector.inViewVolume( v2 ) &&
		                 projector.inViewVolume( v3 ) &&
		                 projector.inViewVolume( v4 );

		if ( result )
		{
			final double[] projected = new double[ 6 ];
			projector.project( projected, 0, v1.x, v1.y, v1.z );
			projector.project( projected, 2, v2.x, v2.y, v2.z );
			projector.project( projected, 4, v3.x, v3.y, v3.z );
			final double p1x = projected[ 0 ];
			final double p1y = projected[ 1 ];
			final double p2x = projected[ 2 ];
			final double p2y = projected[ 3 ];
			final double p3x = projected[ 4 ];
			final double p3y = projected[ 5 ];

			result = ( ( ( p1x - p2x ) * ( p3y - p2y ) - ( p1y - p2y ) * ( p3x - p2x ) ) > 0.0 );
		}

		return result;
	}

	/**
	 * Calculates a transformation matrix for a drag plane through the specified
	 * line. The line is used as the matrix's X-axis. The other axes are
	 * calculated using the given scene-to-view matrix, such that the XY-plane
	 * is front-facing and as parallel to the view as possible.
	 *
	 * @param   scene2view  Scene-to-view matrix.
	 * @param   point       Point on the line.
	 * @param   direction   Direction of the line, used as the plane's x-axis.
	 *
	 * @return  Plane transform.
	 */
	private static Matrix3D calculateDragPlaneAlongLine( final Matrix3D scene2view, final Vector3D point, final Vector3D direction )
	{
		Vector3D dragPlaneY = Vector3D.cross( direction.x, direction.y, direction.z, scene2view.zx, scene2view.zy, scene2view.zz );
		dragPlaneY = dragPlaneY.normalize();

		final Vector3D dragPlaneZ = Vector3D.cross( dragPlaneY, direction );

		return new Matrix3D( direction.x, dragPlaneY.x, dragPlaneZ.x, point.x,
		                     direction.y, dragPlaneY.y, dragPlaneZ.y, point.y,
		                     direction.z, dragPlaneY.z, dragPlaneZ.z, point.z );
	}
}
