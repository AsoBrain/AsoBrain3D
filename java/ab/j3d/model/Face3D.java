/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */package ab.j3d.model;

import java.awt.Color;
import java.awt.Graphics2D;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

import com.numdata.oss.ArrayTools;

/**
 * This class defines a 3D face of a 3D object.
 */
public final class Face3D
{
	/**
	 * Object to which this face belongs.
	 */
	private final Object3D _object;

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
	public final double[] _normal;

	/**
	 * Texture of this face.
	 */
	TextureSpec _texture;

	/**
	 * Face texture U coordinates.
	 */
	int[] _textureU;

	/**
	 * Face texture V coordinates.
	 */
	int[] _textureV;

	/**
	 * Opacity value for this face.
	 */
	private float _opacity;

	/**
	* Smoothing flag this face. Smooth faces are used to approximate
	 * smooth/curved/rounded parts of objects.
	 * <p />
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	*/
	private boolean _smooth;

	/**
	 * Flag to indicate if this face has an backface.
	 */
	private boolean _hasBackface;

	/**
	 * Construct new Face.
	 *
	 * @param   object          Object to which this face belongs.
	 * @param   points          List of points.
	 * @param   texture         Texture to apply to the face.
	 * @param   textureU        Array for horizontal texture coordinates.
	 * @param   textureV        Array for vertical texture coordinates.
	 * @param   opacity         Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   hasBackface     Face has an backface (render both sides).
	 */
	Face3D( final Object3D object , final int[] points , final TextureSpec texture , final int[] textureU , final int[] textureV , final float opacity , final boolean smooth , final boolean hasBackface )
	{
		_object       = object;
		_pointIndices = points;
		_pointCount   = ( points != null ) ? points.length : 0;
		_normal       = new double[ 3 ];
		_texture      = texture;
		_textureU     = textureU;
		_textureV     = textureV;
		_smooth       = smooth;
		_opacity      = opacity;
		_hasBackface  = hasBackface;
	}

	/**
	 * Make sure that the internal vertex buffers meet the requested minimum
	 * capacity requirement. This can be used prior to adding vertices to a face
	 * to optimize buffer allocations.
	 *
	 * @param   vertexCount     Minimum total vertex storage capacity.
	 */
	public void ensureCapacity( final int vertexCount )
	{
		_pointIndices = (int[])ArrayTools.ensureLength( _pointIndices , int.class , -1 , vertexCount );
		_textureU     = (int[])ArrayTools.ensureLength( _textureU     , int.class , -1 , vertexCount );
		_textureV     = (int[])ArrayTools.ensureLength( _textureV     , int.class , -1 , vertexCount );
	}

	/**
	 * Add vertex to face. Note that the last vertex is automatically connected
	 * to the first vertex. The vertex texture U and V coordinates are set to -1.
	 *
	 * @param   x   X-coordinate of vertex to add.
	 * @param   y   Y-coordinate of vertex to add.
	 * @param   z   Z-coordinate of vertex to add.
	 */
	public void addVertex( final double x , final double y , final double z )
	{
		addVertex( x , y , z , -1 , -1 );
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
	public void addVertex( final double x , final double y , final double z , final int tU , final int tV )
	{
		ensureCapacity( _pointCount + 1 );
		_pointIndices[ _pointCount ] = _object.getOrAddPointIndex( x , y ,z );
		_textureU    [ _pointCount ] = tU;
		_textureV    [ _pointCount ] = tV;
		_pointCount++;
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
	 * @param   point   Point that specifies the vertex x-, y- and z-coordinates.
	 * @param   tU      Horizontal texture coordinate.
	 * @param   tV      Vertical texture coordinate.
	 */
	public void addVertex( final Vector3D point , final int tU , final int tV )
	{
		addVertex( point.x , point.y , point.z , tU , tV );
	}

	/**
	 * Paint 2D representation of this 3D face.
	 *
	 * @param   g               Graphics2D context.
	 * @param   gTransform      Projection transform for Graphics2D context (3D->2D, pan, sale).
	 * @param   outlineColor    Color to use for face outlines (<code>null</code> to disable drawing).
	 * @param   fillColor       Color to use for filling faces (<code>null</code> to disable drawing).
	 * @param   pointCoords     Coordinates of points (after view transform is applied).
	 * @param   xs              Temporary storage for 2D coordinates.
	 * @param   ys              Temporary storage for 2D coordinates.
	 *
	 * @see     Object3D#paint
	 */
	public void paint( final Graphics2D g , final Matrix3D gTransform , final Color outlineColor , final Color fillColor , final double[] pointCoords , final int[] xs , final int[] ys )
	{
		final int    vertexCount  = getVertexCount();
		final int[]  pointIndices = getPointIndices();

		if ( ( vertexCount > 0 ) && ( ( outlineColor != null ) || ( fillColor != null ) ) )
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
				if ( ( p == 2 ) && !isHasBackface() )
				{
					show = ( ( ( xs[ 0 ] - xs[ 1 ] ) * ( iy - ys[ 1 ] ) )
					      <= ( ( ys[ 0 ] - ys[ 1 ] ) * ( ix - xs[ 1 ] ) ) );

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
					g.setColor( fillColor );

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

				if ( outlineColor != null )
				{
					g.setColor( outlineColor );
					if ( vertexCount < 3 ) /* point or line */
						g.drawLine( xs[ 0 ] , ys[ 0 ] , xs[ vertexCount - 1 ] , ys[ vertexCount - 1 ] );
					else
						g.drawPolygon( xs , ys , vertexCount );
				}
			}
		}
	}

	/**
	 * Get backface flag of this face. If this flag is set, then both sides of
	 * this face should be rendered; otherwise, only the 'front' or 'visible'
	 * side of a face is rendered.
	 * <p />
	 * Basically, this flag be set for open objects, and should be cleared
	 * (default) if the faces of an object cover the complete interior(s) of
	 * an object.
	 *
	 * @return  <code>True</code> if this face has an backface;
	 *          <code>False</code> otherwise.
	 */
	public boolean isHasBackface()
	{
		return _hasBackface;
	}

	/**
	 * Set backface flag of this face. If this flag is set, then both sides of
	 * this face should be rendered; otherwise, only the 'front' or 'visible'
	 * side of a face is rendered.
	 * <p />
	 * Basically, this flag be set for open objects, and should be cleared
	 * (default) if the faces of an object cover the complete interior(s) of
	 * an object.
	 *
	 * @param hasBackface   <code>True</code> if this face has an backface;
	 *                      <code>False</code> otherwise.
	 */
	public void setHasBackface( final boolean hasBackface )
	{
		_hasBackface = hasBackface;
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
	 * Get the point indices of this face. These indices are an index in the
	 * point list of the <code>Object3D</code> to which this face belongs.
	 * <p />
	 * Because points coordinates are stored with a triplet for each point
	 * (x,y,z), these indices should be multiplied by 3 to get the 'real' index.
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
	public double getX( final int index )
	{
		return _object.getPointCoords()[ _pointIndices[ index ] * 3 ];
	}

	/**
	 * Get Y coordinate of this face's vertex with the specified index.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Y coordinate for the specified vertex.
	 */
	public double getY( final int index )
	{
		return _object.getPointCoords()[ _pointIndices[ index ] * 3 + 1 ];
	}

	/**
	 * Get Z coordinate of this face's vertex with the specified index.
	 *
	 * @param   index   Vertex index.
	 *
	 * @return  Z coordinate for the specified vertex.
	 */
	public double getZ( final int index )
	{
		return _object.getPointCoords()[ _pointIndices[ index ] * 3 + 2 ];
	}
}
