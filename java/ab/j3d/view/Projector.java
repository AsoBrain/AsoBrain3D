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

import java.awt.geom.Point2D;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.geom.BasicRay3D;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.Face3D;

import com.numdata.oss.ArrayTools;

/**
 * A projector defines abstract methods to project 3D points on to a 2D
 * surface (image plate, view plane, screen) and back. Implementations
 * of this class provide several projection methods.
 *
 * @author  Rob Veneberg
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class Projector
{
	/**
	 * Projection policy: perspective.
	 *
	 * @see     #createInstance
	 * @see     PerspectiveProjector
	 */
	public static final int PERSPECTIVE = 0;

	/**
	 * Projection policy: parallel.
	 *
	 * @see     #createInstance
	 * @see     ParallelProjector
	 */
	public static final int PARALLEL = 1;

	/**
	 * Projection policy: isometric.
	 *
	 * @see     #createInstance
	 * @see     IsometricProjector
	 */
	public static final int ISOMETRIC = 2;

	/**
	 * Image width in pixels.
	 */
	protected final int _imageWidth;

	/**
	 * Image height in pixels.
	 */
	protected final int _imageHeight;

	/**
	 * Image resolution in meters per pixel.
	 */
	protected final double _imageResolution;

	/**
	 * View unit in meters per unit (e.g. {@link ViewModel#MM}).
	 */
	protected final double _viewUnit;

	/**
	 * Front clipping plane distance in view units.
	 */
	protected final double _frontClipDistance;

	/**
	 * Back clipping plane distance in view units.
	 */
	protected final double _backClipDistance;

	/**
	 * Linear zoom factor.
	 */
	protected final double _zoomFactor;

	/**
	 * Scale factor from view coordinates to image coordinates (pixels).
	 */
	protected final double _view2pixels;

	/**
	 * Horizontal limit of view volume.
	 * <p />
	 * This is the distance from the left edge of the view volume to the
	 * center of the view volume.
	 * <pre>
	 *    +-------------+
	 *   /             /|
	 *  /             / |
	 * +-------------+  |
	 * |             |  |
	 * |             |  |
	 * |             |  |
	 * |<---->0      |  |
	 * |             |  +
	 * |             | /
	 * |             |/
	 * +-------------+
	 * </pre>
	 */
	protected final double _limitX;

	/**
	 * Vertical limit of view volume.
	 * <p />
	 * This is the distance from the top edge of the view volume to the
	 * center of the view volume.
	 * <pre>
	 *    +-------------+
	 *   /             /|
	 *  /             / |
	 * +-------------+  |
	 * |      ^      |  |
	 * |      |      |  |
	 * |      v      |  |
	 * |      0      |  |
	 * |             |  +
	 * |             | /
	 * |             |/
	 * +-------------+
	 * </pre>
	 */
	protected final double _limitY;

	/**
	 * Create projector with the specified properties.
	 *
	 * @param   projectionPolicy    Projection policy.
	 * @param   imageWidth          Image width in pixels.
	 * @param   imageHeight         Image height in pixels.
	 * @param   imageResolution     Image resolution in meters per pixel.
	 * @param   viewUnit            Unit scale factor (e.g. {@link ViewModel#MM}).
	 * @param   frontClipDistance   Front clipping plane distance in view units.
	 * @param   backClipDistance    Back clipping plane distance in view units.
	 * @param   fieldOfView         Camera's field of view in radians.
	 * @param   zoomFactor          Linear zoom factor.
	 *
	 * @return  Projector instance.
	 *
	 * @throws  IllegalArgumentException if no projector could be created for
	 *          the specified properties.
	 */
	public static Projector createInstance( final int projectionPolicy , final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double fieldOfView , final double zoomFactor )
	{
		final Projector result;

		switch ( projectionPolicy )
		{
			case PERSPECTIVE :
				result = new PerspectiveProjector( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
				break;

			case PARALLEL :
				result = new ParallelProjector( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );
				break;

			case ISOMETRIC :
				result = new IsometricProjector( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );
				break;

			default :
				throw new IllegalArgumentException( "unknown projection policy: " + projectionPolicy );
		}

		return result;
	}

	/**
	 * Construct new Projector.
	 *
	 * @param   imageWidth          Image width in pixels.
	 * @param   imageHeight         Image height in pixels.
	 * @param   imageResolution     Image resolution in meters per pixel.
	 * @param   viewUnit            Unit scale factor (e.g. {@link ViewModel#MM}).
	 * @param   frontClipDistance   Front clipping plane distance in view units.
	 * @param   backClipDistance    Back clipping plane distance in view units.
	 * @param   zoomFactor          Linear zoom factor.
	 */
	protected Projector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
	{
		_viewUnit          = viewUnit;
		_imageWidth        = imageWidth;
		_imageHeight       = imageHeight;
		_imageResolution   = imageResolution;
		_frontClipDistance = -Math.abs( frontClipDistance );
		_backClipDistance  = -Math.abs( backClipDistance );
		_zoomFactor        = zoomFactor;

		final double view2pixels = zoomFactor * viewUnit / imageResolution;

		_view2pixels = view2pixels;
		_limitX      = (double)imageWidth  / ( 2.0 * view2pixels );
		_limitY      = (double)imageHeight / ( 2.0 * view2pixels );
	}

	/**
	 * This function tests if a face lies within the view volume of this
	 * projector. The view coordinates of the face vertices are specified as a
	 * <code>double</code>-array argument.
	 *
	 * @param   face            Face to test against view volume.
	 * @param   vertexCoordinates     View coordinates of object's vertices.
	 *
	 * @return  <code>true</code> if the face lies completely within the view volume;
	 *          <code>false</code> if the face has no vertices, or lies (partly)
	 *          outside the view volume.
	 *
	 * @see     #inViewVolume(double, double, double)
	 * @see     Face3D#getVertexIndices
	 */
	public final boolean inViewVolume( final Face3D face , final double[] vertexCoordinates )
	{
		boolean result = false;

		final int vertexCount = face.getVertexCount();
		if ( vertexCount > 0 )
		{
			result = true;

			final int[] vertexIndices = face.getVertexIndices();
			for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
			{
				final int vertexIndex = vertexIndices[ vertex ] * 3;

				if ( !inViewVolume( vertexCoordinates[ vertexIndex  ] , vertexCoordinates[ vertexIndex + 1 ] , vertexCoordinates[ vertexIndex + 2 ] ) )
				{
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * This function tests if a polygon lies outside the view volume of this projector.
	 *
	 * @param   vertices    Vertices that specify the polygon (one triplet per vertex).
	 *
	 * @return  <code>true</code> if the polygon lies completely outside the view volume;
	 *          <code>false</code> if the polygon has no vertices, or lies (partly)
	 *          inside the view volume.
	 *
	 * @see     #inViewVolume(double, double, double)
	 */
	public final boolean outsideViewVolume( final double[] vertices )
	{
		boolean result = false;

		final int vertexCount = vertices.length / 3;
		if ( vertexCount > 0 )
		{
			result = true;

			for ( int i = 0 ; i < vertexCount ; i++ )
			{
				final int vertexIndex = i * 3;

				if ( inViewVolume( vertices[ vertexIndex ] , vertices[ vertexIndex + 1 ] , vertices[ vertexIndex + 2 ] ) )
				{
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * This function tests if a point defined in view coordinates lies within
	 * the view volume of this projector.
	 *
	 * @param   x           X coordinate of point in view.
	 * @param   y           Y coordinate of point in view.
	 * @param   z           Z coordinate of point in view.
	 *
	 * @return  <code>true</code> if the point lies within the view volume;
	 *          <code>false</code> if the point lies outside the view volume.
	 *
	 * @see     #inViewVolume(Face3D, double[])
	 */
	public abstract boolean inViewVolume( double x , double y , double z );

	/**
	 * This function projects a set of 3D points on a 2D surface
	 * (image plate, view plane, screen). Point coordinates are supplied using
	 * double arrays containing a triplets/duos for each point.
	 *
	 * @param   source      Source array with 3D coordinates.
	 * @param   dest        Destination array for 2D coordinates (may be
	 *                      <code>null</code> or too small to create new).
	 * @param   pointCount  Number of points.
	 *
	 * @return  Array to which the projected coordinates were written
	 *          (may be different from the <code>dest</code> argument).
	 */
	public abstract int[] project( final double[] source , final int[] dest , final int pointCount );

	/**
	 * Get multiplicative scale factor from view coordinates to image
	 * coordinates (pixels).
	 *
	 * @return  Scale factor from view units to pixels.
	 */
	public double getView2pixels()
	{
		return _view2pixels;
	}

	/**
	 * This method does the oposite of project. Where project returns a point on
	 * the screen for a given point in the 3D view, this method returns the
	 * point in the view for a given point on the screen. <code>x</code> and
	 * <code>y</code> are the screen coordinates, and distance is the distance
	 * between the viewing plane and the 'unprojected' coordinate.
	 *
	 * @param   imageX  X image plane coordinate (pixels).
	 * @param   imageY  Y image plane coordinate (pixels).
	 * @param   viewZ   Z coordinate relative to view plane (view units).
	 *
	 * @return  3D view coordinates for the given 2D image coordinates.
	 */
	public abstract Vector3D imageToView( double imageX , double imageY , double viewZ );

	/**
	 * This method projects a 3D point in view coordinates onto the 2D image
	 * (image plate, view plane, screen).
	 *
	 * @param   point   Point in view coordinate system.
	 *
	 * @return  Point on 2D image (pixels).
	 */
	public Point2D viewToImage( final Vector3D point )
	{
		return viewToImage( point.x , point.y , point.z );
	}

	/**
	 * This method projects a 3D point in view coordinates onto the 2D image
	 * (image plate, view plane, screen).
	 *
	 * @param   viewX   X coordinate of point in view.
	 * @param   viewY   Y coordinate of point in view.
	 * @param   viewZ   Z coordinate of point in view.
	 *
	 * @return  Point on 2D image (pixels).
	 */
	public abstract Point2D viewToImage( double viewX , double viewY , double viewZ );

	/**
	 * Get a ray originating from a (mouse) pointer at (<code>pointerX</code> ,
	 * <code>pointerY</code>) on the image plate, pointing into the view volume.
	 * <p />
	 * The ray is defined in the view's coordinate system, but may optionally be
	 * transformed using the <code>transform</code> argument.
	 *
	 * @param   transform   Optional transformation to apply to ray.
	 * @param   pointerX    X coordinate of pointer on image (in pixels).
	 * @param   pointerY    Y coordinate of pointer on image (in pixels).
	 *
	 * @return  Ray from pointer into view volume (VCS if not transformed).
	 */
	public abstract Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY );

	/**
	 * Perspective projector implementation.
	 * <p />
	 * Perspective projection maps 3D points on a 2D surface by taking the
	 * intersection point between the image plate and a line from the eye to
	 * the 3D point.
	 * <pre>
	 *  3D points     |
	 *     (1)---___  |Projected 2D points
	 *              -(a)-____
	 *                |      ---(E) Eye point
	 *                |___---
	 *         (2)__-(b)
	 *                |
	 * *          image plate
	 * </pre>
	 */
	public static final class PerspectiveProjector
		extends Projector
	{
		/**
		 * Eye distance in view coordinates.
		 */
		private final double _eyeDistance;

		/**
		 * Construct perspective projector.
		 *
		 * @param   imageWidth          Image width in pixels.
		 * @param   imageHeight         Image height in pixels.
		 * @param   imageResolution     Image resolution in meters per pixel.
		 * @param   viewUnit            Unit scale factor (e.g. {@link ViewModel#MM}).
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   fieldOfView         Camera's field of view in radians.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		public PerspectiveProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double fieldOfView , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );

			final double view2pixels = _view2pixels;
			final double viewWidth   = (double)imageWidth / view2pixels;

			_eyeDistance = viewWidth / ( 2.0 * Math.tan( fieldOfView / 2.0 ) );
		}

		public Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY )
		{
			final double centerX     = (double)( _imageWidth  >> 1 );
			final double centerY     = (double)( _imageHeight >> 1 );
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double viewX = ( pointerX - centerX ) / view2pixels;
			final double viewY = ( centerY - pointerY ) / view2pixels;

			final double distance = Math.sqrt( viewX * viewX + viewY * viewY + eyeDistance * eyeDistance );

			return new BasicRay3D( transform , viewX , viewY , -eyeDistance , viewX / distance , viewY / distance , -eyeDistance / distance , true );
		}

		public int[] project( final double[] source , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;
			final int[]  result       = (int[])ArrayTools.ensureLength( dest , int.class , -1 , resultLength );

			final int    centerX     = _imageWidth  >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			for ( int sourceIndex = 0 , resultIndex = 0 ; resultIndex < resultLength ; sourceIndex += 3 , resultIndex += 2 )
			{
				final double x = source[ sourceIndex     ];
				final double y = source[ sourceIndex + 1 ];
				final double z = source[ sourceIndex + 2 ];

				final double f = view2pixels / ( 1.0 - ( z + eyeDistance ) / eyeDistance );

				result[ resultIndex     ] = centerX + (int)( f * x + 0.5 );
				result[ resultIndex + 1 ] = centerY - (int)( f * y + 0.5 );
			}

			return result;
		}

		public Vector3D imageToView( final double imageX , final double imageY , final double viewZ )
		{
			final double centerX     = (double)( _imageWidth  >> 1 );
			final double centerY     = (double)( _imageHeight >> 1 );
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double f = view2pixels / ( 1.0 - ( viewZ + eyeDistance ) / eyeDistance );

			final double viewX = ( imageX - centerX ) / f;
			final double viewY = ( centerY - imageY ) / f;

			return Vector3D.INIT.set( viewX , viewY , viewZ );
		}

		public boolean inViewVolume( final double x , final double y , final double z )
		{
//			final boolean result;
//
//			if ( ( z >= _backClipDistance ) && ( z <= _frontClipDistance ) )
//			{
				return true;
//				final double f      = 1.0 - z / _eyeDistance;
//				final double limitX = _limitX * f;
//				final double limitY = _limitY * f;
//
//				result = ( x >= -limitX ) && ( x <= limitX )
//				      && ( y >= -limitY ) && ( y <= limitY );
//			}
//			else /* outside front/back clipping plane */
//			{
//				result = false;
//			}
//
//			return result;
		}

		public Point2D viewToImage( final double viewX , final double viewY , final double viewZ )
		{
			final double centerX     = (double)( _imageWidth  >> 1 );
			final double centerY     = (double)( _imageHeight >> 1 );
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double f = view2pixels / ( 1.0 - ( viewZ + eyeDistance ) / eyeDistance );

			final double imageX = centerX + f * viewX;
			final double imageY = centerY - f * viewY;

			return new Point2D.Double( imageX , imageY );
		}
	}

	/**
	 * Parallel projector implementation.
	 * <p />
	 * Parallel projection maps 3D X and Y coordinates linearly to 2D
	 * coordinates. Depth information (Z) is ignored completely. It only scales,
	 * translates, and flips the Y axis direction.
	 */
	public static class ParallelProjector
		extends Projector
	{
		/**
		 * Direction of pointer ray.
		 */
		private static final Vector3D POINTER_DIRECTION = Vector3D.INIT.set( 0.0 , 0.0 , -1.0 );

		/**
		 * Construct parallel projector.
		 *
		 * @param   imageWidth          Image width in pixels.
		 * @param   imageHeight         Image height in pixels.
		 * @param   imageResolution     Image resolution in meters per pixel.
		 * @param   viewUnit            Unit scale factor in meters per view unit.
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		public ParallelProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );
		}

		public Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY )
		{
			final Vector3D origin    = imageToView( pointerX , pointerY , 0.0 );
			final Vector3D direction = POINTER_DIRECTION;

			return new BasicRay3D( transform , origin , direction , false );
		}

		public int[] project( final double[] source , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;
			final int[]  result       = (int[])ArrayTools.ensureLength( dest , int.class , -1 , resultLength );

			final int    centerX     = _imageWidth  >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;

			for ( int sourceIndex = 0 , resultIndex = 0 ; resultIndex < resultLength ; sourceIndex += 3 , resultIndex +=2 )
			{
				final double x = source[ sourceIndex     ];
				final double y = source[ sourceIndex + 1 ];

				result[ resultIndex     ] = centerX + (int)( view2pixels * x + 0.5 );
				result[ resultIndex + 1 ] = centerY - (int)( view2pixels * y + 0.5 );
			}

			return result;
		}

		public Vector3D imageToView( final double imageX , final double imageY , final double viewZ )
		{
			final double centerX     = (double)_imageWidth / 2.0;
			final double centerY     = (double)_imageHeight / 2.0;
			final double view2pixels = _view2pixels;

			final double viewX = ( imageX - centerX ) / view2pixels;
			final double viewY = ( centerY - imageY ) / view2pixels;

			return Vector3D.INIT.set( viewX , viewY , viewZ );
		}

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			return ( x >= -_limitX ) && ( x <= _limitX )
			    && ( y >= -_limitY ) && ( y <= _limitY )
			    && ( z >= _backClipDistance ) && ( z <= _frontClipDistance );
		}

		public Point2D viewToImage( final double viewX , final double viewY , final double viewZ )
		{
			final double centerX     = (double)( _imageWidth  >> 1 );
			final double centerY     = (double)( _imageHeight >> 1 );
			final double view2pixels = _view2pixels;

			final double imageX = centerX + view2pixels * viewX;
			final double imageY = centerY - view2pixels * viewY;

			return new Point2D.Double( imageX , imageY );
		}
	}

	/**
	 * Isometric projector implementation.
	 * <p />
	 * Isometric projection is a parallel projection method that projects the
	 * view Z-axis onto the rendered X- and Y-axis by using the displacing
	 * points by the half Z-value 30 degrees relative to the X-axis (top-right).
	 */
	public static class IsometricProjector
		extends Projector
	{
		/**
		 * X-component of the Z-axis.
		 */
		private final double _xComponentOfZ;

		/**
		 * Y-component of the Z-axis.
		 */
		private final double _yComponentOfZ;

		/**
		 * Direction of pointer ray (calculated on-demand).
		 *
		 * @see     #getPointerRay
		 */
		private Vector3D _pointerDirection;

		/**
		 * Construct isometric projector.
		 *
		 * @param   imageWidth          Image width in pixels.
		 * @param   imageHeight         Image height in pixels.
		 * @param   imageResolution     Image resolution in meters per pixel.
		 * @param   viewUnit            Unit scale factor (e.g. {@link ViewModel#MM}).
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		public IsometricProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );

			final double zScale = 0.5;
			final double zAngle = Math.PI / 6.0; // 30 decimal degrees

			_xComponentOfZ    = zScale * Math.cos( zAngle );
			_yComponentOfZ    = zScale * Math.sin( zAngle );
			_pointerDirection = null;
		}

		public Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY )
		{
			final Vector3D origin = imageToView( pointerX , pointerY , 0.0 );

			Vector3D direction = _pointerDirection;
			if ( direction == null )
			{
				final double isoX   = _xComponentOfZ;
				final double isoY   = _yComponentOfZ;
				final double length = Math.sqrt( isoX * isoX + isoY * isoY + 1.0 );

				direction = Vector3D.INIT.set( -isoX / length , -isoY / length , -1.0 / length );
				_pointerDirection = direction;
			}

			return new BasicRay3D( transform , origin , direction , false );
		}

		public int[] project( final double[] src , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;
			final int[]  result       = (int[])ArrayTools.ensureLength( dest , int.class , -1 , resultLength );

			final int    centerX     = _imageWidth >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;
			final double isoX        = _xComponentOfZ;
			final double isoY        = _yComponentOfZ;

			for ( int sourceIndex = 0 , resultIndex = 0 ; resultIndex < resultLength ; sourceIndex += 3 , resultIndex +=2 )
			{
				final double x = src[ sourceIndex     ];
				final double y = src[ sourceIndex + 1 ];
				final double z = src[ sourceIndex + 2 ];

				result[ resultIndex     ] = centerX + (int)( view2pixels * ( x - z * isoX ) + 0.5 );
				result[ resultIndex + 1 ] = centerY - (int)( view2pixels * ( y - z * isoY ) + 0.5 );
			}

			return result;
		}

		public Vector3D imageToView( final double imageX , final double imageY , final double viewZ )
		{
			final double centerX     = (double)_imageWidth / 2.0;
			final double centerY     = (double)_imageHeight / 2.0;
			final double view2pixels = _view2pixels;
			final double isoX        = _xComponentOfZ;
			final double isoY        = _yComponentOfZ;

			final double viewX = ( imageX - centerX ) / view2pixels + viewZ * isoX;
			final double viewY = ( centerY - imageY ) / view2pixels + viewZ * isoY;

			return Vector3D.INIT.set( viewX , viewY , viewZ );
		}

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			double tmp;

			return ( z >= _backClipDistance ) && ( z <= _frontClipDistance )
			    && ( ( tmp = ( x - z * _xComponentOfZ     ) ) >= -_limitX ) && ( tmp <= _limitX )
			    && ( ( tmp = (     z * _yComponentOfZ - y ) ) >= -_limitY ) && ( tmp <= _limitY );
		}

		public Point2D viewToImage( final double viewX , final double viewY , final double viewZ )
		{
			final double centerX     = (double)( _imageWidth  >> 1 );
			final double centerY     = (double)( _imageHeight >> 1 );
			final double view2pixels = _view2pixels;
			final double isoX        = _xComponentOfZ;
			final double isoY        = _yComponentOfZ;

			final double imageX = centerX + view2pixels * ( viewX - viewZ * isoX );
			final double imageY = centerY - view2pixels * ( viewY - viewZ * isoY );

			return new Point2D.Double( imageX , imageY );
		}
	}
}
