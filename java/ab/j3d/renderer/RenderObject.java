package ab.j3d.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class contains specifications to render a 3D object on a 2D surface. It is
 * constructed for each Object3D instance by the Renderer class as preparation for
 * the actual rendering process.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public final class RenderObject
{
	Object3D	obj;
	Matrix3D	xform;

	int			nrVerts;
	float[]		verts;
	int[]		ph;
	int[]		pv;
	long[]		pd;

	boolean		vertNormDirty;
	float[]		vertNorm;

	int			nrLights;
	Light[]		lights;
	float[][]	lightNormalDist;

	Face		faces;
	Face		facesFree;

	public final class Face
	{
		Face		next;
		//Face		isBehind;
		//Face		isBefore;
		int			index;
		int[]		vi;
		int			minH;
		int			minV;
		int			minD;
		int			maxH;
		int			maxV;
		int			maxD;
		float		nx;
		float		ny;
		float		nz;
		float		dist;

		/*
		 * All following variables are only valid after calling applyLighting()
		 */
		int[]		ds;
		int[]		sxs;
		int[]		sys;
		int[]		sfs;

		private void set( final int index , final int minH , final int minV , final int minD , final int maxH , final int maxV , final int maxD , final float nx , final float ny , final float nz )
		{
			this.index	= index;
			this.minH	= minH;
			this.minV	= minV;
			this.minD	= minD;
			this.maxH	= maxH;
			this.maxV	= maxV;
			this.maxD	= maxD;
			this.nx		= nx;
			this.ny		= ny;
			this.nz		= nz;

			vi = obj.getFaceVertexIndices( index );
			final int i = vi[ 0 ] * 3;
			dist = nx * verts[ i ] + ny * verts[ i + 1 ] + nz * verts[ i + 2 ];
		}

		public boolean isBehind( final Face other )
		{
			if ( minH > other.maxH ) return false;
			if ( maxH < other.minH ) return false;
			if ( minV > other.maxV ) return false;
			if ( maxV < other.minV ) return false;
			if ( minD > other.maxD ) return true;
			if ( maxD < other.minD ) return false;


			final int i = other.vi[ 0 ] * 3;
			final float[] v = other.getRenderObject().verts;
			final float x = v[ i     ];
			final float y = v[ i + 1 ];
			final float z = v[ i + 2 ];

			return y > ( dist - nx * x - nz * z ) / ny;
		}

		public void applyLighting()
		{
			int i,j,k;

			/*
			 * Initialize shading property buffers
			 */
			i = vi.length;
			if ( i < 3 )
				return;

			if ( ds == null || i > ds.length )
			{
				ds  = new int[ i ];
				sxs = new int[ i ];
				sys = new int[ i ];
				sfs = new int[ i ];
			}

			while ( --i >= 0 )
			{
				ds [ i ] = 0;
				sxs[ i ] = 32768;
				sys[ i ] = 32768;
				sfs[ i ] = 0;
			}

			/*
			 * Calculate shading properties for all light sources.
			 */
			if ( nrLights < 1 )
				return;

			final TextureSpec texture = getTexture();

			if ( obj.isFaceSmooth( index ) )
			{
				final float[] vertNorm = getVertexNormals();

				for ( i = nrLights ; --i >= 0 ; )
				{
					final Light   light      = lights[ i ];
					final float[] normalDist = lightNormalDist[ i ];

					for ( j = vi.length ; --j >= 0 ; )
					{
						k = vi[ j ] * 3;

						light.calculateShadingProperties( texture ,
							normalDist , vi[ j ] * 4 ,
							vertNorm[ k ] , vertNorm[ k + 1 ] , vertNorm[ k + 2 ] ,
							ds , sxs , sys , sfs , j );
					}
				}
			}
			else
			{
				for ( i = nrLights ; --i >= 0 ; )
				{
					final Light   light      = lights[ i ];
					final float[] normalDist = lightNormalDist[ i ];

					for ( j = vi.length ; --j >= 0 ; )
					{
						light.calculateShadingProperties( texture ,
							normalDist , vi[ j ] * 4 ,
							nx , ny , nz ,
							ds , sxs , sys , sfs , j );
					}
				}
			}
		}

		public TextureSpec getTexture()
		{
			return obj.getFace( index ).getTexture();
		}

		public int[] getTextureU()
		{
			return obj.getFace( index ).getTextureU();
		}

		public int[] getTextureV()
		{
			return obj.getFace( index ).getTextureV();
		}

		public RenderObject getRenderObject()
		{
			return RenderObject.this;
		}
	}


	/**
	 * Get Object3D that was used to construct the render object.
	 *
	 * @return	Object3D use to construct the render object.
	 */
	public Object3D getObject()
	{
		return obj;
	}

	/**
	 * Get vertex normals relative to the render view. The result is returned as
	 * a float array with 3 entries per vertex.
	 *
	 * @return	Float array with vertex normals in render view.
	 */
	public float[] getVertexNormals()
	{
		if ( vertNormDirty )
		{
			vertNormDirty = false;

			final int nrVerts3 = nrVerts * 3;

			if ( vertNorm == null || nrVerts3 > vertNorm.length )
				vertNorm = new float[ nrVerts3 ];

			xform.rotate( obj.getVertexNormals() , vertNorm , nrVerts );
		}

		return vertNorm;
	}

	/**
	 * Set properties of the render object.
	 *
	 * @param	obj					Object3D used to define the object.
	 * @param	xform				View transform (rotation, translation).
	 * @param	aperture			Camera aperture.
	 * @param	zoom				Linear zoom factor.
	 * @param	width				Width of render area in pixels.
	 * @param	height				Height of render area in pixels.
	 * @param	backfaceCullling	Discard backfaces if set to <code>true</code>.
	 */
	public void set( final Object3D obj , final Matrix3D xform , final float aperture , final float zoom , final int width , final int height , final boolean backfaceCullling )
	{
		int i,j,k,l,ih,iv,id;
		float x,y,z;

		this.obj           = obj;
		this.xform         = xform;
		this.nrLights      = 0;
		this.vertNormDirty = true;

						nrVerts		= obj.getTotalVertexCount();
		final int		nrVerts3	= nrVerts * 3;
		final float[]	oVerts		= obj.getVertices();
		final int		centerH		= width << 7;
		final int		centerV		= height << 7;
		final float[]			faceNormals	= obj.getFaceNormals();

		final float xx = xform.xx , xy = xform.xy , xz = xform.xz , xo = xform.xo;
		final float yx = xform.yx , yy = xform.yy , yz = xform.yz , yo = xform.yo;
		final float zx = xform.zx , zy = xform.zy , zz = xform.zz , zo = xform.zo;

		/*
		 * Prepare vertex buffers, transform vertices, project vertices.
		 */
		if ( verts == null || nrVerts3 > verts.length )
		{
			verts = new float[ nrVerts3 ];
			ph    = new int  [ nrVerts  ];
			pv    = new int  [ nrVerts  ];
			pd    = new long [ nrVerts  ];
		}


		final float perspectiveFactor = 256f * zoom / aperture;

		for ( i = 0 , j = 0 ; i < nrVerts3 ; i += 3 , j++ )
		{
			final float ox = oVerts[ i     ];
			final float oy = oVerts[ i + 1 ];
			final float oz = oVerts[ i + 2 ];

			x = verts[ i     ] = ox * xx + oy * xy + oz * xz + xo;
			y = verts[ i + 1 ] = ox * yx + oy * yy + oz * yz + yo;
			z = verts[ i + 2 ] = ox * zx + oy * zy + oz * zz + zo;

			id = (int)y;

			if ( id < 10 )
			{
				pd[ j ] = 0;
				ph[ j ] = centerH;
				pv[ j ] = centerV;
			}
			else
			{
				pd[ j ] = (long)( 549755813887d / y ); /*0x7FFFFFFFFF / id;*/
				ph[ j ] = centerH + (int)( x * ( y = perspectiveFactor / y ) );
				pv[ j ] = centerV - (int)( z *   y                           );
			}
		}

		/*
		 * Prepare face buffer and insert faces into this buffer (the number
		 * of faces may be reduced during this as a result of early hidden
		 * surface removal (e.g. back culling)).
		 */
		Face previous,current/*,next,free*/;

		if ( faces != null )
		{
			if ( facesFree != null )
			{
				previous = faces;
				while ( previous.next != null ) previous = previous.next;
				previous.next = facesFree;
			}
			facesFree = faces;
			faces = null;
		}

		nextFace: for ( i = obj.getFaceCount() ; --i >= 0 ; )
		{
			final int[] vi = obj.getFaceVertexIndices( i );
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
				final int h1 = ph[ l = vi[ 0 ] ] >> 8;
				final int v1 = pv[ l           ] >> 8;
				final int h2 = ph[ l = vi[ 1 ] ] >> 8;
				final int v2 = pv[ l           ] >> 8;
				final int h3 = ph[ l = vi[ 2 ] ] >> 8;
				final int v3 = pv[ l           ] >> 8;

				if ( (h1 - h2) * (v3 - v2) >= (v1 - v2) * (h3 - h2) )
					continue nextFace;
			}

			/*
			 * Determine bounding box of face. Don't draw if outside screen range.
			 */
			l = vi[ 0 ];
			if ( pd[ l ] == 0 )
				continue nextFace;

			int		minH = ph[ l ] >> 8;
			int		maxH = minH;
			int		minV = pv[ l ] >> 8;
			int		maxV = minV;
			int		minD = (int)verts[ l * 3 + 1 ];
			int		maxD = minD;

			for ( k = vi.length ; --k >= 1 ; )
			{
				l = vi[ k ];

				if ( pd[ l ] == 0 ) 	continue nextFace;
				ih = ph[ l ] >> 8;
				iv = pv[ l ] >> 8;
				id = (int)verts[ l * 3 + 1 ];

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
			x = faceNormals[ k = i * 3 ];
			y = faceNormals[ k + 1 ];
			z = faceNormals[ k + 2 ];

			/*
			 * Insert face
			 *  1) Reuse face from 'free list' or construct new 'Face' object
			 *  2) Set face properties
			 *  3) Find insertion point in face chain
			 *  4) Insert face in face chain
			 */
			if ( facesFree != null )
				facesFree = ( current = facesFree ).next;
			else
				current = new Face();

			current.set( i , minH , minV , minD , maxH , maxV , maxD ,
				x * xx + y * xy + z * xz ,
				x * yx + y * yy + z * yz ,
				x * zx + y * zy + z * zz );

			current.next = faces;
			faces = current;

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
	 * @param	lightSources	Collection of light sources.
	 */
	public void setLights( final LeafCollection lightSources )
	{
		int i,j,k;

		/*
		 * Set number of light sources.
		 */
		nrLights = ( lightSources != null ) ? lightSources.size() : 0;

		/*
		 * Prepare light source buffer.
		 */
		Light[]   lights          = this.lights;
		float[][] lightNormalDist = this.lightNormalDist;

		if ( nrLights > 0 && ( lights == null || nrLights > lights.length ) )
		{
			lights = new Light[ nrLights ];
			if ( this.lights != null ) System.arraycopy( this.lightNormalDist , 0 , lightNormalDist , 0 , this.lights.length );
			this.lights = lights;

			lightNormalDist = new float[ nrLights ][];
			this.lightNormalDist = lightNormalDist;
		}

		/*
		 * Prepare light directions and distance for light sources that need them.
		 */
		final int nrVerts4 = nrVerts * 4;

		for ( i = 0 ; i < nrLights ; i++ )
		{
			lights[ i ] = (Light)lightSources.getNode( i );

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
					final float dx = x - verts[ j++ ];
					final float dy = y - verts[ j++ ];
					final float dz = z - verts[ j++ ];

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
