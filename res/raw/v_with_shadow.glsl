// Based on http://blog.shayanjaved.com/2011/03/13/shaders-android/
// from Shayan Javed

uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uNormalMatrix;

// the shadow projection matrix
uniform mat4 uShadowProjMatrix;	

// position and normal of the vertices
attribute vec4 aPosition;
attribute vec4 aColor;
attribute vec3 aNormal;

// to pass on
varying vec3 vPosition;      		
varying vec4 vColor;          		
varying vec3 vNormal;
varying vec4 vShadowCoord;


void main() {
	// the vertex position in camera space
	vPosition = vec3(uMVMatrix * aPosition); 

	// the vertex color
	vColor = aColor;
	
	// the vertex normal coordinate in camera space
	vNormal = vec3(uNormalMatrix * vec4(aNormal, 0.0));
	
	vShadowCoord = uShadowProjMatrix * aPosition;
	
	gl_Position = uMVPMatrix * aPosition;                     
}