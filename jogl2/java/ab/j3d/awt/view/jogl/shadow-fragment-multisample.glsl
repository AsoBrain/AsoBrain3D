const float depthOffset = 0.0;

uniform sampler2DShadow shadowMap;

float shadow()
{
	vec4 shadowCoordinate = gl_TexCoord[ 7 ];

	float shadow = 1.0;
	if ( shadowCoordinate.w > 0.0 )
	{
		shadowCoordinate.z += depthOffset;

		float offset = shadowCoordinate.w / 256.0;
		float shadow1 = shadow2DProj( shadowMap, shadowCoordinate + vec4( -0.7 * offset, -0.7 * offset, 0.0, 0.0 ) ).z;
		float shadow2 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  0.0 * offset, -1.0 * offset, 0.0, 0.0 ) ).z;
		float shadow3 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  0.7 * offset, -0.7 * offset, 0.0, 0.0 ) ).z;
		float shadow4 = shadow2DProj( shadowMap, shadowCoordinate + vec4( -1.0 * offset,  0.0 * offset, 0.0, 0.0 ) ).z;
		float shadow5 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  0.0 * offset,  0.0 * offset, 0.0, 0.0 ) ).z;
		float shadow6 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  1.0 * offset,  0.0 * offset, 0.0, 0.0 ) ).z;
		float shadow7 = shadow2DProj( shadowMap, shadowCoordinate + vec4( -0.7 * offset,  0.7 * offset, 0.0, 0.0 ) ).z;
		float shadow8 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  0.0 * offset,  1.0 * offset, 0.0, 0.0 ) ).z;
		float shadow9 = shadow2DProj( shadowMap, shadowCoordinate + vec4(  0.7 * offset,  0.7 * offset, 0.0, 0.0 ) ).z;

		shadow = 0.0;
		shadow += shadow1;
		shadow += shadow2;
		shadow += shadow3;
		shadow += shadow4;
		shadow += shadow5;
		shadow += shadow6;
		shadow += shadow7;
		shadow += shadow8;
		shadow += shadow9;
		shadow /= 9.0;
	}

	return shadow;
}
