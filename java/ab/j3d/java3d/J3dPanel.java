/* ====================================================================
 * $Id$
 * ====================================================================
 * Numdata Open Source Software License, Version 1.0
 *
 * Copyright (c) 2003-2004 Numdata BV.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package ab.j3d.java3d;

import java.awt.Image;
import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * This panel can be used to display 3D models using Java 3D. It allows content
 * to be replaced while the panel is visible. Convenience methods have been
 * created to simplify viewing SODA project data.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class J3dPanel
    extends Canvas3D
{
	/**
	 * Java3D scene.
	 */
	private final BranchGroup _j3dScene;

	/**
	 * Construct Java 3D panel.
	 */
	public J3dPanel()
	{
		super( SimpleUniverse.getPreferredConfiguration() );

		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		// create universe
		final SimpleUniverse  universe     = new SimpleUniverse( this );
		final ViewingPlatform viewPlatform = universe.getViewingPlatform();
		final View            view         = universe.getViewer().getView();
		final Vector3f        viewPoint    = new Vector3f( 0.0f , 0.0f , 5.0f );

		// set camera location/direction in ViewingPlatform
		final Transform3D viewTransform = new Transform3D();
		viewTransform.set( viewPoint );
		viewPlatform.getViewPlatformTransform().setTransform( viewTransform );

		// add orbit behavior to ViewingPlatform
		final OrbitBehavior orbit = new OrbitBehavior( this , OrbitBehavior.REVERSE_ALL | OrbitBehavior.STOP_ZOOM );
		orbit.setSchedulingBounds( bounds );
		viewPlatform.setViewPlatformBehavior( orbit );

		// create static scene
		final BranchGroup staticScene = new BranchGroup();

		// Set up the background
		final Background background = new Background( new Color3f( 0.8f , 0.8f , 0.8f ) );
//		final Background background = new Background( new Color3f( 0.2f , 0.3f , 0.4f ) );
//		Background background = new Background( new Color3f( 0.3f , 0.4f , 0.6f ) );
		background.setApplicationBounds( bounds );
		staticScene.addChild( background );

		// Set up the ambient light
		final Color3f ambientColor = new Color3f( 0.8f , 0.8f , 0.8f );
		final AmbientLight ambientLightNode = new AmbientLight( ambientColor );
		ambientLightNode.setInfluencingBounds( bounds );
		staticScene.addChild( ambientLightNode );

		// Set up the directional lights
		final Color3f  light1Color     = new Color3f (  1.0f ,  0.9f ,  0.8f );
		final Vector3f light1Direction = new Vector3f(  0.6f ,  1.0f ,  1.0f );
		final DirectionalLight light1 = new DirectionalLight( light1Color , light1Direction );
		light1.setInfluencingBounds( bounds );
//		scene.addChild( light1 );

		final Color3f  light2Color     = new Color3f (  1.0f ,  0.9f ,  0.8f );
		final Vector3f light2Direction = new Vector3f( -0.6f , -1.0f , -0.2f );
		final DirectionalLight light2 = new DirectionalLight( light2Color , light2Direction );
		light2.setInfluencingBounds( bounds );
		staticScene.addChild( light2 );

		final Color3f  light3Color     = new Color3f (  0.8f ,  0.7f , 0.6f );
		final Vector3f light3Direction = new Vector3f(  1.0f , -0.5f , -1.0f );
		final DirectionalLight light3 = new DirectionalLight( light3Color , light3Direction );
		light3.setInfluencingBounds( bounds );
		staticScene.addChild( light3 );

//		staticScene.addChild( J3dTools.createGrid( new Point3f( 0.0f , 2f , 0.0f ) , new Point3i( 30 , 4 , 30 ) , 0.5f , 5 , J3dTools.YELLOW ) );
//		staticScene.addChild( CabinetJava3D.createGrid( new Point3f( 0.0f , 1.5f , 0.0f ) , new Point3i( 20 , 6 , 20 ) , 0.25f , 4 , CabinetJava3D.BLACK ) );

		// set transparency stuff
		view.setDepthBufferFreezeTransparent( true );
		view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );
//		view.setBackClipDistance( 100 );

		// create sub-tree for dynamic content
		final BranchGroup dynamicScene = new BranchGroup();
//		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_WRITE );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );
		staticScene.addChild( dynamicScene );

		// add scene to universe
		staticScene.compile();
		universe.addBranchGraph( staticScene );

		_j3dScene = dynamicScene;
	}

	/**
	 * Clear content of panel.
	 */
	public void clearContent()
	{
		_j3dScene.removeAllChildren();
	}

	/**
	 * Set content of panel to the specified <code>BranchGroup</code>. The
	 * <code>ALLOW_DETACH</code> cabaility is set to allow the content to
	 * be removed. After that, the branch group is compiled and added as
	 * dynamic content.
	 *
	 * @param   bg          BranchGroup to set as content.
	 */
	public void setContent( final BranchGroup bg )
	{
		bg.setCapability( BranchGroup.ALLOW_DETACH );
		bg.compile();

		clearContent();
		_j3dScene.addChild( bg );
	}

	/**
	 * Set content of panel to a spinning image box.
	 */
	public void setSpinningImageBoxContent( final Image image )
	{
		final BranchGroup bg = new BranchGroup();

		final TransformGroup boxTransform = new TransformGroup();
		boxTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		bg.addChild( boxTransform );

		final RotationInterpolator spinner = new RotationInterpolator(
		        /* alpha          */ new Alpha( -1 , Alpha.INCREASING_ENABLE , 0 , 0 , 8000 , 0 , 0 , 0 , 0 , 0 ) ,
		        /* transformgroup */ boxTransform ,
		        /* axis           */ new Transform3D() ,
		        /* startValue     */ (float) Math.PI * 2.0f ,
		        /* endValue       */ 0.0f );
		spinner.setSchedulingBounds( new BoundingSphere( new Point3d( 0 , 0 , 0 ) , 100 ) );
		boxTransform.addChild( spinner );

		final Appearance appearance = new Appearance();
		appearance.setTexture( new TextureLoader( image , this ).getTexture() );
		boxTransform.addChild( new Box( 0.15f , 0.15f , 0.15f , Box.GENERATE_TEXTURE_COORDS , appearance ) );

		setContent( bg );
	}
}
