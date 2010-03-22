/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2010 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.probe;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;

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
				@Override
				public void run()
				{
					final ProbeUI ui = new ProbeUI();
					_ui = ui;

					setLayout( new BorderLayout() );
					add( ui , BorderLayout.CENTER );
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
