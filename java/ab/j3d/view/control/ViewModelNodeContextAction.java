/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2008
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
package ab.j3d.view.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import ab.j3d.control.ControlInputEvent;
import ab.j3d.model.Face3DIntersection;
import ab.j3d.view.ViewModelNode;

import com.numdata.oss.ui.BasicAction;
import com.numdata.oss.ui.WindowTools;

/**
 * ViewModelNodeContextAction class.
 *
 * If this action is called via an {@link ViewModelNodePopupMenu}, then the
 * {@link ControlInputEvent}, {@link ViewModelNode} and the {@link Face3DIntersection}
 * will be passed onto the run method.
 *
 * @author  Jark Reijerink
 * @version $Revision$ $Date$
 */
public abstract class ViewModelNodeContextAction
	extends BasicAction
{
	/**
	 * Construct basic action with settings from resource bundle using the
	 * specified key. The following properties are set:
	 * <table>
	 *  <tr><th>resource key  </th><th>assigned to property</th><th>default</th></tr>
	 *  <tr><td>{key}         </td><td>name                </td><td>{key}  </td></tr>
	 *  <tr><td>{key}Icon     </td><td>smallIcon           </td></tr>none  </td></tr>
	 *  <tr><td>{key}Tip      </td><td>shortDescription    </td></tr>none  </td></tr>
	 *  <tr><td>{key}KeyStroke</td><td>keyboardAccellerator</td></tr>none  </td></tr>
	 *  <tr><td>{key}Mnemonic </td><td>mnemonicKey         </td></tr>none  </td></tr>
	 * </table>
	 *
	 * @param   bundle      Resource bundle to get settings from.
	 * @param   key         Resource key to use (also used as action command).
	 */
	protected ViewModelNodeContextAction( final ResourceBundle bundle , final String key )
	{
		super( bundle , key );
	}

	/**
	 * {@inheritDoc}
	 * <br /><br />
	 * Checks if the topLevelComponent is a {@link ViewModelNodePopupMenu}, if so
	 * it passes the parameters to the run method.
	 */
	public void actionPerformed( final ActionEvent event )
	{
		try
		{
			Component topLevelComponent = (Component)event.getSource() ;
			while ( ( topLevelComponent != null ) && ( topLevelComponent.getParent() != null ) )
				topLevelComponent = topLevelComponent.getParent();
			if( topLevelComponent instanceof ViewModelNodePopupMenu )
			{
				final ViewModelNodePopupMenu viewModelNodePopupMenu = (ViewModelNodePopupMenu)topLevelComponent;
				run( viewModelNodePopupMenu.getControlInputEvent() , viewModelNodePopupMenu.getViewModelNode() , viewModelNodePopupMenu.getFace3DIntersection() );
			}
			else
			{
				run( null , null , null );
			}
		}
		catch ( Throwable problem )
		{
			WindowTools.showErrorDialog( null , problem , getClass() );
		}
	}

	public final void run()
	{
		run( null , null , null );
	}

	/**
	 * Run method for use with a {@link ControlInputEvent}.
	 * Parameters may be null.
	 *
	 * @param controlInputEvent      {@link ControlInputEvent} that was used, can be null.
	 * @param viewModelNode          {@link ViewModelNode} on which was clicked, can be null.
	 * @param face3DIntersection     {@link Face3DIntersection} that was found, can be null.
	 */
	protected abstract void run( final ControlInputEvent controlInputEvent , final ViewModelNode viewModelNode , Face3DIntersection face3DIntersection );
}

