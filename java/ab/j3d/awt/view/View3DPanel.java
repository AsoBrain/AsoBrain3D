/* $Id$
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
package ab.j3d.awt.view;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.control.*;
import ab.j3d.view.*;

/**
 * This can be used as container for a 3D view with some controls.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class View3DPanel
	extends JPanel
{
	/**
	 * 3D view.
	 */
	private final View3D _view;

	/**
	 * Tool bar.
	 */
	public JToolBar _toolBar = null;

	/**
	 * Create panel that displays a 3D view.
	 *
	 * @param   locale  Locale to use for controls.
	 * @param   view    3D view to display.
	 */
	public View3DPanel( final Locale locale, final View3D view )
	{
		super( new BorderLayout() );
		_view = view;
		setLocale( locale );
		add( view.getComponent(), BorderLayout.CENTER );
	}

	/**
	 * Add default tool bar to the panel.
	 */
	public void addDefaultToolBar()
	{
		setToolBar( createToolBar( getView(), getLocale() ) );
	}

	/**
	 * Get tool bar.
	 *
	 * @return  Tool bar;
	 *          <code>null</code> if panel has no tool bar.
	 */
	public JToolBar getToolBar()
	{
		return _toolBar;
	}

	/**
	 * Set tool bar. Note that can only be done once.
	 *
	 * @param   toolBar     Tool bar to use.
	 */
	public void setToolBar( final JToolBar toolBar )
	{
		if ( _toolBar != null )
		{
			throw new IllegalStateException( "Already have a tool bar" );
		}

		add( toolBar, BorderLayout.SOUTH );
		_toolBar = toolBar;
	}

	/**
	 * Get 3D view.
	 *
	 * @return  3D view.
	 */
	public View3D getView()
	{
		return _view;
	}

	/**
	 * Create tool bar for a view.
	 *
	 * @param   view    View to create tool bar for.
	 * @param   locale  Preferred locale for internationalization.
	 *
	 * @return  Tool bar.
	 */
	public static JToolBar createToolBar( final View3D view, final Locale locale )
	{
		final String label = view.getLabel();

		final JToolBar toolbar = new JToolBar( label );
		toolbar.setFloatable( false );

		if ( label != null )
		{
			toolbar.add( new JLabel( view.getLabel() + ": " ) );
		}

		final CameraControl cameraControl = view.getCameraControl();
		if ( cameraControl != null )
		{
			toolbar.add( new ZoomToFitAction( locale, cameraControl ) );
		}

		toolbar.add( new RenderingPolicyComboBox( locale, view ) );
		toolbar.add( new GridCheckBox( locale, view ) );

		return toolbar;
	}
}
