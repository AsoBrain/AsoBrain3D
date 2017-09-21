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
import javax.swing.*;


/**
 * Runs the 3D capabilities probe as an application.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ProbeApp
	implements Runnable
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		SwingUtilities.invokeLater( new ProbeApp() );
	}

	public void run()
	{
		final ProbeUI ui = new ProbeUI();
		ui.setPreferredSize( new Dimension( 800, 400 ) );

		final JFrame frame = new JFrame( "3D Capabilities Probe" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setLayout( new BorderLayout() );
		frame.add( ui, BorderLayout.CENTER );
		frame.pack();
		final Toolkit toolkit = frame.getToolkit();
		final GraphicsConfiguration graphicsConfiguration = frame.getGraphicsConfiguration();
		final Rectangle screenBounds = graphicsConfiguration.getBounds();
		final Insets screenInsets = toolkit.getScreenInsets( graphicsConfiguration );
		frame.setLocation( screenBounds.x + ( screenBounds.width + screenInsets.left + screenInsets.right - frame.getWidth() ) / 2, screenBounds.y + ( screenBounds.height + screenInsets.top + screenInsets.bottom - frame.getHeight() ) / 2 );
		frame.setVisible( true );
	}

	/**
	 * Constructs a new instance, so it can be run on the Swing EDT.
	 */
	private ProbeApp()
	{
	}
}
