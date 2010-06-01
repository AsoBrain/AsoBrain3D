/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2010-2010 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package ab.j3d.geom;

import java.util.*;

import ab.j3d.*;
import com.numdata.oss.*;

/**
 * Basic implementation of a triangulation result.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicTriangulation
	implements Triangulation
{
	/**
	 * Primitives that the triangulation consists of.
	 */
	private final List<Primitive> _primitives;

	/**
	 * Vertex coordinates used in the triangulation.
	 */
	private final HashList<Vector3D> _vertices;

	/**
	 * Constructs a new triangulation.
	 */
	public BasicTriangulation()
	{
		_primitives = new ArrayList<Primitive>();
		_vertices  = new HashList<Vector3D>();
	}

	/**
	 * Adds the given vertex to the triangulation, returning its index.
	 *
	 * @param   vertex  Vertex to be added.
	 *
	 * @return  Vertex index.
	 */
	public int addVertex( final Vector3D vertex )
	{
		return _vertices.indexOfOrAdd( vertex );
	}

	@Override
	public Vector3D getVertex( final int index )
	{
		return _vertices.get( index );
	}

	/**
	 * Adds the given primitive to the triangulation.
	 *
	 * @param   primitive    Primitive to be added, specified as three vertex
	 *                      indices (see {@link #getPrimitives()}).
	 */
	public void addPrimitive( final Primitive primitive )
	{
		_primitives.add( primitive );
	}

	@Override
	public Collection<Primitive> getPrimitives()
	{
		return Collections.unmodifiableList( _primitives );
	}

	@Override
	public Collection<int[]> getTriangles()
	{
		final List<Primitive> primitives = _primitives;
		int resultCount = 0;
		for ( final Primitive primitive : primitives )
		{
			final int[] triangles = primitive.getTriangles();
			resultCount += triangles.length / 3;
		}

		final int[][] array = new int[ resultCount ][ 3 ];

		resultCount = 0;
		for ( final Primitive primitive : primitives )
		{
			final int[] primitiveTriangles = primitive.getTriangles();
			final int triangleVertexCount = primitiveTriangles.length;

			int i = 0 ;
			while ( i < triangleVertexCount )
			{
				final int[] triangle = array[ resultCount++ ];
				triangle[ 0 ] = primitiveTriangles[ i++ ];
				triangle[ 1 ] = primitiveTriangles[ i++ ];
				triangle[ 2 ] = primitiveTriangles[ i++ ];
			}
		}

		return Collections.unmodifiableList( Arrays.asList( array ) );
	}

	@Override
	public List<Vector3D> getVertices( final Matrix3D transform )
	{
		final List<Vector3D> result;

		if ( transform.equals( Matrix3D.INIT ) )
		{
			result = _vertices;
		}
		else
		{
			result = new ArrayList<Vector3D>( _vertices.size() );
			for ( final Vector3D vertex : _vertices )
			{
				result.add( transform.transform( vertex ) );
			}
		}

		return result;
	}
}
