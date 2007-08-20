/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.JPanel;

import junit.framework.TestCase;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewModel;

import com.numdata.oss.event.EventDispatcher;
import com.numdata.oss.event.EventFilter;

/**
 * This class tests the {@link ComponentControlInput} class.
 *
 * @see     ComponentControlInput
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class TestComponentControlInput
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestComponentControlInput.class.getName();

	/**
	 * Creates a new Object3D in a plane shape, with a top and a bottom face.
	 *
	 * @param   size    The size of the plane
	 *
	 * @return  The plane object
	 */
	public static Object3D createPlane( final Object tag , final double size )
	{
		final double halfSize = size / 2.0;
		final Vector3D lf = Vector3D.INIT.set( -halfSize , -halfSize , 0.0 );
		final Vector3D rf = Vector3D.INIT.set(  halfSize , -halfSize , 0.0 );
		final Vector3D rb = Vector3D.INIT.set(  halfSize ,  halfSize , 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -halfSize ,  halfSize , 0.0 );

		final Material red   = new Material( Color.RED  .getRGB() );
		final Material green = new Material( Color.GREEN.getRGB() );

		final Object3D result = new Object3D();
		result.setTag( tag );

		/* top    */result.addFace( new Vector3D[]{ lf , lb , rb , rf } , red   , false , false ); // Z =  size
		/* bottom */result.addFace( new Vector3D[]{ lb , lf , rf , rb } , green , false , false ); // Z = -size

		return result;
	}

	/**
	 * Test the {@link ComponentControlInput#mousePressed} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testMousePressed()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testMousePressed()" );

		final ControlTestInput input = new ControlTestInput();

		final Object3D plane1 = createPlane( "Plane 1" , 100.0 );
		final Object3D plane2 = createPlane( "Plane 2" , 100.0 );

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0   );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0  , 0.0 , 0.0 , 0.0 , 0.0 , -75.0 );

		final Node3DCollection<Object3D> scene = input.getScene();
		scene.add( transform1 , plane1 );
		scene.add( transform2 , plane2 );

		int modifiers = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
		MouseEvent e = new MouseEvent( new JPanel() , MouseEvent.MOUSE_PRESSED , 0L , modifiers , 0 , 0 , 1 , false , MouseEvent.BUTTON1 );
		input.mousePressed( e );

		assertTrue("The last event is not a MouseControlEvent", input.getLastEvent() instanceof ControlInputEvent );
		ControlInputEvent event = (ControlInputEvent)input.getLastEvent();

		assertEquals( "The mouse button is not 1" , MouseEvent.BUTTON1 , event.getMouseButton() );
		assertEquals( "The event type should be MOUSE_PRESSED" , MouseEvent.MOUSE_PRESSED , event.getID() );
		final int lastNumber = event.getSequenceNumber();


		modifiers = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		e = new MouseEvent( new JPanel() , MouseEvent.MOUSE_PRESSED , 0L , modifiers , 50 , 50 , 1 , false , MouseEvent.BUTTON2 );
		input.mousePressed( e );

		assertTrue( "The last event is not a MouseControlEvent" , input.getLastEvent() instanceof ControlInputEvent );
		event = (ControlInputEvent)input.getLastEvent();

		assertEquals( "The mouse button is not 2" , MouseEvent.BUTTON2 , event.getMouseButton() );
		assertEquals( "The event type should be MOUSE_PRESSED" , MouseEvent.MOUSE_PRESSED , event.getID() );
		assertEquals( "The event number has changed" , lastNumber , event.getSequenceNumber() );
	}

	/**
	 * Test the {@link ComponentControlInput#mouseReleased} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testMouseReleased()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testMouseReleased()" );

		final ControlTestInput input = new ControlTestInput();

		final Object3D plane1 = createPlane( "Plane 1" , 100.0 );
		final Object3D plane2 = createPlane( "Plane 2" , 100.0 );

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0   );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0  , 0.0 , 0.0 , 0.0 , 0.0 , -75.0 );

		final Node3DCollection<Object3D> scene = input.getScene();
		scene.add( transform1 , plane1 );
		scene.add( transform2 , plane2 );

		int modifiers = MouseEvent.SHIFT_DOWN_MASK;
		MouseEvent e = new MouseEvent( new JPanel() , MouseEvent.MOUSE_RELEASED , 0L , modifiers , 0 , 0 , 1 , false , MouseEvent.BUTTON1 );
		input.mouseReleased( e );

		assertTrue("The last event is not a MouseControlEvent" , input.getLastEvent() instanceof ControlInputEvent );
		ControlInputEvent event = (ControlInputEvent)input.getLastEvent();

		assertEquals( "The mouse button is not 1" , MouseEvent.BUTTON1 , event.getMouseButton() );
		assertEquals( "The event type should be MOUSE_RELEASED" , MouseEvent.MOUSE_RELEASED , event.getID() );
		final int lastNumber = event.getSequenceNumber();

		modifiers = MouseEvent.CTRL_DOWN_MASK;
		e = new MouseEvent( new JPanel() , MouseEvent.MOUSE_RELEASED , 0L , modifiers , 0 , 0 , 1 , false , MouseEvent.BUTTON3 );
		input.mouseReleased( e );

		assertTrue( "The last event is not a MouseControlEvent" , input.getLastEvent() instanceof ControlInputEvent );
		event = (ControlInputEvent)input.getLastEvent();

		assertEquals( "The mouse button is not 3" , MouseEvent.BUTTON3 , event.getMouseButton() );
		assertEquals( "The event type should be MOUSE_RELEASED" , MouseEvent.MOUSE_RELEASED , event.getID() );
		assertEquals( "The event number has not increased" , lastNumber + 1 , event.getSequenceNumber() );
	}

	/**
	 * This inner class implements {@link ControlInput} for testing.
	 */
	public static class ControlTestInput
		extends ComponentControlInput
	{
		/**
		  Static scene;
		 */
		private final Node3DCollection<Object3D> _scene;

		/**
		 * Last event that was handled.
		 */
		private EventObject _lastEvent;

		/**
		 * Create {@link ControlTestInput} with default scene.
		 */
		public ControlTestInput()
		{
			super( new JPanel() );

			_scene = new Node3DCollection();

			_lastEvent = null;
			final EventDispatcher eventQueue = getEventDispatcher();
			eventQueue.appendFilter( new EventFilter()
				{
					public EventObject filterEvent( final EventObject event )
					{
						_lastEvent = event;
						return event;
					}
				} );
		}

		protected Object getIDForObject( final Object3D object )
		{
			return object.getTag();
		}

		protected Projector getProjector()
		{
			return Projector.createInstance( Projector.PERSPECTIVE , 100 , 100 , 1.0 , ViewModel.M , 10.0 , 1000.0 , Math.toRadians( 45.0 ) , 1.0 );
		}

		protected Matrix3D getViewTransform()
		{
			return Matrix3D.INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , -500.0 );
		}

		protected Node3DCollection<Object3D> getScene()
		{
			return _scene;
		}

		/**
		 * Get last event that was handled.
		 *
		 * @return  Last event that was handled.
		 */
		public EventObject getLastEvent()
		{
			return _lastEvent;
		}
	}
}
