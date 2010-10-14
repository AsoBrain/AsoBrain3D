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
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;
import com.numdata.oss.ui.*;

/**
 * This test application can be used for visual testing of the
 * {@link AbToPovConverter}. The application uses the same objects as
 * {@link TestAbToPovConverter}. These objects are defined by the
 * {@link AbPovTestModel}.
 *
 * @see     AbPovTestModel
 * @see     TestAbToPovConverter
 * @see     AbPovTestModel
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
final class AbPovTestApp
{
	/**
	 * No need for objects of this class.
	 */
	private AbPovTestApp() {}

	/**
	 * An object of type {@link AbPovTestModel} is constructed, the objects are
	 * retrieved and a view is created and added to a frame. The
	 * {@link JOGLEngine} is used here.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		/*
		 * Path to test textures.
		 */
		ImageTools.addToSearchPath( "AsoBrain3D/test/ab/j3d/pov" );

		/*
		 * Create testmodel.
		 */
		final AbPovTestModel testModel = new AbPovTestModel();

		/*
		 * Create scene.
		 *
		 * Fill the scene with objects from the testmodel.
		 */
		final Scene scene = new Scene( Scene.MM );
		Scene.addLegacyLights( scene );
		scene.addContentNode( "redbox"    , Matrix3D.getTransform( 10.0 ,  0.0 ,  0.0 , -200.0 ,   0.0 , -250.0 ) , testModel.getRedXRotatedBox3D() );
		scene.addContentNode( "greenbox"  , Matrix3D.getTransform(  0.0 , 10.0 ,  0.0 ,  -50.0 ,   0.0 , -250.0 ) , testModel.getGreenYRotatedBox3D() );
		scene.addContentNode( "bluebox"   , Matrix3D.getTransform(  0.0 ,  0.0 , 10.0 ,  200.0 ,   0.0 , -250.0 ) , testModel.getBlueZRotatedBox3D() );
		scene.addContentNode( "panel"     , Matrix3D.getTransform(  0.0 ,  0.0 , 45.0 , -350.0 ,   0.0 ,    0.0 ) , testModel.getTexturedBox3D() );
		scene.addContentNode( "sphere"    , Matrix3D.getTransform(  0.0 ,  0.0 ,  0.0 ,    0.0 , 300.0 , -200.0 ) , testModel.getSphere3D() );
		scene.addContentNode( "cylinder"  , Matrix3D.getTransform(  0.0 ,  0.0 ,  0.0 ,    0.0 ,   0.0 ,  150.0 ) , testModel.getCylinder3D() );
		scene.addContentNode( "cone"      , Matrix3D.getTransform( 45.0 ,  0.0 ,  0.0 ,  250.0 ,   0.0 ,    0.0 ) , testModel.getCone3D() );
		scene.addContentNode( "extruded"  , Matrix3D.INIT , testModel.getExtrudedObject2D() );
		scene.addContentNode( "colorcube" , Matrix3D.INIT , testModel.getColorCube() );

		/*
		 * Create Java3D-engine.
		 */
		final RenderEngine renderEngine = new JOGLEngine();

		/*
		 * Create and display view.
		 */
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		final View3D view = renderEngine.createView( scene );
		view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent() , BorderLayout.CENTER );
		viewPanel.add( view.createToolBar( Locale.ENGLISH ) , BorderLayout.SOUTH );

		final JFrame frame = WindowTools.createFrame( "Testscene" , 800 , 600 , viewPanel );
		frame.setVisible( true );
	}
}
