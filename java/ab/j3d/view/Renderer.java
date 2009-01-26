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

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
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
 * @noinspection JavaDoc,UnusedDeclaration
 */
public class Renderer
{
	/**
	 * Construct renderer.
	 */
	public Renderer()
	{
	}

	/**
	 * Render a scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	public void renderScene( final List<ViewModelNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		renderLights( nodes );
		renderObjects( nodes , styleFilters, sceneStyle );
	}

	/**
	 * Render lights.
	 */
	protected void renderLights( final List<ViewModelNode> nodes )
	{
		Node3DCollection<Light3D> tmpLights = null;

		for ( final ViewModelNode node : nodes )
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

	protected void renderLight( final Matrix3D light2world , final Light3D light )
	{
	}

	/*
	 * Render objects in scene.
	 *
	 * @param   nodes           Nodes in the scene.
	 * @param   styleFilters    Style filters to apply.
	 * @param   sceneStyle      Render style to use as base for scene.
	 */
	protected void renderObjects( final List<ViewModelNode> nodes , final Collection<RenderStyleFilter> styleFilters , final RenderStyle sceneStyle )
	{
		RenderStyle[] faceStyles = null;

		for ( final ViewModelNode node : nodes )
		{
			final Matrix3D node2world = node.getTransform();
			final Node3DCollection<Object3D> content = node.getContent();

			final RenderStyle nodeStyle  = applyStyle( styleFilters , sceneStyle , node );

			for ( int i = 0 ; i < content.size() ; i++ )
			{
				final Matrix3D object2node  = content.getMatrix( i );
				final Matrix3D object2world = object2node.multiply( node2world );
				final Object3D object       = content.getNode( i );
				final int      faceCount    = object.getFaceCount();

				final RenderStyle objectStyle = applyStyle( styleFilters , nodeStyle , object );

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
					final RenderStyle faceStyle = applyStyle( styleFilters , objectStyle , object.getFace( j ) );

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
						renderMaterialBegin( object2world , object );

						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isMaterialEnabled() )
							{
								renderMaterialFace( object.getFace( j ), faceStyle );
							}
						}

						renderMaterialEnd();
					}

					if ( anyFillEnabled )
					{
						renderFillBegin( object2world , object );

						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isFillEnabled() )
							{
								renderFilledFace( object.getFace( j ), faceStyle );
							}
						}

						renderFillEnd();
					}

					if ( anyStrokeEnabled )
					{
						renderStrokesBegin( object2world , object );

						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isStrokeEnabled() )
							{
								renderStrokedFace( object.getFace( j ), faceStyle );
							}
						}

						renderStrokesEnd();
					}

					if ( anyVertexEnabled )
					{
						renderVerticesBegin( object2world , object );

						for ( int j = 0 ; j < faceCount; j++ )
						{
							final RenderStyle faceStyle = faceStyles[ j ];
							if ( faceStyle.isVertexEnabled() )
							{
								renderFaceVertices( faceStyle , object2world , object.getFace( j ) );
							}
						}

						renderVerticesEnd();
					}

					renderObjectEnd();
				}
			}
		}
	}

	protected void renderObjectBegin( final Matrix3D object2world , final Object3D object , final RenderStyle objectStyle )
	{
	}

	protected void renderMaterialBegin( final Matrix3D object2world , final Object3D object )
	{
	}

	protected void renderMaterialFace( final Face3D face, final RenderStyle style )
	{
		final Material materialOverride = style.getMaterialOverride();

		final Material material = ( materialOverride != null ) ? materialOverride: face.getMaterial();
		if ( material != null )
		{
			final boolean lightingEnabled = style.isMaterialLightingEnabled();
		}
	}

	protected void renderMaterialEnd()
	{
	}

	protected void renderFillBegin( final Matrix3D object2world , final Object3D object )
	{
	}

	protected void renderFilledFace( final Face3D face, final RenderStyle style )
	{
		final Color   color           = style.getFillColor();
		final boolean lightingEnabled = style.isFillLightingEnabled();
	}

	protected void renderFillEnd()
	{
	}

	protected void renderStrokesBegin( final Matrix3D object2world , final Object3D object )
	{
	}

	protected void renderStrokedFace( final Face3D face, final RenderStyle style )
	{
		final Color   color           = style.getStrokeColor();
		final float   width           = style.getStrokeWidth();
		final boolean lightingEnabled = style.isStrokeLightingEnabled();
	}

	protected void renderStrokesEnd()
	{
	}

	protected void renderVerticesBegin( final Matrix3D object2world , final Object3D object )
	{
	}

	protected void renderFaceVertices( final RenderStyle style , final Matrix3D object2world , final Face3D face )
	{
		final Color   color           = style.getVertexColor();
		final boolean lightingEnabled = style.isVertexLightingEnabled();
		final float   size            = style.getVertexSize();
	}

	protected void renderVerticesEnd()
	{
	}

	protected void renderObjectEnd()
	{
	}

	/**
	 * Apply filters to existing style.
	 *
	 * @param   styleFilters    Style filters to apply.
	 * @param   baseStyle       Base style to apply filters to (never <code>null</code>).
	 * @param   context         Context object (never <code>null</code>).
	 *
	 * @return  Filtered style.
	 */
	public static RenderStyle applyStyle( final Collection<RenderStyleFilter> styleFilters , final RenderStyle baseStyle , final Object context )
	{
		RenderStyle result = baseStyle;

		for ( final RenderStyleFilter filter : styleFilters )
		{
			result = filter.applyFilter( result , context );
		}

		return result;
	}
}
