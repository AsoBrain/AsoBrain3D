/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2025 Peter S. Heijnen
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

import TextureMap from '../ab.j3d/TextureMap.js';

/**
 * This interface describes the appearance of an object.
 *
 * @author Peter S. Heijnen
 */
export default interface Appearance
{
	/**
	 * Color map to use. This map provides color and possibly opacity (alpha)
	 * data. Set to <code>null</code> if no color map is used.
	 *
	 * @return Color map; <code>null</code> if no color map is available.
	 */
	colorMap: TextureMap | null;
}
