package ab.j3d.loader.max3ds;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Type:   {@link #KEYFRAMES}
 * Parent: {@link #MAIN_3DS}
 *
 * @noinspection JavaDoc,PublicField,InstanceVariableMayNotBeInitialized
 */
class KeyFramesChunk
	extends Chunk
{
	int _animationLen = 0;

	int _begin = 0;

	int _end = 0;

	Map<String,KeyFrameChunk> _objectKeyframes;

	List<KeyFrameChunk> _cameraKeyframes;

	List<KeyFrameChunk> _lightKeyframes;

	KeyFramesChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		super( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		_objectKeyframes = new HashMap<String,KeyFrameChunk>();
		_cameraKeyframes = new ArrayList<KeyFrameChunk>();
		_lightKeyframes = new ArrayList<KeyFrameChunk>();

		super.processChunk( dataInput , chunkType , remainingChunkBytes );
	}

	protected void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
		switch ( chunkType )
		{
			case KEY_HEADER :
				dataInput.readShort(); /* revision */
				readCString( dataInput ); /* filename */
				_animationLen = dataInput.readInt();
				break;

			case KEY_SEGMENT :
				_begin = dataInput.readInt();
				_end = dataInput.readInt();
				break;

			case KEY_CURTIME :
				dataInput.readInt(); /* current frame */
				break;

			case KEY_OBJECT :
				final KeyFrameChunk keyFrame = new KeyFrameChunk( dataInput , chunkType , remainingChunkBytes );
				_objectKeyframes.put( keyFrame._name , keyFrame );
				break;

			case KEY_CAM_TARGET :
			case KEY_CAMERA_OBJECT :
				_cameraKeyframes.add( new KeyFrameChunk( dataInput , chunkType , remainingChunkBytes ) );
				break;

			case KEY_OMNI_LI_INFO :
			case KEY_AMB_LI_INFO :
			case KEY_SPOT_TARGET :
			case KEY_SPOT_OBJECT :
				_lightKeyframes.add( new KeyFrameChunk( dataInput , chunkType , remainingChunkBytes ) );
				break;

			case KEY_VIEWPORT :
			default : // Ignore unknown chunks
				skipFully( dataInput , remainingChunkBytes );
		}
	}
}
