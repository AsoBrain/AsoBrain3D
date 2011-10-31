/* $Id$
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
package ab.j3d.control.controltest;

import java.awt.*;
import javax.swing.*;

import ab.j3d.control.*;
import ab.j3d.control.controltest.model.*;

/**
 * The GUI manages the GUI for this test application. Among other things it
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
	 * @param   model   {@link Model} for which to create the {@link Model3D},
	 *                  the {@link View3D}s and {@link Control}s.
	 */
	public GUI( final Model model )
	{
		final Model3D model3D = new Model3D( model );

		final SelectionControl selectionControl = new SelectionControl( model );
		final MoveControl      moveControl      = new MoveControl( model );

		final View3D view1 = new View3D( model3D, View3D.TOP_VIEW, View3D.PARALLEL_PROJECTION );
		view1.insertControl( selectionControl );
		view1.insertControl( moveControl );

		final View3D view2 = new View3D( model3D, View3D.FRONT_VIEW, View3D.PARALLEL_PROJECTION );
		view2.insertControl( selectionControl );
		view2.insertControl( moveControl );

		final View3D view3 = new View3D( model3D, View3D.LEFT_VIEW, View3D.PARALLEL_PROJECTION );
		view3.insertControl( selectionControl );
		view3.insertControl( moveControl );

		final View3D view4 = new View3D( model3D, View3D.PERSPECTIVE_VIEW, View3D.PERSPECTIVE_PROJECTION );
		view4.insertControl( selectionControl );
		view4.insertControl( moveControl );

		final JSplitPane topSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, view1.getComponent(), view2.getComponent() );
		final JSplitPane bottomSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, view3.getComponent(), view4.getComponent() );
		final JSplitPane verticalSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT, topSplit, bottomSplit );

		final Container contentPane = new JPanel( new BorderLayout() );
		contentPane.add( verticalSplit, BorderLayout.CENTER );

		final JFrame frame = new JFrame( "ControlTest" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane( contentPane );
		final Toolkit toolkit = frame.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		frame.setBounds( screenBounds.x + screenInsets.left + 300 / 2, screenBounds.y + screenInsets.top + 100 / 2, screenBounds.width - screenInsets.left - screenInsets.right - 300 , screenBounds.height - screenInsets.top - screenInsets.bottom - 100 );
		frame.setVisible( true );

		topSplit.setDividerLocation( 0.5 );
		bottomSplit.setDividerLocation( 0.5 );
		verticalSplit.setDividerLocation( 0.5 );
	}
}
