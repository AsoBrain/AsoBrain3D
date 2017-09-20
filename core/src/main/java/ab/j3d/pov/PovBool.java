/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

import java.io.*;
import java.util.*;


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
	/** Operation: union.        Adds all geometry together.                */ public static final int UNION        = 0;
	/** Operation: intersection. Only the common area between both is left. */ public static final int INTERSECTION = 1;
	/** Operation: difference.   Second shape is subtracted from the first. */ public static final int DIFFERENCE   = 2;
	/** Operation: merge.        Same as UNION (any difference?).           */ public static final int MERGE        = 3;
	/** Operation: object.       An 'object{ }' (only one item).            */ public static final int OBJECT       = 4;

	public static final String[] TYPE_STRINGS = { "union" , "intersection" , "difference" , "merge" , "object" };

	/**
	 * The type of this operation ({@link #UNION}, {@link #INTERSECTION},
	 * {@link #DIFFERENCE}, {@link #MERGE}, or {@link #OBJECT}).
	 */
	private final int _type;

	/**
	 * Sub geometry.
	 */
	private List _items = new ArrayList();

	/**
	 * Construct {@link PovBool} with the specified name and type.
	 *
	 * @param   name        Name of the object.
	 * @param   type        Type of operation ({@link #UNION}, {@link #INTERSECTION},
	 *                      {@link #DIFFERENCE}, {@link #MERGE}, or {@link #OBJECT}).
	 */
	public PovBool( final String name , final int type )
	{
		this( name , type , null );
	}

	/**
	 * Construct {@link PovBool} with the specified name and type, and texture.
	 *
	 * @param   name        Name of the object.
	 * @param   type        Type of operation ({@link #UNION}, {@link #INTERSECTION},
	 *                      {@link #DIFFERENCE}, {@link #MERGE}, or {@link #OBJECT}).
	 * @param   texture     Texture for the shape.
	 */
	public PovBool( final String name , final int type , final PovTexture texture )
	{
		super( name , texture );

		_type = type;
	}

	/**
	 * Get type of this operation.
	 *
	 * @return  Type of this operation ({@link #UNION}, {@link #INTERSECTION},
	 *          {@link #DIFFERENCE}, {@link #MERGE}, or {@link #OBJECT}).
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * Add a new shape to the operation. This shape should be a solid object,
	 * so meshes do not qualify.
	 * <dl>
	 *  <dt>NOTE:</dt>
	 *  <dd>When the type is {@link #DIFFERENCE} the sequence
	 *   in which shapes are added does matter!, the shape from which others
	 *   are subtracted should be add first.</dd>
	 * </dl>
	 *
	 * @param   object  New geometry to add.
	 */
	public void add( final PovGeometry object )
	{
		if ( ( object instanceof PovCamera )
		  || ( object instanceof PovLight  ) )
			throw new RuntimeException( "Cannot add camera's or light to a bool" );

		if ( ( getType() == OBJECT ) && !_items.isEmpty() )
			throw new RuntimeException( "OBJECT type may only contain one child" );

		_items.add( object );
	}

	/**
	 * Get the number of items in the operation.
	 *
	 * @return  Number of items in the operation.
	 */
	public int size()
	{
		return _items.size();
	}

	/**
	 * Get the geometry at specified position.
	 *
	 * @param   index   Index of geometry to get.
	 *
	 * @return  Geometry at specified index.
	 */
	public PovGeometry get( final int index )
	{
		return (PovGeometry)_items.get( index );
	}

	public void write( final PovWriter out )
		throws IOException
	{
		final List items = _items;

		if ( ( items.size() == 1 ) && !isTransformed() && ( getTexture() == null ) )
		{
			final PovGeometry geometry = get( 0 );
			geometry.write( out );
		}
		else if ( !items.isEmpty() )
		{
			String ts = TYPE_STRINGS[ getType() ];
			if ( items.size() == 1 )
				ts = TYPE_STRINGS[ OBJECT ];

			out.write( ts );
			out.write( " //" );
			out.write( getName() );
			out.newLine();
			out.writeln( "{" );
			out.indentIn();

			for ( int i = 0 ; i < items.size() ; i++ )
			{
				final PovGeometry geometry = get( i );
				geometry.write( out );
			}

			writeModifiers( out );

			out.indentOut();
			out.writeln( "}" );
		}
	}
}
