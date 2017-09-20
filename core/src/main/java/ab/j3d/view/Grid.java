/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
	private boolean _enabled = false;

	/**
	 * Transforms grid to world coordinates.
	 */
	@NotNull
	private Matrix3D _grid2wcs = Matrix3D.IDENTITY;


	/**
	 * Minimum X coordinate of grid in cell units.
	 */
	private int _minimumX = -100;

	/**
	 * Minimum Y coordinate of grid in cell units.
	 */
	private int _minimumY = -100;

	/**
	 * Maximum X coordinate of grid in cell units.
	 */
	private int _maximumX = 100;

	/**
	 * Maximum Y coordinate of grid in cell units.
	 */
	private int _maximumY = 100;

	/**
	 * Size of each grid cell in world units.
	 */
	private int _cellSize = 1;

	/**
	 * If set, highlight X/Y grid axes.
	 */
	private boolean _highlightAxes = true;

	/**
	 * Interval for highlighted grid lines. Less or equal to zero if
	 * highlighting is disabled.
	 */
	private int _highlightInterval = 10;

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
	 * Get transform from grid to world coordinates.
	 *
	 * @return  Transform from grid to world coordinates.
	 */
	@NotNull
	public Matrix3D getGrid2wcs()
	{
		return _grid2wcs;
	}

	/**
	 * Set transform from grid to world coordinates.
	 *
	 * @param   grid2wcs    Transforms grid to world coordinates.
	 */
	public void setGrid2wcs( @NotNull final Matrix3D grid2wcs )
	{
		_grid2wcs = grid2wcs;
	}

	/**
	 * Set bounds of grid in cell units.
	 *
	 * @param   minimumX    Minimum X coordinate of grid in cell units.
	 * @param   minimumY    Minimum Y coordinate of grid in cell units.
	 * @param   maximumX    Maximum X coordinate of grid in cell units.
	 * @param   maximumY    Maximum Y coordinate of grid in cell units.
	 *
	 * @throws  IllegalArgumentException if the width or height of the given
	 *          bounds is negative or zero.
	 */
	public void setBounds( final int minimumX, final int minimumY, final int maximumX, final int maximumY )
	{
		_minimumX = minimumX;
		_minimumY = minimumY;
		_maximumX = maximumX;
		_maximumY = maximumY;
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

	/**
	 * Get minimum X coordinate of grid in cell units.
	 *
	 * @return  Minimum X coordinate of grid in cell units.
	 */
	public int getMinimumX()
	{
		return _minimumX;
	}

	/**
	 * Set minimum X coordinate of grid in cell units.
	 *
	 * @param   minimumX    Minimum X coordinate of grid in cell units.
	 */
	public void setMinimumX( final int minimumX )
	{
		_minimumX = minimumX;
	}

	/**
	 * Get minimum Y coordinate of grid in cell units.
	 *
	 * @return  Minimum Y coordinate of grid in cell units.
	 */
	public int getMinimumY()
	{
		return _minimumY;
	}

	/**
	 * Set minimum Y coordinate of grid in cell units.
	 *
	 * @param   minimumY    Minimum Y coordinate of grid in cell units.
	 */
	public void setMinimumY( final int minimumY )
	{
		_minimumY = minimumY;
	}

	/**
	 * Get maximum X coordinate of grid in cell units.
	 *
	 * @return  Maximum X coordinate of grid in cell units.
	 */
	public int getMaximumX()
	{
		return _maximumX;
	}

	/**
	 * Set maximum X coordinate of grid in cell units.
	 *
	 * @param   maximumX    Maximum X coordinate of grid in cell units.
	 */
	public void setMaximumX( final int maximumX )
	{
		_maximumX = maximumX;
	}

	/**
	 * Get maximum Y coordinate of grid in cell units.
	 *
	 * @return  Maximum Y coordinate of grid in cell units.
	 */
	public int getMaximumY()
	{
		return _maximumY;
	}

	/**
	 * Set maximum Y coordinate of grid in cell units.
	 *
	 * @param   maximumY    Maximum Y coordinate of grid in cell units.
	 */
	public void setMaximumY( final int maximumY )
	{
		_maximumY = maximumY;
	}
}
