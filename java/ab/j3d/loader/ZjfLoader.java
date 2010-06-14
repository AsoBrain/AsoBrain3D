/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2010-2010
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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import static java.io.StreamTokenizer.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import ab.j3d.model.*;
import org.jetbrains.annotations.*;

/**
 * Loader for ZJF/ZJV files. This is a propriety format used by Cadenas
 * <a href="http://portal-de.partcommunity.com/">PARTserver</a>.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class ZjfLoader
{
	/**
	 * Load the specified ZJF/ZJV file.
	 *
	 * @param   url     URL to file.
	 *
	 * @return  {@link Node3D}..
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
	static Node3D convertAssemblyToNode3D( final Assembly assembly )
		throws IOException
	{
		final Node3D result = new Node3D();

		final List<Part> parts = assembly.parts;
		final List<Integer> partlist = assembly.partlist;
		final List<Matrix3D> matrixlist = assembly.matrixlist;

		final List<Object3D> convertedParts = new ArrayList<Object3D>( parts.size() );
		for ( final Part part : parts )
		{
			convertedParts.add( convertPartToObject3D( part ) );
		}

		for ( int i = 0 ; i < partlist.size() ; i++ )
		{
			final int partNr = partlist.get( i );
			final Part part = parts.get( partNr );

			final Insert3D insert = new Insert3D( matrixlist.get( i ), convertedParts.get( partNr ) );
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
	static Object3D convertPartToObject3D( final Part part )
	{
		final List<Vector3D> vertexCoordinates = part.vertexCoordinates;
		final List<Vector3D> normals = part.normals;

		final Object3DBuilder builder = new Object3DBuilder();
		builder.setVertexCoordinates( vertexCoordinates );

		for ( final Triangle triangle : part.triangles )
		{
			final List<Integer> vertices = triangle.vertices;
			final List<Integer> normalList = triangle.normalList;

			final int[] vertexIndices = { vertices.get( 0 ), vertices.get( 1 ) , vertices.get( 2 ) };
			final Vector3D[] vertexNormals = ( normalList != null ) ? new Vector3D[] { normals.get( normalList.get( 0 ) ), normals.get( normalList.get( 1 ) ), normals.get( normalList.get( 2 ) ) } : null;
			final Material material = new Material( triangle.color.getRGB() | 0xFF000000 );

			builder.addFace( vertexIndices, material, null, vertexNormals, false, true );
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
	static Assembly parse( final URL url )
		throws IOException
	{
		final InputStream is;

		final String file = url.getFile();
		if ( file.endsWith( ".zjv" ) || file.endsWith( ".ZJV" ) )
		{
			final ZipInputStream zipInputStream = new ZipInputStream( url.openStream() );
			zipInputStream.getNextEntry();
			is = zipInputStream;
		}
		else
		{
			is = url.openStream();
		}

		return parse( new BufferedReader( new InputStreamReader( is ), 131072 ) );
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
	static Assembly parse( final Reader reader )
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

		int count;
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
						assembly.parts.add( currentPart );
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

					assembly.matrixlist.add( new Matrix3D( ax, bx, cx, tx, ay, by, cy, ty, az, bz, cz, tz ) );
					assembly.partlist.add( Integer.valueOf( partNr ) );

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

					assembly.explosionmatrixlist.add( new Matrix3D( ax, bx, cx, tx, ay, by, cy, ty, az, bz, cz, tz ) );

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

						currentPart.triangles.add( new Triangle( currentPart, Arrays.asList( v1, v2, v3 ), currentPart.color, 0 ) );
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
						triangel.normalList = Arrays.asList( v1, v2, v3 );
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
							final Thread thread = new Thread();
							thread.type = data.get( 0 ).intValue();
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

					final int r = (int) getDoubleValue( stream );

					stream.nextToken();
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int g = (int) getDoubleValue( stream );

					stream.nextToken();
					if ( stream.ttype != TT_WORD )
					{
						return null;
					}

					final int b = (int) getDoubleValue( stream );
					currentPart.color = new Color( r, g, b );

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
	private static final int START = 0;
	private static final int END = 1;
	private static final int IN_PART = 2;
	private static final int READ_OPEN = 3;
	private static final int READ_PART_NAME = 4;
	private static final int READ_PART_COL = 5;
	private static final int READ_PART_MID = 20;
	private static final int READ_PART_FACE_LIST3 = 6;
	private static final int READ_PART_POINT_LIST = 7;
	private static final int READ_CLOSE = 8;
	private static final int READY_PART = 9;
	private static final int IN_STRUCT = 10;
	private static final int READ_STRUCT_NAME = 11;
	private static final int READ_MEMBER = 12;
	private static final int READ_PART_NORMAL_LIST = 13;
	private static final int READ_PART_FACE_NORMAL_LIST3 = 14;
	private static final int READ_PART_FACE_COLOR_LIST3 = 21;
	private static final int READ_HARD_POINTS = 15;
	private static final int READ_SOFT_POINTS = 16;
	private static final int READ_MEMBER_MATRIX = 17;
	private static final int READ_EXPLOSION_MATRIX = 18;
	private static final int READ_THREAD_DATA = 19;
	private static final int READ_PART_COLS = 22;

	private static class Assembly
	{
		List<Part> parts = new ArrayList<Part>();
		List<Integer> partlist = new ArrayList<Integer>();
		List<Matrix3D> matrixlist = new ArrayList<Matrix3D>();
		List<Matrix3D> explosionmatrixlist = new ArrayList<Matrix3D>();
	}

	private static class Part
	{
		int alpha;
		Color color;
		String name;
		List<Vector3D> vertexCoordinates = new ArrayList<Vector3D>();
		String mid;
		List<Triangle> triangles = new ArrayList<Triangle>();
		List<Vector3D> normals = new ArrayList<Vector3D>();
		List<Integer> hardPoints;
		List<Integer> softPoints;
		List<Thread> threads = new ArrayList<Thread>();
	}

	private static class Triangle
	{
		Color color;
		List<Integer> vertices;
		List<Integer> normalList;
		Vector3D normal;

		Triangle( Part part, List<Integer> vertices, Color col, int alpha )
		{
			normal = null;
			this.vertices = vertices;
			color = col;

			Vector3D p1 = part.vertexCoordinates.get( vertices.get( 0 ) );
			Vector3D p2 = part.vertexCoordinates.get( vertices.get( 1 ) );
			Vector3D p3 = part.vertexCoordinates.get( vertices.get( 2 ) );
			normal = GeometryTools.getPlaneNormal( p1, p2, p3 );
		}
	}

	private static class Thread
	{

		Thread()
		{
			type = -1;
			startPoint = null;
			endPoint = null;
			diameter = -1D;
			helixPoints = null;
			helixvectors = null;
			helixMatrix = null;
		}

		void calcHelix()
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

		int type;
		Vector3D startPoint;
		Vector3D endPoint;
		double diameter;
		List<Vector3D> helixPoints;
		List<Vector3D> helixvectors;
		Matrix3D helixMatrix;
	}
}
