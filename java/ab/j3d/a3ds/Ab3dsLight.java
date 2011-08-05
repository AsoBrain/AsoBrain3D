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
 * This chunk specifies a light.
 * <pre>
 * Chunk ID :
 * - OBJ_LIGHT      = 0x4600
 *
 * Parent chunk :
 * - EDIT_OBJECT    = 0x4000
 *
 * Possible sub chunks :
 * - LIT_SPOT       = 0x4610;
 * - LIT_OFF        = 0x4620;
 * - LIT_RAY        = 0x4627;
 * - LIT_CAST       = 0x4630;
 * - LIT_OUT_RANGE  = 0x465A;
 * - LIT_IN_RANGE   = 0x4659;
 * - LIT_MULTIPLIER = 0x465B;
 * - LIT_ROLL       = 0x4656;
 * - LIT_RAY_BIAS   = 0x4658;
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsLight
	extends HierarchyChunk
{
	/*
	 * When present, indicates that the light source is turned off.
	 */
	public static final class Off
		extends DataChunk
	{
		/**
		 * Constructor of Chunk with ChunkID to be used
		 * when the Chunk is read from inputstream.
		 *
		 * @param   id      ID of the chunk.
		 */
		public Off( final int id )
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

	/**
	 * This class defines a spot light.
	 */
	public static final class SpotLight
		extends DataChunk
	{
		/**
		 * X-position of spot light target.
		 */
		public float _targetX;

		/**
		 * Y-position of spot light target.
		 */
		public float _targetY;

		/**
		 * Z-position of spot light target.
		 */
		public float _targetZ;

		/**
		 * Hot spot. Defines the light beam frustum.
		 *
		 * @FIXME need description here, don't know the values that go here.
		 */
		public float _hotspot;

		/**
		 * Fall-off. Defines the light strength relative to the distance
		 * between the source and the target.
		 *
		 * @FIXME need description here, don't know the values that go here.
		 */
		public float _falloff;

		/**
		 * Constructor with chunk ID to use reading from an input stream.
		 *
		 * @param   id      ID of the chunk.
		 */
		public SpotLight( final int id )
		{
			super( id );

			_targetX = 0;
			_targetY = 0;
			_targetZ = 0;
			_hotspot = 0;
			_falloff = 0;

			if ( Ab3dsFile.DEBUG )
				System.out.println( "  - Spotlight" );
		}

		/**
		 * Default constructor for generation purposes.
		 */
		public SpotLight()
		{
			this( LIT_SPOT );
		}

		public long getSize()
		{
			return HEADER_SIZE + 5 * FLOAT_SIZE;
		}

		public void read( final Ab3dsInputStream is )
			throws IOException
		{
			readHeader( is );

			_targetX = is.readFloat();
			_targetY = is.readFloat();
			_targetZ = is.readFloat();
			_hotspot = is.readFloat();
			_falloff = is.readFloat();
		}

		public void write( final Ab3dsOutputStream os )
			throws IOException
		{
			writeHeader( os );

			os.writeFloat( _targetX );
			os.writeFloat( _targetY );
			os.writeFloat( _targetZ );
			os.writeFloat( _hotspot );
			os.writeFloat( _falloff );
		}

		public void set( final float x , final float y , final float z , final float hotspot , final float falloff )
		{
			_targetX = x;
			_targetY = y;
			_targetZ = z;
			_hotspot = hotspot;
			_falloff = falloff;
		}
	}

	/**
	 * X-position of light.
	 */
	private float _sourceX;

	/**
	 * Y-position of light.
	 */
	private float _sourceY;

	/**
	 * Z-position of light.
	 */
	private float _sourceZ;

	/**
	 * Default constructor.
	 */
	public Ab3dsLight()
	{
		this( OBJ_LIGHT );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public Ab3dsLight( final int id )
	{
		super(id);

		_sourceX = 0;
		_sourceY = 0;
		_sourceZ = 0;
	}

	/**
	 * Set parameters for light.
	 *
	 * @param   x   X-position of light source.
	 * @param   y   Y-position of light source.
	 * @param   z   Z-position of light source.
	 */
	public void set( final float x , final float y , final float z )
	{
		_sourceX = x;
		_sourceY = y;
		_sourceZ = z;
	}

	public long getSize()
	{
		return super.getSize() + 3 * FLOAT_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		_sourceX = is.readFloat();
		_sourceY = is.readFloat();
		_sourceZ = is.readFloat();

		readSubChunks( is );
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		writeHeader( os );

		os.writeFloat( _sourceX );
		os.writeFloat( _sourceY );
		os.writeFloat( _sourceZ );

		writeSubChunks( os );
	}
}
