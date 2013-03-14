/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.model;

import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import junit.framework.*;

/**
 * Unit test for {@link Box3D}.
 *
 * @author Peter S. Heijnen
 */
public class TestBox3D
extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestBox3D.class.getName();

	/**
	 * Tests integrity of box geometry.
	 *
	 * @throws Exception if the test fails.
	 */
	public void testIntegrity()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testIntegrity" );

		final UVMap boxMap = new BoxUVMap( Scene.M );
		SceneIntegrityChecker.ensureIntegrity( new Box3D( 1.0, 1.0, 1.0, BasicAppearances.CYAN, null, BasicAppearances.WHITE, null, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.MAGENTA, boxMap, BasicAppearances.ALU_PLATE, boxMap ) );
		SceneIntegrityChecker.ensureIntegrity( new Box3D( 0.0, 1.0, 1.0, BasicAppearances.CYAN, null, BasicAppearances.WHITE, null, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.MAGENTA, boxMap, BasicAppearances.ALU_PLATE, boxMap ) );
		SceneIntegrityChecker.ensureIntegrity( new Box3D( 1.0, 0.0, 1.0, BasicAppearances.CYAN, null, BasicAppearances.WHITE, null, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.MAGENTA, boxMap, BasicAppearances.ALU_PLATE, boxMap ) );
		SceneIntegrityChecker.ensureIntegrity( new Box3D( 1.0, 1.0, 0.0, BasicAppearances.CYAN, null, BasicAppearances.WHITE, null, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.ALU_PLATE, boxMap, BasicAppearances.MAGENTA, boxMap, BasicAppearances.ALU_PLATE, boxMap ) );
	}
}
