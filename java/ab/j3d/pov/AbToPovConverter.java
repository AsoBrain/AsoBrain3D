/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
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
	 * Directory containing the used textures.
	 */
	private final String _textureDirectory;

	/**
	 * Construct new converter and set the texture directory.
	 *
	 * @param   textureDirectory    Directory containing POV-textures.
	 */
	public AbToPovConverter( final String textureDirectory )
	{
		if ( textureDirectory == null )
			throw new NullPointerException( "textureDirectory" );

		_textureDirectory = textureDirectory;

		_scene = new PovScene();
	}

	/**
	 * Method to check if an {@link Object3D} contains multiple textures and/or
	 * texture mapping.
	 *
	 * @param   object  The {@link Object3D} to inspect.
	 *
	 * @return  <code>true</code> if the {@link Object3D} contains multiple
	 *          texture and/or texture mapping;
	 *          <code>false</code> otherwise.
	 */
	private static boolean containsMultipleTexturesOrMapping( final Object3D object )
	{
		boolean result = false;

		final int faceCount = object.getFaceCount();
		TextureSpec lastTexture = null;

		for ( int i = 0 ; !result && ( i < faceCount ) ; i++ )
		{
			final Face3D      face    = object.getFace( i );
			final TextureSpec texture = face.getTexture();

			if ( texture != null )
			{
				result = ( ( lastTexture != null ) && !lastTexture.equals( texture ) ) || texture.isTexture();
				lastTexture = texture;
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
	public PovScene convert( final Node3DCollection nodes )
	{
		final PovScene scene = _scene;

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
				scene.add( convertLight3D( transform , (Light3D)node ) );
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
				scene.add( convertObject3D( transform, (Object3D)node ) );
			}
		}

		final PovVector color = new PovVector( new Color( 155 , 155 , 145 ) );

		scene.add( new PovLight( "light1" ,  5000.0 , -5000.0 , 6000.0 , color , true ) );
		scene.add( new PovLight( "light2" , -5000.0 , -5000.0 , 6000.0 , color , true ) );
		scene.add( new PovLight( "light3" , -5000.0 ,  5000.0 , 6000.0 , color , true ) );
		scene.add( new PovLight( "light4" ,  5000.0 ,  5000.0 , 6000.0 , color , true ) );

		/*
		scene.add( new PovLight( "light2" , 5000.0 , -5100.0 , 6000.0 , color , true ) );
		scene.add( new PovLight( "light3" , 5000.0 , -5200.0 , 6000.0 , color , true ) );
		*/

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

		if ( containsMultipleTexturesOrMapping( box ) )
		{
			result = convertObject3D( transform , box );
		}
		else
		{
			final Matrix3D boxTransform = box.getTransform();

			final Vector3D   v1      = Vector3D.INIT;
			final Vector3D   v2      = v1.plus( box.getDX() , box.getDY() , box.getDZ() );
			final Face3D     face    = box.getFace( 0 );
			final PovTexture texture = convertTexture( face.getTexture() );

			result = new PovBox( ( box.getTag() != null ) ? String.valueOf( box.getTag() ) : null , v1 , v2 , texture );
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
		final PovCamera result = new PovCamera(
			/* name     */ ( camera.getTag() != null ) ? String.valueOf( camera.getTag() ) : null ,
		    /* location */ null ,
		    /* lookAt   */ null ,
		    /* angle    */ new PovVector( aspectRatio , 0.0 , 0.0 ) , Math.toDegrees( camera.getAperture() ) );

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

		if ( containsMultipleTexturesOrMapping( cylinder ) )
		{
			result = convertObject3D( transform , cylinder );
		}
		else
		{
			final Face3D     face    = cylinder.getFace( 0 );
			final PovTexture texture = convertTexture( face.getTexture() );

			result = new PovCylinder( ( cylinder.getTag() != null ) ? String.valueOf( cylinder.getTag() ) : null , 0.0 , 0.0 , 0.0 , cylinder.height , cylinder.radiusBottom , cylinder.radiusTop , texture );
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
		//@TODO Convert light when Light3D class is completed.
		return new PovLight( "light", transform.xo, transform.yo, transform.zo, new PovVector( Color.WHITE ), true );
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

		final int      faceCount   = object.getFaceCount();
		final int      pointCount  = object.getPointCount();
		final double[] pointCoords = transform.transform( object.getPointCoords() , null , pointCount );

		final List vertexVectors = new ArrayList( pointCount );
		for ( int pi = 0 , vi = 0 ; vi < pointCount ; pi += 3 , vi++ )
			vertexVectors.add( new PovVector( pointCoords[ pi ] , pointCoords[ pi + 1 ] , pointCoords[ pi + 2 ] ) );

		result.setVertexVectors( vertexVectors );

		double[]    vertexNormals = null;
		TextureSpec lastTexture   = null;
		int         textureIndex  = 0;
		double      textureWidth            = 0.0;
		double      textureHeight            = 0.0;
		boolean     uvMapping     = false;

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D face        = object.getFace( i );
			final int    vertexCount = face.getVertexCount();

			if ( vertexCount > 2 )
			{
				final boolean     smooth       = face.isSmooth();
				final int[]       pointIndices = face.getPointIndices();
				final TextureSpec texture      = face.getTexture();

				/*
				 * Require vertex normals for smooth faces.
				 */
				if ( smooth && ( vertexNormals == null ) )
				{
					vertexNormals = transform.rotate( object.getVertexNormals() , null , pointCount );

					final List normalVectors = new ArrayList( pointCount );
					for ( int pi = 0 , vi = 0 ; vi < pointCount ; pi += 3 , vi++ )
						normalVectors.add( new PovVector( vertexNormals[ pi ] , vertexNormals[ pi + 1 ] , vertexNormals[ pi + 2 ] ) );

					result.setNormalVectors( normalVectors );
				}

				/*
				 * Convert texture.
				 */
				if ( texture != lastTexture )
				{
					final boolean isTexture = texture.isTexture();

					final int w = isTexture ? texture.getTextureWidth ( null ) : -1;
					final int h = isTexture ? texture.getTextureHeight( null ) : -1;

					textureWidth  = (double)w;
					textureHeight = (double)h;
					uvMapping     = ( ( w > 0 ) && ( h > 0 ) );

					textureIndex = result.getOrAddTextureIndex( convertTexture( texture ) );
					lastTexture  = texture;
				}

				/*
				 * For every triangle, the first vertex is the same.
				 */
				final int v1  = pointIndices[ 0 ];
				final int vn1 = smooth ? v1 : 0;
				final int uv1 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( 0 ) / textureWidth , (double)face.getTextureV( 0 ) / textureHeight , 0.0 ) ) : -1;

				int v2;
				int vn2;
				int uv2;

				int v3  = pointIndices[ 1 ];
				int vn3 = smooth ? v3 : 0;
				int uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( 1 ) / textureWidth , (double)face.getTextureV( 1 ) / textureHeight , 0.0 ) ) : -1;

				for ( int j = 2 ; j < vertexCount ; j++ )
				{
					v2  = v3;
					vn2 = vn3;
					uv2 = uv3;

					v3  = pointIndices[ j ];
					vn3 = smooth ? v3 : 0;
					uv3 = uvMapping ? result.getOrAddUvVectorIndex( new PovVector( (double)face.getTextureU( j ) / textureWidth , (double)face.getTextureV( j ) / textureHeight , 0.0 ) ) : -1;

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

		if ( ( sphere.dx != sphere.dy ) || ( sphere.dy != sphere.dz ) || containsMultipleTexturesOrMapping( sphere ) )
		{
			result = convertObject3D( transform , sphere );
		}
		else
		{
			final Face3D     face    = sphere.getFace( 0 );
			final PovTexture texture = convertTexture( face.getTexture() );

			result = new PovSphere( ( sphere.getTag() != null ) ? String.valueOf( sphere.getTag() ) : null , Vector3D.INIT , sphere.dx / 2.0 , texture );
			result.setTransform( new PovMatrix( sphere.xform.multiply( transform ) ) );
		}
		return result;
	}

	/**
	 * Convert a {@link TextureSpec} to a {@link PovTexture} object.
	 *
	 * @param   texture     The {@link TextureSpec} object to convert.
	 *
	 * @return  The resulting {@link PovTexture}.
	 */
	private PovTexture convertTexture( final TextureSpec texture )
	{
		PovTexture result;

		if ( texture != null )
		{
			final PovTexture newTexture = new PovTexture( _textureDirectory , texture );
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