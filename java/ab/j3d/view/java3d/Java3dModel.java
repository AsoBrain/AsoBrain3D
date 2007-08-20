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
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Light;
import javax.media.j3d.PointLight;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.model.Light3D;
import ab.j3d.model.Node3D;
import ab.j3d.model.Node3DCollection;
import ab.j3d.model.Object3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelNode;
import ab.j3d.view.ViewModelView;

/**
 * View model implementation for Java 3D.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class Java3dModel
	extends ViewModel
{
	/**
	 * Java 3D universe containing scene.
	 */
	private final Java3dUniverse _universe;

	/**
	 * Content branch graph of the Java 3D scene.
	 */
	private final BranchGroup _contentGraph;

	/**
	 * Map node ID ({@link Object}) to Java 3D content graph object
	 * ({@link BranchGroup}).
	 */
	private final Map<Object,BranchGroup> _nodeContentMap = new HashMap();

	/**
	 * Construct new Java 3D model using {@link ViewModel#MM} units
	 * and a dark blueish background.
	 */
	public Java3dModel()
	{
		this( MM , new Color( 51 , 77 , 102 ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   unit        Unit scale factor (e.g. {@link ViewModel#MM}).
	 * @param   background  Background color to use for 3D views.
	 */
	public Java3dModel( final double unit , final Color background )
	{
		this( new Java3dUniverse( unit , background ) );
	}

	/**
	 * Construct new Java 3D model.
	 *
	 * @param   j3dUniverse Java 3D universe.
	 */
	public Java3dModel( final Java3dUniverse j3dUniverse )
	{
		super( j3dUniverse.getUnit() );

		_universe     = j3dUniverse;
		_contentGraph = Java3dTools.createDynamicScene( _universe.getContent() );
	}

	/**
	 * Setup the Java 3D side of a view node.
	 * <p />
	 * This adds the following part to the Java 3D content graph:
	 * <pre>
	 *    (G)  {@link Java3dUniverse#getContent()}
	 *     |
	 *    (BG) {@link #_contentGraph}
	 *     |
	 *    <b>(BG) <code>nodeRoot</code> (added to {@link #_nodeContentMap} with <code>id</code> as key)
	 *     |
	 *    (TG) <code>nodeTransform</code>
	 *     |
	 *    (BG) content branch group (re-created on updates)</b>
	 * </pre>
	 *
	 * @param   node    View node being set up.
	 *
	 * @throws  NullPointerException if <code>id</code> is <code>null</code>.
	 */
	protected void initializeNode( final ViewModelNode node )
	{
		final Object id = node.getID();

		final BranchGroup nodeRoot = new BranchGroup();
		nodeRoot.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		nodeRoot.setCapability( BranchGroup.ALLOW_DETACH );
		_nodeContentMap.put( id , nodeRoot );

		final TransformGroup nodeTransform = new TransformGroup();
		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( node.getTransform() ) );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_READ   );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_WRITE );
		nodeTransform.setCapability( TransformGroup.ALLOW_CHILDREN_EXTEND );
		nodeTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		nodeRoot.addChild( nodeTransform );

		_contentGraph.addChild( nodeRoot );
	}

	protected void updateNodeTransform( final ViewModelNode node )
	{
		final Object         id            = node.getID();
		final Matrix3D       transform     = node.getTransform();
		final TransformGroup nodeTransform = getJava3dTransform( id );

		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );
	}

	protected void updateNodeContent( final ViewModelNode node )
	{
		final Object         id               = node.getID();
		final TransformGroup nodeTransform    = getJava3dTransform( id );
		final Matrix3D       transform        = node.getTransform();
		final Node3D         node3D           = node.getNode3D();
		final Material       materialOverride = node.getMaterialOverride();
		final float          opacity          = node.getOpacity();

		final Node3DCollection<Object3D> objects = node3D.collectNodes( null , Object3D.class , Matrix3D.INIT , false );
		final BranchGroup bg = Shape3DBuilder.createBranchGroup( objects , materialOverride , opacity );

		final Node3DCollection<Light3D> lights = node3D.collectNodes( null , Light3D.class , Matrix3D.INIT , false );
		addLights( bg , lights , transform );

		/*
		 * Attach content to scene graph (replace existing branch group).
		 */
		nodeTransform.setTransform( Java3dTools.convertMatrix3DToTransform3D( transform ) );

		if ( nodeTransform.numChildren() == 0 )
			nodeTransform.addChild( bg );
		else
			nodeTransform.setChild( bg , 0 );
	}

	private static void addLights( final BranchGroup bg , final Node3DCollection<Light3D> lights , final Matrix3D transform )
	{
		if ( ( lights != null ) && ( lights.size() > 0 ) )
		{
			final BoundingSphere worldBounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 10000.0 ); // @TODO How to determine the 'correct' world bounds.

			for ( int i = 0 ; i < lights.size() ; i++ )
			{
				final Light3D modelLight = lights.getNode( i );
				final float   fallOff    = (float)modelLight.getFallOff();
				final float   intensity  = (float)modelLight.getIntensity() / 255.0f;

				final Light viewLight;
				if ( fallOff < 0.0f )
				{
					viewLight = new AmbientLight();
					viewLight.setInfluencingBounds( worldBounds );
				}
				else
				{
					final PointLight pointLight = new PointLight();
					if ( fallOff > 0.0f )
					{
						pointLight.setInfluencingBounds( new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , (double)fallOff * 10.0 ) );
						pointLight.setAttenuation( 1.0f , 0.0f , 0.1f / ( fallOff * fallOff ) );
					}
					else if ( fallOff == 0.0f )
					{
						pointLight.setInfluencingBounds( worldBounds );
					}
					pointLight.setPosition( (float)transform.xo , (float)transform.yo , (float)transform.zo );

					viewLight = pointLight;
				}
				viewLight.setColor( new Color3f( intensity , intensity , intensity ) );

				bg.addChild( viewLight );
			}
		}
	}

	public void removeNode( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		if ( nodeRoot != null )
			_contentGraph.removeChild( nodeRoot );

		super.removeNode( id );
	}

	public BranchGroup getJava3dNode( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		return _nodeContentMap.get( id );
	}

	public TransformGroup getJava3dTransform( final Object id )
	{
		final BranchGroup nodeRoot = getJava3dNode( id );
		return (TransformGroup)nodeRoot.getChild( 0 );
	}

	public ViewModelView createView( final Object id )
	{
		if ( id == null )
			throw new NullPointerException( "id" );

		final Java3dView view = new Java3dView( this , _universe , id );
		addView( view );
		return view;
	}

	/**
	 * Get Java 3D universe containing scene.
	 *
	 * @return  Java 3D universe containing scene.
	 */
	public Java3dUniverse getUniverse()
	{
		return _universe;
	}
}
