/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2006
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
package ab.j3d.view.control.planar;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewModelNode;

/**
 * @FIXME Need comment
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public abstract class SubPlanePathControl
	extends AbstractSubPlaneControl
{
	/** Points array. */
	private ArrayList<Point2D> _points = null;

	/**
	 * Construct new PlanarPathControl.
	 */
	protected SubPlanePathControl()
	{
	}

	public void mouseDragged( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		super.mouseDragged( event, viewModelNode, x, y );
		System.out.println( "eventMouseDragged = " + event );
	}

	public void mousePressed( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		_points.add( new Point2D.Double( x , y ) );
		super.mousePressed( event, viewModelNode, x, y );
		System.out.println( "eventMousePressed = " + event );
	}

	public void mouseReleased( final ControlInputEvent event , final ViewModelNode viewModelNode , final double x , final double y )
	{
		super.mouseReleased( event, viewModelNode, x, y );
		System.out.println( "eventMouseReleased = " + event );
	}


}
