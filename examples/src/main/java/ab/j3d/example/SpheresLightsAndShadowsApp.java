/*
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2020 Peter S. Heijnen
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
package ab.j3d.example;

/**
 * Renders a scene with several colored spheres, lit by two spot lights with
 * shadow casting enabled.
 *
 * @author Gerrit Meinders
 */
public class SpheresLightsAndShadowsApp
{
	/**
	 * Runs the example.
	 *
	 * @param args Ignored.
	 */
	public static void main( final String[] args )
	{
		new ExampleApp( new SpheresLightsAndShadowsExample() ).run();
	}

	/**
	 * Not used.
	 */
	private SpheresLightsAndShadowsApp()
	{
	}
}
