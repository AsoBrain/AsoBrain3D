/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

/**
 * Listener for {@link NodeSelectionEvent}s.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface NodeSelectionListener
{
	/**
	 * Called when a {@link ab.j3d.model.Node3D} has been selected
	 *
	 * @param   evt The NodeSelectionEvent
	 */
	void nodeSelected( NodeSelectionEvent evt );
}
