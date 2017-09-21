/*
 * (C) Copyright Numdata BV 2011-2016 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
import Enum from '@numdata/oss/lib/java.lang/Enum';

/**
 * External class definition for ROM.
 *
 * @author  Peter S. Heijnen
 */
export default class Contour
{
	static ShapeClass = Enum.create( 'VOID', 'LINE_SEGMENT', 'OPEN_PATH', 'CW_TRIANGLE', 'CCW_TRIANGLE', 'CW_QUAD', 'CCW_QUAD', 'CW_CONVEX', 'CCW_CONVEX', 'CW_CONCAVE', 'CCW_CONCAVE', 'COMPLEX' );
}
