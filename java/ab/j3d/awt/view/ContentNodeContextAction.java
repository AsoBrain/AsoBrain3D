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
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import ab.j3d.control.*;
import ab.j3d.model.*;


/**
 * This class defines an action for a {@link ContentNode}. This action includes
 * information about the pointer intersection in a 3D view when the action was
 * triggered from there.
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public abstract class ContentNodeContextAction
	extends AbstractAction
{
	/**
	 * Construct basic action with settings from resource bundle using the
	 * specified key. The following properties are set:
	 * <table>
	 *  <tr><th>resource key  </th><th>assigned to property</th><th>default</th></tr>
	 *  <tr><td>{key}         </td><td>name                </td><td>{key}  </td></tr>
	 *  <tr><td>{key}Icon     </td><td>smallIcon           </td></tr>none  </td></tr>
	 *  <tr><td>{key}Tip      </td><td>shortDescription    </td></tr>none  </td></tr>
	 *  <tr><td>{key}KeyStroke</td><td>keyboardAccelerator </td></tr>none  </td></tr>
	 *  <tr><td>{key}Mnemonic </td><td>mnemonicKey         </td></tr>none  </td></tr>
	 * </table>
	 *
	 * @param   bundle  Resource bundle to get settings from.
	 * @param   name    Name of action.
	 */
	protected ContentNodeContextAction( final ResourceBundle bundle, final String name )
	{
		putValue( ACTION_COMMAND_KEY, name );

		final Class<?> thisClass = getClass();
		final ClassLoader classLoader = thisClass.getClassLoader();

		try
		{
			putValue( NAME, bundle.getString( name ) );
		}
		catch ( MissingResourceException e )
		{
			/* ignore, property will not be set */
		}

		try
		{
			putValue( SMALL_ICON, new ImageIcon( classLoader.getResource( bundle.getString( name + "Icon" ) ) ) );
		}
		catch ( MissingResourceException e )
		{
			/* ignore, property will not be set */
		}

		try
		{
			putValue( SHORT_DESCRIPTION, bundle.getString( name + "Tip" ) );
		}
		catch ( MissingResourceException e )
		{
			/* ignore, property will not be set */
		}

		try
		{
			final KeyStroke keyStroke = KeyStroke.getKeyStroke( bundle.getString( name + "KeyStroke" ) );
			if ( keyStroke != null )
			{
				putValue( ACCELERATOR_KEY, keyStroke );
			}
		}
		catch ( MissingResourceException e )
		{
			/* ignore, property will not be set */
		}

		try
		{
			final KeyStroke keyStroke = KeyStroke.getKeyStroke( bundle.getString( name + "Mnemonic" ) );
			if ( ( keyStroke != null ) && ( keyStroke.getKeyCode() >= 0 ) )
			{
				putValue( MNEMONIC_KEY, Integer.valueOf( keyStroke.getKeyCode() ) );
			}
		}
		catch ( MissingResourceException e )
		{
			/* ignore, property will not be set */
		}
	}

	/**
	 * {@inheritDoc}
	 * <br /><br />
	 * Checks if the topLevelComponent is a {@link ContentNodeContextMenu}, if so
	 * it passes the parameters to the run method.
	 */
	public void actionPerformed( final ActionEvent event )
	{
		try
		{
			Component topLevelComponent = (Component)event.getSource() ;
			while ( ( topLevelComponent != null ) && ( topLevelComponent.getParent() != null ) )
			{
				topLevelComponent = topLevelComponent.getParent();
			}

			if( topLevelComponent instanceof ContentNodeContextMenu )
			{
				final ContentNodeContextMenu contentNodeContextMenu = (ContentNodeContextMenu)topLevelComponent;
				run( contentNodeContextMenu.getControlInputEvent(), contentNodeContextMenu.getContentNode(), contentNodeContextMenu.getFace3DIntersection() );
			}
			else
			{
				run( null, null, null );
			}
		}
		catch ( Throwable problem )
		{
			final StringWriter sw = new StringWriter();
			problem.printStackTrace( new PrintWriter( sw ) );
			final String stackTrace = sw.toString();
			final String[] stackTraceLines = stackTrace.split( "\n" );
			final Object message = new JScrollPane( new JList( stackTraceLines ) );
			JOptionPane.showMessageDialog( null, message, problem.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE );
		}
	}

	/**
	 * Run method for use with a {@link ControlInputEvent}.
	 * Parameters may be null.
	 *
	 * @param   controlInputEvent   {@link ControlInputEvent} that was used, can be null.
	 * @param   contentNode         {@link ContentNode} on which was clicked, can be null.
	 * @param   face3DIntersection  {@link Face3DIntersection} that was found, can be null.
	 */
	protected abstract void run( final ControlInputEvent controlInputEvent, final ContentNode contentNode, Face3DIntersection face3DIntersection );
}

