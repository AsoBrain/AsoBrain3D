/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2007-2009
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
import java.util.HashMap;
import java.util.Map;
import javax.media.opengl.GLContext;

import com.sun.opengl.util.texture.Texture;

import ab.j3d.model.Scene;
import ab.j3d.view.RenderEngine;
import ab.j3d.view.View3D;

/**
 * JOGL render engine implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLEngine
	implements RenderEngine
{
	/**
	 * Background color for views.
	 */
	private final Color _background;

	/**
	 * Shared OpenGL rendering context.
	 */
	private GLContext _context = null;

	/**
	 * Texture cache.
	 */
	private final Map<String,Texture> _textureCache;

	/**
	 * Construct new JOGL render engine.
	 */
	public JOGLEngine()
	{
		this( null );
	}

	/**
	 * Construct new JOGL render engine.
	 *
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 */
	public JOGLEngine( final Color background )
	{
		_background = background;
		_textureCache = new HashMap<String,Texture>();
	}

	public View3D createView( final Scene scene )
	{
		return new JOGLView( this , scene , _background );
	}

	/**
	 * Return the GLContext for this model if set.
	 *
	 * @return The GLContext for this model
	 */
	GLContext getContext()
	{
		return _context;
	}

	/**
	 * Set the GL Context for this model.
	 *
	 * @param context GLContext to be set.
	 */
	void setContext( final GLContext context )
	{
		_context = context;
	}

	/**
	 * Returns the model's texture cache.
	 *
	 * @return  Texture cache.
	 */
	Map<String,Texture> getTextureCache()
	{
		return _textureCache;
	}
}
