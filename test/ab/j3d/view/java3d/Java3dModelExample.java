/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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
package ab.j3d.view.java3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Matrix3D;
import ab.j3d.TextureLibrary;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Object3D;

/**
 * Example problem for Java 3D view model implementation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Java3dModelExample
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = Java3dModelExample.class.getName();

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Java3dModelExample()
	{
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final TextureSpec texture = new TextureSpec();
//		texture.code         = "catan-water";
//		texture.textureScale = 1;
//		texture.grain        = true;
		texture.rgb = Color.green.getRGB();

		final TextureLibrary tl = new TextureLibrary()
		{
			public TextureSpec getTextureSpec( final String code )
			{
				return texture;
			}
		};

		final Java3dTools j3dTools = new Java3dTools( tl );
		final Canvas3D canvas3d = Java3dTools.createCanvas3D();
		final Java3dUniverse j3dUniverse = new Java3dUniverse( /*canvas3d ,*/ Java3dUniverse.M );
		final Java3dModel j3dModel = new Java3dModel( j3dTools , j3dUniverse );

		final Vector3D lfb = Vector3D.INIT.set( -1 , -1 , -1 );
		final Vector3D rfb = Vector3D.INIT.set(  1 , -1 , -1 );
		final Vector3D rbb = Vector3D.INIT.set(  1 ,  1 , -1 );
		final Vector3D lbb = Vector3D.INIT.set( -1 ,  1 , -1 );
		final Vector3D lft = Vector3D.INIT.set( -1 , -1 ,  1 );
		final Vector3D rft = Vector3D.INIT.set(  1 , -1 ,  1 );
		final Vector3D rbt = Vector3D.INIT.set(  1 ,  1 ,  1 );
		final Vector3D lbt = Vector3D.INIT.set( -1 ,  1 ,  1 );

		final Object3D cube = new Object3D();
		/* top    */ cube.addFace( new Vector3D[] { lft , lbt , rbt , rft } , texture , false );
		/* bottom */ cube.addFace( new Vector3D[] { lbb , lfb , rfb , rbb } , texture , false );
		/* front  */ cube.addFace( new Vector3D[] { lfb , lft , rft , rfb } , texture , false );
		/* back   */ cube.addFace( new Vector3D[] { rbb , rbt , lbt , lbb } , texture , false );
		/* left   */ cube.addFace( new Vector3D[] { lbb , lbt , lft , lfb } , texture , false );
		/* right  */ cube.addFace( new Vector3D[] { rfb , rft , rbt , rbb } , texture , false );

		j3dModel.createNode( "node" , cube );

		createFrame( 50 , 50 , 400 , 400 , canvas3d ).setVisible( true );

		final ViewingPlatform vwp = new ViewingPlatform(1);
		vwp.setUniverse( j3dUniverse );
		final Canvas3D viewerCanvas = Java3dTools.createCanvas3D();
		final Viewer viewer = new Viewer( viewerCanvas );
		viewer.setViewingPlatform(vwp);
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		final OrbitBehavior orbit = new OrbitBehavior( viewerCanvas , OrbitBehavior.REVERSE_ALL );
		orbit.setSchedulingBounds( bounds );
		vwp.setViewPlatformBehavior( orbit );


		final float DEG45  = (float)Math.PI / 4;
		final float DEG90  = (float)Math.PI / 2;
		final float DEG135 = DEG90 + DEG45;
		final float SQRT2 = (float)Math.sqrt( 2 );
		final float SQRT3 = (float)Math.sqrt( 3 );


//		transform3D.lookAt( new Point3d( 0 , 1 , 10 ) , new Point3d( 0 , 0 , 0 ) , new Vector3d( 0 , 0 , 1 ) );
//		transform3D.invert( transform3D );
		Matrix3D xform = Matrix3D.INIT.rotateZ( Math.toRadians( 45 ) ).rotateX( Math.toRadians( 125.2 ) ).plus( 0 , 0 , -SQRT3 );

		final float unit = j3dUniverse.getUnit();
		if ( ( unit > 0 ) && ( unit != 1 ) )
			xform = xform.setTranslation( xform.xo * unit , xform.yo * unit , xform.zo * unit );
		vwp.getMultiTransformGroup().getTransformGroup( 0 ).setTransform( Java3dTools.convertMatrix3DToTransform3D( xform.inverse() ) );

		j3dUniverse.addViewer( viewer );
		j3dUniverse.getLocale().addBranchGraph( vwp );
		createFrame( 500 , 50 , 400 , 400 , viewerCanvas ).setVisible( true );

		final Component viewComponent = j3dModel.createView( "view" , Vector3D.INIT.set( 0 , 0 , 10 ) , Vector3D.INIT );
		createFrame( 800 , 600 , viewComponent ).setVisible( true );
		j3dModel.updateViews();
	}

	private static JFrame createFrame( final int width , final int height , final Component content )
	{
		final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		return createFrame( ( ss.width - width ) / 2 , ( ss.height - height ) / 2 , width , height , content );
	}

	private static JFrame createFrame( final int x , final int y , final int width , final int height , final Component content )
	{
		final Container contentPane;
		if ( content instanceof Container )
		{
			contentPane = (Container)content;
		}
		else
		{
			contentPane = new JPanel( new BorderLayout() );
			contentPane.add( content , BorderLayout.CENTER );
		}

		final JFrame frame = new JFrame( CLASS_NAME );
//		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane( contentPane );
		frame.setBounds( x , y , width , height );
		return frame;
	}
}
