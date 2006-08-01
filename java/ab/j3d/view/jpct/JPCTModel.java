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

import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * View model implementation for jPCT.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class JPCTModel
	extends ViewModel
{
	/**
	 * Construct new jPCTModel.
	 */
	public JPCTModel( final double unit )
	{
		super( unit );
	}

	protected void initializeNode( final ViewModelNode node )
	{
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
	}

	public ViewModelView createView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final JPCTView view = new JPCTView( this , id );
		addView( view );
		return view;
	}
}
