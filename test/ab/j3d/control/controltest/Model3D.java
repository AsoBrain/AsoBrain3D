/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2009
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
package ab.j3d.control.controltest;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.controltest.model.Floor;
import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.PaintableTriangle;
import ab.j3d.control.controltest.model.SceneElement;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.control.controltest.model.Wall;
import ab.j3d.model.Box3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Object3DBuilder;
import ab.j3d.model.Scene;
import ab.j3d.view.RenderEngine;
import ab.j3d.view.View3D;
import ab.j3d.view.java3d.Java3dEngine;

/**
 * The {@link Model3D} creates a 3d representation of a {@link Model}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class Model3D
{
	/**
	 * The material for the floor.
	 */
	private static final Material FLOOR_MATERIAL = new Material( Color.WHITE.getRGB() );

	/**
	 * The material for a Wall.
	 */
	private static final Material WALL_MATERIAL = new Material( Color.RED.getRGB() );

	/**
	 * The material for a TetraHedron.
	 */
	private static final Material HEDRON_MATERIAL = new Material( Color.GREEN.getRGB() );

	/**
	 * The material for a selected TetraHedron.
	 */
	private static final Material SELECTION_MATERIAL = new Material( Color.BLUE.getRGB() );

	/**
	 * The material for a selected face.
	 */
	private static final Material FACE_SELECTION_MATERIAL = new Material( Color.MAGENTA.getRGB() );

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

		model.addPropertyChangeListener( SceneElement.ELEMENT_CHANGED , propertyListener );
		model.addPropertyChangeListener( Model.SELECTION_CHANGED , propertyListener );
		model.addPropertyChangeListener( Model.FACE_SELECTION_CHANGED   , propertyListener );
		_model = model;

		final Scene scene = new Scene( Scene.FOOT );
		Scene.addLegacyLights( scene );
		_scene = scene;

		_renderEngine = new Java3dEngine( scene, Color.GRAY );

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
			updateSelection( oldSelection , newSelection );
		}
		else if ( Model.FACE_SELECTION_CHANGED == property )
		{
			final PaintableTriangle oldFace = (PaintableTriangle)event.getOldValue();
			final PaintableTriangle newFace = (PaintableTriangle)event.getNewValue();
			updateFaceSelection( oldFace , newFace );
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
			setMaterialOfAllFaces( element3D , SELECTION_MATERIAL );
		}

		_scene.addContentNode( element , transform , element3D );
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
		builder.addQuad( Vector3D.INIT.set( -halfX , -halfY , 0.0 ) ,
		                 Vector3D.INIT.set( -halfX ,  halfY , 0.0 ) ,
		                 Vector3D.INIT.set(  halfX ,  halfY , 0.0 ) ,
		                 Vector3D.INIT.set(  halfX , -halfY , 0.0 ) , FLOOR_MATERIAL , true );
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

		return new Box3D( Matrix3D.INIT , width , depth , height , 0.001 , WALL_MATERIAL , WALL_MATERIAL );
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

		final Material material = HEDRON_MATERIAL;

		final double width     = hedron.getSize();
		final double depth     = Math.cos( Math.toRadians( 30.0 ) ) * width;
		final double height    = Math.sin( Math.toRadians( 71.0 ) ) * depth;

		final double halfWidth = width  / 2.0;
		final double depth1    = height / Math.tan( Math.toRadians( 51.0 ) );
		final double depth2    = depth  - depth1;

		final Vector3D v0 = Vector3D.INIT.set( -halfWidth , -depth2 ,    0.0 );
		final Vector3D v1 = Vector3D.INIT.set(  halfWidth , -depth2 ,    0.0 );
		final Vector3D v2 = Vector3D.INIT.set(        0.0 ,  depth1 ,    0.0 );
		final Vector3D v3 = Vector3D.INIT.set(        0.0 ,     0.0 , height );

		final Object3DBuilder builder = new Object3DBuilder();
		/* Bottom */ builder.addTriangle( v2 , v0 , v1 , material , false );
		/* Back   */ builder.addTriangle( v3 , v1 , v0 , material , false );
		/* Left   */ builder.addTriangle( v3 , v0 , v2 , material , false );
		/* Right  */ builder.addTriangle( v3 , v2 , v1 , material , false );
		return result;
	}

	/**
	 * Updates selection from <code>oldSelection</code> to
	 * <code>newSelection</code>. The old {@link SceneElement} (if not
	 * <code>null</code>) is given back the normal material, while the new
	 * {@link SceneElement} (if not <code>null</code>) is given the color of a
	 * selected {@link SceneElement}.
	 *
	 * @param   oldSelection   Previous selection (may be <code>null</code>).
	 * @param   newSelection   New selection (may be <code>null</code>).
	 */
	private void updateSelection( final SceneElement oldSelection , final SceneElement newSelection )
	{
		final Scene scene = _scene;

		if ( oldSelection != null )
		{
			final ContentNode oldNode = scene.getContentNode( oldSelection );
			final Object3D oldObject3D = ( oldNode != null ) ? (Object3D)oldNode.getNode3D() : null;

			Material material = null;

			if ( oldSelection instanceof Floor )
			{
				material = FLOOR_MATERIAL;
			}
			else if ( oldSelection instanceof Wall )
			{
				material = WALL_MATERIAL;
			}
			else if ( oldSelection instanceof TetraHedron )
			{
				material = HEDRON_MATERIAL;
			}

			setMaterialOfAllFaces( oldObject3D , material );

			final ContentNode contentNode = scene.getContentNode( oldSelection );
			contentNode.fireContentUpdated();
		}

		if ( newSelection != null )
		{
			final ContentNode newNode = scene.getContentNode( newSelection );
			final Object3D newObject3D = ( newNode != null ) ? (Object3D)newNode.getNode3D() : null;

			setMaterialOfAllFaces( newObject3D , SELECTION_MATERIAL );
			final ContentNode contentNode = scene.getContentNode( newSelection );
			contentNode.fireContentUpdated();
		}
	}

	/**
	 * Updates face selection from <code>oldFace</code> to <code>newFace</code>.
	 * The old face (if not <code>null</code>) is given back the normal material,
	 * while the new face (if not <code>null</code>) is given the color of a
	 * selected face.
	 *
	 * @param   oldFace   Previous selection (may be <code>null</code>).
	 * @param   newFace   New selection (may be <code>null</code>).
	 */
	private void updateFaceSelection( final PaintableTriangle oldFace , final PaintableTriangle newFace )
	{
		final Scene scene = _scene;

		TetraHedron oldHedron = null;
		if ( oldFace != null )
		{
			oldHedron = oldFace.getTetraHedron();

			final ContentNode hedronNode = scene.getContentNode( oldHedron );
			final Object3D hedron3D = ( hedronNode != null ) ? (Object3D)hedronNode.getNode3D() : null;
			setMaterialOfAllFaces( hedron3D , SELECTION_MATERIAL );
		}

		TetraHedron newHedron = null;
		if ( newFace != null )
		{
			newHedron = newFace.getTetraHedron();

			final ContentNode hedronNode = scene.getContentNode( newHedron );
			if ( hedronNode != null )
			{
				final Object3D hedron3D = (Object3D)hedronNode.getNode3D();

				final Face3D face = hedron3D.getFace( newFace.getFaceNumber() );
				face.material = FACE_SELECTION_MATERIAL;
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
	 * Set material of all faces of the specified 3D object.
	 *
	 * @param   object      3D object whose face material to set.
	 * @param   material    Material to set.
	 */
	private static void setMaterialOfAllFaces( final Object3D object , final Material material )
	{
		for ( int i = 0 ; i < object.getFaceCount() ; i++ )
		{
			final Face3D face = object.getFace( i );
			face.material = material;
		}
	}
}
