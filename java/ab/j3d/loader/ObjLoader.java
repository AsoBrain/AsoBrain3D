/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2006
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
package ab.j3d.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ab.j3d.Matrix3D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;
import ab.j3d.model.Face3D;
import ab.j3d.model.Object3D;

import com.numdata.oss.TextTools;

/**
 * Loader for Wavefront Object Files (.obj).
 * <p>
 * The following types of data may be included in an .obj file. In this
 * list, the keyword (in parentheses) follows the data type.
 * <p>
 * Vertex data
 * <ul>
 *  <li>geometric vertices (v)
 *  <li>texture vertices (vt)
 *  <li>vertex normals (vn)
 *  <li>parameter space vertices (vp)<br>Free-form curve/surface attributes
 *  <li>rational or non-rational forms of curve or surface type: basis matrix, Bezier, B-spline, Cardinal, Taylor (cstype)
 *  <li>degree (deg)
 *  <li>basis matrix (bmat)
 *  <li>step size (step)
 * </ul>
 * <p>
 * Elements
 * <ul>
 *  <li>point (p)
 *  <li>line (l)
 *  <li>face (f)
 *  <li>curve (curv)
 *  <li>2D curve (curv2)
 *  <li>surface (surf)
 * </ul>
 * <p>
 * Free-form curve/surface body statements
 * <ul>
 *  <li>parameter values (parm)
 *  <li>outer trimming loop (trim)
 *  <li>inner trimming loop (hole)
 *  <li>special curve (scrv)
 *  <li>special point (sp)
 *  <li>end statement (end)
 * </ul>
 * <p>
 * Connectivity between free-form surfaces
 * <ul>
 *  <li>connect (con)
 * </ul>
 * <p>
 * Grouping
 * <ul>
 *  <li>group name (g)
 *  <li>smoothing group (s)
 *  <li>merging group (mg)
 *  <li>object name (o)
 * </ul>
 * <p>
 * Display/render attributes
 * <ul>
 *  <li>bevel interpolation (bevel)
 *  <li>color interpolation (c_interp)
 *  <li>dissolve interpolation (d_interp)
 *  <li>level of detail (lod)
 *  <li>material name (usemtl)
 *  <li>material library (mtllib)
 *  <li>shadow casting (shadow_obj)
 *  <li>ray tracing (trace_obj)
 *  <li>curve approximation technique (ctech)
 *  <li>surface approximation technique (stech)
 * </ul>
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ObjLoader
{
	/**
	 * Pattern for vertices in polygonal geometry statements 'p', 'l', and 'f'.
	 */
	public static final Pattern POLYGON_VERTEX_PATTERN  = Pattern.compile( "(\\d+)(/(\\d+)?(/(\\d+))?)?" ); // vertex#[/textureVertex#1[/vertexNormal#]]

	/**
	 * Default texture map to use for OBJ files.
	 */
	private static final Map DEFAULT_TEXTURES;
	static
	{
		final Map textureMap = new HashMap();

		/* default texture (also used for unknown materials) */
		textureMap.put(  "default"       , new TextureSpec( 0xFFC0C0C0 ) );

		/* basic colors */
		textureMap.put(  "black"         , new TextureSpec( 0xFF000000 ) );
		textureMap.put(  "blue"          , new TextureSpec( 0xFF0000FF ) );
		textureMap.put(  "green"         , new TextureSpec( 0xFF00FF00 ) );
		textureMap.put(  "cyan"          , new TextureSpec( 0xFF00FFFF ) );
		textureMap.put(  "red"           , new TextureSpec( 0xFFFF0000 ) );
		textureMap.put(  "magenta"       , new TextureSpec( 0xFFFF00FF ) );
		textureMap.put(  "yellow"        , new TextureSpec( 0xFFFFFF00 ) );
		textureMap.put(  "white"         , new TextureSpec( 0xFFFCFCFC ) );

		/* materials */
		textureMap.put(  "brass"         , new TextureSpec( 0xFFE0E010 ) );
//		textureMap.put(  "glass"         , new TextureSpec( 0x20102010 ) );
//		textureMap.put(  "light"         , new TextureSpec( 0x80FFFF20 ) );
//		textureMap.put(  "metal"         , new TextureSpec( 0xFFE0E0F8 ) );
//		textureMap.put(  "plastic"       , new TextureSpec( 0xFFC0C0C0 ) );
//		textureMap.put(  "porcelin"      , new TextureSpec( 0xFFFFFFFF ) );
//		textureMap.put(  "steel"         , new TextureSpec( 0xFFD0D0E8 ) );
//		textureMap.put(  "white_plastic" , new TextureSpec( 0xFFC0C0C0 ) );
//		textureMap.put(  "wood"          , new TextureSpec( 0xFF603820 ) );

		DEFAULT_TEXTURES = textureMap;
	}

	/**
	 * Load the specified OBJ file.
	 *
	 * @param   transform       Transormation to apply to the OBJ (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   textureMap      Maps material names ({@link String}) to textures ({@link TextureSpec}.
	 * @param   objReader       Source from which the OBJ file is read (the
	 *                          reader will not be closed by this method).
	 *
	 * @return  {@link Object3D} with loaded OBJ file.
	 *
	 * @throws  IOException if an error occured while loading the OBJ file.
	 */
	public static Object3D load( final Matrix3D transform , final Map textureMap , final BufferedReader objReader )
		throws IOException
	{
		final Map actualTextureMap;
		if ( textureMap != null && !textureMap.isEmpty() )
		{
			actualTextureMap = new HashMap( DEFAULT_TEXTURES.size() + textureMap.size() );
			actualTextureMap.putAll( DEFAULT_TEXTURES );
			actualTextureMap.putAll( textureMap );
		}
		else
		{
			actualTextureMap = DEFAULT_TEXTURES;
		}

		final Object3D result = new Object3D();

		final TextureSpec defaultTexture;
		if ( actualTextureMap.containsKey( "default" ) )
			defaultTexture = (TextureSpec)actualTextureMap.get( "default" );
		else
			defaultTexture = new TextureSpec( 0xFFC0C0C0 );

		/*
		 * Read OBJ data
		 */
//		System.out.println( " - load OBJ data" );

		final List objVertices        = new ArrayList(); // element: Vector3D
		final List objTextureVertices = new ArrayList(); // element: Vector3D (u,v,w)
		final List objVertexNormals   = new ArrayList(); // element: Vector3D (i,j,k)

		double[]    abPointCoords   = null;
		double[]    abVertexNormals = null;
		TextureSpec abTexture       = defaultTexture;

		String line;
		while ( ( line = objReader.readLine() ) != null )
		{
			final int hash = line.indexOf( (int)'#' );

			if ( hash >= 0 )
				line = line.substring( 0 , hash );

			line = line.replaceAll( "\\s+" , " " );
			line = line.trim();

			if ( line.length() > 0 )
			{
				final String[] tokens = TextTools.tokenize( line , ' ' );
				final String name = tokens[ 0 ];
				final int argCount = tokens.length - 1;

				try
				{
					/*
					 * v x y z w
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Specifies a geometric vertex and its x y z coordinates. Rational
					 *     curves and surfaces require a fourth homogeneous coordinate, also
					 *     called the weight.
					 *
					 *     x y z are the x, y, and z coordinates for the vertex. These are
					 *     floating point numbers that define the position of the vertex in
					 *     three dimensions.
					 *
					 *     w is the weight required for rational curves and surfaces. It is
					 *     not required for non-rational curves and surfaces. If you do not
					 *     specify a value for w, the default is 1.0.
					 *
					 *     NOTE: A positive weight value is recommended. Using zero or
					 *     negative values may result in an undefined point in a curve or
					 *     surface.
					 */
					if ( "v".equals( name ) )
					{
						if ( argCount < 3 )
							throw new IOException( "malformed vertex entry: " + line );

						final double x = Double.parseDouble( tokens[ 1 ] );
						final double y = Double.parseDouble( tokens[ 2 ] );
						final double z = Double.parseDouble( tokens[ 3 ] );
//						final double w = ( argCount >= 4 ) ? Double.parseDouble( tokens[ 4 ] ) : 1.0;

						objVertices.add( transform.multiply( x , y , z ) );
					}
					/*
					 * vt u v w
					 *
					 *     Vertex statement for both polygonal and free-form geometry.
					 *
					 *     Specifies a texture vertex and its coordinates. A 1D texture
					 *     requires only u texture coordinates, a 2D texture requires both u
					 *     and v texture coordinates, and a 3D texture requires all three
					 *     coordinates.
					 *
					 *     u is the value for the horizontal direction of the texture.
					 *
					 *     v is an optional argument.
					 *
					 *     v is the value for the vertical direction of the texture. The
					 *     default is 0.
					 *
					 *     w is an optional argument.
					 *
					 *     w is a value for the depth of the texture. The default is 0.
					 */
					else if ( "vt".equals( name ) )
					{
						if ( argCount < 2 )
							throw new IOException( "malformed texture vertex entry: " + line );

						final double u = Double.parseDouble( tokens[ 1 ] );
						final double v = Double.parseDouble( tokens[ 2 ] );
						final double w = ( argCount >= 3 ) ? Double.parseDouble( tokens[ 3 ] ) : 0.0;

						objTextureVertices.add( Vector3D.INIT.set( u , v , w ) );
					}
					/*
					 * vn i j k
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Specifies a normal vector with components i, j, and k.
					 *
					 *     Vertex normals affect the smooth-shading and rendering of geometry.
					 *     For polygons, vertex normals are used in place of the actual facet
					 *     normals.  For surfaces, vertex normals are interpolated over the
					 *     entire surface and replace the actual analytic surface normal.
					 *
					 *     When vertex normals are present, they supersede smoothing groups.
					 *
					 *     i j k are the i, j, and k coordinates for the vertex normal. They
					 *     are floating point numbers.
					 */
					else if ( "vn".equals( name ) )
					{
						if ( argCount < 3 )
							throw new IOException( "malformed vertex normal entry: " + line );

						final double i = Double.parseDouble( tokens[ 1 ] );
						final double j = Double.parseDouble( tokens[ 2 ] );
						final double k = Double.parseDouble( tokens[ 3 ] );

						objVertexNormals.add( transform.rotate( i , j , k ) );
					}
					/*
					 * p  v1 v2 v3 . . .
					 *
					 *     Polygonal geometry statement.
					 *
					 *     Specifies a point element and its vertex. You can specify multiple
					 *     points with this statement. Although points cannot be shaded or
					 *     rendered, they are used by other Advanced Visualizer programs.
					 *
					 *     v is the vertex reference number for a point element. Each point
					 *     element requires one vertex. Positive values indicate absolute
					 *     vertex numbers. Negative values indicate relative vertex numbers.
					 *
					 * l  v1/vt1   v2/vt2   v3/vt3 . . .
					 *
					 *     Polygonal geometry statement.
					 *
					 *     Specifies a line and its vertex reference numbers. You can
					 *     optionally include the texture vertex reference numbers. Although
					 *     lines cannot be shaded or rendered, they are used by other Advanced
					 *     Visualizer programs.
					 *
					 *     The reference numbers for the vertices and texture vertices must be
					 *     separated by a slash (/). There is no space between the number and
					 *     the slash.
					 *
					 *     v is a reference number for a vertex on the line. A minimum of two
					 *     vertex numbers are required. There is no limit on the maximum.
					 *     Positive values indicate absolute vertex numbers. Negative values
					 *     indicate relative vertex numbers.
					 *
					 *     vt is an optional argument.
					 *
					 *     vt is the reference number for a texture vertex in the line
					 *     element. It must always follow the first slash.
					 *
					 * f  v1/vt1/vn1   v2/vt2/vn2   v3/vt3/vn3 . . .
					 *
					 *     Polygonal geometry statement.
					 *
					 *     Specifies a face element and its vertex reference number. You can
					 *     optionally include the texture vertex and vertex normal reference
					 *     numbers.
					 *
					 *     The reference numbers for the vertices, texture vertices, and
					 *     vertex normals must be separated by slashes (/). There is no space
					 *     between the number and the slash.
					 *
					 *     v is the reference number for a vertex in the face element. A
					 *     minimum of three vertices are required.
					 *
					 *     vt is an optional argument.
					 *
					 *     vt is the reference number for a texture vertex in the face
					 *     element. It always follows the first slash.
					 *
					 *     vn is an optional argument.
					 *
					 *     vn is the reference number for a vertex normal in the face element.
					 *     It must always follow the second slash.
					 *
					 *     Face elements use surface normals to indicate their orientation. If
					 *     vertices are ordered counterclockwise around the face, both the
					 *     face and the normal will point toward the viewer. If the vertex
					 *     ordering is clockwise, both will point away from the viewer. If
					 *     vertex normals are assigned, they should point in the general
					 *     direction of the surface normal, otherwise unpredictable results
					 *     may occur.
					 *
					 *     If a face has a texture map assigned to it and no texture vertices
					 *     are assigned in the f statement, the texture map is ignored when
					 *     the element is rendered.
					 *
					 *     NOTE: Any references to fo (face outline) are no longer valid as of
					 *     version 2.11. You can use f (face) to get the same results.
					 *     References to fo in existing .obj files will still be read,
					 *     however, they will be written out as f when the file is saved.
					 */
					else if ( "p".equals( name )
					       || "l".equals( name )
					       || "f".equals( name ) )
					{
						if ( argCount < 1 )
							throw new IOException( "too few face arguments in: " + line );

						final int   objVertexCount      = objVertices.size();
						final int   abPointCoordsLength = objVertexCount * 3;
						final int[] abFacePointIndices  = new int[ argCount ];

						if ( ( abPointCoords == null ) || ( abPointCoords.length < abPointCoordsLength ) )
						{
							if ( objVertexCount < 1 )
								throw new IOException( "vertex used before vertex declaration" );

							final double[] oldCoords = abPointCoords;
							abPointCoords = new double[ abPointCoordsLength ];

							int objIndex = 0;
							int abIndex  = 0;

							if ( oldCoords != null )
							{
								abIndex  = oldCoords.length;
								objIndex = abIndex / 3;

								System.arraycopy( oldCoords , 0 , abPointCoords , 0 , abIndex );
							}

							while ( objIndex < objVertexCount )
							{
								final Vector3D point = (Vector3D)objVertices.get( objIndex++ );

								abPointCoords[ abIndex++ ] = point.x;
								abPointCoords[ abIndex++ ] = point.y;
								abPointCoords[ abIndex++ ] = point.z;
							}
						}

						final boolean abTextureIsMapped;
						final double  abTextureWidth;
						final double  abTextureHeight;
						{
							final boolean hasTexture = abTexture.isTexture();
							final int     width      = hasTexture ? abTexture.getTextureWidth ( null ) : -1;
							final int     height     = hasTexture ? abTexture.getTextureHeight( null ) : -1;

							abTextureIsMapped = ( ( width > 0 ) && ( height > 0 ) );
							abTextureWidth    = (double)width;
							abTextureHeight   = (double)height;
						}

						int[]  abTextureU = null;
						int[]  abTextureV = null;

						for ( int argIndex = 1 ; argIndex <= argCount ; argIndex++ )
						{
							final String arg = tokens[ argIndex ];

							final Matcher matcher = POLYGON_VERTEX_PATTERN.matcher( arg );
							if ( !matcher.matches() )
								throw new IOException( "malformed face argument in: " + line );

							final int objVertexIndex        = Integer.parseInt( matcher.group( 1 ) );
							final int objTextureVertexIndex = ( matcher.group( 3 ) != null ) ? Integer.parseInt( matcher.group( 3 ) ) : 0;
							final int objVertexNormalIndex  = ( matcher.group( 5 ) != null ) ? Integer.parseInt( matcher.group( 5 ) ) : 0;

							final int abFaceVertexIndex  = argCount - argIndex;
							final int abPointIndex       = objVertexIndex - 1;
							final int abPointIndexTimes3 = abPointIndex * 3;

							if ( abPointIndexTimes3 >= abPointCoordsLength )
								throw new IOException( "face references non-existing vertex (vertex=" + objVertexIndex + ", abPoints=" + ( abPointCoords.length / 3 ) + ", objVertices=" + objVertices.size() + ")" );

							if ( abTextureIsMapped && ( objTextureVertexIndex > 0 ) )
							{
								if ( abTextureU == null )
								{
									final int faceVertexCount = abFacePointIndices.length;

									abTextureU = new int[ faceVertexCount ];
									abTextureV = new int[ faceVertexCount ];
								}

								final Vector3D textureVertex = (Vector3D)objTextureVertices.get( objTextureVertexIndex - 1 );
								abTextureU[ abFaceVertexIndex ] = Math.round( (float)( textureVertex.x * abTextureWidth  ) );
								abTextureV[ abFaceVertexIndex ] = Math.round( (float)( textureVertex.y * abTextureHeight ) );
							}

							if ( objVertexNormalIndex > 0 )
							{
								if ( ( abVertexNormals == null ) || ( abVertexNormals.length < abPointCoordsLength ) )
								{
									if ( objVertexNormals.size() < 1 )
										throw new IOException( "vertex normal used before normal declaration" );

									final double[] oldNormals = abVertexNormals;
									abVertexNormals = new double[ abPointCoordsLength ];

									if ( oldNormals != null )
										System.arraycopy( oldNormals , 0 , abVertexNormals , 0 , oldNormals.length );
								}

								final Vector3D vertexNormal = (Vector3D)objVertexNormals.get( objVertexNormalIndex - 1 );

								abVertexNormals[ abPointIndexTimes3     ] = vertexNormal.x;
								abVertexNormals[ abPointIndexTimes3 + 1 ] = vertexNormal.y;
								abVertexNormals[ abPointIndexTimes3 + 2 ] = vertexNormal.z;
							}

							abFacePointIndices[ abFaceVertexIndex ] = abPointIndex;
						}

						result.addFace( abFacePointIndices , abTexture , abTextureU , abTextureV , 1.0f , true , false );
					}
					/*
					 * g group_name1 group_name2 . . .
					 *
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Specifies the group name for the elements that follow it. You can
					 *     have multiple group names. If there are multiple groups on one
					 *     line, the data that follows belong to all groups. Group information
					 *     is optional.
					 *
					 *     group_name is the name for the group. Letters, numbers, and
					 *     combinations of letters and numbers are accepted for group names.
					 *     The default group name is default.
					 */
					else if ( "g".equals( name ) )
					{
//						if ( argCount < 1 )
//							throw new IOException( "too few group arguments in: " + line );

						// groups = ArrayTools.remove( tokens , 0 , 0 );
					}
					/*
					 * mtllib filename1 filename2 . . .
					 *
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Specifies the material library file for the material definitions
					 *     set with the usemtl statement. You can specify multiple filenames
					 *     with mtllib. If multiple filenames are specified, the first file
					 *     listed is searched first for the material definition, the second
					 *     file is searched next, and so on.
					 *
					 *     When you assign a material library using the Model program, only
					 *     one map library per .obj file is allowed. You can assign multiple
					 *     libraries using a text editor.
					 *
					 *     filename is the name of the library file that defines the
					 *     materials.  There is no default.
					 *
					 */
					else if ( "mtllib".equals( name ) )
					{
						if ( argCount < 1 )
							throw new IOException( "too few material library arguments in: " + line );

						// mtllib = ArrayTools.remove( tokens , 0 , 0 );
					}
					/*
					 * o object_name
					 *
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Optional statement; it is not processed by any Wavefront programs.
					 *     It specifies a user-defined object name for the elements defined
					 *     after this statement.
					 *
					 *     object_name is the user-defined object name. There is no default.
					 */
					else if ( "o".equals( name ) ) // object name (e.g. "o complete_lavalamp.lwo")
					{
						final String objectName = getStringAfter( line , tokens , 1 );
						result.setTag( objectName );
					}
					/*
					 * usemtl material_name
					 *
					 *     Polygonal and free-form geometry statement.
					 *
					 *     Specifies the material name for the element following it. Once a
					 *     material is assigned, it cannot be turned off; it can only be
					 *     changed.
					 *
					 *     material_name is the name of the material. If a material name is
					 *     not specified, a white material is used.
					 *
					 */
					else if ( "usemtl".equals( name ) )
					{
						if ( argCount < 1 )
							throw new IOException( "malformed 'usemtl' entry: " + line );

						String material = getStringAfter( line , tokens , 1 );
						material = material.toLowerCase();
						material = material.replace( ' ' , '_' );

						abTexture = (TextureSpec)actualTextureMap.get( material );
						if ( abTexture == null )
						{
							abTexture = defaultTexture;
							//System.err.println( "'usemtl' references unknown material '" + material + "'" );
						}
					}
//					else
//					{
//						System.err.println( "unrecognized entry: " + line );
//					}
				}
				catch ( NumberFormatException e )
				{
					throw new IOException( "malformed numeric value: " + line );
				}
			}
		}

//		System.out.println( " - OBJ file loaded succesfully" );

		result.setPointCoords( abPointCoords );

		if ( abVertexNormals != null )
		{
			/*
			 * If we have vertex normals, consider every face to be 'smooth'
			 * when all its vertices have normals, but not all of them are the
			 * same.
			 */
			for ( int faceIndex = 0 ; faceIndex < result.getFaceCount() ; faceIndex++ )
			{
				final Face3D face             = result.getFace( faceIndex );
				final int[]  facePointIndices = face.getPointIndices();
				final int    faceVertexCount  = facePointIndices.length;

				if ( faceVertexCount > 2 )
				{
					boolean haveAll  = ( faceVertexCount > 2 );
					boolean allSame  = true;
					double  firstVNX = 0.0;
					double  firstVNY = 0.0;
					double  firstVNZ = 0.0;

					for ( int faceVertexIndex = 0 ; haveAll && ( faceVertexIndex < faceVertexCount ) ; faceVertexIndex++ )
					{
						final int vi = facePointIndices[ faceVertexIndex ] * 3;

						final double vnx = abVertexNormals[ vi     ];
						final double vny = abVertexNormals[ vi + 1 ];
						final double vnz = abVertexNormals[ vi + 2 ];

						haveAll = ( vnx != 0.0 ) && ( vny != 0.0 ) && ( vnz != 0.0 );
						if ( haveAll )
						{
							if ( faceVertexIndex == 0 )
							{
								firstVNX = vnx;
								firstVNY = vny;
								firstVNZ = vnz;
							}
							else
							{
								allSame &= ( ( vnx == firstVNX ) && ( vny == firstVNY ) && ( vnz == firstVNZ ) );
							}
						}
					}

					face.setSmooth( haveAll && !allSame );
				}
				else
				{
					face.setSmooth( false );
				}
			}

			result.setVertexNormals( abVertexNormals );
		}

		return result;
	}

	/**
	 * Get string argument starting at the specified token.
	 *
	 * @param   line        Line from OBJ file.
	 * @param   tokens      Tokens from line.
	 * @param   startIndex  Index in tokens where the string argument starts.
	 *
	 * @return  String argument (trailing whitespace removed).
	 *
	 * @throws  NullPointerException if <code>line</code> or <code>tokens</code>
	 *          is <code>null</code>
	 * @throws  IllegalArgumentException if the <code>startIndex</code> is out
	 *          of range.
	 */
	private static String getStringAfter( final String line , final String[] tokens , final int startIndex )
	{
		if ( ( startIndex < 0 ) || ( startIndex >= tokens.length ) )
			throw new IllegalArgumentException( "invalid start index: " + startIndex );

		int start = 0;
		for ( int i = 0 ; i < startIndex ; i++ )
			start = line.indexOf( tokens[ i ] , start ) + tokens[ i ].length();

		while ( Character.isWhitespace( line.charAt( start ) ) )
			start++;

		int end = line.length();
		if ( ( end > start ) && Character.isWhitespace( line.charAt( end - 1 ) ) )
			end--;

		return line.substring( start , end );
	}


	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private ObjLoader()
	{
	}
}
