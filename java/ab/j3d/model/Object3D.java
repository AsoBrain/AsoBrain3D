/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
package ab.j3d.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.PolyPoint2D;
import ab.j3d.Polyline2D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

import com.numdata.oss.ArrayTools;

/**
 * This class defined a 3D object node in a 3D tree. The 3D object consists of
 * points, edges, and faces.
 *
 * @author  G.B.M. Rupert
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Object3D
	extends Node3D
{
	/**
	 * List of faces in this object.
	 */
	private final List _faces = new ArrayList();

	/**
	 * Coordinates of points in object. Points are stored in an array of floats
	 * with a triplet for each point (x,y,z).
	 */
	double[] _pointCoords = {};

	/**
	 * This internal flag is set to indicate that the points or faces changed,
	 * so the normals need to be re-calculated.
	 */
	boolean _normalsDirty = true;

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field).
	 */
	private double[] _vertexNormals = null;

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field) occur.
	 */
	private double[] _faceNormals = null;

	/**
	 * This is used as cache storage for paint(Graphics2D,Matrix3D,Matrix3D).
	 */
	private static double[] _paintPointCoordsCache = {};

	/**
	 * Construct base object. Additional properties need to be set to make the
	 * object usable.
	 */
	public Object3D()
	{
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested,
	 * the ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used
	 * to rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for y!
	 *
	 * @param   xform               Transform to apply to points.
	 * @param   xs                  X coordinates of 2D outline.
	 * @param   zs                  Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   texture             Texture to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 *
	 * @FIXME should be a utility/factory method.
	 */
	public Object3D( final Matrix3D xform , final double[] xs , final double[] zs , final int detail , final TextureSpec texture , final boolean smoothCircumference , final boolean closeEnds )
	{
		double[] coords = new double[ xs.length * detail * 3 ];
		int v = 0;

		int   iPrev;
		double xPrev;

		int   iCur = v / 3;
		double xCur = 0;
		double zCur;

		for ( int i = 0 ; i < xs.length ; i++ )
		{
			iPrev = iCur;
			xPrev = xCur;

			iCur = v / 3;
			xCur = xs[ i ];
			zCur = zs[ i ];

			if ( xCur == 0.0 )
			{
				coords[ v++ ] = 0;
				coords[ v++ ] = 0;
				coords[ v++ ] = zCur;
			}
			else for ( int j = 0 ; j < detail ; j++ )
			{
				final double a = j * 2 * Math.PI / detail;

				coords[ v++ ] =  Math.sin( a ) * xCur;
				coords[ v++ ] = -Math.cos( a ) * xCur;
				coords[ v++ ] = zCur;
			}

			/*
			 * First control point. Add closing face is requested.
			 */
			if ( i == 0 )
			{
				if ( xCur != 0.0 && closeEnds )
				{
					final int[] fv = new int[ detail ];
					for ( int j = 0 ; j < detail ; j++ )
						fv[ j ] = iCur + j;

					addFace( fv , texture , false );
				}
			}
			/*
			 * 2nd + later control points.
			 */
			else if ( xCur != 0.0 || xPrev != 0.0 )
			{
				for ( int j = 0 ; j < detail ; j++ )
				{
					final int nextJ = ( j + 1 ) % detail;

					final int[] fv;
					if ( xCur != 0.0 && xPrev != 0 )
						fv = new int[] { iPrev + j , iCur + j , iCur + nextJ , iPrev + nextJ };
					else if ( xCur != 0.0 )
						fv = new int[] { iPrev , iCur + j , iCur + nextJ };
					else /*if ( xPrev != 0.0 )*/
						fv = new int[] { iPrev + j , iCur , iPrev + nextJ };

					addFace( fv , texture , smoothCircumference );
				}
			}
		}

		/*
		 * Add closing face is requested.
		 */
		if ( ( xCur != 0.0 ) && closeEnds )
		{
			final int[] fv = new int[ detail ];
			for ( int j = 0 ; j < detail ; j++ )
				fv[ j ] = iCur + detail - ( 1 + j );

			addFace( fv , texture , false );
		}


		coords = (double[])ArrayTools.setLength( coords , double.class , v );
		if ( xform != null )
			xform.transform( coords , coords , v / 3 );

		_pointCoords  = coords;
		_normalsDirty = true;
	}

	/**
	 * Clear all data.
	 * <p />
	 * This removes all faces and point coordinates, essentially reverting to
	 * the state after calling the default constructor. However, internal
	 * cache/buffers are preserved to reduce memory fragmentation.
	 */
	public void clear()
	{
		_faces.clear();
		_pointCoords = null;
		_normalsDirty = true;
	}

	/**
	 * The following text is from http://nate.scuzzy.net/normals/normals.html.
	 *
	 * There is no way to calculate a true vertex normal since a vertex is just
	 * a point in space, so the best we can get is an approximation for our vertex
	 * normals. If you understand the ideas behind face normals, calculating vertex
	 * normals will be a breeze. A vertex normal is simply the average of the face
	 * normals that surround a particular vertex. So how do you go about
	 * calculating a vertex normal? For every face in your face list you need to
	 * generate the normal to that face but, do not normalize it. Then for each
	 * vertex in the face add the face normal to the corresponding vertex normal
	 * for the current vertex. After you have gone through all the verticies of
	 * the face you then can normalize the face normal, do not normalize the vertex
	 * normal. After you have done this for all faces walk the vertex normal list
	 * and normalize each normal. Once you have done this you have your vertex
	 * normals. Pretty simple isn't it? Thanks to hude for this better method for
	 * calculating vertex normals.
	 *
	 * PS: We do not follow the suggested implementation in this article, but use
	 *     its suggested calculations.
	 */
	private synchronized void calculateNormals()
	{
		if ( _normalsDirty )
		{
			final int      faceCount   = getFaceCount();
			final double[] pointCoords = getPointCoords();
			final int      pointCount  = getPointCount();

			final double[] faceNormals = (double[])ArrayTools.ensureLength( _faceNormals , double.class , -1 , faceCount * 3 );
			_faceNormals = faceNormals;

			final double[] vertexNormals = (double[])ArrayTools.ensureLength( _vertexNormals , double.class , -1 , pointCount * 3 );
			_vertexNormals = vertexNormals;

			/*
			 * Generate face normals, but do not normalize them yet.
			 */
			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face        = getFace( faceIndex );
				final int    vertexCount = face.getVertexCount();
				final int    normalIndex = faceIndex * 3;

				if ( vertexCount < 3 )
				{
					faceNormals[ normalIndex     ] = 0;
					faceNormals[ normalIndex + 1 ] = 0;
					faceNormals[ normalIndex + 2 ] = 0;
				}
				else
				{
					final int[] pointIndices = face.getPointIndices();

					final int vi1 = pointIndices[ 0 ] * 3;
					final int vi2 = pointIndices[ 1 ] * 3;
					final int vi3 = pointIndices[ 2 ] * 3;

					final double u1 = pointCoords[ vi3     ] - pointCoords[ vi1     ];
					final double u2 = pointCoords[ vi3 + 1 ] - pointCoords[ vi1 + 1 ];
					final double u3 = pointCoords[ vi3 + 2 ] - pointCoords[ vi1 + 2 ];

					final double v1 = pointCoords[ vi2     ] - pointCoords[ vi1     ];
					final double v2 = pointCoords[ vi2 + 1 ] - pointCoords[ vi1 + 1 ];
					final double v3 = pointCoords[ vi2 + 2 ] - pointCoords[ vi1 + 2 ];

					faceNormals[ normalIndex     ] = u2 * v3 - u3 * v2;
					faceNormals[ normalIndex + 1 ] = u3 * v1 - u1 * v3;
					faceNormals[ normalIndex + 2 ] = u1 * v2 - u2 * v1;
				}
			}

			/*
			 * Generate vertex normals.
			 */
			for ( int pointIndex = 0 ; pointIndex < pointCount ; pointIndex++ )
			{
				double vnx = 0;
				double vny = 0;
				double vnz = 0;

				/*
				 * Sum normals of faces that use this point.
				 */
				for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
				{
					final Face3D face         = (Face3D)_faces.get( faceIndex );
					final int    vertexCount  = face.getVertexCount();
					final int[]  pointIndices = face.getPointIndices();

					for ( int vertexIndex = vertexCount ; --vertexIndex >= 0 ; )
					{
						if ( pointIndices[ vertexIndex ] == pointIndex )
						{
							final int normalIndex = faceIndex * 3;
							vnx += faceNormals[ normalIndex     ];
							vny += faceNormals[ normalIndex + 1 ];
							vnz += faceNormals[ normalIndex + 2 ];
							break;
						}
					}
				}

				/*
				 * Normalize vertex normal.
				 */
				if ( vnx != 0 || vny != 0 || vnz != 0 )
				{
					final double l = Math.sqrt( vnx * vnx + vny * vny + vnz * vnz );
					vnx /= l;
					vny /= l;
					vnz /= l;
				}

				final int ni = pointIndex * 3;
				vertexNormals[ ni     ] = vnx;
				vertexNormals[ ni + 1 ] = vny;
				vertexNormals[ ni + 2 ] = vnz;
			}

			/*
			 * Normalize face normals.
			 */
			for ( int faceIndex = 0 ; faceIndex < _faces.size() ; faceIndex++ )
			{
				final int normalIndex = faceIndex * 3;

				final double nx = faceNormals[ normalIndex     ];
				final double ny = faceNormals[ normalIndex + 1 ];
				final double nz = faceNormals[ normalIndex + 2 ];

				if ( nx != 0 || ny != 0 || nz != 0 )
				{
					final double l = Math.sqrt( nx * nx + ny * ny + nz * nz );

					faceNormals[ normalIndex     ] = nx / l;
					faceNormals[ normalIndex + 1 ] = ny / l;
					faceNormals[ normalIndex + 2 ] = nz / l;
				}
			}
		}
		_normalsDirty = false;
	}

	/**
	 * Get outer bounds (bounding box) of the object. Optionally, an existing bounding box
	 * can be specified. The resulting bounds contains all points within the object and
	 * the existing bounding box (if any).
	 *
	 * @param   xform       Transform to apply to points.
	 * @param   bounds      Existing bounding box to use.
	 *
	 * @return  Combined bounding box of this object and the existing bounding box (if any).
	 */
	public final Bounds3D getBounds( final Matrix3D xform , final Bounds3D bounds )
	{
		final int pointCount = getPointCount();
		if ( pointCount < 1 )
			return bounds;

		final boolean isXform = ( xform != null ) && ( xform != Matrix3D.INIT ) && ( !Matrix3D.INIT.equals( xform ) );

		double x1;
		double y1;
		double z1;
		double x2;
		double y2;
		double z2;
		final Bounds3D result;
		if ( bounds != null )
		{
			x1 = bounds.v1.x;
			y1 = bounds.v1.y;
			z1 = bounds.v1.z;
			x2 = bounds.v2.x;
			y2 = bounds.v2.y;
			z2 = bounds.v2.z;
			result = bounds;
		}
		else
		{
			x1 = Double.MAX_VALUE;
			y1 = Double.MAX_VALUE;
			z1 = Double.MAX_VALUE;
			x2 = Double.MIN_VALUE;
			y2 = Double.MIN_VALUE;
			z2 = Double.MIN_VALUE;
			result = Bounds3D.INIT;
		}

		double x;
		double y;
		double z;
		double tx;
		double ty;

		int i = 0;
		for ( int j = 0 ; j < pointCount ; j++ )
		{
			x = _pointCoords[ i++ ];
			y = _pointCoords[ i++ ];
			z = _pointCoords[ i++ ];

			if ( isXform )
			{
				tx = xform.transformX( x , y , z );
				ty = xform.transformY( x , y , z );
				z  = xform.transformZ( x , y , z );
				x  = tx;
				y  = ty;
			}

			if ( x < x1 ) x1 = x;
			if ( y < y1 ) y1 = y;
			if ( z < z1 ) z1 = z;
			if ( x > x2 ) x2 = x;
			if ( y > y2 ) y2 = y;
			if ( z > z2 ) z2 = z;
		}

		if ( x1 > x2 || y1 > y2 || z1 > z2 )
			return null;

		return result.set( result.v1.set( x1 , y1 , z1 ) , result.v2.set( x2 , y2 , z2 ) );
	}

	/**
	 * Get transformed face normals.
	 *
	 * @return  Transformed face normals.
	 */
	public final double[] getFaceNormals()
	{
		if ( _normalsDirty )
			calculateNormals();

		return _faceNormals;
	}

	/**
	 * Get point index for a point with the specified coordinates. If the point
	 * is not defined yet, a new point is added and its index is returned.
	 *
	 * @param   x   X-coordinate of the point.
	 * @param   y   Y-coordinate of the point.
	 * @param   z   Z-coordinate of the point.
	 *
	 * @return  The index of the point.
	 */
	int getOrAddPointIndex( final double x , final double y , final double z )
	{
		final double[] pointCoords = getPointCoords();
		final int      pointCount  = getPointCount();

		int index = pointCount * 3;
		while ( ( index -= 3 ) >= 0 )
		{
			if ( ( x == pointCoords[ index ] ) && ( y == pointCoords[ index + 1 ] ) && ( z == pointCoords[ index + 2 ] ) )
				break;
		}

		if ( index < 0 )
		{
			index = pointCount * 3;
			final double[] newCoords = (double[])ArrayTools.ensureLength( pointCoords , double.class , -1 , index + 3 );
			newCoords[ index     ] = x;
			newCoords[ index + 1 ] = y;
			newCoords[ index + 2 ] = z;

			_pointCoords  = newCoords;
			_normalsDirty = true;
		}

		return index / 3;
	}

	/**
	 * Get number of points in the model. Points are shared amongst faces to
	 * reduce the number of transformations required (points have an average
	 * use count that approaches 3, so the gain is significant).
	 *
	 * @return  Number of points.
	 */
	public final int getPointCount()
	{
		return _pointCoords.length / 3;
	}

	/**
	 * Get coordinates of all points in this object.
	 *
	 * @return  Float array with triplet for each point in this object.
	 */
	public final double[] getPointCoords()
	{
		return _pointCoords;
	}

	/**
	 * Set coordinates of all points in this object. This is only allowed when
	 * no faces have been added yet, since the object integrity is otherwise
	 * lost.
	 *
	 * @param   pointCoords     Float array with triplet for each point.
	 *
	 * @throws  IllegalStateException if faces have been added to the object.
	 */
	public final void setPointCoords( final double[] pointCoords )
	{
		if ( !_faces.isEmpty() )
			throw new IllegalStateException( "can't set coordinates after adding faces" );

		_pointCoords = pointCoords;
		_normalsDirty = true;
	}

	/**
	 * Get transformed vertex normals. Vertex normals are pseudo-normals based
	 * on average face normals at common points.
	 *
	 * @return  Transformed vertex normals.
	 */
	public final double[] getVertexNormals()
	{
		if ( _normalsDirty )
			calculateNormals();

		return _vertexNormals;
	}

	/**
	 * Get number of faces in the model.
	 *
	 * @return  Number of faces.
	 */
	public final int getFaceCount()
	{
		return _faces.size();
	}

	/**
	 * Get face with the specified index.
	 *
	 * @param   index   Index of face to retrieve.
	 *
	 * @return  Face with the specified index.
	 */
	public final Face3D getFace( final int index )
	{
		return (Face3D)_faces.get( index );
	}

	/**
	 * Add a face to this <code>Object3D</code>.
	 *
	 * @param   face    Face to add.
	 */
	protected final void addFace( final Face3D face )
	{
		_faces.add( face );
		_normalsDirty = true;
	}

	/**
	 * Add 'empty' face to this object. Vertices can be added to the returned
	 * face instance.
	 *
	 * @param   texture     Texture to apply to the face.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 *
	 * @return  Face that was added.
	 */
	public final Face3D addFace( final TextureSpec texture , final float opacity , final boolean smooth )
	{
		final Face3D result = new Face3D( this , null , texture , null , null , opacity , smooth );
		addFace( result );
		return result;
	}

	/**
	 * Add face to this object.
	 *
	 * @param   pointIndices    Point indices of added face. These indices refer
	 *                          to points previously defined in this object.
	 * @param   texture         Texture to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 */
	public final void addFace( final int[] pointIndices , final TextureSpec texture , final boolean smooth )
	{
		addFace( pointIndices , texture , null , null , 1.0f , smooth );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   pointIndices    Point indices of added face. These indices refer
	 *                          to points previously defined in this object.
	 * @param   texture         Texture to apply to the face.
	 * @param   textureU        Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV        Vertical texture coordinates (<code>null</code> = none).
	 * @param   opacity         Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 */
	public final void addFace( final int[] pointIndices , final TextureSpec texture , final int[] textureU , final int[] textureV , final float opacity , final boolean smooth )
	{
		addFace( new Face3D( this , pointIndices , texture , textureU , textureV , opacity , smooth ) );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   points      Points that define the face.
	 * @param   texture     Texture to apply to the face.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public final void addFace( final Vector3D[] points , final TextureSpec texture , final boolean smooth )
	{
		addFace( points , texture , null , null , 1.0f , smooth );
	}

	/**
	 * Add face to this object.
	 *
	 * @param   points      Points that define the face.
	 * @param   texture     Texture to apply to the face.
	 * @param   textureU    Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV    Vertical texture coordinates (<code>null</code> = none).
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public final void addFace( final Vector3D[] points , final TextureSpec texture , final int[] textureU , final int[] textureV , final float opacity , final boolean smooth )
	{
		final Face3D face = addFace( texture , opacity , smooth );
		face.ensureCapacity( points.length );

		for ( int i = 0 ; i < points.length ; i++ )
			face.addVertex( points[ i ] , ( textureU == null ) ? -1 : textureU[ i ] , ( textureV == null ) ? -1 : textureV[ i ] );
	}

	/**
	 * Add face based on a 2D shape to this object.
	 *
	 * @param   base        Location/orientation of face.
	 * @param   shape       Shape of face relative to the base.
	 * @param   reversePath If set, the returned path will be reversed.
	 * @param   flipTexture Swap texture coordinates to rotate 90 degrees.
	 * @param   texture     Texture to apply to the face.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public final void addFace( final Matrix3D base , final Polyline2D shape , final boolean reversePath , final boolean flipTexture , final TextureSpec texture , final float opacity , final boolean smooth )
	{
		final int nrVertices = shape.getPointCount() + ( shape.isClosed() ? -1 : 0 );

		if ( nrVertices < 3 )
		{
			/* no edge/point support (yet?) */
		}
		else if ( ( texture != null ) && texture.isTexture() )
		{
			final double txBase = -base.xo * base.xx - base.yo * base.yx - base.zo * base.zx;
			final double tyBase = -base.xo * base.xy - base.yo * base.yy - base.zo * base.zy;

			final int[] vertU = new int[ nrVertices ];
			final int[] vertV = new int[ nrVertices ];

			int minU = 0;
			int maxU = 0;
			int minV = 0;
			int maxV = 0;

			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );

				final int u = (int)( ( flipTexture ? ( tyBase + point.y ) : ( txBase + point.x ) ) * texture.textureScale );
				final int v = (int)( ( flipTexture ? ( txBase + point.x ) : ( tyBase + point.y ) ) * texture.textureScale );

				if ( i == 0 || u < minU ) minU = u;
				if ( i == 0 || u > maxU ) maxU = u;
				if ( i == 0 || v < minV ) minV = v;
				if ( i == 0 || v > maxV ) maxV = v;

				vertU[ i ] = u;
				vertV[ i ] = v;
			}

			final int adjustU = getRangeAdjustment( minU , texture.getTextureWidth( null ) );
			final int adjustV = getRangeAdjustment( minV , texture.getTextureHeight( null ) );

			final Face3D face = addFace( texture , opacity , smooth );
			face.ensureCapacity( nrVertices );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , vertU[ i ] + adjustU , vertV[ i ] + adjustV );
			}
		}
		else
		{
			final Face3D face = addFace( texture , opacity , smooth );
			face.ensureCapacity( nrVertices );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , 0 , 0 );
			}
		}
	}

	/**
	 * Helper method to determine an adjustment value to get a reference value in
	 * a range between 0 and a specified maximum value - 1. The adjustment value
	 * will always be a multiple of the specified maximum value.
	 * <p />
	 * This is used to adjust minimum texture coordinates to the lowest positive
	 * coordinate within the texture image. The adjustment value should be added
	 * to all texture coordinates to make the adjustment.
	 *
	 * @param   value   Value to adjust.
	 * @param   range   Specifies the range 0 to (range - 1).
	 *
	 * @return  Adjustment value to add to the value.
	 */
	static int getRangeAdjustment( final int value , final int range )
	{
		final int result;
		if ( value < 0 )
		{
			result = ( -value + range - 1 ) / range * range;
		}
		else
		{
			result = -value + ( value % range );
		}
		return result;
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Color outlineColor , final Color fillColor , final double shadeFactor )
	{
		final int faceCount = getFaceCount();
		if ( ( faceCount > 0 ) && ( ( outlineColor != null ) || ( fillColor != null ) ) )
		{
			final int pointCount = getPointCount();

			/*
			 * If the array is to small, create a larger one.
			 */
			final double[] pointCoords = viewTransform.transform( _pointCoords , _paintPointCoordsCache , pointCount );
			_paintPointCoordsCache = pointCoords;

			int maxVertexCount = 0;
			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face = getFace( faceIndex );
				maxVertexCount = Math.max( maxVertexCount , face.getVertexCount() );
			}

			final int[] xs = new int[ maxVertexCount ];
			final int[] ys = new int[ maxVertexCount ];

			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face3D face         = getFace( faceIndex );
				final int    vertexCount  = face.getVertexCount();
				final int[]  pointIndices = face.getPointIndices();

				if ( vertexCount > 0 )
				{
					boolean show = true;
					for ( int p = 0 ; p < vertexCount ; p++ )
					{
						final int vi = pointIndices[ p ] * 3;

						final double x  = pointCoords[ vi ];
						final double y  = pointCoords[ vi + 1 ];
						final double z  = pointCoords[ vi + 2 ];

						final int ix = (int)gTransform.transformX( x , y , z );
						final int iy = (int)gTransform.transformY( x , y , z );

						/*
						 * Perform backface removal if we have 3 points, so we can calculate the normal.
						 *
						 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
						 */
						if ( p == 2 )
						{
							show = ( ( ( ( xs[ 0 ] - xs[ 1 ] ) * ( iy - ys[ 1 ] ) )
									 - ( ( ys[ 0 ] - ys[ 1 ] ) * ( ix - xs[ 1 ] ) ) ) <= 0 );
							if ( !show )
								break;
						}

						xs[ p ] = ix;
						ys[ p ] = iy;
					}

					if ( show )
					{
						if ( fillColor != null )
						{
							if ( ( shadeFactor > 0 ) && ( shadeFactor <= 1 ) )
							{
								final double[] faceNormals = getFaceNormals();
								final int      normalIndex = faceIndex * 3;
								final double   nx          = faceNormals[ normalIndex     ];
								final double   ny          = faceNormals[ normalIndex + 1 ];
								final double   nz          = faceNormals[ normalIndex + 2 ];

								g.setColor( getAdjustedFillColor( viewTransform , fillColor , shadeFactor , nx , ny , nz ) );
							}
							else
							{
								g.setColor( fillColor );
							}

							if ( vertexCount < 3 ) /* point or line */
							{
								if ( outlineColor == null )
									g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
							}
							else
							{
								g.fillPolygon( xs , ys , vertexCount );
							}
						}

						if ( outlineColor != null ) /* point or line */
						{
							g.setColor( outlineColor );
							if ( vertexCount < 3 )
								g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
							else
								g.drawPolygon( xs , ys , vertexCount );
						}
					}
				}
			}
		}
	}

	/**
	 * Get adjusted fill color based on the specified properties.
	 * <p />
	 * The <code>shadeFactor</code> is used to modify the fill color based on
	 * the Z component of a face's normal vector. A typical value of
	 * <code>0.5</code> would render faces pointing towards the Z-axis at 100%,
	 * and faces perpendicular to the Z-axis at 50%; specifying <code>0.0</code>
	 * completely disables the effect (always 100%); whilst <code>1.0</code>
	 * makes faces perpendicular to the Z-axis black (0%).
	 *
	 * @param   viewTransform   Transformation from object's to view coordinate system.
	 * @param   fillColor       Color to use for filling (<code>null</code> to disable drawing).
	 * @param   shadeFactor     Amount of shading that may be applied (0=none, 1=extreme).
	 * @param   nx              X-component of normal vector.
	 * @param   ny              Y-component of normal vector.
	 * @param   nz              Z-component of normal vector.
	 *
	 * @return  Fill color.
	 */
	protected static final Color getAdjustedFillColor( final Matrix3D viewTransform , final Color fillColor , final double shadeFactor , final double nx , final double ny , final double nz )
	{
		final Color result;

		if ( ( fillColor == null ) || ( viewTransform == null ) || ( shadeFactor == 0 ) )
		{
			result = fillColor;
		}
		else
		{
			final double rnz = nx * viewTransform.zx + ny * viewTransform.zy + nz * viewTransform.zz;

			final double factor = Math.min( 1.0 , ( 1 - shadeFactor ) + shadeFactor * Math.abs( rnz ) );
			if ( factor < 1 )
			{
				result = new Color( (int)( factor * fillColor.getRed()   + 0.5 ) ,
				                    (int)( factor * fillColor.getGreen() + 0.5 ) ,
				                    (int)( factor * fillColor.getBlue()  + 0.5 ) );
			}
			else
			{
				result = fillColor;
			}
		}

		return result;
	}
}
