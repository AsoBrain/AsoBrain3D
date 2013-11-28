/* $Id$
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

import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * Provides loading and caching of textures for JOGL-based rendering.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TextureCache
{
	/**
	 * Pool number for the next new factory.
	 */
	private static final AtomicInteger THREAD_FACTORY_POOL_NUMBER = new AtomicInteger( 1 );

	/**
	 * Texture cache key for the normalization cube map, used for DOT3 bump
	 * mapping.
	 */
	public static final String NORMALIZATION_CUBE_MAP = "__normalizationCubeMap";

	/**
	 * Cached textures, mapped by arbitrary key objects.
	 */
	private final Map<Object, TextureProxy> _textures;

	/**
	 * Used to load texture data asynchronously.
	 */
	private final ExecutorService _executorService;

	/**
	 * Set of textures, by key object, with an alpha channel.
	 */
	private final Set<Object> _alpha;

	/**
	 * Maximum allowed texture size.
	 */
	private int _maximumTextureSize = 0;

	/**
	 * Whether non-power-of-two textures are supported.
	 */
	private boolean _nonPowerOfTwo = false;

	/**
	 * Whether OpenGL 1.2 support is available.
	 */
	private boolean _isOpenGL12 = false;

	/**
	 * Event listeners.
	 */
	private final List<TextureCacheListener> _listeners;

	/**
	 * Construct new texture cache.
	 */
	public TextureCache()
	{
		_textures = new HashMap<Object, TextureProxy>();
		_alpha = new HashSet<Object>();
		_executorService = Executors.newSingleThreadExecutor( new DaemonThreadFactory() );

		_listeners = new ArrayList<TextureCacheListener>();
	}

	/**
	 * Notifies the texture cache that the GL context was (re)initialized.
	 * Any previously created texture objects are no longer valid.
	 */
	public void init()
	{
		/*
		 * Determine requirements for textures.
		 */
		final GL gl = GLU.getCurrentGL();

		final int[] maxTextureSizeBuffer = new int[ 1 ];
		gl.glGetIntegerv( GL.GL_MAX_TEXTURE_SIZE , maxTextureSizeBuffer , 0 );

		_maximumTextureSize = Math.max( 64 , maxTextureSizeBuffer[ 0 ] );
		_nonPowerOfTwo = gl.isExtensionAvailable( "GL_ARB_texture_non_power_of_two" );
		_isOpenGL12 = gl.isExtensionAvailable( "GL_VERSION_1_2" );

		/*
		 * Remove references to textures that are no longer valid.
		 */
		final Set<Map.Entry<Object, TextureProxy>> textureEntries = _textures.entrySet();
		for ( final Iterator<Map.Entry<Object, TextureProxy>> i = textureEntries.iterator() ; i.hasNext() ; )
		{
			final Map.Entry<Object, TextureProxy> entry = i.next();
			final TextureProxy textureProxy = entry.getValue();

			/*
			 * If texture data is still available, a new texture can be created.
			 * Otherwise, the proxy is of no use anymore.
			 */
			if ( !textureProxy.isTextureDataSet() )
			{
				i.remove();
				_alpha.remove( entry.getKey() );
			}
		}
	}

	/**
	 * Disposes the texture cache, releasing any resources it uses.
	 */
	public void dispose()
	{
		final ExecutorService executorService = _executorService;
		try
		{
			AccessController.doPrivileged( new PrivilegedAction<Object>()
			{
				public Object run()
				{
					return executorService.shutdownNow();
				}
			} );

			executorService.awaitTermination( 10L, TimeUnit.SECONDS );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( SecurityException e )
		{
			/*
			 * Thrown when modifying threads (to shutdown the executor service)
			 * is not allowed. Should be caught, e.g. to avoid an error dialog
			 * when called from an applet.
			 */
			e.printStackTrace();
		}
	}

	/**
	 * Returns the maximum texture size.
	 *
	 * @return  Maximum texture size.
	 */
	public int getMaximumTextureSize()
	{
		return _maximumTextureSize;
	}

	/**
	 * Returns whether non-power-of-two textures are supported.
	 *
	 * @return  <code>true</code> if non-power-of-two textures are supported.
	 */
	public boolean isNonPowerOfTwo()
	{
		return _nonPowerOfTwo;
	}

	/**
	 * Returns whether OpenGL 1.2 support is available.
	 *
	 * @return  <code>true</code> if OpenGL 1.2 support is available.
	 */
	public boolean isOpenGL12()
	{
		return _isOpenGL12;
	}

	/**
	 * Returns whether the specified texture has an alpha channel.
	 *
	 * @param   texture     Name of the texture.
	 *
	 * @return  <code>true</code> if the texture has an alpha channel.
	 */
	public boolean hasAlpha( final TextureMap texture )
	{
		return _alpha.contains( texture );
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   textureMap  Texture map.
	 *
	 * @return  Texture for the specified name;
	 *          <code>null</code> if the texture is not available (or not yet).
	 */
	@Nullable
	public Texture getTexture( @NotNull final TextureMap textureMap )
	{
		final Map<Object,TextureProxy> textures = _textures;

		TextureProxy textureProxy = textures.get( textureMap );
		if ( textureProxy == null )
		{
			textureProxy = new TextureProxy( textureMap, this );
			loadTexture( textureMap, textureProxy );
		}

		final Texture result = textureProxy.getTexture();

		if ( result != null )
		{
			final TextureData textureData = textureProxy.getTextureData();
			if ( ( textureData != null ) && ( textureData.getInternalFormat() == GL.GL_RGBA ) )
			{
				_alpha.add( textureMap );
			}
		}

		return result;
	}

	/**
	 * Get {@link Texture} for color map of {@link Appearance}.
	 *
	 * @param   appearance  Appearance to get color map texture from.
	 *
	 * @return  Color map texture; <code>null</code> if face has no color map or
	 *          no texture coordinates.
	 */
	@Nullable
	public Texture getColorMapTexture( final Appearance appearance )
	{
		Texture result = null;

		if ( appearance != null )
		{
			final TextureMap colorMap = appearance.getColorMap();
			if ( colorMap != null )
			{
				result = getTexture( colorMap );
			}
		}

		return result;
	}

	/**
	 * Get {@link Texture} for bump map of {@link Appearance}.
	 *
	 * @param   appearance  Appearance  to get bump map texture from.
	 *
	 * @return  Color map texture; <code>null</code> if face has no color map or
	 *          no texture coordinates.
	 */
	@Nullable
	public Texture getBumpMapTexture( final Appearance appearance )
	{
		Texture result = null;

		if ( appearance != null )
		{
			final TextureMap bumpMap = appearance.getBumpMap();
			if ( bumpMap != null )
			{
				TextureProxy textureProxy = _textures.get( bumpMap );
				if ( textureProxy == null )
				{
					textureProxy = new BumpTextureProxy( bumpMap, this );
					loadTexture( bumpMap, textureProxy );
				}

				result = textureProxy.getTexture();
			}
		}

		return result;
	}

	/**
	 * Get normalization cube map, used to perform DOT3 bump mapping. For each
	 * 3D texture coordinate, the value of the map represents the normalized
	 * vector from the origin in the direction of the coordinate.
	 *
	 * @return  Normalization cube map.
	 */
	@Nullable
	public Texture getNormalizationCubeMap()
	{
		TextureProxy result = _textures.get( NORMALIZATION_CUBE_MAP );
		if ( result == null )
		{
			result = new TextureProxy( JOGLTools.createNormalizationCubeMap( GLU.getCurrentGL() ) );
			_textures.put( NORMALIZATION_CUBE_MAP , result );
		}
		return result.getTexture();
	}

	/**
	 * Returns a cube map texture for the given cube map.
	 *
	 * @param   cubeMap     Cube map.
	 *
	 * @return  Cube map texture.
	 */
	@Nullable
	public Texture getCubeMap( final CubeMap cubeMap )
	{
		TextureProxy textureProxy = _textures.get( cubeMap );
		if ( textureProxy == null )
		{
			textureProxy = new CubeTextureProxy( cubeMap, this );
			loadTexture( cubeMap, textureProxy );
		}

		return textureProxy.getTexture();
	}

	/**
	 * Asynchronously loads the given texture.
	 *
	 * @param   key             Key identifying the texture.
	 * @param   textureProxy    Texture to be loaded.
	 */
	private void loadTexture( @NotNull final Object key , @NotNull final TextureProxy textureProxy )
	{
		_textures.put( key, textureProxy );

		final Future<TextureData> textureData = _executorService.submit( textureProxy );
		textureProxy.setTextureData( textureData );

		_executorService.submit( new Runnable()
		{
			public void run()
			{
				fireTextureChange( textureProxy );
			}
		} );
	}

	/**
	 * Adds an event listener to the texture cache.
	 *
	 * @param   listener    Listener to be added.
	 */
	public void addListener( @NotNull final TextureCacheListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes an event listener to the texture cache.
	 *
	 * @param   listener    Listener to be removed.
	 */
	public void removeListener( @NotNull final TextureCacheListener listener )
	{
		_listeners.remove( listener );
	}

	/**
	 * Notifies registered event listeners of a change in the given texture.
	 *
	 * @param   textureProxy    Changed texture.
	 */
	protected void fireTextureChange( @NotNull final TextureProxy textureProxy )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				for ( final TextureCacheListener listener : _listeners )
				{
					listener.textureChanged( TextureCache.this , textureProxy );
				}
			}
		} );
	}

	/**
	 * Thread factory used to create worker threads to load textures. Unlike the
	 * default factory, this creates daemon threads so they will not prevent the
	 * runtime from stopping.
	 */
	private static class DaemonThreadFactory
		implements ThreadFactory
	{
		/**
		 * Thread group for new threads.
		 */
		private final ThreadGroup _group;

		/**
		 * Pool number for this factory.
		 */
		private final int _poolNumber;

		/**
		 * Thread number for the next new thread.
		 */
		private final AtomicInteger _threadNumber = new AtomicInteger( 1 );

		/**
		 * Constructs a new thread factory.
		 */
		private DaemonThreadFactory()
		{
			final SecurityManager securityManager = System.getSecurityManager();
			if ( securityManager != null )
			{
				_group = securityManager.getThreadGroup();
			}
			else
			{
				final Thread currentThread = Thread.currentThread();
				_group = currentThread.getThreadGroup();
			}

			_poolNumber = THREAD_FACTORY_POOL_NUMBER.getAndIncrement();
		}

		public Thread newThread( final Runnable runnable )
		{
			final Thread result = new Thread( _group, runnable, "TextureCache-" + _poolNumber + "-thread-" + _threadNumber.getAndIncrement(), 0L );
			result.setDaemon( true );
			result.setPriority( Thread.NORM_PRIORITY );
			return result;
		}
	}
}
