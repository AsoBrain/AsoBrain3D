/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view.jogl;

import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelExample;

/**
 * Example program for the JOGL view model implementation.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JOGLModelExample
	extends ViewModelExample
{
	/**
	 * Construct new jPCTModelExample.
	 */
	public JOGLModelExample()
	{
		super( new JOGLModel( ViewModel.MM ) );
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new JOGLModelExample();
	}
}