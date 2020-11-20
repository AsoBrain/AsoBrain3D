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

import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * Interface for examples that can be run by {@link ExampleApp} or
 * {@link ExampleApplet}.
 *
 * @author Gerrit Meinders
 */
public interface Example
{
	/**
	 * Creates and configures the render engine to be used.
	 *
	 * @return Configured render engine.
	 */
	RenderEngine createEngine();

	/**
	 * Creates the (initial) 3D scene.
	 *
	 * @return Scene.
	 */
	Scene createScene();

	/**
	 * Called when a view is created, to allow it to be configured.
	 *
	 * @param view View to be configured.
	 */
	void configureView( View3D view );

	/**
	 * Called after each frame to allow for the scene and view to be updated.
	 *
	 * @param scene Scene to be updated.
	 * @param view  View to be updated.
	 *
	 * @return <code>true</code> if the view should be updated.
	 */
	boolean animate( Scene scene, View3D view );
}
