/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.view.jogl;

import java.awt.Color;
import javax.media.opengl.GL;

import ab.j3d.Matrix3D;

/**
 * This class encapsulates a {@link GL} context, and provides method to cache
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
	 * Tri-state enum.
	 */
	public static enum TriState
	{
		/** Tri-state: enabled.   */ ENABLED   ,
		/** Tri-state: disabled.  */ DISABLED  ,
		/** Tri-state: undefined. */ UNDEFINED ,
	}

	/**
	 * Wrapped {@link GL} context.
	 */
	private GL _gl;

	/** Cached state of {@link GL#GL_BLEND}.                             */     private         TriState _blendState             = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_COLOR_MATERIAL}.                    */     private         TriState _colorMaterialState     = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_CULL_FACE}.                         */     private         TriState _cullFaceState          = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_LIGHTING}.                          */     private         TriState _lightingState          = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_SMOOTH}.                            */     private         TriState _lineSmoothState        = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_TEXTURE_2D}.                        */     private         TriState _texture2dState         = TriState.UNDEFINED;
	/** Cached state of {@link GL#glBindTexture(int, int)}.              */     private         int      _boundTextureTarget     = -1;
	/** Cached state of {@link GL#glBindTexture(int, int)}.              */     private         int      _boundTextureObject     = -1;
	/** Cached state of {@link GL#glBlendFunc(int, int)}.                */     private         int      _blendFuncSfactor       = -1;
	/** Cached state of {@link GL#glBlendFunc(int, int)}.                */     private         int      _blendFuncDfactor       = -1;
	/** Cached state of {@link GL#glColorMaterial(int, int)}.            */     private         int      _colorMaterialFace      = -1;
	/** Cached state of {@link GL#glColorMaterial(int, int)}.            */     private         int      _colorMaterialMode      = -1;
	/** Cached state of {@link GL#glCullFace(int)}.                      */     private         int      _cullFace               = -1;
	/** Cached state of {@link GL#glDepthFunc(int)}.                     */     private         int      _depthFunc              = -1;
	/** Cached state of {@link GL#glLineWidth(float)}.                   */     private         float    _lineWidth              = -1.0F;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}.         */     private         int      _materialFace           = -1;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}.         */     private         int      _materialPname          = -1;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}.         */     private         float    _materialParam          = -1.0F;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialAmbientFace            = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private float[]  _materialAmbientParams          = null;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialAmbientI               = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialDiffuseFace            = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private float[]  _materialDiffuseParams          = null;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialDiffuseI               = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialSpecularFace           = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private float[]  _materialSpecularParams         = null;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialSpecularI              = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialEmissionFace           = -1;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private float[]  _materialEmissionParams         = null;
	/** Cached state of {@link GL#glMaterialfv(int, int, float[], int)}. */     private int      _materialEmissionI              = -1;
	/** Cached state of {@link GL#glPolygonOffset(float, float)}         */     private         float    _polygonOffsetFactor    = -1.0F;
	/** Cached state of {@link GL#glPolygonOffset(float, float)}         */     private         float    _polygonOffsetUnits     = -1.0F;
	/** Cached state of {@link GL#glShadeModel(int)}.                    */     private         int      _shadeModel             = -1;

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
	}

	/**
	 * This method returns the current {@link GL}.
	 *
	 * @return The current {@link GL}
	 */
	public GL getGL()
	{
		return _gl;
	}

	/**
	 * Resets all cached gl values to TriState.UNDEFINED or -1.
	 */
	public void reset()
	{
		_lightingState         = TriState.UNDEFINED;
		_cullFaceState         = TriState.UNDEFINED;
		_texture2dState        = TriState.UNDEFINED;
		_blendState            = TriState.UNDEFINED;
		_lineSmoothState       = TriState.UNDEFINED;
		_colorMaterialState    = TriState.UNDEFINED;
		_shadeModel            = -1;
		_cullFace              = -1;
		_blendFuncSfactor      = -1;
		_blendFuncDfactor      = -1;
		_boundTextureTarget    = -1;
		_boundTextureObject    = -1;
		_materialFace          = -1;
		_materialPname         = -1;
		_colorMaterialFace     = -1;
		_colorMaterialMode     = -1;
		_depthFunc             = -1;
		_materialParam         = -1.0f;
		_lineWidth             = -1.0f;
		_materialAmbientI      = -1;
		_materialAmbientParams = null;
		_materialDiffuseI      = -1;
		_materialDiffuseParams = null;
		_materialSpecularI      = -1;
		_materialSpecularParams = null;
		_materialEmissionI      = -1;
		_materialEmissionParams = null;
	}

	/**
	 * Enable or disable {@link GL#GL_BLEND}.
	 *
	 * @param enable Enables {@link GL#GL_BLEND} if set to true.
	 */
	public void setBlend( final boolean enable )
	{
		final TriState blend = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( blend != _blendState )
		{
			if ( enable )
			{
				glEnable( GL.GL_BLEND );
			}
			else
			{
				glDisable( GL.GL_BLEND );
			}

			_blendState = blend;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_COLOR_MATERIAL}.
	 *
	 * @param enable Enables {@link GL#GL_COLOR_MATERIAL} if set to true.
	 */
	public void setColorMaterial( final boolean enable )
	{
		final TriState colormaterial = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( colormaterial!= _colorMaterialState )
		{
			if ( enable )
			{
				glEnable( GL.GL_COLOR_MATERIAL );
			}
			else
			{
				glDisable( GL.GL_COLOR_MATERIAL );
			}

			_colorMaterialState = colormaterial;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_CULL_FACE}.
	 *
	 * @param enable Enables {@link GL#GL_CULL_FACE} if set to true.
	 */
	public void setCullFace( final boolean enable )
	{
		final TriState cullFace = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( cullFace != _cullFaceState )
		{
			if ( enable )
			{
				glEnable( GL.GL_CULL_FACE );
			}
			else
			{
				glDisable( GL.GL_CULL_FACE );
			}

			_cullFaceState = cullFace;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_LIGHTING}.
	 *
	 * @param enable Enables {@link GL#GL_LIGHTING} if set to true.
	 */
	public void setLighting( final boolean enable )
	{
		final TriState lighting = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( lighting != _lightingState )
		{
			if ( enable )
			{
				glEnable( GL.GL_LIGHTING );
			}
			else
			{
				glDisable( GL.GL_LIGHTING );
			}

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
		final TriState lineSmooth = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( lineSmooth!= _lineSmoothState )
		{
			if ( enable )
			{
				glEnable( GL.GL_LINE_SMOOTH );
			}
			else
			{
				glDisable( GL.GL_LINE_SMOOTH );
			}

			_lineSmoothState = lineSmooth;
		}
	}

	/**
	 * Enable or disable {@link GL#GL_TEXTURE_2D}.
	 *
	 * @param enable Enables {@link GL#GL_TEXTURE_2D} if set to true.
	 */
	public void setTexture2D( final boolean enable ) //Geen winst!
	{
		final TriState texture2D = enable ? TriState.ENABLED : TriState.DISABLED;
		if ( texture2D != _texture2dState )
		{
			if ( enable )
			{
				glEnable( GL.GL_TEXTURE_2D );
			}
			else
			{
				glDisable( GL.GL_TEXTURE_2D );
			}

			//_texture2dState = texture2D;
		}
	}

	/**
	 * Bind a named texture to a texturing target.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/bindtexture.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/bindtexture.html<a>
	 *
	 * @param target        Specifies the target to which the texture is bound.
	 *                      Must be either GL_TEXTURE_1D or GL_TEXTURE_2D.
	 * @param textureObject Specifies the name of a texture.
	 *
	 * @see GL#glBindTexture
	 */
	public void glBindTexture( final int target , final int textureObject )
	{
		if ( ( target != _boundTextureTarget ) || ( textureObject != _boundTextureObject ) )
		{
			_gl.glBindTexture( target , textureObject );
			//_boundTextureTarget = target;
			//_boundTextureObject = textureObject;
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
	 * Cause a material color to track the current color.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/colormaterial.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/colormaterial.html<a>.
	 *
	 * @param face  Specifies whether front, back, or both front and back material
	 *              parameters should track the current color.
	 *              Accepted values are {@link GL#GL_FRONT}, {@link GL#GL_BACK}
	 *              and {@link GL#GL_FRONT_AND_BACK}.
	 *              The initial value is  {@link GL#GL_FRONT_AND_BACK}.
	 * @param mode  Specifies which of several material parameters track the
	 *              current color.
	 *              Accepted values are {@link GL#GL_EMISSION},  {@link GL#GL_AMBIENT},
	 *              {@link GL#GL_DIFFUSE},  {@link GL#GL_SPECULAR} and
	 *              {@link GL#GL_AMBIENT_AND_DIFFUSE}.
	 *              The initial value is  {@link GL#GL_AMBIENT_AND_DIFFUSE}.
	 *
	 * @see GL#glColorMaterial(int, int)
	 */
	public void glColorMaterial ( final int face , final int mode )
	{
		if ( ( face != _colorMaterialFace ) || ( mode != _colorMaterialMode ) )
		{
			_gl.glColorMaterial( face , mode );

			_colorMaterialFace = face;
			_colorMaterialMode = mode;
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
	 * Specify the value used for depth buffer comparisons.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/depthfunc.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/depthfunc.html<a>.
	 *
	 * @param func  Specifies the depth comparison function. Symbolic constants
	 *              {@link GL#GL_NEVER}, {@link GL#GL_LESS}, {@link GL#GL_EQUAL},
	 *              {@link GL#GL_LEQUAL},{@link GL#GL_GREATER}, {@link GL#GL_NOTEQUAL},
	 *              {@link GL#GL_GEQUAL}, and {@link GL#GL_ALWAYS} are accepted.
	 *              The initial value is {@link GL#GL_LESS}.
	 *
	 * @see GL#glDepthFunc(int)
	 */
	public void glDepthFunc( final int func )
	{
		if ( _depthFunc != func )
		{
			_gl.glDepthFunc ( GL.GL_LEQUAL );
			_depthFunc = func;
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
	 * Specify material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param face  Specifies which face or faces are being updated. Must be one
	 *              of {@link GL#GL_FRONT}, {@link GL#GL_BACK}, or {@link GL#GL_FRONT_AND_BACK}.
	 * @param pname Specifies the single-valued material parameter of the face
	 *              or faces that is being updated. Must be {@link GL#GL_SHININESS}.
	 * @param param Specifies the value that parameter GL_SHININESS will be set to.
	 *
	 * @see GL#glMaterialf(int, int, float)
	 */
	public void glMaterialf ( final int face , final int pname , final float param )
	{
		if ( ( face != _materialFace ) || ( pname != _materialPname ) || ( param != _materialParam ) )
		{
			_gl.glMaterialf( face , pname , param );

			_materialFace  = face;
			_materialPname = pname;
			_materialParam = param;
		}
	}

	/**
	 * Specify material parameters for the lighting model.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/material.html<a>.
	 *
	 * @param face      Specifies which face or faces are being updated. Must be one
	 *                  of {@link GL#GL_FRONT}, {@link GL#GL_BACK}, or {@link GL#GL_FRONT_AND_BACK}.
	 * @param pname     Specifies the material parameter of the face or faces
	 *                  that is being updated. Must be one of {@link GL#GL_AMBIENT}, {@link GL#GL_DIFFUSE},
	 *                  {@link GL#GL_SPECULAR}, {@link GL#GL_EMISSION}, {@link GL#GL_SHININESS},
	 *                  {@link GL#GL_AMBIENT_AND_DIFFUSE}, or {@link GL#GL_COLOR_INDEXES}.
	 * @param params    Specifies a pointer to the value or values that pname will be set to.
	 *
	 * @param i         Specifies a pointer to the value or values that pname will be set to.
	 *
	 * @see GL#glMaterialfv(int, int, float[] , int)
	 */
	public void glMaterialfv( final int face , final int pname , final float[] params , final int i )
	{
		//four types of pnames will be cached: GL_AMBIENT, GL_DIFFUSE, GL_SPECULAR, GL_EMISSION
		final GL gl = _gl;
		switch( pname )
		{
			case GL.GL_AMBIENT:
				if ( face != _materialAmbientFace || params != _materialAmbientParams || i != _materialAmbientI )
				{
					gl.glMaterialfv( face , pname , params , i );
					_materialAmbientFace   = face;
					_materialAmbientParams = params.clone();
					_materialAmbientI      = i;
				}
				break;
			case GL.GL_DIFFUSE:
				if ( face != _materialDiffuseFace || params != _materialDiffuseParams || face != _materialDiffuseI )
				{
					gl.glMaterialfv( face , pname , params , i );
					_materialDiffuseFace   = face;
					_materialDiffuseParams = params.clone();
					_materialDiffuseI      = i;
				}
				break;
			case GL.GL_SPECULAR:
				if ( face != _materialSpecularFace || params != _materialSpecularParams || face != _materialSpecularI )
				{
					gl.glMaterialfv( face , pname , params , i );
					_materialSpecularFace   = face;
					_materialSpecularParams = params.clone();
					_materialSpecularI      = i;
				}
				break;
			case GL.GL_EMISSION:
				if ( face != _materialEmissionFace || params != _materialEmissionParams || face != _materialEmissionI )
				{
					gl.glMaterialfv( face , pname , params , i );
					_materialEmissionFace   = face;
					_materialEmissionParams = params.clone();
					_materialEmissionI      = i;
				}
				break;
			default:
				gl.glMaterialfv( face , pname , params , i );
				break;
		}
	}

	/**
	 * Select flat or smooth shading.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/shademodel.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/shademodel.html<a>.
	 *
	 * @param mode    Specifies a symbolic value representing a shading technique.
	 *                Accepted values are {@link GL#GL_FLAT} and {@link GL#GL_SMOOTH}.
	 *                The initial value is {@link GL#GL_SMOOTH}.
	 *
	 * @see GL#glShadeModel(int)
	 */
	public void glShadeModel( final int mode )
	{
		if ( mode != _shadeModel )
		{
			_gl.glShadeModel( mode );
			_shadeModel = mode;
		}
	}


	/**
	 * Enable server-side GL capabilities.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/enable.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/enable.html<a>.
	 *
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 *
	 * @see GL#glEnable(int)
	 */
	public  void glEnable( final int cap )
	{
		_gl.glEnable( cap );
	}

	/**
	 * Disable server-side GL capabilities.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/enable.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/enable.html<a>.
	 *
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 *
	 * @see GL#glDisable(int)
	 */
	public  void glDisable( final int cap )
	{
		_gl.glDisable( cap );
	}

	/**
	 * Delimit the vertices of a primitive or a group of like primitives.
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/begin.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/begin.html<a>.
	 *
	 * @param mode  Specifies the primitive or primitives that will be created
	 *              from vertices presented between {@link GL#glBegin} and the
	 *              subsequent {@link GL#glEnd}. Ten symbolic constants are
	 *              accepted: {@link GL#GL_POINTS}, {@link GL#GL_LINES},
	 *              {@link GL#GL_LINE_STRIP}, {@link GL#GL_LINE_LOOP},
	 *              {@link GL#GL_TRIANGLES}, {@link GL#GL_TRIANGLE_STRIP},
	 *              {@link GL#GL_TRIANGLE_FAN}, {@link GL#GL_QUADS},
	 *              {@link GL#GL_QUAD_STRIP}, and {@link GL#GL_POLYGON}.
	 *
	 * @see GL#glBegin(int)
	 */
	public  void glBegin ( final int mode )
	{
		_gl.glBegin( mode );
	}

	/**
	 * Delimit the vertices of a primitive or a group of like primitives.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/begin.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/begin.html<a>.
	 *
	 * @see GL#glEnd()
	 */
	public  void glEnd()
	{
		_gl.glEnd();
	}

	/**
	 * Clear GL canvas.
	 *
	 * @param color     Color to clear canvas with.
	 *
	 * @see #glClearColor(float, float, float, float)
	 */
	public void glClearColor( final Color color )
	{
		final float[] rgba = new float[4];
		color.getRGBComponents( rgba );
		glClearColor( rgba[ 0 ] , rgba[ 1 ] , rgba[ 2 ] , rgba[ 3 ] );
	}

	/**
	 * Specify clear values for the color buffers.
	 * Initial value is 0.0f for all parameters.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/clearcolor.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/clearcolor.html<a>.
	 *
	 * @param red       Red component of the clear color.
	 * @param green     Green component of the clear color.
	 * @param blue      Blue component of the clear color.
	 * @param alpha     Alpha component of the clear color.
	 *
	 * @see GL#glClearColor(float, float, float, float)
	 */
	public void glClearColor( final float red , final float green , final float blue , final float alpha )
	{
		_gl.glClearColor( red , green , blue , alpha );
	}

	/**
	 * Set the current color.
	 * The initial value for the current color is (1, 1, 1).<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html<a>.
	 *
	 * @param red       Red component.
	 * @param green     Green component.
	 * @param blue      Blue component.
	 *
	 * @see GL#glColor3f(float, float, float)
	 */
	public  void glColor3f( final float red , final float green , final float blue )
	{
		_gl.glColor3f( red , green , blue );
	}

	/**
	 * Set the current color.
	 * The initial value for the current color is (1, 1, 1, 1).<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/color.html<a>.
	 *
	 * @param red       Red component.
	 * @param green     Green component.
	 * @param blue      Blue component.
	 * @param alpha     Alpha component
	 *
	 * @see GL#glColor4f(float, float, float, float)
	 */
	public void glColor4f( final float red , final float green , final float blue , final float alpha )
	{
		_gl.glColor4f( red , green , blue , alpha );
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param   transform   Transformation to multiply with.
	 */
	public void glMultMatrixd( final Matrix3D transform )
	{
		_gl.glMultMatrixd( new double[]
			{
				transform.xx , transform.yx , transform.zx , 0.0 ,
				transform.xy , transform.yy , transform.zy , 0.0 ,
				transform.xz , transform.yz , transform.zz , 0.0 ,
				transform.xo , transform.yo , transform.zo , 1.0
			} , 0 );
	}

	/**
	 * Set the current normal vector.
	 * The initial value of the current normal is the unit vector, (0, 0, 1).<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/normal.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/normal.html<a>.
	 *
	 * @param nx   Specify the x coordinate of the new normal.
	 * @param ny   Specify the y coordinate of the new normal.
	 * @param nz   Specify the z coordinate of the new normal.
	 *
	 * @see GL#glNormal3d(double, double, double)
	 */
	public  void glNormal3d(  final double nx , final double ny , final double nz )
	{
		_gl.glNormal3d( nx , ny , nz );
	}

	/**
	 * Select a polygon rasterization mode.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonmode.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/polygonmode.html<a>.
	 *
	 * @param face  Specifies the polygons that mode applies to. Must be {@link GL#GL_FRONT}
	 *              for front-facing polygons, {@link GL#GL_BACK} for back-facing polygons,
	 *              or {@link GL#GL_FRONT_AND_BACK} for front- and back-facing polygons.
	 * @param mode  Specifies how polygons will be rasterized. Accepted values are
	 *              {@link GL#GL_POINT}, {@link GL#GL_LINE}, and {@link GL#GL_FILL}.
	 *              The initial value is {@link GL#GL_FILL} for both front- and back-facing polygons.
	 *
	 * @see GL#glPolygonMode(int, int)
	 */
	public void glPolygonMode( final int face , final int mode )
	{
		_gl.glPolygonMode( face , mode );
	}

	/**
	 * Pop the current matrix stack.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/pushmatrix.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/pushmatrix.html<a>.
	 *
	 * @see GL#glPopMatrix()
	 */
	public  void glPopMatrix()
	{
		_gl.glPopMatrix();
	}

	/**
	 * Push the current matrix stack.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/pushmatrix.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/pushmatrix.html<a>.
	 *
	 * @see GL#glPushMatrix()
	 */
	public void glPushMatrix( )
	{
		_gl.glPushMatrix();
	}

	/**
	 * Set the current texture coordinates.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/texcoord.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/texcoord.html<a>.
	 *
	 * @param s     Texture U coordinate.
	 * @param t     Texture V coordinate.
	 *
	 * @see GL#glTexCoord2f(float, float)
	 */
	public  void glTexCoord2f( final float s , final float t )
	{
		_gl.glTexCoord2f( s , t );
	}

	/**
	 * Specify a vertex.<br />
	 * See <a href="http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/vertex.html">http://www.opengl.org/documentation/specs/man_pages/hardcopy/GL/html/gl/vertex.html<a>.
	 *
	 * @param x     X coordinate.
	 * @param y     Y coordinate.
	 * @param z     Z coordinate.
	 *
	 * @see GL#glVertex3d(double, double, double)
	 */
	public  void glVertex3d( final double x , final double y , final double z )
	{
		_gl.glVertex3d( x , y , z );
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
			_gl.glPolygonOffset( factor, units );

			_polygonOffsetFactor = factor;
			_polygonOffsetUnits  = units;
		}
	}
}