/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector;

import com.numdata.oss.event.EventDispatcher;

/**
 * The SceneInputTranslator listens to events on a component and creates
 * {@link java.util.EventObject}s with their information. These
 * {@link java.util.EventObject}s are then dispatched by the
 * {@link EventDispatcher}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class ControlInput
	implements KeyListener , MouseListener , MouseMotionListener
{
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
	 * The {@link EventDispatcher} that dispatches the
	 * {@link java.util.EventObject}s to registered {@link Control}s.
	 */
	private EventDispatcher _eventDispatcher;

//	/**
//	 * The timestamp of the last processed mouse drag event. For efficiency, not
//	 * all mouse drag events are processed. If a certain amount of time passes
//	 * since this last drag, a new event is processed. Any other are discarded.
//	 * After a new event has been processed, this event's timestamp is used as
//	 * the new <code>_lastDrag</code>.
//	 */
//	private long _lastDrag;

	/**
	 * Construct new SceneInputTranslator.
	 *
	 * @param   component   The component to listen to for events
	 */
	protected ControlInput( final Component component )
	{
		_eventNumber = 0;
		_dragStartX  = 0;
		_dragStartY  = 0;
		_wasDragged  = false;
//		_lastDrag    = -1L;

		_eventDispatcher = new EventDispatcher();

		component.addKeyListener( this );
		component.addMouseListener( this );
		component.addMouseMotionListener( this );
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
	private ControlInputEvent createSceneInputEvent( final InputEvent inputEvent )
	{
		final Ray3D pointerRay;

		if ( inputEvent instanceof MouseEvent )
		{
			final MouseEvent mouseEvent = (MouseEvent)inputEvent;

			final Projector projector  = getProjector();
			final Matrix3D  world2view = getViewTransform();
			final Matrix3D  view2world = world2view.inverse();

			pointerRay = projector.getPointerRay( view2world , (double)mouseEvent.getX() , (double)mouseEvent.getY() );
		}
		else
		{
			pointerRay = null;
		}

		return new ControlInputEvent( this , inputEvent , _eventNumber , _wasDragged , _dragStartX , _dragStartY , pointerRay );
	}

	/**
	 * Returns the {@link EventDispatcher} that dispatches the
	 * {@link java.util.EventObject}s to registered {@link Control}s.
	 *
	 * @return  This translators {@link EventDispatcher}.
	 */
	public EventDispatcher getEventDispatcher()
	{
		return _eventDispatcher;
	}

	/**
	 * Returns the ID for an {@link Object3D}.
	 *
	 * @param   object  The object for which to return the ID.
	 *
	 * @return  The ID for <code>object</code>.
	 */
	protected abstract Object getIDForObject( Object3D object );

	/**
	 * Returns a List of {@link Face3DIntersection}s, which hold information
	 * about the objects that are intersected by the specified ray.
	 *
	 * @param   ray     Ray to get intersections for.
	 *
	 * @return  A list of {@link Face3DIntersection}s, ordered from near to far.
	 *
	 * @throws  NullPointerException if <code>ray</code> is <code>null</code>.
	 */
	public List getIntersections( final Ray3D ray )
	{
		final List result = new LinkedList();

		final Node3DCollection scene = getScene();
		for ( int i = 0 ; i < scene.size() ; i++ )
		{
			final Object3D object       = (Object3D)scene.getNode( i );
			final Matrix3D object2world = scene.getMatrix( i );

			object.getIntersectionsWithRay( result , true , getIDForObject( object ) , object2world , ray );
		}

		return result;
	}

	/**
	 * Returns the {@link Projector} for this scene.
	 *
	 * @return  {@link Projector} for this scene.
	 */
	protected abstract Projector getProjector();

	/**
	 * Returns a {@link Node3DCollection} with all {@link Object3D}s in the
	 * scene. Implementing classes should only put objects that need to be
	 * tested for intersection in this collection. The transform matrices in
	 * the {@link Node3DCollection} should hold the matrix for transforming the
	 * object to world coordinates. If the scene is empty, an empty
	 * {@link Node3DCollection} should be returned.
	 *
	 * @return  A {@link Node3DCollection} containing the objects in the scene.
	 */
	protected abstract Node3DCollection getScene();

	/**
	 * Returns the current view transform for this scene. The view transform
	 * transforms world to view coordinates.
	 *
	 * @return  View transform for this scene.
	 */
	protected abstract Matrix3D getViewTransform();

	/**
	 * Simply let {@link KeyEvent} pass through.
	 *
	 * @param   event   Key event.
	 */
	public void keyPressed( final KeyEvent event )
	{
		_eventDispatcher.dispatch( createSceneInputEvent( event ) );
	}

	/**
	 * Simply let {@link KeyEvent} pass through.
	 *
	 * @param   event   Key event.
	 */
	public void keyReleased( final KeyEvent event )
	{
		_eventDispatcher.dispatch( createSceneInputEvent( event ) );
	}

	/**
	 * Simply let {@link KeyEvent} pass through.
	 *
	 * @param   event   Key event.
	 */
	public void keyTyped( final KeyEvent event )
	{
		_eventDispatcher.dispatch( createSceneInputEvent( event ) );
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a
	 * component. Does nothing.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseClicked( final MouseEvent event )
	{
		_eventDispatcher.dispatch( createSceneInputEvent( event ) );
	}

	/**
	 * Called when the mouse has been dragged on the component this translator
	 * is listening to. A new {@link ControlInputEvent} is created and
	 * dispatched to the {@link EventDispatcher}.
	 * <p>
	 * Note that, in order to improve performance, only one drag event every
	 * 100 miliseconds is processed. Any others are discarded.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched.
	 *
	 * @see     #createSceneInputEvent
	 */
	public void mouseDragged( final MouseEvent event )
	{
//		final long timeStamp = event.getWhen();
//		if ( timeStamp > ( _lastDrag + 100L ) )
//		{
			_wasDragged = true;
			_eventDispatcher.dispatch( createSceneInputEvent( event ) );
//			_lastDrag = timeStamp;
//		}
	}

	/**
	 * Invoked when the mouse enters a component. Does nothing.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseEntered( final MouseEvent event ) { }

	/**
	 * Invoked when the mouse exits a component. Does nothing.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseExited( final MouseEvent event ) { }

	/**
	 * Invoked when the mouse cursor has been moved onto a component
	 * but no buttons have been pushed. Does nothing.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseMoved( final MouseEvent event )
	{
	}

	/**
	 * Called when the mouse has been pressed on the component this translator
	 * is listening to. A new {@link ControlInputEvent} is created and
	 * dispatched to the {@link EventDispatcher}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 *
	 * @see     #createSceneInputEvent
	 */
	public void mousePressed( final MouseEvent event )
	{
		_dragStartX = event.getX();
		_dragStartY = event.getY();

		_eventDispatcher.dispatch( createSceneInputEvent( event ) );
	}

	/**
	 * Called when the mouse has been released on the component this translator
	 * is listening to. A new {@link ControlInputEvent} is created and
	 * dispatched to the {@link EventDispatcher}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 *
	 * @see     #createSceneInputEvent
	 */
	public void mouseReleased( final MouseEvent event )
	{
		final ControlInputEvent controlInputEvent = createSceneInputEvent( event );
		_eventDispatcher.dispatch( controlInputEvent );

		if ( !controlInputEvent.isMouseButtonDown() )
		{
			_eventNumber++;
			_wasDragged = false;
		}
	}
}


