uniform float width;
uniform float height;
uniform sampler2DShadow depthNear;
uniform sampler2DShadow depthOpaque;

void depthPeeling()
{
	vec3 coord = gl_FragCoord.xyz;
	coord.x /= width;
	coord.y /= height;
	if ( gl_FragCoord.z >= shadow2D( depthOpaque , coord ).r )
		discard;
	if ( gl_FragCoord.z <= shadow2D( depthNear   , coord ).r )
		discard;
}
