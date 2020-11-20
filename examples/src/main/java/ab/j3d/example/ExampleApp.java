/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
package ab.j3d.example;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * App for running an {@link Example}.
 *
 * @author Gerrit Meinders
 */
public class ExampleApp
implements Runnable
{
	/**
	 * Example to run.
	 */
	private final Example _example;

	/**
	 * Construct new instance.
	 *
	 * @param example Example to run.
	 */
	public ExampleApp( final Example example )
	{
		_example = example;
	}

	@Override
	public void run()
	{
		final Example example = _example;
		final Scene scene = example.createScene();
		final View3D view = example.createEngine().createView( scene );

		view.addViewListener( new ViewListener()
		{
			@Override
			public void beforeFrame( final View3D view )
			{
			}

			@Override
			public void afterFrame( final View3D view )
			{
				if ( example.animate( scene, view ) )
				{
					view.update();
				}
			}
		} );

		example.configureView( view );

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setLayout( new BorderLayout() );
		frame.add( view.getComponent(), BorderLayout.CENTER );
		frame.setSize( 1024, 768 );
		frame.setVisible( true );

		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowOpened( final WindowEvent e )
			{
				example.animate( scene, view );
			}

			@Override
			public void windowClosed( final WindowEvent e )
			{
				view.dispose();
			}
		} );
	}
}
