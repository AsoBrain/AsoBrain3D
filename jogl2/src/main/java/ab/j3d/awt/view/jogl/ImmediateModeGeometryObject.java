/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2019 Peter S. Heijnen
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
 */
package ab.j3d.awt.view.jogl;

import java.util.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import org.jetbrains.annotations.*;

/**
 * A geometry object that uses OpenGL immediate mode rendering. This
 * implementation is provided for backwards compatibility. Immediate mode
 * rendering was deprecated in OpenGL 3 and removed in OpenGL 4.
 *
 * @author  G. Meinders
 */
public class ImmediateModeGeometryObject
	implements GeometryObject
{
	/**
	 * Face groups to be drawn.
	 */
	@NotNull
	private final List<FaceGroup> _faceGroups;

	/**
	 * Type of geometry to be drawn.
	 */
	@NotNull
	private final GeometryType _type;

	/**
	 * Constructs a geometry object for the geometry of the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 * @param   type        Type of geometry to be created.
	 */
	public ImmediateModeGeometryObject( @NotNull final List<FaceGroup> faceGroups, @NotNull final GeometryType type )
	{
		_faceGroups = faceGroups;
		_type = type;
	}

	public void draw()
	{
		switch ( _type )
		{
			case FACES:
				drawFaces();
				break;
			case OUTLINES:
				drawOutlines();
				break;
			case VERTICES:
				drawVertices();
				break;
		}
	}

	/**
	 * Draws the face groups as faces.
	 */
	private void drawFaces()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		for ( final FaceGroup faceGroup : _faceGroups )
		{
			final boolean setVertexNormals = faceGroup.isSmooth();

			final Appearance appearance = faceGroup.getAppearance();
			final TextureMap colorMap = ( appearance == null ) ? null : appearance.getColorMap();

			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<Vertex3D> vertices = face.getVertices();
				final Tessellation tessellation = face.getTessellation();
				final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();

				if ( !primitives.isEmpty() )
				{
					if ( !setVertexNormals )
					{
						final Vector3D normal = face.getNormal();
						gl2.glNormal3d( normal.x, normal.y, normal.z );
					}

					for ( final TessellationPrimitive primitive : primitives )
					{
						if ( primitive instanceof TriangleList )
						{
							gl2.glBegin( GL.GL_TRIANGLES );
						}
						else if ( primitive instanceof TriangleFan )
						{
							gl2.glBegin( GL.GL_TRIANGLE_FAN );
						}
						else if ( primitive instanceof TriangleStrip )
						{
							gl2.glBegin( GL.GL_TRIANGLE_STRIP );
						}
						else if ( primitive instanceof QuadStrip )
						{
							gl2.glBegin( GL2.GL_QUAD_STRIP );
						}
						else if ( primitive instanceof QuadList )
						{
							gl2.glBegin( GL2.GL_QUADS );
						}
						else
						{
							continue;
						}

						for ( final int vertexIndex : primitive.getVertices() )
						{
							final Vertex3D vertex = vertices.get( vertexIndex );

							if ( colorMap != null )
							{
								gl2.glTexCoord2f( vertex.colorMapU, vertex.colorMapV );
							}

							if ( setVertexNormals )
							{
								final Vector3D normal = face.getVertexNormal( vertexIndex );
								gl2.glNormal3d( normal.x, normal.y, normal.z );
							}

							final Vector3D point = vertex.point;
							gl2.glVertex3d( point.x, point.y, point.z );
						}

						gl2.glEnd();
					}
				}
			}
		}
	}

	/**
	 * Draws the face groups as outlines.
	 */
	private void drawOutlines()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		for ( final FaceGroup faceGroup : _faceGroups )
		{
			final boolean setVertexNormals = faceGroup.isSmooth();

			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<Vertex3D> vertices = face.getVertices();
				final int vertexCount = vertices.size();

				if ( vertexCount >= 2 )
				{
					if ( !setVertexNormals )
					{
						final Vector3D normal = face.getNormal();
						gl2.glNormal3d( normal.x, normal.y, normal.z );
					}

					final Tessellation tessellation = face.getTessellation();
					for ( final int[] outline : tessellation.getOutlines() )
					{
						gl2.glBegin( GL.GL_LINE_STRIP );

						for ( final int vertexIndex : outline )
						{
							final Vertex3D vertex = vertices.get( vertexIndex );
							final Vector3D point = vertex.point;

							if ( setVertexNormals )
							{
								final Vector3D normal = face.getVertexNormal( vertexIndex );
								gl2.glNormal3d( normal.x, normal.y, normal.z );
							}

							gl2.glVertex3d( point.x, point.y, point.z );
						}

						gl2.glEnd();
					}
				}
			}
		}
	}

	/**
	 * Draws the face groups as vertices.
	 */
	private void drawVertices()
	{
		final GL gl = GLU.getCurrentGL();
		final GL2 gl2 = gl.getGL2();

		for ( final FaceGroup faceGroup : _faceGroups )
		{
			final boolean setVertexNormals = faceGroup.isSmooth();

			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<Vertex3D> vertices = face.getVertices();
				final int vertexCount = vertices.size();

				if ( vertexCount >= 1 )
				{
					if ( !setVertexNormals )
					{
						final Vector3D normal = face.getNormal();
						gl2.glNormal3d( normal.x, normal.y, normal.z );
					}

					gl2.glBegin( GL.GL_POINTS );

					for ( int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++ )
					{
						final Vertex3D vertex = vertices.get( vertexIndex );
						final Vector3D point = vertex.point;

						if ( setVertexNormals )
						{
							final Vector3D normal = face.getVertexNormal( vertexIndex );
							gl2.glNormal3d( normal.x, normal.y, normal.z );
						}

						gl2.glVertex3d( point.x, point.y, point.z );
					}

					gl2.glEnd();
				}
			}
		}
	}

	public void delete()
	{
		// Not applicable.
	}
}
