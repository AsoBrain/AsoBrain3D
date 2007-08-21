/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2007
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
package ab.j3d.pov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.numdata.oss.io.IndentingWriter;

/**
 * Free patch object of triangles.
 * <pre>
 * mesh // name
 * {
 *     triangle { &lt;x1,y1,z1&gt; , &lt;x2,y2,z2&gt; , &lt;x3,y3,z3&gt; [texture] }
 *     triangle { &lt;x1,y1,z1&gt; , &lt;x2,y2,z2&gt; , &lt;x3,y3,z3&gt; [texture] }
 *     triangle { &lt;x1,y1,z1&gt; , &lt;x2,y2,z2&gt; , &lt;x3,y3,z3&gt; [texture] }
 *     [rotate]
 *     [matrix]
 *     [texture]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovMesh
	extends PovGeometry
{
	/**
	 * Collection of all triangles of the mesh.
	 */
	private List<Triangle> _triangles = new ArrayList<Triangle>();

	/**
	 * The inside vector of a mesh can be used to make it solid. Which, in turn,
	 * makes it possible to use it as constructive solid geometry; for example,
	 * to perform boolean operations, like {@link PovBool#DIFFERENCE} to make
	 * holes.
	 */
	private PovVector _insideVector;

	/**
	 * Represents one triangle of the mesh.
	 */
	private static class Triangle
		extends PovObject
	{
		public final PovVector _p1;
		public final PovVector _p2;
		public final PovVector _p3;
		public final boolean   _smooth;
		public PovTexture      _texture;

		Triangle( final PovVector p1 , final PovVector p2 , final PovVector p3 , final boolean smooth )
		{
			this( p1 , p2 , p3 , smooth , null );
		}

		Triangle( final PovVector p1 , final PovVector p2 , final PovVector p3 , final boolean smooth , final PovTexture texture )
		{
			_p1      = p1;
			_p2      = p2;
			_p3      = p3;
			_smooth  = smooth;
			_texture = texture;
		}

		public void write( final IndentingWriter out )
			throws IOException
		{
			final PovTexture texture    = _texture;

			if ( ( texture != null ) && !texture.isDeclared() )
			{
				out.write( "#declare TM = " );
				texture.write( out );
				out.newLine();
			}

			out.write( "triangle { " );
			_p1.write( out );
			out.write( " , " );
			_p2.write( out );
			out.write( " , " );
			_p3.write( out );
			out.write( (int)' ' );

			if ( texture != null )
			{
				if ( !texture.isDeclared() )
					out.write( "texture { TM }" );
				else
					texture.write( out );
			}

			out.write( " }" );
			out.newLine();
		}
	}

	/**
	 * Creates a mesh with specified name and texture.
	 *
	 * @param   name        Name of the shape.
	 * @param   texture     Texture of the shape.
	 */
	public PovMesh( final String name , final PovTexture texture )
	{
		super( name , texture );

		_insideVector = null;
	}

	/**
	 * Get optional inside vector of a mesh that makes it solid. Which, in turn,
	 * makes it possible to use it as constructive solid geometry; for example,
	 * to perform boolean operations, like {@link PovBool#DIFFERENCE} to make
	 * holes.
	 *
	 * @return  Inside vector of a mesh that makes it solid;
	 *          <code>null</code> if the mesh is not solid (just surfaces).
	 */
	public PovVector getInsideVector()
	{
		return _insideVector;
	}

	/**
	 * Set optional inside vector of a mesh to make it solid. Which, in turn,
	 * makes it possible to use it as constructive solid geometry; for example,
	 * to perform boolean operations, like {@link PovBool#DIFFERENCE} to make
	 * holes.
	 *
	 * @param   insideVector    Inside vector of a mesh to make it solid
	 *                          (typically <code>&lt;0,0,1&gt;</code>;
	 *                          <code>null</code> to make the mesh not solid).
	 */
	public void setInsideVector( final PovVector insideVector )
	{
		_insideVector = insideVector;
	}

	/**
	 * Add triangle to mesh.
	 *
	 * @param   p1          First vertex' coordinates.
	 * @param   p2          Second vertex' coordinates.
	 * @param   p3          Third vertex' coordinates.
	 * @param   smooth      The triangle is used to immitate a smooth surface.
	 * @param   texture     Texture applied to triangle.
	 */
	public void addTriangle( final PovVector p1 , final PovVector p2 , final PovVector p3 , final boolean smooth , final PovTexture texture )
	{
		_triangles.add( new Triangle( p1 , p2 , p3 , smooth , texture ) );
	}

	/**
	 * Adds a number of points that make one face. The N-face is converted into
	 * triangles, because POV-Ray only supports triangles. The face MUST be
	 * convex!
	 *
	 * @param   points      Array of points that make the face.
	 * @param   smooth      Face is smooth.
	 * @param   texture     Texture of the face (may be <code>null</code>).
	 */
	public void addPolygon( final PovVector[] points , final boolean smooth , final PovTexture texture )
	{
		for ( int i = 0 ; i < points.length - 2 ; i++ )
			addTriangle( points[ 0 ] , points[ i + 1 ] , points[ i + 2 ] , smooth , texture );
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		/*
		 * No triangles, no mesh.
		 */
		final List<Triangle> triangles = _triangles;
		if ( !triangles.isEmpty() )
		{
			out.write( "mesh //" );
			out.write( getName() );
			out.write( (int)'{' );
			out.newLine();
			out.indentIn();

			final PovVector insideVector = getInsideVector();
			if ( insideVector != null )
			{
				out.write( "inside_vector " );
				insideVector.write( out );
				out.newLine();
			}

			/*
			 * Figure out if maybe each triangle is same texture.
			 */
			boolean allTrianglesSame = true;
			boolean allTrianglesNull = true;

			final PovTexture first = triangles.get( 0 )._texture;

			for ( int i = 1 ; i < triangles.size() ; i++ )
			{
				final Triangle   triangle = triangles.get( i );
				final PovTexture texture  = triangle._texture;

				allTrianglesNull &= ( texture == null );
				allTrianglesSame &= ( texture != null ) && texture.equals( first );
			}

			final PovTexture defaultTexture = getTexture();
			PovTexture main = defaultTexture;

			/*
			 * All the same textures, but not null,
			 * use global texture;
			 */
			if ( allTrianglesSame && !allTrianglesNull )
			{
				main = first;

				for ( final Triangle triangle : triangles )
				{
					triangle._texture = null;
				}
			}

			for ( final Triangle triangle : triangles )
			{
				triangle.write( out );
			}

			setTexture( main );

			writeModifiers( out );

			/*
			 * Restore original textures.
			 */
			setTexture( defaultTexture );

			if ( allTrianglesSame && !allTrianglesNull )
			{
				for ( final Triangle triangle : triangles )
				{
					triangle._texture = main;
				}
			}

			out.indentOut();
			out.writeln( "}" );
		}
	}
}
