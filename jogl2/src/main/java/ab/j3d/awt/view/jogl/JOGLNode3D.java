/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import ab.j3d.model.*;
import com.jogamp.opengl.*;

/**
 * Implementation-specific scene graph node for the {@link JOGLEngine}.
 *
 * @author Gerrit Meinders
 */
public abstract class JOGLNode3D
extends Node3D
{
	/**
	 * Constructs a new instance.
	 */
	protected JOGLNode3D()
	{
	}

	/**
	 * Renders the node. Any OpenGL state changes that are not performed using
	 * the given state helper must be reverted.
	 *
	 * @param gl            OpenGL interface.
	 * @param state         State helper to use.
	 * @param shaderManager Shader manager.
	 */
	public abstract void render( GL gl, GLStateHelper state, ShaderManager shaderManager );
}
