/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2004 Peter S. Heijnen
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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.image.TextureLoader;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;

/**
 * Utility methods for Java 3D support.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Java3dTools
{
	/**
	 * Texture observer needed by <code>TextureLoader</code> to load textures.
	 */
	private static final Canvas TEXTURE_OBSERVER = new Canvas();

	/**
	 * Map used to cache textures. Maps texture code (<code>String</code>) to
	 * texture (<code>Texture</code>).
	 */
	private final Map _textureCache = new HashMap();

	/**
	 * Singleton <code>Java3dTools</code> instance.
	 */
	private static Java3dTools _singleton;

	/**
	 * Construct <code>Java3dTools</code> for centralized texture caching, etc.
	 */
	private Java3dTools()
	{
	}

	/**
	 * Get singleton instance.
	 *
	 * @return  Singleton <code>Java3dTools</code> instance.
	 */
	public static Java3dTools getInstance()
	{
		Java3dTools result = _singleton;
		if ( result == null )
		{
			result = new Java3dTools();
			_singleton = result;
		}
		return result;
	}

	/**
	 * Convert <code>Matrix3D<code/> to Java3D <code>Transform3D</code> object.
	 *
	 * @param   matrix  Matrix3D to convert.
	 *
	 * @return  <code>Transform3D</code> instance.
	 */
	public static Transform3D convertMatrix3DToTransform3D( final Matrix3D matrix )
	{
		return new Transform3D( new Matrix4d(
			matrix.xx , matrix.xy , matrix.xz , matrix.xo ,
			matrix.yx , matrix.yy , matrix.yz , matrix.yo ,
			matrix.zx , matrix.zy , matrix.zz , matrix.zo ,
			0.0       , 0.0       , 0.0       , 1.0 ) );
	}

	/**
	 * Create StarTrek&tm; Holodeck style unit.
	 *
	 * @param   origin      Grid origin.
	 * @param   size        Grid size in unit unit.
	 * @param   unit        Grid unit size.
	 * @param   interval    Thick line interval (use thick appearance for each n'th line).
	 * @param   color       Grid color.
	 *
	 * @return  Group containing unit shape.
	 */
	public static Group createGrid( final Tuple3f origin , final Tuple3i size , final float unit , final int interval , final Color3f color )
	{
		final Vector3f min     = new Vector3f( origin.x - (float)size.x * unit , origin.y - (float)size.y * unit , origin.z - (float)size.z * unit );
		final Vector3f max     = new Vector3f( origin.x + (float)size.x * unit , origin.y + (float)size.y * unit , origin.z + (float)size.z * unit );
		final int      maxSize = Math.max( Math.max( size.x , size.y ) , size.z );

		final PolygonAttributes polygonAttributes = new PolygonAttributes();
		polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_LINE );
		polygonAttributes.setCullFace( PolygonAttributes.CULL_NONE );

		final ColoringAttributes coloringAttributes = new ColoringAttributes();
		coloringAttributes.setColor( color );
		coloringAttributes.setShadeModel( ColoringAttributes.FASTEST );

		final List thickCoords = new ArrayList();
		final List thinCoords  = new ArrayList();

		for ( int gridIndex = maxSize ; gridIndex >= 0 ; gridIndex-- )
		{
			final List coords = ( ( interval > 0 ) && ( ( gridIndex % interval ) == 0 ) ) ? thickCoords : thinCoords;

			for ( int mult = ( gridIndex == 0 ) ? 1 : -1 ; mult <= 1 ; mult += 2 )
			{
				if ( gridIndex <= size.x )
				{
					final float x = origin.x + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( x , min.y , min.z ) );
					coords.add( new Point3f( x , max.y , min.z ) );
					coords.add( new Point3f( x , max.y , max.z ) );
					coords.add( new Point3f( x , min.y , max.z ) );
				}

				if ( gridIndex <= size.y )
				{
					final float y = origin.y + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( min.x , y , min.z ) );
					coords.add( new Point3f( max.x , y , min.z ) );
					coords.add( new Point3f( max.x , y , max.z ) );
					coords.add( new Point3f( min.x , y , max.z ) );
				}

				if ( gridIndex <= size.z )
				{
					final float z = origin.z + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( min.x , min.y , z ) );
					coords.add( new Point3f( max.x , min.y , z ) );
					coords.add( new Point3f( max.x , max.y , z ) );
					coords.add( new Point3f( min.x , max.y , z ) );
				}
			}
		}

		final Group group = new Group();
		for ( int i = 0 ; i < 2 ; i++ )
		{
			final List vCoords = ( i == 0 ) ? thinCoords : thickCoords;
			if ( vCoords.isEmpty() )
				continue;

			final LineAttributes lineAttributes = new LineAttributes();
			lineAttributes.setLineWidth( ( i == 0 ) ? 1.0f : 3.0f );

			final Appearance appearance = new Appearance();
			appearance.setLineAttributes( lineAttributes );
			appearance.setPolygonAttributes( polygonAttributes );
			appearance.setColoringAttributes( coloringAttributes );

			final QuadArray quadArray = new QuadArray( vCoords.size() , LineStripArray.COORDINATES );
			final Point3f[] pCoords = (Point3f[])vCoords.toArray( new Point3f[ vCoords.size() ] );
			quadArray.setCoordinates( 0 , pCoords );

			group.addChild( new Shape3D( quadArray , appearance ) );
		}
		return group;
	}

	/**
	 * Conveniene method to create a <code>Canvas3D</code> with default
	 * configuration settings.
	 *
	 * @return  Canvas that wascreated.
	 */
	public static Canvas3D createCanvas3D()
	{
		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice device = environment.getDefaultScreenDevice();

		return new Canvas3D( device.getBestConfiguration( new GraphicsConfigTemplate3D() ) )
			{
				/*
				 * Override <code>getMinimumSize()</code> to allow layout manager to
				 * do its job. Otherwise, this will always return the current size of
				 * the canvas, not allowing it to be reduced in size.
				 */
				public Dimension getMinimumSize()
				{
					return new Dimension( 10 , 10 );
				}
			};
	}

	/**
	 * Create a dynamic branch group in the scene graph and add it to the
	 * specified parent node. The dynamic branch group allows child nodes to be
	 * added/removed, etc. While the scene graph is displayed.
	 *
	 * @param   parent      Parent node to add dynamic branch group to.
	 *
	 * @return  Branch group with create dynamic scene (added to static scene).
	 *
	 * @see     #addDynamicContent
	 * @see     #clearDynamicContent
	 */
	public static BranchGroup createDynamicScene( final Group parent )
	{
		final BranchGroup dynamicScene = new BranchGroup();
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_WRITE );
		dynamicScene.setCapability( BranchGroup.ALLOW_CHILDREN_EXTEND );
		parent.addChild( dynamicScene );
		return dynamicScene;
	}

	/**
	 * Add content defined by a <code>BranchGroup</code> to the specified dynamic
	 * scene graph. The <code>ALLOW_DETACH</code> capability of the added content
	 * is set, to allow it to be removed later.
	 *
	 * @param   dynamicScene    Dynamic scene graph root node.
	 * @param   content         Content to set in the dynamic scene.
	 *
	 * @see     #createDynamicScene
	 * @see     #clearDynamicContent
	 */
	public static void addDynamicContent( final Group dynamicScene , final BranchGroup content )
	{
		content.setCapability( BranchGroup.ALLOW_DETACH );
		content.compile();
		dynamicScene.addChild( content );
	}

	/**
	 * Clear content of dynamic scene graph.
	 *
	 * @param   dynamicScene    Dynamic scene graph root node.
	 *
	 * @see     #createDynamicScene
	 * @see     #addDynamicContent
	 */
	public static void clearDynamicContent( final BranchGroup dynamicScene )
	{
		dynamicScene.removeAllChildren();
	}

	/**
	 * Get Java3D <code>Appearance</code> for the specified <code>TextureSpec</code>.
	 *
	 * @param   textureSpec     TextureSpec to get the Appearance for.
	 * @param   opacity         Opacity to apply to the returned appearance.
	 *
	 * @return  Appearance for the specified texture spec.
	 */
	public Appearance getAppearance( final TextureSpec textureSpec , final float opacity )
	{
		final int   rgb = textureSpec.getARGB();
		final float r   = (float)( ( rgb >> 16 ) & 255 ) / 255.0f;
		final float g   = (float)( ( rgb >>  8 ) & 255 ) / 255.0f;
		final float b   = (float)(   rgb         & 255 ) / 255.0f;
		final float ar  = 1.9f * textureSpec.ambientReflectivity;
		final float dr  = 0.9f * textureSpec.diffuseReflectivity;
		final float sr  = 0.5f * textureSpec.specularReflectivity;

		final Material material = new Material();
		material.setLightingEnable( true );
		material.setAmbientColor  ( ar * r , ar * g , ar * b );
		material.setEmissiveColor ( new Color3f( 0.0f , 0.0f , 0.0f ) );
		material.setDiffuseColor  ( dr * r , dr * g , dr * b );
		material.setSpecularColor ( new Color3f( sr , sr , sr ) );
		material.setShininess     ( textureSpec.specularReflectivity * (float)textureSpec.specularExponent );

		final Appearance appearance = new Appearance();
		appearance.setCapability( Appearance.ALLOW_TEXTURE_READ );
		appearance.setMaterial( material );

		if ( textureSpec.isTexture() )
		{
			final Texture texture = getTexture( textureSpec );
			appearance.setTexture( texture );

			final TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode( TextureAttributes.MODULATE );
//			final Transform3D t = new Transform3D();
//			t.setScale( spec.textureScale );
//			textureAttributes.setTextureTransform( t  );
			appearance.setTextureAttributes( textureAttributes );
		}

		// Setup Transparency
		final float combinedOpacity = opacity * textureSpec.opacity;
		if ( combinedOpacity >= 0.0f && combinedOpacity < 0.999f )
		{
			final TransparencyAttributes transparency = new TransparencyAttributes( TransparencyAttributes.NICEST , 1.0f - combinedOpacity );
			appearance.setTransparencyAttributes( transparency );
		}

		return appearance;
	}

	/**
	 * Convert <code>TextureSpec<code/> to Java3D <code>Texture</code>
	 * object.
	 *
	 * @param   spec    TextureSpec to convert.
	 *
	 * @return  Texture for the specified texture spec.
	 */
	public Texture getTexture( final TextureSpec spec )
	{
		Texture result = null;

		if ( ( spec != null ) && ( spec.isTexture() ) )
		{
			final String code = spec.code;
			if ( _textureCache.containsKey( code ) )
			{
				result = (Texture)_textureCache.get( code );
			}
			else
			{
				final Image image = spec.getTextureImage();
				if ( image != null )
				{
					final TextureLoader loader = new TextureLoader( image , TEXTURE_OBSERVER );
					result = loader.getTexture();
					result.setCapability( Texture.ALLOW_SIZE_READ );
				}

				_textureCache.put( code , result );
			}
		}

		return result;
	}

	/**
	 * Show Java 3D scene graph tree. Used for debugging.
	 *
	 * @param   node    Root node of tree to display.
	 */
	public static void showTree( final Node node )
	{
		showTreeNode( "" , null , node );
	}

	/**
	 * Internal recursive method used by the <code>showTree</code> method
	 * to show a tree node and all its child nodes recursively while mainting
	 * the correct list layout.
	 *
	 * @param   prefix          Text prefix for layout.
	 * @param   parentChildren  Child nodes of parent node (needed for layout).
	 * @param   node            Tree node to display.
	 */
	private static void showTreeNode( final String prefix , final List parentChildren , final Node node )
	{
		final List children = new ArrayList();
		if ( node instanceof Group )
		{
			final Group group = (Group)node;
			for ( Enumeration e = group.getAllChildren() ; e.hasMoreElements() ; )
				children.add( e.nextElement() );
		}

		final boolean isLast  = ( parentChildren == null ) || parentChildren.indexOf( node ) == ( parentChildren.size() - 1 );
		final boolean isLeaf  = children.size() == 0;

		if ( parentChildren != null )
		{
			System.out.print( prefix );
			System.out.print( isLast ? "`-" : "|-" );
		}
		System.out.print( isLeaf ? "- " : "[+] " );
		final Class nodeClass = node.getClass();
		System.out.println( nodeClass.getName() );

		final String nextPrefix = prefix + ( ( parentChildren == null ) ? " " : isLast ? "   " : "|  " );

		final String subPrefix = nextPrefix + ( isLeaf ? "     " : "|      " );

//		if ( node instanceof Paneel )
//		{
//			System.out.println( subPrefix + "soort         = " + Paneel.enumPaneelSoort[ paneel.getSoort() ] );
//		}

		System.out.println( subPrefix );

		for ( int i = 0 ; i < children.size() ; i++ )
			showTreeNode( nextPrefix , children , (Node)children.get( i ) );

	}

}
