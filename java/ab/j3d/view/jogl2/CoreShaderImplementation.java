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
package ab.j3d.view.jogl2;

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
