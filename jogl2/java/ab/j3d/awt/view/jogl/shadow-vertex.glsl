varying vec4 v_shadowCoord;

void shadow()
{
	v_shadowCoord = gl_TextureMatrix[ TEXTURE_UNIT_SHADOW ] * gl_ModelViewMatrix * gl_Vertex;
}
