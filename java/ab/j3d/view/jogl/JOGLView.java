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
import java.awt.Graphics2D;
import java.util.Locale;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.Action;
import javax.swing.JPopupMenu;

import com.sun.opengl.util.j2d.Overlay;

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
	 * @see     #setRenderingPolicy
	 */
	private int _renderingPolicy;

	/**
	 * Component through which a rendering of the view is shown.
	 */
	private final GLCanvas _viewComponent;

	/**
	 * Scene input translator for this View.
	 */
	private final ControlInput _controlInput;

	/**
	 * Whether the view needs to be updated to model changes.
	 */
	private boolean _updateNeeded;

	/**
	 * Front clipping plane distance in model units.
	 */
	private final double _frontClipDistance;

	/**
	 * Back clipping plane distance in model units.
	 */
	private final double _backClipDistance;

	/**
	 * Define global overlay for use in the overlay painters.
	 */
	private Overlay _overlay = null;

	/**
	 * Construct new view.
	 *
	 * @param   model       Model for which this view is created.
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 * @param   id          Application-assigned ID of this view.
	 */
	public JOGLView( final JOGLModel model , final Color background , final Object id )
	{
		super( model.getUnit() , id );

		final double unit = model.getUnit();

		_projectionPolicy  = Projector.PERSPECTIVE;
		_renderingPolicy   = SOLID;
		_frontClipDistance = 0.1 / unit;
		_backClipDistance  = 100.0 / unit;

		final GLCanvas glCanvas = new GLCanvas( new GLCapabilities() );
		glCanvas.addGLEventListener( new GLEventListener()
			{
				public void init( final GLAutoDrawable glAutoDrawable )
				{
					initGL( glAutoDrawable.getGL() );
				}

				public void display( final GLAutoDrawable glAutoDrawable )
				{
					renderFrame( glAutoDrawable.getGL() );
					if(hasOverlayPainters())
					{
						final Graphics2D g2d = _overlay.createGraphics();
						final JOGL2dGraphics j2d = new JOGL2dGraphics( g2d , glAutoDrawable, true ); //draw real font
						paintOverlay( j2d );
						g2d.dispose();
					}
				}

				public void displayChanged( final GLAutoDrawable glAutoDrawable , final boolean b, final boolean b1 )
				{

				}

				public void reshape ( final GLAutoDrawable glAutoDrawable , final int x , final int y , final int width , final int height )
				{
					windowReshape( glAutoDrawable.getGL() , x , y , width , height );
				}
			} );

		/* Use heavyweight popups, since we use a heavyweight canvas */
		JPopupMenu.setDefaultLightWeightPopupEnabled( false );

		_model         = model;
		_viewComponent = glCanvas;
		_controlInput  = new ViewControlInput( model , this );
		_updateNeeded  = true;

		if ( background != null )
			_viewComponent.setBackground( background );
		else
			_viewComponent.setBackground( Color.BLACK);

		startRenderer();
	}

	public Component getComponent()
	{
		return _viewComponent;
	}

	public void update()
	{
		/*
		 * Renderer will perform an update before the next frame is rendered.
		 */
		synchronized ( this )
		{
			_updateNeeded = true;
			notifyAll();
		}
	}

	public void setProjectionPolicy( final int policy )
	{
		_projectionPolicy = policy;
	}

	public void setRenderingPolicy( final int policy )
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
		final Thread renderThread = new Thread( new Renderer() , "JOGLView.renderThread:" + getID() );
		renderThread.setDaemon( true );
		renderThread.setPriority( Thread.NORM_PRIORITY );
		renderThread.start();
	}

	/**
	 * Render loop for the view.
	 */
	private class Renderer
		implements Runnable
	{
		public void run()
		{
			while ( true )
			{
				/*
				 * Pause the renderer unless there are changes.
				 */
				try
				{
					synchronized ( JOGLView.this )
					{
						while ( !_updateNeeded )
						{
							JOGLView.this.wait();
						}
						_updateNeeded = false;
					}
				}
				catch ( InterruptedException e )
				{
					break;
				}

				if ( _viewComponent.isShowing() )
					_viewComponent.display();

			}
		}
	}

	/**
	 * Initialize GL context. Called once during initialization.
	 *
	 * @param   gl  GL context.
	 */
	private void initGL( final GL gl )
	{
		/* Initialize overlay */
		_overlay = new Overlay(_viewComponent);

		/* Enable depth buffering. */
		gl.glEnable( GL.GL_DEPTH_TEST );
		gl.glDepthFunc( GL.GL_LEQUAL );

		/* Set smoothing. */
		gl.glEnable( GL.GL_BLEND );
		gl.glBlendFunc( GL.GL_SRC_ALPHA , GL.GL_ONE_MINUS_SRC_ALPHA );
		gl.glEnable( GL.GL_LINE_SMOOTH );

		/* Initial clear. */
		JOGLTools.glClearColor( gl , _viewComponent.getBackground() );
	}

	/**
	 * Called whenever the GL canvas is resized.
	 *
	 * @param   gl      GL context.
	 * @param   x       X offset.
	 * @param   y       Y offset.
	 * @param   width   Width of canvas.
	 * @param   height  Height of canvas.
	 */
	private void windowReshape( final GL gl , final int x , final int y , final int width , final int height )
	{
		/* Setup size of window to draw in. */
		gl.glViewport( x , y , width , height );

		/* Setup the projection matrix. */
		gl.glMatrixMode( GL.GL_PROJECTION );
		gl.glLoadIdentity();

		final Camera3D camera = getCamera();

		final double fov    = Math.toDegrees( camera.getAperture() );
		final double aspect = 1.0;
		final double near   = _frontClipDistance;
		final double far    = _backClipDistance;

		final GLU glu = new GLU();
		glu.gluPerspective( fov , aspect , near , far );
	}

	/**
	 * Render entire scene (called from render loop).
	 *
	 * @param   gl  GL context.
	 */
	private void renderFrame( final GL gl )
	{
		final boolean fill;
		final boolean outline;
		final Color outlineColor;
//		final boolean useTextures;
//		final boolean backfaceCulling;
//		final boolean applyLighting;

		final int renderingPolicy = _renderingPolicy;
		switch ( renderingPolicy )
		{
				case SOLID     : fill = true;  outline = false; outlineColor = null;        /*useTextures = true;  backfaceCulling = true;  applyLighting = true; */ break;
				case SCHEMATIC : fill = true;  outline = true;  outlineColor = Color.BLACK; /*useTextures = false; backfaceCulling = true;  applyLighting = false;*/ break;
				case SKETCH    : fill = true;  outline = false; outlineColor = null;        /*useTextures = true;  backfaceCulling = true;  applyLighting = true; */ break;
				case WIREFRAME : fill = false; outline = true;  outlineColor = Color.RED;   /*useTextures = false; backfaceCulling = false; applyLighting = false;*/ break;
				default        : fill = false; outline = false; outlineColor = null;        /*useTextures = false; backfaceCulling = false; applyLighting = true; */ break;
		}

		/* Clear depth buffer. */
		gl.glClear( GL.GL_DEPTH_BUFFER_BIT );

		/* Clear color buffer. */
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );

		/* Setup view. */
		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();

		/* Clear first. */
		JOGLTools.glClearColor( gl , _viewComponent.getBackground() );

		/*
		 * Setup the camera.
		 */
		final CameraControl cameraControl   = getCameraControl();
		final Matrix3D      cameraTransform = cameraControl.getTransform();

		final double aspect = (double)_viewComponent.getWidth() / (double)_viewComponent.getHeight();
		gl.glScaled( 1.0 , aspect , 1.0 );

		JOGLTools.glMultMatrixd( gl , cameraTransform );

		/*
		 * Render the view model nodes.
		 */
		final Object[] nodeIDs = _model.getNodeIDs();

		/* Initialize first light */
		int lightNumber = GL.GL_LIGHT0;

		for ( final Object id : nodeIDs )
		{
			final ViewModelNode viewModelNode = _model.getNode( id );

			final Node3D   node3D        = viewModelNode.getNode3D();
			final Matrix3D nodeTransform = viewModelNode.getTransform();
			/*
			 * Render lights.
			 */
			final Node3DCollection<Light3D> lights = node3D.collectNodes( null , Light3D.class , nodeTransform, false );

			if ( lights != null )
			{
				if ( lightNumber - GL.GL_LIGHT0 > GL.GL_MAX_LIGHTS )
					throw new IllegalStateException( "No more than " + GL.GL_MAX_LIGHTS + " lights supported." );

				final Light3D light         = lights.getNode( 0 );
				final float   viewIntensity = (float)light.getIntensity() / 255.0f;

				if ( light.isAmbient() )
				{
					gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, new float[] { viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_AMBIENT , new float[]  { viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
				}
				else
				{
					gl.glLightfv( lightNumber , GL.GL_POSITION , new float[] {-(float)nodeTransform.xo , (float)nodeTransform.yo , -(float)nodeTransform.zo , 0.0f} , 0 );

					gl.glLightfv( lightNumber , GL.GL_DIFFUSE  , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_SPECULAR , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
					gl.glLightfv( lightNumber , GL.GL_EMISSION , new float[] {  viewIntensity , viewIntensity , viewIntensity , 1.0f } , 0 );
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
				for ( int i = 0 ; i < objects.size() ; i++ )
				{
					JOGLTools.paintObject3D( gl , objects.getNode( i ), objects.getMatrix( i ) , fill, null , outline , outlineColor, false );
				}
			}
		}


	}
}