/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2005
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

import java.awt.Graphics2D;
import java.awt.Paint;

import ab.j3d.Matrix3D;

/**
 * This class defines an insertion point for (a) detached sub-graph(s) into the
 * scene graph containing this node.
 * <p />
 * This allows a, often static, sub-graph to be inserted into various separate
 * scene graphs or even multiple times within the same scene graph, without
 * requiring duplication of nodes.
 * <p />
 * A transformation matrix can be set to define the location, orientation,
 * scale, etc. of the inserted sub-graph within the scene graph containing this
 * node. Initially, this is set an identity matrix.
 * <p />
 * Note that sub-graphs must be added just like normal child nodes. The only
 * difference is, that the sub-graphs parent reference does not reference back
 * to this node (in fact, the parent of detached sub-graphs should always be
 * <code>null</code>).
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ ($Date$, $Author$)
 */
public final class Insert3D
	extends Node3D
{
	/**
	 * Transformation from this node to parent node.
	 */
	private Matrix3D _transform;

	/**
	 * Construct node with default (void) properties.
	 */
	public Insert3D()
	{
		_transform = Matrix3D.INIT;
	}

	/**
	 * Construct node with the specified transformation matrix.
	 *
	 * @param   transform   Explicit matrix to use for transformation.
	 */
	public Insert3D( final Matrix3D transform )
	{
		_transform = transform;
	}

	/**
	 * Construct node with the specified transformation matrix and child node.
	 *
	 * @param   transform   Explicit matrix to use for transformation.
	 * @param   childNode   Child node to add to this node.
	 */
	public Insert3D( final Matrix3D transform , final Node3D childNode )
	{
		this( transform );
		addChild( childNode );
	}

	public void gatherLeafs( final Node3DCollection leafs , final Class leafClass , final Matrix3D previousTransform , final boolean upwards )
	{
		if ( upwards )
			throw new IllegalStateException( "can't traverse up from insert" );

		final Matrix3D newTransform;

		final Matrix3D thisTransform = getTransform();
		if ( ( thisTransform != null ) && ( thisTransform != Matrix3D.INIT ) )
			newTransform = thisTransform.multiply( previousTransform );
		else
			newTransform = previousTransform;

		super.gatherLeafs( leafs , leafClass , newTransform , upwards );
	}

	/**
	 * Get matrix with transformation.
	 *
	 * @return  Matrix3D with transformation matrix.
	 */
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set transformation using an explicit matrix for the transformation
	 * (transformation variables are ignored).
	 *
	 * @param   transform      Explicit matrix to use for transformation.
	 */
	public void setTransform( final Matrix3D transform )
	{
		_transform = transform;
	}

	/**
	 * Override {@link Node3D#setParentOfChild(Node3D)} to allow child nodes to
	 * be used in various scene graphs or multiple times within the same scene
	 * graph.
	 *
	 * @param   node    Node whose {@link #_parent} field to update.
	 */
	void setParentOfChild( final Node3D node )
	{
		/* do not update parent, so the child will not be detached */

		if ( node.getParent() != null )
			throw new IllegalStateException( "inserted child nodes should be detached from other scene graphs" );
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final boolean alternateAppearance )
	{
		final Matrix3D matrix = getTransform();
		super.paint( g , gTransform , matrix.multiply( viewTransform ) , alternateAppearance );
	}

	public void paint( final Graphics2D g , final Matrix3D gTransform , final Matrix3D viewTransform , final Paint outlinePaint , final Paint fillPaint , final float shadeFactor )
	{
		final Matrix3D matrix = getTransform();
		super.paint( g , gTransform , matrix.multiply( viewTransform ) , outlinePaint , fillPaint , shadeFactor );
	}
}
