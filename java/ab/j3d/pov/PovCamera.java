/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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

import ab.j3d.*;
import com.numdata.oss.io.*;

/**
 * Pov Camera
 * <pre>
 * camera // name
 * {
 *     location  &lt; x , y , z &gt;
 *     up        &lt; 0 , 0 , 1 &gt;
 *     right     &lt; 1.33 , 0 , 0 &gt;
 *     sky       &lt; 0 , 0 , 1 &gt;
 *     look_at   &lt; x , y , z &gt;
 * }
 * </pre>
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovCamera
	extends PovGeometry
{
	/**
	 * Location of camera.
	 */
	private PovVector _location;

	/**
	 * Direction/target of camera.
	 */
	private PovVector _lookAt;

	/**
	 * Right vector of camera. Also determines aspect ratio (e.g. "&lt;1.33 , 0.0 , 0.0&gt;" for a 4:3 aspect ratio).
	 */
	private PovVector _right;

	/**
	 * Camera opening angle in decimal degrees (aperture; typically 45).
	 */
	private double _angle;

	/**
	 * Construct camera.
	 * <p>
	 * The standard ratio used in POV-Ray is 4:3, wich means the rendered
	 * image will get deformed if resolutions when a ratio other than 4:3
	 * is used. Since the resulting image does not have a fixed ratio (the user
	 * can choose any size, for example), the ratio also needs to be specified
	 * in the POV-Ray camera.
	 *
	 * @param   name            Name of camera object.
	 * @param   view2scene      Transforms view to POV-scene coordinates.
	 * @param   angle           Camera opening angle in decimal degrees
	 *                          (aperture; typically 45).
	 * @param   aspectRatio     Aspect ratio of image (for square pixels:
	 *                          width / height).
	 */
	public PovCamera( final String name, final Matrix3D view2scene, final double angle, final double aspectRatio )
	{
		this( name, null, null, new PovVector( aspectRatio, 0.0, 0.0 ), angle );

		setTransform( new PovMatrix( new double[]
			{
				 view2scene.xx,  view2scene.yx,  view2scene.zx,
				 view2scene.xy,  view2scene.yy,  view2scene.zy,
				-view2scene.xz, -view2scene.yz, -view2scene.zz,
				 view2scene.xo,  view2scene.yo,  view2scene.zo
			} ) );
	}

	/**
	 * Construct camera.
	 *
	 * @param   name        Name of camera object.
	 * @param   x           X coordinate location of camera in scene.
	 * @param   y           Y coordinate location of camera in scene.
	 * @param   z           Z coordinate location of camera in scene.
	 * @param   tx          X coordinate location of target point in scene.
	 * @param   ty          Y coordinate location of target point in scene.
	 * @param   tz          Z coordinate location of target point in scene.
	 * @param   angle       Camera opening angle in decimal degrees (aperture; typically 45).
	 */
	public PovCamera( final String name , final double x , final double y , final double z , final double tx , final double ty , final double tz , final double angle )
	{
		this( name , new PovVector( x , y , z ) , new PovVector( tx , ty , tz ) , null , angle );
	}

	/**
	 * Construct camera.
	 *
	 * @param   name        Name of camera object.
	 * @param   location    Location of camera in scene.
	 * @param   lookAt      Location of target point in scene.
	 * @param   angle       Camera opening angle in decimal degrees (aperture; typically 45).
	 */
	public PovCamera( final String name , final PovVector location , final PovVector lookAt , final double angle )
	{
		this ( name , location , lookAt , null , angle );
	}

	/**
	 * Construct camera.
	 *
	 * @param   name        Name of camera object.
	 * @param   location    Location of camera in scene.
	 * @param   lookAt      Location of target point in scene.
	 * @param   right       Right vector of camera (optional).
	 * @param   angle       Camera opening angle in decimal degrees (aperture; typically 45).
	 */
	public PovCamera( final String name , final PovVector location , final PovVector lookAt , final PovVector right , final double angle )
	{
		super( name );

		_location = location;
		_lookAt   = lookAt;
		_angle    = angle;
		_right    = ( ( right != null ) && ( right.getX() > 0.0 ) ) ? right : new PovVector( 1.33 , 0.0 , 0.0 );
	}

	@Override
	public void write( final IndentingWriter out )
		throws IOException
	{
		out.write( "camera" );
		final String name = getName();
		if ( name != null )
		{
			out.write( " // " );
			out.write( name );
		}
		out.newLine();
		out.writeln( "{" );
		out.indentIn();

		final PovVector right = getRight();
		out.write( "right  " );
		right.write( out );
		out.newLine();

		out.write( "angle  " );
		out.write( format( getAngle() ) );
		out.newLine();

		final PovVector location = getLocation();
		final PovVector lookAt   = getLookAt();

		if ( ( location != null ) && ( lookAt != null ) )
		{
			out.write( "location\t" );
			location.write( out );
			out.newLine();

			out.writeln( "up\t\t<0,0,1>" );

			out.writeln( "sky\t\t<0,0,1>" );

			out.write( "look_at\t" );
			lookAt.write( out );
			out.newLine();
		}

		writeModifiers( out );

		out.indentOut();
		out.writeln( "}" );
	}

	/**
	 * Get location of camera.
	 *
	 * @return  Location of camera.
	 */
	public final PovVector getLocation()
	{
		return _location;
	}

	/**
	 * Set location of camera.
	 *
	 * @param   location    Location of camera.
	 */
	public final void setLocation( final PovVector location )
	{
		_location = location;
	}

	/**
	 * Get direction/target of camera.
	 *
	 * @return  Direction/target of camera.
	 */
	public final PovVector getLookAt()
	{
		return _lookAt;
	}

	/**
	 * Set direction/target of camera.
	 *
	 * @param   lookAt  Direction/target of camera.
	 */
	public final void setLookAt( final PovVector lookAt )
	{
		_lookAt = lookAt;
	}

	/**
	 * Get right vector of camera. Also determines aspect ratio (e.g.
	 * "&lt;1.33 , 0.0 , 0.0&gt;" for a 4:3 aspect ratio).
	 *
	 * @return  Right vector of camera.
	 */
	public final PovVector getRight()
	{
		return _right;
	}

	/**
	 * Set right vector of camera. Also determines aspect ratio (e.g.
	 * "&lt;1.33 , 0.0 , 0.0&gt;" for a 4:3 aspect ratio).
	 *
	 * @param   right   Right vector of camera.
	 */
	public final void setRight( final PovVector right )
	{
		_right = right;
	}

	/**
	 * Get camera opening angle (aperture).
	 *
	 * @return  Camera opening angle (decimal degrees; typically 45).
	 */
	public final double getAngle()
	{
		return _angle;
	}

	/**
	 * Set camera opening angle (aperture).
	 *
	 * @param   angle   Camera opening angle (decimal degrees; typically 45).
	 */
	public final void setAngle( final double angle )
	{
		_angle = angle;
	}
}
