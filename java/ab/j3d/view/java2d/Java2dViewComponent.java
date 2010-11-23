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
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * UI component that renders the view.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
final class Java2dViewComponent
	extends JComponent
{
	/**
	 * Practical minimum size of images in dialog.
	 */
	private static final int MINIMUM_IMAGE_SIZE = 150;

	/**
	 * Stroke to use for sketched rendering.
	 */
	private static final BasicStroke SKETCH_STROKE = new BasicStroke( 0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );

	/**
	 * View that is rendered.
	 */
	private Java2dView _view;

	/**
	 * Construct view component.
	 *
	 * @param   view    View that is rendered.
	 */
	Java2dViewComponent( final Java2dView view )
	{
		_view = view;
		setDoubleBuffered( true );
	}

	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension( MINIMUM_IMAGE_SIZE, MINIMUM_IMAGE_SIZE );
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension( MINIMUM_IMAGE_SIZE, MINIMUM_IMAGE_SIZE );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		if ( isOpaque() )
		{
			g.setColor( getBackground() );
			g.fillRect( 0, 0, getWidth(), getHeight() );
		}

		final Java2dView view = _view;
		final Scene scene = view.getScene();
		final BSPTree bspTree = scene.getBspTree();
		final Projector projector = view.getProjector();
		final Matrix3D model2view = view.getScene2View();

		final Insets insets = getInsets();
		final int imageWidth = getWidth() - insets.left - insets.right;
		final int imageHeight = getHeight() - insets.top - insets.bottom;

		final boolean fill;
		final boolean outline;
		final boolean useTextures;
		final boolean backfaceCulling;
		final boolean applyLighting;

		final RenderingPolicy renderingPolicy = view.getRenderingPolicy();
		switch ( renderingPolicy )
		{
				case SOLID     : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
				case SCHEMATIC : fill = true;  outline = true;  useTextures = false; backfaceCulling = true;  applyLighting = false; break;
				case SKETCH    : fill = true;  outline = false; useTextures = true;  backfaceCulling = true;  applyLighting = true;  break;
				case WIREFRAME : fill = false; outline = true;  useTextures = false; backfaceCulling = false; applyLighting = false; break;
				default        : fill = false; outline = false; useTextures = false; backfaceCulling = false; applyLighting = true;  break;
		}

		final Matrix3D view2model = view.getView2Scene();
		final Vector3D viewPoint  = Vector3D.INIT.set( view2model.xo, view2model.yo, view2model.zo );
		final RenderedPolygon[] renderQueue = bspTree.getRenderQueue( viewPoint, projector, model2view, backfaceCulling, true );

		final Graphics2D g2d = (Graphics2D)g.create( insets.left, insets.top, imageWidth, imageHeight );
		paintQueue( g2d, renderQueue, outline, fill, applyLighting, useTextures );

		if ( renderingPolicy == RenderingPolicy.SKETCH )
		{
			g2d.setStroke( SKETCH_STROKE );
			paintQueue( g2d, renderQueue, true, false, false, false );
		}

		view.paintOverlay( g2d );

		g2d.dispose();
	}

	/**
	 * Paint all specified polygons.
	 *
	 * @param   g                   Graphics context to paint on.
	 * @param   polygons            Polygons to paint.
	 * @param   outline             Paint polygon outlines.
	 * @param   fill                Fill polygons (vs. outline only).
	 * @param   applyLighting       Apply lighting effect to filled polygons.
	 * @param   useMaterialColor    Try to apply material properties when filling polygons.
	 */
	public void paintQueue( final Graphics2D g, final RenderedPolygon[] polygons, final boolean outline, final boolean fill, final boolean applyLighting, final boolean useMaterialColor )
	{
		for ( final RenderedPolygon polygon : polygons )
		{
			paintPolygon( g, polygon, outline, fill, applyLighting, useMaterialColor );
		}
	}

	/**
	 * Paint the specified polygon.
	 *
	 * @param   g                   Graphics context to paint on.
	 * @param   polygon             Polygon to paint.
	 * @param   outline             Paint polygon outlines.
	 * @param   fill                Fill polygons (vs. outline only).
	 * @param   applyLighting       Apply lighting effect to filled polygons.
	 * @param   useMaterialColor    Try to apply material properties when filling polygons.
	 */
	public void paintPolygon( final Graphics2D g, final RenderedPolygon polygon, final boolean outline, final boolean fill, final boolean applyLighting, final boolean useMaterialColor )
	{
		final Object antiAliasingValue = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );

		Color fillPaint = null;
		if ( fill || ( polygon._vertexCount < 3 ) )
		{
			final Material material = polygon._material;

			if ( polygon._alternateAppearance )
			{
				fillPaint = polygon._object.alternateFillColor;
			}
			else if ( useMaterialColor && ( material != null ) && ( material.colorMap == null ) )
			{
				fillPaint = new Color( material.getARGB() );
			}
			else
			{
				fillPaint = polygon._object.fillColor;
			}

			if ( fill && applyLighting )
			{
				final float shadeFactor = 0.5f;

				final float factor = Math.min( 1.0f, ( 1.0f - shadeFactor ) + shadeFactor * Math.abs( (float)polygon._planeNormalZ ) );
				if ( factor < 1.0f )
				{
					final Color color = fillPaint;
					final float[] rgb = color.getRGBComponents( null );

					fillPaint = new Color( factor * rgb[ 0 ], factor * rgb[ 1 ], factor * rgb[ 2 ], rgb[ 3 ] );
				}
			}

			if ( fillPaint != null )
			{
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
				g.setPaint( fillPaint );
				g.fill( polygon );
			}
		}

		if ( outline || ( fillPaint == null ) )
		{
			final Color outlineColor = ( fillPaint != null ) ? Color.DARK_GRAY : polygon._alternateAppearance ? polygon._object.alternateOutlineColor : polygon._object.outlineColor;
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g.setPaint( outlineColor );
			g.draw( polygon );
		}

		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antiAliasingValue );
	}

}
