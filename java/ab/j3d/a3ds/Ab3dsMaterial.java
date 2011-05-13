/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Sjoerd Bouwman
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
package ab.j3d.a3ds;

import java.io.*;

/**
 * This chunk specifies a material definition.
 * <pre>
 * Chunk ID :
 * - EDIT_MATERIAL      = 0xAFFF
 *
 * Parent chunk :
 * - EDIT3DS            = 0x3D3D
 *
 * Possible sub chunks :
 * - MAT_NAME           = 0xA000
 * - MAT_AMBIEN         = 0xA010
 * - MAT_DIFFUSE        = 0xA020
 * - MAT_SPECULAR       = 0xA030
 * - MAT_SHININESS      = 0xA040
 * - MAT_SHINSTRENGTH   = 0xA041
 * - MAT_TRANSPARENCY   = 0xA050
 * - MAT_TRANSFALLOFF   = 0xA052
 * - MAT_REFLECTBLUR    = 0xA053
 * - MAT_TYPE           = 0xA100
 * - MAT_ILLUMINATION   = 0xA084
 * - MAT_WIRETHICKNESS  = 0xA087
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsMaterial
	extends DataChunk
{
	/**
	 * Material type (Max: shader basic): flat.
	 */
	public static final int TYPE_FLAT  = 1;

	/**
	 * Material type (Max: shader basic): gouraud.
	 */
	public static final int TYPE_GOUR  = 2;

	/**
	 * Material type (Max: shader basic): phong.
	 */
	public static final int TYPE_PHONG = 3;

	/**
	 * Material type (Max: shader basic): metalic.
	 */
	public static final int TYPE_METAL = 4;

	/*
	 * Material properties.
	 */
	private String _name;
	private Ab3dsRGB _ambient;
	private Ab3dsRGB _diffuse;
	private Ab3dsRGB _specular;
	private int _shininess; // Max: Glossiness
	private int _shinstrength; // Max: Specular level
	private int _transparency = 0; // Max: 0 = opacity 100%
	private int _transfalloff = 0;
	private int _reflectblur = 0;
	private boolean _twoSided = false;
	private int _type;
	private int _selfilum;
	private float _wireThickness;

	/*
	 * Texture maps.
	 */
	private TextureMap _texture1Map;
	private TextureMap _texture2Map;
	private TextureMap _opacityMap;
	private TextureMap _bumpMap;
	private TextureMap _specularMap;
	private TextureMap _shininessMap;
	private TextureMap _illuminationMap;
	private TextureMap _reflectionMap;

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public Ab3dsMaterial( final int id )
	{
		super( id );
	}

	/**
	 * Constructor for material with name.
	 *
	 * @param   name    Name of the new material.
	 */
	public Ab3dsMaterial( final String name )
	{
		this( EDIT_MATERIAL );
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	public Ab3dsRGB getAmbient()
	{
		return _ambient;
	}

	public Ab3dsRGB getDiffuse()
	{
		return _diffuse;
	}

	public Ab3dsRGB getSpecular()
	{
		return _specular;
	}

	public int getShininess()
	{
		return _shininess;
	}

	public int getShinstrength()
	{
		return _shinstrength;
	}

	public float getTransparency()
	{
		return (float)_transparency / 255.0f;
	}

	public int getTransfalloff()
	{
		return _transfalloff;
	}

	public int getReflectblur()
	{
		return _reflectblur;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	public int getType()
	{
		return _type;
	}

	public int getSelfilum()
	{
		return _selfilum;
	}

	public float getWireThickness()
	{
		return _wireThickness;
	}

	public TextureMap getTexture1Map()
	{
		return _texture1Map;
	}

	public TextureMap getTexture2Map()
	{
		return _texture2Map;
	}

	public TextureMap getOpacityMap()
	{
		return _opacityMap;
	}

	public TextureMap getBumpMap()
	{
		return _bumpMap;
	}

	public TextureMap getSpecularMap()
	{
		return _specularMap;
	}

	public TextureMap getShininessMap()
	{
		return _shininessMap;
	}

	public TextureMap getIlluminationMap()
	{
		return _illuminationMap;
	}

	public TextureMap getReflectionMap()
	{
		return _reflectionMap;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  Size of the chunk in bytes.
	 */
	public long getSize()
	{
		long size = HEADER_SIZE;					// chunk itself.

		size+= HEADER_SIZE + STRING_SIZE( _name );	// name
		size+= HEADER_SIZE + _ambient.getSize();	// ambient color
		size+= HEADER_SIZE + _diffuse.getSize();	// diffuse color
		size+= HEADER_SIZE + _specular.getSize();	// specular color
		size+= 6 * (2*HEADER_SIZE + INT_SIZE);		// shininess, shinystr, trans, transfalloff, relfect, illum
		size+= HEADER_SIZE + INT_SIZE;				// type
		size+= HEADER_SIZE + FLOAT_SIZE;			// wire thickness

		if ( _texture1Map != null ) 	size+= _texture1Map.getSize();
		if ( _texture2Map != null ) 	size+= _texture2Map.getSize();
		if ( _opacityMap != null ) 		size+= _opacityMap.getSize();
		if ( _bumpMap != null ) 		size+= _bumpMap.getSize();
		if ( _specularMap != null ) 	size+= _specularMap.getSize();
		if ( _shininessMap != null ) 	size+= _shininessMap.getSize();
		if ( _illuminationMap != null ) size+= _illuminationMap.getSize();
		if ( _reflectionMap != null ) 	size+= _reflectionMap.getSize();

		return size;
	}

	/**
	 * Reads the chunk from the input stream.
	 *
	 * @param   is      Stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		/*
		 * Read properties.
		 */
		int id = is.readInt();
		long size = is.readLong();
		while ( !is.isEOF() && is.getPointer() < _chunkEnd )
		{
			switch ( id )
			{
				case MAT_NAME			: _name = is.readString(); 				break;
				case MAT_AMBIENT		: _ambient = readColor( is, size - HEADER_SIZE ); break;
				case MAT_DIFFUSE		: _diffuse = readColor( is, size - HEADER_SIZE ); break;
				case MAT_SPECULAR		: _specular = readColor( is, size - HEADER_SIZE ); break;
				case MAT_SHININESS  	: _shininess = readDoubleByte( is );	break;
				case MAT_SHINSTRENGTH  	: _shinstrength = readDoubleByte( is );	break;
				case MAT_TRANSPARENCY  	: _transparency = readDoubleByte( is );	break;
				case MAT_TRANSFALLOFF  	: _transfalloff = readDoubleByte( is );	break;
				case MAT_REFLECTBLUR   	: _reflectblur = readDoubleByte( is );	break;
				case MAT_TWO_SIDED      : _twoSided = true; is.skip( size - HEADER_SIZE ); break;
				case MAT_TYPE			: _type = is.readInt();					break;
				case MAT_ILLUMINATION	: _selfilum = readDoubleByte( is );		break;
				case MAT_WIRETHICKNESS	: _wireThickness = is.readFloat();		break;

				case MAT_TEXT1_MAP		: _texture1Map = readMap( is , id ); 	break;
				case MAT_TEXT2_MAP		: _texture2Map = readMap( is , id ); 	break;
				case MAT_OPACITY_MAP	: _opacityMap = readMap( is , id ); 	break;
				case MAT_BUMP_MAP		: _bumpMap = readMap( is , id ); 		break;
				case MAT_SPECULAR_MAP	: _specularMap = readMap( is , id ); 	break;
				case MAT_SHINI_MAP		: _shininessMap = readMap( is , id ); 	break;
				case MAT_ILLUM_MAP		: _illuminationMap = readMap( is , id );break;
				case MAT_REFLECT_MAP	: _reflectionMap = readMap( is , id );	break;

				default 				: is.skip( size - HEADER_SIZE );		// skip unknown chunks.
			}

			if ( is.getPointer() < _chunkEnd )
			{
				id = is.readInt();
				size = is.readLong();
			}
		}

		if ( Ab3dsFile.DEBUG )
		{
			System.out.println( "Material : " + _name );
			//System.out.println( "Ambient  : " + _ambient );
			//System.out.println( "Diffuse  : " + _diffuse );
			//System.out.println( "Specular : " + _specular );
			//System.out.println( "Shiny    : " + _shininess );
			//System.out.println( "Shinstr  : " + _shinstrength );
			//System.out.println( "Trans    : " + _transparency );
			//System.out.println( "TransFO  : " + _transfalloff );
			//System.out.println( "ReflBlur : " + _reflectblur );
			//System.out.println( "Type     : " + _type );
			//System.out.println( "Illum    : " + _selfilum );
			//System.out.println( "Wire     : " + _wireThickness );
			//System.out.println( "Map1     : " + _texture1Map );
		}
	}

	/**
	 * Read texture map from stream.
	 *
	 * @param   is      Stream to read from.
	 * @param   id      Chunk ID / map type.
	 *
	 * @return  Texture map that was read.
	 *
	 * @throws  IOException when an io error occurred.
	 */
	private static TextureMap readMap( final Ab3dsInputStream is , final int id )
		throws IOException
	{
		/*
		 * Go back 4 bytes, cause we've already read the size.
		 */
		is.skip( -4 );
		final TextureMap map = new TextureMap( id );
		map.read( is );
		return map;
	}

	/**
	 * Set ambient color as floats.
	 *
	 * @param   r   Red segment (0..1).
	 * @param   g   Green segment (0..1).
	 * @param   b   Blue segment (0..1).
	 */
	public void setAmbient( final float r , final float g , final float b )
	{
		_ambient = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set ambient color as ints.
	 *
	 * @param   r   Red segment (0..255).
	 * @param   g   Green segment (0..255).
	 * @param   b   Blue segment (0..255).
	 */
	public void setAmbient( final int r , final int g , final int b )
	{
		_ambient = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set parameters for material.
	 *
	 * @param   type            Material type (FLAT,PHONG,METAL).
	 * @param   shininess       Shininess of the material.
	 * @param   shinstrength    Amount of shininess.
	 * @param   transparency    Thrue percentage.
	 * @param   selfilum        Selfilum percentage.
	 * @param   wireThickness   Thickness of wires in wireframe view.
	 */
	public void setBasic( final int type , final int shininess , final int shinstrength , final int transparency , final int selfilum , final int wireThickness )
	{
		_type = type;
		_shininess = shininess;
		_shinstrength = shinstrength;
		_transparency = transparency;
		_selfilum = selfilum;
		_wireThickness = wireThickness;
	}

	/**
	 * Set diffuse color as floats.
	 *
	 * @param   r   Red segment (0..1).
	 * @param   g   Green segment (0..1).
	 * @param   b   Blue segment (0..1).
	 */
	public void setDiffuse( final float r , final float g , final float b )
	{
		_diffuse = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set diffuse color as ints.
	 *
	 * @param   r   Red segment (0..255).
	 * @param   g   Green segment (0..255).
	 * @param   b   Blue segment (0..255).
	 */
	public void setDiffuse( final int r , final int g , final int b )
	{
		_diffuse = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set specular color as floats.
	 *
	 * @param   r   Red segment (0..1).
	 * @param   g   Green segment (0..1).
	 * @param   b   Blue segment (0..1).
	 */
	public void setSpecular( final float r , final float g , final float b )
	{
		_specular = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set specular color as ints.
	 *
	 * @param   r   Red segment (0..255).
	 * @param   g   Green segment (0..255).
	 * @param   b   Blue segment (0..255).
	 */
	public void setSpecular( final int r , final int g , final int b )
	{
		_specular = new Ab3dsRGB( r , g , b );
	}

	/**
	 * Set primary texture map.
	 *
	 * @param   map     New primary texture map.
	 */
	public void setTexture1Map( final TextureMap map )
	{
		_texture1Map = map;
	}

	/**
	 * Writes the chunk the output stream.
	 *
	 * @param   os      Stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );

		os.writeInt( MAT_NAME );
		os.writeLong( HEADER_SIZE + STRING_SIZE( _name ) );
		os.writeString( _name );

		writeColor( os , MAT_AMBIENT , _ambient );
		writeColor( os , MAT_DIFFUSE , _diffuse );
		writeColor( os , MAT_SPECULAR , _specular );

		writeDoubleByte( os , MAT_SHININESS , _shininess );
		writeDoubleByte( os , MAT_SHINSTRENGTH , _shinstrength );
		writeDoubleByte( os , MAT_TRANSPARENCY , _transparency );
		writeDoubleByte( os , MAT_TRANSFALLOFF , _transfalloff );
		writeDoubleByte( os , MAT_REFLECTBLUR , _reflectblur );

		os.writeInt( MAT_TYPE );
		os.writeLong( HEADER_SIZE + INT_SIZE );
		os.writeInt( _type );

		writeDoubleByte( os , MAT_ILLUMINATION , _selfilum );

		os.writeInt( MAT_WIRETHICKNESS );
		os.writeLong( HEADER_SIZE + FLOAT_SIZE );
		os.writeFloat( _wireThickness );

		if ( _texture1Map     != null ) _texture1Map    .write( os );
		if ( _texture2Map     != null ) _texture2Map    .write( os );
		if ( _opacityMap      != null ) _opacityMap     .write( os );
		if ( _bumpMap         != null ) _bumpMap        .write( os );
		if ( _specularMap     != null ) _specularMap    .write( os );
		if ( _shininessMap    != null ) _shininessMap   .write( os );
		if ( _illuminationMap != null ) _illuminationMap.write( os );
		if ( _reflectionMap   != null ) _reflectionMap  .write( os );
	}
}
