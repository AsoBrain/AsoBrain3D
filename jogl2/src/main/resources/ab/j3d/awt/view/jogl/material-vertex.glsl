/*
 * Material color.
 */
varying vec4 diffuseColor;
void color()
{
	diffuseColor = gl_Color;
}

/*
 * Color mapping.
 */
varying vec2 colorMapCoord;
void texture()
{
	diffuseColor = gl_Color;
	colorMapCoord = ( gl_TextureMatrix[ TEXTURE_UNIT_COLOR ] * gl_MultiTexCoord0 ).st;
}
