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
		frame.setSize( 1024, 700 );

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
	 * Appearance for 3D object.
	 */
	private final Appearance _objectAppearance = BasicAppearance.createForColor( new Color4f( 1.0f, 1.0f, 1.0f, 0.3f ) );

	/**
	 * Override appearance to use for top object.
	 */
	private final Appearance _topAppearance = BasicAppearances.BLUE;

	/**
	 * Override appearance to use for bottom object.
	 */
	private final Appearance _bottomAppearance = BasicAppearances.RED;

	/**
	 * Appearance for slice.
	 */
	private final Appearance _sliceAppearance = BasicAppearances.ALU_PLATE;

	/**
	 * Appearance to use for cutting plane.
	 */
	private final BasicAppearance _planeAppearance = BasicAppearance.createForColor( new Color4f( 0.3f, 0.3f, 0.3f, 0.3f ) );

	/**
	 * UV-map to use for all textures.
	 */
	private final BoxUVMap _uvMap = new BoxUVMap( Scene.MM );

	/**
	 * The object slicer.
	 */
	private final Object3DSlicer _slicer;

	/**
	 * Show cutting plane.
	 */
	private boolean _showCuttingPlane = true;

	/**
	 * Size of shown cutting plane.
	 */
	private double _cuttingPlaneSize = 300.0;

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

		final Object3DSlicer slicer = new Object3DSlicer();
		slicer.setTopEnabled( true );
		slicer.setTopCapped( true );
		slicer.setTopAppearance( _topAppearance );
		slicer.setSliceEnabled( true );
		slicer.setSliceAppearance( _sliceAppearance );
		slicer.setSliceUVMap( _uvMap );
		slicer.setBottomEnabled( true );
		slicer.setBottomCapped( true );
		slicer.setBottomAppearance( _bottomAppearance );
		_slicer = slicer;

		update();

		final JPanel viewPanel = createView( viewFrom, viewAt );
		viewPanel.add( createOptionsPanel(), BorderLayout.EAST );
		_viewPanel = viewPanel;
	}

	/**
	 * Slice 3D object and update scene.
	 */
	private void update()
	{
		final Scene scene = _scene;

		Matrix3D plane2object = Matrix3D.getTransform( _cuttingPlaneRy, _cuttingPlaneRx, 0.0, 0.0, 0.0, 0.0 );
		plane2object = plane2object.setTranslation( plane2object.rotate( 0.0, 0.0, _cuttingPlaneDistance ) );

		if ( _showCuttingPlane )
		{
			final Object3D planeObject = new Object3D();
			final Appearance planeAppearance = _planeAppearance;
			final FaceGroup planeGroup = planeObject.getFaceGroup( planeAppearance, false, true );
			final Face3DBuilder planeFaceBuilder = new Face3DBuilder( planeObject, Vector3D.POSITIVE_Z_AXIS );
			final double size = _cuttingPlaneSize;
			planeFaceBuilder.addQuad( new Vector3D( -size, -size, 0.0 ), new Vector3D( size, -size, 0.0 ), new Vector3D( size, size, 0.0 ), new Vector3D( -size, size, 0.0 ) );
			planeGroup.addFace( planeFaceBuilder.buildFace3D() );
			scene.addContentNode( "cuttingPlane", plane2object, planeObject );
		}
		else
		{
			scene.removeContentNode( "cuttingPlane" );
		}

		final Object3D object = _object;
		final Bounds3D objectBounds = object.getOrientedBoundingBox();

		final Object3DSlicer slicer = _slicer;
		slicer.setCuttingPlane( plane2object );
		slicer.slice( object );

		final boolean objectShown = ( scene.getContentNode( "object" ) != null );
		final Object3D topObject = slicer.getTopObject();
		final Object3D sliceObject = slicer.getSliceObject();
		final Object3D bottomObject = slicer.getBottomObject();

		if ( topObject != null )
		{
		final boolean topOnly = !objectShown && !slicer.isBottomEnabled() && ( !slicer.isSliceEnabled() || !slicer.isTopCapped() );
			scene.addContentNode( "slicedTop", topOnly ? Matrix3D.IDENTITY : Matrix3D.getTranslation( 0.0, 0.0, objectBounds.sizeZ() ), topObject );
		}
		else
		{
			scene.removeContentNode( "slicedTop" );
		}

		if ( sliceObject != null )
		{
			scene.addContentNode( "slice", Matrix3D.IDENTITY, sliceObject );
		}
		else
		{
			scene.removeContentNode( "slice" );
		}

		if ( bottomObject != null )
		{
			final boolean bottomOnly = !objectShown && !slicer.isTopEnabled() && ( !slicer.isSliceEnabled() || !slicer.isBottomCapped() );
			scene.addContentNode( "slicedBottom", bottomOnly ? Matrix3D.IDENTITY : Matrix3D.getTranslation( 0.0, 0.0, -objectBounds.sizeZ() ), bottomObject );
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
			/**
			 * {@link Object3DSlicerExample#_cuttingPlaneRx} at drag start.
			 */
			private double _dragStartCuttingPlaneRx = 0.0;

			/**
			 * {@link Object3DSlicerExample#_cuttingPlaneRy} at drag start.
			 */
			private double _dragStartCuttingPlaneRy = 0.0;

			/**
			 * {@link Object3DSlicerExample#_cuttingPlaneDistance} at drag start.
			 */
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
	 * Construct slicer options panel.
	 *
	 * @return  Panel with slicer options.
	 */
	private JComponent createOptionsPanel()
	{
		final Box result = new Box( BoxLayout.Y_AXIS );

		final JCheckBox objectEnabledCheckBox = new JCheckBox( "Object enabled" );
		result.add( objectEnabledCheckBox );
		objectEnabledCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return ( _scene.getContentNode( "object" ) != null );
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					if ( value )
					{
						_scene.addContentNode( "object", Matrix3D.IDENTITY, _object );
					}
					else
					{
						_scene.removeContentNode( "object" );
					}
					update();
				}
			}
		} );

		final JCheckBox planeEnabledCheckBox = new JCheckBox( "Show cutting plane" );
		result.add( planeEnabledCheckBox );
		planeEnabledCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _showCuttingPlane;
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_showCuttingPlane = value;
					update();
				}
			}
		} );

		final JCheckBox topEnabledCheckBox = new JCheckBox( "Top enabled" );
		result.add( topEnabledCheckBox );
		topEnabledCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _slicer.isTopEnabled();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setTopEnabled( value );
					update();
				}
			}
		} );

		final JCheckBox topCappedCheckBox = new JCheckBox( "Top capped" );
		result.add( topCappedCheckBox );
		topCappedCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _slicer.isTopCapped();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setTopCapped( value );
					update();
				}
			}
		} );

		final JCheckBox topAppearanceOverrideCheckBox = new JCheckBox( "Top appearance override" );
		result.add( topAppearanceOverrideCheckBox );
		topAppearanceOverrideCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return ( _slicer.getTopAppearance() != null );
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setTopAppearance( value ? _topAppearance : null );
					update();
				}
			}
		} );

		final JCheckBox bottomEnabledCheckBox = new JCheckBox( "Bottom enabled" );
		result.add( bottomEnabledCheckBox );
		bottomEnabledCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _slicer.isBottomEnabled();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setBottomEnabled( value );
					update();
				}
			}
		} );

		final JCheckBox bottomCappedCheckBox = new JCheckBox( "Bottom capped" );
		result.add( bottomCappedCheckBox );
		bottomCappedCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _slicer.isBottomCapped();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setBottomCapped( value );
					update();
				}
			}
		} );

		final JCheckBox bottomAppearanceOverrideCheckBox = new JCheckBox( "Bottom appearance override" );
		result.add( bottomAppearanceOverrideCheckBox );
		bottomAppearanceOverrideCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return ( _slicer.getBottomAppearance() != null );
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setBottomAppearance( value ? _bottomAppearance : null );
					update();
				}
			}
		} );

		final JCheckBox sliceEnabledCheckBox = new JCheckBox( "Slice enabled" );
		result.add( sliceEnabledCheckBox );
		sliceEnabledCheckBox.setModel( new JToggleButton.ToggleButtonModel()
		{
			@Override
			public boolean isSelected()
			{
				return _slicer.isSliceEnabled();
			}

			@Override
			public void setSelected( final boolean value )
			{
				super.setSelected( value );
				if ( value != isSelected() )
				{
					_slicer.setSliceEnabled( value );
					update();
				}
			}
		} );

		result.add( Box.createGlue() );
		return result;
	}


	/**
	 * Load 3D object to slice.
	 *
	 * @return  3D object.
	 */
	private Object3D loadObject()
	{
		final String path = "data/bordatore.stl";
		final Appearance appearance = _objectAppearance;
		final Matrix3D transform = Matrix3D.getTransform( 180.0, 0.0, 0.0, -47.6, 157.4, 64.0 );

		final StlLoader loader = new StlLoader();
		loader.setAppearance( appearance );
		final Object3D object;

		final InputStream in = Object3DSlicerExample.class.getResourceAsStream( path );
		try
		{
			if ( in == null )
			{
				throw new RuntimeException( "Demo data not found!" );
			}

			object = loader.load( transform, in );
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

		object.smooth( 30.0, 0.1, true );

		final BoxUVMap boxMap = _uvMap;
		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			for ( final Face3D face3D : faceGroup.getFaces() )
			{
				final UVGenerator uvGenerator = UVGenerator.getColorMapInstance( appearance, boxMap, face3D.getNormal(), false );

				for ( final Vertex3D vertex3D : face3D.getVertices() )
				{
					uvGenerator.generate( vertex3D.point );
					vertex3D.colorMapU = uvGenerator.getU();
					vertex3D.colorMapV = uvGenerator.getV();
				}
			}
		}

		return object;
	}
}
