/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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

import java.awt.Color;

import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * View model implementation for JOGL.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLModel
	extends ViewModel
{
	/**
	 * Background color for the model.
	 */
	private final Color _background;

	/**
	 * Construct new Java 2D view model using {@link ViewModel#MM} units.
	 */
	public JOGLModel()
	{
		this( MM , null );
	}

	/**
	 * Construct new Java 2D view model.
	 *
	 * @param   unit    Unit scale factor (e.g. {@link ViewModel#MM}).
	 */
	public JOGLModel( final double unit )
	{
		this( unit , null );
	}

	/**
	 * Construct new Java 2D view model.
	 *
	 * @param   unit        Unit scale factor (e.g. {@link ViewModel#MM}).
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 */
	public JOGLModel( final double unit , final Color background )
	{
		super( unit );

		_background = background;
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

	public ViewModelView createView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final JOGLView view = new JOGLView( this , _background , id );
		addView( view );

		return view;
	}
}