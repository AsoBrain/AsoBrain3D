/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 2004-2004 Numdata BV
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
package ab.j3d.view.java2d;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Iterator;

import ab.j3d.Matrix3D;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModelView;

/**
 * Java 2D implementation of view model view.
 *
 * @see     Java2dModel
 * @see     ViewModelView
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java2dView
	extends ViewModelView
{
	/**
	 * Model for which this view was created.
	 */
	private final Java2dModel _model;

	/**
	 * Panel to draw on.
	 */
	private final Canvas _canvas;

	/**
	 * Construct new view.
	 *
	 * @param   model           Model for which this view is created.
	 * @param   id              Application-assigned ID of this view.
	 * @param   viewControl     Control to use for this view.
	 */
	Java2dView( final Java2dModel model , final Object id , final ViewControl viewControl )
	{
		super( id , viewControl );
		_model = model;
		_canvas = new Canvas()
		{
			/**
			 * Paint contents of model on canvas.
			 *
			 * @param   g   Graphics context.
			 */
			public void paint( final Graphics g )
			{
				super.paint( g );

				final Matrix3D gXform        = getViewTransform();
				final Matrix3D viewTransform = Matrix3D.INIT; //@FIXME How to get this info??
				final Color    outlineColor  = Color.black;
				final Color    fillColor     = Color.white;
				final float    shadeFactor   = 0.5f;

				for ( Iterator i = _model.getPaintQueue() ; i.hasNext() ; )
				{
					final Object o = i.next();
					if ( o instanceof Object3D )
					{
						((Object3D)o).paint( g , gXform , viewTransform , outlineColor , fillColor , shadeFactor );
					}
				}
			}
		};
	}

	public Component getComponent()
	{
		return _canvas;
	}

	public void update()
	{
		_canvas.repaint();
	}
}
