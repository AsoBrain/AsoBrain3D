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

import java.awt.*;
import java.util.*;

import ab.j3d.*;

/**
 * Described how things should be rendered.
 *
 * @see     RenderStyleFilter
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderStyle
	implements Cloneable
{
	/**
	 * Flag to indicate that rendering with material is enabled.
	 */
	private boolean _materialEnabled;

	/**
	 * Flag to indicate that lighting should be applied to rendering with
	 * material.
	 */
	private boolean _materialLightingEnabled;

	/**
	 * Override original material with this one. This may be <code>null</code>
	 * to disable override.
	 */
	private Material _materialOverride;

	/**
	 * Extra alpha value for material. This can be used to make the material
	 * (partially) translucent.
	 */
	private float _materialAlpha;

	/**
	 * Flag to indicate that filling is enabled.
	 */
	private boolean _fillEnabled;

	/**
	 * Color to use for filling.
	 */
	private Color _fillColor = null;

	/**
	 * Flag to indicate that lighting should be applied to filling.
	 */
	private boolean _fillLightingEnabled;

	/**
	 * Flag to indicate that stroke (outline) is enabled.
	 */
	private boolean _strokeEnabled;

	/**
	 * Color to use for stroking.
	 */
	private Color _strokeColor;

	/**
	 * Width of strokes.
	 */
	private float _strokeWidth;

	/**
	 * Flag to indicate that lighting should be applied to strokes.
	 */
	private boolean _strokeLightingEnabled;

	/**
	 * Flag to indicate that vertex rendering is enabled.
	 */
	private boolean _vertexEnabled;

	/**
	 * Color to use for vertices.
	 */
	private Color _vertexColor = null;

	/**
	 * Size of vertices.
	 */
	private float _vertexSize;

	/**
	 * Flag to indicate that lighting should be applied to vertices.
	 */
	private boolean _vertexLightingEnabled;

	/**
	 * Flag to indicate that backfaces should be culled.
	 */
	private boolean _backfaceCullingEnabled;

	/**
	 * Construct default style. This is a 'realistic' setting.
	 */
	public RenderStyle()
	{
		_materialEnabled = true;
		_materialLightingEnabled = true;
		_materialOverride = null;
		_materialAlpha = 1.0f;
		_fillEnabled = false;
		_fillLightingEnabled = true;
		_strokeEnabled = false;
		_strokeColor = null;
		_strokeWidth = 1.0f;
		_strokeLightingEnabled = false;
		_vertexEnabled = false;
		_vertexSize = 1.0f;
		_vertexLightingEnabled = false;
		_backfaceCullingEnabled = true;
	}

	/**
	 * Apply filters to this style.
	 *
	 * @param   styleFilters    Style filters to apply.
	 * @param   context         Context object (never <code>null</code>).
	 *
	 * @return  Filtered style.
	 */
	public RenderStyle applyFilters( final Collection<RenderStyleFilter> styleFilters , final Object context )
	{
		RenderStyle result = this;

		for ( final RenderStyleFilter filter : styleFilters )
		{
			result = filter.applyFilter( result , context );
		}

		return result;
	}

	/**
	 * Blend two colors.
	 *
	 * @param   src     Incoming color, blended with the destination color.
	 * @param   dst     Destination color.
	 *
	 * @return  Blended color.
	 */
	public static Color blendColors( final Color src , final Color dst )
	{
		final Color result;

		if ( ( dst != null ) && ( src.getAlpha() < 255 ) )
		{
			final float[] srcRGBA = src.getRGBComponents( null );
			final float[] dstRGBA = dst.getRGBComponents( null );

			final float srcRatio = srcRGBA[ 3 ];
			final float dstRatio = dstRGBA[ 3 ] * ( 1.0f - srcRatio );

			final float alpha = dstRatio + srcRatio;
			final float red   = ( dstRatio * dstRGBA[ 0 ] + srcRatio * srcRGBA[ 0 ] ) / alpha;
			final float green = ( dstRatio * dstRGBA[ 1 ] + srcRatio * srcRGBA[ 1 ] ) / alpha;
			final float blue  = ( dstRatio * dstRGBA[ 2 ] + srcRatio * srcRGBA[ 2 ] ) / alpha;

			result = new Color( red, green, blue, alpha );
		}
		else
		{
			result = src;
		}

		return result;
	}

	public RenderStyle clone()
	{
		try
		{
			return (RenderStyle)super.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			throw new AssertionError( e.getMessage() );
		}
	}

	/**
	 * Check wether rendering with materials is enabled.
	 *
	 * @return  <code>true</code> if rendering with materials is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isMaterialEnabled()
	{
		return _materialEnabled;
	}

	/**
	 * Enable/disable rendering with materials.
	 *
	 * @param   enable  Enable vs. disable rendering with materials.
	 */
	public void setMaterialEnabled( final boolean enable )
	{
		_materialEnabled = enable;
	}

	/**
	 * Check wether lighting is applied to rendering with material.
	 *
	 * @return  <code>true</code> if lighting is applied to rendering with material;
	 *          <code>false</code> otherwise.
	 */
	public boolean isMaterialLightingEnabled()
	{
		return _materialLightingEnabled;
	}

	/**
	 * Enable/disable application of lighting to rendering with material.
	 *
	 * @param   enable  Enable vs. disable of lighting for material rendering.
	 */
	public void setMaterialLightingEnabled( final boolean enable )
	{
		_materialLightingEnabled = enable;
	}

	/**
	 * Get material override. The original material will be replaced with this
	 * one.
	 *
	 * @return  Material override;
	 *          <code>null</code> if disabled.
	 */
	public Material getMaterialOverride()
	{
		return _materialOverride;
	}

	/**
	 * Set material override. The original material will be replaced with this
	 * one. This may be <code>null</code> to disable override.
	 *
	 * @param   material    Material override (<code>null</code> to disable).
	 */
	public void setMaterialOverride( final Material material )
	{
		_materialOverride = material;
	}

	/**
	 * Get extra alpha value for material.
	 *
	 * @return  Extra alpha value for material (0.0 = completely translucent;
	 *          1.0 = use unaltered alpha values from material).
	 * Extra alpha value for material. This can be used to make the material
	 * (partially) translucent.
	 */
	public float getMaterialAlpha()
	{
		return _materialAlpha;
	}

	/**
	 * Set extra alpha value for material. This can be used to make the material
	 * (partially) translucent.
	 *
	 * @param   alpha   Extra alpha value for material (0.0 = completely
	 *                  translucent; 1.0 = use unaltered alpha values from
	 *                  material).
	 */
	public void setMaterialAlpha( final float alpha )
	{
		_materialAlpha = alpha;
	}

	/**
	 * Check wether filling is enabled.
	 *
	 * @return  <code>true</code> if filling is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isFillEnabled()
	{
		return _fillEnabled;
	}

	/**
	 * Enable/disable that filling is enabled.
	 *
	 * @param   enable  Enable vs. disable filling is enabled.
	 */
	public void setFillEnabled( final boolean enable )
	{
		_fillEnabled = enable;
	}

	/**
	 * Get color used for filling.
	 *
	 * @return  Color used for filling;
	 *          <code>null</code> if undefined.
	 */
	public Color getFillColor()
	{
		return _fillColor;
	}

	/**
	 * Set color to use for filling.
	 *
	 * @param   color   Color to use for filling.
	 */
	public void setFillColor( final Color color )
	{
		_fillColor = color;
	}

	/**
	 * Check wether lighting is applied to filling.
	 *
	 * @return  <code>true</code> if lighting is applied to filling;
	 *          <code>false</code> otherwise.
	 */
	public boolean isFillLightingEnabled()
	{
		return _fillLightingEnabled;
	}

	/**
	 * Enable/disable application of lighting to filling.
	 *
	 * @param   enable  Enable vs. disable lighting for filling.
	 */
	public void setFillLightingEnabled( final boolean enable )
	{
		_fillLightingEnabled =  enable;
	}

	/**
	 * Check wether stroke (outline) rendering is enabled.
	 *
	 * @return  <code>true</code> if stroke (outline) rendering is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isStrokeEnabled()
	{
		return _strokeEnabled;
	}

	/**
	 * Enable/disable rendering of strokes (outlines).
	 *
	 * @param   enable  Enable vs. disable rendering of strokes (outline).
	 */
	public void setStrokeEnabled( final boolean enable )
	{
		_strokeEnabled = enable;
	}

	/**
	 * Get color used for strokes.
	 *
	 * @return  Color used for strokes;
	 *          <code>null</code> if undefined.
	 */
	public Color getStrokeColor()
	{
		return _strokeColor;
	}

	/**
	 * Set color to use for strokes.
	 *
	 * @param   color   Color to use for strokes.
	 */
	public void setStrokeColor( final Color color )
	{
		_strokeColor = color;
	}

	/**
	 * Get width of strokes.
	 *
	 * @return  Width of strokes.
	 */
	public float getStrokeWidth()
	{
		return _strokeWidth;
	}

	/**
	 * Set width of strokes.
	 *
	 * @param   width   Width of strokes.
	 */
	public void setStrokeWidth( final float width )
	{
		_strokeWidth = width;
	}

	/**
	 * Check wether lighting are applied to strokes.
	 *
	 * @return  <code>true</code> if lighting is applied to strokes;
	 *          <code>false</code> otherwise.
	 */
	public boolean isStrokeLightingEnabled()
	{
		return _strokeLightingEnabled;
	}

	/**
	 * Enable/disable application of lighting to strokes.
	 *
	 * @param   enable  Enable vs. disable lighting for strokes.
	 */
	public void setStrokeLightingEnabled( final boolean enable )
	{
		_strokeLightingEnabled = enable;
	}

	/**
	 * Check wether vertex rendering is enabled.
	 *
	 * @return  <code>true</code> if vertex rendering is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isVertexEnabled()
	{
		return _vertexEnabled;
	}

	/**
	 * Enable/disable vertex rendering.
	 *
	 * @param   enable  Enable vs. disable vertex rendering.
	 */
	public void setVertexEnabled( final boolean enable )
	{
		_vertexEnabled = enable;
	}

	/**
	 * Get color used for vertices.
	 *
	 * @return  Color used for vertices;
	 *          <code>null</code> if undefined.
	 */
	public Color getVertexColor()
	{
		return _vertexColor;
	}

	/**
	 * Set color to use for vertices.
	 *
	 * @param   color   Color to use for vertices.
	 */
	public void setVertexColor( final Color color )
	{
		_vertexColor = color;
	}

	/**
	 * Check wether lighting are applied to vertices.
	 *
	 * @return  <code>true</code> if lighting are applied to vertices.
	 */
	public boolean isVertexLightingEnabled()
	{
		return _vertexLightingEnabled;
	}

	/**
	 * Enable/disable application of lighting to vertices.
	 *
	 * @param   enable  Enable vs. disable lighting for vertices.
	 */
	public void setVertexLightingEnabled( final boolean enable )
	{
		_vertexLightingEnabled = enable;
	}

	/**
	 * Get size of vertices.
	 *
	 * @return  Size of vertices.
	 */
	public float getVertexSize()
	{
		return _vertexSize;
	}

	/**
	 * Set size of vertices.
	 *
	 * @param   size    Size of vertices.
	 */
	public void setVertexSize( final float size )
	{
		_vertexSize = size;
	}

	/**
	 * Check wether backfaces are culled.
	 *
	 * @return  <code>true</code> if backfaces are culled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isBackfaceCullingEnabled()
	{
		return _backfaceCullingEnabled;
	}

	/**
	 * Enable/disable backfaces culling.
	 *
	 * @param   enable  Enable vs. disable culling of backfaces.
	 */
	public void setBackfaceCullingEnabled( final boolean enable )
	{
		_backfaceCullingEnabled = enable;
	}

	@Override
	public boolean equals( Object o )
	{
		if ( this == o ) return true;
		if ( !( o instanceof RenderStyle ) ) return false;

		RenderStyle that = (RenderStyle)o;

		if ( _backfaceCullingEnabled != that._backfaceCullingEnabled )
			return false;
		if ( _fillEnabled != that._fillEnabled ) return false;
		if ( _fillLightingEnabled != that._fillLightingEnabled ) return false;
		if ( Float.compare( that._materialAlpha, _materialAlpha ) != 0 )
		{
			return false;
		}
		if ( _materialEnabled != that._materialEnabled ) return false;
		if ( _materialLightingEnabled != that._materialLightingEnabled )
		{
			return false;
		}
		if ( _strokeEnabled != that._strokeEnabled ) return false;
		if ( _strokeLightingEnabled != that._strokeLightingEnabled )
			return false;
		if ( Float.compare( that._strokeWidth, _strokeWidth ) != 0 )
			return false;
		if ( _vertexEnabled != that._vertexEnabled ) return false;
		if ( _vertexLightingEnabled != that._vertexLightingEnabled )
			return false;
		if ( Float.compare( that._vertexSize, _vertexSize ) != 0 ) return false;
		if ( _fillColor != null ? !_fillColor.equals( that._fillColor ) : that._fillColor != null )
		{
			return false;
		}
		if ( _materialOverride != null ? !_materialOverride.equals( that._materialOverride ) : that._materialOverride != null )
		{
			return false;
		}
		if ( _strokeColor != null ? !_strokeColor.equals( that._strokeColor ) : that._strokeColor != null )
		{
			return false;
		}
		if ( _vertexColor != null ? !_vertexColor.equals( that._vertexColor ) : that._vertexColor != null )
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = ( _materialEnabled ? 1 : 0 );
		result = 31 * result + ( _materialLightingEnabled ? 1 : 0 );
		result = 31 * result + ( _materialOverride != null ? _materialOverride.hashCode() : 0 );
		result = 31 * result + ( _materialAlpha != +0.0f ? Float.floatToIntBits( _materialAlpha ) : 0 );
		result = 31 * result + ( _fillEnabled ? 1 : 0 );
		result = 31 * result + ( _fillColor != null ? _fillColor.hashCode() : 0 );
		result = 31 * result + ( _fillLightingEnabled ? 1 : 0 );
		result = 31 * result + ( _strokeEnabled ? 1 : 0 );
		result = 31 * result + ( _strokeColor != null ? _strokeColor.hashCode() : 0 );
		result = 31 * result + ( _strokeWidth != +0.0f ? Float.floatToIntBits( _strokeWidth ) : 0 );
		result = 31 * result + ( _strokeLightingEnabled ? 1 : 0 );
		result = 31 * result + ( _vertexEnabled ? 1 : 0 );
		result = 31 * result + ( _vertexColor != null ? _vertexColor.hashCode() : 0 );
		result = 31 * result + ( _vertexSize != +0.0f ? Float.floatToIntBits( _vertexSize ) : 0 );
		result = 31 * result + ( _vertexLightingEnabled ? 1 : 0 );
		result = 31 * result + ( _backfaceCullingEnabled ? 1 : 0 );
		return result;
	}
}
