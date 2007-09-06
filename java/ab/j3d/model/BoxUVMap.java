/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import ab.j3d.Material;
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
	private UVMap _sideMap;

	/**
	 * UV map used to map the front and rear.
	 */
	private UVMap _frontMap;

	/**
	 * UV map used to map the top and bottom.
	 */
	private UVMap _topMap;

	/**
	 * Construct new UV-map that applies a uniform box mapping with the
	 * UV-origin located at the given spatial coordinates.
	 *
	 * @param   modelUnits  Size of a model unit in meters.
	 * @param   origin      Location of the UV-origin in spatial coordinates.
	 */
	public BoxUVMap( final double modelUnits , final Vector3D origin )
	{
		_sideMap  = new PlanarUVMap( modelUnits , origin , Vector3D.INIT.set( 1.0 , 0.0 , 0.0 ) );
		_frontMap = new PlanarUVMap( modelUnits , origin , Vector3D.INIT.set( 0.0 , 1.0 , 0.0 ) );
		_topMap   = new PlanarUVMap( modelUnits , origin , Vector3D.INIT.set( 0.0 , 0.0 , 1.0 ) );
	}

	public void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final float[] textureU , final float[] textureV )
	{
		if ( vertexIndices.length < 3 )
		{
			_topMap.generate( material , vertexCoordinates , vertexIndices , textureU , textureV );
		}
		else
		{
			/*
			 * Determine the face normal. Note that this assumes that the given
			 * indices represent a single face.
			 */
			final int base1 = vertexIndices[ 0 ] * 3;
			final int base2 = vertexIndices[ 1 ] * 3;
			final int base3 = vertexIndices[ 2 ] * 3;

			final Vector3D normal = Vector3D.cross( vertexCoordinates[ base1     ] - vertexCoordinates[ base2     ] ,
			                                        vertexCoordinates[ base1 + 1 ] - vertexCoordinates[ base2 + 1 ] ,
			                                        vertexCoordinates[ base1 + 2 ] - vertexCoordinates[ base2 + 2 ] ,
			                                        vertexCoordinates[ base3     ] - vertexCoordinates[ base2     ] ,
			                                        vertexCoordinates[ base3 + 1 ] - vertexCoordinates[ base2 + 1 ] ,
			                                        vertexCoordinates[ base3 + 2 ] - vertexCoordinates[ base2 + 2 ] );

			/*
			 * Choose the appropriate planar map.
			 */
			final double threshold = 0.5 * Math.sqrt( 2.0 ) * normal.length();

			if ( Math.abs( normal.x ) >= threshold )
			{
				_sideMap.generate( material , vertexCoordinates , vertexIndices , textureU , textureV );
			}
			else if ( Math.abs( normal.y ) >= threshold )
			{
				_frontMap.generate( material , vertexCoordinates , vertexIndices , textureU , textureV );
			}
			else
			{
				_topMap.generate( material , vertexCoordinates , vertexIndices , textureU , textureV );
			}
		}
	}
}
