/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.view.jpct;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3DCollection;

/**
 * Utility methods for jPCT support.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JPCTTools()
	{
	}

	/**
	 * Converts an {@link ab.j3d.model.Object3D} into a jPCT {@link Object3D}
	 *
	 * @param   object3d    Object to be converted.
	 *
	 * @return  Result of the conversion.
	 */
	// Code must be changed to increase performance, but works for now.
	public static Object3D convert2Object3D( final ab.j3d.model.Object3D object3d )
	{
		final Node3DCollection nodes = new Node3DCollection();
		object3d.gatherLeafs( nodes , ab.j3d.model.Object3D.class , Matrix3D.INIT , false );

		/*
		 * Determine number of triangles for creation of an Object3D.
		 */
		int maxTriangles = 0;
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ab.j3d.model.Object3D node = (ab.j3d.model.Object3D)nodes.getNode( i );

			final int faceCount = node.getFaceCount();
			for ( int j = 0 ; j < faceCount ; j++ )
			{
				final Face3D face = node.getFace( j );

				final int vertexCount = face.getVertexCount();
				maxTriangles += ( vertexCount > 2 ) ? vertexCount - 2 : 0;
			}
		}

		final TextureManager textureManager = TextureManager.getInstance();

		final Object3D result = new Object3D( maxTriangles );
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ab.j3d.model.Object3D node      = (ab.j3d.model.Object3D)nodes.getNode( i );
			final int                   faceCount = node.getFaceCount();

			for ( int j = 0 ; j < faceCount ; j++ )
			{
				final Face3D      face        = node.getFace( j );
				final int         vertexCount = face.getVertexCount();
				final TextureSpec textureSpec = face.getTexture();
				final TextureInfo texture     = convert2TextureInfo( textureSpec );
				final int         textureID   = textureManager.getTextureID( textureSpec.code );

				final Image textureImage  = textureSpec.getTextureImage();
				final boolean isTexture = textureSpec.isTexture() && ( textureImage != null );

				final float textureWidth  = isTexture ? (float)textureImage.getWidth( null )  : 0.0f;
				final float textureHeight = isTexture ? (float)textureImage.getHeight( null ) : 0.0f;

				if ( vertexCount < 2 )
				{
					/* Ignore... */
				}
				else if ( vertexCount == 2 )
				{
					/* Ignore for now, in future draw a line? */
				}
				else
				{
					for( int k = 0 ; k + 2 < vertexCount ; k++ )
					{
						final SimpleVector vert1 = new SimpleVector( face.getX(     0 ), face.getY(     0 ), face.getZ(     0 ) );
						final SimpleVector vert2 = new SimpleVector( face.getX( k + 1 ), face.getY( k + 1 ), face.getZ( k + 1 ) );
						final SimpleVector vert3 = new SimpleVector( face.getX( k + 2 ), face.getY( k + 2 ), face.getZ( k + 2 ) );

						if ( isTexture )
						{
							result.addTriangle( vert3 , (float)face.getTextureU( k + 2 ) / textureWidth , (float)face.getTextureV( k + 2 ) / textureHeight , vert2 , (float)face.getTextureU( k + 1 ) / textureWidth , (float)face.getTextureV( k + 1 ) / textureHeight , vert1 , (float)face.getTextureU( 0 ) / textureWidth , (float)face.getTextureV( 0 ) / textureHeight , textureID );
						}
						else
						{
							result.addTriangle( vert3 , vert2 , vert1 , texture );
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Converts a {@link TextureSpec} to jPCT {@link TextureInfo}.
	 *
	 * @param   textureSpec     Texture specification to be converted.
	 *
	 * @return  Result of the conversion.
	 */
	public static TextureInfo convert2TextureInfo( final TextureSpec textureSpec )
	{
		final String textureCode = textureSpec.code;

		final TextureManager textureManager = TextureManager.getInstance();

		if ( !textureManager.containsTexture( textureCode ) )
		{
			if ( textureSpec.isTexture() )
			{
				final Image image = textureSpec.getTextureImage();
				textureManager.addTexture( textureCode , new Texture( image ) );
			}
			else
			{
				final int size = 8; // smaller sizes are not supported
				final BufferedImage image = new BufferedImage( size , size , BufferedImage.TYPE_INT_ARGB ); // @TODO CHECK IF REALLY ARGB!
				final Graphics g = image.getGraphics();
				g.setColor( textureSpec.getColor() );
				g.fillRect( 0 , 0 , size , size );
				g.dispose();
				textureManager.addTexture( textureCode , new Texture( image , true ) );
			}
		}

		return new TextureInfo( textureManager.getTextureID( textureCode ) );
	}

	/**
	 * Converts a {@link Matrix3D} to an equivalent jPCT {@link Matrix}.
	 *
	 * @param   m   Matrix to be converted.
	 *
	 * @return  Result of the conversion.
	 */
	public static Matrix convert2Matrix( final Matrix3D m )
	{
		final Matrix result = new Matrix();

		result.setDump( new float[]
		{
			(float)m.xx , (float)m.yx , (float)m.zx , 0.0f ,
			(float)m.xy , (float)m.yy , (float)m.zy , 0.0f ,
			(float)m.xz , (float)m.yz , (float)m.zz , 0.0f ,
			(float)m.xo , (float)m.yo , (float)m.zo , 1.0f
		} );

		return result;
	}

	/**
	 * Sets the transformation of the given object. This requires the given
	 * matrix to be split into a rotation and translation matrix.
	 *
	 * @param   object3D    Object to set the transformation of.
	 * @param   transform   Transformation to be set.
	 */
	public static void setTransformation( final Object3D object3D , final Matrix3D transform )
	{
		final Matrix matrix = convert2Matrix( transform );

		final SimpleVector translationVector = matrix.getTranslation();
		final Matrix translation = new Matrix();
		translation.translate( translationVector );

		final Matrix rotation = matrix.cloneMatrix();
		translationVector.scalarMul( -1.0f );
		rotation.translate( translationVector );

		object3D.setRotationMatrix( rotation );
		object3D.setTranslationMatrix( translation );
	}
}
