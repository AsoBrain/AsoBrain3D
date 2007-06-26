/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2006 Numdata BV
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
package ab.j3d.view.java3d;

import java.awt.Color;

import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelExample;

/**
 * Example program for the Java 3D view model implementation.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Java3dModelExample
	extends ViewModelExample
{
	/**
	 * Construct application.
	 */
	private Java3dModelExample()
	{
		super( new Java3dModel( ViewModel.MM , Color.BLACK ) );
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new Java3dModelExample();
	}

}