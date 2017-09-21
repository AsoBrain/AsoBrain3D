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
package ab.j3d.pov;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * Base class for each object pov object.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class PovObject
{
	/**
	 * Number format to format numeric values as integers.
	 */
	private static final NumberFormat INT_FORMAT;
	static
	{
		final NumberFormat nf = NumberFormat.getIntegerInstance( Locale.US );
		nf.setGroupingUsed( false );
		INT_FORMAT = nf;
	}

	/**
	 * Number format to format numeric as floating-point values.
	 */
	private static final NumberFormat FLOAT_FORMAT;
	static
	{
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		nf.setMinimumFractionDigits( 1 );
		nf.setMaximumFractionDigits( 5 );
		nf.setGroupingUsed( false );
		FLOAT_FORMAT = nf;
	}

	/**
	 * Writes the PovObject to the specified writer.
	 * <p>
	 * The method should use {@link PovWriter#indentIn} and
	 * {@link PovWriter#indentOut} to keep the output readable.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws  IOException when writing failed.
	 */
	public abstract void write( final PovWriter out )
		throws IOException;

	/**
	 * Format integer value.
	 *
	 * @param   value   Floating-point value.
	 *
	 * @return  Formatted string.
	 */
	protected static String format( final int value )
	{
		return INT_FORMAT.format( (long)value );
	}

	/**
	 * Format float-point value.
	 *
	 * @param   value   Floating-point value.
	 *
	 * @return  Formatted string.
	 */
	protected static String format( final double value )
	{
		return FLOAT_FORMAT.format( ( value == -0.0 ) ? 0.0 : value );
	}
}
