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
package ab.j3d;

/**
 * This abstract class defines an interface through which an 3D object can easily
 * be created from (mostly 2D) shapes.
 * <p />
 * Implementors only need to implement simple shapes, since more comlex ones are
 * automatically converted into simpler elements. If an implementation can provide
 * a better/more direct implementation of a complex shape, such an implementation
 * is always preferred.
 *
 * @author  HRM Bleumink
 * @author  Peter S. Heijnen
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public abstract class Abstract3DObjectBuilder
{
	/**
	 * Construct builder.
	 */
	protected Abstract3DObjectBuilder()
	{
	}

	/**
	 * Add arc.
	 *
	 * @param   centerPoint     Center-point of circle on which the arc is defined.
	 * @param   radius          Radius of circle on which the arc is defined.
	 * @param   startAngle      Start-angle of arc relative to X-axis (radians).
	 * @param   endAngle        End-angle of arc relative to X-axis (radians).
	 * @param   startWidth      Start-width of arc (0.0 => no width).
	 * @param   endWidth        End-width of arc (0.0 => no width).
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addArc( final Vector3D centerPoint , final double radius , final double startAngle , final double endAngle , final double startWidth , final double endWidth , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		double enclosedAngle = ( endAngle - startAngle );
		while ( enclosedAngle < 0.0 )
			enclosedAngle += 2.0 * Math.PI;

		final int     nrSegments = 32;
		final double  angleStep  = enclosedAngle / (double)nrSegments;
		final boolean extruded   = ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT );

		if ( ( startWidth == 0.0 ) && ( endWidth == 0.0 ) )
		{
			double angle       = startAngle;
			double cos         = Math.cos( angle );
			double sin         = Math.sin( angle );

			Vector3D point1 = centerPoint.plus( radius * cos , radius * sin , 0.0 );

			for ( int i = 0 ; i < nrSegments ; i++ )
			{
				angle += angleStep;
				cos   = Math.cos( angle );
				sin   = Math.sin( angle );

				final Vector3D point2 = centerPoint.plus( radius * cos , radius * sin , 0.0 );

				if ( extruded )
				{
					final Vector3D point1a = point1.plus( extrusion );
					final Vector3D point2a = point2.plus( extrusion );

					addQuad( point1 , point2 , point2a , point1a , null , stroke , textureSpec , fill );
				}
				else
				{
					addLine( point1 , point2 , -1 , textureSpec );
				}

				point1 = point2;
			}
		}
		else
		{
			final double radiusStep = ( endWidth - startWidth ) / (double)( 2 * nrSegments );

			double angle       = startAngle;
			double cos         = Math.cos( angle );
			double sin         = Math.sin( angle );
			double innerRadius = radius - startWidth / 2.0;
			double outerRadius = innerRadius + startWidth;

			Vector3D inner1 = centerPoint.plus( innerRadius * cos , innerRadius * sin , 0.0 );
			Vector3D outer1 = centerPoint.plus( outerRadius * cos , outerRadius * sin , 0.0 );

			for ( int i = 0 ; i < nrSegments ; i++ )
			{
				angle       += angleStep;
				innerRadius -= radiusStep;
				outerRadius += radiusStep;
				cos         = Math.cos( angle );
				sin         = Math.sin( angle );

				final Vector3D inner2 = centerPoint.plus( innerRadius * cos , innerRadius * sin , 0.0 );
				final Vector3D outer2 = centerPoint.plus( outerRadius * cos , outerRadius * sin , 0.0 );

				if ( extruded )
				{
					final Vector3D extrudedInner1 = inner1.plus( extrusion );
					final Vector3D extrudedOuter1 = outer1.plus( extrusion );
					final Vector3D extrudedOuter2 = outer2.plus( extrusion );
					final Vector3D extrudedInner2 = inner2.plus( extrusion );

					final boolean isFirst = ( i == 0 );
					final boolean isLast  = ( i == ( nrSegments -1 ) );

					addQuad( outer1 , outer2 , inner2 , inner1 , null , stroke , textureSpec , fill );

					if ( isFirst ) addQuad( outer1 , inner1 , extrudedInner1 , extrudedOuter1 , null , stroke , textureSpec , fill );
					addQuad( inner1 , inner2 , extrudedInner2 , extrudedInner1 , null , stroke , textureSpec , fill );
					addQuad( inner2 , outer2 , extrudedOuter2 , extrudedInner2 , null , stroke , textureSpec , fill );
					if ( isLast  ) addQuad( outer2 , outer1 , extrudedOuter1 , extrudedOuter2 , null , stroke , textureSpec , fill );

					addQuad( extrudedOuter1 , extrudedInner1 , extrudedInner2 , extrudedOuter2 , null , stroke , textureSpec , fill );
				}
				else
				{
					addQuad( outer1 , inner1 , inner2 , outer2 , null , stroke , textureSpec , fill );
				}

				inner1 = inner2;
				outer1 = outer2;
			}
		}
	}

	/**
	 * Add circle primitive.
	 *
	 * @param   centerPoint     Center-point of circle.
	 * @param   radius          Radius of circle.
	 * @param   normal          Normal pointing out of the circle's center.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public abstract void addCircle( final Vector3D centerPoint , final double radius , final Vector3D normal , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill );

	/**
	 * Add line primitive.
	 *
	 * @param   point1          First point.
	 * @param   point2          Second point.
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 */
	public abstract void addLine( final Vector3D point1 , final Vector3D point2 , final int stroke , final TextureSpec textureSpec );

	/**
	 * Add line.
	 *
	 * @param   point1          First point.
	 * @param   point2          Second point.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addLine( final Vector3D point1 , final Vector3D point2 , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D p1a = point1.plus( extrusion );
			final Vector3D p2a = point2.plus( extrusion );

			addQuad( point1 , p1a , p2a , point2 , null , -1 , textureSpec , fill );
		}
		else
		{
			addLine( point1 , point2 , stroke , textureSpec );
		}
	}

	/**
	 * Add quad primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   textureSpec     Texture specification to use for shading.
	 */
	public abstract void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final TextureSpec textureSpec );

	/**
	 * Add quad with optional extrusion.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );
			final Vector3D point4a = point4.plus( extrusion );

			if ( fill )
			{
				addQuad( point4  , point3  , point2  , point1  , textureSpec );
				addQuad( point1  , point2  , point2a , point1a , textureSpec );
				addQuad( point2  , point3  , point3a , point2a , textureSpec );
				addQuad( point3  , point4  , point4a , point3a , textureSpec );
				addQuad( point4  , point1  , point1a , point4a , textureSpec );
				addQuad( point1a , point2a , point3a , point4a , textureSpec );
			}
			else
			{
				addLine( point1  , point1a , stroke , textureSpec );
				addLine( point1  , point2  , stroke , textureSpec );
				addLine( point1a , point2a , stroke , textureSpec );

				addLine( point2  , point2a , stroke , textureSpec );
				addLine( point2  , point3  , stroke , textureSpec );
				addLine( point2a , point3a , stroke , textureSpec );

				addLine( point3  , point3a , stroke , textureSpec );
				addLine( point3  , point4  , stroke , textureSpec );
				addLine( point3a , point4a , stroke , textureSpec );

				addLine( point4  , point4a , stroke , textureSpec );
				addLine( point4  , point1  , stroke , textureSpec );
				addLine( point4a , point1a , stroke , textureSpec );
			}
		}
		else
		{
			if ( fill )
			{
				addQuad( point1 , point2 , point3 , point4 , textureSpec );
			}
			else
			{
				addLine( point1 , point2 , stroke , textureSpec );
				addLine( point2 , point3 , stroke , textureSpec );
				addLine( point3 , point4 , stroke , textureSpec );
				addLine( point4 , point1 , stroke , textureSpec );
			}
		}
	}

	/**
	 * Add text.
	 *
	 * @param   text            Text value.
	 * @param   origin          Starting point
	 * @param   height          Height.
	 * @param   rotationAngle   Rotation angle.
	 * @param   obliqueAngle    Oblique angle.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   textureSpec     Texture specification to use for shading.
	 */
	public abstract void addText( final String text , final Vector3D origin , final double height , final double rotationAngle , final double obliqueAngle , final Vector3D extrusion , final TextureSpec textureSpec );

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   textureSpec     Texture specification to use for shading.
	 */
	public abstract void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final TextureSpec textureSpec );

	/**
	 * Add triangle with optional extrusion.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   extrusion       Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke          Stroke to use (<code>1</code> if it's a dash linetype,
	 *                          <code>2</code> if it's a dot linetype,
	 *                          <code>3</code> if it's a dashdot linetype,
	 *                          <code>-1</code> otherwise).
	 * @param   textureSpec     Texture specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D extrusion , final int stroke , final TextureSpec textureSpec , final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );

			if ( fill )
			{
				addTriangle( point3  , point2  , point1  ,           textureSpec );
				addQuad    ( point1  , point2  , point2a , point1a , textureSpec );
				addQuad    ( point2  , point3  , point3a , point2a , textureSpec );
				addQuad    ( point3  , point1  , point1a , point3a , textureSpec );
				addTriangle( point1a , point2a , point3a ,           textureSpec );
			}
			else
			{
				addLine( point1  , point1a , stroke , textureSpec );
				addLine( point1  , point2  , stroke , textureSpec );
				addLine( point1a , point2a , stroke , textureSpec );

				addLine( point2  , point2a , stroke , textureSpec );
				addLine( point2  , point3  , stroke , textureSpec );
				addLine( point2a , point3a , stroke , textureSpec );

				addLine( point3  , point3a , stroke , textureSpec );
				addLine( point3  , point1  , stroke , textureSpec );
				addLine( point3a , point1a , stroke , textureSpec );
			}
		}
		else
		{
			if ( fill )
			{
				addTriangle( point1 , point2 , point3 , textureSpec );
			}
			else
			{
				addLine( point1 , point2 , stroke , textureSpec );
				addLine( point2 , point3 , stroke , textureSpec );
				addLine( point3 , point1 , stroke , textureSpec );
			}
		}
	}
}
