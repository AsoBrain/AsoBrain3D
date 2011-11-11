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
package ab.j3d.loader;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.xml.*;
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
	 * ID of each material to be written in the
	 * <code>&lt;library_materials&gt;</code> section.
	 */
	private final Map<Appearance, String> _libraryMaterials;

	/**
	 * ID (value) of each image (key) to be written in the
	 * <code>&lt;library_images&gt;</code> section.
	 */
	private final Map<String, String> _libraryImages;

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
		_libraryGeometry = new LinkedHashMap<Node3D, String>();
		_libraryMaterials = new LinkedHashMap<Appearance, String>();
		_libraryImages = new LinkedHashMap<String, String>();
	}

	/**
	 * Writes the COLLADA file to the given output stream.
	 *
	 * @param   out     Output stream to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	public void write( final OutputStream out )
		throws XMLException
	{
		final XMLWriterFactory factory = XMLWriterFactory.newInstance();
		factory.setIndenting( true );

		final XMLWriter writer = factory.createXMLWriter( out, "UTF-8" );
		write( writer );
		writer.flush();
	}

	/**
	 * Writes the COLLADA file to the given writer.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	public void write( final XMLWriter writer )
		throws XMLException
	{
		_idMap.clear();
		_libraryGeometry.clear();
		_libraryMaterials.clear();
		_libraryImages.clear();

		writer.startDocument();

		writer.setPrefix( XMLConstants.DEFAULT_NS_PREFIX, NS_COLLADA_1_4 );
		writer.startTag( NS_COLLADA_1_4, "COLLADA" );
		writer.attribute( null, "version", "1.4.1" );

		writeAsset( writer );
		writeLibraryVisualScenes( writer );
		writeLibraryGeometries( writer );
		writeLibraryMaterials( writer );
		writeLibraryEffects( writer );
		writeLibraryImages( writer );
		writeScene( writer );

		writer.endTag( NS_COLLADA_1_4, "COLLADA" );

		writer.endDocument();
		writer.flush();
	}

	/**
	 * Allocates a unique ID.
	 *
	 * @return  Allocated ID.
	 */
	protected String allocateID()
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
	protected String allocateID( @NotNull final String prefix )
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
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeAsset( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "asset" );

		final String currentDateTime = DatatypeConverter.printDateTime( Calendar.getInstance() );

		writer.startTag( NS_COLLADA_1_4, "created" );
		writer.text( currentDateTime );
		writer.endTag( NS_COLLADA_1_4, "created" );

		writer.startTag( NS_COLLADA_1_4, "modified" );
		writer.text( currentDateTime );
		writer.endTag( NS_COLLADA_1_4, "modified" );

		writer.emptyTag( NS_COLLADA_1_4, "unit" );
		writer.attribute( null, "name", "scene_unit" );
		writer.attribute( null, "meter", DatatypeConverter.printDouble( _scene.getUnit() ) );
		writer.endTag( NS_COLLADA_1_4, "unit" );

		writer.startTag( NS_COLLADA_1_4, "up_axis" );
		writer.text( "Z_UP" );
		writer.endTag( NS_COLLADA_1_4, "up_axis" );

		writer.endTag( NS_COLLADA_1_4, "asset" );
	}

	/**
	 * Writes a <code>library_visual_scenes</code> element.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeLibraryVisualScenes( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "library_visual_scenes" );
		writeVisualScene( writer );
		writer.endTag( NS_COLLADA_1_4, "library_visual_scenes" );
	}

	/**
	 * Writes a <code>visual_scene</code> element.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeVisualScene( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "visual_scene" );
		writer.attribute( null, "id", "scene" );

		for ( final ContentNode contentNode : _scene.getContentNodes() )
		{
			writeNode( writer, contentNode );
		}

		writer.endTag( NS_COLLADA_1_4, "visual_scene" );
	}

	/**
	 * Writes a <code>node</code> hierarchy for the given content node.
	 *
	 * @param   writer          Writer to write to.
	 * @param   contentNode     Content node to be written.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeNode( final XMLWriter writer, final ContentNode contentNode )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "node" );
		writer.attribute( null, "name", convertToNCName( String.valueOf( contentNode.getID() ) ) );

		writeMatrix( writer, contentNode.getTransform() );
		writeNode( writer, contentNode.getNode3D() );

		writer.endTag( NS_COLLADA_1_4, "node" );
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
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeNode( final XMLWriter writer, final Node3D node )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "node" );

		final Object tag = node.getTag();
		if ( tag != null )
		{
			writer.attribute( null, "name", convertToNCName( String.valueOf( tag ) ) );
		}

		if ( node instanceof Object3D )
		{
			String geometryID = _libraryGeometry.get( node );
			if ( geometryID == null )
			{
				geometryID = allocateID();
				_libraryGeometry.put( node, geometryID );
			}

			writer.startTag( NS_COLLADA_1_4, "instance_geometry" );
			writer.attribute( null, "url", '#' + geometryID );

			writer.startTag( NS_COLLADA_1_4, "bind_material" );
			writer.startTag( NS_COLLADA_1_4, "technique_common" );

			final Set<String> boundMaterials = new HashSet<String>();

			final Object3D mesh = (Object3D)node;
			for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
			{
				final Appearance appearance = faceGroup.getAppearance();
				final String materialID = getMaterialID( appearance );
				if ( boundMaterials.add( materialID ) )
				{
					writer.startTag( NS_COLLADA_1_4, "instance_material" );
					writer.attribute( null, "symbol", materialID );
					writer.attribute( null, "target", '#' + materialID );

					writer.emptyTag( NS_COLLADA_1_4, "bind_vertex_input" );
					writer.attribute( null, "semantic", "UVSET0" );
					writer.attribute( null, "input_semantic", "TEXCOORD" );
					writer.attribute( null, "input_set", "0" );
					writer.endTag( NS_COLLADA_1_4, "bind_vertex_input" );

					writer.endTag( NS_COLLADA_1_4, "instance_material" );
				}
			}

			writer.endTag( NS_COLLADA_1_4, "technique_common" );
			writer.endTag( NS_COLLADA_1_4, "bind_material" );

			writer.endTag( NS_COLLADA_1_4, "instance_geometry" );
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

		writer.endTag( NS_COLLADA_1_4, "node" );
	}

	/**
	 * Writes a <code>matrix</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   matrix  Matrix to be written.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeMatrix( final XMLWriter writer, final Matrix3D matrix )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "matrix" );

		writer.text( DatatypeConverter.printDouble( matrix.xx ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.xy ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.xz ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.xo ) );
		writer.text( "  " );
		writer.text( DatatypeConverter.printDouble( matrix.yx ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.yy ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.yz ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.yo ) );
		writer.text( "  " );
		writer.text( DatatypeConverter.printDouble( matrix.zx ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.zy ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.zz ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( matrix.zo ) );
		writer.text( "  " );
		writer.text( DatatypeConverter.printDouble( 0.0 ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( 0.0 ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( 0.0 ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printDouble( 1.0 ) );

		writer.endTag( NS_COLLADA_1_4, "matrix" );
	}

	/**
	 * Writes a <code>library_geometries</code> element containing all of the
	 * geometry referenced by {@link #writeLibraryVisualScenes}.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeLibraryGeometries( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "library_geometries" );

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

		writer.endTag( NS_COLLADA_1_4, "library_geometries" );
	}

	/**
	 * Writes a <code>geometry</code> element for the given geometry.
	 *
	 * @param   writer      Writer to write to.
	 * @param   mesh        Geometry to be written.
	 * @param   geometryID  ID of the geometry element.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeGeometry( final XMLWriter writer, final Object3D mesh, final String geometryID )
		throws XMLException
	{
		/*
		 * Write vertex coordinates.
		 */
		final String positionID = geometryID + "-position";
		final String positionArrayID = positionID + "-array";

		writer.startTag( NS_COLLADA_1_4, "geometry" );
		writer.attribute( null, "id", geometryID );

		writer.startTag( NS_COLLADA_1_4, "mesh" );

		writer.startTag( NS_COLLADA_1_4, "source" );
		writer.attribute( null, "id", positionID );

		writer.startTag( NS_COLLADA_1_4, "float_array" );
		writer.attribute( null, "id", positionArrayID );
		writer.attribute( null, "count", DatatypeConverter.printInt( mesh.getVertexCount() * 3 ) );
		for ( final Vector3D position : mesh.getVertexCoordinates() )
		{
			writer.text( DatatypeConverter.printDouble( position.x ) );
			writer.text( " " );
			writer.text( DatatypeConverter.printDouble( position.y ) );
			writer.text( " " );
			writer.text( DatatypeConverter.printDouble( position.z ) );
			writer.text( " " );
		}
		writer.endTag( NS_COLLADA_1_4, "float_array" );

		writer.startTag( NS_COLLADA_1_4, "technique_common" );

		writer.startTag( NS_COLLADA_1_4, "accessor" );
		writer.attribute( null, "source", positionArrayID );
		writer.attribute( null, "count", DatatypeConverter.printInt( mesh.getVertexCount() ) );
		writer.attribute( null, "stride", "3" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "X" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "Y" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "Z" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.endTag( NS_COLLADA_1_4, "accessor" );
		writer.endTag( NS_COLLADA_1_4, "technique_common" );
		writer.endTag( NS_COLLADA_1_4, "source" );

		int globalVertexCount = 0;

		/*
		 * Write vertex normals.
		 */
		final String normalID = geometryID + "-normal";
		final String normalArrayID = normalID + "-array";

		writer.startTag( NS_COLLADA_1_4, "source" );
		writer.attribute( null, "id", normalID );

		writer.startTag( NS_COLLADA_1_4, "float_array" );
		writer.attribute( null, "id", normalArrayID );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				globalVertexCount += face.getVertexCount();
			}
		}
		writer.attribute( null, "count", DatatypeConverter.printInt( globalVertexCount * 3 ) );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( int i = 0; i < face.getVertexCount(); i++ )
				{
					final Vector3D normal = face.getVertexNormal( i );
					writer.text( DatatypeConverter.printDouble( normal.x ) );
					writer.text( " " );
					writer.text( DatatypeConverter.printDouble( normal.y ) );
					writer.text( " " );
					writer.text( DatatypeConverter.printDouble( normal.z ) );
					writer.text( " " );
				}
			}
		}
		writer.endTag( NS_COLLADA_1_4, "float_array" );

		writer.startTag( NS_COLLADA_1_4, "technique_common" );

		writer.startTag( NS_COLLADA_1_4, "accessor" );
		writer.attribute( null, "source", normalArrayID );
		writer.attribute( null, "count", DatatypeConverter.printInt( globalVertexCount ) );
		writer.attribute( null, "stride", "3" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "X" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "Y" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "Z" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.endTag( NS_COLLADA_1_4, "accessor" );
		writer.endTag( NS_COLLADA_1_4, "technique_common" );
		writer.endTag( NS_COLLADA_1_4, "source" );

		/*
		 * Write texture coordinates.
		 */
		final String texcoordID = geometryID + "-texcoord";
		final String texcoordArrayID = texcoordID + "-array";

		writer.startTag( NS_COLLADA_1_4, "source" );
		writer.attribute( null, "id", texcoordID );

		writer.startTag( NS_COLLADA_1_4, "float_array" );
		writer.attribute( null, "id", texcoordArrayID );

		writer.attribute( null, "count", DatatypeConverter.printInt( globalVertexCount * 2 ) );

		for ( final FaceGroup faceGroup : mesh.getFaceGroups() )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( final Face3D.Vertex vertex : face.getVertices() )
				{
					// TODO: Would be nice to avoid long lists of NaN for objects without textures.
					writer.text( DatatypeConverter.printFloat( vertex.colorMapU ) );
					writer.text( " " );
					writer.text( DatatypeConverter.printFloat( vertex.colorMapV ) );
					writer.text( " " );
				}
			}
		}
		writer.endTag( NS_COLLADA_1_4, "float_array" );

		writer.startTag( NS_COLLADA_1_4, "technique_common" );

		writer.startTag( NS_COLLADA_1_4, "accessor" );
		writer.attribute( null, "source", texcoordArrayID );
		writer.attribute( null, "count", DatatypeConverter.printInt( globalVertexCount ) );
		writer.attribute( null, "stride", "2" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "S" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.emptyTag( NS_COLLADA_1_4, "param" );
		writer.attribute( null, "name", "T" );
		writer.attribute( null, "type", "float" );
		writer.endTag( NS_COLLADA_1_4, "param" );

		writer.endTag( NS_COLLADA_1_4, "accessor" );
		writer.endTag( NS_COLLADA_1_4, "technique_common" );
		writer.endTag( NS_COLLADA_1_4, "source" );

		/*
		 * Define vertices.
		 */
		final String verticesID = geometryID + "-vertices";
		writer.startTag( NS_COLLADA_1_4, "vertices" );
		writer.attribute( null, "id", verticesID );

		writer.emptyTag( NS_COLLADA_1_4, "input" );
		writer.attribute( null, "semantic", "POSITION" );
		writer.attribute( null, "source", '#' + positionID );
		writer.endTag( NS_COLLADA_1_4, "input" );

		writer.endTag( NS_COLLADA_1_4, "vertices" );

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

					writer.startTag( NS_COLLADA_1_4, tagName );
					writer.attribute( null, "count", DatatypeConverter.printInt( count ) );
					writer.attribute( null, "material", materialID );

					writer.emptyTag( NS_COLLADA_1_4, "input" );
					writer.attribute( null, "semantic", "VERTEX" );
					writer.attribute( null, "source", '#' + verticesID );
					writer.attribute( null, "offset", "0" );
					writer.endTag( NS_COLLADA_1_4, "input" );

					writer.emptyTag( NS_COLLADA_1_4, "input" );
					writer.attribute( null, "semantic", "NORMAL" );
					writer.attribute( null, "source", '#' + normalID );
					writer.attribute( null, "offset", "1" );
					writer.endTag( NS_COLLADA_1_4, "input" );

					writer.emptyTag( NS_COLLADA_1_4, "input" );
					writer.attribute( null, "semantic", "TEXCOORD" );
					writer.attribute( null, "source", '#' + texcoordID );
					writer.attribute( null, "offset", "1" );
					writer.attribute( null, "set", "0" );
					writer.endTag( NS_COLLADA_1_4, "input" );

					writer.startTag( NS_COLLADA_1_4, "p" );

					for ( final int vertexIndex : primitive.getVertices() )
					{
						final Face3D.Vertex vertex = face.getVertex( vertexIndex );
						final int globalVertexIndex = globalVertexCount + vertexIndex;

						writer.text( DatatypeConverter.printInt( vertex.vertexCoordinateIndex ) );
						writer.text( " " );

						writer.text( DatatypeConverter.printInt( globalVertexIndex ) );
						writer.text( " " );
					}

					writer.endTag( NS_COLLADA_1_4, "p" );

					writer.endTag( NS_COLLADA_1_4, tagName );
				}

				globalVertexCount += face.getVertexCount();
			}
		}

		writer.endTag( NS_COLLADA_1_4, "mesh" );

		writer.endTag( NS_COLLADA_1_4, "geometry" );
	}

	/**
	 * Writes a <code>library_materials</code> element containing material
	 * definitions for any previously referenced materials.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeLibraryMaterials( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "library_materials" );

		for ( final Map.Entry<Appearance, String> entry : _libraryMaterials.entrySet() )
		{
			final String materialID = entry.getValue();
			final String effectID = 'e' + materialID;

			writer.startTag( NS_COLLADA_1_4, "material" );
			writer.attribute( null, "id", materialID );

			writer.emptyTag( NS_COLLADA_1_4, "instance_effect" );
			writer.attribute( null, "url", '#' + effectID );
			writer.endTag( NS_COLLADA_1_4, "instance_effect" );

			writer.endTag( NS_COLLADA_1_4, "material" );
		}

		writer.endTag( NS_COLLADA_1_4, "library_materials" );
	}

	/**
	 * Returns the ID of the material that represents the given appearance,
	 * if it exists; otherwise a new material ID is allocated.
	 *
	 * @param   appearance  Appearance to get a material ID for.
	 *
	 * @return  Material ID for the given appearance.
	 */
	protected String getMaterialID( final Appearance appearance )
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
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeLibraryEffects( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "library_effects" );

		for ( final Map.Entry<Appearance, String> entry : _libraryMaterials.entrySet() )
		{
			final Appearance appearance = entry.getKey();
			final String materialID = entry.getValue();
			final String effectID = 'e' + materialID;

			writer.startTag( NS_COLLADA_1_4, "effect" );
			writer.attribute( null, "id", effectID );

			writer.startTag( NS_COLLADA_1_4, "profile_COMMON" );

			final Color4 ambientColor = appearance.getAmbientColor();
			final Color4 diffuseColor = appearance.getDiffuseColor();
			final Color4 specularColor = appearance.getSpecularColor();
			final TextureMap colorMap = appearance.getColorMap();
			final CubeMap reflectionMap = appearance.getReflectionMap();

			String diffuseSamplerID = null;
			if ( colorMap != null )
			{
				final URL colorMapUrl = colorMap.getImageUrl();
				if ( colorMapUrl != null )
				{
					diffuseSamplerID = writeTextureSampler( writer, colorMapUrl.toExternalForm() );
				}
			}

			writer.startTag( NS_COLLADA_1_4, "technique" );
			writer.attribute( null, "sid", "default" );

			writer.startTag( NS_COLLADA_1_4, "phong" );

			writer.startTag( NS_COLLADA_1_4, "ambient" );
			writeColor( writer, ambientColor.getRedFloat(), ambientColor.getGreenFloat(), ambientColor.getBlueFloat(), 1.0f );
			writer.endTag( NS_COLLADA_1_4, "ambient" );

			writer.startTag( NS_COLLADA_1_4, "diffuse" );
			if ( diffuseSamplerID != null )
			{
				writer.emptyTag( NS_COLLADA_1_4, "texture" );
				writer.attribute( null, "texture", diffuseSamplerID );
				writer.attribute( null, "texcoord", "UVSET0" );
				writer.endTag( NS_COLLADA_1_4, "texture" );
			}
			else
			{
				writeColor( writer, diffuseColor.getRedFloat(), diffuseColor.getGreenFloat(), diffuseColor.getBlueFloat(), 1.0f );
			}
			writer.endTag( NS_COLLADA_1_4, "diffuse" );

			writer.startTag( NS_COLLADA_1_4, "specular" );
			writeColor( writer, specularColor.getRedFloat(), specularColor.getGreenFloat(), specularColor.getBlueFloat(), 1.0f );
			writer.endTag( NS_COLLADA_1_4, "specular" );

			writer.startTag( NS_COLLADA_1_4, "shininess" );
			writeFloat( writer, (float)appearance.getShininess() );
			writer.endTag( NS_COLLADA_1_4, "shininess" );

			if ( reflectionMap != null )
			{
/*
				writer.startTag( NS_COLLADA_1_4, "reflective" );
				writeColor( writer, reflectionMap.getIntensityRed(), reflectionMap.getIntensityGreen(), reflectionMap.getIntensityBlue(), 1.0f );
				writer.endTag( NS_COLLADA_1_4, "reflective" );
*/

				writer.startTag( NS_COLLADA_1_4, "reflectivity" );
				writeFloat( writer, ( appearance.getReflectionMin() + appearance.getReflectionMax() ) / 2.0f );
				writer.endTag( NS_COLLADA_1_4, "reflectivity" );
			}

/*
			writer.startTag( NS_COLLADA_1_4, "transparent" );
			writeColor( writer, 0.0f, 0.0f, 0.0f, 1.0f );
			writer.endTag( NS_COLLADA_1_4, "transparent" );
*/

			writer.startTag( NS_COLLADA_1_4, "transparency" );
			writeFloat( writer, diffuseColor.getAlphaFloat() );
			writer.endTag( NS_COLLADA_1_4, "transparency" );

			writer.endTag( NS_COLLADA_1_4, "phong" );
			writer.endTag( NS_COLLADA_1_4, "technique" );
			writer.endTag( NS_COLLADA_1_4, "profile_COMMON" );
			writer.endTag( NS_COLLADA_1_4, "effect" );

		}

		writer.endTag( NS_COLLADA_1_4, "library_effects" );
	}

	/**
	 * Writes <code>newparam</code> elements needed to use the given image
	 * and returns the SID used to reference the texture.
	 *
	 * @param   writer      Writer to write to.
	 * @param   imageUrl    Texture image URL.
	 *
	 * @return  Texture image SID.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected String writeTextureSampler( final XMLWriter writer, final String imageUrl )
		throws XMLException
	{
		final String imageID = getImageID( imageUrl );

		final String surfaceID = allocateID();
		writer.startTag( NS_COLLADA_1_4, "newparam" );
		writer.attribute( null, "sid", surfaceID );
		writer.startTag( NS_COLLADA_1_4, "surface" );
		writer.attribute( null, "type", "2D" );
		writer.startTag( NS_COLLADA_1_4, "init_from" );
		writer.text( imageID );
		writer.endTag( NS_COLLADA_1_4, "init_from" );
		writer.endTag( NS_COLLADA_1_4, "surface" );
		writer.endTag( NS_COLLADA_1_4, "newparam" );

		final String result = allocateID();
		writer.startTag( NS_COLLADA_1_4, "newparam" );
		writer.attribute( null, "sid", result );
		writer.startTag( NS_COLLADA_1_4, "sampler2D" );
		writer.startTag( NS_COLLADA_1_4, "source" );
		writer.text( surfaceID );
		writer.endTag( NS_COLLADA_1_4, "source" );
		writer.endTag( NS_COLLADA_1_4, "sampler2D" );
		writer.endTag( NS_COLLADA_1_4, "newparam" );

		return result;
	}

	/**
	 * Writes a <code>float</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   value   Float value to be written.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeFloat( final XMLWriter writer, final float value )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "float" );
		writer.text( DatatypeConverter.printFloat( value ) );
		writer.endTag( NS_COLLADA_1_4, "float" );
	}

	/**
	 * Writes a <code>float3</code> element.
	 *
	 * @param   writer  Writer to write to.
	 * @param   x       First float value to be written.
	 * @param   y       Second float value to be written.
	 * @param   z       Third float value to be written.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeFloat3( final XMLWriter writer, final float x, final float y, final float z )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "float3" );
		writer.text( DatatypeConverter.printFloat( x ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printFloat( y ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printFloat( z ) );
		writer.endTag( NS_COLLADA_1_4, "float3" );
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
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeColor( final XMLWriter writer, final float r, final float g, final float b, final float a )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "color" );
		writer.text( DatatypeConverter.printFloat( r ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printFloat( g ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printFloat( b ) );
		writer.text( " " );
		writer.text( DatatypeConverter.printFloat( a ) );
		writer.endTag( NS_COLLADA_1_4, "color" );
	}

	/**
	 * Writes a <code>library_images</code> element containing definitions of
	 * the images previously referenced by materials/effects.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeLibraryImages( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "library_images" );

		for ( final Map.Entry<String, String> entry : _libraryImages.entrySet() )
		{
			final String imageUrl = entry.getKey();
			final String imageId = entry.getValue();

			writeImage( writer, imageId, imageUrl );
		}

		writer.endTag( NS_COLLADA_1_4, "library_images" );
	}

	/**
	 * Writes an <code>image</code> element for an external image file.
	 *
	 * @param   writer      Writer to write to.
	 * @param   imageId     Unique identifier of <code>image</code> element.
	 * @param   imageUri    URI of image file.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeImage( final XMLWriter writer, final String imageId, final String imageUri )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "image" );
		writer.attribute( null, "id", imageId );

		writer.startTag( NS_COLLADA_1_4, "init_from" );
		writer.text( imageUri );
		writer.endTag( NS_COLLADA_1_4, "init_from" );

		writer.endTag( NS_COLLADA_1_4, "image" );
	}

	/**
	 * Returns the ID of the image with the given URL, if an ID is known;
	 * otherwise a new image ID is allocated.
	 *
	 * @param   source  URL of the image.
	 *
	 * @return  Image ID for the specified image.
	 */
	protected String getImageID( final String source )
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
	 * Returns the URLs of all images that were written during the most recent
	 * call to {@link #write}.
	 *
	 * @return  Image URLs.
	 */
	public Collection<String> getImages()
	{
		return _libraryImages.keySet();
	}

	/**
	 * Writes a <code>scene</code> element, referring to the single visual
	 * scene defined by {@link #writeLibraryVisualScenes}.
	 *
	 * @param   writer  Writer to write to.
	 *
	 * @throws  XMLException if there's a problem writing the XML document.
	 */
	protected void writeScene( final XMLWriter writer )
		throws XMLException
	{
		writer.startTag( NS_COLLADA_1_4, "scene" );

		writer.emptyTag( NS_COLLADA_1_4, "instance_visual_scene" );
		writer.attribute( null, "url", "#scene" );
		writer.endTag( NS_COLLADA_1_4, "instance_visual_scene" );

		writer.endTag( NS_COLLADA_1_4, "scene" );
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
	protected String convertToNCName( final String s )
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