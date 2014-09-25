// Vertex shader to generate the Depth Map
// Used for shadow mapping - generates depth map from the light's viewpoint
precision highp float;

// model-view projection matrix
uniform mat4 uMVPMatrix;

// position of the vertices
attribute vec4 aShadowPosition; 

void main() {
	gl_Position = uMVPMatrix * aShadowPosition; 
}