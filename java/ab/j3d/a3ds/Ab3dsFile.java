package ab.a3ds;

/*
 * $Id$
 *
 * (C) Copyright 1999-2002 Sjoerd Bouwman (aso@asobrain.com)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it as you see fit.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import java.io.*;

/**
 * This is a representation of a .3ds file it contains the main entrance to
 * all chunks inside (the main3DS chunk).
 *
 * @author	Sjoerd Bouwman
 *
 * @version	$Revision$ ($Date$, $Author$)
 */
public class Ab3dsFile 
{
	/**
	 * Set to true to receive debugging to console.
	 */
	public static final boolean DEBUG = false;

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
	 * @param	name	the name of the 3ds model.
	 */
	public Ab3dsFile( String name )
	{
		clear( name );
	}

	/**
	 * Clears the all content and specifies a new name.
	 *
	 * @param	newName	the new name for the model.
	 */
	public void clear( String newName )
	{
		_name = newName;
		_main = new HierarchyChunk( Chunk.MAIN3DS );
		_main.add( new HierarchyChunk( Chunk.EDIT3DS ) );
	}

	/**
	 * Gets the edit chunk from the model.
	 *
	 * @return	the edit chunk.
	 */
	public HierarchyChunk getEditChunk()
	{
		if ( getMainChunk() == null )
			return null;
		return (HierarchyChunk)getMainChunk().getFirstChunkByID( Chunk.EDIT3DS );
	}

	/**
	 * Gets the main3DS chunk from the model.
	 *
	 * @return	the main chunk.
	 */
	public HierarchyChunk getMainChunk()
	{
		if ( _main.getID() != Chunk.MAIN3DS )
			return null;
		return (HierarchyChunk)_main;
	}

	/**
	 * Build any given model from 3ds hierarchy.
	 *
	 * @return	hierarchy	the model to build from.
	 */
	public Object getModelFrom3DS()
	{
		throw new RuntimeException( "Not implemented 'Object Ab3dsFile.getModelFrom3DS()'" );
	}

	/**
	 * Gets the name of the model.
	 *
	 * @return	the name of the 3dsfile.
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
	 * @param	file	the file to load from.
	 */
	public void load(File file)
	{
		try
		{
			FileInputStream fis = new FileInputStream(file);
			Ab3dsInputStream is = new Ab3dsInputStream( fis );
			/*
			 * Main chunk.
			 */
			if ( DEBUG ) System.out.println( "READING 3DS FILE" );
			 
			_main = new HierarchyChunk( is.readInt() );

			if ( _main.getID() != Chunk.MAIN3DS )
				throw new RuntimeException( "File is not a valid .3DS file! (does not start with 0x4D4D)" );
			
			_main.read( is );

			if ( DEBUG ) System.out.println( "FINISHED 3DS FILE" );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loads a 3ds file into the hierarchy.
	 *
	 * @param	filename	the name of the file to load from.
	 */
	public void load( String filename )
	{
		load( new File( filename ) );
	}

	/**
	 * Test main
	 *
	 * @param	argv	Command line arguments.
	 */
	public static void main( String argv[] )
	{
		Ab3dsFile f = new Ab3dsFile( "test" );

		f.load( new File( "C:\\progra~1\\Graphics\\3dsmax\\meshes\\Tv.3ds" ) );

		System.out.println( "---------------------------" );
		
		//f.saveAs( new File( "C:\\progra~1\\Graphics\\3dsmax\\meshes\\Tv2.3ds" ) );



		//f.load( new File( "C:\\temp\\3ds\\write.3ds" ) );

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
	 * @param	file	the file to save to.
	 */
	public void saveAs( File file )
	{
		try
		{
			FileOutputStream fos = new FileOutputStream( file );
			Ab3dsOutputStream os = new Ab3dsOutputStream( fos );
			/*
			 * Main chunk.
			 */
			System.out.println( "WRITING 3DS FILE" );
			 
			_main.write( os );
	
			System.out.println( "WRITING 3DS FILE" );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves the current hierarchy as a 3ds file.
	 *
	 * @param	filename	the name of the file to save to.
	 */
	public void saveAs( String filename )
	{
		saveAs( new File( filename ) );
	}

	/**
	 * Build 3ds hierarchy from from given model.
	 *
	 * @param	hierarchy	the model to build from.
	 */
	public void set3DSFromModel( Object hierarchy )
	{
		throw new RuntimeException( "Not implemented 'Ab3dsFile.set3DSFromModel( Object )'" );
	}

}
