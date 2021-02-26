/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * Object with parametric geometry. The entire object shares a single 3D vector
 * as its parameter, while a linear function using that parameter is associated
 * with each vertex.
 *
 * @author Gerrit Meinders
 */
public class ParametricObject3D
extends Object3D
{
	/**
	 * Parametric coordinates.
	 */
	private final List<LinearFunction3D> _parametricCoordinates;

	/**
	 * Parameter value matching the current vertex coordinates.
	 */
	private Vector3D _parameter;

	/**
	 * Constructs a new parametric 3D object from two given variations, using
	 * the size of the objects as parameter.
	 */
	public ParametricObject3D()
	{
		_parametricCoordinates = new ArrayList<>();
		_parameter = Vector3D.ZERO;
	}

	/**
	 * Constructs a new parametric 3D object from two given variations, using
	 * the size of the objects as parameter.
	 *
	 * @param geometry              Object to be parameterized.
	 * @param parametricCoordinates Parametric vertex coordinates to use.
	 * @param initialParameter      Initial parameter value, corresponding
	 *                              with the current state of the geometry.
	 */
	public ParametricObject3D( final Object3D geometry, final List<LinearFunction3D> parametricCoordinates, final Vector3D initialParameter )
	{
		for ( final FaceGroup faceGroup : geometry.getFaceGroups() )
		{
			addFaceGroup( faceGroup );
		}

		_parametricCoordinates = parametricCoordinates;
		_parameter = initialParameter;

		updateVertexCoordinates();
	}

	/**
	 * Creates a parametric object 3D based on the given objects, using their
	 * respective sizes as parameter.
	 *
	 * <p>It is required that both objects have an identical tessellation.
	 * The tessellation of the first object is reused by the resulting
	 * parametric object, so that object should no longer be used.
	 *
	 * @param a First object.
	 * @param b Second object.
	 *
	 * @return Parametric object.
	 *
	 * @throws NullPointerException if {@link Object3D#getOrientedBoundingBox()}
	 * returns {@code null} for either object.
	 */
	public static @NotNull ParametricObject3D fromObjectsBySize( final @NotNull Object3D a, final @NotNull Object3D b )
	{
		final Bounds3D boundsA = Objects.requireNonNull( a.getOrientedBoundingBox(), "first object is empty" );
		final Bounds3D boundsB = Objects.requireNonNull( b.getOrientedBoundingBox(), "second object is empty" );
		return fromObjects( a, boundsA, b, boundsB );
	}

	/**
	 * Creates a parametric object 3D based on the given objects, using the
	 * given bounds as offset and parameter. This is useful for objects that are
	 * part of a larger whole, such as a glass pane inside a wooden frame.
	 *
	 * <p>It is required that both objects have an identical tessellation.
	 * The tessellation of the first object is reused by the resulting
	 * parametric object, so that object should no longer be used.
	 *
	 * @param objectA First object.
	 * @param boundsA Bounds used for the first object.
	 * @param objectB Second object.
	 * @param boundsB Bounds used for the second object.
	 *
	 * @return Parametric object.
	 */
	public static ParametricObject3D fromObjects( final Object3D objectA, final Bounds3D boundsA, final Object3D objectB, final Bounds3D boundsB )
	{
		final Vector3D minA = boundsA.min();
		final Vector3D minB = boundsB.min();

		final Vector3D sizeA = boundsA.size();
		final Vector3D sizeB = boundsB.size();

		final List<LinearFunction3D> parametricCoordinates = new ArrayList<>( objectA.getVertexCount() );
		{
			final List<Vector3D> coordinatesA = objectA.getVertexCoordinates();
			final List<Vector3D> coordinatesB = objectB.getVertexCoordinates();

			for ( int i = 0; i < coordinatesA.size(); i++ )
			{
				final Vector3D pointA = coordinatesA.get( i );
				final Vector3D pointB = coordinatesB.get( i );

				final LinearFunction3D point = LinearFunction3D.fromPoints( sizeA, pointA.minus( minA ), sizeB, pointB.minus( minB ) );
				parametricCoordinates.add( point );
			}
		}

		return new ParametricObject3D( objectA, parametricCoordinates, sizeA );
	}

	/**
	 * Returns the parameter value for the current geometry.
	 *
	 * @return Parameter value.
	 */
	public Vector3D getParameter()
	{
		return _parameter;
	}

	/**
	 * Sets the parameter value and updates the geometry accordingly.
	 *
	 * @param parameter Parameter value.
	 */
	public void setParameter( final Vector3D parameter )
	{
		if ( !_parameter.equals( parameter ) )
		{
			_parameter = parameter;
			updateVertexCoordinates();
		}
	}

	/**
	 * Updates vertex coordinates (including {@link Vertex3D#point}) based on
	 * the current parameter value, {@link #_parameter}.
	 */
	private void updateVertexCoordinates()
	{
		final Vector3D parameter = _parameter;
		final List<LinearFunction3D> parametricCoordinates = _parametricCoordinates;

		final List<Vector3D> coordinates = new ArrayList<>( parametricCoordinates.size() );
		for ( final LinearFunction3D parametricCoordinate : parametricCoordinates )
		{
			coordinates.add( parametricCoordinate.get( parameter ) );
		}
		setVertexCoordinates( coordinates );

		final List<FaceGroup> original = getFaceGroups();
		final List<FaceGroup> updated = new ArrayList<>( original.size() );
		for ( final FaceGroup originalGroup : original )
		{
			final FaceGroup updatedGroup = new FaceGroup( originalGroup.getAppearance(), originalGroup.isSmooth(), originalGroup.isSmooth() );
			for ( final Face3D face : originalGroup.getFaces() )
			{
				updatedGroup.addFace( face );
				for ( final Vertex3D vertex : face.getVertices() )
				{
					vertex.point = coordinates.get( vertex.vertexCoordinateIndex );
				}
				face.updateNormal();
			}
			updated.add( updatedGroup );
		}
		setFaceGroups( updated );
	}

	/**
	 * Returns the parametric vertex coordinates of the object. Each element
	 * corresponds to an element of {@link #getVertexCoordinates()}.
	 *
	 * @return Parametric vertex coordinates.
	 */
	public List<LinearFunction3D> getParametricCoordinates()
	{
		return Collections.unmodifiableList( _parametricCoordinates );
	}

	/**
	 * Sets the parametric vertex coordinates of the object.
	 *
	 * @param parametricCoordinates Parametric vertex coordinates.
	 */
	public void setParametricCoordinates( final List<LinearFunction3D> parametricCoordinates )
	{
		_parametricCoordinates.clear();
		_parametricCoordinates.addAll( parametricCoordinates );
	}
}
