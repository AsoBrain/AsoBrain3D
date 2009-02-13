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
	/** Left map index.   */ public static final int LEFT   = 0;
	/** Right map index.  */ public static final int RIGHT  = 1;
	/** Front map index.  */ public static final int FRONT  = 2;
	/** Back map index.   */ public static final int BACK   = 3;
	/** Bottom map index. */ public static final int BOTTOM = 4;
	/** Top map index.    */ public static final int TOP    = 5;

	/**
	 * Transforms box to world coordinates.
	 */
	private Matrix3D _box2wcs;

	/**
	 * UV maps used to map the sides.
	 */
	private UVMap[] _maps;

	/**
	 * Texture flipping for each map.
	 */
	private boolean[] _flips;

	/**
	 * Construct new UV-map that applies a uniform box mapping.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 */
	public BoxUVMap( final double modelUnits )
	{
		this( modelUnits , Matrix3D.INIT );
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
		this( modelUnits , Matrix3D.INIT.setTranslation( origin ) );
	}

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * specified box coordinate system.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   box2wcs   Transforms spatial to box coordinates.
	 */
	public BoxUVMap( final double modelUnits , final Matrix3D box2wcs )
	{
		this( modelUnits , box2wcs, false , false , false , false , false , false );
	}

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * specified box coordinate system.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   box2wcs     Transforms box to world coordinates.
	 * @param   flipLeft    Flip left texture direction.
	 * @param   flipRight   Flip right texture direction.
	 * @param   flipFront   Flip front texture direction.
	 * @param   flipBack    Flip right texture direction.
	 * @param   flipTop     Flip top texture direction.
	 * @param   flipBottom  Flip bottom texture direction.
	 */
	public BoxUVMap( final double modelUnits , final Matrix3D box2wcs , final boolean flipLeft , final boolean flipRight , final boolean flipFront , final boolean flipBack , final boolean flipTop , final boolean flipBottom )
	{
		final UVMap[] maps = new UVMap[ 6 ];
		final boolean[] flips = new boolean[ 6 ];

		maps[ LEFT ] = new PlanarUVMap( modelUnits , Matrix3D.INIT.set(
			-box2wcs.xy ,  box2wcs.xz , -box2wcs.xx , box2wcs.xo ,    // [  0 ,  0 , -1 ]
			-box2wcs.yy ,  box2wcs.yz , -box2wcs.yx , box2wcs.yo ,    // [ -1 ,  0 ,  0 ] * box2wcs
			-box2wcs.zy ,  box2wcs.zz , -box2wcs.zx , box2wcs.zo ) ); // [  0 ,  1 ,  0 ]

		flips[ LEFT ] = flipLeft;

		maps[ RIGHT ] = new PlanarUVMap( modelUnits , Matrix3D.INIT.set(
			 box2wcs.xy ,  box2wcs.xz ,  box2wcs.xx , box2wcs.xo ,    // [ 0 ,  0 ,  1 ]
			 box2wcs.yy ,  box2wcs.yz ,  box2wcs.yx , box2wcs.yo ,    // [ 1 ,  0 ,  0 ] * box2wcs
			 box2wcs.zy ,  box2wcs.zz ,  box2wcs.zx , box2wcs.zo ) ); // [ 0 ,  1 ,  0 ]

		flips[ RIGHT ] = flipRight;

		maps[ FRONT ] = new PlanarUVMap( modelUnits , Matrix3D.INIT.set(
			 box2wcs.xx ,  box2wcs.xz , -box2wcs.xy , box2wcs.xo ,    // [ 1 ,  0 ,  0 ]
			 box2wcs.yx ,  box2wcs.yz , -box2wcs.yy , box2wcs.yo ,    // [ 0 ,  0 , -1 ] * box2wcs
			 box2wcs.zx ,  box2wcs.zz , -box2wcs.zy , box2wcs.zo ) ); // [ 0 ,  1 ,  0 ]

		flips[ FRONT ] = flipFront;

		maps[ BACK ] = new PlanarUVMap( modelUnits , Matrix3D.INIT.set(
			-box2wcs.xx ,  box2wcs.xz ,  box2wcs.xy , box2wcs.xo ,    // [ -1 ,  0 ,  0 ]
			-box2wcs.yx ,  box2wcs.yz ,  box2wcs.yy , box2wcs.yo ,    // [  0 ,  0 ,  1 ] * box2wcs
			-box2wcs.zx ,  box2wcs.zz ,  box2wcs.zy , box2wcs.zo ) ); // [  0 ,  1 ,  0 ]

		flips[ BACK ] = flipBack;

		maps[ BOTTOM ] = new PlanarUVMap( modelUnits , Matrix3D.INIT.set(
			-box2wcs.xx ,  box2wcs.xy , -box2wcs.xz , box2wcs.xo ,    // [ -1 ,  0 ,  0 ]
			-box2wcs.yx ,  box2wcs.yy , -box2wcs.yz , box2wcs.yo ,    // [  0 ,  1 ,  0 ] * box2wcs
			-box2wcs.zx ,  box2wcs.zy , -box2wcs.zz , box2wcs.zo ) ); // [  0 ,  0 , -1 ]

		flips[ BOTTOM ] = flipBottom;

		maps[ TOP ] = new PlanarUVMap( modelUnits , box2wcs );

		flips[ TOP ] = flipTop;

		_box2wcs = box2wcs;
		_maps = maps;
		_flips = flips;
	}

	public Point2D.Float[] generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final boolean flipTexture )
	{
		final int map = getTargetMap( vertexCoordinates , vertexIndices );
		return _maps[ map ].generate( material , vertexCoordinates , vertexIndices , _flips[ map ] ^ flipTexture );
	}

	public Point2D.Float generate( final Material material , final Vector3D wcsPoint , final Vector3D normal , final boolean flipTexture )
	{
		final int map = getTargetMap( normal );
		return _maps[ map ].generate( material , wcsPoint , normal , _flips[ map ] ^ flipTexture );
	}

	/**
	 * Try to determine the face normal and use it to choose the target map.
	 *
	 * Note that this assumes that the given indices represent a single face.
	 *
	 * @param   vertexCoordinates   Vertex coordinates, as xyz-triplets.
	 * @param   vertexIndices       Indices for all vertices in the face.
	 *
	 * @return  Target map.
	 */
	public int getTargetMap( final double[] vertexCoordinates , final int[] vertexIndices )
	{
		final int result;

		if ( vertexIndices.length >= 3 )
		{
			final int base1 = vertexIndices[ 0 ] * 3;
			final int base2 = vertexIndices[ 1 ] * 3;
			final int base3 = vertexIndices[ 2 ] * 3;

			final double xo = vertexCoordinates[ base2 ];
			final double yo = vertexCoordinates[ base2 + 1 ];
			final double zo = vertexCoordinates[ base2 + 2 ];

			result = getTargetMap( Vector3D.cross(
				vertexCoordinates[ base1     ] - xo ,
				vertexCoordinates[ base1 + 1 ] - yo ,
				vertexCoordinates[ base1 + 2 ] - zo ,
				vertexCoordinates[ base3     ] - xo,
				vertexCoordinates[ base3 + 1 ] - yo,
				vertexCoordinates[ base3 + 2 ] - zo ) );
		}
		else
		{
			result = 0;
		}

		return result;
	}

	/**
	 * Get target map based on the specified normal vector. The normal vector is
	 * rotated using the
	 *
	 * @param   normal  Normal vector (does not need to be normalized).
	 *
	 * @return  Target map.
	 */
	private int getTargetMap( final Vector3D normal )
	{
		final Vector3D boxNormal = _box2wcs.inverseRotate( normal );

		final boolean negX = ( boxNormal.x < 0.0 );
		final boolean negY = ( boxNormal.y < 0.0 );
		final boolean negZ = ( boxNormal.z < 0.0 );
		final double  absX = negX ? -boxNormal.x : boxNormal.x;
		final double  absY = negY ? -boxNormal.y : boxNormal.y;
		final double  absZ = negZ ? -boxNormal.z : boxNormal.z;

		return ( absZ >= absX ) ? ( absZ >= absY ) ? negZ ? BOTTOM : TOP
		                                           : negY ? FRONT : BACK
		                        : ( absX >= absY ) ? negX ? LEFT : RIGHT
		                                           : negY ? FRONT : BACK;
	}
}
