/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2017 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.junit.*;
import junit.framework.*;

/**
 * Unit test for {@link Cylinder3D} class.
 *
 * @author Peter S. Heijnen
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
	 * @throws Exception if the test fails.
	 */
	public void testConstructor()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testConstructor" );
		final BasicAppearance appearance = new BasicAppearance();
		appearance.setDiffuseColor( Color4.WHITE );
		appearance.setSpecularColor( Color4.WHITE );
		appearance.setColorMap( new BasicTextureMap( "test", 123.0f, 456.0f ) );

		final UVMap uvMap = new BoxUVMap( Scene.MM );

		for ( int i = 0; i <= 0x7F; i++ )
		{
			final Cylinder3D cylinder3d = new Cylinder3D( ( ( i & 0x01 ) == 0 ) ? 50.0 : 0.0, 100.0, 19,
			                                              ( ( i & 0x02 ) == 0 ) ? appearance : null,
			                                              ( ( i & 0x04 ) == 0 ) ? uvMap : null, false,
			                                              ( ( i & 0x08 ) == 0 ) ? appearance : null,
			                                              ( ( i & 0x10 ) == 0 ) ? uvMap : null,
			                                              ( ( i & 0x20 ) == 0 ) ? appearance : null,
			                                              ( ( i & 0x40 ) == 0 ) ? uvMap : null, false );

			SceneIntegrityChecker.ensureIntegrity( cylinder3d );
		}
	}

	/**
	 * Tests that the normal vectors of the cylinder's sides are calculated
	 * properly.
	 */
	public void testNormals()
	{
		System.out.println( CLASS_NAME + ".testNormals()" );

		System.out.println( " - Regular normals" );
		final Cylinder3D regularCylinder = new Cylinder3D( 1.0, 2.0, 4, null, null, true, null, null, null, null, false );
		final Vector3D[] regularNormals = { Vector3D.POSITIVE_Y_AXIS, Vector3D.POSITIVE_X_AXIS, Vector3D.NEGATIVE_Y_AXIS, Vector3D.NEGATIVE_X_AXIS };
		SceneIntegrityChecker.ensureIntegrity( regularCylinder );
		assertNormals( "regular cylinder", regularNormals, regularCylinder );

		System.out.println( " - Flipped normals" );
		final Cylinder3D flippedCylinder = new Cylinder3D( 1.0, 2.0, 4, null, null, true, null, null, null, null, true );
		final Vector3D[] flippedNormals = { Vector3D.NEGATIVE_Y_AXIS, Vector3D.POSITIVE_X_AXIS, Vector3D.POSITIVE_Y_AXIS, Vector3D.NEGATIVE_X_AXIS };
		SceneIntegrityChecker.ensureIntegrity( flippedCylinder );
		assertNormals( "flipped cylinder", flippedNormals, flippedCylinder );
	}

	/**
	 * Tests that the given cylinder has the given normals.
	 *
	 * @param message         Message prefix.
	 * @param expectedNormals Expected vertex normal vectors.
	 * @param actual          Actual cylinder.
	 */
	private static void assertNormals( final String message, final Vector3D[] expectedNormals, final Cylinder3D actual )
	{
		final List<FaceGroup> faceGroups = actual.getFaceGroups();
		// Only 1 face group for a cylinder without caps.
		assertEquals( message + ": unexpected number of face groups", 1, faceGroups.size() );
		final FaceGroup faceGroup = faceGroups.get( 0 );

		final List<Face3D> faces = faceGroup.getFaces();
		assertEquals( message + ": unexpected number of faces", faces.size(), expectedNormals.length );

		for ( int i = 0; i < expectedNormals.length; i++ )
		{
			final Face3D face = faces.get( i );
			assertEquals( "Invalid face vertex count", 4, face.getVertexCount() );

			final int j = ( i + 1 ) % expectedNormals.length;
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 0: unexpected normal", expectedNormals[ j ], face.getVertexNormal( 0 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 1: unexpected normal", expectedNormals[ i ], face.getVertexNormal( 1 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 2: unexpected normal", expectedNormals[ i ], face.getVertexNormal( 2 ), 1.0e-8 );
			Vector3DTester.assertEquals( message + ", face " + i + ", vertex 3: unexpected normal", expectedNormals[ j ], face.getVertexNormal( 3 ), 1.0e-8 );
		}
	}

	/**
	 * Tests {@link Cylinder3D#collidesWith}.
	 */
	public void testCollidesWith()
	{
		final String where = CLASS_NAME + ".testCollidesWith()";
		System.out.println( where );

		final Appearance appearance = BasicAppearances.WHITE;
		final Cylinder3D cylinder1 = new Cylinder3D( 10, 50, 16, appearance, null, false, appearance, null, appearance, null, false );
		final Cylinder3D cylinder2 = new Cylinder3D( 5, 20, 16, appearance, null, false, appearance, null, appearance, null, false );

		// Along X-axis
		checkCollision( "Outside", cylinder1, cylinder2, Matrix3D.getTranslation( -80, 0, 2.5 ), false );
		checkCollision( "Touch outside", cylinder1, cylinder2, Matrix3D.getTranslation( -70, 0, 2.5 ), false );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( -60, 0, 2.5 ), true );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( -40, 0, 2.5 ), true );
		checkCollision( "Touch inside", cylinder1, cylinder2, Matrix3D.getTranslation( -30, 0, 2.5 ), true );
		checkCollision( "Inside", cylinder1, cylinder2, Matrix3D.getTranslation( -20, 0, 2.5 ), true );
		checkCollision( "Inside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 2.5 ), true );
		checkCollision( "Inside", cylinder1, cylinder2, Matrix3D.getTranslation( 20, 0, 2.5 ), true );
		checkCollision( "Touch inside", cylinder1, cylinder2, Matrix3D.getTranslation( 30, 0, 2.5 ), true );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( 40, 0, 2.5 ), true );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( 60, 0, 2.5 ), true );
		checkCollision( "Touch outside", cylinder1, cylinder2, Matrix3D.getTranslation( 70, 0, 2.5 ), false );
		checkCollision( "Outside", cylinder1, cylinder2, Matrix3D.getTranslation( 80, 0, 2.5 ), false );

		// Along Z-axis
		checkCollision( "Outside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, -7.5 ), false );
		checkCollision( "Touch outside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, -5.0 ), false );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, -2.5 ), true );
		checkCollision( "Touch inside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 0 ), true );
		checkCollision( "Inside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 2.5 ), true );
		checkCollision( "Touch inside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 5.0 ), true );
		checkCollision( "Intersect", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 7.5 ), true );
		checkCollision( "Touch outside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 10.0 ), false );
		checkCollision( "Outside", cylinder1, cylinder2, Matrix3D.getTranslation( 0, 0, 12.5 ), false );
	}

	/**
	 * Tests the specified collision, both ways.
	 *
	 * @param message           Failure message.
	 * @param first             First cylinder.
	 * @param second            Second cylinder.
	 * @param fromSecondToFirst Transform from second to first.
	 * @param expected          Whether a collision is expected.
	 */
	private static void checkCollision( final String message, final Cylinder3D first, final Cylinder3D second, final Matrix3D fromSecondToFirst, final boolean expected )
	{
		final boolean firstResult = first.collidesWith( fromSecondToFirst, second );
		assertEquals( message + ", first.collidesWith(second)", expected, firstResult );

		final boolean secondResult = second.collidesWith( fromSecondToFirst.inverse(), first );
		assertEquals( message + ", second.collidesWith(first)", expected, secondResult );
	}
}
