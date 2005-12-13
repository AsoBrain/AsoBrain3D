/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2000-2005
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
package ab.j3d.pov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.numdata.oss.io.IndentingWriter;

/**
 * Constructive Solid Geometry.
 * <pre>
 * union/intersection/difference/merge/object // name
 * {
 *     geometry1
 *     geometry2
 *     [texture]
 *     [rotation]
 *     [translation]
 *     [matrix]
 * }
 * </pre>
 * The type named 'object' may only contain one item.
 *
 * @author  Sjoerd Bouwman
 * @version $Revision$ ($Date$, $Author$)
 */
public class PovBool
	extends PovGeometry
{
	/** Operation: union.        Adds all geometry together.                 */ public static final int UNION        = 0;
	/** Operation: intersection. Only the common area between both is left.  */ public static final int INTERSECTION = 1;
	/** Operation: difference.   Second shape is subtracted from the first.  */ public static final int DIFFERENCE   = 2;
	/** Operation: merge.        Same as UNION (any difference?).            */ public static final int MERGE        = 3;
	/** Operation: object.       An 'object{ }' (only one item).             */ public static final int OBJECT       = 4;

	public static final String[] TYPE_STRINGS = { "union" , "intersection" , "difference" , "merge" , "object" };

	/**
	 * The type of this boolean operation.
	 */
	public final int type;

	/**
	 * Sub geometry.
	 */
	private List items = new ArrayList();

	/**
	 * Creates a PovBool with name and type.
	 *
	 * @param   name    Name of the shape.
	 * @param   type    Type of bool (see class javadoc).
	 */
	public PovBool( final String name , final int type )
	{
		this( name , type , null );
	}

	/**
	 * Creates a PovBool with name, type and texture.
	 *
	 * @param   name        Name of the shape.
	 * @param   type        Type of bool (see class javadoc).
	 * @param   texture     Texture for the shape.
	 */
	public PovBool( final String name , final int type , final PovTexture texture )
	{
		super( name , texture );
		this.type = type;
	}

	/**
	 * Add a new shape to the boolean. This shape should
	 * be a solid object, so meshes do not qualify.
	 *
	 * NOTE: When the type is DIFFERENCE the sequence in which
	 * shapes are added does matter!, the shape from which others
	 * are subtracted should be add first.
	 *
	 * @param   object  New geometry to add.
	 */
	public void add( final PovGeometry object )
	{
		if ( object instanceof PovCamera || object instanceof PovLight )
			throw new RuntimeException( "Cannot add camera's or light to a bool" );
		if ( type == OBJECT && items.size() > 0 )
			throw new RuntimeException( "OBJECT type may only contain one child" );

		items.add( object );
	}

	/**
	 * Get the geometry at specified position.
	 *
	 * @param   i   Index of geometry to get.
	 *
	 * @return  Geometry at specified index.
	 */
	public PovGeometry get( final int i )
	{
		return (PovGeometry)items.get( i );
	}

	/**
	 * Get the number of items in the boolean.
	 *
	 * @return  Number of items in the boolean.
	 */
	public int getSize()
	{
		return items.size();
	}

	public void write( final IndentingWriter out )
		throws IOException
	{
		if ( ( items.size() == 1 ) && ( rotation == null ) && ( translation == null ) && ( xform == null ) && ( texture == null ) )
		{
			final PovGeometry geometry = get( 0 );
			geometry.write( out );
		}
		else if ( !items.isEmpty() )
		{
			String ts = TYPE_STRINGS[ type ];
			if ( items.size() == 1 )
				ts = TYPE_STRINGS[ OBJECT ];

			out.write  ( ts );
			out.write  ( " //" );
			out.writeln( name );
			out.writeln( "{" );
			out.indentIn();

			for ( int i = 0 ; i < items.size() ; i++ )
			{
				final PovGeometry geometry = get( i );
				geometry.write( out );
			}

			writeTransformation( out );
			writeTexture( out );

			out.indentOut();
			out.writeln( "}" );
		}
	}

}
