/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import javax.swing.JPanel;

import junit.framework.TestCase;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ComponentControlInput;
import ab.j3d.control.ControlInput;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.geom.BasicRay3D;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Object3D;
import ab.j3d.view.java2d.Java2dModel;

import com.numdata.oss.event.EventDispatcher;
import com.numdata.oss.event.EventFilter;

/**
 * This class tests the {@link ViewControlInput} class.
 *
 * @see     ViewControlInput
 *
 * @author  G.B.M. Rupert
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class TestViewControlInput
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestViewControlInput.class.getName();

	/**
	 * Creates a new Object3D in a plane shape, with a top and a bottom face.
	 *
	 * @param   size    The size of the plane
	 *
	 * @return  The plane object
	 */
	public static Object3D createPlane( final double size )
	{
		final double halfSize = size / 2.0;
		final Vector3D lf = Vector3D.INIT.set( -halfSize , -halfSize , 0.0 );
		final Vector3D rf = Vector3D.INIT.set(  halfSize , -halfSize , 0.0 );
		final Vector3D rb = Vector3D.INIT.set(  halfSize ,  halfSize , 0.0 );
		final Vector3D lb = Vector3D.INIT.set( -halfSize ,  halfSize , 0.0 );

		final Material red   = new Material( Color.RED  .getRGB() );
		final Material green = new Material( Color.GREEN.getRGB() );

		final Object3D result = new Object3D();

		/* top    */result.addFace( new Vector3D[]{ lf , lb , rb , rf } , red   , false , false ); // Z =  size
		/* bottom */result.addFace( new Vector3D[]{ lb , lf , rf , rb } , green , false , false ); // Z = -size

		return result;
	}

	/**
	 * Test the {@link ControlInput#getIntersections} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testGetIntersections()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetIntersections()" );

		/*
		 * Create test scene.
		 */
		final Object3D plane1 = createPlane( 100.0 );
		final Object3D plane2 = createPlane( 100.0 );
		final Object3D plane3 = createPlane( 100.0 );
		final Object3D plane4 = createPlane( 100.0 );
		final Object3D plane5 = createPlane( 100.0 );
		final Object3D plane6 = createPlane( 100.0 );
		final Object3D plane7 = createPlane( 100.0 );

		final Matrix3D transform1 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,    0.0 ,   0.0 , 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,   10.0 ,  -1.0 , 0.0 );
		final Matrix3D transform3 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,  -10.0 , -20.0 , 0.0 );
		final Matrix3D transform4 = Matrix3D.getTransform(  45.0 , 90.0 , 0.0 , -150.0 ,   0.0 , 0.0 );
		final Matrix3D transform5 = Matrix3D.getTransform( -45.0 , 90.0 , 0.0 , -150.0 ,   0.0 , 0.0 );
		final Matrix3D transform6 = Matrix3D.getTransform(  90.0 ,  0.0 , 0.0 ,  150.0 ,   0.0 , 0.0 );
		final Matrix3D transform7 = Matrix3D.getTransform(   0.0 , 90.0 , 0.0 ,  150.0 ,   0.0 , 0.0 );

		final Java2dModel model = new Java2dModel( ViewModel.MM );
		model.createNode( "Plane 1" , transform1 , plane1 , null , 0.0f );
		model.createNode( "Plane 2" , transform2 , plane2 , null , 0.0f );
		model.createNode( "Plane 3" , transform3 , plane3 , null , 0.0f );
		model.createNode( "Plane 4" , transform4 , plane4 , null , 0.0f );
		model.createNode( "Plane 5" , transform5 , plane5 , null , 0.0f );
		model.createNode( "Plane 6" , transform6 , plane6 , null , 0.0f );
		model.createNode( "Plane 7" , transform7 , plane7 , null , 0.0f );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( model , model.createView( "view" ) );

		List<Face3DIntersection> selection;

		selection = input.getIntersections( new BasicRay3D( -45.0 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 3" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 1" , selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 45.0 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 2" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 1" , selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 0.0 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 3 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 3" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 2" , selection.get( 1 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 1" , selection.get( 2 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -125.0 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 4" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 5" , selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -149.9 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 4" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 5" , selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -150.0 , -500.0 , 0.0 , 0.0 , 1.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );

		selection = input.getIntersections( new BasicRay3D( 200.0 , -25.0 , 0.0 , -Math.sqrt( 0.5 ) , Math.sqrt( 0.5 ) , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 2 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 6" , selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1" , "Plane 7" , selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 100.0 , 0.0 , -25.0 , 1.0 , 0.0 , 0.0, true ) );
		assertEquals( "The number of intersected faces is not 2, but " + selection.size() , 1 , selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0" , "Plane 7" , selection.get( 0 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 100.0 , 0.0 , -100.0 , 1.0 , 0.0 , 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 0 , selection.size() );

		selection = input.getIntersections( new BasicRay3D( -60.1  , 0.0 , -500.0, 0.0 , 0.0 , 1.0, true ) );
		assertEquals( "Incorrect number of intersected faces;" , 0 , selection.size() );
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

		/*
		 * Create test scene.
		 */
		final Object3D plane1 = createPlane( 100.0 );
		final Object3D plane2 = createPlane( 100.0 );

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0   );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0  , 0.0 , 0.0 , 0.0 , 0.0 , -75.0 );

		final Java2dModel model = new Java2dModel( ViewModel.MM );
		model.createNode( "Plane 1" , transform1 , plane1 , null , 0.0f );
		model.createNode( "Plane 2" , transform2 , plane2 , null , 0.0f );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( model , model.createView( "view" ) );

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

		/*
		 * Create test scene.
		 */
		final Object3D plane1 = createPlane( 100.0 );
		final Object3D plane2 = createPlane( 100.0 );

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0   );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0  , 0.0 , 0.0 , 0.0 , 0.0 , -75.0 );

		final Java2dModel model = new Java2dModel( ViewModel.MM );
		model.createNode( "Plane 1" , transform1 , plane1 , null , 0.0f );
		model.createNode( "Plane 2" , transform2 , plane2 , null , 0.0f );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( model , model.createView( "view" ) );

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
	public static class ViewControlTestInput
		extends ViewControlInput
	{
		/**
		 * Last event that was handled.
		 */
		private EventObject _lastEvent;

		/**
		 * Create view control input used in tests.
		 */
		public ViewControlTestInput( final ViewModel viewModel , final ViewModelView view )
		{
			super( viewModel , view );

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

		protected Projector getProjector()
		{
			return Projector.createInstance( Projector.PERSPECTIVE , 100 , 100 , 1.0 , ViewModel.M , 10.0 , 1000.0 , Math.toRadians( 45.0 ) , 1.0 );
		}

		protected Matrix3D getViewTransform()
		{
			return Matrix3D.INIT.set( 1.0 , 0.0 , 0.0 , 0.0 , 0.0 , 0.0 , 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , -500.0 );
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