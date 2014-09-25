// Pixel shader to generate the Depth Map
// Used for shadow mapping - generates depth map from the light's viewpoint
precision highp float;

varying vec4 vPosition; 

// from Fabien Sangalard's DEngine 
vec4 pack (float depth)
{
	const vec4 bitSh = vec4(256.0 * 256.0 * 256.0,
							256.0 * 256.0,
							256.0,
							1.0);
	const vec4 bitMsk = vec4(0,
							 1.0 / 256.0,
							 1.0 / 256.0,
							 1.0 / 256.0);
	vec4 comp = fract(depth * bitSh);
	comp -= comp.xxyz * bitMsk;
	return comp;
}

void main() {
	// the depth
	float normalizedDistance  = vPosition.z / vPosition.w;
	// scale -1.0;1.0 to 0.0;1.0 
	normalizedDistance = (normalizedDistance + 1.0) / 2.0;

	// pack value into 32-bit RGBA texture
	gl_FragColor = pack(normalizedDistance);
}