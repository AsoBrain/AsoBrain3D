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
 * This library is distributed in the hope that it will be useful );
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ====================================================================
 */
package ab.j3d.appearance;

import ab.j3d.*;

/**
 * This class provides some basic materials. Use them as you like or not.
 *
 * @author  Peter S. Heijnen
 * @version $Revision$ $Date$
 */
public class BasicAppearances
{
	public static final Appearance BLACK;
	public static final Appearance DARKGRAY;
	public static final Appearance GRAY;
	public static final Appearance LIGHTGRAY;
	public static final Appearance WHITE;
	public static final Appearance RED;
	public static final Appearance ORANGE;
	public static final Appearance YELLOW;
	public static final Appearance GREEN;
	public static final Appearance CYAN;
	public static final Appearance BLUE;
	public static final Appearance MAGENTA;
	public static final Appearance DARK_BROWN;
	public static final Appearance BROWN;
	public static final Appearance BEIGE;
	public static final Appearance INVISIBLE;
	public static final Appearance TRANSPARENT;
	public static final Appearance GLASS;
	public static final Appearance GLASS_WHITE;
	public static final Appearance METAL;
	public static final Appearance GOLD;
	public static final Appearance CHROME;
	public static final Appearance MATTE_CHROME;
	public static final Appearance SILVER;
	public static final Appearance NICKEL;
	public static final Appearance ZINC;
	public static final Appearance ALUMINIUM;
	public static final Appearance ALU_PLATE;
	public static final Appearance STEEL;
	public static final Appearance MESSING;
	public static final Appearance TITANIUM;
	public static final Appearance ZAMAC;

	static
	{
		final BasicAppearance black = new BasicAppearance();
		black.setAmbientColor( new Color4f( 0.01f, 0.01f, 0.01f ) );
		black.setDiffuseColor( new Color4f( 0.01f, 0.01f, 0.01f ) );
		black.setSpecularColor( new Color4f( 0.5f, 0.5f, 0.5f ) );
		black.setShininess( 64 );
		BLACK = black; // "black"

		final BasicAppearance darkgray = new BasicAppearance();
		darkgray.setAmbientColor( new Color4f( 0.25f, 0.25f, 0.25f ) );
		darkgray.setDiffuseColor( new Color4f( 0.25f, 0.25f, 0.25f ) );
		darkgray.setSpecularColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		darkgray.setShininess( 64 );
		DARKGRAY = darkgray; // "darkgray"

		final BasicAppearance gray = new BasicAppearance();
		gray.setAmbientColor( new Color4f( 0.5f, 0.5f, 0.5f ) );
		gray.setDiffuseColor( new Color4f( 0.5f, 0.5f, 0.5f ) );
		gray.setSpecularColor( new Color4f( 0.3f, 0.3f, 0.3f ) );
		gray.setShininess( 64 );
		GRAY = gray; // "gray"

		final BasicAppearance lightgray = new BasicAppearance();
		lightgray.setAmbientColor( new Color4f( 0.75f, 0.75f, 0.75f ) );
		lightgray.setDiffuseColor( new Color4f( 0.75f, 0.75f, 0.75f ) );
		lightgray.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		lightgray.setShininess( 32 );
		LIGHTGRAY = lightgray; // "lightgray"

		final BasicAppearance white = new BasicAppearance();
		white.setAmbientColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		white.setDiffuseColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		white.setSpecularColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		white.setShininess( 8 );
		WHITE = white; // "white"

		final BasicAppearance red = new BasicAppearance();
		red.setAmbientColor( new Color4f( 1.0f, 0.0f, 0.0f ) );
		red.setDiffuseColor( new Color4f( 1.0f, 0.0f, 0.0f ) );
		red.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		red.setShininess( 32 );
		RED = red; // "red"

		final BasicAppearance orange = new BasicAppearance();
		orange.setAmbientColor( new Color4f( 1.0f, 0.38f, 0.0f ) );
		orange.setDiffuseColor( new Color4f( 1.0f, 0.38f, 0.0f ) );
		orange.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		orange.setShininess( 32 );
		ORANGE = orange; // "orange"

		final BasicAppearance yellow = new BasicAppearance();
		yellow.setAmbientColor( new Color4f( 1.0f, 1.0f, 0.0f ) );
		yellow.setDiffuseColor( new Color4f( 1.0f, 1.0f, 0.0f ) );
		yellow.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		yellow.setShininess( 32 );
		YELLOW = yellow; // "yellow"

		final BasicAppearance green = new BasicAppearance();
		green.setAmbientColor( new Color4f( 0.0f, 1.0f, 0.0f ) );
		green.setDiffuseColor( new Color4f( 0.0f, 1.0f, 0.0f ) );
		green.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		green.setShininess( 32 );
		GREEN = green; // "green"

		final BasicAppearance cyan = new BasicAppearance();
		cyan.setAmbientColor( new Color4f( 0.0f, 1.0f, 1.0f ) );
		cyan.setDiffuseColor( new Color4f( 0.0f, 1.0f, 1.0f ) );
		cyan.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		cyan.setShininess( 32 );
		CYAN = cyan; // "cyan"

		final BasicAppearance blue = new BasicAppearance();
		blue.setAmbientColor( new Color4f( 0.0f, 0.0f, 1.0f ) );
		blue.setDiffuseColor( new Color4f( 0.0f, 0.0f, 1.0f ) );
		blue.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		blue.setShininess( 32 );
		BLUE = blue; // "blue"

		final BasicAppearance magenta = new BasicAppearance();
		magenta.setAmbientColor( new Color4f( 1.0f, 0.0f, 1.0f ) );
		magenta.setDiffuseColor( new Color4f( 1.0f, 0.0f, 1.0f ) );
		magenta.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		magenta.setShininess( 32 );
		MAGENTA = magenta; // "magenta"

		final BasicAppearance darkBrown = new BasicAppearance();
		darkBrown.setAmbientColor( new Color4f( 0.24f, 0.16f, 0.05f ) );
		darkBrown.setDiffuseColor( new Color4f( 0.24f, 0.16f, 0.05f ) );
		darkBrown.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		darkBrown.setShininess( 32 );
		DARK_BROWN = darkBrown; // "dark_brown"

		final BasicAppearance brown = new BasicAppearance();
		brown.setAmbientColor( new Color4f( 0.76f, 0.51f, 0.37f ) );
		brown.setDiffuseColor( new Color4f( 0.76f, 0.51f, 0.37f ) );
		brown.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		brown.setShininess( 32 );
		BROWN = brown; // "brown"

		final BasicAppearance beige = new BasicAppearance();
		beige.setAmbientColor( new Color4f( 0.88f, 0.87f, 0.78f ) );
		beige.setDiffuseColor( new Color4f( 0.88f, 0.87f, 0.78f ) );
		beige.setSpecularColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		beige.setShininess( 32 );
		BEIGE = beige; // "beige"

		final BasicAppearance invisible = new BasicAppearance();
		invisible.setAmbientColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		invisible.setDiffuseColor( new Color4f( 1.0f, 1.0f, 1.0f, 0.0f ) );
		invisible.setSpecularColor( new Color4f( 0.0f, 0.0f, 0.0f ) );
		invisible.setShininess( 64 );
		INVISIBLE = invisible; // "invisible"

		final BasicAppearance transparent = new BasicAppearance();
		transparent.setAmbientColor( new Color4f( 0.01f, 0.01f, 0.01f ) );
		transparent.setDiffuseColor( new Color4f( 0.01f, 0.01f, 0.01f, 0.1f ) );
		transparent.setSpecularColor( new Color4f( 0.5f, 0.5f, 0.5f ) );
		transparent.setShininess( 64 );
		TRANSPARENT = transparent; // "transparent"

		final BasicAppearance glass = new BasicAppearance();
		glass.setAmbientColor( new Color4f( 0.05f, 0.1f, 0.07f ) );
		glass.setDiffuseColor( new Color4f( 0.2f, 0.3f, 0.25f, 0.3f ) );
		glass.setSpecularColor( new Color4f( 0.5f, 0.55f, 0.5f ) );
		glass.setShininess( 64 );
		glass.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "ab3d/maps/reflect-sky-bw.jpg" ) ) );
		glass.setReflectionMin( 0.0f );
		glass.setReflectionMax( 0.8f );
		glass.setReflectionColor( new Color4f( 1.0f, 1.0f, 1.0f ) );
		GLASS = glass; // "glass"

		final BasicAppearance glassWhite = new BasicAppearance();
		glassWhite.setAmbientColor( new Color4f( 0.2f, 0.2f, 0.2f ) );
		glassWhite.setDiffuseColor( new Color4f( 0.3f, 0.3f, 0.3f, 0.5f ) );
		glassWhite.setSpecularColor( new Color4f( 0.3f, 0.3f, 0.3f ) );
		glassWhite.setShininess( 32 );
		glassWhite.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "ab3d/maps/reflect-sky-bw.jpg" ) ) );
		glassWhite.setReflectionMin( 0.0f );
		glassWhite.setReflectionMax( 0.5f );
		GLASS_WHITE = glassWhite; // "glass_white"

		final BasicAppearance metal = new BasicAppearance();
		metal.setAmbientColor( new Color4f( 0.13f, 0.13f, 0.13f ) );
		metal.setDiffuseColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		metal.setSpecularColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		metal.setShininess( 16 );
		metal.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		metal.setReflectionMin( 0.2f );
		metal.setReflectionMax( 1.0f );
		metal.setReflectionColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		METAL = metal; // "metal"

		final BasicAppearance gold = new BasicAppearance();
		gold.setAmbientColor( new Color4f( 0.25f, 0.2f, 0.07f ) );
		gold.setDiffuseColor( new Color4f( 0.75f, 0.61f, 0.23f ) );
		gold.setSpecularColor( new Color4f( 0.63f, 0.65f, 0.37f ) );
		gold.setShininess( 128 );
		gold.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		gold.setReflectionMin( 0.2f );
		gold.setReflectionMax( 1.0f );
		gold.setReflectionColor( new Color4f( 0.75f, 0.61f, 0.23f ) );
		GOLD = gold; // "gold"

		final BasicAppearance chrome = new BasicAppearance();
		chrome.setAmbientColor( new Color4f( 0.25f, 0.25f, 0.25f ) );
		chrome.setDiffuseColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		chrome.setSpecularColor( new Color4f( 0.77f, 0.77f, 0.77f ) );
		chrome.setShininess( 128 );
		chrome.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		chrome.setReflectionMin( 0.8f );
		chrome.setReflectionMax( 1.0f );
		chrome.setReflectionColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		CHROME = chrome; // "chrome"

		final BasicAppearance matteChrome = new BasicAppearance();
		matteChrome.setAmbientColor( new Color4f( 0.25f, 0.25f, 0.25f ) );
		matteChrome.setDiffuseColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		matteChrome.setSpecularColor( new Color4f( 0.6f, 0.6f, 0.6f ) );
		matteChrome.setShininess( 16 );
		matteChrome.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		matteChrome.setReflectionMin( 0.0f );
		matteChrome.setReflectionMax( 0.2f );
		matteChrome.setReflectionColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		MATTE_CHROME = matteChrome; // "matteChrome"

		final BasicAppearance silver = new BasicAppearance();
		silver.setAmbientColor( new Color4f( 0.19f, 0.19f, 0.19f ) );
		silver.setDiffuseColor( new Color4f( 0.51f, 0.51f, 0.51f ) );
		silver.setSpecularColor( new Color4f( 0.51f, 0.51f, 0.51f ) );
		silver.setShininess( 64 );
		silver.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		silver.setReflectionMin( 0.2f );
		silver.setReflectionMax( 1.0f );
		silver.setReflectionColor( new Color4f( 0.51f, 0.51f, 0.51f ) );
		SILVER = silver; // "silver"

		final BasicAppearance nickel = new BasicAppearance();
		nickel.setAmbientColor( new Color4f( 0.25f, 0.25f, 0.25f ) );
		nickel.setDiffuseColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		nickel.setSpecularColor( new Color4f( 0.77f, 0.77f, 0.77f ) );
		nickel.setShininess( 128 );
		nickel.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		nickel.setReflectionMin( 0.2f );
		nickel.setReflectionMax( 1.0f );
		nickel.setReflectionColor( new Color4f( 0.4f, 0.4f, 0.4f ) );
		NICKEL = nickel; // "nickel"

		final BasicAppearance zinc = new BasicAppearance();
		zinc.setAmbientColor( new Color4f( 0.65f, 0.67f, 0.67f ) );
		zinc.setDiffuseColor( new Color4f( 0.65f, 0.67f, 0.67f ) );
		zinc.setSpecularColor( new Color4f( 0.45f, 0.47f, 0.47f ) );
		zinc.setShininess( 32 );
		zinc.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		zinc.setReflectionMin( 0.1f );
		zinc.setReflectionMax( 0.3f );
		zinc.setReflectionColor( new Color4f( 0.65f, 0.69f, 0.69f ) );

		ZINC = zinc; // "zinc"
		final BasicAppearance aluminium = new BasicAppearance();
		aluminium.setAmbientColor( new Color4f( 0.19f, 0.19f, 0.19f ) );
		aluminium.setDiffuseColor( new Color4f( 0.71f, 0.71f, 0.71f ) );
		aluminium.setSpecularColor( new Color4f( 0.51f, 0.51f, 0.51f ) );
		aluminium.setShininess( 64 );
		aluminium.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		aluminium.setReflectionMin( 0.2f );
		aluminium.setReflectionMax( 1.0f );
		aluminium.setReflectionColor( new Color4f( 0.71f, 0.71f, 0.71f ) );
		ALUMINIUM = aluminium; // "aluminium"

		final BasicAppearance aluPlate = new BasicAppearance();
		aluPlate.setAmbientColor( new Color4f( 0.21f, 0.21f, 0.21f ) );
		aluPlate.setDiffuseColor( new Color4f( 0.61f, 0.61f, 0.61f ) );
		aluPlate.setSpecularColor( new Color4f( 0.31f, 0.31f, 0.31f ) );
		aluPlate.setShininess( 32 );
		aluPlate.setColorMap( new FileTextureMap( BasicAppearances.class.getResource( "/ab3d/maps/alu-plate.jpg" ), 0.1f, 0.1f ) );
		aluPlate.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		aluPlate.setReflectionMin( 0.0f );
		aluPlate.setReflectionMax( 0.1f );
		ALU_PLATE = aluPlate; // "alu-plate"

		final BasicAppearance steel = new BasicAppearance();
		steel.setAmbientColor( new Color4f( 0.13f, 0.13f, 0.13f ) );
		steel.setDiffuseColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		steel.setSpecularColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		steel.setShininess( 16 );
		steel.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		steel.setReflectionMin( 0.2f );
		steel.setReflectionMax( 1.0f );
		steel.setReflectionColor( new Color4f( 0.45f, 0.45f, 0.45f ) );
		STEEL = steel; // "steel"

		final BasicAppearance messing = new BasicAppearance();
		messing.setAmbientColor( new Color4f( 0.33f, 0.22f, 0.03f ) );
		messing.setDiffuseColor( new Color4f( 0.78f, 0.57f, 0.11f ) );
		messing.setSpecularColor( new Color4f( 0.99f, 0.94f, 0.81f ) );
		messing.setShininess( 32 );
		messing.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		messing.setReflectionMin( 0.2f );
		messing.setReflectionMax( 1.0f );
		messing.setReflectionColor( new Color4f( 0.78f, 0.57f, 0.11f ) );
		MESSING = messing; // "messing"

		final BasicAppearance titanium = new BasicAppearance();
		titanium.setAmbientColor( new Color4f( 0.18f, 0.17f, 0.16f ) );
		titanium.setDiffuseColor( new Color4f( 0.44f, 0.43f, 0.39f ) );
		titanium.setSpecularColor( new Color4f( 0.88f, 0.87f, 0.78f ) );
		titanium.setShininess( 32 );
		titanium.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		titanium.setReflectionMin( 0.2f );
		titanium.setReflectionMax( 1.0f );
		titanium.setReflectionColor( new Color4f( 0.44f, 0.43f, 0.39f ) );
		TITANIUM = titanium; // "titanium"

		final BasicAppearance zamac = new BasicAppearance();
		zamac.setAmbientColor( new Color4f( 0.09f, 0.05f, 0.09f ) );
		zamac.setDiffuseColor( new Color4f( 0.4f, 0.44f, 0.51f ) );
		zamac.setSpecularColor( new Color4f( 0.3f, 0.3f, 0.49f ) );
		zamac.setShininess( 16 );
		zamac.setReflectionMap( new SingleImageCubeMap( BasicAppearances.class.getResource( "/ab3d/maps/reflect-sky-bw.jpg" ) ) );
		zamac.setReflectionMin( 0.2f );
		zamac.setReflectionMax( 1.0f );
		zamac.setReflectionColor( new Color4f( 0.4f, 0.44f, 0.51f ) );
		ZAMAC = zamac; // "zamac"
	}
}
