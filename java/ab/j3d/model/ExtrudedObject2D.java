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

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.geom.*;

/**
 * This class extends {@link Object3D}. The vertices and faces are generated out
 * of a Java 2D simple {@link Shape}. An extrusion vector is used to define the
 * coordinate displacement.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class ExtrudedObject2D
	extends Object3D
{
	/**
	 * 2D shape to extrude.
	 */
	public final Shape shape;

	/**
	 * Extrusion vector (control-point displacement). This is a displacement
	 * relative to the shape being extruded.
	 */
	public final Vector3D extrusion;

	/**
	 * The maximum allowable distance between the control points and a
	 * flattened curve.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator(AffineTransform, double)
	 */
	public final double flatness;

	/**
	 * Flag to indicate if extruded faces have a backface.
	 */
	public final boolean twoSided;

	/**
	 * Indicates whether normals are flipped.
	 */
	public final boolean flipNormals;

	/**
	 * Indicates whether the top and bottom are capped.
	 */
	public final boolean caps;

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           2D shape to extrude.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   uvMap           Provides UV coordinates.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   twoSided        Indicates that extruded faces are two-sided.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform, double )
	 */
	public ExtrudedObject2D( final Shape shape, final Vector3D extrusion, final UVMap uvMap, final Material topMaterial, final Material bottomMaterial, final Material sideMaterial, final double flatness, final boolean twoSided, final boolean flipNormals, final boolean caps )
	{
		this.shape = shape;
		this.extrusion = extrusion;
		this.flatness = flatness;
		this.twoSided = twoSided;
		this.flipNormals = flipNormals;
		this.caps = caps;

		final Abstract3DObjectBuilder builder = new Object3DBuilder( this );
		if ( caps )
		{
			builder.addExtrudedShape( shape, flatness, extrusion, true, Matrix3D.IDENTITY, true, topMaterial, uvMap, false, true, bottomMaterial, uvMap, false, true, sideMaterial, uvMap, false, twoSided, flipNormals, false );
		}
		else
		{
			builder.addExtrudedShape( shape, flatness, extrusion, Matrix3D.IDENTITY, sideMaterial, uvMap, false, twoSided, flipNormals, false );
		}
	}

	@Override
	protected Bounds3D calculateOrientedBoundingBox()
	{
		final Rectangle2D bounds2d = shape.getBounds2D();
		final Vector3D extrusion = this.extrusion;

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
		return clazz.getSimpleName() + '@' + Integer.toHexString( hashCode() ) + "{shape=" + shape + ", extrusion=" + extrusion.toFriendlyString() + ", flatness=" + flatness + ", twoSided=" + twoSided + ", flipNormals=" + flipNormals + ", caps=" + caps + ", tag=" + getTag() + '}';
	}
}
