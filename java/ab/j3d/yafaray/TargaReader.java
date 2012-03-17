/*
 * $Id$
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
package ab.j3d.yafaray;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Reads uncompressed 24-bit per pixel TARGA images. This is the format that
 * YafaRay uses to write renderer output. Since no other kinds of TARGA images
 * are supported, this class was made package private.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
class TargaReader
{
	/**
	 * Constructs a new instance.
	 */
	public TargaReader()
	{
	}

	/**
	 * Reads a TARGA image from the given stream.
	 *
	 * @param   in  Stream to read from.
	 *
	 * @return  Image that was read.
	 *
	 * @throws  IOException if an I/O error occurs or the image format is not
	 *          supported.
	 */
	public BufferedImage read( final InputStream in )
	throws IOException
	{
		final int idLength = in.read();
		final int colorMapType = in.read();

		final int imageType = in.read();
		if ( imageType != 2 )
		{
			throw new IOException( "Unsupported image: image type = " + imageType );
		}

		// Color map specification
		final int firstEntryIndex = readUnsignedShortLE( in );
		final int colorMapLength = readUnsignedShortLE( in );
		final int colorMapEntrySize = in.read();

		// Image specification
		final int originX = readUnsignedShortLE( in );
		final int originY = readUnsignedShortLE( in );
		final int imageWidth = readUnsignedShortLE( in );
		final int imageHeight = readUnsignedShortLE( in );

		final int bitsPerPixel = in.read();
		if ( bitsPerPixel != 24 )
		{
			throw new IOException( "Unsupported image: bits per pixel = " + bitsPerPixel );
		}

		final int imageDescriptor = in.read();

		skip( in, idLength );
		skip( in, colorMapLength * colorMapEntrySize / 8 );

		final int imageDataLength = imageWidth * imageHeight * bitsPerPixel / 8;
		final byte[] imageData = new byte[ imageDataLength ];
		readFully( in, imageData );

		final DataBufferByte dataBuffer = new DataBufferByte( imageData, imageData.length );
		final int[] bandOffsets = { 2, 1, 0 };
		final WritableRaster raster = WritableRaster.createInterleavedRaster( dataBuffer, imageWidth, imageHeight, imageWidth * bitsPerPixel / 8, bitsPerPixel / 8, bandOffsets, new Point() );

		final BufferedImage result = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB );
		result.setData( raster );
		return result;
	}

	/**
	 * Reads from the given stream into the given array, until it is full.
	 *
	 * @param   in      Stream to read from.
	 * @param   result  Array to write to.
	 *
	 * @throws  EOFException if the stream ends before the array is full.
	 * @throws  IOException if an I/O error occurs.
	 */
	private void readFully( final InputStream in, final byte[] result )
	throws IOException
	{
		int totalRead = 0;
		do
		{
			final int read = in.read( result, totalRead, result.length - totalRead );
			if ( read == -1 )
			{
				throw new EOFException();
			}
			totalRead += read;
		}
		while ( totalRead < result.length );
	}

	/**
	 * Discards the specified number of bytes from the given stream.
	 *
	 * @param   in      Stream to read from.
	 * @param   count   Number of bytes to skip.
	 *
	 * @throws  EOFException if the stream ends before the bytes are skipped.
	 * @throws  IOException if an I/O error occurs.
	 */
	private void skip( final InputStream in, final int count )
	throws IOException
	{
		for ( int i = 0; i < count; i++ )
		{
			if ( in.read() == -1 )
			{
				throw new EOFException();
			}
		}
	}

	/**
	 * Reads an unsigned 16-bit integer from the given stream, using
	 * little-endian byte order.
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Unsigned 16-bit integer.
	 *
	 * @throws  EOFException if the stream ends before reading two bytes.
	 * @throws  IOException if an I/O error occurs.
	 */
	private int readUnsignedShortLE( final InputStream in )
	throws IOException
	{
		final int a = in.read();
		if ( a == -1 )
		{
			throw new EOFException();
		}

		final int b = in.read();
		if ( b == -1 )
		{
			throw new EOFException();
		}

		return a | b << 8;
	}
}
