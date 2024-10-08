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

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Represents an OpenGL Shading Language (GLSL) shader program.
 *
 * @author Gerrit Meinders
 */
public interface ShaderProgram
{
	/**
	 * Attaches the given shader to the program. The shader will be included
	 * when the program is next linked.
	 *
	 * @param   shader  Shader to be attached.
	 */
	void attach( Shader shader );

	/**
	 * Detaches the given shader from the program. The shader will no longer be
	 * included when the program is next linked.
	 *
	 * @param   shader  Shader to be detached.
	 */
	void detach( Shader shader );

	/**
	 * Links the currently attached shaders into an executable program.
	 *
	 * @throws  RuntimeException if the shaders fail to link.
	 */
	void link();

	/**
	 * Validates the program.
	 *
	 * @throws  RuntimeException if the program fails to validate.
	 */
	void validate();

	/**
	 * Returns the program information log. The log may contain helpful
	 * information about problems encountered during compilation or validation.
	 *
	 * @return  Program information log.
	 */
	@Nullable
	String getInfoLog();

	/**
	 * Activates the shader program, replacing OpenGL's fixed functionality.
	 * Note that at most one shader program can be active at any given time.
	 * If another shader program is currently active, it's implicitly
	 * deactivated.
	 *
	 * <p>
	 * If necessary, the program is automatically linked.
	 */
	void enable();

	/**
	 * Deactivates the shader program, reverting back to OpenGL's fixed
	 * functionality.
	 */
	void disable();

	/**
	 * Deletes the resources used by the shader program object. The shader
	 * objects that the program consists of remain unaffected.
	 */
	void dispose();

	/**
	 * Sets the uniform variable identified by the given name. If no variable
	 * exists with the given name, no action is taken and exception is thrown.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 */
	void setUniform( String identifier , float value );

	/**
	 * Sets the uniform variable identified by the given name. If no variable
	 * exists with the given name, no action is taken and exception is thrown.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 */
	void setUniform( String identifier , int value );

	/**
	 * Sets the uniform variable identified by the given name. If no variable
	 * exists with the given name, no action is taken and exception is thrown.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 */
	void setUniform( String identifier , boolean value );

	/**
	 * Sets the uniform variable identified by the given name. If no variable
	 * exists with the given name, no action is taken and exception is thrown.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   value       Value to be set.
	 */
	void setUniform( String identifier , Vector3D value );

	/**
	 * Sets the uniform variable identified by the given name. If no variable
	 * exists with the given name, no action is taken and exception is thrown.
	 *
	 * @param   identifier  Name of the variable.
	 * @param   x           First element of the vector.
	 * @param   y           Second element of the vector.
	 * @param   z           Third element of the vector.
	 */
	void setUniform( String identifier, float x, float y, float z );
}
