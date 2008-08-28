/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2008
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Box3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;

/**
 * This class can be used to convert a 3D scene from <code>ab.j3d.model</code>
 * into a POV-Ray scene ({@link PovScene}).
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public final class AbToPovConverter
{
	/**
	 * Variable that will hold the converted scene.
	 */
	private PovScene _scene;

	/**
	 * Directory containing the used materials.
	 */
	private final String _imageMapDirectory;

	/**
	 * Construct new converter and set the image map directory.
	 *
	 * @param   imageMapDirectory    Directory containing POV-image maps.
	 */
	public AbToPovConverter( final String imageMapDirectory )
	{
		if ( imageMapDirectory == null )
			throw new NullPointerException( "imageMapDirectory" );

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
			final Material material = face.getMaterial();

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
	 * @param   nodes   Nodes that need to be converted.
	 *
	 * @return  The resulting {@link PovScene} object.
	 */
	public PovScene convert( final Node3DCollection<Node3D> nodes )
	{
		final PovScene scene = _scene;

		int ambient = 0;

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final Node3D   node      = nodes.getNode( i );
			final Matrix3D transform = nodes.getMatrix( i );

			if ( node instanceof Camera3D )
			{
//				scene.add( convertCamera3D( transform , (Camera3D)node , 1.0 ) );
			}
			else if ( node instanceof Light3D )
			{
				final Light3D light = (Light3D)node;
				if ( light.isAmbient() )
				{
					ambient += light.getIntensity();
				}
				else
				{
					scene.add( convertLight3D( transform , light ) );
				}
			}
			else if ( node instanceof Box3D )
			{
				scene.add( convertBox3D( transform , (Box3D)node ) );
			}
			else if ( node instanceof Cylinder3D )
			{
				scene.add( convertCylinder3D( transform , (Cylinder3D)node ) );
			}
			else if ( node instanceof Sphere3D )
			{
				scene.add( convertSphere3D( transform , (Sphere3D)node ) );
			}
//			else if ( node instanceof ExtrudedObject2D ) // not optimized support available
//			{
//				scene.add( convertExtrudedObject2D( transform , (ExtrudedObject2D)node ) );
//			}
			else if ( node instanceof Object3D )
			{
				scene.add( convertObject3D( transform , (Object3D)node ) );
			}
		}

		final double povAmbient = (double)ambient / 255.0;
		scene.setAmbientLight( new PovVector( povAmbient , povAmbient , povAmbient ) );

		return scene;
	}

	/**
	 * This method constructs a {@link PovBox} from a {@link Box3D} object.
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   box         The {@link Box3D} to be converted.
	 *
	 * @return  The resulting {@link PovGeometry} object.
	 */
	public PovGeometry convertBox3D( final Matrix3D transform , final Box3D box )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( box ) )
		{
			result = convertObject3D( transform , box );
		}
		else
		{
			final Matrix3D boxTransform = box.getTransform();
			final Face3D   anyFace      = box.getFace( 0 );

			final String     name    = ( box.getTag() != null ) ? String.valueOf( box.getTag() ) : null;
			final Vector3D   v1      = Vector3D.INIT;
			final Vector3D   v2      = v1.plus( box.getDX() , box.getDY() , box.getDZ() );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.getMaterial() );

			result = new PovBox( name , v1 , v2 , texture );
			result.setTransform( new PovMatrix( boxTransform.multiply( transform ) ) );
		}

		return result;
	}

	/**
	 * This method constructs a {@link PovCamera} from a {@link Camera3D} object.
	 * <p>
	 * The standard ratio used in POV-Ray is 4:3, wich means the rendered
	 * image will get deformed if resolutions with a ratio other than 4:3
	 * are used. Since the resulting image does not have a fixed ratio (the user
	 * can choose any size, for example), the ratio also needs to be specified
	 * in the POV-Ray camera.
	 *
	 * @param   transform       Camera transform (camera -> model, NOT model->camera/view transform).
	 * @param   camera          The {@link Camera3D} object to be converted.
	 * @param   aspectRatio     Aspect ratio of image (for square pixels: width / height).
	 *
	 * @return  The resulting {@link PovCamera} object.
	 */
	public static PovCamera convertCamera3D( final Matrix3D transform , final Camera3D camera , final double aspectRatio )
	{
		final String    name     = ( camera.getTag() != null ) ? String.valueOf( camera.getTag() ) : null;
		final PovVector location = null;
		final PovVector lookAt   = null;
		final PovVector right    = new PovVector( aspectRatio , 0.0 , 0.0 );
		final double    angle    = Math.toDegrees( camera.getAperture() );

		final PovCamera result = new PovCamera( name , location , lookAt , right , angle );

		result.setTransform( new PovMatrix( new double[]
			{
				 transform.xx ,  transform.yx ,  transform.zx ,
				 transform.xy ,  transform.yy ,  transform.zy ,
				-transform.xz , -transform.yz , -transform.zz ,
				 transform.xo ,  transform.yo ,  transform.zo
			} ) );

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
	public PovGeometry convertCylinder3D( final Matrix3D transform , final Cylinder3D cylinder )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( cylinder ) )
		{
			result = convertObject3D( transform , cylinder );
		}
		else
		{
			final String     name    = ( cylinder.getTag() != null ) ? String.valueOf( cylinder.getTag() ) : null;
			final Face3D     anyFace = cylinder.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.getMaterial() );

			result = new PovCylinder( name , 0.0 , 0.0 , 0.0 , cylinder.height , cylinder.radiusBottom , cylinder.radiusTop , texture );
			result.setTransform( new PovMatrix( cylinder.xform.multiply( transform ) ) );
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
	public static PovLight convertLight3D( final Matrix3D transform , final Light3D light )
	{
		if ( light.isAmbient() )
			throw new IllegalArgumentException( "ambient lights can't be created, only set as global property" );

		final String    name    = ( light.getTag() != null ) ? String.valueOf( light.getTag() ) : null;
		final PovVector color   = new PovVector( Color.WHITE , (double)light.getIntensity() / 255.0 );
		final double    fallOff = light.getFallOff();

		final PovLight result = new PovLight( name , transform.xo , transform.yo , transform.zo , color , true );

		if ( fallOff > 0.0001 )
		{
			result.setFadeDistance( fallOff );
			result.setFadePower( PovLight.FADE_QUADRATIC );
		}
		else
		{
			result.setFadePower( PovLight.FADE_NONE );
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
	 * Triangle 1: ( 0 , 1 , 2 )<br>
	 * Triangle 2: ( 0 , 2 , 3 )
	 *
	 * @param   transform   Transform to apply to node.
	 * @param   object      The {@link Object3D} to be converted.
	 *
	 * @return  The resulting {@link PovMesh2} object.
	 */
	public PovMesh2 convertObject3D( final Matrix3D transform , final Object3D object )
	{
		final PovMesh2 result = new PovMesh2( ( object.getTag() != null ) ? String.valueOf( object.getTag() ) : null );

		final int      faceCount         = object.getFaceCount();
		final int      vertexCount       = object.getVertexCount();
		final double[] vertexCoordinates = object.getVertexCoordinates( transform , null );

		final List<PovVector> vertexVectors = new ArrayList<PovVector>( vertexCount );
		for ( int pi = 0 , vi = 0 ; vi < vertexCount ; pi += 3 , vi++ )
			vertexVectors.add( new PovVector( vertexCoordinates[ pi ] , vertexCoordinates[ pi + 1 ] , vertexCoordinates[ pi + 2 ] ) );

		result.setVertexVectors( vertexVectors );

		double[] vertexNormals = null;
		Material lastMaterial  = null;
		int      textureIndex  = 0;
		boolean  uvMapping     = false;

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face            = object.getFace( i );
			final int    faceVertexCount = face.getVertexCount();

			if ( faceVertexCount > 2 )
			{
				final boolean  smooth        = face.isSmooth();
				final int[]    vertexIndices = face.getVertexIndices();
				final Material material      = face.getMaterial();

				/*
				 * Require vertex normals for smooth faces.
				 */
				if ( smooth && ( vertexNormals == null ) )
				{
					vertexNormals = object.getVertexNormals( transform , null );

					final List<PovVector> normalVectors = new ArrayList<PovVector>( vertexCount );
					for ( int pi = 0 , vi = 0 ; vi < vertexCount ; pi += 3 , vi++ )
						normalVectors.add( new PovVector( vertexNormals[ pi ] , vertexNormals[ pi + 1 ] , vertexNormals[ pi + 2 ] ) );

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
				final int v1  = vertexIndices[ 0 ];
				final int vn1 = smooth ? v1 : 0;
				final int uv1 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( 0 ) , (double)face.getTextureV( 0 ) , 0.0 ) ) : -1;

				int v2;
				int vn2;
				int uv2;

				int v3  = vertexIndices[ 1 ];
				int vn3 = smooth ? v3 : 0;
				int uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( 1 ) , (double)face.getTextureV( 1 ) , 0.0 ) ) : -1;

				for ( int j = 2 ; j < faceVertexCount ; j++ )
				{
					v2  = v3;
					vn2 = vn3;
					uv2 = uv3;

					v3  = vertexIndices[ j ];
					vn3 = smooth ? v3 : 0;
					uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( j ) , (double)face.getTextureV( j ) , 0.0 ) ) : -1;

					result.addTriangle( v1 , uv1 , vn1 , v2 , uv2 , vn2 , v3 , uv3 , vn3 , textureIndex );
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
	public PovGeometry convertSphere3D( final Matrix3D transform , final Sphere3D sphere )
	{
		final PovGeometry result;

		if ( ( sphere.dx != sphere.dy ) || ( sphere.dy != sphere.dz ) || containsMultipleMaterialsOrMaps( sphere ) )
		{
			result = convertObject3D( transform , sphere );
		}
		else
		{
			final Face3D     face    = sphere.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( face.getMaterial() );

			result = new PovSphere( ( sphere.getTag() != null ) ? String.valueOf( sphere.getTag() ) : null , Vector3D.INIT , sphere.dx / 2.0 , texture );
			result.setTransform( new PovMatrix( sphere.xform.multiply( transform ) ) );
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
	private PovTexture convertMaterialToPovTexture( final Material material )
	{
		PovTexture result;

		if ( material != null )
		{
			final PovTexture newTexture = new PovTexture( _imageMapDirectory , material );
			final String textureName = newTexture.getName();

			result = _scene.getTexture( textureName );
			if ( result == null )
			{
//				newTexture.setReflection( 0.05 );
				newTexture.setDeclared();
				_scene.addTexture( textureName , newTexture );
				result = newTexture;
			}
		}
		else
		{
			result = null;
		}

		return result;
	}
}
