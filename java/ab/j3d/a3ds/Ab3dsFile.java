/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Sjoerd Bouwman
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
package ab.j3d.a3ds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This is a representation of a .3ds file it contains the main entrance to
 * all chunks inside (the main3DS chunk).
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ $Date$
 */
public final class Ab3dsFile
{
	/**
	 * Set to true to receive debugging to console.
	 */
	public static boolean DEBUG = false;

	/**
	 * Name of scene.
	 */
	private String _name;

	/**
	 * The main entrance point of the model.
	 */
	protected HierarchyChunk _main;

	/**
	 * Creates a new Ab3dsFile with specified name.
	 *
	 * @param   name    Name of the 3ds model.
	 */
	public Ab3dsFile( final String name )
	{
		clear( name );
	}

	/**
	 * Clears the all content and specifies a new name.
	 *
	 * @param   newName New name for the model.
	 */
	public void clear( final String newName )
	{
		_name = newName;
		_main = new HierarchyChunk( Chunk.MAIN3DS );
		_main.add( new HierarchyChunk( Chunk.EDIT3DS ) );
	}

	/**
	 * Gets the edit chunk from the model.
	 *
	 * @return  Edit chunk.
	 */
	public HierarchyChunk getEditChunk()
	{
		return ( getMainChunk() == null ) ? null
		     : ((HierarchyChunk)getMainChunk().getFirstChunkByID( Chunk.EDIT3DS ));
	}

	/**
	 * Gets the main3DS chunk from the model.
	 *
	 * @return  the main chunk.
	 */
	public HierarchyChunk getMainChunk()
	{
		return ( _main.getID() != Chunk.MAIN3DS ) ? null : _main;
	}

	/**
	 * Gets the name of the model.
	 *
	 * @return  the name of the 3dsfile.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Load 3ds model from file.
	 */
	public void load()
	{
		load( _name );
	}

	/**
	 * Loads a 3ds file into the hierarchy.
	 *
	 * @param   file    File to load from.
	 */
	public void load( final File file )
	{
		try
		{
			final FileInputStream fis = new FileInputStream(file);
			try
			{
				final Ab3dsInputStream is = new Ab3dsInputStream( fis );

				/*
				 * Main chunk.
				 */
				if ( DEBUG )
					System.out.println( "READING 3DS FILE" );

				_main = new HierarchyChunk( is.readInt() );

				if ( _main.getID() != Chunk.MAIN3DS )
					throw new RuntimeException( "File is not a valid .3DS file! (does not start with 0x4D4D)" );

				_main.read( is );

				if ( DEBUG )
					System.out.println( "FINISHED 3DS FILE" );
			}
			finally
			{
				fis.close();
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loads a 3ds file into the hierarchy.
	 *
	 * @param   filename    Name of the file to load from.
	 */
	public void load( final String filename )
	{
		load( new File( filename ) );
	}

	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		boolean ok = true;

		for ( int i = 0 ; ok && i < args.length ; i++ )
		{
			final String arg = args[ i ];

			if ( "-d".equals( arg )
			  || "--debug".equals( arg ) )
			{
				DEBUG = true;
			}
			else
			{
				System.err.println( "error: invalid command-line argument: " + arg );
				ok = false;
			}
		}

		if ( ok )
		{
			final Ab3dsFile f = new Ab3dsFile( "test" );
			f.load( new File( "C:\\progra~1\\Graphics\\3dsmax\\meshes\\Tv.3ds" ) );
			System.out.println( "---------------------------" );

			//f.saveAs( new File( "C:\\progra~1\\Graphics\\3dsmax\\meshes\\Tv2.3ds" ) );
			//f.load( new File( "C:\\temp\\3ds\\write.3ds" ) );
		}
	}

	/**
	 * Saves the current hierarchy as a 3ds file.
	 */
	public void save()
	{
		saveAs( _name );
	}

	/**
	 * Saves the current hierarchy as a 3ds file.
	 *
	 * @param   file    File to save to.
	 */
	public void saveAs( final File file )
	{
		try
		{
			final FileOutputStream fos = new FileOutputStream( file );
			try
			{
				final Ab3dsOutputStream os = new Ab3dsOutputStream( fos );

				/*
				 * Main chunk.
				 */
				System.out.println( "WRITING 3DS FILE" );

				_main.write( os );

				System.out.println( "WRITING 3DS FILE" );
			}
			finally
			{
				fos.close();
			}
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves the current hierarchy as a 3ds file.
	 *
	 * @param   filename    Name of the file to save to.
	 */
	public void saveAs( final String filename )
	{
		saveAs( new File( filename ) );
	}
}
