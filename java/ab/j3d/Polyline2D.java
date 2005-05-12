/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2001-2005
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
package ab.j3d;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a polyline in 2D.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Polyline2D
{
	/**
	 * Type of poyline: unknown.
	 *
	 * This special value is used to indicate that the polyline type was not
	 * yet determined.
	 *
	 * @see     #getType
	 */
	public static final int UNKNOWN = -1;

	/**
	 * Type of poyline: void.
	 *
	 * Polyline without any points.
	 *
	 * @see     #getType
	 */
	public static final int VOID = 0;

	/**
	 * Type of poyline: point.
	 *
	 * Polyline with single point.
	 *
	 * @see     #getType
	 */
	public static final int POINT = 1;

	/**
	 * Type of poyline: line.
	 *
	 * Polyline with one linear segment between two points.
	 *
	 * @see     #PATH
	 * @see     #getType
	 */
	public static final int LINE = 2;

	/**
	 * Type of poyline: path.
	 *
	 * Open polyline with at least two points not constituting a line.
	 *
	 * @see     #LINE
	 * @see     #getType
	 */
	public static final int PATH = 3;

	/**
	 * Type of poyline: convex.
	 *
	 * Closed polyline with only convex angles (any straight line of infinite
	 * length that intersects the polyline will intersect it twice and twice
	 * only).
	 *
	 * @see     #CONCAVE
	 * @see     #getType
	 */
	public static final int CONVEX  = 4;

	/**
	 * Type of poyline: concave.
	 *
	 * Any closed polyline that is not convex.
	 *
	 * @see     #CONVEX
	 * @see     #getType
	 */
	public static final int CONCAVE = 5;

	/**
	 * Collection of control points that describe the polyline.
	 */
	private final List _points;

	/**
	 * Cache for line's type. This value will reset when points
	 * are added/removed.
	 */
	private int _typeCache;

	/**
	 * Cache for line's bounds. This value will reset when points
	 * are added/removed.
	 */
	private Bounds2D _boundsCache;

	/**
	 * Cache for enclosed rectangel of a convex shape.
	 *
	 * @see     #getEnclosedRectangle
	 */
	private Bounds2D _encloseCache;

	/**
	 * Constructor for polyline.
	 *
	 * @param   capacity    Capacity of polyline (number of control points).
	 */
	public Polyline2D( final int capacity )
	{
		_points       = new ArrayList( capacity );
		_typeCache    = UNKNOWN;
		_boundsCache  = null;
		_encloseCache = null;
	}

	/**
	 * Constructor for polyline.
	 */
	public Polyline2D()
	{
		this( 5 );
	}

	/**
	 * Clone constructor.
	 *
	 * @param   original    Polyline to clone.
	 */
	public Polyline2D( final Polyline2D original )
	{
		final List orignalPoints = original._points;
		final int  pointCount    = orignalPoints.size();
		final List clonedPoints  = new ArrayList( pointCount );

		for ( int i = 0 ; i < pointCount ; i++ )
			clonedPoints.add( new PolyPoint2D( (PolyPoint2D)orignalPoints.get( i ) ) );

		_points       = clonedPoints;
		_typeCache    = original._typeCache;
		_boundsCache  = original._boundsCache;
		_encloseCache = original._encloseCache;
	}

	/**
	 * Constructor for polyline for rectangular shape based at origin.
	 * <p />
	 * If <code>dx != 0</code> and <code>dy != 0</code>, then the result will be the rectanle:
	 * <pre>
	 *   (0,0) -> (dx,0) -> (dx,dy) -> (0,dy) -> (0,0)
	 * </pre>
	 * <p />
	 * If <code>dx != 0</code> and <code>dy = 0</code>; or
	 * if <code>dx = 0</code> and <code>dy != 0</code>, then the result will be the line:
	 * <pre>
	 *   (0,0) -> (dx,dy)
	 * </pre>
	 * <p />
	 * If <code>dy = 0</code> and <code>dy = 0</code>, then the result will be the point:
	 * <pre>
	 *   (0,0)
	 * </pre>
	 *
	 * @param   dx      Delta-X (may be 0).
	 * @param   dy      Delta-Y (may be 0).
	 */
	public Polyline2D( final double dx , final double dy )
	{
		_points = new ArrayList();

		addStartPoint( 0.0 , 0.0 );
		if ( dx != 0.0 )
		{
			addLineSegment( dx , 0.0 );
			if ( dy != 0.0 )
			{
				addLineSegment( dx , dy );
				addLineSegment( 0.0 , dy );
				close();
			}
		}
		else if ( dy != 0.0 )
		{
			addLineSegment( 0.0 , dy );
		}
	}

	/**
	 * Copy contents from another polyline. All existing data will overwritten.
	 *
	 * @param   original    Polyline to copy.
	 */
	public void copy( final Polyline2D original )
	{
		final List points = _points;
		points.clear();
		points.addAll( original._points ); // use this because there is no 'List.ensureCapacity()' or something
		for ( int i = 0 ; i < points.size() ; i++ )
			points.set( i , new PolyPoint2D( (PolyPoint2D) points.get( i ) ) );

		_typeCache    = original._typeCache;
		_boundsCache  = original._boundsCache;
		_encloseCache = original._encloseCache;
	}


	/**
	 * Clear all points in the polyline.
	 */
	public void clear()
	{
		_points.clear();

		_typeCache    = UNKNOWN;
		_boundsCache  = null;
		_encloseCache = null;
	}

	/**
	 * Add control point to polyline.
	 *
	 * @param   x       X coordinate of control point.
	 * @param   y       Y coordinate of control point.
	 * @param   bulge  Bulge factor (only meaningful for arc segments).
	 */
	private void addImpl( final double x , final double y , final double bulge )
	{
		addImpl( new PolyPoint2D( x , y , bulge ) );
	}

	/**
	 * Add control point to polyline.
	 *
	 * @param   point   Control point.
	 */
	private void addImpl( final PolyPoint2D point )
	{
		_points.add( point );

		_typeCache    = UNKNOWN;
		_boundsCache  = null;
		_encloseCache = null;
	}

	/**
	 * Add start point to polyline.
	 *
	 * @param   x   X coordinate of segment end point.
	 * @param   y   Y coordinate of segment end point.
	 *
	 * @see     #addLineSegment
	 * @see     #addArcSegmentWithAngle
	 * @see     #addArcSegmentWithBulge
	 */
	public void addStartPoint( final double x , final double y )
	{
		if ( getPointCount() != 0 )
			throw new IllegalStateException( "can't add second start point" );

		addImpl( x , y , 0.0 );
	}

	/**
	 * Add line segment to polyline.
	 *
	 * @param   endX   X coordinate of segment end point.
	 * @param   endY   Y coordinate of segment end point.
	 *
	 * @see     #addStartPoint
	 * @see     #addArcSegmentWithAngle
	 * @see     #addArcSegmentWithBulge
	 */
	public void addLineSegment( final double endX , final double endY )
	{
		if ( getPointCount() == 0 )
			throw new IllegalStateException( "can't add segment without start point" );

		addImpl( endX , endY , 0.0 );
	}

	/**
	 * Add arc segment to polyline.
	 *
	 * @param   endX    X coordinate of segment end point.
	 * @param   endY    Y coordinate of segment end point.
	 * @param   angle   Enclosed angle of arc segment (radians).
	 *
	 * @see     #addStartPoint
	 * @see     #addLineSegment
	 * @see     #addArcSegmentWithBulge
	 */
	public void addArcSegmentWithAngle( final double endX , final double endY , final double angle )
	{
		if ( angle == 0.0 )
			throw new IllegalArgumentException( "angle can't be 0.0" );

		addArcSegmentWithBulge( endX , endY , Math.tan( angle / 4.0 ) );
	}

	/**
	 * Add arc segment to polyline.
	 *
	 * @param   endX    X coordinate of segment end point.
	 * @param   endY    Y coordinate of segment end point.
	 * @param   bulge  Bulge factor for arc segment.
	 *
	 * @see     #addStartPoint
	 * @see     #addLineSegment
	 * @see     #addArcSegmentWithAngle
	 */
	public void addArcSegmentWithBulge( final double endX , final double endY , final double bulge )
	{
		if ( bulge == 0.0 )
			throw new IllegalArgumentException( "bulge can't be 0.0" );

		if ( getPointCount() == 0 )
			throw new IllegalStateException( "can't add segment without start point" );

		addImpl( endX , endY , bulge );
	}

	/**
	 * Add copy of control point (from other polyline) to this polyline.
	 *
	 * @param   point   Control point.
	 *
	 * @throws  NullPointerException if <code>point</code> is <code>null</code>.
	 */
	public void addCopyOf( final PolyPoint2D point )
	{
		addImpl( point.x , point.y , point.bulge );
	}

	/**
	 * Close the polyline (if it was not closed already).
	 */
	public void close()
	{
		final int pointCount = getPointCount();
		if ( pointCount > 2 )
		{
			final PolyPoint2D first = getPoint( 0 );
			final PolyPoint2D last  = getPoint( pointCount - 1 );

			if ( ( first.x != last.x ) || ( first.y != last.y ) );
			{
				addImpl( first.x , first.y , 0.0 );

				_typeCache    = UNKNOWN;
				_boundsCache  = null;
				_encloseCache = null;
			}
		}
	}

	/**
	 * Adjust the position of a segment perdendular to the segment direction.
	 * This is used to reduce/enlarge the polyline at one of its segments.
	 *
	 * @param   startIndex      Start index of segment in polyline.
	 * @param   adjustment      Position adjustment of segment.
	 *
	 * @return  <code>true</code> if the adjustment was succesful;
	 *          <code>false</code> if the adjustment failed (it may fail when
	 *          the adjustment changes the structure of the polyline, e.g.
	 *          number of segments).
	 */
	public boolean adjustSegment( final int startIndex , final double adjustment)
	{
		/*
		 * Ignore 0-adjustments.
		 */
		if ( adjustment < 0.001 && adjustment > -0.001 )
			return true;

		final boolean isClosed = isClosed();

		/*
		 * Determin prev/start/end/next index (ignore bad indices);
		 */
		final int maxIndex = getPointCount() - 1;
		if ( startIndex < 0 || startIndex >= maxIndex )
			return false;

		final int endIndex = startIndex + 1;

		final PolyPoint2D prev  = ( startIndex > 0 ) ? getPoint( startIndex - 1 ) : isClosed ? getPoint( maxIndex - 1 ) : null;
		final PolyPoint2D start = getPoint( startIndex );
		final PolyPoint2D end   = getPoint( endIndex );
		final PolyPoint2D next  = ( endIndex < maxIndex ) ? getPoint( endIndex + 1 ) : isClosed ? getPoint( 1 ) : null;

		/*
		 * Determine lenght and direction of segment (ignore 0-length segments).
		 */
		double x = end.x - start.x;
		double y = end.y - start.y;
		double l = Math.sqrt( x * x + y * y );
		if ( l < 0.001 )
			return false;

		final double baseDirX = x / l;
		final double baseDirY = y / l;

		/*
		 * Adjust start point using the previous control point. Assume 90 degree angle if no such point exists.
		 */
		final PolyPoint2D newStart;
		if ( prev != null )
		{
			x = prev.x - start.x;
			y = prev.y - start.y;
			l = Math.sqrt( x * x + y * y );
			if ( l < 0.001 )
				return false;

			final double prevDirX = x / l;
			final double prevDirY = y / l;

			final double cos = prevDirX * baseDirX + prevDirY * baseDirY;
			if ( cos < -0.999 || cos > 0.999 )
				return false;

			final double sin = 1.0 - ( cos * cos );
			double hyp = Math.sqrt( ( adjustment * adjustment ) / sin  );
			if ( hyp > l )
				return false;

			final double f = prevDirX * ( baseDirY + prevDirY ) - prevDirY * ( baseDirX + prevDirX );
			if ( ( f < -0.001 ) ^ ( adjustment > 0 ) )
				hyp = -hyp;

			newStart = new PolyPoint2D( start.x + hyp * prevDirX , start.y + hyp * prevDirY , start.bulge );
		}
		else
		{
			newStart = new PolyPoint2D( start.x - adjustment * baseDirY , start.y + adjustment * baseDirX , start.bulge );
		}

		/*
		 * Adjust end point using the next control point. Assume 90 degree angle if no such point exists.
		 */
		final PolyPoint2D newEnd;

		if ( next != null )
		{
			x = next.x - end.x;
			y = next.y - end.y;
			l = Math.sqrt( x * x + y * y );
			if ( l < 0.001 )
				return false;

			final double nextDirX = x / l;
			final double nextDirY = y / l;

			final double cos = nextDirX * baseDirX + nextDirY * baseDirY;
			if ( cos < -0.999 || cos > 0.999 )
				return false;

			final double sin = 1.0 - ( cos * cos );
			double hyp = Math.sqrt( ( adjustment * adjustment ) / sin  );
			if ( hyp > l )
				return false;

			final double f = nextDirX * ( baseDirY + nextDirY ) - nextDirY * ( baseDirX + nextDirX );
			if ( ( f < -0.001 ) ^ ( adjustment > 0 ) )
				hyp = -hyp;

			newEnd = new PolyPoint2D( end.x + hyp * nextDirX , end.y + hyp * nextDirY , end.bulge );
		}
		else
		{
			newEnd = new PolyPoint2D( end.x - adjustment * baseDirY , end.y + adjustment * baseDirX , end.bulge );
		}

		/*
		 * Set new start and end of polyline segement (clear cached properties).
		 */
		_points.set(  startIndex , newStart );
		_points.set( endIndex , newEnd );

		_typeCache = UNKNOWN;
		_boundsCache = null;
		_encloseCache = null;

		/*
		 * If the polyline was closed, make sure the first and last control point are synchronized.
		 */
		if ( isClosed )
		{
			if ( startIndex == 0 )
				_points.set( maxIndex , newStart );

			if ( endIndex == maxIndex )
				_points.set( 0 , newEnd );
		}

		return true;
	}

	/**
	 * Extrude surface from a poly line segment. The segment must be straight and the start
	 * point can not be the end point of the polyline. The X-axis of the extruded surface will
	 * match the segment direction, the Y-axis will match the origin's Z-axis, and the Z-axis
	 * will be perpendular to the segment. Note that the returned matrix is relative to the
	 * coordinate system of the polyline, so it may need to be transformed to a common coordinate
	 * system.
	 *
	 * @param   index   Index of start point of segment.
	 *
	 * @return  Matrix representing base of extruded surface, or <code>null</code> if no
	 *          extruded surface can be constructed (invalid segment, segment is not
	 *          straight, ....).
	 */
	public Matrix3D extrudeSurface( final int index )
	{
		Matrix3D result = null;

		/*
		 * Check index, then get the two points that define the segment.
		 */
		if ( ( index >= 0 ) && ( index < getPointCount() - 1 ) )
		{
			final PolyPoint2D p1 = getPoint( index );
			final PolyPoint2D p2 = getPoint( index + 1 );

			/*
			 * @FIXME should check here if the segment is straight.
			 */

			/*
			 * Determine directional vector of segment. Skip segment if it has length 0.
			 */
			double dirX = p2.x - p1.x;
			double dirY = p2.y - p1.y;

			final double l = Math.sqrt( dirX * dirX + dirY * dirY );
			if ( l > 0.0 )
			{
				dirX /= l;
				dirY /= l;

				/*
				 * Determine transform to face that is extruded from the segment in
				 * positive Z-direction. The base Z coordinate will match the bottom of the
				 * panel. The face is always rectangular with a width matching the segment
				 * length and a height matching the panel thickness.
				 */
				result = Matrix3D.INIT.set(
					dirX , 0.0 ,  dirY , p1.x ,
					dirY , 0.0 , -dirX , p1.y ,
					 0.0 , 1.0 ,   0.0 ,  0.0 );
			}
		}

		return result;
	}

	/**
	 * Get bounding box of polyline.
	 *
	 * @return  Bounds2D with bounding box for polyline;
	 *          <code>null</code> if the poyline has no bounds (it's void).
	 */
	public synchronized Bounds2D getBounds()
	{
		Bounds2D result = _boundsCache;
		if ( result == null )
		{
			int pointIndex = getPointCount();
			if ( pointIndex != 0 )
			{
				double minX = Double.POSITIVE_INFINITY;
				double minY = Double.POSITIVE_INFINITY;
				double maxX = Double.NEGATIVE_INFINITY;
				double maxY = Double.NEGATIVE_INFINITY;

				while ( --pointIndex >= 0 )
				{
					final PolyPoint2D p = getPoint( pointIndex );

					if ( p.x < minX ) minX = p.x;
					if ( p.y < minY ) minY = p.y;
					if ( p.x > maxX ) maxX = p.x;
					if ( p.y > maxY ) maxY = p.y;
				}

				result = new Bounds2D( minX , minY , maxX , maxY );
				_boundsCache = result;
			}
		}

		return result;
	}

	/**
	 * Get width of bounding box of polyline.
	 *
	 * @return  Width of bounding box of polyline.
	 */
	public double getWidth()
	{
		final Bounds2D bounds = getBounds();
		return ( bounds == null ) ? 0.0 : bounds.getWidth();
	}

	/**
	 * Get height of bounding box of polyline.
	 *
	 * @return  Height of bounding box of polyline.
	 */
	public double getHeight()
	{
		final Bounds2D bounds = getBounds();
		return ( bounds == null ) ? 0.0 : bounds.getHeight();
	}

	/**
	 * Find rectangular shape enclosed by the convex shape defined by this polyline.
	 * FIXME : This method is not 'perfect'. It does find a rectangle that is completely
	 * within the convex, but it can be optimized (speed, memory-use and result).
	 *
	 * This method does not guarantee that returned rectangle is the most optimal
	 * solution.
	 *
	 * THIS IS ONLY VALID FOR POLYLINES DEFINING A 'CONVEX' SHAPE.
	 *
	 * @return  Bounds2D that is enclosed by this polyline.
	 */
	public Bounds2D getEnclosedRectangle()
	{
		/*
		 * Only for convex shapes.
		 */
		if ( getType() != CONVEX )
			throw new RuntimeException( "Polyline2D.getEnclosedRectangle() is for convex shapes only!" );

		if ( _encloseCache == null )
		{
			/*
			 * Get bounds.
			 */
			final Bounds2D bounds = getBounds();
			if ( bounds == null )
				return null;

			/*
			 * Define starting points.
			 */
			double minX = bounds.minX;
			double minY = bounds.minY;
			double maxX = bounds.maxX;
			double maxY = bounds.maxY;

			/*
			 * Amount to move when line is not in shape.
			 */
			final double horizontalStep = ( maxX - minX ) / 30.0;
			final double verticalStep = ( maxY - minY ) / 30.0;

			/*
			 * The four outer points and wether they are intersecting with convex.
			 */
			PolyPoint2D p1 = null;
			PolyPoint2D p2 = null;
			PolyPoint2D p3 = null;
			PolyPoint2D p4 = null;

			boolean p1i = false;
			boolean p2i = false;
			boolean p3i = false;
			boolean p4i = false;

			/*
			 * Enclosed shape is found when all points lay within convex.
			 */
			while ( !( p1i && p2i && p3i && p4i) )
			{
				/*
				 * Check if we missed it.
				 */
				if ( minX > bounds.maxX || maxX < bounds.minX ||
				     minY > bounds.maxY || maxY < bounds.minY )
					throw new RuntimeException( "No enclosed rectangle can be found" );

				/*
				 * If point has changed, create new point and check intersection.
				 */
				if ( p1 == null )
				{
					p1 = new PolyPoint2D( minX , minY , 0.0 );
					p1i = isIntersectingConvex_Point( this , p1 );
				}

				if ( p2 == null )
				{
					p2 = new PolyPoint2D( minX , maxY , 0.0 );
					p2i = isIntersectingConvex_Point( this , p2 );
				}

				if ( p3 == null )
				{
					p3 = new PolyPoint2D( maxX , minY , 0.0 );
					p3i = isIntersectingConvex_Point( this , p3 );
				}

				if ( p4 == null )
				{
					p4 = new PolyPoint2D( maxX , maxY , 0.0 );
					p4i = isIntersectingConvex_Point( this , p4 );
				}

				/*
				 * If one of the points on a side line does not intersect,
				 * the line does not intersect. If the line does not intersect,
				 * move it inwards.
				 */
				if ( !(p1i && p2i) ) { minX += horizontalStep; p1 = null; p2 = null; }
				if ( !(p1i && p3i) ) { minY += verticalStep; p1 = null; p3 = null; }
				if ( !(p3i && p4i) ) { maxX -= horizontalStep; p3 = null; p4 = null; }
				if ( !(p2i && p4i) ) { maxY -= verticalStep; p2 = null; p4 = null; }
			}

			_encloseCache = new Bounds2D( minX , minY , maxX - minX , maxY - minY );
		}

		return _encloseCache;
	}

	/**
	 * Gets intersecting shape between this polyline and an other one.
	 * <p />
	 * This method only delagates all the different types of shapes, it
	 * does no testing itself.
	 *
	 * @param   other   Other shape to get intersection with.
	 *
	 * @return  Shape describing the intersecting shape;
	 *          <code>null</code> if no intersection was found.
	 */
	public Polyline2D getIntersection( final Polyline2D other )
	{
		if ( other == null )
		{
			return null;
		}
		else if ( other == this )
		{
			return this;
		}
		else
		{
			switch ( getType() )
			{
				case VOID:
					return null;

				case POINT:
					return other.contains( getPoint( 0 ) ) ? this : null;

				case LINE:
					switch ( other.getType() )
					{
						case VOID:
							return null;

						case POINT:
							return isIntersectingLine_Point( this , other.getPoint( 0 ) ) ? other : null;

						case LINE:
							return getIntersectionLine_Line( getPoint( 0 ) , getPoint( 1 ) , other.getPoint( 0 ) , other.getPoint( 1 ) );

						case PATH:
							{
								final Bounds2D thisBounds  = getBounds();
								final Bounds2D otherBounds = other.getBounds();
								return thisBounds.intersects( otherBounds ) ? getIntersectionPath_Line( other , getPoint( 0 ) , getPoint( 1 ) ) : null;
							}

						case CONVEX:
							{
								final Bounds2D thisBounds  = getBounds();
								final Bounds2D otherBounds = other.getBounds();
								return thisBounds.intersects( otherBounds ) ? getIntersectionConvex_Line( other , this ) : null;
							}
					}
					break;

				case PATH:
					switch ( other.getType() )
					{
						case VOID:
							return null;

						case POINT:
							return isIntersectingPath_Point( this , other.getPoint( 0 ) ) ? other : null;

						case LINE:
							{
								final Bounds2D thisBounds  = getBounds();
								final Bounds2D otherBounds = other.getBounds();
								return thisBounds.intersects( otherBounds ) ? getIntersectionPath_Line( this , other.getPoint( 0 ) , other.getPoint( 1 ) ) : null;
							}

						case CONVEX:
							{
								final Bounds2D thisBounds  = getBounds();
								final Bounds2D otherBounds = other.getBounds();
								return thisBounds.intersects( otherBounds ) ? getIntersectionConvex_Path( other , this ) : null;
							}

						case PATH:
							{
								final Bounds2D thisBounds  = getBounds();
								final Bounds2D otherBounds = other.getBounds();
								return thisBounds.intersects( otherBounds ) ? getIntersectionPath_Path( this , other ) : null;
							}
					}
					break;

				case CONVEX:
					switch ( other.getType() )
					{
						case VOID:
							return null;

						case POINT:
							return isIntersectingConvex_Point( this , other.getPoint( 0 ) ) ? other : null;

						case LINE:
						{
							final Bounds2D thisBounds  = getBounds();
							final Bounds2D otherBounds = other.getBounds();
							return thisBounds.intersects( otherBounds ) ? getIntersectionConvex_Line( this , other ) : null;
						}

						case CONVEX:
						{
							final Bounds2D thisBounds  = getBounds();
							final Bounds2D otherBounds = other.getBounds();
							return thisBounds.intersects( otherBounds ) ? getIntersectionConvex_Convex( this , other ) : null;
						}

						case PATH:
						{
							final Bounds2D thisBounds  = getBounds();
							final Bounds2D otherBounds = other.getBounds();
							return thisBounds.intersects( otherBounds ) ? getIntersectionConvex_Path( this , other ) : null;
						}
					}
					break;
			}

			throw new RuntimeException( "Can't get intersection between type #" + getType() + " and type #" + other.getType() );
		}
	}

	public boolean contains( final PolyPoint2D point )
	{
		return contains( point.x , point.y );
	}

	public boolean contains( final double x , final double y )
	{
		final boolean result;

		final int type = getType();
		switch ( type )
		{
			case VOID :
				result = false;
				break;

			case POINT  :
				final PolyPoint2D other = getPoint( 0 );
				result = ( x == other.x ) && ( y == other.y );
				break;

			case LINE   :
				result = isIntersectingLine_Point( this , new PolyPoint2D( x , y , 0.0 ) );
				break;

			case PATH   :
				result = isIntersectingPath_Point( this , new PolyPoint2D( x , y , 0.0 ) );
				break;

			case CONVEX :
				result = isIntersectingConvex_Point( this , new PolyPoint2D( x , y , 0.0 ) );
				break;

			default :
				throw new IllegalArgumentException( "unknown type: " + type );
		}

		return result;
	}


	/**
	 * Gets the intersecting area of two convex Polyline2D's.
	 *
	 * Figure out intersection between two convex 2d shapes... ah well..
	 * It seems like a lot of code, but if the shapes have no
	 * intersection, it is almost as fast as isIntersectingConvex_Convex().
	 *
	 * This consists of two stages:
	 *
	 * 1) Find all intersecting line segments
	 * 2) Position the found segments head-to-tail.
	 *
	 * Stage 1, find intersecting segments.
	 *
	 *      Walk thrue both polys to find the segments that intersect with the other.
	 *      First we have to know if we start inside or outside the other poly.
	 *      Now find all intersecting points of one segment of the current poly with
	 *      all segments of the other poly (for two convex polys, this can be 0, 1 or 2 points):
	 *
	 *      - no intersections ? inside  ? stay inside, add segment.
	 *                           outside ? stay outside, do nothing.
	 *      - one intersection ? inside  ? go outside, add segment( head , intersection ).
	 *                           outside ? go inside, add segment( tail , intersection ).
	 *      - two intersections? inside  ? error, from the inside you can never have two intersections.
	 *                           outside ? stay outside, add segment( intersection1 , intersection2 ).
	 *
	 *      Optimisations after walking thrue the first poly:
	 *
	 *      1) If we started inside and we stayed inside the whole time,
	 *         intersection must be convex1 itself.
	 *      2) If we stayed outside the whole time and a point of convex2
	 *         is inside of convex1, intersection is convex2 itself.
	 *      3) If we stayed outside the whole time and a point of convex2
	 *         is outside of convex1, there is no intersection.
	 *
	 * Stage 2, head-to-tail.
	 *
	 *      Start with a random segment (we use the first segment).
	 *      We add the head and the tail of this first segment to the result poly.
	 *      Now walk thrue all the rest of the segments, removing them
	 *      if you add one to the result poly.
	 *      The head of the next segment must be equal to the tail
	 *      of the last one. If so, add the tail of the segment to
	 *      the polyline.
	 *
	 * @param   convex1     A polyline that is guaranteed convex.
	 * @param   convex2     Another polyline that is convex.
	 *
	 * @return  the intersecting area of the two convex polylines.
	 */
	private static Polyline2D getIntersectionConvex_Convex( final Polyline2D convex1 , final Polyline2D convex2 )
	{
		final int INSIDE  = -1;
		final int UNKNOWN = 0;
		final int OUTSIDE = 1;

		/*
		 * This is where we gather the intersecting segments.
		 * After this is done we only have to sort them and
		 * create a new polyline.
		 */
		final PolyPoint2D[] collect1 = new PolyPoint2D[ 2 * convex1.getPointCount() ];
		final PolyPoint2D[] collect2 = new PolyPoint2D[ 2 * convex2.getPointCount() ];
		int collect1Pos = 0;
		int collect2Pos = 0;

		/*
		 * Helpers
		 */
		boolean corner; // intersection is other polys cornerpoint.
		boolean stayedInside = false;
		PolyPoint2D this1;
		PolyPoint2D this2;
		PolyPoint2D other1;
		PolyPoint2D other2;
		PolyPoint2D sect1;
		PolyPoint2D sect2;
		PolyPoint2D[] is = null; // possible intersections.
		int i;
		int j;
		int pos1;
		int pos2;

		/*
		 * First walk thrue convex1, then thrue convex2 to get all intersection segments.
		 */
		for ( int x = 0 ; x < 2 ; x++ ) // loop twice
		{
			//System.out.println( "------- X = " + x );
			final Polyline2D poly1 = ( x == 0 ) ? convex1 : convex2;
			final Polyline2D poly2 = ( x == 1 ) ? convex1 : convex2;

			/*
			 * Gather all line peaces of convex1 that intersect with convex2.
			 */
			i = poly1.getPointCount();

			/*
			 * l1/l2 = last point on convex1/2
			 * c1/c2 = current point on convex1/2
			 * is[0]/is[1] = first/second intersection on segment
			 */
			this1 = poly1.getPoint( --i );

			/*
			 * First we want to know if the startpoint is on the other convex.
			 */
			pos1 = isIntersectingConvex_Point( poly2 , this1 ) ? INSIDE : OUTSIDE;

			/*
			 * In the second pass, if the first pass did not find a segment,
			 * and a point of convex2 lies outside convex1 (inside == false),
			 * they don't intersect. If it lies inside convex1 (inside = true),
			 * then the intersection is convex1.
			 */
			if ( x == 1 && collect1Pos == 0 )
				return pos1 == INSIDE ? poly1 : null;
			if ( x == 1 && stayedInside )
				return poly2;
			stayedInside = pos1 == INSIDE;

			while( --i >= 0 )   // loop convex1.polycount (2 * 2 * polycount max)
			{
				this2 = poly1.getPoint( i );
				pos2 = UNKNOWN;

				// intersection is other polys cornerpoint.
				corner = false;

				j = poly2.getPointCount();
				other1 = poly2.getPoint( --j );

				//System.out.println( "Shape 1 : " + this1 + " , " + this2 );
				/*
				 * Get all intersections of this segment.
				 */
				sect1 = null;
				sect2 = null;   // clear intersections.
				while( --j >= 0 ) // loop convex2.polycount (2 * 2 * polycount * polycount max)
				{
					other2 = poly2.getPoint( j );

					//System.out.println( "Shape 2 : " + other1 + " , " + other2 );

					is = getIntersectionLine_Line( this1.x , this1.y , this2.x , this2.y , other1.x , other1.y , other2.x , other2.y , is );

					if ( is != null ) // found an intersection
					{
						//System.out.println( "Intersection: " + is[0] );

						if ( sect1 == null || sect1.almostEquals( is[0] ) ) // found first intersection (or duplicate).
						{
							sect1 = is[0];
							sect2 = is[1];
						}
						else   // found second intersection (may not be a line).
						{
							//if ( is[1] != null )
								//throw new ProductionModelException( ProductionModelException.INTERNAL_ERROR , "Don't know how to handle" );
							if ( is[1] == null )

							sect2 = is[0];
						}

						if ( other1.almostEquals( is[0] ) )
							corner = true;

						if ( sect1 != null && sect2 != null )
							break;
					}

					/*
					 * Shift.
					 */
					other1 = other2;
				}

				/*
				 * Sort the intersection points.
				 */
				if ( sect1 != null && sect2 != null )
				{
					if ( PolyPoint2D.getLength( this1 , sect2 ) < PolyPoint2D.getLength( this1 , sect1 ) )
					{
						final PolyPoint2D temp = sect1;
						sect1 = sect2; sect2 = temp;
					}
				}

				//System.out.println( "Intersections : " + sect1 + " , " + sect2 );

				/*
				 * Calculate if 'current point' (c1) is inside or outside.
				 */
/*				if ( is[0] == null ) // no intersections
				{
					c1pos = l1pos;
				}
				else*/ if ( sect1 != null ) // 1 or 2 intersections
				{
					if ( sect2 == null && !corner && !this1.almostEquals( sect1 ) ) // 1 intersection
						pos2 = -pos1;

					if ( this2.almostEquals( sect1 ) || this2.almostEquals( sect2 ) ) // intersection is on current point
						pos2 = INSIDE;

					else if ( pos2 == UNKNOWN )
						pos2 = isIntersectingConvex_Point( poly2 , this2 ) ? INSIDE : OUTSIDE;
				}

				/*
				 * Sub segments, generate intersecting segments.
				 */
				PolyPoint2D addHead = null;
				PolyPoint2D addTail = null;
				if ( sect1 == null )
				{
					// no intersections
					if ( pos1 == INSIDE /*&& c1pos == INSIDE*/ )
					{
						addHead = this1;
						addTail = this2;
					}
					pos2 = pos1;
				}
				else // 1 or 2 intersections
				{
					// first intersectionpoint is not l1.
					if ( pos1 == INSIDE && !this1.almostEquals( sect1 ) )
					{
						addHead = this1;
						addTail = sect1;
					}

					if ( sect2 == null ) // 1 intersection
					{
						// 1 intersection and intersectionpoint is not c1
						if ( pos2 == INSIDE && !this2.almostEquals( sect1 ) )
						{
							addHead = sect1;
							addTail = this2;
						}

						// special case when intersection is one point.
						if ( pos1 == OUTSIDE && this2.almostEquals( sect1 ) && corner )
						{
							addHead = this2;
							addTail = null;
						}
					}
					else // 2 intersections
					{
						// second intersectionpoint is not c1
						if ( pos2 == INSIDE && !this2.almostEquals( sect2 ) )
							throw new RuntimeException( "Linesegment cannot have two intersections and end in convex shape" );

						if ( !sect1.almostEquals( sect2 ) )
						{
							addHead = sect1;
							addTail = sect2;
						}
					}
				}
				if ( addHead != null )
				{
					if ( x == 0 )
					{
						//System.out.println( "Add [ " + addHead + " , " + addTail + "]" );
						collect1[ collect1Pos++ ] = addHead;
						collect1[ collect1Pos++ ] = addTail;
					}
					else
					{
						//System.out.println( "Add [ " + addHead + " , " + addTail + "]" );
						collect2[ collect2Pos++ ] = addHead;
						collect2[ collect2Pos++ ] = addTail;
					}
				}

				if ( pos2 == OUTSIDE || sect2 != null )
					stayedInside = false;

				/*
				 * Shift
				 */
				this1 = this2;
				pos1 = pos2;
			}
		}

		final int collect1Count = collect1Pos / 2;
		final int collect2Count = collect2Pos / 2;

		//System.out.println( "Collect1 : " );
		//for ( i = 0 ; i < collect1Count ; i++ )
			//System.out.println( "Segment : [" + collect1[ i * 2 ] + " , " + collect1[ i * 2 + 1 ] + "]" );

		//System.out.println( "Collect2 : " );
		//for ( i = 0 ; i < collect2Count ; i++ )
			//System.out.println( "Segment : [" + collect2[ i * 2 ] + " , " + collect2[ i * 2 + 1 ] + "]" );

		/*
		 * Now we have all the segments.
		 * place the segments in order, then we have the intersection.
		 * Walk true all segments to position them head to tail.
		 */
		final Polyline2D result = new Polyline2D();

		if ( collect1Count < 2 && collect2Count < 2 )
		{
			result.addImpl( collect1[ 0 ] );
			if ( collect1[1] != null )
				result.addImpl( collect1[1] );

			return result;
		}

		if ( true )
		{
			final List segments = new ArrayList( collect1Count + collect2Count );
			for ( i = 0 ; i < collect1Count ; i++ )
			{
				final Polyline2D add = new Polyline2D();
				add.addImpl( collect1[ i * 2 ] );

				if ( collect1[ i * 2 + 1 ] != null )
					add.addImpl( collect1[ i * 2 + 1 ] );

				segments.add( add );
			}

			for ( i = 0 ; i < collect2Count ; i++ )
			{
				final Polyline2D add = new Polyline2D();
				add.addImpl( collect2[ i * 2 ] );

				if ( collect2[ i * 2 + 1 ] != null )
					add.addImpl( collect2[ i * 2 + 1 ] );

				segments.add( add );
			}

			return placeHeadToTail( segments );
		}
		return null;

		///*
		 //* The following method works fine for collects that only
		 //* jump to the other collect once....(otherwise it forgets segments)
		 //*/

		///*
		 //* 1) Start walking thrue collect1's segments until we cannot match
		 //* 	  next segment with last.
		 //* 2) Move to collect2 and find the matching segment first.
		 //* 3) Walk thrue collect2 (loop back to index 0 if reached end)
		 //*    until we reached the starting point of collect2 again.
		 //* 4) Move back to collect1 and walk thrue until the end.
		 //*/
		//int col1pos = 0;	// pointer in collect1
		//boolean visitedCollect2 = false; // safety for not entering collect2 twice.

		//PolyPoint2D lastTail = collect1[ 0 ];
		//PolyPoint2D lastHead = null;
		////System.out.println( "1) Match : " + lastTail );
		//result.append( lastTail.x , lastTail.y );

		//PolyPoint2D head,tail;

		//while( col1pos < collect1Count || !visitedCollect2 )
		//{
			//head = collect1[ col1pos*2 ];
			//tail = collect1[ col1pos*2 + 1 ];

			////System.out.println( "1) Head = " + head + " , Tail = " + tail );

			//if ( col1pos < collect1Count && ( head == null || tail == null || lastTail.almostEquals( head  ) ) )
			//{
				///*
				 //* If we match, add the point.
				 //*/
				//if ( head != null && tail != null )
				//{
					////System.out.println( "1) Match : " + tail );
					//result.append( tail.x , tail.y );
					//lastTail = tail;
					//lastHead = head;
				//}
				//col1pos++;
				//continue;
			//}

			////System.out.println( "Jump 2)" );
			////System.out.println( "Last = " + lastTail );
			///*
			 //* Otherwise jump to collect2 and back.
			 //*/

			///*
			 //* If we dont match and we already visited col2 -> error
			 //*/
			//if ( visitedCollect2 )
				//throw new ProductionModelException( ProductionModelException.INTERNAL_ERROR ,
					//"Segments found on convex1 are not in order" );
			//visitedCollect2 = true;

			///*
			 //* Find entrypoint in collect2
			 //*/
			//int col2pos = 0;	// pointer in collect2
			//int col2entry = 0;
			//int col2direction = 0;
			//for ( col2pos = 0 ; col2pos < collect2Count ; col2pos++ )
			//{
				//head = collect2[ col2pos*2 ];
				//tail = collect2[ col2pos*2 + 1];

				///*
				 //* Filter points segments.
				 //*/
				//if ( head != null && tail != null )
				//{
					///*
					 //* Test for positive direction.
					 //*/
					//if ( head.almostEquals( lastTail ) && !tail.almostEquals( lastHead ) )
					//{
						//col2entry = col2pos;
						//col2direction = 1;
						//break;
					//}

					///*
					 //* Test for negative direction.
					 //*/
					//if ( tail.almostEquals( lastTail ) && !head.almostEquals( lastHead ) )
					//{
						//col2entry = col2pos;
						//col2direction = -1;
						//break;
					//}
				//}
			//}

			///*
			 //* Keep walking true collect2 until we are at entry point again.
			 //*/
			//for ( boolean firstLoop = true ; firstLoop || col2pos != col2entry ;  )
			//{
				//firstLoop = false;

				//head = collect2[ col2pos*2 + (col2direction==1?0:1) ];
				//tail = collect2[ col2pos*2 + (col2direction==1?1:0) ];

				//if ( head != null && tail != null && head.almostEquals( lastTail ) && !tail.almostEquals( lastHead ) )
				//{
					////System.out.println( "2) Match : " + tail );
					//result.append( tail.x , tail.y );
					//lastTail = tail;
					//lastHead = head;
				//}

				//col2pos = col2pos + col2direction;
				//if ( col2pos < 0 )
					//col2pos = collect2Count - 1;
				//if ( col2pos > collect2Count - 1 )
					//col2pos = 0;
			//}

			////System.out.println( "Jump 1)" );
		//}

		//return result;
	}

	/**
	 * Gets the intersection between a convex PolyLine2D and a line PolyLine2D.
	 *
	 * @param   convex  Convex Polyline to get intersection from.
	 * @param   line    Line to get intersection from.
	 *
	 * @return  PolyLine2D describing the intersection between the two.
	 */
	private static Polyline2D getIntersectionConvex_Line( final Polyline2D convex , final Polyline2D line )
	{
		final PolyPoint2D p1 = line.getPoint( 0 );
		final PolyPoint2D p2 = line.getPoint( 1 );
		final boolean p1Inside = isIntersectingConvex_Point( convex , p1 );
		final boolean p2Inside = isIntersectingConvex_Point( convex , p2 );

		/*
		 * If both points are inside, intersection is line.
		 */
		if ( p1Inside && p2Inside )
			return line;

		/*
		 * All found intersection points.
		 */
		final PolyPoint2D[] iPoints = new PolyPoint2D[ convex.getPointCount() ];
		int iPointCount = 0;

		PolyPoint2D[] sect = new PolyPoint2D[2];

		int i = convex.getPointCount();

		PolyPoint2D p3 = convex.getPoint( --i );
		PolyPoint2D p4;

		/*
		 * Gather all intersection points (lines are automatically added
		 * because the ends of an intersecting line are added from other
		 * convex segments).
		 */
		while ( i > 0 )
		{
			p4 = convex.getPoint( --i );

			sect = getIntersectionLine_Line( p1.x , p1.y , p2.x , p2.y , p3.x , p3.y , p4.x , p4.y , sect );
			if ( sect != null )
			{
				if ( sect[0] != null )
					iPoints[ iPointCount++ ] = sect[0];
				if ( sect[1] != null )
					iPoints[ iPointCount++ ] = sect[1];
			}

			p3 = p4;
		}

		/*
		 * No intersection.
		 */
		final Polyline2D result;

		if ( iPointCount == 0 )
		{
			if ( p1Inside || p2Inside )
			{
				result = new Polyline2D( 1 );
				result.addCopyOf( p1Inside ? p1 : p2 );

			}
			else
			{
				result = null;
			}
			return result;
		}

		result = new Polyline2D();

		/*
		 * Point intersection.
		 */
		if ( iPointCount == 1 || ( iPointCount == 2 && iPoints[0].almostEquals( iPoints[1] ) ) )
		{
			final PolyPoint2D intersection = iPoints[0];

			if ( p1Inside && !p1.almostEquals( intersection ) )
				result.addCopyOf( p1 );

			result.addImpl( intersection );

			if ( p2Inside && !p2.almostEquals( intersection ) )
				result.addCopyOf( p2 );

			return result;
		}

		/*
		 * 2 intersections, defenately line between two points.
		 */
		if ( iPointCount == 2 )
		{
			result.addImpl( iPoints[0] );
			result.addImpl( iPoints[1] );
			return result;
		}

		/*
		 * Usually we don't get this far in this method, but there are
		 * cases where multiple points of the convex shape lay on the
		 * same line (when generating a side view of a shape on a plane).
		 *
		 * The intersecting line is the line between the point closest
		 * to p1 and the point closest to p2.
		 * (There is also a case, where all points are equal. Return one point only).
		 */
		PolyPoint2D p1Close       = iPoints[0];
		double      p1CloseLength = PolyPoint2D.getLength( p1, p1Close );
		PolyPoint2D p2Close       = iPoints[0];
		double      p2CloseLength = PolyPoint2D.getLength( p2 , p1Close );
		PolyPoint2D p;

		for ( i = 1 ; i < iPointCount ; i++ )
		{
			p = iPoints[i];
			final double p1Length = PolyPoint2D.getLength( p , p1 );
			final double p2Length = PolyPoint2D.getLength( p , p2 );
			if ( p1Length < p1CloseLength )
			{
				p1Close = p;
				p1CloseLength = p1Length;
			}
			if ( p2Length < p2CloseLength )
			{
				p2Close = p;
				p2CloseLength = p2Length;
			}
		}

		if ( p1Close.almostEquals( p2Close ) )
		{
			result.addImpl( p1Close );
			return result;
		}

		result.addImpl( p1Close );
		result.addImpl( p2Close );
		return result;
	}

	/**
	 * Gets the intersecting area of two convex Polyline2D's.
	 *
	 * Figure out intersection of a convex poly and a path.
	 * This is a simplified method of getIntersectionConvex_Convex.
	 *
	 * There is only one stage (of which only one cycle):
	 * Walk thrue all segments of the path. Find all intersection points
	 * of that segment with the convex poly. Now apply the same algorithm
	 * as with two convex polys (inside/outside algorithm), only now
	 * we can directly add the found segments to the result poly.
	 *
	 * Since we do not support polylines consisting of multiple loose
	 * parts, we throw a RunTimeException when the path intersects
	 * the convex more than one time. So the path may only enter/exit the
	 * convex once.
	 *
	 * @param   convex      Polyline that is guaranteed convex.
	 * @param   path        Polyline that is a path.
	 *
	 * @return  Intersecting area of the two convex polylines.
	 *
	 * @see     #getIntersectionConvex_Convex
	 */
	private static Polyline2D getIntersectionConvex_Path( final Polyline2D convex , final Polyline2D path )
	{
		final int INSIDE  = -1;
		final int UNKNOWN =  0;
		final int OUTSIDE =  1;

		final Polyline2D result = new Polyline2D();

		/*
		 * Helpers
		 */
		boolean corner = false; // intersection is other polys cornerpoint.
		int l1pos;
		int c1pos;

		/*
		 * Gather all line peaces of the path that intersect with the convex.
		 */
		int i = path.getPointCount();
		int j;

		/*
		 * l1/l2 = last point on path/convex
		 * c1/c2 = current point on path/convex
		 * i1/i2 = first/second intersection on segment
		 */
		PolyPoint2D l1;
		PolyPoint2D c1;
		PolyPoint2D l2;
		PolyPoint2D c2;
		PolyPoint2D i1;
		PolyPoint2D i2;
		PolyPoint2D[] is = null;
		l1 = path.getPoint( --i );

		/*
		 * First we want to know if the startpoint of the path is on the convex.
		 */
		l1pos = isIntersectingConvex_Point( convex , l1 ) ? INSIDE : OUTSIDE;

		/*
		 * Optimalisation to check if the path is entirely inside/outside the convex.
		 * Also to check if result consists of multiple loose parts.
		 */
		boolean entered = l1pos == INSIDE;

		/*
		 * If we start inside, add the fist point.
		 */
		if ( l1pos == INSIDE )
			result.addStartPoint( l1.x , l1.y );

		while( --i >= 0 )
		{
			c1 = path.getPoint( i );
			c1pos = UNKNOWN;

			j = convex.getPointCount();
			l2 = convex.getPoint( --j );
			i1 = i2 = null;	// clear intersections.

			//System.out.println( "Segment : " + l1 + " , " + c1 );

			while( --j >= 0 )
			{
				c2 = convex.getPoint( j );
				is = getIntersectionLine_Line( l1.x , l1.y , c1.x , c1.y , l2.x , l2.y , c2.x , c2.y , is );

				/*
				 * If this is the first intersection found,
				 * move it into i1 and clear i2. If this
				 * is the second, stop searching.
				 */
				if ( is != null ) // found an intersection
				{
					//System.out.println( "Intersection: " + is[0] );

					if ( i1 == null || i1.almostEquals( is[0] ) ) // found first intersection (or duplicate).
					{
						i1 = is[0];
						i2 = is[1];
					}
					else			   // found second intersection (may not be a line).
					{
						if ( is[1] != null )
							throw new RuntimeException( "Don't know how to handle" );
						i2 = is[0];
					}

					if ( c2.almostEquals( is[0] ) )
						corner = true;

					if ( i1 != null && i2 != null )
						break;
				}

				/*
				 * Shift.
				 */
				l2 = c2;
			}

			//System.out.println( "intersections : " + i1 + " , " + i2 );

			/*
			 * Calculate if 'current point' (c1) is inside or outside.
			 */
			if ( i1 != null ) // 1 or 2 intersections
			{
				if ( i2 == null && !corner ) // 1 true intersection
					c1pos = -l1pos;

				if ( c1.almostEquals( i1 ) || c1.almostEquals( i2 ) )
					c1pos = INSIDE;
			}

			if ( c1pos == UNKNOWN )
				c1pos = isIntersectingConvex_Point( convex , c1 ) ? INSIDE : OUTSIDE;

			//System.out.println( "l1 pos = " + l1pos + " , c1pos = " + c1pos );

			/*
			 * Sub segments, generate intersecting segments.
			 */
			if ( i1 != null )
			{
				/*
				 * If we already entered once, and we are about to again, error.
				 */
				if ( entered && ( (l1pos == OUTSIDE && c1pos == INSIDE) || ( i2 != null && !corner ) ) )
					throw new RuntimeException( "Path may only intersect convex poly once. We only support unairy result sets." );

				/*
				 * If we have two intersections and endpoint is inside (but not on edge)
				 * we have an error.
				 */
				if ( i2 != null && c1pos == INSIDE && !c1.almostEquals( i2 ) && !corner )
					throw new RuntimeException( "Linesegment cannot have two intersections and end in convex shape" );

				/*
				 * If first intersection is not starting point, add point
				 * (doesn't matter if we are going in or outside).
				 */
				if ( !i1.almostEquals( l1 ) && ( i2 != null || c1pos == INSIDE ) || l1pos == INSIDE )
				{
					if ( ( result.getPointCount() == 0 ) || !i1.almostEquals( result.getPoint( result.getPointCount() - 1 ) ) )
						result.addImpl( i1.x , i1.y , 0.0 );
				}
			}

			/*
			 * If second intersection is not ending point, add point
			 * (in this case, we are always exiting again).
			 */
			if ( i2 != null && !i2.almostEquals( c1 ) )
				result.addImpl( i2.x , i2.y , 0.0 );

			/*
			 * If endpoint is inside, add the point.
			 */
			if ( c1pos == INSIDE && !c1.almostEquals( i1 ) )
				result.addImpl( c1.x , c1.y , 0.0 );

			/*
			 * If one of the points are inside or we have 2 intersections
			 * we must have been inside.
			 */
			if ( c1pos == INSIDE || l1pos == INSIDE || i2 != null )
				entered = true;

			/*
			 * Shift
			 */
			l1 = c1;
			l1pos = c1pos;
		}

		/*
		 * If we never entered the convext, there is no intersection.
		 */
		if ( !entered )
			return null;

		return result;
	}

	/**
	 * Gets the intersection between two lines.
	 *
	 * Calls getIntersectionLine_Line with 8 floats to do the work.
	 *
	 * @param   p1      First point of line 1
	 * @param   p2      Second point of line 1
	 * @param   p3      First point of line 2
	 * @param   p4      Second point of line 2
	 *
	 * @return  Polyline describing the intersection.
	 */
	private static Polyline2D getIntersectionLine_Line( final PolyPoint2D p1 , final PolyPoint2D p2 , final PolyPoint2D p3 , final PolyPoint2D p4 )
	{
		final PolyPoint2D[] sect = getIntersectionLine_Line( p1.x , p1.y , p2.x , p2.y , p3.x , p3.y , p4.x , p4.y , null );
		final PolyPoint2D start = sect[ 0 ];
		if ( sect == null || start == null )
			return null; // no intersection

		final Polyline2D result = new Polyline2D();

		result.addImpl( start.x , start.y , 0.0 ); // intersection point

		final PolyPoint2D end = sect[ 1 ];
		if ( end != null )
			result.addImpl( end.x , end.y , 0.0 ); // intersecting line

		return result;
	}

	/**
	 * Get intersection of two line segments.
	 *
	 * This uses the following algorithm:
	 *
	 *    "Intersection point of two lines (2 dimension)"
	 *    Author: Paul Bourke (april 1989)
	 *    http://astronomy.swin.edu.au/pbourke/geometry/lineline2d/
	 *
	 * @param   x1      Start point of line 1.
	 * @param   y1      Start point of line 1
	 * @param   x2      End point of line 1
	 * @param   y2      End point of line 1
	 * @param   x3      Start point of line 2.
	 * @param   y3      Start point of line 2
	 * @param   x4      End point of line 2
	 * @param   y4      End point of line 2
	 * @param   cache   Cached array of polyline so we don't have to create one.
	 *
	 * @return  Array of points describing the intersection between the two
	 *          (can be 0..2 points).
	 */
	private static PolyPoint2D[] getIntersectionLine_Line( final double x1 , final double y1 , final double x2 , final double y2 , final double x3 , final double y3 , final double x4 , final double y4 , PolyPoint2D[] cache )
	{
		if ( cache == null || cache.length < 2 )
			cache = new PolyPoint2D[2];

		final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
		final double d  = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );

		if ( d == 0 ) /* lines are parallel */
		{
			if ( n1 == 0 ) /* they are coincident */
			{
				double sx1;
				double sx2;
				boolean isPositive;

				if ( x1 <= x2 ) { sx1 = x1; sx2 = x2; isPositive = true; }
				           else { sx1 = x2; sx2 = x1; isPositive = false; }

				if ( x3 <= x4 )
				{
					if ( x3 > sx1 ) sx1 = x3;
					if ( x4 < sx2 ) sx2 = x4;
				}
				else
				{
					if ( x4 > sx1 ) sx1 = x4;
					if ( x3 < sx2 ) sx2 = x3;
				}

				if ( sx1 > sx2 )
					return null;

				double sy1;
				double sy2;

				if ( y1 <= y2 ) { sy1 = y1; sy2 = y2; }
				           else { sy1 = y2; sy2 = y1; isPositive = !isPositive;	 }

				if ( y3 <= y4 )
				{
					if ( y3 > sy1 ) sy1 = y3;
					if ( y4 < sy2 ) sy2 = y4;
				}
				else
				{
					if ( y4 > sy1 ) sy1 = y4;
					if ( y3 < sy2 ) sy2 = y3;
				}

				if ( sy1 > sy2 )
					return null;

				/*
				 * Return the intersection.
				 */
				if ( sx1 == sx2 && sy1 == sy2 )
				{
					cache[0] = new PolyPoint2D( sx1 , sy1 , 0.0 );
					cache[1] = null;
				}
				else if ( isPositive )
				{
					cache[0] = new PolyPoint2D( sx1 , sy1 , 0.0 );
					cache[1] = new PolyPoint2D( sx2 , sy2 , 0.0 );
				}
				else
				{
					cache[0] = new PolyPoint2D( sx1 , sy2 , 0.0 );
					cache[1] = new PolyPoint2D( sx2 , sy1 , 0.0 );
				}

				return cache;
			}
			else /* they are non-coincident */
			{
				return null;
			}
		}
		else /* lines intersect at some point */
		{
			/*
			 * Test if intersection point is within both line segments
			 */
			final double ua = n1 / d;
			if ( ua < -0.00001 || ua > 1.00001 ) return null; // float round error fix

			final double n2 = ( x2 - x1 ) * ( y1 - y3 ) - ( y2 - y1 ) * ( x1 - x3 );
			final double ub = n2 / d;
			if ( ub < -0.00001 || ub > 1.00001 ) return null;

			final double x = x1 + ua * (x2-x1);
			final double y = y1 + ua * (y2-y1);

			cache[0] = new PolyPoint2D( x , y , 0.0 );
			cache[1] = null;
			/*
			 * We have an intersection point!
			 */
			return cache;
		}

	}

	/**
	 * Gets the intersection between a path and a line.
	 *
	 * Works only when result is one peace of one segment of path (or one point).
	 *
	 * @param   path    Path.
	 * @param   p1      First point of line
	 * @param   p2      Second point of line.
	 *
	 * @return  Polyline describing the intersection.
	 */
	private static Polyline2D getIntersectionPath_Line( final Polyline2D path , final PolyPoint2D p1 , final PolyPoint2D p2 )
	{
		/*
		 * First gather all intersecting points and lines.
		 */
		final Polyline2D[] iPoints = new Polyline2D[ path.getPointCount() ];
		int iPointCount = 0;
		final Polyline2D[]  iLines = new Polyline2D[ path.getPointCount() ];
		int iLineCount = 0;
		Polyline2D sect;

		int i = path.getPointCount();

		PolyPoint2D p3 = path.getPoint( --i );
		PolyPoint2D p4;

		while ( i > 0 )
		{
			p4 = path.getPoint( --i );

			sect = getIntersectionLine_Line( p1 , p2 , p3 , p4 );

			if ( sect != null && sect.getPointCount() > 0 )
			{
				if ( sect.getPointCount() == 1 )
					iPoints[ iPointCount++ ] = sect;
				else if ( sect.getPointCount() == 2 )
					iLines[ iLineCount++ ] = sect;
			}

			p3 = p4;
		}

		/*
		 * No intersection.
		 */
		if ( iLineCount == 0 && iPointCount == 0 )
			return null;

		/*
		 * 1 intersection.
		 */
		if ( iLineCount == 1 && iPointCount == 0 )
			return iLines[ 0 ];
		if ( iPointCount == 1 && iLineCount == 0 )
			return iPoints[ 0 ];

		/*
		 * Now we have to figure out if the result is actually one
		 * peace. What we have is a number of Points and Lines. What
		 * we want is one Line or on Point (or exception/null).
		 */
		Polyline2D result = null;

		for ( i = 0 ; i < iLineCount ; i++ )
		{
			if ( result == null )
			{
				result = iLines[i];
			}
			else
			{
				/*
				 * If we want to add a line and the current result is a line,
				 * they must be intersecting and in the same direction.
				 * (for now, we don't allow multiple lines).
				 */
				throw new RuntimeException( Polyline2D.class.getName() + ".getIntersectionPath_Path() : Multiple paths" );
			}
		}

		for ( i = 0 ; i < iPointCount ; i++ )
		{
			if ( result == null )
			{
				result = iPoints[i];
			}
			else
			{
				/*
				 * If we want to add a point that is not on the current result,
				 * we would have a multiple result, which we cannot do..
				 * (Same if we have a point and want to add a line)
				 */
				if ( !result.isIntersecting( iPoints[i] ) )
					throw new RuntimeException( Polyline2D.class.getName() + ".getIntersectionPath_Path() : Multiple paths" );
			}
		}

		return result;
	}

	/**
	 * Gets intersection between two paths.
	 *
	 * Works only when seperate intersecting segments can be layed
	 * head to tail.
	 *
	 * @param   path1   First path.
	 * @param   path2   Second path.
	 *
	 * @return  Polyline describing the intersection.
	 */
	private static Polyline2D getIntersectionPath_Path( final Polyline2D path1 , final Polyline2D path2 )
	{
		/*
		 * First gather all intersecting points and lines.
		 */
		final List segments = new ArrayList( path1.getPointCount() + path2.getPointCount() );
		Polyline2D sect;

		int i = path1.getPointCount();

		PolyPoint2D p3 = path1.getPoint( --i );
		PolyPoint2D p4;

		while ( i > 0 )
		{
			p4 = path1.getPoint( --i );

			sect = getIntersectionPath_Line( path2 , p3 , p4 );

			if ( sect != null && sect.getPointCount() > 0 )
				segments.add( sect );

			p3 = p4;
		}

		///*
		 //* No intersection.
		 //*/
		//if ( segments.size() == 0 )
			//return null;

		///*
		 //* 1 intersection.
		 //*/
		//if ( segments.size() == 1 )
			//return (Polyline2D)segments.get( 0 );

		/*
		 * Multiple intersections
		 */
		return placeHeadToTail( segments );
	}

	/**
	 * Get length of segment.
	 *
	 * @param   index   Index of start point of segment.
	 *
	 * @return  Length of segment, or -1 if the length of the specified segment can not
	 *          be determined (invalid index).
	 */
	public double getLength( final int index )
	{
		final double result;

		if ( ( ( index < 0 ) || ( index >= getPointCount() - 1 ) ) )
		{
			result = -1.0;
		}
		else
		{
			final PolyPoint2D p1 = getPoint( index );
			final PolyPoint2D p2 = getPoint( index + 1 );

			result = PolyPoint2D.getLength( p1 , p2 );
		}

		return result;
	}

	/**
	 * Get control point at specified index from this polyline.
	 *
	 * @param   index   Index of control point to get.
	 *
	 * @return  PolyPoint2D instance at specified index.
	 */
	public PolyPoint2D getPoint( final int index )
	{
		return (PolyPoint2D)_points.get( index );
	}

	/**
	 * Get number of control points in this polyline.
	 *
	 * @return  Number of control points in this polyline.
	 */
	public int getPointCount()
	{
		return _points.size();
	}

	/**
	 * Get type of polyline.
	 *
	 * @return  {@link #VOID}, {@link #POINT}, {@link #PATH}, {@link #CONVEX},
	 *          or {@link #CONCAVE}.
	 */
	public int getType()
	{
		if ( _typeCache == UNKNOWN )
		{
			/*
			 * If we have no control points, it's the void.
			 */
			if ( getPointCount() < 1 )
				_typeCache = VOID;

			/*
			 * Single control point => point
			 */
			else if ( getPointCount() == 1 )
				_typeCache = POINT;

			/*
			 * Two control points => line.
			 */
			else if ( getPointCount() == 2 )
				_typeCache = LINE;

			else
			{
				/*
				 * All points at same coordinates => point
				 */
				final PolyPoint2D first    = getPoint( 0 );
				final double      x        = first.x;
				final double      y        = first.y;
				final boolean     isClosed = isClosed();

				boolean isPoint        = true;
				boolean hasOrientation = false;
				boolean isLeftOriented = false;

				for ( int i = 1 ; i < getPointCount() ; i++ )
				{
					final PolyPoint2D previous = getPoint( i - 1 );
					final PolyPoint2D current  = getPoint( i );
					final PolyPoint2D next     = getPoint( ( i + 1 ) % getPointCount() );

					/*
					 * Point detection
					 */
					isPoint = isPoint && ( current.x == x ) && ( current.y == y );
					if ( isPoint )
						continue;

					/*
					 * Area detection.
					 */
					if ( isClosed )
					{
						final double f = ( current.x - previous.x ) * ( next.y - previous.y ) - ( current.y - previous.y ) * ( next.x - previous.x );

						if ( f > 0 ) /* left orientation */
						{
							if ( hasOrientation && !isLeftOriented )
							{
								_typeCache = CONCAVE;
								break;
							}

							hasOrientation = true;
							isLeftOriented = true;
						}
						else if ( f < 0 ) /* right orientation */
						{
							if ( hasOrientation && isLeftOriented )
							{
								_typeCache = CONCAVE;
								break;
							}

							hasOrientation = true;
							isLeftOriented = false;
						}
						/* else { straight line } */
					}
				}

				if ( _typeCache == UNKNOWN )
				{
					/*
					 * If all control points are at the same coordinates, the result is a point.
					 */
					if ( isPoint )
						_typeCache = POINT;

					/*
					 * If we found an orientation (only possible if the polyline is closed),
					 * it must be a convex area. Note that concave areas are returned by the
					 * loop above immediately.
					 */
					else if ( hasOrientation )
						_typeCache = CONVEX;

					/*
					 * We have no better clue, so it should be a path.
					 */
					else
						_typeCache = PATH;
				}
			}
		}

		return _typeCache;
	}

	/**
	 * Test if the specified transformation has any effect on 2D polylines.
	 *
	 * @param   xform   Transformation matrix.
	 *
	 * @return  <code>true</code> if the transformation has effect on 2D polylines;
	 *          <code>false</code> otherwise.
	 */
	private static boolean hasEffect( final Matrix3D xform )
	{
		return ( xform != null )
		    && ( ( xform.xx != 1.0 ) || ( xform.xy != 0.0 ) || ( xform.xo != 0.0 )
		      || ( xform.yx != 0.0 ) || ( xform.yy != 1.0 ) || ( xform.yo != 0.0 ) );
	}

	/**
	 * Test if the polyline defines a closed path. A polyline must at least
	 * contain two control points and have the first and last control meet at
	 * the exact same point to be classified as closed.
	 *
	 * @return  <code>true</code> if the polyline defines a closed path;
	 *          <code>false</code> otherwise.
	 */
	public boolean isClosed()
	{
		final boolean result;

		if ( getPointCount() < 2 )
		{
			result = false;
		}
		else
		{
			final PolyPoint2D first = getPoint( 0 );
			final PolyPoint2D last  = getPoint( getPointCount() - 1 );

			result = ( first == last ) || first.equals( last );
		}

		return result;
	}

	/**
	 * Checks if this shape is intersecting with an other one.
	 *
	 * @param   other   Polyline to check for intersection.
	 *
	 * @return  <code>true</code> if shapes are intersecting;
	 *          <code>false</code> otherwise.
	 */
	public boolean isIntersecting( final Polyline2D other )
	{
		boolean result = false;

		if ( other == this )
		{
			result = true;
		}
		else if ( other != null )
		{
			final int myType = getType();
			if ( myType != VOID )
			{
				final int otherType = other.getType();
				if ( otherType != VOID )
				{
					if ( getBounds().intersects( other.getBounds() ) ) // Check bounding box.
					{
						switch ( myType <<5| otherType )
						{
							case POINT  <<5| POINT  : result = getPoint( 0 ).equals( other.getPoint( 0 ) ); break;
							case POINT  <<5| LINE   : result = isIntersectingLine_Point( other , getPoint( 0 ) ); break;
							case POINT  <<5| PATH   : result = isIntersectingPath_Point( other , getPoint( 0 ) ); break;
							case POINT  <<5| CONVEX : result = isIntersectingConvex_Point( other , getPoint( 0 ) ); break;
							case LINE   <<5| POINT  : result = isIntersectingLine_Point( this , other.getPoint( 0 ) ); break;
							case LINE   <<5| LINE   : result = isIntersectingLine_Line( this , other ); break;
							case LINE   <<5| PATH   : result = isIntersectingPath_Line( other , this ); break;
							case LINE   <<5| CONVEX : result = isIntersectingConvex_Line( other , this ); break;
							case PATH   <<5| POINT  : result = isIntersectingPath_Point( this , other.getPoint( 0 ) ); break;
							case PATH   <<5| LINE   : result = isIntersectingPath_Line( this , other ); break;
							case PATH   <<5| PATH   : result = isIntersectingPath_Path( this , other ); break;
							case PATH   <<5| CONVEX : result = isIntersectingConvex_Path( other , this ); break;
							case CONVEX <<5| POINT  : result = isIntersectingConvex_Point( this , other.getPoint( 0 ) ); break;
							case CONVEX <<5| LINE   : result = isIntersectingConvex_Line( this , other ); break;
							case CONVEX <<5| PATH   : result = isIntersectingConvex_Path( this , other ); break;
							case CONVEX <<5| CONVEX : result = isIntersectingConvex_Convex( this , other ); break;
							default : throw new RuntimeException( "Can't test intersection between type #" + myType + " and type #" + otherType );
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Two convex polys intersect when a line of one intersects with a line on the other.
	 * If this is not the case we can have one of these three situations:
	 * - polys do not intersect.
	 * - convex1 is completely within convex2
	 * - convex2 is completely within convex1
	 * So we have to check intersection between one point on convex1 and convex2
	 * to check if convex1 is completely on convex2 (and the otherway around).
	 *
	 * @param   convex1     First convex.
	 * @param   convex2     Second convex.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingConvex_Convex( final Polyline2D convex1 , final Polyline2D convex2 )
	{
		/*
		 * First check if a point on convex1 is inside convex2
		 * (to ensure convex1 is not completely inside convex2).
		 */
		if ( isIntersectingConvex_Point( convex2 , convex1.getPoint( 0 ) ) )
			return true;

		/*
		 * The same for point on convex2.
		 */
		if ( isIntersectingConvex_Point( convex1 , convex2.getPoint( 0 ) ) )
			return true;

		/*
		 * Now test for any intersecting segments.
		 */
		return isIntersectingPath_Path( convex1 , convex2 );
	}

	/**
	 * Checks for intersection between a convex and a line.
	 *
	 * @param   convex  Convex.
	 * @param   line    Line.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingConvex_Line( final Polyline2D convex , final Polyline2D line )
	{
		if ( isIntersectingConvex_Point( convex , line.getPoint( 0 ) ) )
			return true;

		return isIntersectingPath_Line( convex , line );
	}

	/**
	 * A path and a convex intersect when a line of one intersects with a line of the other.
	 * If this is not the case we can have one of these two situations:
	 * - polys do not intersect.
	 * - line is completely within convex
	 * So we have to check intersection between one point on the line and the convex poly.
	 *
	 * @param   convex  Convex.
	 * @param   path    Path.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingConvex_Path( final Polyline2D convex , final Polyline2D path )
	{
		/*
		 * First check if a point on the path is inside the convex
		 * (to ensure path is not completely inside the convex).
		 */
		if ( isIntersectingConvex_Point( convex , path.getPoint( 0 ) ) )
			return true;

		/*
		 * Now test for any intersection between segments.
		 */
		return isIntersectingPath_Path( convex , path );
	}

	/**
	 * Test is a point is inside a convex area. The algorithm works as follows:
	 * <i>
	 * Traverse all segments of the path that define the area. For each segment,
	 * determine if the specified point is on the left side or right side (or right
	 * in the center). If the point is always at the left side or center, or if
	 * the point is always on the right side or center, the point is inside the
	 * area. Otherwise, the point is outside the area.
	 * </i>
	 *
	 * @param   convex  Convex.
	 * @param   point   Point.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingConvex_Point( final Polyline2D convex , final PolyPoint2D point )
	{
		PolyPoint2D p;
		double x1;
		double y1;
		double x2;
		double y2;

		int i = convex.getPointCount();

		final double px = point.x;
		final double py = point.y;
		p  = convex.getPoint( --i );
		x2 = p.x;
		y2 = p.y;

		boolean hasSide = false;
		boolean isLeft = false;

		while ( --i >= 0 )
		{
			p = convex.getPoint( i );
			x1 = x2; x2 = p.x;
			y1 = y2; y2 = p.y;

			final double f = ( x2 - x1 ) * ( py - y1 ) - ( y2 - y1 ) * ( px - x1 );

			if ( f > 0 ) /* point is on left side */
			{
				if ( hasSide && !isLeft ) return false;
				hasSide = true;
				isLeft = true;
			}
			else if ( f < 0 ) /* point is on right side */
			{
				if ( hasSide && isLeft ) return false;
				hasSide = true;
				isLeft = false;
			}
		}

		return hasSide;
	}

	/**
	 * Checks for intersection between two lines.
	 *
	 * @param   line1   First line.
	 * @param   line2   Second line.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingLine_Line( final Polyline2D line1 , final Polyline2D line2 )
	{
		final PolyPoint2D p1 = line1.getPoint( 0 );
		final PolyPoint2D p2 = line1.getPoint( 1 );
		final PolyPoint2D p3 = line2.getPoint( 0 );
		final PolyPoint2D p4 = line2.getPoint( 1 );

		return isIntersectingLine_Line( p1.x , p1.y , p2.x , p2.y , p3.x , p3.y , p4.x , p4.y );
	}

	/**
	 * Test intersection between two line segments.
	 *
	 * This uses the following algorithm:
	 *
	 *    "Intersection point of two lines (2 dimension)"
	 *    Author: Paul Bourke (april 1989)
	 *    http://astronomy.swin.edu.au/pbourke/geometry/lineline2d/
	 *
	 * @param   x1      Start point of line 1.
	 * @param   y1      Start point of line 1.
	 * @param   x2      End point of line 1.
	 * @param   y2      End point of line 1.
	 * @param   x3      Start point of line 2.
	 * @param   y3      Start point of line 2.
	 * @param   x4      End point of line 2.
	 * @param   y4      End point of line 2.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingLine_Line( final double x1 , final double y1 , final double x2 , final double y2 , final double x3 , final double y3 , final double x4 , final double y4 )
	{
		final double n1 = ( x4 - x3 ) * ( y1 - y3 ) - ( y4 - y3 ) * ( x1 - x3 );
		final double d  = ( y4 - y3 ) * ( x2 - x1 ) - ( x4 - x3 ) * ( y2 - y1 );

		if ( d == 0 ) /* lines are parallel */
		{
			if ( n1 == 0 ) /* they are coincident */
			{
				double sx1;
				double sx2;

				if ( x1 <= x2 ) { sx1 = x1; sx2 = x2; }
				           else { sx1 = x2; sx2 = x1; }

				if ( x3 <= x4 )
				{
					if ( x3 > sx1 ) sx1 = x3;
					if ( x4 < sx2 ) sx2 = x4;
				}
				else
				{
					if ( x4 > sx1 ) sx1 = x4;
					if ( x3 < sx2 ) sx2 = x3;
				}

				if ( sx1 > sx2 )
					return false;

				double sy1;
				double sy2;

				if ( y1 <= y2 ) { sy1 = y1; sy2 = y2; }
				           else { sy1 = y2; sy2 = y1; }

				if ( y3 < y4 )
				{
					if ( y3 > sy1 ) sy1 = y3;
					if ( y4 < sy2 ) sy2 = y4;
				}
				else
				{
					if ( y4 > sy1 ) sy1 = y4;
					if ( y3 < sy2 ) sy2 = y3;
				}

				if ( sy1 > sy2 )
					return false;

				return true;
			}
			else /* they are non-coincident */
			{
				return false;
			}
		}
		else /* lines intersect at some point */
		{
			/*
			 * Test if intersection point is within both line segments
			 */
			final double ua = n1 / d;
			if ( ua < 0.0 || ua > 1.0 ) return false;

			final double n2 = ( x2 - x1 ) * ( y1 - y3 ) - ( y2 - y1 ) * ( x1 - x3 );
			final double ub = n2 / d;
			if ( ub < 0.0 || ub > 1.0 ) return false;

			/*
			 * We have an intersection point!
			 */
			return true;
		}

	}

	/**
	 * Checks for intersection between a line and a point.
	 *
	 * @param   line    Line.
	 * @param   point   Point.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingLine_Point( final Polyline2D line , final PolyPoint2D point )
	{
		final double px = point.x;
		final double py = point.y;

		final PolyPoint2D p1  = line.getPoint( 0 );
		final double x1 = p1.x;
		final double y1 = p1.y;

		final PolyPoint2D p2  = line.getPoint( 1 );
		final double x2 = p2.x;
		final double y2 = p2.y;

		if ( ( ( x1 < x2 ) ? ( px >= x1 && px <= x2 ) : ( px >= x2 && px <= x1 ) )
		  && ( ( y1 < y2 ) ? ( py >= y1 && py <= y2 ) : ( py >= y2 && py <= y1 ) ) )
		{
			if ( ( x1 == x2 ) ||
			     ( y1 == y2 ) ||
			     ( x1 == px && y1 == py ) ||
			     ( x2 == px && y2 == py ) ||
			     ( ( px - x1 ) / ( x2 - x1 ) == ( py - y1 ) / ( y2 - y1 ) ) )
				return true;
		}

		return false;
	}

	/**
	 * Checks for intersection between a path and a line.
	 *
	 * @param   path    Path.
	 * @param   line    Line.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingPath_Line( final Polyline2D path , final Polyline2D line )
	{
		final PolyPoint2D p1 = line.getPoint( 0 );
		final PolyPoint2D p2 = line.getPoint( 1 );

		int i = path.getPointCount();

		PolyPoint2D p3 = path.getPoint( --i );
		PolyPoint2D p4;

		while ( i > 0 )
		{
			p4 = path.getPoint( --i );

			if ( isIntersectingLine_Line( p1.x , p1.y , p2.x , p2.y , p3.x , p3.y , p4.x , p4.y ) )
				return true;

			p3 = p4;
		}

		return false;
	}

	/**
	 * Checks for intersection between two paths.
	 *
	 * @param   path1   First path.
	 * @param   path2   Second path.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingPath_Path( final Polyline2D path1 , final Polyline2D path2 )
	{
		PolyPoint2D p;
		double x1;
		double y1;
		double x2;
		double y2;
		double x3;
		double y3;
		double x4;
		double y4;

		int i = path1.getPointCount();
		p  = path1.getPoint( --i );
		x2 = p.x;
		y2 = p.y;

		while ( --i >= 0 )
		{
			p = path1.getPoint( i );
			x1 = x2; x2 = p.x;
			y1 = y2; y2 = p.y;


			int j = path2.getPointCount();
			p  = path2.getPoint( --j );
			x4 = p.x;
			y4 = p.y;

			while ( --j >= 0 )
			{
				p = path2.getPoint( j );
				x3 = x4; x4 = p.x;
				y3 = y4; y4 = p.y;

				if ( isIntersectingLine_Line( x1 , y1 , x2 , y2 , x3 , y3 , x4 , y4 ) )
					return true;
			}
		}

		return false;
	}

	/**
	 * Checks for intersection between a path and a point.
	 *
	 * @param   path    Path.
	 * @param   point   Point.
	 *
	 * @return  true if shapes are intersecting, otherwise false.
	 */
	private static boolean isIntersectingPath_Point( final Polyline2D path , final PolyPoint2D point )
	{
		int i = path.getPointCount();

		final double px = point.x;
		final double py = point.y;

		PolyPoint2D p = path.getPoint( --i );
		double x1;
		double y1;
		double x2 = p.x;
		double y2 = p.y;

		while ( --i >= 0 )
		{
			p = path.getPoint( i );
			x1 = x2;
			y1 = y2;
			x2 = p.x;
			y2 = p.y;

			if ( ( ( x1 < x2 ) ? ( px >= x1 && px <= x2 ) : ( px >= x2 && px <= x1 ) )
			  && ( ( y1 < y2 ) ? ( py >= y1 && py <= y2 ) : ( py >= y2 && py <= y1 ) ) )
			{
				if ( ( x1 == x2 ) ||
				     ( y1 == y2 ) ||
				     ( x1 == px && y1 == py ) ||
				     ( x2 == px && y2 == py ) ||
				     ( ( px - x1 ) / ( x2 - x1 ) == ( py - y1 ) / ( y2 - y1 ) ) )
					return true;
			}

		}

		return false;
	}

	/**
	 * Checks if this polyline describes a rectangle.
	 * (4 sides and 4 corners of 90 degrees).
	 *
	 * @return  true if polyline is a rectangle
	 */
	public boolean isRectangle()
	{
		/*
		 * Rectangle always contains 5 (5? yes 5 : startpoint + 3 points + endpoint)
		 * and is closed.
		 */
		if ( getPointCount() != 5 || !isClosed() )
			return false;

		final double squareCos = Math.cos( Math.PI / 2.0 );

		/*
		 * Check if all 4 angles are 90 degrees.
		 */
		for ( int i = 0 ; i < 4 ; i++ )
		{
			final PolyPoint2D p1 = getPoint( i );
			final PolyPoint2D p2 = getPoint( i + 1 );
			final PolyPoint2D p3 = getPoint( ( i + 2 ) % 5 );

			final double angleCos = (p2.x - p1.x) * (p2.x - p3.x) + (p2.y - p1.y) * (p2.y - p3.y);

			/*
			 * Not quite precise measurement, but that is not really
			 * nessesary, the point is, that the shape is 'almost' a
			 * rectangle.
			 */
			if ( angleCos < squareCos - 0.1 || angleCos > squareCos + 0.1 )
				return false;
		}

		return true;
	}

	/**
	 * Places the specified polyline2d's (segments) head to tail to make one
	 * big polyline. The segments must make up a valid shape!.
	 *
	 * @param   segments    List with segments of Polyline2D to place head to tail.
	 *
	 * @return  The segments put together.
	 */
	private static Polyline2D placeHeadToTail( final List segments )
	{
		if ( segments.size() == 0 )
			return null;

		if ( segments.size() == 1 )
			return (Polyline2D)segments.get( 0 );

		//for ( int i = 0 ; i < segments.size() ; i++ )
		//{
			//Polyline2D segment = (Polyline2D)segments.get( i );
			//System.out.print( " " + i + ") " + segment.getPoint( 0 ) );
			//if ( segment.getPointCount() > 1 )
				//System.out.println( segment.getPoint( 1 ) );
			//else
				//System.out.println( "" );
		//}

		final Polyline2D result = new Polyline2D();

		Polyline2D segment = (Polyline2D)segments.get( 0 );
		PolyPoint2D firstHead = segment.getPointCount() > 1 ? segment.getPoint( 0 ) : null;
		PolyPoint2D lastHead = segment.getPoint( 0 );
		PolyPoint2D lastTail = segment.getPoint( segment.getPointCount() - 1 );
		PolyPoint2D head = null;
		PolyPoint2D tail = null;
		PolyPoint2D temp;
		segments.remove( 0 );

		if ( firstHead != null )
			result.addImpl( firstHead );
		else
			firstHead = lastTail;
		result.addImpl( lastTail );

		while ( segments.size() > 0 )
		{
			boolean foundNext = false;

			for ( int i = 0 ; i < segments.size() ; i++ )
			{
				segment = (Polyline2D)segments.get( i );
				head = segment.getPoint( 0 );
				tail = segment.getPoint( segment.getPointCount() - 1 );


				if ( lastTail.almostEquals( head , 0.01 ) )
				{
					if ( segment.getPointCount() == 1 )
						tail = null;
					else if ( lastHead.almostEquals( tail , 0.01 ) )
						continue;

					segments.remove( i );
					foundNext = true;
					break;
				}

				/*
				 * Reversed segment.
				 */
				else if( lastTail.almostEquals( tail , 0.01 ) && segment.getPointCount() > 1 )
				{
					temp = head;
					head = tail;
					tail = temp;

					if ( lastHead.almostEquals( tail , 0.01 ) )
						continue;

					segments.remove( i );
					foundNext = true;
					break;
				}
			}

			/*
			 * If we didn't found another one, it must be complete?
			 */
			if ( !foundNext )
			{
				//System.out.println( "Warning : Seperate segments, assuming shape is complete" );
				//System.out.println( "Result : \n" + result );
				return result;
				//throw new ProductionModelException( ProductionModelException.NOT_IMPLEMENTED , "Seperate segments" );
			}

			if ( tail != null )
			{
				if ( tail.almostEquals( firstHead , 0.01 ) )
				{
					result.close();
					//System.out.println( "Closing result (" + segments.size() + " left) : \n" + result );
					return result;
				}

				result.addImpl( tail );
				lastTail = tail;
				lastHead = head;
				//if ( head == null )
					//System.out.println( "!!!!!!!!!!! Head == null !!!!!!!!!!" );
			}
		}

		//System.out.println( "End call result : \n" + result );
		return result;
	}

	/**
	 * Construct polyline from string representation that was previously
	 * generated by the toString() method.
	 *
	 * @param   str     String representation of polyline.
	 *
	 * @return  Polyline that was created (never <code>null</code>).
	 */
	public static Polyline2D createInstance( final String str )
	{
		if ( ( str == null ) || ( str.length() == 0 ) )
			throw new IllegalArgumentException( str );

		final Polyline2D result = new Polyline2D();

		int start = 0;
		while ( start < str.length() )
		{
			int end = str.indexOf( (int)'|' , start );
			if ( end < 0 )
				end = str.length();

			final String pointStr = str.substring( start , end );
			result.addImpl( PolyPoint2D.createInstance( pointStr.trim() ) );

			start = end + 1;
		}

		return result;
	}

	/**
	 * Get Java 2D shape for polyline.
	 *
	 * @return  Java 2D shape.
	 */
	public Shape getShape()
	{
		final GeneralPath result = new GeneralPath();

		final int pointCount = getPointCount();
		if ( pointCount > 0 )
		{
			final PolyPoint2D p1 = getPoint( 0 );
			result.moveTo( (float)p1.x , (float)p1.y );

			for ( int i = 1 ; i < pointCount ; i++ )
			{
				final PolyPoint2D p2 = getPoint( i );
				if ( p2.isArcSegment() )
				{
					final double[] curves = PolyPoint2D.calculateArcCurves( p1.x , p1.y , p2.x , p2.y , p2.bulge );
					for ( int j = 0 ; j < curves.length ; j += 6 )
					{
						result.curveTo( (float)curves[ j     ] , (float)curves[ j + 1 ] ,
						                (float)curves[ j + 2 ] , (float)curves[ j + 3 ] ,
						                (float)curves[ j + 4 ] , (float)curves[ j + 5 ] );
					}

				}
				else
				{
					result.lineTo( (float)p2.x , (float)p2.y );
				}
			}
		}

		return result;
	}

	/**
	 * Get string representation of this object.
	 *
	 * @return  String representing this object.
	 */
	public synchronized String toString()
	{
		final StringBuffer sb = new StringBuffer();
		for ( int i = 0 ; i < getPointCount() ; i++ )
		{
			if ( i > 0 ) sb.append( '|' );
			sb.append( getPoint( i ) );
		}
		return sb.toString();
	}

	/**
	 * Transform a polyline using the specified transformation matrix. Although
	 * the matrix is specifies a 3D transformation, only the X and Y components
	 * of the result is saved.
	 *
	 * @param   xform           Transformation to apply to the polyline.
	 * @param   reversePoints   If set, the returned path will be reversed.
	 *
	 * @return  Polyline2D that is the result of the transformation (may return
	 *          this instance if the transformation has no effect).
	 */
	public synchronized Polyline2D transform( final Matrix3D xform , final boolean reversePoints )
	{
		final Polyline2D result;

		if ( !reversePoints && !hasEffect( xform ) )
		{
			result = this;
		}
		else
		{
			result = new Polyline2D();

			final int nrPoints = getPointCount();
			for ( int i = 0 ; i < nrPoints ; i++ )
			{
				final PolyPoint2D p = getPoint( reversePoints ? nrPoints - i - 1 : i );

				final double tx = p.x * xform.xx + p.y * xform.xy + xform.xo;
				final double ty = p.x * xform.yx + p.y * xform.yy + xform.yo;

				result.addImpl( new PolyPoint2D( tx , ty , p.bulge ) );
			}
		}

		return result;
	}
}
