/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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
package ab.j3d.view.java2d;

import java.awt.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.*;

/**
 * Java 2D view implementation.
 *
 * @see     Java2dEngine
 * @see     View3D
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dView
	extends View3D
{
	/**
	 * Practical minimum size of images in dialog.
	 */
	private static final int MINIMUM_IMAGE_SIZE = 150;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final ViewComponent _viewComponent;

	/**
	 * Insets cache.
	 */
	private Insets _insetsCache = null;

	/**
	 * The SceneInputTranslator for this View.
	 */
	private final ViewControlInput _controlInput;

	/**
	 * Front clipping plane distance in view units.
	 */
	private double _frontClipDistance;

	/**
	 * Back clipping plane distance in view units.
	 */
	private double _backClipDistance;

	/**
	 * Stroke to use for sketched rendering.
	 */
	private static final BasicStroke SKETCH_STROKE = new BasicStroke( 0.5f , BasicStroke.CAP_BUTT , BasicStroke.JOIN_BEVEL );

	/**
	 * UI component to present view to user.
	 */
	private final class ViewComponent
		extends JComponent
	{
		/**
		 * Construct view component.
		 */
		private ViewComponent()
		{
			setDoubleBuffered( true );

			_insetsCache = null;
		}

		public Dimension getMinimumSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Dimension getPreferredSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public void paintComponent( final Graphics g )
		{
			if ( isOpaque() )
			{
				g.setColor( getBackground() );
				g.fillRect( 0 , 0 , getWidth() , getHeight() );
			}

			final Scene       scene      = getScene();
			final BSPTree     bspTree    = scene.getBspTree();
			final Projector   projector  = getProjector();
			final Matrix3D    model2view = getScene2View();

			final Insets insets      = getInsets( _insetsCache );
			final int    imageWidth  = getWidth()  - insets.left - insets.right;
			final int    imageHeight = getHeight() - insets.top  - insets.bottom;

			final boolean fill;
			final boolean outline;
			final boolean useTextures;
			final boolean backfaceCulling;
			final boolean applyLighting;

			final RenderingPolicy renderingPolicy = getRenderingPolicy();
			switch ( renderingPolicy )
			{
					case SOLID     : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
					case SCHEMATIC : fill = true;  outline = true;  useTextures = false; backfaceCulling = true;  applyLighting = false; break;
					case SKETCH    : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
					case WIREFRAME : fill = false; outline = true;  useTextures = false; backfaceCulling = false; applyLighting = false; break;
					default        : fill = false; outline = false; useTextures = false; backfaceCulling = false; applyLighting = true;  break;
			}

			final Matrix3D view2model = getView2Scene();
			final Vector3D viewPoint  = Vector3D.INIT.set( view2model.xo , view2model.yo , view2model.zo );
			final RenderedPolygon[] renderQueue = bspTree.getRenderQueue( viewPoint , projector , model2view , backfaceCulling , true );

			final Graphics2D g2d = (Graphics2D)g.create( insets.left , insets.top , imageWidth , imageHeight );
			Painter.paintQueue( g2d , renderQueue , outline , fill , applyLighting , useTextures );

			if ( renderingPolicy == RenderingPolicy.SKETCH )
			{
				g2d.setStroke( SKETCH_STROKE );
				Painter.paintQueue( g2d , renderQueue , true , false , false , false );
			}

			paintOverlay( g2d );

			g2d.dispose();

			_insetsCache = insets;
		}
	}

	/**
	 * Construct new view.
	 *
	 * @param   scene       Scene to view.
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 */
	public Java2dView( final Scene scene , final Color background )
	{
		super( scene );

		_frontClipDistance = 0.1;
		_backClipDistance = 100.0;

		/*
		 * Create view component.
		 */
		final ViewComponent viewComponent = new ViewComponent();
		viewComponent.setOpaque( true );
		if ( background != null )
		{
			viewComponent.setBackground( background );
		}
		_viewComponent = viewComponent;

		_controlInput = new ViewControlInput( this );

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		appendControl( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	public void setBackground( final Color background )
	{
		_viewComponent.setBackground( background );
	}

	public double getFrontClipDistance()
	{
		return _frontClipDistance;
	}

	public void setFrontClipDistance( final double frontClipDistance )
	{
		_frontClipDistance = frontClipDistance;
		update();
	}

	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	public void setBackClipDistance( final double backClipDistance )
	{
		_backClipDistance = backClipDistance;
		update();
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	public void update()
	{
		_viewComponent.repaint();
	}

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public Projector getProjector()
	{
		final ViewComponent viewComponent     = _viewComponent;
		final Insets        insets            = viewComponent.getInsets( _insetsCache );
		final int           imageWidth        = viewComponent.getWidth() - insets.left - insets.right;
		final int           imageHeight       = viewComponent.getHeight() - insets.top - insets.bottom;
		final double        imageResolution   = getResolution();

		final Scene         scene             = getScene();
		final double        viewUnit          = scene.getUnit();

		final double        fieldOfView       = getFieldOfView();
		final double        zoomFactor        = getZoomFactor();
		final double        frontClipDistance = _frontClipDistance;
		final double        backClipDistance  = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy() , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
	}

	protected ViewControlInput getControlInput()
	{
		return _controlInput;
	}
}
