/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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

import java.awt.Graphics2D;

/**
 * An {@link OverlayPainter} is allowed to paint over an allready rendered view.
 * The method {@link #paint} is called after the view has been rendered, after
 * which the painters can paint whatever they wish on the view component. An
 * example of this is a bounding box that is drawn around a selected object.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface OverlayPainter
{
	/**
	 * Called after a view has been rendered, after which the painters can paint
	 * whatever they wish on the view component.
	 *
	 * @param   viewModel   {@link ViewModel} which contains the objects in the
	 *                      scene.
	 * @param   view        {@link ViewModelView} which has rendered the scene.
	 * @param   g2d         {@link Graphics2D} object which can do the painting.
	 */
	void paint( final ViewModel viewModel , final ViewModelView view , final Graphics2D g2d );
}
