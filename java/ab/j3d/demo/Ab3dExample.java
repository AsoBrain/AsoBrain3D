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

import ab.j3d.view.*;
import ab.j3d.view.java2d.*;
import ab.j3d.view.jogl.*;

/**
 * Utility class for AsoBrain 3D demo applications.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Ab3dExample
{
	/**
	 * Create render engine for this example. An engine can be specified using
	 * the 'engineName' applet parameter. Possible values:
	 * <dl>
	 *  <dt>default (also if invalid name is specified)</dt>
	 *  <dd>JOGL renderer with default configuration options.</dd>
	 *  <dt>safe</dt>
	 *  <dd>JOGL renderer with all optional features disabled ('safe mode').</dd>
	 *  <dt>lucious</dt>
	 *  <dd>JOGL renderer with all features enabled.</dd>
	 *  <dt>2d</dt>
	 *  <dd>Java 2D renderer (-very- limited).</dd>
	 * </dl>
	 *
	 * @param   engineName  Name of engine to create.
	 *
	 * @return  Render engine.
	 */
	public static RenderEngine createRenderEngine( final String engineName )
	{
		final RenderEngine engine;

		if ( "2d".equals( engineName ) )
		{
			engine = new Java2dEngine( Color.BLACK );
		}
		else if ( "safe".equals( engineName ) )
		{
			engine = new JOGLEngine( JOGLConfiguration.createSafeInstance() );
		}
		else if ( "lucious".equals( engineName ) )
		{
			engine = new JOGLEngine( JOGLConfiguration.createLuciousInstance() );
		}
		else /* default */
		{
			engine = new JOGLEngine( JOGLConfiguration.createDefaultInstance() );
		}

		return engine;
	}
}
