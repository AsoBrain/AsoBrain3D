/*
 * (C) Copyright Numdata BV 2013-2013 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import java.lang.reflect.*;

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
	 * @param configuration Configuration settings.
	 *
	 * @return JOGL render engine.
	 */
	public static RenderEngine createJOGLEngine( final JOGLConfiguration configuration )
	{
		try
		{
			final Class<?> clazz = Class.forName( "ab.j3d.awt.view.jogl.JOGLEngine" );
			final Class<? extends RenderEngine> engineClass = clazz.asSubclass( RenderEngine.class );
			final Constructor<? extends RenderEngine> constructor = engineClass.getConstructor( JOGLConfiguration.class );
			return constructor.newInstance( configuration );
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
