uniform sampler2D front;
uniform sampler2D back;

/**
 * Blends two textures using the same blending formula as
 * 'RenderStyle.blendColor'.
 */
void main()
{
	vec4 src = texture2D( front , gl_TexCoord[ 0 ].st );
	vec4 dst = texture2D( back  , gl_TexCoord[ 0 ].st );

	float alpha = src.a + dst.a * ( 1.0 - src.a );
	vec4 result = vec4( ( src.rgb * src.a + dst.rgb * dst.a * ( 1.0 - src.a ) ) / alpha , alpha );

	gl_FragColor = result;
}
