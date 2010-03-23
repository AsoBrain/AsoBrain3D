/*
 * Material color.
 */
void color()
{
	gl_FrontColor = gl_Color;
	gl_BackColor = gl_Color;
}

/*
 * Color mapping.
 */
void texture()
{
	gl_TexCoord[ 0 ] = gl_TextureMatrix[ 0 ] * gl_MultiTexCoord0;
	gl_FrontColor = gl_Color;
	gl_BackColor = gl_Color;
}
