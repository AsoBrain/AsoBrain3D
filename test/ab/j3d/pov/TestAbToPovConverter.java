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

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import ab.j3d.*;
import ab.j3d.model.*;
import ab.j3d.view.*;
import com.numdata.oss.ui.*;
import junit.framework.*;

/**
 * This class tests the conversion of the test model to POV-Ray.
 * All objects are converted separately.
 *
 * @see     AbPovTestModel
 * @see     AbToPovConverter
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class TestAbToPovConverter
	extends TestCase
{
	/**
	 * This method tests if the needed texture declarations are generated.
	 * All textures should be declared.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testDeclarations()
		throws IOException
	{
		ImageTools.addToSearchPath( getTestDirectory() );
		final String texturesDirectory = getTexturesDirectory();

		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final Scene            scene           = testModel.getScene();
			final AbToPovConverter converter       = new AbToPovConverter( texturesDirectory );
			final PovScene         povScene        = converter.convert( scene );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			povScene.write( povWriter );

			final String povScript = stringWriter.toString();

			/*
			 * The whole scene needs to be converted, but only the texture
			 * definition part is needed.
			 */
			final int texturesStart         = povScript.indexOf( " * Texture definitions" ) - 3;
			final int declaredGeometryStart = povScript.indexOf( " * Geometry" ) - 3;

			actual = povScript.substring( texturesStart , declaredGeometryStart );
		}

		final String expected =
			"/*\n" +
			" * Texture definitions\n" +
			" */\n" +

			"#declare TEX_CUBE_BACK =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_BACK\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_BACK_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_BACK_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,1.0,1000.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,1.0,0.001> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_BOTTOM =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_BOTTOM\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_BOTTOM_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_BOTTOM_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1000.0,1.0,1000.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <0.001,1.0,0.001> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_FRONT =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_FRONT\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_FRONT_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_FRONT_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1000.0,1000.0,1.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <0.001,0.001,1.0> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_LEFT =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_LEFT\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_LEFT_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_LEFT_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1000.0,1.0,1.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <0.001,1.0,1.0> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_RIGHT =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_RIGHT\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_RIGHT_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_RIGHT_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,1000.0,1.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.001,1.0> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_TOP =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_TOP\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_CUBE_TOP_TEXTURE_AND_COLOR =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/CUBE_TOP_TEXTURE_AND_COLOR\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,1000.0,1000.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.001,0.001> filter 1.0\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_MFCs =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/MFCs\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_MPXs =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\timage_map  { jpeg \"" + texturesDirectory + "/MPXs\" }\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_0_0_255 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <0.001,0.001,1.0>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <0.0,0.0,1.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_0_255_0 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <0.001,1.0,0.001>\n" +
			"\t\t\ttransmit   0.8\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <0.0,1.0,0.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_255_0_0 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.001,0.001>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,0.0,0.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_255_0_255 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.001,1.0>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,0.0,1.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_255_175_175 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.68627,0.68627>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_RGB_255_255_255 =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,1.0,1.0>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    1.0\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      1.0\n" +
			"\t\t\tphong_size 4.0\n" +
			"\t\t}\n" +
			"\t}\n\n" +

			"#declare TEX_orange =\n" +
			"\ttexture\n" +
			"\t{\n" +
			"\t\tpigment\n" +
			"\t\t{\n" +
			"\t\t\tcolor      rgb <1.0,0.38,0.001>\n" +
			"\t\t}\n" +
			"\t\tfinish\n" +
			"\t\t{\n" +
			"\t\t\tambient    rgb <1.0,1.0,0.0>\n" +
			"\t\t\tdiffuse    1.0\n" +
			"\t\t\tphong      0.2\n" +
			"\t\t\tphong_size 8.0\n" +
			"\t\t}\n" +
			"\t}\n\n";

		Assert.assertEquals( "Declaration generation error" , expected , actual );
	}

	/**
	 * This method tests the conversion from {@link View3D} object to
	 * {@link PovCamera}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testView3DToPovCamera()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel testModel = new AbPovTestModel();

			final View3D    view          = testModel.getView();
			final String    name          = view.getLabel();
			final Matrix3D  view2scene    = view.getView2Scene();
			final double    angle         = Math.toDegrees( view.getFieldOfView() );
			final Component viewComponent = view.getComponent();
			final double    aspectRatio   = (double) viewComponent.getWidth() / (double) viewComponent.getHeight();

			final PovCamera camera = new PovCamera( name, view2scene, angle, aspectRatio );

			final Writer stringWriter = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			camera.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"camera\n" +
			"{\n" +
			"\tright  <1.33,0.0,0.0>\n" +
			"\tangle  45.0\n" +
			"\tmatrix < 1.0 , 0.0 , 0.0 ,\n" +
			"\t         0.0 , 0.0 , 1.0 ,\n" +
			"\t         0.0 , 1.0 , 0.0 ,\n" +
			"\t         0.0 , -1000.0 , 0.0 >\n" +
			"}\n";

		Assert.assertEquals( "Camera to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a red, rotated (10 degrees around
	 * x-axis) {@link Box3D} object to a {@link PovBox}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testRedXRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.getTransform( -10.0 , 0.0 , 0.0 , -200.0 , 0.0 , -250.0 ) , testModel.getRedXRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			povBox.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>,\n" +
			"\t<100.0,200.0,100.0>\n" +
			"\ttexture { TEX_RGB_255_0_0 }\n" +
			"\tmatrix < 1.0 , 0.0 , 0.0 ,\n" +
			"\t         0.0 , 0.984807753012208 , 0.1736481776669303 ,\n" +
			"\t         0.0 , -0.1736481776669303 , 0.984807753012208 ,\n" +
			"\t         -200.0 , 0.0 , -250.0 >\n" +
			"}\n";

		Assert.assertEquals( "RedBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a green, rotated (10 degrees around
	 * y-axis) {@link Box3D} object to a {@link PovBox}. The box characteristics
	 * opacity, ambient, diffuse, specular reflectivity and specular exponent
	 * are also tested.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testGreenYRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.getTransform(  0.0 , 10.0 ,  0.0 ,  -50.0 ,   0.0 , -250.0 ) , testModel.getGreenYRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			povBox.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>,\n" +
			"\t<100.0,200.0,100.0>\n" +
			"\ttexture { TEX_RGB_0_255_0 }\n" +
			"\tmatrix < 0.984807753012208 , 0.0 , -0.1736481776669303 ,\n" +
			"\t         0.0 , 1.0 , 0.0 ,\n" +
			"\t         0.1736481776669303 , 0.0 , 0.984807753012208 ,\n" +
			"\t         -50.0 , 0.0 , -250.0 >\n" +
			"}\n";

		Assert.assertEquals( "GreenBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a blue, rotated (10 degrees around
	 * z-axis) {@link Box3D} object to a {@link PovBox}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testBlueZRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.getTransform(  0.0 ,  0.0 , 10.0 ,  200.0 ,   0.0 , -250.0 ) , testModel.getBlueZRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			povBox.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>,\n" +
			"\t<100.0,200.0,100.0>\n" +
			"\ttexture { TEX_RGB_0_0_255 }\n" +
			"\tmatrix < 0.984807753012208 , 0.1736481776669303 , 0.0 ,\n" +
			"\t         -0.1736481776669303 , 0.984807753012208 , 0.0 ,\n" +
			"\t         0.0 , 0.0 , 1.0 ,\n" +
			"\t         200.0 , 0.0 , -250.0 >\n" +
			"}\n";

		Assert.assertEquals( "BlueBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a textured {@link Box3D} object
	 * (a wooden panel) with a different side-texture to a {@link PovMesh2}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testTexturedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovMesh2         mesh            = converter.convertObject3D( Matrix3D.getTransform(  0.0 ,  0.0 , 45.0 , -350.0 ,   0.0 ,    0.0 ) , testModel.getTexturedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			mesh.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n"
			+ "{\n"
			+ "\tvertex_vectors\n"
			+ "\t{\n"
			+ "\t\t8,\n"
			+ "\t\t<-350.0,0.0,0.0> , <-208.57864,141.42136,0.0> , <-215.64971,148.49242,0.0> ,\n"
			+ "\t\t<-357.07107,7.07107,0.0> , <-350.0,0.0,200.0> , <-208.57864,141.42136,200.0> ,\n"
			+ "\t\t<-215.64971,148.49242,200.0> , <-357.07107,7.07107,200.0>\n"
			+ "\t}\n"
			+ "\tuv_vectors\n"
			+ "\t{\n"
			+ "\t\t13,\n"
			+ "\t\t<1.0,0.0> , <1.0,1.0> , <0.0,1.0> ,\n"
			+ "\t\t<0.0,0.0> , <-1.0,1.0> , <-1.0,0.0> ,\n"
			+ "\t\t<0.05,0.0> , <0.05,1.0> , <-0.05,1.0> ,\n"
			+ "\t\t<-0.05,0.0> , <1.0,0.05> , <0.0,0.05> ,\n"
			+ "\t\t<-1.0,0.05>\n"
			+ "\t}\n"
			+ "\ttexture_list\n"
			+ "\t{\n"
			+ "\t\t2,\n"
			+ "\t\ttexture { TEX_MPXs }\n"
			+ "\t\ttexture { TEX_MFCs }\n"
			+ "\t}\n"
			+ "\tface_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<1,5,4>,0 , <1,4,0>,0 , <3,7,6>,0 , <3,6,2>,0 , <2,6,5>,1 , <2,5,1>,1 ,\n"
			+ "\t\t<0,4,7>,1 , <0,7,3>,1 , <5,6,7>,1 , <5,7,4>,1 , <0,3,2>,1 , <0,2,1>,1\n"
			+ "\t}\n"
			+ "\tuv_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<0,1,2> , <0,2,3> , <3,2,4> , <3,4,5> , <6,7,2> , <6,2,3> ,\n"
			+ "\t\t<3,2,8> , <3,8,9> , <0,10,11> , <0,11,3> , <3,11,12> , <3,12,5>\n"
			+ "\t}\n"
			+ "\tuv_mapping\n"
			+ "}\n";

		Assert.assertEquals( "Textured Box3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a {@link Sphere3D} object to a
	 * {@link PovSphere}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testSphere3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      sphere          = converter.convertSphere3D( Matrix3D.INIT.setTranslation( 0.0 , 300.0 , -200.0 ) , testModel.getSphere3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			sphere.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"sphere\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>, 50.0\n" +
			"\ttexture { TEX_RGB_0_0_255 }\n" +
			"\tmatrix < 1.0 , 0.0 , 0.0 ,\n" +
			"\t         0.0 , 1.0 , 0.0 ,\n" +
			"\t         0.0 , 0.0 , 1.0 ,\n" +
			"\t         0.0 , 300.0 , -200.0 >\n" +
			"}\n";

		Assert.assertEquals( "Sphere3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a {@link Cylinder3D} object to a
	 * {@link PovCylinder}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testCylinder3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      cylinder        = converter.convertCylinder3D( Matrix3D.INIT.setTranslation( 0.0 , 0.0 , 150.0 ) , testModel.getCylinder3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			cylinder.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"cone\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>,50.0\n" +
			"\t<0.0,0.0,100.0>,50.0\n" +
			"\ttexture { TEX_RGB_255_0_255 }\n" +
			"\tmatrix < 1.0 , 0.0 , 0.0 ,\n" +
			"\t         0.0 , 1.0 , 0.0 ,\n" +
			"\t         0.0 , 0.0 , 1.0 ,\n" +
			"\t         0.0 , 0.0 , 150.0 >\n" +
			"}\n";

		Assert.assertEquals( "Cylinder3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a cone (also a {@link Cylinder3D}) to
	 * a {@link PovCylinder}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testCone3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      cylinder        = converter.convertCone3D( Matrix3D.getTransform( -45.0 , 0.0 , 0.0 , 250.0 , 0.0 , 0.0 ) , testModel.getCone3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			cylinder.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"cone\n" +
			"{\n" +
			"\t<0.0,0.0,0.0>,100.0\n" +
			"\t<0.0,0.0,200.0>,50.0\n" +
			"\ttexture { TEX_RGB_255_255_255 }\n" +
			"\tmatrix < 1.0 , 0.0 , 0.0 ,\n" +
			"\t         0.0 , 0.7071067811865476 , 0.7071067811865475 ,\n" +
			"\t         0.0 , -0.7071067811865475 , 0.7071067811865476 ,\n" +
			"\t         250.0 , 0.0 , 0.0 >\n" +
			"}\n";

		Assert.assertEquals( "Cone3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an {@link Object3D} (a colored cube
	 * with a different texture per face) to a {@link PovMesh2}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testColorCubeToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getColorCube() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			mesh.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n"
			+ "{\n"
			+ "\tvertex_vectors\n"
			+ "\t{\n"
			+ "\t\t8,\n"
			+ "\t\t<-100.0,-100.0,100.0> , <-100.0,100.0,100.0> , <100.0,100.0,100.0> ,\n"
			+ "\t\t<100.0,-100.0,100.0> , <-100.0,100.0,-100.0> , <-100.0,-100.0,-100.0> ,\n"
			+ "\t\t<100.0,-100.0,-100.0> , <100.0,100.0,-100.0>\n"
			+ "\t}\n"
			+ "\tuv_vectors\n"
			+ "\t{\n"
			+ "\t\t4,\n"
			+ "\t\t<0.0,1.0> , <1.0,1.0> , <1.0,0.0> ,\n"
			+ "\t\t<0.0,0.0>\n"
			+ "\t}\n"
			+ "\ttexture_list\n"
			+ "\t{\n"
			+ "\t\t6,\n"
			+ "\t\ttexture { TEX_CUBE_TOP }\n"
			+ "\t\ttexture { TEX_CUBE_BOTTOM }\n"
			+ "\t\ttexture { TEX_CUBE_FRONT }\n"
			+ "\t\ttexture { TEX_CUBE_BACK }\n"
			+ "\t\ttexture { TEX_CUBE_LEFT }\n"
			+ "\t\ttexture { TEX_CUBE_RIGHT }\n"
			+ "\t}\n"
			+ "\tface_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<3,2,1>,0 , <3,1,0>,0 , <7,6,5>,1 , <7,5,4>,1 , <6,3,0>,2 , <6,0,5>,2 ,\n"
			+ "\t\t<4,1,2>,3 , <4,2,7>,3 , <5,0,1>,4 , <5,1,4>,4 , <7,2,3>,5 , <7,3,6>,5\n"
			+ "\t}\n"
			+ "\tuv_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> ,\n"
			+ "\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3>\n"
			+ "\t}\n"
			+ "\tuv_mapping\n"
			+ "}\n";

		Assert.assertEquals( "ColorCube3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an {@link Object3D} (a colored cube
	 * with a different texture per face) to a {@link PovMesh2}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link PovWriter}.
	 */
	public void testTexturedColorCubeToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getTexturedColorCube() );
			final StringWriter     stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			mesh.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n"
			+ "{\n"
			+ "\tvertex_vectors\n"
			+ "\t{\n"
			+ "\t\t8,\n"
			+ "\t\t<-100.0,-100.0,100.0> , <-100.0,100.0,100.0> , <100.0,100.0,100.0> ,\n"
			+ "\t\t<100.0,-100.0,100.0> , <-100.0,100.0,-100.0> , <-100.0,-100.0,-100.0> ,\n"
			+ "\t\t<100.0,-100.0,-100.0> , <100.0,100.0,-100.0>\n"
			+ "\t}\n"
			+ "\tuv_vectors\n"
			+ "\t{\n"
			+ "\t\t4,\n"
			+ "\t\t<0.0,0.0> , <0.0,0.5> , <0.5,0.5> ,\n"
			+ "\t\t<0.5,0.0>\n"
			+ "\t}\n"
			+ "\ttexture_list\n"
			+ "\t{\n"
			+ "\t\t6,\n"
			+ "\t\ttexture { TEX_CUBE_TOP_TEXTURE_AND_COLOR }\n"
			+ "\t\ttexture { TEX_CUBE_BOTTOM_TEXTURE_AND_COLOR }\n"
			+ "\t\ttexture { TEX_CUBE_FRONT_TEXTURE_AND_COLOR }\n"
			+ "\t\ttexture { TEX_CUBE_BACK_TEXTURE_AND_COLOR }\n"
			+ "\t\ttexture { TEX_CUBE_LEFT_TEXTURE_AND_COLOR }\n"
			+ "\t\ttexture { TEX_CUBE_RIGHT_TEXTURE_AND_COLOR }\n"
			+ "\t}\n"
			+ "\tface_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<3,2,1>,0 , <3,1,0>,0 , <7,6,5>,1 , <7,5,4>,1 , <6,3,0>,2 , <6,0,5>,2 ,\n"
			+ "\t\t<4,1,2>,3 , <4,2,7>,3 , <5,0,1>,4 , <5,1,4>,4 , <7,2,3>,5 , <7,3,6>,5\n"
			+ "\t}\n"
			+ "\tuv_indices\n"
			+ "\t{\n"
			+ "\t\t12,\n"
			+ "\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> ,\n"
			+ "\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3>\n"
			+ "\t}\n"
			+ "\tuv_mapping\n"
			+ "}\n";

		Assert.assertEquals( "ColorCube3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an extruded {@link java.awt.Shape} to
	 * a {@link PovMesh2}.
	 */
	public void testExtrudedObject2DAToPov()
	{
		final AbPovTestModel   testModel       = new AbPovTestModel();
		final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
		final PovMesh2         mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getExtrudedObject2DA() );

		final List<Vector3D[]> expectedTriangles = new ArrayList<Vector3D[]>( Arrays.asList(
			new Vector3D[] { new Vector3D( -300.0, 0.0, -250.0 ), new Vector3D( -300.0, 100.0, -150.0 ), new Vector3D( -400.0, 100.0, -150.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 0.0, -250.0 ), new Vector3D( -400.0, 100.0, -150.0 ), new Vector3D( -400.0, 0.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 100.0, -250.0 ), new Vector3D( -300.0, 200.0, -150.0 ), new Vector3D( -300.0, 100.0, -150.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 100.0, -250.0 ), new Vector3D( -300.0, 100.0, -150.0 ), new Vector3D( -300.0, 0.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 100.0, -250.0 ), new Vector3D( -400.0, 200.0, -150.0 ), new Vector3D( -300.0, 200.0, -150.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 100.0, -250.0 ), new Vector3D( -300.0, 200.0, -150.0 ), new Vector3D( -300.0, 100.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 0.0, -250.0 ), new Vector3D( -400.0, 100.0, -150.0 ), new Vector3D( -400.0, 200.0, -150.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 0.0, -250.0 ), new Vector3D( -400.0, 200.0, -150.0 ), new Vector3D( -400.0, 100.0, -250.0 ) }
		) );
		assertSameMesh( expectedTriangles, mesh );

		final List<PovTexture> textureList = mesh.getTextureList();
		assertEquals( "Unexpected number of textures.", 1, textureList.size() );
		final PovTexture texture = textureList.get( 0 );
		assertEquals( "Unexpected texture.", "RGB_255_175_175", texture.getName() );
	}

	/**
	 * This method tests the conversion of an extruded {@link java.awt.Shape} to
	 * a {@link PovMesh2}.
	 */
	public void testExtrudedObject2DBToPov()
	{
		final AbPovTestModel   testModel       = new AbPovTestModel();
		final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
		final PovMesh2         mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getExtrudedObject2DB() );

		final List<Vector3D[]> expectedTriangles = new ArrayList<Vector3D[]>( Arrays.asList(
			new Vector3D[] { new Vector3D( -400.0, 100.0, -300.0 ), new Vector3D( -300.0, 100.0, -300.0 ), new Vector3D( -300.0, 0.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 100.0, -300.0 ), new Vector3D( -300.0, 0.0, -300.0 ), new Vector3D( -400.0, 0.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 0.0, -250.0 ), new Vector3D( -400.0, 0.0, -300.0 ), new Vector3D( -300.0, 0.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 0.0, -250.0 ), new Vector3D( -300.0, 0.0, -300.0 ), new Vector3D( -300.0, 0.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 100.0, -250.0 ), new Vector3D( -400.0, 100.0, -300.0 ), new Vector3D( -400.0, 0.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -400.0, 100.0, -250.0 ), new Vector3D( -400.0, 0.0, -300.0 ), new Vector3D( -400.0, 0.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 100.0, -250.0 ), new Vector3D( -300.0, 100.0, -300.0 ), new Vector3D( -400.0, 100.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 100.0, -250.0 ), new Vector3D( -400.0, 100.0, -300.0 ), new Vector3D( -400.0, 100.0, -250.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 0.0, -250.0 ), new Vector3D( -300.0, 0.0, -300.0 ), new Vector3D( -300.0, 100.0, -300.0 ) },
			new Vector3D[] { new Vector3D( -300.0, 0.0, -250.0 ), new Vector3D( -300.0, 100.0, -300.0 ), new Vector3D( -300.0, 100.0, -250.0 ) }
		) );
		assertSameMesh( expectedTriangles, mesh );

		final List<PovTexture> textureList = mesh.getTextureList();
		assertEquals( "Unexpected number of textures.", 1, textureList.size() );
		final PovTexture texture = textureList.get( 0 );
		assertEquals( "Unexpected texture.", "orange", texture.getName() );
	}

	/**
	 * This method tests the {@link AbToPovConverter#convertLight3D} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public void testLight3DToPov()
		throws Exception
	{
		final String actual;
		{
			final AbPovTestModel  testModel       = new AbPovTestModel();
			final Matrix3D        transform       = Matrix3D.INIT.setTranslation( 500.0 , -500.0 , 500.0 );
			final PovLight        light           = AbToPovConverter.convertLight3D( transform , testModel.getLight3D() );
			final StringWriter    stringWriter    = new StringWriter();
			final PovWriter povWriter = PovScene.getPovWriter( stringWriter );

			light.write( povWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"light_source\n" +
			"{\n" +
			"\t<500.0,-500.0,500.0>\n" +
			"\tcolor <1.0,1.0,1.0>\n" +
			"\tfade_distance 100.0\n" +
			"\tfade_power 2\n" +
			"}\n";
		Assert.assertEquals( "Light3D to pov conversion error" , expected , actual );
	}

	/**
	 * Get the path to the package directory.
	 *
	 * @return The path to the package directory.
	 */
	private static String getTestDirectory()
	{
		final Class<?>    thisClass        = TestAbToPovConverter.class;
		final ClassLoader classLoader      = thisClass.getClassLoader();
		final Package     thisPackage      = thisClass.getPackage();
		final String      packageName      = thisPackage.getName();
		final URL         packageDirectory = classLoader.getResource( packageName.replace( '.' , '/' ) );

		return packageDirectory.getPath();
	}

	/**
	 * Get the path to the test textures.
	 *
	 * @return The path to the test textures.
	 */
	private static String getTexturesDirectory()
	{
		final String testDirectory = getTestDirectory();
		return testDirectory + "/textures";
	}

	/**
	 * Write the whole test scene to a POV-Ray file.
	 *
	 * @throws  IOException if the scene could not be written.
	 */
	public static void writeToFile()
		throws IOException
	{
		final AbPovTestModel testModel = new AbPovTestModel();
		final Scene          scene     = testModel.getScene();

		final View3D    view          = testModel.getView();
		final Matrix3D  view2scene    = view.getView2Scene();
		final Component viewComponent = view.getComponent();
		final double    aspectRatio   = (double)viewComponent.getWidth() / (double)viewComponent.getHeight();

		final AbToPovConverter converter = new AbToPovConverter( getTexturesDirectory() );
		final PovScene povScene = converter.convert( scene );
		povScene.add( new PovCamera( view.getLabel(), view2scene, Math.toDegrees( view.getFieldOfView() ), aspectRatio ) );
		povScene.write( new File( "test.pov" ) );
	}

	/**
	 * Asserts that the given mesh matches the given list of triangles.
	 *
	 * @param   expected    Vertex coordinates of the expected triangles.
	 * @param   actual      Actual mesh.
	 */
	private static void assertSameMesh( final List<Vector3D[]> expected, final PovMesh2 actual )
	{
		final List<PovVector> vertices = actual.getVertexVectors();

		final List<PovMesh2.Triangle> unexpected = new ArrayList<PovMesh2.Triangle>();
		final List<Vector3D[]> missing = new ArrayList<Vector3D[]>( expected );

		for ( final PovMesh2.Triangle actualTriangle : actual.getTriangles() )
		{
			final PovVector actualVertex1 = vertices.get( actualTriangle._vertexIndex1 );
			final PovVector actualVertex2 = vertices.get( actualTriangle._vertexIndex2 );
			final PovVector actualVertex3 = vertices.get( actualTriangle._vertexIndex3 );

			boolean found = false;
			for ( Iterator<Vector3D[]> iterator = missing.iterator() ; iterator.hasNext() ; )
			{
				final Vector3D[] expectedTriangle = iterator.next();
				if ( isSameTriangle( expectedTriangle, actualVertex1, actualVertex2, actualVertex3 ) )
				{
					found = true;
					iterator.remove();
					break;
				}
			}

			if ( !found )
			{
				unexpected.add( actualTriangle );
			}
		}

		if ( !missing.isEmpty() || !unexpected.isEmpty() )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Meshes differ:\n" );

			message.append( "\tMissing triangles:\n" );
			for ( final Vector3D[] triangle : missing )
			{
				message.append( "\t\t" );
				message.append( triangle[ 0 ].toFriendlyString() );
				message.append( ", " );
				message.append( triangle[ 1 ].toFriendlyString() );
				message.append( ", " );
				message.append( triangle[ 2 ].toFriendlyString() );
				message.append( "\n" );
			}

			message.append( "\tUnexpected  triangles:\n" );
			for ( final PovMesh2.Triangle triangle : unexpected )
			{
				message.append( "\t\t" );
				message.append( vertices.get( triangle._vertexIndex1 ) );
				message.append( ", " );
				message.append( vertices.get( triangle._vertexIndex2 ) );
				message.append( ", " );
				message.append( vertices.get( triangle._vertexIndex3 ) );
				message.append( "\n" );
			}

			fail( message.toString() );
		}
	}

	/**
	 * Returns whether the given sets of vertex coordinates represent the same
	 * triangle. Both triangles must have the same winding to be equal, but
	 * the starting point may be different.
	 *
	 * @param   expectedTriangle    Vertex coordinates of the expected triangle.
	 * @param   actualVertex1       Vertex coordinate 1 of the actual triangle.
	 * @param   actualVertex2       Vertex coordinate 2 of the actual triangle.
	 * @param   actualVertex3       Vertex coordinate 3 of the actual triangle.
	 *
	 * @return  <code>true</code> if the triangles are the same.
	 */
	private static boolean isSameTriangle( final Vector3D[] expectedTriangle, final PovVector actualVertex1, final PovVector actualVertex2, final PovVector actualVertex3 )
	{
		return ( expectedTriangle[ 0 ].equals( actualVertex1.getX(), actualVertex1.getY(), actualVertex1.getZ() ) &&
		         expectedTriangle[ 1 ].equals( actualVertex2.getX(), actualVertex2.getY(), actualVertex2.getZ() ) &&
		         expectedTriangle[ 2 ].equals( actualVertex3.getX(), actualVertex3.getY(), actualVertex3.getZ() ) ) ||

		       ( expectedTriangle[ 1 ].equals( actualVertex1.getX(), actualVertex1.getY(), actualVertex1.getZ() ) &&
		         expectedTriangle[ 2 ].equals( actualVertex2.getX(), actualVertex2.getY(), actualVertex2.getZ() ) &&
		         expectedTriangle[ 0 ].equals( actualVertex3.getX(), actualVertex3.getY(), actualVertex3.getZ() ) ) ||

		       ( expectedTriangle[ 2 ].equals( actualVertex1.getX(), actualVertex1.getY(), actualVertex1.getZ() ) &&
		         expectedTriangle[ 0 ].equals( actualVertex2.getX(), actualVertex2.getY(), actualVertex2.getZ() ) &&
		         expectedTriangle[ 1 ].equals( actualVertex3.getX(), actualVertex3.getY(), actualVertex3.getZ() ) );
	}
}
