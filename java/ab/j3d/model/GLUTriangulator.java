/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static javax.media.opengl.GL.*;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.glu.GLU.*;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

/**
 * Implements the {@link Triangulator} interface using the tesselator provided
 * by GLU.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class GLUTriangulator
	implements Triangulator
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = GLUTriangulator.class.getName();

	// @FIXME comment
	private double _flatness;

	private boolean _normalFlipping;

	/**
	 * Constructs a new triangulator.
	 *
	 * @param   flatness    @FIXME comment
	 */
	public GLUTriangulator( final double flatness )
	{
		_flatness = flatness;
	}

	public boolean isNormalFlipping()
	{
		return _normalFlipping;
	}

	public void setNormalFlipping( final boolean normalFlipping )
	{
		_normalFlipping = normalFlipping;
	}

	/**
	 * Triangulates the given shape.
	 *
	 * @param   shape   Shape to be triangulated.
	 *
	 * @return  Triangulation result.
	 */
	public Triangulation triangulate( final Shape shape )
	{
		System.out.println( CLASS_NAME + ".triangulate" );
		final long start = System.nanoTime();

		final GLU glu = new GLU();
		final GLUtessellator tessellator = glu.gluNewTess();

		final TriangulationImpl    triangulation        = new TriangulationImpl();
		final TriangulationBuilder triangulationBuilder = new TriangulationBuilder( triangulation );

		glu.gluTessCallback( tessellator , GLU_TESS_BEGIN  , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_VERTEX , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_END    , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_ERROR  , triangulationBuilder );

		final PathIterator iterator = shape.getPathIterator( null, _flatness );

		glu.gluBeginPolygon( tessellator );

		final double[] coords       = new double[ 6 ];
		final double[] vertexCoords = new double[ 3 ];

		boolean insideContour = false;
		while ( !iterator.isDone() )
		{
			final int type = iterator.currentSegment( coords );

			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
				{
					if ( insideContour )
					{
						glu.gluTessEndContour( tessellator );
						glu.gluTessBeginContour( tessellator );
					}

					insideContour = true;

					final int vertexIndex = triangulation.addVertex( Vector3D.INIT.set( coords[ 0 ] , coords[ 1 ] , 0.0 ) );
					vertexCoords[ 0 ] = coords[ 0 ];
					vertexCoords[ 1 ] = coords[ 1 ];
					// vertexCoords[ 2 ] = 0.0; (implicitly zero)
					glu.gluTessVertex( tessellator , vertexCoords , 0 , Integer.valueOf( vertexIndex ) );
					break;
				}

				case PathIterator.SEG_CLOSE:
					break;

				case PathIterator.SEG_LINETO:
				{
					final int vertexIndex = triangulation.addVertex( Vector3D.INIT.set( coords[ 0 ] , coords[ 1 ] , 0.0 ) );
					vertexCoords[ 0 ] = coords[ 0 ];
					vertexCoords[ 1 ] = coords[ 1 ];
					// vertexCoords[ 2 ] = 0.0; (implicitly zero)
					glu.gluTessVertex( tessellator , vertexCoords , 0 , Integer.valueOf( vertexIndex ) );
					break;
				}

				default:
					throw new AssertionError( "Unexpected segment type for flattened path: " + type );
			}

			iterator.next();
		}

		glu.gluEndPolygon( tessellator );

		final long end = System.nanoTime();
		System.out.println( " - " + triangulation._vertices.size() + " vertices, " + triangulation._triangles.size() + " triangles in " + ( (double)( ( end - start ) / 100000L ) / 10.0 ) + " ms" );

		return triangulationBuilder.getTriangulation();
	}

	private class TriangulationBuilder
		extends GLUtessellatorCallbackAdapter
	{
		private final TriangulationImpl _triangulation;

		private final int[] _vertexBuffer;

		private int _type;

		TriangulationBuilder( final TriangulationImpl triangulation )
		{
			_triangulation = triangulation;
			_vertexBuffer  = new int[ 2 ];
			_type          = -1;
		}

		public Triangulation getTriangulation()
		{
			return _triangulation;
		}

		public void begin( final int type )
		{
			_type = type;

			final int[] vertexBuffer = _vertexBuffer;
			vertexBuffer[ 0 ] = -1;
			vertexBuffer[ 1 ] = -1;
		}

		public void vertex( final Object data )
		{
			final int vertexIndex = (Integer)data;

			if ( !addToVertexBuffer( vertexIndex ) )
			{
				_triangulation.addTriangle( getTriangle( vertexIndex ) );

				switch ( _type )
				{
					case GL_TRIANGLE_FAN:
						_vertexBuffer[ 1 ] = vertexIndex;
						break;

					case GL_TRIANGLE_STRIP:
						_vertexBuffer[ 0 ] = _vertexBuffer[ 1 ];
						_vertexBuffer[ 1 ] = vertexIndex;
						break;

					case GL_TRIANGLES:
						_vertexBuffer[ 0 ] = -1;
						_vertexBuffer[ 1 ] = -1;
						break;

					default:
						throw new AssertionError( "Unexpected type: " + _type );
				}
			}
		}

		public void end()
		{
			_type = -1;
		}

		public void error( final int errorCode )
		{
			final GLU glu = new GLU();
			throw new RuntimeException( glu.gluErrorString( errorCode ) );
		}

		/**
		 * Returns the triangle formed by the two vertices in the vertex buffer
		 * and the given vertex.
		 *
		 * @param   vertexIndex     Vertex index of the third vertex of the
		 *                          triangle.
		 * @return  Vertex indices of the corners of the triangle.
		 */
		private int[] getTriangle( final int vertexIndex )
		{
			final int[] vertexBuffer = _vertexBuffer;
			return _normalFlipping ? new int[] { vertexBuffer[ 0 ] , vertexBuffer[ 1 ] , vertexIndex }
			                       : new int[] { vertexIndex , vertexBuffer[ 1 ] , vertexBuffer[ 0 ] };
		}

		/**
		 * Adds the given vertex index to the vertex buffer if it's not full.
		 *
		 * @param   vertexIndex     Vertex index to be added.
		 *
		 * @return  <code>true</code> if the vertex index was added;
		 *          <code>false</code> if the buffer was full.
		 */
		private boolean addToVertexBuffer( final int vertexIndex )
		{
			final boolean result;

			final int[] vertexBuffer = _vertexBuffer;

			if ( vertexBuffer[ 1 ] == -1 )
			{
				if ( vertexBuffer[ 0 ] == -1 )
				{
					vertexBuffer[ 0 ] = vertexIndex;
					result = true;
				}
				else
				{
					vertexBuffer[ 1 ] = vertexIndex;
					result = true;
				}
			}
			else
			{
				result = false;
			}

			return result;
		}
	}

	private static class TriangulationImpl implements Triangulation
	{
		private final List<int[]> _triangles;

		private final List<Vector3D> _vertices;

		/**
		 * Constructs a new triangulation.
		 */
		TriangulationImpl()
		{
			_triangles = new ArrayList<int[]>();
			_vertices  = new ArrayList<Vector3D>();
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
			_vertices.add( vertex );
			return _vertices.size() - 1;
		}

		/**
		 * Adds the given triangle to the triangulation.
		 *
		 * @param   triangle    Triangle to be added, specified as three vertex
		 *                      indices (see {@link #getTriangles()}).
		 */
		public void addTriangle( final int[] triangle )
		{
			_triangles.add( triangle );
		}

		public Collection<int[]> getTriangles()
		{
			return Collections.unmodifiableList( _triangles );
		}

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
					result.add( transform.multiply( vertex ) );
				}
			}

			return result;
		}
	}
}
