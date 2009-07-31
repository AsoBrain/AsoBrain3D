/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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
package ab.j3d.view;

import java.awt.Color;

import ab.j3d.model.ContentNode;
import ab.j3d.model.Object3D;

/**
 * Provides the default rendering style attributes for a view.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ViewStyleFilter
	implements RenderStyleFilter
{
	/**
	 * Last seen node.
	 */
	private ContentNode _node;

	/**
	 * Construct filter.
	 */
	public ViewStyleFilter()
	{
		_node = null;
	}

	public RenderStyle applyFilter( final RenderStyle style , final Object context )
	{
		RenderStyle result = style;

		if ( context instanceof View3D )
		{
			result = createStyle( ((View3D)context).getRenderingPolicy() );
		}
		else if ( context instanceof ContentNode )
		{
			_node = (ContentNode)context;
		}
		else if ( context instanceof Object3D )
		{
			final boolean noFillColor = ( result.getFillColor() == null );
			final boolean noStrokeColor = ( result.getStrokeColor() == null );

			if ( noFillColor || noStrokeColor )
			{
				final ContentNode node      = _node;
				final boolean       alternate = node.isAlternate();
				final Object3D      object3D  = (Object3D)context;

				result = result.clone();

				if ( noFillColor )
				{
					result.setFillColor( alternate ? object3D.alternateFillColor : object3D.fillColor );
				}

				if ( noStrokeColor )
				{
					result.setStrokeColor( alternate ? object3D.alternateOutlineColor : object3D.outlineColor );
				}
			}
		}

		return result;
	}

	/**
	 * Create style based on rendering policy.
	 *
	 * @param   renderingPolicy     Rendering policy.
	 *
	 * @return  Render style.
	 */
	public static RenderStyle createStyle( final RenderingPolicy renderingPolicy )
	{
		final RenderStyle result;

		switch ( renderingPolicy )
		{
			case SCHEMATIC:
				result = new RenderStyle();
				result.setMaterialEnabled( false );
				result.setMaterialLightingEnabled( false );
				result.setFillEnabled( true );
				result.setFillColor( Color.WHITE );
				result.setFillLightingEnabled( false );
				result.setStrokeEnabled( true );
				result.setStrokeColor( Color.BLACK );
				break;

			case SKETCH:
				result = new RenderStyle();
				result.setMaterialEnabled( true );
				result.setMaterialAlpha( 0.5f );
				result.setStrokeEnabled( true );
				result.setStrokeColor( Color.BLACK );
				result.setStrokeWidth( 2.5f );
				break;

			case SOLID:
				result = new RenderStyle();
				break;

			case WIREFRAME:
				result = new RenderStyle();
				result.setMaterialEnabled( false );
				result.setStrokeEnabled( true );
				result.setStrokeColor( Color.BLACK );
				result.setStrokeWidth( 1.5f );
				result.setBackfaceCullingEnabled( false );
				break;

			default :
				throw new AssertionError();
		}

		return result;
	}
}
