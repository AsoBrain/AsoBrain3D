/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2008-2009 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package ab.j3d.pov;

import java.io.IOException;

import com.numdata.oss.io.IndentingWriter;

/**
 * An infinite flat surface. The plane is not a thin boundary or can be compared
 * to a sheet of paper. A plane is a solid object of infinite size that divides
 * POV-space in two parts, inside and outside the plane.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class PovPlane
	extends PovGeometry
{
	/**
	 * Normal pointing out of the plane.
	 */
	private final PovVector _normal;

	/**
	 * Distance from the origin to the plane, in the direction of its normal.
	 */
	private final double _distance;

	/**
	 * Constructs new plane.
	 *
	 * @param   name        Name of the object.
	 * @param   normal      Normal pointing out of the plane.
	 * @param   distance    Distance from the origin to the plane, in the
	 *                      direction of the plane's normal.
	 */
	public PovPlane( final String name , final PovVector normal , final double distance )
	{
		super( name );
		_normal   = normal;
		_distance = distance;
	}

	/**
	 * Constructs new plane.
	 *
	 * @param   name        Name of the object.
	 * @param   texture     Texture to be applied to the object.
	 * @param   normal      Normal pointing out of plane.
	 * @param   distance    Distance from the origin to the plane, in the
	 *                      direction pointed by the plane's normal.
	 */
	public PovPlane( final String name , final PovTexture texture , final PovVector normal , final double distance )
	{
		super( name , texture );

		_normal   = normal;
		_distance = distance;
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		out.writeln( "plane" );
		out.writeln( "{" );
		out.indentIn();

		_normal.write( out );
		out.write( " , " );
		out.writeln( Double.toString( _distance ) );

		final PovTexture texture = getTexture();
		if ( texture != null )
		{
			texture.write( out );
		}

		out.indentOut();
		out.writeln( "}" );
	}
}
