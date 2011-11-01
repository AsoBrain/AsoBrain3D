/*
 * Material color.
 */
varying vec4 diffuseColor;
vec4 color()
{
	return diffuseColor;
}

/*
 * Color mapping.
 */
uniform sampler2D colorMap;
varying vec2 colorMapCoord;
vec4 texture()
{
	return diffuseColor * texture2D( colorMap, colorMapCoord.st );
}
