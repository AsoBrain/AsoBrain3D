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
package ab.j3d.view;

import java.lang.reflect.*;

import ab.j3d.awt.view.*;
import ab.j3d.awt.view.java2d.*;

/**
 * Factory for creating render engines.
 *
 * @author Gerrit Meinders
 */
public class RenderEngineFactory
{
	/**
	 * Constructs a new instance.
	 */
	private RenderEngineFactory()
	{
	}

	/**
	 * Creates a render engine based on JOGL. If no such engine is not available,
	 * an exception is thrown.
	 *
	 * @param textureLibrary Texture library.
	 * @param configuration Configuration settings.
	 *
	 * @return JOGL render engine.
	 */
	public static RenderEngine createJOGLEngine( final TextureLibrary textureLibrary, final JOGLConfiguration configuration )
	{
		try
		{
			final Class<?> clazz = Class.forName( "ab.j3d.awt.view.jogl.JOGLEngine" );
			final Class<? extends RenderEngine> engineClass = clazz.asSubclass( RenderEngine.class );
			final Constructor<? extends RenderEngine> constructor = engineClass.getConstructor( TextureLibrary.class, JOGLConfiguration.class );
			return constructor.newInstance( textureLibrary, configuration );
		}
		catch ( ClassNotFoundException ignored )
		{
			return new Java2dEngine();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
