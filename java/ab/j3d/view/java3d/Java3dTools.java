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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.image.TextureLoader;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

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
	 * Convert <code>Object3D<code/> to Java3D <code>Node</code> object.
	 *
	 * @param   xform               Transform to apply to vertices.
	 * @param   object3d            Object3D to convert.
	 * @param   textureOverride     Texture to use instead of actual object texture.
	 * @param   opacity             Extra object opacity (0.0=translucent, 1.0=opaque).
	 *
	 * @return  A <code>BranchGroup</code> is returned containing
	 *          <code>Shape3D</code>s for each separate <code>Appearance</code>.
	 */
	public Node convertObject3DToNode( final Matrix3D xform , final Object3D object3d , final TextureSpec textureOverride , final float opacity )
	{
		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );
		result.setCapability( BranchGroup.ALLOW_DETACH );

		final Map appearances = new HashMap();
		final Shape3D lines   = new Shape3D();

		final int      faceCount    = object3d.getFaceCount();
		final double[] pointCoords  = object3d.getPointCoords();
		final double[] pointNormals = object3d.getVertexNormals();

		for ( int i = 0 ; i < faceCount ; i++ )
		{
			final Face3D      face        = object3d.getFace( i );
			final int         vertexCount = face.getVertexCount();
			final TextureSpec texture     = ( textureOverride != null ) ? textureOverride : face.getTexture();

			if ( ( texture != null ) && ( texture.code != null ) && ( vertexCount >= 2 ) )
			{
				final int[] pointIndices = face.getPointIndices();
				final int[] textureU     = ( textureOverride != null ) ? null : face.getTextureU();
				final int[] textureV     = ( textureOverride != null ) ? null : face.getTextureV();

				final List[] data;
				if ( appearances.containsKey( texture) )
					data = (List[])appearances.get( texture );
				else
					appearances.put( texture , data = new List[] { new ArrayList() , new ArrayList() , new ArrayList() } );

				if ( vertexCount == 2 )
				{
					int pointIndex  = pointIndices[ 0 ] * 3;
					final double p1X = pointCoords[ pointIndex     ];
					final double p1Y = pointCoords[ pointIndex + 1 ];
					final double p1Z = pointCoords[ pointIndex + 2 ];

					pointIndex = pointIndices[ 1 ] * 3;
					final double p2X = pointCoords[ pointIndex     ];
					final double p2Y = pointCoords[ pointIndex + 1 ];
					final double p2Z = pointCoords[ pointIndex + 2 ];

					final Point3d point1 = new Point3d(
						xform.transformX( p1X , p1Y , p1Z ) ,
						xform.transformY( p1X , p1Y , p1Z ) ,
						xform.transformZ( p1X , p1Y , p1Z ) );

					final Point3d point2 = new Point3d(
						xform.transformX( p2X , p2Y , p2Z ) ,
						xform.transformY( p2X , p2Y , p2Z ) ,
						xform.transformZ( p2X , p2Y , p2Z ) );

					final GeometryArray geom = new LineArray( 2 , LineArray.COORDINATES );
					geom.setCoordinate( 0 , new double[]{ point1.x , point1.y , point1.z } );
					geom.setCoordinate( 1 , new double[]{ point2.x , point2.y , point2.z } );

					lines.addGeometry( geom );
				}
				else
				{
					final Texture j3dTexture       = ( ( textureU == null ) || ( textureV == null ) ) ? null : getTexture( texture );
					final List    j3dVertices      = data[ 0 ];
					final List    j3dTextureCoords = data[ 1 ];
					final List    j3dFaceNormals   = data[ 2 ];

					final int nrTriangles = vertexCount - 2;
					for ( int triangleIndex = 0 ; triangleIndex < nrTriangles ; triangleIndex++ )
					{
						for ( int subIndex = 3 ; --subIndex >= 0 ; )
						{
							final int   vertexIndex = ( subIndex == 0 ) ? 0 : ( triangleIndex + subIndex );
							final int   pointIndex  = pointIndices[ vertexIndex ] * 3;

							final double pointX = pointCoords[ pointIndex     ];
							final double pointY = pointCoords[ pointIndex + 1 ];
							final double pointZ = pointCoords[ pointIndex + 2 ];

							final double normalX = pointNormals[ pointIndex     ];
							final double normalY = pointNormals[ pointIndex + 1 ];
							final double normalZ = pointNormals[ pointIndex + 2 ];

							j3dVertices.add( new Point3d(
								xform.transformX( pointX , pointY , pointZ ) ,
								xform.transformY( pointX , pointY , pointZ ) ,
								xform.transformZ( pointX , pointY , pointZ ) ) );

							j3dFaceNormals.add( new Vector3f(
								(float)xform.rotateX( normalX , normalY , normalZ ) ,
								(float)xform.rotateY( normalX , normalY , normalZ ) ,
								(float)xform.rotateZ( normalX , normalY , normalZ ) ) );

							j3dTextureCoords.add( ( j3dTexture == null ) ? new TexCoord2f() : new TexCoord2f(
								(float)textureU[ vertexIndex ] / (float)j3dTexture.getWidth()  ,
								(float)textureV[ vertexIndex ] / (float)j3dTexture.getHeight() ) );
						}
					}
				}
			}
		}

		final Set      appearanceTextures = appearances.keySet();
		final Iterator appearanceIterator = appearanceTextures.iterator();

		if ( appearanceTextures.size() == 1 )
		{
			final TextureSpec texture    = (TextureSpec)appearanceIterator.next();
			final Appearance  appearance = getAppearance( texture , opacity );
			final List[]      data       = (List[])appearances.get( texture );

			if ( ( (List)data[ 0 ] ).size() > 2 )
				result.addChild( createShape3D( appearance , data[ 0 ] , data[ 1 ] , data[ 2 ] ) );
		}
		else
		{
			while ( appearanceIterator.hasNext() )
			{
				final TextureSpec texture    = (TextureSpec)appearanceIterator.next();
				final Appearance  appearance = getAppearance( texture , opacity );
				final List[]      data       = (List[])appearances.get( texture );

				if ( ( (List)data[ 0 ] ).size() > 2 )
					result.addChild( createShape3D( appearance , data[ 0 ] , data[ 1 ] , data[ 2 ] ) );
			}
		}

		result.addChild( lines );

		return result;
	}

	/**
	 * Convert AB <code>Matrix3D<code/> to Java3D <code>Matrix4f</code> object.
	 *
	 * @param   matrix  Matrix3D to convert.
	 *
	 * @return  <code>Matrix4f</code> instance.
	 */
	public static Matrix4d convertMatrix3DToMatrix4d( final Matrix3D matrix )
	{
		return new Matrix4d(
			matrix.xx , matrix.xy , matrix.xz , matrix.xo ,
			matrix.yx , matrix.yy , matrix.yz , matrix.yo ,
			matrix.zx , matrix.zy , matrix.zz , matrix.zo ,
			0.0 , 0.0 , 0.0 , 1.0 );
	}

	/**
	 * Convert AB <code>Matrix3D<code/> to Java3D <code>Matrix4f</code> object.
	 *
	 * @param   matrix  Matrix3D to convert.
	 *
	 * @return  <code>Matrix4f</code> instance.
	 */
	public static Transform3D convertMatrix3DToTransform3D( final Matrix3D matrix )
	{
		return new Transform3D( convertMatrix3DToMatrix4d( matrix ) );
	}

	/**
	 * Convert Java3D <code>Tranform3D<code/> object to AB <code>Matrix3D</code>.
	 *
	 * @param   transform   Transform3D to convert.
	 *
	 * @return  <code>Matrix3D</code> instance.
	 */
	public static Matrix3D convertTransform3DToMatrix3D( final Transform3D transform )
	{
		final Matrix4d m4d = new Matrix4d();
		transform.get( m4d );

		return Matrix3D.INIT.set(
			m4d.m00 , m4d.m01 , m4d.m02 , m4d.m03 ,
			m4d.m10 , m4d.m11 , m4d.m12 , m4d.m13 ,
			m4d.m20 , m4d.m21 , m4d.m22 , m4d.m23 );
	}

	/**
	 * Get the transform to look from a specified point to a specified point.
	 *
	 * @param   from    Point to look from.
	 * @param   to      Point to look at.
	 *
	 * @return  The transform to look from 'from' to 'to'.
	 */
	public static Transform3D createFromToTransform3D( final Vector3D from , final Vector3D to )
	{
		if ( from.equals( to ) )
			throw new IllegalArgumentException( "getTransfrom( from , to ); 'from' and 'to' can not be the same!" );

		final Vector3d upVector;
		if ( ( from.x == 0 ) && ( from.y == 0 ) && ( from.z != 0 ) )
			upVector = new Vector3d( 0.0 , 1.0 , 0.0 );
		else
			upVector = new Vector3d( 0.0 , 0.0 , 1.0 );

		final Transform3D transform = new Transform3D();
		transform.setTranslation( new Vector3d( from.x , from.y , from.z ) );
		transform.lookAt( new Point3d( from.x , from.y , from.z ) , new Point3d( to.x , to.y , to.z ) , upVector );
		transform.invert();
		return transform;
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
	 * Set content of panel to a spinning image box.
	 *
	 * @param   image   Image to place on box.
	 *
	 * @return  Java 3D content graph with spinning image box.
	 */
	public static BranchGroup createImageSpinnerContent( final Image image )
	{
		final BranchGroup result = new BranchGroup();
		result.setCapability( BranchGroup.ALLOW_CHILDREN_READ );

		final TransformGroup boxTransform = new TransformGroup();
		boxTransform.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		result.addChild( boxTransform );

		final RotationInterpolator spinner = new RotationInterpolator(
		        /* alpha          */ new Alpha( -1 , Alpha.INCREASING_ENABLE , 0L , 0L , 8000L , 0L , 0L , 0L , 0L , 0L ) ,
		        /* transformgroup */ boxTransform ,
		        /* axis           */ new Transform3D() ,
		        /* startValue     */ (float) Math.PI * 2.0f ,
		        /* endValue       */ 0.0f );
		spinner.setSchedulingBounds( new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 ) );
		boxTransform.addChild( spinner );

		final Appearance appearance = new Appearance();
		final TextureLoader loader = new TextureLoader( image , TEXTURE_OBSERVER );
		appearance.setTexture( loader.getTexture() );
		boxTransform.addChild( new Box( 0.15f , 0.15f , 0.15f , Box.GENERATE_TEXTURE_COORDS , appearance ) );

		return result;
	}

	/**
	 * Create orbit control with default scheduling bounds for the specified
	 * canvas. The behavior must be assigned to a <code>TransformGroup</code> to
	 * be effective.
	 *
	 * @param   canvas  Canvas to create orbit control for.
	 * @param   unit    Unit scale factor (e.g. <code>MM</code>).
	 *
	 * @return  Orbit behavior.
	 */
	public static OrbitBehavior createOrbitBehavior( final Canvas3D canvas , final double unit )
	{
		final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );

		final OrbitBehavior orbit = new SimpleOrbitBehavior( canvas , unit );
		orbit.setSchedulingBounds( bounds );

		return orbit;
	}

	/**
	 * Create a <code>Shape3D</code> by using the specified parameters.
	 *
	 * @param   appearance          Appearance of the Shape3D to create.
	 * @param   j3dVertices         Vertices of the Shape3D to create.
	 * @param   j3dTextureCoords    Texture U- and V-coordinates of the Shape3D.
	 * @param   j3dFaceNormals      Face normals of the Shape3D to create.
	 *
	 * @return  Shape3d created out of the specified parameters.
	 */
	private static Shape3D createShape3D( final Appearance appearance , final List j3dVertices , final List j3dTextureCoords , final List j3dFaceNormals )
	{
		final boolean hasTexture = ( appearance.getTexture() != null );

		final int           what = GeometryArray.COORDINATES | GeometryArray.NORMALS | ( hasTexture ? GeometryArray.TEXTURE_COORDINATE_2 : 0 );
		final GeometryArray geom = new TriangleArray( j3dVertices.size() , what );

		final Point3d[] coordA = (Point3d[])j3dVertices.toArray( new Point3d[ j3dVertices.size() ] );
		geom.setCoordinates( 0 , coordA );

		if ( hasTexture )
		{
			final TexCoord2f[] textCoordsA = (TexCoord2f[])j3dTextureCoords.toArray( new TexCoord2f[ j3dTextureCoords.size() ] );
			geom.setTextureCoordinates( 0 , 0 , textCoordsA );
		}

		geom.setNormals( 0 , (Vector3f[])j3dFaceNormals.toArray( new Vector3f[ j3dFaceNormals.size() ] ) );

		return new Shape3D( geom , appearance );
	}

	/**
	 * Create Transform3D to translate, rotate, and scale geometry in Java 3D.
	 *
	 * @param   position    Target location.
	 * @param   rotation    Rotation around the Z-axis.
	 * @param   scale       Scale factor (0.001 => shrink 1000*).
	 *
	 * @return  Transform3D to perform the specified transformation.
	 */
	public static Transform3D createTransform3D( final Vector3D position , final double rotation , final double scale )
	{
		final Transform3D xform   = new Transform3D();
		final Transform3D operand = new Transform3D();

		operand.rotY( rotation );
		operand.setTranslation( new Vector3d( position.x , position.y , position.z ) );
		xform.mul( operand );

		operand.rotX( -Math.PI / 2.0 );
		operand.setScale( scale );
		xform.mul( operand );

		return new Transform3D( xform );
	}

	/**
	 * Conveniene method to create a <code>Canvas3D</code> with default
	 * configuration settings.
	 *
	 * @return  Canvas that wascreated.
	 */
	public static Canvas3D createCanvas3D()
	{
		return new Canvas3D( Java3dUniverse.getPreferredConfiguration() )
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
