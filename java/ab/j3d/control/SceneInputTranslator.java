/*
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import ab.j3d.Vector3D;
import ab.j3d.Matrix3D;
import ab.j3d.view.Projector;

/**
 * The SceneInputTranslator listens to events on a component and creates
 * {@link ControlEvent}s with their information. These {@link ControlEvent}s
 * are then dispatched by the {@link ControlEventQueue}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public abstract class SceneInputTranslator
	implements MouseListener, MouseMotionListener
{
	/**
	 * The number of the current series of events. A mouse click, drag and
	 * release all have the same number, the next series of mouse events have a
	 * number increased by one.
	 */
	private int _eventNumber;

	/**
	 * Wether or not the mouse has been dragged since the last mouse press
	 * event.
	 */
	private boolean _mouseDragged;

	/**
	 * The {@link ControlEventQueue} that dispatches the {@link ControlEvent}s
	 * to registered {@link Control}s.
	 */
	private ControlEventQueue _eventQueue;

	/**
	 * The timestamp of the last processed mouse drag event. For efficiency, not
	 * all mouse drag events are processed. If a certain amount of time passes
	 * since this last drag, a new event is processed. Any other are discarded.
	 * After a new event has been processed, this event's timestamp is used as
	 * the new <code>_lastDrag</code>.
	 */
	private long _lastDrag;

	/**
	 * Construct new SceneInputTranslator.
	 *
	 * @param   component   The component to listen to for events
	 */
	protected SceneInputTranslator( final Component component )
	{
		component.addMouseListener( this );
		component.addMouseMotionListener( this );

		_eventNumber = 0;
		_mouseDragged = false;
		_lastDrag = 0L;

		_eventQueue = new ControlEventQueue();
	}

	/**
	 * Returns the {@link ControlEventQueue} that dispatches the
	 * {@link ControlEvent}s to registered {@link Control}s.
	 *
	 * @return  This translators {@link ControlEventQueue}.
	 */
	public ControlEventQueue getEventQueue()
	{
		return _eventQueue;
	}

	/**
	 * Returns the {@link IntersectionSupport} for this scene. This
	 * {@link IntersectionSupport} is used by {@link #createMouseControlEvent} to
	 * get the objects beneath the mouse pointer.
	 *
	 * @return  {@link IntersectionSupport} for this scene.
	 *
	 * @see     #createMouseControlEvent
	 */
	protected abstract IntersectionSupport getIntersectionSupport();

	/**
	 * Returns the {@link Projector} for this scene. The projector is used by
	 * {@link #screenToWorld} to convert a location on the screen to world
	 * coordinates.
	 *
	 * @return  {@link Projector} for this scene.
	 *
	 * @see     #screenToWorld
	 */
	protected abstract Projector getProjector();

	/**
	 * Returns the current view transform for this scene. The view transform is
	 * used to transform a point in {@link #screenToWorld} from view coordinates
	 * to world coordinates.
	 *
	 * @return  the view transform for this scene.
	 *
	 * @see     #screenToWorld
	 */
	protected abstract Matrix3D getViewTransform();

	/**
	 * Creates a {@link MouseControlEvent} for a specific {@link MouseEvent}.
	 * This method calls {@link #screenToWorld} to get the line 'shot' from the
	 * mouse, and {@link #getIntersectionSupport} to get the
	 * {@link IntersectionSupport}, which can calculate the objects under the
	 * mouse.
	 *
	 * @param   e               {@link MouseEvent} to create a
	 *                          {@link MouseControlEvent} for
	 * @param   eventNumber     Number of the current series of events.
	 * @param   mouseDragged    Wether or not the mouse has been dragged since
	 *                          the last mouse press event.
	 *
	 * @return  a {@link MouseControlEvent} for the specified {@link MouseEvent}
	 *
	 * @see     #_eventNumber
	 * @see     #_mouseDragged
	 * @see     #screenToWorld
	 * @see     #getIntersectionSupport
	 */
	private MouseControlEvent createMouseControlEvent( final MouseEvent e, final int eventNumber, final boolean mouseDragged )
	{
		final IntersectionSupport support = getIntersectionSupport();

		final int x = e.getX();
		final int y = e.getY();

		final Vector3D lineStart = screenToWorld( x , y ,    0.0 );
		final Vector3D linePoint = screenToWorld( x , y , -100.0 );

		final List facesClicked = support.getIntersections( lineStart, linePoint );

		return new MouseControlEvent( e , eventNumber , mouseDragged , facesClicked , lineStart , linePoint );
	}

	/**
	 * This method returns the point in the world for a given point on the
	 * screen. <code>screenX</code> and <code>screenY</code> are the screen
	 * coordinates, and distance is the distance between the viewing plane and
	 * the 'unprojected' coordinate.<p>
	 * This method uses {@link #getProjector} to get the {@link Projector} for
	 * this scene and {@link #getViewTransform} to get the camera position.
	 *
	 * @param   screenX     x-value of the screen coordinate
	 * @param   screenY     y-value of the screen coordinate.
	 * @param   distance    distance between the world coordinate and the
	 *                      viewing plane.
	 *
	 * @return  World location for the given screen coordinate.
	 *
	 * @see     #getProjector
	 * @see     #getViewTransform
	 */
	private Vector3D screenToWorld( final int screenX , final int screenY , final double distance )
	{
		final Projector projector = getProjector();
		final Matrix3D viewTransform = getViewTransform();
		final Matrix3D inverseView = viewTransform.inverse();

		Vector3D linePoint = projector.screenToWorld( screenX , screenY , distance );
		linePoint = inverseView.multiply( linePoint );

		return linePoint;
	}

	/**
	 * Called when the mouse has been pressed on the component this translator
	 * is listening to. A new {@link MouseControlEvent} is created and
	 * dispatched to the {@link ControlEventQueue}.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 *
	 * @see     #createMouseControlEvent
	 */
	public void mousePressed( final MouseEvent e )
	{
		final MouseControlEvent event = createMouseControlEvent( e, _eventNumber, _mouseDragged );

		_eventQueue.dispatchEvent( event );
	}

	/**
	 * Called when the mouse has been released on the component this translator
	 * is listening to. A new {@link MouseControlEvent} is created and
	 * dispatched to the {@link ControlEventQueue}.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 *
	 * @see     #createMouseControlEvent
	 */
	public void mouseReleased( final MouseEvent e )
	{
		final MouseControlEvent event = createMouseControlEvent( e, _eventNumber, _mouseDragged );

		_eventQueue.dispatchEvent( event );

		final int mod = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK;
		if ( ( e.getModifiersEx() & mod ) == 0 )
		{
			_eventNumber++;
			_mouseDragged = false;
		}
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a
	 * component. Does nothing.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 */
	public void mouseClicked( final MouseEvent e ) { }

	/**
	 * Invoked when the mouse enters a component. Does nothing.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 */
	public void mouseEntered( final MouseEvent e ) { }

	/**
	 * Invoked when the mouse exits a component. Does nothing.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 */
	public void mouseExited( final MouseEvent e ) { }

	/**
	 * Called when the mouse has been dragged on the component this translator
	 * is listening to. A new {@link MouseControlEvent} is created and
	 * dispatched to the {@link ControlEventQueue}. <p>
	 * Note that, in order to improve performance, only one drag event every 100
	 * miliseconds is processed. Any others are discarded.
     *
     * @param   e   {@link MouseEvent} that was dispatched
	 *
	 * @see     #createMouseControlEvent
	 */
	public void mouseDragged( final MouseEvent e ) {
		final long timeStamp = e.getWhen();
		if ( timeStamp > _lastDrag + 100L )
		{
			final MouseControlEvent event = createMouseControlEvent( e, _eventNumber, true );

			_eventQueue.dispatchEvent( event );

			_mouseDragged = true;
			_lastDrag = timeStamp;
		}
	}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed. Does nothing.
     *
     * @param   e   {@link MouseEvent} that was dispatched
     */
	public void mouseMoved( final MouseEvent e ) { }

}


