/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

import javax.media.opengl.GLException;

/**
 * Represents an OpenGL Shading Language (GLSL) shader object.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface Shader
{
	/**
	 * Type of shader.
	 */
	enum Type
	{
		/** Vertex shader.   */ VERTEX ,
		/** Fragment shader. */ FRAGMENT
	}

	/**
	 * Returns the underlying shader object.
	 *
	 * @return  Shader object.
	 */
	int getShaderObject();

	/**
	 * Sets the source code of the shader, replacing any previously set code.
	 * The source code is automatically compiled.
	 *
	 * @param   source  Source code of the shader.
	 *
	 * @throws GLException if compilation of the source code fails.
	 */
	void setSource( String... source );

	/**
	 * Deletes the resources used by the shader object.
	 */
	void dispose();
}
