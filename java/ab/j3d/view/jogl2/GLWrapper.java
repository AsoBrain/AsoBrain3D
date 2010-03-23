/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
package ab.j3d.view.jogl2;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * This class encapsulates a {@link GL} pipeline, and provides method to cache
 * several {@link GL} settings to prevent calling the {@link GL} context too
 * often.
 *
 * @author  Jark Reijerink
 * @author  Wijnand Wieskamp
 * @version $Revision$ $Date$
 */
public class GLWrapper
{
	/**
	 * Wrapped {@link GL} context.
	 */
	private GL _gl;

	/** Cached GL state. */ private boolean _blendState             = false;
	/** Cached GL state. */ private boolean _colorMaterialState     = false;
	/** Cached GL state. */ private boolean _cullFaceState          = false;
	/** Cached GL state. */ private boolean _lightingState          = false;
	/** Cached GL state. */ private boolean _lineSmoothState        = false;
	/** Cached GL state. */ private int     _blendFuncSfactor       = GL.GL_ONE;
	/** Cached GL state. */ private int     _blendFuncDfactor       = GL.GL_ZERO;
	/** Cached GL state. */ private float   _color4fR               = 1.0f;
	/** Cached GL state. */ private float   _color4fG               = 1.0f;
	/** Cached GL state. */ private float   _color4fB               = 1.0f;
	/** Cached GL state. */ private float   _color4fA               = 1.0f;
	/** Cached GL state. */ private int     _cullFace               = GL2.GL_BACK;
	/** Cached GL state. */ private float   _lineWidth              = 1.0f;
	/** Cached GL state. */ private float   _materialAmbientR       = 0.2f;
	/** Cached GL state. */ private float   _materialAmbientG       = 0.2f;
	/** Cached GL state. */ private float   _materialAmbientB       = 0.2f;
	/** Cached GL state. */ private float   _materialAmbientA       = 1.0f;
	/** Cached GL state. */ private float   _materialDiffuseR       = 0.8f;
	/** Cached GL state. */ private float   _materialDiffuseG       = 0.8f;
	/** Cached GL state. */ private float   _materialDiffuseB       = 0.8f;
	/** Cached GL state. */ private float   _materialDiffuseA       = 1.0f;
	/** Cached GL state. */ private float   _materialSpecularR      = 0.0f;
	/** Cached GL state. */ private float   _materialSpecularG      = 0.0f;
	/** Cached GL state. */ private float   _materialSpecularB      = 0.0f;
	/** Cached GL state. */ private float   _materialSpecularA      = 1.0f;
	/** Cached GL state. */ private float   _materialShininess      = 0.0f;
	/** Cached GL state. */ private float   _materialEmissionR      = 0.0f;
	/** Cached GL state. */ private float   _materialEmissionG      = 0.0f;
	/** Cached GL state. */ private float   _materialEmissionB      = 0.0f;
	/** Cached GL state. */ private float   _materialEmissionA      = 1.0f;
	/** Cached GL state. */ private int     _polygonModeMode        = GL2.GL_FILL;
	/** Cached GL state. */ private float   _polygonOffsetFactor    = 0.0f;
	/** Cached GL state. */ private float   _polygonOffsetUnits     = 0.0f;
	/** Cached GL state. */ private int     _shadeModel             = GL2.GL_SMOOTH;

	/**
	 * Wrapper for the JOGL {@link GL} class.
	 * This class tries to reduce the number of gl calls made to OpenGL, this
	 * improves performance greatly.
	 *
	 * @param gl GL class to wrap.
	 */
	public GLWrapper( final GL gl )
	{
		_gl = gl;
		resetAll();
	}

	/**
	 * Resets all OpenGL parameters that are cached by the wrapper to the values
	 * that the wrapper specifies. This ensures there are no inconsistencies
	 * between the wrapper and the OpenGL context.
	 */
	private void resetAll()
	{
		final GL2 gl = _gl.getGL2();
		setEnabled( GL.GL_BLEND , _blendState );
		setEnabled( GL2.GL_COLOR_MATERIAL , _colorMaterialState );
		setEnabled( GL.GL_CULL_FACE , _cullFaceState );
		setEnabled( GL2.GL_LIGHTING , _lightingState );
		setEnabled( GL.GL_LINE_SMOOTH , _lineSmoothState );
		gl.glBlendFunc( _blendFuncSfactor , _blendFuncDfactor );
		gl.glColor4f( _color4fR , _color4fG , _color4fB , _color4fA );
		gl.glCullFace( _cullFace );
		gl.glLineWidth( _lineWidth );
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_AMBIENT , new float[] { _materialAmbientR , _materialAmbientG , _materialAmbientB , _materialAmbientA } , 0 );
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_DIFFUSE , new float[] { _materialDiffuseR , _materialDiffuseG , _materialDiffuseB , _materialDiffuseA } , 0 );
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_SPECULAR , new float[] { _materialSpecularR , _materialSpecularG , _materialSpecularB , _materialSpecularA } , 0 );
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_EMISSION , new float[] { _materialEmissionR , _materialEmissionG , _materialEmissionB , _materialEmissionA } , 0 );
		gl.glMaterialf( GL.GL_FRONT_AND_BACK , GL2.GL_SHININESS , _materialShininess );
		gl.glPolygonMode( GL.GL_FRONT_AND_BACK , _polygonModeMode );
		gl.glPolygonOffset( _polygonOffsetFactor , _polygonOffsetUnits );
		gl.glShadeModel( _shadeModel );
	}

	/**
	 * Enables or disables the given parameter, using {@link GL#glEnable} or
	 * {@link GL#glDisable}.
	 *
	 * @param   parameter   Parameter to be enabled or disabled.
	 * @param   enable      <code>true</code> to enable the parameter;
	 *                      <code>false</code> to disable it.
	 */
	private void setEnabled( final int parameter , final boolean enable )
	{
		if ( enable )
		{
			_gl.glEnable( parameter );
		}
		else
		{
			_gl.glDisable( parameter );
		}
	}

	/**
	 * Enable or disable {@link GL#GL_BLEND}.
	 *
	 * @param enable Enables {@link GL#GL_BLEND} if set to true.
	 */
	public void setBlend( final boolean enable )
	{
		final Boolean blend = Boolean.valueOf( enable );
		if ( blend != _blendState )
		{
			setEnabled( GL.GL_BLEND , enable );
			_blendState = blend;
		}
	}

	/**
	 * Enable or disable {@link GL2#GL_COLOR_MATERIAL}.
	 *
	 * @param enable Enables {@link GL2#GL_COLOR_MATERIAL} if set to true.
	 */
	public void setColorMaterial( final boolean enable )
	{
		final Boolean colormaterial = Boolean.valueOf( enable );
		if ( colormaterial!= _colorMaterialState )
		{
			setEnabled( GL2.GL_COLOR_MATERIAL , enable );
			_colorMaterialState = colormaterial;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_CULL_FACE}.
	 *
	 * @param   cullFace    Enables {@link GL#GL_CULL_FACE} if set to true.
	 */
	public void setCullFace( final boolean cullFace )
	{
		if ( cullFace != _cullFaceState )
		{
			setEnabled( GL.GL_CULL_FACE , cullFace );
			_cullFaceState = cullFace;
		}
	}

	/**
	 * Enable or disable {@link GL2#GL_LIGHTING}.
	 *
	 * @param enable Enables {@link GL2#GL_LIGHTING} if set to true.
	 */
	public void setLighting( final boolean enable )
	{
		final Boolean lighting = Boolean.valueOf( enable );
		if ( lighting != _lightingState )
		{
			setEnabled( GL2.GL_LIGHTING , enable );
			_lightingState = lighting;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_LINE_SMOOTH}.
	 *
	 * @param enable Enables {@link GL#GL_LINE_SMOOTH} if set to true.
	 */
	public void setLineSmooth( final boolean enable )
	{
		final Boolean lineSmooth = Boolean.valueOf( enable );
		if ( lineSmooth!= _lineSmoothState )
		{
			setEnabled( GL.GL_LINE_SMOOTH , enable );
			_lineSmoothState = lineSmooth;
		}
	}

	/**
	 * Specify pixel arithmetic. {@link GL#GL_BLEND} needs to be enabled.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/blendfunc.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/blendfunc.html<a>.
	 *
	 * @param sFactor   Specifies how the red, green, blue, and alpha source blending
	 *                  factors are computed. The following symbolic constants are accepted:<br />
	 *                  {@link GL#GL_ZERO}, {@link GL#GL_ONE}, {@link GL#GL_DST_COLOR},
	 *                  {@link GL#GL_ONE_MINUS_DST_COLOR}, {@link GL#GL_SRC_ALPHA},
	 *                  {@link GL#GL_ONE_MINUS_SRC_ALPHA}, {@link GL#GL_DST_ALPHA},
	 *                  {@link GL#GL_ONE_MINUS_DST_ALPHA}, and {@link GL#GL_SRC_ALPHA_SATURATE}.
	 *                  The initial value is {@link GL#GL_ONE}.
	 * @param dFactor   Specifies how the red, green, blue, and alpha destination blending
	 *                  factors are computed. The following symbolic constants are accepted:<br />
	 *                  {@link GL#GL_ZERO}, {@link GL#GL_ONE}, {@link GL#GL_DST_COLOR},
	 *                  {@link GL#GL_ONE_MINUS_DST_COLOR}, {@link GL#GL_SRC_ALPHA},
	 *                  {@link GL#GL_ONE_MINUS_SRC_ALPHA}, {@link GL#GL_DST_ALPHA},
	 *                  {@link GL#GL_ONE_MINUS_DST_ALPHA}, and {@link GL#GL_SRC_ALPHA_SATURATE}.
	 *                  The initial value is {@link GL#GL_ZERO}.
	 *
	 * @see GL#glBlendFunc(int, int)
	 */
	public void glBlendFunc( final int sFactor , final int dFactor )
	{
		if ( sFactor != _blendFuncSfactor || dFactor != _blendFuncDfactor )
		{
			_gl.glBlendFunc( sFactor , dFactor );
			_blendFuncSfactor = sFactor;
			_blendFuncDfactor = dFactor;
		}
	}

	/**
	 * Specify whether front- or back-facing facets can be culled.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/cullface.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/cullface.html<a>.
	 *
	 * @param cullFaceMode  Specifies whether front- or back-facing facets are
	 *                      candidates for culling. Symbolic constants
	 *                      {@link GL#GL_FRONT}, {@link GL#GL_BACK}, and
	 *                      {@link GL#GL_FRONT_AND_BACK} are accepted.
	 *                      The initial value is {@link GL#GL_BACK}.
	 *
	 * @see GL#glCullFace(int)
	 */
	public void glCullFace( final int cullFaceMode )
	{
		if ( cullFaceMode != _cullFace )
		{
			_gl.glCullFace( cullFaceMode );
			_cullFace = cullFaceMode;
		}
	}

	/**
	 * Specify the width of rasterized lines.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/linewidth.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/linewidth.html<a>.
	 *
	 * @param width Specifies the width of rasterized lines. The initial value is 1.
	 *
	 * @see GL#glLineWidth(float)
	 */
	public void glLineWidth ( final float width )
	{
		if ( _lineWidth != width )
		{
			_gl.glLineWidth( width );
			_lineWidth = width;
		}
	}

	/**
	 * Specify ambient reflection material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param   r       Red.
	 * @param   g       Green.
	 * @param   b       Blue.
	 * @param   a       Alpha.
	 *
	 * @see     GL2#glMaterialfv(int, int, float[] , int)
	 */
	public void setMaterialAmbient( final float r , final float g , final float b , final float a )
	{
		if ( ( r != _materialAmbientR ) || ( g != _materialAmbientG ) || ( b != _materialAmbientB ) || ( a != _materialAmbientA ) )
		{
			final GL2 gl = _gl.getGL2();
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_AMBIENT , new float[] { r , g , b , a } , 0 );
			_materialAmbientR = r;
			_materialAmbientG = g;
			_materialAmbientB = b;
			_materialAmbientA = a;
		}
	}

	/**
	 * Specify diffuse reflection material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param   r       Red.
	 * @param   g       Green.
	 * @param   b       Blue.
	 * @param   a       Alpha.
	 *
	 * @see     GL2#glMaterialfv(int, int, float[] , int)
	 */
	public void setMaterialDiffuse( final float r , final float g , final float b , final float a )
	{
		if ( ( r != _materialDiffuseR ) || ( g != _materialDiffuseG ) || ( b != _materialDiffuseB ) || ( a != _materialDiffuseA ) )
		{
			final GL2 gl = _gl.getGL2();
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_DIFFUSE , new float[] { r , g , b , a } , 0 );
			_materialDiffuseR = r;
			_materialDiffuseG = g;
			_materialDiffuseB = b;
			_materialDiffuseA = a;
		}
	}

	/**
	 * Specify specular reflection material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param   r       Red.
	 * @param   g       Green.
	 * @param   b       Blue.
	 * @param   a       Alpha.
	 *
	 * @see     GL2#glMaterialfv(int, int, float[] , int)
	 */
	public void setMaterialSpecular( final float r , final float g , final float b , final float a )
	{
		if ( ( r != _materialSpecularR ) || ( g != _materialSpecularG ) || ( b != _materialSpecularB ) || ( a != _materialSpecularA ) )
		{
			final GL2 gl = _gl.getGL2();
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_SPECULAR , new float[] { r , g , b , a } , 0 );
			_materialSpecularR = r;
			_materialSpecularG = g;
			_materialSpecularB = b;
			_materialSpecularA = a;
		}
	}

	/**
	 * Set the shininess of material for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param   shininess   Shininess factor.
	 *
	 * @see     GL2#glMaterialf(int, int, float)
	 */
	public void setMaterialShininess( final float shininess )
	{
		if ( shininess != _materialShininess )
		{
			final GL2 gl = _gl.getGL2();
			gl.glMaterialf( GL.GL_FRONT_AND_BACK , GL2.GL_SHININESS , shininess );
			_materialShininess = shininess;
		}
	}

	/**
	 * Specify emission reflection material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param   r       Red.
	 * @param   g       Green.
	 * @param   b       Blue.
	 * @param   a       Alpha.
	 *
	 * @see     GL2#glMaterialfv(int, int, float[] , int)
	 */
	public void setMaterialEmission( final float r , final float g , final float b , final float a )
	{
		if ( ( r != _materialEmissionR ) || ( g != _materialEmissionG ) || ( b != _materialEmissionB ) || ( a != _materialEmissionA ) )
		{
			final GL2 gl = _gl.getGL2();
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK , GL2.GL_EMISSION , new float[] { r , g , b , a } , 0 );
			_materialEmissionR = r;
			_materialEmissionG = g;
			_materialEmissionB = b;
			_materialEmissionA = a;
		}
	}

	/**
	 * Select flat or smooth shading.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/shademodel.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/shademodel.html<a>.
	 *
	 * @param mode    Specifies a symbolic value representing a shading technique.
	 *                Accepted values are {@link GL2#GL_FLAT} and {@link GL2#GL_SMOOTH}.
	 *                The initial value is {@link GL2#GL_SMOOTH}.
	 *
	 * @see GL2#glShadeModel(int)
	 */
	public void glShadeModel( final int mode )
	{
		if ( mode != _shadeModel )
		{
			final GL2 gl = _gl.getGL2();
			gl.glShadeModel( mode );
			_shadeModel = mode;
		}
	}

	/**
	 * Set the current color to use when no lighting is used).
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html<a>.
	 *
	 * @param red       Red component.
	 * @param green     Green component.
	 * @param blue      Blue component.
	 * @param alpha     Alpha component
	 *
	 * @see GL2#glColor4f(float, float, float, float)
	 */
	public void setColor( final float red , final float green , final float blue , final float alpha )
	{
		if ( ( red != _color4fR ) || ( green != _color4fG ) || ( blue != _color4fB ) || ( alpha != _color4fA ) )
		{
			final GL2 gl = _gl.getGL2();
			gl.glColor4f( red , green , blue , alpha );
			_color4fR = red;
			_color4fG = green;
			_color4fB = blue;
			_color4fA = alpha;
		}
	}

	/**
	 * Set the scale and units used to calculate depth values.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonoffset.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonoffset.html<a>.
	 *
	 * @param factor    Specifies a scale factor that is used to create a variable
	 *                  depth offset for each polygon. The initial value is 0.
	 * @param units     Is multiplied by an implementation-specific value to
	 *                  create a constant depth offset. The initial value is 0.
	 *
	 * @see GL#glPolygonOffset(float, float)
	 */
	public void glPolygonOffset( final float factor , final float units )
	{
		if ( ( factor != _polygonOffsetFactor ) || ( units != _polygonOffsetUnits ) )
		{
			_gl.glPolygonOffset( factor , units );

			_polygonOffsetFactor = factor;
			_polygonOffsetUnits  = units;
		}
	}

	/**
	 * Set mode for rasterizing polygons.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonmode.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonmode.html<a>.
	 *
	 * @param   mode    Specifies how polygons will be rasterized.  Accepted
	 *                  values are GL_POINT, GL_LINE, and GL_FILL. The
	 *                  initial value is GL_FILL for both front- and back-
	 *                  facing polygons.
	 *
	 * @see GL2#glPolygonMode(int, int)
	 */
	public void setPolygonMode( final int mode )
	{
		if ( mode != _polygonModeMode )
		{
			final GL2 gl = _gl.getGL2();
			gl.glPolygonMode( GL.GL_FRONT_AND_BACK , mode );
			_polygonModeMode  = mode;
		}
	}
}
