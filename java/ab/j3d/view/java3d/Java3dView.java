/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2010
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
package ab.j3d.view.java3d;

import java.awt.*;
import java.awt.image.*;
import javax.media.j3d.*;
import javax.media.j3d.Transform3D;
import javax.swing.*;
import javax.vecmath.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.*;

/**
 * Java 3D view implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
final class Java3dView
	extends View3D
{
	/**
	 * The hardcoded resolution (meters per pixel) used by Java 3D.
	 *
	 * @see     javax.media.j3d.Screen3D#METERS_PER_PIXEL
	 */
	private static final double JAVA3D_RESOLUTION = Scene.INCH / 90.0;

	/**
	 * Transform group in view branch.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private final TransformGroup _tg;

	/**
	 * The {@link View} object is what ties all things together that are
	 * needed to create a rendering of the scene.
	 * <p />
	 * Note that the {@link View} object is actually outside the scene
	 * graph; it attaches to a {@link ViewPlatform} in the graph.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private final View _java3dView;

	/**
	 * Canvas with on-screen representation of this view.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private final Canvas3D _canvas;

	/**
	 * Cached {@link Matrix3d} instance (used by {@link #update()}).
	 */
	private final Matrix3d _rotation = new Matrix3d();

	/**
	 * Cached {@link Vector3d} instance (used by {@link #update()}).
	 */
	private final Vector3d _translation = new Vector3d();

	/**
	 * Cached {@link Transform3D} instance (used by {@link #update()}).
	 */
	private final Transform3D _transform3d = new Transform3D();

	/**
	 * The SceneInputTranslator for this View.
	 */
	private final ViewControlInput _controlInput;

	/**
	 * The component that displays the rendered scene. This class subclasses
	 * {@link Canvas3D}, but it overrides some functions to allow
	 * {@link ViewOverlay}s to paint on top of the rendered
	 * scene.
	 */
	private final class ViewComponent
		extends Canvas3D
	{
		/**
		 * Wether or not this overlay is double buffered.
		 */
		private boolean _overlayDoubleBuffered;

		/**
		 * The image used as buffer.
		 */
		private BufferedImage _overlayBufferImage;

		/**
		 * Creates a new view component.
		 */
		private ViewComponent()
		{
			super( Java3dTools.getGraphicsConfiguration() );

			_overlayDoubleBuffered = false;
			_overlayBufferImage    = null;
		}

		/**
		 * Overrides {@link Canvas3D#paint} and sets the field of view and
		 * screen scale before a new frame is rendered.
		 *
		 * @param   g   Graphics context
		 */
		public void paint( final Graphics g )
		{
			final View     java3dView      = _java3dView;
			final double   aperture        = getFieldOfView();
			final double   imageResolution = getResolution();
			final double   zoomFactor      = getZoomFactor();

			java3dView.setFieldOfView( aperture );

			final double scale = ( JAVA3D_RESOLUTION / imageResolution ) * zoomFactor;
			java3dView.setScreenScale( scale );

			super.paint( g );
		}

		/**
		 * Called after the rendering has been completed. This method makes sure
		 * {@link ViewOverlay}s get to paint over the rendered
		 * scene.
		 */
		public void postRender()
		{
			BufferedImage overlayBufferImage = null;

			if ( hasOverlay() )
			{
				if ( _overlayDoubleBuffered )
				{
					final int width  = getWidth();
					final int height = getHeight();

					final Graphics2D g2d;

					overlayBufferImage = _overlayBufferImage;
					if ( ( overlayBufferImage == null ) || ( overlayBufferImage.getWidth() != width ) || ( overlayBufferImage.getHeight() != height ) )
					{
						final GraphicsConfiguration gc = getGraphicsConfiguration();
						overlayBufferImage = gc.createCompatibleImage( width , height , Transparency.BITMASK );
						g2d = (Graphics2D)overlayBufferImage.getGraphics();
					}
					else
					{
						g2d = (Graphics2D)overlayBufferImage.getGraphics();
						g2d.setBackground( new Color( 0 , 0 , 0 , 0 ) );
						g2d.clearRect( 0 , 0 , width , height );
					}

					paintOverlay( g2d );
					g2d.dispose();

					final J3DGraphics2D j3dg2d = getGraphics2D();
					j3dg2d.drawAndFlushImage( overlayBufferImage , 0 , 0 , this );
				}
				else
				{
					final J3DGraphics2D g2d = getGraphics2D();
					paintOverlay( g2d );
					g2d.flush( false );
				}
			}

			_overlayBufferImage = overlayBufferImage;
		}

		/**
		 * Override {@link #getMinimumSize} to allow layout manager to
		 * do its job. Otherwise, this will always return the current size of
		 * the canvas, not allowing it to be reduced in size.
		 *
		 * @return  a dimension object indicating this component's minimum size.
		 */
		public Dimension getMinimumSize()
		{
			return new Dimension( 10 , 10 );
		}
	}

	/**
	 * Construct view node using Java3D for rendering.
	 *
	 * @param   scene       Scene to view.
	 * @param   universe    Java3D universe for which the view is created.
	 *
	 * @see     Java3dUniverse#createView
	 */
	Java3dView( final Scene scene , final Java3dUniverse universe )
	{
		super( scene );

		/* Use heavyweight popups, since Java3D uses a heavyweight canvas (Canvas3D). */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		/*
		 * Create view branch.
		 */
		final TransformGroup tg         = new TransformGroup();
		final Canvas3D       canvas     = new ViewComponent();
		final View           java3dView = universe.createView( tg , canvas );

		java3dView.setScreenScalePolicy( View.SCALE_EXPLICIT );
		java3dView.setWindowResizePolicy( View.VIRTUAL_WORLD );

		_tg = tg;
		_canvas = canvas;
		_java3dView = java3dView;

		_controlInput = new ViewControlInput( this );

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		appendControl( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	public void setBackground( final Color background )
	{
		_canvas.setBackground( background );
	}

	public double getFrontClipDistance()
	{
		return _java3dView.getFrontClipDistance();
	}

	public void setFrontClipDistance( final double front )
	{
		_java3dView.setFrontClipDistance( front );
	}

	public double getBackClipDistance()
	{
		return _java3dView.getBackClipDistance();
	}

	public void setBackClipDistance( final double back )
	{
		_java3dView.setBackClipDistance( back );
	}

	public Component getComponent()
	{
		return _canvas;
	}

	public void setProjectionPolicy( final ProjectionPolicy policy )
	{
		if ( policy != getProjectionPolicy() )
		{
			final View java3dView = _java3dView;

			switch ( policy )
			{
				case PERSPECTIVE :
					java3dView.setProjectionPolicy( View.PERSPECTIVE_PROJECTION );
					break;

				case ISOMETRIC :
				case PARALLEL :
					java3dView.setProjectionPolicy( View.PARALLEL_PROJECTION );
					break;

				default :
					throw new IllegalArgumentException( "Invalid projection policy: " + policy );
			}
		}

		super.setProjectionPolicy( policy );

	}

	public void update()
	{
		final Matrix3D  scene2view = getScene2View();
		final Scene     scene      = getScene();
		final double    unit       = scene.getUnit();

		/*
		 * Determine rotation and translation. If a unit is set, use it to
		 * scale the translation.
		 */
		double xo = scene2view.xo;
		double yo = scene2view.yo;
		double zo = scene2view.zo;

		if ( ( unit > 0.0 ) && ( unit != 1.0 ) )
		{
			xo *= unit;
			yo *= unit;
			zo *= unit;
		}

		/*
		 * Finally, set the 'TransformGroup' transform to the INVERSE matrix.
		 *
		 * Reuse 'Matrix3d', 'Vector3d', and 'Transform3D' objects here to
		 * prevent creation of too many temporary objects
		 * (sorry for the bad readability).
		 */
		final Matrix3d       rotation    = _rotation;
		final Vector3d       translation = _translation;
		final Transform3D    transform3d = _transform3d;
		final TransformGroup tg          = _tg;

		translation.x = - xo * ( rotation.m00 = scene2view.xx )
		                - yo * ( rotation.m01 = scene2view.yx )
		                - zo * ( rotation.m02 = scene2view.zx );

		translation.y = - xo * ( rotation.m10 = scene2view.xy )
		                - yo * ( rotation.m11 = scene2view.yy )
		                - zo * ( rotation.m12 = scene2view.zy );

		translation.z = - xo * ( rotation.m20 = scene2view.xz )
		                - yo * ( rotation.m21 = scene2view.yz )
		                - zo * ( rotation.m22 = scene2view.zz );

		transform3d.set( rotation , translation , 1.0 );
		tg.setTransform( transform3d );
	}

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public Projector getProjector()
	{
		final View java3dview = _java3dView;

		final Canvas3D  canvas     = _canvas;
		final int       width      = canvas.getWidth();
		final int       height     = canvas.getHeight();
		final double    resolution = getResolution();

		final Scene     scene      = getScene();
		final double    unit       = scene.getUnit();

		final double    frontClip  = java3dview.getFrontClipDistance() / unit;
		final double    backClip   = java3dview.getBackClipDistance() / unit;
		final double    aperture   = getFieldOfView();
		final double    zoomFactor = getZoomFactor();

		return Projector.createInstance( getProjectionPolicy() , width , height , resolution , unit , frontClip , backClip , aperture , zoomFactor );
	}

	protected ViewControlInput getControlInput()
	{
		return _controlInput;
	}
}
