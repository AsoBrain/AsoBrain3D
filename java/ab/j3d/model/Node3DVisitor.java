/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d.model;

import ab.j3d.*;
import org.jetbrains.annotations.*;

/**
 * Visitor for tree of {@link Node3D}s.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Node3DVisitor
{
	/**
	 * Visits the given node.
	 *
	 * @param   node    Node to visit.
	 */
	void visitNode( @NotNull Node3D node );

	/**
	 * Notify entering of tree branch.
	 * <p>
	 * This is called prior iteration and before calling {@link #visitNode} for
	 * the given <code>branch</code>. This call is always reflected by a
	 * matching call to {@link #exitBranch} with the same arguments.
	 *
	 * @param   parent  From which node the branch is entered.
	 * @param   branch  Branch that is being entered.
	 */
	void enterBranch( @NotNull Node3D parent, @NotNull Node3D branch );

	/**
	 * Apply transformation matrix. The given transform should be multiplied
	 * with any existing transform to determine a combined transform.
	 *
	 * @param   transform   Transformation to apply.
	 */
	void applyTranform( @NotNull Matrix3D transform );

	/**
	 * Notify exiting of tree branch.
	 * <p>
	 * This is called to reflect a prior call to the {@link #enterBranch} method
	 * and is always called after a call to {@link #visitNode(Node3D)} for the
	 * given <code>branch</code>.
	 *
	 * @param   parent  From which node the branch is entered.
	 * @param   branch  Branch that is being entered.
	 */
	void exitBranch( @NotNull Node3D parent, @NotNull Node3D branch );
}
