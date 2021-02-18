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

import Vector3D from "./Vector3D";
import Bounds3D from "./Bounds3D";

/**
 * This class is used to represent a 3D transformation matrix (although
 * it may also be used for 2D transformations).
 *
 * <p>
 * The matrix is organized as follows:
 * <pre>
 * | xx xy xz xo |
 * | yx yy yz yo |
 * | zx zy zz zo |
 * | 0  0  0  1  |
 * </pre>
 *
 * @author  Peter S. Heijnen
 */
export default class Matrix3D
{
    /**
     * Identity matrix.
     * @type {Matrix3D}
     */
    static IDENTITY: Matrix3D;

    /**
     * Get transformation matrix based on 6 parameters specifying rotation angles
     * and a translation vector. Starting with the identity matrix, rotation is
     * performed (Z,X,Y order), than the translation is set.
     *
     * @param {number} rx Rotation angle around X axis (degrees, <strong>clockwise</strong>).
     * @param {number} ry Rotation angle around Y axis (degrees, counter-clockwise).
     * @param {number} rz Rotation angle around Z axis (degrees, counter-clockwise).
     * @param {number} tx X component of translation vector.
     * @param {number} ty Y component of translation vector.
     * @param {number} tz Z component of translation vector.
     *
     * @return {Matrix3D} Transformation matrix.
     */
    static getTransform( rx: number, ry: number, rz: number, tx: number, ty: number, tz: number ): Matrix3D;

    /**
     * Get transformation matrix based on the specified 'from' and 'to' points.
     * The result can be used to convert world coordinates to view coordinates.
     *
     * <p>
     * An up-vector must be specified to determine the correct view orientation.
     * Note that both a primary and a secondary up-vector is needed; the primary
     * up-vector is used when possible, the secondary up-vector is used when the
     * from-to vector is parallel to the primary up-vector.
     *
     * @param {Vector3D} from        Point to look from.
     * @param {Vector3D} to          Point to look at.
     * @param {Vector3D} upPrimary   Primary up-vector (must be normalized).
     * @param {Vector3D} upSecondary Secondary up-vector (must be normalized).
     *
     * @return {Matrix3D} Transformation matrix.
     *
     * @throws TypeError if any of the arguments is <code>null</code>.
     * @throws Error if the from and two points are too close.
     */
    static getFromToTransform( from: Vector3D, to: Vector3D, upPrimary: Vector3D, upSecondary: Vector3D ): Matrix3D;

    /**
     * Calculate transform for a plane that passes through <code>origin</code>
     * and has the specified <code>normal</code> vector.
     *
     * <p>
     * The main purpose for this method is creating a suitable 3D transformation
     * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
     * the X/Y-axes on the plane is inherently indeterminate, but this function
     * tries to find reasonable defaults.
     *
     * <p>
     * A 0-vector multiplied with the resulting transform will match the
     * <code>origin</code>.
     *
     * @param {Vector3D} origin          Origin of plane.
     * @param {Vector3D} normal          Normal of plane.
     * @param {boolean} rightHanded     3D-space is right- vs. left-handed.
     *
     * @return {Matrix3D} Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
     */
    static getPlaneTransform( origin: Vector3D, normal: Vector3D, rightHanded: boolean ): Matrix3D;

    /**
     * Calculate transform for a plane that passes through <code>origin</code>
     * and has the specified <code>normal</code> vector.
     *
     * <p>
     * The main purpose for this method is creating a suitable 3D transformation
     * for a 2D plane whose Z-axis points 'out' of the plane; the orientation of
     * the X/Y-axes on the plane is inherently indeterminate, but this function
     * tries to find reasonable defaults.
     *
     * <p>
     * A 0-vector multiplied with the resulting transform will match the
     * <code>origin</code>.
     *
     * @param {number} originX Origin X-coordinate of plane.
     * @param {number} originY Origin X-coordinate of plane.
     * @param {number} originZ Origin X-coordinate of plane.
     * @param {number} normalX X-component of normal vector of plane.
     * @param {number} normalY X-component of normal vector of plane.
     * @param {number} normalZ X-component of normal vector of plane.
     * @param {boolean} rightHanded 3D-space is right- vs. left-handed.
     *
     * @return {Matrix3D} Transformation matrix (translation set to 0-vector) to be used for extrusion of 2D shapes.
     */
    static _getPlaneTransform( originX: number, originY: number, originZ: number, normalX: number, normalY: number, normalZ: number, rightHanded: boolean ): Matrix3D;

    /**
     * Get transformation matrix for a rotation about an arbitrary axis. The
     * rotation is axis is specified by a pivot point and rotation axis
     * direction. The rotation angle is specified in radians.
     *
     * <p>
     * Also read <a href='http://www.cprogramming.com/tutorial/3d/rotation.html'>Rotation About an Arbitrary Axis</a>
     * (written by: Confuted, with a cameo by Silvercord (Charles Thibualt)).
     *
     * @param {Vector3D} pivot       Pivot point about which the rotation is performed.
     * @param {Vector3D} direction   Rotation axis direction (must be a unit vector).
     * @param {number} thetaRad    Rotate theta radians.
     *
     * @return {Matrix3D} Transformation matrix with requested rotation.
     */
    static getRotationTransform( pivot: Vector3D, direction: Vector3D, thetaRad: number ): Matrix3D;

    /**
     * Get transformation matrix for a rotation about an arbitrary axis. The
     * rotation is axis is specified by a pivot point and rotation axis
     * direction. The rotation angle is specified in radians.
     *
     * <p>
     * Read <a href='http://www.mlahanas.de/Math/orientation.htm'>We consider the problem of the coordinate transformation about a rotation axis.</a>
     * (written by Michael Lahanas) for a simple explanation of the problem and
     * solution.
     *
     * @param {number} pivotX      Pivot point about which the rotation is performed.
     * @param {number} pivotY      Pivot point about which the rotation is performed.
     * @param {number} pivotZ      Pivot point about which the rotation is performed.
     * @param {number} directionX  Rotation axis direction (must be a unit vector).
     * @param {number} directionY  Rotation axis direction (must be a unit vector).
     * @param {number} directionZ  Rotation axis direction (must be a unit vector).
     * @param {number} thetaRad    Rotate theta radians.
     *
     * @return {Matrix3D} Transformation matrix with requested rotation.
     */
    static _getRotationTransform( pivotX: number, pivotY: number, pivotZ: number, directionX: number, directionY: number, directionZ: number, thetaRad: number ): Matrix3D;

    /**
     * Returns a transformation matrix that scales coordinates by the given
     * factor.
     *
     * @param {number} scale Uniform scaling factor.
     *
     * @return {Matrix3D} Scaling matrix with the given factor.
     */
    static getScaleTransform( scale: number ): Matrix3D;

    /**
     * Returns a transformation matrix that scales coordinates by the given
     * factors.
     *
     * @param {number} x Scaling factor for the x-axis.
     * @param {number} y Scaling factor for the y-axis.
     * @param {number} z Scaling factor for the z-axis.
     *
     * @return {Matrix3D} Scaling matrix with the given factors.
     */
    static getScaleTransform( x: number, y: number, z: number ): Matrix3D;

    /**
     * Execute matrix multiplication between two matrices.
     *
     * @param {number} xx1     X quotient for X component of first matrix.
     * @param {number} xy1     Y quotient for X component of first matrix.
     * @param {number} xz1     Z quotient for X component of first matrix.
     * @param {number} xo1     Translation of X component of first matrix.
     * @param {number} yx1     X quotient for Y component of first matrix.
     * @param {number} yy1     Y quotient for Y component of first matrix.
     * @param {number} yz1     Z quotient for Y component of first matrix.
     * @param {number} yo1     Translation of Y component of first matrix.
     * @param {number} zx1     X quotient for Z component of first matrix.
     * @param {number} zy1     Y quotient for Z component of first matrix.
     * @param {number} zz1     Z quotient for Z component of first matrix.
     * @param {number} zo1     Translation of Z component of first matrix.
     * @param {number} xx2     X quotient for X component of second matrix.
     * @param {number} xy2     Y quotient for X component of second matrix.
     * @param {number} xz2     Z quotient for X component of second matrix.
     * @param {number} xo2     Translation of X component of second matrix.
     * @param {number} yx2     X quotient for Y component of second matrix.
     * @param {number} yy2     Y quotient for Y component of second matrix.
     * @param {number} yz2     Z quotient for Y component of second matrix.
     * @param {number} yo2     Translation of Y component of second matrix.
     * @param {number} zx2     X quotient for Z component of second matrix.
     * @param {number} zy2     Y quotient for Z component of second matrix.
     * @param {number} zz2     Z quotient for Z component of second matrix.
     * @param {number} zo2     Translation of Z component of second matrix.
     *
     * @return {Matrix3D} Resulting matrix.
     */
    static _multiply( xx1: number, xy1: number, xz1: number, xo1: number, yx1: number, yy1: number, yz1: number, yo1: number, zx1: number, zy1: number, zz1: number, zo1: number, xx2: number, xy2: number, xz2: number, xo2: number, yx2: number, yy2: number, yz2: number, yo2: number, zx2: number, zy2: number, zz2: number, zo2: number ): Matrix3D;

    /**
     * Get translation matrix from the specified translation vector.
     *
     * @param {Vector3D} translation Translation vector.
     *
     * @return {Matrix3D} Translation matrix.
     */
    static getTranslation( translation: Vector3D ): Matrix3D;

    /**
     * Get translation matrix from the specified translation vector.
     *
     * @param {number} x X-translation.
     * @param {number} y Y-translation.
     * @param {number} z Z-translation.
     *
     * @return {Matrix3D} Translation matrix.
     */
    static getTranslation( x: number, y: number, z: number ): Matrix3D;

    /**
     * Construct a new matrix.
     *
     * @param {number} nxx X quotient for X component.
     * @param {number} nxy Y quotient for X component.
     * @param {number} nxz Z quotient for X component.
     * @param {number} nxo Translation of X component.
     * @param {number} nyx X quotient for Y component.
     * @param {number} nyy Y quotient for Y component.
     * @param {number} nyz Z quotient for Y component.
     * @param {number} nyo Translation of Y component.
     * @param {number} nzx X quotient for Z component.
     * @param {number} nzy Y quotient for Z component.
     * @param {number} nzz Z quotient for Z component.
     * @param {number} nzo Translation of Z component.
     */
    constructor( nxx: number, nxy: number, nxz: number, nxo: number, nyx: number, nyy: number, nyz: number, nyo: number, nzx: number, nzy: number, nzz: number, nzo: number );

    /**
     * X quotient for X component.
     * @type {number}
     */
    xx: number;
    /**
     * Y quotient for X component.
     * @type {number}
     */
    xy: number;
    /**
     * Z quotient for X component.
     * @type {number}
     */
    xz: number;
    /**
     * Translation of X component.
     * @type {number}
     */
    xo: number;
    /**
     * X quotient for Y component.
     * @type {number}
     */
    yx: number;
    /**
     * Y quotient for Y component.
     * @type {number}
     */
    yy: number;
    /**
     * Z quotient for Y component.
     * @type {number}
     */
    yz: number;
    /**
     * Translation of Y component.
     * @type {number}
     */
    yo: number;
    /**
     * X quotient for Z component.
     * @type {number}
     */
    zx: number;
    /**
     * Y quotient for Z component.
     * @type {number}
     */
    zy: number;
    /**
     * Z quotient for Z component.
     * @type {number}
     */
    zz: number;
    /**
     * Translation of Z component.
     * @type {number}
     */
    zo: number;

    /**
     * Compare this matrix to another matrix.
     *
     * @param {Matrix3D} other Matrix to compare with.
     *
     * @return {boolean} <code>true</code> if the objects are almost equal; <code>false</code> if not.
     *
     * @see GeometryTools#almostEqual
     */
    almostEquals( other: Matrix3D ): boolean;

    /**
     * Returns whether the given object is a matrix equal to this.
     *
     * @param {*} other Object to compare with.
     *
     * @returns {boolean} true if equal.
     */
    equals( other: object ): boolean;

    /**
     * Get rotation matrix. This eliminates scaling and translation properties
     * from the current transformation matrix.
     *
     * @return {Matrix3D} Rotation material.
     */
    getRotation(): Matrix3D;

    /**
     * Construct inverse matrix.
     * <pre>
     *       | xx xy xz xo |
     *       | yx yy yz yo |
     *  T  = | zx zy zz zo |
     *       | 0  0  0  1  |
     *
     *       | xx yz zx -xo*xx-yo*yx-zo*zx |
     *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
     *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
     *       | 0  0  0  1                  |
     * </pre>
     *
     * @return {Matrix3D} Inverse matrix.
     */
    inverse(): Matrix3D;

    /**
     * Get <code>xo</code> of inverse transform.
     * <pre>
     *       | xx xy xz xo |
     *       | yx yy yz yo |
     *  T  = | zx zy zz zo |
     *       | 0  0  0  1  |
     *
     *       | xx yz zx <b>-xo*xx-yo*yx-zo*zx</b> |
     *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
     *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
     *       | 0  0  0  1                  |
     * </pre>
     *
     * @return {number} X-axis translation of inverse matrix.
     */
    inverseXo(): number;

    /**
     * Get <code>yo</code> of inverse transform.
     * <pre>
     *       | xx xy xz xo |
     *       | yx yy yz yo |
     *  T  = | zx zy zz zo |
     *       | 0  0  0  1  |
     *
     *       | xx yz zx -xo*xx-yo*yx-zo*zx |
     *   -1  | xy yy zy <b>-xo*xy-yo*yy-zo*zy</b> |
     *  T  = | xz yz zz -xo*xz-yo*yz-zo*zz |
     *       | 0  0  0  1                  |
     * </pre>
     *
     * @return {number} Y-axis translation of inverse matrix.
     */
    inverseYo(): number;

    /**
     * Get <code>zo</code> of inverse transform.
     * <pre>
     *       | xx xy xz xo |
     *       | yx yy yz yo |
     *  T  = | zx zy zz zo |
     *       | 0  0  0  1  |
     *
     *       | xx yz zx -xo*xx-yo*yx-zo*zx |
     *   -1  | xy yy zy -xo*xy-yo*yy-zo*zy |
     *  T  = | xz yz zz <b>-xo*xz-yo*yz-zo*zz</b> |
     *       | 0  0  0  1                  |
     * </pre>
     *
     * @return {number} Z-axis translation of inverse matrix.
     */
    inverseZo(): number;

    /**
     * Translate the transform by the specified vector.
     *
     * @param {Vector3D} v Vector specifying the translation.
     *
     * @return {Matrix3D} new Matrix3D with translation
     */
    minus( v: Vector3D ): Matrix3D;

    /**
     * Translate the transform by the specified vector.
     *
     * @param {number} x X-coordinate of vector specifying the translation.
     * @param {number} y Y-coordinate of vector specifying the translation.
     * @param {number} z Z-coordinate of vector specifying the translation.
     *
     * @return {Matrix3D} new Matrix3D with translation
     */
    minus( x: number, y: number, z: number ): Matrix3D;

    /**
     * Execute matrix multiplication between this and another matrix.
     *
     * @param {Matrix3D} other Matrix to multiply with.
     *
     * @return {Matrix3D} Resulting matrix.
     */
    multiply( other: Matrix3D ): Matrix3D;

    /**
     * Execute matrix multiplication between this and the inverse of another
     * matrix.
     *
     * @param {Matrix3D} other   Matrix whose inverse to multiply with.
     *
     * @return {Matrix3D} Resulting matrix.
     */
    multiplyInverse( other: Matrix3D ): Matrix3D;

    /**
     * Translate the transform by the specified vector.
     *
     * @param {Vector3D} v Vector specifying the translation.
     *
     * @return {Matrix3D} new Matrix3D with translation
     */
    plus( v: Vector3D ): Matrix3D;

    /**
     * Translate the transform by the specified vector.
     *
     * @param {number} x X-coordinate of vector specifying the translation.
     * @param {number} y Y-coordinate of vector specifying the translation.
     * @param {number} z Z-coordinate of vector specifying the translation.
     *
     * @return {Matrix3D} new Matrix3D with translation
     */
    plus( x: number, y: number, z: number ): Matrix3D;

    /**
     * Rotate about the X-axis in counter-clockwise direction.
     *
     * <p>
     * The returned matrix is the current matrix multiplied by the following
     * transformation matrix:
     * <pre>
     *    [   1     0     0   ]
     *    [   0    cos  -sin  ]
     *    [   0    sin   cos  ]
     * </pre>
     *
     * @param {number} thetaRad Rotate theta radians about the X-axis
     *
     * @return {Matrix3D} Rotated matrix.
     */
    rotateX( thetaRad: number ): Matrix3D;

    /**
     * Rotate about the Y-axis in counter-clockwise direction.
     *
     * <p>
     * The returned matrix is the current matrix multiplied by the following
     * transformation matrix:
     * <pre>
     *    [  cos    0    sin  ]
     *    [   0     1     0   ]
     *    [ -sin    0    cos  ]
     * </pre>
     *
     * @param {number} thetaRad Rotate theta radians about the Y-axis
     *
     * @return {Matrix3D} Rotated matrix.
     */
    rotateY( thetaRad: number ): Matrix3D;

    /**
     * Rotate about the Z-axis in counter-clockwise direction.
     *
     * <p>
     * The returned matrix is the current matrix multiplied by the following
     * transformation matrix:
     * <pre>
     *    [  cos  -sin    0   ]
     *    [  sin   cos    0   ]
     *    [   0     0     1   ]
     * </pre>
     *
     * @param {number} thetaRad Rotate theta radians about the Z-axis
     *
     * @return {Matrix3D} Rotated matrix.
     */
    rotateZ( thetaRad: number ): Matrix3D;

    /**
     * Set all values in the matrix and return the resulting matrix.
     *
     * @param {number} nxx X quotient for X component.
     * @param {number} nxy Y quotient for X component.
     * @param {number} nxz Z quotient for X component.
     * @param {number} nxo Translation of X component.
     * @param {number} nyx X quotient for Y component.
     * @param {number} nyy Y quotient for Y component.
     * @param {number} nyz Z quotient for Y component.
     * @param {number} nyo Translation of Y component.
     * @param {number} nzx X quotient for Z component.
     * @param {number} nzy Y quotient for Z component.
     * @param {number} nzz Z quotient for Z component.
     * @param {number} nzo Translation of Z component.
     *
     * @return {Matrix3D} Resulting vector.
     */
    set( nxx: number, nxy: number, nxz: number, nxo: number, nyx: number, nyy: number, nyz: number, nyo: number, nzx: number, nzy: number, nzz: number, nzo: number ): Matrix3D;

    /**
     * Scale this matrix by the given factor.
     *
     * @param {number} s Uniform scaling factor.
     *
     * @return {Matrix3D} This matrix scaled with the given factor.
     */
    scale( s: number ): Matrix3D;

    /**
     * Scale this matrix by the given factors.
     *
     * @param {number} x Scaling factor for the x-axis.
     * @param {number} y Scaling factor for the y-axis.
     * @param {number} z Scaling factor for the z-axis.
     *
     * @return {Matrix3D} This matrix scaled with the given factors.
     */
    scale( x: number, y: number, z: number ): Matrix3D;

    /**
     * Get translation vector from this transform.
     *
     * @return {Vector3D} Translation vector.
     */
    getTranslation(): Vector3D;

    /**
     * Set translation of a transform to the specified vector.
     *
     * @param {Vector3D} v Translation vector.
     *
     * @return {Matrix3D} Resulting matrix.
     */
    setTranslation( v: Vector3D ): Matrix3D;

    /**
     * Set translation of a transform to the specified vector.
     *
     * @param {number} x X-value of vector.
     * @param {number} y Y-value of vector.
     * @param {number} z Z-value of vector.
     *
     * @return {Matrix3D} Resulting matrix.
     */
    setTranslation( x: number, y: number, z: number ): Matrix3D;

    /**
     * Get string representation of object.
     *
     * @return {string} String representation of object.
     */
    toString(): string;

    /**
     * Get string representation of object.
     *
     * @return {string} String representation of object.
     */
    toFriendlyString(): string;

    /**
     * Transform a bounds using this transform.
     *
     * @param {Bounds3D} b Bounds to transform.
     *
     * @return {Vector3D} Resulting vector.
     */
    transform( b: Bounds3D ): Vector3D;

    /**
     * Transform a vector using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {Vector3D} Resulting vector.
     */
    transform( v: Vector3D ): Vector3D;

    /**
     * Transform a vector using this transform.
     *
     * @param {number} x X-value of vector.
     * @param {number} y Y-value of vector.
     * @param {number} z Z-value of vector.
     *
     * @return {Vector3D} Resulting vector.
     */
    transform( x: number, y: number, z: number ): Vector3D;

    /**
     * This function transforms a set of points. Point coordinates are supplied
     * using double arrays containing a triplet for each point.
     *
     * @param {number[]} source      Source array.
     * @param {number[]} dest        Destination array (may be <code>null</code> or too small to create new).
     * @param {number} pointCount  Number of vertices.
     *
     * @return {number[]} Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
     *
     * @see     #transform(double, double, double)
     * @see     #transform(Vector3D)
     */
    transformArray( source: number[], dest: number[], pointCount: number ): number[];

    /**
     * Transform a vector to X-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting X coordinate.
     */
    transformX( v: Vector3D ): number;

    /**
     * Transform a vector to X-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting X coordinate.
     */
    transformX( x: number, y: number, z: number ): number;

    /**
     * Transform a vector to Y-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting Y coordinate.
     */
    transformY( v: Vector3D ): number;

    /**
     * Transform a vector to Y-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Y coordinate.
     */
    transformY( x: number, y: number, z: number ): number;

    /**
     * Transform a vector to Z-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting Z coordinate.
     */
    transformZ( v: Vector3D ): number;

    /**
     * Transform a vector to Z-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Z coordinate.
     */
    transformZ( x: number, y: number, z: number ): number;

    /**
     * Transform a vector using the inverse of this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {Vector3D} Resulting vector.
     */
    inverseTransform( v: Vector3D ): Vector3D;

    /**
     * Transform a vector using the inverse of this transform.
     *
     * @param {number} x X-value of vector.
     * @param {number} y Y-value o ector.
     * @param {number} z Z-value o ector.
     *
     * @return {Vector3D} Resulting vector.
     */
    inverseTransform( x: number, y: number, z: number ): Vector3D;

    /**
     * Inverse transform a vector to X-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting X coordinate.
     */
    inverseTransformX( v: Vector3D ): number;

    /**
     * Inverse transform a vector to X-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting X coordinate.
     */
    inverseTransformX( x: number, y: number, z: number ): number;

    /**
     * Inverse transform a vector to Y-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting Y coordinate.
     */
    inverseTransformY( v: Vector3D ): number;

    /**
     * Inverse transform a vector to Y-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Y coordinate.
     */
    inverseTransformY( x: number, y: number, z: number ): number;

    /**
     * Inverse transform a vector to Z-coordinate using this transform.
     *
     * @param {Vector3D} v Vector to transform.
     *
     * @return {number} Resulting Z coordinate.
     */
    inverseTransformZ( v: Vector3D ): number;

    /**
     * Inverse transform a vector to Z-coordinate using this transform.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Z coordinate.
     */
    inverseTransformZ( x: number, y: number, z: number ): number;

    /**
     * Rotate a vector using this transform. This multiplies the vector with
     * this matrix, excluding the translational components.
     *
     * @param {Vector3D} v Vector to rotate.
     *
     * @return  Rotated vector.
     */
    rotate( v: Vector3D ): Vector3D;

    /**
     * Rotate a vector using this transform. This multiplies the vector with
     * this matrix, excluding the translational components.
     *
     * @param {number} x X component of directional vector to rotate.
     * @param {number} y Y component of directional vector to rotate.
     * @param {number} z Z component of directional vector to rotate.
     *
     * @return  Rotated vector.
     */
    rotate( x: number, y: number, z: number ): Vector3D;

    /**
     * This function performs just the rotational part of of the transform on a
     * set of vectors. Vectors are supplied using float arrays with a triplet for
     * each vector.
     *
     * @param {number[]} source Source array.
     * @param {number[]} dest Destination array (may be <code>null</code> or too small to create new).
     * @param {number} vectorCount Number of vertices.
     *
     * @return {number[]} Array to which the transformed coordinates were written (may be different from the <code>dest</code> argument).
     *
     * @see     #transform(double, double, double)
     * @see     #transform(Vector3D)
     */
    rotateArray( source: number[], dest: number[], vectorCount: number ): number[];

    /**
     * Rotate a vector to X-coordinate using this rotate.
     *
     * @param {Vector3D} v Vector.
     *
     * @return {number} Resulting X coordinate.
     */
    rotateVectorX( v: Vector3D ): number;

    /**
     * Rotate a vector to X-coordinate using this rotate.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting X coordinate.
     */
    rotateVectorX( x: number, y: number, z: number ): number;

    /**
     * Rotate a vector to Y-coordinate using this rotate.
     *
     * @param {Vector3D} v Vector.
     *
     * @return {number} Resulting Y coordinate.
     */
    rotateVectorY( v: Vector3D ): number;

    /**
     * Rotate a vector to Y-coordinate using this rotate.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Y coordinate.
     */
    rotateVectorY( x: number, y: number, z: number ): number;

    /**
     * Rotate a vector to Z-coordinate using this rotate.
     *
     * @param {Vector3D} v Vector.
     *
     * @return {number} Resulting Z coordinate.
     */
    rotateVectorZ( v: Vector3D ): number;

    /**
     * Rotate a vector to Z-coordinate using this rotate.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     *
     * @return {number} Resulting Z coordinate.
     */
    rotateVectorZ( x: number, y: number, z: number ): number;

    /**
     * Rotate a vector using the inverse of this transform. This multiplies the
     * vector with the inverse of this matrix, excluding the translational
     * components.
     *
     * @param {Vector3D} v Vector to rotate.
     *
     * @return  Rotated vector.
     */
    inverseRotate( v: Vector3D ): Vector3D;

    /**
     * Rotate a vector using the inverse of this transform. This multiplies the
     * vector with the inverse of this matrix, excluding the translational
     * components.
     *
     * @param {number} x X component of directional vector to rotate.
     * @param {number} y Y component of directional vector to rotate.
     * @param {number} z Z component of directional vector to rotate.
     *
     * @return  Rotated vector.
     */
    inverseRotate( x: number, y: number, z: number ): Vector3D;

    /**
     * Calculates the X-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {Vector3D} v Vector to rotate.
     *
     * @return {number} Resulting X coordinate.
     */
    inverseRotateX( v: Vector3D ): number;

    /**
     * Calculates the X-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {number} x X component of directional vector to rotate.
     * @param {number} y Y component of directional vector to rotate.
     * @param {number} z Z component of directional vector to rotate.
     *
     * @return {number} Resulting X coordinate.
     */
    inverseRotateX( x: number, y: number, z: number ): number;

    /**
     * Calculates the Y-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {Vector3D} v Vector to rotate.
     *
     * @return {number} Resulting Y-coordinate.
     */
    inverseRotateY( v: Vector3D ): number;

    /**
     * Calculates the Y-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {number} x X component of directional vector to rotate.
     * @param {number} y Y component of directional vector to rotate.
     * @param {number} z Z component of directional vector to rotate.
     *
     * @return {number} Resulting Y-coordinate.
     */
    inverseRotateY( x: number, y: number, z: number ): number;

    /**
     * Calculates the Z-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {Vector3D} v Vector to rotate.
     *
     * @return {number} Resulting Z-coordinate.
     */
    inverseRotateZ( v: Vector3D ): number;

    /**
     * Calculates the Z-coordinate of the given vector when rotated with the
     * inverse of this matrix.
     *
     * @param {number} x X component of directional vector to rotate.
     * @param {number} y Y component of directional vector to rotate.
     * @param {number} z Z component of directional vector to rotate.
     *
     * @return {number} Resulting Z-coordinate.
     */
    inverseRotateZ( x: number, y: number, z: number ): number;

    /**
     * Returns whether the coordinate system represented by the matrix follows
     * the right-hand rule.
     *
     * @return {boolean} <code>true</code> if the coordinate system is right-handed.
     */
    isRighthanded(): boolean;

    /**
     * Returns the determinant of the matrix.
     *
     * @return {number} Determinant of the matrix.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Determinant">Determinant (Wikipedia)</a>
     */
    determinant(): number;

    /**
     * Returns an array containing the elements of the matrix in row-major order.
     *
     * @returns {number[]} Elements of the matrix (row-major order).
     */
    toArray(): number[];

    /**
     * Returns an array containing the elements of the matrix in row-major order.
     *
     * @returns {number[]} Elements of the matrix (row-major order).
     */
    toColumnMajorArray(): number[];
}
