/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2012 Peter S. Heijnen
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
package ab.j3d.awt.view.java3d;

import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.awt.image.Raster;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.media.j3d.Appearance;
import javax.media.j3d.*;
import javax.vecmath.*;
import javax.vecmath.Vector3f;

import ab.j3d.*;
import ab.j3d.appearance.*;

/**
 * Utility methods for Java 3D support.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class Java3dTools
{
	/**
	 * Texture observer needed by {@link #loadTexture} to load textures.
	 *
	 * @see     #loadTexture
	 */
	private static final Canvas TEXTURE_OBSERVER = new Canvas();

	/**
	 * Color model for <code>loadTexture()</code>.
	 */
	private static final ComponentColorModel TEXTURE_CM = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), new int[] { 8, 8, 8, 8 }, true, false, Transparency.TRANSLUCENT, 0 );

	/**
	 * Texture map cache (maps map name to texture).
	 */
	private final Map<TextureMap,Texture> _textureCache = new HashMap<TextureMap,Texture>();

	/**
	 * Singleton {@link Java3dTools} instance.
	 */
	private static Java3dTools _singleton;

	/**
	 * Construct {@link Java3dTools} for centralized texture caching, etc.
	 */
	private Java3dTools()
	{
	}

	/**
	 * Get singleton instance.
	 *
	 * @return  Singleton {@link Java3dTools} instance.
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
	 * Convert <code>Matrix3D<code/> to Java3D {@link Transform3D} object.
	 *
	 * @param   matrix  Matrix3D to convert.
	 *
	 * @return  {@link Transform3D} instance.
	 */
	public static Transform3D convertMatrix3DToTransform3D( final Matrix3D matrix )
	{
		return new Transform3D( new Matrix4d(
			matrix.xx, matrix.xy, matrix.xz, matrix.xo,
			matrix.yx, matrix.yy, matrix.yz, matrix.yo,
			matrix.zx, matrix.zy, matrix.zz, matrix.zo,
			0.0      , 0.0      , 0.0      , 1.0 ) );
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
	public static Group createGrid( final Tuple3f origin, final Tuple3i size, final float unit, final int interval, final Color3f color )
	{
		final Vector3f min     = new Vector3f( origin.x - (float)size.x * unit, origin.y - (float)size.y * unit, origin.z - (float)size.z * unit );
		final Vector3f max     = new Vector3f( origin.x + (float)size.x * unit, origin.y + (float)size.y * unit, origin.z + (float)size.z * unit );
		final int      maxSize = Math.max( Math.max( size.x, size.y ), size.z );

		final PolygonAttributes polygonAttributes = new PolygonAttributes();
		polygonAttributes.setPolygonMode( PolygonAttributes.POLYGON_LINE );
		polygonAttributes.setCullFace( PolygonAttributes.CULL_NONE );

		final ColoringAttributes coloringAttributes = new ColoringAttributes();
		coloringAttributes.setColor( color );
		coloringAttributes.setShadeModel( ColoringAttributes.FASTEST );

		final List<Point3f> thickCoords = new ArrayList<Point3f>();
		final List<Point3f> thinCoords  = new ArrayList<Point3f>();

		for ( int gridIndex = maxSize ; gridIndex >= 0 ; gridIndex-- )
		{
			final List<Point3f> coords = ( ( interval > 0 ) && ( ( gridIndex % interval ) == 0 ) ) ? thickCoords : thinCoords;

			for ( int mult = ( gridIndex == 0 ) ? 1 : -1 ; mult <= 1 ; mult += 2 )
			{
				if ( gridIndex <= size.x )
				{
					final float x = origin.x + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( x, min.y, min.z ) );
					coords.add( new Point3f( x, max.y, min.z ) );
					coords.add( new Point3f( x, max.y, max.z ) );
					coords.add( new Point3f( x, min.y, max.z ) );
				}

				if ( gridIndex <= size.y )
				{
					final float y = origin.y + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( min.x, y, min.z ) );
					coords.add( new Point3f( max.x, y, min.z ) );
					coords.add( new Point3f( max.x, y, max.z ) );
					coords.add( new Point3f( min.x, y, max.z ) );
				}

				if ( gridIndex <= size.z )
				{
					final float z = origin.z + (float)( mult * gridIndex ) * unit;
					coords.add( new Point3f( min.x, min.y, z ) );
					coords.add( new Point3f( max.x, min.y, z ) );
					coords.add( new Point3f( max.x, max.y, z ) );
					coords.add( new Point3f( min.x, max.y, z ) );
				}
			}
		}

		final Group group = new Group();
		for ( int i = 0 ; i < 2 ; i++ )
		{
			final List<Point3f> vCoords = ( i == 0 ) ? thinCoords : thickCoords;
			if ( vCoords.isEmpty() )
			{
				continue;
			}

			final LineAttributes lineAttributes = new LineAttributes();
			lineAttributes.setLineWidth( ( i == 0 ) ? 1.0f : 3.0f );

			final Appearance appearance = new Appearance();
			appearance.setLineAttributes( lineAttributes );
			appearance.setPolygonAttributes( polygonAttributes );
			appearance.setColoringAttributes( coloringAttributes );

			final QuadArray quadArray = new QuadArray( vCoords.size(), LineStripArray.COORDINATES );
			final Point3f[] pCoords = vCoords.toArray( new Point3f[ vCoords.size() ] );
			quadArray.setCoordinates( 0, pCoords );

			final Shape3D child = new Shape3D( quadArray, appearance );

			/*@FIXME dirty hack to prevent "Intesection not allowed" exceptions when checking for mouseclicks */
			child.setPickable( false );

			group.addChild( child );
		}
		return group;
	}

	/**
	 * Returns the best {@link GraphicsConfiguration} for the default
	 * {@link GraphicsDevice}.
	 *
	 * @return  best {@link GraphicsConfiguration} for the default
	 *          {@link GraphicsDevice}.
	 */
	public static GraphicsConfiguration getGraphicsConfiguration()
	{
		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice device = environment.getDefaultScreenDevice();

		return device.getBestConfiguration( new GraphicsConfigTemplate3D() );
	}

	/**
	 * Conveniene method to create a {@link Canvas3D} with default
	 * configuration settings.
	 *
	 * @return  Canvas that wascreated.
	 */
	public static Canvas3D createCanvas3D()
	{
		return new Canvas3D( getGraphicsConfiguration() )
			{
				/*
				 * Override <code>getMinimumSize()</code> to allow layout manager to
				 * do its job. Otherwise, this will always return the current size of
				 * the canvas, not allowing it to be reduced in size.
				 */
				@Override
				public Dimension getMinimumSize()
				{
					return new Dimension( 10, 10 );
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
	 * Add content defined by a {@link BranchGroup} to the specified dynamic
	 * scene graph. The <code>ALLOW_DETACH</code> capability of the added content
	 * is set, to allow it to be removed later.
	 *
	 * @param   dynamicScene    Dynamic scene graph root node.
	 * @param   content         Content to set in the dynamic scene.
	 *
	 * @see     #createDynamicScene
	 * @see     #clearDynamicContent
	 */
	public static void addDynamicContent( final Group dynamicScene, final BranchGroup content )
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
	 * Get Java3D {@link Appearance} for the specified material.
	 *
	 * @param   abAppearance    Material to get the {@link Appearance} for.
	 * @param   opacity         Opacity to apply to the returned appearance.
	 * @param   hasBackFace     Flag to indicate if face has a back-face.
	 *
	 * @return  Appearance for the specified texture spec.
	 */
	public Appearance getAppearance( final ab.j3d.appearance.Appearance abAppearance, final float opacity, final boolean hasBackFace )
	{
		final Color4 abAmbient = abAppearance.getAmbientColor();
		final Color4 abDiffuse = abAppearance.getDiffuseColor();
		final Color4 abSpecular = abAppearance.getSpecularColor();
		final Color4 abEmissive = abAppearance.getEmissiveColor();

		final Material j3dMaterial = new Material();
		j3dMaterial.setLightingEnable( true );
		j3dMaterial.setAmbientColor( abAmbient.getRedFloat(), abAmbient.getGreenFloat(), abAmbient.getBlueFloat() );
		j3dMaterial.setEmissiveColor( abEmissive.getRedFloat(), abEmissive.getGreenFloat(), abEmissive.getBlueFloat() );
		j3dMaterial.setDiffuseColor( abDiffuse.getRedFloat(), abDiffuse.getGreenFloat(), abDiffuse.getBlueFloat() );
		j3dMaterial.setSpecularColor( abSpecular.getRedFloat(), abSpecular.getGreenFloat(), abSpecular.getBlueFloat() );
		j3dMaterial.setShininess( (float) abAppearance.getShininess() );

		final Appearance appearance = new Appearance();
		appearance.setCapability( Appearance.ALLOW_TEXTURE_READ );
		appearance.setMaterial( j3dMaterial );

		final Texture texture = getColorMapTexture( abAppearance );
		if ( texture != null )
		{
			appearance.setTexture( texture );

			final TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode( TextureAttributes.MODULATE );
			appearance.setTextureAttributes( textureAttributes );
		}

		// Setup Transparency
		boolean noCulling = hasBackFace;

		final float combinedOpacity = opacity * abDiffuse.getAlphaFloat();
		if ( combinedOpacity >= 0.0f && combinedOpacity < 0.999f )
		{
			final TransparencyAttributes transparency = new TransparencyAttributes( TransparencyAttributes.NICEST, 1.0f - combinedOpacity );
			appearance.setTransparencyAttributes( transparency );
			noCulling = true;
		}

		if ( noCulling )
		{
			final PolygonAttributes polygonAttributes = new PolygonAttributes();
			polygonAttributes.setBackFaceNormalFlip( true );
			polygonAttributes.setCullFace( PolygonAttributes.CULL_NONE );
			appearance.setPolygonAttributes( polygonAttributes );
		}

		appearance.setColoringAttributes( new ColoringAttributes( abDiffuse.getRedFloat(), abDiffuse.getGreenFloat(), abDiffuse.getBlueFloat(), ColoringAttributes.FASTEST ) );

		return appearance;
	}

	/**
	 * Get {@link Texture} for color map of the specified material.
	 *
	 * @param   material    Material to get color map texture for.
	 *
	 * @return  Texture for the specified name;
	 *          <code>null</code> if the name was empty or no map by the
	 *          given name was found.
	 */
	public Texture getColorMapTexture( final ab.j3d.appearance.Appearance material )
	{
		Texture result = null;

		if ( material != null )
		{
			final TextureMap colorMap = material.getColorMap();
			if ( colorMap != null )
			{
				final Map<TextureMap,Texture> cache = _textureCache;
				if ( cache.containsKey( colorMap ) )
				{
					result = cache.get( colorMap );
				}
				else
				{
					Image image = null;
					try
					{
						image = colorMap.loadImage();
					}
					catch ( IOException e )
					{
						System.err.println( "getColorMapTexture( " + colorMap + " ) => " + e );
					}

					if ( image != null )
					{
						result = loadTexture( image );
						result.setCapability( Texture.ALLOW_SIZE_READ );
					}

					cache.put( colorMap, result );
				}
			}
		}

		return result;
	}

	/**
	 * Helper method to perform actual texture loading.
	 *
	 * @param   image   Image to use for image.
	 *
	 * @return  Texture2D instance for image;
	 *          <code>null</code> if a problem occured.
	 *
	 * @see     #getColorMapTexture
	 */
	public static Texture2D loadTexture( final Image image )
	{
		Texture2D result = null;

		if ( image != null )
		{
			final Component observer = TEXTURE_OBSERVER;
			observer.prepareImage( image, null );

			while ( true )
			{
				final int status = observer.checkImage( image, null );
				if ( ( status & ImageObserver.ERROR ) != 0 )
				{
					break;
				}
				else if ( ( status & ImageObserver.ALLBITS ) != 0 )
				{
					final int width  = getAdjustedTextureSize( image.getWidth( observer ) );
					final int height = getAdjustedTextureSize( image.getHeight( observer ) );

					final BufferedImage bufferedImage = new BufferedImage( TEXTURE_CM, Raster.createInterleavedRaster( DataBuffer.TYPE_BYTE, width, height, width * 4, 4, new int[] { 0, 1, 2, 3 }, null ), false, null );

					final Graphics g = bufferedImage.getGraphics();
					g.drawImage( image, 0, 0, width, height, observer );
					g.dispose();

					result = new Texture2D( Texture2D.BASE_LEVEL, Texture.RGBA, width, height );
					result.setImage( 0, new ImageComponent2D( ImageComponent.FORMAT_RGBA, bufferedImage, false, false ) );
					result.setMinFilter( Texture.BASE_LEVEL_LINEAR );
					result.setMagFilter( Texture.BASE_LEVEL_LINEAR );
					break;
				}

				try
				{
					Thread.sleep( 100L );
				}
				catch ( InterruptedException e )
				{
				}
			}
		}

		return result;
	}

	/**
	 * Helper method to calculate an adjusted texture size. The Java 3D API
	 * requires this to be a power of 2.
	 *
	 * @param   size   Raw texture image size.
	 *
	 * @return  Adjusted size to comply with Java 3D API (may be unaltered).
	 *
	 * @see     #loadTexture
	 */
	private static int getAdjustedTextureSize( final int size )
	{
		int result;

		if ( size > 1 )
		{
			result = 2;
			while ( size > result )
			{
				result *= 2;
			}

			if ( size != result )
			{
				final int halfResult = result / 2;
				if ( ( result - size ) > ( size - halfResult ) )
				{
					result = halfResult;
				}
			}
		}
		else
		{
			result = size;
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
		showTreeNode( "", null, node );
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
	private static void showTreeNode( final String prefix, final List parentChildren, final Node node )
	{
		final List children = new ArrayList();
		if ( node instanceof Group )
		{
			final Group group = (Group)node;
			for ( Enumeration e = group.getAllChildren() ; e.hasMoreElements() ; )
			{
				children.add( e.nextElement() );
			}
		}

		final boolean isLast  = ( parentChildren == null ) || parentChildren.indexOf( node ) == ( parentChildren.size() - 1 );
		final boolean isLeaf  = children.isEmpty();

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
//			System.out.println( subPrefix + "soort         = " + Paneel.enumPaneelSoort[ paneel.getPanelType() ] );
//		}

		System.out.println( subPrefix );

		for ( int i = 0 ; i < children.size() ; i++ )
		{
			showTreeNode( nextPrefix, children, (Node) children.get( i ) );
		}

	}

}
