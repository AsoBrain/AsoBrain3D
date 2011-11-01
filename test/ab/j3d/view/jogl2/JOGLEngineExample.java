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
package ab.j3d.view.jogl2;

import ab.j3d.view.RenderEngineExample;

/**
 * Example program for the JOGL render engine implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLEngineExample
	extends RenderEngineExample
{
	/**
	 * Construct new JOGLModelExample.
	 */
	public JOGLEngineExample()
	{
		super( createJOGLEngine() );
	}

	private static JOGLEngine createJOGLEngine()
	{
		final JOGLConfiguration configuration = new JOGLConfiguration();
		configuration.setPerPixelLightingEnabled( true );
		configuration.setReflectionMapsEnabled( true );

		final JOGLEngine result = new JOGLEngine( configuration );
		return result;
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new JOGLEngineExample();
	}
}
