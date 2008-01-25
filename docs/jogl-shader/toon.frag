uniform vec4 Colour1,Colour2,Colour3,Colour4;
varying vec3 normal;
void main(){
	float intensity;
	vec4 colour;
	vec3 n = normalize(normal);
	vec3 lDir = normalize(vec3(gl_LightSource[0].position));
	intensity = dot(lDir,n);
	intensity = dot(lDir,n);
	if (intensity > 0.95)
		colour = Colour1;
	else if (intensity > 0.5)
		colour = Colour2;
	else if (intensity > 0.25)
		colour = Colour3;
	else
		colour = Colour4;
		gl_FragColor = colour;
}