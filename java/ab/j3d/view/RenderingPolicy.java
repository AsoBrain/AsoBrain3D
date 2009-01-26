/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2009
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

/**
 * Rendering policy.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public enum RenderingPolicy
{
	/**
	 * Rendering policy: solid.
	 * <p />
	 * This should result in a photorealistic rendering of the scene, taking the
	 * physical properties of the scene into account as much as possible.
	 * <p />
	 * Example implementation: ray-tracing / per-pixel shading and texture mapping.
	 */
	SOLID ,

	/**
	 * Rendering policy: schematic.
	 * <p />
	 * This should clarify the structure and design of the scene. This is
	 * generally a form that should allow manipulation of (large) objects in a
	 * scene and could be used to provide dimension information.
	 * <p />
	 * Example implementation: flat shading / functional color coding.
	 */
	SCHEMATIC ,

	/**
	 * Rendering policy: sketch.
	 * <p />
	 * A non-photorealistic rendering method that give a good idea of what is
	 * intended by the scene, but does not require much detail.
	 * <p />
	 * Example implementation: pencil sketch / cartoon rendering / silhouette.
	 */
	SKETCH ,

	/**
	 * Rendering policy: wireframe.
	 * <p />
	 * Technical rendering including only edges, points, or iconic
	 * representations of elements in a scene. This is the classical rendering
	 * method in CAD software. This provides a quick overview and insight to
	 * the complexity of a scene.
	 * <p />
	 * Example implementation: pencil sketch / cartoon rendering / silhouette.
	 */
	WIREFRAME
}
