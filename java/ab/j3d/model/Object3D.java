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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.PolyPoint2D;
import ab.j3d.Polyline2D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

/**
 * This class defined a 3D object node in a 3D tree. The 3D object consists of
 * vertices, edges, and faces.
 *
 * @author  G.B.M. Rupert
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class Object3D
	extends Node3D
{
	/**
	 * Vertices of model. Vertices are stored in an array of floats with
	 * a triplet for each vertex (x,y,z).
	 */
	private float[] _vertices = {};

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field) occur.
	 */
	private float[] _faceNormals = null;

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field).
	 */
	private float[] _vertexNormals = null;

	/**
	 * List of faces in this object.
	 */
	private final List _faces = new ArrayList();

	/**
	 * This internal flag is set to indicate that the vertices, edges, or
	 * faces changed and the normals need to be re-calculated.
	 */
	private boolean _normalsDirty = true;

	/**
	 * This is used as cache storage for paint(Graphics,Matrix3D,Matrix3D).
	 */
	private static final float[] _paintVertexCache = {};

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
	 * @param   xform               Transform to apply to vertices.
	 * @param   xs                  X coordinates of 2D outline.
	 * @param   zs                  Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   texture             Texture to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 *
	 * @FIXME should be a utility/factory method.
	 */
	public Object3D( final Matrix3D xform , final float[] xs , final float[] zs , final int detail , final TextureSpec texture , final boolean smoothCircumference , final boolean closeEnds )
	{
		float[]       vertices    = new float[ xs.length * detail * 3 ];
		int[][]       faceVert    = new int[ xs.length * detail ][];
		TextureSpec[] faceMat     = new TextureSpec[ faceVert.length ];
		float[]       faceOpacity = new float[ faceVert.length ];
		boolean[]     faceSmooth  = new boolean[ faceVert.length ];
		int v = 0;
		int f = 0;

		int   iPrev;
		float xPrev;

		int   iCur = v / 3;
		float xCur = 0;
		float zCur;

		for ( int i = 0 ; i < xs.length ; i++ )
		{
			iPrev = iCur;
			xPrev = xCur;

			iCur = v / 3;
			xCur = xs[ i ];
			zCur = zs[ i ];

			if ( xCur == 0.0f )
			{
				vertices[ v++ ] = 0;
				vertices[ v++ ] = 0;
				vertices[ v++ ] = zCur;
			}
			else for ( int j = 0 ; j < detail ; j++ )
			{
				final float a = (float)( j * 2 * Math.PI / detail );

				vertices[ v++ ] =  (float)(Math.sin( a ) * xCur);
				vertices[ v++ ] = -(float)(Math.cos( a ) * xCur);
				vertices[ v++ ] = zCur;
			}

			/*
			 * First control point. Add closing face is requested.
			 */
			if ( i == 0 )
			{
				if ( xCur != 0.0f && closeEnds )
				{
					final int[] fv = new int[ detail ];
					for ( int j = 0 ; j < detail ; j++ )
						fv[ j ] = iCur + j;

					faceVert   [ f   ] = fv;
					faceMat    [ f   ] = texture;
					faceOpacity[ f   ] = 1.0f;
					faceSmooth [ f++ ] = false;
				}
			}
			/*
			 * 2nd + later control points.
			 */
			else if ( xCur != 0.0f || xPrev != 0.0f )
			{
				for ( int j = 0 ; j < detail ; j++ )
				{
					final int nextJ = ( j + 1 ) % detail;

					if ( xCur != 0.0f && xPrev != 0 )
						faceVert[ f ] = new int[] { iPrev + j , iCur + j , iCur + nextJ , iPrev + nextJ };
					else if ( xCur != 0.0f )
						faceVert[ f ] = new int[] { iPrev , iCur + j , iCur + nextJ };
					else /*if ( xPrev != 0f )*/
						faceVert[ f ] = new int[] { iPrev + j , iCur , iPrev + nextJ };

					faceMat    [ f   ] = texture;
					faceOpacity[ f   ] = 1.0f;
					faceSmooth [ f++ ] = smoothCircumference;
				}
			}
		}

		/*
		 * Add closing face is requested.
		 */
		if ( xCur != 0.0f && closeEnds )
		{
			final int[] fv = new int[ detail ];
			for ( int j = 0 ; j < detail ; j++ )
				fv[ j ] = iCur + detail - ( 1 + j );

			faceVert   [ f   ] = fv;
			faceMat    [ f   ] = texture;
			faceOpacity[ f   ] = 1.0f;
			faceSmooth [ f++ ] = false;
		}


		/*
		 * Construct final vertex and face arrays
		 */
		if ( v != vertices.length )
		{
			final float[] newVertices = new float[ v ];
			System.arraycopy( vertices , 0 , newVertices , 0 , v );
			vertices = newVertices;
		}

		if ( f != faceVert.length )
		{
			final int[][] newFaces = new int[ f ][];
			System.arraycopy( faceVert , 0 , newFaces , 0 , f );
			faceVert = newFaces;

			final TextureSpec[] newMat = new TextureSpec[ f ];
			System.arraycopy( faceMat , 0 , newMat , 0 , f );
			faceMat = newMat;

			final float[] newOpacity = new float[ f ];
			System.arraycopy( faceOpacity , 0 , newOpacity , 0 , f );
			faceOpacity = newOpacity;

			final boolean[] newSmooth = new boolean[ f ];
			System.arraycopy( faceSmooth , 0 , newSmooth , 0 , f );
			faceSmooth = newSmooth;
		}

		/*
		 * Transform vertices
		 */
		if ( xform != null )
			xform.transform( vertices , vertices , vertices.length / 3 );

		set( vertices , faceVert , faceMat , null , null , faceOpacity , faceSmooth );
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
			final int     faceCount         = _faces.size();
			final int     faceCountTimes3   = faceCount * 3;
			final float[] vertices          = _vertices;
			final int     vertexCountTimes3 = vertices.length;
			final int     vertexCount       = vertexCountTimes3 / 3;

			float[] faceNormals = _faceNormals;
			if ( faceNormals == null || faceNormals.length < faceCountTimes3 )
				_faceNormals = faceNormals = new float[ faceCountTimes3 ];

			float[] vertexNormals = _vertexNormals;
			if ( vertexNormals == null || vertexNormals.length < vertexCountTimes3 )
				_vertexNormals = vertexNormals = new float[ vertexCountTimes3 ];

			/*
			 * Generate face normals, but do not normalize them yet.
			 */
			for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
			{
				final Face face        = getFace( faceIndex );
				final int  nrPoints    = face.getVertexCount();
				final int  normalIndex = faceIndex * 3;

				if ( nrPoints < 3 )
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

					final float u1 = vertices[ vi3     ] - vertices[ vi1     ];
					final float u2 = vertices[ vi3 + 1 ] - vertices[ vi1 + 1 ];
					final float u3 = vertices[ vi3 + 2 ] - vertices[ vi1 + 2 ];

					final float v1 = vertices[ vi2     ] - vertices[ vi1     ];
					final float v2 = vertices[ vi2 + 1 ] - vertices[ vi1 + 1 ];
					final float v3 = vertices[ vi2 + 2 ] - vertices[ vi1 + 2 ];

					faceNormals[ normalIndex     ] = u2 * v3 - u3 * v2;
					faceNormals[ normalIndex + 1 ] = u3 * v1 - u1 * v3;
					faceNormals[ normalIndex + 2 ] = u1 * v2 - u2 * v1;
				}
			}

			/*
			 * Generate vertex normals.
			 */
			for ( int vertexIndex = 0 ; vertexIndex < vertexCount ; vertexIndex++ )
			{
				float vnx = 0;
				float vny = 0;
				float vnz = 0;

				/*
				 * Sum normals of faces that use this vertex.
				 */
				for ( int faceIndex = 0 ; faceIndex < faceCount ; faceIndex++ )
				{
					final Face  face         = (Face)_faces.get( faceIndex );
					final int   nrPoints     = face.getVertexCount();
					final int[] pointIndices = face.getPointIndices();

					for ( int faceVertexIndex = nrPoints ; --faceVertexIndex >= 0 ; )
					{
						if ( pointIndices[ faceVertexIndex ] == vertexIndex )
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
					final float l = (float)Math.sqrt( vnx * vnx + vny * vny + vnz * vnz );
					vnx /= l;
					vny /= l;
					vnz /= l;
				}

				final int ni = vertexIndex * 3;
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

				final float nx = faceNormals[ normalIndex     ];
				final float ny = faceNormals[ normalIndex + 1 ];
				final float nz = faceNormals[ normalIndex + 2 ];

				if ( nx != 0 || ny != 0 || nz != 0 )
				{
					final float l = (float)Math.sqrt( nx * nx + ny * ny + nz * nz );

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
	 * can be specified. The resulting bounds contains all vertices within the object and
	 * the existing bounding box (if any).
	 *
	 * @param   xform       Transform to apply to vertices.
	 * @param   bounds      Existing bounding box to use.
	 *
	 * @return  Combined bounding box of this object and the existing bounding box (if any).
	 */
	public final Bounds3D getBounds( final Matrix3D xform , final Bounds3D bounds )
	{
		if ( _vertices == null || _vertices.length < 3 )
			return bounds;

		final boolean isXform = ( xform != null && xform != Matrix3D.INIT && !Matrix3D.INIT.equals( xform ) );

		float x1;
		float y1;
		float z1;
		float x2;
		float y2;
		float z2;
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
			x1 = Float.MAX_VALUE;
			y1 = Float.MAX_VALUE;
			z1 = Float.MAX_VALUE;
			x2 = Float.MIN_VALUE;
			y2 = Float.MIN_VALUE;
			z2 = Float.MIN_VALUE;
			result = Bounds3D.INIT;
		}

		float x;
		float y;
		float z;
		float tx;
		float ty;

		int i = 0;
		while ( i < _vertices.length )
		{
			x = _vertices[ i++ ];
			y = _vertices[ i++ ];
			z = _vertices[ i++ ];

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
	 * Get number of vertices in the model.
	 *
	 * @return  Number of vertices.
	 */
	public final int getTotalVertexCount()
	{
		return( _vertices.length / 3 );
	}

	/**
	 * Get transformed vertex normals.
	 *
	 * @return  Transformed vertex normals.
	 */
	public final float[] getVertexNormals()
	{
		if ( _normalsDirty )
			calculateNormals();

		return _vertexNormals;
	}

	/**
	 * Get vertices.
	 *
	 * @return  Float array with vertices that define the object.
	 */
	public final float[] getVertices()
	{
		return _vertices;
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
	int getOrAddPointIndex( final float x , final float y , final float z )
	{
		float[] vertices = _vertices;
		final int nrVerticesTimes3 = vertices.length;

		int index = 0;
		while ( ( index < nrVerticesTimes3 )
		     && ( ( x != vertices[ index     ] )
		       || ( y != vertices[ index + 1 ] )
		       || ( z != vertices[ index + 2 ] ) ) )
		{
			index += 3;
		}
		if ( index == nrVerticesTimes3 )
		{
//			if ( index >= _vertices.length )
			{
				vertices = new float[ index + 3 ];
				System.arraycopy( _vertices , 0 , vertices , 0 , index );
				_vertices = vertices;
			}

			vertices[ index     ] = x;
			vertices[ index + 1 ] = y;
			vertices[ index + 2 ] = z;

			_normalsDirty = true;
		}

		return index / 3;
	}

	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D viewTransform , final Color outlineColor , final Color fillColor , final float shadeFactor )
	{
		if ( ( getFaceCount() > 0 ) && ( ( outlineColor != null ) || ( fillColor != null ) ) )
		{
			/*
			 * If the array is to small, create a larger one.
			 */
			float[] ver;
			synchronized ( _paintVertexCache )
			{
				ver = _paintVertexCache;
				if ( ver.length < _vertices.length )
					ver = new float[ _vertices.length ];
			}

			synchronized ( ver )
			{
				viewTransform.transform( _vertices , ver , _vertices.length / 3 );

				int maxLen = 0;
				for ( int f = 0 ; f < getFaceCount() ; f++ )
					maxLen = Math.max( maxLen , getFace( f ).getVertexCount() );

				final int[] xs = new int[ maxLen ];
				final int[] ys = new int[ maxLen ];

				for ( int f = 0 ; f < getFaceCount() ; f++ )
				{
					final Face  face = getFace( f );
					final int   len  = face.getVertexCount();

					if ( len > 0 )
					{
						final int[] pts  = getFaceVertexIndices( f );

						boolean show = true;
						for ( int p = 0 ; p < len ; p++ )
						{
							final int vi = pts[ p ] * 3;

							final float x  = ver[ vi ];
							final float y  = ver[ vi + 1 ];
							final float z  = ver[ vi + 2 ];

							final int ix = (int)gXform.transformX( x , y , z );
							final int iy = (int)gXform.transformY( x , y , z );

							if ( p == 2 )
							{
								/*
								 * Perform backface removal if we have 3 points, so we can calculate the normal.
								 *
								 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
								 */
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
									final float[] faceNormals = getFaceNormals();
									final int     normalIndex = f * 3;
									final float   nx          = faceNormals[ normalIndex     ];
									final float   ny          = faceNormals[ normalIndex + 1 ];
									final float   nz          = faceNormals[ normalIndex + 2 ];

									g.setColor( getAdjustedFillColor( viewTransform , fillColor , shadeFactor , nx , ny , nz ) );
								}
								else
								{
									g.setColor( fillColor );
								}

								if ( len < 3 ) /* point or line */
								{
									if ( outlineColor == null )
										g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ len - 1 ] , ys[ len - 1 ] );
								}
								else
								{
									g.fillPolygon( xs , ys , len );
								}
							}

							if ( outlineColor != null ) /* point or line */
							{
								g.setColor( outlineColor );
								if ( len < 3 )
									g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ len - 1 ] , ys[ len - 1 ] );
								else
									g.drawPolygon( xs , ys , len );
							}
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
	protected final Color getAdjustedFillColor( final Matrix3D viewTransform , final Color fillColor , final float shadeFactor , final float nx , final float ny , final float nz )
	{
		final Color result;

		if ( ( fillColor == null ) || ( viewTransform == null ) || ( shadeFactor == 0 ) )
		{
			result = fillColor;
		}
		else
		{
			final float rnz = nx * viewTransform.zx + ny * viewTransform.zy + nz * viewTransform.zz;

			final float factor = Math.min( 1.0f , ( 1 - shadeFactor ) + shadeFactor * Math.abs( rnz ) );
			if ( factor < 1 )
			{
				result = new Color( (int)( factor * fillColor.getRed()   + 0.5f ) ,
				                    (int)( factor * fillColor.getGreen() + 0.5f ) ,
				                    (int)( factor * fillColor.getBlue()  + 0.5f ) );
			}
			else
			{
				result = fillColor;
			}
		}

		return result;
	}

	/**
	 * Set properties of this object.
	 *
	 * @param   vertices    Vertices of model (stored as x/y/z triplets).
	 * @param   faceVert    Faces of model using indices in the vertex table.
	 * @param   faceMat     Material of each face.
	 * @param   faceTU      Face texture U coordinates for each face.
	 * @param   faceTV      Face texture V coordinates for each face.
	 * @param   faceOpacity Face opacity (0=transparent, 1=opaque).
	 * @param   faceSmooth  Face smoothing flag for each face.
	 */
	public final void set( final float[] vertices , final int[][] faceVert , final TextureSpec[] faceMat , final int[][] faceTU , final int[][] faceTV , final float[] faceOpacity , final boolean[] faceSmooth )
	{
		_vertices  = vertices;

		final boolean hasTexture = ( faceMat != null && faceMat.length >= faceVert.length );
		for ( int i = 0 ; i < faceVert.length ; i++ )
		{
			final int[]       f_vertices = faceVert[ i ];
			final TextureSpec f_material = hasTexture ? faceMat[ i ] : null;
			final int[]       f_textureU = ( hasTexture && faceTU != null ) ? faceTU [ i ] : null; // UGLY!
			final int[]       f_textureV = ( hasTexture && faceTV != null ) ? faceTV [ i ] : null; // UGLY!
			final float       f_opacity  = faceOpacity != null ? faceOpacity[ i ] : 1.0f;
			final boolean     f_smooth   = faceSmooth [ i ];

			final int[] points    = new int[ f_vertices.length ];
			final int[] textureUs = new int[ f_vertices.length ];
			final int[] textureVs = new int[ f_vertices.length ];

			for ( int j = 0 ; j < f_vertices.length ; j++ )
			{
				final int pointIndex = f_vertices[ j ];
				final int textureU   = ( hasTexture && f_textureU != null ) ? f_textureU[ j ] : -1;
				final int textureV   = ( hasTexture && f_textureV != null ) ? f_textureV[ j ] : -1;

				points   [ j ] = pointIndex;
				textureUs[ j ] = textureU;
				textureVs[ j ] = textureV;
			}

			final Face face = new Face( points , f_material , textureUs , textureVs , f_opacity , f_smooth );
			addFace( face );
		}

		_normalsDirty = true;
	}

	/**
	 * Set properties of this object.
	 *
	 * @param   vertices    Vertices of model (stored as x/y/z triplets).
	 * @param   faceVert    Faces of model using indices in the vertex table.
	 * @param   texture     Material for all faces in the model.
	 * @param   smooth      Smoothing flag for all faces.
	 */
	public final void set( final float[] vertices , final int[][] faceVert , final TextureSpec texture , final boolean smooth )
	{
		final TextureSpec[] faceMat = new TextureSpec[ faceVert.length ];
		for ( int i = 0 ; i < faceMat.length ; i++ )
			faceMat[ i ] = texture;

		final boolean[] faceSmooth = new boolean[ faceVert.length ];
		for ( int i = 0 ; i < faceSmooth.length ; i++ )
			faceSmooth[ i ] = smooth;

		set( vertices , faceVert ,  faceMat , null , null , null , faceSmooth );
	}

	/**
	 * Add a face to this <code>Object3D</code>.
	 *
	 * @param   face    Face to add.
	 */
	public final void addFace( final Face face )
	{
		_faces.add( face );
	}

	/**
	 * Add simple face to this object.
	 *
	 * @param   points      Points that define the face.
	 * @param   texture     Texture to apply to the face.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public final void addFace( final Vector3D[] points , final TextureSpec texture , final float opacity , final boolean smooth )
	{
		final Face face = new Face( null , texture , null , null , opacity , smooth );
		for ( int i = 0 ; i < points.length ; i++ )
			face.addVertex( points[ i ] );

		addFace( face );
	}

	/**
	 * Add face based on a 2D shape to this object.
	 *
	 * @param   base        Location/orientation of face.
	 * @param   shape       Shape of face relative to the base.
	 * @param   reversePath If set, the returned path will be reversed.
	 * @param   texture     Texture to apply to the face.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   swapUV      Swap texture coordinates to rotate 90 degrees.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public final void addFace( final Matrix3D base , final Polyline2D shape , final boolean reversePath , final TextureSpec texture , final float opacity , final boolean swapUV , final boolean smooth )
	{
		final int nrVertices = shape.getPointCount() + ( shape.isClosed() ? -1 : 0 );

		if ( nrVertices < 3 )
		{
			/* no edge/point support (yet?) */
		}
		else if ( ( texture != null ) && texture.isTexture() )
		{
			final float txBase = -base.xo * base.xx - base.yo * base.yx - base.zo * base.zx;
			final float tyBase = -base.xo * base.xy - base.yo * base.yy - base.zo * base.zy;

			final int[] vertU = new int[ nrVertices ];
			final int[] vertV = new int[ nrVertices ];

			int minU = 0;
			int maxU = 0;
			int minV = 0;
			int maxV = 0;

			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );

				final int u = (int)( ( swapUV ? ( tyBase + point.y ) : ( txBase + point.x ) ) * texture.textureScale );
				final int v = (int)( ( swapUV ? ( txBase + point.x ) : ( tyBase + point.y ) ) * texture.textureScale );

				if ( i == 0 || u < minU ) minU = u;
				if ( i == 0 || u > maxU ) maxU = u;
				if ( i == 0 || v < minV ) minV = v;
				if ( i == 0 || v > maxV ) maxV = v;

				vertU[ i ] = u;
				vertV[ i ] = v;
			}

			final int adjustU = getRangeAdjustment( minU , texture.getTextureWidth( null ) );
			final int adjustV = getRangeAdjustment( minV , texture.getTextureHeight( null ) );

			final Face face = new Face( null , texture , null , null , opacity , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , vertU[ i ] + adjustU , vertV[ i ] + adjustV );
			}
			addFace( face );
		}
		else
		{
			final Face face = new Face( null , texture , null , null , opacity , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , 0 , 0 );
			}
			addFace( face );
		}
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
	public final Face getFace( final int index )
	{
		return (Face)_faces.get( index );
	}

	/**
	 * Get transformed face normals.
	 *
	 * @return  Transformed face normals.
	 */
	public final float[] getFaceNormals()
	{
		if ( _normalsDirty )
			calculateNormals();

		return _faceNormals;
	}

	/**
	 * Get texture to use for rendering the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Texture to used to render the face.
	 */
	public final TextureSpec getFaceTexture( final int face )
	{
		return getFace( face ).getTexture();
	}

	/**
	 * Get texture U coordinates for the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array with texture U coordinates of face.
	 */
	public final int[] getFaceTextureU( final int face )
	{
		return getFace( face ).getTextureU();
	}

	/**
	 * Get texture V coordinates for the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array with texture V coordinates of face.
	 */
	public final int[] getFaceTextureV( final int face )
	{
		return getFace( face ).getTextureV();
	}

	/**
	 * Get number of vertices used to define the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Number of vertices used to define the face.
	 */
	public final int getFaceVertexCount( final int face )
	{
		return getFace( face ).getVertexCount();
	}

	/**
	 * Get vertex indices to define the shape of the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array of vertex indices to define the face shape.
	 */
	public final int[] getFaceVertexIndices( final int face )
	{
		return getFace( face ).getPointIndices();
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

	/**
	 * This class defines a 3D face of a 3D object.
	 */
	public final class Face
	{
		/**
		 * Point indices of this face. These indices indicate the index of the
		 * point in the <code>_vertices</code> array of the <code>Object3D</code>.
		 * Because points in the <code>_vertices</code> array are stored with a
		 * triplet for each vertex (x,y,z), these indices should be multiplied
		 * by 3 to get the 'real' index.
		 */
		private int[] _pointIndices;

		/**
		 * Number of points of this face.
		 */
		private int _pointCount;

		/**
		 * Normal of this face.
		 */
		public final float[] _normal;

		/**
		* Smoothing flag this face. Smooth faces are used to approximate
		 * smooth/curved/rounded parts of objects.
		 * <p />
		 * This information would typically be used to select the most appropriate
		 * shading algorithm.
		*/
		private boolean _smooth;

		/**
		 * Face texture U coordinates.
		 */
		int[] _textureU;

		/**
		 * Face texture V coordinates.
		 */
		int[] _textureV;

		/**
		 * Texture of this face.
		 */
		TextureSpec _texture;

		/**
		 * Opacity value for this face.
		 */
		private float _opacity;

		/**
		 * Construct new Face.
		 *
		 * @param   points  List of points.
		 * @param   texture Texture to apply to the face.
		 * @param   tU      Array for horizontal texture coordinates.
		 * @param   tV      Array for vertical texture coordinates.
		 * @param   opacity Opacity of face (0=transparent, 1=opaque).
		 * @param   smooth  Face is smooth/curved vs. flat.
		 */
		public Face( final int[] points , final TextureSpec texture , final int[] tU , final int[] tV , final float opacity , final boolean smooth )
		{
			_pointIndices = points;
			_pointCount   = points != null ? points.length : 0;
			_normal       = new float[ 3 ];
			_texture      = texture;
			_textureU     = tU;
			_textureV     = tV;
			_opacity      = opacity;
			_smooth       = smooth;
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex. The vertex texture U and V coordinates are set to -1.
		 *
		 * @param   x   X-coordinate of vertex to add.
		 * @param   y   Y-coordinate of vertex to add.
		 * @param   z   Z-coordinate of vertex to add.
		 */
		public void addVertex( final float x , final float y , final float z )
		{
			addVertex( x , y , z , -1 , -1 );
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex. The vertex texture U and V coordinates are set to -1.
		 *
		 * @param   point   Point that specifies the vertex x-, y- and z-coordinates.
		 */
		public void addVertex( final Vector3D point )
		{
			addVertex( point.x , point.y , point.z , -1 , -1 );
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex.
		 *
		 * @param   x   X-coordinate of vertex to add.
		 * @param   y   Y-coordinate of vertex to add.
		 * @param   z   Z-coordinate of vertex to add.
		 * @param   tU  Horizontal texture coordinate.
		 * @param   tV  Vertical texture coordinate.
		 */
		public void addVertex( final float x , final float y , final float z , final int tU , final int tV )
		{
			ensureCapacity( _pointCount + 1 );
			_pointIndices[ _pointCount ] = getOrAddPointIndex( x , y ,z );
			_textureU    [ _pointCount ] = tU;
			_textureV    [ _pointCount ] = tV;
			_pointCount++;
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex.
		 *
		 * @param   point   Point that specifies the vertex x-, y- and z-coordinates.
		 * @param   tU      Horizontal texture coordinate.
		 * @param   tV      Vertical texture coordinate.
		 */
		public void addVertex( final Vector3D point , final int tU , final int tV )
		{
			addVertex( point.x , point.y , point.z , tU , tV );
		}

		/**
		 * Get number of vertices that define this face.
		 *
		 * @return  Number of vertices.
		 */
		public int getVertexCount()
		{
			return _pointCount;
		}

		/**
		 * Get X coordinate of this face's vertex with the specified index.
		 *
		 * @param   index   Vertex index.
		 *
		 * @return  X coordinate for the specified vertex.
		 */
		public float getX( final int index )
		{
			return _vertices[ _pointIndices[ index ] * 3 ];
		}

		/**
		 * Get Y coordinate of this face's vertex with the specified index.
		 *
		 * @param   index   Vertex index.
		 *
		 * @return  Y coordinate for the specified vertex.
		 */
		public float getY( final int index )
		{
			return _vertices[ _pointIndices[ index ] * 3 + 1 ];
		}

		/**
		 * Get Z coordinate of this face's vertex with the specified index.
		 *
		 * @param   index   Vertex index.
		 *
		 * @return  Z coordinate for the specified vertex.
		 */
		public float getZ( final int index )
		{
			return _vertices[ _pointIndices[ index ] * 3 + 2 ];
		}

		/**
		 * Get horizontal texture coordinate (U) of this face's vertex with the
		 * specified index.
		 *
		 * @param   index   Vertex index.
		 *
		 * @return  Horizontal texture coordinate (U) for the specified vertex.
		 */
		public int getTextureU( final int index )
		{
			return ( _textureU != null ) ? _textureU[ index ] : 0;
		}

		/**
		 * Get horizontal texture coordinate (V) of this face's vertex with the
		 * specified index.
		 *
		 * @param   index   Vertex index.
		 *
		 * @return  Horizontal texture coordinate (U) for the specified vertex.
		 */
		public int getTextureV( final int index )
		{
			return ( _textureV != null ) ? _textureV[ index ] : 0;
		}

		/**
		 * Get the point indices of this face. These indices indicate the index of
		 * the point in the <code>_vertices</code> array of the <code>Object3D</code>.
		 * Because points in the <code>_vertices</code> array are stored with a
		 * triplet for each vertex (x,y,z), these indices should be multiplied
		 * by 3 to get the 'real' index.
		 *
		 * @return  The point indices of this face.
		 */
		public int[] getPointIndices()
		{
			return _pointIndices;
		}

		/**
		 * Get smoothing flag of this face. Smooth faces are used to approximate
		 * smooth/curved/rounded parts of objects.
		 * <p />
		 * This information would typically be used to select the most appropriate
		 * shading algorithm.
		 *
		 * @return  <code>true</code> if face is smooth (curved/rounded);
		 *          <code>false</code> if face is not smooth (flat).
		 */
		public boolean isSmooth()
		{
			return _smooth;
		}

		/**
		 * Set smoothing flag this face. Smooth faces are used to approximate
		 * smooth/curved/rounded parts of objects.
		 * <p />
		 * This information would typically be used to select the most appropriate
		 * shading algorithm.
		 *
		 * @param   smooth  <code>true</code> if face is smooth (curved/rounded);
		 *                  <code>false</code> if face is not smooth (flat).
		 */
		public void setSmooth( final boolean smooth )
		{
			_smooth = smooth;
		}

		/**
		 * Get the horizontal texture coordinates of this face.
		 *
		 * @return  The horizontal texture coordinates of this face.
		 */
		public int[] getTextureU()
		{
			return _textureU;
		}

		/**
		 * Get the vertical texture coordinates of this face.
		 *
		 * @return  The vertical texture coordinates of this face.
		 */
		public int[] getTextureV()
		{
			return _textureV;
		}

		/**
		 * Get texture of this face.
		 *
		 * @return  Texture of this face..
		 */
		public TextureSpec getTexture()
		{
			return _texture;
		}

		/**
		 * Set texture of this face.
		 *
		 * @param   texture     Texture to assign to this face.
		 */
		public void setTexture( final TextureSpec texture )
		{
			_texture = texture;
		}

		/**
		 * Get opacity. This ranges from fully opaque (1.0) to completely
		 * translucent (0.0).
		 *
		 * @return  Opacity (0.0 - 1.0).
		 */
		public float getOpacity()
		{
			return _opacity;
		}

		/**
		 * Get opacity. This ranges from fully opaque (1.0) to completely
		 * translucent (0.0).
		 *
		 * @param   opacity     Opacity (0.0 - 1.0).
		 */
		public void setOpacity( final float opacity )
		{
			_opacity = opacity;
		}

		/**
		 * Make sure that the internal vertex buffers meet the requested minimum
		 * capacity requirement.
		 *
		 * @param   capacity    Minimum capacity requirement.
		 */
		private void ensureCapacity( final int capacity )
		{
			if ( _pointIndices == null || _pointIndices.length < capacity )
			{
				int[] temp;

				if ( _pointIndices != null )
				{
					temp = new int[ capacity ];

					System.arraycopy( _pointIndices , 0 , temp , 0 , _pointCount );
					_pointIndices = temp;
				}
				else
				{
					_pointIndices = new int[ capacity ];
				}

				if ( _textureU != null )
				{
					temp  = new int[ capacity ];

					System.arraycopy( _textureU , 0 , temp , 0 , _pointCount );
					_textureU = temp;
				}
				else
				{
					_textureU = new int[ capacity ];
				}

				if ( _textureV != null )
				{
					temp  = new int[ capacity ];

					System.arraycopy( _textureV , 0 , temp , 0 , _pointCount );
					_textureV = temp;
				}
				else
				{
					_textureV = new int[ capacity ];
				}
			}
		}
	}
}
