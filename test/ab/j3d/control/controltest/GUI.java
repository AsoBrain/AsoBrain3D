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
package ab.j3d.control.controltest;

import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;

import ab.j3d.control.Control;
import ab.j3d.control.controltest.model.Model;

/**
 * The GUI mamanges the GUI for this test application. Among other things it
 * creates the {@link JFrame}, the {@link Model3D}, and four {@link View3D}s on
 * that 3d model. It also registers a number of {@link Control}s to these views,
 * so that the user can manipulate the {@link Model}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class GUI
{

	/**
	 * Construct new GUI.
	 *
	 * @param   main    The main application that created this class.
	 */
	public GUI( final ControlTest main )
	{

		final Model3D model3D = new Model3D( main );

		final JFrame frame = new JFrame( "ControlTest" );
		final Container contentPane = frame.getContentPane();
		contentPane.setLayout( new BorderLayout() );

		final View3D view1 = new View3D( model3D , View3D.TOP_VIEW         , View3D.PERSPECTIVE_PROJECTION );
		final View3D view2 = new View3D( model3D , View3D.FRONT_VIEW       , View3D.PERSPECTIVE_PROJECTION );
		final View3D view3 = new View3D( model3D , View3D.LEFT_VIEW        , View3D.PERSPECTIVE_PROJECTION );
		final View3D view4 = new View3D( model3D , View3D.PERSPECTIVE_VIEW , View3D.PERSPECTIVE_PROJECTION );

		final JSplitPane topSplit      = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , view1.getComponent() , view2.getComponent() );
		final JSplitPane bottomSplit   = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , view3.getComponent() , view4.getComponent() );
		final JSplitPane verticalSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT   , topSplit              , bottomSplit         );
		contentPane.add( verticalSplit , BorderLayout.CENTER );

		final JToolBar toolBar = createToolBar();
		contentPane.add( toolBar , BorderLayout.NORTH );

		final SelectionControl selectionControl = new SelectionControl( main );
		view1.addControl( selectionControl );
		view2.addControl( selectionControl );
		view3.addControl( selectionControl );
		view4.addControl( selectionControl );

		frame.setBounds( 200 , 0 , 1200 , 1000 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible( true );

		topSplit.setDividerLocation( 0.5 );
		bottomSplit.setDividerLocation( 0.5 );
		verticalSplit.setDividerLocation( 0.5 );
	}

	/**
	 * Creates the {@link JToolBar} for this application.
	 *
	 * @return  {@link JToolBar} for this application.
	 */
	private static JToolBar createToolBar()
	{
		final JToolBar result = new JToolBar( "Actions" );
		final ButtonGroup group = new ButtonGroup();

		final JToggleButton moveButton = new JToggleButton( "Move object" );
		group.add( moveButton );
		result.add( moveButton );

		final JToggleButton rotateButton = new JToggleButton( "Rotate object" );
		group.add( rotateButton );
		result.add( rotateButton );

		final JToggleButton resizeButton = new JToggleButton( "Resize object" );
		group.add( resizeButton );
		result.add( resizeButton );

		result.addSeparator();

		final JToggleButton rotateCameraButton = new JToggleButton( "Rotate camera" );
		group.add( rotateCameraButton );
		result.add( rotateCameraButton );

		final JToggleButton moveCameraButton = new JToggleButton( "Move camera" );
		group.add( moveCameraButton );
		result.add( moveCameraButton );

		final JToggleButton zoomCameraButton = new JToggleButton( "Zoom camera" );
		group.add( zoomCameraButton );
		result.add( zoomCameraButton );

		rotateCameraButton.setSelected( true );

		return result;
	}
}
