/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2001-2004 Numdata BV.  All rights reserved.
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
package ab.light3d.renderer;

import java.util.ArrayList;
import java.util.List;

import ab.light3d.Matrix3D;
import ab.light3d.PolyPoint2D;
import ab.light3d.Polyline2D;
import ab.light3d.TextureSpec;
import ab.light3d.Vector3D;

/**
 * This class implements a builder for an <code>Object3D</code>. It allows
 * faces and vertices to be created incrementally.
 * incremental
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class Object3DBuilder
{
	/**
	 * List of faces in the 3D object being build.
	 *
	 * @see Face
	 */
	private final List _faces = new ArrayList();

	/**
	 * This class represents a vertex of a face. A vertex contains the coordinates
	 * of the vertex in the object space and texture coordinates (if applicable).
	 * <p />
	 * Instances of this class should be created using the
	 * <code>Face.addVertex()</code> method.
	 *
	 * @see Face#addVertex
	 */
	public static final class Vertex
	{
		/**
		 * Location of vertex in object space.
		 */
		private final Vector3D _point;

		/**
		 * Horizontal texture coordinate.
		 */
		private final int _tu;

		/**
		 * Vertical texture coordinate.
		 */
		private final int _tv;

		/**
		 * Construct face vertex.
		 *
		 * @param   point   Location of vertex in object space.
		 * @param   tu      Horizontal texture coordinate.
		 * @param   tv      Vertical texture coordinate.
		 */
		public Vertex( final Vector3D point , final int tu , final int tv )
		{
			_point = point;
			_tu = tu;
			_tv = tv;
		}

		/**
		 * Helper for builder to generate data for this vertex.
		 *
		 * @param   points      List of points.
		 * @param   vertexIndex Index in result arrays.
		 * @param   vert        Result array for vertex indices.
		 * @param   tu          Result array for horizontal texture coordinates.
		 * @param   tv          Result array for vertical texture coordinates.
		 */
		public void build( final List points , final int vertexIndex , final int[] vert , final int[] tu , final int[] tv )
		{
			int index = points.indexOf( _point );
			if ( index < 0 )
			{
				index = points.size();
				points.add( _point );
			}

			vert[ vertexIndex ] = index;

			if ( tu != null )
				tu[ vertexIndex ] = _tu;

			if ( tv != null )
				tv[ vertexIndex ] = _tv;
		}

	}

	/**
	 * This class represents a face of an 3D object. A face contains rendering
	 * paramters for the face and a list of vertices that define the shape
	 * of the face.
	 * <p />
	 * Instances of this class should be created using the
	 * <code>Object3DBuilder.addFace()</code> methods.
	 *
	 * @see Object3DBuilder#addFace
	 */
	public static final class Face
	{
		/**
		 * Texture to apply to this face.
		 */
		private final TextureSpec _texture;

		/**
		 * Face is smooth/curved vs. flat.
		 */
		private final boolean _smooth;

		/**
		 * List of vertices in this face.
		 */
		private final List _vertices = new ArrayList();

		/**
		 * Construct 3D object face.
		 *
		 * @param   texture     Texture to apply to the face.
		 * @param   smooth      Face is smooth/curved vs. flat.
		 */
		public Face( final TextureSpec texture , final boolean smooth )
		{
			if ( texture == null )
				throw new RuntimeException( "texture == null" );

			_texture = texture;
			_smooth = smooth;
		}

		/**
		 * Determine wether the face texture has a texture image (for which
		 * texture coordinates should be provided).
		 *
		 * @return  <code>true</code> if face has a texture image;
		 *			<code>false</code> otherwise.
		 */
		public boolean hasTextureImage()
		{
			return _texture.isTexture();
		}

		/**
		 * Determine wether the face texture has a texture image (for which
		 * texture coordinates should be provided).
		 *
		 * @return  <code>true</code> if face is smooth/curved;
		 *			<code>false</code> if face is flat.
		 */
		public boolean isSmooth()
		{
			return _smooth;
		}

		/**
		 * Add vertex to this face.
		 *
		 * @param   point   Location of vertex in object space.
		 * @param   tu      Horizontal texture coordinate.
		 * @param   tv      Vertical texture coordinate.
		 */
		public void addVertex( final Vector3D point , final int tu , final int tv )
		{
			_vertices.add( new Vertex( point , tu , tv ) );
		}

		/**
		 * Helper for builder to generate data for this vertex.
		 *
		 * @param   points      List of points.
		 * @param   faceIndex   Index in result arrays.
		 * @param   vert        Result array for face vertex coordinates.
		 * @param   mat         Result array for face materials.
		 * @param   tu          Result array for horizontal texture coordinates.
		 * @param   tv          Result array for vertical texture coordinates.
		 * @param   smooth      Result array for face smoothing.
		 */
		public void build( final List points , final int faceIndex , final int[][] vert , final TextureSpec[] mat , final int[][] tu , final int[][] tv , final boolean[] smooth )
		{
			final int     nrVertices = _vertices.size();
			final boolean hasTexture = hasTextureImage();

			vert[ faceIndex ] = new int[ nrVertices ];
			mat[ faceIndex ] = _texture;
			if ( hasTexture )
			{
				tu[ faceIndex ] = new int[ nrVertices ];
				tv[ faceIndex ] = new int[ nrVertices ];
			}
			smooth[ faceIndex ] = _smooth;

			for ( int vertexIndex = 0 ; vertexIndex < nrVertices ; vertexIndex++ )
			{
				final Vertex vertex = (Vertex)_vertices.get( vertexIndex );
				vertex.build( points , vertexIndex , vert[ faceIndex ] , tu[ faceIndex ] , tv[ faceIndex ] );

			}
		}
	}

	/**
	 * Add empty face to the 3D object. Vertices should be added to this shape
	 * by the caller.
	 *
	 * @param   texture     Texture to apply to the face.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 *
	 * @return  Face that was added to this object.
	 */
	public Face addFace( final TextureSpec texture , final boolean smooth )
	{
		final Face face = new Face( texture , smooth );
		_faces.add( face );
		return face;
	}

	/**
	 * Add empty face to the 3D object. Vertices should be added to this shape
	 * by the caller.
	 *
	 * @param   base        Location/orientation of face.
	 * @param   shape       Shape of face relative to the base.
	 * @param   texture     Texture to apply to the face.
	 * @param   swapUV      Swap texture coordinates to rotate 90 degrees.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public void addFace( final Matrix3D base , final Polyline2D shape , final TextureSpec texture , final boolean swapUV , final boolean smooth )
	{
		final int nrVertices = shape.getPointCount() + ( shape.isClosed() ? -1 : 0 );
		if ( nrVertices < 3 )
			return;

		if ( texture.isTexture() )
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
				final PolyPoint2D point = shape.getPoint( i );

				final int u = (int)( ( swapUV ? ( tyBase + point.y ) : ( txBase + point.x ) ) * texture.textureScale );
				final int v = (int)( ( swapUV ? ( txBase + point.x ) : ( tyBase + point.y ) ) * texture.textureScale );

				if ( i == 0 || u < minU ) minU = u;
				if ( i == 0 || u > maxU ) maxU = u;
				if ( i == 0 || v < minV ) minV = v;
				if ( i == 0 || v > maxV ) maxV = v;

				vertU[ i ] = u;
				vertV[ i ] = v;
			}

			final int adjustU = getRangeAdjustment( minU , texture.getTextureWidth() );
			final int adjustV = getRangeAdjustment( minV , texture.getTextureHeight() );

			final Face face = addFace( texture , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , vertU[ i ] + adjustU , vertV[ i ] + adjustV );
			}
		}
		else
		{
			final Face face = addFace( texture , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( i );
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
	 * @param   value       Value to adjust.
	 * @param   range       Specifies the range 0 to (range - 1).
	 *
	 * @return  Adjustment value to add to the value.
	 */
	public static int getRangeAdjustment( final int value , final int range )
	{
		if ( value < 0 )
		{
			return ( -value + range - 1 ) / range * range;
		}
		else
		{
			return -value + ( value % range );
		}
	}

	/**
	 * Build 3D object from the data that was fed to the builder.
	 *
	 * @param   obj3d   Target object to build.
	 */
	public void build( final Object3D obj3d )
	{
		final int nrFaces = _faces.size();

		boolean hasTexture = false;
		for ( int i = 0 ; !hasTexture && i < nrFaces ; i++ )
			hasTexture = ((Face)_faces.get( i )).hasTextureImage();

		/*
		 * Build face data and point list to generate vertex data.
		 */
		final List          points     = new ArrayList();
		final int[][]       faceVert   = new int[ nrFaces ][];
		final TextureSpec[] faceMat    = new TextureSpec[ nrFaces ];
		final int[][]       faceTU     = hasTexture ? new int[ nrFaces ][] : null;
		final int[][]       faceTV     = hasTexture ? new int[ nrFaces ][] : null;
		final boolean[]     faceSmooth = new boolean[ nrFaces ];

		for ( int i = 0 ; i < nrFaces ; i++ )
		{
			final Face face = (Face)_faces.get( i );
			face.build( points , i , faceVert , faceMat , faceTU , faceTV , faceSmooth );
		}

		/*
		 * Generate vertex data from point list.
		 */
		final int     nrVertices = points.size();
		final float[] vertices   = new float[ nrVertices * 3 ];

		for ( int i = 0 , j = 0 ; i < nrVertices ; i++ )
		{
			final Vector3D point = (Vector3D)points.get( i );

			vertices[ j++ ] = point.x;
			vertices[ j++ ] = point.y;
			vertices[ j++ ] = point.z;
		}

		/*
		 * Hurray, we can set the resulting data now.
		 */
		obj3d.set( vertices , faceVert , faceMat , faceTU , faceTV , faceSmooth );
	}

	/**
	 * Build 3D object from the data that was fed to the builder.
	 *
	 * @return  Object that was build.
	 */
	public Object3D build()
	{
		final Object3D obj3d = new Object3D();
		build( obj3d );
		return obj3d;
	}
}
