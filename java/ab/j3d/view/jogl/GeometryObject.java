/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2011-2011 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

/**
 * Stores geometry in such a way that it can be rendered efficiently.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public interface GeometryObject
{
	/**
	 * Draws the geometry represented by this object.
	 */
	void draw();

	/**
	 * Deletes the resources allocated for this object.
	 */
	void delete();
}
