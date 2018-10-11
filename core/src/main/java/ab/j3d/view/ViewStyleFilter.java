/*
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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

import ab.j3d.*;

/**
 * Provides the default rendering style attributes for a view.
 *
 * @author  Peter S. Heijnen
 */
public class ViewStyleFilter
	implements RenderStyleFilter
{
	@Override
	public RenderStyle applyFilter( final RenderStyle style , final Object context )
	{
		return ( context instanceof View3D ) ? createStyle( ( (View3D)context ).getRenderingPolicy() ) : style;
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
				result.setFillColor( new Color4f( 1.0f, 1.0f, 1.0f )  );
				result.setFillLightingEnabled( false );
				result.setStrokeEnabled( true );
				result.setStrokeColor( new Color4f( 0.0f, 0.0f, 0.0f ) );
				break;

			case SKETCH:
				result = new RenderStyle();
				result.setMaterialEnabled( true );
				result.setStrokeEnabled( true );
				result.setStrokeColor( new Color4f( 0.0f, 0.0f, 0.0f ) );
				result.setStrokeWidth( 2.0f );
				break;

			case SOLID:
				result = new RenderStyle();
				break;

			case WIREFRAME:
				result = new RenderStyle();
				result.setMaterialEnabled( false );
				result.setStrokeEnabled( true );
				result.setStrokeColor( new Color4f( 0.0f, 0.0f, 0.0f ) );
				result.setBackfaceCullingEnabled( false );
				break;

			default :
				throw new AssertionError();
		}

		return result;
	}
}
