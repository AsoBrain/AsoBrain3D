/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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
package ab.j3d.view.renderer;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This class contains specifications to render a 3D object on a 2D surface. It is
 * constructed for each Object3D instance by the Renderer class as preparation for
 * the actual rendering process.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class RenderObject
{
	Object3D    _obj;
	Matrix3D    _xform;

	int         _nrVerts;
	float[]     _verts;
	int[]       _ph;
	int[]       _pv;
	long[]      _pd;

	boolean     _vertNormDirty;
	float[]     _vertNorm;

	int         _nrLights;
	Light3D[]   _lights;
	float[][]   _lightNormalDist;

	Face        _faces;
	Face        _facesFree;

	/**
	 * Sindle face of rendered object.
	 */ 
	public final class Face
	{
		Object3D.Face _face3d;
		Face        _next;
		//Face      isBehind;
		//Face      isBefore;
		int         _index;
		int[]       _vi;
		int         _minH;
		int         _minV;
		int         _minD;
		int         _maxH;
		int         _maxV;
		int         _maxD;
		float       _nx;
		float       _ny;
		float       _nz;
		float       _dist;

		/*
		 * All following variables are only valid after calling applyLighting()
		 */
		int[]       _ds;
		int[]       _sxs;
		int[]       _sys;
		int[]       _sfs;

		public Face()
		{
			_face3d = null;
			_next   = null;
			_index  = 0;
			_vi     = null;
			_minH   = 0;
			_minV   = 0;
			_minD   = 0;
			_maxH   = 0;
			_maxV   = 0;
			_maxD   = 0;
			_nx     = 0;
			_ny     = 0;
			_nz     = 0;
			_dist   = 0;
			_ds     = null;
			_sxs    = null;
			_sys    = null;
			_sfs    = null;
		}
		private void set( final Object3D.Face face3d , final int index , final int minH , final int minV , final int minD , final int maxH , final int maxV , final int maxD , final float nx , final float ny , final float nz )
		{
			_face3d = face3d;
			_index  = index;
			_minH   = minH;
			_minV   = minV;
			_minD   = minD;
			_maxH   = maxH;
			_maxV   = maxV;
			_maxD   = maxD;
			_nx     = nx;
			_ny     = ny;
			_nz     = nz;

			_vi = face3d.getPointIndices();
			final int i = _vi[ 0 ] * 3;
			_dist = nx * _verts[ i ] + ny * _verts[ i + 1 ] + nz * _verts[ i + 2 ];
		}

		public boolean isBehind( final Face other )
		{
			if ( _minH > other._maxH ) return false;
			if ( _maxH < other._minH ) return false;
			if ( _minV > other._maxV ) return false;
			if ( _maxV < other._minV ) return false;
			if ( _minD > other._maxD ) return true;
			if ( _maxD < other._minD ) return false;


			final int i = other._vi[ 0 ] * 3;
			final float[] v = other.getRenderObject()._verts;
			final float x = v[ i     ];
			final float y = v[ i + 1 ];
			final float z = v[ i + 2 ];

			return y > ( _dist - _nx * x - _nz * z ) / _ny;
		}

		public void applyLighting()
		{
			int i;
			int j;
			int k;

			/*
			 * Initialize shading property buffers
			 */
			i = _vi.length;
			if ( i < 3 )
				return;

			if ( _ds == null || i > _ds.length )
			{
				_ds  = new int[ i ];
				_sxs = new int[ i ];
				_sys = new int[ i ];
				_sfs = new int[ i ];
			}

			while ( --i >= 0 )
			{
				_ds [ i ] = 0;
				_sxs[ i ] = 32768;
				_sys[ i ] = 32768;
				_sfs[ i ] = 0;
			}

			/*
			 * Calculate shading properties for all light sources.
			 */
			if ( _nrLights < 1 )
				return;

			final TextureSpec texture = getTexture();

			if ( _face3d.isSmooth() )
			{
				final float[] vertNorm = getVertexNormals();

				for ( i = _nrLights ; --i >= 0 ; )
				{
					final Light3D light      = _lights[ i ];
					final float[] normalDist = _lightNormalDist[ i ];

					for ( j = _vi.length ; --j >= 0 ; )
					{
						k = _vi[ j ] * 3;

						light.calculateShadingProperties( texture ,
							normalDist , _vi[ j ] * 4 ,
							vertNorm[ k ] , vertNorm[ k + 1 ] , vertNorm[ k + 2 ] ,
							_ds , _sxs , _sys , _sfs , j );
					}
				}
			}
			else
			{
				for ( i = _nrLights ; --i >= 0 ; )
				{
					final Light3D light      = _lights[ i ];
					final float[] normalDist = _lightNormalDist[ i ];

					for ( j = _vi.length ; --j >= 0 ; )
					{
						light.calculateShadingProperties( texture ,
							normalDist , _vi[ j ] * 4 ,
							_nx , _ny , _nz ,
							_ds , _sxs , _sys , _sfs , j );
					}
				}
			}
		}

		public TextureSpec getTexture()
		{
			return _face3d.getTexture();
		}

		public int[] getTextureU()
		{
			return _face3d.getTextureU();
		}

		public int[] getTextureV()
		{
			return _face3d.getTextureV();
		}

		public RenderObject getRenderObject()
		{
			return RenderObject.this;
		}
	}

	public RenderObject()
	{
		_obj             = null;
		_xform           = null;

		_nrVerts         = 0;
		_verts           = null;
		_ph              = null;
		_pv              = null;
		_pd              = null;

		_vertNormDirty   = false;
		_vertNorm        = null;

		_nrLights        = 0;
		_lights          = null;
		_lightNormalDist = null;

		_faces           = null;
		_facesFree       = null;
	}
	/**
	 * Get Object3D that was used to construct the render object.
	 *
	 * @return  Object3D use to construct the render object.
	 */
	public Object3D getObject()
	{
		return _obj;
	}

	/**
	 * Get vertex normals relative to the render view. The result is returned as
	 * a float array with 3 entries per vertex.
	 *
	 * @return  Float array with vertex normals in render view.
	 */
	public float[] getVertexNormals()
	{
		if ( _vertNormDirty )
		{
			_vertNormDirty = false;

			final int nrVerts3 = _nrVerts * 3;

			if ( _vertNorm == null || nrVerts3 > _vertNorm.length )
				_vertNorm = new float[ nrVerts3 ];

			_xform.rotate( _obj.getVertexNormals() , _vertNorm , _nrVerts );
		}

		return _vertNorm;
	}

	/**
	 * Set properties of the render object.
	 *
	 * @param   obj                 Object3D used to define the object.
	 * @param   xform               View transform (rotation, translation).
	 * @param   aperture            Camera aperture.
	 * @param   zoom                Linear zoom factor.
	 * @param   width               Width of render area in pixels.
	 * @param   height              Height of render area in pixels.
	 * @param   backfaceCullling    Discard backfaces if set to <code>true</code>.
	 */
	public void set( final Object3D obj , final Matrix3D xform , final float aperture , final float zoom , final int width , final int height , final boolean backfaceCullling )
	{
		int i;
		int j;
		int k;
		int l;
		int ih;
		int iv;
		int id;
		float x;
		float y;
		float z;

		_obj           = obj;
		_xform         = xform;
		_nrLights      = 0;
		_vertNormDirty = true;

		_nrVerts = obj.getTotalVertexCount();
		final int     nrVerts3    = _nrVerts * 3;
		final float[] oVerts      = obj.getVertices();
		final float[] faceNormals = obj.getFaceNormals();
		final int     centerH     = width <<  7;
		final int     centerV     = height << 7;

		obj.getFaceNormals();

		final float xx = xform.xx;
		final float xy = xform.xy;
		final float xz = xform.xz;
		final float xo = xform.xo;
		final float yx = xform.yx;
		final float yy = xform.yy;
		final float yz = xform.yz;
		final float yo = xform.yo;
		final float zx = xform.zx;
		final float zy = xform.zy;
		final float zz = xform.zz;
		final float zo = xform.zo;

		/*
		 * Prepare vertex buffers, transform vertices, project vertices.
		 */
		if ( _verts == null || nrVerts3 > _verts.length )
		{
			_verts = new float[ nrVerts3 ];
			_ph    = new int  [ _nrVerts  ];
			_pv    = new int  [ _nrVerts  ];
			_pd    = new long [ _nrVerts  ];
		}


		final float perspectiveFactor = 256.0f * zoom / aperture;

		for ( i = 0 , j = 0 ; i < nrVerts3 ; i += 3 , j++ )
		{
			final float ox = oVerts[ i     ];
			final float oy = oVerts[ i + 1 ];
			final float oz = oVerts[ i + 2 ];

			x = _verts[ i     ] = ox * xx + oy * xy + oz * xz + xo;
			y = _verts[ i + 1 ] = ox * yx + oy * yy + oz * yz + yo;
			z = _verts[ i + 2 ] = ox * zx + oy * zy + oz * zz + zo;

			id = (int)y;

			if ( id < 10 )
			{
				_pd[ j ] = 0;
				_ph[ j ] = centerH;
				_pv[ j ] = centerV;
			}
			else
			{
				_pd[ j ] = (long)( 549755813887.0 / y ); /*0x7FFFFFFFFF / id;*/
				_ph[ j ] = centerH + (int)( x * ( y = perspectiveFactor / y ) );
				_pv[ j ] = centerV - (int)( z *   y                           );
			}
		}

		/*
		 * Prepare face buffer and insert faces into this buffer (the number
		 * of faces may be reduced during this as a result of early hidden
		 * surface removal (e.g. back culling)).
		 */
		Face previous;
		Face current/*,next,free*/;

		if ( _faces != null )
		{
			if ( _facesFree != null )
			{
				previous = _faces;
				while ( previous._next != null ) previous = previous._next;
				previous._next = _facesFree;
			}
			_facesFree = _faces;
			_faces = null;
		}

		nextFace: for ( i = obj.getFaceCount() ; --i >= 0 ; )
		{
			final Object3D.Face face3d = obj.getFace( i );
			final int[] vi = face3d.getPointIndices();
			if ( vi.length < 3 )
				continue nextFace;

			/*
			 * Backface culling.
			 *
			 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
			 *
			 * If c is positive the polygon is visible. (note that we negate this
			 * test below, since the y-coordinates are drawn top to bottom and are
			 * therefore mirrored).
			 */
			if ( backfaceCullling )
			{
				final int h1 = _ph[ l = vi[ 0 ] ] >> 8;
				final int v1 = _pv[ l           ] >> 8;
				final int h2 = _ph[ l = vi[ 1 ] ] >> 8;
				final int v2 = _pv[ l           ] >> 8;
				final int h3 = _ph[ l = vi[ 2 ] ] >> 8;
				final int v3 = _pv[ l           ] >> 8;

				if ( (h1 - h2) * (v3 - v2) >= (v1 - v2) * (h3 - h2) )
					continue nextFace;
			}

			/*
			 * Determine bounding box of face. Don't draw if outside screen range.
			 */
			l = vi[ 0 ];
			if ( _pd[ l ] == 0 )
				continue nextFace;

			int minH = _ph[ l ] >> 8;
			int maxH = minH;
			int minV = _pv[ l ] >> 8;
			int maxV = minV;
			int minD = (int)_verts[ l * 3 + 1 ];
			int maxD = minD;

			for ( k = vi.length ; --k >= 1 ; )
			{
				l = vi[ k ];

				if ( _pd[ l ] == 0 )
					continue nextFace;

				ih = _ph[ l ] >> 8;
				iv = _pv[ l ] >> 8;
				id = (int)_verts[ l * 3 + 1 ];

				if ( ih < minH ) minH = ih;
				if ( ih > maxH ) maxH = ih;
				if ( iv < minV ) minV = iv;
				if ( iv > maxV ) maxV = iv;
				if ( id < minD ) minD = id;
				if ( id > maxD ) maxD = id;
			}

			if ( minH >= width || maxH < 0 || minV >= height || maxV < 0 )
				continue nextFace;

			/*
			 * Calculate face normal
			 */
			final int ni = i * 3;
			x = faceNormals[ ni     ];
			y = faceNormals[ ni + 1 ];
			z = faceNormals[ ni + 2 ];

			/*
			 * Insert face
			 *  1) Reuse face from 'free list' or construct new 'Face' object
			 *  2) Set face properties
			 *  3) Find insertion point in face chain
			 *  4) Insert face in face chain
			 */
			if ( _facesFree != null )
				_facesFree = ( current = _facesFree )._next;
			else
				current = new Face();

			current.set( face3d , i , minH , minV , minD , maxH , maxV , maxD ,
				x * xx + y * xy + z * xz ,
				x * yx + y * yy + z * yz ,
				x * zx + y * zy + z * zz );

			current._next = _faces;
			_faces = current;

			//for ( previous = null , next = faces ; next != null && next.isBehind( current ) ; )
				//next = ( previous = next ).next;

			//if ( previous == null )
				//faces = current;
			//else
				//previous.next = current;

			//current.next = next;
		}
	}

	/**
	 * Set light sources to use for lighting effects of the object. This will be used
	 * by the <code>applyLighting()</code> method of the <code>Face</code> class.
	 *
	 * @param   lightSources    Collection of light sources.
	 */
	public void setLights( final Node3DCollection lightSources )
	{
		int i;
		int j;
		int k;

		/*
		 * Set number of light sources.
		 */
		_nrLights = ( lightSources != null ) ? lightSources.size() : 0;

		/*
		 * Prepare light source buffer.
		 */
		Light3D[] lights          = _lights;
		float[][] lightNormalDist = _lightNormalDist;

		if ( _nrLights > 0 && ( lights == null || _nrLights > lights.length ) )
		{
			lights = new Light3D[ _nrLights ];
			if ( _lights != null )
				System.arraycopy( _lightNormalDist , 0 , lightNormalDist , 0 , this._lights.length );
			_lights = lights;

			lightNormalDist = new float[ _nrLights ][];
			_lightNormalDist = lightNormalDist;
		}

		/*
		 * Prepare light directions and distance for light sources that need them.
		 */
		final int nrVerts4 = _nrVerts * 4;

		if ( lightSources != null ) for ( i = 0 ; i < _nrLights ; i++ )
		{
			lights[ i ] = (Light3D)lightSources.getNode( i );

			if ( lights[ i ].requiresNormalsOrDistance() )
			{
				final Matrix3D m = lightSources.getMatrix( i );
				final float x = m.xo;
				final float y = m.yo;
				final float z = m.zo;

				float[] dest = lightNormalDist[ i ];
				if ( dest == null || dest.length < nrVerts4 )
					lightNormalDist[ i ] = dest = new float[ nrVerts4 ];

				for ( j = 0 , k = 0 ; k < nrVerts4 ; )
				{
					final float dx = x - _verts[ j++ ];
					final float dy = y - _verts[ j++ ];
					final float dz = z - _verts[ j++ ];

					final float d = (float)Math.sqrt( dx * dx + dy * dy + dz * dz );
					if ( d < 1.0f )
					{
						dest[ k++ ] = dx;
						dest[ k++ ] = dy;
						dest[ k++ ] = dz;
						dest[ k++ ] = 1.0f;
					}
					else
					{
						dest[ k++ ] = dx / d;
						dest[ k++ ] = dy / d;
						dest[ k++ ] = dz / d;
						dest[ k++ ] =      d;
					}
				}
			}
		}
	}
}
