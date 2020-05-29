/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
	 * Number format with two fraction digits.
	 */
	private final NumberFormat _twoDecimalFormat;

	{
		final NumberFormat twoDecimals = NumberFormat.getNumberInstance( Locale.US );
		twoDecimals.setMinimumFractionDigits( 2 );
		twoDecimals.setMaximumFractionDigits( 2 );
		twoDecimals.setGroupingUsed( false );
		_twoDecimalFormat = twoDecimals;
	}

	/**
	 * Whether to fix problems when possible.
	 */
	private boolean _fixErrors = true;

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
	public void checkRecursively( @NotNull final Node3D node )
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
	public void checkNode( @NotNull final Node3D node )
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
	public void checkTransform( @NotNull final Matrix3D transform )
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
	public void checkObject3D( @NotNull final Object3D object )
	{
		final List<FaceGroup> faceGroups = new ArrayList<FaceGroup>( object.getFaceGroups() );
		final List<Vector3D> vertexCoordinates = object.getVertexCoordinates();

		final Collection<Face3D> facesToRemove = new ArrayList<Face3D>();

		for ( int faceGroupIndex = 0; faceGroupIndex < faceGroups.size(); faceGroupIndex++ )
		{
			final FaceGroup faceGroup = faceGroups.get( faceGroupIndex );

			final List<Face3D> faces = faceGroup.getFaces();

			for ( int faceIndex = 0; faceIndex < faces.size(); faceIndex++ )
			{
				final Face3D face = faces.get( faceIndex );

				final String error = checkFace( vertexCoordinates, face );
				if ( error != null )
				{
					if ( isFixErrors() )
					{
						facesToRemove.add( face );
					}
					else
					{
						reportError( "Face " + object + ".faceGroups[" + faceGroupIndex + "].faces[" + faceIndex + "]:" + error );
					}
				}
			}

			if ( facesToRemove.size() == faces.size() )
			{
				object.removeFaceGroup( faceGroup );
			}
			else if ( !facesToRemove.isEmpty() )
			{
				for ( final Face3D face : facesToRemove )
				{
					faceGroup.removeFace( face );
				}
				facesToRemove.clear();
			}
		}
	}

	/**
	 * Check properties of the given 3D face and return a problem that was
	 * found, if any.
	 *
	 * @param vertexCoordinates Vertex coordinates.
	 * @param face              Face to check.
	 *
	 * @return Message describing a problem with the face; {@code null} if no
	 * error was found.
	 */
	@Nullable
	protected String checkFace( @NotNull final Collection<Vector3D> vertexCoordinates, @NotNull final Face3D face )
	{
		final List<Vertex3D> faceVertices = face.getVertices();
		final Tessellation tessellation = face.getTessellation();
		final List<TessellationPrimitive> primitives = tessellation.getPrimitives();
		final List<int[]> outlines = tessellation.getOutlines();

		final StringBuilder error = new StringBuilder();

		if ( faceVertices.isEmpty() )
		{
			error.append( "\n\tface has no vertices" );
		}

		Vector3D faceCross;
		try
		{
			faceCross = face.getCross();
		}
		catch ( final Exception e )
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
		catch ( final Exception e )
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

		final String result;

		if ( error.length() > 0 )
		{
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
			catch ( final Exception e )
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
				error.append( _twoDecimalFormat.format( vertex.colorMapU ) );
				error.append( ',' );
				error.append( _twoDecimalFormat.format( vertex.colorMapV ) );
				error.append( ']' );

			}

			result = error.toString();
		}
		else
		{
			result = null;
		}

		return result;
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
	 * Get whether to fix problems when possible.
	 *
	 * @return Whether to fix problems when possible.
	 */
	public boolean isFixErrors()
	{
		return _fixErrors;
	}

	/**
	 * Set whether to fix problems when possible.
	 *
	 * @param fixErrors Whether to fix problems when possible.
	 */
	public void setFixErrors( final boolean fixErrors )
	{
		_fixErrors = fixErrors;
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
