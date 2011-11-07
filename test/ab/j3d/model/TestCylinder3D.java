/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.net.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.junit.*;
import junit.framework.*;

/**
 * Unit test for {@link Cylinder3D} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class TestCylinder3D
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
		final BasicAppearance appearance = new BasicAppearance();
		appearance.setDiffuseColor( Color4.WHITE );
		appearance.setSpecularColor( Color4.WHITE );
		appearance.setColorMap( new FileTextureMap( new URL( "file:test" ), 123.0f, 456.0f ) );

		final BoxUVMap uvMap = new BoxUVMap( Scene.MM );

		for ( int i = 0 ; i <= 0x3F ; i++ )
		{
			new Cylinder3D( 50.0, 100.0 , 32 ,
			                ( ( i & 0x01 ) == 0 ) ? appearance : null ,
			                ( ( i & 0x02 ) == 0 ) ? uvMap : null  , false ,
			                ( ( i & 0x04 ) == 0 ) ? appearance : null  ,
			                ( ( i & 0x08 ) == 0 ) ? uvMap : null  ,
			                ( ( i & 0x10 ) == 0 ) ? appearance : null  ,
			                ( ( i & 0x20 ) == 0 ) ? uvMap : null  , false );
		}
	}

	/**
	 * Tests that the normal vectors of the cylinder's sides are calculated
	 * properly.
	 */
	public static void testNormals()
	{
		System.out.println( CLASS_NAME + ".testNormals()" );

		final Cylinder3D regularCylinder = new Cylinder3D( 1.0, 2.0, 4, null, null, true, null, null, null, null, false );
		final Vector3D[] regularNormals = { Vector3D.NEGATIVE_Y_AXIS, Vector3D.POSITIVE_X_AXIS, Vector3D.POSITIVE_Y_AXIS, Vector3D.NEGATIVE_X_AXIS };
		assertNormals( "regular cylinder", regularNormals, regularCylinder );

		final Cylinder3D flippedCylinder = new Cylinder3D( 1.0, 2.0, 4, null, null, true, null, null, null, null, true );
		final Vector3D[] flippedNormals = { Vector3D.POSITIVE_Y_AXIS, Vector3D.NEGATIVE_X_AXIS, Vector3D.NEGATIVE_Y_AXIS, Vector3D.POSITIVE_X_AXIS };
		assertNormals( "flipped cylinder", flippedNormals, flippedCylinder );
	}

	/**
	 * Tests that the given cylinder has the given normals.
	 *
	 * @param   message             Message prefix.
	 * @param   expectedNormals     Expected vertex normal vectors.
	 * @param   actual              Actual cylinder.
	 */
	private static void assertNormals( final String message, final Vector3D[] expectedNormals, final Cylinder3D actual )
	{
		final List<FaceGroup> faceGroups = actual.getFaceGroups();
		// Only 1 face group for a cylinder without caps.
		assertEquals( message + ": unexpected number of face groups", faceGroups.size(), 1 );
		final FaceGroup faceGroup = faceGroups.get( 0 );

		final List<Face3D> faces = faceGroup.getFaces();
		assertEquals( message + ": unexpected number of faces", faces.size(), expectedNormals.length );

		for ( int i = 0 ; i < expectedNormals.length ; i++ )
		{
			final Face3D face = faces.get( i );
			assertEquals( face.getVertexCount(), 4 );

			final int j = ( i + 1 ) % expectedNormals.length;
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 0: unexpected normal", expectedNormals[ j ], face.getVertexNormal( 0 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 1: unexpected normal", expectedNormals[ i ], face.getVertexNormal( 1 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 2: unexpected normal", expectedNormals[ i ], face.getVertexNormal( 2 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 3: unexpected normal", expectedNormals[ j ], face.getVertexNormal( 3 ), 1.0e-8 );
		}
	}
}
