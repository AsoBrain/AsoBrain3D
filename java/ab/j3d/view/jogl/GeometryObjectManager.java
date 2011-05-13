/*
 * $Id$
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
package ab.j3d.view.jogl;

import java.util.*;

import ab.j3d.model.*;
import com.numdata.oss.ensemble.*;
import org.jetbrains.annotations.*;

/**
 * Provides geometry objects, which are automatically created when first
 * requested and deleted when no longer in use.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class GeometryObjectManager
{
	/**
	 * Factory used to create geometry objects.
	 */
	private final GeometryObjectFactory _geometryObjectFactory;

	/**
	 * Geometry objects being managed.
	 */
	private Map<Duet<FaceGroup, GeometryType>, GeometryObject> _geometryObjects = new HashMap<Duet<FaceGroup, GeometryType>, GeometryObject>();

	/**
	 * Geometry objects that were not used during the last frame.
	 */
	private Set<Duet<FaceGroup, GeometryType>> _unusedGeometryObjects = new HashSet<Duet<FaceGroup, GeometryType>>();

	/**
	 * Constructs a new manager for geometry objects.
	 */
	public GeometryObjectManager()
	{
		this( new GeometryObjectFactory() );
	}

	/**
	 * Constructs a new manager for geometry objects.
	 *
	 * @param   geometryObjectFactory   Creates the managed geometry objects.
	 */
	public GeometryObjectManager( final GeometryObjectFactory geometryObjectFactory )
	{
		_geometryObjectFactory = geometryObjectFactory;
	}

	/**
	 * Returns a geometry object for the given faces.
	 *
	 * @param   faceGroup   Faces to be included in the geometry.
	 * @param   type        Type of geometry.
	 *
	 * @return  Geometry object.
	 */
	@NotNull
	public GeometryObject getGeometryObject( @NotNull final FaceGroup faceGroup, @NotNull final GeometryType type )
	{
		final Map<Duet<FaceGroup, GeometryType>, GeometryObject> geometryObjects = _geometryObjects;

		final BasicDuet<FaceGroup, GeometryType> key = new BasicDuet<FaceGroup, GeometryType>( faceGroup, type );
		GeometryObject result = geometryObjects.get( key );

		if ( result == null )
		{
			result = _geometryObjectFactory.newGeometryObject( Collections.singletonList( faceGroup ), type );
			geometryObjects.put( key, result );
		}

		_unusedGeometryObjects.remove( key );

		return result;
	}

	/**
	 * Notifies the manager that a frame was just rendered.
	 */
	public void frameRendered()
	{
		deleteUnusedObjects( _geometryObjects, _unusedGeometryObjects );
	}

	/**
	 * Deletes all geometry objects provided by this manager.
	 */
	public void dispose()
	{
		for ( final GeometryObject geometryObject : _geometryObjects.values() )
		{
			geometryObject.delete();
		}
		_geometryObjects.clear();

		_unusedGeometryObjects.clear();
	}

	/**
	 * Deletes unused geometry objects based on the given set of keys.
	 *
	 * @param   geometryObjects     Map of geometry objects.
	 * @param   unusedKeys          Keys that were not rendered.
	 */
	private static <K> void deleteUnusedObjects( final Map<K, GeometryObject> geometryObjects, final Set<K> unusedKeys )
	{
		for ( final K key : unusedKeys )
		{
			final GeometryObject geometryObject = geometryObjects.remove( key );
			geometryObject.delete();
		}
		unusedKeys.clear();
		unusedKeys.addAll( geometryObjects.keySet() );
	}
}
