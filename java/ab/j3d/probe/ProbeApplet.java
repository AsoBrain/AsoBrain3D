/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2009-2009 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.probe;

import java.awt.BorderLayout;
import javax.swing.JApplet;

/**
 * Runs the 3D capabilities probe as an applet.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ProbeApplet
	extends JApplet
{
	private ProbeUI _ui;

	/**
	 * Construct new probe applet.
	 */
	public ProbeApplet()
	{
	}

	@Override
	public void init()
	{
		final ProbeUI ui = new ProbeUI();
		_ui = ui;

		setLayout( new BorderLayout() );
		add( ui , BorderLayout.CENTER );
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
	}
}
