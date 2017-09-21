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

import java.awt.*;

/**
 * An {@link ViewOverlay} is allowed to paint over an already rendered view.
 * <p />
 * The method {@link #paintOverlay} is called after the view has been rendered,
 * after which the painters can paint whatever they wish on the rendered image.
 * <p />
 * An example of this is a bounding box that is drawn around a selected object.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface ViewOverlay
{
	/**
	 * Add view to overlay. Called by the view when it starts using this overlay.
	 *
	 * @param   view    View to add.
	 */
	void addView( View3D view );

	/**
	 * Remove view from overlay. Called by the view when it no longer uses this
	 * overlay.
	 *
	 * @param   view    View to remove.
	 */
	void removeView( View3D view );

	/**
	 * Called after a view has rendered the scene, to allow this overlay to add
	 * content on top of the rendered image.
	 *
	 * @param   view    {@link View3D} that rendered the scene.
	 * @param   g2d     {@link Graphics2D} to paint the overlay.
	 */
	void paintOverlay( View3D view, Graphics2D g2d );
}
