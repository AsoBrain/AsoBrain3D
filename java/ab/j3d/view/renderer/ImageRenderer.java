/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

import com.numdata.oss.ArrayTools;

/**
 * This class implements a software renderer for 3D scenes, shading the scene
 * on a per-pixel basis to create a {@link BufferedImage}.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class ImageRenderer
{
	/**
	 * Z-Buffer. Each entry corresponds to a pixel's Z-coordinate. If a
	 * pixel is drawn, its Z-coordinate must exceed this value to be
	 * visible. In such a case, the Z-buffer is updated with the new
	 * Z-coordinate.
	 */
	private int[] _depthBuffer;

	/**
	 * Flag to indicate that the renderer should abort it works as soon as possible.
	 */
	boolean _abort;

	/**
	 * Temporary collection with objects (Object3D) to be rendered.
	 *
	 * @see     Object3D
	 */
	private final Node3DCollection _collectedObjects = new Node3DCollection();

	/**
	 * Temporary collection with light sources (Light).
	 *
	 * @see     Light3D
	 */
	private final Node3DCollection _collectedLights = new Node3DCollection();

	/**
	 * Temporary object with information about a rendered object.
	 *
	 * @see     #renderObject
	 */
	private final RenderObject _renderObject = new RenderObject();

	/**
	 * This hashtable is used to share phong tables between materials.
	 * The key is the specular exponent that was used to calculate the
	 * phong table.
	 */
	private static final Map _phongTableCache = new HashMap();

	/**
	 * This hashtable is used to share textures between materials. The
	 * key is the 'texture' field value, the elements are Object arrays
	 * with the following layout:
	 *
	 *  [ 0 ] = Integer : _argb
	 *  [ 1 ] = int[][] : _pixels
	 *  [ 2 ] = Boolean : _transparent
	 */
	private static final Map _textureCache = new HashMap();

	/**
	 * Construct renderer.
	 */
	public ImageRenderer()
	{
		_depthBuffer   = null;
		_abort         = false;
	}

	/**
	 * Request abort of running {@link #renderScene} request. This can be used
	 * in multi-threaded applications to request the renderer to stop working
	 * when its output has become irrelivant, typically because the scene has
	 * changed and a new frame should be rendered.
	 */
	public void abort()
	{
		_abort = true;
	}

	/**
	 * Test if abort was called during the last {@link #renderScene} request.
	 * This can be used in multi-threaded applications to test if the last
	 * rendered image was completed or not.
	 * <p />
	 * This flag is cleared immediately when {@link #renderScene} is called; it
	 * is set by {@link #abort().
	 */
	public boolean isAborted()
	{
		return _abort;
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   backgroundColor     Background color to use.
	 */
	public BufferedImage renderScene( final BufferedImage oldImage , final int width , final int height , final Color backgroundColor , final Camera3D camera )
	{
		_abort = false;

		/*
		 * Create image.
		 */
		final BufferedImage image;
		if ( ( oldImage == null ) || ( oldImage.getWidth() != width ) || ( oldImage.getHeight() != height ) )
			image = new BufferedImage( width , height , BufferedImage.TYPE_INT_RGB );
		else
			image = oldImage;

		/*
		 * Initialize frame and Z buffer. Re-create these buffers if necessary.
		 */
		final int            bufferSize   = width * height;
		final WritableRaster raster       = image.getRaster();
		final DataBufferInt  rasterBuffer = (DataBufferInt)raster.getDataBuffer();
		final int[]          frameBuffer  = rasterBuffer.getData();
		final int[]          depthBuffer  = (int[])ArrayTools.ensureLength( _depthBuffer , int.class , -1 , bufferSize );
		_depthBuffer = depthBuffer;

		/*
		 * Clear buffers in 2 phases:
		 *  1) use for-loop to clear fixed number of entries (512 seems reasonable?);
		 *  2) use System.arraycopy for base 2 fill of buffers (how expensive is this?).
		 */
		final int backgroundRGB = backgroundColor.getRGB();
		for ( int i = ( bufferSize < 512 ) ? bufferSize : 512 ; --i >= 0 ; )
		{
			frameBuffer[ i ] = backgroundRGB;
			depthBuffer[ i ] = 0x7FFFFFFF;
		}

		for ( int i = 512 ; !_abort  && ( i < bufferSize ) ; )
		{
			int c = bufferSize - i;
			if ( c > i )
				c = i;

			System.arraycopy( frameBuffer      , 0 , frameBuffer      , i , c );
			System.arraycopy( depthBuffer , 0 , depthBuffer , i , c );
			i += c;
		}

		/*
		 * 1) Gather objects in this world.
		 * 2) Gather lights in this world. Prepare per-light cached array.
		 * 3) Cycle through all available models and render them.
		 */
		if ( !_abort  )
		{
			_collectedObjects.clear();
			camera.gatherLeafs( _collectedObjects , Object3D.class , Matrix3D.INIT , true );
			if ( !_abort  )
			{
				_collectedLights.clear();
				camera.gatherLeafs( _collectedLights , Light3D.class , Matrix3D.INIT , true );

				for ( int i = 0 ; !_abort   && ( i < _collectedObjects.size() ) ; i++ )
					renderObject( depthBuffer , frameBuffer , width , height , camera , _collectedObjects.getMatrix( i ) , (Object3D)_collectedObjects.getNode( i ) );
			}
		}

		return image;
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   object          The object to render.
	 * @param   cameraXform     The transformation of the camera.
	 * @param   width           Width of rendering image.
	 * @param   height          Height of rendering image.
	 */
	private void renderObject( final int[] depthBuffer , final int[] frameBuffer , final int width , final int height , final Camera3D camera , final Matrix3D cameraXform , final Object3D object )
	{
		final RenderObject renderObject = _renderObject;

		renderObject.set( object , cameraXform , Math.tan( camera.getAperture() / 2.0 ) , camera.getZoomFactor() , width , height , true );
		if ( !_abort )
		{
			renderObject.setLights( _collectedLights );
			if ( !_abort )
			{
				/*
				 * For each face:
				 *   - determine if it's invisible (outside view volume & backface culling)
				 *   - calculate weight point (average of vertices)
				 */
				for ( RenderObject.Face face = renderObject._faces ; !_abort && ( face != null ) ; face = face._next )
				{
					face.applyLighting();
					renderFace( depthBuffer , frameBuffer , width , height , face );
				}
			}
		}
	}

	/**
	 * Render the specified face. This is the most complex process of the renderer. It will
	 * find the left and right edges around a face and use the calculated lighting values
	 * to render scanlines.
	 *
	 * @param   face            Face to be rendered.
	 */
	private static void renderFace( final int[] depthBuffer , final int[] frameBuffer , final int width , final int height , final RenderObject.Face face )
	{
		int  i;
		int  j;
		int  k;
		int  m;
		int  n;
		long d1;
		long d2;

		final RenderObject ro = face.getRenderObject();

		final int[]       ph            = ro._ph;
		final int[]       pv            = ro._pv;
		final long[]      pd            = ro._pd;
		final int[]       vertexIndices = face._vi;
		final int         vertexCount   = vertexIndices.length;
		final TextureSpec textureSpec   = face.getTexture();
		final int[][]     texturePixels = getTextureImage( textureSpec );
		final boolean     hasTexture    = texturePixels != null;
		final int[]       tus           = hasTexture ? face.getTextureU() : null;
		final int[]       tvs           = hasTexture ? face.getTextureV() : null;
		final int         colorRGB      = textureSpec.getARGB();
		final int[]       ds            = face._ds;
		final int[]       sxs           = face._sxs;
		final int[]       sys           = face._sys;
		final int[]       sfs           = face._sfs;

		/*
		 * Determine minimum and maximum Y value, and set the first vertex
		 * to an element with the minimum Y value.
		 */
		int minH  = ph[ vertexIndices[ 0 ] ] >> 8;
		int maxH  = minH;
		int minV  = pv[ vertexIndices[ 0 ] ] >> 8;
		int maxV  = minV;
		int first = 0;

		for ( i = vertexCount , m = 0 ; --i > 0 ; )
		{
			n = vertexIndices[ i ];
			j = ph[ n ] >> 8;
			k = pv[ n ] >> 8;

			if ( pd[ n ] == 0 ) return;

			if ( j < minH ) { minH = j; }
			if ( j > maxH ) { maxH = j; }
			if ( k < minV ) { minV = k; first = i; }
			if ( k > maxV ) { maxV = k; }

			if ( sfs != null && sfs[ i ] > m ) m = sfs[ i ];
		}

		final short[][] phongTable = ( m > 0xFF ) && ( sxs != null ) && ( sys != null ) && ( sfs != null ) ? getPhongTable( textureSpec ) : null;

		/*
		 * Ignore face if it completely outside the screen area.
		 */
		if ( maxH <= 0 || minH >= width  ||
		     maxV <= 0 || minV >= height )
		{
			return;
		}

		/*
		 * Setup state variables used by the interpolation loops.
		 */
		int  v     = minV;
		int  nextV;

		int  li1;           // Index in shape to 1st vertex of segement
		int  li2   = first; // Index in shape to 2nd vertex of segement
		int  lv2   = minV;  // 'left'  vertical coordinate at end of segment

		int  ri1;           // Index in shape to 1st vertex of segement
		int  ri2   = first; // Index in shape to 2nd vertex of segement
		int  rv2   = minV;  // 'right' vertical coordinate at end of segment

		int  lh    = 0;     // 'left'  Horizontal coordinate counter     * 2^8
		int  lhc   = 0;     // 'left'  Horizontal coordinate coefficient * 2^8
		int  rh    = 0;     // 'right' Horizontal coordinate counter     * 2^8
		int  rhc   = 0;     // 'right' Horizontal coordinate coefficient * 2^8

		long ld    = 0L;    // 'left'  Depth counter     * 2^8
		long ldc   = 0L;    // 'left'  Depth coefficient * 2^8
		long rd    = 0L;    // 'right' Depth counter     * 2^8
		long rdc   = 0L;    // 'right' Depth coefficient * 2^8

		long ltu   = 0L;    // 'left'  Texture U-coordinate counter     * 2^8
		long ltuc  = 0L;    // 'left'  Texture U-coordinate coefficient * 2^8
		long rtu   = 0L;    // 'right' Texture U-coordinate counter     * 2^8
		long rtuc  = 0L;    // 'right' Texture U-coordinate coefficient * 2^8

		long ltv   = 0L;    // 'left'  Texture V-coordinate counter     * 2^8
		long ltvc  = 0L;    // 'left'  Texture V-coordinate coefficient * 2^8
		long rtv   = 0L;    // 'right' Texture V-coordinate counter     * 2^8
		long rtvc  = 0L;    // 'right' Texture V-coordinate coefficient * 2^8

		int  ldr   = 0;     // 'left'  Diffuse reflection counter     * 2^8
		int  ldrc  = 0;     // 'left'  Diffuse reflection coefficient * 2^8
		int  rdr   = 0;     // 'right' Diffuse reflection counter     * 2^8
		int  rdrc  = 0;     // 'right' Diffuse reflection coefficient * 2^8

		int  lsx   = 0;     // 'left'  Specular X-coordinate counter     * 2^8
		int  lsxc  = 0;     // 'left'  Specular X-coordinate coefficient * 2^8
		int  rsx   = 0;     // 'right' Specular X-coordinate counter     * 2^8
		int  rsxc  = 0;     // 'right' Specular X-coordinate coefficient * 2^8

		int  lsy   = 0;     // 'left'  Specular Y-coordinate counter     * 2^8
		int  lsyc  = 0;     // 'left'  Specular Y-coordinate coefficient * 2^8
		int  rsy   = 0;     // 'right' Specular Y-coordinate counter     * 2^8
		int  rsyc  = 0;     // 'right' Specular Y-coordinate coefficient * 2^8

		int  lsf   = 0;     // 'left'  Specular intensity factor counter     * 2^8
		int  lsfc  = 0;     // 'left'  Specular intensity factor coefficient * 2^8
		int  rsf   = 0;     // 'right' Specular intensity factor counter     * 2^8
		int  rsfc  = 0;     // 'right' Specular intensity factor coefficient * 2^8

		/*
		 * Handle exception: single horizontal line. The the description of the 'segment
		 * loop' for details why we need to handle this specially.
		 */
		if ( minV == maxV )
		{
			/*
			 * Find vertices with minimum and maximum X.
			 */
			for ( lh = rh = ph[ vertexIndices[ li1 = ri1 = 0 ] ] , i = vertexCount ; --i > 0 ; )
			{
				j = ph[ vertexIndices[ i ] ];
				if ( j < lh ) { li1 = i; lh = j; }
				if ( j > rh ) { ri1 = i; rh = j; }
			}

			/*
			 * Draw single scan line and exit.
			 */
			ld = pd[ li1 ];
			rd = pd[ ri1 ];

			ldr = ds[ li1 ];
			rdr = ds[ ri1 ];

			if ( hasTexture )
			{
				ltu = (long)tus[ li1 ] * ld;
				rtu = (long)tus[ ri1 ] * rd;
				ltv = (long)tvs[ li1 ] * ld;
				rtv = (long)tvs[ ri1 ] * rd;
			}

			if ( phongTable != null )
			{
				lsx = sxs[ li1 ];
				rsx = sxs[ ri1 ];
				lsy = sys[ li1 ];
				rsy = sys[ ri1 ];
				lsf = sfs[ li1 ];
				rsf = sfs[ ri1 ];
			}

			renderScanlines( depthBuffer , frameBuffer , width , height ,
			    v , v , lh , 0 , rh , 0 , ld , 0L , rd , 0L , colorRGB ,
				texturePixels , ltu , 0L , rtu , 0L , ltv , 0L , rtv , 0L , ldr , 0 , rdr , 0 ,
				phongTable , lsx , 0 , rsx , 0 , lsy , 0 , rsy , 0 , lsf , 0 , rsf , 0 );

			return;
		}

		/*
		 * Segment loop: cycle through line segments defining the shape from top to bottom.
		 *
		 * The proces uses a 'left' and 'right' side. Starting from the 'top' vertex, the
		 * 'left' side will traverse the vertices in negative direction; the 'right' side
		 * will traverse the vertices in positive direction.
		 *
		 * Horizontal segments are skipped during the proces (we can't handle horizontal
		 * lines very well). This is not that bad, as only the top and bottom segments of
		 * convex shapes can be horizontal. The only case that must be handled specially,
		 * occurs when the shape renders a single horizontal line.
		 */
		while ( v < maxV )
		{
			if ( v >= height ) return;

			/*
			 * Update 'left' side if needed.
			 */
			if ( lv2 == v )
			{
				do
				{
					if ( (li2 = (li1 = li2) - 1) < 0 ) li2 = vertexCount - 1;
					lv2 = pv[ vertexIndices[ li2 ] ] >> 8;
				}
				while ( lv2 == v );

				d1 = pd[ m = vertexIndices[ li1 ] ];
				d2 = pd[ n = vertexIndices[ li2 ] ];

				i = (j = lv2 - (pv[ m ] >> 8)) + 1; if ( j == 0 ) j = 1;


				lhc = ph[ n ] - (lh = ph[ m ]);
				     if ( lhc < 0 ) lhc = ( lhc - 0x100 ) / i;
				else if ( lhc > 0 ) lhc = ( lhc + 0x100 ) / i;
				lh += 0x80;

				//lzc = (d2        - (lz =  d1       )) / j; lz += 0x80;
				ldc = d2 - ( ld = d1 );
				     if ( ldc < 0 ) ldc = ( ldc - 0x100L ) / (long)i;
				else if ( ldc > 0 ) ldc = ( ldc + 0x100L ) / (long)i;
				ld += 0x80L;

				ldrc = (ds[ li2 ] - (ldr =  ds[ li1 ])) / j; ldr += 0x80;

				if ( hasTexture )
				{
					ltuc = ( ( (long)tus[ li2 ] * d2 ) - ( ltu = ( (long)tus[ li1 ] * d1 ) ) ) / (long)j; ltu += 0x80L;
					ltvc = ( ( (long)tvs[ li2 ] * d2 ) - ( ltv = ( (long)tvs[ li1 ] * d1 ) ) ) / (long)j; ltv += 0x80L;
				}

				if ( phongTable != null )
				{
					lsxc = (sxs[ li2 ] - (lsx = sxs[ li1 ])) / j; lsx += 0x80;
					lsyc = (sys[ li2 ] - (lsy = sys[ li1 ])) / j; lsy += 0x80;
					lsfc = (sfs[ li2 ] - (lsf = sfs[ li1 ])) / j; lsf += 0x80;
				}
			}

			/*
			 * Update 'right' side if needed.
			 */
			if ( rv2 == v )
			{
				do
				{
					if ( (ri2 = ( ri1 = ri2 ) + 1) == vertexCount ) ri2 = 0;
					rv2 = pv[ vertexIndices[ ri2 ] ] >> 8;
				}
				while ( rv2 == v );

				d1 = pd[ m = vertexIndices[ ri1 ] ];
				d2 = pd[ n = vertexIndices[ ri2 ] ];

				i = ( j = rv2 - (pv[ m ] >> 8)) + 1; if ( j == 0 ) j = 1;

				rhc = ph[ n ] - (rh = ph[ m ]);
				     if ( rhc < 0 ) rhc = ( rhc - 0x100 ) / i;
				else if ( rhc > 0 ) rhc = ( rhc + 0x100 ) / i;
				rh += 0x80;

				//rzc = ( d2        - (rz =  d1       )) / j; rz += 0x80;
				rdc = d2 - ( rd =  d1 );
				     if ( rdc < 0 ) rdc = ( rdc - 0x100L ) / (long)i;
				else if ( rdc > 0 ) rdc = ( rdc + 0x100L ) / (long)i;
				rd += 0x80L;

				rdrc = ( ds[ ri2 ] - (rdr =  ds[ ri1 ])) / j; rdr += 0x80;

				if ( hasTexture )
				{
					rtuc = (( (long)tus[ ri2 ] * d2) - (rtu = ( (long)tus[ ri1 ] * d1))) / (long)j; rtu += 0x80L;
					rtvc = (( (long)tvs[ ri2 ] * d2) - (rtv = ( (long)tvs[ ri1 ] * d1))) / (long)j; rtv += 0x80L;
				}

				if ( phongTable != null )
				{
					rsxc = (sxs[ ri2 ] - (rsx = sxs[ ri1 ])) / j; rsx += 0x80;
					rsyc = (sys[ ri2 ] - (rsy = sys[ ri1 ])) / j; rsy += 0x80;
					rsfc = (sfs[ ri2 ] - (rsf = sfs[ ri1 ])) / j; rsf += 0x80;
				}
			}

			/*
			 * Prepare for pixel-loop.
			 */
			nextV = ( lv2 < rv2 ) ? lv2 : rv2;
			if ( nextV == maxV ) nextV++;
			if ( nextV > height ) nextV = height;

			renderScanlines( depthBuffer , frameBuffer , width , height ,
			    v , nextV , lh , lhc , rh , rhc , ld , ldc , rd , rdc , colorRGB ,
				texturePixels , ltu , ltuc , rtu , rtuc , ltv , ltvc , rtv , rtvc , ldr , ldrc , rdr , rdrc ,
				phongTable , lsx , lsxc , rsx , rsxc , lsy , lsyc , rsy , rsyc , lsf , lsfc , rsf , rsfc );

			/*
			 * Update counters.
			 */
			if ( nextV < height )
			{
				i = nextV - v;
				if ( lv2 != nextV )
				{
					lh  += lhc  * i;
					ld  += ldc  * (long)i;
					ltu += ltuc * (long)i;
					ltv += ltvc * (long)i;
					ldr  += ldrc  * i;
					lsx += lsxc * i;
					lsy += lsyc * i;
					lsf += lsfc * i;
				}

				if ( rv2 != nextV )
				{
					rh  += rhc  * i;
					rd  += rdc  * (long)i;
					rtu += rtuc * (long)i;
					rtv += rtvc * (long)i;
					rdr  += rdrc  * i;
					rsx += rsxc * i;
					rsy += rsyc * i;
					rsf += rsfc * i;
				}
			}

			v = nextV;
		}
	}

	/**
	 * This is the core render loop to render a set of scanlines for a face.
	 *
	 * @param   v           Scanline parameter.
	 * @param   nextV       Scanline parameter.
	 * @param   lh          Scanline parameter.
	 * @param   lhc         Scanline parameter.
	 * @param   rh          Scanline parameter.
	 * @param   rhc         Scanline parameter.
	 * @param   ld          Scanline parameter.
	 * @param   ldc         Scanline parameter.
	 * @param   rd          Scanline parameter.
	 * @param   rdc         Scanline parameter.
	 * @param   colorRGB    Scanline parameter.
	 * @param   texture     Scanline parameter.
	 * @param   ltu         Scanline parameter.
	 * @param   ltuc        Scanline parameter.
	 * @param   rtu         Scanline parameter.
	 * @param   rtuc        Scanline parameter.
	 * @param   ltv         Scanline parameter.
	 * @param   ltvc        Scanline parameter.
	 * @param   rtv         Scanline parameter.
	 * @param   rtvc        Scanline parameter.
	 * @param   ldr         Scanline parameter.
	 * @param   ldrc        Scanline parameter.
	 * @param   rdr         Scanline parameter.
	 * @param   rdrc        Scanline parameter.
	 * @param   phongTable  Scanline parameter.
	 * @param   lsx         Scanline parameter.
	 * @param   lsxc        Scanline parameter.
	 * @param   rsx         Scanline parameter.
	 * @param   rsxc        Scanline parameter.
	 * @param   lsy         Scanline parameter.
	 * @param   lsyc        Scanline parameter.
	 * @param   rsy         Scanline parameter.
	 * @param   rsyc        Scanline parameter.
	 * @param   lsf         Scanline parameter.
	 * @param   lsfc        Scanline parameter.
	 * @param   rsf         Scanline parameter.
	 * @param   rsfc        Scanline parameter.
	 */
	private static void renderScanlines(
	    final int[] depthBuffer , final int[] frameBuffer , final int width , final int height ,
		int v , final int nextV ,
		int  lh  , final int  lhc  , int  rh  , final int  rhc  ,
		long ld  , final long ldc  , long rd  , final long rdc  ,

		final int colorRGB , final int[][] texture ,
		long ltu , final long ltuc , long rtu , final long rtuc ,
		long ltv , final long ltvc , long rtv , final long rtvc ,
		int  ldr , final int  ldrc , int  rdr , final int  rdrc ,
		final short[][] phongTable ,
		int  lsx , final int  lsxc , int  rsx , final int  rsxc ,
		int  lsy , final int  lsyc , int  rsy , final int  rsyc ,
		int  lsf , final int  lsfc , int  rsf , final int  rsfc )
	{
		final int tw = ( texture != null ) ? texture[ 0 ].length : 0;
		final int th = ( texture != null ) ? texture.length    : 0;
//		final int ma = ( colorRGB >> 24 ) & 0xFF;
		final int mr = ( colorRGB >> 16 ) & 0xFF;
		final int mg = ( colorRGB >> 8  ) & 0xFF;
		final int mb = colorRGB           & 0xFF;

		int i;
		int j;
		int c;
		int r;
		int g;
		int b;
		int s;

		int  h1;  // 'pixel' Horizontal coordinate counter     * 2^8
		int  h2;  // 'pixel' Horizontal coordinate coefficient * 2^8
		long d1;  // 'pixel' Z-coordinate counter     * 2^8
		long d2;  // 'pixel' Z-coordinate coefficient * 2^8
		long tu1; // 'pixel' Texture U-coordinate counter     * 2^8
		long tu2; // 'pixel' Texture U-coordinate coefficient * 2^8
		long tv1; // 'pixel' Texture V-coordinate counter     * 2^8
		long tv2; // 'pixel' Texture V-coordinate coefficient * 2^8
		int  dr1; // 'pixel' Diffuse reflection counter     * 2^8
		int  dr2; // 'pixel' Diffuse reflection coefficient * 2^8
		int  sx1; // 'pixel' Specular X-coordinate counter     * 2^8
		int  sx2; // 'pixel' Specular X-coordinate coefficient * 2^8
		int  sy1; // 'pixel' Specular Y-coordinate counter     * 2^8
		int  sy2; // 'pixel' Specular Y-coordinate coefficient * 2^8
		int  sf1; // 'pixel' Specular intensity factor counter     * 2^8
		int  sf2; // 'pixel' Specular intensity factor coefficient * 2^8

		do
		{
			i = lh >> 8;
			j = rh >> 8;

			if ( ( v >= 0 ) && ( v < height )
			  && ( i >= 0 || j >= 0 )
			  && ( i < width || j < width ) )
			{
				/*
				 * Determine scanline pixel interpolation variables (swap sides if lh > rh)
				 */
				if ( i <= j )
				{
					h1  = i;   h2  = j;
					d1  = ld;  d2  = rd;
					tu1 = ltu; tu2 = rtu;
					tv1 = ltv; tv2 = rtv;
					dr1 = ldr; dr2 = rdr;
					sx1 = lsx; sx2 = rsx;
					sy1 = lsy; sy2 = rsy;
					sf1 = lsf; sf2 = rsf;

					if ( lhc < -0x100 )
					{
						h1  += (lhc >> 8) + 1;
						d1  +=  ldc;
						dr1 +=  ldrc;
						sf1 +=  lsfc;
					}

					if ( rhc >  0x100 )
					{
						h2  += (rhc >> 8) - 1;
						d2  +=  rdc;
						dr2 +=  rdrc;
						sf2 +=  rsfc;
					}
				}
				else // lx > rx - swap sides
				{
					h1  = j;   h2  = i;
					d1  = rd;  d2  = ld;
					tu1 = rtu; tu2 = ltu;
					tv1 = rtv; tv2 = ltv;
					dr1 = rdr; dr2 = ldr;
					sx1 = rsx; sx2 = lsx;
					sy1 = rsy; sy2 = lsy;
					sf1 = rsf; sf2 = lsf;

					if ( rhc < -0x100 )
					{
						h1  += (rhc >> 8) + 1;
						d1  +=  rdc;
						dr1 +=  rdrc;
						sf1 +=  rsfc;
					}

					if ( lhc >  0x100 )
					{
						h2  += (lhc >> 8) - 1;
						d2  +=  ldc;
						dr2 +=  ldrc;
						sf2 +=  lsfc;
					}
				}

				if ( ( i = ( h2 -= h1 ) ) == 0 ) i = 1;

				d2  = ( d2  - d1  ) / (long)i;
				tu2 = ( tu2 - tu1 ) / (long)i;
				tv2 = ( tv2 - tv1 ) / (long)i;
				dr2 = ( dr2 - dr1 ) / i;
				sx2 = ( sx2 - sx1 ) / i;
				sy2 = ( sy2 - sy1 ) / i;
				sf2 = ( sf2 - sf1 ) / i;

				/*
				 * Clip left/right. On the left side, we must adjust all interpolation
				 * counters. On the right side, we can simply reduce the number of pixels
				 * drawn.
				 */
				if ( h1 < 0 )
				{
					 d1 -=  d2 * (long)h1;
					tu1 -= tu2 * (long)h1;
					tv1 -= tv2 * (long)h1;
					dr1 -= dr2 * h1;
					sx1 -= sx2 * h1;
					sy1 -= sy2 * h1;
					sf1 -= sf2 * h1;

					h2 += h1;
					h1 = 0;
				}

				if ( h1 + h2 >= width )
					h2 = width - h1 - 1;

				/*
				 * Execute pixel-loop.
				 */
				if ( ( phongTable != null ) && ((lsf > 0xFF) || (rsf > 0xFF)) )
				{
					if ( texture != null )
					{
						for ( i = width * v + h1 ; h2-- >= 0 ; i++ )
						{
							if ( (j = (int)(0x7FFFFFFFFFL / d1)) < depthBuffer[ i ] )
							{
								depthBuffer[ i ] = j;

								// fix , tu1 and tv1 become < 0 don't know how that happens
								final int myU = (int)( ( tu1 / d1 ) % (long)tw );
								final int myV = (int)( ( tv1 / d1 ) % (long)th );
								c = texture[ myV + (myV<0?th:0)][ myU + (myU<0?tw:0) ];
								// original
								//c = texture[ (int)( ( tv1 / d1 ) % th )) ]
								           //[ (int)( ( tu1 / d1 ) % tw )) ];
								s = (int)phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

								if ( (r = (dr1 * ((c >> 16) & 0xFF) + s) >> 16) > 255 ) r = 255;
								if ( (g = (dr1 * ((c >>  8) & 0xFF) + s) >> 16) > 255 ) g = 255;
								if ( (b = (dr1 * ( c        & 0xFF) + s) >> 16) > 255 ) b = 255;

								frameBuffer[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
							tu1 += tu2;
							tv1 += tv2;
							sx1 += sx2;
							sy1 += sy2;
							sf1 += sf2;
						}
					}
					else // no texture
					{
						for ( i = width * v + h1 ; h2-- >= 0 ; i++ )
						{
							if ( (j = (int)(0x7FFFFFFFFFL / d1)) < depthBuffer[ i ] )
							{
								depthBuffer[ i ] = j;

								s = (int)phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

								if ( ( r = ( dr1 * mr + s ) >> 16 ) > 255 ) r = 255;
								if ( ( g = ( dr1 * mg + s ) >> 16 ) > 255 ) g = 255;
								if ( ( b = ( dr1 * mb + s ) >> 16 ) > 255 ) b = 255;

								frameBuffer[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
							sx1 += sx2;
							sy1 += sy2;
							sf1 += sf2;
						}
					}
				}
				else
				{
					if ( texture != null )
					{
						for ( i = width * v + h1 ; h2-- >= 0 ; i++ )
						{
							if ( (j = (int)(0x7FFFFFFFFFL / d1)) < depthBuffer[ i ] )
							{
								depthBuffer[ i ] = j;

								// fix , tu1 and tv1 become < 0 don't know how that happens
								final int myU = (int)( ( tu1 / d1 ) % (long)tw );
								final int myV = (int)( ( tv1 / d1 ) % (long)th );
								c = texture[ myV + (myV<0?th:0)][ myU + (myU<0?tw:0) ];
								// original
								//c = texture[ (int)( ( tv1 / d1 ) % th )) ]
								           //[ (int)( ( tu1 / d1 ) % tw )) ];

								if ( (r = (dr1 * ((c >> 16) & 0xFF)) >> 16) > 255 ) r = 255;
								if ( (g = (dr1 * ((c >>  8) & 0xFF)) >> 16) > 255 ) g = 255;
								if ( (b = (dr1 * ( c        & 0xFF)) >> 16) > 255 ) b = 255;

								frameBuffer[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
							tu1 += tu2;
							tv1 += tv2;
						}
					}
					else // no texture
					{
						for ( i = width * v + h1 ; h2-- >= 0 ; i++ )
						{
							if ( (j = (int)(0x7FFFFFFFFFL / d1)) < depthBuffer[ i ] )
							{
								depthBuffer[ i ] = j;

								if ( ( r = ( dr1 * mr ) >> 16 ) > 255 ) r = 255;
								if ( ( g = ( dr1 * mg ) >> 16 ) > 255 ) g = 255;
								if ( ( b = ( dr1 * mb ) >> 16 ) > 255 ) b = 255;

								frameBuffer[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
						}
					}
				}
			}

			lh  += lhc;  rh  += rhc;
			ld  += ldc;  rd  += rdc;
			ltu += ltuc; rtu += rtuc;
			ltv += ltvc; rtv += rtvc;
			ldr += ldrc; rdr += rdrc;
			lsx += lsxc; rsx += rsxc;
			lsy += lsyc; rsy += rsyc;
			lsf += lsfc; rsf += rsfc;

			v++;
		}
		while ( v < nextV );
	}

	/**
	 * Get phong table for the specified texture. The returned phong table
	 * contains intensity values ranging from 0 to 256.
	 *
	 * @param   texture     Texture to get phong table for.
	 *
	 * @return  2-dimensional array representing phong table.
	 */
	private static short[][] getPhongTable( final TextureSpec texture )
	{
		final int exponent = texture.specularExponent;

		/*
		 * Get phong table from cache.
		 */
		final Integer cacheKey = new Integer( exponent );
		short[][] result = (short[][])_phongTableCache.get( cacheKey );
		if ( result == null )
		{
			/*
			 * Build a new phong table.
			 */
			int x;
			int y;
			double xc;
			double yc;
			double c;
			short s;
			short[] t;

			result = new short[ 256 ][];
			for ( y = 0 ; y <= 128 ; y++ )
			{
				result[ 128 - y ] = t = new short[ 256 ];
				if ( y < 128 ) result[ y + 128 ] = t;

				for ( x = 0 ; x <= 128 ; x++ )
				{
					xc = (double)x / 128.0;
					yc = (double)y / 128.0;
					c  = 1.0 - Math.sqrt( xc * xc + yc * yc );
					if ( c < 0.0 ) c = 0.0;

					t[ 128 - x ] = s = (short)( 256.0 * Math.pow( c , (double)exponent ) );
					if ( x < 128 ) t[ x + 128 ] = s;
				}
			}

			_phongTableCache.put( cacheKey , result );
		}

		return result;
	}

	/**
	 * Get image for the specified texture. The image is returned as a
	 * 2-dimensional array of integers in ARGB format. The primary index is the
	 * Y-coordinate, the secondary index is the X-coordinate (or U and V
	 * coordinates respectively when applied in rendering).
	 *
	 * @param   texture     Texture to get image for.
	 *
	 * @return  2-dimensional array representing texture image;
	 *          <code>null</code> if no image could be created.
	 */
	private static int[][] getTextureImage( final TextureSpec texture )
	{
		int[][] result;

		if ( ( texture == null ) || !texture.isTexture() )
		{
			result = null;
		}
		else
		{
			result = (int[][])_textureCache.get( texture.code );
			if ( result == null )
			{
				/*
				 * Grab image pixels.
				 */
				int   tw     = 0;
				int   th     = 0;
				int[] pixels = null;

				final Image image = texture.getTextureImage();
				if ( image != null )
				{
					try
					{
						final PixelGrabber pg = new PixelGrabber( image , 0 , 0 , -1 , -1 , true );
						if ( pg.grabPixels() )
						{
							tw = pg.getWidth();
							th = pg.getHeight();
							if ( tw > 0 && th > 0 )
								pixels = (int[])pg.getPixels();
						}
					}
					catch ( InterruptedException ie ) {}
				}

				/*
				 * 1) Convert pixels to 2-dimensional array.
				 * 2) Flip texture vertically to get the origin at the lower-left corner.
				 */
				if ( pixels != null )
				{
					result = new int[ th ][];
					for ( int y = 0 ; y < th ; y++ )
						System.arraycopy( pixels , ( th - y - 1 ) * tw , result[ y ] = new int[ tw ] , 0 , tw );
				}
			}

			/*
			 * Put texture info in cache.
			 */
			_textureCache.put( texture.code , result );
		}

		return result;
	}
}
