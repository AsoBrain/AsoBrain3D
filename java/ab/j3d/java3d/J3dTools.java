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

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple3i;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import ab.j3d.Matrix3D;
import ab.j3d.TextureLibrary;
import ab.j3d.TextureSpec;
import ab.j3d.renderer.LeafCollection;
import ab.j3d.renderer.Object3D;
import ab.j3d.renderer.TreeNode;

/**
 * Utility class with various 'tools' for using Java 3D. Most code here deals
 * with translating 'AsoBrain 3D Toolkit' objects to their equivalents in the
 * Java 3D API.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class J3dTools
{
	public static final Color3f BLACK  = new Color3f( 0.0f , 0.0f , 0.0f );
	public static final Color3f WHITE  = new Color3f( 1.0f , 1.0f , 1.0f );
	public static final Color3f RED    = new Color3f( 1.0f , 0.0f , 0.0f );
	public static final Color3f YELLOW = new Color3f( 1.0f , 1.0f , 0.0f );

	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = J3dTools.class.getName();

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private J3dTools()
	{
	}

	/**
	 * Translate a complete 'AsoBrain 3D Toolkit' model to a Java3D
	 * scene. The model can be translated/rotated/scaled to position the model
	 * within the target scene.
	 *
	 * @param   position        Target location.
	 * @param   rotation        Rotation around the Z-axis.
	 * @param   scale           Scale factor (0.001 => shrink 1000*).
	 * @param   textureLibrary  Texture library to use for translation.
	 * @param   abModel         Source model.
	 *
	 * @return  TransformGroup representing the root node of the scene.
	 */
	public static TransformGroup abToJ3D( final Vector3f position , final float rotation , final float scale , final TextureLibrary textureLibrary , final float opacity , final TreeNode abModel )
	{
		final TransformGroup tg = getTransform( position , rotation , scale );

		final LeafCollection leafs = new LeafCollection();
		abModel.gatherLeafs( leafs , Object3D.class , Matrix3D.INIT , false );

		for ( int i = 0 ; i < leafs.size() ; i++ )
			tg.addChild( abToJ3D( textureLibrary , opacity , leafs.getMatrix( i ) , (Object3D)leafs.getNode( i ) ) );

		return tg;
	}

	/**
	 * Convenience method to show a Java3D scene in a JFrame.
	 *
	 * @param   scene       Scene to display.
	 * @param   viewPoint   Viewpoint within scene.
	 * @param   createOrbit Create orbit control (to rotate/pan/scale scene).
	 */
	public static void showScene( final BranchGroup scene , final Vector3f viewPoint , final boolean createOrbit )
	{
		final Canvas3D canvas = createCanvas3D( scene , viewPoint , createOrbit );

		final JFrame frame = new JFrame( CLASS_NAME , canvas.getGraphicsConfiguration() );
		frame.getContentPane().add( canvas );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( 800 , 600 );
		frame.setVisible( true );
	}

	/**
	 * Convenience method to create a Canvas3D to display a Java3D scene.
	 *
	 * @param   scene       Scene to display.
	 * @param   viewPoint   Viewpoint within scene.
	 * @param   createOrbit Create orbit control (to rotate/pan/scale scene).
	 *
	 * @return  Canvas3D containing the specified scene.
	 */
	public static Canvas3D createCanvas3D( final BranchGroup scene , final Vector3f viewPoint , final boolean createOrbit )
	{
		final GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();

		// create universe
		final Canvas3D         canvas   = new Canvas3D( gc );
		final SimpleUniverse   universe = new SimpleUniverse( canvas );
		final ViewingPlatform  view     = universe.getViewingPlatform();

		// add scene to universe
		scene.compile();
		universe.addBranchGraph( scene );

		// set camera location/direction in ViewingPlatform
		final Transform3D viewTransform = new Transform3D();
		viewTransform.set( viewPoint );
		view.getViewPlatformTransform().setTransform( viewTransform );

		// add orbit behavior to ViewingPlatform
		if ( createOrbit )
		{
			final OrbitBehavior orbit = new OrbitBehavior( canvas , OrbitBehavior.REVERSE_ALL | OrbitBehavior.STOP_ZOOM );
			final BoundingSphere bounds = new BoundingSphere( new Point3d( 0.0 , 0.0 , 0.0 ) , 100.0 );
			orbit.setSchedulingBounds( bounds );
			view.setViewPlatformBehavior( orbit );
		}
		return canvas;
	}

	public static Transform3D abToJ3D( final Matrix3D m )
	{
		return new Transform3D( new float[] {
			m.xx , m.xy , m.xz , m.xo ,
			m.yx , m.yy , m.yz , m.yo ,
			m.zx , m.zy , m.zz , m.zo ,
			0.0f , 0.0f , 0.0f , 1.0f } );
	}

	public static Group abToJ3D( final TextureLibrary textureLibrary , final float opacity , final Matrix3D xform , final Object3D obj )
	{
		final int     faceCount     = obj.getFaceCount();
		final float[] vertices      = obj.getVertices();
		final float[] vertexNormals = obj.getVertexNormals();
		final float[] vertex        = new float[ 3 ];
		final float[] faceNormal    = new float[ 3 ];

		final Map appearances = new HashMap();

		for ( int face = 0 ; face < faceCount ; face++ )
		{
			final int[]       faceVert    = obj.getFaceVertexIndices( face );
			final int[]       faceTU      = obj.getFaceTextureU( face );
			final int[]       faceTV      = obj.getFaceTextureV( face );
			final TextureSpec faceTexture = obj.getFaceTexture( face );

			if ( faceTexture == null || faceTexture.code == null )
				continue;

			final int nrTriangles = faceVert.length - 2;
			if ( nrTriangles < 1 )
				continue;

			final Texture texture = ( ( faceTU == null ) || ( faceTV == null ) || !faceTexture.isTexture() ) ? null : getTexture( faceTexture );

			final List j3dVertices;
			final List j3dTextureCoords;
			final List j3dFaceNormals;
			{
				final List[] data;
				if ( appearances.containsKey( faceTexture.code ) )
					data = (List[])appearances.get( faceTexture.code );
				else
					appearances.put( faceTexture.code , data = new List[] { new ArrayList() , new ArrayList() , new ArrayList() } );

				j3dVertices      = data[ 0 ];
				j3dTextureCoords = data[ 1 ];
				j3dFaceNormals   = data[ 2 ];
			}

			for ( int triangleIndex = 0 ; triangleIndex < nrTriangles ; triangleIndex++ )
			{
				for ( int subIndex = 3 ; --subIndex >= 0 ; )
				{
					final int vertexIndex = ( subIndex == 0 ) ? 0 : ( triangleIndex + subIndex );
					final int vi = faceVert[ vertexIndex ] * 3;

					vertex[ 0 ] = vertices[ vi     ];
					vertex[ 1 ] = vertices[ vi + 1 ];
					vertex[ 2 ] = vertices[ vi + 2 ];
					xform.transform( vertex , vertex , 1 );

					float tu = 0;
					float tv = 0;
					if ( texture != null )
					{
						tu = (float)faceTU[ vertexIndex ] / texture.getWidth();
						tv = (float)faceTV[ vertexIndex ] / texture.getHeight();
					}

					faceNormal[ 0 ] = vertexNormals[ vi     ];
					faceNormal[ 1 ] = vertexNormals[ vi + 1 ];
					faceNormal[ 2 ] = vertexNormals[ vi + 2 ];
					xform.rotate( faceNormal , faceNormal , 1 );

					j3dVertices.add( new Point3f( vertex ) );
					j3dTextureCoords.add( new TexCoord2f( tu , tv ) );
					j3dFaceNormals.add( new Vector3f( faceNormal ) );
				}
			}
		}

		final Group group = new Group();


		for ( Iterator appearanceEnum = appearances.keySet().iterator() ; appearanceEnum.hasNext() ; )
		{
			final String     code       = (String)appearanceEnum.next();
			final Appearance appearance = abToJ3D( textureLibrary.getTextureSpec( code ) , opacity );
			final boolean    hasTexture = ( appearance.getTexture() != null );

			final List j3dVertices;
			final List j3dTextureCoords;
			final List j3dFaceNormals;
			{
				final List[]   data = (List[])appearances.get( code );
				j3dVertices      = data[ 0 ];
				j3dTextureCoords = data[ 1 ];
				j3dFaceNormals   = data[ 2 ];
			}

			final int what = GeometryArray.COORDINATES | GeometryArray.NORMALS | ( hasTexture ? GeometryArray.TEXTURE_COORDINATE_2 : 0 );
			final GeometryArray geom = new TriangleArray( j3dVertices.size() , what );

			final Point3f[] coordA = (Point3f[])j3dVertices.toArray( new Point3f[ j3dVertices.size() ] );
			geom.setCoordinates( 0 , coordA );

			if ( hasTexture )
			{
				final TexCoord2f[] textCoordsA = (TexCoord2f[])j3dTextureCoords.toArray( new TexCoord2f[ j3dTextureCoords.size() ] );
				geom.setTextureCoordinates( 0 , 0 , textCoordsA );
			}

			final Vector3f[] normA = (Vector3f[])j3dFaceNormals.toArray( new Vector3f[ j3dFaceNormals.size() ] );
			geom.setNormals( 0 , normA );

			final Shape3D shape = new Shape3D( geom , appearance );
			group.addChild( shape );
		}

		return group;
	}

	/**
	 * Translate <code>TextureSpec<code/> to Java3D <code>Appearance</code>
	 * object.
	 *
	 * @param   spec    TextureSpec to translate.
	 */
	public static Appearance abToJ3D( final TextureSpec spec , final float opacity )
	{
		final int   rgb = spec.getARGB();
		final float r   = ( ( rgb >> 16 ) & 255 ) / 255.0f;
		final float g   = ( ( rgb >>  8 ) & 255 ) / 255.0f;
		final float b   = (   rgb         & 255 ) / 255.0f;
		final float ar  = 1.5f * spec.ambientReflectivity;
		final float dr  = 1.2f * spec.diffuseReflectivity;
		final float sr  = 0.5f * spec.specularReflectivity;

		final Material material = new Material();
		material.setLightingEnable( true );
		material.setAmbientColor ( ar * r , ar * g , ar * b );
		material.setEmissiveColor( BLACK );
		material.setDiffuseColor ( dr * r , dr * g , dr * b );
		material.setSpecularColor( new Color3f( sr , sr , sr ) );
		material.setShininess    ( spec.specularReflectivity * spec.specularExponent );

		final Appearance appearance = new Appearance();
		appearance.setCapability( Appearance.ALLOW_TEXTURE_READ );
		appearance.setMaterial( material );

		if ( spec.isTexture() )
		{
			final Texture texture = getTexture( spec );

//			texture.setMinFilter( Texture.NICEST );
//			texture.setMagFilter( Texture.NICEST );
			appearance.setTexture( texture );

			final TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode( TextureAttributes.MODULATE );
//			final Transform3D t = new Transform3D();
//			t.setScale( 1f / spec.textureScale );
//			textureAttributes.setTextureTransform( t  );
			appearance.setTextureAttributes( textureAttributes );
		}

		// Setup Transparency
		final float combinedOpacity = opacity * spec.opacity;
		if ( combinedOpacity >= 0.0f && combinedOpacity < 0.999f )
		{
			final TransparencyAttributes transparency = new TransparencyAttributes( TransparencyAttributes.NICEST , 1.0f - combinedOpacity );
			appearance.setTransparencyAttributes( transparency );
		}

		return appearance;
	}

	private static final Canvas TEXTURE_OBSERVER = new Canvas();
	private static final Map _textureCache = new HashMap();

	private static Texture getTexture( final TextureSpec code )
	{
		Texture result = null;

		if ( ( code != null ) && ( code.isTexture() ) )
		{
			result = (Texture)_textureCache.get( code.code );
			if ( result == null )
			{
				final Image image = code.getTextureImage();
				if ( image != null )
				{
					result = new TextureLoader( image , TEXTURE_OBSERVER ).getTexture();
					result.setCapability( Texture.ALLOW_SIZE_READ );
					_textureCache.put( code , result );
				}
			}
		}

		return result;
	}

	/**
	 * Create StarTrek&tm; Holodeck style unit.
	 *
	 * @param   origin      Grid origin.
	 * @param   size        Grid size in unit unit.
	 * @param   unit        Grid unit size.
	 * @param   interval    Thick line interval (use thick appearance for each n'th line).
	 *
	 * @return  Group containing unit shape.
	 */
	public static Group createGrid( final Tuple3f origin , final Tuple3i size , final float unit , final int interval , final Color3f color )
	{
		final Vector3f min     = new Vector3f( origin.x - size.x * unit , origin.y - size.y * unit , origin.z - size.z * unit );
		final Vector3f max     = new Vector3f( origin.x + size.x * unit , origin.y + size.y * unit , origin.z + size.z * unit );
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
					final float x = origin.x + mult * gridIndex * unit;
					coords.add( new Point3f( x , min.y , min.z ) );
					coords.add( new Point3f( x , max.y , min.z ) );
					coords.add( new Point3f( x , max.y , max.z ) );
					coords.add( new Point3f( x , min.y , max.z ) );
				}

				if ( gridIndex <= size.y )
				{
					final float y = origin.y + mult * gridIndex * unit;
					coords.add( new Point3f( min.x , y , min.z ) );
					coords.add( new Point3f( max.x , y , min.z ) );
					coords.add( new Point3f( max.x , y , max.z ) );
					coords.add( new Point3f( min.x , y , max.z ) );
				}

				if ( gridIndex <= size.z )
				{
					final float z = origin.z + mult * gridIndex * unit;
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
			lineAttributes.setLineWidth( ( i == 0 ) ? 1 : 3 );

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
	 * Create TransformGroup to translate, rotate, and scale geometry in a
	 * Java3D sub-tree.
	 *
	 * @param   position    Target location.
	 * @param   rotation    Rotation around the Z-axis.
	 * @param   scale       Scale factor (0.001 => shrink 1000*).
	 *
	 * @return  TransformGroup to perform the specified transformation.
	 */
	public static TransformGroup getTransform( final Vector3f position , final float rotation , final float scale )
	{
		final Transform3D xform   = new Transform3D();
		final Transform3D operand = new Transform3D();

		operand.rotY( rotation );
		operand.setTranslation( position );
		xform.mul( operand );

		operand.rotX( -Math.PI / 2 );
		operand.setScale( scale );
		xform.mul( operand );

		return new TransformGroup( xform );
	}
}
