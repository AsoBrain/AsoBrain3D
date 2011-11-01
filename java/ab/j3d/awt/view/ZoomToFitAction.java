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

import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.control.*;

/**
 * This action can be used to perform 'Zoom to fit' on a camera control.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ZoomToFitAction
	extends AbstractAction
{
	/**
	 * Camera control to perform action on.
	 */

	private final CameraControl _cameraControl;

	/**
	 * Create action.
	 *
	 * @param   locale          Locale to use.
	 * @param   cameraControl   Camera control to perform action on.
	 */
	public ZoomToFitAction( final Locale locale, final CameraControl cameraControl )
	{
		_cameraControl = cameraControl;
		final ResourceBundle bundle = ResourceBundle.getBundle( "LocalStrings", locale );

		putValue( ACTION_COMMAND_KEY, CameraControl.ZOOM_TO_FIT );
		putValue( NAME, bundle.getString( CameraControl.ZOOM_TO_FIT ) );
		putValue( SHORT_DESCRIPTION, bundle.getString( CameraControl.ZOOM_TO_FIT + "Tip" ) );
		putValue( MNEMONIC_KEY, Integer.valueOf( KeyEvent.VK_Z ) );

		final URL iconUrl = CameraControl.class.getResource( "/ab3d/resetView-16x16.gif" );
		if ( iconUrl != null )
		{
			putValue( SMALL_ICON, new ImageIcon( iconUrl ) );
		}
	}

	public void actionPerformed( final ActionEvent e )
	{
		_cameraControl.zoomToFit();
	}
}
