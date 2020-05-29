/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
 */
package ab.j3d.pov;

import java.io.*;
import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Base class for each object pov object.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public abstract class PovObject
{
	/**
	 * Returns a number format for floats.
	 *
	 * @return Number format.
	 */
	@NotNull
	static NumberFormat getFloatFormat()
	{
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		nf.setMinimumFractionDigits( 1 );
		nf.setMaximumFractionDigits( 5 );
		nf.setGroupingUsed( false );
		return nf;
	}

	/**
	 * Number format to format numeric as floating-point values.
	 */
	private final NumberFormat _floatFormat = getFloatFormat();

	/**
	 * Writes the PovObject to the specified writer.
	 * <p>
	 * The method should use {@link PovWriter#indentIn} and
	 * {@link PovWriter#indentOut} to keep the output readable.
	 *
	 * @param   out     Writer to use for output.
	 *
	 * @throws IOException when writing failed.
	 */
	public abstract void write( final PovWriter out )
	throws IOException;

	/**
	 * Format float-point value.
	 *
	 * @param value Floating-point value.
	 *
	 * @return Formatted string.
	 */
	protected String format( final double value )
	{
		return _floatFormat.format( value + 0.0 ); // Prevent negative zero.
	}
}
