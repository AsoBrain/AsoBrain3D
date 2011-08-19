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
import java.util.concurrent.*;
import javax.swing.*;

import com.numdata.oss.log.*;

/**
 * <p>Encapsulates a set of changes to the scene. This implementation
 * prepares updates asynchronously using a given {@link ExecutorService}.
 * When all preparations are completed, the scene is updated on the EDT.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class AsynchronousSceneUpdate
	extends SceneUpdate
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( AsynchronousSceneUpdate.class );

	/**
	 * Executor service used to perform tasks asynchronously.
	 */
	private ExecutorService _executorService;

	/**
	 * Prepares updates asynchronously.
	 */
	private PreparationRunner _preparationRunner;

	/**
	 * If set, the updater should effectively be synchronous, with the exception
	 * that preparations may already have been completed asynchronously, saving
	 * some time.
	 */
	private static final boolean WAIT_FOR_PREPARATIONS_ON_EDT = false;

	/**
	 * Constructs a new instance.
	 *
	 * @param   scene               Scene to be updated.
	 * @param   executorService     Used to perform tasks asynchronously.
	 */
	AsynchronousSceneUpdate( final Scene scene, final ExecutorService executorService )
	{
		super( scene );
		_executorService = executorService;
		_preparationRunner = null;
	}

	@Override
	public void add( final NodeUpdate change )
	{
		if ( !SwingUtilities.isEventDispatchThread() )
		{
			throw new IllegalStateException( "Should only be called from EDT." );
		}

		_updates.add( change );

		PreparationRunner preparationRunner = _preparationRunner;
		if ( preparationRunner == null )
		{
			preparationRunner = new PreparationRunner();
			_preparationRunner = preparationRunner;
			_executorService.submit( preparationRunner );
		}

		preparationRunner.add( change );
	}

	private static class PreparationRunner
		implements Runnable
	{
		/**
		 * Updates to be prepared.
		 */
		private final Queue<NodeUpdate> _updates = new ConcurrentLinkedQueue<NodeUpdate>();

		/**
		 * Set when no more updates will be added.
		 */
		private volatile boolean _finish = false;

		/**
		 * Lock used to implement {@link #waitFor()}.
		 */
		private final Semaphore _waitForLock = new Semaphore( 0 );

		/**
		 * Adds the given update to the queue of updates to be prepared.
		 * It will be prepared asynchronously.
		 *
		 * @param   nodeUpdate  Update to be prepared asynchronously.
		 */
		public void add( final NodeUpdate nodeUpdate )
		{
			if ( !SwingUtilities.isEventDispatchThread() )
			{
				throw new IllegalStateException( "Should only be called from EDT." );
			}

			if ( _finish )
			{
				throw new IllegalStateException( "Already finished." );
			}

			_updates.add( nodeUpdate );
		}

		/**
		 * Notifies the runner that no more updates will be added, allowing the
		 * {@link #run()} method to finish.
		 */
		public void finish()
		{
			if ( !SwingUtilities.isEventDispatchThread() )
			{
				throw new IllegalStateException( "Should only be called from EDT." );
			}

			_finish = true;
		}

		/**
		 * Waits for all preparation tasks to finish. May only be called after
		 * {@link #finish()}.
		 *
		 * @throws  InterruptedException if the calling thread is interrupted
		 *          while waiting.
		 */
		public void waitFor()
			throws InterruptedException
		{
			if ( !_finish )
			{
				throw new IllegalStateException( "finish() not called" );
			}

			_waitForLock.acquire();
			_waitForLock.release(); /// For any subsequent calls.
		}

		@Override
		public void run()
		{
			try
			{
				while ( true )
				{
					final NodeUpdate update = _updates.poll();
					if ( update == null )
					{
						if ( _finish )
						{
							break;
						}
						else
						{
							try
							{
								Thread.sleep( 10L );
							}
							catch ( InterruptedException e )
							{
								// Ignore.
							}
						}
					}
					else
					{
						update.prepare();
					}
				}
			}
			finally
			{
				_waitForLock.release();
			}
		}
	}

	@Override
	public void run()
	{
		if ( !SwingUtilities.isEventDispatchThread() )
		{
			throw new IllegalStateException( "Should only be called from EDT." );
		}

		LOG.trace( "Running asynchronous scene update." );

		final PreparationRunner preparationRunner = _preparationRunner;
		if ( preparationRunner == null )
		{
			LOG.trace( "No preparation needed. Running updates." );
			runUpdate();
		}
		else
		{
			LOG.trace( "Initiating completion of preparations." );
			preparationRunner.finish();
			_preparationRunner = null;

			if ( WAIT_FOR_PREPARATIONS_ON_EDT )
			{
				try
				{
					LOG.trace( "Waiting for preparations to finish." );
					preparationRunner.waitFor();
					LOG.trace( "Running updates." );
					runUpdate();
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}
				LOG.trace( "Done." );
			}
			else
			{
				LOG.trace( "Scheduling update." );
				_executorService.submit( new Runnable()
				{
					@Override
					public void run()
					{
						if ( isCurrent() )
						{
							try
							{
								LOG.trace( "Update is current. Waiting for preparations to finish." );
								preparationRunner.waitFor();

								LOG.trace( "Invoking updates on EDT." );
								SwingUtilities.invokeAndWait( new Runnable()
								{
									@Override
									public void run()
									{
										LOG.trace( "Running updates on EDT." );
										runUpdate();
									}
								} );
								LOG.trace( "Done." );
							}
							catch ( Exception e )
							{
								e.printStackTrace();
							}
						}
						else
						{
							LOG.trace( "Update is no longer current." );
						}
					}
				} );
			}
		}
	}
}
