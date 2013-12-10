varying vec3 vertex;
varying vec3 normal;

/*
 * Per-pixel lighting. (point lights)
 */
void lighting()
{
	// source: http://www.clockworkcoders.com/oglsl/tutorial5.htm
	vertex = ( gl_ModelViewMatrix * gl_Vertex ).xyz;
	normal = gl_NormalMatrix * gl_Normal;
}
