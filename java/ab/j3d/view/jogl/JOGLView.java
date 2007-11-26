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
import java.awt.Component;
import java.awt.Dimension;
import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.Action;
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
import ab.j3d.view.SwitchRenderingPolicyAction;
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
	 * Projection policy of this view.
	 *
	 * @see     #setProjectionPolicy
	 */
	private int _projectionPolicy;

	/**
	 * Rendering policy of this view.
	 *
	 * @see     ViewModelView#setRenderingPolicy
	 */
	private RenderingPolicy _renderingPolicy;

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
	 * Grid values
	 */
	private int _gridX       = 0;

	/** Y offset of grid */
	private int _gridY       = 0;

	/** Z offset of grid */
	private int _gridZ       = 0;

	/** Dx size of grid */
	private int _gridDx      = 0;

	/** Dy size of grid */
	private int _gridDy      = 0;

	/** Gridspacing, on every value a line is drawn */
	private int _gridSpacing = 0;

	/**
	 * Texture cache
	 */
	private Map<String, SoftReference<Texture>> _textureCache;

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
	public JOGLView( final JOGLModel model , final Color background , final Object id , final Map<String, SoftReference<Texture>> textureCache )
	{
		super( model.getUnit() , id );
		_textureCache      = textureCache;
		final double unit  = model.getUnit();

		_projectionPolicy  = Projector.PERSPECTIVE;
		_renderingPolicy   = RenderingPolicy.SOLID;
		_frontClipDistance = 0.1 / unit;
		_backClipDistance  = 100.0 / unit;
		_renderThread      = null;

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		_model             = model;
		final GLCanvas glCanvas;


		final GLCapabilities capabilities   = new GLCapabilities();
		capabilities.setSampleBuffers( true );
		/* set multisampling to 4, most graphic cards support this, if they don't support multisampling it will silently fail */
		capabilities.setNumSamples( 4 );

		/* See if the model already contains a context. */
		if( _model.getContext() != null )
		{
			glCanvas = new GLCanvas( capabilities , null , _model.getContext() , null );
		}
		else
		{
			glCanvas = new GLCanvas( capabilities , null , null , null );
			_model.setContext( glCanvas.getContext() );
		}

		glCanvas.addGLEventListener( new GLEventListener()
			{
			/**
			 * Define JOGL2dGraphics here to enable caching.
			 */
			private JOGL2dGraphics _j2d = null;

			/**
			 * GLWrapper handeling GL calls.
			 */
			private GLWrapper _glWrapper = null;

			public void init( final GLAutoDrawable glAutoDrawable )
				{
					_glWrapper = new GLWrapper( glAutoDrawable.getGL() );
					initGL( _glWrapper );
					final Overlay overlay = new Overlay( glAutoDrawable );
					_j2d = new JOGL2dGraphics( overlay.createGraphics() , glAutoDrawable );
				}

				public void display( final GLAutoDrawable glAutoDrawable )
				{
					renderFrame( _glWrapper , glAutoDrawable.getWidth() , glAutoDrawable.getHeight() );

					if( hasOverlayPainters() )
					{
						paintOverlay( _j2d );
					}
					_glWrapper.reset();
				}

				public void displayChanged( final GLAutoDrawable glAutoDrawable , final boolean b , final boolean b1 )
				{

				}

				public void reshape ( final GLAutoDrawable glAutoDrawable , final int x , final int y , final int width , final int height )
				{
					windowReshape( _glWrapper , x , y , width , height );
					glCanvas.setMinimumSize( new Dimension( 10 , 10 ) ); //fix for making the joglview smaller
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

	public void setProjectionPolicy( final int policy )
	{
		_projectionPolicy = policy;
	}

	public void setRenderingPolicy( final RenderingPolicy policy )
	{
		_renderingPolicy = policy;
	}

	public Projector getProjector()
	{
		final GLCanvas viewComponent     = _viewComponent;
		final int      imageWidth        = viewComponent.getWidth();
		final int      imageHeight       = viewComponent.getHeight();
		final double   imageResolution   = getResolution();

		final double   viewUnit          = getUnit();

		final int      projectionPolicy  = _projectionPolicy;
		final double   fieldOfView       = getAperture();
		final double   zoomFactor        = getZoomFactor();
		final double   frontClipDistance = _frontClipDistance;
		final double   backClipDistance  = _backClipDistance;

		return Projector.createInstance( projectionPolicy , imageWidth , imageHeight , imageResolution , viewUnit , frontClipDistance , backClipDistance , fieldOfView , zoomFactor );
	}

	protected ControlInput getControlInput()
	{
		return _controlInput;
	}

	public Action[] getActions( final Locale locale )
	{
		return new Action[] { new SwitchRenderingPolicyAction( locale , this , _renderingPolicy ) };
	}

	/**
	 * Start render thread.
	 */
	private void startRenderer()
	{
		RenderThread renderThread = _renderThread;
		if( renderThread == null || !renderThread.isAlive() )
		{
			if ( _viewComponent.isShowing() )
			{
				renderThread = new RenderThread();
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

		private RenderThread()
		{
			super( "JOGLView.renderThread:" + getID() );
			setDaemon( true );
			setPriority( NORM_PRIORITY );
			_updateRequested = true;
			_terminationRequested = false;
		}

		@Override
		public void run()
		{
			final Thread thread1 = Thread.currentThread();
			System.out.println( "Render thread started: " + thread1.getName() );
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

			final Thread thread = Thread.currentThread();
			System.out.println( "Renderer thread died: " + thread.getName() );
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
		final GL gl = glWrapper.getgl();
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthMask( true );
		glWrapper.glDepthFunc( GL.GL_LESS );

		/* Set smoothing. */
		glWrapper.setBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
		glWrapper.setBlend( true );
		gl.glEnable( GL.GL_LINE_SMOOTH ); //enable smooth lines
		gl.glHint( GL.GL_LINE_SMOOTH_HINT , GL.GL_NICEST );
		gl.glEnable( GL.GL_POLYGON_SMOOTH ); //enable smooth polygons
		gl.glHint( GL.GL_POLYGON_SMOOTH_HINT , GL.GL_NICEST );

		/* Initial clear. */
		JOGLTools.glClearColor( glWrapper , _viewComponent.getBackground() );
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
		final GL gl = glWrapper.getgl();
		gl.glViewport( x , y , width , height );

		if ( _projectionPolicy != Projector.PARALLEL )
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
	 * Draws a grid on the view using the specified parameters.
	 *
	 * The grid will be centered around the x,y position.
	 *
	 * @param x         X position of the grid.
	 * @param y         Y position of the grid.
	 * @param z         Z position of the grid.
	 * @param dx        Width of the grid.
	 * @param dy        Height of the grid.
	 * @param spacing   Spacing between the grid lines.
	 */
	public void drawGrid( final int x , final int y , final int z , final int dx , final int dy , final int spacing )
	{
		_gridX       = x;
		_gridY       = y;
		_gridZ       = z;
		_gridDx      = dx;
		_gridDy      = dy;
		_gridSpacing = spacing;
	}

	/**
	 * Removes the grid that is being drawn.
	 *
	 */
	public void removeGrid()
	{
		_gridX       = 0;
		_gridY       = 0;
		_gridZ       = 0;
		_gridDx      = 0;
		_gridDy      = 0;
		_gridSpacing = 0;
	}

	/**
	 * If set to true creates standard size grid centered around 0,0.
	 *
	 * Not all 3d views implement this method yet.
	 *
	 * @param isTrue    If set to true it will draw a grid.
	 */
	public final void setGrid( final boolean isTrue )
	{
		if( isTrue )
			drawGrid( 0 , 0 , -35 , 50000 , 50000 , 500 );
		else
		{
			_gridX       = 0;
			_gridY       = 0;
			_gridZ       = 0;
			_gridDx      = 0;
			_gridDy      = 0;
			_gridSpacing = 0;
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
		final GL gl = glWrapper.getgl();

		//check if the projector is parallel here, because the zoomfactor can be changed without resizing the window.
		if ( _projectionPolicy == Projector.PARALLEL )
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
		JOGLTools.glClearColor( glWrapper , _viewComponent.getBackground() );

		/*
		 * Setup the camera.
		 */
		final CameraControl cameraControl   = getCameraControl();
		final Matrix3D      cameraTransform = cameraControl.getTransform();

		final double aspect = (double)_viewComponent.getWidth() / (double)_viewComponent.getHeight();

		if( _projectionPolicy == Projector.PERSPECTIVE )
		{
			gl.glScaled( 1.0 , aspect , 1.0 );
			gl.glEnable( GL.GL_NORMALIZE ); //normalize lighting normals after scaling
		}

		JOGLTools.glMultMatrixd( glWrapper , cameraTransform );

		/*
		 * Render the view model nodes.
		 */
		final Object[] nodeIDs = _model.getNodeIDs();

		/* Initialize first light */
		int lightNumber = GL.GL_LIGHT0;

		/* Set Light Model to two sided lighting. */
		gl.glLightModeli( GL.GL_LIGHT_MODEL_TWO_SIDE , GL.GL_TRUE );

		for ( final Object id : nodeIDs )
		{
			final ViewModelNode viewModelNode = _model.getNode( id );
			final Node3D   node3D        = viewModelNode.getNode3D();
			final Matrix3D nodeTransform = viewModelNode.getTransform();

			/*
			 * Render lights.
			 */
			final Node3DCollection<Light3D> lights = node3D.collectNodes( null , Light3D.class , nodeTransform , false );

			if ( lights != null )
			{
				if ( lightNumber - GL.GL_LIGHT0 > GL.GL_MAX_LIGHTS )
					throw new IllegalStateException( "No more than " + GL.GL_MAX_LIGHTS + " lights supported." );

				final Light3D light         = lights.getNode( 0 );
				final float   viewIntensity = (float)light.getIntensity() / 255.0f;

				if ( light.isAmbient() )
				{
					gl.glLightModelfv( GL.GL_LIGHT_MODEL_AMBIENT , new float[] { viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_AMBIENT , new float[] { viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
				}
				else
				{
					gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] { (float)nodeTransform.xo , (float)nodeTransform.yo , (float)nodeTransform.zo , 0.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_DIFFUSE  , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_SPECULAR , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
				}

				gl.glEnable( lightNumber );
				lightNumber++;
			}

			/*
			 * Render objects.
			 */
			final Node3DCollection<Object3D> objects = node3D.collectNodes( null , Object3D.class , nodeTransform , false );
			if ( objects != null )
			{
				switch ( _renderingPolicy )
				{
					case SCHEMATIC:
						gl.glEnable( GL.GL_POLYGON_OFFSET_FILL );
						gl.glPolygonOffset( 1.0f , 1.0f );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( false );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache , true , viewModelNode.getMaterialOverride() );
						}

						gl.glDisable( GL.GL_POLYGON_OFFSET_FILL );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( false );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache , false , viewModelNode.getMaterialOverride() );
						}
						break;
					case SKETCH:
						gl.glEnable( GL.GL_POLYGON_OFFSET_FILL );
						gl.glPolygonOffset( 1.0f , 1.0f );
						gl.glLineWidth( 2.0f );

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( true );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , true , _textureCache , true  , viewModelNode.getMaterialOverride() );
						}

						gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);

						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( false );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache , false , viewModelNode.getMaterialOverride() );
						}

						gl.glLineWidth( 1.0f );
						break;
					case SOLID:
						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( true );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , true , viewModelNode.isAlternate() , true , _textureCache , true , viewModelNode.getMaterialOverride() );
						}
						break;
					case WIREFRAME:
						for ( int i = 0 ; i < objects.size() ; i++ )
						{
							glWrapper.setLighting( false );
							JOGLTools.paintObject3D( glWrapper , objects.getNode( i ) , objects.getMatrix( i ) , false , viewModelNode.isAlternate() , false , _textureCache ,false , viewModelNode.getMaterialOverride() );
						}
						break;
				}
			}
		}
		JOGLTools.drawGrid( glWrapper , _gridX , _gridY , _gridZ , _gridDx , _gridDy , _gridSpacing );
	}
}