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
class GLUTriangulator
	implements Triangulator
{
	/**
	 * Flatness used when flattening input shapes.
	 */
	private double _flatness;

	/**
	 * Assumed normal of the shapes being triangulated.
	 */
	private Vector3D _normal;

	/**
	 * Constructs a new triangulator.
	 */
	GLUTriangulator()
	{
		_flatness = 1.0;
		_normal   = Vector3D.INIT;
	}

	public double getFlatness()
	{
		return _flatness;
	}

	public void setFlatness( final double flatness )
	{
		_flatness = flatness;
	}

	public Vector3D getNormal()
	{
		return _normal;
	}

	public void setNormal( final Vector3D normal )
	{
		_normal = normal;
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
		final GLU glu = new GLU();
		final GLUtessellator tessellator = glu.gluNewTess();

		final PathIterator iterator = shape.getPathIterator( null, _flatness );

		switch ( iterator.getWindingRule() )
		{
			case PathIterator.WIND_EVEN_ODD:
				glu.gluTessProperty( tessellator , GLU_TESS_WINDING_RULE , (double)GLU_TESS_WINDING_ODD );
				break;

			case PathIterator.WIND_NON_ZERO:
				glu.gluTessProperty( tessellator , GLU_TESS_WINDING_RULE , (double)GLU_TESS_WINDING_NONZERO );
				break;

			default:
				throw new AssertionError( "Illegal winding rule: " + iterator.getWindingRule() );
		}

		final Vector3D normal = _normal;
		if ( normal != Vector3D.INIT )
		{
			glu.gluTessNormal( tessellator , normal.x , normal.y , normal.z );
		}

		final TriangulationImpl    triangulation        = new TriangulationImpl();
		final TriangulationBuilder triangulationBuilder = new TriangulationBuilder( triangulation );

		glu.gluTessCallback( tessellator , GLU_TESS_BEGIN   , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_VERTEX  , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_END     , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_ERROR   , triangulationBuilder );
		glu.gluTessCallback( tessellator , GLU_TESS_COMBINE , triangulationBuilder );

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

		return triangulationBuilder.getTriangulation();
	}

	/**
	 * Builds the list of triangles for a {@link TriangulationImpl} from the
	 * callbacks it receives from a GLU tessellator.
	 */
	private static class TriangulationBuilder
		extends GLUtessellatorCallbackAdapter
	{
		/**
		 * Triangulation being built.
		 */
		private final TriangulationImpl _triangulation;

		/**
		 * Stores up to two vertex indices gathered from previous events.
		 * Elements that don't contain a vertex index are set to '-1'.
		 *
		 * @see     #addToVertexBuffer(int)
		 */
		private final int[] _vertexBuffer;

		/**
		 * Specifies the type of primitive represented by vertices presented
		 * between calls to {@link #begin(int)} and {@link #end()}.
		 */
		private int _type;

		/**
		 * Indicates whether triangles should be flipped.
		 */
		private boolean _flip;

		/**
		 * Constructs a new triangulation builder that operates on the given
		 * triangulation.
		 *
		 * @param   triangulation   Triangulation being built.
		 */
		TriangulationBuilder( final TriangulationImpl triangulation )
		{
			_triangulation = triangulation;
			_vertexBuffer  = new int[ 2 ];
			_type          = -1;
			_flip          = false;
		}

		/**
		 * Returns the triangulation.
		 *
		 * @return  Triangulation.
		 */
		public Triangulation getTriangulation()
		{
			return _triangulation;
		}

		public void begin( final int type )
		{
			_type = type;
			_flip = false;

			final int[] vertexBuffer = _vertexBuffer;
			vertexBuffer[ 0 ] = -1;
			vertexBuffer[ 1 ] = -1;
		}

		public void vertex( final Object data )
		{
			final int vertexIndex = (Integer)data;

			if ( !addToVertexBuffer( vertexIndex ) )
			{
				final int[] triangle = getTriangle( vertexIndex );
				if ( isTriangle( triangle ) )
				{
					_triangulation.addTriangle( triangle );
				}

				switch ( _type )
				{
					case GL_TRIANGLE_FAN:
						_vertexBuffer[ 1 ] = vertexIndex;
						break;

					case GL_TRIANGLE_STRIP:
						_vertexBuffer[ 0 ] = _vertexBuffer[ 1 ];
						_vertexBuffer[ 1 ] = vertexIndex;
						_flip = !_flip;
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

		public void combine( final double[] coords , final Object[] vertexData , final float[] weight , final Object[] output )
		{
			if (                                  !vertexData[ 0 ].equals( vertexData[ 1 ] )   ||
			     ( ( vertexData[ 1 ] != null ) && !vertexData[ 0 ].equals( vertexData[ 1 ] ) ) ||
			     ( ( vertexData[ 2 ] != null ) && !vertexData[ 0 ].equals( vertexData[ 2 ] ) ) )
			{
				final int combinedIndex = _triangulation.addVertex( Vector3D.INIT.set( coords[ 0 ], coords[ 1 ], coords[ 2 ] ) );
				output[ 0 ] = Integer.valueOf( combinedIndex );
			}
		}

		public void end()
		{
			_type = -1;
		}

		public void error( final int errorCode )
		{
			final GLU glu = new GLU();

			String errorString;
			try
			{
				errorString = glu.gluErrorString( errorCode );
			}
			catch ( Exception e )
			{
				errorString = "unknown error: " + errorCode;
			}

			throw new RuntimeException( errorString );
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
			return _flip ? new int[] { vertexBuffer[ 0 ] , vertexBuffer[ 1 ] , vertexIndex }
			             : new int[] { vertexBuffer[ 1 ] , vertexBuffer[ 0 ] , vertexIndex };
		}

		/**
		 * Returns whether the given vertex triplet forms a triangle, as opposed
		 * to a line or point.
		 *
		 * @param   triplet     Vertex indices to be evaluated.
		 *
		 * @return  <code>true</code> if the vertices represent a triangle;
		 *          <code>false</code> otherwise.
		 */
		private boolean isTriangle( final int[] triplet )
		{
			final List<Vector3D> vertices = _triangulation._vertices;
			final Vector3D vertex0 = vertices.get( triplet[ 0 ] );
			final Vector3D vertex1 = vertices.get( triplet[ 1 ] );
			final Vector3D vertex2 = vertices.get( triplet[ 2 ] );

			final Vector3D edge01 = vertex1.minus( vertex0 );
			final Vector3D edge02 = vertex2.minus( vertex0 );

			// Two edges of a triangle can't be co-linear.
			return !Vector3D.INIT.equals( Vector3D.cross( edge01 , edge02 ) );
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

	/**
	 * Basic implementation of a triangulation result.
	 */
	private static class TriangulationImpl implements Triangulation
	{
		/**
		 * Vertex indices of the triangles that the triangulation consists of.
		 */
		private final List<int[]> _triangles;

		/**
		 * Vertex coordinates used in the triangulation.
		 */
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
			int result = _vertices.indexOf( vertex );
			if ( result == -1 )
			{
				_vertices.add( vertex );
				result = _vertices.size() - 1;
			}
			return result;
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
