/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2007-2007 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package ab.j3d.view;

import ab.j3d.Matrix3D;
import ab.j3d.model.Light3D;

/**
 * Provides utility methods to simplify the use of a {@link ViewModel}.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public final class ViewModelTools
{
	/**
	 * No instances should be constructed.
	 */
	private ViewModelTools()
	{
	}

	/**
	 * Adds appropriate 'legacy lights' to the given view model. Lights will be
	 * created to mimic the behavior of the Java3D view before April 2007, when
	 * lights were still hard-coded into the view. The added lights are given
	 * IDs starting with "legacy-light".
	 *
	 * @param   model   Model to add legacy lights to.
	 *
	 * @throws  NullPointerException if <code>model</code> is <code>null</code>.
	 */
	public static void addLegacyLights( final ViewModel model )
	{
		if ( model == null )
			throw new NullPointerException( "model" );

		model.createNode( "legacy-ambient-1" , Matrix3D.INIT , new Light3D( 255 , -1.0 ) , null , 1.0f );

		/* A distance of over 100000 units from the origin should be sufficient to mimic a directional light. */
		model.createNode( "legacy-light-1" , Matrix3D.INIT.plus(  60000.0 ,  100000.0 ,  100000.0 ) , new Light3D( 255 , 300000.0 ) , null , 1.0f );
		model.createNode( "legacy-light-2" , Matrix3D.INIT.plus( -60000.0 , -100000.0 , - 20000.0 ) , new Light3D( 230 , 300000.0 ) , null , 1.0f );
		model.createNode( "legacy-light-3" , Matrix3D.INIT.plus( 100000.0 ,  -50000.0 , -100000.0 ) , new Light3D( 208 , 300000.0 ) , null , 1.0f );
	}
}
