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
package ab.j3d.view;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;

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
	 * Flag to indicate that rendering with appearance is enabled.
	 */
	private boolean _materialEnabled;

	/**
	 * Flag to indicate that lighting should be applied to rendering with
	 * appearance.
	 */
	private boolean _materialLightingEnabled;

	/**
	 * Override original appearance with this one. This may be <code>null</code>
	 * to disable override.
	 */
	private Appearance _appearanceOverride;

	/**
	 * Extra alpha value for appearance. This can be used to make the appearance
	 * (partially) translucent.
	 */
	private float _extraAlpha;

	/**
	 * Flag to indicate that filling is enabled.
	 */
	private boolean _fillEnabled;

	/**
	 * Color to use for filling.
	 */
	private Color4 _fillColor = null;

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
	private Color4 _strokeColor;

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
	private Color4 _vertexColor = null;

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
		_appearanceOverride = null;
		_extraAlpha = 1.0f;
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
	public static Color4 blendColors( final Color4 src, final Color4 dst )
	{
		return ( dst != null ) ? blendColors( src, dst.getRedFloat(), dst.getGreenFloat(), dst.getBlueFloat(), dst.getAlphaFloat() ) : src;
	}

	/**
	 * Blend two colors.
	 *
	 * @param   src         Incoming color, blended with the destination color.
	 * @param   dstRed      Red intensity of destination color.
	 * @param   dstGreen    Green intensity of destination color.
	 * @param   dstBlue     Blue value of destination color.
	 * @param   dstAlpha    Alpha value of destination color.
	 *
	 * @return  Blended color.
	 */
	public static Color4 blendColors( final Color4 src, final float dstRed, final float dstGreen, final float dstBlue, final float dstAlpha )
	{
		final Color4 result;

		final float srcAlpha = src.getAlphaFloat();
		if ( srcAlpha < 1.0f )
		{
			final float dstRatio = dstAlpha * ( 1.0f - srcAlpha );

			final float alpha = dstRatio + srcAlpha;
			final float red   = ( dstRatio * dstRed + srcAlpha * src.getRedFloat() ) / alpha;
			final float green = ( dstRatio * dstGreen + srcAlpha * src.getGreenFloat() ) / alpha;
			final float blue  = ( dstRatio * dstBlue + srcAlpha * src.getBlueFloat() ) / alpha;

			result = new Color4f( red, green, blue, alpha );
		}
		else
		{
			result = src;
		}

		return result;
	}

	@Override
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
	 * Check whether rendering with appearances is enabled.
	 *
	 * @return  <code>true</code> if rendering with appearances is enabled;
	 *          <code>false</code> otherwise.
	 */
	public boolean isMaterialEnabled()
	{
		return _materialEnabled;
	}

	/**
	 * Enable/disable rendering with appearances.
	 *
	 * @param   enable  Enable vs. disable rendering with appearances.
	 */
	public void setMaterialEnabled( final boolean enable )
	{
		_materialEnabled = enable;
	}

	/**
	 * Check whether lighting is applied to rendering with appearance.
	 *
	 * @return  <code>true</code> if lighting is applied to rendering with appearance;
	 *          <code>false</code> otherwise.
	 */
	public boolean isMaterialLightingEnabled()
	{
		return _materialLightingEnabled;
	}

	/**
	 * Enable/disable application of lighting to rendering with appearance.
	 *
	 * @param   enable  Enable vs. disable of lighting for appearance rendering.
	 */
	public void setMaterialLightingEnabled( final boolean enable )
	{
		_materialLightingEnabled = enable;
	}

	/**
	 * Get appearance override. The original appearance will be replaced with
	 * this one.
	 *
	 * @return  Appearance override;
	 *          <code>null</code> if disabled.
	 */
	public Appearance getAppearanceOverride()
	{
		return _appearanceOverride;
	}

	/**
	 * Set appearance override. The original appearance will be replaced with
	 * this one. This may be <code>null</code> to disable override.
	 *
	 * @param   appearance    Appearance override (<code>null</code> to disable).
	 */
	public void setAppearanceOverride( final Appearance appearance )
	{
		_appearanceOverride = appearance;
	}

	/**
	 * Get extra alpha value for appearance.
	 *
	 * @return  Extra alpha value for appearance (0.0 = completely translucent;
	 *          1.0 = use unaltered alpha values from appearance).
	 * Extra alpha value for appearance. This can be used to make the appearance
	 * (partially) translucent.
	 */
	public float getExtraAlpha()
	{
		return _extraAlpha;
	}

	/**
	 * Set extra alpha value for appearance. This can be used to make the appearance
	 * (partially) translucent.
	 *
	 * @param   alpha   Extra alpha value for appearance (0.0 = completely
	 *                  translucent; 1.0 = use unaltered alpha values from
	 *                  appearance).
	 */
	public void setExtraAlpha( final float alpha )
	{
		_extraAlpha = alpha;
	}

	/**
	 * Check whether filling is enabled.
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
	public Color4 getFillColor()
	{
		return _fillColor;
	}

	/**
	 * Set color to use for filling.
	 *
	 * @param   color   Color to use for filling.
	 */
	public void setFillColor( final Color4 color )
	{
		_fillColor = color;
	}

	/**
	 * Check whether lighting is applied to filling.
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
	 * Check whether stroke (outline) rendering is enabled.
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
	public Color4 getStrokeColor()
	{
		return _strokeColor;
	}

	/**
	 * Set color to use for strokes.
	 *
	 * @param   color   Color to use for strokes.
	 */
	public void setStrokeColor( final Color4 color )
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
	 * Check whether lighting are applied to strokes.
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
	 * Check whether vertex rendering is enabled.
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
	public Color4 getVertexColor()
	{
		return _vertexColor;
	}

	/**
	 * Set color to use for vertices.
	 *
	 * @param   color   Color to use for vertices.
	 */
	public void setVertexColor( final Color4 color )
	{
		_vertexColor = color;
	}

	/**
	 * Check whether lighting are applied to vertices.
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
	 * Check whether backfaces are culled.
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
	public boolean equals( final Object o )
	{
		if ( this == o ) return true;
		if ( !( o instanceof RenderStyle ) ) return false;

		RenderStyle that = (RenderStyle)o;

		if ( _backfaceCullingEnabled != that._backfaceCullingEnabled )
			return false;
		if ( _fillEnabled != that._fillEnabled ) return false;
		if ( _fillLightingEnabled != that._fillLightingEnabled ) return false;
		if ( Float.compare( that._extraAlpha, _extraAlpha ) != 0 )
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
		if ( _appearanceOverride != null ? !_appearanceOverride.equals( that._appearanceOverride ) : that._appearanceOverride != null )
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
		result = 31 * result + ( _appearanceOverride != null ? _appearanceOverride.hashCode() : 0 );
		result = 31 * result + ( _extraAlpha != +0.0f ? Float.floatToIntBits( _extraAlpha ) : 0 );
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
