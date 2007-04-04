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
package ab.j3d.control.controltest;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.control.controltest.model.Floor;
import ab.j3d.control.controltest.model.Model;
import ab.j3d.control.controltest.model.PaintableTriangle;
import ab.j3d.control.controltest.model.SceneElement;
import ab.j3d.control.controltest.model.TetraHedron;
import ab.j3d.control.controltest.model.Wall;
import ab.j3d.model.Box3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelTools;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.java3d.Java3dModel;

/**
 * The {@link Model3D} creates a 3d representation of a {@link Model}. It uses a
 * {@link ViewModel} to hold the 3D scene. To display this 3d scene, the method
 * {@link #createView} creates a {@link ViewModelView} for the ViewModel.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public final class Model3D
{
	/**
	 * The texture for the floor.
	 */
	private static final TextureSpec FLOOR_TEXTURE = new TextureSpec( Color.WHITE );

	/**
	 * The texture for a Wall.
	 */
	private static final TextureSpec WALL_TEXTURE = new TextureSpec( Color.RED );

	/**
	 * The texture for a TetraHedron.
	 */
	private static final TextureSpec HEDRON_TEXTURE = new TextureSpec( Color.GREEN );

	/**
	 * The texture for a selected TetraHedron.
	 */
	private static final TextureSpec SELECTION_TEXTURE = new TextureSpec( Color.BLUE );

	/**
	 * The texture for a selected face.
	 */
	private static final TextureSpec FACE_SELECTION_TEXTURE = new TextureSpec( Color.MAGENTA );

	/**
	 * {@link Model} whose objects should be translated to 3d.
	 */
	private final Model _model;

	/**
	 * The {@link ViewModel} that holds the 3D scene.
	 */
	private final ViewModel _viewModel;

	/**
	 * A {@link Set} with all {@link SceneElement}s in the 3d scene.
	 */
	private final Set _elements;

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

		_viewModel = new Java3dModel( ViewModel.FOOT , Color.GRAY);
		ViewModelTools.addLegacyLights( _viewModel );

		_elements = new HashSet();

		updateScene();
	}

	/**
	 * Creates a {@link ViewModelView} that displays the 3d scene. The id object
	 * is used to identify the view, and should be unique for each view.
	 *
	 * @param   id      The ID object used to register the view.
	 *
	 * @return  A {@link ViewModelView} that displays the 3D scene.
	 */
	public ViewModelView createView( final Object id )
	{
		return _viewModel.createView( id );
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
		final ViewModel viewModel = _viewModel;

		/*** ALLEEN 'EIGEN' NODES REMOVEN (LAZY REMOVE) ****/

		final Set removedElements = new HashSet( _elements );

		final Set scene = _model.getScene();
		for ( Iterator iterator = scene.iterator() ; iterator.hasNext() ; )
		{
			final SceneElement element = (SceneElement) iterator.next();


			if ( _elements.contains( element ) )
			{
				viewModel.removeNode( element );
				removedElements.remove( element );
			}

			updateElement( element );
		}

		for ( Iterator iterator = removedElements.iterator() ; iterator.hasNext() ; )
		{
			final Object id = iterator.next();

			viewModel.removeNode( id );
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
			setTextureOfAllFaces( element3D , SELECTION_TEXTURE );
		}

		_viewModel.createNode( element , transform , element3D , null , 1.0f);
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

		final Vector3D[] floorCoords = new Vector3D[]
		{
			Vector3D.INIT.set( -halfX , -halfY , 0.0 ) ,
			Vector3D.INIT.set( -halfX ,  halfY , 0.0 ) ,
			Vector3D.INIT.set(  halfX ,  halfY , 0.0 ) ,
			Vector3D.INIT.set(  halfX , -halfY , 0.0 ) ,
		};

		final Object3D result = new Object3D();
		result.addFace( floorCoords , FLOOR_TEXTURE , false , true );
		return result;
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

		return new Box3D( Matrix3D.INIT , width , depth , height , WALL_TEXTURE , WALL_TEXTURE );
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

		final TextureSpec texture = HEDRON_TEXTURE;

		final double width     = hedron.getSize();
		final double depth     = Math.cos( Math.toRadians( 30.0 ) ) * width;
		final double height    = Math.sin( Math.toRadians( 71.0 ) ) * depth;

		final double halfWidth = width  / 2.0;
		final double depth1    = height / Math.tan( Math.toRadians( 51.0 ) );
		final double depth2    = depth  - depth1;

		result.setVertexCoordinates( new double[]
			{
				/* 0 */ -halfWidth , -depth2 ,    0.0 ,
				/* 1 */  halfWidth , -depth2 ,    0.0 ,
				/* 2 */        0.0 ,  depth1 ,    0.0 ,
				/* 3 */        0.0 ,     0.0 , height ,
			} );


		/* Bottom */ result.addFace( new int[] { 2 , 0 , 1 } , texture , false );
		/* Back   */ result.addFace( new int[] { 3 , 1 , 0 } , texture , false );
		/* Left   */ result.addFace( new int[] { 3 , 0 , 2 } , texture , false );
		/* Right  */ result.addFace( new int[] { 3 , 2 , 1 } , texture , false );

		return result;
	}

	/**
	 * Updates selection from <code>oldSelection</code> to
	 * <code>newSelection</code>. The old {@link SceneElement} (if not
	 * <code>null</code>) is given back the normal texture, while the new
	 * {@link SceneElement} (if not <code>null</code>) is given the color of a
	 * selected {@link SceneElement}.
	 *
	 * @param   oldSelection   Previous selection (may be <code>null</code>).
	 * @param   newSelection   New selection (may be <code>null</code>).
	 */
	private void updateSelection( final SceneElement oldSelection , final SceneElement newSelection )
	{
		final ViewModel viewModel = _viewModel;

		if ( oldSelection != null )
		{
			final Object3D oldObject3D = (Object3D)viewModel.getNode3D( oldSelection );
			TextureSpec texture = null;

			if ( oldSelection instanceof Floor )
			{
				texture = FLOOR_TEXTURE;
			}
			else if ( oldSelection instanceof Wall )
			{
				texture = WALL_TEXTURE;
			}
			else if ( oldSelection instanceof TetraHedron )
			{
				texture = HEDRON_TEXTURE;
			}

			setTextureOfAllFaces( oldObject3D , texture );
			viewModel.updateNode( oldSelection );
		}

		if ( newSelection != null )
		{
			final Object3D newObject3D = (Object3D)viewModel.getNode3D( newSelection );
			setTextureOfAllFaces( newObject3D , SELECTION_TEXTURE );
			viewModel.updateNode( newSelection );
		}
	}

	/**
	 * Updates face selection from <code>oldFace</code> to <code>newFace</code>.
	 * The old face (if not <code>null</code>) is given back the normal texture,
	 * while the new face (if not <code>null</code>) is given the color of a
	 * selected face.
	 *
	 * @param   oldFace   Previous selection (may be <code>null</code>).
	 * @param   newFace   New selection (may be <code>null</code>).
	 */
	private void updateFaceSelection( final PaintableTriangle oldFace , final PaintableTriangle newFace )
	{
		final ViewModel viewModel = _viewModel;

		TetraHedron oldHedron = null;
		if ( oldFace != null )
		{
			oldHedron = oldFace.getTetraHedron();

			final Object3D hedron3D = (Object3D)viewModel.getNode3D( oldHedron );

			setTextureOfAllFaces( hedron3D , SELECTION_TEXTURE );
		}

		TetraHedron newHedron = null;
		if ( newFace != null )
		{
			newHedron = newFace.getTetraHedron();

			final Object3D hedron3D = (Object3D)viewModel.getNode3D( newHedron );
			final Face3D   face     = hedron3D.getFace( newFace.getFaceNumber() );

			face.setTexture( FACE_SELECTION_TEXTURE );
		}

		if ( oldHedron != null )
			viewModel.updateNode( oldHedron );

		if ( ( newHedron != null ) && ( newHedron != oldHedron ) )
			viewModel.updateNode( newHedron );
	}

	/**
	 * Set texture of all faces of the specified 3D object.
	 *
	 * @param   object      3D object whose face textures to set.
	 * @param   texture     Texture to set.
	 */
	private static void setTextureOfAllFaces( final Object3D object , final TextureSpec texture )
	{
		for ( int i = 0 ; i < object.getFaceCount() ; i++ )
		{
			final Face3D face = object.getFace( i );
			face.setTexture( texture );
		}
	}
}
