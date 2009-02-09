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

import javax.media.opengl.GL;

/**
 * Represents an OpenGL Shading Language (GLSL) vertex shader.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class VertexShader
	extends Shader
{
	/**
	 * Constructs a new vertex shader.
	 */
	public VertexShader()
	{
		super( GL.GL_VERTEX_SHADER );
	}
}
