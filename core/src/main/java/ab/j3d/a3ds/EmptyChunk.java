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
 * This chunk class can be used to handle unknown/unsupported chunk types.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class EmptyChunk
	extends DataChunk
{
	/**
	 * Constructor with chunk ID.
	 *
	 * @param   id      ID of the chunk.
	 */
	public EmptyChunk( final int id )
	{
		super( id );
	}

	public long getSize()
	{
		return HEADER_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );
	}
}
