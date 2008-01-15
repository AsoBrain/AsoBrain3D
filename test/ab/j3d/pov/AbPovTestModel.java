/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2007
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
package ab.j3d.pov;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.model.Box3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.java2d.Java2dModel;

/**
 * This class constructs a testmodel ({@link Java2dModel}) for testing the
 * {@link AbToPovConverter}. The model is used by both the test and test
 * application.
 *
 * @see     TestAbToPovConverter
 * @see     AbPovTestApp
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public final class AbPovTestModel
{
	/**
	 * The used model.
	 */
	private final Java2dModel _viewModel = new Java2dModel();

	/**
	 * Construct new {@link AbPovTestModel}.
	 */
	public AbPovTestModel() {

		/*
		 * Create model.
		 */
		final Java2dModel viewModel = _viewModel;

		/*
		 * Fill model with objects from the testmodel.
		 */
		viewModel.createNode( "camera"              , null , getCamera3D()              , null , 1.0f );
		viewModel.createNode( "redbox"              , null , getRedXRotatedBox3D()      , null , 1.0f );
		viewModel.createNode( "greenbox"            , null , getGreenYRotatedBox3D()    , null , 1.0f );
		viewModel.createNode( "bluebox"             , null , getBlueZRotatedBox3D()     , null , 1.0f );
		viewModel.createNode( "panel"               , null , getTexturedBox3D()         , null , 1.0f );
		viewModel.createNode( "sphere"              , null , getSphere3D()              , null , 1.0f );
		viewModel.createNode( "cylinder"            , null , getCylinder3D()            , null , 1.0f );
		viewModel.createNode( "cone"                , null , getCone3D()                , null , 1.0f );
		viewModel.createNode( "extruded"            , null , getExtrudedObject2D()      , null , 1.0f );
		viewModel.createNode( "colorcube"           , null , getColorCube()             , null , 1.0f );
		viewModel.createNode( "texturedcolorcube"   , null , getTexturedColorCube()     , null , 1.0f );

		/*
		 * Create view.
		 */
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		final ViewModelView view = viewModel.createView( "view" );
		view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );
	}

	/**
	 * Get a reference to the used model.
	 *
	 * @return The test model.
	 */
	public ViewModel getModel()
	{
		return _viewModel;
	}

	/**
	 * Construct a camera with a zoomfactor of 1.0 and a field of view of 45
	 * degrees.
	 *
	 * @see Camera3D
	 *
	 * @return The constructed {@link Camera3D} object.
	 */
	public Camera3D getCamera3D()
	{
		final Node3D node = _viewModel.getNode3D( "camera" );
		final Camera3D camera;

		if ( node == null )
		{
			camera =  new Camera3D( 1.0 , 45.0 );
		}
		else
		{
			camera = (Camera3D)node;
		}

		return camera;
	}

	/**
	 * This method constructs a red box of size 100 mm, rotated 10 degrees
	 * around the x-axis.
	 *
	 * @see Box3D
	 *
	 * @return The constructed {@link Box3D}.
	 */
	public Box3D getRedXRotatedBox3D()
	{
		final Node3D node = _viewModel.getNode3D( "redbox" );
		final Box3D box;

		if ( node == null )
		{
			final Material material  = new Material( Color.RED.getRGB() );
			final Matrix3D rotate    = Matrix3D.INIT.rotateX( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( -200.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			box = new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material );
		}
		else
		{
			box = (Box3D)node;
		}

		return box;
	}

	/**
	 * This method constructs a green box of size 100 mm, rotated 10 degrees
	 * around the y-axis.
	 *
	 * @see Box3D
	 *
	 * @return The constructed {@link Box3D}.
	 */
	public Box3D getGreenYRotatedBox3D()
	{
		final Node3D node = _viewModel.getNode3D( "greenbox" );
		final Box3D box;

		if ( node == null )
		{
			final Material material  = new Material( Color.GREEN.getRGB() );
			final Matrix3D rotate    = Matrix3D.INIT.rotateY( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( -50.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			material.diffuseColorAlpha = 0.2f;

			box = new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material );
		}
		else
		{
			box = (Box3D)node;
		}

		return box;
	}

	/**
	 * This method constructs a blue box of size 100 mm, rotated 10 degrees
	 * around the z-axis.
	 *
	 * @see Box3D
	 *
	 * @return The constructed {@link Box3D}.
	 */
	public Box3D getBlueZRotatedBox3D()
	{
		final Node3D node = _viewModel.getNode3D( "bluebox" );
		final Box3D box;

		if ( node == null )
		{
			final Material material  = new Material( Color.BLUE.getRGB() );
			final Matrix3D rotate    = Matrix3D.INIT.rotateZ( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( 200.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			box =  new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material );
		}
		else
		{
			box = (Box3D)node;
		}

		return box;
	}

	/**
	 * This method constructs a textured box (wooden panel) with 2 different
	 * textures and a width of 200 mm, a height of 200 mm and a depth of 10 mm.
	 * The panel is rotated 45 degrees around the z-axix.
	 *
	 * @see Box3D
	 *
	 * @return The constructed {@link Box3D}.
	 */
	public Box3D getTexturedBox3D()
	{
		final Node3D node = _viewModel.getNode3D( "texturedbox" );
		final Box3D box;

		if ( node == null )
		{
			Matrix3D transform = Matrix3D.INIT.rotateZ( Math.toRadians( 45.0 ) );
			transform = transform.setTranslation( -350.0 , 0.0 , 0.0 );

			final Material mainMaterial = new Material();
			mainMaterial.code           = "MPXs";
			mainMaterial.colorMap       = "MPXs";
			mainMaterial.colorMapWidth  = 0.2;
			mainMaterial.colorMapHeight = 0.2;

			final Material sideMaterial = new Material();
			sideMaterial.code           = "MFCs";
			sideMaterial.colorMap       = "MFCs";
			sideMaterial.colorMapWidth  = 0.2;
			sideMaterial.colorMapHeight = 0.2;

			box =  new Box3D( transform , 200.0 , 10.0 , 200.0 , 0.001 , mainMaterial , sideMaterial );
		}
		else
		{
			box = (Box3D)node;
		}

		return box;
	}

	/**
	 * This method constructs a blue sphere with a radius of 100 mm and 20
	 * faces per axis.
	 *
	 * @see Sphere3D
	 *
	 * @return The constructed {@link Sphere3D}.
	 */
	public Sphere3D getSphere3D()
	{
		final Node3D node = _viewModel.getNode3D( "sphere" );
		final Sphere3D sphere;

		if ( node == null )
		{
			final Material material  = new Material( Color.BLUE.getRGB() );
			final Matrix3D transform = Matrix3D.INIT.setTranslation( 0.0 , 300.0 , -200.0 );

			sphere = new Sphere3D( transform , 100.0 , 100.0 , 100.0 , 20 , 20 , material , false );
		}
		else
		{
			sphere = (Sphere3D)node;
		}

		return sphere;
	}

	/**
	 * This method constructs a magenta colored cylinder with a topradius of
	 * 50 mm, a bottomradius of 50 mm, a height of 100 mm and 100 faces to
	 * approximate a circle.
	 *
	 * @see Cylinder3D
	 *
	 * @return The constructed {@link Cylinder3D}.
	 */
	public Cylinder3D getCylinder3D()
	{
		final Node3D node = _viewModel.getNode3D( "cylinder" );
		final Cylinder3D cylinder;

		if ( node == null )
		{
			final Material material  = new Material( Color.MAGENTA.getRGB() );
			final Matrix3D transform = Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 150.0 );

			cylinder = new Cylinder3D( transform , 50.0 , 50.0 , 100.0 , 100 , material , true , true );
		}
		else
		{
			cylinder = (Cylinder3D)node;
		}

		return cylinder;
	}

	/**
	 * This method constructs a white cone with a topradius of 50 mm, a
	 * bottomradius of 100 mm, a height of 200 mm and 100 faces to approximate
	 * a circle. Note that the cone is actually a cylinder.
	 *
	 * @see Cylinder3D
	 *
	 * @return The constructed {@link Cylinder3D}.
	 */
	public Cylinder3D getCone3D()
	{
		final Node3D node = _viewModel.getNode3D( "cone" );
		final Cylinder3D cone;

		if ( node == null )
		{
			final Material material  = new Material( Color.WHITE.getRGB() );
			final Matrix3D rotate    = Matrix3D.INIT.rotateX( Math.toRadians( 45.0 ) );
			final Matrix3D transform = rotate.setTranslation( 250.0 , 0.0 , 0.0 );

			cone = new Cylinder3D( transform , 100.0 , 50.0 , 200.0 , 100 , material , true , true );
		}
		else
		{
			cone = (Cylinder3D)node;
		}

		return cone;
	}

	/**
	 * This method constructs a cube with a different color per face.
	 * The width, height and depth of the cube are all 200 mm (-100 to 100).
	 *
	 * @see Object3D
	 *
	 * @return The constructed {@link Object3D}.
	 */
	public Object3D getColorCube()
	{
		final Node3D node = _viewModel.getNode3D( "colorcube" );
		final Object3D cube;

		if ( node == null )
		{
			final Vector3D lfb = Vector3D.INIT.set( -100.0 , -100.0 , -100.0 );
			final Vector3D rfb = Vector3D.INIT.set(  100.0 , -100.0 , -100.0 );
			final Vector3D rbb = Vector3D.INIT.set(  100.0 ,  100.0 , -100.0 );
			final Vector3D lbb = Vector3D.INIT.set( -100.0 ,  100.0 , -100.0 );
			final Vector3D lft = Vector3D.INIT.set( -100.0 , -100.0 ,  100.0 );
			final Vector3D rft = Vector3D.INIT.set(  100.0 , -100.0 ,  100.0 );
			final Vector3D rbt = Vector3D.INIT.set(  100.0 ,  100.0 ,  100.0 );
			final Vector3D lbt = Vector3D.INIT.set( -100.0 ,  100.0 ,  100.0 );

			final Material topMaterial    = new Material(); topMaterial   .colorMap = "CUBE_TOP";
			final Material bottomMaterial = new Material(); bottomMaterial.colorMap = "CUBE_BOTTOM";
			final Material frontMaterial  = new Material(); frontMaterial .colorMap = "CUBE_FRONT";
			final Material backMaterial   = new Material(); backMaterial  .colorMap = "CUBE_BACK";
			final Material leftMaterial   = new Material(); leftMaterial  .colorMap = "CUBE_LEFT";
			final Material rightMaterial  = new Material(); rightMaterial .colorMap = "CUBE_RIGHT";

			final float[] textureU = { 0.5f , 0.5f , 0.0f , 0.0f };
			final float[] textureV = { 0.0f , 0.5f , 0.5f , 0.0f };

			cube = new Object3D();
			cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , topMaterial    , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , bottomMaterial , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , frontMaterial  , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , backMaterial   , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , leftMaterial   , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , rightMaterial  , textureU , textureV , 1.0f , false, false );
		}
		else
		{
			cube = (Object3D)node;
		}

		return cube;
	}

	/**
	 * This method constructs a cube with a different color per face and a different colormap per face.
	 * The width, height and depth of the cube are all 200 mm (-100 to 100).
	 *
	 * @see Object3D
	 *
	 * @return The constructed {@link Object3D}.
	 */
	public Object3D getTexturedColorCube()
	{
		final Node3D node = _viewModel.getNode3D( "texturedcolorcube" );
		final Object3D cube;

		if ( node == null )
		{
			final Vector3D lfb = Vector3D.INIT.set( -100.0 , -100.0 , -100.0 );
			final Vector3D rfb = Vector3D.INIT.set(  100.0 , -100.0 , -100.0 );
			final Vector3D rbb = Vector3D.INIT.set(  100.0 ,  100.0 , -100.0 );
			final Vector3D lbb = Vector3D.INIT.set( -100.0 ,  100.0 , -100.0 );
			final Vector3D lft = Vector3D.INIT.set( -100.0 , -100.0 ,  100.0 );
			final Vector3D rft = Vector3D.INIT.set(  100.0 , -100.0 ,  100.0 );
			final Vector3D rbt = Vector3D.INIT.set(  100.0 ,  100.0 ,  100.0 );
			final Vector3D lbt = Vector3D.INIT.set( -100.0 ,  100.0 ,  100.0 );

			final Material topMaterial    = new Material(); topMaterial   .colorMap = "CUBE_TOP_TEXTURE_AND_COLOR";       topMaterial.diffuseColorRed     = 1.0f; topMaterial.diffuseColorGreen       = 0.0f; topMaterial.diffuseColorBlue        = 0.0f;
			final Material bottomMaterial = new Material(); bottomMaterial.colorMap = "CUBE_BOTTOM_TEXTURE_AND_COLOR";    bottomMaterial.diffuseColorRed  = 0.0f; bottomMaterial.diffuseColorGreen    = 1.0f; bottomMaterial.diffuseColorBlue     = 0.0f;
			final Material frontMaterial  = new Material(); frontMaterial .colorMap = "CUBE_FRONT_TEXTURE_AND_COLOR";     frontMaterial.diffuseColorRed   = 0.0f; frontMaterial.diffuseColorGreen     = 0.0f; frontMaterial.diffuseColorBlue      = 1.0f;
			final Material backMaterial   = new Material(); backMaterial  .colorMap = "CUBE_BACK_TEXTURE_AND_COLOR";      backMaterial.diffuseColorRed    = 1.0f; backMaterial.diffuseColorGreen      = 1.0f; backMaterial.diffuseColorBlue       = 0.0f;
			final Material leftMaterial   = new Material(); leftMaterial  .colorMap = "CUBE_LEFT_TEXTURE_AND_COLOR";      leftMaterial.diffuseColorRed    = 0.0f; leftMaterial.diffuseColorGreen      = 1.0f; leftMaterial.diffuseColorBlue       = 1.0f;
			final Material rightMaterial  = new Material(); rightMaterial .colorMap = "CUBE_RIGHT_TEXTURE_AND_COLOR";     rightMaterial.diffuseColorRed   = 1.0f; rightMaterial.diffuseColorGreen     = 0.0f; rightMaterial.diffuseColorBlue      = 1.0f;

			final float[] textureU = { 0.5f , 0.5f , 0.0f , 0.0f };
			final float[] textureV = { 0.0f , 0.5f , 0.5f , 0.0f };

			cube = new Object3D();
			cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , topMaterial    , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , bottomMaterial , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , frontMaterial  , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , backMaterial   , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , leftMaterial   , textureU , textureV , 1.0f , false, false );
			cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , rightMaterial  , textureU , textureV , 1.0f , false, false );
		}
		else
		{
			cube = (Object3D)node;
		}

		return cube;
	}

	/**
	 * This method constructs an extruded object. The object is rectangular with
	 * a width and height of 100 mm and all extruded vertices are placed 100
	 * back (y) and 100 up (z).
	 *
	 * @see ExtrudedObject2D
	 *
	 * @return The constructed {@link ExtrudedObject2D}.
	 */
	public ExtrudedObject2D getExtrudedObject2D()
	{
		final Node3D node = _viewModel.getNode3D( "extruded" );
		final ExtrudedObject2D extrudedObject;

		if ( node == null )
		{
			final Material material  = new Material( Color.PINK.getRGB() );
			final Shape    shape     = new Rectangle2D.Double( 0.0 , 0.0 , 100.0 , 100.0 );
			final Vector3D extrusion = Vector3D.INIT.set( 0.0 , 100.0 , 100.0 );
			final Matrix3D transform = Matrix3D.INIT.setTranslation( -400.0 , 0.0 , -250.0 );

			extrudedObject = new ExtrudedObject2D( shape , extrusion , transform , material , 1.0 , true , false , false );
		}
		else
		{
			extrudedObject = (ExtrudedObject2D)node;
		}

		return extrudedObject;
	}

	/**
	 * This method constructs a light object with an intensity of 1 and falloff
	 * of 1.0.
	 *
	 * @see Light3D
	 *
	 * @return The constructed {@link Light3D}.
	 */
	public Light3D getLight3D()
	{
		final Node3D node = _viewModel.getNode3D( "light" );
		final Light3D light;

		if ( node == null )
		{
			light = new Light3D( 255 , 100.0 );
		}
		else
		{
			light = (Light3D)node;
		}

		return light;
	}
}
