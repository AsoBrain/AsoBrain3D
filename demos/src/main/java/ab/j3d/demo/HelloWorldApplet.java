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
import javax.swing.*;

/**
 * Applet that runs the {@link HelloWorld} example.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class HelloWorldApplet
	extends JApplet
{
	@Override
	public void init()
	{
		final HelloWorld example = new HelloWorld();
		final Component content = example.init( getParameter( "engine" ) );

		setLayout( new BorderLayout() );
		add( content, BorderLayout.CENTER );
	}
}
