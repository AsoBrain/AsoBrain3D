/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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
package ab.j3d.loader;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;
import com.numdata.oss.ui.*;

/**
 * This is a sample application for the {@link Max3DSLoader} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Max3DSLoaderApp
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		try
		{
//			final double   unit      = Scene.FOOT;
//			final double   unit      = Scene.INCH;
			final double   unit      = Scene.METER;

			final Matrix3D transform = Matrix3D.INIT; // .rotateX( Math.toRadians( 90.0 ) );

			final String   path      = "/numdata/3d/dosch/furniture/3ds/Kitchen/kitchen01.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/toilet2/Wc.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/fishtank/fishtank.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/flower01_s/flower01.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/vtr/vtr.3ds";
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man1/man1.3ds"; // standing straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man2/man2.3ds"; // sitting + lifted arms
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man3/man3.3ds"; // on knees + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man4/man4.3ds"; // walking straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man5/man5.3ds"; // kneeling + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man6/man6.3ds"; // bending  + grasping
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man7/man7.3ds"; // kneeling + accepting
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/man8/man8.3ds"; // stair walking + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman1/woman1.3ds"; // standing straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman2/woman2.3ds"; // sitting + lifted arms
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman3/woman3.3ds"; // on knees + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman4/woman4.3ds"; // walking straight
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman5/woman5.3ds"; // kneeling + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman6/woman6.3ds"; // bending  + grasping
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman7/woman7.3ds"; // kneeling + accepting
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman8/woman8.3ds"; // stair walking + reaching
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/woman9/woman1.3ds"; // full nudity
//			final String   path      = "/numdata/3d/3ds-from-web/3dcafe/tricycle/trecic3.3ds";

			final File sourceFile = new File( path );

			final Node3D root = Max3DSLoader.load( transform, sourceFile );
			/*
			final Ab3dsFile ab3dsFile = new Ab3dsFile();
			ab3dsFile.load( sourceFile );
			final Node3D root = ab3dsFile.createModel( sourceFile.getParentFile() );
			*/

			final Bounds3D bounds   = root.calculateBounds( Matrix3D.IDENTITY );
			final Vector3D size     = bounds.size();
			final double   toCM     = unit / Scene.CM;

			final Vector3D viewFrom = Vector3D.INIT.set( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
			final Vector3D viewTo   = bounds.center();

			System.out.println( "object size = " + Math.round( toCM * size.x ) + " x " + Math.round( toCM * size.y ) + " x " + Math.round( toCM * size.z ) + " cm" );

			final Scene scene = new Scene( unit );
			Scene.addLegacyLights( scene );
			scene.addContentNode( "obj" , Matrix3D.INIT.plus( 0.0 , 0.0 , -bounds.v1.z ) , root );
			scene.setAnimated( true );

			final RenderEngine renderEngine = new JOGLEngine(); // new Color( 51 , 77 , 102 ) );

			final JOGLView view = (JOGLView)renderEngine.createView( scene );
			view.addStatisticsOverlay();
			view.setCameraControl( new FromToCameraControl( view , viewFrom, viewTo ) );
			view.setBackClipDistance( size.length() * 2.0 );
			view.setFrontClipDistance( size.length() / 10000.0 );

			final JPanel viewPanel = new JPanel( new BorderLayout() );
			viewPanel.add( view.getComponent() , BorderLayout.CENTER );
			viewPanel.add( view.createToolBar( new Locale( "nl" ) ) , BorderLayout.SOUTH );

			final JFrame frame = WindowTools.createFrame( renderEngine.getClass() + " example" , 800 , 600 , viewPanel );
			frame.setVisible( true );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Max3DSLoaderApp()
	{
	}
}
