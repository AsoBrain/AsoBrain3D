/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2008
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
