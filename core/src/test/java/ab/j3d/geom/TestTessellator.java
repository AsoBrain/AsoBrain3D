/*
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
 */
package ab.j3d.geom;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.awt.*;
import ab.j3d.model.*;
import junit.framework.*;

/**
 * Unit test for the {@link Tessellator}.
 *
 * @author G. Meinders
 */
public class TestTessellator
extends TestCase
{
	/**
	 * Test some extra shapes to test basic tessellator functionality.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testExtremeShapes()
	throws Exception
	{
		{
			final Shape emptyPath = new Path2D.Float();

			final Tessellator tessellator = ShapeTools.createTessellator( emptyPath, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertTrue( "Expected no primitives.", primitives.isEmpty() );
		}

		{
			final Path2D point = new Path2D.Float();
			point.moveTo( 10.0, 10.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( point, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertTrue( "Expected no primitives.", primitives.isEmpty() );
		}

		{
			final Path2D point = new Path2D.Float();
			point.moveTo( 10.0, 10.0 );
			point.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( point, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertTrue( "Expected no primitives.", primitives.isEmpty() );
		}

		{
			final Path2D line = new Path2D.Float();
			line.moveTo( 10.0, 10.0 );
			line.lineTo( 20.0, 30.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( line, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertTrue( "Expected no primitives.", primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0, 10.0 );
			clockwise.lineTo( 20.0, 30.0 );
			clockwise.lineTo( 15.0, 5.0 );
			clockwise.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertFalse( "Expected primitives.", primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0, 10.0 );
			clockwise.lineTo( 20.0, 30.0 );
			clockwise.lineTo( 15.0, 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0, 10.0 );
			clockwise.lineTo( 120.0, 30.0 );
			clockwise.lineTo( 115.0, 5.0 );
			clockwise.closePath();

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			tessellator.getCounterClockwiseOutlines();

			assertFalse( "Expected primitives.", primitives.isEmpty() );
		}

		{
			final Path2D clockwise = new Path2D.Float();
			clockwise.moveTo( 10.0, 10.0 );
			clockwise.lineTo( 20.0, 30.0 );
			clockwise.lineTo( 15.0, 5.0 );
			clockwise.closePath();
			clockwise.moveTo( 110.0, 10.0 );
			clockwise.lineTo( 120.0, 30.0 );
			clockwise.lineTo( 115.0, 5.0 );

			final Tessellator tessellator = ShapeTools.createTessellator( clockwise, 0.0 );

			final Collection<TessellationPrimitive> primitives = tessellator.getCounterClockwisePrimitives();
			checkIntegrity( tessellator );

			assertFalse( "Expected primitives.", primitives.isEmpty() );
		}
	}

	/**
	 * Test some extra shapes to test basic tessellator functionality.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testE()
	throws Exception
	{
		/*
		 *  +------------+
		 *  |            |
		 *  |   +--------+
		 *  |   |
		 *  |   |
		 *  |   +--------+
		 *  |            |
		 *  |   +--------+
		 *  |   |
		 *  |   |
		 *  |   +--------+
		 *  |            |
		 *  +------------+
		 */
		final Path2D path = new Path2D.Float();
		path.moveTo( 35.0, -0.65 );
		path.lineTo( 35.0, 0.65 );
		path.lineTo( 1.3, 0.65 );
		path.lineTo( 1.3, 39.35 );
		path.lineTo( 35.0, 39.35 );
		path.lineTo( 35.0, 40.65 );
		path.lineTo( 0.0, 40.65 );
		path.lineTo( 0.0, -40.65 );
		path.lineTo( 35.0, -40.65 );
		path.lineTo( 35.0, -39.35 );
		path.lineTo( 1.3, -39.35 );
		path.lineTo( 1.3, -0.65 );
		path.lineTo( 35.0, -0.65 );
		path.closePath();

		final Tessellator tessellator = ShapeTools.createTessellator( path, 0.2 );
		checkIntegrity( tessellator );

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addExtrudedShape( tessellator, false, Matrix3D.IDENTITY, Matrix3D.getTranslation( 0.0, 0.0, 500.0 ), true, true, null, null, false, true, null, null, false, true, null, null, false, false, false, false );
		final Object3D object = builder.getObject3D();

		SceneIntegrityChecker.ensureIntegrity( object );
	}

	/**
	 * Check result of tessellator.
	 *
	 * @param tessellator Tessellator to check result of.
	 */
	public static void checkIntegrity( final Tessellator tessellator )
	{
		final List<Vector2D> vertices = tessellator.getVertexList();
		final List<TessellationPrimitive> ccwPrimitives = tessellator.getCounterClockwisePrimitives();
		final List<TessellationPrimitive> cwPrimitives = tessellator.getClockwisePrimitives();
		final List<int[]> ccwOutlines = tessellator.getCounterClockwiseOutlines();
		final List<int[]> cwOutlines = tessellator.getClockwiseOutlines();

		final StringBuilder sb = new StringBuilder();

		for ( int vertexIndex = 0; vertexIndex < vertices.size(); vertexIndex++ )
		{
			final Vector2D vertex = vertices.get( vertexIndex );

			if ( Double.isNaN( vertex.x ) || Double.isNaN( vertex.y ) )
			{
				sb.append( "\n\tvertices[" );
				sb.append( vertexIndex );
				sb.append( "] has bad (NaN) point: " );
				sb.append( vertex );
			}
		}

		for ( int clock = 0; clock < 2; clock++ )
		{
			final boolean clockwise = ( clock == 1 );
			final String prefix = clockwise ? "cw" : "ccw";
			final List<TessellationPrimitive> primitives = clockwise ? cwPrimitives : ccwPrimitives;
			final List<int[]> outlines = clockwise ? cwOutlines : ccwOutlines;

			for ( int primitiveIndex = 0; primitiveIndex < primitives.size(); primitiveIndex++ )
			{
				final TessellationPrimitive primitive = primitives.get( primitiveIndex );

				final int[] triangles = primitive.getTriangles();
				if ( ( triangles.length == 0 ) || ( ( triangles.length % 3 ) != 0 ) )
				{
					sb.append( "\n\t" );
					sb.append( prefix );
					sb.append( "Primitives[" );
					sb.append( primitiveIndex );
					sb.append( "] has invalid triangle list length " );
					sb.append( triangles.length );
					sb.append( " (must be non-zero multiple of 3) in " );
					sb.append( primitive );
				}

				for ( int triangleIndex = 0; triangleIndex < triangles.length; triangleIndex += 3 )
				{
					final Vector2D v1 = vertices.get( triangles[ triangleIndex ] );
					final Vector2D v2 = vertices.get( triangles[ triangleIndex + 1 ] );
					final Vector2D v3 = vertices.get( triangles[ triangleIndex + 2 ] );

					final double area = GeometryTools.getTriangleArea( v1, v2, v3 );
					if ( area == 0.0 )
					{
						sb.append( "\n\t" );
						sb.append( prefix );
						sb.append( "Primitives[" );
						sb.append( primitiveIndex );
						sb.append( "].triangles[" );
						sb.append( triangleIndex );
						sb.append( "] (triangle index=" );
						sb.append( triangleIndex / 3 );
						sb.append( ") is 0-area triangle of " );
						sb.append( primitive );
						sb.append( "\n\t\tv1 = " );
						sb.append( v1.toFriendlyString() );
						sb.append( "\n\t\tv2 = " );
						sb.append( v2.toFriendlyString() );
						sb.append( "\n\t\tv3 = " );
						sb.append( v3.toFriendlyString() );
					}
				}
			}

			for ( int outlineIndex = 0; outlineIndex < outlines.size(); outlineIndex++ )
			{
				final int[] outline = outlines.get( outlineIndex );
				if ( outline.length < 2 )
				{
					sb.append( "\n\t" );
					sb.append( prefix );
					sb.append( "Outlines[ " );
					sb.append( outlineIndex );
					sb.append( "] has invalid length " );
					sb.append( outline.length );
					sb.append( " (must be 2 or more) in " );
					sb.append( Arrays.toString( outline ) );
				}
			}
		}

		if ( sb.length() > 0 )
		{
			appendTessellatorProperties( sb, tessellator );

			fail( sb.toString() );
		}
	}

	private static void appendTessellatorProperties( final StringBuilder sb, final Tessellator tessellator )
	{
		final List<Vector2D> vertices = tessellator.getVertexList();
		final List<TessellationPrimitive> ccwPrimitives = tessellator.getCounterClockwisePrimitives();
		final List<TessellationPrimitive> cwPrimitives = tessellator.getClockwisePrimitives();
		final List<int[]> ccwOutlines = tessellator.getCounterClockwiseOutlines();
		final List<int[]> cwOutlines = tessellator.getClockwiseOutlines();

		sb.append( "\nTessellator properties:" );
		sb.append( "\n\tvertex count    = " );
		sb.append( vertices.size() );
		sb.append( "\n\tprimitive count = " );
		sb.append( ccwPrimitives.size() );
		sb.append( "\n\toutline count   = " );
		sb.append( ccwOutlines.size() );

		sb.append( "\n\tCounter-clockwise primitives:" );
		for ( int i = 0; i < ccwPrimitives.size(); i++ )
		{
			final TessellationPrimitive primitive = ccwPrimitives.get( i );
			sb.append( "\n\t\t[" );
			sb.append( i );
			sb.append( "]: " );
			sb.append( primitive );

		}

		sb.append( "\n\tClockwise primitives:" );
		for ( int i = 0; i < cwPrimitives.size(); i++ )
		{
			final TessellationPrimitive primitive = cwPrimitives.get( i );
			sb.append( "\n\t\t[" );
			sb.append( i );
			sb.append( "]: " );
			sb.append( primitive );

		}

		sb.append( "\n\tCounter-clockwise outlines:" );
		for ( int i = 0; i < ccwOutlines.size(); i++ )
		{
			final int[] outline = ccwOutlines.get( i );
			sb.append( "\n\t\t[" );
			sb.append( i );
			sb.append( "]: " );
			sb.append( Arrays.toString( outline ) );

		}

		sb.append( "\n\tClockwise outlines:" );
		for ( int i = 0; i < cwOutlines.size(); i++ )
		{
			final int[] outline = cwOutlines.get( i );
			sb.append( "\n\t\t[" );
			sb.append( i );
			sb.append( "]: " );
			sb.append( Arrays.toString( outline ) );

		}

		sb.append( "\n\tVertices:" );
		for ( int i = 0; i < vertices.size(); i++ )
		{
			final Vector2D vertex = vertices.get( i );
			sb.append( "\n\t\t[" );
			sb.append( i );
			sb.append( "]=" );
			sb.append( vertex.toFriendlyString() );

		}
	}

}
