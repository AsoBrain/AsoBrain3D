/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.pov;

import java.util.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.model.Face3D.*;
import org.jetbrains.annotations.*;

/**
 * This class can be used to convert a 3D scene from <code>ab.j3d.model</code>
 * into a POV-Ray scene ({@link PovScene}).
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class AbToPovConverter
{
	/**
	 * Variable that will hold the converted scene.
	 */
	private final PovScene _scene;

	/**
	 * Directory containing the used materials.
	 */
	private final String _imageMapDirectory;

	/**
	 * Construct new converter and set the image map directory.
	 *
	 * @param   imageMapDirectory    Directory containing POV-image maps.
	 */
	public AbToPovConverter( @NotNull final String imageMapDirectory )
	{
		_imageMapDirectory = imageMapDirectory;

		_scene = new PovScene();
	}

	/**
	 * Method to check if an {@link Object3D} contains multiple materials and/or
	 * maps.
	 *
	 * @param   object  The {@link Object3D} to inspect.
	 *
	 * @return  <code>true</code> if the {@link Object3D} contains multiple
	 *          materials and/or maps;
	 *          <code>false</code> otherwise.
	 */
	private static boolean containsMultipleMaterialsOrMaps( final Object3D object )
	{
		boolean result = false;

		final int faceCount = object.getFaceCount();
		Material lastMaterial = null;

		for ( int i = 0 ; !result && ( i < faceCount ) ; i++ )
		{
			final Face3D   face     = object.getFace( i );
			final Material material = face.material;

			if ( material != null )
			{
				result = ( ( lastMaterial != null ) && !lastMaterial.equals( material ) ) || ( material.colorMap != null );
				lastMaterial = material;
			}
		}

		return result;
	}

	/**
	 * Work horse of the converter. Takes a {@link Node3DCollection} and then
	 * converts all individual nodes. Objects with texture mapping and/or
	 * multiple textures and extruded objects are converted as
	 * {@link Object3D}'s.
	 *
	 * @param   scene   Scene containing the nodes.
	 *
	 * @return  The resulting {@link PovScene} object.
	 */
	public PovScene convert( final Scene scene )
	{
		final PovScene povScene = _scene;
		povScene.setAmbientLight( new PovVector( (double)scene.getAmbientRed(), (double)scene.getAmbientGreen(), (double)scene.getAmbientBlue() ) );
		scene.walk( new ConvertingVisitor() );
		return povScene;
	}

	/**
	 * This method constructs a {@link PovBox} from a {@link Box3D} object.
	 *
	 * @param   box2wcs     Transform to apply to node.
	 * @param   box         The {@link Box3D} to be converted.
	 *
	 * @return  The resulting {@link PovGeometry} object.
	 */
	public PovGeometry convertBox3D( final Matrix3D box2wcs, final Box3D box )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( box ) )
		{
			result = convertObject3D( box2wcs, box );
		}
		else
		{
			final Face3D   anyFace      = box.getFace( 0 );

			final String     name    = ( box.getTag() != null ) ? String.valueOf( box.getTag() ) : null;
			final Vector3D   v1      = Vector3D.INIT;
			final Vector3D   v2      = v1.plus( box.getDX(), box.getDY(), box.getDZ() );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.material );

			result = new PovBox( name, v1, v2, texture );
			result.setTransform( new PovMatrix( box2wcs ) );
		}

		return result;
	}

	/**
	 * This method constructs a {@link PovCylinder} from a {@link Cone3D}
	 * object.
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   cone    The {@link Cone3D} to be converted.
	 *
	 * @return  The resulting {@link PovGeometry} object.
	 */
	public PovGeometry convertCone3D( final Matrix3D transform, final Cone3D cone )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( cone ) )
		{
			result = convertObject3D( transform, cone );
		}
		else
		{
			final String     name    = ( cone.getTag() != null ) ? String.valueOf( cone.getTag() ) : null;
			final Face3D     anyFace = cone.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.material );

			result = new PovCylinder( name, 0.0, 0.0, 0.0, cone.height, cone.radiusBottom, cone.radiusTop, texture );
			result.setTransform( new PovMatrix( transform ) );
		}

		return result;
	}

	/**
	 * This method constructs a {@link PovCylinder} from a {@link Cylinder3D}
	 * object.
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   cylinder    The {@link Cylinder3D} to be converted.
	 *
	 * @return  The resulting {@link PovGeometry} object.
	 */
	public PovGeometry convertCylinder3D( final Matrix3D transform, final Cylinder3D cylinder )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( cylinder ) )
		{
			result = convertObject3D( transform, cylinder );
		}
		else
		{
			final String     name    = ( cylinder.getTag() != null ) ? String.valueOf( cylinder.getTag() ) : null;
			final Face3D     anyFace = cylinder.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.material );

			result = new PovCylinder( name, 0.0, 0.0, 0.0, cylinder.height, cylinder.radius, cylinder.radius, texture );
			result.setTransform( new PovMatrix( transform ) );
		}

		return result;
	}

	/**
	 * This method converts a {@link Light3D} object to a {@link PovLight}
	 * object.
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   light       The {@link Light3D} object to be converted.
	 *
	 * @return  The resulting {@link PovLight} object.
	 */
	public static PovLight convertLight3D( final Matrix3D transform, final Light3D light )
	{
		final String name = ( light.getTag() != null ) ? String.valueOf( light.getTag() ) : null;

		final PovVector color = new PovVector( (double)light.getDiffuseRed(), (double)light.getDiffuseGreen(), (double)light.getDiffuseBlue() );
		final PovLight result = new PovLight( name, transform.xo, transform.yo, transform.zo, color, true );

		if ( light instanceof DirectionalLight3D )
		{
			/*
			 * NOTE: Directional lights are not attenuated.
			 */
			final DirectionalLight3D directionalLight = (DirectionalLight3D)light;
			final Vector3D pointAt = transform.transform( directionalLight.getDirection() );

			result.setPointAt( new PovVector( pointAt ) );
			result.setParallel( true );
		}
		else
		{
			if ( light instanceof SpotLight3D )
			{
				final SpotLight3D spotLight = (SpotLight3D)light;
				final Vector3D pointAt = transform.transform( spotLight.getDirection() );

				result.setSpotlight( true );
				result.setPointAt( new PovVector( pointAt ) );
				result.setFallOff( (double)spotLight.getSpreadAngle() );
				result.setRadius( (double)spotLight.getSpreadAngle() );
				result.setTightness( (double)spotLight.getConcentration() / 128.0 * 100.0 );
			}
			// else: regular point light

			if ( light.getQuadraticAttenuation() > 0.0f )
			{
				result.setFadeDistance( (double)light.getFullIntensityDistance() );
				result.setFadePower( PovLight.FADE_QUADRATIC );
			}
			else if ( light.getLinearAttenuation() > 0.0f )
			{
				result.setFadeDistance( (double)light.getFullIntensityDistance() );
				result.setFadePower( PovLight.FADE_LINEAR );
			}
			else
			{
				result.setFadePower( PovLight.FADE_NONE );
			}
		}

		return result;
	}

	/**
	 * This method converts an {@link Object3D} to a {@link PovMesh2}.
	 * All faces are converted to one or more mesh triangles and the face
	 * textures are uv-mapped to the triangles. For every triangle the first
	 * vertex is the same (a {@link Face3D} is always convex).
	 * <pre>
	 *  0  _________ 1
	 *    |\        |
	 *    | \       |
	 *    |  \      |
	 *    |   \     |
	 *    |    \    |
	 *    |     \   |
	 *    |      \  |
	 *    |       \ |
	 *  3 |________\| 2
	 *
	 * </pre>
	 * Triangle 1: ( 0, 1, 2 )<br>
	 * Triangle 2: ( 0, 2, 3 )
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   object      The {@link Object3D} to be converted.
	 *
	 * @return  The resulting {@link PovMesh2} object.
	 */
	public PovMesh2 convertObject3D( final Matrix3D transform, final Object3D object )
	{
		final PovMesh2 result = new PovMesh2( ( object.getTag() != null ) ? String.valueOf( object.getTag() ) : null );

		final int            faceCount         = object.getFaceCount();
		final List<Vector3D> vertexCoordinates = object.getVertexCoordinates();

		final List<PovVector> vertexVectors = new ArrayList<PovVector>( vertexCoordinates.size() );
		for ( final Vector3D point : vertexCoordinates )
		{
			vertexVectors.add( new PovVector( transform.transform( point ) ) );
		}

		result.setVertexVectors( vertexVectors );

		double[] vertexNormals = null;
		Material lastMaterial  = null;
		int      textureIndex  = 0;
		boolean  uvMapping     = false;

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face = object.getFace( i );
			final List<Vertex> vertices = face.vertices;
			final int vertexCount = vertices.size();

			if ( vertexCount > 2 )
			{
				final boolean  smooth = face.smooth;
				final Material material = face.material;

				/*
				 * Require vertex normals for smooth faces.
				 */
				if ( smooth && ( vertexNormals == null ) )
				{
					vertexNormals = object.getVertexNormals();

					final List<PovVector> normalVectors = new ArrayList<PovVector>( vertexCoordinates.size() );
					for ( int vertexCoordinateIndex = 0 ; vertexCoordinateIndex < vertexCoordinates.size() ; vertexCoordinateIndex++ )
					{
						final int vi3 = vertexCoordinateIndex * 3;
						final double nx = vertexNormals[ vi3 ];
						final double ny = vertexNormals[ vi3 + 1 ];
						final double nz = vertexNormals[ vi3 + 2 ];
						normalVectors.add( new PovVector( transform.rotate( nx, ny, nz ) ) );
					}

					result.setNormalVectors( normalVectors );
				}

				/*
				 * Convert material.
				 */
				if ( material != lastMaterial )
				{
					uvMapping    = ( material.colorMap != null );
					textureIndex = result.getOrAddTextureIndex( convertMaterialToPovTexture( material ) );
					lastMaterial = material;
				}

				/*
				 * For every triangle, the first vertex is the same.
				 */
				final Vertex vertex0 = vertices.get( 0 );
				final int v1  = vertex0.vertexCoordinateIndex;
				final int vn1 = smooth ? v1 : 0;
				final int uv1 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)vertex0.colorMapU, (double)vertex0.colorMapV, 0.0 ) ) : -1;

				int v2;
				int vn2;
				int uv2;

				final Vertex vertex1 = vertices.get( 1 );
				int v3  = vertex1.vertexCoordinateIndex;
				int vn3 = smooth ? v3 : 0;
				int uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)vertex1.colorMapU, (double)vertex1.colorMapV, 0.0 ) ) : -1;

				for ( int j = 2 ; j < vertexCount ; j++ )
				{
					v2  = v3;
					vn2 = vn3;
					uv2 = uv3;

					final Vertex vertexJ = vertices.get( j );
					v3  = vertexJ.vertexCoordinateIndex;
					vn3 = smooth ? v3 : 0;
					uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)vertexJ.colorMapU, (double)vertexJ.colorMapV, 0.0 ) ) : -1;

					result.addTriangle( v1, uv1, vn1, v2, uv2, vn2, v3, uv3, vn3, textureIndex );
				}
			}
		}

		return result;
	}

	/**
	 * This method constructs a {@link PovSphere} from a {@link Sphere3D} object.
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   sphere      The {@link Sphere3D} to be converted.
	 *
	 * @return  The resulting {@link PovGeometry} object.
	 */
	public PovGeometry convertSphere3D( final Matrix3D transform, final Sphere3D sphere )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( sphere ) )
		{
			result = convertObject3D( transform, sphere );
		}
		else
		{
			final Face3D     face    = sphere.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( face.material );

			result = new PovSphere( ( sphere.getTag() != null ) ? String.valueOf( sphere.getTag() ) : null, Vector3D.INIT, sphere.radius, texture );
			result.setTransform( new PovMatrix( transform ) );
		}
		return result;
	}

	/**
	 * Convert a {@link Material} to a {@link PovTexture} object.
	 *
	 * @param   material    The {@link Material} object to convert.
	 *
	 * @return  The resulting {@link PovTexture}.
	 */
	@Nullable
	private PovTexture convertMaterialToPovTexture( final Material material )
	{
		PovTexture result;

		if ( material != null )
		{
			final PovTexture newTexture = new PovTexture( _imageMapDirectory, material );
			final String textureName = newTexture.getName();

			result = _scene.getTexture( textureName );
			if ( result == null )
			{
//				newTexture.setReflection( 0.05 );
				newTexture.setDeclared();
				_scene.addTexture( textureName, newTexture );
				result = newTexture;
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * This {@link Node3DTreeWalker} calls conversion methods for all
	 * convertable {@link Node3D} instances it encounters.
	 */
	private class ConvertingVisitor
		implements Node3DVisitor
	{
		@Override
		public boolean visitNode( @NotNull final Node3DPath path )
		{
			final Node3D node = path.getNode();
			if ( node instanceof Light3D )
			{
				_scene.add( convertLight3D( path.getTransform(), (Light3D)node ) );
			}
			else if ( node instanceof Box3D )
			{
				_scene.add( convertBox3D( path.getTransform(), (Box3D)node ) );
			}
			else if ( node instanceof Cone3D )
			{
				_scene.add( convertCone3D( path.getTransform(), (Cone3D)node ) );
			}
			else if ( node instanceof Cylinder3D )
			{
				_scene.add( convertCylinder3D( path.getTransform(), (Cylinder3D)node ) );
			}
			else if ( node instanceof Sphere3D )
			{
				_scene.add( convertSphere3D( path.getTransform(), (Sphere3D)node ) );
			}
//			else if ( node instanceof ExtrudedObject2D ) // not optimized support available
//			{
//				scene.add( convertExtrudedObject2D( path.getTransform(), (ExtrudedObject2D)node ) );
//			}
			else if ( node instanceof Object3D )
			{
				_scene.add( convertObject3D( path.getTransform(), (Object3D)node ) );
			}

			return true;
		}
	}
}
