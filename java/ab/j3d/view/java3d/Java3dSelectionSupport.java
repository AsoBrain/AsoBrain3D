/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.java3d;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;

import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;

import ab.j3d.view.NodeSelectionListener;
import ab.j3d.view.SelectionSupport;

/**
 * Java3dSelectionSupport implements {@link SelectionSupport} for Java 3D. It
 * allows {@link NodeSelectionListener}s to receive updates when a {@link
 * Node3D} has been selected.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public class Java3dSelectionSupport
extends SelectionSupport
implements MouseListener
{
	/**
	 * The {@link Canvas3D} on which the user clicks
	 */
	private Canvas3D _canvas;

	/**
	 * The {@link Java3dUniverse} in which nodes should be selected.
	 */
	private Java3dUniverse _universe;

	/**
	 * The {@link PickCanvas} does the actual picking for us.
	 */
	private PickCanvas _pickCanvas;


	/**
	 * Construct a new Java3dSelectionSupport.
	 *
	 * @param canvas   The {@link Canvas3D} on which the user clicks
	 * @param universe The {@link Java3dUniverse} in which nodes should be
	 *                 selected.
	 */
	public Java3dSelectionSupport( final Canvas3D canvas, final Java3dUniverse universe )
	{
		_canvas = canvas;
		_canvas.addMouseListener( this );

		_universe = universe;

		_pickCanvas = new PickCanvas( _canvas, _universe.getLocale() );
		_pickCanvas.setMode( PickCanvas.GEOMETRY );
	}


	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a
	 * component.
	 */
	public void mouseClicked( MouseEvent e )
	{
		_pickCanvas.setShapeLocation( e );

		PickResult result = _pickCanvas.pickClosest();
		if ( result == null )
		{
			return;
		}

		Node node = result.getObject();
		while ( node.getUserData() instanceof Node )
		{
			node = (Node)node.getUserData();
		}
		Object id = node.getUserData();

		fireEvent( this, id );
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed( MouseEvent e )
	{
	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased( MouseEvent e )
	{
	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered( MouseEvent e )
	{
	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited( MouseEvent e )
	{
	}
}
