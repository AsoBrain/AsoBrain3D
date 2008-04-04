/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2008-2008 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
