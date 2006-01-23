/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
package ab.j3d.view.renderer;

import java.awt.Graphics;

import ab.j3d.Matrix3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;

/**
 * This class implements a software renderer for 3D scenes, shading the scene
 * using as wireframe to a {@link Graphics} context.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class WireframeRenderer
{
	/**
	 * Temporary object nodes.
	 */
	private final Node3DCollection _collectedObjects;

	/**
	 * Temporary render objects.
	 */
	protected RenderObject[] _renderObjects;

	/**
	 * Construct renderer.
	 */
	public WireframeRenderer()
	{
		_collectedObjects = new Node3DCollection();
		_renderObjects    = null;
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   g       Graphics to paint on.
	 * @param   x       Origin X-coordinate of painted image.
	 * @param   y       Origin Y-coordinate of painted image.
	 * @param   width   Width of painted image.
	 * @param   height  Height of painted image.
	 * @param   camera  Node with camera that defines the view.
	 */
	public final void renderScene( final Graphics g , final int x , final int y , final int width , final int height , final Camera3D camera )
	{
		final Node3DCollection objects = _collectedObjects;
		objects.clear();
		camera.gatherLeafs( objects , Object3D.class , Matrix3D.INIT , true );
		final int nrObjects = objects.size();

		if ( nrObjects > 0 )
		{
			RenderObject[] renderObjects = _renderObjects;
			if ( ( renderObjects == null ) || ( nrObjects > renderObjects.length ) )
			{
				final RenderObject[] newRenderObjects = new RenderObject[ nrObjects ];
				if ( renderObjects != null )
					System.arraycopy( renderObjects , 0 , newRenderObjects , 0 , renderObjects.length );

				renderObjects = newRenderObjects;
				_renderObjects = newRenderObjects;
			}

			/*
			 * Add all objects and draw them.
			 */
			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				RenderObject ro = renderObjects[ i ];
				if ( ro == null )
					renderObjects[ i ] = ro = new RenderObject();

				ro.set( (Object3D)objects.getNode( i ) , objects.getMatrix( i ) , Math.tan( camera.getAperture() / 2.0 ) , camera.getZoomFactor() , width , height , true );
			}

			for ( int i = 0 ; i < nrObjects ; i++ )
			{
				final RenderObject ro = renderObjects[ i ];

				for ( RenderObject.Face face = ro._faces ; face != null ; face = face._next )
					renderFace( g , x , y , face );
			}
		}
	}

	/**
	 * Render scene from camera.
	 *
	 * @param   g       Graphics context to paint on.
	 * @param   x       Origin X coordinate.
	 * @param   y       Origin Y coordinate.
	 * @param   face    Face to render.
	 */
	protected void renderFace( final Graphics g , final int x , final int y , final RenderObject.Face face )
	{
		final int[] vertexIndices = face._vi;
		if ( vertexIndices.length >= 3 )
		{
			final RenderObject ro = face.getRenderObject();
			final int[] vertexX = ro._ph;
			final int[] vertexY = ro._pv;

			int vertexIndex = vertexIndices[ vertexIndices.length - 1 ];
			int x1 = x + ( vertexX[ vertexIndex ] >> 8 );
			int y1 = y + ( vertexY[ vertexIndex ] >> 8 );
			int x2;
			int y2;

			for ( int vertex = 0 ; vertex < vertexIndices.length ; vertex++ )
			{
				vertexIndex = vertexIndices[ vertex ];
				x2 = x + ( vertexX[ vertexIndex ] >> 8 );
				y2 = y + ( vertexY[ vertexIndex ] >> 8 );

				g.drawLine( x1 , y1 , x2 , y2 );

				x1 = x2;
				y1 = y2;
			}
		}
	}
}
