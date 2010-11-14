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
 * This class provides a tree walker, which can be used to traverse a scene
 * graph. A {@link Node3DVisitor} will be called for each visited node.
 * <p>
 * A depth-first algorithm is provided through the {@link #walk} methods.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Node3DTreeWalker
{
	/**
	 * Perform depth-first scene graph walk, starting at the given node with an
	 * identity transformation.
	 * <p>
	 * The <code>visitor</code> is called for each visited node, including the
	 * given <code>node</code>.
	 *
	 * @param   visitor     Visitor that will be called for each visited node.
	 * @param   node        Root {@link Node3D} to start tree walk at.
	 *
	 * @return  <code>true</code> if the tree walk was finished normally;
	 *          <code>false</code> if the tree walk was aborted.
	 */
	public static boolean walk( @NotNull final Node3DVisitor visitor, @NotNull final Node3D node )
	{
		return walk( visitor, Matrix3D.IDENTITY, node );
	}

	/**
	 * Perform depth-first scene graph walk, starting at the given node with the
	 * given transformation matrix.
	 * <p>
	 * The <code>visitor</code> is called for each visited node, including the
	 * given <code>node</code>.
	 *
	 * @param   visitor     Visitor that will be called for each visited node.
	 * @param   node        Root {@link Node3D} to start tree walk at.
	 * @param   transform   Initial transformation matrix.
	 *
	 * @return  <code>true</code> if the tree walk was finished normally;
	 *          <code>false</code> if the tree walk was aborted.
	 */
	public static boolean walk( @NotNull final Node3DVisitor visitor, @NotNull final Matrix3D transform, @NotNull final Node3D node )
	{
		return walk( visitor, createPath( null, transform, node ) );
	}

	/**
	 * Perform depth-first scene graph walk, starting at the given path element.
	 * <p>
	 * The <code>visitor</code> is called for each visited node, including the
	 * given <code>node</code>.
	 *
	 * @param   visitor     Visitor that will be called for each visited node.
	 * @param   path        Path element to walk from.
	 *
	 * @return  <code>true</code> if the tree walk was finished normally;
	 *          <code>false</code> if the tree walk was aborted.
	 */
	public static boolean walk( @NotNull final Node3DVisitor visitor, @NotNull final Node3DPath path )
	{
		boolean result = visitor.visitNode( path );
		if ( result )
		{
			final Node3D node = path.getNode();
			final Matrix3D transform = path.getTransform();

			for ( final Node3D child : node.getChildren() )
			{
				if ( !walk( visitor, createPath( path, transform, child ) ) )
				{
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Create path to the specified node. Any transformation defined by the node
	 * (if it's a {@link Transform3D}) must be combined with the given
	 * <code>transform</code>
	 *
	 * @param   parentPath          Path to parent (<code>null</code> if root).
	 * @param   currentTransform    Current transform to use as base.
	 * @param   node                Node to create path to.
	 *
	 * @return  {@link Node3DPath} for the <code>node</code>.
	 */
	@NotNull
	protected static Node3DPath createPath( @Nullable final Node3DPath parentPath, @NotNull final Matrix3D currentTransform, @NotNull final Node3D node )
	{
		return new Node3DPath( parentPath, getCombinedTransform( currentTransform, node ), node );
	}

	/**
	 * Determine combined transform for the given node, based on a 'current'
	 * transform.
	 *
	 * @param   currentTransform    Current transform to use as base.
	 * @param   node                Node whose transform to apply (if any).
	 *
	 * @return  Combined transformation matrix for <code>node</code>.
	 */
	@NotNull
	protected static Matrix3D getCombinedTransform( @NotNull final Matrix3D currentTransform, @NotNull final Node3D node )
	{
		Matrix3D result = currentTransform;

		if ( node instanceof Transform3D )
		{
			final Matrix3D nodeTransform = ( (Transform3D) node ).getTransform();
			if ( ( nodeTransform != null ) && ( nodeTransform != Matrix3D.IDENTITY ) )
			{
				result = nodeTransform.multiply( result );
			}
		}

		return result;
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Node3DTreeWalker()
	{
	}
}