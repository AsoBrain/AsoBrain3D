const int lightCount = 3;

varying vec3 vertex;
varying vec3 normal;

/*
 * Per-pixel lighting.
 *
 * Based on source from: http://www.clockworkcoders.com/oglsl/tutorial5.htm
 * See also: http://www.gamedev.net/reference/articles/article2428.asp
 */

vec4 frontLighting( in vec4 color );
vec4 backLighting( in vec4 color );

vec4 lighting( in vec4 color )
{
    return frontLighting( color );
}

/*
 * Lighting for front-facing fragments.
 */
vec4 frontLighting( in vec4 color )
{
	vec3 result = vec3( 0.0 , 0.0 , 0.0 );

	vec3 N = normalize( normal );
	vec3 E = normalize( -vertex );

	for ( int i = 0 ; i < lightCount ; i++ )
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

			result += ambient + color.rgb * diffuse + specular;
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
					attenuation *= pow( spotEffect , gl_LightSource[ i ].spotExponent );

					diffuse   = gl_LightSource      [ i ].diffuse .rgb * attenuation *      max( dot( N , L ) , 0.0 );
					specular  = gl_FrontLightProduct[ i ].specular.rgb * attenuation * pow( max( dot( R , E ) , 0.0 ) , gl_FrontMaterial.shininess );
				}
				else
				{
					diffuse   = vec3( 0.0 );
					specular  = vec3( 0.0 );
				}
			}

			result += ambient + color.rgb * diffuse + specular;
		}
	}

	return vec4( color.rgb * gl_FrontLightModelProduct.sceneColor.rgb + result.rgb , color.a );
}

/*
 * Lighting for back-facing fragments.
 */
vec4 backLighting( in vec4 color )
{
	vec3 result = vec3( 0.0 , 0.0 , 0.0 );

	vec3 N = -normalize( normal );
	vec3 E = normalize( -vertex );

	for ( int i = 0 ; i < lightCount ; i++ )
	{
		/*
		 * Note on 'gl_LightSource' vs 'gl_BackLightProduct':
		 * For diffuse color, 'gl_BackLightProduct.diffuse' is unsuitable,
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
			vec3 ambient  = gl_BackLightProduct[ i ].ambient .rgb;
			vec3 diffuse  = gl_LightSource     [ i ].diffuse .rgb *      max( dot( N , L ) , 0.0 );
			vec3 specular = gl_BackLightProduct[ i ].specular.rgb * pow( max( dot( N , H ) , 0.0 ) , gl_BackMaterial.shininess );

			result += ambient + color.rgb * diffuse + specular;
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

			vec3 ambient = gl_BackLightProduct[ i ].ambient.rgb;
			vec3 diffuse;
			vec3 specular;

			if ( gl_LightSource[ i ].spotCutoff == 180.0 )
			{
				/*
				 * Point light.
				 */
				diffuse  = gl_LightSource     [ i ].diffuse .rgb * attenuation *      max( dot( N , L ) , 0.0 );
				specular = gl_BackLightProduct[ i ].specular.rgb * attenuation * pow( max( dot( R , E ) , 0.0 ) , gl_BackMaterial.shininess );
			}
			else
			{
				float spotEffect = dot( normalize( gl_LightSource[ i ].spotDirection ) , -L );
				if ( spotEffect > gl_LightSource[ i ].spotCosCutoff )
				{
					attenuation *= pow( spotEffect , gl_LightSource[ i ].spotExponent );

					diffuse   = gl_LightSource     [ i ].diffuse .rgb * attenuation *      max( dot( N , L ) , 0.0 );
					specular  = gl_BackLightProduct[ i ].specular.rgb * attenuation * pow( max( dot( R , E ) , 0.0 ) , gl_BackMaterial.shininess );
				}
				else
				{
					diffuse   = vec3( 0.0 );
					specular  = vec3( 0.0 );
				}
			}

			result += ambient + color.rgb * diffuse + specular;
		}
	}

	return vec4( color.rgb * gl_BackLightModelProduct.sceneColor.rgb + result.rgb , color.a );
}
