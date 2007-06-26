/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2007
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
package ab.j3d.view.jpct;

import java.awt.Color;

import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelExample;

/**
 * Example program for the jPCT view model implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTModelExample
	extends ViewModelExample
{
	/**
	 * Construct new jPCTModelExample.
	 */
	public JPCTModelExample()
	{
		super( new JPCTModel( ViewModel.MM, Color.BLACK ) );
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new JPCTModelExample();
	}
}
