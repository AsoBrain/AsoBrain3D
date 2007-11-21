package ab.j3d.view.jogl;

import javax.media.opengl.GL;

public class GLWrapper
{
	private static final byte ENABLED = 1;

	private static final byte DISABLED = 0;

	private static final byte UNDEFINED = -1;

	private byte _gl_LIGHTING = UNDEFINED;

	private byte _gl_CULL_FACE = UNDEFINED;

	private byte _gl_TEXTURE_2D = UNDEFINED;

	private byte _gl_BLEND = UNDEFINED;

	private byte _gl_SMOOTH = UNDEFINED;

	private byte _gl_COLOR_MATERIAL = UNDEFINED;

	private int _gl_SHADE_MODEL = UNDEFINED;

	private int _gl_CULL_FACE_MODE = UNDEFINED;

	private int _gl_POLYGON_MODE_MODE = UNDEFINED;

	private int _gl_POLYGON_MODE_FACE = UNDEFINED;

	private int _gl_BLEND_FUNC_SFACTOR = UNDEFINED ;

	private int _gl_BLEND_FUNC_DFACTOR = UNDEFINED ;

	private int _gl_BIND_TEXTURE_TARGET = UNDEFINED;

	private int _gl_BIND_TEXTURE_OBJECT = UNDEFINED;

	private int _gl_MATERIALF_FACE = UNDEFINED;

	private int _gl_MATERIALF_PNAME = UNDEFINED;

	private int _gl_COLOR_MATERIAL_FACE = UNDEFINED;

	private int _gl_COLOR_MATERIAL_MODE = UNDEFINED;

	private int _gl_DEPTHFUNC = UNDEFINED;

	private float _gl_MATERIALF_PARAM = UNDEFINED;

	private float _gl_LINE_WIDTH = UNDEFINED;

	private double [] _mult_MATRIXD_DOUBLES;

	private GL _gl;

	public GLWrapper( GL gl )
	{
		_gl = gl;
	}

		public void glPushMatrix( )
	{
		_gl.glPushMatrix();
	}

	public  void setLighting( final boolean enable ) // 420
	{
		final byte lighting = enable ? ENABLED : DISABLED;
		if ( lighting != _gl_LIGHTING )
		{
			if ( enable )
			{
				_gl.glEnable( GL.GL_LIGHTING );
			}
			else
			{
				_gl.glDisable( GL.GL_LIGHTING );
			}
			_gl_LIGHTING = lighting;
		}
	}

	public void setCullFace( final boolean enable ) // 420
	{
		final byte cullFace = enable ? ENABLED : DISABLED;
		if ( cullFace != _gl_CULL_FACE )
		{
			if ( enable )
				_gl.glEnable( GL.GL_CULL_FACE );
			else
				_gl.glDisable( GL.GL_CULL_FACE );
			_gl_CULL_FACE = cullFace;
		}
	}


	public  void setCullFaceMode( final int cullFaceMode ) //420
	{
		if ( cullFaceMode != _gl_CULL_FACE_MODE )
		{
			_gl.glCullFace( cullFaceMode );
			_gl_CULL_FACE_MODE = cullFaceMode;
		}
	}

	public void setShadeModel( final int shadeModel ) //420
	{
		if ( shadeModel != _gl_SHADE_MODEL )
		{
			_gl.glShadeModel( shadeModel );
			_gl_SHADE_MODEL = shadeModel;
		}
	}

	public void setPolygonMode( final int face , final int mode )
	{
		_gl.glPolygonMode( face , mode );
	}

	public void setBlendFunc ( final int sFactor , final int dFactor) // Geen Winst!
	{
		if ( sFactor != _gl_BLEND_FUNC_SFACTOR || dFactor != _gl_BLEND_FUNC_DFACTOR )
		{
			_gl.glBlendFunc( sFactor , dFactor );
			_gl_BLEND_FUNC_SFACTOR = sFactor;
			_gl_BLEND_FUNC_DFACTOR = dFactor;
		}
	}

	public void setTexture2D( final boolean enable ) //Geen winst!
	{
		final byte texture2D = enable ? ENABLED : DISABLED;
		if ( texture2D != _gl_TEXTURE_2D )
		{
			if ( enable )
				_gl.glEnable( GL.GL_TEXTURE_2D );
			else
				_gl.glDisable( GL.GL_TEXTURE_2D );
			_gl_TEXTURE_2D = texture2D;
		}
	}

	public  void setBindTexture( final int target , final int textureObject ) //252
	{
		if ( target != _gl_BIND_TEXTURE_TARGET || textureObject != _gl_BIND_TEXTURE_OBJECT )
		{
			_gl.glBindTexture( target , textureObject );
			_gl_BIND_TEXTURE_TARGET = target;
			_gl_BIND_TEXTURE_OBJECT = textureObject;
		}
	}

	public  void setDisableTexture( final int target )
	{
		_gl.glDisable( target );
	}

	public  void glEnd ( )
	{
		_gl.glEnd();
	}

	public  void glBegin ( final int mode )
	{
		_gl.glBegin( mode );
	}

	public  void glTexCoord2f( final float textureU , final float textureV )
	{
		_gl.glTexCoord2f( textureU , textureV );
	}

	public  void glNormal3d(  final double vertexNormal , final double vertexNormal1 , final double vertexNormal2 )
	{
		_gl.glNormal3d( vertexNormal , vertexNormal1 , vertexNormal2 );
	}

	public  void glVertex3d( final double vertexCoordinate , final double vertexCoordinate1 , final double vertexCoordinate2 )
	{
		_gl.glVertex3d( vertexCoordinate , vertexCoordinate1 , vertexCoordinate2 );
	}

	public void glMultMatrixd( final double[] doubles , final int i ) //Geen winst
	{
		if ( doubles != _mult_MATRIXD_DOUBLES )
		{
			_gl.glMultMatrixd( doubles , i );
			_mult_MATRIXD_DOUBLES = doubles.clone();
		}
	}

	public void glColor4f( final float a , final float r , final float g , final float b )
	{
		_gl.glColor4f( a , r , g , b );
	}


	public  void glColor4f( final float [] rgba )
	{
		_gl.glColor4f( rgba[0] , rgba[1] , rgba[2] , rgba[3] );
	}

	public  void glColor3f( final float r , final float g , final float b )
	{
		_gl.glColor3f( r , g , b );
	}

	public void glMaterialf ( final int face , final int pname , final float param ) //418
	{
		if ( face != _gl_MATERIALF_FACE || pname != _gl_MATERIALF_PNAME || param != _gl_MATERIALF_PARAM )
		{
			_gl.glMaterialf( face , pname , param );
			_gl_MATERIALF_FACE = face;
			_gl_MATERIALF_PNAME = pname;
			_gl_MATERIALF_PARAM = param;
		}
	}

	public void glColorMaterial ( final int face , final int mode )
	{
		if ( face != _gl_COLOR_MATERIAL_FACE || mode != _gl_COLOR_MATERIAL_MODE  )
		{
			_gl.glColorMaterial( face , mode );
			_gl_COLOR_MATERIAL_FACE = face;
			_gl_COLOR_MATERIAL_MODE = mode;
		}
	}

	public  void glPopMatrix()
	{
		_gl.glPopMatrix();
	}

	public void setBlend( final boolean enable )
	{
		final byte blend = enable ? ENABLED : DISABLED;
		if ( blend != _gl_BLEND )
		{
			if ( enable )
				_gl.glEnable( GL.GL_BLEND );
			else
				_gl.glDisable( GL.GL_BLEND );
			_gl_BLEND = blend;
		}
	}

	public void glLineWidth ( final float width )
	{
		if ( _gl_LINE_WIDTH != width )
		{
			_gl.glLineWidth( width );
			_gl_LINE_WIDTH = width;
		}
	}

	public void setSmooth( final boolean enable )
	{
		final byte smooth = enable ? ENABLED : DISABLED;
		if ( smooth!= _gl_SMOOTH )
		{
			if ( enable )
				_gl.glEnable( GL.GL_LINE_SMOOTH );
			else
				_gl.glDisable( GL.GL_LINE_SMOOTH );
			_gl_SMOOTH = smooth;
		}
	}

	public void glDepthFunc ( final int func )
	{
		if ( _gl_DEPTHFUNC != func )
		{
			_gl.glDepthFunc ( GL.GL_LEQUAL );
			_gl_DEPTHFUNC = func;
		}
	}

	public void setColorMaterial( final boolean enable )
	{
		final byte colormaterial = enable ? ENABLED : DISABLED;
		if ( colormaterial!= _gl_COLOR_MATERIAL )
		{
			if ( enable )
				_gl.glEnable( GL.GL_COLOR_MATERIAL );
			else
				_gl.glDisable( GL.GL_COLOR_MATERIAL );
			_gl_COLOR_MATERIAL = colormaterial;
		}
	}

	public void glClearColor( final float r , final float g , final float b , final float a )
	{
		_gl.glClearColor( r , g , b , a );
	}

	public GL getgl()
	{
		return _gl;
	}
}
