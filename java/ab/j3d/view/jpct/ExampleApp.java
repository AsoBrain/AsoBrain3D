package ab.j3d.view.jpct;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Node3DCollection;

import com.numdata.oss.ui.WindowTools;

public class ExampleApp
{
	/**
	 * Default size of the framebuffer
	 */
	private static final int WIDTH = 1024;
	private static final int HEIGHT = 768;

	private World _world;

	private TextureManager _texMan;

	private Object3D _cube;

	private FrameBuffer _buffer;

	private boolean _exit;



	private Component _test;


	public static void main( final String[] args )
	{
		new ExampleApp();
	}

	public ExampleApp()
	{
		_buffer = new FrameBuffer( WIDTH , HEIGHT, FrameBuffer.SAMPLINGMODE_NORMAL );
		_buffer.enableRenderer( IRenderer.RENDERER_SOFTWARE, IRenderer.MODE_OPENGL );

		_world = new World();
		_world.setAmbientLight( 200, 200, 200 );

		_texMan = TextureManager.getInstance();

		_exit = false;

		buildWorld();

		final Camera camera = _world.getCamera();
		camera.setPosition( new SimpleVector( 0.0f , 0.0f , -30.0f ) );


		_test = new JPanel();

		final JFrame frame = WindowTools.createFrame( "Example App jPCT", WIDTH, HEIGHT, _test );
		frame.setVisible( true );

		mainLoop();
	}

	private void buildWorld()
	{
		final ab.j3d.model.Object3D abCube = createCube( 3.0 );
		final Object3D jpctCube = new Object3D( 12 );

		final Node3DCollection<ab.j3d.model.Object3D> nodes = abCube.collectNodes( null , ab.j3d.model.Object3D.class , Matrix3D.INIT , false );

		for ( int i = 0 ; i < nodes.size() ; i++ )
		{
			final ab.j3d.model.Object3D object3d  = nodes.getNode( i );
			final int                   faceCount = object3d.getFaceCount();

			for ( int j = 0 ; j < faceCount ; j++ )
			{
				final Face3D      face    = object3d.getFace( j );
				final TextureInfo texture = new TextureInfo( JPCTTools.getTextureID( face.getMaterial() ) );

				final SimpleVector vert1 = new SimpleVector( face.getX( 0 ), face.getY( 0 ), face.getZ( 0 ) );
				final SimpleVector vert2 = new SimpleVector( face.getX( 1 ), face.getY( 1 ), face.getZ( 1 ) );
				final SimpleVector vert3 = new SimpleVector( face.getX( 2 ), face.getY( 2 ), face.getZ( 2 ) );
				final SimpleVector vert4 = new SimpleVector( face.getX( 3 ), face.getY( 3 ), face.getZ( 3 ) );

				jpctCube.addTriangle( vert3 , vert2 , vert1 , texture );
				jpctCube.addTriangle( vert4 , vert3 , vert1 , texture );
			}
		}

		_cube = jpctCube; // _cube = Primitives.getCube( 3 );
		_world.addObject( _cube );

		// Add extra light.
		_world.addLight( new SimpleVector( 0.0 , -50.0 , -10.0 ) , 50.0f , 200.0f , 150.0f );

		// Build all objects.
		_world.buildAllObjects();
	}

	// COPIED FROM VIEWMODELEXAMPLE!
	public static ab.j3d.model.Object3D createCube( final double size )
	{
		final Vector3D lfb = Vector3D.INIT.set( -size , -size , -size );
		final Vector3D rfb = Vector3D.INIT.set(  size , -size , -size );
		final Vector3D rbb = Vector3D.INIT.set(  size ,  size , -size );
		final Vector3D lbb = Vector3D.INIT.set( -size ,  size , -size );
		final Vector3D lft = Vector3D.INIT.set( -size , -size ,  size );
		final Vector3D rft = Vector3D.INIT.set(  size , -size ,  size );
		final Vector3D rbt = Vector3D.INIT.set(  size ,  size ,  size );
		final Vector3D lbt = Vector3D.INIT.set( -size ,  size ,  size );

		final Material red     = new Material( Color.RED    .getRGB() );
		final Material magenta = new Material( Color.MAGENTA.getRGB() );
		final Material blue    = new Material( Color.BLUE   .getRGB() );
		final Material cyan    = new Material( Color.CYAN   .getRGB() );
		final Material green   = new Material( Color.GREEN  .getRGB() );
		final Material yellow  = new Material( Color.YELLOW .getRGB() );

		final ab.j3d.model.Object3D cube = new ab.j3d.model.Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , red     , false , false ); // Z =  size
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , green   , false , false ); // Z = -size
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , cyan    , false , false ); // Y = -size
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , magenta , false , false ); // Y =  size
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , yellow  , false , false ); // X = -size
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , blue    , false , false ); // X =  size

		return cube;
	}

	private void mainLoop()
	{
		final Thread currentThread = Thread.currentThread();
		currentThread.setPriority( Thread.NORM_PRIORITY );

		while ( !_exit )
		{
			_buffer.clear();

			_cube.rotateX( 0.01f );
			_cube.rotateY( -0.01f );

			_world.renderScene( _buffer );
			_world.draw( _buffer );

			_buffer.update();
			_buffer.display( _test.getGraphics() );

			Thread.yield();
		}
	}
}
