/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2010
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

import java.util.*;
import java.util.concurrent.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

import ab.j3d.*;
import com.numdata.oss.*;
import com.sun.opengl.util.texture.*;
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
	 * Texture cache key for the normalization cube map, used for DOT3 bump
	 * mapping.
	 */
	public static final String NORMALIZATION_CUBE_MAP = "__normalizationCubeMap";

	/**
	 * Cached textures, by name.
	 */
	private final Map<String, TextureProxy> _textures;

	/**
	 * Used to load texture data asynchronously.
	 */
	private final ExecutorService _executorService;

	/**
	 * Set of textures, by name, with an alpha channel.
	 */
	private final Set<String> _alpha;

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
		Thread.dumpStack();

		_textures = new HashMap<String, TextureProxy>();
		_alpha = new HashSet<String>();

		final DefaultThreadFactory threadFactory = new DefaultThreadFactory();
		threadFactory.setNamePrefix( TextureCache.class.getName() );
		threadFactory.setDaemon( true );
		_executorService = Executors.newSingleThreadExecutor( threadFactory );

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
		final Set<Map.Entry<String, TextureProxy>> textureEntries = _textures.entrySet();
		for ( final Iterator<Map.Entry<String, TextureProxy>> i = textureEntries.iterator() ; i.hasNext() ; )
		{
			final Map.Entry<String, TextureProxy> entry = i.next();
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
		executorService.shutdownNow();
		try
		{
			executorService.awaitTermination( 10L, TimeUnit.SECONDS );
		}
		catch ( InterruptedException e )
		{
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
	public boolean hasAlpha( final String texture )
	{
		return _alpha.contains( texture );
	}

	/**
	 * Get {@link Texture} for the specified map.
	 *
	 * @param   texture     Name of the texture map.
	 *
	 * @return  Texture for the specified name; <code>null</code> if the name was
	 *          empty or no map by the given name was found.
	 */
	public Texture getTexture( final String texture )
	{
		Texture result = null;

		if ( TextTools.isNonEmpty( texture ) )
		{
			final Map<String,TextureProxy> textures = _textures;

			TextureProxy textureProxy = textures.get( texture );
			if ( textureProxy == null )
			{
				textureProxy = new TextureProxy( texture , this );
				loadTexture( texture , textureProxy );
			}

			result = textureProxy.getTexture();

			if ( result != null )
			{
				final TextureData textureData = textureProxy.getTextureData();
				if ( ( textureData != null ) && ( textureData.getInternalFormat() == GL.GL_RGBA ) )
				{
					_alpha.add( texture );
				}
			}
		}

		return result;
	}

	/**
	 * Get {@link Texture} for color map of {@link Material}.
	 *
	 * @param   material        Material to get color map texture from.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public Texture getColorMapTexture( final Material material )
	{
		final Texture result;

		if ( ( material != null ) && ( material.colorMap != null ) )
		{
			result = getTexture( material.colorMap );
		}
		else
		{
			result = null;
		}
		return result;
	}

	/**
	 * Get {@link Texture} for bump map of {@link Material}.
	 *
	 * @param   material    MAterial to get bump map texture from.
	 *
	 * @return Color map texture; <code>null</code> if face has no color map or no
	 *         texture coordinates.
	 */
	public Texture getBumpMapTexture( final Material material )
	{
		Texture result = null;

		if ( ( material != null ) && TextTools.isNonEmpty( material.bumpMap ) )
		{
			final String texture = material.bumpMap;

			final Map<String, TextureProxy> textures = _textures;

			TextureProxy textureProxy = textures.get( texture );
			if ( textureProxy == null )
			{
				textureProxy = new BumpTextureProxy( texture , this );
				loadTexture( texture , textureProxy );
			}

			result = textureProxy.getTexture();
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
	 * Returns a cube map based on the specified image. The image must have
	 * an aspect ratio of 4:3, consisting of 12 squares with the following
	 * layout:
	 * <pre>
	 *     +---+
	 *     | Y+|
	 * +---+---+---+---+
	 * | X-| Z+| X+| Z-|
	 * +---+---+---+---+
	 *     | Y-|
	 *     +---+
	 * </pre>
	 *
	 * @param   cube    Name of the cube map image.
	 *
	 * @return  Cube map texture.
	 */
	public Texture getCubeMap( final String cube )
	{
		final String key = "cube:" + cube;

		final Map<String, TextureProxy> textures = _textures;

		TextureProxy textureProxy = textures.get( key );
		if ( textureProxy == null )
		{
			textureProxy = new CubeTextureProxy( cube , this );
			loadTexture( key , textureProxy );
		}

		return textureProxy.getTexture();
	}

	/**
	 * Returns a cube map based on the specified images.
	 *
	 * @param   x1  Image on the negative-X side of the cube.
	 * @param   y1  Image on the negative-Y side of the cube.
	 * @param   z1  Image on the negative-Z side of the cube.
	 * @param   x2  Image on the positive-X side of the cube.
	 * @param   y2  Image on the positive-Y side of the cube.
	 * @param   z2  Image on the positive-Z side of the cube.
	 *
	 * @return  Cube map texture.
	 */
	public Texture getCubeMap( final String x1, final String y1, final String z1, final String x2, final String y2, final String z2 )
	{
		final String key = "cube6:" + x1 + ":" + y1 + ":" + z1 + ":" + x2 + ":" + y2 + ":" + z2;

		final Map<String, TextureProxy> textures = _textures;

		TextureProxy textureProxy = textures.get( key );
		if ( textureProxy == null )
		{
			textureProxy = new CubeTextureProxy( x1 , y1 , z1 , x2 , y2 , z2 , this );
			loadTexture( key , textureProxy );
		}

		return textureProxy.getTexture();
	}

	/**
	 * Asynchronously loads the given texture.
	 *
	 * @param   key             Key identifying the texture.
	 * @param   textureProxy    Texture to be loaded.
	 */
	private void loadTexture( @NotNull final String key , @NotNull final TextureProxy textureProxy )
	{
		_textures.put( key , textureProxy );

		final Future<TextureData> textureData = _executorService.submit( textureProxy );
		textureProxy.setTextureData( textureData );

		_executorService.submit( new Runnable()
		{
			@Override
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
			@Override
			public void run()
			{
				for ( final TextureCacheListener listener : _listeners )
				{
					listener.textureChanged( TextureCache.this , textureProxy );
				}
			}
		} );
	}
}
