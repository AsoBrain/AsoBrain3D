/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package pov;

import java.util.Locale;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
import ab.j3d.view.ViewModel;
import ab.j3d.view.FromToViewControl;
import ab.j3d.view.java3d.Java3dModel;
import ab.j3d.Vector3D;
import ab.j3d.TextureSpec;
import ab.j3d.Matrix3D;
import ab.j3d.model.Box3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.ExtrudedObject2D;
import com.numdata.oss.ui.WindowTools;
import com.numdata.oss.ui.ImageTools;

/**
 * This class constructs a testmodel (Java3D) for testing the ABToPovConverter.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class ABTestModel
{
	/**
	 * Construct an object of type ABTestModel.
	 */
	public ABTestModel() {
	}

	/**
	 * Constructs the test-model and a simple view.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final ABTestModel ABModel = new ABTestModel();
		final ViewModel viewModel = new Java3dModel();

		ImageTools.addToSearchPath( "/home/rob/" );     // /home/rob/textures

		// Transforms for colorCube and light
		Matrix3D colorCubeTransform   = Matrix3D.INIT.rotateY( Math.toRadians( 45 ) );
		colorCubeTransform            = colorCubeTransform.rotateZ( Math.toRadians( 45 ) );
		colorCubeTransform            = colorCubeTransform.setTranslation( 0.0 , 300.0 , 0.0 );
		final Matrix3D lightTransform = Matrix3D.INIT.setTranslation( -2000.0 , 2000.0 , -2000.0 );

		viewModel.createNode( "redbox"      , null               , ABModel.getRedXRotatedBox3D()   , null , 1.0f );
		viewModel.createNode( "greenbox"    , null               , ABModel.getGreenYRotatedBox3D() , null , 1.0f );
		viewModel.createNode( "bluebox"     , null               , ABModel.getBlueZRotatedBox3D()  , null , 1.0f );
		viewModel.createNode( "texturedbox" , null               , ABModel.getTexturedBox3D()      , null , 1.0f );
		viewModel.createNode( "sphere"      , null               , ABModel.getSphere3D()           , null , 1.0f );
		viewModel.createNode( "cylinder"    , null               , ABModel.getCylinder3D()         , null , 1.0f );
		viewModel.createNode( "cone"        , null               , ABModel.getCone3D()             , null , 1.0f );
		viewModel.createNode( "extruded"    , null               , ABModel.getExtrudedObject2D()   , null , 1.0f );
		viewModel.createNode( "colorcube"   , colorCubeTransform , ABModel.getColorCube( 100 )     , null , 1.0f );
		viewModel.createNode( "light"       , lightTransform     , ABModel.getLight3D()            , null , 1.0f );

		// Create view for the test model
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;
		viewModel.createView( "view" , new FromToViewControl( viewFrom , viewAt ) );
		final JFrame frame = WindowTools.createFrame( "Testscene" , 800 , 600 , viewModel.createViewPanel( Locale.ENGLISH , "view" ) );
		frame.setVisible( true );
	}

	/**
	 * This method constructs a red box of size 100 mm, rotated 45 degrees around the x-axis.
	 *
	 * @return The constructed object.
	 */
	public Box3D getRedXRotatedBox3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.RED );
		final Matrix3D    rotate    = Matrix3D.INIT.rotateX( Math.toRadians( 45 ) );
		final Matrix3D    translate = Matrix3D.INIT.setTranslation( -200 , 0.0 , -200 );
		final Matrix3D    transform = rotate.multiply( translate );

		return new Box3D( transform , 100 , 100 , 100 , texture  , texture );
	}

	/**
	 * This method constructs a green box of size 100 mm, rotated 45 degrees around the y-axis.
	 *
	 * @return The constructed object.
	 */
	public Box3D getGreenYRotatedBox3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.GREEN );
		final Matrix3D    rotate    = Matrix3D.INIT.rotateY( Math.toRadians( 45 ) );
		final Matrix3D    translate = Matrix3D.INIT.setTranslation( -50 , 0.0 , -200 );
		final Matrix3D    transform = rotate.multiply( translate );

		return new Box3D( transform , 100 , 100 , 100 , texture  , texture );
	}

	/**
	 * This method constructs a blue box of size 100 mm, rotated 45 degrees around the z-axis.
	 *
	 * @return The constructed object.
	 */
	public Box3D getBlueZRotatedBox3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.BLUE );
		final Matrix3D    rotate    = Matrix3D.INIT.rotateZ( Math.toRadians( 45 ) );
		final Matrix3D    translate = Matrix3D.INIT.setTranslation( 200 , 0.0 , -200 );
		final Matrix3D    transform = rotate.multiply( translate );

		return new Box3D( transform , 100 , 100 , 100 , texture  , texture );
	}

	/**
	 * This method constructs a textured box (wooden panel) with 2 different textures.
	 *
	 * @return The constructed object.
	 */
	public Box3D getTexturedBox3D()
	{
		final TextureSpec mainTexture = new TextureSpec( "MPXs" , -1 , 1.0f , 1.0f , 0.3f , 0.5f , 0.7f , 8 , false );
		final TextureSpec sideTexture = new TextureSpec( "MFCs" , -1 , 1.0f , 2.0f , 0.3f , 0.5f , 0.7f , 8 , false );
		      Matrix3D    transform   = Matrix3D.INIT.rotateZ( Math.toRadians( 315 ) );
		transform = transform.setTranslation( -300 , 0.0 , 0.0 );

		return new Box3D( transform , 200 , 10 , 200 , mainTexture , sideTexture );
	}

	/**
	 * This method constructs a blue sphere with a radius of 100 mm and 20 faces per axis.
	 *
	 * @return The constructed object.
	 */
	public Sphere3D getSphere3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.BLUE );
		final Matrix3D    transform = Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 0.0 );

		return new Sphere3D( transform , 100 , 100 , 100 , 20 , 20 , texture , false );
	}

	/**
	 * This method constructs a magenta colored cylinder with a topradius of 50 mm, a bottomradius of 50 mm, a height
	 * of 100 mm and 100 faces to approximate a circle.
	 *
	 * @return The constructed object.
	 */
	public Cylinder3D getCylinder3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.MAGENTA );
		final Matrix3D    transform = Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 100 );

		return new Cylinder3D( transform , 50 , 50 , 100 , 100 , texture , true , true );
	}

	/**
	 * This method constructs a white cylinder with a topradius of 50 mm, a bottomradius of 100 mm, a height
	 * of 200 mm and 100 faces to approximate a circle (a cone).
	 *
	 * @return The constructed object.
	 */
	public Cylinder3D getCone3D()
	{
		final TextureSpec texture   = new TextureSpec( Color.WHITE );
		final Matrix3D    rotate    = Matrix3D.INIT.rotateX( Math.toRadians( 45 ) );
		final Matrix3D    transform = rotate.setTranslation( 200 , 0.0 , 0.0 );

		return new Cylinder3D( transform , 100 , 50 , 200 , 100 , texture , true , true );
	}

	/**
	 * This method constructs a colored cube out of seperate faces.
	 *
	 * @param size The size of the cube in mm (a size of 100 means the width and height of the cube will be 200).
	 * @return The constructed object.
	 */
	public Object3D getColorCube( final int size )
	{
		final Vector3D lfb = Vector3D.INIT.set( -size , -size , -size );
		final Vector3D rfb = Vector3D.INIT.set(  size , -size , -size );
		final Vector3D rbb = Vector3D.INIT.set(  size ,  size , -size );
		final Vector3D lbb = Vector3D.INIT.set( -size ,  size , -size );
		final Vector3D lft = Vector3D.INIT.set( -size , -size ,  size );
		final Vector3D rft = Vector3D.INIT.set(  size , -size ,  size );
		final Vector3D rbt = Vector3D.INIT.set(  size ,  size ,  size );
		final Vector3D lbt = Vector3D.INIT.set( -size ,  size ,  size );

		final TextureSpec red     = new TextureSpec( Color.red     );
		final TextureSpec magenta = new TextureSpec( Color.magenta );
		final TextureSpec blue    = new TextureSpec( Color.blue    );
		final TextureSpec cyan    = new TextureSpec( Color.cyan    );
		final TextureSpec green   = new TextureSpec( Color.green   );
		final TextureSpec yellow  = new TextureSpec( Color.yellow  );

		final Object3D cube = new Object3D();
		cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , red     , false , false ); // Z =  size (top)
		cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , green   , false , false ); // Z = -size (bottom)
		cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , cyan    , false , false ); // Y = -size (front)
		cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , magenta , false , false ); // Y =  size (back)
		cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , yellow  , false , false ); // X = -size (left)
		cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , blue    , false , false ); // X =  size (right)

		return cube;
	}

	/**
	 * This method constructs an extruded object. The object is rectangular with a width and height of 100 mm and
	 * all extruded vertices are placed 100 back (y) and 100 up (z).
	 *
	 * @return The constructed object.
	 */
	public ExtrudedObject2D getExtrudedObject2D()
	{
		final TextureSpec texture   = new TextureSpec( Color.PINK );
		final Shape       shape     = new Rectangle2D.Double( 0.0 , 0.0 , 100 , 100 );
		final Vector3D    extrusion = Vector3D.INIT.set( 0.0 , 100 , 100 );
		final Matrix3D    transform = Matrix3D.INIT.setTranslation( -400 , 0.0 , -200 );

		return new ExtrudedObject2D( shape , extrusion , transform , texture , 1.0 , true );
	}

	/**
	 * This method constructs a light object with an intensity of 1 and falloff of 1.0.
	 *
	 * @return The constructed object.
	 */
	public Light3D getLight3D()
	{
		return new Light3D( 1 , 1.0 );
	}
}
