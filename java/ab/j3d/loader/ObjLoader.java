/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2006-2009
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

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.ResourceLoaderMaterial;
import ab.j3d.Vector3D;
import ab.j3d.Vector3f;
import ab.j3d.geom.Abstract3DObjectBuilder;
import ab.j3d.model.Object3D;
import ab.j3d.model.Object3DBuilder;

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
	 * Materials from OBJ MTL file.
	 */
	private static final Map<String,Material> objMaterials = new HashMap<String,Material>();

	/**
	 * Default materials to use for OBJ files.
	 */
	private static final Map<String,Material> DEFAULT_MATERIALS;

	/**
	 * Load the specified OBJ file.
	 *
	 * @param   transform       Transormation to apply to the OBJ (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   loader          {@link ResourceLoader} to load OBJ models from.
	 * @param   objFileName     Name of OBJ modelfile to be loaded.
	 *
	 * @return  {@link Object3D} with loaded OBJ file.
	 *
	 * @throws  IOException if an error occured while loading the OBJ file.
	 */
	public static Object3D load( final Matrix3D transform , final ResourceLoader loader , final String objFileName )
		throws IOException
	{
		final Object3DBuilder builder = new Object3DBuilder();
		final String objectName = load( builder, transform, loader, objFileName );
		final Object3D result = builder.getObject3D();
		result.setTag( objectName );
		return result;
	}

	/**
	 * Load the specified OBJ file.
	 *
	 * @param   builder         Builder of resulting 3D object.
	 * @param   transform       Transormation to apply to the OBJ (mostly used
	 *                          to for scaling and axis alignment).
	 * @param   loader          {@link ResourceLoader} to load OBJ models from.
	 * @param   objFileName     Name of OBJ modelfile to be loaded.
	 *
	 * @return  Object name defined in OBJ file.
	 *
	 * @throws  IOException if an error occured while loading the OBJ file.
	 */
	public static String load( final Abstract3DObjectBuilder builder , final Matrix3D transform , final ResourceLoader loader , final String objFileName )
		throws IOException
	{
		if ( loader == null )
			throw new NullPointerException( "loader" );

		final Map<String,Material> actualMaterials = DEFAULT_MATERIALS;
		final Material defaultMaterial = actualMaterials.containsKey( "default" ) ? actualMaterials.get( "default" ) : new Material( 0xFFC0C0C0 );

		/*
		 * Read OBJ data
		 */
		final List<Vector3D> vertices = new ArrayList<Vector3D>();
		final List<Point2D.Float> textureVertices = new ArrayList<Point2D.Float>();
		final List<Vector3D> vertexNormals = new ArrayList<Vector3D>();
		final List<ObjFace> faces = new ArrayList<ObjFace>();

		Material material = defaultMaterial;

		String objectName = null;

		String line;
		final InputStream inputStream = loader.getResource( objFileName );
		if ( inputStream == null )
			throw new FileNotFoundException( objFileName );

		final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
		while ( ( line = readLine( bufferedReader ) ) != null )
		{
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

						vertices.add( transform.transform( x , y , z ) );
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

						final float u = Float.parseFloat( tokens[ 1 ] );
						final float v = Float.parseFloat( tokens[ 2 ] );
						final float w = ( argCount >= 3 ) ? Float.parseFloat( tokens[ 3 ] ) : 0.0f;

						textureVertices.add( new Vector3f( u , v , w ) );
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

						vertexNormals.add( transform.rotate( i , j , k ) );
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
					else if ( "p".equals( name ) ||
					          "l".equals( name ) ||
					          "f".equals( name ) )
					{
						if ( argCount < 1 )
							throw new IOException( "too few face arguments in: " + line );

						final int objVertexCount = vertices.size();
						if ( objVertexCount < 1 )
							throw new IOException( "vertex used before vertex declaration" );

						final List<ObjFaceVertex> faceVertices = new ArrayList<ObjFaceVertex>( argCount - 1 );

						for ( int argIndex = 1 ; argIndex <= argCount ; argIndex++ )
						{
							final String arg = tokens[ argIndex ];

							final Matcher matcher = POLYGON_VERTEX_PATTERN.matcher( arg );
							if ( !matcher.matches() )
								throw new IOException( "malformed face argument in: " + line );

							final int vertexIndex        = Integer.parseInt( matcher.group( 1 ) ) - 1;
							final int textureVertexIndex = ( matcher.group( 3 ) != null ) ? Integer.parseInt( matcher.group( 3 ) ) - 1 : -1;
							final int vertexNormalIndex  = ( matcher.group( 5 ) != null ) ? Integer.parseInt( matcher.group( 5 ) ) - 1 : -1;

							faceVertices.add( new ObjFaceVertex( vertexIndex , textureVertexIndex , vertexNormalIndex ) );
						}

						faces.add( new ObjFace( faceVertices , material ) );
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
						loadMaterial( loader , line.substring( 7 ) );
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
						objectName = getStringAfter( line , tokens , 1 );
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
						actualMaterials.putAll( objMaterials );
						if ( argCount < 1 )
							throw new IOException( "malformed 'usemtl' entry: " + line );

						String materialName = getStringAfter( line , tokens , 1 );
						materialName = materialName.replace( ' ' , '_' );
						material = actualMaterials.get( materialName );
						if ( materialName == null )
						{
							material = defaultMaterial;
							System.err.println( "'usemtl' references unknown material '" + materialName + "'" );
						}
					}
/*
					else
					{
						System.err.println( "unrecognized entry: " + line );
					}
*/
				}
				catch ( NumberFormatException e )
				{
					throw new IOException( "malformed numeric value: " + line );
				}
			}
		}

//		System.out.println( " - OBJ file loaded succesfully" );

		builder.setVertexCoordinates( vertices );

		List<Vector3D> assignedVertexNormals = null;

		for ( final ObjFace objFace : faces )
		{
			final List<ObjFaceVertex> faceVertices = objFace._vertices;
			final int faceVertexCount = faceVertices.size();

			final int[] vertexIndices = new int[faceVertexCount];
			Point2D.Float[] texturePoints = null;
			Vector3D[] faceVertexNormals = null;
			boolean smooth = false;
			Vector3D fixedVertexNormal = null;

			for ( int faceVertexIndex = 0 ; faceVertexIndex < faceVertexCount ; faceVertexIndex++ )
			{
				final ObjFaceVertex objFaceVertex = faceVertices.get( faceVertexCount - faceVertexIndex - 1 );

				final int vertexIndex = objFaceVertex._vertexIndex;
				if ( vertexIndex >= vertices.size() )
					throw new IOException( "out-of-bounds vertex (" + vertexIndex + " >= " + vertices.size() + ")" );

				vertexIndices[ faceVertexIndex ] = vertexIndex;

				final int textureVertexIndex = objFaceVertex._textureVertexIndex;
				if ( textureVertexIndex >= 0 )
				{
					if ( textureVertexIndex >= textureVertices.size() )
						throw new IOException( "out-of-bounds texture vertex (" + textureVertexIndex + " >= " + textureVertices.size() + ")" );

					if ( texturePoints == null )
					{
						texturePoints = new Point2D.Float[ faceVertexCount ];
					}

					texturePoints[ faceVertexIndex ] = textureVertices.get( textureVertexIndex );
				}

				final int vertexNormalIndex = objFaceVertex._vertexNormalIndex;
				if ( vertexNormalIndex >= 0 )
				{
					if ( vertexNormalIndex >= vertexNormals.size() )
						throw new IOException( "out-of-bounds vertex normal (" + vertexNormalIndex + " >= " + vertexNormals.size() + ")" );

					final Vector3D vertexNormal = vertexNormals.get( vertexNormalIndex );

					if ( faceVertexNormals == null )
					{
						faceVertexNormals = new Vector3D[ faceVertexCount ];
					}

					if ( assignedVertexNormals == null )
					{
						assignedVertexNormals = new ArrayList<Vector3D>( vertices.size() );
						for ( int i = vertices.size() ; --i >= 0 ; )
						{
							assignedVertexNormals.add( Vector3D.ZERO );
						}
					}

					assignedVertexNormals.set( vertexIndex , vertexNormal );

					if ( fixedVertexNormal == null )
					{
						fixedVertexNormal = vertexNormal;
					}
					else
					{
						smooth |= !fixedVertexNormal.equals( vertexNormal );
					}

					faceVertexNormals[ faceVertexIndex ] = vertexNormal;
				}
			}

			builder.addFace( vertexIndices , objFace._material , texturePoints , faceVertexNormals , smooth , false );
		}

		if ( assignedVertexNormals != null )
		{
			builder.setVertexNormals( assignedVertexNormals );
		}

		return objectName;
	}

	static
	{
		final Map<String,Material> materials = new HashMap<String,Material>();

		/* default material (also used for unknown materials) */
		materials.put( "default"       , new Material( 0xFFC0C0C0 ) );

		/* basic colors */
		materials.put( "black"         , new Material( 0xFF000000 ) );
		materials.put( "blue"          , new Material( 0xFF0000FF ) );
		materials.put( "green"         , new Material( 0xFF00FF00 ) );
		materials.put( "cyan"          , new Material( 0xFF00FFFF ) );
		materials.put( "red"           , new Material( 0xFFFF0000 ) );
		materials.put( "magenta"       , new Material( 0xFFFF00FF ) );
		materials.put( "yellow"        , new Material( 0xFFFFFF00 ) );
		materials.put( "white"         , new Material( 0xFFFCFCFC ) );

		/* materials */
//		materials.put( "brass"         , new Material( 0xFFE0E010 ) );
//		materials.put( "glass"         , new Material( 0x20102010 ) );
//		materials.put( "light"         , new Material( 0x80FFFF20 ) );
//		materials.put( "metal"         , new Material( 0xFFE0E0F8 ) );
//		materials.put( "plastic"       , new Material( 0xFFC0C0C0 ) );
//		materials.put( "porcelin"      , new Material( 0xFFFFFFFF ) );
//		materials.put( "steel"         , new Material( 0xFFD0D0E8 ) );
//		materials.put( "white_plastic" , new Material( 0xFFC0C0C0 ) );
//		materials.put( "wood"          , new Material( 0xFF603820 ) );

		DEFAULT_MATERIALS = materials;
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

	/**
	 * Loads a MTL file with materials used by the OBJ file.
	 * Materials are stored in objMaterials.
	 *
	 *  @param  loader          Resource loader to load textures from.
	 *  @param  materialName    Name of the the MTL file.
	 *
	 *  @throws  IOException when material could not be loaded or contains malformed known entries. Unknown entries are ignored, e.g. "Ka foobar" will throw an exception,
	 *                              but "Kgt foobar" won't because Kgt is not a known MTL entry.
	 */
	private static void loadMaterial( final ResourceLoader loader , final String materialName )
		throws IOException
	{
		String line;
		ResourceLoaderMaterial tempMaterial = null;
		final BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( loader.getResource( materialName ) ) );
		while ( ( line = readLine( bufferedReader ) ) != null )
		{
			if ( line.length() > 0 )
			{
				final String[] tokens   = TextTools.tokenize( line , ' ' );
				final String   name     = tokens[ 0 ];
				final int argCount      = tokens.length - 1;
				try
				{
					// New Material
					if ( "newmtl".equals( name ) )
					{
						if ( argCount < 1 )
						    throw new IOException( "Malformed material entry: " + line );
						tempMaterial = new ResourceLoaderMaterial( loader );
						tempMaterial.code = ( tokens[ 1 ] );
						objMaterials.put( ( tokens[ 1 ] ) , tempMaterial );
					}
					// Ambient lightning
					else if ( "Ka".equals( name ) )
					{
						if ( argCount < 3 )
						    throw new IOException( "Malformed ambient lightning entry: " + line );
						tempMaterial.ambientColorRed   = Float.valueOf( tokens[ 1 ] );
						tempMaterial.ambientColorGreen = Float.valueOf( tokens[ 2 ] );
						tempMaterial.ambientColorBlue  = Float.valueOf( tokens[ 3 ] );
					}
					// Diffuse lightning
					else if ( "Kd".equals( name ) )
					{
						if ( argCount < 3 )
						    throw new IOException( "Malformed diffuse lightning entry: " + line );
						tempMaterial.diffuseColorRed   = Float.valueOf( tokens[ 1 ] );
						tempMaterial.diffuseColorGreen = Float.valueOf( tokens[ 2 ] );
						tempMaterial.diffuseColorBlue  = Float.valueOf( tokens[ 3 ] );
					}
					// Specular lightning
					else if ( "Ks".equals( name ) )
					{
						if ( argCount < 3 )
						    throw new IOException( "Malformed specular lightning entry: " + line );
						tempMaterial.specularColorRed   = Float.valueOf( tokens[ 1 ] );
						tempMaterial.specularColorGreen = Float.valueOf( tokens[ 2 ] );
						tempMaterial.specularColorBlue  = Float.valueOf( tokens[ 3 ] );
					}
					// Shininess
					else if ( "Ns".equals( name ) )
					{
						if ( argCount < 1 )
						    throw new IOException( "Malformed shininess entry: " + line );
						final float shininess = Float.parseFloat( tokens[ 1 ] );
						// value range is from 0 to 1000
						if ( shininess > 1000.0f )
						{
							// set material shininess to max
							tempMaterial.shininess = 128;
						}
						else
						{
							tempMaterial.shininess = (int)( shininess * 128.0f / 1000.0f );
						}
					}
					// Alpha blending
					else if ( "d".equals( name ) || "Tr".equals( name ) )
					{
						if ( argCount < 1 )
						    throw new IOException( "Malformed transparency entry: " + line );
						tempMaterial.diffuseColorAlpha = Float.parseFloat( tokens[ 1 ] );
					}
					// Texture mapping
					else if ( "map_Kd".equals( name ) || "map_Ka".equals( name ) || "bump".equals( name ) )
					{
						if ( argCount < 1 )
						    throw new IOException( "Malformed texture entry: " + line );
						tempMaterial.colorMap = ( tokens[ 1 ] );
					}
					//Non-recognized, non-# (comment) line.
					//@TODO: Implement following MTL Lines:

					else
					{
						//System.err.println( "### Ignoring MTL line: " + line );
					}
				}
				catch ( NumberFormatException e )
				{
					throw new IOException( "malformed numeric value: " + line );
				}
			}
		}
		bufferedReader.close();
	}

	/**
	 * Reads line from supplied BufferedReader containing OBJ or MTL files.
	 *      Functions :
	 *      Ignores rest of line after "#",
	 *      Removes trailing "\" (WaveFront line split) and adds next line to current line ( repeating process for multiple splitted line ).
	 *      Removes double or more whitespaces and replaces them with a single whitespace
	 *
	 * @param   bufferedReader      The {@link BufferedReader} containing file to read from.
	 *
	 * @return  Next line read from the {@link BufferedReader} or null on EOF.
	 *
	 * @throws  IOException  if next line could not be read.
	 */
	private static String readLine( final BufferedReader bufferedReader )
		throws IOException
	{
		String line = bufferedReader.readLine();
		if ( line != null )
		{
		final int hash = line.indexOf( (int) '#' );
			if ( hash >= 0 )
				line = line.substring( 0 , hash );
			line = line.trim();
			while ( line.length() > 0 && line.charAt( line.length() - 1 ) == '\\' )
			{
				line = MessageFormat.format( "{0} {1}" , line.substring( 0 , line.length() - 1 ) , bufferedReader.readLine() );
			}
			line = line.replaceAll( "\\s+" , " " );
		}
		return line;
	}

	/**
	 * This class is used to represent a face in an OBJ file.
	 */
	private static class ObjFace
	{
		/**
		 * Vertices in face.
		 */
		private final List<ObjFaceVertex> _vertices;

		/**
		 * Material of face.
		 */
		private final Material _material;

		/**
		 * Construct face.
		 * @param   vertices    Vertices in face.
		 * @param   material    Material of face.
		 */
		private ObjFace( final List<ObjFaceVertex> vertices, final Material material )
		{
			_vertices = vertices;
			_material= material;
		}
	}

	/**
	 * This class is used to represent a vertex of a face in an OBJ file.
	 */
	private static class ObjFaceVertex
	{
		/**
		 * Index of vertex (0=first).
		 */
		private final int _vertexIndex;

		/**
		 * Index of texture vertex (0=first, -1=undefined).
		 */
		private final int _textureVertexIndex;

		/**
		 * Index of vertex normal (0=first, -1=undefined).
		 */
		private final int _vertexNormalIndex;

		/**
		 * Construct vertex.
		 *
		 * @param   vertexIndex         Index of vertex (0=first).
		 * @param   textureVertexIndex  Index of texture vertex (0=first, -1=undefined).
		 * @param   vertexNormalIndex   Index of vertex normal (0=first, -1=undefined).
		 */
		private ObjFaceVertex( final int vertexIndex, final int textureVertexIndex, final int vertexNormalIndex )
		{
			_vertexIndex = vertexIndex;
			_textureVertexIndex = textureVertexIndex;
			_vertexNormalIndex = vertexNormalIndex;
		}
	}
}
