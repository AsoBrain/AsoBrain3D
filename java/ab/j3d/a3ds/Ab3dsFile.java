/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
package ab.j3d.a3ds;

import java.io.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.loader.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * This is a representation of a .3ds file it contains the main entrance to all
 * chunks inside (the main3DS chunk).
 *
 * @author Sjoerd Bouwman
 */
public class Ab3dsFile
{
	/**
	 * Tessellation for a face with a single triangle whose vertices are defined in
	 * counter-clockwise order.
	 */
	private static final Tessellation CCW_TRIANGLE_TESSELLATION = new Tessellation( Collections.singletonList( new int[] { 0, 1, 2, 0 } ), Collections.<TessellationPrimitive>singletonList( new TriangleList( new int[] { 0, 1, 2 } ) ) );

	/**
	 * Set to true to receive debugging to console.
	 */
	public static boolean DEBUG = false;

	/**
	 * The main entrance point of the model.
	 */
	private HierarchyChunk _main;

	/**
	 * Maps material names of the last loaded model to appearances.
	 *
	 * This is only valid for the last time {@link #createModel} was called.
	 */
	private Map<String, Appearance> _appearancesByName = Collections.emptyMap();

	/**
	 * Maps material names of the last loaded model to 3DS material chunks.
	 *
	 * This is only valid for the last time {@link #createModel} was called.
	 */
	private Map<String, Ab3dsMaterial> _materialsByName = Collections.emptyMap();

	/**
	 * Creates a new 3DS file.
	 */
	public Ab3dsFile()
	{
		_main = new HierarchyChunk( Chunk.MAIN3DS );
		_main.add( new HierarchyChunk( Chunk.EDIT3DS ) );
	}

	/**
	 * Gets the edit chunk from the model.
	 *
	 * @return Edit chunk.
	 */
	@Nullable
	public HierarchyChunk getEditChunk()
	{
		final HierarchyChunk mainChunk = getMainChunk();
		return ( mainChunk == null ) ? null : (HierarchyChunk)mainChunk.getFirstChunkByID( Chunk.EDIT3DS );
	}

	/**
	 * Gets the main3DS chunk from the model.
	 *
	 * @return the main chunk.
	 */
	@Nullable
	public HierarchyChunk getMainChunk()
	{
		return ( _main.getID() != Chunk.MAIN3DS ) ? null : _main;
	}

	/**
	 * Loads a 3DS model from a file.
	 *
	 * @param file File to be loaded.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void load( @NotNull final File file )
	throws IOException
	{
		final FileInputStream fis = new FileInputStream( file );
		try
		{
			load( fis );
		}
		finally
		{
			fis.close();
		}
	}

	/**
	 * Loads a 3DS model from the given stream.
	 *
	 * @param in Stream to load from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void load( @NotNull final InputStream in )
	throws IOException
	{
		final Ab3dsInputStream is = new Ab3dsInputStream( in );

		if ( DEBUG )
		{
			System.out.println( "Reading 3DS file." );
		}

		final HierarchyChunk main = new HierarchyChunk( is.readInt() );
		_main = main;

		if ( main.getID() != Chunk.MAIN3DS )
		{
			throw new RuntimeException( "File is not a valid .3DS file! (does not start with 0x4D4D)" );
		}

		main.read( is );

		if ( DEBUG )
		{
			System.out.println( "Finished 3DS file." );
		}
	}

	/**
	 * Saves the current hierarchy as a 3DS file.
	 *
	 * @param file File to save to.
	 */
	public void save( final File file )
	{
		try
		{
			final FileOutputStream fos = new FileOutputStream( file );
			try
			{
				save( fos );
			}
			finally
			{
				fos.close();
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves the current 3DS hierarchy to the given stream.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void save( @NotNull final OutputStream out )
	throws IOException
	{
		final Ab3dsOutputStream os = new Ab3dsOutputStream( out );

		if ( DEBUG )
		{
			System.out.println( "Writing 3DS file." );
		}

		_main.write( os );

		if ( DEBUG )
		{
			System.out.println( "Writing 3DS file." );
		}
	}

	/**
	 * Creates a 3D model from the loaded 3DS file. Since no context information is
	 * provided, textures are not included in the created model.
	 *
	 * @return Root node of 3D model.
	 *
	 * @throws IllegalStateException if no model is loaded.
	 */
	@NotNull
	public Node3D createModel()
	{
		final HierarchyChunk editChunk = getEditChunk();
		if ( editChunk == null )
		{
			throw new IllegalStateException( "No model loaded. (Missing edit chunk.)" );
		}

		/*
		 * Create materials and keep track of additional material
		 * specifications, e.g. two-sidedness.
		 */
		final Map<String, Appearance> materials = new HashMap<String, Appearance>();
		final Map<String, Ab3dsMaterial> materialChunks = new HashMap<String, Ab3dsMaterial>();

		for ( final Chunk chunk : editChunk.getChunksByID( Chunk.EDIT_MATERIAL ) )
		{
			final Ab3dsMaterial materialChunk = (Ab3dsMaterial)chunk;
			final String name = materialChunk.getName();
			materialChunks.put( name, materialChunk );
			materials.put( name, createAppearance( materialChunk ) );
		}

		return createModel( materials, materialChunks );
	}

	/**
	 * Creates a 3D model from the loaded 3DS file.
	 *
	 * @param context Directory used to resolve relative paths.
	 *
	 * @return Root node of 3D model.
	 *
	 * @throws IllegalStateException if no model is loaded.
	 */
	@NotNull
	public Node3D createModel( @NotNull final File context )
	{
		final HierarchyChunk editChunk = getEditChunk();
		if ( editChunk == null )
		{
			throw new IllegalStateException( "No model loaded. (Missing edit chunk.)" );
		}

		/*
		 * Create materials and keep track of additional material
		 * specifications, e.g. two-sidedness.
		 */
		final Map<String, Appearance> materials = new HashMap<String, Appearance>();
		final Map<String, Ab3dsMaterial> materialChunks = new HashMap<String, Ab3dsMaterial>();

		for ( final Chunk chunk : editChunk.getChunksByID( Chunk.EDIT_MATERIAL ) )
		{
			final Ab3dsMaterial materialChunk = (Ab3dsMaterial)chunk;
			final String name = materialChunk.getName();
			materialChunks.put( name, materialChunk );

			final BasicAppearance appearance = createAppearance( materialChunk );

			final TextureMap textureMap = materialChunk.getTexture1Map();
			if ( textureMap != null )
			{
				appearance.setColorMap( new BasicTextureMap( textureMap.getPath() ) );
			}

			materials.put( name, appearance );
		}

		return createModel( materials, materialChunks );
	}

	/**
	 * Creates a 3D model from the loaded 3DS file.
	 *
	 * @param resourceLoader Resource loader to be used.
	 *
	 * @return Root node of 3D model.
	 *
	 * @throws IllegalStateException if no model is loaded.
	 */
	@NotNull
	public Node3D createModel( @NotNull final ResourceLoader resourceLoader )
	{
		final HierarchyChunk editChunk = getEditChunk();
		if ( editChunk == null )
		{
			throw new IllegalStateException( "No model loaded. (Missing edit chunk.)" );
		}

		/*
		 * Create materials and keep track of additional material
		 * specifications, e.g. two-sidedness.
		 */
		final Map<String, Appearance> materials = new HashMap<String, Appearance>();
		final Map<String, Ab3dsMaterial> materialChunks = new HashMap<String, Ab3dsMaterial>();

		for ( final Chunk chunk : editChunk.getChunksByID( Chunk.EDIT_MATERIAL ) )
		{
			final Ab3dsMaterial materialChunk = (Ab3dsMaterial)chunk;
			final String name = materialChunk.getName();
			materialChunks.put( name, materialChunk );

			final BasicAppearance appearance = createAppearance( materialChunk );

			final TextureMap textureMap = materialChunk.getTexture1Map();
			if ( textureMap != null )
			{
				appearance.setColorMap( new BasicTextureMap( textureMap.getPath() ) );
			}

			materials.put( name, appearance );
		}

		return createModel( materials, materialChunks );
	}


	/**
	 * Get main chunk of 3DS file.
	 *
	 * @return Main chunk of 3DS file.
	 */
	public HierarchyChunk getMain()
	{
		return _main;
	}

	/**
	 * Get map from material names in the last loaded model to appearances.
	 *
	 * This is only valid for the last time {@link #createModel} was called.
	 *
	 * @return Map from material names in the last loaded model to appearances.
	 */
	public Map<String, Appearance> getAppearancesByName()
	{
		return Collections.unmodifiableMap( _appearancesByName );
	}

	/**
	 * Get map from material names in the last loaded model to 3DS material
	 * chunks.
	 *
	 * This is only valid for the last time {@link #createModel} was called.
	 *
	 * @return Map from material names in the last loaded model to 3DS material
	 *         chunks.
	 */
	public Map<String, Ab3dsMaterial> getMaterialsByName()
	{
		return Collections.unmodifiableMap( _materialsByName );
	}

	/**
	 * Creates a basic appearance from the given material chunk, excluding any
	 * texture maps.
	 *
	 * @param materialChunk Material chunk.
	 *
	 * @return Appearance.
	 */
	private static BasicAppearance createAppearance( @NotNull final Ab3dsMaterial materialChunk )
	{
		final BasicAppearance appearance = new BasicAppearance();

		final Ab3dsRGB ambient = materialChunk.getAmbient();
		appearance.setAmbientColor( new Color4f( ambient.getRed(), ambient.getGreen(), ambient.getBlue() ) );

		final Ab3dsRGB diffuse = materialChunk.getDiffuse();
		appearance.setDiffuseColor( new Color4f( diffuse.getRed(), diffuse.getGreen(), diffuse.getBlue(), 1.0f - materialChunk.getTransparency() ) );

		final Ab3dsRGB specular = materialChunk.getSpecular();
		appearance.setSpecularColor( new Color4f( specular.getRed(), specular.getGreen(), specular.getBlue() ) );
		appearance.setShininess( materialChunk.getShininess() );

		return appearance;
	}

	/**
	 * Creates a 3D model from the 3DS file, using the given material information.
	 *
	 * @param materials      Materials by material name.
	 * @param materialChunks Material chunks by material name.
	 *
	 * @return Root node of 3D model.
	 *
	 * @throws IllegalStateException if no model is loaded.
	 */
	@NotNull
	private Node3D createModel( @NotNull final Map<String, Appearance> materials, @NotNull final Map<String, Ab3dsMaterial> materialChunks )
	{
		_appearancesByName = materials;
		_materialsByName = materialChunks;

		final Node3D result = new Node3D();

		final HierarchyChunk editChunk = getEditChunk();
		if ( editChunk == null )
		{
			throw new IllegalStateException( "No model loaded. (Missing edit chunk.)" );
		}

		for ( final Chunk chunk : editChunk.getChunksByID( Chunk.EDIT_OBJECT ) )
		{
			final ObjectChunk objectChunk = (ObjectChunk)chunk;
			for ( final Chunk meshChunk : objectChunk.getChunksByID( Chunk.OBJ_TRIMESH ) )
			{
				final HierarchyChunk hierarchyChunk = (HierarchyChunk)meshChunk;

				final FaceList faceList = (FaceList)hierarchyChunk.getFirstChunkByID( Chunk.TRI_FACEL1 );
				final VertexList vertexList = (VertexList)hierarchyChunk.getFirstChunkByID( Chunk.TRI_VERTEXLIST );

				if ( ( faceList != null ) && ( vertexList != null ) )
				{
					/*
					 * Map materials by face.
					 */
					final List<Chunk> faceMaterialChunks = faceList.getChunksByID( Chunk.TRI_MATERIAL );
					final List<Appearance> appearanceByFace = new ArrayList<Appearance>( Collections.nCopies( faceList.getFaceCount(), (Appearance)null ) );
					final List<Ab3dsMaterial> materialChunkByFace = new ArrayList<Ab3dsMaterial>( Collections.nCopies( faceList.getFaceCount(), (Ab3dsMaterial)null ) );
					for ( final Chunk faceMaterialChunk : faceMaterialChunks )
					{
						final FaceList.FaceMaterial faceMaterial = (FaceList.FaceMaterial)faceMaterialChunk;
						final Appearance appearance = materials.get( faceMaterial.name );
						final Ab3dsMaterial materialChunk = materialChunks.get( faceMaterial.name );
						for ( final int face : faceMaterial.faces )
						{
							appearanceByFace.set( face, appearance );
							materialChunkByFace.set( face, materialChunk );
						}
					}

					/*
					 * Prepare information needed to calculate vertex normals
					 * based on smoothing groups.
					 */
					final List<List<Integer>> facesByVertex = new ArrayList<List<Integer>>( Collections.nCopies( vertexList.getVertexCount(), (List<Integer>)null ) );
					final List<Vector3D> faceNormals = new ArrayList<Vector3D>( faceList.getFaceCount() );
					for ( int faceIndex = 0; faceIndex < faceList.getFaceCount(); faceIndex++ )
					{
						final FaceList.Triangle face = faceList.getFace( faceIndex );
						for ( int faceVertexIndex = 1; faceVertexIndex <= 3; faceVertexIndex++ )
						{
							final int objectVertexIndex = face.getVertex( faceVertexIndex );
							List<Integer> faces = facesByVertex.get( objectVertexIndex );
							if ( faces == null )
							{
								faces = new ArrayList<Integer>();
								facesByVertex.set( objectVertexIndex, faces );
							}
							faces.add( Integer.valueOf( faceIndex ) );
						}

						final Vector3D v1 = vertexList.getVertex( face.getVertex( 1 ) );
						final Vector3D v2 = vertexList.getVertex( face.getVertex( 2 ) );
						final Vector3D v3 = vertexList.getVertex( face.getVertex( 3 ) );

						final Vector3D normal = Vector3D.cross( v2.minus( v1 ), v3.minus( v1 ) );
						faceNormals.add( normal.normalize() );
					}

					/*
					 * Build geometry.
					 */
					final Object3DBuilder builder = new Object3DBuilder();

					final SmoothingGroups smoothingGroups = (SmoothingGroups)faceList.getFirstChunkByID( Chunk.TRI_SMOOTH );
					final MappingCoordinates mappingCoordinates = (MappingCoordinates)hierarchyChunk.getFirstChunkByID( Chunk.TRI_MAP_COORDS );
					// TODO: Support standard mappings. (e.g. spherical)
//					final StandardMapping standardMapping = (StandardMapping)hierarchyChunk.getFirstChunkByID( Chunk.TRI_MAP_STAND );

					for ( int faceIndex = 0; faceIndex < faceList.getFaceCount(); faceIndex++ )
					{
						final FaceList.Triangle face = faceList.getFace( faceIndex );

						final List<Vertex3D> vertices = new ArrayList<Vertex3D>( 3 );
						for ( int faceVertexIndex = 1; faceVertexIndex <= 3; faceVertexIndex++ )
						{
							final int objectVertexIndex = face.getVertex( faceVertexIndex );

							/*
							 * Calculate vertex normal based on smoothing groups.
							 *
							 * Official documentation on the subject, via the
							 * Internet Archive:
							 * http://replay.web.archive.org/20070401142147/http://sparks.discreet.com/knowledgebase/webhelp/html/idx_AT_computing_face_and_vertex_normals.htm
							 */
							Vector3D vertexNormal = faceNormals.get( faceIndex );
							if ( smoothingGroups != null )
							{
								int smoothingGroup = smoothingGroups.getSmoothingGroup( faceIndex );
								if ( smoothingGroup != 0 )
								{
									double normalX = vertexNormal.x;
									double normalY = vertexNormal.y;
									double normalZ = vertexNormal.z;

									/*
									 * Merge all overlapping smoothing groups.
									 */
									int smoothingGroupBefore;
									do
									{
										smoothingGroupBefore = smoothingGroup;
										for ( final int otherFaceIndex : facesByVertex.get( objectVertexIndex ) )
										{
											final int otherGroup = smoothingGroups.getSmoothingGroup( otherFaceIndex );
											if ( ( smoothingGroup & otherGroup ) != 0 )
											{
												smoothingGroup |= otherGroup;
											}
										}
									}
									while ( smoothingGroupBefore != smoothingGroup );

									/*
									 * Calculate normal based on smoothing groups.
									 */
									for ( final int otherFaceIndex : facesByVertex.get( objectVertexIndex ) )
									{
										if ( otherFaceIndex != faceIndex )
										{
											final int otherGroup = smoothingGroups.getSmoothingGroup( otherFaceIndex );
											if ( ( smoothingGroup & otherGroup ) != 0 )
											{
												final Vector3D otherNormal = faceNormals.get( otherFaceIndex );
												normalX += otherNormal.x;
												normalY += otherNormal.y;
												normalZ += otherNormal.z;
											}
										}
									}

									if ( !vertexNormal.equals( normalX, normalY, normalZ ) )
									{
										vertexNormal = Vector3D.normalize( normalX, normalY, normalZ );
									}
								}
							}

							final Vector3D vertexCoordinate = vertexList.getVertex( objectVertexIndex );
							final float colorMapU = ( mappingCoordinates == null ) ? Float.NaN : mappingCoordinates.getMapU( objectVertexIndex );
							final float colorMapV = ( mappingCoordinates == null ) ? Float.NaN : mappingCoordinates.getMapV( objectVertexIndex );

							final Vertex3D vertex = new Vertex3D( vertexCoordinate, objectVertexIndex, colorMapU, colorMapV );
							vertex.setNormal( vertexNormal );
							vertices.add( vertex );
						}

						final Ab3dsMaterial materialChunk = materialChunkByFace.get( faceIndex );

						final Appearance appearance = appearanceByFace.get( faceIndex );
						final boolean twoSided = ( materialChunk != null ) && materialChunk.isTwoSided();

						builder.addFace( vertices, CCW_TRIANGLE_TESSELLATION, appearance, false, twoSided );
					}

					final Collection<Vector3D> vertexCoordinates = new ArrayList<Vector3D>( vertexList.getVertexCount() );
					for ( int i = 0; i < vertexList.getVertexCount(); i++ )
					{
						vertexCoordinates.add( vertexList.getVertex( i ) );
					}
					builder.setVertexCoordinates( vertexCoordinates );

					final Object3D object = builder.getObject3D();
					object.smooth( 0.0, 1.0, false );
					result.addChild( object );
				}
			}
		}

		return result;
	}

	/**
	 * Loads a 3DS model from the given stream.
	 *
	 * @param resourceLoader Resource loader to be used.
	 * @param in             Stream to read from.
	 *
	 * @return Root node of 3D model.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static Node3D load( @NotNull final ResourceLoader resourceLoader, @NotNull final InputStream in )
	throws IOException
	{
		final Ab3dsFile file = new Ab3dsFile();
		file.load( in );
		return file.createModel( resourceLoader );
	}
}
