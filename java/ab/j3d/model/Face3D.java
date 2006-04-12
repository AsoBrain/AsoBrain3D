/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2006
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

import com.numdata.oss.ArrayTools;

import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.geom.Polygon3D;

/**
 * This class defines a 3D face of a 3D object.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Face3D
	implements Polygon3D
{
	/**
	 * X component of cross product of first and second edge of this face.
	 */
	double _crossX;

	/**
	 * Y component of cross product of first and second edge of this face.
	 */
	double _crossY;

	/**
	 * Z component of cross product of first and second edge of this face.
	 */
	double _crossZ;

	/**
	 * Distance component of plane relative to origin. This defines the
	 * <code>D</code> variable in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 *
	 * @see     #_normal
	 */
	private double _distance;

	/**
	 * Plane normal. This defines the <code>A</code>, <code>B</code>, and
	 * <code>C</code> variables  in the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>Using the individual normal components (X,Y,Z) may be more efficient,
	 *   since this does not require a {@link Vector3D} instance.</dd>
	 * </dl>
	 */
	private Vector3D _normal;

	/**
	 * X component of plane normal. This defines the <code>A</code> variable in
	 * the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 */
	double _normalX;

	/**
	 * Y component of plane normal. This defines the <code>B</code> variable in
	 * the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 */
	double _normalY;

	/**
	 * Z component of plane normal. This defines the <code>C</code> variable in
	 * the plane equation:
	 * <pre>
	 *   A * x + B * y + C * z = D
	 * </pre>
	 */
	double _normalZ;

	/**
	 * Object to which this face belongs.
	 */
	private final Object3D _object;

	/**
	 * Opacity value for this face.
	 */
	private float _opacity;

	/**
	 * Vertex indices of this face. These indices indicate the index of the
	 * vertex in the {@link Object3D#_vertexCoordinates} array.
	 * <p />
	 * Because coordinates in the {@link Object3D#_vertexCoordinates} array are
	 * stored with a triplet for each vertex, these indices should be multiplied
	 * by 3 to get the 'real' index.
	 */
	private int[] _vertexIndices;

	/**
	 * Number of vertices of this face.
	 */
	private int _vertexCount;

	/**
	* Smoothing flag this face. Smooth faces are used to approximate
	 * smooth/curved/rounded parts of objects.
	 * <p />
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	*/
	private boolean _smooth;

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
	 * Flag to indicate that the plane is two-sided. This means, that the
	 * plane is 'visible' on both sides.
	 */
	private boolean _twoSided;

	/**
	 * Construct new face.
	 *
	 * @param   object          Object to which this face belongs.
	 * @param   vertexIndices   Vertex indices of added face. These indices refer
	 *                          to vertices previously defined in <code>object</code>.
	 * @param   texture         Texture to apply to the face.
	 * @param   textureU        Array for horizontal texture coordinates.
	 * @param   textureV        Array for vertical texture coordinates.
	 * @param   opacity         Opacity of face (0=transparent, 1=opaque).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	Face3D( final Object3D object , final int[] vertexIndices , final TextureSpec texture , final int[] textureU , final int[] textureV , final float opacity , final boolean smooth , final boolean twoSided )
	{
		if ( object == null )
			throw new NullPointerException( "object" );

		_object        = object;
		_vertexIndices = vertexIndices;
		_vertexCount   = ( vertexIndices != null ) ? vertexIndices.length : 0;
		_texture       = texture;
		_textureU      = textureU;
		_textureV      = textureV;
		_smooth        = smooth;
		_opacity       = opacity;
		_twoSided      = twoSided;

		_crossX   = Double.NaN;
		_crossY   = Double.NaN;
		_crossZ   = Double.NaN;
		_normalX  = Double.NaN;
		_normalY  = Double.NaN;
		_normalZ  = Double.NaN;
		_distance = Double.NaN;
		_normal   = null;
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
	 * @param   x           X-coordinate of vertex to add.
	 * @param   y           Y-coordinate of vertex to add.
	 * @param   z           Z-coordinate of vertex to add.
	 * @param   textureU    Horizontal texture coordinate.
	 * @param   textureV    Vertical texture coordinate.
	 */
	public void addVertex( final double x , final double y , final double z , final int textureU , final int textureV )
	{
		final int vertexCount = _vertexCount;

		ensureCapacity( vertexCount + 1 );
		_vertexIndices[ vertexCount ] = _object.getVertexIndex( x , y ,z );
		_textureU     [ vertexCount ] = textureU;
		_textureV     [ vertexCount ] = textureV;

		_vertexCount = vertexCount + 1;

		invalidate();
	}

	/**
	 * Add vertex to face. Note that the last vertex is automatically connected
	 * to the first vertex. The vertex texture U and V coordinates are set to -1.
	 *
	 * @param   vertex  Vertex coordinates.
	 */
	public void addVertex( final Vector3D vertex )
	{
		addVertex( vertex.x , vertex.y , vertex.z , -1 , -1 );
	}

	/**
	 * Add vertex to face. Note that the last vertex is automatically connected
	 * to the first vertex.
	 *
	 * @param   coordinates     Vertex coordinates.
	 * @param   textureU        Horizontal texture coordinate.
	 * @param   textureV        Vertical texture coordinate.
	 */
	public void addVertex( final Vector3D coordinates , final int textureU , final int textureV )
	{
		addVertex( coordinates.x , coordinates.y , coordinates.z , textureU , textureV );
	}

	/**
	 * This internal method is used to calculate the cross product between the
	 * first two edges of this face. This vector is basically the plane's normal
	 * before normalization.
	 * <p />
	 * If the product can not be calculated (less than 3 vertices, coincident
	 * vertices, parallel edges), the cross product will be set to
	 * <code>( 0.0 , 0.0 , 0.0 )</code>.
	 * <p />
	 * The cross product is cached in the {@link #_crossX}, {@link #_crossY},
	 * and {@link #_crossZ} fields. The cache is invalidated by setting the
	 * {@link #_crossY} field to {@link Double#NaN}.
	 *
	 * @see     #calculateNormal()
	 * @see     #invalidate()
	 * @see     Object3D#calculateVertexNormals()
	 */
	void calculateCross()
	{
		if ( Double.isNaN( _crossY ) )
		{
			final double x;
			final double y;
			final double z;

			int i = _vertexCount;
			if ( i >= 3 )
			{
				final double[] vertexCoordinates = _object.getVertexCoordinates();
				final int[]    vertexIndices     = _vertexIndices;

				final double x0 = vertexCoordinates[ i = vertexIndices[ 1 ] * 3 ];
				final double y0 = vertexCoordinates[ i + 1 ];
				final double z0 = vertexCoordinates[ i + 2 ];

				final double u1 = vertexCoordinates[ i = vertexIndices[ 0 ] * 3 ] - x0;
				final double u2 = vertexCoordinates[ i + 1                     ] - y0;
				final double u3 = vertexCoordinates[ i + 2                     ] - z0;

				final double v1 = vertexCoordinates[ i = vertexIndices[ 2 ] * 3 ] - x0;
				final double v2 = vertexCoordinates[ i + 1                     ] - y0;
				final double v3 = vertexCoordinates[ i + 2                     ] - z0;

				x = u2 * v3 - u3 * v2;
				y = u3 * v1 - u1 * v3;
				z = u1 * v2 - u2 * v1;
			}
			else
			{
				x = 0.0;
				y = 0.0;
				z = 0.0;
			}

			_crossX = x;
			_crossY = y;
			_crossZ = z;
		}
	}

	/**
	 * This internal method is used to calculate the plane normal.
	 * <p />
	 * The normal is cached in the {@link #_normalX}, {@link #_normalY}, and
	 * {@link #_normalZ} fields. The cache is invalidated by setting the
	 * {@link #_normalY} field to {@link Double#NaN}.
	 * <p />
	 * If the normal can not be calculated (less than 3 vertices, coincident
	 * vertices, parallel edges), the normal will be set to
	 * <code>( {@link Double#NaN} , 0.0 , {@link Double#NaN} )</code>.
	 *
	 * @see     #calculateCross()
	 * @see     #invalidate()
	 */
	void calculateNormal()
	{
		if ( Double.isNaN( _normalY ) )
		{
			calculateCross();

			double x = _crossX;
			double y = _crossY;
			double z = _crossZ;

			if ( ( x != 0.0 ) || ( y != 0.0 ) || ( z != 0.0 ) )
			{
				final double l = Math.sqrt( x * x + y * y + z * z );

				x /= l;
				y /= l;
				z /= l;
			}
			else
			{
				x = Double.NaN;
				y = 0.0;
				z = Double.NaN;
			}

			_normalX = x;
			_normalY = y;
			_normalZ = z;
		}
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
		_vertexIndices = (int[])ArrayTools.ensureLength( _vertexIndices , int.class , -1 , vertexCount );
		_textureU      = (int[])ArrayTools.ensureLength( _textureU      , int.class , -1 , vertexCount );
		_textureV      = (int[])ArrayTools.ensureLength( _textureV      , int.class , -1 , vertexCount );
	}

	public double getDistance()
	{
		double result = _distance;
		if ( Double.isNaN( result ) )
		{
			calculateNormal();

			if ( !Double.isNaN( _normalX ) )
			{
				final int      vertexIndex       = _vertexIndices[ 0 ] * 3;
				final double[] vertexCoordinates = _object.getVertexCoordinates();

				final double x0 = vertexCoordinates[ vertexIndex     ];
				final double y0 = vertexCoordinates[ vertexIndex + 1 ];
				final double z0 = vertexCoordinates[ vertexIndex + 2 ];

				result = Vector3D.dot( _normalX , _normalY , _normalZ , x0 , y0 , z0 );
			}
			else
			{
				result = 0.0;
			}

			_distance = result;
		}

		return result;
	}

	public Vector3D getNormal()
	{
		Vector3D result = _normal;
		if ( result == null )
		{
			calculateNormal();
			result = Vector3D.INIT.set( _normalX , _normalY , _normalZ );
			_normal = result;
		}

		return result;
	}

	public double getNormalX()
	{
		calculateNormal();
		return _normalX;
	}

	public double getNormalY()
	{
		calculateNormal();
		return _normalY;
	}

	public double getNormalZ()
	{
		calculateNormal();
		return _normalZ;
	}

	/**
	 * Get object to which this face belongs.
	 *
	 * @return  Object to which this face belongs.
	 */
	public Object3D getObject()
	{
		return _object;
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
	 * Invalidate derived data, because the face has been modified.
	 */
	void invalidate()
	{
		_crossX   = Double.NaN;
		_crossY   = Double.NaN;
		_crossZ   = Double.NaN;
		_normalX  = Double.NaN;
		_normalY  = Double.NaN;
		_normalZ  = Double.NaN;
		_distance = Double.NaN;
		_normal   = null;

		_object._vertexNormalsDirty = true;
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

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	/**
	 * Set flag to indicate that the plane is two-sided. This means, if set,
	 * that both sides of the plane are 'visible'; if not set, the plane
	 * is only visible from the side in which the plane normal points.
	 *
	 * @param   twoSided    <code>true</code> if the plane is two-sided;
	 *                      <code>false</code> otherwise.
	 */
	public void setTwoSided( final boolean twoSided )
	{
		_twoSided = twoSided;
	}

	public int getVertexCount()
	{
		return _vertexCount;
	}

	/**
	 * Get the vertex indices of this face. These indices are an index in the
	 * vertex list of the {@link Object3D} to which this face belongs.
	 * <p />
	 * Because vertex coordinates are stored with a triplet for each vertex,
	 * these indices should be multiplied by 3 to get the 'real' index.
	 *
	 * @return  The vertex indices of this face.
	 */
	public int[] getVertexIndices()
	{
		return _vertexIndices;
	}

	/**
	 * Get X component of vertex normal with the specified index.
	 *
	 * @param   index   Index of vertex in face.
	 *
	 * @return  X component of vertex normal.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	public double getVertexNormalX( final int index )
	{
		_object.calculateVertexNormals();
		return _object._vertexNormals[ _vertexIndices[ index ] * 3 ];
	}

	/**
	 * Get Y component of vertex normal with the specified index.
	 *
	 * @param   index   Index of vertex in face.
	 *
	 * @return  Y component of vertex normal.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	public double getVertexNormalY( final int index )
	{
		_object.calculateVertexNormals();
		return _object._vertexNormals[ _vertexIndices[ index ] * 3 + 1 ];
	}

	/**
	 * Get Z component of vertex normal with the specified index.
	 *
	 * @param   index   Index of vertex in face.
	 *
	 * @return  X component of vertex normal.
	 *
	 * @throws  IndexOutOfBoundsException if <code>index</code> is out of bounds.
	 */
	public double getVertexNormalZ( final int index )
	{
		_object.calculateVertexNormals();
		return _object._vertexNormals[ _vertexIndices[ index ] * 3 + 2 ];
	}

	public double getX( final int index )
	{
		return _object._vertexCoordinates[ _vertexIndices[ index ] * 3 ];
	}

	public double getY( final int index )
	{
		return _object._vertexCoordinates[ _vertexIndices[ index ] * 3 + 1 ];
	}

	public double getZ( final int index )
	{
		return _object._vertexCoordinates[ _vertexIndices[ index ] * 3 + 2 ];
	}


	/**
	 * Create human-readable representation of this object.
	 * This is aspecially useful for debugging purposes.
	 *
	 * @return  Human-readable representation of this object.
	 */
	public String toFriendlyString( final String prefix )
	{
		final StringBuffer sb = new StringBuffer();

		final Object3D    object            = _object;
		final int         faceIndex         = object.getFaceIndex( this );
		final int[]       vertexIndices     = _vertexIndices;
		final double[]    vertexCoordinates = object.getVertexCoordinates();
		final TextureSpec texture           = _texture;
		final float       opacity           = _opacity;
		final boolean     smooth            = _smooth;
		final boolean     twoSided          = _twoSided;

		sb.append( prefix );
		sb.append( "face[ " );
		sb.append( faceIndex );
		sb.append( " ]:" );
		sb.append( '\n' );

		sb.append( prefix );
		sb.append( "  texture     = " );
		sb.append( texture );
		sb.append( '\n' );

		sb.append( prefix );
		sb.append( "  opacity     = " );
		sb.append( opacity );
		sb.append( '\n' );

		sb.append( prefix );
		sb.append( "  smooth      = " );
		sb.append( smooth );
		sb.append( '\n' );

		sb.append( prefix );
		sb.append( "  normal      = " );
		sb.append( Vector3D.toFriendlyString( getNormal() ) );
		sb.append( '\n' );

		sb.append( prefix );
		sb.append( "  twoSided    = " );
		sb.append( twoSided );
		sb.append( '\n' );

		for ( int i = 0 ; i < vertexIndices.length ; i++ )
		{
			final int    index = vertexIndices[ i ];
			final double x     = vertexCoordinates[ index * 3     ];
			final double y     = vertexCoordinates[ index * 3 + 1 ];
			final double z     = vertexCoordinates[ index * 3 + 2 ];

			sb.append( prefix );
			sb.append( "  vertex[ " );
			if ( i < 10 ) sb.append( ' ' );
			sb.append( i );
			sb.append( " ] = index=" );
			sb.append( index );
			sb.append( ", coordinates=" );
			sb.append( Vector3D.toFriendlyString( Vector3D.INIT.set( x , y , z ) ) );
			sb.append( '\n' );
		}

		return sb.toString();
	}
}
