/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
package ab.j3d.view;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Node3D;

/**
 * Node in view model.
 *
 * @see     ViewModel
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ViewModelNode
{
	/**
	 * Application-assigned ID of this node.
	 */
	private final Object _id;

	/**
	 * Transform for this node.
	 */
	private Matrix3D _transform;

	/**
	 * Use alternate color.
	 */
	private boolean _alternate = false;

	/**
	 * Root in the 3D scene associated with this node.
	 */
	private Node3D _node3D;

	/**
	 * Material to use instead of actual materials.
	 */
	protected Material _materialOverride;

	/**
	 * Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	protected float _opacity;

	/**
	 * Construct new view model node. The <code>materialOverride</code> and
	 * <code>opacity</code> values can be used to provide extra hints for
	 * rendering objects.
	 *
	 * @param   id                  Application-assigned ID of this node.
	 * @param   transform           Initial transform (<code>null</code> => identity).
	 * @param   node3D              Root in the 3D scene.
	 * @param   materialOverride    Material to use instead of actual materials.
	 * @param   opacity             Extra opacity (0.0=translucent, 1.0=opaque).
	 */
	public ViewModelNode( final Object id , final Matrix3D transform , final Node3D node3D , final Material materialOverride , final float opacity )
	{
		_id              = id;
		_node3D          = node3D;
		_materialOverride = materialOverride;
		_opacity         = opacity;
		_transform       = ( transform != null ) ? transform : Matrix3D.INIT;
	}

	/**
	 * Get application-assigned ID of this node.
	 *
	 * @return  Application-assigned ID of this node.
	 */
	public final Object getID()
	{
		return _id;
	}

	/**
	 * Get the transform for this node.
	 *
	 * @return  Transform for this node.
	 */
	public final Matrix3D getTransform()
	{
		return _transform;
	}

	/**
	 * Set the transform for this node.
	 *
	 * @param   transform   Transform to set.
	 */
	public void setTransform( final Matrix3D transform )
	{
		if ( transform == null )
			throw new NullPointerException( "transform" );

		_transform = transform;
	}

	/**
	 * Get root in the 3D scene associated with this node.
	 *
	 * @return  Root in the 3D scene associated with this node.
	 */
	public final Node3D getNode3D()
	{
		return _node3D;
	}

	/**
	 * Set root in the 3D scene associated with this node.
	 *
	 * @param   node3D  Root in the 3D scene associated with this node.
	 */
	public void setNode3D( final Node3D node3D )
	{
		_node3D = node3D;
	}

	/**
	 * Get material to override actual materials.
	 *
	 * @return  Material to use instead of actual materials;
	 *          <code>null</code> to use only actual materials.
	 */
	public Material getMaterialOverride()
	{
		return _materialOverride;
	}

	/**
	 * Set material to override actual materials.
	 *
	 * @param   material    Material to use instead of actual materials;
	 *                      <code>null</code> to use only actual materials.
	 */
	public void setMaterialOverride( final Material material )
	{
		_materialOverride = material;
	}

	/**
	 * Get extra opacity, multiplied with existing opacity values.
	 *
	 * @return  Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	public float getOpacity()
	{
		return _opacity;
	}

	/**
	 * Set extra opacity, multiplied with existing opacity values.
	 *
	 * @param   opacity     Extra opacity (0.0=translucent, 1.0=opaque/unchanged).
	 */
	public void setOpacity( final float opacity )
	{
		_opacity = opacity;
	}

	/**
	 * Returns true if an alternate color is to be used.
	 *
	 * @return true if an alternate color is to be used.
	 */
	public boolean isAlternate()
	{
		return _alternate;
	}

	/**
	 * If set to true draws an alternate color.
	 *
	 * @param alternate whether to use an alternate color or not.
	 */
	public void setAlternate( final boolean alternate )
	{
		_alternate = alternate;
	}
}
