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
package ab.j3d.model;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

import ab.j3d.*;
import ab.j3d.control.*;
import ab.j3d.view.*;
import ab.j3d.view.jogl.*;

/**
 * Test application for visualizing geometry intersection tests.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class CollisionTestApp
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final Material white = new Material( Color.WHITE.getRGB() );
		final Material red = new Material( Color.RED.getRGB() );

		final Transform3D firstTransform = new Transform3D();
		final Transform3D secondTransform = new Transform3D();

		final ContentNode firstNode = new ContentNode( "first", Matrix3D.getTranslation( -1.5, 0.0, 0.0 ), firstTransform );
		final ContentNode secondNode = new ContentNode( "second", Matrix3D.getTranslation( 1.5, 0.0, 0.0 ), secondTransform );

		final Scene scene = new Scene( Scene.MM );
		scene.addContentNode( firstNode );
		scene.addContentNode( secondNode );

		final Light3D light1 = new Light3D();
		final Light3D light2 = new Light3D();
		light1.setIntensity( 200.0f );
		light2.setIntensity( 50.0f );
		scene.addContentNode( "light1", Matrix3D.getTranslation( 10.0, -10.0, 10.0 ), light1 );
		scene.addContentNode( "light2", Matrix3D.getTranslation( -5.0, 10.0, 5.0 ), light2 );

		final JOGLEngine engine = new JOGLEngine();

		final View3D view = engine.createView( scene );
		view.setFrontClipDistance( 0.001 );
		view.setBackClipDistance( 100.0 );
		view.setCameraControl( new FromToCameraControl2( view, new Vector3D( -5.0, -10.0, 5.0 ), Vector3D.INIT ) );
		view.setBackground( Background.createGradient( Color.LIGHT_GRAY, Color.GRAY ) );

		final Grid grid = view.getGrid();
		grid.setEnabled( true );
		grid.setCellSize( 1 );

		final JToolBar toolBar = new JToolBar( SwingConstants.HORIZONTAL );

		for ( final ContentNode target : Arrays.asList( firstNode, secondNode ) )
		{
			toolBar.add( new AbstractAction( "Sphere" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Node3D root = target.getNode3D();
					root.removeAllChildren();
					root.addChild( new Sphere3D( 1.0, 16, 8, white ) );
					target.fireContentUpdated();
				}
			} );

			toolBar.add( new AbstractAction( "Cube" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Node3D root = target.getNode3D();
					final Transform3D transform = new Transform3D( Matrix3D.getTranslation( -1.0, -1.0, -1.0 ) );
					transform.addChild( new Box3D( 2.0, 2.0, 2.0, null, white ) );
					root.removeAllChildren();
					root.addChild( transform );
					target.fireContentUpdated();
				}
			} );

			toolBar.add( new AbstractAction( "Cylinder" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Node3D root = target.getNode3D();
					final Transform3D transform = new Transform3D( Matrix3D.getTranslation( 0.0, 0.0, -1.0 ) );
					transform.addChild( new Cylinder3D( 2.0, 1.0, 16, white, null, true, white, null, white, null, false ) );
					root.removeAllChildren();
					root.addChild( transform );
					target.fireContentUpdated();
				}
			} );

			toolBar.add( new AbstractAction( "Cone" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Node3D root = target.getNode3D();
					final Transform3D transform = new Transform3D( Matrix3D.getTranslation( 0.0, 0.0, -1.0 ) );
					transform.addChild( new Cone3D( 2.0, 1.0, 0.2, 16, white, null, true, white, null, white, null, false ) );
					root.removeAllChildren();
					root.addChild( transform );
					target.fireContentUpdated();
				}
			} );

			toolBar.add( new AbstractAction( "Prism" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Path2D shape = createShape();

					final Node3D root = target.getNode3D();
					final Transform3D transform = new Transform3D( Matrix3D.getTranslation( 0.0, 0.0, -1.0 ) );
					transform.addChild( new ExtrudedObject2D( shape, new Vector3D( 0.0, 0.0, 2.0 ), null, white, white, white, 0.01, false, false, true ) );
					root.removeAllChildren();
					root.addChild( transform );
					target.fireContentUpdated();
				}
			} );

			toolBar.add( new AbstractAction( "Free-form" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Path2D shape = createShape();

					final Object3DBuilder builder = new Object3DBuilder();
					builder.addExtrudedShape( shape, 0.1, new Vector3D( 0.0, 0.0, 0.2 ), Matrix3D.getTranslation( 0.0, 0.0, 0.8 ), white, null, false, white, null, false, white, null, false, false, false, true );
					builder.addExtrudedShape( shape, 0.1, new Vector3D( 0.0, 0.0, 0.2 ), Matrix3D.getTransform( 0.0, 0.0, 90.0, 0.0, 0.0, 0.0 ), white, null, false, white, null, false, white, null, false, false, false, true );

					final Node3D root = target.getNode3D();
					root.removeAllChildren();
					root.addChild( builder.getObject3D() );
					target.fireContentUpdated();
				}
			} );

			toolBar.addSeparator();
		}

		final JPanel panel = new JPanel( new BorderLayout() );
		panel.add( toolBar, BorderLayout.PAGE_START );
		panel.add( view.getComponent() );

		final JFrame frame = new JFrame( "Collision Test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setContentPane( panel );
		frame.setSize( 1024, 768 );
		frame.setVisible( true );

		final Matrix3D orbit1 = Matrix3D.getRotationTransform( Vector3D.INIT, randomUnitVector(), Math.toRadians( 1.3 ) );
		final Matrix3D orbit2 = Matrix3D.getRotationTransform( Vector3D.INIT, randomUnitVector(), Math.toRadians( 0.9 ) );
		final Matrix3D tumble1 = Matrix3D.getRotationTransform( Vector3D.INIT, randomUnitVector(), Math.toRadians( 0.7 ) );
		final Matrix3D tumble2 = Matrix3D.getRotationTransform( Vector3D.INIT, randomUnitVector(), Math.toRadians( 1.1 ) );

		final List<ContentNode> collisions = new ArrayList<ContentNode>();

		final Timer timer = new Timer( 30, new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				{
					final Matrix3D current = firstTransform.getTransform();
					firstTransform.setTransform( current.multiply( tumble1 ) );
				}
				{
					final Matrix3D current = secondTransform.getTransform();
					secondTransform.setTransform( current.multiply( tumble2 ) );
				}

				{
					final Matrix3D current = firstNode.getTransform();
					firstNode.setTransform( current.multiply( orbit1 ) );
				}
				{
					final Matrix3D current = secondNode.getTransform();
					secondNode.setTransform( current.multiply( orbit2 ) );
				}

				firstNode.fireContentUpdated();
				secondNode.fireContentUpdated();

				collisions.clear();
				if ( firstNode.collidesWith( secondNode ) )
				{
					collisions.add( firstNode );
					collisions.add( secondNode );
				}
			}
		} );
		timer.start();

		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				timer.stop();
			}
		} );

		view.appendRenderStyleFilter( new RenderStyleFilter()
		{
			@Override
			public RenderStyle applyFilter( final RenderStyle style, final Object context )
			{
				RenderStyle result = style;
				if ( collisions.contains( context ) )
				{
					result = style.clone();
					result.setMaterialOverride( red );
				}
				return result;
			}
		} );
	}

	private static Path2D createShape()
	{
		final Path2D shape = new Path2D.Double();

		// A
		shape.moveTo( -1.0, -1.0 );
		shape.lineTo( -0.7, -1.0 );
		shape.lineTo( -0.6, -0.6 );
		shape.lineTo( -0.4, -0.6 );
		shape.lineTo( -0.3, -1.0 );
		shape.lineTo(  0.0, -1.0 );
		shape.lineTo( -0.4,  1.0 );
		shape.lineTo( -0.6,  1.0 );
		shape.closePath();

		// B
		shape.moveTo(  0.0, -1.0 );
		shape.lineTo(  0.3, -1.0 );
		shape.curveTo( 1.0, -1.0, 1.0, 0.0, 0.3, 0.0 );
		shape.curveTo( 1.0, 0.0, 1.0, 1.0, 0.3, 1.0 );
		shape.lineTo(  0.0, 1.0 );
		shape.closePath();

		return shape;
	}

	private static Vector3D randomUnitVector()
	{
		return Vector3D.polarToCartesian( 1.0, Math.toRadians( 360.0 * Math.random() ), Math.toRadians( 180.0 * Math.random() ) );
	}

	/**
	 * Construct new CollisionTestApp.
	 */
	private CollisionTestApp()
	{
	}
}
