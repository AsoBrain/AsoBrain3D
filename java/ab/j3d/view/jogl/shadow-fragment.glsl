const float depthOffset = 0.0;

uniform sampler2DShadow shadowMap;

float shadow()
{
	vec4 shadowCoordinate = gl_TexCoord[ 7 ];

	float shadow = 1.0;
	if ( shadowCoordinate.w > 0.0 )
	{
		shadowCoordinate.z += depthOffset;
		shadow = shadow2DProj( shadowMap, shadowCoordinate ).z;
	}

	return shadow;
}
