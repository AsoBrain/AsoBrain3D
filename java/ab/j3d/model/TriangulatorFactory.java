/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

/**
 * Creates triangulators, choosing the appropriate implementation based on
 * available libraries.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TriangulatorFactory
{
	private static boolean neverInstantiated = true;

	public static TriangulatorFactory newInstance()
	{
		Class<? extends Triangulator> triangulatorClass = GLUTriangulator.class;

		try
		{
			triangulatorClass.newInstance();
			if ( neverInstantiated )
				System.out.println( "TriangulatorFactory is using 'GLU' triangulator." );
		}
		catch ( Throwable t )
		{
			// @TODO Determine which specific error has to be caught.
			// Continue with the other candidates.
			triangulatorClass = null;
		}

		if ( triangulatorClass == null )
		{
			if ( neverInstantiated )
				System.out.println( "TriangulatorFactory is using 'Area' triangulator." );
			triangulatorClass = AreaTriangulator.class;
		}

		final TriangulatorFactory result = new TriangulatorFactory( triangulatorClass );
		neverInstantiated = false;
		return result;
	}

	/**
	 * Type of triangulators returned by the factory.
	 */
	private final Class<? extends Triangulator> _triangulatorClass;

	/**
	 * Construct new triangulator factory.
	 */
	private TriangulatorFactory( final Class<? extends Triangulator> triangulatorClass )
	{
		_triangulatorClass = triangulatorClass;
	}

	/**
	 * Returns a new triangulator instance.
	 *
	 * @return  Triangulator.
	 */
	public Triangulator newTriangulator()
	{
		try
		{
			return _triangulatorClass.newInstance();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
