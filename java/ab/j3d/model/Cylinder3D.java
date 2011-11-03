/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a bound 3D cylinder.
 * <p>
 * The cylinder has its base at the local origin, has a given constant radius,
 * and extends upto a given height along the positive Z-axis.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Cylinder3D
	extends Object3D
{
	/**
	 * Height of cylinder (z-axis).
	 */
	public final double height;

	/**
	 * Radius at bottom (z=0).
	 */
	public final double radius;

	/**
	 * Number of edges to approximate circle.
	 */
	private int _numEdges;

	/**
	 * Appearance of cylinder circumference.
	 */
	private Appearance _sideAppearance;

	/**
	 * UV map to use for circumference.
	 */
	private UVMap _sideMap;

	/**
	 * Apply smoothing to circumference of cylinder.
	 */
	private boolean _smoothCircumference;

	/**
	 * Appearance for top cap (<code>null</code> => no cap).
	 */
	private Appearance _topAppearance;

	/**
	 * UV map for top cap.
	 */
	private UVMap _topMap;

	/**
	 * Appearance for bottom cap (<code>null</code> => no cap).
	 */
	private Appearance _bottomAppearance;

	/**
	 * UV map for bottom cap.
	 */
	private UVMap _bottomMap;

	/**
	 * If set, flip normals.
	 */
	private boolean _flipNormals;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
	 *
	 * @param   height                  Height of cylinder (z-axis).
	 * @param   radius                  Radius.
	 * @param   numEdges                Number of edges to approximate circle (minimum: 3).
	 * @param   sideAppearance          Appearance of cylinder circumference.
	 * @param   sideMap                 UV map to use for circumference.
	 * @param   smoothCircumference     Apply smoothing to circumference of cylinder.
	 * @param   topAppearance           Appearance for top cap (<code>null</code> => no cap).
	 * @param   topMap                  UV map for top cap.
	 * @param   bottomAppearance        Appearance for bottom cap (<code>null</code> => no cap).
	 * @param   bottomMap               UV map for bottom cap.
	 * @param   flipNormals             If set, flip normals.
	 */
	public Cylinder3D( final double height, final double radius, final int numEdges, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		if ( ( radius <= 0.0 ) || ( height <= 0.0 ) || ( numEdges < 3 ) )
		{
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor" );
		}

		this.height = height;
		this.radius = radius;
		_numEdges = numEdges;
		_sideAppearance = sideAppearance;
		_sideMap = sideMap;
		_smoothCircumference = smoothCircumference;
		_topAppearance = topAppearance;
		_topMap = topMap;
		_bottomAppearance = bottomAppearance;
		_bottomMap = bottomMap;
		_flipNormals = flipNormals;

		/*
		 * Setup properties of cylinder.
		 */
		final int vertexCount = numEdges + numEdges;
		final Vector3D[] vertexCoordinatesArray = new Vector3D[ vertexCount ];
		final List<Vector3D> vertexCoordinates = Arrays.asList( vertexCoordinatesArray );

		final double radStep = 2.0 * Math.PI / (double)numEdges;

		/*
		 * Generate vertices.
		 */
		final int topCoordinatesOffset = numEdges;

		final Vector3D[] vertexNormals = new Vector3D[ numEdges ];

		for ( int i = 0 ; i < numEdges ; i++ )
		{
			final double angle = (double)i * radStep;
			final double normalX = Math.sin( angle );
			final double normalY = -Math.cos( angle );
			final double x = radius * normalX;
			final double y = radius * normalY;

			vertexCoordinatesArray[ i ] = new Vector3D( x, y, 0.0 );
			vertexCoordinatesArray[ topCoordinatesOffset + i ] = new Vector3D( x, y, height );
			vertexNormals[ i ] = flipNormals ? new Vector3D( -normalX, -normalY, 0.0 ) : new Vector3D( normalX, normalY, 0.0 );
		}

		setVertexCoordinates( vertexCoordinates );

		/*
		 * Bottom face (if it exists).
		 */
		if ( bottomAppearance != null )
		{
			final int[] vertexIndices = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( numEdges - 1 - i ) : i;
			}

			final float[] texturePoints = ( bottomMap != null ) ? bottomMap.generate( bottomAppearance.getColorMap(), vertexCoordinates, vertexIndices, false ) : null;
			final FaceGroup faceGroup = getFaceGroup( bottomAppearance, false, false );
			faceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, null ) );
		}

		/*
		 * Circumference.
		 */
		final FaceGroup sideFaceGroup = getFaceGroup( sideAppearance, smoothCircumference, false );
		for ( int i1 = 0 ; i1 < numEdges ; i1++ )
		{
			final int   i2 = ( i1 + 1 ) % numEdges;

			final int[] vertexIndices = flipNormals ? new int[] { numEdges + i2, numEdges + i1, i1, i2 } : new int[] { i2, i1, numEdges + i1, numEdges + i2 };
			final Vector3D[] faceVertexNormals = new Vector3D[] { vertexNormals[ i2 ], vertexNormals[ i1 ], vertexNormals[ i1 ], vertexNormals[ i2 ] };

			final TextureMap colorMap = ( sideAppearance == null ) ? null : sideAppearance.getColorMap();
			final float[] texturePoints = ( sideMap != null ) ? sideMap.generate( colorMap, vertexCoordinates, vertexIndices, false ) : null;
			sideFaceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, faceVertexNormals ) );
		}

		/*
		 * Top face (if it exists).
		 */
		if ( topAppearance != null )
		{
			final int[] vertexIndices = new int[ numEdges ];

			final int lastVertex = vertexCount - 1;

			for ( int i = 0 ; i < numEdges ; i++ )
			{
				vertexIndices[ i ] = flipNormals ? ( lastVertex - numEdges + 1 + i ) : ( lastVertex - i );
			}

			final float[] texturePoints = ( topMap != null ) ? topMap.generate( topAppearance.getColorMap(), vertexCoordinates, vertexIndices, false ) : null;
			final FaceGroup faceGroup = getFaceGroup( topAppearance, false, false );
			faceGroup.addFace( new Face3D( this, vertexIndices, texturePoints, null ) );
		}
	}

	/**
	 * Get height of cylinder.
	 *
	 * @return  Height of cylinder.
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * Get radius of cylinder.
	 *
	 * @return  Radius of cylinder.
	 */
	public double getRadius()
	{
		return radius;
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final double radius = this.radius;
		return new Bounds3D( -radius, -radius, 0.0, radius, radius, height );
	}

	@Override
	public boolean collidesWith( final Matrix3D fromOtherToThis, final Object3D other )
	{
		final boolean result;

		if ( other instanceof Sphere3D )
		{
			final Sphere3D sphere = (Sphere3D)other;
			final double x = fromOtherToThis.transformX( 0.0, 0.0, 0.0 );
			final double y = fromOtherToThis.transformY( 0.0, 0.0, 0.0 );
			final double z = fromOtherToThis.transformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereCylinderIntersection( x, y, z, sphere._radius, height, radius );
		}
		else
		{
			result = super.collidesWith( fromOtherToThis, other );
		}

		return result;
	}

	@Override
	public boolean equals( final Object other )
	{
		final boolean result;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof Cylinder3D )
		{
			final Cylinder3D cylinder = (Cylinder3D)other;
			result = ( height               == cylinder.height               ) &&
			         ( radius               == cylinder.radius               ) &&
			         ( _numEdges            == cylinder._numEdges            ) &&
			         ( ( _sideAppearance    == null ) ? ( cylinder._sideAppearance   == null ) : _sideAppearance  .equals( cylinder._sideAppearance   ) ) &&
			         ( ( _topAppearance     == null ) ? ( cylinder._topAppearance    == null ) : _topAppearance   .equals( cylinder._topAppearance    ) ) &&
			         ( ( _bottomAppearance  == null ) ? ( cylinder._bottomAppearance == null ) : _bottomAppearance.equals( cylinder._bottomAppearance ) ) &&
			         ( ( _sideMap           == null ) ? ( cylinder._sideMap          == null ) : _sideMap         .equals( cylinder._sideMap          ) ) &&
			         ( ( _topMap            == null ) ? ( cylinder._topMap           == null ) : _topMap          .equals( cylinder._topMap           ) ) &&
			         ( ( _bottomMap         == null ) ? ( cylinder._bottomMap        == null ) : _bottomMap       .equals( cylinder._bottomMap        ) ) &&
			         ( _smoothCircumference == cylinder._smoothCircumference ) &&
			         ( _flipNormals         == cylinder._flipNormals         );
		}
		else
		{
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return MathTools.hashCode( height ) ^
		       MathTools.hashCode( radius ) ^
		       _numEdges ^
		       ( ( _sideAppearance != null ) ? _sideAppearance.hashCode() : 0 ) ^
		       ( ( _topAppearance != null ) ? _topAppearance.hashCode() : 0 ) ^
		       ( ( _bottomAppearance != null ) ? _bottomAppearance.hashCode() : 0 );
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{tag=" + getTag() + ", height=" + height + ", radius=" + radius + '}';
	}
}
