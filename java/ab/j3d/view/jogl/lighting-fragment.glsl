// To enable multi-pass lighting: #define MULTIPASS_LIGHTING
varying vec3 vertex;
varying vec3 normal;

uniform samplerCube reflectionMap;
uniform float reflectionMin;
uniform float reflectionMax;
uniform vec3 reflectionColor;

/*
 * Per-pixel lighting and reflection.
 *
 * Based on source from: http://www.clockworkcoders.com/oglsl/tutorial5.htm
 * See also: http://www.gamedev.net/reference/articles/article2428.asp
 */

vec4 frontLighting( in vec4 color );

float shadow();

vec4 lighting( in vec4 color )
{
	vec4 result = frontLighting( color );

	if ( ( reflectionMin > 0.0 ) || ( reflectionMax > 0.0 ) )
	{
		vec3 N = normalize( normal );
		vec3 E = normalize( -vertex );
		vec3 R = ( gl_TextureMatrix[ 2 ] * vec4( -reflect( E , N ) , 1 ) ).xyz;
		float reflectivity = reflectionMin + ( reflectionMax - reflectionMin ) * max( 1.0 - dot( N , E ) , 0.0 );
		vec3 reflection = reflectionColor * textureCube( reflectionMap , R ).rgb;
		result.rgb += reflectivity * reflection;
	}

	return result;
}

/*
 * Lighting for front-facing fragments.
 */
vec4 frontLighting( in vec4 color )
{
	vec3 result = vec3( 0.0 , 0.0 , 0.0 );

	vec3 N = normalize( normal );
	vec3 E = normalize( -vertex );

#ifdef MULTIPASS_LIGHTING
	const int i = 0;
#else
	const int lightCount = 3;
	for ( int i = 0; i < lightCount; i++ )
#endif
	{
		/*
		 * Note on 'gl_LightSource' vs 'gl_FrontLightProduct':
		 * For diffuse color, 'gl_FrontLightProduct.diffuse' is unsuitable,
		 * because it includes the material's diffuse color, which is also
		 * included in 'color' (multiplied with 'diffuse' below.)
		 */
		if ( gl_LightSource[ i ].position.w == 0.0 )
		{
			/*
			 * Directional light.
			 */
			vec3 L = normalize( gl_LightSource[ i ].position.xyz );
			vec3 H = gl_LightSource[ i ].halfVector.xyz;
			vec3 ambient  = gl_FrontLightProduct[ i ].ambient .rgb;
			vec3 diffuse  = gl_LightSource      [ i ].diffuse .rgb *      max( dot( N , L ) , 0.0 );
			vec3 specular = gl_FrontLightProduct[ i ].specular.rgb * pow( max( dot( N , H ) , 0.0 ) , gl_FrontMaterial.shininess );

			result += ambient + ( color.rgb * diffuse + specular ) * shadow();
		}
		else
		{
			vec3 D = gl_LightSource[ i ].position.xyz - vertex;
			vec3 L = normalize( D );
			vec3 R = normalize( -reflect( L , N ) );

			float dist = length( D );
			float attenuation = 1.0 / (
				gl_LightSource[ i ].constantAttenuation  +
				gl_LightSource[ i ].linearAttenuation    * dist +
				gl_LightSource[ i ].quadraticAttenuation * dist * dist );

			vec3 ambient = gl_FrontLightProduct[ i ].ambient.rgb;
			vec3 diffuse;
			vec3 specular;

			if ( gl_LightSource[ i ].spotCutoff == 180.0 )
			{
				/*
				 * Point light.
				 */
				diffuse  = gl_LightSource      [ i ].diffuse .rgb * attenuation *      max( dot( N , L ) , 0.0 );
				specular = gl_FrontLightProduct[ i ].specular.rgb * attenuation * pow( max( dot( R , E ) , 0.0 ) , gl_FrontMaterial.shininess );
			}
			else
			{
				float spotEffect = dot( normalize( gl_LightSource[ i ].spotDirection ) , -L );
				if ( spotEffect > gl_LightSource[ i ].spotCosCutoff )
				{
					float correctedSpotEffect = ( spotEffect - gl_LightSource[ i ].spotCosCutoff ) /
					                            ( 1.0        - gl_LightSource[ i ].spotCosCutoff );

					attenuation *= pow( correctedSpotEffect, gl_LightSource[ i ].spotExponent );

					diffuse   = gl_LightSource      [ i ].diffuse .rgb * attenuation *      max( dot( N , L ) , 0.0 );
					specular  = gl_FrontLightProduct[ i ].specular.rgb * attenuation * pow( max( dot( R , E ) , 0.0 ) , gl_FrontMaterial.shininess );
				}
				else
				{
					diffuse   = vec3( 0.0 );
					specular  = vec3( 0.0 );
				}
			}

			result += ambient + ( color.rgb * diffuse + specular ) * shadow();
		}
	}

	return vec4( color.rgb * gl_FrontLightModelProduct.sceneColor.rgb + result.rgb , color.a );
}
