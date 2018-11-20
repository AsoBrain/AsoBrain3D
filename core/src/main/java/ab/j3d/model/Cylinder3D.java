/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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
 */
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class defines a bound 3D cylinder.
 *
 * <p>The cylinder has its base at the local origin, has a given constant
 * radius, and extends up to a given height along the positive Z-axis.
 *
 * @author Peter S. Heijnen
 */
public class Cylinder3D
extends Object3D
{
	/**
	 * Height of cylinder (z-axis).
	 */
	private final double _height;

	/**
	 * Radius at bottom (z=0).
	 */
	private final double _radius;

	/**
	 * Number of edges to approximate circle.
	 */
	private final int _numEdges;

	/**
	 * Appearance of cylinder circumference.
	 */
	private final Appearance _sideAppearance;

	/**
	 * UV map to use for circumference.
	 */
	private final UVMap _sideMap;

	/**
	 * Apply smoothing to circumference of cylinder.
	 */
	private final boolean _smoothCircumference;

	/**
	 * Appearance for top cap ({@code null} => no cap).
	 */
	private final Appearance _topAppearance;

	/**
	 * UV map for top cap.
	 */
	private final UVMap _topMap;

	/**
	 * Appearance for bottom cap ({@code null} => no cap).
	 */
	private final Appearance _bottomAppearance;

	/**
	 * UV map for bottom cap.
	 */
	private final UVMap _bottomMap;

	/**
	 * If set, flip normals.
	 */
	private final boolean _flipNormals;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to
	 * create a cone.
	 *
	 * @param height              Height of cylinder (z-axis).
	 * @param radius              Radius.
	 * @param numEdges            Number of edges to approximate circle (minimum:
	 *                            3).
	 * @param sideAppearance      Appearance of cylinder circumference.
	 * @param sideMap             UV map to use for circumference.
	 * @param smoothCircumference Apply smoothing to circumference of cylinder.
	 * @param topAppearance       Appearance for top cap ({@code null} => no cap).
	 * @param topMap              UV map for top cap.
	 * @param bottomAppearance    Appearance for bottom cap ({@code null} => no
	 *                            cap).
	 * @param bottomMap           UV map for bottom cap.
	 * @param flipNormals         If set, flip normals.
	 */
	public Cylinder3D( final double height, final double radius, final int numEdges, @Nullable final Appearance sideAppearance, @Nullable final UVMap sideMap, final boolean smoothCircumference, @Nullable final Appearance topAppearance, @Nullable final UVMap topMap, @Nullable final Appearance bottomAppearance, @Nullable final UVMap bottomMap, final boolean flipNormals )
	{
		if ( height < 0.0 )
		{
			throw new IllegalArgumentException( "height: " + height );
		}

		if ( radius <= 0.0 )
		{
			throw new IllegalArgumentException( "radius: " + radius );
		}

		if ( numEdges < 3 )
		{
			throw new IllegalArgumentException( "numEdges: " + numEdges );
		}

		_height = height;
		_radius = radius;
		_numEdges = numEdges;
		_sideAppearance = sideAppearance;
		_sideMap = sideMap;
		_smoothCircumference = smoothCircumference;
		_topAppearance = topAppearance;
		_topMap = topMap;
		_bottomAppearance = bottomAppearance;
		_bottomMap = bottomMap;
		_flipNormals = flipNormals;

		if ( height != 0.0 )
		{
			/*
			 * Generate vertices and normals.
			 */
			final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>( numEdges + numEdges );
			final List<Vector3D> vertexNormals = new ArrayList<Vector3D>( numEdges );
			final List<Vector3D> faceNormals = new ArrayList<Vector3D>( numEdges );

			final double radStep = ( flipNormals ? -2.0 : 2.0 ) * Math.PI / (double)numEdges;

			for ( int i = 0; i < numEdges; i++ )
			{
				final double vertexAngle = (double)i * radStep;
				final double vertexNormalX = Math.sin( vertexAngle );
				final double vertexNormalY = Math.cos( vertexAngle );

				final double faceAngle = ( (double)i + 0.5 ) * radStep;
				final double faceNormalX = Math.sin( faceAngle );
				final double faceNormalY = Math.cos( faceAngle );

				vertexCoordinates.add( new Vector3D( radius * vertexNormalX, radius * vertexNormalY, 0.0 ) );
				vertexNormals.add( flipNormals ? new Vector3D( -vertexNormalX, -vertexNormalY, 0.0 ) : new Vector3D( vertexNormalX, vertexNormalY, 0.0 ) );
				faceNormals.add( flipNormals ? new Vector3D( -faceNormalX, -faceNormalY, 0.0 ) : new Vector3D( faceNormalX, faceNormalY, 0.0 ) );
			}

			for ( int i = 0; i < numEdges; i++ )
			{
				final Vector3D point = vertexCoordinates.get( i );
				vertexCoordinates.add( new Vector3D( point.x, point.y, height ) );
			}

			setVertexCoordinates( vertexCoordinates );

			/*
			 * Bottom face (if it exists).
			 */
			if ( bottomAppearance != null || ( ( sideAppearance == null ) && ( topAppearance == null ) ) )
			{
				final FaceGroup faceGroup = getFaceGroup( bottomAppearance, false, false );
				final Vector3D faceNormal = flipNormals ? Vector3D.POSITIVE_Z_AXIS : Vector3D.NEGATIVE_Z_AXIS;
				final List<Vertex3D> faceVertices = new ArrayList<Vertex3D>( numEdges );
				final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( bottomAppearance, bottomMap, faceNormal, false );

				final int[] outline = new int[ numEdges + 1 ];
				final int[] fanVertices = new int[ numEdges ];

				for ( int i = 0 ; i < numEdges; i++ )
				{
					outline[ i ] = i;
					fanVertices[ i ] = i;

					final Vector3D point = vertexCoordinates.get( i );
					uvGenerator.generate( point );
					faceVertices.add( new Vertex3D( point, faceNormal, i, uvGenerator.getU(), uvGenerator.getV() ) );
				}

				final Tessellation tessellation = new Tessellation( Collections.singletonList( outline ), Collections.<TessellationPrimitive>singletonList( new TriangleFan( fanVertices ) ) );

				faceGroup.addFace( new Face3D( faceNormal, 0.0, faceVertices, tessellation ) );
			}

			/*
			 * Circumference.
			 */
			final FaceGroup sideFaceGroup = getFaceGroup( sideAppearance, smoothCircumference, ( topAppearance == null ) || ( bottomAppearance == null ) );
			final Tessellation sideTessellation = new Tessellation( Collections.singletonList( new int[] { 0, 1, 2, 3, 0 } ), Collections.<TessellationPrimitive>singletonList( new QuadList( new int[] { 0, 1, 2, 3 } ) ) );

			for ( int i1 = 0; i1 < numEdges; i1++ )
			{
				final Vector3D faceNormal = faceNormals.get( i1 );

				final int vertexIndex0 = ( i1 + 1 ) % numEdges;
				final int vertexIndex1 = i1;
				final int vertexIndex2 = numEdges + vertexIndex1;
				final int vertexIndex3 = numEdges + vertexIndex0;

				final List<Vertex3D> faceVertices = new ArrayList<Vertex3D>( 4 );
				final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( sideAppearance, sideMap, faceNormal, false );

				Vector3D point = vertexCoordinates.get( vertexIndex0 );
				uvGenerator.generate( point );
				faceVertices.add( new Vertex3D( point, vertexNormals.get( vertexIndex0 ), vertexIndex0, uvGenerator.getU(), uvGenerator.getV() ) );

				point = vertexCoordinates.get( vertexIndex1 );
				uvGenerator.generate( point );
				faceVertices.add( new Vertex3D( point, vertexNormals.get( vertexIndex1 ), vertexIndex1, uvGenerator.getU(), uvGenerator.getV() ) );

				point = vertexCoordinates.get( vertexIndex2 );
				uvGenerator.generate( point );
				faceVertices.add( new Vertex3D( point, vertexNormals.get( vertexIndex1 ), vertexIndex2, uvGenerator.getU(), uvGenerator.getV() ) );

				point = vertexCoordinates.get( vertexIndex3 );
				uvGenerator.generate( point );
				faceVertices.add( new Vertex3D( point, vertexNormals.get( vertexIndex0 ), vertexIndex3, uvGenerator.getU(), uvGenerator.getV() ) );

				sideFaceGroup.addFace( new Face3D( faceNormal, faceVertices, sideTessellation ) );
			}

			/*
			 * Top face (if it exists).
			 */
			if ( topAppearance != null || ( ( sideAppearance == null ) && ( bottomAppearance == null ) ) )
			{
				final FaceGroup faceGroup = getFaceGroup( topAppearance, false, false );
				final Vector3D faceNormal = flipNormals ? Vector3D.NEGATIVE_Z_AXIS : Vector3D.POSITIVE_Z_AXIS;
				final List<Vertex3D> faceVertices = new ArrayList<Vertex3D>( numEdges );
				final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( topAppearance, topMap, faceNormal, false );

				final int[] outline = new int[ numEdges + 1 ];
				final int[] fanVertices = new int[ numEdges ];
				final int lastVertex = numEdges + numEdges - 1;

				for ( int i = 0 ; i < numEdges; i++ )
				{
					final int vertexCoordinateIndex = lastVertex - i;

					outline[ i ] = i;
					fanVertices[ i ] = i;

					final Vector3D point = vertexCoordinates.get( vertexCoordinateIndex );
					uvGenerator.generate( point );
					faceVertices.add( new Vertex3D( point, faceNormal, vertexCoordinateIndex, uvGenerator.getU(), uvGenerator.getV() ) );
				}

				outline[ numEdges ] = outline [ 0 ]; // close outline
				final Tessellation tessellation = new Tessellation( Collections.singletonList( outline ), Collections.<TessellationPrimitive>singletonList( new TriangleFan( fanVertices ) ) );

				faceGroup.addFace( new Face3D( faceNormal, flipNormals ? -height : height, faceVertices, tessellation ) );
			}
		}
		else // create disc
		{
			final List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>( numEdges );
			final double radStep = ( flipNormals ? -2.0 : 2.0 ) * Math.PI / (double)numEdges;

			for ( int i = 0; i < numEdges; i++ )
			{
				final double angle = (double)i * radStep;
				final double normalX = Math.sin( angle );
				final double normalY = -Math.cos( angle );

				vertexCoordinates.add( new Vector3D( radius * normalX, radius * normalY, 0.0 ) );
			}

			setVertexCoordinates( vertexCoordinates );

			final FaceGroup faceGroup = getFaceGroup( bottomAppearance, false, true );
			final Vector3D faceNormal = flipNormals ? Vector3D.NEGATIVE_Z_AXIS : Vector3D.POSITIVE_Z_AXIS;
			final List<Vertex3D> faceVertices = new ArrayList<Vertex3D>( numEdges );
			final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( bottomAppearance, bottomMap, faceNormal, false );
			final int[] outline = new int[ numEdges + 1 ];
			final int[] fanVertices = new int[ numEdges ];

			for ( int i = 0 ; i < numEdges; i++ )
			{
				outline[ i ] = i;
				fanVertices[ i ] = i;

				final Vector3D point = vertexCoordinates.get( i );
				uvGenerator.generate( point );
				faceVertices.add( new Vertex3D( point, faceNormal, i, uvGenerator.getU(), uvGenerator.getV() ) );
			}

			final Tessellation tessellation = new Tessellation( Collections.singletonList( outline ), Collections.<TessellationPrimitive>singletonList( new TriangleFan( fanVertices ) ) );

			faceGroup.addFace( new Face3D( faceNormal, 0.0, faceVertices, tessellation ) );
		}
	}

	/**
	 * Get height of cylinder.
	 *
	 * @return Height of cylinder.
	 */
	public double getHeight()
	{
		return _height;
	}

	/**
	 * Get radius of cylinder.
	 *
	 * @return Radius of cylinder.
	 */
	public double getRadius()
	{
		return _radius;
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final double radius = getRadius();
		return new Bounds3D( -radius, -radius, 0.0, radius, radius, getHeight() );
	}

	@Override
	public boolean collidesWith( @NotNull final Matrix3D fromOtherToThis, @NotNull final Object3D other )
	{
		final boolean result;

		if ( other instanceof Sphere3D )
		{
			final Sphere3D sphere = (Sphere3D)other;
			final double x = fromOtherToThis.transformX( 0.0, 0.0, 0.0 );
			final double y = fromOtherToThis.transformY( 0.0, 0.0, 0.0 );
			final double z = fromOtherToThis.transformZ( 0.0, 0.0, 0.0 );
			result = GeometryTools.testSphereCylinderIntersection( x, y, z, sphere.getRadius(), _height, _radius );
		}
		else
		{
			boolean containsPoint = false;

			/*
			 * Test if the cylinder contains the other object. Theoretically
			 * checking a single vertex should be sufficient, but the
			 * containment check includes points (almost) on the boundary of the
			 * cylinder, so checking only a single vertex would lead to false
			 * positives.
			 */
			final List<Vector3D> vertexCoordinates = getVertexCoordinates();
			final List<Vector3D> otherVertexCoordinates = other.getVertexCoordinates();
			if ( !vertexCoordinates.isEmpty() && !otherVertexCoordinates.isEmpty() )
			{
				/*
				 * Only solid cylinders can contain anything.
				 */
				if ( ( _topAppearance != null ) && ( _bottomAppearance != null ) )
				{
					containsPoint = true;
					for ( final Vector3D point : otherVertexCoordinates )
					{
						final double x = fromOtherToThis.transformX( point );
						final double y = fromOtherToThis.transformY( point );
						final double z = fromOtherToThis.transformZ( point );
						if ( !GeometryTools.testCylinderContainsPoint( x, y, z, _height, _radius ) )
						{
							containsPoint = false;
							break;
						}
					}
				}

				/*
				 * If the other object is also a cylinder, test if it contains
				 * this cylinder.
				 */
				if ( !containsPoint && other instanceof Cylinder3D )
				{
					final Cylinder3D otherCylinder = (Cylinder3D)other;

					/*
					 * Only solid cylinders can contain anything.
					 */
					if ( otherCylinder._topAppearance != null && otherCylinder._bottomAppearance != null )
					{
						containsPoint = true;
						for ( final Vector3D point : vertexCoordinates )
						{
							final double x = fromOtherToThis.inverse().transformX( point );
							final double y = fromOtherToThis.inverse().transformY( point );
							final double z = fromOtherToThis.inverse().transformZ( point );
							if ( !GeometryTools.testCylinderContainsPoint( x, y, z, otherCylinder._height, otherCylinder._radius ) )
							{
								containsPoint = false;
								break;
							}
						}
					}
				}
			}

			result = containsPoint || super.collidesWith( fromOtherToThis, other );
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
			result = ( _height == cylinder._height ) &&
			         ( _radius == cylinder._radius ) &&
			         ( _numEdges == cylinder._numEdges ) &&
			         ( ( _sideAppearance == null ) ? ( cylinder._sideAppearance == null ) : _sideAppearance.equals( cylinder._sideAppearance ) ) &&
			         ( ( _topAppearance == null ) ? ( cylinder._topAppearance == null ) : _topAppearance.equals( cylinder._topAppearance ) ) &&
			         ( ( _bottomAppearance == null ) ? ( cylinder._bottomAppearance == null ) : _bottomAppearance.equals( cylinder._bottomAppearance ) ) &&
			         ( ( _sideMap == null ) ? ( cylinder._sideMap == null ) : _sideMap.equals( cylinder._sideMap ) ) &&
			         ( ( _topMap == null ) ? ( cylinder._topMap == null ) : _topMap.equals( cylinder._topMap ) ) &&
			         ( ( _bottomMap == null ) ? ( cylinder._bottomMap == null ) : _bottomMap.equals( cylinder._bottomMap ) ) &&
			         ( _smoothCircumference == cylinder._smoothCircumference ) &&
			         ( _flipNormals == cylinder._flipNormals );
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
		return MathTools.hashCode( _height ) ^
		       MathTools.hashCode( _radius ) ^
		       _numEdges ^
		       ( ( _sideAppearance != null ) ? _sideAppearance.hashCode() : 0 ) ^
		       ( ( _topAppearance != null ) ? _topAppearance.hashCode() : 0 ) ^
		       ( ( _bottomAppearance != null ) ? _bottomAppearance.hashCode() : 0 );
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{tag=" + getTag() + ", height=" + _height + ", radius=" + _radius + '}';
	}
}
