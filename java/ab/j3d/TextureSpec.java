package ab.light3d;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2003 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2003 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import java.awt.*;
import java.awt.image.PixelGrabber;
import java.util.Hashtable;
import java.io.Serializable;
import java.net.URL;

/**
 * This is the class represents a texture specification in the database.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class TextureSpec
	implements Serializable
{
	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = -8129387219382329102L;

	/**
	 *  Database table name.
	 */
	public final static String TABLE_NAME = "TextureSpecs";

	/**
	 * Unique record ID.
	 */
	public long    ID           = -1;

	/**
	 * Code that uniquely identifies the texture (it should be used instead
	 * of ID to make it independent from the database ID).
	 */
	public String code = null;

	/**
	 * RGB value for textures without texture image (-1 -> has texture image).
	 */
	public int rgb = 0x00FFFFFF;

	/**
	 * This scale factor can be used to convert world coordinates to
	 * texture coordinates (multiplying world coordinates with this factor
	 * will result in texture coordinates). This value is not used by
	 * the material itself, but may be used by the modelling engine to
	 * calculate texture coordinates. This value defaults to 1.0.
	 */
	public float  textureScale          = 1.0f;

	/**
	 * Ambient reflectivity coefficient. This determines the amount of
	 * reflected light from ambient sources (normally just 1). This value
	 * may range from almost 0 for objects that absorb most ambient light
	 * to near 1 for objects that are highly reflective. Typical values
	 * range from 0,1 to 0,2 for dull surfaces and 0,7 to 0,8 for bright
	 * surfaces. In many cases this will be the same as the diffuse
	 * reflectivity coefficient.
	 */
	public float  ambientReflectivity   = 0.3f;

	/**
	 * Diffuse reflectivity coefficient. This determines the amount of
	 * reflected light from diffuse sources. This value may range from
	 * almost 0 for objects that absorb most ambient light to near 1 for
	 * objects that are highly reflective. Typical values range from 0,1
	 * to 0,2 for dull surfaces and 0,7 to 0,8 for bright surfaces.
	 */
	public float  diffuseReflectivity   = 0.5f;

	/**
	 * Specular reflection coefficient. Specular reflection is total or
	 * near total reflection of incoming light in a concentrated region.
	 */
	public float  specularReflectivity  = 0.7f;

	/**
	 * Specular reflection exponent. This exponent is an indicator for
	 * the shinyness or dullness of the material. Shiny surfaces have a
	 * large value for n (100+) and very dull surfaces approach 1. For
	 * optimization reasons, only 1, 2, 4, 8, 16, 32, 64, 128, and 256
	 * are supported.
	 */
	public int specularExponent = 8;

	/**
	 * Flag to indicate that this material's texture has a 'grain'. If so,
	 * it is important how the material is oriented.
	 */
	public boolean grain = false;


/////////////////////////////////////////////////////////////////
/// INTERNAL DATA
/////////////////////////////////////////////////////////////////

	/**
	 * Value returned by parseRGBString is an invalid string is detected.
	 */
	public final static int BADRGB = 0x00FFFFFF;

	/**
	 * Texture path prefix from where material texture images are loaded.
	 *
	 * @FIXME This static prevents applications from using multiple material libraries.
	 */
	public static String textureFilenamePrefix = "";

	/**
	 * Texture path suffix from where material texture images are loaded.
	 *
	 * FIXME: This static prevents applications from using multiple
	 *        material libraries.
	 */
	public static String textureFilenameSuffix = ".jpg";

	/**
	 * Last used 'texture' value. If this is unequal to the 'texture'
	 * field, the values below should be re-assigned.
	 */
	private transient boolean _initDone = false;

	/**
	 * This hashtable is used to share textures between materials. The
	 * key is the 'texture' field value, the elements are Object arrays
	 * with the following layout:
	 *
	 *  [ 0 ] = Integer : _argb
	 *  [ 1 ] = int[][] : _pixels
	 *  [ 2 ] = Boolean : _transparent
	 */
	private static final Hashtable _textureCache = new Hashtable();

	/**
	 * This buffer contains all pixels of a texture in ARGB format. This
	 * is set to <code>null</code> if no texture is available for this
	 * material.
	 */
	private transient int[][] _pixels = null;

	/**
	 * Flag to indicate that this material is (partially) transparent.
	 */
	private transient boolean _transparent = false;

	/**
	 * Exponent that was used to create the phong approximation table.
	 */
	private transient int _phongTableExponent = -1;

	/**
	 * Phong approximation table. This is a 256x256 bitmap with values
	 * from 0 to 256 for the selected phong exponent.
	 */
	private transient short[][] _phongTable = null;

	/**
	 * This hashtable is used to share phong tables between materials.
	 * The key is the specular exponent that was used to calculate the
	 * phong table.
	 */
	private static final Hashtable _phongTableCache = new Hashtable();

	/**
	 * This method returns the texture color as ARGB.
	 *
	 * @return	Texture color as ARGB.
	 */
	public int getARGB()
	{
		//if ( !_initDone ) initTexture();
		//return _argb;
		return 0xFF000000 | rgb;
	}

	/**
	 * Get phong table for this material.
	 */
	public short[][] getPhongTable()
	{
		/*
		 * If we already have the phong table, return it immediately.
		 */
		if ( _phongTableExponent == specularExponent && _phongTable != null )
			return _phongTable;

		synchronized ( _phongTableCache )
		{
			final Integer key = new Integer( specularExponent );
			_phongTableExponent = specularExponent;

			/*
			 * Get phong table from cache.
			 */
			_phongTable = (short[][])_phongTableCache.get( key );
			if ( _phongTable != null )
				return _phongTable;

			/*
			 * If everything failed, build a new phong table.
			 */
			_phongTableCache.put( key , _phongTable = new short[ 256 ][] );

			int x,y;
			double xc,yc,c;
			short s,t[];

			for ( y = 0 ; y <= 128 ; y++ )
			{
				_phongTable[ 128 - y ] = t = new short[ 256 ];
				if ( y < 128 ) _phongTable[ y + 128 ] = t;

				for ( x = 0 ; x <= 128 ; x++ )
				{
					xc = x / 128.0;
					yc = y / 128.0;
					c  = 1d - Math.sqrt( xc * xc + yc * yc );
					if ( c < 0d ) c = 0d;

					t[ 128 - x ] = s = (short)( 256 * Math.pow( c , specularExponent ) );
					if ( x < 128 ) t[ x + 128 ] = s;
				}
			}
		}

		return( _phongTable );
	}

	/**
	 * Get texture for this material. The texture is returned as a 2-dimensional array
	 * of integers in ARGB format. The primary index is the Y-coordinate, the secondary
	 * index is the X-coordinate (or U and V coordinates respectively when applied in
	 * rendering).
	 *
	 * @return	2-dimensional array representing texture.
	 */
	public int[][] getTexture()
	{
		if ( !_initDone ) initTexture();
		return _pixels;
	}

	/**
	 * This method returns the texture bitmap height.
	 *
	 * @return	Texture bitmap height; 0 if not available.
	 */
	public int getTextureHeight()
	{
		if ( !_initDone ) initTexture();
		return ( _pixels != null ) ? _pixels.length : 0;
	}

	/**
	 * This method returns the texture bitmap width.
	 *
	 * @return	Texture bitmap width; 0 if not available.
	 */
	public int getTextureWidth()
	{
		if ( !_initDone ) initTexture();
		return ( _pixels != null && _pixels.length > 0 ) ? _pixels[0].length : 0;
	}

	/**
	 * This internal method is called to initialize texture data based
	 * on the field values. This involves analyzing values and getting
	 * texture images from disk. Caching is used to share information
	 * between instances with the same base texture.
	 */
	private synchronized void initTexture()
	{
		/*
		 * Only initialize if not initialized before.
		 */
		if ( _initDone ) return;
		_initDone = true;

		/*
		 * Set default material properties (for renderers that don't render textures,
		 * and for a default if we were unable to load the texture).
		 */
		_pixels       = null;
		_transparent  = false;
		//_argb         = 0xFF000000 | rgb;

		/*
		 * Ignore 'null' textures
		 */
		if ( code == null || code.length() == 0 )
			return;

		/*
		 * Handle RGB values.
		 */
		if ( rgb >= 0 )
		{
			/*
			 * Normalize color
			 */
			//int r = ( rgb >> 16 ) & 0xFF;
			//int g = ( rgb >>  8 ) & 0xFF;
			//int b =   rgb         & 0xFF;

			//int m = Math.max( r , Math.max( g , b ) );
			//if ( m > 0 && m != 255 )
			//{
				//r = r * 255 / m;
				//g = g * 255 / m;
				//b = b * 255 / m;
			//}

			//_argb = 0xFF000000 + ( r << 16 ) + ( g << 8 ) + b;

			_transparent = false; /*( argb & 0xFF000000 ) != 0xFF000000;*/
			return;
		}

		/*
		 * Try to retrieve the texture info from cache.
		 */
		final Object[] cache = (Object[])_textureCache.get( code );
		if ( cache != null )
		{
			_pixels      =  (int[][])cache[ 0 ];
			_transparent = ((Boolean)cache[ 1 ]).booleanValue();
			return;
		}

		/*
		 * Retrieve image
		 */
		final String filename = textureFilenamePrefix + code + textureFilenameSuffix;
		Image image; 
		try
		{
			image = Toolkit.getDefaultToolkit().getImage( new URL( filename ) );
		}
		catch ( Exception e )
		{
			image = Toolkit.getDefaultToolkit().getImage( filename );
		}

		/*
		 * Grab image pixels.
		 */
		int   tw     = 0;
		int   th     = 0;
		int[] pixels = null;

		if ( image != null )
		{
			try
			{
				final PixelGrabber pg = new PixelGrabber( image , 0 , 0 , -1 , -1 , true );
				if ( pg.grabPixels() )
				{
					tw = pg.getWidth();
					th = pg.getHeight();
					if ( tw > 0 && th > 0 )
						pixels = (int[])pg.getPixels();
				}
			}
			catch ( InterruptedException ie ) {}
		}

		/*
		 * 1) Convert pixels to 2-dimensional array.
		 * 2) Flip texture vertically to get the origin at the lower-left corner.
		 * 3) Analyze pixels to see if any is transparent (alpha < 255).
		 */
		if ( pixels != null )
		{
			_pixels = new int[ th ][];

			for ( int y = 0 ; y < th ; y++ )
			{
				System.arraycopy( pixels , ( th - y - 1 ) * tw ,
					_pixels[ y ] = new int[ tw ] , 0 , tw );
			}

			for ( int i = tw * th ; --i >= 0 ; )
			{
				if ( ( pixels[ i ] & 0xFF000000 ) != 0xFF000000 )
				{
						_transparent = true;
						break;
				}
			}
		}

		/*
		 * Put texture info in cache.
		 */
		_textureCache.put( code , new Object[] {
			/* 0 */ _pixels ,
			/* 1 */ new Boolean( _transparent ) } );
	}

	/**
	 * This method returns <code>true</code> if the material has a texture,
	 * or <code>false</code> if not.
	 *
	 * @return		<code>true</code> if the material has a texture,
	 *				<code>false</code> if not.
	 */
	public boolean isSolidColor()
	{
		return ( rgb >= 0 );
	}

	/**
	 * This method returns <code>true</code> if the material has a texture,
	 * or <code>false</code> if not.
	 *
	 * @return		<code>true</code> if the material has a texture,
	 *				<code>false</code> if not.
	 */
	public boolean isTexture()
	{
		return( getTexture() != null );
	}

	/**
	 * This method returns <code>true</code> if the material is (partially) transparent.
	 *
	 * @return		<code>true</code> if the material is (partially) transparent;
	 *				<code>false</code> otherwise.
	 */
	public boolean isTransparent()
	{
		if ( !_initDone ) initTexture();
		return _transparent;
	}

}
