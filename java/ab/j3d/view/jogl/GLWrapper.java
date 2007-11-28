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
 */
public class GLWrapper
{
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

	/** Cached state of {@link GL#GL_BLEND}.                     */ private TriState _blendState         = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_COLOR_MATERIAL}.            */ private TriState _colorMaterialState = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_CULL_FACE}.                 */ private TriState _cullFaceState      = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_LIGHTING}.                  */ private TriState _lightingState      = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_SMOOTH}.                    */ private TriState _lineSmoothState    = TriState.UNDEFINED;
	/** Cached state of {@link GL#GL_TEXTURE_2D}.                */ private TriState _texture2dState     = TriState.UNDEFINED;
	/** Cached state of {@link GL#glBindTexture(int, int)}.      */ private int      _boundTextureTarget = -1;
	/** Cached state of {@link GL#glBindTexture(int, int)}.      */ private int      _boundTextureObject = -1;
	/** Cached state of {@link GL#glBlendFunc(int, int)}.        */ private int      _blendFuncSfactor   = -1;
	/** Cached state of {@link GL#glBlendFunc(int, int)}.        */ private int      _blendFuncDfactor   = -1;
	/** Cached state of {@link GL#glColorMaterial(int, int)}.    */ private int      _colorMaterialFace  = -1;
	/** Cached state of {@link GL#glColorMaterial(int, int)}.    */ private int      _colorMaterialMode  = -1;
	/** Cached state of {@link GL#glCullFace(int)}.              */ private int      _cullFace           = -1;
	/** Cached state of {@link GL#glDepthFunc(int)}.             */ private int      _depthFunc          = -1;
	/** Cached state of {@link GL#glLineWidth(float)}.           */ private float    _lineWidth          = -1;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}. */ private int      _materialFace       = -1;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}. */ private int      _materialPname      = -1;
	/** Cached state of {@link GL#glMaterialf(int, int, float)}. */ private float    _materialParam      = -1;
	/** Cached state of {@link GL#glShadeModel(int)}.            */ private int      _shadeModel         = -1;

	public GLWrapper( GL gl )
	{
		_gl = gl;
	}

	public GL getGL()
	{
		return _gl;
	}

	/**
	 * resets all cached gl values to TriState.UNDEFINED or null.
	 */
	public void reset()
	{
		_lightingState      = TriState.UNDEFINED;
		_cullFaceState      = TriState.UNDEFINED;
		_texture2dState     = TriState.UNDEFINED;
		_blendState         = TriState.UNDEFINED;
		_lineSmoothState    = TriState.UNDEFINED;
		_colorMaterialState = TriState.UNDEFINED;
		_shadeModel         = -1;
		_cullFace           = -1;
		_blendFuncSfactor   = -1;
		_blendFuncDfactor   = -1;
		_boundTextureTarget = -1;
		_boundTextureObject = -1;
		_materialFace       = -1;
		_materialPname = -1;
		_colorMaterialFace  = -1;
		_colorMaterialMode  = -1;
		_depthFunc          = -1;
		_materialParam      = (float)-1.0;
		_lineWidth          = (float)-1.0;
	}

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

	public void setCullFace( final boolean enable ) // 420
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

	public  void setLighting( final boolean enable ) // 420
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

	public  void glBindTexture( final int target , final int textureObject ) //252
	{
		if ( ( target != _boundTextureTarget ) || ( textureObject != _boundTextureObject ) )
		{
			_gl.glBindTexture( target , textureObject );
			//_boundTextureTarget = target;
			//_boundTextureObject = textureObject;
		}
	}

	public void glBlendFunc( final int sFactor , final int dFactor) // Geen Winst!
	{
		if ( sFactor != _blendFuncSfactor || dFactor != _blendFuncDfactor )
		{
			_gl.glBlendFunc( sFactor , dFactor );
			_blendFuncSfactor = sFactor;
			_blendFuncDfactor = dFactor;
		}
	}

	public void glColorMaterial ( final int face , final int mode )
	{
		if ( ( face != _colorMaterialFace ) || ( mode != _colorMaterialMode ) )
		{
			_gl.glColorMaterial( face , mode );

			_colorMaterialFace = face;
			_colorMaterialMode = mode;
		}
	}

	public  void glCullFace( final int cullFaceMode ) //420
	{
		if ( cullFaceMode != _cullFace )
		{
			_gl.glCullFace( cullFaceMode );
			_cullFace = cullFaceMode;
		}
	}

	public void glDepthFunc ( final int func )
	{
		if ( _depthFunc != func )
		{
			_gl.glDepthFunc ( GL.GL_LEQUAL );
			_depthFunc = func;
		}
	}

	public void glLineWidth ( final float width )
	{
		if ( _lineWidth != width )
		{
			_gl.glLineWidth( width );
			_lineWidth = width;
		}
	}

	public void glMaterialf ( final int face , final int pname , final float param ) //418
	{
		if ( ( face != _materialFace ) || ( pname != _materialPname ) || ( param != _materialParam ) )
		{
			_gl.glMaterialf( face , pname , param );

			_materialFace  = face;
			_materialPname = pname;
			_materialParam = param;
		}
	}

	public void glShadeModel( final int shadeModel ) //420
	{
		if ( shadeModel != _shadeModel )
		{
			_gl.glShadeModel( shadeModel );
			_shadeModel = shadeModel;
		}
	}

	public  void glEnable( final int target )
	{
		_gl.glEnable( target );
	}

	public  void glDisable( final int target )
	{
		_gl.glDisable( target );
	}

	public  void glBegin ( final int mode )
	{
		_gl.glBegin( mode );
	}

	/**
	 * Clear GL canvas.
	 *
	 * @param color     Color to clear canvas with.
	 */
	public void glClearColor( final Color color )
	{
		final float[] rgba = new float[4];
		color.getRGBComponents( rgba );
		glClearColor( rgba[ 0 ] , rgba[ 1 ] , rgba[ 2 ] , rgba[ 3 ] );
	}

	public void glClearColor( final float r , final float g , final float b , final float a )
	{
		_gl.glClearColor( r , g , b , a );
	}

	public  void glColor3f( final float r , final float g , final float b )
	{
		_gl.glColor3f( r , g , b );
	}

	public void glColor4f( final float a , final float r , final float g , final float b )
	{
		_gl.glColor4f( a , r , g , b );
	}

	public  void glColor4f( final float[] rgba )
	{
		_gl.glColor4f( rgba[ 0 ] , rgba[ 1 ] , rgba[ 2 ] , rgba[ 3 ] );
	}

	public  void glEnd()
	{
		_gl.glEnd();
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

	public  void glNormal3d(  final double vertexNormal , final double vertexNormal1 , final double vertexNormal2 )
	{
		_gl.glNormal3d( vertexNormal , vertexNormal1 , vertexNormal2 );
	}

	public void glPolygonMode( final int face , final int mode )
	{
		_gl.glPolygonMode( face , mode );
	}

	public  void glPopMatrix()
	{
		_gl.glPopMatrix();
	}

	public void glPushMatrix( )
	{
		_gl.glPushMatrix();
	}

	public  void glTexCoord2f( final float textureU , final float textureV )
	{
		_gl.glTexCoord2f( textureU , textureV );
	}

	public  void glVertex3d( final double vertexCoordinate , final double vertexCoordinate1 , final double vertexCoordinate2 )
	{
		_gl.glVertex3d( vertexCoordinate , vertexCoordinate1 , vertexCoordinate2 );
	}
}
