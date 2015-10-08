$input a_position, a_texcoord0, a_color
$output v_texcoord0, v_common2

/*
 * Copyright 2011-2015 Branimir Karadzic. All rights reserved.
 * License: http://www.opensource.org/licenses/BSD-2-Clause
 */

#include "common.sh"

void main()
{
	gl_Position = mul(u_viewProj, vec4(a_position.xy, 0.0, 1.0) );
	v_texcoord0 = a_texcoord0;
	v_common2 = a_color;
}