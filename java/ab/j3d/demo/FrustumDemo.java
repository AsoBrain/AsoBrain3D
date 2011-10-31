/*
 * $Id$
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
package ab.j3d.demo;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.awt.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * TODO
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class FrustumDemo
{
	/**
	 * The scene.
	 */
	protected Scene _scene;

	protected View3D _normalView;

	private FrustumNode _frustumNode;

	/**
	 * Initialize application.
	 *
	 * @param   engineName  Name of engine to use.
	 *
	 * @return  Component containing example.
	 *
	 * @see     {@link Ab3dExample#createRenderEngine(String)}
	 */
	public Component init( final String engineName )
	{
		final RenderEngine engine = Ab3dExample.createRenderEngine( engineName );

		final Scene scene = createScene();
		_scene = scene;

		final JPanel panel = new JPanel();
		panel.setLayout( new GridLayout( 1, 2 ) );

		final View3D normalView = engine.createView( scene );
		normalView.setFrontClipDistance( 1.0 );
		normalView.setBackClipDistance( 100.0 );
		normalView.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );

//		normalView.setProjectionPolicy( ProjectionPolicy.PARALLEL );
//		normalView.setZoomFactor( 0.1 );

//		normalView.setCameraControl( new FromToCameraControl( normalView, new Vector3D( 1.55, -19.73, 7.79 ), new Vector3D( 0.32, 0.33, 0.80 ), Vector3D.POSITIVE_Z_AXIS ) );
		normalView.setCameraControl( new FromToCameraControl( normalView, new Vector3D( 1.55, -19.73, 7.79 ), Vector3D.ZERO, Vector3D.POSITIVE_Z_AXIS ) );
		panel.add( normalView.getComponent() );
		_normalView = normalView;

		normalView.addViewListener( new ViewListener()
		{
			public void beforeFrame( final View3D view )
			{
				_frustumNode.update();
			}

			public void afterFrame( final View3D view )
			{
			}
		} );

		final View3D frustumView = engine.createView( scene );
		frustumView.appendRenderStyleFilter( new RenderStyleFilter()
		{
			public RenderStyle applyFilter( final RenderStyle style, final Object context )
			{
				RenderStyle result = style;
				if ( context instanceof Node3DPath )
				{
					final Node3DPath path = (Node3DPath)context;
					if ( path.getNode() instanceof Object3D )
					{
						final Object3D object = (Object3D)path.getNode();
						final Projector projector = normalView.getProjector();
						final Matrix3D scene2View = normalView.getScene2View();
						final Matrix3D object2Scene = path.getTransform();
						final boolean visible = projector.inViewVolume( object2Scene.multiply( scene2View ), object.getOrientedBoundingBox() );

						result = style.clone();
						result.setMaterialAlpha( visible ? 1.0f: 0.2f );
					}
				}
				return result;
			}
		} );
		frustumView.setViewFrustumCulling( false );
		frustumView.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
		frustumView.setCameraControl( new FromToCameraControl( frustumView, new Vector3D( 10.0, -20.0, 10.0 ), new Vector3D( 0.32, 0.33, 0.80 ), Vector3D.POSITIVE_Z_AXIS ) );
		panel.add( frustumView.getComponent() );

		return panel;
	}

	/**
	 * Create scene to show.
	 *
	 * @return  {@link Scene} to show.
	 */
	protected Scene createScene()
	{
		final Scene scene = new Scene( Scene.M );

		final Material backgroundMaterial = Materials.ALU_PLATE;
		final BoxUVMap backgroundMap = new BoxUVMap( Scene.CM );

		scene.addContentNode( "alu-plate", Matrix3D.getTranslation( -500.0, 10.0, -500.0 ), new Box3D( 1000.0, 0.1, 1000.0, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap ) );
		scene.addContentNode( "Hello world", Matrix3D.IDENTITY, HelloWorld.createHelloWorld3D() );
		addPrimitivesBelt( scene );

//		scene.addContentNode( "box", Matrix3D.IDENTITY, new Box3D( 1.0, 1.0, 1.0, null, Materials.WHITE ) );

		scene.setAmbient( 0.4f, 0.4f, 0.4f );

		final SpotLight3D spotLight1 = new SpotLight3D( Vector3D.normalize( -3.0, 5.0, -3.0 ), 10.0f );
		spotLight1.setIntensity( 3.0f );
		spotLight1.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight1.setConcentration( 1.0f );
		spotLight1.setCastingShadows( true );
		scene.addContentNode( "spotLight1", Matrix3D.getTranslation( 4.0, -5.0, 3.0 ), spotLight1 );

		final SpotLight3D spotLight2 = new SpotLight3D( Vector3D.normalize( 0.0, 1.0, 0.0 ), 35.0f );
		spotLight2.setIntensity( 1.0f );
		spotLight2.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight2.setConcentration( 1.0f );
		spotLight2.setCastingShadows( true );
		scene.addContentNode( "spotLight2", Matrix3D.getTranslation( 0.0, -10.0, 0.0 ), spotLight2 );

		final FrustumNode frustumNode = new FrustumNode();
		scene.addContentNode( "frustum", Matrix3D.IDENTITY, frustumNode );
		_frustumNode = frustumNode;

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
	 * @param   scene   Scene to add content to.
	 */
	private static void addPrimitivesBelt( final Scene scene )
	{
		final Transform3D box = new Transform3D( Matrix3D.getTranslation( -1.0, -1.0, -1.0 ), new Box3D( 2.0, 2.0, 2.0, Materials.RED, null, Materials.GREEN, null, Materials.BLUE, null, Materials.CYAN, null, Materials.MAGENTA, null, Materials.YELLOW, null ) );
		final Transform3D boxRotator = new Rotator( 0.0 );
		boxRotator.addChild( box );
		scene.addContentNode( "box", Matrix3D.IDENTITY, boxRotator );

		final Sphere3D sphere = new Sphere3D( 1.0, 13, 13, Materials.BLUE );
		final Transform3D sphereRotator = new Rotator( 60.0 );
		sphereRotator.addChild( sphere );
		scene.addContentNode( "sphere", Matrix3D.IDENTITY, sphereRotator );

		final Cylinder3D cylinder = new Cylinder3D( 2.0, 1.0, 15, Materials.GREEN, null, true, Materials.BLUE, null, Materials.RED, null, false );
		final Transform3D cylinderRotator = new Rotator( 120.0 );
		cylinderRotator.addChild( cylinder );
		scene.addContentNode( "cylinder", Matrix3D.IDENTITY, cylinderRotator );

		final Cone3D cone = new Cone3D( 2.0, 1.0, 0.0, 15, Materials.GREEN, null, true, Materials.BLUE, null, Materials.RED, null, false );
		final Transform3D coneRotator = new Rotator( 180.0 );
		coneRotator.addChild( cone );
		scene.addContentNode( "cone", Matrix3D.IDENTITY, coneRotator );

		final Shape shapeToExtrude = new Arc2D.Double( -1.0, -1.0, 2.0, 2.0, 80.0, 280.0, Arc2D.PIE);
		final Object3DBuilder builder = new Object3DBuilder();
		final Tessellator tessellatedShape = ShapeTools.createTessellator( shapeToExtrude, 0.025 );
		builder.addExtrudedShape( tessellatedShape, new Vector3D( 0.0, 0.0, 1.0 ), true, Matrix3D.IDENTITY, true, Materials.BLUE, null, false, true, Materials.RED, null, false, true, Materials.GREEN, null, false, false, false, false );
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
	 * @return  {@link Object3D} containing 'AB' mesh.
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
		freeShape.lineTo(  0.0, -1.0 );
		freeShape.lineTo( -0.4, 1.0 );
		freeShape.lineTo( -0.6, 1.0 );
		freeShape.closePath();

		// B
		freeShape.moveTo( 0.0, -1.0 );
		freeShape.lineTo(  0.3, -1.0 );
		freeShape.curveTo( 1.0, -1.0, 1.0, 0.0, 0.3, 0.0 );
		freeShape.curveTo( 1.0, 0.0, 1.0, 1.0, 0.3, 1.0 );
		freeShape.lineTo( 0.0, 1.0 );
		freeShape.closePath();

		final Object3DBuilder builder = new Object3DBuilder();
		final Tessellator freeShapeTessellator = ShapeTools.createTessellator( freeShape, 0.1 );
		builder.addExtrudedShape( freeShapeTessellator, new Vector3D( 0.0, 0.0, 0.5 ), true, Matrix3D.getTranslation( 0.0, 0.0, 0.8 ), true, Materials.BLUE, null, false, true, Materials.RED, null, false, true, Materials.GREEN, null, false, false, false, true );
		builder.addExtrudedShape( freeShapeTessellator, new Vector3D( 0.0, 0.0, 0.5 ), true, Matrix3D.getTransform( 0.0, 0.0, 90.0, 0.0, 0.0, 0.0 ), true, Materials.BLUE, null, false, true, Materials.RED, null, false, true, Materials.GREEN, null, false, false, false, true );
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
		 * @param   orbitStartAngle     Start azimuth angle on orbital sphere.
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
			final double time = (double) System.currentTimeMillis();
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

	private class FrustumNode
		extends Node3D
	{
		public void update()
		{
			final View3D view = _normalView;
			final Projector projector = view.getProjector();
			final Matrix4D projection = projector.getProjectionMatrix();
			final Matrix4D scene2projection = projection.multiply( view.getScene2View() );

			removeAllChildren();

			// Fast extraction of viewing frustum planes; see http://crazyjoke.free.fr/doc/3D/plane%20extraction.pdf
			final ViewingFrustum frustum = new ViewingFrustum( scene2projection );
			final Vector4D left = frustum.getLeftPlane();
			final Vector4D right = frustum.getRightPlane();
			final Vector4D bottom = frustum.getBottomPlane();
			final Vector4D top = frustum.getTopPlane();
			final Vector4D near = frustum.getNearPlane();
			final Vector4D far = frustum.getFarPlane();

			final Sphere3D smallSphere = new Sphere3D( 0.1, 6, 6, Materials.YELLOW );
			final Vector3D lbn = intersectPlanes( left, bottom, near );
			final Vector3D rbn = intersectPlanes( right, bottom, near );
			final Vector3D ltn = intersectPlanes( left, top, near );
			final Vector3D rtn = intersectPlanes( right, top, near );
			final Vector3D lbf = intersectPlanes( left, bottom, far );
			final Vector3D rbf = intersectPlanes( right, bottom, far );
			final Vector3D ltf = intersectPlanes( left, top, far );
			final Vector3D rtf = intersectPlanes( right, top, far );
			final Vector3D eye = intersectPlanes( left, right, bottom );

			addChild( new Transform3D( Matrix3D.IDENTITY.plus( lbn ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( rbn ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( ltn ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( rtn ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( lbf ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( rbf ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( ltf ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( rtf ), smallSphere ) );
			addChild( new Transform3D( Matrix3D.IDENTITY.plus( eye ), smallSphere ) );

//			addChild( createPlane( left, new Material( 0x80ff0000 ) ) );
//			addChild( createPlane( right, new Material( ~0x80ff0000 ) ) );
//			addChild( createPlane( bottom, new Material( 0x8000ff00 ) ) );
//			addChild( createPlane( top, new Material( ~0x8000ff00 ) ) );
//			addChild( createPlane( near, new Material( 0x800000ff ) ) );
//			addChild( createPlane( far, new Material( ~0x800000ff ) ) );

			final Material yellow = new Material( 0x80ffff00 );
			addChild( createQuad( lbn, lbf, ltf, ltn, yellow ) );
			addChild( createQuad( rbn, rbf, rtf, rtn, yellow ) );
			addChild( createQuad( lbn, rbn, rbf, lbf, yellow ) );
			addChild( createQuad( ltn, rtn, rtf, ltf, yellow ) );
		}

		/**
		 * Returns the single intersection point of three planes. If the planes
		 * do not have a single intersection point, the result is undefined.
		 *
		 * @param   p1  Coefficients of the first plane equation.
		 * @param   p2  Coefficients of the second plane equation.
		 * @param   p3  Coefficients of the third plane equation.
		 *
		 * @return  Intersection point.
		 */
		private Vector3D intersectPlanes( final Vector4D p1, final Vector4D p2, final Vector4D p3 )
		{
			final Vector3D c1 = Vector3D.cross( p2.x, p2.y, p2.z, p3.x, p3.y, p3.z );
			final Vector3D c2 = Vector3D.cross( p3.x, p3.y, p3.z, p1.x, p1.y, p1.z );
			final Vector3D c3 = Vector3D.cross( p1.x, p1.y, p1.z, p2.x, p2.y, p2.z );

			final double d = Vector3D.dot( p1.x, p1.y, p1.z, c1.x, c1.y, c1.z );
			final double d1 = -p1.w / d;
			final double d2 = -p2.w / d;
			final double d3 = -p3.w / d;

			final double x = Vector3D.dot( c1.x, c2.x, c3.x, d1, d2, d3 );
			final double y = Vector3D.dot( c1.y, c2.y, c3.y, d1, d2, d3 );
			final double z = Vector3D.dot( c1.z, c2.z, c3.z, d1, d2, d3 );

			return new Vector3D( x, y, z );
		}

		/**
		 * Creates geometry for (part of) a plane with the equation
		 * <var>a</var>x + <var>b</var>y + * <var>c</var>z + <var>d</var> = 0.0
		 *
		 * @param   plane       4D vector specifying the plane coefficients and
		 *                      plane distance, i.e. the vector [a, b, c, d].
		 * @param   material    Material to be used.
		 *
		 * @return  Rectangle on the specified plane.
		 */
		private Object3D createPlane( final Vector4D plane, final Material material )
		{
			final double normalLength = Vector3D.length( plane.x, plane.y, plane.z );
			final Vector3D normal = Vector3D.normalize( plane.x / normalLength, plane.y / normalLength, plane.z / normalLength );
			final Vector3D point = normal.multiply( -plane.w / normalLength );
			final Matrix3D planeTransform = Matrix3D.getPlaneTransform( point, normal, true );

			// Make the size of the plane independent of the plane. (Don't ask me how it works.)
			final double size = 1.0 / Math.sqrt( planeTransform.determinant() );

			final Vector3D p1 = planeTransform.transform( -size, -size, 0.0 );
			final Vector3D p2 = planeTransform.transform(  size, -size, 0.0 );
			final Vector3D p3 = planeTransform.transform(  size,  size, 0.0 );
			final Vector3D p4 = planeTransform.transform( -size,  size, 0.0 );

			final Object3DBuilder builder = new Object3DBuilder();
			builder.addQuad( p1, p2, p3, p4, material, true );
			return builder.getObject3D();
		}

		private Object3D createQuad( final Vector3D p1, final Vector3D p2, final Vector3D p3, final Vector3D p4, final Material material )
		{
			final Object3DBuilder builder = new Object3DBuilder();
			builder.addQuad( p1, p2, p3, p4, material, true );
			return builder.getObject3D();
		}
	}

}
