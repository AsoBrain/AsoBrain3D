/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2009 Peter S. Heijnen
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

import junit.framework.TestCase;

import ab.j3d.Material;
import ab.j3d.Vector3D;
import ab.j3d.geom.BoxUVMap;

/**
 * Unit test for {@link Cylinder3D} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class TestCylinder3D
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestCylinder3D.class.getName();

	/**
	 * Test constructor for cylinder object.
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

		new Cylinder3D( 50.0 , 50.0 , 100.0 , 16 , material , false , false , false , false );
		new Cylinder3D(  0.0 , 50.0 , 100.0 , 16 , material , false , false , false , false );
		new Cylinder3D( 50.0 ,  0.0 , 100.0 , 16 , material , false , false , false , false );
		new Cylinder3D( 50.0 , 50.0 , 100.0 , 16 , material , false , false , true  , false );
		new Cylinder3D(  0.0 , 50.0 , 100.0 , 16 , material , false , false , true  , false );
		new Cylinder3D( 50.0 ,  0.0 , 100.0 , 16 , material , false , false , true  , false );
		new Cylinder3D( 50.0 , 50.0 , 100.0 , 16 , material , false , false , false , true );
		new Cylinder3D(  0.0 , 50.0 , 100.0 , 16 , material , false , false , false , true );
		new Cylinder3D( 50.0 ,  0.0 , 100.0 , 16 , material , false , false , false , true );
		new Cylinder3D( 50.0 , 50.0 , 100.0 , 16 , material , false , false , true  , true );
		new Cylinder3D(  0.0 , 50.0 , 100.0 , 16 , material , false , false , true  , true );
		new Cylinder3D( 50.0 ,  0.0 , 100.0 , 16 , material , false , false , true  , true );

		for ( int i = 0 ; i <= 0xFF ; i++ )
		{
			new Cylinder3D( new Vector3D( 100.0 , 200.0 , 300.0 ) , Vector3D.POSITIVE_X_AXIS ,
			                ( ( i & 0x01 ) == 0 ) ? 50.0 : 0.0 ,
			                ( ( i & 0x02 ) == 0 ) ? 50.0 : 0.0 , 100.0 , 32 ,
			                ( ( i & 0x04 ) == 0 ) ? material : null ,
			                ( ( i & 0x08 ) == 0 ) ? uvMap : null  , false ,
			                ( ( i & 0x10 ) == 0 ) ? material : null  ,
			                ( ( i & 0x20 ) == 0 ) ? uvMap : null  ,
			                ( ( i & 0x40 ) == 0 ) ? material : null  ,
			                ( ( i & 0x80 ) == 0 ) ? uvMap : null  , false );
		}
	}
}