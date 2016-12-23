/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.demo;

import java.awt.*;

import ab.j3d.awt.view.*;
import ab.j3d.awt.view.java2d.*;
import ab.j3d.view.*;

/**
 * Utility class for AsoBrain 3D demo applications.
 *
 * @author  Peter S. Heijnen
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
	 *  <dt>luscious</dt>
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
		else
		{
			final TextureLibrary textureLibrary = new ClassLoaderTextureLibrary( Ab3dExample.class.getClassLoader() );
			if ( "safe".equals( engineName ) )
			{
				engine = RenderEngineFactory.createJOGLEngine( textureLibrary, JOGLConfiguration.createSafeInstance() );
			}
			else if ( "luscious".equals( engineName ) )
			{
				engine = RenderEngineFactory.createJOGLEngine( textureLibrary, JOGLConfiguration.createLusciousInstance() );
			}
			else /* default */
			{
				engine = RenderEngineFactory.createJOGLEngine( textureLibrary, JOGLConfiguration.createDefaultInstance() );
			}
		}

		return engine;
	}
}
