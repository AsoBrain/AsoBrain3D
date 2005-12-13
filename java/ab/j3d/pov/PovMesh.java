/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2005
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
	public List triangles = new ArrayList();

	/**
	 * The inside vector to make the mesh solid.
	 */
	public PovVector insideVector = null;

	/**
	 * Represents one triangle of the mesh.
	 */
	public static class Triangle
		extends PovObject
	{
		public final PovVector p1;
		public final PovVector p2;
		public final PovVector p3;
		public final boolean   smooth;
		public PovTexture      texture;

		public Triangle( final PovVector p1 , final PovVector p2 , final PovVector p3 , final boolean smooth )
		{
			this( p1 , p2 , p3 , smooth , null );
		}

		public Triangle( final PovVector p1 , final PovVector p2 , final PovVector p3 , final boolean smooth , final PovTexture texture )
		{
			this.p1      = p1;
			this.p2      = p2;
			this.p3      = p3;
			this.smooth  = smooth;
			this.texture = texture;
		}

		public void write( final IndentingWriter out )
			throws IOException
		{
			if ( texture != null && !texture.isDeclared() )
			{
				out.write( "#declare TM = " );
				texture.write( out );
				out.newLine();
				out.write( "triangle { " + p1 + " , " + p2 + " , " + p3 + " " );
				if ( texture != null )
					out.write( "texture { TM }" );
				out.writeln( " }" );
			}
			else
			{
				out.write( "triangle { " + p1 + " , " + p2 + " , " + p3 + " " );
				if ( texture != null )
					texture.write( out );
				out.writeln( " }" );
			}
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
	}

	/**
	 * Adds a triangle to the mesh.
	 *
	 * @param   t   New triangle.
	 */
	public void add( final Triangle t )
	{
		triangles.add( t );
	}

	/**
	 * Adds a number of points that make one face.
	 * The N-face is converted into triangles because
	 * pov only supports triangles.
	 * The face MUST be convex!
	 *
	 * @param   points      Array of points that make the face.
	 * @param   smooth      Face is smooth.
	 * @param   texture     Texture of the face (may be <code>null</code>).
	 */
	public void add( final PovVector[] points , final boolean smooth , final PovTexture texture )
	{
		for ( int i = 0 ; i < points.length - 2 ; i++ )
		{
			add( new Triangle( points[0] , points[i+1] , points[i+2], smooth , texture ) );
		}
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		/*
		 * No triangles, no mesh.
		 */
		if ( triangles.size() == 0 )
			return;

		out.writeln( "mesh //" + name );
		out.writeln( "{" );
		out.indentIn();

		if ( insideVector != null )
			out.writeln( "inside_vector " + insideVector );

		/*
		 * Figure out if maybee each triangle is same texture.
		 */
		boolean allTrianglesSame = true;
		boolean allTrianglesNull = true;
		PovTexture last = ((Triangle)triangles.get( 0 )).texture;
		for ( int i = 1 ; i < triangles.size() ; i++ )
		{
			final Triangle t = (Triangle)triangles.get( i );
			if ( t.texture == null )
			{
				last = null;
				allTrianglesSame = false;
				continue;
			}
			allTrianglesNull = false;
			if ( !t.texture.equals( last ) )
				allTrianglesSame = false;
			last = t.texture;
		}

		PovTexture main = texture;
		/*
		 * All the same textures, but not null,
		 * use global texture;
		 */
		if ( allTrianglesSame && !allTrianglesNull )
		{
			main = last;
			for ( int i = 0 ; i < triangles.size() ; i++ )
				((Triangle)triangles.get( i )).texture = null;
		}

		for ( int i = 0 ; i < triangles.size() ; i++ )
		{
			((Triangle)triangles.get( i )).write( out );
		}

		writeTransformation( out );

		if ( main != null )
		{
			main.write( out );
			out.newLine();
		}

		out.indentOut();
		out.writeln( "}" );
	}
}
