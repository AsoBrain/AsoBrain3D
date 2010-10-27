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
package ab.j3d.view.jogl;

import java.util.*;
import javax.media.opengl.*;

import ab.j3d.*;

/**
 * Handles OpenGL state changes and verifies that changes were properly applied.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class DebugGLStateHelper
	extends CachingGLStateHelper
{
	/**
	 * Constructs a new instance.
	 *
	 * @param   gl  OpenGL interface.
	 */
	public DebugGLStateHelper( final GL gl )
	{
		super( gl );
	}

	@Override
	public void setColor( final float red, final float green, final float blue, final float alpha )
	{
		super.setColor( red, green, blue, alpha );

		final GL gl = _gl;
		final float[] actual = new float[4];
		gl.glGetFloatv( GL.GL_CURRENT_COLOR, actual, 0 );
		assertEquals( new float[] { red, green, blue, alpha }, actual );
	}

	@Override
	protected void setMaterial( final Material material, final float red, final float green, final float blue, final float alpha )
	{
		final GL gl = _gl;

		super.setMaterial( material, red, green, blue, alpha);

		{
			final float[] expected = { red, green, blue, alpha };
			final float[] actual = new float[ 4 ];
			gl.glGetFloatv( GL.GL_CURRENT_COLOR, actual, 0 );
			assertEquals( expected, actual );
		}

		{
			final float[] expected = { material.ambientColorRed, material.ambientColorGreen, material.ambientColorBlue, alpha };
			final float[] actual = new float[ 4 ];
			gl.glGetMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT, actual, 0 );
			assertEquals( expected, actual );
			gl.glGetMaterialfv( GL.GL_BACK, GL.GL_AMBIENT, actual, 0 );
			assertEquals( expected, actual );
		}

		{
			final float[] expected = { red, green, blue, alpha };
			final float[] actual = new float[ 4 ];
			gl.glGetMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, actual, 0 );
			assertEquals( expected, actual );
			gl.glGetMaterialfv( GL.GL_BACK, GL.GL_DIFFUSE, actual, 0 );
			assertEquals( expected, actual );
		}

		{
			final float[] expected = { material.specularColorRed, material.specularColorGreen, material.specularColorBlue, alpha };
			final float[] actual = new float[ 4 ];
			gl.glGetMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, actual, 0 );
			assertEquals( expected, actual );
			gl.glGetMaterialfv( GL.GL_BACK, GL.GL_SPECULAR, actual, 0 );
			assertEquals( expected, actual );
		}

		{
			final float[] expected = { material.emissiveColorRed, material.emissiveColorGreen, material.emissiveColorBlue, alpha };
			final float[] actual = new float[ 4 ];
			gl.glGetMaterialfv( GL.GL_FRONT, GL.GL_EMISSION, actual, 0 );
			assertEquals( expected, actual );
			gl.glGetMaterialfv( GL.GL_BACK, GL.GL_EMISSION, actual, 0 );
			assertEquals( expected, actual );
		}

		{
			final float[] expected = { (float)material.shininess };
			final float[] actual = new float[ 1 ];
			gl.glGetMaterialfv( GL.GL_FRONT, GL.GL_SHININESS, actual, 0 );
			assertEquals( expected, actual );
			gl.glGetMaterialfv( GL.GL_BACK, GL.GL_SHININESS, actual, 0 );
			assertEquals( expected, actual );
		}
	}

	@Override
	public void setBlendFunc( final int source, final int destination )
	{
		super.setBlendFunc( source, destination );

		final GL gl = _gl;
		final int[] actual = new int[ 4 ];
		gl.glGetIntegerv( GL.GL_BLEND_SRC_RGB  , actual, 0 );
		gl.glGetIntegerv( GL.GL_BLEND_SRC_ALPHA, actual, 1 );
		gl.glGetIntegerv( GL.GL_BLEND_DST_RGB  , actual, 2 );
		gl.glGetIntegerv( GL.GL_BLEND_DST_ALPHA, actual, 3 );
		assertEquals( new int[] { source, source, destination, destination }, actual );
	}

	private static void assertEquals( final float[] expected, final float[] actual )
	{
		for ( int i = 0; i < Math.min( expected.length, actual.length ); i++ )
		{
			if ( expected[ i ] != actual[ i ] )
			{
				throw new AssertionError( "Unexpected values: expected " + Arrays.toString( expected ) + ", but was " + Arrays.toString( actual ) );
			}
		}

		if ( expected.length != actual.length )
		{
			throw new AssertionError( "Additional/missing values: expected " + Arrays.toString( expected ) + ", but was " + Arrays.toString( actual ) );
		}
	}

	private static void assertEquals( final int[] expected, final int[] actual )
	{
		for ( int i = 0; i < Math.min( expected.length, actual.length ); i++ )
		{
			if ( expected[ i ] != actual[ i ] )
			{
				throw new AssertionError( "Unexpected values: expected " + Arrays.toString( expected ) + ", but was " + Arrays.toString( actual ) );
			}
		}

		if ( expected.length != actual.length )
		{
			throw new AssertionError( "Additional/missing values: expected " + Arrays.toString( expected ) + ", but was " + Arrays.toString( actual ) );
		}
	}
}
