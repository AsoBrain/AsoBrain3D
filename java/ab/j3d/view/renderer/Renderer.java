/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2004 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2004 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
package ab.j3d.renderer;

import java.awt.Container;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * This class implements a background rendering thread.
 * 
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Renderer
    extends Thread
{
	/**
	 * Component that will display the rendered image. It is used for two things:
	 * <ol>
	 *  <li>
	 *   The rendered image size is determined by the component's size
	 *   (reduced by its insets).
	 *  </li>
	 *  <li>
	 *   Its <code>repaint()</code> method will be envoked when a new image has
	 *   been rendered.
	 *  </li>
	 * </ol>
	 */
	private final Container _targetComponent;

	/**
	 * Camera from where a scene is observed.
	 */
	private final Camera _camera;

	/**
	 * This thread control flag is set when <code>requestTermination()</code>
	 * is called. It is used as exit condition by the main thread loop.
	 *
	 * @see     #requestTermination()
	 * @see     #isAlive()
	 * @see     #join()
	 */
	private boolean _terminationRequested;

	/**
	 * This thread control flag is set when <code>requestUpdate()</code> is
	 * called. It is used to trigger the thread loop to start rendering a new
	 * image. It is also tested at various loop points in the rendering code
	 * to abort rendering of a previous image, so the next rendering will be
	 * completed as soon as possible.
	 *
	 * @see     #requestUpdate()
	 */
	private boolean _updateRequested;

	/**
	 * Last succesfully rendered image.
	 */
	private BufferedImage _renderedImage;

	/**
	 * Z-Buffer. Each entry corresponds to a pixel's Z-coordinate. If a
	 * pixel is drawn, its Z-coordinate must exceed this value to be
	 * visible. In such a case, the Z-buffer is updated with the new
	 * Z-coordinate.
	 */
	private int[] _depthBuffer;

	/**
	 * Temporary collection with objects (Object3D) to be rendered.
	 *
	 * @see     Object3D
	 */
	private final LeafCollection objects = new LeafCollection();

	/**
	 * Temporary collection with light sources (Light).
	 *
	 * @see     Light
	 */
	private final LeafCollection lights = new LeafCollection();

	/**
	 * Temporary object with information about a rendered object.
	 *
	 * @see     #renderObject
	 */
	private final RenderObject renderObject = new RenderObject();

	/**
	 * Construct (and start) render thread.
	 */
	public Renderer( final Container targetComponent , final Camera camera )
	{
		_targetComponent      = targetComponent;
		_camera               = camera;
		_renderedImage        = null;
		_depthBuffer          = null;
		_terminationRequested = false;
		_updateRequested      = true;

		setPriority( Thread.MIN_PRIORITY );
		setName( getClass().getName() );
		start();
	}

	/**
	 * Get last succesfully rendered image. This will return <code>null</code>
	 * while the renderer is updating the image.
	 *
	 * @return  Rendered image;
	 *          <code>null</code> if the image is not available.
	 */
	public BufferedImage getRenderedImage()
	{
		return _renderedImage;
	}

	/**
	 * Request update of rendered image.
	 */
	public void requestUpdate()
	{
		if ( !_terminationRequested )
		{
			_updateRequested = true;
			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * Request termination of the render thread.
	 */
	public void requestTermination()
	{
		_updateRequested = false;
		_terminationRequested = true;
		synchronized ( this )
		{
			notifyAll();
		}
	}

	/**
	 * This is the thread body to update the rendered image on demand.
	 */
	public void run()
	{
		BufferedImage oldImage = null;
		boolean needRepaint = false;

		while ( !_terminationRequested && _targetComponent.isVisible() )
		{
			try
			{
				if ( _updateRequested )
				{
					_updateRequested = false;
					_renderedImage = null;

					final int    background = _targetComponent.getBackground().getRGB();
					final Insets insets     = _targetComponent.getInsets();
					final int    width      = _targetComponent.getWidth() - insets.left - insets.right;
					final int    height     = _targetComponent.getHeight() - insets.top - insets.bottom;

					final BufferedImage newImage = renderFrame( oldImage , width , height , background );
					needRepaint = true; //|= ( oldImage != newImage );
					oldImage = newImage;
				}

				if ( needRepaint && !_updateRequested )
				{
					_renderedImage = oldImage;
					_targetComponent.repaint();
					needRepaint = false;
				}
			}
			catch ( Exception e )
			{
				System.out.println( "Exception (" + e + ") during rendering!" );
			}

			/*
			 * No update needed or an exception occured.
			 *
			 * Wait 300ms or wait to be notified.
			 */
			try
			{
				while ( !_updateRequested )
				{
					synchronized ( this )
					{
						wait( 300 );
					}
				}
			}
			catch ( InterruptedException e ) { /*ignored*/ }
		}

		_renderedImage        = null;
		_depthBuffer          = null;
		_terminationRequested = false;
		_updateRequested      = false;
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   backgroundColor     Background color to use.
	 */
	public BufferedImage renderFrame( final BufferedImage oldImage , final int width , final int height , final int backgroundColor )
	{
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
		final int   bufferSize  = width * height;
		final int[] frameBuffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		final int[] depthBuffer;

		if ( ( _depthBuffer == null ) || ( _depthBuffer.length < bufferSize ) )
		{
			depthBuffer = new int[ bufferSize ];
			_depthBuffer = depthBuffer;
		}
		else
		{
			depthBuffer = _depthBuffer;
		}

		/*
		 * Clear buffers in 2 phases:
		 *  1) use for-loop to clear fixed number of entries (512 seems reasonable?);
		 *  2) use System.arraycopy for base 2 fill of buffers (how expensive is this?).
		 */
		for ( int i = ( bufferSize < 512 ) ? bufferSize : 512 ; --i >= 0 ; )
		{
			frameBuffer[ i ] = backgroundColor;
			depthBuffer[ i ] = 0x7FFFFFFF;
		}

		for ( int i = 512 ; !_updateRequested  && ( i < bufferSize ) ; )
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
		if ( !_updateRequested  )
		{
			objects.clear();
			_camera.gatherLeafs( objects , Object3D.class , Matrix3D.INIT , true );
			if ( !_updateRequested  )
			{
				lights.clear();
				_camera.gatherLeafs( lights , Light.class , Matrix3D.INIT , true );

				for ( int i = 0 ; !_updateRequested   && ( i < objects.size() ) ; i++ )
					renderObject( depthBuffer , frameBuffer , width , height , objects.getMatrix( i ) , (Object3D)objects.getNode( i ) );
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
	private void renderObject( final int[] depthBuffer , final int[] frameBuffer , final int width , final int height , final Matrix3D cameraXform , final Object3D object )
	{
		renderObject.set( object , cameraXform , _camera.aperture , _camera.zoom , width , height , true );
		if ( !_updateRequested )
		{
			renderObject.setLights( lights );
			if ( !_updateRequested )
			{
				/*
				 * For each face:
				 *   - determine if it's invisible (outside view volume & backface culling)
				 *   - calculate weight point (average of vertices)
				 */
				for ( RenderObject.Face face = renderObject.faces ; !_updateRequested && ( face != null ) ; face = face.next )
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
		int i,j,k,m,n;
		long d1,d2;

		final RenderObject ro = face.getRenderObject();

		final int[]			ph = ro.ph;
		final int[]			pv = ro.pv;
		final long[]		pd = ro.pd;
		final int[]			vertexIndices	= face.vi;
		final TextureSpec	textureSpec		= face.getTexture();
		final int[][]		texturePixels	= face.getTexturePixels();
		final boolean		hasTexture		= texturePixels != null;
		final int[]			tus				= hasTexture ? face.getTextureU() : null;
		final int[]			tvs				= hasTexture ? face.getTextureV() : null;
		final int			colorRGB		= textureSpec.getARGB();
		final int[]			ds				= face.ds;
		final int[]			sxs				= face.sxs;
		final int[]			sys				= face.sys;
		final int[]			sfs				= face.sfs;

		/*
		 * Determine minimum and maximum Y value, and set the first vertex
		 * to an element with the minimal Y value.
		 */
		int minH  = ph[ vertexIndices[ 0 ] ] >> 8;
		int maxH  = minH;
		int minV  = pv[ vertexIndices[ 0 ] ] >> 8;
		int maxV  = minV;
		int first = 0;

		for ( i = vertexIndices.length , m = 0 ; --i > 0 ; )
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

		final short[][] phongTable = ( m > 0xFF ) && ( sxs != null ) && ( sys != null ) && ( sfs != null ) ? textureSpec.getPhongTable() : null;

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
		int     v     = minV;
		int     nextV;

		int		li1;            // Index in shape to 1st vertex of segement
		int		li2  = first;   // Index in shape to 2nd vertex of segement
		int		lv2  = minV;    // 'left'  vertical coordinate at end of segment

		int		ri1;            // Index in shape to 1st vertex of segement
		int		ri2  = first;   // Index in shape to 2nd vertex of segement
		int		rv2  = minV;    // 'right' vertical coordinate at end of segment

		int		lh   = 0;       // 'left'  Horizontal coordinate counter     * 2^8
		int		lhc  = 0;       // 'left'  Horizontal coordinate coefficient * 2^8
		int		rh   = 0;       // 'right' Horizontal coordinate counter     * 2^8
		int		rhc  = 0;       // 'right' Horizontal coordinate coefficient * 2^8

		long	ld   = 0;		// 'left'  Depth counter     * 2^8
		long	ldc  = 0;		// 'left'  Depth coefficient * 2^8
		long	rd   = 0;		// 'right' Depth counter     * 2^8
		long	rdc  = 0;		// 'right' Depth coefficient * 2^8

		long	ltu  = 0;		// 'left'  Texture U-coordinate counter     * 2^8
		long	ltuc = 0;		// 'left'  Texture U-coordinate coefficient * 2^8
		long	rtu  = 0;		// 'right' Texture U-coordinate counter     * 2^8
		long	rtuc = 0;		// 'right' Texture U-coordinate coefficient * 2^8

		long	ltv  = 0;		// 'left'  Texture V-coordinate counter     * 2^8
		long	ltvc = 0;		// 'left'  Texture V-coordinate coefficient * 2^8
		long	rtv  = 0;		// 'right' Texture V-coordinate counter     * 2^8
		long	rtvc = 0;		// 'right' Texture V-coordinate coefficient * 2^8

		int		ldr  = 0;		// 'left'  Diffuse reflection counter     * 2^8
		int		ldrc = 0;		// 'left'  Diffuse reflection coefficient * 2^8
		int		rdr  = 0;		// 'right' Diffuse reflection counter     * 2^8
		int		rdrc = 0;		// 'right' Diffuse reflection coefficient * 2^8

		int		lsx  = 0;		// 'left'  Specular X-coordinate counter     * 2^8
		int		lsxc = 0;		// 'left'  Specular X-coordinate coefficient * 2^8
		int		rsx  = 0;		// 'right' Specular X-coordinate counter     * 2^8
		int		rsxc = 0;		// 'right' Specular X-coordinate coefficient * 2^8

		int		lsy  = 0;		// 'left'  Specular Y-coordinate counter     * 2^8
		int		lsyc = 0;		// 'left'  Specular Y-coordinate coefficient * 2^8
		int		rsy  = 0;		// 'right' Specular Y-coordinate counter     * 2^8
		int		rsyc = 0;		// 'right' Specular Y-coordinate coefficient * 2^8

		int		lsf  = 0;		// 'left'  Specular intensity factor counter     * 2^8
		int		lsfc = 0;		// 'left'  Specular intensity factor coefficient * 2^8
		int		rsf  = 0;		// 'right' Specular intensity factor counter     * 2^8
		int		rsfc = 0;		// 'right' Specular intensity factor coefficient * 2^8

		/*
		 * Handle exception: single horizontal line. The the description of the 'segment
		 * loop' for details why we need to handle this specially.
		 */
		if ( minV == maxV )
		{
			/*
			 * Find vertices with minimum and maximum X.
			 */
			for ( lh = rh = ph[ vertexIndices[ li1 = ri1 = 0 ] ] , i = vertexIndices.length ; --i > 0 ; )
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
				ltu = tus[ li1 ] * ld;
				rtu = tus[ ri1 ] * rd;
				ltv = tvs[ li1 ] * ld;
				rtv = tvs[ ri1 ] * rd;
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
			    v , v , lh , 0 , rh , 0 , ld , 0 , rd , 0 , colorRGB ,
				texturePixels , ltu , 0 , rtu , 0 , ltv , 0 , rtv , 0 , ldr , 0 , rdr , 0 ,
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
					if ( (li2 = (li1 = li2) - 1) < 0 ) li2 = vertexIndices.length - 1;
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
				     if ( ldc < 0 ) ldc = ( ldc - 0x100 ) / i;
				else if ( ldc > 0 ) ldc = ( ldc + 0x100 ) / i;
				ld += 0x80;

				ldrc = (ds[ li2 ] - (ldr =  ds[ li1 ])) / j; ldr += 0x80;

				if ( hasTexture )
				{
					ltuc = ((tus[ li2 ] * d2) - (ltu = (tus[ li1 ] * d1))) / j; ltu += 0x80;
					ltvc = ((tvs[ li2 ] * d2) - (ltv = (tvs[ li1 ] * d1))) / j; ltv += 0x80;
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
					if ( (ri2 = ( ri1 = ri2 ) + 1) == vertexIndices.length ) ri2 = 0;
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
				     if ( rdc < 0 ) rdc = ( rdc - 0x100 ) / i;
				else if ( rdc > 0 ) rdc = ( rdc + 0x100 ) / i;
				rd += 0x80;

				rdrc = ( ds[ ri2 ] - (rdr =  ds[ ri1 ])) / j; rdr += 0x80;

				if ( hasTexture )
				{
					rtuc = ((tus[ ri2 ] * d2) - (rtu = (tus[ ri1 ] * d1))) / j; rtu += 0x80;
					rtvc = ((tvs[ ri2 ] * d2) - (rtv = (tvs[ ri1 ] * d1))) / j; rtv += 0x80;
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
					ld  += ldc  * i;
					ltu += ltuc * i;
					ltv += ltvc * i;
					ldr  += ldrc  * i;
					lsx += lsxc * i;
					lsy += lsyc * i;
					lsf += lsfc * i;
				}

				if ( rv2 != nextV )
				{
					rh  += rhc  * i;
					rd  += rdc  * i;
					rtu += rtuc * i;
					rtv += rtvc * i;
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
	 * @param	v			Scanline parameter.
	 * @param	nextV		Scanline parameter.
	 * @param	lh			Scanline parameter.
	 * @param	lhc			Scanline parameter.
	 * @param	rh			Scanline parameter.
	 * @param	rhc			Scanline parameter.
	 * @param	ld			Scanline parameter.
	 * @param	ldc			Scanline parameter.
	 * @param	rd			Scanline parameter.
	 * @param	rdc			Scanline parameter.
	 * @param	colorRGB	Scanline parameter.
	 * @param	texture		Scanline parameter.
	 * @param	ltu			Scanline parameter.
	 * @param	ltuc		Scanline parameter.
	 * @param	rtu			Scanline parameter.
	 * @param	rtuc		Scanline parameter.
	 * @param	ltv			Scanline parameter.
	 * @param	ltvc		Scanline parameter.
	 * @param	rtv			Scanline parameter.
	 * @param	rtvc		Scanline parameter.
	 * @param	ldr			Scanline parameter.
	 * @param	ldrc		Scanline parameter.
	 * @param	rdr			Scanline parameter.
	 * @param	rdrc		Scanline parameter.
	 * @param	phongTable	Scanline parameter.
	 * @param	lsx			Scanline parameter.
	 * @param	lsxc		Scanline parameter.
	 * @param	rsx			Scanline parameter.
	 * @param	rsxc		Scanline parameter.
	 * @param	lsy			Scanline parameter.
	 * @param	lsyc		Scanline parameter.
	 * @param	rsy			Scanline parameter.
	 * @param	rsyc		Scanline parameter.
	 * @param	lsf			Scanline parameter.
	 * @param	lsfc		Scanline parameter.
	 * @param	rsf			Scanline parameter.
	 * @param	rsfc		Scanline parameter.
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
		final int        tw            = ( texture != null ) ? texture[0].length : 0;
		final int        th            = ( texture != null ) ? texture.length    : 0;
//		final int        ma            = (colorRGB >> 24) & 0xFF;
		final int        mr            = (colorRGB >> 16) & 0xFF;
		final int        mg            = (colorRGB >>  8) & 0xFF;
		final int        mb            =  colorRGB        & 0xFF;

		int i,j,c,r,g,b,s;

		int     h1;		// 'pixel' Horizontal coordinate counter     * 2^8
		int     h2;		// 'pixel' Horizontal coordinate coefficient * 2^8
		long	d1;		// 'pixel' Z-coordinate counter     * 2^8
		long	d2;		// 'pixel' Z-coordinate coefficient * 2^8
		long	tu1;	// 'pixel' Texture U-coordinate counter     * 2^8
		long	tu2;	// 'pixel' Texture U-coordinate coefficient * 2^8
		long	tv1;	// 'pixel' Texture V-coordinate counter     * 2^8
		long	tv2;	// 'pixel' Texture V-coordinate coefficient * 2^8
		int		dr1;	// 'pixel' Diffuse reflection counter     * 2^8
		int		dr2;	// 'pixel' Diffuse reflection coefficient * 2^8
		int		sx1;	// 'pixel' Specular X-coordinate counter     * 2^8
		int		sx2;	// 'pixel' Specular X-coordinate coefficient * 2^8
		int		sy1;	// 'pixel' Specular Y-coordinate counter     * 2^8
		int		sy2;	// 'pixel' Specular Y-coordinate coefficient * 2^8
		int		sf1;	// 'pixel' Specular intensity factor counter     * 2^8
		int		sf2;	// 'pixel' Specular intensity factor coefficient * 2^8

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

				d2  = ( d2  - d1  ) / i;
				tu2 = ( tu2 - tu1 ) / i;
				tv2 = ( tv2 - tv1 ) / i;
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
					 d1 -=  d2 * h1;
					tu1 -= tu2 * h1;
					tv1 -= tv2 * h1;
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
								final int myU = (int)( ( tu1 / d1 ) % tw );
								final int myV = (int)( ( tv1 / d1 ) % th );
								c = texture[ myV + (myV<0?th:0)][ myU + (myU<0?tw:0) ];
								// original
								//c = texture[ (int)( ( tv1 / d1 ) % th )) ]
								           //[ (int)( ( tu1 / d1 ) % tw )) ];
								s = phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

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

								s = phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

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
								final int myU = (int)( ( tu1 / d1 ) % tw );
								final int myV = (int)( ( tv1 / d1 ) % th );
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
}
