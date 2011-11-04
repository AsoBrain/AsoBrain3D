/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.demo;

import java.awt.*;
import java.net.*;
import javax.swing.*;

/**
 * Applet that runs the {@link AsoBrainAnimation} example.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class AsoBrainAnimationApplet
	extends JApplet
{
	/**
	 * Demo application.
	 */
	private AsoBrainAnimation _example = null;

	/**
	 * Location of images.
	 */
	private URL _imagesUrl = null;

	@Override
	public void init()
	{
		final String images = getParameter( "images" );
		if ( images != null )
		{
			try
			{
				_imagesUrl = new URL( images );
			}
			catch ( MalformedURLException e )
			{
				System.err.println( "Malformed images URL: " + images );
				System.err.println();
				System.err.println( "(" + e + ')' );
			}
		}

		final AsoBrainAnimation example = new AsoBrainAnimation();
		final Component content = example.init( getParameter( "engine" ) );
		_example = example;

		setLayout( new BorderLayout() );
		add( content, BorderLayout.CENTER );
	}

	@Override
	public void start()
	{
		_example.start();
	}

	@Override
	public void stop()
	{
		_example.stop();
	}

	@Override
	public void destroy()
	{
		_example = null;
	}
}
