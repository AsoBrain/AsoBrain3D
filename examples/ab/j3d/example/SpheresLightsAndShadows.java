/* $Id$
 *
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
 */
package ab.j3d.example;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;
import org.jetbrains.annotations.*;

/**
 * Renders a scene with several colored spheres, lit by two spot lights with
 * shadow casting enabled. Uses the {@link JOGLEngine}.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class SpheresLightsAndShadows
	extends ExampleApplet
{
	@NotNull
	@Override
	protected JOGLEngine createEngine()
	{
		final JOGLConfiguration configuration = new JOGLConfiguration();
		configuration.setPerPixelLightingEnabled( true );
		configuration.setShadowEnabled( true );
		configuration.setShadowMultisampleEnabled( true );
		return new JOGLEngine( configuration );
	}

	@NotNull
	@Override
	protected Scene createScene()
	{
		final Material white = new Material();
		white.setSpecularColor( 0x404040 );

		final Material color1 = new Material();
		color1.setDiffuseColor( 0xffff6040 );
		color1.setSpecularColor( 0x404040 );

		final Material color2 = new Material();
		color2.setDiffuseColor( 0xff40c040 );
		color2.setSpecularColor( 0x404040 );

		final Material color3 = new Material();
		color3.setDiffuseColor( 0xff4060ff );
		color3.setSpecularColor( 0x404040 );

		final Scene scene = new Scene( Scene.MM );
		scene.setAmbient( 0.2f, 0.2f, 0.2f );

//		scene.addContentNode( "sphere-0", Matrix3D.getTranslation( 0.0, 0.0, 1.0 ), new Sphere3D( 1.0, 16, 8, white ) );
		scene.addContentNode( "sphere-1", Matrix3D.getTranslation( 0.0, 0.0, 1.0 ), new GeoSphere3D( 1.0, 1, color1 ) );
		scene.addContentNode( "sphere-2", Matrix3D.getTranslation( 2.0, 1.0, 1.0 ), new GeoSphere3D( 1.0, 1, color2 ) );
		scene.addContentNode( "sphere-3", Matrix3D.getTranslation( 1.0, 5.0, 1.0 ), new GeoSphere3D( 1.0, 1, color3 ) );

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addQuad( new Vector3D( -10.0, -10.0, 0.0 ),
		                 new Vector3D( -10.0, 10.0, 0.0 ),
		                 new Vector3D( 10.0, 10.0, 0.0 ),
		                 new Vector3D( 10.0, -10.0, 0.0 ), white, false );
		final Object3D floor = builder.getObject3D();

		scene.addContentNode( "floor", null, floor );

		final SpotLight3D light1 = new SpotLight3D( Vector3D.normalize( 0.5, 1.0, -1.0 ), 20.0f  );
		light1.setIntensity( 30.0f, 28.0f, 16.0f );
		// light1.setAttenuation( 0.0f, 0.0f, 1.0f ); (the default)
		light1.setCastingShadows( true );
		light1.setConcentration( 1.5f );
		scene.addContentNode( "light-1", Matrix3D.getTranslation( -2.5, -5.0, 6.0 ), light1 );

		final SpotLight3D light2 = new SpotLight3D( Vector3D.normalize( -0.5, 1.0, -1.0 ), 20.0f  );
		light2.setIntensity( 16.0f, 18.0f, 20.0f );
//		light2.setAttenuation( 0.0f, 0.0f, 1.0f ); (the default)
		light2.setCastingShadows( true );
		light2.setConcentration( 2.0f );
		scene.addContentNode( "light-2", Matrix3D.getTranslation( 3.5, -6.0, 6.0 ), light2 );
		return scene;
	}

	@Override
	protected void configureView( @NotNull final View3D view )
	{
//		_view.setCameraControl( new FromToCameraControl( 4.0, -8.0, 6.0 ) );

		view.setFrontClipDistance( 0.01 );
		view.setBackClipDistance( 50.0 );

		view.setRenderingPolicy( RenderingPolicy.SOLID );
		view.setBackground( Background.createSolid( new Color( 0.2f, 0.2f, 0.2f ) ) );
	}

	@Override
	protected boolean animate( @NotNull final Scene scene, @NotNull final View3D view )
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
