/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2006 Peter S. Heijnen
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

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
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
	Object3D    _object;
	Matrix3D    _object2view;

	int         _vertexCount;
	double[]    _vertexCoordinates;
	int[]       _projectedX;
	int[]       _projectedY;
	long[]      _vertexDepths;

	boolean     _vertexNormalsDirty;
	double[]    _vertexNormals;

	int         _lightCount;
	Light3D[]   _lights;
	double[][]  _lightNormalDist;

	Face        _faces;
	Face        _facesFree;

	/**
	 * Sindle face of rendered object.
	 */
	public final class Face
	{
		Face3D      _face3d;
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
		double      _nx;
		double      _ny;
		double      _nz;
		double      _dist;

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
			_nx     = 0.0;
			_ny     = 0.0;
			_nz     = 0.0;
			_dist   = 0.0;
			_ds     = null;
			_sxs    = null;
			_sys    = null;
			_sfs    = null;
		}
		private void set( final Face3D face3d , final int index , final int minH , final int minV , final int minD , final int maxH , final int maxV , final int maxD , final double nx , final double ny , final double nz )
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

			_vi = face3d.getVertexIndices();
			final int i = _vi[ 0 ] * 3;
			final double[] vertexCoordinates = _vertexCoordinates;
			_dist = nx * vertexCoordinates[ i ] + ny * vertexCoordinates[ i + 1 ] + nz * vertexCoordinates[ i + 2 ];
		}

		public boolean isBehind( final Face other )
		{
			final boolean result;

			if ( ( _minH > other._maxH )
			  || ( _maxH < other._minH )
			  || ( _minV > other._maxV )
			  || ( _maxV < other._minV ) )
			{
				result = false;
			}
			else if ( _minD > other._maxD )
			{
				result = true;
			}
			else if ( _maxD < other._minD )
			{
				result = false;
			}
			else
			{
				final int      i = other._vi[ 0 ] * 3;
				final double[] v = other.getRenderObject()._vertexCoordinates;
				final double   x = v[ i     ];
				final double   y = v[ i + 1 ];
				final double   z = v[ i + 2 ];

				result = ( y > ( _dist - _nx * x - _nz * z ) / _ny );
			}

			return result;
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
			final int lightCount = _lightCount;
			if ( lightCount < 1 )
				return;

			final Material material = getMaterial();

			if ( _face3d.isSmooth() )
			{
				final double[] vertNorm = getVertexNormals();

				for ( i = lightCount ; --i >= 0 ; )
				{
					final Light3D light      = _lights[ i ];
					final double[] normalDist = _lightNormalDist[ i ];

					for ( j = _vi.length ; --j >= 0 ; )
					{
						k = _vi[ j ] * 3;

						calculateShadingProperties( light , material ,
							normalDist , _vi[ j ] * 4 ,
							vertNorm[ k ] , vertNorm[ k + 1 ] , vertNorm[ k + 2 ] ,
							_ds , _sxs , _sys , _sfs , j );
					}
				}
			}
			else
			{
				for ( i = lightCount ; --i >= 0 ; )
				{
					final Light3D light      = _lights[ i ];
					final double[] normalDist = _lightNormalDist[ i ];

					for ( j = _vi.length ; --j >= 0 ; )
					{
						calculateShadingProperties( light , material ,
							normalDist , _vi[ j ] * 4 ,
							_nx , _ny , _nz ,
							_ds , _sxs , _sys , _sfs , j );
					}
				}
			}
		}

		/**
		 * Calculate vertex shading properties based on a light source. Several
		 * metrics about the vertex must be provided. Note that lightNormals will
		 * only be provided if the requiresNormalsOrDistance() method of this light
		 * returns <code>true</code>.
		 *
		 * <code>viewNormals</code> is an array of tripplets for the x,y,z values
		 * respectively.
		 *
		 * <code>lightNormalAndDist</code> contains light normals and distances
		 * stored as quads; x,y,z of the light direction vector (normal), and
		 * distance as last value.
		 *
		 * All indices must be absolute (so you may need to multiply them by three
		 * or four before passing them as arguments).
		 * <p>
		 * Diffuse reflection is based on a 0-256 scale and all affecting light
		 * sources should add to this value.
		 * <p>
		 * Because of current limitations, only one specular light source is supported
		 * (since there is only one set of specular properties). Therefore, only one
		 * light source can fill this in (this is currently priority based, with the
		 * brightest light source overruling fainter light sources).
		 *
		 * @param   material            Material of surface.
		 * @param   lightNormalAndDist  Float array with light normals and distance.
		 * @param   lightIndex          Index in light array.
		 * @param   nx                  X-coordinate of normal
		 * @param   ny                  Y-coordinate of normal
		 * @param   nz                  Z-coordinate of normal
		 * @param   ds                  Diffuse reflection result array.
		 * @param   sxs                 Specular reflection X-component result array.
		 * @param   sys                 Specular reflection Y-component result array.
		 * @param   sfs                 Specular reflection fraction result array.
		 * @param   targetIndex         Index in result arrays.
		 */
		public void calculateShadingProperties(
			final Light3D light ,
			final Material material ,
			final double[] lightNormalAndDist , final int lightIndex ,
			final double nx , final double ny , final double nz ,
			final int[] ds , final int[] sxs , final int[] sys , final int[] sfs, final int targetIndex )
		{
			final int    intensity = light.getIntensity();
			final double fallOff   = light.getFallOff();

			/*
			 * Handle ambient light.
			 */
			if ( fallOff < 0.0 )
			{
				/*
				 * Calculate diffuse reflection of the ambient light
				 * using the following formula:
				 *
				 *     Id = Il * Ka
				 *
				 * Where:
				 *     Il       = light intensity
				 *     Ka       = ambient reflectivity of material
				 *
				 * We just add this to any existing diffuse reflection value.
				 */
				final double id = (double)intensity;

				ds[ targetIndex ] += (int)( id * 256.0 );
			}
			/*
			 * Handle point light
			 */
			else
			{
				/*
				 * Get direction of light.
				 */
				final double lightNormalX  = lightNormalAndDist[ lightIndex     ];
				final double lightNormalY  = lightNormalAndDist[ lightIndex + 1 ];
				final double lightNormalZ  = lightNormalAndDist[ lightIndex + 2 ];
				final double lightDistance = lightNormalAndDist[ lightIndex + 3 ];

				/*
				 * Get cos( angle ) between light and normal (this is
				 * simply the inner product of the light direction and
				 * normal vectors.
				 */
				final double cosLightAngle = nx * lightNormalX + ny * lightNormalY + nz * lightNormalZ;

				/*
				 * Only apply light if it does not come from the back side.
				 */
				if ( cosLightAngle > 0.0 )
				{
					/*
					 * Calculate light intensity of light at the given distance.
					 */
					final double il = (double)intensity * ( fallOff / ( fallOff + lightDistance ) );

					/*
					 * Start with the diffuse reflection part of the point light.
					 * As basis, we use the following formula:
					 *
					 *     Id = Il * Kd * cos( gamma )
					 *
					 * Where:
					 *     Il       = light intensity
					 *     Kd       = diffusion reflection coefficient of material,
					 *     gamma    = angle between normal and light
					 *
					 * We just add this to any existing diffuse reflection value.
					 */
					final double id = il * cosLightAngle;

					ds[ targetIndex ] += (int)( id * 256.0 );

					/*
					 * To add specular reflection, we have the following formula:
					 *
					 *     Is = Il * Ks * cos^n( theta )
					 *
					 * Where:
					 *     Il       = light intensity
					 *     Ks       = specular reflection coefficient of material,
					 *     n        = specular reflection exponent
					 *     theta    = angle between light and view
					 *
					 * However, the cos^n factor is not calculated here. Instead, the
					 * the x and y coordinates of the light direction vector are stored
					 * in the shading properties array. These are used by the rendering
					 * engine to interpolate the light normal accross a face. The result
					 * of the first part of the formula is also stored.
					 *
					 * Since only one specular light source is supported at this moment,
					 * the properties will only be stored if this light is brighter than
					 * any previous specular light source.
					 */
					final double is = il;
					final int isInt = (int)( is * 256.0 );

					if ( isInt > sfs[ targetIndex ] )
					{
						sxs[ targetIndex ] = (int)(32767.5 * lightNormalX + 32768.0 );
						sys[ targetIndex ] = (int)(32767.5 * lightNormalY + 32768.0 );
						sfs[ targetIndex ] = ( isInt > 131072 ) ? 131072 : isInt;
					}
				}
			}
		}

		public Material getMaterial()
		{
			return _face3d.getMaterial();
		}

		public float[] getTextureU()
		{
			return _face3d.getTextureU();
		}

		public float[] getTextureV()
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
		_object             = null;
		_object2view          = null;

		_vertexCount        = 0;
		_vertexCoordinates  = null;
		_projectedX         = null;
		_projectedY         = null;
		_vertexDepths       = null;

		_vertexNormals      = null;
		_vertexNormalsDirty = false;

		_lightCount         = 0;
		_lights             = null;
		_lightNormalDist    = null;

		_faces              = null;
		_facesFree          = null;
	}
	/**
	 * Get Object3D that was used to construct the render object.
	 *
	 * @return  Object3D use to construct the render object.
	 */
	public Object3D getObject()
	{
		return _object;
	}

	/**
	 * Get vertex normals relative to the render view. The result is returned as
	 * a float array with 3 entries per vertex.
	 *
	 * @return  Float array with vertex normals in render view.
	 */
	public double[] getVertexNormals()
	{
		double[] result = _vertexNormals;

		if ( _vertexNormalsDirty )
		{
			result = _object.getVertexNormals( _object2view , result );

			_vertexNormals      = result;
			_vertexNormalsDirty = false;
		}

		return result;
	}

	/**
	 * Set properties of the render object.
	 *
	 * @param   object                 Object3D used to define the object.
	 * @param   object2view               View transform (rotation, translation).
	 * @param   aperture            Camera aperture.
	 * @param   zoom                Linear zoom factor.
	 * @param   width               Width of render area in pixels.
	 * @param   height              Height of render area in pixels.
	 * @param   backfaceCullling    Discard backfaces if set to <code>true</code>.
	 */
	public void set( final Object3D object , final Matrix3D object2view , final double aperture , final double zoom , final int width , final int height , final boolean backfaceCullling )
	{
		int i;
		int j;
		int k;
		int l;
		int ih;
		int iv;
		int id;

		final int      vertexCount       = object.getVertexCount();
		final double[] vertexCoordinates = ( _vertexCoordinates = object.getVertexCoordinates( object2view , _vertexCoordinates ) );
		final int      centerH           = width << 7;
		final int      centerV           = height << 7;

		_object             = object;
		_object2view        = object2view;
		_lightCount         = 0;
		_vertexCount        = vertexCount;
		_vertexNormalsDirty = true;

		/*
		 * Prepare vertex buffers, transform vertices, project vertices.
		 */
		if ( ( _vertexDepths == null ) || ( vertexCount > _vertexDepths.length ) )
		{
			_projectedX   = new int [ vertexCount ];
			_projectedY   = new int [ vertexCount ];
			_vertexDepths = new long[ vertexCount ];
		}


		final double perspectiveFactor = 256.0 * zoom / aperture;

		for ( i = 0 , j = 0 ; j < vertexCount ; i += 3 , j++ )
		{
			final double x = vertexCoordinates[ i     ];
			final double y = vertexCoordinates[ i + 1 ];
			final double z = vertexCoordinates[ i + 2 ];

			id = (int)y;

			if ( id < 10 )
			{
				_vertexDepths[ j ] = 0L;
				_projectedX  [ j ] = centerH;
				_projectedY  [ j ] = centerV;
			}
			else
			{
				final double d = perspectiveFactor / y;

				_vertexDepths[ j ] = (long)( 549755813887.0 / y ); /*0x7FFFFFFFFF / id;*/
				_projectedX  [ j ] = centerH + (int)( x * d );
				_projectedY  [ j ] = centerV - (int)( z * d );
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
				while ( previous._next != null )
					previous = previous._next;

				previous._next = _facesFree;
			}
			_facesFree = _faces;
			_faces = null;
		}

		nextFace: for ( i = object.getFaceCount() ; --i >= 0 ; )
		{
			final Face3D face3d          = object.getFace( i );
			final int    faceVertexCount = face3d.getVertexCount();
			final int[]  vertexIndices   = face3d.getVertexIndices();

			if ( faceVertexCount >= 3 )
			{
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
					final int h1 = _projectedX[ l = vertexIndices[ 0 ] ] >> 8;
					final int v1 = _projectedY[ l ] >> 8;
					final int h2 = _projectedX[ l = vertexIndices[ 1 ] ] >> 8;
					final int v2 = _projectedY[ l ] >> 8;
					final int h3 = _projectedX[ l = vertexIndices[ 2 ] ] >> 8;
					final int v3 = _projectedY[ l ] >> 8;

					if ( ( h1 - h2 ) * ( v3 - v2 ) >= ( v1 - v2 ) * ( h3 - h2 ) )
						continue;
				}

				/*
				 * Determine bounding box of face. Don't draw if outside screen range.
				 */
				l = vertexIndices[ 0 ];
				if ( _vertexDepths[ l ] != 0L )
				{
					int minH = _projectedX[ l ] >> 8;
					int maxH = minH;
					int minV = _projectedY[ l ] >> 8;
					int maxV = minV;
					int minD = (int)_vertexCoordinates[ l * 3 + 1 ];
					int maxD = minD;

					for ( k = faceVertexCount ; --k >= 1; )
					{
						l = vertexIndices[ k ];

						if ( _vertexDepths[ l ] == 0L )
							continue nextFace;

						ih = _projectedX[ l ] >> 8;
						iv = _projectedY[ l ] >> 8;
						id = (int)_vertexCoordinates[ l * 3 + 1 ];

						if ( ih < minH ) minH = ih;
						if ( ih > maxH ) maxH = ih;
						if ( iv < minV ) minV = iv;
						if ( iv > maxV ) maxV = iv;
						if ( id < minD ) minD = id;
						if ( id > maxD ) maxD = id;
					}

					if ( minH < width && maxH >= 0 && minV < height && maxV >= 0 )
					{
						/*
						 * Calculate face normal
						 */
						final Vector3D normal = face3d.getNormal();

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

						current.set( face3d , i , minH , minV , minD , maxH , maxV , maxD , object2view.rotateX( normal.x , normal.y , normal.z ) , object2view.rotateY( normal .x , normal.y , normal.z ) , object2view.rotateZ( normal .x , normal.y , normal.z ) );
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
			}
		}
	}

	/**
	 * Set light sources to use for lighting effects of the object. This will be used
	 * by the <code>applyLighting()</code> method of the <code>Face</code> class.
	 *
	 * @param   lightSources    Collection of light sources.
	 */
	public void setLights( final Node3DCollection<Light3D> lightSources )
	{
		int i;
		int j;
		int k;

		/*
		 * Set number of light sources.
		 */
		_lightCount = ( lightSources != null ) ? lightSources.size() : 0;

		/*
		 * Prepare light source buffer.
		 */
		Light3D[]  lights          = _lights;
		double[][] lightNormalDist = _lightNormalDist;

		if ( _lightCount > 0 && ( lights == null || _lightCount > lights.length ) )
		{
			lights = new Light3D[ _lightCount ];
			if ( _lights != null )
				System.arraycopy( _lightNormalDist , 0 , lightNormalDist , 0 , _lights.length );
			_lights = lights;

			lightNormalDist = new double[ _lightCount ][];
			_lightNormalDist = lightNormalDist;
		}

		/*
		 * Prepare light directions and distance for light sources that need them.
		 */
		final int nrVerts4 = _vertexCount * 4;

		if ( lightSources != null ) for ( i = 0 ; i < _lightCount ; i++ )
		{
			lights[ i ] = lightSources.getNode( i );

			if ( requiresNormalsOrDistance( lights[ i ] ) )
			{
				final Matrix3D m = lightSources.getMatrix( i );
				final double x = m.xo;
				final double y = m.yo;
				final double z = m.zo;

				double[] dest = lightNormalDist[ i ];
				if ( dest == null || dest.length < nrVerts4 )
					lightNormalDist[ i ] = dest = new double[ nrVerts4 ];

				for ( j = 0 , k = 0 ; k < nrVerts4 ; )
				{
					final double dx = x - _vertexCoordinates[ j++ ];
					final double dy = y - _vertexCoordinates[ j++ ];
					final double dz = z - _vertexCoordinates[ j++ ];

					final double d = Math.sqrt( dx * dx + dy * dy + dz * dz );
					if ( d < 1.0 )
					{
						dest[ k++ ] = dx;
						dest[ k++ ] = dy;
						dest[ k++ ] = dz;
						dest[ k++ ] = 1.0;
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

	/**
	 * This function should return <code>true</code> if this light model requires
	 * the normal of a surface to calculate its color. This is the case for most
	 * light sources, but an "ambient" light is one exception to this rule (there
	 * may be more). If this function returns <code>false</code> time can be saved
	 * by not calculating the normals.
	 *
	 * @param   light       Light to consider.
	 *
	 * @return  <code>true</code> if surface normals are required by
	 *          calculateColor(), <code>false</code> otherwise.
	 */
	public static boolean requiresNormalsOrDistance( final Light3D light )
	{
		return ( light.getFallOff() >= 0.0 );
	}
}
