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

import java.awt.image.BufferedImage;

import com.threed.jpct.TextureInfo;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.Object3D;
import com.threed.jpct.Matrix;
import com.threed.jpct.SimpleVector;

import ab.j3d.TextureSpec;
import ab.j3d.Matrix3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Face3D;

/**
 * Utility methods for jPCT support.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTTools
{
	private static final TextureManager _texMan = TextureManager.getInstance();

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JPCTTools()
	{
	}

	// Code must be changed to increase performance, but works for now.
	public static Object3D convert2Object3D( final ab.j3d.model.Object3D object3d )
	{
		final int maxTriangles = 500; // @TODO How to derive this value from object3d?
		final Object3D result = new Object3D( maxTriangles );

		final Node3DCollection nodes = new Node3DCollection();
		object3d.gatherLeafs( nodes , ab.j3d.model.Object3D.class , Matrix3D.INIT , false );

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ab.j3d.model.Object3D node  = (ab.j3d.model.Object3D)nodes.getNode( i );
			final int                   faceCount = node.getFaceCount();

			for ( int j = 0 ; j < faceCount ; j++ )
			{
				final Face3D      face        = node.getFace( j );
				final int         vertexCount = face.getVertexCount();
				final TextureInfo texture     = convert2TextureInfo( face.getTexture() );

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

						result.addTriangle( vert3 , vert2 , vert1 , texture );
					}
				}
			}
		}

		return result;
	}

	// Only converts solid colors, should add texture support..
	public static TextureInfo convert2TextureInfo( final TextureSpec texture )
	{
		final String textureCode = texture.code;

		final Texture result = _texMan.getTexture( textureCode );
		if ( result == null )
		{
			if ( texture.isTexture() )
			{
				// Not implemented yet...
			}
			else
			{
				final BufferedImage image = new BufferedImage( 1 , 1 , BufferedImage.TYPE_INT_ARGB ); // @TODO CHECK IF REALLY ARGB!
				image.setRGB( 0 , 0 , texture.getARGB() );

				_texMan.addTexture( textureCode , new Texture( image ) );
			}
		}

		return new TextureInfo( _texMan.getTextureID( textureCode ) );
	}

	public static Matrix convert2Matrix( final Matrix3D m )
	{
		final Matrix result = new Matrix();

		result.setDump( new float[]
//		{
//			(float)m.xx , (float)m.xy , (float)m.xz , (float)m.xo ,
//			(float)m.yx , (float)m.yy , (float)m.yz , (float)m.yo ,
//			(float)m.zx , (float)m.zy , (float)m.zz , (float)m.zo ,
//			       0.0f ,        0.0f ,        0.0f ,        1.0f
//		} );
		{
			(float)m.xx , (float)m.yx , (float)m.zx , 0.0f ,
			(float)m.xy , (float)m.yy , (float)m.zy , 0.0f ,
			(float)m.xz , (float)m.yz , (float)m.zz , 0.0f ,
			(float)m.xo , (float)m.yo , (float)m.zo , 1.0f
		} );

		return result;
	}
}
