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
package ab.j3d.probe;

import java.awt.*;
import java.lang.reflect.*;
import javax.swing.*;

/**
 * Runs the 3D capabilities probe as an applet.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ProbeApplet
	extends JApplet
{
	/**
	 * User interface component.
	 */
	private ProbeUI _ui;

	/**
	 * Construct new probe applet.
	 */
	public ProbeApplet()
	{
		_ui = null;
	}

	@Override
	public void init()
	{
		try
		{
			SwingUtilities.invokeAndWait( new Runnable()
			{
				public void run()
				{
					final ProbeUI ui = new ProbeUI();
					_ui = ui;

					setLayout( new BorderLayout() );
					add( ui, BorderLayout.CENTER );
				}
			} );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}
		catch ( InvocationTargetException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void start()
	{
	}

	@Override
	public void stop()
	{
	}

	@Override
	public void destroy()
	{
		final ProbeUI ui = _ui;
		if ( ui != null )
		{
			ui.dispose();
			_ui = null;
		}
	}
}
