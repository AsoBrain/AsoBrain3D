/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2009
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

import java.util.ArrayList;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Box3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.DirectionalLight3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Face3D.Vertex;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Scene;
import ab.j3d.model.Sphere3D;
import ab.j3d.model.SpotLight3D;

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

		final Node3DCollection<Node3D> nodes = scene.getContent();
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
				povScene.add( convertLight3D( transform , (Light3D)node ) );
			}
			else if ( node instanceof Box3D )
			{
				povScene.add( convertBox3D( transform , (Box3D)node ) );
			}
			else if ( node instanceof Cylinder3D )
			{
				povScene.add( convertCylinder3D( transform , (Cylinder3D)node ) );
			}
			else if ( node instanceof Sphere3D )
			{
				povScene.add( convertSphere3D( transform , (Sphere3D)node ) );
			}
//			else if ( node instanceof ExtrudedObject2D ) // not optimized support available
//			{
//				scene.add( convertExtrudedObject2D( transform , (ExtrudedObject2D)node ) );
//			}
			else if ( node instanceof Object3D )
			{
				povScene.add( convertObject3D( transform , (Object3D)node ) );
			}
		}

		povScene.setAmbientLight( new PovVector( (double)scene.getAmbientRed() , (double)scene.getAmbientGreen() , (double)scene.getAmbientBlue() ) );

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
	public PovGeometry convertBox3D( final Matrix3D box2wcs , final Box3D box )
	{
		final PovGeometry result;

		if ( containsMultipleMaterialsOrMaps( box ) )
		{
			result = convertObject3D( box2wcs , box );
		}
		else
		{
			final Face3D   anyFace      = box.getFace( 0 );

			final String     name    = ( box.getTag() != null ) ? String.valueOf( box.getTag() ) : null;
			final Vector3D   v1      = Vector3D.INIT;
			final Vector3D   v2      = v1.plus( box.getDX() , box.getDY() , box.getDZ() );
			final PovTexture texture = convertMaterialToPovTexture( anyFace.material );

			result = new PovBox( name , v1 , v2 , texture );
			result.setTransform( new PovMatrix( box2wcs ) );
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
			final PovTexture texture = convertMaterialToPovTexture( anyFace.material );

			result = new PovCylinder( name , 0.0 , 0.0 , 0.0 , cylinder.height , cylinder.radiusBottom , cylinder.radiusTop , texture );
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
	public static PovLight convertLight3D( final Matrix3D transform , final Light3D light )
	{
		final String    name   = ( light.getTag() != null ) ? String.valueOf( light.getTag() ) : null;

		/*
		 * NOTE: The light intensity has to be halved for attenuated POV lights,
		 * because POV defines attenuation with respect to the full light
		 * intensity, while 'Light3D' specifies the distance to half light
		 * intensity.
		 */
		final PovLight result;

		if ( light instanceof DirectionalLight3D )
		{
			final DirectionalLight3D directionalLight3D = (DirectionalLight3D)light;
			final PovVector color  = new PovVector( (double)light.getDiffuseRed() , (double)light.getDiffuseGreen() , (double)light.getDiffuseBlue() );
			result = new PovLight( name , transform.xo , transform.yo , transform.zo , color , true );
			final PovVector direction  = new PovVector( directionalLight3D.getDirection() );

			result.makeParallel( direction );
		}
		else if ( light instanceof SpotLight3D )
		{
			final SpotLight3D spotLight3D = (SpotLight3D)light;
			// Set concentration to 100.0 max
			final double tightness  = (double)spotLight3D.getConcentration() / 128.0 * 100.0;
			final double angle = (double)spotLight3D.getSpreadAngle();
			final PovVector color  = new PovVector( (double)light.getDiffuseRed() , (double)light.getDiffuseGreen() , (double)light.getDiffuseBlue() );
			result = new PovLight( name , transform.xo , transform.yo , transform.zo , color , true );
			final PovVector target  = new PovVector( spotLight3D.getDirection() );
			result.makeSpot( target , 0.0 , angle );
			result.setTightness( tightness );
		}
		else
		{
			if ( light.getQuadraticAttenuation() > 0.0f || light.getLinearAttenuation() > 0.0f )
			{
				final PovVector color  = new PovVector( 0.5 * (double)light.getDiffuseRed() , 0.5 * (double)light.getDiffuseGreen() , 0.5 * (double)light.getDiffuseBlue() );
				result = new PovLight( name , transform.xo , transform.yo , transform.zo , color , true );
			}
			else
			{
				final PovVector color  = new PovVector( (double)light.getDiffuseRed() , (double)light.getDiffuseGreen() , (double)light.getDiffuseBlue() );
				result = new PovLight( name , transform.xo , transform.yo , transform.zo , color , true );
			}
		}

		if ( light.getQuadraticAttenuation() > 0.0f )
		{
			result.setFadeDistance( (double)light.getHalfIntensityDistance() );
			result.setFadePower( PovLight.FADE_QUADRATIC );
		}
		else if ( light.getLinearAttenuation() > 0.0f )
		{
			result.setFadeDistance( (double)light.getHalfIntensityDistance() );
			result.setFadePower( PovLight.FADE_LINEAR );
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

		final int      faceCount             = object.getFaceCount();
		final int      vertexCoordinateCount = object.getVertexCount();
		final double[] vertexCoordinates     = object.getVertexCoordinates();

		final List<PovVector> vertexVectors = new ArrayList<PovVector>( vertexCoordinateCount );
		for ( int vertexCoordinateIndex = 0 ; vertexCoordinateIndex < vertexCoordinateCount ; vertexCoordinateIndex++ )
		{
			final int vi3 = vertexCoordinateIndex * 3;
			vertexVectors.add( new PovVector( transform.transform( vertexCoordinates[ vi3 ] , vertexCoordinates[ vi3 + 1 ] , vertexCoordinates[ vi3 + 2 ] ) ) );
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

					final List<PovVector> normalVectors = new ArrayList<PovVector>( vertexCoordinateCount );
					for ( int vertexCoordinateIndex = 0 ; vertexCoordinateIndex < vertexCoordinateCount ; vertexCoordinateIndex++ )
					{
						final int vi3 = vertexCoordinateIndex * 3;
						final double nx = vertexNormals[ vi3 ];
						final double ny = vertexNormals[ vi3 + 1 ];
						final double nz = vertexNormals[ vi3 + 2 ];
						normalVectors.add( new PovVector( transform.rotate( nx , ny , nz ) ) );
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
				final int uv1 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)( vertex0.colorMapU * material.colorMapWidth ) , (double)( vertex0.colorMapV * material.colorMapHeight ) , 0.0 ) ) : -1;

				int v2;
				int vn2;
				int uv2;

				final Vertex vertex1 = vertices.get( 1 );
				int v3  = vertex1.vertexCoordinateIndex;
				int vn3 = smooth ? v3 : 0;
				int uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)( vertex1.colorMapU * material.colorMapWidth ) , (double)( vertex1.colorMapV * material.colorMapHeight ) , 0.0 ) ) : -1;

				for ( int j = 2 ; j < vertexCount ; j++ )
				{
					v2  = v3;
					vn2 = vn3;
					uv2 = uv3;

					final Vertex vertexJ = vertices.get( j );
					v3  = vertexJ.vertexCoordinateIndex;
					vn3 = smooth ? v3 : 0;
					uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)( vertexJ.colorMapU * material.colorMapWidth ) , (double)( vertexJ.colorMapV * material.colorMapHeight ) , 0.0 ) ) : -1;

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

		if ( containsMultipleMaterialsOrMaps( sphere ) )
		{
			result = convertObject3D( transform , sphere );
		}
		else
		{
			final Face3D     face    = sphere.getFace( 0 );
			final PovTexture texture = convertMaterialToPovTexture( face.material );

			result = new PovSphere( ( sphere.getTag() != null ) ? String.valueOf( sphere.getTag() ) : null , Vector3D.INIT , sphere.radius , texture );
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
