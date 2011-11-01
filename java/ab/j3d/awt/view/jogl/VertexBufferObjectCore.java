/*
 * $Id$
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
package ab.j3d.awt.view.jogl;

import java.nio.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Stores geometry using vertex buffer objects.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class VertexBufferObjectCore
	extends VertexBufferObject
{
	/**
	 * Identifies the vertex buffer object.
	 */
	private int _vertexBufferObject;

	/**
	 * Constructs a new vertex buffer object for the geometry of the given
	 * face groups.
	 *
	 * @param   faceGroups  Face groups.
	 * @param   type        Type of geometry to be created.
	 */
	public VertexBufferObjectCore( @NotNull final List<FaceGroup> faceGroups, @NotNull final GeometryType type )
	{
		final GL gl = GLU.getCurrentGL();

		/*
		 * Allocate a vertex buffer object.
		 */
		final int[] ids = new int[ 1 ];
		gl.glGenBuffers( ids.length, ids, 0 );
		final int vertexBufferObject = ids[ 0 ];
		_vertexBufferObject = vertexBufferObject;

		/*
		 * Create the buffer data.
		 */
		final ByteBuffer vertexBuffer = createBufferData( faceGroups, type );

		/*
		 * Copy the buffered data into the vertex buffer object.
		 */
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vertexBufferObject );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertexBuffer.remaining(), vertexBuffer, GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
	}

	@Override
	public void draw()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, _vertexBufferObject );
		performDrawOperations( gl );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
	}

	@Override
	public void delete()
	{
		final GL gl = GLU.getCurrentGL();
		gl.glDeleteBuffers( 1, new int[] { _vertexBufferObject }, 0 );
	}
}
