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
package ab.j3d.view;

/**
 * Common interface for 3D views that use a JOGL-based rendering engine. This
 * allows some of the advanced features supported by this engine to be used
 * without creating a direct dependency on a specific JOGL version.
 *
 * @author Gerrit Meinders
 */
public interface JOGLViewInterface
{
	/**
	 * Returns the view's rendering configuration.
	 *
	 * @return Rendering configuration.
	 */
	JOGLConfiguration getConfiguration();
}
