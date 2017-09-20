/* $Id$
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
package ab.j3d.loader.max3ds;

import java.awt.*;
import java.io.*;
import java.util.*;

import ab.j3d.*;

/**
 * <pre>
 * Type   : {@link #KEY_OBJECT},
 *          {@link #KEY_CAM_TARGET},
 *          {@link #KEY_CAMERA_OBJECT},
 *          {@link #KEY_OMNI_LI_INFO},
 *          {@link #KEY_AMB_LI_INFO},
 *          {@link #KEY_SPOT_TARGET},
 *          {@link #KEY_SPOT_OBJECT}
 * Parent : {@link #KEYFRAMES} ({@link KeyFramesChunk})
 * </pre>
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class KeyFrameChunk
	extends Chunk
{
	String _name;

	short _parent;

	short _id;

	Vector3f _pivot;

	ArrayList<Frame> _track;

	float _morphSmoothAngle;

	Vector3f _boundingBoxMin;

	Vector3f _boundingBoxMax;

	KeyFrameChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		super( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		_track = new ArrayList<Frame>();
		super.processChunk( in, chunkType, remainingChunkBytes );
	}

	@Override
	protected void processChildChunk( final InputStream in, final int chunkType, final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case NODE_ID :
				_id = readShort( in );
				break;

			case TRACK_HEADER :
				_name = readCString( in );
				 readShort( in ); /* flag1 */
				readShort( in ); /* flag2 */
				_parent = readShort( in );
				break;

			case TRACK_PIVOT :
				_pivot = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				break;

			case TRACK_POS_TAG :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._position = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				}
				break;

			case TRACK_ROT_TAG :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					final float angle = readFloat( in );
					final float axisX = readFloat( in );
					final float axisY = readFloat( in );
					final float axisZ = readFloat( in );

					frame._rotation = Matrix3D.getRotationTransform( 0.0, 0.0, 0.0, (double)axisX, (double)axisY, (double)axisZ, (double)angle );
				}
				break;

			case TRACK_SCL_TAG :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._scale = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				}
				break;

			case MORPH_SMOOTH :
				_morphSmoothAngle = readFloat( in );
				break;

			case KEY_FOV_TRACK :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._fieldOfView = readFloat( in );
				}
				break;

			case KEY_ROLL_TRACK :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._roll = readFloat( in );
				}
				break;

			case KEY_COLOR_TRACK :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._color = new Color( readFloat( in ), readFloat( in ), readFloat( in ), 1.0f );
				}
				break;

			case KEY_HOTSPOT_TRACK :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._hotSpot = readFloat( in );
				}
				break;

			case KEY_FALLOFF_TRACK :
				readShort( in ); /* flags */
				readLong( in ); /* unknown */

				for ( int frameCount = readInt( in ); --frameCount >= 0; )
				{
					final Frame frame = getFrame( readInt( in ) ); /* frame number */
					readShort( in ); /* acceleration data */

					frame._fallOff = readFloat( in );
				}
				break;

			case BOUNDING_BOX :
				_boundingBoxMin = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				_boundingBoxMax = new Vector3f( readFloat( in ), readFloat( in ), readFloat( in ) );
				break;

			case INSTANCE_NAME :
				_name = readCString( in );
				break;

			default : // Ignore unknown chunks
				skipFully( in, remainingChunkBytes );
		}
	}

	private Frame getFrame( final int number )
	{
		Frame result = null;

		final ArrayList<Frame> track = _track;

		int i;
		for ( i = 0; i < track.size(); i++ )
		{
			final Frame frame = track.get( i );

			if ( number > frame._number )
			{
				break;
			}

			if ( number == frame._number )
			{
				result = frame;
				break;
			}
		}

		if ( result == null )
		{
			result = new Frame();
			result._number = number;
			track.add( i, result );
		}

		return result;
	}

	public static class Frame
	{
		public int _number = 0;

		public Vector3f _position = null;

		public Matrix3D _rotation = null;

		public Vector3f _scale = null;

		public float _fieldOfView = 0.0f;

		public float _roll = 0.0f;

		public String _morphName = null;

		public float _hotSpot = 0.0f;

		public float _fallOff = 0.0f;

		public Color _color = null;
	}
}
