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
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.image.TextureLoader;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.java3d.J3dTools;
import ab.j3d.renderer.LeafCollection;
import ab.j3d.renderer.Object3D;
import ab.j3d.renderer.TreeNode;

import com.numdata.soda.mountings.db.MountingDb;

/**
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 * @FIXME Need comment
 */
public class ABtoJ3DConvertor
{
	/**
	 * The Ab root node to convert from.
	 */
	private TreeNode _abRootNode;

	/**
	 * The j3d parent node to convert the Ab root node to.
	 */
	private TransformGroup _j3dParentNode;

	/**
	 * Database access provider.
	 */
	private MountingDb _db;

	/**
	 * The J3D model.
	 */
	private J3DModel _j3dModel;

	//--------
	private final Canvas TEXTURE_OBSERVER = new Canvas();
	private final Map _textureCache = new HashMap();
	//--------

	/**
	 * Construct new ABtoJ3DConvertor.
	 *
	 * @param   db          Database access provider.
	 * @param   abRootNode  The Ab root node to convert from.
	 * @param   J3DModel    The j3d model.
	 */
	public ABtoJ3DConvertor( final MountingDb db , final TreeNode abRootNode , final J3DModel J3DModel )
	{
		_db          = db;
		_abRootNode  = abRootNode;
		_j3dModel    = J3DModel;

		/*
		 * Create the j3d-parent node and set the scaling transform.
		 * The scaling transfrom is needed, because j3d uses meters and
		 * AB uses milimeters. A scalar of 0.001 transforms this correctly.
		 */
		_j3dParentNode = new TransformGroup();
		final Transform3D scaleTransform = new Transform3D();
		scaleTransform.setScale( 0.001 );
		_j3dParentNode.setTransform( scaleTransform );

		/*
		 * Set capabilities.
		 */
		_j3dParentNode.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		_j3dParentNode.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE  );
		_j3dParentNode.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );

		/*
		 * Finally add the node to the j3d model and do an update.
		 */
		_j3dModel.addNode( _j3dParentNode );
		update();
	}

	/**
	 * Convert the ab root node to the j3d parent node.
	 */
	public void update()
	{
		_j3dParentNode.removeAllChildren();

		final LeafCollection leafs = new LeafCollection();
		_abRootNode.gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		for ( int i = 0 ; i < leafs.size() ; i++ )
		{
			_j3dParentNode.addChild( convertObject3D( 1 , leafs.getMatrix( i ) , (Object3D)leafs.getNode( i ) ) );
		}
	}

	private Group convertObject3D( final float opacity , final Matrix3D xform , final Object3D obj )
	{
		final int     faceCount     = obj.getFaceCount();
		final float[] vertices      = obj.getVertices();
		final float[] vertexNormals = obj.getVertexNormals();
		final float[] vertex        = new float[ 3 ];
		final float[] faceNormal    = new float[ 3 ];

		final Map appearances = new HashMap();

		for ( int face = 0 ; face < faceCount ; face++ )
		{
			final int[]       faceVert    = obj.getFaceVertexIndices( face );
			final int[]       faceTU      = obj.getFaceTextureU( face );
			final int[]       faceTV      = obj.getFaceTextureV( face );
			final TextureSpec faceTexture = obj.getFaceTexture( face );

			if ( faceTexture == null || faceTexture.code == null )
				continue;

			final int nrTriangles = faceVert.length - 2;
			if ( nrTriangles < 1 )
				continue;

			final Texture texture = ( ( faceTU == null ) || ( faceTV == null ) || !faceTexture.isTexture() ) ? null : getTexture( faceTexture );

			final List j3dVertices;
			final List j3dTextureCoords;
			final List j3dFaceNormals;
			{
				final List[] data;
				if ( appearances.containsKey( faceTexture.code ) )
					data = (List[])appearances.get( faceTexture.code );
				else
					appearances.put( faceTexture.code , data = new List[] { new ArrayList() , new ArrayList() , new ArrayList() } );

				j3dVertices      = data[ 0 ];
				j3dTextureCoords = data[ 1 ];
				j3dFaceNormals   = data[ 2 ];
			}

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

		final BranchGroup group = new BranchGroup();
		group.setCapability( BranchGroup.ALLOW_DETACH );

		for ( Iterator appearanceEnum = appearances.keySet().iterator() ; appearanceEnum.hasNext() ; )
		{
			final String     code       = (String)appearanceEnum.next();
			final Appearance appearance = J3dTools.abToJ3D( _db.getTextureSpec( code ) , opacity );
			final boolean    hasTexture = ( appearance.getTexture() != null );

			final List j3dVertices;
			final List j3dTextureCoords;
			final List j3dFaceNormals;
			{
				final List[]   data = (List[])appearances.get( code );
				j3dVertices      = data[ 0 ];
				j3dTextureCoords = data[ 1 ];
				j3dFaceNormals   = data[ 2 ];
			}

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

			final Shape3D shape = new Shape3D( geom , appearance );
			group.addChild( shape );
		}

		return group;
	}

	private Texture getTexture( final TextureSpec code )
	{
		Texture result = null;

		if ( ( code != null ) && ( code.isTexture() ) )
		{
			result = (Texture)_textureCache.get( code.code );
			if ( result == null )
			{
				final Image image = code.getTextureImage();
				if ( image != null )
				{
					result = new TextureLoader( image , TEXTURE_OBSERVER ).getTexture();
					result.setCapability( Texture.ALLOW_SIZE_READ );
					_textureCache.put( code.code , result );
				}
			}
		}

		return result;
	}

	/**
	 * Set the transform for the j3d parent node.
	 *
	 * @param   transform   Transform to set.
	 *                      Note: Because j3d uses meters and AB uses milimeters
	 *                            a scalar of 0.001 should be added.
	 */
	public void setTransform( final Matrix3D transform )
	{
		_j3dParentNode.setTransform( J3dTools.abToJ3D( transform ) );
	}

	/**
	 * Get the ab root node.
	 *
	 * @return  The ab root node.
	 */
	public TreeNode getAbRootNode()
	{
		return _abRootNode;
	}
}
