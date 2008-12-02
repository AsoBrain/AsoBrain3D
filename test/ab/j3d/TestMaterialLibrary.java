/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2008 Peter S. Heijnen
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
package ab.j3d;

import java.io.IOException;

import junit.framework.TestCase;

import com.numdata.oss.db.HsqlDbServices;
import com.numdata.oss.net.AuthenticationInformant;
import com.numdata.oss.net.Server;
import com.numdata.oss.net.TestClient;

/**
 * Unit test for the {@link MaterialLibrary}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class TestMaterialLibrary
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestMaterialLibrary.class.getName();

	/**
	 * Test memory library.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testMemory()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testMemory()" );
		checkLibrarySanity( new MemoryMaterialLibrary() );
	}

	/**
	 * Test caching library.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testCaching()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testCaching()" );
		checkLibrarySanity( new CachingMaterialLibrary( new MemoryMaterialLibrary() ) );
	}

	/**
	 * Test database library.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testDatabase()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testDatabase()" );
		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( Material.class );

		checkLibrarySanity( new DbServicesMaterialLibrary( db ) );
	}

	/**
	 * Test client/server functionality.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testClientServer()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testClientServer()" );
		final MaterialLibrary serverLibrary = new MemoryMaterialLibrary();

		final Server server = new Server( new ClientMaterialLibrary.RequestHandler()
			{
				protected MaterialLibrary getMaterialLibrary( final AuthenticationInformant informant )
				{
					return serverLibrary;
				}
			} );

		final TestClient client = new TestClient( server , "localhost" , "test" );

		final MaterialLibrary clientLibrary = new ClientMaterialLibrary( client );
		checkLibrarySanity( clientLibrary );
	}

	/**
	 * Helper method to check library sanity.
	 *
	 * @param   library     Library to check.
	 *
	 * @throws  Exception if the test fails.
	 */
	private static void checkLibrarySanity( final MaterialLibrary library )
		throws Exception
	{
		System.out.println( " - empty library" );

		assertNull( "Empty library test #1" , library.getMaterialByCode( "first" ) );
		assertNull( "Empty library test #2" , library.getMaterialByCode( "second" ) );

		System.out.println( " - storeMaterial()" );

		try
		{
			library.storeMaterial( null );
			fail( "storeMaterial() test #1 - should cause NPE" );
		}
		catch ( NullPointerException e )
		{
			/* should occur */
		}

		final Material firstMaterial = new Material();
		firstMaterial.code = "first";
		library.storeMaterial( firstMaterial );
		final int firstID = firstMaterial.ID;
		assertTrue( "storeMaterial() test #2 - first should have ID, but is " + firstID , firstID >= 0 );
		library.storeMaterial( firstMaterial );
		assertEquals( "storeMaterial() test #3 - first should have same ID" , firstID , firstMaterial.ID );

		final Material secondMaterial = new Material();
		secondMaterial.code = "second";
		library.storeMaterial( secondMaterial );
		final int secondID = secondMaterial.ID;
		assertTrue( "storeMaterial() test #4 - second should have ID, but is " + secondID , secondID >= 0 );
		assertTrue( "storeMaterial() test #5 - second should have higher ID, but is " + secondID , secondID > firstID );
		library.storeMaterial( secondMaterial );
		assertEquals( "storeMaterial() test #6 - second should have same ID" , secondID , secondMaterial.ID );

		final Material thirdMaterial = new Material();
		thirdMaterial.code = "third";
		library.storeMaterial( thirdMaterial );
		final int thirdID = thirdMaterial.ID;
		assertTrue( "storeMaterial() test #7 - third should have ID, but is " + thirdID , thirdID >= 0 );
		assertTrue( "storeMaterial() test #8 - third should have higher ID, but is " + thirdID , thirdID > secondID );
		library.storeMaterial( thirdMaterial );
		assertEquals( "storeMaterial() test #9 - third should have same ID" , thirdID , thirdMaterial.ID );

		try
		{
			library.storeMaterial( null );
			fail( "storeMaterial() test #10 - expected NPE" );
		}
		catch ( NullPointerException e )
		{
			/* Success! */
		}

		/*
		 * Test code and ID modifications.
		 */
		firstMaterial.ID = -1;
		try
		{
			library.storeMaterial( firstMaterial );
			fail( "storeMaterial() test #11 - expected exception due to key constraint" );
		}
		catch ( IOException e )
		{
			/* Success! */
		}
		firstMaterial.ID = firstID;

		secondMaterial.code = "other-second";
		library.storeMaterial( secondMaterial );
		assertEquals( "storeMaterial() test #12" , secondID , secondMaterial.ID );
		secondMaterial.code = "second";
		library.storeMaterial( secondMaterial );

		thirdMaterial.code = "second";
		try
		{
			library.storeMaterial( thirdMaterial );
			fail( "storeMaterial() test #13 - expected exception due to key constraint" );
		}
		catch ( IOException e )
		{
			/* Success! */
		}
		thirdMaterial.code = "third";
		library.storeMaterial( thirdMaterial );

		System.out.println( " - getMaterialByCode()" );

		try
		{
			library.getMaterialByCode( null );
			fail( "getMaterialByCode() test #1 - expected NPE" );
		}
		catch ( NullPointerException e )
		{
			/* Success! */
		}

		assertNull( "getMaterialByCode() test #2" , library.getMaterialByCode( "" ) );
		assertNull( "getMaterialByCode() test #3" , library.getMaterialByCode( "other" ) );
		assertNotNull( "getMaterialByCode() test #4" , library.getMaterialByCode( "first" ) );
		assertNotNull( "getMaterialByCode() test #5" , library.getMaterialByCode( "second" ) );
		assertNotNull( "getMaterialByCode() test #6" , library.getMaterialByCode( "third" ) );
	}
}
