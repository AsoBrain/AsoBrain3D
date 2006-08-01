package ab.j3d.view.jpct;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Graphics;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.Camera;

public class ExampleApplet
	extends Applet
	implements Runnable
{
	private Thread _loopThread = null;

	private World _world = null;

	private Object3D _cube = null;

	private FrameBuffer _buffer = null;

	private boolean _exit = false;

	public ExampleApplet()
	{
	}

	public void init()
	{
		final AppletContext appletContext = getAppletContext();
		appletContext.showStatus( "Initializing..." );

		_buffer = new FrameBuffer( 480, 300, FrameBuffer.SAMPLINGMODE_NORMAL );
		_buffer.enableRenderer( IRenderer.RENDERER_SOFTWARE, IRenderer.MODE_OPENGL );

		_world = new World();
		_world.setAmbientLight( 200, 200, 200 );

		_cube = Primitives.getCube( 3 );
		_world.addObject( _cube );

		_world.buildAllObjects();

		final Camera camera = _world.getCamera();
		camera.setPosition( new SimpleVector( 0.0f , 0.0f , -30.0f ) );

		appletContext.showStatus( "Done !" );
	}

	public void destroy()
	{
		TextureManager.getInstance().flush();
		Object3D.resetNextID();
		_exit = true;
		super.destroy();
	}

	public void paint( final Graphics g )
	{
	}

	public void update( final Graphics g )
	{
	}

	public String getAppletInfo()
	{
		return ( "Example Applet " );
	}

	public void run()
	{
		if ( Thread.currentThread() == _loopThread )
		{
			mainLoop();
		}
	}

	public void start()
	{
		if ( _loopThread == null )
		{
			_loopThread = new Thread( this );
			_loopThread.start();
		}
	}

	private void mainLoop()
	{
		Thread.currentThread().setPriority( Thread.NORM_PRIORITY );
		while ( !_exit )
		{
			_buffer.clear();
			_cube.rotateX( 0.01f );
			_cube.rotateY( -0.01f );
			_world.renderScene( _buffer );
			_world.draw( _buffer );
			_buffer.update();
			_buffer.display( this.getGraphics() );
			Thread.yield();
		}
		_loopThread = null;
	}
}