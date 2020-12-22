/*
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
package ab.j3d.awt.view.java2d;

import java.awt.*;
import java.awt.image.*;

import ab.j3d.*;
import ab.j3d.awt.view.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * Java 2D view implementation.
 *
 * @author G.B.M. Rupert
 */
public class Java2dView
extends OffscreenView3D
{
	/**
	 * Component through which a rendering of the view is shown.
	 */
	@NotNull
	private final Java2dViewComponent _viewComponent;

	/**
	 * The SceneInputTranslator for this View.
	 */
	@NotNull
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
	 * Binary Space Partitioning Tree ({@link BSPTree}) of the scene.
	 *
	 * <p>The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 */
	@NotNull
	private final BSPTree _bspTree;

	/**
	 * This internal flag is set to indicate that the scene is
	 * changed, so the {@link BSPTree} needs to be re-calculated.
	 */
	private boolean _bspTreeDirty;

	/**
	 * Construct new view.
	 *
	 * @param scene      Scene to view.
	 * @param background Background color to use for 3D views. May be
	 *                   {@code null}, in which case the default
	 *                   background color of the current look and feel is
	 *                   used.
	 */
	public Java2dView( final @NotNull Scene scene, final @Nullable Color background )
	{
		super( scene );

		_frontClipDistance = 0.1 / scene.getUnit();
		_backClipDistance = 100.0 / scene.getUnit();

		/*
		 * Create view component.
		 */
		final Java2dViewComponent viewComponent = new Java2dViewComponent( this );
		viewComponent.setOpaque( true );
		if ( background != null )
		{
			viewComponent.setBackground( background );
		}
		_viewComponent = viewComponent;

		final ViewControlInput controlInput = new ViewControlInput( this );
		_controlInput = controlInput;

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		controlInput.addControlInputListener( defaultViewControl );
		addOverlay( defaultViewControl );

		_bspTree = new BSPTree();
		_bspTreeDirty = true;

		update();
	}

	@Override
	public void setBackground( @NotNull final Background background )
	{
		final Color4 color = background.getColor();
		_viewComponent.setBackground( new Color( color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat() ) );
	}

	@Override
	public double getFrontClipDistance()
	{
		return _frontClipDistance;
	}

	@Override
	public void setFrontClipDistance( final double front )
	{
		_frontClipDistance = front;
		update();
	}

	@Override
	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	@Override
	public void setBackClipDistance( final double back )
	{
		_backClipDistance = back;
		update();
	}

	@Override
	public Component getComponent()
	{
		return _viewComponent;
	}

	@Override
	public void contentNodeAdded( final @NotNull SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeAdded( event );
	}

	@Override
	public void contentNodeContentUpdated( final @NotNull SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeContentUpdated( event );
	}

	@Override
	public void contentNodePropertyChanged( final @NotNull SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodePropertyChanged( event );
	}

	@Override
	public void contentNodeRemoved( final @NotNull SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeRemoved( event );
	}

	@Override
	public void update()
	{
		_viewComponent.repaint();
	}

	@NotNull
	public BSPTree getBspTree()
	{
		final BSPTree result = _bspTree;

		if ( _bspTreeDirty )
		{
			result.reset();
			result.addScene( getScene() );
			result.build();

			_bspTreeDirty = false;
		}

		return result;
	}

	@Override
	public Projector getProjector()
	{
		final Java2dViewComponent viewComponent = _viewComponent;
		final Insets insets = viewComponent.getInsets( null );
		final int imageWidth = viewComponent.getWidth() - insets.left - insets.right;
		final int imageHeight = viewComponent.getHeight() - insets.top - insets.bottom;

		final Scene scene = getScene();
		final double viewUnit = scene.getUnit();

		final double fieldOfView = getFieldOfView();
		final double zoomFactor = getZoomFactor();
		final double frontClipDistance = _frontClipDistance;
		final double backClipDistance = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy(), imageWidth, imageHeight, getResolution(), viewUnit, frontClipDistance, backClipDistance, fieldOfView, zoomFactor );
	}

	@Override
	protected void paintOverlay( @NotNull final Graphics2D g2d )
	{
		super.paintOverlay( g2d );
	}

	@Override
	public ViewControlInput getControlInput()
	{
		return _controlInput;
	}

	@Override
	public void setSize( final int width, final int height )
	{
		_viewComponent.setSize( width, height );
	}

	@Override
	public BufferedImage renderImage( final int width, final int height )
	{
		_viewComponent.setSize( width, height );
		final BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = result.createGraphics();
		_viewComponent.paintComponent( g2d );
		g2d.dispose();
		return result;
	}
}
