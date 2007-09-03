/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

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

	public void addLine( final Vector3D point1 , final Vector3D point2 , final int stroke , final Material material )
	{
		_target.addFace( new Vector3D[] { point1 , point2 } , material , false , true );
	}

	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Material material , final boolean hasBackface )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 } , material , false , hasBackface );
	}

	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final Material material , final boolean hasBackface )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 , point4 } , material , false , hasBackface );
	}

	public void addCircle( final Vector3D centerPoint , final double radius , final Vector3D normal , final Vector3D extrusion , final int stroke , final Material material , final boolean fill )
	{
		final Matrix3D  base      = Matrix3D.getPlaneTransform( centerPoint , normal , true );
		final Ellipse2D ellipse2d = new Ellipse2D.Double( -radius , -radius , radius * 2.0 , radius * 2.0 );

		ExtrudedObject2D.generate( _target , ellipse2d , extrusion , base , material , radius * 0.02 , true );
	}

	public void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final Material material )
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
	 * @param   material            Material to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 *
	 * @return  Generated {@link Object3D}.
	 */
	public static Object3D constructRotatedObject( final Matrix3D xform , final double[] radii , final double[] zCoordinates , final int detail , final Material material , final boolean smoothCircumference , final boolean closeEnds )
	{
		final Object3D result = new Object3D();
		int[] prevVertexIndices = null;

		for ( int i = 0 ; i < radii.length ; i++ )
		{
			final double radius = radii[ i ];
			final double z      = zCoordinates[ i ];

			/*
			 * Based on 'radius', create a list of vertex indices at this point.
			 */
			final int[] vertexIndices;
			if ( MathTools.almostEqual( radius , 0.0 ) )
			{
				vertexIndices = new int[] { ( xform == null )
					? result.getVertexIndex( 0.0 , 0.0 , z )
					: result.getVertexIndex( xform.transformX( 0.0 , 0.0 , z ) , xform.transformY( 0.0 , 0.0 , z ) , xform.transformZ( 0.0 , 0.0 , z ) ) };
			}
			else
			{
				vertexIndices = new int[ detail ];

				final double stepSize = 2.0 * Math.PI / (double)detail;
				for ( int step = 0 ; step < detail ; step++ )
				{
					final double angle = (double)step * stepSize;
					final double x     =  Math.sin( angle ) * radius;
					final double y     = -Math.cos( angle ) * radius;

					vertexIndices[ step ] = ( xform == null )
						? result.getVertexIndex( x , y , z )
						: result.getVertexIndex( xform.transformX( x , y , z ) , xform.transformY( x , y , z ) , xform.transformZ( x , y , z ) );
				}

				if ( closeEnds )
				{
					if ( i == 0 )
					{
						result.addFace( vertexIndices , material , false );
					}
					else if ( i == radii.length - 1 )
					{
						final int[] reversed = new int[ detail ];
						for ( int step = 0 ; step < detail ; step++ )
							reversed[ step ] = vertexIndices[ detail - 1 - step ];

						result.addFace( reversed , material , false );
					}
				}
			}

			/*
			 * Construct faces between this and the previous 'row'.
			 */
			if ( prevVertexIndices != null )
			{
				if ( vertexIndices.length > 1 )
				{
					if ( prevVertexIndices.length > 1 )
					{
						for ( int step = 0 ; step < detail ; step++ )
						{
							final int nextStep = ( step + 1 ) % detail;
							result.addFace( new int[] { prevVertexIndices[ step ] , vertexIndices[ step ] , vertexIndices[ nextStep ] , prevVertexIndices[ nextStep ] } , material , smoothCircumference );
						}
					}
					else
					{
						for ( int step = 0 ; step < detail ; step++ )
							result.addFace( new int[] { prevVertexIndices[ 0 ] , vertexIndices[ step ] , vertexIndices[ ( step + 1 ) % detail ] } , material , smoothCircumference );
					}
				}
				else if ( prevVertexIndices.length > 1 )
				{
					for ( int step = 0 ; step < detail ; step++ )
						result.addFace( new int[] { prevVertexIndices[ step ] , vertexIndices[ 0 ] , prevVertexIndices[ ( step + 1 ) % detail ] } , material , smoothCircumference );
				}
			}
			prevVertexIndices = vertexIndices;
		}

		return result;
	}
}
