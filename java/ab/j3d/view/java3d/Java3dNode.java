/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.soda.Gerwin.AbtoJ3D;

import java.awt.Canvas;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.image.TextureLoader;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.renderer.LeafCollection;
import ab.j3d.renderer.Object3D;
import ab.j3d.renderer.TreeNode;

import com.numdata.soda.mountings.db.MountingDb;

/**
 * @FIXME Need comment
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class ABtoJ3DConvertor
	extends ViewNode
{
	/**
	 * Texture observer needed by <code>TextureLoader</code> to load textures.
	 */
	private static final Canvas TEXTURE_OBSERVER = new Canvas();

	/**
	 * The j3d root node to convert the Ab root node to.
	 */
	private TransformGroup _j3dRootNode;

	/**
	 * Database access provider.
	 */
	private MountingDb _db;

	/**
	 * The J3D model.
	 */
	private J3DModel _j3dModel;

	/**
	 * Construct new ABtoJ3DConvertor.
	 *
	 * @param   db          Database access provider.
	 * @param   abRootNode  The Ab root node to convert from.
	 * @param   j3dModel    The j3d model.
	 */
	public ABtoJ3DConvertor( final MountingDb db , final TreeNode abRootNode , final J3DModel j3dModel )
	{
		super( abRootNode );

		_db       = db;
		_j3dModel = j3dModel;

		/*
		 * Create the j3d-root node and set the scaling transform.
		 * The scaling transfrom is needed, because j3d uses meters and
		 * AB uses milimeters. A scalar of 0.001 transforms this correctly.
		 */
		_j3dRootNode = new TransformGroup();
		final Transform3D scaleTransform = new Transform3D();
		scaleTransform.rotX( Math.toRadians( -90 ) );
		scaleTransform.setScale( 0.001 );
		_j3dRootNode.setTransform( scaleTransform );

		/*
		 * Set capabilities.
		 */
		_j3dRootNode.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		_j3dRootNode.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE  );
		_j3dRootNode.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );

		/*
		 * Finally do an update to convert the ab root node to the j3d root node.
		 */
		update();
	}

	public void update()
	{
		final LeafCollection leafs = new LeafCollection();
		getAbRootNode().gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		/*
		 * Nodes are not directly added to the j3d root node too avoid flickering.
		 */
		final BranchGroup group = new BranchGroup();
		group.setCapability( BranchGroup.ALLOW_DETACH );

		for ( int i = 0 ; i < leafs.size() ; i++ )
		{
			group.addChild( convertObject3D( leafs.getMatrix( i ) , (Object3D)leafs.getNode( i ) ) );
		}

		if ( _j3dRootNode.numChildren() == 0 )
		{
			_j3dRootNode.addChild( group );
		}
		else
		{
			_j3dRootNode.setChild( group , 0 );
		}
	}

	public void setTransform( final Matrix3D transform )
	{
		super.setTransform( transform );
		_j3dRootNode.setTransform( convertMatrix3D( transform ) );
	}

	/**
	 * Get the j3d root node.
	 *
	 * @return  The j3d root node.
	 */
	public Group getJ3dRootNode()
	{
		return _j3dRootNode;
	}

	/**
	 * Convert <code>Object3D<code/> to Java3D <code>Node</code>
	 * object.
	 *
	 * @param   xform   Transform to apply to vertices.
	 * @param   obj     Object3D to convert.
	 *
	 * @return  If only one j3d <code>Appearance</code> is created
	 *          a <code>Shape3D</code> is returned. Otherwise a <code>
	 *          BranchGroup</code> is returned containing <code>Shape3D</code>s
	 *          for each separate <code>Appearance</code>.
	 */
	private Node convertObject3D( final Matrix3D xform , final Object3D obj )
	{
		final Map appearances = new HashMap();

		final int     faceCount     = obj.getFaceCount();
		final float[] vertices      = obj.getVertices();
		final float[] vertexNormals = obj.getVertexNormals();

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Object3D.Face face        = obj.getFace( i );
			final int[]         faceVert    = face.getPointIndices();
			final int[]         faceTU      = face.getTextureU();
			final int[]         faceTV      = face.getTextureV();
			final TextureSpec   faceTexture = face.getTexture();

			if ( faceTexture == null || faceTexture.code == null )
				continue;

			final int nrTriangles = faceVert.length - 2;
			if ( nrTriangles < 1 )
				continue;

			final Texture texture = ( ( faceTU == null ) || ( faceTV == null ) || !faceTexture.isTexture() ) ? null : convertTextureSpec( faceTexture );

			final List[] data;
			if ( appearances.containsKey( faceTexture.code ) )
			{
				data = (List[])appearances.get( faceTexture.code );
			}
			else
			{
				appearances.put( faceTexture.code , data = new List[] { new ArrayList() , new ArrayList() , new ArrayList() } );
			}

			final List    j3dVertices      = data[ 0 ];
			final List    j3dTextureCoords = data[ 1 ];
			final List    j3dFaceNormals   = data[ 2 ];
			final float[] vertex           = new float[ 3 ];
			final float[] faceNormal       = new float[ 3 ];

			for ( int triangleIndex = 0 ; triangleIndex < nrTriangles ; triangleIndex++ )
			{
				for ( int subIndex = 3 ; --subIndex >= 0 ; )
				{
					final int vertexIndex = ( subIndex == 0 ) ? 0 : ( triangleIndex + subIndex );
					final int vi = faceVert[ vertexIndex ] * 3;

					vertex[ 0 ] = vertices[ vi     ];
					vertex[ 1 ] = vertices[ vi + 1 ];
					vertex[ 2 ] = vertices[ vi + 2 ];
					xform.transform( vertex , vertex , 1 );

					float tu = 0;
					float tv = 0;
					if ( texture != null )
					{
						tu = (float)faceTU[ vertexIndex ] / texture.getWidth();
						tv = (float)faceTV[ vertexIndex ] / texture.getHeight();
					}

					faceNormal[ 0 ] = vertexNormals[ vi     ];
					faceNormal[ 1 ] = vertexNormals[ vi + 1 ];
					faceNormal[ 2 ] = vertexNormals[ vi + 2 ];
					xform.rotate( faceNormal , faceNormal , 1 );

					j3dVertices.add( new Point3f( vertex ) );
					j3dTextureCoords.add( new TexCoord2f( tu , tv ) );
					j3dFaceNormals.add( new Vector3f( faceNormal ) );
				}
			}
		}

		final Node result;
		if ( appearances.size() == 1 )
		{
			final String     code       = (String)appearances.keySet().iterator().next();
			final Appearance appearance = convertTextureSpec( _db.getTextureSpec( code ) , 1 );
			final List[]     data       = (List[])appearances.get( code );

			result = createShape( appearance , data[ 0 ] , data[ 1 ] , data[ 2 ] );
		}
		else
		{
			result = new BranchGroup();
			result.setCapability( BranchGroup.ALLOW_DETACH );

			for ( Iterator appearanceEnum = appearances.keySet().iterator() ; appearanceEnum.hasNext() ; )
			{
				final String     code       = (String)appearanceEnum.next();
				final Appearance appearance = convertTextureSpec( _db.getTextureSpec( code ) , 1 );
				final List[]     data       = (List[])appearances.get( code );

				((BranchGroup)result).addChild( createShape( appearance , data[ 0 ] , data[ 1 ] , data[ 2 ] ) );
			}
		}

		return result;
	}

	/**
	 * Create a <code>Shape3D</code> by using the specified parameters.
	 *
	 * @param   appearance          Appearance of the Shape3D to create.
	 * @param   j3dVertices         Vertices of the Shape3D to create.
	 * @param   j3dTextureCoords    Texture U- and V-coordinates of the Shape3D.
	 * @param   j3dFaceNormals      Face normals of the Shape3D to create.
	 *
	 * @return  Shape3d created out of the specified parameters.
	 */
	private Shape3D createShape( final Appearance appearance , final List j3dVertices , final List j3dTextureCoords , final List j3dFaceNormals )
	{
		final boolean hasTexture = ( appearance.getTexture() != null );

		final int what = GeometryArray.COORDINATES | GeometryArray.NORMALS | ( hasTexture ? GeometryArray.TEXTURE_COORDINATE_2 : 0 );
		final GeometryArray geom = new TriangleArray( j3dVertices.size() , what );

		final Point3f[] coordA = (Point3f[])j3dVertices.toArray( new Point3f[ j3dVertices.size() ] );
		geom.setCoordinates( 0 , coordA );

		if ( hasTexture )
		{
			final TexCoord2f[] textCoordsA = (TexCoord2f[])j3dTextureCoords.toArray( new TexCoord2f[ j3dTextureCoords.size() ] );
			geom.setTextureCoordinates( 0 , 0 , textCoordsA );
		}

		final Vector3f[] normA = (Vector3f[])j3dFaceNormals.toArray( new Vector3f[ j3dFaceNormals.size() ] );
		geom.setNormals( 0 , normA );

		return new Shape3D( geom , appearance );
	}

	/**
	 * Convert <code>TextureSpec<code/> to Java3D <code>Texture</code>
	 * object.
	 *
	 * @param   spec    TextureSpec to convert.
	 */
	private Texture convertTextureSpec( final TextureSpec spec )
	{
		Texture result = null;

		if ( ( spec != null ) && ( spec.isTexture() ) )
		{
			final J3DModel j3dModel = _j3dModel;

			result = j3dModel.getTextureFromCache( spec.code );
			if ( result == null )
			{
				final Image image = spec.getTextureImage();
				if ( image != null )
				{
					result = new TextureLoader( image , TEXTURE_OBSERVER ).getTexture();
					result.setCapability( Texture.ALLOW_SIZE_READ );
					j3dModel.addTextureToCache( spec.code , result );
				}
			}
		}

		return result;
	}

	/**
	 * Convert <code>TextureSpec<code/> to Java3D <code>Appearance</code>
	 * object.
	 *
	 * @param   spec    TextureSpec to convert.
	 * @param   opacity Opacity of the appearance.
	 */
	private Appearance convertTextureSpec( final TextureSpec spec , final float opacity )
	{
		final int   rgb = spec.getARGB();
		final float r   = ( ( rgb >> 16 ) & 255 ) / 255.0f;
		final float g   = ( ( rgb >>  8 ) & 255 ) / 255.0f;
		final float b   = (   rgb         & 255 ) / 255.0f;
		final float ar  = 1.5f * spec.ambientReflectivity;
		final float dr  = 1.2f * spec.diffuseReflectivity;
		final float sr  = 0.5f * spec.specularReflectivity;

		final Material material = new Material();
		material.setLightingEnable( true );
		material.setAmbientColor  ( ar * r , ar * g , ar * b );
		material.setEmissiveColor ( new Color3f( 0.0f , 0.0f , 0.0f ) );
		material.setDiffuseColor  ( dr * r , dr * g , dr * b );
		material.setSpecularColor ( new Color3f( sr , sr , sr ) );
		material.setShininess     ( spec.specularReflectivity * spec.specularExponent );

		final Appearance appearance = new Appearance();
		appearance.setCapability( Appearance.ALLOW_TEXTURE_READ );
		appearance.setMaterial( material );

		if ( spec.isTexture() )
		{
			final Texture texture = convertTextureSpec( spec );
			appearance.setTexture( texture );

			final TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode( TextureAttributes.MODULATE );
			final Transform3D t = new Transform3D();
			t.setScale( spec.textureScale );
			textureAttributes.setTextureTransform( t  );
			appearance.setTextureAttributes( textureAttributes );
		}

		// Setup Transparency
		final float combinedOpacity = opacity * spec.opacity;
		if ( combinedOpacity >= 0.0f && combinedOpacity < 0.999f )
		{
			final TransparencyAttributes transparency = new TransparencyAttributes( TransparencyAttributes.NICEST , 1.0f - combinedOpacity );
			appearance.setTransparencyAttributes( transparency );
		}

		return appearance;
	}

	/**
	 * Convert <code>Matrix3D<code/> to Java3D <code>Transform3D</code>
	 * object.
	 *
	 * @param   matrix  Matrix3D to convert.
	 */
	private Transform3D convertMatrix3D( final Matrix3D matrix )
	{
		return new Transform3D( new float[] {
			matrix.xx , matrix.xy , matrix.xz , matrix.xo ,
			matrix.yx , matrix.yy , matrix.yz , matrix.yo ,
			matrix.zx , matrix.zy , matrix.zz , matrix.zo ,
			0.0f , 0.0f , 0.0f , 1.0f } );
	}
}
