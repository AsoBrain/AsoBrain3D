/* $Id$
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
package ab.j3d.awt.view.java2d;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.control.*;
import org.jetbrains.annotations.*;

/**
 * Java 2D view implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Java2dView
	extends View3D
{
	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final Java2dViewComponent _viewComponent;

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
	 * Binary Space Partitioning Tree ({@link BSPTree}) of the scene.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 */
	private final BSPTree _bspTree;

	/**
	 * This internal flag is set to indicate that the scene is
	 * changed, so the {@link BSPTree} needs to be re-calculated.
	 */
	private boolean _bspTreeDirty;

	/**
	 * Construct new view.
	 *
	 * @param   scene       Scene to view.
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 */
	public Java2dView( final Scene scene, final Color background )
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
	public void setFrontClipDistance( final double frontClipDistance )
	{
		_frontClipDistance = frontClipDistance;
		update();
	}

	@Override
	public double getBackClipDistance()
	{
		return _backClipDistance;
	}

	@Override
	public void setBackClipDistance( final double backClipDistance )
	{
		_backClipDistance = backClipDistance;
		update();
	}

	@Override
	public Component getComponent()
	{
		return _viewComponent;
	}

	@Override
	public void contentNodeAdded( final SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeAdded( event );
	}

	@Override
	public void contentNodeContentUpdated( final SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeContentUpdated( event );
	}

	@Override
	public void contentNodePropertyChanged( final SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodePropertyChanged( event );
	}

	@Override
	public void contentNodeRemoved( final SceneUpdateEvent event )
	{
		_bspTreeDirty = true;
		super.contentNodeRemoved( event );
	}

	@Override
	public void update()
	{
		_viewComponent.repaint();
	}

	/**
	 * Get binary Space Partitioning Tree ({@link BSPTree}) of the scene.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 *
	 * @return  The Binary Space Partitioning Tree of the scene.
	 */
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

	/**
	 * Returns the {@link Projector} for this view.
	 *
	 * @return  the {@link Projector} for this view
	 */
	@Override
	public Projector getProjector()
	{
		final Java2dViewComponent viewComponent = _viewComponent;
		final Insets insets = viewComponent.getInsets( null );
		final int imageWidth = viewComponent.getWidth() - insets.left - insets.right;
		final int imageHeight = viewComponent.getHeight() - insets.top - insets.bottom;
		final double imageResolution = getResolution();

		final Scene scene = getScene();
		final double viewUnit = scene.getUnit();

		final double fieldOfView = getFieldOfView();
		final double zoomFactor = getZoomFactor();
		final double frontClipDistance = _frontClipDistance;
		final double backClipDistance = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy(), imageWidth, imageHeight, imageResolution, viewUnit, frontClipDistance, backClipDistance, fieldOfView, zoomFactor );
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
}
