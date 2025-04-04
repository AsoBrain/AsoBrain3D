/* ====================================================================
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import org.jetbrains.annotations.*;

/**
 * Represents a specific API for using OpenGL Shading Language (GLSL) shaders.
 * GLSL is supported in the OpenGL core since version 2.0, but for older OpenGL
 * versions, extensions need to be used for GLSL support.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ShaderImplementation
{
	/**
	 * Creates a shader object of the given type.
	 *
	 * @param   type    Type of shader.
	 *
	 * @return  Shader object.
	 */
	@NotNull
	Shader createShader( @NotNull Shader.Type type );

	/**
	 * Creates a shader program.
	 *
	 * @param   name    Name of the program, for display in log and error
	 *                  messages.
	 *
	 * @return  Shader program.
	 */
	@NotNull
	ShaderProgram createProgram( @Nullable String name );
}
