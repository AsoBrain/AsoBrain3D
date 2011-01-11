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
package ab.j3d.appearance;

import java.awt.image.*;

import org.jetbrains.annotations.*;

/**
 * Basic implementation of {@link CubeMap} interface.
 *
 * @author  G. Meinders
 * @version $Revision$ ($Date$, $Author$)
 */
public class BasicCubeMap
	implements CubeMap
{
	/**
	 * Image on the negative-X side of the cube.
	 */
	private BufferedImage _imageX1 = null;

	/**
	 * Image on the negative-Y side of the cube.
	 */
	private BufferedImage _imageY1 = null;

	/**
	 * Image on the negative-Z side of the cube.
	 */
	private BufferedImage _imageZ1 = null;

	/**
	 * Image on the positive-X side of the cube.
	 */
	private BufferedImage _imageX2 = null;

	/**
	 * Image on the positive-Y side of the cube.
	 */
	private BufferedImage _imageY2 = null;

	/**
	 * Image on the positive-Z side of the cube.
	 */
	private BufferedImage _imageZ2 = null;

	/**
	 * Construct cube map. Cube map properties must be set for this cube map to
	 * be functional.
	 */
	public BasicCubeMap()
	{
	}

	/**
	 * Create cube map from individual images for each side of the cube.
	 *
	 * @param   mapX1   Image on the negative-X side of the cube.
	 * @param   mapY1   Image on the negative-Y side of the cube.
	 * @param   mapZ1   Image on the negative-Z side of the cube.
	 * @param   mapX2   Image on the positive-X side of the cube.
	 * @param   mapY2   Image on the positive-Y side of the cube.
	 * @param   mapZ2   Image on the positive-Z side of the cube.
	 */
	public BasicCubeMap( @NotNull final BufferedImage mapX1, @NotNull final BufferedImage mapY1, @NotNull final BufferedImage mapZ1, @NotNull final BufferedImage mapX2, @NotNull final BufferedImage mapY2, @NotNull final BufferedImage mapZ2 )
	{
		_imageX1 = mapX1;
		_imageY1 = mapY1;
		_imageZ1 = mapZ1;
		_imageX2 = mapX2;
		_imageY2 = mapY2;
		_imageZ2 = mapZ2;
	}

	@Override
	public BufferedImage getImageX1()
	{
		return _imageX1;
	}

	/**
	 * Set image on the negative-X side of the cube.
	 *
	 * @param   map     Image on the negative-X side of the cube.
	 */
	public void setImageX1( final BufferedImage map )
	{
		_imageX1 = map;
	}

	@Override
	public BufferedImage getImageY1()
	{
		return _imageY1;
	}

	/**
	 * Set image on the negative-Y side of the cube.
	 *
	 * @param   map     Image on the negative-Y side of the cube.
	 */
	public void setImageY1( final BufferedImage map )
	{
		_imageY1 = map;
	}

	@Override
	public BufferedImage getImageZ1()
	{
		return _imageZ1;
	}

	/**
	 * Set image on the negative-Z side of the cube.
	 *
	 * @param   map     Image on the negative-Z side of the cube.
	 */
	public void setImageZ1( final BufferedImage map )
	{
		_imageZ1 = map;
	}

	@Override
	public BufferedImage getImageX2()
	{
		return _imageX2;
	}

	/**
	 * Set image on the positive-X side of the cube.
	 *
	 * @param   map     Image on the positive-X side of the cube.
	 */
	public void setImageX2( final BufferedImage map )
	{
		_imageX2 = map;
	}

	@Override
	public BufferedImage getImageY2()
	{
		return _imageY2;
	}

	/**
	 * Set image on the positive-Y side of the cube.
	 *
	 * @param   map     Image on the positive-Y side of the cube.
	 */
	public void setImageY2( final BufferedImage map )
	{
		_imageY2 = map;
	}

	@Override
	public BufferedImage getImageZ2()
	{
		return _imageZ2;
	}

	/**
	 * Set image on the positive-Z side of the cube.
	 *
	 * @param   map     Image on the positive-Z side of the cube.
	 */
	public void setImageZ2( final BufferedImage map )
	{
		_imageZ2 = map;
	}
}
