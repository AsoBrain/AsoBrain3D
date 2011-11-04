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
import java.text.*;
import java.util.*;
import java.util.zip.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Writes a {@link Node3D} in OBJ format.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class ObjWriter
{
	/**
	 * Number format with up to 6 decimals.
	 */
	private static final NumberFormat DECIMAL_FORMAT;

	static
	{
		final NumberFormat df = NumberFormat.getNumberInstance( Locale.US );
		df.setGroupingUsed( false );
		df.setMinimumFractionDigits( 1 );
		df.setMaximumFractionDigits( 6 );
		DECIMAL_FORMAT = df;
	}

	/**
	 * Texture map URI to resolve texture map names against.
	 */
	private URI _textureMapURI = null;

	/**
	 * Suffix added to material codes to create texture map names.
	 */
	private String _textureMapSuffix = ".jpg";

	/**
	 * Returns the URI used to resolve the location of texture maps.
	 *
	 * @return  Texture map URI; <code>null</code> if not used.
	 */
	@Nullable
	public URI getTextureMapURI()
	{
		return _textureMapURI;
	}

	/**
	 * Sets the URI used to resolve the location of texture maps.
	 *
	 * @param   textureMapURI   Texture map URI; <code>null</code> if not used.
	 */
	public void setTextureMapURI( @Nullable final URI textureMapURI )
	{
		_textureMapURI = textureMapURI;
	}

	/**
	 * Returns the suffix that is appended to texture map names. This may be
	 * used to specify a file extension, which is usually not
	 * included in {@link Material#colorMap} and so on.
	 *
	 * @return  Texture map suffix.
	 */
	@Nullable
	public String getTextureMapSuffix()
	{
		return _textureMapSuffix;
	}

	/**
	 * Sets the suffix that is appended to texture map names.
	 *
	 * @param   textureMapSuffix    Texture map suffix.
	 */
	public void setTextureMapSuffix( @Nullable final String textureMapSuffix )
	{
		_textureMapSuffix = textureMapSuffix;
	}

	/**
	 * Writes an OBJ file for the given node.
	 *
	 * @param   out                 Stream to write to.
	 * @param   node                Node to be written.
	 * @param   materialLibraries   Names of material libraries.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void write( final OutputStream out, final Node3D node, final String... materialLibraries )
		throws IOException
	{
		final BufferedWriter objWriter = new BufferedWriter( new OutputStreamWriter( out, "US-ASCII" ) );
		for ( final String materialLibrary : materialLibraries )
		{
			objWriter.write( "mtllib " );
			objWriter.write( materialLibrary );
			objWriter.write( "\n" );
		}
		final ObjGenerator objGenerator = new ObjGenerator( objWriter );
		Node3DTreeWalker.walk( objGenerator, node );

		objWriter.flush();
	}

	/**
	 * Writes an MTL file containing the given appearances.
	 *
	 * @param   out             Stream to write to.
	 * @param   appearances     Appearances to be written.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void writeMTL( final OutputStream out, final Collection<? extends Appearance> appearances )
		throws IOException
	{
		final BufferedWriter mtlWriter = new BufferedWriter( new OutputStreamWriter( out, "US-ASCII" ) );
		final MtlGenerator mtlGenerator = new MtlGenerator( mtlWriter );

		for ( final Appearance appearance : appearances )
		{
			mtlGenerator.writeAppearance( appearance );
		}

		mtlWriter.flush();
	}

	/**
	 * Writes a ZIP file with an OBJ and MTL file for the given node.
	 *
	 * @param   out     Stream to write to.
	 * @param   node    Node to be written.
	 * @param   name    Name for the OBJ/MTL files (without extension).
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void writeZIP( final OutputStream out, final Node3D node, final String name )
		throws IOException
	{
		final ZipOutputStream zipOut = new ZipOutputStream( new BufferedOutputStream( out ) );

		zipOut.putNextEntry( new ZipEntry( name + ".mtl" ) );

		final BufferedWriter mtlWriter = new BufferedWriter( new OutputStreamWriter( zipOut, "US-ASCII" ) );
		final MtlGenerator mtlGenerator = new MtlGenerator( mtlWriter );
		Node3DTreeWalker.walk( mtlGenerator, node );

		mtlWriter.flush();
		zipOut.closeEntry();

		zipOut.putNextEntry( new ZipEntry( name + ".obj" ) );

		final BufferedWriter objWriter = new BufferedWriter( new OutputStreamWriter( zipOut, "US-ASCII" ) );
		objWriter.write( "mtllib " + name + ".mtl\n" );
		final ObjGenerator objGenerator = new ObjGenerator( objWriter );
		Node3DTreeWalker.walk( objGenerator, node );

		objWriter.flush();
		zipOut.closeEntry();

		zipOut.finish();
		zipOut.flush();
	}

	/**
	 * Node visitor that generates an MTL file containing definitions of the
	 * materials of all visited {@link Object3D}s.
	 */
	private class MtlGenerator
		implements Node3DVisitor
	{
		/**
		 * Stream to write the MTL file to.
		 */
		private final Writer _out;

		/**
		 * Previously written appearances, by material name used in the MTL.
		 */
		private final Map<Appearance, String> _appearanceToMaterial = new HashMap<Appearance, String>();

		/**
		 * Set of names currently in use.
		 */
		private final Set<String> _uniqueNames = new HashSet<String>();

		/**
		 * Constructs a new instance.
		 *
		 * @param   out     Stream to write to.
		 */
		MtlGenerator( final Writer out )
		{
			_out = out;
		}

		public boolean visitNode( @NotNull final Node3DPath path )
		{
			try
			{
				final Node3D node = path.getNode();
				if ( node instanceof Object3D )
				{
					final Object3D object = (Object3D)node;
					for ( final FaceGroup faceGroup : object.getFaceGroups() )
					{
						final Appearance appearance = faceGroup.getAppearance();
						if ( ( appearance != null ) && !_appearanceToMaterial.containsKey( appearance ) )
						{
							writeAppearance( appearance );
						}
					}
				}
			}
			catch ( IOException e )
			{
				throw new RuntimeException( e );
			}

			return true;
		}

		/**
		 * Writes the given appearance.
		 *
		 * @param   appearance  Appearance to be written.
		 *
		 * @throws  IOException if an I/O error occurs.
		 */
		public void writeAppearance( final Appearance appearance )
			throws IOException
		{
			final String materialName = generateMaterialName( appearance );
			writeMaterial( materialName, appearance );
			_appearanceToMaterial.put( appearance, materialName );
		}

		/**
		 * Writes a definition of the given appearance.
		 *
		 * @param   materialName    Material name used in the MTL.
		 * @param   appearance      Appearance to be written.
		 *
		 * @throws  IOException if an I/O error occurs.
		 */
		private void writeMaterial( final String materialName, final Appearance appearance )
			throws IOException
		{
			final Writer out = _out;
			out.write( "newmtl " );
			out.write( materialName );
			out.write( '\n' );

			final Color4 ambientColor = appearance.getAmbientColor();
			out.write( "Ka " );
			out.write( DECIMAL_FORMAT.format( ambientColor.getRedFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( ambientColor.getGreenFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( ambientColor.getBlueFloat() ) );
			out.write( '\n' );

			final Color4 diffuseColor = appearance.getDiffuseColor();
			out.write( "Kd " );
			out.write( DECIMAL_FORMAT.format( diffuseColor.getRedFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( diffuseColor.getGreenFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( diffuseColor.getBlueFloat() ) );
			out.write( '\n' );

			final Color4 specularColor = appearance.getSpecularColor();
			out.write( "Ks " );
			out.write( DECIMAL_FORMAT.format( specularColor.getRedFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( specularColor.getGreenFloat() ) );
			out.write( ' ' );
			out.write( DECIMAL_FORMAT.format( specularColor.getBlueFloat() ) );
			out.write( '\n' );

			out.write( "illum 2\n" );

			out.write( "Ns " );
			final int shininess = appearance.getShininess();
			// NOTE: This is simply the inverse of what 'ObjLoader' does.
			out.write( String.valueOf( Math.min( 128, shininess * 1000 / 128 ) ) );
			out.write( '\n' );

			out.write( "d " );
			out.write( DECIMAL_FORMAT.format( diffuseColor.getAlphaFloat() ) );
			out.write( '\n' );

			if ( appearance instanceof Material )
			{
				final Material material = (Material)appearance;

				final TextureMap colorMap = appearance.getColorMap();
				if ( colorMap != null )
				{
					out.write( "map_Kd " );
					out.write( getTextureMapLocation( material.colorMap ) );
					out.write( '\n' );
				}

				final TextureMap bumpMap = appearance.getBumpMap();
				if ( bumpMap != null )
				{
					out.write( "bump " );
					out.write( getTextureMapLocation( material.bumpMap ) );
					out.write( '\n' );
				}
			}
		}

		/**
		 * Returns the file name or URI of the texture map with the given name.
		 *
		 * @param   name    Texture map name.
		 *
		 * @return  File name or URI for the texture map.
		 */
		private String getTextureMapLocation( final String name )
		{
			String result = name;
			if ( _textureMapSuffix != null )
			{
				result += _textureMapSuffix;
			}

			if ( _textureMapURI != null )
			{
				final URI resolved = _textureMapURI.resolve( name );
				result = resolved.toString();
			}
			return result;
		}

		/**
		 * Generates a name for the given appearance. The generated name is
		 * unique within the MTL file.
		 *
		 * @param   appearance  Appearance to generate a name for.
		 *
		 * @return  Name for the given appearance.
		 */
		private String generateMaterialName( final Appearance appearance )
		{
			return generateUniqueName( "material" );
		}

		/**
		 * Generates a unique name from the given name. If the name is already
		 * unique, it's returned as-is.
		 *
		 * @param   name    Name to make unique.
		 *
		 * @return  Unique name.
		 */
		private String generateUniqueName( final String name )
		{
			String result = name;
			int uniqueSuffix = 1;
			while ( _uniqueNames.contains( result ) )
			{
				result = name + "_" + uniqueSuffix++;
			}
			_uniqueNames.add( result );
			return result;
		}

		/**
		 * Returns the material name used in the MTL for the given appearance.
		 *
		 * @param   appearance  Appearance to get the name of.
		 *
		 * @return  Material name.
		 */
		public String getMaterialName( final Appearance appearance )
		{
			return _appearanceToMaterial.get( appearance );
		}
	}

	/**
	 * Node visitor that generates an OBJ file containing geometry for the
	 * visited {@link Object3D}s.
	 */
	private static class ObjGenerator
		implements Node3DVisitor
	{
		/**
		 * Stream to write the OBJ file to.
		 */
		private Writer _out;

		/**
		 * Current vertex index.
		 */
		private int _vertexIndex = 1;

		/**
		 * Current appearance.
		 */
		@Nullable
		private Appearance _currentAppearance = null;

		/**
		 * Provides material names used in the MTL file.
		 */
		@Nullable
		private MtlGenerator _materials;

		/**
		 * Constructs a new instance.
		 *
		 * @param   out     Stream to write to.
		 */
		private ObjGenerator( @NotNull final Writer out )
		{
			this( out, null );
		}

		/**
		 * Constructs a new instance.
		 *
		 * @param   out         Stream to write to.
		 * @param   materials   Provides material names used in the MTL file;
		 *                      <code>null</code> if not available.
		 */
		private ObjGenerator( @NotNull final Writer out, @Nullable final MtlGenerator materials )
		{
			_out = out;
			_materials = materials;
		}

		public boolean visitNode( @NotNull final Node3DPath path )
		{
			final Writer out = _out;

			int vertexIndex = _vertexIndex;
			Appearance currentAppearance = _currentAppearance;

			try
			{
				final Node3D node = path.getNode();
				if ( node instanceof Object3D )
				{
					final Object3D object = (Object3D)node;
					out.write( "o " );
					out.write( getObjectName( object ) );
					out.write( '\n' );

					final Matrix3D transform = path.getTransform();

					for ( final FaceGroup faceGroup : object.getFaceGroups() )
					{
						final Appearance appearance = faceGroup.getAppearance();
						if ( currentAppearance != appearance )
						{
							currentAppearance = appearance;
							final String materialName = getMaterialName( appearance );
							out.write( "usemtl " );
							out.write( materialName );
							out.write( '\n' );
						}

						for ( final Face3D face : faceGroup.getFaces() )
						{
							final int vertexCount = face.getVertexCount();

							for ( int i = 0; i < vertexCount; i++ )
							{
								final Face3D.Vertex vertex = face.getVertex( i );

								out.write( "v " );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.transformX( vertex.point ) ) );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.transformY( vertex.point ) ) );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.transformZ( vertex.point ) ) );
								out.write( '\n' );

								out.write( "vt " );
								out.write( DECIMAL_FORMAT.format( Float.isNaN( vertex.colorMapU ) ? 0.0f : vertex.colorMapU ) );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( Float.isNaN( vertex.colorMapV ) ? 0.0f : vertex.colorMapV ) );
								out.write( '\n' );

								final Vector3D normal = face.getVertexNormal( i );
								out.write( "vn " );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.rotateX( normal ) ) );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.rotateY( normal ) ) );
								out.write( ' ' );
								out.write( DECIMAL_FORMAT.format( transform.rotateZ( normal ) ) );
								out.write( '\n' );
							}

							final Tessellation tessellation = face.getTessellation();
							for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
							{
								final int[] triangles = primitive.getTriangles();
								for ( int i = 0 ; i < triangles.length ; i += 3 )
								{
									out.write( 'f' );
									for ( int j = 0; j < 3; j++ )
									{
										out.write( ' ' );
										out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
										out.write( '/' );
										out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
										out.write( '/' );
										out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
									}
									out.write( '\n' );

									if ( faceGroup.isTwoSided() )
									{
										out.write( 'f' );
										for ( int j = 2; j >= 0; j-- )
										{
											out.write( ' ' );
											out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
											out.write( '/' );
											out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
											out.write( '/' );
											out.write( String.valueOf( vertexIndex + triangles[ i + j ] ) );
										}
										out.write( '\n' );
									}
								}
							}

							vertexIndex += vertexCount;
						}
					}
				}
			}
			catch ( IOException e )
			{
				throw new RuntimeException( e );
			}

			_vertexIndex = vertexIndex;
			_currentAppearance = currentAppearance;

			return true;
		}

		/**
		 * Returns a name for the given object.
		 *
		 * @param   object  Object to be named.
		 *
		 * @return  Name for the object.
		 */
		@NotNull
		private String getObjectName( @NotNull final Object3D object )
		{
			return String.valueOf( object.getTag() );
		}

		/**
		 * Returns the material name for the given appearance.
		 *
		 * @param   appearance  Appearance to get the name of.
		 *
		 * @return  Material name.
		 */
		@NotNull
		private String getMaterialName( @Nullable final Appearance appearance )
		{
			final String result;

			if ( _materials != null )
			{
				result = _materials.getMaterialName( appearance );
			}
			else if ( appearance instanceof Material )
			{
				result = ( (Material)appearance ).code;
			}
			else
			{
				result = "default";
			}

			return result;
		}
	}
}
