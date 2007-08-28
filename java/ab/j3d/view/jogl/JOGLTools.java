/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2007
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
package ab.j3d.view.jogl;

import java.awt.Color;
import javax.media.opengl.GL;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

/**
 * Utility methods related to JOGL implementation of view model.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private JOGLTools()
	{
	}

	/**
	 * Paint 3D object on GL context.
	 *
	 * @param   gl              GL context.
	 * @param   object3D        Object to draw.
	 * @param   node2gl         Transformation from node to current GL transform.
	 * @param   fill            Fill polygons.
	 * @param   fillColor       Override color to use for filling.
	 * @param   outline         Draw polygon outlines.
	 * @param   outlineColor    Override color to use for outlines.
	 * @param   useAlternative  Use alternative vs. regular object colors.
	 */
	public static void paintObject3D( final GL gl , final Object3D object3D , final Matrix3D node2gl , final boolean fill , final Color fillColor , final boolean outline , final Color outlineColor , final boolean useAlternative )
	{
		/*
		 * Push the current matrix stack down by one
		 */
		gl.glPushMatrix();

		/*
		 * Set the object transform
		 */
		glMultMatrixd( gl , node2gl );

		/*
		 * Draw the object3d.
		 */
		for ( int i = 0 ; i < object3D.getFaceCount(); i++ )
		{
			final Face3D face = object3D.getFace( i );

			if ( fill )
			{
				if ( fillColor != null )
				{
					final float[] argb = fillColor.getRGBComponents( null );
					gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
					gl.glDisable( GL.GL_LIGHTING );
				}
				else
				{
					gl.glShadeModel( face.isSmooth() ? GL.GL_SMOOTH : GL.GL_FLAT );
					setMaterial( gl , face.getMaterial() );
					gl.glEnable( GL.GL_LIGHTING );
				}

				gl.glPolygonMode( face.isTwoSided() ? GL.GL_FRONT_AND_BACK : GL.GL_FRONT , GL.GL_FILL );
				fillFace( gl , object3D.getFace( i ) );
			}

			if ( outline )
			{
				final Color color;
				if ( outlineColor != null )
				{
					color = outlineColor;
				}
				else if ( useAlternative && ( object3D.alternateOutlinePaint instanceof Color ) )
				{
					color = (Color)object3D.alternateOutlinePaint;
				}
				else if ( object3D.outlinePaint instanceof Color )
				{
					color = (Color)object3D.outlinePaint;
				}
				else
				{
					color = Color.WHITE;
				}

				final float[] argb = color.getRGBComponents( null );
				gl.glColor4f( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
				gl.glDisable( GL.GL_LIGHTING ) ;
				gl.glPolygonMode( GL.GL_FRONT , GL.GL_FILL );
				drawFace( gl , face );
			}
		}

		/*
		 * Pop matrix stack, replace current matrix with one below it on the stack.
		 */
		gl.glPopMatrix();
	}

	/**
	 * Draw 3D face (outline) on GL context.
	 *
	 * @param   gl      GL context.
	 * @param   face    Face draw.
	 */
	private static void drawFace( final GL gl , final Face3D face )
	{
		final Object3D object            = face.getObject();
		final double[] vertexCoordinates = object.getVertexCoordinates();

		final int vertexCount = face.getVertexCount();
		if ( vertexCount > 1 )
		{
			final int[] faceVertexIndices = face.getVertexIndices();

			gl.glBegin( GL.GL_LINES );

			final int viLast = faceVertexIndices[ vertexCount - 1 ] * 3;
			double v1x = vertexCoordinates[ viLast     ];
			double v1y = vertexCoordinates[ viLast + 1 ];
			double v1z = vertexCoordinates[ viLast + 2 ];

			for ( int i = 0 ; i < vertexCount ; i++ )
			{
				final int vi2 = faceVertexIndices[ i ] * 3;

				final double v2x = vertexCoordinates[ vi2     ];
				final double v2y = vertexCoordinates[ vi2 + 1 ];
				final double v2z = vertexCoordinates[ vi2 + 2 ];

				gl.glVertex3d( v1x, v1y, v1z );
				gl.glVertex3d( v2x, v2y, v2z );

				v1x = v2x;
				v1y = v2y;
				v1z = v2z;
			}

			gl.glEnd();
		}
	}

	/**
	 * Fill 3D face on GL context.
	 *
	 * @param   gl      GL context.
	 * @param   face    Face draw.
	 */
	private static void fillFace( final GL gl , final Face3D face )
	{
		final Object3D object            = face.getObject();
		final double[] vertexCoordinates = object.getVertexCoordinates();

		final int vertexCount = face.getVertexCount();
		switch ( vertexCount )
		{
			case 0 :
			case 1 :
				break;

			case 2 :
				{
					final int[] faceVertexIndices = face.getVertexIndices();

					final int vi1 = faceVertexIndices[ 0 ] * 3;
					final int vi2 = faceVertexIndices[ 1 ] * 3;

					final double v1x = vertexCoordinates[ vi1     ];
					final double v1y = vertexCoordinates[ vi1 + 1 ];
					final double v1z = vertexCoordinates[ vi1 + 2 ];
					final double v2x = vertexCoordinates[ vi2     ];
					final double v2y = vertexCoordinates[ vi2 + 1 ];
					final double v2z = vertexCoordinates[ vi2 + 2 ];

					drawLine( gl , v1x , v1y , v1z , v2x , v2y , v2z );
				}
				break;

			case 3 :
				{
					final int[] faceVertexIndices = face.getVertexIndices();

					final int vi1 = faceVertexIndices[ 0 ] * 3;
					final int vi2 = faceVertexIndices[ 1 ] * 3;
					final int vi3 = faceVertexIndices[ 2 ] * 3;

					final double v1x = vertexCoordinates[ vi1     ];
					final double v1y = vertexCoordinates[ vi1 + 1 ];
					final double v1z = vertexCoordinates[ vi1 + 2 ];
					final double v2x = vertexCoordinates[ vi2     ];
					final double v2y = vertexCoordinates[ vi2 + 1 ];
					final double v2z = vertexCoordinates[ vi2 + 2 ];
					final double v3x = vertexCoordinates[ vi3     ];
					final double v3y = vertexCoordinates[ vi3 + 1 ];
					final double v3z = vertexCoordinates[ vi3 + 2 ];

					drawTriangle( gl , v1x , v1y , v1z , v2x , v2y , v2z , v3x , v3y , v3z );
				}
				break;

			case 4 :
				{
					final int[] faceVertexIndices = face.getVertexIndices();

					final int vi1 = faceVertexIndices[ 0 ] * 3;
					final int vi2 = faceVertexIndices[ 1 ] * 3;
					final int vi3 = faceVertexIndices[ 2 ] * 3;
					final int vi4 = faceVertexIndices[ 3 ] * 3;

					final double v1x = vertexCoordinates[ vi1     ];
					final double v1y = vertexCoordinates[ vi1 + 1 ];
					final double v1z = vertexCoordinates[ vi1 + 2 ];
					final double v2x = vertexCoordinates[ vi2     ];
					final double v2y = vertexCoordinates[ vi2 + 1 ];
					final double v2z = vertexCoordinates[ vi2 + 2 ];
					final double v3x = vertexCoordinates[ vi3     ];
					final double v3y = vertexCoordinates[ vi3 + 1 ];
					final double v3z = vertexCoordinates[ vi3 + 2 ];
					final double v4x = vertexCoordinates[ vi4     ];
					final double v4y = vertexCoordinates[ vi4 + 1 ];
					final double v4z = vertexCoordinates[ vi4 + 2 ];

					drawQuad( gl , v1x , v1y , v1z , v2x , v2y , v2z , v3x , v3y , v3z , v4x , v4y , v4z );
				}
				break;

			default :
				{
					final int[] triangles = face.triangulate();
					if ( triangles != null )
					{
						// Draw the triangles.
						final int[] faceVertexIndices = face.getVertexIndices();

						for ( int j = 0 ; j < triangles.length ; j = j + 3 )
						{
							final int vi1 = faceVertexIndices[ triangles[ j     ] ] * 3;
							final int vi2 = faceVertexIndices[ triangles[ j + 1 ] ] * 3;
							final int vi3 = faceVertexIndices[ triangles[ j + 2 ] ] * 3;

							final double v1x = vertexCoordinates[ vi1     ];
							final double v1y = vertexCoordinates[ vi1 + 1 ];
							final double v1z = vertexCoordinates[ vi1 + 2 ];
							final double v2x = vertexCoordinates[ vi2     ];
							final double v2y = vertexCoordinates[ vi2 + 1 ];
							final double v2z = vertexCoordinates[ vi2 + 2 ];
							final double v3x = vertexCoordinates[ vi3     ];
							final double v3y = vertexCoordinates[ vi3 + 1 ];
							final double v3z = vertexCoordinates[ vi3 + 2 ];

							drawTriangle( gl , v1x , v1y , v1z , v2x , v2y , v2z , v3x , v3y , v3z );
						}
					}
				}
				break;
		}
	}

	/**
	 * Draw line using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 */
	public static void drawLine( final GL gl , final float v1X , final float v1Y , final float v1Z ,
	                                           final float v2X , final float v2Y , final float v2Z )
	{
		gl.glBegin( GL.GL_LINES );
		gl.glVertex3f( v1X , v1Y , v1Z );
		gl.glVertex3f( v2X , v2Y , v2Z );
		gl.glEnd();
	}

	/**
	 * Draw line using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 */
	public static void drawLine( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                                           final double v2X , final double v2Y , final double v2Z )
	{
		gl.glBegin( GL.GL_LINES );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glEnd();
	}

	/**
	 * Draw quad using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 * @param   v3X     X coordinate of third vertex.
	 * @param   v3Y     Y coordinate of third vertex.
	 * @param   v3Z     Z coordinate of third vertex.
	 * @param   v4X     X coordinate of fourth vertex.
	 * @param   v4Y     Y coordinate of fourth vertex.
	 * @param   v4Z     Z coordinate of fourth vertex.
	 */
	public static void drawQuad( final GL gl , final float v1X , final float v1Y , final float v1Z ,
	                                           final float v2X , final float v2Y , final float v2Z ,
	                                           final float v3X , final float v3Y , final float v3Z ,
	                                           final float v4X , final float v4Y , final float v4Z )
	{
		gl.glBegin( GL.GL_QUADS );
		gl.glVertex3f( v1X , v1Y , v1Z );
		gl.glVertex3f( v2X , v2Y , v2Z );
		gl.glVertex3f( v3X , v3Y , v3Z );
		gl.glVertex3f( v4X , v4Y , v4Z );
		gl.glEnd();
	}

	/**
	 * Draw quad using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 * @param   v3X     X coordinate of third vertex.
	 * @param   v3Y     Y coordinate of third vertex.
	 * @param   v3Z     Z coordinate of third vertex.
	 * @param   v4X     X coordinate of fourth vertex.
	 * @param   v4Y     Y coordinate of fourth vertex.
	 * @param   v4Z     Z coordinate of fourth vertex.
	 */
	public static void drawQuad( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                                           final double v2X , final double v2Y , final double v2Z ,
	                                           final double v3X , final double v3Y , final double v3Z ,
	                                           final double v4X , final double v4Y , final double v4Z )
	{
		gl.glBegin( GL.GL_QUADS );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glVertex3d( v3X , v3Y , v3Z );
		gl.glVertex3d( v4X , v4Y , v4Z );
		gl.glEnd();
	}

	/**
	 * Draw triangle using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 * @param   v3X     X coordinate of third vertex.
	 * @param   v3Y     Y coordinate of third vertex.
	 * @param   v3Z     Z coordinate of third vertex.
	 */
	public static void drawTriangle( final GL gl , final float v1X , final float v1Y , final float v1Z ,
	                                               final float v2X , final float v2Y , final float v2Z ,
	                                               final float v3X , final float v3Y , final float v3Z )
	{
		gl.glBegin( GL.GL_TRIANGLES );
		gl.glVertex3f( v1X , v1Y , v1Z );
		gl.glVertex3f( v2X , v2Y , v2Z );
		gl.glVertex3f( v3X , v3Y , v3Z );
		gl.glEnd();
	}

	/**
	 * Draw triangle using GL.
	 *
	 * @param   gl      GL context.
	 * @param   v1X     X coordinate of first vertex.
	 * @param   v1Y     Y coordinate of first vertex.
	 * @param   v1Z     Z coordinate of first vertex.
	 * @param   v2X     X coordinate of second vertex.
	 * @param   v2Y     Y coordinate of second vertex.
	 * @param   v2Z     Z coordinate of second vertex.
	 * @param   v3X     X coordinate of third vertex.
	 * @param   v3Y     Y coordinate of third vertex.
	 * @param   v3Z     Z coordinate of third vertex.
	 */
	public static void drawTriangle( final GL gl , final double v1X , final double v1Y , final double v1Z ,
	                                               final double v2X , final double v2Y , final double v2Z ,
	                                               final double v3X , final double v3Y , final double v3Z )
	{
		gl.glBegin( GL.GL_TRIANGLES );
		gl.glVertex3d( v1X , v1Y , v1Z );
		gl.glVertex3d( v2X , v2Y , v2Z );
		gl.glVertex3d( v3X , v3Y , v3Z );
		gl.glEnd();
	}

	/**
	 * Clear GL canvas.
	 *
	 * @param   gl      GL context.
	 * @param   color   Color to clear canvas with.
	 */
	public static void glClearColor( final GL gl , final Color color )
	{
		final float[] argb  = new float[ 4 ];
		color.getRGBComponents( argb );

		gl.glClearColor( argb[ 0 ] , argb[ 1 ] , argb[ 2 ] , argb[ 3 ] );
	}

	/**
	 * Multiply current GL transform with the specific 3D transformation matrix.
	 *
	 * @param   gl          GL context.
	 * @param   transform   Transformation to multiply with.
	 */
	public static void glMultMatrixd( final GL gl , final Matrix3D transform )
	{
		gl.glMultMatrixd( new double[]
			{
				transform.xx , transform.yx , transform.zx , 0.0 ,
				transform.xy , transform.yy , transform.zy , 0.0 ,
				transform.xz , transform.yz , transform.zz , 0.0 ,
				transform.xo , transform.yo , transform.zo , 1.0
			} , 0 );
	}

	/**
	 * Set GL material properties.
	 *
	 * @param   gl          GL context.
	 * @param   material    Material properties.
	 */
	public static void setMaterial( final GL gl , final Material material )
	{
		/* Set color, used when lights are disabled. */
		gl.glColor4f( material.diffuseColorRed  , material.diffuseColorGreen  , material.diffuseColorBlue  , material.diffuseColorAlpha );

		/* Set material properties. */
		gl.glMaterialfv( GL.GL_FRONT , GL.GL_AMBIENT  , new float[] { material.ambientColorRed  , material.ambientColorGreen  , material.ambientColorBlue  , 1.0f } , 0 );
		gl.glMaterialfv( GL.GL_FRONT , GL.GL_DIFFUSE  , new float[] { material.diffuseColorRed  , material.diffuseColorGreen  , material.diffuseColorBlue  , material.diffuseColorAlpha } , 0 );
		gl.glMaterialfv( GL.GL_FRONT , GL.GL_SPECULAR , new float[] { material.specularColorRed , material.specularColorGreen , material.specularColorBlue , 1.0f } , 0 );
		gl.glMaterialfv( GL.GL_FRONT , GL.GL_EMISSION , new float[] { material.emissiveColorRed , material.emissiveColorGreen , material.emissiveColorBlue , 1.0f } , 0 );
	}
}