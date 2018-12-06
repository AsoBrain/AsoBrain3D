/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2018 Peter S. Heijnen
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

import java.util.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * A group of faces with a common appearance.
 *
 * @author G. Meinders
 */
public class FaceGroup
{
	/**
	 * Faces in this group.
	 */
	private final List<Face3D> _faces;

	/**
	 * Smooth rendering flag for faces in this group. Smooth faces are used to
	 * approximate smooth/curved/rounded parts of objects.
	 * <p />
	 * This flag is typically used to adjust the shading algorithm, e.g. by
	 * interpolating vertex normals across the surface (Phong shading).
	 */
	private boolean _smooth;

	/**
	 * Apparance of faces in this group.
	 */
	private Appearance _appearance;

	/**
	 * Flag to indicate that faces in this group are two-sided as opposed to
	 * single-sided. This means, that the plane is 'visible' on both sides (no
	 * back-face culling is performed).
	 */
	private boolean _twoSided;

	/**
	 * Construct new face group.
	 *
	 * @param appearance Material to apply to the face.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Face is two-sided.
	 */
	public FaceGroup( @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		_faces = new ArrayList<Face3D>();
		_appearance = appearance;
		_smooth = smooth;
		_twoSided = twoSided;
	}

	/**
	 * Construct new face group.
	 *
	 * @param appearance Material to apply to the face.
	 * @param smooth     Face is smooth/curved vs. flat.
	 * @param twoSided   Face is two-sided.
	 * @param faces      Initial faces in group.
	 */
	public FaceGroup( @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided, @NotNull final Collection<Face3D> faces )
	{
		_faces = new ArrayList<Face3D>( faces );
		_appearance = appearance;
		_smooth = smooth;
		_twoSided = twoSided;
	}

	public List<Face3D> getFaces()
	{
		return Collections.unmodifiableList( _faces );
	}

	/**
	 * Add a face to this group.
	 *
	 * @param face Face to add.
	 */
	public void addFace( final Face3D face )
	{
		_faces.add( face );
	}

	/**
	 * Remove a face from this group.
	 *
	 * @param face Face to remove.
	 */
	public void removeFace( final Face3D face )
	{
		_faces.remove( face );
	}

	public boolean isSmooth()
	{
		return _smooth;
	}

	public void setSmooth( final boolean smooth )
	{
		_smooth = smooth;
	}

	@Nullable
	public Appearance getAppearance()
	{
		return _appearance;
	}

	public void setAppearance( @Nullable final Appearance appearance )
	{
		_appearance = appearance;
	}

	public boolean isTwoSided()
	{
		return _twoSided;
	}

	public void setTwoSided( final boolean twoSided )
	{
		_twoSided = twoSided;
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;
		if ( obj == this )
		{
			result = true;
		}
		else if ( obj instanceof FaceGroup )
		{
			final FaceGroup other = (FaceGroup)obj;
			result = _smooth == other._smooth &&
			         _twoSided == other._twoSided &&
			         _appearance.equals( other._appearance ) &&
			         _faces.equals( other._faces );
		}
		else
		{
			result = false;
		}
		return result;
	}
}
