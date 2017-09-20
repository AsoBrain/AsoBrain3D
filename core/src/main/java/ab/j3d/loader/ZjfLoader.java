/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2016 Peter S. Heijnen
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
 */
package ab.j3d.loader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import ab.j3d.*;
import ab.j3d.appearance.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import static java.io.StreamTokenizer.*;
import org.jetbrains.annotations.*;

/**
 * Loader for ZJF/ZJV files. This is a propriety format used by Cadenas
 * <a href="http://portal-de.partcommunity.com/">PARTserver</a>.
 *
 * @author  Peter S. Heijnen
 */
public class ZjfLoader
{
	/**
	 * Load the specified ZJF/ZJV file.
	 *
	 * @param   url     URL to file.
	 *
	 * @return  {@link Node3D}..
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public static Node3D load( @NotNull final URL url )
		throws IOException
	{
		return convertAssemblyToNode3D( parse( url ) );
	}

	/**
	 * Load the specified ZJF file.
	 *
	 * @param   reader  ZJF file reader.
	 *
	 * @return  {@link Node3D}.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public static Node3D load( @NotNull final Reader reader )
		throws IOException
	{
		return convertAssemblyToNode3D( parse( reader ) );
	}

	/**
	 * Convert {@link Assembly} to {@link Node3D}.
	 *
	 * @param   assembly    Assembly to convert.
	 *
	 * @return  {@link Node3D}.
	 */
	public static Node3D convertAssemblyToNode3D( final Assembly assembly )
	{
		final Node3D result = new Node3D();

		final List<Part> parts = assembly._partDefinitions;
		final List<Integer> partlist = assembly._partList;
		final List<Matrix3D> matrixlist = assembly._matrixList;

		final List<Object3D> convertedParts = new ArrayList<Object3D>( parts.size() );
		for ( final Part part : parts )
		{
			convertedParts.add( convertPartToObject3D( part ) );
		}

		for ( int i = 0 ; i < partlist.size() ; i++ )
		{
			final int partNr = partlist.get( i );
			final Part part = parts.get( partNr );

			final Transform3D insert = new Transform3D( matrixlist.get( i ), convertedParts.get( partNr ) );
			insert.setTag( part.name );
			result.addChild( insert );
		}

		return result;
	}

	/**
	 * Convert {@link Part} to {@link Object3D}.
	 *
	 * @param   part    Part to convert.
	 *
	 * @return  {@link Object3D}.
	 */
	public static Object3D convertPartToObject3D( final Part part )
	{
		final List<Vector3D> partVertices = part.vertexCoordinates;
		final List<Vector3D> partNormals = part.normals;

		final Object3DBuilder builder = new Object3DBuilder();
		builder.setVertexCoordinates( partVertices );

		for ( final Triangle triangle : part.triangles )
		{
			final int[] vertices = triangle.vertices;
			final int[] normalList = triangle.normalList;

			final Vector3D[] vertexNormals = ( normalList != null ) ? new Vector3D[] { partNormals.get( normalList[ 0 ] ), partNormals.get( normalList[ 1 ] ), partNormals.get( normalList[ 2 ] ) } : null;
			final Appearance appearance = BasicAppearance.createForColor( null, triangle.color );

			builder.addFace( vertices, appearance, null, vertexNormals, false, true );
		}

		return builder.getObject3D();
	}

	/**
	 * Parse ZJF/ZJV file specified by URL.
	 *
	 * @param   url	 URL to file.
	 *
	 * @return  {@link Assembly} that was parsed.
	 *
	 * @throws  IOException if an I/O or parse error occurs.
	 */
	public static Assembly parse( final URL url )
		throws IOException
	{

		final Assembly result;

		final InputStream is = url.openStream();
		try
		{
			final String file = url.getFile();
			if ( file.endsWith( ".zjv" ) || file.endsWith( ".ZJV" ) )
			{
				final ZipInputStream zipInputStream = new ZipInputStream( is );
				try
				{
					zipInputStream.getNextEntry();
					final BufferedReader reader = new BufferedReader( new InputStreamReader( zipInputStream ), 131072 );
					try
					{
						result = parse( reader );
					}
					finally
					{
						reader.close();
					}
				}
				finally
				{
					zipInputStream.close();
				}
			}
			else
			{
				final BufferedReader reader = new BufferedReader( new InputStreamReader( is ), 131072 );
				try
				{
					result = parse( reader );
				}
				finally
				{
					reader.close();
				}
			}
		}
		finally
		{
			is.close();
		}

		return result;
	}

	/**
	 * Parse ZJF file from reader.
	 *
	 * @param   reader  ZJF file reader.
	 *
	 * @return  {@link Assembly} that was parsed.
	 *
	 * @throws  IOException if an I/O or parse error occurs.
	 */
	public static Assembly parse( final Reader reader )
		throws IOException
	{
		final Assembly assembly = new Assembly();

		final StreamTokenizer stream = new StreamTokenizer( reader );

		stream.resetSyntax();
		stream.wordChars( (int) 'a', (int) 'z' );
		stream.wordChars( (int) 'A', (int) 'Z' );
		stream.wordChars( (int) '0', (int) '9' );
		stream.wordChars( 160, 255 );
		stream.whitespaceChars( 0, 32 );
		stream.commentChar( (int) '/' );
		stream.quoteChar( (int) '"' );
		stream.quoteChar( (int) '\'' );
		stream.quoteChar( (int) '"' );
		stream.ordinaryChar( (int)'.' );
		stream.ordinaryChar( (int)'-' );
		stream.wordChars( (int)'.', (int)'.' );
		stream.wordChars( (int)'-', (int)'-' );
		stream.quoteChar( (int) '"' );

		Part currentPart = null;

		int state = START;
		int nextState = START;
		do
		{
			stream.nextToken();

			switch ( state )
			{
				case READ_OPEN:
					if ( stream.ttype != (int)'{' )
					{
						return null;
					}
					state = nextState;
					break;

				case READ_CLOSE:
					if ( stream.ttype != (int)'}' )
					{
						return null;
					}
					state = nextState;
					break;

				case START:
					if ( stream.ttype == TT_EOF )
					{
						continue;
					}

					if ( ( stream.ttype == TT_WORD ) && ( "P".equals( stream.sval ) ) )
					{
						currentPart = new Part();
						assembly._partDefinitions.add( currentPart );
						state = READ_OPEN;
						nextState = IN_PART;
					}

					if ( ( stream.ttype != TT_WORD ) || !( "S".equals( stream.sval ) ) )
					{
						continue;
					}

					currentPart = new Part();
					state = READ_OPEN;
					nextState = IN_STRUCT;
					break;

				case IN_STRUCT:
					if ( ( stream.ttype == TT_WORD ) && "N".equals( stream.sval ) )
					{
						state = READ_OPEN;
						nextState = READ_STRUCT_NAME;
					}
					else if ( ( stream.ttype == TT_WORD ) && "M".equals( stream.sval ) )
					{
						state = READ_OPEN;
						nextState = READ_MEMBER_MATRIX;
					}
					else if ( ( stream.ttype == TT_WORD ) && "ME".equals( stream.sval ) )
					{
						state = READ_OPEN;
						nextState = READ_EXPLOSION_MATRIX;
					}
					else if ( stream.ttype == (int)'}' )
					{
						state = START;
					}
					break;

				case READ_MEMBER_MATRIX:
				{
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int partNr = (int)getDoubleValue( stream );
					stream.nextToken(); final double ax = getDoubleValue( stream );
					stream.nextToken(); final double ay = getDoubleValue( stream );
					stream.nextToken(); final double az = getDoubleValue( stream );
					stream.nextToken(); final double bx = getDoubleValue( stream );
					stream.nextToken(); final double by = getDoubleValue( stream );
					stream.nextToken(); final double bz = getDoubleValue( stream );
					stream.nextToken(); final double cx = getDoubleValue( stream );
					stream.nextToken(); final double cy = getDoubleValue( stream );
					stream.nextToken(); final double cz = getDoubleValue( stream );
					stream.nextToken(); final double tx = getDoubleValue( stream );
					stream.nextToken(); final double ty = getDoubleValue( stream );
					stream.nextToken(); final double tz = getDoubleValue( stream );

					assembly._matrixList.add( new Matrix3D( ax, bx, cx, tx, ay, by, cy, ty, az, bz, cz, tz ) );
					assembly._partList.add( Integer.valueOf( partNr ) );

					state = READ_MEMBER;
					break;
				}

				case READ_EXPLOSION_MATRIX:
				{
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int partNr = (int) getDoubleValue( stream );
					stream.nextToken(); final double ax = getDoubleValue( stream );
					stream.nextToken(); final double ay = getDoubleValue( stream );
					stream.nextToken(); final double az = getDoubleValue( stream );
					stream.nextToken(); final double bx = getDoubleValue( stream );
					stream.nextToken(); final double by = getDoubleValue( stream );
					stream.nextToken(); final double bz = getDoubleValue( stream );
					stream.nextToken(); final double cx = getDoubleValue( stream );
					stream.nextToken(); final double cy = getDoubleValue( stream );
					stream.nextToken(); final double cz = getDoubleValue( stream );
					stream.nextToken(); final double tx = getDoubleValue( stream );
					stream.nextToken(); final double ty = getDoubleValue( stream );
					stream.nextToken(); final double tz = getDoubleValue( stream );

					assembly._explosionMatrixList.add( new Matrix3D( ax, bx, cx, tx, ay, by, cy, ty, az, bz, cz, tz ) );

					state = READ_MEMBER;
					break;
				}

				case READ_MEMBER:
					state = IN_STRUCT;
					break;

				case IN_PART:
					if ( ( stream.ttype == TT_WORD ) && ( "N".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_NAME;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "C".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_COL;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "MID".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_MID;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "PL".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_POINT_LIST;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "FL3".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_FACE_LIST3;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "NL".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_NORMAL_LIST;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "NL3".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_FACE_NORMAL_LIST3;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "CF".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_FACE_COLOR_LIST3;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "COLS".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_PART_COLS;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "LLF".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_HARD_POINTS;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "LLR".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_SOFT_POINTS;
					}
					else if ( ( stream.ttype == TT_WORD ) && ( "THRL".equals( stream.sval ) ) )
					{
						state = READ_OPEN;
						nextState = READ_THREAD_DATA;
					}
					else if ( stream.ttype == (int)'}' )
					{
						state = READY_PART;
					}
					break;

				case READY_PART:
					stream.pushBack();
					state = 0;
					break;

				case READ_PART_POINT_LIST:
					stream.pushBack();
					do
					{
						stream.nextToken();
						if ( stream.ttype == (int)'}' )
						{
							continue;
						}
						final double v1 = getDoubleValue( stream );
						stream.nextToken();
						final double v2 = getDoubleValue( stream );
						stream.nextToken();
						final double v3 = getDoubleValue( stream );

						currentPart.vertexCoordinates.add( new Vector3D( v1, v2, v3 ) );
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_PART_FACE_LIST3:
					stream.pushBack();

					do
					{
						stream.nextToken();

						if ( stream.ttype == (int)'}' )
						{
							continue;
						}
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}

						final int v1 = (int) getDoubleValue( stream );

						stream.nextToken();
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}

						final int v2 = (int) getDoubleValue( stream );

						stream.nextToken();
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}

						final int v3 = (int) getDoubleValue( stream );

						currentPart.triangles.add( new Triangle( currentPart, v1, v2, v3, currentPart.color ) );
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_PART_NORMAL_LIST:
					stream.pushBack();

					do
					{
						stream.nextToken();
						if ( stream.ttype == (int)'}' )
						{
							continue;
						}
						final double v1 = getDoubleValue( stream );
						stream.nextToken();
						final double v2 = getDoubleValue( stream );
						stream.nextToken();
						final double v3 = getDoubleValue( stream );

						currentPart.normals.add( new Vector3D( v1, v2, v3 ) );
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_PART_FACE_NORMAL_LIST3:
					stream.pushBack();

					int i = 0;
					do
					{
						stream.nextToken();
						if ( stream.ttype == (int)'}' )
						{
							continue;
						}
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}
						final int v1 = (int) getDoubleValue( stream );
						stream.nextToken();
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}
						final int v2 = (int) getDoubleValue( stream );
						stream.nextToken();
						if ( stream.ttype != TT_WORD )
						{
							return null;
						}
						final int v3 = (int) getDoubleValue( stream );

						final Triangle triangel = currentPart.triangles.get( i++ );
						triangel.normalList = new int[] { v1, v2, v3 };
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_PART_FACE_COLOR_LIST3:
					stream.pushBack();

					do
					{
						stream.nextToken();
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_PART_COLS:
					stream.pushBack();

					do
					{
						stream.nextToken();
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_HARD_POINTS:
					currentPart.hardPoints = new ArrayList<Integer>();
					stream.pushBack();

					do
					{
						stream.nextToken();
						if ( stream.ttype != (int)'}' )
						{
							final int val = (int) getDoubleValue( stream );
							currentPart.hardPoints.add( Integer.valueOf( val ) );
						}
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_SOFT_POINTS:
					currentPart.softPoints = new ArrayList<Integer>();
					stream.pushBack();

					do
					{
						stream.nextToken();
						if ( stream.ttype != (int)'}' )
						{
							final int val = (int) getDoubleValue( stream );
							currentPart.softPoints.add( Integer.valueOf( val ) );
						}
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;

				case READ_THREAD_DATA:
				{
					final List<Double> data = new ArrayList<Double>( 8 );
					stream.pushBack();

					do
					{
						stream.nextToken();
						if ( ( stream.ttype != (int)'}' ) && ( stream.sval != null ) )
						{
							data.add( new Double( stream.sval ) );
						}

						if ( data.size() == 8 )
						{
							final Double threadType = data.get( 0 );

							final Thread thread = new Thread();
							thread.type = threadType.intValue();
							thread.startPoint = new Vector3D( data.get( 1 ), data.get( 2 ), data.get( 3 ) );
							thread.endPoint = new Vector3D( data.get( 4 ), data.get( 5 ), data.get( 6 ) );
							thread.diameter = data.get( 7 );
							thread.calcHelix();
							currentPart.threads.add( thread );
							data.clear();
						}
					}
					while ( ( stream.ttype != (int)'}' ) && ( stream.ttype != TT_EOF ) );

					state = IN_PART;
					break;
				}

				case READ_PART_COL:
					int a = 0;

					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int red = (int) getDoubleValue( stream );

					stream.nextToken();
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int green = (int) getDoubleValue( stream );

					stream.nextToken();
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int blue = (int) getDoubleValue( stream );
					currentPart.color = new Color4f( red, green, blue );

					stream.nextToken();

					if ( stream.ttype == TT_WORD )
					{
						a = (int) getDoubleValue( stream );
					}
					else
					{
						stream.pushBack();
					}

					state = READ_CLOSE;

					currentPart.alpha = a;
					nextState = IN_PART;
					break;

				case READ_PART_MID:
					if ( stream.ttype != (int)'"' )
					{
						return null;
					}

					currentPart.mid = stream.sval;
					state = READ_CLOSE;
					nextState = IN_PART;
					break;

				case READ_PART_NAME:
					if ( stream.ttype != (int)'"' )
					{
						return null;
					}

					currentPart.name = stream.sval;
					state = READ_CLOSE;
					nextState = IN_PART;
					break;

				case READ_STRUCT_NAME:
					if ( stream.ttype != (int)'"' )
					{
						return null;
					}

					currentPart.name = stream.sval;
					state = READ_CLOSE;
					nextState = IN_STRUCT;
					break;

				case END:
			}
		}
		while ( stream.ttype != TT_EOF );

		return assembly;
	}

	/**
	 * Get double value from stream.
	 *
	 * @param   stream  Stream to read from.
	 *
	 * @return  Double value.
	 */
	private static double getDoubleValue( final StreamTokenizer stream )
	{
		final double result;

		if ( stream.ttype == TT_WORD )
		{
			final String val = stream.sval;
			result = Double.parseDouble( val );
		}
		else if ( stream.ttype == TT_NUMBER )
		{
			result = stream.nval;
		}
		else
		{
			result = 1.0;
		}

		return result;
	}

	/*
	 * Parser states.
	 */

	/** Parser state. */ private static final int START = 0;
	/** Parser state. */ private static final int END = 1;
	/** Parser state. */ private static final int IN_PART = 2;
	/** Parser state. */ private static final int READ_OPEN = 3;
	/** Parser state. */ private static final int READ_PART_NAME = 4;
	/** Parser state. */ private static final int READ_PART_COL = 5;
	/** Parser state. */ private static final int READ_PART_MID = 20;
	/** Parser state. */ private static final int READ_PART_FACE_LIST3 = 6;
	/** Parser state. */ private static final int READ_PART_POINT_LIST = 7;
	/** Parser state. */ private static final int READ_CLOSE = 8;
	/** Parser state. */ private static final int READY_PART = 9;
	/** Parser state. */ private static final int IN_STRUCT = 10;
	/** Parser state. */ private static final int READ_STRUCT_NAME = 11;
	/** Parser state. */ private static final int READ_MEMBER = 12;
	/** Parser state. */ private static final int READ_PART_NORMAL_LIST = 13;
	/** Parser state. */ private static final int READ_PART_FACE_NORMAL_LIST3 = 14;
	/** Parser state. */ private static final int READ_PART_FACE_COLOR_LIST3 = 21;
	/** Parser state. */ private static final int READ_HARD_POINTS = 15;
	/** Parser state. */ private static final int READ_SOFT_POINTS = 16;
	/** Parser state. */ private static final int READ_MEMBER_MATRIX = 17;
	/** Parser state. */ private static final int READ_EXPLOSION_MATRIX = 18;
	/** Parser state. */ private static final int READ_THREAD_DATA = 19;
	/** Parser state. */ private static final int READ_PART_COLS = 22;

	/**
	 * Assembly is the main element of a ZJF file.
	 */
	public static class Assembly
	{
		private List<Part> _partDefinitions = new ArrayList<Part>();
		private List<Integer> _partList = new ArrayList<Integer>();
		private List<Matrix3D> _matrixList = new ArrayList<Matrix3D>();
		private List<Matrix3D> _explosionMatrixList = new ArrayList<Matrix3D>();

		/**
		 * Get part definitions in this assembly.
		 *
		 * @return  List of part definitions (reference by part list).
		 */
		public List<Part> getPartDefinitions()
		{
			return Collections.unmodifiableList( _partDefinitions );
		}

		/**
		 * Get part usages. Each element is a part definition index. The
		 * matrix list should match this part list.
		 *
		 * @return  Part definition indices.
		 */
		public List<Integer> getPartList()
		{
			return Collections.unmodifiableList( _partList );
		}

		/**
		 * Get part transformation matrices. Each element is used to place a
		 * part in 3D space. This matrix list should match the part list.
		 *
		 * @return  Part transformation matrices.
		 */
		public List<Matrix3D> getMatrixList()
		{
			return Collections.unmodifiableList( _matrixList );
		}
	}

	public static class Part
	{
		private int alpha;
		private Color4f color;
		private String name;
		private List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>();
		private String mid;
		private List<Triangle> triangles = new ArrayList<Triangle>();
		private List<Vector3D> normals = new ArrayList<Vector3D>();
		private List<Integer> hardPoints;
		private List<Integer> softPoints;
		private List<Thread> threads = new ArrayList<Thread>();

		public String getName()
		{
			return name;
		}
	}

	private static class Triangle
	{
		Color4f color;
		int[] vertices;
		int[] normalList;
		Vector3D normal;

		private Triangle( final Part part, final int v1, final int v2, final int v3, final Color4f col )
		{
			vertices = new int[] { v1, v2, v3 };
			color = col;

			final Vector3D p1 = part.vertexCoordinates.get( v1 );
			final Vector3D p2 = part.vertexCoordinates.get( v2 );
			final Vector3D p3 = part.vertexCoordinates.get( v3 );
			normal = GeometryTools.getPlaneNormal( p1, p2, p3 );
		}
	}

	public static class Thread
	{
		int type = -1;
		Vector3D startPoint = null;
		Vector3D endPoint = null;
		double diameter = -1D;
		List<Vector3D> helixPoints = null;
		List<Vector3D> helixvectors = null;
		Matrix3D helixMatrix = null;

		private Thread()
		{
		}

		protected void calcHelix()
		{
			final double l = Vector3D.length( endPoint.x - startPoint.x, endPoint.y - startPoint.y, endPoint.z - startPoint.z );
			final double r = ( type == 0 ) ? diameter / 1.9 : diameter / 2.1;

			final int rotations = (int) ( ( l / r ) * 3.0 );
			final int degrees = rotations * 360;
			final double stepX = (float) ( l / (double) degrees );

			helixPoints = new ArrayList<Vector3D>( 1 + degrees / 30 );
			helixvectors = new ArrayList<Vector3D>( 1 + degrees / 30 );

			double xpos = 0.0;

			for ( int angle = 1 ; angle < degrees ; angle += 30 )
			{
				xpos += stepX;
				final double x = ( l / (double) degrees ) * (double) ( degrees - angle );
				final double y = r * Math.cos( Math.toRadians( angle ) );
				final double z = r * Math.sin( Math.toRadians( angle ) );

				helixPoints.add( new Vector3D( x, y, z ) );
				helixvectors.add( new Vector3D( xpos, 0.0, 0.0 ) );
			}

			calcMatrix();
		}

		void calcMatrix()
		{
			final Vector3D xVec = Vector3D.normalize( endPoint.x - startPoint.x, endPoint.y - startPoint.y, endPoint.z - startPoint.z );

			final double absX = Math.abs( xVec.x );
			final double absY = Math.abs( xVec.y );
			final double absZ = Math.abs( xVec.z );

			final Vector3D zVec = ( absX < absY ) ? ( absX < absZ ) ? Vector3D.cross( xVec, Vector3D.POSITIVE_X_AXIS )
			                                                        : Vector3D.cross( Vector3D.POSITIVE_Z_AXIS, xVec )
			                                      : ( absY < absZ ) ? Vector3D.cross( Vector3D.POSITIVE_Y_AXIS, xVec )
			                                                        : Vector3D.cross( Vector3D.POSITIVE_Z_AXIS, xVec );

			final Vector3D yVec = Vector3D.cross( xVec, zVec );

			helixMatrix = new Matrix3D( xVec.x, yVec.x, zVec.x, startPoint.x,
			                            xVec.y, yVec.y, zVec.y, startPoint.y,
			                            xVec.z, yVec.z, zVec.z, startPoint.z );
		}
	}
}
