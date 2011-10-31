/* $Id$
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
package ab.j3d.view.java2d;

import java.awt.*;

import ab.j3d.model.*;
import ab.j3d.view.*;

/**
 * Java 2D render engine implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Java2dEngine
	implements RenderEngine
{
	/**
	 * Background color for the model.
	 */
	private final Color _background;

	/**
	 * Construct new Java 2D render engine.
	 */
	public Java2dEngine()
	{
		this( null );
	}

	/**
	 * Construct new Java 2D render engine.
	 *
	 * @param   background  Background color to use for 3D views. May be
	 *                      <code>null</code>, in which case the default
	 *                      background color of the current look and feel is
	 *                      used.
	 */
	public Java2dEngine( final Color background )
	{
		_background   = background;
	}

	public View3D createView( final Scene scene )
	{
		return new Java2dView( scene, _background );
	}

	public void dispose()
	{
	}
}
