/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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

import ab.j3d.*;
import ab.j3d.geom.*;
import junit.framework.*;

/**
 * Unit test for {@link Cone3D} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class TestCone3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestCone3D.class.getName();

	/**
	 * Test constructor for cone object.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testConstructor()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testConstructor" );
		final Material material = new Material();
		material.code = "test";
		material.colorMap = "test";
		material.colorMapWidth = 123.0f;
		material.colorMapHeight = 456.0f;

		final BoxUVMap uvMap = new BoxUVMap( Scene.MM );

		final double[] radiusBottom = { 0.0, 50.0, 50.0, 25.0 };
		final double[] radiusTop = { 50.0, 0.0, 25.0, 50.0 };

		for ( int i = 0 ; i <= 0xFF ; i++ )
		{
			new Cone3D( 100.0 ,
			            radiusBottom[ i & 0x03 ] ,
			            radiusTop[ i & 0x03 ] , 32 ,
			            ( ( i & 0x04 ) == 0 ) ? material : null ,
			            ( ( i & 0x08 ) == 0 ) ? uvMap : null  , false ,
			            ( ( i & 0x10 ) == 0 ) ? material : null  ,
			            ( ( i & 0x20 ) == 0 ) ? uvMap : null  ,
			            ( ( i & 0x40 ) == 0 ) ? material : null  ,
			            ( ( i & 0x80 ) == 0 ) ? uvMap : null  , false );
		}
	}
}