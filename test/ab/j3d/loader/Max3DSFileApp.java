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
package ab.j3d.loader;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.awt.view.*;
import ab.j3d.awt.view.jogl.*;
import ab.j3d.control.*;
import ab.j3d.loader.max3ds.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * This is a sample application for the {@link Max3DSFile} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Max3DSFileApp
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
			final double   unit      = Scene.MM;

			final Matrix3D transform = Matrix3D.IDENTITY; // .rotateX( Math.toRadians( 90.0 ) );

//			final String   path      = "fishtank/fishtank.3ds";
			final String   path      = "flower01_s/flower01.3ds";
//			final String   path      = "vtr/vtr.3ds";
//			final String   path      = "man1/man1.3ds"; // standing straight
//			final String   path      = "man2/man2.3ds"; // sitting + lifted arms
//			final String   path      = "man3/man3.3ds"; // on knees + reaching
//			final String   path      = "man4/man4.3ds"; // walking straight
//			final String   path      = "man5/man5.3ds"; // kneeling + reaching
//			final String   path      = "man6/man6.3ds"; // bending  + grasping
//			final String   path      = "man7/man7.3ds"; // kneeling + accepting
//			final String   path      = "man8/man8.3ds"; // stair walking + reaching
//			final String   path      = "woman1/woman1.3ds"; // standing straight
//			final String   path      = "woman2/woman2.3ds"; // sitting + lifted arms
//			final String   path      = "woman3/woman3.3ds"; // on knees + reaching
//			final String   path      = "woman4/woman4.3ds"; // walking straight
//			final String   path      = "woman5/woman5.3ds"; // kneeling + reaching
//			final String   path      = "woman6/woman6.3ds"; // bending  + grasping
//			final String   path      = "woman7/woman7.3ds"; // kneeling + accepting
//			final String   path      = "woman8/woman8.3ds"; // stair walking + reaching
//			final String   path      = "woman9/woman1.3ds"; // full nudity
//			final String   path      = "tricycle/trecic3.3ds";


			final Max3DSFile maxFile = new Max3DSFile( new FileInputStream( path ) );

			final Bounds3D bounds   = Bounds3D.EMPTY; // object3d.getBounds( null , null );
			final Vector3D size     = bounds.size();
			final double   toCM     = 100.0 * unit;

			final Vector3D viewFrom = new Vector3D( 0.0 , bounds.v1.y - 3.0 / unit , bounds.v2.z / 2.0 + 1.2 / unit );
			final Vector3D viewAt   = new Vector3D( 0.0 , 0.0 , bounds.v2.z / 2.0 );

			System.out.println( "object size = " + Math.round( toCM * size.x ) + " x " + Math.round( toCM * size.y ) + " x " + Math.round( toCM * size.z ) + " cm" );

			final Scene scene = new Scene( unit );
			Scene.addLegacyLights( scene );
			maxFile.addMeshesToScene( scene );

			final RenderEngine renderEngine = new JOGLEngine(); // new Color( 51 , 77 , 102 ) );

			final View3D view = renderEngine.createView( scene );
			view.setCameraControl( new FromToCameraControl( view , viewFrom , viewAt ) );

			final JPanel viewPanel = new JPanel( new BorderLayout() );
			viewPanel.add( view.getComponent() , BorderLayout.CENTER );
			viewPanel.add( View3DPanel.createToolBar( view, new Locale( "nl" ) ) , BorderLayout.SOUTH );

			final JFrame frame = new JFrame( renderEngine.getClass() + " example" );
			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			frame.setContentPane( viewPanel );
			final Toolkit toolkit = frame.getToolkit();
			final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
			final Rectangle screenBounds = graphicsConfiguration.getBounds();
			final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
			frame.setSize( 800, 800 );
			frame.setLocation( screenBounds.x + ( screenBounds.width + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2,
			                   screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );

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
	private Max3DSFileApp()
	{
	}
}