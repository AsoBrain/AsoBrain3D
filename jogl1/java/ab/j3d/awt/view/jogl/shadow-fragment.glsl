const float depthOffset = 0.0;

uniform sampler2DShadow shadowMap;
varying vec4 v_shadowCoord;

float shadow()
{
	vec4 shadowCoordinate = v_shadowCoord;

	float shadow = 1.0;
	if ( shadowCoordinate.w > 0.0 )
	{
		shadowCoordinate.z += depthOffset;
		shadow = shadow2DProj( shadowMap, shadowCoordinate ).z;
	}

	return shadow;
}
