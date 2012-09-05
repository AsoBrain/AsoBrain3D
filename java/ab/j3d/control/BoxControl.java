/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
import java.util.*;
import java.util.List;

import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.planar.*;
import org.jetbrains.annotations.*;

/**
 * A control for manipulating a content node based on its bounding box.
 *
 * <p>
 * The following diagram shows a fold-out of the box, including the sides and
 * edges.
 * <pre>
 *                    +--+------------+--+
 *                    |  |     Y+     |  |
 *                    +--+------------+--+
 *                    |  |            |  |
 *                    |X-|    Top     |X+|
 *                    |  |    (Z+)    |  |
 *                    +--+------------+--+
 *                    |  |     Y-     |  |
 * +--+------------+--+--+------------+--+--+------------+--+--+------------+--+
 * |  |     Y+     |  |  |     Y+     |  |  |     Y+     |  |  |     Y+     |  |
 * +--+------------+--+--+------------+--+--+------------+--+--+------------+--+
 * |  |            |  |  |            |  |  |            |  |  |            |  |
 * |X-|    Left    |X+|X-|   Front    |X+|X-|   Right    |X+|X-|    Rear    |X+|
 * |  |    (X-)    |  |  |    (Y-)    |  |  |    (X+)    |  |  |    (Y+)    |  |
 * +--+------------+--+--+------------+--+--+------------+--+--+------------+--+
 * |  |     Y-     |  |  |     Y-     |  |  |     Y-     |  |  |     Y-     |  |
 * +---------------+--+--+------------+--+--+------------+--+--+------------+--+
 *                    |  |     Y+     |  |
 *                    +--+------------+--+
 *                    |  |            |  |
 *                    |X-|   Bottom   |X+|
 *                    |  |    (Z-)    |  |
 *                    +--+------------+--+
 *                    |  |     Y-     |  |
 *                    +--+------------+--+
 * </pre>
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class BoxControl
	implements ContentNodeControl, ViewOverlay
{
	/**
	 * The (six) sides of the box.
	 */
	private final List<BoxSide> _sides;

	/**
	 * Side that is currently being dragged.
	 */
	private BoxSide _activeSide = null;

	/**
	 * Listener to be used as a fallback, when no part of the box has captured
	 * an event.
	 */
	private ContentNodeControl _listener = null;

	/**
	 * Precedence of the control relative to other content node controls.
	 */
	private int _precedence = 0;

	/**
	 * An arbitrary object that will be included in the {@link #toString} of
	 * the control for easily recognizing a particular control.
	 */
	private Object _tag = null;

	/**
	 * Whether the mouse is currently positioned inside the box.
	 */
	private boolean _hover;

	/**
	 * Construct new control.
	 *
	 * @param   node    Content node to manipulate.
	 */
	public BoxControl( final ContentNode node )
	{
		this( node, false );
	}

	/**
	 * Construct new control.
	 *
	 * @param   node            Content node to manipulate.
	 * @param   insideOfBox     <code>true</code> for an inverted box,
	 *                          controlled from the inside.
	 */
	public BoxControl( final ContentNode node, final boolean insideOfBox )
	{
		final List<BoxSide> sides = new ArrayList<BoxSide>( BoxSide.Side.values().length );
		for ( final BoxSide.Side side : BoxSide.Side.values() )
		{
			sides.add( new ContentNodeBoxSide( node, side, insideOfBox ) );
		}
		_sides = sides;
	}

	public int getPrecedence()
	{
		return _precedence;
	}

	/**
	 * Sets the precedence of the control.
	 *
	 * @param   precedence  Precedence to be set.
	 */
	public void setPrecedence( final int precedence )
	{
		_precedence = precedence;
	}

	/**
	 * Returns the object used to tag this control.
	 *
	 * @return  Tag object.
	 */
	@Nullable
	public Object getTag()
	{
		return _tag;
	}

	/**
	 * Sets an arbitrary object used to tag this control (e.g. for debugging).
	 *
	 * @param   tag     Tag object.
	 */
	public void setTag( @Nullable final Object tag )
	{
		_tag = tag;
	}

	/**
	 * Returns the control that handles events for the entire box, if no sides
	 * or edges handle a given event.
	 *
	 * @return  Control for the entire box.
	 */
	public ContentNodeControl getListener()
	{
		return _listener;
	}

	/**
	 * Sets a control to handle events for the entire box, if no sides or edges
	 * handle a given event.
	 *
	 * @param   listener    Control to be set.
	 */
	public void setListener( final ContentNodeControl listener )
	{
		_listener = listener;
	}

	/**
	 * Sets the delegate to be used for the specified side of the box.
	 *
	 * @param   side        Side of the box.
	 * @param   listener    Delegate to be set.
	 */
	public void setListener( final BoxSide.Side side, final BoxControlDelegate listener )
	{
		setListener( side, listener, BoxSide.BehaviorHint.NONE );
	}

	/**
	 * Sets the delegate to be used for the specified side of the box.
	 * For convenience, this method also sets the side's behavior hint.
	 *
	 * @param   side            Side of the box.
	 * @param   listener        Delegate to be set.
	 * @param   behaviorHint    Behavior hint to be set.
	 */
	public void setListener( final BoxSide.Side side, final BoxControlDelegate listener, final BoxSide.BehaviorHint behaviorHint )
	{
		final BoxSide sideObject = getSide( side );
		sideObject.setListener( listener );
		sideObject.setBehaviorHint( behaviorHint );
	}

	/**
	 * Sets the delegate to be used for the specified side of the box.
	 * For convenience, this method also sets the side's behavior.
	 *
	 * @param   side        Side of the box.
	 * @param   listener    Delegate to be set.
	 * @param   behavior    Behavior to be set.
	 */
	public void setListener( final BoxSide.Side side, final BoxControlDelegate listener, final BoxSide.Behavior behavior )
	{
		setListener( side, listener, behavior, BoxSide.BehaviorHint.NONE );
	}

	/**
	 * Sets the delegate to be used for the specified side of the box.
	 * For convenience, this method also sets the side's behavior (and hint).
	 *
	 * @param   side            Side of the box.
	 * @param   listener        Delegate to be set.
	 * @param   behavior        Behavior to be set.
	 * @param   behaviorHint    Behavior hint to be set.
	 */
	public void setListener( final BoxSide.Side side, final BoxControlDelegate listener, final BoxSide.Behavior behavior, final BoxSide.BehaviorHint behaviorHint )
	{
		final BoxSide sideObject = getSide( side );
		sideObject.setListener( listener );
		sideObject.setBehavior( behavior );
		sideObject.setBehaviorHint( behaviorHint );
	}

	/**
	 * Returns a side of the box.
	 *
	 * @param   side    Specifies a side.
	 *
	 * @return  Object representing the specified side.
	 */
	public BoxSide getSide( @NotNull final BoxSide.Side side )
	{
		return _sides.get( side.ordinal() );
	}

	public Double getDepth( final Ray3D pointerRay )
	{
		Double result = null;

		final boolean entireBoxHasListener = ( _listener != null );

		for ( final BoxSide side : _sides )
		{
			if ( side.isEnabled() || entireBoxHasListener )
			{
				final Double reference = side.getDepth( pointerRay );
				if ( reference != null )
				{
					if ( ( result == null ) || ( reference < result ) )
					{
						result = reference;
					}
				}
			}
		}

		return result;
	}

	public void mouseMoved( final ControlInputEvent event, final ContentNode contentNode )
	{
		boolean hover = false;
		for ( final BoxSide side : _sides )
		{
			side.mouseMoved( event, contentNode );
			hover |= side.isHover();
		}
		_hover = hover;

		if ( _listener != null )
		{
			_listener.mouseMoved( event, contentNode );
		}
	}

	/**
	 * Returns whether the mouse is hovering above the box.
	 *
	 * @return  {@code true} if the mouse is hovering above the box.
	 */
	public boolean isHover()
	{
		return _hover;
	}

	public boolean mousePressed( final ControlInputEvent event, final ContentNode contentNode )
	{
		boolean result = false;

		if ( event.isMouseButton1Down() )
		{
			for ( final BoxSide side : _sides )
			{
				if ( side.isEnabled() && side.mousePressed( event, contentNode ) )
				{
					_activeSide = side;
					result = true;
					break;
				}
			}

			if ( !result )
			{
				final ContentNodeControl listener = _listener;
				if ( listener != null )
				{
					result = listener.mousePressed( event, contentNode );
				}
			}
		}

		return result;
	}

	public boolean mouseDragged( final ControlInputEvent event, final ContentNode contentNode )
	{
		boolean result = false;

		final BoxSide side = _activeSide;
		if ( side != null )
		{
			result = side.mouseDragged( event, contentNode );
		}
		else
		{
			final ContentNodeControl listener = _listener;
			if ( listener != null )
			{
				result = listener.mouseDragged( event, contentNode );
			}
		}

		return result;
	}

	public void mouseReleased( final ControlInputEvent event, final ContentNode contentNode )
	{
		final BoxSide activeSide = _activeSide;
		if ( activeSide != null )
		{
			activeSide.mouseReleased( event, contentNode );
			_activeSide = null;
		}
		else
		{
			final ContentNodeControl listener = _listener;
			if ( listener != null )
			{
				listener.mouseReleased( event, contentNode );
			}
		}
	}

	public void paintOverlay( final View3D view, final Graphics2D g )
	{
		for ( final BoxSide side : _sides )
		{
			side.paintOverlay( view, g );
		}
	}

	public void addView( final View3D view )
	{
	}

	public void removeView( final View3D view )
	{
	}

	/**
	 * Configures the edges on all sides of the box to use the specified
	 * listeners. This can be used, for example, to easily create a control to
	 * resize a box by dragging edges in the desired direction.
	 *
	 * @param   x1  Listener for edges on the left side of the box.
	 * @param   x2  Listener for edges on the right side of the box.
	 * @param   y1  Listener for edges on the front side of the box.
	 * @param   y2  Listener for edges on the rear side of the box.
	 * @param   z1  Listener for edges on the bottom side of the box.
	 * @param   z2  Listener for edges on the top side of the box.
	 */
	public void setupEdges( @Nullable final BoxControlDelegate x1, @Nullable final BoxControlDelegate x2, @Nullable final BoxControlDelegate y1, @Nullable final BoxControlDelegate y2, @Nullable final BoxControlDelegate z1, @Nullable final BoxControlDelegate z2 )
	{
		final BoxSide top = getSide( BoxSide.Side.TOP );
		top.setEdgeBehavior( BoxSide.Behavior.PLANE );
		top.setEdgeListenerX1( x1 );
		top.setEdgeListenerX2( x2 );
		top.setEdgeListenerY1( y1 );
		top.setEdgeListenerY2( y2 );

		final BoxSide bottom = getSide( BoxSide.Side.BOTTOM );
		bottom.setEdgeBehavior( BoxSide.Behavior.PLANE );
		bottom.setEdgeListenerX1( x2 );
		bottom.setEdgeListenerX2( x1 );
		bottom.setEdgeListenerY1( y1 );
		bottom.setEdgeListenerY2( y2 );

		final BoxSide left = getSide( BoxSide.Side.LEFT );
		left.setEdgeBehavior( BoxSide.Behavior.PLANE );
		left.setEdgeListenerX1( y2 );
		left.setEdgeListenerX2( y1 );
		left.setEdgeListenerY1( z1 );
		left.setEdgeListenerY2( z2 );

		final BoxSide right = getSide( BoxSide.Side.RIGHT );
		right.setEdgeBehavior( BoxSide.Behavior.PLANE );
		right.setEdgeListenerX1( y1 );
		right.setEdgeListenerX2( y2 );
		right.setEdgeListenerY1( z1 );
		right.setEdgeListenerY2( z2 );

		final BoxSide front = getSide( BoxSide.Side.FRONT );
		front.setEdgeBehavior( BoxSide.Behavior.PLANE );
		front.setEdgeListenerX1( x1 );
		front.setEdgeListenerX2( x2 );
		front.setEdgeListenerY1( z1 );
		front.setEdgeListenerY2( z2 );

		final BoxSide rear = getSide( BoxSide.Side.REAR );
		rear.setEdgeBehavior( BoxSide.Behavior.PLANE );
		rear.setEdgeListenerX1( x2 );
		rear.setEdgeListenerX2( x1 );
		rear.setEdgeListenerY1( z1 );
		rear.setEdgeListenerY2( z2 );
	}

	/**
	 * Configures the edges on the left (X-) and right (X+) of the box to use
	 * the given listeners. This affects edges on all sides of the box.
	 *
	 * @param   x1  Listener for edges on the left side of the box.
	 * @param   x2  Listener for edges on the right side of the box.
	 */
	public void setupLeftRight( @Nullable final BoxControlDelegate x1, @Nullable final BoxControlDelegate x2 )
	{
		final BoxSide left = getSide( BoxSide.Side.LEFT );
		left.setListener( x1 );
		left.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide right = getSide( BoxSide.Side.RIGHT );
		right.setListener( x2 );
		right.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide top = getSide( BoxSide.Side.TOP );
		top.setEdgeBehavior( BoxSide.Behavior.PLANE );
		top.setEdgeListenerX1( x1 );
		top.setEdgeListenerX2( x2 );

		final BoxSide bottom = getSide( BoxSide.Side.BOTTOM );
		bottom.setEdgeBehavior( BoxSide.Behavior.PLANE );
		bottom.setEdgeListenerX1( x1 );
		bottom.setEdgeListenerX2( x2 );

		final BoxSide front = getSide( BoxSide.Side.FRONT );
		front.setEdgeBehavior( BoxSide.Behavior.PLANE );
		front.setEdgeListenerX1( x1 );
		front.setEdgeListenerX2( x2 );

		final BoxSide rear = getSide( BoxSide.Side.REAR );
		rear.setEdgeBehavior( BoxSide.Behavior.PLANE );
		rear.setEdgeListenerX1( x2 );
		rear.setEdgeListenerX2( x1 );
	}

	/**
	 * Configures the edges on the front (Y-) and rear (Y+) of the box to use
	 * the given listeners. This affects edges on all sides of the box.
	 *
	 * @param   y1  Listener for edges on the front side of the box.
	 * @param   y2  Listener for edges on the rear side of the box.
	 */
	public void setupFrontRear( @Nullable final BoxControlDelegate y1, @Nullable final BoxControlDelegate y2 )
	{
		final BoxSide front = getSide( BoxSide.Side.FRONT );
		front.setListener( y1 );
		front.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide rear = getSide( BoxSide.Side.REAR );
		rear.setListener( y2 );
		rear.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide top = getSide( BoxSide.Side.TOP );
		top.setEdgeBehavior( BoxSide.Behavior.PLANE );
		top.setEdgeListenerY1( y1 );
		top.setEdgeListenerY2( y2 );

		final BoxSide bottom = getSide( BoxSide.Side.BOTTOM );
		bottom.setEdgeBehavior( BoxSide.Behavior.PLANE );
		bottom.setEdgeListenerY1( y1 );
		bottom.setEdgeListenerY2( y2 );

		final BoxSide left = getSide( BoxSide.Side.LEFT );
		left.setEdgeBehavior( BoxSide.Behavior.PLANE );
		left.setEdgeListenerX1( y2 );
		left.setEdgeListenerX2( y1 );

		final BoxSide right = getSide( BoxSide.Side.RIGHT );
		right.setEdgeBehavior( BoxSide.Behavior.PLANE );
		right.setEdgeListenerX1( y1 );
		right.setEdgeListenerX2( y2 );
	}

	/**
	 * Configures the edges on the bottom (Z-) and top (Z+) of the box to use
	 * the given listeners. This affects edges on all sides of the box.
	 *
	 * @param   z1  Listener for edges on the bottom side of the box.
	 * @param   z2  Listener for edges on the top side of the box.
	 */
	public void setupBottomTop( @Nullable final BoxControlDelegate z1, @Nullable final BoxControlDelegate z2 )
	{
		final BoxSide bottom = getSide( BoxSide.Side.BOTTOM );
		bottom.setListener( z1 );
		bottom.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide top = getSide( BoxSide.Side.TOP );
		top.setListener( z2 );
		top.setBehavior( BoxSide.Behavior.NORMAL );

		final BoxSide front = getSide( BoxSide.Side.FRONT );
		front.setEdgeBehavior( BoxSide.Behavior.PLANE );
		front.setEdgeListenerY1( z1 );
		front.setEdgeListenerY2( z2 );

		final BoxSide rear = getSide( BoxSide.Side.REAR );
		rear.setEdgeBehavior( BoxSide.Behavior.PLANE );
		rear.setEdgeListenerY1( z1 );
		rear.setEdgeListenerY2( z2 );

		final BoxSide left = getSide( BoxSide.Side.LEFT );
		left.setEdgeBehavior( BoxSide.Behavior.PLANE );
		left.setEdgeListenerY1( z1 );
		left.setEdgeListenerY2( z2 );

		final BoxSide right = getSide( BoxSide.Side.RIGHT );
		right.setEdgeBehavior( BoxSide.Behavior.PLANE );
		right.setEdgeListenerY1( z1 );
		right.setEdgeListenerY2( z2 );
	}

	/**
	 * Sets the width of all edges.
	 *
	 * @param   width   Width to be set.
	 */
	public void setEdgeWidth( final double width )
	{
		for ( final BoxSide side : _sides )
		{
			side.setEdgeWidth( width );
		}
	}

	/**
	 * Sets the color of all edges.
	 *
	 * @param   color   Color to be set.
	 */
	public void setEdgeColor( final Color color )
	{
		for ( final BoxSide side : _sides )
		{
			side.setEdgeColor( color );
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "[tag=" + _tag + ']';
	}
}
