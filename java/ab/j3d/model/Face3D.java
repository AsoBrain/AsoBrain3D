/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2004-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */package ab.j3d.model;

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
	private double _opacity;

	/**
	* Smoothing flag this face. Smooth faces are used to approximate
	 * smooth/curved/rounded parts of objects.
	 * <p />
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	*/
	private boolean _smooth;

	/**
	 * Construct new Face.
	 *
	 * @param   object      Object to which this face belongs.
	 * @param   points      List of points.
	 * @param   texture     Texture to apply to the face.
	 * @param   textureU    Array for horizontal texture coordinates.
	 * @param   textureV    Array for vertical texture coordinates.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	Face3D( final Object3D object , final int[] points , final TextureSpec texture , final int[] textureU , final int[] textureV , final double opacity , final boolean smooth )
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
	public double getOpacity()
	{
		return _opacity;
	}

	/**
	 * Get opacity. This ranges from fully opaque (1.0) to completely
	 * translucent (0.0).
	 *
	 * @param   opacity     Opacity (0.0 - 1.0).
	 */
	public void setOpacity( final double opacity )
	{
		_opacity = opacity;
	}

	/**
	 * Make sure that the internal vertex buffers meet the requested minimum
	 * capacity requirement.
	 *
	 * @param   vertexCount     Minimum vertex storage capacity.
	 */
	void ensureCapacity( final int vertexCount )
	{
		_pointIndices = (int[])ArrayTools.ensureLength( _pointIndices , int.class , -1 , vertexCount );
		_textureU     = (int[])ArrayTools.ensureLength( _textureU     , int.class , -1 , vertexCount );
		_textureV     = (int[])ArrayTools.ensureLength( _textureV     , int.class , -1 , vertexCount );
	}
}
