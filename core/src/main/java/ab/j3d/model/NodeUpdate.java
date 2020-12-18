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
package ab.j3d.model;

/**
 * <p>Encapsulates an change to the scene. The change is split into two
 * phases: {@link #prepare()} and {@link #update(Scene)}.
 *
 * <p>During the preparation phase, tasks are performed to minimize the
 * duration of the update phase. These tasks may be performed asynchronously
 * on another thread.
 *
 * <p>During the update phase, modifications can be made to the scene.
 * To prevent inconsistencies, this phase is performed on the EDT.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface NodeUpdate
{
	/**
	 * Makes preparations for a subsequent {@link #update(Scene)} call.
	 * The scene and any nodes in the scene must not be affected.
	 */
	default void prepare()
	{
	}

	/**
	 * Updates the scene.
	 *
	 * @param   scene   Scene to be updated.
	 */
	void update( Scene scene );
}
