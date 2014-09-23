// Based on http://blog.shayanjaved.com/2011/03/13/shaders-android/
// from Shayan Javed
// And dEngine source from Fabien Sanglard

precision mediump float;

// The position of the light in eye space.
uniform vec3 uLightPos;       	
  
// Texture variables: depth texture
uniform sampler2D uShadowTexture;

// This define the value to move one pixel left or right
uniform float uxPixelOffset;
// This define the value to move one pixel up or down
uniform float uyPixelOffset;		
  
// from vertex shader - values get interpolated
varying vec3 vPosition;
varying vec4 vColor;
varying vec3 vNormal;
  
// shadow coordinates
varying vec4 vShadowCoord;

float lookup( vec2 offSet) 
{ 
	vec4 shadowMapPosition = vShadowCoord / vShadowCoord.w;

	float distanceFromLight = texture2D(uShadowTexture, (shadowMapPosition + 
	                               vec4(offSet.x * uxPixelOffset, offSet.y * uyPixelOffset, 0.05, 0.0)).st ).z;
			
	//add bias to reduce shadow acne (error margin)
	float bias = 0.0005;

	return float(distanceFromLight > shadowMapPosition.z - bias);
}

float shadowPCF()
{
	float shadow = 1.0;

	for (float y = -1.5; y <= 1.5; y = y + 1.0) {
		for (float x = -1.5; x <= 1.5; x = x + 1.0) {
			shadow += lookup(vec2(x,y));
		}
	}
		
	shadow /= 16.0;
	shadow += 0.2;
	
	return shadow;
}
  
void main()                    		
{        
	vec3 lightVec = uLightPos - vPosition;
	lightVec = normalize(lightVec);
   	
   	// Phong shading with diffuse and ambient component
	float diffuseComponent = max(0.0,dot(lightVec, vNormal) );
	float ambientComponent = 0.3;
 		
 	// Shadow
   	float shadow = 1.0;
	
	//if the fragment is not behind light view frustum
	if (vShadowCoord.w > 0.0) {
			
		shadow = shadowPCF();
		
		//scale 0.0-1.0 to 0.2-1.0
		//otherways everything in shadow would be black
		shadow = (shadow * 0.8) + 0.2;
	}

	// Final output color with shadow and lighting
    gl_FragColor = (vColor * (diffuseComponent + ambientComponent) * shadow);                                  		
}                                                                     	
