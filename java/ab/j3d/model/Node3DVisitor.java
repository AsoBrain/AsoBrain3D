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

import org.jetbrains.annotations.*;

/**
 * Visitor for {@link Node3D} objects. This visitor is typically envoked by a
 * {@link Node3DTreeWalker}.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public interface Node3DVisitor
{
	/**
	 * Visits the node with given path. The return value indicates wether the
	 * 'tour' along which the visitor is taken should be continued or aborted.
	 *
	 * @param   path    Path to the node that is visited.
	 *
	 * @return  <code>true</code> if the 'tour' should continue;
	 *          <code>false</code> if the 'tour' should be aborted.
	 */
	boolean visitNode( @NotNull Node3DPath path );
}
