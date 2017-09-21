/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.demo;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import org.jetbrains.annotations.*;

/**
 * This shows the 'www.ASOBRAiN.com' animation.
 *
 * @author  Peter S. Heijnen
 */
public class AsoBrainAnimation
{
	/**
	 * The scene.
	 */
	protected Scene _scene;

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

		final View3D view = engine.createView( scene );
		view.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
		view.setCameraControl( new FromToCameraControl( view, new Vector3D( 0.0, 0.0, 8.0 ), Vector3D.ZERO, Vector3D.POSITIVE_Y_AXIS ) );
		return view.getComponent();
	}

	/**
	 * Create scene to show.
	 *
	 * @return  {@link Scene} to show.
	 */
	protected Scene createScene()
	{
		final Scene scene = new Scene( Scene.M );

		final BasicAppearance backgroundMaterial = new BasicAppearance( "alu-plate" );
		backgroundMaterial.setAmbientColor( new Color4f( 0.31f, 0.31f, 0.31f ) );
		backgroundMaterial.setDiffuseColor( new Color4f( 0.41f, 0.41f, 0.41f ) );
		backgroundMaterial.setSpecularColor( new Color4f( 0.51f, 0.51f, 0.51f ) );
		backgroundMaterial.setShininess( 16 );
		backgroundMaterial.setColorMap( new BasicTextureMap( "maps/alu-plate.jpg", 0.1f, 0.1f ) );

		final BoxUVMap backgroundMap = new BoxUVMap( 0.04 );
		final Rotator plateRotator = new Rotator( Matrix3D.getTranslation( 10.0, 0.0, 0.0 ), new Vector3D( 500.0, 500.0, 0.0 ), 0.0, -1.0 / 50.0 );
		plateRotator.addChild( new Box3D( 1000.0, 1000.0, 0.1, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap, backgroundMaterial, backgroundMap ) );
		scene.addContentNode( "backplate", Matrix3D.getTranslation( -500.0, -500.0, -0.2 ), plateRotator );

		scene.setAmbient( 0.2f, 0.2f, 0.2f );

		final SpotLight3D yellowLight = new SpotLight3D( Vector3D.normalize( 0.0, 0.0, -1.0 ), 45.0f );
		yellowLight.setIntensity( 0.8f, 0.8f, 0.6f );
		yellowLight.setAttenuation( 1.0f, 0.0f, 0.0f );
		yellowLight.setConcentration( 2.0f );
		yellowLight.setCastingShadows( true );
		final Rotator spot1Rotator = new Rotator( Matrix3D.getTranslation( 5.0, 0.0, 10.0 ), new Vector3D( -5.0, 0.0, 0.0 ), 0.0, 1.0 / 11.0 );
		spot1Rotator.addChild( yellowLight );
		scene.addContentNode( "yellowLight", Matrix3D.IDENTITY, spot1Rotator );

		final SpotLight3D blueLight = new SpotLight3D( Vector3D.normalize( 0.0, 0.0, -1.0 ), 45.0f );
		blueLight.setIntensity( 0.6f, 0.7f, 0.8f );
		blueLight.setAttenuation( 1.0f, 0.0f, 0.0f );
		blueLight.setConcentration( 2.0f );
		blueLight.setCastingShadows( true );
		final Rotator spot2Rotator = new Rotator( Matrix3D.getTranslation( 5.0, 0.0, 10.0 ), new Vector3D( -5.0, 0.0, 0.0 ), 180.0, 1.0 / 11.0 );
		spot2Rotator.addChild( blueLight );
		scene.addContentNode( "blueLight", Matrix3D.IDENTITY, spot2Rotator );

		final Alpha flashIntensity = new Alpha();
		flashIntensity.setStartAt( System.currentTimeMillis() + 5000L );
		flashIntensity.setLength( 3000L );
		flashIntensity.setRepeatDelay( 10000L );
		flashIntensity.setConstant( 1.0 );
		flashIntensity.setLinearFactor( -1.0 );

		final SpotLight3D flashLight = new AlphaIntensitySpotLight3D( Vector3D.normalize( 0.0, 0.0, -1.0 ), 45.0f, flashIntensity );
		flashLight.setDiffuse( 3.0f, 3.0f, 3.0f );
		flashLight.setSpecular( 1.0f, 1.0f, 1.0f );
		flashLight.setAttenuation( 0.0f, 0.01f, 0.01f );
		flashLight.setConcentration( 2.0f );
		scene.addContentNode( "flashLight", Matrix3D.getTranslation( 0.0, 0.0, 10.0 ), flashLight );

		final Font font1 = new Font( "sansserif", Font.PLAIN, 1 );
		final BasicAppearance wwwComColor = new BasicAppearance( "blue" );
		wwwComColor.setAmbientColor( new Color4f( 0.5f, 0.5f, 1.0f ) );
		wwwComColor.setDiffuseColor( new Color4f( 0.5f, 0.5f, 1.0f ) );
		wwwComColor.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		wwwComColor.setShininess( 16 );

		final BasicAppearance asoBrainColor = new BasicAppearance( "white" );
		asoBrainColor.setAmbientColor( new Color4f( 0.9f, 1.0f, 1.0f ) );
		asoBrainColor.setDiffuseColor( new Color4f( 0.9f, 1.0f, 1.0f ) );
		asoBrainColor.setSpecularColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		asoBrainColor.setShininess( 16 );

		final Object3DBuilder wwwBuilder = new Object3DBuilder();
		ShapeTools.addText( wwwBuilder, Matrix3D.IDENTITY, "www.", font1, 0.4, 1.0, 0.5, 0.0, 0.05, 0.01, wwwComColor, null, wwwComColor, null, wwwComColor, null );
		scene.addContentNode( "www.", Matrix3D.getTranslation( -1.7, 0.0, 1.0 ), wwwBuilder.getObject3D() );

		final Object3DBuilder asoBuilder = new Object3DBuilder();
		ShapeTools.addText( asoBuilder, Matrix3D.IDENTITY, "ASO", font1, 0.7, 1.0, 0.5, 0.0, 0.1, 0.01, asoBrainColor, null, asoBrainColor, null, asoBrainColor, null );
		scene.addContentNode( "ASO", Matrix3D.getTranslation( -0.3, -0.1, 1.0 ), asoBuilder.getObject3D() );

		final Object3DBuilder brainBuilder = new Object3DBuilder();
		ShapeTools.addText( brainBuilder, Matrix3D.IDENTITY, "BRAiN", font1, 0.7, 0.0, 0.5, 0.0, 0.1, 0.01, asoBrainColor, null, asoBrainColor, null, asoBrainColor, null );
		scene.addContentNode( "BRAiN", Matrix3D.getTranslation( -0.25, 0.1, 1.0 ), brainBuilder.getObject3D() );

		final Object3DBuilder comBuilder = new Object3DBuilder();
		ShapeTools.addText( comBuilder, Matrix3D.IDENTITY, ".com", font1, 0.4, 0.0, 0.5, 0.0, 0.05, 0.01, wwwComColor, null, wwwComColor, null, wwwComColor, null );
		scene.addContentNode( ".com", Matrix3D.getTranslation( 1.8, 0.0, 1.0 ), comBuilder.getObject3D() );

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
	 * This rotates an object around itself and along a big circle.
	 */
	private static class Rotator
		extends Transform3D
	{
		/**
		 * Rotation speed in degrees per second.
		 */
		private final double _rotationSpeed;

		/**
		 * Rotation start angle.
		 */
		private final double _startAngle;

		/**
		 * Rotation pivot point.
		 */
		private final Vector3D _pivot;

		/**
		 * Construct rotator.
		 * @param   base            Base transform of {@link Transform3D}.
		 * @param   pivot           Rotation pivot point.
		 * @param   startAngle      Rotation start angle.
		 * @param   rotationSpeed   Rotation speed in degrees per second.
		 */
		private Rotator( final Matrix3D base, final Vector3D pivot, final double startAngle, final double rotationSpeed )
		{
			super( base );
			_rotationSpeed = rotationSpeed;
			_startAngle = startAngle;
			_pivot = pivot;
		}

		@NotNull
		@Override
		public Matrix3D getTransform()
		{
			final double time = (double) System.currentTimeMillis();
			final double orbitAngleZ = _startAngle + time * _rotationSpeed;

			final Matrix3D rotationTransform = Matrix3D.getRotationTransform( _pivot, Vector3D.POSITIVE_Z_AXIS, Math.toRadians( orbitAngleZ ) );
			return rotationTransform.multiply( super.getTransform() );
		}
	}

	/**
	 * This {@link SpotLight3D} uses an {@link Alpha} instance to determine its
	 * relative intensity.
	 */
	private static class AlphaIntensitySpotLight3D
		extends SpotLight3D
	{
		/**
		 * {@link Alpha} used for light intensity.
		 */
		Alpha _intensityAlpha;


		/**
		 * Construct spot light.
		 *
		 * @param   direction       Direction that the spot light points in.
		 * @param   spreadAngle     Spread angle (see super for more info).
		 * @param   intensityAlpha  {@link Alpha} used for light intensity.
		 */
		private AlphaIntensitySpotLight3D( final Vector3D direction, final float spreadAngle, final Alpha intensityAlpha )
		{
			super( direction, spreadAngle );
			_intensityAlpha = intensityAlpha;
		}

		/**
		 * Get alpha to use for light intensity.
		 *
		 * @return  Alpha to use for light intensity.
		 */
		public Alpha getIntensityAlpha()
		{
			return _intensityAlpha;
		}

		/**
		 * Set alpha to use for light intensity.
		 *
		 * @param   alpha   Alpha to use for light intensity.
		 */
		public void setIntensityAlpha( final Alpha alpha )
		{
			_intensityAlpha = alpha;
		}

		/**
		 * Get current intensity from {@link Alpha}.
		 *
		 * @return  Current intensity from {@link Alpha}.
		 */
		protected float getAlphaIntensity()
		{
			final Alpha intensityAlpha = getIntensityAlpha();
			return (float) intensityAlpha.get();
		}

		@Override
		public float getDiffuseRed()
		{
			return super.getDiffuseRed() * getAlphaIntensity();
		}

		@Override
		public float getDiffuseGreen()
		{
			return super.getDiffuseGreen() * getAlphaIntensity();
		}

		@Override
		public float getDiffuseBlue()
		{
			return super.getDiffuseBlue() * getAlphaIntensity();
		}

		@Override
		public float getSpecularRed()
		{
			return super.getSpecularRed() * getAlphaIntensity();
		}

		@Override
		public float getSpecularGreen()
		{
			return super.getSpecularGreen() * getAlphaIntensity();
		}

		@Override
		public float getSpecularBlue()
		{
			return super.getSpecularBlue() * getAlphaIntensity();
		}
	}
}
