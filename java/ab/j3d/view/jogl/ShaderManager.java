/* $Id$
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
package ab.j3d.view.jogl;

import java.io.*;
import java.util.*;
import javax.media.opengl.*;

import ab.j3d.appearance.*;
import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Handles the creation, modification and deletion of shader objects.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ShaderManager
{
	/**
	 * Shader implementation to be used.
	 */
	private ShaderImplementation _shaderImplementation;

	/**
	 * Shaders by name.
	 */
	private final Map<String, Shader> _shaders;

	/**
	 * Shader programs by name.
	 */
	private final Map<String, ShaderProgram> _shaderPrograms;

	/**
	 * Currently active shader program.
	 */
	private ShaderProgram _activeShaderProgram = null;

	/**
	 * Whether texturing is enabled.
	 */
	private boolean _textureEnabled = false;

	/**
	 * Whether lighting is enabled.
	 */
	private boolean _lightingEnabled = false;

	/**
	 * Whether shadow casting is enabled.
	 */
	private boolean _shadowsEnabled = false;

	/**
	 * Whether lighting is rendered in multiple passes, i.e. per-light.
	 */
	private boolean _multiPassLightingEnabled = false;

	/**
	 * Constructs a new shader manager.
	 *
	 * @param   shaderImplementation    Shader implementation to be used.
	 */
	public ShaderManager( final ShaderImplementation shaderImplementation )
	{
		_shaderImplementation = shaderImplementation;
		_shaders = new HashMap<String, Shader>();
		_shaderPrograms = new HashMap<String, ShaderProgram>();
	}

	/**
	 * Initializes the shaders and shader programs used.
	 *
	 * @throws  IOException if there was a problem reading the shader file.
	 */
	public void init()
		throws IOException
	{
		/*
		 * Load vertex and fragment shaders.
		 */
		register( "lighting-vertex", loadShader( Shader.Type.VERTEX, "lighting-vertex.glsl" ) );
		register( "lighting-fragment-single", loadShader( Shader.Type.FRAGMENT, "lighting-fragment.glsl" ) );
		register( "lighting-fragment-multi", loadShader( Shader.Type.FRAGMENT, "lighting-fragment.glsl", "#define MULTIPASS_LIGHTING" ) );
		register( "material-vertex", loadShader( Shader.Type.VERTEX, "material-vertex.glsl" ) );
		register( "material-fragment", loadShader( Shader.Type.FRAGMENT, "material-fragment.glsl" ) );

		register( "shadow-vertex", loadShader( Shader.Type.VERTEX, "shadow-vertex.glsl" ) );
		register( "shadow-fragment-disabled", loadShader( Shader.Type.FRAGMENT, "shadow-fragment-disabled.glsl" ) );
//		register( "shadow-fragment", loadShader( Shader.Type.FRAGMENT, "shadow-fragment.glsl" ) );
		register( "shadow-fragment", loadShader( Shader.Type.FRAGMENT, "shadow-fragment-multisample.glsl" ) );

		/*
		 * Generate 'main' functions for various shader usages.
		 */
		register( "unlit-vertex", createVertexShaderMain( "color", null ) );
		register( "unlit-fragment", createFragmentShaderMain( "color", null ) );
		register( "colored-vertex", createVertexShaderMain( "color", "lighting" ) );
		register( "colored-fragment", createFragmentShaderMain( "color", "lighting" ) );
		register( "textured-vertex", createVertexShaderMain( "texture", "lighting" ) );
		register( "textured-fragment", createFragmentShaderMain( "texture", "lighting" ) );

		/*
		 * Create distinct shader programs for the various kinds of rendering
		 * that occur during a single rendering pass.
		 */

		// No shadows at all.
		createShaderProgram( "unlit",
		                     "unlit-vertex", "unlit-fragment",
		                     "material-vertex", "material-fragment",
		                     "shadow-vertex", "shadow-fragment-disabled" );

		createShaderProgram( "colored",
		                     "colored-vertex", "colored-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-single",
		                     "shadow-vertex", "shadow-fragment-disabled" );

		createShaderProgram( "textured",
		                     "textured-vertex", "textured-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-single",
		                     "shadow-vertex", "shadow-fragment-disabled" );

		// No shadows right now, but still rendered with multi-pass lighting.
		createShaderProgram( "colored-multi",
		                     "colored-vertex", "colored-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-multi",
		                     "shadow-vertex", "shadow-fragment-disabled" );

		createShaderProgram( "textured-multi",
		                     "textured-vertex", "textured-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-multi",
		                     "shadow-vertex", "shadow-fragment-disabled" );

		// Shadows enabled, rendered with multi-pass lighting.
		createShaderProgram( "unlit-shadow",
		                     "unlit-vertex", "unlit-fragment",
		                     "material-vertex", "material-fragment",
		                     "shadow-vertex", "shadow-fragment" );

		createShaderProgram( "colored-shadow",
		                     "colored-vertex", "colored-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-multi",
		                     "shadow-vertex", "shadow-fragment" );

		createShaderProgram( "textured-shadow",
		                     "textured-vertex", "textured-fragment",
		                     "material-vertex", "material-fragment",
		                     "lighting-vertex", "lighting-fragment-multi",
		                     "shadow-vertex", "shadow-fragment" );
	}

	/**
	 * Enables rendering using shaders.
	 */
	public void enable()
	{
		setupShaders();
		useShader( getShaderProgram() );
	}

	/**
	 * Disables rendering using shaders.
	 */
	public void disable()
	{
		useShader( null );
	}

	/**
	 * Creates a shader program.
	 *
	 * @param   name        Name that identifies the shader program.
	 * @param   shaders     Names of shaders to be attached.
	 */
	public void createShaderProgram( final String name, final String... shaders )
	{
		final ShaderImplementation shaderImplementation = _shaderImplementation;
		if ( shaderImplementation != null )
		{
			final ShaderProgram shaderProgram = shaderImplementation.createProgram( name );
			for ( final String shader : shaders )
			{
				shaderProgram.attach( getShader( shader ) );
			}
			_shaderPrograms.put( name, shaderProgram );
		}
	}

	/**
	 * Sets whether lighting is enabled.
	 *
	 * @param   lightingEnabled     <code>true</code> to enable lighting.
	 */
	public void setLightingEnabled( final boolean lightingEnabled )
	{
		_lightingEnabled = lightingEnabled;
		update();
	}

	/**
	 * Sets whether textures are enabled.
	 *
	 * @param   textureEnabled  <code>true</code> to enable textures.
	 */
	public void setTextureEnabled( final boolean textureEnabled )
	{
		_textureEnabled = textureEnabled;
		update();
	}

	/**
	 * Sets whether shadows are enabled.
	 *
	 * @param   shadowsEnabled  <code>true</code> to enable shadows.
	 */
	public void setShadowsEnabled( final boolean shadowsEnabled )
	{
		_shadowsEnabled = shadowsEnabled;
		update();
	}

	/**
	 * Sets whether multi-pass lighting is enabled.
	 *
	 * @param   multiPassLightingEnabled    <code>true</code> to enable it.
	 */
	public void setMultiPassLightingEnabled( final boolean multiPassLightingEnabled )
	{
		_multiPassLightingEnabled = multiPassLightingEnabled;
		update();
	}

	/**
	 * Sets the reflectivity properties of the currently active shader.
	 *
	 * @param   reflectionMap       Specifies reflection properties.
	 */
	public void setReflectivity( @NotNull final ReflectionMap reflectionMap )
	{
		setReflectivity( reflectionMap.getReflectivityMin(), reflectionMap.getReflectivityMax(), reflectionMap.getIntensityRed(), reflectionMap.getIntensityGreen(), reflectionMap.getIntensityBlue() );
	}

	/**
	 * Sets the reflectivity properties of the currently active shader.
	 *
	 * @param   reflectionMin       Reflectivity perpendicular to face normals.
	 * @param   reflectionMax       Reflectivity parallel to face normals.
	 * @param   reflectionRed       Red intensity of reflections.
	 * @param   reflectionGreen     Green intensity of reflections.
	 * @param   reflectionBlue      Blue intensity of reflections.
	 */
	public void setReflectivity( final float reflectionMin, final float reflectionMax, final float reflectionRed, final float reflectionGreen, final float reflectionBlue )
	{
		final ShaderProgram active = _activeShaderProgram;
		if ( active != null )
		{
			active.setUniform( "reflectionMin", reflectionMin );
			active.setUniform( "reflectionMax", reflectionMax );
			active.setUniform( "reflectionColor", reflectionRed, reflectionGreen, reflectionBlue );
		}
	}

	/**
	 * Called when a property is changed to update the currently active shader.
	 */
	private void update()
	{
		if ( _activeShaderProgram != null )
		{
			useShader( getShaderProgram() );
		}
	}

	/**
	 * Returns the shader program to be used for rendering, based on the current
	 * properties set on the shader manager.
	 *
	 * @return  Shader program.
	 */
	@Nullable
	private ShaderProgram getShaderProgram()
	{
		final ShaderProgram result;

		final boolean shadowsEnabled = _shadowsEnabled;
		if ( _lightingEnabled )
		{
			final boolean multiPassLightingEnabled = _multiPassLightingEnabled;
			if ( _textureEnabled )
			{
				result = getShaderProgram( shadowsEnabled ? "textured-shadow" :
				                           multiPassLightingEnabled ? "textured-multi" : "textured" );
			}
			else
			{
				result = getShaderProgram( shadowsEnabled ? "colored-shadow" :
				                           multiPassLightingEnabled ? "colored-multi" : "colored" );
			}
		}
		else
		{
			result = getShaderProgram( shadowsEnabled ? "unlit-shadow" : "unlit" );
		}

		return result;
	}

	/**
	 * Returns the shader program with the given name.
	 *
	 * @param   name    Name that identifies the shader program.
	 *
	 * @return  Shader program.
	 */
	@Nullable
	public ShaderProgram getShaderProgram( final String name )
	{
		return _shaderPrograms.get( name );
	}

	/**
	 * Returns the shader with the given name.
	 *
	 * @param   name    Name of the shader.
	 *
	 * @return  Shader object.
	 */
	private Shader getShader( final String name )
	{
		final Shader result = _shaders.get( name );
		if ( result == null )
		{
			throw new NoSuchElementException( "Unknown shader: " + name );
		}

		return result;
	}

	/**
	 * Registers the given shader, such that it can be used to create a
	 * shader program.
	 *
	 * @param   name    Name that identifies the shader.
	 * @param   shader  Shader object.
	 *
	 * @see     #createShaderProgram(String, String...)
	 */
	public void register( final String name, final Shader shader )
	{
		_shaders.put( name, shader );
	}

	/**
	 * Loads a shader of the specified type.
	 *
	 * @param   shaderType      Type of shader.
	 * @param   name            Name of the resource to be loaded.
	 * @param   prefixLines     Shader program lines to be prepended, if any.
	 *
	 * @return  Loaded shader.
	 *
	 * @throws  GLException if compilation of the shader fails.
	 * @throws  IOException if there was a problem reading the shader file.
	 */
	@NotNull
	public Shader loadShader( final Shader.Type shaderType, final String name, final String... prefixLines )
		throws IOException
	{
		final ShaderImplementation shaderImplementation = _shaderImplementation;
		if ( shaderImplementation == null )
		{
			throw new IllegalStateException( "Must have shader implementation to be able to load any shaders" );
		}

		final Class<?> clazz = JOGLRenderer.class;
		final InputStream sourceIn = clazz.getResourceAsStream( name );
		if ( sourceIn == null )
		{
			throw new FileNotFoundException( name );
		}

		final String source = TextTools.loadText( sourceIn );

		final Shader result = shaderImplementation.createShader( shaderType );
		if ( prefixLines.length == 0 )
		{
			result.setSource( source );
		}
		else
		{
			final String[] prefixedSource = new String[ prefixLines.length + 1 ];
			for ( int i = 0 ; i < prefixLines.length ; i++ )
			{
				final String prefixLine = prefixLines[ i ];
				prefixedSource[ i ] = prefixLine + '\n';
			}
			prefixedSource[ prefixLines.length ] = source;
			result.setSource( prefixedSource );
		}

		return result;
	}

	/**
	 * Releases all OpenGL resources allocated by the shader manager.
	 */
	public void dispose()
	{
		for ( final ShaderProgram shaderProgram : _shaderPrograms.values() )
		{
			shaderProgram.dispose();
		}
		_shaderPrograms.clear();

		for ( final Shader shader : _shaders.values() )
		{
			shader.dispose();
		}
		_shaders.clear();

		useShader( null );
	}

	/**
	 * Creates a vertex shader providing the main method for rendering with the
	 * specified settings.
	 *
	 * @param   colorFunction       Name of the color function, defined in
	 *                              another vertex shader.
	 * @param   lightingFunction    Name of the lighting function, defined
	 *                              in another vertex shader;
	 *                              <code>null</code> to use no lighting.
	 *
	 * @return  Created vertex shader.
	 */
	@Nullable
	private Shader createVertexShaderMain( final String colorFunction, final String lightingFunction )
	{
		Shader result = null;

		final ShaderImplementation shaderImplementation = _shaderImplementation;
		if ( shaderImplementation != null )
		{
			final StringBuilder source = new StringBuilder();

			source.append( "void " );
			source.append( colorFunction );
			source.append( "();" );

			source.append( "void shadow();" );

			if ( lightingFunction != null )
			{
				source.append( "void " );
				source.append( lightingFunction );
				source.append( "();" );
			}

			source.append( "void main()" );
			source.append( '{' );

			source.append( "gl_Position = ftransform();" );
			source.append( colorFunction );
			source.append( "();" );

			if ( lightingFunction != null )
			{
				source.append( lightingFunction );
				source.append( "();" );
			}

			source.append( "shadow();" );

			source.append( '}' );

			result = shaderImplementation.createShader( Shader.Type.VERTEX );
			result.setSource( source.toString() );
		}

		return result;
	}

	/**
	 * Creates a fragment shader providing the main method for rendering with
	 * the specified settings.
	 *
	 * @param   colorFunction           Name of the color function, defined in
	 *                                  another fragment shader.
	 * @param   lightingFunction        Name of the lighting function, defined
	 *                                  in another fragment shader;
	 *                                  <code>null</code> to use no lighting.
	 *
	 * @return  Created vertex shader.
	 */
	@Nullable
	private Shader createFragmentShaderMain( final String colorFunction, final String lightingFunction )
	{
		Shader result = null;

		final ShaderImplementation shaderImplementation = _shaderImplementation;
		if ( shaderImplementation != null )
		{
			final StringBuilder source = new StringBuilder();

			source.append( "vec4 " );
			source.append( colorFunction );
			source.append( "();" );

			if ( lightingFunction != null )
			{
				source.append( "vec4 " );
				source.append( lightingFunction );
				source.append( "( in vec4 color );" );
			}

			source.append( "void main()" );
			source.append( '{' );

			source.append( "gl_FragColor = " );
			if ( lightingFunction != null )
			{
				source.append( lightingFunction );
				source.append( "( " );
				source.append( colorFunction );
				source.append( "() );" );
			}
			else
			{
				source.append( colorFunction );
				source.append( "();" );
			}
			// TODO: Check if this works well with multi-pass lighting. Could end up too bright.
			source.append( "gl_FragColor.rgb += gl_FrontMaterial.emission.rgb * gl_FragColor.a;" );

			source.append( '}' );

			result = shaderImplementation.createShader( Shader.Type.FRAGMENT );
			result.setSource( source.toString() );
		}

		return result;
	}

	/**
	 * Enables the given shader program, replacing the current one.
	 *
	 * @param   shaderProgram   Shader program to be used; <code>null</code> to
	 *                          use OpenGL's fixed functionality instead.
	 */
	private void useShader( final ShaderProgram shaderProgram )
	{
		final ShaderProgram active = _activeShaderProgram;
		if ( active != shaderProgram )
		{
			_activeShaderProgram = shaderProgram;

			try
			{
				if ( active != null )
				{
					active.disable();
				}

				if ( shaderProgram != null )
				{
					shaderProgram.enable();
				}
			}
			catch ( GLException e )
			{
				e.printStackTrace();
				disableShaders();
			}
		}
	}

	/**
	 * Permanently disables shaders, e.g. because of an error while compiling
	 * or linking a shader program.
	 */
	public void disableShaders()
	{
		System.out.println( "ShaderManager: Disabling shaders. (" + _shaderImplementation + ')' );
		dispose();
		_shaderImplementation = null;
	}

	/**
	 * Returns whether shader support is available. Shaders may not be available
	 * when the target hardware or driver doesn't support them or when shaders
	 * have been permanently disabled using {@link #disableShaders()}.
	 *
	 * @return  <code>true</code> if shaders are enabled.
	 */
	public boolean isShaderSupportAvailable()
	{
		return ( _shaderImplementation != null );
	}

	/**
	 * Sets uniform variables of GLSL shaders, specifying e.g. the texture units
	 * to be used.
	 */
	private void setupShaders()
	{
		/*
		 * Set shader program properties.
		 */
		for ( final ShaderProgram shaderProgram : _shaderPrograms.values() )
		{
			shaderProgram.enable();
			shaderProgram.setUniform( "colorMap", JOGLRenderer.TEXTURE_UNIT_COLOR - GL.GL_TEXTURE0 );
			shaderProgram.setUniform( "bumpMap", JOGLRenderer.TEXTURE_UNIT_BUMP - GL.GL_TEXTURE0 );
			shaderProgram.setUniform( "reflectionMap", JOGLRenderer.TEXTURE_UNIT_ENVIRONMENT - GL.GL_TEXTURE0 );
			shaderProgram.setUniform( "shadowMap", JOGLRenderer.TEXTURE_UNIT_SHADOW - GL.GL_TEXTURE0 );

			shaderProgram.disable();
			shaderProgram.validate();
		}
	}
}
