varying vec3 normal;
varying vec4 frontColor;
void main()
{
	normal = gl_Normal;
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}