/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2008-2010
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

import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;

/**
 * This action calls {@link View3D#zoomToFitScene()}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ZoomToFitAction
	extends BasicAction
{
	/**
	 * The {@link View3D} this action belongs to.
	 */
	private View3D _view;

	/**
	 * Create action.
	 *
	 * @param   locale  Preferred locale for internationalization.
	 * @param   view    View to create action for.
	 */
	public ZoomToFitAction( final Locale locale , final View3D view )
	{
		super( ResourceBundleTools.getBundle( ZoomToFitAction.class , locale ) , "zoomToFit" );
		_view = view;
	}

	@Override
	public void run()
	{
		_view.zoomToFitScene();
	}
}