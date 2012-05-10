/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.control;

import ab.j3d.*;
import ab.j3d.model.*;

/**
 * Empty implementation of the {@link BoxControlDelegate} interface.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class AbstractBoxControlDelegate
	implements BoxControlDelegate
{
	public boolean isEnabled()
	{
		return true;
	}

	public boolean isVisible()
	{
		return isEnabled();
	}

	public void mousePressed( final ControlInputEvent event, final ContentNode node )
	{
	}

	public void mouseDragged( final ControlInputEvent event, final ContentNode node, final Vector3D offset )
	{
	}

	public void mouseReleased( final ControlInputEvent event, final ContentNode node )
	{
	}
}
