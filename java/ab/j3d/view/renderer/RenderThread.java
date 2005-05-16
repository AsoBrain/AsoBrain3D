/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2005 Peter S. Heijnen
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
package ab.j3d.view.renderer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import ab.j3d.model.Camera3D;

/**
 * This class implements a background rendering thread.
 *
 * @author Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class RenderThread
	extends Thread
{
	/**
	 * Component that will display the rendered image. It is used for two things:
	 * <ol>
	 *  <li>
	 *   The rendered image size is determined by the component's size
	 *   (reduced by its insets).
	 *  </li>
	 *  <li>
	 *   Its <code>repaint()</code> method will be envoked when a new image has
	 *   been rendered.
	 *  </li>
	 * </ol>
	 */
	private final Container _targetComponent;

	/**
	 * Camera that defines the view of the rendered scene.
	 */
	protected final Camera3D _camera;

	/**
	 * Renderer that will render the scene to a {@link BufferedImage}.
	 *
	 * @see     ImageRenderer
	 */
	private ImageRenderer _imageRenderer;

	/**
	 * This thread control flag is set when <code>requestTermination()</code>
	 * is called. It is used as exit condition by the main thread loop.
	 *
	 * @see     #requestTermination()
	 * @see     #isAlive()
	 * @see     #join()
	 */
	private boolean _terminationRequested;

	/**
	 * This thread control flag is set when <code>requestUpdate()</code> is
	 * called. It is used to trigger the thread loop to start rendering a new
	 * image. It is also tested at various loop points in the rendering code
	 * to abort rendering of a previous image, so the next rendering will be
	 * completed as soon as possible.
	 *
	 * @see     #requestUpdate()
	 */
	protected boolean _updateRequested;

	/**
	 * Last succesfully rendered image.
	 */
	private BufferedImage _renderedImage;

	/**
	 * Construct (and start) render thread.
	 *
	 * @param   targetComponent     On-screen component to render for.
	 * @param   camera              Camera that defines the view.
	 */
	public RenderThread( final Container targetComponent , final Camera3D camera )
	{
		_targetComponent      = targetComponent;
		_camera               = camera;
		_updateRequested      = true;
		_terminationRequested = false;
		_imageRenderer             = new ImageRenderer();
		_renderedImage        = null;

		final Class thisClass = getClass();
		setName( thisClass.getName() );
		setPriority( Thread.MIN_PRIORITY );
		start();
	}

	/**
	 * Get last succesfully rendered image. This will return <code>null</code>
	 * while the renderer is updating the image.
	 *
	 * @return  Rendered image;
	 *          <code>null</code> if the image is not available.
	 */
	public BufferedImage getRenderedImage()
	{
		return _renderedImage;
	}

	/**
	 * Request update of rendered image.
	 */
	public void requestUpdate()
	{
		if ( ( _imageRenderer != null ) && !_terminationRequested )
		{
			_updateRequested = true;
			_imageRenderer.abort();

			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * Request termination of the render thread.
	 */
	public void requestTermination()
	{
		if ( _imageRenderer != null )
		{
			_terminationRequested = true;
			_updateRequested = false;
			_imageRenderer.abort();

			synchronized ( this )
			{
				notifyAll();
			}
		}
	}

	/**
	 * This is the thread body to update the rendered image on demand.
	 */
	public void run()
	{
		final Container targetComponent = _targetComponent;

		boolean needRepaint = false;

		while ( !_terminationRequested && targetComponent.isVisible() )
		{
			try
			{
				if ( _updateRequested )
				{
					_updateRequested = false;
					render();
					needRepaint = true; //|= ( oldImage != _renderedImage );
				}

				if ( needRepaint && !_updateRequested )
				{
					targetComponent.repaint();
					needRepaint = false;
				}
			}
			catch ( Throwable t )
			{
				System.err.println( "Render exception: " + t );
				t.printStackTrace( System.err );
			}

			/*
			 * No update needed or an exception occured.
			 *
			 * Wait 300ms or wait to be notified.
			 */
			try
			{
				while ( !_updateRequested )
				{
					synchronized ( this )
					{
						wait( 300L );
					}
				}
			}
			catch ( InterruptedException e ) { /*ignored*/ }
		}

		_terminationRequested = false;
		_updateRequested      = false;
		_imageRenderer        = null;
	}

	/**
	 * This method is called to perform the actual work.
	 */
	protected void render()
	{
		final Container     component     = _targetComponent;
		final Camera3D      camera        = _camera;
		final ImageRenderer imageRenderer = _imageRenderer;

		final Color  background = component.getBackground();
		final Insets insets     = component.getInsets();
		final int    width      = Math.max( 1 , component.getWidth() - insets.left - insets.right );
		final int    height     = Math.max( 1 , component.getHeight() - insets.top - insets.bottom );

		final BufferedImage oldImage = _renderedImage;
		_renderedImage = null;
		final BufferedImage newImage = imageRenderer.renderScene( oldImage , width , height , background , camera );
		_renderedImage = imageRenderer.isAborted() ? oldImage : newImage;
	}
}
