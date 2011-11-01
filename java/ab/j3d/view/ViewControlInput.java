/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * The <code>ViewControlInput</code> receives input events for a {@link View3D},
 * converts them to {@link ControlInputEvent}s, and sends them to registered
 * {@link ControlInputListener listeners}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewControlInput
	implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	/**
	 * {@link View3D} whose input events to monitor.
	 */
	private final View3D _view;

	/**
	 * X coordinate of pointer when drag operation was started. This is stored
	 * when a {@link MouseEvent#MOUSE_PRESSED} event is received.
	 */
	protected int _dragStartX;

	/**
	 * X coordinate of pointer when drag operation was started. This is stored
	 * when a {@link MouseEvent#MOUSE_PRESSED} event is received.
	 */
	protected int _dragStartY;

	/**
	 * List of registered listeners.
	 */
	private final List<ControlInputListener> _controlInputListeners = new ArrayList<ControlInputListener>();

	/**
	 * Listener that is given first pick when filtering events. The rest of the
	 * listener ordering is retained. When the focus is released, the original
	 * listener ordering is restored.
	 */
	private ControlInputListener _focusListener = null;

	/**
	 * The number of the current series of events. This number is increased
	 * after a {@link MouseEvent#MOUSE_RELEASED} event with no mouse buttons
	 * down.
	 */
	private int _eventNumber;

	/**
	 * Wether or not the mouse has been dragged since the last mouse press
	 * event.
	 */
	private boolean _wasDragged;

	/**
	 * Construct new ViewInputTranslator.
	 *
	 * @param   view    {@link View3D} whose input events to monitor.
	 */
	public ViewControlInput( @NotNull final View3D view )
	{
		_view = view;

		_dragStartX = 0;
		_dragStartY = 0;
		_eventNumber = 0;
		_wasDragged = false;

		final Component component = view.getComponent();
		if ( component != null )
		{
			component.addKeyListener( this );
			component.addMouseListener( this );
			component.addMouseMotionListener( this );
			component.addMouseWheelListener( this );
		}
	}

	/**
	 * Creates a {@link ControlInputEvent} for a specific {@link InputEvent}.
	 *
	 * @param   inputEvent  {@link InputEvent} to create a
	 *                      {@link ControlInputEvent} for.
	 *
	 * @return  A {@link ControlInputEvent} for the specified {@link InputEvent}
	 *
	 * @see     #_eventNumber
	 * @see     #_wasDragged
	 */
	protected ControlInputEvent createControlnputEvent( final InputEvent inputEvent )
	{
		boolean wasDragged = _wasDragged;

		final int eventID = inputEvent.getID();
		if ( eventID == MouseEvent.MOUSE_PRESSED )
		{
			_dragStartX = ((MouseEvent)inputEvent).getX();
			_dragStartY = ((MouseEvent)inputEvent).getY();
		}
		else if ( eventID == MouseEvent.MOUSE_DRAGGED )
		{
			wasDragged = true;
		}

		final ControlInputEvent result = new ControlInputEvent( this, inputEvent, _eventNumber, wasDragged, _dragStartX, _dragStartY );

		if ( ( eventID == MouseEvent.MOUSE_RELEASED ) && !result.isMouseButtonDown() )
		{
			_eventNumber++;
			wasDragged = false;
		}

		_wasDragged = wasDragged;

		return result;
	}

	/**
	 * Add a {@link ControlInputListener}. This listener will be called before
	 * all previously registered listeners.
	 *
	 * @param   listener    Listener to add.
	 */
	public void addControlInputListener( final ControlInputListener listener )
	{
		_controlInputListeners.add( listener );
	}

	/**
	 * Add a {@link ControlInputListener}. This listener will be called before
	 * all previously registered listeners.
	 *
	 * @param   listener    Listener to add.
	 */
	public void insertControlInputListener( final ControlInputListener listener )
	{
		_controlInputListeners.add( 0, listener );
	}

	/**
	 * Removes a previously registered {@link ControlInputListener}.
	 *
	 * @param   listener    Listener to remove
	 */
	public void removeControlInputListener( final ControlInputListener listener )
	{
		_controlInputListeners.remove( listener );
	}

	/**
	 * Tests whether the specified listener currently has this dispatcher's focus.
	 *
	 * @param   listener  Listener to test.
	 *
	 * @return  <code>true</code> if the specified listener has focus;
	 *          <code>false</code> otherwise.
	 */
	public boolean hasFocus( @NotNull final ControlInputListener listener )
	{
		return ( listener == _focusListener );
	}

	/**
	 * Requests that focus is transferred to the specified listener.
	 * <p>
	 * Until the focus is released or transferred to another listener, only the
	 * specified listener will receive events.
	 *
	 * @param   listener    Listener to be given focus.
	 */
	public void requestFocus( @NotNull final ControlInputListener listener )
	{
		_focusListener = listener;
	}

	/**
	 * Requests that focus is released from any listener set as such. This will
	 * restore the original listener ordering.
	 */
	public void releaseFocus()
	{
		_focusListener = null;
	}

	/**
	 * This converts an {@link InputEvent} to a {@link ControlInputEvent}, and
	 * sends it to registered listeners. If a focus listener is set, only that
	 	 * listener will receive the event.
	 *
	 * @param   inputEvent  Input event to dispatch.
	 */
	protected void dispatchControlInputEvent( final InputEvent inputEvent )
	{
		dispatchControlInputEvent( createControlnputEvent( inputEvent ) );
	}

	/**
	 * Send event to registered listeners. If a focus listener is set, only that
	 * listener will receive the event.
	 *
	 * @param   event   Event to dispatch.
	 */
	protected void dispatchControlInputEvent( final ControlInputEvent event )
	{
		final ControlInputListener focusListener = _focusListener;
		if ( focusListener != null )
		{
			focusListener.inputReceived( event );
		}

		// re-test focus; the focus listener may have released focus
		if ( _focusListener == null )
		{
			final List<ControlInputListener> listeners = _controlInputListeners;
			/* NOTE: use indexed loop here to prevent ConcurrentModificationException's when listeners change the listener list */
			//noinspection ForLoopReplaceableByForEach
			for ( int i = 0 ; i < listeners.size() ; i++ )
			{
				final ControlInputListener listener = listeners.get( i );
				if ( listener != focusListener )
				{
					listener.inputReceived( event );
				}
			}
		}
	}

	/**
	 * Returns the {@link Projector} for the {@link View3D}.
	 *
	 * @return  The {@link Projector} for the {@link View3D}.
	 */
	public Projector getProjector()
	{
		return _view.getProjector();
	}

	/**
	 * Returns a List of {@link Face3DIntersection}s, which hold information
	 * about the objects that are intersected by the specified ray.
	 *
	 * @param   ray     Ray to get intersections for.
	 *
	 * @return  A list of {@link Face3DIntersection}s, ordered from near to far.
	 */
	public List<Face3DIntersection> getIntersections( @NotNull final Ray3D ray )
	{
		final RayIntersectionVisitor intersectionVisitor = new RayIntersectionVisitor( ray );

		final Scene scene = _view.getScene();
		for ( final ContentNode contentNode : scene.getContentNodes() )
		{
			intersectionVisitor.walk( contentNode );
		}

		return intersectionVisitor.intersections;
	}

	/**
	 * Get {@link View3D} being monitored for input events.
	 *
	 * @return  {@link View3D} being monitored for input events.
	 */
	public View3D getView()
	{
		return _view;
	}

	/**
	 * Returns the current view transform for the {@link View3D}.
	 *
	 * @return  the view transform for the {@link View3D}.
	 */
	public Matrix3D getScene2View()
	{
		return _view.getScene2View();
	}

	/**
	 * Returns the current view transform for the {@link View3D}.
	 *
	 * @return  the view transform for the {@link View3D}.
	 */
	public Matrix3D getView2Scene()
	{
		return _view.getView2Scene();
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyPressed( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyReleased( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyTyped( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseClicked( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseDragged( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseEntered( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseExited( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseMoved( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mousePressed( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseReleased( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseWheelMoved( final MouseWheelEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * This visitor collects intersections with a given ray.
	 */
	private static class RayIntersectionVisitor
		implements Node3DVisitor
	{
		/**
		 * Intersections with ray.
		 */
		public final List<Face3DIntersection> intersections = new ArrayList<Face3DIntersection>();

		/**
		 * ID of currently visited object (content node).
		 */
		private Object _objectId = null;

		/**
		 * Ray to find intersections with.
		 */
		private final Ray3D _ray;

		/**
		 * Create visitor.
		 *
		 * @param   ray     Ray to find intersections with.
		 */
		private RayIntersectionVisitor( final Ray3D ray )
		{
			_ray = ray;
		}

		/**
		 * Walk through the specified content node.
		 *
		 * @param   contentNode     Content node to walk through.
		 */
		public void walk( final ContentNode contentNode )
		{
			_objectId = contentNode.getID();
			Node3DTreeWalker.walk( this, contentNode.getTransform(), contentNode.getNode3D() );
		}

		public boolean visitNode( @NotNull final Node3DPath path )
		{
			final Node3D node = path.getNode();
			if ( node instanceof Object3D )
			{
				final Object3D object = (Object3D) node;
				object.getIntersectionsWithRay( intersections, true, _objectId, path, path.getTransform(), _ray );
			}

			return true;
		}
	}
}
