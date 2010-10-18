/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2009-2010 Peter S. Heijnen
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
package ab.j3d.geom;

import java.awt.*;
import java.util.*;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Transforms 2-dimensional shapes into tessellated primitives. The result
 * consists primarily of {@link TessellationPrimitive} objects stored in a
 * {@link Tessellation} that is build by a {@link TessellationBuilder}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface Tessellator
{
	/**
	 * Returns the normal of the shapes being tessellated. The default value is
	 * [0, 0, 0].
	 *
	 * @return  Normal of tessellated shapes.
	 */
	Vector3D getNormal();

	/**
	 * Sets the normal used of the shapes being tessellated.
	 *
	 * @param   normal  Normal to be set.
	 */
	void setNormal( Vector3D normal );

	/**
	 * Returns the flatness used when flattening input shapes.
	 *
	 * @return  Flatness used when flattening input shapes.
	 */
	double getFlatness();

	/**
	 * Sets the flatness used when flattening input shapes.
	 *
	 * @param   flatness    Flatness to be set.
	 */
	void setFlatness( double flatness );

	/**
	 * Tessellates the given shape and returns the result.
	 *
	 * @param   result  Tessellation result.
	 * @param   shape   Shape to be tessellated.
	 */
	void tessellate( @NotNull TessellationBuilder result, @NotNull Shape shape );

	/**
	 * Tessellates a combination of <code>positive</code> and
	 * <code>negative</code> shapes. The result is the difference between the
	 * two.
	 *
	 * @param   result      Tessellation result.
	 * @param   positive    Positive geometry.
	 * @param   negative    Negative geometry.
	 */
	void tessellate( @NotNull TessellationBuilder result, @NotNull Shape positive , @NotNull Collection<? extends Shape> negative );
}
