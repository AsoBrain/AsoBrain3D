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
package ab.xml;

import java.io.*;

/**
 * Factory for creating {@link XMLWriter} instances using an XML API that is
 * available on the current platform.
 *
 * <p>
 * The following APIs are currently supported:
 * <ul>
 *   <li>The Streaming API for XML (StAX)</li>
 *   <li>XML Pull</li>
 * </ul>
 * For an overview of differences with these APIs, see {@link XMLWriter}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public abstract class XMLWriterFactory
{
	/**
	 * Class names of factory implementations.
	 */
	private static final String[] FACTORY_CLASS_NAMES =
	{
		"ab.xml.XmlPullWriterFactory", "ab.xml.StaxWriterFactory"
	};

	/**
	 * Create a new factory that uses an XML API that is available on the
	 * current platform.
	 *
	 * @return  Factory instance.
	 *
	 * @throws  FactoryException if no factory can be loaded.
	 */
	public static XMLWriterFactory newInstance()
	{
		XMLWriterFactory result = null;

		for ( final String className : FACTORY_CLASS_NAMES )
		{
			final Class<XMLWriterFactory> clazz;
			try
			{
				//noinspection unchecked
				clazz = (Class<XMLWriterFactory>)Class.forName( className );
				result = clazz.newInstance();
				break;
			}
			catch ( FactoryException e )
			{
				// Factory determined a problem. Use another factory.
			}
			catch ( NoClassDefFoundError e )
			{
				// If the underlying API is not available.
			}
			catch ( ClassNotFoundException e )
			{
				/*
				 * If the factory class doesn't exist.
				 * If so, the list of factory classes should be corrected.
				 */
				throw new FactoryException( e );
			}
			catch ( IllegalAccessException e )
			{
				/*
				 * If the factory class doesn't have a public constructor.
				 * If so, the factory class should be corrected.
				 */
				throw new FactoryException( e );
			}
			catch ( Throwable e )
			{
				/*
				 * Any other problem with the factory probably indicates a
				 * programming error as well.
				 */
				throw new FactoryException( e );
			}
		}

		if ( result == null )
		{
			throw new FactoryException( "Could not find an implementation that is supported by the current platform." );
		}

		return result;
	}

	/**
	 * Whether XML writers created by the factory should automatically indent
	 * the written XML. Indenting is off by default.
	 */
	private boolean _indenting;

	/**
	 * Constructs a new factory instance.
	 */
	protected XMLWriterFactory()
	{
		_indenting = false;
	}

	/**
	 * Returns whether XML writers created by the factory should automatically
	 * indent the written XML. Indenting is disabled by default.
	 *
	 * @return  <code>true</code> if indenting is enabled.
	 */
	public boolean isIndenting()
	{
		return _indenting;
	}

	/**
	 * Sets whether XML writers created by the factory should automatically
	 * indent the written XML. Indenting is disabled by default.
	 *
	 * @param   indenting   <code>true</code> to enable indenting.
	 */
	public void setIndenting( final boolean indenting )
	{
		_indenting = indenting;
	}

	/**
	 * Creates an XML writer.
	 *
	 * @param   out         Stream to write to.
	 * @param   encoding    Character encoding to be used.
	 *
	 * @return  Created XML writer.
	 *
	 * @throws  XMLException if an XML-related exception occurs.
	 */
	public abstract XMLWriter createXMLWriter( final OutputStream out, final String encoding )
		throws XMLException;
}
