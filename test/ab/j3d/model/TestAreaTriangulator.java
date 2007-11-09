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

import junit.framework.TestCase;

/**
 * Unit test for the {@link AreaTriangulator}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestAreaTriangulator
	extends TestCase
{
	public void testTriangulation()
	{
		final AreaTriangulator triangulator = new AreaTriangulator();
		TriangulatorTester.testExtremeShapes( triangulator );
	}
}
