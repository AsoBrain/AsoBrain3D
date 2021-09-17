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
package ab.j3d.awt;

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * This class extends {@link Object3D}. The vertices and faces are generated out
 * of a Java 2D simple {@link Shape}. An extrusion vector is used to define the
 * coordinate displacement.
 *
 * @author G.B.M. Rupert
 */
public class ExtrudedObject2D
extends Object3D
{
	/**
	 * 2D shape to extrude.
	 */
	private final @NotNull Shape _shape;

	/**
	 * Extrusion vector (control-point displacement). This is a displacement
	 * relative to the shape being extruded.
	 */
	private final @NotNull Vector3D _extrusion;

	/**
	 * The maximum allowable distance between the control points and a
	 * flattened curve.
	 *
	 * @see FlatteningPathIterator
	 * @see Shape#getPathIterator(AffineTransform, double)
	 */
	private final double _flatness;

	/**
	 * Flag to indicate if extruded faces have a back-face.
	 */
	private final boolean _twoSided;

	/**
	 * Indicates whether normals are flipped.
	 */
	private final boolean _flipNormals;

	/**
	 * Indicates whether the top and bottom are capped.
	 */
	private final boolean _caps;

	/**
	 * Construct extruded object.
	 *
	 * @param shape            2D shape to extrude.
	 * @param extrusion        Extrusion vector (control-point displacement).
	 * @param uvMap            Provides UV coordinates.
	 * @param topAppearance    Appearance to apply to the top cap.
	 * @param bottomAppearance Appearance to apply to the bottom cap.
	 * @param sideAppearance   Appearance to apply to the extruded sides.
	 * @param flatness         Flatness to use.
	 * @param twoSided         Indicates that extruded faces are two-sided.
	 * @param flipNormals      If <code>true</code>, normals are flipped to
	 *                         point in the opposite direction.
	 * @param caps             If <code>true</code>, the top and bottom of the
	 *                         extruded shape are capped.
	 *
	 * @see FlatteningPathIterator
	 * @see Shape#getPathIterator(AffineTransform, double)
	 */
	public ExtrudedObject2D( final @NotNull Shape shape, final @NotNull Vector3D extrusion, final @Nullable UVMap uvMap, final @Nullable Appearance topAppearance, final @Nullable Appearance bottomAppearance, final @Nullable Appearance sideAppearance, final double flatness, final boolean twoSided, final boolean flipNormals, final boolean caps )
	{
		_shape = shape;
		_extrusion = extrusion;
		_flatness = flatness;
		_twoSided = twoSided;
		_flipNormals = flipNormals;
		_caps = caps;

		final Object3DBuilder builder = getBuilder();
		if ( caps )
		{
			builder.addExtrudedShape( ShapeTools.createTessellator( shape, flatness ), extrusion, true, Matrix3D.IDENTITY, true, topAppearance, uvMap, false, true, bottomAppearance, uvMap, false, true, sideAppearance, uvMap, false, twoSided, flipNormals, false );
		}
		else
		{
			builder.addExtrudedShape( ShapeTools.createContours( shape, flatness, true, true ), extrusion, Matrix3D.IDENTITY, sideAppearance, uvMap, false, twoSided, flipNormals, false );
		}
	}

	public @NotNull Shape getShape()
	{
		return _shape;
	}

	public @NotNull Vector3D getExtrusion()
	{
		return _extrusion;
	}

	public double getFlatness()
	{
		return _flatness;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	public boolean isFlipNormals()
	{
		return _flipNormals;
	}

	public boolean isCaps()
	{
		return _caps;
	}

	@Override
	protected @NotNull Bounds3D calculateOrientedBoundingBox()
	{
		final Rectangle2D bounds2d = _shape.getBounds2D();
		final Vector3D extrusion = _extrusion;

		final double minX = bounds2d.getMinX() + Math.min( 0.0, extrusion.x );
		final double maxX = bounds2d.getMaxX() + Math.max( 0.0, extrusion.x );
		final double minY = bounds2d.getMinY() + Math.min( 0.0, extrusion.y );
		final double maxY = bounds2d.getMaxY() + Math.max( 0.0, extrusion.y );
		final double minZ = Math.min( 0.0, extrusion.z );
		final double maxZ = Math.max( 0.0, extrusion.z );

		return new Bounds3D( minX, minY, minZ, maxX, maxY, maxZ );
	}

	@Override
	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{shape=" + _shape + ", extrusion=" + _extrusion.toFriendlyString() + ", flatness=" + _flatness + ", twoSided=" + _twoSided + ", flipNormals=" + _flipNormals + ", caps=" + _caps + ", tag=" + getTag() + '}';
	}
}
