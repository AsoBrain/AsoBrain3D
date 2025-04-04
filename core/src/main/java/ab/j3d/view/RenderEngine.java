/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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

import ab.j3d.model.*;

/**
 * This class provides functionality to create {@link Scene} views.
 *
 * @author Peter S. Heijnen
 */
public interface RenderEngine
{
	/**
	 * Create a new view for the specified scene. Please call {@link
	 * View3D#dispose()} if you no longer use the created view.
	 *
	 * @param scene Scene to view.
	 *
	 * @return View that was created.
	 */
	View3D createView( Scene scene );

	/**
	 * Create a new offscreen view for the specified scene. Please call {@link
	 * View3D#dispose()} if you no longer use the created view.
	 *
	 * @param scene Scene to view.
	 *
	 * @return View that was created.
	 *
	 * @throws UnsupportedOperationException if offscreen rendering is not
	 * supported by this engine.
	 */
	OffscreenView3D createOffscreenView( Scene scene );

	/**
	 * Disposes the render engine, releasing any resources it uses.
	 */
	void dispose();
}
