/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2010-2010
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

import java.awt.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Specifies the properties of a grid for use in a 3D view.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class Grid
{
	/**
	 * Grid enabled/disabled flag.
	 */
	private boolean _enabled;

	/**
	 * Transforms grid to world coordinates.
	 */
	@NotNull
	private Matrix3D _grid2wcs;

	/**
	 * Bounds of grid in cell units.
	 */
	@NotNull
	private Rectangle _bounds;

	/**
	 * Size of each grid cell in world units.
	 */
	private int _cellSize;

	/**
	 * If set, highlight X/Y grid axes.
	 */
	private boolean _highlightAxes;

	/**
	 * Interval for highlighted grid lines. Less or equal to zero if
	 * highlighting is disabled.
	 */
	private int _highlightInterval;

	/**
	 * Construct new Grid.
	 */
	public Grid()
	{
		_enabled = false;
		_grid2wcs = Matrix3D.INIT;
		_bounds = new Rectangle( -100, -100, 200, 200 );
		_cellSize = 1;
		_highlightAxes = true;
		_highlightInterval = 10;
	}

	/**
	 * Get grid enabled option.
	 *
	 * @return  <code>true</code> if grid is enabled;
	 *          <code>false</code> if grid is disabled.
	 */
	public boolean isEnabled()
	{
		return _enabled;
	}

	/**
	 * Get grid enabled option.
	 *
	 * @param   enabled     Grid enabled.
	 */
	public void setEnabled( final boolean enabled )
	{
		_enabled = enabled;
	}

	/**
	 * Get tranform from grid to world coordinates.
	 *
	 * @return  Transform from grid to world coordinates.
	 */
	@NotNull
	public Matrix3D getGrid2wcs()
	{
		return _grid2wcs;
	}

	/**
	 * Set tranform from grid to world coordinates.
	 *
	 * @param   grid2wcs    Transforms grid to world coordinates.
	 */
	public void setGrid2wcs( @NotNull final Matrix3D grid2wcs )
	{
		_grid2wcs = grid2wcs;
	}

	/**
	 * Get bounds of grid in cell units.
	 *
	 * @return  Bounds of grid in cell units.
	 */
	@NotNull
	public Rectangle getBounds()
	{
		return _bounds;
	}

	/**
	 * Set bounds of grid in cell units.
	 *
	 * @param   bounds  Bounds of grid in cell units.
	 *
	 * @throws  IllegalArgumentException if the width or height of the given
	 *          bounds is negative or zero.
	 */
	public void setBounds( @NotNull final Rectangle bounds )
	{
		if ( ( bounds.width <= 0 ) || ( bounds.height <= 0 ) )
		{
			throw new IllegalArgumentException( "bounds: " + bounds );
		}

		_bounds = bounds;
	}

	/**
	 * Get size of each grid cell in world units.
	 *
	 * @return  Size of each grid cell in world units.
	 */
	public int getCellSize()
	{
		return _cellSize;
	}

	/**
	 * Size of each grid cell in world units.
	 *
	 * @param   cellSize    Size of each grid cell in world units.
	 *
	 * @throws  IllegalArgumentException if the specified cell size is negative
	 *          or zero.
	 */
	public void setCellSize( final int cellSize )
	{
		if ( cellSize <= 0 )
		{
			throw new IllegalArgumentException( "cellSize: " + cellSize );
		}

		_cellSize = cellSize;
	}

	/**
	 * Get X/Y grid axes highlight option.
	 *
	 * @return  <code>true</code> if X/Y grid axes are highlighted;
	 *          <code>false</code> otherwise.
	 */
	public boolean isHighlightAxes()
	{
		return _highlightAxes;
	}

	/**
	 * Set X/Y grid axes highlight option.
	 *
	 * @param   highlightAxes   If set, highlight X/Y grid axes.
	 */
	public void setHighlightAxes( final boolean highlightAxes )
	{
		_highlightAxes = highlightAxes;
	}

	/**
	 * Get interval for highlighted grid lines.
	 *
	 * @return  Interval for highlighted grid lines;
	 *          <= 0 if disabled.
	 */
	public int getHighlightInterval()
	{
		return _highlightInterval;
	}

	/**
	 * Set interval for highlighted grid lines.
	 *
	 * @param   highlightInterval   Interval for highlighted grid lines
	 *                              (<= 0 to disable).
	 */
	public void setHighlightInterval( final int highlightInterval )
	{
		_highlightInterval = highlightInterval;
	}
}
