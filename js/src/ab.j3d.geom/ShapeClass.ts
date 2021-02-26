/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
import { Enum } from '@numdata/oss';

export class ShapeClass extends Enum
{
	static readonly VOID = new ShapeClass( 'VOID' );
	static readonly LINE_SEGMENT = new ShapeClass( 'LINE_SEGMENT' );
	static readonly OPEN_PATH = new ShapeClass( 'OPEN_PATH' );
	static readonly CW_TRIANGLE = new ShapeClass( 'CW_TRIANGLE' );
	static readonly CCW_TRIANGLE = new ShapeClass( 'CCW_TRIANGLE' );
	static readonly CW_QUAD = new ShapeClass( 'CW_QUAD' );
	static readonly CCW_QUAD = new ShapeClass( 'CCW_QUAD' );
	static readonly CW_CONVEX = new ShapeClass( 'CW_CONVEX' );
	static readonly CCW_CONVEX = new ShapeClass( 'CCW_CONVEX' );
	static readonly CW_CONCAVE = new ShapeClass( 'CW_CONCAVE' );
	static readonly CCW_CONCAVE = new ShapeClass( 'CCW_CONCAVE' );
	static readonly COMPLEX = new ShapeClass( 'COMPLEX' );
	static readonly values = [ ShapeClass.VOID, ShapeClass.LINE_SEGMENT, ShapeClass.OPEN_PATH, ShapeClass.CW_TRIANGLE, ShapeClass.CCW_TRIANGLE, ShapeClass.CW_QUAD, ShapeClass.CCW_QUAD, ShapeClass.CW_CONVEX, ShapeClass.CCW_CONVEX, ShapeClass.CW_CONCAVE, ShapeClass.CCW_CONCAVE, ShapeClass.COMPLEX ];
}
