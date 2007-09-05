/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.model;

import ab.j3d.Material;

/**
 * Defines a mapping from spatial coordinates to texture coordinates.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface UVMap
{
	/**
	 * Generates texture coordinates for the given face.
	 *
	 * @param   material            Material used to define texture scale.
	 * @param   vertexCoordinates   Vertex coordinates, as xyz-triplets.
	 * @param   vertexIndices       Indices for all vertices in the face. Each
	 *                              index corresponds to three elements of
	 *                              <code>vertexCoordinates</code>, representing
	 *                              the x, y and z-coordinates of the vertex.
	 * @param   textureU            Texture u-coordinates to be set.
	 * @param   textureV            Texture v-coordinates to be set.
	 */
	void generate( final Material material , final double[] vertexCoordinates , final int[] vertexIndices , final float[] textureU , final float[] textureV );
}
