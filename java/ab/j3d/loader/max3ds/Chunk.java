/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Base class of all chunks.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
abstract class Chunk
{
	/*
	 * Chunk type definitions.
	 */
	protected static final int MAIN_3DS             = 0x4D4D;
	protected static final int NULL_CHUNK           = 0x0000;
	protected static final int UNKNOWN1             = 0x0001;
	protected static final int TDS_VERSION          = 0x0002;
	protected static final int COLOR_FLOAT          = 0x0010;
	protected static final int COLOR_BYTE           = 0x0011;
	protected static final int CLR_BYTE_GAMA        = 0x0012;
	protected static final int CLR_FLOAT_GAMA       = 0x0013;
	protected static final int PRCT_INT_FRMT        = 0x0030;
	protected static final int PRCT_FLT_FRMT        = 0x0031;
	protected static final int MASTER_SCALE         = 0x0100;
	protected static final int BACKGRD_BITMAP       = 0x1100;
	protected static final int BACKGRD_COLOR        = 0x1200;
	protected static final int USE_BCK_COLOR        = 0x1201;
	protected static final int V_GRADIENT           = 0x1300;
	protected static final int SHADOW_BIAS          = 0x1400;
	protected static final int SHADOW_MAP_SIZE      = 0x1420;
	protected static final int SHADOW_MAP_RANGE     = 0x1450;
	protected static final int RAYTRACE_BIAS        = 0x1460;
	protected static final int O_CONSTS             = 0x1500;
	protected static final int GEN_AMB_COLOR        = 0x2100;
	protected static final int FOG_FLAG             = 0x2200;
	protected static final int FOG_BACKGROUND       = 0x2210;
	protected static final int DISTANCE_QUEUE       = 0x2300;
	protected static final int LAYERED_FOG_OPT      = 0x2302;
	protected static final int DQUEUE_BACKGRND      = 0x2310;
	protected static final int DEFAULT_VIEW         = 0x3000;
	protected static final int VIEW_CAMERA          = 0x3080;
	protected static final int EDIT_3DS             = 0x3D3D;
	protected static final int MESH_VERSION         = 0x3D3E;
	protected static final int NAMED_OBJECT         = 0x4000;
	protected static final int OBJ_TRIMESH          = 0x4100;
	protected static final int VERTEX_LIST          = 0x4110;
	protected static final int VERTEX_OPTIONS       = 0x4111;
	protected static final int FACES_ARRAY          = 0x4120;
	protected static final int MESH_MAT_GROUP       = 0x4130;
	protected static final int TEXT_COORDS          = 0x4140;
	protected static final int SMOOTH_GROUP         = 0x4150;
	protected static final int COORD_SYS            = 0x4160;
	protected static final int MESH_COLOR           = 0x4165;
	protected static final int MESH_TEXTURE_INFO    = 0x4170;
	protected static final int LIGHT_OBJ            = 0x4600;
	protected static final int LIGHT_SPOTLIGHT      = 0x4610;
	protected static final int LIGHT_ATTENU_ON      = 0x4625;
	protected static final int LIGHT_SPOT_SHADOWED  = 0x4630;
	protected static final int LIGHT_LOC_SHADOW     = 0x4641;
	protected static final int LIGHT_SEE_CONE       = 0x4650;
	protected static final int LIGHT_SPOT_OVERSHOOT = 0x4652;
	protected static final int LIGHT_SPOT_ROLL      = 0x4656;
	protected static final int LIGHT_SPOT_BIAS      = 0x4658;
	protected static final int LIGHT_IN_RANGE       = 0x4659;
	protected static final int LIGHT_OUT_RANGE      = 0x465A;
	protected static final int LIGHT_MULTIPLIER     = 0x465B;
	protected static final int CAMERA_FLAG          = 0x4700;
	protected static final int CAMERA_RANGES        = 0x4720;
	protected static final int KEY_VIEWPORT         = 0x7001;
	protected static final int VIEWPORT_DATA        = 0x7011;
	protected static final int VIEWPORT_DATA3       = 0x7012;
	protected static final int VIEWPORT_SIZE        = 0x7020;
	protected static final int XDATA_SECTION        = 0x8000;
	protected static final int MAT_NAME             = 0xA000;
	protected static final int MAT_AMB_COLOR        = 0xA010;
	protected static final int MAT_DIF_COLOR        = 0xA020;
	protected static final int MAT_SPEC_CLR         = 0xA030;
	protected static final int MAT_SHINE            = 0xA040;
	protected static final int MAT_SHINE_STR        = 0xA041;
	protected static final int MAT_ALPHA            = 0xA050;
	protected static final int MAT_ALPHA_FAL        = 0xA052;
	protected static final int MAT_REF_BLUR         = 0xA053;
	protected static final int MAT_TWO_SIDED        = 0xA081;
	protected static final int MAT_SELF_ILUM        = 0xA084;
	protected static final int MAT_WIREFRAME_ON     = 0xA085;
	protected static final int MAT_WIRE_SIZE        = 0xA087;
	protected static final int IN_TRANC_FLAG        = 0xA08A;
	protected static final int MAT_SOFTEN           = 0xA08C;
	protected static final int MAT_WIRE_ABS         = 0xA08E;
	protected static final int MAT_SHADING          = 0xA100;
	protected static final int TEXMAP_ONE           = 0xA200;
	protected static final int MAT_REFLECT_MAP      = 0xA220;
	protected static final int MAT_FALLOFF          = 0xA240;
	protected static final int MAT_TEX_BUMP_PER     = 0xA252;
	protected static final int MAT_TEX_BUMPMAP      = 0xA230;
	protected static final int MAT_REFL_BLUR        = 0xA250;
	protected static final int MAT_TEXNAME          = 0xA300;
	protected static final int MAT_SXP_TEXT_DATA    = 0xA320;
	protected static final int MAT_SXP_BUMP_DATA    = 0xA324;
	protected static final int MAT_TEX2MAP          = 0xA33A;
	protected static final int MAT_TEX_FLAGS        = 0xA351;
	protected static final int MAT_TEX_BLUR         = 0xA353;
	protected static final int TEXTURE_V_SCALE      = 0xA354;
	protected static final int TEXTURE_U_SCALE      = 0xA356;
	protected static final int MAT_BLOCK            = 0xAFFF;
	protected static final int KEYFRAMES            = 0xB000;
	protected static final int KEY_AMB_LI_INFO      = 0xB001;
	protected static final int KEY_OBJECT           = 0xB002;
	protected static final int KEY_CAMERA_OBJECT    = 0xB003;
	protected static final int KEY_CAM_TARGET       = 0xB004;
	protected static final int KEY_OMNI_LI_INFO     = 0xB005;
	protected static final int KEY_SPOT_TARGET      = 0xB006;
	protected static final int KEY_SPOT_OBJECT      = 0xB007;
	protected static final int KEY_SEGMENT          = 0xB008;
	protected static final int KEY_CURTIME          = 0xB009;
	protected static final int KEY_HEADER           = 0xB00A;
	protected static final int TRACK_HEADER         = 0xB010;
	protected static final int INSTANCE_NAME        = 0xB011;
	protected static final int TRACK_PIVOT          = 0xB013;
	protected static final int BOUNDING_BOX         = 0xB014;
	protected static final int MORPH_SMOOTH         = 0xB015;
	protected static final int TRACK_POS_TAG        = 0xB020;
	protected static final int TRACK_ROT_TAG        = 0xB021;
	protected static final int TRACK_SCL_TAG        = 0xB022;
	protected static final int KEY_FOV_TRACK        = 0xB023;
	protected static final int KEY_ROLL_TRACK       = 0xB024;
	protected static final int KEY_COLOR_TRACK      = 0xB025;
	protected static final int KEY_HOTSPOT_TRACK    = 0xB027;
	protected static final int KEY_FALLOFF_TRACK    = 0xB028;
	protected static final int NODE_ID              = 0xB030;

	/**
	 * Construct chunk from data input.
	 *
	 * @param   dataInput           Data input source.
	 * @param   chunkType           This chunk's type.
	 * @param   remainingChunkBytes Remaining number of unread bytes in this chunk.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected Chunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
//		System.out.println( this + "() - type=0x" + Integer.toHexString( chunkType ) + ", remainingChunkBytes=" + remainingChunkBytes );
		processChunk( dataInput , chunkType , remainingChunkBytes );
	}

	/**
	 * Read chunk data and process its sub chunks.
	 *
	 * @param   dataInput           Data input source.
	 * @param   chunkType           This chunk's type.
	 * @param   remainingChunkBytes Remaining number of unread bytes in this chunk.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected void processChunk( final DataInput dataInput, final int chunkType , final int remainingChunkBytes )
		throws IOException
	{
//		System.out.println( "  Chunk.processChunk( ... , chunkType=0x" + Integer.toHexString( chunkType ) + " , remainingChunkBytes=" + remainingChunkBytes + " )" );
		int todo = remainingChunkBytes;
		while ( todo > 0 )
		{
			final int childType = dataInput.readUnsignedShort();
			final int childSize = dataInput.readInt();

			if ( childSize > todo )
			{
				throw new IOException( "Header length doesn't match up: End ID#:" + Integer.toHexString( childType ) + " len left to read=" + todo + " parentID#=" + Integer.toHexString( chunkType ) );
			}

//			System.out.println( "    processChildChunk( ... , chunkType=0x" + Integer.toHexString( childType ) + " , remainingChunkBytes=" + ( childSize - 6 ) + " )" );
			processChildChunk( dataInput , childType , childSize - 6 );

			todo -= childSize;
		}
	}

	/**
	 * Process a child chunk.
	 *
	 * @param   dataInput           Data input source.
	 * @param   chunkType           Child's chunk type.
	 * @param   remainingChunkBytes Remaining number of unread bytes in child chunk.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected abstract void processChildChunk( final DataInput dataInput , final int chunkType , final int remainingChunkBytes )
		throws IOException;

	/**
	 * Skip number of data bytes.
	 *
	 * @param   dataInput       Data input source.
	 * @param   numberOfBytes   Number of bytes to skip.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected static void skipFully( final DataInput dataInput , final int numberOfBytes )
		throws IOException
	{
		int remainder = numberOfBytes;
		while ( remainder > 0 )
		{
			final int skipped = dataInput.skipBytes( remainder );
			if ( skipped <= 0 )
				throw new EOFException();

			remainder -= skipped;
		}
	}

	/**
	 * Read 0-terminated string with variable data length The number of bytes
	 * read is the length of the returned string + 1.
	 *
	 * @param   dataInput   Data input source.
	 *
	 * @return  String that was read.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected static String readCString( final DataInput dataInput )
		throws IOException
	{
		final StringBuilder sb = new StringBuilder();

		for ( byte b = dataInput.readByte() ; b != (byte)0 ; b = dataInput.readByte() )
		{
			sb.append( (char)b );
		}

		return sb.toString();
	}

	/**
	 * Read 0-terminated string with fixed data length. The number of bytes read
	 * is always <code>numberOfBytes</code>.
	 *
	 * @param   dataInput       Data input source.
	 * @param   numberOfBytes   Number of bytes to read.
	 *
	 * @return  String that was read.
	 *
	 * @throws  IOException if a read error occured.
	 */
	protected static String readCString( final DataInput dataInput , final int numberOfBytes )
		throws IOException
	{
		final byte[] bytes = new byte[ numberOfBytes ];
		dataInput.readFully( bytes );

		return new String( bytes , 0 , numberOfBytes - 1 );
	}
}
