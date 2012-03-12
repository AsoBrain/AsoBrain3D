/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.example;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.awt.view.jogl.*;
import ab.j3d.control.*;
import ab.j3d.geom.*;
import ab.j3d.loader.*;
import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * This application provides an example for  the {@link Object3DSlicer} class.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Object3DSlicerExample
{
	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 *
	 * @throws  Exception if the application crashes.
	 */
	public static void main( final String[] args )
		throws Exception
	{
		final Object3DSlicerExample example = new Object3DSlicerExample();

		final JFrame frame = new JFrame( "Slicer Demo" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setContentPane( example._viewPanel );
		frame.setSize( 800, 600 );

		final Toolkit toolkit = frame.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		frame.setLocation( screenBounds.x + ( screenBounds.width  + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2,
		                   screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );

		frame.setVisible( true );
	}

	/**
	 * 3D scene.
	 */
	private final Scene _scene;

	/**
	 * Object to slice.
	 */
	private final Object3D _object;

	/**
	 * Panel with 3D view of result.
	 */
	private final JPanel _viewPanel;

	/**
	 * Cutting plane rotation parameter.
	 */
	private double _cuttingPlaneRx = 0.0;

	/**
	 * Cutting plane rotation parameter.
	 */
	private double _cuttingPlaneRy = 0.0;

	/**
	 * Cutting plane translation parameter.
	 */
	private double _cuttingPlaneDistance = 0.0;

	/**
	 * Construct demo.
	 */
	Object3DSlicerExample()
	{
		final Object3D object = loadObject();
		_object = object;

		final Vector3D viewFrom = new Vector3D( 0.0, -2000.0, 250.0 );
		final Vector3D viewAt = Vector3D.ZERO;

		final Scene scene = new Scene( Scene.MM );
		Scene.addLegacyLights( scene );
		scene.addContentNode( "object", Matrix3D.IDENTITY, object );
		_scene = scene;

		update();

		_viewPanel = createView( viewFrom, viewAt );
	}

	/**
	 * Slice 3D object and update scene.
	 */
	private void update()
	{
		final Scene scene = _scene;

		Matrix3D planeTransform = Matrix3D.getTransform( _cuttingPlaneRy, _cuttingPlaneRx, 0.0, 0.0, 0.0, 0.0 );
		planeTransform = planeTransform.setTranslation( planeTransform.rotate( 0.0, 0.0, _cuttingPlaneDistance ) );

		final BasicPlane3D cuttingPlane = new BasicPlane3D( planeTransform, true );

		final Object3DSlicer slicer = new Object3DSlicer();
		slicer.setSliceAppearance( BasicAppearances.RED );
		slicer.setTopAppearance( BasicAppearances.BLUE );
		slicer.setBottomAppearance( BasicAppearances.GREEN );
		final Object3D object = _object;
		final Bounds3D objectBounds = object.getOrientedBoundingBox();
		slicer.slice( object, cuttingPlane );

		final Object3D planeObject = new Object3D();
		final Appearance planeAppearance = BasicAppearance.createForColor( new Color4f( 0.3f, 0.3f, 0.3f, 0.3f ) );
		final FaceGroup planeGroup = planeObject.getFaceGroup( planeAppearance, false, true );
		final Face3DBuilder planeFaceBuilder = new Face3DBuilder( planeObject, Vector3D.POSITIVE_Z_AXIS );
		planeFaceBuilder.addQuad( new Vector3D( -250.0, -250.0, 0.0 ), new Vector3D( 250.0, -250.0, 0.0 ), new Vector3D( 250.0, 250.0, 0.0 ), new Vector3D( -250.0, 250.0, 0.0 ) );
		planeGroup.addFace( planeFaceBuilder.buildFace3D() );
		scene.addContentNode( "cuttingPlane", planeTransform, planeObject );

		final Object3D topObject = slicer.getTopObject();
		if ( topObject != null )
		{
			scene.addContentNode( "slicedTop", Matrix3D.getTranslation( 0.0, 0.0, objectBounds.sizeZ() ), topObject );
		}
		else
		{
			scene.removeContentNode( "slicedTop" );
		}

		final Object3D sliceObject = slicer.getSliceObject();
		if ( sliceObject != null )
		{
			scene.addContentNode( "slice", Matrix3D.IDENTITY, sliceObject );
		}
		else
		{
			scene.removeContentNode( "slice" );
		}

		final Object3D bottomObject = slicer.getBottomObject();
		if ( bottomObject != null )
		{
			scene.addContentNode( "slicedBottom", Matrix3D.getTranslation( 0.0, 0.0, -objectBounds.sizeZ() ), bottomObject );
		}
		else
		{
			scene.removeContentNode( "slicedBottom" );
		}
	}

	/**
	 * Create 3D view panel.
	 *
	 * @param   viewFrom    Camera from-point.
	 * @param   viewAt      Camera to-point.
	 *
	 * @return  3D view panel.
	 */
	private JPanel createView( final Vector3D viewFrom, final Vector3D viewAt )
	{
		final JOGLConfiguration configuration = JOGLConfiguration.createSafeInstance();
		configuration.setFSAAEnabled( true );
		configuration.setPerPixelLightingEnabled( true );
		configuration.setVertexBufferObjectsEnabled( true );

		final RenderEngine renderEngine = new JOGLEngine( configuration );

		final View3D view = renderEngine.createView( _scene );
		view.setRenderingPolicy( RenderingPolicy.SKETCH );
		view.setBackground( Background.createGradient( new Color4f( 0x67, 0x79, 0x88 ), new Color4f( 0x17, 0x47, 0x72 ), new Color4f( 0x85, 0xA4, 0xBF ), new Color4f( 0x9F, 0xB8, 0xCE ) ) );
		final FromToCameraControl cameraControl = new FromToCameraControl( view, viewFrom, viewAt );
		cameraControl.setPanEventModifiers( 0 );
		view.setCameraControl( cameraControl );

		final ViewControlInput controlInput = view.getControlInput();
		controlInput.addControlInputListener( new MouseControl()
		{
			private double _dragStartCuttingPlaneRx = 0.0;
			private double _dragStartCuttingPlaneRy = 0.0;
			private double _dragStartCuttingPlaneDistance = 0.0;

			@Override
			public void mousePressed( final ControlInputEvent event )
			{
				if ( event.getMouseButtonDown() == 2 )
				{
					_dragStartCuttingPlaneRx = _cuttingPlaneRx;
					_dragStartCuttingPlaneRy = _cuttingPlaneRy;
				}
				else if ( event.getMouseButtonDown() == 3 )
				{
					_dragStartCuttingPlaneDistance = _cuttingPlaneDistance;
				}
			}

			@Override
			public void mouseDragged( final ControlInputEvent event )
			{
				if ( event.getMouseButtonDown() == 2 )
				{
					_cuttingPlaneRx = _dragStartCuttingPlaneRx + event.getDragDeltaX();
					_cuttingPlaneRy = _dragStartCuttingPlaneRy - event.getDragDeltaY();
					update();
				}
				else if ( event.getMouseButtonDown() == 3 )
				{
					_cuttingPlaneDistance = _dragStartCuttingPlaneDistance + event.getDragDeltaX() - event.getDragDeltaY();
					update();
				}

				super.mouseDragged( event );
			}
		} );

		final JPanel viewPanel = new JPanel( new BorderLayout() );
		viewPanel.add( view.getComponent(), BorderLayout.CENTER );
		viewPanel.add( View3DPanel.createToolBar( view, new Locale( "nl" ) ), BorderLayout.SOUTH );
		return viewPanel;
	}

	/**
	 * Load 3D object to slice.
	 *
	 * @return  3D object.
	 */
	private Object3D loadObject()
	{
		final Matrix3D transform = Matrix3D.getTransform( 180.0, 0.0, 0.0, 0.0, 0.0, 50.0 );
		final String path = "data/bordatore.stl";

		final StlLoader loader = new StlLoader();
		loader.setAppearance( BasicAppearance.createForColor( new Color4f( 1.0f, 1.0f, 1.0f, 0.3f ) ) );
		final Object3D object;

		final InputStream in = Object3DSlicerExample.class.getResourceAsStream( path );
		try
		{
			if ( in == null )
			{
				throw new RuntimeException( "Demo data not found!" );
			}

			object = loader.load( transform, in );
			object.smooth( 30.0, 0.1, true );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			if ( in != null )
			{
				try
				{
					in.close();
				}
				catch ( IOException e )
				{
					/* ignore */
				}
			}
		}
		return object;
	}
}
