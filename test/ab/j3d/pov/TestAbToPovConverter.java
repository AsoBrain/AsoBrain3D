/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2008
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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import junit.framework.Assert;
import junit.framework.TestCase;

import ab.j3d.Matrix3D;
import ab.j3d.model.Box3D;
import ab.j3d.model.Camera3D;
import ab.j3d.model.Cylinder3D;
import ab.j3d.model.ExtrudedObject2D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.ViewModel;
import ab.j3d.view.ViewModelView;

import com.numdata.oss.io.IndentingWriter;
import com.numdata.oss.ui.ImageTools;

/**
 * This class tests the conversion of the testmodel to POV-Ray.
 * All objects are converted seperately.
 *
 * @see     AbPovTestModel
 * @see     AbToPovConverter
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public final class TestAbToPovConverter
	extends TestCase
{
	/**
	 * This method tests if the needed texture declarations are generated.
	 * All textures should be declared.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testDeclarations()
		throws IOException
	{
		ImageTools.addToSearchPath( getTestDirectory() );
		final String texturesDirectory = getTexturesDirectory();

		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final ViewModel        viewModel       = testModel.getModel();
			final AbToPovConverter converter       = new AbToPovConverter( texturesDirectory );
			final PovScene         scene           = converter.convert( viewModel.getScene() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			scene.write( indentingWriter );

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
			"\t}\n\n";

		Assert.assertEquals( "Declaration generation error" , expected , actual );
	}

	/**
	 * This method tests the conversion from {@link Camera3D} object to
	 * {@link PovCamera}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testCamera3DToPovCamera()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel testModel = new AbPovTestModel();
			final ViewModel      model     = testModel.getModel();

			final ViewModelView view          = testModel.getView();
			final Matrix3D      viewTransform = view.getViewTransform();
			final Component     viewComponent = view.getComponent();
			final double        aspectRatio   = (double)viewComponent.getWidth() / (double)viewComponent.getHeight();

			final PovCamera       camera          = AbToPovConverter.convertCamera3D( viewTransform.inverse() , view.getCamera() , aspectRatio );
			final StringWriter    stringWriter    = new StringWriter();
			final IndentingWriter indentingWriter = PovScene.getIndentingWriter( stringWriter );

			camera.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testRedXRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.INIT , testModel.getRedXRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			povBox.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testGreenYRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.INIT , testModel.getGreenYRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			povBox.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testBlueZRotatedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      povBox          = converter.convertBox3D( Matrix3D.INIT , testModel.getBlueZRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			povBox.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testTexturedBox3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovMesh2         mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getTexturedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"\tvertex_vectors\n" +
			"\t{\n" +
			"\t\t8,\n" +
			"\t\t<-350.0,0.0,0.0> , <-208.57864,141.42136,0.0> , <-215.64971,148.49242,0.0> ,\n" +
			"\t\t<-357.07107,7.07107,0.0> , <-350.0,0.0,200.0> , <-208.57864,141.42136,200.0> ,\n" +
			"\t\t<-215.64971,148.49242,200.0> , <-357.07107,7.07107,200.0>\n" +
			"\t}\n" +
			"\tuv_vectors\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0.25,0.0> , <0.25,1.0> , <1.25,1.0> ,\n" +
			"\t\t<1.25,0.0> , <0.0,0.0> , <0.0,1.0> ,\n" +
			"\t\t<0.05,1.0> , <0.05,0.0> , <0.0,0.25> ,\n" +
			"\t\t<0.0,1.25> , <0.05,1.25> , <0.05,0.25>\n" +
			"\t}\n" +
			"\ttexture_list\n" +
			"\t{\n" +
			"\t\t2,\n" +
			"\t\ttexture { TEX_MPXs }\n" +
			"\t\ttexture { TEX_MFCs }\n" +
			"\t}\n" +
			"\tface_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,4,5>,0 , <0,5,1>,0 , <2,6,7>,0 , <2,7,3>,0 , <1,5,6>,1 , <1,6,2>,1 ,\n" +
			"\t\t<3,7,4>,1 , <3,4,0>,1 , <4,7,6>,1 , <4,6,5>,1 , <1,2,3>,1 , <1,3,0>,1\n" +
			"\t}\n" +
			"\tuv_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <4,5,6> , <4,6,7> ,\n" +
			"\t\t<4,5,6> , <4,6,7> , <8,9,10> , <8,10,11> , <8,9,10> , <8,10,11>\n" +
			"\t}\n" +
			"\tuv_mapping\n" +
			"}\n";

		Assert.assertEquals( "Textured Box3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a {@link Sphere3D} object to a
	 * {@link PovSphere}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testSphere3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      sphere          = converter.convertSphere3D( Matrix3D.INIT , testModel.getSphere3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			sphere.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testCylinder3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      cylinder        = converter.convertCylinder3D( Matrix3D.INIT , testModel.getCylinder3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			cylinder.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testCone3DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      cylinder        = converter.convertCylinder3D( Matrix3D.INIT , testModel.getCone3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			cylinder.write( indentingWriter );
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
	 * {@link IndentingWriter}.
	 */
	public static void testColorCubeToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getColorCube() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"\tvertex_vectors\n" +
			"\t{\n" +
			"\t\t8,\n" +
			"\t\t<-100.0,-100.0,100.0> , <-100.0,100.0,100.0> , <100.0,100.0,100.0> ,\n" +
			"\t\t<100.0,-100.0,100.0> , <-100.0,100.0,-100.0> , <-100.0,-100.0,-100.0> ,\n" +
			"\t\t<100.0,-100.0,-100.0> , <100.0,100.0,-100.0>\n" +
			"\t}\n" +
			"\tuv_vectors\n" +
			"\t{\n" +
			"\t\t4,\n" +
			"\t\t<0.5,0.0> , <0.5,0.5> , <0.0,0.5> ,\n" +
			"\t\t<0.0,0.0>\n" +
			"\t}\n" +
			"\ttexture_list\n" +
			"\t{\n" +
			"\t\t6,\n" +
			"\t\ttexture { TEX_CUBE_TOP }\n" +
			"\t\ttexture { TEX_CUBE_BOTTOM }\n" +
			"\t\ttexture { TEX_CUBE_FRONT }\n" +
			"\t\ttexture { TEX_CUBE_BACK }\n" +
			"\t\ttexture { TEX_CUBE_LEFT }\n" +
			"\t\ttexture { TEX_CUBE_RIGHT }\n" +
			"\t}\n" +
			"\tface_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,1,2>,0 , <0,2,3>,0 , <4,5,6>,1 , <4,6,7>,1 , <5,0,3>,2 , <5,3,6>,2 ,\n" +
			"\t\t<7,2,1>,3 , <7,1,4>,3 , <4,1,0>,4 , <4,0,5>,4 , <6,3,2>,5 , <6,2,7>,5\n" +
			"\t}\n" +
			"\tuv_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> ,\n" +
			"\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3>\n" +
			"\t}\n" +
			"\tuv_mapping\n" +
			"}\n";

		Assert.assertEquals( "ColorCube3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an {@link Object3D} (a colored cube
	 * with a different texture per face) to a {@link PovMesh2}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testTexturedColorCubeToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getTexturedColorCube() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"\tvertex_vectors\n" +
			"\t{\n" +
			"\t\t8,\n" +
			"\t\t<-100.0,-100.0,100.0> , <-100.0,100.0,100.0> , <100.0,100.0,100.0> ,\n" +
			"\t\t<100.0,-100.0,100.0> , <-100.0,100.0,-100.0> , <-100.0,-100.0,-100.0> ,\n" +
			"\t\t<100.0,-100.0,-100.0> , <100.0,100.0,-100.0>\n" +
			"\t}\n" +
			"\tuv_vectors\n" +
			"\t{\n" +
			"\t\t4,\n" +
			"\t\t<0.5,0.0> , <0.5,0.5> , <0.0,0.5> ,\n" +
			"\t\t<0.0,0.0>\n" +
			"\t}\n" +
			"\ttexture_list\n" +
			"\t{\n" +
			"\t\t6,\n" +
			"\t\ttexture { TEX_CUBE_TOP_TEXTURE_AND_COLOR }\n" +
			"\t\ttexture { TEX_CUBE_BOTTOM_TEXTURE_AND_COLOR }\n" +
			"\t\ttexture { TEX_CUBE_FRONT_TEXTURE_AND_COLOR }\n" +
			"\t\ttexture { TEX_CUBE_BACK_TEXTURE_AND_COLOR }\n" +
			"\t\ttexture { TEX_CUBE_LEFT_TEXTURE_AND_COLOR }\n" +
			"\t\ttexture { TEX_CUBE_RIGHT_TEXTURE_AND_COLOR }\n" +
			"\t}\n" +
			"\tface_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,1,2>,0 , <0,2,3>,0 , <4,5,6>,1 , <4,6,7>,1 , <5,0,3>,2 , <5,3,6>,2 ,\n" +
			"\t\t<7,2,1>,3 , <7,1,4>,3 , <4,1,0>,4 , <4,0,5>,4 , <6,3,2>,5 , <6,2,7>,5\n" +
			"\t}\n" +
			"\tuv_indices\n" +
			"\t{\n" +
			"\t\t12,\n" +
			"\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> ,\n" +
			"\t\t<0,1,2> , <0,2,3> , <0,1,2> , <0,2,3> , <0,1,2> , <0,2,3>\n" +
			"\t}\n" +
			"\tuv_mapping\n" +
			"}\n";

		Assert.assertEquals( "ColorCube3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an {@link ExtrudedObject2D} to
	 * a {@link PovMesh2}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testExtrudedObject2DToPov()
		throws IOException
	{
		final String actual;
		{
			final AbPovTestModel   testModel       = new AbPovTestModel();
			final AbToPovConverter converter       = new AbToPovConverter( getTexturesDirectory() );
			final PovGeometry      mesh            = converter.convertObject3D( Matrix3D.INIT , testModel.getExtrudedObject2D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = PovScene.getIndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"\tvertex_vectors\n" +
			"\t{\n" +
			"\t\t8,\n" +
			"\t\t<-400.0,0.0,-250.0> , <-400.0,100.0,-150.0> , <-300.0,0.0,-250.0> ,\n" +
			"\t\t<-300.0,100.0,-150.0> , <-300.0,100.0,-250.0> , <-300.0,200.0,-150.0> ,\n" +
			"\t\t<-400.0,100.0,-250.0> , <-400.0,200.0,-150.0>\n" +
			"\t}\n" +
			"\tface_indices\n" +
			"\t{\n" +
			"\t\t8,\n" +
			"\t\t<0,1,3> , <0,3,2> , <2,3,5> , <2,5,4> , <4,5,7> , <4,7,6> ,\n" +
			"\t\t<6,7,1> , <6,1,0>\n" +
			"\t}\n" +
			"\ttexture { TEX_RGB_255_175_175 }\n" +
			"}\n";

		Assert.assertEquals( "ExtrudedObject2D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the {@link AbToPovConverter#convertLight3D} method.
	 *
	 * @throws  Exception if the test fails.
	 */
	public static void testLight3DToPov()
		throws Exception
	{
		final String actual;
		{
			final AbPovTestModel  testModel       = new AbPovTestModel();
			final Matrix3D        transform       = Matrix3D.INIT.setTranslation( 500.0 , -500.0 , 500.0 );
			final PovLight        light           = AbToPovConverter.convertLight3D( transform , testModel.getLight3D() );
			final StringWriter    stringWriter    = new StringWriter();
			final IndentingWriter indentingWriter = PovScene.getIndentingWriter( stringWriter );

			light.write( indentingWriter );
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
		return testDirectory + "/decors";
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
		final ViewModel      viewModel = testModel.getModel();

		//final Object[]      viewIDs       = viewModel.getViewIDs();
		final ViewModelView view          = testModel.getView();
		final Matrix3D      viewTransform = view.getViewTransform();
		final Component     viewComponent = view.getComponent();
		final double        aspectRatio   = (double)viewComponent.getWidth() / (double)viewComponent.getHeight();

		final AbToPovConverter converter = new AbToPovConverter( getTexturesDirectory() );
		final PovScene scene = converter.convert( viewModel.getScene() );
		scene.add( AbToPovConverter.convertCamera3D( viewTransform.inverse(), view.getCamera(), aspectRatio ) );
		scene.write( new File( "test.pov" ) );
	}
}
