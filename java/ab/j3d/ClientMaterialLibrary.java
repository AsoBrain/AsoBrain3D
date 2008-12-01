/* $Id$
 * ====================================================================
 * Copyright (C) 2003-2008 Numdata BV
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.numdata.oss.TextTools;
import com.numdata.oss.net.AuthenticationInformant;
import com.numdata.oss.net.Client;
import com.numdata.oss.net.Packet;

/**
 * This class implements {@link MaterialLibrary} on top of a {@link Client}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ClientMaterialLibrary
	implements MaterialLibrary
{
	/**
	 * Client to implement library on top of.
	 */
	private final Client _client;

	/**
	 * Construct library.
	 *
	 * @param   client    Client to implement library on top of.
	 */
	public ClientMaterialLibrary( final Client client )
	{
		_client = client;
	}

	public Material getMaterialByCode( final String code )
		throws IOException
	{
		Material result = null;

		if ( code == null )
			throw new NullPointerException( "code" );

		if ( TextTools.isNonEmpty( code ) )
		{
			final Packet request = new Packet( "getMaterialByCode" );
			request.setAttribute( "code" , code );

			result = _client.request( request , Material.class , true );
		}

		return result;
	}

	public List<Material> getMaterials()
		throws IOException
	{
		final Packet request = new Packet( "getMaterials" );
		return _client.request( request , List.class , false );
	}

	public void storeMaterial( final Material material )
		throws IOException
	{
		if ( material == null )
			throw new NullPointerException( "material" );

		final Packet request = new Packet( "storeMaterial" );
		request.setAttribute( "material" , material );
		material.ID = _client.request( request , Integer.class , false );
	}

	/**
	 * This implements the handler for client requests.
	 */
	public abstract static class RequestHandler
		implements com.numdata.oss.net.RequestHandler
	{
		/**
		 * Library to use.
		 *
		 * @param   informant   Provides authentication information.
		 *
		 * @return  Library to use.
		 */
		protected abstract MaterialLibrary getMaterialLibrary( final AuthenticationInformant informant );

		public Serializable handleRequest( final AuthenticationInformant informant , final Packet request )
			throws IOException
		{
			final Serializable result;

			final String message = request.getMessage();

			if ( "getMaterialByCode".equals( message ) )
			{
				final String code = request.getAttribute( "code" , String.class );

				final MaterialLibrary materialLibrary = getMaterialLibrary( informant );
				result = materialLibrary.getMaterialByCode( code );
			}
			else if ( "getMaterials".equals( message ) )
			{
				final MaterialLibrary materialLibrary = getMaterialLibrary( informant );
				result = new ArrayList<Material>( materialLibrary.getMaterials() );
			}
			else if ( "storeMaterial".equals( message ) )
			{
				final Material material = request.getAttribute( "material" , Material.class );

//				AuthorizationChecker.checkAuthorization( informant , "MaterialLibrary.storeMaterial" );

				final MaterialLibrary materialLibrary = getMaterialLibrary( informant );
				materialLibrary.storeMaterial( material );
				result = Integer.valueOf( material.ID );
			}

			else
			{
				result = NOT_HANDLED;
			}

			return result;
		}
	}
}