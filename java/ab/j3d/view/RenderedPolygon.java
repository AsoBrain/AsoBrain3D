/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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
package ab.j3d.view;

import java.awt.Polygon;

import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

/**
 * The <code>RenderedPolygon</code> class extends the {@link Polygon} features
 * with rendering properties for 3D face projected on a 2D surface.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class RenderedPolygon
	extends Polygon
{
	/**
	 * Object for which the polygon was defined.
	 */
	public Object3D _object;

	/**
	 * Texture applied to this face. Taken from {@link Face3D#getTexture()}.
	 *
	 * @see     Face3D#getTexture
	 * @see     Face3D#getTextureU
	 * @see     Face3D#getTextureV
	 */
	public TextureSpec _texture;

	/**
	 * Use alternate vs. regular appearance for face.
	 *
	 * @see     Object3D#fillPaint
	 * @see     Object3D#outlinePaint
	 * @see     Object3D#alternateFillPaint
	 * @see     Object3D#alternateOutlinePaint
	 */
	public boolean _alternateAppearance;

	/**
	 * The total number of vertices in polygon.
	 */
	public final int _vertexCount;

	/**
	 * Array of projected X coordinates. The number of elements matches the
	 * {@link #_vertexCount} field, and the {@link #xpoints} field must always
	 * remain the same as this field.
	 */
	public final int[] _projectedX;

	/**
	 * Array of projected Y coordinates. The number of elements matches the
	 * {@link #_vertexCount} field, and the {@link #xpoints} field must always
	 * remain the same as this field.
	 */
	public final int[] _projectedY;

	/**
	 * Array with X coordinates of points in view space.
	 */
	public final double[] _viewX;

	/**
	 * Array with Y coordinates of points in view space.
	 */
	public final double[] _viewY;

	/**
	 * Array with Z coordinates of points in view space.
	 */
	public final double[] _viewZ;

	/**
	 * X component of face's plane normal in view space.
	 * <p />
	 * Any point (x,y,z,) on the face's plane satifies the plane equation:
	 * <pre>  (x,y,z) &#183; ({@link #_planeNormalX},{@link #_planeNormalY},{@link #_planeNormalZ}) = {@link #_planeConstant}</pre>
	 */
	public double _planeNormalX;

	/**
	 * Y component of face's plane normal in view space.
	 * <p />
	 * Any point (x,y,z,) on the face's plane satifies the plane equation:
	 * <pre>  (x,y,z) &#183; ({@link #_planeNormalX},{@link #_planeNormalY},{@link #_planeNormalZ}) = {@link #_planeConstant}</pre>
	 */
	public double _planeNormalY;

	/**
	 * Z component of face's plane normal in view space.
	 * <p />
	 * Any point (x,y,z,) on the face's plane satifies the plane equation:
	 * <pre>  (x,y,z) &#183; ({@link #_planeNormalX},{@link #_planeNormalY},{@link #_planeNormalZ}) = {@link #_planeConstant}</pre>
	 */
	public double _planeNormalZ;

	/**
	 * Plane constant of face in view space.
	 * <p />
	 * Any point (x,y,z,) on the face's plane satifies the plane equation:
	 * <pre>  (x,y,z) &#183; ({@link #_planeNormalX},{@link #_planeNormalY},{@link #_planeNormalZ}) = {@link #_planeConstant}</pre>
	 */
	public double _planeConstant;

	/**
	* The smallest of all the x values of this polygon.
	*/
	public int _minX;

	/**
	* The largest of all the x values of this polygon.
	*/
	public int _maxX;

	/**
	* The smallest of all the y values of this polygon.
	*/
	public int _minY;

	/**
	* The largest of all the y values of this polygon.
	*/
	public int _maxY;

	/**
	 * The smallest of all the z values of this polygon.
	 */
	public double _minZ;

	/**
	 * The largest of all the z values of this polygon.
	 */
	public double _maxZ;

	/**
	 * Debugging variable. Not for other uses. Should be removed once
	 * {@link RenderQueue} works properly.
	 */
	public String _name = "";

	/**
	 * Debugging variable. Not for other uses. Should be removed once
	 * {@link RenderQueue} works properly.
	 */
	public String _text = "";

	/**
	 * Construct polygon.
	 *
	 * @param   vertexCount     Desired number of vertices in polygon.
	 */
	RenderedPolygon( final int vertexCount )
	{
		this( vertexCount , new int[ vertexCount ] , new int[ vertexCount ] );
	}

	/**
	 * Private constructor for polygon. Created for efficiency related to
	 * array allocation in the {@link Polygon#Polygon(int[],int[],int)}
	 * constructor.
	 *
	 * @param   vertexCount     Desired number of vertices in polygon.
	 * @param   projectedX      Newly created array for projected X coordinates.
	 * @param   projectedY      Newly created array for projected Y coordinates.
	 */
	private RenderedPolygon( final int vertexCount , final int[] projectedX , final int[] projectedY )
	{
		super( projectedX , projectedY , 0 );

		_object              = null;
		_vertexCount         = vertexCount;
		_projectedX          = projectedX;
		_projectedY          = projectedY;
		_viewX               = new double[ vertexCount ];
		_viewY               = new double[ vertexCount ];
		_viewZ               = new double[ vertexCount ];
		_planeNormalX        = 0.0;
		_planeNormalY        = 0.0;
		_planeNormalZ        = 1.0;
		_planeConstant       = 0.0;
		_minX                = 0;
		_maxX                = 0;
		_minY                = 0;
		_maxY                = 0;
		_minZ                = 0.0;
		_maxZ                = 0.0;
		_texture             = null;
		_alternateAppearance = false;

		xpoints = projectedX;
		ypoints = projectedY;
		npoints = vertexCount;
	}

	/**
	 * Initialize polygon with face properties.
	 *
	 * @param   face                            Face to get polygon properties from.
	 * @param   objectViewCoordinates           Vertex coordinates of object in view space.
	 * @param   objectProjectedCoordindates     Projected points on image plate (pixels).
	 * @param   faceViewNormals                 Normals of object faces in view space.
	 * @param   alternateAppearance             Use alternate vs. regular object appearance.
	 *
	 * @see     Face3D
	 * @see     Object3D#getVertexCoordinates
	 * @see     Object3D#getFaceNormals
	 */
	public void initialize( final Face3D face , final double[] objectViewCoordinates , final int[] objectProjectedCoordindates , final double[] faceViewNormals , final boolean alternateAppearance )
	{
		final int      pointCount   = _vertexCount;
		final int[]    projectedX   = _projectedX;
		final int[]    projectedY   = _projectedY;
		final double[] viewX        = _viewX;
		final double[] viewY        = _viewY;
		final double[] viewZ        = _viewZ;

		if ( ( xpoints != projectedX ) || ( ypoints != projectedY ) || ( npoints != pointCount ) )
			throw new IllegalStateException();

		if ( face.getVertexCount() != pointCount )
			throw new IllegalArgumentException();

		final Object3D object        = face.getObject();
		final int[]    vertexIndices = face.getVertexIndices();

		int    minX = Integer.MAX_VALUE;
		int    maxX = Integer.MIN_VALUE;
		int    minY = Integer.MAX_VALUE;
		int    maxY = Integer.MIN_VALUE;
		double minZ = Double.MAX_VALUE;
		double maxZ = Double.MIN_VALUE;

		for ( int faceVertex = 0 ; faceVertex < pointCount ; faceVertex++ )
		{
			final int objectVertex  = vertexIndices[ faceVertex ];
			final int objectVertex2 = objectVertex * 2;
			final int objectVertex3 = objectVertex * 3;

			final int projX = objectProjectedCoordindates[ objectVertex2     ];
			final int projY = objectProjectedCoordindates[ objectVertex2 + 1 ];
			projectedX[ faceVertex ] = projX;
			projectedY[ faceVertex ] = projY;

			viewX[ faceVertex ] = objectViewCoordinates[ objectVertex3     ];
			viewY[ faceVertex ] = objectViewCoordinates[ objectVertex3 + 1 ];
			viewZ[ faceVertex ] = objectViewCoordinates[ objectVertex3 + 2 ];
			final double z      = objectViewCoordinates[ objectVertex3 + 2 ];

			minX = projX < minX ? projX : minX;
			maxX = projX > maxX ? projX : maxX;
			minY = projY < minY ? projY : minY;
			maxY = projY > maxY ? projY : maxY;
			minZ = z     < minZ ? z     : minZ;
			maxZ = z     > maxZ ? z     : maxZ;
		}

		_minX = minX;
		_maxX = maxX;
		_minY = minY;
		_maxY = maxY;
		_minZ = minZ;
		_maxZ = maxZ;

		final double planeNormalX;
		final double planeNormalY;
		final double planeNormalZ;
		final double planeConstant;

		final double x0 = viewX[ 0 ];
		final double y0 = viewY[ 0 ];
		final double z0 = viewZ[ 0 ];

		if ( faceViewNormals != null )
		{
			final int faceIndex3 = object.getFaceIndex( face ) * 3;

			planeNormalX = faceViewNormals[ faceIndex3 ];
			planeNormalY = faceViewNormals[ faceIndex3 + 1 ];
			planeNormalZ = faceViewNormals[ faceIndex3 + 2 ];
		}
		else
		{
			final double x1 = viewX[ 1 ];
			final double y1 = viewY[ 1 ];
			final double z1 = viewZ[ 1 ];
			final double x2 = viewX[ 2 ];
			final double y2 = viewY[ 2 ];
			final double z2 = viewZ[ 2 ];

			planeNormalX = ( y0 - y1 ) * ( z2 - z1 ) - ( z0 - z1 ) * ( y2 - y1 );
			planeNormalY = ( z0 - z1 ) * ( x2 - x1 ) - ( x0 - x1 ) * ( z2 - z1 );
			planeNormalZ = ( x0 - x1 ) * ( y2 - y1 ) - ( y0 - y1 ) * ( x2 - x1 );
		}

		planeConstant = planeNormalX * x0 + planeNormalY * y0 + planeNormalZ * z0;

		_object              = object;
		_planeNormalX        = planeNormalX;
		_planeNormalY        = planeNormalY;
		_planeNormalZ        = planeNormalZ;
		_planeConstant       = planeConstant;
		_texture             = face.getTexture();
		_alternateAppearance = alternateAppearance;
	}

	/**
	 * Release polygon for reuse.
	 */
	void destroy()
	{
		invalidate();
		_object  = null;
		_texture = null;
	}

	/**
	 * Test if this is a back face.
	 * <p />
	 * A backface is identified by a negative Z component of the face normal.
	 * This is derived as follows:
	 * <pre>
	 * (x1,y1,z1) = first vertex of face
	 * (x2,y2,z2) = second point of face
	 * (x3,y3,z3) = third point of face
	 * </pre>
	 * We can derive the face normal <code>(a,b,c)</code> from these points
	 * by taking the cross product between the two edges defined between them:
	 * <pre>
	 * a = ( y1 - y2 ) * ( z3 - z2 ) - ( z1 - z2 ) * ( y3 - y2 )
	 * b = ( z1 - z2 ) * ( x3 - x2 ) - ( x1 - x2 ) * ( z3 - z2 )
	 * c = ( x1 - x2 ) * ( y3 - y2 ) - ( y1 - y2 ) * ( x3 - x2 )
	 * </pre>
	 * Since we are only interested in the Z component of the normal
	 * (<code>c</code>), we can simply use the projected 2D points of the
	 * face. This has the advantage of properly handling depth deformation due
	 * to (perspective) projection.
	 *
	 * @return  <code>true</code> if this is a backface;
	 *          <code>false</code> if this is not a (back)face.
	 */
	public boolean isBackface()
	{
		final boolean result;

		if ( _vertexCount < 3 ) /* a void, point, or line can not be culled */
		{
			result = false;
		}
		else
		{
			final int x2 = _projectedX[ 1 ];
			final int y2 = _projectedY[ 1 ];

			result = ( ( _projectedX[ 0 ] - x2 ) * ( _projectedY[ 2 ] - y2 )
			        >= ( _projectedY[ 0 ] - y2 ) * ( _projectedX[ 2 ] - x2 ) );
		}

		return result;
	}

	/**
	 * Returns a string with the values of all fields of this
	 * {@link RenderedPolygon}. This string is formatted along multiple lines.
	 *
	 * @return string with the values of all fields of this
	 *          {@link RenderedPolygon}.
	 */
	public String toFriendlyString()
	{
		final StringBuffer sb = new StringBuffer();

		sb.append(   "Object: "               ); sb.append( _object );
		sb.append( "\nTexture: "              ); sb.append( ( _texture == null ? "null" : _texture.code ) );
		sb.append( "\nAlternate appearance: " ); sb.append( _alternateAppearance );
		sb.append( "\nNormal: "               ); sb.append( Vector3D.toFriendlyString( Vector3D.INIT.set( _planeNormalX , _planeNormalY , _planeNormalZ ) ) );
		sb.append( "\nPlane constant: "       ); sb.append( _planeConstant );

		sb.append( "\nCoordinates:" );
		for ( int i = 0; i < _vertexCount; i++ )
		{
			sb.append( "\n\t" );
			sb.append( Vector3D.toFriendlyString( Vector3D.INIT.set( _viewX[ i ] , _viewY[ i ] , _viewZ[ i ] ) ) );
			sb.append( "\n" );
		}

		sb.append( "Projected coordinates:" );
		for ( int i = 0 ; i < _vertexCount ; i++ )
		{
			sb.append( "\n\t[ " );
			sb.append( _projectedX[ i ] );
			sb.append( " , " );
			sb.append( _projectedY[ i ] );
			sb.append( " ]\n" );
		}

		sb.append( "\nMinimum Z: " );
		sb.append( _minZ );
		sb.append( "\nMaximum Z: " );
		sb.append( _maxZ );

		sb.append( '\n' );

		return sb.toString();
	}
}
