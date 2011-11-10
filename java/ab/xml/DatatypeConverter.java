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

import java.util.*;
import javax.xml.datatype.*;

/**
 * Provides conversion of Java types to XML syntax. Replacement for
 * <code>javax.xml.bind.DatatypeConverter</code>, which is not available on
 * all target platforms.
 *
 * <p>This implementation uses the <code>javax.xml.datatype</code> package.
 * On Android, this requires at least API Level 8 (Android 2.2).
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class DatatypeConverter
{
	/**
	 * Cached datatype factory instance.
	 */
	private static DatatypeFactory _datatypeFactory;

	/**
	 * Returns a datatype factory.
	 *
	 * @return  Datatype factory instance.
	 */
	private static DatatypeFactory getDatatypeFactory()
	{
		DatatypeFactory result = _datatypeFactory;
		if ( result == null )
		{
			try
			{
				result = DatatypeFactory.newInstance();
			}
			catch ( DatatypeConfigurationException e )
			{
				throw new RuntimeException( e );
			}
			_datatypeFactory = result;
		}
		return result;
	}

	/**
	 * Converts the given calendar's date and time into a valid lexical value
	 * for the XML Schema <code>dateTime</code> data type.
	 *
	 * @param   calendar    Calendar to be converted.
	 *
	 * @return  String representation of the calendar's date and time.
	 */
	public static String printDateTime( final Calendar calendar )
	{
		final GregorianCalendar gregorianCalendar;
		if ( calendar instanceof GregorianCalendar )
		{
			gregorianCalendar = (GregorianCalendar)calendar;
		}
		else
		{
			gregorianCalendar = new GregorianCalendar( calendar.getTimeZone() );
			gregorianCalendar.setTime( calendar.getTime() );
		}
		final DatatypeFactory datatypeFactory = getDatatypeFactory();
		final XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar( gregorianCalendar );
		return xmlGregorianCalendar.toXMLFormat();
	}

	/**
	 * Converts the given data-time value to a calendar instance. The given
	 * value must be a valid lexical value of the XML Schema
	 * <code>dateTime</code> data type.
	 *
	 * @param   value   Value to be parsed.
	 *
	 * @return  String representation of the calendar's date and time.
	 */
	public static Calendar parseDateTime( final String value )
	{
		final DatatypeFactory datatypeFactory = getDatatypeFactory();
		final XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar( value );
		return xmlGregorianCalendar.toGregorianCalendar();
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * <code>int</code> data type.
	 *
	 * @param   v   Value to be converted.
	 *
	 * @return  String representation of the value.
	 */
	public static String printInt( final int v )
	{
		return String.valueOf( v );
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * <code>float</code> data type.
	 *
	 * @param   v   Value to be converted.
	 *
	 * @return  String representation of the value.
	 */
	public static String printFloat( final float v )
	{
		final String result;
		if ( v == Float.POSITIVE_INFINITY )
		{
			result = "INF";
		}
		else if ( v == Float.NEGATIVE_INFINITY )
		{
			result = "-INF";
		}
		else
		{
			result = String.valueOf( v );
		}
		return result;
	}

	/**
	 * Converts the given value into a valid lexical value for the XML Schema
	 * <code>double</code> data type.
	 *
	 * @param   v   Value to be converted.
	 *
	 * @return  String representation of the value.
	 */
	public static String printDouble( final double v )
	{
		final String result;
		if ( v == Double.POSITIVE_INFINITY )
		{
			result = "INF";
		}
		else if ( v == Double.NEGATIVE_INFINITY )
		{
			result = "-INF";
		}
		else
		{
			result = String.valueOf( v );
		}
		return result;
	}
}
