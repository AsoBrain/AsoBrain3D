/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.control.controltest;

import java.beans.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.awt.view.*;
import ab.j3d.control.controltest.model.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import ab.j3d.view.View3D;

/**
 * The {@link Model3D} creates a 3d representation of a {@link Model}.
 *
 * @author  Mart Slot
 */
public class Model3D
{
	/**
	 * The appearance for the floor.
	 */
	private static final Appearance FLOOR_APPEARANCE = createApperance( Color4.WHITE );

	/**
	 * The appearance for a Wall.
	 */
	private static final Appearance WALL_APPEARANCE = createApperance( Color4.RED );

	/**
	 * The appearance for a TetraHedron.
	 */
	private static final Appearance HEDRON_APPEARANCE = createApperance( Color4.GREEN );

	/**
	 * The appearance for a selected TetraHedron.
	 */
	private static final Appearance SELECTION_APPEARANCE = createApperance( Color4.BLUE );

	/**
	 * The appearance for a selected face.
	 */
	private static final Appearance FACE_SELECTION_APPEARANCE = createApperance( Color4.MAGENTA );

	/**
	 * Create appearance for solid color.
	 *
	 * @param   color   Color to create appearance for.
	 *
	 * @return  Appearance for solid color.
	 */
	public static Appearance createApperance( final Color4 color )
	{
		final BasicAppearance result = new BasicAppearance();
		result.setAmbientColor( color );
		result.setDiffuseColor( color );
		result.setSpecularColor( Color4.WHITE );
		result.setShininess( 16 );
		return result;
	}

	/**
	 * {@link Model} whose objects should be translated to 3d.
	 */
	private final Model _model;

	/**
	 * 3D scene.
	 */
	private final Scene _scene;

	/**
	 * Render engine for 3D scene.
	 */
	private final RenderEngine _renderEngine;

	/**
	 * A {@link Set} with all {@link SceneElement}s in the 3d scene.
	 */
	private final Set<SceneElement> _elements;

	/**
	 * Construct new Model3D to represent the given {@link Model} in 3D.
	 *
	 * @param   model   {@link Model} whose objects should be translated to 3d.
	 *
	 * @throws  NullPointerException if <code>main</code> is <code>null</code>.
	 */
	public Model3D( final Model model )
	{
		final PropertyChangeListener propertyListener = new PropertyChangeListener()
		{
			public void propertyChange( final PropertyChangeEvent event )
			{
				Model3D.this.propertyChange( event );
			}
		};

		model.addPropertyChangeListener( SceneElement.ELEMENT_CHANGED, propertyListener );
		model.addPropertyChangeListener( Model.SELECTION_CHANGED, propertyListener );
		model.addPropertyChangeListener( Model.FACE_SELECTION_CHANGED, propertyListener );
		_model = model;

		final Scene scene = new Scene( Scene.FOOT );
		Scene.addLegacyLights( scene );
		_scene = scene;

		_renderEngine = RenderEngineFactory.createJOGLEngine( new DebugTextureLibrary(), new JOGLConfiguration() );

		_elements = new HashSet<SceneElement>();

		updateScene();
	}

	/**
	 * Creates a {@link ab.j3d.view.View3D} that displays the 3d scene. The id object
	 * is used to identify the view, and should be unique for each view.
	 *
	 * @return  A {@link View3D} that displays the 3D scene.
	 */
	public View3D createView()
	{
		return _renderEngine.createView( _scene );
	}

	/**
	 * Called when the {@link Model} has changed, the selection has changed or
	 * a {@link SceneElement} has changed.
	 *
	 * @param   event   {@link PropertyChangeEvent} that was thrown.
	 */
	private void propertyChange( final PropertyChangeEvent event )
	{
		final String property = event.getPropertyName();

		if ( Model.MODEL_CHANGED == property )
		{
			updateScene();
		}
		else if ( SceneElement.ELEMENT_CHANGED == property )
		{
			final SceneElement element = (SceneElement)event.getNewValue();
			updateElement( element );
		}
		else if ( Model.SELECTION_CHANGED == property )
		{
			final SceneElement oldSelection = (SceneElement)event.getOldValue();
			final SceneElement newSelection = (SceneElement)event.getNewValue();
			updateSelection( oldSelection, newSelection );
		}
		else if ( Model.FACE_SELECTION_CHANGED == property )
		{
			final PaintableTriangle oldFace = (PaintableTriangle)event.getOldValue();
			final PaintableTriangle newFace = (PaintableTriangle)event.getNewValue();
			updateFaceSelection( oldFace, newFace );
		}
	}

	/**
	 * Updates the scene. Any existing 3d objects are either updated or deleted
	 * if they were removed from the {@link Model}.
	 */
	private void updateScene()
	{
		final Scene scene = _scene;

		/*** ALLEEN 'EIGEN' NODES REMOVEN (LAZY REMOVE) ****/

		final Set<SceneElement> removedElements = new HashSet<SceneElement>( _elements );

		for ( final SceneElement element : _model.getScene() )
		{
			if ( _elements.contains( element ) )
			{
				scene.removeContentNode( element );
				removedElements.remove( element );
			}

			updateElement( element );
		}

		for ( final SceneElement element : removedElements )
		{
			scene.removeContentNode( element );
		}
	}

	/**
	 * Updates a {@link SceneElement} in the 3D world. This method recreates the
	 * 3d object and then adds it to the scene.
	 *
	 * @param   element     {@link SceneElement} to update.
	 */
	private void updateElement( final SceneElement element )
	{
		Object3D element3D = null;
		final Matrix3D transform = element.getTransform();

		if ( element instanceof Floor )
		{
			element3D = createFloor3D( (Floor)element );
		}
		else if ( element instanceof Wall )
		{
			element3D = createWall3D( (Wall)element );
		}
		else if ( element instanceof TetraHedron )
		{
			element3D = createTetraHedron3D( (TetraHedron)element );
		}

		if ( element == _model.getSelection() )
		{
			setAppearanceOfAllFaces( element3D, SELECTION_APPEARANCE );
		}

		_scene.addContentNode( element, transform, element3D );
		_elements.add( element );
	}

	/**
	 * Creates a 3D representation of a {@link Floor}.
	 *
	 * @param   floor   The {@link Floor} for which to make the {@link Object3D}
	 *
	 * @return  3D object representing the {@link Floor}.
	 */
	private static Object3D createFloor3D( final Floor floor )
	{
		final double halfX = floor.getXSize() / 2.0;
		final double halfY = floor.getYSize() / 2.0;

		final Object3DBuilder builder = new Object3DBuilder();
		builder.addQuad( new Vector3D( -halfX, -halfY, 0.0 ),
		                 new Vector3D( -halfX,  halfY, 0.0 ),
		                 new Vector3D(  halfX,  halfY, 0.0 ),
		                 new Vector3D(  halfX, -halfY, 0.0 ), FLOOR_APPEARANCE, true );
		return builder.getObject3D();
	}

	/**
	 * Creates a 3D representation of a {@link Wall}.
	 *
	 * @param   wall    The {@link Wall} for which to make the {@link Object3D}.
	 *
	 * @return  3D object representing the {@link Wall}.
	 */
	private static Object3D createWall3D( final Wall wall )
	{
		final double width  = wall.getXSize();
		final double height = wall.getZSize();
		final double depth  = wall.getYSize();

		final Appearance appearance = WALL_APPEARANCE;
		return new Box3D( width, depth, height, new BoxUVMap( Scene.MM ), appearance );
	}

	/**
	 * Creates a 3D representation of a {@link TetraHedron}.
	 *
	 * @param   hedron  {@link TetraHedron} for which to make the
	 *                  {@link Object3D}.
	 *
	 * @return  3D object representing the {@link TetraHedron}.
	 */
	private static Object3D createTetraHedron3D( final TetraHedron hedron )
	{
		final Object3D result = new Object3D();

		final Appearance appearance = HEDRON_APPEARANCE;

		final double width     = hedron.getSize();
		final double depth     = Math.cos( Math.toRadians( 30.0 ) ) * width;
		final double height    = Math.sin( Math.toRadians( 71.0 ) ) * depth;

		final double halfWidth = width  / 2.0;
		final double depth1    = height / Math.tan( Math.toRadians( 51.0 ) );
		final double depth2    = depth  - depth1;

		final Vector3D v0 = new Vector3D( -halfWidth, -depth2,    0.0 );
		final Vector3D v1 = new Vector3D(  halfWidth, -depth2,    0.0 );
		final Vector3D v2 = new Vector3D(        0.0,  depth1,    0.0 );
		final Vector3D v3 = new Vector3D(        0.0,     0.0, height );

		final Object3DBuilder builder = new Object3DBuilder();
		/* Bottom */ builder.addTriangle( v2, v0, v1, appearance, false );
		/* Back   */ builder.addTriangle( v3, v1, v0, appearance, false );
		/* Left   */ builder.addTriangle( v3, v0, v2, appearance, false );
		/* Right  */ builder.addTriangle( v3, v2, v1, appearance, false );
		return result;
	}

	/**
	 * Updates selection from <code>oldSelection</code> to
	 * <code>newSelection</code>. The old {@link SceneElement} (if not
	 * <code>null</code>) is given back the normal appearance, while the new
	 * {@link SceneElement} (if not <code>null</code>) is given the color of a
	 * selected {@link SceneElement}.
	 *
	 * @param   oldSelection   Previous selection (may be <code>null</code>).
	 * @param   newSelection   New selection (may be <code>null</code>).
	 */
	private void updateSelection( final SceneElement oldSelection, final SceneElement newSelection )
	{
		final Scene scene = _scene;

		if ( oldSelection != null )
		{
			final ContentNode oldNode = scene.getContentNode( oldSelection );
			final Object3D oldObject3D = ( oldNode != null ) ? (Object3D)oldNode.getNode3D() : null;

			Appearance appearance = null;

			if ( oldSelection instanceof Floor )
			{
				appearance = FLOOR_APPEARANCE;
			}
			else if ( oldSelection instanceof Wall )
			{
				appearance = WALL_APPEARANCE;
			}
			else if ( oldSelection instanceof TetraHedron )
			{
				appearance = HEDRON_APPEARANCE;
			}

			setAppearanceOfAllFaces( oldObject3D, appearance );

			final ContentNode contentNode = scene.getContentNode( oldSelection );
			contentNode.fireContentUpdated();
		}

		if ( newSelection != null )
		{
			final ContentNode newNode = scene.getContentNode( newSelection );
			final Object3D newObject3D = ( newNode != null ) ? (Object3D)newNode.getNode3D() : null;

			setAppearanceOfAllFaces( newObject3D, SELECTION_APPEARANCE );
			final ContentNode contentNode = scene.getContentNode( newSelection );
			contentNode.fireContentUpdated();
		}
	}

	/**
	 * Updates face selection from <code>oldFace</code> to <code>newFace</code>.
	 * The old face (if not <code>null</code>) is given back the normal appearance,
	 * while the new face (if not <code>null</code>) is given the color of a
	 * selected face.
	 *
	 * @param   oldFace   Previous selection (may be <code>null</code>).
	 * @param   newFace   New selection (may be <code>null</code>).
	 */
	private void updateFaceSelection( final PaintableTriangle oldFace, final PaintableTriangle newFace )
	{
		final Scene scene = _scene;

		TetraHedron oldHedron = null;
		if ( oldFace != null )
		{
			oldHedron = oldFace.getTetraHedron();

			final ContentNode hedronNode = scene.getContentNode( oldHedron );
			final Object3D hedron3D = ( hedronNode != null ) ? (Object3D)hedronNode.getNode3D() : null;
			setAppearanceOfAllFaces( hedron3D, SELECTION_APPEARANCE );
		}

		TetraHedron newHedron = null;
		if ( newFace != null )
		{
			newHedron = newFace.getTetraHedron();

			final ContentNode hedronNode = scene.getContentNode( newHedron );
			if ( hedronNode != null )
			{
				final Object3D hedron3D = (Object3D)hedronNode.getNode3D();

				// FIXME: broken
/*
				final Face3D face = hedron3D.getFace( newFace.getFaceNumber() );
				face.appearance = FACE_SELECTION_APPEARANCE;
*/
			}
		}

		if ( oldHedron != null )
		{
			final ContentNode contentNode = scene.getContentNode( oldHedron );
			contentNode.fireContentUpdated();
		}

		if ( ( newHedron != null ) && ( newHedron != oldHedron ) )
		{
			final ContentNode contentNode = scene.getContentNode( newHedron );
			contentNode.fireContentUpdated();
		}
	}

	/**
	 * Set appearance of all faces of the specified 3D object.
	 *
	 * @param   object      3D object whose face appearance to set.
	 * @param   appearance    Appearance to set.
	 */
	private static void setAppearanceOfAllFaces( final Object3D object, final Appearance appearance )
	{
		for ( final FaceGroup faceGroup : object.getFaceGroups() )
		{
			faceGroup.setAppearance( appearance );
		}
	}
}
