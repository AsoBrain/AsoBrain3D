/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2004-2007
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
package ab.j3d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ab.j3d.Material;
import ab.j3d.Matrix3D;
import ab.j3d.Vector3D;

import com.numdata.oss.MathTools;

/**
 * This class extends {@link Object3D}. The vertices and faces are generated out
 * of a Java 2D simple {@link Shape}. An extrusion vector is used to define the
 * coordinate displacement.
 *
 * @author  G.B.M. Rupert
 * @version $Revision$ $Date$
 */
public final class ExtrudedObject2D
	extends Object3D
{
	/**
	 * Transform to apply.
	 */
	public final Matrix3D transform;

	/**
	 * Base shape.
	 */
	public final Shape shape;

	/**
	 * Extrusion vector (control-point displacement). This is a displacement
	 * relative to the shape being extruded.
	 */
	public final Vector3D extrusion;

	/**
	 * The maximum allowable distance between the control points and a
	 * flattened curve.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator(AffineTransform, double)
	 */
	public final double flatness;

	/**
	 * Flag to indicate if extruded faces have a backface.
	 */
	public final boolean hasBackface;

	/**
	 * Indicates whether normals are flipped.
	 */
	public final boolean flipNormals;

	/**
	 * Indicates whether the top and bottom are capped.
	 */
	public final boolean caps;

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface )
	{
		this( shape , extrusion , transform , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals )
	{
		this( shape , extrusion , transform , topMaterial , bottomMaterial , sideMaterial , flatness , hasBackface , flipNormals , false );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply to the extruded shape.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this( shape , extrusion , transform , material , material , material , flatness , hasBackface , flipNormals , caps );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final Material bottomMaterial , final Material sideMaterial , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;
		this.flipNormals = flipNormals;
		this.caps        = caps;

		generate( this , shape , extrusion , transform , topMaterial , false , bottomMaterial , false , sideMaterial , false , flatness , hasBackface , flipNormals , caps );
	}

	/**
	 * Construct extruded object.
	 *
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   topFlipUV       Whether the top U and V coordinates are flipped.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   bottomFlipUV    Whether the bottom U and V coordinates are flipped.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   sideFlipUV      Whether the side U and V coordinates are flipped.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, the top and bottom of the
	 *                          extruded shape are capped.
	 *
	 * @see     FlatteningPathIterator
	 * @see     Shape#getPathIterator( AffineTransform , double )
	 */
	public ExtrudedObject2D( final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final boolean topFlipUV , final Material bottomMaterial , final boolean bottomFlipUV , final Material sideMaterial , final boolean sideFlipUV , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		this.shape       = shape;
		this.extrusion   = extrusion;
		this.transform   = transform;
		this.flatness    = flatness;
		this.hasBackface = hasBackface;
		this.flipNormals = flipNormals;
		this.caps        = caps;

		generate( this , shape , extrusion , transform , topMaterial , topFlipUV , bottomMaterial , bottomFlipUV , sideMaterial , sideFlipUV , flatness , hasBackface , flipNormals , caps );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   material        Material to apply.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 */
	public static void generate( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material material , final double flatness , final boolean hasBackface )
	{
		generate( target , shape , extrusion , transform , material , false , material , false , material , false , flatness , hasBackface , false , false );
	}

	/**
	 * Generate data from object properties.
	 *
	 * @param   target          Target {@link Object3D} to store generated data.
	 * @param   shape           Base shape.
	 * @param   extrusion       Extrusion vector (control-point displacement).
	 * @param   transform       Transform to apply.
	 * @param   topMaterial     Material to apply to the top cap.
	 * @param   topFlipUV       Whether the top U and V coordinates are flipped.
	 * @param   bottomMaterial  Material to apply to the bottom cap.
	 * @param   bottomFlipUV    Whether the bottom U and V coordinates are flipped.
	 * @param   sideMaterial    Material to apply to the extruded sides.
	 * @param   sideFlipUV      Whether the side U and V coordinates are flipped.
	 * @param   flatness        Flatness to use.
	 * @param   hasBackface     Flag to indicate if extruded faces have a backface.
	 * @param   flipNormals     If <code>true</code>, normals are flipped to
	 *                          point in the opposite direction.
	 * @param   caps            If <code>true</code>, top and bottom caps are
	 *                          generated.
	 */
	private static void generate( final Object3D target , final Shape shape , final Vector3D extrusion , final Matrix3D transform , final Material topMaterial , final boolean topFlipUV , final Material bottomMaterial , final boolean bottomFlipUV , final Material sideMaterial , final boolean sideFlipUV , final double flatness , final boolean hasBackface , final boolean flipNormals , final boolean caps )
	{
		final double  ex            = extrusion.x;
		final double  ey            = extrusion.y;
		final double  ez            = extrusion.z;
		final boolean hasExtrusion  = !MathTools.almostEqual( ex , 0.0 ) || !MathTools.almostEqual( ey , 0.0 ) || !MathTools.almostEqual( ez , 0.0 );
		final boolean flipExtrusion = flipNormals ^ ( ez < 0.0 );

		final PathIterator pathIterator = shape.getPathIterator( null , flatness );

		final double[] coords = new double[ 6 ];
		int lastIndex          = -1;
		int lastExtrudedIndex  = -1;
		int lastMoveTo         = -1;
		int lastExtrudedMoveTo = -1;

		final boolean generateTextureCoordinates = ( sideMaterial != null ) && ( sideMaterial.colorMap != null );

		final UVMap uvMap = new BoxUVMap( 0.001 ); // @FIXME Retrieve model units instead of assuming millimeters.

		while ( !pathIterator.isDone() )
		{
			final int[] vertexIndices;

			boolean hasBackfaceOverride = hasBackface;

			final int type = pathIterator.currentSegment( coords );
			switch ( type )
			{
				case FlatteningPathIterator.SEG_MOVETO:
				{
					final double shapeX = coords[ 0 ];
					final double shapeY = coords[ 1 ];

					final double x1 = transform.transformX( shapeX , shapeY , 0.0 );
					final double y1 = transform.transformY( shapeX , shapeY , 0.0 );
					final double z1 = transform.transformZ( shapeX , shapeY , 0.0 );

					lastIndex  = target.getVertexIndex( x1 , y1 , z1 );
					lastMoveTo = lastIndex;

					if ( hasExtrusion )
					{
						final double x2 = transform.transformX( shapeX + ex , shapeY + ey , ez );
						final double y2 = transform.transformY( shapeX + ex , shapeY + ey , ez );
						final double z2 = transform.transformZ( shapeX + ex , shapeY + ey , ez );

						lastExtrudedIndex  = target.getVertexIndex( x2 , y2 , z2 );
						lastExtrudedMoveTo = lastExtrudedIndex;
					}

					vertexIndices = null;
					break;
				}

				case FlatteningPathIterator.SEG_LINETO:
				{
					final double shapeX = coords[ 0 ];
					final double shapeY = coords[ 1 ];

					final double x1 = transform.transformX( shapeX , shapeY , 0.0 );
					final double y1 = transform.transformY( shapeX , shapeY , 0.0 );
					final double z1 = transform.transformZ( shapeX , shapeY , 0.0 );

					final int vertexIndex = target.getVertexIndex( x1 , y1 , z1 );

					int extrudedVertexIndex = -1;
					if ( hasExtrusion )
					{
						final double x2 = transform.transformX( shapeX + ex , shapeY + ey , ez );
						final double y2 = transform.transformY( shapeX + ex , shapeY + ey , ez );
						final double z2 = transform.transformZ( shapeX + ex , shapeY + ey , ez );
						extrudedVertexIndex = target.getVertexIndex( x2 , y2 , z2 );
					}

					if ( lastIndex != vertexIndex )
					{
						if ( hasExtrusion )
						{
							vertexIndices = flipExtrusion ? new int[] { vertexIndex , extrudedVertexIndex , lastExtrudedIndex   , lastIndex   }
							                              : new int[] { lastIndex   , lastExtrudedIndex   , extrudedVertexIndex , vertexIndex };
							lastExtrudedIndex = extrudedVertexIndex;
						}
						else if ( !caps )
						{
							vertexIndices = new int[] { lastIndex , vertexIndex };
							hasBackfaceOverride = true;
						}
						else
						{
							vertexIndices = null;
						}
					}
					else
					{
						vertexIndices = null;
					}

					lastIndex = vertexIndex;
					break;
				}

				case FlatteningPathIterator.SEG_CLOSE:
				{
					if ( lastIndex != lastMoveTo )
					{
						if ( hasExtrusion )
						{
							vertexIndices = flipExtrusion ? new int[] { lastMoveTo , lastExtrudedMoveTo , lastExtrudedIndex  , lastIndex  }
							                              : new int[] { lastIndex  , lastExtrudedIndex  , lastExtrudedMoveTo , lastMoveTo };
							lastExtrudedIndex = lastExtrudedMoveTo;
						}
						else if ( !caps )
						{
							vertexIndices = new int[] { lastIndex , lastMoveTo };
							hasBackfaceOverride = true;
						}
						else
						{
							vertexIndices = null;
						}
					}
					else
					{
						vertexIndices = null;
					}

					lastIndex = lastMoveTo;
					break;
				}

				default:
					vertexIndices = null;
			}

			if ( vertexIndices != null )
			{
				final float[] textureU;
				final float[] textureV;

				if ( generateTextureCoordinates )
				{
					textureU = new float[ vertexIndices.length ];
					textureV = new float[ vertexIndices.length ];

					uvMap.generate( sideMaterial , target.getVertexCoordinates(), vertexIndices , textureU , textureV );
				}
				else
				{
					textureU = null;
					textureV = null;
				}

				if ( sideFlipUV )
				{
					target.addFace( vertexIndices , sideMaterial , textureV , textureU , 1.0f , false , hasBackfaceOverride );
				}
				else
				{
					target.addFace( vertexIndices , sideMaterial , textureU , textureV , 1.0f , false , hasBackfaceOverride );
				}
			}

			pathIterator.next();
		}

		if ( caps )
		{
			final TriangulatorFactory triangulatorFactory = TriangulatorFactory.newInstance();
			final Triangulator        triangulator        = triangulatorFactory.newTriangulator();

			triangulator.setFlatness( flatness );

			final Vector3D topNormal    = Vector3D.INIT.set( 0.0 , 0.0 , flipExtrusion ? -1.0 :  1.0 );
			final Vector3D bottomNormal = Vector3D.INIT.set( 0.0 , 0.0 , flipExtrusion ?  1.0 : -1.0 );

			triangulator.setNormal( bottomNormal );
			generateCap( target , shape , transform                   , bottomMaterial , bottomFlipUV , uvMap , triangulator );
			triangulator.setNormal( topNormal );
			generateCap( target , shape , transform.plus( extrusion ) , topMaterial    , topFlipUV    , uvMap , triangulator );

			if ( hasBackface && hasExtrusion )
			{
				triangulator.setNormal( topNormal );
				generateCap( target , shape , transform                   , bottomMaterial , bottomFlipUV , uvMap , triangulator );
				triangulator.setNormal( bottomNormal );
				generateCap( target , shape , transform.plus( extrusion ) , topMaterial    , topFlipUV    , uvMap , triangulator );
			}
		}
	}

	private static void generateCap( final Object3D target , final Shape shape , final Matrix3D transform , final Material material , final boolean flipUV , final UVMap uvMap , final Triangulator triangulator )
	{
		// @TODO Triangulation can be performed outside of this method for a performance gain, since the triangulation for top and bottom caps is essentially the same.
		final Triangulation  triangulation = triangulator.triangulate( shape );
		final List<Vector3D> vertices      = triangulation.getVertices( transform );

		final Collection<int[]> triangles = triangulation.getTriangles();
		final Collection<int[]> faces     = quadify( vertices , triangles );

		for ( final int[] face : faces )
		{
			final int[] faceVertices = new int[ face.length ];

			for ( int i = 0 ; i < face.length ; i++ )
			{
				final int index = face[ i ];
				final Vector3D vertexCoordinate = vertices.get( index );
				faceVertices[ i ] = target.getVertexIndex( vertexCoordinate.x , vertexCoordinate.y , vertexCoordinate.z );
			}

			final float[] faceTextureU;
			final float[] faceTextureV;
			if ( material == null )
			{
				faceTextureU = null;
				faceTextureV = null;
			}
			else
			{
				faceTextureU = new float[ face.length ];
				faceTextureV = new float[ face.length ];
				uvMap.generate( material , target.getVertexCoordinates() , faceVertices , flipUV ? faceTextureV : faceTextureU , flipUV ? faceTextureU : faceTextureV );
			}

			target.addFace( faceVertices , material , faceTextureU , faceTextureV , 1.0f , false , false );
		}
	}

	/**
	 * Converts the given triangles into a single quad, if possible. This is a
	 * temporary measure to overcome the current limitations of the 3D view
	 * implementations.
	 *
	 * @FIXME Remove when face outlines are drawn instead of triangle outlines.
	 *
	 * @param   vertices    Vertex coordinates, by vertex index.
	 * @param   triangles   Triangles to be processed, as vertex index triplets.
	 *
	 * @return  A single quad replacing the triangles, if possible;
	 *          otherwise, the given triangles are returned.
	 */
	private static Collection<int[]> quadify( final List<Vector3D> vertices , final Collection<int[]> triangles )
	{
		final Collection<int[]> result;

		/*
		 * Only a pair of triangles can be converted to a quad.
		 */
		if ( triangles.size() == 2 )
		{
			final Iterator<int[]> iterator = triangles.iterator();
			final int[] triangle1 = iterator.next();
			final int[] triangle2 = iterator.next();

			/*
			 * Find the index of the first point of the line shared by the two
			 * triangles, for each triangle.
			 */
			int offset1 = -1;
			int offset2 = -1;
			for ( int i = 0 ; i < 3 ; i++ )
			{
				for ( int j = 0 ; j < 3 ; j++ )
				{
					if ( ( triangle1[ i ] == triangle2[ ( j + 1 ) % 3 ] ) &&
						 ( triangle1[ ( i + 1 ) % 3 ] == triangle2[ j ] ) )
					{
						offset1 = i;
						offset2 = j;
						break;
					}
				}
			}

			if ( ( offset1 == -1 ) || ( offset2 == -1 ) )
			{
				result = triangles;
			}
			else
			{
				/*
				 * Determine the direction at each corner of the potential quad,
				 * as either clockwise or counter-clockwise. If all directions
				 * match, a quad is created.
				 */
				final int vertex1 = triangle1[ ( offset1 + 2 ) % 3 ];
				final int vertex2 = triangle1[ offset1 ];
				final int vertex3 = triangle2[ ( offset2 + 2 ) % 3 ];
				final int vertex4 = triangle2[ offset2 ];

				final Vector3D point1 = vertices.get( vertex1 );
				final Vector3D point2 = vertices.get( vertex2 );
				final Vector3D point3 = vertices.get( vertex3 );
				final Vector3D point4 = vertices.get( vertex4 );

				final Vector3D segment12 = point2.minus( point1 );
				final Vector3D segment23 = point3.minus( point2 );
				final Vector3D segment34 = point4.minus( point3 );
				final Vector3D segment41 = point1.minus( point4 );

				final Vector3D cross1 = Vector3D.cross( segment41 , segment12 );
				final Vector3D cross2 = Vector3D.cross( segment12 , segment23 );
				final Vector3D cross3 = Vector3D.cross( segment23 , segment34 );
				final Vector3D cross4 = Vector3D.cross( segment34 , segment41 );

				if ( ( cross1.z == cross2.z ) &&
				     ( cross2.z == cross3.z ) &&
				     ( cross3.z == cross4.z ) )
				{
					result = Collections.singleton( new int[] { vertex1 , vertex2 , vertex3 , vertex4 } );
				}
				else
				{
					result = triangles;
				}
			}
		}
		else
		{
			result = triangles;
		}

		return result;
	}
}
