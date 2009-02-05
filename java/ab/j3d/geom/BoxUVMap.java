/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.geom;

import java.awt.geom.Point2D;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Defines a box UV-mapping.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class BoxUVMap
	implements UVMap
{
	/**
	 * UV map used to map the sides.
	 */
	private UVMap _leftMap;

	/**
	 * UV map used to map the sides.
	 */
	private UVMap _rightMap;

	/**
	 * UV map used to map the front and rear.
	 */
	private UVMap _frontMap;

	/**
	 * UV map used to map the front and rear.
	 */
	private UVMap _backMap;

	/**
	 * UV map used to map the top and bottom.
	 */
	private UVMap _topMap;

	/**
	 * UV map used to map the top and bottom.
	 */
	private UVMap _bottomMap;

	/**
	 * Rotation applied to the box.
	 */
	private Matrix3D _rotation;

	/**
	 * Construct new UV-map that applies a uniform box mapping.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 */
	public BoxUVMap( final double modelUnits )
	{
		this( modelUnits , Vector3D.INIT , Matrix3D.INIT );
	}

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * UV-origin located at the given spatial coordinates.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Location of the UV-origin in spatial coordinates.
	 */
	public BoxUVMap( final double modelUnits , final Vector3D origin )
	{
		this( modelUnits , origin , Matrix3D.INIT );
	}

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * UV-origin located at the given spatial coordinates.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Location of the UV-origin in spatial coordinates.
	 * @param   rotation    Rotation transform applied to the box.
	 */
	public BoxUVMap( final double modelUnits , final Vector3D origin , final Matrix3D rotation )
	{
		final Matrix3D translated = rotation.setTranslation( origin );

		_leftMap = new PlanarUVMap( modelUnits , translated.multiply(
			0.0 , -1.0 ,  0.0 , 0.0 ,
			0.0 ,  0.0 ,  1.0 , 0.0 ,
			0.0 ,  0.0 ,  0.0 , 0.0 ) );

		_rightMap = new PlanarUVMap( modelUnits , translated.multiply(
			0.0 , 1.0 , 0.0 , 0.0 ,
			0.0 , 0.0 , 1.0 , 0.0 ,
			0.0 , 0.0 , 0.0 , 0.0 ) );

		_frontMap = new PlanarUVMap( modelUnits , translated.multiply(
			1.0 , 0.0 , 0.0 , 0.0 ,
			0.0 , 0.0 , 1.0 , 0.0 ,
			0.0 , 0.0 , 0.0 , 0.0 ) );

		_backMap = new PlanarUVMap( modelUnits , translated.multiply(
			-1.0 , 0.0 , 0.0 , 0.0 ,
			 0.0 , 0.0 , 1.0 , 0.0 ,
			 0.0 , 0.0 , 0.0 , 0.0 ) );

		_topMap = new PlanarUVMap( modelUnits , translated );

		_bottomMap = new PlanarUVMap( modelUnits , translated.multiply(
			1.0 ,  0.0 , 0.0 , 0.0 ,
			0.0 , -1.0 , 0.0 , 0.0 ,
			0.0 ,  0.0 , 0.0 , 0.0 ) );

		_rotation = rotation;
	}

	public void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture , final float[] textureU , final float[] textureV )
	{
		final UVMap map;

		/*
		 * Try to determine the face normal and use it to choose the target map.
		 * Note that this assumes that the given indices represent a single face.
		 */
		if ( vertexIndices.length >= 3 )
		{
			final int base1 = vertexIndices[ 0 ] * 3;
			final int base2 = vertexIndices[ 1 ] * 3;
			final int base3 = vertexIndices[ 2 ] * 3;

			final double xo = vertexCoordinates[ base2 ];
			final double yo = vertexCoordinates[ base2 + 1 ];
			final double zo = vertexCoordinates[ base2 + 2 ];

			map = getTargetMap( Vector3D.cross(
				vertexCoordinates[ base1     ] - xo ,
				vertexCoordinates[ base1 + 1 ] - yo ,
				vertexCoordinates[ base1 + 2 ] - zo ,
				vertexCoordinates[ base3     ] - xo,
				vertexCoordinates[ base3 + 1 ] - yo,
				vertexCoordinates[ base3 + 2 ] - zo ) );
		}
		else
		{
			map = _topMap;
		}

		map.generate( material , vertexCoordinates , vertexIndices , flipTexture , textureU , textureV );
	}

	public Point2D.Float generate( final Material material , final Vector3D point , final Vector3D normal , final boolean flipTexture )
	{
		final UVMap map = getTargetMap( normal );
		return map.generate( material , point , normal , flipTexture );
	}

	/**
	 * Get target map based on the specified normal vector. The normal vector is
	 * rotated using the
	 *
	 * @param   normal  Normal vector (does not need to be normalized).
	 *
	 * @return  Target map.
	 */
	private UVMap getTargetMap( final Vector3D normal )
	{
		final Vector3D rotatedNormal = _rotation.inverseRotate( normal );

		final boolean negX = ( rotatedNormal.x < 0.0 );
		final boolean negY = ( rotatedNormal.y < 0.0 );
		final boolean negZ = ( rotatedNormal.z < 0.0 );
		final double  absX = negX ? -rotatedNormal.x : rotatedNormal.x;
		final double  absY = negY ? -rotatedNormal.y : rotatedNormal.y;
		final double  absZ = negZ ? -rotatedNormal.z : rotatedNormal.z;

		return ( absZ >= absX ) ? ( absZ >= absY ) ? negZ ? _bottomMap : _topMap
		                                           : negY ? _frontMap : _backMap
		                        : ( absX >= absY ) ? negX ? _leftMap : _rightMap
		                                           : negY ? _frontMap : _backMap;
	}
}
