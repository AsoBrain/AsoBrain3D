/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2010-2010 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

/**
 * Receives notification from a {@link View3D} whenever rendering of a frame
 * starts or ends.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public interface ViewListener
{
	/**
	 * Called just before the view starts to render a frame.
	 *
	 * @param   view    View that renders the frame.
	 */
	void beforeFrame( View3D view );

	/**
	 * Called just after the view finishes rendering a frame.
	 *
	 * @param   view    View that renders the frame.
	 */
	void afterFrame( View3D view );
}
