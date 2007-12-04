package ab.j3d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import ab.j3d.loader.ResourceLoader;

/**
 * Material which loads its texture from a resourceloader.
 */
public class ResourceLoaderMaterial
	extends Material
{
	/**
	 * Resourceloader to use.
	 */
	private ResourceLoader _resourceloader;

	/**
	 * Texture cache
	 */
	private Map<String,SoftReference<BufferedImage>> _textureCache = new HashMap<String,SoftReference<BufferedImage>>();

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4730962090640159465L;

	/**
	 * Constructs a new ResourceLoaderMaterial with given {@link ResourceLoader}
	 *
	 * @param resourceLoader     {@link ResourceLoader} to use.
	 */
	public ResourceLoaderMaterial( final ResourceLoader resourceLoader )
	{
		_resourceloader = resourceLoader;
	}

	/**
	 * Construct ResourceLoaderMaterial for ARGB value.
	 *
	 * @param   argb    ARGB color specification.
	 *
	 * @see     java.awt.Color
	 */
	public ResourceLoaderMaterial( final int argb )
	{
		super( argb );
		_resourceloader = null;
	}

	public BufferedImage getColorMapImage( final boolean useCache )
	{
		BufferedImage result;
		final ResourceLoader resourceloader = _resourceloader;
		if ( resourceloader != null )
		{
			final Map<String,SoftReference<BufferedImage>> textureCache = _textureCache;
			if ( useCache && textureCache.containsKey( super.colorMap ) && textureCache
			.get( super.colorMap ) != null )
			{
				result = textureCache.get( super.colorMap ).get();
			}
			else
			{

				try
				{
					final InputStream is = resourceloader.getResource( super.colorMap );
					result = ImageIO.read( is );
					textureCache.put( super.colorMap , new SoftReference<BufferedImage>( result ) );

				}
				catch ( IOException e )
				{
					result = null;
					e.printStackTrace();
				}
			}
		}
		else
			result = super.getColorMapImage( true );
		return result;
	}
}