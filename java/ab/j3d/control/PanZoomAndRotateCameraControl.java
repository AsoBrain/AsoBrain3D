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
 * ====================================================================
 */
package ab.j3d.control;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.view.*;

/**
 * This class implements a camera control that allows only camera panning and
 * zooming.
 * <p />
 * Panning is achieved by modifying the translational compnents of the view
 * matrix ({@link View3D#setScene2View}, while the zoom effect is
 * achieved by manipulating the view's zoom factor ({@link View3D#setZoomFactor}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class PanZoomAndRotateCameraControl
	extends PanAndZoomCameraControl
{
	/**
	 * Create pan and zoom camera control.
	 *
	 * @param   view    View to be controlled.
	 */
	public PanZoomAndRotateCameraControl( final View3D view )
	{
		super( view );
	}

	@Override
	public void mouseDragged( final ControlInputEvent event )
	{
		if ( isCaptured() )
		{
			if ( event.isMouseButton1Down() )
			{
				rotateDrag( event );
			}
		}
	}

	/**
	 * Moved the 'from' point around the 'to' point.
	 *
	 * @param   event   Drag event.
	 */
	protected void rotateDrag( final ControlInputEvent event )
	{
		System.out.println( "------[rotateDrag]---------------------------------------------" );
		final View3D view = _view;
		final Component viewComponent = view.getComponent();

//		final Vector3D from     = _dragStartFrom;
//		final Vector3D to       = _dragStartTo;
//		final Vector3D up       = _dragStartUp;
		final Matrix3D scene2view = _dragStartScene2View;
		System.out.println( "scene2view = " + scene2view.toFriendlyString() );
		final Vector3D from = scene2view.inverseRotate( -scene2view.xo, -scene2view.yo, -scene2view.zo );
		System.out.println( "from = " + from.toFriendlyString() );
		final Vector3D viewDirection = new Vector3D( -scene2view.zx,  -scene2view.zy,  -scene2view.zz );
		System.out.println( "viewDirection = " + viewDirection );

		final double viewSize;
		{
			final double pixels2units = view.getPixelsToUnitsFactor();
			final int componentWidth = viewComponent.getWidth();
			final int componentHeight = viewComponent.getHeight();
			final double viewWidth = (double)componentWidth * pixels2units;
			final double viewHeight = (double)componentHeight * pixels2units;
			viewSize = Math.max( viewWidth, viewHeight );
			System.out.println( "component size = " + componentWidth + " x " + componentHeight );
			System.out.println( " => pixels2units = " + pixels2units );
			System.out.println( "     => view size = " + viewWidth + " x " + viewHeight );
			System.out.println( "          => view size = " + viewSize );
		}

		final double fieldOfView = view.getFieldOfView();
		System.out.println( "fieldOfView = " + Math.toDegrees( fieldOfView ) + "degrees" );
		final double distance2width = 2.0 * Math.tan( 0.5 * fieldOfView );
		System.out.println( "  => distance2width = " + distance2width );
		final double eyeDistance = viewSize / distance2width;
		System.out.println( "      => eyeDistance = " + eyeDistance );
		final Vector3D eyePosition = new Vector3D( from.x - eyeDistance * viewDirection.x, from.y - eyeDistance * viewDirection.y, from.z - eyeDistance * viewDirection.z );
		final Vector3D targetPosition = new Vector3D( from.x + eyeDistance * viewDirection.x, from.y + eyeDistance * viewDirection.y, from.z + eyeDistance * viewDirection.z );
		System.out.println( "          => eyePosition = " + eyePosition );
		System.out.println( "             targetPosition = " + targetPosition );

		final double toRadians = view.getPixelsToRadiansFactor();
		final double deltaX = -toRadians * (double)event.getDragDeltaX();
		final double deltaY = toRadians * (double)event.getDragDeltaY();
		System.out.println( "drag amount:" + Math.toDegrees( deltaX ) );
		System.out.println( "  deltaX = DEG " + Math.toDegrees( deltaX ) );
		System.out.println( "  deltaY = DEG " + Math.toDegrees( deltaY ) );


		final Matrix3D orientation     = Matrix3D.getRotationTransform( new Vector3D( 0.0, 0.0, eyeDistance ), Vector3D.POSITIVE_Y_AXIS, deltaX );
		System.out.println( "orientation = " + orientation );

		final Matrix3D view2scene = orientation.multiply( scene2view.inverse() );
		System.out.println( "* NEW * scene2view = " + Matrix3D.toFriendlyString( view2scene.inverse() ) );
		view.setScene2View( view2scene.inverse() );

//		final Vector3D orientedUp      = orientation.transform( up );
//
//		Vector3D delta = from.minus( to );
//		delta = orientation.transform( delta );
//
//		Vector3D elevationAxis = Vector3D.cross( delta , orientedUp );
//		elevationAxis = elevationAxis.normalize();
//
//		final Matrix3D elevation = Matrix3D.getRotationTransform( Vector3D.INIT , elevationAxis , deltaY );
//		final Vector3D newUp  = elevation.transform( orientedUp );
//
//		Vector3D newFrom = elevation.transform( delta );
//		newFrom = newFrom.plus( to );
//
//		look( newFrom , to , newUp );
	}
}
