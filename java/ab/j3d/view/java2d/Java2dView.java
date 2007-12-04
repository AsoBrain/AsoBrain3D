/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.JComponent;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInput;
import ab.j3d.view.BSPTree;
import ab.j3d.view.Projector;
import ab.j3d.view.RenderedPolygon;
import ab.j3d.view.SwitchRenderingPolicyAction;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelView;

/**
 * Java 2D implementation of view model view.
 *
 * @see     Java2dModel
 * @see     ViewModelView
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
final class Java2dView
	extends ViewModelView
{
	/**
	 * Practical minimum size of images in dialog.
	 */
	private static final int MINIMUM_IMAGE_SIZE = 150;

	/**
	 * Model for which this view was created.
	 */
	private final Java2dModel _model;

	/**
	 * Projection policy of this view.
	 *
	 * @see     #setProjectionPolicy
	 */
	private int _projectionPolicy;

	/**
	 * Rendering policy of this view.
	 *
	 * @see     ViewModelView#setRenderingPolicy
	 */
	private RenderingPolicy _renderingPolicy;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final ViewComponent _viewComponent;

	/**
	 * The SceneInputTranslator for this View.
	 */
	private final ControlInput _controlInput;

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
		 * Insets cache.
		 */
		private Insets _insets;

		/**
		 * Construct view component.
		 */
		private ViewComponent()
		{
			setDoubleBuffered( true );

			_insets = null;
		}

		public Dimension getMinimumSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Dimension getPreferredSize()
		{
			return new Dimension( MINIMUM_IMAGE_SIZE , MINIMUM_IMAGE_SIZE );
		}

		public Projector getProjector ()
		{
			final Insets      insets            = getInsets( _insets );
			final int         imageWidth        = getWidth() - insets.left - insets.right;
			final int         imageHeight       = getHeight() - insets.top - insets.bottom;
			final double      imageResolution   = getResolution();

			final double      viewUnit          = getUnit();

			final int         projectionPolicy  = _projectionPolicy;
			final double      fieldOfView       = getAperture();
			final double      zoomFactor        = getZoomFactor();
			final double      frontClipDistance =   -0.1 / viewUnit;
			final double      backClipDistance  = -100.0 / viewUnit;

			return Projector.createInstance( projectionPolicy , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
		}

		public void paintComponent( final Graphics g )
		{
			if ( isOpaque() )
			{
				g.setColor( getBackground() );
				g.fillRect( 0 , 0 , getWidth() , getHeight() );
			}

			final BSPTree   bspTree    = _model.getBspTree();
			final Projector projector  = getProjector();
			final Matrix3D  model2view = getViewTransform();

			final Insets insets      = getInsets( _insets );
			final int    imageWidth  = getWidth()  - insets.left - insets.right;
			final int    imageHeight = getHeight() - insets.top  - insets.bottom;

			final boolean fill;
			final boolean outline;
			final boolean useTextures;
			final boolean backfaceCulling;
			final boolean applyLighting;

			final RenderingPolicy renderingPolicy = _renderingPolicy;
			switch ( renderingPolicy )
			{
					case SOLID     : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
					case SCHEMATIC : fill = true;  outline = true;  useTextures = false; backfaceCulling = true;  applyLighting = false; break;
					case SKETCH    : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
					case WIREFRAME : fill = false; outline = true;  useTextures = false; backfaceCulling = false; applyLighting = false; break;
					default        : fill = false; outline = false; useTextures = false; backfaceCulling = false; applyLighting = true;  break;
			}

			final Matrix3D view2model = getInverseViewTransform();
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

			_insets = insets;
		}
	}

	/**
	 * Construct new view.
	 *
	 * @param   model       Model for which this view is created.
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 * @param   id          Application-assigned ID of this view.
	 */
	Java2dView( final Java2dModel model , final Color background , final Object id )
	{
		super( model.getUnit() , id );

		_model = model;

		_projectionPolicy = Projector.PERSPECTIVE;
		_renderingPolicy  = RenderingPolicy.SOLID;

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

		/*
		 * Update view to initial transform.
		 */
		update();

		_controlInput = new ViewControlInput( model , this );
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	public void update()
	{
		_viewComponent.repaint();
	}

	public void setProjectionPolicy( final int policy )
	{
		if ( policy != _projectionPolicy )
		{
			_projectionPolicy = policy;
			update();
		}
	}

	public void setRenderingPolicy( final RenderingPolicy policy )
	{
		if ( policy != _renderingPolicy )
		{
			_renderingPolicy = policy;
			update();
		}
	}

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	public Projector getProjector()
	{
		return _viewComponent.getProjector();
	}

	protected ControlInput getControlInput()
	{
		return _controlInput;
	}

	public Action[] getActions( final Locale locale )
	{
		return new Action[] { new SwitchRenderingPolicyAction( locale , this , _renderingPolicy ) };
	}
}