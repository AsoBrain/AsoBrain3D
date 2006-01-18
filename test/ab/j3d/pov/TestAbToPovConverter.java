/* $Id$
 * ====================================================================
 * (C) Copyright Numdata BV 2005-2006
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
import ab.j3d.model.Light3D;
import ab.j3d.model.Object3D;
import ab.j3d.model.Sphere3D;
import ab.j3d.view.ViewModel;

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
			final AbToPovConverter converter       = new AbToPovConverter( texturesDirectory );
			final PovScene         scene           = converter.convert( testModel.getModel() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			scene.write( indentingWriter );
			String temp = stringWriter.toString();

			/*
			 * The whole scene needs to be converted, but only the texture
			 * definition part is needed.
			 */
			temp = temp.substring( temp.indexOf( " * Texture definitions" ) - 3 , temp.indexOf( " * Declared geometry" ) - 3 );

			actual = temp;
		}

		final String expected =
			"/*\n" +
			" * Texture definitions\n" +
			" */\n" +

			"#declare TEX_CUBE_BACK =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_BACK\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_CUBE_BOTTOM =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_BOTTOM\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_CUBE_FRONT =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_FRONT\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_CUBE_LEFT =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_LEFT\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_CUBE_RIGHT =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_RIGHT\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_CUBE_TOP =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "CUBE_TOP\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_MFCs =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "MFCs\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_MPXs =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            image_map { jpeg \"" + texturesDirectory + "MPXs\" }\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_0_0_255 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment { color rgb < 0.0 , 0.0 , 1.0 > }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_0_255_0 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment\n" +
			"        {\n" +
			"            color rgb < 0.0 , 1.0 , 0.0 >\n" +
			"            filter 0.8\n" +
			"        }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_255_0_0 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment { color rgb < 1.0 , 0.0 , 0.0 > }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_255_0_255 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment { color rgb < 1.0 , 0.0 , 1.0 > }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_255_175_175 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment { color rgb < 1.0 , 0.6862745098039216 , 0.6862745098039216 > }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n" +

			"#declare TEX_RGB_255_255_255 =\n" +
			"    texture\n" +
			"    {\n" +
			"        pigment { color rgb < 1.0 , 1.0 , 1.0 > }\n" +
			"        finish\n" +
			"        {\n" +
			"            phong 0.5\n" +
			"            ambient 0.3\n" +
			"            diffuse 0.3\n" +
			"            specular 0.3\n" +
			"            reflection 0.05\n" +
			"        }\n" +
			"    }\n\n";

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
			final AbPovTestModel  testModel       = new AbPovTestModel();
			final ViewModel       model           = testModel.getModel();
			final Object[]        ids             = model.getViewIDs();

			//@TODO Test conversion of Camera3D when integrated in model.
			final PovCamera       camera          = AbToPovConverter.convertCamera3D( model.getView( ids[ 0 ] ) );
			final StringWriter    stringWriter    = new StringWriter();
			final IndentingWriter indentingWriter = new IndentingWriter( stringWriter );

			camera.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected = "camera // camera\n" +
		                        "{\n" +
		                        "    right    < 1.33 , 0.0 , 0.0 >\n" +
		                        "    angle    45.0\n" +
		                        "    matrix < 1.0 , 0.0 , 0.0 ,\n" +
		                        "             0.0 , 0.0 , 1.0 ,\n" +
		                        "             0.0 , 1.0 , 0.0 ,\n" +
		                        "             0.0 , -1000.0 , 0.0 >\n" +
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
			final PovBox           povBox          = converter.convertBox3D( testModel.getRedXRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			povBox.texture.setDeclared();

			povBox.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box // box\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 >,\n" +
			"    < 100.0 , 200.0 , 100.0 >\n" +
			"    texture { TEX_RGB_255_0_0 }\n" +
			"    matrix < 1.0 , 0.0 , 0.0 ,\n" +
			"             0.0 , 0.984807753012208 , 0.17364817766693033 ,\n" +
			"             0.0 , -0.17364817766693033 , 0.984807753012208 ,\n" +
			"             -200.0 , 0.0 , -250.0 >\n" +
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
			final PovBox           povBox          = converter.convertBox3D( testModel.getGreenYRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			povBox.texture.setDeclared();

			povBox.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box // box\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 >,\n" +
			"    < 100.0 , 200.0 , 100.0 >\n" +
			"    texture { TEX_RGB_0_255_0 }\n" +
			"    matrix < 0.984807753012208 , 0.0 , -0.17364817766693033 ,\n" +
			"             0.0 , 1.0 , 0.0 ,\n" +
			"             0.17364817766693033 , 0.0 , 0.984807753012208 ,\n" +
			"             -50.0 , 0.0 , -250.0 >\n" +
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
			final PovBox           povBox          = converter.convertBox3D( testModel.getBlueZRotatedBox3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			povBox.texture.setDeclared();

			povBox.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"box // box\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 >,\n" +
			"    < 100.0 , 200.0 , 100.0 >\n" +
			"    texture { TEX_RGB_0_0_255 }\n" +
			"    matrix < 0.984807753012208 , 0.17364817766693033 , 0.0 ,\n" +
			"             -0.17364817766693033 , 0.984807753012208 , 0.0 ,\n" +
			"             0.0 , 0.0 , 1.0 ,\n" +
			"             200.0 , 0.0 , -250.0 >\n" +
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
			final PovMesh2         mesh            = converter.convertObject3D( testModel.getTexturedBox3D() , Matrix3D.INIT );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"    vertex_vectors\n" +
			"    {\n" +
			"        8,\n" +
			"        < -350.0 , 0.0 , 0.0 > , < -350.0 , 0.0 , 200.0 > , < -208.5786437626905 , 141.42135623730948 , 200.0 > ,\n" +
			"        < -208.5786437626905 , 141.42135623730948 , 0.0 > , < -215.64971157455597 , 148.49242404917496 , 0.0 > , < -215.64971157455597 , 148.49242404917496 , 200.0 > ,\n" +
			"        < -357.0710678118655 , 7.0710678118654755 , 200.0 > , < -357.0710678118655 , 7.0710678118654755 , 0.0 >\n" +
			"    }\n" +
			"    uv_vectors\n" +
			"    {\n" +
			"        12,\n" +
			"        < 0.53125 , -3.0 > , < 0.53125 , 0.125 > , < 3.65625 , 0.125 > ,\n" +
			"        < 3.65625 , -3.0 > , < 0.0 , -6.0 > , < 0.0 , 0.25 > ,\n" +
			"        < 0.3125 , 0.25 > , < 0.3125 , -6.0 > , < 0.0 , 0.0625 > ,\n" +
			"        < 0.3125 , 0.0625 > , < 0.3125 , 6.3125 > , < 0.0 , 6.3125 >\n" +
			"    }\n" +
			"    texture_list\n" +
			"    {\n" +
			"        2,\n" +
			"        texture { TEX_MPXs }\n" +
			"        texture { TEX_MFCs }\n" +
			"    }\n" +
			"    face_indices\n" +
			"    {\n" +
			"        12,\n" +
			"        < 0 , 1 , 2 > , 0 , < 0 , 2 , 3 > , 0 , < 4 , 5 , 6 > , 0 ,\n" +
			"        < 4 , 6 , 7 > , 0 , < 3 , 2 , 5 > , 1 , < 3 , 5 , 4 > , 1 ,\n" +
			"        < 7 , 6 , 1 > , 1 , < 7 , 1 , 0 > , 1 , < 1 , 6 , 5 > , 1 ,\n" +
			"        < 1 , 5 , 2 > , 1 , < 3 , 4 , 7 > , 1 , < 3 , 7 , 0 > , 1\n" +
			"    }\n" +
			"    uv_indices\n" +
			"    {\n" +
			"        12,\n" +
			"        < 0 , 1 , 2 > , < 0 , 2 , 3 > , < 3 , 2 , 1 > ,\n" +
			"        < 3 , 1 , 0 > , < 4 , 5 , 6 > , < 4 , 6 , 7 > ,\n" +
			"        < 7 , 6 , 5 > , < 7 , 5 , 4 > , < 8 , 9 , 10 > ,\n" +
			"        < 8 , 10 , 11 > , < 11 , 10 , 9 > , < 11 , 9 , 8 >\n" +
			"    }\n" +
			"    uv_mapping\n" +
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
			final PovSphere        sphere          = converter.convertSphere3D( testModel.getSphere3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			sphere.texture.setDeclared();

			sphere.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"sphere // sphere\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 >, 50.0\n" +
			"    matrix < 1.0 , 0.0 , 0.0 ,\n" +
			"             0.0 , 1.0 , 0.0 ,\n" +
			"             0.0 , 0.0 , 1.0 ,\n" +
			"             0.0 , 300.0 , -200.0 >\n" +
			"texture { TEX_RGB_0_0_255 }\n" +
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
			final PovCylinder      cylinder        = converter.convertCylinder3D( testModel.getCylinder3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			cylinder.texture.setDeclared();

			cylinder.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"cone // cylinder\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 > , 50.0\n" +
			"    < 0.0 , 0.0 , 100.0 > , 50.0\n" +
			"    matrix < 1.0 , 0.0 , 0.0 ,\n" +
			"             0.0 , 1.0 , 0.0 ,\n" +
			"             0.0 , 0.0 , 1.0 ,\n" +
			"             0.0 , 0.0 , 150.0 >\n\n" +
			"    texture { TEX_RGB_255_0_255 }\n" +
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
			final PovCylinder      cylinder        = converter.convertCylinder3D( testModel.getCone3D() );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			cylinder.texture.setDeclared();

			cylinder.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"cone // cylinder\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 > , 100.0\n" +
			"    < 0.0 , 0.0 , 200.0 > , 50.0\n" +
			"    matrix < 1.0 , 0.0 , 0.0 ,\n" +
			"             0.0 , 0.7071067811865476 , 0.7071067811865475 ,\n" +
			"             0.0 , -0.7071067811865475 , 0.7071067811865476 ,\n" +
			"             250.0 , 0.0 , 0.0 >\n\n" +
			"    texture { TEX_RGB_255_255_255 }\n" +
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
			final PovMesh2         mesh            = converter.convertObject3D( testModel.getColorCube() , Matrix3D.INIT );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"    vertex_vectors\n" +
			"    {\n" +
			"        8,\n" +
			"        < -100.0 , -100.0 , 100.0 > , < -100.0 , 100.0 , 100.0 > , < 100.0 , 100.0 , 100.0 > ,\n" +
			"        < 100.0 , -100.0 , 100.0 > , < -100.0 , 100.0 , -100.0 > , < -100.0 , -100.0 , -100.0 > ,\n" +
			"        < 100.0 , -100.0 , -100.0 > , < 100.0 , 100.0 , -100.0 >\n" +
			"    }\n" +
			"    uv_vectors\n" +
			"    {\n" +
			"        4,\n" +
			"        < 2.0 , 0.0 > , < 2.0 , 2.0 > , < 0.0 , 2.0 > ,\n" +
			"        < 0.0 , 0.0 >\n" +
			"    }\n" +
			"    texture_list\n" +
			"    {\n" +
			"        6,\n" +
			"        texture { TEX_CUBE_TOP }\n" +
			"        texture { TEX_CUBE_BOTTOM }\n" +
			"        texture { TEX_CUBE_FRONT }\n" +
			"        texture { TEX_CUBE_BACK }\n" +
			"        texture { TEX_CUBE_LEFT }\n" +
			"        texture { TEX_CUBE_RIGHT }\n" +
			"    }\n" +
			"    face_indices\n" +
			"    {\n" +
			"        12,\n" +
			"        < 0 , 1 , 2 > , 0 , < 0 , 2 , 3 > , 0 , < 4 , 5 , 6 > , 1 ,\n" +
			"        < 4 , 6 , 7 > , 1 , < 5 , 0 , 3 > , 2 , < 5 , 3 , 6 > , 2 ,\n" +
			"        < 7 , 2 , 1 > , 3 , < 7 , 1 , 4 > , 3 , < 4 , 1 , 0 > , 4 ,\n" +
			"        < 4 , 0 , 5 > , 4 , < 6 , 3 , 2 > , 5 , < 6 , 2 , 7 > , 5\n" +
			"    }\n" +
			"    uv_indices\n" +
			"    {\n" +
			"        12,\n" +
			"        < 0 , 1 , 2 > , < 0 , 2 , 3 > , < 0 , 1 , 2 > ,\n" +
			"        < 0 , 2 , 3 > , < 0 , 1 , 2 > , < 0 , 2 , 3 > ,\n" +
			"        < 0 , 1 , 2 > , < 0 , 2 , 3 > , < 0 , 1 , 2 > ,\n" +
			"        < 0 , 2 , 3 > , < 0 , 1 , 2 > , < 0 , 2 , 3 >\n" +
			"    }\n" +
			"    uv_mapping\n" +
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
			final PovMesh2         mesh            = converter.convertObject3D( testModel.getExtrudedObject2D() , Matrix3D.INIT );
			final StringWriter     stringWriter    = new StringWriter();
			final IndentingWriter  indentingWriter = new IndentingWriter( stringWriter );

			mesh.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"mesh2\n" +
			"{\n" +
			"    vertex_vectors\n" +
			"    {\n" +
			"        8,\n" +
			"        < -400.0 , 0.0 , -250.0 > , < -400.0 , 100.0 , -150.0 > , < -300.0 , 100.0 , -150.0 > ,\n" +
			"        < -300.0 , 0.0 , -250.0 > , < -300.0 , 200.0 , -150.0 > , < -300.0 , 100.0 , -250.0 > ,\n" +
			"        < -400.0 , 200.0 , -150.0 > , < -400.0 , 100.0 , -250.0 >\n" +
			"    }\n" +
			"    face_indices\n" +
			"    {\n" +
			"        8,\n" +
			"        < 0 , 1 , 2 > , < 0 , 2 , 3 > , < 3 , 2 , 4 > ,\n" +
			"        < 3 , 4 , 5 > , < 5 , 4 , 6 > , < 5 , 6 , 7 > ,\n" +
			"        < 7 , 6 , 1 > , < 7 , 1 , 0 >\n" +
			"    }\n" +
			"    uv_indices\n" +
			"    {\n" +
			"        8,\n" +
			"        < 0 , 0 , 0 > , < 0 , 0 , 0 > , < 0 , 0 , 0 > ,\n" +
			"        < 0 , 0 , 0 > , < 0 , 0 , 0 > , < 0 , 0 , 0 > ,\n" +
			"        < 0 , 0 , 0 > , < 0 , 0 , 0 >\n" +
			"    }\n" +
			"    texture { TEX_RGB_255_175_175 }\n" +
			"}\n";

		Assert.assertEquals( "ExtrudedObject2D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a {@link Light3D} object to a
	 * {@link PovLight}.
	 *
	 * @throws IOException When there was a problem writing to the
	 * {@link IndentingWriter}.
	 */
	public static void testLight3DToPov()
		throws IOException
	{
		final String actual;
		{
			//@TODO Properly test the light conversion when Light3D is completed.
			//final AbPovTestModel  testModel       = new AbPovTestModel();

			final Matrix3D        transform       = Matrix3D.INIT.setTranslation( 500.0 , -500.0 , 500.0 );
			final PovLight        light           = AbToPovConverter.convertLight3D( /* testModel.getLight3D() , */ transform );
			final StringWriter    stringWriter    = new StringWriter();
			final IndentingWriter indentingWriter = new IndentingWriter( stringWriter );

			light.write( indentingWriter );
			actual = stringWriter.toString();
		}

		final String expected =
			"light_source // light\n" +
			"{\n" +
			"    < 0.0 , 0.0 , 0.0 >\n" +
			"    color < 1.0 , 1.0 , 1.0 >\n" +
			"    matrix < 1.0 , 0.0 , 0.0 ,\n" +
			"             0.0 , 1.0 , 0.0 ,\n" +
			"             0.0 , 0.0 , 1.0 ,\n" +
			"             500.0 , -500.0 , 500.0 >\n" +
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
		final Class       thisClass        = TestAbToPovConverter.class;
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
		return testDirectory + "/textures/";
	}

	/**
	 * Write the whole testscene to a POV-Ray file.
	 */
	public static void writeToFile()
	{
		final AbPovTestModel   testModel = new AbPovTestModel();
		final ViewModel        viewModel = testModel.getModel();
		final AbToPovConverter converter = new AbToPovConverter( getTexturesDirectory() );
		final PovScene         scene     = converter.convert( viewModel );
		final Object[]         ids       = viewModel.getViewIDs();

		scene.add( AbToPovConverter.convertCamera3D( viewModel.getView( ids[ 0 ] ) ) );
		scene.write( new File( "test.pov" ) );
	}
}
