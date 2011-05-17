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
package ab.j3d.loader;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import junit.framework.*;

/**
 * This class tests the {@link ZipResourceLoader} class.
 *
 * @see     ZipResourceLoader
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public class TestZipResourceLoader
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestZipResourceLoader.class.getName();

	/**
	 * Test the {@link ZipResourceLoader#getResource} method.
	 *
	 * Creates one byte array of 10000 bytes.
	 * Then divides it into three files and puts it into a ZipInputStream
	 * Extraction of the three files is tested and the three files together
	 * are checked against the created byte array to see if the data wasn't
	 * modified.
	 *
	 * @throws Exception if the test fails.
	 */
	public static void testGetResource()
		throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetResource()" );

		final byte[] expectedFiles = new byte[ 10000 ];

		final Random random = new Random();
		random.nextBytes( expectedFiles );

		final ByteArrayOutputStream baot    =   new ByteArrayOutputStream( );
		final ZipOutputStream zout          =   new ZipOutputStream( baot );

		final ZipEntry ze1 = new ZipEntry( "file1" );
		zout.putNextEntry( ze1 );
		zout.write( expectedFiles , 0 , 3000 );
		zout.closeEntry();

		final ZipEntry ze2 = new ZipEntry( "file2" );
		zout.putNextEntry( ze2 );
		zout.write( expectedFiles , 3000 , 5000 );
		zout.closeEntry();

		final ZipEntry ze3 = new ZipEntry( "file3" );
		zout.putNextEntry( ze3 );
		zout.write( expectedFiles , 8000 , 2000 );
		zout.closeEntry();

		zout.close();

		final byte[] be1 = new byte[ 3000 ];
		System.arraycopy( expectedFiles , 0    , be1 , 0 , be1.length );
		final byte[] be2 = new byte[ 5000 ];
		System.arraycopy( expectedFiles , 3000 , be2 , 0 , be2.length );
		final byte[] be3 = new byte[ 2000 ];
		System.arraycopy( expectedFiles , 8000 , be3 , 0 , be3.length );

		final ZipResourceLoader resourceLoader = new ZipResourceLoader( new ByteArrayInputStream( baot.toByteArray() ) );

		final BufferedInputStream rl1 = new BufferedInputStream( resourceLoader.getResource( "file1" ) );
		final byte[] file1 = new byte[ be1.length ];
		rl1.read( file1 );
		assertEquals( "Incorrect file or file not found." , Arrays.toString( be1 ) , Arrays.toString( file1 ) );

		final BufferedInputStream rl2 = new BufferedInputStream( resourceLoader.getResource( "file2" ) );
		final byte[] file2 = new byte[ be2.length ];
		rl2.read( file2 );
		assertEquals( "Incorrect file or file not found." , Arrays.toString( be2 ) , Arrays.toString( file2 ) );

		final BufferedInputStream rl3 = new BufferedInputStream( resourceLoader.getResource( "file3" ) );
		final byte[] file3 = new byte[ be3.length ];
		rl3.read( file3 );
		assertEquals( "Incorrect file or file not found." , Arrays.toString( be3 ) , Arrays.toString( file3 ) );

		final byte[] actualFiles = new byte[10000];

		System.arraycopy( file1 , 0 , actualFiles , 0                           , file1.length );
		System.arraycopy( file2 , 0 , actualFiles , file1.length                , file2.length );
		System.arraycopy( file3 , 0 , actualFiles , file1.length + file2.length , file3.length );
		assertEquals( "Input and Output file don't match" , Arrays.toString( expectedFiles ), Arrays.toString( actualFiles ) );
	}
}