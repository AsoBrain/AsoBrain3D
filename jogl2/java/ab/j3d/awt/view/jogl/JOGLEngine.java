/* ====================================================================
 *  $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import javax.media.opengl.*;

import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * JOGL render engine implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLEngine
	implements RenderEngine
{
	/**
	 * Shared OpenGL rendering context.
	 */
	private GLContext _context = null;

	/**
	 * Texture cache.
	 */
	private final TextureCache _textureCache;

	/**
	 * Configuration settings.
	 */
	private JOGLConfiguration _configuration;

	/**
	 * Number of registered views. All views may share the same context.
	 */
	private int _registeredViewCount;

	/**
	 * Provides a shared off-screen GL context.
	 */
	private GLAutoDrawable _sharedDrawable;

	/**
	 * Construct new JOGL render engine.
	 */
	public JOGLEngine()
	{
		this( new JOGLConfiguration() );
	}

	/**
	 * Construct new JOGL render engine.
	 *
	 * @param   configuration   Configuration settings.
	 */
	public JOGLEngine( final JOGLConfiguration configuration )
	{
		_configuration = configuration;
		_textureCache = new TextureCache();
	}

	public void dispose()
	{
		_textureCache.dispose();
		if ( _sharedDrawable != null )
		{
			_sharedDrawable.destroy();
		}
	}

	/**
	 * Returns the configuration settings to be used.
	 *
	 * @return  Configuration.
	 */
	public JOGLConfiguration getConfiguration()
	{
		return _configuration;
	}

	public View3D createView( final Scene scene )
	{
		synchronized ( this )
		{
			return new JOGLView( this, scene );
		}
	}

	/**
	 * Returns a shared off-screen drawable which provides the master GL context.
	 * This is needed to ensure that the master GL context is not disposed before
	 * any of the slave contexts that depend on it.
	 *
	 * <p>For details, see <a href="http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/GLSharedContextSetter.html#lifecycle">GLSharedContextSetter:
	 * Lifecycle Considerations</a>.
	 *
	 * @param profile      Profile to use.
	 * @param capabilities Needed capabilities.
	 *
	 * @return Shared drawable.
	 */
	GLAutoDrawable getSharedAutoDrawable( final GLProfile profile, final GLCapabilities capabilities )
	{
		GLAutoDrawable sharedDrawable = _sharedDrawable;

		if ( sharedDrawable == null )
		{
			// GLProfile and GLCapabilities should be equal across all shared GL drawable/context.
			final boolean createNewDevice = true; // use 'own' display device!
			sharedDrawable = GLDrawableFactory.getFactory( profile ).createDummyAutoDrawable( null, createNewDevice, capabilities, null );
			sharedDrawable.display(); // triggers GLContext object creation and native realization.

			_sharedDrawable = sharedDrawable;
		}

		return sharedDrawable;
	}

	/**
	 * Returns the model's texture cache.
	 *
	 * @return  Texture cache.
	 */
	TextureCache getTextureCache()
	{
		return _textureCache;
	}

	/**
	 * Registers a view that uses the engine's shared context.
	 */
	void registerView()
	{
		_registeredViewCount++;
	}

	/**
	 * Unregisters a view that uses the engine's shared context.
	 */
	void unregisterView()
	{
		if ( _registeredViewCount == 0 )
		{
			throw new IllegalStateException( "No registered views." );
		}
		_registeredViewCount--;
	}

	/**
	 * Returns the number of registered views.
	 *
	 * @return  Number of registered views.
	 */
	int getRegisteredViewCount()
	{
		return _registeredViewCount;
	}
}
