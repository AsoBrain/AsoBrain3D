/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2021 Peter S. Heijnen
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

import UVGenerator from './UVGenerator';
import Matrix3D from '../ab.j3d/Matrix3D';

/**
 * This is the {@link UVGenerator} implementation uses a {@link Matrix3D} to
 * transform 3D to 2D coordinates.
 *
 * @author Peter S. Heijnen
 */
export default class TransformUVGenerator
	extends UVGenerator
{
	/**
	 * Transform from 3D model to 2D U/V-coordinates.
	 */
	private readonly _uvTransform: Matrix3D;

	/**
	 * Construct generator.
	 *
	 * @param uvTransform Transform from 3D model to 2D U/V-coordinates.
	 */
	constructor( uvTransform: Matrix3D )
	{
		super();
		this._uvTransform = uvTransform;
	}

	generate( x: number, y: number, z: number ): void
	{
		const transform = this._uvTransform;
		this.u = transform.transformX( x, y, z );
		this.v = transform.transformY( x, y, z );
	}
}
