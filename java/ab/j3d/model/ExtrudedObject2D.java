/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BoxUVMap;
import ab.j3d.geom.UVMap;

/**
 * This class extends {@link Object3D}. The vertices and faces are generated out
 * of a Java 2D simple {@link Shape}. An extrusion vector is used to define the
 * coordinate displacement.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ExtrudedObject2D
	extends Object3D
{
	/**
	 * Transform to apply.
	 */
	public final Matrix3D transform;

	/**
	 * Base shape.
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
	public final boolean hasBackface;

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
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface )
	{
		this( shape , extrusion , transform , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals )
	{
		this( shape , extrusion , transform , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , flipNormals , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   uvMap           Provides UV coordinates.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final UVMap uvMap , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals )
	{
		this( shape , extrusion , transform , uvMap , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , flipNormals , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply to the extruded shape.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this( shape , extrusion , transform , material , material , material , flatness , hasBackface , flipNormals , caps );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   uvMap           Provides UV coordinates.
	 * @param   material        Material to apply to the extruded shape.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final UVMap uvMap , final Material material , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this( shape , extrusion , transform , uvMap , material , material , material , flatness , hasBackface , flipNormals , caps );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this( shape , extrusion , transform , new BoxUVMap( Scene.MM , Matrix3D.INIT ) , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , flipNormals , caps );
		// @FIXME Retrieve model units instead of assuming millimeters for UV map.
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   uvMap           Provides UV coordinates.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final UVMap uvMap , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;
		this.flipNormals = flipNormals;
		this.caps        = caps;

		final Object3DBuilder builder = new Object3DBuilder( this );
		builder.addExtrudedShape( shape , flatness , extrusion , transform , uvMap , topMaterial , false , bottomMaterial , false , sideMaterial , false , hasBackface , flipNormals , caps );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape               Base shape.
	 * @param   extrusion           Extrusion vector (control-point displacement).
	 * @param   transform           Transform to apply.
	 * @param   topMaterial         Material to apply to the top cap.
	 * @param   topFlipTexture      Whether the top texture direction is flipped.
	 * @param   bottomMaterial      Material to apply to the bottom cap.
	 * @param   bottomFlipTexture   Whether the bottom texture direction is flipped.
	 * @param   sideMaterial        Material to apply to the extruded sides.
	 * @param   sideFlipTexture     Whether the side texture direction is flipped.
	 * @param   flatness            Flatness to use.
	 * @param   hasBackface         Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals         If  <code>true</code>, normals are flipped to
	 *                              point in the opposite direction.
	 * @param   caps                If <code>true</code>, the top and bottom of the
	 *                              extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final boolean topFlipTexture , final Material bottomMaterial , final boolean bottomFlipTexture , final Material sideMaterial , final boolean sideFlipTexture , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;
		this.flipNormals = flipNormals;
		this.caps        = caps;

		final UVMap uvMap = new BoxUVMap( Scene.MM , Matrix3D.INIT ); // @FIXME Retrieve model units instead of assuming millimeters.

		final Object3DBuilder builder = new Object3DBuilder( this );
		builder.addExtrudedShape( shape , flatness , extrusion , transform , uvMap , topMaterial , topFlipTexture , bottomMaterial , bottomFlipTexture , sideMaterial , sideFlipTexture , hasBackface , flipNormals , caps );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 */
	public static void generateExtrudedShape( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface )
	{
		final UVMap uvMap = new BoxUVMap( Scene.MM , Matrix3D.INIT ); // @FIXME Retrieve model units instead of assuming millimeters.

		final Object3DBuilder builder = new Object3DBuilder( target );
		builder.addExtrudedShape( shape , flatness , extrusion , transform , uvMap , material , false , material , false , material , false , hasBackface , false , false );
	}
}
