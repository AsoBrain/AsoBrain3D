/*
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
 */
package ab.j3d.view;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.java2d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import junit.framework.*;

/**
 * This class tests the {@link ViewControlInput} class.
 *
 * @author G.B.M. Rupert
 * @author Mart Slot
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
	 * @param size The size of the plane
	 *
	 * @return The plane object
	 */
	private static Object3D createPlane( final double size )
	{
		final double halfSize = size / 2.0;
		final Vector3D lf = new Vector3D( -halfSize, -halfSize, 0.0 );
		final Vector3D rf = new Vector3D( halfSize, -halfSize, 0.0 );
		final Vector3D rb = new Vector3D( halfSize, halfSize, 0.0 );
		final Vector3D lb = new Vector3D( -halfSize, halfSize, 0.0 );

		final BasicAppearance red = new BasicAppearance();
		red.setAmbientColor( Color4.RED );
		red.setDiffuseColor( Color4.RED );
		red.setSpecularColor( Color4.WHITE );
		red.setShininess( 16 );

		final BasicAppearance green = new BasicAppearance();
		green.setAmbientColor( Color4.GREEN );
		green.setDiffuseColor( Color4.GREEN );
		green.setSpecularColor( Color4.WHITE );
		green.setShininess( 16 );

		final Object3DBuilder builder = new Object3DBuilder();
		/* top    */
		builder.addFace( new Vector3D[] { lf, lb, rb, rf }, red, false, false ); // Z =  size
		/* bottom */
		builder.addFace( new Vector3D[] { lb, lf, rf, rb }, green, false, false ); // Z = -size
		return builder.getObject3D();
	}

	/**
	 * Test the {@link ViewControlInput#getIntersections} method.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testGetIntersections()
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

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform( 90.0, 0.0, 0.0, 10.0, -1.0, 0.0 );
		final Matrix3D transform3 = Matrix3D.getTransform( 90.0, 0.0, 0.0, -10.0, -20.0, 0.0 );
		final Matrix3D transform4 = Matrix3D.getTransform( 45.0, 90.0, 0.0, -150.0, 0.0, 0.0 );
		final Matrix3D transform5 = Matrix3D.getTransform( -45.0, 90.0, 0.0, -150.0, 0.0, 0.0 );
		final Matrix3D transform6 = Matrix3D.getTransform( 90.0, 0.0, 0.0, 150.0, 0.0, 0.0 );
		final Matrix3D transform7 = Matrix3D.getTransform( 0.0, 90.0, 0.0, 150.0, 0.0, 0.0 );

		final Scene scene = new Scene( Scene.MM );
		scene.addContentNode( "Plane 1", transform1, plane1 );
		scene.addContentNode( "Plane 2", transform2, plane2 );
		scene.addContentNode( "Plane 3", transform3, plane3 );
		scene.addContentNode( "Plane 4", transform4, plane4 );
		scene.addContentNode( "Plane 5", transform5, plane5 );
		scene.addContentNode( "Plane 6", transform6, plane6 );
		scene.addContentNode( "Plane 7", transform7, plane7 );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( new Java2dView( scene, null ) );

		List<Face3DIntersection> selection;

		selection = input.getIntersections( new BasicRay3D( -45.0, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 3", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 1", selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 45.0, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 2", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 1", selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 0.0, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 3, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 3", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 2", selection.get( 1 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 1", selection.get( 2 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -125.0, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 4", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 5", selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -149.9, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 4", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 5", selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( -150.0, -500.0, 0.0, 0.0, 1.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );

		selection = input.getIntersections( new BasicRay3D( 200.0, -25.0, 0.0, -Math.sqrt( 0.5 ), Math.sqrt( 0.5 ), 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 2, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 6", selection.get( 0 ).getObjectID() );
		assertEquals( "The planes are not listed from front to back at pos #1", "Plane 7", selection.get( 1 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 100.0, 0.0, -25.0, 1.0, 0.0, 0.0, true ) );
		assertEquals( "The number of intersected faces is not 2, but " + selection.size(), 1, selection.size() );
		assertEquals( "The planes are not listed from front to back at pos #0", "Plane 7", selection.get( 0 ).getObjectID() );

		selection = input.getIntersections( new BasicRay3D( 100.0, 0.0, -100.0, 1.0, 0.0, 0.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 0, selection.size() );

		selection = input.getIntersections( new BasicRay3D( -60.1, 0.0, -500.0, 0.0, 0.0, 1.0, true ) );
		assertEquals( "Incorrect number of intersected faces;", 0, selection.size() );
	}

	/**
	 * Test the {@link ViewControlInput#mousePressed} method.
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

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0, 0.0, 0.0, 0.0, 0.0, -75.0 );

		final Scene scene = new Scene( Scene.MM );
		scene.addContentNode( "Plane 1", transform1, plane1 );
		scene.addContentNode( "Plane 2", transform2, plane2 );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( new Java2dView( scene, null ) );

		int modifiers = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
		MouseEvent e = new MouseEvent( new JPanel(), MouseEvent.MOUSE_PRESSED, 0L, modifiers, 0, 0, 1, false, MouseEvent.BUTTON1 );
		input.mousePressed( e );

		assertTrue( "The last event is not a MouseControlEvent", input.getLastEvent() instanceof ControlInputEvent );
		ControlInputEvent event = (ControlInputEvent)input.getLastEvent();
		assertSame( "Should get event back", e, event.getInputEvent() );

		final int lastNumber = event.getSequenceNumber();

		modifiers = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		e = new MouseEvent( new JPanel(), MouseEvent.MOUSE_PRESSED, 0L, modifiers, 50, 50, 1, false, MouseEvent.BUTTON2 );
		input.mousePressed( e );

		assertTrue( "The last event is not a MouseControlEvent", input.getLastEvent() instanceof ControlInputEvent );
		event = (ControlInputEvent)input.getLastEvent();

		assertSame( "Should get event back", e, event.getInputEvent() );

		assertEquals( "The event number has changed", lastNumber, event.getSequenceNumber() );
	}

	/**
	 * Test the {@link ViewControlInput#mouseReleased} method.
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

		final Matrix3D transform1 = Matrix3D.getTransform( 90.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
		final Matrix3D transform2 = Matrix3D.getTransform( 0.0, 0.0, 0.0, 0.0, 0.0, -75.0 );

		final Scene scene = new Scene( Scene.MM );
		scene.addContentNode( "Plane 1", transform1, plane1 );
		scene.addContentNode( "Plane 2", transform2, plane2 );

		/*
		 * Perform tests.
		 */
		final ViewControlTestInput input = new ViewControlTestInput( new Java2dView( scene, null ) );

		int modifiers = MouseEvent.SHIFT_DOWN_MASK;
		MouseEvent e = new MouseEvent( new JPanel(), MouseEvent.MOUSE_RELEASED, 0L, modifiers, 0, 0, 1, false, MouseEvent.BUTTON1 );
		input.mouseReleased( e );

		assertTrue( "The last event is not a MouseControlEvent", input.getLastEvent() instanceof ControlInputEvent );
		ControlInputEvent event = (ControlInputEvent)input.getLastEvent();

		assertSame( "Should get event back", e, event.getInputEvent() );
		final int lastNumber = event.getSequenceNumber();

		modifiers = MouseEvent.CTRL_DOWN_MASK;
		e = new MouseEvent( new JPanel(), MouseEvent.MOUSE_RELEASED, 0L, modifiers, 0, 0, 1, false, MouseEvent.BUTTON3 );
		input.mouseReleased( e );

		assertTrue( "The last event is not a MouseControlEvent", input.getLastEvent() instanceof ControlInputEvent );
		event = (ControlInputEvent)input.getLastEvent();

		assertSame( "Should get event back", e, event.getInputEvent() );
		assertEquals( "The event number has not increased", lastNumber + 1, event.getSequenceNumber() );
	}

	/**
	 * This inner class implements {@link ViewControlInput} for testing.
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
		 *
		 * @param view {@link View3D} whose input events to monitor.
		 */
		public ViewControlTestInput( final View3D view )
		{
			super( view );

			_lastEvent = null;

			addControlInputListener( new ControlInputListener()
			{
				@Override
				public void inputReceived( final ControlInputEvent event )
				{
					_lastEvent = event;
				}
			} );
		}

		@Override
		public Projector getProjector()
		{
			return Projector.createInstance( ProjectionPolicy.PERSPECTIVE, 100, 100, 1.0, Scene.M, 10.0, 1000.0, Math.toRadians( 45.0 ), 1.0 );
		}

		@Override
		public Matrix3D getScene2View()
		{
			return new Matrix3D( 1.0, 0.0, 0.0, 0.0,
			                     0.0, 0.0, 1.0, 0.0,
			                     0.0, -1.0, 0.0, -500.0 );
		}

		@Override
		public Matrix3D getView2Scene()
		{
			return new Matrix3D( 1.0, 0.0, 0.0, 0.0,
			                     0.0, 0.0, -1.0, -500.0,
			                     0.0, 1.0, 0.0, 0.0 );
		}

		/**
		 * Get last event that was handled.
		 *
		 * @return Last event that was handled.
		 */
		public EventObject getLastEvent()
		{
			return _lastEvent;
		}
	}
}
