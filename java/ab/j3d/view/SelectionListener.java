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
 * The SelectionListener can register itself with a SelectionModel. It is then
 * notified when selection changes.
 * @author Mart Slot
 * @version $Revision$ $Date$
 */
public interface SelectionListener
{
	/**
	 * Called when selection has changed.
	 * @param e The SelectionChangeEvent.
	 */
	public void selectionChanged( SelectionChangeEvent e );
}
