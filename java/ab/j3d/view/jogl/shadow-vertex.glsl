void shadow()
{
	gl_TexCoord[ 7 ] = gl_TextureMatrix[ 7 ] * gl_ModelViewMatrix * gl_Vertex;
}
