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
 * This chunk specifies a list of faces for a mesh.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class FaceList
	extends HierarchyChunk
{
	/**
	 * Array of faces in list.
	 */
	private Triangle[] _faces = null;

	public static final class FaceMaterial
		extends DataChunk
	{
		public String name;
		public int[]  faces;

		/**
		 * Constructor of Chunk with ChunkID to be used when the Chunk is read from
		 * inputstream.
		 *
		 * @param id ID of the chunk.
		 */
		public FaceMaterial( final int id )
		{
			super( id );
			if ( Ab3dsFile.DEBUG ) System.out.println( "  - Reading face materials" );
		}

		public FaceMaterial( final String name )
		{
			this( TRI_MATERIAL );
			this.name = name;
		}

		public long getSize()
		{
			return HEADER_SIZE + STRING_SIZE( name ) + INT_SIZE + faces.length * INT_SIZE;
		}

		public void read( final Ab3dsInputStream is )
			throws IOException
		{
			readHeader( is );

			name = is.readString();
			if ( Ab3dsFile.DEBUG )
				System.out.println( "  Material = " + name );

			faces = new int[ is.readInt() ];
			for ( int i = 0; i < faces.length; i++ )
				faces[ i ] = is.readInt();
		}

		public void write( final Ab3dsOutputStream os )
			throws IOException
		{
			writeHeader( os );

			os.writeString( name );

			os.writeInt( faces.length );
			for ( int i = 0; i < faces.length; i++ )
				os.writeInt( faces[ i ] );
		}

		public void set( final int[] faces )
		{
			this.faces = faces;
		}
	}

	/**
	 * This class defines a tringular face.
	 */
	public static final class Triangle
	{
		/**
		 * Vertex index for first corner of triangle.
		 */
		private final int v1;

		/**
		 * Vertex index for second corner of triangle.
		 */
		private final int v2;

		/**
		 * Vertex index for third corner of triangle.
		 */
		private final int v3;

		private final int info;

		public Triangle( final int v1 , final int v2 , final int v3 , final int info )
		{
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.info = info;
		}

		public int getVertex( final int index )
		{
			switch( index )
			{
				case 1 : return v1;
				case 2 : return v2;
				case 3 : return v3;
			}
			throw new RuntimeException( "Face always has 3 vertices (index 1..3)" );
		}
	}

	/**
	 * No-op constructor for generation purposes.
	 */
	public FaceList()
	{
		this( TRI_FACEL1 );
	}

	/**
	 * Constructor of Chunk with ChunkID to be used
	 * when the Chunk is read from inputstream.
	 *
	 * @param   id      ID of the chunk.
	 */
	public FaceList( final int id )
	{
		super( id );

		if ( Ab3dsFile.DEBUG )
			System.out.println( "  - Reading face list" );
	}

	/**
	 * Get face at specified index.
	 *
	 * @param   index   Index of Face to get.
	 *
	 * @return  Face at specified index.
	 */
	public Triangle getFace( final int index )
	{
		return _faces[index];
	}

	/**
	 * Get total number of faces in list.
	 *
	 * return   Number of faces in list.
	 */
	public int getFaceCount()
	{
		return _faces.length;
	}

	/**
	 * Set all faces at once.
	 *
	 * @param   faces   Array of new faces.
	 */
	public void set( final Triangle[] faces )
	{
		_faces = faces;
	}

	public long getSize()
	{
		return super.getSize() + INT_SIZE + _faces.length * 4 * INT_SIZE;
	}

	public void read( final Ab3dsInputStream is )
		throws IOException
	{
		readHeader( is );

		final int faceCount = is.readInt();

		_faces = new Triangle[ faceCount ];

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final int v1   = is.readInt();
			final int v2   = is.readInt();
			final int v3   = is.readInt();
			final int info = is.readInt();

			_faces[ i ] = new Triangle( v1 , v2 , v3 , info );
		}

		readSubChunks( is );
	}

	public void write( final Ab3dsOutputStream os )
		throws IOException
	{
		if ( Ab3dsFile.DEBUG )
			System.out.println( "  - Writing face list" );

		writeHeader( os );

		os.writeInt( _faces.length );

		for ( int i = 0 ; i < _faces.length ; i++ )
		{
			os.writeInt( _faces[i].v1 );
			os.writeInt( _faces[i].v2 );
			os.writeInt( _faces[i].v3 );
			os.writeInt( _faces[i].info );
		}

		writeSubChunks( os );
	}

}
