$input v_wpos, v_texcoord0, v_view, v_pos_radius, v_color_attn, v_dir_fov, v_specular // in...

#include "common.sh"

SAMPLER2D(u_gbuffer0, 15);
SAMPLER2D(u_gbuffer1, 14);
SAMPLER2D(u_gbuffer2, 13);
SAMPLER2D(u_gbuffer_depth, 12);
#ifdef HAS_SHADOWMAP
	SAMPLER2D(u_texShadowmap, 11);
	uniform mat4 u_shadowmapMatrices[4];
#endif
	
uniform vec4 u_fogColorDensity; 
uniform vec4 u_fogParams;


vec3 calcLight(vec4 dirFov
	, vec3 _wpos
	, vec3 _normal
	, vec3 _view
	, vec2 uv
	, vec3 light_pos
	, float light_radius
	, vec3 light_color
	, float attn_param
	, vec3 light_specular)
{
	vec3 lp = light_pos.xyz - _wpos;
	float dist = length(lp);
	float attn = pow(max(0, 1 - dist / light_radius), attn_param);
	
	vec3 toLightDir = normalize(lp);
	
	if(dirFov.w < 3.14159)
	{
		float cosDir = dot(normalize(dirFov.xyz), normalize(-toLightDir));
		float cosCone = cos(dirFov.w * 0.5);
	
		if(cosDir < cosCone)
			discard;
		attn *= (cosDir - cosCone) / (1 - cosCone);
	}

	vec4 materialSpecularShininess = vec4(1, 1, 1, 4); // todo use uniform
	vec2 lc = lit(toLightDir, _normal, _view, materialSpecularShininess.w);
	vec3 rgb = 
		attn * (light_color * saturate(lc.x) 
		+ light_specular.xyz * materialSpecularShininess.xyz *
		#ifdef SPECULAR_TEXTURE
			texture2D(u_texSpecular, uv).rgb * 
		#endif
		saturate(lc.y));
	return rgb;
}


void main()
{
	vec3 screen_coord = getScreenCoord(v_wpos);
	
	vec3 normal = texture2D(u_gbuffer1, screen_coord.xy * 0.5 + 0.5).xyz * 2 - 1;
	vec4 color = texture2D(u_gbuffer0, screen_coord.xy * 0.5 + 0.5);

	vec3 wpos = getViewPosition(u_gbuffer_depth, u_camInvViewProj, screen_coord.xy * 0.5 + 0.5);
	
	float ndotl = -dot(normal, v_dir_fov.xyz);
	vec3 view = normalize(v_view);
	vec3 diffuse = color.rgb * calcLight(v_dir_fov
		, wpos
		, normal
		, view
		, screen_coord.xy
		, v_pos_radius.xyz
		, v_pos_radius.w
		, v_color_attn.xyz
		, v_color_attn.w
		, v_specular.xyz); 
	#ifdef HAS_SHADOWMAP
		diffuse = diffuse * pointLightShadow(u_texShadowmap, u_shadowmapMatrices, vec4(wpos, 1.0), v_dir_fov.w); 
	#endif

		
	gl_FragColor.xyz = diffuse;
	gl_FragColor.w = 1;
}
