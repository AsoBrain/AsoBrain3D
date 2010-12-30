/* $Id$
 * ====================================================================
 * AsoBrain 3D Toolkit
 * Copyright (C) 1999-2010 Peter S. Heijnen
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
package ab.j3d;

/**
 * This class provides some basic materials. Use them as you like or not.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public final class Materials
{
	public static final Material BLACK        = new Material( "black"      , 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 1.0f, 0.50f, 0.50f, 0.50f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material DARKGRAY     = new Material( "darkgray"   , 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 1.0f, 0.40f, 0.40f, 0.40f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material GRAY         = new Material( "gray"       , 0.50f, 0.50f, 0.50f, 0.50f, 0.50f, 0.50f, 1.0f, 0.30f, 0.30f, 0.30f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material LIGHTGRAY    = new Material( "lightgray"  , 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material WHITE        = new Material( "white"      , 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 1.0f, 1.00f, 1.00f, 1.00f,   8, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material RED          = new Material( "red"        , 1.00f, 0.00f, 0.00f, 1.00f, 0.00f, 0.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material ORANGE       = new Material( "orange"     , 1.00f, 0.38f, 0.00f, 1.00f, 0.38f, 0.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material YELLOW       = new Material( "yellow"     , 1.00f, 1.00f, 0.00f, 1.00f, 1.00f, 0.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material GREEN        = new Material( "green"      , 0.00f, 1.00f, 0.00f, 0.00f, 1.00f, 0.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material CYAN         = new Material( "cyan"       , 0.00f, 1.00f, 1.00f, 0.00f, 1.00f, 1.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material BLUE         = new Material( "blue"       , 0.00f, 0.00f, 1.00f, 0.00f, 0.00f, 1.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material MAGENTA      = new Material( "magenta"    , 1.00f, 0.00f, 1.00f, 1.00f, 0.00f, 1.00f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material DARK_BROWN   = new Material( "dark_brown" , 0.24f, 0.16f, 0.05f, 0.24f, 0.16f, 0.05f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material BROWN        = new Material( "brown"      , 0.76f, 0.51f, 0.37f, 0.76f, 0.51f, 0.37f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material BEIGE        = new Material( "beige"      , 0.88f, 0.87f, 0.78f, 0.88f, 0.87f, 0.78f, 1.0f, 0.20f, 0.20f, 0.20f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material INVISIBLE    = new Material( "invisible"  , 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 0.0f, 0.00f, 0.00f, 0.00f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material TRANSPARENT  = new Material( "transparent", 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.1f, 0.50f, 0.50f, 0.50f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, false );
	public static final Material GLASS        = new Material( "glass"      , 0.05f, 0.10f, 0.07f, 0.20f, 0.30f, 0.25f, 0.3f, 0.50f, 0.55f, 0.50f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.0f, 0.8f, 1.00f, 1.00f, 1.00f );
	public static final Material GLASS_WHITE  = new Material( "glass_white", 0.20f, 0.20f, 0.20f, 0.30f, 0.30f, 0.30f, 0.5f, 0.30f, 0.30f, 0.30f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.0f, 0.5f, 1.00f, 1.00f, 1.00f );
	public static final Material METAL        = new Material( "metal"      , 0.13f, 0.13f, 0.13f, 0.45f, 0.45f, 0.45f, 1.0f, 0.45f, 0.45f, 0.45f,  16, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.45f, 0.45f, 0.45f );
	public static final Material GOLD         = new Material( "gold"       , 0.25f, 0.20f, 0.07f, 0.75f, 0.61f, 0.23f, 1.0f, 0.63f, 0.65f, 0.37f, 128, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.75f, 0.61f, 0.23f );
	public static final Material CHROME       = new Material( "chrome"     , 0.25f, 0.25f, 0.25f, 0.40f, 0.40f, 0.40f, 1.0f, 0.77f, 0.77f, 0.77f, 128, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.8f, 1.0f, 0.40f, 0.40f, 0.40f );
	public static final Material MATTE_CHROME = new Material( "matteChrome", 0.25f, 0.25f, 0.25f, 0.40f, 0.40f, 0.40f, 1.0f, 0.60f, 0.60f, 0.60f,  16, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.0f, 0.2f, 0.40f, 0.40f, 0.40f );
	public static final Material SILVER       = new Material( "silver"     , 0.19f, 0.19f, 0.19f, 0.51f, 0.51f, 0.51f, 1.0f, 0.51f, 0.51f, 0.51f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.51f, 0.51f, 0.51f );
	public static final Material NICKEL       = new Material( "nickel"     , 0.25f, 0.25f, 0.25f, 0.40f, 0.40f, 0.40f, 1.0f, 0.77f, 0.77f, 0.77f, 128, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.40f, 0.40f, 0.40f );
	public static final Material ZINC         = new Material( "zinc"       , 0.65f, 0.67f, 0.67f, 0.65f, 0.67f, 0.67f, 1.0f, 0.45f, 0.47f, 0.47f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.1f, 0.3f, 0.65f, 0.69f, 0.69f );
	public static final Material ALUMINIUM    = new Material( "aluminium"  , 0.19f, 0.19f, 0.19f, 0.71f, 0.71f, 0.71f, 1.0f, 0.51f, 0.51f, 0.51f,  64, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.71f, 0.71f, 0.71f );
	public static final Material ALU_PLATE    = new Material( "alu-plate"  , 0.21f, 0.21f, 0.21f, 0.61f, 0.61f, 0.61f, 1.0f, 0.31f, 0.31f, 0.31f,  32, 0.0f, 0.0f, 0.0f, "ab3d/maps/alu-plate", 0.1f, 0.1f, null, 0.0f, 0.0f, /*"ab3d/maps/alu-plate-bump", 0.1f, 0.1f*/ true, "ab3d/maps/reflect-sky-bw", 0.0f, 0.1f, 1.0f, 1.0f, 1.0f );
	public static final Material STEEL        = new Material( "steel"      , 0.13f, 0.13f, 0.13f, 0.45f, 0.45f, 0.45f, 1.0f, 0.45f, 0.45f, 0.45f,  16, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.45f, 0.45f, 0.45f );
	public static final Material MESSING      = new Material( "messing"    , 0.33f, 0.22f, 0.03f, 0.78f, 0.57f, 0.11f, 1.0f, 0.99f, 0.94f, 0.81f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.78f, 0.57f, 0.11f );
	public static final Material TITANIUM     = new Material( "titanium"   , 0.18f, 0.17f, 0.16f, 0.44f, 0.43f, 0.39f, 1.0f, 0.88f, 0.87f, 0.78f,  32, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.44f, 0.43f, 0.39f );
	public static final Material ZAMAC        = new Material( "zamac"      , 0.09f, 0.05f, 0.09f, 0.40f, 0.44f, 0.51f, 1.0f, 0.30f, 0.30f, 0.49f,  16, 0.0f, 0.0f, 0.0f, null, 0.0f, 0.0f, null, 0.0f, 0.0f, false, "ab3d/maps/reflect-sky-bw", 0.2f, 1.0f, 0.40f, 0.44f, 0.51f );

}
