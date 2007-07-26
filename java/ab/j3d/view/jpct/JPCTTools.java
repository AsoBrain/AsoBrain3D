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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

import ab.j3d.MapTools;
import ab.j3d.Material;
import ab.j3d.Matrix3D;
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

		final Object3D result = new Object3D( maxTriangles );
		result.setShadingMode( Object3D.SHADING_FAKED_FLAT );
		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ab.j3d.model.Object3D node              = (ab.j3d.model.Object3D)nodes.getNode( i );
			final double[]              vertexCoordinates = node.getVertexCoordinates();
			final int                   faceCount         = node.getFaceCount();

			for ( int j = 0 ; j < faceCount ; j++ )
			{
				final Face3D      face        = node.getFace( j );
				final int         vertexCount = face.getVertexCount();

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
					final int[] vertexIndices = face.getVertexIndices();
					final int   texture       = getTextureID( face.getMaterial() );

					int vi = vertexIndices[ 0 ] * 3;

					final SimpleVector p0 = new SimpleVector( vertexCoordinates[ vi ] , vertexCoordinates[ vi + 1 ] , vertexCoordinates[ vi + 2 ] );
					final float        u0 = face.getTextureU( 0 );
					final float        v0 = face.getTextureV( 0 );

					vi = vertexIndices[ 1 ] * 3;

					final SimpleVector p1 = new SimpleVector( vertexCoordinates[ vi ] , vertexCoordinates[ vi + 1 ] , vertexCoordinates[ vi + 2 ] );
					float u1 = face.getTextureU( 1 );
					float v1 = face.getTextureV( 1 );

					final SimpleVector p2 = new SimpleVector();

					for( int k = 2 ; k < vertexCount ; k++ )
					{
						vi = vertexIndices[ k ] * 3;

						p2.x = (float)vertexCoordinates[ vi     ];
						p2.y = (float)vertexCoordinates[ vi + 1 ];
						p2.z = (float)vertexCoordinates[ vi + 2 ];
						final float u2 = face.getTextureU( k );
						final float v2 = face.getTextureV( k );

						result.addTriangle( p2 , u2 , v2 , p1 , u1 , v1 , p0 , u0 , v0 , texture );

						p1.set( p2 );
						u1 = u2;
						v1 = v2;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Converts a {@link Material} to jPCT texture and return its ID.
	 *
	 * @param   material    Material specification to be converted.
	 *
	 * @return  Texture ID.
	 */
	public static int getTextureID( final Material material )
	{
		final TextureManager textureManager = TextureManager.getInstance();

		int result = -1;

		final String colorMap = material.colorMap;
		if ( colorMap != null )
		{
			result = textureManager.getTextureID( colorMap );
			if ( result < 0 )
			{
				final Image colorMapImage = MapTools.getImage( colorMap );
				if ( colorMapImage != null )
				{
					textureManager.addTexture( colorMap , new Texture( colorMapImage ) );
					result = textureManager.getTextureID( colorMap );
				}
			}
		}

		if ( result < 0 )
		{
			final int    argb       = material.getARGB();
			final String argbString = String.valueOf( argb );

			result = textureManager.getTextureID( argbString );
			if ( result < 0 )
			{
				final int size = 8; // smaller sizes are not supported
				final BufferedImage image = new BufferedImage( size , size , BufferedImage.TYPE_INT_ARGB ); // @TODO CHECK IF REALLY ARGB!
				final Graphics g = image.getGraphics();
				g.setColor( new Color( argb ) );
				g.fillRect( 0 , 0 , size , size );
				g.dispose();

				textureManager.addTexture( argbString , new Texture( image , true ) );
				result = textureManager.getTextureID( argbString );
			}
		}

		return result;
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
