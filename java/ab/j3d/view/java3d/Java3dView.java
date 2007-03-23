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
package ab.j3d.view.java3d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.J3DGraphics2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.swing.Action;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInput;
import ab.j3d.view.Projector;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelView;

/**
 * Java 3D implementation of view model view.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
final class Java3dView
	extends ViewModelView
{
	/**
	 * The hardcoded resolution (meters per pixel) used by Java 3D.
	 *
	 * @see     javax.media.j3d.Screen3D#METERS_PER_PIXEL
	 */
	private static final double JAVA3D_RESOLUTION = ViewModel.INCH / 90.0;

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
	private final View _view;

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
	private final ControlInput _controlInput;

	/**
	 * The component that displays the rendered scene. This class subclasses
	 * {@link Canvas3D}, but it overrides some functions to allow
	 * {@link ab.j3d.view.OverlayPainter}s to paint on top of the rendered
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
			final View     view            = _view;
			final double   aperture        = getAperture();
			final double   imageResolution = getResolution();
			final double   zoomFactor      = getZoomFactor();

			view.setFieldOfView( aperture );

			final double scale = ( JAVA3D_RESOLUTION / imageResolution ) * zoomFactor;
			view.setScreenScale( scale );

			super.paint( g );
		}

		/**
		 * Called after the rendering has been completed. This method makes sure
		 * {@link ab.j3d.view.OverlayPainter}s get to paint over the rendered
		 * scene.
		 */
		public void postRender()
		{
			BufferedImage overlayBufferImage = null;

			if ( hasOverlayPainters() )
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
	 * @param   model       {@link Java3dModel} for which this class is a view.
	 * @param   universe    Java3D universe for which the view is created.
	 * @param   id          Application-assigned ID of this view.
	 *
	 * @see     Java3dUniverse#createView
	 */
	Java3dView( final Java3dModel model, final Java3dUniverse universe , final Object id )
	{
		super( model.getUnit() , id );

		/*
		 * Create view branch.
		 */
		final TransformGroup tg     = new TransformGroup();
		final Canvas3D       canvas = new ViewComponent();
		final View           view   = universe.createView( tg , canvas );

		view.setScreenScalePolicy( View.SCALE_EXPLICIT );
		view.setWindowResizePolicy( View.VIRTUAL_WORLD );

		_tg     = tg;
		_canvas = canvas;
		_view   = view;

		/*
		 * Update view to initial transform.
		 */
		update();

		_controlInput = new ViewControlInput( model , this );
	}

	public Component getComponent()
	{
		return _canvas;
	}

	public void setProjectionPolicy( final int policy )
	{
		final View view = _view;

		switch ( policy )
		{
			case Projector.PERSPECTIVE :
				view.setProjectionPolicy( View.PERSPECTIVE_PROJECTION );
				break;

			case Projector.ISOMETRIC :
			case Projector.PARALLEL :
				view.setProjectionPolicy( View.PARALLEL_PROJECTION );
				break;

			default :
				throw new IllegalArgumentException( "Invalid projection policy: " + policy );
		}
	}

	public void update()
	{
		final Matrix3D viewTransform = getViewTransform();
		final double   unit          = getUnit();

		/*
		 * Determine rotation and translation. If a unit is set, use it to
		 * scale the translation.
		 */
		double xo = viewTransform.xo;
		double yo = viewTransform.yo;
		double zo = viewTransform.zo;

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

		translation.x = - xo * ( rotation.m00 = viewTransform.xx )
		                - yo * ( rotation.m01 = viewTransform.yx )
		                - zo * ( rotation.m02 = viewTransform.zx );

		translation.y = - xo * ( rotation.m10 = viewTransform.xy )
		                - yo * ( rotation.m11 = viewTransform.yy )
		                - zo * ( rotation.m12 = viewTransform.zy );

		translation.z = - xo * ( rotation.m20 = viewTransform.xz )
		                - yo * ( rotation.m21 = viewTransform.yz )
		                - zo * ( rotation.m22 = viewTransform.zz );

		transform3d.set( rotation , translation , 1.0 );
		tg.setTransform( transform3d );
	}

	public void setRenderingPolicy( final int policy )
	{
		/* @FIXME how can we implement such a feature? I think this really requires different geometry! Maybe something with 'alternate appearance' helps a little? */
	}

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public Projector getProjector()
	{
		final View view = _view;
		final Canvas3D canvas = _canvas;

		final int policy = view.getProjectionPolicy() == View.PARALLEL_PROJECTION ? Projector.PARALLEL : Projector.PERSPECTIVE;

		final int    width      = canvas.getWidth();
		final int    height     = canvas.getHeight();
		final double resolution = getResolution();
		final double unit       = getUnit();
		final double frontClip  = view.getFrontClipDistance() / unit;
		final double backClip   = view.getBackClipDistance()  / unit;
		final double aperture   = getAperture();
		final double zoomFactor = getZoomFactor();

		return Projector.createInstance( policy , width , height , resolution , unit , frontClip , backClip , aperture , zoomFactor );
	}

	protected ControlInput getControlInput()
	{
		return _controlInput;
	}

	public Action[] getActions( final Locale locale )
	{
		return new Action[ 0 ];
	}
}
