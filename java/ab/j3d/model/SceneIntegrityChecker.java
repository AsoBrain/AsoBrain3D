/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2013 Peter S. Heijnen
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
package ab.j3d.model;

import java.text.*;
import java.util.*;

import ab.j3d.*;
import ab.j3d.geom.*;
import org.jetbrains.annotations.*;

/**
 * This class can be used to check the integrity of a 3D scene.
 *
 * @author Peter S. Heijnen
 */
public class SceneIntegrityChecker
{
	/**
	 * Number format with one fraction digit.
	 */
	protected static final NumberFormat ONE_DECIMAL_FORMAT;

	/**
	 * Number format with two fraction digits.
	 */
	protected static final NumberFormat TWO_DECIMAL_FORMAT;

	static
	{
		final NumberFormat oneDecimal = NumberFormat.getNumberInstance( Locale.US );
		oneDecimal.setMinimumFractionDigits( 1 );
		oneDecimal.setMaximumFractionDigits( 1 );
		oneDecimal.setGroupingUsed( false );
		ONE_DECIMAL_FORMAT = oneDecimal;

		final NumberFormat twoDecimals = NumberFormat.getNumberInstance( Locale.US );
		twoDecimals.setMinimumFractionDigits( 2 );
		twoDecimals.setMaximumFractionDigits( 2 );
		twoDecimals.setGroupingUsed( false );
		TWO_DECIMAL_FORMAT = twoDecimals;
	}

	/**
	 * Errors that were found.
	 */
	private final List<String> _errors = new ArrayList<String>();

	/**
	 * Convenience method that throws a {@link RuntimeException} when the given
	 * node or any of its descendants contains integrity errors.
	 *
	 * @param node Node whose integrity to check.
	 */
	public static void ensureIntegrity( final Node3D node )
	{
		ensureIntegrity( node, null );
	}

	/**
	 * Convenience method that throws a {@link RuntimeException} when the given
	 * node or any of its descendants contains integrity errors.
	 *
	 * @param node    Node whose integrity to check.
	 * @param message Optional message to prepend to exception message.
	 */
	public static void ensureIntegrity( final Node3D node, final String message )
	{
		final SceneIntegrityChecker checker = new SceneIntegrityChecker();
		checker.checkRecursively( node );
		if ( checker.isFailed() )
		{
			throw new RuntimeException( ( ( message != null ) && !message.isEmpty() ) ? message + '\n' + checker.getErrorText() : checker.getErrorText() );
		}
	}

	/**
	 * Check integrity of the given node and all its descendants.
	 *
	 * @param node Root node whose integrity to check.
	 */
	public void checkRecursively( final Node3D node )
	{
		Node3DTreeWalker.walk( new Node3DVisitor()
		{
			public boolean visitNode( @NotNull final Node3DPath path )
			{
				final Matrix3D transform = path.getTransform();
				final Node3D node = path.getNode();

				checkTransform( transform );
				checkNode( node );

				return true;
			}
		}, node );
	}

	/**
	 * Check integrity of the given node.
	 *
	 * @param node Node whose integrity to check.
	 */
	public void checkNode( final Node3D node )
	{
		if ( node instanceof Object3D )
		{
			checkObject3D( (Object3D)node );
		}
	}

	/**
	 * Check integrity of the given transformation matrix.
	 *
	 * @param transform Transformation matrix whose integrity to check.
	 */
	public void checkTransform( final Matrix3D transform )
	{
		if ( Double.isNaN( transform.xx ) || Double.isNaN( transform.xy ) || Double.isNaN( transform.xz ) || Double.isNaN( transform.xo ) ||
		     Double.isNaN( transform.yx ) || Double.isNaN( transform.yy ) || Double.isNaN( transform.yz ) || Double.isNaN( transform.yo ) ||
		     Double.isNaN( transform.zx ) || Double.isNaN( transform.zy ) || Double.isNaN( transform.zz ) || Double.isNaN( transform.zo ) )
		{
			reportError( "NaN in " + transform );
		}
	}

	/**
	 * Check integrity of the given 3D object.
	 *
	 * @param object 3D object whose integrity to check.
	 */
	public void checkObject3D( final Object3D object )
	{
		final List<FaceGroup> faceGroups = object.getFaceGroups();
		final List<Vector3D> vertexCoordinates = object.getVertexCoordinates();
		for ( int faceGroupIndex = 0; faceGroupIndex < faceGroups.size(); faceGroupIndex++ )
		{
			final FaceGroup faceGroup = faceGroups.get( faceGroupIndex );

			final List<Face3D> faces = faceGroup.getFaces();
			for ( int faceIndex = 0; faceIndex < faces.size(); faceIndex++ )
			{
				final Face3D face = faces.get( faceIndex );
				final List<Vertex3D> faceVertices = face.getVertices();
				final Tessellation tessellation = face.getTessellation();
				final List<TessellationPrimitive> primitives = tessellation.getPrimitives();
				final List<int[]> outlines = tessellation.getOutlines();

				final StringBuilder error = new StringBuilder();

				Vector3D faceCross;
				try
				{
					faceCross = face.getCross();
				}
				catch ( Exception e )
				{
					error.append( "\n\tFailed to determine face cross-product: " );
					error.append( e );
					faceCross = null;
				}

				Vector3D faceNormal = null;
				boolean badFaceNormal = true;
				try
				{
					faceNormal = face.getNormal();
					badFaceNormal = ( faceNormal == null ) || !faceNormal.isNonZero();
					if ( badFaceNormal )
					{
						error.append( "\n\tface has bad (zero/NaN) normal: " );
						error.append( faceNormal );
					}
// The following code makes sure the face normal matches the vertices (cross aligned with normal), but may fail due to rounding errors
//					else if ( ( cross != null ) && cross.isNonZero() )
//					{
//						final Vector3D normalizedCross = cross.normalize();
//						if ( !normalizedCross.almostEquals( faceNormal ) )
//						{
//							error.append( "\n\tface normal " );
//							error.append( faceNormal.toFriendlyString() );
//							error.append( " is not aligned with cross: " );
//							error.append( normalizedCross.toFriendlyString() );
//						}
//					}
				}
				catch ( Exception e )
				{
					error.append( "\n\tFailed to determine face normal: " );
					error.append( e );
				}

				for ( int vertexIndex = 0; vertexIndex < faceVertices.size(); vertexIndex++ )
				{
					final Vertex3D vertex = faceVertices.get( vertexIndex );
					final Vector3D point = vertex.point;

					if ( Double.isNaN( point.x ) || Double.isNaN( point.y ) || Double.isNaN( point.z ) )
					{
						error.append( "\n\tvertices[" );
						error.append( vertexIndex );
						error.append( "] has bad (NaN) point: " );
						error.append( point );
					}
					else if ( ( vertex.vertexCoordinateIndex < 0 ) || ( vertex.vertexCoordinateIndex >= vertexCoordinates.size() ) )
					{
						error.append( "\n\tvertices[" );
						error.append( vertexIndex );
						error.append( "] has invalid 'vertexCoordinateIndex' " );
						error.append( vertex.vertexCoordinateIndex );
						error.append( " (must be between 0 and " );
						error.append( vertexCoordinates.size() - 1 );
						error.append( ')' );
					}

					if ( !badFaceNormal )
					{
						final Vector3D vertexNormal = face.getVertexNormal( vertexIndex );
						if ( !vertexNormal.isNonZero() )
						{
							error.append( "\n\tvertices[" );
							error.append( vertexIndex );
							error.append( "] has bad (zero/NaN) vertex-normal: " );
							error.append( vertexNormal );
						}
					}
				}

				for ( int primitiveIndex = 0; primitiveIndex < primitives.size(); primitiveIndex++ )
				{
					final TessellationPrimitive primitive = primitives.get( primitiveIndex );

					final int[] primitiveVertices = primitive.getVertices();
					for ( int vertexIndex = 0; vertexIndex < primitiveVertices.length; vertexIndex++ )
					{
						final int faceVertexIndex = primitiveVertices[ vertexIndex ];
						if ( ( faceVertexIndex < 0 ) || ( faceVertexIndex >= faceVertices.size() ) )
						{
							error.append( "\n\ttesselation.primitives[" );
							error.append( primitiveIndex );
							error.append( "].vertices[" );
							error.append( vertexIndex );
							error.append( "] has invalid vertex index " );
							error.append( faceVertexIndex );
							error.append( " (must be between 0 and " );
							error.append( faceVertices.size() - 1 );
							error.append( ')' );
						}
					}

					final int[] triangles = primitive.getTriangles();
					if ( ( triangles.length == 0 ) || ( ( triangles.length % 3 ) != 0 ) )
					{
						error.append( "\n\ttesselation.primitives[" );
						error.append( primitiveIndex );
						error.append( "] has invalid triangle list length " );
						error.append( triangles.length );
						error.append( " (must be non-zero multiple of 3) in " );
						error.append( primitive );
					}

					for ( int triangleIndex = 0; triangleIndex < triangles.length; triangleIndex++ )
					{
						final int faceVertexIndex = triangles[ triangleIndex ];
						if ( ( faceVertexIndex < 0 ) || ( faceVertexIndex >= faceVertices.size() ) )
						{
							error.append( "\n\ttesselation.primitives[" );
							error.append( primitiveIndex );
							error.append( "].triangles[" );
							error.append( triangleIndex );
							error.append( "] has invalid vertex index " );
							error.append( faceVertexIndex );
							error.append( " (must be between 0 and " );
							error.append( faceVertices.size() - 1 );
							error.append( ')' );
						}
					}
				}

				if ( error.length() > 0 )
				{
					error.insert( 0, "Face " + object + ".faceGroups[" + faceGroupIndex + "].faces[" + faceIndex + "]:" );

					error.append( "\nFace properties:" );
					error.append( "\n\tvertex count    = " );
					error.append( faceVertices.size() );
					error.append( "\n\tprimitive count = " );
					error.append( primitives.size() );
					error.append( "\n\toutline count   = " );
					error.append( outlines.size() );
					error.append( "\n\ttwo-sided       = " );
					error.append( face.isTwoSided() );
					error.append( "\n\tcross           = " );
					error.append( faceCross );
					error.append( "\n\tnormal          = " );
					error.append( faceNormal );
					error.append( "\n\tdistance        = " );
					try
					{
						error.append( face.getDistance() );
					}
					catch ( Exception e )
					{
						error.append( e );
					}

					error.append( "\n\tPrimitives:" );
					for ( int i = 0; i < primitives.size(); i++ )
					{
						final TessellationPrimitive primitive = primitives.get( i );
						error.append( "\n\t\t[" );
						error.append( i );
						error.append( "]: " );
						error.append( primitive );

					}

					error.append( "\n\tOutlines:" );
					for ( int i = 0; i < outlines.size(); i++ )
					{
						final int[] outline = outlines.get( i );
						error.append( "\n\t\t[" );
						error.append( i );
						error.append( "]: " );
						error.append( Arrays.toString( outline ) );

					}

					error.append( "\n\tVertices:" );
					for ( int i = 0; i < faceVertices.size(); i++ )
					{
						final Vertex3D vertex = faceVertices.get( i );
						error.append( "\n\t\t[" );
						error.append( i );
						error.append( "]: point=" );
						error.append( vertex.point.toShortFriendlyString() );
						error.append( ", vertexCoordinateIndex=" );
						error.append( vertex.vertexCoordinateIndex );
						error.append( ", colorMapU,V=[" );
						error.append( TWO_DECIMAL_FORMAT.format( vertex.colorMapU ) );
						error.append( ',' );
						error.append( TWO_DECIMAL_FORMAT.format( vertex.colorMapV ) );
						error.append( ']' );

					}

					reportError( error.toString() );
				}
			}
		}
	}

	/**
	 * Test whether all performed checks were successful.
	 *
	 * @return {@code true} if all performed checks were successful.
	 */
	public boolean isSuccessful()
	{
		return !_errors.isEmpty();
	}

	/**
	 * Test whether errors were found.
	 *
	 * @return {@code true} if at least one check failed.
	 */
	public boolean isFailed()
	{
		return !_errors.isEmpty();
	}

	/**
	 * Get errors that were found.
	 *
	 * @return Errors that were found.
	 */
	public Collection<String> getErrors()
	{
		return Collections.unmodifiableCollection( _errors );
	}

	/**
	 * Get errors as a single string. This returns an empty string if no errors
	 * were found. If multiple errors are found, individual error messages are
	 * separated by newline characters.
	 *
	 * @return Errors as a single string.
	 */
	public String getErrorText()
	{
		final String result;

		final List<String> errors = _errors;
		if ( errors.isEmpty() )
		{
			result = "";
		}
		else if ( errors.size() == 1 )
		{
			result = errors.get( 0 );
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			for ( final String error : errors )
			{
				if ( sb.length() > 0 )
				{
					sb.append( '\n' );
				}

				sb.append( error );
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Reset checker state.
	 */
	public void reset()
	{
		_errors.clear();
	}

	/**
	 * Records error that was found by checks.
	 *
	 * @param message Error message.
	 */
	protected void reportError( final String message )
	{
		_errors.add( message );
	}
}
