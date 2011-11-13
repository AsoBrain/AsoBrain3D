/*
 * $Id$
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
package ab.j3d.loader;

import java.io.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import ab.xml.*;
import junit.framework.*;

/**
 * Unit test for {@link ColladaWriter}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class TestColladaWriter
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestColladaWriter.class.getName();

	/**
	 * Tests that a COLLADA file can be written for a simple scene.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testWrite()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testWrite()" );

		final Scene scene = new Scene( Scene.M );
		final UVMap aluUVMap = new BoxUVMap( Scene.M );

		final Box3D cube = new Box3D( 1.0, 1.0, 1.0, BasicAppearances.CYAN, null, BasicAppearances.WHITE, null, BasicAppearances.ALU_PLATE, aluUVMap, BasicAppearances.ALU_PLATE, aluUVMap, BasicAppearances.MAGENTA, aluUVMap, BasicAppearances.ALU_PLATE, aluUVMap );
		scene.addContentNode( "cube", Matrix3D.getTransform( 0.0, 45.0, 45.0, 0.0, 0.0, 0.0 ), cube );

		final Sphere3D sphere = new Sphere3D( 0.5, 8, 8, BasicAppearances.ORANGE );
		scene.addContentNode( "sphere", Matrix3D.getTranslation( 2.0, 0.0, 0.5 ), sphere );

		final Cylinder3D cylinder = new Cylinder3D( 1.0, 0.5, 8, BasicAppearances.RED, null, true, BasicAppearances.GREEN, null, BasicAppearances.BLUE, null, false );
		scene.addContentNode( "cylinder", Matrix3D.getTranslation( 0.0, 2.0, 0.0 ), cylinder );

		final ColladaWriter writer = new ColladaWriter( scene );
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			writer.write( out );
		}
		finally
		{
			out.close();
		}

		final InputStream actual = new ByteArrayInputStream( out.toByteArray() );
		final InputStream expected = TestColladaWriter.class.getResourceAsStream( "TestColladaWriter-testWrite.xml" );
		try
		{
			XMLTestTools.assertXMLEquals( expected, actual );
		}
		finally
		{
			expected.close();
		}
	}
}
