/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;
import ab.j3d.Vector3D;

import com.numdata.oss.ArrayTools;

/**
 * A projector defines an abstract method to project 3D points on to a 2D
 * surface (image plate, view plane, screen). Implementations of this class
 * provide projection methods.
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
	protected double _limitX;

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
	protected double _limitY;

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
	}

	/**
	 * This function tests if a face lies within the view volume of this
	 * projector. The view coordinates of the face vertices are specified as a
	 * <code>double</code>-array argument.
	 *
	 * @param   face            Face to test against view volume.
	 * @param   pointCoords     View coordinates of object's points.
	 *
	 * @return  <code>true</code> if the face lies completely within the view volume;
	 *          <code>false</code> if the face has no vertices, or lies (partly)
	 *          outside the view volume.
	 *
	 * @see     #inViewVolume(double, double, double)
	 * @see     Face3D#getPointIndices
	 * @see     Object3D#getPointCoords
	 */
	public final boolean inViewVolume( final Face3D face , final double[] pointCoords )
	{
		boolean result = false;

		final int[] pointIndices = face.getPointIndices();
		final int   vertexCount   = pointIndices.length;

		if ( vertexCount > 0 )
		{
			result = true;

			for ( int vertexIndex = 0 ; vertexIndex < vertexCount ; vertexIndex++ )
			{
				final int pointIndex = pointIndices[ vertexIndex ] * 3;

				if ( !inViewVolume( pointCoords[ pointIndex  ] , pointCoords[ pointIndex + 1 ] , pointCoords[ pointIndex + 2 ] ) )
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

	public abstract Vector3D screenToWorld( int x , int y, double distance );

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
		 * Scale factor from view (plate) coordinates to pixels.
		 */
		protected final double _view2pixels;

		/**
		 * Eye distance in view coordinates.
		 */
		protected final double _eyeDistance;

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

			final double view2pixels = zoomFactor * viewUnit / imageResolution;
			final double viewWidth   = (double)imageWidth / view2pixels;

			_eyeDistance = viewWidth / ( 2.0 * Math.tan( fieldOfView / 2.0 ) );
			_view2pixels = view2pixels;

			_limitX =  (double)imageWidth  / ( 2.0 * view2pixels );
			_limitY =  (double)imageHeight / ( 2.0 * view2pixels );
		}

		public int[] project( final double[] source , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;
			final int[]  result       = (int[])ArrayTools.ensureLength( dest , int.class , -1 , resultLength );

			final int    centerX     = _imageWidth >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			for ( int sourceIndex = 0 , resultIndex = 0 ; resultIndex < resultLength ; sourceIndex += 3 , resultIndex +=2 )
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

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			final boolean result;

			if ( ( z >= _backClipDistance ) && ( z <= _frontClipDistance ) )
			{
				final double f      = 1.0 - z / _eyeDistance;
				final double limitX = _limitX * f;
				final double limitY = _limitY * f;

				result = ( x >= -limitX ) && ( x <= limitX )
				      && ( y >= -limitY ) && ( y <= limitY );
			}
			else /* outside front/back clipping plane */
			{
				result = false;
			}

			return result;
		}

		public Vector3D screenToWorld(int x, int y, double distance){
			final int    centerX     = _imageWidth >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;
			final double eyeDistance = _eyeDistance;

			final double f = view2pixels / ( 1.0 - ( distance + eyeDistance ) / eyeDistance );

			double worldX = (x - centerX - 0.5) / f;
			double worldY = (-y + centerY - 0.5) / f;
			double worldZ = distance;

			return Vector3D.INIT.set( worldX, worldY, worldZ);
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
		 * Scale factor from view coordinates to pixels.
		 */
		protected final double _view2pixels;

		/**
		 * Construct parallel projector.
		 *
		 * @param   imageWidth          Image width in pixels.
		 * @param   imageHeight         Image height in pixels.
		 * @param   imageResolution     Image resolution in meters per pixel.
		 * @param   viewUnit            Unit scale factor (e.g. {@link ViewModel#MM}).
		 * @param   frontClipDistance   Front clipping plane distance in view units.
		 * @param   backClipDistance    Back clipping plane distance in view units.
		 * @param   zoomFactor          Linear zoom factor.
		 */
		public ParallelProjector( final int imageWidth , final int imageHeight , final double imageResolution , final double viewUnit , final double frontClipDistance , final double backClipDistance , final double zoomFactor )
		{
			super( imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , zoomFactor );

			final double view2pixels = zoomFactor * 0.5 * (double)imageWidth * viewUnit;

			_view2pixels = view2pixels;
			_limitX      = view2pixels * 0.5 * (double)_imageWidth;
			_limitY      = view2pixels * 0.5 * (double)_imageHeight;
		}

		public int[] project( final double[] source , final int[] dest , final int pointCount )
		{
			final int    resultLength = pointCount * 2;
			final int[]  result       = (int[])ArrayTools.ensureLength( dest , int.class , -1 , resultLength );

			final int    centerX     = _imageWidth >> 1;
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

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			return ( x >= -_limitX ) && ( x <= _limitX )
			    && ( y >= -_limitY ) && ( y <= _limitY )
			    && ( z >= _backClipDistance ) && ( z <= _frontClipDistance );
		}

		public Vector3D screenToWorld(int x, int y, double distance){
			final int    centerX     = _imageWidth >> 1;
			final int    centerY     = _imageHeight >> 1;
			final double view2pixels = _view2pixels;

			double worldX = (x - centerX - 0.5) / view2pixels;
			double worldY = (-y + centerY - 0.5) / view2pixels;
			double worldZ = distance;

			return Vector3D.INIT.set( worldX, worldY, worldZ);
		}
	}

	/**
	 * Isometric projector implementation.
	 * <p />
	 * Isometric projection is a parallel projection method that projects the
	 * view Z-axis onto the rendered X- and Y-axis by using the displacing
	 * points by the half Z-value 30 degrees relative to the X-axis (top-right).
	 */
	public static final class IsometricProjector
		extends ParallelProjector
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

			_xComponentOfZ = 0.5 * Math.cos( Math.PI / 6.0 );
			_yComponentOfZ = 0.5 * Math.sin( Math.PI / 6.0 );
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

		public boolean inViewVolume( final double x , final double y , final double z )
		{
			double tmp;

			return ( z >= _backClipDistance ) && ( z <= _frontClipDistance )
			    && ( ( tmp = ( x - z * _xComponentOfZ     ) ) >= -_limitX ) && ( tmp <= _limitX )
			    && ( ( tmp = (     z * _yComponentOfZ - y ) ) >= -_limitY ) && ( tmp <= _limitY );
		}

		public Vector3D screenToWorld(int x, int y, double distance){
			return Vector3D.INIT;
		}
	}
}
