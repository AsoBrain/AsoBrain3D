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
 * Receives notification from a {@link View3D} whenever rendering of a frame
 * starts or ends.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ViewListener
{
	/**
	 * Called just before the view starts to render a frame.
	 *
	 * @param   view    View that renders the frame.
	 */
	void beforeFrame( View3D view );

	/**
	 * Called just after the view finishes rendering a frame.
	 *
	 * @param   view    View that renders the frame.
	 */
	void afterFrame( View3D view );
}
