/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
import java.awt.Component;
import java.awt.Dimension;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JPopupMenu;

import com.sun.opengl.util.j2d.Overlay;
import com.sun.opengl.util.texture.Texture;

import ab.j3d.Matrix3D;
import ab.j3d.control.CameraControl;
import ab.j3d.control.ControlInput;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.Projector;
import ab.j3d.view.Projector.ProjectionPolicy;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * JOGL implementation of view model view.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLView
	extends ViewModelView
{
	/**
	 * Model being viewed.
	 */
	private JOGLModel _model;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final GLCanvas _viewComponent;

	/**
	 * Scene input translator for this View.
	 */
	private final ControlInput _controlInput;

	/**
	 * Front clipping plane distance in model units.
	 */
	private final double _frontClipDistance;

	/**
	 * Back clipping plane distance in model units.
	 */
	private final double _backClipDistance;

	/**
	 * Render thread.
	 */
	private RenderThread _renderThread;

	/**
	 * Texture cache
	 */
	private Map<String, SoftReference<Texture>> _textureCache;

	/**
	 * Maximum number of lights possible. Standard value is 8 because all
	 * OpenGL implementations have atleast this number of lights.
	 */
	private int _maxLights = 8;

	/**
	 * Construct new view.
	 *
	 * @param   model           Model for which this view is created.
	 * @param   background      Background color to use for 3D views. May be
	 *                          <code>null</code>, in which case the default
	 *                          background color of the current look and feel is
	 *                          used.
	 * @param   id              Application-assigned ID of this view.
	 * @param   textureCache    Texture cache.
	 */
	public JOGLView( final JOGLModel model , final Color background , final Object id , final Map<String,SoftReference<Texture>> textureCache )
	{
		super( model.getUnit() , id );
		final double unit = model.getUnit();

		_model = model;

		_frontClipDistance = 0.1 / unit;
		_backClipDistance  = 100.0 / unit;
		_renderThread      = null;

		_textureCache      = textureCache;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		final GLCanvas glCanvas;


		final GLCapabilities capabilities   = new GLCapabilities();
		capabilities.setSampleBuffers( true );
		/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
		capabilities.setNumSamples( 4 );

		/* See if the model already contains a context. */
		if ( model.getContext() != null )
		{
			glCanvas = new GLCanvas( capabilities , null , model.getContext() , null );
		}
		else
		{
			glCanvas = new GLCanvas( capabilities , null , null , null );
			model.setContext( glCanvas.getContext() );
		}

		glCanvas.addGLEventListener( new GLEventListener()
			{
			/**
			 * Define JOGL2dGraphics here to enable caching.
			 */
			private JOGLGraphics2D _j2d = null;

			/**
			 * GLWrapper handeling GL calls.
			 */
			private GLWrapper _glWrapper = null;

			public void init( final GLAutoDrawable glAutoDrawable )
				{
					final GL gl = glAutoDrawable.getGL();

					System.out.println();
					System.out.println( " About OpenGL:" );
					System.out.println( "---------------" );
					System.out.println( "Version:    " + gl.glGetString( GL.GL_VERSION                  ) );
					System.out.println( "Vendor:     " + gl.glGetString( GL.GL_VENDOR                   ) );
					System.out.println( "Extensions: " + gl.glGetString( GL.GL_EXTENSIONS               ) );
					System.out.println( "Renderer:   " + gl.glGetString( GL.GL_RENDERER                 ) );
					try
					{
						System.out.println( "Shaders:    " + gl.glGetString( GL.GL_SHADING_LANGUAGE_VERSION ) );
					}
					catch ( Exception e )
					{
						System.out.println( "Shaders:    n/a" );
					}
					System.out.println();

					glAutoDrawable.setGL( new DebugGL( gl ) );
					final GLWrapper glWrapper = new GLWrapper( gl );
					_glWrapper = glWrapper;
					initGL( glWrapper );
					final Overlay overlay = new Overlay( glAutoDrawable );
					_j2d = new JOGLGraphics2D( overlay.createGraphics() , glAutoDrawable );
					glCanvas.setMinimumSize( new Dimension( 0 , 0 ) ); //resize workaround
				}

				public void display( final GLAutoDrawable glAutoDrawable )
				{
					final GLWrapper glWrapper = _glWrapper;

				renderFrame( glWrapper , glAutoDrawable.getWidth() , glAutoDrawable.getHeight() );

					if ( hasOverlayPainters() )
					{
						paintOverlay( _j2d );
					}

					glWrapper.reset();
				}

				public void displayChanged( final GLAutoDrawable glAutoDrawable , final boolean b , final boolean b1 )
				{

				}

				public void reshape ( final GLAutoDrawable glAutoDrawable , final int x , final int y , final int width , final int height )
				{
					windowReshape( _glWrapper , x , y , width , height );

				}
			} );

		_viewComponent = glCanvas;

		if ( background != null )
			_viewComponent.setBackground( background );

		_controlInput  = new ViewControlInput( model , this );
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	public void update()
	{
		startRenderer();
	}

	public Projector getProjector()
	{
		final GLCanvas viewComponent     = _viewComponent;
		final int      imageWidth        = viewComponent.getWidth();
		final int      imageHeight       = viewComponent.getHeight();
		final double   imageResolution   = getResolution();

		final double   viewUnit          = getUnit();

		final double   fieldOfView       = getAperture();
		final double   zoomFactor        = getZoomFactor();
		final double   frontClipDistance = _frontClipDistance;
		final double   backClipDistance  = _backClipDistance;

		return Projector.createInstance( getProjectionPolicy() , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
	}

	protected ControlInput getControlInput()
	{
		return _controlInput;
	}

	/**
	 * Start render thread.
	 */
	private void startRenderer()
	{
		RenderThread renderThread = _renderThread;
		if ( renderThread == null || !renderThread.isAlive() )
		{
			if ( _viewComponent.isShowing() )
			{
				renderThread  = new RenderThread();
				_renderThread = renderThread;
				renderThread.start();
			}
		}
		else
		{
			renderThread.requestUpdate();
		}
	}

	/**
	 * Render loop for the view.
	 */
	private class RenderThread
		extends Thread
	{
		/**
		 * This thread control flag is set when <code>requestUpdate()</code> is
		 * called. It is used to trigger the thread loop to start rendering a new
		 * image. It is also tested at various loop points in the rendering code
		 * to abort rendering of a previous image, so the next rendering will be
		 * completed as soon as possible.
		 *
		 * @see     #requestUpdate()
		 */
		protected boolean _updateRequested;

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
		 * Construct render thread.
		 */
		private RenderThread()
		{
			super( "JOGLView.renderThread:" + getID() );
			setDaemon( true );
			setPriority( NORM_PRIORITY );
			_updateRequested = true;
			_terminationRequested = false;
		}

		public void run()
		{
			System.out.println( "Render thread started: " + Thread.currentThread() );

			final GLCanvas viewComponent = _viewComponent;
			while ( !_terminationRequested && viewComponent.isShowing() )
			{
				try
				{
					if ( _updateRequested )
					{
						_updateRequested = false;

						if ( viewComponent.isShowing() )
						{
							viewComponent.display();
						}
					}
				}
				catch ( Throwable t )
				{
					System.err.println( "Render exception: " + t );
					t.printStackTrace( System.err );
				}

				/*
				 * No update needed or an exception occured.
				 *
				 * Wait 300ms or wait to be notified.
				 */
				try
				{
					synchronized ( this )
					{
						wait( 300L );
					}
				}
				catch ( InterruptedException e ) { /*ignored*/ }
			}

			_terminationRequested = false;
			_updateRequested      = false;

			System.out.println( "Renderer thread died: " + Thread.currentThread() );
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
			_terminationRequested = true;

			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * Initialize GL context. Called once during initialization.
	 *
	 * @param   glWrapper  GLWrapper.
	 */
	private void initGL( final GLWrapper glWrapper )
	{
		/* Enable depth buffering. */
		final GL gl = glWrapper.getGL();
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthMask( true );
		glWrapper.glDepthFunc( GL.GL_LESS );

// @FIXME Disable explicit smoothing options for now. This causes extremely slow rendering on some machines. Should we set smoothing based on hardware capabilities?
//		/* Set smoothing. */
//		glWrapper.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
//		glWrapper.setBlend( true );
//		gl.glEnable( GL.GL_LINE_SMOOTH ); //enable smooth lines
//		gl.glHint( GL.GL_LINE_SMOOTH_HINT , GL.GL_NICEST );
//		gl.glEnable( GL.GL_POLYGON_SMOOTH ); //enable smooth polygons
//		gl.glHint( GL.GL_POLYGON_SMOOTH_HINT , GL.GL_NICEST );
//		gl.glShadeModel( GL.GL_SMOOTH );

		/* Initial clear. */
		glWrapper.glClearColor( _viewComponent.getBackground() );

		/* Find out the maximum number of lights possible. */

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect( 4 );
		byteBuffer.order( ByteOrder.LITTLE_ENDIAN );
		final IntBuffer intBuffer = byteBuffer.asIntBuffer();
		gl.glGetIntegerv( GL.GL_MAX_LIGHTS, intBuffer );
		intBuffer.position( 0 );
		_maxLights = intBuffer.get();
	}

	/**
	 * Called whenever the GL canvas is resized.
	 *
	 * @param   glWrapper   GLWrapper.
	 * @param   x           X offset.
	 * @param   y           Y offset.
	 * @param   width       Width of canvas.
	 * @param   height      Height of canvas.
	 */
	private void windowReshape( final GLWrapper glWrapper , final int x , final int y , final int width , final int height )
	{
		final Camera3D camera   = getCamera();
		final double   fov      = Math.toDegrees( camera.getAperture() );
		final double   aspect   = 1.0;
		final double   near     = _frontClipDistance;
		final double   far      = _backClipDistance;

		/* Setup size of window to draw in. */
		final GL gl = glWrapper.getGL();
		gl.glViewport( x , y , width , height );

		if ( getProjectionPolicy() != ProjectionPolicy.PARALLEL )
		{
			//View is not parallel so let's use perspective view. No support for isometric view yet.

			/* Setup the projection matrix. */
			gl.glMatrixMode( GL.GL_PROJECTION );
			gl.glLoadIdentity();

			/* Set the perspective view */
			final GLU glu = new GLU();
			glu.gluPerspective( fov , aspect , near , far );
			gl.glHint( GL.GL_PERSPECTIVE_CORRECTION_HINT , GL.GL_NICEST ); // nice perspective calculations
		}
	}

	/**
	 * Render entire scene (called from render loop).
	 *
	 * @param   glWrapper   GLWrapper.
	 * @param   width       Width of GLAutoDrawable
	 * @param   height      Height of GLAutoDrawable
	 */
	private void renderFrame( final GLWrapper glWrapper , final int width, final int height )
	{
		final GL gl = glWrapper.getGL();

		//check if the projector is parallel here, because the zoomfactor can be changed without resizing the window.
		if ( getProjectionPolicy() == ProjectionPolicy.PARALLEL )
		{
			final double   near     = _frontClipDistance;
			final double   far      = _backClipDistance;
			final Camera3D camera3D = getCamera();
			final double   scale    = camera3D.getZoomFactor() * getUnit() / getResolution();

			gl.glMatrixMode( GL.GL_PROJECTION );
			gl.glLoadIdentity();

			final double left   = (double)-width    / 2.0;
			final double right  = (double)width     / 2.0;
			final double bottom = (double)-height   / 2.0;
			final double top    = (double)height    / 2.0;

			gl.glOrtho( left , right , bottom , top , near , far );

			gl.glScaled( scale , scale ,  scale );
			gl.glEnable( GL.GL_NORMALIZE ); //normalize lighting normals after scaling
		}

		/* Clear depth and color buffer. */
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT );

		/* Setup view. */
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();

		/* Clear first. */
		glWrapper.glClearColor( _viewComponent.getBackground() );

		/*
		 * Setup the camera.
		 */
		final CameraControl cameraControl   = getCameraControl();
		final Matrix3D      cameraTransform = cameraControl.getTransform();

		final double aspect = (double)_viewComponent.getWidth() / (double)_viewComponent.getHeight();

		if ( getProjectionPolicy() == ProjectionPolicy.PERSPECTIVE )
		{
			gl.glScaled( 1.0 , aspect , 1.0 );
			gl.glEnable( GL.GL_NORMALIZE ); //normalize lighting normals after scaling
		}

		glWrapper.glMultMatrixd( cameraTransform );

		/*
		 * Render the view model nodes.
		 */
		final List<ViewModelNode> nodes = _model.getNodes();

		/* Initialize first light */
		int lightNumber = GL.GL_LIGHT0;

		/* Set Light Model to two sided lighting. */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE , GL.GL_TRUE );

		/* Set local view point */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_LOCAL_VIEWER , GL.GL_TRUE );

		//disable all lights
		//@FIXME is there a better way to do this?

		for( int i = 0 ; i < _maxLights ; i++ )
		{
			glWrapper.glDisable( GL.GL_LIGHT0 + i );
		}
		gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );

		//draw lights
		for ( final ViewModelNode viewModelNode : nodes )
		{
			final Node3D   node3D        = viewModelNode.getNode3D();
			final Matrix3D nodeTransform = viewModelNode.getTransform();

			/*
			 * Render lights.
			 */
			final Node3DCollection<Light3D> lights = node3D.collectNodes( null , Light3D.class , nodeTransform , false );

			if ( lights != null )
			{
				if ( lightNumber - GL.GL_LIGHT0 > _maxLights )
					throw new IllegalStateException( "No more than " + _maxLights + " lights supported." );

				final Light3D light         = lights.getNode( 0 );
				final float   viewIntensity = (float)light.getIntensity() / 255.0f;

				if ( light.isAmbient() )
				{
					gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT , new float[] { viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
				}
				else
				{
					gl.glLightfv( lightNumber , GL.GL_AMBIENT  , new float[] { 0.0f , 0.0f , 0.0f , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] { (float)nodeTransform.xo , (float)nodeTransform.yo , (float)nodeTransform.zo , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_DIFFUSE  , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_SPECULAR , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					glWrapper.glEnable( lightNumber );
					lightNumber++;
				}
			}
		}

		//draw objects
		for ( final ViewModelNode viewModelNode : nodes )
		{
			final Node3D   node3D        = viewModelNode.getNode3D();
			final Matrix3D nodeTransform = viewModelNode.getTransform();

			/*
			 * Render objects.
			 */
			final Node3DCollection<Object3D> objects = node3D.collectNodes( null , Object3D.class , nodeTransform , false );
			if ( objects != null )
			{
				final Map<String, SoftReference<Texture>> textureCache = _textureCache;
				switch ( getRenderingPolicy() )
				{
					case SCHEMATIC:
						glWrapper.glEnable( GL.GL_POLYGON_OFFSET_FILL );
						glWrapper.glDisable( GL.GL_LIGHTING );
						gl.glPolygonOffset( 1.0f , 1.0f );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache , true , viewModelNode.getMaterialOverride() );
						}

						glWrapper.glDisable( GL.GL_POLYGON_OFFSET_FILL );
						gl.glLineWidth( 1.0f );
						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache , false , viewModelNode.getMaterialOverride() );
						}
						break;
					case SKETCH:
						glWrapper.glEnable( GL.GL_POLYGON_OFFSET_FILL );
						gl.glPolygonOffset( 1.0f , 1.0f );
						gl.glLineWidth( 2.0f );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( true );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , true , textureCache , true  , viewModelNode.getMaterialOverride() );
						}

						glWrapper.glDisable( GL.GL_POLYGON_OFFSET_FILL );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( false );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , textureCache, false , viewModelNode.getMaterialOverride() );
						}

						gl.glLineWidth( 1.0f );
						break;
					case SOLID:
						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.glEnable( GL.GL_LIGHTING );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , true , false , true , _textureCache , true , viewModelNode.getMaterialOverride() );
						}
						break;
					case WIREFRAME:
						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.glDisable( GL.GL_LIGHTING );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache ,false , viewModelNode.getMaterialOverride() );
						}
						break;
				}
			}
		}

		if ( isGridEnabled() )
		{
			JOGLTools.drawGrid( glWrapper , getGrid2wcs(), getGridBounds() , getGridCellSize() , isGridHighlightAxes() , getGridHighlightInterval() );
		}
	}

}
