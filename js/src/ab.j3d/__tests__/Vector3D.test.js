/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 2006-2018 Peter S. Heijnen
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
import { assert } from 'chai';

import Vector3D from '../Vector3D';

/**
 * This class tests the {@link Vector3D} class.
 *
 * @author Peter S. Heijnen
 */
describe( 'Vector3D', function()
{
	/**
	 * Test {@link Vector3D#isNonZero()} method.
	 */
	it( 'isNonZero', function()
	{
		assert.isFalse( new Vector3D( 0.0, 0.0, 0.0 ).isNonZero(), "Bad result from isNonZero( 0.0, 0.0, 0.0 )" );
		assert.isTrue( new Vector3D( 0.0, 0.0, 1.0 ).isNonZero(), "Bad result from isNonZero( 0.0, 0.0, 1.0 )" );
		assert.isFalse( new Vector3D( 0.0, 0.0, NaN ).isNonZero(), "Bad result from isNonZero( 0.0, 0.0, NaN )" );
		assert.isTrue( new Vector3D( 0.0, 1.0, 0.0 ).isNonZero(), "Bad result from isNonZero( 0.0, 1.0, 0.0 )" );
		assert.isTrue( new Vector3D( 0.0, 1.0, 1.0 ).isNonZero(), "Bad result from isNonZero( 0.0, 1.0, 1.0 )" );
		assert.isFalse( new Vector3D( 0.0, 1.0, NaN ).isNonZero(), "Bad result from isNonZero( 0.0, 1.0, NaN )" );
		assert.isFalse( new Vector3D( 0.0, NaN, 0.0 ).isNonZero(), "Bad result from isNonZero( 0.0, NaN, 0.0 )" );
		assert.isFalse( new Vector3D( 0.0, NaN, 1.0 ).isNonZero(), "Bad result from isNonZero( 0.0, NaN, 1.0 )" );
		assert.isFalse( new Vector3D( 0.0, NaN, NaN ).isNonZero(), "Bad result from isNonZero( 0.0, NaN, NaN )" );
		assert.isTrue( new Vector3D( 1.0, 0.0, 0.0 ).isNonZero(), "Bad result from isNonZero( 1.0, 0.0, 0.0 )" );
		assert.isTrue( new Vector3D( 1.0, 0.0, 1.0 ).isNonZero(), "Bad result from isNonZero( 1.0, 0.0, 1.0 )" );
		assert.isFalse( new Vector3D( 1.0, 0.0, NaN ).isNonZero(), "Bad result from isNonZero( 1.0, 0.0, NaN )" );
		assert.isTrue( new Vector3D( 1.0, 1.0, 0.0 ).isNonZero(), "Bad result from isNonZero( 1.0, 1.0, 0.0 )" );
		assert.isTrue( new Vector3D( 1.0, 1.0, 1.0 ).isNonZero(), "Bad result from isNonZero( 1.0, 1.0, 1.0 )" );
		assert.isFalse( new Vector3D( 1.0, 1.0, NaN ).isNonZero(), "Bad result from isNonZero( 1.0, 1.0, NaN )" );
		assert.isFalse( new Vector3D( 1.0, NaN, 0.0 ).isNonZero(), "Bad result from isNonZero( 1.0, NaN, 0.0 )" );
		assert.isFalse( new Vector3D( 1.0, NaN, 1.0 ).isNonZero(), "Bad result from isNonZero( 1.0, NaN, 1.0 )" );
		assert.isFalse( new Vector3D( 1.0, NaN, NaN ).isNonZero(), "Bad result from isNonZero( 1.0, NaN, NaN )" );
		assert.isFalse( new Vector3D( NaN, 0.0, 0.0 ).isNonZero(), "Bad result from isNonZero( NaN, 0.0, 0.0 )" );
		assert.isFalse( new Vector3D( NaN, 0.0, 1.0 ).isNonZero(), "Bad result from isNonZero( NaN, 0.0, 1.0 )" );
		assert.isFalse( new Vector3D( NaN, 0.0, NaN ).isNonZero(), "Bad result from isNonZero( NaN, 0.0, NaN )" );
		assert.isFalse( new Vector3D( NaN, 1.0, 0.0 ).isNonZero(), "Bad result from isNonZero( NaN, 1.0, 0.0 )" );
		assert.isFalse( new Vector3D( NaN, 1.0, 1.0 ).isNonZero(), "Bad result from isNonZero( NaN, 1.0, 1.0 )" );
		assert.isFalse( new Vector3D( NaN, 1.0, NaN ).isNonZero(), "Bad result from isNonZero( NaN, 1.0, NaN )" );
		assert.isFalse( new Vector3D( NaN, NaN, 0.0 ).isNonZero(), "Bad result from isNonZero( NaN, NaN, 0.0 )" );
		assert.isFalse( new Vector3D( NaN, NaN, 1.0 ).isNonZero(), "Bad result from isNonZero( NaN, NaN, 1.0 )" );
		assert.isFalse( new Vector3D( NaN, NaN, NaN ).isNonZero(), "Bad result from isNonZero( NaN, NaN, NaN )" );
	} );
} );
