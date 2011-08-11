/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

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
	 * Image width in pixels.
	 */
	protected final int _imageWidth;

	/**
	 * Image height in pixels.
	 */
	protected final int _imageHeight;

	/**
	 * Image width in pixels.
	 */
	protected final int _imageCenterX;

	/**
	 * Image height in pixels.
	 */
	protected final int _imageCenterY;

	/**
	 * Image resolution in meters per pixel.
	 */
	protected final double _imageResolution;

	/**
	 * View unit in meters per unit (e.g. {@link ab.j3d.model.Scene#MM}).
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
	 * Viewing frustum.
	 */
	private ViewingFrustum _viewingFrustum;

	/**
	 * Create projector with the specified properties.
	 *
	 * @param   projectionPolicy    Projection policy.
	 * @param   imageWidth          Image width in pixels.
	 * @param   imageHeight         Image height in pixels.
	 * @param   imageResolution     Image resolution in meters per pixel.
	 * @param   viewUnit            Unit scale factor (e.g. {@link ab.j3d.model.Scene#MM}).
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
	public static Projector createInstance( final ProjectionPolicy projectionPolicy , final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double fieldOfView , final double zoomFactor )
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
	 * @param   viewUnit            Unit scale factor (e.g. {@link ab.j3d.model.Scene#MM}).
	 * @param   frontClipDistance   Front clipping plane distance in view units.
	 * @param   backClipDistance    Back clipping plane distance in view units.
	 * @param   zoomFactor          Linear zoom factor.
	 *
	 * @throws  IllegalArgumentException if <code>frontClipDistance</code> is
	 *          negative, zero or not less than <code>backClipDistance</code>.
	 */
	protected Projector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
	{
		if ( frontClipDistance <= 0.0 )
			throw new IllegalArgumentException( "frontClipDistance <= 0.0: " + frontClipDistance );
		if ( frontClipDistance >= backClipDistance )
			throw new IllegalArgumentException( "frontClipDistance >= backClipDistance: " + frontClipDistance + " >= " + backClipDistance );

		_viewUnit          = viewUnit;
		_imageWidth        = imageWidth;
		_imageHeight       = imageHeight;
		_imageCenterX      = imageWidth / 2;
		_imageCenterY      = imageHeight / 2;
		_imageResolution   = imageResolution;
		_frontClipDistance = frontClipDistance;
		_backClipDistance  = backClipDistance;
		_zoomFactor        = zoomFactor;

		final double view2pixels = zoomFactor * viewUnit / imageResolution;

		_view2pixels = view2pixels;
		_limitX      = (double)imageWidth  / ( 2.0 * view2pixels );
		_limitY      = (double)imageHeight / ( 2.0 * view2pixels );
	}

	/**
	 * Returns whether the given bounds intersect the view volume.
	 *
	 * @param   transform   Transform from bounds to scene space.
	 * @param   bounds      Bounds to be checked.
	 *
	 * @return  <code>true</code> if the bounds intersect the view volume.
	 */
	public boolean inViewVolume( final Matrix3D transform, final Bounds3D bounds )
	{
		final ViewingFrustum frustum = getViewingFrustum();
		return ( frustum == null ) || frustum.contains( transform, bounds );
	}

	/**
	 * Returns the viewing frustum for this projector.
	 *
	 * @return  Viewing frustum.
	 */
	@Nullable
	public ViewingFrustum getViewingFrustum()
	{
		ViewingFrustum result = _viewingFrustum;
		if ( result == null )
		{
			final Matrix4D projectionMatrix = getProjectionMatrix();
			if ( projectionMatrix != null )
			{
				result = new ViewingFrustum( projectionMatrix );
				_viewingFrustum = result;
			}
		}
		return result;
	}

	/**
	 * This function tests if a face lies within the view volume of this
	 * projector. The view coordinates of the face vertices are specified as a
	 * <code>double</code>-array argument.
	 *
	 * @param   face                Face to test against view volume.
	 * @param   vertexCoordinates   View coordinates of object's vertices.
	 *
	 * @return  <code>true</code> if the face lies completely within the view volume;
	 *          <code>false</code> if the face has no vertices, or lies (partly)
	 *          outside the view volume.
	 *
	 * @see     #inViewVolume(double, double, double)
	 */
	public final boolean inViewVolume( final Face3D face , final double[] vertexCoordinates )
	{
		boolean result = false;

		final int vertexCount = face.getVertexCount();
		if ( vertexCount > 0 )
		{
//			final int[] vertexIndices = face.getVertexIndices();
//			for ( int vertex = 0 ; vertex < vertexCount ; vertex++ )
//			{
//				final int vertexIndex = vertexIndices[ vertex ] * 3;
//
//				final double x = vertexCoordinates[ vertexIndex ];
//				final double y = vertexCoordinates[ vertexIndex + 1 ];
//				final double z = vertexCoordinates[ vertexIndex + 2 ];
//
//				if ( inViewVolume( x , y , z ) )
//				{
					result = true;
//					break;
//				}
//			}
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

		if ( vertices.length / 3 > 0 )
		{
			result = true;

			for ( int i = 0 ; result && ( i < vertices.length ) ; i += 3 )
				result = !inViewVolume( vertices[ i ] , vertices[ i + 1 ] , vertices[ i + 2 ] );
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
	 * This function tests if a point defined in view coordinates lies within
	 * the view volume of this projector.
	 *
	 * @param   point       Point in view coordinates.
	 *
	 * @return  <code>true</code> if the point lies within the view volume;
	 *          <code>false</code> if the point lies outside the view volume.
	 */
	public boolean inViewVolume( final Vector3D point )
	{
		return inViewVolume( point.x, point.y, point.z );
	}

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
	public int[] project( final double[] source , final int[] dest , final int pointCount )
	{
		final int    resultLength = pointCount * 2;

		int[] result = dest;
		if ( ( result == null ) || ( result.length < resultLength ) )
		{
			result = new int[ resultLength ];
		}

		final Point tmp = new Point();

		int sourceIndex = 0;
		int resultIndex = 0 ;

		while ( resultIndex < resultLength )
		{
			project( tmp , source[ sourceIndex++ ] , source[ sourceIndex++ ] , source[ sourceIndex++ ] );
			result[ resultIndex++ ] = tmp.x;
			result[ resultIndex++ ] = tmp.y;
		}

		return result;
	}

	/**
	 * This method projects a 3D point in view coordinates onto the 2D image
	 * (image plate, view plane, screen).
	 *
	 * @param   result  Result target.
	 * @param   point   Point in view coordinate system.
	 */
	public void project( final Point2D result , final Vector3D point )
	{
		project( result, point.x, point.y, point.z );
	}

	/**
	 * This method projects a 3D point in view coordinates onto the 2D image
	 * (image plate, view plane, screen).
	 *
	 * @param   result  Result target.
	 * @param   viewX   X coordinate of point in view coordinate system.
	 * @param   viewY   Y coordinate of point in view coordinate system.
	 * @param   viewZ   Z coordinate of point in view coordinate system.
	 */
	public abstract void project( final Point2D result , final double viewX , final double viewY , final double viewZ );

	/**
	 * Get image width in pixels.
	 *
	 * @return  Image width in pixels.
	 */
	public int getImageWidth()
	{
		return _imageWidth;
	}

	/**
	 * Get image height in pixels.
	 *
	 * @return  Image height in pixels.
	 */
	public int getImageHeight()
	{
		return _imageHeight;
	}

	/**
	 * Get image resolution in meters per pixel.
	 *
	 * @return  Image resolution in meters per pixel.
	 */
	public double getImageResolution()
	{
		return _imageResolution;
	}

	/**
	 * Get view unit in meters per unit.
	 *
	 * @return  View unit in meters per unit (e.g. {@link ab.j3d.model.Scene#MM}).
	 */
	public double getViewUnit()
	{
		return _viewUnit;
	}

	/**
	 * Get front clipping plane distance in view units.
	 *
	 * @return  Front clipping plane distance in view units.
	 */
	public double getFrontClipDistance()
	{
		return _frontClipDistance;
	}

	/**
	 * Get back clipping plane distance in view units.
	 *
	 * @return  Back clipping plane distance in view units.
	 */
	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	/**
	 * Get linear zoom factor.
	 *
	 * @return  Linear zoom factor.
	 */
	public double getZoomFactor()
	{
		return _zoomFactor;
	}

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
	 * Returns a projection matrix for this projection.
	 *
	 * @return  Projection matrix.
	 */
	public abstract Matrix4D getProjectionMatrix();

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
		 * @param   viewUnit            Unit scale factor (e.g. {@link ab.j3d.model.Scene#MM}).
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   fieldOfView         Camera's field of view in radians.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		PerspectiveProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double fieldOfView , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );

			final double view2pixels = _view2pixels;
			final double viewWidth   = (double)imageWidth / view2pixels;

			_eyeDistance = viewWidth / ( 2.0 * Math.tan( fieldOfView / 2.0 ) );
		}

		public Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY )
		{
			final double centerX     = (double)( _imageCenterX );
			final double centerY     = (double)( _imageCenterY );
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double viewX = ( pointerX - centerX ) / view2pixels;
			final double viewY = ( centerY - pointerY ) / view2pixels;

			final double distance = Math.sqrt( viewX * viewX + viewY * viewY + eyeDistance * eyeDistance );

			final double directionX = viewX / distance;
			final double directionY = viewY / distance;
			final double directionZ = -eyeDistance / distance;

			return new BasicRay3D( transform , viewX - eyeDistance * directionX , viewY - eyeDistance * directionY , -eyeDistance - eyeDistance * directionZ , directionX , directionY , directionZ , true );
		}

		public int[] project( final double[] source , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;

			int[] result = dest;
			if ( ( result == null ) || ( result.length < resultLength ) )
			{
				result = new int[ resultLength ];
			}

			final Point tmp = new Point();

			int sourceIndex = 0;
			int resultIndex = 0 ;

			while ( resultIndex < resultLength )
			{
				project( tmp , source[ sourceIndex++ ] , source[ sourceIndex++ ] , source[ sourceIndex++ ] );
				result[ resultIndex++ ] = tmp.x;
				result[ resultIndex++ ] = tmp.y;
			}

			return result;
		}

		public void project( final Point2D result , final double viewX , final double viewY , final double viewZ )
		{
			final double f = _view2pixels / ( 1.0 - ( viewZ + _eyeDistance ) / _eyeDistance );
			final double x = _imageCenterX + f * viewX;
			final double y = _imageCenterY - f * viewY;
			result.setLocation( x, y );
		}

		public Vector3D imageToView( final double imageX , final double imageY , final double viewZ )
		{
			final double centerX     = (double)( _imageCenterX );
			final double centerY     = (double)( _imageCenterY );
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double f = view2pixels / ( 1.0 - ( viewZ + eyeDistance ) / eyeDistance );

			final double viewX = ( imageX - centerX ) / f;
			final double viewY = ( centerY - imageY ) / f;

			return Vector3D.INIT.set( viewX , viewY , viewZ );
		}

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			final boolean result;

			final double depth = -z;
			if ( ( depth >= _frontClipDistance ) && ( depth <= _backClipDistance ) )
			{
				result = true;
//				final double f      = 1.0 - z / _eyeDistance;
//				final double limitX = _limitX * f;
//				final double limitY = _limitY * f;
//
//				result = ( x >= -limitX ) && ( x <= limitX )
//				      && ( y >= -limitY ) && ( y <= limitY );
			}
			else /* outside front/back clipping plane */
			{
				result = false;
			}

			return result;
		}

		/**
		 * Returns the eye distance.
		 *
		 * @return  Distance from the screen to the user's eye(s).
		 */
		public double getEyeDistance()
		{
			return _eyeDistance;
		}

		@Override
		public Matrix4D getProjectionMatrix()
		{
			final double zNear = getFrontClipDistance();
			final double zFar = getBackClipDistance();
			final double aspect = (double)getImageWidth() / (double)getImageHeight();
			final double f = 2.0 * getEyeDistance() * getView2pixels() / (double)getImageHeight();

			return new Matrix4D(
				f / aspect, 0.0, 0.0, 0.0,
				0.0, f, 0.0, 0.0,
				0.0, 0.0, ( zFar + zNear ) / ( zNear - zFar ), 2.0 * zFar * zNear / ( zNear - zFar ),
				0.0, 0.0, -1.0, 0.0
			);
		}
	}

	/**
	 * Parallel projector implementation.
	 * <p />
	 * Parallel projection maps 3D X and Y coordinates linearly to 2D
	 * coordinates. Depth information (Z) is ignored completely. It only scales,
	 * translates, and flips the Y axis direction.
	 */
	static class ParallelProjector
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
		ParallelProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );
		}

		public Ray3D getPointerRay( final Matrix3D transform , final double pointerX , final double pointerY )
		{
			final Vector3D origin    = imageToView( pointerX , pointerY , 0.0 );
			final Vector3D direction = POINTER_DIRECTION;

			return new BasicRay3D( transform , origin , direction , false );
		}

		public void project( final Point2D result , final double viewX , final double viewY , final double viewZ )
		{
			final double x = _imageCenterX + _view2pixels * viewX;
			final double y = _imageCenterY - _view2pixels * viewY;
			result.setLocation( x, y );
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
			final double depth = -z;
			return ( x >= -_limitX ) && ( x <= _limitX )
			    && ( y >= -_limitY ) && ( y <= _limitY )
			    && ( depth >= _frontClipDistance ) && ( depth <= _backClipDistance );
		}

		@Override
		public Matrix4D getProjectionMatrix()
		{
			final double left = -_limitX;
			final double right = _limitX;
			final double bottom = -_limitY;
			final double top = _limitY;
			final double near = _frontClipDistance;
			final double far = _backClipDistance;

			return new Matrix4D(
				2.0 / ( right - left ), 0.0, 0.0, -( right + left ) / ( right - left ),
				0.0, 2.0 / ( top - bottom ), 0.0, -( top + bottom ) / ( top - bottom ),
				0.0, 0.0, -2.0 / ( far - near ), -( far + near ) / ( far - near ),
				0.0, 0.0, 0.0, 1.0
			);
		}
	}

	/**
	 * Isometric projector implementation.
	 * <p />
	 * Isometric projection is a parallel projection method that projects the
	 * view Z-axis onto the rendered X- and Y-axis by using the displacing
	 * points by the half Z-value 30 degrees relative to the X-axis (top-right).
	 */
	static class IsometricProjector
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
		 * @param   viewUnit            Unit scale factor (e.g. {@link ab.j3d.model.Scene#MM}).
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		IsometricProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
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

		public void project( final Point2D result , final double viewX , final double viewY , final double viewZ )
		{
			final double x = _imageCenterX + _view2pixels * ( viewX - viewZ * _xComponentOfZ );
			final double y = _imageCenterY - _view2pixels * ( viewY - viewZ * _yComponentOfZ );
			result.setLocation( x, y );
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

			final double depth = -z;
			return ( depth >= _frontClipDistance ) && ( depth <= _backClipDistance )
			    && ( ( tmp = ( x - z * _xComponentOfZ     ) ) >= -_limitX ) && ( tmp <= _limitX )
			    && ( ( tmp = (     z * _yComponentOfZ - y ) ) >= -_limitY ) && ( tmp <= _limitY );
		}

		@Override
		public Matrix4D getProjectionMatrix()
		{
			return null; // TODO: Implement isometric projection matrix.
		}
	}
}
