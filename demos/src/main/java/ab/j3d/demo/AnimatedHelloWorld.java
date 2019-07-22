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
package ab.j3d.demo;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * An extended 'Hello world' example with a simple animation going.
 *
 * @author Peter S. Heijnen
 */
public class AnimatedHelloWorld
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		SwingUtilities.invokeAndWait( new Runnable()
		{
			@Override
			public void run()
			{
				final AnimatedHelloWorld demo = new AnimatedHelloWorld();
				Ab3dExample.createFrame( demo.getClass().getSimpleName(), 800, 600, demo.init( "luscious" ) );
				demo.start();

			}
		} );
	}

	/**
	 * The scene.
	 */
	protected Scene _scene;

	/**
	 * Initialize application.
	 *
	 * @param engineName Name of engine to use.
	 *
	 * @return Component containing example.
	 *
	 * @see Ab3dExample#createRenderEngine(String)
	 */
	public Component init( final String engineName )
	{
		final RenderEngine engine = Ab3dExample.createRenderEngine( engineName );

		final Scene scene = createScene();
		_scene = scene;

		final View3D view = engine.createView( scene );
		view.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
		final Vector3D lookFrom = new Vector3D( 1.55, -19.73, 7.79 );
		final Vector3D lookAt = new Vector3D( 0.32, 0.33, 0.80 );
		view.setCameraControl( new FromToCameraControl( view, lookFrom, lookAt, Vector3D.POSITIVE_Z_AXIS ) );
		return view.getComponent();
	}

	/**
	 * Create scene to show.
	 *
	 * @return {@link Scene} to show.
	 */
	protected Scene createScene()
	{
		final Scene scene = new Scene( Scene.M );

		final Appearance backgroundAppearance = BasicAppearances.ALU_PLATE;
		final BoxUVMap backgroundMap = new BoxUVMap( Scene.CM );
		scene.addContentNode( "alu-plate", Matrix3D.getTranslation( -500.0, 10.0, -500.0 ), new Box3D( 1000.0, 0.1, 1000.0, backgroundAppearance, backgroundMap, backgroundAppearance, backgroundMap, backgroundAppearance, backgroundMap, backgroundAppearance, backgroundMap, backgroundAppearance, backgroundMap, backgroundAppearance, backgroundMap ) );

		scene.addContentNode( "Hello world", Matrix3D.IDENTITY, HelloWorld.createHelloWorld3D() );

		addPrimitivesBelt( scene );

		scene.setAmbient( 0.4f, 0.4f, 0.4f );

		final SpotLight3D spotLight1a = new SpotLight3D( Vector3D.normalize( -3.0, 5.0, -3.0 ), 10.0f );
		spotLight1a.setIntensity( 1.0f, 2.0f, 2.0f );
		spotLight1a.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight1a.setConcentration( 1.0f );
		spotLight1a.setCastingShadows( true );
		scene.addContentNode( "spotLight1a", Matrix3D.getTranslation( 4.0, -5.0, 3.0 ), spotLight1a );

		final SpotLight3D spotLight1b = new SpotLight3D( Vector3D.normalize( -3.0, 5.0, -3.0 ), 10.0f );
		spotLight1b.setIntensity( 2.0f, 1.0f, 1.0f );
		spotLight1b.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight1b.setConcentration( 1.0f );
		spotLight1b.setCastingShadows( true );
		scene.addContentNode( "spotLight1b", Matrix3D.getTranslation( 5.0, -5.0, 3.0 ), spotLight1b );

		final SpotLight3D spotLight2 = new SpotLight3D( Vector3D.normalize( 0.0, 1.0, 0.0 ), 35.0f );
		spotLight2.setIntensity( 1.0f );
		spotLight2.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight2.setConcentration( 1.0f );
		spotLight2.setCastingShadows( true );
		scene.addContentNode( "spotLight2", Matrix3D.getTranslation( 0.0, -10.0, 0.0 ), spotLight2 );

		return scene;
	}

	/**
	 * Start animation.
	 */
	public void start()
	{
		_scene.setAnimated( true );
	}

	/**
	 * Stop animation.
	 */
	public void stop()
	{
		_scene.setAnimated( false );
	}

	/**
	 * Add animated belt of primities to the scene.
	 *
	 * @param scene Scene to add content to.
	 */
	private static void addPrimitivesBelt( final Scene scene )
	{
		final Transform3D box = new Transform3D( Matrix3D.getTranslation( -1.0, -1.0, -1.0 ), new Box3D( 2.0, 2.0, 2.0, BasicAppearances.RED, null, BasicAppearances.GREEN, null, BasicAppearances.BLUE, null, BasicAppearances.CYAN, null, BasicAppearances.MAGENTA, null, BasicAppearances.YELLOW, null ) );
		final Transform3D boxRotator = new Rotator( 0.0 );
		boxRotator.addChild( box );
		scene.addContentNode( "box", Matrix3D.IDENTITY, boxRotator );

		final Sphere3D sphere = new Sphere3D( 1.0, 13, 13, BasicAppearances.BLUE );
		final Transform3D sphereRotator = new Rotator( 60.0 );
		sphereRotator.addChild( sphere );
		scene.addContentNode( "sphere", Matrix3D.IDENTITY, sphereRotator );

		final Cylinder3D cylinder = new Cylinder3D( 2.0, 1.0, 15, BasicAppearances.GREEN, null, true, BasicAppearances.BLUE, null, BasicAppearances.RED, null, false );
		final Transform3D cylinderRotator = new Rotator( 120.0 );
		cylinderRotator.addChild( cylinder );
		scene.addContentNode( "cylinder", Matrix3D.IDENTITY, cylinderRotator );

		final Cone3D cone = new Cone3D( 2.0, 1.0, 0.0, 15, BasicAppearances.GREEN, null, true, BasicAppearances.BLUE, null, BasicAppearances.RED, null, false );
		final Transform3D coneRotator = new Rotator( 180.0 );
		coneRotator.addChild( cone );
		scene.addContentNode( "cone", Matrix3D.IDENTITY, coneRotator );

		final Shape shapeToExtrude = new Arc2D.Double( -1.0, -1.0, 2.0, 2.0, 80.0, 280.0, Arc2D.PIE );

		final Object3DBuilder builder = new Object3DBuilder();
		final Tessellator shapeTessellator = ShapeTools.createTessellator( shapeToExtrude, 0.025 );
		builder.addExtrudedShape( shapeTessellator, Vector3D.POSITIVE_Z_AXIS, true, Matrix3D.IDENTITY, true, BasicAppearances.BLUE, null, false, true, BasicAppearances.RED, null, false, true, BasicAppearances.GREEN, null, false, false, false, false );
		final Object3D extrudedShape = builder.getObject3D();

		final Transform3D extrudedShapeRotator = new Rotator( 240.0 );
		extrudedShapeRotator.addChild( extrudedShape );
		scene.addContentNode( "extrudedShape", Matrix3D.IDENTITY, extrudedShapeRotator );

		final Object3D mesh = createAbMesh();
		final Transform3D meshRotator = new Rotator( 300.0 );
		meshRotator.addChild( mesh );
		scene.addContentNode( "mesh", Matrix3D.IDENTITY, meshRotator );
	}

	/**
	 * Create a mesh with two 'AB' characters.
	 *
	 * @return {@link Object3D} containing 'AB' mesh.
	 */
	private static Object3D createAbMesh()
	{
		final Path2D freeShape = new Path2D.Double();

		// A
		freeShape.moveTo( -1.0, -1.0 );
		freeShape.lineTo( -0.7, -1.0 );
		freeShape.lineTo( -0.6, -0.6 );
		freeShape.lineTo( -0.4, -0.6 );
		freeShape.lineTo( -0.3, -1.0 );
		freeShape.lineTo( 0.0, -1.0 );
		freeShape.lineTo( -0.4, 1.0 );
		freeShape.lineTo( -0.6, 1.0 );
		freeShape.closePath();

		// B
		freeShape.moveTo( 0.0, -1.0 );
		freeShape.lineTo( 0.3, -1.0 );
		freeShape.curveTo( 1.0, -1.0, 1.0, 0.0, 0.3, 0.0 );
		freeShape.curveTo( 1.0, 0.0, 1.0, 1.0, 0.3, 1.0 );
		freeShape.lineTo( 0.0, 1.0 );
		freeShape.closePath();

		final Object3DBuilder builder = new Object3DBuilder();
		final Tessellator freeShapeTessellator = ShapeTools.createTessellator( freeShape, 0.1 );
		builder.addExtrudedShape( freeShapeTessellator, new Vector3D( 0.0, 0.0, 0.5 ), true, Matrix3D.getTranslation( 0.0, 0.0, 0.8 ), true, BasicAppearances.BLUE, null, false, true, BasicAppearances.RED, null, false, true, BasicAppearances.GREEN, null, false, false, false, true );
		builder.addExtrudedShape( freeShapeTessellator, new Vector3D( 0.0, 0.0, 0.5 ), true, Matrix3D.getTransform( 0.0, 0.0, 90.0, 0.0, 0.0, 0.0 ), true, BasicAppearances.BLUE, null, false, true, BasicAppearances.RED, null, false, true, BasicAppearances.GREEN, null, false, false, false, true );
		return builder.getObject3D();
	}

	/**
	 * This rotates an object around itself and along a big circle.
	 */
	private static class Rotator
	extends Transform3D
	{
		/**
		 * Orbit radius.
		 */
		private final double _orbitRadius;

		/**
		 * Orbit rotation speed about X-axis in degrees per millisecond.
		 */
		private final double _orbitRotationSpeedX;

		/**
		 * Orbit rotation speed about Z-axis in degrees per millisecond.
		 */
		private final double _orbitRotationSpeedZ;

		/**
		 * Start azimuth angle on orbital sphere.
		 */
		private final double _orbitStartAngle;

		/**
		 * Rotation about X-axis in degrees per millisecond.
		 */
		private final double _roationSpeedX;

		/**
		 * Rotation about Y-axis in degrees per millisecond.
		 */
		private final double _rotationSpeedY;

		/**
		 * Construct rotator.
		 *
		 * @param orbitStartAngle Start azimuth angle on orbital sphere.
		 */
		private Rotator( final double orbitStartAngle )
		{
			final Random random = new Random();

			_orbitRadius = 6.0;
			_orbitRotationSpeedX = 1.0 / 13.0;
			_orbitRotationSpeedZ = 1.0 / 11.0;
			_orbitStartAngle = orbitStartAngle;
			_roationSpeedX = 1.0 / ( 5.0 + random.nextDouble() * 15.0 );
			_rotationSpeedY = 1.0 / ( 5.0 + random.nextDouble() * 15.0 );
		}

		@NotNull
		@Override
		public Matrix3D getTransform()
		{
			final double time = (double)System.currentTimeMillis();
			final double orbitAngleZ = _orbitStartAngle + time * _orbitRotationSpeedZ;
			final double orbitAngleX = time * _orbitRotationSpeedX;
			final double rotationAngleX = time * _roationSpeedX;
			final double rotationAngleZ = time * _rotationSpeedY;

			Matrix3D orbitTransform = Matrix3D.getTransform( orbitAngleX, 0.0, orbitAngleZ, 0.0, 0.0, 0.0 );
			orbitTransform = orbitTransform.plus( orbitTransform.transform( _orbitRadius, 0.0, 0.0 ) );
			final Matrix3D rotationTransform = Matrix3D.getTransform( rotationAngleX, rotationAngleZ, 0.0, 0.0, 0.0, 0.0 );
			return rotationTransform.multiply( orbitTransform );
		}
	}
}
