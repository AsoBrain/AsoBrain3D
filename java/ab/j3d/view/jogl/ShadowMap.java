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

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;

/**
 * Encapsulates a shadow map that represents shadows cast by objects as seen
 * from a single light source.
 *
 * <h3>Sample usage</h3>
 * <p>
 * The following code snippet shows how a shadow map object would typically be
 * used to render shadows.
 * </p>
 * <pre>
 * ShadowMap shadowMap;
 *
 * init( GL gl )
 * {
 *   shadowMap = new ShadowMap( size );
 *   shadowMap.init( gl );
 * }
 *
 * display( GL gl )
 * {
 *   shadowMap.setLight( light, lightTransform );
 *
 *   shadowMap.begin( gl );
 *   // render scene
 *   shadowMap.end( gl );
 *
 *   // bind depth texture
 *   shadowMap.loadProjectionMatrix( gl );
 *   // render scene with projected shadow map
 * }
 * </pre>
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class ShadowMap
{
	/**
	 * Transformation from normalized screen coordinates (-1 to 1) to
	 * texture coordinates (0 to 1).
	 */
	private static final double[] SCREEN_TO_TEXTURE =
	{
		0.5, 0.0, 0.0, 0.0,
		0.0, 0.5, 0.0, 0.0,
		0.0, 0.0, 0.5, 0.0,
		0.5, 0.5, 0.5, 1.0
	};

	/**
	 * Length and width of the shadow map.
	 */
	private final int _size;

	/**
	 * Whether a color map should be rendered.
	 */
	private final boolean _renderColorMap;

	/**
	 * Framebuffer used to render the shadow map.
	 */
	private Framebuffer _framebuffer;

	/**
	 * Texture used as the framebuffer's color attachment.
	 */
	private int _colorTexture;

	/**
	 * Texture used as the framebuffer's depth attachment.
	 */
	private int _depthTexture;

	/**
	 * Light that casts the shadows to be mapped.
	 */
	private Light3D _light;

	/**
	 * Light-to-scene transformation.
	 */
	private Matrix3D _transform;

	/**
	 * Light projection matrix.
	 */
	private double[] _projectionMatrix;

	/**
	 * Transformation matrix to view the scene from the light source.
	 */
	private double[] _modelviewMatrix;

	/**
	 * Constructs a new shadow map of the given size.
	 *
	 * @param   shadowSize      Length and width of the shadow map.
	 */
	public ShadowMap( final int shadowSize )
	{
		this( shadowSize, false );
	}

	/**
	 * Constructs a new shadow map of the given size. If requested, a color map
	 * is rendered in addition to a depth map. This can be used to easily view
	 * what the light 'sees'.
	 *
	 * @param   shadowSize      Length and width of the shadow map.
	 * @param   renderColorMap  <code>true</code> to render a color map;
	 *                          otherwise only depth is rendered.
	 */
	public ShadowMap( final int shadowSize, final boolean renderColorMap )
	{
		_size = shadowSize;
		_renderColorMap = renderColorMap;

		_framebuffer = null;
		_colorTexture = 0;
		_depthTexture = 0;

		_light = null;
		_transform = Matrix3D.IDENTITY;

		_projectionMatrix = null;
		_modelviewMatrix = null;
	}

	/**
	 * Sets the light to render the shadow map for.
	 *
	 * @param   light       Light.
	 * @param   transform   Light to scene transform.
	 */
	public void setLight( final Light3D light, final Matrix3D transform )
	{
		_light = light;
		_transform = transform;
	}

	/**
	 * Initializes the shadow map.
	 *
	 * @param   gl  OpenGL pipeline.
	 */
	public void init( final GL gl )
	{
		final int textureCount = _renderColorMap ? 2 : 1;

		final int[] textures = new int[ textureCount ];
		gl.glGenTextures( textures.length, textures, 0 );

		final Framebuffer framebuffer = new Framebuffer();
		framebuffer.bind();

		final int size = _size;
		{
			final int depthTexture = textures[ 0 ];
			gl.glBindTexture( GL.GL_TEXTURE_2D, depthTexture );
			gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT24, size, size, 0, GL.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_INT, null );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_COMPARE_MODE, GL.GL_COMPARE_R_TO_TEXTURE );
			_depthTexture = depthTexture;
		}
		gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_TEXTURE_2D, _depthTexture, 0 );

		if ( _renderColorMap )
		{
			final int colorTexture = textures[ 1 ];
			gl.glBindTexture( GL.GL_TEXTURE_2D, colorTexture );
			gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, size, size, 0, GL.GL_RGBA, GL.GL_UNSIGNED_INT, null );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE );
			gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE );
			_colorTexture = colorTexture;

			gl.glFramebufferTexture2DEXT( GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, _colorTexture, 0 );
		}
		else
		{
			framebuffer.disableColorAttachments();
		}

		framebuffer.check();
		Framebuffer.unbind();
		_framebuffer = framebuffer;
	}

	/**
	 * Prepares for rendering of the shadow map.
	 *
	 * @param   gl      OpenGL pipeline.
	 * @param   scene   Scene being rendered.
	 */
	public void begin( final GL gl, final Scene scene )
	{
		final GLU glu = new GLU();

		// Store projection matrix for final rendering pass.
		gl.glPushAttrib( GL.GL_VIEWPORT_BIT );
		gl.glMatrixMode( GL.GL_PROJECTION );
		gl.glPushMatrix();

		// Adjust viewport size and projection matrix for shadow map.
		gl.glViewport( 0, 0, _size, _size );
		gl.glLoadIdentity();

		// View from light.
		Matrix3D lightViewTransform = Matrix3D.IDENTITY;
		final Bounds3D sceneBounds = scene.getBounds();
		if ( sceneBounds != null )
		{
			final Vector3D lightTarget = sceneBounds.center();

			final Light3D light = _light;
			final Matrix3D lightNodeTransform = _transform;

			if ( light instanceof DirectionalLight3D )
			{
				final DirectionalLight3D directional = (DirectionalLight3D)light;
				lightViewTransform = Matrix3D.getFromToTransform( Vector3D.ZERO, lightNodeTransform.rotate( directional.getDirection() ), Vector3D.POSITIVE_Z_AXIS, Vector3D.POSITIVE_Y_AXIS );
			}
			else if ( light instanceof SpotLight3D )
			{
				final SpotLight3D spot = (SpotLight3D)light;
				lightViewTransform = Matrix3D.getFromToTransform( lightNodeTransform.getTranslation(), lightNodeTransform.transform( spot.getDirection() ), Vector3D.POSITIVE_Z_AXIS, Vector3D.POSITIVE_Y_AXIS );
			}
			else
			{
				lightViewTransform = Matrix3D.getFromToTransform( lightNodeTransform.getTranslation(), lightTarget, Vector3D.POSITIVE_Z_AXIS, Vector3D.POSITIVE_Y_AXIS );
			}

			final Bounds3D transformedBounds = GeometryTools.convertObbToAabb( lightViewTransform, sceneBounds );
			final Vector3D min = transformedBounds.min();
			final Vector3D max = transformedBounds.max();

			final double zFar = -min.z;

			if ( light instanceof DirectionalLight3D )
			{
				final double zNear = -max.z;
				gl.glOrtho( min.x, max.x, min.y, max.y, zNear, zFar );
			}
			else
			{
				final double zNear = Math.max( zFar / 10000.0, -max.z );

				if ( light instanceof SpotLight3D )
				{
					final SpotLight3D spot = (SpotLight3D)light;
					glu.gluPerspective( 2.0 * (double)spot.getSpreadAngle(), 1.0, zNear, zFar );
				}
				else // point light
				{
					// TODO: Calculating optimal field of view would improve shadows and avoid clipping. However, a true point-light (FoV=180) cannot be modeled in this way.
					glu.gluPerspective( 30.0, 1.0, zNear, zFar );
				}
			}
		}

		gl.glMatrixMode( GL.GL_MODELVIEW );
		gl.glLoadIdentity();
		JOGLTools.glMultMatrixd( gl, lightViewTransform );

		final double[] projectionMatrix = new double[ 16 ];
		gl.glGetDoublev( GL.GL_PROJECTION_MATRIX, projectionMatrix, 0 );
		_projectionMatrix = projectionMatrix;

		final double[] modelviewMatrix = new double[ 16 ];
		gl.glGetDoublev( GL.GL_MODELVIEW_MATRIX, modelviewMatrix, 0 );
		_modelviewMatrix = modelviewMatrix;

		_framebuffer.bind();

		/* Clear buffers. */
		gl.glClearDepth( 1.0 );
		if ( _renderColorMap )
		{
			gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
			gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		}
		else
		{
			gl.glClear( GL.GL_DEPTH_BUFFER_BIT );
		}
	}

	/**
	 * Finishes rendering of the shadow map.
	 *
	 * @param   gl      OpenGL pipeline.
	 */
	public void end( final GL gl )
	{
		Framebuffer.unbind();

		// Restore viewport size and camera projection.
		gl.glMatrixMode( GL.GL_PROJECTION );
		gl.glPopMatrix();
		gl.glPopAttrib();
	}

	/**
	 * Loads the projection matrix for projecting the shadow map on the scene.
	 *
	 * @param   gl      OpenGL pipeline.
	 */
	public void loadProjectionMatrix( final GL gl )
	{
		gl.glLoadMatrixd( SCREEN_TO_TEXTURE, 0 );
		gl.glMultMatrixd( _projectionMatrix, 0 );
		gl.glMultMatrixd( _modelviewMatrix, 0 );
	}

	/**
	 * Returns the depth texture.
	 *
	 * @return  OpenGL texture.
	 */
	public int getDepthTexture()
	{
		return _depthTexture;
	}

	/**
	 * Returns the color texture, if any.
	 *
	 * @return  OpenGL texture.
	 */
	public int getColorTexture()
	{
		return _colorTexture;
	}
}
