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

import ab.j3d.*;

/**
 * Pov Transformation matrix.
 * <pre>
 * &lt; xx, yx, zx,     // \
 *   xy, yy, zy,     //  --- Rotation
 *   xz, yz, zz,     // /
 *   xo, yo, zo &gt;     // Translation
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovMatrix
	extends PovObject
{
	/**
	 * Number format to format numeric as floating-point values.
	 */
	private static final NumberFormat DOUBLE_FORMAT;
	static
	{
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US );
		nf.setMinimumFractionDigits( 1 );
		nf.setMaximumFractionDigits( 16 );
		nf.setGroupingUsed( false );
		DOUBLE_FORMAT = nf;
	}

	/**
	 * Matrix data as a <code>double</code>-array. This data is organized as
	 * follows:
	 * <table>
	 *   <tr><th>Index</th><th>{@link Matrix3D} field</th></tr>
	 *   <tr><td>    0</td><td>{@link Matrix3D#xx}   </td></tr>
	 *   <tr><td>    1</td><td>{@link Matrix3D#yx}   </td></tr>
	 *   <tr><td>    2</td><td>{@link Matrix3D#zx}   </td></tr>
	 *   <tr><td>    3</td><td>{@link Matrix3D#xy}   </td></tr>
	 *   <tr><td>    4</td><td>{@link Matrix3D#yy}   </td></tr>
	 *   <tr><td>    5</td><td>{@link Matrix3D#zy}   </td></tr>
	 *   <tr><td>    6</td><td>{@link Matrix3D#xz}   </td></tr>
	 *   <tr><td>    7</td><td>{@link Matrix3D#yz}   </td></tr>
	 *   <tr><td>    8</td><td>{@link Matrix3D#zz}   </td></tr>
	 *   <tr><td>    9</td><td>{@link Matrix3D#xo}   </td></tr>
	 *   <tr><td>   10</td><td>{@link Matrix3D#yo}   </td></tr>
	 *   <tr><td>   11</td><td>{@link Matrix3D#zo}   </td></tr>
	 * </table>
	 */
	private final double[] _data;

	/**
	 * Construct matrix from a {@link Matrix3D}.
	 *
	 * @param   m       {@link Matrix3D} instance to use as template.
	 */
	public PovMatrix( final Matrix3D m )
	{
		this( new double[]
			{
				m.xx, m.yx, m.zx,
				m.xy, m.yy, m.zy,
				m.xz, m.yz, m.zz,
				m.xo, m.yo, m.zo
			} );
	}

	/**
	 * Construct matrix from a <code>double</code>-array. The data must be
	 * organized as follows:
	 * <table>
	 *   <tr><th>Index</th><th>{@link Matrix3D} field</th></tr>
	 *   <tr><td>    0</td><td>{@link Matrix3D#xx}   </td></tr>
	 *   <tr><td>    1</td><td>{@link Matrix3D#yx}   </td></tr>
	 *   <tr><td>    2</td><td>{@link Matrix3D#zx}   </td></tr>
	 *   <tr><td>    3</td><td>{@link Matrix3D#xy}   </td></tr>
	 *   <tr><td>    4</td><td>{@link Matrix3D#yy}   </td></tr>
	 *   <tr><td>    5</td><td>{@link Matrix3D#zy}   </td></tr>
	 *   <tr><td>    6</td><td>{@link Matrix3D#xz}   </td></tr>
	 *   <tr><td>    7</td><td>{@link Matrix3D#yz}   </td></tr>
	 *   <tr><td>    8</td><td>{@link Matrix3D#zz}   </td></tr>
	 *   <tr><td>    9</td><td>{@link Matrix3D#xo}   </td></tr>
	 *   <tr><td>   10</td><td>{@link Matrix3D#yo}   </td></tr>
	 *   <tr><td>   11</td><td>{@link Matrix3D#zo}   </td></tr>
	 * </table>
	 *
	 * @param   data    Matrix data.
	 */
	public PovMatrix( final double[] data )
	{
		_data = data;
	}

	/**
	 * Translates a {@link PovMatrix} back into a {@link Matrix3D}, to enable
	 * operations (instead of programming the {@link PovMatrix} to do the same
	 * as {@link Matrix3D}).
	 *
	 * @return  Translated {@link Matrix3D}.
	 */
	public Matrix3D getMatrix3D()
	{
		final double[] data = _data;

		return Matrix3D.IDENTITY.set(
			data[  0 ], data[  3 ], data[  6 ], data[  9 ],
			data[  1 ], data[  4 ], data[  7 ], data[ 10 ],
			data[  2 ], data[  5 ], data[  8 ], data[ 11 ] );
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
		return DOUBLE_FORMAT.format( ( value == -0.0 ) ? 0.0 : value );
	}

	@Override
	public void write( final PovWriter out )
		throws IOException
	{
		final double[]     data        = _data;

		out.write( "matrix < " );
		out.write( format( data[  0 ] ) ); out.write( ", " );
		out.write( format( data[  1 ] ) ); out.write( ", " );
		out.write( format( data[  2 ] ) ); out.write( ","  );
		out.newLine();

		out.write( "         " );
		out.write( format( data[  3 ] ) ); out.write( ", " );
		out.write( format( data[  4 ] ) ); out.write( ", " );
		out.write( format( data[  5 ] ) ); out.write( ","  );
		out.newLine();

		out.write( "         " );
		out.write( format( data[  6 ] ) ); out.write( ", " );
		out.write( format( data[  7 ] ) ); out.write( ", " );
		out.write( format( data[  8 ] ) ); out.write( ","  );
		out.newLine();

		out.write( "         " );
		out.write( format( data[  9 ] ) ); out.write( ", " );
		out.write( format( data[ 10 ] ) ); out.write( ", " );
		out.write( format( data[ 11 ] ) ); out.write( " >"  );
		out.newLine();
	}

	/**
	 * Writes a short version of the {@link PovObject} to the specified writer.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void writeShort( final PovWriter out )
		throws IOException
	{
		final double[] data = _data;

		out.write( (int)'<' );
		out.write( format( data[  0 ] ) ); out.write( (int)',' );
		out.write( format( data[  1 ] ) ); out.write( (int)',' );
		out.write( format( data[  2 ] ) ); out.write( ",    "  );
		out.write( format( data[  3 ] ) ); out.write( (int)',' );
		out.write( format( data[  4 ] ) ); out.write( (int)',' );
		out.write( format( data[  5 ] ) ); out.write( ",    "  );
		out.write( format( data[  6 ] ) ); out.write( (int)',' );
		out.write( format( data[  7 ] ) ); out.write( (int)',' );
		out.write( format( data[  8 ] ) ); out.write( ",    "  );
		out.write( format( data[  9 ] ) ); out.write( (int)',' );
		out.write( format( data[ 10 ] ) ); out.write( (int)',' );
		out.write( format( data[ 11 ] ) );
		out.write( (int)'>' );
	}
}
