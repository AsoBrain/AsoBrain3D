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

import java.util.Collection;
import java.util.List;

import ab.j3d.Matrix3D;
import ab.j3d.model.ContentNode;
import ab.j3d.model.Face3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

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
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	public void renderScene( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		renderLights( nodes );
		renderObjects( nodes , styleFilters, sceneStyle );
	}

	/**
	 * Render lights.
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

	protected abstract void renderLight( final Matrix3D light2world , final Light3D light );

	/*
	 * Render objects in scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	protected void renderObjects( final List<ContentNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		RenderStyle[] faceStyles = null;

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

				boolean anyMaterialEnabled = false;
				boolean anyFillEnabled     = false;
				boolean anyStrokeEnabled   = false;
				boolean anyVertexEnabled   = false;

				if ( ( faceStyles == null ) || ( faceCount >= faceStyles.length ) )
				{
					faceStyles = new RenderStyle[ faceCount ];
				}

				for ( int j = 0 ; j < faceCount; j++ )
				{
					final RenderStyle faceStyle = objectStyle.applyFilters( styleFilters , object.getFace( j ) );

					anyMaterialEnabled |= faceStyle.isMaterialEnabled();
					anyFillEnabled     |= faceStyle.isFillEnabled();
					anyStrokeEnabled   |= faceStyle.isStrokeEnabled();
					anyVertexEnabled   |= faceStyle.isVertexEnabled();

					faceStyles[ j ] = faceStyle;
				}

				if ( anyMaterialEnabled || anyFillEnabled || anyStrokeEnabled || anyVertexEnabled )
				{
					renderObjectBegin( object2world , object , objectStyle );

					if ( anyMaterialEnabled )
					{
						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isMaterialEnabled() )
							{
								renderMaterialFace( object.getFace( j ), faceStyle );
							}
						}
					}

					if ( anyFillEnabled )
					{
						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isFillEnabled() )
							{
								renderFilledFace( object.getFace( j ), faceStyle );
							}
						}
					}

					if ( anyStrokeEnabled )
					{
						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isStrokeEnabled() )
							{
								renderStrokedFace( object.getFace( j ), faceStyle );
							}
						}
					}

					if ( anyVertexEnabled )
					{
						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isVertexEnabled() )
							{
								renderFaceVertices( faceStyle , object2world , object.getFace( j ) );
							}
						}
					}

					renderObjectEnd();
				}
			}
		}
	}

	protected abstract void renderObjectBegin( final Matrix3D object2world , final Object3D object , final RenderStyle objectStyle );

	protected abstract void renderMaterialFace( final Face3D face , final RenderStyle style );

	protected abstract void renderFilledFace( final Face3D face, final RenderStyle style );

	protected abstract void renderStrokedFace( final Face3D face, final RenderStyle style );

	protected abstract void renderFaceVertices( final RenderStyle style , final Matrix3D object2world , final Face3D face );

	protected abstract void renderObjectEnd();

}
