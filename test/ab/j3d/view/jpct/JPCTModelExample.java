/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2006-2006 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jpct;

import ab.j3d.view.jpct.JPCTModel;
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
		super( new JPCTModel( ViewModel.MM ) );
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
