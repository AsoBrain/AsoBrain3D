/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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

import java.awt.BorderLayout;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ab.j3d.Vector3D;
import ab.j3d.control.FromToCameraControl;
import ab.j3d.view.ViewModelTools;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.java3d.Java3dModel;

import com.numdata.oss.ui.ImageTools;
import com.numdata.oss.ui.WindowTools;

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
	 * {@link Java3dModel} is used here.
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
		 * Create Java3D-model.
		 */
		final Java3dModel viewModel = new Java3dModel();
		ViewModelTools.addLegacyLights( viewModel );

		/*
		 * Fill the Java3D-model with objects from the testmodel.
		 */
		viewModel.createNode( "camera"    , null , testModel.getCamera3D()           , null , 1.0f );
		viewModel.createNode( "redbox"    , null , testModel.getRedXRotatedBox3D()   , null , 1.0f );
		viewModel.createNode( "greenbox"  , null , testModel.getGreenYRotatedBox3D() , null , 1.0f );
		viewModel.createNode( "bluebox"   , null , testModel.getBlueZRotatedBox3D()  , null , 1.0f );
		viewModel.createNode( "panel"     , null , testModel.getTexturedBox3D()      , null , 1.0f );
		viewModel.createNode( "sphere"    , null , testModel.getSphere3D()           , null , 1.0f );
		viewModel.createNode( "cylinder"  , null , testModel.getCylinder3D()         , null , 1.0f );
		viewModel.createNode( "cone"      , null , testModel.getCone3D()             , null , 1.0f );
		viewModel.createNode( "extruded"  , null , testModel.getExtrudedObject2D()   , null , 1.0f );
		viewModel.createNode( "colorcube" , null , testModel.getColorCube()          , null , 1.0f );

		/*
		 * Create and display view.
		 */
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		final ViewModelView view = viewModel.createView( "view" );
		view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent() , BorderLayout.CENTER );
		viewPanel.add( view.createToolBar( Locale.ENGLISH ) , BorderLayout.SOUTH );

		final JFrame frame = WindowTools.createFrame( "Testscene" , 800 , 600 , viewPanel );

		frame.setVisible( true );
	}
}
