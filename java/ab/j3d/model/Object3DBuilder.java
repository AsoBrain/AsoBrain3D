/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
package ab.j3d.model;

import java.awt.geom.Ellipse2D;

import ab.j3d.Abstract3DObjectBuilder;
import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

/**
 * This class provides an implementation of the {@link Abstract3DObjectBuilder}
 * class for creating an {@link Object3D} instance.
 *
 * @see     Abstract3DObjectBuilder
 * @see     Object3D
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Object3DBuilder
	extends Abstract3DObjectBuilder
{
	/**
	 * The {@link Object3D} being build.
	 */
	private final Object3D _target;

	/**
	 * Construct builder.
	 */
	public Object3DBuilder()
	{
		_target = new Object3D();
	}

	/**
	 * Get {@link Node3D} that was built.
	 *
	 * @return  The {@link Node3D} that was built.
	 */
	public Node3D getObject3D()
	{
		return _target;
	}

	public void addLine( final Vector3D point1 , final Vector3D point2 , final int stroke , final TextureSpec textureSpec )
	{
		_target.addFace( new Vector3D[] { point1 , point2 } , textureSpec , false , true );
	}

	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final TextureSpec textureSpec , final boolean hasBackface )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 } , textureSpec , false , hasBackface );
	}

	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final TextureSpec textureSpec , final boolean hasBackface )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 , point4 } , textureSpec , false , hasBackface );
	}

	public void addCircle( final Vector3D centerPoint , final double radius , final Vector3D normal , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		final Matrix3D  base      = Matrix3D.getPlaneTransform( centerPoint , normal , true );
		final Ellipse2D ellipse2d = new Ellipse2D.Double( -radius , -radius , radius * 2.0 , radius * 2.0 );

		ExtrudedObject2D.generate( _target , ellipse2d , extrusion , base , textureSpec , radius * 0.02 , true );
	}

	public void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final TextureSpec textureSpec )
	{
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested,
	 * the ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used
	 * to rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for Z!
	 *
	 * @param   xform               Transform to apply to points.
	 * @param   radii               X coordinates of 2D outline.
	 * @param   zCoordinates        Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   texture             Texture to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 *
	 * @return  Generated {@link Object3D}.
	 */
	public static Object3D constructRotatedObject( final Matrix3D xform , final double[] radii , final double[] zCoordinates , final int detail , final TextureSpec texture , final boolean smoothCircumference , final boolean closeEnds )
	{
		final Object3D result = new Object3D();
		int[] prevPointIndices = null;

		for ( int i = 0 ; i < radii.length ; i++ )
		{
			final double radius = radii[ i ];
			final double z      = zCoordinates[ i ];

			/*
			 * Based on 'radius', create a list of point indices at this point.
			 */
			final int[] pointIndices;
			if ( Matrix3D.almostEqual( radius , 0.0 ) )
			{
				pointIndices = new int[] { ( xform == null )
					? result.getOrAddPointIndex( 0.0 , 0.0 , z )
					: result.getOrAddPointIndex( xform.transformX( 0.0 , 0.0 , z ) , xform.transformY( 0.0 , 0.0 , z ) , xform.transformZ( 0.0 , 0.0 , z ) ) };
			}
			else
			{
				pointIndices = new int[ detail ];

				final double stepSize = 2.0 * Math.PI / (double)detail;
				for ( int step = 0 ; step < detail ; step++ )
				{
					final double angle = (double)step * stepSize;
					final double x     =  Math.sin( angle ) * radius;
					final double y     = -Math.cos( angle ) * radius;

					pointIndices[ step ] = ( xform == null )
						? result.getOrAddPointIndex( x , y , z )
						: result.getOrAddPointIndex( xform.transformX( x , y , z ) , xform.transformY( x , y , z ) , xform.transformZ( x , y , z ) );
				}

				if ( closeEnds )
				{
					if ( i == 0 )
					{
						result.addFace( pointIndices , texture , false );
					}
					else if ( i == radii.length - 1 )
					{
						final int[] reversed = new int[ detail ];
						for ( int step = 0 ; step < detail ; step++ )
							reversed[ step ] = pointIndices[ detail - 1 - step ];

						result.addFace( reversed , texture , false );
					}
				}
			}

			/*
			 * Construct faces between this and the previous 'row'.
			 */
			if ( prevPointIndices != null )
			{
				if ( pointIndices.length > 1 )
				{
					if ( prevPointIndices.length > 1 )
					{
						for ( int step = 0 ; step < detail ; step++ )
						{
							final int nextStep = ( step + 1 ) % detail;
							result.addFace( new int[] { prevPointIndices[ step ] , pointIndices[ step ] , pointIndices[ nextStep ] , prevPointIndices[ nextStep ] } , texture , smoothCircumference );
						}
					}
					else
					{
						for ( int step = 0 ; step < detail ; step++ )
							result.addFace( new int[] { prevPointIndices[ 0 ] , pointIndices[ step ] , pointIndices[ ( step + 1 ) % detail ] } , texture , smoothCircumference );
					}
				}
				else if ( prevPointIndices.length > 1 )
				{
					for ( int step = 0 ; step < detail ; step++ )
						result.addFace( new int[] { prevPointIndices[ step ] , pointIndices[ 0 ] , prevPointIndices[ ( step + 1 ) % detail ] } , texture , smoothCircumference );
				}
			}
			prevPointIndices = pointIndices;
		}

		return result;
	}
}