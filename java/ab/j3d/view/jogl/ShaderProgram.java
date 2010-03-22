/* ====================================================================
 * $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
package ab.j3d.view.jogl;

import javax.media.opengl.GLException;

import ab.j3d.Vector3D;

/**
 * Represents an OpenGL Shading Language (GLSL) shader program.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ShaderProgram
{
	/**
	 * Returns the underlying program object.
	 *
	 * @return  Program object.
	 */
	int getProgramObject();

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
	 * @throws GLException if the shaders fail to link.
	 */
	void link();

	/**
	 * Validates the program.
	 *
	 * @throws  GLException if the program fails to validate.
	 */
	void validate();

	/**
	 * Returns the program information log. The log may contain helpful
	 * information about problems encountered during compilation or validation.
	 *
	 * @return  Program information log.
	 */
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
	void setUniform( String identifier , Vector3D value );
}
