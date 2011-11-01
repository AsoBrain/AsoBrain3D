/* $Id$
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

/**
 * This interface describes a filter for {@link RenderStyle}s. These filters
 * are used to define rendering styles in the 3D scene.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface RenderStyleFilter
{
	/**
	 * Apply filter to existing style.
	 *
	 * IMPORTANT: If the style is modified, it should be cloned before doing so.
	 *
	 * @param   style       Style to filter (never <code>null</code>).
	 * @param   context     Context object (never <code>null</code>).
	 *
	 * @return  Filtered style.
	 */
	RenderStyle applyFilter( RenderStyle style, Object context );
}
