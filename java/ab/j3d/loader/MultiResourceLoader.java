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
package ab.j3d.loader;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Combines multiple resource loaders, providing access to their combined
 * resources.
 *
 * <p>
 * Resources are added to this loader by mounting resources loaders. A resource
 * loader can be mounted using any resource name as a mount point. It is also
 * possible to mount only part of a resource loader by providing the resource
 * name of a sub-tree.
 *
 * <p>
 * Mounted resource loaders may overlap, in which case they are merged on a
 * per-resource basis. If any resource loader can provide a resource with a
 * given name, that resource will be returned. If multiple resource loaders
 * provide a given resource, the loader that was mounted first takes precedence.
 *
 * @author  G. Meinders
 * @version $Revision$ $Date$
 */
public class MultiResourceLoader
	implements ResourceLoader
{
	/**
	 * Resource loaders that were mounted.
	 */
	@NotNull
	private final List<Mount> _mounts;

	/**
	 * Constructs a new resource loader.
	 */
	public MultiResourceLoader()
	{
		_mounts = new ArrayList<Mount>();
	}

	/**
	 * Mounts a resource loader, adding (part of) its contents to the root of
	 * this resource loader.
	 *
	 * @param   resourceLoader  Resource loader.
	 */
	public void mount( @NotNull final ResourceLoader resourceLoader )
	{
		mount( resourceLoader, "", "" );
	}

	/**
	 * Mounts a resource loader, adding (part of) its contents to this resource
	 * loader.
	 *
	 * @param   resourceLoader  Resource loader.
	 * @param   mountPoint      Resource name where the contents of the given
	 *                          resource loader is mounted.
	 * @param   subTree         Name of a sub-tree of the given resource loader
	 *                          to be mounted.
	 */
	public void mount( @NotNull final ResourceLoader resourceLoader, @NotNull final String mountPoint, @NotNull final String subTree )
	{
		_mounts.add( new Mount( resourceLoader, mountPoint, subTree ) );
	}

	@Override
	public InputStream getResource( final String name )
		throws IOException
	{
		InputStream result = null;

		for ( final Mount entry : _mounts )
		{
			if ( name.startsWith( entry._mountPoint ) )
			{
				try
				{
					result = entry._resourceLoader.getResource( entry._subTree + name.substring( entry._mountPoint.length() ) );
				}
				catch ( FileNotFoundException e )
				{
					// Try the next resource loader.
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		if ( result == null )
		{
			throw new FileNotFoundException( name );
		}

		return result;
	}

	/**
	 * Defines how a resource loader is mounted.
	 */
	private static class Mount
	{
		/**
		 * Resource loader that is mounted.
		 */
		@NotNull
		private ResourceLoader _resourceLoader;

		/**
		 * Mount point.
		 */
		@NotNull
		private String _mountPoint;

		/**
		 * Sub-tree.
		 */
		@NotNull
		private String _subTree;

		/**
		 * Constructs a new instance.
		 *
		 * @param   resourceLoader  Resource loader that is mounted.
		 * @param   mountPoint      Mount point.
		 * @param   subTree         Sub-tree.
		 */
		private Mount( @NotNull final ResourceLoader resourceLoader, @NotNull final String mountPoint, @NotNull final String subTree )
		{
			_resourceLoader = resourceLoader;
			_mountPoint = mountPoint;
			_subTree = subTree;
		}
	}
}
