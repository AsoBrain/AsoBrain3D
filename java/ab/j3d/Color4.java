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
package ab.j3d;

/**
 * This interface defines a color using red, green, blue, and alpha properties.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public interface Color4
{
	/**
	 * Get red component.
	 *
	 * @return  Red component (0.0 - 1.0).
	 */
	float getRedFloat();

	/**
	 * Get red component as integer.
	 *
	 * @return  Red component (0 - 255).
	 */
	int getRedInt();

	/**
	 * Get green component.
	 *
	 * @return  Green component (0.0 - 1.0).
	 */
	float getGreenFloat();

	/**
	 * Get green component as integer.
	 *
	 * @return  Green component (0 - 255).
	 */
	int getGreenInt();

	/**
	 * Get blue component.
	 *
	 * @return  Blue component (0.0 - 1.0).
	 */
	float getBlueFloat();

	/**
	 * Get blue component as integer.
	 *
	 * @return  Blue component (0 - 255).
	 */
	int getBlueInt();

	/**
	 * Get alpha value.
	 *
	 * @return  Alpha value (0.0 - 1.0 = transparent - opaque).
	 */
	float getAlphaFloat();

	/**
	 * Get alpha value as integer.
	 *
	 * @return  Alpha value (0 - 255 = transparent - opaque).
	 */
	int getAlphaInt();

	/**
	 * Get color encoded as integer in RGB format (bits 0-7: blue, bits 8-15:
	 * green, bits 16-23: red).
	 *
	 * @return  Color as RGB integer.
	 */
	int getRGB();

	/**
	 * Get color encoded as integer in ARGB format (bits 0-7: blue, bits 8-15:
	 * green, bits 16-23: red, bits 24-31: alpha).
	 *
	 * @return  Color as ARGB integer.
	 */
	int getARGB();
}
