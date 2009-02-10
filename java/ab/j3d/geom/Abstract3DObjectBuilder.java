/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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
package ab.j3d.geom;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;
import ab.j3d.model.Scene;

import com.numdata.oss.MathTools;

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
	 * Get index of vertex at the specified point. If no vertex was found at the
	 * specified point, a new one is created.
	 *
	 * @param   point   Point to get vertex index of.
	 *
	 * @return  Vertex index.
	 *
	 * @deprecated We should not used indexed geometry here.
	 */
	public abstract int getVertexIndex( Vector3D point );

	/**
	 * Set coordinates of all vertices in this object. This is only allowed when
	 * no faces have been added yet, since the object integrity is otherwise
	 * lost.
	 *
	 * @param   vertexPoints    Vertex points (one triplet per vertex).
	 */
	public abstract void setVertexCoordinates( double[] vertexPoints );

	/**
	 * Set coordinates of all vertices in this object. This is only allowed when
	 * no faces have been added yet, since the object integrity is otherwise
	 * lost.
	 *
	 * @param   vertexPoints    Vertex points.
	 */
	public abstract void setVertexCoordinates( List<Vector3D> vertexPoints );

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices. This is only allowed when vertex
	 * coordinates have been set and all faces have been added, since the object
	 * integrity is otherwise lost.
	 *
	 * @param   vertexNormals   Vertex normals (one triplet per vertex).
	 */
	public abstract void setVertexNormals( double[] vertexNormals );

	/**
	 * Set vertex normals. Vertex normals are pseudo-normals based on average
	 * face normals at common vertices. This is only allowed when vertex
	 * coordinates have been set and all faces have been added, since the object
	 * integrity is otherwise lost.
	 *
	 * @param   vertexNormals   Vertex normals.
	 */
	public abstract void setVertexNormals( List<Vector3D> vertexNormals );

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
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public void addArc( final Vector3D centerPoint , final double radius , final double startAngle , final double endAngle , final double startWidth , final double endWidth , final Vector3D extrusion , final int stroke , final Material material , final boolean fill )
	{
		final double twoPI = 2.0 * Math.PI;

		double enclosedAngle = ( endAngle - startAngle ) % twoPI;
		if ( enclosedAngle < 0.0 )
			enclosedAngle += twoPI;

		final int     nrSegments = ( enclosedAngle < Math.PI / 4.0 ) ? 3 : (int)( ( 32.0 * enclosedAngle ) / twoPI + 0.5 );
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

					addQuad( point1 , point2 , point2a , point1a , null , stroke , material , fill , true );
				}
				else
				{
					addLine( point1 , point2 , -1 , material );
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

					addQuad( outer1 , outer2 , inner2 , inner1 , null , stroke , material , fill , true );

					if ( isFirst ) addQuad( outer1 , inner1 , extrudedInner1 , extrudedOuter1 , null , stroke , material , fill , true );
					addQuad( inner1 , inner2 , extrudedInner2 , extrudedInner1 , null , stroke , material , fill , true );
					addQuad( inner2 , outer2 , extrudedOuter2 , extrudedInner2 , null , stroke , material , fill , true );
					if ( isLast  ) addQuad( outer2 , outer1 , extrudedOuter1 , extrudedOuter2 , null , stroke , material , fill , true );

					addQuad( extrudedOuter1 , extrudedInner1 , extrudedInner2 , extrudedOuter2 , null , stroke , material , fill , true );
				}
				else
				{
					addQuad( outer1 , inner1 , inner2 , outer2 , null , stroke , material , fill , true );
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
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 */
	public abstract void addCircle( Vector3D centerPoint , double radius , Vector3D normal , Vector3D extrusion , int stroke , Material material , boolean fill );

	/**
	 * Add line primitive.
	 *
	 * @param   point1      First point.
	 * @param   point2      Second point.
	 * @param   stroke      Stroke to use (<code>1</code> if it's a dash linetype,
	 *                      <code>2</code> if it's a dot linetype,
	 *                      <code>3</code> if it's a dashdot linetype,
	 *                      <code>-1</code> otherwise).
	 * @param   material    Material specification to use for shading.
	 */
	public abstract void addLine( Vector3D point1 , Vector3D point2 , int stroke , Material material );

	/**
	 * Add line.
	 *
	 * @param   point1      First point.
	 * @param   point2      Second point.
	 * @param   extrusion   Extrusion to apply (<code>null</code> or 0-vector => no extrusion).
	 * @param   stroke      Stroke to use (<code>1</code> if it's a dash linetype,
	 *                      <code>2</code> if it's a dot linetype,
	 *                      <code>3</code> if it's a dashdot linetype,
	 *                      <code>-1</code> otherwise).
	 * @param   material    Material specification to use for shading.
	 * @param   fill        Create filled shape vs. create wireframe.
	 */
	public void addLine( final Vector3D point1 , final Vector3D point2 , final Vector3D extrusion , final int stroke , final Material material , final boolean fill )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D p1a = point1.plus( extrusion );
			final Vector3D p2a = point2.plus( extrusion );

			addQuad( point1 , p1a , p2a , point2 , null , -1 , material , fill , true );
		}
		else
		{
			addLine( point1 , point2 , stroke , material );
		}
	}

	/**
	 * Add face without texture.
	 *
	 * @param   points          Vertex coordinates that define the face.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( Vector3D[] points , Material material , boolean smooth , boolean twoSided );

	/**
	 * Add face without texture.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( int[] vertexIndices , Material material , boolean smooth , boolean twoSided );

	/**
	 * Add face with texture.
	 *
	 * @param   points          Vertex coordinates that define the face.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Flip texture direction.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( Vector3D[] points , Material material , UVMap uvMap , boolean flipTexture , boolean smooth , boolean twoSided );

	/**
	 * Add face with texture.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Flip texture direction.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public abstract void addFace( int[] vertexIndices , Material material , UVMap uvMap , boolean flipTexture , boolean smooth , boolean twoSided );

	/**
	 * Add face.
	 *
	 * @param   points      Points for vertices that define the face.
	 * @param   material    Material to apply to the face.
	 * @param   textureU    Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV    Vertical texture coordinates (<code>null</code> = none).
	 * @param   smooth      Face is smooth/curved vs. flat.
	 * @param   twoSided    Face is two-sided.
	 *
	 * @deprecated  Use of textureU/V arguments is discouraged.
	 */
	public abstract void addFace( Vector3D[] points , Material material , float[] textureU , float[] textureV , boolean smooth , boolean twoSided );

	/**
	 * Add face.
	 *
	 * @param   vertexIndices   Vertex indices of added face.
	 * @param   material        Material to apply to the face.
	 * @param   textureU        Horizontal texture coordinates (<code>null</code> = none).
	 * @param   textureV        Vertical texture coordinates (<code>null</code> = none).
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 *
	 * @deprecated  Use of textureU/V arguments is discouraged.
	 */
	public abstract void addFace( int[] vertexIndices , Material material , float[] textureU , float[] textureV , boolean smooth , boolean twoSided );

	/**
	 * Add quad primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   point4          Fourth vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   hasBackface     Flag to indicate if face has a backface.
	 */
	public abstract void addQuad( Vector3D point1 , Vector3D point2 , Vector3D point3 , Vector3D point4 , Material material , boolean hasBackface );

	/**
	 * Add textured quad.
	 *
	 * @param   point1      First vertex coordinates.
	 * @param   colorMapU1  First vertex color map U coordinate.
	 * @param   colorMapV1  First vertex color map V coordinate.
	 * @param   point2      Second vertex coordinates.
	 * @param   colorMapU2  Second vertex color map U coordinate.
	 * @param   colorMapV2  Second vertex color map V coordinate.
	 * @param   point3      Third vertex coordinates.
	 * @param   colorMapU3  Thrid vertex color map U coordinate.
	 * @param   colorMapV3  Thrid vertex color map V coordinate.
	 * @param   point4      Fourth vertex coordinates.
	 * @param   colorMapU4  First vertex color map U coordinate.
	 * @param   colorMapV4  First vertex color map V coordinate.
	 * @param   material    Material specification to use for shading.
	 * @param   hasBackface Flag to indicate if face has a backface.
	 */
	public abstract void addQuad( Vector3D point1 , float colorMapU1 , float colorMapV1 , Vector3D point2 , float colorMapU2 , float colorMapV2 , Vector3D point3 , float colorMapU3 , float colorMapV3 , Vector3D point4 , float colorMapU4 , float colorMapV4 , Material material , boolean hasBackface );

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
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 * @param   hasBackface     Flag to indicate if face has a backface.
	 */
	public void addQuad( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D point4 , final Vector3D extrusion , final int stroke , final Material material , final boolean fill , final boolean hasBackface )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );
			final Vector3D point4a = point4.plus( extrusion );

			if ( fill )
			{
				addQuad( point4  , point3  , point2  , point1  , material , false );
				addQuad( point1  , point2  , point2a , point1a , material , false );
				addQuad( point2  , point3  , point3a , point2a , material , false );
				addQuad( point3  , point4  , point4a , point3a , material , false );
				addQuad( point4  , point1  , point1a , point4a , material , false );
				addQuad( point1a , point2a , point3a , point4a , material , false );
			}
			else
			{
				addLine( point1  , point1a , stroke , material );
				addLine( point1  , point2  , stroke , material );
				addLine( point1a , point2a , stroke , material );

				addLine( point2  , point2a , stroke , material );
				addLine( point2  , point3  , stroke , material );
				addLine( point2a , point3a , stroke , material );

				addLine( point3  , point3a , stroke , material );
				addLine( point3  , point4  , stroke , material );
				addLine( point3a , point4a , stroke , material );

				addLine( point4  , point4a , stroke , material );
				addLine( point4  , point1  , stroke , material );
				addLine( point4a , point1a , stroke , material );
			}
		}
		else
		{
			if ( fill )
			{
				addQuad( point1 , point2 , point3 , point4 , material , hasBackface );
			}
			else
			{
				addLine( point1 , point2 , stroke , material );
				addLine( point2 , point3 , stroke , material );
				addLine( point3 , point4 , stroke , material );
				addLine( point4 , point1 , stroke , material );
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
	 * @param   material        Material specification to use for shading.
	 */
	public abstract void addText( String text , Vector3D origin , double height , double rotationAngle , double obliqueAngle , Vector3D extrusion , Material material );

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   hasBackface     Flag to indicate if face has a backface.
	 */
	public abstract void addTriangle( Vector3D point1 , Vector3D point2 , Vector3D point3 , Material material , boolean hasBackface );

	/**
	 * Add triangle primitive.
	 *
	 * @param   point1          First vertex coordinates.
	 * @param   point2          Second vertex coordinates.
	 * @param   point3          Third vertex coordinates.
	 * @param   material        Material specification to use for shading.
	 * @param   hasBackface     Flag to indicate if face has a backface.
	 */
	public abstract void addTriangle( Vector3D point1 , float colorMapU1 , float colorMapV1 , Vector3D point2 , float colorMapU2 , float colorMapV2 , Vector3D point3 , float colorMapU3 , float colorMapV3 , Material material , boolean hasBackface );

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
	 * @param   material        Material specification to use for shading.
	 * @param   fill            Create filled shape vs. create wireframe.
	 * @param   hasBackface     Flag to indicate if face has a backface.
	 */
	public void addTriangle( final Vector3D point1 , final Vector3D point2 , final Vector3D point3 , final Vector3D extrusion , final int stroke , final Material material , final boolean fill , final boolean hasBackface )
	{
		if ( ( extrusion != null ) && !extrusion.almostEquals( Vector3D.INIT ) )
		{
			final Vector3D point1a = point1.plus( extrusion );
			final Vector3D point2a = point2.plus( extrusion );
			final Vector3D point3a = point3.plus( extrusion );

			if ( fill )
			{
				addTriangle( point3  , point2  , point1  ,           material , false );
				addQuad    ( point1  , point2  , point2a , point1a , material , false );
				addQuad    ( point2  , point3  , point3a , point2a , material , false );
				addQuad    ( point3  , point1  , point1a , point3a , material , false );
				addTriangle( point1a , point2a , point3a ,           material , false );
			}
			else
			{
				addLine( point1  , point1a , stroke , material );
				addLine( point1  , point2  , stroke , material );
				addLine( point1a , point2a , stroke , material );

				addLine( point2  , point2a , stroke , material );
				addLine( point2  , point3  , stroke , material );
				addLine( point2a , point3a , stroke , material );

				addLine( point3  , point3a , stroke , material );
				addLine( point3  , point1  , stroke , material );
				addLine( point3a , point1a , stroke , material );
			}
		}
		else
		{
			if ( fill )
			{
				addTriangle( point1 , point2 , point3 , material , hasBackface );
			}
			else
			{
				addLine( point1 , point2 , stroke , material );
				addLine( point2 , point3 , stroke , material );
				addLine( point3 , point1 , stroke , material );
			}
		}
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested,
	 * the ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used
	 * to rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for Z!
	 *
	 * @param   xform               Transform to apply to points.
	 * @param   radii               X coordinates of 2D outline.
	 * @param   zCoordinates        Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   material            Material to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 */
	public void addRotatedObject( final Matrix3D xform , final double[] radii , final double[] zCoordinates , final int detail , final Material material , final boolean smoothCircumference , final boolean closeEnds )
	{
		int[] prevVertexIndices = null;

		for ( int i = 0 ; i < radii.length ; i++ )
		{
			final double radius = radii[ i ];
			final double z      = zCoordinates[ i ];

			/*
			 * Based on 'radius', create a list of vertex indices at this point.
			 */
			final int[] vertexIndices;
			if ( MathTools.almostEqual( radius , 0.0 ) )
			{
				final Vector3D point = ( xform != null ) ? xform.transform( 0.0 , 0.0 , z ) : Vector3D.INIT.set( 0.0 , 0.0 , z );
				vertexIndices = new int[] { getVertexIndex( point ) };
			}
			else
			{
				vertexIndices = new int[ detail ];

				final double stepSize = 2.0 * Math.PI / (double)detail;
				for ( int step = 0 ; step < detail ; step++ )
				{
					final double angle = (double)step * stepSize;
					final double x     =  Math.sin( angle ) * radius;
					final double y     = -Math.cos( angle ) * radius;

					final Vector3D point = ( xform != null ) ? xform.transform( x , y , z ) : Vector3D.INIT.set( x , y , z );
					vertexIndices[ step ] = getVertexIndex( point );
				}

				if ( closeEnds )
				{
					if ( i == 0 )
					{
						addFace( vertexIndices , material , false , false );
					}
					else if ( i == radii.length - 1 )
					{
						final int[] reversed = new int[ detail ];
						for ( int step = 0 ; step < detail ; step++ )
						{
							reversed[ step ] = vertexIndices[ detail - 1 - step ];
						}

						addFace( reversed , material , false , false );
					}
				}
			}

			/*
			 * Construct faces between this and the previous 'row'.
			 */
			if ( prevVertexIndices != null )
			{
				if ( vertexIndices.length > 1 )
				{
					if ( prevVertexIndices.length > 1 )
					{
						for ( int step = 0 ; step < detail ; step++ )
						{
							final int nextStep = ( step + 1 ) % detail;
							addFace( new int[] { prevVertexIndices[ step ] , vertexIndices[ step ] , vertexIndices[ nextStep ] , prevVertexIndices[ nextStep ] } , material , smoothCircumference , false );
						}
					}
					else
					{
						for ( int step = 0 ; step < detail ; step++ )
						{
							addFace( new int[] { prevVertexIndices[ 0 ] , vertexIndices[ step ] , vertexIndices[ ( step + 1 ) % detail ] } , material , smoothCircumference , false );
						}
					}
				}
				else if ( prevVertexIndices.length > 1 )
				{
					for ( int step = 0 ; step < detail ; step++ )
					{
						addFace( new int[] { prevVertexIndices[ step ] , vertexIndices[ 0 ] , prevVertexIndices[ ( step + 1 ) % detail ] } , material , smoothCircumference , false );
					}
				}
			}
			prevVertexIndices = vertexIndices;
		}
	}

	/**
	 * Add extruded shape.
	 *
	 * @param   shape               Shape to add.
	 * @param   extrusion           Extrusion vector (control-point displacement).
	 * @param   transform           Transform to apply.
	 * @param   topMaterial         Material to apply to the top cap.
	 * @param   topFlipTexture      Whether the top texture direction is flipped.
	 * @param   bottomMaterial      Material to apply to the bottom cap.
	 * @param   bottomFlipTexture   Whether the bottom texture direction is flipped.
	 * @param   sideMaterial        Material to apply to the extruded sides.
	 * @param   sideFlipTexture     Whether the side texture direction is flipped.
	 * @param   flatness            Flatness to use.
	 * @param   hasBackface         Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals         If <code>true</code>, normals are flipped to
	 *                              point in the opposite direction.
	 * @param   caps                If <code>true</code>, top and bottom caps are
	 *                              generated.
	 */
	public void addExtrudedShape( final Shape shape , final double flatness , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final boolean topFlipTexture , final Material bottomMaterial , final boolean bottomFlipTexture , final Material sideMaterial , final boolean sideFlipTexture , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		final double  ex            = extrusion.x;
		final double  ey            = extrusion.y;
		final double  ez            = extrusion.z;
		final boolean hasExtrusion  = !MathTools.almostEqual( ex , 0.0 ) || !MathTools.almostEqual( ey , 0.0 ) || !MathTools.almostEqual( ez , 0.0 );
		final boolean flipExtrusion = flipNormals ^ ( ez < 0.0 );

		final UVMap uvMap = new BoxUVMap( Scene.MM , transform ); // @FIXME Retrieve model units instead of assuming millimeters.

		if ( !caps || hasExtrusion )
		{
			final PathIterator pathIterator = shape.getPathIterator( null , flatness );

			final double[] coords = new double[ 6 ];

			Vector3D lastPoint           = null;
			Vector3D lastExtrudedPoint   = null;
			Vector3D moveToPoint         = null;
			Vector3D moveToExtrudedPoint = null;

			while ( !pathIterator.isDone() )
			{
				final int type = pathIterator.currentSegment( coords );
				switch ( type )
				{
					case FlatteningPathIterator.SEG_MOVETO:
					{
						final double shapeX = coords[ 0 ];
						final double shapeY = coords[ 1 ];

						lastPoint = transform.transform( shapeX , shapeY , 0.0 );
						moveToPoint = lastPoint;

						if ( hasExtrusion )
						{
							lastExtrudedPoint = transform.transform( shapeX + ex , shapeY + ey , ez );
							moveToExtrudedPoint = lastExtrudedPoint;
						}
						break;
					}

					case FlatteningPathIterator.SEG_LINETO:
					{
						final double shapeX = coords[ 0 ];
						final double shapeY = coords[ 1 ];

						final Vector3D point = transform.transform( shapeX , shapeY , 0.0 );

						if ( ( lastPoint != null ) && !lastPoint.equals( point ) )
						{
							if ( hasExtrusion )
							{
								final Vector3D extrudedPoint = transform.transform( shapeX + ex , shapeY + ey , ez );

								if ( flipExtrusion )
								{
									addFace( new Vector3D[] { point , extrudedPoint , lastExtrudedPoint , lastPoint} , sideMaterial , uvMap , sideFlipTexture , false , hasBackface );
								}
								else
								{
									addFace( new Vector3D[] { lastPoint , lastExtrudedPoint , extrudedPoint , point } , sideMaterial , uvMap , sideFlipTexture , false , hasBackface );
								}

								lastExtrudedPoint = extrudedPoint;
							}
							else /*if ( !caps )*/
							{
								addFace( new Vector3D[] { lastPoint , point } , sideMaterial , uvMap , sideFlipTexture , false , true );
							}

							lastPoint = point;
						}
						break;
					}

					case FlatteningPathIterator.SEG_CLOSE:
					{
						if ( !lastPoint.equals( moveToPoint ) )
						{
							if ( hasExtrusion )
							{
								if ( flipExtrusion )
								{
									addFace( new Vector3D[] { moveToPoint , moveToExtrudedPoint , lastExtrudedPoint , lastPoint } , sideMaterial , uvMap , sideFlipTexture , false , hasBackface );
								}
								else
								{
									addFace( new Vector3D[] { lastPoint , lastExtrudedPoint , moveToExtrudedPoint , moveToPoint } , sideMaterial , uvMap , sideFlipTexture , false , hasBackface );
								}
								lastExtrudedPoint = moveToExtrudedPoint;
							}
							else /*if ( !caps )*/
							{
								addFace( new Vector3D[] { lastPoint , moveToPoint } , sideMaterial , uvMap , sideFlipTexture , false , true );
							}
						}

						lastPoint = moveToPoint;
						break;
					}
				}

				pathIterator.next();
			}
		}

		if ( caps )
		{
			final TriangulatorFactory triangulatorFactory = TriangulatorFactory.newInstance();
			final Triangulator        triangulator        = triangulatorFactory.newTriangulator();

			triangulator.setFlatness( flatness );

			final Vector3D topNormal    = Vector3D.INIT.set( 0.0 , 0.0 , flipExtrusion ? -1.0 :  1.0 );
			final Vector3D bottomNormal = Vector3D.INIT.set( 0.0 , 0.0 , flipExtrusion ?  1.0 : -1.0 );

			triangulator.setNormal( bottomNormal );
			addTriangulatedShape( transform , shape , triangulator , bottomMaterial , uvMap , bottomFlipTexture , hasBackface );

			if ( hasExtrusion )
			{
				// @TODO Should not triangulate twice for a performance gain, since the triangulation for top and bottom caps is essentially the same.
				triangulator.setNormal( topNormal );
				addTriangulatedShape( transform.plus( extrusion ) , shape , triangulator , topMaterial , uvMap , topFlipTexture , hasBackface );
			}
		}
	}

	/**
	 * Add triangulated shape.
	 *
	 * @param   transform       Transform from 2D to 3D coordinates.
	 * @param   shape           Shape to add.
	 * @param   shapeNormal     Normal to use for shape.
	 * @param   flatness        Flatness used to approximate curves.
	 * @param   material        Material to apply to the bottom cap.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Whether the bottom texture direction is flipped.
	 * @param   twoSided        Resulting face will be two-sided (has backface).
	 */
	public void addTriangulatedShape( final Matrix3D transform , final Shape shape , final Vector3D shapeNormal , final double flatness , final Material material , final UVMap uvMap , final boolean flipTexture , final boolean twoSided )
	{
		final TriangulatorFactory triangulatorFactory = TriangulatorFactory.newInstance();

		final Triangulator triangulator = triangulatorFactory.newTriangulator();
		triangulator.setFlatness( flatness );
		triangulator.setNormal( shapeNormal );

		addTriangulatedShape( transform , shape , triangulator , material , uvMap , flipTexture , twoSided );
	}

	/**
	 * Add triangulated shape.
	 *
	 * @param   transform       Transform from 2D to 3D coordinates.
	 * @param   shape           Shape to add.
	 * @param   triangulator    Triangulator to use.
	 * @param   material        Material to apply to the bottom cap.
	 * @param   uvMap           UV-map used to generate texture coordinates.
	 * @param   flipTexture     Whether the bottom texture direction is flipped.
	 * @param   twoSided        Resulting face will be two-sided (has backface).
	 */
	public void addTriangulatedShape( final Matrix3D transform , final Shape shape , final Triangulator triangulator , final Material material , final UVMap uvMap , final boolean flipTexture , final boolean twoSided )
	{
		final Triangulation triangulation = triangulator.triangulate( shape );
		final List<Vector3D> vertices = triangulation.getVertices( transform );

		final Vector3D[] points = new Vector3D[ 3 ];

		for ( final int[] triangle : triangulation.getTriangles() )
		{
			points[ 2 ] = vertices.get( triangle[ 0 ] );
			points[ 1 ] = vertices.get( triangle[ 1 ] );
			points[ 0 ] = vertices.get( triangle[ 2 ] );

			addFace( points , material , uvMap , flipTexture , false , twoSided );
		}
	}
}
