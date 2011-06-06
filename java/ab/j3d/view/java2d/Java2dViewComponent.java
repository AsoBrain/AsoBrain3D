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
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

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
	 * View that is rendered.
	 */
	private final Java2dView _view;

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
		final Java2dView view = _view;
		final Scene scene = view.getScene();

		/* Setup initial style and apply style filters to this view. */
		final RenderStyle defaultStyle = new RenderStyle();
		final Collection<RenderStyleFilter> styleFilters = view.getRenderStyleFilters();
		final RenderStyle viewStyle = defaultStyle.applyFilters( styleFilters , view );

		final Map<Node3D,RenderStyle> nodeStyles = new HashMap<Node3D, RenderStyle>( );
		scene.walk( new Node3DVisitor()
		{
			@Override
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				final Node3D node = path.getNode();

				final Node3DPath parentPath = path.getParent();
				final RenderStyle parentStyle = ( parentPath != null ) ? nodeStyles.get( parentPath.getNode() ) : viewStyle;

				final RenderStyle nodeStyle = parentStyle.applyFilters( styleFilters, node );
				nodeStyles.put( node, nodeStyle );
				return true;
			}
		} );

		final Matrix3D view2scene = view.getView2Scene();
		final Vector3D viewPoint  = view2scene.getTranslation();
		final Matrix3D scene2view = view.getScene2View();
		final Projector projector = view.getProjector();

		final Insets insets = getInsets();
		final int componentWidth = getWidth();
		final int imageWidth = componentWidth - insets.left - insets.right;
		final int componentHeight = getHeight();
		final int imageHeight = componentHeight - insets.top - insets.bottom;

		final BSPTree bspTree = scene.getBspTree();
		final RenderedPolygon[] renderQueue = bspTree.getRenderQueue( viewPoint, projector, scene2view, viewStyle.isBackfaceCullingEnabled(), true );

		if ( isOpaque() )
		{
			g.setColor( getBackground() );
			g.fillRect( 0, 0, componentWidth, componentHeight );
		}

		final Graphics2D g2d = (Graphics2D)g.create( insets.left, insets.top, imageWidth, imageHeight );

		for ( final RenderedPolygon polygon : renderQueue )
		{
			final RenderStyle renderStyle = nodeStyles.get( polygon._object );
			if ( renderStyle != null )
			{
				paintPolygon( g2d, polygon, renderStyle );
			}
		}

		view.paintOverlay( g2d );

		g2d.dispose();
	}

	/**
	 * Paint the specified polygon.
	 *
	 * @param   g               Graphics context to paint on.
	 * @param   polygon         Polygon to paint.
	 * @param   renderStyle     Render style to use.
	 */
	private static void paintPolygon( final Graphics2D g, final RenderedPolygon polygon, final RenderStyle renderStyle )
	{
		final Object antiAliasingValue = g.getRenderingHint( RenderingHints.KEY_ANTIALIASING );

		final boolean fill = renderStyle.isMaterialEnabled() || renderStyle.isFillEnabled();

		Color fillPaint;
		if ( fill )
		{
			final Appearance appearance = polygon._appearance;

			if ( renderStyle.isMaterialEnabled() && ( appearance != null ) )
			{
				fillPaint = new Color( appearance.getDiffuseColorRed(), appearance.getDiffuseColorGreen(), appearance.getDiffuseColorBlue(), appearance.getDiffuseColorAlpha() );
			}
			else
			{
				fillPaint = renderStyle.getFillColor();
			}

			if ( fillPaint != null )
			{
				if ( ( renderStyle.isMaterialEnabled() ? renderStyle.isMaterialLightingEnabled() : renderStyle.isFillLightingEnabled() ) )
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

				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
				g.setPaint( fillPaint );
				g.fill( polygon );
			}
		}

		if ( renderStyle.isStrokeEnabled() )
		{
			final Color outlineColor = renderStyle.getStrokeColor();
			if ( outlineColor != null )
			{
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				g.setPaint( outlineColor );
				g.draw( polygon );
			}
		}

		g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antiAliasingValue );
	}
}
