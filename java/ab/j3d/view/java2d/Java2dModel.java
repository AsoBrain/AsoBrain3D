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
package ab.j3d.view.java2d;

import java.awt.Component;

import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.control.Control;

/**
 * Java 2D implementation of view model.
 *
 * @author G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dModel
	extends ViewModel
{

	/**
	 * Construct new Java 2D view model using {@link ViewModel#MM} units.
	 */
	public Java2dModel()
	{
		this( MM );
	}

	/**
	 * Construct new Java 2D view model.
	 *
	 * @param   unit    Unit scale factor (e.g. {@link ViewModel#MM}).
	 */
	public Java2dModel( final double unit )
	{
		super( unit );
	}

	protected void initializeNode( final ViewModelNode node )
	{
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{

		updateViews();
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		updateViews();
	}

	public Component createView( final Object id , final ViewControl viewControl )
	{
		final Java2dView view = new Java2dView( this , id , viewControl );

		addView( view );
		return view.getComponent();
	}

	/**
	 * Returns wether or not this view supports {@link Control}s. For a
	 * {@link Java2dModel}, this is always <code>true</code>.
	 *
	 * @return  <code>true</code>, because a {@link Java2dModel} supports
	 *          {@link Control}s.
	 */
	public boolean supportsControls()
	{
		return true;
	}

}
