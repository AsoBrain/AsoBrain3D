/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.JComponent;

import ab.j3d.Matrix3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.DragSupport;
import ab.j3d.view.ViewControl;
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
public final class Java2dView
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
	 * @see     #setRenderingPolicy
	 */
	private int _renderingPolicy;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final ViewComponent _viewComponent;

	/**
	 * Simple mounting view panel to show a 3D perspective rendering of the
	 * selected/active mounting.
	 */
	private class ViewComponent
		extends JComponent
	{
		/**
		 * Insets cache.
		 */
		private Insets _insets;

		private ViewComponent()
		{
			final Color originalBackground = getBackground();
			setDoubleBuffered( true );
			setBackground( ( originalBackground == null ) ? new Color( 51 , 77 , 102 ) : originalBackground.brighter() );

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

		public void paintComponent( final Graphics g )
		{
			super.paintComponent( g );

			final Insets insets = getInsets( _insets );
			final int    width  = getWidth() - insets.left - insets.right;
			final int    height = getHeight() - insets.top - insets.bottom;

			g.setColor( getBackground() );
			g.fillRect( insets.left , insets.top , width , height );

			final Java2dModel      model            = _model;
			final Node3DCollection paintQueue       = model.getPaintQueue();
			final Matrix3D         viewTransform    = getViewTransform();
			final int              projectionPolicy = _projectionPolicy;
			final boolean          hasPerspective   = ( projectionPolicy != PARALLEL );
			final double           scale            = 1000.0 / (double)Math.max( width , height );

			final Matrix3D projectionTransform = Matrix3D.INIT.set(
				 scale ,    0.0 , 0.0 , (double)width  / 2.0 ,
				   0.0 , -scale , 0.0 , (double)height / 2.0 ,
				   0.0 ,    0.0 , 0.0 , 0.0 );

			final PolygonRenderer renderer = new PolygonRenderer( projectionTransform , viewTransform , hasPerspective );
			for ( int i = 0 ; i < paintQueue.size() ; i++ )
			{
				final Matrix3D matrix = paintQueue.getMatrix( i );
				final Object3D object = (Object3D)paintQueue.getNode( i );

				if ( object != null )
					renderer.add( matrix.multiply( viewTransform ) , object , false );
			}

			final Graphics2D g2d = (Graphics2D)g.create( insets.left , insets.top , width , height );

			final boolean fill;
			final boolean outline;
			final boolean useTextures;

			switch ( _renderingPolicy )
			{
					case SOLID     : fill = true;  outline = false; useTextures = true;  break;
					case SCHEMATIC : fill = true;  outline = true;  useTextures = false; break;
					case SKETCH    : fill = true;  outline = false; useTextures = false; break;
					case WIREFRAME : fill = false; outline = true;  useTextures = false; break;
					default        : fill = false; outline = false; useTextures = false; break;
			}

			renderer.paint( g2d , fill , outline , useTextures );

//			final RenderingHints renderingHints = g2d.getRenderingHints();
//			final Object oldAntiAliasing = renderingHints.get( RenderingHints.KEY_ANTIALIASING );
//			renderingHints.put( RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_ON );
//			g2d.setRenderingHints( renderingHints );
//
//			renderer.paint( g2d , false , true );
//
//			renderingHints.put( RenderingHints.KEY_ANTIALIASING , oldAntiAliasing );
//			g2d.setRenderingHints( renderingHints ); /* <= is this really needed if we 'dispose()' below? */

			g2d.dispose();
			_insets = insets;
		}
	}


	/**
	 * Construct new view.
	 *
	 * @param   model           Model for which this view is created.
	 * @param   id              Application-assigned ID of this view.
	 * @param   viewControl     Control to use for this view.
	 */
	Java2dView( final Java2dModel model , final Object id , final ViewControl viewControl )
	{
		super( id , viewControl );

		_model = model;

		_projectionPolicy = PERSPECTIVE;
		_renderingPolicy  = SCHEMATIC;

		/*
		 * Create view component.
		 */
		final ViewComponent viewComponent = new ViewComponent();
		_viewComponent = viewComponent;

		/*
		 * Update view to initial transform.
		 */
		update();

		/*
		 * Add DragSupport to handle drag events.
		 */
		final DragSupport ds = new DragSupport( viewComponent , 0.001 );
		ds.addDragListener( viewControl );
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
		_projectionPolicy = policy;
	}

	public void setRenderingPolicy( final int policy )
	{
		_renderingPolicy = policy;
	}
}
