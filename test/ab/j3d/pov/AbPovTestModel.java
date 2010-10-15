/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import java.awt.*;
import java.awt.geom.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.java2d.*;

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
		scene.addContentNode( "redbox"           , Matrix3D.getTransform( 10.0,  0.0,  0.0, -200.0,   0.0, -250.0 ), getRedXRotatedBox3D() );
		scene.addContentNode( "greenbox"         , Matrix3D.getTransform(  0.0, 10.0,  0.0,  -50.0,   0.0, -250.0 ), getGreenYRotatedBox3D() );
		scene.addContentNode( "bluebox"          , Matrix3D.getTransform(  0.0,  0.0, 10.0,  200.0,   0.0, -250.0 ), getBlueZRotatedBox3D() );
		scene.addContentNode( "panel"            , Matrix3D.getTransform(  0.0,  0.0, 45.0, -350.0,   0.0,    0.0 ), getTexturedBox3D() );
		scene.addContentNode( "sphere"           , Matrix3D.getTransform(  0.0,  0.0,  0.0,    0.0, 300.0, -200.0 ), getSphere3D() );
		scene.addContentNode( "cylinder"         , Matrix3D.getTransform(  0.0,  0.0,  0.0,    0.0,   0.0,  150.0 ), getCylinder3D() );
		scene.addContentNode( "cone"             , Matrix3D.getTransform( 45.0,  0.0,  0.0,  250.0,   0.0,    0.0 ), getCone3D() );
		scene.addContentNode( "extruded"         , Matrix3D.INIT, getExtrudedObject2D() );
		scene.addContentNode( "colorcube"        , Matrix3D.INIT, getColorCube() );
		scene.addContentNode( "texturedcolorcube", Matrix3D.INIT, getTexturedColorCube() );

		/*
		 * Create view.
		 */
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0, -1000.0, 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		final View3D view = new Java2dView( scene, null );
		view.setCameraControl( new FromToCameraControl( view, viewFrom, viewAt ) );
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
	 * This method constructs a red box of size 100 mm, rotated 10 degrees
	 * around the x-axis.
	 *
	 * @see Box3D
	 *
	 * @return The constructed {@link Box3D}.
	 */
	public Box3D getRedXRotatedBox3D()
	{
		final Box3D result;

		final ContentNode node = _scene.getContentNode( "redbox" );
		if ( node == null )
		{
			final Color color = Color.RED;
			final Material material  = createMaterialWithColor( color );
			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), material );
		}
		else
		{
			result = (Box3D)node.getNode3D();
		}

		return result;
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
		final Box3D result;

		final ContentNode node = _scene.getContentNode( "greenbox" );
		if ( node == null )
		{
			final Material material = createMaterialWithColor( Color.GREEN );
			material.diffuseColorAlpha = 0.2f;

			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), material );
		}
		else
		{
			result = (Box3D)node.getNode3D();
		}

		return result;
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
		final Box3D result;

		final ContentNode node = _scene.getContentNode( "bluebox" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.BLUE );
			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), material );
		}
		else
		{
			result = (Box3D)node.getNode3D();
		}

		return result;
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
		final Box3D result;

		final ContentNode node = _scene.getContentNode( "texturedbox" );
		if ( node == null )
		{
			final Material mainMaterial = createMaterialWithColorMap( "MPXs" );
			mainMaterial.colorMapWidth = 0.2f;
			mainMaterial.colorMapHeight = 0.2f;

			final Material sideMaterial = createMaterialWithColorMap( "MFCs" );
			sideMaterial.colorMapWidth = 0.2f;
			sideMaterial.colorMapHeight = 0.2f;

			result = new Box3D( 200.0, 10.0, 200.0, new BoxUVMap( Scene.MM ), mainMaterial, mainMaterial, sideMaterial, sideMaterial, sideMaterial, sideMaterial );
		}
		else
		{
			result = (Box3D)node.getNode3D();
		}

		return result;
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
		final Sphere3D result;

		final ContentNode node = _scene.getContentNode( "sphere" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.BLUE );
			result = new Sphere3D( 50.0, 20, 20, material );
		}
		else
		{
			result = (Sphere3D)node.getNode3D();
		}

		return result;
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
		final Cylinder3D cylinder;

		final ContentNode node = _scene.getContentNode( "cylinder" );
		if ( node == null )
		{
			final Material material = createMaterialWithColor( Color.MAGENTA );
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM );
			cylinder = new Cylinder3D( 100.0, 50.0, 100, material, uvMap, true, material, uvMap, material, uvMap, false );
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
	 * a circle.
	 *
	 * @see Cone3D
	 *
	 * @return The constructed {@link Cone3D}.
	 */
	public Cone3D getCone3D()
	{
		final Cone3D result;

		final ContentNode node = _scene.getContentNode( "cone" );
		if ( node == null )
		{
			final Material material = createMaterialWithColor( Color.WHITE );
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM );
			result = new Cone3D( 200.0, 100.0, 50.0, 100, material, uvMap, true, material, uvMap, material, uvMap, false );
		}
		else
		{
			result = (Cone3D)node.getNode3D();
		}

		return result;
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
		final Object3D cube;

		final ContentNode node = _scene.getContentNode( "colorcube" );
		if ( node == null )
		{
			final Vector3D lfb = Vector3D.INIT.set( -100.0, -100.0, -100.0 );
			final Vector3D rfb = Vector3D.INIT.set(  100.0, -100.0, -100.0 );
			final Vector3D rbb = Vector3D.INIT.set(  100.0,  100.0, -100.0 );
			final Vector3D lbb = Vector3D.INIT.set( -100.0,  100.0, -100.0 );
			final Vector3D lft = Vector3D.INIT.set( -100.0, -100.0,  100.0 );
			final Vector3D rft = Vector3D.INIT.set(  100.0, -100.0,  100.0 );
			final Vector3D rbt = Vector3D.INIT.set(  100.0,  100.0,  100.0 );
			final Vector3D lbt = Vector3D.INIT.set( -100.0,  100.0,  100.0 );

			final Material topMaterial    = createMaterialWithColorMap( "CUBE_TOP" );
			final Material bottomMaterial = createMaterialWithColorMap( "CUBE_BOTTOM" );
			final Material frontMaterial  = createMaterialWithColorMap( "CUBE_FRONT" );
			final Material backMaterial   = createMaterialWithColorMap( "CUBE_BACK" );
			final Material leftMaterial   = createMaterialWithColorMap( "CUBE_LEFT" );
			final Material rightMaterial  = createMaterialWithColorMap( "CUBE_RIGHT" );

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft, lbt, rbt, rft }, topMaterial   , null, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lfb, rfb, rbb }, bottomMaterial, null, null, false, false );
			builder.addFace( new Vector3D[] { lfb, lft, rft, rfb }, frontMaterial , null, null, false, false );
			builder.addFace( new Vector3D[] { rbb, rbt, lbt, lbb }, backMaterial  , null, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lbt, lft, lfb }, leftMaterial  , null, null, false, false );
			builder.addFace( new Vector3D[] { rfb, rft, rbt, rbb }, rightMaterial , null, null, false, false );
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
		final Object3D result;

		final ContentNode node = _scene.getContentNode( "texturedcolorcube" );
		if ( node == null )
		{
			final Vector3D lfb = Vector3D.INIT.set( -100.0, -100.0, -100.0 );
			final Vector3D rfb = Vector3D.INIT.set(  100.0, -100.0, -100.0 );
			final Vector3D rbb = Vector3D.INIT.set(  100.0,  100.0, -100.0 );
			final Vector3D lbb = Vector3D.INIT.set( -100.0,  100.0, -100.0 );
			final Vector3D lft = Vector3D.INIT.set( -100.0, -100.0,  100.0 );
			final Vector3D rft = Vector3D.INIT.set(  100.0, -100.0,  100.0 );
			final Vector3D rbt = Vector3D.INIT.set(  100.0,  100.0,  100.0 );
			final Vector3D lbt = Vector3D.INIT.set( -100.0,  100.0,  100.0 );

			final Material topMaterial    = createMaterialWithColorMap( "CUBE_TOP_TEXTURE_AND_COLOR" );    topMaterial.diffuseColorRed     = 1.0f; topMaterial.diffuseColorGreen       = 0.0f; topMaterial.diffuseColorBlue        = 0.0f;
			final Material bottomMaterial = createMaterialWithColorMap( "CUBE_BOTTOM_TEXTURE_AND_COLOR" ); bottomMaterial.diffuseColorRed  = 0.0f; bottomMaterial.diffuseColorGreen    = 1.0f; bottomMaterial.diffuseColorBlue     = 0.0f;
			final Material frontMaterial  = createMaterialWithColorMap( "CUBE_FRONT_TEXTURE_AND_COLOR" );  frontMaterial.diffuseColorRed   = 0.0f; frontMaterial.diffuseColorGreen     = 0.0f; frontMaterial.diffuseColorBlue      = 1.0f;
			final Material backMaterial   = createMaterialWithColorMap( "CUBE_BACK_TEXTURE_AND_COLOR" );   backMaterial.diffuseColorRed    = 1.0f; backMaterial.diffuseColorGreen      = 1.0f; backMaterial.diffuseColorBlue       = 0.0f;
			final Material leftMaterial   = createMaterialWithColorMap( "CUBE_LEFT_TEXTURE_AND_COLOR" );   leftMaterial.diffuseColorRed    = 0.0f; leftMaterial.diffuseColorGreen      = 1.0f; leftMaterial.diffuseColorBlue       = 1.0f;
			final Material rightMaterial  = createMaterialWithColorMap( "CUBE_RIGHT_TEXTURE_AND_COLOR" );  rightMaterial.diffuseColorRed   = 1.0f; rightMaterial.diffuseColorGreen     = 0.0f; rightMaterial.diffuseColorBlue      = 1.0f;

			final Point2D.Float[] texturePoints = { new Point2D.Float( 0.5f, 0.0f ), new Point2D.Float( 0.5f, 0.5f ), new Point2D.Float( 0.0f, 0.5f ), new Point2D.Float( 0.0f, 0.0f ) };

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft, lbt, rbt, rft }, topMaterial   , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lfb, rfb, rbb }, bottomMaterial, texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lfb, lft, rft, rfb }, frontMaterial , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rbb, rbt, lbt, lbb }, backMaterial  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lbt, lft, lfb }, leftMaterial  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rfb, rft, rbt, rbb }, rightMaterial , texturePoints, null, false, false );
			result = builder.getObject3D();
		}
		else
		{
			result = (Object3D)node.getNode3D();
		}

		return result;
	}

	/**
	 * This method constructs an extruded object. The object is rectangular with
	 * a width and height of 100 mm and all extruded vertices are placed 100
	 * back (y) and 100 up (z).
	 *
	 * @return The constructed extruded object.
	 */
	public Object3D getExtrudedObject2D()
	{
		final Object3D result;

		final ContentNode node = _scene.getContentNode( "extruded" );
		if ( node == null )
		{
			final Material material  = createMaterialWithColor( Color.PINK );
			final Shape    shape     = new Rectangle2D.Double( 0.0, 0.0, 100.0, 100.0 );
			final Vector3D extrusion = Vector3D.INIT.set( 0.0, 100.0, 100.0 );
			final Matrix3D transform = Matrix3D.INIT.setTranslation( -400.0, 0.0, -250.0 );

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addExtrudedShape( shape, 1.0, extrusion, transform, material, new BoxUVMap( Scene.MM, Matrix3D.INIT ), false, true, false, false );
			result = builder.getObject3D();
		}
		else
		{
			result = (Object3D)node.getNode3D();
		}

		return result;
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
		final Light3D result;

		final ContentNode node = _scene.getContentNode( "light" );
		if ( node == null )
		{
			result = new Light3D();
			final float fallOffDistance = 100.0f;
			result.setAttenuation( 0.0f, 0.0f, 1.0f / ( fallOffDistance * fallOffDistance ) );
		}
		else
		{
			result = (Light3D)node.getNode3D();
		}

		return result;
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
