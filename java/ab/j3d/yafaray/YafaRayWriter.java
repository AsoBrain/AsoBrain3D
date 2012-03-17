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

package ab.j3d.yafaray;

import java.io.*;
import java.net.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.xml.*;
import org.jetbrains.annotations.*;

/**
 * Writes a YafaRay scene.
 *
 * <p>
 * Documentation about the YafaRay XML-format is limited, but some useful
 * references are:
 * <ul>
 *     <li><a href="http://www.yafaray.org/development/documentation/XMLspecs">YafaRay XML scene specifications</a></li>
 *     <li><a href="http://www.yafaray.org/development/documentation/XMLparameters">YafaRay XML scene parameters</a></li>
 * </ul>
 * </p>
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class YafaRayWriter
{
	/**
	 * XML writer to be used.
	 */
	private final XMLWriter _writer;

	/**
	 * Maps appearances to YafaRay material identifiers.
	 */
	private final Map<Appearance, String> _appearanceMap = new HashMap<Appearance, String>();

	/**
	 * Index used to generate unique material names.
	 */
	private int _materialIndex = 0;

	/**
	 * Index used to generate unique light names.
	 */
	public int _lightIndex = 0;

	/**
	 * Width of the image.
	 */
	private int _width = 1024;

	/**
	 * Height of the image.
	 */
	private int _height = 768;

	/**
	 * Camera location.
	 */
	private Vector3D _cameraFrom;

	/**
	 * Camera target.
	 */
	private Vector3D _cameraTo;

	/**
	 * Constructs a new instance.
	 *
	 * @param   out     Output stream to write to.
	 *
	 * @throws  XMLException if no {@link XMLWriter} can be created.
	 */
	public YafaRayWriter( final OutputStream out )
	throws XMLException
	{
		final XMLWriterFactory writerFactory = XMLWriterFactory.newInstance();
		writerFactory.setIndenting( true );
		_writer = writerFactory.createXMLWriter( out, "UTF-8" );
	}

	/**
	 * Sets the size of the image to be rendered.
	 *
	 * @param   width   Width of the image.
	 * @param   height  Height of the image.
	 */
	public void setOutputSize( final int width, final int height )
	{
		_width = width;
		_height = height;
	}

	/**
	 * Sets the location and target of the camera.
	 *
	 * @param   from    Location of the camera.
	 * @param   to      Target that the camera is pointed at.
	 */
	public void setCamera( final Vector3D from, final Vector3D to )
	{
		_cameraFrom = from;
		_cameraTo = to;
	}

	/**
	 * Writes an YafaRay scene specification for the given scene.
	 *
	 * @param   scene   Scene to be written.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	public void write( final Scene scene )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.startDocument();

		writer.startTag( null, "scene" );
		writer.attribute( null, "type", "triangle" );

		scene.walk( new Node3DVisitor()
		{
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				final Node3D node = path.getNode();

				try
				{
					if ( node instanceof Object3D )
					{
						final Object3D object = (Object3D)node;
						for ( final FaceGroup faceGroup : object.getFaceGroups() )
						{
							final Appearance appearance = faceGroup.getAppearance();
							String identifier = _appearanceMap.get( appearance );
							if ( identifier== null )
							{
								identifier = writeMaterial( appearance );
								_appearanceMap.put( appearance, identifier );
							}
						}

						writeMesh( object, path.getTransform() );
					}
					else if ( node instanceof Light3D )
					{
						final Light3D light = (Light3D)node;
						final Matrix3D transform = path.getTransform();

						writer.startTag( null, "light" );
						writer.attribute( null, "name", "light" + _lightIndex++ );
						writeValue( "type", "spherelight" );
//						writeValue( "type", "pointlight" );
						writeColor( "color", (double)light.getDiffuseRed(), (double)light.getDiffuseGreen(), (double)light.getDiffuseBlue() );
						writePoint( "from", transform.getTranslation() );

						final double radius = 0.1;
						final double power = 1.0;

						writeFloat( "power", power / ( radius * radius ) );
						writeFloat( "radius", radius );
						writeInteger( "samples", 16 );
/*
						<light name="Lamp.001">
						<color r="1" g="1" b="1" a="1"/>
						<corner x="-0.25" y="-0.25" z="1.99646"/>
						<from x="0" y="0" z="1.99646"/>
						<point1 x="-0.25" y="0.25" z="1.99646"/>
						<point2 x="0.25" y="-0.25" z="1.99646"/>
						<power fval="5"/>
						<samples ival="16"/>
						<type sval="arealight"/>
*/
						writer.endTag( null, "light" );
					}
				}
				catch ( XMLException e )
				{
					throw new RuntimeException( e );
				}

				return true;
			}
		} );

		writer.startTag( null, "camera" );
		final String cameraName = "camera0";
		writer.attribute( null, "name", cameraName );

		writeValue( "type", "perspective" );
		writePoint( "from", _cameraFrom );
		writePoint( "to", _cameraTo );
		final Vector3D cameraDirection = _cameraFrom.directionTo( _cameraTo );
		final Vector3D left = Vector3D.cross( cameraDirection, Vector3D.POSITIVE_Z_AXIS.multiply( 1.0 / 0.001 ) );
		final Vector3D up = Vector3D.cross( left, cameraDirection );
		writePoint( "up", _cameraFrom.plus( up ) );
		writeInteger( "resx", _width );
		writeInteger( "resy", _height );

		writer.endTag( null, "camera" );
/*
		<camera name="cam">
			...
			<aperture fval="0"/>
			<bokeh_rotation fval="0"/>
			<bokeh_type sval="disk1"/>
			<dof_distance fval="0"/>
			<focal fval="1.37374"/>
		</camera>
*/

		final Vector3D sunDirection = Vector3D.normalize( -1.0, -0.5, 3.5 );
//		Vector3D sunDirection = Vector3D.normalize( 1.0, 0.0, 2.0 );
//		Vector3D sunDirection = Vector3D.normalize( -0.5, 1.0, -2.0 );
		writer.startTag( null, "light" );
		writer.attribute( null, "name", "light" + _lightIndex++ );
		writeValue( "type", "sunlight" );
		writeFloat( "angle", 0.5 );
		writeColor( "color", 1.0, 1.0, 1.0 );
		writeVector( "direction", sunDirection );
		writeFloat( "power", 1.0 );
//		writeInteger( "samples", 16 );
		writer.endTag( null, "light" );

/*
		final String backgroundName = "background0";
		writer.startTag( null, "background" );
		writer.attribute( null, "name", backgroundName );
		writeValue( "type", "constant" );
		writeColor( "color", 1.0, 1.0, 1.0 );
		writer.endTag( null, "background" );
*/

		final String backgroundName = "background0";
		writer.startTag( null, "background" );
		writer.attribute( null, "name", backgroundName );
		writeValue( "type", "sunsky" );
		writeVector( "from", sunDirection );
		writer.endTag( null, "background" );

/*
		<integrator name="default">
			<bounces ival="3"/>
			<caustic_mix ival="5"/>
			<diffuseRadius fval="1"/>
			<fg_bounces ival="3"/>
			<fg_samples ival="32"/>
			<raydepth ival="4"/>
			<search ival="150"/>
			<shadowDepth ival="2"/>
			<show_map bval="false"/>
			<transpShad bval="false"/>
			<use_background bval="false"/>
		</integrator>
*/

		final String integratorName = "integrator0";
		writer.startTag( null, "integrator" );
		writer.attribute( null, "name", integratorName );
		switch ( 2 )
		{
			case 0:
			{
				writeValue( "type", "directlighting" );
				break;
			}

			case 1:
			{
				writeValue( "type", "pathtracing" );
				break;
			}

			case 2:
			{
				writeValue( "type", "photonmapping" );
//				writeInteger( "search", 160 );
				writeInteger( "photons", 200000 );
				writeBoolean( "finalGather", true );
				writeInteger( "fg_samples", 64 );
				writeBoolean( "use_background", false );
//				writeBoolean( "show_map", true );
				break;
			}
		}
		writer.endTag( null, "integrator" );

		final String volumeIntegratorName = "integrator1";
		writer.startTag( null, "integrator" );
		writer.attribute( null, "name", volumeIntegratorName );
		writeValue( "type", "none" );
		writer.endTag( null, "integrator" );

		writer.startTag( null, "render" );
		writeValue( "camera_name", cameraName );
		writeValue( "background_name", backgroundName );
		writeValue( "integrator_name", integratorName );
		writeValue( "volintegrator_name", volumeIntegratorName );
		writeInteger( "threads", Math.max( 1, Runtime.getRuntime().availableProcessors() - 1 ) );

		writeFloat( "gamma", 2.2 );
		writeInteger( "width", _width );
		writeInteger( "height", _height );
		writeInteger( "xstart", 0 );
		writeInteger( "ystart", 0 );
//		``writeValue( "filter_type", "mitchell" );

		writeInteger( "AA_inc_samples", 2 );
		writeInteger( "AA_minsamples", 2 );
		writeInteger( "AA_passes", 2 );
		writeFloat( "AA_pixelwidth", 1.5 );
		writeFloat( "AA_threshold", 0.05 );
/*
		<background_name sval="world_background"/>
		<clamp_rgb bval="true"/>
		<filter_type sval="mitchell"/>
		<integrator_name sval="default"/>
		<volintegrator_name sval="volintegr"/>
		<z_channel bval="true"/>
*/
		writer.endTag( null, "render" );

		writer.endTag( null, "scene" );

		writer.endDocument();
		writer.flush();
	}

	/**
	 * Writes a YafaRay material reprseenting the given appearance.
	 *
	 * @param   appearance  Appearance to be written.
	 *
	 * @return  Name of the YafaRay material.
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private String writeMaterial( final Appearance appearance )
	throws XMLException
	{
		final int materialIndex = _materialIndex++;
		final String name = "material" + materialIndex;
		String textureMapperName = null;
		String textureName = null;

		final XMLWriter writer = _writer;

		final TextureMap colorMap = appearance.getColorMap();
		if ( colorMap != null )
		{
			final URL imageUrl = colorMap.getImageUrl();
			if ( imageUrl != null )
			{
				final URI imageUri;
				try
				{
					imageUri = imageUrl.toURI();
				}
				catch ( URISyntaxException e )
				{
					throw new RuntimeException( e );
				}

				// FIXME: Hard-coded image URI (web server)
				final URI imageRoot = URI.create( "http://localhost/ivenza/images/decors/" );
				final URI relativeImage = imageRoot.relativize( imageUri );

				textureName = "texture" + materialIndex;
	/*
	<texture name="t1">
		<calc_alpha bval="true"/>
		<clipping sval="repeat"/>
		<cropmax_x fval="1"/>
		<cropmax_y fval="1"/>
		<cropmin_x fval="0"/>
		<cropmin_y fval="0"/>
		<filename sval="C:\WallPapers\Lotus.jpg"/>
		<gamma fval="2"/>
		<type sval="image"/>
		<use_alpha bval="true"/>
		<xrepeat ival="1"/>
		<yrepeat ival="1"/>
	</texture>
	*/
				writer.startTag( null, "texture" );
				writer.attribute( null, "name", textureName );
				writeValue( "type", "image" );
				// FIXME: Hard-coded image URI (local storage)
				writeValue( "filename", new File( "D:/numdata/soda/Ivenza_EnterpriseWeb/webapp/images/decors", relativeImage.toString() ).toString() );
				writer.endTag( null, "texture" );
			}
		}

		writer.startTag( null, "material" );
		writer.attribute( null, "name", name );

		writeValue( "type", "shinydiffusemat" );
		writeColor( "color", appearance.getDiffuseColor() );
		writeFloat( "transparency", 1.0 - (double)appearance.getDiffuseColor().getAlphaFloat() );
		if ( appearance.getDiffuseColor().getAlphaFloat() < 0.5f )
		{
			writeFloat( "IOR", 1520.0 );
		}

		if ( colorMap != null )
		{
			textureMapperName = "textureMapper" + materialIndex;

			writer.startTag( null, "list_element" );

			writeValue( "element", "shader_node" );
			writeValue( "type", "texture_mapper" );
			writeValue( "name", textureMapperName );
			writeValue( "texture", textureName );
			writeValue( "texco", "uv" );

			writer.endTag( null, "list_element" );
		}

		if ( textureMapperName != null )
		{
			writeValue( "diffuse_shader", textureMapperName );
		}

		if ( appearance.getReflectionMap() != null )
		{
			writeColor( "mirror_color", appearance.getReflectionColor() );
			writeFloat( "specular_reflect", (double)( appearance.getReflectionMin() + appearance.getReflectionMax() ) / 2.0 );
		}

		writer.endTag( null, "material" );

		return name;
	}

	/**
	 * Writes a YafaRay mesh for the given object.
	 *
	 * @param   object          Object to be written.
	 * @param   objectToScene   Transforms the object into scene coordinates.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeMesh( final Object3D object, final Matrix3D objectToScene )
	throws XMLException
	{
		int vertexCount = 0;
		int triangleCount = 0;

		final List<FaceGroup> faceGroups = object.getFaceGroups();
		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				vertexCount += face.getVertexCount();
				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					triangleCount += primitive.getTriangles().length / 3;
				}
			}
		}

		final XMLWriter writer = _writer;
		writer.startTag( null, "mesh" );
		writer.attribute( null, "vertices", String.valueOf( vertexCount ) );
		writer.attribute( null, "faces", String.valueOf( triangleCount ) );
		writer.attribute( null, "has_orco", String.valueOf( false ) );
		writer.attribute( null, "has_uv", String.valueOf( true ) );
		writer.attribute( null, "type", String.valueOf( 0 ) );

		int vertexIndex = 0;
		for ( final FaceGroup faceGroup : faceGroups )
		{
			final String materialName = _appearanceMap.get( faceGroup.getAppearance() );
			writeValue( "set_material", materialName );

			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<Vertex3D> vertices = face.getVertices();
				for ( final Vertex3D vertex : vertices )
				{
					writePoint( "p", objectToScene.transform( vertex.point ) );

					writer.emptyTag( null, "uv" );
					writer.attribute( null, "u", String.valueOf( Float.isNaN( vertex.colorMapU ) ? 0.0f : vertex.colorMapU ) );
					writer.attribute( null, "v", String.valueOf( Float.isNaN( vertex.colorMapV ) ? 0.0f : vertex.colorMapV ) );
					writer.endTag( null, "uv" );
				}

				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final int[] triangles = primitive.getTriangles();
					for ( int i = 0; i < triangles.length; i += 3 )
					{
						final int a = triangles[ i ];
						final int b = triangles[ i + 1 ];
						final int c = triangles[ i + 2 ];

						writer.emptyTag( null, "f" );
						writer.attribute( null, "a", String.valueOf( vertexIndex + a ) );
						writer.attribute( null, "b", String.valueOf( vertexIndex + b ) );
						writer.attribute( null, "c", String.valueOf( vertexIndex + c ) );
						writer.attribute( null, "uv_a", String.valueOf( vertexIndex + a ) );
						writer.attribute( null, "uv_b", String.valueOf( vertexIndex + b ) );
						writer.attribute( null, "uv_c", String.valueOf( vertexIndex + c ) );
						writer.endTag( null, "f" );
					}
				}

				vertexIndex += vertices.size();
			}
		}

		writer.endTag( null, "mesh" );
	}

	/**
	 * Writes a parameter with an integer value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeInteger( final String name, final int value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "ival", String.valueOf( value ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a boolean value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeBoolean( final String name, final boolean value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "bval", String.valueOf( value ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a string value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeValue( final String name, final String value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "sval", value );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with an float value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeFloat( final String name, final double value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "fval", String.valueOf( value ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a point value. The value typically represents a
	 * point in space, and may be transformed to account for scene scale.
	 *
	 * @param   name    Name of the parameter.
	 * @param   x       X-coordinate of the point.
	 * @param   y       Y-coordinate of the point.
	 * @param   z       Z-coordinate of the point.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writePoint( final String name, final double x, final double y, final double z )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "x", String.valueOf( 0.001 * x ) );
		writer.attribute( null, "y", String.valueOf( 0.001 * y ) );
		writer.attribute( null, "z", String.valueOf( 0.001 * z ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a point value. The value typically represents a
	 * point in space, and may be transformed to account for scene scale.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writePoint( final String name, final Vector3D value )
	throws XMLException
	{
		writePoint( name, value.x, value.y, value.z );
	}

	/**
	 * Writes a parameter with a vector value. This is typically a unit vector,
	 * and as such no transformations are applied to account for scene scale.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeVector( final String name, final Vector3D value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "x", String.valueOf( value.x ) );
		writer.attribute( null, "y", String.valueOf( value.y ) );
		writer.attribute( null, "z", String.valueOf( value.z ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a color value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   r       Red-component of the color.
	 * @param   g       Green-component of the color.
	 * @param   b       Blue-component of the color.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeColor( final String name, final double r, final double g, final double b )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "r", String.valueOf( r ) );
		writer.attribute( null, "g", String.valueOf( g ) );
		writer.attribute( null, "b", String.valueOf( b ) );
		writer.endTag( null, name );
	}

	/**
	 * Writes a parameter with a color value.
	 *
	 * @param   name    Name of the parameter.
	 * @param   value   Value of the parameter.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	private void writeColor( final String name, final Color4 value )
	throws XMLException
	{
		final XMLWriter writer = _writer;
		writer.emptyTag( null, name );
		writer.attribute( null, "r", String.valueOf( value.getRedFloat() ) );
		writer.attribute( null, "g", String.valueOf( value.getGreenFloat() ) );
		writer.attribute( null, "b", String.valueOf( value.getBlueFloat() ) );
		writer.endTag( null, name );
	}
}
