/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2015 Peter S. Heijnen
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
package ab.j3d.view;

import java.awt.image.*;

import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Offscreen 3D view.
 *
 * @author Gerrit Meinders
 */
public abstract class OffscreenView3D
extends View3D
{
	/**
	 * Construct new view.
	 *
	 * @param scene Scene to view.
	 */
	protected OffscreenView3D( @NotNull final Scene scene )
	{
		super( scene );
	}

	/**
	 * Renders the view to a buffered image with the specified size.
	 *
	 * @param width  Width of the image.
	 * @param height Height of the image.
	 *
	 * @return Rendered image.
	 */
	public abstract BufferedImage renderImage( int width, int height );
}
