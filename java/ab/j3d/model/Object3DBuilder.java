/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
package ab.j3d.model;

import java.awt.geom.Ellipse2D;

import ab.j3d.Abstract3DObjectBuilder;
import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

/**
 * This class provides an implementation of the {@link Abstract3DObjectBuilder}
 * class for creating an {@link Object3D} instance.
 *
 * @see     Abstract3DObjectBuilder
 * @see     Object3D
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Object3DBuilder
	extends Abstract3DObjectBuilder
{
	/**
	 * The {@link Object3D} being build.
	 */
	private Object3D _target;

	/**
	 * Construct builder.
	 */
	public Object3DBuilder()
	{
		_target = new Object3D();
	}

	/**
	 * Get {@link Object3D} that was build.
	 *
	 * @return  The {@link Object3D} that was build.
	 */
	public Object3D getObject3D()
	{
		return _target;
	}

	public void addLine( final Vector3D point1 , final Vector3D point2 , final int stroke , final TextureSpec textureSpec )
	{
		_target.addFace( new Vector3D[] { point1 , point2 } , textureSpec , false , true );
	}

	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final TextureSpec textureSpec )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 } , textureSpec , false , true );
	}

	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final TextureSpec textureSpec )
	{
		_target.addFace( new Vector3D[] { point1 , point2 , point3 , point4 } , textureSpec , false , true );
	}

	public void addCircle( final Vector3D centerPoint , final double radius , final Vector3D normal , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		final Matrix3D  base      = Matrix3D.getPlaneTransform( centerPoint , normal , true );
		final Ellipse2D ellipse2d = new Ellipse2D.Double( -radius , -radius , radius * 2.0 , radius * 2.0 );

		ExtrudedObject2D.generate( _target , ellipse2d , extrusion , base , textureSpec , radius * 0.02 , true );
	}

	public void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final TextureSpec textureSpec )
	{
	}
}
