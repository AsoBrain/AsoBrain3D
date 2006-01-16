/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import java.util.Locale;
import javax.swing.JFrame;
import ab.j3d.view.FromToViewControl;
import ab.j3d.view.java3d.Java3dModel;
import ab.j3d.Vector3D;
import com.numdata.oss.ui.WindowTools;
import com.numdata.oss.ui.ImageTools;

/**
 * This test application can be used for visual testing of the AbToPovConverter. The application uses the same objects
 * as the test. These objects are defined by AbPovTestModel.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 * @see     AbPovTestModel
 * @see     TestAbToPovConverter
 */
public class AbPovTestApp
{
	private AbPovTestApp() {}

	/**
	 * An object of type AbPovTestModel is constructed, the objects retrieved and
	 * a view is created and added to a frame. The Java3dModel is used here.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		// Path to test textures.
		ImageTools.addToSearchPath( "AsoBrain3D/test/ab/j3d/pov" );

		// Get testmodel
		final AbPovTestModel testModel = new AbPovTestModel();

		// Create 3dmodel
		final Java3dModel viewModel = new Java3dModel();

		// Fill model with objects from the testmodel.
		viewModel.createNode( "camera"      , null               , testModel.getCamera3D()           , null , 1.0f );
		viewModel.createNode( "redbox"      , null               , testModel.getRedXRotatedBox3D()   , null , 1.0f );
		viewModel.createNode( "greenbox"    , null               , testModel.getGreenYRotatedBox3D() , null , 1.0f );
		viewModel.createNode( "bluebox"     , null               , testModel.getBlueZRotatedBox3D()  , null , 1.0f );
		viewModel.createNode( "panel"       , null               , testModel.getTexturedBox3D()      , null , 1.0f );
		viewModel.createNode( "sphere"      , null               , testModel.getSphere3D()           , null , 1.0f );
		viewModel.createNode( "cylinder"    , null               , testModel.getCylinder3D()         , null , 1.0f );
		viewModel.createNode( "cone"        , null               , testModel.getCone3D()             , null , 1.0f );
		viewModel.createNode( "extruded"    , null               , testModel.getExtrudedObject2D()   , null , 1.0f );
		viewModel.createNode( "colorcube"   , null               , testModel.getColorCube()          , null , 1.0f );

		// Create and display view
		final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , -1000.0 , 0.0 );
		final Vector3D viewAt   = Vector3D.INIT;

		viewModel.createView( "view" , new FromToViewControl( viewFrom , viewAt ) );

		final JFrame frame = WindowTools.createFrame( "Testscene" , 800 , 600 , viewModel.createViewPanel( Locale.ENGLISH , "view" ) );

		frame.setVisible( true );
	}
}
