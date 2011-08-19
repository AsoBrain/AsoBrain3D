/*
 * $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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
package ab.j3d.model;

import java.util.*;

/**
 * <p>Encapsulates a set of changes to the scene. This implementation simply
 * performs all updates at once when {@link #run} is called.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class SceneUpdate
{
	/**
	 * Scene to be updated.
	 */
	protected final Scene _scene;

	/**
	 * Sequence number of the update, with respect to other updates.
	 */
	private final int _sequenceNumber;

	/**
	 * Changes included in the update.
	 */
	protected final List<NodeUpdate> _updates;

	/**
	 * Constructs a new instance.
	 *
	 * @param   scene   Scene to be updated.
	 */
	SceneUpdate( final Scene scene )
	{
		_scene = scene;
		_sequenceNumber = scene.incrementAndGetUpdateSequenceNumber();
		_updates = new ArrayList<NodeUpdate>();
	}

	/**
	 * Adds the given change to the update.
	 *
	 * @param   change  Changes the scene.
	 */
	public void add( final NodeUpdate change )
	{
		_updates.add( change );
	}

	/**
	 * Runs the update.
	 */
	public void run()
	{
		runPrepare();
		runUpdate();
	}

	/**
	 * Calls {@link NodeUpdate#prepare()} for all registered updates.
	 */
	protected void runPrepare()
	{
		for ( final NodeUpdate update : _updates )
		{
			try
			{
				update.prepare();
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
	}

	/**
	 * Calls {@link NodeUpdate#update(Scene)} for all registered updates.
	 */
	protected void runUpdate()
	{
		final Scene scene = _scene;

		if ( !_updates.isEmpty() )
		{
			for ( final NodeUpdate update : _updates )
			{
				try
				{
					update.update( scene );
				}
				catch ( Exception e )
				{
					throw new RuntimeException( e );
				}
			}
		}
	}

	/**
	 * Returns whether this is the current scene update.
	 *
	 * @return  <code>true</code> if this is the current update.
	 */
	public boolean isCurrent()
	{
		return _sequenceNumber == _scene.getUpdateSequenceNumber();
	}
}
