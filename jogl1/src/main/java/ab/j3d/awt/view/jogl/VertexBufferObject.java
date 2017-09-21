/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.awt.view.jogl;

import java.nio.*;
import java.util.*;
import javax.media.opengl.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Stores geometry using vertex buffer objects.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public abstract class VertexBufferObject
	implements GeometryObject
{
	/**
	 * List of draw operations needed to draw the geometry stored in the buffer.
	 */
	protected List<DrawOperation> _drawOperations = null;

	/**
	 * Specifies which attributes are specified for each vertex.
	 */
	protected VertexFormat _vertexFormat = null;

	/**
	 * Constructs a new vertex buffer object.
	 */
	protected VertexBufferObject()
	{
	}

	/**
	 * Creates vertex buffer data for the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 * @param   type        Type of geometry to be created.
	 *
	 * @return  Vertex buffer data.
	 */
	protected ByteBuffer createBufferData( @NotNull final List<FaceGroup> faceGroups, @NotNull final GeometryType type )
	{
		final ByteBuffer vertexBuffer;
		switch ( type )
		{
			default:
			case FACES:
				vertexBuffer = createBufferDataForFaces( faceGroups );
				break;
			case OUTLINES:
				vertexBuffer = createBufferDataForOutlines( faceGroups );
				break;
			case VERTICES:
				vertexBuffer = createBufferDataForVertices( faceGroups );
				break;
		}
		return vertexBuffer;
	}

	/**
	 * Creates vertex buffer data for the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 *
	 * @return  Vertex buffer data.
	 */
	protected ByteBuffer createBufferDataForFaces( final List<FaceGroup> faceGroups )
	{
		/*
		 * Count primitives and vertices. Count triangles and quads, so they can
		 * be batched together and drawn with a single call.
		 *
		 * vertexCount    = number of vertices needed for -all- primitives
		 * triangleCount  = number of independent triangles (drawn as 1 primitive)
		 * quadCount      = number of independent triangles (drawn as 1 primitive)
		 * primitiveCount = number of primitives (except independent triangles/quads)
		 */
		int vertexCount = 0;
		int triangleCount = 0;
		int quadCount = 0;
		int primitiveCount = 0;

		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final Tessellation tessellation = face.getTessellation();

				final Collection<TessellationPrimitive> primitives = tessellation.getPrimitives();
				for ( final TessellationPrimitive primitive : primitives )
				{
					final int[] vertices = primitive.getVertices();
					vertexCount += vertices.length;

					if ( primitive instanceof TriangleList )
					{
						triangleCount += vertices.length / 3;
					}
					else if ( primitive instanceof QuadList )
					{
						quadCount += vertices.length / 4;
					}
					else if ( primitive instanceof TriangleFan )
					{
						if ( vertices.length == 3 )
						{
							triangleCount++;
						}
						else if ( vertices.length == 4 )
						{
							quadCount++;
						}
						else
						{
							primitiveCount++;
						}
					}
					else if ( primitive instanceof TriangleStrip )
					{
						primitiveCount++;
					}
					else if ( primitive instanceof QuadStrip )
					{
						primitiveCount++;
					}
					else
					{
						throw new IllegalArgumentException( "Unsupported primitive: " + primitive );
					}
				}
			}
		}

		if ( triangleCount > 0 )
		{
			primitiveCount++;
		}

		if ( quadCount > 0 )
		{
			primitiveCount++;
		}

		/*
		 * Allocate a byte buffer for the vertex data.
		 */
		final DefaultVertexFormat vertexFormat = new DefaultVertexFormat();
		_vertexFormat = vertexFormat;

		final int bytesPerVertex = vertexFormat.getBytesPerVertex();

		final ByteBuffer vertexBuffer = ByteBuffer.allocate( vertexCount * bytesPerVertex );
		vertexBuffer.order( ByteOrder.LITTLE_ENDIAN );

		/*
		 * All triangle faces are stored together, to render with one call.
		 */
		final ByteBuffer triangleBuffer = vertexBuffer.duplicate();
		final int triangleOffset = ( vertexCount - 3 * triangleCount - 4 * quadCount ) * bytesPerVertex;
		triangleBuffer.position( triangleOffset );
		triangleBuffer.order( ByteOrder.LITTLE_ENDIAN );

		/*
		 * All quad faces are stored together, to render with one call.
		 */
		final ByteBuffer quadBuffer = vertexBuffer.duplicate();
		final int quadOffset = ( vertexCount - 4 * quadCount ) * bytesPerVertex;
		quadBuffer.position( quadOffset );
		quadBuffer.order( ByteOrder.LITTLE_ENDIAN );

		/*
		 * Fill the buffer and keep track of associated draw operations.
		 */
		final ArrayList<DrawOperation> drawOperations = new ArrayList<DrawOperation>( primitiveCount );
		_drawOperations = drawOperations;

		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final Tessellation tessellation = face.getTessellation();
				for ( final TessellationPrimitive primitive : tessellation.getPrimitives() )
				{
					final int[] vertices = primitive.getVertices();

					final ByteBuffer buffer;
					int mode = -1;

					if ( primitive instanceof TriangleList )
					{
						buffer = triangleBuffer;
					}
					else if ( primitive instanceof QuadList )
					{
						buffer = quadBuffer;
					}
					else if ( primitive instanceof TriangleFan )
					{
						if ( vertices.length == 3 )
						{
							buffer = triangleBuffer;
						}
						else if ( vertices.length == 4 )
						{
							buffer = quadBuffer;
						}
						else
						{
							buffer = vertexBuffer;
							mode = GL.GL_TRIANGLE_FAN;
						}
					}
					else if ( primitive instanceof TriangleStrip )
					{
						buffer = vertexBuffer;
						mode = GL.GL_TRIANGLE_STRIP;
					}
					else if ( primitive instanceof QuadStrip )
					{
						buffer = vertexBuffer;
						mode = GL.GL_QUAD_STRIP;
					}
					else
					{
						throw new IllegalArgumentException( "Unsupported primitive: " + primitive );
					}

					final int offset = buffer.position();

					for ( final int vertexIndex : vertices )
					{
						vertexFormat.encode( buffer, vertexIndex, face, faceGroup );
					}

					if ( mode != -1 )
					{
						drawOperations.add( new DrawArrays( mode, offset / bytesPerVertex, vertices.length ) );
					}
				}
			}
		}

		if ( triangleCount > 0 )
		{
			drawOperations.add( new DrawArrays( GL.GL_TRIANGLES, triangleOffset / bytesPerVertex, triangleCount * 3 ) );
		}

		if ( quadCount > 0 )
		{
			drawOperations.add( new DrawArrays( GL.GL_QUADS, quadOffset / bytesPerVertex, quadCount * 4 ) );
		}

		vertexBuffer.rewind();
		return vertexBuffer;
	}

	/**
	 * Creates vertex buffer data for the outlines of the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 *
	 * @return  Vertex buffer data.
	 */
	protected ByteBuffer createBufferDataForOutlines( final List<FaceGroup> faceGroups )
	{
		/*
		 * Count primitives and vertices.
		 */
		int vertexCount = 0;
		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<int[]> outlines = face.getOutlines();
				for ( final int[] outline : outlines )
				{
					vertexCount += ( outline.length - 1 ) * 2;
				}
			}
		}

		/*
		 * Allocate a byte buffer for the vertex data.
		 */
		final SimpleVertexFormat vertexFormat = new SimpleVertexFormat();
		_vertexFormat = vertexFormat;

		final int bytesPerVertex = vertexFormat.getBytesPerVertex();

		final ByteBuffer vertexBuffer = ByteBuffer.allocate( vertexCount * bytesPerVertex );
		vertexBuffer.order( ByteOrder.LITTLE_ENDIAN );

		/*
		 * Fill the buffer and keep track of associated draw operations.
		 */
		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( final int[] outline : face.getOutlines() )
				{
					for ( int i = 0; i < outline.length - 1; i++ )
					{
						vertexFormat.encode( vertexBuffer, outline[ i ], face, faceGroup );
						vertexFormat.encode( vertexBuffer, outline[ i + 1 ], face, faceGroup );
					}
				}
			}
		}

		_drawOperations = Collections.<DrawOperation>singletonList( new DrawArrays( GL.GL_LINES, 0, vertexCount ) );

		vertexBuffer.rewind();
		return vertexBuffer;
	}

	/**
	 * Creates vertex buffer data for the vertices of the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 *
	 * @return  Vertex buffer data.
	 */
	protected ByteBuffer createBufferDataForVertices( final List<FaceGroup> faceGroups )
	{
		/*
		 * Count primitives and vertices.
		 */
		int vertexCount = 0;
		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				final List<int[]> outlines = face.getOutlines();
				for ( final int[] outline : outlines )
				{
					vertexCount += ( outline.length - 1 ) * 2;
				}
			}
		}

		/*
		 * Allocate a byte buffer for the vertex data.
		 */
		final SimpleVertexFormat vertexFormat = new SimpleVertexFormat();
		_vertexFormat = vertexFormat;

		final int bytesPerVertex = vertexFormat.getBytesPerVertex();

		final ByteBuffer vertexBuffer = ByteBuffer.allocate( vertexCount * bytesPerVertex );
		vertexBuffer.order( ByteOrder.LITTLE_ENDIAN );

		/*
		 * Fill the buffer and keep track of associated draw operations.
		 */
		for ( final FaceGroup faceGroup : faceGroups )
		{
			for ( final Face3D face : faceGroup.getFaces() )
			{
				for ( final int[] outline : face.getOutlines() )
				{
					for ( int i = 0; i < outline.length - 1; i++ )
					{
						vertexFormat.encode( vertexBuffer, outline[ i ], face, faceGroup );
						vertexFormat.encode( vertexBuffer, outline[ i + 1 ], face, faceGroup );
					}
				}
			}
		}

		_drawOperations = Collections.<DrawOperation>singletonList( new DrawArrays( GL.GL_LINES, 0, vertexCount ) );

		vertexBuffer.rewind();
		return vertexBuffer;
	}

	/**
	 * Performs the draw operations needed to draw the geometry stored in the
	 * buffer.
	 *
	 * @param   gl  OpenGL interface.
	 */
	protected void performDrawOperations( final GL gl )
	{
		_vertexFormat.enable( gl );
		for ( final DrawOperation drawOperation : _drawOperations )
		{
			drawOperation.draw( gl );
		}
		_vertexFormat.disable( gl );
	}

	/**
	 * A draw operation performed using the vertex buffer.
	 */
	private interface DrawOperation
	{
		/**
		 * Performs the draw operation on the given GL.
		 *
		 * @param   gl  GL object.
		 */
		void draw( GL gl );
	}

	/**
	 * Represents a {@link GL#glDrawArrays} call.
	 */
	private static class DrawArrays
		implements DrawOperation
	{
		/**
		 * Type of primitives to be drawn.
		 */
		private int _mode;

		/**
		 * Index of the first vertex.
		 */
		private int _start;

		/**
		 * Number of vertices.
		 */
		private int _count;

		/**
		 * Constructs a new {@link GL#glDrawArrays} call.
		 *
		 * @param   mode    Type of primitives to be drawn.
		 * @param   start   Index of the first vertex.
		 * @param   count   Number of vertices.
		 */
		private DrawArrays( final int mode, final int start, final int count )
		{
			_mode = mode;
			_count = count;
			_start = start;
		}

		public void draw( final GL gl )
		{
			gl.glDrawArrays( _mode, _start, _count );
		}

		@Override
		public String toString()
		{
			final Class<?> clazz = getClass();
			return clazz.getName() + "[mode=" + _mode + ", start=" + _start + ", count=" + _count + ']';
		}
	}

	/**
	 * Represents a {@link GL#glDrawElements} call.
	 */
	private static class DrawElements
		implements DrawOperation
	{
		/**
		 * Type of primitives to be drawn.
		 */
		private int _mode;

		/**
		 * Number of vertices.
		 */
		private int _count;

		/**
		 * Data type of vertex indices.
		 */
		private int _type;

		/**
		 * Index of the first vertex index.
		 */
		private long _indices;

		/**
		 * Constructs a new {@link GL#glDrawArrays} call.
		 *
		 * @param   mode        Type of primitives to be drawn.
		 * @param   count       Number of vertices.
		 * @param   type        Data type of vertex indices.
		 * @param   indices     Index of the first vertex index.
		 */
		private DrawElements( final int mode, final int count, final int type, final long indices )
		{
			_mode = mode;
			_count = count;
			_type = type;
			_indices = indices;
		}

		public void draw( final GL gl )
		{
			gl.glDrawElements( _mode, _count, _type, _indices );
		}
	}

	/**
	 * Specifies which attributes are specified for each vertex and which data
	 * types are used.
	 */
	private interface VertexFormat
	{
		/**
		 * Enables drawing of geometry with this vertex format.
		 *
		 * @param   gl  OpenGL interface.
		 */
		void enable( @NotNull GL gl );

		/**
		 * Disables drawing of geometry with this vertex format.
		 *
		 * @param   gl  OpenGL interface.
		 */
		void disable( @NotNull GL gl );

		/**
		 * Returns the number of bytes needed to store one vertex using this
		 * format.
		 *
		 * @return  Number of bytes per vertex.
		 */
		int getBytesPerVertex();

		/**
		 * Adds the given vertex at the current position in the given byte
		 * buffer, encoded using this vertex format.
		 *
		 * @param   target          Byte buffer to add the vertex to.
		 * @param   vertexIndex     Index of the vertex.
		 * @param   face            Face that contains the vertex.
		 * @param   faceGroup       Face group containing the face.
		 */
		void encode( @NotNull ByteBuffer target, int vertexIndex, @NotNull Face3D face, @NotNull FaceGroup faceGroup );
	}

	/**
	 * A typical vertex format that specifies the vertex coordinate, vertex
	 * normal and a single texture coordinate, using 32-bit floating points.
	 */
	private static class DefaultVertexFormat
		implements VertexFormat
	{
		public int getBytesPerVertex()
		{
			return 12 + 12 + 8; // vertex + normal + texcoord
		}

		public void enable( @NotNull final GL gl )
		{
			gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
			gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
			gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );

			final int bytesPerVertex = getBytesPerVertex();
			gl.glVertexPointer( 3, GL.GL_FLOAT, bytesPerVertex, 0L );
			gl.glNormalPointer( GL.GL_FLOAT, bytesPerVertex, 12L );
			gl.glTexCoordPointer( 2, GL.GL_FLOAT, bytesPerVertex, 24L );
		}

		public void disable( @NotNull final GL gl )
		{
			gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
			gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
			gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
		}

		public void encode( @NotNull final ByteBuffer target, final int vertexIndex, @NotNull final Face3D face, @NotNull final FaceGroup faceGroup )
		{
			final Vertex3D vertex = face.getVertex( vertexIndex );

			target.putFloat( (float)vertex.point.x );
			target.putFloat( (float)vertex.point.y );
			target.putFloat( (float)vertex.point.z );

			final Vector3D normal;
			if ( faceGroup.isSmooth() )
			{
				normal = face.getVertexNormal( vertexIndex );
			}
			else
			{
				normal = face.getNormal();
			}
			target.putFloat( (float)normal.x );
			target.putFloat( (float)normal.y );
			target.putFloat( (float)normal.z );

			target.putFloat( vertex.colorMapU );
			target.putFloat( vertex.colorMapV );
		}
	}

	/**
	 * A vertex format that only specifies the vertex coordinate.
	 */
	private static class SimpleVertexFormat
		implements VertexFormat
	{
		public int getBytesPerVertex()
		{
			return 12;
		}

		public void enable( @NotNull final GL gl )
		{
			gl.glEnableClientState( GL.GL_VERTEX_ARRAY );

			final int bytesPerVertex = getBytesPerVertex();
			gl.glVertexPointer( 3, GL.GL_FLOAT, bytesPerVertex, 0L );
		}

		public void disable( @NotNull final GL gl )
		{
			gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
		}

		public void encode( @NotNull final ByteBuffer target, final int vertexIndex, @NotNull final Face3D face, @NotNull final FaceGroup faceGroup )
		{
			final Vertex3D vertex = face.getVertex( vertexIndex );
			target.putFloat( (float)vertex.point.x );
			target.putFloat( (float)vertex.point.y );
			target.putFloat( (float)vertex.point.z );
		}
	}
}
