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
package ab.j3d.example;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * Renders a scene with several colored spheres, lit by two spot lights with
 * shadow casting enabled. Uses the {@link RenderEngine}.
 *
 * @author G. Meinders
 */
public class SpheresLightsAndShadows
extends ExampleApplet
{
	@Override
	protected RenderEngine createEngine()
	{
		final JOGLConfiguration configuration = new JOGLConfiguration();
		configuration.setPerPixelLightingEnabled( true );
		configuration.setShadowEnabled( true );
		configuration.setShadowMultisampleEnabled( true );
		return RenderEngineFactory.createJOGLEngine( new NullTextureLibrary(), configuration );
	}

	@Override
	protected Scene createScene()
	{
		final BasicAppearance white = new BasicAppearance();
		white.setDiffuseColor( Color4.WHITE );
		white.setAmbientColor( Color4.WHITE );
		white.setSpecularColor( Color4.DARK_GRAY );
		white.setShininess( 16 );

		final BasicAppearance color1 = new BasicAppearance();
		color1.setDiffuseColor( new Color4f( 0xffff6040 ) );
		color1.setAmbientColor( new Color4f( 0xffff6040 ) );
		color1.setSpecularColor( Color4.DARK_GRAY );
		color1.setShininess( 16 );

		final BasicAppearance color2 = new BasicAppearance();
		color2.setDiffuseColor( new Color4f( 0xff40c040 ) );
		color2.setAmbientColor( new Color4f( 0xff40c040 ) );
		color2.setSpecularColor( Color4.DARK_GRAY );
		color2.setShininess( 16 );

		final BasicAppearance color3 = new BasicAppearance();
		color3.setDiffuseColor( new Color4f( 0xff4060ff ) );
		color3.setAmbientColor( new Color4f( 0xff4060ff ) );
		color3.setSpecularColor( Color4.DARK_GRAY );
		color3.setShininess( 16 );

		final Scene scene = new Scene( Scene.MM );
		scene.setAmbient( 0.2f, 0.2f, 0.2f );

//		scene.addContentNode( "sphere-0", Matrix3D.getTranslation( 0.0, 0.0, 1.0 ), new Sphere3D( 1.0, 16, 8, white ) );
		scene.addContentNode( "sphere-1", Matrix3D.getTranslation( 0.0, 0.0, 1.0 ), new GeoSphere3D( 1.0, 3, color1 ) );
		scene.addContentNode( "sphere-2", Matrix3D.getTranslation( 2.0, 1.0, 1.0 ), new GeoSphere3D( 1.0, 3, color2 ) );
		scene.addContentNode( "sphere-3", Matrix3D.getTranslation( 1.0, 5.0, 1.0 ), new GeoSphere3D( 1.0, 3, color3 ) );

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addQuad( new Vector3D( -10.0, -10.0, 0.0 ),
		                 new Vector3D( -10.0, 10.0, 0.0 ),
		                 new Vector3D( 10.0, 10.0, 0.0 ),
		                 new Vector3D( 10.0, -10.0, 0.0 ), white, false );
		final Object3D floor = builder.getObject3D();

		scene.addContentNode( "floor", null, floor );

		final SpotLight3D light1 = new SpotLight3D( Vector3D.normalize( 0.5, 1.0, -1.0 ), 20.0f );
		light1.setIntensity( 30.0f, 28.0f, 16.0f );
		// light1.setAttenuation( 0.0f, 0.0f, 1.0f ); (the default)
		light1.setCastingShadows( true );
		light1.setConcentration( 1.5f );
		scene.addContentNode( "light-1", Matrix3D.getTranslation( -2.5, -5.0, 6.0 ), light1 );

		final SpotLight3D light2 = new SpotLight3D( Vector3D.normalize( -0.5, 1.0, -1.0 ), 20.0f );
		light2.setIntensity( 16.0f, 18.0f, 20.0f );
//		light2.setAttenuation( 0.0f, 0.0f, 1.0f ); (the default)
		light2.setCastingShadows( true );
		light2.setConcentration( 2.0f );
		scene.addContentNode( "light-2", Matrix3D.getTranslation( 3.5, -6.0, 6.0 ), light2 );
		return scene;
	}

	@Override
	protected void configureView( final View3D view )
	{
//		_view.setCameraControl( new FromToCameraControl( 4.0, -8.0, 6.0 ) );

		view.setFrontClipDistance( 0.01 );
		view.setBackClipDistance( 50.0 );

		view.setRenderingPolicy( RenderingPolicy.SOLID );
		view.setBackground( Background.createSolid( new Color4f( 0.2f, 0.2f, 0.2f ) ) );
	}

	@Override
	protected boolean animate( final Scene scene, final View3D view )
	{
		final double alpha = (double)( System.currentTimeMillis() % 10000L ) / 10000.0;

		final double angle = 2.0 * Math.PI * alpha;
		final double cos = Math.cos( angle );
		final double sin = Math.sin( angle );
		final double z = 4.0 - 2.0 * sin;
		final double distance = 10.0 + sin;

		final Vector3D to = new Vector3D( 0.0, 2.0, 0.0 );
		final Vector3D from = to.plus( cos * distance, sin * distance, z );
		view.setScene2View( Matrix3D.getFromToTransform( from, to, Vector3D.POSITIVE_Z_AXIS, Vector3D.POSITIVE_Y_AXIS ) );

		return true;
	}
}
