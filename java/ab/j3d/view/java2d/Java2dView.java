/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
public final class Java2dView
	extends View3D
{
	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final Java2dViewComponent _viewComponent;

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

		_controlInput = new ViewControlInput( this );

		final DefaultViewControl defaultViewControl = new DefaultViewControl();
		appendControl( defaultViewControl );
		addOverlay( defaultViewControl );

		update();
	}

	@Override
	public void setBackground( @NotNull final Background background )
	{
		_viewComponent.setBackground( background.getColor() );
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
	public void update()
	{
		_viewComponent.repaint();
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
		final Insets insets = viewComponent.getInsets( _insetsCache );
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
	protected ViewControlInput getControlInput()
	{
		return _controlInput;
	}
}
