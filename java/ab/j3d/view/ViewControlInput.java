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
package ab.j3d.view;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.control.ControlInput;
import ab.j3d.geom.Ray3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.model.Scene;

/**
 * The ViewInputTranslator subclasses {@link ControlInput} to provide
 * an {@link ControlInput} for a {@link View3D}.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class ViewControlInput
	extends ControlInput
	implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	/**
	 * {@link View3D} whose input events to monitor.
	 */
	private final View3D _view;

	/**
	 * Construct new ViewInputTranslator.
	 *
	 * @param   view    {@link View3D} whose input events to monitor.
	 */
	public ViewControlInput( final View3D view )
	{
		_view  = view;

		final Component component = view.getComponent();
		component.addKeyListener( this );
		component.addMouseListener( this );
		component.addMouseMotionListener( this );
		component.addMouseWheelListener( this );
	}

	/**
	 * Returns the {@link Projector} for the {@link View3D}.
	 *
	 * @return  The {@link Projector} for the {@link View3D}.
	 */
	protected Projector getProjector()
	{
		return _view.getProjector();
	}

	public List<Face3DIntersection> getIntersections( final Ray3D ray )
	{
		final List<Face3DIntersection> result = new LinkedList<Face3DIntersection>();

		final Scene scene = _view.getScene();

		for ( final Object nodeID : scene.getContentNodeIDs() )
		{
			final ContentNode node = scene.getContentNode( nodeID );
			final Node3D        node3D = node.getNode3D();

			final Matrix3D node2model = node.getTransform();

			final Node3DCollection<Object3D> subGraphNodes = new Node3DCollection<Object3D>();
			node3D.collectNodes( subGraphNodes , Object3D.class , node2model , false );

			for ( int i = 0 ; i < subGraphNodes.size() ; i++ )
			{
				final Object3D object       = subGraphNodes.getNode( i );
				final Matrix3D object2world = subGraphNodes.getMatrix( i );

				object.getIntersectionsWithRay( result , true , nodeID , object2world , ray );
			}
		}

		return result;
	}

	/**
	 * Get {@link View3D} being monitored for input events.
	 *
	 * @return  {@link View3D} being monitored for input events.
	 */
	public View3D getView()
	{
		return _view;
	}

	/**
	 * Returns the current view transform for the {@link View3D}.
	 *
	 * @return  the view transform for the {@link View3D}.
	 */
	protected Matrix3D getScene2View()
	{
		return _view.getScene2View();
	}

	/**
	 * Returns the current view transform for the {@link View3D}.
	 *
	 * @return  the view transform for the {@link View3D}.
	 */
	protected Matrix3D getView2Scene()
	{
		return _view.getView2Scene();
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyPressed( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyReleased( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link KeyEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link KeyEvent} that was dispatched
	 */
	public void keyTyped( final KeyEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseClicked( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseDragged( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseEntered( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseExited( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseMoved( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mousePressed( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseReleased( final MouseEvent event )
	{
		dispatchControlInputEvent( event );
	}

	/**
	 * Simply pass {@link MouseEvent} on to {@link #dispatchControlInputEvent}.
	 *
	 * @param   event   {@link MouseEvent} that was dispatched
	 */
	public void mouseWheelMoved( final MouseWheelEvent event )
	{
		dispatchControlInputEvent( event );
	}
}
