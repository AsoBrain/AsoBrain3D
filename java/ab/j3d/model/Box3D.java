/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2007 Peter S. Heijnen
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
import ab.j3d.Matrix3D;

/**
 * This class defines a 3D box.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Box3D
	extends Object3D
{
	/** Vertices for front face.  */ private static final int[] FRONT_VERTICES  = { 0 , 4 , 5 , 1 };
	/** Vertices for rear face.   */ private static final int[] REAR_VERTICES   = { 2 , 6 , 7 , 3 };
	/** Vertices for right face.  */ private static final int[] RIGHT_VERTICES  = { 1 , 5 , 6 , 2 };
	/** Vertices for left face.   */ private static final int[] LEFT_VERTICES   = { 3 , 7 , 4 , 0 };
	/** Vertices for top face.    */ private static final int[] TOP_VERTICES    = { 4 , 7 , 6 , 5 };
	/** Vertices for bottom face. */ private static final int[] BOTTOM_VERTICES = { 1 , 2 , 3 , 0 };

	/**
	 * Transformation applied to all vertices of the box.
	 */
	private final Matrix3D _xform;

	/**
	 * Width of box (x-axis).
	 */
	private final double _dx;

	/**
	 * Height of box (y-axis).
	 */
	private final double _dy;

	/**
	 * Depth of box (z-axis).
	 */
	private final double _dz;

	/**
	 * Set box properties.
	 *
	 * @param   xform           Transformation to apply to all vertices of the box.
	 * @param   dx              Width of box (x-axis).
	 * @param   dy              Height of box (y-axis).
	 * @param   dz              Depth of box (z-axis).
	 * @param   mainMaterial    Main material of box.
	 * @param   sideMaterial    Material for sides of box.
	 */
	public Box3D( final Matrix3D xform , final double dx , final double dy , final double dz , final Material mainMaterial , final Material sideMaterial )
	{
		_xform = xform;
		_dx    = dx;
		_dy    = dy;
		_dz    = dz;

		/*
		 * Create vertex coordinates.
		 */
		final double[] vertexCoordinates =
		{
			0.0 , 0.0 , 0.0 , // 0
			_dx , 0.0 , 0.0 , // 1
			_dx , _dy , 0.0 , // 2
			0.0 , _dy , 0.0 , // 3
			0.0 , 0.0 , _dz , // 4
			_dx , 0.0 , _dz , // 5
			_dx , _dy , _dz , // 6
			0.0 , _dy , _dz   // 7
		};

		setVertexCoordinates( xform.transform( vertexCoordinates , vertexCoordinates , 8 ) );

		/*
		 * Determine flat side.
		 */
		final double ax = Math.abs( dx );
		final double ay = Math.abs( dy );
		final double az = Math.abs( dz );

		final boolean flatY = ( ay < ax && ay < az );
		final boolean flatX = ( ax < ay && ax < az );
		final boolean flatZ = ( az < ax && az < ay );

		addFaces( FRONT_VERTICES , REAR_VERTICES   , flatY ? mainMaterial : sideMaterial ,  flatZ , xform.xo , xform.xo + _dx , xform.zo , xform.zo + _dz );
		addFaces( RIGHT_VERTICES , LEFT_VERTICES   , flatX ? mainMaterial : sideMaterial ,  flatZ , xform.yo , xform.yo + _dy , xform.zo , xform.zo + _dz );
		addFaces( TOP_VERTICES   , BOTTOM_VERTICES , flatZ ? mainMaterial : sideMaterial , !flatX , xform.xo , xform.xo + _dx , xform.yo , xform.yo + _dy );
	}

	/**
	 * Helper method to add two opposite faces.
	 *
	 * @param   frontVertices   Vertices that define the 'front' face.
	 * @param   rearVertices    Vertices that define the 'rear' face.
	 * @param   material        Material to use for faces.
	 * @param   flipUV          <code>true</code> te reverse meaning of u and v.
	 * @param   modelHor1       Start horizontal model coordinate.
	 * @param   modelHor2       End horizontal model coordinate.
	 * @param   modelVer1       Start vertical model coordinate.
	 * @param   modelVer2       End vertical model coordinate.
	 */
	private void addFaces( final int[] frontVertices , final int[] rearVertices , final Material material , final boolean flipUV , final double modelHor1 , final double modelHor2 , final double modelVer1 , final double modelVer2 )
	{
		if ( ( material != null ) && ( material.colorMap != null ) )
		{
			final double modelUnit = 0.001;

			double min = ( flipUV ? modelVer1 : modelHor1 );
			double max = ( flipUV ? modelVer2 : modelHor2 );

			double adjustment = material.colorMapWidth;
			if ( adjustment > 0.0 )
			{
				adjustment *= modelUnit;
				min /= adjustment;
				max /= adjustment;
			}

			adjustment = Math.min( Math.floor( min ) , Math.floor( max ) );

			final float u1 = (float)( min - adjustment );
			final float u2 = (float)( max - adjustment );

			min = ( flipUV ? modelHor1 : modelVer1 );
			max = ( flipUV ? modelHor2 : modelVer2 );

			adjustment = material.colorMapHeight;
			if ( adjustment > 0.0 )
			{
				adjustment *= modelUnit;
				min /= adjustment;
				max /= adjustment;
			}

			adjustment = Math.min( Math.floor( min ) , Math.floor( max ) );

			final float v1 = (float)( min - adjustment );
			final float v2 = (float)( max - adjustment );

			final float[] frontU = new float[] { u1 , u1 , u2 , u2 };
			final float[] frontV = new float[] { v1 , v2 , v2 , v1 };

			final float[] rearU  = new float[] { u2 , u2 , u1 , u1 };
			final float[] rearV  = frontV;

			addFace( frontVertices , material , frontU , frontV , 1.0f , false , false );
			addFace( rearVertices  , material , rearU  , rearV  , 1.0f , false , false );
		}
		else
		{
			addFace( frontVertices , material , null , null , 1.0f , false , false );
			addFace( rearVertices  , material , null , null , 1.0f , false , false );
		}

	}

	/**
	 * Get width of box (x-axis).
	 *
	 * @return  Width of box (x-axis).
	 */
	public double getDX()
	{
		return _dx;
	}

	/**
	 * Get height of box (y-axis).
	 *
	 * @return  Height of box (y-axis).
	 */
	public double getDY()
	{
		return _dy;
	}

	/**
	 * Get depth of box (z-axis).
	 *
	 * @return  Depth of box (z-axis).
	 */
	public double getDZ()
	{
		return _dz;
	}

	/**
	 * Get transformation applied to all vertices of the box.
	 *
	 * @return  Transformation applied to all vertices of the box.
	 */
	public Matrix3D getTransform()
	{
		return _xform;
	}
}
