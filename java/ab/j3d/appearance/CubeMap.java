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
 * A cube map consists of six images, each projected onto a side of a cup.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public interface CubeMap
{
	/**
	 * Get image on the negative-X side of the cube.
	 *
	 * @return  Image on the negative-X side of the cube.
	 */
	@Nullable
	BufferedImage getImageX1();

	/**
	 * Get image on the negative-Y side of the cube.
	 *
	 * @return  Image on the negative-Y side of the cube.
	 */
	@Nullable
	BufferedImage getImageY1();

	/**
	 * Get image on the negative-Z side of the cube.
	 *
	 * @return  Image on the negative-Z side of the cube.
	 */
	@Nullable
	BufferedImage getImageZ1();

	/**
	 * Get image on the positive-X side of the cube.
	 *
	 * @return  Image on the positive-X side of the cube.
	 */
	@Nullable
	BufferedImage getImageX2();

	/**
	 * Get image on the positive-Y side of the cube.
	 *
	 * @return  Image on the positive-Y side of the cube.
	 */
	@Nullable
	BufferedImage getImageY2();

	/**
	 * Get image on the positive-Z side of the cube.
	 *
	 * @return  Image on the positive-Z side of the cube.
	 */
	@Nullable
	BufferedImage getImageZ2();
}
