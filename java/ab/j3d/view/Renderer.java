/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2009-2009
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

import java.util.*;

import ab.j3d.*;
import ab.j3d.model.*;

/**
 * This provides a possible base class for renderers.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public abstract class Renderer
{
	/**
	 * Construct renderer.
	 */
	protected Renderer()
	{
	}

	/**
	 * Render a scene.
	 *
	 * @param   scene           Scene to be rendered.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 * @param   background      Background to be rendered.
	 * @param   grid            Grid to be rendered (when enabled).
	 */
	public void renderScene( final Scene scene , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle, final Background background, final Grid grid )
	{
		renderBackground( background );

		if ( grid.isEnabled() )
		{
			renderGrid( grid );
		}

		renderContentNodes( scene.getContentNodes() , styleFilters , sceneStyle );
	}

	/**
	 * Renders the given background.
	 *
	 * @param   background  Background to be rendered.
	 */
	protected void renderBackground( final Background background )
	{
	}

	/**
	 * Renders the given grid. This method is only called when the given grid is
	 * enabled.
	 *
	 * @param   grid    Grid to be rendered.
	 */
	protected void renderGrid( final Grid grid )
	{
	}

	/**
	 * Render the content nodes of a scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	public void renderContentNodes( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		renderLights( nodes );
		renderObjects( nodes , styleFilters, sceneStyle );
	}

	/**
	 * Render lights.
	 *
	 * @param   nodes   Content nodes that may contain lights.
	 */
	protected void renderLights( final List<ContentNode> nodes )
	{
		Node3DCollection<Light3D> tmpLights = null;

		for ( final ContentNode node : nodes )
		{
			final Matrix3D nodeTransform = node.getTransform();

			final Node3D node3D = node.getNode3D();
			tmpLights = node3D.collectNodes( tmpLights, Light3D.class , nodeTransform , false );

			if ( ( tmpLights != null ) && ( tmpLights.size() > 0 ) )
			{
				for ( int i = 0 ; i < tmpLights.size() ; i++ )
				{
					final Light3D  light       = tmpLights.getNode( i );
					final Matrix3D light2world = tmpLights.getMatrix( i );

					renderLight( light2world , light );
				}

				tmpLights.clear();
			}
		}
	}

	/**
	 * Renders the given light.
	 *
	 * @param   light2world     Light to world transformation.
	 * @param   light           Light to be rendered.
	 */
	protected abstract void renderLight( final Matrix3D light2world , final Light3D light );

	/**
	 * Render objects in scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	protected void renderObjects( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		for ( final ContentNode node : nodes )
		{
			final Matrix3D node2world = node.getTransform();
			final Node3DCollection<Object3D> content = node.getContent();

			final RenderStyle nodeStyle = sceneStyle.applyFilters( styleFilters , node );

			for ( int i = 0 ; i < content.size() ; i++ )
			{
				final Matrix3D object2node  = content.getMatrix( i );
				final Matrix3D object2world = object2node.multiply( node2world );
				final Object3D object       = content.getNode( i );
				final int      faceCount    = object.getFaceCount();

				final RenderStyle objectStyle = nodeStyle.applyFilters( styleFilters , object );

				if ( faceCount > 0 )
				{
					renderObjectBegin( object2world , object , objectStyle );
					renderObject( object , objectStyle , styleFilters , object2world );
					renderObjectEnd();
				}
			}
		}
	}

	/**
	 * Prepares for rendering of the given object, e.g. by setting transforms.
	 *
	 * @param   object2world    Object to world transformation.
	 * @param   object          Object to be rendered.
	 * @param   objectStyle     Render style applied to the object.
	 */
	protected abstract void renderObjectBegin( Matrix3D object2world, Object3D object, RenderStyle objectStyle );

	/**
	 * Renders the given object.
	 *
	 * @param   object          Object to be rendered.
	 * @param   objectStyle     Render style applied to the object.
	 * @param   styleFilters    Style filters to be applied.
	 * @param   object2world    Object to world transformation.
	 */
	protected abstract void renderObject( Object3D object, RenderStyle objectStyle, Collection<RenderStyleFilter> styleFilters, Matrix3D object2world );

	/**
	 * Performs any cleanup needed after rendering an object.
	 */
	protected abstract void renderObjectEnd();
}
