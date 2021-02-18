/**
 * This class represents a 3D vector.
 *
 * @author Peter S. Heijnen
 */
export default class Vector3D
{
    /**
     * Zero-vector.
     */
    static ZERO: Vector3D;
    /**
     * Positive X-axis direction vector.
     */
    static POSITIVE_X_AXIS: Vector3D;
    /**
     * Negative X-axis direction vector.
     */
    static NEGATIVE_X_AXIS: Vector3D;
    /**
     * Positive Y-axis direction vector.
     */
    static POSITIVE_Y_AXIS: Vector3D;
    /**
     * Negative Y-axis direction vector.
     */
    static NEGATIVE_Y_AXIS: Vector3D;
    /**
     * Positive Z-axis direction vector.
     */
    static POSITIVE_Z_AXIS: Vector3D;
    /**
     * Negative Z-axis direction vector.
     */
    static NEGATIVE_Z_AXIS: Vector3D;

    /**
     * Get angle between this vector and another one specified as argument.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {number} angle between vectors in radians.
     */
    static angle( v1: Vector3D, v2: Vector3D ): number;

    /**
     * Test if two vectors are parallel to each other.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {boolean} {@code true} if the vectors are parallel; {@code false} if not.
     */
    static areParallel( v1: Vector3D, v2: Vector3D ): boolean;

    /**
     * Test if two vectors define the same direction.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {boolean} {@code true} if the vectors define the same direction; {@code false} if not.
     */
    static areSameDirection( v1: Vector3D, v2: Vector3D ): boolean;

    /**
     * Test if two vectors are perpendicular to each other.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {boolean} {@code true} if the vectors are perpendicular; {@code false} if not.
     */
    static arePerpendicular( v1: Vector3D, v2: Vector3D ): boolean;

    /**
     * Get cos(angle) between this vector and another one specified as argument.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {number} cos(angle) between vectors.
     */
    static cosAngle( v1: Vector3D, v2: Vector3D ): number;

    /**
     * Determine cross product between two vectors.
     *
     * <p>The cross product is related to the sine function by the equation
     *
     * <blockquote>|a &times; b| = |a| |b| sin &theta;</blockquote>
     *
     * where &theta; denotes the angle between the two vectors.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {Vector3D} Resulting vector.
     */
    static cross( v1: Vector3D, v2: Vector3D ): Vector3D;

    /**
     * Determine Z component of cross between two vectors.
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {number} Resulting vector.
     */
    static crossZ( v1: Vector3D, v2: Vector3D ): number;

    /**
     * Calculate distance between two point vectors.
     *
     * @param {Vector3D} p1 First point vector to calculate the distance between.
     * @param {Vector3D} p2 Second point vector to calculate the distance between.
     *
     * @return {number} Distance between this and the specified other vector.
     */
    static distanceBetween( p1: Vector3D, p2: Vector3D ): number;

    /**
     * Get direction from one point to another point.
     *
     * @param {Vector3D} from Point vector for from-point.
     * @param {Vector3D} to   Point vector for to-point.
     *
     * @return {Vector3D} Direction from from-point to to-point.
     */
    static direction( from: Vector3D, to: Vector3D ): Vector3D;

    /**
     * Calculate average of two vectors (i.e. center between two point vectors).
     *
     * @param {Vector3D} v1 First vector.
     * @param {Vector3D} v2 Second vector.
     *
     * @return {Vector3D} Average vector (i.e. center point).
     */
    static average( v1: Vector3D, v2: Vector3D ): Vector3D;

    /**
     * Calculate dot product (a.k.a. inner product) of this vector and another one
     * specified as argument.
     *
     * <p The dot product is related to the cosine function by the equation
     * *
     * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
     *
     * where &theta; denotes the angle between the two vectors.
     *
     * @param {Vector3D} v1 First vector operand.
     * @param {Vector3D} v2 Second vector operand.
     *
     * @return {number} Dot product.
     */
    static dot( v1: Vector3D, v2: Vector3D ): number;

    /**
     * Calculate dot product (a.k.a. inner product) of this vector and another one
     * specified as argument.
     *
     * <p The dot product is related to the cosine function by the equation
     * *
     * <blockquote>a &middot; b = |a| |b| cos &theta;</blockquote>
     *
     * where &theta; denotes the angle between the two vectors.
     *
     * @param {number} x1 X-coordinate of first vector operand.
     * @param {number} y1 Y-coordinate of first vector operand.
     * @param {number} z1 Z-coordinate of first vector operand.
     * @param {number} x2 X-coordinate of second vector operand.
     * @param {number} y2 Y-coordinate of second vector operand.
     * @param {number} z2 Z-coordinate of second vector operand.
     *
     * @return {number} Dot product.
     */
    static dot6( x1: number, y1: number, z1: number, x2: number, y2: number, z2: number ): number;

    /**
     * This function translates cartesian coordinates to polar/spherical
     * coordinates.
     *
     * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
         * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
     * the zenith.
     *
     * <p>See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical
     * Coordinates</a> at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br
     * /> See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate
     * System Transformation</a> by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul
     * Bourke</a>.
     *
     * @param {number} x Cartesian X coordinate.
     * @param {number} y Cartesian Y coordinate.
     * @param {number} z Cartesian Z coordinate.
     *
     * @return {Vector3D} Polar coordinates (radius,azimuth,zenith) based on cartesian
     *         coordinates defined by this vector.
     */
    static cartesianToPolar( x: number, y: number, z: number ): Vector3D;

    /**
     * This function translates polar/spherical coordinates to cartesian
     * coordinates.
     *
     * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
         * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
     * the zenith.
     *
     * <p>See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical
     * Coordinates</a> at <a href="http://mathworld.wolfram.com/">MathWorld</a>.<br
     * /> See <a href="http://astronomy.swin.edu.au/~pbourke/projection/coords/">Coordinate
     * System Transformation</a> by <a href="http://astronomy.swin.edu.au/~pbourke/">Paul
     * Bourke</a>.
     *
     * @param {number} radius  Radius of sphere.
     * @param {number} azimuth Angle measured from the x-axis in the XY-plane (0 => point on
     *                XZ-plane).
     * @param {number} zenith  Angle measured from the z-axis toward the XY-plane (0 =>
     *                point on Z-axis).
     *
     * @return {Vector3D} Cartesian coordinates based on polar coordinates
     *         (radius,azimuth,zenith) defined by this vector.
     */
    static polarToCartesian( radius: number, azimuth: number, zenith: number ): Vector3D;

    /**
     * Construct new vector.
     *
     * @param {number} x X-coordinate of vector.
     * @param {number} y Y-coordinate of vector.
     * @param {number} z Z-coordinate of vector.
     */
    constructor( x: number, y: number, z: number );

    /**
     * X component of 2D vector.
     * @type number
     */
    x: number;
    /**
     * Y component of 2D vector.
     * @type number
     */
    y: number;
    /**
     * Z component of 3D vector.
     * @type number
     */
    z: number;

    /**
     * Calculate distance between this point vector and another.
     *
     * @param {Vector3D} other Point vector to calculate the distance to.
     *
     * @return {number} Distance between this and the other vector.
     */
    distanceTo( other: Vector3D ): number;

    /**
     * Get direction from this point vector to another.
     *
     * @param {Vector3D} other Point vector to calculate the direction to.
     *
     * @return {Vector3D} Direction from this to the other vector.
     */
    directionTo( other: Vector3D ): Vector3D;

    /**
     * Compare this vector to another vector.
     *
     * @param other Vector to compare with.
     *
     * @return {boolean} true if the objects are almost equal; {@code false} if not.
     *
     * @see GeometryTools#almostEqual
     */
    almostEquals( other: object ): boolean;

    /**
     * Returns whether the given object is a vector equal to this.
     *
     * @param {*} other Object to compare with.
     *
     * @returns {boolean} true if equal.
     */
    equals( other: object ): boolean;

    /**
     * Get inverse vector.
     *
     * @return {Vector3D} Inverse vector.
     */
    inverse(): Vector3D;

    /**
     * Test whether this vector is a non-zero vector (its length is non-zero).
     *
     * This will return {@code false} when all components of this vector are
     * zero or any component is NaN.
     *
     * @return {boolean} {@code true} if this is a non-zero vector.
     */
    isNonZero(): boolean;

    /**
     * Calculate length of vector.
     *
     * @return {number} Length of vector.
     */
    length(): number;

    /**
     * Subtract another vector from this vector.
     *
     * @param {Vector3D} other Vector to subtract from this vector.
     *
     * @return {Vector3D} Resulting vector.
     */
    minus( other: Vector3D ): Vector3D;

    /**
     * Determine vector after scalar multiplication.
     *
     * @param {number} factor Scale multiplication factor.
     *
     * @return {Vector3D} Resulting vector.
     */
    multiply( factor: number ): Vector3D;

    /**
     * Normalize this vector (make length 1). If the vector has length 0 or 1, it
     * will be returned as-is.
     *
     * @return {Vector3D} Normalized vector.
     */
    normalize(): Vector3D;

    /**
     * Add another vector to this vector.
     *
     * @param {Vector3D} other Vector to add to this vector.
     *
     * @return {Vector3D} Resulting vector.
     */
    plus( other: Vector3D ): Vector3D;

    /**
     * Set vector to the specified coordinates.
     *
     * @param x X-coordinate of vector.
     * @param y Y-coordinate of vector.
     * @param z Z-coordinate of vector.
     *
     * @return {Vector3D} Resulting vector.
     */
    set( x: number, y: number, z: number ): Vector3D;

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
     * This function translates cartesian coordinates to polar/spherical
     * coordinates.
     *
     * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
         * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
     * the zenith.
     *
     * @return {Vector3D} Polar coordinates (radius,azimuth,zenith) based on cartesian
     *         coordinates defined by this vector.
     */
    cartesianToPolar(): Vector3D;

    /**
     * This function translates polar/spherical coordinates to cartesian
     * coordinates.
     *
     * <p>The polar/spherical coordinates are defined as the triplet {@code (r,
         * &theta;, &rho; )}, where r is radius, &theta; is the azimuth, and &rho; is
     * the zenith.
     *
     * @return {Vector3D} Cartesian coordinates based on polar coordinates
     *         (radius,azimuth,zenith) defined by this vector.
     */
    polarToCartesian(): Vector3D;
}
