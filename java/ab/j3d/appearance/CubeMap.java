/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
package ab.j3d.appearance;

/**
 * A cube map consists of six images, each projected onto a side of a cup.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders
 */
public class CubeMap
{
	/**
	 * Name of the cube map.
	 */
	private String _name;

	/**
	 * Image on the left side of the cube.
	 */
	private TextureMap _left = null;

	/**
	 * Image on the front side of the cube.
	 */
	private TextureMap _front = null;

	/**
	 * Image on the bottom side of the cube.
	 */
	private TextureMap _bottom = null;

	/**
	 * Image on the right side of the cube.
	 */
	private TextureMap _right = null;

	/**
	 * Image on the rear side of the cube.
	 */
	private TextureMap _rear = null;

	/**
	 * Image on the top side of the cube.
	 */
	private TextureMap _top = null;

	/**
	 * Constructs a new instance.
	 *
	 * @param name Name of the cube map.
	 */
	public CubeMap( final String name )
	{
		_name = name;
		_left = new BasicTextureMap( name + "-left" );
		_front = new BasicTextureMap( name + "-front" );
		_bottom = new BasicTextureMap( name + "-bottom" );
		_right = new BasicTextureMap( name + "-right" );
		_rear = new BasicTextureMap( name + "-rear" );
		_top = new BasicTextureMap( name + "-top" );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param name   Name of the cube map.
	 * @param left   Image on the left side of the cube.
	 * @param front  Image on the front side of the cube.
	 * @param bottom Image on the bottom side of the cube.
	 * @param right  Image on the right side of the cube.
	 * @param rear   Image on the rear side of the cube.
	 * @param top    Image on the top side of the cube.
	 */
	public CubeMap( final String name, final TextureMap left, final TextureMap front, final TextureMap bottom, final TextureMap right, final TextureMap rear, final TextureMap top )
	{
		_name = name;
		_left = left;
		_front = front;
		_bottom = bottom;
		_right = right;
		_rear = rear;
		_top = top;
	}

	public String getName()
	{
		return _name;
	}

	/**
	 * Get image on the left side of the cube.
	 *
	 * @return Image on the left side of the cube.
	 */
	public TextureMap getImageLeft()
	{
		return _left;
	}

	/**
	 * Get image on the front side of the cube.
	 *
	 * @return Image on the front side of the cube.
	 */
	public TextureMap getImageFront()
	{
		return _front;
	}

	/**
	 * Get image on the bottom side of the cube.
	 *
	 * @return Image on the bottom side of the cube.
	 */
	public TextureMap getImageBottom()
	{
		return _bottom;
	}

	/**
	 * Get image on the right side of the cube.
	 *
	 * @return Image on the right side of the cube.
	 */
	public TextureMap getImageRight()
	{
		return _right;
	}

	/**
	 * Get image on the rear side of the cube.
	 *
	 * @return Image on the rear side of the cube.
	 */
	public TextureMap getImageRear()
	{
		return _rear;
	}

	/**
	 * Get image on the top side of the cube.
	 *
	 * @return Image on the top side of the cube.
	 */
	public TextureMap getImageTop()
	{
		return _top;
	}
}
