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
 * Represents an OpenGL Shading Language (GLSL) fragment shader.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class FragmentShader
	extends Shader
{
	/**
	 * Constructs a new fragment shader.
	 */
	public FragmentShader()
	{
		super( GL.GL_FRAGMENT_SHADER );
	}
}
