/* $Id$
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
package ab.j3d.model;

import ab.j3d.*;
import junit.framework.*;

/**
 * This class tests the {@link Object3DBuilder} class.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public class TestObject3DBuilder
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestObject3DBuilder.class.getName();

	/**
	 * Test {@link Object3DBuilder#getVertexIndex(Vector3D)} method.
	 */
	public static void testGetVertexIndex()
	{
		System.out.println( CLASS_NAME + ".testGetVertexIndex" );

		final Object3DBuilder builder = new Object3DBuilder();
		assertEquals( "[pre] vertexCount", 0, builder.getVertexCount() );

		assertEquals( "test1 - vertexIndex", 0, builder.getVertexIndex( new Vector3D( 0.0, 0.0, 0.0 ) ) );
		assertEquals( "test1 - vertexCount", 1, builder.getVertexCount() );

		assertEquals( "test2 - vertexIndex", 0, builder.getVertexIndex( new Vector3D( 0.0, 0.0, 0.0 ) ) );
		assertEquals( "test2 - vertexCount", 1, builder.getVertexCount() );

		assertEquals( "test3 - vertexIndex", 1, builder.getVertexIndex( new Vector3D( 1.0, 0.0, 0.0 ) ) );
		assertEquals( "test3 - vertexCount", 2, builder.getVertexCount() );

		assertEquals( "test4 - vertexIndex", 2, builder.getVertexIndex( new Vector3D( 0.0, 1.0, 0.0 ) ) );
		assertEquals( "test4 - vertexCount", 3, builder.getVertexCount() );

		assertEquals( "test5 - vertexIndex", 3, builder.getVertexIndex( new Vector3D( 0.0, 0.0, 1.0 ) ) );
		assertEquals( "test5 - vertexCount", 4, builder.getVertexCount() );
	}
}