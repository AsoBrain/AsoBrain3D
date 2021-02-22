/**
 * External class definition for ROM.
 *
 * @author  Peter S. Heijnen
 */
declare class Contour
{
}

declare namespace Contour
{
    class ShapeClass
    {
        static values: ShapeClass[];
        static VOID: ShapeClass;
        static LINE_SEGMENT: ShapeClass;
        static OPEN_PATH: ShapeClass;
        static CW_TRIANGLE: ShapeClass;
        static CCW_TRIANGLE: ShapeClass;
        static CW_QUAD: ShapeClass;
        static CCW_QUAD: ShapeClass;
        static CW_CONVEX: ShapeClass;
        static CCW_CONVEX: ShapeClass;
        static CW_CONCAVE: ShapeClass;
        static CCW_CONCAVE: ShapeClass;
        static COMPLEX: ShapeClass;
    }
}

export default Contour;
