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
package ab.j3d.view.jogl;

import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Creates appropriate {@link GeometryObject} instances based on available
 * OpenGL capabilities.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class GeometryObjectFactory
{
	/**
	 * Specifies a {@link GeometryObject} implementation.
	 */
	private enum Implementation
	{
		/** Immediate mode rendering.              */ IMMEDIATE_MODE,
		/** Vertex buffer object, OpenGL 1.5 core. */ VERTEX_BUFFER_OBJECT_CORE,
		/** Vertex buffer object, ARB extension.   */ VERTEX_BUFFER_OBJECT_ARB
	}

	/**
	 * Implementation to be used. <code>null</code> if not yet determined.
	 */
	@Nullable
	private Implementation _implementation;

	/**
	 * Constructs a new factory.
	 */
	public GeometryObjectFactory()
	{
		_implementation = null;
	}

	/**
	 * Constructs a new geometry object for the given face groups.
	 *
	 * @param   faceGroups  Face groups.
	 * @param   type        Type of geometry to create.
	 *
	 * @return  Geometry object.
	 */
	@NotNull
	public GeometryObject newGeometryObject( final List<FaceGroup> faceGroups, final GeometryType type )
	{
		GeometryObject result = null;

		final Implementation implementation = getImplementation();
		if ( implementation != null )
		{
			switch ( implementation )
			{
				case VERTEX_BUFFER_OBJECT_CORE:
					result = new VertexBufferObjectCore( faceGroups, type );
					break;

				case VERTEX_BUFFER_OBJECT_ARB:
					result = new VertexBufferObjectARB( faceGroups, type );
					break;

				default:
			}
		}

		if ( result == null )
		{
			result = new ImmediateModeGeometryObject( faceGroups, type );
		}

		return result;
	}

	/**
	 * Returns the implementation to be used.
	 *
	 * @return  Implementation to be used.
	 */
	@Nullable
	private Implementation getImplementation()
	{
		Implementation result = _implementation;
		if ( result == null )
		{
			final GL gl = GLU.getCurrentGL();
			if ( gl.isFunctionAvailable( "glBindBuffer" ) )
			{
				result = Implementation.VERTEX_BUFFER_OBJECT_CORE;
			}
			else if ( gl.isFunctionAvailable( "glBindBufferARB" ) )
			{
				result = Implementation.VERTEX_BUFFER_OBJECT_ARB;
			}
			else
			{
				result = Implementation.IMMEDIATE_MODE;
			}
			_implementation = result;
			System.err.println( "Using " + result + " implementation." );
		}
		return result;
	}
}
