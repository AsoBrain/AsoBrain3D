/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
 */
package ab.j3d.loader;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Writes a {@link Node3D} in OBJ format.
 *
 * @author G. Meinders
 */
public class ObjWriter
{
	/**
	 * Number format with up to 6 decimals.
	 */
	private final NumberFormat _decimalFormat;

	{
		final NumberFormat df = NumberFormat.getNumberInstance( Locale.US );
		df.setGroupingUsed( false );
		df.setMinimumFractionDigits( 1 );
		df.setMaximumFractionDigits( 6 );
		_decimalFormat = df;
	}

	/**
	 * Previously written appearances, by material name used in the MTL.
	 */
	private final Map<Appearance, String> _appearanceNames = new HashMap<Appearance, String>();

	/**
	 * Set of names currently in use.
	 */
	private final Collection<String> _uniqueNames = new HashSet<String>();

	/**
	 * Vertex positions.
	 */
	private final HashList<Vector3D> _vertices = new HashList<Vector3D>();

	/**
	 * Vertex texture coordinates.
	 */
	private final HashList<Vector2f> _textureVertices = new HashList<Vector2f>();

	/**
	 * Vertex normals.
	 */
	private final HashList<Vector3D> _normals = new HashList<Vector3D>();

	/**
	 * Texture library to use.
	 */
	private final TextureLibrary _textureLibrary;

	/**
	 * Converter for image URLs used in MTL files.
	 */
	private TextureNameConverter _textureNameConverter = new DefaultTextureNameConverter();

	/**
	 * Whether reflection maps are written in MTL files.
	 *
	 * <p>Cube maps are used for reflections, and they are not well supported
	 * in common rendering software. E.g. Blender imports the OBJ using only the
	 * final 'cube_top' texture as if it were a longitude/latitude map.
	 */
	private boolean _reflectionMapsEnabled = false;

	/**
	 * Whether unsupported reflection properties are written as comments.
	 */
	private boolean _reflectionCommentsEnabled = false;

	/**
	 * Whether {@link Object3D objects} should be written as named polygon
	 * groups, instead of individual objects.
	 */
	private boolean _writeObjectsAsGroups = false;

	/**
	 * Whether to write vertex normals when they are equal to the implicit
	 * normal of a face. Always writing face normals may offer improved
	 * compatibility, but also produces larger output.
	 */
	private boolean _writeFaceNormals = false;

	/**
	 * Constructs a new instance.
	 *
	 * @param textureLibrary Texture library used to include textures in the
	 *                       output (ZIP only).
	 */
	public ObjWriter( @Nullable final TextureLibrary textureLibrary )
	{
		_textureLibrary = textureLibrary;
	}

	/**
	 * Returns the converter for image URLs used in MTL files.
	 *
	 * @return URL converter.
	 */
	public TextureNameConverter getTextureNameConverter()
	{
		return _textureNameConverter;
	}

	/**
	 * Sets the converter for image URLs used in MTL files.
	 *
	 * @param textureNameConverter URL converter to set.
	 */
	public void setTextureNameConverter( final TextureNameConverter textureNameConverter )
	{
		_textureNameConverter = textureNameConverter;
	}

	public boolean isReflectionMapsEnabled()
	{
		return _reflectionMapsEnabled;
	}

	public void setReflectionMapsEnabled( final boolean reflectionMapsEnabled )
	{
		_reflectionMapsEnabled = reflectionMapsEnabled;
	}

	/**
	 * Returns whether unsupported reflection properties are written as
	 * comments.
	 *
	 * @return {@code true} to write comments with unsupported reflection
	 * properties.
	 */
	public boolean isReflectionCommentsEnabled()
	{
		return _reflectionCommentsEnabled;
	}

	/**
	 * Sets whether unsupported reflection properties are written as comments.
	 *
	 * @param reflectionCommentsEnabled {@code true} to write comments with
	 *                                unsupported reflection properties.
	 */
	public void setReflectionCommentsEnabled( final boolean reflectionCommentsEnabled )
	{
		_reflectionCommentsEnabled = reflectionCommentsEnabled;
	}

	/**
	 * Returns whether {@link Object3D objects} should be written as named
	 * polygon groups, instead of individual objects.
	 *
	 * @return {@code true} when writing objects as groups.
	 */
	public boolean isWriteObjectsAsGroups()
	{
		return _writeObjectsAsGroups;
	}

	/**
	 * Sets whether {@link Object3D objects} should be written as named polygon
	 * groups, instead of individual objects.
	 *
	 * @param writeObjectsAsGroups {@code true} to write objects as groups.
	 */
	public void setWriteObjectsAsGroups( final boolean writeObjectsAsGroups )
	{
		_writeObjectsAsGroups = writeObjectsAsGroups;
	}

	public boolean isWriteFaceNormals()
	{
		return _writeFaceNormals;
	}

	public void setWriteFaceNormals( final boolean writeFaceNormals )
	{
		_writeFaceNormals = writeFaceNormals;
	}

	/**
	 * Add {@link Appearance} to MTL file. If the appearance was added before,
	 * calling this method will have no effect.
	 *
	 * @param appearance Appearance to add.
	 *
	 * @return Name of appearance.
	 */
	public String addAppearance( @NotNull final Appearance appearance )
	{
		final Map<Appearance, String> appearanceNames = _appearanceNames;

		String result = appearanceNames.get( appearance );
		if ( result == null )
		{
			result = generateUniqueName( "material" );
			addAppearance( result, appearance );
		}

		return result;
	}

	/**
	 * Add {@link Appearance} to MTL file. If the appearance was added before,
	 * calling this method will have no effect.
	 *
	 * @param name       Name to assign to the appearance.
	 * @param appearance Appearance to add.
	 *
	 * @throws IllegalArgumentException if the appearance was already added with
	 * another name.
	 */
	public void addAppearance( final String name, @NotNull final Appearance appearance )
	{
		final Map<Appearance, String> appearanceNames = _appearanceNames;
		final String oldName = appearanceNames.put( appearance, name );
		if ( ( oldName != null ) && !oldName.equals( name ) )
		{
			throw new IllegalArgumentException( "Trying to add appearance as '" + name + "', but it was was already added as '" + oldName + '"' );
		}
	}

	/**
	 * Generates a unique name from the given name. If the name is already unique,
	 * it's returned as-is.
	 *
	 * @param name Name to make unique.
	 *
	 * @return Unique name.
	 */
	private String generateUniqueName( final String name )
	{
		String result = name;
		int uniqueSuffix = 1;
		while ( _uniqueNames.contains( result ) )
		{
			result = name + '_' + uniqueSuffix++;
		}
		_uniqueNames.add( result );
		return result;
	}

	/**
	 * Writes a ZIP file with an OBJ and MTL file for the given node.
	 *
	 * @param out             Stream to write to.
	 * @param node            Node to be written.
	 * @param name            Name for the OBJ/MTL files (without extension).
	 * @param includeTextures Whether to include texture images in the ZIP.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeZIP( final OutputStream out, final Node3D node, final String name, final boolean includeTextures )
	throws IOException
	{
		final ZipOutputStream zipOut = new ZipOutputStream( new BufferedOutputStream( out ) );

		zipOut.putNextEntry( new ZipEntry( name + ".obj" ) );
		write( zipOut, node, name + ".mtl" );
		zipOut.closeEntry();

		final Map<String, Appearance> appearances = new HashMap<String, Appearance>();
		for ( final Map.Entry<Appearance, String> entry : _appearanceNames.entrySet() )
		{
			appearances.put( entry.getValue(), entry.getKey() );
		}

		zipOut.putNextEntry( new ZipEntry( name + ".mtl" ) );
		writeMTL( zipOut, appearances );
		zipOut.closeEntry();

		if ( includeTextures && _textureLibrary != null )
		{
			final byte[] buffer = new byte[ 10000 ];

			final Set<TextureMap> textures = new LinkedHashSet<TextureMap>();
			for ( final Appearance appearance : _appearanceNames.keySet() )
			{
				final TextureMap colorMap = appearance.getColorMap();
				if ( colorMap != null )
				{
					textures.add( colorMap );
				}

				final TextureMap bumpMap = appearance.getBumpMap();
				if ( bumpMap != null )
				{
					textures.add( bumpMap );
				}

/*
				final CubeMap reflectionMap = appearance.getReflectionMap();
				if ( reflectionMap != null )
				{
					textures.add( reflectionMap.getImageLeft() );
					textures.add( reflectionMap.getImageFront() );
					textures.add( reflectionMap.getImageBottom() );
					textures.add( reflectionMap.getImageRight() );
					textures.add( reflectionMap.getImageRear() );
					textures.add( reflectionMap.getImageTop() );
				}
*/
			}

			for ( final TextureMap texture : textures )
			{
				final InputStream imageIn = _textureLibrary.openImageStream( texture );
				if ( imageIn != null )
				{
					final String imageName = _textureNameConverter.convert( texture.getName() );
					zipOut.putNextEntry( new ZipEntry( imageName ) );

					try
					{
						int bytesRead;
						while ( ( bytesRead = imageIn.read( buffer ) ) != -1 )
						{
							zipOut.write( buffer, 0, bytesRead );
						}
					}
					finally
					{
						imageIn.close();
					}

					zipOut.closeEntry();
				}
			}
		}

		zipOut.finish();
		zipOut.flush();
	}

	/**
	 * Writes an MTL file containing the given appearances.
	 *
	 * @param out             Stream to write to.
	 * @param node            Node to write appearances for.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeMTL( final OutputStream out, final Node3D node )
	throws IOException
	{
		final Writer writer = new BufferedWriter( new OutputStreamWriter( out, "US-ASCII" ) );
		writeMTL( writer, node );
		writer.flush();
	}

	/**
	 * Writes an MTL file containing the given appearances.
	 *
	 * @param writer          Stream to write to.
	 * @param node            Node to write appearances for.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeMTL( final Writer writer, final Node3D node )
	throws IOException
	{
		Node3DTreeWalker.walk( new Node3DVisitor()
		{
			@Override
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				final Node3D node = path.getNode();
				if ( node instanceof Object3D )
				{
					final Object3D object = (Object3D)node;
					for ( final FaceGroup faceGroup : object.getFaceGroups() )
					{
						final Appearance appearance = faceGroup.getAppearance();
						if ( appearance != null )
						{
							addAppearance( appearance );
						}
					}
				}
				return true;
			}
		}, node );

		for ( final Map.Entry<Appearance, String> entry : _appearanceNames.entrySet() )
		{
			writeMtlRecord( writer, entry.getValue(), entry.getKey() );
		}
	}

	/**
	 * Writes an MTL file containing the given appearances.
	 *
	 * @param out             Stream to write to.
	 * @param appearances     Appearances to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeMTL( final OutputStream out, final Map<String, ? extends Appearance> appearances )
	throws IOException
	{
		final Writer writer = new BufferedWriter( new OutputStreamWriter( out, "US-ASCII" ) );
		writeMTL( writer, appearances );
		writer.flush();
	}

	/**
	 * Writes an MTL file containing the given appearances.
	 *
	 * @param writer          Stream to write to.
	 * @param appearances     Appearances to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeMTL( final Writer writer, final Map<String, ? extends Appearance> appearances )
	throws IOException
	{
		for ( final Map.Entry<String, ? extends Appearance> entry : appearances.entrySet() )
		{
			writeMtlRecord( writer, entry.getKey(), entry.getValue() );
		}
	}

	/**
	 * Writes a definition of the given appearance.
	 *
	 * @param out          Character stream to write record to.
	 * @param materialName Material name used in the MTL.
	 * @param appearance   Appearance to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void writeMtlRecord( final Appendable out, final CharSequence materialName, final Appearance appearance )
	throws IOException
	{
		//noinspection SpellCheckingInspection
		out.append( "newmtl " );
		out.append( materialName );
		out.append( "\r\n" );

		final Color4 ambientColor = appearance.getAmbientColor();
		out.append( "Ka " );
		out.append( _decimalFormat.format( ambientColor.getRedFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( ambientColor.getGreenFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( ambientColor.getBlueFloat() ) );
		out.append( "\r\n" );

		final Color4 diffuseColor = appearance.getDiffuseColor();
		out.append( "Kd " );
		out.append( _decimalFormat.format( diffuseColor.getRedFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( diffuseColor.getGreenFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( diffuseColor.getBlueFloat() ) );
		out.append( "\r\n" );

		final Color4 specularColor = appearance.getSpecularColor();
		out.append( "Ks " );
		out.append( _decimalFormat.format( specularColor.getRedFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( specularColor.getGreenFloat() ) );
		out.append( ' ' );
		out.append( _decimalFormat.format( specularColor.getBlueFloat() ) );
		out.append( "\r\n" );

		//noinspection SpellCheckingInspection
		out.append( "illum 2\r\n" );

		out.append( "Ns " );
		final int shininess = appearance.getShininess();
		// NOTE: This is simply the inverse of what 'ObjLoader' does.
		out.append( String.valueOf( Math.min( 128, shininess * 1000 / 128 ) ) );
		out.append( "\r\n" );

		out.append( "d " );
		out.append( _decimalFormat.format( diffuseColor.getAlphaFloat() ) );
		out.append( "\r\n" );

		final TextureMap colorMap = appearance.getColorMap();
		if ( colorMap != null )
		{
			out.append( "map_Kd " );
			out.append( _textureNameConverter.convert( colorMap.getName() ) );
			out.append( "\r\n" );
		}

		final TextureMap bumpMap = appearance.getBumpMap();
		if ( bumpMap != null )
		{
			out.append( "bump " );
			out.append( _textureNameConverter.convert( bumpMap.getName() ) );
			out.append( "\r\n" );
		}

		final CubeMap reflectionMap = appearance.getReflectionMap();
		if ( reflectionMap != null )
		{
			if ( isReflectionMapsEnabled() )
			{
				out.append( "refl -type cube_left " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageLeft().getName() ) );
				out.append( "\r\n" );

				out.append( "refl -type cube_front " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageFront().getName() ) );
				out.append( "\r\n" );

				out.append( "refl -type cube_bottom " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageBottom().getName() ) );
				out.append( "\r\n" );

				out.append( "refl -type cube_right " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageRight().getName() ) );
				out.append( "\r\n" );

				out.append( "refl -type cube_back " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageRear().getName() ) );
				out.append( "\r\n" );

				out.append( "refl -type cube_top " );
				out.append( _textureNameConverter.convert( reflectionMap.getImageTop().getName() ) );
				out.append( "\r\n" );
			}

			if ( isReflectionCommentsEnabled() )
			{
				// NOTE: MTL doesn't support minimum/maximum reflectivity. Write as comment instead.
				out.append( "#reflect " );
				out.append( String.valueOf( appearance.getReflectionMin() ) );
				out.append( " " );
				out.append( String.valueOf( appearance.getReflectionMax() ) );
				out.append( "\r\n" );
			}
		}
	}

	/**
	 * Writes an OBJ file for the given node.
	 *
	 * @param out               Stream to write to.
	 * @param node              Node to be written.
	 * @param materialLibraries Names of material libraries.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void write( final OutputStream out, final Node3D node, final String... materialLibraries )
	throws IOException
	{
		final Writer objWriter = new BufferedWriter( new OutputStreamWriter( out, "US-ASCII" ) );
		write( objWriter, node, materialLibraries );
		objWriter.flush();
	}

	/**
	 * Writes an OBJ file for the given node.
	 *
	 * @param out               Stream to write to.
	 * @param node              Node to be written.
	 * @param materialLibraries Names of material libraries.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void write( final Writer out, final Node3D node, final String... materialLibraries )
	throws IOException
	{
		for ( final String materialLibrary : materialLibraries )
		{
			//noinspection SpellCheckingInspection
			out.write( "mtllib " );
			out.write( materialLibrary );
			out.write( "\r\n" );
		}

		final ArrayList<Node3DPath> nodes = new ArrayList<Node3DPath>();
		Node3DTreeWalker.walk( new Node3DCollector( nodes, Object3D.class ), node );

		final Map<Node3DPath, Map<Vertex3D, ObjVertex>> vertexMaps = createVertexMap( nodes );

		writeVertexList( out );
		writeTextureVertexList( out );
		writeNormalList( out );
		writeObjects( out, nodes, vertexMaps );
	}

	/**
	 * Write vertex list.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeVertexList( @NotNull final Appendable out )
	throws IOException
	{
		for ( final Vector3D vertex : _vertices )
		{
			out.append( "v " );
			out.append( ' ' );
			out.append( _decimalFormat.format( vertex.x ) );
			out.append( ' ' );
			out.append( _decimalFormat.format( vertex.y ) );
			out.append( ' ' );
			out.append( _decimalFormat.format( vertex.z ) );
			out.append( "\r\n" );
		}
	}

	/**
	 * Write texture vertex list.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeTextureVertexList( @NotNull final Appendable out )
	throws IOException
	{
		for ( final Vector2f textureVertex : _textureVertices )
		{
			out.append( "vt " );
			out.append( _decimalFormat.format( (double)textureVertex.getX() ) );
			out.append( ' ' );
			out.append( _decimalFormat.format( (double)textureVertex.getY() ) );
			out.append( "\r\n" );
		}
	}

	/**
	 * Write normal list.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeNormalList( @NotNull final Appendable out )
	throws IOException
	{
		for ( final Vector3D normal : _normals )
		{
			out.append( "vn " );
			out.append( ' ' );
			out.append( _decimalFormat.format( normal.x ) );
			out.append( ' ' );
			out.append( _decimalFormat.format( normal.y ) );
			out.append( ' ' );
			out.append( _decimalFormat.format( normal.z ) );
			out.append( "\r\n" );
		}
	}

	/**
	 * Write visited {@link Object3D} to output.
	 *
	 * @param out        Stream to write to.
	 * @param objects    Objects to write.
	 * @param vertexMaps Map from 3D scene to OBJ file vertices.
	 *
	 * @throws IOException if the object could not be written.
	 */
	private void writeObjects( final Appendable out, final Iterable<Node3DPath> objects, final Map<Node3DPath, Map<Vertex3D, ObjVertex>> vertexMaps )
	throws IOException
	{
		Appearance currentAppearance = null;

		for ( final Node3DPath path : objects )
		{
			final Object3D object = (Object3D)path.getNode();
			final Map<Vertex3D, ObjVertex> vertexMap = vertexMaps.get( path );

			out.append( isWriteObjectsAsGroups() ? "g " : "o " );
			out.append( getObjectName( object ) );
			out.append( "\r\n" );

			for ( final FaceGroup faceGroup : object.getFaceGroups() )
			{
				final Appearance appearance = faceGroup.getAppearance();
				if ( appearance == null )
				{
					throw new IOException( "Can't write null-appearance of object " + object );
				}

				//noinspection ObjectEquality
				if ( appearance != currentAppearance )
				{
					final String materialName = addAppearance( appearance );

					//noinspection SpellCheckingInspection
					out.append( "usemtl " );
					out.append( materialName );
					out.append( "\r\n" );

					currentAppearance = appearance;
				}

				for ( final Face3D face : faceGroup.getFaces() )
				{
					writeFace( out, faceGroup, face, vertexMap );
				}
			}
		}
	}

	/**
	 * Write {@link Face3D} to output.
	 *
	 * @param out       Stream to write to.
	 * @param faceGroup Face group to which the face belongs.
	 * @param vertexMap Map from 3D scene to OBJ file vertices.
	 * @param face      Face to write.
	 *
	 * @throws IOException if the object could not be written.
	 */
	private void writeFace( @NotNull final Appendable out, @NotNull final FaceGroup faceGroup, @NotNull final Face3D face, @NotNull final Map<Vertex3D, ObjVertex> vertexMap )
	throws IOException
	{
		final boolean twoSided = faceGroup.isTwoSided();

		final ObjVertex[] faceVertices = new ObjVertex[ face.getVertexCount() ];
		for ( int i = 0; i < face.getVertexCount(); i++ )
		{
			final Vertex3D vertex = face.getVertex( i );

			final ObjVertex objVertex = vertexMap.get( vertex );
			if ( objVertex == null )
			{
				throw new AssertionError( "vertex not defined" );
			}

			faceVertices[ i ] = objVertex;
		}

		final Tessellation tessellation = face.getTessellation();
		for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
		{

			final int[] triangles = primitive.getTriangles();
			for ( int i = 0; i < triangles.length; i += 3 )
			{
				writeFace( out, twoSided, faceVertices[ triangles[ i ] ], faceVertices[ triangles[ i + 1 ] ], faceVertices[ triangles[ i + 2 ] ] );
			}
		}
	}

	/**
	 * Write face to OBJ file.
	 *
	 * @param out      Stream to write to.
	 * @param twoSided Write two-sided face (actually write 2nd face with reversed
	 *                 vertices).
	 * @param vertices Vertices that define the face.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeFace( final Appendable out, final boolean twoSided, final ObjVertex... vertices )
	throws IOException
	{
		out.append( 'f' );
		for ( final ObjVertex vertex : vertices )
		{
			writeVertex( out, vertex );
		}
		out.append( "\r\n" );

		if ( twoSided )
		{
			out.append( 'f' );
			for ( int i = vertices.length; --i >= 0; )
			{
				writeVertex( out, vertices[ i ] );
			}
			out.append( "\r\n" );
		}
	}

	/**
	 * Write vertex.
	 *
	 * @param out    Stream to write to.
	 * @param vertex Vertex to write.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeVertex( @NotNull final Appendable out, @NotNull final ObjVertex vertex )
	throws IOException
	{
		out.append( ' ' );
		out.append( String.valueOf( vertex._vertexIndex ) );

		if ( ( vertex._textureVertexIndex > 0 ) || ( vertex._normalIndex > 0 ) )
		{
			out.append( '/' );

			if ( vertex._textureVertexIndex > 0 )
			{
				out.append( String.valueOf( vertex._textureVertexIndex ) );
			}

			if ( vertex._normalIndex > 0 )
			{
				out.append( '/' );
				out.append( String.valueOf( vertex._normalIndex ) );
			}
		}
	}

	/**
	 * Returns a name for the given object.
	 *
	 * @param object Object to be named.
	 *
	 * @return Name for the object.
	 */
	@NotNull
	private CharSequence getObjectName( @NotNull final Object3D object )
	{
		final String basename = "object";  // ( object.getTag() == null ) ? "object" : String.valueOf( object.getTag() );
		return generateUniqueName( basename );
	}

	/**
	 * Create map from 3D scene vertices to OBJ file vertices.
	 *
	 * @param nodes Nodes containing 3D scene.
	 *
	 * @return Map from 3D scene to OBJ file vertices.
	 */
	private Map<Node3DPath, Map<Vertex3D, ObjVertex>> createVertexMap( final Iterable<Node3DPath> nodes )
	{
		final HashList<Vector3D> vertices = _vertices;
		final HashList<Vector2f> textureVertices = _textureVertices;
		final HashList<Vector3D> normals = _normals;

		final Map<Node3DPath, Map<Vertex3D, ObjVertex>> vertexMaps = new IdentityHashMap<Node3DPath, Map<Vertex3D, ObjVertex>>();
		for ( final Node3DPath path : nodes )
		{
			final Matrix3D transform = path.getTransform();
			final Object3D object = (Object3D)path.getNode();

			final Map<Vertex3D, ObjVertex> vertexMap = new IdentityHashMap<Vertex3D, ObjVertex>();

			for ( final FaceGroup faceGroup : object.getFaceGroups() )
			{
				for ( final Face3D face : faceGroup.getFaces() )
				{
					final Vector3D faceNormal = face.getNormal();
					final int vertexCount = face.getVertexCount();

					boolean hasTextureVertex = false;
					for ( int i = 0; i < vertexCount; i++ )
					{
						final Vertex3D vertex = face.getVertex( i );
						if ( !Double.isNaN( vertex.colorMapU ) && !Double.isNaN( vertex.colorMapV ) )
						{
							hasTextureVertex = true;
							break;
						}
					}

					boolean hasVertexNormal = isWriteFaceNormals();
					if ( !hasVertexNormal )
					{
						for ( int i = 0; i < vertexCount; i++ )
						{
							final Vector3D vertexNormal = face.getVertexNormal( i );
							if ( vertexNormal.isNonZero() && !vertexNormal.almostEquals( faceNormal ) )
							{
								hasVertexNormal = true;
								break;
							}
						}
					}

					for ( int i = 0; i < vertexCount; i++ )
					{
						final Vertex3D vertex = face.getVertex( i );
						final Vector3D vertexNormal = face.getVertexNormal( i );

						final int v = 1 + vertices.indexOfOrAdd( transform.transform( vertex.point ) );
						final int vt = hasTextureVertex ? ( 1 + textureVertices.indexOfOrAdd( new Vector2f( Float.isNaN( vertex.colorMapU ) ? 0.0f : vertex.colorMapU, Float.isNaN( vertex.colorMapV ) ? 0.0f : vertex.colorMapV ) ) ) : 0;
						final int vn = hasVertexNormal ? 1 + normals.indexOfOrAdd( transform.rotate( vertexNormal ) ) : 0;

						vertexMap.put( vertex, new ObjVertex( v, vt, vn ) );
					}
				}
			}

			vertexMaps.put( path, vertexMap );
		}

		return vertexMaps;
	}

	/**
	 * OBJ file vertex.
	 */
	private static class ObjVertex
	{
		/**
		 * Vertex index (1+).
		 */
		private final int _vertexIndex;

		/**
		 * Texture vertex index (1+ or 0=undefined).
		 */
		private final int _textureVertexIndex;

		/**
		 * Normal index (1+ or 0=undefined).
		 */
		private final int _normalIndex;

		/**
		 * Create vertex.
		 *
		 * @param vertexIndex        Vertex index (1+).
		 * @param textureVertexIndex Texture vertex index (1+ or 0=undefined).
		 * @param normalIndex        Normal index (1+ or 0=undefined).
		 */
		private ObjVertex( final int vertexIndex, final int textureVertexIndex, final int normalIndex )
		{
			_vertexIndex = vertexIndex;
			_textureVertexIndex = textureVertexIndex;
			_normalIndex = normalIndex;
		}

		@Override
		public int hashCode()
		{
			return _vertexIndex ^ _textureVertexIndex ^ _normalIndex;
		}

		@Override
		public boolean equals( final Object obj )
		{
			final boolean result;

			if ( obj == this )
			{
				result = true;
			}
			else if ( obj instanceof ObjVertex )
			{
				final ObjVertex other = (ObjVertex)obj;
				result = ( ( other._vertexIndex == _vertexIndex ) && ( other._textureVertexIndex == _textureVertexIndex ) && ( other._normalIndex == _normalIndex ) );
			}
			else
			{
				result = false;
			}

			return result;
		}
	}
}
