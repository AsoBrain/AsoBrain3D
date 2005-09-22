/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2005
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
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import ab.j3d.Matrix3D;
import ab.j3d.view.DragSupport;
import ab.j3d.view.ViewControl;
import ab.j3d.view.ViewModelView;
import ab.j3d.view.SelectionSupport;
import ab.j3d.view.NodeSelectionListener;

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
	 * Universe for which this view is defined.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Transform group in view branch.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private TransformGroup _tg;

	/**
	 * The <code>View</code> object is what ties all things together that are
	 * needed to create a rendering of the scene.
	 * <p />
	 * Note that the <code>View</code> object is actually outside the scene
	 * graph; it attaches to a <code>ViewPlatform</code> in the graph.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private final View _view;

	/**
	 * Canvas with on-screen representation of this view.
	 *
	 * @see     Java3dUniverse#createView
	 */
	private Canvas3D _canvas;

	/**
	 * Cached <code>Matrix3d</code> instance (used by <code>update()</code).
	 */
	private Matrix3d _rotation = new Matrix3d();

	/**
	 * Cached <code>Vector3d</code> instance (used by <code>update()</code).
	 */
	private Vector3d _translation = new Vector3d();

	/**
	 * Cached <code>Matrix4d</code> instance (used by <code>update()</code).
	 */
	private Transform3D _transform3d = new Transform3D();

	/**
	 * The JAva3D Selection support for this view.
	 * {@link NodeSelectionListener}s can register themselves to receive events
	 * when a node is selected
	 */
	private Java3dSelectionSupport _selectionSupport;

	/**
	 * Construct view node using Java3D for rendering.
	 *
	 * @param   universe        Java3D universe for which the view is created.
	 * @param   id              Application-assigned ID of this view.
	 * @param   viewControl     Control to use for this view.
	 *
	 * @see     Java3dUniverse#createView
	 */
	Java3dView( final Java3dUniverse universe , final Object id , final ViewControl viewControl )
	{
		super( id , viewControl );

		/*
		 * Create view branch.
		 */
		final TransformGroup tg     = new TransformGroup();
		final Canvas3D       canvas = Java3dTools.createCanvas3D();
		final View           view   = universe.createView( tg , canvas );

		_universe = universe;
		_tg       = tg;
		_canvas   = canvas;
		_view     = view;

		/*
		 * Update view to initial transform.
		 */
		update();

		_selectionSupport = new Java3dSelectionSupport( _canvas , _universe);

		/*
		 * Add DragSupport to handle drag events.
		 */
		final DragSupport ds = new DragSupport( _canvas , universe.getUnit() );
		ds.addDragListener( viewControl );
	}

	/**
	 * Returns the {@link SelectionSupport} for this view.
	 * @return The {@link SelectionSupport} for this view.
	 */
	public SelectionSupport getSelectionSupport()
	{
		return _selectionSupport;
	}

	public Component getComponent()
	{
		return _canvas;
	}

	public void setProjectionPolicy( final int policy )
	{
		final View view = _view;

		switch ( policy )
		{
			case PERSPECTIVE :
				view.setProjectionPolicy( View.PERSPECTIVE_PROJECTION );
				break;

			case ISOMETRIC :
			case PARALLEL :
				view.setProjectionPolicy( View.PARALLEL_PROJECTION );
				break;

			default :
				throw new IllegalArgumentException( "Invalid projection policy: " + policy );
		}
	}

	public void update()
	{
		/*
		 * Get the view-transform from the view control.
		 */
		final ViewControl control       = getViewControl();
		final Matrix3D    viewTransform = control.getTransform();

		/*
		 * Determine rotation and translation. If a unit is set, use it to
		 * scale the translation.
		 */
		final Java3dUniverse universe = _universe;
		final double         unit     = universe.getUnit();

		double xo = viewTransform.xo;
		double yo = viewTransform.yo;
		double zo = viewTransform.zo;

		if ( ( unit > 0 ) && ( unit != 1 ) )
		{
			xo *= unit;
			yo *= unit;
			zo *= unit;
		}

		/*
		 * Finally, set the 'TransformGroup' transform to the INVERSE matrix.
		 *
		 * Reuse 'Matrix3d', 'Vector3d', and 'Transform3D' objects here to
		 * prevent creation of too many temporary objects
		 * (sorry for the bad readability).
		 */
		final Matrix3d       rotation    = _rotation;
		final Vector3d       translation = _translation;
		final Transform3D    transform3d = _transform3d;
		final TransformGroup tg          = _tg;

		translation.x = - xo * ( rotation.m00 = viewTransform.xx )
		                - yo * ( rotation.m01 = viewTransform.yx )
		                - zo * ( rotation.m02 = viewTransform.zx );

		translation.y = - xo * ( rotation.m10 = viewTransform.xy )
		                - yo * ( rotation.m11 = viewTransform.yy )
		                - zo * ( rotation.m12 = viewTransform.zy );

		translation.z = - xo * ( rotation.m20 = viewTransform.xz )
		                - yo * ( rotation.m21 = viewTransform.yz )
		                - zo * ( rotation.m22 = viewTransform.zz );

		transform3d.set( rotation , translation , 1.0 );
		tg.setTransform( transform3d );
	}

	public void setRenderingPolicy( final int policy )
	{
		/* @FIXME how can we implement such a feature? I think this really requires different geometry! Maybe something with 'alternate appearance' helps a little? */
	}
}
