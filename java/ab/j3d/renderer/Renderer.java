package common.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 1999,2000,2001 - All Rights Reserved
 *
 * This software may not be used, copyied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */

import java.awt.*;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.event.*;
//import ab.components.*;
import common.db.TextureSpec;
import common.model.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Stack;

/**
 * The renderer....
 *
 * @author	Peter S. Heijnen
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class Renderer
	implements ComponentListener, Runnable, MouseListener, MouseMotionListener, ImageProducer
{
	private static final ColorModel _colorModel = ColorModel.getRGBdefault();
	
	/*
	 * Values for control mode.
	 */
	public static final int ZOOM   = 1;
	public static final int PAN    = 2;
	public static final int ROTATE = 3;

	/*
	 * Available render modes to pass to renderScene()
	 */
	public final static int QUICK = 1;
	public final static int FULL  = 2;

	/**
	 * Component that uses this renderer.
	 */
	protected final Component _owner;
	
	/**
	 * Transform of model.
	 */	
	private Transform _modelTransform;

	/**
	 * Transform of light.
	 */	
	private Transform _lightTransform;

	/**
	 * Base node of rendered model.
	 */
	private Transform _base;

	/**
	 * The camera node with which this ViewPanel is associated. The node can
	 * be considered the model of ViewPanel.
	 */
	protected Camera _camera;

	/**
	 * Transform of camera.
	 */
	private Transform _cameraTransform;
	
	/*
	 * Minimum/maximum coordiantes of displayed model.
	 */
	private float _minX = 0f;
	private float _minY = 0f;
	private float _minZ = 0f;
	private float _maxX = 0f;
	private float _maxY = 0f;
	private float _maxZ = 0f;
	
	/**
	 * This is the current "control mode" of the view. This
	 * may be ZOOM, PAN, or ROTATE.
	 */
	private int _controlMode = ROTATE;

	/*
	 * Control variables.
	 */
	private float _mouseSensitivity = 1.4f;
	private float _movementSpeed = 20.0f;
	private float _zoomSpeed = 30.0f;
	
	private int   _initX;
	private int   _initY;
	private float _initRotationX;
	private float _initRotationZ;
	private float _initTranslationX;
	private float _initTranslationY;
	private float _initTranslationZ;

	/**
	 * This is the current rendering mode (either QUICK or FULL),
	 * this may set to QUICK during frequent updates and to FULL
	 * when those updates are done. The renderer may use this to
	 * speed up its operation (e.g. by lowering detail) during
	 * updates.
	 */
	private int _renderingMode = FULL;

	/**
	 * This flag is set when the update thread should run.
	 */
	private boolean _isRunning;

	/**
	 * Flag to indicate that the panel must be updated.
	 */
	private boolean _updatePending;

	/**
	 * This boolean is set during rendering.
	 */
	protected boolean _updating = true;

	/**
	 * Thread that handles the update process.
	 */
	private Thread _updateThread;

	/**
	 * Width of frame.
	 */
	protected int _width;

	/**
	 * Height of frame.
	 */
	protected int _height;

	/**
	 * Background color (ARGB)
	 */
	public int _background = 0x0FFC0C0C0;
	
	/**
	 * Z-Buffer. Each entry corresponds to a pixel's Z-coordinate. If a
	 * pixel is drawn, its Z-coordinate must exceed this value to be
	 * visible. In such a case, the Z-buffer is updated with the new
	 * Z-coordinate.
	 */
	private int[] _depthBuffer = null;

	/**
	 * Actual frame buffer with storage for all pixels. Each pixel is
	 * stored in the usual 0xaarrggbb format.
	 */
	private int[] _pixels = null;

	/**
	 * Registered ImageConsumers.
	 */
	private Vector _imageConsumers = new Vector();
	
	/**
	 * Image with frame buffer contents.
	 */
	protected Image _image = null;
	
	/**
	 * This boolean flag indicates if the frame image was painted.
	 */
	protected boolean _imagePainted = false;
	
	/**
	 * Flag to indicate that a temporary wireframe is drawn
	 * during the rendering process.
	 */
	protected boolean _showTemporaryWireframe = true;

	/*
	 * Solid renderer temporary storage.
	 */
	private LeafCollection _sObjects         = new LeafCollection();
	private LeafCollection _sLights          = new LeafCollection();
	private RenderObject _solidObject = new RenderObject();

	/*
	 * Wireframe renderer temporary storage.
	 */
	private LeafCollection _wfObjects    = new LeafCollection();
	private RenderObject[] _wireObjects = null;

	///**
	 //* Flag to indicate that a wireframe is to be drawn over the
	 //* rendered image. This may help interpreting the image.
	 //*/
	//protected boolean _showWireframeOverlay = false;

	//public void setShowWireframeOverlay( boolean flag )
	//{
		//if ( flag != _showWireframeOverlay )
		//{
			//_showWireframeOverlay = flag;
			//requestUpdate();
		//}
	//}

	//public boolean isShowWireframeOverlay()
	//{
		//return _showWireframeOverlay;
	//}
	
	///**
	 //* Flag to select base/light control.
	 //*/
	//protected boolean _controlLight = false;
	
	/**
	 * Construct renderer for the specified component with the specified
	 * initial dimensions.
	 *
	 * @param	owner	Owner component of renderer.
	 * @param	width	Initial width in pixels.
	 * @param	height	Initial height in pixels.
	 */
	public Renderer( Component owner , int width , int height )
	{
		initialize( width , height );
		_owner = owner;

		/*
		 * Build world
		 */
		TreeNode world = buildWorld();
		reset();

		/*
		 * Prepare 'owner' component.
		 */
		if ( _owner != null )
		{
			_owner.setSize( width , height );
			_owner.addComponentListener( this );
			_owner.addMouseListener( this );
			_owner.addMouseMotionListener( this );
			
			requestUpdate();
		}
	}

	/**
	 * Adds an ImageConsumer to the list of consumers interested in
	 * data for this image.
	 *
	 * @see ImageConsumer
	 */
	public void addConsumer( ImageConsumer ic )
	{
		synchronized ( _imageConsumers )
		{
			if ( !_imageConsumers.contains( ic ) )
				_imageConsumers.addElement( ic );
			
		    final int        width  = _width;
		    final int        height = _height;
			final int[]      p      = _pixels;
		    final ColorModel cm     = _colorModel;
			    
			if ( isConsumer( ic ) )
			    ic.setColorModel( _colorModel );

			if ( isConsumer( ic ) )
			    ic.setDimensions( _width , _height );
			    
			if ( isConsumer( ic ) )
			    ic.setHints( ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES );

		    if ( isConsumer( ic ) )
				sendPixels( ic );
		}
	}

	/**
	 * This method constructs the graphics tree and therefore
	 * defined what is to be seen by the user.
	 *
	 * @param	trasnforms	Vector to which transforms must be
	 *						added that can be manipulated by the
	 *						user.
	 *
	 * @return	Graphics tree (the world).
	 */
	protected TreeNode buildWorld()
	{
		Transform location;
		
		/*
		 * Create world.
		 */
		TreeNode world = new TreeNode();
		
		/*
		 * Add base nodes for object.
		 */
		world
			.addChild( _modelTransform = new Transform() )
				.addChild( _base = new Transform() );
		
		/*
		 * Add ambient light to make sure everything is visible.
		 */
		world.addChild( new Light( 500 , -1f ) ); // 384
		
		/*
		 * Add a point light for more lively lighting effects.
		 */
		world
			.addChild( _lightTransform = new Transform( -750.0f , -2500.0f , 1700.0f ) )
				.addChild( new Light( 10000 , 30.0f ) );

		/*
		 * Put camera in graphics tree.
		 */
		world
			.addChild( _cameraTransform = new Transform( 0 , -3000 , 0 ) )
				.addChild( _camera = new Camera( 300f , 60f ) );

		return( world );
	}

	/**
	 * Place base in center of view.
	 */
	public void center()
	{
		_base.setTranslation(
			-( _minX + _maxX ) / 2f ,
			-( _minY + _maxY ) / 2f ,
			-( _minZ + _maxZ ) / 2f );
	}

	/**
	 * Respond to 'component hidden' event. This is used to stop the update thread
	 * of the renderer.
	 *
	 * @param	e	Component event.
	 */
	public synchronized void componentHidden( ComponentEvent e ) 
	{
		_isRunning = false;
		requestUpdate();
	}

	/**
	 * Respond to 'component moved' event.
	 *
	 * @param	e	Component event.
	 */
	public void componentMoved( ComponentEvent e ) 
	{
	}

	/**
	 * Respond to 'component resized' event. This is used to re-initialize the
	 * image buffers to the new component size.
	 *
	 * @param	e	Component event.
	 */
	public void componentResized( ComponentEvent e ) 
	{
		if ( e.getSource() == _owner )
		{
			_depthBuffer = null;
			Dimension d = e.getComponent().getSize();
			initialize( d.width , d.height );
			requestUpdate();
		}
	}

	/**
	 * Respond to 'component shown' event. This is used to start the update thread
	 * of the renderer.
	 *
	 * @param	e	Component event.
	 */
	public synchronized void componentShown( ComponentEvent e )
	{
		if ( e.getSource() == _owner )
		{
			_depthBuffer = null;
			Dimension d = e.getComponent().getSize();
			initialize( d.width , d.height );
			requestUpdate();
		}

		if ( _isRunning )
			return;

		_isRunning = true;
		_updateThread = new Thread( this );
		_updateThread.setPriority( Thread.MIN_PRIORITY );
		_updateThread.setName( "" + this );
		_updateThread.start();
	}

	/**
	 * Get base transform of rendered model.
	 */
	public Transform getBase()
	{
		return _base;
	}

	/**
	 * Get transform for model.
	 */
	public Transform getModelTransform()
	{
		return _modelTransform;
	}

	/**
	 * Get pixel data of rendered image.
	 *
	 * @return	Pixel data.
	 */
	public int[] getPixels()
	{
		return _pixels;
	}

	/**
	 * Get string with view settings of renderer.
	 *
	 * @return	String with view settings of renderer.
	 */
	public String getViewSettings()
	{
		return Bounds3D.INIT.set(
			Vector3D.INIT.set(
				_modelTransform.getRotationX() ,
				_modelTransform.getRotationY() ,
				_modelTransform.getRotationZ() ) ,
			Vector3D.INIT.set(
				_cameraTransform.getTranslationX() ,
				_cameraTransform.getTranslationY() ,
				_cameraTransform.getTranslationZ() )
			).toString();
	}

	/**
	 * Initiailization of renderer.
	 *
	 * @param	width		Width of renderer view.
	 * @param	height		Height of renderer view.
	 */
	private final void initialize( int width , int height )
	{
		if ( width == _width && height == _height )
			return;

		_width       = width;
		_height      = height;
		_depthBuffer = null;
		_pixels      = null;
		_image       = null;


		if ( _camera != null )
		{
			int minSize = Math.min( width , height );
			//_camera.scale = minSize * 0.000375f;
			//, true , 5000.0f ) );
		}
			
		requestUpdate();
	}

	/**
	 * Determine if an ImageConsumer is on the list of consumers currently
	 * interested in data for this image.
	 *
	 * @return true if the ImageConsumer is on the list; false otherwise
	 *
	 * @see ImageConsumer
	 */
	public boolean isConsumer( ImageConsumer ic )
	{
		return _imageConsumers.contains( ic );
	}

	/**
	 * Get flag to indicate that a temporary wireframe is drawn
	 * during the rendering process.
	 *
	 * @return	<code>true</code> if a temporary wireframe is
	 *			drawn, <code>false</code> if not.
	 */
	public boolean isShowTemporaryWireframe()
	{
		return( _showTemporaryWireframe );
	}

	/**
	 * Handle mouse button 'clicked' event.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseClicked( MouseEvent e )
	{
		//if ( e.getClickCount() == 3 )
		//{
			//if ( ( e.getModifiers() & MouseEvent.BUTTON1_MASK ) != 0 )
			//{
				//_showWireframeOverlay = !_showWireframeOverlay;
				//requestUpdate();
			//}
			//else
			//{
				//_controlLight = !_controlLight;
			//}
		//}
	}

	/**
	 * Handle mouse 'dragged' event. When this event occurs, manipulate
	 * the view settings and repaint the view. The renderer is set to QUICK mode,
	 * to allow fast manipulation until the mouse button is released.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseDragged( MouseEvent e )
	{
		int dx = e.getX() - _initX;
		int dy = e.getY() - _initY;

		
		int modifiers = e.getModifiers();
		int mode      = _controlMode;

		if ( ( modifiers & e.BUTTON2_MASK ) != 0 )
		{
			mode = PAN;
		}
		else if ( ( modifiers & e.BUTTON3_MASK ) != 0 )
		{
			mode = ZOOM;
		}

		final Transform x = /*_controlLight ? _lightTransform :*/ _cameraTransform;
		
		switch ( mode )
		{
			case ROTATE :
				setRenderingMode( Renderer.QUICK );
				_modelTransform.setRotationZ( _initRotationZ + _mouseSensitivity * dx );
				_modelTransform.setRotationX( _initRotationX - _mouseSensitivity * dy );
				requestUpdate();
				break;

			case PAN :
				setRenderingMode( Renderer.QUICK );
				x.setTranslation(
					_initTranslationX - dx * _movementSpeed ,
					_initTranslationY ,
					_initTranslationZ + dy * _movementSpeed );
				requestUpdate();
				break;
				
			case ZOOM :
				setRenderingMode( Renderer.QUICK );
				x.setTranslation(
					_initTranslationX ,
					Math.max( -10000 , _initTranslationY - dy * _movementSpeed/* )*/ ) ,
					_initTranslationZ );
				requestUpdate();
				break;
		}
	}

	/**
	 * Handle mouse 'entered' event.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseEntered( MouseEvent e )
	{
	}

	/**
	 * Handle mouse 'exited' event.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseExited( MouseEvent e )
	{
	}

	/**
	 * Handle mouse 'moved' event.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseMoved( MouseEvent e )
	{
	}

	/**
	 * Handle mouse button 'pressed' event. When this event occurs, request focus,
	 * and save view settings (so that we can manipulate it later).
	 *
	 * @param	e	Mouse event.
	 */
	public void mousePressed( MouseEvent e )
	{
		((Component)e.getSource()).requestFocus();

		_initX = e.getX();
		_initY = e.getY();
	
		final Transform x = /*_controlLight ? _lightTransform :*/ _cameraTransform;
		
		_initRotationX    = _modelTransform.getRotationX();
		_initRotationZ    = _modelTransform.getRotationZ();
		_initTranslationX = x.getTranslationX();
		_initTranslationY = x.getTranslationY();
		_initTranslationZ = x.getTranslationZ();
	}

	/**
	 * Handle mouse button 'released' event. When this event occurs, change the
	 * renderer back to FULL mode, so that the manipulated view will be rendered
	 * fully.
	 *
	 * @param	e	Mouse event.
	 */
	public void mouseReleased( MouseEvent e )
	{
		setRenderingMode( Renderer.FULL );
		//requestUpdate();
	}

	/**
	 * Paint the component.
	 *
	 * @param	g		Graphics context.
	 */
	public void paint( Graphics g )
	{
		if ( _camera != null )
		{
			if ( _updating || (_image == null) )
			{
				_imagePainted = false;
				if ( _showTemporaryWireframe )
				{
				    g.setColor( _owner.getForeground() );
					renderWireframe( g , _camera , _width , _height );
				}
			}
			else
			{
				_imagePainted = true;
				//System.out.println( "*drawImage*" );
				g.drawImage( _image , 0 , 0 , _owner );
			}
		}
	}

	/**
	 * Remove an ImageConsumer from the list of consumers interested in
	 * data for this image.
	 *
	 * @see ImageConsumer
	 */
	public void removeConsumer( ImageConsumer ic )
	{
		//System.out.println( "*removeConsumer:" + ic + "*" );
		_imageConsumers.removeElement( ic );
	}

	/**
	 * Render scene from camera.
	 */
	public void renderSolid( final int backgroundColor )
	{
		final Camera camera = _camera;
		final int    width  = _width;
		final int    height = _height;
		
		_updatePending = false;
		_updating = true;
		
		/*
		 * Initialize frame and Z buffer. Re-create these buffers if necessary.
		 */
		final int bufferSize = width * height;
		
		if ( _pixels  == null || _pixels .length < bufferSize ||
		     _depthBuffer == null || _depthBuffer.length < bufferSize )
		{
			_pixels      = new int[ bufferSize ];
			_depthBuffer = new int[ bufferSize ];
			_image       = null;
		}

		if ( _owner != null && _image == null )
		{
			_image = _owner.createImage( this );
			_imagePainted = false;
		}

		/*
		 * Clear buffers in 2 phases:
		 *  1) use for-loop to clear fixed number of entries (512 seems reasonable?);
		 *  2) use System.arraycopy for base 2 fill of buffers (how expensive is this?).
		 */
		for ( int i = ( bufferSize < 512 ) ? bufferSize : 512 ; --i >= 0 ; )
		{
			_pixels     [ i ] = backgroundColor;
			_depthBuffer[ i ] = 0x7FFFFFFF; //0x80000000;
		}

		for ( int i = 512 ; i < bufferSize ; )
		{
			if ( _updatePending ) { _updating = false; return; }
			int c = bufferSize - i;
			if ( c > i ) c = i;
			System.arraycopy( _pixels      , 0 , _pixels      , i , c );
			System.arraycopy( _depthBuffer , 0 , _depthBuffer , i , c );
			i += c;
		}
		
		/*
		 * Gather objects in this world.
		 */
		_sObjects.clear();
		camera.gatherLeafs( _sObjects , Object3D.class , Matrix3D.INIT , true );
		if ( _updatePending ) { _updating = false; return; }

		/*
		 * Gather lights in this world. Prepare per-light cached array.
		 */
		_sLights.clear();
		camera.gatherLeafs( _sLights , Light.class , Matrix3D.INIT , true );

		if ( _updatePending ) { _updating = false; return; }
				
		/*
		 * Cycle through all available models and draw them.
		 */
		for ( int i = 0 ; i < _sObjects.size() ; i++ )
		{
			renderSolidObject( (Object3D)_sObjects.getNode( i ) , _sObjects.getMatrix( i ) , camera , width , height );
		}

		//System.out.println();
		_updating = false;
		return;
	}

	protected final void renderSolidFace( RenderObject.Face face )
	{
		int i,j,k,m,n;
		long d1,d2;

		final int         width         = _width;
		final int         height        = _height;
		
		final RenderObject ro = face.getRenderObject();

		final int[]			ph = ro.ph;
		final int[]			pv = ro.pv;
		final long[]		pd = ro.pd;
		final int[]			vertexIndices	= face.vi;
		final TextureSpec	texture			= face.getTexture();
		final int[][]		pixels			= face.getTexturePixels();
		final boolean		hasTexture		= pixels != null;
		final int[]			tus				= hasTexture ? face.getTextureU() : null;
		final int[]			tvs				= hasTexture ? face.getTextureV() : null;
		final int			colorRGB		= texture.getARGB();
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
		
		final short[][] phongTable = ( m > 0xFF ) && ( sxs != null ) && ( sys != null ) && ( sfs != null ) ? texture.getPhongTable() : null;

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
		int     nextV = v;
		int     dx;
		
		int		li1  = first;	// Index in shape to 1st vertex of segement
		int		li2  = first;	// Index in shape to 2nd vertex of segement
		int		lv2  = minV;	// 'left'  vertical coordinate at end of segment
		
		int		ri1  = first;	// Index in shape to 1st vertex of segement
		int		ri2  = first;	// Index in shape to 2nd vertex of segement
		int		rv2  = minV;	// 'right' vertical coordinate at end of segment
		
		int		lh   = 0;		// 'left'  Horizontal coordinate counter     * 2^8
		int		lhc  = 0;		// 'left'  Horizontal coordinate coefficient * 2^8
		int		rh   = 0;		// 'right' Horizontal coordinate counter     * 2^8
		int		rhc  = 0;		// 'right' Horizontal coordinate coefficient * 2^8
		
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

			i = ( lh < rh ) ? lh : rh;

			renderSolidScanlines( v , v , lh , 0 , rh , 0 , ld , 0 , rd , 0 , colorRGB ,
				pixels , ltu , 0 , rtu , 0 , ltv , 0 , rtv , 0 , ldr , 0 , rdr , 0 ,
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
			
			renderSolidScanlines( v , nextV , lh , lhc , rh , rhc , ld , ldc , rd , rdc , colorRGB ,
				pixels , ltu , ltuc , rtu , rtuc , ltv , ltvc , rtv , rtvc , ldr , ldrc , rdr , rdrc ,
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
	 * Draw a line.
	 */
	protected final void renderSolidLine3D( int x1 , int y1 , int z1 , int x2 , int y2 , int z2 , int c )
	{
		int i,j,x,y,z;
		final int[] db = _depthBuffer;
		final int[] sb = _pixels;
		
		/*
		 * Change x2,y2,z2 into delta
		 */
		x2 -= x1;
		y2 -= y1;
		z2 -= z1;

		/*
		 * Handle 'horizontal' oriented line.
		 */
		
		if ( Math.abs( x2 ) > Math.abs( y2 ) )
		{
			if ( x2 < 0 )
			{
				x1 -= ( x2 = -x2 );
				y1 -= ( y2 = -y2 );
				z1 -= ( z2 = -z2 );
			}

			final int hx = x2 / 2;
			for ( i = 0 ; i <= x2 ; i++ )
			{
				x = x1 + i;
				y = y1 + ( y2 * i + hx ) / x2;
				z = z1 + ( z2 * i + hx ) / x2;
				
				if ( x >= 0 && x < _width && y >= 0 && y < _height && z >= db[ j = y * _width + x ] )
				{
					db[ j ] = z;
					sb[ j ] = c;
				}
			}
		}

		/*
		 * Handle dot
		 */
		else if ( y2 == 0 )
		{
			if ( x1 >= 0 && x1 < _width && y1 >= 0 && y1 < _height && z1 >= db[ j = y1 * _width + x1 ] )
			{
				db[ j ] = z1;
				sb[ j ] = c;
			}
			return;
		}

		/*
		 * Handle 'vertical' oriented line.
		 */
		else
		{
			if ( y2 < 0 )
			{
				x1 -= ( x2 = -x2 );
				y1 -= ( y2 = -y2 );
				z1 -= ( z2 = -z2 );
			}

			final int hy = y2 / 2;
			for ( i = 0 ; i <= y2 ; i++ )
			{
				x = x1 + ( i * x2 + hy ) / y2;
				y = y1 + i;
				z = z1 + ( z2 * i + hy ) / y2;
				
				if ( x >= 0 && x < _width && y >= 0 && y < _height && z >= db[ j = y * _width + x ] )
				{
					db[ j ] = z;
					sb[ j ] = c;
				}
			}
		}
	}

	/**
	 * Render scene from camera.
	 *
	 * @param	camera			Node with camera that defines the view.
	 * @param	renderingMode	Rendering mode to use (QUICK or FULL).
	 */
	protected void renderSolidObject( 
		final Object3D object , final Matrix3D cameraXform , final Camera camera , int width , int height )
	{
		_solidObject.set( object , cameraXform , camera.aperture , camera.zoom , width , height , true );
		if ( _updatePending ) return;
			
		_solidObject.setLights( _sLights );
		if ( _updatePending ) return;
		
		/*
		 * For each face:
		 *   - determine if it's invisible (outside view volume & backface culling)
		 *   - calculate weight point (average of vertices)
		 */
		final int[]	ph = _solidObject.ph;
		final int[]	pv = _solidObject.pv;
		
		int i,j,k,h1,v1,h2,v2;
		int[] vi;
				
	    final int c = ( _owner != null ) ? _owner.getForeground().getRGB() : 0xFF000000;
		
		for ( RenderObject.Face face = _solidObject.faces ; face != null ; face = face.next )
		{
			face.applyLighting();

			/*
			 * Draw polygon.
			 */
			renderSolidFace( face );

			/*
			 * Draw wireframe
			 */
			//if ( _showWireframeOverlay )
			//{
				//vi = face.vi;
				
				//h1 = ph[ k = vi[ vi.length - 1 ] ] >> 8;
				//v1 = pv[ k                       ] >> 8;
				
				//for ( j = 0 ; j < vi.length ; j++ )
				//{
					//h2 = ph[ k = vi[ j ] ] >> 8;
					//v2 = pv[ k           ] >> 8;
					
					//renderSolidLine3D( h1 , v1 , 0x7FFFFFFF , h2 , v2 , 0x7FFFFFFF , c );

					//h1 = h2;
					//v1 = v2;
					
				//}
			//}
		}
	}

	protected final void renderSolidScanlines(
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
		final int        width         = _width;
		final int        height        = _height;
		final int        tw            = ( texture != null ) ? texture[0].length : 0;
		final int        th            = ( texture != null ) ? texture.length    : 0;
		final int        ma            = (colorRGB >> 24) & 0xFF; 
		final int        mr            = (colorRGB >> 16) & 0xFF; 
		final int        mg            = (colorRGB >>  8) & 0xFF; 
		final int        mb            =  colorRGB        & 0xFF;

		int i,j,c,r,g,b,s;
		
		int     h1   = 0;		// 'pixel' Horizontal coordinate counter     * 2^8
		int     h2   = 0;		// 'pixel' Horizontal coordinate coefficient * 2^8
		long	d1   = 0;		// 'pixel' Z-coordinate counter     * 2^8
		long	d2   = 0;		// 'pixel' Z-coordinate coefficient * 2^8
		long	tu1  = 0;		// 'pixel' Texture U-coordinate counter     * 2^8
		long	tu2  = 0;		// 'pixel' Texture U-coordinate coefficient * 2^8
		long	tv1  = 0;		// 'pixel' Texture V-coordinate counter     * 2^8
		long	tv2  = 0;		// 'pixel' Texture V-coordinate coefficient * 2^8
		int		dr1  = 0;		// 'pixel' Diffuse reflection counter     * 2^8
		int		dr2  = 0;		// 'pixel' Diffuse reflection coefficient * 2^8
		int		sx1  = 0;		// 'pixel' Specular X-coordinate counter     * 2^8
		int		sx2  = 0;		// 'pixel' Specular X-coordinate coefficient * 2^8
		int		sy1  = 0;		// 'pixel' Specular Y-coordinate counter     * 2^8
		int		sy2  = 0;		// 'pixel' Specular Y-coordinate coefficient * 2^8
		int		sf1  = 0;		// 'pixel' Specular intensity factor counter     * 2^8
		int		sf2  = 0;		// 'pixel' Specular intensity factor coefficient * 2^8

		int[] db = _depthBuffer;
		
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
							//if ( (j = (int)(d1 >> 8)) >= _zBuffer[ i ] )
							if ( (j = (int)(0x7FFFFFFFFFl / d1)) < db[ i ] )
							{
								db[ i ] = j;

								// fix , tu1 and tv1 become < 0 don't know how that happens
								int myU = (int)( ( tu1 / d1 ) % tw );
								int myV = (int)( ( tv1 / d1 ) % th );
								c = texture[ myV + (myV<0?th:0)][ myU + (myU<0?tw:0) ];
								// original								
								//c = texture[ (int)( ( tv1 / d1 ) % th )) ]
								           //[ (int)( ( tu1 / d1 ) % tw )) ];
								s = phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

								if ( (r = (dr1 * ((c >> 16) & 0xFF) + s) >> 16) > 255 ) r = 255;
								if ( (g = (dr1 * ((c >>  8) & 0xFF) + s) >> 16) > 255 ) g = 255;
								if ( (b = (dr1 * ( c        & 0xFF) + s) >> 16) > 255 ) b = 255;

								_pixels[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
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
							if ( (j = (int)(0x7FFFFFFFFFl / d1)) < db[ i ] )
							{
								db[ i ] = j;

								s = phongTable[ sy1 >> 8 ][ sx1 >> 8 ] * sf1;

								if ( ( r = ( dr1 * mr + s ) >> 16 ) > 255 ) r = 255;
								if ( ( g = ( dr1 * mg + s ) >> 16 ) > 255 ) g = 255;
								if ( ( b = ( dr1 * mb + s ) >> 16 ) > 255 ) b = 255;

								_pixels[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
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
							if ( (j = (int)(0x7FFFFFFFFFl / d1)) < db[ i ] )
							{
								db[ i ] = j;

								// fix , tu1 and tv1 become < 0 don't know how that happens
								int myU = (int)( ( tu1 / d1 ) % tw );
								int myV = (int)( ( tv1 / d1 ) % th );
								c = texture[ myV + (myV<0?th:0)][ myU + (myU<0?tw:0) ];
								// original								
								//c = texture[ (int)( ( tv1 / d1 ) % th )) ]
								           //[ (int)( ( tu1 / d1 ) % tw )) ];
								
								if ( (r = (dr1 * ((c >> 16) & 0xFF)) >> 16) > 255 ) r = 255;
								if ( (g = (dr1 * ((c >>  8) & 0xFF)) >> 16) > 255 ) g = 255;
								if ( (b = (dr1 * ( c        & 0xFF)) >> 16) > 255 ) b = 255;

								_pixels[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
							tu1 += tu2;
							tv1 += tv2;
						}
					}
					else // no texture
					{
						//System.out.println( "Starting pixels at (" + h1 + "," + y + ")" );
						//System.out.println( "   length  = " + ( h2 + 1 ) );
						//System.out.println( "   Z.start = " + d1 );
						
						for ( i = width * v + h1 ; h2-- >= 0 ; i++ )
						{
							if ( (j = (int)(0x7FFFFFFFFFl / d1)) < db[ i ] )
							{
								db[ i ] = j;
								
								if ( ( r = ( dr1 * mr ) >> 16 ) > 255 ) r = 255;
								if ( ( g = ( dr1 * mg ) >> 16 ) > 255 ) g = 255;
								if ( ( b = ( dr1 * mb ) >> 16 ) > 255 ) b = 255;

								_pixels[ i ] = 0xFF000000 + (r << 16) + (g << 8) + b;
							}

							d1  += d2;
							dr1 += dr2;
						}
						
						//System.out.println( "   Z.end    = " + ( d1 - d2 ) );
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
	 * Render scene from camera.
	 *
	 * @param	target	Target component.
	 * @param	camera	Node with camera that defines the view.
	 */
	public synchronized final void renderWireframe( Graphics g , final Camera camera , final int width , final int height )
	{
		/*
		 * Gather leafs with models in this world.
		 */
		_wfObjects.clear();
		_camera.gatherLeafs( _wfObjects , Object3D.class , Matrix3D.INIT , true );

		/*
		 * Prepare object buffer.
		 */
		final int nrObjects = _wfObjects.size();
		if ( nrObjects < 1 )
			return;

		RenderObject[] objects = _wireObjects;
		if ( _wireObjects == null || nrObjects > _wireObjects.length )
		{
			objects = new RenderObject[ nrObjects ];
			if ( _wireObjects != null )
				System.arraycopy( _wireObjects , 0 , objects , 0 , _wireObjects.length );
			_wireObjects = objects;
		}
		
		/*
		 * Add all objects and draw them.
		 */
		for ( int i = 0 ; i < nrObjects ; i++ )
		{
			RenderObject ro = _wireObjects[ i ];
			if ( ro == null )
				_wireObjects[ i ] = ro = new RenderObject();

			ro.set( (Object3D)_wfObjects.getNode( i ) , _wfObjects.getMatrix( i ) , camera.aperture , camera.zoom , width , height , true );
		}

		for ( int i = 0 ; i < nrObjects ; i++ )
		{
			RenderObject ro = _wireObjects[ i ];
			
			for ( RenderObject.Face face = ro.faces ; face != null ; face = face.next )
				renderWireframeFace( g , face );
		}

		
		//class Iter
		//{
			//int	obj = 0;
			//RenderObject.Face face = null;

			//public RenderObject.Face next()
			//{
				//while ( face == null || face.index < 0 )
				//{
					//if ( face == null )
					//{
						//if ( obj >= nrObjects )
							//return null;

						//face = _wireObjects[ obj++ ].faces;
					//}
					//else
					//{
						//face = face.next;
					//}
				//}
				
				//RenderObject.Face result = face;
				//face = face.next;
				//return result;
			//}

			//public void reset()
			//{
				//obj = 0;
				//face = null;
			//}
		//}


		//Iter iter = new Iter();
		//RenderObject.Face face,other;

		//for (;;)
		//{
			//iter.reset();
			//face = iter.next();
			//if ( face == null )
				//return;

			////while ( ( other = iter.next() ) != null )
			////{
				////if ( other.isBehind( face ) )
				////{
					////face = other;
					////iter.reset();
				////}
			////}

			//renderWireframeFace( g , face );
			//face.index = -1;
		//}	
	}

	/**
	 * Render scene from camera.
	 *
	 * @param	target	Target component.
	 * @param	camera	Node with camera that defines the view.
	 */
	protected void renderWireframeFace( Graphics g , RenderObject.Face face )
	{
		int i,k,h1,v1,h2,v2;
		
		int[] vi = face.vi;
		if ( vi.length < 3 )
			return;
		
		int[] h = new int[ vi.length ];
		int[] v = new int[ vi.length ];

		final int[] ph = face.this$0.ph;
		final int[] pv = face.this$0.pv;
		
		for ( i = 0 ; i < vi.length ; i++ )
		{
			h[ i ] = ph[ k = vi[ i ] ] >> 8;
			v[ i ] = pv[ k           ] >> 8;
		}

		int c = 0xFFFFFF; //face.getTexture().getARGB();
		//float nx = 0.1f + 0.5f * ( 1.0f + face.nx );
		//float ny = 0.1f + 0.0f * ( 1.0f + face.ny );
		//float nz = 0.1f + 0.5f * ( 1.0f + face.nz );

		//g.setColor( new Color(	
			//Math.min( (int)(nx * ((c >> 16) & 255)) , 255 ) ,
			//Math.min( (int)(ny * ((c >>  8) & 255)) , 255 ) ,
			//Math.min( (int)(nz * ( c        & 255)) , 255 ) ) );
	
		//g.fillPolygon( h , v , vi.length );

		//g.setColor( Color.black );
		
		h1 = h[ k = vi.length - 1 ];
		v1 = v[ k ];
		
		for ( i = 0 ; i < vi.length ; i++ )
		{
			h2 = h[ i ];
			v2 = v[ i ];
			
			g.drawLine( h1 , v1 , h2 , v2 );

			h1 = h2;
			v1 = v2;
		}
	}

	/**
	 * Requests that a given ImageConsumer have the image data delivered
	 * one more time in top-down, left-right order.
	 *
	 * @see ImageConsumer
	 */
	public void requestTopDownLeftRightResend( ImageConsumer ic ) 
	{
		if ( isConsumer( ic ) )
		    ic.setDimensions( _width , _height );
	    
	    if ( isConsumer( ic ) )
			ic.setPixels( 0 , 0 , _width , _height , _colorModel , _pixels , 0 , _width );
	    
	    if ( isConsumer( ic ) )
		    ic.imageComplete( ImageConsumer.SINGLEFRAMEDONE );
	}

	/**
	 * Set flag to indicate that the frame must be updated. This
	 * will only set the flag, render() must be called to actually
	 * do the rendering at a suitable time.
	 */
	public synchronized void requestUpdate()
	{
		_updatePending = true;
		notifyAll();
	}

	/**
	 * Reset to default render view.
	 */
	public void reset()
	{
		//_modelTransform.setRotationX( -10f );
		//_modelTransform.setRotationZ( -35f );
		_cameraTransform.setTranslation( 0f , -4000f , 0f );
		center();

		requestUpdate();
	}

	/**
	 * This is the thread body to update the rendered image on demand.
	 */
	public void run()
	{
		while ( _isRunning )
		{
			/*
			 * Only update if our owner is visible and we have a camera.
			 */
			if ( _owner.isVisible() && ( _camera != null ) )
			{
				try
				{
					/*
					 * If an update is pending, re-render the scene.
					 */
					if ( _updatePending )
					{
						_updatePending = false;

						/*
						 * Prepare for rendering.
						 */
						_updating = true;

						if ( _showTemporaryWireframe )
						{
							_owner.repaint();
							if ( _renderingMode == QUICK )
								continue;
						}

						/*
						 * Re-render the scene.
						 */
						renderSolid( _background );

						_updating = false;
						
						if ( !_updatePending )
						{
	
							updateImageConsumers();
							//System.out.println( "*repaint*" );
							_owner.repaint();
						}
								

						///*
						 //* Re-create offscreen image when imageSource was updated.
						 //*/
						//if ( _image == null && _imageSource != null )
						//{
							//_image = _owner.createImage( _imageSource );
							//_owner.repaint();
						//}

						///*
						 //* Inform ImageConsumer's that the image was updated.
						 //*/
						//_updating = false;
						//if ( !_updatePending )
							//_imageSource.newPixels( 0 , 0 , _width , _height );
						
						continue;
					}

					/*
					 * Repaint owner if it doesn't show the renderer image yet.
					 */
					if ( !_imagePainted )
					{
						//System.out.println( "*run.repaintX*" );
						_owner.repaint();
					}
				} 
				catch ( Exception e ) 
				{
					System.out.println( "Exception (" + e + ") during rendering!" );
					e.printStackTrace();
				}
			}

			/*
			 * No update needed, so sleep for a while until we are needed again.
			 */	
			try 
			{
				for (;;)
				{
					synchronized ( this ) 
					{
						if ( _updatePending ) break;
						wait( 300 ); 
					}
				}
			}
			catch ( Exception e ) { /*ignored*/ }
		}
		_updateThread = null;
	}

	/**
	 * Send rendered image data to an image consumer.
	 *
	 * @param	ic		Image consumer to send data to.
	 */
	private synchronized void sendPixels( ImageConsumer ic )
	{
		try
		{
		    final int        width  = _width;
		    final int        height = _height;
			final int[]      p      = _pixels;
		    final ColorModel cm     = _colorModel;
			    
			if ( isConsumer( ic ) )
			    ic.setDimensions( _width , _height );
			    
		    if ( isConsumer( ic ) )
		    {
				for ( int y = 0 , o = 0 ; y < height ; y++ , o += width )
				{
					if ( _updatePending ) break;
					ic.setPixels( 0 , y , width , 1 , cm , p , o , width );
				}
						    
			    if ( isConsumer( ic ) )
						ic.imageComplete( _updatePending ? ImageConsumer.IMAGEABORTED : ImageConsumer.SINGLEFRAMEDONE );
		    }
		}
		catch ( Exception e )
		{
		    if ( isConsumer( ic ) )
				ic.imageComplete( ImageConsumer.IMAGEERROR );
		}
	}

	/**
	 * Set current control mode for panel.
	 *
	 * @param	mode	Control mode for panel (ZOOM,PAN,ROTATE).
	 */
	public void setControlMode( int mode )
	{
		_controlMode = mode;
	}

	/**
	 * Set minimum/maximum coordiantes of displayed model.
	 */
	public void setLimits( float minX , float minY , float minZ , float maxX , float maxY , float maxZ )
	{
		_minX = minX;
		_minY = minY;
		_minZ = minZ;
		_maxX = maxX;
		_maxY = maxY;
		_maxZ = maxZ;
	}

	/**
	 * Set the rendering mode to use. This may be set to QUICK 
	 * during frequent updates, and to FULL when those updates
	 * are done. The renderer may use this to speed up its
	 * operation (e.g. by lowering detail) during updates.
	 *
	 * @param	mode	Rendering mode to use (QUICK or FULL).
	 */
	public void setRenderingMode( int mode )
	{
		if ( _renderingMode == mode )
			return;

		_renderingMode = mode;
		requestUpdate();
	}

	/**
	 * Set flag to indicate that a temporary wireframe is drawn
	 * during the rendering process.
	 *
	 * @param	show	<code>true</code> to indicate that a
	 *					temporary wireframe should be drawn,
	 *					<code>false</code> if not.
	 */
	public void setShowTemporaryWireframe( boolean show )
	{
		_showTemporaryWireframe = show;
	}

	/**
	 * Set view settings based on string previously returned by #getViewSettings().
	 *
	 * @return	String with view settings.
	 */
	public void setViewSettings( String settings )
	{
		if ( settings == null || settings.length() ==  0 )
			return;
			
		try
		{
			Bounds3D b = Bounds3D.fromString( settings );
			Vector3D rv = b.v1;
			_modelTransform.setRotation( rv.x , rv.y , rv.z );
			Vector3D tv = b.v2;
			_cameraTransform.setTranslation( tv.x , tv.y , tv.z );
		}
		catch ( Exception e ) { /* ignored */ }
	}

	/**
	 * Adds an ImageConsumer to the list of consumers interested in
	 * data for this image, and immediately start delivery of the
	 * image data through the ImageConsumer interface.
	 *
	 * @param	ic		Image consumer to add.
	 */
	public void startProduction( ImageConsumer ic )
	{
		addConsumer( ic );
	}

	/**
	 * Send new frame buffer contents to any ImageConsumers that are currently 
	 * interested in the data for this image source.
	 *
	 * @see ImageConsumer
	 */
	private void updateImageConsumers()
	{
		synchronized ( _imageConsumers )
		{
	 		for ( Enumeration e = _imageConsumers.elements() ; e.hasMoreElements() ; )
	 		{
		    	ImageConsumer ic = (ImageConsumer)e.nextElement();
		    	
			    if ( isConsumer( ic ) )
					sendPixels( ic );
	 		}
	    }
	}

}
