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

import java.io.IOException;

/**
 * This chunk specifies a RGB color value.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsRGB extends DataChunk
{
	/**
	 * If true, the color is specified by floats, otherwise by ints.
	 */
	private final boolean _floats;

	/**
	 * Float value (0..1) of color's red segment.
	 */
	private float _fr;

	/**
	 * Float value (0..1) of color's green segment.
	 */
	private float _fg;

	/**
	 * Float value (0..1) of color's blue segment.
	 */
	private float _fb;

	/**
	 * Integer value (0..255) of color's red segment.
	 */
	private byte  _r;

	/**
	 * Integer value (0..255) of color's green segment.
	 */
	private byte  _g;

	/**
	 * Integer value (0..255) of color's blue segment.
	 */
	private byte  _b;

	/**
	 * Constructs a rgb color with specified float values.
	 *
	 * @param   r   Red segment (0..1).
	 * @param   g   Green segment (0..1).
	 * @param   b   Blue segment (0..1).
	 */
	public Ab3dsRGB( final float r , final float g , final float b )
	{
		this( RGB_FLOAT , true );
		_fr = r;
		_fg = g;
		_fb = b;
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 * @param   floats  If true, the rgb is specified as floats, otherwise with bytes.
	 */
	public Ab3dsRGB( final int id, final boolean floats )
	{
		super( id );

		_floats = floats;
		_fr     = 0;
		_fg     = 0;
		_fb     = 0;
		_r      = 0;
		_g      = 0;
		_b      = 0;

//		if ( Ab3dsFile.DEBUG )
//			System.out.println( "  - RGB " + (floats?"floats":"bytes") );
	}

	/**
	 * Constructs a rgb color with specified int values.
	 *
	 * @param   r   Red segment (0..255).
	 * @param   g   Green segment (0..255).
	 * @param   b   Blue segment (0..255).
	 */
	public Ab3dsRGB( final int r , final int g , final int b )
	{
		this( RGB_BYTE , false );

		_r      = (byte)r;
		_g      = (byte)g;
		_b      = (byte)b;
	}

	public long getSize()
	{
		return HEADER_SIZE + (_floats ? 3*FLOAT_SIZE : 3*BYTE_SIZE);
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		if ( _floats )
		{
			_fr = is.readFloat();
			_fg = is.readFloat();
			_fb = is.readFloat();
		}
		else
		{
			_r = is.readByte();
			_g = is.readByte();
			_b = is.readByte();
		}
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );

		if ( _floats )
		{
			os.writeFloat( _fr );
			os.writeFloat( _fg );
			os.writeFloat( _fb );
		}
		else
		{
			os.writeByte( _r );
			os.writeByte( _g );
			os.writeByte( _b );
		}
	}

	/**
	 * Returns a String representation of this chunk.
	 *
	 * @return  this chunk as a string.
	 */
	public String toString()
	{
		final String result;

		if ( _floats )
			result = "R:" + _fr + " G:" + _fg + " B:" + _fb;
		else
			result = "R:" + _r + " G:" + _g + " B:" + _b;

		return result;
	}
}
