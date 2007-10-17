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
	public static TriangulatorFactory newInstance()
	{
		Class<? extends Triangulator> triangulatorClass = null;

		try
		{
			triangulatorClass = GLUTriangulator.class;
		}
		catch ( Error e )
		{
			// @TODO Determine which specific error has to be caught.
			// Continue with the other candidates.
		}

		if ( triangulatorClass == null )
		{
			triangulatorClass = AreaTriangulator.class;
		}

		return new TriangulatorFactory( triangulatorClass );
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
