package common.renderer;

import java.awt.Graphics;
import java.util.Hashtable;
import common.db.TextureSpec;
import common.model.Matrix3D;

/**
 * This class defines a 3D sphere.
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 */
public class Sphere3D
	extends Object3D
{
	public final Matrix3D xform;
	public final float   dx;
	public final float   dy;
	public final float   dz;

	/**
	 * Constructor for sphere.
	 *
	 * @param	xform		Transformation to apply to the object's vertices.
	 * @param	width		Width of sphere (x-axis).
	 * @param	height		Height of sphere (y-axis).
	 * @param	depth		Depth of sphere (z-axis).
	 * @param	p			Number of faces around Y-axis to approximate the sphere.
	 * @param	q			Number of faces around X/Z-axis to approximate the sphere.
	 * @param	texture		Texture of sphere.
	 * @param	smooth		Smooth surface.
	 */
	public Sphere3D( final Matrix3D xform , final float dx , final float dy , final float dz , final int p , final int q , final TextureSpec texture , final boolean smooth )
	{
		if ( texture == null )
			texture = new TextureSpec();

		this.xform =  xform;
		this.dx    = dx;
		this.dy    = dy;
		this.dz    = dz;

		generate( p , q , texture , smooth );
	}

	/**
	 * Constructor for sphere.
	 *
	 * @param	xform		Transformation to apply to the object's vertices.
	 * @param	radius		Radius of sphere.
	 * @param	p			Number of faces around Y-axis to approximate the sphere.
	 * @param	q			Number of faces around X/Z-axis to approximate the sphere.
	 */
	public Sphere3D( final Matrix3D xform , final float radius , final int p , final int q )
	{
		this( xform , radius * 2 , radius * 2 , radius * 2 , p , q , null , false );
	}

	/**
	 * Generate Object3D properties.
	 *
	 * @param	p			Number of faces around Y-axis to approximate the sphere.
	 * @param	q			Number of faces around X/Z-axis to approximate the sphere.
	 * @param	texture		Texture of sphere.
	 * @param	smooth		Smooth surface.
	 */
	public void generate( final int p , final int q , TextureSpec texture , final boolean smooth )
	{
		if ( texture == null )
			texture = new TextureSpec();

		final int vertexCount = p * ( q - 1 ) + 2;
		final int faceCount = p * q;

		float[] vertices = new float[ vertexCount * 3 ];
		int[][] faceVert = new int[ faceCount ][];

		/*
		 * Generate vertices.
		 */
		int v = 0;
		
		vertices[ v++ ] = 0;
		vertices[ v++ ] = 0;
		vertices[ v++ ] = dz / -2f;
		
		for ( int qc = 1 ; qc < q ; qc++ )
		{
			float qa = (float)( qc * Math.PI / q );
			
			float radiusX =  (float)( Math.sin( qa ) * dx / 2 );
			float radiusY =  (float)( Math.sin( qa ) * dy / 2 );
			float circleZ = -(float)( Math.cos( qa ) * dz / 2 );
				
			for ( int pc = 0 ; pc < p ; pc++ )
			{
				float pa = (float)( pc * 2 * Math.PI / p );

				vertices[ v++ ] =  (float)( Math.sin( pa ) * radiusX );
				vertices[ v++ ] = -(float)( Math.cos( pa ) * radiusY );
				vertices[ v++ ] = circleZ;
			}
		}
		
		vertices[ v++ ] = 0;
		vertices[ v++ ] = 0;
		vertices[ v++ ] = dz / 2f;

		xform.transform( vertices , vertices , v / 3 );
		
		/*
		 * Define faces (new style)
		 */
		final int lastQ = q - 1;
		final int lastV = vertexCount - 1;
		
		for ( int f = 0 , qc = 0 ; qc < q ; qc++ )
		{
			for ( int pc = 0 ; pc < p ; pc++ , f++ )
			{
				int p1 = ( qc - 1 ) * p +     pc             + 1;
				int p2 = ( qc - 1 ) * p + ( ( pc + 1 ) % p ) + 1;
				int p3 =   qc       * p +     pc             + 1;
				int p4 =   qc       * p + ( ( pc + 1 ) % p ) + 1;

				faceVert  [ f ] = ( qc == 0     ) ? new int[] { 0 , p3 , p4 } :
				                  ( qc == lastQ ) ? new int[] { p2 , p1 , lastV } :
				                                    new int[] { p2 , p1 , p3 , p4 };
			}
		}
		
		/*
		 * Set Object3D properties.
		 */
		set( vertices ,  faceVert , texture , smooth );
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
	public void paint( Graphics g , Matrix3D gXform , Matrix3D objXform )
	{
		if ( true )
		{
			super.paint( g , gXform , objXform );
			return;
		}

		float x = objXform.transformX( xform.xo , xform.yo , xform.zo );
		float y = objXform.transformY( xform.xo , xform.yo , xform.zo );
		
		drawOval( g , gXform , x - dx / 2 , y - dy / 2 , dx , dy );
	}

}
