/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2009
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
import ab.j3d.model.ContentNode;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Object3DBuilder;
import ab.j3d.model.Scene;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.View3D;
import ab.j3d.view.java2d.Java2dEngine;
import ab.j3d.view.java2d.Java2dView;

/**
 * This class constructs a testmodel ({@link Java2dEngine}) for testing the
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
	 * The used scene.
	 */
	private final Scene _scene = new Scene( Scene.MM );

	/**
	 * The used model.
	 */
	private final View3D _view;

	/**
	 * Construct new {@link AbPovTestModel}.
	 */
	public AbPovTestModel()
	{
		/*
		 * Fill scene with objects from the testmodel.
		 */
		final Scene scene = _scene;
		scene.addContentNode( "camera"            , Matrix3D.INIT , getCamera3D() );
		scene.addContentNode( "redbox"            , Matrix3D.INIT , getRedXRotatedBox3D() );
		scene.addContentNode( "greenbox"          , Matrix3D.INIT , getGreenYRotatedBox3D() );
		scene.addContentNode( "bluebox"           , Matrix3D.INIT , getBlueZRotatedBox3D() );
		scene.addContentNode( "panel"             , Matrix3D.INIT , getTexturedBox3D() );
		scene.addContentNode( "sphere"            , Matrix3D.getTransform(  0.0 , 0.0 , 0.0 ,   0.0 , 300.0 , -200.0 ) , getSphere3D() );
		scene.addContentNode( "cylinder"          , Matrix3D.getTransform(  0.0 , 0.0 , 0.0 ,   0.0 ,   0.0 ,  150.0 ) , getCylinder3D() );
		scene.addContentNode( "cone"              , Matrix3D.getTransform( 45.0 , 0.0 , 0.0 , 250.0 ,   0.0 ,    0.0 ) , getCone3D() );
		scene.addContentNode( "extruded"          , Matrix3D.INIT , getExtrudedObject2D() );
		scene.addContentNode( "colorcube"         , Matrix3D.INIT , getColorCube() );
		scene.addContentNode( "texturedcolorcube" , Matrix3D.INIT , getTexturedColorCube() );

		/*
		 * Create view.
		 */
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		final View3D view = new Java2dView( scene , null );
		view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );
		_view = view;
	}

	/**
	 * Get a reference to the used scene.
	 *
	 * @return The test scene.
	 */
	public Scene getScene()
	{
		return _scene;
	}

	/**
	 * Get a reference to the used model.
	 *
	 * @return The test model.
	 */
	public View3D getView()
	{
		return _view;
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
		final Camera3D camera3D;

		final ContentNode node = _scene.getContentNode( "camera" );
		if ( node == null )
		{
			camera3D = new Camera3D( 1.0, 45.0 );
		}
		else
		{
			camera3D = (Camera3D)node.getNode3D();
		}

		return camera3D;
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
		final Box3D box;

		final ContentNode node = _scene.getContentNode( "redbox" );
		if ( node == null )
		{
			final Color color = Color.RED;
			final Material material  = createMaterialWithColor( color );
			final Matrix3D rotate    = Matrix3D.INIT.rotateX( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( -200.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			box = new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material , material , material , material , material );
		}
		else
		{
			box = (Box3D)node.getNode3D();
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
		final Box3D box;

		final ContentNode node = _scene.getContentNode( "greenbox" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.GREEN );
			final Matrix3D rotate    = Matrix3D.INIT.rotateY( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( -50.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			material.diffuseColorAlpha = 0.2f;

			box = new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material , material , material , material , material );
		}
		else
		{
			box = (Box3D)node.getNode3D();
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
		final Box3D box;

		final ContentNode node = _scene.getContentNode( "bluebox" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.BLUE );
			final Matrix3D rotate    = Matrix3D.INIT.rotateZ( Math.toRadians( 10.0 ) );
			final Matrix3D translate = Matrix3D.INIT.setTranslation( 200.0 , 0.0 , -250.0 );
			final Matrix3D transform = rotate.multiply( translate );

			box =  new Box3D( transform , 100.0 , 200.0 , 100.0 , 0.001 , material , material , material , material , material , material );
		}
		else
		{
			box = (Box3D)node.getNode3D();
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
		final Box3D box;

		final ContentNode node = _scene.getContentNode( "texturedbox" );
		if ( node == null )
		{
			Matrix3D transform = Matrix3D.INIT.rotateZ( Math.toRadians( 45.0 ) );
			transform = transform.setTranslation( -350.0 , 0.0 , 0.0 );

			final Material mainMaterial = createMaterialWithColorMap( "MPXs" );
			mainMaterial.colorMapWidth = 0.2f;
			mainMaterial.colorMapHeight = 0.2f;

			final Material sideMaterial = createMaterialWithColorMap( "MFCs" );
			sideMaterial.colorMapWidth = 0.2f;
			sideMaterial.colorMapHeight = 0.2f;

			box = new Box3D( transform , 200.0 , 10.0 , 200.0 , 0.001 , mainMaterial , mainMaterial , sideMaterial , sideMaterial , sideMaterial , sideMaterial );
		}
		else
		{
			box = (Box3D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "sphere" );
		final Sphere3D sphere;

		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.BLUE );
			sphere = new Sphere3D( 100.0 , 20 , 20 , material );
		}
		else
		{
			sphere = (Sphere3D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "cylinder" );
		final Cylinder3D cylinder;

		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.MAGENTA );
			cylinder = new Cylinder3D( 50.0 , 50.0 , 100.0 , 100 , material , true , true , true , true );
		}
		else
		{
			cylinder = (Cylinder3D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "cone" );
		final Cylinder3D cone;

		if ( node == null )
		{
			final Material material = createMaterialWithColor( Color.WHITE );
			cone = new Cylinder3D( 100.0 , 50.0 , 200.0 , 100 , material , true , true , true , true );
		}
		else
		{
			cone = (Cylinder3D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "colorcube" );
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

			final Material topMaterial    = createMaterialWithColorMap( "CUBE_TOP" );
			final Material bottomMaterial = createMaterialWithColorMap( "CUBE_BOTTOM" );
			final Material frontMaterial  = createMaterialWithColorMap( "CUBE_FRONT" );
			final Material backMaterial   = createMaterialWithColorMap( "CUBE_BACK" );
			final Material leftMaterial   = createMaterialWithColorMap( "CUBE_LEFT" );
			final Material rightMaterial  = createMaterialWithColorMap( "CUBE_RIGHT" );

			final float[] textureU = { 0.5f , 0.5f , 0.0f , 0.0f };
			final float[] textureV = { 0.0f , 0.5f , 0.5f , 0.0f };

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft , lbt , rbt , rft } , topMaterial    , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , bottomMaterial , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lfb , lft , rft , rfb } , frontMaterial  , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , backMaterial   , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , leftMaterial   , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , rightMaterial  , textureU , textureV , false , false );
			cube = builder.getObject3D();
		}
		else
		{
			cube = (Object3D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "texturedcolorcube" );
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

			final Material topMaterial    = createMaterialWithColorMap( "CUBE_TOP_TEXTURE_AND_COLOR" );    topMaterial.diffuseColorRed     = 1.0f; topMaterial.diffuseColorGreen       = 0.0f; topMaterial.diffuseColorBlue        = 0.0f;
			final Material bottomMaterial = createMaterialWithColorMap( "CUBE_BOTTOM_TEXTURE_AND_COLOR" ); bottomMaterial.diffuseColorRed  = 0.0f; bottomMaterial.diffuseColorGreen    = 1.0f; bottomMaterial.diffuseColorBlue     = 0.0f;
			final Material frontMaterial  = createMaterialWithColorMap( "CUBE_FRONT_TEXTURE_AND_COLOR" );  frontMaterial.diffuseColorRed   = 0.0f; frontMaterial.diffuseColorGreen     = 0.0f; frontMaterial.diffuseColorBlue      = 1.0f;
			final Material backMaterial   = createMaterialWithColorMap( "CUBE_BACK_TEXTURE_AND_COLOR" );   backMaterial.diffuseColorRed    = 1.0f; backMaterial.diffuseColorGreen      = 1.0f; backMaterial.diffuseColorBlue       = 0.0f;
			final Material leftMaterial   = createMaterialWithColorMap( "CUBE_LEFT_TEXTURE_AND_COLOR" );   leftMaterial.diffuseColorRed    = 0.0f; leftMaterial.diffuseColorGreen      = 1.0f; leftMaterial.diffuseColorBlue       = 1.0f;
			final Material rightMaterial  = createMaterialWithColorMap( "CUBE_RIGHT_TEXTURE_AND_COLOR" );  rightMaterial.diffuseColorRed   = 1.0f; rightMaterial.diffuseColorGreen     = 0.0f; rightMaterial.diffuseColorBlue      = 1.0f;

			final float[] textureU = { 0.5f , 0.5f , 0.0f , 0.0f };
			final float[] textureV = { 0.0f , 0.5f , 0.5f , 0.0f };

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft , lbt , rbt , rft } , topMaterial    , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , bottomMaterial , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lfb , lft , rft , rfb } , frontMaterial  , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , backMaterial   , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , leftMaterial   , textureU , textureV , false , false );
			builder.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , rightMaterial  , textureU , textureV , false , false );
			cube = builder.getObject3D();
		}
		else
		{
			cube = (Object3D)node.getNode3D();
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
		final ExtrudedObject2D extrudedObject;

		final ContentNode node = _scene.getContentNode( "extruded" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.PINK );
			final Shape    shape     = new Rectangle2D.Double( 0.0 , 0.0 , 100.0 , 100.0 );
			final Vector3D extrusion = Vector3D.INIT.set( 0.0 , 100.0 , 100.0 );
			final Matrix3D transform = Matrix3D.INIT.setTranslation( -400.0 , 0.0 , -250.0 );

			extrudedObject = new ExtrudedObject2D( shape , extrusion , transform , material , 1.0 , true , false , false );
		}
		else
		{
			extrudedObject = (ExtrudedObject2D)node.getNode3D();
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
		final ContentNode node = _scene.getContentNode( "light" );
		final Light3D light;

		if ( node == null )
		{
			light = new Light3D( 255 , 100.0 );
		}
		else
		{
			light = (Light3D)node.getNode3D();
		}

		return light;
	}

	/**
	 * Create material for solid color.
	 *
	 * @param   color   Color.
	 *
	 * @return  {@link Material}.
	 */
	private static Material createMaterialWithColor( final Color color )
	{
		final Material result = new Material( color.getRGB() );
		result.specularColorRed = 1.0f;
		result.specularColorGreen = 1.0f;
		result.specularColorBlue = 1.0f;
		result.shininess = 16;
		return result;
	}

	/**
	 * Create material with color map.
	 *
	 * @param   colorMap    Color map name.
	 *
	 * @return  {@link Material}.
	 */
	private static Material createMaterialWithColorMap( final String colorMap )
	{
		final Material result = new Material();
		result.code = colorMap;
		result.specularColorRed = 1.0f;
		result.specularColorGreen = 1.0f;
		result.specularColorBlue = 1.0f;
		result.shininess = 16;
		result.colorMap = colorMap;
		return result;
	}
}
