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

/**
 * Provides OpenGL Shading Language (GLSL) shaders using the core API, available
 * in OpenGL 2.0 and above.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class CoreShaderImplementation
	implements ShaderImplementation
{
	public Shader createShader( final Shader.Type type )
	{
		return new CoreShader( type );
	}

	public ShaderProgram createProgram( final String name )
	{
		return new CoreShaderProgram( name );
	}
}
