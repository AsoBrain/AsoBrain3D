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
 * This is the base class for all 3ds file chunks.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public abstract class Chunk
{
	/**
	 * ID of Chunk.
	 */
	private final int _id;

	/**
	 * Position and size in the 3ds file.
	 */
	protected long _chunkStart;
	protected long _chunkEnd;
	protected long _chunkSize;

	/**
	 * Size definitions.
	 */
	public static final int BYTE_SIZE = 1;
	public static final int INT_SIZE  = 2;
	public static final int LONG_SIZE = 4;
	public static final int FLOAT_SIZE= 4;
	public static final int BOOLEAN_SIZE= 1;

	public static final long HEADER_SIZE = INT_SIZE + LONG_SIZE;

	/*
	 * Main chunk
	 */
	public static final int MAIN3DS 		= 0x4D4D;

	/*
	 * Editor/Viewport chucks
	 */
	public static final int EDIT3DS 		= 0x3D3D;
	public static final int EDIT_CONFIG1	= 0x0100;
	public static final int EDIT_CONFIG2	= 0x3D3E;
	public static final int EDIT_BACKGR		= 0x1200;
	public static final int EDIT_AMBIENT	= 0x2100;
	public static final int EDIT_VIEW		= 0x7001;
	public static final int EDIT_VIEW_P2	= 0x7011;
	public static final int EDIT_VIEW_P1	= 0x7012;
	public static final int EDIT_VIEW_P3	= 0x7020;

	public static final int TOP				= 0x0001;
	public static final int BOTTOM			= 0x0002;
	public static final int LEFT			= 0x0003;
	public static final int RIGHT			= 0x0004;
	public static final int FRONT			= 0x0005;
	public static final int BACK			= 0x0006;
	public static final int USER			= 0x0007;
	public static final int LIGHT			= 0x0009;
	//public static final int DISABLED		= 0x0010;
	//public static final int BOGUS			= 0x0011;
	public static final int RGB_FLOAT		= 0x0010;
	public static final int RGB_BYTE		= 0x0011;
	public static final int RGB_BYTE_GAMMA  = 0x0012;
	public static final int RGB_FLOAT_GAMMA = 0x0013;
	public static final int DOUBLE_BYTE		= 0x0030;
	public static final int CAMERA			= 0xFFFF;

	/*
	 * Material library chunks
	 */
	public static final int EDIT_MATERIAL	= 0xAFFF;

		public static final int MAT_NAME		 = 0xA000;
		public static final int MAT_AMBIENT		 = 0xA010;
		public static final int MAT_DIFFUSE		 = 0xA020;
		public static final int MAT_SPECULAR	 = 0xA030;
		public static final int MAT_SHININESS	 = 0xA040;
		public static final int MAT_SHINSTRENGTH = 0xA041;
		public static final int MAT_TRANSPARENCY = 0xA050;
		public static final int MAT_TRANSFALLOFF = 0xA052;
		public static final int MAT_REFLECTBLUR  = 0xA053;
		public static final int MAT_TWO_SIDED    = 0xA081;
		public static final int MAT_TYPE		 = 0xA100;
		public static final int MAT_ILLUMINATION = 0xA084;
		public static final int MAT_WIRETHICKNESS= 0xA087;

		public static final int MAT_TEXT1_MAP	= 0xA200;
		public static final int MAT_TEXT2_MAP	= 0xA33A;
		public static final int MAT_OPACITY_MAP	= 0xA210;
		public static final int MAT_BUMP_MAP	= 0xA230;
		public static final int MAT_SPECULAR_MAP= 0xA204;
		public static final int MAT_SHINI_MAP	= 0xA33C;
		public static final int MAT_ILLUM_MAP	= 0xA33D;
		public static final int MAT_REFLECT_MAP	= 0xA220;

		public static final int MAT_TEXT1_MASK	= 0xA33E;
		public static final int MAT_TEXT2_MASK	= 0xA340;
		public static final int MAT_OPACITY_MASK= 0xA342;
		public static final int MAT_BUMP_MASK	= 0xA344;
		public static final int MAT_SPECULAR_MASK= 0xA348;
		public static final int MAT_SHINI_MASK	= 0xA346;
		public static final int MAT_ILLUM_MASK	= 0xA34A;
		public static final int MAT_REFLECT_MASK= 0xA34C;

			public static final int MAP_PATH		= 0xA300;
			public static final int MAP_OPTIONS		= 0xA351;
			public static final int MAP_BLUR		= 0xA353;
			public static final int MAP_U_SCALE		= 0xA354;
			public static final int MAP_V_SCALE		= 0xA356;
			public static final int MAP_U_OFFSET	= 0xA358;
			public static final int MAP_V_OFFSET	= 0xA35A;
			public static final int MAP_ROTATION	= 0xA35C;

	///*
	 //* Unknown chunks.
	 //*/
	//public static final int EDIT_UNKNWN01	= 0x1100;
	//public static final int EDIT_UNKNWN02	= 0x1201;
	//public static final int EDIT_UNKNWN03	= 0x1300;
	//public static final int EDIT_UNKNWN04	= 0x1400;
	//public static final int EDIT_UNKNWN05	= 0x1420;
	//public static final int EDIT_UNKNWN06	= 0x1450;
	//public static final int EDIT_UNKNWN07	= 0x1500;
	//public static final int EDIT_UNKNWN08	= 0x2200;
	//public static final int EDIT_UNKNWN09	= 0x2201;
	//public static final int EDIT_UNKNWN10	= 0x2210;
	//public static final int EDIT_UNKNWN11	= 0x2300;
	//public static final int EDIT_UNKNWN12	= 0x2302;
	//public static final int EDIT_UNKNWN13	= 0x2000;
	//public static final int EDIT_UNKNWN14	= 0xAFFF;

	///*
	 //* Keyframer chunks.
	 //*/
	//public static final int KEYF3DS			= 0xB000;
	//public static final int KEYF_UNKNWN01	= 0xB00A;
	//public static final int KEYF_FRAMES		= 0xB008;
	//public static final int KEYF_UNKNWN02	= 0xB009;
	//public static final int KEYF_OBJDES		= 0xB002;
	//public static final int KEYF_OBJHIERARCH 	= 0xB010;
	//public static final int KEYF_OBJDUMMYNAME 	= 0xB011;
	//public static final int KEYF_OBJUNKNWN01= 0xB013;
	//public static final int KEYF_OBJUNKNWN02= 0xB014;
	//public static final int KEYF_OBJUNKNWN03= 0xB015;
	//public static final int KEYF_OBJPIVOT	= 0xB020;
	//public static final int KEYF_OBJUNKNWN04= 0xB021;
	//public static final int KEYF_OBJUNKNWN05= 0xB022;

	/*
	 * Object chunks
	 */
	public static final int EDIT_OBJECT		= 0x4000;

		public static final int OBJ_TRIMESH		= 0x4100;
			public static final int TRI_VERTEXLIST	= 0x4110;
			public static final int TRI_VERT_OPTIONS= 0x4111;
			public static final int TRI_FACEL1		= 0x4120;
			public static final int TRI_MATERIAL	= 0x4130;
			public static final int TRI_MAP_COORDS  = 0x4140;
			public static final int TRI_SMOOTH		= 0x4150;
			public static final int TRI_LOCAL		= 0x4160;
			public static final int TRI_VISIBLE		= 0x4165;
			public static final int TRI_MAP_STAND 	= 0x4170;

		public static final int OBJ_LIGHT		= 0x4600;
			public static final int LIT_SPOT		= 0x4610;
			public static final int LIT_OFF			= 0x4620;
			public static final int LIT_RAY			= 0x4627;
			public static final int LIT_CAST        = 0x4630;
			public static final int LIT_OUT_RANGE	= 0x465A;
			public static final int LIT_IN_RANGE	= 0x4659;
			public static final int LIT_MULTIPLIER  = 0x465B;
			public static final int LIT_ROLL        = 0x4656;
			public static final int LIT_RAY_BIAS	= 0x4658;


		public static final int OBJ_CAMERA		= 0x4700;
			public static final int CAM_UNKNWN01	= 0x4710;
			public static final int CAM_UNKNWN02	= 0x4720;

		public static final int OBJ_UNKNWN01	= 0x4710;
		public static final int OBJ_UNKNWN02	= 0x4720;


	/**
	 * Constructor of Chunk with ChunkID.
	 *
	 * @param   id      ID of the chunk.
	 */
	public Chunk( final int id )
	{
		_id = id;
	}

	/**
	 * Returns the size in bytes of a string.
	 *
	 * @param   str		the string to get size for.
	 *
	 * @return  the size in bytes of the String.
	 */
	public static final int STRING_SIZE( final String str )
	{
		return str.length() + 1;
	}

	/**
	 * Create a new chunk with specified ID,
	 * this is used when reading from input stream.
	 * This method figures out what the chunk is.
	 *
	 * @param   id      ID of the Chunk
	 *
	 * @return  the new Chunk with specified ID.
	 */
	public static Chunk createChunk( final int id, final Chunk parent )
	{
		switch ( id )
		{
			case EDIT_OBJECT	: return new ObjectChunk( id );
			case TRI_VERTEXLIST : return new VertexList( id );
			case TRI_FACEL1 	: return new FaceList( id );
			case TRI_MATERIAL 	: return new FaceList.FaceMaterial( id );
			case TRI_MAP_STAND 	: return new StandardMapping( id );
			case TRI_MAP_COORDS : return new MappingCoordinates( id );
			case TRI_SMOOTH     : return new SmoothingGroups( parent );
			case OBJ_LIGHT		: return new Ab3dsLight( id );
			case LIT_SPOT		: return new Ab3dsLight.SpotLight( id );
			case LIT_OFF		: return new Ab3dsLight.Off( id );
			case LIT_RAY		: return new EmptyChunk( id );
			case LIT_CAST		: return new EmptyChunk( id );
			case LIT_IN_RANGE 	:
			case LIT_OUT_RANGE 	:
			case LIT_MULTIPLIER :
			case LIT_RAY_BIAS 	:
			case LIT_ROLL 		: return new FloatChunk( id );
			case OBJ_CAMERA		: return new Ab3dsCamera();
			case RGB_FLOAT		: return new Ab3dsRGB( id , true, false );
			case RGB_BYTE		: return new Ab3dsRGB( id , false, false );
			case RGB_BYTE_GAMMA : return new Ab3dsRGB( id , false, true );
			case RGB_FLOAT_GAMMA: return new Ab3dsRGB( id , false, true );

			case EDIT_MATERIAL	: return new Ab3dsMaterial( id );
			case MAT_TEXT1_MAP	:
			case MAT_TEXT2_MAP 	:
			case MAT_OPACITY_MAP :
			case MAT_BUMP_MAP 	:
			case MAT_SPECULAR_MAP :
			case MAT_SHINI_MAP 	:
			case MAT_ILLUM_MAP 	:
			case MAT_REFLECT_MAP : return new TextureMap( id );

			case OBJ_TRIMESH  	:
			case MAIN3DS		:
//			case KEYF3DS		:
			//case EDIT_CONFIG1 :
			//case EDIT_CONFIG2 :
			case EDIT3DS		: return new HierarchyChunk( id );
		}

		return new UnknownChunk( id );
	}

	/**
	 * Get the byte as Hex string.
	 *
	 * @param   dec	decimal to get hex for.
	 *
	 * @return  dec as Hex String.
	 */
	public static String getHex( final byte dec )
	{
		String hex = Integer.toHexString( (int)dec );
		while ( hex.length() < 2 ) hex = "0" + hex;
		return hex;
	}

	/**
	 * Get the int as Hex string.
	 *
	 * @param   dec	decimal to get hex for.
	 *
	 * @return  dec as Hex String.
	 */
	public static String getHex( final int dec )
	{
		String hex = Integer.toHexString( dec );
		while ( hex.length() < 4 ) hex = "0" + hex;
		return hex;
	}

	/**
	 * Get the long as Hex string.
	 *
	 * @param   dec	decimal to get hex for.
	 *
	 * @return  dec as Hex String.
	 */
	public static String getHex( final long dec )
	{
		String hex = Long.toHexString( dec );
		while( hex.length() < 8 ) hex = "0" + hex;
		return hex;
	}

	/**
	 * Gets the ID of the Chunk.
	 *
	 * @return  the ID of this chunk.
	 */
	public final int getID()
	{
		return _id;
	}

	/**
	 * Returns the size in bytes of the chunk.
	 *
	 * @return  the size of the chunk in bytes.
	 */
	public abstract long getSize();

	/**
	 * Reads the chunk from the input stream.
	 *
	 * @param   is	the stream to read from.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public abstract void read( Ab3dsInputStream is ) throws IOException;

	/**
	 * @param   is	the stream to read from.
	 * @param   fp	filepointer to the current position in stream.
	 *
	 * @return  filepointer at point of return.
	 */
	public final void readHeader( final Ab3dsInputStream is )
		throws IOException
	{
		_chunkStart = is.getPointer() - 2;
		_chunkSize  = is.readLong();
		if ( _chunkSize == 0L )
		{
			throw new IOException( "illegal chunk size: " + _chunkSize );
		}
		_chunkEnd   = _chunkStart + _chunkSize;
	}

	/**
	 * Returns a String representation of this chunk.
	 *
	 * @return  this chunk as a string.
	 */
	public String toString()
	{
		return getClass().getSimpleName() + " id="+getHex(_id);
	}

	/**
	 * Writes the chunk the output stream.
	 *
	 * @param   os	the stream to write to.
	 *
	 * @throws IOException when an io error occurred.
	 */
	public abstract void write( Ab3dsOutputStream os ) throws IOException;

	/**
	 * @param   os  Stream to write to.
	 */
	public final void writeHeader( final Ab3dsOutputStream os )
		throws IOException
	{
		if ( Ab3dsFile.DEBUG )
			System.out.println( "Write chunk : " + getHex( getID() ) + "   size = " + getSize() );
		os.writeInt( getID() );
		os.writeLong( getSize() );
	}

}
