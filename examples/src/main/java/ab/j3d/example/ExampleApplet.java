/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
 * @author G. Meinders
 */
public abstract class ExampleApplet
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
	 * Construct new instance.
	 */
	protected ExampleApplet()
	{
	}

	/**
	 * Creates and configures the render engine to be used.
	 *
	 * @return Configured render engine.
	 */
	protected abstract RenderEngine createEngine();

	/**
	 * Creates the (initial) 3D scene.
	 *
	 * @return Scene.
	 */
	protected abstract Scene createScene();

	/**
	 * Called when a view is created, to allow it to be configured.
	 *
	 * @param view View to be configured.
	 */
	protected abstract void configureView( View3D view );

	/**
	 * Called after each frame to allow for the scene and view to be updated.
	 *
	 * @param scene Scene to be updated.
	 * @param view  View to be updated.
	 *
	 * @return <code>true</code> if the view should be updated.
	 */
	protected abstract boolean animate( Scene scene, View3D view );

	@Override
	public void init()
	{
		_scene = createScene();
		_engine = createEngine();
	}

	@Override
	public void start()
	{
		final View3D view = _engine.createView( _scene );
		_view = view;

		view.addViewListener( new AnimationViewListener() );

		configureView( view );
		animate( _scene, view );

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
		public void beforeFrame( final View3D view )
		{
		}

		public void afterFrame( final View3D view )
		{
			if ( animate( _scene, view ) )
			{
				view.update();
			}
		}
	}
}
