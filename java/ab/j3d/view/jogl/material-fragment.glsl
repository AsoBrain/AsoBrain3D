/*
 * Material color.
 */
vec4 color()
{
	return gl_Color;
}

/*
 * Color mapping.
 */
uniform sampler2D colorMap;
vec4 texture()
{
	return gl_Color * texture2D( colorMap , gl_TexCoord[ 0 ].st );
}
