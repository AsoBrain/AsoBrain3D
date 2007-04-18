/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Group;
import javax.media.j3d.Locale;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 * This class extends the <code>VirtualUniverse</code> class and adds extra
 * functionality to add multiple views and dynamic content.
 *
 * @see     VirtualUniverse
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public class Java3dUniverse
	extends VirtualUniverse
{

	/**
	 * The scene content is either a user-defined group node, or create using the
	 * internal <code>createStaticScene()</code> method.
	 */
	private final Group _content;

	/**
	 * Reference position within the universe. It defines the Virtual World
	 * coordinate system. This is where branch graphs are added.
	 */
	private final Locale _locale;

	/**
	 * Characteristics of a (physical) head in the universe.
	 * <p />
	 * <b>IMPORTANT: Created on-demand. Never access field, always use getter.</b>
	 *
	 * @see     #getPhysicalBody
	 */
	private PhysicalBody _physicalBody;

	/**
	 * Characteristics of the (physical) environment in which this universe is
	 * defined.
	 * <p />
	 * <b>IMPORTANT: Created on-demand. Never access field, always use getter.</b>
	 *
	 * @see     #getPhysicalEnvironment
	 */
	private PhysicalEnvironment _physicalEnvironment;

	/**
	 * Unit scale factor in this universe. This scale factor, when multiplied,
	 * converts design units to Java 3D units (meters).
	 */
	private final double _unit;

	/**
	 * Construct universe with the specified unit scale and no views.
	 * <p />
	 * A default scene graph is created with default lighting, background, etc.
	 * Applications can access (a part of) this scene graph using the
	 * <code>getContent()</code> method to add or remove content from the scene.
	 * <p />
	 * The unit scale factor, when multiplied, converts design units to Java 3D
	 * units (meters). A <code>TransformGroup</code> is created within the
	 * scene graph to perform the scaling.
	 *
	 * @param   unit            Unit scale factor (e.g. <code>MM</code>).
	 * @param   background      Background color to use for 3D views.
	 *
	 * @see     #createView
	 * @see     #getContent
	 */
	public Java3dUniverse( final double unit , final Color background )
	{
		_locale              = new Locale( this );
		_physicalBody        = null;
		_physicalEnvironment = null;
		_unit                = unit;
		_content             = createContent( background );
	}

	/**
	 * Construct universe with the specified content and unit scale, and no views.
	 * <p />
	 * The <code>BranchGroup</code> in the content graph must be added to the
	 * universe using <code>addBranchGroup<()/code> by the caller, since it is
	 * not necessarily the same as the specifiec content graph (the latter may
	 * be a descendant node).
	 * <p />
	 * The unit scale factor is stored as-is and is not used in any way by this
	 * constructor.
	 *
	 * @param   content     Content graph.
	 * @param   unit        Unit scale factor.
	 *
	 * @see     #addBranchGroup
	 */
	public Java3dUniverse( final Group content , final double unit )
	{
		_locale              = new Locale( this );
		_physicalBody        = null;
		_physicalEnvironment = null;
		_unit                = unit;
		_content             = content;
	}

	/**
	 * Add branch group to the universe. This is a convenience method that
	 * simply adds the specified group to this universe's locale.
	 *
	 * @param   bg      BranchGroup to attach to this Universe's Locale.
	 */
	public void addBranchGroup( final BranchGroup bg )
	{
		_locale.addBranchGraph( bg );
	}

	/**
	 * Create content graph to which applications can add their own content.
	 *
	 * @param   backgroundColor     Background to apply to content.
	 *
	 * @return  Content scene graph object.
	 *
	 * @see     Java3dTools#createDynamicScene
	 * @see     #getContent
	 * @see     #getUnit
	 */
	protected Group createContent( final Color backgroundColor )
	{
		final Group result;

		final BranchGroup scene = createContentScene( backgroundColor );

		final double unit = getUnit();
		if ( ( unit > 0.0 ) && ( unit != 1.0 ) )
		{
			final Transform3D xform = new Transform3D();
			xform.setScale( unit );

			result = new TransformGroup( xform );
			result.setCapability( TransformGroup.ALLOW_CHILDREN_READ );
			scene.addChild( result );
		}
		else
		{
			result = scene;
		}

		result.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );

		scene.compile();
		addBranchGroup( scene );

		return result;
	}

	/**
	 * Create view in this universe.
	 * <p />
	 * This adds the following view branch to the scene.
	 * <pre>
	 *     (Locale)
	 *         :
	 *   (BranchGroup)
	 *         |
	 * (TransformGroup)
	 *         |
	 *  (ViewPlatform) ---[View]--- [Canvas3D]
	 *                     |  |
	 *    [PhysicalBody] --'  `-- [PhysicalEnvironment]
	 * </pre>
	 * <table>
	 *   <tr><td><code>Locale</code>             </td><td>As returned by <code>getLocale()</code>.</td></tr>
	 *   <tr><td><code>BranchGroup</code>        </td><td>Created by this method.</td></tr>
	 *   <tr><td><code>TransformGroup</code>     </td><td>Supplied <code>transformGroup</code> argument.</td></tr>
	 *   <tr><td><code>ViewPlatform</code>       </td><td>Created by this method.</td></tr>
	 *   <tr><td><code>View</code>               </td><td>Created by this method; returned as method result.</td></tr>
	 *   <tr><td><code>Canvas3D</code>           </td><td>Supplied <code>canvas</code> argument.</td></tr>
	 *   <tr><td><code>PhysicalBody</code>       </td><td>As returned by <code>getPhysicalBody()</code>.</td></tr>
	 *   <tr><td><code>PhysicalEnvironment</code></td><td>As returned by <code>getPhysicalEnvironment()</code>.</td></tr>
	 * </table>
	 *
	 * @param   transformGroup  Defines the view's transform.
	 * @param   canvas          Canvas to render on.
	 *
	 * @return  View that was created.
	 *
	 * @see     Java3dTools#createCanvas3D
	 * @see     #getLocale
	 * @see     #getPhysicalBody
	 * @see     #getPhysicalEnvironment
	 */
	public View createView( final TransformGroup transformGroup , final Canvas3D canvas )
	{
		transformGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
		transformGroup.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );

		final ViewPlatform viewPlatform = new ViewPlatform();
		transformGroup.addChild( viewPlatform );

		final View view = new View();
		view.setPhysicalBody( getPhysicalBody() );
		view.setPhysicalEnvironment( getPhysicalEnvironment() );
		view.attachViewPlatform( viewPlatform );
		view.addCanvas3D( canvas );
		view.setDepthBufferFreezeTransparent( true );
		view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );
		view.setBackClipDistance( 100.0 );
		view.setFrontClipDistance( 0.1 );
		view.setSceneAntialiasingEnable( true );

		final BranchGroup branchGroup = new BranchGroup();
		branchGroup.setCapability( BranchGroup.ALLOW_DETACH );
		branchGroup.addChild( transformGroup );

		addBranchGroup( branchGroup );

		return view;
	}

	/**
	 * Called by <code>createContent()</code> to create a scene to add to the
	 * initial scene graph. Note that the unit scale factor is NOT applied to
	 * this scene (everything is in meters, which is the Java 3D default).
	 *
	 * @param   backgroundColor     Background to apply to content.
	 *
	 * @return  Scene graph object.
	 *
	 * @see     Java3dTools#createDynamicScene
	 * @see     #getContent
	 * @see     #getUnit
	 */
	protected BranchGroup createContentScene( final Color backgroundColor )
	{
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		// create static scene
		final BranchGroup scene = new BranchGroup();
		scene.setCapability( BranchGroup.ALLOW_CHILDREN_READ );

		// Set up the background
		if ( backgroundColor != null )
		{
			final Background background = new Background( new Color3f( backgroundColor ) );
			background.setApplicationBounds( bounds );
			scene.addChild( background );
		}

		// add scene to scene graph
		return scene;
	}

	/**
	 * Content scene graph object. Applications, may use the returned node to
	 * add content to the universe.
	 *
	 * @return  Content scene graph object.
	 */
	public Group getContent()
	{
		return _content;
	}

	/**
	 * Get reference position within the universe. It defines the Virtual World
	 * coordinate system. This is where branch graphs are added.
	 *
	 * @return  Locale object, which defined the position of this universe.
	 */
	public Locale getLocale()
	{
		return _locale;
	}

	/**
	 * Get characteristics of the (physical) environment in which this universe
	 * is defined.
	 *
	 * @return  PhysicalBody instance representing head properties.
	 *
	 * @see     #getPhysicalEnvironment
	 */
	public PhysicalBody getPhysicalBody()
	{
		PhysicalBody result = _physicalBody;
		if ( result == null )
		{
			result = new PhysicalBody();
			_physicalBody = result;
		}

		return result;
	}

	/**
	 * Get characteristics of a (physical) head in the universe.
	 *
	 * @return  PhysicalEnvironment instance representing environment properties.
	 *
	 * @see     #getPhysicalBody
	 */
	public PhysicalEnvironment getPhysicalEnvironment()
	{
		PhysicalEnvironment result = _physicalEnvironment;
		if ( result == null )
		{
			result = new PhysicalEnvironment();
			_physicalEnvironment = result;
		}

		return result;
	}

	/**
	 * Unit scale factor in this universe. This scale factor, when multiplied,
	 * converts design units to Java 3D units (meters).
	 *
	 * @return  Unit scale factor in this universe.
	 */
	public double getUnit()
	{
		return _unit;
	}
}
