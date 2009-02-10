/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2009
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

import java.awt.Point;
import java.awt.Polygon;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Face3D.Vertex;
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
	 * Face for which the polygon was defined.
	 */
	public Face3D _face;

	/**
	 * Material applied to this face. Taken from {@link Face3D#material}.
	 *
	 * @see     Face3D#material
	 */
	public Material _material;

	/**
	 * Use alternate vs. regular appearance for face.
	 *
	 * @see     Object3D#fillColor
	 * @see     Object3D#outlineColor
	 * @see     Object3D#alternateFillColor
	 * @see     Object3D#alternateOutlineColor
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
	public int _minImageX;

	/**
	* The largest of all the x values of this polygon.
	*/
	public int _maxImageX;

	/**
	* The smallest of all the y values of this polygon.
	*/
	public int _minImageY;

	/**
	* The largest of all the y values of this polygon.
	*/
	public int _maxImageY;

	/**
	 * The smallest of all the z values of this polygon.
	 */
	public double _minViewZ;

	/**
	 * The largest of all the z values of this polygon.
	 */
	public double _maxViewZ;

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
		_face                = null;
		_vertexCount         = vertexCount;

		_viewX = new double[ vertexCount ];
		_viewY = new double[ vertexCount ];
		_viewZ = new double[ vertexCount ];

		_projectedX = projectedX;
		_projectedY = projectedY;

		_planeNormalX  = 0.0;
		_planeNormalY  = 0.0;
		_planeNormalZ  = 1.0;
		_planeConstant = 0.0;

		_minImageX = 0;
		_maxImageX = 0;
		_minImageY = 0;
		_maxImageY = 0;
		_minViewZ = 0.0;
		_maxViewZ = 0.0;

		_material = null;
		_alternateAppearance = false;

		xpoints = projectedX;
		ypoints = projectedY;
		npoints = vertexCount;
	}

	/**
	 * Initialize polygon with face properties.
	 *
	 * @param   object2view             Transforms object to view coordinates.
	 * @param   projector               Projects view coordinates on image plate (pixels).
	 * @param   object                  Object to get face from.
	 * @param   face                    Face to initialize polygon with.
	 * @param   alternateAppearance     Use alternate vs. regular object appearance.
	 */
	public void initialize( final Matrix3D object2view , final Projector projector , final Object3D object , final Face3D face , final boolean alternateAppearance )
	{
		final int      pointCount = _vertexCount;
		final int[]    projectedX = _projectedX;
		final int[]    projectedY = _projectedY;
		final double[] viewX      = _viewX;
		final double[] viewY      = _viewY;
		final double[] viewZ      = _viewZ;

		if ( ( xpoints != projectedX ) || ( ypoints != projectedY ) || ( npoints != pointCount ) )
			throw new IllegalStateException();

		final List<Vertex> vertices = face.vertices;
		if ( vertices.size() != pointCount )
			throw new IllegalArgumentException();

		int    minImageX = Integer.MAX_VALUE;
		int    maxImageX = Integer.MIN_VALUE;
		int    minImageY = Integer.MAX_VALUE;
		int    maxImageY = Integer.MIN_VALUE;
		double minViewZ = Double.POSITIVE_INFINITY;
		double maxViewZ = Double.NEGATIVE_INFINITY;

		final Point projectedPoint = new Point();

		for ( int vertexIndex = 0 ; vertexIndex < pointCount ; vertexIndex++ )
		{
			final Vertex vertex = vertices.get( vertexIndex );
			final Vector3D oPoint = vertex.point;

			final double x = object2view.transformX( oPoint );
			final double y = object2view.transformY( oPoint );
			final double z = object2view.transformZ( oPoint );

			viewX[ vertexIndex ] = x;
			viewY[ vertexIndex ] = y;
			viewZ[ vertexIndex ] = z;

			minViewZ = z < minViewZ ? z : minViewZ;
			maxViewZ = z > maxViewZ ? z : maxViewZ;

			if ( projector != null )
			{
				projector.project( projectedPoint , x , y , z );
				final int projX = projectedPoint.x;
				final int projY = projectedPoint.y;

				projectedX[ vertexIndex ] = projX;
				projectedY[ vertexIndex ] = projY;

				minImageX = projX < minImageX ? projX : minImageX;
				maxImageX = projX > maxImageX ? projX : maxImageX;
				minImageY = projY < minImageY ? projY : minImageY;
				maxImageY = projY > maxImageY ? projY : maxImageY;
			}
		}

		final double planeNormalX = object2view.rotateX( face.normal );
		final double planeNormalY = object2view.rotateY( face.normal );
		final double planeNormalZ = object2view.rotateZ( face.normal );
		final double planeConstant = planeNormalX * viewX[ 0 ] + planeNormalY * viewY[ 0 ] + planeNormalZ * viewZ[ 0 ];

		_object              = object;
		_face                = face;
		_planeNormalX        = planeNormalX;
		_planeNormalY        = planeNormalY;
		_planeNormalZ        = planeNormalZ;
		_planeConstant       = planeConstant;
		_minImageX           = minImageX;
		_maxImageX           = maxImageX;
		_minImageY           = minImageY;
		_maxImageY           = maxImageY;
		_minViewZ            = minViewZ;
		_maxViewZ            = maxViewZ;
		_material            = face.material;
		_alternateAppearance = alternateAppearance;
	}

	/**
	 * Release polygon for reuse.
	 */
	void destroy()
	{
		invalidate();
		_object   = null;
		_face     = null;
		_material = null;
	}

	/**
	 * Test if polygon is in view volume.
	 *
	 * @param   imageWidth      Width of image.
	 * @param   imageHeight     Height of image.
	 *
	 * @return  <code>true</code> if polygon is (possibly) in view volume;
	 *          <code>false</code> if polygon is (surely) outside view volume.
	 */
	public boolean inViewVolume( final int imageWidth , final int imageHeight )
	{
		return ( _minImageX < imageWidth ) && ( _maxImageX >= 0 ) && ( _minImageY < imageHeight ) && ( _maxImageY >= 0 );
	}

	/**
	 * Test if this is a back face.
	 * <p>
	 * A backface is identified by a negative Z component of the face normal.
	 * <p>
	 * Since we are only interested in the Z component of the normal, we can
	 * simply use the projected 2D points of the face. This has the advantage of
	 * properly handling depth deformation due to (perspective) projection.
	 *
	 * @return  <code>true</code> if this is a backface;
	 *          <code>false</code> if this is not a (back)face.
	 */
	public boolean isBackface()
	{
		final boolean result;

		if ( _vertexCount < 3 ) /* a void, point, or line can not be a face, so no backface either */
		{
			result = false;
		}
		else
		{
			final int[] projectedX = _projectedX;
			final int[] projectedY = _projectedY;

			final int x2 = projectedX[ 1 ];
			final int y2 = projectedY[ 1 ];

			result = ( ( projectedX[ 0 ] - x2 ) * ( projectedY[ 2 ] - y2 )
			        >= ( projectedY[ 0 ] - y2 ) * ( projectedX[ 2 ] - x2 ) );
		}

		return result;
	}

	/**
	 * Get the estimated surface area factor of this rendered polygon.
	 * <p />
	 * This method does not return a estimated surface size, but only
	 * a factor which can be used to i.e. determine the polygon that has
	 * potentially the greatest surface area from a list of polygons.
	 *
	 * @return  The estimated surface area factor of this rendered polygon.
	 *
	 * @see     BSPTree#getPartitionPlane
	 */
	public double getEstimatedSurfaceAreaFactor()
	{
		final double x1 = _viewX[ 1 ] - _viewX[ 0 ];
		final double x2 = _viewX[ 2 ] - _viewX[ 1 ];
		final double y1 = _viewY[ 1 ] - _viewY[ 0 ];
		final double y2 = _viewY[ 2 ] - _viewY[ 1 ];
		final double z1 = _viewZ[ 1 ] - _viewZ[ 0 ];
		final double z2 = _viewZ[ 2 ] - _viewZ[ 1 ];

		final double d1   = ( x1 * x1 ) + ( y1 * y1 ) + ( z1 * z1 );
		final double d2   = ( x2 * x2 ) + ( y2 * y2 ) + ( z2 * z2 );

		return d1 * d2;
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
		final StringBuilder sb = new StringBuilder();

		sb.append(   "Object: "               ); sb.append( _object );
		sb.append( "\nFace: "                 ); sb.append( _face );
		sb.append( "\nMaterial: "             ); sb.append( ( _material == null ? "null" : _material.code ) );
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
		sb.append( _minViewZ );
		sb.append( "\nMaximum Z: " );
		sb.append( _maxViewZ );

		sb.append( '\n' );

		return sb.toString();
	}
}
