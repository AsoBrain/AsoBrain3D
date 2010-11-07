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
 * Abstract implementation of {@link Node3DVisitor}. This keeps track of a
 * 'current' transform and implements the transform stack
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class AbstractNode3DVisitor
	implements Node3DVisitor
{
	/**
	 * Current combined transformation matrix.
	 */
	@NotNull
	private Matrix3D _transform;

	/**
	 * Stack for pushed transforms.
	 */
	private Node3DPath _path;

	/**
	 * Construct visitor. The initial transformation matrix is set to the
	 * identity matrix.
	 */
	public AbstractNode3DVisitor()
	{
		this( Matrix3D.IDENTITY );
	}

	/**
	 * Construct visitor.
	 *
	 * @param   transform   Initial transformation matrix.
	 */
	public AbstractNode3DVisitor( @NotNull final Matrix3D transform )
	{
		_transform = transform;
		_path = null;
	}

	/**
	 * Get the shortest path from the initial node that was visisted and the
	 * last batch that was entered.
	 * <dl>
	 *  <dt>NOTE 1</dt>
	 *  <dd>This path does not include the node being visited.</dd>
	 *  <dt>NOTE 2</dt>
	 *  <dd>Returns <code>null</code> when the initial node is visited.</dd>
	 *  <dt>NOTE 3</dt>
	 *  <dd>You can call {@link #getExtendedPath(Node3D)} with the visited
	 *   {@link Node3D} from {@link #visitNode} to create a path that includes
	 *   the visited node.</dd>
	 * </dl>
	 *
	 * @return  Shortest path from the initial node the visitor started at the last batch that was entered.
	 *
	 * @see     #getExtendedPath
	 */
	@Nullable
	public Node3DPath getPath()
	{
		return _path;
	}

	/**
	 * Extend path for the given node and using the current combined transform.
	 * This may be called from {@link #visitNode} to create a path that includes
	 * the visited node.
	 *
	 * @param   node    Node to extend path for.
	 *
	 * @return  {@link Node3DPath}.
	 */
	@NotNull
	public Node3DPath getExtendedPath( @NotNull final Node3D node )
	{
		return new Node3DPath( _path, getTransform(), node );
	}

	/**
	 * Get current combined transformation matrix.
	 *
	 * @return  Current combined transformation matrix.
	 */
	@NotNull
	public Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set current transformation matrix.
	 * <dl>
	 *  <dt>NOTE</dt>
	 *  <dd> This transform will be overwritten during a 'visit' when
	 *   {@link #exitBranch(Node3D, Node3D)} is called.</dd>
	 * </dl>
	 *
	 * @param   transform   Transformation matrix.
	 */
	public void setTransform( @NotNull final Matrix3D transform )
	{
		_transform = transform;
	}

	@Override
	public void applyTranform( @NotNull final Matrix3D transform )
	{
		setTransform( transform.multiply( getTransform() ) );
	}

	@Override
	public void enterBranch( @NotNull final Node3D parent, @NotNull final Node3D branch )
	{
		_path = getExtendedPath( parent );
	}

	@Override
	public void exitBranch( @NotNull final Node3D parent, @NotNull final Node3D branch )
	{
		final Node3DPath path = _path;
		if ( ( path == null ) || ( path.getNode() != parent ) )
		{
			throw new IllegalStateException( "unbalanced exitBranch" );
		}

		setTransform( path.getTransform() );
		_path = path.getPrevious();
	}
}
