package ab.j3d.renderer;

/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2000-2002 - All Rights Reserved
 * (C) Copyright Peter S. Heijnen 1999-2002 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV or Peter S. Heijnen. Please
 * contact Numdata BV or Peter S. Heijnen for license information.
 */
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import ab.j3d.Bounds3D;
import ab.j3d.Matrix3D;
import ab.j3d.PolyPoint2D;
import ab.j3d.Polyline2D;
import ab.j3d.TextureSpec;
import ab.j3d.Vector3D;

/**
 * This class defined a 3D object node in a 3D tree. The 3D object consists of
 * vertices, edges, and faces.
 *
 * @author	Peter S. Heijnen
 * @version	$Revision$ ($Date$, $Author$)
 */
public class Object3D
	extends TreeNode
{
	/**
	 * Vertices of model. Vertices are stored in an array of floats with
	 * a triplet for each vertex (x,y,z).
	 */
	private float[] _vertices = {};

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field).
	 */
	private	float[] _vertexNormals = null;

	/**
	 * List of faces in this object.
	 */
	private final List _faces = new ArrayList();

	/**
	 * This internal flag is set to indicate that the vertices, edges, or
	 * faces changed and the normals need to be re-calculated.
	 */
	private boolean _normalsDirty = true;

	/**
	 * This is used as cache storage for paint(Graphics,Matrix3D,Matrix3D).
	 */
	private static final float[] _paintVertexCache = {};

	/**
	 * Construct base object. Additional properties need to be set to make the
	 * object usable.
	 */
	public Object3D()
	{
	}

	/**
	 * This constructor can be used to create a 3D object that is constructed
	 * using a 2D outline which is rotated around the Y-axis. If requested,
	 * the ends will be closed (if not done so already).
	 *
	 * The 'detail' parameter is used to specify the number of segments used
	 * to rotate around the Z-axis (minimum: 3).
	 *
	 * NOTE: To construct an outer surface, use increasing values for y!
	 *
	 * @param   xform               Transform to apply to vertices.
	 * @param   xs                  X coordinates of 2D outline.
	 * @param   zs                  Z coordinates of 2D outline.
	 * @param   detail              Number of segments around the Y-axis.
	 * @param   texture             Texture to apply to faces.
	 * @param   smoothCircumference Set 'smooth' flag for circumference faces.
	 * @param   closeEnds           Close ends of shape (make solid).
	 */
	public Object3D( final Matrix3D xform , final float[] xs , final float[] zs , final int detail , final TextureSpec texture , final boolean smoothCircumference , final boolean closeEnds )
	{
		float[]       vertices    = new float[ xs.length * detail * 3 ];
		int[][]       faceVert    = new int[ xs.length * detail ][];
		TextureSpec[] faceMat     = new TextureSpec[ faceVert.length ];
		float[]       faceOpacity = new float[ faceVert.length ];
		boolean[]     faceSmooth  = new boolean[ faceVert.length ];
		int v = 0;
		int f = 0;

		int		iPrev;
		float	xPrev;

		int		iCur = v / 3;
		float	xCur = 0;
		float	zCur;

		for ( int i = 0 ; i < xs.length ; i++ )
		{
			iPrev = iCur;
			xPrev = xCur;

			iCur = v / 3;
			xCur = xs[ i ];
			zCur = zs[ i ];

			if ( xCur == 0f )
			{
				vertices[ v++ ] = 0;
				vertices[ v++ ] = 0;
				vertices[ v++ ] = zCur;
			}
			else for ( int j = 0 ; j < detail ; j++ )
			{
				final float a = (float)( j * 2 * Math.PI / detail );

				vertices[ v++ ] =  (float)(Math.sin( a ) * xCur);
				vertices[ v++ ] = -(float)(Math.cos( a ) * xCur);
				vertices[ v++ ] = zCur;
			}

			/*
			 * First control point. Add closing face is requested.
			 */
			if ( i == 0 )
			{
				if ( xCur != 0.0f && closeEnds )
				{
					final int[] fv = new int[ detail ];
					for ( int j = 0 ; j < detail ; j++ )
						fv[ j ] = iCur + j;

					faceVert   [ f   ] = fv;
					faceMat    [ f   ] = texture;
					faceOpacity[ f   ] = 1.0f;
					faceSmooth [ f++ ] = false;
				}
			}
			/*
			 * 2nd + later control points.
			 */
			else if ( xCur != 0.0f || xPrev != 0.0f )
			{
				for ( int j = 0 ; j < detail ; j++ )
				{
					final int nextJ = ( j + 1 ) % detail;

					if ( xCur != 0.0f && xPrev != 0 )
						faceVert[ f ] = new int[] { iPrev + j , iCur + j , iCur + nextJ , iPrev + nextJ };
					else if ( xCur != 0.0f )
						faceVert[ f ] = new int[] { iPrev , iCur + j , iCur + nextJ };
					else /*if ( xPrev != 0f )*/
						faceVert[ f ] = new int[] { iPrev + j , iCur , iPrev + nextJ };

					faceMat    [ f   ] = texture;
					faceOpacity[ f   ] = 1.0f;
					faceSmooth [ f++ ] = smoothCircumference;
				}
			}
		}

		/*
		 * Add closing face is requested.
		 */
		if ( xCur != 0.0f && closeEnds )
		{
			final int[] fv = new int[ detail ];
			for ( int j = 0 ; j < detail ; j++ )
				fv[ j ] = iCur + detail - ( 1 + j );

			faceVert   [ f   ] = fv;
			faceMat    [ f   ] = texture;
			faceOpacity[ f   ] = 1.0f;
			faceSmooth [ f++ ] = false;
		}


		/*
		 * Construct final vertex and face arrays
		 */
		if ( v != vertices.length )
		{
			final float[] newVertices = new float[ v ];
			System.arraycopy( vertices , 0 , newVertices , 0 , v );
			vertices = newVertices;
		}

		if ( f != faceVert.length )
		{
			final int[][] newFaces = new int[ f ][];
			System.arraycopy( faceVert , 0 , newFaces , 0 , f );
			faceVert = newFaces;

			final TextureSpec[] newMat = new TextureSpec[ f ];
			System.arraycopy( faceMat , 0 , newMat , 0 , f );
			faceMat = newMat;

			final float[] newOpacity = new float[ f ];
			System.arraycopy( faceOpacity , 0 , newOpacity , 0 , f );
			faceOpacity = newOpacity;

			final boolean[] newSmooth = new boolean[ f ];
			System.arraycopy( faceSmooth , 0 , newSmooth , 0 , f );
			faceSmooth = newSmooth;
		}

		/*
		 * Transform vertices
		 */
		if ( xform != null )
			xform.transform( vertices , vertices , vertices.length / 3 );

		set( vertices , faceVert , faceMat , null , null , faceOpacity , faceSmooth );
	}

	/**
	 * The following text is from http://nate.scuzzy.net/normals/normals.html.
	 *
	 * There is no way to calculate a true vertex normal since a vertex is just
	 * a point in space, so the best we can get is an approximation for our vertex
	 * normals. If you understand the ideas behind face normals, calculating vertex
	 * normals will be a breeze. A vertex normal is simply the average of the face
	 * normals that surround a particular vertex. So how do you go about
	 * calculating a vertex normal? For every face in your face list you need to
	 * generate the normal to that face but, do not normalize it. Then for each
	 * vertex in the face add the face normal to the corresponding vertex normal
	 * for the current vertex. After you have gone through all the verticies of
	 * the face you then can normalize the face normal, do not normalize the vertex
	 * normal. After you have done this for all faces walk the vertex normal list
	 * and normalize each normal. Once you have done this you have your vertex
	 * normals. Pretty simple isn't it? Thanks to hude for this better method for
	 * calculating vertex normals.
	 *
	 * PS: We do not follow the suggested implementation in this article, but use
	 *     its suggested calculations.
	 */
	private synchronized void calculateNormals()
	{
		if ( !_normalsDirty )
			return;

		final int       faceCount         =  _faces.size();
	 	final float[]   vertices          = _vertices;
	 	final int       vertexCount       =  vertices.length / 3;
	 	final int       vertexCountTimes3 =  vertices.length;

	 	float[] vertexNormals = _vertexNormals;
	 	if ( vertexNormals == null || vertexNormals.length < vertexCountTimes3 )
	 		_vertexNormals = vertexNormals = new float[ vertexCountTimes3 ];

	 	/*
	 	 * Generate face normals, but do not normalize them yet.
	 	 */
		for ( int fi = 0 ; fi < faceCount ; )
		{
			final Face face = getFace( fi++ );

			if ( face.getPointCount() < 3 )
				continue;

			final int[] pointIndices = face.getPointIndices();
			final int vi1 = pointIndices[ 0 ] * 3;
			final int vi2 = pointIndices[ 1 ] * 3;
			final int vi3 = pointIndices[ 2 ] * 3;

			final float u1 = vertices[ vi3     ] - vertices[ vi1     ];
			final float u2 = vertices[ vi3 + 1 ] - vertices[ vi1 + 1 ];
			final float u3 = vertices[ vi3 + 2 ] - vertices[ vi1 + 2 ];

			final float v1 = vertices[ vi2     ] - vertices[ vi1     ];
			final float v2 = vertices[ vi2 + 1 ] - vertices[ vi1 + 1 ];
			final float v3 = vertices[ vi2 + 2 ] - vertices[ vi1 + 2 ];

			final float r1 = u2 * v3 - u3 * v2;
			final float r2 = u3 * v1 - u1 * v3;
			final float r3 = u1 * v2 - u2 * v1;

			face.setNormal( r1 , r2 , r3 );
		}

	 	/*
	 	 * Generate vertex normals.
	 	 */
	 	for ( int vi = 0 ; vi < vertexCount ; vi++ )
	 	{
		 	float vnx = 0f;
		 	float vny = 0f;
		 	float vnz = 0f;

		 	/*
		 	 * Sum normals of faces that use this vertex.
		 	 */
		 	for ( int fi = 0 ; fi < faceCount ; fi++ )
		 	{
				final Face face    = (Face)_faces.get( fi );
				final int nrPoints = face.getPointCount();

				for ( int fvi = nrPoints ; --fvi >= 0 ; )
				{
					if ( face.getPointIndices()[ fvi ] == vi )
					{
						final float[] faceNormal = face.getNormal();
						final float fnx = faceNormal[ 0 ];
						final float fny = faceNormal[ 1 ];
						final float fnz = faceNormal[ 2 ];

						if ( fnx != 0 || fny != 0 || fnz != 0 )
						{
							vnx += fnx;
							vny += fny;
							vnz += fnz;
						}
						break;
					}
				}
		 	}

		 	/*
		 	 * Normalize vertex normal.
		 	 */
			if ( vnx != 0f || vny != 0f || vnz != 0f )
			{
				final float l = (float)Math.sqrt( vnx * vnx + vny * vny + vnz * vnz );
				vnx /= l;
				vny /= l;
				vnz /= l;
			}

			final int ni = vi * 3;
			vertexNormals[ ni     ] = vnx;
			vertexNormals[ ni + 1 ] = vny;
			vertexNormals[ ni + 2 ] = vnz;
	 	}

	 	/*
	 	 * Normalize face normals.
	 	 */
		for ( int i = 0 ; i < _faces.size() ; i++ )
		{
			final Face    face       = getFace( i );
			final float[] faceNormal = face.getNormal();

			final float r1 = faceNormal[ 0 ];
			final float r2 = faceNormal[ 1 ];
			final float r3 = faceNormal[ 2 ];

			if ( r1 != 0f || r2 != 0f || r3 != 0f )
			{
				final float l = (float)Math.sqrt( r1 * r1 + r2 * r2 + r3 * r3 );
				final float fnx = r1 / l;
				final float fny = r2 / l;
				final float fnz = r3 / l;

				face.setNormal( fnx , fny , fnz );
			}
		}

		_normalsDirty = false;
	}

	/**
	 * Get outer bounds (bounding box) of the object. Optionally, an existing bounding box
	 * can be specified. The resulting bounds contains all vertices within the object and
	 * the existing bounding box (if any).
	 *
	 * @param   xform       Transform to apply to vertices.
	 * @param   bounds      Existing bounding box to use.
	 *
	 * @return  Combined bounding box of this object and the existing bounding box (if any).
	 */
	public final Bounds3D getBounds( final Matrix3D xform , final Bounds3D bounds )
	{
		if ( _vertices == null || _vertices.length < 3 )
			return bounds;

		final boolean isXform = ( xform != null && xform != Matrix3D.INIT && !Matrix3D.INIT.equals( xform ) );

		float x1,y1,z1,x2,y2,z2;
		final Bounds3D result;
		if ( bounds != null )
		{
			x1 = bounds.v1.x;
			y1 = bounds.v1.y;
			z1 = bounds.v1.z;
			x2 = bounds.v2.x;
			y2 = bounds.v2.y;
			z2 = bounds.v2.z;
			result = bounds;
		}
		else
		{
			x1 = Float.MAX_VALUE;
			y1 = Float.MAX_VALUE;
			z1 = Float.MAX_VALUE;
			x2 = Float.MIN_VALUE;
			y2 = Float.MIN_VALUE;
			z2 = Float.MIN_VALUE;
			result = Bounds3D.INIT;
		}

		float x,y,z,tx,ty;
		for ( int i = 0 ; i < _vertices.length ; )
		{
			x = _vertices[ i++ ];
			y = _vertices[ i++ ];
			z = _vertices[ i++ ];

			if ( isXform )
			{
				tx = xform.transformX( x , y , z );
				ty = xform.transformY( x , y , z );
				z  = xform.transformZ( x , y , z );
				x  = tx;
				y  = ty;
			}

			if ( x < x1 ) x1 = x;
			if ( y < y1 ) y1 = y;
			if ( z < z1 ) z1 = z;
			if ( x > x2 ) x2 = x;
			if ( y > y2 ) y2 = y;
			if ( z > z2 ) z2 = z;
		}

		if ( x1 > x2 || y1 > y2 || z1 > z2 )
			return null;

		return result.set( result.v1.set( x1 , y1 , z1 ) , result.v2.set( x2 , y2 , z2 ) );
	}

	/**
	 * Get number of vertices in the model.
	 *
	 * @return  Number of vertices.
	 */
	public final int getTotalVertexCount()
	{
		return( _vertices.length / 3 );
	}

	/**
	 * Get transformed vertex normals.
	 *
	 * @return  Transformed vertex normals.
	 */
	public final float[] getVertexNormals()
	{
		if ( _normalsDirty ) calculateNormals();
		return _vertexNormals;
	}

	/**
	 * Get vertices.
	 *
	 * @return  Float array with vertices that define the object.
	 */
	public final float[] getVertices()
	{
		return _vertices;
	}

	/**
	 * Add point to this <code>Object3D</code>.
	 *
	 * @param   x   X-coordinate of the point.
	 * @param   y   Y-coordinate of the point.
	 * @param   z   Z-coordinate of the point.
	 */
	public void addPoint( final float x , final float y , final float z )
	{
		final int objectPointCount = getTotalVertexCount() * 3;
		ensureCapacity( objectPointCount + 3 );

		_vertices[ objectPointCount     ] = x;
		_vertices[ objectPointCount + 1 ] = y;
		_vertices[ objectPointCount + 2 ] = z;

		_normalsDirty = true;
	}

	/**
	 * Get the index of the specified point in the <code>_vertices</code> list.
	 *
	 * @param   x   x-coordinate of the point.
	 * @param   y   y-coordinate of the point.
	 * @param   z   z-coordinate of the point.
	 *
	 * @return  The index of the specified point in the <code>_vertices</code> list;
	 *          -1 if the specified point is not in the <code>_vertices</code> list.
	 */
	public int getPointIndex( final float x , final float y , final float z )
	{
		boolean found = false;
		int     index;
		for ( index = 0 ; index < getTotalVertexCount() && !found ; index++ )
		{
			final float vx = _vertices[ index * 3     ];
			final float vy = _vertices[ index * 3 + 1 ];
			final float vz = _vertices[ index * 3 + 2 ];

			found = ( x == vx && y == vy && z == vz );
		}

		return found ? index - 1 : -1;
	}

	private void ensureCapacity( final int length )
	{
		final int vertexCount = getTotalVertexCount();

		if ( _vertices == null || vertexCount < length )
		{
			float[] temp;

			if ( _vertices != null )
			{
				temp  = new float[ length ];

				System.arraycopy( _vertices , 0 , temp , 0 , vertexCount * 3 );
				_vertices = temp;
			}
			else
			{
				_vertices = new float[ length ];
			}

			if ( _vertexNormals != null )
			{
				temp  = new float[ length ];

				System.arraycopy( _vertexNormals, 0 , temp , 0 , vertexCount * 3 );
				_vertexNormals = temp;
			}
			else
			{
				_vertexNormals = new float[ length ];
			}
		}
	}

	/**
	 * Paint 2D representation of this 3D object. The object coordinates are
	 * transformed using the objXform argument. By default, the object is painted
	 * by drawing the outlines of its 'visible' faces. Derivatives of this class
	 * may implement are more realistic approach (sphere, cylinder).
	 *
	 * Objects are painted on the specified graphics context after being
	 * transformed again by gXform. This may be used to pan/scale the object on
	 * the graphics context (NOTE: IT MAY NOT ROTATE THE OBJECT!).
	 *
	 * @param   g           Graphics context.
	 * @param   gXform      Transformation to pan/scale the graphics context.
	 * @param   objXform    Transformation from object's to view coordinate system.
	 */
	public void paint( final Graphics g , final Matrix3D gXform , final Matrix3D objXform )
	{
		/*
		 * If the array is to small, create a larger one.
		 */
		float[] ver;
		synchronized ( _paintVertexCache )
		{
			ver = _paintVertexCache;
			if ( ver.length < _vertices.length )
				ver = new float[ _vertices.length ];
		}

		synchronized ( ver )
		{
			objXform.transform( _vertices , ver , _vertices.length / 3 );

			for ( int f = 0 ; f < getFaceCount() ; f++ )
			{
				final int[] pts = getFaceVertexIndices( f );

				/*
				 * Perform backface removal
				 *
				 * c = (x1-x2)*(y3-y2)-(y1-y2)*(x3-x2)
				 */
				int i = pts[ pts.length - 1 ] * 3;
				final int ix1 = (int)gXform.transformX( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );
				final int iy1 = (int)gXform.transformY( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );

				i = pts[ 0 ] * 3;
				int ix2 = (int)gXform.transformX( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );
				int iy2 = (int)gXform.transformY( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );

				i = pts[ 1 ] * 3;
				int ix3 = (int)gXform.transformX( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );
				int iy3 = (int)gXform.transformY( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );

				final float c = ( ( ix1 - ix2 ) * ( iy3 - iy2 ) ) - ( ( iy1 - iy2 ) * ( ix3 - ix2 ) );
				if ( c <= 0 )
				{
					g.drawLine( ix1 , iy1 , ix2 , iy2 );
					g.drawLine( ix2 , iy2 , ix3 , iy3 );

					for ( int p = 2 ; p < pts.length - 1 ; p++ )
					{
						i = pts[ p ] * 3;
						ix2 = ix3;
						iy2 = iy3;
						ix3 = (int)gXform.transformX( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );
						iy3 = (int)gXform.transformY( ver[ i ] , ver[ i + 1 ] , ver[ i + 2 ] );

						g.drawLine( ix2 , iy2 , ix3 , iy3 );
					}
				}
			}
		}
	}

	/**
	 * Set properties of this object.
	 *
	 * @param   vertices    Vertices of model (stored as x/y/z triplets).
	 * @param   faceVert    Faces of model using indices in the vertex table.
	 * @param   faceMat     Material of each face.
	 * @param   faceTU      Face texture U coordinates for each face.
	 * @param   faceTV      Face texture V coordinates for each face.
	 * @param   faceOpacity Face opacity (0=transparent, 1=opaque).
	 * @param   faceSmooth  Face smoothing flag for each face.
	 */
	public final void set( final float[] vertices , final int[][] faceVert , final TextureSpec[] faceMat , final int[][] faceTU , final int[][] faceTV , final float[] faceOpacity , final boolean[] faceSmooth )
	{
		_vertices  = vertices;

		final boolean hasTexture = ( faceMat != null && faceMat.length >= faceVert.length );
		for ( int i = 0 ; i < faceVert.length ; i++ )
		{
			final int[]       f_vertices = faceVert[ i ];
			final TextureSpec f_material = hasTexture ? faceMat[ i ] : null;
			final int[]       f_textureU = ( hasTexture && faceTU != null ) ? faceTU [ i ] : null; // UGLY!
			final int[]       f_textureV = ( hasTexture && faceTV != null ) ? faceTV [ i ] : null; // UGLY!
			final float       f_opacity  = faceOpacity != null ? faceOpacity[ i ] : 1.0f;
			final boolean     f_smooth   = faceSmooth [ i ];

			final int[] points    = new int[ f_vertices.length ];
			final int[] textureUs = new int[ f_vertices.length ];
			final int[] textureVs = new int[ f_vertices.length ];

			for ( int j = 0 ; j < f_vertices.length ; j++ )
			{
				final int pointIndex = f_vertices[ j ];
				final int textureU   = ( hasTexture && f_textureU != null ) ? f_textureU[ j ] : -1;
				final int textureV   = ( hasTexture && f_textureV != null ) ? f_textureV[ j ] : -1;

				points   [ j ] = pointIndex;
				textureUs[ j ] = textureU;
				textureVs[ j ] = textureV;
			}

			final Face face = new Face( points , f_material , textureUs , textureVs , f_opacity , f_smooth );
			addFace( face );
		}

		_normalsDirty = true;
	}

	/**
	 * Set properties of this object.
	 *
	 * @param   vertices    Vertices of model (stored as x/y/z triplets).
	 * @param   faceVert    Faces of model using indices in the vertex table.
	 * @param   texture     Material for all faces in the model.
	 * @param   smooth      Smoothing flag for all faces.
	 */
	public final void set( final float[] vertices , final int[][] faceVert , final TextureSpec texture , final boolean smooth )
	{
		final TextureSpec[] faceMat = new TextureSpec[ faceVert.length ];
		for ( int i = 0 ; i < faceMat.length ; i++ )
			faceMat[ i ] = texture;

		final boolean[] faceSmooth = new boolean[ faceVert.length ];
		for ( int i = 0 ; i < faceSmooth.length ; i++ )
			faceSmooth[ i ] = smooth;

		set( vertices , faceVert ,  faceMat , null , null , null , faceSmooth );
	}

	/**
	 * Add a face to this <code>Object3D</code>.
	 *
	 * @param   face    Face to add.
	 */
	public void addFace( final Face face )
	{
		_faces.add( face );
	}

	/**
	 * Add empty face to the 3D object. Vertices should be added to this shape
	 * by the caller.
	 *
	 * @param   base        Location/orientation of face.
	 * @param   shape       Shape of face relative to the base.
	 * @param   reversePath If set, the returned path will be reversed.
	 * @param   texture     Texture to apply to the face.
	 * @param   opacity     Opacity of face (0=transparent, 1=opaque).
	 * @param   swapUV      Swap texture coordinates to rotate 90 degrees.
	 * @param   smooth      Face is smooth/curved vs. flat.
	 */
	public void addFace( final Matrix3D base , final Polyline2D shape , final boolean reversePath , final TextureSpec texture , final float opacity , final boolean swapUV , final boolean smooth )
	{
		final int nrVertices = shape.getPointCount() + ( shape.isClosed() ? -1 : 0 );
		if ( nrVertices < 3 )
			return;

		final Face face;
		if ( ( texture != null ) && texture.isTexture() )
		{
			final float txBase = -base.xo * base.xx - base.yo * base.yx - base.zo * base.zx;
			final float tyBase = -base.xo * base.xy - base.yo * base.yy - base.zo * base.zy;

			final int[] vertU = new int[ nrVertices ];
			final int[] vertV = new int[ nrVertices ];

			int minU = 0;
			int maxU = 0;
			int minV = 0;
			int maxV = 0;

			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );

				final int u = (int)( ( swapUV ? ( tyBase + point.y ) : ( txBase + point.x ) ) * texture.textureScale );
				final int v = (int)( ( swapUV ? ( txBase + point.x ) : ( tyBase + point.y ) ) * texture.textureScale );

				if ( i == 0 || u < minU ) minU = u;
				if ( i == 0 || u > maxU ) maxU = u;
				if ( i == 0 || v < minV ) minV = v;
				if ( i == 0 || v > maxV ) maxV = v;

				vertU[ i ] = u;
				vertV[ i ] = v;
			}

			final int adjustU = getRangeAdjustment( minU , texture.getTextureWidth( null ) );
			final int adjustV = getRangeAdjustment( minV , texture.getTextureHeight( null ) );

			face = new Face( null , texture , null , null , opacity , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , vertU[ i ] + adjustU , vertV[ i ] + adjustV );
			}
		}
		else
		{
			face = new Face( null , texture , null , null , opacity , smooth );
			for ( int i = 0 ; i < nrVertices ; i++ )
			{
				final PolyPoint2D point = shape.getPoint( reversePath ? nrVertices - 1 - i : i );
				face.addVertex( base.multiply( point.x , point.y , 0 ) , 0 , 0 );
			}
		}

		addFace( face );
	}

	/**
	 * Get number of faces in the model.
	 *
	 * @return  Number of faces.
	 */
	public int getFaceCount()
	{
		return _faces.size();
	}

	/**
	 * Get face with the specified index.
	 *
	 * @param   index   Index of face to retrieve.
	 *
	 * @return  Face with the specified index.
	 */
	public Face getFace( final int index )
	{
		return (Face)_faces.get( index );
	}

	/**
	 * Get transformed face normals.
	 *
	 * @return  Transformed face normals.
	 */
	public final float[] getFaceNormals()
	{
		if ( _normalsDirty ) calculateNormals();

		final float[] faceNormals = new float[ getFaceCount() * 3 ];

		for ( int i = 0 ; i < getFaceCount() ; i++ )
		{
			final Face    face       = getFace( i );
			final float[] faceNormal = face.getNormal();

			faceNormals[ i * 3     ] = faceNormal[ 0 ];
			faceNormals[ i * 3 + 1 ] = faceNormal[ 1 ];
			faceNormals[ i * 3 + 2 ] = faceNormal[ 2 ];
		}

		return faceNormals;
	}

	/**
	 * Check wether the surface defined by the specified face is smooth (curved).
	 *
	 * @param   face    Index of face.
	 *
	 * @return  <code>true</code> if the surface face is smooth/curved;
	 *          <code>false</code> if the surface is flat.
	 */
	public final boolean isFaceSmooth( final int face )
	{
		return ( (Face)_faces.get( face ) ).isSmooth();
	}

	/**
	 * Get texture to use for rendering the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Texture to used to render the face.
	 */
	public final TextureSpec getFaceTexture( final int face )
	{
		return getFace( face ).getTexture();
	}

	/**
	 * Get texture U coordinates for the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array with texture U coordinates of face.
	 */
	public final int[] getFaceTextureU( final int face )
	{
		return getFace( face ).getTextureU();
	}

	/**
	 * Get texture V coordinates for the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array with texture V coordinates of face.
	 */
	public final int[] getFaceTextureV( final int face )
	{
		return getFace( face ).getTextureV();
	}

	/**
	 * Get number of vertices used to define the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Number of vertices used to define the face.
	 */
	public final int getFaceVertexCount( final int face )
	{
		return getFace( face ).getPointCount();
	}

	/**
	 * Get vertex indices to define the shape of the specified face.
	 *
	 * @param   face    Index of face.
	 *
	 * @return  Array of vertex indices to define the face shape.
	 */
	public final int[] getFaceVertexIndices( final int face )
	{
		return getFace( face ).getPointIndices();
	}

	/**
	 * Helper method to determine an adjustment value to get a reference value in
	 * a range between 0 and a specified maximum value - 1. The adjustment value
	 * will always be a multiple of the specified maximum value.
	 * <p />
	 * This is used to adjust minimum texture coordinates to the lowest positive
	 * coordinate within the texture image. The adjustment value should be added
	 * to all texture coordinates to make the adjustment.
	 *
	 * @param   value   Value to adjust.
	 * @param   range   Specifies the range 0 to (range - 1).
	 *
	 * @return  Adjustment value to add to the value.
	 */
	public static int getRangeAdjustment( final int value , final int range )
	{
		final int result;
		if ( value < 0 )
		{
			result = ( -value + range - 1 ) / range * range;
		}
		else
		{
			result = -value + ( value % range );
		}
		return result;
	}

	/**
	 * This class defines a 3D face of a 3D object.
	 */
	public class Face
	{
		/**
		 * Point indices of this face. These indices indicate the index of the
		 * point in the <code>_vertices</code> array of the <code>Object3D</code>.
		 * Because points in the <code>_vertices</code> array are stored with a
		 * triplet for each vertex (x,y,z), these indices should be multiplied
		 * by 3 to get the 'real' index.
		 */
		private int[] _pointIndices;

		/**
		 * Number of points of this face.
		 */
		private int _pointCount;

		/**
		 * Normal of this face.
		 */
		public float[] _normal;

		/**
		* Smoothing flag this face.
		*/
		private boolean _smooth;

		/**
		 * Face texture U coordinates.
		 */
		int[] _textureU;

		/**
		 * Face texture V coordinates.
		 */
		int[] _textureV;

		/**
		 * Texture of this face.
		 */
		TextureSpec _texture;

		/**
		 * Opacity value for this face.
		 */
		private float _opacity;

		/**
		 * Construct new Face.
		 *
		 * @param   points  List of points.
		 * @param   texture Texture to apply to the face.
		 * @param   tU      Array for horizontal texture coordinates.
		 * @param   tV      Array for vertical texture coordinates.
		 * @param   opacity Opacity of face (0=transparent, 1=opaque).
		 * @param   smooth  Face is smooth/curved vs. flat.
		 */
		public Face( final int[] points , final TextureSpec texture , final int[] tU , final int[] tV , final float opacity , final boolean smooth )
		{
			_pointIndices = points;
			_pointCount   = points != null ? points.length : 0;
			_normal       = new float[ 3 ];
			_texture      = texture;
			_textureU     = tU;
			_textureV     = tV;
			_opacity      = opacity;
			_smooth       = smooth;
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex. The vertex texture U and V coordinates are set to -1.
		 *
		 * @param   x   X-coordinate of vertex to add.
		 * @param   y   Y-coordinate of vertex to add.
		 * @param   z   Z-coordinate of vertex to add.
		 */
		public void addVertex( final float x , final float y , final float z )
		{
			addVertex( x , y , z , -1 , -1 );
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex. The vertex texture U and V coordinates are set to -1.
		 *
		 * @param   point   Point that specifies the vertex x-, y- and z-coordinates.
		 */
		public void addVertex( final Vector3D point )
		{
			addVertex( point.x , point.y , point.z , -1 , -1 );
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex.
		 *
		 * @param   x   X-coordinate of vertex to add.
		 * @param   y   Y-coordinate of vertex to add.
		 * @param   z   Z-coordinate of vertex to add.
		 * @param   tU  Horizontal texture coordinate.
		 * @param   tV  Vertical texture coordinate.
		 */
		public void addVertex( final float x , final float y , final float z , final int tU , final int tV )
		{
			int pointIndex = getPointIndex( x ,y ,z );

			if ( pointIndex == -1 )
			{
				addPoint( x , y , z );
				pointIndex = getTotalVertexCount() - 1;
			}

			ensureCapacity( _pointCount + 1 );
			_pointIndices[ _pointCount ] = pointIndex;
			_textureU    [ _pointCount ] = tU;
			_textureV    [ _pointCount ] = tV;

			_pointCount++;
		}

		/**
		 * Add vertex to face. Note that the last vertex is automatically connected
		 * to the first vertex.
		 *
		 * @param   point   Point that specifies the vertex x-, y- and z-coordinates.
		 * @param   tU      Horizontal texture coordinate.
		 * @param   tV      Vertical texture coordinate.
		 */
		public void addVertex( final Vector3D point , final int tU , final int tV )
		{
			addVertex( point.x , point.y , point.z , tU , tV );
		}

		/**
		 * Get number of vertices that define this face.
		 *
		 * @return  Number of vertices.
		 */
		public int getPointCount()
		{
			return _pointCount;
		}

		/**
		 * Get point with the specified index from this face.
		 *
		 * @param   index   Point index.
		 *
		 * @return  Vertex with the specified index.
		 */
		public Vector3D getPoint( final int index )
		{
			final int pointIndex = _pointIndices[ index ] * 3;
			final float x = _vertices[ pointIndex     ];
			final float y = _vertices[ pointIndex + 1 ];
			final float z = _vertices[ pointIndex + 2 ];

			return Vector3D.INIT.set( x , y , z );
		}

		/**
		 * Get the point indices of this face. These indices indicate the index of
		 * the point in the <code>_vertices</code> array of the <code>Object3D</code>.
		 * Because points in the <code>_vertices</code> array are stored with a
		 * triplet for each vertex (x,y,z), these indices should be multiplied
		 * by 3 to get the 'real' index.
		 *
		 * @return  The point indices of this face.
		 */
		public int[] getPointIndices()
		{
			return _pointIndices;
		}

		/**
		 * Get the smoothing flag of this face.
		 *
		 * @return  The smoothing flag of this face.
		 */
		public boolean isSmooth()
		{
			return _smooth;
		}

		/**
		 * Get the horizontal texture coordinates of this face.
		 *
		 * @return  The horizontal texture coordinates of this face.
		 */
		public int[] getTextureU()
		{
			return _textureU;
		}

		/**
		 * Get the vertical texture coordinates of this face.
		 *
		 * @return  The vertical texture coordinates of this face.
		 */
		public int[] getTextureV()
		{
			return _textureV;
		}

		/**
		 * Get the texture of this face.
		 *
		 * @return  The texture of this face.
		 */
		public TextureSpec getTexture()
		{
			return _texture;
		}

		/**
		 * Get the opacity of this face.
		 *
		 * @return  The opacity of this face.
		 */
		public float getOpacity()
		{
			return _opacity;
		}

		/**
		 * Set the normal of this face.
		 *
		 * @param   x   x-coordinate of the normal of this face.
		 * @param   y   y-coordinate of the normal of this face.
		 * @param   z   z-coordinate of the normal of this face.
		 */
		public void setNormal( final float x , final float y , final float z )
		{
			_normal[ 0 ] = x;
			_normal[ 1 ] = y;
			_normal[ 2 ] = z;
		}

		/**
		 * Get the normal of this face.
		 *
		 * @return  The normal of this face.
		 */
		public float[] getNormal()
		{
			return _normal;
		}

		private void ensureCapacity( final int length )
		{
			if ( _pointIndices == null || _pointIndices.length < length )
			{
				int[] temp;

				if ( _pointIndices != null )
				{
					temp = new int[ length ];

					System.arraycopy( _pointIndices , 0 , temp , 0 , _pointCount );
					_pointIndices = temp;
				}
				else
				{
					_pointIndices = new int[ length ];
				}

				if ( _textureU != null )
				{
					temp  = new int[ length ];

					System.arraycopy( _textureU , 0 , temp , 0 , _pointCount );
					_textureU = temp;
				}
				else
				{
					_textureU = new int[ length ];
				}

				if ( _textureV != null )
				{
					temp  = new int[ length ];

					System.arraycopy( _textureV , 0 , temp , 0 , _pointCount );
					_textureV = temp;
				}
				else
				{
					_textureV = new int[ length ];
				}
			}
		}
	}
}
