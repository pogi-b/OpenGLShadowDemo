package com.projectI.openglshadowdemo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.projectI.openglshadowdemo.common.RenderConstants;

import android.opengl.GLES20;

public class Plane {
	private final FloatBuffer planePosition;
	private final FloatBuffer planeNormal;
	private final FloatBuffer planeColor;
	
	//TODO: remove
	int translateY = 0;
	int translateZ = 0;

	float[] planePositionData = {
			// X, Y, Z, 
			-25.0f, -5, -25.0f + translateZ,				
			-25.0f, -5, 25.0f + translateZ, 
			25.0f, -5, -25.0f + translateZ, 
			-25.0f, -5, 25.0f + translateZ, 				
			25.0f, -5, 25.0f + translateZ, 
			25.0f, -5, -25.0f + translateZ
			};

	float[] planeNormalData = {
			// nX, nY, nZ
			0.0f, 1.0f, 0.0f,			
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,				
			0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f
			};
			
	float[] planeColorData = {
			// R, G, B, A
			0.5f, 0.5f, 0.5f, 1.0f,
			0.5f, 0.5f, 0.5f, 1.0f,
			0.5f, 0.5f, 0.5f, 1.0f,
			0.5f, 0.5f, 0.5f, 1.0f,
			0.5f, 0.5f, 0.5f, 1.0f,
			0.5f, 0.5f, 0.5f, 1.0f
		};
	
	public Plane() {
		// Buffer initialization
		ByteBuffer bPos = ByteBuffer.allocateDirect(planePositionData.length * RenderConstants.FLOAT_SIZE_IN_BYTES);
		bPos.order(ByteOrder.nativeOrder());
		planePosition = bPos.asFloatBuffer();
		
		ByteBuffer bNormal = ByteBuffer.allocateDirect(planeNormalData.length * RenderConstants.FLOAT_SIZE_IN_BYTES);
		bNormal.order(ByteOrder.nativeOrder());
		planeNormal = bNormal.asFloatBuffer();
		
		ByteBuffer bColor = ByteBuffer.allocateDirect(planeColorData.length * RenderConstants.FLOAT_SIZE_IN_BYTES);
		bColor.order(ByteOrder.nativeOrder());
		planeColor = bColor.asFloatBuffer();
					
		planePosition.put(planePositionData).position(0);
		planeNormal.put(planeNormalData).position(0);
		planeColor.put(planeColorData).position(0);
	}
	
	public void render(int positionAttribute, int normalAttribute, int colorAttribute, boolean onlyPosition) {
		
		// Pass position information to shader
		planePosition.position(0);		
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false,
        		0, planePosition);        
                
        GLES20.glEnableVertexAttribArray(positionAttribute);                       
        
        if (!onlyPosition)
        {
        	// Pass normal information to shader
	        planeNormal.position(0);
	        GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 
	        		 0, planeNormal);
	        
	        GLES20.glEnableVertexAttribArray(normalAttribute);  
	        
	     // Pass color information to shader
	        planeColor.position(0);
	        GLES20.glVertexAttribPointer(colorAttribute, 4, GLES20.GL_FLOAT, false, 
	        		0, planeColor);
	        
	        GLES20.glEnableVertexAttribArray(colorAttribute);  
        }
        
        // Draw the plane
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6); 
	}
}