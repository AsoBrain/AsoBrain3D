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
 * This chunk specifies a camera.
 * <pre>
 * Chunk ID :
 * - OBJ_CAMERA = 0x4700
 *
 * Parent chunk :
 * - EDIT_OBJECT = 0x4000
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsCamera
	extends HierarchyChunk
{
	/**
	 * X-position of camera.
	 */
	private float _x;

	/**
	 * Y-position of camera.
	 */
	private float _y;

	/**
	 * Z-position of camera.
	 */
	private float _z;

	/**
	 * Target X-position of camera.
	 */
	private float _tx;

	/**
	 * Target Y-position of camera.
	 */
	private float _ty;

	/**
	 * Target Z-position of camera.
	 */
	private float _tz;

	/**
	 * Camera angle (tilt).
	 */
	private float _bank;

	/**
	 * Lens specification in mm.
	 */
	private float _lens;

	/**
	 * Default constructor. ID of chuck is always <code>OBJ_CAMERA</code>.
	 *
	 * @see     #OBJ_CAMERA
	 */
	public Ab3dsCamera()
	{
		super( OBJ_CAMERA );

		_x    = 0;
		_y    = 0;
		_z    = 0;
		_tx   = 0;
		_ty   = 0;
		_tz   = 0;
		_bank = 0;
		_lens = 0;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
	 */
	public long getSize()
	{
		return super.getSize() + 8 * FLOAT_SIZE;
	}

	/**
	 * Reads the chunk from the input stream.
	 *
	 * @param   is  Stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		_x = is.readFloat();
		_y = is.readFloat();
		_z = is.readFloat();

		_tx = is.readFloat();
		_ty = is.readFloat();
		_tz = is.readFloat();

		_bank = is.readFloat();
		_lens = is.readFloat();

		readSubChunks( is );
	}

	/**
	 * Parameters for the camera.
	 *
	 * @param   x       X-position of camera
	 * @param   y       Y-position of camera
	 * @param   z       Z-position of camera
	 * @param   tx      Target x-position of camera
	 * @param   ty      Target y-position of camera
	 * @param   tz      Target z-position of camera
	 * @param   bank    Camera angle (tilt).
	 * @param   lens    Lens specification in mm.
	 */
	public void set( final float x , final float y , final float z , final float tx , final float ty , final float tz , final float bank , final float lens )
	{
		_x = x;
		_y = y;
		_z = z;
		_tx = tx;
		_ty = ty;
		_tz = tz;
		_bank = bank;
		_lens = lens;
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

		os.writeFloat( _x );
		os.writeFloat( _y );
		os.writeFloat( _z );

		os.writeFloat( _tx );
		os.writeFloat( _ty );
		os.writeFloat( _tz );

		os.writeFloat( _bank );
		os.writeFloat( _lens );

		writeSubChunks( os );
	}

}
