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
package ab.j3d.a3ds;

import java.io.*;

/**
 * Smoothing groups list.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class SmoothingGroups
	extends Chunk
{
	/**
	 * Smoothing group bit mask by face index.
	 */
	private int[] _smoothingGroupsPerFace;

	/**
	 * Constructs a new instance.
	 *
	 * @param   parent  Parent chunk.
	 */
	public SmoothingGroups( final Chunk parent )
	{
		super( TRI_SMOOTH );
		final FaceList faceList = (FaceList)parent;
		_smoothingGroupsPerFace = new int[ faceList.getFaceCount() ];
	}

	@Override
	public long getSize()
	{
		return HEADER_SIZE + (long)( _smoothingGroupsPerFace.length * LONG_SIZE );
	}

	/**
	 * Returns the smoothing groups that the given face belongs to.
	 *
	 * @param   faceIndex   Index of the face.
	 *
	 * @return  Smoothing groups, as a bit mask.
	 */
	public int getSmoothingGroup( final int faceIndex )
	{
		return _smoothingGroupsPerFace[ faceIndex ];
	}

	@Override
	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );
		for ( int i = 0 ; i < _smoothingGroupsPerFace.length ; i++ )
		{
			_smoothingGroupsPerFace[ i ] = (int)is.readLong();
		}
	}

	@Override
	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );
		for ( final int smoothingGroup : _smoothingGroupsPerFace )
		{
			os.writeLong( (long)smoothingGroup );
		}
	}
}
