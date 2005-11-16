/*
 * $Id$
 *
 * (C) Copyright Numdata BV 2005-2005 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package pov;

import junit.framework.*;

/**
 * This class tests the conversion from the AB-model defined by ABTestModel.java to a povray version
 * 3.6 textfile. Of course, this test and the testmodel must be consistent.
 *
 * @author  Rob Veneberg
 * @version $Revision$ $Date$
 */
public class TestABToPovConverter
	extends TestCase
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestABToPovConverter.class.getName();

	/**
	 * This method tests if the needed pov includes are generated.
	 */
	public void testIncludes()
	{
		System.out.println( CLASS_NAME + ".testIncludes" );

		final String actual;
		final String expected;

		expected = "include \"colors.inc\"\n\n";

		actual = expected;

		Assert.assertEquals( "Include generation error" , expected , actual );
	}

	/**
	 * This method tests the generated global pov settings. The ambient light value is assumed to be 2.
	 */
	public void testGlobalSettings()
	{
		System.out.println( CLASS_NAME + ".testGlobalSettings" );

		final String actual;
		final String expected;

		expected = "global_settings{ ambient_light < 2 , 2 , 2 > }\n\n";

		actual = expected;

		Assert.assertEquals( "Global settings generation error" , expected , actual );
	}

	/**
	 * This method tests if the needed pov declarations are generated.
	 */
	public void testDeclarations()
	{
		System.out.println( CLASS_NAME + ".testDeclarations" );

		final String actual;
		final String expected;

		expected = "#declare TEXTURE_MPXs =\n" +
				   "texture {\n" +
				   "  pigment {\n" +
				   "    image_map { jpeg \"/home/rob/textures/MPXs\" }\n" +
				   "  }\n" +
				   "  scale < 0.1 , 0.1 , 0.1 >\n" +
				   "}\n\n" +

				   "#declare TEXTURE_MFCs =\n" +
				   "texture {\n" +
				   "  pigment {\n" +
				   "    image_map { jpeg \"/home/rob/textures/MFCs\" }\n" +
				   "  }\n" +
				   "  scale < 0.05 , 0.05 , 0.05 >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Declaration generation error" , expected , actual );
	}

	/**
	 * This method tests the conversion from Camera3D object to pov camera.
	 */
	public void testCamera3DToPovCamera()
	{
		System.out.println( CLASS_NAME + ".testCamera3DToPovCamera" );

		final String actual;
		final String expected;

		expected = "camera\n" +
				   "{\n" +
				   "  location < 0.0 , 0.0 , -1.0 >\n" +
				   "  look_at  < 0.0 , 0.0 ,  0.0 >\n" +
				   "  angle 45.0\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Camera to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a red, rotated (around x-axis) Box3D object to a pov box.
	 */
	public void testRedXRotatedBox3DToPov()
	{
		System.out.println( CLASS_NAME + ".testRedXRotatedBox3DToPov" );

		final String actual;
		final String expected;

		expected = "box\n" +
				   "{\n" +
				   "  < 0.0 , 0.0 , 0.0 >\n" +
				   "  < 0.1 , 0.1 , 0.1 >\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Red\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  1.0 ,  0.0                ,  0.0                ,\n" +
				   "            0.0 ,  0.7071067811865476 , -0.7071067811865475 ,\n" +
				   "            0.0 ,  0.7071067811865475 ,  0.7071067811865476 ,\n" +
				   "           -0.2 , -0.2                ,  0.0                >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "RedBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a green, rotated (around y-axis) Box3D object to a pov box. The Box3D
	 * characteristics opacity, ambient, diffuse and specular reflectivity and specular exponent are
	 * also tested.
	 */
	public void testGreenYRotatedBox3DToPov()
	{
		System.out.println( CLASS_NAME + ".testGreenYRotatedBox3DToPov" );

		final String actual;
		final String expected;

		expected = "box\n" +
				   "{\n" +
				   "  < 0.0 , 0.0 , 0.0 >\n" +
				   "  < 0.1 , 0.1 , 0.1 >\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Green\n" +
				   "      filter 0.8\n" +           // AB -> opacity
				   "    }\n" +
				   "    finish\n" +
				   "    {\n" +
				   "      ambient 5.0\n" +          // AB -> ambientReflectivity
				   "      diffuse 0.6\n" +          // AB -> diffuseReflectivity
				   "      specular 0.75\n" +        // AB -> specularReflectivity
				   "      roughness 0.001\n" +      // AB -> specularExponent
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  0.7071067811865476 , -0.7071067811865475  ,  0.0 ,\n" +
				   "            0.7071067811865475 ,  0.7071067811865476  ,  0.0 ,\n" +
				   "            0.0                ,  0.0                 ,  1.0 ,\n" +
				   "           -0.05               , -0.2                 ,  0.0 >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "GreenBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a blue, rotated (around z-axis) Box3D object to a pov box.
	 */
	public void testBlueZRotatedBox3DToPov()
	{
		System.out.println( CLASS_NAME + ".testBlueZRotatedBox3DToPov" );

		final String actual;
		final String expected;

		expected = "box\n" +
				   "{\n" +
				   "  < 0.0 , 0.0 , 0.0 >\n" +
				   "  < 0.1 , 0.1 , 0.1 >\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Blue\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  0.7071067811865476 ,  0.0 ,  0.7071067811865475 ,\n" +
				   "            0.0                ,  1.0 ,  0.0                ,\n" +
				   "           -0.7071067811865475 ,  0.0 ,  0.7071067811865476 ,\n" +
				   "            0.2                , -0.2 ,  0.0                >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "BlueBox3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a textured Box3D object (a wooden panel) with a different side-texture to pov.
	 * In pov the object needs to be constructed out of seperate polygons because a box in pov can't have
	 * more than one texture.
	 */
	public void testTexturedBox3DToPov()
	{
		System.out.println( CLASS_NAME + ".testTexturedBox3DToPov" );

		final String actual;
		final String expected;

		expected = "union\n" +
				   "{\n" +
				   "  polygon\n" +          //Frontside of panel
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.0 > < 0.2 , 0.0 , 0.0 > < 0.2 , 0.2 , 0.0 > < 0.0 , 0.2 , 0.0 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MPXs\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +          //Backside of panel
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.01 > < 0.2 , 0.0 , 0.01 > < 0.2 , 0.2 , 0.01 > < 0.0 , 0.2 , 0.01 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MPXs\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +          //Leftside of panel
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.0 > < 0.0 , 0.0 , 0.01 > < 0.0 , 0.2 , 0.01 > < 0.0 , 0.2 , 0.0 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MFCs\n" +
				   "      rotate < 0 , 90 , 0 >\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +          //Rightside of panel
				   "  {\n" +
				   "    4 , < 0.2 , 0.0 , 0.0 > < 0.2 , 0.0 , 0.01 > < 0.2 , 0.2 , 0.01 > < 0.2 , 0.2 , 0.0 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MFCs\n" +
				   "      rotate < 0 , 90 , 0 >\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +          //Topside of panel
				   "  {\n" +
				   "    4 , < 0.0 , 0.2 , 0.0 > < 0.0 , 0.2 , 0.01 > < 0.2 , 0.2 , 0.01 > < 0.2 , 0.2 , 0.0 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MFCs\n" +
				   "      rotate < 90 , 0 , 0>\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +          //Bottomside of panel
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.0 > < 0.0 , 0.0 , 0.01 > < 0.2 , 0.0 , 0.01 > < 0.2 , 0.0 , 0.0 >\n" +
				   "    texture\n" +
				   "    {\n" +
				   "      TEXTURE_MFCs\n" +
				   "      rotate < 90 , 0 , 0>\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  0.7071067811865474 , 0.0 , -0.7071067811865477 ,\n" +
				   "            0.0                , 1.0 ,  0.0                ,\n" +
				   "            0.7071067811865477 , 0.0 ,  0.7071067811865474 ,\n" +
				   "           -0.3                , 0.0 ,  0.0                >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Textured Box3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a Sphere3D object to a pov sphere.
	 */
	public void testSphere3DToPov()
	{
		System.out.println( CLASS_NAME + ".testSphere3DToPov" );

		final String actual;
		final String expected;

		expected = "sphere\n" +
				   "{\n" +
		           "  < 0 , 0 , 0 >, 0.05\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Blue\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  1.0 ,  0.0 ,  0.0 ,\n" +
				   "            0.0 ,  1.0 ,  0.0 ,\n" +
				   "            0.0 ,  0.0 ,  1.0 ,\n" +
				   "            0.0 ,  0.0 ,  0.0 >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Sphere3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a Cylinder3D object to a pov cylinder.
	 */
	public void testCylinder3DToPov()
	{
		System.out.println( CLASS_NAME + ".testCylinder3DToPov" );

		final String actual;
		final String expected;

		expected = "cylinder\n" +
				   "{\n" +
				   "  < 0.0 , 0.0 , 0.0 >,\n" +
				   "  < 0.0 , 0.1 , 0.0 >,\n" +
				   "  0.05\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Magenta\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  1.0 ,  0.0 ,  0.0 ,\n" +
				   "            0.0 ,  1.0 ,  0.0 ,\n" +
				   "            0.0 ,  0.0 ,  1.0 ,\n" +
				   "            0.0 ,  0.1 ,  0.0 >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Cylinder3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a Cone3D object to a pov cone.
	 */
	public void testCone3DToPov()
	{
		System.out.println( CLASS_NAME + ".testCone3DToPov" );

		final String actual;
		final String expected;

		expected = "cone\n" +
				   "{\n" +
				   "  < 0.0 , 0.0 , 0.0 >, 0.1\n" +
				   "  < 0.0 , 0.2 , 0.0 >, 0.05\n" +
				   "  texture\n" +
				   "  {\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color White\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  1.0 ,  0.0                ,  0.0                ,\n" +
				   "            0.0 ,  0.7071067811865476 , -0.7071067811865475 ,\n" +
				   "            0.0 ,  0.7071067811865475 ,  0.7071067811865476 ,\n" +
				   "            0.2 ,  0.0                ,  0.0                >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Cone3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an AB-object (a colored cube) consisting out of polygons to
	 * a pov object consisting out of polygons.
	 */
	public void testColorCubeToPov()
	{
		System.out.println( CLASS_NAME + ".testColorCubeToPov" );

		final String actual;
		final String expected;

		expected = "union\n" +
				   "{\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < -0.1 , -0.1 , -0.1 > < -0.1 , 0.1 , -0.1 > < 0.1 , 0.1 , -0.1 > < 0.1 , -0.1 , -0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Cyan\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < -0.1 , -0.1 , 0.1 >, < -0.1 , 0.1 , 0.1 > < 0.1 , 0.1 , 0.1 > < 0.1 , -0.1 , 0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Magenta\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < 0.1 , -0.1 , -0.1 > < 0.1 , 0.1 , -0.1 > < 0.1 , 0.1 , 0.1 > < 0.1 , -0.1 , 0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Blue\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < -0.1 , -0.1 , -0.1 > < -0.1 , -0.1 , 0.1 > < -0.1 , 0.1 , 0.1 > < -0.1 , 0.1 , -0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Yellow\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < -0.1 , 0.1 , -0.1 > < -0.1 , 0.1 , 0.1 > < 0.1 , 0.1 , 0.1 > < 0.1 , 0.1 , -0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Red\n" +
				   "    }\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < -0.1 , -0.1 , -0.1 > < -0.1 , -0.1 , 0.1 > < 0.1 , -0.1 , 0.1 > < 0.1 , -0.1 , -0.1 >\n" +
				   "    pigment\n" +
				   "    {\n" +
				   "      color Green\n" +
				   "    }\n" +
				   "  }\n" +
				   "  matrix <  0.5000000000000001 , -0.7071067811865475 ,  0.5                ,\n" +
				   "            0.5                ,  0.7071067811865476 ,  0.4999999999999999 ,\n" +
				   "           -0.7071067811865475 ,  0.0                ,  0.7071067811865476 ,\n" +
				   "            0.0                ,  0.0                ,  0.3                >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "ColorCube3D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of an ExtrudedObject2D object to pov. There is no extruded object type in pov, so the
	 * object needs to be constructed out of polygons in pov.
	 */
	public void testExtrudedObject2DToPov()
	{
		System.out.println( CLASS_NAME + ".testExtrudedObject2DToPov" );

		final String actual;
		final String expected;

		expected = "union\n" +
				   "{\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.0 > < 0.1 , 0.0 , 0.0 > < 0.1 , 0.1 , 0.1 > < 0.0 , 0.1 , 0.1 >\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.1 > < 0.1 , 0.0 , 0.1 > < 0.1 , 0.1 , 0.2 > < 0.0 , 0.1 , 0.2 >\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < 0.1 , 0.0 , 0.0 > < 0.1 , 0.0 , 0.1 > < 0.1 , 0.1 , 0.2 > < 0.1 , 0.1 , 0.1 >\n" +
				   "  }\n" +
				   "  polygon\n" +
				   "  {\n" +
				   "    4 , < 0.0 , 0.0 , 0.0 > < 0.0 , 0.0 , 0.1 > < 0.0 , 0.1 , 0.2 > < 0.0 , 0.1 , 0.1 >\n" +
				   "  }\n" +
				   "  pigment\n" +
				   "  {\n" +
				   "    color Pink\n" +
				   "  }\n" +
				   "  matrix <  1.0 ,  0.0 , 0.0 ,\n" +
				   "            0.0 ,  1.0 , 0.0 ,\n" +
				   "            0.0 ,  0.0 , 1.0 ,\n" +
				   "           -0.4 , -0.2 , 0.0 >\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "ExtrudedObject2D to pov conversion error" , expected , actual );
	}

	/**
	 * This method tests the conversion of a Light3D object to a pov light.
	 */
	public void testLight3DToPov()
	{
		System.out.println( CLASS_NAME + ".testLight3DToPov" );

		final String actual;
		final String expected;

		expected = "light_source\n" +
				   "{\n" +
				   "  < -2.0 ,  2.0 , -2.0 >\n" +
				   "  color White\n" +
				   "}\n\n";

		actual = expected;

		Assert.assertEquals( "Light3D to pov conversion error" , expected , actual );
	}
}