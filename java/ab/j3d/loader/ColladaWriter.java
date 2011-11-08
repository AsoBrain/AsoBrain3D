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
package ab.j3d.loader;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.bind.*;
import javax.xml.stream.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Writes a {@link Scene} in COLLADA format.
 *
 * @see     <a href="http://collada.org/">COLLADA - Digital Asset and FX Exchange Schema</a>
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ColladaWriter
{
	/**
	 * Namespace URI and schema location for COLLADA version 1.4.
	 */
	public static final String NS_COLLADA_1_4 = "http://www.collada.org/2005/11/COLLADASchema";

	/**
	 * Namespace URI and schema location for COLLADA version 1.5.
	 */
	public static final String NS_COLLADA_1_5 = "http://www.collada.org/2008/03/COLLADASchema";

	/**
	 * Scene to be written.
	 */
	private final Scene _scene;

	/**
	 * Keeps track to the last allocated ID for each prefix.
	 */
	private final Map<String, Integer> _idMap;

	/**
	 * ID of each piece of geometry to be written in the
	 * &lt;library_geometries&gt; section.
	 */
	private final Map<Node3D, String> _libraryGeometry;

	/**
	 * ID of each material to be written in the &lt;library_materials&gt; section.
	 */
	private final Map<Appearance, String> _libraryMaterials;

	/**
	 * ID of each image to be written in the &lt;library_images&gt; section.
	 */
	private final Map<URI, String> _libraryImages;

	/**
	 * Internet Media Type for COLLADA.
	 *
	 * @see     <a href="http://www.iana.org/assignments/media-types/model/vnd.collada+xml">http://www.iana.org/assignments/media-types/model/vnd.collada+xml</a>
	 */
	public static final String MEDIA_TYPE = "model/vnd.collada+xml";

	/**
	 * Constructs a new instance.
	 *
	 * @param   scene   Scene to be written.
	 */
	public ColladaWriter( final Scene scene )
	{
		_scene = scene;
		_idMap = new HashMap<String, Integer>();
		_libraryGeometry = new HashMap<Node3D, String>();
		_libraryMaterials = new LinkedHashMap<Appearance, String>();
		_libraryImages = new LinkedHashMap<URI, String>();
	}

	/**
	 * Writes the COLLADA file to the given output stream.
	 *
	 * @param   out     Output stream to write to.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void write( final OutputStream out )
		throws IOException
	{
		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		try
		{
			final XMLStreamWriter plainWriter = xmlOutputFactory.createXMLStreamWriter( out );
			final XMLStreamWriter indentingWriter = new IndentingXMLStreamWriter( plainWriter );
			write( indentingWriter );
		}
		catch ( XMLStreamException e )
		{
			throw new IOException( e );
		}
	}

	/**
	 * Writes the COLLADA file to the given writer.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if an XML-related error occurs.
	 */
	public void write( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		_idMap.clear();
		_libraryGeometry.clear();
		_libraryMaterials.clear();
		_libraryImages.clear();

		writer.writeStartDocument();

		writer.writeStartElement( "COLLADA" );
		writer.writeDefaultNamespace( NS_COLLADA_1_4 );
		writer.writeAttribute( "version", "1.4.1" );

		writeAsset( writer );
		writeLibraryVisualScenes( writer );
		writeLibraryGeometries( writer );
		writeLibraryMaterials( writer );
		writeLibraryEffects( writer );
		writeLibraryImages( writer );
		writeScene( writer );

		writer.writeEndElement(); // COLLADA

		writer.writeEndDocument();
		writer.flush();
	}

	/**
	 * Allocates a unique ID.
	 *
	 * @return  Allocated ID.
	 */
	private String allocateID()
	{
		return allocateID( "o" );
	}

	/**
	 * Allocates a unique ID with the given prefix. The ID is unique within the
	 * document being written.
	 *
	 * @param   prefix  Prefix to be used.
	 *
	 * @return  Created ID.
	 */
	private String allocateID( @NotNull final String prefix )
	{
		final Integer previous = _idMap.get( prefix );
		final int id = ( ( previous == null ) ? 0 : previous ) + 1;
		_idMap.put( prefix, Integer.valueOf( id ) );
		return prefix + id;
	}

	/**
	 * Writes an asset element for the scene.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeAsset( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "asset" );

		final String currentDateTime = DatatypeConverter.printDateTime( Calendar.getInstance() );

		writer.writeStartElement( "created" );
		writer.writeCharacters( currentDateTime );
		writer.writeEndElement();

		writer.writeStartElement( "modified" );
		writer.writeCharacters( currentDateTime );
		writer.writeEndElement();

		writer.writeEmptyElement( "unit" );
		writer.writeAttribute( "name", "scene_unit" );
		writer.writeAttribute( "meter", DatatypeConverter.printDouble( _scene.getUnit() ) );

		writer.writeStartElement( "up_axis" );
		writer.writeCharacters( "Z_UP" );
		writer.writeEndElement();

		writer.writeEndElement();
	}

	/**
	 * Writes a <code>library_visual_scenes</code> element.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeLibraryVisualScenes( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "library_visual_scenes" );
		writeVisualScene( writer );
		writer.writeEndElement();
	}

	/**
	 * Writes a <code>visual_scene</code> element.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeVisualScene( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "visual_scene" );
		writer.writeAttribute( "id", "scene" );

		for ( final ContentNode contentNode : _scene.getContentNodes() )
		{
			writeNode( writer, contentNode );
		}

		writer.writeEndElement();
	}

	/**
	 * Writes a <code>node</code> hierarchy for the given content node.
	 *
	 * @param   writer          Writer to write to.
	 * @param   contentNode     Content node to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeNode( final XMLStreamWriter writer, final ContentNode contentNode )
		throws XMLStreamException
	{
		writer.writeStartElement( "node" );
		writer.writeAttribute( "name", convertToNCName( String.valueOf( contentNode.getID() ) ) );

		writeMatrix( writer, contentNode.getTransform() );
		writeNode( writer, contentNode.getNode3D() );

		writer.writeEndElement();
	}

	/**
	 * Writes a <code>node</code> hierarchy for the given node.
	 *
	 * <p>Instances of {@link Object3D} are written as
	 * <code>instance_geometry</code> elements that refer to geometry
	 * definitions written by {@link #writeLibraryGeometries}.
	 *
	 * @param   writer  Writer to write to.
	 * @param   node    Node to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeNode( final XMLStreamWriter writer, final Node3D node )
		throws XMLStreamException
	{
		writer.writeStartElement( "node" );

		final Object tag = node.getTag();
		if ( tag != null )
		{
			writer.writeAttribute( "name", convertToNCName( String.valueOf( tag ) ) );
		}

		if ( node instanceof Object3D )
		{
			String geometryID = _libraryGeometry.get( node );
			if ( geometryID == null )
			{
				geometryID = allocateID();
				_libraryGeometry.put( node, geometryID );
			}

			writer.writeStartElement( "instance_geometry" );
			writer.writeAttribute( "url", "#" + geometryID );

			writer.writeStartElement( "bind_material" );
			writer.writeStartElement( "technique_common" );

			final Set<String> boundMaterials = new HashSet<String>();

			final Object3D mesh = (Object3D)node;
			for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
			{
				final Appearance appearance = faceGroup.getAppearance();
				final String materialID = getMaterialID( appearance );
				if ( boundMaterials.add( materialID ) )
				{
					writer.writeStartElement( "instance_material" );
					writer.writeAttribute( "symbol", materialID );
					writer.writeAttribute( "target", "#" + materialID );

					writer.writeEmptyElement( "bind_vertex_input" );
					writer.writeAttribute( "semantic", "UVSET0" );
					writer.writeAttribute( "input_semantic", "TEXCOORD" );
					writer.writeAttribute( "input_set", "0" );

					writer.writeEndElement(); // instance_material
				}
			}

			writer.writeEndElement(); // technique_common
			writer.writeEndElement(); // bind_material

			writer.writeEndElement(); // instance_geometry
		}
		else if ( node instanceof Light3D )
		{
			// TODO: <instance_light>
		}
		else if ( node instanceof Transform3D )
		{
			final Transform3D transform = (Transform3D)node;
			writeMatrix( writer, transform.getTransform() );
		}

		for ( final Node3D childNode : node.getChildren() )
		{
			writeNode( writer, childNode );
		}

		writer.writeEndElement();
	}

	/**
	 * Writes a <code>matrix</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   matrix  Matrix to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeMatrix( final XMLStreamWriter writer, final Matrix3D matrix )
		throws XMLStreamException
	{
		writer.writeStartElement( "matrix" );

		writer.writeCharacters( DatatypeConverter.printDouble( matrix.xx ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.xy ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.xz ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.xo ) );
		writer.writeCharacters( "  " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.yx ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.yy ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.yz ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.yo ) );
		writer.writeCharacters( "  " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.zx ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.zy ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.zz ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( matrix.zo ) );
		writer.writeCharacters( "  " );
		writer.writeCharacters( DatatypeConverter.printDouble( 0.0 ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( 0.0 ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( 0.0 ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printDouble( 1.0 ) );

		writer.writeEndElement();
	}

	/**
	 * Writes a <code>library_geometries</code> element containing all of the
	 * geometry referenced by {@link #writeLibraryVisualScenes}.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeLibraryGeometries( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "library_geometries" );

		for ( final Map.Entry<Node3D, String> entry : _libraryGeometry.entrySet() )
		{
			final Node3D node = entry.getKey();
			final String geometryID = entry.getValue();

			if ( node instanceof Object3D )
			{
				writeGeometry( writer, (Object3D)node, geometryID );
			}
			else
			{
				throw new AssertionError( "Don't know how to write geometry for node: " + node );
			}
		}

		writer.writeEndElement(); // library_geometries
	}

	/**
	 * Writes a <code>geometry</code> element for the given geometry.
	 *
	 * @param   writer      Writer to write to.
	 * @param   mesh        Geometry to be written.
	 * @param   geometryID  ID of the geometry element.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeGeometry( final XMLStreamWriter writer, final Object3D mesh, final String geometryID )
		throws XMLStreamException
	{
		/*
		 * Write vertex coordinates.
		 */
		final String positionID = geometryID + "-position";
		final String positionArrayID = positionID + "-array";

		writer.writeStartElement( "geometry" );
		writer.writeAttribute( "id", geometryID );

		writer.writeStartElement( "mesh" );

		writer.writeStartElement( "source" );
		writer.writeAttribute( "id", positionID );

		writer.writeStartElement( "float_array" );
		writer.writeAttribute( "id", positionArrayID );
		writer.writeAttribute( "count", DatatypeConverter.printInt( mesh.getVertexCount() * 3 ) );
		for ( final Vector3D position : mesh.getVertexCoordinates() )
		{
			writer.writeCharacters( DatatypeConverter.printDouble( position.x ) );
			writer.writeCharacters( " " );
			writer.writeCharacters( DatatypeConverter.printDouble( position.y ) );
			writer.writeCharacters( " " );
			writer.writeCharacters( DatatypeConverter.printDouble( position.z ) );
			writer.writeCharacters( " " );
		}
		writer.writeEndElement(); // float_array

		writer.writeStartElement( "technique_common" );

		writer.writeStartElement( "accessor" );
		writer.writeAttribute( "source", positionArrayID );
		writer.writeAttribute( "count", DatatypeConverter.printInt( mesh.getVertexCount() ) );
		writer.writeAttribute( "stride", "3" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "X" );
		writer.writeAttribute( "type", "float" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "Y" );
		writer.writeAttribute( "type", "float" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "Z" );
		writer.writeAttribute( "type", "float" );

		writer.writeEndElement(); // accessor
		writer.writeEndElement(); // technique_common
		writer.writeEndElement(); // source

		int globalVertexCount = 0;

		/*
		 * Write vertex normals.
		 */
		final String normalID = geometryID + "-normal";
		final String normalArrayID = normalID + "-array";

		writer.writeStartElement( "source" );
		writer.writeAttribute( "id", normalID );

		writer.writeStartElement( "float_array" );
		writer.writeAttribute( "id", normalArrayID );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				globalVertexCount += face.getVertexCount();
			}
		}
		writer.writeAttribute( "count", DatatypeConverter.printInt( globalVertexCount * 3 ) );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( int i = 0; i < face.getVertexCount(); i++ )
				{
					final Vector3D normal = face.getVertexNormal( i );
					writer.writeCharacters( DatatypeConverter.printDouble( normal.x ) );
					writer.writeCharacters( " " );
					writer.writeCharacters( DatatypeConverter.printDouble( normal.y ) );
					writer.writeCharacters( " " );
					writer.writeCharacters( DatatypeConverter.printDouble( normal.z ) );
					writer.writeCharacters( " " );
				}
			}
		}
		writer.writeEndElement(); // float_array

		writer.writeStartElement( "technique_common" );

		writer.writeStartElement( "accessor" );
		writer.writeAttribute( "source", normalArrayID );
		writer.writeAttribute( "count", DatatypeConverter.printInt( globalVertexCount ) );
		writer.writeAttribute( "stride", "3" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "X" );
		writer.writeAttribute( "type", "float" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "Y" );
		writer.writeAttribute( "type", "float" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "Z" );
		writer.writeAttribute( "type", "float" );

		writer.writeEndElement(); // accessor
		writer.writeEndElement(); // technique_common
		writer.writeEndElement(); // source

		/*
		 * Write texture coordinates.
		 */
		final String texcoordID = geometryID + "-texcoord";
		final String texcoordArrayID = texcoordID + "-array";

		writer.writeStartElement( "source" );
		writer.writeAttribute( "id", texcoordID );

		writer.writeStartElement( "float_array" );
		writer.writeAttribute( "id", texcoordArrayID );

		writer.writeAttribute( "count", DatatypeConverter.printInt( globalVertexCount * 2 ) );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( final Face3D.Vertex vertex : face.getVertices() )
				{
					// TODO: Would be nice to avoid long lists of NaN for objects without textures.
					writer.writeCharacters( DatatypeConverter.printFloat( vertex.colorMapU ) );
					writer.writeCharacters( " " );
					writer.writeCharacters( DatatypeConverter.printFloat( vertex.colorMapV ) );
					writer.writeCharacters( " " );
				}
			}
		}
		writer.writeEndElement(); // float_array

		writer.writeStartElement( "technique_common" );

		writer.writeStartElement( "accessor" );
		writer.writeAttribute( "source", texcoordArrayID );
		writer.writeAttribute( "count", DatatypeConverter.printInt( globalVertexCount ) );
		writer.writeAttribute( "stride", "2" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "S" );
		writer.writeAttribute( "type", "float" );

		writer.writeEmptyElement( "param" );
		writer.writeAttribute( "name", "T" );
		writer.writeAttribute( "type", "float" );

		writer.writeEndElement(); // accessor
		writer.writeEndElement(); // technique_common
		writer.writeEndElement(); // source

		/*
		 * Define vertices.
		 */
		final String verticesID = geometryID + "-vertices";
		writer.writeStartElement( "vertices" );
		writer.writeAttribute( "id", verticesID );

		writer.writeEmptyElement( "input" );
		writer.writeAttribute( "semantic", "POSITION" );
		writer.writeAttribute( "source", "#" + positionID );

		writer.writeEndElement(); // vertices

		/*
		 * Write geometric primitives.
		 */
		globalVertexCount = 0;
		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			final Appearance appearance = faceGroup.getAppearance();
			final String materialID = getMaterialID( appearance );

			for ( final Face3D face : faceGroup.getFaces() )
			{
				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final String tagName;
					final int count;

					if ( primitive instanceof TriangleFan )
					{
						tagName = "trifans";

						// NOTE: According to Google SketchUp the number of triangles. According to the spec, it should be the number of nested <p> elements.
						count = primitive.getVertices().length - 2;
					}
					else if ( primitive instanceof TriangleStrip )
					{
						tagName = "tristrips";

						// NOTE: According to Google SketchUp the number of triangles. According to the spec, it should be the number of nested <p> elements.
						count = primitive.getVertices().length - 2;
					}
					else if ( primitive instanceof TriangleList )
					{
						tagName = "triangles";
						count = primitive.getVertices().length / 3;
					}
					else
					{
						throw new AssertionError( "Unsupported primitive: " + primitive );
					}

					writer.writeStartElement( tagName );
					writer.writeAttribute( "count", DatatypeConverter.printInt( count ) );
					writer.writeAttribute( "material", materialID );

					writer.writeEmptyElement( "input" );
					writer.writeAttribute( "semantic", "VERTEX" );
					writer.writeAttribute( "source", "#" + verticesID );
					writer.writeAttribute( "offset", "0" );

					writer.writeEmptyElement( "input" );
					writer.writeAttribute( "semantic", "NORMAL" );
					writer.writeAttribute( "source", "#" + normalID );
					writer.writeAttribute( "offset", "1" );

					writer.writeEmptyElement( "input" );
					writer.writeAttribute( "semantic", "TEXCOORD" );
					writer.writeAttribute( "source", "#" + texcoordID );
					writer.writeAttribute( "offset", "1" );
					writer.writeAttribute( "set", "0" );

					writer.writeStartElement( "p" );

					for ( final int vertexIndex : primitive.getVertices() )
					{
						final Face3D.Vertex vertex = face.getVertex( vertexIndex );
						final int globalVertexIndex = globalVertexCount + vertexIndex;

						writer.writeCharacters( DatatypeConverter.printInt( vertex.vertexCoordinateIndex ) );
						writer.writeCharacters( " " );

						writer.writeCharacters( DatatypeConverter.printInt( globalVertexIndex ) );
						writer.writeCharacters( " " );
					}

					writer.writeEndElement(); // p

					writer.writeEndElement(); // triangles, trifans, tristrips
				}

				globalVertexCount += face.getVertexCount();
			}
		}

		writer.writeEndElement(); // mesh

		writer.writeEndElement(); // geometry
	}

	/**
	 * Writes a <code>library_materials</code> element containing material
	 * definitions for any previously referenced materials.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeLibraryMaterials( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "library_materials" );

		for ( final Map.Entry<Appearance, String> entry : _libraryMaterials.entrySet() )
		{
			final String materialID = entry.getValue();
			final String effectID = "e" + materialID;

			writer.writeStartElement( "material" );
			writer.writeAttribute( "id", materialID );

			writer.writeEmptyElement( "instance_effect" );
			writer.writeAttribute( "url", "#" + effectID );

			writer.writeEndElement(); // material
		}

		writer.writeEndElement(); // library_materials
	}

	/**
	 * Returns the ID of the material that represents the given appearance,
	 * if it exists; otherwise a new material ID is allocated.
	 *
	 * @param   appearance  Appearance to get a material ID for.
	 *
	 * @return  Material ID for the given appearance.
	 */
	private String getMaterialID( final Appearance appearance )
	{
		String result = _libraryMaterials.get( appearance );
		if ( result == null )
		{
			result = allocateID( "m" );
			_libraryMaterials.put( appearance, result );
		}
		return result;
	}

	/**
	 * Writes a <code>library_effects</code> element containing definitions of
	 * the effects used to implement any previously referenced materials.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeLibraryEffects( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "library_effects" );

		for ( final Map.Entry<Appearance, String> entry : _libraryMaterials.entrySet() )
		{
			final Appearance appearance = entry.getKey();
			final String materialID = entry.getValue();
			final String effectID = "e" + materialID;

			writer.writeStartElement( "effect" );
			writer.writeAttribute( "id", effectID );

			writer.writeStartElement( "profile_COMMON" );

			String diffuseSamplerID = null;
			if ( appearance instanceof Material )
			{
				final Material material = (Material)appearance;
				if ( !TextTools.isEmpty( material.colorMap ) )
				{
					final URI uri = URI.create( material.colorMap + ".jpg" );
					diffuseSamplerID = writeTextureSampler( writer, uri );
				}
			}

			writer.writeStartElement( "technique" );
			writer.writeAttribute( "sid", "default" );

			writer.writeStartElement( "phong" );

			writer.writeStartElement( "ambient" );
			writeColor( writer, appearance.getAmbientColorRed(), appearance.getAmbientColorGreen(), appearance.getAmbientColorBlue(), 1.0f );
			writer.writeEndElement();

			writer.writeStartElement( "diffuse" );
			if ( diffuseSamplerID != null )
			{
				writer.writeEmptyElement( "texture" );
				writer.writeAttribute( "texture", diffuseSamplerID );
				writer.writeAttribute( "texcoord", "UVSET0" );
			}
			else
			{
				writeColor( writer, appearance.getDiffuseColorRed(), appearance.getDiffuseColorGreen(), appearance.getDiffuseColorBlue(), 1.0f );
			}
			writer.writeEndElement();

			writer.writeStartElement( "specular" );
			writeColor( writer, appearance.getSpecularColorRed(), appearance.getSpecularColorGreen(), appearance.getSpecularColorBlue(), 1.0f );
			writer.writeEndElement();

			writer.writeStartElement( "shininess" );
			writeFloat( writer, (float)appearance.getShininess() );
			writer.writeEndElement();

			final ReflectionMap reflectionMap = appearance.getReflectionMap();
			if ( reflectionMap != null )
			{
/*
				writer.writeStartElement( "reflective" );
				writeColor( writer, reflectionMap.getIntensityRed(), reflectionMap.getIntensityGreen(), reflectionMap.getIntensityBlue(), 1.0f );
				writer.writeEndElement();
*/

				writer.writeStartElement( "reflectivity" );
				writeFloat( writer, ( reflectionMap.getReflectivityMin() + reflectionMap.getReflectivityMax() ) / 2.0f );
				writer.writeEndElement();
			}

/*
			writer.writeStartElement( "transparent" );
			writeColor( writer, 0.0f, 0.0f, 0.0f, 1.0f );
			writer.writeEndElement();
*/

			writer.writeStartElement( "transparency" );
			writeFloat( writer, appearance.getDiffuseColorAlpha() );
			writer.writeEndElement();

			writer.writeEndElement(); // phong
			writer.writeEndElement(); // technique
			writer.writeEndElement(); // profile_COMMON
			writer.writeEndElement(); // effect

		}

		writer.writeEndElement(); // library_effects
	}

	/**
	 * Writes <code>newparam</code> elements needed to use the given image
	 * and returns the SID used to reference the texture.
	 *
	 * @param   writer  Writer to write to.
	 * @param   source  Source of the texture.
	 *
	 * @return  Texture image SID.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private String writeTextureSampler( final XMLStreamWriter writer, final URI source )
		throws XMLStreamException
	{
		final String imageID = getImageID( source );

		final String surfaceID = allocateID();
		writer.writeStartElement( "newparam" );
		writer.writeAttribute( "sid", surfaceID );
		writer.writeStartElement( "surface" );
		writer.writeAttribute( "type", "2D" );
		writer.writeStartElement( "init_from" );
		writer.writeCharacters( imageID );
		writer.writeEndElement(); // init_from
		writer.writeEndElement(); // surface
		writer.writeEndElement(); // newparam

		final String result = allocateID();
		writer.writeStartElement( "newparam" );
		writer.writeAttribute( "sid", result );
		writer.writeStartElement( "sampler2D" );
		writer.writeStartElement( "source" );
		writer.writeCharacters( surfaceID );
		writer.writeEndElement(); // source
		writer.writeEndElement(); // sampler2D
		writer.writeEndElement(); // newparam

		return result;
	}

	/**
	 * Writes a <code>float</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   value   Float value to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeFloat( final XMLStreamWriter writer, final float value )
		throws XMLStreamException
	{
		writer.writeStartElement( "float" );
		writer.writeCharacters( DatatypeConverter.printFloat( value ) );
		writer.writeEndElement();
	}

	/**
	 * Writes a <code>float3</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   x       First float value to be written.
	 * @param   y       Second float value to be written.
	 * @param   z       Third float value to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeFloat3( final XMLStreamWriter writer, final float x, final float y, final float z )
		throws XMLStreamException
	{
		writer.writeStartElement( "float3" );
		writer.writeCharacters( DatatypeConverter.printFloat( x ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printFloat( y ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printFloat( z ) );
		writer.writeEndElement();
	}

	/**
	 * Writes a <code>color</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   r       Red component to be written.
	 * @param   g       Green component to be written.
	 * @param   b       Blue component to be written.
	 * @param   a       Alpha component to be written.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeColor( final XMLStreamWriter writer, final float r, final float g, final float b, final float a )
		throws XMLStreamException
	{
		writer.writeStartElement( "color" );
		writer.writeCharacters( DatatypeConverter.printFloat( r ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printFloat( g ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printFloat( b ) );
		writer.writeCharacters( " " );
		writer.writeCharacters( DatatypeConverter.printFloat( a ) );
		writer.writeEndElement();
	}

	/**
	 * Writes a <code>library_images</code> element containing definitions of
	 * the images previously referenced by materials/effects.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeLibraryImages( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "library_images" );

		for ( final Map.Entry<URI, String> entry : _libraryImages.entrySet() )
		{
			final URI texture = entry.getKey();
			final String imageID = entry.getValue();

			writer.writeStartElement( "image" );
			writer.writeAttribute( "id", imageID );

			writer.writeStartElement( "init_from" );
			writer.writeCharacters( texture.toString() );
			writer.writeEndElement(); // init_from

			writer.writeEndElement(); // image
		}

		writer.writeEndElement(); // library_images
	}

	/**
	 * Returns the ID of the image with the given URI, if an ID is known;
	 * otherwise a new image ID is allocated.
	 *
	 * @param   source  URI of the image.
	 *
	 * @return  Image ID for the specified image.
	 */
	private String getImageID( final URI source )
	{
		String imageID = _libraryImages.get( source );
		if ( imageID == null )
		{
			imageID = allocateID( "i" );
			_libraryImages.put( source, imageID );
		}
		return imageID;
	}

	/**
	 * Returns the URIs of all images that were written during the most recent
	 * call to {@link #write}.
	 *
	 * @return  Image URIs.
	 */
	public Collection<URI> getImages()
	{
		return _libraryImages.keySet();
	}

	/**
	 * Writes a <code>scene</code> element, referring to the single visual
	 * scene defined by {@link #writeLibraryVisualScenes}.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLStreamException if the element can't be written.
	 */
	private void writeScene( final XMLStreamWriter writer )
		throws XMLStreamException
	{
		writer.writeStartElement( "scene" );

		writer.writeEmptyElement( "instance_visual_scene" );
		writer.writeAttribute( "url", "#scene" );

		writer.writeEndElement();
	}

	/**
	 * Converts an arbitrary string to a valid NCName. If the string is empty,
	 * which is not a valid NCName, this method returns <code>"_"</code>.
	 *
	 * @param   s   String to be converted.
	 *
	 * @return  Valid string for the NCName data type.
	 *
	 * @see     <a href="http://www.w3.org/TR/2000/CR-xmlschema-2-20001024/#NCName">XML Schema Part 2: Datatypes, section 3.3.9: NCName</a>
	 */
	private String convertToNCName( final String s )
	{
		final String result;

		if ( s.isEmpty() )
		{
			result = "_";
		}
		else
		{
			final int length = s.length();
			final StringBuilder builder = new StringBuilder( length );

			char c = s.charAt( 0 );
			if ( Character.isLetter( c ) || ( c == '_' ) )
			{
				builder.append( c );
			}
			else
			{
				builder.append( '_' );
			}

			for ( int i = 1 ; i < length ; i++ )
			{
				c = s.charAt( i );
				if ( Character.isLetterOrDigit( c ) || ( c == '_' ) )
				{
					builder.append( c );
				}
				else
				{
					builder.append( '_' );
				}
			}
			result = builder.toString();
		}

		return result;
	}
}
