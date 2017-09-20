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

/**
 * Indicates that a factory instance could not be created.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class FactoryException
	extends RuntimeException
{
	/** Serialized data version. */
	private static final long serialVersionUID = 7574420257480434915L;

	/**
	 * Constructs a new instance.
	 */
	public FactoryException()
	{
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   message     Detail message.
	 */
	public FactoryException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   message     Detail message.
	 * @param   cause       Cause of the exception.
	 */
	public FactoryException( final String message, final Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param   cause       Cause of the exception.
	 */
	public FactoryException( final Throwable cause )
	{
		super( cause.getMessage(), cause );
	}
}
