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
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.numdata.oss.ui.WindowTools;

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
		ui.setPreferredSize( new Dimension( 800 , 400 ) );

		final JFrame frame = new JFrame( "3D Capabilities Probe" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		frame.setLayout( new BorderLayout() );
		frame.add( ui , BorderLayout.CENTER );
		frame.pack();
		WindowTools.center( frame );
		frame.setVisible( true );
	}

	/**
	 * Constructs a new instance, so it can be run on the Swing EDT.
	 */
	private ProbeApp()
	{
	}
}
