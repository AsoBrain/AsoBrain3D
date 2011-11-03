/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
import ab.j3d.appearance.*;
import ab.j3d.awt.*;
import ab.j3d.awt.view.java2d.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

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
public class AbPovTestModel
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
		 * Fill scene with objects from the test model.
		 */
		final Scene scene = _scene;
		scene.addContentNode( "redbox"           , Matrix3D.getTransform( 10.0,  0.0,  0.0, -200.0,   0.0, -250.0 ), getRedXRotatedBox3D() );
		scene.addContentNode( "greenbox"         , Matrix3D.getTransform(  0.0, 10.0,  0.0,  -50.0,   0.0, -250.0 ), getGreenYRotatedBox3D() );
		scene.addContentNode( "bluebox"          , Matrix3D.getTransform(  0.0,  0.0, 10.0,  200.0,   0.0, -250.0 ), getBlueZRotatedBox3D() );
		scene.addContentNode( "panel"            , Matrix3D.getTransform(  0.0,  0.0, 45.0, -350.0,   0.0,    0.0 ), getTexturedBox3D() );
		scene.addContentNode( "sphere"           , Matrix3D.getTransform(  0.0,  0.0,  0.0,    0.0, 300.0, -200.0 ), getSphere3D() );
		scene.addContentNode( "cylinder"         , Matrix3D.getTransform(  0.0,  0.0,  0.0,    0.0,   0.0,  150.0 ), getCylinder3D() );
		scene.addContentNode( "cone"             , Matrix3D.getTransform( 45.0,  0.0,  0.0,  250.0,   0.0,    0.0 ), getCone3D() );
		scene.addContentNode( "extrudedA"        , Matrix3D.IDENTITY, getExtrudedObject2DA() );
		scene.addContentNode( "extrudedB"        , Matrix3D.IDENTITY, getExtrudedObject2DB() );
		scene.addContentNode( "colorcube"        , Matrix3D.IDENTITY, getColorCube() );
		scene.addContentNode( "texturedcolorcube", Matrix3D.IDENTITY, getTexturedColorCube() );

		/*
		 * Create view.
		 */
		final Vector3D viewFrom = new Vector3D( 0.0, -1000.0, 0.0 );
		final Vector3D viewAt   = Vector3D.ZERO;

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
			final Color4 color = Color4.RED;
			final Appearance appearance  = createAppearanceWithColor( color );
			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), appearance );
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
			final Appearance appearance = createAppearanceWithColor( new Color4f( 0.0f, 1.0f, 0.0f, 0.2f ) );
			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), appearance );
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
			final Appearance appearance  = createAppearanceWithColor( Color4.BLUE );
			result = new Box3D( 100.0, 200.0, 100.0, new BoxUVMap( Scene.MM ), appearance );
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
			final Appearance mainAppearance = createAppearanceWithColorMap( "ab/j3d/pov/textures/MPXs.jpg", 0.2f, 0.2f );
			final Appearance sideAppearance = createAppearanceWithColorMap( "ab/j3d/pov/textures/MFCs.jpg", 0.2f, 0.2f );

			result = new Box3D( 200.0, 10.0, 200.0, new BoxUVMap( Scene.MM ), mainAppearance, mainAppearance, sideAppearance, sideAppearance, sideAppearance, sideAppearance );
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
			final Appearance appearance  = createAppearanceWithColor( Color4.BLUE );
			result = new Sphere3D( 50.0, 20, 20, appearance );
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
			final Appearance appearance = createAppearanceWithColor( Color4.MAGENTA );
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM );
			cylinder = new Cylinder3D( 100.0, 50.0, 100, appearance, uvMap, true, appearance, uvMap, appearance, uvMap, false );
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
			final Appearance appearance = createAppearanceWithColor( Color4.WHITE );
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM );
			result = new Cone3D( 200.0, 100.0, 50.0, 100, appearance, uvMap, true, appearance, uvMap, appearance, uvMap, false );
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
			final Vector3D lfb = new Vector3D( -100.0, -100.0, -100.0 );
			final Vector3D rfb = new Vector3D(  100.0, -100.0, -100.0 );
			final Vector3D rbb = new Vector3D(  100.0,  100.0, -100.0 );
			final Vector3D lbb = new Vector3D( -100.0,  100.0, -100.0 );
			final Vector3D lft = new Vector3D( -100.0, -100.0,  100.0 );
			final Vector3D rft = new Vector3D(  100.0, -100.0,  100.0 );
			final Vector3D rbt = new Vector3D(  100.0,  100.0,  100.0 );
			final Vector3D lbt = new Vector3D( -100.0,  100.0,  100.0 );

			final Appearance topAppearance    = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_TOP.jpg", 0.0f, 0.0f );
			final Appearance bottomAppearance = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_BOTTOM.jpg", 0.0f, 0.0f );
			final Appearance frontAppearance  = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_FRONT.jpg", 0.0f, 0.0f );
			final Appearance backAppearance   = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_BACK.jpg", 0.0f, 0.0f );
			final Appearance leftAppearance   = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_LEFT.jpg", 0.0f, 0.0f );
			final Appearance rightAppearance  = createAppearanceWithColorMap( "ab/j3d/pov/textures/CUBE_RIGHT.jpg", 0.0f, 0.0f );

			final float[] texturePoints = { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft, lbt, rbt, rft }, topAppearance   , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lfb, rfb, rbb }, bottomAppearance, texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lfb, lft, rft, rfb }, frontAppearance , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rbb, rbt, lbt, lbb }, backAppearance  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lbt, lft, lfb }, leftAppearance  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rfb, rft, rbt, rbb }, rightAppearance , texturePoints, null, false, false );
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
			final Vector3D lfb = new Vector3D( -100.0, -100.0, -100.0 );
			final Vector3D rfb = new Vector3D(  100.0, -100.0, -100.0 );
			final Vector3D rbb = new Vector3D(  100.0,  100.0, -100.0 );
			final Vector3D lbb = new Vector3D( -100.0,  100.0, -100.0 );
			final Vector3D lft = new Vector3D( -100.0, -100.0,  100.0 );
			final Vector3D rft = new Vector3D(  100.0, -100.0,  100.0 );
			final Vector3D rbt = new Vector3D(  100.0,  100.0,  100.0 );
			final Vector3D lbt = new Vector3D( -100.0,  100.0,  100.0 );

			final BasicAppearance topAppearance    = createAppearanceWithColorMap( "CUBE_TOP_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f );    topAppearance   .setDiffuseColor( Color4.RED );
			final BasicAppearance bottomAppearance = createAppearanceWithColorMap( "CUBE_BOTTOM_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f ); bottomAppearance.setDiffuseColor( Color4.GREEN );
			final BasicAppearance frontAppearance  = createAppearanceWithColorMap( "CUBE_FRONT_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f );  frontAppearance .setDiffuseColor( Color4.BLUE );
			final BasicAppearance backAppearance   = createAppearanceWithColorMap( "CUBE_BACK_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f );   backAppearance  .setDiffuseColor( Color4.YELLOW );
			final BasicAppearance leftAppearance   = createAppearanceWithColorMap( "CUBE_LEFT_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f );   leftAppearance  .setDiffuseColor( Color4.CYAN );
			final BasicAppearance rightAppearance  = createAppearanceWithColorMap( "CUBE_RIGHT_TEXTURE_AND_COLOR.jpg", 0.0f, 0.0f );  rightAppearance .setDiffuseColor( Color4.MAGENTA );

			final float[] texturePoints = { 0.5f, 0.0f, 0.5f, 0.5f, 0.0f, 0.5f, 0.0f, 0.0f};

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addFace( new Vector3D[] { lft, lbt, rbt, rft }, topAppearance   , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lfb, rfb, rbb }, bottomAppearance, texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lfb, lft, rft, rfb }, frontAppearance , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rbb, rbt, lbt, lbb }, backAppearance  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { lbb, lbt, lft, lfb }, leftAppearance  , texturePoints, null, false, false );
			builder.addFace( new Vector3D[] { rfb, rft, rbt, rbb }, rightAppearance , texturePoints, null, false, false );
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
	public Object3D getExtrudedObject2DA()
	{
		final Object3D result;

		final ContentNode node = _scene.getContentNode( "extrudedA" );
		if ( node == null )
		{
			final Appearance appearance  = createAppearanceWithColor( Color4.PINK );
			final Shape    shape     = new Rectangle2D.Double( 0.0, 0.0, 100.0, 100.0 );
			final Vector3D extrusion = new Vector3D( 0.0, 100.0, 100.0 );
			final Matrix3D transform = Matrix3D.getTranslation( -400.0, 0.0, -250.0 );

			final Object3DBuilder builder = new Object3DBuilder();
			ShapeTools.addExtrudedShape( builder, shape, 1.0, extrusion, transform, appearance, new BoxUVMap( Scene.MM, Matrix3D.IDENTITY ), false, true, false, false );
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
	public Object3D getExtrudedObject2DB()
	{
		final Object3D result;

		final ContentNode node = _scene.getContentNode( "extrudedB" );
		if ( node == null )
		{
			final Appearance appearance = BasicAppearances.ORANGE;
			final BoxUVMap uvMap = new BoxUVMap( Scene.MM, Matrix3D.IDENTITY );
			final Shape shape = new Rectangle2D.Double( 0.0, 0.0, 100.0, 100.0 );
			final Vector3D extrusion = new Vector3D( 0.0, 0.0, -50.0 );
			final Matrix3D transform = Matrix3D.getTranslation( -400.0, 0.0, -250.0 );

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addExtrudedShape( ShapeTools.createTessellator( shape, 1.0 ), extrusion, true, transform, true, appearance, uvMap, false, false, null, null, false, true, appearance, uvMap, false, true, false, false );
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
	 * Create appearance for solid color.
	 *
	 * @param   color   Color.
	 *
	 * @return  {@link BasicAppearance}.
	 */
	private static BasicAppearance createAppearanceWithColor( final Color4 color )
	{
		final BasicAppearance result = new BasicAppearance();
		result.setAmbientColor( color );
		result.setDiffuseColor( color );
		result.setSpecularColor( Color4.WHITE );
		result.setShininess( 16 );
		return result;
	}

	/**
	 * Create appearance with color map.
	 *
	 * @param   colorMap        Color map.
	 * @param   physicalWidth   Physical width of color map.
	 * @param   physicalHeight  Physical height of color map.
	 *
	 * @return  {@link BasicAppearance}.
	 */
	private static BasicAppearance createAppearanceWithColorMap( final String colorMap, final float physicalWidth, final float physicalHeight )
	{
		final BasicAppearance result = new BasicAppearance();
		result.setSpecularColor( Color4.WHITE );
		result.setShininess( 16 );
		result.setColorMap( new FileTextureMap( AbPovTestModel.class.getResource( colorMap ), physicalWidth, physicalHeight ) );
		return result;
	}
}
