package common.renderer;

import java.awt.Graphics;
import java.util.Hashtable;
import common.db.TextureSpec;
import common.model.Matrix3D;

/**
 * This class defines a 3D cylinder with optionally different radi for the caps. Using
 * these radi, this class may also be used to construct cones.
 *
 * @version 1.0 (20011128, PSH) 
 * @author	Peter S. Heijnen
 */
public class Cylinder3D
	extends Object3D
{
	public final Matrix3D xform;
	public final float   height;
	public final float   radiusTop;
	public final float   radiusBottom;

	/**
	 * Constructor for cylinder object. Radius of top or bottom may be set to 0 to create
	 * a cone.
	 *
	 * @param	xform				Transform to apply to the cylinder's vertices.
	 * @param	radiusBottom		Radius at bottom.
	 * @param	radiusTop			Radius at top.
	 * @param	height				Height of box (y-axis).
	 * @param	numEdges			Number of edges to approximate circle (minimum: 3).
	 * @param	texture				Texture of cylinder.
	 * @param	smoothCircumference	Apply smoothing to circumference of cylinder.
	 * @param	smoothCaps			Apply smoothing to caps of cylinder.
	 */
	public Cylinder3D( final Matrix3D xform , final float radiusBottom , final float radiusTop , final float height , final int numEdges , final TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( radiusBottom < 0 || radiusTop < 0 || height < 0 || numEdges < 3 )
			throw new IllegalArgumentException( "inacceptable arguments to Cylinder constructor (height=" + height + ", text=" + texture + ")" );

		if ( radiusBottom == 0 && radiusTop == 0 )
			throw new IllegalArgumentException( "radius of bottom or top of cylinder must be non-zero" );

		this.xform        = xform;
		this.radiusTop    = radiusTop;
		this.radiusBottom = radiusBottom;
		this.height       = height;

		generate( numEdges , texture , smoothCircumference , smoothCaps );
	}

	/**
	 * Generate Object3D properties for cylinder.
	 *
	 * @param	numEdges			Number of edges to approximate circle (minimum: 3).
	 * @param	texture				Texture of cylinder.
	 * @param	smoothCircumference	Apply smoothing to circumference of cylinder.
	 * @param	smoothCaps			Apply smoothing to caps of cylinder.
	 */
	public void generate( final int numEdges , TextureSpec texture , final boolean smoothCircumference , final boolean smoothCaps )
	{
		if ( texture == null )
			texture = new TextureSpec();

		/*
		 * Setup properties of cylinder.
		 */
		final boolean   hasBottom   = ( radiusBottom > 0 );
		final boolean   hasTop      = ( radiusTop > 0 );
		final int       vertexCount = ( hasBottom ? numEdges : 1 ) + ( hasTop ? numEdges : 1 );
		final int       faceCount   = ( hasBottom ? 1 : 0 ) + numEdges + ( hasTop ? 1 : 0 );
		final float[]   vertices    = new float[ vertexCount * 3 ];
		final int[][]   faceVert    = new int  [ faceCount ][];
		final boolean[] faceSmooth  = new boolean[ faceCount ];

		/*
		 * Generate vertices.
		 */
		int v = 0;

		if ( hasBottom )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				float a = (float)( i * 2 * Math.PI / numEdges );
				
				vertices[ v++ ] =  (float)( Math.sin( a ) * radiusBottom );
				vertices[ v++ ] = -(float)( Math.cos( a ) * radiusBottom );
				vertices[ v++ ] = 0;
			}
		}
		else
		{
			vertices[ v++ ] = 0;
			vertices[ v++ ] = 0;
			vertices[ v++ ] = 0;
		}

		if ( hasTop )
		{
			for ( int i = 0 ; i < numEdges ; i++ )
			{
				float a = (float)( i * 2 * Math.PI / numEdges );
				
				vertices[ v++ ] =  (float)( Math.sin( a ) * radiusTop );
				vertices[ v++ ] = -(float)( Math.cos( a ) * radiusTop );
				vertices[ v++ ] = height;
			}
		}
		else
		{
			vertices[ v++ ] = 0;
			vertices[ v++ ] = 0;
			vertices[ v++ ] = height;
		}

		xform.transform( vertices , vertices , vertices.length / 3 );

		/*
		 * Construct faces
		 */
		int f = 0;

		/*
		 * Bottom face (if it exists).
		 */
		if ( hasBottom )
		{
			int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = i;
				
			faceVert  [ f   ] = fv;
			faceSmooth[ f++ ] = smoothCaps;
		}

		/*
		 * Circumference.
		 */
		if ( hasBottom && hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				int i2 = ( i1 + 1 ) % numEdges;
				faceVert  [ f   ] = new int[] { i2 , i1 , numEdges + i1 , numEdges + i2 };
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}
		else if ( hasBottom )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				int i2 = ( i1 + 1 ) % numEdges;
				faceVert  [ f   ] = new int[] { i2 , i1 , numEdges };
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}
		else if ( hasTop )
		{
			for ( int i1 = 0 ; i1 < numEdges ; i1++ )
			{
				int i2 = ( i1 + 1 ) % numEdges;
				
				faceVert  [ f   ] = new int[] { 0 , 1 + i1 , 1 + i2 };
				faceSmooth[ f++ ] = smoothCircumference;
			}
		}

		/*
		 * Top face (if it exists).
		 */
		if ( hasTop )
		{
			int[] fv = new int[ numEdges ];
			for ( int i = 0 ; i < numEdges ; i++ )
				fv[ i ] = ( hasBottom ? numEdges : 1 ) + ( numEdges - i - 1 );
				
			faceVert  [ f   ] = fv;
			faceSmooth[ f++ ] = smoothCaps;
		}
		
		/*
		 * Set Object3D properties.
		 */
		set( vertices ,  faceVert , texture , faceSmooth );
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
		
		/*
		 * Frontal = circle.
		 *
		 * ? 0 ?
		 * ? 0 ?
		 */
		if ( Matrix3D.almost0( objXform.xy ) && Matrix3D.almost0( objXform.yy ) )
		{
			float minR = ( radiusBottom > radiusTop ) ? radiusBottom : radiusTop;
			float maxR = ( radiusBottom > radiusTop ) ? radiusBottom : radiusTop;
			drawOval( g , gXform , x - maxR, y - maxR , maxR * 2 , maxR * 2 );

			/*
			 * @FIXME should also draw small oval is the smaller surface is visible
			 */
			//if ( minR != maxR && <smaller oval is visable> )
				//drawOval( g , gXform , x - minR, y - minR , minR * 2 , minR * 2 );
				
			return;
		}

		/*
		 * Side, no rotation at all, = poly
		 *
		 * ? 0 ?
		 * 0 1 0
		 */
		float[] pts = null;
		if ( Matrix3D.almost0( objXform.xy ) && Matrix3D.almost0( objXform.yx ) &&
			 Matrix3D.almost1( objXform.yy ) && Matrix3D.almost0( objXform.yz ) )
		{
			pts = new float[] { -radiusBottom , 0      , radiusBottom , 0      ,
			                     -radiusTop    , height , radiusTop    , height };
		}
	
		/*
		 * Side, mirrored Y, = poly
		 *
		 * ? 0 ?
		 * 0 -1 0
		 */
		if ( Matrix3D.almost0(  objXform.xy ) && Matrix3D.almost0( objXform.yx ) &&
			 Matrix3D.almost1( -objXform.yy ) && Matrix3D.almost0( objXform.yz ) )
		{
			pts = new float[] { -radiusBottom , 0       , radiusBottom , 0       ,
			                     -radiusTop    , -height , radiusTop    , -height };
		}

		/*
		 * Side, 90deg z rotation, = poly
		 *
		 * 0 1 0
		 * ? 0 ?
		 */
		if ( Matrix3D.almost0( objXform.xx ) && Matrix3D.almost1( objXform.xy ) &&
			 Matrix3D.almost0( objXform.xz ) && Matrix3D.almost0( objXform.yy ) )
		{
			pts = new float[] { 0      , -radiusBottom , 0      , radiusBottom ,
			                    height , -radiusTop    , height , radiusTop	};
		}
		
		/*
		 * Side, 90deg z rotation, mirrored x = poly
		 *
		 * 0 -1 0
		 * ? 0 ?
		 */
		if ( Matrix3D.almost0( objXform.xx ) && Matrix3D.almost1( -objXform.xy ) &&
			 Matrix3D.almost0( objXform.xz ) && Matrix3D.almost0(  objXform.yy ) )
		{
			pts = new float[] {       0 , -radiusBottom ,       0 , radiusBottom ,
			                    -height , -radiusTop    , -height , radiusTop	  };
		}

		/*
		 * If the points are filled, draw them.
		 */
		if ( pts != null && pts.length == 8 )
		{
			drawLine( g , gXform , x + pts[ 0 ] , y + pts[ 1 ] , x + pts[ 2 ] , y + pts[ 3 ] );
			drawLine( g , gXform , x + pts[ 4 ] , y + pts[ 5 ] , x + pts[ 6 ] , y + pts[ 7 ] );
			drawLine( g , gXform , x + pts[ 0 ] , y + pts[ 1 ] , x + pts[ 4 ] , y + pts[ 5 ] );
			drawLine( g , gXform , x + pts[ 2 ] , y + pts[ 3 ] , x + pts[ 6 ] , y + pts[ 7 ] );
			return;
		}

		/*
		 * Not painted, paint fully.
		 */
		super.paint( g , gXform , objXform );
	}

}
