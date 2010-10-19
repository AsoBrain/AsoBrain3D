/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
import java.util.List;

import ab.j3d.*;
import ab.j3d.geom.ShapeTools.*;
import ab.j3d.geom.tessellator.*;
import static ab.j3d.geom.tessellator.GLUtessellator.*;
import org.jetbrains.annotations.*;

/**
 * Implements the {@link Tessellator} interface using the tessellator provided
 * by GLU.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class GLUTessellator
	implements Tessellator
{
	/**
	 * Flatness used when flattening input shapes.
	 */
	private double _flatness;

	/**
	 * Assumed normal of the shapes being tessellated.
	 */
	@NotNull
	private Vector3D _normal;

	/**
	 * Constructs a new tessellator.
	 */
	public GLUTessellator()
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

	@NotNull
	@Override
	public Vector3D getNormal()
	{
		return _normal;
	}

	@Override
	public void setNormal( @NotNull final Vector3D normal )
	{
		_normal = normal;
	}

	@Override
	public void tessellate( @NotNull final TessellationBuilder tessellationBuilder, @NotNull final Shape shape )
	{
		final PathIterator iterator = shape.getPathIterator( null, _flatness );

		final int gluWindingRule;

		switch ( iterator.getWindingRule() )
		{
			case PathIterator.WIND_EVEN_ODD:
				gluWindingRule = GLU_TESS_WINDING_ODD;
				break;

			case PathIterator.WIND_NON_ZERO:
				gluWindingRule = GLU_TESS_WINDING_NONZERO;
				break;

			default:
				throw new AssertionError( "Illegal winding rule: " + iterator.getWindingRule() );
		}

		final Collection<Contour> contours = new LinkedList<Contour>();
		Contour.addContours( contours, iterator, false, false );

		if ( !contours.isEmpty() )
		{
			final GLUtessellator gluTessellator = createTessellator( tessellationBuilder, gluWindingRule );

			gluTessellator.gluBeginPolygon();
			tessellateContours( gluTessellator, false, tessellationBuilder, contours, false );
			gluTessellator.gluEndPolygon();
		}
	}

	@Override
	public void tessellate( @NotNull final TessellationBuilder tessellationBuilder, @NotNull final Shape positive, @NotNull final Collection<? extends Shape> negative )
	{
		if ( negative.isEmpty() )
		{
			tessellate( tessellationBuilder, positive );
		}
		else
		{
			final List<Contour> positiveContours = Contour.createContours( positive, _flatness, true, false );

			final List<Contour> negativeContours = new ArrayList<Contour>( negative.size() );
			for ( final Shape shape : negative )
			{
				Contour.addContours( negativeContours, shape.getPathIterator( null, _flatness ), true, false );
			}

			tessellate( tessellationBuilder, positiveContours, negativeContours );
		}
	}

	/**
	 * Tessellates a combination of <code>positive</code> and
	 * <code>negative</code> outlines. The result is the difference between the
	 * two.
	 *
	 * @param   tessellationBuilder     Builder of tessellation result.
	 * @param   positive                Positive geometry contours.
	 * @param   negative                Negative geometry contours.
	 */
	public void tessellate( @NotNull final TessellationBuilder tessellationBuilder, @NotNull final List<Contour> positive, @NotNull final List<Contour> negative )
	{
		if ( !tessellateSimpleConvexIfPossible( tessellationBuilder, positive, negative ) )
		{
			final GLUtessellator gluTessellator = createTessellator( tessellationBuilder, GLU_TESS_WINDING_POSITIVE );

			final boolean reverse = _normal.z < 0.0;

			gluTessellator.gluBeginPolygon();
			tessellateContours( gluTessellator, false, tessellationBuilder, positive, reverse );

			if ( !negative.isEmpty() )
			{
				tessellateContours( gluTessellator, true, tessellationBuilder, negative, !reverse );
			}

			gluTessellator.gluEndPolygon();
		}
	}

	/**
	 * Helper method to handle trivial tessellation case(s) for which no
	 * full blown tessellation algorithm is needed.
	 *
	 * @param   tessellationBuilder     Builder of tessellation result.
	 * @param   positive                Positive geometry contours.
	 * @param   negative                Negative geometry contours.
	 *
	 * @return  <code>true</code> if tessellation case is performed;
	 *          <code>false</code> if the geometry has to be tessellated.
	 */
	protected static boolean tessellateSimpleConvexIfPossible( @NotNull final TessellationBuilder tessellationBuilder, @NotNull final List<Contour> positive, @NotNull final List<Contour> negative )
	{
		final boolean result;

		if ( positive.isEmpty() )
		{
			result = true;
		}
		else if ( negative.isEmpty() && ( positive.size() == 1 ) )
		{
			final Contour contour = positive.get( 0 );
			final ShapeClass shapeClass = contour.getShapeClass();

			result = shapeClass.isConvex();
			if ( result )
			{
				final List<Contour.Point> points = contour._points;
				final int pointCount = points.size();
				final int lastPoint = pointCount - 1;

				final int[] outline = new int[ pointCount ];
				final boolean reverseOutline = shapeClass.isClockwise();

				for ( int i = 0; i < pointCount; i++ )
				{
					final Contour.Point point = points.get( i );
					final int vertexIndex = tessellationBuilder.addVertex( point.x, point.y, 0.0 );
					outline[ reverseOutline ? lastPoint - i : i ] = vertexIndex;
				}

				tessellationBuilder.addOutline( outline );
				tessellationBuilder.addPrimitive( new TriangleFan( outline ) );
			}
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Create a tessellator a {@link PathIterator}. The iterator is only
	 * allowed to produce line segments.
	 *
	 * @param   tessellationBuilder     Builder of tessellation result.
	 * @param   gluWindingRule          Winding rule to use.
	 *
	 * @return  An initialized {@link GLUTessellator}.
	 */
	private GLUtessellator createTessellator( final TessellationBuilder tessellationBuilder, final int gluWindingRule )
	{
		final GLUtessellator gluTessellator = GLUtessellatorImpl.gluNewTess();
		gluTessellator.gluTessProperty( GLU_TESS_WINDING_RULE, (double) gluWindingRule );

		final GLUtessellatorCallback proxy = new TessellationBuilderProxy( tessellationBuilder );
		gluTessellator.gluTessCallback( GLU_TESS_BEGIN, proxy );
		gluTessellator.gluTessCallback( GLU_TESS_VERTEX, proxy );
		gluTessellator.gluTessCallback( GLU_TESS_END, proxy );
		gluTessellator.gluTessCallback( GLU_TESS_ERROR, proxy );
		gluTessellator.gluTessCallback( GLU_TESS_COMBINE, proxy );

		final Vector3D normal = _normal;
		gluTessellator.gluTessNormal( normal.x, normal.y, normal.z );

		return gluTessellator;
	}

	/**
	 * Tessellate contours using a {@link GLUTessellator}.
	 *
	 * @param   gluTessellator      GLU tessellator to use.
	 * @param   wasContourStarted   Indicates that a contour was previously
	 *                              started prior to calling this method.
	 * @param   tessellationBuilder  Builder of tessellation result.
	 * @param   contours            Contours to tessellate.
	 * @param   reverse             Reverse vertex order of contours.
	 *
	 * @return  <code>true</code> if a contour was started.
	 */
	private static boolean tessellateContours( final GLUtessellator gluTessellator, final boolean wasContourStarted, final TessellationBuilder tessellationBuilder, final Iterable<Contour> contours, final boolean reverse )
	{
		boolean contourStarted = wasContourStarted;
		final double[] coords = new double[ 3 ];

		for ( final Contour contour : contours )
		{
			if ( contourStarted )
			{
				gluTessellator.gluTessEndContour();
				gluTessellator.gluTessBeginContour();
			}

			final List<Contour.Point> contourPoints = contour._points;
			final ShapeClass shapeClass = contour.getShapeClass();
			final boolean reverseOutline = reverse ^ shapeClass.isClockwise(); /* always create counter-clockwise outlines */
			final int pointCount = contourPoints.size();
			final int lastPoint = pointCount - 1;
			final int[] outline = new int[ pointCount ];

			for ( int i = 0; i < pointCount; i++ )
			{
				final Contour.Point point = contourPoints.get( reverse ? lastPoint - i : i );
				coords[ 0 ] = point.x;
				coords[ 1 ] = point.y;

				final int vertexIndex = tessellationBuilder.addVertex( point.x, point.y, 0.0 );
				gluTessellator.gluTessVertex( coords, 0, Integer.valueOf( vertexIndex ) );
				outline[ reverseOutline ? lastPoint - i : i ] = vertexIndex;
			}

			tessellationBuilder.addOutline( outline );
			contourStarted = true;
		}

		return contourStarted;
	}

	/**
	 * Builds the list of triangles for a {@link TessellationBuilder} from the
	 * callbacks it receives from a GLU tessellator.
	 */
	private static class TessellationBuilderProxy
		extends GLUtessellatorCallbackAdapter
	{
		/**
		 * Triangulation being built.
		 */
		private final TessellationBuilder _tessellationBuilder;

		/**
		 * Specifies the type of primitive represented by vertices presented
		 * between calls to {@link #begin(int)} and {@link #end()}.
		 */
		private int _type;

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
		 * @param   tessellationBuilder   Triangulation being built.
		 */
		TessellationBuilderProxy( final TessellationBuilder tessellationBuilder )
		{
			_tessellationBuilder = tessellationBuilder;
			_type = GL_TRIANGLES;
			_vertexBuffer = new int[ 8 ];
			_vertexCount = 0;
		}

		@Override
		public void begin( final int type )
		{
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
			output[ 0 ] = Integer.valueOf( _tessellationBuilder.addVertex( coords[ 0 ], coords[ 1 ], coords[ 2 ] ) );
		}

		@Override
		public void end()
		{
			final TessellationBuilder tessellationBuilder = _tessellationBuilder;
			final int[] vertices = Arrays.copyOf( _vertexBuffer, _vertexCount );

			switch ( _type )
			{
				case GL_TRIANGLES:
					tessellationBuilder.addPrimitive( new TriangleList( vertices ) );
					break;

				case GL_TRIANGLE_STRIP:
					tessellationBuilder.addPrimitive( new TriangleStrip( vertices ) );
					break;

				case GL_TRIANGLE_FAN:
					tessellationBuilder.addPrimitive( new TriangleFan( vertices ) );
					break;

				default:
					throw new AssertionError( "Unexpected type: " + _type );
			}

			_vertexCount = 0;
		}

		@Override
		public void error( final int errorCode )
		{
			final String errorString;

			switch ( errorCode )
			{
				case GLU_INVALID_ENUM:
					errorString = "invalid enumerant";
					break;

				case GLU_OUT_OF_MEMORY:
					errorString = "out of memory";
					break;

				case GLU_INVALID_VALUE:
					errorString = "invalid value";
					break;

				case GLU_TESS_COORD_TOO_LARGE:
					errorString = "tess coord too large";
					break;

				case GLU_TESS_MISSING_BEGIN_POLYGON:
					errorString = "missing begin polygon";
					break;

				case GLU_TESS_MISSING_BEGIN_CONTOUR:
					errorString = "missing begin contour";
					break;

				case GLU_TESS_MISSING_END_CONTOUR:
					errorString = "missing end contour";
					break;

				case GLU_TESS_MISSING_END_POLYGON:
					errorString = "missing end polygon";
					break;

				case GLU_TESS_NEED_COMBINE_CALLBACK:
					errorString = "need combine callback";
					break;

				default:
					errorString = "unknown error code: " + errorCode;
			}

			throw new RuntimeException( errorString );
		}
	}
}
