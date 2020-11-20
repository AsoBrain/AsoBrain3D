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
import javax.swing.*;

import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * Applet with a single 3D view, used as a base class for (most) examples.
 *
 * @author Gerrit Meinders
 */
public class ExampleApplet
extends JApplet
{
	/**
	 * Rendering engine being used.
	 */
	private RenderEngine _engine = null;

	/**
	 * 3D scene being rendered.
	 */
	private Scene _scene = null;

	/**
	 * 3D view of the scene.
	 */
	private View3D _view = null;

	/**
	 * Example to run.
	 */
	private final Example _example;

	/**
	 * Construct new instance.
	 *
	 * @param example Example to run.
	 */
	public ExampleApplet( final Example example )
	{
		_example = example;
	}

	@Override
	public void init()
	{
		_scene = _example.createScene();
		_engine = _example.createEngine();
	}

	@Override
	public void start()
	{
		final View3D view = _engine.createView( _scene );
		_view = view;

		view.addViewListener( new AnimationViewListener() );

		_example.configureView( view );
		_example.animate( _scene, view );

		setLayout( new BorderLayout() );
		add( view.getComponent(), BorderLayout.CENTER );
	}

	@Override
	public void stop()
	{
		removeAll();
		_view.dispose();
		_view = null;
	}

	@Override
	public void destroy()
	{
		_engine = null;
		_scene = null;
	}

	/**
	 * Lets the view render continuously and updates the camera every frame.
	 */
	protected class AnimationViewListener
	implements ViewListener
	{
		@Override
		public void beforeFrame( final View3D view )
		{
		}

		@Override
		public void afterFrame( final View3D view )
		{
			if ( _example.animate( _scene, view ) )
			{
				view.update();
			}
		}
	}
}
