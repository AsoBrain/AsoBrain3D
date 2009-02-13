/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Object3DBuilder;
import ab.j3d.model.Transform3D;

import com.numdata.oss.TextTools;

/**
 * Loader for 3D Studio or 3D Studio MAX (<code>.3DS</colorMap>) files.
 * <p>
 * A <code>.3DS</code> file consists of chunks, each starting with an ID and the
 * chunk length. This allows easy skipping of unrecognized chunks.
 * <dl>
 *  <dt>NOTE:</dt>
 *  <dd>
 *   This class has been written for loading efficiency. Please use the classes
 *   in the <strong><code>ab.j3d.a3ds</code> package</strong> if you need to
 *   write or process <code>.3DS</code> files in any other way.
 *  </dd>
 * </dl>
 *
 * @FIXME   Add support for lights.
 * @FIXME   Add support for unit set in file.
 * @FIXME   Properly handle material properties.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Max3DSLoader
{
	/**
	 * Root of scene graph to which geometry is added.
	 */
	private final Node3D _root;

	/**
	 * URL of location from which supplemental files (e.g. textures) may be
	 * loaded. This may be <code>null</code> if no such location is available.
	 */
	private URL _supplementURL;

	/**
	 * Maps material names ({@link String}) to {@link Material} objects
	 * representing the materials in the <code>.3DS</code> file.
	 */
	private final Map<String,Material> _materials = new HashMap();

	/**
	 * 3D object currently being constructed.
	 */
	private Object3DBuilder _object3DBuilder;

	/**
	 * Texture for material currently being contructed/used.
	 */
	private Material _material;

	/**
	 * Temporary texture coordinates array.
	 */
	private Point2D.Float[] _textureCoords;

	/**
	 * Load a scene graph from the contents of a 3D Studio or 3D Studio MAX
	 * (<code>.3DS</code>) file.
	 *
	 * @param   transform       Optional transormation to apply to the file
	 *                          (typically used to scaling and axis alignment).
	 * @param   file            The <code>.3DS</code> file to read.
	 *
	 * @return  A {@link Node3D} with the loaded file.
	 *
	 * @throws  FileNotFoundException if the file is not accessible.
	 * @throws  IOException if an error occured while loading the file.
	 * @throws  SecurityException if a security manager prevents file access.
	 */
	public static Node3D load( final Matrix3D transform , final File file )
		throws IOException
	{
		final FileInputStream in = new FileInputStream( file );
		try
		{
			URL supplementURL = null;
			try
			{
				File directory = file.getParentFile();
				if ( directory == null )
				{
					final File canonicalFile = file.getCanonicalFile();
					directory = canonicalFile.getParentFile();
				}

				supplementURL = directory.toURL();
			}
			catch ( IOException e )
			{
				/* ignore errors related to getting the supplemental directory */
			}
			catch ( SecurityException e )
			{
				/* ignore errors related to getting the supplemental directory */
			}

			return load( transform , supplementURL , in );
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * Load a scene graph from the contents of a 3D Studio or 3D Studio MAX
	 * (<code>.3DS</code>) file.
	 *
	 * @param   transform       Optional transormation to apply to the file
	 *                          (typically used to scaling and axis alignment).
	 * @param   supplementURL   URL of location from which supplemental files
	 *                          (e.g. textures) may be loaded (<code>null</code>
	 *                          if no such location is available).
	 * @param   in              Stream to load the <code>.3DS</code> file from.
	 *
	 * @return  A {@link Node3D} with the loaded file.
	 *
	 * @throws  IOException if an error occured while loading the file.
	 */
	public static Node3D load( final Matrix3D transform , final URL supplementURL , final InputStream in )
		throws IOException
	{
		final Node3D result = ( transform == null || Matrix3D.INIT.equals( transform ) ) ? new Transform3D( transform ) : new Node3D();

		final Max3DSLoader loader = new Max3DSLoader( result , supplementURL );
		while ( true )
		{
			final int chunkID;
			final int chunkLength;
			try
			{
				chunkID     = readShort( in );
				chunkLength = readLong( in );
			}
			catch ( EOFException e )
			{
				/*
				 * Stop reading when the end-of-file is reached while trying to
				 * read the next chunk's ID and length. Don't know how to detect
				 * the end otherwise.
				 */
				break;
			}

			loader.readChunkData( in , chunkID , chunkLength );
		}

		return result;
	}

	/**
	 * Internal constructor for loader.
	 *
	 * @param   root            Root of scene graph to add geometry to.
	 * @param   supplementURL   URL of location from which supplemental files
	 *                          (e.g. textures) may be loaded (<code>null</code>
	 *                          if no such location is available).
	 */
	private Max3DSLoader( final Node3D root , final URL supplementURL )
	{
		_root = root;
		_supplementURL = supplementURL;
		_object3DBuilder = null;
		_material = null;
		_textureCoords = null;
	}

	/**
	 * Read a chunk and recursively process its sub chunks, if applicable.
	 *
	 * @param   in      Stream to read the next chunk from.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private void readChunk( final InputStream in )
		throws IOException
	{
		final int chunkID     = readShort( in );
		final int chunkLength = readLong( in );

		readChunkData( in , chunkID , chunkLength );
	}

	/**
	 * Read chunk data and recursively process its sub chunks, if applicable.
	 *
	 * @param   in              Stream to read the chunk data from.
	 * @param   chunkID         ID of chunk to read.
	 * @param   chunkLength     Length of chunk to read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private void readChunkData( final InputStream in , final int chunkID , final int chunkLength )
		throws IOException
	{
//		System.out.println( "Process chunk ID 0x" + Integer.toHexString( chunkID ) );
		switch ( chunkID )
		{

			/**** MAIN/STRUCTURAL CHUNKS *************************************/

			case 0x3D3D : // MMAGIC - Editor data
				{
					// System.out.println( "### Start of editor data" );
					readChunk( in );
				}
				break;

			case 0x4D4D : // M3DMAGIC - Model data
				{
					// System.out.println( "### Start of model data" );
					readChunk( in );
				}
				break;

			/**** LIGHT CHUNKS ***********************************************/

			case 0x2100 : // LIGHT_AMBIENT - Ambient light
				{
					final float[] color = readColor( in );
					System.out.println( " - LIGHT - ambient: " + color );
				}
				break;

			/**** OBJECT CHUNKS **********************************************/

			case 0x4000 : // OBJECT_NAME - Identification of object
				{
					final String objectName = readString( in );
					// System.out.println( " - START: object: " + objectName );

					_object3DBuilder = null;
					_material = null;
					_textureCoords = null;

					if ( ( objectName.length() > 0 ) && ( objectName.charAt( 0 ) != '$' ) ) // ignore hidden objects
					{
						final Object3DBuilder builder = new Object3DBuilder();
						_object3DBuilder = builder;
						final Object3D object3d = builder.getObject3D();
						object3d.setTag( objectName );
						_material = new Material( 0xFFC0C0C0 );

						readChunk( in );
					}
					else
					{
						in.skip( (long)( chunkLength - objectName.length() - 1 - 6 ) );
					}
				}
				break;

			case 0x4100 : // OBJECT_N_TRI - Object definition
				{
					// System.out.println( "### OBJECT_N_TRI" );
					readChunk( in );
				}
				break;

			case 0x4110 : // OBJECT_POINT_ARRAY - Point coordinates array
				{
					// System.out.println( "    > point coordinates array" );
					final int vertexCount = readShort( in );

					final double[] vertexCoordinates = new double[ vertexCount * 3 ];
					for ( int i = 0 ; i < vertexCoordinates.length ; i++ )
						vertexCoordinates[ i ] = (double)readFloat( in );

					_object3DBuilder.setVertexCoordinates( vertexCoordinates );
				}
				break;

			case 0x4120 : // OBJECT_FACE_ARRAY - Face array
				{
					// System.out.println( "    > face array" );

					/*
					 * Read in the list of polygons and allocate a TriangleArray
					 * big enough to contain them. Don't fill it in yet though,
					 * we want to one surface at a time.
					 */
					final int faceCount = readShort( in );

					final Object3DBuilder builder       = _object3DBuilder;
					final Material        material      = _material;
					final Point2D.Float[] textureCoords = _textureCoords;

					for ( int i = 0 ; i < faceCount ; i++ )
					{
						final int vertexIndex1 = readShort( in );
						final int vertexIndex2 = readShort( in );
						final int vertexIndex3 = readShort( in );
						/*nal int flags       */ readShort( in );

						final Point2D.Float[] texturePoints;

						if ( textureCoords != null )
						{
							texturePoints = new Point2D.Float[] { textureCoords[ vertexIndex3 ] , textureCoords[ vertexIndex2 ] , textureCoords[ vertexIndex1 ] };
						}
						else
						{
							texturePoints = null;
						}

						final boolean hasBackface = true; // ( material.opacity < 0.99f );
						_object3DBuilder.addFace( new int[] { vertexIndex3 , vertexIndex2 , vertexIndex1 } , material , texturePoints , null , false , hasBackface );
					}

					_root.addChild( builder.getObject3D() );
				}
				break;

			case 0x4130 : // OBJECT_MAT_GROUP - Materials used by object - associates material with the object.
				{
					 System.out.println( "    > material group" );

					final String materialName = readString( in );
					final int    faceCount    = readShort( in );

					final Material material = _materials.get( materialName );
					if ( material == null )
						throw new IOException( "can't find referenced material: " + materialName );

					final Object3DBuilder builder = _object3DBuilder;
					final Object3D object = builder.getObject3D();

					for ( int i = 0 ; i < faceCount ; i++ )
					{
						final int faceIndex = readShort( in );
						final Face3D face = object.getFace( faceIndex );
						face.material = material;
					}

					_material = material;
				}
				break;

			case 0x4140 : // OBJECT_TEX_VERTS - 2D Texture coordinates - only valid with planar mapping
				{
					final Object3D object = _object3DBuilder.getObject3D();
					final int faceVertexCount = readShort( in );
					final int vertexCount     = object.getVertexCount();

					if ( faceVertexCount != vertexCount )
						throw new IOException( "Number of texture vertices != #model vertices (" + faceVertexCount + " != " + vertexCount + ')' );

					final Point2D.Float[] textureCoords = new Point2D.Float[ faceVertexCount ];

					for ( int i = 0 ; i < faceVertexCount ; i++ )
					{
						textureCoords[ i ] = new Point2D.Float( readFloat( in ) , readFloat( in ) );
					}

					_textureCoords = textureCoords;
				}
				break;

			case 0x4150 :// OBJECT_SMOOTH_GROUP - List of smooth surfaces
				{
					// System.out.println( "    > smooth group" );

					/*
					 * Contains a list of smoothed surfaces.  Construct each surface
					 * at a time, specifying vertex normals and texture coordinates
					 * (if present).
					 */
					final Object3D object    = _object3DBuilder.getObject3D();
					final int      faceCount = object.getFaceCount();

					for ( int i = 0 ; i < faceCount ; i++ )
					{
						final int groupBitMask = readLong( in ); // may be part of any of 32 'smoothing groups' - any will do for us

						if ( groupBitMask != 0 )
						{
							final Face3D face = object.getFace( i );
							face.smooth = true;
						}
					}
				}
				break;

			/**** MATERIAL CHUNKS ********************************************/
			case 0xA000 : // MAT_NAME - Start of a material
				{
					final String materialName = readString( in );
					System.out.println( " - START: material: " + materialName );

					final Material material = new Material( Color.RED.getRGB() );
					_materials.put( materialName , material );
					_material = material;
				}
				break;

			case 0xA010 : // MAT_AMBIENT - Ambient color of material
				{
					final float[] color = readColor( in );
					System.out.println( "    > ambient color: " + color );

					_material.ambientColorRed   = color[ 0 ];
					_material.ambientColorGreen = color[ 1 ];
					_material.ambientColorBlue  = color[ 2 ];
				}
				break;

			case 0xA020 : // MAT_DIFFUSE - Diffuse color of material
				{
					final float[] color = readColor( in );
					System.out.println( "    > diffuse color: " + color );

					_material.diffuseColorRed   = color[ 0 ];
					_material.diffuseColorGreen = color[ 1 ];
					_material.diffuseColorBlue  = color[ 2 ];
				}
				break;

			case 0xA030 : // MAT_SPECULAR - Specular color of material
				{
					final float[] color = readColor( in );
					System.out.println( "    > specular color: " + color );

					_material.specularColorRed   = color[ 0 ];
					_material.specularColorGreen = color[ 1 ];
					_material.specularColorBlue  = color[ 2 ];
				}
				break;

			case 0xA040 : // MAT_SHININESS - shininess percentage
				{
					final float shininess = readPercentage( in );
					System.out.println( "    > shininess: " + shininess );
				}
				break;

			case 0xA041 : // MAT_SHININESS_STRENGTH - shininess strength
				{
					final float shininessStrength = readPercentage( in );
					System.out.println( "    > shininess strength: " + shininessStrength );

					// Java 3D: Material.setShininess( ( 1.0f - ( ( _shininess + shininessStrength ) / 2.0f ) ) * 128.0f );
				}
				break;

			case 0xA050 : // MAT_TRANSPARENCY - Transparency percentage
				{
					final float transparency = readPercentage( in );
					System.out.println( "    > transparency: " + transparency );

					if ( transparency > 0.1f )
					{
						_material.diffuseColorAlpha = 1.0f - transparency;
					}
				}
				break;

			case 0xA081 : // MAT_TWO_SIDE - Turn off face culling
				{
					System.out.println( "    > two-sided" );
				}
				break;

			case 0xA085 : // MAT_WIRE - Select wireframe rendering
				{
					System.out.println( "    > wireframe" );
				}
				break;

			case 0xA087 : // MAT_WIRESIZE - Wireframe line thickness
				{
					final float width = readFloat( in );
					System.out.println( "    > wireframe line width: " + width );
				}
				break;

			case 0xA100 : // MAT_SHADING - Shading mode (wireframe/flat/metalic/phong)
				{
					final int shading = readShort( in );
					System.out.println( "    > shading: " + shading );

					switch ( shading )
					{
						case  0 : // Wireframe
							break;

						case  1 : // Flat
							break;

						case  2 : // Metalic
							break;

						default :
						case  3 : // Phong
							break;
					}
				}
				break;

			case 0xA200 : // MAT_TEXMAP - Texture map
				{
					/*
					 * Read in name chunk.
					 */
					/*    int    chnunkID    */ readShort( in );
					/*    int    chunkLength */ readLong( in );
					final String materialName = readString( in );
//					System.out.println( "    > texture map: " + materialName );

					/*
					 * Read texure image
					 */
					final URL  supplementURL = _supplementURL;
					final URL  imageURL      = new URL( supplementURL , TextTools.replace( TextTools.replace( materialName , '-' , "minus" ) , '+' , "plus" ).replace( ' ' , '_' ) );

					_material.colorMap = imageURL.toExternalForm();
				}
				break;

			case 0xAFFF : // MAT_ENTRY - Material definitions
				{
					// System.out.println( "### Start of material data" );
					readChunk( in );
				}
				break;


			/**** HIERARCHY CHUNKS *******************************************/

			case 0xB000 : // HIERARCHY - Start of the instance tree
				{
					_object3DBuilder = null;
					_material = null;
					_textureCoords = null;
					// System.out.println( "### HIERARCHY" );
					readChunk( in );
				}
				break;

			case 0xB002 : // HIERARCHY_NODE - Object instance
				{
					// System.out.println( "### HIERARCHY_NODE" );
					readChunk( in );
				}
				break;

			case 0xB010 : // HIERARCHY_LINK - Details of an object instance - defines an instance of an object.
				{
					/* node name */ readString ( in );
					/* flags1    */ readShort( in );
					/* flags2    */ readShort( in );
					/* dummy     */ readShort( in );
				}
				break;

			case 0xB011 : // INSTANCE_NAME - Name given to object instance
				{
					/* instanceName */ readString( in );
				}
				break;

			/**** OTHER/IGNORED CHUNKS ***************************************/

			default :
//				System.out.println( "            * implicitly skipped: 0x" + Integer.toHexString( chunkID ) + " *" );
//
//			case 0x0100 : // MASTER_SCALE     - Global scaling factor
//			case 0x4111 : // POINT_FLAG_ARRAY - Point flags array
//			case 0x4160 : // MESH_MATRIX      - Object transform (transformation that has already been applied to vertices, should convert back?)
//			case 0xB013 : // PIVOT            - Pivot point
//			case 0xB020 : // POS_TRACK        - Position animation key frame track
//			case 0xB021 : // ROT_TRACK        - Rotation animation key frame track
//			case 0xB022 : // SCL_TRACK        - Scale animation key frame track
//			case 0xB030 : // NODE_ID          - Node ID
				in.skip( (long)( chunkLength - 6 ) );
				break;
		}
	}

	/**
	 * Read a 'char' (single byte integer, unsigned 8 bits).
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Character value that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static int readChar( final InputStream in )
		throws IOException
	{
		final int result = in.read();
		if ( result < 0 )
			throw new EOFException();

		return result;
	}

	/**
	 * Read a 'short' integer (two byte integer, unsigned 16 bits, big endian).
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Integer value that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static int readShort( final InputStream in )
		throws IOException
	{
		final int b0 = readChar( in );
		final int b1 = readChar( in );

		return ( b1 << 8 ) + b0;
	}

	/**
	 * Read a 'long' integer (a four byte integer, signed 32 bits, big endian).
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Integer value that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static int readLong( final InputStream in )
		throws IOException
	{
		final int b0 = readChar( in );
		final int b1 = readChar( in );
		final int b2 = readChar( in );
		final int b3 = readChar( in );

		return ( b3 << 24 ) + ( b2 << 16 ) + ( b1 << 8 ) + b0;
	}

	/**
	 * Read a 'float' value (four byte IEEE floating point number, 32 bit, big
	 * endian).
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Floating-point value that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static float readFloat( final InputStream in )
		throws IOException
	{
		return Float.intBitsToFloat( readLong( in ) );
	}

	/**
	 * Read a 'cstr' (a zero byte terminated ASCII string without a length).
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  String that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static String readString( final InputStream in )
		throws IOException
	{
		final StringBuffer result = new StringBuffer();

		for ( int ch = readChar( in ) ; ch != 0 ; ch = readChar( in ) )
			result.append( (char)ch );

		return result.toString();
	}

	/**
	 * Read a color.
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Color that was read.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static float[] readColor( final InputStream in )
		throws IOException
	{
		final float[] result;

		final int chunkID      = readShort( in );
		/*        chunkLength */ readLong( in );

		switch ( chunkID )
		{
			case 0x0010 : // COLOR_F
				result = new float[] { readFloat( in ), readFloat( in ), readFloat( in ) };
				break;

			case 0x0011 : // COLOR_24
				result = new float[] { (float)readChar( in ) / 255.0f , (float)readChar( in ) / 255.0f , (float)readChar( in ) / 255.0f };
				break;

			default :
				throw new IOException( "color expected, but encountered 0x" + Integer.toHexString( chunkID ) );
		}

		return result;
	}

	/**
	 * Read a percentage and convert it to a value between 0.0 and 1.0.
	 *
	 * @param   in      Stream to read from.
	 *
	 * @return  Percentage that was read and converted to a value between 0.0
	 *          and 1.0.
	 *
	 * @throws  EOFException if the end of the file was reached.
	 * @throws  IOException if a read error occured.
	 */
	private static float readPercentage( final InputStream in )
		throws IOException
	{
		final float result;

		final int chunkID      = readShort( in );
		/*        chunkLength */ readLong( in );

		switch ( chunkID )
		{
			case 0x0030 : // INT_PERCENTAGE
				result = (float)readShort( in ) / 100.0f;
				break;

			case 0x0031 : // FLOAT_PERCENTAGE
				result = readFloat( in );
				break;

			default :
				throw new IOException( "percentage expected, but encountered 0x" + Integer.toHexString( chunkID ) );
		}

		return result;
	}
}
