/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2005
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import com.numdata.oss.io.IndentingWriter;

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
	public static final NumberFormat INT_FORMAT;
	static
	{
		final NumberFormat nf = NumberFormat.getIntegerInstance( Locale.US );
		nf.setGroupingUsed( false );
		INT_FORMAT = nf;
	}

	/**
	 * Number format to format numeric as floating-point values.
	 */
	public static final NumberFormat FLOAT_FORMAT;
	static
	{
		final NumberFormat nf = new DecimalFormat( "0.0#################################" , new DecimalFormatSymbols( Locale.US ) );
		nf.setMinimumFractionDigits( 1 );
		nf.setGroupingUsed( false );
		FLOAT_FORMAT = nf;
	}

	/**
	 * Writes the PovObject to the specified output stream.
	 * The method should use indentIn and indentOut to maintain the overview.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public abstract void write( final IndentingWriter out )
		throws IOException;

}
