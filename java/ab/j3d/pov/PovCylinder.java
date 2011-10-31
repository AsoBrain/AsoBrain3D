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


/**
 * Pov cylinder.
 * <pre>
 * cone
 * {
 *     &lt; x1 , y1 , z1 &gt; , radius1
 *     &lt; x2 , y2 , z2 &gt; , radius2
 *     [rotate]
 *     [matrix]
 *     [texture]
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovCylinder
	extends PovGeometry
{
	/**
	 * Lower center of cylinder/cone.
	 */
	private PovVector _p1;

	/**
	 * Upper center of cylinder/cone.
	 */
	private PovVector _p2;

	/**
	 * The radius of the cylinder/cone at p1.
	 */
	private double _r1;

	/**
	 * The radius of the cylinder/cone at p2.
	 */
	private double _r2;

	/**
	 * Creates a cylinder with name, bounds and texture using 8 doubles.
	 *
	 * @param   name        Name of the shape.
	 * @param   x1          Bottom x-position of cylinder.
	 * @param   y1          Bottom y-position of cylinder.
	 * @param   z1          Bottom z-position of of cylinder.
	 * @param   r1          Radius of cylinder at lower cap.
	 * @param   x2          Top x-position of cylinder.
	 * @param   y2          Top y-position of cylinder.
	 * @param   z2          Top z-position of cylinder.
	 * @param   r2          Radius of cylinder at upper cap.
	 * @param   texture     Texture of the shape.
	 */
	public PovCylinder( final String name , final double x1 , final double y1 , final double z1 , final double r1 , final double x2 , final double y2 , final double z2 , final double r2 , final PovTexture texture )
	{
		this( name , new PovVector( x1 , y1 , z1 ) , r1 , new PovVector( x2 , y2 , z2 ) , r2 , texture );
	}

	/**
	 * Creates a cylinder with name, bounds and texture using 6 doubles.
	 *
	 * @param   name        Name of the shape.
	 * @param   x           X-position of cylinder.
	 * @param   y           Y-position of cylinder.
	 * @param   z           Z-position of cylinder.
	 * @param   h           Height of cylinder
	 * @param   r1          Radius of cylinder at lower cap.
	 * @param   r2          Radius of cylinder at upper cap.
	 * @param   texture     Texture of the shape.
	 */
	public PovCylinder( final String name , final double x , final double y , final double z , final double h , final double r1 , final double r2 , final PovTexture texture )
	{
		this( name , x , y , z , r1 , x , y , z + h , r2 , texture );
	}

	/**
	 * Creates a cylinder with name, bounds and texture using 5 doubles.
	 *
	 * @param   name        Name of the shape.
	 * @param   x           X-position of cylinder.
	 * @param   y           Y-position of cylinder.
	 * @param   z           Z-position of cylinder.
	 * @param   h           Height of cylinder
	 * @param   r           Radius of cylinder.
	 * @param   texture     Texture of the shape.
	 */
	public PovCylinder( final String name , final double x , final double y , final double z , final double h , final double r , final PovTexture texture )
	{
		this( name , x , y , z , r , x , y , z + h , r , texture );
	}

	/**
	 * Creates a cylinder with name, bounds and texture using 2 vectors for
	 * position and height and 2 doubles for radius.
	 *
	 * @param   name        Name of the shape.
	 * @param   p1          Position of lower cap.
	 * @param   p2          Position of upper cap.
	 * @param   r1          Radius of cylinder at lower cap.
	 * @param   r2          Radius of cylinder at upper cap.
	 * @param   texture     Texture of the shape.
	 */
	public PovCylinder( final String name , final PovVector p1 , final double r1 , final PovVector p2 , final double r2 , final PovTexture texture )
	{
		super( name , texture );

		_p1 = p1;
		_r1 = r1;
		_p2 = p2;
		_r2 = r2;
	}

	/**
	 * Get lower center of cylinder/cone.
	 *
	 * @return  Lower center of cylinder/cone.
	 */
	public final PovVector getP1()
	{
		return _p1;
	}

	/**
	 * Set lower center of cylinder/cone.
	 *
	 * @param   p1  Lower center of cylinder/cone.
	 */
	public final void setP1( final PovVector p1 )
	{
		_p1 = p1;
	}

	/**
	 * Get upper center of cylinder/cone.
	 *
	 * @return  Upper center of cylinder/cone.
	 */
	public final PovVector getP2()
	{
		return _p2;
	}

	/**
	 * Set upper center of cylinder/cone.
	 *
	 * @param   p2  Upper center of cylinder/cone.
	 */
	public final void setP2( final PovVector p2 )
	{
		_p2 = p2;
	}

	/**
	 * Get radius of the cylinder/cone at p1.
	 *
	 * @return  Radius of the cylinder/cone at p1.
	 */
	public final double getR1()
	{
		return _r1;
	}

	/**
	 * Set radius of the cylinder/cone at p1.
	 *
	 * @param   r1  Radius of the cylinder/cone at p1.
	 */
	public final void setR1( final double r1 )
	{
		_r1 = r1;
	}

	/**
	 * Get radius of the cylinder/cone at p2.
	 *
	 * @return  Radius of the cylinder/cone at p2.
	 */
	public final double getR2()
	{
		return _r2;
	}

	/**
	 * Set radius of the cylinder/cone at p2.
	 *
	 * @param   r2  Radius of the cylinder/cone at p2.
	 */
	public final void setR2( final double r2 )
	{
		_r2 = r2;
	}

	public void write( final PovWriter out )
		throws IOException
	{
		out.write( "cone" );
		final String name = getName();
		if ( name != null )
		{
			out.write( " // " );
			out.write( name );
		}
		out.newLine();
		out.writeln( "{" );
		out.indentIn();

		final PovVector p1 = getP1();
		p1.write( out );
		out.write( (int)',' );
		out.write( format( getR1() ) );
		out.newLine();

		final PovVector p2 = getP2();
		p2.write( out );
		out.write( (int)',' );
		out.write( format( getR2() ) );
		out.newLine();

		writeModifiers( out );

		out.indentOut();
		out.writeln( "}" );
	}
}
