const int lightCount = 3;

varying vec3 vertex;
varying vec3 normal;

/*
 * Per-pixel lighting. (point lights)
 */
vec4 lighting( in vec4 color )
{
	vec3 material = vec3( 0.0 , 0.0 , 0.0 );
	vec3 specular = vec3( 0.0 , 0.0 , 0.0 );

	// based on source from: http://www.clockworkcoders.com/oglsl/tutorial5.htm
	vec3 N = normalize( normal );
	vec3 E = normalize( -vertex );
	for ( int i = 0 ; i < lightCount ; i++ )
	{
		vec3 distVector = gl_LightSource[ i ].position.xyz - vertex;

		vec3 L = normalize( distVector );
		vec3 R = normalize( -reflect( L , N ) );
		vec3 Iamb  = gl_FrontLightProduct[ i ].ambient .rgb;
		vec3 Idiff = gl_LightSource      [ i ].diffuse .rgb *      max( dot( N , L ) , 0.0 );
		vec3 Ispec = gl_FrontLightProduct[ i ].specular.rgb * pow( max( dot( R , E ) , 0.0 ) , 0.3 * gl_FrontMaterial.shininess );

		/*
		 * For diffuse color, gl_FrontLightProduct.diffuse is unsuitable,
		 * because it includes the material's diffuse color, which is also
		 * included in 'color' (multiplied with 'Idiff' below.)
		 */

		float dist = length( distVector );
		float attenuation = 1.0 / (
			gl_LightSource[ i ].constantAttenuation  +
			gl_LightSource[ i ].linearAttenuation    * dist +
			gl_LightSource[ i ].quadraticAttenuation * dist * dist );

		material += Iamb + color.rgb * Idiff * attenuation;
		specular += Ispec * attenuation;
	}
	return vec4( gl_FrontLightModelProduct.sceneColor.rgb + material + specular , color.a );
}
