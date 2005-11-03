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
 * A Control listens to controlevent, and manipulates the view or 3d world with
 * the information in the events it receives.
 *
 * @author  Mart Slot
 * @version $Revision$ $Date$
 */
public interface Control
{
	/**
	 * Handle a ControlEvent. If the event should be passed on to the next
	 * Control, the event should be returned. If not, null should be returned.
	 * @param e The ControlEvent.
	 * @return  The ControlEvent, or null
	 */
	public ControlEvent handleEvent (ControlEvent e);
}
