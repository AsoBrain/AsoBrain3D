/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.geom;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import static javax.media.opengl.GL.*;
import javax.media.opengl.glu.*;
import static javax.media.opengl.glu.GLU.*;

import ab.j3d.*;
import ab.j3d.geom.ShapeTools.*;
import ab.j3d.geom.Triangulation.*;
import ab.j3d.geom.Triangulation.Primitive.*;

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
		_normal = Vector3D.POSITIVE_Z_AXIS;
	}

	@Override
	public double getFlatness()
	{
		return _flatness;
	}

	@Override
	public void setFlatness( final double flatness )
	{
		_flatness = flatness;
	}

	@Override
	public Vector3D getNormal()
	{
		return _normal;
	}

	@Override
	public void setNormal( final Vector3D normal )
	{
		_normal = normal;
	}

	@Override
	public Triangulation triangulate( final Shape shape )
	{
		final GLU glu = new GLU();
		final GLUtessellator tessellator = glu.gluNewTess();

		final PathIterator iterator = shape.getPathIterator( null , _flatness );

		switch ( iterator.getWindingRule() )
		{
			case PathIterator.WIND_EVEN_ODD:
				glu.gluTessProperty( tessellator, GLU_TESS_WINDING_RULE, (double)GLU_TESS_WINDING_ODD );
				break;

			case PathIterator.WIND_NON_ZERO:
				glu.gluTessProperty( tessellator, GLU_TESS_WINDING_RULE, (double)GLU_TESS_WINDING_NONZERO );
				break;

			default:
				throw new AssertionError( "Illegal winding rule: " + iterator.getWindingRule() );
		}

		final Vector3D normal = _normal;
		if ( normal == null )
		{
			throw new IllegalStateException( "must have normal" );
		}

		final BasicTriangulation triangulation = new BasicTriangulation();
		final TriangulationBuilder triangulationBuilder = new TriangulationBuilder( triangulation );

		glu.gluTessCallback( tessellator, GLU_TESS_BEGIN  , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_VERTEX , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_END    , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_ERROR  , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_COMBINE, triangulationBuilder );

		glu.gluBeginPolygon( tessellator );

		final ShapeClass shapeClass = ShapeTools.getShapeClass( shape );
		if ( shapeClass.isCounterClockwise() )
		{
			glu.gluTessNormal( tessellator, normal.x, normal.y, normal.z );
		}
		else
		{
			glu.gluTessNormal( tessellator, -normal.x, -normal.y, -normal.z );
		}

		createContour( triangulation, glu, tessellator, false, iterator );

		glu.gluEndPolygon( tessellator );

		return triangulationBuilder.getTriangulation();
	}

	@Override
	public Triangulation triangulate( final Shape positive, final Iterable<? extends Shape> negative )
	{
		final BasicTriangulation triangulation = new BasicTriangulation();
		final TriangulationBuilder triangulationBuilder = new TriangulationBuilder( triangulation );

		final Vector3D normal = _normal;
		if ( normal == null )
		{
			throw new IllegalStateException( "must have normal" );
		}

		final GLU glu = new GLU();

		final GLUtessellator tessellator = glu.gluNewTess();
		glu.gluTessProperty( tessellator, GLU_TESS_WINDING_RULE, (double)GLU_TESS_WINDING_ODD );
		glu.gluTessCallback( tessellator, GLU_TESS_BEGIN  , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_VERTEX , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_END    , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_ERROR  , triangulationBuilder );
		glu.gluTessCallback( tessellator, GLU_TESS_COMBINE, triangulationBuilder );

		glu.gluBeginPolygon( tessellator );

		ShapeClass shapeClass = ShapeTools.getShapeClass( positive );
		boolean normalFlipped = shapeClass.isClockwise();
		if ( normalFlipped )
		{
			glu.gluTessNormal( tessellator, -normal.x, -normal.y, -normal.z );
		}
		else
		{
			glu.gluTessNormal( tessellator, normal.x, normal.y, normal.z );
		}

		boolean contourStarted = createContour( triangulation, glu, tessellator, false, positive.getPathIterator( null, _flatness ) );

		glu.gluTessNormal( tessellator, -normal.x, -normal.y, -normal.z );
		for ( final Shape shape : negative )
		{
			shapeClass = ShapeTools.getShapeClass( shape );

			normalFlipped = shapeClass.isCounterClockwise();
			if ( normalFlipped )
			{
				glu.gluTessNormal( tessellator, -normal.x, -normal.y, -normal.z );
			}
			else
			{
				glu.gluTessNormal( tessellator, normal.x, normal.y, normal.z );
			}

			contourStarted = createContour( triangulation, glu, tessellator, contourStarted, shape.getPathIterator( null, _flatness ) );
		}

		glu.gluEndPolygon( tessellator );

		return triangulationBuilder.getTriangulation();
	}

	/**
	 * Create a contour from a {@link PathIterator}. The iterator is only
	 * allowed to produce line segments.
	 *
	 * @param   glu                 Provides access to the OpenGL Utility Library (GLU).
	 * @param   tessellator         Tesselator to use.
	 * @param   contourWasStarted   Was a contour started by previous call.
	 * @param   triangulation       Target triangulation.
	 * @param   pathIterator        Path iterator to create contour from.
	 *
	 * @return  <code>true</code> if a contour was started;
	 *          <code>false</code> if no contour was started.
	 */
	private static boolean createContour( final BasicTriangulation triangulation, final GLU glu, final GLUtessellator tessellator, final boolean contourWasStarted, final PathIterator pathIterator )
	{
		final double[] coords = new double[ 6 ];
		final double[] moveCoords = new double[ 6 ];

		boolean contourStarted = contourWasStarted;
		boolean moved = true;

		while ( !pathIterator.isDone() )
		{
			final int type = pathIterator.currentSegment( coords );

			switch ( type )
			{
				case PathIterator.SEG_MOVETO:
				{
					moveCoords[ 0 ] = coords[ 0 ];
					moveCoords[ 1 ] = coords[ 1 ];
					moved = true;
					break;
				}

				case PathIterator.SEG_CLOSE:
					break;

				case PathIterator.SEG_LINETO:
				{
					if ( moved )
					{
						if ( contourStarted )
						{
							glu.gluTessEndContour( tessellator );
							glu.gluTessBeginContour( tessellator );
						}

						final int vertexIndex = triangulation.addVertex( Vector3D.INIT.set( moveCoords[ 0 ], moveCoords[ 1 ], 0.0 ) );
						glu.gluTessVertex( tessellator, moveCoords, 0, Integer.valueOf( vertexIndex ) );

						contourStarted = true;
						moved = false;
					}

					final int vertexIndex = triangulation.addVertex( Vector3D.INIT.set( coords[ 0 ], coords[ 1 ], 0.0 ) );
					coords[ 2 ] = 0.0;
					glu.gluTessVertex( tessellator, coords, 0, Integer.valueOf( vertexIndex ) );
					break;
				}

				default:
					throw new AssertionError( "Unexpected segment type for flattened path: " + type );
			}

			pathIterator.next();
		}

		return contourStarted;
	}

	/**
	 * Builds the list of triangles for a {@link BasicTriangulation} from the
	 * callbacks it receives from a GLU tessellator.
	 */
	private static class TriangulationBuilder
		extends GLUtessellatorCallbackAdapter
	{
		/**
		 * Triangulation being built.
		 */
		private final BasicTriangulation _triangulation;

		/**
		 * Specifies the type of primitive represented by vertices presented
		 * between calls to {@link #begin(int)} and {@link #end()}.
		 */
		private Type _type;

		/**
		 * Stores vertices of the primitive that is currently being built.
		 */
		private int[] _vertexBuffer;

		/**
		 * Current number of vertices in {@link #_vertexBuffer}.
		 */
		private int _vertexCount;

		/**
		 * Constructs a new triangulation builder that operates on the given
		 * triangulation.
		 *
		 * @param   triangulation   Triangulation being built.
		 */
		TriangulationBuilder( final BasicTriangulation triangulation )
		{
			_triangulation = triangulation;
			_type = Type.TRIANGLES;
			_vertexBuffer = new int[ 8 ];
			_vertexCount = 0;
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

		@Override
		public void begin( final int glType )
		{
			final Type type;

			switch ( glType )
			{
				case GL_TRIANGLES:
					type = Type.TRIANGLES;
					break;

				case GL_TRIANGLE_STRIP:
					type = Type.TRIANGLE_STRIP;
					break;

				case GL_TRIANGLE_FAN:
					type = Type.TRIANGLE_FAN;
					break;

				default:
					throw new AssertionError( "Unexpected type: " + _type );
			}

			_type = type;
			_vertexCount = 0;
		}

		@Override
		public void vertex( final Object data )
		{
			int[] vertexBuffer = _vertexBuffer;
			final int vertexCount = _vertexCount;

			if ( vertexCount == vertexBuffer.length )
			{
				vertexBuffer = Arrays.copyOf( vertexBuffer, vertexCount * 2 );
				_vertexBuffer = vertexBuffer;
			}

			vertexBuffer[ vertexCount ] = (Integer)data;
			_vertexCount = vertexCount + 1;
		}

		@Override
		public void combine( final double[] coords, final Object[] vertexData, final float[] weight, final Object[] output )
		{
			output[ 0 ] = Integer.valueOf( _triangulation.addVertex( new Vector3D( coords[ 0 ], coords[ 1 ], coords[ 2 ] ) ) );
		}

		@Override
		public void end()
		{
			_triangulation.addPrimitive( new Primitive( _type, Arrays.copyOf( _vertexBuffer, _vertexCount ) ) );
			_vertexCount = 0;
		}

		@Override
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
	}
}
