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
package ab.j3d.demo;

import java.awt.*;

import ab.j3d.*;
import ab.j3d.awt.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * A 3D version of the 'Hello world' program.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class HelloWorld
{
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

		final Vector3D lookFrom = new Vector3D( 6.45, -8.21, 1.89 );
		final Vector3D lookAt = new Vector3D( 0.21, 1.20, -0.59 );

		final View3D view = engine.createView( scene );
		view.setBackground( Background.createSolid( new Color4f( 0.0f, 0.0f, 0.0f ) ) );
		view.setCameraControl( new FromToCameraControl( view, lookFrom, lookAt, Vector3D.POSITIVE_Z_AXIS ) );
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
		scene.addContentNode( "Hello world", Matrix3D.IDENTITY, createHelloWorld3D() );

		scene.setAmbient( 0.3f, 0.3f, 0.3f );

		final SpotLight3D spotLight = new SpotLight3D( Vector3D.normalize( -10.0, 6.0, -2.0 ), 6.0f );
		spotLight.setIntensity( 3.0f );
		spotLight.setAttenuation( 1.0f, 0.0f, 0.0f );
		spotLight.setConcentration( 1.0f );
		spotLight.setCastingShadows( true );
		scene.addContentNode( "spotLight", Matrix3D.getTranslation( 11.0, -6.0, 2.0 ), spotLight );

		final DirectionalLight3D directionalLight = new DirectionalLight3D( Vector3D.normalize( 1.0, 1.0, 1.0 ), 1.0f );
		scene.addContentNode( "directionalLight", Matrix3D.IDENTITY, directionalLight );

		return scene;
	}

	/**
	 * Create 'Hello world' in 3D.
	 *
	 * @return  3D text object.
	 */
	public static Object3D createHelloWorld3D()
	{
		final String text = "Hello world";
		final Font font = new Font( "serif", Font.PLAIN, 2 );
		final Object3DBuilder builder = new Object3DBuilder();
		final BoxUVMap uvMap = new BoxUVMap( Scene.M );
		ShapeTools.addText( builder, Matrix3D.getTransform( -90.0, 0.0, 0.0, 0.0, 0.0, 0.0 ), text, font, 1.0, 0.5, 0.5, 0.5, 0.5, 0.025, Materials.CHROME, uvMap, Materials.CHROME, uvMap, Materials.GOLD, uvMap );
		return builder.getObject3D();
	}

}
