/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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

import TextureMap from './TextureMap';

/**
 * A cube map consists of six images, each projected onto a side of a cup.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders
 */
export default interface CubeMap
{
	/**
	 * Name of the cube map.
	 */
	name: string;

	/**
	 * Image on the left side of the cube.
	 */
	left: TextureMap;

	/**
	 * Image on the front side of the cube.
	 */
	front: TextureMap;

	/**
	 * Image on the bottom side of the cube.
	 */
	bottom: TextureMap;

	/**
	 * Image on the right side of the cube.
	 */
	right: TextureMap;

	/**
	 * Image on the rear side of the cube.
	 */
	rear: TextureMap;

	/**
	 * Image on the top side of the cube.
	 */
	top: TextureMap;
}
