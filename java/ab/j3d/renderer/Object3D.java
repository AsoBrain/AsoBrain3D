package ab.light3d.renderer;

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

import ab.light3d.Bounds3D;
import ab.light3d.Matrix3D;
import ab.light3d.TextureSpec;

/**
 * This class defined a 3D object node in a 3D tree. The 3D object consists of
 * vertices, edges, and faces.
 *
 * @version	$Revision$ ($Date$, $Author$)
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
	 * Faces of model. Faces are stored in an two dimensional array of integers.
	 * Faces are defined using indices in the vertex table to define the outer
	 * bounds of the face. A simple triangle will have 3 indices. Note that indices
	 * are absolute, so they come in multiples of 3.
	 */
	private int[][] _faceVert = {};

	/**
	 * Smoothing flag for faces of model.
	 */
	private boolean[] _faceSmooth = {};

	/**
	 * Face texture U coordinates. This two dimensional array contains U coordinates
	 * within the material texture matching the material (if the material has no
	 * texture, the matching entry in this array is ignored.
	 */
	int[][] _faceTU = {};

	/**
	 * Face texture V coordinates. This two dimensional array contains V coordinates
	 * within the material texture matching the material (if the material has no
	 * texture, the matching entry in this array is ignored.
	 */
	int[][] _faceTV = {};

	/**
	 * Array with material of each face of the model. Each face has an
	 * entry in the array with the corresponding material.
	 */
	TextureSpec[] _faceMat = null;

	/**
	 * Opacity value for faces of model.
	 */
	private float[] _faceOpacity = {};

	/**
	 * This internal flag is set to indicate that the vertices, edges, or
	 * faces changed and the normals need to be re-calculated.
	 */
	private boolean	_normalsDirty = true;

	/**
	 * Array of floats with normals of each face of the model. Normals are
	 * stored in an array of floats with a triplet for each normal (x,y,z).
	 * The number of normals is equal to the number of faces (obviously).
	 * <p>
	 * This is only calculated when the model changes (indicated by the
	 * _normalsDirty field).
	 */
	private	float[] _faceNormals = null;

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
	 * @param	xform				Transform to apply to vertices.
	 * @param	xs					X coordinates of 2D outline.
	 * @param	zs					Z coordinates of 2D outline.
	 * @param	detail				Number of segments around the Y-axis.
	 * @param	texture				Texture to apply to faces.
	 * @param	smoothCircumference	Set 'smooth' flag for circumference faces.
	 * @param	closeEnds			Close ends of shape (make solid).
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

		final int[][]	faces             = _faceVert;
		final int		faceCount         =  faces.length;
		final int		faceCountTimes3   =  faceCount * 3;
	 	final float[]	vertices          = _vertices;
	 	final int		vertexCount       =  vertices.length / 3;
	 	final int		vertexCountTimes3 =  vertices.length;

	 	float[] faceNormals = _faceNormals;
	 	if ( faceNormals == null || faceNormals.length < faceCountTimes3 )
	 		_faceNormals = faceNormals = new float[ faceCountTimes3 ];

	 	float[] vertexNormals = _vertexNormals;
	 	if ( vertexNormals == null || vertexNormals.length < vertexCountTimes3 )
	 		_vertexNormals = vertexNormals = new float[ vertexCountTimes3 ];

	 	/*
	 	 * Generate face normals, but do not normalize them yet.
	 	 */
		for ( int fi = 0 , ni = 0 ; fi < faceCount ; )
		{
			final int[] face = faces[ fi++ ];

			if ( face.length < 3 )
				continue;

			final int vi1 = face[ 0 ] * 3;
			final int vi2 = face[ 1 ] * 3;
			final int vi3 = face[ 2 ] * 3;

			final float u1 = vertices[ vi3     ] - vertices[ vi1     ];
			final float u2 = vertices[ vi3 + 1 ] - vertices[ vi1 + 1 ];
			final float u3 = vertices[ vi3 + 2 ] - vertices[ vi1 + 2 ];

			final float v1 = vertices[ vi2     ] - vertices[ vi1     ];
			final float v2 = vertices[ vi2 + 1 ] - vertices[ vi1 + 1 ];
			final float v3 = vertices[ vi2 + 2 ] - vertices[ vi1 + 2 ];

			final float r1 = u2 * v3 - u3 * v2;
			final float r2 = u3 * v1 - u1 * v3;
			final float r3 = u1 * v2 - u2 * v1;

			faceNormals[ ni++ ] = r1;
			faceNormals[ ni++ ] = r2;
			faceNormals[ ni++ ] = r3;
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
				final int[] face = faces[ fi ];

				for ( int fvi = face.length ; --fvi >= 0 ; )
				{
					if ( face[ fvi ] == vi )
					{
						final int ni = fi * 3;

						final float fnx = faceNormals[ ni     ];
						final float fny = faceNormals[ ni + 1 ];
						final float fnz = faceNormals[ ni + 2 ];

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
		for ( int i = 0 ; i < faceCountTimes3 ; i += 3 )
		{
			final float r1 = faceNormals[ i     ];
			final float r2 = faceNormals[ i + 1 ];
			final float r3 = faceNormals[ i + 2 ];

			if ( r1 != 0f || r2 != 0f || r3 != 0f )
			{
				final float l = (float)Math.sqrt( r1 * r1 + r2 * r2 + r3 * r3 );
				faceNormals[ i     ] = r1 / l;
				faceNormals[ i + 1 ] = r2 / l;
				faceNormals[ i + 2 ] = r3 / l;
			}
		}

		_normalsDirty = false;
	}

	/**
	 * Draws a line between the specified coordinates. Coordinates are transformed
	 * using the specified matrix.
	 *
	 * @param	g		Graphics context.
	 * @param	gXform 	Transform for coordinates on graphics context.
	 * @param	x1		X coordinate of line's start point.
	 * @param	y1		Y coordinate of line's start point.
	 * @param	x2		X coordinate of line's end point.
	 * @param	y2		Y coordinate of line's end point.
	 */
	public static void drawLine( final Graphics g , final Matrix3D gXform , final float x1 , final float y1 , final float x2 , final float y2 )
	{
		g.drawLine( (int)gXform.transformX( x1 , y1 , 0 ) ,
		            (int)gXform.transformY( x1 , y1 , 0 ) ,
		            (int)gXform.transformX( x2 , y2 , 0 ) ,
		            (int)gXform.transformY( x2 , y2 , 0 ) );
	}

	/**
	 * Draws a rectangle circle/oval with the specified coordinates. Coordinates
	 * are transformed using the specified matrix.
	 *
	 * @param	g		Graphics context.
	 * @param	gXform 	Transform for coordinates on graphics context.
	 * @param	x		X coordinate of lower-left corner.
	 * @param	y		Y coordinate of lower-left corder.
	 * @param	w		Width of oval.
	 * @param	h		Height of oval.
	 */
	public static void drawOval( final Graphics g , final Matrix3D gXform , final float x , final float y , final float w , final float h )
	{
		final int x1 = (int)gXform.transformX( x , y , 0 );
		final int y1 = (int)gXform.transformY( x , y , 0 );
		final int x2 = (int)gXform.transformX( x + w , y + h , 0 );
		final int y2 = (int)gXform.transformY( x + w , y + h , 0 );

		g.drawOval( Math.min( x1 , x2 ) , Math.min( y1 , y2 ) ,
		            Math.abs( x2 - x1 ) , Math.abs( y2 - y1 ) );
	}

	/**
	 * Get outer bounds (bounding box) of the object. Optionally, an existing bounding box
	 * can be specified. The resulting bounds contains all vertices within the object and
	 * the existing bounding box (if any).
	 *
	 * @param	xform		Transform to apply to vertices.
	 * @param	bounds		Existing bounding box to use.
	 *
	 * @return	Combined bounding box of this object and the existing bounding box (if any).
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
	 * Get number of faces in the model.
	 *
	 * @return	Number of faces.
	 */
	public final int getFaceCount()
	{
		return( _faceVert.length );
	}

	/**
	 * Get transformed face normals.
	 *
	 * @return	Transformed face normals.
	 */
	public final float[] getFaceNormals()
	{
		if ( _normalsDirty ) calculateNormals();
		return _faceNormals;
	}

	/**
	 * Get texture to use for rendering the specified face.
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Texture to used to render the face.
	 */
	public final TextureSpec getFaceTexture( final int face )
	{
		return ( _faceMat != null ? _faceMat[ face ] : null );
	}

	/**
	 * Get texture U coordinates for the specified face.
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Array with texture U coordinates of face.
	 */
	public final int[] getFaceTextureU( final int face )
	{
		return ( _faceTU == null ) ? null : _faceTU[ face ];
	}

	/**
	 * Get texture V coordinates for the specified face.
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Array with texture V coordinates of face.
	 */
	public final int[] getFaceTextureV( final int face )
	{
		return ( _faceTV == null ) ? null : _faceTV[ face ];
	}

	/**
	 * Get number of vertices used to define the specified face.
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Number of vertices used to define the face.
	 */
	public final int getFaceVertexCount( final int face )
	{
		return _faceVert[ face ].length;
	}

	/**
	 * Get vertex indices to define the shape of the specified face.
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Array of vertex indices to define the face shape.
	 */
	public final int[] getFaceVertexIndices( final int face )
	{
		return _faceVert[ face ];
	}

	/**
	 * Get number of vertices in the model.
	 *
	 * @return	Number of vertices.
	 */
	public final int getTotalVertexCount()
	{
		return( _vertices.length / 3 );
	}

	/**
	 * Get transformed vertex normals.
	 *
	 * @return	Transformed vertex normals.
	 */
	public final float[] getVertexNormals()
	{
		if ( _normalsDirty ) calculateNormals();
		return _vertexNormals;
	}

	/**
	 * Get vertices.
	 *
	 * @return	Float array with vertices that define the object.
	 */
	public final float[] getVertices()
	{
		return _vertices;
	}

	/**
	 * Get opacity of the specified face (0=transparent, 1=opaque).
	 *
	 * @param	face	Index of face.
	 *
	 * @return	Opacity of the specified face (0=transparent, 1=opaque).
	 */
	public final float getFaceOpacity( final int face )
	{
		return ( _faceOpacity != null ) ? _faceOpacity[ face ] : 1.0f;
	}

	/**
	 * Check wether the surface defined by the specified face is smooth (curved).
	 *
	 * @param	face	Index of face.
	 *
	 * @return	<code>true</code> if the surface face is smooth/curved;
	 *			<code>false</code> if the surface is flat.
	 */
	public final boolean isFaceSmooth( final int face )
	{
		return _faceSmooth != null ? _faceSmooth[ face ] : false;
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
	 * @param	g			Graphics context.
	 * @param	gXform		Transformation to pan/scale the graphics context.
	 * @param	objXform	Transformation from object's to view coordinate system.
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
				final float x1 = ver[ pts[ 0 ] * 3     ];
				final float y1 = ver[ pts[ 0 ] * 3 + 1 ];

				final float x2 = ver[ pts[ 1 ] * 3     ];
				final float y2 = ver[ pts[ 1 ] * 3 + 1 ];

				final float x3 = ver[ pts[ 2 ] * 3     ];
				final float y3 = ver[ pts[ 2 ] * 3 + 1 ];

				final float c = ( x1 - x2 ) * ( y3 - y2 ) - ( y1 - y2 ) * ( x3 - x2 );
				if ( c > 0 )
					continue;

				/*
				 * Now paint it.
				 */
				for ( int p = 0 ; p < pts.length ; p++ )
				{
					final int next = ( p + 1 ) % pts.length;

					drawLine( g , gXform ,
						ver[ pts[ p ] * 3 ] , ver[ pts[ p ] * 3 + 1 ] ,
						ver[ pts[ next ] * 3 ] , ver[ pts[ next ] * 3 + 1 ] );
				}
			}
		}
	}

	/**
	 * Set properties of this object.
	 *
	 * @param	vertices	Vertices of model (stored as x/y/z triplets).
	 * @param	faceVert	Faces of model using indices in the vertex table.
	 * @param	faceMat		Material of each face.
	 * @param	faceTU		Face texture U coordinates for each face.
	 * @param	faceTV		Face texture V coordinates for each face.
	 * @param	faceSmooth	Face smoothing flag for each face.
	 */
	public final void set( final float[] vertices , final int[][] faceVert , final TextureSpec[] faceMat , final int[][] faceTU , final int[][] faceTV , final float[] faceOpacity , final boolean[] faceSmooth )
	{
		final boolean hasTexture = ( faceMat != null && faceMat.length >= faceVert.length );

		_vertices    = vertices;
		_faceVert    = faceVert;
		_faceMat     = hasTexture ? faceMat : null;
		_faceTU      = hasTexture ? faceTU  : null;
		_faceTV      = hasTexture ? faceTV  : null;
		_faceOpacity = faceOpacity;
		_faceSmooth  = faceSmooth;

		_normalsDirty = true;
	}

	/**
	 * Set properties of this object.
	 *
	 * @param	vertices	Vertices of model (stored as x/y/z triplets).
	 * @param	faceVert	Faces of model using indices in the vertex table.
	 * @param	texture		Material for all faces in the model.
	 * @param	smooth		Smoothing flag for all faces.
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
}
