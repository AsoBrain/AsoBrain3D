/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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

import java.awt.Color;

import ab.j3d.view.BSPTree;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

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
	 * Background color for the model.
	 */
	private final Color _background;

	/**
	 * Binary Space Partitioning Tree ({@link BSPTree}) of this model.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 */
	private final BSPTree _bspTree;

	/**
	 * This internal flag is set to indicate that the scene is
	 * changed, so the {@link BSPTree} needs to be re-calculated.
	 *
	 * @see #updateViews
	 */
	private boolean _bspTreeDirty;

	/**
	 * Construct new Java 2D view model using {@link ViewModel#MM} units.
	 */
	public Java2dModel()
	{
		this( MM , null );
	}

	/**
	 * Construct new Java 2D view model.
	 *
	 * @param   unit    Unit scale factor (e.g. {@link ViewModel#MM}).
	 */
	public Java2dModel( final double unit )
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
	public Java2dModel( final double unit , final Color background )
	{
		super( unit );

		_background   = background;
		_bspTree      = new BSPTree();
		_bspTreeDirty = true;
	}

	protected void initializeNode( final ViewModelNode node )
	{
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{

		updateViews();
	}

	public void updateOverlay()
	{
		updateViews();
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		updateViews();
	}

	public void updateViews()
	{
		_bspTreeDirty = true;
		super.updateViews();
	}

	public ViewModelView createView()
	{
		final Java2dView view = new Java2dView( this , _background );
		addView( view );
		return view;
	}

	/**
	 * Binary Space Partitioning Tree ({@link BSPTree}) of this model.
	 * <p />
	 * The tree is only calculated when the scene changes (indicated by
	 * the {@link #_bspTreeDirty} field).
	 *
	 * @return  The Binary Space Partitioning Tree of this model.
	 */
	public BSPTree getBspTree()
	{
		final BSPTree result = _bspTree;

		if ( _bspTreeDirty )
		{
			result.reset();
			result.addScene( getScene() );
			result.build();

			_bspTreeDirty = false;
		}

		return result;
	}
}
