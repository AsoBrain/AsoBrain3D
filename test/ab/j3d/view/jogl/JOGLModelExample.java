/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
package ab.j3d.view.jogl;

import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.control.ControlInputEvent;
import ab.j3d.view.ViewControlInput;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelExample;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.control.planar.PlaneControl;
import ab.j3d.view.control.planar.PlaneMoveControl;

/**
 * Example program for the JOGL view model implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLModelExample
	extends ViewModelExample
{
	/**
	 * Number of cells in grid.
	 */
	private static final int GRID_CELL_COUNT = 100;

	/**
	 * Size of each grid cell.
	 */
	private static final int GRID_CELL_SIZE = 100;

	/**
	 * Interval to use for highlighting grid lines.
	 */
	private static final int GRID_HIGHLIGHT_INTERVAL = 10;

	/**
	 * Construct new JOGLModelExample.
	 */
	public JOGLModelExample()
	{
		super( new JOGLModel( ViewModel.MM )
			{
				public ViewModelView createView( final Object id )
				{
					final JOGLView result = (JOGLView)super.createView( id );
					result.drawGrid( Matrix3D.INIT , GRID_CELL_COUNT , GRID_CELL_SIZE , true , GRID_HIGHLIGHT_INTERVAL );
					return result;
				}
			} );

	}

	protected PlaneControl createPlaneControl( final Matrix3D plane2wcs )
	{
		return new PlaneMoveControl( plane2wcs , true )
			{
				public void mousePressed( final ControlInputEvent event , final ViewModelNode viewModelNode , final Vector3D wcsPoint )
				{
					super.mousePressed( event , viewModelNode , wcsPoint );

					viewModelNode.setAlternate( true );

					final ViewControlInput controlInput = (ViewControlInput)event.getSource();
					final JOGLView view = (JOGLView)controlInput.getView();

					final Matrix3D plane2wcs = getPlane2Wcs();
					final Matrix3D node2world = viewModelNode.getTransform();
					view.drawGrid( plane2wcs.setTranslation( node2world.xo , node2world.yo , node2world.zo ) , GRID_CELL_COUNT , GRID_CELL_SIZE , true , GRID_HIGHLIGHT_INTERVAL );
				}

				public void mouseReleased( final ControlInputEvent event , final ViewModelNode viewModelNode , final Vector3D wcsPoint )
				{
					super.mouseReleased( event , viewModelNode , wcsPoint );

					viewModelNode.setAlternate( false );

					final ViewControlInput controlInput = (ViewControlInput)event.getSource();

					final JOGLView view = (JOGLView)controlInput.getView();
					view.drawGrid( Matrix3D.INIT , GRID_CELL_COUNT , GRID_CELL_SIZE , true , GRID_HIGHLIGHT_INTERVAL );
				}
			};
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new JOGLModelExample();
	}
}