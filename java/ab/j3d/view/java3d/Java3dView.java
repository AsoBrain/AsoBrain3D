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
package ab.j3d.view.java3d;

import java.awt.Component;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;

import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Matrix3D;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModelView;

/**
 * Java 3D implementation of view model view.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dView
	extends ViewModelView
{
	/**
	 * Perspective projection policy constant.
	 *
	 * @see     #setProjectionPolicy
	 */
	private static final int PERSPECTIVE = 0;

	/**
	 * Parallel projection policy constant.
	 *
	 * @see     #setProjectionPolicy
	 */
	private static final int PARALLEL    = 1;

	/**
	 * Viewer with all Java 3D related information about this view.
	 */
	private final Viewer _viewer;

	/**
	 * Universe for which this view is defined.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Construct Java3D view.
	 *
	 * @param   universe        Java3D universe for which the view is created.
	 * @param   id              Application-assigned ID of this view.
	 * @param   viewControl     Control to use for this view.
	 */
	Java3dView( final Java3dUniverse universe , final Object id , final ViewControl viewControl )
	{
		super( id , viewControl );

		final Canvas3D canvas3d = Java3dTools.createCanvas3D();

		final ViewingPlatform viewingPlatform = new ViewingPlatform( 1 );
		viewingPlatform.setUniverse( universe );
		viewingPlatform.setViewPlatformBehavior( Java3dTools.createOrbitBehavior( canvas3d ) );

		final Viewer viewer = new Viewer( canvas3d );
		viewer.setViewingPlatform( viewingPlatform );

		_viewer = viewer;

		// set transparency stuff
		final View view = viewer.getView();
		view.setDepthBufferFreezeTransparent( true );
		view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );

		// view.setBackClipDistance( 100 );
		setProjectionPolicy( PERSPECTIVE );

		// place view in universe
		_universe = universe;
		_universe.addViewer( viewer );
		_universe.getLocale().addBranchGraph( viewingPlatform );
	}

	/**
	 * Get canvas with on-screen representation of this view.
	 *
	 * @return  Canvas with on-screen representation of this view.
	 */
	private Canvas3D getCanvas3D()
	{
		return _viewer.getCanvas3D();
	}

	/**
	 * Get transform group for this view.
	 *
	 * @return  <code>TransformGroup</code> for this view.
	 */
	private TransformGroup getTransformGroup()
	{
		return _viewer.getViewingPlatform().getMultiTransformGroup().getTransformGroup( 0 );
	}

	/**
	 * Get Java3D scene graph object.
	 *
	 * @return  Java3D scene graph object.
	 */
	public Group getSceneGraphObject()
	{
		return _viewer.getViewingPlatform();
	}

	public Component getComponent()
	{
		return getCanvas3D();
	}

	public void update()
	{
		Matrix3D xform = getViewControl().getTransform();

		final float unit = _universe.getUnit();
		if ( ( unit > 0 ) && ( unit != 1 ) )
			xform = xform.setTranslation( xform.xo * unit , xform.yo * unit , xform.zo * unit );

		getTransformGroup().setTransform( Java3dTools.convertMatrix3DToTransform3D( xform ) );
	}

	/**
	 * Set projection policy of this view. The policy can be either
	 * <code>PERSPECTIVE</code> or <code>PARALLEL</code>.
	 *
	 * @param   policy      Projection policy of this view
	 *                      (<code>PERSPECTIVE</code> or <code>PARALLEL</code>).
	 */
	public void setProjectionPolicy( final int policy )
	{
		switch ( policy )
		{
			case PERSPECTIVE :
				_viewer.getView().setProjectionPolicy( View.PERSPECTIVE_PROJECTION );
				break;

			case PARALLEL :
				_viewer.getView().setProjectionPolicy( View.PARALLEL_PROJECTION );
				break;

			default :
				throw new IllegalArgumentException( "Invalid projection policy: " + policy );
		}

	}
}
