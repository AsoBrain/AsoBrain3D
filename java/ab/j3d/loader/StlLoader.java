/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.loader;

import java.io.*;
import java.util.regex.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * STL File Format
 *
 * An STL file is a triangular representation of a 3D surface geometry. The
 * surface is tessellated logically into a set of oriented triangles (facets).
 * Each facet is described by the unit outward normal and three points listed in
 * counterclockwise order representing the vertices of the triangle. While the
 * aspect ratio and orientation of individual facets is governed by the surface
 * curvature, the size of the facets is driven by the tolerance controlling the
 * quality of the surface representation in terms of the distance of the facets
 * from the surface. The choice of the tolerance is strongly dependent on the
 * target application of the produced STL file. In industrial processing, where
 * stereolithography machines perform a computer controlled layer by layer laser
 * curing of a photo-sensitive resin, the tolerance may be in order of 0.1 mm to
 * make the produced 3D part precise with highly worked out details. However
 * much larger values are typically used in pre-production STL prototypes, for
 * example for visualization purposes.
 *
 * The native STL format has to fulfill the following specifications:
 *
 * (i) The normal and each vertex of every facet are specified by three
 * coordinates each, so there is a total of 12 numbers stored for each facet.
 *
 * (ii) Each facet is part of the boundary between the interior and the exterior
 * of the object. The orientation of the facets (which way is ``out'' and which
 * way is ``in'') is specified redundantly in two ways which must be consistent.
 * First, the direction of the normal is outward. Second, the vertices are
 * listed in counterclockwise order when looking at the object from the outside
 * (right-hand rule).
 *
 * (iii) Each triangle must share two vertices with each of its adjacent
 * triangles. This is known as vertex-to-vertex rule.
 *
 * (iv) The object represented must be located in the all-positive octant (all
 * vertex coordinates must be positive).
 *
 * However, for non-native STL applications, the STL format can be generalized.
 * The normal, if not specified (three zeros might be used instead), can be
 * easily computed from the coordinates of the vertices using the right-hand
 * rule.
 *
 * Moreover, the vertices can be located in any octant. And finally, the facet
 * can even be on the interface between two objects (or two parts of the same
 * object). This makes the generalized STL format suitable for modelling of 3D
 * non-manifolds objects.
 *
 * The STL standard includes two data formats - ASCII and binary. While the
 * ASCII form is more descriptive, the binary form is far more common due to the
 * very large resulting size of the CAD data when saved in the ASCII format.
 * The first line in the ASCII format is a description line that must start with
 * the word ``solid'' in lower case, followed eventually by additional
 * information as the file name, author, date etc. The last line should be the
 * keyword ``endsolid''. The lines in between contain descriptions of individual
 * facets as
 *
 * facet normal 0.0 0.0 1.0
 * outer loop
 *    vertex  1.0  1.0  0.0
 *    vertex -1.0  1.0  0.0
 *    vertex  0.0 -1.0  0.0
 * endloop
 * endfacet
 *
 * Binary STL files consist of a 80 byte header line that can be interpreted as
 * a comment string. The following 4 bytes interpreted as a long integer give
 * the total number of facets. What follows is a normal and 3 vertices for each
 * facet, each coordinate represented as a 4 byte floating point number
 * (12 bytes in all). There is a 2 byte spacer between each facet. The result is
 * that each facet is represented by 50 bytes, 12 for the normal, 36 for the 3
 * vertices, and 2 for the spacer.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class StlLoader
{
	/**
	 * Regex pattern to recognize 'facet normal <normalX> <normalY> <normalZ>' line.
	 */
	private static final Pattern FACET_PATTERN = Pattern.compile( "\\s*facet\\s+normal\\s+([-+\\d\\.eE]+)\\s+([-+\\d\\.eE]+)\\s+([-+\\d\\.eE]+)\\s*" );

	/**
	 * Regex pattern to recognize 'vertex <x> <y> <z>' line.
	 */
	private static final Pattern VERTEX_PATTERN = Pattern.compile( "\\s*vertex\\s+([-+\\d\\.eE]+)\\s+([-+\\d\\.eE]+)\\s+([-+\\d\\.eE]+)\\s*" );

	/**
	 * Regex pattern to recognize 'endfacet' line.
	 */
	private static final Pattern ENDFACET_PATTERN = Pattern.compile( "\\s*endfacet\\s*" );

	/**
	 * Appearance to use for resulting 3D object.
	 */
	Appearance _appearance = BasicAppearances.ALUMINIUM;

	/**
	 * UV-map applied to loaded objects.
	 */
	@Nullable
	UVMap _uvMap = null;

	/**
	 * Whether face normals should be flipped.
	 */
	private boolean _flipNormals;

	/**
	 * Get appearance used for resulting 3D object.
	 *
	 * @return  Appearance used for resulting 3D object.
	 */
	public Appearance getAppearance()
	{
		return _appearance;
	}

	/**
	 * Set appearance to use for resulting 3D object.
	 *
	 * @param   appearance    Appearance to use for resulting 3D object.
	 */
	public void setAppearance( final Appearance appearance )
	{
		_appearance = appearance;
	}

	/**
	 * Returns the UV-map applied to loaded objects.
	 *
	 * @return  UV-map.
	 */
	@Nullable
	public UVMap getUvMap()
	{
		return _uvMap;
	}

	/**
	 * Sets the UV-map to be applied to loaded objects.
	 *
	 * @param   uvMap   UV-map to be used.
	 */
	public void setUvMap( @Nullable final UVMap uvMap )
	{
		_uvMap = uvMap;
	}

	/**
	 * Returns whether face normals should be flipped. This can be used to fix
	 * models that appear inside-out.
	 *
	 * @return  <code>true</code> to flip face normals.
	 */
	public boolean isFlipNormals()
	{
		return _flipNormals;
	}

	/**
	 * Sets whether face normals should be flipped. This can be used to fix
	 * models that appear inside-out.
	 *
	 * @param   flipNormals     <code>true</code> to flip face normals.
	 */
	public void setFlipNormals( final boolean flipNormals )
	{
		_flipNormals = flipNormals;
	}

	/**
	 * Load the specified STL file.
	 *
	 * @param   transform       Transormation to apply to the STL (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   in              Stream to read STL file from.
	 *
	 * @return  {@link Object3D} with loaded STL file.
	 *
	 * @throws  IOException if an error occured while loading the STL file.
	 */
	public Object3D load( final Matrix3D transform, @NotNull final InputStream in )
		throws IOException
	{
		final Object3DBuilder builder = new Object3DBuilder();
		final String objectName = load( builder, transform, in );
		final Object3D result = builder.getObject3D();
		result.setTag( objectName );
		return result;
	}

	/**
	 * Load the specified STL file.
	 *
	 * @param   builder         Builder of resulting 3D object.
	 * @param   transform       Transormation to apply to the STL (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   in              Stream to read STL file from.
	 *
	 * @return  Object name defined in STL file.
	 *
	 * @throws  IOException if an error occured while loading the STL file.
	 */
	public String load( @NotNull final Object3DBuilder builder, @NotNull final Matrix3D transform, @NotNull final InputStream in )
		throws IOException
	{
		final String result;

		final BufferedInputStream bufferedIn = new BufferedInputStream( in );
		final boolean isAscii = isAsciiFormat( bufferedIn );

		if ( isAscii )
		{
			result = loadAscii( builder, transform, new BufferedReader( new InputStreamReader( bufferedIn, "US-ASCII" ) ) );
		}
		else
		{
			result = loadBinary( builder, transform, bufferedIn );
		}

		return result;
	}

	/**
	 * Returns whether the given stream contains ASCII STL data.
	 *
	 * @param   in  Stream to read from. The current position is marked before
	 *              any bytes are read and restored afterwards.
	 *
	 * @return  <code>true</code> if the stream contains ASCII STL data.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private static boolean isAsciiFormat( final BufferedInputStream in )
		throws IOException
	{
		boolean result = false;
		try
		{
			final int limit = 1024;
			in.mark( limit );

			boolean magic = true;
			boolean header = true;

			for ( int i = 0; i < limit; i++ )
			{
				final int read = in.read();
				if ( read == -1 )
				{
					break;
				}
				final char c = (char)read;

				if ( magic )
				{
					/*
					 * Check magic number: "solid".
					 *
					 * Binary STL files should not start with this sequence,
					 * however we have encountered binary STL files that do.
					 */
					magic = false;
					if ( ( c != 's' ) || ( in.read() != (int)'o' ) || ( in.read() != (int)'l' ) || ( in.read() != (int)'i' ) || ( in.read() != (int)'d' ) )
					{
						break;
					}
				}
				else if ( header )
				{
					// Skip until newline.
					if ( c == '\r' || c == '\n' )
					{
						header = false;
					}
				}
				else
				{
					if ( c <= ' ' )
					{
						// Skip any whitespace and control characters.
					}
					else
					{
						// Check first word: 'facet' or 'endsolid'.
						result = ( c == 'f' ) ?  ( in.read() == (int)'a' ) && ( in.read() == (int)'c' ) && ( in.read() == (int)'e' ) && ( in.read() == (int)'t' ) :
						         ( c == 'e' ) && ( in.read() == (int)'n' ) && ( in.read() == (int)'d' ) && ( in.read() == (int)'s' ) && ( in.read() == (int)'o' ) && ( in.read() == (int)'l' ) && ( in.read() == (int)'i' ) && ( in.read() == (int)'d' );
						break;
					}
				}
			}
		}
		finally
		{
			in.reset();
		}

		return result;
	}

	/**
	 * Load the specified ASCII STL file.
	 *
	 * @param   builder         Builder of resulting 3D object.
	 * @param   transform       Transormation to apply to the STL (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   reader          Reader to read STL file from.
	 *
	 * @return  Object name defined in STL file.
	 *
	 * @throws  IOException if an error occured while loading the STL file.
	 */
	protected String loadAscii( @NotNull final Object3DBuilder builder, @NotNull final Matrix3D transform, @NotNull final BufferedReader reader )
		throws IOException
	{
		final Appearance appearance = _appearance;
		final UVMap uvMap = _uvMap;

		//noinspection MismatchedReadAndWriteOfArray
		final int[] vertexIndices = new int[ 3 ]; // read indirectly using '.clone()'

		int faceVertexIndex = 0;

		for ( String line = reader.readLine(); line != null; line = reader.readLine() )
		{
			Matcher matcher = FACET_PATTERN.matcher( line );
			if ( matcher.matches() )
			{
				faceVertexIndex = 3;
			}
			else
			{
				matcher = VERTEX_PATTERN.matcher( line );
				if ( matcher.matches() )
				{
					if ( faceVertexIndex == 0 )
					{
						throw new IOException( "Invalid facet defined in STL file. Vertex before facet or more than 3 vertices encountered for facet." );
					}

					try
					{
						final Vector3D point = transform.transform( Double.parseDouble( matcher.group( 1 ) ), Double.parseDouble( matcher.group( 2 ) ), Double.parseDouble( matcher.group( 3 ) ) );
						vertexIndices[ --faceVertexIndex ] = builder.getVertexIndex( point );
					}
					catch ( NumberFormatException e )
					{
						throw new IOException( "Malformed float value in STL file. Offending line: " + line );
					}
				}
				else
				{
					matcher = ENDFACET_PATTERN.matcher( line );
					if ( matcher.matches() )
					{
						final int vertexCount = 3 - faceVertexIndex;
						if ( vertexCount != 3 )
						{
							throw new IOException( "Invalid facet defined in STL file. Should have 3 vertices, but have " + vertexCount );
						}

						final int[] clone = new int[ 3 ];
						if ( _flipNormals )
						{
							clone[ 0 ] = vertexIndices[ 2 ];
							clone[ 1 ] = vertexIndices[ 1 ];
							clone[ 2 ] = vertexIndices[ 0 ];
						}
						else
						{
							clone[ 0 ] = vertexIndices[ 0 ];
							clone[ 1 ] = vertexIndices[ 1 ];
							clone[ 2 ] = vertexIndices[ 2 ];
						}

						builder.addFace( clone, appearance, uvMap, false, false, false );
						faceVertexIndex = 0;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Load the specified binary STL file.
	 *
	 * <p>See: <a href="http://en.wikipedia.org/wiki/STL_(file_format)">STL (file format) at Wikipedia</a>
	 *
	 * @param   builder         Builder of resulting 3D object.
	 * @param   transform       Transormation to apply to the STL (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   in              Stream to read STL file from.
	 *
	 * @return  Object name defined in STL file.
	 *
	 * @throws  IOException if an error occured while loading the STL file.
	 */
	public String loadBinary( @NotNull final Object3DBuilder builder, @NotNull final Matrix3D transform, @NotNull final InputStream in )
		throws IOException
	{
		final Appearance appearance = _appearance;
		final UVMap uvMap = _uvMap;

		final byte[] header = new byte[ 80 ];
		for ( int headerOffset = 0; headerOffset < header.length; )
		{
			final int readNow = in.read( header, headerOffset, header.length - headerOffset );
			if ( readNow < 0 )
			{
				throw new EOFException();
			}

			headerOffset += readNow;
		}

		try
		{
			final Vector3D[] points = new Vector3D[ 3 ];
			final Vector3D[] normals = new Vector3D[ 3 ];

			final long numberOfTriangles = readUnsignedInt32( in );
			for ( long i = 0L; i < numberOfTriangles; i++ )
			{
				final Vector3D normal = transform.rotate( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
				normals[ 0 ] = normal;
				normals[ 1 ] = normal;
				normals[ 2 ] = normal;

				// Convert to clockwise, as needed by 'addFace' used below.
				if ( _flipNormals )
				{
					points[ 0 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
					points[ 1 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
					points[ 2 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
				}
				else
				{
					points[ 0 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
					points[ 2 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
					points[ 1 ] = transform.transform( readFloat32( in ), readFloat32( in ), readFloat32( in ) );
				}

				/*
				 * Skip 'attribute byte count' which apparently does not
				 * specify a number of bytes that follows this 'count', but is
				 * used by some packages to store color information.
				 */
				readUnsignedInt16( in );

				builder.addFace( points, appearance, uvMap, normals, false, false, false );
			}
		}
		catch ( EOFException e )
		{
			// End of file reached.
		}

		final String name = new String( header, 6, 80 - 6 );
		return name.trim();
	}

	/**
	 * Read unsigned 16-bit integer from stream.
	 *
	 * @param   in  Stream to read from.
	 *
	 * @return  Value that was read.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private static int readUnsignedInt16( final InputStream in )
		throws IOException
	{
		final int ch1 = in.read();
		final int ch2 = in.read();

		if ( ( ch1 | ch2 ) < 0 )
		{
			throw new EOFException();
		}

		return ( ch2 << 8 ) + ch1;
	}

	/**
	 * Read unsigned 32-bit integer from stream.
	 *
	 * @param   in  Stream to read from.
	 *
	 * @return  Value that was read.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private static long readUnsignedInt32( final InputStream in )
		throws IOException
	{
		final int ch1 = in.read();
		final int ch2 = in.read();
		final int ch3 = in.read();
		final int ch4 = in.read();

		if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
		{
			throw new EOFException();
		}

		return ( (long)ch4 << 24 ) + ( (long)ch3 << 16 ) + ( (long)ch2 << 8 ) + (long)ch1;
	}

	/**
	 * Read 32-bit floating-point value from stream.
	 *
	 * @param   in  Stream to read from.
	 *
	 * @return  Value that was read.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private static double readFloat32( final InputStream in )
		throws IOException
	{
		final int ch1 = in.read();
		final int ch2 = in.read();
		final int ch3 = in.read();
		final int ch4 = in.read();

		if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
		{
			throw new EOFException();
		}

		return (double) Float.intBitsToFloat( ( ch4 << 24 ) + ( ch3 << 16 ) + ( ch2 << 8 ) + ch1 );
	}
}
