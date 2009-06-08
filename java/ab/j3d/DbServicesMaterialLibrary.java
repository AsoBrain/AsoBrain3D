/* $Id$
 * ====================================================================
 * Copyright (C) 2003-2009 Numdata BV
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

import java.sql.SQLException;
import java.util.List;

import com.numdata.oss.TextTools;
import com.numdata.oss.db.DatabaseException;
import com.numdata.oss.db.DbServices;

/**
 * This class implements {@link MaterialLibrary} on top of {@link DbServices}.
 *
 * @author  Sjoerd Bouwman
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class DbServicesMaterialLibrary
	implements MaterialLibrary
{
	/**
	 * Database services to implement library on top of.
	 */
	private DbServices _db;

	/**
	 * Constructor to create a local server.
	 *
	 * @param   db  Database services to implement library on top of.
	 */
	public DbServicesMaterialLibrary( final DbServices db )
	{
		_db = db;
	}

	/**
	 * Get database the library is implemented on top of.
	 *
	 * @return  Database the library is implemented on top of.
	 */
	public DbServices getDB()
	{
		return _db;
	}

	/**
	 * Set database to implement library on top of.
	 *
	 * @param   db  Database to implement library on top of.
	 */
	public void setDB( final DbServices db )
	{
		_db = db;
	}

	public Material getMaterialByCode( final String code )
		throws DatabaseException
	{
		if ( code == null )
			throw new NullPointerException( "code" );

		Material result = null;

		if ( TextTools.isNonEmpty( code ) )
		{
			try
			{
				final DbServices db = getDB();
				result = db.retrieveObject( null , Material.class , DbServices.appendEquals( null , "code" , code ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e.getMessage() , e );
			}
		}

		return result;
	}

	public List<Material> getMaterials()
		throws DatabaseException
	{
		try
		{
			final DbServices db = getDB();
			return db.retrieveList( null , Material.class , null );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e.getMessage() , e );
		}
	}

	public void storeMaterial( final Material material )
		throws DatabaseException
	{
		if ( material == null )
			throw new NullPointerException( "material" );

		final DbServices db = getDB();
		try
		{
			db.storeObject( material );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e.getMessage() , e );
		}
	}
}
