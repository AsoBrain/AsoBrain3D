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
package ab.j3d.model;

import java.util.*;

import ab.j3d.appearance.*;
import org.jetbrains.annotations.*;

/**
 * A group of faces with a common appearance.
 *
 * @author G. Meinders
 * @version $Revision$ $Date$
 */
public class FaceGroup
{
	/**
	 * Faces in this group.
	 */
	private List<Face3D> _faces;

	/**
	* Smoothing flag this face. Smooth faces are used to approximate
	 * smooth/curved/rounded parts of objects.
	 * <p />
	 * This information would typically be used to select the most appropriate
	 * shading algorithm.
	*/
	private boolean _smooth;

	/**
	 * Material of this face.
	 */
	private Appearance _appearance;

	/**
	 * Flag to indicate that the plane is two-sided. This means, that the
	 * plane is 'visible' on both sides.
	 */
	private final boolean _twoSided;

	/**
	 * Construct new face group.
	 *
	 * @param   appearance      Material to apply to the face.
	 * @param   smooth          Face is smooth/curved vs. flat.
	 * @param   twoSided        Face is two-sided.
	 */
	public FaceGroup( @Nullable final Appearance appearance, final boolean smooth, final boolean twoSided )
	{
		_faces = new ArrayList<Face3D>();
		_appearance = appearance;
		_smooth = smooth;
		_twoSided = twoSided;
	}

	/**
	 * Returns the faces in this group.
	 *
	 * @return  Faces in this group.
	 */
	public List<Face3D> getFaces()
	{
		return Collections.unmodifiableList( _faces );
	}

	/**
	 * Add a face to this group.
	 *
	 * @param   face    Face to add.
	 */
	final void addFace( final Face3D face )
	{
		_faces.add( face );
	}

	/**
	 * Returns the smoothing flag of faces in this group. Smooth faces are used
	 * to approximate smooth/curved/rounded parts of objects.
	 *
	 * @return  Smoothing flag.
	 */
	public boolean isSmooth()
	{
		return _smooth;
	}

	/**
	 * Sets the smoothing flag of faces in this group. Smooth faces are used
	 * to approximate smooth/curved/rounded parts of objects.
	 *
	 * @param   smooth  Smoothing flag.
	 */
	public void setSmooth( final boolean smooth )
	{
		_smooth = smooth;
	}

	/**
	 * Returns the material of faces in this group.
	 *
	 * @return  Material of faces in this group.
	 */
	public Appearance getAppearance()
	{
		return _appearance;
	}

	/**
	 * Sets the material of faces in this group.
	 *
	 * @param   appearance    Material to be set.
	 */
	public void setAppearance( final Appearance appearance )
	{
		_appearance = appearance;
	}

	/**
	 * Returns whether faces in this group are two-sided.
	 *
	 * @return  <code>true</code> if faces are two-sided.
	 */
	public boolean isTwoSided()
	{
		return _twoSided;
	}
}
