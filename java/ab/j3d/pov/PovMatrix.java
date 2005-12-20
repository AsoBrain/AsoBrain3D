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

import com.numdata.oss.io.IndentingWriter;

import ab.j3d.Matrix3D;

/**
 * Pov Transformation matrix.
 * <pre>
 * &lt; xx , yx , zx ,     // \
 *   xy , yy , zy ,     //  --- Rotation
 *   xz , yz , zz ,     // /
 *   xo , yo , zo &gt;     // Translation
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovMatrix
	extends PovObject
{
	/**
	 * The data in the matrix as a double array.
	 * Index :  0    1    2    3    4    5    6    7    8    9    10   11
	 * Value :  xx , yz , zx , zy , yy , zy , xz , yz , zz , xo , yo , zo
	 */
	public final double[] data;

	public PovMatrix( Matrix3D m )
	{
		data = new double[]
		{
			m.xx , m.yx , m.zx ,
			m.xy , m.yy , m.zy ,
			m.xz , m.yz , m.zz ,
			m.xo , m.yo , m.zo
		};
	}

	/**
	 * Matrix constructor comment.
	 */
	public PovMatrix( final double[] data )
	{
		this.data = data;
	}

	/**
	 * Rounds the data to 2 digits and places a space if no '-' is printed.
	 *
	 * @param   i   Data index.
	 *
	 * @return  Value of data as formatted string.
	 */
	private String data( final int i )
	{
		final int round = (int)Math.round( data[ i ] * 100.0 );

		final StringBuffer sb = new StringBuffer();

		if ( round >= 0 )
			sb.append( ' ' );

		sb.append( round / 100 );

		final int percent = ( Math.abs( round ) % 100 );
		if ( percent != 0 )
		{
			sb.append( '.' );
			sb.append( percent );
		}

		return sb.toString();
	}

	/**
	 * Translates a PovMatrix back into a Matrix3D, to enable operations
	 * (instead of programming the PovMatrix to do the same as Matrix3D).
	 *
	 * @return  Translated Matrix3D.
	 */
	public Matrix3D getMatrix3D()
	{
		return Matrix3D.INIT.set(
			data[0] , data[3] , data[6] , data[ 9] ,
			data[1] , data[4] , data[7] , data[10] ,
			data[2] , data[5] , data[8] , data[11] );
	}

	/**
	 * Writes the PovObject to the specified output stream.
	 * The method should use indentIn and indentOut to maintain the overview.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void write( final IndentingWriter out )
		throws IOException
	{
		writeAB( out );
	}

	/**
	 * Writes a short version of the PovObject to the specified output stream.
	 * The method should use indentIn and indentOut to maintain the overview.
	 *
	 * @param   out     IndentingWriter to use for writing.
	 *
	 * @throws  IOException when writing failed.
	 */
	public void writeShort( final IndentingWriter out )
		throws IOException
	{
		out.write( "< " );
		out.write( data( 0 ) );
		out.write( (int)',' );
		out.write( data( 1 ) );
		out.write( (int)',' );
		out.write( data( 2 ) );
		out.write( ",     " );
		out.write( data( 3 ) );
		out.write( (int)',' );
		out.write( data( 4 ) );
		out.write( (int)',' );
		out.write( data( 5 ) );
		out.write( ",     " );
		out.write( data( 6 ) );
		out.write( (int)',' );
		out.write( data( 7 ) );
		out.write( (int)',' );
		out.write( data( 8 ) );
		out.write( ",     " );
		out.write( data( 9 ) );
		out.write( (int)',' );
		out.write( data( 10 ) );
		out.write( (int)',' );
		out.write( data( 11 ) );
		out.write( " >" );
	}

	/**
	 * Write this matrix in a readable way, as defined in the povray documentation.
	 *
	 * @param out The outputstream to write to.
	 * @throws IOException If an error occured while writing to out.
	 */
	public void writeAB( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "matrix < " + data[0] + " , " + data[1] + " , " + data[2] + " ," );
		out.writeln( "         " + data[3] + " , " + data[4] + " , " + data[5] + " ," );
		out.writeln( "         " + data[6] + " , " + data[7] + " , " + data[8] + " ," );
		out.writeln( "         " + data[9] + " , " + data[10] + " , " + data[11] + " >" );
	}
}
