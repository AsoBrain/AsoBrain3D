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
package ab.j3d.awt.view;

import java.util.*;
import javax.swing.*;

import ab.j3d.control.*;
import ab.j3d.view.*;

/**
 * This can be used as container for a 3D view with some controls.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class View3DPanel
{
	/**
	 * Create tool bar to control this view.
	 *
	 * @param   view    View to create tool bar for.
	 * @param   locale  Preferred locale for internationalization.
	 *
	 * @return  Tool bar.
	 */
	public static JToolBar createToolBar( final View3D view, final Locale locale )
	{
		final String label = view.getLabel();

		final JToolBar toolbar = new JToolBar( label );

		if ( label != null )
		{
			toolbar.add( new JLabel( view.getLabel() + ": " ) );
		}

		final CameraControl cameraControl = view.getCameraControl();
		if ( cameraControl != null )
		{
			toolbar.add( new ZoomToFitAction( locale, cameraControl ) );
		}

		toolbar.add( new RenderingPolicyComboBox( locale, view, view.getRenderingPolicy() ) );
		toolbar.add( new GridCheckBox( locale, view ) );

		return toolbar;
	}
}
